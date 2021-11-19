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
package au.gov.asd.tac.constellation.plugins.arrangements.utilities;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.arrangements.GraphTaxonomy;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * provides a set of functions pertaining to a graph's components and its
 * vertices
 *
 * @author algol
 */
public final class ArrangementUtilities {

    public static final int FUNDAMENTAL_SIZE = 2; //20;
    
    private ArrangementUtilities() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Find the minimum sum of weighted edges that must be traversed to reach
     * all other reachable vertices from the given one, moving either only
     * forward, only backward, or both.
     *
     * The edge weights here are special: the weight of a edge is the sum of the
     * "radii" of the two vertices it joins. Results are appended to
     * distancesToVertices, which should be cleared first, else the breadth
     * first search will stop when vertices contained in distancesToVertices are
     * encountered.
     *
     * @param graph the read lock that will be used for the operation.
     * @param vxId the id of the vertex to start from.
     * @param goForward can transactions be traveled along in the forward
     * direction.
     * @param goBackward can transactions be traveled along in the reverse
     * direction.
     * @param minRadius the minimum radius of vertices.
     *
     * @return the minimum distance to each vertex in the graph.
     */
    public static float[] getMinDistancesToReachableVertices(
            final GraphReadMethods graph,
            final int vxId,
            final boolean goForward,
            final boolean goBackward,
            final float minRadius) {
        final int nradiusAttr = VisualConcept.VertexAttribute.LABEL_RADIUS.get(graph);
        final float[] distancesToVertices = new float[graph.getVertexCapacity()];

        // Fill the array with -1 to indicate "unknown distance".
        final int NO_DISTANCE = -1;
        Arrays.fill(distancesToVertices, NO_DISTANCE);

        // Add the starting vertex; no traversal is required to know that the starting vertex
        // is zero distance from itself.
        distancesToVertices[vxId] = 0;

        // This will hold all vertices that have been reached so far but not processed.
        final ArrayDeque<Integer> vxQueue = new ArrayDeque<>();
        vxQueue.add(vxId);

        while (!vxQueue.isEmpty()) {
            // Get the first vertex in the queue...
            final int parentVxId = vxQueue.removeFirst();

            // ...and its distance.
            final float parentRadius = Math.max(minRadius, nradiusAttr != Graph.NOT_FOUND ? graph.getFloatValue(nradiusAttr, parentVxId) : 1);
            final float parentDistance = distancesToVertices[parentVxId] + parentRadius;

            if (goForward) {
                // Process outgoing edges.
                final int outCount = graph.getVertexEdgeCount(parentVxId, Graph.OUTGOING);
                for (int ePosition = 0; ePosition < outCount; ePosition++) {
                    final int edgeId = graph.getVertexEdge(parentVxId, Graph.OUTGOING, ePosition);

                    // Get the destination vertex of this outgoing edge.
                    final int childVxId = graph.getEdgeDestinationVertex(edgeId);
                    if (distancesToVertices[childVxId] == NO_DISTANCE) {
                        final float childRadius = Math.max(minRadius, nradiusAttr != Graph.NOT_FOUND ? graph.getFloatValue(nradiusAttr, childVxId) : 1);
                        final float childDistance = parentDistance + childRadius;
                        vxQueue.add(childVxId);
                        distancesToVertices[childVxId] = childDistance;
                    }
                }
            }

            if (goBackward) {
                // Process incoming edges.
                final int inCount = graph.getVertexEdgeCount(parentVxId, Graph.INCOMING);
                for (int ePosition = 0; ePosition < inCount; ePosition++) {
                    final int edgeId = graph.getVertexEdge(parentVxId, Graph.INCOMING, ePosition);

                    // Get the source vertex of this incoming edge.
                    final int childVxId = graph.getEdgeSourceVertex(edgeId);
                    if (distancesToVertices[childVxId] == NO_DISTANCE) {
                        final float childRadius = 1.5F * (nradiusAttr != Graph.NOT_FOUND ? graph.getFloatValue(nradiusAttr, childVxId) : 1);
                        final float childDistance = parentDistance + childRadius;
                        vxQueue.add(childVxId);
                        distancesToVertices[childVxId] = childDistance;
                    }
                }
            }
        }

        return distancesToVertices;
    }

