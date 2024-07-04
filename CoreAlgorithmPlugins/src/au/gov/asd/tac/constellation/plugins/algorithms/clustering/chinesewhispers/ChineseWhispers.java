/*
 * Copyright 2010-2024 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.chinesewhispers;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.ClusteringConcept;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Chinese whispers clustering based on BiemannTextGraph06.
 * <p>
 * Chinese Whispers - an Efficient Graph Clustering Algorithm and its
 * Application to Natural Language processing Problems. Chris Biemann University
 * of Liepzig, NLP Department
 * <p>
 * Intuitively, the algorithm works as follows in a bottom-up fashion: First all
 * nodes get different classes. Then the nodes are processed for a small number
 * of iterations and inherit the strongest class in the local neighborhood. This
 * is the class whose sum of edge weights to the current node is maximal. In
 * case of multiple strongest classes, one is chosen randomly. Regions of the
 * same class stabilize during the iteration and grow until they reach the
 * border of a stable region of another class. Note that classes are updated
 * immediately: a node can obtain classes from the neighborhood that were
 * introduced there in the same iteration.
 * <pre>
 * initialize: forall vi in V: class(vi)=i while changes: forall v in V,
 * randomized order: class(v)=highest ranked class in neighborhood of v;
 * </pre> Apart from ties, the classes usually do not change any more after a
 * handful of iterations. The number of iterations depends on the diameter of
 * the graph: the larger the distance between two nodes is, the more iterations
 * it takes to percolate information from one to another.
 *
 * @author algol
 */
public final class ChineseWhispers {

    private final GraphWriteMethods wg;
    private final int clusterId;

    // Map a vxId to it's cluster number.
    private int[] vxClusters;

    private final SecureRandom random = new SecureRandom();

    public ChineseWhispers(final GraphWriteMethods wg) {
        this.wg = wg;
        clusterId = ClusteringConcept.VertexAttribute.CHINESE_WHISPERS_CLUSTER.ensure(wg);
    }

    public void cluster() {
        final int vxCount = wg.getVertexCount();

        // An array containing vertex positions in a random order.
        final int[] positions = new int[vxCount];

        // For each vertex index, the cluster that the vertex is in.
        vxClusters = new int[wg.getVertexCapacity()];
        Arrays.fill(vxClusters, Graph.NOT_FOUND);
        for (int position = 0; position < vxCount; position++) {
            final int vxId = wg.getVertex(position);
            positions[position] = position;
            vxClusters[vxId] = position;
        }

        // For now we'll use a fixed number of iterations and break if there are
        // no changes (or the number of changes increases). Figure out a better
        // limit later if we want to. (See class comment above.)
        int prevChanges = Integer.MAX_VALUE;
        for (int iteration = 0; iteration < 50; iteration++) {
            int changes = 0;
            shuffle(random, positions);

            for (final int position : positions) {
                final int vxId = wg.getVertex(position);

                // Find the vertices connected to this vertex by the highest sum
                // of link weights. Since in general we don;t do weights, we use
                // the maximum number of transactions connected to a vertex.
                final ArrayList<Integer> candidates = new ArrayList<>();
                final int neighbours = wg.getVertexLinkCount(vxId);
                int maxWeight = 0;
                for (int npos = 0; npos < neighbours; npos++) {
                    final int linkId = wg.getVertexLink(vxId, npos);

                    final int weight = wg.getLinkTransactionCount(linkId);
                    if (weight > maxWeight) {
                        // This weight is a new maximum, so clear the previous candidates.
                        candidates.clear();
                        maxWeight = weight;
                    }

                    if (weight == maxWeight) {
                        // Find the vertex at the other end of the link and remember it as a candidate.
                        final int neighbourVxId = GraphElementType.LINK.getOtherVertex(wg, linkId, vxId);
                        candidates.add(neighbourVxId);
                    }
                }

                if (!candidates.isEmpty()) {
                    // Choose a random vertex from the candidates and change the cluster if it's different.
                    final int rix = random.nextInt(candidates.size());
                    final int rneighbour = candidates.get(rix);
                    if (vxClusters[vxId] != vxClusters[rneighbour]) {
                        vxClusters[vxId] = vxClusters[rneighbour];
                        changes++;
                    }
                }
            }

            // If there were no changes of cluster in this iteration, or there
            // were more changes than last time, we're finished.
            if (changes == 0 || changes > prevChanges) {
                break;
            }

            prevChanges = changes;
        }

        // Set the cluster attribute for each vertex.
        for (int position = 0; position < vxCount; position++) {
            final int vxId = wg.getVertex(position);

            wg.setIntValue(clusterId, vxId, vxClusters[vxId]);
        }
    }

    /**
     * Return the number of the cluster containing vxId.
     *
     * @param vxId A graph vertex.
     *
     * @return The number of the cluster containing vxId.
     */
    public int getCluster(final int vxId) {
        return vxClusters[vxId];
    }

    /**
     * Shuffle the given integer array.
     *
     * @param random
     * @param a
     */
    private static void shuffle(final SecureRandom random, final int[] a) {
        for (int i = a.length - 1; i > 0; i--) {
            final int ix = random.nextInt(i + 1);
            final int tmp = a[ix];
            a[ix] = a[i];
            a[i] = tmp;
        }
    }
}
