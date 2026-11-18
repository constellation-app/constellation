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
import java.util.List;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;

/**
 * Arranges a graph into a radial tree layout
 *
 * @author Quasar985
 */
public class NewTreeArranger2 extends TreeArranger implements Arranger {

    private int xAttr;
    private int yAttr;
    private int zAttr;

    private final float nodeDistance;

    public NewTreeArranger2() {
        nodeDistance = 6F;
    }

    public NewTreeArranger2(final float nodeDistance) {
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

        // Set root position to origin
        graph.setFloatValue(xAttr, root, 0);
        graph.setFloatValue(yAttr, root, 0);
        graph.setFloatValue(zAttr, root, 0);

        // Store angular positions grouped by depth.
        final List<List<NodePosition>> layers = new ArrayList<>();

        calculateNodeAngles(root, 1, 0, 2 * Math.PI, orderedChildren, layers);

        // Calculate the radius required by each layer.
        final double[] layerRadii = calculateLayerRadii(layers, layerDistance);

        // Finally assign the actual Cartesian coordinates.
        for (int depth = 1; depth < layers.size(); depth++) {
            final double radius = layerRadii[depth];

            for (final NodePosition position : layers.get(depth)) {
                graph.setFloatValue(xAttr, position.vxId, (float) (radius * Math.cos(position.angle)));
                graph.setFloatValue(yAttr, position.vxId, (float) (radius * Math.sin(position.angle)));
                graph.setFloatValue(zAttr, position.vxId, 0);
            }
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

    /**
     * Calculates compact radial distances for each depth layer.
     *
     * Reduces unnecessary expansion while retaining enough separation to keep the graph readable.
     */
    private double[] calculateLayerRadii(final List<List<NodePosition>> layers, final float layerDistance) {
        final double[] radii = new double[layers.size()];

        // Target separation between nodes on the same layer.
        final double minimumDistance = Math.max(0, nodeDistance);

        // Allow slightly tighter spacing than the original algorithm.
        final double targetDistance = minimumDistance * 0.85;

        // Radial separation between consecutive layers.
        final double radialSpacing = minimumDistance * 0.75;

        // Reduce the normal expansion of each layer.
        final double layerScale = 0.70;

        double previousRadius = 0;

        for (int depth = 1; depth < layers.size(); depth++) {
            final List<NodePosition> layer = layers.get(depth);

            if (layer.isEmpty()) {
                continue;
            }

            // Sort nodes into circular angular order.
            layer.sort((a, b) -> Double.compare(a.angle, b.angle));

            // Calculate the normal radius for this layer.
            final double normalRadius = depth * layerDistance * layerScale;

            // Ensure consecutive layers remain sufficiently separated.
            final double minimumRadius = previousRadius + radialSpacing;

            double requiredRadius = normalRadius;

            /*
         * Calculate the radius required to maintain
         * sufficient spacing between nodes on this layer.
             */
            if (layer.size() > 1 && targetDistance > 0) {
                double minimumAngleGap = Double.MAX_VALUE;

                for (int i = 0; i < layer.size(); i++) {
                    final double currentAngle = layer.get(i).angle;
                    final double nextAngle = (i + 1 < layer.size()) ? layer.get(i + 1).angle : layer.get(0).angle + 2 * Math.PI;
                    final double angleGap = nextAngle - currentAngle;

                    minimumAngleGap = Math.min(minimumAngleGap, angleGap);
                }

                // Calculate the radius needed for the closest pair.
                final double sinHalfGap = Math.sin(minimumAngleGap / 2.0);

                if (sinHalfGap > 0) {
                    final double spacingRadius = targetDistance / (2.0 * sinHalfGap);
                    requiredRadius = Math.max(requiredRadius, spacingRadius);
                }
            }

            // Use the smallest radius that satisfies our constraints.
            radii[depth] = Math.max(requiredRadius, minimumRadius);

            previousRadius = radii[depth];
        }

        return radii;
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
