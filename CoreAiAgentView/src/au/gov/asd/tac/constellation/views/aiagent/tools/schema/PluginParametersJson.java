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
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType.MultiChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.PasswordParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Sets plugin parameter values from the JSON arguments a model supplies.
 * <p>
 * This follows {@code RestServiceUtilities.parametersFromJson} (scalars are
 * set through {@link PluginParameter#setStringValue(String)} and checked with
 * {@link PluginParameter#getError()}) so behaviour matches the REST API, but
 * also handles single and multiple choice parameters, which the REST helper
 * can't set from JSON arrays, accepts short parameter names, and refuses
 * password parameters.
 *
 * @author constellation
 */
public final class PluginParametersJson {

    private PluginParametersJson() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * A parameter couldn't be set.
     */
    public static final class InvalidParameterException extends IllegalArgumentException {

        private static final long serialVersionUID = 1L;

        public InvalidParameterException(final String message) {
            super(message);
        }
    }

    /**
     * Set parameter values from a JSON object.
     *
     * @param json a JSON object mapping parameter ids to values; may be null.
     * @param parameters the parameters to set.
     * @return the ids of the parameters that were set.
     * @throws InvalidParameterException if a parameter is unknown or a value
     * is invalid.
     */
    public static List<String> apply(final JsonNode json, final PluginParameters parameters) {
        final List<String> set = new ArrayList<>();
        if (json == null || json.isNull()) {
            return set;
        }
        if (!json.isObject()) {
            throw new InvalidParameterException("parameters must be a JSON object mapping parameter ids to values");
        }
        if (parameters == null) {
            if (!json.isEmpty()) {
                throw new InvalidParameterException("this plugin has no parameters");
            }
            return set;
        }
        for (final Map.Entry<String, JsonNode> entry : json.properties()) {
            final PluginParameter<?> parameter = resolve(entry.getKey(), parameters);
            if (parameter == null) {
                final List<String> names = new ArrayList<>();
                parameters.getParameters().keySet().forEach(id -> names.add(PluginParametersSchema.propertyName(id, parameters)));
                throw new InvalidParameterException(String.format("No such parameter: %s. Valid parameters are: %s",
                        entry.getKey(), String.join(", ", names)));
            }
            setValue(parameter, entry.getValue());
            set.add(parameter.getId());
        }
        return set;
    }

    /**
     * Find a parameter by id, by the part of the id after the last '.', or by
     * display name (ignoring case).
     *
     * @param key the key the model used.
     * @param parameters the parameters.
     * @return the parameter, or null.
     */
    static PluginParameter<?> resolve(final String key, final PluginParameters parameters) {
        final Map<String, PluginParameter<?>> all = parameters.getParameters();
        if (all.containsKey(key)) {
            return all.get(key);
        }
        PluginParameter<?> match = null;
        for (final PluginParameter<?> parameter : all.values()) {
            final String id = parameter.getId();
            final String shortId = id.substring(id.lastIndexOf('.') + 1);
            if (shortId.equalsIgnoreCase(key) || parameter.getName() != null && parameter.getName().equalsIgnoreCase(key)) {
                if (match != null) {
                    // ambiguous
                    return null;
                }
                match = parameter;
            }
        }
        return match;
    }

    @SuppressWarnings("unchecked")
    static void setValue(final PluginParameter<?> parameter, final JsonNode value) {
        final String id = parameter.getId();
        final String typeId = parameter.getType().getId();
        if (PasswordParameterType.ID.equals(typeId)) {
            throw new InvalidParameterException(String.format("Parameter %s is a password and can't be set by the assistant", id));
        }

        switch (typeId) {
            case MultiChoiceParameterType.ID -> {
                final PluginParameter<MultiChoiceParameterValue> multi = (PluginParameter<MultiChoiceParameterValue>) parameter;
                final List<String> options = MultiChoiceParameterType.getOptions(multi);
                final List<String> choices = new ArrayList<>();
                if (value.isArray()) {
                    value.forEach(node -> choices.add(matchOption(id, node.asText(), options)));
                } else if (!value.isNull() && !value.asText().isBlank()) {
                    for (final String part : value.asText().split(",")) {
                        choices.add(matchOption(id, part.trim(), options));
                    }
                }
                MultiChoiceParameterType.setChoices(multi, choices);
            }
            case SingleChoiceParameterType.ID -> {
                final PluginParameter<SingleChoiceParameterValue> single = (PluginParameter<SingleChoiceParameterValue>) parameter;
                final List<String> options = SingleChoiceParameterType.getOptions(single);
                if (value.isNull()) {
                    SingleChoiceParameterType.setChoice(single, null);
                } else {
                    SingleChoiceParameterType.setChoice(single, options.isEmpty() ? value.asText() : matchOption(id, value.asText(), options));
                }
            }
            case BooleanParameterType.ID -> {
                final String text = value.asText().trim().toLowerCase(Locale.ROOT);
                if (!"true".equals(text) && !"false".equals(text)) {
                    throw new InvalidParameterException(String.format("Parameter %s must be true or false, not '%s'", id, value.asText()));
                }
                parameter.setBooleanValue(Boolean.parseBoolean(text));
            }
            default -> {
                if (value.isArray() || value.isObject()) {
                    throw new InvalidParameterException(String.format("Parameter %s takes a single %s value, not %s", id, typeId,
                            value.isArray() ? "an array" : "an object"));
                }
                final String text = value.isNull() ? null : value.asText();
                final String validation = text == null ? null : parameter.validateString(text);
                if (validation != null) {
                    throw new InvalidParameterException(String.format("Can't set parameter %s to '%s': %s", id, text, validation));
                }
                parameter.setStringValue(text);
            }
        }

        final String error = parameter.getError();
        if (error != null) {
            throw new InvalidParameterException(String.format("Can't set parameter %s to '%s': %s", id, value.asText(), error));
        }
    }

    private static String matchOption(final String id, final String value, final List<String> options) {
        for (final String option : options) {
            if (option.equals(value)) {
                return option;
            }
        }
        for (final String option : options) {
            if (option.equalsIgnoreCase(value)) {
                return option;
            }
        }
        throw new InvalidParameterException(String.format("'%s' is not a valid choice for %s. Valid choices are: %s",
                value, id, String.join(", ", options)));
    }
}
