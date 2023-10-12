/*
 * Copyright 2010-2021 Australian Signals Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package au.gov.asd.tac.constellation.views.analyticview;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersPane;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import au.gov.asd.tac.constellation.views.analyticview.aggregators.AnalyticAggregator;
import au.gov.asd.tac.constellation.views.analyticview.analytics.AnalyticInfo;
import au.gov.asd.tac.constellation.views.analyticview.analytics.AnalyticPlugin;
import au.gov.asd.tac.constellation.views.analyticview.questions.AnalyticQuestion;
import au.gov.asd.tac.constellation.views.analyticview.questions.AnalyticQuestionDescription;
import au.gov.asd.tac.constellation.views.analyticview.results.AnalyticResult;
import au.gov.asd.tac.constellation.views.analyticview.state.AnalyticViewConcept;
import au.gov.asd.tac.constellation.views.analyticview.state.AnalyticViewState;
import au.gov.asd.tac.constellation.views.analyticview.utilities.AnalyticException;
import au.gov.asd.tac.constellation.views.analyticview.utilities.AnalyticUtilities;
import com.github.rjeschke.txtmark.Processor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import org.openide.util.Lookup;

/**
 * The pane holding gui elements related to configuration of an analytic question.
 *
 * @author cygnus_x-1
 */
public class AnalyticConfigurationPane extends VBox {

    private static final Logger LOGGER = Logger.getLogger(AnalyticConfigurationPane.class.getName());

    public static final String AGGREGATOR_PARAMETER_ID = PluginParameter.buildId(AnalyticConfigurationPane.class, "aggregator");
    private static final String GLOBAL_PARAMS_GROUP = "Question Parameters";

    private static final List<SelectableAnalyticPlugin> SELECTABLE_PLUGINS = new ArrayList<>();
    private static final Map<Plugin, SelectableAnalyticPlugin> PLUGIN_TO_SELECTABLE_PLUGIN_MAP = new HashMap<>();
    private static final Map<String, SelectableAnalyticPlugin> NAME_TO_SELECTABLE_PLUGIN_MAP = new HashMap<>();

    private final GridPane analyticSelectionPane;
    private final VBox categoryAndQuestionSelectionPane;
    private final TitledPane categoryListPane;
    private final ListView<String> categoryList;
    private final TitledPane questionListPane;
    private final ListView<AnalyticQuestionDescription<?>> questionList;
    private final TitledPane pluginListPane;
    private final ListView<SelectableAnalyticPlugin> pluginList;
    private final TitledPane informationPane;
    private final TabPane informationTabPane;
    private final Tab parametersTab;
    private final Tab documentationTab;
    private WebView documentationView;

    private static boolean stateChanged = false;
    private static boolean selectionSuppressed = false;
    // Only accessed on the FX application thread to remain thread-safe
    private AnalyticQuestionDescription<?> currentQuestion = null;
    private final Map<String, List<SelectableAnalyticPlugin>> categoryToPluginsMap;
    private final Map<AnalyticQuestionDescription<?>, List<SelectableAnalyticPlugin>> questionToPluginsMap;
    private final PluginParameters globalAnalyticParameters = new PluginParameters();

