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
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.arrangements.uncollide.experimental.BoundingBox2D.Box2D;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.python.core.PyObject;
import org.python.modules.math;

/**
 * http://gamedev.tutsplus.com/tutorials/implementation/quick-tip-use-quadtrees-to-detect-likely-collisions-in-2d-space/
 *
 * @author algol
 */
public class QuadTree {

    private static final int MAX_OBJECTS = 10;
    private static final int MAX_LEVELS = 5;

    private static final int TOP_R = 0;
    private static final int TOP_L = 1;
    private static final int BOT_L = 2;
    private static final int BOT_R = 3;
    
    private static int XID; 
    private static int YID;
    private static int ZID;
    private static int RID;
    private static int X2ID;
    private static int Y2ID;
    private static int Z2ID;

    private final int level;
    private final List<Integer> objects;
    private final Box2D box;
    private final GraphWriteMethods wg;
    private QuadTree[] nodes;
    private static final Logger LOG = Logger.getLogger(QuadTree.class.getName());

    public QuadTree(final Box2D box, final GraphWriteMethods wg) {
        this(0, box, wg);
    }

    /*
     * Constructor
     */
    public QuadTree(final int level, final Box2D box, final GraphWriteMethods wg) {
        this.level = level;
        this.box = box;
        objects = new ArrayList<>();
        nodes = null;
        
        this.wg = wg;
        XID = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName());
        YID = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName());
        ZID = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z.getName());
        RID = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.NODE_RADIUS.getName());
        X2ID = wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "x2", "x2", 0, null);
        Y2ID = wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "y2", "y2", 0, null);
        Z2ID = wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "z2", "z2", 0, null);
    }

    public List<Box2D> getSubs() {
        final List<Box2D> boxes = new ArrayList<>();
        getSubs(boxes);

        return boxes;
    }

    private void getSubs(List<Box2D> boxes) {
        boxes.add(box);
        if (nodes != null) {
            for (final QuadTree qt : nodes) {
                qt.getSubs(boxes);
            }
        }
    }

    /*
     * Clears the quadtree.
     * <p>
     * Recursively clear all objects from all nodes.
     */
    public void clear() {
        objects.clear();

        if (nodes != null) {
            for (int i = 0; i < nodes.length; i++) {
                nodes[i].clear();
                nodes[i] = null;
            }

            nodes = null;
        }
    }

    /*
     * Splits the node into four subnodes.
     * <p>
     * Divide the node into four equal parts and initialise the four subnodes with the new bounds.
     */
    private void split() {
        final float minx = box.minx;
        final float miny = box.miny;
        final float maxx = box.maxx;
        final float maxy = box.maxy;
        final float midx = minx + (maxx - minx) / 2;
        final float midy = miny + (maxy - miny) / 2;

        nodes = new QuadTree[4];
        nodes[TOP_R] = new QuadTree(level + 1, new Box2D(midx, miny, maxx, midy), wg);
        nodes[TOP_L] = new QuadTree(level + 1, new Box2D(minx, miny, midx, midy), wg);
        nodes[BOT_L] = new QuadTree(level + 1, new Box2D(minx, midy, midx, maxy), wg);
        nodes[BOT_R] = new QuadTree(level + 1, new Box2D(midx, midy, maxx, maxy), wg);
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
        final double midx = box.minx + ((box.maxx - box.minx) / 2f);
        final double midy = box.miny + ((box.maxy - box.miny) / 2f);

        // Object can completely fit within the top/bottom quadrants.
        final boolean topQuadrant = wg.getFloatValue(YID, vxId) + wg.getFloatValue(RID, vxId) < midy;
        final boolean bottomQuadrant = wg.getFloatValue(YID, vxId) - wg.getFloatValue(RID, vxId) > midy;

        // Object can completely fit within the left quadrants.
        if (wg.getFloatValue(XID, vxId) + wg.getFloatValue(RID, vxId) < midx) {
            if (topQuadrant) {
                index = TOP_L;
            } else if (bottomQuadrant) {
                index = BOT_L;
            }
        } // Object can completely fit within the right quadrants.
        else if (wg.getFloatValue(XID, vxId) - wg.getFloatValue(RID, vxId) > midx) {
            if (topQuadrant) {
                index = TOP_R;
            } else if (bottomQuadrant) {
                index = BOT_R;
            }
        }

        return index;
    }

    /*
     * Insert the object into the quadtree. If the node exceeds the capacity, it will split and add
     * objects that fit to their corresponding nodes.
     */
    private void insert(final int vxId) {
        if (nodes != null) {
            int index = getIndex(vxId);

            if (index != -1) {
                nodes[index].insert(vxId);

                return;
            }
        }

        objects.add(vxId);

        if (objects.size() > MAX_OBJECTS && level < MAX_LEVELS) {
            if (nodes == null) {
                split();
            }

            int i = 0;
            while (i < objects.size()) {
                int index = getIndex(objects.get(i));
                if (index != -1) {
                    nodes[index].insert(objects.remove(i));
                } else {
                    i++;
                }
            }
        }
    }
    
    public void insertAll() {
        clear();
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
     * Uncollide this orb from its colliding neighbors.
     *
     * @param orb The orb to be uncollided.
     * @param padding The minimum distance between the orb's edge and the edges
     * of each neighbor.
     * @return the number of collisions.
     */
    public int uncollide(final int subject, final float padding) {
        final List<Integer> possibles = new ArrayList<>();
        getPossibleColliders(possibles, subject);

        // We need to deal with pathological cases such as everything at the same x,y point,
        // or everything co-linear.
        // We add a perturbation so points go different ways at different stages.
        float perturbation = 1e-4f;
        int collided = 0;
        for (final int possible : possibles) {
            if (subject != possible) {
                float DeltaX = wg.getFloatValue(XID, subject) - wg.getFloatValue(XID, possible);
                float DeltaY = wg.getFloatValue(YID, subject) - wg.getFloatValue(YID, possible);
                final double ll = DeltaX * DeltaX + DeltaY * DeltaY;
                final double r = wg.getFloatValue(RID, possible) + wg.getFloatValue(RID, subject) + padding;
                if (ll <= r * r) {
                    final double l = Math.sqrt(ll);
                    collided++;
                    final float nudge = l != 0 ? (float) Math.min((l - r) / l * 0.5, -0.1) : -0.1f;
                    DeltaX *= nudge;
                    DeltaX += perturbation;
                    DeltaY *= nudge;
                    DeltaY += perturbation;
                    perturbation = -perturbation;
//                    System.out.printf("-Collided %f %f %f x=%f y=%f\n  %s <> %s\n", l, r, nudge, x, y, circle, possible);
                    wg.setFloatValue(XID, subject, wg.getFloatValue(XID, subject) - DeltaX);
                    wg.setFloatValue(YID, subject, wg.getFloatValue(YID, subject) - DeltaY);
                    wg.setFloatValue(XID, possible, wg.getFloatValue(XID, possible) + DeltaX);
                    wg.setFloatValue(YID, possible, wg.getFloatValue(YID, possible) + DeltaY);
                }
            }
        }

        return collided;
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
    public boolean nodeCollides(final int subject, final float padding) {
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
                final double r = wg.getFloatValue(RID, possible) + wg.getFloatValue(RID, subject) + padding;
                if (0 < l && l <= r*r) {
//                    LOG.info("l:" + l + "    r:" + r);
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * Nudges two nodes in exactly the same place so that they do not overlap.
     *
     * @param subject The vertex to check for twins.
     * @param padding The minimum distance between the vertex's edge and the edges
     * of each neighbor.
     */
    private void nudgeTwins(final int subject, final float padding) {
        final List<Integer> possibles = new ArrayList<>();
        getPossibleColliders(possibles, subject);

        // We need to deal with pathological cases such as everything at the same x,y point,
        // or everything co-linear.
        // We add a perturbation so points go different ways at different stages.
        for (final int possible : possibles) {
            if (subject != possible) {
                float DeltaX = wg.getFloatValue(XID, subject) - wg.getFloatValue(XID, possible);
                float DeltaY = wg.getFloatValue(YID, subject) - wg.getFloatValue(YID, possible);
                final double ll = DeltaX * DeltaX + DeltaY * DeltaY;
                final double r = wg.getFloatValue(RID, possible) + wg.getFloatValue(RID, subject) + padding;
                if (ll == 0) {
                    final float nudge = (float) (math.sqrt(r)/1.4); // sqrt(2A^2) = R then A = r/sqrt(2). The nudge needed to not overlap if moving both nodes away form each by the same value.

                    // The two nodes will be immediately alongside each other.
                    wg.setFloatValue(XID, subject, wg.getFloatValue(XID, subject) - nudge);
                    wg.setFloatValue(XID, possible, wg.getFloatValue(XID, possible) + nudge);
                }
            }
        }
    }
    
    
    public void nudgeAllTwins(final float padding){
        for (int position = 0; position < wg.getVertexCount(); position++) {
            nudgeTwins(wg.getVertex(position), padding);
        }
    }
    
    /**
     * Check the entire graph for collisions. Return the number of verticies
     * checked before finding a collision.
     * Returns 0 if no collision is found.
     *
     * @param subject The vertex to check for collisions.
     * @param padding The minimum distance between the vertex's edge and the edges
     * of each neighbor.
     * @return the number of collisions.
     */
    public int findCollision(final float padding){
        int verticiesChecked = 1;
        for (int position = 0; position < wg.getVertexCount(); position++) {
            if(nodeCollides(wg.getVertex(position), padding)) {
                return verticiesChecked;
            } else {
                verticiesChecked += 1;
            }      
        }
        return 0;
    }
    
    public int uncollideAll(final float padding){
        int totalCollided = 0;
        for (int position = 0; position < wg.getVertexCount(); position++) {
            totalCollided += uncollide(wg.getVertex(position), padding);
        }
        return totalCollided;
    }


    @Override
    public String toString() {
        return String.format("[QTree level=%d size=%d %s]", level, objects.size(), box);
    }
}
