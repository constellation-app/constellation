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

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author twilight_sparkle
 */
class PQNode {

    private PQNode parent;
    public final PQNodeList children = new PQNodeList();
    public final Map<NodeLabel, Set<PQNode>> labeledChildren = new EnumMap<>(NodeLabel.class);
    private DirectionIndicator directionIndicator = null;

    public final NodeType type;
    private NodeLabel label = NodeLabel.EMPTY;

    private int numLeafDescendants;
    private int pertinentChildCount = 0;
    private int pertinentLeafCount = 0;

    // Only used for leaf nodes to keep track of the virtual edges they describe in the bush form
    private int virtualNum;
    private int realNum;

    public PQNode(final NodeType type) {
        this.type = type;
        numLeafDescendants = type == NodeType.LEAF_NODE ? 1 : 0;
        for (final NodeLabel l : NodeLabel.values()) {
            labeledChildren.put(l, new HashSet<>());
        }
    }

    public PQNode(final NodeType type, final int virtaulNum, final int realNum) {
        this(type);
        this.virtualNum = virtaulNum;
        this.realNum = realNum;
    }

    public PQNode getParent() {
        return parent;
    }

    public void setParent(final PQNode parent) {
        this.parent = parent;
    }

    public DirectionIndicator getDirectionIndicator() {
        return directionIndicator;
    }

    public void setDirectionIndicator(final DirectionIndicator directionIndicator) {
        this.directionIndicator = directionIndicator;
    }

    public NodeLabel getLabel() {
        return label;
    }

    public void setLabel(final NodeLabel label) {
        this.label = label;
    }

    public int getPertinentChildCount() {
        return pertinentChildCount;
    }

    public void setPertinentChildCount(final int pertinentChildCount) {
        this.pertinentChildCount = pertinentChildCount;
    }

    public int getPertinentLeafCount() {
        return pertinentLeafCount;
    }

    public void setPertinentLeafCount(final int pertinentLeafCount) {
        this.pertinentLeafCount = pertinentLeafCount;
    }

    public int getVirtualNum() {
        return virtualNum;
    }

    public void setVirtualNum(final int virtualNum) {
        this.virtualNum = virtualNum;
    }

    public int getRealNum() {
        return realNum;
    }

    public void setRealNum(final int realNum) {
        this.realNum = realNum;
    }

    public int numChildren() {
        return children.getSize();
    }

    // Returns a copy of the set, used to iterate over when some of the items need to be modified or removed.
    public Set<PQNode> getLabelView(final NodeLabel label) {
        final Set<PQNode> result = new HashSet<>();
        result.addAll(labeledChildren.get(label));
        return result;
    }

    public void relabel(final NodeLabel newLabel) {
        if (parent != null) {
            parent.labeledChildren.get(label).remove(this);
            parent.labeledChildren.get(newLabel).add(this);
        }
        label = newLabel;
    }

    private void propogateDescsendantChange(final int delta) {
        numLeafDescendants += delta;
        if (parent != null) {
            parent.propogateDescsendantChange(delta);
        }
    }

    public void addChild(final PQNode child) {
        addChild(child, true);
    }

    public void addFirstChild(final PQNode child) {
        addChild(child, false);
    }

    private void addChild(final PQNode child, final boolean toEnd) {
        child.parent = this;

        if (toEnd) {
            children.addLast(child);
        } else {
            children.addFirst(child);
        }
        labeledChildren.get(child.label).add(child);
        propogateDescsendantChange(child.numLeafDescendants);
    }

    public void removeChild(final PQNode child) {
        child.parent = null;
        children.remove(child);
        labeledChildren.get(child.label).remove(child);
        propogateDescsendantChange(-child.numLeafDescendants);
    }

    public void replaceChild(final PQNode child, final PQNode newChild) {
        child.parent = null;
        newChild.parent = this;
        children.replace(child, newChild);
        labeledChildren.get(child.label).remove(child);
        labeledChildren.get(newChild.label).add(newChild);
        propogateDescsendantChange(newChild.numLeafDescendants - child.numLeafDescendants);
    }

    public void reverseChildren() {
        // reverse the direction indicator if present
        if (directionIndicator != null) {
            directionIndicator.reverse();
        }
        children.reverse();
    }

    // Appends the specified sibling node's children to the end of this node's list of children.
    // and removes the concatenated sibling from the shared parent.
    // Note as a result numDescendants of this node needs to be changed, but the change shouldn't be propogated upwards.
    public void concatenateSibling(final PQNode toConcatenate) {
        for (final PQNode child : toConcatenate.children) {
            child.parent = this;
            labeledChildren.get(child.label).add(child);
            numLeafDescendants += child.numLeafDescendants;
        }
        children.concatenate(toConcatenate.children);
        parent.children.remove(toConcatenate);
        parent.labeledChildren.get(toConcatenate.label).remove(toConcatenate);
    }

    // Flattens the specified child node's children into this node's list of children
    // Note as a result no numLeafDescendants fields need to be changed.
    public void flatten(final PQNode toFlatten) {
        labeledChildren.get(toFlatten.label).remove(toFlatten);
        for (final PQNode child : toFlatten.children) {
            child.parent = this;
            labeledChildren.get(child.label).add(child);
        }
        children.flatten(toFlatten);
    }

    // Removes all empty children from one side of the specified dividing node,
    // and all full children from the other side. Empty children are removed from the right side
    // if reverse is false, or the left side if reverse is true.
    // Should only be called on QNodes with at most one partial child.
    public List<PQNode> trimAndFlattenQNodeChildren(final PQNode dividingNode, final boolean reverse) {
        final List<PQNode> removed = new LinkedList<>();
        NodeLabel toRemove = reverse ^ (dividingNode == null) ? NodeLabel.EMPTY : NodeLabel.FULL;
        PQNode toFlatten = null;
        for (final PQNode child : children) {
            if (toFlatten != null) {
                flatten(toFlatten);
                toFlatten = null;
            }
            if (child.label.equals(toRemove)) {
                removeChild(child);
                removed.add(child);
            } else if (child.label.equals(NodeLabel.PARTIAL) && child != dividingNode) {
                for (PQNode grandchild : child.getLabelView(toRemove)) {
                    child.removeChild(grandchild);
                    removed.add(grandchild);
                }
                toFlatten = child;
            }
            if (child == dividingNode) {
                if (child.label.equals(NodeLabel.PARTIAL)) {
                    if (reverse) {
                        child.reverseChildren();
                    }
                    toFlatten = child;
                }
                toRemove = (toRemove.equals(NodeLabel.EMPTY)) ? NodeLabel.FULL : NodeLabel.EMPTY;
            }
        }
        if (toFlatten != null) {
            flatten(toFlatten);
        }
        // After performing any trimming (if necessary), reverse everything if specified.
        if (reverse) {
            reverseChildren();
        }
        return removed;
    }