    public AnalyticConfigurationPane() {

        // create the parameters needed for all analytic questions
        createGlobalParameters();

        // set up the list of available analytic plugins
        Lookup.getDefault().lookupAll(AnalyticPlugin.class).forEach(plugin -> {
            final SelectableAnalyticPlugin selectablePlugin = new SelectableAnalyticPlugin(plugin);
            SELECTABLE_PLUGINS.add(selectablePlugin);
            PLUGIN_TO_SELECTABLE_PLUGIN_MAP.put(plugin, selectablePlugin);
            NAME_TO_SELECTABLE_PLUGIN_MAP.put(plugin.getName(), selectablePlugin);
        });
        Collections.sort(SELECTABLE_PLUGINS, (selectablePlugin1, selectablePlugin2)
                -> selectablePlugin1.plugin.getName().compareToIgnoreCase(selectablePlugin2.plugin.getName()));

        // build the pane which allows selection of analytics
        this.analyticSelectionPane = new GridPane();
        analyticSelectionPane.prefWidthProperty().bind(this.widthProperty());
        analyticSelectionPane.minHeightProperty().bind(this.heightProperty().multiply(0.3));

        // build the pane holding the category and question panes
        this.categoryAndQuestionSelectionPane = new VBox();

        // set up the list of analytic categories
        this.categoryToPluginsMap = new HashMap<>();
        SELECTABLE_PLUGINS.forEach(selectablePlugin -> {
            if (selectablePlugin.getPlugin().isVisible()) {
                final String categoryName = selectablePlugin.plugin.getClass().getAnnotation(AnalyticInfo.class).analyticCategory();
                final List<SelectableAnalyticPlugin> categoryPlugins;
                if (!categoryToPluginsMap.containsKey(categoryName)) {
                    categoryPlugins = new ArrayList<>();
                    categoryToPluginsMap.put(categoryName, categoryPlugins);
                } else {
                    categoryPlugins = categoryToPluginsMap.get(categoryName);
                }
                categoryPlugins.add(selectablePlugin);
            }
        });
        assert categoryToPluginsMap.values().stream()
                .allMatch(plugins -> plugins.stream()
                .map(plugin -> plugin.getPlugin().getResultType())
                .collect(Collectors.toSet()).size() <= 1) :
                "A category should be populated only with analytics of the same result type.";
        this.categoryList = new ListView<>();
        final List<String> categories = new ArrayList<>(categoryToPluginsMap.keySet());
        Collections.sort(categories, String::compareToIgnoreCase);
        categoryList.getItems().addAll(categories);
        categoryList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            currentQuestion = null;
            populateParameterPane(globalAnalyticParameters);
            setPluginsFromSelectedCategory();
        });

        // build the pane holding the list of analytic categories
        this.categoryListPane = new TitledPane("Categories", categoryList);
        categoryListPane.setExpanded(false);

        // set up the list of analytic questions
        this.questionToPluginsMap = new HashMap<>();
        AnalyticUtilities.getAnalyticQuestionDescriptions().forEach(question -> {
            final List<Class> questionPluginClasses = question.getPluginClasses();
            SELECTABLE_PLUGINS.forEach(selectablePlugin -> {
                if (questionPluginClasses.contains(selectablePlugin.plugin.getClass())) {
                    final List<SelectableAnalyticPlugin> questionPlugins;
                    if (!questionToPluginsMap.containsKey(question)) {
                        questionPlugins = new ArrayList<>();
                        questionToPluginsMap.put(question, questionPlugins);
                    } else {
                        questionPlugins = questionToPluginsMap.get(question);
                    }
                    if (selectablePlugin.getPlugin().isVisible()) {
                        questionPlugins.add(selectablePlugin);
                    }
                }
            });
        });
        this.questionList = new ListView<>();
        final List<AnalyticQuestionDescription<?>> questions = new ArrayList<>(questionToPluginsMap.keySet());
        Collections.sort(questions, (question1, question2) -> question1.getName().compareToIgnoreCase(question2.getName()));
        questionList.getItems().addAll(questions);
        questionList.setCellFactory(list -> new ListCell<AnalyticQuestionDescription<?>>() {
            @Override
            protected void updateItem(final AnalyticQuestionDescription<?> item, final boolean empty) {
                super.updateItem(item, empty);
                setText(item == null ? "" : item.getName());
            }
        });
        questionList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            currentQuestion = newValue;
            @SuppressWarnings("unchecked") //AGGREGATOR_PARAMETER_ID is always a SingleChoiceParameter
            final PluginParameter<SingleChoiceParameterValue> aggregator = (PluginParameter<SingleChoiceParameterValue>) globalAnalyticParameters.getParameters().get(AGGREGATOR_PARAMETER_ID);
            SingleChoiceParameterType.getOptionsData(aggregator).forEach(aggregatorParameterValue -> {
                if (((AnalyticAggregatorParameterValue) aggregatorParameterValue).getObjectValue().getClass().equals(currentQuestion.getAggregatorType())) {
                    SingleChoiceParameterType.setChoiceData(aggregator, (AnalyticAggregatorParameterValue) aggregatorParameterValue);
                }
            });
            populateParameterPane(globalAnalyticParameters);
            setPluginsFromSelectedQuestion();
        });

        // build the pane holding the list of analytic questions
        this.questionListPane = new TitledPane("Questions", questionList);

        // ensure that only one of either the category or question pane are expanded at any time
        categoryListPane.expandedProperty().addListener((observable, oldValue, newValue) -> {

            questionListPane.setExpanded(!categoryListPane.isExpanded());
            if (categoryListPane.isExpanded()) {
                currentQuestion = null;
                populateParameterPane(globalAnalyticParameters);
                setPluginsFromSelectedCategory();
            }

        });
        questionListPane.expandedProperty().addListener((observable, oldValue, newValue) -> {
            categoryListPane.setExpanded(!questionListPane.isExpanded());
            if (questionListPane.isExpanded()) {
                currentQuestion = questionList.getSelectionModel().getSelectedItem();
                populateParameterPane(globalAnalyticParameters);
                setPluginsFromSelectedQuestion();
            }
        });

        // populate the category and question pane
        categoryAndQuestionSelectionPane.getChildren().addAll(categoryListPane, questionListPane);

        // set up the list of analytic plugins
        this.pluginList = new ListView<>();
        pluginList.setCellFactory(selectableAnalytics -> new ListCell<SelectableAnalyticPlugin>() {
            @Override
            protected void updateItem(final SelectableAnalyticPlugin item, final boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    item.setParent(this);
                    setGraphic(item.checkbox);
                    setText(item.plugin.getName());
                }
            }
        });

        pluginList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!selectionSuppressed) {
                if (newValue != null) {
                    populateDocumentationPane(newValue);
                    populateParameterPane(newValue.getAllParameters());
                    // only update state when the categories are expanded
                    if (categoryListPane.isExpanded()) {
                        updateState(true);
                    }
                } else {
                    populateDocumentationPane(null);
                    populateParameterPane(globalAnalyticParameters);
                }
            }
        });

        // build the pane holding the list of analytic plugins
        this.pluginListPane = new TitledPane("Analytics", pluginList);
        pluginListPane.prefHeightProperty().bind(categoryAndQuestionSelectionPane.heightProperty());
        pluginListPane.setCollapsible(false);

        // populate the analytic selection pane
        analyticSelectionPane.addRow(0, categoryAndQuestionSelectionPane, pluginListPane);
        final ColumnConstraints categoryAndQuestionColumnConstraint = new ColumnConstraints();
        categoryAndQuestionColumnConstraint.setPercentWidth(50);
        final ColumnConstraints pluginAndDocumentationColumnConstraint = new ColumnConstraints();
        pluginAndDocumentationColumnConstraint.setPercentWidth(50);
        analyticSelectionPane.getColumnConstraints().addAll(categoryAndQuestionColumnConstraint, pluginAndDocumentationColumnConstraint);

        // build the pane holding parameters for the analytics
        this.parametersTab = new Tab("Parameters", null);
        parametersTab.setClosable(false);

        // build the pane holding documentation for the analytics
        this.documentationTab = new Tab("Documentation", null);
        documentationTab.setClosable(false);

        // build the documentation webview on the javafx thread
        final CountDownLatch cdl = new CountDownLatch(1);
        Platform.runLater(() -> {
            this.documentationView = new WebView();
            if (JavafxStyleManager.isDarkTheme()) {
                documentationView.getEngine().setUserStyleSheetLocation(getClass().getResource("resources/analytic-view-dark.css").toExternalForm());
            }        
            populateDocumentationPane(null);
            cdl.countDown();
        });

        // populate the documentation pane
        try {
            cdl.await();
            final VBox docBox = new VBox();
            docBox.setPadding(new Insets(5, 5, 5, 5));
            docBox.getChildren().add(documentationView);
            documentationTab.setContent(docBox);
        } catch (final InterruptedException ex) {
            this.documentationView = null;
            documentationTab.setContent(null);
            Thread.currentThread().interrupt();
        }

        // build the pane holding the parameters and documentation tabs
        this.informationTabPane = new TabPane(parametersTab, documentationTab);
        informationTabPane.setSide(Side.LEFT);
        informationTabPane.prefWidthProperty().bind(this.widthProperty());

        // build the pane providing information about the current analytic
        this.informationPane = new TitledPane("Information", informationTabPane);
        informationPane.setCollapsible(false);

        // populate the analytic configuration pane
        this.getChildren().addAll(analyticSelectionPane, informationPane);

        // set an initial state for the analytic configuration pane
        reset();
    }

    /**
     * Reset to the initial state for the analytic configuration pane. The question list will be expanded, the first question selected, and the analytics list populated based on the selected question.
     */
    protected final void reset() {
        Platform.runLater(() -> {
            categoryList.getSelectionModel().select(0);
            questionList.getSelectionModel().select(0);
            categoryListPane.setExpanded(false);
            questionListPane.setExpanded(true);
        });
    }

    /**
     * Get the question which is currently selected in this pane.
     *
     * @return the current {@link AnalyticQuestionDescription}.
     */
    protected final AnalyticQuestionDescription<?> getCurrentQuestion() {
        return currentQuestion;
    }

    /**
     * Answer the question which is currently selected in this pane.
     *
     * @return the answered {@link AnalyticQuestion}.
     */
    protected final AnalyticQuestion<?> answerCurrentQuestion() throws AnalyticException {

        // build question
        final AnalyticQuestion<?> question = new AnalyticQuestion<>(currentQuestion);

        // retrieve and set any global parameters in the question
        final AnalyticAggregatorParameterValue aggregatorParameterValue = (AnalyticAggregatorParameterValue) globalAnalyticParameters.getSingleChoice(AGGREGATOR_PARAMETER_ID);
        if (aggregatorParameterValue == null) {
            throw new AnalyticException("You must select an aggregation method!");
        }
        final AnalyticAggregator aggregator = (AnalyticAggregator<?>) aggregatorParameterValue.getObjectValue();
        question.setAggregator(aggregator);

        // add and set parameters for each plugin in the question
        final List<SelectableAnalyticPlugin> selectedPlugins = new ArrayList<>();
        pluginList.getItems().forEach(selectablePlugin -> {
            if (selectablePlugin.checkbox.isSelected()) {
                selectedPlugins.add(selectablePlugin);
                question.addPlugin(selectablePlugin.plugin, selectablePlugin.getPluginSpecificParameters());
            }
        });
        if (selectedPlugins.isEmpty()) {
            throw new AnalyticException("You must select at least one analytic!");
        }

        final Graph currentGraph = GraphManager.getDefault().getActiveGraph();

        // update the analytic view state
        PluginExecution.withPlugin(new AnalyticViewStateWriter(currentQuestion, selectedPlugins)).executeLater(currentGraph);

        // answer the question
        return question.answer(currentGraph);
    }

    private void populateDocumentationPane(final SelectableAnalyticPlugin plugin) {
        if (documentationView != null) {
            if (plugin == null || plugin.getPlugin() == null || plugin.getPlugin().getDocumentationUrl() == null) {
                documentationView.getEngine().loadContent("<html>No Documentation Available</html>", "text/html");
            } else {
                try {
                    final Path path = Paths.get(plugin.getPlugin().getDocumentationUrl());
                    final InputStream pageInput = new FileInputStream(path.toString());
                    documentationView.getEngine().loadContent(Processor.process(pageInput), "text/html");
                } catch (final IOException ex) {
                    LOGGER.log(Level.WARNING, ex.getMessage());
                }
            }
        }
    }

    /**
     * Updates the AnalyticViewState by running a plugin to save the graph state
     *
     * @param pluginWasSelected true if the triggered update was from a plugin being selected
     */
    protected void updateState(final boolean pluginWasSelected) {
        stateChanged = true;
        PluginExecution.withPlugin(new AnalyticViewStateUpdater(this, pluginWasSelected)).executeLater(GraphManager.getDefault().getActiveGraph());
    }

    /**
     * Saves the state of the graph by fetching all currently selected plugins and updating the state only when the state has been changed
     */
    protected void saveState() {
        if (stateChanged) {
            stateChanged = false;
            if (categoryListPane.isExpanded()) {
                final List<SelectableAnalyticPlugin> selectedPlugins = new ArrayList<>();
                pluginList.getItems().forEach(selectablePlugin -> {
                    if (selectablePlugin.isSelected()) {
                        selectedPlugins.add(selectablePlugin);
                    }
                });
                PluginExecution.withPlugin(new AnalyticViewStateWriter(currentQuestion, selectedPlugins)).executeLater(GraphManager.getDefault().getActiveGraph());
            }
        }
    }

    private void createGlobalParameters() {
        globalAnalyticParameters.addGroup(GLOBAL_PARAMS_GROUP, new PluginParametersPane.TitledSeparatedParameterLayout(GLOBAL_PARAMS_GROUP, 14, false));

        final PluginParameter<SingleChoiceParameterValue> aggregatorParameter = SingleChoiceParameterType.build(AGGREGATOR_PARAMETER_ID, AnalyticAggregatorParameterValue.class);
        aggregatorParameter.setName("Aggregation Method");
        aggregatorParameter.setDescription("The method used to aggregate the results from multiple analytics");
        SingleChoiceParameterType.setOptionsData(aggregatorParameter, new ArrayList<>());
        globalAnalyticParameters.addParameter(aggregatorParameter, GLOBAL_PARAMS_GROUP);
    }

    private void updateGlobalParameters() {
        @SuppressWarnings("unchecked")
        final PluginParameter<SingleChoiceParameterValue> aggregatorParameter = (PluginParameter<SingleChoiceParameterValue>) globalAnalyticParameters.getParameters().get(AGGREGATOR_PARAMETER_ID);
        final List<AnalyticAggregatorParameterValue> aggregators = new ArrayList<>();
        if (categoryListPane.isExpanded()) {
            @SuppressWarnings("unchecked") //return type of getResultType is actually Class<? extends AnalyticResult<?>>
            final Class<? extends AnalyticResult<?>> pluginResultType = pluginList.getItems().get(0).getPlugin().getResultType();
            AnalyticUtilities.lookupAnalyticAggregators(pluginResultType)
                    .forEach(aggregator -> aggregators.add(new AnalyticAggregatorParameterValue(aggregator)));
            SingleChoiceParameterType.setOptionsData(aggregatorParameter, aggregators);
            SingleChoiceParameterType.setChoiceData(aggregatorParameter, aggregators.get(0));
        } else if (questionListPane.isExpanded() && currentQuestion != null) {
            final Class<? extends AnalyticAggregator<?>> questionAggregatorType = currentQuestion.getAggregatorType();
            aggregators.add(new AnalyticAggregatorParameterValue(AnalyticUtilities.lookupAnalyticAggregator(questionAggregatorType)));
            SingleChoiceParameterType.setOptionsData(aggregatorParameter, aggregators);
            SingleChoiceParameterType.setChoiceData(aggregatorParameter, aggregators.get(0));
        }
        pluginList.getItems().forEach(selectablePlugin
                -> selectablePlugin.setUpdatedParameter(aggregatorParameter.getId(), aggregatorParameter.getStringValue()));
    }

    private void populateParameterPane(final PluginParameters pluginParameters) {
        final PluginParametersPane pluginParametersPane = PluginParametersPane.buildPane(pluginParameters, null);
        // The parameters should only be editable if we are looking at a category rather than a question.
        pluginParametersPane.setDisable(questionListPane.isExpanded());
        parametersTab.setContent(pluginParametersPane);
    }

    private void setPluginsFromSelectedCategory() {
        final String selectedCategory = categoryList.getSelectionModel().getSelectedItem();
        final List<SelectableAnalyticPlugin> categoryPlugins = selectedCategory == null ? new ArrayList<>() : categoryToPluginsMap.get(selectedCategory);
        final List<SelectableAnalyticPlugin> selectablePlugins = new ArrayList<>();
        setSuppressedFlag(true);
        for (final SelectableAnalyticPlugin selectablePlugin : categoryPlugins) {
            selectablePlugin.checkbox.setDisable(false);
            selectablePlugin.checkbox.setSelected(false);
            selectablePlugins.add(selectablePlugin);
        }
        pluginList.setItems(FXCollections.observableArrayList(selectablePlugins));
        pluginList.getSelectionModel().clearSelection();
        updateSelectablePluginsParameters();
        updateGlobalParameters();
        setSuppressedFlag(false);
        updateState(false);
    }

    private void setPluginsFromSelectedQuestion() {
        final List<SelectableAnalyticPlugin> questionPlugins = new ArrayList<>();
        final AnalyticQuestionDescription<?> selectedQuestion = questionList.getSelectionModel().getSelectedItem();
        SELECTABLE_PLUGINS.forEach(selectablePlugin -> {
            if (selectedQuestion != null && selectedQuestion.getPluginClasses().contains(selectablePlugin.plugin.getClass())) {
                questionPlugins.add(selectablePlugin);
            }
        });
        final List<SelectableAnalyticPlugin> selectablePlugins = new ArrayList<>();
        for (final SelectableAnalyticPlugin selectablePlugin : questionPlugins) {
            selectablePlugin.checkbox.setDisable(true);
            selectablePlugin.checkbox.setSelected(true);
            selectablePlugins.add(selectablePlugin);
        }
        pluginList.setItems(FXCollections.observableArrayList(selectablePlugins));
        pluginList.getSelectionModel().clearSelection();
        updateSelectablePluginsParameters();
        updateGlobalParameters();
    }

    public final void updateSelectablePluginsParameters() {
        if (categoryListPane.isExpanded()) {
            LOGGER.log(Level.INFO, "Update selectable plugins parameters in analytic config pane.");
            pluginList.getItems().forEach(selectablePlugin -> selectablePlugin.parameters.updateParameterValues(selectablePlugin.updatedParameters));

        } else if (questionListPane.isExpanded() && currentQuestion != null) {
            pluginList.getItems().forEach(selectablePlugin -> {
                selectablePlugin.parameters.updateParameterValues(selectablePlugin.updatedParameters);
                currentQuestion.initialiseParameters(selectablePlugin.plugin, selectablePlugin.parameters);
            });
        }
        updateGlobalParameters();
    }

    public final List<SelectableAnalyticPlugin> getAllSelectablePlugins() {
        return Collections.unmodifiableList(SELECTABLE_PLUGINS);
    }

    public final SelectableAnalyticPlugin lookupSelectablePlugin(final Plugin plugin) {
        return PLUGIN_TO_SELECTABLE_PLUGIN_MAP.get(plugin);
    }

    public static final SelectableAnalyticPlugin lookupSelectablePlugin(final String selectableAnalyticPluginName) {
        return NAME_TO_SELECTABLE_PLUGIN_MAP.get(selectableAnalyticPluginName);
    }

    private static void setSuppressedFlag(final boolean newValue) {
        selectionSuppressed = newValue;
    }

    public final class SelectableAnalyticPlugin {

        private final CheckBox checkbox;
        private ListCell<SelectableAnalyticPlugin> parent;
        private final AnalyticPlugin plugin;
        private final PluginParameters parameters;
        private final PluginParameters updatedParameters;

        public SelectableAnalyticPlugin(final AnalyticPlugin<?> plugin) {
            this.checkbox = new CheckBox();
            // Allows triggering of selection listener when a checkbox is changed
            this.checkbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (parent != null) {
                    if (parent.getListView().getSelectionModel().getSelectedItem() == this) {
                        parent.getListView().getSelectionModel().clearSelection();
                    }
                    parent.getListView().getSelectionModel().select(this);
                }
            });
            this.plugin = plugin;
            this.parameters = new PluginParameters();
            parameters.addGroup(GLOBAL_PARAMS_GROUP, new PluginParametersPane.TitledSeparatedParameterLayout(GLOBAL_PARAMS_GROUP, 14, false));
            globalAnalyticParameters.getParameters().values().forEach(parameter -> parameters.addParameter(parameter, GLOBAL_PARAMS_GROUP));
            final String parameterSpecificGroupName = plugin.getName() + " Parameters";
            parameters.addGroup(parameterSpecificGroupName, new PluginParametersPane.TitledSeparatedParameterLayout(parameterSpecificGroupName, 14, false));
            plugin.createParameters().getParameters().values().forEach(parameter -> parameters.addParameter(parameter, parameterSpecificGroupName));
            plugin.onPrerequisiteAttributeChange(GraphManager.getDefault().getActiveGraph(), parameters);
            this.updatedParameters = parameters.copy();
        }

        public final void setParent(final ListCell<SelectableAnalyticPlugin> parent) {
            this.parent = parent;
        }

        public final boolean isSelected() {
            return checkbox.isSelected();
        }

        public final void setSelected(final boolean isSelected) {
            checkbox.setSelected(isSelected);
        }

        public final AnalyticPlugin<?> getPlugin() {
            return plugin;
        }

        public final PluginParameters getAllParameters() {
            return parameters;
        }

        public final PluginParameters getPluginSpecificParameters() {
            final PluginParameters pluginParameters = new PluginParameters();
            parameters.getParameters().entrySet().forEach(parameter -> {
                if (!globalAnalyticParameters.hasParameter(parameter.getKey())) {
                    pluginParameters.addParameter(parameter.getValue());
                }
            });
            return pluginParameters;
        }

        public final void setUpdatedParameter(final String parameterId, final String parameterValue) {
            this.updatedParameters.setStringValue(parameterId, parameterValue);
        }

        public final void setUpdatedParameters(final PluginParameters parameters) {
            this.updatedParameters.updateParameterValues(parameters);
        }
    }

    /**
     * Write the given AnalyticViewState to the active graph.
     */
    @PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.LOW_LEVEL})
    private static final class AnalyticViewStateWriter extends SimpleEditPlugin {

        private final AnalyticQuestionDescription<?> question;
        private final List<SelectableAnalyticPlugin> plugins;

        public AnalyticViewStateWriter(final AnalyticQuestionDescription<?> question, final List<SelectableAnalyticPlugin> plugins) {
            this.question = question;
            this.plugins = plugins;
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            final int stateAttributeId = AnalyticViewConcept.MetaAttribute.ANALYTIC_VIEW_STATE.ensure(graph);
            final AnalyticViewState newState = graph.getObjectValue(stateAttributeId, 0) == null ? new AnalyticViewState()
                    : new AnalyticViewState(graph.getObjectValue(stateAttributeId, 0));
            newState.addAnalyticQuestion(question, plugins);
            graph.setObjectValue(stateAttributeId, 0, newState);
        }

        @Override
        protected boolean isSignificant() {
            return true;
        }

        @Override
        public String getName() {
            return "Analytic View: Update State";
        }
    }

    /**
     * Update the display by reading and writing to/from the state attribute.
     */
    @PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.LOW_LEVEL})
    private static final class AnalyticViewStateUpdater extends SimpleEditPlugin {

        private final AnalyticConfigurationPane analyticConfigurationPane;
        private final boolean pluginWasSelected;

        public AnalyticViewStateUpdater(final AnalyticConfigurationPane analyticConfigurationPane, final boolean pluginWasSelected) {
            this.analyticConfigurationPane = analyticConfigurationPane;
            this.pluginWasSelected = pluginWasSelected;
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            final String currentCategory = analyticConfigurationPane.categoryList.getSelectionModel().getSelectedItem();
            final int stateAttributeId = AnalyticViewConcept.MetaAttribute.ANALYTIC_VIEW_STATE.ensure(graph);

            // Make a copy in case the state on the graph is currently being modified.
            final AnalyticViewState currentState = graph.getObjectValue(stateAttributeId, 0) == null
                    ? new AnalyticViewState()
                    : new AnalyticViewState(graph.getObjectValue(stateAttributeId, 0));

            if (pluginWasSelected) {
                // remove all plugins matching category
                currentState.removePluginsMatchingCategory(currentCategory);
                // grab all plugins from currently selected category
                final List<SelectableAnalyticPlugin> checkedPlugins = new ArrayList<>();
                // adding items to checkedPlugins array when they are selected
                analyticConfigurationPane.pluginList.getItems().forEach(selectablePlugin -> {
                    if (selectablePlugin.isSelected()) {
                        checkedPlugins.add(selectablePlugin);
                    }
                });
                if (!checkedPlugins.isEmpty()) {
                    currentState.addAnalyticQuestion(analyticConfigurationPane.currentQuestion, checkedPlugins);
                }
                analyticConfigurationPane.saveState();
            }

            // Utilized for Question pane - TODO: when multiple tabs + saving of
            // questions is supported, link this currentquestion variable with
            // the saved/loaded question
            Platform.runLater(() -> analyticConfigurationPane.currentQuestion = currentState.getActiveAnalyticQuestions().isEmpty() ? null
                    : currentState.getActiveAnalyticQuestions().get(currentState.getCurrentAnalyticQuestionIndex()));
            if (!currentState.getActiveSelectablePlugins().isEmpty()) {
                for (final SelectableAnalyticPlugin selectedPlugin : currentState.getActiveSelectablePlugins().get(currentState.getCurrentAnalyticQuestionIndex())) {
                    if (currentCategory.equals(selectedPlugin.plugin.getClass().getAnnotation(AnalyticInfo.class).analyticCategory())) {
                        Platform.runLater(() -> {
                            AnalyticConfigurationPane.setSuppressedFlag(true);
                            selectedPlugin.setSelected(true);
                            AnalyticConfigurationPane.setSuppressedFlag(false);
                        });
                    }
                }
            }
        }

        @Override
        protected boolean isSignificant() {
            return false;
        }

        @Override
        public String getName() {
            return "Analytic View: Update State";
        }
    }
}
