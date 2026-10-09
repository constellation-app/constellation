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
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecutor;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.arrangements.ArrangementPluginRegistry;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.views.aiagent.api.AgentTool;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolContext;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolResult;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolRisk;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;

/**
 * Adds vertices and transactions to the graph from a table of records, as the
 * REST API's add_recordstore service does.
 *
 * @author doubtsec
 */
@ServiceProvider(service = AgentTool.class, position = 500)
public class AddRecordsTool extends AbstractAgentTool {

    static final int MAX_ROWS = 2000;

    @Override
    public String name() {
        return "add_records";
    }

    @Override
    public String description() {
        return "Add vertices and transactions to the graph from a table. Column names use Constellation record store keys: "
                + "'source.<Attribute>' for a vertex, plus 'destination.<Attribute>' and 'transaction.<Attribute>' for a connection. "
                + "Example: columns ['source.Identifier','source.Type','destination.Identifier','destination.Type','transaction.Type'], "
                + "rows [['alice@example.com','Email','bob@example.com','Email','Communication']]. "
                + "Vertices with the same Identifier and Type are merged. The change is undoable.";
    }

    @Override
    protected ObjectNode buildSchema() {
        final ObjectNode schema = objectSchema();
        final ObjectNode columns = property(schema, "columns", "array", "Column keys, e.g. 'source.Identifier'.");
        columns.putObject("items").put("type", "string");
        final ObjectNode rows = property(schema, "rows", "array", "Rows of values, one value per column (null to skip).");
        rows.putObject("items").put("type", "array").putObject("items").put("type", "string");
        property(schema, "arrange", "boolean", "Arrange the graph afterwards (default true).");
        property(schema, "complete_with_schema", "boolean", "Fill in default attributes such as colours and icons (default true).");
        property(schema, "graph_id", "string", GRAPH_ID_DESCRIPTION);
        required(schema, "columns", "rows");
        return schema;
    }

    @Override
    public ToolRisk risk() {
        return ToolRisk.MODIFIES_GRAPH;
    }

    @Override
    public String describeCall(final JsonNode args) {
        final int rows = args.has("rows") ? args.get("rows").size() : 0;
        return String.format("Add %d record%s to the graph", rows, rows == 1 ? "" : "s");
    }

    @Override
    public ToolResult execute(final JsonNode args, final ToolContext context) throws InterruptedException, PluginException {
        final Graph graph = GraphSupport.requireGraph(context, args);
        final List<String> columns = new ArrayList<>();
        args.get("columns").forEach(node -> columns.add(node.asText()));
        if (columns.isEmpty()) {
            return ToolResult.error("columns must not be empty");
        }
        final JsonNode rows = args.get("rows");
        if (rows.size() > MAX_ROWS) {
            return ToolResult.error("At most " + MAX_ROWS + " rows can be added at once");
        }

        final RecordStore recordStore = new GraphRecordStore();
        boolean hasTransactions = false;
        for (final JsonNode row : rows) {
            if (!row.isArray()) {
                return ToolResult.error("Each row must be an array of values");
            }
            recordStore.add();
            for (int i = 0; i < columns.size() && i < row.size(); i++) {
                final JsonNode value = row.get(i);
                if (value != null && !value.isNull()) {
                    recordStore.set(columns.get(i), value.asText());
                }
            }
            hasTransactions |= columns.stream().anyMatch(c -> c.startsWith(GraphRecordStoreUtilities.TRANSACTION));
            if (hasTransactions && !columns.contains(GraphRecordStoreUtilities.TRANSACTION + "Source")) {
                recordStore.set(GraphRecordStoreUtilities.TRANSACTION + "Source", "AI Assistant");
            }
        }

        final GraphDiff.Snapshot before = GraphDiff.capture(graph);
        final AddRecordsPlugin plugin = new AddRecordsPlugin(recordStore, bool(args, "complete_with_schema", true),
                bool(args, "arrange", true), graph);
        PluginExecutor.startWith(plugin).followedBy(InteractiveGraphPluginRegistry.RESET_VIEW).executeNow(graph);
        final GraphDiff.Snapshot after = GraphDiff.capture(graph);
        final ObjectNode result = NODES.objectNode();
        result.put("records", rows.size());
        result.set("changes", GraphDiff.diff(before, after));
        return ToolResult.ok(json(result), GraphDiff.summary(before, after));
    }

    /**
     * Adds a record store to the graph as one undoable step.
     */
    @PluginInfo(pluginType = PluginType.IMPORT, tags = {PluginTags.IMPORT})
    static final class AddRecordsPlugin extends SimpleEditPlugin {

        private final RecordStore recordStore;
        private final boolean completeWithSchema;
        private final boolean arrange;
        private final Graph graph;

        AddRecordsPlugin(final RecordStore recordStore, final boolean completeWithSchema, final boolean arrange, final Graph graph) {
            this.recordStore = recordStore;
            this.completeWithSchema = completeWithSchema;
            this.arrange = arrange;
            this.graph = graph;
        }

        @Override
        public String getName() {
            return "AI Assistant: Add Records";
        }

        @Override
        protected void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            GraphRecordStoreUtilities.addRecordStoreToGraph(wg, recordStore, false, completeWithSchema, null);
            if (arrange) {
                // as in the REST API: arrange in a later write so new and existing vertices are laid out together
                PluginExecutor.startWith(ArrangementPluginRegistry.GRID_COMPOSITE)
                        .followedBy(ArrangementPluginRegistry.PENDANTS)
                        .followedBy(ArrangementPluginRegistry.UNCOLLIDE)
                        .executeWriteLater(graph);
            }
        }
    }
}
