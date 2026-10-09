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
package au.gov.asd.tac.constellation.views.aiagent.agent;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A small JSON Schema validator covering the subset of JSON Schema that tool
 * schemas use: object properties, required properties, primitive types, enums
 * and array items. Its messages are returned to the model so it can correct
 * its arguments.
 *
 * @author constellation
 */
public final class ArgumentValidator {

    private ArgumentValidator() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Validate arguments against a schema.
     *
     * @param args the arguments.
     * @param schema the schema.
     * @return a list of problems, empty if the arguments are valid.
     */
    public static List<String> validate(final JsonNode args, final JsonNode schema) {
        final List<String> problems = new ArrayList<>();
        validate(args, schema, "arguments", problems);
        return problems;
    }

    private static void validate(final JsonNode value, final JsonNode schema, final String path, final List<String> problems) {
        if (schema == null || !schema.isObject()) {
            return;
        }
        if (schema.has("enum") && schema.get("enum").isArray()) {
            boolean found = false;
            for (final JsonNode option : schema.get("enum")) {
                if (option.asText().equals(value.asText())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                final List<String> options = new ArrayList<>();
                schema.get("enum").forEach(option -> options.add(option.asText()));
                problems.add(String.format("%s must be one of %s but was '%s'", path, options, value.asText()));
            }
            return;
        }
        if (schema.has("anyOf")) {
            for (final JsonNode option : schema.get("anyOf")) {
                final List<String> optionProblems = new ArrayList<>();
                validate(value, option, path, optionProblems);
                if (optionProblems.isEmpty()) {
                    return;
                }
            }
            problems.add(path + " doesn't match any of the allowed forms");
            return;
        }
        final String type = schema.hasNonNull("type") ? schema.get("type").asText().toLowerCase(Locale.ROOT) : null;
        if (type == null) {
            return;
        }
        switch (type) {
            case "object" -> {
                if (!value.isObject()) {
                    problems.add(path + " must be an object");
                    return;
                }
                final JsonNode required = schema.get("required");
                if (required != null && required.isArray()) {
                    required.forEach(name -> {
                        if (!value.has(name.asText()) || value.get(name.asText()).isNull()) {
                            problems.add(String.format("%s.%s is required", path, name.asText()));
                        }
                    });
                }
                final JsonNode properties = schema.get("properties");
                if (properties != null && properties.isObject()) {
                    value.properties().forEach(entry -> {
                        final JsonNode propertySchema = properties.get(entry.getKey());
                        if (propertySchema == null) {
                            if (schema.has("additionalProperties") && !schema.get("additionalProperties").asBoolean(true)) {
                                problems.add(String.format("%s.%s is not a known property", path, entry.getKey()));
                            }
                        } else if (!entry.getValue().isNull()) {
                            validate(entry.getValue(), propertySchema, path + "." + entry.getKey(), problems);
                        }
                    });
                }
            }
            case "array" -> {
                if (!value.isArray()) {
                    problems.add(path + " must be an array");
                    return;
                }
                final JsonNode items = schema.get("items");
                for (int i = 0; i < value.size(); i++) {
                    validate(value.get(i), items, path + "[" + i + "]", problems);
                }
            }
            case "string" -> {
                if (!value.isTextual() && !value.isNumber() && !value.isBoolean()) {
                    problems.add(path + " must be a string");
                }
            }
            case "integer" -> {
                if (!value.isIntegralNumber() && !(value.isTextual() && value.asText().matches("-?\\d+"))) {
                    problems.add(path + " must be an integer");
                }
            }
            case "number" -> {
                if (!value.isNumber() && !(value.isTextual() && value.asText().matches("-?\\d+(\\.\\d+)?"))) {
                    problems.add(path + " must be a number");
                }
            }
            case "boolean" -> {
                if (!value.isBoolean() && !(value.isTextual() && value.asText().matches("(?i)true|false"))) {
                    problems.add(path + " must be a boolean");
                }
            }
            default -> {
                // other types aren't produced by our tools
            }
        }
    }
}
