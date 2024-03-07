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
 public class BoundingBox3DNGTest extends ConstellationTest {

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

        int attrZ = VisualConcept.VertexAttribute.Z.ensure(baseGraph);
        if (attrZ == Graph.NOT_FOUND) {
            fail();
        }

        // Top left front
        int vxId1 = baseGraph.addVertex();
        baseGraph.setFloatValue(attrX, vxId1, -1.0f);
        baseGraph.setFloatValue(attrY, vxId1, 1.0f);
        baseGraph.setFloatValue(attrZ, vxId1, 1.0f);
        // Top right front
        int vxId2 = baseGraph.addVertex();
        baseGraph.setFloatValue(attrX, vxId2, 1.0f);
        baseGraph.setFloatValue(attrY, vxId2, 1.0f);
        baseGraph.setFloatValue(attrZ, vxId2, 1.0f);
        // Bottom right front
        int vxId3 = baseGraph.addVertex();
        baseGraph.setFloatValue(attrX, vxId3, 1.0f);
        baseGraph.setFloatValue(attrY, vxId3, -1.0f);
        baseGraph.setFloatValue(attrZ, vxId3, 1.0f);
        // Bottom left front
        int vxId4 = baseGraph.addVertex();
        baseGraph.setFloatValue(attrX, vxId4, -1.0f);
        baseGraph.setFloatValue(attrY, vxId4, -1.0f);
        baseGraph.setFloatValue(attrZ, vxId4, 1.0f);
        // Top left back
        int vxId5 = baseGraph.addVertex();
        baseGraph.setFloatValue(attrX, vxId5, -1.0f);
        baseGraph.setFloatValue(attrY, vxId5, 1.0f);
        baseGraph.setFloatValue(attrZ, vxId5, -1.0f);
        // Top right back
        int vxId6 = baseGraph.addVertex();
        baseGraph.setFloatValue(attrX, vxId6, 1.0f);
        baseGraph.setFloatValue(attrY, vxId6, 1.0f);
        baseGraph.setFloatValue(attrZ, vxId6, -1.0f);
        // Bottom right back
        int vxId7 = baseGraph.addVertex();
        baseGraph.setFloatValue(attrX, vxId7, 1.0f);
        baseGraph.setFloatValue(attrY, vxId7, -1.0f);
        baseGraph.setFloatValue(attrZ, vxId7, -1.0f);
        // Bottom left back
        int vxId8 = baseGraph.addVertex();
        baseGraph.setFloatValue(attrX, vxId8, -1.0f);
        baseGraph.setFloatValue(attrY, vxId8, -1.0f);
        baseGraph.setFloatValue(attrZ, vxId8, -1.0f);
    }

    /**
     * Test the constructor works when given a graph with verticies
     */
    @Test
    public void testConstructor() {
        BoundingBox3D actual = new BoundingBox3D(baseGraph);

        assertEquals(actual.minX, -1.0f);
        assertEquals(actual.midX, 0.0f);
        assertEquals(actual.maxX, 1.0f);
        assertEquals(actual.minY, -1.0f);
        assertEquals(actual.midY, 0.0f);
        assertEquals(actual.maxY, 1.0f);
        assertEquals(actual.minZ, -1.0f);
        assertEquals(actual.midZ, 0.0f);
        assertEquals(actual.maxZ, 1.0f);
    }

    /**
     * Test the constructor throws an exception when the graph is empty
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testConstructorThrowsException() {
        StoreGraph emptyGraph = new StoreGraph();
        VisualConcept.VertexAttribute.X.ensure(emptyGraph);
        VisualConcept.VertexAttribute.Y.ensure(emptyGraph);
        VisualConcept.VertexAttribute.Z.ensure(emptyGraph);

        new BoundingBox3D(emptyGraph); // This should fail as the graph has no verticies.
    }

    /**
     * Test of topLeftFrontOctant method, of class BoundingBox3D.
     */
    @Test
    public void testTopLeftFrontOctant() {
        System.out.println("getTopLeftFrontOctant");
        BoundingBox3D base = new BoundingBox3D(baseGraph);
        BoundingBox3D actualResult = base.topLeftFrontOctant();
        assertEquals(actualResult.minX, -1.0f);
        assertEquals(actualResult.midX, -0.5f);
        assertEquals(actualResult.maxX, 0.0f);
        assertEquals(actualResult.minY, 0.0f);
        assertEquals(actualResult.midY, 0.5f);
        assertEquals(actualResult.maxY, 1.0f);
        assertEquals(actualResult.minZ, 0.0f);
        assertEquals(actualResult.midZ, 0.5f);
        assertEquals(actualResult.maxZ, 1.0f);
    }

    /**
     * Test of topRightFrontOctant method, of class BoundingBox3D.
     */
    @Test
    public void testTopRightFrontOctant() {
        System.out.println("getTopRightFrontOctant");
        BoundingBox3D base = new BoundingBox3D(baseGraph);
        BoundingBox3D actualResult = base.topRightFrontOctant();
        assertEquals(actualResult.minX, 0.0f);
        assertEquals(actualResult.midX, 0.5f);
        assertEquals(actualResult.maxX, 1.0f);
        assertEquals(actualResult.minY, 0.0f);
        assertEquals(actualResult.midY, 0.5f);
        assertEquals(actualResult.maxY, 1.0f);
        assertEquals(actualResult.minZ, 0.0f);
        assertEquals(actualResult.midZ, 0.5f);
        assertEquals(actualResult.maxZ, 1.0f);
    }

    /**
     * Test of bottomLeftFrontOctant method, of class BoundingBox3D.
     */
    @Test
    public void testBottomLeftFrontOctant() {
        System.out.println("getBottomLeftFrontOctant");
        BoundingBox3D base = new BoundingBox3D(baseGraph);
        BoundingBox3D actualResult = base.bottomLeftFrontOctant();
        assertEquals(actualResult.minX, -1.0f);
        assertEquals(actualResult.midX, -0.5f);
        assertEquals(actualResult.maxX, 0.0f);
        assertEquals(actualResult.minY, -1.0f);
        assertEquals(actualResult.midY, -0.5f);
        assertEquals(actualResult.maxY, 0.0f);
        assertEquals(actualResult.minZ, 0.0f);
        assertEquals(actualResult.midZ, 0.5f);
        assertEquals(actualResult.maxZ, 1.0f);
    }

    /**
     * Test of bottomRightFrontOctant method, of class BoundingBox3D.
     */
    @Test
    public void testBottomRightFrontOctant() {
        System.out.println("getBottomRightFrontOctant");
        BoundingBox3D base = new BoundingBox3D(baseGraph);
        BoundingBox3D actualResult = base.bottomRightFrontOctant();
        assertEquals(actualResult.minX, 0.0f);
        assertEquals(actualResult.midX, 0.5f);
        assertEquals(actualResult.maxX, 1.0f);
        assertEquals(actualResult.minY, -1.0f);
        assertEquals(actualResult.midY, -0.5f);
        assertEquals(actualResult.maxY, 0.0f);
        assertEquals(actualResult.minZ, 0.0f);
        assertEquals(actualResult.midZ, 0.5f);
        assertEquals(actualResult.maxZ, 1.0f);
    }

    /**
     * Test of topLeftBackOctant method, of class BoundingBox3D.
     */
    @Test
    public void testTopLeftBackOctant() {
        System.out.println("getTopLeftBackOctant");
        BoundingBox3D base = new BoundingBox3D(baseGraph);
        BoundingBox3D actualResult = base.topLeftBackOctant();
        assertEquals(actualResult.minX, -1.0f);
        assertEquals(actualResult.midX, -0.5f);
        assertEquals(actualResult.maxX, 0.0f);
        assertEquals(actualResult.minY, 0.0f);
        assertEquals(actualResult.midY, 0.5f);
        assertEquals(actualResult.maxY, 1.0f);
        assertEquals(actualResult.minZ, -1.0f);
        assertEquals(actualResult.midZ, -0.5f);
        assertEquals(actualResult.maxZ, 0.0f);
    }

    /**
     * Test of topRightBackOctant method, of class BoundingBox3D.
     */
    @Test
    public void testTopRightBackOctant() {
        System.out.println("getTopRightBackOctant");
        BoundingBox3D base = new BoundingBox3D(baseGraph);
        BoundingBox3D actualResult = base.topRightBackOctant();
        assertEquals(actualResult.minX, 0.0f);
        assertEquals(actualResult.midX, 0.5f);
        assertEquals(actualResult.maxX, 1.0f);
        assertEquals(actualResult.minY, 0.0f);
        assertEquals(actualResult.midY, 0.5f);
        assertEquals(actualResult.maxY, 1.0f);
        assertEquals(actualResult.minZ, -1.0f);
        assertEquals(actualResult.midZ, -0.5f);
        assertEquals(actualResult.maxZ, 0.0f);
    }

    /**
     * Test of bottomLeftBackOctant method, of class BoundingBox3D.
     */
    @Test
    public void testBottomLeftBackOctant() {
        System.out.println("getBottomLeftBackOctant");
        BoundingBox3D base = new BoundingBox3D(baseGraph);
        BoundingBox3D actualResult = base.bottomLeftBackOctant();
        assertEquals(actualResult.minX, -1.0f);
        assertEquals(actualResult.midX, -0.5f);
        assertEquals(actualResult.maxX, 0.0f);
        assertEquals(actualResult.minY, -1.0f);
        assertEquals(actualResult.midY, -0.5f);
        assertEquals(actualResult.maxY, 0.0f);
        assertEquals(actualResult.minZ, -1.0f);
        assertEquals(actualResult.midZ, -0.5f);
        assertEquals(actualResult.maxZ, 0.0f);
    }

    /**
     * Test of bottomRightBackOctant method, of class BoundingBox3D.
     */
    @Test
    public void testBottomRightBackOctant() {
        System.out.println("getBottomRightBackOctant");
        BoundingBox3D base = new BoundingBox3D(baseGraph);
        BoundingBox3D actualResult = base.bottomRightBackOctant();
        assertEquals(actualResult.minX, 0.0f);
        assertEquals(actualResult.midX, 0.5f);
        assertEquals(actualResult.maxX, 1.0f);
        assertEquals(actualResult.minY, -1.0f);
        assertEquals(actualResult.midY, -0.5f);
        assertEquals(actualResult.maxY, 0.0f);
        assertEquals(actualResult.minZ, -1.0f);
        assertEquals(actualResult.midZ, -0.5f);
        assertEquals(actualResult.maxZ, 0.0f);
    }

}
