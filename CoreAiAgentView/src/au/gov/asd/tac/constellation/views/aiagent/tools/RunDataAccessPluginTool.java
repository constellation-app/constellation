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
import au.gov.asd.tac.constellation.graph.node.create.NewDefaultSchemaGraphAction;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.views.aiagent.api.AgentTool;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolContext;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolResult;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolRisk;
import au.gov.asd.tac.constellation.views.dataaccess.CoreGlobalParameters;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * Runs a Data Access View plugin, creating a graph first if none is open.
 *
 * @author doubtsec
 */
@ServiceProvider(service = AgentTool.class, position = 410)
public class RunDataAccessPluginTool extends AbstractAgentTool {

    static final String DEFAULT_QUERY_NAME = "Assistant query";
    private static final long NEW_GRAPH_TIMEOUT_MILLIS = 30_000;

    @Override
    public String name() {
        return "run_data_access_plugin";
    }

    @Override
    public String description() {
        return "Run a Data Access View plugin (from list_data_access_plugins or search_plugins). These can query external systems. "
                + "Pass parameters keyed by the ids from describe_plugin. If no graph is open, a new graph is created first. "
                + "The run can be undone.";
    }

    @Override
    protected ObjectNode buildSchema() {
        final ObjectNode schema = objectSchema();
        property(schema, "plugin", "string", "The Data Access View plugin name.");
        final ObjectNode parameters = property(schema, "parameters", "object", "Parameter values keyed by parameter id.");
        parameters.put("additionalProperties", true);
        property(schema, "query_name", "string", "A name for the query, recorded with the results (default '" + DEFAULT_QUERY_NAME + "').");
        property(schema, "datetime_range", "string", "Optional time range for plugins that use one, e.g. 'P7D' or "
                + "'2024-01-01T00:00Z[UTC];2024-01-31T00:00Z[UTC]'.");
        required(schema, "plugin");
        return schema;
    }

    @Override
    public JsonNode normaliseArguments(final JsonNode args) {
        JsonNode normalised = alias(args, "plugin", "plugin_name", "name", "plugin_id", "id", "plugin_class", "class_name");
        normalised = alias(normalised, "parameters", "params", "args", "arguments", "plugin_parameters");
        return RunPluginTool.nestStrayParameters(normalised, Set.of("plugin", "parameters", "query_name", "datetime_range"));
    }

    @Override
    public String checkArguments(final JsonNode args) {
        try {
            final Plugin plugin = PluginCatalog.resolve(text(args, "plugin", null));
            final PluginCatalog.Entry entry = PluginCatalog.describe(plugin);
            return entry.isDataAccess() ? RunPluginTool.checkParameters(plugin, args.get("parameters"))
                    : entry.name() + " is not a Data Access View plugin; use run_plugin instead.";
        } catch (final IllegalArgumentException ex) {
            return ex.getMessage();
        }
    }

    @Override
    public ToolRisk risk() {
        return ToolRisk.EXTERNAL_QUERY;
    }

    @Override
    public String describeCall(final JsonNode args) {
        try {
            final PluginCatalog.Entry entry = PluginCatalog.describe(PluginCatalog.resolve(text(args, "plugin", null)));
            return "Data Access: " + entry.name() + " (" + entry.dataAccessType() + ")"
                    + (entry.description().isBlank() ? "" : " — " + entry.description());
        } catch (final RuntimeException ex) {
            return "Run Data Access plugin " + text(args, "plugin", "?");
        }
    }

    @Override
    public ToolResult execute(final JsonNode args, final ToolContext context) throws InterruptedException, PluginException {
        final Plugin plugin = PluginCatalog.resolve(text(args, "plugin", null));
        final PluginCatalog.Entry entry = PluginCatalog.describe(plugin);
        if (!entry.isDataAccess()) {
            return ToolResult.error(entry.name() + " is not a Data Access View plugin; use run_plugin instead.");
        }

        Graph graph = context.getActiveGraph();
        if (graph == null) {
            context.progress("Creating a new graph");
            graph = createGraph();
        }

        // overlay the Data Access View global parameters, as QueryPhasePane.runPlugins does
        final ObjectNode parameters = args.get("parameters") instanceof ObjectNode object ? object.deepCopy() : NODES.objectNode();
        final var created = DescribePluginTool.createParameters(plugin, graph);
        if (created != null && created.hasParameter(CoreGlobalParameters.QUERY_NAME_PARAMETER_ID)
                && !parameters.has(CoreGlobalParameters.QUERY_NAME_PARAMETER_ID)) {
            parameters.put(CoreGlobalParameters.QUERY_NAME_PARAMETER_ID, text(args, "query_name", DEFAULT_QUERY_NAME));
        }
        final String range = text(args, "datetime_range", null);
        if (range != null && created != null && created.hasParameter(CoreGlobalParameters.DATETIME_RANGE_PARAMETER_ID)
                && !parameters.has(CoreGlobalParameters.DATETIME_RANGE_PARAMETER_ID)) {
            parameters.put(CoreGlobalParameters.DATETIME_RANGE_PARAMETER_ID, range);
        }
        return RunPluginTool.run(graph, plugin, entry, parameters, context);
    }

    private static Graph createGraph() throws InterruptedException {
        try {
            SwingUtilities.invokeAndWait(() -> new NewDefaultSchemaGraphAction().actionPerformed(null));
        } catch (final InvocationTargetException ex) {
            throw new IllegalStateException("Couldn't create a new graph: " + ex.getCause().getMessage(), ex.getCause());
        }
        final long deadline = System.currentTimeMillis() + NEW_GRAPH_TIMEOUT_MILLIS;
        Graph graph = GraphManager.getDefault().getActiveGraph();
        while (graph == null && System.currentTimeMillis() < deadline) {
            Thread.sleep(100);
            graph = GraphManager.getDefault().getActiveGraph();
        }
        if (graph == null) {
            throw new IllegalStateException("Timed out waiting for a new graph to open");
        }
        return graph;
    }
}
