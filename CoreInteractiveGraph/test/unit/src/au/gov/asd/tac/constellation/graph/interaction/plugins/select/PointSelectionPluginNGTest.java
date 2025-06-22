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

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.ConnectionMode;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.graphics.IntArray;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * PointSelectionPlugin Tests.
 * <br>
 * The parameters toggleSelection and clearSelection used by the plugin work as
 * follows:
 * <br>
 * toggleSelection: If true is as if control key is held down so consecutive
 * selections will select multiple elements, or deselect selected elements.
 * <br>
 * clearSelection: If true deselects all other elements aside from the one being
 * selected. If toggle selection is true this should be false, otherwise
 * consecutive selections will be deselected.
 *
 * @author sol695510
 */
public class PointSelectionPluginNGTest {

    private StoreGraph storeGraph;
    
    private int vAttrId;
    private int tAttrId;
    private int connectionModeAttrId;
    
    private int vxId1;
    private int vxId2;
    private int vxId3;
    private int vxId4;
    
    private int txId1;
    private int txId2;
    private int txId3;
    private int txId4;
    private int txId5;
    
    private IntArray vxIds;
    private IntArray txIds;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
    }

    /**
     * Creates a basic graph with 4 vertices and 5 transactions with selection
     * attributes added for testing.
     *
     * @throws Exception
     */
    @BeforeMethod
    public void setUpMethod() throws Exception {
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema();
        storeGraph = new StoreGraph(schema);

        vAttrId = VisualConcept.VertexAttribute.SELECTED.ensure(storeGraph);
        tAttrId = VisualConcept.TransactionAttribute.SELECTED.ensure(storeGraph);
        connectionModeAttrId = VisualConcept.GraphAttribute.CONNECTION_MODE.ensure(storeGraph);
        
        storeGraph.setObjectValue(connectionModeAttrId, 0, ConnectionMode.TRANSACTION);

        vxId1 = storeGraph.addVertex();
        vxId2 = storeGraph.addVertex();
        vxId3 = storeGraph.addVertex();
        vxId4 = storeGraph.addVertex();

        txId1 = storeGraph.addTransaction(vxId1, vxId2, false);
        txId2 = storeGraph.addTransaction(vxId2, vxId3, false);
        txId3 = storeGraph.addTransaction(vxId3, vxId1, false);
        txId4 = storeGraph.addTransaction(vxId3, vxId2, false);
        txId5 = storeGraph.addTransaction(vxId2, vxId3, false);
        
        storeGraph.setBooleanValue(vAttrId, vxId2, true);
        storeGraph.setBooleanValue(vAttrId, vxId3, true);
        storeGraph.setBooleanValue(tAttrId, txId2, true);
        storeGraph.setBooleanValue(tAttrId, txId3, true);
        
        vxIds = new IntArray();
        txIds = new IntArray();
        
        vxIds.add(vxId1, vxId2);
        txIds.add(txId1, txId2);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }
    
    /**
     * Test of PointSelectionPlugin. Toggle and Clear Selection enabled
     *
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testToggleAndClearSelection() throws InterruptedException, PluginException {
        System.out.println("toggleAndClearSelection");

        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId3));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId4));
        
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId1));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId2));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId3));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId4));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId5));

        final PointSelectionPlugin instance = new PointSelectionPlugin(vxIds, txIds, true, true);
        PluginExecution.withPlugin(instance).executeNow(storeGraph);
        
        // vxId1, txId1 become true because of selection
        // vxId2, txId2 become false because of selection + toggle
        // vxId3, txId3 become false because of clear selection
        // vxId4, txId4, txId5 remain false
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId3));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId4));
        
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId1));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId2));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId3));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId4));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId5));
    }
    
    /**
     * Test of PointSelectionPlugin. Toggle enabled. Clear selection not enabled.
     *
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testToggleAndNoClearSelection() throws InterruptedException, PluginException {
        System.out.println("toggleAndNoClearSelection");

        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId3));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId4));
        
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId1));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId2));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId3));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId4));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId5));

        final PointSelectionPlugin instance = new PointSelectionPlugin(vxIds, txIds, true, false);
        PluginExecution.withPlugin(instance).executeNow(storeGraph);
        
        // vxId1, txId1 become true because of selection
        // vxId2, txId2 become false because of selection + toggle
        // vxId3, txId3 remain true
        // vxId4, txId4, txId5 remain false
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId3));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId4));
        
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId1));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId2));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId3));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId4));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId5));
    }
    
    /**
     * Test of PointSelectionPlugin. Toggle not enabled. Clear selection enabled.
     *
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testNoToggleAndClearSelection() throws InterruptedException, PluginException {
        System.out.println("noToggleAndClearSelection");

        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId3));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId4));
        
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId1));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId2));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId3));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId4));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId5));

        final PointSelectionPlugin instance = new PointSelectionPlugin(vxIds, txIds, false, true);
        PluginExecution.withPlugin(instance).executeNow(storeGraph);
        
        // vxId1, txId1 become true because of selection
        // vxId2, txId2 remain true because of selection
        // vxId3, txId3 become false because of clear selection
        // vxId4, txId4, txId5 remain false
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId3));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId4));
        
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId1));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId2));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId3));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId4));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId5));
    }
    
    /**
     * Test of PointSelectionPlugin. Toggle and Clear selection not enabled.
     *
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testNoToggleAndNoClearSelection() throws InterruptedException, PluginException {
        System.out.println("noToggleAndNoClearSelection");

        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId3));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId4));
        
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId1));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId2));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId3));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId4));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId5));

        final PointSelectionPlugin instance = new PointSelectionPlugin(vxIds, txIds, false, false);
        PluginExecution.withPlugin(instance).executeNow(storeGraph);
        
        // vxId1, txId1 become true because of selection
        // vxId2, txId2 remain true because of selection
        // vxId3, txId3 remain true
        // vxId4, txId4, txId5 remain false
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId3));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId4));
        
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId1));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId2));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId3));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId4));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId5));
    }
    
    /**
     * Test of PointSelectionPlugin. Edge Connection Mode
     *
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testEdgeConnectionMode() throws InterruptedException, PluginException {
        System.out.println("edgeConnectionMode");
        
        storeGraph.setObjectValue(connectionModeAttrId, 0, ConnectionMode.LINK);

        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId3));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId4));
        
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId1));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId2));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId3));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId4));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId5));

        final PointSelectionPlugin instance = new PointSelectionPlugin(vxIds, txIds, false, true);
        PluginExecution.withPlugin(instance).executeNow(storeGraph);
        
        // vxId1, txId1 become true because of selection
        // vxId2, txId2 remain true because of selection
        // vxId3, txId3 become false because of clear selection
        // vxId4, txId4 remains false
        // txId5 becomes true due to being on the same edge as selected txId2
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId3));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId4));
        
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId1));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId2));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId3));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId4));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId5));
    }
    
    /**
     * Test of PointSelectionPlugin. Link Connection Mode
     *
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testLinkConnectionMode() throws InterruptedException, PluginException {
        System.out.println("linkConnectionMode");
        
        storeGraph.setObjectValue(connectionModeAttrId, 0, ConnectionMode.LINK);

        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId3));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId4));
        
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId1));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId2));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId3));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId4));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId5));

        final PointSelectionPlugin instance = new PointSelectionPlugin(vxIds, txIds, false, true);
        PluginExecution.withPlugin(instance).executeNow(storeGraph);
        
        // vxId1, txId1 become true because of selection
        // vxId2, txId2 remain true because of selection
        // vxId3, txId3 become false because of clear selection
        // vxId4 remains false
        // txId4, txId5 become true due to being on the same link as selected txId2
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId3));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId4));
        
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId1));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId2));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId3));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId4));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId5));
    }
}
