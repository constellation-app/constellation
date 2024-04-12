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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap;

import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.infomap.InfomapBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

/**
 * NodeBase
 *
 * @author algol
 */
public class NodeBase {

    private static final Logger LOGGER = Logger.getLogger(NodeBase.class.getName());

    private static int uid = 0;

    private final int id;
    private final String name;
    private int index;           // Temporary index used in finding best module.
    private int originalIndex;   // Index in the original network (for leaf nodes).

    private NodeBase parent;
    private NodeBase previous;   // Sibling.
    private NodeBase next;       // Sibling.
    private NodeBase firstChild;
    private NodeBase lastChild;
    private double codelength;

    protected SubStructure subStructure;
    protected int childDegree;
    protected boolean childrenChanged;
    protected int numLeafMembers;
    protected ArrayList<Edge<NodeBase>> inEdges;
    protected ArrayList<Edge<NodeBase>> outEdges;

    public NodeBase() {
        this(StringUtils.EMPTY);
    }

    public NodeBase(final String name) {
        this.id = uid++;
        this.name = name;
        index = 0;
        codelength = 0;
        subStructure = new SubStructure();
        childDegree = 0;
        childrenChanged = false;
        numLeafMembers = 1;
        inEdges = new ArrayList<>();
        outEdges = new ArrayList<>();

        originalIndex = -1;
    }

    public static int uid() {
        return uid;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(final int index) {
        this.index = index;
    }

    public int getOriginalIndex() {
        return originalIndex;
    }

    public void setOriginalIndex(final int originalIndex) {
        this.originalIndex = originalIndex;
    }

    public NodeBase getParent() {
        return parent;
    }

    public void setParent(final NodeBase parent) {
        this.parent = parent;
    }

    public NodeBase getFirstChild() {
        return firstChild;
    }

    public void setFirstChild(final NodeBase firstChild) {
        this.firstChild = firstChild;
    }

    public double getCodelength() {
        return codelength;
    }

    public void setCodelength(final double codelength) {
        this.codelength = codelength;
    }

    public void addChild(final NodeBase child) {
        if (firstChild == null) {
            child.previous = null;
            firstChild = child;
        } else {
            child.previous = lastChild;
            lastChild.next = child;
        }

        lastChild = child;
        child.next = null;
        child.parent = this;
        childDegree++;
    }

    public void releaseChildren() {
        firstChild = null;
        lastChild = null;
        childDegree = 0;
    }

    public Edge<NodeBase> addOutEdge(final NodeBase target, final double weight, final double flow) {
        final Edge<NodeBase> edge = new Edge<>(this, target, weight, flow);
        outEdges.add(edge);
        target.inEdges.add(edge);

        return edge;
    }

    public void replaceChildrenWithGrandChildren() {
        if (firstChild == null) {
            return;
        }

        final Iterator<NodeBase> nodeIter = getChildren().iterator();
        int nOriginalChildrenLeft = childDegree;
        do {
            final NodeBase node = nodeIter.next();
            node.replaceWithChildren();
        } while (--nOriginalChildrenLeft != 0);
    }

    public void replaceWithChildren() {
        if (isLeaf() || isRoot()) {
            return;
        }

        // Reparent children.
        int deltaChildDegree = 0;
        NodeBase child = firstChild;
        do {
            child.parent = parent;
            child = child.next;
            deltaChildDegree++;
        } while (child != null);
        parent.childDegree += deltaChildDegree - 1; // -1 as this node is deleted.

        if (parent.firstChild == this) {
            parent.firstChild = firstChild;
        } else {
            previous.next = firstChild;
            firstChild.previous = previous;
        }

        if (parent.lastChild == this) {
            parent.lastChild = lastChild;
        } else {
            next.previous = lastChild;
            lastChild.next = next;
        }

        // Release connected nodes before delete, otherwise children are deleted and neighbours are reconnected.
        firstChild = null;
        next = null;
        previous = null;
        parent = null;
    }

    public boolean isRoot() {
        return parent == null;
    }

    public boolean isLeaf() {
        return firstChild == null;
    }

    public boolean isDangling() {
        return outEdges.isEmpty();
    }

    public int getOutDegree() {
        return outEdges.size();
    }

    public int getInDegree() {
        return inEdges.size();
    }

    public int getDegree() {
        return getOutDegree() + getInDegree();
    }

    public int getChildDegree() {
        return childDegree;
    }

    public void setChildDegree(final int value) {
        childDegree = value;
        childrenChanged = false;
    }

    public InfomapBase getSubInfomap() {
        return subStructure.getSubInfomap();
    }

    public SubStructure getSubStructure() {
        return subStructure;
    }

    public int getNumLeafNodes() {
        return numLeafMembers;
    }

    public void setNumLeafNodes(final int value) {
        numLeafMembers = value;
    }

    public List<Edge<NodeBase>> getOutEdges() {
        return Collections.unmodifiableList(outEdges);
    }

    public List<Edge<NodeBase>> getInEdges() {
        return Collections.unmodifiableList(inEdges);
    }

    public Iterable<NodeBase> getChildren() {
        return () -> new ChildrenIterator(NodeBase.this);
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NodeBase other = (NodeBase) obj;

        return this.id == other.id;
    }

    public void dumpEdges() {
        final String formattedString = String.format("id %d; index %d; out> %d; in< %d\n", id, index, outEdges.size(), inEdges.size());
        LOGGER.log(Level.INFO, formattedString);
    }

    /**
     * Debug.
     *
     * @return
     */
    private static int getNodeBaseid(final NodeBase n) {
        return n == null ? -1 : n.id;
    }

    protected String getNeighbourhood() {
        return String.format("parent: %d, prev: %d, next: %d, firstChild: %d, lastChild: %d, childDegree: %d",
                getNodeBaseid(parent), getNodeBaseid(previous), getNodeBaseid(next), getNodeBaseid(firstChild), getNodeBaseid(lastChild), childDegree);
    }

    @Override
    public String toString() {
        if (true) {
            return String.format("[NodeBase %d index=%d original=%d] %s", id, index, originalIndex, getNeighbourhood());
        }

        return String.format("[NodeBase %d:%s index=%d] %s", id, name, index, getNeighbourhood());
    }

    public static class ChildrenIterator implements Iterator<NodeBase> {

        private NodeBase current;

        public ChildrenIterator(final NodeBase node) {
            current = node.firstChild;
        }

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public NodeBase next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            final NodeBase node = current;
            current = current.next;

            return node;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }
    }
}
