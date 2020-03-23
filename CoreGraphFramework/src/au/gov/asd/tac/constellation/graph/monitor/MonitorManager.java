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
package au.gov.asd.tac.constellation.graph.monitor;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.manager.GraphManagerListener;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sirius
 */
public class MonitorManager implements GraphManagerListener, GraphChangeListener {

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

    public MonitorManager(int requiredStartCount) {
        this.requiredStartCount = requiredStartCount;
    }

    public MonitorManager start() {
        if (++currentStartCount == requiredStartCount) {
            if (VERBOSE) {
                System.out.println("@@MONITOR_MANAGER::starting");
            }
            newActiveGraph(GraphManager.getDefault().getActiveGraph());
            GraphManager.getDefault().addGraphManagerListener(this);
        }
        return this;
    }

    public MonitorManager stop() {
        if (currentStartCount-- == requiredStartCount) {
            if (VERBOSE) {
                System.out.println("@@MONITOR_MANAGER::stopping");
            }
            GraphManager.getDefault().removeGraphManagerListener(this);
            newActiveGraph(null);
        }
        return this;
    }

    @Override
    public void graphOpened(Graph graph) {
    }

    @Override
    public void graphClosed(Graph graph) {
    }

    public Graph getCurrentGraph() {
        return currentGraph;
    }

    @Override
    public void newActiveGraph(Graph graph) {
        if (VERBOSE) {
            System.out.println("@@MONITOR_MANAGER::newActiveGraph()");
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
    public void graphChanged(GraphChangeEvent event) {
        if (VERBOSE) {
            System.out.println("@@MONITOR_MANAGER::graphChanged()");
        }
        final long latestId = event.getLatest().getId();
        if (latestId > latestEventId) {
            latestEventId = latestId;
            updateMonitors(event.getGraph(), false);
        }
    }

    private void updateMonitors(Graph graph, boolean newGraph) {
        if (VERBOSE) {
            System.out.println("@@MONITOR_MANAGER::updateMonitors()");
        }
        final ReadableGraph rg = graph == null ? null : graph.getReadableGraph();
        try {
            for (MonitorEntry monitorEntry : monitorEntries) {
                if (VERBOSE) {
                    System.out.println("@@MONITOR_MANAGER::updatingMonitor(" + monitorEntry.monitor + ")");
                }
                monitorEntry.monitor.update(rg);
            }

            for (ListenerEntry listenerEntry : listenerEntries) {
                int updateCount = 0;
                for (MonitorTest test : listenerEntry.tests) {
                    if (VERBOSE) {
                        System.out.println("@@MONITOR_MANAGER::testingMonitor(" + test.listenerEntry.listener + ", " + test.monitorEntry.monitor + ", " + test.filter + ")");
                    }
                    if (test.filter.matchesTransition(test.monitorEntry.monitor)) {
                        if (VERBOSE) {
                            System.out.println("@@MONITOR_MANAGER::listenerAlerted(" + test.listenerEntry.listener + ", " + test.monitorEntry.monitor + ", " + test.filter + ")");
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

    public void addMonitorListener(MonitorListener listener, MonitorTransitionFilter filter, Monitor... monitors) {

        // Find the listener entry or create a new one
        ListenerEntry listenerEntry = null;
        for (ListenerEntry entry : listenerEntries) {
            if (entry.listener == listener) {
                listenerEntry = entry;
                break;
            }
        }
        if (listenerEntry == null) {
            listenerEntry = new ListenerEntry(listener);
            listenerEntries.add(listenerEntry);
        }

        for (Monitor monitor : monitors) {

            MonitorEntry monitorEntry = null;
            for (MonitorEntry entry : monitorEntries) {
                if (entry.monitor == monitor) {
                    monitorEntry = entry;
                    break;
                }
            }
            if (monitorEntry == null) {
                monitorEntry = new MonitorEntry(monitor);
                monitorEntries.add(monitorEntry);
            }

            MonitorTest test = new MonitorTest(listenerEntry, monitorEntry, filter);
            monitorEntry.tests.add(test);
            listenerEntry.tests.add(test);
        }
    }

    public void removeMonitorListener(MonitorListener listener) {
        ListenerEntry listenerEntry = null;
        for (ListenerEntry entry : listenerEntries) {
            if (entry.listener == listener) {
                listenerEntry = entry;
                break;
            }
        }
        if (listenerEntry != null) {
            for (MonitorTest test : listenerEntry.tests) {
                test.monitorEntry.tests.remove(test);
                if (test.monitorEntry.tests.isEmpty()) {
                    monitorEntries.remove(test.monitorEntry);
                }
            }
            listenerEntries.remove(listenerEntry);
        }
    }

    public void removeMonitor(Monitor monitor) {
        MonitorEntry monitorEntry = null;
        for (MonitorEntry entry : monitorEntries) {
            if (entry.monitor == monitor) {
                monitorEntry = entry;
                break;
            }
        }
        if (monitorEntry != null) {
            for (MonitorTest test : monitorEntry.tests) {
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

        public MonitorEntry(Monitor monitor) {
            this.monitor = monitor;
        }
    }

    private static final class ListenerEntry {

        private final MonitorListener listener;
        private List<MonitorTest> tests = new ArrayList<>();

        public ListenerEntry(MonitorListener listener) {
            this.listener = listener;
        }
    }

    private static final class MonitorTest {

        private final ListenerEntry listenerEntry;
        private final MonitorEntry monitorEntry;
        private final MonitorTransitionFilter filter;

        public MonitorTest(ListenerEntry listenerEntry, MonitorEntry monitorEntry, MonitorTransitionFilter filter) {
            this.listenerEntry = listenerEntry;
            this.monitorEntry = monitorEntry;
            this.filter = filter;
        }
    }
}
