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
package au.gov.asd.tac.constellation.views.aiagent.tools.schema;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.ActionParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.ColorParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.DateTimeRangeParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.LocalDateParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType.MultiChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.NumberParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.PasswordParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.views.aiagent.agent.Redactor;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Generates a JSON Schema describing a plugin's parameters, so the model can
 * fill them in.
 * <p>
 * Password parameters are never included. Object, action and parameter list
 * parameters can't be set from JSON and are left out; if one of them is
 * required the plugin is reported as needing interactive use. Hidden and
 * disabled parameters are left out.
 *
 * @author doubtsec
 */
public final class PluginParametersSchema {

    private static final int MAX_ENUM_VALUES = 200;
    private static final int MAX_DEFAULT_CHARS = 200;
    private static final Set<String> UNSUPPORTED_TYPES = Set.of("object", "parameterlist", ActionParameterType.ID);

    private PluginParametersSchema() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * The schema and what was left out of it.
     *
     * @param schema the JSON Schema (type object).
     * @param excludedRequired required parameters that couldn't be included,
     * which means the plugin needs interactive use.
     * @param excluded every parameter left out, with the reason.
     */
    public record Result(ObjectNode schema, List<String> excludedRequired, List<String> excluded) {

        public boolean requiresInteractiveUse() {
            return !excludedRequired.isEmpty();
        }
    }

    /**
     * Build a schema for a set of plugin parameters.
     *
     * @param parameters the parameters, may be null.
     * @return the schema.
     */
    public static Result build(final PluginParameters parameters) {
        final JsonNodeFactory factory = JsonNodeFactory.instance;
        final ObjectNode schema = factory.objectNode();
        schema.put("type", "object");
        final ObjectNode properties = schema.putObject("properties");
        final ArrayNode required = factory.arrayNode();
        final List<String> excludedRequired = new ArrayList<>();
        final List<String> excluded = new ArrayList<>();

        if (parameters != null) {
            parameters.getParameters().forEach((id, parameter) -> {
                final String typeId = parameter.getType().getId();
                if (PasswordParameterType.ID.equals(typeId) || Redactor.isSecretName(id)) {
                    excluded.add(id + " (secret)");
                    return;
                }
                if (UNSUPPORTED_TYPES.contains(typeId) || StringParameterType.isLabel(parameter)) {
                    excluded.add(id + " (" + typeId + ")");
                    if (parameter.isRequired()) {
                        excludedRequired.add(id);
                    }
                    return;
                }
                if (!parameter.isVisible() || !parameter.isEnabled()) {
                    excluded.add(id + " (hidden or disabled)");
                    return;
                }
                final String name = propertyName(id, parameters);
                properties.set(name, property(parameter));
                if (parameter.isRequired()) {
                    required.add(name);
                }
            });
        }
        if (!required.isEmpty()) {
            schema.set("required", required);
        }
        schema.put("additionalProperties", false);
        return new Result(schema, excludedRequired, excluded);
    }

    /**
     * The name a parameter is shown to the model under: the part of its id
     * after the plugin class name (e.g. {@code include_connections_in}) when
     * that is unique, otherwise the full id. Short names save tokens, and
     * some tool call parsers (e.g. Ollama's for Gemma models) can't handle
     * keys containing dots. {@link PluginParametersJson} accepts both.
     *
     * @param id the parameter id.
     * @param parameters all the plugin's parameters.
     * @return the property name.
     */
    public static String propertyName(final String id, final PluginParameters parameters) {
        final String shortName = id.substring(id.lastIndexOf('.') + 1);
        if (shortName.isEmpty() || shortName.equals(id) || !shortName.matches("[A-Za-z0-9_]+")) {
            return id;
        }
        final long matches = parameters.getParameters().keySet().stream()
                .filter(other -> other.substring(other.lastIndexOf('.') + 1).equalsIgnoreCase(shortName))
                .count();
        return matches == 1 ? shortName : id;
    }

