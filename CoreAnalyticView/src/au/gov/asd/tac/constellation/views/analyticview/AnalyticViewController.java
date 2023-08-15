/*
 * Copyright 2010-2023 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.LayersConcept;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.views.analyticview.AnalyticConfigurationPane.SelectableAnalyticPlugin;
import au.gov.asd.tac.constellation.views.analyticview.analytics.AnalyticInfo;
import au.gov.asd.tac.constellation.views.analyticview.questions.AnalyticQuestion;
import au.gov.asd.tac.constellation.views.analyticview.questions.AnalyticQuestionDescription;
import au.gov.asd.tac.constellation.views.analyticview.results.AnalyticResult;
import au.gov.asd.tac.constellation.views.analyticview.results.EmptyResult;
import au.gov.asd.tac.constellation.views.analyticview.state.AnalyticStateReaderPlugin;
import au.gov.asd.tac.constellation.views.analyticview.state.AnalyticStateWriterPlugin;
import au.gov.asd.tac.constellation.views.analyticview.state.AnalyticViewConcept;
import au.gov.asd.tac.constellation.views.analyticview.visualisation.GraphVisualisation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.ListView;

/**
 * Controls UI when changing between graphs
 *
 * @author Delphinus8821
 */
public class AnalyticViewController {

    private static final Logger LOGGER = Logger.getLogger(AnalyticViewController.class.getName());

    protected static final String SELECT_ON_GRAPH_PLUGIN_NAME = "Analytic View: Update Selection on Graph";

    // Analytic view controller instance
    private static AnalyticViewController instance = null;
    private AnalyticViewTopComponent parent;

    private int currentAnalyticQuestionIndex = 0;
    private final List<AnalyticQuestionDescription<?>> activeAnalyticQuestions;
    private final List<List<AnalyticConfigurationPane.SelectableAnalyticPlugin>> activeSelectablePlugins;
    private AnalyticResult<?> result;
    private boolean resultsVisible = false;
    private boolean categoriesVisible = false;
    private AnalyticQuestionDescription<?> currentQuestion = null;
    private AnalyticQuestion question;
    private String activeCategory;
    private HashMap<GraphVisualisation, Boolean> visualisations = new HashMap<>();

    public AnalyticViewController() {
        this.activeAnalyticQuestions = new ArrayList<>();
        this.activeSelectablePlugins = new ArrayList<>();
    }

    /**
     * Singleton instance retrieval
     *
     * @return the instance, if one is not made, it will make one.
     */
    public static synchronized AnalyticViewController getDefault() {
        if (instance == null) {
            instance = new AnalyticViewController();
        }
        return instance;
    }

    /**
     *
     * @param parent the TopComponent which this controller controls.
     * @return the instance to allow chaining
     */
    public AnalyticViewController init(final AnalyticViewTopComponent parent) {
        this.parent = parent;
        return instance;
    }

    public AnalyticViewTopComponent getParent() {
        return parent;
    }

    public void setActiveCategory(final String activeCategory) {
        this.activeCategory = activeCategory;
    }

    public void setCurrentQuestion(final AnalyticQuestionDescription<?> currentQuestion) {
        this.currentQuestion = currentQuestion;
    }

    public void setQuestion(final AnalyticQuestion question) {
        this.question = question;
    }

    public void setCategoriesVisible(final boolean categoriesVisible) {
        this.categoriesVisible = categoriesVisible;
    }

    public HashMap<GraphVisualisation, Boolean> getVisualisations() {
        return visualisations;
    }

    public void setVisualisations(final HashMap<GraphVisualisation, Boolean> visualisations) {
        this.visualisations = visualisations;

    }

    /**
     * Update which graph visualisations are on the graph and what state they are in
     * 
     * @param visualisationName
     * @param activated
     */
    public void updateVisualisations(final GraphVisualisation visualisationName, final boolean activated) {
        visualisations.entrySet().forEach(node -> {
            if (node.getKey().getName().equals(visualisationName.getName())) {
                visualisations.remove(node.getKey());
                visualisations.put(visualisationName, activated);
            } else {
                visualisations.put(visualisationName, activated);
            }
        });
    }

    public void deactivateResultUpdates() {
        final AnalyticViewPane pane = parent.createContent();
        pane.deactivateResultChanges();
        writeState();
    }

