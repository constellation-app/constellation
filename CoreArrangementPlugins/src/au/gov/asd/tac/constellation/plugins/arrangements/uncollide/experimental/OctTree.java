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
package au.gov.asd.tac.constellation.plugins.arrangements.uncollide.experimental;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;

/**
 * http://gamedev.tutsplus.com/tutorials/implementation/quick-tip-use-quadtrees-to-detect-likely-collisions-in-2d-space/
 *
 * @author algol
 * @author Nova
 */
public class OctTree extends AbstractTree {

    // Top/bottom, left/right, front/back; top-left-back is (0, 0, 0).
    protected static final int TOP_R_F = 0;
    protected static final int TOP_L_F = 1;
    protected static final int BOT_L_F = 2;
    protected static final int BOT_R_F = 3;
    protected static final int TOP_R_B = 4;
    protected static final int TOP_L_B = 5;
    protected static final int BOT_L_B = 6;
    protected static final int BOT_R_B = 7;

    private final int zId;

    /**
     * Constructor creates QuadTree and inserts all nodes
     *
     * @param graph The graph the QuadTree should be based on
     */
    protected OctTree(final GraphReadMethods graph) {
        super(graph, Dimensions.THREE);
        zId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z.getName());
        insertAll();
    }

    /**
     * Create a subtree of the current tree
     *
     * @param parent
     * @param box
     */
    protected OctTree(OctTree parent, final BoundingBox3D box) {
        super(parent, box);
        // Inherit parent values for graph based variables.
        zId = parent.zId;
    }

    /*
     * Splits the node into four subnodes.
     * <p>
     * Divide the node into four equal parts and initialise the four subnodes with the new bounds.
     */
    @Override
    protected void split() {
        final BoundingBox3D box3D = (BoundingBox3D) this.box;
        nodes = new OctTree[8];
        nodes[TOP_R_F] = new OctTree(this, box3D.topRightFrontOctant());
        nodes[TOP_L_F] = new OctTree(this, box3D.topLeftFrontOctant());
        nodes[BOT_L_F] = new OctTree(this, box3D.bottomLeftFrontOctant());
        nodes[BOT_R_F] = new OctTree(this, box3D.bottomRightFrontOctant());
        nodes[TOP_R_B] = new OctTree(this, box3D.topRightBackOctant());
        nodes[TOP_L_B] = new OctTree(this, box3D.topLeftBackOctant());
        nodes[BOT_L_B] = new OctTree(this, box3D.bottomLeftBackOctant());
        nodes[BOT_R_B] = new OctTree(this, box3D.bottomRightBackOctant());
    }

    /*
     * Determine which node the object belongs to.
     * <p>
     * -1 means object cannot completely fit within a child node and is part of the parent node.
     * <p>
     * Determine where an object belongs in the quadtree by determining which node the object can fit into.
     */
    @Override
    protected int getIndex(final int vxId) {
        final BoundingBox3D box3D = (BoundingBox3D) this.box;
        int index = -1;

        // Object can completely fit within the top/bottom halves.
        final boolean bottomHalf = wg.getFloatValue(yId, vxId) + wg.getFloatValue(rId, vxId) < box3D.midY;
        final boolean topHalf = wg.getFloatValue(yId, vxId) - wg.getFloatValue(rId, vxId) > box3D.midY;

        // Object can completely fit witin the left/right halves.
        final boolean leftHalf = wg.getFloatValue(xId, vxId) + wg.getFloatValue(rId, vxId) < box3D.midX;
        final boolean rightHalf = wg.getFloatValue(xId, vxId) - wg.getFloatValue(rId, vxId) > box3D.midX;

        // Object can completely fit in front/back halves.
        final boolean backHalf = wg.getFloatValue(zId, vxId) + wg.getFloatValue(rId, vxId) < box3D.midZ;
        final boolean frontHalf = wg.getFloatValue(zId, vxId) - wg.getFloatValue(rId, vxId) > box3D.midZ;

        if (topHalf) {
            if (leftHalf) {
                if (frontHalf) {
                    index = TOP_L_F;
                } else if (backHalf) {
                    index = TOP_L_B;
                } else {
                    // Do nothing
                }
            } else if (rightHalf) {
                if (frontHalf) {
                    index = TOP_R_F;
                } else if (backHalf) {
                    index = TOP_R_B;
                } else {
                    // Do nothing
                }
            } else {
                // Do nothing
            }
        } else if (bottomHalf) {
            if (leftHalf) {
                if (frontHalf) {
                    index = BOT_L_F;
                } else if (backHalf) {
                    index = BOT_L_B;
                }
            } else if (rightHalf) {
                if (frontHalf) {
                    index = BOT_R_F;
                } else if (backHalf) {
                    index = BOT_R_B;
                }
            }
        } else {
            // Do nothing
        }

        return index;
    }

    @Override
    protected double getDelta(final int vertex1, final int vertex2) {
        float deltaX = wg.getFloatValue(xId, vertex1) - wg.getFloatValue(xId, vertex2);
        float deltaY = wg.getFloatValue(yId, vertex1) - wg.getFloatValue(yId, vertex2);
        float deltaZ = wg.getFloatValue(zId, vertex1) - wg.getFloatValue(zId, vertex2);
        return Math.cbrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
    }

    @Override
    protected double getCollisionDistance(final int vertex1, final int vertex2) {
        return Math.cbrt(3 * wg.getFloatValue(rId, vertex1)) + Math.cbrt(3 * wg.getFloatValue(rId, vertex2));
    }
}
