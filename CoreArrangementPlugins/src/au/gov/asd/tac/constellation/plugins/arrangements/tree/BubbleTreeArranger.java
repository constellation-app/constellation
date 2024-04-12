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
package au.gov.asd.tac.constellation.plugins.arrangements.tree;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.algorithms.tree.SpanningTree;
import au.gov.asd.tac.constellation.plugins.arrangements.Arranger;
import au.gov.asd.tac.constellation.plugins.arrangements.utilities.ArrangementUtilities;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3d;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

/**
 *
 * @author algol
 */
public class BubbleTreeArranger implements Arranger {

    private final Set<Integer> roots;
    private final boolean isMinimal;
    private final boolean nAlgo;
    private GraphWriteMethods tree;
    private boolean maintainMean;

    private int xId;
    private int yId;
    private int zId;
    private int nradiusId;

    private int[] vxDepth;

    public BubbleTreeArranger(final Set<Integer> roots, final boolean isMinimal) {
        this.roots = roots;
        this.isMinimal = isMinimal;
        nAlgo = true;
        maintainMean = false;
    }

    /**
     * Causes the arrangement to occur.
     *
     * @param wg the graph to be arranged.
     *
     * @throws InterruptedException if the thread is interrupted because the
     * arrangement has been canceled.
     */
    @Override
    public void arrange(final GraphWriteMethods wg) throws InterruptedException {
        final float[] oldMean = maintainMean ? ArrangementUtilities.getXyzMean(wg) : null;

        // Find a root.
        int root = Graph.NOT_FOUND;
        final int wgVxCount = wg.getVertexCount();
        for (int position = 0; position < wgVxCount; position++) {
            final int vxId = wg.getVertex(position);
            if (roots.contains(vxId)) {
                root = vxId;
                break;
            }
        }

        if (root == Graph.NOT_FOUND && wgVxCount > 0) {
            // The user didn't specify a root in this component, so we'll just pick one.
            root = wg.getVertex(0);
        }

        final int rootVxId = root;

        final SpanningTree st = new SpanningTree(wg);

        tree = st.createSpanningTree(isMinimal, false, rootVxId);

        tree.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "x", "x", 0, null);
        tree.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "y", "y", 0, null);
        tree.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "z", "z", 0, null);

        xId = tree.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName());
        yId = tree.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName());
        zId = tree.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z.getName());
        nradiusId = tree.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.NODE_RADIUS.getName());

        vxDepth = new int[tree.getVertexCapacity()];

        // Lay out the tree graph.
        final double[][] relativePositions = new double[tree.getVertexCapacity()][];
        final int treeRootVxId = st.convertOrigVxToTree(rootVxId);
        computeRelativePosition(treeRootVxId, relativePositions, 1);
        calcLayout(treeRootVxId, relativePositions);

        // Copy the x,y,z values from the arranged spanning tree back to the original graph.
        float minx = Float.MAX_VALUE;
        float miny = Float.MAX_VALUE;
        float maxx = -Float.MAX_VALUE;
        float maxy = -Float.MAX_VALUE;
        float maxRadius = 0;
        int maxDepth = 1;
        final int wgxId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName());
        final int wgyId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName());
        final int wgzId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z.getName());
        final int wgx2Id = wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "x2", "x2", 0, null);
        final int wgy2Id = wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "y2", "y2", 0, null);
        final int wgz2Id = wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "z2", "z2", 0, null);
        final int vxCount = tree.getVertexCount();
        for (int position = 0; position < vxCount; position++) {
            // Make the layout 3D in x,y,z, 2D in x2,y2,z2.
            final int treeVxId = tree.getVertex(position);
            final int vxId = st.convertTreeVxToOrig(treeVxId);

            final float x = tree.getFloatValue(xId, treeVxId);
            final float y = tree.getFloatValue(yId, treeVxId);
            final float z = tree.getFloatValue(zId, treeVxId);
            final float nradius = nradiusId != Graph.NOT_FOUND ? tree.getFloatValue(nradiusId, treeVxId) : 1;

            wg.setFloatValue(wgxId, vxId, x);
            wg.setFloatValue(wgyId, vxId, y);

            wg.setFloatValue(wgx2Id, vxId, x);
            wg.setFloatValue(wgy2Id, vxId, y);
            wg.setFloatValue(wgz2Id, vxId, z);

            minx = Math.min(x, minx);
            maxx = Math.max(x, maxx);
            miny = Math.min(y, miny);
            maxy = Math.max(y, maxy);
            maxDepth = Math.max(vxDepth[treeVxId], maxDepth);
            maxRadius = Math.max(nradius, maxRadius);
        }

        // Add depth to the alternate layout.
        // Make the height half of the breadth, and at least high enough for the largest vertex.
        final float breadth = Math.max(maxx - minx, maxy - miny);
        final float layerHeight = Math.max(breadth / 2 / maxDepth, maxRadius * 2 * (float) Math.sqrt(2));

        for (int position = 0; position < vxCount; position++) {
            final int treeVxId = tree.getVertex(position);
            final int vxId = st.convertTreeVxToOrig(treeVxId);

            final float z = -vxDepth[treeVxId] * layerHeight;
            wg.setFloatValue(wgzId, vxId, z);
        }

        if (maintainMean) {
            ArrangementUtilities.moveMean(wg, oldMean);
        }
    }

    /**
     * Determine the relative positions of a vertex.
     *
     * @param vxId
     * @param relativePositions An array of double[5].
     * @param depth
     * @return
     */
    private double computeRelativePosition(final int vxId, final double[][] relativePositions, final int depth) {
        vxDepth[vxId] = depth;
        final float nradius = nradiusId != Graph.NOT_FOUND ? tree.getFloatValue(nradiusId, vxId) : 1;

        double sizeFather = Math.hypot(nradius, nradius);
        if (sizeFather < 1e-5) {
            sizeFather = Math.sqrt(2);
        }

        float sizeVirtualVx = 1;
        if (tree.getVertexTransactionCount(vxId, Graph.INCOMING) == 0) {
            sizeVirtualVx = 0;
        }

        // Initialise vertex position.
        if (relativePositions[vxId] == null) {
            relativePositions[vxId] = new double[5];
        }

        relativePositions[vxId][0] = 0;
        relativePositions[vxId][1] = 0;

        // Special case if the vertex is a leaf.
        if (tree.getVertexTransactionCount(vxId, Graph.OUTGOING) == 0) {
            relativePositions[vxId][2] = 0;
            relativePositions[vxId][3] = 0;
            relativePositions[vxId][4] = sizeFather;

            return relativePositions[vxId][4];
        }

        // Recursive call to obtain the set of radius of the children of vxId.
        // A virtual node is dynamically inserted in the neighbourhood of vxId in order to
        // reserve space for the connection of the father of vxId.
        final int nc = tree.getVertexTransactionCount(vxId, Graph.OUTGOING) + 1;
        final double[] angularSector = new double[nc];
        final double[] realCircleRadius = new double[nc];
        realCircleRadius[0] = sizeVirtualVx;
        double sumRadius = sizeVirtualVx;
        for (int position = 0; position < nc - 1; position++) {
            final int txId = tree.getVertexTransaction(vxId, Graph.OUTGOING, position);

            final int dstVxId = tree.getTransactionDestinationVertex(txId);
            realCircleRadius[position + 1] = computeRelativePosition(dstVxId, relativePositions, depth + 1);
            sumRadius += realCircleRadius[position + 1];
        }

        double resolution = 0;
        if (nAlgo) {
            final double[] subCircleRadius = new double[nc];
            subCircleRadius[0] = realCircleRadius[0];
            double maxRadius = 0;
            int maxRadiusIndex = 0;
            for (int i = 0; i < nc; i++) {
                subCircleRadius[i] = realCircleRadius[i];
                if (maxRadius < subCircleRadius[i]) {
                    maxRadius = subCircleRadius[i];
                    maxRadiusIndex = i;
                }
            }

            if (maxRadius > (sumRadius / 2)) {
                final double ratio = (sumRadius - maxRadius) > 1e-5 ? maxRadius / (sumRadius - maxRadius) : 1;
                for (int i = 0; i < nc; i++) {
                    if (i != maxRadiusIndex) {
                        subCircleRadius[i] *= ratio;
                    }
                }

                sumRadius += 2F * maxRadius;
            }

            for (int i = 0; i < nc; i++) {
                angularSector[i] = 2 * Math.PI * subCircleRadius[i] / sumRadius;
            }
        } else {
            resolution = 2 * Math.PI;
            final Integer[] index = new Integer[nc];
            for (int i = 0; i < nc; i++) {
                index[i] = i;
            }

            Arrays.sort(index, (lhsIx, rhsIx) -> {
                final double lhs = realCircleRadius[lhsIx];
                final double rhs = realCircleRadius[rhsIx];

                if (lhs < rhs) {
                    return 1;
                } else if (lhs > rhs) {
                    return -1;
                } else {
                    return 0;
                    // Do nothing
                }
            });

            int i = 0;
            for (; i < index.length; i++) {
                final double radius = realCircleRadius[index[i]];
                final double angleMax = 2 * Math.asin(radius / (radius + sizeFather));
                final double angle = radius * resolution / sumRadius;

                if (angle > angleMax) {
                    angularSector[index[i]] = angleMax;
                    sumRadius -= radius;
                    resolution -= angleMax;
                } else {
                    break;
                }
            }

            if (i < index.length) {
                for (; i < index.length; i++) {
                    final double radius = realCircleRadius[index[i]];
                    final double angle = radius * resolution / sumRadius;
                    angularSector[index[i]] = angle;
                }

                resolution = 0;
            } else {
                resolution /= nc;
            }
        }

        double angle = 0;
        final ArrayList<BoundingCircle> circles = new ArrayList<>(nc);
        for (int i = 0; i < nc; i++) {
            circles.add(new BoundingCircle());
        }

        for (int i = 0; i < nc; i++) {
            double packRadius;
            if (Math.abs(Math.sin(angularSector[i])) > 1e-5) {
                packRadius = realCircleRadius[i] / Math.sin(angularSector[i] / 2);
            } else {
                packRadius = 0;
            }

            packRadius = Math.max(packRadius, sizeFather + realCircleRadius[i]);

            if (i > 0) {
                angle += (angularSector[i - 1] + angularSector[i]) / 2 + resolution;
            }

            final BoundingCircle circle = circles.get(i);
            circle.setX(packRadius * Math.cos(angle));
            circle.setY(packRadius * Math.sin(angle));
            circle.setRadius(realCircleRadius[i]);
        }

        final BoundingCircle circleH = BoundingCircle.enclosingCircle(circles);
        final double[] relpos = relativePositions[vxId];
        relpos[2] = -circleH.getX();
        relpos[3] = -circleH.getY();
        relpos[4] = Math.sqrt(circleH.getRadius() * circleH.getRadius() - circleH.getY() * circleH.getY()) - Math.abs(circleH.getX());

        // Set relative position of all children
        // according to the centre of the enclosing circle.
        final int nOut = tree.getVertexTransactionCount(vxId, Graph.OUTGOING);
        for (int position = 0; position < nOut; position++) {
            final int txId = tree.getVertexTransaction(vxId, Graph.OUTGOING, position);

            final int outVxId = tree.getTransactionDestinationVertex(txId);
            final double[] outrelpos = relativePositions[outVxId];
            outrelpos[0] = circles.get(position + 1).getX() - circleH.getX();
            outrelpos[1] = circles.get(position + 1).getY() - circleH.getY();
        }

        return circleH.getRadius();
    }

    private void calcLayout2(final int vxId, final double[][] relativePositions, final Vector3d enclosingCircleCentre, final Vector3d originalNodePosition) {
        // Make rotation around the centre of the enclosing circle in order to align
        // the virtual vertex, the enclosing circle's centre, and the grandfather of the vertex.
        final Vector3d bend = new Vector3d(relativePositions[vxId][4], 0, 0);
        final Vector3d zeta = new Vector3d(relativePositions[vxId][2], relativePositions[vxId][3], 0);

        final Vector3d vect = Vector3d.subtract(originalNodePosition, enclosingCircleCentre);
        vect.normalize();
        final Vector3d vect3 = Vector3d.add(zeta, bend);
        vect3.normalize();

        final double cosAlpha = Vector3d.dotProduct(vect3, vect);
        final double sinAlpha = Vector3d.crossProduct(vect, vect3).getZ();

        final Vector3d rot1 = new Vector3d(cosAlpha, -sinAlpha, 0);
        final Vector3d rot2 = new Vector3d(sinAlpha, cosAlpha, 0);

        zeta.set(Vector3d.add(Vector3d.multiply(rot1, zeta.getX()), Vector3d.multiply(rot2, zeta.getY())));

        final float x = (float) (enclosingCircleCentre.getX() + zeta.getX());
        final float y = (float) (enclosingCircleCentre.getY() + zeta.getY());
        tree.setFloatValue(xId, vxId, x);
        tree.setFloatValue(yId, vxId, y);
        tree.setFloatValue(zId, vxId, 0);

        // If we could add a bend to the transaction, we'd do it here.
        // Make the recursive call to place the children of vxId.
        final int nOut = tree.getVertexTransactionCount(vxId, Graph.OUTGOING);
        for (int position = 0; position < nOut; position++) {
            final int txId = tree.getVertexTransaction(vxId, Graph.OUTGOING, position);
            final int outVxId = tree.getTransactionDestinationVertex(txId);

            Vector3d newpos = new Vector3d(relativePositions[outVxId][0], relativePositions[outVxId][1], 0);
            newpos = Vector3d.add(Vector3d.multiply(rot1, newpos.getX()), Vector3d.multiply(rot2, newpos.getY()));
            newpos.add(enclosingCircleCentre);
            calcLayout2(outVxId, relativePositions, newpos, Vector3d.add(enclosingCircleCentre, zeta));
        }
    }

    private void calcLayout(final int vxId, final double[][] relativePositions) {
        // Make the recursive call to place the children of vxId.
        final int nOut = tree.getVertexTransactionCount(vxId, Graph.OUTGOING);
        for (int position = 0; position < nOut; position++) {
            final int txId = tree.getVertexTransaction(vxId, Graph.OUTGOING, position);
            final int dstVxId = tree.getTransactionDestinationVertex(txId);

            final double d0 = relativePositions[dstVxId][0] - relativePositions[vxId][2];
            final double d1 = relativePositions[dstVxId][1] - relativePositions[vxId][3];

            final Vector3d origin = new Vector3d(d0, d1, 0);
            final Vector3d tmp = new Vector3d();

            calcLayout2(vxId, relativePositions, origin, tmp);
        }
    }

    @Override
    public void setMaintainMean(final boolean b) {
        maintainMean = b;
    }
}
