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

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;

/**
 *
 * @author twilight_sparkle
 */
public class AdjacencyMatrixBuilder extends GraphBuilder {

    private static final boolean DIRECTED_DEFAULT = false;

    public static AdjacencyMatrixBuilder addGraphWithMatrix(final GraphWriteMethods graph, final int[][] adjacencyMatrix) {
        return addGraphWithMatrix(graph, adjacencyMatrix, DIRECTED_DEFAULT);
    }

    public static AdjacencyMatrixBuilder addGraphWithMatrix(final int[][] adjacencyMatrix, final boolean directed) {
        return addGraphWithMatrix(new StoreGraph(), adjacencyMatrix, directed);
    }

    public static AdjacencyMatrixBuilder addGraphWithMatrix(final GraphWriteMethods graph, final int[][] adjacencyMatrix, final boolean directed) {
        final int size = adjacencyMatrix.length;
        final int[] nodes = new int[size];

        for (int i = 0; i < size; i++) {
            nodes[i] = constructVertex(graph);
        }

        final MutableIntList transactionList = new IntArrayList();
        for (int i = 0; i < size; i++) {
            final int innerBound = directed ? 0 : i;
            for (int j = innerBound; j < size; j++) {
                for (int k = 0; k < adjacencyMatrix[i][j]; k++) {
                    transactionList.add(constructTransaction(graph, i, j, directed));
                }
            }
        }

        return new AdjacencyMatrixBuilder(graph, nodes, transactionList.toArray());
    }

    public final int[] nodes;
    public final int[] transactions;

    private AdjacencyMatrixBuilder(final GraphWriteMethods graph, final int[] nodes, final int[] transactions) {
        super(graph);
        this.nodes = nodes;
        this.transactions = transactions;
    }
}
