/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.plugins.arrangements.uncollide.experimental;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author liam.banks
 */
public class UncollideArrangementNGTest {
    
    /**
     * Test of arrange method, of class UncollideArrangement.
     * @throws java.lang.Exception
     */
    @Test
    public void testArrange2D() throws Exception {
        System.out.println("arrange2D");
        
        StoreGraph graphWithTwoTwins = new StoreGraph();
        
        int attrX = VisualConcept.VertexAttribute.X.ensure(graphWithTwoTwins);
        int attrY = VisualConcept.VertexAttribute.Y.ensure(graphWithTwoTwins);
        int attrR = VisualConcept.VertexAttribute.NODE_RADIUS.ensure(graphWithTwoTwins);

        // Zero vertex
        int twoTwinsSubject = graphWithTwoTwins.addVertex();
        graphWithTwoTwins.setFloatValue(attrX, twoTwinsSubject, 0.0f);
        graphWithTwoTwins.setFloatValue(attrY, twoTwinsSubject, 0.0f);
        graphWithTwoTwins.setFloatValue(attrR, twoTwinsSubject, 1.0f);
        // First twin
        int twin1 = graphWithTwoTwins.addVertex();
        graphWithTwoTwins.setFloatValue(attrX, twin1, 0.1f);
        graphWithTwoTwins.setFloatValue(attrY, twin1, 0.1f);
        graphWithTwoTwins.setFloatValue(attrR, twin1, 1.0f);
        // Second twin
        int twin2 = graphWithTwoTwins.addVertex();
        graphWithTwoTwins.setFloatValue(attrX, twin2, -0.1f);
        graphWithTwoTwins.setFloatValue(attrY, twin2, -0.1f);
        graphWithTwoTwins.setFloatValue(attrR, twin2, 1.0f);
        // Non-twin collider
        int collider = graphWithTwoTwins.addVertex();
        graphWithTwoTwins.setFloatValue(attrX, collider, 1.0f);
        graphWithTwoTwins.setFloatValue(attrY, collider, 1.0f);
        graphWithTwoTwins.setFloatValue(attrR, collider, 1.0f);
        // Non-Collider
        int noncollider = graphWithTwoTwins.addVertex();
        graphWithTwoTwins.setFloatValue(attrX, collider, 1.0f);
        graphWithTwoTwins.setFloatValue(attrY, collider, 1.0f);
        graphWithTwoTwins.setFloatValue(attrR, collider, 1.0f);
        
        QuadTree qt = new QuadTree(graphWithTwoTwins);
        assertEquals(qt.hasCollision(), true); //Assert that before the uncollide arrangement is run the graph has a collision. 
        final UncollideArrangement arranger = new UncollideArrangement(Dimensions.Two, 20);
        arranger.arrange(graphWithTwoTwins);
        qt = new QuadTree(graphWithTwoTwins);
        assertEquals(qt.hasCollision(), false); //Assert that after the uncollide arrangement has run there is no longer a collision. 
        
    } 
}
