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
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author liam.banks
 */
public class QuadTreeNGTest {
    private static QuadTree twoTwinsQT;
    private static int twoTwinsSubject;
    private static int twin1;
    private static int twin2;
    private static QuadTree noCollisionsQT;
    private static int noCollisionSubject;

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
        
        twoTwinsQT = new QuadTree(graphWithTwoTwins);
    
        StoreGraph graphWithNoCollisions = new StoreGraph();
        
        attrX = VisualConcept.VertexAttribute.X.ensure(graphWithNoCollisions);
        attrY = VisualConcept.VertexAttribute.Y.ensure(graphWithNoCollisions);
        attrZ = VisualConcept.VertexAttribute.Z.ensure(graphWithNoCollisions);
        attrR = VisualConcept.VertexAttribute.NODE_RADIUS.ensure(graphWithNoCollisions);

        // Zero vertex
        noCollisionSubject = graphWithNoCollisions.addVertex();
        graphWithNoCollisions.setFloatValue(attrX, noCollisionSubject, 0.0f);
        graphWithNoCollisions.setFloatValue(attrY, noCollisionSubject, 0.0f);
        graphWithNoCollisions.setFloatValue(attrR, noCollisionSubject, 1.0f);
        // distant vertex
        int distantVertex1 = graphWithNoCollisions.addVertex();
        graphWithNoCollisions.setFloatValue(attrX, distantVertex1, 1000f);
        graphWithNoCollisions.setFloatValue(attrY, distantVertex1, 1000f);
        graphWithNoCollisions.setFloatValue(attrR, distantVertex1, 1.0f);
        // second distant vertex
        int distantVertex2 = graphWithNoCollisions.addVertex();
        graphWithNoCollisions.setFloatValue(attrX, distantVertex2, -1000f);
        graphWithNoCollisions.setFloatValue(attrY, distantVertex2, -1000f);
        graphWithNoCollisions.setFloatValue(attrR, distantVertex2, 1.0f);
        
        noCollisionsQT = new QuadTree(graphWithNoCollisions);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
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
     * Test of findCollision method, of class QuadTree.
     */
    @Test
    public void testHasCollision() {
        System.out.println("findCollision");

        assertEquals(noCollisionsQT.hasCollision(), false);
        assertEquals(twoTwinsQT.hasCollision(), true);
    }
    
}
