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
package au.gov.asd.tac.constellation.graph.monitor;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.manager.GraphManagerListener;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sirius
 */
public class ListeningGraphMonitor extends ManualGraphMonitor implements GraphChangeListener, GraphManagerListener {

    private final AtomicInteger manageCount = new AtomicInteger(0);
    private final int requiredManageCount;
    private static final Logger LOGGER = Logger.getLogger(ListeningGraphMonitor.class.getName());

    // The currently active graph
    private Graph activeGraph;

    // The most recent event that this monitor has seen
    private long lastEvent = Long.MIN_VALUE;

    private boolean managed = false;

    private ListenerRecord activeGraphListener;

    public ListeningGraphMonitor() {
        requiredManageCount = 1;
    }

    public ListeningGraphMonitor(final int requiredManageCount) {
        this.requiredManageCount = requiredManageCount;
    }

    /**
     * Sets the listener that gets called when the active graph changes.
     *
     * @param listener the new listener.
     */
    public final void setActiveGraphListener(final GraphMonitorListener listener) {
        if (activeGraphListener != null) {
            removeListener(activeGraphListener);
        }
        activeGraphListener = listener == null ? null : addListener(listener);
    }

    /**
     * Start listening for changes on a specified graph.
     *
     * @param graph the graph to start listening to.
     */
    public void setGraph(final Graph graph) {
        if (graph != activeGraph) {
            if (VERBOSE) {
                LOGGER.log(Level.INFO,"Graph Monitor: activeGraphChanged()");
            }

            if (activeGraph != null) {
                activeGraph.removeGraphChangeListener(this);
            }

            activeGraph = graph;

            if (activeGraph != null) {
                try (final ReadableGraph rg = activeGraph.getReadableGraph()) {
                    setGraph(rg);

                    if (activeGraphListener != null) {
                        activeGraphListener.graphChangedAlways(rg);
                    }
                }

                activeGraph.addGraphChangeListener(this);
            } else if (activeGraphListener != null) {
                activeGraphListener.graphChangedAlways(null);
            }
        }
    }

    /**
     * Returns the graph that this graph monitor is currently listening to.
     *
     * @return the graph that this graph monitor is currently listening to.
     */
    public Graph getActiveGraph() {
        return activeGraph;
    }

    @Override
    public void graphChanged(final GraphChangeEvent event) {
        if (VERBOSE) {
            LOGGER.log(Level.INFO,"Graph Monitor: graphChanged()");
        }

        final GraphChangeEvent latestEvent = event.getLatest();
        if (latestEvent.getId() > lastEvent) {
            lastEvent = latestEvent.getId();  
            
            try (final ReadableGraph rg = latestEvent.getGraph().getReadableGraph()) {
                update(rg, true);
            }
        }
    }

    @Override
    public void graphOpened(final Graph graph) {
        // Required for GraphManagerListener
    }

    @Override
    public void graphClosed(final Graph graph) {
        // GraphManagerListener
    }

    @Override
    public void newActiveGraph(final Graph graph) {
        setGraph(graph);
    }

    /**
     * Sets whether this GraphMonitor is managed or unmanaged.
     *
     * If managed, the GraphMonitor will listen to the GraphManager and keep its
     * active graph up-to-date with the currently active graph in the
     * application.
     *
     * If unmanaged, it is the responsibility of the called to set the active
     * graph manually.
     *
     * @param managed specifies if the monitor should become managed or
     * unmanaged.
     */
    public void setManaged(final boolean managed) {
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
}
