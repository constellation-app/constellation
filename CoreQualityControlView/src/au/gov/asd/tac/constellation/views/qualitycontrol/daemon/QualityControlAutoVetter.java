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
package au.gov.asd.tac.constellation.views.qualitycontrol.daemon;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.manager.GraphManagerListener;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeEvent;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeListener;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.views.qualitycontrol.rules.QualityControlRule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Lookup;

/**
 * Quality control vetter which listens to graph changes and rates objects in a graph for quality.
 * <p>
 * When the graph or graph quality control changes, listeners will be notified.
 *
 * @author algol
 */
public final class QualityControlAutoVetter implements GraphManagerListener, GraphChangeListener {

    private static final Logger LOGGER = Logger.getLogger(QualityControlAutoVetter.class.getName());

    private static QualityControlAutoVetter instance = null;
    private static final List<QualityControlAutoVetterListener> buttonListeners = new ArrayList<>();

    private QualityControlState state;

    private Graph currentGraph;
    private long lastGlobalModificationCounter;
    private long lastCameraModificationCounter;
    private long lastAttributeModificationCounter;

    private final List<QualityControlListener> listeners;

    private static List<QualityControlRule> rules = null;
    private static List<QualityControlRule> uRules = null;

    /**
     * Constructor for QualityControlAutoVetter.
     */
    private QualityControlAutoVetter() {
        listeners = new ArrayList<>();

        currentGraph = null;
        state = new QualityControlState(null, new ArrayList<>(), new ArrayList<>());

        GraphManager.getDefault().addGraphManagerListener(this);
    }

    /**
     * Add a listener for the current open graph, if any
     */
    public void init() {
        initWithRefresh(false);
    }

    /**
     * Add a listener for the current open graph, if any
     *
     * @param forceRefresh force the view to refresh
     */
    public void initWithRefresh(final boolean forceRefresh) {
        if (forceRefresh) {
            currentGraph = null;
        }

        newActiveGraph(GraphManager.getDefault().getActiveGraph());
    }

    @Override
    public void graphOpened(final Graph graph) {
        // Method intentionally left blank
    }

    @Override
    public void graphClosed(final Graph graph) {
        newActiveGraph(null);

        // remove dangling reference to the graph
        currentGraph = null;

        // inform all listeners to clear any Quality Control data
        setQualityControlState(null);
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
     * Rather than just check the graph every time it changes, we do some optimisation; we only update quality control
     * if something (possibly) relevant has changed. For instance, changing colors has no effect on quality control.
     * <p>
     * <b>IMPORTANT</b>: the set of attributes checked here MUST contain the attributes that are checked in
     * {@link #updateQualityControlState} below, otherwise changes of relevant values won't cause a quality control
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
                final int cameraAttribute = VisualConcept.GraphAttribute.CAMERA.get(readableGraph);

                final long thisGlobalModificationCounter = readableGraph.getGlobalModificationCounter();
                final long thisCameraModificationCounter = readableGraph.getValueModificationCounter(cameraAttribute);
                final long thisAttributeModificationCounter = readableGraph.getAttributeModificationCounter();

                if (thisGlobalModificationCounter != lastGlobalModificationCounter) {
                    if (lastGlobalModificationCounter == -1 || lastCameraModificationCounter == thisCameraModificationCounter || lastAttributeModificationCounter != thisAttributeModificationCounter) {
                        updateQualityControlState(graph);
                    }
                    lastGlobalModificationCounter = thisGlobalModificationCounter;
                    lastCameraModificationCounter = thisCameraModificationCounter;
                    lastAttributeModificationCounter = thisAttributeModificationCounter;
                }
            } finally {
                readableGraph.release();
            }
        }
    }

    protected Graph getCurrentGraph() {
        return currentGraph;
    }

    protected long getlastGlobalModCount() {
        return lastGlobalModificationCounter;
    }

    protected long getlastCameraModCount() {
        return lastCameraModificationCounter;
    }

    /**
     * Triggers an update of the QualityControlEvent as well as notifies listeners. This is used when the priority of
     * categories is changed.
     */
    public void updateQualityEvents() {
        final Graph graph = currentGraph;
        if (graph != null) {
            final ReadableGraph readableGraph = graph.getReadableGraph();
            try {
                updateQualityControlState(graph);
            } finally {
                readableGraph.release();
            }
            listeners.stream().forEach(listener -> listener.qualityControlChanged(state));
        }
    }

    /**
     * Build a new QualityControlState with an updated list of QualityControlEvent.
     *
     * @param graph The graph to vet for quality control, may be null if there is no current graph.
     */
    public static void updateQualityControlState(final Graph graph) {
        // notify listeners that rules are running
        buttonListeners.stream().forEach(listener -> listener.qualityControlRuleChanged(false));

        final Future<?> stateFuture = PluginExecution.withPlugin(
                new QualityControlStateUpdater()
        ).executeLater(graph);

        try {
            if (stateFuture != null) {
                stateFuture.get();
            }
        } catch (final InterruptedException ex) {
            LOGGER.log(Level.SEVERE, "Quality Control State updater was interrupted", ex);
            Thread.currentThread().interrupt();
        } catch (final ExecutionException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }

        // notify listeners that rules have finished
        buttonListeners.stream().forEach(listener -> listener.qualityControlRuleChanged(true));
    }

    protected static List<QualityControlRule> getRules() {
        if (rules == null) {
            rules = new ArrayList<>(Lookup.getDefault().lookupAll(QualityControlRule.class));
            uRules = Collections.unmodifiableList(rules);
        }

        return uRules;
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
     * Add a {@link QualityControlListener} which will be notified when the quality control state changes.
     * <p>
     * The listener will immediately be called back with the current state.
     * <p>
     * When calling this method, a separate call to {@code init()} should also be made if you want the listener to
     * pickup existing graphs.
     *
     * @param listener The listener to register.
     */
    public void addListener(final QualityControlListener listener) {
        listeners.add(listener);

        // If we have a list size of 1 then this is the first entry added so
        // its time to re-add a change listener to currentGraph.
        if (listeners.size() == 1 && currentGraph != null) {
            currentGraph.addGraphChangeListener(this);
        }
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

        // After removing a listener, check if there are any further listeners
        // still using the auto vetter - if there are not, stop listening for
        // currentGraph change. This can be reactivated if listeners are again
        // added.
        if (listeners.isEmpty() && currentGraph != null) {
            currentGraph.removeGraphChangeListener(this);
        }
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
        if (instance == null) {
            instance = new QualityControlAutoVetter();
        }
        return instance;
    }

    protected static synchronized void destroyInstance() {
        GraphManager.getDefault().removeGraphManagerListener(instance);
        instance = null;
    }
}
