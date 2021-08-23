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

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Nova
 */
public class UncollideArrangementNGTest {

    /**
     * Test of arrange method, of class UncollideArrangement.
     *
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
        // Same Location
        int twoTwinsSameLocation = graphWithTwoTwins.addVertex();
        graphWithTwoTwins.setFloatValue(attrX, twoTwinsSameLocation, 0.0f);
        graphWithTwoTwins.setFloatValue(attrY, twoTwinsSameLocation, 0.0f);
        graphWithTwoTwins.setFloatValue(attrR, twoTwinsSameLocation, 1.0f);
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
        final UncollideArrangement arranger = new UncollideArrangement(Dimensions.TWO, 20);
        arranger.arrange(graphWithTwoTwins);
        qt = new QuadTree(graphWithTwoTwins);
        assertEquals(qt.hasCollision(), false); //Assert that after the uncollide arrangement has run there is no longer a collision.
    }

    /**
     * Test of arrange method, of class UncollideArrangement.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testArrange3D() throws Exception {
        System.out.println("arrange3D");

        StoreGraph graphWithTwoTwins = new StoreGraph();

        int attrX = VisualConcept.VertexAttribute.X.ensure(graphWithTwoTwins);
        int attrY = VisualConcept.VertexAttribute.Y.ensure(graphWithTwoTwins);
        int attrZ = VisualConcept.VertexAttribute.Z.ensure(graphWithTwoTwins);
        int attrR = VisualConcept.VertexAttribute.NODE_RADIUS.ensure(graphWithTwoTwins);

        // Zero vertex
        int twoTwinsSubject = graphWithTwoTwins.addVertex();
        graphWithTwoTwins.setFloatValue(attrX, twoTwinsSubject, 0.0f);
        graphWithTwoTwins.setFloatValue(attrY, twoTwinsSubject, 0.0f);
        graphWithTwoTwins.setFloatValue(attrZ, twoTwinsSubject, 0.0f);
        graphWithTwoTwins.setFloatValue(attrR, twoTwinsSubject, 1.0f);
        // Same Location
        int twoTwinsSameLocation = graphWithTwoTwins.addVertex();
        graphWithTwoTwins.setFloatValue(attrX, twoTwinsSameLocation, 0.0f);
        graphWithTwoTwins.setFloatValue(attrY, twoTwinsSameLocation, 0.0f);
        graphWithTwoTwins.setFloatValue(attrZ, twoTwinsSameLocation, 0.0f);
        graphWithTwoTwins.setFloatValue(attrR, twoTwinsSameLocation, 1.0f);
        // First twin
        int twin1 = graphWithTwoTwins.addVertex();
        graphWithTwoTwins.setFloatValue(attrX, twin1, 0.1f);
        graphWithTwoTwins.setFloatValue(attrY, twin1, 0.1f);
        graphWithTwoTwins.setFloatValue(attrZ, twin1, 0.1f);
        graphWithTwoTwins.setFloatValue(attrR, twin1, 1.0f);
        // Second twin
        int twin2 = graphWithTwoTwins.addVertex();
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

        OctTree ot = new OctTree(graphWithTwoTwins);
        assertEquals(ot.hasCollision(), true); //Assert that before the uncollide arrangement is run the graph has a collision.
        final UncollideArrangement arranger = new UncollideArrangement(Dimensions.THREE, 20);
        arranger.arrange(graphWithTwoTwins);
        ot = new OctTree(graphWithTwoTwins);
        assertEquals(ot.hasCollision(), false); //Assert that after the uncollide arrangement has run there is no longer a collision.
    }

}
