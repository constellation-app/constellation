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
package au.gov.asd.tac.constellation.graph.visual.plugins.hop;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import java.util.BitSet;

/**
 * provide hop out capability for the hop actions.
 *
 * @author algol
 */
final class HopUtilities {

    public static final int HOP_OUT_HALF = 0;
    public static final int HOP_OUT_ONE = 1;
    public static final int HOP_OUT_FULL = 2;

    private HopUtilities() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Do a half hop out.
     *
     * @param graph The graph.
     *
     * @return True if any hopping was done, false otherwise.
     */
    static boolean hopOutHalf(final GraphWriteMethods graph, boolean outgoing, boolean incoming, boolean undirected) {
        final int txSelectedAttr = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);
        final int vxSelectedAttr = VisualConcept.VertexAttribute.SELECTED.ensure(graph);

        final BitSet vxToSelect = new BitSet(graph.getVertexCapacity());
        final BitSet txToSelect = new BitSet(graph.getTransactionCapacity());

        // Process all transactions in the graph
        for (int position = 0; position < graph.getTransactionCount(); position++) {
            final int txId = graph.getTransaction(position);

            // If this transaction is selected, we (intend to) select the vertices at either end.
            // If this transaction is not selected, we (intend to) select it if either one of the vertices at the ends are selected.
            // Either way, we only (intend to) select something if it isn't already selected.
            // This allows us to keep selecting until there's nothing left that is unselected.
            final int txSourceId = graph.getTransactionSourceVertex(txId);
            final int txDestId = graph.getTransactionDestinationVertex(txId);
            final boolean txIsSelected = graph.getBooleanValue(txSelectedAttr, txId);
            final boolean txDirected = graph.getTransactionDirection(txId) != Graph.UNDIRECTED;
            if (txIsSelected) {
                final boolean srcsel = graph.getBooleanValue(vxSelectedAttr, txSourceId);
                final boolean dstsel = graph.getBooleanValue(vxSelectedAttr, txDestId);

                // If the transaction is directed:
                //      Select the source vertex if following incoming transactions
                //      Select the destination vertex if following outgoing transactions
                // If the transaction is undirected
                //      Select the source and destination vertex if following undirected transactions
                if (txDirected) {
                    if (incoming && !srcsel) {
                        vxToSelect.set(txSourceId);
                    }
                    if (outgoing && !dstsel) {
                        vxToSelect.set(txDestId);
                    }
                } else if (undirected) {
                    if (!srcsel) {
                        vxToSelect.set(txSourceId);
                    }
                    if (!dstsel) {
                        vxToSelect.set(txDestId);
                    }
                } else {
                    // Do nothing
                }
            } else {
                final boolean txSourceIsSelected = graph.getBooleanValue(vxSelectedAttr, txSourceId);
                final boolean txDestIsSelected = graph.getBooleanValue(vxSelectedAttr, txDestId);
                if (txDirected) {
                    if ((outgoing && txSourceIsSelected) || (incoming && txDestIsSelected)) {
                        txToSelect.set(txId);
                    }
                } else {
                    if (undirected && (txSourceIsSelected || txDestIsSelected)) {
                        txToSelect.set(txId);
                    }
                }
            }
        }

        for (int i = txToSelect.nextSetBit(0); i >= 0; i = txToSelect.nextSetBit(i + 1)) {
            graph.setBooleanValue(txSelectedAttr, i, true);
        }

        for (int i = vxToSelect.nextSetBit(0); i >= 0; i = vxToSelect.nextSetBit(i + 1)) {
            graph.setBooleanValue(vxSelectedAttr, i, true);
        }

        return !txToSelect.isEmpty() || !vxToSelect.isEmpty();
    }

    /**
     * Do a hop out (two half hops).
     *
     * @param graph
     */
    static void hopOutOne(final GraphWriteMethods graph, boolean outgoing, boolean incoming, boolean undirected) {
        hopOutHalf(graph, outgoing, incoming, undirected);
        hopOutHalf(graph, outgoing, incoming, undirected);
    }

    /**
     * Do a full hop out; this selects the entire component.
     *
     * @param graph
     */
    static void hopOutFull(final GraphWriteMethods graph, boolean outgoing, boolean incoming, boolean undirected) {
        while (hopOutHalf(graph, outgoing, incoming, undirected)) {
            // do nothing
        }
    }
}
