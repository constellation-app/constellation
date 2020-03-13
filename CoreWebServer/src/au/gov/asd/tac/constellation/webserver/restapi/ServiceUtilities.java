/*
 * Copyright 2010-2020 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.pluginframework.parameters.PluginParameter;
import au.gov.asd.tac.constellation.pluginframework.parameters.PluginParameters;
import au.gov.asd.tac.constellation.webserver.api.EndpointException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author algol
 */
public class ServiceUtilities {
    private ServiceUtilities() {
    }

    /**
     * Constants for the HTTP method used by a service.
     */
    public static enum HttpMethod {
        GET,
        POST,
        PUT;

        /**
         * A helper to convert a string to an HttpMethod without throwing an exception.
         *
         * @param s The String to be converted.
         *
         * @return An enum member, or null if the String was invalid.
         */
        public static HttpMethod getValue(final String s) {
            try {
                return HttpMethod.valueOf(s.toUpperCase(Locale.US));
            }
            catch(final IllegalArgumentException ex) {
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
     * Build a parameter id for a service parameter in a consistent way.
     *
     * @param serviceName The service name.
     * @param parameterName The parameter name.
     *
     * @return A parameter id that is unique to the service.
     */
    public static String buildId(final String serviceName, final String parameterName) {
        return String.format("%s.%s", serviceName, parameterName);
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
    private static List<?> toList(final ArrayNode array) {
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
                    throw new EndpointException(String.format("Can't set parameter '%s' to value '%s': %s", parameterName, entry.getValue().asText(), paramError));
                }
            } else {
                throw new EndpointException(String.format("No such parameter: %s", parameterName));
            }
        });
    }
}
