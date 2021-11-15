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
    
    private static final Logger LOGGER = Logger.getLogger(JsonUtilities.class.getName());
        
    private JsonUtilities() {
        throw new IllegalStateException("Utility class");
    }
    
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
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
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
        return getDoubleField(0, node, keys);
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
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
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
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
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
        final StringBuilder sb = new StringBuilder();

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
        final ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode node = mapper.readTree(rawString);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
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