    /**
     * Get the mean of the x,y,z coordinates of the vertices of a graph.
     *
     * @param rg the graph read lock that will be used to perform this
     * operation.
     * @return the mean of the x,y,z coordinates of the vertices of a graph.
     */
    public static float[] getXyzMean(final GraphReadMethods rg) {
        final double[] mean = new double[]{
            0, 0, 0
        };

        final int vxCount = rg.getVertexCount();
        if (vxCount != 0) {
            final int xAttr = rg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName());
            final int yAttr = rg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName());
            final int zAttr = rg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z.getName());

            // Use a double[] in case we get big numbers.
            for (int position = 0; position < vxCount; position++) {
                final int vxId = rg.getVertex(position);

                final float x = rg.getFloatValue(xAttr, vxId);
                final float y = rg.getFloatValue(yAttr, vxId);
                final float z = rg.getFloatValue(zAttr, vxId);

                mean[0] += x;
                mean[1] += y;
                mean[2] += z;
            }

            mean[0] /= vxCount;
            mean[1] /= vxCount;
            mean[2] /= vxCount;
        }

        return new float[]{
            (float) mean[0], (float) mean[1], (float) mean[2]
        };
    }

    /**
     * Move a graph from its current mean to a given mean.
     *
     * @param wg the graph write lock that will used for this operation.
     * @param oldMean the old mean that will be updated.
     */
    public static void moveMean(final GraphWriteMethods wg, final float[] oldMean) {
        final float[] newMean = ArrangementUtilities.getXyzMean(wg);
        final int xAttr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName());
        final int yAttr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName());
        final int zAttr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z.getName());
        final int x2Attr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X2.getName());
        final int y2Attr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y2.getName());
        final int z2Attr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z2.getName());
        final boolean xyz2 = x2Attr != Graph.NOT_FOUND && y2Attr != Graph.NOT_FOUND && z2Attr != Graph.NOT_FOUND;

        final int vxCount = wg.getVertexCount();
        for (int position = 0; position < vxCount; position++) {
            final int vxId = wg.getVertex(position);

            wg.setFloatValue(xAttr, vxId, wg.getFloatValue(xAttr, vxId) - newMean[0] + oldMean[0]);
            wg.setFloatValue(yAttr, vxId, wg.getFloatValue(yAttr, vxId) - newMean[1] + oldMean[1]);
            wg.setFloatValue(zAttr, vxId, wg.getFloatValue(zAttr, vxId) - newMean[2] + oldMean[2]);

            if (xyz2) {
                wg.setFloatValue(x2Attr, vxId, wg.getFloatValue(x2Attr, vxId) - newMean[0] + oldMean[0]);
                wg.setFloatValue(y2Attr, vxId, wg.getFloatValue(y2Attr, vxId) - newMean[1] + oldMean[1]);
                wg.setFloatValue(z2Attr, vxId, wg.getFloatValue(z2Attr, vxId) - newMean[2] + oldMean[2]);
            }
        }
    }

    @Deprecated
    public static float[] getSubsetMean(final GraphWriteMethods graph, final BitSet vertices) {
        final double[] mean = new double[]{
            0, 0, 0
        };

        final double n = vertices.cardinality();
        if (n != 0) {
            // Get/set the x,y,z attributes.
            if (VisualConcept.VertexAttribute.X.get(graph) == Graph.NOT_FOUND) {
                graph.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "x", "x", null, null);
            }
            if (VisualConcept.VertexAttribute.Y.get(graph) == Graph.NOT_FOUND) {
                graph.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "y", "y", null, null);
            }
            if (VisualConcept.VertexAttribute.Z.get(graph) == Graph.NOT_FOUND) {
                graph.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "z", "z", null, null);
            }

            final int xAttr = VisualConcept.VertexAttribute.X.get(graph);
            final int yAttr = VisualConcept.VertexAttribute.Y.get(graph);
            final int zAttr = VisualConcept.VertexAttribute.Z.get(graph);

            // Use a double[] in case we get big numbers.
            for (int vxId = vertices.nextSetBit(0); vxId >= 0; vxId = vertices.nextSetBit(vxId + 1)) {
                final float x = graph.getFloatValue(xAttr, vxId);
                final float y = graph.getFloatValue(yAttr, vxId);
                final float z = graph.getFloatValue(zAttr, vxId);

                mean[0] += x;
                mean[1] += y;
                mean[2] += z;
            }

            mean[0] /= n;
            mean[1] /= n;
            mean[2] /= n;
        }

        return new float[]{
            (float) mean[0], (float) mean[1], (float) mean[2]
        };
    }

    /**
     * Gather a Graph's vxIds into a BitSet for faster checking.
     *
     * @param rg The graph.
     *
     * @return A BitSet where vertex ids in the Graph are set.
     */
    public static BitSet vertexBits(final GraphReadMethods rg) {
        final int vxCount = rg.getVertexCount();
        final BitSet bs = new BitSet(rg.getVertexCapacity());
        for (int position = 0; position < vxCount; position++) {
            final int vxId = rg.getVertex(position);
            bs.set(vxId);
        }

        return bs;
    }

    /**
     * Returns a GraphTaxonomy, with each taxon representing the vertices in a
     * (weak) component.
     * <p>
     * This procedure is fundamentally linear, but may be slowed by construction
     * of reporting structures. It is implemented as a breadth-first traversal.
     * <p>
     * @param wg The graph to get the components from.
     *
     * @return a GraphTaxonomy, with each taxon representing the vertices in a
     * (weak) component.
     */
    public static GraphTaxonomy getComponents(final GraphWriteMethods wg) {
        Map<Integer, Set<Integer>> components = new HashMap<>();
        Map<Integer, Integer> nodeToComponent = new HashMap<>();
        final int singletonsComponentID = -1;
        final int doubletsComponentID = -2;
        components.put(singletonsComponentID, new HashSet<>());
        components.put(doubletsComponentID, new HashSet<>());
        final BitSet potentials = vertexBits(wg);
        for (int vxID = potentials.nextSetBit(0); vxID >= 0; vxID = potentials.nextSetBit(vxID + 1)) {
            Set<Integer> component = new HashSet<>();
            component.add(vxID);
            nodeToComponent.put(vxID, vxID);
            potentials.clear(vxID);
            if (wg.getVertexNeighbourCount(vxID) != 0) {
                Deque<Integer> neighbours = new LinkedList<>();
                neighbours.add(vxID);
                while (!neighbours.isEmpty()) {
                    final Integer nxID = neighbours.remove();
                    for (int i = 0; i < wg.getVertexNeighbourCount(nxID); i++) {
                        final int nextNxID = wg.getVertexNeighbour(nxID, i);
                        if (potentials.get(nextNxID)) {
                            component.add(nextNxID);
                            nodeToComponent.put(nextNxID, vxID);
                            neighbours.add(nextNxID);
                            potentials.clear(nextNxID);
                        }
                    }
                }
            }
            if (component.size() == 1) {
                components.get(singletonsComponentID).addAll(component);
                nodeToComponent.put(vxID, singletonsComponentID);
            } else if (component.size() == 2) {
                components.get(doubletsComponentID).addAll(component);
                for (int vert : component) {
                    nodeToComponent.put(vert, doubletsComponentID);
                }
            } else {
                components.put(vxID, component);
            }
        }
        return new GraphTaxonomy(wg, components, nodeToComponent, singletonsComponentID, doubletsComponentID);
    }

    /**
     * Given a vertex, find all of the vertices in its component.
     *
     * @param rg The graph containing the vertex.
     * @param seedVxId The vertex to start from.
     *
     * @return A Set&lt;Integer&gt; containing all of the vertices in the same
     * component as rootVxId.
     */
    public static Set<Integer> getComponentContainingVertex(final GraphReadMethods rg, final int seedVxId) {
        final Set<Integer> component = new HashSet<>();

        final ArrayDeque<Integer> neighbours = new ArrayDeque<>();
        neighbours.add(seedVxId);
        component.add(seedVxId);
        while (!neighbours.isEmpty()) {
            final Integer vxId = neighbours.removeFirst();
            final int nNeighbours = rg.getVertexNeighbourCount(vxId);
            for (int nbPosition = 0; nbPosition < nNeighbours; nbPosition++) {
                final int nbId = rg.getVertexNeighbour(vxId, nbPosition);

                if (!component.contains(nbId)) {
                    neighbours.add(nbId);
                    component.add(nbId);
                }
            }
        }

        return component;
    }

    /**
     * Returns a GraphTaxonomy, with each taxon representing the vertices in a
     * (weak) component.
     * <p>
     * This procedure is fundamentally linear, but may be slowed by construction
     * of reporting structures. It is implemented as a breadth-first traversal.
     * <p>
     * @param graph The graph to get the components from.
     * @param verticesToArrange a bit set specifying which vertices to arrange.
     *
     * @return a GraphTaxonomy, with each taxon representing the vertices in a
     * (weak) component.
     */
    @Deprecated
    public static GraphTaxonomy getComponents(final GraphWriteMethods graph, final BitSet verticesToArrange) {
        final Map<Integer, Set<Integer>> tax = new HashMap<>();

        final BitSet tmp = new BitSet();
        tmp.or(verticesToArrange);
        for (int vxId = tmp.nextSetBit(0); vxId >= 0; vxId = tmp.nextSetBit(vxId + 1)) {
            if (graph.getVertexNeighbourCount(vxId) == 0) {
                // Short cut to avoid extra work.
                final Set<Integer> s = new HashSet<>();
                s.add(vxId);
                tax.put(vxId, s);
                tmp.clear(vxId);
            } else {
                final Set<Integer> component = getComponentContainingVertex(graph, vxId, tmp);
                tax.put(component.iterator().next(), component);

                // Clear the vertices in this component from the vertices BitSet.
                for (int i : component) {
                    tmp.clear(i);
                }
            }
        }

        return new GraphTaxonomy(graph, tax);
    }

    /**
     * Given a vertex, find all of the vertices in its component.
     *
     * @param graph The graph containing the vertex.
     * @param seedVxId The vertex to start from.
     * @param verticesToArrange a BitSet specifying which vertices to arrange.
     *
     * @return A Set&lt;Integer%gt; containing all of the vertices in the same
     * component as rootVxId.
     */
    @Deprecated
    public static Set<Integer> getComponentContainingVertex(final GraphReadMethods graph, final int seedVxId, final BitSet verticesToArrange) {
        final Set<Integer> component = new HashSet<>();

        final ArrayDeque<Integer> neighbours = new ArrayDeque<>();
        neighbours.add(seedVxId);
        component.add(seedVxId);
        while (!neighbours.isEmpty()) {
            final Integer vxId = neighbours.removeFirst();
//            component.add(vxId);
//            Debug.debug("@added to component: %d (%d)\n", vxId, component.size());
            final int nNeighbours = graph.getVertexNeighbourCount(vxId);
            for (int nbPosition = 0; nbPosition < nNeighbours; nbPosition++) {
                final int nbId = graph.getVertexNeighbour(vxId, nbPosition);

                if (verticesToArrange.get(nbId) && !component.contains(nbId)) {
                    neighbours.add(nbId);
                    component.add(nbId);
                }
            }
        }

        return component;
    }

    /**
     * Get the vertices that are sources, ie those with in-degree zero.
     *
     * @param graph the graph write lock that will be used to perform this
     * operation.
     * @return the vertices that are sources, ie those with in-degree zero.
     */
    public static Deque<Integer> getSources(final GraphWriteMethods graph) {
        final Deque<Integer> q = new ArrayDeque<>();

        final int vxCount = graph.getVertexCount();
        for (int position = 0; position < vxCount; position++) {
            final int vxId = graph.getVertex(position);

            if (graph.getVertexEdgeCount(vxId, Graph.INCOMING) == 0) {
                q.add(vxId);
            }
        }

        return q;
    }

    /**
     * Set x2,y2,z2 to be the same as x,y,z.
     *
     * @param wg the graph write lock that will be used to perform this
     * operation.
     */
    public static void setXYZ2FromXYZ(final GraphWriteMethods wg) {
        final int x2Attr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X2.getName());
        final int y2Attr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y2.getName());
        final int z2Attr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z2.getName());

        // If x2,y2,z2 attributes do not exist, do not set them.
        if (x2Attr != Graph.NOT_FOUND && y2Attr != Graph.NOT_FOUND && z2Attr != Graph.NOT_FOUND) {
            final int xAttr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName());
            final int yAttr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName());
            final int zAttr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z.getName());

            final int vxCount = wg.getVertexCount();
            for (int i = 0; i < vxCount; i++) {
                final int vxId = wg.getVertex(i);

                wg.setFloatValue(x2Attr, vxId, wg.getFloatValue(xAttr, vxId));
                wg.setFloatValue(y2Attr, vxId, wg.getFloatValue(yAttr, vxId));
                wg.setFloatValue(z2Attr, vxId, wg.getFloatValue(zAttr, vxId));
            }
        }
    }
}
