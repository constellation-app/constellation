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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.arrangements.GraphTaxonomy;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * build a taxonomy by trees
 *
 * @author algol
 * @author sol
 */
public class TaxFromTrees {

    /**
     * Given a Graph, build a taxonomy by trees. First, the graph will be
     * reduced to a simple graph. Then, each taxon will be a set of vertices
     * such that the subgraph induced from the set is a tree and such that the
     * set contains at most one vertex that was a member of a cycle in the
     * original graph (viewed as undirected). The taxonomy has minimum
     * cardinality satisfying theses properties. The representative of each
     * taxon will the member of the taxon that participated in the cycle, if one
     * existed; else, a random member will be chosen. The edges of each tree are
     * also included in the taxon.
     *
     * The supplied vertexAttributeManger is consulted to set the name of the
     * taxa. Each taxon is given the name of the taxon leader's key.
     *
     * If skipSingletonTaxa is true, does not record taxa that have only one
     * element.
     *
     * @param graph the write lock that will be used to perform the operation.
     * @param skipSingletons should singletons be excluded.
     *
     * @return the graph taxonomy.
     */
    public static GraphTaxonomy getTaxonomy(final GraphWriteMethods graph, final boolean skipSingletons) {
        final int vxCount = graph.getVertexCount();

        // Every vertex starts in its own tree; its only member is itself.
        final HashMap<Integer, Set<Integer>> members = new HashMap<>();
        final Map<Integer, Integer> nodeToTaxa = new HashMap<>();
        for (int position = 0; position < vxCount; position++) {
            final int vxId = graph.getVertex(position);

            final Set<Integer> vertices = new HashSet<>();
            vertices.add(vxId);
            members.put(vxId, vertices);
            nodeToTaxa.put(vxId, vxId);
        }

        // Track the vertices that have been "deleted".
        // Any vertex with valences[vxId]==Graph.NOT_FOUND has been "deleted".
        final int[] valences = new int[graph.getVertexCapacity()];
        Arrays.fill(valences, Graph.NOT_FOUND);

        final ArrayDeque<Integer> verticesToPluck = new ArrayDeque<>();

        // Find all vertices with valence 1.
        for (int position = 0; position < vxCount; position++) {
            final int vxId = graph.getVertex(position);

            final int valence = graph.getVertexNeighbourCount(vxId);
            valences[vxId] = valence;
            if (valence == 1) {
                verticesToPluck.addLast(vxId);
            }
        }

        // While there are vertices with valence 1, remove them.
        while (!verticesToPluck.isEmpty()) {
            final int vxToRemoveId = verticesToPluck.removeFirst();

            // Get the neighbour of this vertex: there is only one.
            // Since we're not actually deleting vertices from the graph, we need to check the valences of the neighbours.
            // Any neighbour with valences[neighbourId]==Graph.NOT_FOUND has been "deleted".
            int neighbourId = Graph.NOT_FOUND;
            for (int vxPosition = 0; vxPosition < graph.getVertexNeighbourCount(vxToRemoveId); vxPosition++) {
                neighbourId = graph.getVertexNeighbour(vxToRemoveId, vxPosition);
                if (valences[neighbourId] != Graph.NOT_FOUND) {
                    break;
                }
            }

            if (valences[neighbourId] != Graph.NOT_FOUND) {
                // Get the set of the neighbour's members.
                Set<Integer> neighbourVertices = members.get(neighbourId);

                // Pass the members of the vertex being removed along to the neighbour.
                Set<Integer> vertices = members.get(vxToRemoveId);
                neighbourVertices.addAll(vertices);

                for (int vertex : vertices) {
                    nodeToTaxa.put(vertex, neighbourId);
                }

                // Remove this vertex.
                // This means the neighbour's valence decrements.
                valences[vxToRemoveId] = Graph.NOT_FOUND;
                members.remove(vxToRemoveId);
                valences[neighbourId]--;

                // If the neighbour has valence 1, add it to the queue.
                if (valences[neighbourId] == 1) {
                    verticesToPluck.addLast(neighbourId);
                }
            }
        }

        if (skipSingletons) {
            final Iterator<Integer> i = members.keySet().iterator();
            while (i.hasNext()) {
                final Integer vxId = i.next();
                final Set<Integer> vertices = members.get(vxId);
                if (vertices.size() == 1) {
                    i.remove();
                }
            }
        }

        return new GraphTaxonomy(graph, members, nodeToTaxa);
    }
}
