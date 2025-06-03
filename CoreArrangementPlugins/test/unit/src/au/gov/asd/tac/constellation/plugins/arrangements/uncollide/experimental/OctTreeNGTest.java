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
package au.gov.asd.tac.constellation.plugins.arrangements.uncollide.experimental;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Nova
 */
public class OctTreeNGTest {

    private static OctTree twoTwinsOT;
    private static int twoTwinsSubject;
    private static int twin1;
    private static int twin2;
    private static OctTree noCollisionsOT;
    private static int noCollisionSubject;
    private static int distantVertexTRF;
    private static int distantVertexTRB;
    private static int distantVertexTLF;
    private static int distantVertexTLB;
    private static int distantVertexBRF;
    private static int distantVertexBRB;
    private static int distantVertexBLF;
    private static int distantVertexBLB;

    @BeforeClass
    public static void setUpClass() throws Exception {
        StoreGraph graphWithTwoTwins = new StoreGraph();

        int attrX = VisualConcept.VertexAttribute.X.ensure(graphWithTwoTwins);
        int attrY = VisualConcept.VertexAttribute.Y.ensure(graphWithTwoTwins);
        int attrZ = VisualConcept.VertexAttribute.Z.ensure(graphWithTwoTwins);
        int attrR = VisualConcept.VertexAttribute.NODE_RADIUS.ensure(graphWithTwoTwins);

        // Zero vertex
        twoTwinsSubject = graphWithTwoTwins.addVertex();
        graphWithTwoTwins.setFloatValue(attrX, twoTwinsSubject, 0.0f);
        graphWithTwoTwins.setFloatValue(attrY, twoTwinsSubject, 0.0f);
        graphWithTwoTwins.setFloatValue(attrZ, twoTwinsSubject, 0.0f);
        graphWithTwoTwins.setFloatValue(attrR, twoTwinsSubject, 1.0f);
        
        // First twin
        twin1 = graphWithTwoTwins.addVertex();
        graphWithTwoTwins.setFloatValue(attrX, twin1, 0.1f);
        graphWithTwoTwins.setFloatValue(attrY, twin1, 0.1f);
        graphWithTwoTwins.setFloatValue(attrZ, twin1, 0.1f);
        graphWithTwoTwins.setFloatValue(attrR, twin1, 1.0f);
        
        // Second twin
        twin2 = graphWithTwoTwins.addVertex();
        graphWithTwoTwins.setFloatValue(attrX, twin2, -0.1f);
        graphWithTwoTwins.setFloatValue(attrY, twin2, -0.1f);
        graphWithTwoTwins.setFloatValue(attrZ, twin2, -0.1f);
        graphWithTwoTwins.setFloatValue(attrR, twin2, 1.0f);
        
        // Non-twin collider
        int collider = graphWithTwoTwins.addVertex();
        graphWithTwoTwins.setFloatValue(attrX, collider, 1.0f);
        graphWithTwoTwins.setFloatValue(attrY, collider, 1.0f);
        graphWithTwoTwins.setFloatValue(attrZ, collider, 1.0f);
        graphWithTwoTwins.setFloatValue(attrR, collider, 1.0f);

        twoTwinsOT = new OctTree(graphWithTwoTwins);

        StoreGraph graphWithNoCollisions = new StoreGraph();

        attrX = VisualConcept.VertexAttribute.X.ensure(graphWithNoCollisions);
        attrY = VisualConcept.VertexAttribute.Y.ensure(graphWithNoCollisions);
        attrZ = VisualConcept.VertexAttribute.Z.ensure(graphWithNoCollisions);
        attrR = VisualConcept.VertexAttribute.NODE_RADIUS.ensure(graphWithNoCollisions);

        // Zero vertex
        noCollisionSubject = graphWithNoCollisions.addVertex();
        graphWithNoCollisions.setFloatValue(attrX, noCollisionSubject, 0.0f);
        graphWithNoCollisions.setFloatValue(attrY, noCollisionSubject, 0.0f);
        graphWithNoCollisions.setFloatValue(attrY, noCollisionSubject, 0.0f);
        graphWithNoCollisions.setFloatValue(attrR, noCollisionSubject, 1.0f);
        
        // Distant top right front octant
        distantVertexTRF = graphWithNoCollisions.addVertex();
        graphWithNoCollisions.setFloatValue(attrX, distantVertexTRF, 1000f);
        graphWithNoCollisions.setFloatValue(attrY, distantVertexTRF, 1000f);
        graphWithNoCollisions.setFloatValue(attrZ, distantVertexTRF, 1000f);
        graphWithNoCollisions.setFloatValue(attrR, distantVertexTRF, 1.0f);
        
        // Distant top right back octant
        distantVertexTRB = graphWithNoCollisions.addVertex();
        graphWithNoCollisions.setFloatValue(attrX, distantVertexTRB, 1000f);
        graphWithNoCollisions.setFloatValue(attrY, distantVertexTRB, 1000f);
        graphWithNoCollisions.setFloatValue(attrZ, distantVertexTRB, -1000f);
        graphWithNoCollisions.setFloatValue(attrR, distantVertexTRB, 1.0f);
        
        // Distant top left front octant
        distantVertexTLF = graphWithNoCollisions.addVertex();
        graphWithNoCollisions.setFloatValue(attrX, distantVertexTLF, -1000f);
        graphWithNoCollisions.setFloatValue(attrY, distantVertexTLF, 1000f);
        graphWithNoCollisions.setFloatValue(attrZ, distantVertexTLF, 1000f);
        graphWithNoCollisions.setFloatValue(attrR, distantVertexTLF, 1.0f);
        
        // Distant top left back octant
        distantVertexTLB = graphWithNoCollisions.addVertex();
        graphWithNoCollisions.setFloatValue(attrX, distantVertexTLB, -1000f);
        graphWithNoCollisions.setFloatValue(attrY, distantVertexTLB, 1000f);
        graphWithNoCollisions.setFloatValue(attrZ, distantVertexTLB, -1000f);
        graphWithNoCollisions.setFloatValue(attrR, distantVertexTLB, 1.0f);
        
        // Distant bottom right front octant
        distantVertexBRF = graphWithNoCollisions.addVertex();
        graphWithNoCollisions.setFloatValue(attrX, distantVertexBRF, 1000f);
        graphWithNoCollisions.setFloatValue(attrY, distantVertexBRF, -1000f);
        graphWithNoCollisions.setFloatValue(attrZ, distantVertexBRF, 1000f);
        graphWithNoCollisions.setFloatValue(attrR, distantVertexBRF, 1.0f);
        
        // Distant bottom right back octant
        distantVertexBRB = graphWithNoCollisions.addVertex();
        graphWithNoCollisions.setFloatValue(attrX, distantVertexBRB, 1000f);
        graphWithNoCollisions.setFloatValue(attrY, distantVertexBRB, -1000f);
        graphWithNoCollisions.setFloatValue(attrZ, distantVertexBRB, -1000f);
        graphWithNoCollisions.setFloatValue(attrR, distantVertexBRB, 1.0f);
        
        // Distant bottom right back octant
        distantVertexBLF = graphWithNoCollisions.addVertex();
        graphWithNoCollisions.setFloatValue(attrX, distantVertexBLF, -1000f);
        graphWithNoCollisions.setFloatValue(attrY, distantVertexBLF, -1000f);
        graphWithNoCollisions.setFloatValue(attrZ, distantVertexBLF, 1000f);
        graphWithNoCollisions.setFloatValue(attrR, distantVertexBLF, 1.0f);
        
        // second distant vertex
        distantVertexBLB = graphWithNoCollisions.addVertex();
        graphWithNoCollisions.setFloatValue(attrX, distantVertexBLB, -1000f);
        graphWithNoCollisions.setFloatValue(attrY, distantVertexBLB, -1000f);
        graphWithNoCollisions.setFloatValue(attrZ, distantVertexBLB, -1000f);
        graphWithNoCollisions.setFloatValue(attrR, distantVertexBLB, 1.0f);

        noCollisionsOT = new OctTree(graphWithNoCollisions);
    }

