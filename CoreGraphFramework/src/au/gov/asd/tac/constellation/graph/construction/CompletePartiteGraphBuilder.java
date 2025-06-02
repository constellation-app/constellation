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

/**
 *
 * @author twilight_sparkle
 */
public class CompletePartiteGraphBuilder extends GraphBuilder {

    private static final int[] PARTITION_SIZES_DEFAULT = {3, 3, 3};
    private static final boolean DIRECTED_DEFAULT = false;

    public static CompletePartiteGraphBuilder addCompletePartiteGraph(final GraphWriteMethods graph) {
        return addCompletePartiteGraph(graph, PARTITION_SIZES_DEFAULT, DIRECTED_DEFAULT);
    }

    public static CompletePartiteGraphBuilder addCompletePartiteGraph(final GraphWriteMethods graph, final int[] partitionSizes, final boolean directed) {
        final int numPartitions = partitionSizes.length;

        final int[] noJumps = new int[0];
        final int[][] partitionVertices = new int[numPartitions][];
        final int[][][] partitionTransactions = new int[numPartitions][numPartitions][];

        for (int i = 0; i < numPartitions; i++) {
            final int size = partitionSizes[i];

            // Construct each partition (circulant graph with no jumps, ie. a graph of singletons)
            final CirculantGraphBuilder partition = CirculantGraphBuilder.addCirculant(size, noJumps, directed);

            // Add each partition to the graph
            final AddGraphBuilder union = AddGraphBuilder.addGraph(graph, partition.graph);

            // Store the indices of the nodes in each partition as they are in graph.
            partitionVertices[i] = union.nodes;
        }

        // For each pair of partitions form and record the transactions between them
        for (int i = 0; i < numPartitions; i++) {
            for (int j = i + 1; j < numPartitions; j++) {
                final ConnectionBuilder connector = ConnectionBuilder.makeConnection(graph, partitionVertices[i], partitionVertices[j], directed ? ConnectionBuilder.ConnectionDirection.LEFT_TO_RIGHT : ConnectionBuilder.ConnectionDirection.UNDIRECTED);
                partitionTransactions[i][j] = connector.connectingTransactions;
            }
        }

        return new CompletePartiteGraphBuilder(graph, partitionVertices, partitionTransactions);
    }

    public final int[] nodes;
    public final int[] transactions;
    public final int[][] partitionVertices;
    public final int[][][] partitionTransactions;

    private CompletePartiteGraphBuilder(final GraphWriteMethods graph, final int[][] partitionVertices, final int[][][] partitionTransactions) {
        super(graph);
        this.partitionVertices = partitionVertices;
        nodes = squashGrouping(partitionVertices);
        this.partitionTransactions = partitionTransactions;
        transactions = squashGrouping(partitionTransactions);
    }

}
