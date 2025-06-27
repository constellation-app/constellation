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
package au.gov.asd.tac.constellation.graph.interaction.plugins.select;

import au.gov.asd.tac.constellation.graph.LayersConcept;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author aldebaran30701
 */
public class BoxSelectionPluginNGTest {
    
    private Camera camera;
    
    private int attrX;
    private int attrY;
    
    private int vxId1;
    private int vxId2;
    private int vxId3;
    private int vxId4;
    
    private int txId1;
    private int txId2;
    
    private int vSelectedAttrId;
    private int tSelectedAttrId;
    
    private StoreGraph graph;

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
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema();
        graph = new StoreGraph(schema);

        attrX = VisualConcept.VertexAttribute.X.ensure(graph);
        attrY = VisualConcept.VertexAttribute.Y.ensure(graph);
        VisualConcept.VertexAttribute.Z.ensure(graph);

        vSelectedAttrId = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        tSelectedAttrId = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);
        LayersConcept.VertexAttribute.LAYER_VISIBILITY.ensure(graph);
        LayersConcept.TransactionAttribute.LAYER_VISIBILITY.ensure(graph);

        VisualConcept.VertexAttribute.NODE_RADIUS.ensure(graph);
        
        vxId1 = graph.addVertex();
        graph.setFloatValue(attrX, vxId1, -3F);
        graph.setFloatValue(attrY, vxId1, 3F);
        
        vxId2 = graph.addVertex();
        graph.setFloatValue(attrX, vxId2, 3F);
        graph.setFloatValue(attrY, vxId2, 3F);
        
        vxId3 = graph.addVertex();
        graph.setFloatValue(attrX, vxId3, -3F);
        graph.setFloatValue(attrY, vxId3, -3F);
        
        vxId4 = graph.addVertex();
        graph.setFloatValue(attrX, vxId4, 3F);
        graph.setFloatValue(attrY, vxId4, -3F);
        
        txId1 = graph.addTransaction(vxId1, vxId2, false);
        txId2 = graph.addTransaction(vxId1, vxId4, false);
        
        camera = new Camera();
        camera.setMixRatio(0);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }
    
    /**
     * Test of edit method, of class BoxSelectionPlugin. Nothing Selected
     * 
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testEditNothingSelected() throws InterruptedException, PluginException {
        System.out.println("editNothingSelected");

        beginningAsserts();
        
        final BoxSelectionPlugin instance = new BoxSelectionPlugin(false, false, camera, new float[]{-0.25F, -0.2F, 0.1F, 0F});
        PluginExecution.withPlugin(instance).executeNow(graph);

        // Should remain false after running
        assertFalse(graph.getBooleanValue(vSelectedAttrId, vxId1));
        assertFalse(graph.getBooleanValue(vSelectedAttrId, vxId2));
        assertFalse(graph.getBooleanValue(vSelectedAttrId, vxId3));
        assertFalse(graph.getBooleanValue(vSelectedAttrId, vxId4));
        
        assertFalse(graph.getBooleanValue(tSelectedAttrId, txId1));
        assertFalse(graph.getBooleanValue(tSelectedAttrId, txId2));
    }
    
    /**
     * Test of edit method, of class BoxSelectionPlugin. Append to existing selection
     * 
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testEditAppend() throws InterruptedException, PluginException {
        System.out.println("editAppend");
        
        VisualConcept.VertexAttribute.VISIBILITY.ensure(graph);
        VisualConcept.TransactionAttribute.VISIBILITY.ensure(graph);
        
        beginningAsserts();

        BoxSelectionPlugin instance = new BoxSelectionPlugin(true, false, camera, new float[]{-0.1F, 0.1F, 0.1F, -0.1F});
        PluginExecution.withPlugin(instance).executeNow(graph);

        // txId2 should be within the box -0.1, 0.1, 0.1, -0.1
        assertFalse(graph.getBooleanValue(vSelectedAttrId, vxId1));
        assertFalse(graph.getBooleanValue(vSelectedAttrId, vxId2));
        assertFalse(graph.getBooleanValue(vSelectedAttrId, vxId3));
        assertFalse(graph.getBooleanValue(vSelectedAttrId, vxId4));

        assertFalse(graph.getBooleanValue(tSelectedAttrId, txId1));
        assertTrue(graph.getBooleanValue(tSelectedAttrId, txId2));
        
        instance = new BoxSelectionPlugin(true, false, camera, new float[]{-0.3F, 0.3F, 0.25F, 0.15F});
        PluginExecution.withPlugin(instance).executeNow(graph);

        // vx1 and vx2 should be within the box -0.3, 0.3, 0.25, 0.15. tx1 also gets selected consequently
        // tx2 remains selected
        assertTrue(graph.getBooleanValue(vSelectedAttrId, vxId1));
        assertTrue(graph.getBooleanValue(vSelectedAttrId, vxId2));
        assertFalse(graph.getBooleanValue(vSelectedAttrId, vxId3));
        assertFalse(graph.getBooleanValue(vSelectedAttrId, vxId4));

        assertTrue(graph.getBooleanValue(tSelectedAttrId, txId1));
        assertTrue(graph.getBooleanValue(tSelectedAttrId, txId2));
    }
    
    /**
     * Test of edit method, of class BoxSelectionPlugin. Toggle selection
     * 
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testEditToggle() throws InterruptedException, PluginException {
        System.out.println("editToggle");
        
        VisualConcept.VertexAttribute.VISIBILITY.ensure(graph);
        VisualConcept.TransactionAttribute.VISIBILITY.ensure(graph);
        
        graph.setBooleanValue(tSelectedAttrId, txId2, true);
        
        assertFalse(graph.getBooleanValue(vSelectedAttrId, vxId1));
        assertFalse(graph.getBooleanValue(vSelectedAttrId, vxId2));
        assertFalse(graph.getBooleanValue(vSelectedAttrId, vxId3));
        assertFalse(graph.getBooleanValue(vSelectedAttrId, vxId4));

        assertFalse(graph.getBooleanValue(tSelectedAttrId, txId1));
        assertTrue(graph.getBooleanValue(tSelectedAttrId, txId2));

        BoxSelectionPlugin instance = new BoxSelectionPlugin(false, true, camera, new float[]{-0.1F, 0.1F, 0.1F, -0.1F});
        PluginExecution.withPlugin(instance).executeNow(graph);

        // txId2 should be within the box -0.1, 0.1, 0.1, -0.1. Toggle will turn this false
        assertFalse(graph.getBooleanValue(vSelectedAttrId, vxId1));
        assertFalse(graph.getBooleanValue(vSelectedAttrId, vxId2));
        assertFalse(graph.getBooleanValue(vSelectedAttrId, vxId3));
        assertFalse(graph.getBooleanValue(vSelectedAttrId, vxId4));

        assertFalse(graph.getBooleanValue(tSelectedAttrId, txId1));
        assertFalse(graph.getBooleanValue(tSelectedAttrId, txId2));
        
        instance = new BoxSelectionPlugin(false, true, camera, new float[]{-0.3F, 0.3F, 0.25F, 0.15F});
        PluginExecution.withPlugin(instance).executeNow(graph);

        // vx1 and vx2 should be within the box -0.3, 0.3, 0.25, 0.15. tx1 also gets selected consequently
        assertTrue(graph.getBooleanValue(vSelectedAttrId, vxId1));
        assertTrue(graph.getBooleanValue(vSelectedAttrId, vxId2));
        assertFalse(graph.getBooleanValue(vSelectedAttrId, vxId3));
        assertFalse(graph.getBooleanValue(vSelectedAttrId, vxId4));

        assertTrue(graph.getBooleanValue(tSelectedAttrId, txId1));
        assertFalse(graph.getBooleanValue(tSelectedAttrId, txId2));
    }
    
    /**
     * Test of edit method, of class BoxSelectionPlugin. Replace selection
     * 
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testEditReplace() throws InterruptedException, PluginException {
        System.out.println("editReplace");
        
        VisualConcept.VertexAttribute.VISIBILITY.ensure(graph);
        VisualConcept.TransactionAttribute.VISIBILITY.ensure(graph);
        
        beginningAsserts();

        BoxSelectionPlugin instance = new BoxSelectionPlugin(false, false, camera, new float[]{-0.1F, 0.1F, 0.1F, -0.1F});
        PluginExecution.withPlugin(instance).executeNow(graph);

        // txId2 should be within a box -0.1, 0.1, 0.1, -0.1
        assertFalse(graph.getBooleanValue(vSelectedAttrId, vxId1));
        assertFalse(graph.getBooleanValue(vSelectedAttrId, vxId2));
        assertFalse(graph.getBooleanValue(vSelectedAttrId, vxId3));
        assertFalse(graph.getBooleanValue(vSelectedAttrId, vxId4));

        assertFalse(graph.getBooleanValue(tSelectedAttrId, txId1));
        assertTrue(graph.getBooleanValue(tSelectedAttrId, txId2));
        
        instance = new BoxSelectionPlugin(false, false, camera, new float[]{-0.3F, 0.3F, 0.25F, 0.15F});
        PluginExecution.withPlugin(instance).executeNow(graph);

        // vx1 and vx2 should be within the box -0.3, 0.3, 0.25, 0.15. tx1 also gets selected consequently
        // tx2 is deselected
        assertTrue(graph.getBooleanValue(vSelectedAttrId, vxId1));
        assertTrue(graph.getBooleanValue(vSelectedAttrId, vxId2));
        assertFalse(graph.getBooleanValue(vSelectedAttrId, vxId3));
        assertFalse(graph.getBooleanValue(vSelectedAttrId, vxId4));

        assertTrue(graph.getBooleanValue(tSelectedAttrId, txId1));
        assertFalse(graph.getBooleanValue(tSelectedAttrId, txId2));
    }
    
    /**
     * Test of edit method, of class BoxSelectionPlugin. Alternate coordinates present but not used, no mix required
     * 
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testEditAltCoordsPresentNoMix() throws InterruptedException, PluginException {
        System.out.println("editAltCoordsPresentNoMix");
        
        setupAltCoords();
        beginningAsserts();
        
        final BoxSelectionPlugin instance = new BoxSelectionPlugin(false, false, camera, new float[]{-0.3F, 0.3F, 0.25F, 0.15F});
        PluginExecution.withPlugin(instance).executeNow(graph);

        // vxId1 and vxId2  + tx1 should be within the box -0.3, 0.3, 0.25, 0.15
        assertTrue(graph.getBooleanValue(vSelectedAttrId, vxId1));
        assertTrue(graph.getBooleanValue(vSelectedAttrId, vxId2));
        assertFalse(graph.getBooleanValue(vSelectedAttrId, vxId3));
        assertFalse(graph.getBooleanValue(vSelectedAttrId, vxId4));

        assertTrue(graph.getBooleanValue(tSelectedAttrId, txId1));
        assertFalse(graph.getBooleanValue(tSelectedAttrId, txId2));
    }
    
    /**
     * Test of edit method, of class BoxSelectionPlugin. Alternate coordinates used with mix
     * 
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testEditAltCoordsMix() throws InterruptedException, PluginException {
        System.out.println("editAlCoordsMix");
        
        setupAltCoords();
        beginningAsserts();
        
        camera.setMixRatio(10);
        
        final BoxSelectionPlugin instance = new BoxSelectionPlugin(false, false, camera, new float[]{-0.3F, 0.3F, 0.25F, 0.15F});
        PluginExecution.withPlugin(instance).executeNow(graph);

        // vxId1 and vxId2  + tx1 should be within the box -0.3, 0.3, 0.25, 0.15 normally.
        // With alternate coordiantes and mixing, those graph elements should still be in the box
        assertTrue(graph.getBooleanValue(vSelectedAttrId, vxId1));
        assertTrue(graph.getBooleanValue(vSelectedAttrId, vxId2));
        assertFalse(graph.getBooleanValue(vSelectedAttrId, vxId3));
        assertFalse(graph.getBooleanValue(vSelectedAttrId, vxId4));

        assertTrue(graph.getBooleanValue(tSelectedAttrId, txId1));
        assertFalse(graph.getBooleanValue(tSelectedAttrId, txId2));
    }
    
    /**
     * Test of edit method, of class BoxSelectionPlugin. Alternate coordinates used
     * 
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testEditAltCoords() throws InterruptedException, PluginException {
        System.out.println("editAltCoords");
        
        setupAltCoords();       
        beginningAsserts();
        
        // 20 is the max mix ratio, giving a mix of 1
        camera.setMixRatio(20);
        
        final BoxSelectionPlugin instance = new BoxSelectionPlugin(false, false, camera, new float[]{-0.3F, 0.3F, 0.25F, 0.15F});
        PluginExecution.withPlugin(instance).executeNow(graph);

        // vxId1 and vxId2  + tx1 should be within the box -0.3, 0.3, 0.25, 0.15 normally.
        // But with alternate coordiantes used, none of those end up in the box and instead txId2 should fall in it
        assertFalse(graph.getBooleanValue(vSelectedAttrId, vxId1));
        assertFalse(graph.getBooleanValue(vSelectedAttrId, vxId2));
        assertFalse(graph.getBooleanValue(vSelectedAttrId, vxId3));
        assertFalse(graph.getBooleanValue(vSelectedAttrId, vxId4));

        assertFalse(graph.getBooleanValue(tSelectedAttrId, txId1));
        assertTrue(graph.getBooleanValue(tSelectedAttrId, txId2));
    }
    
    private void beginningAsserts() {
        assertFalse(graph.getBooleanValue(vSelectedAttrId, vxId1));
        assertFalse(graph.getBooleanValue(vSelectedAttrId, vxId2));
        assertFalse(graph.getBooleanValue(vSelectedAttrId, vxId3));
        assertFalse(graph.getBooleanValue(vSelectedAttrId, vxId4));

        assertFalse(graph.getBooleanValue(tSelectedAttrId, txId1));
        assertFalse(graph.getBooleanValue(tSelectedAttrId, txId2));
    }
    
    private void setupAltCoords() {
        final int attrX2 = VisualConcept.VertexAttribute.X2.ensure(graph);
        final int attrY2 = VisualConcept.VertexAttribute.Y2.ensure(graph);
        VisualConcept.VertexAttribute.Z2.ensure(graph);
        
        graph.setFloatValue(attrX2, vxId1, -2F);
        graph.setFloatValue(attrY2, vxId1, 4F);
        
        graph.setFloatValue(attrX2, vxId2, 4F);
        graph.setFloatValue(attrY2, vxId2, 4F);
        
        graph.setFloatValue(attrX2, vxId3, -2F);
        graph.setFloatValue(attrY2, vxId3, -2F);
        
        graph.setFloatValue(attrX2, vxId4, 4F);
        graph.setFloatValue(attrY2, vxId4, -2F);
    }
}
