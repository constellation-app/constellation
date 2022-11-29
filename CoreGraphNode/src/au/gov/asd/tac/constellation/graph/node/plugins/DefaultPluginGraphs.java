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
package au.gov.asd.tac.constellation.graph.node.plugins;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import java.util.Map;

/**
 * interface for graph plugins
 *
 * @author sirius
 */
public class DefaultPluginGraphs implements PluginGraphs {

    private final PluginManager pluginManager;

    public DefaultPluginGraphs(final PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    @Override
    public Graph getGraph() {
        return pluginManager.getGraph();
    }

    @Override
    public Map<String, Graph> getAllGraphs() {
        return GraphNode.getAllGraphs();
    }

    @Override
    public Graph createNewGraph(final String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setSilent(final boolean silent) {
        ThreadConstraints threadConstraints = ThreadConstraints.getConstraints();
        final int silentCount = threadConstraints.getSilentCount();
        if (silent) {
            threadConstraints.setSilentCount(silentCount + 1);
        } else {
            threadConstraints.setSilentCount(silentCount - 1);
        }
    }

    @Override
    public boolean graphExists(final String id) {
        for (Graph graph : GraphNode.getAllGraphs().values()) {
            if (graph.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void waitAtGate(int gate) throws InterruptedException {
        if (pluginManager.getSynchronizer() != null) {
            pluginManager.getSynchronizer().waitForGate(gate);
        }
    }
}
