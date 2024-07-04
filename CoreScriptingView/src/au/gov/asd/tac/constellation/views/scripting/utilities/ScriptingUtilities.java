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
package au.gov.asd.tac.constellation.views.scripting.utilities;

import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.utilities.SubgraphUtilities;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginRegistry;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.views.scripting.ScriptingModule;
import au.gov.asd.tac.constellation.views.scripting.graph.SGraph;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.lookup.ServiceProvider;

/**
 * Core scripting utilities.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = ScriptingModule.class)
public class ScriptingUtilities implements ScriptingModule {
    
    private static final Logger LOGGER = Logger.getLogger(ScriptingUtilities.class.getName());

    @Override
    public String getName() {
        return "utilities";
    }

    /**
     * Provide a NetBeans open file dialog box that remembers where it was last
     * opened.
     *
     * @param dirKey A key to where the dialog box was last opened.
     *
     * @return A string containing a file name, or null if the user selected
     * Cancel.
     */
    public String openFile(final String dirKey) {
        final File file = new FileChooserBuilder(dirKey).showOpenDialog();
        return file != null ? file.getAbsolutePath() : null;
    }

    /**
     * Provide a map of graph name to graph for every graph currently open in
     * Constellation.
     *
     * @return a map of graph name to graph as a {@link SGraph}.
     */
    public Map<String, SGraph> getOpenGraphs() {
        final Map<String, SGraph> openGraphs = new HashMap<>();
        GraphNode.getAllGraphs().forEach((graphId, graph) -> openGraphs.put(GraphNode.getGraphNode(graphId).getDisplayName(), new SGraph(graph)));
        return openGraphs;
    }

    /**
     * Create an in-memory copy of the given graph.
     *
     * @return a copy of the given graph as an {@link SGraph}.
     */
    public SGraph copyGraph(final SGraph graph) {
        try (final ReadableGraph readableGraph = graph.getGraph().getReadableGraph()) {
            final StoreGraph copyGraph = SubgraphUtilities.copyGraph(readableGraph);
            return new SGraph(new DualGraph(copyGraph, false));
        }

    }

    /**
     * Lookup a plugin by name and execute it with default parameter values.
     *
     * @param graph The graph on which to execute the plugin.
     * @param pluginName The name of the plugin to execute.
     */
    public void executePlugin(final SGraph graph, final String pluginName) {
        final Plugin plugin = PluginRegistry.get(pluginName);
        final PluginParameters parameters = new PluginParameters();
        parameters.appendParameters(plugin.createParameters());
        try {
            PluginExecution.withPlugin(plugin).withParameters(parameters).executeNow(graph.getGraph());
        } catch (final InterruptedException ex) {
            LOGGER.log(Level.SEVERE, ex, () -> pluginName + " was interrupted");
            Thread.currentThread().interrupt();
        } catch (final PluginException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * Lookup a plugin by name and execute it with custom parameter values.
     *
     * @param graph The graph on which to execute the plugin.
     * @param pluginName The name of the plugin you wish to execute.
     * @param pluginParameters A map of parameters to their values for use with
     * the plugin you wish to execute.
     */
    public void executePlugin(final SGraph graph, final String pluginName, final Map<String, String> pluginParameters) {
        final Plugin plugin = PluginRegistry.get(pluginName);
        final PluginParameters parameters = new PluginParameters();
        parameters.appendParameters(plugin.createParameters());
        try {
            pluginParameters.forEach((parameterName, parameterValue) -> {
                if (parameters.hasParameter(parameterName)) {
                    parameters.getParameters().get(parameterName).setStringValue(parameterValue);
                }
            });
            PluginExecution.withPlugin(plugin).withParameters(parameters).executeNow(graph.getGraph());
        } catch (final InterruptedException ex) {
            LOGGER.log(Level.SEVERE, ex, () -> pluginName + " was interrupted");
            Thread.currentThread().interrupt();
        } catch (final PluginException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }
}
