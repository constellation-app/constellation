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

import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.views.aiagent.api.AgentTool;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolContext;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolPolicy;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolResult;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolRisk;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javax.swing.SwingUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * Makes another open graph the active graph.
 *
 * @author constellation
 */
@ServiceProvider(service = AgentTool.class, position = 120)
public class SetActiveGraphTool extends AbstractAgentTool {

    @Override
    public String name() {
        return "set_active_graph";
    }

    @Override
    public String description() {
        return "Make another open graph the active graph, so later tools act on it. Use list_graphs to find graph ids.";
    }

    @Override
    protected ObjectNode buildSchema() {
        final ObjectNode schema = objectSchema();
        property(schema, "graph_id", "string", "The id of the graph to make active.");
        required(schema, "graph_id");
        return schema;
    }

    @Override
    public JsonNode normaliseArguments(final JsonNode args) {
        return alias(args, "graph_id", "id", "graph");
    }

    @Override
    public ToolRisk risk() {
        return ToolRisk.READ_ONLY;
    }

    @Override
    public ToolPolicy defaultPolicy() {
        return ToolPolicy.ASK;
    }

    @Override
    public String describeCall(final JsonNode args) {
        final String graphId = text(args, "graph_id", "");
        final GraphNode node = GraphNode.getGraphNode(graphId);
        return "Switch to graph " + (node == null ? graphId : "\"" + node.getDisplayName() + "\"");
    }

    @Override
    public ToolResult execute(final JsonNode args, final ToolContext context) throws Exception {
        final String graphId = text(args, "graph_id", "");
        final GraphNode node = GraphNode.getGraphNode(graphId);
        if (node == null || node.getTopComponent() == null) {
            return ToolResult.error("No graph with id " + graphId + ". Use list_graphs to see the open graphs.");
        }
        SwingUtilities.invokeAndWait(() -> node.getTopComponent().requestActive());
        return ToolResult.ok("Graph \"" + node.getDisplayName() + "\" is now active.", "Switched to " + node.getDisplayName());
    }
}
