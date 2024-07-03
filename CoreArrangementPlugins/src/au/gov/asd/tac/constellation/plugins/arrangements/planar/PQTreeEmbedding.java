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
package au.gov.asd.tac.constellation.plugins.arrangements.planar;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.plugins.arrangements.planar.STNumbering.TreeNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author twilight_sparkle
 */
public class PQTreeEmbedding {

    private final GraphReadMethods graph;
    private final List<TreeNode> numberedComponent;
    private final int n;

    public final List<List<Integer>> adjacencyLists;
    private final PQTree tree;

    public PQTreeEmbedding(final GraphReadMethods graph, final List<TreeNode> stNumberedBiconnectedComponent) {
        this.graph = graph;
        numberedComponent = stNumberedBiconnectedComponent;
        n = numberedComponent.size();
        this.adjacencyLists = new ArrayList<>();
        generateInitialAdjacencyLists();
        tree = new PQTree(n);
    }

    private void generateInitialAdjacencyLists() {
        final Map<Integer, Integer> componentVxIDsToStNumbers = new HashMap<>();
        int i = 1;
        for (final TreeNode node : numberedComponent) {
            componentVxIDsToStNumbers.put(node.vxID, i++);
        }
        i = 1;
        for (final TreeNode node : numberedComponent) {
            final List<Integer> adjacencyList = new LinkedList<>();
            for (int j = 0; j < graph.getVertexNeighbourCount(node.vxID); j++) {
                final int neighbourID = graph.getVertexNeighbour(node.vxID, j);
                if (componentVxIDsToStNumbers.containsKey(neighbourID)) {
                    final int neighbour = componentVxIDsToStNumbers.get(neighbourID);
                    if (neighbour > i) {
                        adjacencyList.add(neighbour);
                    }
                }
            }
            adjacencyLists.add(adjacencyList);
            i++;
        }
    }

    public void embedComponent() {
        // Add leaf nodes for the root of the tree
        tree.addLeaves(tree.getRoot(), adjacencyLists.get(0));
        adjacencyLists.set(0, new LinkedList<>());

        // create the upward embedding
        for (int i = 2; i <= n; i++) {
            final List<Integer> virtaulNodes = adjacencyLists.get(i - 1);
            tree.reduce();
            adjacencyLists.set(i - 1, tree.readPertinentFrontier());
            final int listToReverse = tree.readAndRemoveDirectionIndicator();
            if (listToReverse != -1) {
                final List<Integer> toReverse = adjacencyLists.get(listToReverse - 1);
                final List<Integer> reversed = new LinkedList<>();
                for (final int neighbour : toReverse) {
                    reversed.add(0, neighbour);
                }
                adjacencyLists.set(listToReverse - 1, reversed);
            }
            tree.vertexAddition(virtaulNodes);
        }

        // convert the upward embedding into an entire embedding.
        final Set<Integer> oldNumbers = new HashSet<>();
        entireEmbed(n, oldNumbers);

    }

    public void entireEmbed(final int number, final Set<Integer> oldNumbers) {
        final List<Integer> neighbours = new LinkedList<>(adjacencyLists.get(number - 1));
        oldNumbers.add(number);
        for (final int neighbour : neighbours) {
            if (neighbour < number) {
                adjacencyLists.get(neighbour - 1).add(0, number);
            }
            if (!oldNumbers.contains(neighbour)) {
                entireEmbed(neighbour, oldNumbers);
            }
        }
    }
}
