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
package au.gov.asd.tac.constellation.graph.monitor;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.manager.GraphManagerListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sirius
 */
public class MonitorManager implements GraphManagerListener, GraphChangeListener {

    private static final Logger LOGGER = Logger.getLogger(MonitorManager.class.getName());

    private static final boolean VERBOSE = false;

    private final int requiredStartCount;
    private int currentStartCount = 0;

    private Graph currentGraph = null;

    private final List<MonitorEntry> monitorEntries = new ArrayList<>();
    private final List<ListenerEntry> listenerEntries = new ArrayList<>();

    private long latestEventId = -1L;

    public MonitorManager() {
        this(1);
    }

    public MonitorManager(final int requiredStartCount) {
        this.requiredStartCount = requiredStartCount;
    }

    public MonitorManager start() {
        if (++currentStartCount == requiredStartCount) {
            if (VERBOSE) {
                LOGGER.log(Level.INFO,"@@MONITOR_MANAGER::starting");
            }
            newActiveGraph(GraphManager.getDefault().getActiveGraph());
            GraphManager.getDefault().addGraphManagerListener(this);
        }
        return this;
    }

    public MonitorManager stop() {
        if (currentStartCount-- == requiredStartCount) {
            if (VERBOSE) {
                LOGGER.log(Level.INFO,"@@MONITOR_MANAGER::stopping");
            }
            GraphManager.getDefault().removeGraphManagerListener(this);
            newActiveGraph(null);
        }
        return this;
    }

    @Override
    public void graphOpened(final Graph graph) {
        // Required for implementation of GraphManagerListener
    }

    @Override
    public void graphClosed(final Graph graph) {
        // Required for implementation of GraphManagerListener
    }

    public Graph getCurrentGraph() {
        return currentGraph;
    }

    @Override
    public void newActiveGraph(final Graph graph) {
        if (VERBOSE) {
            LOGGER.log(Level.INFO,"@@MONITOR_MANAGER::newActiveGraph()");
        }

        if (graph != currentGraph) {
            if (currentGraph != null) {
                currentGraph.removeGraphChangeListener(this);
            }
            currentGraph = graph;
            if (currentGraph != null) {
                currentGraph.addGraphChangeListener(this);
            }

            latestEventId = -1L;
            updateMonitors(currentGraph, true);
        }
    }

    @Override
    public void graphChanged(final GraphChangeEvent event) {
        if (VERBOSE) {
            LOGGER.log(Level.INFO,"@@MONITOR_MANAGER::graphChanged()");
        }
        final long latestId = event.getLatest().getId();
        if (latestId > latestEventId) {
            latestEventId = latestId;
            updateMonitors(event.getGraph(), false);
        }
    }

    private void updateMonitors(final Graph graph, final boolean newGraph) {
        if (VERBOSE) {
            LOGGER.log(Level.INFO,"@@MONITOR_MANAGER::updateMonitors()");
        }
        final ReadableGraph rg = graph == null ? null : graph.getReadableGraph();
        try {
            for (final MonitorEntry monitorEntry : monitorEntries) {
                if (VERBOSE) {
                    LOGGER.log(Level.INFO,"@@MONITOR_MANAGER::updatingMonitor({0})", monitorEntry.monitor);
                }
                monitorEntry.monitor.update(rg);
            }

            for (final ListenerEntry listenerEntry : listenerEntries) {
                int updateCount = 0;
                for (final MonitorTest test : listenerEntry.tests) {
                    if (VERBOSE) {
                        final String log = String.format("@@MONITOR_MANAGER::testingMonitor(" + test.listenerEntry.listener + ", " + test.monitorEntry.monitor + ", " + test.filter + ")");
                        LOGGER.log(Level.INFO, log);
                    }
                    if (test.filter.matchesTransition(test.monitorEntry.monitor)) {
                        if (VERBOSE) {
                            final String log = String.format("@@MONITOR_MANAGER::listenerAlerted(" + test.listenerEntry.listener + ", " + test.monitorEntry.monitor + ", " + test.filter + ")");
                            LOGGER.log(Level.INFO, log);
                        }
                        listenerEntry.listener.monitorUpdated(this, test.monitorEntry.monitor, rg, newGraph, ++updateCount);
                    }
                }
            }
        } finally {
            if (rg != null) {
                rg.release();
            }
        }
    }

    public void addMonitorListener(final MonitorListener listener, final MonitorTransitionFilter filter, final Monitor... monitors) {
        // Find the listener entry or create a new one
        ListenerEntry listenerEntry = null;
        for (final ListenerEntry entry : listenerEntries) {
            if (entry.listener == listener) {
                listenerEntry = entry;
                break;
            }
        }
        if (listenerEntry == null) {
            listenerEntry = new ListenerEntry(listener);
            listenerEntries.add(listenerEntry);
        }

        for (final Monitor monitor : monitors) {
            MonitorEntry monitorEntry = null;
            for (final MonitorEntry entry : monitorEntries) {
                if (entry.monitor == monitor) {
                    monitorEntry = entry;
                    break;
                }
            }
            if (monitorEntry == null) {
                monitorEntry = new MonitorEntry(monitor);
                monitorEntries.add(monitorEntry);
            }

            final MonitorTest test = new MonitorTest(listenerEntry, monitorEntry, filter);
            monitorEntry.tests.add(test);
            listenerEntry.tests.add(test);
        }
    }

    public void removeMonitorListener(final MonitorListener listener) {
        ListenerEntry listenerEntry = null;
        for (final ListenerEntry entry : listenerEntries) {
            if (entry.listener == listener) {
                listenerEntry = entry;
                break;
            }
        }
        if (listenerEntry != null) {
            for (final MonitorTest test : listenerEntry.tests) {
                test.monitorEntry.tests.remove(test);
                if (test.monitorEntry.tests.isEmpty()) {
                    monitorEntries.remove(test.monitorEntry);
                }
            }
            listenerEntries.remove(listenerEntry);
        }
    }

    public void removeMonitor(final Monitor monitor) {
        MonitorEntry monitorEntry = null;
        for (final MonitorEntry entry : monitorEntries) {
            if (entry.monitor == monitor) {
                monitorEntry = entry;
                break;
            }
        }
        if (monitorEntry != null) {
            for (final MonitorTest test : monitorEntry.tests) {
                test.listenerEntry.tests.remove(test);
                if (test.listenerEntry.tests.isEmpty()) {
                    listenerEntries.remove(test.listenerEntry);
                }
            }
            monitorEntries.remove(monitorEntry);
        }
    }

    private static final class MonitorEntry {

        private final Monitor monitor;
        private List<MonitorTest> tests = new ArrayList<>();

        public MonitorEntry(final Monitor monitor) {
            this.monitor = monitor;
        }
    }

    private static final class ListenerEntry {

        private final MonitorListener listener;
        private List<MonitorTest> tests = new ArrayList<>();

        public ListenerEntry(final MonitorListener listener) {
            this.listener = listener;
        }
    }

    private static final class MonitorTest {

        private final ListenerEntry listenerEntry;
        private final MonitorEntry monitorEntry;
        private final MonitorTransitionFilter filter;

        public MonitorTest(final ListenerEntry listenerEntry, final MonitorEntry monitorEntry, final MonitorTransitionFilter filter) {
            this.listenerEntry = listenerEntry;
            this.monitorEntry = monitorEntry;
            this.filter = filter;
        }
    }
}
