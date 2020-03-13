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
package au.gov.asd.tac.constellation.webserver.impl;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.pluginframework.Plugin;
import au.gov.asd.tac.constellation.pluginframework.PluginException;
import au.gov.asd.tac.constellation.pluginframework.PluginExecution;
import au.gov.asd.tac.constellation.pluginframework.PluginRegistry;
import au.gov.asd.tac.constellation.pluginframework.parameters.PluginParameters;
import au.gov.asd.tac.constellation.webserver.api.EndpointException;
import au.gov.asd.tac.constellation.webserver.api.RestUtilities;
import au.gov.asd.tac.constellation.webserver.restapi.ServiceUtilities;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author algol
 */
public class PluginImpl {

    /**
     * Return a list of plugin names.
     *
     * @param alias Return aliases if true, else return full names.
     * @param out An OutputStream to write the response to.
     *
     * @throws IOException
     */
    public static void get_list(final boolean alias, final OutputStream out) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final ArrayNode root = mapper.createArrayNode();
        for (final String name : PluginRegistry.getPluginClassNames()) {
            final String s = alias ? PluginRegistry.getAlias(name) : name;
            root.add(s);
        }

        mapper.writeValue(out, root);
    }

    /**
     * Run a plugin, optionally with parameters.
     *
     * @param pluginName The name of the plugin to run.
     * @param in An InputStream to read the plugin parameters from.
     *
     * @throws IOException
     */
    public static void post_run(final String graphId, final String pluginName, final InputStream in) throws IOException {
        final Graph graph = graphId == null ? RestUtilities.getActiveGraph() : GraphNode.getGraph(graphId);
        try {
            final ObjectMapper mapper = new ObjectMapper();
            final JsonNode json = mapper.readTree(in);
            if (json.size() > 0) {
                final Plugin plugin = PluginRegistry.get(pluginName);
                final PluginParameters parameters = plugin.createParameters();
                ServiceUtilities.parametersFromJson((ObjectNode)json, parameters);
                PluginExecution.withPlugin(plugin).withParameters(parameters).executeNow(graph);
            } else {
                PluginExecution.withPlugin(pluginName).executeNow(graph);
            }
        } catch (final InterruptedException | PluginException | IllegalArgumentException ex) {
            throw new EndpointException(ex);
        }
    }
}
