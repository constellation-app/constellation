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
package au.gov.asd.tac.constellation.graph.interaction.plugins.delete;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import static org.testng.Assert.assertEquals;
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

    private int attrX;
    private int attrY;
    
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
    
    private int vAttrId;
    private int tAttrId;
    
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
        graph = new StoreGraph();

        attrX = VisualConcept.VertexAttribute.X.ensure(graph);
        attrY = VisualConcept.VertexAttribute.Y.ensure(graph);
        vAttrId = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        tAttrId = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);

        vxId1 = graph.addVertex();
        graph.setFloatValue(attrX, vxId1, 1.0f);
        graph.setFloatValue(attrY, vxId1, 1.0f);
        graph.setBooleanValue(vAttrId, vxId1, false);
        
        vxId2 = graph.addVertex();
        graph.setFloatValue(attrX, vxId2, 5.0f);
        graph.setFloatValue(attrY, vxId2, 1.0f);
        graph.setBooleanValue(vAttrId, vxId2, false);
        
        vxId3 = graph.addVertex();
        graph.setFloatValue(attrX, vxId3, 1.0f);
        graph.setFloatValue(attrY, vxId3, 5.0f);
        graph.setBooleanValue(vAttrId, vxId3, false);
        
        vxId4 = graph.addVertex();
        graph.setFloatValue(attrX, vxId4, 5.0f);
        graph.setFloatValue(attrY, vxId4, 5.0f);
        graph.setBooleanValue(vAttrId, vxId4, false);
        
        vxId5 = graph.addVertex();
        graph.setFloatValue(attrX, vxId5, 10.0f);
        graph.setFloatValue(attrY, vxId5, 10.0f);
        graph.setBooleanValue(vAttrId, vxId5, false);
        
        vxId6 = graph.addVertex();
        graph.setFloatValue(attrX, vxId6, 15.0f);
        graph.setFloatValue(attrY, vxId6, 15.0f);
        graph.setBooleanValue(vAttrId, vxId6, false);
        
        vxId7 = graph.addVertex();
        graph.setFloatValue(attrX, vxId7, 100.0f);
        graph.setFloatValue(attrY, vxId7, 100.0f);
        graph.setBooleanValue(vAttrId, vxId7, false);

        txId1 = graph.addTransaction(vxId1, vxId2, false);
        txId2 = graph.addTransaction(vxId1, vxId3, false);
        txId3 = graph.addTransaction(vxId2, vxId4, false);
        txId4 = graph.addTransaction(vxId4, vxId2, false);
        txId5 = graph.addTransaction(vxId5, vxId6, false);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Tests that nothing is deleted as nothing is selected
     *
     * @throws InterruptedException
     * @throws PluginException
     */
    @Test
    public void testNothingSelected() throws InterruptedException, PluginException {
        DeleteSelectionPlugin instance = new DeleteSelectionPlugin();
        PluginExecution.withPlugin(instance).executeNow(graph);
        assertEquals(graph.getVertexCount(), 7);
        assertEquals(graph.getTransactionCount(), 5);
    }

    /**
     * Tests that some vertices and transactions are deleted when those vertices
     * are selected
     *
     * @throws InterruptedException
     * @throws PluginException
     */
    @Test
    public void testSomeVxSelected() throws InterruptedException, PluginException {
        graph.setBooleanValue(vAttrId, vxId2, true);
        graph.setBooleanValue(vAttrId, vxId4, true);
        graph.setBooleanValue(vAttrId, vxId6, true);
        graph.setBooleanValue(vAttrId, vxId7, true);

        DeleteSelectionPlugin instance = new DeleteSelectionPlugin();
        PluginExecution.withPlugin(instance).executeNow(graph);

        assertEquals(graph.getVertexCount(), 3);
        assertEquals(graph.getTransactionCount(), 1);
    }

    /**
     * Tests that some transactions are deleted when they are selected
     *
     * @throws InterruptedException
     * @throws PluginException
     */
    @Test
    public void testSomeTxSelected() throws InterruptedException, PluginException {
        graph.setBooleanValue(tAttrId, txId1, true);
        graph.setBooleanValue(tAttrId, txId2, true);

        DeleteSelectionPlugin instance = new DeleteSelectionPlugin();
        PluginExecution.withPlugin(instance).executeNow(graph);

        assertEquals(graph.getVertexCount(), 7);
        assertEquals(graph.getTransactionCount(), 3);
    }

    /**
     * Tests that everything is deleted when all vertices and transactions are
     * selected
     *
     * @throws InterruptedException
     * @throws PluginException
     */
    @Test
    public void testAllSelected() throws InterruptedException, PluginException {
        graph.setBooleanValue(vAttrId, vxId1, true);
        graph.setBooleanValue(vAttrId, vxId2, true);
        graph.setBooleanValue(vAttrId, vxId3, true);
        graph.setBooleanValue(vAttrId, vxId4, true);
        graph.setBooleanValue(vAttrId, vxId5, true);
        graph.setBooleanValue(vAttrId, vxId6, true);
        graph.setBooleanValue(vAttrId, vxId7, true);

        graph.setBooleanValue(tAttrId, txId1, true);
        graph.setBooleanValue(tAttrId, txId2, true);
        graph.setBooleanValue(tAttrId, txId3, true);
        graph.setBooleanValue(tAttrId, txId4, true);
        graph.setBooleanValue(tAttrId, txId5, true);

        DeleteSelectionPlugin instance = new DeleteSelectionPlugin();
        PluginExecution.withPlugin(instance).executeNow(graph);

        assertEquals(graph.getVertexCount(), 0);
        assertEquals(graph.getTransactionCount(), 0);
    }
}
