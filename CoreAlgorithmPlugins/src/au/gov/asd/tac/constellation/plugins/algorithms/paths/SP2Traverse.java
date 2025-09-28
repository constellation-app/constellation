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
package au.gov.asd.tac.constellation.plugins.algorithms.paths;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import java.util.BitSet;

/**
 *
 * @author canis_majoris
 */
public class SP2Traverse {

    /**
     * This method uses parallel-BFS to traverse the graph and gather the
     * eccentricities of each node, ignoring direction.
     *
     * @param graph
     * @return
     */
    protected static void traverse(final GraphWriteMethods graph) {

        /*
         Initialising variables
         */
        final int vxCount = graph.getVertexCount();
        final int selectedNodeAttrId = VisualConcept.VertexAttribute.SELECTED.get(graph);
        final int selectedTransactionAttrId = VisualConcept.TransactionAttribute.SELECTED.get(graph);

        final BitSet update = new BitSet(vxCount);
        final BitSet[] receivedFrom = new BitSet[vxCount]; // this keeps track of what information a node has received
        final BitSet[] sendBuffer = new BitSet[vxCount];   // each turn, this stores all the information a node is receiving
        final int[] ecc = new int[vxCount];
        final BitSet turn = new BitSet(vxCount);
        final BitSet newUpdate = new BitSet(vxCount);
        final BitSet goal = new BitSet(vxCount);
        final BitSet[] pathed = new BitSet[vxCount];

        final BitSet[] connectedTo = FindSubgraphs.traverse(graph);

        final BitSet check = new BitSet(vxCount); // used if a seed only has one neighbour, skipping that hop
        final BitSet seeds = new BitSet(vxCount); // used to store all the seeds

        if (selectedNodeAttrId == Graph.NOT_FOUND || selectedTransactionAttrId == Graph.NOT_FOUND) {
            return;
        }
        boolean beginning = true;

        // this block attempt to find the seeds
        for (int vxId = 0; vxId < vxCount; vxId++) {
            // get the vertex ID at this position
            final int position = graph.getVertex(vxId);

            receivedFrom[vxId] = new BitSet(vxCount);
            sendBuffer[vxId] = new BitSet(vxCount);
            pathed[vxId] = new BitSet(vxCount);
            ecc[vxId] = -1;

            if (graph.getBooleanValue(selectedNodeAttrId, position) && graph.getVertexNeighbourCount(position) == 1) {
                // dealing with special case for nodes with one neighbour
                seeds.set(graph.getVertexNeighbour(position, 0), true);
                check.set(graph.getVertexPosition(graph.getVertexNeighbour(position, 0)), true);
            } else if (graph.getBooleanValue(selectedNodeAttrId, position) && graph.getVertexNeighbourCount(position) > 1) {
                seeds.set(vxId, true);
            } else {
                // Ignore nodes with no neighbour
            }
        }
        // now to determine how many pairs of nodes we need to get the shortest path between
        int pairsize = 0;
        boolean specialcase = false;
        if (seeds.cardinality() < 1) {
            return;
        } else if (seeds.cardinality() == 1) {
            specialcase = true;
            pairsize = 1 + check.cardinality();
        } else if (seeds.cardinality() == 2) {
            pairsize = 1 + check.cardinality();
        } else {
            pairsize = (((seeds.cardinality() - 1) * (seeds.cardinality())) / 2) + check.cardinality();
        }

        int pairCounter = 0;
        // for each pair

        for (int one = seeds.nextSetBit(0); one >= 0; one = seeds.nextSetBit(one + 1)) {
            int start = one;
            if (specialcase) {
                start = 0;
            }
            for (int two = seeds.nextSetBit(start); two >= 0; two = seeds.nextSetBit(two + 1)) {
                if (!specialcase && one > two || (one == two && !check.get(one))) {
                    continue;
                }

                if (!connectedTo[one].get(two)) {
                    pairCounter++;
                    if (pairCounter == pairsize) {
                        break;
                    }
                    continue;
                }

                // initialise variables for this shortest path pair
                goal.clear();
                update.clear();
                turn.clear();

                if (!beginning) {
                    for (int vxId = 0; vxId < vxCount; vxId++) {
                        receivedFrom[vxId].clear();
                        sendBuffer[vxId].clear();
                        ecc[vxId] = -1;
                    }
                } else {
                    beginning = false;
                }
                update.set(one, true);

                // now we start the algorithm
                while (true) {
                    newUpdate.clear();

                    // we aren't progressing any further
                    if (update.isEmpty()) {
                        break;
                    }
                    // check if we have arrived at the destination, otherwise, just update everything's information
                    boolean success = false;
                    boolean tempcess = true;
                    for (int vxId = update.nextSetBit(0); vxId >= 0; vxId = update.nextSetBit(vxId + 1)) {
                        if (vxId == two) {
                            goal.set(vxId, true);
                            success = true;
                            tempcess = false;
                        }
                        if (tempcess && !pathed[vxId].get(two)) {
                            tempcess = false;
                        }
                        receivedFrom[vxId].or(sendBuffer[vxId]);
                        receivedFrom[vxId].set(vxId, true);
                        sendBuffer[vxId].clear();
                    }
                    if (tempcess) {
                        goal.or(update);
                        success = true;
                    }

                    // for everything in transit, update the eccentricity
                    for (int vxId = turn.nextSetBit(0); vxId >= 0; vxId = turn.nextSetBit(vxId + 1)) {
                        ecc[vxId]++;
                    }
                    turn.clear();

                    // if we have arrived at our destination, select everything on the path
                    if (success) {
                        for (int g = goal.nextSetBit(0); g >= 0; g = goal.nextSetBit(g + 1)) {
                            for (int src = receivedFrom[g].nextSetBit(0); src >= 0; src = receivedFrom[g].nextSetBit(src + 1)) {
                                final int srcId = graph.getVertex(src);
                                pathed[src].set(two, true);
                                boolean removeFromCheck = false;
                                for (int neighbourPosition = 0; neighbourPosition < graph.getVertexNeighbourCount(srcId); neighbourPosition++) {
                                    final int dstId = graph.getVertexNeighbour(srcId, neighbourPosition);
                                    final int dst = graph.getVertexPosition(dstId);
                                    final int lxId = graph.getLink(srcId, dstId);
                                    if (check.get(src) && (graph.getBooleanValue(selectedNodeAttrId, dstId) && graph.getVertexNeighbourCount(dstId) == 1)) {
                                        graph.setBooleanValue(selectedNodeAttrId, srcId, true);
                                        graph.setBooleanValue(selectedNodeAttrId, dstId, true);
                                        if (!graph.getBooleanValue(selectedTransactionAttrId, graph.getLinkTransaction(lxId, 0))) {
                                            for (int t = 0; t < graph.getLinkTransactionCount(lxId); t++) {
                                                final int txId = graph.getLinkTransaction(lxId, t);
                                                graph.setBooleanValue(selectedTransactionAttrId, txId, true);
                                            }
                                        }
                                        removeFromCheck = true;
                                    } else {
                                        if (receivedFrom[two].get(dst) && (ecc[src] == ecc[dst] - 1 || ecc[src] == ecc[dst] + 1)) {
                                            graph.setBooleanValue(selectedNodeAttrId, srcId, true);
                                            graph.setBooleanValue(selectedNodeAttrId, dstId, true);
                                            if (!graph.getBooleanValue(selectedTransactionAttrId, graph.getLinkTransaction(lxId, 0))) {
                                                for (int t = 0; t < graph.getLinkTransactionCount(lxId); t++) {
                                                    final int txId = graph.getLinkTransaction(lxId, t);
                                                    graph.setBooleanValue(selectedTransactionAttrId, txId, true);
                                                }
                                            }
                                        }
                                    }
                                }
                                if (removeFromCheck) {
                                    check.set(src, false);
                                }
                            }
                        }
                        break;
                    }

                    // for each neighbour, check if there is any new information it needs to receive
                    for (int src = update.nextSetBit(0); src >= 0; src = update.nextSetBit(src + 1)) {
                        final int vxId = graph.getVertex(src);
                        for (int neighbourPosition = 0; neighbourPosition < graph.getVertexNeighbourCount(vxId); neighbourPosition++) {
                            final int dst = graph.getVertexPosition(graph.getVertexNeighbour(vxId, neighbourPosition));
                            if (!receivedFrom[src].equals(receivedFrom[dst])) {
                                sendBuffer[dst].or(diff(receivedFrom[src], receivedFrom[dst]));
                                turn.or(sendBuffer[dst]);
                                newUpdate.set(dst, true);
                            }
                        }
                    }
                    update.clear();
                    update.or(newUpdate);
                }

                pairCounter++;
                if (pairCounter == pairsize) {
                    break;
                }
            }
            if (pairCounter == pairsize) {
                break;
            }
        }
    }

    /**
     * Performs andNot without overwriting on two BitSets
     *
     * @param one bitset one
     * @param two bitset two
     * @return
     */
    private static BitSet diff(final BitSet one, final BitSet two) {
        final BitSet o = new BitSet(one.length());
        final BitSet t = new BitSet(two.length());
        o.or(one);
        t.or(two);
        o.andNot(t);
        return o;
    }
}
