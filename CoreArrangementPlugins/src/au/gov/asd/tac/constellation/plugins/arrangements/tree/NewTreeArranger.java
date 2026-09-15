/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.arrangements.tree;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.arrangements.Arranger;
import au.gov.asd.tac.constellation.plugins.arrangements.utilities.ArrangementUtilities;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;

/**
 *
 * @author Quasar985
 */
public class NewTreeArranger implements Arranger {

    private GraphWriteMethods graph;
    private int xAttr;
    private int yAttr;
    private int zAttr;

    final private float nodeDistance;

    enum SubTreeMode {
        NONE,
        DIM,
        HIDE
    }

    public NewTreeArranger() {
        nodeDistance = 10F;
    }

    public NewTreeArranger(final float nodeDistance) {
        this.nodeDistance = nodeDistance;
    }

    @Override
    public void setMaintainMean(final boolean b) {
        // Let Intentially blank
    }

    @Override
    public void arrange(final GraphWriteMethods graph) throws InterruptedException {
        System.out.println("new arrange");
        this.graph = graph;

        xAttr = VisualConcept.VertexAttribute.X.ensure(graph);
        yAttr = VisualConcept.VertexAttribute.Y.ensure(graph);
        zAttr = VisualConcept.VertexAttribute.Z.ensure(graph);

        final int vxCount = graph.getVertexCount();
        if (vxCount <= 0) {
            return;
        }

        final BitSet verticesToArrange = ArrangementUtilities.vertexBits(graph);

        final int rootVxId = findRootNodeId(verticesToArrange, vxCount); // TODO: try most central node instead
        //final int rootVxId = 28; // ONLY for strangetreearrangement.star, is the node thats most central (at least VETEX 28 is, idk if it has the same id)

        // Make a list of what nodes have what "children"
        final MutableIntObjectMap<List<VxInfo>> orderedChildren = new IntObjectHashMap<>();
        final BitSet onlyChildren = new BitSet();
        final BitSet vxsToGo = (BitSet) verticesToArrange.clone();
        vxsToGo.clear(rootVxId);

        orderChildren(rootVxId, vxsToGo, orderedChildren, onlyChildren);

        calculateLayout(rootVxId, nodeDistance, orderedChildren);
    }

    private int findRootNodeId(final BitSet verticesToArrange, final int vxCount) {
        int rootVxId = graph.getVertex(0);
        int bestValence = -1;
        for (int position = 0; position < vxCount; position++) {
            final int vxId = graph.getVertex(position);

            if (!verticesToArrange.get(vxId)) {
                continue;
            }

            final int valence = graph.getVertexNeighbourCount(vxId);
            if (valence > bestValence) {
                rootVxId = vxId;
                bestValence = valence;
            }
        }

        return rootVxId;
    }

    // COPIED FROM CircTreeArranger
    /**
     * For each vertex, record its children in order of the number of their descendants.
     * <p>
     * Record result an AtomicQueue stored by parent in hash table. Returns number of children for vertex.
     */
    private int orderChildren(final int vxId, final BitSet vxsToGo, final MutableIntObjectMap<List<VxInfo>> orderedChildren, final BitSet onlyChildren) {
        final List<VxInfo> children = new ArrayList<>();

        // For the specified vertex, get its children and record how many children they have.
        for (int position = 0; position < graph.getVertexNeighbourCount(vxId); position++) {
            final int nbId = graph.getVertexNeighbour(vxId, position);
            if (vxsToGo.get(nbId)) {
                children.add(new VxInfo(nbId, 0));
                vxsToGo.clear(nbId);
            }
        }

        if (children.isEmpty()) {
            return 1;
        } else if (children.size() == 1) {
            onlyChildren.set(children.iterator().next().vxId);
        }

        // Remove these children from consideration.
        removeChildren(vxsToGo, children);

        int result = 1;
        final List<VxInfo> numChildren = new ArrayList<>();
        for (final VxInfo vxInfo : children) {
            final int nChildren = orderChildren(vxInfo.vxId, vxsToGo, orderedChildren, onlyChildren);
            numChildren.add(new VxInfo(vxInfo.vxId, nChildren));
            result += nChildren;
        }

        Collections.sort(numChildren);
        orderedChildren.put(vxId, numChildren);

        return result;
    }

