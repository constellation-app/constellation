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
package au.gov.asd.tac.constellation.plugins.arrangements.planar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
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
    final private List<Set<PQNode>> leaves;
    private PQNode directionIndicatorLocation = null;
    private int numPertinentLeaves;

    public void addLeaves(final PQNode toNode, final List<Integer> childNums) {
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
//            if (leaf.virtualNum == currentNumber) {
            leaf.setPertinentLeafCount(1);
            if (leaf.getParent() != null) {
                leaf.getParent().setPertinentChildCount(leaf.getParent().getPertinentChildCount() + 1);
                nodesToBubble.addLast(leaf.getParent());
            }
            nodesToProcess.addLast(leaf);
//            }
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

    public void vertexAddition(List<Integer> virtualNodeNums) {
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
            case PNODE:
                for (final PQNode child : node.labeledChildren.get(NodeLabel.FULL)) {
                    readPertinentFrontier(child, frontier);
                }
                break;
            case QNODE:
                for (final PQNode child : node.children) {
                    if (child.getLabel().equals(NodeLabel.FULL)) {
                        readPertinentFrontier(child, frontier);
                    }
                }
                break;
            case LEAF_NODE:
                if (node.getVirtualNum() == currentNumber) {
                    frontier.add(node.getRealNum());
                }
                break;
            default:
                break;
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

    // Removes all PQNodes with the given label from node's list of children.
    // If there are no children with this label, nothing happens and null is returned.
    // If there is only one child with this label, it is returned.
    // If there is more than one child with this label, a new P-Node with the same label is created and is made the parent of all the removed nodes. This new P-node is returned.
    private PQNode encapsulateChildrenWithLabel(final PQNode node, final NodeLabel label) {
        final int numLabeledChildren = node.labeledChildren.get(label).size();
        if (numLabeledChildren == 0) {
            return null;
        } else if (numLabeledChildren == 1) {
            final PQNode labeledNode = node.labeledChildren.get(label).iterator().next();
            node.removeChild(labeledNode);
            return labeledNode;
        } else {
            // Do nothing
        }
        final PQNode newPNode = new PQNode(NodeType.PNODE);
        newPNode.relabel(label);
        // Move all children of node with the given label to be children of the new PNode.
        for (final PQNode child : node.getLabelView(label)) {
            node.removeChild(child);
            newPNode.addChild(child);
        }
        return newPNode;
    }

    // Matches leaf nodes
    private boolean templateL1(final PQNode candidate) {
        // Check whether candidate is a leaf node
        if (candidate.type != NodeType.LEAF_NODE) {
            return false;
        }
        // If the candidate's virtual number matches the current number, relabel it as full
        if (candidate.getVirtualNum() == currentNumber) {
            candidate.relabel(NodeLabel.FULL);
        }
        return true;
    }

    // Matches P-nodes where all children are empty, or all children are full
    private boolean templateP1(final PQNode candidate) {
        // Check whether candidate is a P node
        if (candidate.type != NodeType.PNODE) {
            return false;
        }
        // Check whether all children of candidate are full - if so relabel candidate as full.
        if (candidate.numChildren() == candidate.labeledChildren.get(NodeLabel.FULL).size()) {
            candidate.relabel(NodeLabel.FULL);
            return true;
        }
        // If not all children of candidate are full, check if they are all empty.
        boolean matches = (candidate.numChildren() == candidate.labeledChildren.get(NodeLabel.EMPTY).size());
        if (matches) {
        }
        return matches;
    }

    // Matches P-nodes that are the root of the pertinent subtree containing no partial nodes
    private boolean templateP2(final PQNode candidate) {
        // Check whether candidate is a P node with no partial children
        if (candidate.type != NodeType.PNODE || !candidate.labeledChildren.get(NodeLabel.PARTIAL).isEmpty()) {
            return false;
        }

        // If there is only one full node we need not change anything.
        if (candidate.labeledChildren.get(NodeLabel.FULL).size() == 1) {
            return true;
        }

        // Encapsulate full children and add the encapsulation to candidate. Note there will always be two or more full children for this template to match.
        final PQNode fullPNode = encapsulateChildrenWithLabel(candidate, NodeLabel.FULL);
        candidate.addChild(fullPNode);

        return true;
    }

    // Matches P-nodes that are not the root of the pertinent subtree containing no partial nodes
    private boolean templateP3(final PQNode candidate) {
        // Check whether candidate is a P node with no partial children
        if (candidate.type != NodeType.PNODE || !candidate.labeledChildren.get(NodeLabel.PARTIAL).isEmpty()) {
            return false;
        }

        // Encapsulate empty children (note there will always be 1 or more to match this template)
        final PQNode emptyPNode = encapsulateChildrenWithLabel(candidate, NodeLabel.EMPTY);

        // Encapsulate full children (note there will always be 1 or more to match this template)
        final PQNode fullPNode = encapsulateChildrenWithLabel(candidate, NodeLabel.FULL);

        // Make a new QNode which will take both the empty and full encapsulations as children and replace candidate in the tree. NodeLabel it partial.
        final PQNode partialQNode = new PQNode(NodeType.QNODE);
        partialQNode.relabel(NodeLabel.PARTIAL);
        partialQNode.addChild(emptyPNode);
        partialQNode.addChild(fullPNode);
        subtreeReplace(candidate, partialQNode);

        return true;
    }

    // Matches P-nodes that are the root of the pertinent subtree containing exactly one partial node
    private boolean templateP4(final PQNode candidate) {
        // Check whether candidate is a P node with exaclty one partial child
        if (candidate.type != NodeType.PNODE || candidate.labeledChildren.get(NodeLabel.PARTIAL).size() != 1) {
            return false;
        }

        // Get the single partial node which is a child of candidate
        final PQNode partialChild = candidate.labeledChildren.get(NodeLabel.PARTIAL).iterator().next();

        // Encapsulate full children and add the encapsulation to the partial child of candidate
        final PQNode fullChild = encapsulateChildrenWithLabel(candidate, NodeLabel.FULL);
        if (fullChild != null) {
            partialChild.addChild(fullChild);
        }

        candidate.relabel(NodeLabel.PARTIAL);

        return true;
    }

    // Matches P-nodes that are not the root of the pertinent subtree containing exactly one partial node
    private boolean templateP5(final PQNode candidate) {
        // Check whether candidate is a P node with exaclty one partial child
        if (candidate.type != NodeType.PNODE || candidate.labeledChildren.get(NodeLabel.PARTIAL).size() != 1) {
            return false;
        }

        // Get the single partial node which is a child of candidate
        final PQNode partialChild = candidate.labeledChildren.get(NodeLabel.PARTIAL).iterator().next();

        // Encapsulate empty children and add the encapsulation as the first child of the partial child of candidate
        final PQNode emptyChild = encapsulateChildrenWithLabel(candidate, NodeLabel.EMPTY);
        if (emptyChild != null) {
            partialChild.addFirstChild(emptyChild);
        }
        // Encapsulate full children and add to the partial child of candidate
        final PQNode fullChild = encapsulateChildrenWithLabel(candidate, NodeLabel.FULL);
        if (fullChild != null) {
            partialChild.addChild(fullChild);
        }

        // Replace candidate with its partial child
        candidate.removeChild(partialChild);    // Probably not necessary
        subtreeReplace(candidate, partialChild);
        return true;
    }

    // Matches P-nodes that are the root of the pertinent subtree containing exactly two partial node
    private boolean templateP6(final PQNode candidate) {
        // Check whether candidate is a P node with exaclty two partial children.
        if (candidate.type != NodeType.PNODE || candidate.labeledChildren.get(NodeLabel.PARTIAL).size() != 2) {
            return false;
        }

        // Get the two partial nodes which are children of candidate, noting the order they actually occur in is not important because it is a P-node.
        final Iterator<PQNode> iter = candidate.labeledChildren.get(NodeLabel.PARTIAL).iterator();
        final PQNode firstPartialChild = iter.next();
        final PQNode secondPartialChild = iter.next();

        // Encapsulate full children and add to the first partial child of candidate
        final PQNode fullChild = encapsulateChildrenWithLabel(candidate, NodeLabel.FULL);
        if (fullChild != null) {
            firstPartialChild.addChild(fullChild);
        }

        // Reverse the children of the second partial child, concatenate them to first partial child, removing the second partial child from candidate's children in the process.
        secondPartialChild.reverseChildren();
        firstPartialChild.concatenateSibling(secondPartialChild);

        candidate.relabel(NodeLabel.PARTIAL);

        return true;
    }

    // Matches Q-nodes where all children are empty, or all children are full
    private boolean templateQ1(final PQNode candidate) {
        // Check whether candidate is a Q node
        if (candidate.type != NodeType.QNODE) {
            return false;
        }
        // Check whether all children of candidate are full - if so relabel candidate as full.
        if (candidate.numChildren() == candidate.labeledChildren.get(NodeLabel.FULL).size()) {
            candidate.relabel(NodeLabel.FULL);
            return true;
        }
        // If not all children of candidate are full, check if they are all empty.
        return candidate.numChildren() == candidate.labeledChildren.get(NodeLabel.EMPTY).size();
    }

    // Matches Q-nodes with at most one partial child, deleting extra partial children if necessary
    private boolean templateQ2(final PQNode candidate) {
        // Check whether candidate is a Q node
        if (candidate.type != NodeType.QNODE) {
            return false;
        }

        // If this node's children are not in a valid order to be planar, it will be planarized by this cleaning process.
        // Regardless of whether node deletion is performed, the remaining partial child is flattened
        // and reversed as necessary. The entire node is also reversed if necessary.
        final List<PQNode> permanentlyRemovedNodes = candidate.cleanSinglyPartialQNode();

        for (final PQNode destroyed : permanentlyRemovedNodes) {
            removeLeaves(destroyed);
            numPertinentLeaves -= destroyed.getPertinentLeafCount();
        }

        candidate.relabel(NodeLabel.PARTIAL);

        return true;
    }

    // Matches Q-nodes that are the root of the pertinent subtree with exactly two partial children, deleting extra partial children if necessary.
    private boolean templateQ3(final PQNode candidate) {
        // Check whether candidate is a Q node with two or more partial children
//        int numPartialChildren = candidate.labeledChildren.get(NodeLabel.Partial).size();
        if (candidate.type != NodeType.QNODE /*|| numPartialChildren < 2*/) {
            return false;
        }

        // If this node has more than two partial children or its children are otherwise not in a valid order to be planar,
        // it will be planarized by this cleaning process. Regardless of whether node deletion is performed, the two remaining partial children are flattened and reversed as necessary.
        final List<PQNode> permanentlyRemovedNodes = candidate.cleanDoublyPartialQNode();

        for (final PQNode destroyed : permanentlyRemovedNodes) {
            removeLeaves(destroyed);
            numPertinentLeaves -= destroyed.getPertinentLeafCount();
        }

        candidate.relabel(NodeLabel.PARTIAL);

        return true;
    }

    private void removeLeaves(final PQNode node) {
        if (node.type.equals(NodeType.LEAF_NODE)) {
            leaves.get(node.getVirtualNum() - 1).remove(node);
        } else {
            for (final PQNode child : node.children) {
                removeLeaves(child);
            }
        }
    }
}
