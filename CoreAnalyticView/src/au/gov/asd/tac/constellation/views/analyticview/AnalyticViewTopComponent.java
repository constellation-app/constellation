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
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import au.gov.asd.tac.constellation.views.JavaFxTopComponent;
import au.gov.asd.tac.constellation.views.analyticview.analytics.AnalyticPlugin;
import au.gov.asd.tac.constellation.views.analyticview.results.AnalyticResult;
import au.gov.asd.tac.constellation.views.analyticview.state.AnalyticViewConcept;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

/**
 * The Analytic View Top Component.
 */
@TopComponent.Description(
        preferredID = "AnalyticViewTopComponent",
        iconBase = "au/gov/asd/tac/constellation/views/analyticview/resources/analytic-view.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(
        mode = "explorer",
        openAtStartup = false
)
@ActionID(
        category = "Window",
        id = "au.gov.asd.tac.constellation.views.analyticview.AnalyticViewTopComponent"
)
@ActionReferences({
    @ActionReference(path = "Menu/Views", position = 0),
    @ActionReference(path = "Shortcuts", name = "CS-Z")
})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_AnalyticViewAction",
        preferredID = "AnalyticViewTopComponent"
)
@Messages({
    "CTL_AnalyticViewAction=Analytic View",
    "CTL_AnalyticViewTopComponent=Analytic View",
    "HINT_AnalyticViewTopComponent=Analytic View"
})
public final class AnalyticViewTopComponent extends JavaFxTopComponent<AnalyticViewPane> {

    private final AnalyticViewPane analyticViewPane;
    private final AnalyticController analyticController;
    private boolean suppressed = false;
    private String currentGraphId = StringUtils.EMPTY;

    public AnalyticViewTopComponent() {
        super();
        this.analyticController = new AnalyticController();
        this.analyticViewPane = new AnalyticViewPane(analyticController);
        initComponents();
        setName(Bundle.CTL_AnalyticViewTopComponent());
        setToolTipText(Bundle.HINT_AnalyticViewTopComponent());
        super.initContent();

        // analytic view specific listeners
        addStructureChangeHandler(graph -> {
            if (needsUpdate() && !suppressed) {
                analyticViewPane.getConfigurationPane().saveState();
            }
        });
        addAttributeValueChangeHandler(AnalyticViewConcept.MetaAttribute.ANALYTIC_VIEW_STATE, graph -> {
            if (needsUpdate() && !suppressed) {
                analyticViewPane.getConfigurationPane().saveState();
            }
        });
        addAttributeValueChangeHandler(VisualConcept.VertexAttribute.SELECTED, graph -> {
            if (needsUpdate()) {
                analyticController.selectOnInternalVisualisations(GraphElementType.VERTEX, graph);
            }
        });
        addAttributeValueChangeHandler(VisualConcept.TransactionAttribute.SELECTED, graph -> {
            if (needsUpdate()) {
                analyticController.selectOnInternalVisualisations(GraphElementType.TRANSACTION, graph);
            }
        });
        addIgnoredEvent(AnalyticController.SELECT_ON_GRAPH_PLUGIN_NAME);

        // plugin specific listeners
        final Map<SchemaAttribute, List<AnalyticPlugin<?>>> prerequisiteAttributes = new HashMap<>();
        analyticViewPane.getConfigurationPane().getAllSelectablePlugins().forEach(selectablePlugin -> {
            final AnalyticPlugin<?> plugin = selectablePlugin.getPlugin();
            plugin.getPrerequisiteAttributes().forEach(attribute -> {
                if (!prerequisiteAttributes.containsKey(attribute)) {
                    prerequisiteAttributes.put(attribute, new ArrayList<>());
                }
                prerequisiteAttributes.get(attribute).add(plugin);
            });
        });
        prerequisiteAttributes.forEach((attribute, plugins) -> addAttributeValueChangeHandler(attribute, graph -> {
            if (needsUpdate() && !suppressed) {
                plugins.forEach(plugin -> {
                    final PluginParameters updatedParameters = plugin.createParameters().copy();
                    plugin.onPrerequisiteAttributeChange(graph, updatedParameters);
                    analyticViewPane.getConfigurationPane().lookupSelectablePlugin(plugin).setUpdatedParameters(updatedParameters);
                });
                analyticViewPane.getConfigurationPane().updateSelectablePluginsParameters();
            }
        }));
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    @Override
    protected AnalyticViewPane createContent() {
        return analyticViewPane;
    }

    @Override
    protected String createStyle() {
        return JavafxStyleManager.isDarkTheme()
                ? "resources/analytic-view-dark.css"
                : null;

    }

    @Override
    protected void handleNewGraph(final Graph graph) {
        if (!needsUpdate()) {
            return;
        }
        if (graph != null) {
            currentGraphId = graph.getId();
        }
        if (analyticViewPane != null) {
            analyticViewPane.setIsRunnable(graph != null);
            analyticViewPane.reset();
        }
        suppressed = true;
        manualUpdate();
        suppressed = false;
        if (analyticViewPane != null) {
            analyticViewPane.getConfigurationPane().updateState(false);
        }
    }

    @Override
    protected void handleGraphOpened(final Graph graph) {
        if (needsUpdate()) {
            if (graph != null) {
                currentGraphId = graph.getId();
            }
            analyticViewPane.getConfigurationPane().updateState(false);
        }
    }

    @Override
    protected void handleComponentOpened() {
        super.handleComponentOpened();
        if (needsUpdate()) {
            final Graph current = GraphManager.getDefault().getActiveGraph();
            if (current != null) {
                currentGraphId = current.getId();
            }
            analyticViewPane.getConfigurationPane().updateState(false);
        }
    }

    @Override
    protected void componentShowing() {
        super.componentShowing();
        final Graph current = GraphManager.getDefault().getActiveGraph();
        if (current != null && !current.getId().equals(currentGraphId)) {
            analyticViewPane.reset();
        }
        analyticViewPane.getConfigurationPane().updateState(false);
    }

    public class AnalyticController {

        protected static final String SELECT_ON_GRAPH_PLUGIN_NAME = "Analytic View: Update Selection on Graph";

        public void selectOnGraph(final GraphElementType elementType, final List<Integer> elementIds) {
            PluginExecution.withPlugin(new SelectOnGraphPlugin(elementType, elementIds)).executeLater(getCurrentGraph());
        }

        public void selectOnInternalVisualisations(final GraphElementType elementType, final Graph graph) {
            final AnalyticResult<?> result = analyticViewPane.getResultsPane().getResult();

            if (graph != null && result != null) {
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

                result.setSelectionOnVisualisation(elementType, selected);
            }
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
            return AnalyticViewTopComponent.AnalyticController.SELECT_ON_GRAPH_PLUGIN_NAME;
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
}
