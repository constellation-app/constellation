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
package au.gov.asd.tac.constellation.graph.construction;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author twilight_sparkle
 */
public class AddGraphBuilder extends GraphBuilder {

    public static AddGraphBuilder addGraph(final GraphReadMethods toAdd) {
        return addGraph(new StoreGraph(), toAdd);
    }

    public static AddGraphBuilder addGraph(final GraphWriteMethods graph, final GraphReadMethods toAdd) {
        // Copy the nodes from toAdd into graph, recording a mapping between IDs
        final int[] addedVerts = new int[toAdd.getVertexCount()];
        final Map<Integer, Integer> addedOldToNewIDs = new HashMap<>();
        for (int i = 0; i < toAdd.getVertexCount(); i++) {
            addedVerts[i] = constructVertex(graph);
            addedOldToNewIDs.put(toAdd.getVertex(i), addedVerts[i]);
        }

        // Copy the transactions from toAdd into graph.
        final int[] addedTransactions = new int[toAdd.getTransactionCount()];
        for (int i = 0; i < toAdd.getTransactionCount(); i++) {
            final int source = toAdd.getTransactionSourceVertex(toAdd.getTransaction(i));
            final int dest = toAdd.getTransactionDestinationVertex(toAdd.getTransaction(i));
            final boolean directed = toAdd.getTransactionDirection(toAdd.getTransaction(i)) != Graph.FLAT;
            addedTransactions[i] = constructTransaction(graph, addedOldToNewIDs.get(source), addedOldToNewIDs.get(dest), directed);
        }

        // Return the AddGraphBuilder associated with the addition to graph.
        return new AddGraphBuilder(graph, addedVerts, addedTransactions, addedOldToNewIDs);

    }

    public final int[] nodes;
    public final int[] transactions;
    public final Map<Integer, Integer> addedOldToNewIDs;

    public int[] getSubsetOfAddedVerts(final int[] originalIDs) {
        final int[] newIDs = new int[originalIDs.length];
        for (int i = 0; i < newIDs.length; i++) {
            newIDs[i] = addedOldToNewIDs.get(originalIDs[i]);
        }
        return newIDs;
    }

    private AddGraphBuilder(final GraphWriteMethods graph, final int[] addedVerts, final int[] addedTransactions, final Map<Integer, Integer> addedOldToNewIDs) {
        super(graph);
        this.nodes = addedVerts;
        this.transactions = addedTransactions;
        this.addedOldToNewIDs = addedOldToNewIDs;
    }
}
