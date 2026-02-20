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
package au.gov.asd.tac.constellation.graph.visual.graphics;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class BBoxfNGTest {
    
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of add method, of class BBoxf.
     */
    @Test
    public void testAdd() {
        System.out.println("add");
        
        final BBoxf instance = new BBoxf();
        
        // define initial bounding box
        instance.add(0, 0, 0);
        
        assertEquals(instance.getMin()[BBoxf.X], 0F);
        assertEquals(instance.getMin()[BBoxf.Y], 0F);
        assertEquals(instance.getMin()[BBoxf.Z], 0F);
        
        assertEquals(instance.getMax()[BBoxf.X], 0F);
        assertEquals(instance.getMax()[BBoxf.Y], 0F);
        assertEquals(instance.getMax()[BBoxf.Z], 0F);
        
        // extend box min
        instance.add(-1.2F, -2.3F, -3.4F);
        
        assertEquals(instance.getMin()[BBoxf.X], -1.2F);
        assertEquals(instance.getMin()[BBoxf.Y], -2.3F);
        assertEquals(instance.getMin()[BBoxf.Z], -3.4F);
        
        assertEquals(instance.getMax()[BBoxf.X], 0F);
        assertEquals(instance.getMax()[BBoxf.Y], 0F);
        assertEquals(instance.getMax()[BBoxf.Z], 0F);
        
        // extend box max
        instance.add(4, 5.5F, 6);
        
        assertEquals(instance.getMin()[BBoxf.X], -1.2F);
        assertEquals(instance.getMin()[BBoxf.Y], -2.3F);
        assertEquals(instance.getMin()[BBoxf.Z], -3.4F);
        
        assertEquals(instance.getMax()[BBoxf.X], 4F);
        assertEquals(instance.getMax()[BBoxf.Y], 5.5F);
        assertEquals(instance.getMax()[BBoxf.Z], 6F);
    }

    /**
     * Test of isEmpty method, of class BBoxf.
     */
    @Test
    public void testIsEmpty() {
        System.out.println("isEmpty");
        
        final BBoxf instance = new BBoxf();
        assertTrue(instance.isEmpty());
        instance.add(0, 0, 0);
        assertFalse(instance.isEmpty());
    }

    /**
     * Test of getCentre method, of class BBoxf.
     */
    @Test
    public void testGetCentre() {
        System.out.println("getCentre");
        
        final BBoxf instance = new BBoxf();
        
        instance.add(0, 0, 0);
        assertEquals(instance.getCentre()[BBoxf.X], 0F);
        assertEquals(instance.getCentre()[BBoxf.Y], 0F);
        assertEquals(instance.getCentre()[BBoxf.Z], 0F);
        
        instance.add(-1, -2, -3);
        instance.add(4, 6, 8);
        assertEquals(instance.getCentre()[BBoxf.X], 1.5F);
        assertEquals(instance.getCentre()[BBoxf.Y], 2F);
        assertEquals(instance.getCentre()[BBoxf.Z], 2.5F);
    }

    /**
     * Test of toString method, of class BBoxf.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        
        final BBoxf instance = new BBoxf();
        
        instance.add(0, 0, 0);
        instance.add(-1, -2, -3);
        instance.add(4, 6, 8);
        
        assertEquals(instance.toString(), "BB[[-1.0, -2.0, -3.0],[4.0, 6.0, 8.0]]");
    }

    /**
     * Test of getGraphBoundingBox method, of class BBoxf.
     */
    @Test
    public void testGetGraphBoundingBox() {
        System.out.println("getGraphBoundingBox");
        
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema();
        final StoreGraph graph = new StoreGraph(schema);
        
        final int vxId1 = graph.addVertex();
        final int vxId2 = graph.addVertex();
        final int vxId3 = graph.addVertex();
        
        final int xVertexAttribute = VisualConcept.VertexAttribute.X.ensure(graph);
        final int yVertexAttribute = VisualConcept.VertexAttribute.Y.ensure(graph);
        final int zVertexAttribute = VisualConcept.VertexAttribute.Z.ensure(graph);
        
        graph.setFloatValue(xVertexAttribute, vxId1, 1.1F);
        graph.setFloatValue(yVertexAttribute, vxId1, -2.2F);
        graph.setFloatValue(zVertexAttribute, vxId1, 3.3F);
        
        graph.setFloatValue(xVertexAttribute, vxId2, -1.1F);
        graph.setFloatValue(yVertexAttribute, vxId2, 2.2F);
        graph.setFloatValue(zVertexAttribute, vxId2, -3.3F);
        
        graph.setFloatValue(xVertexAttribute, vxId3, -4.4F);
        graph.setFloatValue(yVertexAttribute, vxId3, 5.5F);
        graph.setFloatValue(zVertexAttribute, vxId3, 6.6F);
        
        final BBoxf result = BBoxf.getGraphBoundingBox(graph);
        
        assertEquals(result.getMin()[BBoxf.X], -4.4F);
        assertEquals(result.getMin()[BBoxf.Y], -2.2F);
        assertEquals(result.getMin()[BBoxf.Z], -3.3F);
        
        assertEquals(result.getMax()[BBoxf.X], 1.1F);
        assertEquals(result.getMax()[BBoxf.Y], 5.5F);
        assertEquals(result.getMax()[BBoxf.Z], 6.6F);
    } 
}
