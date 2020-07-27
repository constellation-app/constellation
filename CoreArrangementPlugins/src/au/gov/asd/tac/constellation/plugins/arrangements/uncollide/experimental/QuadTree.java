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
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.python.modules.math;

/**
 * http://gamedev.tutsplus.com/tutorials/implementation/quick-tip-use-quadtrees-to-detect-likely-collisions-in-2d-space/
 *
 * @author algol
 * @author Nova
 */
class QuadTree {

    private static final int MAX_OBJECTS = 10;
    private static final int MAX_LEVELS = 5;

    private static final int TOP_R = 0;
    private static final int TOP_L = 1;
    private static final int BOT_L = 2;
    private static final int BOT_R = 3;
    
    private static final Logger LOG = Logger.getLogger(QuadTree.class.getName());
    
    private final int XID; 
    private final int YID;
    private final int RID;
    private final GraphReadMethods wg;

    private final int level;
    private final BoundingBox2D box;
    private final List<Integer> objects;
    private QuadTree[] nodes;

    /**
     * Constructor creates QuadTree and inserts all nodes
     * 
     * @param graph  The graph the QuadTree should be based on
     */
    QuadTree(final GraphReadMethods graph) {
        this.level = 0;
        this.box = new BoundingBox2D(graph);
        this.objects = new ArrayList<>();
        this.nodes = null;   
        
        wg = graph;
        XID = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName());
        YID = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName());
        RID = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.NODE_RADIUS.getName());
        
        insertAll();
    }

    /**
     * Create a subtree of the current tree
     * 
     * @param parent
     * @param box 
     */
    private QuadTree(QuadTree parent, final BoundingBox2D box) {
        this.level = parent.level + 1;
        this.box = box;
        objects = new ArrayList<>();
        nodes = null;   
        
        // Inherit parent values for graph based variables.
        wg = parent.wg;
        XID = parent.XID;
        YID = parent.YID;
        RID = parent.RID;
    }

    /*
     * Splits the node into four subnodes.
     * <p>
     * Divide the node into four equal parts and initialise the four subnodes with the new bounds.
     */
    private void split() {
        nodes = new QuadTree[4];
        nodes[TOP_R] = new QuadTree(this, box.topRightQuadrant());
        nodes[TOP_L] = new QuadTree(this, box.topLeftQuadrant());
        nodes[BOT_L] = new QuadTree(this, box.bottomLeftQuadrant());
        nodes[BOT_R] = new QuadTree(this, box.bottomRightQuadrant());
    }

    /*
     * Determine which node the object belongs to.
     * <p>
     * -1 means object cannot completely fit within a child node and is part of the parent node.
     * <p>
     * Determine where an object belongs in the quadtree by determining which node the object can fit into.
     */
    private int getIndex(final int vxId) {
        int index = -1;
        
        // Object can completely fit within the top/bottom halfss.
        final boolean topHalf = wg.getFloatValue(YID, vxId) + wg.getFloatValue(RID, vxId) < box.midY;
        final boolean bottomHalf = wg.getFloatValue(YID, vxId) - wg.getFloatValue(RID, vxId) > box.midY;

        // Object can completely fit within the left half.
        if (wg.getFloatValue(XID, vxId) + wg.getFloatValue(RID, vxId) < box.midX) {
            if (topHalf) {
                index = TOP_L; // fits in top left quadrant
            } else if (bottomHalf) {
                index = BOT_L; // fits in bottom left quadrant
            }
        } // Object can completely fit within the right half.
        else if (wg.getFloatValue(XID, vxId) - wg.getFloatValue(RID, vxId) > box.midX) {
            if (topHalf) {
                index = TOP_R; // fints in top right quadrant
            } else if (bottomHalf) {
                index = BOT_R; // fits in bottom right quadrant
            }
        }

        return index;
    }

    /*
     * Insert the object into the quadtree. If the node exceeds the capacity, it will split and add
     * objects that fit to their corresponding nodes.
     */
    private void insert(final int vxId) {
        if (nodes != null) { // if their are subnodes
            int index = getIndex(vxId); // find the correct subnode

            if (index != -1) { // if it fits neatly in a subnode
                nodes[index].insert(vxId); // insert into that subnode

                return;
            }
        }

        // if it fits in this node 
        
        objects.add(vxId); // add to list of objects

        if (objects.size() > MAX_OBJECTS && level < MAX_LEVELS) {
            if (nodes == null) { // if no subnodes then split
                split();
            }

            int i = 0;
            while (i < objects.size()) { // For each object get the index and insert it into the subnode if it fits in one. If it fits in a subnode remove it from this list of objects.
                int index = getIndex(objects.get(i));
                if (index != -1) {
                    nodes[index].insert(objects.remove(i));
                } else {
                    i++;
                }
            }
        }
    }
    
    private void insertAll() {
        for (int position = 0; position < wg.getVertexCount(); position++) {
            insert(wg.getVertex(position));
        }
    }

    /*
     * Return all objects that could collide with the given object.
     */
    private List<Integer> getPossibleColliders(final List<Integer> colliders, final int vxId) {
        // Recursively find all child colliders...
        final int index = getIndex(vxId);
        if (index != -1 && nodes != null) {
            nodes[index].getPossibleColliders(colliders, vxId);
        }

        // ...and colliders at this level.
        colliders.addAll(objects);

        return colliders;
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
    private boolean nodeCollides(final int subject, final float padding) {
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
                final double r = math.sqrt(2*wg.getFloatValue(RID, possible)) + math.sqrt(2*wg.getFloatValue(RID, subject)) + padding;
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
     * @param padding  The extra distance added to the sum of the radius needed
     * before two nodes can be considered to not be colliding.
     * @param twinThreshold A scaling factor for the collision distance within 
     * which the two noes are considered to be "twins". That is the distance
     * between them is so insignificant that we consider them in the same spot.
     * 
     * @return  A set of vertex ideas for verticies  that are twins with the subject
     */
    public Set<Integer> getTwins(final int subject, final float padding, final double twinThreshold) {
        final List<Integer> possibles = new ArrayList<>();
        getPossibleColliders(possibles, subject);
        Set<Integer> twins = new HashSet<>();
        for (final int possible : possibles) {
            if (subject != possible) {
                float deltaX = wg.getFloatValue(XID, subject) - wg.getFloatValue(XID, possible);
                float deltaY = wg.getFloatValue(YID, subject) - wg.getFloatValue(YID, possible);
                final double delta = math.sqrt(deltaX * deltaX + deltaY * deltaY);
                final double r = math.sqrt(2*wg.getFloatValue(RID, possible)) + math.sqrt(2*wg.getFloatValue(RID, subject)) + padding;
                final double criticalValue = r*twinThreshold; // The required distance for the nodes to be uncollided
                if ( delta < criticalValue ) {
                    twins.add(possible);
                }
            }
        }
        return twins;
    }
    
    /**
     * Check the entire graph for collisions. 
     *
     * @param subject  The vertex to check for collisions.
     * @param padding  The minimum distance between the vertex's edge and the edges
     * of each neighbor.
     * @return  boolean indicating whether the graph contains colliding verticies
     */
    public boolean hasCollision(final float padding){
        for (int position = 0; position < wg.getVertexCount(); position++) {
            if(nodeCollides(wg.getVertex(position), padding)) {
                return true;
            }
        }
        return false;
    }

}