    /**
     * Update the results values and record whether the results pane is currently visible
     *
     * @param newResults
     */
    public void updateResults(final AnalyticResult<?> newResults) {
        result = newResults;
        resultsVisible = result != null;
    }

    /**
     * Remove a question from the list
     *
     * @param question
     */
    public void removeAnalyticQuestion(final AnalyticQuestionDescription<?> question) {
        activeSelectablePlugins.remove(activeAnalyticQuestions.indexOf(question));
        activeAnalyticQuestions.remove(question);
    }

    /**
     * Check the currently selected Question index of plugins for other plugins matching the selected category
     *
     * @param currentCategory the currently selected plugin category to remove from
     */
    public void removePluginsMatchingCategory(final String currentCategory) {
        if (!activeSelectablePlugins.isEmpty()) {
            activeSelectablePlugins.get(currentAnalyticQuestionIndex).removeIf(plugin
                    -> (plugin.getPlugin().getClass().getAnnotation(AnalyticInfo.class)
                            .analyticCategory().equals(currentCategory))
            );
        }
    }

    /**
     * Add an analytic question to the list
     *
     * @param question
     * @param selectablePlugins
     */
    public void addAnalyticQuestion(final AnalyticQuestionDescription<?> question, final List<AnalyticConfigurationPane.SelectableAnalyticPlugin> selectablePlugins) {
        if (activeAnalyticQuestions.contains(question)) {
            currentAnalyticQuestionIndex = activeAnalyticQuestions.indexOf(question);
            selectablePlugins.forEach(plugin -> {
                if (!activeSelectablePlugins.get(currentAnalyticQuestionIndex).contains(plugin)) {
                    activeSelectablePlugins.get(currentAnalyticQuestionIndex).add(plugin);
                }
            });
        } else {
            // does not contain question
            activeAnalyticQuestions.add(currentAnalyticQuestionIndex, question);
            activeSelectablePlugins.add(currentAnalyticQuestionIndex, selectablePlugins);
        }
    }

    /**
     * Add attributes required by the Analytic View for it to function
     */
    public void addAttributes() {
        final Graph activeGraph = GraphManager.getDefault().getActiveGraph();
        if (activeGraph != null) {
            PluginExecution.withPlugin(new AddAttributesPlugin()).executeLater(activeGraph);
        }
    }

    /**
     * Updates the AnalyticViewState by running a plugin to save the graph state
     *
     * @param pluginWasSelected true if the triggered update was from a plugin
     * being selected
     */
    public void updateState(final boolean pluginWasSelected, final ListView<AnalyticConfigurationPane.SelectableAnalyticPlugin> pluginList) {
        final AnalyticViewPane pane = parent.createContent();
        final String currentCategory = pane.getConfigurationPane().getCategoryList().getSelectionModel().getSelectedItem();

        if (pluginWasSelected) {
            // remove all plugins matching category
            removePluginsMatchingCategory(currentCategory);
            // grab all plugins from currently selected category
            final List<AnalyticConfigurationPane.SelectableAnalyticPlugin> checkedPlugins = new ArrayList<>();
            // adding items to checkedPlugins array when they are selected
            pluginList.getItems().forEach(selectablePlugin -> {
                if (selectablePlugin.isSelected()) {
                    checkedPlugins.add(selectablePlugin);
                }
            });
            if (!checkedPlugins.isEmpty()) {
                addAnalyticQuestion(pane.getConfigurationPane().getCurrentQuestion(), checkedPlugins);
            }
        }
        writeState();
    }

    /**
     * Reads the graph's analytic_view_state attribute and populates the Analytic View pane.
     */
    public void readState() {
        if (parent == null) {
            return;
        }
        final AnalyticViewPane pane = parent.createContent();
        final Graph graph = GraphManager.getDefault().getActiveGraph();
        if (pane == null || graph == null) {
            return;
        }
        PluginExecution.withPlugin(new AnalyticStateReaderPlugin(pane)).executeLater(graph);
    }

