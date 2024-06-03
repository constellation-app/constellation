/*
 * Copyright 2010-2024 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.scatterplot;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.monitor.AttributeValueMonitor;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import au.gov.asd.tac.constellation.views.JavaFxTopComponent;
import au.gov.asd.tac.constellation.views.scatterplot.state.ScatterPlotConcept;
import au.gov.asd.tac.constellation.views.scatterplot.state.ScatterPlotState;
import au.gov.asd.tac.constellation.views.scatterplot.state.ScatterPlotStateWriter;
import java.util.Set;
import java.util.function.Consumer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

/**
 * Scatter Plot.
 */
@TopComponent.Description(
        preferredID = "ScatterPlotTopComponent",
        iconBase = "au/gov/asd/tac/constellation/views/scatterplot/resources/scatter-plot.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(
        mode = "explorer",
        openAtStartup = false
)
@ActionID(
        category = "Window",
        id = "au.gov.asd.tac.constellation.views.scatterplot.ScatterPlotTopComponent"
)
@ActionReferences({
    @ActionReference(path = "Menu/Views", position = 1200),
    @ActionReference(path = "Shortcuts", name = "CS-O")
})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_ScatterPlotAction",
        preferredID = "ScatterPlotTopComponent"
)
@Messages({
    "CTL_ScatterPlotAction=Scatter Plot",
    "CTL_ScatterPlotTopComponent=Scatter Plot",
    "HINT_ScatterPlotTopComponent=Scatter Plot"
})
public final class ScatterPlotTopComponent extends JavaFxTopComponent<ScatterPlotPane> {

    private ScatterPlotState currentState;
    private ScatterPlotPane scatterPlotPane;
    private Consumer<Graph> selectionHandler;
    private Consumer<Graph> refreshHandler;
    private AttributeValueMonitor xAttributeMonitor;
    private AttributeValueMonitor yAttributeMonitor;
    private AttributeValueMonitor selectedAttributeMonitor;

    private static final String SELECTED_ATTRIBUTE_NAME = "selected";

    public ScatterPlotTopComponent() {
        setName(Bundle.CTL_ScatterPlotTopComponent());
        setToolTipText(Bundle.HINT_ScatterPlotTopComponent());
        initComponents();

        currentState = null;
        scatterPlotPane = new ScatterPlotPane(ScatterPlotTopComponent.this);
        initContent();

        selectionHandler = graph -> {
            ScatterPlotState state = null;
            try {
                state = getState(graph);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                ScatterPlotErrorDialog.create("Error getting scatter plot state: " + e.getMessage());
            }

            if (state != null && state.isSelectedOnly()) {
                scatterPlotPane.getChartPane().refreshChart(state);
            } else {
                final Set<ScatterData> selectedElements = scatterPlotPane.getChartPane().getSelectionFromGraph(state);
                scatterPlotPane.getChartPane().selectElementsOnChart(selectedElements, null);
            }
        };

        refreshHandler = graph -> {
            ScatterPlotState state = null;
            if (graph != null) {
                try {
                    state = getState(graph);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    ScatterPlotErrorDialog.create("Error getting scatter plot state: " + e.getMessage());
                }
            }

            if (currentState != state) {
                if (currentState != null && currentState.getXAttribute() != null && currentState.getYAttribute() != null) {
                    if (xAttributeMonitor != null) {
                        removeAttributeValueChangeHandler(xAttributeMonitor);
                    }
                    if (yAttributeMonitor != null) {
                        removeAttributeValueChangeHandler(yAttributeMonitor);
                    }
                    if (selectedAttributeMonitor != null) {
                        removeAttributeValueChangeHandler(selectedAttributeMonitor);
                    }
                }
                if (state != null && state.getXAttribute() != null && state.getYAttribute() != null) {
                    xAttributeMonitor = addAttributeValueChangeHandler(state.getElementType(), state.getXAttribute().getName(), refreshHandler);
                    yAttributeMonitor = addAttributeValueChangeHandler(state.getElementType(), state.getYAttribute().getName(), refreshHandler);
                    if (!SELECTED_ATTRIBUTE_NAME.equals(state.getXAttribute().getName()) && !SELECTED_ATTRIBUTE_NAME.equals(state.getYAttribute().getName())) {
                        selectedAttributeMonitor = addAttributeValueChangeHandler(state.getElementType(), SELECTED_ATTRIBUTE_NAME, selectionHandler);
                    }
                }

                currentState = state;
            }

            scatterPlotPane.refreshScatterPlot(state);
        };

        xAttributeMonitor = null;
        yAttributeMonitor = null;

        addStructureChangeHandler(refreshHandler);
        addAttributeCountChangeHandler(refreshHandler);
        addAttributeValueChangeHandler(ScatterPlotConcept.MetaAttribute.SCATTER_PLOT_STATE, refreshHandler);
    }

    /**
     * Get the current ScatterPlotState of the active graph, or create one if none exists.
     *
     * @return the current ScatterPlotState of the active graph, or create one if none exists.
     * @throws InterruptedException if the operation is interrupted during execution.
     */
    public ScatterPlotState getState() throws InterruptedException {
        return getState(currentGraph);
    }

    /**
     * Get the current ScatterPlotState of the given graph, or create one if none exists.
     *
     * @param graph the state will be read from the graph using this read lock.
     * @return the current ScatterPlotState of the given graph, or create one if none exists.
     * @throws InterruptedException if the operation is interrupted or canceled.
     */
    public ScatterPlotState getState(final Graph graph) throws InterruptedException {
        ScatterPlotState state = null;
        boolean newState = false;

        if (graph == null) {
            return state;
        } else {
            try (final ReadableGraph readableGraph = graph.getReadableGraph()) {
                final int stateAttribute = ScatterPlotConcept.MetaAttribute.SCATTER_PLOT_STATE.get(readableGraph);
                if (stateAttribute == Graph.NOT_FOUND) {
                    state = new ScatterPlotState();
                    newState = true;
                } else {
                    state = readableGraph.getObjectValue(stateAttribute, 0);
                    if (state == null) {
                        state = new ScatterPlotState();
                        newState = true;
                    }
                }
            }
        }

        if (newState) {
            final ScatterPlotState activeState = state;
            final Graph activeGraph = GraphManager.getDefault().getActiveGraph();
            PluginExecution.withPlugin(new ScatterPlotStateWriter(activeState)).executeLater(activeGraph);
        }

        return state;
    }

    @Override
    protected ScatterPlotPane createContent() {
        return scatterPlotPane;
    }

    @Override
    protected String createStyle() {
        return JavafxStyleManager.isDarkTheme()
            ? "resources/scatter-pane-dark.css"
            : "resources/scatter-pane-light.css";
    }

    @Override
    protected void handleNewGraph(final Graph graph) {
        if (!needsUpdate()) {
            return;
        }
        if (graph == null) {
            scatterPlotPane.getOptionsPane().disableOptions();
            refreshHandler.accept(graph);
            currentGraph = null;
        } else {
            currentGraph = graph;
            refreshHandler.accept(graph);
            scatterPlotPane.getOptionsPane().enableOptions();
            scatterPlotPane.getOptionsPane().refreshOptions(currentState);
        }
    }

    @Override
    protected void componentShowing() {
        super.componentShowing();
        refreshHandler.accept(currentGraph);
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
}