    // Trims all empty children between the two specified dividing nodes
    // and all full children outside of the two dividing nodes.
    // Should only be called on QNodes with two partial children.
    public List<PQNode> trimAndFlattenQNodeChildren(final PQNode divideStart, final PQNode divideEnd) {
        final List<PQNode> removed = new LinkedList<>();
        NodeLabel toRemove = NodeLabel.FULL;
        PQNode toFlatten = null;
        for (final PQNode child : children) {
            if (toFlatten != null) {
                flatten(toFlatten);
                toFlatten = null;
            }
            if (child == divideStart) {
                if (child.label.equals(NodeLabel.PARTIAL)) {
                    toFlatten = child;
                }
                toRemove = NodeLabel.EMPTY;
            } else if (child == divideEnd) {
                if (child.label.equals(NodeLabel.PARTIAL)) {
                    child.reverseChildren();
                    toFlatten = child;
                }
                toRemove = NodeLabel.FULL;
            }
            if (child.label.equals(toRemove)) {
                removeChild(child);
                removed.add(child);
            } else if (child.label.equals(NodeLabel.PARTIAL) && child != divideStart && child != divideEnd) {
                for (final PQNode grandchild : child.getLabelView(toRemove)) {
                    child.removeChild(grandchild);
                    removed.add(grandchild);
                }
                toFlatten = child;
            }
        }
        if (toFlatten != null) {
            flatten(toFlatten);
        }
        return removed;
    }

    // Finds the best child on which to separate of division so that when the node is trimmed
    // the minimum number of leaf descendants are removed.
    // Should only be called on QNodes with at most one partial child.
    public List<PQNode> cleanSinglyPartialQNode() {
        int count = 0;
        int reverseCount = 0;
        int lastChange = 0;
        int carry = 0;
        int maxCount = 0;
        int maxReverseCount = 0;
        boolean reverse;
        PQNode maxPos = null;
        PQNode maxReversePos = null;
        PQNode lastChild = null;
        for (final PQNode child : children) {
            if (carry != 0) {
                count -= carry;
                carry = 0;
            }
            reverseCount -= lastChange;
            switch (child.label) {
                case EMPTY:
                    count += child.numLeafDescendants;
                    lastChange = child.numLeafDescendants;
                    break;
                case FULL:
                    count -= child.numLeafDescendants;
                    lastChange = -child.numLeafDescendants;
                    break;
                case PARTIAL:
                    for (PQNode grandchild : child.labeledChildren.get(NodeLabel.EMPTY)) {
                        count += grandchild.numLeafDescendants;
                        lastChange += grandchild.numLeafDescendants;
                    }
                    for (PQNode grandchild : child.labeledChildren.get(NodeLabel.FULL)) {
                        carry += grandchild.numLeafDescendants;
                        reverseCount += grandchild.numLeafDescendants;
                    }
                    break;
                default:
                    break;
            }
            if (count > maxCount) {
                maxCount = count;
                maxPos = child;
            }
            if (reverseCount > maxReverseCount) {
                maxReverseCount = reverseCount;
                maxReversePos = lastChild;
            }
            lastChild = child;
        }
        maxReverseCount -= lastChange;
        if (reverseCount > maxReverseCount) {
            maxReverseCount = reverseCount;
            maxReversePos = lastChild;
        }
        reverse = maxReverseCount > maxCount;
        return trimAndFlattenQNodeChildren(reverse ? maxReversePos : maxPos, reverse);
    }

    // Finds the best children on which to separate the full section so that when the node is trimmed
    // the minimum number of leaf descendants are removed.
    // Should only be called on QNodes with at most one partial child.
    public List<PQNode> cleanDoublyPartialQNode() {
        int count = 0;
        int carry = 0;
        int maxZoneScore = 0;
        int maxCountSinceAnchor = 0;
        int countAtAnchor = 0;
        PQNode anchorPos = null;
        PQNode maxNodeSinceAnchor = null;
        PQNode maxAnchorPos = null;
        PQNode maxNodeSinceMaxAnchor = null;
        for (final PQNode child : children) {
            count += carry;
            carry = 0;
            switch (child.label) {
                case EMPTY:
                    carry = -child.numLeafDescendants;
                    break;
                case FULL:
                    carry = child.numLeafDescendants;
                    break;
                case PARTIAL:
                    for (PQNode grandchild : child.labeledChildren.get(NodeLabel.EMPTY)) {
                        if (count <= countAtAnchor) {
                            count -= grandchild.numLeafDescendants;
                        } else {
                            carry -= grandchild.numLeafDescendants;
                        }
                    }
                    for (PQNode grandchild : child.labeledChildren.get(NodeLabel.FULL)) {
                        if (count <= countAtAnchor) {
                            carry += grandchild.numLeafDescendants;
                        } else {
                            count += grandchild.numLeafDescendants;
                        }
                    }
                    break;
                default:
                    break;
            }
            if (count <= countAtAnchor) {
                final int zoneScore = maxCountSinceAnchor - countAtAnchor;
                if (zoneScore >= maxZoneScore) {
                    maxAnchorPos = anchorPos;
                    maxNodeSinceMaxAnchor = maxNodeSinceAnchor;
                    maxZoneScore = zoneScore;
                }
                anchorPos = child;
                countAtAnchor = count;
                maxCountSinceAnchor = countAtAnchor;
            } else if (count > maxCountSinceAnchor) {
                maxCountSinceAnchor = count;
                maxNodeSinceAnchor = child;
            }
        }
        count += carry;
        if (count > maxCountSinceAnchor) {
            maxCountSinceAnchor = count;
            maxNodeSinceAnchor = null;
        }
        final int zoneScore = maxCountSinceAnchor - countAtAnchor;
        if (zoneScore >= maxZoneScore) {
            maxAnchorPos = anchorPos;
            maxNodeSinceMaxAnchor = maxNodeSinceAnchor;
        }
        return trimAndFlattenQNodeChildren(maxAnchorPos, maxNodeSinceMaxAnchor);
    }

    public static class PQNodeTest {

        public PQNodeTest() {
            //Only used for testing, unpopulated method
        }

        private PQNode makeNode() {
            return makeNode(NodeType.PNODE);
        }

        private PQNode makeQNodeWithChildren(final PQNode[] children) {
            final PQNode node = makeNode(NodeType.QNODE);
            for (final PQNode child : children) {
                node.addChild(child);
            }
            return node;
        }

        private PQNode makeNode(final NodeType type) {
            return new PQNode(type);
        }

