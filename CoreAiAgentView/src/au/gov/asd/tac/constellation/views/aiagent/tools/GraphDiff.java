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
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Before and after snapshots of a graph, used to tell the model what a plugin
 * changed (plugin execution returns no value).
 *
 * @author doubtsec
 */
public final class GraphDiff {

    private GraphDiff() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * A snapshot of the parts of a graph we report on.
     *
     * @param positions a hash of each vertex's x, y and z, by vertex id, so we
     * can report how many vertices a plugin moved (arrangements only move the
     * selected vertices when there is a selection).
     */
    public record Snapshot(int vertices, int transactions, int selectedVertices, int selectedTransactions,
            Set<String> vertexAttributes, Set<String> transactionAttributes, Set<String> graphAttributes, long modificationCounter,
            Map<Integer, Integer> positions) {
    }

    public static Snapshot capture(final Graph graph) {
        try (final ReadableGraph rg = graph.getReadableGraph()) {
            return new Snapshot(rg.getVertexCount(), rg.getTransactionCount(),
                    GraphSupport.countSelected(rg, GraphElementType.VERTEX), GraphSupport.countSelected(rg, GraphElementType.TRANSACTION),
                    new LinkedHashSet<>(GraphSupport.attributeNames(rg, GraphElementType.VERTEX)),
                    new LinkedHashSet<>(GraphSupport.attributeNames(rg, GraphElementType.TRANSACTION)),
                    new LinkedHashSet<>(GraphSupport.attributeNames(rg, GraphElementType.GRAPH)),
                    rg.getGlobalModificationCounter(), positions(rg));
        }
    }

    private static Map<Integer, Integer> positions(final ReadableGraph rg) {
        final int x = VisualConcept.VertexAttribute.X.get(rg);
        final int y = VisualConcept.VertexAttribute.Y.get(rg);
        final int z = VisualConcept.VertexAttribute.Z.get(rg);
        final Map<Integer, Integer> positions = new HashMap<>();
        if (x == Graph.NOT_FOUND || y == Graph.NOT_FOUND) {
            return positions;
        }
        for (int position = 0; position < rg.getVertexCount(); position++) {
            final int vertex = rg.getVertex(position);
            positions.put(vertex, Objects.hash(rg.getFloatValue(x, vertex), rg.getFloatValue(y, vertex),
                    z == Graph.NOT_FOUND ? 0F : rg.getFloatValue(z, vertex)));
        }
        return positions;
    }

    /**
     * @return the number of vertices in both snapshots whose position changed.
     */
    static int moved(final Snapshot before, final Snapshot after) {
        int moved = 0;
        for (final Map.Entry<Integer, Integer> entry : after.positions().entrySet()) {
            final Integer old = before.positions().get(entry.getKey());
            if (old != null && !old.equals(entry.getValue())) {
                moved++;
            }
        }
        return moved;
    }

    /**
     * Describe what changed between two snapshots.
     *
     * @param before the earlier snapshot.
     * @param after the later snapshot.
     * @return a JSON description of the changes.
     */
    public static ObjectNode diff(final Snapshot before, final Snapshot after) {
        final ObjectNode result = JsonNodeFactory.instance.objectNode();
        result.put("graph_modified", before.modificationCounter() != after.modificationCounter());
        result.put("vertices", change(before.vertices(), after.vertices()));
        result.put("transactions", change(before.transactions(), after.transactions()));
        result.put("selected_vertices", change(before.selectedVertices(), after.selectedVertices()));
        result.put("selected_transactions", change(before.selectedTransactions(), after.selectedTransactions()));
        result.put("vertices_moved", moved(before, after));
        added(result, "new_vertex_attributes", before.vertexAttributes(), after.vertexAttributes());
        added(result, "new_transaction_attributes", before.transactionAttributes(), after.transactionAttributes());
        added(result, "new_graph_attributes", before.graphAttributes(), after.graphAttributes());
        added(result, "removed_vertex_attributes", after.vertexAttributes(), before.vertexAttributes());
        added(result, "removed_transaction_attributes", after.transactionAttributes(), before.transactionAttributes());
        return result;
    }

    /**
     * A one line summary of a diff.
     */
    public static String summary(final Snapshot before, final Snapshot after) {
        final StringBuilder summary = new StringBuilder();
        if (before.vertices() != after.vertices()) {
            summary.append(String.format("vertices %d→%d; ", before.vertices(), after.vertices()));
        }
        if (before.transactions() != after.transactions()) {
            summary.append(String.format("transactions %d→%d; ", before.transactions(), after.transactions()));
        }
        if (before.selectedVertices() != after.selectedVertices()) {
            summary.append(String.format("selected vertices %d→%d; ", before.selectedVertices(), after.selectedVertices()));
        }
        final int moved = moved(before, after);
        if (moved > 0) {
            summary.append(String.format("moved %d of %d vertices; ", moved, after.vertices()));
        }
        final Set<String> newAttributes = new LinkedHashSet<>(after.vertexAttributes());
        newAttributes.removeAll(before.vertexAttributes());
        final Set<String> newTxAttributes = new LinkedHashSet<>(after.transactionAttributes());
        newTxAttributes.removeAll(before.transactionAttributes());
        newAttributes.addAll(newTxAttributes);
        if (!newAttributes.isEmpty()) {
            summary.append("new attributes ").append(String.join(", ", newAttributes)).append("; ");
        }
        if (summary.isEmpty()) {
            return before.modificationCounter() != after.modificationCounter() ? "graph values changed" : "no change to the graph";
        }
        return summary.substring(0, summary.length() - 2);
    }

    private static String change(final int before, final int after) {
        return before == after ? Integer.toString(after) : String.format("%d -> %d", before, after);
    }

    private static void added(final ObjectNode result, final String name, final Set<String> before, final Set<String> after) {
        final Set<String> added = new LinkedHashSet<>(after);
        added.removeAll(before);
        if (!added.isEmpty()) {
            final ArrayNode array = result.putArray(name);
            added.forEach(array::add);
        }
    }
}
