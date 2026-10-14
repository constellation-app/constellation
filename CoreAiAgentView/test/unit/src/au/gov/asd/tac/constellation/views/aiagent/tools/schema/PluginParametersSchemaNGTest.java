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

import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginRegistry;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.DateTimeRangeParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType.MultiChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.ObjectParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.PasswordParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.utilities.json.JsonFactoryUtilities;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;
import org.testng.annotations.Test;

/**
 * Tests for {@link PluginParametersSchema} and {@link PluginParametersJson}.
 */
public class PluginParametersSchemaNGTest {

    private static PluginParameters parameters() {
        final PluginParameters parameters = new PluginParameters();
        final PluginParameter<?> name = StringParameterType.build("Test.name");
        name.setName("Name");
        name.setDescription("The name");
        name.setRequired(true);
        parameters.addParameter(name);
        final PluginParameter<IntegerParameterValue> count = IntegerParameterType.build("Test.count");
        IntegerParameterType.setMinimum(count, 1);
        IntegerParameterType.setMaximum(count, 10);
        count.setIntegerValue(3);
        parameters.addParameter(count);
        parameters.addParameter(BooleanParameterType.build("Test.flag"));
        final PluginParameter<SingleChoiceParameterValue> mode = SingleChoiceParameterType.build("Test.mode");
        SingleChoiceParameterType.setOptions(mode, List.of("Fast", "Slow"));
        SingleChoiceParameterType.setChoice(mode, "Fast");
        parameters.addParameter(mode);
        final PluginParameter<MultiChoiceParameterValue> tags = MultiChoiceParameterType.build("Test.tags");
        MultiChoiceParameterType.setOptions(tags, List.of("a", "b", "c"));
        parameters.addParameter(tags);
        parameters.addParameter(DateTimeRangeParameterType.build("Test.range"));
        parameters.addParameter(PasswordParameterType.build("Test.password"));
        final PluginParameter<?> hidden = StringParameterType.build("Test.hidden");
        hidden.setVisible(false);
        parameters.addParameter(hidden);
        final PluginParameter<?> object = ObjectParameterType.build("Test.object");
        object.setRequired(true);
        parameters.addParameter(object);
        return parameters;
    }

    @Test
    public void testSchema() {
        final PluginParametersSchema.Result result = PluginParametersSchema.build(parameters());
        final ObjectNode schema = result.schema();
        final JsonNode properties = schema.get("properties");
        assertEquals(schema.get("type").asText(), "object");
        // short, dot-free names
        assertEquals(properties.get("name").get("type").asText(), "string");
        assertTrue(properties.get("name").get("description").asText().startsWith("Name: The name"));
        assertEquals(properties.get("count").get("type").asText(), "integer");
        assertEquals(properties.get("count").get("minimum").asInt(), 1);
        assertEquals(properties.get("count").get("maximum").asInt(), 10);
        assertTrue(properties.get("count").get("description").asText().contains("Current value: 3"));
        assertEquals(properties.get("flag").get("type").asText(), "boolean");
        assertEquals(properties.get("mode").get("enum").size(), 2);
        assertEquals(properties.get("tags").get("type").asText(), "array");
        assertEquals(properties.get("tags").get("items").get("enum").size(), 3);
        assertTrue(properties.get("range").get("description").asText().contains("P7D"));
        // never exposed
        assertFalse(properties.has("password"));
        assertFalse(properties.has("hidden"));
        assertFalse(properties.has("object"));
        assertEquals(schema.get("required").get(0).asText(), "name");
        assertEquals(schema.get("required").size(), 1);
        assertTrue(result.requiresInteractiveUse());
        assertEquals(result.excludedRequired(), List.of("Test.object"));
    }

    @Test
    public void testPropertyNamesStayFullWhenAmbiguous() {
        final PluginParameters parameters = new PluginParameters();
        parameters.addParameter(StringParameterType.build("A.value"));
        parameters.addParameter(StringParameterType.build("B.value"));
        parameters.addParameter(StringParameterType.build("A.other"));
        assertEquals(PluginParametersSchema.propertyName("A.value", parameters), "A.value");
        assertEquals(PluginParametersSchema.propertyName("A.other", parameters), "other");
    }

