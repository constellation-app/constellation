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
package au.gov.asd.tac.constellation.plugins.arrangements.uncollide.experimental;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Nova
 */
public class QuadTreeNGTest {

    private static QuadTree twoTwinsQT;
    private static int twoTwinsSubject;
    private static int twin1;
    private static int twin2;
    private static QuadTree noCollisionsQT;
    private static int noCollisionSubject;
    private static int distantTR;
    private static int distantTL;
    private static int distantBR;
    private static int distantBL;

    @BeforeClass
    public static void setUpClass() throws Exception {
        StoreGraph graphWithTwoTwins = new StoreGraph();

        int attrX = VisualConcept.VertexAttribute.X.ensure(graphWithTwoTwins);
        int attrY = VisualConcept.VertexAttribute.Y.ensure(graphWithTwoTwins);
        int attrR = VisualConcept.VertexAttribute.NODE_RADIUS.ensure(graphWithTwoTwins);

        // Zero vertex
        twoTwinsSubject = graphWithTwoTwins.addVertex();
        graphWithTwoTwins.setFloatValue(attrX, twoTwinsSubject, 0.0f);
        graphWithTwoTwins.setFloatValue(attrY, twoTwinsSubject, 0.0f);
        graphWithTwoTwins.setFloatValue(attrR, twoTwinsSubject, 1.0f);
        // First twin
        twin1 = graphWithTwoTwins.addVertex();
        graphWithTwoTwins.setFloatValue(attrX, twin1, 0.1f);
        graphWithTwoTwins.setFloatValue(attrY, twin1, 0.1f);
        graphWithTwoTwins.setFloatValue(attrR, twin1, 1.0f);
        // Second twin
        twin2 = graphWithTwoTwins.addVertex();
        graphWithTwoTwins.setFloatValue(attrX, twin2, -0.1f);
        graphWithTwoTwins.setFloatValue(attrY, twin2, -0.1f);
        graphWithTwoTwins.setFloatValue(attrR, twin2, 1.0f);
        // Non-twin collider
        int collider = graphWithTwoTwins.addVertex();
        graphWithTwoTwins.setFloatValue(attrX, collider, 1.0f);
        graphWithTwoTwins.setFloatValue(attrY, collider, 1.0f);
        graphWithTwoTwins.setFloatValue(attrR, collider, 1.0f);

        twoTwinsQT = new QuadTree(graphWithTwoTwins);

        StoreGraph graphWithNoCollisions = new StoreGraph();

        attrX = VisualConcept.VertexAttribute.X.ensure(graphWithNoCollisions);
        attrY = VisualConcept.VertexAttribute.Y.ensure(graphWithNoCollisions);
        attrR = VisualConcept.VertexAttribute.NODE_RADIUS.ensure(graphWithNoCollisions);

        // Zero vertex
        noCollisionSubject = graphWithNoCollisions.addVertex();
        graphWithNoCollisions.setFloatValue(attrX, noCollisionSubject, 0.0f);
        graphWithNoCollisions.setFloatValue(attrY, noCollisionSubject, 0.0f);
        graphWithNoCollisions.setFloatValue(attrR, noCollisionSubject, 1.0f);
        // distant vertex
        distantTR = graphWithNoCollisions.addVertex();
        graphWithNoCollisions.setFloatValue(attrX, distantTR, 1000f);
        graphWithNoCollisions.setFloatValue(attrY, distantTR, 1000f);
        graphWithNoCollisions.setFloatValue(attrR, distantTR, 1.0f);
        // second distant vertex
        distantBL = graphWithNoCollisions.addVertex();
        graphWithNoCollisions.setFloatValue(attrX, distantBL, -1000f);
        graphWithNoCollisions.setFloatValue(attrY, distantBL, -1000f);
        graphWithNoCollisions.setFloatValue(attrR, distantBL, 1.0f);
        // second distant vertex
        distantTL = graphWithNoCollisions.addVertex();
        graphWithNoCollisions.setFloatValue(attrX, distantTL, -1000f);
        graphWithNoCollisions.setFloatValue(attrY, distantTL, 1000f);
        graphWithNoCollisions.setFloatValue(attrR, distantTL, 1.0f);
        // second distant vertex
        distantBR = graphWithNoCollisions.addVertex();
        graphWithNoCollisions.setFloatValue(attrX, distantBR, 1000f);
        graphWithNoCollisions.setFloatValue(attrY, distantBR, -1000f);
        graphWithNoCollisions.setFloatValue(attrR, distantBR, 1.0f);

        noCollisionsQT = new QuadTree(graphWithNoCollisions);
    }

    /**
     * Test of getTwins method, of class QuadTree.
     */
    @Test
    public void testGetTwins() {
        System.out.println("getTwins");
        double twinThreshold = 0.5;

        List<Integer> expResult = new ArrayList<>();

        List<Integer> noCollisionResult = noCollisionsQT.getTwins(noCollisionSubject, twinThreshold);
        assertEquals(noCollisionResult, expResult); // Check empty set is returned if there are no collisions.

        expResult.add(twin1);
        expResult.add(twin2);
        List<Integer> result = twoTwinsQT.getTwins(twoTwinsSubject, twinThreshold);
        assertEquals(result, expResult);
    }

    /**
     * Test of hasCollision method, of class QuadTree.
     */
    @Test
    public void testHasCollision() {
        System.out.println("findCollision");

        assertEquals(noCollisionsQT.hasCollision(), false);
        assertEquals(twoTwinsQT.hasCollision(), true);
    }

    /**
     * Test of split method, of class QuadTree.
     */
    @Test
    public void testSplit() {
        BoundingBox2D box2D = (BoundingBox2D) twoTwinsQT.box;

        QuadTree[] nodes = new QuadTree[4];
        nodes[QuadTree.TOP_R] = new QuadTree(twoTwinsQT, box2D.topRightQuadrant());
        nodes[QuadTree.TOP_L] = new QuadTree(twoTwinsQT, box2D.topLeftQuadrant());
        nodes[QuadTree.BOT_L] = new QuadTree(twoTwinsQT, box2D.bottomLeftQuadrant());
        nodes[QuadTree.BOT_R] = new QuadTree(twoTwinsQT, box2D.bottomRightQuadrant());

        twoTwinsQT.split();
        assertThat(twoTwinsQT.nodes)
                .usingRecursiveComparison()
                .isEqualTo(nodes);
    }

    /**
     * Test of getIndex method, of class QuadTree.
     */
    @Test
    public void testGetIndex() {
        assertEquals(twoTwinsQT.getIndex(twoTwinsSubject), -1);
        assertEquals(noCollisionsQT.getIndex(distantTR), QuadTree.TOP_R);
        assertEquals(noCollisionsQT.getIndex(distantTL), QuadTree.TOP_L);
        assertEquals(noCollisionsQT.getIndex(distantBR), QuadTree.BOT_R);
        assertEquals(noCollisionsQT.getIndex(distantBL), QuadTree.BOT_L);
    }

    /**
     * Test of nodeCollides method, of class QuadTree.
     */
    @Test
    public void testNodeCollides() {
        assertEquals(twoTwinsQT.nodeCollides(twoTwinsSubject), true);
        assertEquals(noCollisionsQT.nodeCollides(noCollisionSubject), false);
    }

}
