/*
 * Copyright 2010-2020 Australian Signals Directorate
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
import java.util.ArrayList;
import java.util.List;
import org.python.modules.math;

/**
 * http://gamedev.tutsplus.com/tutorials/implementation/quick-tip-use-quadtrees-to-detect-likely-collisions-in-2d-space/
 *
 * @author algol
 * @author Nova
 */
class OctTree extends AbstractTree{
    // Top/bottom, left/right, front/back; top-left-back is (0, 0, 0).
    private static final int TOP_R_F = 0;
    private static final int TOP_L_F = 1;
    private static final int BOT_L_F = 2;
    private static final int BOT_R_F = 3;
    private static final int TOP_R_B = 4;
    private static final int TOP_L_B = 5;
    private static final int BOT_L_B = 6;
    private static final int BOT_R_B = 7;
    
    private final int ZID;

    /**
     * Constructor creates QuadTree and inserts all nodes
     * 
     * @param graph  The graph the QuadTree should be based on
     */
    OctTree(final GraphReadMethods graph) {
        super(graph);
        this.box = new BoundingBox3D(graph);
        ZID = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z.getName());
    }

    /**
     * Create a subtree of the current tree
     * 
     * @param parent
     * @param box 
     */
    private OctTree(OctTree parent, final BoundingBox3D box) {
        super(parent, box);
        // Inherit parent values for graph based variables.
        ZID = parent.ZID;
    }

    /*
     * Splits the node into four subnodes.
     * <p>
     * Divide the node into four equal parts and initialise the four subnodes with the new bounds.
     */
    @Override
    protected void split() {
        BoundingBox3D box3D = (BoundingBox3D) this.box;
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
        BoundingBox3D box3D = (BoundingBox3D) this.box;
        int index = -1;
        
        // Object can completely fit within the top/bottom halves.
        final boolean bottomHalf = wg.getFloatValue(YID, vxId) + wg.getFloatValue(RID, vxId) < box3D.midY;
        final boolean topHalf = wg.getFloatValue(YID, vxId) - wg.getFloatValue(RID, vxId) > box3D.midY;
        
        // Object can completely fit witin the left/right halves.
        final boolean leftHalf = wg.getFloatValue(XID, vxId) + wg.getFloatValue(RID, vxId) < box3D.midX;
        final boolean rightHalf = wg.getFloatValue(XID, vxId) - wg.getFloatValue(RID, vxId) > box3D.midX;
        
        // Object can completely fit in front/back halves.
        final boolean backHalf = wg.getFloatValue(ZID, vxId) + wg.getFloatValue(RID, vxId) < box3D.midZ;
        final boolean frontHalf = wg.getFloatValue(ZID, vxId) + wg.getFloatValue(RID, vxId) > box3D.midZ;


        if (topHalf) { 
            if (leftHalf) { 
                if (frontHalf) {
                    index = TOP_L_F;
                } else if (backHalf) {
                    index = TOP_L_B;
                }
            } else if (rightHalf) {
                if (frontHalf) {
                    index = TOP_R_F;
                } else if (backHalf) {
                    index = TOP_R_B;
                }
            }
        } 
        else if (bottomHalf) {
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
        }

        return index;
    }


    /**
     * Returns boolean indicating whether or not the vertex collides with any
     * other verticies. Two verticies in exactly the same spot are not counted
     * as overlapping.
     *
     * @param subject The vertex to check for collisions.
     * @param padding The minimum distance between the vertex's edge and the edges
     * of each neighbor.
     * @return the number of collisions.
     */
    @Override
    protected boolean nodeCollides(final int subject) {
        final List<Integer> possibles = new ArrayList<>();
        getPossibleColliders(possibles, subject);

        // We need to deal with pathological cases such as everything at the same x,y point,
        // or everything co-linear.
        // We add a perturbation so points go different ways at different stages.
        for (final int possible : possibles) {
            if (subject != possible) {
                float DeltaX = wg.getFloatValue(XID, subject) - wg.getFloatValue(XID, possible);
                float DeltaY = wg.getFloatValue(YID, subject) - wg.getFloatValue(YID, possible);
                final double l = DeltaX * DeltaX + DeltaY * DeltaY;
                final double r = math.sqrt(2*wg.getFloatValue(RID, possible)) + math.sqrt(2*wg.getFloatValue(RID, subject));
                if (l < r*r) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Check the subject for "twin" verticies
     * 
     * A twin verticie is defined as a verticie that falls within twinThreshold
     *  x (subject radius + twin radius + padding) of the subject.
     * The average radius is the average of the subject verticies radius and the
     * potential twins radius.
     * @param subject  The id of the vertex you wish to check for twins.
     * @param twinThreshold A scaling factor for the collision distance within 
     * which the two noes are considered to be "twins". That is the distance
     * between them is so insignificant that we consider them in the same spot.
     * 
     * @return  A set of vertex ideas for verticies  that are twins with the subject
     */
    @Override
    public List<Integer> getTwins(final int subject, final double twinThreshold) {
        final List<Integer> possibles = new ArrayList<>();
        getPossibleColliders(possibles, subject);
        List<Integer> twins = new ArrayList<>();
        for (final int possible : possibles) {
            if (subject != possible) {
                float deltaX = wg.getFloatValue(XID, subject) - wg.getFloatValue(XID, possible);
                float deltaY = wg.getFloatValue(YID, subject) - wg.getFloatValue(YID, possible);
                final double delta = math.sqrt(deltaX * deltaX + deltaY * deltaY);
                final double r = math.sqrt(2*wg.getFloatValue(RID, possible)) + math.sqrt(2*wg.getFloatValue(RID, subject));
                final double criticalValue = r*twinThreshold; // The required distance for the nodes to be uncollided
                if ( delta < criticalValue ) {
                    twins.add(possible);
                }
            }
        }
        return twins;
    }

}
