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
package au.gov.asd.tac.constellation.views.layers.utilities;

import au.gov.asd.tac.constellation.graph.LayersConcept;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.views.layers.state.LayersViewConcept;
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
 * @author aldebaran30701
 */
public class ShuffleElementBitmaskPluginNGTest {

    private int layerMaskV;
    private int layerMaskT;
    private int layerVisibilityV;
    private int layerVisibilityT;
    private int selectedV;
    private int selectedT;
    
    private int vxId1;
    private int vxId2;
    private int vxId3;
    private int vxId4;
    private int vxId5;
    private int vxId6;
    private int vxId7;
    
    private int txId1;
    private int txId2;
    private int txId3;
    
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
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    public void setupGraph() {
        graph = new StoreGraph();

        // Create LayerMask attributes
        layerMaskV = LayersConcept.VertexAttribute.LAYER_MASK.ensure(graph);
        layerMaskT = LayersConcept.TransactionAttribute.LAYER_MASK.ensure(graph);

        // Create LayerVisilibity Attributes
        layerVisibilityV = LayersConcept.VertexAttribute.LAYER_VISIBILITY.ensure(graph);
        layerVisibilityT = LayersConcept.TransactionAttribute.LAYER_VISIBILITY.ensure(graph);

        // Create Selected Attributes
        selectedV = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        selectedT = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);

        // Adding 2 Vertices - not selected, layer 1, visible
        vxId1 = graph.addVertex();
        graph.setIntValue(layerMaskV, vxId1, 0b11);
        graph.setFloatValue(layerVisibilityV, vxId1, 1.0f);
        graph.setBooleanValue(selectedV, vxId1, false);

        // Layer 3, invisible
        vxId2 = graph.addVertex();
        graph.setIntValue(layerMaskV, vxId2, 0b1001);
        graph.setFloatValue(layerVisibilityV, vxId2, 0.0f);
        graph.setBooleanValue(selectedV, vxId2, false);

        // Adding 2 Transactions - not selected, layer 1, visible
        txId1 = graph.addTransaction(vxId1, vxId2, true);
        graph.setIntValue(layerMaskT, txId1, 0b11);
        graph.setFloatValue(layerVisibilityT, txId1, 1.0f);
        graph.setBooleanValue(selectedT, txId1, false);

