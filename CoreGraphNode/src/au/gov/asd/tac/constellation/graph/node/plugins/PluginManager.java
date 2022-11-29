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
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginSynchronizer;

/**
 * all plugins are registers with the manager
 *
 * @author sirius
 */
public class PluginManager {

    private final DefaultPluginEnvironment environment;
    private final PluginSynchronizer synchronizer;
    private final Plugin plugin;
    private final Graph graph;
    private final String graphId;
    private final Thread pluginThread;
    private final boolean interactive;

    public PluginManager(final DefaultPluginEnvironment environment, final Plugin plugin, final Graph graph, final boolean interactive, PluginSynchronizer synchronizer) {
        this.pluginThread = Thread.currentThread();
        this.environment = environment;
        this.synchronizer = synchronizer;
        this.plugin = plugin;
        this.graph = graph;
        this.graphId = graph == null ? null : graph.getId();
        this.interactive = interactive;
    }

    public PluginManager(final DefaultPluginEnvironment environment, final Plugin plugin, final GraphReadMethods graph, final boolean interactive, PluginSynchronizer synchronizer) {
        this.pluginThread = Thread.currentThread();
        this.environment = environment;
        this.synchronizer = synchronizer;
        this.plugin = plugin;
        this.graph = null;
        this.graphId = graph == null ? null : graph.getId();
        this.interactive = interactive;
    }

    public DefaultPluginEnvironment getEnvironment() {
        return environment;
    }

    public PluginSynchronizer getSynchronizer() {
        return synchronizer;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public Graph getGraph() {
        return graph;
    }

    public GraphNode getGraphNode() {
        return GraphNode.getGraphNode(graphId);
    }

    public Thread getPluginThread() {
        return pluginThread;
    }

    public boolean isInteractive() {
        return interactive;
    }
}
