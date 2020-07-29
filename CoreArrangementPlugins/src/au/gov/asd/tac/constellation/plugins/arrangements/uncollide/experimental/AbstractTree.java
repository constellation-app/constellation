/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.plugins.arrangements.uncollide.experimental;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.python.modules.math;

/**
 * This class provides an abstract representation of a Tree designed to help
 * detect collisions quickly by dividing an X-dimensional space into 2^X
 * sub-spaces. The minimum value of X is 2.
 * 
 * As constellation only
 * @author liam.banks
 */
public abstract class AbstractTree {
    protected static final int MAX_OBJECTS = 10;
    protected static final int MAX_LEVELS = 5;
    
    protected final int XID; 
    protected final int YID;
    protected final int RID;
    protected final GraphReadMethods wg;

    protected final int level;
    protected AbstractBoundingBox box;
    protected List<Integer> objects;
    protected AbstractTree[] nodes;
    
    /**
     * Constructor creates QuadTree and inserts all nodes
     * 
     * @param graph  The graph the QuadTree should be based on
     */
    AbstractTree(final GraphReadMethods graph) {
        this.level = 0;
        this.objects = new ArrayList<>();
        this.nodes = null;
        
        this.wg = graph;
        this.XID = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName());
        this.YID = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName());
        this.RID = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.NODE_RADIUS.getName());
        
        insertAll();
    }
    
    /**
     * Create a subtree of the current tree
     * 
     * @param parent
     * @param box 
     */
    AbstractTree(AbstractTree parent, final AbstractBoundingBox box) {
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
     * Splits the node into 2^X subnodes.
     * <p>
     * Divide the node into 2^X equal parts and initialise the 2^X subnodes with the new bounds.
     */
    abstract protected void split();
    
    /*
     * Determine which node the object belongs to.
     * <p>
     * -1 means object cannot completely fit within a child node and is part of the parent node.
     * <p>
     * Determine where an object belongs in the quadtree by determining which node the object can fit into.
     */
    abstract protected int getIndex(final int vxId);
    
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
    abstract protected boolean nodeCollides(final int subject);
    
    /**
     * Check the subject for "twin" verticies
     * 
     * A twin vertex is defined as a vertex that falls within "twinThreshold
     *  x collision distance" of the subject.
     * 
     * @param subject  The id of the vertex you wish to check for twins.
     * @param twinThreshold A scaling factor for the collision distance within 
     * which the two noes are considered to be "twins". That is the distance
     * between them is so insignificant that we consider them in the same spot.
     * 
     * @return  A set of vertex ideas for verticies that are twins with the subject
     */
    abstract public List<Integer> getTwins(final int subject, final double twinThreshold);
    
    /*
     * Insert the object into the tree. If the node exceeds the capacity, it will split and add
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
    
    /**
     * Insert all verticies in the graph into the tree.
     */
    private void insertAll() {
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
     * @return  boolean indicating whether the graph contains colliding verticies
     */
    public final boolean hasCollision(){
        for (int position = 0; position < wg.getVertexCount(); position++) {
            if(nodeCollides(wg.getVertex(position))) {
                return true;
            }
        }
        return false;
    }
    
}
