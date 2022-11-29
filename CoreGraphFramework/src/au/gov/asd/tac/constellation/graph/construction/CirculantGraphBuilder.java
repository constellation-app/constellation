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
public class CirculantGraphBuilder extends GraphBuilder {

    private static final int SIZE_DEFAULT = 10;
    private static final int[] JUMPS_DEFAULT = {1, 2};
    private static final boolean DIRECTED_DEFAULT = false;

    public static CirculantGraphBuilder addCirculant(final GraphWriteMethods graph) {
        return addCirculant(graph, SIZE_DEFAULT, JUMPS_DEFAULT, DIRECTED_DEFAULT);
    }

    public static CirculantGraphBuilder addCirculant(final int size, final int[] jumps, final boolean directed) {
        return addCirculant(new StoreGraph(), size, jumps, directed);
    }

    public static CirculantGraphBuilder addCirculant(final GraphWriteMethods graph, final int size, final int[] jumps, final boolean directed) {
        final int[] nodes = new int[size];
        final int numJumps = jumps.length;
        final int[][] jumpTransactions = new int[numJumps][];

        // Add the nodes
        for (int i = 0; i < size; i++) {
            nodes[i] = constructVertex(graph);
        }

        // Add the jumpTransactions
        for (int i = 0; i < numJumps; i++) {
            final int jump = jumps[i];
            jumpTransactions[i] = new int[size];
            for (int j = 0; j < size; j++) {
                jumpTransactions[i][j] = constructTransaction(graph, nodes[i], nodes[(i + jump) % size], directed);
            }
        }

        return new CirculantGraphBuilder(graph, nodes, jumpTransactions);
    }

    public final int[] nodes;
    public final int[] transactions;
    public final int[][] jumpTransactions;

    private CirculantGraphBuilder(final GraphWriteMethods graph, final int[] nodes, final int[][] jumpTransactions) {
        super(graph);
        this.nodes = nodes;
        this.jumpTransactions = jumpTransactions;
        this.transactions = squashGrouping(jumpTransactions);
    }

}