    // COPIED FROM CircTreeArranger
    /**
     * Remove the children from the set of vertex ids.
     *
     * @param vxs
     * @param children
     */
    private static void removeChildren(final BitSet vxs, final List<VxInfo> children) {
        for (final VxInfo vxInfo : children) {
            vxs.clear(vxInfo.vxId);
        }
    }

    // COPIED FROM CircTreeArranger
    /**
     * A vertex id and the number of children the vertex has.
     */
    private static class VxInfo implements Comparable<VxInfo> {

        final int vxId;
        final int nChildren;

        public VxInfo(final int vxId, final int nChildren) {
            this.vxId = vxId;
            this.nChildren = nChildren;
        }

        @Override
        public int compareTo(final VxInfo o) {
            return nChildren - o.nChildren;
        }

        @Override
        public String toString() {
            return String.format("VxInfo[vxId=%d,nChildren=%d]", vxId, nChildren);
        }
    }

    /**
     * Executes the radial tree layout algorithm.
     *
     * @param root The center node of the tree hierarchy.
     * @param layerDistance The radial distance (radius step) between parent and child layers.
     */
    private void calculateLayout(final int root, final float layerDistance, final MutableIntObjectMap<List<VxInfo>> orderedChildren) {
        if (root == Graph.NOT_FOUND) {
            return;
        }

        // Root is always placed precisely at the origin (0, 0)
        graph.setFloatValue(xAttr, root, 0);
        graph.setFloatValue(yAttr, root, 0);
        graph.setFloatValue(zAttr, root, 0);

        // Distribute children across the full 360-degree circle (0 to 2*PI)
        calculateNodePositions(root, 1, 0, 2 * Math.PI, layerDistance, orderedChildren);
    }

    private void calculateNodePositions(final int parent, final int depth, final double startAngle, final double endAngle, final float layerDistance, final MutableIntObjectMap<List<VxInfo>> orderedChildren) {
        final List<VxInfo> children = orderedChildren.get(parent);
        if (children == null || children.isEmpty()) {
            return;
        }

        final double totalParentLeaves = getLeafCount(parent, orderedChildren);
        final double angleRange = endAngle - startAngle;

        // The radius grows linearly based on the tree depth
        final double radius = depth * layerDistance;

        double currentAngle = startAngle;

        for (final VxInfo child : children) {
            // Allocate a slice of the angular wedge proportional to the child's leaf count
            final double childAngleWedge = (getLeafCount(child.vxId, orderedChildren) / totalParentLeaves) * angleRange;

            // Center the node within its dedicated angular slice
            final double nodeAngle = currentAngle + (childAngleWedge / 2.0);

            // Convert Polar coordinates (radius, angle) to Cartesian coordinates (x, y)
            graph.setFloatValue(xAttr, child.vxId, (float) (radius * Math.cos(nodeAngle)));
            graph.setFloatValue(yAttr, child.vxId, (float) (radius * Math.sin(nodeAngle)));
            graph.setFloatValue(zAttr, child.vxId, 0);

            // Recursively layout the next subtree depth layer
            calculateNodePositions(child.vxId, depth + 1, currentAngle, currentAngle + childAngleWedge, layerDistance, orderedChildren);

            // Advance the angle pointer for the next sibling
            currentAngle += childAngleWedge;
        }
    }

    private int getLeafCount(final int vxId, final MutableIntObjectMap<List<VxInfo>> orderedChildren) {
        final List<VxInfo> children = orderedChildren.get(vxId);
        if (children == null || children.isEmpty()) {
            return 1;
        }
        int count = 0;
        for (final VxInfo child : children) {
            count += getLeafCount(child.vxId, orderedChildren);
        }
        return count;
    }
}
