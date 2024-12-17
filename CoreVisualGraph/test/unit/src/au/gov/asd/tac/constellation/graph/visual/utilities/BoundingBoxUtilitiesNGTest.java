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
package au.gov.asd.tac.constellation.graph.visual.utilities;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.utilities.camera.BoundingBox;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import static org.testng.Assert.assertNull;
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
public class BoundingBoxUtilitiesNGTest {
    
    private StoreGraph graph;
    
    private int vxId1;
    private int vxId2;
    private int vxId3;
    private int vxId4;
    
    private int tId1;
    private int tId2;
    
    private int xVertexAttribute;
    private int yVertexAttribute;
    private int zVertexAttribute;
    private int x2VertexAttribute;
    private int y2VertexAttribute;
    private int z2VertexAttribute;
    private int selectedVertexAttribute;
    private int selectedTransactionAttribute;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema();
        graph = new StoreGraph(schema);
        
        vxId1 = graph.addVertex();
        vxId2 = graph.addVertex();
        vxId3 = graph.addVertex();
        vxId4 = graph.addVertex();
        
        tId1 = graph.addTransaction(vxId1, vxId2, true);
        tId2 = graph.addTransaction(vxId1, vxId4, true);
        
        xVertexAttribute = VisualConcept.VertexAttribute.X.ensure(graph);
        yVertexAttribute = VisualConcept.VertexAttribute.Y.ensure(graph);
        zVertexAttribute = VisualConcept.VertexAttribute.Z.ensure(graph);
        x2VertexAttribute = VisualConcept.VertexAttribute.X2.ensure(graph);
        y2VertexAttribute = VisualConcept.VertexAttribute.Y2.ensure(graph);
        z2VertexAttribute = VisualConcept.VertexAttribute.Z2.ensure(graph);
        selectedVertexAttribute = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        selectedTransactionAttribute = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);
               
        graph.setFloatValue(xVertexAttribute, vxId1, 1F);
        graph.setFloatValue(yVertexAttribute, vxId1, 2F);
        graph.setFloatValue(zVertexAttribute, vxId1, 3F);
        graph.setFloatValue(x2VertexAttribute, vxId1, 4F);
        graph.setFloatValue(y2VertexAttribute, vxId1, 5F);
        graph.setFloatValue(z2VertexAttribute, vxId1, 6F);
        
        graph.setFloatValue(xVertexAttribute, vxId2, 2F);
        graph.setFloatValue(yVertexAttribute, vxId2, 1F);
        graph.setFloatValue(zVertexAttribute, vxId2, 0F);
        graph.setFloatValue(x2VertexAttribute, vxId2, 5F);
        graph.setFloatValue(y2VertexAttribute, vxId2, 4F);
        graph.setFloatValue(z2VertexAttribute, vxId2, 3F);
        
        graph.setFloatValue(xVertexAttribute, vxId3, 0F);
        graph.setFloatValue(yVertexAttribute, vxId3, 2F);
        graph.setFloatValue(zVertexAttribute, vxId3, 4F);
        graph.setFloatValue(x2VertexAttribute, vxId3, 0F);
        graph.setFloatValue(y2VertexAttribute, vxId3, 2F);
        graph.setFloatValue(z2VertexAttribute, vxId3, 5F);
        
        graph.setFloatValue(xVertexAttribute, vxId4, 2F);
        graph.setFloatValue(yVertexAttribute, vxId4, 2F);
        graph.setFloatValue(zVertexAttribute, vxId4, 2F);
        graph.setFloatValue(x2VertexAttribute, vxId4, 2F);
        graph.setFloatValue(y2VertexAttribute, vxId4, 2F);
        graph.setFloatValue(z2VertexAttribute, vxId4, 2F);
        
        graph.setBooleanValue(selectedVertexAttribute, vxId1, true);
        graph.setBooleanValue(selectedVertexAttribute, vxId3, true);
        
        graph.setBooleanValue(selectedTransactionAttribute, tId2, true);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of recalculateFromGraph method, of class BoundingBoxUtilities. Whole Graph.
     */
    @Test
    public void testRecalculateFromGraphWholeGraph() {
        System.out.println("recalculateFromGraphWholeGraph");
        
        final BoundingBox box = new BoundingBox();
        
        assertNull(box.getBoundingBoxMinimum());
        assertNull(box.getBoundingBoxMaximum());
        
        BoundingBoxUtilities.recalculateFromGraph(box, graph, false);
        
        assertTrue(box.getBoundingBoxMinimum().areSame(new Vector3f(0F, 1F, 0F)));
        assertTrue(box.getBoundingBoxMaximum().areSame(new Vector3f(2F, 2F, 4F)));
        assertTrue(box.getMin2().areSame(new Vector3f(0F, 2F, 2F)));
        assertTrue(box.getMax2().areSame(new Vector3f(5F, 5F, 6F)));
    }
    
    /**
     * Test of recalculateFromGraph method, of class BoundingBoxUtilities. Only selected elements
     */
    @Test
    public void testRecalculateFromGraphSelectedOnly() {
        System.out.println("recalculateFromGraphSelectedOnly");
        
        final BoundingBox box = new BoundingBox();
        
        assertNull(box.getBoundingBoxMinimum());
        assertNull(box.getBoundingBoxMaximum());
        
        BoundingBoxUtilities.recalculateFromGraph(box, graph, true);
        
        assertTrue(box.getBoundingBoxMinimum().areSame(new Vector3f(0F, 2F, 2F)));
        assertTrue(box.getBoundingBoxMaximum().areSame(new Vector3f(2F, 2F, 4F)));
        assertTrue(box.getMin2().areSame(new Vector3f(0F, 2F, 2F)));
        assertTrue(box.getMax2().areSame(new Vector3f(4F, 5F, 6F)));
    }

    /**
     * Test of encompassSpecifiedElements method, of class BoundingBoxUtilities.
     */
    @Test
    public void testEncompassSpecifiedElements() {
        System.out.println("encompassSpecifiedElements");
        
        
        final BoundingBox box = new BoundingBox();
        final int[] vertices = new int[]{vxId2, vxId3};
        
        assertNull(box.getBoundingBoxMinimum());
        assertNull(box.getBoundingBoxMaximum());
        
        BoundingBoxUtilities.encompassSpecifiedElements(box, graph, vertices);
        
        assertTrue(box.getBoundingBoxMinimum().areSame(new Vector3f(0F, 1F, 0F)));
        assertTrue(box.getBoundingBoxMaximum().areSame(new Vector3f(2F, 2F, 4F)));
        assertTrue(box.getMin2().areSame(new Vector3f(0F, 2F, 3F)));
        assertTrue(box.getMax2().areSame(new Vector3f(5F, 4F, 5F)));
    }   
}