        public void testAdd() {

            Iterator<PQNode> iter;
            PQNode node = makeNode();
            PQNode child = makeNode();
            PQNode fullChild1 = makeNode();
            PQNode fullChild2 = makeNode();
            PQNode grandChild = makeNode();
            PQNode grandChild2 = makeNode();

            child.label = NodeLabel.EMPTY;
            child.numLeafDescendants = 1;
            fullChild1.label = NodeLabel.FULL;
            fullChild1.numLeafDescendants = 1;
            fullChild2.label = NodeLabel.FULL;
            fullChild2.numLeafDescendants = 1;
            grandChild.label = NodeLabel.EMPTY;
            grandChild.numLeafDescendants = 2;
            grandChild2.label = NodeLabel.EMPTY;
            grandChild2.numLeafDescendants = 1;

            // Test that a new node has no children
            assert node.children.getSize() == 0;
            assert node.parent == null;
            assert node.numLeafDescendants == 0;

            iter = node.children.iterator();
            assert !iter.hasNext();

            assert node.labeledChildren.get(NodeLabel.EMPTY).isEmpty();
            assert node.labeledChildren.get(NodeLabel.FULL).isEmpty();
            assert node.labeledChildren.get(NodeLabel.PARTIAL).isEmpty();

            // Test adding a child
            node.addChild(child);

            // Check parent metrics
            assert node.children.getSize() == 1;
            assert node.parent == null;
            assert node.numLeafDescendants == 1;

            // Check list of children
            iter = node.children.iterator();
            assert iter.next() == child;
            assert !iter.hasNext();

            // Check sets of children
            assert node.labeledChildren.get(NodeLabel.EMPTY).size() == 1;
            assert node.labeledChildren.get(NodeLabel.EMPTY).contains(child);
            assert node.labeledChildren.get(NodeLabel.FULL).isEmpty();
            assert node.labeledChildren.get(NodeLabel.PARTIAL).isEmpty();

            // Check child metrics
            assert child.children.getSize() == 0;
            assert child.parent == node;
            assert child.numLeafDescendants == 1;
            assert !child.children.iterator().hasNext();

            // Test adding a second child with a different label
            node.addChild(fullChild1);

            // Check parent metrics
            assert node.children.getSize() == 2;
            assert node.parent == null;
            assert node.numLeafDescendants == 2;

            // Check lists of children
            iter = node.children.iterator();
            assert iter.next() == child;
            assert iter.next() == fullChild1;
            assert !iter.hasNext();

            // Check sets of children
            assert node.labeledChildren.get(NodeLabel.EMPTY).size() == 1;
            assert node.labeledChildren.get(NodeLabel.EMPTY).contains(child);
            assert node.labeledChildren.get(NodeLabel.FULL).size() == 1;
            assert node.labeledChildren.get(NodeLabel.FULL).contains(fullChild1);
            assert node.labeledChildren.get(NodeLabel.PARTIAL).isEmpty();

            // Check child metrics
            assert fullChild1.children.getSize() == 0;
            assert fullChild1.parent == node;
            assert fullChild1.numLeafDescendants == 1;
            assert !fullChild1.children.iterator().hasNext();

            // Test adding a child with the same label to the beginning of the list
            node.addFirstChild(fullChild2);

            // Check parent metrics
            assert node.children.getSize() == 3;
            assert node.parent == null;
            assert node.numLeafDescendants == 3;

            // Check lists of children
            iter = node.children.iterator();
            assert iter.next() == fullChild2;
            assert iter.next() == child;
            assert iter.next() == fullChild1;
            assert !iter.hasNext();

            // Check sets of children
            assert node.labeledChildren.get(NodeLabel.EMPTY).size() == 1;
            assert node.labeledChildren.get(NodeLabel.EMPTY).contains(child);
            assert node.labeledChildren.get(NodeLabel.FULL).size() == 2;
            assert node.labeledChildren.get(NodeLabel.FULL).contains(fullChild1);
            assert node.labeledChildren.get(NodeLabel.FULL).contains(fullChild2);
            assert node.labeledChildren.get(NodeLabel.PARTIAL).isEmpty();

            // Check child metrics
            assert fullChild2.children.getSize() == 0;
            assert fullChild2.parent == node;
            assert fullChild2.numLeafDescendants == 1;
            assert !fullChild2.children.iterator().hasNext();

            // Test adding a grandchild
            child.addFirstChild(grandChild);

            // Check grandparent metrics
            assert node.children.getSize() == 3;
            assert node.parent == null;
            assert node.numLeafDescendants == 5;

            // Check parent metrics
            assert child.children.getSize() == 1;
            assert child.parent == node;
            assert child.numLeafDescendants == 3;

            // Check list of children
            iter = child.children.iterator();
            assert iter.next() == grandChild;
            assert !iter.hasNext();

            // Check sets of children
            assert child.labeledChildren.get(NodeLabel.EMPTY).size() == 1;
            assert child.labeledChildren.get(NodeLabel.EMPTY).contains(grandChild);
            assert child.labeledChildren.get(NodeLabel.FULL).isEmpty();
            assert child.labeledChildren.get(NodeLabel.PARTIAL).isEmpty();

            // Check child metrics
            assert grandChild.children.getSize() == 0;
            assert grandChild.parent == child;
            assert grandChild.numLeafDescendants == 2;
            assert !grandChild.children.iterator().hasNext();

            // Test adding a second grandchild
            child.addChild(grandChild2);

            // Check grandparent metrics
            assert node.children.getSize() == 3;
            assert node.parent == null;
            assert node.numLeafDescendants == 6;

            // Check parent metrics
            assert child.children.getSize() == 2;
            assert child.parent == node;
            assert child.numLeafDescendants == 4;

            // Check list of children
            iter = child.children.iterator();
            assert iter.next() == grandChild;
            assert iter.next() == grandChild2;
            assert !iter.hasNext();

            // Check sets of children
            assert child.labeledChildren.get(NodeLabel.EMPTY).size() == 2;
            assert child.labeledChildren.get(NodeLabel.EMPTY).contains(grandChild);
            assert child.labeledChildren.get(NodeLabel.EMPTY).contains(grandChild2);
            assert child.labeledChildren.get(NodeLabel.FULL).isEmpty();
            assert child.labeledChildren.get(NodeLabel.PARTIAL).isEmpty();

            // Check child metrics
            assert grandChild2.children.getSize() == 0;
            assert grandChild2.parent == child;
            assert grandChild2.numLeafDescendants == 1;
            assert !grandChild2.children.iterator().hasNext();

        }

