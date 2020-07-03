/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.qualitycontrol.daemon;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.manager.GraphManagerListener;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeEvent;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeListener;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.plugins.templates.SimpleReadPlugin;
import au.gov.asd.tac.constellation.views.qualitycontrol.QualityControlEvent;
import au.gov.asd.tac.constellation.views.qualitycontrol.rules.QualityControlRule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Quality control vetter which listens to graph changes and rates objects in a
 * graph for quality.
 * <p>
 * When the graph or graph quality control changes, listeners will be notified.
 *
 * @author algol
 */
public final class QualityControlAutoVetter implements GraphManagerListener, GraphChangeListener {

    private static QualityControlAutoVetter INSTANCE = null;

    private static final List<QualityControlAutoVetterListener> buttonListeners = new ArrayList<>();

    private QualityControlState state;

    private Graph currentGraph;
    private long lastGlobalModificationCounter;

    private final List<QualityControlListener> listeners;

    private static List<QualityControlRule> RULES = null;
    private static List<QualityControlRule> U_RULES = null;

    /**
     * Constructor for QualityControlAutoVetter.
     */
    private QualityControlAutoVetter() {
        listeners = new ArrayList<>();

        currentGraph = null;
        state = new QualityControlState(null, new ArrayList<>(), new ArrayList<>());

        newActiveGraph(GraphManager.getDefault().getActiveGraph());
        GraphManager.getDefault().addGraphManagerListener(this);
    }

    @Override
    public void graphOpened(final Graph graph) {
        newActiveGraph(GraphManager.getDefault().getActiveGraph());
    }

    @Override
    public void graphClosed(final Graph graph) {
        newActiveGraph(GraphManager.getDefault().getActiveGraph());
    }

    @Override
    public void newActiveGraph(final Graph graph) {
        if (currentGraph != graph) {
            if (currentGraph != null) {
                currentGraph.removeGraphChangeListener(this);
                currentGraph = null;
            }

            currentGraph = graph;
            if (graph != null) {
                currentGraph.addGraphChangeListener(this);
            }

            lastGlobalModificationCounter = -1;

            graphChanged(null);
        }
    }

    /**
     * The graph has changed, so we might have to re-vet quality control.
     * <p>
     * Rather than just check the graph every time it changes, we do some
     * optimisation; we only update quality control if something (possibly)
     * relevant has changed. For instance, changing colors has no effect on
     * quality control.
     * <p>
     * <b>IMPORTANT</b>: the set of attributes checked here MUST contain the
     * attributes that are checked in {@link #updateQualityControlState} below,
     * otherwise changes of relevant values won't cause a quality control
     * re-vet.
     *
     * @param event The graph change event.
     */
    @Override
    public void graphChanged(final GraphChangeEvent event) {
        final Graph graph = currentGraph;
        if (graph != null) {
            final ReadableGraph readableGraph = graph.getReadableGraph();
            try {
                final long thisGlobalModificationCounter = readableGraph.getGlobalModificationCounter();
                if (thisGlobalModificationCounter != lastGlobalModificationCounter) {
                    updateQualityControlState(readableGraph);
                    lastGlobalModificationCounter = thisGlobalModificationCounter;
                } else {
                    return;
                }
            } finally {
                readableGraph.release();
            }
        } else {
            updateQualityControlState(null);
        }
    }

