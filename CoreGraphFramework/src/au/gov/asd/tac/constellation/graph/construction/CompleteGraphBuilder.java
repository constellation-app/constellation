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
package au.gov.asd.tac.constellation.graph.construction;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.StoreGraph;

/**
 *
 * @author twilight_sparkle
 */
public class CompleteGraphBuilder extends GraphBuilder {

    private static final int SIZE_DEFAULT = 5;
    private static final boolean DIRECTED_DEFAULT = false;

    public static CompleteGraphBuilder addCompleteGraph(final GraphWriteMethods graph) {
        return addCompleteGraph(graph, SIZE_DEFAULT, DIRECTED_DEFAULT);
    }

    public static CompleteGraphBuilder addCompleteGraph(final int size, final boolean directed) {
        return addCompleteGraph(new StoreGraph(), size, directed);
    }

    public static CompleteGraphBuilder addCompleteGraph(final GraphWriteMethods graph, final int size, final boolean directed) {
        final int[] nodes = new int[size];
        final int[] transactions = new int[size * (size - 1) / 2];
        int currentTransaction = 0;
        for (int i = 0; i < size; i++) {
            nodes[i] = constructVertex(graph);
            for (int j = i - 1; j >= 0; j--) {
                transactions[currentTransaction++] = constructTransaction(graph, nodes[j], nodes[i], directed);
            }
        }
        final int source = nodes[0];
        final int sink = nodes[size - 1];

        return new CompleteGraphBuilder(graph, source, sink, nodes, transactions);
    }

    public final int source;
    public final int sink;
    public final int[] nodes;
    public final int[] transactions;

    private CompleteGraphBuilder(final GraphWriteMethods graph, final int source, final int sink, final int[] nodes, final int[] transactions) {
        super(graph);
        this.source = source;
        this.sink = sink;
        this.nodes = nodes;
        this.transactions = transactions;
    }

}
