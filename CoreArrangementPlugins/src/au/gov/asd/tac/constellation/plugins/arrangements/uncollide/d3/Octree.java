/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.arrangements.uncollide.d3;

import au.gov.asd.tac.constellation.plugins.arrangements.uncollide.d3.BoundingBox3D.Box3D;
import java.util.ArrayList;
import java.util.List;

/**
 * http://gamedev.tutsplus.com/tutorials/implementation/quick-tip-use-quadtrees-to-detect-likely-collisions-in-2d-space/
 *
 * @author algol
 */
public class Octree {

    private static final int MAX_OBJECTS = 10;
    private static final int MAX_LEVELS = 5;

    // Top/bottom, left/right, front/back; top-left-back is (0, 0, 0).
    private static final int TOP_R_F = 0;
    private static final int TOP_L_F = 1;
    private static final int BOT_L_F = 2;
    private static final int BOT_R_F = 3;
    private static final int TOP_R_B = 4;
    private static final int TOP_L_B = 5;
    private static final int BOT_L_B = 6;
    private static final int BOT_R_B = 7;

    private final int level;
    private final List<Orb3D> objects;
    private final Box3D box;
    private Octree[] nodes;

    public Octree(final Box3D box) {
        this(0, box);
    }

    /*
     * Constructor
     */
    public Octree(final int level, final Box3D box) {
        this.level = level;
        this.box = box;
        objects = new ArrayList<>();
        nodes = null;
    }

    public List<Box3D> getSubs() {
        final List<Box3D> boxes = new ArrayList<>();
        getSubs(boxes);

        return boxes;
    }

    private void getSubs(final List<Box3D> boxes) {
        boxes.add(box);
        for (final Octree qt : nodes) {
            if (qt != null) {
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
     * Splits the node into eight subnodes.
     * <p>
     * Divide the node into four equal parts and initialise the four subnodes with the new bounds.
     */
    private void split() {
        final float minx = box.minx;
        final float miny = box.miny;
        final float minz = box.minz;
        final float maxx = box.maxx;
        final float maxy = box.maxy;
        final float maxz = box.maxz;
        final float midx = minx + (maxx - minx) / 2;
        final float midy = miny + (maxy - miny) / 2;
        final float midz = minz + (maxz - minz) / 2;

        nodes = new Octree[8];
        nodes[TOP_R_F] = new Octree(level + 1, new Box3D(midx, miny, midz, maxx, midy, maxz));
        nodes[TOP_L_F] = new Octree(level + 1, new Box3D(minx, miny, midz, midx, midy, maxz));
        nodes[BOT_L_F] = new Octree(level + 1, new Box3D(minx, midy, midz, midx, maxy, maxz));
        nodes[BOT_R_F] = new Octree(level + 1, new Box3D(midx, midy, midz, maxx, maxy, maxz));
        nodes[TOP_R_B] = new Octree(level + 1, new Box3D(midx, miny, minz, minz, midy, midz));
        nodes[TOP_L_B] = new Octree(level + 1, new Box3D(minx, miny, minz, midx, midy, midz));
        nodes[BOT_L_B] = new Octree(level + 1, new Box3D(minx, midy, minz, midx, maxy, midz));
        nodes[BOT_R_B] = new Octree(level + 1, new Box3D(midx, midy, minz, maxx, maxy, midz));
    }

    /*
     * Determine which node the object belongs to.
     * <p>
     * -1 means object cannot completely fit within a child node and is part of the parent node.
     * <p>
     * Determine where an object belongs in the quadtree by determining which node the object can fit into.
     */
    private int getIndex(final Orb3D orb) {
        int index = -1;
        final double midx = box.minx + ((box.maxx - box.minx) / 2F);
        final double midy = box.miny + ((box.miny - box.maxy) / 2F);
        final double midz = box.minz + ((box.minz - box.maxz) / 2F);

        // Object can completely fit within the top/bottom quadrants.
        final boolean topQuadrant = orb.getY() + orb.r < midy;
        final boolean bottomQuadrant = orb.getY() - orb.r > midy;

        // Object can completely fit within the front/back quadrants.
        final boolean frontQuadrant = orb.getZ() - orb.r > midz;
        final boolean backQuadrant = orb.getZ() + orb.r < midz;

        // Object can completely fit within the left quadrants.
        if (orb.getX() + orb.r < midx) {
            if (topQuadrant) {
                if (frontQuadrant) {
                    index = TOP_L_F;
                } else {
                    index = backQuadrant ? TOP_L_B : -1;
                }
            } else if (bottomQuadrant) {
                if (frontQuadrant) {
                    index = BOT_L_F;
                } else {
                    index = backQuadrant ? BOT_L_B : -1;
                }
            }
        } else if (orb.getX() - orb.r > midx) {
            // Object can completely fit within the right quadrants.
            if (topQuadrant) {
                if (frontQuadrant) {
                    index = TOP_R_F;
                } else {
                    index = backQuadrant ? TOP_R_B : -1;
                }
            } else if (bottomQuadrant) {
                if (frontQuadrant) {
                    index = BOT_R_F;
                } else {
                    index = backQuadrant ? BOT_R_B : -1;
                }
            }
        } else {
            return index;
        }

        return index;
    }

    /*
     * Insert the object into the quadtree. If the node exceeds the capacity, it will split and add
     * objects that fit to their corresponding nodes.
     */
    public void insert(final Orb3D orb) {
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
    public List<Orb3D> getPossibleColliders(final List<Orb3D> colliders, final Orb3D orb) {
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
     * @param padding The minimum orb between the orb's edge and the edges of
     * each neighbor.
     * @return the number of collisions.
     */
    public int uncollide(final Orb3D orb, final float padding) {
        final List<Orb3D> possibles = new ArrayList<>();
        getPossibleColliders(possibles, orb);

        // We need to deal with pathological cases such as everything at the same x,y point,
        // or everything co-linear.
        // We add a perturbation so points go different ways at different stages.
        float perturbation = 1e-4F;
        int collided = 0;
        for (final Orb3D possible : possibles) {
            if (orb != possible) {
                float x = orb.getX() - possible.getX();
                float y = orb.getY() - possible.getY();
                float z = orb.getZ() - possible.getZ();
                final double ll = x * x + y * y + z * z;
                final double r = possible.r + orb.r + padding;
                if (ll <= r * r) {
                    final double l = Math.sqrt(ll);
                    collided++;
                    final float nudge = l != 0 ? (float) Math.min((l - r) / l * 0.5, -0.1) : -0.1F;
                    x *= nudge;
                    x += perturbation;
                    y *= nudge;
                    y += perturbation;
                    z *= nudge;
                    z += perturbation;
                    perturbation = -perturbation;
                    orb.setX(orb.getX() - x);
                    orb.setY(orb.getY() - y);
                    orb.setZ(orb.getZ() - z);
                    possible.setX(possible.getX() + x);
                    possible.setY(possible.getY() + y);
                    possible.setZ(possible.getZ() + z);
                }
            }
        }

        return collided;
    }
}
