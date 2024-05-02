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
package au.gov.asd.tac.constellation.graph.visual.utilities;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.utilities.camera.BoundingBox;

/**
 * Determine the bounding box of two sets of 3D vertices.
 * <p>
 * The graph renderer uses two sets of vertices to display a graph. The bounding
 * box must therefore take both sets of vertices into account.
 * <p>
 * There are two ways of providing vertices. First, when a graph is added and
 * the vertex array buffer is being built, vertexes can be added one by one
 * using addVertex(). Second, when a node is moved, an entire array buffer can
 * be passed.
 * <p>
 * When a node is moved by the user, the bounding box must be recalculated by
 * scanning all of the nodes, because we don't know if the nodes that were moved
 * made the bounding box bigger or smaller, or had no effect.
 * <p>
 * A bounding box can be used to place the camera in a position that can view
 * all of the nodes.
 *
 * @author algol
 */
public final class BoundingBoxUtilities {
    
    private BoundingBoxUtilities() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Recalculate the bounding box for the graph.
     *
     * @param box
     * @param graph The graph.
     * @param isSelected If true, only selected vertices are used to determine
     * the bounding box, otherwise all vertices are used.
     */
    public static void recalculateFromGraph(final BoundingBox box, final GraphReadMethods graph, final boolean isSelected) {
        if (isSelected) {
            encompassSelectedElements(box, graph);
        } else {
            encompassEntireGraph(box, graph);
        }
    }

    /**
     * Recalculate the bounding box for all vertices in the graph.
     *
     * @param graph Recalculate the bounding box of all vertices in this graph.
     */
    private static void encompassEntireGraph(final BoundingBox box, final GraphReadMethods rg) {
        box.resetMinMax();
        final int nVertices = rg.getVertexCount();
        if (nVertices > 0) {

            // Primary vertices.
            final int xAttr = rg.getAttribute(GraphElementType.VERTEX, "x");
            final int yAttr = rg.getAttribute(GraphElementType.VERTEX, "y");
            final int zAttr = rg.getAttribute(GraphElementType.VERTEX, "z");

            for (int position = 0; position < nVertices; position++) {
                final int vxId = rg.getVertex(position);
                final float x = rg.getFloatValue(xAttr, vxId);
                final float y = rg.getFloatValue(yAttr, vxId);
                final float z = rg.getFloatValue(zAttr, vxId);
                box.addVertex(x, y, z);
            }

            // Secondary vertices.
            final int x2Attr = rg.getAttribute(GraphElementType.VERTEX, "x2");
            final int y2Attr = rg.getAttribute(GraphElementType.VERTEX, "y2");
            final int z2Attr = rg.getAttribute(GraphElementType.VERTEX, "z2");
            if (x2Attr != Graph.NOT_FOUND && y2Attr != Graph.NOT_FOUND && z2Attr != Graph.NOT_FOUND) {
                for (int position = 0; position < nVertices; position++) {
                    final int vxId = rg.getVertex(position);
                    final float x2 = rg.getFloatValue(x2Attr, vxId);
                    final float y2 = rg.getFloatValue(y2Attr, vxId);
                    final float z2 = rg.getFloatValue(z2Attr, vxId);
                    box.addVertex2(x2, y2, z2);
                }

                if (box.getMin2().isZero() && box.getMax2().isZero()) {
                    box.zero2();
                }
            } else {
                box.zero2();
            }
        } else {
            box.zero();
            box.zero2();
        }
        box.setEmpty(false);
    }

