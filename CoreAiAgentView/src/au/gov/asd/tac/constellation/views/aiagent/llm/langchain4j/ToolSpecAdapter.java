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
package au.gov.asd.tac.constellation.views.aiagent.llm.langchain4j;

import au.gov.asd.tac.constellation.views.aiagent.llm.ToolSpec;
import com.fasterxml.jackson.databind.JsonNode;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonAnyOfSchema;
import dev.langchain4j.model.chat.request.json.JsonArraySchema;
import dev.langchain4j.model.chat.request.json.JsonBooleanSchema;
import dev.langchain4j.model.chat.request.json.JsonEnumSchema;
import dev.langchain4j.model.chat.request.json.JsonIntegerSchema;
import dev.langchain4j.model.chat.request.json.JsonNumberSchema;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;
import dev.langchain4j.model.chat.request.json.JsonStringSchema;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts our JSON Schema tool descriptions into LangChain4j
 * {@link ToolSpecification}s.
 * <p>
 * Only the subset of JSON Schema that tools in this module produce is
 * supported: objects, arrays, strings, enums, integers, numbers, booleans and
 * {@code anyOf}. Anything else becomes an unconstrained string.
 *
 * @author doubtsec
 */
public final class ToolSpecAdapter {

    private ToolSpecAdapter() {
        throw new IllegalStateException("Utility class");
    }

    public static ToolSpecification toToolSpecification(final ToolSpec spec) {
        return ToolSpecification.builder()
                .name(spec.name())
                .description(spec.description())
                .parameters(toObjectSchema(spec.parameters()))
                .build();
    }

    public static List<ToolSpecification> toToolSpecifications(final List<ToolSpec> specs) {
        final List<ToolSpecification> result = new ArrayList<>(specs.size());
        specs.forEach(spec -> result.add(toToolSpecification(spec)));
        return result;
    }

    static JsonObjectSchema toObjectSchema(final JsonNode schema) {
        final JsonObjectSchema.Builder builder = JsonObjectSchema.builder();
        if (schema == null || !schema.isObject()) {
            return builder.build();
        }
        if (schema.hasNonNull("description")) {
            builder.description(schema.get("description").asText());
        }
        final JsonNode properties = schema.get("properties");
        if (properties != null && properties.isObject()) {
            properties.properties().forEach(entry -> builder.addProperty(entry.getKey(), toElement(entry.getValue())));
        }
        final JsonNode required = schema.get("required");
        if (required != null && required.isArray() && !required.isEmpty()) {
            final List<String> names = new ArrayList<>();
            required.forEach(node -> names.add(node.asText()));
            builder.required(names);
        }
        if (schema.has("additionalProperties") && schema.get("additionalProperties").isBoolean()) {
            builder.additionalProperties(schema.get("additionalProperties").asBoolean());
        }
        return builder.build();
    }

    static JsonSchemaElement toElement(final JsonNode schema) {
        final String description = schema.hasNonNull("description") ? schema.get("description").asText() : null;
        if (schema.has("enum") && schema.get("enum").isArray()) {
            final List<String> values = new ArrayList<>();
            schema.get("enum").forEach(node -> values.add(node.asText()));
            return JsonEnumSchema.builder().enumValues(values).description(description).build();
        }
        if (schema.has("anyOf") && schema.get("anyOf").isArray()) {
            final List<JsonSchemaElement> options = new ArrayList<>();
            schema.get("anyOf").forEach(node -> options.add(toElement(node)));
            return JsonAnyOfSchema.builder().anyOf(options).description(description).build();
        }
        final String type = schema.hasNonNull("type") ? schema.get("type").asText() : "string";
        return switch (type) {
            case "object" ->
                toObjectSchema(schema);
            case "array" ->
                JsonArraySchema.builder()
                .items(schema.has("items") ? toElement(schema.get("items")) : JsonStringSchema.builder().build())
                .description(description)
                .build();
            case "integer" ->
                JsonIntegerSchema.builder().description(description).build();
            case "number" ->
                JsonNumberSchema.builder().description(description).build();
            case "boolean" ->
                JsonBooleanSchema.builder().description(description).build();
            default ->
                JsonStringSchema.builder().description(description).build();
        };
    }
}