    /**
     * Build a new QualityControlState with an updated list of
     * QualityControlEvent.
     *
     * @param graph The graph to vet for quality control, may be null if there
     * is no current graph.
     */
    public static void updateQualityControlState(final GraphReadMethods graph) {
        // notify listeners that rules are running
        buttonListeners.stream().forEach(listener -> listener.qualityControlRuleChanged(false));

        final Graph currentGraph = GraphManager.getDefault().getActiveGraph();
        Future<?> stateFuture = PluginExecution.withPlugin(new QualityControlViewStateUpdater(graph))
                .executeLater(currentGraph);

        try {
            if (stateFuture != null) {
                stateFuture.get();
            }
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
        // notify listeners that rules have finished
        buttonListeners.stream().forEach(listener -> listener.qualityControlRuleChanged(true));
    }

    private static List<QualityControlRule> getRules() {
        if (RULES == null) {
            RULES = new ArrayList<>(Lookup.getDefault().lookupAll(QualityControlRule.class));
            U_RULES = Collections.unmodifiableList(RULES);
        }

        return U_RULES;
    }

    /**
     * The current quality control state.
     *
     * @return The current quality control state.
     */
    public QualityControlState getQualityControlState() {
        return state;
    }

    public void setQualityControlState(final QualityControlState state) {
        this.state = state;
        listeners.stream().forEach(listener -> listener.qualityControlChanged(state));
    }

    /**
     * Add a {@link QualityControlListener} which will be notified when the
     * quality control state changes.
     * <p>
     * The listener will immediately be called back with the current state.
     *
     * @param listener The listener to register.
     */
    public void addListener(final QualityControlListener listener) {
        listeners.add(listener);
    }

    /**
     * Manually cause this listener to be called.
     * <p>
     * This is useful to get the current state immediately.
     *
     * @param listener The listener to invoke.
     */
    public void invokeListener(final QualityControlListener listener) {
        listener.qualityControlChanged(state);
    }

    /**
     * Remove the specified listener from the list of listeners.
     *
     * @param listener The listener to be removed.
     */
    public void removeListener(final QualityControlListener listener) {
        listeners.remove(listener);
    }

    public void addObserver(final QualityControlAutoVetterListener buttonListener) {
        buttonListeners.add(buttonListener);
    }

    public void removeObserver(final QualityControlAutoVetterListener buttonListener) {
        buttonListeners.remove(buttonListener);
    }

    /**
     * Get singleton instance of QualityControlAutoVetter
     *
     * @return singleton instance of QualityControlAutoVetter
     */
    public static synchronized QualityControlAutoVetter getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new QualityControlAutoVetter();
        }
        return INSTANCE;
    }

    private static final class QualityControlViewStateUpdater extends SimpleReadPlugin {

        private final GraphReadMethods readableGraph;

        public QualityControlViewStateUpdater(final GraphReadMethods graph) {
            this.readableGraph = graph;
        }

        @Override
        public void read(final GraphReadMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            final List<QualityControlRule> registeredRules = new ArrayList<>();
            final List<Integer> vertexList = new ArrayList<>();
            final List<String> identifierList = new ArrayList<>();
            final List<SchemaVertexType> typeList = new ArrayList<>();

            if (readableGraph != null) {
                final int selectedAttribute = VisualConcept.VertexAttribute.SELECTED.get(graph);
                final int identifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.get(graph);
                final int typeAttribute = AnalyticConcept.VertexAttribute.TYPE.get(graph);

                if (selectedAttribute != Graph.NOT_FOUND && identifierAttribute
                        != Graph.NOT_FOUND && typeAttribute != Graph.NOT_FOUND) {
                    final int vxCount = graph.getVertexCount();
                    for (int position = 0; position < vxCount; position++) {
                        final int vertex = graph.getVertex(position);
                        final String identifier = graph.getStringValue(identifierAttribute, vertex);
                        final SchemaVertexType type = graph.getObjectValue(typeAttribute, vertex);
                        final boolean selected = graph.getBooleanValue(selectedAttribute, vertex);

                        if (selected) {
                            vertexList.add(vertex);
                            identifierList.add(identifier);
                            typeList.add(type);
                        }
                    }
                }

                // Set up and run each rule.
                if (!vertexList.isEmpty()) {
                    for (final QualityControlRule rule : getRules()) {
                        final Thread t = new Thread(() -> {
                            rule.clearResults();
                            rule.executeRule(graph, vertexList);
                            registeredRules.add(rule);
                        });
                        t.start();
                    }
                }
            }
            final Graph currentGraph = GraphManager.getDefault().getActiveGraph();
            PluginExecution.withPlugin(new QualityControlViewStateSaver(vertexList, identifierList, typeList, registeredRules))
                    .executeLater(currentGraph);
        }

        @Override
        public String getName() {
            return "Quality Control View: Run Rule";
        }
    }

    private static final class QualityControlViewStateSaver extends SimpleEditPlugin {

        private final List<Integer> vertexList;
        private final List<String> identifierList;
        private final List<SchemaVertexType> typeList;
        private final List<QualityControlRule> registeredRules;

        public QualityControlViewStateSaver(final List<Integer> vertexList, final List<String> identifierList, final List<SchemaVertexType> typeList, final List<QualityControlRule> registeredRules) {
            this.vertexList = vertexList;
            this.identifierList = identifierList;
            this.typeList = typeList;
            this.registeredRules = registeredRules;
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            // Build quality control events based on results of rules.
            // Sort by descending risk.
            final List<QualityControlEvent> qualityControlEvents = new ArrayList<>();
            for (int i = 0; i < vertexList.size(); i++) {
                final QualityControlEvent qualityControlEvent
                        = new QualityControlEvent(vertexList.get(i),
                                identifierList.get(i), typeList.get(i),
                                Collections.unmodifiableList(registeredRules));
                qualityControlEvents.add(qualityControlEvent);
            }
            Collections.sort(qualityControlEvents, Collections.reverseOrder());

            final String graphId = graph == null ? null : graph.getId();
            QualityControlAutoVetter.getInstance().setQualityControlState(
                    new QualityControlState(graphId, qualityControlEvents,
                            registeredRules));
        }

        @Override
        protected boolean isSignificant() {
            return true;
        }

        @Override
        public String getName() {
            return "Quality Control View: Save State";
        }
    }
}
