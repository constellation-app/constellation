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
package au.gov.asd.tac.constellation.plugins.arrangements.uncollide.d2;

import au.gov.asd.tac.constellation.plugins.arrangements.uncollide.d2.BoundingBox2D.Box2D;
import java.util.ArrayList;
import java.util.List;

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

    private final int level;
    private final List<Orb2D> objects;
    private final Box2D box;
    private QuadTree[] nodes;

    public QuadTree(final Box2D box) {
        this(0, box);
    }

    /*
     * Constructor
     */
    public QuadTree(final int level, final Box2D box) {
        this.level = level;
        this.box = box;
        objects = new ArrayList<>();
        nodes = null;
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
        nodes[TOP_R] = new QuadTree(level + 1, new Box2D(midx, miny, maxx, midy));
        nodes[TOP_L] = new QuadTree(level + 1, new Box2D(minx, miny, midx, midy));
        nodes[BOT_L] = new QuadTree(level + 1, new Box2D(minx, midy, midx, maxy));
        nodes[BOT_R] = new QuadTree(level + 1, new Box2D(midx, midy, maxx, maxy));
    }

    /*
     * Determine which node the object belongs to.
     * <p>
     * -1 means object cannot completely fit within a child node and is part of the parent node.
     * <p>
     * Determine where an object belongs in the quadtree by determining which node the object can fit into.
     */
    private int getIndex(final Orb2D orb) {
        int index = -1;
        final double midx = box.minx + ((box.maxx - box.minx) / 2F);
        final double midy = box.miny + ((box.maxy - box.miny) / 2F);

        // Object can completely fit within the top/bottom quadrants.
        final boolean topQuadrant = orb.getY() + orb.r < midy;
        final boolean bottomQuadrant = orb.getY() - orb.r > midy;

        // Object can completely fit within the left quadrants.
        if (orb.getX() + orb.r < midx) {
            if (topQuadrant) {
                index = TOP_L;
            } else if (bottomQuadrant) {
                index = BOT_L;
            } else {
                // Do nothing
            }
        } // Object can completely fit within the right quadrants.
        else if (orb.getX() - orb.r > midx) {
            if (topQuadrant) {
                index = TOP_R;
            } else if (bottomQuadrant) {
                index = BOT_R;
            } else {
                // Do nothing
            }
        } else {
            // Do nothing
            return index;
        }

        return index;
    }

    /*
     * Insert the object into the quadtree. If the node exceeds the capacity, it will split and add
     * objects that fit to their corresponding nodes.
     */
    public void insert(final Orb2D orb) {
        if (nodes != null) {
            int index = getIndex(orb);

            if (index != -1) {
                nodes[index].insert(orb);

                return;
            }
        }

        objects.add(orb);

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

    /*
     * Return all objects that could collide with the given object.
     */
    public List<Orb2D> getPossibleColliders(final List<Orb2D> colliders, final Orb2D orb) {
        // Recursively find all child colliders...
        final int index = getIndex(orb);
        if (index != -1 && nodes != null) {
            nodes[index].getPossibleColliders(colliders, orb);
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
    public int uncollide(final Orb2D orb, final float padding) {
        final List<Orb2D> possibles = new ArrayList<>();
        getPossibleColliders(possibles, orb);

        // We need to deal with pathological cases such as everything at the same x,y point,
        // or everything co-linear.
        // We add a perturbation so points go different ways at different stages.
        float perturbation = 1e-4F;
        int collided = 0;
        for (final Orb2D possible : possibles) {
            if (orb != possible) {
                float x = orb.getX() - possible.getX();
                float y = orb.getY() - possible.getY();
                final double ll = x * x + y * y;
                final double r = possible.r + orb.r + padding;
                if (ll <= r * r) {
                    final double l = Math.sqrt(ll);
                    collided++;
                    final float nudge = l != 0 ? (float) Math.min((l - r) / l * 0.5, -0.1) : -0.1F;
                    x *= nudge;
                    x += perturbation;
                    y *= nudge;
                    y += perturbation;
                    perturbation = -perturbation;
//                    System.out.printf("-Collided %f %f %f x=%f y=%f\n  %s <> %s\n", l, r, nudge, x, y, circle, possible);
                    orb.setX(orb.getX() - x);
                    orb.setY(orb.getY() - y);
                    possible.setX(possible.getX() + x);
                    possible.setY(possible.getY() + y);
                }
            }
        }

        return collided;
    }

    @Override
    public String toString() {
        return String.format("[QTree level=%d size=%d %s]", level, objects.size(), box);
    }
}