    /**
     * Test of split method, of class OctTree.
     */
    @Test
    public void testSplit() {
        BoundingBox3D box3D = (BoundingBox3D) twoTwinsOT.box;

        OctTree[] nodes = new OctTree[8];
        nodes[OctTree.TOP_R_F] = new OctTree(twoTwinsOT, box3D.topRightFrontOctant());
        nodes[OctTree.TOP_L_F] = new OctTree(twoTwinsOT, box3D.topLeftFrontOctant());
        nodes[OctTree.BOT_L_F] = new OctTree(twoTwinsOT, box3D.bottomLeftFrontOctant());
        nodes[OctTree.BOT_R_F] = new OctTree(twoTwinsOT, box3D.bottomRightFrontOctant());
        nodes[OctTree.TOP_R_B] = new OctTree(twoTwinsOT, box3D.topRightBackOctant());
        nodes[OctTree.TOP_L_B] = new OctTree(twoTwinsOT, box3D.topLeftBackOctant());
        nodes[OctTree.BOT_L_B] = new OctTree(twoTwinsOT, box3D.bottomLeftBackOctant());
        nodes[OctTree.BOT_R_B] = new OctTree(twoTwinsOT, box3D.bottomRightBackOctant());

        twoTwinsOT.split();
        assertThat(twoTwinsOT.nodes)
                .usingRecursiveComparison()
                .isEqualTo(nodes);

    }

