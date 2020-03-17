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
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginRegistry;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.webserver.api.EndpointException;
import au.gov.asd.tac.constellation.webserver.api.RestUtilities;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
                for (final Iterator<Map.Entry<String, JsonNode>> it = json.fields(); it.hasNext();) {
                    final Map.Entry<String, JsonNode> entry = it.next();
                    final String parameterName = entry.getKey();
                    if (parameters.hasParameter(parameterName)) {
                        // Set the parameter with error checking.
                        //
                        final PluginParameter<?> param = parameters.getParameters().get(parameterName);
                        final JsonNode node = entry.getValue();
                        if (node.isArray()) {
                            // If the parameter is a JSON array, convert it to a Java List.
                            //
                            param.setObjectValue(toList((ArrayNode) node));
                        } else {
                            // Convert the parameter to a String, and let PluginParameter deal with the type conversion.
                            //
                            param.setStringValue(node.asText());
                        }
                        final String paramError = param.getError();
                        if (paramError != null) {
                            throw new EndpointException(String.format("Can't set parameter '%s' to value '%s': %s", parameterName, entry.getValue().asText(), paramError));
                        }
                    } else {
                        throw new EndpointException(String.format("No such parameter: %s", parameterName));
                    }
                }
                PluginExecution.withPlugin(plugin).withParameters(parameters).executeNow(graph);
            } else {
                PluginExecution.withPlugin(pluginName).executeNow(graph);
            }
        } catch (final InterruptedException | PluginException | IllegalArgumentException ex) {
            throw new EndpointException(ex);
        }
    }

    /**
     * Convert a JSON ArrayNode to a Java List.
     * <p>
     * The type of list is taken from the type of the first element of the
     * ArrayNode.
     *
     * @param array A JSON ArrayNode.
     *
     * @return A List<Float>, List<Integer>, or List<String>.
     */
    public static List<?> toList(final ArrayNode array) {
        final int size = array.size();
        List<?> list;
        if (size == 0) {
            list = new ArrayList<>(size);
        } else if (array.get(0).getClass() == IntNode.class) {
            final List<Integer> values = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                values.add(array.get(i).intValue());
            }

            list = values;
        } else if (array.get(0).getClass() == DoubleNode.class) {
            final List<Float> values = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                values.add(array.get(i).floatValue());
            }

            list = values;
        } else {
            final List<String> values = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                values.add(array.get(i).asText());
            }

            list = values;
        }

        return list;
    }
}
