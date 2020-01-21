/*
 * Copyright 2010-2019 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.graph.schema.SchemaAttribute;
import au.gov.asd.tac.constellation.pluginframework.Plugin;
import au.gov.asd.tac.constellation.pluginframework.PluginException;
import au.gov.asd.tac.constellation.pluginframework.PluginExecution;
import au.gov.asd.tac.constellation.pluginframework.PluginInteraction;
import au.gov.asd.tac.constellation.pluginframework.gui.PluginParametersPane;
import au.gov.asd.tac.constellation.pluginframework.parameters.PluginParameter;
import au.gov.asd.tac.constellation.pluginframework.parameters.PluginParameters;
import au.gov.asd.tac.constellation.pluginframework.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.pluginframework.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.pluginframework.templates.SimpleEditPlugin;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
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
 * The pane holding gui elements related to configuration of an analytic
 * question.
 *
 * @author cygnus_x-1
 */
public class AnalyticConfigurationPane extends VBox {

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
    private final ListView<AnalyticQuestionDescription> questionList;
    private final TitledPane pluginListPane;
    private final ListView<SelectableAnalyticPlugin> pluginList;
    private final TitledPane informationPane;
    private final TabPane informationTabPane;
    private final Tab parametersTab;
    private final Tab documentationTab;
    private WebView documentationView;
    private static boolean suppressed = false;

    private AnalyticQuestionDescription currentQuestion = null;
    private final Map<String, List<SelectableAnalyticPlugin>> categoryToPluginsMap;
    private final Map<AnalyticQuestionDescription, List<SelectableAnalyticPlugin>> questionToPluginsMap;
    private final PluginParameters globalAnalyticParameters = new PluginParameters();
    private static boolean onLoad = true;

