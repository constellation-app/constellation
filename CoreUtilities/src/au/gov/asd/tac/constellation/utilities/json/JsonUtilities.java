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
package au.gov.asd.tac.constellation.utilities.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * JSON Utilities
 *
 * @author twilight_sparkle
 * @author arcturus
 */
public class JsonUtilities {

    private JsonUtilities() {
        throw new IllegalStateException("Utility class");
    }

    private static final Logger LOGGER = Logger.getLogger(JsonUtilities.class.getName());

    public static String getTextField(final JsonNode node, final String... keys) {
        return getTextField(null, node, keys);
    }

    public static String getTextField(final String defaultVal, final JsonNode node, final String... keys) {
        JsonNode current = node;
        for (final String key : keys) {
            if (!current.hasNonNull(key)) {
                return defaultVal;
            }
            current = current.get(key);
        }
        return current.textValue();
    }

    public static Iterator<JsonNode> getFieldIterator(final JsonNode node, final String... keys) {
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

    public static Iterator<String> getTextFieldIterator(final JsonNode node, final String... keys) {
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
        final Iterator<JsonNode> nodeIter = current.iterator();
        return new Iterator<String>() {
            @Override
            public boolean hasNext() {
                return nodeIter.hasNext();
            }

            @Override
            public String next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return nodeIter.next().textValue();
            }
        };
    }

    public static int getIntegerField(final JsonNode node, final String... keys) {
        return getIntegerField(0, node, keys);
    }

    public static int getIntegerField(final int defaultVal, final JsonNode node, final String... keys) {
        JsonNode current = node;
        for (final String key : keys) {
            if (!current.hasNonNull(key)) {
                return defaultVal;
            }
            current = current.get(key);
        }
        return current.intValue();
    }

    public static long getLongField(final JsonNode node, final String... keys) {
        return getLongField(0, node, keys);
    }

    public static long getLongField(final long defaultVal, final JsonNode node, final String... keys) {
        JsonNode current = node;
        for (final String key : keys) {
            if (!current.hasNonNull(key)) {
                return defaultVal;
            }
            current = current.get(key);
        }
        return current.longValue();
    }

    public static double getDoubleField(final JsonNode node, final String... keys) {
        return getDoubleField(0, node, keys);
    }

    public static double getDoubleField(final double defaultVal, final JsonNode node, final String... keys) {
        JsonNode current = node;
        for (final String key : keys) {
            if (!current.hasNonNull(key)) {
                return defaultVal;
            }
            current = current.get(key);
        }
        return current.doubleValue();
    }

