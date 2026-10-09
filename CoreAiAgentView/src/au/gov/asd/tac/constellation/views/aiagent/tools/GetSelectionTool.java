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
import au.gov.asd.tac.constellation.views.aiagent.api.AgentTool;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolContext;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolResult;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolRisk;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.openide.util.lookup.ServiceProvider;

/**
 * Returns the selected vertices and transactions.
 *
 * @author doubtsec
 */
@ServiceProvider(service = AgentTool.class, position = 160)
public class GetSelectionTool extends AbstractAgentTool {

    @Override
    public String name() {
        return "get_selection";
    }

    @Override
    public String description() {
        return "Return the vertices and transactions the user currently has selected, with their ids and main attributes.";
    }

    @Override
    protected ObjectNode buildSchema() {
        final ObjectNode schema = objectSchema();
        property(schema, "limit", "integer", "Maximum rows of each element type (default 50).");
        property(schema, "graph_id", "string", GRAPH_ID_DESCRIPTION);
        return schema;
    }

    @Override
    public ToolRisk risk() {
        return ToolRisk.READ_ONLY;
    }

    @Override
    public ToolResult execute(final JsonNode args, final ToolContext context) {
        final Graph graph = GraphSupport.requireGraph(context, args);
        final int limit = integer(args, "limit", QueryElementsTool.DEFAULT_LIMIT, 1, QueryElementsTool.MAX_LIMIT);
        final ObjectNode result = NODES.objectNode();
        try (final ReadableGraph rg = graph.getReadableGraph()) {
            for (final String type : new String[]{"vertex", "transaction"}) {
                final ObjectNode query = NODES.objectNode();
                query.put("element_type", type);
                query.put("selected_only", true);
                query.put("limit", limit);
                result.set("vertex".equals(type) ? "vertices" : "transactions", QueryElementsTool.query(rg, query));
            }
        }
        return ToolResult.ok(json(result), String.format("%d vertices, %d transactions selected",
                result.get("vertices").get("total_matches").asInt(), result.get("transactions").get("total_matches").asInt()));
    }
}
