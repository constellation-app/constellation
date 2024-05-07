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
package au.gov.asd.tac.constellation.webserver.services;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginRegistry;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.webserver.api.RestUtilities;
import au.gov.asd.tac.constellation.webserver.restapi.RestService;
import au.gov.asd.tac.constellation.webserver.restapi.RestServiceException;
import au.gov.asd.tac.constellation.webserver.restapi.RestServiceUtilities;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.openide.util.lookup.ServiceProvider;

/**
 * Run some plugins, possibly in parallel, optionally specifying a graph.
 *
 * @author algol
 */
@ServiceProvider(service = RestService.class)
public class RunPlugins extends RestService {

    private static final String NAME = "run_plugins";
    private static final String GRAPH_ID_PARAMETER_ID = "graph_id";
    private static final String RUN_IN_PARAMETER_ID = "run_in";
    private static final String PLUGINS_PARAMETER_ID = "plugins";

    // Run style.
    //
    private static final String RUN_STYLE_SERIES = "series";
    private static final String RUN_STYLE_PARALLEL = "parallel";

    // Plugin argument keys.
    //
    private static final String PLUGIN_NAME = "plugin_name";
    private static final String PLUGIN_ARGS = "plugin_args";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Run some plugins, possibly in parallel, optionally specifying a graph.";
    }

    @Override
    public String[] getTags() {
        return new String[]{"plugin"};
    }

    @Override
    public RestServiceUtilities.HttpMethod getHttpMethod() {
        return RestServiceUtilities.HttpMethod.POST;
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<StringParameterValue> graphIdParam = StringParameterType.build(GRAPH_ID_PARAMETER_ID);
        graphIdParam.setName("Graph id");
        graphIdParam.setDescription("The id of the graph to run the plugins on. (Default is the active graph)");
        parameters.addParameter(graphIdParam);

        final PluginParameter<StringParameterValue> runInParam = StringParameterType.build(RUN_IN_PARAMETER_ID);
        runInParam.setName("Run in");
        runInParam.setDescription("Run in 'series' (the default) or 'parallel'.");
        runInParam.setStringValue(RUN_STYLE_SERIES);
        parameters.addParameter(runInParam);

        final PluginParameter<StringParameterValue> pluginsParam = StringParameterType.build(PLUGINS_PARAMETER_ID);
        pluginsParam.setName("Plugins and arguments (body)");
        pluginsParam.setDescription("A JSON list containing objects with 'plugin_name' and 'plugin_args' arguments.");
        pluginsParam.setRequestBodyExampleJson("#/components/examples/runPluginsExample");
        pluginsParam.setRequired(true);
        parameters.addParameter(pluginsParam);

        return parameters;
    }

    @Override
    public void callService(final PluginParameters parameters, final InputStream in, final OutputStream out) throws IOException {
        final String graphId = parameters.getStringValue(GRAPH_ID_PARAMETER_ID);
        final Graph graph = graphId == null ? RestUtilities.getActiveGraph() : GraphNode.getGraph(graphId);
        if (graph == null) {
            throw new RestServiceException(HTTP_UNPROCESSABLE_ENTITY, "No graph with id " + graphId);
        }

        final String runStyle = parameters.getStringValue(RUN_IN_PARAMETER_ID);
        if (!RUN_STYLE_SERIES.equals(runStyle) && !RUN_STYLE_PARALLEL.equals(runStyle)) {
            final String msg = String.format("%s must be '%s' or '%s'", RUN_IN_PARAMETER_ID, RUN_STYLE_SERIES, RUN_STYLE_PARALLEL);
            throw new RestServiceException(HTTP_UNPROCESSABLE_ENTITY, msg);
        }

        // First, collect all the plugins and their optional arguments.
        //
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode json = mapper.readTree(in);
        if (!json.isArray()) {
            final String msg = String.format("Argument for %s must be a list", NAME);
            throw new RestServiceException(HTTP_UNPROCESSABLE_ENTITY, msg);
        }

        final ConcurrentLinkedQueue<PluginError> errorQueue = new ConcurrentLinkedQueue<>();
        final ArrayNode pluginList = (ArrayNode) json;
        final List<PluginInstance> pluginInstances = new ArrayList<>();
        pluginList.forEach(pluginItem -> {
            if (!pluginItem.has(PLUGIN_NAME) || !pluginItem.get(PLUGIN_NAME).isTextual()) {
                final String msg = String.format("Each plugin argument must have %s", PLUGIN_NAME);
                throw new RestServiceException(HTTP_UNPROCESSABLE_ENTITY, msg);
            }

            final String pluginName = pluginItem.get(PLUGIN_NAME).textValue();
            final Plugin plugin = PluginRegistry.get(pluginName);

            final PluginParameters pluginParameters = plugin.createParameters();
            if (pluginParameters != null && pluginItem.has(PLUGIN_ARGS)) {
                final ObjectNode pluginArgs = (ObjectNode) pluginItem.get(PLUGIN_ARGS);
                RestServiceUtilities.parametersFromJson(pluginArgs, pluginParameters);
            }

            pluginInstances.add(new PluginInstance(graph, plugin, pluginParameters, errorQueue));
        });

        // Time to execute.
        // If series, just run them sequentially.
        // If parallel, do the things with threads.
        // Either way, any errors in execution will be caught and written to the error queue.
        //
        if (runStyle.equals(RUN_STYLE_SERIES)) {
            pluginInstances.forEach(PluginInstance::run);
        } else {
            final List<Thread> pluginThreads = new ArrayList<>();
            pluginInstances.forEach(pi -> {
                final Thread thread = new Thread(pi, pi.plugin.getName());
                pluginThreads.add(thread);
                thread.start();
            });

            pluginThreads.forEach(thread -> {
                try {
                    thread.join();
                } catch (final InterruptedException ex) {
                    thread.interrupt();
                    errorQueue.add(new PluginError(String.format("Thread %s", thread.getName()), ex));
                }
            });
        }

        // The plugins have finished, so look at the error queue to see if anything
        // didn't work. If there are errors, pass them back.
        //
        final StringBuilder buf = new StringBuilder();
        while (!errorQueue.isEmpty()) {
            buf.append(errorQueue.remove().toString());
        }

        if (buf.length() > 0) {
            throw new RestServiceException(buf.toString());
        }
    }

    /**
     * An instance of a plugin and its parameters.
     * <p>
     * This instance can be run. Any plugin exceptions will be added to a queue
     * and not thrown, so the instance can be run in a thread.
     */
    private static class PluginInstance implements Runnable {

        final Graph graph;
        final Plugin plugin;
        final PluginParameters params;
        final Queue<PluginError> errorQueue;

        PluginInstance(final Graph graph, final Plugin plugin, final PluginParameters params, final Queue<PluginError> errorQueue) {
            this.graph = graph;
            this.plugin = plugin;
            this.params = params;
            this.errorQueue = errorQueue;
        }

        @Override
        public void run() {
            try {
                if (params != null) {
                    PluginExecution.withPlugin(plugin).withParameters(params).executeNow(graph);
                } else {
                    PluginExecution.withPlugin(plugin).executeNow(graph);
                }
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
                errorQueue.add(new PluginError(plugin.getName(), ex));
            } catch (final PluginException ex) {
                errorQueue.add(new PluginError(plugin.getName(), ex));
            }
        }
    }

    private static class PluginError {

        final String pluginName;
        final Exception ex;

        public PluginError(final String pluginName, final Exception ex) {
            this.pluginName = pluginName;
            this.ex = ex;
        }

        @Override
        public String toString() {
            final String exString = ex.getMessage();
            return String.format("Plugin:%n%s%nException:%n%s%n", pluginName, exString);
        }
    }
}