        public void testRemove() {

            Iterator<PQNode> iter;
            PQNode node = makeNode();
            PQNode child = makeNode();
            PQNode fullChild1 = makeNode();
            PQNode fullChild2 = makeNode();
            PQNode grandChild = makeNode();
            PQNode grandChild2 = makeNode();

            child.label = NodeLabel.EMPTY;
            child.numLeafDescendants = 1;
            fullChild1.label = NodeLabel.FULL;
            fullChild1.numLeafDescendants = 1;
            fullChild2.label = NodeLabel.FULL;
            fullChild2.numLeafDescendants = 1;
            grandChild.label = NodeLabel.EMPTY;
            grandChild.numLeafDescendants = 2;
            grandChild2.label = NodeLabel.EMPTY;
            grandChild2.numLeafDescendants = 1;

            // Build same structure as in testAdd()
            node.addChild(child);
            node.addChild(fullChild1);
            node.addFirstChild(fullChild2);
            child.addChild(grandChild);
            child.addChild(grandChild2);

            // Test removing a child
            node.removeChild(fullChild1);

            // Check parent metrics
            assert node.children.getSize() == 2;
            assert node.parent == null;
            assert node.numLeafDescendants == 5;

            // Check list of children
            iter = node.children.iterator();
            assert iter.next() == fullChild2;
            assert iter.next() == child;
            assert !iter.hasNext();

            // Check sets of children
            assert node.labeledChildren.get(NodeLabel.EMPTY).size() == 1;
            assert node.labeledChildren.get(NodeLabel.EMPTY).contains(child);
            assert node.labeledChildren.get(NodeLabel.FULL).size() == 1;
            assert node.labeledChildren.get(NodeLabel.FULL).contains(fullChild2);
            assert node.labeledChildren.get(NodeLabel.PARTIAL).isEmpty();

            // Test removing a grandchild
            child.removeChild(grandChild2);

            // Check grandparent metrics
            assert node.children.getSize() == 2;
            assert node.parent == null;
            assert node.numLeafDescendants == 4;

            // Check parent metrics
            assert child.children.getSize() == 1;
            assert child.parent == node;
            assert child.numLeafDescendants == 3;

            // Check list of children
            iter = child.children.iterator();
            assert iter.next() == grandChild;
            assert !iter.hasNext();

            // Check sets of children
            assert child.labeledChildren.get(NodeLabel.EMPTY).size() == 1;
            assert child.labeledChildren.get(NodeLabel.EMPTY).contains(grandChild);
            assert child.labeledChildren.get(NodeLabel.FULL).isEmpty();
            assert child.labeledChildren.get(NodeLabel.PARTIAL).isEmpty();

            // Test removing a child with a grandchild
            node.removeChild(child);

            // Check parent metrics
            assert node.children.getSize() == 1;
            assert node.parent == null;
            assert node.numLeafDescendants == 1;

            // Check lists of children
            iter = node.children.iterator();
            assert iter.next() == fullChild2;
            assert !iter.hasNext();

            // Check sets of children
            assert node.labeledChildren.get(NodeLabel.EMPTY).isEmpty();
            assert node.labeledChildren.get(NodeLabel.FULL).size() == 1;
            assert node.labeledChildren.get(NodeLabel.FULL).contains(fullChild2);
            assert node.labeledChildren.get(NodeLabel.PARTIAL).isEmpty();

            // Test removing the last child
            node.removeChild(fullChild2);

            // Check parent metrics
            assert node.children.getSize() == 0;
            assert node.parent == null;
            assert node.numLeafDescendants == 0;

            // Check lists of children
            iter = node.children.iterator();
            assert !iter.hasNext();

            // Check sets of children
            assert node.labeledChildren.get(NodeLabel.EMPTY).isEmpty();
            assert node.labeledChildren.get(NodeLabel.FULL).isEmpty();
            assert node.labeledChildren.get(NodeLabel.PARTIAL).isEmpty();

        }

        public void testReplace() {

            Iterator<PQNode> iter;
            PQNode node = makeNode();
            PQNode child1 = makeNode();
            PQNode child2 = makeNode();
            PQNode replacementChild1 = makeNode();
            PQNode replacementChild2 = makeNode();
            PQNode grandChild = makeNode();
            PQNode grandChild2 = makeNode();
            PQNode replacementGrandChild = makeNode();

            child1.label = NodeLabel.EMPTY;
            replacementChild1.label = NodeLabel.FULL;
            child2.label = NodeLabel.FULL;
            replacementChild2.label = NodeLabel.EMPTY;
            grandChild.label = NodeLabel.EMPTY;
            grandChild2.label = NodeLabel.FULL;
            replacementGrandChild.label = NodeLabel.PARTIAL;

            child1.numLeafDescendants = 1;
            replacementChild1.numLeafDescendants = 2;
            child2.numLeafDescendants = 1;
            replacementChild2.numLeafDescendants = 3;
            grandChild.numLeafDescendants = 2;
            grandChild2.numLeafDescendants = 1;
            replacementGrandChild.numLeafDescendants = 0;

            // Build the beginning structure
            node.addChild(child1);
            node.addChild(child2);
            child2.addChild(grandChild);
            replacementChild2.addChild(grandChild2);

            // Test replacing a leaf node a child
            node.replaceChild(child1, replacementChild1);

            // Check parent metrics
            assert node.children.getSize() == 2;
            assert node.parent == null;
            assert node.numLeafDescendants == 5;

            // Check list of children
            iter = node.children.iterator();
            assert iter.next() == replacementChild1;
            assert iter.next() == child2;
            assert !iter.hasNext();

            // Check sets of children
            assert node.labeledChildren.get(NodeLabel.EMPTY).isEmpty();
            assert node.labeledChildren.get(NodeLabel.FULL).size() == 2;
            assert node.labeledChildren.get(NodeLabel.FULL).contains(replacementChild1);
            assert node.labeledChildren.get(NodeLabel.FULL).contains(child2);
            assert node.labeledChildren.get(NodeLabel.PARTIAL).isEmpty();

            // Check child metrics
            assert replacementChild1.children.getSize() == 0;
            assert replacementChild1.parent == node;
            assert replacementChild1.numLeafDescendants == 2;
            assert !replacementChild1.children.iterator().hasNext();

            // Test replacing a leaf node grandchild
            child2.replaceChild(grandChild, replacementGrandChild);

            // Check grandparent metrics
            assert node.children.getSize() == 2;
            assert node.parent == null;
            assert node.numLeafDescendants == 3;

            // Check parent metrics
            assert child2.children.getSize() == 1;
            assert child2.parent == node;
            assert child2.numLeafDescendants == 1;

            // Check list of children
            iter = child2.children.iterator();
            assert iter.next() == replacementGrandChild;
            assert !iter.hasNext();

            // Check sets of children
            assert child2.labeledChildren.get(NodeLabel.EMPTY).isEmpty();
            assert child2.labeledChildren.get(NodeLabel.FULL).isEmpty();
            assert child2.labeledChildren.get(NodeLabel.PARTIAL).size() == 1;
            assert child2.labeledChildren.get(NodeLabel.PARTIAL).contains(replacementGrandChild);

            // Check child metrics
            assert replacementGrandChild.children.getSize() == 0;
            assert replacementGrandChild.parent == child2;
            assert replacementGrandChild.numLeafDescendants == 0;
            assert !replacementGrandChild.children.iterator().hasNext();

            // Test replacing an internal child
            node.replaceChild(child2, replacementChild2);

            // Check parent metrics
            assert node.children.getSize() == 2;
            assert node.parent == null;
            assert node.numLeafDescendants == 6;

            // Check lists of children
            iter = node.children.iterator();
            assert iter.next() == replacementChild1;
            assert iter.next() == replacementChild2;
            assert !iter.hasNext();

            // Check sets of children
            assert node.labeledChildren.get(NodeLabel.EMPTY).size() == 1;
            assert node.labeledChildren.get(NodeLabel.EMPTY).contains(replacementChild2);
            assert node.labeledChildren.get(NodeLabel.FULL).size() == 1;
            assert node.labeledChildren.get(NodeLabel.FULL).contains(replacementChild1);
            assert node.labeledChildren.get(NodeLabel.PARTIAL).isEmpty();

            // Check child metrics
            assert replacementChild2.children.getSize() == 1;
            assert replacementChild2.parent == node;
            assert replacementChild2.numLeafDescendants == 4;

        }

