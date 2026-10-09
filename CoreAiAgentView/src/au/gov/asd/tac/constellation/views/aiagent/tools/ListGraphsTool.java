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
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.views.aiagent.api.AgentTool;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolContext;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolResult;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolRisk;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;
import org.openide.util.lookup.ServiceProvider;

/**
 * Lists the open graphs.
 *
 * @author constellation
 */
@ServiceProvider(service = AgentTool.class, position = 110)
public class ListGraphsTool extends AbstractAgentTool {

    @Override
    public String name() {
        return "list_graphs";
    }

    @Override
    public String description() {
        return "List the graphs open in Constellation with their ids, names and sizes, and which one is active.";
    }

    @Override
    protected ObjectNode buildSchema() {
        return objectSchema();
    }

    @Override
    public ToolRisk risk() {
        return ToolRisk.READ_ONLY;
    }

    @Override
    public ToolResult execute(final JsonNode args, final ToolContext context) {
        final Graph active = context.getActiveGraph();
        final ArrayNode graphs = NODES.arrayNode();
        for (final Map.Entry<String, Graph> entry : GraphNode.getAllGraphs().entrySet()) {
            final Graph graph = entry.getValue();
            final ObjectNode node = graphs.addObject();
            node.put("graph_id", entry.getKey());
            node.put("name", GraphSupport.graphName(graph));
            node.put("active", graph == active);
            try (final ReadableGraph rg = graph.getReadableGraph()) {
                node.put("vertex_count", rg.getVertexCount());
                node.put("transaction_count", rg.getTransactionCount());
            }
        }
        return ToolResult.ok(json(graphs), graphs.size() + " open graph" + (graphs.size() == 1 ? "" : "s"));
    }
}
