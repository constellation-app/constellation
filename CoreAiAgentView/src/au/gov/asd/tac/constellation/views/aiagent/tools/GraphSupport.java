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
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolContext;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Helpers shared by the graph tools.
 *
 * @author constellation
 */
public final class GraphSupport {

    public static final String NO_GRAPH = "No graph is open. Ask the user to open a graph or create one with File > New Graph.";

    private GraphSupport() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * The graph a tool should act on: the graph named by the optional
     * {@code graph_id} argument, otherwise the active graph.
     *
     * @param context the tool context.
     * @param args the tool arguments.
     * @return the graph.
     * @throws IllegalStateException if there is no such graph.
     */
    public static Graph requireGraph(final ToolContext context, final JsonNode args) {
        final String graphId = args != null && args.hasNonNull("graph_id") ? args.get("graph_id").asText() : null;
        if (graphId != null && !graphId.isBlank()) {
            final Graph graph = GraphNode.getGraph(graphId);
            if (graph == null) {
                throw new IllegalStateException("No graph with id " + graphId + ". Use list_graphs to see the open graphs.");
            }
            return graph;
        }
        final Graph graph = context.getActiveGraph();
        if (graph == null) {
            throw new IllegalStateException(NO_GRAPH);
        }
        return graph;
    }

    /**
     * @param graph a graph.
     * @return the graph's display name, or its id if it has no node.
     */
    public static String graphName(final Graph graph) {
        if (graph == null) {
            return null;
        }
        try {
            final GraphNode node = GraphNode.getGraphNode(graph);
            if (node != null && node.getDisplayName() != null) {
                return node.getDisplayName();
            }
        } catch (final RuntimeException ex) {
            // fall back to the id
        }
        return graph.getId();
    }

    /**
     * Parse an element type name.
     *
     * @param name "vertex", "node", "transaction", "edge", "graph", ...
     * @param fallback the type to use if the name is null or blank.
     * @return the element type.
     */
    public static GraphElementType elementType(final String name, final GraphElementType fallback) {
        if (name == null || name.isBlank()) {
            return fallback;
        }
        return switch (name.trim().toLowerCase(Locale.ROOT)) {
            case "vertex", "vertices", "node", "nodes" ->
                GraphElementType.VERTEX;
            case "transaction", "transactions", "edge", "edges", "link", "links", "connection", "connections" ->
                GraphElementType.TRANSACTION;
            case "graph", "meta" ->
                GraphElementType.GRAPH;
            default ->
                throw new IllegalArgumentException("Unknown element_type '" + name + "'. Use 'vertex' or 'transaction'.");
        };
    }

    /**
     * @param type an element type.
     * @return a lower case name for the type.
     */
    public static String typeName(final GraphElementType type) {
        return switch (type) {
            case VERTEX ->
                "vertex";
            case TRANSACTION ->
                "transaction";
            default ->
                type.name().toLowerCase(Locale.ROOT);
        };
    }

    /**
     * @param type an element type.
     * @param count how many.
     * @return "vertex"/"vertices" or "transaction"/"transactions".
     */
    public static String plural(final GraphElementType type, final int count) {
        if (type == GraphElementType.VERTEX) {
            return count == 1 ? "vertex" : "vertices";
        }
        return typeName(type) + (count == 1 ? "" : "s");
    }

    /**
     * Find an attribute by name, ignoring case if there is no exact match.
     *
     * @param graph the graph.
     * @param type the element type.
     * @param name the attribute name.
     * @return the attribute id, or {@link Graph#NOT_FOUND}.
     */
    public static int attribute(final GraphReadMethods graph, final GraphElementType type, final String name) {
        if (name == null) {
            return Graph.NOT_FOUND;
        }
        final int exact = graph.getAttribute(type, name);
        if (exact != Graph.NOT_FOUND) {
            return exact;
        }
        final int count = graph.getAttributeCount(type);
        for (int i = 0; i < count; i++) {
            final int attribute = graph.getAttribute(type, i);
            if (graph.getAttributeName(attribute).equalsIgnoreCase(name)) {
                return attribute;
            }
        }
        // models often abbreviate long names: accept a unique prefix
        final String lower = name.toLowerCase(Locale.ROOT);
        int match = Graph.NOT_FOUND;
        for (int i = 0; i < count; i++) {
            final int attribute = graph.getAttribute(type, i);
            if (lower.length() >= 3 && graph.getAttributeName(attribute).toLowerCase(Locale.ROOT).startsWith(lower)) {
                if (match != Graph.NOT_FOUND) {
                    return Graph.NOT_FOUND;
                }
                match = attribute;
            }
        }
        return match;
    }

