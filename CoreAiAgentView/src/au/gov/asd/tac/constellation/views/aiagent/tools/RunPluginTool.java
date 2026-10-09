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
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.views.aiagent.api.AgentTool;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolContext;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolResult;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolRisk;
import au.gov.asd.tac.constellation.views.aiagent.tools.schema.PluginParametersJson;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;

/**
 * Runs a plugin on the graph as an undoable step.
 *
 * @author doubtsec
 */
@ServiceProvider(service = AgentTool.class, position = 320)
public class RunPluginTool extends AbstractAgentTool {

    @Override
    public String name() {
        return "run_plugin";
    }

    @Override
    public String description() {
        return "Run a plugin (analytic, arrangement, selection, editing, ...) on the graph. Pass only the parameters you want to change, "
                + "keyed by the parameter ids from describe_plugin. Returns what changed in the graph. Each run can be undone. "
                + "Use run_data_access_plugin for Data Access View plugins.";
    }

    @Override
    protected ObjectNode buildSchema() {
        final ObjectNode schema = objectSchema();
        property(schema, "plugin", "string", "The plugin name from search_plugins.");
        final ObjectNode parameters = property(schema, "parameters", "object", "Parameter values keyed by parameter id.");
        parameters.put("additionalProperties", true);
        property(schema, "graph_id", "string", GRAPH_ID_DESCRIPTION);
        required(schema, "plugin");
        return schema;
    }

    @Override
    public JsonNode normaliseArguments(final JsonNode args) {
        JsonNode normalised = alias(args, "plugin", "plugin_name", "name", "plugin_id", "id", "plugin_class", "class_name");
        normalised = alias(normalised, "parameters", "params", "args", "arguments", "plugin_parameters");
        return normalised;
    }

    @Override
    public String checkArguments(final JsonNode args) {
        try {
            final Plugin plugin = PluginCatalog.resolve(text(args, "plugin", null));
            final PluginCatalog.Entry entry = PluginCatalog.describe(plugin);
            return entry.isDataAccess() ? entry.name() + " is a Data Access View plugin; use run_data_access_plugin instead."
                    : checkParameters(plugin, args.get("parameters"));
        } catch (final IllegalArgumentException ex) {
            return ex.getMessage();
        }
    }

    @Override
    public ToolRisk risk() {
        return ToolRisk.MODIFIES_GRAPH;
    }

    @Override
    public String describeCall(final JsonNode args) {
        try {
            final Plugin plugin = PluginCatalog.resolve(text(args, "plugin", null));
            final PluginCatalog.Entry entry = PluginCatalog.describe(plugin);
            final String warning = writesFiles(plugin, entry) ? "⚠ May write files. " : "";
            return warning + entry.name() + (entry.description().isBlank() ? "" : " — " + entry.description());
        } catch (final RuntimeException ex) {
            return "Run plugin " + text(args, "plugin", "?");
        }
    }

    /**
     * Check that parameter values can be applied, so a call with bad
     * parameters is sent back to the model rather than shown for approval.
     *
     * @param plugin the plugin.
     * @param parameterJson the parameter values.
     * @return the problem, or null if the values can be applied.
     */
    static String checkParameters(final Plugin plugin, final JsonNode parameterJson) {
        Graph graph = null;
        try {
            graph = GraphManager.getDefault().getActiveGraph();
        } catch (final RuntimeException ex) {
            // no graph manager: check against the plugin's defaults
        }
        try {
            PluginParametersJson.apply(parameterJson, DescribePluginTool.createParameters(plugin, graph));
            return null;
        } catch (final IllegalArgumentException ex) {
            return ex.getMessage() + " Call describe_plugin to see the parameters.";
        }
    }

    static boolean writesFiles(final Plugin plugin, final PluginCatalog.Entry entry) {
        if (entry.tags().contains(PluginTags.EXPORT)) {
            return true;
        }
        final PluginParameters parameters = plugin.createParameters();
        return parameters != null && parameters.getParameters().values().stream()
                .anyMatch(parameter -> FileParameterType.ID.equals(parameter.getType().getId()));
    }

    @Override
    public ToolResult execute(final JsonNode args, final ToolContext context) throws InterruptedException, PluginException {
        final Graph graph = GraphSupport.requireGraph(context, args);
        final Plugin plugin = PluginCatalog.resolve(text(args, "plugin", null));
        final PluginCatalog.Entry entry = PluginCatalog.describe(plugin);
        if (entry.isDataAccess()) {
            return ToolResult.error(entry.name() + " is a Data Access View plugin; use run_data_access_plugin instead.");
        }
        return run(graph, plugin, entry, args.get("parameters"), context);
    }

    /**
     * Run a plugin with parameters from JSON and report what changed.
     */
    static ToolResult run(final Graph graph, final Plugin plugin, final PluginCatalog.Entry entry, final JsonNode parameterJson,
            final ToolContext context) throws InterruptedException, PluginException {
        final PluginParameters parameters = DescribePluginTool.createParameters(plugin, graph);
        final List<String> set;
        try {
            set = PluginParametersJson.apply(parameterJson, parameters);
        } catch (final IllegalArgumentException ex) {
            return ToolResult.error(ex.getMessage() + " Call describe_plugin to see the parameters.");
        }

        context.progress("Running " + entry.name());
        final GraphDiff.Snapshot before = GraphDiff.capture(graph);
        PluginExecution.withPlugin(plugin).withParameters(parameters).interactively(false).executeNow(graph);
        final GraphDiff.Snapshot after = GraphDiff.capture(graph);

        final ObjectNode result = NODES.objectNode();
        result.put("plugin", entry.name());
        result.put("status", "completed");
        final ArrayNode setParameters = result.putArray("parameters_set");
        set.forEach(setParameters::add);
        result.set("changes", GraphDiff.diff(before, after));
        result.put("undo", "The user can undo this with Edit > Undo, or you can call undo_last_change.");
        return ToolResult.ok(json(result), entry.name() + ": " + GraphDiff.summary(before, after));
    }
}
