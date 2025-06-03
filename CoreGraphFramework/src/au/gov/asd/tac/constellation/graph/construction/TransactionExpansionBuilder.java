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

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author twilight_sparkle
 */
public class TransactionExpansionBuilder extends GraphBuilder {

    public static TransactionExpansionBuilder expandTransactionsInGraph(final GraphWriteMethods graph, 
            final GraphReadMethods expansionGraph, final int sourceVertexToIdentify, 
            final int destinationVertexToIdentify, final int[] transactionsToExpand) {
        final int[][] expandedVertices = new int[transactionsToExpand.length][];
        final int[][] expandedTransactions = new int[transactionsToExpand.length][];

        for (int i = 0; i < transactionsToExpand.length; i++) {
            final Map<Integer, Integer> identificationMap = new HashMap<>();
            identificationMap.put(sourceVertexToIdentify, graph.getTransactionSourceVertex(transactionsToExpand[i]));
            identificationMap.put(destinationVertexToIdentify, graph.getTransactionDestinationVertex(transactionsToExpand[i]));
            final UnionBuilder u = UnionBuilder.unionGraph(graph, expansionGraph, identificationMap);
            expandedVertices[i] = u.addedVerts;
            expandedTransactions[i] = u.allNewTransactions;
        }

        // Return the VertexExpansionBuilder associated with this expansion of graph.
        return new TransactionExpansionBuilder(graph, expandedVertices, expandedTransactions);

    }

    public final int[] nodes;
    public final int[] transactions;
    public final int[][] expandedVertices;
    public final int[][] expandedTransactions;

    private TransactionExpansionBuilder(final GraphWriteMethods graph, final int[][] expandedVertices, final int[][] expandedTransactions) {
        super(graph);
        this.expandedVertices = expandedVertices;
        nodes = squashGrouping(expandedVertices);
        this.expandedTransactions = expandedTransactions;
        transactions = squashGrouping(expandedTransactions);
    }
}
