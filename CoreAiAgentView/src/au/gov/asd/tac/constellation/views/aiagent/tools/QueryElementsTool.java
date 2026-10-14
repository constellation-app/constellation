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
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.views.aiagent.api.AgentTool;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolContext;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolResult;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolRisk;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;

/**
 * Finds vertices or transactions matching attribute filters and returns
 * selected columns, a page at a time.
 *
 * @author doubtsec
 */
@ServiceProvider(service = AgentTool.class, position = 140)
public class QueryElementsTool extends AbstractAgentTool {

    static final int DEFAULT_LIMIT = 50;
    static final int MAX_LIMIT = 500;

    @Override
    public String name() {
        return "query_elements";
    }

    @Override
    public String description() {
        return "Find vertices or transactions whose attributes match filters, and return chosen attribute columns, a page at a time. "
                + "Each row has the element id, which other tools (e.g. select_elements) accept. "
                + "To find the top N by a score, use sort_by with limit N. "
                + "Transactions also include their source and destination vertex labels.";
    }

    @Override
    protected ObjectNode buildSchema() {
        final ObjectNode schema = objectSchema();
        enumProperty(schema, "element_type", List.of("vertex", "transaction"), "What to query (default vertex).");
        final ObjectNode filters = property(schema, "filters", "array",
                "Conditions on attribute values. Text comparisons ignore case.");
        final ObjectNode filter = filters.putObject("items");
        filter.put("type", "object");
        final ObjectNode filterProperties = filter.putObject("properties");
        filterProperties.putObject("attribute").put("type", "string").put("description", "Attribute name, e.g. Label or Type.");
        final ArrayNode ops = filterProperties.putObject("op").put("type", "string").putArray("enum");
        GraphSupport.Filter.OPERATORS.forEach(ops::add);
        filterProperties.putObject("value").put("type", "string").put("description", "Value to compare with (not needed for is_empty/not_empty).");
        filter.putArray("required").add("attribute").add("op");
        enumProperty(schema, "match", List.of("all", "any"), "Whether every filter (default) or any filter must match.");
        final ObjectNode attributes = property(schema, "attributes", "array", "Attribute columns to return (default Label, Identifier, Type).");
        attributes.putObject("items").put("type", "string");
        property(schema, "selected_only", "boolean", "Only include selected elements.");
        property(schema, "sort_by", "string", "Attribute to sort by; numbers sort numerically. The attribute is added to the columns.");
        property(schema, "descending", "boolean", "Sort highest first (default true when sort_by is given), so limit=5 gives the top 5.");
        property(schema, "offset", "integer", "Number of matching rows to skip (default 0).");
        property(schema, "limit", "integer", "Maximum rows to return (default " + DEFAULT_LIMIT + ", max " + MAX_LIMIT + ").");
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
        try (final ReadableGraph rg = graph.getReadableGraph()) {
            final ObjectNode result = query(rg, args);
            final GraphElementType type = GraphSupport.elementType(result.get("element_type").asText(), GraphElementType.VERTEX);
            final int total = result.get("total_matches").asInt();
            return ToolResult.ok(json(result), String.format("%d matching %s, returned %d", total, GraphSupport.plural(type, total),
                    result.get("returned").asInt()));
        }
    }