        // Layer 3, invisible
        txId2 = graph.addTransaction(vxId1, vxId2, false);
        graph.setIntValue(layerMaskT, txId2, 0b1001);
        graph.setFloatValue(layerVisibilityT, vxId2, 0.0f);
        graph.setBooleanValue(selectedT, vxId2, false);
    }

    public void setupGraph7Vertices() {
        graph = new StoreGraph();

        // Create LayerMask attributes
        layerMaskV = LayersConcept.VertexAttribute.LAYER_MASK.ensure(graph);
        layerMaskT = LayersConcept.TransactionAttribute.LAYER_MASK.ensure(graph);

        // Create LayerVisilibity Attributes
        layerVisibilityV = LayersConcept.VertexAttribute.LAYER_VISIBILITY.ensure(graph);
        layerVisibilityT = LayersConcept.TransactionAttribute.LAYER_VISIBILITY.ensure(graph);

        // Create Selected Attributes
        selectedV = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        selectedT = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);

        final int layerMaskSelectedAttr = LayersViewConcept.GraphAttribute.LAYER_MASK_SELECTED.ensure(graph);

        // enable layer 5
        graph.setLongValue(layerMaskSelectedAttr, 0, 0b000001);
        assertEquals(graph.getLongValue(layerMaskSelectedAttr, 0), 0b000001);

        // Adding 7 Vertices - not selected, layer 5 visible
        vxId1 = graph.addVertex();
        graph.setIntValue(layerMaskV, vxId1, 0b11);
        graph.setFloatValue(layerVisibilityV, vxId1, 0.0f);
        graph.setBooleanValue(selectedV, vxId1, false);

        vxId2 = graph.addVertex();
        graph.setIntValue(layerMaskV, vxId2, 0b101);
        graph.setFloatValue(layerVisibilityV, vxId2, 0.0f);
        graph.setBooleanValue(selectedV, vxId2, false);

        vxId3 = graph.addVertex();
        graph.setIntValue(layerMaskV, vxId3, 0b1001);
        graph.setFloatValue(layerVisibilityV, vxId3, 0.0f);
        graph.setBooleanValue(selectedV, vxId3, false);

        vxId4 = graph.addVertex();
        graph.setIntValue(layerMaskV, vxId4, 0b10001);
        graph.setFloatValue(layerVisibilityV, vxId4, 0.0f);
        graph.setBooleanValue(selectedV, vxId4, false);

        // vx 5 on layer 5 is the only element visible
        vxId5 = graph.addVertex();
        graph.setIntValue(layerMaskV, vxId5, 0b100001);
        graph.setFloatValue(layerVisibilityV, vxId5, 1.0f);
        graph.setBooleanValue(selectedV, vxId5, false);

        vxId6 = graph.addVertex();
        graph.setIntValue(layerMaskV, vxId6, 0b1000001);
        graph.setFloatValue(layerVisibilityV, vxId6, 0.0f);
        graph.setBooleanValue(selectedV, vxId6, false);

        vxId7 = graph.addVertex();
        graph.setIntValue(layerMaskV, vxId7, 0b10000001);
        graph.setFloatValue(layerVisibilityV, vxId7, 0.0f);
        graph.setBooleanValue(selectedV, vxId7, false);
    }

    /**
     * Test of edit method, of class ShuffleElementBitmaskPlugin.
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testEdit() throws InterruptedException, PluginException {
        System.out.println("testEdit");

        setupGraph();
        // Check Vertex set correctly
        assertEquals(graph.getIntValue(layerMaskV, vxId1), 0b11);
        assertEquals(graph.getFloatValue(layerVisibilityV, vxId1), 1.0f);
        assertFalse(graph.getBooleanValue(selectedV, vxId1));

        assertEquals(graph.getIntValue(layerMaskV, vxId2), 0b1001);
        assertEquals(graph.getFloatValue(layerVisibilityV, vxId2), 0.0f);
        assertFalse(graph.getBooleanValue(selectedV, vxId2));

        // Check Transaction set correctly
        assertEquals(graph.getIntValue(layerMaskT, vxId1), 0b11);
        assertEquals(graph.getFloatValue(layerVisibilityT, vxId1), 1.0f);
        assertFalse(graph.getBooleanValue(selectedT, vxId1));

        assertEquals(graph.getIntValue(layerMaskT, vxId2), 0b1001);
        assertEquals(graph.getFloatValue(layerVisibilityT, vxId2), 0.0f);
        assertFalse(graph.getBooleanValue(selectedT, vxId2));

        PluginExecution.withPlugin(new ShuffleElementBitmaskPlugin(2)).executeNow(graph);

        // Test should now have vx2 and tx2 on layer 2 instead of layer 3.
        // Check Vertex set correctly
        assertEquals(graph.getIntValue(layerMaskV, vxId1), 0b11);
        assertEquals(graph.getFloatValue(layerVisibilityV, vxId1), 1.0f);
        assertFalse(graph.getBooleanValue(selectedV, vxId1));

        assertEquals(graph.getIntValue(layerMaskV, vxId2), 0b101);
        assertEquals(graph.getFloatValue(layerVisibilityV, vxId2), 0.0f);
        assertFalse(graph.getBooleanValue(selectedV, vxId2));

        // Check Transaction set correctly
        assertEquals(graph.getIntValue(layerMaskT, vxId1), 0b11);
        assertEquals(graph.getFloatValue(layerVisibilityT, vxId1), 1.0f);
        assertFalse(graph.getBooleanValue(selectedT, vxId1));

        assertEquals(graph.getIntValue(layerMaskT, vxId2), 0b101);
        assertEquals(graph.getFloatValue(layerVisibilityT, vxId2), 0.0f);
        assertFalse(graph.getBooleanValue(selectedT, vxId2));

    }

    /**
     * Test of edit method, of class ShuffleElementBitmaskPlugin.
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testEditToggledLayer() throws InterruptedException, PluginException {
        System.out.println("testEditToggledLayer");

        setupGraph();

        final int layerMaskSelectedAttr = LayersViewConcept.GraphAttribute.LAYER_MASK_SELECTED.ensure(graph);

        // enable layer 3
        graph.setLongValue(layerMaskSelectedAttr, 0, 0b0001);
        assertEquals(graph.getLongValue(layerMaskSelectedAttr, 0), 0b0001);

        graph.setFloatValue(layerVisibilityV, vxId1, 0.0f);
        graph.setFloatValue(layerVisibilityV, vxId2, 1.0f);
        graph.setFloatValue(layerVisibilityT, txId1, 0.0f);
        graph.setFloatValue(layerVisibilityT, txId2, 1.0f);

        // Check Vertex set correctly
        assertEquals(graph.getIntValue(layerMaskV, vxId1), 0b11);
        assertEquals(graph.getFloatValue(layerVisibilityV, vxId1), 0.0f);
        assertFalse(graph.getBooleanValue(selectedV, vxId1));

        assertEquals(graph.getIntValue(layerMaskV, vxId2), 0b1001);
        assertEquals(graph.getFloatValue(layerVisibilityV, vxId2), 1.0f);
        assertFalse(graph.getBooleanValue(selectedV, vxId2));

        // Check Transaction set correctly
        assertEquals(graph.getIntValue(layerMaskT, vxId1), 0b11);
        assertEquals(graph.getFloatValue(layerVisibilityT, vxId1), 0.0f);
        assertFalse(graph.getBooleanValue(selectedT, vxId1));

        assertEquals(graph.getIntValue(layerMaskT, vxId2), 0b1001);
        assertEquals(graph.getFloatValue(layerVisibilityT, vxId2), 1.0f);
        assertFalse(graph.getBooleanValue(selectedT, vxId2));

        PluginExecution.withPlugin(new ShuffleElementBitmaskPlugin(2)).executeNow(graph);

        // Test should now have vx2 and tx2 on layer 2 instead of layer 3.
        // Check Vertex set correctly
        assertEquals(graph.getIntValue(layerMaskV, vxId1), 0b11);
        assertEquals(graph.getFloatValue(layerVisibilityV, vxId1), 0.0f);
        assertFalse(graph.getBooleanValue(selectedV, vxId1));

        assertEquals(graph.getIntValue(layerMaskV, vxId2), 0b101);
        assertEquals(graph.getFloatValue(layerVisibilityV, vxId2), 1.0f);
        assertFalse(graph.getBooleanValue(selectedV, vxId2));

        // Check Transaction set correctly
        assertEquals(graph.getIntValue(layerMaskT, vxId1), 0b11);
        assertEquals(graph.getFloatValue(layerVisibilityT, vxId1), 0.0f);
        assertFalse(graph.getBooleanValue(selectedT, vxId1));

        assertEquals(graph.getIntValue(layerMaskT, vxId2), 0b101);
        assertEquals(graph.getFloatValue(layerVisibilityT, vxId2), 1.0f);
        assertFalse(graph.getBooleanValue(selectedT, vxId2));

    }

    /**
     * Test of edit method, of class ShuffleElementBitmaskPlugin.
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testEditToggledLayerSelectedVx() throws InterruptedException, PluginException {
        System.out.println("testEditToggledLayerSelectedVx");
        // Graph viewing layer 3
        // vx1 layer 1 invis unselect
        // vx2 layer 2 invis unselect
        // vx3 layer 3 visible selected
        // tx1 layer 1 invis unselect
        // tx2 layer 2 invis unselect
        // tx3 layer 3 visible unselect

        setupGraph();

        // Add a third vertex
        vxId3 = graph.addVertex();
        graph.setIntValue(layerMaskV, vxId3, 0b1001);
        graph.setFloatValue(layerVisibilityV, vxId3, 1.0f);
        graph.setBooleanValue(selectedV, vxId3, true);

        // Add a third transaction
        txId3 = graph.addTransaction(vxId2, vxId3, true);
        graph.setIntValue(layerMaskT, txId3, 0b1001);
        graph.setFloatValue(layerVisibilityT, txId3, 1.0f);
        graph.setBooleanValue(selectedT, txId3, false);

        // Set vx and tx to crrect layers
        graph.setIntValue(layerMaskV, vxId1, 0b11);
        graph.setIntValue(layerMaskV, vxId2, 0b101);
        graph.setIntValue(layerMaskT, txId1, 0b11);
        graph.setIntValue(layerMaskT, txId2, 0b101);

        final int layerMaskSelectedAttr = LayersViewConcept.GraphAttribute.LAYER_MASK_SELECTED.ensure(graph);

        // enable layer 3
        graph.setLongValue(layerMaskSelectedAttr, 0, 0b0001);
        assertEquals(graph.getLongValue(layerMaskSelectedAttr, 0), 0b0001);

        graph.setFloatValue(layerVisibilityV, vxId1, 0.0f);
        graph.setFloatValue(layerVisibilityV, vxId2, 0.0f);
        graph.setFloatValue(layerVisibilityV, vxId3, 1.0f);

        graph.setFloatValue(layerVisibilityT, txId1, 0.0f);
        graph.setFloatValue(layerVisibilityT, txId2, 0.0f);
        graph.setFloatValue(layerVisibilityT, txId3, 1.0f);

        // Check Vertex set correctly
        assertEquals(graph.getIntValue(layerMaskV, vxId1), 0b11);
        assertEquals(graph.getFloatValue(layerVisibilityV, vxId1), 0.0f);
        assertFalse(graph.getBooleanValue(selectedV, vxId1));

        assertEquals(graph.getIntValue(layerMaskV, vxId2), 0b101);
        assertEquals(graph.getFloatValue(layerVisibilityV, vxId2), 0.0f);
        assertFalse(graph.getBooleanValue(selectedV, vxId2));

        assertEquals(graph.getIntValue(layerMaskV, vxId3), 0b1001);
        assertEquals(graph.getFloatValue(layerVisibilityV, vxId3), 1.0f);
        assertTrue(graph.getBooleanValue(selectedV, vxId3));

        // Check Transaction set correctly
        assertEquals(graph.getIntValue(layerMaskT, txId1), 0b11);
        assertEquals(graph.getFloatValue(layerVisibilityT, txId1), 0.0f);
        assertFalse(graph.getBooleanValue(selectedT, txId1));

        assertEquals(graph.getIntValue(layerMaskT, txId2), 0b101); // TODO:  why different
        assertEquals(graph.getFloatValue(layerVisibilityT, txId2), 0.0f); // why different
        assertFalse(graph.getBooleanValue(selectedT, txId2));

        assertEquals(graph.getIntValue(layerMaskT, txId3), 0b1001);
        assertEquals(graph.getFloatValue(layerVisibilityT, txId3), 1.0f);
        assertFalse(graph.getBooleanValue(selectedT, txId3));

        PluginExecution.withPlugin(new ShuffleElementBitmaskPlugin(2)).executeNow(graph);

        // Test should now have vx2 and tx2 on layer 2 instead of layer 3.
        // Check Vertex set correctly
        assertEquals(graph.getIntValue(layerMaskV, vxId1), 0b11);
        assertEquals(graph.getFloatValue(layerVisibilityV, vxId1), 0.0f);
        assertFalse(graph.getBooleanValue(selectedV, vxId1));

        assertEquals(graph.getIntValue(layerMaskV, vxId2), 0b101);
        assertEquals(graph.getFloatValue(layerVisibilityV, vxId2), 0.0f);
        assertFalse(graph.getBooleanValue(selectedV, vxId2));

        assertEquals(graph.getIntValue(layerMaskV, vxId3), 0b101);
        assertEquals(graph.getFloatValue(layerVisibilityV, vxId3), 1.0f);
        assertTrue(graph.getBooleanValue(selectedV, vxId3));

        // Check Transaction set correctly
        assertEquals(graph.getIntValue(layerMaskT, txId1), 0b11);
        assertEquals(graph.getFloatValue(layerVisibilityT, txId1), 0.0f);
        assertFalse(graph.getBooleanValue(selectedT, txId1));

        assertEquals(graph.getIntValue(layerMaskT, txId2), 0b101);
        assertEquals(graph.getFloatValue(layerVisibilityT, txId2), 0.0f);
        assertFalse(graph.getBooleanValue(selectedT, txId2));

        assertEquals(graph.getIntValue(layerMaskT, txId3), 0b101);
        assertEquals(graph.getFloatValue(layerVisibilityT, txId3), 1.0f);
        assertFalse(graph.getBooleanValue(selectedT, txId3));
    }

    @Test
    public void testEditSevenLayersSevenVx() throws InterruptedException, PluginException {
        System.out.println("testEditSevenLayersSevenVx");
        setupGraph7Vertices();

        // trigger shuffle of layer 4
        PluginExecution.withPlugin(new ShuffleElementBitmaskPlugin(4)).executeNow(graph);

        assertEquals(graph.getIntValue(layerMaskV, vxId1), 0b11);
        assertEquals(graph.getFloatValue(layerVisibilityV, vxId1), 0.0f);

        assertEquals(graph.getIntValue(layerMaskV, vxId2), 0b101);
        assertEquals(graph.getFloatValue(layerVisibilityV, vxId2), 0.0f);

        assertEquals(graph.getIntValue(layerMaskV, vxId3), 0b1001);
        assertEquals(graph.getFloatValue(layerVisibilityV, vxId3), 0.0f);

        assertEquals(graph.getIntValue(layerMaskV, vxId4), 0b10001);
        assertEquals(graph.getFloatValue(layerVisibilityV, vxId4), 0.0f);

        // Vx 5 has shifted up a position
        assertEquals(graph.getIntValue(layerMaskV, vxId5), 0b10001);
        assertEquals(graph.getFloatValue(layerVisibilityV, vxId5), 1.0f);

        // Vx 6 has shifted up a position
        assertEquals(graph.getIntValue(layerMaskV, vxId6), 0b100001);
        assertEquals(graph.getFloatValue(layerVisibilityV, vxId6), 0.0f);

        // Vx 7 has shifted up a position
        assertEquals(graph.getIntValue(layerMaskV, vxId7), 0b1000001);
        assertEquals(graph.getFloatValue(layerVisibilityV, vxId7), 0.0f);

    }

    /**
     * Test of incrementBitmask method, of class ShuffleElementBitmaskPlugin.
     */
    @Test
    public void testIncrementBitmask() {
        System.out.println("incrementBitmask");

        // Shift into second layer position
        int currentIndex = 2;
        long currentBitmask = 0b1001;
        long expResult = 0b101;
        long result = ShuffleElementBitmaskPlugin.incrementBitmask(currentIndex, currentBitmask);
        assertEquals(result, expResult);

        // shift into first layer position
        currentIndex = 1;
        currentBitmask = 0b101;
        expResult = 0b11;
        result = ShuffleElementBitmaskPlugin.incrementBitmask(currentIndex, currentBitmask);
        assertEquals(result, expResult);

        // shift into 19th layer position from layer 20
        currentIndex = 19;
        currentBitmask = 0b100000000000000000001;
        expResult = 0b10000000000000000001;
        result = ShuffleElementBitmaskPlugin.incrementBitmask(currentIndex, currentBitmask);
        assertEquals(result, expResult);
    }
}
