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
package au.gov.asd.tac.constellation.plugins.reporting;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.manager.GraphManagerListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author sirius
 */
public class GraphReportManager {

    private static final List<GraphReportListener> LISTENERS = new ArrayList<>();

    private static final Map<String, GraphReport> GRAPH_REPORTS = Collections.synchronizedMap(new HashMap<>());

    static {
        GraphManager.getDefault().addGraphManagerListener(new GraphManagerListener() {

            @Override
            public void graphOpened(Graph graph) {
                GRAPH_REPORTS.put(graph.getId(), new GraphReport(graph));
            }

            @Override
            public void graphClosed(Graph graph) {
                GRAPH_REPORTS.remove(graph.getId());
            }

            @Override
            public void newActiveGraph(Graph graph) {
                // Method required for GraphManagerListener, intentionally left blank
            }
        });

        for (Graph graph : GraphManager.getDefault().getAllGraphs().values()) {
            GRAPH_REPORTS.put(graph.getId(), new GraphReport(graph));
        }
    }

    public static synchronized void addGraphReportListener(GraphReportListener listener) {
        if (listener != null && !LISTENERS.contains(listener)) {
            LISTENERS.add(listener);
        }
    }

    public static synchronized void removeGraphReportListener(GraphReportListener listener) {
        LISTENERS.remove(listener);
    }

    static synchronized void fireNewPluginReport(PluginReport pluginReport) {
        LISTENERS.stream().forEach(listener -> listener.newPluginReport(pluginReport));
    }

    public static GraphReport getGraphReport(String graphId) {
        return GRAPH_REPORTS.get(graphId);
    }
}