    @Test
    public void testApply() throws Exception {
        final PluginParameters parameters = parameters();
        final List<String> set = PluginParametersJson.apply(JsonFactoryUtilities.getObjectMapper().readTree(
                "{\"Test.name\":\"x\",\"count\":7,\"Test.flag\":true,\"Test.mode\":\"slow\",\"Test.tags\":[\"a\",\"c\"],\"Test.range\":\"P7D\"}"),
                parameters);
        assertEquals(set.size(), 6);
        assertEquals(parameters.getStringValue("Test.name"), "x");
        assertEquals(parameters.getIntegerValue("Test.count"), 7);
        assertTrue(parameters.getBooleanValue("Test.flag"));
        assertEquals(parameters.getStringValue("Test.mode"), "Slow");
        assertEquals(MultiChoiceParameterType.getChoices(
                (PluginParameter<MultiChoiceParameterValue>) parameters.getParameters().get("Test.tags")), List.of("a", "c"));
        assertEquals(parameters.getDateTimeRangeValue("Test.range").toString(), "P7D UTC");
    }

    @Test
    public void testApplyErrors() throws Exception {
        assertError("{\"Test.nope\":1}", "No such parameter");
        assertError("{\"Test.mode\":\"medium\"}", "not a valid choice");
        assertError("{\"Test.tags\":[\"z\"]}", "not a valid choice");
        assertError("{\"Test.flag\":\"maybe\"}", "true or false");
        assertError("{\"Test.count\":\"lots\"}", "Test.count");
        assertError("{\"Test.password\":\"x\"}", "password");
        assertError("{\"Test.name\":[1]}", "single");
    }

    private static void assertError(final String json, final String expected) throws Exception {
        final JsonNode node = JsonFactoryUtilities.getObjectMapper().readTree(json);
        final IllegalArgumentException ex = expectThrows(IllegalArgumentException.class, () -> PluginParametersJson.apply(node, parameters()));
        assertTrue(ex.getMessage().contains(expected), ex.getMessage());
    }

    /**
     * Every plugin on the classpath must produce a usable schema, and setting
     * its parameters to their own current values must work.
     */
    @Test
    public void testEveryRegisteredPlugin() {
        final List<String> failures = new ArrayList<>();
        int checked = 0;
        for (final String name : PluginRegistry.getPluginClassNames()) {
            final Plugin plugin;
            final PluginParameters parameters;
            try {
                plugin = PluginRegistry.get(name);
                parameters = plugin.createParameters();
            } catch (final RuntimeException | LinkageError ex) {
                continue;
            }
            try {
                final PluginParametersSchema.Result result = PluginParametersSchema.build(parameters);
                assertEquals(result.schema().get("type").asText(), "object");
                result.schema().get("properties").properties().forEach(entry -> {
                    if (!entry.getValue().has("type")) {
                        failures.add(name + "." + entry.getKey() + " has no type");
                    }
                });
                // round trip the defaults of simple scalar parameters
                if (parameters != null) {
                    final ObjectNode defaults = JsonFactoryUtilities.getObjectMapper().createObjectNode();
                    result.schema().get("properties").properties().forEach(entry -> {
                        final PluginParameter<?> parameter = PluginParametersJson.resolve(entry.getKey(), parameters);
                        final String type = entry.getValue().get("type").asText();
                        if (("integer".equals(type) || "number".equals(type) || "boolean".equals(type))
                                && parameter.getStringValue() != null && !parameter.getStringValue().isBlank()) {
                            defaults.put(entry.getKey(), parameter.getStringValue());
                        }
                    });
                    PluginParametersJson.apply(defaults, parameters);
                }
                checked++;
            } catch (final RuntimeException ex) {
                failures.add(name + ": " + ex);
            }
        }
        assertTrue(failures.isEmpty(), String.join("\n", failures));
        assertTrue(checked > 0, "no plugins found on the classpath");
    }
}
