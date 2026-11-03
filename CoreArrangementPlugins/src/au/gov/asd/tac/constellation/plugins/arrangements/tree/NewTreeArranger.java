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
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.api.map.primitive.MutableIntIntMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
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

    private final float nodeDistance;

    public NewTreeArranger() {
        nodeDistance = 10F;
    }

    public NewTreeArranger(final float nodeDistance) {
        this.nodeDistance = nodeDistance;
    }

    @Override
    public void setMaintainMean(final boolean b) {
        // Left Intentially Blank
    }

    @Override
    public void arrange(final GraphWriteMethods graph) throws InterruptedException {
        this.graph = graph;

        xAttr = VisualConcept.VertexAttribute.X.ensure(graph);
        yAttr = VisualConcept.VertexAttribute.Y.ensure(graph);
        zAttr = VisualConcept.VertexAttribute.Z.ensure(graph);

        final int vxCount = graph.getVertexCount();
        if (vxCount <= 0) {
            return;
        }

        final BitSet verticesToArrange = ArrangementUtilities.vertexBits(graph);

        final int rootVxId = findRootNodeId(verticesToArrange, vxCount);

        // Make a list of what nodes have what "children"
        final MutableIntObjectMap<List<VxInfo>> orderedChildren = new IntObjectHashMap<>();
        final BitSet onlyChildren = new BitSet();
        final BitSet vxsToGo = (BitSet) verticesToArrange.clone();
        vxsToGo.clear(rootVxId);

        orderChildren(rootVxId, vxsToGo, orderedChildren, onlyChildren);

        final MutableIntIntMap map = calcAllNodeRadii(verticesToArrange, vxCount, orderedChildren);
        System.out.println(map);

        calculateLayout(rootVxId, nodeDistance, orderedChildren, map);
    }

    // Finds the node that has the most neighbours plus the most neighbour's neighbours
    private int findRootNodeId(final BitSet verticesToArrange, final int vxCount) {
        int rootVxId = graph.getVertex(0);
        int bestValence = -1;

        for (int position = 0; position < vxCount; position++) {
            final int vxId = graph.getVertex(position);
            if (!verticesToArrange.get(vxId)) {
                continue;
            }

            // Find valence of all neighbours
            final int nxCount = graph.getVertexNeighbourCount(vxId);
            int neighbourValence = 0;
            for (int nxPos = 0; nxPos < nxCount; nxPos++) {
                final int nxId = graph.getVertexNeighbour(vxId, nxPos);
                neighbourValence += graph.getVertexNeighbourCount(nxId);
            }

            final int valence = nxCount + neighbourValence;
            if (valence > bestValence) {
                rootVxId = vxId;
                bestValence = valence;
            }
        }
        return rootVxId;
    }
    
    // Function that finds the node with the most neighbours