        public void testReverse() {
            Iterator<PQNode> iter;
            PQNode node = makeNode();
            PQNode child1 = makeNode();
            PQNode child2 = makeNode();
            PQNode grandChild1 = makeNode();
            PQNode grandChild2 = makeNode();

            child1.label = NodeLabel.EMPTY;
            child1.numLeafDescendants = 1;
            child2.label = NodeLabel.FULL;
            child2.numLeafDescendants = 1;
            grandChild1.label = NodeLabel.EMPTY;
            grandChild1.numLeafDescendants = 2;
            grandChild2.label = NodeLabel.EMPTY;
            grandChild2.numLeafDescendants = 0;

            node.addChild(child1);
            node.addChild(child2);
            child2.addChild(grandChild1);
            child2.addChild(grandChild2);

            //Test reversing the parent
            node.reverseChildren();

            // Check node metrics
            assert node.children.getSize() == 2;
            assert node.parent == null;
            assert node.numLeafDescendants == 4;

            // Check list of children
            iter = node.children.iterator();
            assert iter.next() == child2;
            assert iter.next() == child1;
            assert !iter.hasNext();

            // Check sets of children
            assert node.labeledChildren.get(NodeLabel.EMPTY).size() == 1;
            assert node.labeledChildren.get(NodeLabel.FULL).size() == 1;
            assert node.labeledChildren.get(NodeLabel.EMPTY).contains(child1);
            assert node.labeledChildren.get(NodeLabel.FULL).contains(child2);
            assert node.labeledChildren.get(NodeLabel.PARTIAL).isEmpty();

            // Test reversing a child with no children
            child1.reverseChildren();

            // Check node metrics
            assert child1.children.getSize() == 0;
            assert child1.parent == node;
            assert child1.numLeafDescendants == 1;

            // Check list of children
            iter = child1.children.iterator();
            assert !iter.hasNext();

            // Check sets of children
            assert child1.labeledChildren.get(NodeLabel.EMPTY).isEmpty();
            assert child1.labeledChildren.get(NodeLabel.FULL).isEmpty();
            assert child1.labeledChildren.get(NodeLabel.PARTIAL).isEmpty();

            // Test reversing a child with children
            child2.reverseChildren();

            // Check node metrics
            assert child2.children.getSize() == 2;
            assert child2.parent == node;
            assert child2.numLeafDescendants == 3;

            // Check list of grandparentchildren
            iter = child2.children.iterator();
            assert iter.next() == grandChild2;
            assert iter.next() == grandChild1;
            assert !iter.hasNext();

            // Check sets of children
            assert child2.labeledChildren.get(NodeLabel.EMPTY).size() == 2;
            assert child2.labeledChildren.get(NodeLabel.EMPTY).contains(grandChild1);
            assert child2.labeledChildren.get(NodeLabel.EMPTY).contains(grandChild2);
            assert child2.labeledChildren.get(NodeLabel.FULL).isEmpty();
            assert child2.labeledChildren.get(NodeLabel.PARTIAL).isEmpty();

        }

        public void testConcatenate() {

            Iterator<PQNode> iter;
            PQNode node = makeNode();
            PQNode child1 = makeNode();
            PQNode child2 = makeNode();
            PQNode child3 = makeNode();
            PQNode child2grandChild1 = makeNode();
            PQNode child2grandChild2 = makeNode();
            PQNode child3grandChild1 = makeNode();
            PQNode child3grandChild2 = makeNode();

            child1.label = NodeLabel.EMPTY;
            child1.numLeafDescendants = 0;
            child2.label = NodeLabel.FULL;
            child2.numLeafDescendants = 0;
            child3.label = NodeLabel.FULL;
            child3.numLeafDescendants = 0;
            child2grandChild1.label = NodeLabel.EMPTY;
            child2grandChild1.numLeafDescendants = 2;
            child2grandChild2.label = NodeLabel.EMPTY;
            child2grandChild2.numLeafDescendants = 0;
            child3grandChild1.label = NodeLabel.FULL;
            child3grandChild1.numLeafDescendants = 1;
            child3grandChild2.label = NodeLabel.FULL;
            child3grandChild2.numLeafDescendants = 0;

            node.addChild(child1);
            node.addChild(child2);
            node.addChild(child3);
            child2.addChild(child2grandChild1);
            child2.addChild(child2grandChild2);
            child3.addChild(child3grandChild1);
            child3.addChild(child3grandChild2);

            // Test concatenating a child in the middle
            child3.concatenateSibling(child2);

            // Check parent node metrics
            assert node.children.getSize() == 2;
            assert node.parent == null;
            assert node.numLeafDescendants == 3;

            // Check list of children
            iter = node.children.iterator();
            assert iter.next() == child1;
            assert iter.next() == child3;
            assert !iter.hasNext();

            // Check sets of children
            assert node.labeledChildren.get(NodeLabel.EMPTY).size() == 1;
            assert node.labeledChildren.get(NodeLabel.EMPTY).contains(child1);
            assert node.labeledChildren.get(NodeLabel.FULL).size() == 1;
            assert node.labeledChildren.get(NodeLabel.FULL).contains(child3);
            assert node.labeledChildren.get(NodeLabel.PARTIAL).isEmpty();

            // Check concatenated child metrics
            assert child3.children.getSize() == 4;
            assert child3.parent == node;
            assert child3.numLeafDescendants == 3;

            // check concatenated child's list of children
            iter = child3.children.iterator();
            assert iter.next() == child3grandChild1;
            assert iter.next() == child3grandChild2;
            assert iter.next() == child2grandChild1;
            assert iter.next() == child2grandChild2;
            assert !iter.hasNext();

            // Check concatenated child's sets of children
            assert child3.labeledChildren.get(NodeLabel.EMPTY).size() == 2;
            assert child3.labeledChildren.get(NodeLabel.EMPTY).contains(child2grandChild1);
            assert child3.labeledChildren.get(NodeLabel.EMPTY).contains(child2grandChild2);
            assert child3.labeledChildren.get(NodeLabel.FULL).size() == 2;
            assert child3.labeledChildren.get(NodeLabel.FULL).contains(child3grandChild1);
            assert child3.labeledChildren.get(NodeLabel.FULL).contains(child3grandChild2);
            assert child3.labeledChildren.get(NodeLabel.PARTIAL).isEmpty();

            // check grandchildren parent pointers
            assert child3grandChild1.parent == child3;
            assert child3grandChild2.parent == child3;
            assert child2grandChild1.parent == child3;
            assert child2grandChild2.parent == child3;

            // Test concatenating a child at the beginning with no children
            child3.concatenateSibling(child1);

            // Check parent node metrics
            assert node.children.getSize() == 1;
            assert node.parent == null;
            assert node.numLeafDescendants == 3;

            // Check list of children
            iter = node.children.iterator();
            assert iter.next() == child3;
            assert !iter.hasNext();

            // Check sets of children
            assert node.labeledChildren.get(NodeLabel.EMPTY).isEmpty();
            assert node.labeledChildren.get(NodeLabel.FULL).size() == 1;
            assert node.labeledChildren.get(NodeLabel.FULL).contains(child3);
            assert node.labeledChildren.get(NodeLabel.PARTIAL).isEmpty();

            // Check concatenated child metrics
            assert child3.children.getSize() == 4;
            assert child3.parent == node;
            assert child3.numLeafDescendants == 3;

            // check concatenated child's list of children
            iter = child3.children.iterator();
            assert iter.next() == child3grandChild1;
            assert iter.next() == child3grandChild2;
            assert iter.next() == child2grandChild1;
            assert iter.next() == child2grandChild2;
            assert !iter.hasNext();

            // Check concatenated child's sets of children
            assert child3.labeledChildren.get(NodeLabel.EMPTY).size() == 2;
            assert child3.labeledChildren.get(NodeLabel.EMPTY).contains(child2grandChild1);
            assert child3.labeledChildren.get(NodeLabel.EMPTY).contains(child2grandChild2);
            assert child3.labeledChildren.get(NodeLabel.FULL).size() == 2;
            assert child3.labeledChildren.get(NodeLabel.FULL).contains(child3grandChild1);
            assert child3.labeledChildren.get(NodeLabel.FULL).contains(child3grandChild2);
            assert child3.labeledChildren.get(NodeLabel.PARTIAL).isEmpty();

        }

