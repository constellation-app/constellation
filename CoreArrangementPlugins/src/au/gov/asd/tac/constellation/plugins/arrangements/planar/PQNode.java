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

import java.util.EnumMap;
import java.util.HashSet;
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

    protected int getNumLeafDescendants() {
        return numLeafDescendants;
    }
    
    protected void setNumLeafDescendants(final int numLeafDescendants) {
        this.numLeafDescendants = numLeafDescendants;
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
                case EMPTY -> {
                    count += child.numLeafDescendants;
                    lastChange = child.numLeafDescendants;
                }
                case FULL -> {
                    count -= child.numLeafDescendants;
                    lastChange = -child.numLeafDescendants;
                }
                case PARTIAL -> {
                    for (PQNode grandchild : child.labeledChildren.get(NodeLabel.EMPTY)) {
                        count += grandchild.numLeafDescendants;
                        lastChange += grandchild.numLeafDescendants;
                    }
                    for (PQNode grandchild : child.labeledChildren.get(NodeLabel.FULL)) {
                        carry += grandchild.numLeafDescendants;
                        reverseCount += grandchild.numLeafDescendants;
                    }
                }
                default -> {
                    // Do nothing 
                }
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
                case EMPTY ->
                    carry = -child.numLeafDescendants;
                case FULL ->
                    carry = child.numLeafDescendants;
                case PARTIAL -> {
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
                }
                default -> {
                    // Do nothing 
                }
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
}
