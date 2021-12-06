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
package au.gov.asd.tac.constellation.plugins.arrangements.tree;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.arrangements.Arranger;
import au.gov.asd.tac.constellation.plugins.arrangements.utilities.ArrangementUtilities;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import org.apache.commons.collections4.CollectionUtils;

/**
 * This class provides the arrangement of a single tree (undirected), drawn
 * radially from its root.
 *
 * The algorithm will work, if the graph is more than a tree, by ignoring
 * additional edges. The root may be specified; if not specified, it is chosen
 * as one of the maximum valence.
 *
 * @author algol
 * @author sol
 *
 */
public final class CircTreeArranger implements Arranger {
    // Vertex radii are measured in square sides, visible radii are measured in circle radii.

    private static final int MAX_IN_ONE_CIRCLE = 16;
    private static final float TWO_PI = (float) (2 * Math.PI);
    private GraphWriteMethods graph;
    private final CircTreeChoiceParameters params;
    private int xAttr;
    private int yAttr;
    private int zAttr;
    private int radiusAttr;
    private boolean maintainMean;

    /**
     * Construct a new ArrangeInCircTree instance.
     *
     * @param params Parameters.
     */
    public CircTreeArranger(final CircTreeChoiceParameters params) {
        this.params = params;
        maintainMean = false;
    }

    @Override
    public void setMaintainMean(final boolean b) {
        maintainMean = b;
    }

