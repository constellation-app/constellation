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
package au.gov.asd.tac.constellation.graph.interaction.plugins.delete;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for the Delete Selection Plugin
 * 
 * @author Delphinus8821
 */
public class DeleteSelectionPluginNGTest {
    
    private int attrX, attrY;
    private int vxId1, vxId2, vxId3, vxId4, vxId5, vxId6, vxId7;
    private int txId1, txId2, txId3, txId4, txId5;
    private int vAttrId, tAttrId;
    private Graph graph;
    
    public DeleteSelectionPluginNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        graph = new DualGraph(schema);
        
        WritableGraph wg = graph.getWritableGraph("Delete", true);
        try {     
            attrX = VisualConcept.VertexAttribute.X.ensure(wg);
            attrY = VisualConcept.VertexAttribute.Y.ensure(wg);
            vAttrId = VisualConcept.VertexAttribute.SELECTED.ensure(wg);
            tAttrId = VisualConcept.TransactionAttribute.SELECTED.ensure(wg);

            vxId1 = wg.addVertex();
            wg.setFloatValue(attrX, vxId1, 1.0f);
            wg.setFloatValue(attrY, vxId1, 1.0f);
            wg.setBooleanValue(vAttrId, vxId1, false);
            vxId2 = wg.addVertex();
            wg.setFloatValue(attrX, vxId2, 5.0f);
            wg.setFloatValue(attrY, vxId2, 1.0f);
            wg.setBooleanValue(vAttrId, vxId2, false);
            vxId3 = wg.addVertex();
            wg.setFloatValue(attrX, vxId3, 1.0f);
            wg.setFloatValue(attrY, vxId3, 5.0f);
            wg.setBooleanValue(vAttrId, vxId3, false);
            vxId4 = wg.addVertex();
            wg.setFloatValue(attrX, vxId4, 5.0f);
            wg.setFloatValue(attrY, vxId4, 5.0f);
            wg.setBooleanValue(vAttrId, vxId4, false);
            vxId5 = wg.addVertex();
            wg.setFloatValue(attrX, vxId5, 10.0f);
            wg.setFloatValue(attrY, vxId5, 10.0f);
            wg.setBooleanValue(vAttrId, vxId5, false);
            vxId6 = wg.addVertex();
            wg.setFloatValue(attrX, vxId6, 15.0f);
            wg.setFloatValue(attrY, vxId6, 15.0f);
            wg.setBooleanValue(vAttrId, vxId6, false);
            vxId7 = wg.addVertex();
            wg.setFloatValue(attrX, vxId7, 100.0f);
            wg.setFloatValue(attrY, vxId7, 100.0f);
            wg.setBooleanValue(vAttrId, vxId7, false);

            txId1 = wg.addTransaction(vxId1, vxId2, false);
            txId2 = wg.addTransaction(vxId1, vxId3, false);
            txId3 = wg.addTransaction(vxId2, vxId4, false);
            txId4 = wg.addTransaction(vxId4, vxId2, false);
            txId5 = wg.addTransaction(vxId5, vxId6, false);
        } finally {
            wg.commit();
        }
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of edit method, of class DeleteSelectionPlugin.
     * @throws Exception
     */
    @Test
    public void testEdit() throws Exception {        
        testNothingSelected();
        testSomeVxSelected();
        testSomeTxSelected();
        testAllSelected();      
    }
    
    /**
     * Tests that nothing is deleted as nothing is selected
     * 
     * @throws InterruptedException
     * @throws PluginException
     * @throws Exception
     */
    public void testNothingSelected() throws InterruptedException, PluginException, Exception {
        setUpMethod();
        GraphWriteMethods testGraph = graph.getWritableGraph("Test", true);
        DeleteSelectionPlugin instance = new DeleteSelectionPlugin();
        PluginExecution.withPlugin(instance).executeNow(testGraph);
        assertEquals(testGraph.getVertexCount(), 7);
        assertEquals(testGraph.getTransactionCount(), 5);    
    }
    
    /**
     * Tests that some vertices and transactions are deleted when those vertices are selected
     * 
     * @throws InterruptedException
     * @throws PluginException
     * @throws Exception
     */ 
    public void testSomeVxSelected() throws InterruptedException, PluginException, Exception {
        setUpMethod();
        GraphWriteMethods testGraph = graph.getWritableGraph("Test", true);
        testGraph.setBooleanValue(vAttrId, vxId2, true);
        testGraph.setBooleanValue(vAttrId, vxId4, true);  
        testGraph.setBooleanValue(vAttrId, vxId6, true);
        testGraph.setBooleanValue(vAttrId, vxId7, true);
        
        DeleteSelectionPlugin instance = new DeleteSelectionPlugin();
        PluginExecution.withPlugin(instance).executeNow(testGraph);
        
        assertEquals(testGraph.getVertexCount(), 3);
        assertEquals(testGraph.getTransactionCount(), 1);   
    }
    
    /**
     * Tests that some transactions are deleted when they are selected 
     * 
     * @throws InterruptedException
     * @throws PluginException
     * @throws Exception 
     */
    public void testSomeTxSelected() throws InterruptedException, PluginException, Exception {
        setUpMethod();
        GraphWriteMethods testGraph = graph.getWritableGraph("Test", true);
        testGraph.setBooleanValue(tAttrId, txId1, true);
        testGraph.setBooleanValue(tAttrId, txId2, true);
        
        DeleteSelectionPlugin instance = new DeleteSelectionPlugin();
        PluginExecution.withPlugin(instance).executeNow(testGraph);
        
        assertEquals(testGraph.getVertexCount(), 7);
        assertEquals(testGraph.getTransactionCount(), 3);  
    }
    
    /** 
     * Tests that everything is deleted when all vertices and transactions are selected 
     * 
     * @throws InterruptedException
     * @throws PluginException
     * @throws Exception
     */
    public void testAllSelected() throws InterruptedException, PluginException, Exception {
        setUpMethod();
        GraphWriteMethods testGraph = graph.getWritableGraph("Test", true);
        testGraph.setBooleanValue(vAttrId, vxId1, true);
        testGraph.setBooleanValue(vAttrId, vxId2, true);
        testGraph.setBooleanValue(vAttrId, vxId3, true);
        testGraph.setBooleanValue(vAttrId, vxId4, true);  
        testGraph.setBooleanValue(vAttrId, vxId5, true);
        testGraph.setBooleanValue(vAttrId, vxId6, true);
        testGraph.setBooleanValue(vAttrId, vxId7, true);
        
        testGraph.setBooleanValue(tAttrId, txId1, true);
        testGraph.setBooleanValue(tAttrId, txId2, true);
        testGraph.setBooleanValue(tAttrId, txId3, true);
        testGraph.setBooleanValue(tAttrId, txId4, true);
        testGraph.setBooleanValue(tAttrId, txId5, true);
        
        DeleteSelectionPlugin instance = new DeleteSelectionPlugin();
        PluginExecution.withPlugin(instance).executeNow(testGraph);
        
        assertEquals(testGraph.getVertexCount(), 0);
        assertEquals(testGraph.getTransactionCount(), 0);  
    }
    
}