    /**
     * Like {@link #attribute} but throws a helpful error if the attribute
     * doesn't exist.
     */
    public static int requireAttribute(final GraphReadMethods graph, final GraphElementType type, final String name) {
        final int attribute = attribute(graph, type, name);
        if (attribute == Graph.NOT_FOUND) {
            throw new IllegalArgumentException(String.format("No %s attribute named '%s'. Available attributes: %s",
                    typeName(type), name, String.join(", ", attributeNames(graph, type))));
        }
        return attribute;
    }

    /**
     * @param graph the graph.
     * @param type the element type.
     * @return the names of the attributes of that type.
     */
    public static List<String> attributeNames(final GraphReadMethods graph, final GraphElementType type) {
        final int count = graph.getAttributeCount(type);
        final List<String> names = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            names.add(graph.getAttributeName(graph.getAttribute(type, i)));
        }
        return names;
    }

    /**
     * The attributes shown for an element when the model doesn't ask for
     * specific ones.
     */
    public static List<String> defaultColumns(final GraphReadMethods graph, final GraphElementType type) {
        final List<String> wanted = type == GraphElementType.VERTEX
                ? List.of(VisualConcept.VertexAttribute.LABEL.getName(), VisualConcept.VertexAttribute.IDENTIFIER.getName(),
                        AnalyticConcept.VertexAttribute.TYPE.getName())
                : List.of(AnalyticConcept.TransactionAttribute.TYPE.getName(), VisualConcept.TransactionAttribute.LABEL.getName(),
                        "DateTime");
        final List<String> columns = new ArrayList<>();
        wanted.forEach(name -> {
            if (graph.getAttribute(type, name) != Graph.NOT_FOUND) {
                columns.add(name);
            }
        });
        return columns;
    }

    /**
     * @param graph the graph.
     * @param type vertex or transaction.
     * @return the number of selected elements of the type.
     */
    public static int countSelected(final GraphReadMethods graph, final GraphElementType type) {
        final String name = type == GraphElementType.VERTEX ? VisualConcept.VertexAttribute.SELECTED.getName()
                : VisualConcept.TransactionAttribute.SELECTED.getName();
        final int selected = graph.getAttribute(type, name);
        if (selected == Graph.NOT_FOUND) {
            return 0;
        }
        int count = 0;
        final int elementCount = type.getElementCount(graph);
        for (int position = 0; position < elementCount; position++) {
            if (graph.getBooleanValue(selected, type.getElement(graph, position))) {
                count++;
            }
        }
        return count;
    }

    /**
     * The most common values of an attribute.
     *
     * @param graph the graph.
     * @param type the element type.
     * @param attribute the attribute id.
     * @param limit the maximum number of values.
     * @return value to count, most common first.
     */
    public static Map<String, Integer> topValues(final GraphReadMethods graph, final GraphElementType type, final int attribute, final int limit) {
        final Map<String, Integer> counts = new LinkedHashMap<>();
        final int elementCount = type.getElementCount(graph);
        for (int position = 0; position < elementCount; position++) {
            final String value = graph.getStringValue(attribute, type.getElement(graph, position));
            counts.merge(value == null ? "(empty)" : value, 1, Integer::sum);
        }
        final Map<String, Integer> top = new LinkedHashMap<>();
        counts.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(limit)
                .forEachOrdered(entry -> top.put(entry.getKey(), entry.getValue()));
        return top;
    }

    /**
     * A condition on an attribute value.
     *
     * @param attribute the attribute name.
     * @param op the comparison.
     * @param value the value to compare with, may be null for is_empty and
     * not_empty.
     */
    public record Filter(String attribute, String op, String value) {

        public static final List<String> OPERATORS = List.of("equals", "not_equals", "contains", "starts_with", "ends_with",
                "greater_than", "less_than", "is_empty", "not_empty", "regex");

        /**
         * Parse filters from a JSON array of {attribute, op, value} objects.
         */
        public static List<Filter> parse(final JsonNode filters) {
            final List<Filter> result = new ArrayList<>();
            if (filters == null || filters.isNull()) {
                return result;
            }
            if (!filters.isArray()) {
                throw new IllegalArgumentException("filters must be an array of {attribute, op, value} objects");
            }
            filters.forEach(node -> {
                final String op = node.path("op").asText("equals").toLowerCase(Locale.ROOT);
                if (!OPERATORS.contains(op)) {
                    throw new IllegalArgumentException("Unknown filter op '" + op + "'. Use one of " + OPERATORS);
                }
                result.add(new Filter(node.path("attribute").asText(), op, node.hasNonNull("value") ? node.get("value").asText() : null));
            });
            return result;
        }
    }

    /**
     * A filter bound to an attribute id, ready to test elements.
     */
    public static final class BoundFilter {

        private final Filter filter;
        private final int attribute;
        private final Pattern pattern;
        private final Double number;

        public BoundFilter(final GraphReadMethods graph, final GraphElementType type, final Filter filter) {
            this.filter = filter;
            this.attribute = requireAttribute(graph, type, filter.attribute());
            if ("regex".equals(filter.op())) {
                try {
                    pattern = Pattern.compile(filter.value() == null ? "" : filter.value(), Pattern.CASE_INSENSITIVE);
                } catch (final PatternSyntaxException ex) {
                    throw new IllegalArgumentException("Invalid regex: " + ex.getDescription(), ex);
                }
            } else {
                pattern = null;
            }
            Double parsed = null;
            if (filter.value() != null && ("greater_than".equals(filter.op()) || "less_than".equals(filter.op()))) {
                try {
                    parsed = Double.valueOf(filter.value());
                } catch (final NumberFormatException ex) {
                    parsed = null;
                }
            }
            this.number = parsed;
        }

        public boolean matches(final GraphReadMethods graph, final int element) {
            final String actual = graph.getStringValue(attribute, element);
            final String expected = filter.value() == null ? "" : filter.value();
            final String a = actual == null ? "" : actual;
            return switch (filter.op()) {
                case "equals" ->
                    sameValue(a, expected);
                case "not_equals" ->
                    !sameValue(a, expected);
                case "contains" ->
                    a.toLowerCase(Locale.ROOT).contains(expected.toLowerCase(Locale.ROOT));
                case "starts_with" ->
                    a.toLowerCase(Locale.ROOT).startsWith(expected.toLowerCase(Locale.ROOT));
                case "ends_with" ->
                    a.toLowerCase(Locale.ROOT).endsWith(expected.toLowerCase(Locale.ROOT));
                case "is_empty" ->
                    a.isEmpty();
                case "not_empty" ->
                    !a.isEmpty();
                case "regex" ->
                    pattern.matcher(a).find();
                case "greater_than", "less_than" ->
                    compare(a, expected) * ("greater_than".equals(filter.op()) ? 1 : -1) > 0;
                default ->
                    false;
            };
        }

        /**
         * Equal ignoring case; schema types also match on the last part of
         * their hierarchy, so "Email" matches "Online Identifier.Email".
         */
        static boolean sameValue(final String actual, final String expected) {
            return actual.equalsIgnoreCase(expected)
                    || !expected.isEmpty() && actual.toLowerCase(Locale.ROOT).endsWith("." + expected.toLowerCase(Locale.ROOT));
        }

        private int compare(final String actual, final String expected) {
            if (number != null) {
                try {
                    return Double.compare(Double.parseDouble(actual), number);
                } catch (final NumberFormatException ex) {
                    // fall through to a text comparison
                }
            }
            if (actual.isEmpty()) {
                return 0;
            }
            return actual.compareToIgnoreCase(expected);
        }
    }

    /**
     * Test an element against filters.
     *
     * @param graph the graph.
     * @param element the element id.
     * @param filters the filters.
     * @param matchAll true to require every filter to match, false for any.
     * @return true if the element matches (always true with no filters).
     */
    public static boolean matches(final GraphReadMethods graph, final int element, final List<BoundFilter> filters, final boolean matchAll) {
        if (filters.isEmpty()) {
            return true;
        }
        for (final BoundFilter filter : filters) {
            final boolean match = filter.matches(graph, element);
            if (matchAll && !match) {
                return false;
            }
            if (!matchAll && match) {
                return true;
            }
        }
        return matchAll;
    }

    /**
     * Bind filters to a graph.
     */
    public static List<BoundFilter> bind(final GraphReadMethods graph, final GraphElementType type, final List<Filter> filters) {
        final List<BoundFilter> bound = new ArrayList<>(filters.size());
        filters.forEach(filter -> bound.add(new BoundFilter(graph, type, filter)));
        return bound;
    }
}
