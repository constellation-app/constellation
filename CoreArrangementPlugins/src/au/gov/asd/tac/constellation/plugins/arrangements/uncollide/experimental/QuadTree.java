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

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import org.python.modules.math;

/**
 * http://gamedev.tutsplus.com/tutorials/implementation/quick-tip-use-quadtrees-to-detect-likely-collisions-in-2d-space/
 *
 * @author algol
 * @author Nova
 */
public class QuadTree extends AbstractTree {

    protected static final int TOP_R = 0;
    protected static final int TOP_L = 1;
    protected static final int BOT_L = 2;
    protected static final int BOT_R = 3;

    /**
     * Constructor creates QuadTree and inserts all nodes
     *
     * @param graph The graph the QuadTree should be based on
     */
    protected QuadTree(final GraphReadMethods graph) {
        super(graph, Dimensions.TWO);
        this.box = new BoundingBox2D(graph);
        insertAll();
    }

    /**
     * Create a subtree of the current tree
     *
     * @param parent
     * @param box
     */
    protected QuadTree(final QuadTree parent, final BoundingBox2D box) {
        super(parent, box);
    }

    /*
     * Splits the node into four subnodes.
     * <p>
     * Divide the node into four equal parts and initialise the four subnodes with the new bounds.
     */
    @Override
    protected void split() {
        final BoundingBox2D box2D = (BoundingBox2D) this.box;
        nodes = new QuadTree[4];
        nodes[TOP_R] = new QuadTree(this, box2D.topRightQuadrant());
        nodes[TOP_L] = new QuadTree(this, box2D.topLeftQuadrant());
        nodes[BOT_L] = new QuadTree(this, box2D.bottomLeftQuadrant());
        nodes[BOT_R] = new QuadTree(this, box2D.bottomRightQuadrant());
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
        int index = -1;

        // Object can completely fit within the top/bottom halves.
        final boolean bottomHalf = wg.getFloatValue(yId, vxId) + wg.getFloatValue(rId, vxId) < box.midY;
        final boolean topHalf = wg.getFloatValue(yId, vxId) - wg.getFloatValue(rId, vxId) > box.midY;

        // Object can completely fit witin the left/right halves.
        final boolean leftHalf = wg.getFloatValue(xId, vxId) + wg.getFloatValue(rId, vxId) < box.midX;
        final boolean rightHalf = wg.getFloatValue(xId, vxId) - wg.getFloatValue(rId, vxId) > box.midX;

        // Object can completely fit within the left half.
        if (leftHalf) {
            if (topHalf) {
                index = TOP_L; // fits in top left quadrant
            } else if (bottomHalf) {
                index = BOT_L; // fits in bottom left quadrant
            } else {
                // Do nothing
            }
        } else if (rightHalf) {
            // Object can completely fit within the right half.
            if (topHalf) {
                index = TOP_R; // fits in top right quadrant
            } else if (bottomHalf) {
                index = BOT_R; // fits in bottom right quadrant
            } else {
                // Do nothing
            }
        } else {
            // Do nothing
            return index;
        }
        return index;
    }

    @Override
    protected double getDelta(final int vertex1, final int vertex2) {
        float deltaX = wg.getFloatValue(xId, vertex1) - wg.getFloatValue(xId, vertex2);
        float deltaY = wg.getFloatValue(yId, vertex1) - wg.getFloatValue(yId, vertex2);
        return math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    @Override
    protected double getCollisionDistance(final int vertex1, final int vertex2) {
        return math.sqrt(2 * wg.getFloatValue(rId, vertex1)) + math.sqrt(2 * wg.getFloatValue(rId, vertex2));
    }
}
