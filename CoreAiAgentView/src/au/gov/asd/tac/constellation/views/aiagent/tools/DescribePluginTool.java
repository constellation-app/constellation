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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.views.aiagent.api.AgentTool;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolContext;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolResult;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolRisk;
import au.gov.asd.tac.constellation.views.aiagent.tools.schema.PluginParametersSchema;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.lookup.ServiceProvider;

/**
 * Describes a plugin and the JSON Schema of its parameters.
 *
 * @author doubtsec
 */
@ServiceProvider(service = AgentTool.class, position = 310)
public class DescribePluginTool extends AbstractAgentTool {

    private static final Logger LOGGER = Logger.getLogger(DescribePluginTool.class.getName());

    @Override
    public String name() {
        return "describe_plugin";
    }

    @Override
    public String description() {
        return "Describe a plugin: what it does and a JSON Schema of its parameters (ids, types, allowed values and current values). "
                + "Call this before run_plugin or run_data_access_plugin.";
    }

    @Override
    protected ObjectNode buildSchema() {
        final ObjectNode schema = objectSchema();
        property(schema, "plugin", "string", "The plugin name from search_plugins.");
        required(schema, "plugin");
        return schema;
    }

    @Override
    public JsonNode normaliseArguments(final JsonNode args) {
        JsonNode normalised = alias(args, "plugin", "plugin_name", "name", "plugin_id", "id", "plugin_class", "class_name");
        return normalised;
    }

    @Override
    public String checkArguments(final JsonNode args) {
        try {
            PluginCatalog.resolve(text(args, "plugin", null));
            return null;
        } catch (final IllegalArgumentException ex) {
            return ex.getMessage();
        }
    }

    @Override
    public ToolRisk risk() {
        return ToolRisk.READ_ONLY;
    }

    @Override
    public ToolResult execute(final JsonNode args, final ToolContext context) {
        final Plugin plugin = PluginCatalog.resolve(text(args, "plugin", null));
        final PluginCatalog.Entry entry = PluginCatalog.describe(plugin);
        final PluginParameters parameters = createParameters(plugin, context.getActiveGraph());
        final PluginParametersSchema.Result schema = PluginParametersSchema.build(parameters);

        final ObjectNode result = NODES.objectNode();
        result.put("name", entry.name());
        result.put("plugin", entry.alias());
        result.put("description", entry.description());
        final ArrayNode tags = result.putArray("tags");
        entry.tags().forEach(tags::add);
        if (entry.isDataAccess()) {
            result.put("data_access_category", entry.dataAccessType());
        }
        result.put("run_with", entry.isDataAccess() ? "run_data_access_plugin" : "run_plugin");
        result.set("parameters", schema.schema());
        if (schema.requiresInteractiveUse()) {
            result.put("requires_interactive_use", true);
            result.put("note", "This plugin needs parameters that can't be set by the assistant ("
                    + String.join(", ", schema.excludedRequired()) + "). Ask the user to run it from the menus.");
        }
        return ToolResult.ok(json(result), entry.name() + ": " + schema.schema().get("properties").size() + " parameters");
    }

    /**
     * Create a plugin's parameters, updated for the graph where possible, the
     * same way the plugin's own dialog would be.
     */
    static PluginParameters createParameters(final Plugin plugin, final Graph graph) {
        final PluginParameters parameters = plugin.createParameters();
        if (parameters != null && graph != null) {
            try {
                plugin.updateParameters(graph, parameters);
            } catch (final RuntimeException ex) {
                LOGGER.log(Level.FINE, "updateParameters failed for " + plugin.getName(), ex);
            }
        }
        return parameters;
    }
}