        public void testFlatten() {

            Iterator<PQNode> iter;
            PQNode node = makeNode();
            PQNode child1 = makeNode();
            PQNode child2 = makeNode();
            PQNode child3 = makeNode();
            PQNode child2grandChild1 = makeNode();
            PQNode child2grandChild2 = makeNode();
            PQNode child3grandChild1 = makeNode();
            PQNode child3grandChild2 = makeNode();

            child1.label = NodeLabel.EMPTY;
            child1.numLeafDescendants = 1;
            child2.label = NodeLabel.FULL;
            child2.numLeafDescendants = 0;
            child3.label = NodeLabel.FULL;
            child3.numLeafDescendants = 0;
            child2grandChild1.label = NodeLabel.EMPTY;
            child2grandChild1.numLeafDescendants = 2;
            child2grandChild2.label = NodeLabel.EMPTY;
            child2grandChild2.numLeafDescendants = 0;
            child3grandChild1.label = NodeLabel.FULL;
            child3grandChild1.numLeafDescendants = 1;
            child3grandChild2.label = NodeLabel.FULL;
            child3grandChild2.numLeafDescendants = 0;

            node.addChild(child1);
            node.addChild(child2);
            node.addChild(child3);
            child2.addChild(child2grandChild1);
            child2.addChild(child2grandChild2);
            child3.addChild(child3grandChild1);
            child3.addChild(child3grandChild2);

            // Test flattening a child in the middle
            node.flatten(child2);

            // Check node metrics
            assert node.children.getSize() == 4;
            assert node.parent == null;
            assert node.numLeafDescendants == 4;

            // Check list of children
            iter = node.children.iterator();
            assert iter.next() == child1;
            assert iter.next() == child2grandChild1;
            assert iter.next() == child2grandChild2;
            assert iter.next() == child3;
            assert !iter.hasNext();

            // Check sets of children
            assert node.labeledChildren.get(NodeLabel.EMPTY).size() == 3;
            assert node.labeledChildren.get(NodeLabel.EMPTY).contains(child1);
            assert node.labeledChildren.get(NodeLabel.EMPTY).contains(child2grandChild1);
            assert node.labeledChildren.get(NodeLabel.EMPTY).contains(child2grandChild2);
            assert node.labeledChildren.get(NodeLabel.FULL).size() == 1;
            assert node.labeledChildren.get(NodeLabel.FULL).contains(child3);
            assert node.labeledChildren.get(NodeLabel.PARTIAL).isEmpty();

            // Test flattening a child at the end
            node.flatten(child3);

            // Check node metrics
            assert node.children.getSize() == 5;
            assert node.parent == null;
            assert node.numLeafDescendants == 4;

            // Check list of children
            iter = node.children.iterator();
            assert iter.next() == child1;
            assert iter.next() == child2grandChild1;
            assert iter.next() == child2grandChild2;
            assert iter.next() == child3grandChild1;
            assert iter.next() == child3grandChild2;
            assert !iter.hasNext();

            // Check sets of children
            assert node.labeledChildren.get(NodeLabel.EMPTY).size() == 3;
            assert node.labeledChildren.get(NodeLabel.EMPTY).contains(child1);
            assert node.labeledChildren.get(NodeLabel.EMPTY).contains(child2grandChild1);
            assert node.labeledChildren.get(NodeLabel.EMPTY).contains(child2grandChild2);
            assert node.labeledChildren.get(NodeLabel.FULL).size() == 2;
            assert node.labeledChildren.get(NodeLabel.FULL).contains(child3grandChild1);
            assert node.labeledChildren.get(NodeLabel.FULL).contains(child3grandChild2);
            assert node.labeledChildren.get(NodeLabel.PARTIAL).isEmpty();

        }

