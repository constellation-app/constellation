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
package au.gov.asd.tac.constellation.graph.visual.plugins.hop;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.visual.VisualGraphPluginRegistry;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Hop Full Test.
 *
 * @author altair
 */
public class HopFullNGTest {
   
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
    private int txId4;
    private int txId5;
    
    private int xVertexAttribute;
    private int yVertexAttribute;
    private int zVertexAttribute;
    private int selectedVertexAttribute;
    private int selectedTransactionAttribute;
    
    private StoreGraph graph;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        graph = null;
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema();
        graph = new StoreGraph(schema);
        
        xVertexAttribute = VisualConcept.VertexAttribute.X.ensure(graph);
        yVertexAttribute = VisualConcept.VertexAttribute.Y.ensure(graph);
        zVertexAttribute = VisualConcept.VertexAttribute.Z.ensure(graph);
        selectedVertexAttribute = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        selectedTransactionAttribute = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);

        vxId1 = graph.addVertex();
        graph.setFloatValue(xVertexAttribute, vxId1, 1.0f);
        graph.setFloatValue(yVertexAttribute, vxId1, 1.0f);
        graph.setBooleanValue(selectedVertexAttribute, vxId1, false);
        
        vxId2 = graph.addVertex();
        graph.setFloatValue(xVertexAttribute, vxId2, 5.0f);
        graph.setFloatValue(yVertexAttribute, vxId2, 1.0f);
        graph.setBooleanValue(selectedVertexAttribute, vxId2, false);
        
        vxId3 = graph.addVertex();
        graph.setFloatValue(xVertexAttribute, vxId3, 1.0f);
        graph.setFloatValue(yVertexAttribute, vxId3, 5.0f);
        graph.setBooleanValue(selectedVertexAttribute, vxId3, false);
        
        vxId4 = graph.addVertex();
        graph.setFloatValue(xVertexAttribute, vxId4, 5.0f);
        graph.setFloatValue(yVertexAttribute, vxId4, 5.0f);
        graph.setBooleanValue(selectedVertexAttribute, vxId4, false);
        
        vxId5 = graph.addVertex();
        graph.setFloatValue(xVertexAttribute, vxId5, 10.0f);
        graph.setFloatValue(yVertexAttribute, vxId5, 10.0f);
        graph.setBooleanValue(selectedVertexAttribute, vxId5, false);
        
        vxId6 = graph.addVertex();
        graph.setFloatValue(xVertexAttribute, vxId6, 15.0f);
        graph.setFloatValue(yVertexAttribute, vxId6, 15.0f);
        graph.setBooleanValue(selectedVertexAttribute, vxId6, false);
        
        vxId7 = graph.addVertex();
        graph.setFloatValue(xVertexAttribute, vxId7, 100.0f);
        graph.setFloatValue(yVertexAttribute, vxId7, 100.0f);
        graph.setBooleanValue(selectedVertexAttribute, vxId7, false);

        txId1 = graph.addTransaction(vxId1, vxId2, false);
        txId2 = graph.addTransaction(vxId1, vxId3, false);
        txId3 = graph.addTransaction(vxId2, vxId4, true);
        txId4 = graph.addTransaction(vxId4, vxId2, true);
        txId5 = graph.addTransaction(vxId5, vxId6, false);
    }

    @Test
    public void fullHopTest() throws InterruptedException, PluginException {
        graph.setBooleanValue(selectedVertexAttribute, vxId2, true);
        graph.setBooleanValue(selectedVertexAttribute, vxId5, true);
        graph.setBooleanValue(selectedVertexAttribute, vxId7, true);

        assertFalse(String.format("Node [%d] should be de-selected", vxId1), graph.getBooleanValue(selectedVertexAttribute, vxId1));
        assertTrue(String.format("Node [%d] should be selected", vxId2), graph.getBooleanValue(selectedVertexAttribute, vxId2));
        assertFalse(String.format("Node [%d] should be de-selected", vxId3), graph.getBooleanValue(selectedVertexAttribute, vxId3));
        assertFalse(String.format("Node [%d] should be de-selected", vxId4), graph.getBooleanValue(selectedVertexAttribute, vxId4));
        assertTrue(String.format("Node [%d] should be selected", vxId5), graph.getBooleanValue(selectedVertexAttribute, vxId5));
        assertFalse(String.format("Node [%d] should be de-selected", vxId6), graph.getBooleanValue(selectedVertexAttribute, vxId6));
        assertTrue(String.format("Node [%d] should be selected", vxId7), graph.getBooleanValue(selectedVertexAttribute, vxId7));
        assertFalse(String.format("Transaction [%d] should be de-selected", txId1), graph.getBooleanValue(selectedTransactionAttribute, txId1));
        assertFalse(String.format("Transaction [%d] should be de-selected", txId2), graph.getBooleanValue(selectedTransactionAttribute, txId2));
        assertFalse(String.format("Transaction [%d] should be de-selected", txId3), graph.getBooleanValue(selectedTransactionAttribute, txId3));
        assertFalse(String.format("Transaction [%d] should be de-selected", txId4), graph.getBooleanValue(selectedTransactionAttribute, txId4));
        assertFalse(String.format("Transaction [%d] should be de-selected", txId5), graph.getBooleanValue(selectedTransactionAttribute, txId5));

        PluginExecution.withPlugin(VisualGraphPluginRegistry.HOP_OUT)
                .withParameter(HopOutPlugin.HOPS_PARAMETER_ID, HopUtilities.HOP_OUT_FULL)
                .executeNow(graph);

        assertTrue(String.format("Node [%d] should be selected", vxId1), graph.getBooleanValue(selectedVertexAttribute, vxId1));
        assertTrue(String.format("Node [%d] should be selected", vxId2), graph.getBooleanValue(selectedVertexAttribute, vxId2));
        assertTrue(String.format("Node [%d] should be selected", vxId3), graph.getBooleanValue(selectedVertexAttribute, vxId3));
        assertTrue(String.format("Node [%d] should be selected", vxId4), graph.getBooleanValue(selectedVertexAttribute, vxId4));
        assertTrue(String.format("Node [%d] should be selected", vxId5), graph.getBooleanValue(selectedVertexAttribute, vxId5));
        assertTrue(String.format("Node [%d] should be selected", vxId6), graph.getBooleanValue(selectedVertexAttribute, vxId6));
        assertTrue(String.format("Node [%d] should be selected", vxId7), graph.getBooleanValue(selectedVertexAttribute, vxId7));
        assertTrue(String.format("Transaction [%d] should be selected", txId1), graph.getBooleanValue(selectedTransactionAttribute, txId1));
        assertTrue(String.format("Transaction [%d] should be selected", txId2), graph.getBooleanValue(selectedTransactionAttribute, txId2));
        assertTrue(String.format("Transaction [%d] should be selected", txId3), graph.getBooleanValue(selectedTransactionAttribute, txId3));
        assertTrue(String.format("Transaction [%d] should be selected", txId4), graph.getBooleanValue(selectedTransactionAttribute, txId4));
        assertTrue(String.format("Transaction [%d] should be selected", txId5), graph.getBooleanValue(selectedTransactionAttribute, txId5));
    }

    @Test
    public void fullHopTest2() throws InterruptedException, PluginException {
        graph.setBooleanValue(selectedVertexAttribute, vxId1, true);
        graph.setBooleanValue(selectedTransactionAttribute, txId3, true);

        assertTrue(String.format("Node [%d] should be selected", vxId1), graph.getBooleanValue(selectedVertexAttribute, vxId1));
        assertFalse(String.format("Node [%d] should be de-selected", vxId2), graph.getBooleanValue(selectedVertexAttribute, vxId2));
        assertFalse(String.format("Node [%d] should be de-selected", vxId3), graph.getBooleanValue(selectedVertexAttribute, vxId3));
        assertFalse(String.format("Node [%d] should be de-selected", vxId4), graph.getBooleanValue(selectedVertexAttribute, vxId4));
        assertFalse(String.format("Node [%d] should be de-selected", vxId5), graph.getBooleanValue(selectedVertexAttribute, vxId5));
        assertFalse(String.format("Node [%d] should be de-selected", vxId6), graph.getBooleanValue(selectedVertexAttribute, vxId6));
        assertFalse(String.format("Node [%d] should be de-selected", vxId7), graph.getBooleanValue(selectedVertexAttribute, vxId7));
        assertFalse(String.format("Transaction [%d] should be de-selected", txId1), graph.getBooleanValue(selectedTransactionAttribute, txId1));
        assertFalse(String.format("Transaction [%d] should be de-selected", txId2), graph.getBooleanValue(selectedTransactionAttribute, txId2));
        assertTrue(String.format("Transaction [%d] should be selected", txId3), graph.getBooleanValue(selectedTransactionAttribute, txId3));
        assertFalse(String.format("Transaction [%d] should be de-selected", txId4), graph.getBooleanValue(selectedTransactionAttribute, txId4));
        assertFalse(String.format("Transaction [%d] should be de-selected", txId5), graph.getBooleanValue(selectedTransactionAttribute, txId5));

        PluginExecution.withPlugin(VisualGraphPluginRegistry.HOP_OUT)
                .withParameter(HopOutPlugin.HOPS_PARAMETER_ID, HopUtilities.HOP_OUT_FULL)
                .executeNow(graph);

        assertTrue(String.format("Node [%d] should be selected", vxId1), graph.getBooleanValue(selectedVertexAttribute, vxId1));
        assertTrue(String.format("Node [%d] should be selected", vxId2), graph.getBooleanValue(selectedVertexAttribute, vxId2));
        assertTrue(String.format("Node [%d] should be selected", vxId3), graph.getBooleanValue(selectedVertexAttribute, vxId3));
        assertTrue(String.format("Node [%d] should be selected", vxId4), graph.getBooleanValue(selectedVertexAttribute, vxId4));
        assertFalse(String.format("Node [%d] should be de-selected", vxId5), graph.getBooleanValue(selectedVertexAttribute, vxId5));
        assertFalse(String.format("Node [%d] should be de-selected", vxId6), graph.getBooleanValue(selectedVertexAttribute, vxId6));
        assertFalse(String.format("Node [%d] should be de-selected", vxId7), graph.getBooleanValue(selectedVertexAttribute, vxId7));
        assertTrue(String.format("Transaction [%d] should be selected", txId1), graph.getBooleanValue(selectedTransactionAttribute, txId1));
        assertTrue(String.format("Transaction [%d] should be selected", txId2), graph.getBooleanValue(selectedTransactionAttribute, txId2));
        assertTrue(String.format("Transaction [%d] should be selected", txId3), graph.getBooleanValue(selectedTransactionAttribute, txId3));
        assertTrue(String.format("Transaction [%d] should be selected", txId4), graph.getBooleanValue(selectedTransactionAttribute, txId4));
        assertFalse(String.format("Transaction [%d] should be de-selected", txId5), graph.getBooleanValue(selectedTransactionAttribute, txId5));
    }
}
