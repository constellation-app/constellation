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

/**
 *
 * @author twilight_sparkle
 */
public class TreeGraphBuilder extends GraphBuilder {

    private static final int[] CHILDREN_AT_DEPTHS_DEFAULT = {2, 2, 2};
    private static final TreeDirection DIRECTION_DEFAULT = TreeDirection.UNDIRECTED;

    public enum TreeDirection {

        AWAY_FROM_ROOT,
        TOWARDS_ROOT,
        UNDIRECTED;
    }

    public static TreeGraphBuilder addTree(final GraphWriteMethods graph) {
        return addTree(graph, CHILDREN_AT_DEPTHS_DEFAULT, DIRECTION_DEFAULT);
    }

    public static TreeGraphBuilder addTree(final int childrenPerNode, final int numLevels, final TreeDirection direction) {
        return addTree(new StoreGraph(), childrenPerNode, numLevels, direction);
    }

    public static TreeGraphBuilder addTree(final GraphWriteMethods graph, final int childrenPerNode, final int numLevels, final TreeDirection direction) {
        final int[] childrenPerNodeAtDepths = new int[numLevels];
        for (int i = 0; i < numLevels; i++) {
            childrenPerNodeAtDepths[i] = childrenPerNode;
        }
        return addTree(graph, childrenPerNodeAtDepths, direction);
    }

    public static TreeGraphBuilder addTree(final int[] childrenPerNodeAtDepths, final TreeDirection direction) {
        return addTree(new StoreGraph(), childrenPerNodeAtDepths, direction);
    }

    public static TreeGraphBuilder addTree(final GraphWriteMethods graph, final int[] childrenPerNodeAtDepths, final TreeDirection direction) {
        final int depth = childrenPerNodeAtDepths.length + 1;
        final int[][] nodesAtLevels = new int[depth][];
        final int[][] transactionsAtLevels = new int[depth - 1][];

        final boolean directed = direction != TreeDirection.UNDIRECTED;
        final boolean reverse = direction == TreeDirection.TOWARDS_ROOT;
        final int root = constructVertex(graph);
        nodesAtLevels[0] = new int[1];
        nodesAtLevels[0][0] = root;
        for (int i = 1; i < depth; i++) {
            final int nodesAtLevel = nodesAtLevels[i - 1].length * childrenPerNodeAtDepths[i - 1];
            nodesAtLevels[i] = new int[nodesAtLevel];
            transactionsAtLevels[i - 1] = new int[nodesAtLevel];
            int currentNode = 0;
            for (final int node : nodesAtLevels[i - 1]) {
                for (int j = 0; j < childrenPerNodeAtDepths[i - 1]; j++) {
                    nodesAtLevels[i][currentNode] = constructVertex(graph);
                    transactionsAtLevels[i - 1][currentNode] = constructTransaction(graph, node, nodesAtLevels[i][currentNode], directed, reverse);
                    currentNode += 1;
                }
            }
        }

        return new TreeGraphBuilder(graph, nodesAtLevels, transactionsAtLevels);
    }

    public final int[] nodes;
    public final int[] transactions;
    public final int[][] nodesAtLevels;
    public final int[][] transactionsAtLevels;

    private TreeGraphBuilder(final GraphWriteMethods graph, final int[][] nodesAtLevels, final int[][] transactionsAtLevels) {
        super(graph);
        this.nodesAtLevels = nodesAtLevels;
        nodes = squashGrouping(nodesAtLevels);
        this.transactionsAtLevels = transactionsAtLevels;
        transactions = squashGrouping(transactionsAtLevels);
    }
}