    public AnalyticConfigurationPane() {

        // create the parameters needed for all analytic questions
        createGlobalParameters();

        // set up the list of available analytic plugins
        Lookup.getDefault().lookupAll(AnalyticPlugin.class).forEach((plugin) -> {
            final SelectableAnalyticPlugin selectablePlugin = new SelectableAnalyticPlugin(plugin);
            SELECTABLE_PLUGINS.add(selectablePlugin);
            PLUGIN_TO_SELECTABLE_PLUGIN_MAP.put(plugin, selectablePlugin);
            NAME_TO_SELECTABLE_PLUGIN_MAP.put(plugin.getName(), selectablePlugin);
        });
        Collections.sort(SELECTABLE_PLUGINS, (selectablePlugin1, selectablePlugin2) -> {
            return selectablePlugin1.plugin.getName().compareToIgnoreCase(selectablePlugin2.plugin.getName());
        });

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
        this.categoryList = new ListView();
        final List<String> categories = new ArrayList<>(categoryToPluginsMap.keySet());
        Collections.sort(categories, (category1, category2) -> {
            return category1.compareToIgnoreCase(category2);
        });
        categoryList.getItems().addAll(categories);
        categoryList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                currentQuestion = null;
                populateParameterPane(globalAnalyticParameters);
                setPluginsFromSelectedCategory();
            });
        });

        // build the pane holding the list of analytic categories
        this.categoryListPane = new TitledPane("Categories", categoryList);

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
        this.questionList = new ListView();
        final List<AnalyticQuestionDescription> questions = new ArrayList(questionToPluginsMap.keySet());
        Collections.sort(questions, (question1, question2) -> {
            return question1.getName().compareToIgnoreCase(question2.getName());
        });
        questionList.getItems().addAll(questions);
        questionList.setCellFactory((ListView<AnalyticQuestionDescription> list) -> {
            ListCell<AnalyticQuestionDescription> cell = new ListCell<AnalyticQuestionDescription>() {

                @Override
                protected void updateItem(final AnalyticQuestionDescription item, final boolean empty) {
                    super.updateItem(item, empty);
                    setText(item == null ? "" : item.getName());
                }
            };

            return cell;
        });
        questionList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                currentQuestion = newValue;
                SingleChoiceParameterType.getOptionsData((PluginParameter<SingleChoiceParameterValue>) globalAnalyticParameters.getParameters().get(AGGREGATOR_PARAMETER_ID)).forEach((aggregatorParameterValue) -> {
                    if (((AnalyticAggregatorParameterValue) aggregatorParameterValue).getObjectValue().getClass().equals(currentQuestion.getAggregatorType())) {
                        SingleChoiceParameterType.setChoiceData((PluginParameter<SingleChoiceParameterValue>) globalAnalyticParameters.getParameters().get(AGGREGATOR_PARAMETER_ID), (AnalyticAggregatorParameterValue) aggregatorParameterValue);
                    }
                });
                populateParameterPane(globalAnalyticParameters);
                setPluginsFromSelectedQuestion();
            });
        });

        // build the pane holding the list of analytic questions
        this.questionListPane = new TitledPane("Questions", questionList);

        // ensure that only one of either the category or question pane are expanded at any time
        categoryListPane.expandedProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                questionListPane.setExpanded(!categoryListPane.isExpanded());
                if (categoryListPane.isExpanded()) {
                    currentQuestion = null;
                    populateParameterPane(globalAnalyticParameters);
                    setPluginsFromSelectedCategory();
                }
            });
        });
        questionListPane.expandedProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                categoryListPane.setExpanded(!questionListPane.isExpanded());
                if (questionListPane.isExpanded()) {
                    currentQuestion = questionList.getSelectionModel().getSelectedItem();
                    populateParameterPane(globalAnalyticParameters);
                    setPluginsFromSelectedQuestion();
                }
            });
        });

        // populate the category and question pane
        categoryAndQuestionSelectionPane.getChildren().addAll(categoryListPane, questionListPane);

        // set up the list of analytic plugins
        this.pluginList = new ListView();
        pluginList.setCellFactory((ListView<SelectableAnalyticPlugin> selectableAnalytics) -> {
            ListCell<SelectableAnalyticPlugin> cell = new ListCell<SelectableAnalyticPlugin>() {

                @Override
                protected void updateItem(final SelectableAnalyticPlugin item, final boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        //item.checkbox.selectedProperty().addListener((observable, oldValue, newValue) -> {item.});
                        item.setParent(this);
                        setGraphic(item.checkbox);
                        setText(item.plugin.getName());
                    }
                }
            };

            return cell;
        });
        pluginList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(!suppressed){
                if (newValue != null) {
                    populateDocumentationPane(newValue);
                    populateParameterPane(newValue.getAllParameters());
                    // only update state when the categories are expanded
                    if(categoryListPane.isExpanded()){  
                        updateState(false);
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
            documentationView.setFontScale(0.5);
            documentationView.getEngine().setUserStyleSheetLocation(
                    getClass().getResource("resources/documentation.css").toExternalForm());
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
        } catch (InterruptedException ex) {
            this.documentationView = null;
            documentationTab.setContent(null);
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
     * Reset to the initial state for the analytic configuration pane. The
     * question list will be expanded, the first question selected, and the
     * analytics list populated based on the selected question.
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
    protected final AnalyticQuestionDescription getCurrentQuestion() {
        return currentQuestion;
    }

    /**
     * Answer the question which is currently selected in this pane.
     *
     * @return the answered {@link AnalyticQuestion}.
     */
    protected final AnalyticQuestion answerCurrentQuestion() throws AnalyticException {

        // build question
        final AnalyticQuestion question = new AnalyticQuestion(currentQuestion);

        // retrieve and set any global parameters in the question
        final AnalyticAggregatorParameterValue aggregatorParameterValue = (AnalyticAggregatorParameterValue) globalAnalyticParameters.getSingleChoice(AGGREGATOR_PARAMETER_ID);
        if (aggregatorParameterValue == null) {
            throw new AnalyticException("You must select an aggregation method!");
        }
        final AnalyticAggregator aggregator = (AnalyticAggregator) aggregatorParameterValue.getObjectValue();
        question.setAggregator(aggregator);

        // add and set parameters for each plugin in the question
        final List<SelectableAnalyticPlugin> selectedPlugins = new ArrayList<>();
        pluginList.getItems().forEach((selectablePlugin) -> {
            if (selectablePlugin.checkbox.isSelected()) {
                selectedPlugins.add(selectablePlugin);
                question.addPlugin(selectablePlugin.plugin, selectablePlugin.getPluginSpecificParameters());
            }
        });
        if (selectedPlugins.isEmpty()) {
            throw new AnalyticException("You must select at least one analytic!");
        }

        // update the analytic view state
        PluginExecution.withPlugin(new AnalyticViewStateWriter(currentQuestion, selectedPlugins)).executeLater(GraphManager.getDefault().getActiveGraph());

        // answer the question
        return question.answer(GraphManager.getDefault().getActiveGraph());
    }

    private void populateDocumentationPane(final SelectableAnalyticPlugin plugin) {
        if (documentationView != null) {
            if (plugin == null || plugin.getPlugin() == null || plugin.getPlugin().getDocumentationUrl() == null) {
                documentationView.getEngine().loadContent("<html>No Documentation Available</html>", "text/html");
            } else {
                documentationView.getEngine().load(plugin.getPlugin().getDocumentationUrl());
            }
        }
    }
    protected void setQuestion(List<AnalyticQuestionDescription<?>> question, final List<List<SelectableAnalyticPlugin>> plugins){
        // get question only if one exists
        this.currentQuestion = question.isEmpty() ? null : question.get(0);
        
        //categoryList.getSelectionModel().clearSelection(); // TODO: may need this to clear old selections
        
        // for each list of plugins
        for(List<SelectableAnalyticPlugin> selectedPluginList : plugins){
            // for each plugin within the list of plugins
            for(SelectableAnalyticPlugin selectedPlugin : selectedPluginList){
            
                // get the selected plugin and its category
                final SelectableAnalyticPlugin temp = selectedPlugin;
                final String category = temp.plugin.getClass().getAnnotation(AnalyticInfo.class).analyticCategory();

                // run on different thread to update the UI items
                Platform.runLater(
                () -> {
                    //when plugin is the same category as the selected category
                    if(categoryList.getSelectionModel().getSelectedItem().equals(category)){
                        categoryList.getSelectionModel().select(categoryList.getItems().indexOf(category));
                        categoryListPane.setExpanded(true);
                    }     
                });
                
                Platform.runLater(
                () -> {  
                    //setting plugin enabled (DOES NOT KEEP STATE)??
                    //selectedPlugin.checkbox.setSelected(true);
                    selectedPlugin.setSelected(true);
                });

            }
        }
    }
    
    /**
     * Refresh the state of the display by reading the Analytic View state attribute.
     */
    protected void refreshState(){
        //PluginExecution.withPlugin(new AnalyticViewStateReader(this)).executeLater(GraphManager.getDefault().getActiveGraph());
    }
    
    /**
     * Update/ save the state using the parameters checked on the display
     */
    protected void updateState(boolean isOnLoad){
        this.onLoad = isOnLoad;
        PluginExecution.withPlugin(new AnalyticViewStateUpdaterr(this)).executeLater(GraphManager.getDefault().getActiveGraph());
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
        final PluginParameter aggregatorParameter = globalAnalyticParameters.getParameters().get(AGGREGATOR_PARAMETER_ID);
        final List<AnalyticAggregatorParameterValue> aggregators = new ArrayList<>();
        if (categoryListPane.isExpanded()) {
            final Class<? extends AnalyticResult> pluginResultType = pluginList.getItems().get(0).getPlugin().getResultType();
            AnalyticUtilities.lookupAnalyticAggregators(pluginResultType)
                    .forEach(aggregator -> aggregators.add(new AnalyticAggregatorParameterValue((AnalyticAggregator) aggregator)));
            SingleChoiceParameterType.setOptionsData(aggregatorParameter, aggregators);
            SingleChoiceParameterType.setChoiceData(aggregatorParameter, aggregators.get(0));
        } else if (questionListPane.isExpanded() && currentQuestion != null) {
            final Class<? extends AnalyticAggregator> questionAggregatorType = currentQuestion.getAggregatorType();
            aggregators.add(new AnalyticAggregatorParameterValue(AnalyticUtilities.lookupAnalyticAggregator(questionAggregatorType)));
            SingleChoiceParameterType.setOptionsData(aggregatorParameter, aggregators);
            SingleChoiceParameterType.setChoiceData(aggregatorParameter, aggregators.get(0));
        }
        pluginList.getItems().forEach(selectablePlugin -> {
            selectablePlugin.setUpdatedParameter(aggregatorParameter.getId(), aggregatorParameter.getStringValue());
        });
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
        for (SelectableAnalyticPlugin selectablePlugin : categoryPlugins) {
            selectablePlugin.checkbox.setDisable(false);
            selectablePlugin.checkbox.setSelected(false);
            selectablePlugins.add(selectablePlugin);
        }
        pluginList.setItems(FXCollections.observableArrayList(selectablePlugins));
        pluginList.getSelectionModel().clearSelection();
        updateSelectablePluginsParameters();
        updateGlobalParameters();
        updateState(true);
    }

    private void setPluginsFromSelectedQuestion() {
        final List<SelectableAnalyticPlugin> questionPlugins = new ArrayList<>();
        final AnalyticQuestionDescription selectedQuestion = questionList.getSelectionModel().getSelectedItem();
        SELECTABLE_PLUGINS.forEach((selectablePlugin) -> {
            if (selectedQuestion != null && selectedQuestion.getPluginClasses().contains(selectablePlugin.plugin.getClass())) {
                questionPlugins.add(selectablePlugin);
            }
        });
        final List<SelectableAnalyticPlugin> selectablePlugins = new ArrayList<>();
        for (SelectableAnalyticPlugin selectablePlugin : questionPlugins) {
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
            pluginList.getItems().forEach(selectablePlugin -> {
                selectablePlugin.parameters.updateParameterValues(selectablePlugin.updatedParameters); // concurrent modification exception
            });
        } else if (questionListPane.isExpanded() && currentQuestion != null) {
            pluginList.getItems().forEach(selectablePlugin -> {
                selectablePlugin.parameters.updateParameterValues(selectablePlugin.updatedParameters);
                if(currentQuestion != null){currentQuestion.initialiseParameters(selectablePlugin.plugin, selectablePlugin.parameters);} // illegal argument exception
            });
        }
    }

    public final List<SelectableAnalyticPlugin> getAllSelectablePlugins() {
        return Collections.unmodifiableList(SELECTABLE_PLUGINS);
    }

    public final SelectableAnalyticPlugin lookupSelectablePlugin(Plugin plugin) {
        return PLUGIN_TO_SELECTABLE_PLUGIN_MAP.get(plugin);
    }

    public static final SelectableAnalyticPlugin lookupSelectablePlugin(final String selectableAnalyticPluginName) {
        return NAME_TO_SELECTABLE_PLUGIN_MAP.get(selectableAnalyticPluginName);
    }

    public final class SelectableAnalyticPlugin {

        private final CheckBox checkbox;
        private ListCell<SelectableAnalyticPlugin> parent;
        private final AnalyticPlugin plugin;
        private PluginParameters parameters;
        private PluginParameters updatedParameters;

        public SelectableAnalyticPlugin(final AnalyticPlugin plugin) {
            this.checkbox = new CheckBox();
            this.checkbox.selectedProperty().addListener((observable, oldValue, newValue) -> {if(parent!=null){parent.getListView().getSelectionModel().select(this);}});
            this.plugin = plugin;
            this.parameters = new PluginParameters();
            parameters.addGroup(GLOBAL_PARAMS_GROUP, new PluginParametersPane.TitledSeparatedParameterLayout(GLOBAL_PARAMS_GROUP, 14, false));
            globalAnalyticParameters.getParameters().values().forEach((parameter) -> {
                parameters.addParameter(parameter, GLOBAL_PARAMS_GROUP);
            });
            final String parameterSpecificGroupName = plugin.getName() + " Parameters";
            parameters.addGroup(parameterSpecificGroupName, new PluginParametersPane.TitledSeparatedParameterLayout(parameterSpecificGroupName, 14, false));
            plugin.createParameters().getParameters().values().forEach((parameter) -> {
                parameters.addParameter(parameter, parameterSpecificGroupName);
            });
            plugin.onPrerequisiteAttributeChange(GraphManager.getDefault().getActiveGraph(), parameters);
            this.updatedParameters = parameters.copy();
        }
        
        public final void setParent(ListCell<SelectableAnalyticPlugin> parent){
            this.parent = parent;
        }

        public final boolean isSelected() {
            return checkbox.isSelected();
        }

        public final void setSelected(final boolean isSelected) {
            checkbox.setSelected(isSelected);
        }

        public final AnalyticPlugin getPlugin() {
            return plugin;
        }

        public final PluginParameters getAllParameters() {
            return parameters;
        }

        public final PluginParameters getPluginSpecificParameters() {
            PluginParameters pluginParameters = new PluginParameters();
            parameters.getParameters().entrySet().forEach((parameter) -> {
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
     * Write the given ScatterPlotState to the active graph.
     */
    private static final class AnalyticViewStateWriter extends SimpleEditPlugin {

        private final AnalyticQuestionDescription question;
        private final List<SelectableAnalyticPlugin> plugins;

        public AnalyticViewStateWriter(final AnalyticQuestionDescription question, final List<SelectableAnalyticPlugin> plugins) {
            this.question = question;
            this.plugins = plugins;
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            AnalyticViewState newState;
            int stateAttributeId = AnalyticViewConcept.MetaAttribute.ANALYTIC_VIEW_STATE.ensure(graph);
            newState = graph.getObjectValue(stateAttributeId, 0) == null ? new AnalyticViewState()
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
     * TODO : REFACTOR THIS CLASS NAME
     */
    private static final class AnalyticViewStateUpdaterr extends SimpleEditPlugin {

        private final AnalyticConfigurationPane analyticConfigurationPane;

        public AnalyticViewStateUpdaterr(final AnalyticConfigurationPane analyticConfigurationPane) {
            this.analyticConfigurationPane = analyticConfigurationPane;
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            
            // fetch current state
            AnalyticViewState fetchedState;
            // grab currently expanded category (Centrality, Global, Metrics, Similarity)
            String currentCategory = analyticConfigurationPane.categoryList.getSelectionModel().getSelectedItem();
            System.out.println("current category: " + currentCategory);
            // get state attribute id
            int stateAttributeId = AnalyticViewConcept.MetaAttribute.ANALYTIC_VIEW_STATE.ensure(graph);
            
            // if no state currently, make a new state and use defaults for setting.
            fetchedState = graph.getObjectValue(stateAttributeId, 0);
            
            if(fetchedState == null){
                fetchedState = new AnalyticViewState();
            }
            
            // TODO: this line may not be necessary
            fetchedState = graph.getObjectValue(stateAttributeId, 0) == null ? new AnalyticViewState()
                    : new AnalyticViewState(graph.getObjectValue(stateAttributeId, 0));
            

            // updates the state with a selection or deselection of the currently opened category
            if(!analyticConfigurationPane.onLoad){
                final AnalyticViewState currentState = fetchedState;
                final Runnable task = () -> {
                    // remove all plugins matching category
                    currentState.removePluginsMatchingCategory(currentCategory);                    
                };
                task.run();

                Thread thread = new Thread(task);
                thread.start();
                

                
                // grab all plugins from currently selected category
                List<SelectableAnalyticPlugin> checkedPlugins = new ArrayList<>();
            
                // adding items to checkedPlugins array.
                analyticConfigurationPane.pluginList.getItems().forEach(selectablePlugin -> {
                    if(selectablePlugin.isSelected()){ // AND Matches category
                        checkedPlugins.add(selectablePlugin);
                    }
                });
                
                // add all plugins to state attribute
                if(checkedPlugins.size() != 0){
                    currentState.addAnalyticQuestion(analyticConfigurationPane.currentQuestion, checkedPlugins);
                    graph.setObjectValue(stateAttributeId, 0, currentState); // save - move thi location
                }
            }
            
            // only this section when the user first loads the category
            
            // get question only if one exists
            analyticConfigurationPane.currentQuestion = currentState.getActiveAnalyticQuestions().isEmpty() ? null : currentState.getActiveAnalyticQuestions().get(0);

            // on load

            List<SelectableAnalyticPlugin> checkedWithinCategory = new ArrayList<>();

            // for each list of plugins
            for(List<SelectableAnalyticPlugin> selectedPluginList : currentState.getActiveSelectablePlugins()){
                // if the first index is the same category as the current category.
                if(!selectedPluginList.isEmpty() && currentCategory.equals(selectedPluginList.get(0).plugin.getClass().getAnnotation(AnalyticInfo.class).analyticCategory())){
                    // add plugin to selection
                    checkedWithinCategory.add(selectedPluginList.get(0));

                    Platform.runLater(
                    () -> {
                        analyticConfigurationPane.suppressed = true;
                        selectedPluginList.get(0).setSelected(true);
                        analyticConfigurationPane.suppressed = false;
                    });
                }
            }
        }

        @Override
        protected boolean isSignificant() {
            return false;
        }

        @Override
        public String getName() {
            return "Analytic View: updaterr State"; // todo refactor this name
        }
    }
}
