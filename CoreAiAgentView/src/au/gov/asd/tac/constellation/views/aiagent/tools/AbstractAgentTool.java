/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.aiagent.tools;

import au.gov.asd.tac.constellation.utilities.json.JsonFactoryUtilities;
import au.gov.asd.tac.constellation.views.aiagent.api.AgentTool;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;

/**
 * Base class for the built in tools, with helpers for building schemas and
 * results.
 *
 * @author doubtsec
 */
public abstract class AbstractAgentTool implements AgentTool {

    protected static final ObjectMapper MAPPER = JsonFactoryUtilities.getObjectMapper();
    protected static final JsonNodeFactory NODES = JsonNodeFactory.instance;
    protected static final String GRAPH_ID_DESCRIPTION = "Optional id of the graph to use; defaults to the active graph.";

    private JsonNode schema;

    @Override
    public final synchronized JsonNode inputSchema() {
        if (schema == null) {
            schema = buildSchema();
        }
        return schema;
    }

    /**
     * @return the JSON Schema for the tool's arguments.
     */
    protected abstract ObjectNode buildSchema();

    protected static ObjectNode objectSchema() {
        final ObjectNode node = NODES.objectNode();
        node.put("type", "object");
        node.putObject("properties");
        return node;
    }

    protected static ObjectNode property(final ObjectNode schema, final String name, final String type, final String description) {
        final ObjectNode property = ((ObjectNode) schema.get("properties")).putObject(name);
        property.put("type", type);
        property.put("description", description);
        return property;
    }

    protected static ObjectNode enumProperty(final ObjectNode schema, final String name, final List<String> values, final String description) {
        final ObjectNode property = property(schema, name, "string", description);
        final ArrayNode options = property.putArray("enum");
        values.forEach(options::add);
        return property;
    }

    protected static void required(final ObjectNode schema, final String... names) {
        final ArrayNode required = schema.putArray("required");
        for (final String name : names) {
            required.add(name);
        }
    }

    /**
     * Copy the first alias found to the canonical property name, if the
     * canonical property is missing.
     *
     * @param args the arguments.
     * @param name the canonical property name.
     * @param aliases alternative names models use.
     * @return the arguments, possibly with the canonical property added.
     */
    protected static JsonNode alias(final JsonNode args, final String name, final String... aliases) {
        if (!(args instanceof ObjectNode object) || object.hasNonNull(name)) {
            return args;
        }
        for (final String alias : aliases) {
            if (object.hasNonNull(alias)) {
                final ObjectNode copy = object.deepCopy();
                copy.set(name, copy.remove(alias));
                return copy;
            }
        }
        return args;
    }

    protected static String json(final JsonNode node) {
        try {
            return MAPPER.writeValueAsString(node);
        } catch (final JsonProcessingException ex) {
            return String.valueOf(node);
        }
    }

    protected static String text(final JsonNode args, final String name, final String fallback) {
        return args != null && args.hasNonNull(name) && !args.get(name).asText().isBlank() ? args.get(name).asText() : fallback;
    }

    protected static int integer(final JsonNode args, final String name, final int fallback, final int min, final int max) {
        int value = fallback;
        if (args != null && args.hasNonNull(name)) {
            final JsonNode node = args.get(name);
            if (node.canConvertToInt()) {
                value = node.asInt();
            } else if (node.isTextual()) {
                try {
                    value = Integer.parseInt(node.asText().trim());
                } catch (final NumberFormatException ex) {
                    value = fallback;
                }
            }
        }
        return Math.max(min, Math.min(max, value));
    }

    protected static boolean bool(final JsonNode args, final String name, final boolean fallback) {
        if (args == null || !args.hasNonNull(name)) {
            return fallback;
        }
        final JsonNode node = args.get(name);
        return node.isBoolean() ? node.asBoolean() : Boolean.parseBoolean(node.asText());
    }
}
