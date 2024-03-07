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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Nova
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class BoundingBox2DNGTest extends ConstellationTest {

    private static StoreGraph baseGraph;

    @BeforeClass
    public static void generateGraph() {
        baseGraph = new StoreGraph();

        int attrX = VisualConcept.VertexAttribute.X.ensure(baseGraph);
        if (attrX == Graph.NOT_FOUND) {
            fail();
        }

        int attrY = VisualConcept.VertexAttribute.Y.ensure(baseGraph);
        if (attrY == Graph.NOT_FOUND) {
            fail();
        }

        // Top left
        int vxId1 = baseGraph.addVertex();
        baseGraph.setFloatValue(attrX, vxId1, -1.0f);
        baseGraph.setFloatValue(attrY, vxId1, 1.0f);
        // Top right
        int vxId2 = baseGraph.addVertex();
        baseGraph.setFloatValue(attrX, vxId2, 1.0f);
        baseGraph.setFloatValue(attrY, vxId2, 1.0f);
        // Bottom right
        int vxId3 = baseGraph.addVertex();
        baseGraph.setFloatValue(attrX, vxId3, 1.0f);
        baseGraph.setFloatValue(attrY, vxId3, -1.0f);
        // Bottom left
        int vxId4 = baseGraph.addVertex();
        baseGraph.setFloatValue(attrX, vxId4, -1.0f);
        baseGraph.setFloatValue(attrY, vxId4, -1.0f);
    }

    /**
     * Test the constructor works when given a graph with verticies
     */
    @Test
    public void testConstructor() {
        BoundingBox2D actual = new BoundingBox2D(baseGraph);

        assertEquals(actual.minX, -1.0f);
        assertEquals(actual.midX, 0.0f);
        assertEquals(actual.maxX, 1.0f);
        assertEquals(actual.minY, -1.0f);
        assertEquals(actual.midY, 0.0f);
        assertEquals(actual.maxY, 1.0f);
    }

    /**
     * Test the constructor throws an exception when given an empty graph.
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testConstructorThrowsException() {
        StoreGraph emptyGraph = new StoreGraph();
        VisualConcept.VertexAttribute.X.ensure(emptyGraph);
        VisualConcept.VertexAttribute.Y.ensure(emptyGraph);

        new BoundingBox2D(emptyGraph); // This should fail as the graph has no verticies.
    }

    /**
     * Test of getTopLeftQuadrant method, of class BoundingBox2D.
     */
    @Test
    public void testGetTopLeftQuadrant() {
        System.out.println("getTopLeftQuadrant");
        BoundingBox2D base = new BoundingBox2D(baseGraph);
        BoundingBox2D actualResult = base.topLeftQuadrant();
        assertEquals(actualResult.minX, -1.0f);
        assertEquals(actualResult.midX, -0.5f);
        assertEquals(actualResult.maxX, 0.0f);
        assertEquals(actualResult.minY, 0.0f);
        assertEquals(actualResult.midY, 0.5f);
        assertEquals(actualResult.maxY, 1.0f);
    }

    /**
     * Test of getTopRightQuadrant method, of class BoundingBox2D.
     */
    @Test
    public void testTopRightQuadrant() {
        System.out.println("getTopRightQuadrant");
        BoundingBox2D base = new BoundingBox2D(baseGraph);
        BoundingBox2D actualResult = base.topRightQuadrant();
        assertEquals(actualResult.minX, 0.0f);
        assertEquals(actualResult.midX, 0.5f);
        assertEquals(actualResult.maxX, 1.0f);
        assertEquals(actualResult.minY, 0.0f);
        assertEquals(actualResult.midY, 0.5f);
        assertEquals(actualResult.maxY, 1.0f);
    }

    /**
     * Test of getBottomLeftQuadrant method, of class BoundingBox2D.
     */
    @Test
    public void testGetBottomLeftQuadrant() {
        System.out.println("getBottomLeftQuadrant");
        BoundingBox2D base = new BoundingBox2D(baseGraph);
        BoundingBox2D actualResult = base.bottomLeftQuadrant();
        assertEquals(actualResult.minX, -1.0f);
        assertEquals(actualResult.midX, -0.5f);
        assertEquals(actualResult.maxX, 0.0f);
        assertEquals(actualResult.minY, -1.0f);
        assertEquals(actualResult.midY, -0.5f);
        assertEquals(actualResult.maxY, 0.0f);
    }

    /**
     * Test of getBottomRightQuadrant method, of class BoundingBox2D.
     */
    @Test
    public void testBottomRightQuadrant() {
        System.out.println("getBottomRightQuadrant");
        BoundingBox2D base = new BoundingBox2D(baseGraph);
        BoundingBox2D actualResult = base.bottomRightQuadrant();
        assertEquals(actualResult.minX, 0.0f);
        assertEquals(actualResult.midX, 0.5f);
        assertEquals(actualResult.maxX, 1.0f);
        assertEquals(actualResult.minY, -1.0f);
        assertEquals(actualResult.midY, -0.5f);
        assertEquals(actualResult.maxY, 0.0f);
    }

}