    public static Iterator<Integer> getIntegerFieldIterator(final JsonNode node, final String... keys) {
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
        final Iterator<JsonNode> nodeIter = current.iterator();
        return new Iterator<Integer>() {
            @Override
            public boolean hasNext() {
                return nodeIter.hasNext();
            }

            @Override
            public Integer next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return nodeIter.next().intValue();
            }
        };
    }

    public static boolean getBooleanField(final JsonNode node, final String... keys) {
        return getBooleanField(false, node, keys);
    }

    public static boolean getBooleanField(final boolean defaultVal, final JsonNode node, final String... keys) {
        JsonNode current = node;
        for (final String key : keys) {
            if (!current.hasNonNull(key)) {
                return defaultVal;
            }
            current = current.get(key);
        }
        return current.booleanValue();
    }

    public static Iterator<Boolean> getBooleanFieldIterator(final JsonNode node, final String... keys) {
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
        final Iterator<JsonNode> nodeIter = current.iterator();
        return new Iterator<Boolean>() {
            @Override
            public boolean hasNext() {
                return nodeIter.hasNext();
            }

            @Override
            public Boolean next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return nodeIter.next().booleanValue();
            }
        };
    }

    /**
     * Return the {@code JsonNode} found after traversing through nodes with
     * supplied keys from starting {@code JsonNode} - or null if path doesn't
     * return a valid node.
     *
     * @param node Node to start iteration from.
     * @param keys Text names of nodes to traverse through.
     * @return {@code JsonNode} at end of traversal.
     */
    public static JsonNode getChildNode(final JsonNode node, final String... keys) {
        JsonNode current = node;
        for (final String key : keys) {
            if (!current.hasNonNull(key)) {
                return null;
            }
            current = current.get(key);
        }
        return current;
    }

    /**
     * Private helper method returning string value of node. No validation of
     * node is performed, it is the responsibility of calling method to ensure
     * node is not null.
     *
     * @param node Node to extract string value from.
     * @return String representation of node except returns null if the node is
     * null.
     */
    public static String getNodeText(final JsonNode node) {
        if (node.isNull()) {
            return null;
        } else if (node.isTextual()) {
            return node.textValue();
        }
        return node.toString();
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
    public static String getTextValue(final String attribute, final JsonNode node) {
        return node.has(attribute) ? getNodeText(node.get(attribute)) : null;
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
    public static String getTextValues(final String attribute, final JsonNode node, final String delimiter) {
        final StringBuilder sb = new StringBuilder();

        if (node.has(attribute)) {
            for (final JsonNode entry : node.get(attribute)) {
                if (sb.length() > 0) {
                    sb.append(delimiter);
                }
                sb.append(getNodeText(entry));
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
    public static String getTextValueOfFirstSubElement(final String attribute, final String innerAttribute, final JsonNode node) {
        return node.has(attribute) && node.get(attribute).has(0) && node.get(attribute).get(0).has(innerAttribute) 
                ? getNodeText(node.get(attribute).get(0).get(innerAttribute)) 
                : null;
    }

    /**
     * Format a raw JSON string into a formatted, indented string.
     *
     * @param rawString The text to format
     * @return The formatted JSON, or plain old text if it isn't JSON.
     */
    public static String prettyPrint(final String rawString) {
        final ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readTree(rawString));
        } catch (final IOException ex) {
            // If there is a formatting issue, just return the raw JSON as it was passed in
            return rawString;
        }
    }

    /**
     * Converts a map to JSON String. Will return an empty string if no entries
     * are valid in the map.
     *
     * @param <K> the key type of the map
     * @param <V> the value type of the map
     * @param factory the jsonFactory object to use
     * @param map the map to convert to a json string
     * @return the JSON String representation of the map
     */
    public static <K, V> String getMapAsString(final JsonFactory factory, final Map<K, V> map) {
        if (MapUtils.isNotEmpty(map)) {
            final ByteArrayOutputStream json = new ByteArrayOutputStream();
            try (final JsonGenerator jg = factory.createGenerator(json)) {
                jg.writeStartObject();
                for (final Map.Entry<K, V> entry : map.entrySet()) {
                    final K key = entry.getKey();
                    final V value = entry.getValue();
                    if (key != null && value != null) {
                        jg.writeStringField(key.toString(), value.toString());
                    }
                }
                jg.writeEndObject();
                jg.flush();
                return json.toString(StandardCharsets.UTF_8.name());

            } catch (final IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        }

        return StringUtils.EMPTY;
    }

    /**
     * Converts a JSON String to a String, String map. Will return an empty Map
     * if no valid items within the String
     *
     * @param factory the jsonFactory object to use
     * @param mapAsString the JSON String representation of the map
     * @return A String, String map based on the JSON String
     */
    public static Map<String, String> getStringAsMap(final JsonFactory factory, final String mapAsString) {
        final Map<String, String> map = new HashMap<>();
        if (StringUtils.isNotEmpty(mapAsString)) {
            try (final JsonParser jp = factory.createParser(mapAsString)) {
                if (jp.nextToken() == JsonToken.START_OBJECT) {
                    while (jp.nextToken() != JsonToken.END_OBJECT) {
                        if (jp.getCurrentToken() == JsonToken.FIELD_NAME) {
                            final String fieldName = jp.getText();
                            jp.nextToken();
                            final String fieldValue = jp.getText();
                            map.put(fieldName, fieldValue);
                        }
                    }
                }
            } catch (final IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        }
        return map;
    }
}
