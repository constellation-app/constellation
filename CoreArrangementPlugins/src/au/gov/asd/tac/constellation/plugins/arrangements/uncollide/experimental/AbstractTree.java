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
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides an abstract representation of a Tree designed to help
 * detect collisions quickly by dividing an X-dimensional space into 2^X
 * sub-spaces. The minimum value of X is 2.
 *
 * @author Nova
 */
public abstract class AbstractTree {

    protected static final int MAX_OBJECTS = 50;
    protected static final int MAX_LEVELS = 4;

    final int xId;
    final int yId;
    final int rId;
    final GraphReadMethods wg;

    final int level;
    AbstractBoundingBox box;
    List<Integer> objects;
    AbstractTree[] nodes;

    /**
     * Constructor creates QuadTree and inserts all nodes
     *
     * @param graph The graph the QuadTree should be based on
     */
    protected AbstractTree(final GraphReadMethods graph, final Dimensions d) {
        this.level = 0;
        this.objects = new ArrayList<>();
        this.nodes = null;
        this.box = BoxFactory.create(graph, d);

        this.wg = graph;
        this.xId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName());
        this.yId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName());
        this.rId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.NODE_RADIUS.getName());
    }

    /**
     * Create a subtree of the current tree
     *
     * @param parent
     * @param box
     */
    protected AbstractTree(final AbstractTree parent, final AbstractBoundingBox box) {
        this.level = parent.level + 1;
        this.box = box;
        objects = new ArrayList<>();
        nodes = null;
        // Inherit parent values for graph based variables.
        wg = parent.wg;
        xId = parent.xId;
        yId = parent.yId;
        rId = parent.rId;
    }

    /*
     * Splits the node into 2^X subnodes.
     * <p>
     * Divide the node into 2^X equal parts and initialise the 2^X subnodes with the new bounds.
     */
    protected abstract void split();

    /*
     * Determine which node the object belongs to.
     * <p>
     * -1 means object cannot completely fit within a child node and is part of the parent node.
     * <p>
     * Determine where an object belongs in the quadtree by determining which node the object can fit into.
     */
    protected abstract int getIndex(final int vxId);

    protected abstract double getDelta(final int vertex1, final int vertex2);

    protected abstract double getCollisionDistance(final int vertex1, final int vertex2);

    /*
     * Insert the object into the tree. If the node exceeds the capacity, it will split and add
     * objects that fit to their corresponding nodes.
     */
    private void insert(final int vxId) {
        if (nodes != null) { // if their are subnodes
            final int index = getIndex(vxId); // find the correct subnode

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
                final int index = getIndex(objects.get(i));
                if (index != -1) {
                    nodes[index].insert(objects.remove(i));
                } else {
                    i++;
                }
            }
        }
    }

    /**
     * Insert all verticies in the graph into the tree.
     */
    protected final void insertAll() {
        for (int position = 0; position < wg.getVertexCount(); position++) {
            insert(wg.getVertex(position));
        }
    }

    /*
     * Return all objects that could collide with the given object.
     */
    protected final List<Integer> getPossibleColliders(final List<Integer> colliders, final int vxId) {
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
     * Check the entire graph for collisions.
     *
     * @return boolean indicating whether the graph contains colliding verticies
     */
    public final boolean hasCollision() {
        for (int position = 0; position < wg.getVertexCount(); position++) {
            if (nodeCollides(wg.getVertex(position))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns boolean indicating whether or not the vertex collides with any
     * other verticies. Two verticies in exactly the same spot are not counted
     * as overlapping.
     *
     * @param subject The vertex to check for collisions.
     * @param padding The minimum distance between the vertex's edge and the
     * edges of each neighbor.
     * @return the number of collisions.
     */
    protected final boolean nodeCollides(final int subject) {
        final List<Integer> possibles = new ArrayList<>();
        getPossibleColliders(possibles, subject);

        // We need to deal with pathological cases such as everything at the same x,y point,
        // or everything co-linear.
        // We add a perturbation so points go different ways at different stages.
        for (final int possible : possibles) {
            if (subject != possible) {
                final double delta = getDelta(subject, possible);
                final double collisionDistance = getCollisionDistance(subject, possible);
                if (delta < collisionDistance) {
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
     * x (subject radius + twin radius + padding) of the subject. The average
     * radius is the average of the subject verticies radius and the potential
     * twins radius.
     *
     * @param subject The id of the vertex you wish to check for twins.
     * @param twinThreshold A scaling factor for the collision distance within
     * which the two noes are considered to be "twins". That is the distance
     * between them is so insignificant that we consider them in the same spot.
     *
     * @return A set of vertex ideas for verticies that are twins with the
     * subject
     */
    public List<Integer> getTwins(final int subject, final double twinThreshold) {
        final List<Integer> possibles = new ArrayList<>();
        getPossibleColliders(possibles, subject);
        final List<Integer> twins = new ArrayList<>();
        for (final int possible : possibles) {
            if (subject != possible) {
                final double delta = getDelta(subject, possible);
                final double collisionDistance = getCollisionDistance(subject, possible);
                final double twinDistance = collisionDistance * twinThreshold; // The required distance for the nodes to be uncollided
                if (delta < twinDistance) {
                    twins.add(possible);
                }
            }
        }
        return twins;
    }

}
