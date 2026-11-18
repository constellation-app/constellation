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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;

/**
 * Arranges a graph into a radial tree layout
 *
 * @author Quasar985
 */
public class NewTreeArranger3 extends TreeArranger implements Arranger {

    private int xAttr;
    private int yAttr;
    private int zAttr;

    private final float nodeDistance;

    public NewTreeArranger3() {
        nodeDistance = 6F;
    }

    public NewTreeArranger3(final float nodeDistance) {
        this.nodeDistance = nodeDistance;
    }

    @Override
    public void setMaintainMean(final boolean b) {
        // Left intentionally blank
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

        calculateLayout(rootVxId, nodeDistance, orderedChildren);
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

    /**
     * Calculates the radial tree layout.
     *
     * First calculates the angular positions of all nodes. Then determines the required radius of each layer to
     * maintain minimum node separation.
     */
    private void calculateLayout(final int root, final float layerDistance, final MutableIntObjectMap<List<VxInfo>> orderedChildren) {
        if (root == Graph.NOT_FOUND) {
            return;
        }

        graph.setFloatValue(xAttr, root, 0);
        graph.setFloatValue(yAttr, root, 0);
        graph.setFloatValue(zAttr, root, 0);

        final List<List<NodePosition>> layers = new ArrayList<>();

        calculateNodeAngles(root, 1, 0, 2 * Math.PI, orderedChildren, layers);

        // Calculate each node's individual radial position.
        final Map<Integer, Double> radii = new HashMap<>();

        calculateAdaptiveRadii(root, 0, nodeDistance, layerDistance, orderedChildren, radii);

        // Assign coordinates.
        for (final List<NodePosition> layer : layers) {
            for (final NodePosition position : layer) {

                final double radius = radii.getOrDefault(position.vxId, 0.0);

                graph.setFloatValue(xAttr, position.vxId, (float) (radius * Math.cos(position.angle)));
                graph.setFloatValue(yAttr, position.vxId, (float) (radius * Math.sin(position.angle)));
                graph.setFloatValue(zAttr, position.vxId, 0);
            }
        }
    }

    /**
     * Calculates individual radial distances for every node.
     *
     * Each branch can expand independently instead of forcing all nodes at the same depth onto a common circular layer.
     */
    private void calculateAdaptiveRadii(
            final int vxId,
            final double parentRadius,
            final double nodeDistance,
            final double layerDistance,
            final MutableIntObjectMap<List<VxInfo>> orderedChildren,
            final Map<Integer, Double> radii) {

        radii.put(vxId, parentRadius);
        final List<VxInfo> children = orderedChildren.get(vxId);

        if (children == null || children.isEmpty()) {
            return;
        }

        final double minimumRadialSpacing = Math.max(0, nodeDistance * 0.75);
        final double normalSpacing = Math.max(0, layerDistance * 0.70);

        // Determine how much radial room each child needs.
        final List<BranchRequirement> requirements = new ArrayList<>();

        for (final VxInfo child : children) {
            final double angularRequirement = calculateBranchSpacing(child.vxId, orderedChildren);
            final double requiredSpacing = Math.max(minimumRadialSpacing, Math.max(normalSpacing, angularRequirement));

            requirements.add(new BranchRequirement(child.vxId, requiredSpacing));
        }

        // Sort branches by their required radial spacing.
        requirements.sort((a, b) -> Double.compare(a.spacing, b.spacing));

        // Position each branch independently.
        for (final BranchRequirement requirement : requirements) {
            final double childRadius = parentRadius + requirement.spacing;
            calculateAdaptiveRadii(requirement.vxId, childRadius, nodeDistance, layerDistance, orderedChildren, radii);
        }
    }

    /**
     * Estimates the radial spacing required by a branch based on the angular separation of its descendant nodes.
     */
    private double calculateBranchSpacing(
            final int vxId,
            final MutableIntObjectMap<List<VxInfo>> orderedChildren) {

        final List<VxInfo> children = orderedChildren.get(vxId);

        if (children == null || children.isEmpty()) {
            return 0;
        }

        final int leafCount
                = getLeafCount(vxId, orderedChildren);

        // Increase spacing gradually for larger subtrees.
        return Math.sqrt(leafCount) * nodeDistance * 0.35;
    }

    /**
     * Stores an individual branch's radial requirements.
     */
    private static class BranchRequirement {

        final int vxId;
        final double spacing;

        BranchRequirement(
                final int vxId,
                final double spacing) {

            this.vxId = vxId;
            this.spacing = spacing;
        }
    }

    /**
     * Calculates angular positions without assigning coordinates.
     *
     * Child subtrees retain their proportional angular allocation based on their number of descendant leaves.
     */
    private void calculateNodeAngles(final int vxId, final int depth, final double startAngle, final double endAngle, final MutableIntObjectMap<List<VxInfo>> orderedChildren, final List<List<NodePosition>> layers) {
        final List<VxInfo> children = orderedChildren.get(vxId);
        if (children == null || children.isEmpty()) {
            return;
        }

        final double totalParentLeaves = getLeafCount(vxId, orderedChildren);
        final double angleRange = endAngle - startAngle;

        double currentAngle = startAngle;
        for (final VxInfo child : children) {
            final double childLeafCount = getLeafCount(child.vxId, orderedChildren);

            final double childAngleWedge = (childLeafCount / totalParentLeaves) * angleRange;

            // Centre the node inside its allocated angular wedge.
            final double nodeAngle = currentAngle + (childAngleWedge / 2.0);

            // Ensure that the layer exists.
            while (layers.size() <= depth) {
                layers.add(new ArrayList<>());
            }

            layers.get(depth).add(new NodePosition(child.vxId, nodeAngle));

            // Recursively calculate the next depth.
            calculateNodeAngles(child.vxId, depth + 1, currentAngle, currentAngle + childAngleWedge, orderedChildren, layers);
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

    /**
     * Stores a vertex ID and its calculated angular position.
     */
    private static class NodePosition {

        final int vxId;
        final double angle;

        NodePosition(final int vxId, final double angle) {
            this.vxId = vxId;
            this.angle = angle;
        }
    }

}
