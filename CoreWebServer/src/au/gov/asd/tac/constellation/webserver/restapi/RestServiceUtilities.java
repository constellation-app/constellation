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
package au.gov.asd.tac.constellation.webserver.restapi;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * Various helper functions for REST services.
 *
 * @author algol
 */
public class RestServiceUtilities {

    private RestServiceUtilities() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Constants for the HTTP method used by a service.
     */
    public enum HttpMethod {
        GET,
        POST,
        PUT;

        /**
         * A helper to convert a string to an HttpMethod without throwing an
         * exception.
         *
         * @param s The String to be converted.
         *
         * @return An enum member, or null if the String was invalid.
         */
        public static HttpMethod getValue(final String s) {
            try {
                return HttpMethod.valueOf(s.toUpperCase(Locale.ENGLISH));
            } catch (final IllegalArgumentException ex) {
                return null;
            }
        }
    }

    /**
     * Constants for common MIME types.
     */
    public static final String APPLICATION_JSON = "application/json";
    public static final String IMAGE_PNG = "image/png";

    /**
     * Convert a JSON ArrayNode to a Java List.
     * <p>
     * The type of list is taken from the type of the first element of the
     * ArrayNode.
     *
     * @param array A JSON ArrayNode.
     *
     * @return A List&lt;Float&gt;, List&lt;Integer&gt;, or List&lt;String&gt;.
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

    /**
     * Set PluginParameters values from JSON.
     *
     * Given an existing ObjectNode, parse the keys and values into an existing
     * PluginParameters instance. Scalar values are supported in so far as
     * PluginParameters.setStringValue() can interpret them. List values are
     * supported in so far as PluginParameters.setObjectValue() can interpret
     * them.
     *
     * @param json An OnjectNode.
     * @param parameters The parameters to be assigned values.
     */
    public static void parametersFromJson(final ObjectNode json, final PluginParameters parameters) {
        json.fields().forEachRemaining(entry -> {
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
                    throw new RestServiceException(String.format("Can't set parameter '%s' to value '%s': %s", parameterName, entry.getValue().asText(), paramError));
                }
            } else {
                throw new RestServiceException(String.format("No such parameter: %s", parameterName));
            }
        });
    }

    public static String activeGraphId() {
        final Graph existingGraph = GraphManager.getDefault().getActiveGraph();

        return existingGraph != null ? existingGraph.getId() : null;
    }

    /**
     * Wait for another graph to become active and return the graph id.
     *
     * Creating and opening a graph are asynchronous operations, but we don't
     * want to put the burden of figuring out when the graph is ready on the
     * caller. Instead we wait for the asynchronous operation to finish by
     * comparing the id of the active graph to that of an existing graph
     * (possibly null). This could be fooled by the user changing graphs, but
     * what are you going to do?
     *
     * @param existingId
     *
     * @return The new graph id.
     */
    public static CompletableFuture<String> waitForGraphChange(final String existingId) {

        return CompletableFuture.supplyAsync(() -> {
            while (true) {
                final Graph newGraph = GraphManager.getDefault().getActiveGraph();
                if (newGraph != null) {
                    final String newId = newGraph != null ? newGraph.getId() : null;
                    if ((existingId == null && newId != null) || (existingId != null && newId != null && !existingId.equals(newId))) {
                        // - there was no existing graph, and the new graph is active, or
                        // - there was an existing graph, and the active graph is not the existing graph.
                        // - we assume the user hasn't interfered by manually switching to another graph at the same time.
                        //
                        return newId;
                    }
                }
            }
        });
    }
}