        public void testTrimSinglyPartial() {

            Iterator<PQNode> iter;
            PQNode node;
            PQNode child1 = makeNode();
            PQNode child2 = makeNode();
            PQNode child3 = makeNode();
            PQNode child4 = makeNode();
            PQNode[] children = {child1, child2, child3, child4};

            // Test 1 setup
            child1.label = NodeLabel.EMPTY;
            child2.label = NodeLabel.EMPTY;
            child3.label = NodeLabel.FULL;
            child4.label = NodeLabel.EMPTY;
            node = makeQNodeWithChildren(children);

            // Test 1 - test trimming pointer at first node, with deletion immediately after
            node.trimAndFlattenQNodeChildren(child1, false);

            iter = node.children.iterator();
            assert iter.next() == child1;
            assert iter.next() == child3;
            assert !iter.hasNext();

            // Test 2 setup
            child1.label = NodeLabel.EMPTY;
            child2.label = NodeLabel.FULL;
            child3.label = NodeLabel.FULL;
            child4.label = NodeLabel.FULL;
            node = makeQNodeWithChildren(children);

            // Test 2 - test trimming pointer at a middle node, with deletion immediately before and after
            node.trimAndFlattenQNodeChildren(child3, false);

            iter = node.children.iterator();
            assert iter.next() == child1;
            assert iter.next() == child4;
            assert !iter.hasNext();

            // Test 3 setup
            child1.label = NodeLabel.EMPTY;
            child2.label = NodeLabel.EMPTY;
            child3.label = NodeLabel.FULL;
            child4.label = NodeLabel.FULL;
            node = makeQNodeWithChildren(children);

            // Test 3 - test trimming pointer at end with deletion immediately before and at pointer
            node.trimAndFlattenQNodeChildren(child4, false);

            iter = node.children.iterator();
            assert iter.next() == child1;
            assert iter.next() == child2;
            assert !iter.hasNext();

            // Test 4 setup
            child1.label = NodeLabel.FULL;
            child2.label = NodeLabel.FULL;
            child3.label = NodeLabel.EMPTY;
            child4.label = NodeLabel.FULL;
            node = makeQNodeWithChildren(children);

            // Test 4 - test trimming pointer is null, with deletion in the middle somewhere
            node.trimAndFlattenQNodeChildren(null, false);

            iter = node.children.iterator();
            assert iter.next() == child1;
            assert iter.next() == child2;
            assert iter.next() == child4;
            assert !iter.hasNext();

            // Test 5 setup
            child1.label = NodeLabel.EMPTY;
            child2.label = NodeLabel.FULL;
            child3.label = NodeLabel.EMPTY;
            child4.label = NodeLabel.EMPTY;
            node = makeQNodeWithChildren(children);

            // Test 5 - reverse of test 1
            node.trimAndFlattenQNodeChildren(child3, true);

            iter = node.children.iterator();
            assert iter.next() == child4;
            assert iter.next() == child2;
            assert !iter.hasNext();

            // Test 6 setup
            child1.label = NodeLabel.FULL;
            child2.label = NodeLabel.FULL;
            child3.label = NodeLabel.FULL;
            child4.label = NodeLabel.EMPTY;
            node = makeQNodeWithChildren(children);

            // Test 6 - reverse of test 2
            node.trimAndFlattenQNodeChildren(child1, true);

            iter = node.children.iterator();
            assert iter.next() == child4;
            assert iter.next() == child1;
            assert !iter.hasNext();

            // Test 7 setup
            child1.label = NodeLabel.EMPTY;
            child2.label = NodeLabel.FULL;
            child3.label = NodeLabel.EMPTY;
            child4.label = NodeLabel.EMPTY;
            node = makeQNodeWithChildren(children);

            // Test 7 - reverse of test 3
            node.trimAndFlattenQNodeChildren(null, true);

            iter = node.children.iterator();
            assert iter.next() == child4;
            assert iter.next() == child3;
            assert iter.next() == child1;
            assert !iter.hasNext();

            // Test 8 setup
            child1.label = NodeLabel.FULL;
            child2.label = NodeLabel.EMPTY;
            child3.label = NodeLabel.FULL;
            child4.label = NodeLabel.FULL;
            node = makeQNodeWithChildren(children);

            // Test 8 - reverse of test 4
            node.trimAndFlattenQNodeChildren(child4, true);

            iter = node.children.iterator();
            assert iter.next() == child4;
            assert iter.next() == child3;
            assert iter.next() == child1;
            assert !iter.hasNext();

            // Setup for tests with partial nodes
            PQNode emptyChild = makeNode();
            emptyChild.label = NodeLabel.EMPTY;
            PQNode fullChild = makeNode();
            fullChild.label = NodeLabel.FULL;
            PQNode partialChild = null;
            PQNode gChild1 = makeNode();
            PQNode gChild2 = makeNode();
            PQNode gChild3 = makeNode();
            PQNode gChild4 = makeNode();
            gChild1.label = NodeLabel.EMPTY;
            gChild2.label = NodeLabel.EMPTY;
            gChild3.label = NodeLabel.FULL;
            gChild4.label = NodeLabel.FULL;
            PQNode[] topLevelChildren = {emptyChild, partialChild, fullChild};
            PQNode[] topLevelChildrenReverse = {fullChild, partialChild, emptyChild};
            PQNode[] secondLevelChildren = {gChild1, gChild2, gChild3, gChild4};

            // Test 9 setup
            partialChild = makeQNodeWithChildren(secondLevelChildren);
            partialChild.label = NodeLabel.PARTIAL;
            topLevelChildren[1] = partialChild;
            node = makeQNodeWithChildren(topLevelChildren);

            // Test 9 - test trimming pointer at partial node.
            node.trimAndFlattenQNodeChildren(partialChild, false);

            iter = node.children.iterator();
            assert iter.next() == emptyChild;
            assert iter.next() == gChild1;
            assert iter.next() == gChild2;
            assert iter.next() == gChild3;
            assert iter.next() == gChild4;
            assert iter.next() == fullChild;
            assert !iter.hasNext();

            // Test 10 setup
            partialChild = makeQNodeWithChildren(secondLevelChildren);
            partialChild.label = NodeLabel.PARTIAL;
            topLevelChildren[1] = partialChild;
            node = makeQNodeWithChildren(topLevelChildren);

            // Test 10 - test trimming pointer immediately before partial node.
            node.trimAndFlattenQNodeChildren(emptyChild, false);

            iter = node.children.iterator();
            assert iter.next() == emptyChild;
            PQNode next = iter.next();
            assert next == gChild3;
            assert iter.next() == gChild4;
            assert iter.next() == fullChild;
            assert !iter.hasNext();

            // Test 11 setup
            partialChild = makeQNodeWithChildren(secondLevelChildren);
            partialChild.label = NodeLabel.PARTIAL;
            topLevelChildren[1] = partialChild;
            node = makeQNodeWithChildren(topLevelChildren);

            // Test 11 - test trimming pointer immediately after partial node.
            node.trimAndFlattenQNodeChildren(fullChild, false);

            iter = node.children.iterator();
            assert iter.next() == emptyChild;
            assert iter.next() == gChild1;
            assert iter.next() == gChild2;
            assert !iter.hasNext();

            // Test 12 setup
            partialChild = makeQNodeWithChildren(secondLevelChildren);
            partialChild.label = NodeLabel.PARTIAL;
            topLevelChildrenReverse[1] = partialChild;
            node = makeQNodeWithChildren(topLevelChildrenReverse);

            // Test 12 - reverse of test 9.
            node.trimAndFlattenQNodeChildren(partialChild, true);

            iter = node.children.iterator();
            assert iter.next() == emptyChild;
            assert iter.next() == gChild1;
            assert iter.next() == gChild2;
            assert iter.next() == gChild3;
            assert iter.next() == gChild4;
            assert iter.next() == fullChild;
            assert !iter.hasNext();

            // Test 13 setup
            partialChild = makeQNodeWithChildren(secondLevelChildren);
            partialChild.label = NodeLabel.PARTIAL;
            topLevelChildrenReverse[1] = partialChild;
            node = makeQNodeWithChildren(topLevelChildrenReverse);

            // Test 13 - reverse of test 10.
            node.trimAndFlattenQNodeChildren(emptyChild, true);

            iter = node.children.iterator();
            assert iter.next() == gChild4;
            assert iter.next() == gChild3;
            assert iter.next() == fullChild;
            assert !iter.hasNext();

            // Test 14 setup
            partialChild = makeQNodeWithChildren(secondLevelChildren);
            partialChild.label = NodeLabel.PARTIAL;
            topLevelChildrenReverse[1] = partialChild;
            node = makeQNodeWithChildren(topLevelChildrenReverse);

            // Test 14 - reverse of test 11.
            node.trimAndFlattenQNodeChildren(fullChild, true);

            iter = node.children.iterator();
            assert iter.next() == emptyChild;
            assert iter.next() == gChild2;
            assert iter.next() == gChild1;
            assert iter.next() == fullChild;
            assert !iter.hasNext();

        }