    @Override
    public void arrange(final GraphWriteMethods graph) throws InterruptedException {
        this.graph = graph;

        // x, y, z
        if (VisualConcept.VertexAttribute.X.get(graph) == Graph.NOT_FOUND) {
            graph.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "x", "x", null, null);
        }
        if (VisualConcept.VertexAttribute.Y.get(graph) == Graph.NOT_FOUND) {
            graph.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "y", "y", null, null);
        }
        if (VisualConcept.VertexAttribute.Z.get(graph) == Graph.NOT_FOUND) {
            graph.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "z", "z", null, null);
        }
        xAttr = VisualConcept.VertexAttribute.X.get(graph);
        yAttr = VisualConcept.VertexAttribute.Y.get(graph);
        zAttr = VisualConcept.VertexAttribute.Z.get(graph);
        radiusAttr = VisualConcept.VertexAttribute.LABEL_RADIUS.get(graph);

        final int vxCount = graph.getVertexCount();
        if (vxCount > 0) {
            final BitSet verticesToArrange = ArrangementUtilities.vertexBits(graph);

            int rootVxId = params.rootVxId;
            if (rootVxId == Graph.NOT_FOUND) {
                int bestValence = -1;
                for (int position = 0; position < vxCount; position++) {
                    final int vxId = graph.getVertex(position);

                    if (verticesToArrange.get(vxId)) {
                        final int valence = graph.getVertexNeighbourCount(vxId);
                        if (valence > bestValence) {
                            rootVxId = vxId;
                            bestValence = valence;
                        }
                    }
                }
            }

            final float[] oldCentre = maintainMean ? ArrangementUtilities.getXyzMean(graph) : null;

            // Gather the vxIds into a BitSet for faster checking.
            BitSet vxsToGo = (BitSet) verticesToArrange.clone();
            vxsToGo.clear(rootVxId);

            // Map vxIds to their ordered children (vxId and nChildren).
            HashMap<Integer, ArrayList<VxInfo>> orderedChildren = new HashMap<>();
            BitSet onlyChildren = new BitSet();

            orderChildren(rootVxId, vxsToGo, orderedChildren, onlyChildren);

            // Find spacings.
            vxsToGo = (BitSet) verticesToArrange.clone();
            vxsToGo.clear(rootVxId);
            final float[] childrenRadii = new float[graph.getVertexCapacity()];
            final float[] fullRadii = new float[graph.getVertexCapacity()];
            final AnnulusInfo[] annulusInfo = new AnnulusInfo[graph.getVertexCapacity()];

            findSpacingOf(rootVxId, vxsToGo, orderedChildren, onlyChildren, params.scale, params.strictCircularLayout, childrenRadii, fullRadii, annulusInfo);

            // Do the arrangement.
            vxsToGo = (BitSet) verticesToArrange.clone();
            vxsToGo.clear(rootVxId);

            final float ourLocX = 0;
            final float ourLocY = 0;

            positionThis(rootVxId, vxsToGo, orderedChildren, ourLocX, ourLocY, 0, 0, params.strictCircularLayout, childrenRadii, fullRadii, annulusInfo, 0);

            if (maintainMean) {
                final float[] newCentre = ArrangementUtilities.getXyzMean(graph);
                for (int vxId = verticesToArrange.nextSetBit(0); vxId >= 0; vxId = verticesToArrange.nextSetBit(vxId + 1)) {
                    graph.setFloatValue(xAttr, vxId, graph.getFloatValue(xAttr, vxId) - newCentre[0] + oldCentre[0]);
                    graph.setFloatValue(yAttr, vxId, graph.getFloatValue(yAttr, vxId) - newCentre[1] + oldCentre[1]);
                }
            }
        }
    }

    /**
     * Remove the children from the set of vertex ids.
     *
     * @param vxs
     * @param children
     */
    private static void removeChildren(final BitSet vxs, final ArrayList<VxInfo> children) {
        for (VxInfo vxInfo : children) {
            vxs.clear(vxInfo.vxId);
        }
    }

    /**
     * For each vertex, record its children in order of the number of their
     * descendants.
     * <p>
     * Record result an AtomicQueue stored by parent in hash table. Returns
     * number of children for vertex.
     */
    private int orderChildren(
            final int vxId,
            final BitSet vxsToGo,
            final HashMap<Integer, ArrayList<VxInfo>> orderedChildren,
            final BitSet onlyChildren) {
        final ArrayList<VxInfo> children = new ArrayList<>();

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
        } else {
            // Do nothing
        }

        // Remove these children from consideration.
        removeChildren(vxsToGo, children);

        int result = 1;
        final ArrayList<VxInfo> numChildren = new ArrayList<>();
        for (VxInfo vxInfo : children) {
            final int nChildren = orderChildren(vxInfo.vxId, vxsToGo, orderedChildren, onlyChildren);
            numChildren.add(new VxInfo(vxInfo.vxId, nChildren));
            result += nChildren;
        }

        Collections.sort(numChildren);
        orderedChildren.put(vxId, numChildren);

        return result;
    }

    private float findSpacingOf(
            final int vxId,
            final BitSet vxsToGo,
            final HashMap<Integer, ArrayList<VxInfo>> orderedChildren,
            final BitSet onlyChildren,
            final float scale,
            final boolean strictCircularLayout,
            final float[] childrenRadii,
            final float[] fullRadii,
            final AnnulusInfo[] annulusInfo) throws InterruptedException {
        // Get the radius of the starting vertex.
        // We don't want a radius of zero; this will break things higher up and result in NaN values for x,y,z.
        // Instead, we'll use a minimum radius (pulled out of a hat).
        final float minRadius = 0.1F;
        final float selfRadius = scale * (radiusAttr != Graph.NOT_FOUND ? Math.max(graph.getFloatValue(radiusAttr, vxId), minRadius) : 1);

        // Find adjacent vertices to work on.
        final ArrayList<VxInfo> children = orderedChildren.get(vxId);

        if (CollectionUtils.isEmpty(children)) {
            fullRadii[vxId] = selfRadius;

            return selfRadius;
        } else if (children.size() == 1) {
            // Remove this child from consideration.
            removeChildren(vxsToGo, children);
            final VxInfo child = children.iterator().next();
            final float childRadius = findSpacingOf(child.vxId, vxsToGo, orderedChildren, onlyChildren, scale, strictCircularLayout, childrenRadii, fullRadii, annulusInfo);

            // Just sum our's and child's radii with a selfRadius buffer.
            // Since the single child will be pointed away from our parent,
            // we could pretend that we are actually smaller, but it is a
            // fudge that could get us in trouble.
            float fullRadius = selfRadius + selfRadius + childRadius;

            // Record and return result.
            childrenRadii[vxId] = fullRadius;
            fullRadii[vxId] = fullRadius;

            return fullRadius;
        } else {
            // Do nothing
        }

        // Force inner circle of childless children, if it makes sense to do so.
        int maxThisCircle = MAX_IN_ONE_CIRCLE;
        int nChildless = 0;
        int nWithChildren = 0;
        for (VxInfo child : children) {
            if (orderedChildren.containsKey(child.vxId)) {
                nWithChildren++;
            } else {
                nChildless++;
            }
        }

        if (nChildless > 2 && nChildless < MAX_IN_ONE_CIRCLE && nWithChildren > 0) {
            maxThisCircle = nChildless;
        }

        if (strictCircularLayout || children.size() <= maxThisCircle) {
            // Remove these children from consideration.
            removeChildren(vxsToGo, children);

            // Iterate through each, adding contributions to radius.
            float childrenCircum = 0;
            float maxChildRadius = 0;
            for (VxInfo child : children) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                final float childRadius = findSpacingOf(child.vxId, vxsToGo, orderedChildren, onlyChildren, scale, strictCircularLayout, childrenRadii, fullRadii, annulusInfo);
                if (maxChildRadius < childRadius) {
                    maxChildRadius = childRadius;
                }

                childrenCircum += 2 * childRadius;
            }

            final float radiusFromCircum = childrenCircum / TWO_PI;
            final float radiusFromMaxChild = selfRadius + selfRadius + maxChildRadius;
            float fullRadius = Math.max(radiusFromCircum, radiusFromMaxChild);
            childrenRadii[vxId] = fullRadius;

            fullRadius += maxChildRadius;
            fullRadii[vxId] = fullRadius;

            return fullRadius;
        } else {
            // We will fill up circles, each centered about the root, building out in radius.

            // Remove these children from consideration.
            removeChildren(vxsToGo, children);

            float innerRadius = 2 * selfRadius;

            // Iterate through each, adding contributions to radius.
            float annulusCircum = 0;
            float maxChildRadiusThisAnnulus = 0;
            final ArrayDeque<VxInfo> needRadii = new ArrayDeque<>();
            float lastChildRadius = 0;
            for (VxInfo child : children) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                final float childRadius = findSpacingOf(child.vxId, vxsToGo, orderedChildren, onlyChildren, scale, strictCircularLayout, childrenRadii, fullRadii, annulusInfo);

                // Will this be too much for this annulus?
                final float maxCircumThisAnnulus = TWO_PI * (innerRadius + maxChildRadiusThisAnnulus);
                final float neededCircumThisAnnulus = annulusCircum + 2 * childRadius;

                // ...or should we bump to a new annulus because of a big change in size?
                final boolean bump = lastChildRadius > 0 && childRadius > 4 * lastChildRadius;
                lastChildRadius = childRadius;

                // Go to new annulus if needed.
                if (bump || neededCircumThisAnnulus > maxCircumThisAnnulus) {
                    final float rad = innerRadius + maxChildRadiusThisAnnulus;
                    while (!needRadii.isEmpty()) {
                        final VxInfo vxi = needRadii.removeFirst();
                        annulusInfo[vxi.vxId] = new AnnulusInfo(rad, annulusCircum);
                    }

                    innerRadius += 2 * maxChildRadiusThisAnnulus;
                    annulusCircum = 0;
                    maxChildRadiusThisAnnulus = 0;

                }

                if (maxChildRadiusThisAnnulus < childRadius) {
                    maxChildRadiusThisAnnulus = childRadius;
                }

                annulusCircum += 2 * childRadius;
                needRadii.addLast(child);
            }

            final float rad = innerRadius + maxChildRadiusThisAnnulus;
            while (!needRadii.isEmpty()) {
                final VxInfo vxi = needRadii.removeFirst();
                annulusInfo[vxi.vxId] = new AnnulusInfo(rad, annulusCircum);
            }

            // Record and return result.
            final float fullRadius = innerRadius + 2 * maxChildRadiusThisAnnulus;
            fullRadii[vxId] = fullRadius;

            return fullRadius;
        }
    }

    private void positionThis(
            final int vxId,
            final BitSet vxsToGo,
            final HashMap<Integer, ArrayList<VxInfo>> orderedChildren,
            float ourLocX,
            float ourLocY,
            final float parentOffsetX,
            final float parentOffsetY,
            final boolean strictCircularLayout,
            final float[] childrenRadii,
            final float[] fullRadii,
            final AnnulusInfo[] annulusInfo,
            float parentAngle) throws InterruptedException {
        // Keep parent angle in check.
        if (parentAngle > TWO_PI) {
            parentAngle -= TWO_PI;
        }

        // Get radius from satellites.
        final float childrenRadius = childrenRadii[vxId];

        // Get adjacent vertices to work on.
        final ArrayList<VxInfo> children = orderedChildren.get(vxId);

        if (CollectionUtils.isEmpty(children)) {
            // Position us in centre.
            graph.setFloatValue(xAttr, vxId, ourLocX);
            graph.setFloatValue(yAttr, vxId, ourLocY);
            graph.setFloatValue(zAttr, vxId, 0);

            return;
        } else if (children.size() == 1) {
            // Remove these children from consideration.
            removeChildren(vxsToGo, children);

            final VxInfo child = children.iterator().next();
            final float actualChildRadius = fullRadii[child.vxId];
            final float parentLength = (float) Math.sqrt(parentOffsetX * parentOffsetX + parentOffsetY * parentOffsetY);
            float offsetX = 0;
            float offsetY = 0;

            if (parentLength > 0) {
                offsetX = (childrenRadius * parentOffsetX) / parentLength;
                offsetY = (childrenRadius * parentOffsetY) / parentLength;
            } else {
                offsetX = childrenRadius;
            }

            ourLocX -= (offsetX * actualChildRadius) / childrenRadius;
            ourLocY -= (offsetY * actualChildRadius) / childrenRadius;

            // Position us back from centre.
            graph.setFloatValue(xAttr, vxId, ourLocX);
            graph.setFloatValue(yAttr, vxId, ourLocY);
            graph.setFloatValue(zAttr, vxId, 0);

            // Position child forward from centre.
            positionThis(child.vxId, vxsToGo, orderedChildren, ourLocX + offsetX, ourLocY + offsetY, offsetX, offsetY, strictCircularLayout, childrenRadii, fullRadii, annulusInfo, parentAngle);

            return;
        } else {
            // More than one child.

            if (strictCircularLayout || annulusInfo[children.get(children.size() - 1).vxId] == null) { // annulusInfo[children.get(0).vxId]==null
                // Remove these children from consideration.
                removeChildren(vxsToGo, children);

                // Position us in centre.
                graph.setFloatValue(xAttr, vxId, ourLocX);
                graph.setFloatValue(yAttr, vxId, ourLocY);
                graph.setFloatValue(zAttr, vxId, 0);

                // Figure out available and needed circumferences.
                float neededCircumference = 0;
                for (VxInfo child : children) {
                    neededCircumference += 2 * fullRadii[child.vxId];
                }

                // Starting orientation is perpendicular to parent's.
                float accumCircle = neededCircumference * (0.25F + parentAngle / TWO_PI);

                // Loop through each child, positioning it and its satellites.
                float oldRadiusIncrement = 0;
                boolean doneOne = false;
                float accumToRadians = neededCircumference != 0 ? TWO_PI / neededCircumference : 0;
                for (VxInfo child : children) {
                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                    final float radiusIncrement = fullRadii[child.vxId];
                    if (doneOne) {
                        accumCircle += radiusIncrement + oldRadiusIncrement;
                    }

                    doneOne = true;
                    oldRadiusIncrement = radiusIncrement;

                    final float angle = accumToRadians * accumCircle;
                    final float offsetX = childrenRadius * (float) Math.sin(angle);
                    final float offsetY = childrenRadius * (float) Math.cos(angle);
                    positionThis(child.vxId, vxsToGo, orderedChildren, ourLocX + offsetX, ourLocY + offsetY, offsetX, offsetY, strictCircularLayout, childrenRadii, fullRadii, annulusInfo, angle);
                }
            } else {
                // Remove these children from consideration.
                removeChildren(vxsToGo, children);

                // Position us in centre.
                graph.setFloatValue(xAttr, vxId, ourLocX);
                graph.setFloatValue(yAttr, vxId, ourLocY);
                graph.setFloatValue(zAttr, vxId, 0);

                // Figure out available and needed circumferences.
                float cumAngle = 0;
                float lastAnnulus = 0;
                final float outermostAnnulus = annulusInfo[children.get(children.size() - 1).vxId].radius;
                for (VxInfo child : children) {
                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                    // Get child's radius and annulus.
                    final float childRadius = fullRadii[child.vxId];
                    final AnnulusInfo thisAnnulusInfo = annulusInfo[child.vxId];
                    final float childAnnulusRadius = thisAnnulusInfo.radius;
                    final float childAnnulusCircumSum = thisAnnulusInfo.circumference;

                    // Angle for this child.
                    final float angIncrement = TWO_PI * (childRadius / childAnnulusCircumSum);
                    cumAngle += angIncrement;

                    // The outermost annulus starts perpendicular to its parent's angle.
                    if (lastAnnulus != 0 && lastAnnulus != childAnnulusRadius && outermostAnnulus == childAnnulusRadius) {
                        cumAngle += TWO_PI * (parentAngle / TWO_PI + 0.25F);
                    }

                    lastAnnulus = childAnnulusRadius;

                    final float offsetX = childAnnulusRadius * (float) Math.sin(cumAngle);
                    final float offsetY = childAnnulusRadius * (float) Math.cos(cumAngle);
                    positionThis(child.vxId, vxsToGo, orderedChildren, ourLocX + offsetX, ourLocY + offsetY, offsetX, offsetY, strictCircularLayout, childrenRadii, fullRadii, annulusInfo, cumAngle);

                    cumAngle += angIncrement;
                }
            }
        }
    }

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
     * management of Annulus information
     */
    private static class AnnulusInfo {

        final float radius;
        final float circumference;

        public AnnulusInfo(final float radius, final float circumference) {
            this.radius = radius;
            this.circumference = circumference;
        }

        @Override
        public String toString() {
            return String.format("AnnulusInfo[r=%f,c=%f]", radius, circumference);
        }
    }
}
