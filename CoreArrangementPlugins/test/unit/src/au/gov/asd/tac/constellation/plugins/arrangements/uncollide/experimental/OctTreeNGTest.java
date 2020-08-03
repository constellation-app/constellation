/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.plugins.arrangements.uncollide.experimental;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import java.util.ArrayList;
import java.util.List;
import static org.testng.Assert.*;
import static org.assertj.core.api.Assertions.assertThat;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author liam.banks
 */
public class OctTreeNGTest {
    
    private static OctTree twoTwinsQT;
    private static int twoTwinsSubject;
    private static int twin1;
    private static int twin2;
    private static OctTree noCollisionsQT;
    private static int noCollisionSubject;
    private static int distantVertex1;
    private static int distantVertex2;

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
        
        
        twoTwinsQT = new OctTree(graphWithTwoTwins);
    
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
        // distant vertex
        distantVertex1 = graphWithNoCollisions.addVertex();
        graphWithNoCollisions.setFloatValue(attrX, distantVertex1, 1000f);
        graphWithNoCollisions.setFloatValue(attrY, distantVertex1, 1000f);
        graphWithNoCollisions.setFloatValue(attrZ, distantVertex1, 1000f);
        graphWithNoCollisions.setFloatValue(attrR, distantVertex1, 1.0f);
        // second distant vertex
        distantVertex2 = graphWithNoCollisions.addVertex();
        graphWithNoCollisions.setFloatValue(attrX, distantVertex2, -1000f);
        graphWithNoCollisions.setFloatValue(attrY, distantVertex2, -1000f);
        graphWithNoCollisions.setFloatValue(attrZ, distantVertex2, -1000f);
        graphWithNoCollisions.setFloatValue(attrR, distantVertex2, 1.0f);
        
        noCollisionsQT = new OctTree(graphWithNoCollisions);
    }

    /**
     * Test of split method, of class OctTree.
     */
    @Test
    public void testSplit() {
        BoundingBox3D box3D = (BoundingBox3D) twoTwinsQT.box;
        
        OctTree[] nodes = new OctTree[8];
        nodes[OctTree.TOP_R_F] = new OctTree(twoTwinsQT, box3D.topRightFrontOctant());
        nodes[OctTree.TOP_L_F] = new OctTree(twoTwinsQT, box3D.topLeftFrontOctant());
        nodes[OctTree.BOT_L_F] = new OctTree(twoTwinsQT, box3D.bottomLeftFrontOctant());
        nodes[OctTree.BOT_R_F] = new OctTree(twoTwinsQT, box3D.bottomRightFrontOctant());
        nodes[OctTree.TOP_R_B] = new OctTree(twoTwinsQT, box3D.topRightBackOctant());
        nodes[OctTree.TOP_L_B] = new OctTree(twoTwinsQT, box3D.topLeftBackOctant());
        nodes[OctTree.BOT_L_B] = new OctTree(twoTwinsQT, box3D.bottomLeftBackOctant());
        nodes[OctTree.BOT_R_B] = new OctTree(twoTwinsQT, box3D.bottomRightBackOctant());
        
        twoTwinsQT.split();
        assertThat(twoTwinsQT.nodes)
            .usingRecursiveComparison()
            .isEqualTo(nodes);
        
    }

    /**
     * Test of getIndex method, of class OctTree.
     */
    @Test
    public void testGetIndex() {
        assertEquals(twoTwinsQT.getIndex(twoTwinsSubject), -1);
        assertEquals(noCollisionsQT.getIndex(distantVertex1), OctTree.TOP_R_F);
        assertEquals(noCollisionsQT.getIndex(distantVertex2), OctTree.BOT_L_B);
    }

    /**
     * Test of nodeCollides method, of class OctTree.
     */
    @Test
    public void testNodeCollides() {
        assertEquals(twoTwinsQT.nodeCollides(twoTwinsSubject), true);
        assertEquals(noCollisionsQT.nodeCollides(noCollisionSubject), false);
    }

    /**
     * Test of getTwins method, of class OctTree.
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
     * Test of hasCollision method, of class OctTree.
     */
    @Test
    public void testHasCollision() {
        System.out.println("findCollision");

        assertEquals(noCollisionsQT.hasCollision(), false);
        assertEquals(twoTwinsQT.hasCollision(), true);
    }
    
}
