/*
 * Copyright 2010-2021 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.arrangements.tree;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.arrangements.GraphTaxonomy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Each taxon consists of nodes that have the same set of neighbours.
 *
 * @author algol
 */
public class TaxFromNeighbours {

    /**
     * Produce a taxonomy of the given graph; each taxon consists of vertices
     * that have the same neighbors.
     *
     * @param graph the write lock that will be used to perform the operation.
     * @param verticesToConsider the set of vertices to be included.
     *
     * @return the graph taxonomy.
     */
    public static GraphTaxonomy getTaxonomy(final GraphWriteMethods graph, final Iterable<Integer> verticesToConsider) {
        // We'll use a Map<Set, Set<Integer>>, where a Set key represents a set of neighbour vertexIds defined by
        // the bits that are set.
        final Map<Set<Integer>, Set<Integer>> commonNeighbours = new HashMap<>();
        for (final int vxId : verticesToConsider) {
            // Get neighbours of this vertex.
            final HashSet<Integer> neighbours = new HashSet<>();
            final int nn = graph.getVertexNeighbourCount(vxId);
            for (int vxPosition = 0; vxPosition < nn; vxPosition++) {
                final int nbId = graph.getVertexNeighbour(vxId, vxPosition);
                neighbours.add(nbId);
            }

            if (!commonNeighbours.containsKey(neighbours)) {
                final Set<Integer> vertices = new HashSet<>();
                commonNeighbours.put(neighbours, vertices);
            }

            final Set<Integer> vertices = commonNeighbours.get(neighbours);
            vertices.add(vxId);
        }

        // Now each key (a set of neighbours) maps to a set of vertices that have those neighbours in common).
        final Map<Integer, Set<Integer>> tax = new HashMap<>();
        final Map<Integer, Integer> nodeToTaxa = new HashMap<>();
        for (final Map.Entry<Set<Integer>, Set<Integer>> entry : commonNeighbours.entrySet()) {
            final Set<Integer> vertices = entry.getValue();
            final Integer key = vertices.iterator().next();
            tax.put(key, vertices);
            for (final int vertex : vertices) {
                nodeToTaxa.put(vertex, key);
            }
        }

        return new GraphTaxonomy(graph, tax, nodeToTaxa);
    }
}
