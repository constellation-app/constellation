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
package au.gov.asd.tac.constellation.plugins.arrangements.planar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author twilight_sparkle
 */
class PQTree {

    private PQNode root;
    private PQNode pertinentRoot;
    private int currentNumber;
    private final List<Set<PQNode>> leaves;
    private PQNode directionIndicatorLocation = null;
    private int numPertinentLeaves;

    public void addLeaves(final PQNode toNode, final Iterable<Integer> childNums) {
        for (final int i : childNums) {
            final PQNode leaf = new PQNode(NodeType.LEAF_NODE, i, currentNumber);
            toNode.addChild(leaf);
            leaves.get(i - 1).add(leaf);
        }
    }

    public PQTree(final int numberNodes) {
        currentNumber = 1;
        this.root = new PQNode(NodeType.PNODE);
        leaves = new ArrayList<>(Collections.nCopies(numberNodes, new HashSet<>()));
    }

    public PQNode getRoot() {
        return root;
    }

    public void setRoot(final PQNode root) {
        this.root = root;
    }

    private Deque<PQNode> bubble() {
        final Deque<PQNode> nodesToProcess = new LinkedList<>();
        final Deque<PQNode> nodesToBubble = new LinkedList<>();
        final Set<PQNode> bubbled = new HashSet<>();

        for (final PQNode leaf : leaves.get(currentNumber - 1)) {
            leaf.setPertinentLeafCount(1);
            if (leaf.getParent() != null) {
                leaf.getParent().setPertinentChildCount(leaf.getParent().getPertinentChildCount() + 1);
                nodesToBubble.addLast(leaf.getParent());
            }
            nodesToProcess.addLast(leaf);
        }
        numPertinentLeaves = nodesToProcess.size();
        boolean toBreak = false;
        boolean offTheTop = false;
        while (!nodesToBubble.isEmpty()) {
            final PQNode toBubble = nodesToBubble.removeFirst();
            if (nodesToBubble.isEmpty() && !offTheTop) {
                toBreak = true;
            }
            if (!bubbled.contains(toBubble)) {
                bubbled.add(toBubble);
                if (toBubble.getParent() != null) {
                    toBubble.getParent().setPertinentChildCount(toBubble.getParent().getPertinentChildCount() + 1);
                    nodesToBubble.addLast(toBubble.getParent());
                } else {
                    offTheTop = true;
                }
            }
            if (toBreak) {
                break;
            }
        }
        return nodesToProcess;
    }

    public void reduce() {
        currentNumber++;
        final Deque<PQNode> nodesToProcess = bubble();
        PQNode node = null;
        while (!nodesToProcess.isEmpty()) {
            node = nodesToProcess.removeFirst();
            if (node.getPertinentLeafCount() < numPertinentLeaves) {
                // node is not the root of the pertinent subtree
                node.getParent().setPertinentLeafCount(node.getParent().getPertinentLeafCount()
                        + node.getPertinentLeafCount());
                node.getParent().setPertinentChildCount(node.getParent().getPertinentChildCount() - 1);
                if (node.getParent().getPertinentChildCount() == 0) {
                    nodesToProcess.addLast(node.getParent());
                }
            } else {
                // As we are at the pertinent root we will not be processing anything above it and hence we need to reset the counts of its ancestors
                PQNode ancestor = node.getParent();
                while (ancestor != null) {
                    ancestor.setPertinentChildCount(0);
                    ancestor.setPertinentLeafCount(0);
                    ancestor = ancestor.getParent();
                }
            }
            // reset the now processed node's count ready for the next pass.
            node.setPertinentLeafCount(0);
        }
        pertinentRoot = node;
    }

    public void vertexAddition(final List<Integer> virtualNodeNums) {
        final PQNode nextPNode = new PQNode(NodeType.PNODE);
        if (pertinentRoot.getLabel().equals(NodeLabel.FULL)) {
            subtreeReplace(pertinentRoot, nextPNode);
        } else {
            addDirectionIndicator(pertinentRoot);
            boolean first = true;
            for (final PQNode node : pertinentRoot.getLabelView(NodeLabel.FULL)) {
                if (first) {
                    subtreeReplace(node, nextPNode);
                    first = false;
                } else {
                    pertinentRoot.removeChild(node);
                }
            }
            // There were no full nodes because graph is not planar and they were the most efficient to remove.
            // In this case we simply add the next PNode to the end of the pertinent root
            if (first) {
                pertinentRoot.addChild(nextPNode);
            }
        }
        addLeaves(nextPNode, virtualNodeNums);
    }

    public List<Integer> readPertinentFrontier() {
        // If the last processed node matched P4 or P6, then it is no longer the pertinent root,
        // instead its single partial child is the pertinent root and full children of the last matched
        // node have been moved to this child.
        if (!pertinentRoot.getLabel().equals(NodeLabel.FULL) && pertinentRoot.labeledChildren.get(NodeLabel.FULL).isEmpty()) {
            pertinentRoot = pertinentRoot.labeledChildren.get(NodeLabel.PARTIAL).iterator().next();
        }
        final List<Integer> pertinentFrontier = new LinkedList<>();
        readPertinentFrontier(pertinentRoot, pertinentFrontier);
        return pertinentFrontier;
    }

    private void addDirectionIndicator(final PQNode toNode) {
        final DirectionIndicator indicator = new DirectionIndicator(currentNumber, false);
        toNode.setDirectionIndicator(indicator);
        directionIndicatorLocation = toNode;
    }

    // reads the current direction indicator if any.
    // Returns -1 if there is no direction indicator or the indicator has the same direction as the frontier.
    // Returns the number of the adjacency list to be reversed if the direction indicator is in the opposite direction to the frontier.
    public int readAndRemoveDirectionIndicator() {
        int listToReverse = -1;
        if (directionIndicatorLocation != null) {
            final DirectionIndicator indicator = directionIndicatorLocation.getDirectionIndicator();
            directionIndicatorLocation.setDirectionIndicator(null);
            if (indicator.isReversed()) {
                listToReverse = indicator.getNumber();
            }
        }
        directionIndicatorLocation = null;
        return listToReverse;
    }

    private void readPertinentFrontier(final PQNode node, final List<Integer> frontier) {
        switch (node.type) {
            case PNODE -> {
                for (final PQNode child : node.labeledChildren.get(NodeLabel.FULL)) {
                    readPertinentFrontier(child, frontier);
                }
            }
            case QNODE -> {
                for (final PQNode child : node.children) {
                    if (child.getLabel().equals(NodeLabel.FULL)) {
                        readPertinentFrontier(child, frontier);
                    }
                }
            }
            case LEAF_NODE -> {
                if (node.getVirtualNum() == currentNumber) {
                    frontier.add(node.getRealNum());
                }
            }
            default -> {
                // Do nothing 
            }
        }
    }

    // Replace the subtree rooted at existing by the subtree rooted at replacement in the current tree.
    private void subtreeReplace(final PQNode existing, final PQNode replacement) {
        if (existing == root) {
            root = replacement;
            return;
        }
        final PQNode existingParent = existing.getParent();
        existingParent.replaceChild(existing, replacement);
    }
}
