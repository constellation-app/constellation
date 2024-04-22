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
package au.gov.asd.tac.constellation.graph.construction;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author twilight_sparkle
 */
public class UnionBuilder extends GraphBuilder {

    public static UnionBuilder unionGraph(final GraphWriteMethods graph, final GraphReadMethods toUnion, final Map<Integer, Integer> identificationMapping) {
        // Add toUnion to graph
        final AddGraphBuilder a = AddGraphBuilder.addGraph(graph, toUnion);

        // Remap vertex IDs in the identificaiton mapping for the union
        final Map<Integer, Integer> addedOldToNewIDs = a.addedOldToNewIDs;
        final Map<Integer, Integer> newIDVertexMapping = new HashMap<>();
        for (final Entry<Integer, Integer> entry : identificationMapping.entrySet()) {
            newIDVertexMapping.put(addedOldToNewIDs.get(entry.getKey()), entry.getValue());
        }

        // Add new transactions for vertex identification and record mapping from old to new transactions
        final Map<Integer, Integer> newIDTransactionMapping = new HashMap<>();
        for (int i = 0; i < a.transactions.length; i++) {
            final int trans = graph.getTransaction(a.transactions[i]);
            final int source = graph.getTransactionSourceVertex(trans);
            final int dest = graph.getTransactionDestinationVertex(trans);
            final boolean directed = graph.getTransactionDirection(trans) == Graph.FLAT;
            if (newIDVertexMapping.containsKey(source)) {
                if (newIDVertexMapping.containsKey(dest)) {
                    final int newTrans = constructTransaction(graph, newIDVertexMapping.get(source), newIDVertexMapping.get(dest), directed);
                    newIDTransactionMapping.put(trans, newTrans);
                } else {
                    final int newTrans = constructTransaction(graph, newIDVertexMapping.get(source), dest, directed);
                    newIDTransactionMapping.put(trans, newTrans);
                }
            } else if (newIDVertexMapping.containsKey(dest)) {
                final int newTrans = constructTransaction(graph, source, newIDVertexMapping.get(dest), directed);
                newIDTransactionMapping.put(trans, newTrans);
            }
        }

        // Record the newly added nodes, remove the copies of the identified nodes
        final int[] addedVerts = new int[a.nodes.length - newIDVertexMapping.size()];
        int currentPos = 0;
        for (int i = 0; i < a.nodes.length; i++) {
            if (!newIDVertexMapping.containsKey(a.nodes[i])) {
                addedVerts[currentPos++] = a.nodes[i];
            } else {
                graph.removeVertex(a.nodes[i]);
            }
        }

        // Record the newly added transactions; those from toUnion itself and those from identification.
        final int[] addedTransactions = new int[a.transactions.length - newIDTransactionMapping.size()];
        final int[] unionedTransactions = new int[newIDTransactionMapping.size()];
        final int[] allNewTransactions = new int[a.transactions.length];
        int currentAddedPos = 0;
        int currentUnionedPos = 0;
        for (int i = 0; i < a.transactions.length; i++) {
            if (newIDTransactionMapping.containsKey(a.transactions[i])) {
                unionedTransactions[currentUnionedPos++] = newIDTransactionMapping.get(a.transactions[i]);
                allNewTransactions[i] = newIDTransactionMapping.get(a.transactions[i]);
            } else {
                addedTransactions[currentAddedPos++] = a.transactions[i];
                allNewTransactions[i] = a.transactions[i];
            }
        }

        // Return the UnionBuilder associated with the union to this graph.
        return new UnionBuilder(graph, addedVerts, addedTransactions, unionedTransactions, allNewTransactions);

    }

    public final int[] addedVerts;
    public final int[] addedTransactions;
    public final int[] unionedTransactions;
    public final int[] allNewTransactions;

    private UnionBuilder(final GraphWriteMethods graph, final int[] addedVerts, final int[] addedTransactions, 
            final int[] unionedTransactions, final int[] allNewTransactions) {
        super(graph);
        this.addedVerts = addedVerts;
        this.addedTransactions = addedTransactions;
        this.unionedTransactions = unionedTransactions;
        this.allNewTransactions = allNewTransactions;
    }
}