    static ObjectNode query(final GraphReadMethods rg, final JsonNode args) {
        final GraphElementType type = GraphSupport.elementType(text(args, "element_type", "vertex"), GraphElementType.VERTEX);
        if (type != GraphElementType.VERTEX && type != GraphElementType.TRANSACTION) {
            throw new IllegalArgumentException("element_type must be vertex or transaction");
        }
        final List<GraphSupport.BoundFilter> filters = GraphSupport.bind(rg, type, GraphSupport.Filter.parse(args.get("filters")));
        final boolean matchAll = !"any".equalsIgnoreCase(text(args, "match", "all"));
        final boolean selectedOnly = bool(args, "selected_only", false);
        final int offset = integer(args, "offset", 0, 0, Integer.MAX_VALUE);
        final int limit = integer(args, "limit", DEFAULT_LIMIT, 1, MAX_LIMIT);

        final List<String> columns = new ArrayList<>();
        if (args.has("attributes") && args.get("attributes").isArray() && !args.get("attributes").isEmpty()) {
            args.get("attributes").forEach(node -> columns.add(node.asText()));
        } else {
            columns.addAll(GraphSupport.defaultColumns(rg, type));
        }
        final String sortBy = text(args, "sort_by", null);
        if (sortBy != null) {
            final String sortName = rg.getAttributeName(GraphSupport.requireAttribute(rg, type, sortBy));
            if (columns.stream().noneMatch(column -> column.equalsIgnoreCase(sortName))) {
                columns.add(sortName);
            }
        }
        final int[] columnIds = new int[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            columnIds[i] = GraphSupport.requireAttribute(rg, type, columns.get(i));
            columns.set(i, rg.getAttributeName(columnIds[i]));
        }

        final int selectedAttribute = rg.getAttribute(type, type == GraphElementType.VERTEX
                ? VisualConcept.VertexAttribute.SELECTED.getName() : VisualConcept.TransactionAttribute.SELECTED.getName());
        final List<Integer> matches = new ArrayList<>();
        final int count = type.getElementCount(rg);
        for (int position = 0; position < count; position++) {
            final int element = type.getElement(rg, position);
            if (selectedOnly && (selectedAttribute == Graph.NOT_FOUND || !rg.getBooleanValue(selectedAttribute, element))) {
                continue;
            }
            if (GraphSupport.matches(rg, element, filters, matchAll)) {
                matches.add(element);
            }
        }

        if (sortBy != null) {
            final int sortAttribute = GraphSupport.requireAttribute(rg, type, sortBy);
            Comparator<Integer> comparator = Comparator.comparing(element -> sortKey(rg.getStringValue(sortAttribute, element)));
            if (bool(args, "descending", true)) {
                comparator = comparator.reversed();
            }
            matches.sort(comparator);
        }

        final int vertexLabel = rg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.LABEL.getName());
        final ObjectNode result = NODES.objectNode();
        result.put("element_type", GraphSupport.typeName(type));
        result.put("total_matches", matches.size());
        result.put("offset", offset);
        final ArrayNode rows = result.putArray("rows");
        for (int i = offset; i < Math.min(matches.size(), offset + limit); i++) {
            final int element = matches.get(i);
            final ObjectNode row = rows.addObject();
            row.put("id", element);
            if (type == GraphElementType.TRANSACTION) {
                row.put("source", vertexName(rg, vertexLabel, rg.getTransactionSourceVertex(element)));
                row.put("destination", vertexName(rg, vertexLabel, rg.getTransactionDestinationVertex(element)));
            }
            for (int c = 0; c < columnIds.length; c++) {
                putValue(row, columns.get(c), rg, columnIds[c], element);
            }
        }
        result.put("returned", rows.size());
        if (offset + rows.size() < matches.size()) {
            result.put("next_offset", offset + rows.size());
        }
        return result;
    }

    /**
     * Numbers and booleans are returned as JSON numbers and booleans, which
     * models read more reliably than strings such as "8.2E-4".
     */
    static void putValue(final ObjectNode row, final String name, final GraphReadMethods rg, final int attribute, final int element) {
        switch (rg.getNativeAttributeType(attribute)) {
            case BYTE, SHORT, INT, LONG ->
                row.put(name, rg.getLongValue(attribute, element));
            case FLOAT, DOUBLE -> {
                final double value = rg.getDoubleValue(attribute, element);
                if (Double.isFinite(value)) {
                    row.put(name, value);
                } else {
                    row.putNull(name);
                }
            }
            case BOOLEAN ->
                row.put(name, rg.getBooleanValue(attribute, element));
            default ->
                row.put(name, rg.getStringValue(attribute, element));
        }
    }

    private static String vertexName(final GraphReadMethods rg, final int labelAttribute, final int vertex) {
        final String label = labelAttribute == Graph.NOT_FOUND ? null : rg.getStringValue(labelAttribute, vertex);
        return label == null ? "#" + vertex : label;
    }

    private static SortKey sortKey(final String value) {
        if (value == null) {
            return new SortKey(Double.NEGATIVE_INFINITY, "");
        }
        try {
            return new SortKey(Double.parseDouble(value), value);
        } catch (final NumberFormatException ex) {
            return new SortKey(Double.NaN, value.toLowerCase(java.util.Locale.ROOT));
        }
    }

    /**
     * Numbers sort before text, numerically; text sorts alphabetically.
     */
    private record SortKey(double number, String text) implements Comparable<SortKey> {

        @Override
        public int compareTo(final SortKey other) {
            final boolean thisNumber = !Double.isNaN(number);
            final boolean otherNumber = !Double.isNaN(other.number);
            if (thisNumber && otherNumber) {
                return Double.compare(number, other.number);
            }
            if (thisNumber != otherNumber) {
                return thisNumber ? -1 : 1;
            }
            return text.compareTo(other.text);
        }
    }
}