    /**
     * Executes a plugin to write the current state to the graph's analytic_view_state Attribute.
     *
     * @return a future of the plugin
     */
    public Future<?> writeState() {
        final Graph graph = GraphManager.getDefault().getActiveGraph();
        if (graph == null) {
            return null;
        }

        // controller out of sync with graph...
        return PluginExecution.withPlugin(new AnalyticStateWriterPlugin(currentAnalyticQuestionIndex, activeAnalyticQuestions, activeSelectablePlugins, result, resultsVisible, currentQuestion, question, categoriesVisible, activeCategory, visualisations)).executeLater(graph);
    }



    public void selectOnGraph(final GraphElementType elementType, final List<Integer> elementIds) {
        PluginExecution.withPlugin(new SelectOnGraphPlugin(elementType, elementIds)).executeLater(GraphManager.getDefault().getActiveGraph());
    }

    public void selectOnInternalVisualisations(final GraphElementType elementType, final Graph graph) {
        final AnalyticViewPane pane = parent.createContent();
        final AnalyticResult<?> results = pane.getResultsPane().getResult();

        if (graph != null && results != null) {
            final List<Integer> selected = new ArrayList<>();
            final ReadableGraph readableGraph = graph.getReadableGraph();
            try {
                switch (elementType) {
                    case VERTEX:
                        final int vertexSelectedAttribute = VisualConcept.VertexAttribute.SELECTED.get(readableGraph);
                        final int vertexCount = readableGraph.getVertexCount();
                        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
                            final int vertexId = readableGraph.getVertex(vertexPosition);
                            final boolean vertexSelected = readableGraph.getBooleanValue(vertexSelectedAttribute, vertexId);
                            if (vertexSelected) {
                                selected.add(vertexId);
                            }
                        }
                        break;
                    case TRANSACTION:
                        final int transactionSelectedAttribute = VisualConcept.TransactionAttribute.SELECTED.get(readableGraph);
                        final int transactionCount = readableGraph.getTransactionCount();
                        for (int transactionPosition = 0; transactionPosition < transactionCount; transactionPosition++) {
                            final int transactionId = readableGraph.getTransaction(transactionPosition);
                            final boolean transactionSelected = readableGraph.getBooleanValue(transactionSelectedAttribute, transactionId);
                            if (transactionSelected) {
                                selected.add(transactionId);
                            }
                        }
                        break;
                    default:
                        break;
                }
            } finally {
                readableGraph.release();
            }
            results.setSelectionOnVisualisation(elementType, selected);
        } 
    }
    
    @PluginInfo(pluginType = PluginType.SELECTION, tags = {PluginTags.SELECT})
    public static class SelectOnGraphPlugin extends SimpleEditPlugin {

        final GraphElementType elementType;
        final List<Integer> elementIds;

        public SelectOnGraphPlugin(final GraphElementType elementType, final List<Integer> elementIds) {
            this.elementType = elementType;
            this.elementIds = elementIds;
        }

        @Override
        public String getName() {
            return AnalyticViewController.SELECT_ON_GRAPH_PLUGIN_NAME;
        }

        @Override
        protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            switch (elementType) {
                case VERTEX:
                    final int vertexSelectedAttribute = VisualConcept.VertexAttribute.SELECTED.get(graph);
                    final int vertexCount = graph.getVertexCount();
                    for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
                        final int vertexId = graph.getVertex(vertexPosition);
                        graph.setBooleanValue(vertexSelectedAttribute, vertexId, elementIds.contains(vertexId));
                    }
                    break;
                case TRANSACTION:
                    final int transactionSelectedAttribute = VisualConcept.TransactionAttribute.SELECTED.get(graph);
                    final int transactionCount = graph.getTransactionCount();
                    for (int transactionPosition = 0; transactionPosition < transactionCount; transactionPosition++) {
                        final int transactionId = graph.getTransaction(transactionPosition);
                        graph.setBooleanValue(transactionSelectedAttribute, transactionId, elementIds.contains(transactionId));
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Plugin to add the required Analytic View attributes.
     */
    @PluginInfo(pluginType = PluginType.CREATE, tags = {PluginTags.CREATE})
    protected class AddAttributesPlugin extends SimpleEditPlugin {

        @Override
        public String getName() {
            return "Analytic View: Add Required Attributes";
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
            AnalyticViewConcept.MetaAttribute.ANALYTIC_VIEW_STATE.ensure(graph);
        }
    }
}
