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
import au.gov.asd.tac.constellation.views.aiagent.api.AgentTool;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolContext;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolResult;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolRisk;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;

/**
 * Lists a graph's attributes.
 *
 * @author constellation
 */
@ServiceProvider(service = AgentTool.class, position = 130)
public class GetAttributesTool extends AbstractAgentTool {

    @Override
    public String name() {
        return "get_attributes";
    }

    @Override
    public String description() {
        return "List the attributes (columns) defined for vertices, transactions or the graph itself, with their types and descriptions. "
                + "Use the exact attribute names in other tools.";
    }

    @Override
    protected ObjectNode buildSchema() {
        final ObjectNode schema = objectSchema();
        enumProperty(schema, "element_type", List.of("vertex", "transaction", "graph", "all"), "Which attributes to list (default all).");
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
        final String which = text(args, "element_type", "all");
        final ObjectNode result = NODES.objectNode();
        int total = 0;
        try (final ReadableGraph rg = graph.getReadableGraph()) {
            for (final GraphElementType type : List.of(GraphElementType.VERTEX, GraphElementType.TRANSACTION, GraphElementType.GRAPH)) {
                if (!"all".equalsIgnoreCase(which) && GraphSupport.elementType(which, GraphElementType.VERTEX) != type) {
                    continue;
                }
                final ArrayNode attributes = result.putArray(GraphSupport.typeName(type));
                final int count = rg.getAttributeCount(type);
                for (int i = 0; i < count; i++) {
                    final int attribute = rg.getAttribute(type, i);
                    final ObjectNode node = attributes.addObject();
                    node.put("name", rg.getAttributeName(attribute));
                    node.put("type", rg.getAttributeType(attribute));
                    final String description = rg.getAttributeDescription(attribute);
                    if (description != null && !description.isBlank()) {
                        node.put("description", description);
                    }
                    total++;
                }
            }
        }
        return ToolResult.ok(json(result), total + " attributes");
    }
}
