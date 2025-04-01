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
package au.gov.asd.tac.constellation.views.analyticview;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeEvent;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import au.gov.asd.tac.constellation.views.JavaFxTopComponent;
import au.gov.asd.tac.constellation.views.analyticview.analytics.AnalyticPlugin;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
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
    
    private static final String ANALYTIC_VIEW_GRAPH_CHANGED_THREAD_NAME = "Analytic View Graph Changed Updater";
    private final AnalyticViewPane analyticViewPane;
    private final AnalyticViewController analyticController;
    private boolean suppressed = false;
    private String currentGraphId = StringUtils.EMPTY;
    private LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<>();
    private Thread refreshThread;
    private final Runnable refreshRunnable;
    private long latestGraphChangeID = 0;
    private Graph activeGraph;

    public AnalyticViewTopComponent() {
        super();
        this.analyticController = AnalyticViewController.getDefault().init(this);
        this.analyticViewPane = new AnalyticViewPane(analyticController);
        initComponents();
        setName(Bundle.CTL_AnalyticViewTopComponent());
        setToolTipText(Bundle.HINT_AnalyticViewTopComponent());
        super.initContent();

        // analytic view specific listeners
        addStructureChangeHandler(graph -> {
            if (needsUpdate() && !suppressed) {
                analyticController.writeState();
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
        addIgnoredEvent(AnalyticViewController.SELECT_ON_GRAPH_PLUGIN_NAME);

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
        
        refreshRunnable = () -> {
            final List<Object> devNull = new ArrayList<>();
            while (!queue.isEmpty()) {
                queue.drainTo(devNull);
            }

            // update analytic view pane
            analyticController.readState();
        };
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
                : "resources/analytic-view-light.css";
    }

    @Override
    protected void handleNewGraph(final Graph graph) {
        if (needsUpdate() && graph != null) {
            currentGraphId = graph.getId();
            activeGraph = graph;

            if (analyticViewPane != null) {
                analyticViewPane.reset();
                analyticViewPane.setIsRunnable(true);
                analyticController.readState();
                if (GraphManager.getDefault().getActiveGraph() == null) {
                    analyticViewPane.reset();
                }
            }
        }
    }

    @Override
    protected void handleGraphOpened(final Graph graph) {
        if (graph != null) {
            currentGraphId = graph.getId();
        }
        if (analyticViewPane != null) {
            analyticViewPane.reset();
            analyticController.readState();
            activeGraph = GraphManager.getDefault().getActiveGraph();
            if (activeGraph == null) {
                analyticViewPane.reset();
            }
        }
    }

    @Override
    protected void handleComponentOpened() {
        super.handleComponentOpened();
        activeGraph = GraphManager.getDefault().getActiveGraph();
        if (activeGraph != null) {
            currentGraphId = activeGraph.getId();
        }
        analyticController.readState();

        if (activeGraph == null) {
            analyticViewPane.reset();
        }
    }

    @Override
    protected void componentShowing() {
        super.componentShowing();
        activeGraph = GraphManager.getDefault().getActiveGraph();
        if (activeGraph != null && !activeGraph.getId().equals(currentGraphId)) {
            analyticViewPane.reset();
        }
        analyticController.readState();
        handleNewGraph(activeGraph);
    }
    
    @Override
    protected void handleGraphChange(final GraphChangeEvent event) {
        if (event == null) { // can be null at this point in time
            return;
        }
        final GraphChangeEvent newEvent = event.getLatest();
        if (newEvent == null) { // latest event may be null - defensive check
            return;
        }
        if (newEvent.getId() > latestGraphChangeID) {
            latestGraphChangeID = newEvent.getId();
            if (activeGraph != null) {
                queue.add(newEvent);
                if (refreshThread == null || !refreshThread.isAlive()) {
                    refreshThread = new Thread(refreshRunnable);
                    refreshThread.setName(ANALYTIC_VIEW_GRAPH_CHANGED_THREAD_NAME);
                    refreshThread.start();
                }
            }
        }
    }

    @Override
    protected void handleComponentClosed() {
        super.handleComponentClosed();
        analyticViewPane.reset();
    }
}