        public void testTrimDoublyPartial() {
            Iterator<PQNode> iter;
            PQNode node;
            PQNode child1 = makeNode();
            PQNode child2 = makeNode();
            PQNode child3 = makeNode();
            PQNode child4 = makeNode();
            PQNode child5 = makeNode();
            PQNode[] children = {child1, child2, child3, child4, child5};

            // Test 1 setup
            child1.label = NodeLabel.EMPTY;
            child2.label = NodeLabel.FULL;
            child3.label = NodeLabel.EMPTY;
            child4.label = NodeLabel.FULL;
            child5.label = NodeLabel.EMPTY;
            node = makeQNodeWithChildren(children);

            // Test 1 - test trimming pointers so nodes inside are removed
            node.trimAndFlattenQNodeChildren(child2, child5);

            iter = node.children.iterator();
            assert iter.next() == child1;
            assert iter.next() == child2;
            assert iter.next() == child4;
            assert iter.next() == child5;
            assert !iter.hasNext();

            // Test 2 setup
            child1.label = NodeLabel.FULL;
            child2.label = NodeLabel.EMPTY;
            child3.label = NodeLabel.FULL;
            child4.label = NodeLabel.FULL;
            child5.label = NodeLabel.FULL;
            node = makeQNodeWithChildren(children);

            // Test 2 - test trimming pointers so nodes at and outside pointers are removed
            node.trimAndFlattenQNodeChildren(child2, child5);

            iter = node.children.iterator();
            assert iter.next() == child3;
            assert iter.next() == child4;
            assert !iter.hasNext();

            // Test 3 setup
            child1.label = NodeLabel.EMPTY;
            child2.label = NodeLabel.FULL;
            child3.label = NodeLabel.FULL;
            child4.label = NodeLabel.EMPTY;
            child5.label = NodeLabel.FULL;
            node = makeQNodeWithChildren(children);

            // Test 3 - test trimming pointers spanning whole node
            node.trimAndFlattenQNodeChildren(child1, null);

            iter = node.children.iterator();
            assert iter.next() == child2;
            assert iter.next() == child3;
            assert iter.next() == child5;
            assert !iter.hasNext();

            // Setup for tests with partial nodes
            PQNode emptyChild1 = makeNode();
            emptyChild1.label = NodeLabel.EMPTY;
            PQNode emptyChild2 = makeNode();
            emptyChild2.label = NodeLabel.EMPTY;
            PQNode fullChild = makeNode();
            fullChild.label = NodeLabel.FULL;
            PQNode partialChild1 = null;
            PQNode partialChild2 = null;
            PQNode g1Child1 = makeNode();
            PQNode g1Child2 = makeNode();
            PQNode g1Child3 = makeNode();
            PQNode g1Child4 = makeNode();
            PQNode g2Child1 = makeNode();
            PQNode g2Child2 = makeNode();
            PQNode g2Child3 = makeNode();
            PQNode g2Child4 = makeNode();
            g1Child1.label = NodeLabel.EMPTY;
            g1Child2.label = NodeLabel.EMPTY;
            g1Child3.label = NodeLabel.FULL;
            g1Child4.label = NodeLabel.FULL;
            g2Child1.label = NodeLabel.EMPTY;
            g2Child2.label = NodeLabel.EMPTY;
            g2Child3.label = NodeLabel.FULL;
            g2Child4.label = NodeLabel.FULL;
            PQNode[] topLevelChildren = {emptyChild1, partialChild1, fullChild, partialChild2, emptyChild2};
            PQNode[] secondLevelChildren1 = {g1Child1, g1Child2, g1Child3, g1Child4};
            PQNode[] secondLevelChildren2 = {g2Child1, g2Child2, g2Child3, g2Child4};

            // Test 4 setup
            partialChild1 = makeQNodeWithChildren(secondLevelChildren1);
            partialChild1.label = NodeLabel.PARTIAL;
            topLevelChildren[1] = partialChild1;
            partialChild2 = makeQNodeWithChildren(secondLevelChildren2);
            partialChild2.label = NodeLabel.PARTIAL;
            topLevelChildren[3] = partialChild2;
            node = makeQNodeWithChildren(topLevelChildren);

            // Test 4 - test trimming pointers at partial nodes.
            node.trimAndFlattenQNodeChildren(partialChild1, partialChild2);

            iter = node.children.iterator();
            assert iter.next() == emptyChild1;
            assert iter.next() == g1Child1;
            assert iter.next() == g1Child2;
            assert iter.next() == g1Child3;
            assert iter.next() == g1Child4;
            assert iter.next() == fullChild;
            assert iter.next() == g2Child4;
            assert iter.next() == g2Child3;
            assert iter.next() == g2Child2;
            assert iter.next() == g2Child1;
            assert iter.next() == emptyChild2;
            assert !iter.hasNext();

            // Test 5 setup
            partialChild1 = makeQNodeWithChildren(secondLevelChildren1);
            partialChild1.label = NodeLabel.PARTIAL;
            topLevelChildren[1] = partialChild1;
            partialChild2 = makeQNodeWithChildren(secondLevelChildren2);
            partialChild2.label = NodeLabel.PARTIAL;
            topLevelChildren[3] = partialChild2;
            node = makeQNodeWithChildren(topLevelChildren);

            // Test 5 - test trimming pointers around first partial node.
            node.trimAndFlattenQNodeChildren(emptyChild1, fullChild);

            iter = node.children.iterator();
            assert iter.next() == g1Child3;
            assert iter.next() == g1Child4;
            assert iter.next() == g2Child1;
            assert iter.next() == g2Child2;
            assert iter.next() == emptyChild2;
            assert !iter.hasNext();

            // Test 6 setup
            partialChild1 = makeQNodeWithChildren(secondLevelChildren1);
            partialChild1.label = NodeLabel.PARTIAL;
            topLevelChildren[1] = partialChild1;
            partialChild2 = makeQNodeWithChildren(secondLevelChildren2);
            partialChild2.label = NodeLabel.PARTIAL;
            topLevelChildren[3] = partialChild2;
            node = makeQNodeWithChildren(topLevelChildren);

            // Test 6 - test trimming pointers around second partial node.
            node.trimAndFlattenQNodeChildren(fullChild, emptyChild2);

            iter = node.children.iterator();
            assert iter.next() == emptyChild1;
            assert iter.next() == g1Child1;
            assert iter.next() == g1Child2;
            assert iter.next() == fullChild;
            assert iter.next() == g2Child3;
            assert iter.next() == g2Child4;
            assert iter.next() == emptyChild2;
            assert !iter.hasNext();

        }

    }

}