    /**
     * Test of getIndex method, of class OctTree.
     */
    @Test
    public void testGetIndex() {
        assertEquals(twoTwinsOT.getIndex(twoTwinsSubject), -1);
        assertEquals(noCollisionsOT.getIndex(distantVertexTRF), OctTree.TOP_R_F);
        assertEquals(noCollisionsOT.getIndex(distantVertexTRB), OctTree.TOP_R_B);
        assertEquals(noCollisionsOT.getIndex(distantVertexTLF), OctTree.TOP_L_F);
        assertEquals(noCollisionsOT.getIndex(distantVertexTLB), OctTree.TOP_L_B);
        assertEquals(noCollisionsOT.getIndex(distantVertexBRF), OctTree.BOT_R_F);
        assertEquals(noCollisionsOT.getIndex(distantVertexBRB), OctTree.BOT_R_B);
        assertEquals(noCollisionsOT.getIndex(distantVertexBLF), OctTree.BOT_L_F);
        assertEquals(noCollisionsOT.getIndex(distantVertexBLB), OctTree.BOT_L_B);
    }

    /**
     * Test of nodeCollides method, of class OctTree.
     */
    @Test
    public void testNodeCollides() {
        assertEquals(twoTwinsOT.nodeCollides(twoTwinsSubject), true);
        assertEquals(noCollisionsOT.nodeCollides(noCollisionSubject), false);
    }

    /**
     * Test of getTwins method, of class OctTree.
     */
    @Test
    public void testGetTwins() {
        System.out.println("getTwins");
        double twinThreshold = 0.5;

        List<Integer> expResult = new ArrayList<>();

        List<Integer> noCollisionResult = noCollisionsOT.getTwins(noCollisionSubject, twinThreshold);
        assertEquals(noCollisionResult, expResult); // Check empty set is returned if there are no collisions.

        expResult.add(twin1);
        expResult.add(twin2);
        List<Integer> result = twoTwinsOT.getTwins(twoTwinsSubject, twinThreshold);
        assertEquals(result, expResult);
    }

    /**
     * Test of hasCollision method, of class OctTree.
     */
    @Test
    public void testHasCollision() {
        System.out.println("findCollision");

        assertEquals(noCollisionsOT.hasCollision(), false);
        assertEquals(twoTwinsOT.hasCollision(), true);
    }
}