    /**
     * Recalculate the bounding box for selected vertices in the graph.
     *
     * @param rg Recalculate the bounding box of selected vertices in this
     * graph.
     */
    private static void encompassSelectedElements(final BoundingBox box, final GraphReadMethods rg) {
        box.setEmpty(true);
        box.resetMinMax();
        final int selectedAttr = rg.getAttribute(GraphElementType.VERTEX, "selected");
        final int nVertices = rg.getVertexCount();
        if (selectedAttr != Graph.NOT_FOUND || nVertices > 0) {
            // Primary vertices.
            final int xAttr = rg.getAttribute(GraphElementType.VERTEX, "x");
            final int yAttr = rg.getAttribute(GraphElementType.VERTEX, "y");
            final int zAttr = rg.getAttribute(GraphElementType.VERTEX, "z");

            for (int position = 0; position < nVertices; position++) {
                final int vxId = rg.getVertex(position);
                final boolean isSelected = rg.getBooleanValue(selectedAttr, vxId);
                if (isSelected) {
                    final float x = rg.getFloatValue(xAttr, vxId);
                    final float y = rg.getFloatValue(yAttr, vxId);
                    final float z = rg.getFloatValue(zAttr, vxId);
                    box.addVertex(x, y, z);

                    box.setEmpty(false);
                }
            }

            // Secondary vertices.
            final int x2Attr = rg.getAttribute(GraphElementType.VERTEX, "x2");
            final int y2Attr = rg.getAttribute(GraphElementType.VERTEX, "y2");
            final int z2Attr = rg.getAttribute(GraphElementType.VERTEX, "z2");
            if (x2Attr != Graph.NOT_FOUND && y2Attr != Graph.NOT_FOUND && z2Attr != Graph.NOT_FOUND) {
                for (int position = 0; position < nVertices; position++) {
                    final int vxId = rg.getVertex(position);
                    final boolean isSelected = rg.getBooleanValue(selectedAttr, vxId);
                    if (isSelected) {
                        final float x2 = rg.getFloatValue(x2Attr, vxId);
                        final float y2 = rg.getFloatValue(y2Attr, vxId);
                        final float z2 = rg.getFloatValue(z2Attr, vxId);
                        box.addVertex2(x2, y2, z2);
                    }
                }
            } else {
                box.zero2();
            }

            // If any transactions are selected, add the vertices at the ends.
            final int txSelectedAttr = rg.getAttribute(GraphElementType.TRANSACTION, "selected");
            if (txSelectedAttr != Graph.NOT_FOUND) {
                final int txCount = rg.getTransactionCount();
                for (int position = 0; position < txCount; position++) {
                    final int txId = rg.getTransaction(position);
                    final boolean isSelected = rg.getBooleanValue(txSelectedAttr, txId);
                    if (isSelected) {
                        final int vxSrcId = rg.getTransactionSourceVertex(txId);
                        final float xsrc = rg.getFloatValue(xAttr, vxSrcId);
                        final float ysrc = rg.getFloatValue(yAttr, vxSrcId);
                        final float zsrc = rg.getFloatValue(zAttr, vxSrcId);
                        box.addVertex(xsrc, ysrc, zsrc);

                        final int vxDstId = rg.getTransactionDestinationVertex(txId);
                        final float xdst = rg.getFloatValue(xAttr, vxDstId);
                        final float ydst = rg.getFloatValue(yAttr, vxDstId);
                        final float zdst = rg.getFloatValue(zAttr, vxDstId);
                        box.addVertex(xdst, ydst, zdst);

                        box.setEmpty(false);
                    }
                }

                if (x2Attr != Graph.NOT_FOUND && y2Attr != Graph.NOT_FOUND && z2Attr != Graph.NOT_FOUND) {
                    for (int position = 0; position < txCount; position++) {
                        final int txId = rg.getTransaction(position);
                        final boolean isSelected = rg.getBooleanValue(txSelectedAttr, txId);
                        if (isSelected) {
                            final int vxSrcId = rg.getTransactionSourceVertex(txId);
                            final float x2src = rg.getFloatValue(x2Attr, vxSrcId);
                            final float y2src = rg.getFloatValue(y2Attr, vxSrcId);
                            final float z2src = rg.getFloatValue(z2Attr, vxSrcId);
                            box.addVertex2(x2src, y2src, z2src);

                            final int vxDstId = rg.getTransactionDestinationVertex(txId);
                            final float x2dst = rg.getFloatValue(x2Attr, vxDstId);
                            final float y2dst = rg.getFloatValue(y2Attr, vxDstId);
                            final float z2dst = rg.getFloatValue(z2Attr, vxDstId);
                            box.addVertex2(x2dst, y2dst, z2dst);
                        }
                    }
                }
            }
        } else {
            box.zero();
            box.zero2();
        }
    }

    /**
     * Recalculate the bounding box for specified vertices in the graph.
     *
     * @param box
     * @param rg Recalculate the bounding box of specified vertices in the
     * graph.
     * @param vertices The vertex ids to use in the recalculation.
     */
    public static void encompassSpecifiedElements(final BoundingBox box, final GraphReadMethods rg, final int[] vertices) {
        box.resetMinMax();
        final int nVertices = vertices.length;
        if (nVertices > 0) {
            // Primary vertices.
            final int xAttr = rg.getAttribute(GraphElementType.VERTEX, "x");
            final int yAttr = rg.getAttribute(GraphElementType.VERTEX, "y");
            final int zAttr = rg.getAttribute(GraphElementType.VERTEX, "z");

            for (final int vxId : vertices) {
                final float x = rg.getFloatValue(xAttr, vxId);
                final float y = rg.getFloatValue(yAttr, vxId);
                final float z = rg.getFloatValue(zAttr, vxId);
                box.addVertex(x, y, z);
            }

            // Secondary vertices.
            final int x2Attr = rg.getAttribute(GraphElementType.VERTEX, "x2");
            final int y2Attr = rg.getAttribute(GraphElementType.VERTEX, "y2");
            final int z2Attr = rg.getAttribute(GraphElementType.VERTEX, "z2");
            if (x2Attr != Graph.NOT_FOUND && y2Attr != Graph.NOT_FOUND && z2Attr != Graph.NOT_FOUND) {
                for (final int vxId : vertices) {
                    final float x2 = rg.getFloatValue(x2Attr, vxId);
                    final float y2 = rg.getFloatValue(y2Attr, vxId);
                    final float z2 = rg.getFloatValue(z2Attr, vxId);
                    box.addVertex2(x2, y2, z2);
                }

                if (box.getMin2().isZero() && box.getMax2().isZero()) {
                    box.zero2();
                }
            } else {
                box.zero2();
            }
        } else {
            box.zero();
            box.zero2();
        }
        box.setEmpty(false);
    }
}
