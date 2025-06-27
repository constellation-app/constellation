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

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import java.security.SecureRandom;
import java.util.BitSet;

/**
 *
 * @author canis_majoris
 */
public class FindSubgraphs {

    private static final SecureRandom RAND = new SecureRandom();

    /**
     * This method uses parallel-BFS to traverse the graph and gather the
     * eccentricities of each node Currently, direction is ignored
     *
     * @param graph
     * @return
     */
    protected static BitSet[] traverse(final GraphWriteMethods graph) {
        final int vxCount = graph.getVertexCount();
        final BitSet update = new BitSet(vxCount);
        final BitSet[] receivedFrom = new BitSet[vxCount];
        final BitSet[] sendBuffer = new BitSet[vxCount];
        final BitSet newUpdate = new BitSet(vxCount);

        final int kThreshold = 10000;
        final int k = 64;

        // this is utilising something called k-BFS, which is meant to be faster than parallel-bfs
        // start from k-random seeds rather than the whole graph (k is set to 100 here)
        final BitSet seeds = new BitSet(vxCount);
        if (vxCount > kThreshold) {
            for (int i = 0; i < k; i++) {
                if (graph.getVertexNeighbourCount(graph.getVertex(i)) > 0) {
                    seeds.set(RAND.nextInt(vxCount), true);
                }
            }
        }

        // initialising variables
        for (int vxId = 0; vxId < vxCount; vxId++) {
            // get the vertex ID at this position
            final int position = graph.getVertex(vxId);

            receivedFrom[vxId] = new BitSet(vxCount);
            sendBuffer[vxId] = new BitSet(vxCount);

            // set up an initial message for this node if it is a seed
            if (vxCount > kThreshold && !seeds.get(vxId)) {
                continue;
            }
            // assuming the node has neighbours
            if (graph.getVertexNeighbourCount(position) > 0) {
                update.set(vxId, true);
            }
        }

        while (true) {
            newUpdate.clear();

            if (update.isEmpty()) {
                break;
            }
            // each node with messages needs to update its own information
            for (int vxId = update.nextSetBit(0); vxId >= 0; vxId = update.nextSetBit(vxId + 1)) {
                receivedFrom[vxId].or(sendBuffer[vxId]);
                receivedFrom[vxId].set(vxId, true);
                sendBuffer[vxId].clear();
            }

            // for each neighbour, check if there is any new information it needs to receive
            for (int src = update.nextSetBit(0); src >= 0; src = update.nextSetBit(src + 1)) {
                final int vxId = graph.getVertex(src);
                for (int neighbourPosition = 0; neighbourPosition < graph.getVertexNeighbourCount(vxId); neighbourPosition++) {
                    final int dst = graph.getVertexPosition(graph.getVertexNeighbour(vxId, neighbourPosition));
                    if (!receivedFrom[src].equals(receivedFrom[dst])) {
                        sendBuffer[dst].or(diff(receivedFrom[src], receivedFrom[dst]));
                        newUpdate.set(dst, true);
                    }
                }
            }
            update.clear();
            update.or(newUpdate);
        }

        return receivedFrom;
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
