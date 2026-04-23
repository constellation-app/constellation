/*
 * Copyright 2010-2025 Australian Signals Directorate
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
import org.eclipse.collections.api.iterator.IntIterator;
import org.eclipse.collections.api.map.primitive.MutableIntIntMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;

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
        final MutableIntObjectMap<MutableIntSet> members = new IntObjectHashMap<>();
        final MutableIntIntMap nodeToTaxa = new IntIntHashMap();
        for (int position = 0; position < vxCount; position++) {
            final int vxId = graph.getVertex(position);

            final MutableIntSet vertices = new IntHashSet();
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
                final MutableIntSet neighbourVertices = members.get(neighbourId);

                // Pass the members of the vertex being removed along to the neighbour.
                final MutableIntSet vertices = members.get(vxToRemoveId);
                neighbourVertices.addAll(vertices);

                final IntIterator verticesIter = vertices.intIterator();
                while (verticesIter.hasNext()) {
                    final int vertex = verticesIter.next();
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
            members.removeIf((key, value) -> value.size() == 1);
        }

        return new GraphTaxonomy(graph, members, nodeToTaxa);
    }
}
