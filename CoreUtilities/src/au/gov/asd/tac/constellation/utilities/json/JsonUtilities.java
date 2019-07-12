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
package au.gov.asd.tac.constellation.utilities.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * JSON Utilities
 *
 * @author twilight_sparkle
 * @author arcturus
 */
public class JsonUtilities {

    public static String getTextField(JsonNode node, String... keys) {
        return getTextField(null, node, keys);
    }

    public static String getTextField(String defaultVal, JsonNode node, String... keys) {
        JsonNode current = node;
        for (final String key : keys) {
            if (!current.hasNonNull(key)) {
                return defaultVal;
            }
            current = current.get(key);
        }
        return current.textValue();
    }

    public static Iterator<JsonNode> getFieldIterator(JsonNode node, String... keys) {
        JsonNode current = node;
        for (final String key : keys) {
            if (!current.hasNonNull(key)) {
                return new Iterator<JsonNode>() {
                    @Override
                    public boolean hasNext() {
                        return false;
                    }

                    @Override
                    public JsonNode next() {
                        throw new NoSuchElementException();
                    }
                };
            }
            current = current.get(key);
        }
        return current.iterator();
    }

    public static Iterator<String> getTextFieldIterator(JsonNode node, String... keys) {
        JsonNode current = node;
        for (final String key : keys) {
            if (!current.hasNonNull(key)) {
                return new Iterator<String>() {
                    @Override
                    public boolean hasNext() {
                        return false;
                    }

                    @Override
                    public String next() {
                        throw new NoSuchElementException();
                    }
                };
            }
            current = current.get(key);
        }
        Iterator<JsonNode> nodeIter = current.iterator();
        return new Iterator<String>() {
            @Override
            public boolean hasNext() {
                return nodeIter.hasNext();
            }

            @Override
            public String next() {
                return nodeIter.next().textValue();
            }
        };
    }

    public static int getIntegerField(JsonNode node, String... keys) {
        return getIntegerField(0, node, keys);
    }

    public static int getIntegerField(int defaultVal, JsonNode node, String... keys) {
        JsonNode current = node;
        for (final String key : keys) {
            if (!current.hasNonNull(key)) {
                return defaultVal;
            }
            current = current.get(key);
        }
        return current.intValue();
    }

    public static long getLongField(JsonNode node, String... keys) {
        return getLongField(0, node, keys);
    }

    public static long getLongField(long defaultVal, JsonNode node, String... keys) {
        JsonNode current = node;
        for (final String key : keys) {
            if (!current.hasNonNull(key)) {
                return defaultVal;
            }
            current = current.get(key);
        }
        return current.longValue();
    }

    public static double getDoubleField(JsonNode node, String... keys) {
        return getLongField(0, node, keys);
    }

    public static double getDoubleField(double defaultVal, JsonNode node, String... keys) {
        JsonNode current = node;
        for (final String key : keys) {
            if (!current.hasNonNull(key)) {
                return defaultVal;
            }
            current = current.get(key);
        }
        return current.doubleValue();
    }

    public static Iterator<Integer> getIntegerFieldIterator(JsonNode node, String... keys) {
        JsonNode current = node;
        for (final String key : keys) {
            if (!current.hasNonNull(key)) {
                return new Iterator<Integer>() {
                    @Override
                    public boolean hasNext() {
                        return false;
                    }

                    @Override
                    public Integer next() {
                        throw new NoSuchElementException();
                    }
                };
            }
            current = current.get(key);
        }
        Iterator<JsonNode> nodeIter = current.iterator();
        return new Iterator<Integer>() {
            @Override
            public boolean hasNext() {
                return nodeIter.hasNext();
            }

            @Override
            public Integer next() {
                return nodeIter.next().intValue();
            }
        };
    }

    public static boolean getBooleanField(JsonNode node, String... keys) {
        return getBooleanField(false, node, keys);
    }

    public static boolean getBooleanField(boolean defaultVal, JsonNode node, String... keys) {
        JsonNode current = node;
        for (final String key : keys) {
            if (!current.hasNonNull(key)) {
                return defaultVal;
            }
            current = current.get(key);
        }
        return current.booleanValue();
    }

    public static Iterator<Boolean> getBooleanFieldIterator(JsonNode node, String... keys) {
        JsonNode current = node;
        for (final String key : keys) {
            if (!current.hasNonNull(key)) {
                return new Iterator<Boolean>() {
                    @Override
                    public boolean hasNext() {
                        return false;
                    }

                    @Override
                    public Boolean next() {
                        throw new NoSuchElementException();
                    }
                };
            }
            current = current.get(key);
        }
        Iterator<JsonNode> nodeIter = current.iterator();
        return new Iterator<Boolean>() {
            @Override
            public boolean hasNext() {
                return nodeIter.hasNext();
            }

            @Override
            public Boolean next() {
                return nodeIter.next().booleanValue();
            }
        };
    }

    /**
     * Return the {@code textValue()} of a {@code JsonNode} if it exists
     * <p>
     * Example: root.get("thing").textValue()
     *
     * @param attribute the attribute of the node to return.
     * @param node the node that holds the attribute.
     * @return a {@code String} or null if not found
     */
    public static String getTextValue(String attribute, JsonNode node) {
        if (node.has(attribute)) {
            return node.get(attribute).textValue();
        } else {
            return null;
        }
    }

    /**
     * Return the concatenated {@code textValue()} separated by
     * {@code delimiter} of a {@code JsonNode} which has an array of values
     *
     * @param attribute the attribute of the node to return.
     * @param node the node that holds the attribute
     * @param delimiter the delimiter to separate multiple values.
     * @return a {@code String} or null if not found
     */
    public static String getTextValues(String attribute, JsonNode node, String delimiter) {
        StringBuilder sb = new StringBuilder();

        if (node.has(attribute)) {
            for (JsonNode entry : node.get(attribute)) {
                if (sb.length() > 0) {
                    sb.append(delimiter);
                }

                sb.append(entry.textValue());
            }

            return sb.toString();
        } else {
            return null;
        }
    }

    /**
     * Return the {@code textValue()} of a {@code JsonNode} which is inside
     * another {@code JsonNode} indexed at position 0.
     *
     * <p>
     * Example: {@code root.get("thing").get(0).get("uri").textValue()}
     *
     * @param attribute the attribute of the outer node.
     * @param innerAttribute the attribute of the inner node.
     * @param node the outer node.
     * @return a {@code String} or null if not found
     */
    public static String getTextValueOfFirstSubElement(String attribute, String innerAttribute, JsonNode node) {
        if (node.has(attribute)) {
            if (node.get(attribute).has(0) && node.get(attribute).get(0).has(innerAttribute)) {
                return node.get(attribute).get(0).get(innerAttribute).textValue();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Format a raw JSON string into a formatted, indented string.
     *
     * @param rawString The text to format
     * @return The formatted JSON, or plain old text if it isn't JSON.
     */
    public static String prettyPrint(String rawString) {
        // Jackson object mapper for formatting JSON fields returned from GRAVITRON
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode node = mapper.readTree(rawString);
            String prettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
            return prettyJson;
        } catch (IOException ex) {
            // If there is a formatting issue, just return the raw JSON as it was passed in
            return rawString;
        }
    }
}
