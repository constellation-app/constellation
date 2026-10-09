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
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openide.util.lookup.ServiceProvider;

/**
 * Summary statistics for one attribute.
 *
 * @author constellation
 */
@ServiceProvider(service = AgentTool.class, position = 150)
public class AttributeStatsTool extends AbstractAgentTool {

    @Override
    public String name() {
        return "attribute_stats";
    }

    @Override
    public String description() {
        return "Summarise one attribute: how many elements have a value, how many distinct values there are, the most common values, "
                + "and min/max/mean when the values are numbers.";
    }

    @Override
    protected ObjectNode buildSchema() {
        final ObjectNode schema = objectSchema();
        enumProperty(schema, "element_type", List.of("vertex", "transaction"), "Element type (default vertex).");
        property(schema, "attribute", "string", "The attribute name.");
        property(schema, "top_n", "integer", "How many of the most common values to return (default 10, max 100).");
        property(schema, "graph_id", "string", GRAPH_ID_DESCRIPTION);
        required(schema, "attribute");
        return schema;
    }

    @Override
    public JsonNode normaliseArguments(final JsonNode args) {
        return alias(args, "attribute", "attribute_name", "name", "column");
    }

    @Override
    public ToolRisk risk() {
        return ToolRisk.READ_ONLY;
    }

    @Override
    public ToolResult execute(final JsonNode args, final ToolContext context) {
        final Graph graph = GraphSupport.requireGraph(context, args);
        final GraphElementType type = GraphSupport.elementType(text(args, "element_type", "vertex"), GraphElementType.VERTEX);
        final int topN = integer(args, "top_n", 10, 1, 100);
        final ObjectNode result = NODES.objectNode();
        try (final ReadableGraph rg = graph.getReadableGraph()) {
            final int attribute = GraphSupport.requireAttribute(rg, type, text(args, "attribute", null));
            result.put("element_type", GraphSupport.typeName(type));
            result.put("attribute", rg.getAttributeName(attribute));
            result.put("attribute_type", rg.getAttributeType(attribute));
            final int count = type.getElementCount(rg);
            int empty = 0;
            int numeric = 0;
            double min = Double.POSITIVE_INFINITY;
            double max = Double.NEGATIVE_INFINITY;
            double sum = 0;
            final Set<String> distinct = new HashSet<>();
            for (int position = 0; position < count; position++) {
                final String value = rg.getStringValue(attribute, type.getElement(rg, position));
                if (value == null || value.isEmpty()) {
                    empty++;
                    continue;
                }
                distinct.add(value);
                try {
                    final double number = Double.parseDouble(value);
                    if (!Double.isNaN(number)) {
                        numeric++;
                        min = Math.min(min, number);
                        max = Math.max(max, number);
                        sum += number;
                    }
                } catch (final NumberFormatException ex) {
                    // not numeric
                }
            }
            result.put("element_count", count);
            result.put("with_value", count - empty);
            result.put("empty", empty);
            result.put("distinct_values", distinct.size());
            if (numeric > 0 && numeric == count - empty) {
                result.put("min", min);
                result.put("max", max);
                result.put("mean", sum / numeric);
            }
            final ObjectNode top = result.putObject("top_values");
            GraphSupport.topValues(rg, type, attribute, topN).forEach(top::put);
        }
        return ToolResult.ok(json(result), String.format("%s: %d distinct values", result.get("attribute").asText(),
                result.get("distinct_values").asInt()));
    }
}