    @SuppressWarnings("unchecked")
    static ObjectNode property(final PluginParameter<?> parameter) {
        final ObjectNode property = JsonNodeFactory.instance.objectNode();
        final String typeId = parameter.getType().getId();
        final StringBuilder description = new StringBuilder();
        if (parameter.getName() != null && !parameter.getName().isBlank()) {
            description.append(parameter.getName());
        }
        if (parameter.getDescription() != null && !parameter.getDescription().isBlank()) {
            if (!description.isEmpty()) {
                description.append(": ");
            }
            description.append(parameter.getDescription());
        }

        switch (typeId) {
            case BooleanParameterType.ID ->
                property.put("type", "boolean");
            case IntegerParameterType.ID, FloatParameterType.ID -> {
                property.put("type", IntegerParameterType.ID.equals(typeId) ? "integer" : "number");
                if (parameter.getParameterValue() instanceof NumberParameterValue number) {
                    if (number.getMinimumValue() != null) {
                        property.put("minimum", number.getMinimumValue().doubleValue());
                    }
                    if (number.getMaximumValue() != null) {
                        property.put("maximum", number.getMaximumValue().doubleValue());
                    }
                }
            }
            case SingleChoiceParameterType.ID -> {
                property.put("type", "string");
                final List<String> options = SingleChoiceParameterType.getOptions((PluginParameter<SingleChoiceParameterValue>) parameter);
                addEnum(property, options, description);
            }
            case MultiChoiceParameterType.ID -> {
                property.put("type", "array");
                final ObjectNode items = property.putObject("items");
                items.put("type", "string");
                final List<String> options = MultiChoiceParameterType.getOptions((PluginParameter<MultiChoiceParameterValue>) parameter);
                addEnum(items, options, description);
            }
            case DateTimeRangeParameterType.ID -> {
                property.put("type", "string");
                appendSentence(description, "Either a relative period with an optional time zone, e.g. 'P7D UTC' or 'P1M', "
                        + "or an absolute range of two ISO-8601 zoned date-times separated by ';', e.g. "
                        + "'2024-01-01T00:00Z[UTC];2024-01-31T23:59:59Z[UTC]'.");
            }
            case LocalDateParameterType.ID -> {
                property.put("type", "string");
                property.put("format", "date");
            }
            case ColorParameterType.ID -> {
                property.put("type", "string");
                appendSentence(description, "A colour name (e.g. 'Red', 'Cherry') or '#RRGGBB'.");
            }
            case FileParameterType.ID -> {
                property.put("type", "string");
                appendSentence(description, "A file path.");
            }
            default ->
                property.put("type", "string");
        }

        final String current = currentValue(parameter);
        if (current != null && !current.isBlank() && !PasswordParameterType.ID.equals(typeId)) {
            appendSentence(description, "Current value: " + abbreviate(current) + ".");
        }
        if (!description.isEmpty()) {
            property.put("description", description.toString());
        }
        return property;
    }

    @SuppressWarnings("unchecked")
    private static String currentValue(final PluginParameter<?> parameter) {
        final String typeId = parameter.getType().getId();
        try {
            if (MultiChoiceParameterType.ID.equals(typeId)) {
                return String.join(", ", MultiChoiceParameterType.getChoices((PluginParameter<MultiChoiceParameterValue>) parameter));
            }
            if (SingleChoiceParameterType.ID.equals(typeId)) {
                return SingleChoiceParameterType.getChoice((PluginParameter<SingleChoiceParameterValue>) parameter);
            }
            return parameter.getStringValue();
        } catch (final RuntimeException ex) {
            return null;
        }
    }

    private static void addEnum(final ObjectNode node, final List<String> options, final StringBuilder description) {
        if (options == null || options.isEmpty()) {
            return;
        }
        if (options.size() <= MAX_ENUM_VALUES) {
            final ArrayNode values = node.putArray("enum");
            options.forEach(values::add);
        } else {
            appendSentence(description, String.format("One of %d options, e.g. %s.", options.size(),
                    String.join(", ", options.subList(0, 10))));
        }
    }

    private static void appendSentence(final StringBuilder description, final String sentence) {
        if (!description.isEmpty()) {
            description.append(description.charAt(description.length() - 1) == '.' ? " " : ". ");
        }
        description.append(sentence);
    }

    private static String abbreviate(final String value) {
        final String singleLine = value.replace('\n', ' ');
        return singleLine.length() <= MAX_DEFAULT_CHARS ? singleLine : singleLine.substring(0, MAX_DEFAULT_CHARS) + "...";
    }
}
