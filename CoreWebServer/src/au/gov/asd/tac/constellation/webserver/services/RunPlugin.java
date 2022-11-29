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
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.openide.util.lookup.ServiceProvider;

/**
 * Run a plugin, optionally specifying a graph.
 *
 * @author algol
 */
@ServiceProvider(service = RestService.class)
public class RunPlugin extends RestService {

    private static final String NAME = "run_plugin";
    private static final String PLUGIN_NAME_PARAMETER_ID = "plugin_name";
    private static final String GRAPH_ID_PARAMETER_ID = "graph_id";
    private static final String ARGS_PARAMETER_ID = "args";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Run a plugin, optionally specifying a graph.";
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

        final PluginParameter<StringParameterValue> nameParam = StringParameterType.build(PLUGIN_NAME_PARAMETER_ID);
        nameParam.setName("Plugin name");
        nameParam.setDescription("The name of the plugin to run.");
        nameParam.setRequired(true);
        parameters.addParameter(nameParam);

        final PluginParameter<StringParameterValue> graphIdParam = StringParameterType.build(GRAPH_ID_PARAMETER_ID);
        graphIdParam.setName("Graph id");
        graphIdParam.setDescription("The id of a graph to run the plugin on. (Default is the active graph)");
        parameters.addParameter(graphIdParam);

        final PluginParameter<StringParameterValue> argsParam = StringParameterType.build(ARGS_PARAMETER_ID);
        argsParam.setName("Plugin arguments (body)");
        argsParam.setDescription("A JSON object containing parameter names and values to be passed to the plugin.");
        argsParam.setRequestBodyExampleJson("#/components/examples/runPluginExample");
        parameters.addParameter(argsParam);

        return parameters;
    }

    @Override
    public void callService(final PluginParameters parameters, InputStream in, OutputStream out) throws IOException {
        final String pluginName = parameters.getStringValue(PLUGIN_NAME_PARAMETER_ID);
        final String graphId = parameters.getStringValue(GRAPH_ID_PARAMETER_ID);

        final Graph graph = graphId == null ? RestUtilities.getActiveGraph() : GraphNode.getGraph(graphId);
        if (graph != null) {
            try {
                final ObjectMapper mapper = new ObjectMapper();
                final JsonNode json = mapper.readTree(in);
                if (json.size() > 0) {
                    final Plugin plugin = PluginRegistry.get(pluginName);
                    final PluginParameters pluginParameters = plugin.createParameters();
                    RestServiceUtilities.parametersFromJson((ObjectNode) json, pluginParameters);
                    PluginExecution.withPlugin(plugin).withParameters(pluginParameters).executeNow(graph);
                } else {
                    PluginExecution.withPlugin(pluginName).executeNow(graph);
                }
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new RestServiceException(ex);
            } catch (final PluginException | IllegalArgumentException ex) {
                throw new RestServiceException(HTTP_UNPROCESSABLE_ENTITY, ex.getMessage());
            }
        } else {
            throw new RestServiceException(HTTP_UNPROCESSABLE_ENTITY, "No graph with id " + graphId);
        }
    }
}