//    private int findRootNodeId(final BitSet verticesToArrange, final int vxCount) {
//        int rootVxId = graph.getVertex(0);
//        int bestValence = -1;
//
//        for (int position = 0; position < vxCount; position++) {
//            final int vxId = graph.getVertex(position);
//            if (!verticesToArrange.get(vxId)) {
//                continue;
//            }
//
//            final int valence = graph.getVertexNeighbourCount(vxId);
//            if (valence > bestValence) {
//                rootVxId = vxId;
//                bestValence = valence;
//            }
//        }
//        return rootVxId;
//    }

    // COPIED FROM CircTreeArranger
    /**
     * For each vertex, record its children in order of the number of their descendants.
     * <p>
     * Record result an AtomicQueue stored by parent in hash table. Returns number of children for vertex.
     *
     * Returns the number of children
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
        }

        if (children.size() == 1) {
            onlyChildren.set(children.iterator().next().vxId);
        }

        // Remove these children from consideration.
        removeChildren(vxsToGo, children);

        int result = 1;
        final List<VxInfo> numChildren = new ArrayList<>(); // TODO: rename
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

    /**
     * Executes the radial tree layout algorithm.
     *
     * @param root The center node of the tree hierarchy.
     * @param layerDistance The radial distance (radius step) between parent and child layers.
     */
    private void calculateLayout(final int root, final float layerDistance, final MutableIntObjectMap<List<VxInfo>> orderedChildren, final MutableIntIntMap map) {
        if (root == Graph.NOT_FOUND) {
            return;
        }

        // Set root position to origin
        graph.setFloatValue(xAttr, root, 0);
        graph.setFloatValue(yAttr, root, 0);
        graph.setFloatValue(zAttr, root, 0);

        // Old
        // Distribute children across 360 degrees
        //calculateNodePositions(root, 1, 0, 2 * Math.PI, layerDistance, orderedChildren);
        // New, currently has issues
//        final MutableIntList list = IntLists.mutable.empty();
//        list.add(root);
//        func(root, 0, 2 * Math.PI, layerDistance, orderedChildren, map, list, 0F);
        // balloon
        balloonArrange(root, orderedChildren, layerDistance);
    }

    private void calculateNodePositions(final int vxId, final int depth, final double startAngle, final double endAngle, final float layerDistance, final MutableIntObjectMap<List<VxInfo>> orderedChildren) {
        final List<VxInfo> children = orderedChildren.get(vxId);
        if (children == null || children.isEmpty()) {
            return;
        }

        final double totalParentLeaves = getLeafCount(vxId, orderedChildren);
        final double angleRange = endAngle - startAngle;
        final double radius = depth * layerDistance;

        double currentAngle = startAngle;

//        final MutableIntList leaves = getSingleLeaves(vxId, orderedChildren);
//
//        final float x = graph.getFloatValue(xAttr, vxId);
//        final float y = graph.getFloatValue(yAttr, vxId);
//
//        final int addedDepth = positionRing(leaves, x, y, null) * 2;
//        final int localDepth = depth + addedDepth; // todo better name
//        final double radius = localDepth * layerDistance;
        for (final VxInfo child : children) {
//            if (leaves.contains(child.vxId)) {
//                continue;
//            }
            // Allocate a slice of the angular wedge proportional to the child's leaf count
            final double childAngleWedge = (getLeafCount(child.vxId, orderedChildren) / totalParentLeaves) * angleRange;

            // Center the node within its angular slice
            final double nodeAngle = currentAngle + (childAngleWedge / 2.0);
            // Convert to Cartesian coordinates
            graph.setFloatValue(xAttr, child.vxId, (float) (radius * Math.cos(nodeAngle)));
            graph.setFloatValue(yAttr, child.vxId, (float) (radius * Math.sin(nodeAngle)));
            graph.setFloatValue(zAttr, child.vxId, 0);

            // Recursively layout the next subtree depth layer
            calculateNodePositions(child.vxId, depth + 1, currentAngle, currentAngle + childAngleWedge, layerDistance, orderedChildren);
            //calculateNodePositions(child.vxId, localDepth + 1, currentAngle, currentAngle + childAngleWedge, layerDistance, orderedChildren);

            // Advance the angle pointer for the next sibling
            currentAngle += childAngleWedge;
        }
    }

    private int positionRing(final MutableIntList leaves, final float x, final float y, final MutableIntList list, final float layerDistance, final float parentAngle) {
        // determine number of rings based on number of neighbours, only include leaf neighbours
        final int numLeaves = leaves.size();
        int remainingLeaves = numLeaves;

        int numRings = 0;
        while (remainingLeaves > 0) {
            numRings++;
            remainingLeaves -= nodesPerRing(numRings);
        }

        // resuign this to track leaves
        remainingLeaves = 0;

        for (int i = 1; i <= numRings; i++) {
            final int nodesPerRing = nodesPerRing(i);
            // If outermost ring, spread nodes evenly
            final boolean isOutermostRing = (i == numRings);
            final double denominator = isOutermostRing ? (numLeaves - remainingLeaves) : nodesPerRing;
            final double angleDelta = (2 * Math.PI) / denominator;
            final double radius = i * layerDistance;
            // Start positioning nodes in same driection parent was place, if outermost ring has an even number of nodes then one node will overlapp with parent trasnaction, so rotate slighlty
            double currentAngle = (isOutermostRing && (numLeaves - remainingLeaves) % 2 == 0) ? (parentAngle + angleDelta / 2) : parentAngle;
            for (int j = 0; j < nodesPerRing; j++) {
                if (remainingLeaves >= numLeaves) {
                    break;
                }

                final int vxId = leaves.get(remainingLeaves);
                remainingLeaves++;
                // Convert to Cartesian coordinates
                graph.setFloatValue(xAttr, vxId, (float) (radius * Math.cos(currentAngle) + x));
                graph.setFloatValue(yAttr, vxId, (float) (radius * Math.sin(currentAngle) + y));
                graph.setFloatValue(zAttr, vxId, 0);
                if (list != null) {
                    list.add(vxId);
                }
                currentAngle += angleDelta;
            }
        }

        return numRings;
    }

    private int nodesPerRing(final int ringLevel) {
        return 6 * ringLevel + 3;
    }

    private MutableIntList getSingleLeaves(final int vxId, final MutableIntObjectMap<List<VxInfo>> orderedChildren) {
        final List<VxInfo> children = orderedChildren.get(vxId);
        final MutableIntList leaves = new IntArrayList();
        if (children == null || children.isEmpty()) {
            return leaves;
        }

        for (final VxInfo child : children) {
            if (graph.getVertexNeighbourCount(child.vxId) == 1) {
                leaves.add(child.vxId);
            }
        }
        return leaves;
    }

    private MutableIntIntMap calcAllNodeRadii(final BitSet verticesToArrange, final int vxCount, final MutableIntObjectMap<List<VxInfo>> orderedChildren) {
        final MutableIntIntMap map = new IntIntHashMap();

        // TODO: maybe start with root, then iterate through ordered children, then all other nodes. Although I dont think this will fix the problem because root isnt alwasy most central
        for (int position = 0; position < vxCount; position++) {
            final int vxId = graph.getVertex(position);
            if (!verticesToArrange.get(vxId)) {
                continue;
            }
            calcNodeRadii(map, vxId, orderedChildren);
        }

        return map;
    }

    // New recursive function to get the radius of every node
    private int calcNodeRadii(final MutableIntIntMap map, final int vxId, final MutableIntObjectMap<List<VxInfo>> orderedChildren) {
        if (map.containsKey(vxId)) {
            return map.get(vxId);
        }
        // If leaf
        if (graph.getVertexNeighbourCount(vxId) == 1) {
            map.put(vxId, 1);
            return 1;
        }

        final int nonLeafRadius = calcNonLeafRadius(vxId, orderedChildren);

        // For each child
        final List<VxInfo> children = orderedChildren.get(vxId);
        int biggestRadius = 0;
        if (children != null) {
            for (final VxInfo child : children) {
                final int radius = calcNodeRadii(map, child.vxId, orderedChildren);
                if (radius > biggestRadius) {
                    biggestRadius = radius;
                }
            }
        }

        final int finalRadius = nonLeafRadius + biggestRadius;
        map.put(vxId, finalRadius);

        return map.get(vxId);
    }

    private int calcNonLeafRadius(final int vxId, final MutableIntObjectMap<List<VxInfo>> orderedChildren) {
        final MutableIntList leaves = getSingleLeaves(vxId, orderedChildren);
        final int numLeaves = leaves.size();
        int reaminingLeaves = numLeaves;

        int numRings = 0;
        while (reaminingLeaves > 0) {
            numRings++;
            reaminingLeaves -= nodesPerRing(numRings);
        }

        return numRings;
    }

    private void func(final int vxId, final double startAngle, final double endAngle, final float layerDistance, final MutableIntObjectMap<List<VxInfo>> orderedChildren, final MutableIntIntMap radiusMap, final MutableIntList list, final float parentAngle) {
        final List<VxInfo> children = orderedChildren.get(vxId);
        if (children == null || children.isEmpty()) {
            return;
        }

        final double totalParentLeaves = getLeafCount(vxId, orderedChildren);
        final double angleRange = endAngle - startAngle;

        double currentAngle = startAngle;
        final MutableIntList leaves = getSingleLeaves(vxId, orderedChildren);

        final float x = graph.getFloatValue(xAttr, vxId);
        final float y = graph.getFloatValue(yAttr, vxId);

        if (!leaves.isEmpty()) {
            positionRing(leaves, x, y, list, layerDistance, parentAngle);
        }

        final double parentRadius = radiusMap.get(vxId) * layerDistance;
        final double totalradius = calcAllChildRadius(children, radiusMap) * layerDistance;

        final int neighbourCOunt = graph.getVertexNeighbourCount(vxId);
        final int nonLeafNodeCount = neighbourCOunt - leaves.size();
        final double childAngleWedge = ((float) 1 / nonLeafNodeCount) * angleRange;
        // walk through our order children list, and place them on the radius that we've pre-calculated
        // if child is leaf, place them close by
        for (final VxInfo child : children) {
            if (leaves.contains(child.vxId)) {
                continue;
            }

            // Allocate a slice of the angular wedge proportional to the child's leaf count
            //final double childAngleWedge = (getLeafCount(child.vxId, orderedChildren) / totalParentLeaves) * angleRange; // I feel like this should be tied to radius now
//            final double childAngleWedge = ((float) radiusMap.get(child.vxId) / totalradius) * angleRange;
            System.out.println("vxId: " + vxId + " child.vxId: " + child.vxId + " radiusMap.get(child.vxId): " + radiusMap.get(child.vxId) + " parentRadius: " + parentRadius + " totalradius: " + totalradius + " childAngleWedge: " + childAngleWedge);

            // Center the node within its angular slice
            final double nodeAngle = currentAngle + (childAngleWedge / 2.0);

            // Convert to Cartesian coordinates
            if (!list.contains(child.vxId)) {
                graph.setFloatValue(xAttr, child.vxId, (float) (totalradius * Math.cos(nodeAngle) + x));
                graph.setFloatValue(yAttr, child.vxId, (float) (totalradius * Math.sin(nodeAngle) + y));
                graph.setFloatValue(zAttr, child.vxId, 0);
                list.add(child.vxId);
            }
            // Recursively layout the next subtree depth layer
            //func(child.vxId, currentAngle, currentAngle + childAngleWedge, layerDistance, orderedChildren, radiusMap, list, (float) nodeAngle);
            func(child.vxId, 0, 2 * Math.PI, layerDistance, orderedChildren, radiusMap, list, (float) nodeAngle); // testing what happens if i always give 360 degrees, not good, overlaps

            // Advance the angle pointer for the next sibling
            currentAngle += childAngleWedge;
        }
    }

    private int calcAllChildRadius(final List<VxInfo> children, final MutableIntIntMap radiusMap) {
        int sum = 0;
        for (final VxInfo child : children) {
            sum += radiusMap.get(child.vxId);
        }

        return sum;
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

    // NEW BALLOON TREE THING
    private static final double BALLOON_SCALE = 1.0;//0.4;
    private static final double RING_SCALE = 0.6;//0.4;

    // Simple Node Definition
    public static class Node {

        public int id;
        public List<Node> children = new ArrayList<>();

        // Calculated layout metrics
        public double x;
        public double y;
        public double radius;      // Bounding radius of the subtree
        public int descendantCount; // Total leaves/nodes under this subtree

        public Node(final int id) {
            this.id = id;
        }
    }

    /**
     * Executes the full Balloon Layout on the provided tree structure.
     */
    private void layout(final Node root, final double minEdgeLength, final double nodeRadius) {
        if (root == null) {
            return;
        }

        // Phase 1: Bottom-Up Pass (Calculate geometry sizing)
        calculateSubtreeSizes(root, minEdgeLength, nodeRadius);

        // Phase 2: Top-Down Pass (Position coordinates in a polar coordinate system)
        root.x = 0;
        root.y = 0;
        assignPositions(root, 0, 2 * Math.PI);
    }

    /**
     * Phase 1: Recursive Bottom-Up size allocation. The radius of a node's balloon boundary is directly proportional to
     * its structural weight.
     */
    private void calculateSubtreeSizes(final Node node, final double minEdgeLength, final double nodeRadius) {

        node.descendantCount = 1; // Count itself

        if (node.children.isEmpty()) {
            node.radius = nodeRadius;
            return;
        }

        double maxChildRadius = 0;
        for (final Node child : node.children) {
            calculateSubtreeSizes(child, minEdgeLength, nodeRadius);
            node.descendantCount += child.descendantCount;
            if (child.radius > maxChildRadius) {
                maxChildRadius = child.radius;
            }
        }

        // Formula: Subtree radius scales with weight, ensuring spacing bounds do not overlap
        node.radius = maxChildRadius + (minEdgeLength * BALLOON_SCALE) + (Math.log(node.descendantCount) * nodeRadius * BALLOON_SCALE);
    }

    /**
     * Phase 2: Recursive Top-Down position routing. Places the children on a local concentric ring with angular
     * distributions.
     */
    private void assignPositions(final Node parent, final double startAngle, final double endAngle) {
        if (parent.children.isEmpty()) {
            return;
        }

        int totalWeight = 0;
        for (final Node child : parent.children) {
            totalWeight += child.descendantCount;
        }

        // Distance from parent center to the center of the child ring
        final double ringRadius = parent.radius * RING_SCALE;
        double currentAngle = startAngle;
        final double totalWedgeRange = endAngle - startAngle;

        for (final Node child : parent.children) {
            // Allocate wedge width relative to child subtree significance
            final double childWedge = totalWedgeRange * ((double) child.descendantCount / totalWeight);
            final double midAngle = currentAngle + (childWedge / 2.0);

            // Compute Cartesian coordinates from Polar vectors
            child.x = parent.x + ringRadius * Math.cos(midAngle);
            child.y = parent.y + ringRadius * Math.sin(midAngle);

            // Recursively position sub-children within this node's isolated wedge boundary
            double nextStartAngle = midAngle - (childWedge / 2.0);
            double nextEndAngle = midAngle + (childWedge / 2.0);
            assignPositions(child, nextStartAngle, nextEndAngle);

            currentAngle += childWedge;
        }
    }

    // --- Demo Execution Code ---
    private void balloonArrange(final int rootId, final MutableIntObjectMap<List<VxInfo>> orderedChildren, final float layerDistance) {

        // Convert exisitng tree into format this thing understands
        final MutableIntObjectMap<Node> nodeMap = new IntObjectHashMap<>();

        // Make all nodes
        final int numNodes = graph.getVertexCount();
        for (int i = 0; i < numNodes; i++) {
            final int id = graph.getVertex(i);

            // if node doesnt exist in map, create
            final Node node;
            if (nodeMap.containsKey(id)) {
                node = nodeMap.get(id);
            } else {
                node = new Node(id);
                nodeMap.put(id, node);
            }

            if (orderedChildren.containsKey(id)) {
                final List<VxInfo> children = orderedChildren.get(id);

                // Add children to node
                for (final VxInfo child : children) {
                    // Create child if it doesnt already exist
                    final Node childNode;
                    if (nodeMap.containsKey(child.vxId)) {
                        childNode = nodeMap.get(child.vxId);
                    } else {
                        childNode = new Node(child.vxId);
                        nodeMap.put(child.vxId, childNode);

                    }

                    node.children.add(childNode);
                }
            }
        }

        final Node root = nodeMap.get(rootId);

        // Initialize and execute Layout Configuration
        layout(root, layerDistance, 10.0);
        //layout(root, layerDistance, 5.0);

        // Print final coordinates
        //System.out.printf("%-10s | %-8s | %-8s | %-8s\n", "Node ID", "Coord X", "Coord Y", "Hull Rad");
        //System.out.println("----------------------------------------------");
        printCoordinates(root);
    }

    private void printCoordinates(final Node node) {
        //System.out.printf("%-10s | %8.2f | %8.2f | %8.2f\n", node.id, node.x, node.y, node.radius);
        graph.setFloatValue(xAttr, node.id, (float) node.x);
        graph.setFloatValue(yAttr, node.id, (float) node.y);
        graph.setFloatValue(zAttr, node.id, 0);
        for (final Node child : node.children) {
            printCoordinates(child);
        }
    }
}
