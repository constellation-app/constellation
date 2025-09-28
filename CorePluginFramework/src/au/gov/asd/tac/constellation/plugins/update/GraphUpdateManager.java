/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.update;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.manager.GraphManagerListener;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeEvent;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeListener;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

/**
 * This class is responsible for connecting CONSTELLATION's graph listening
 * framework with its graph update framework. An instance of GraphUpdateManager
 * can be used to listen to graph changes and inform its
 * {@link GraphUpdateController} of the changes. The controller will in turn
 * register changes with an {@link org.netbeans.api.autoupdate.UpdateManager} as
 * required.
 * <p>
 * It is important to note that
 * {@link GraphChangeListener#graphChanged graphChanged} is called on the EDT.
 * This means that all UpdateComponents that respond to graph changes will
 * update on the EDT unless they explicitly specify otherwise in
 * {@link UpdateComponent#updateThread updateThread}.
 * <p>
 * Typical use involves constructing a graph manager with a
 * GraphUpdateController and then calling
 * {@link GraphUpdateManager#setManaged setManaged(true)} to start listening to
 * the graph. There is also the option at creation time to specify how many
 * times this method needs to be called before the manager actually starts
 * listening to the graph. This is useful when a view needs to start responding
 * to graph changes only after two things have finished on separate threads
 * e.g., <code>componentOpened()</code> for a TopComponent on the EDT and some
 * initialisation code on the javafx thread.
 * <p>
 * To stop the manager from listening to the graph,
 * {@link GraphUpdateManager#setManaged setManaged(false)} should be called.
 * This is used, for example, when <code>componentClosed()</code> is called for
 * a TopComponent that has a GraphUpdateManager. Listening will only occur when
 * the number of <code>setManaged(true)</code> calls exceeds the sum of
 * <code>manageCount</code> and the number of <code>setManaged(false)</code>
 * calls.
 *
 * @see GraphChangeListener
 * @see GraphUpdateController
 * @author sirius
 */
public class GraphUpdateManager implements GraphChangeListener, GraphManagerListener {

    private final GraphUpdateController graphUpdateController;
    private final Predicate<GraphReadMethods> graphCondition;
    private final int requiredManageCount;

    private final AtomicInteger manageCount = new AtomicInteger(0);

    // The most recent event that this monitor has seen
    private long lastEvent = Long.MIN_VALUE;

    private boolean managed = false;

    private Graph activeGraph;

    /**
     * Creates a new GraphUpdateManager connected to the specified
     * GraphUpdateController. It will start listening to the graph after
     * setManaged has been called once.
     *
     * @param graphUpdateComponentController The GraphUpdateController to inform
     * of graph changes
     */
    public GraphUpdateManager(GraphUpdateController graphUpdateComponentController) {
        this(graphUpdateComponentController, 1);
    }

    public GraphUpdateManager(GraphUpdateController graphUpdateComponentController, Predicate<GraphReadMethods> graphCondition) {
        this(graphUpdateComponentController, 1, graphCondition);
    }

    /**
     * Creates a new GraphUpdateManager connected to the specified
     * GraphUpdateController that needs to receive the specified number of
     * {@link GraphUpdateManager#setManaged setManaged} requests before
     * listening commences.
     *
     * @param graphUpdateComponentController The GraphUpdateController to inform
     * of graph changes
     * @param requiredManagedCount The number of times setManaged() needs to be
     * called before listening commences.
     */
    public GraphUpdateManager(GraphUpdateController graphUpdateComponentController, int requiredManagedCount) {
        this(graphUpdateComponentController, requiredManagedCount, o -> true);
    }

    public GraphUpdateManager(GraphUpdateController graphUpdateComponentController, int requiredManagedCount, Predicate<GraphReadMethods> graphCondition) {
        this.graphUpdateController = graphUpdateComponentController;
        this.requiredManageCount = requiredManagedCount;
        this.graphCondition = graphCondition;
    }

    /**
     * Get the current graph that this manager is listening to
     *
     * @return The active Graph.
     */
    public Graph getActiveGraph() {
        return activeGraph;
    }

    @Override
    public void graphChanged(GraphChangeEvent event) {
        event = event.getLatest();
        if (event.getId() > lastEvent) {
            lastEvent = event.getId();

            ReadableGraph rg = event.getGraph().getReadableGraph();
            try {
                graphUpdateController.update(rg);
            } finally {
                rg.release();
            }
        }
    }

    @Override
    public void graphOpened(Graph graph) {
        // Require for GraphManagerListener, intentionally left blank
    }

    @Override
    public void graphClosed(Graph graph) {
        // Require for GraphManagerListener, intentionally left blank
    }

    @Override
    public void newActiveGraph(Graph graph) {
        setGraph(graph);
    }

    /**
     * Requests this manager to start or stop listening to the graph. Listening
     * will only occur when the number of <code>setManaged(true)</code> calls
     * exceeds the sum of <code>manageCount</code> and the number of
     * <code>setManaged(false)</code> calls.
     *
     * @param managed True if this is a request to listen to the graph, false if
     * it is a request to stop listening.
     */
    public void setManaged(boolean managed) {

        final int currentManageCount = managed ? manageCount.incrementAndGet() : manageCount.decrementAndGet();
        final boolean newManaged = currentManageCount >= requiredManageCount;

        if (this.managed != newManaged) {
            if (newManaged) {
                GraphManager.getDefault().addGraphManagerListener(this);
                setGraph(GraphManager.getDefault().getActiveGraph());
            } else {
                GraphManager.getDefault().removeGraphManagerListener(this);
                setGraph((Graph) null);
            }
            this.managed = newManaged;
        }
    }

    /**
     * Start listening for changes on the specified graph. This is usually
     * called as a consequence of
     * {@link GraphManagerListener#newActiveGraph newActiveGraph} however it may
     * also be invoked manually.
     *
     * @param graph the graph to start listening to.
     */
    public void setGraph(Graph graph) {
        if (graph != activeGraph) {

            if (activeGraph != null) {
                activeGraph.removeGraphChangeListener(this);
                isListening = false;
            }

            activeGraph = graph;
            testCurrentGraph();
        }
    }

    boolean isListening = false;

    public void testCurrentGraph() {
        if (activeGraph != null) {
            final ReadableGraph rg = activeGraph.getReadableGraph();
            try {
                if (graphCondition.test(rg)) {
                    if (!isListening) {
                        activeGraph.addGraphChangeListener(this);
                        graphUpdateController.update(rg);
                        isListening = true;
                    }
                } else {
                    if (isListening) {
                        activeGraph.removeGraphChangeListener(this);
                        graphUpdateController.update(null);
                        isListening = false;
                    }
                }
            } finally {
                rg.release();
            }
        } else {
            graphUpdateController.update(null);
        }
    }

}
