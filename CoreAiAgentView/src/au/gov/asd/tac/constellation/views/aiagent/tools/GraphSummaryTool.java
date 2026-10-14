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
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.views.aiagent.api.AgentTool;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolContext;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolResult;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolRisk;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.openide.util.lookup.ServiceProvider;

/**
 * Summarises a graph: counts, schema, selection and the most common types.
 *
 * @author doubtsec
 */
@ServiceProvider(service = AgentTool.class, position = 100)
public class GraphSummaryTool extends AbstractAgentTool {

    private static final int TOP_TYPES = 15;

    @Override
    public String name() {
        return "get_graph_summary";
    }

    @Override
    public String description() {
        return "Summarise a graph: its name and schema, vertex and transaction counts, how many are selected, "
                + "and the most common vertex and transaction types. Use this first to orient yourself.";
    }

    @Override
    protected ObjectNode buildSchema() {
        final ObjectNode schema = objectSchema();
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
        final ObjectNode result = summarise(graph);
        return ToolResult.ok(json(result), String.format("%s: %d vertices, %d transactions", result.get("name").asText(),
                result.get("vertex_count").asInt(), result.get("transaction_count").asInt()));
    }

    static ObjectNode summarise(final Graph graph) {
        final ObjectNode result = NODES.objectNode();
        result.put("graph_id", graph.getId());
        result.put("name", GraphSupport.graphName(graph));
        try (final ReadableGraph rg = graph.getReadableGraph()) {
            result.put("schema", rg.getSchema() == null ? "none" : rg.getSchema().getFactory().getLabel());
            result.put("vertex_count", rg.getVertexCount());
            result.put("transaction_count", rg.getTransactionCount());
            result.put("selected_vertices", GraphSupport.countSelected(rg, GraphElementType.VERTEX));
            result.put("selected_transactions", GraphSupport.countSelected(rg, GraphElementType.TRANSACTION));
            result.put("vertex_attribute_count", rg.getAttributeCount(GraphElementType.VERTEX));
            result.put("transaction_attribute_count", rg.getAttributeCount(GraphElementType.TRANSACTION));
            final int vxType = rg.getAttribute(GraphElementType.VERTEX, AnalyticConcept.VertexAttribute.TYPE.getName());
            if (vxType != Graph.NOT_FOUND) {
                final ObjectNode types = result.putObject("top_vertex_types");
                GraphSupport.topValues(rg, GraphElementType.VERTEX, vxType, TOP_TYPES).forEach(types::put);
            }
            final int txType = rg.getAttribute(GraphElementType.TRANSACTION, AnalyticConcept.TransactionAttribute.TYPE.getName());
            if (txType != Graph.NOT_FOUND) {
                final ObjectNode types = result.putObject("top_transaction_types");
                GraphSupport.topValues(rg, GraphElementType.TRANSACTION, txType, TOP_TYPES).forEach(types::put);
            }
        }
        return result;
    }
}
