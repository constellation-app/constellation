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
package au.gov.asd.tac.constellation.graph.interaction.plugins.select;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.graphics.IntArray;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * PointSelectionPlugin Tests.
 * 
 * @author sol695510
 */
public class PointSelectionPluginNGTest {
    
    private StoreGraph storeGraph;
    
    // Vertex and transaction attribute IDs respectively.
    private int vAttrId, tAttrId;
    
    // Vertex and transaction IDs respectively.
    private int vxId1, vxId2, vxId3;
    private int txId1, txId2, txId3;
    
    // Arrays used by the PointSelectionPlugin that contain the selected graph elements during plugin execution.
    final private IntArray vxIds = new IntArray();
    final private IntArray txIds = new IntArray();
    
    public PointSelectionPluginNGTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws Exception {
    }
    
    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    /**
     * Creates a basic graph with 3 vertices and 3 transactions with selection attributes added for testing.
     * 
     * @throws Exception 
     */
    @BeforeMethod
    public void setUpMethod() throws Exception {
        
        storeGraph = new StoreGraph();
        
        vAttrId = VisualConcept.VertexAttribute.SELECTED.ensure(storeGraph);
        tAttrId = VisualConcept.TransactionAttribute.SELECTED.ensure(storeGraph);
        
        vxId1 = storeGraph.addVertex();
        vxId2 = storeGraph.addVertex();
        vxId3 = storeGraph.addVertex();
        
        txId1 = storeGraph.addTransaction(vxId1, vxId2, false);
        txId2 = storeGraph.addTransaction(vxId2, vxId3, false);
        txId3 = storeGraph.addTransaction(vxId3, vxId1, false);
        
        // Ensures arrays don't contain IDs from previous tests when going into a subsequent test.
        vxIds.clear();
        txIds.clear();
        
        selectAllAndAssert(null, false);
    }
    
    @AfterMethod
    public void tearDownMethod() throws Exception {
    }
    
    /**
     * Tests when a vertex is selected and;
     * all vertices and transaction are unselected,
     * toggle selection is true,
     * and clear selection is true.
     * 
     * @throws Exception 
     */
    @Test
    public void testNoElementsSelectedAndVertexSelected() throws Exception {
        
        selectAllAndAssert(null, false);
        
        vxIds.add(vxId1);
        
        final boolean toggleSelection = true;
        final boolean clearSelection = true;

        Plugin selectPoint = new PointSelectionPlugin(vxIds, txIds, toggleSelection, clearSelection);
        PluginExecution.withPlugin(selectPoint).executeNow(storeGraph);
        
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId3));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId1));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId2));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId3));
    }
    
    /**
     * Tests when a transaction is selected and;
     * all vertices and transaction are unselected,
     * toggle selection is true,
     * and clear selection is true.
     * 
     * @throws Exception 
     */
    @Test
    public void testNoElementsSelectedAndTransactionSelected() throws Exception {
        
        selectAllAndAssert(null, false);
        
        txIds.add(txId1);

        final boolean toggleSelection = true;
        final boolean clearSelection = true;
        
        Plugin selectPoint = new PointSelectionPlugin(vxIds, txIds, toggleSelection, clearSelection);
        PluginExecution.withPlugin(selectPoint).executeNow(storeGraph);
        
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId3));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId1));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId2));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId3));
    }
    
    /**
     * Tests when a vertex is selected and;
     * all vertices and transactions are selected,
     * toggle selection is true,
     * and clear selection is true.
     * 
     * @throws Exception 
     */
    @Test
    public void testAllElementsSelectedAndVertexSelected() throws Exception {
        
        selectAllAndAssert(null, true);
        
        vxIds.add(vxId1);
        
        final boolean toggleSelection = true;
        final boolean clearSelection = true;
        
        Plugin selectPoint = new PointSelectionPlugin(vxIds, txIds, toggleSelection, clearSelection);
        PluginExecution.withPlugin(selectPoint).executeNow(storeGraph);
        
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId3));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId1));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId2));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId3));
    }
    
    /**
     * Tests when a transaction is selected and;
     * all vertices and transactions are selected,
     * toggle selection is true,
     * and clear selection is true.
     * 
     * @throws Exception 
     */
    @Test
    public void testAllElementsSelectedAndTransactionSelected() throws Exception {
        
        selectAllAndAssert(null, true);
        
        txIds.add(txId1);
        
        final boolean toggleSelection = true;
        final boolean clearSelection = true;
        
        Plugin selectPoint = new PointSelectionPlugin(vxIds, txIds, toggleSelection, clearSelection);
        PluginExecution.withPlugin(selectPoint).executeNow(storeGraph);
        
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId3));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId1));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId2));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId3));
    }
    
    /**
     * Tests when a vertex is selected and;
     * only vertices are selected,
     * toggle selection is true,
     * and clear selection is true.
     * 
     * @throws Exception 
     */
    @Test
    public void testOnlyVerticesSelectedAndVertexSelected() throws Exception {
        
        selectAllAndAssert(GraphElementType.VERTEX, true);
        
        vxIds.add(vxId1);
        
        final boolean toggleSelection = true;
        final boolean clearSelection = true;

        Plugin selectPoint = new PointSelectionPlugin(vxIds, txIds, toggleSelection, clearSelection);
        PluginExecution.withPlugin(selectPoint).executeNow(storeGraph);
        
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId3));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId1));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId2));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId3));
    }
    
    /**
     * Tests when a transaction is selected and;
     * only vertices are selected,
     * toggle selection is true,
     * and clear selection is true.
     * 
     * @throws Exception 
     */
    @Test
    public void testOnlyVerticesSelectedAndTransactionSelected() throws Exception {
        
        selectAllAndAssert(GraphElementType.VERTEX, true);
        
        txIds.add(txId1);
        
        final boolean toggleSelection = true;
        final boolean clearSelection = true;

        Plugin selectPoint = new PointSelectionPlugin(vxIds, txIds, toggleSelection, clearSelection);
        PluginExecution.withPlugin(selectPoint).executeNow(storeGraph);
        
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId3));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId1));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId2));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId3));
    }
    
    /**
     * Tests when a vertex is selected and;
     * only transactions are selected,
     * toggle selection is true,
     * and clear selection is true.
     * 
     * @throws Exception 
     */
    @Test
    public void testOnlyTransactionsSelectedAndVertexSelected() throws Exception {
        
        selectAllAndAssert(GraphElementType.TRANSACTION, true);
        
        vxIds.add(vxId1);
        
        final boolean toggleSelection = true;
        final boolean clearSelection = true;
        
        Plugin selectPoint = new PointSelectionPlugin(vxIds, txIds, toggleSelection, clearSelection);
        PluginExecution.withPlugin(selectPoint).executeNow(storeGraph);
        
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId3));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId1));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId2));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId3));
    }
    
    /**
     * Tests when a transaction is selected and;
     * only transactions are selected,
     * toggle selection is true,
     * and clear selection is true.
     * 
     * @throws Exception 
     */
    @Test
    public void testOnlyTransactionsSelectedAndTransactionSelected() throws Exception {
        
        selectAllAndAssert(GraphElementType.TRANSACTION, true);
        
        txIds.add(txId1);
        
        final boolean toggleSelection = true;
        final boolean clearSelection = true;
        
        Plugin selectPoint = new PointSelectionPlugin(vxIds, txIds, toggleSelection, clearSelection);
        PluginExecution.withPlugin(selectPoint).executeNow(storeGraph);
        
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId3));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId1));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId2));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId3));
    }
    
    /**
     * Selects all of given type of graph element or all graph elements,
     * and asserts whether the selections have been made correctly.
     * 
     * @param type The type of graph element being selected, or null if all graph elements are to be selected.
     * @param selectAll Whether the selection attribute for a graph element will be set to true; selected, or false; unselected.
     */
    private void selectAllAndAssert(final GraphElementType type, final boolean selectAll) {
        
        if (null == type) {
            storeGraph.setBooleanValue(vAttrId, vxId1, selectAll);
            storeGraph.setBooleanValue(vAttrId, vxId2, selectAll);
            storeGraph.setBooleanValue(vAttrId, vxId3, selectAll);
            
            storeGraph.setBooleanValue(tAttrId, txId1, selectAll);
            storeGraph.setBooleanValue(tAttrId, txId2, selectAll);
            storeGraph.setBooleanValue(tAttrId, txId3, selectAll);
            
            assertEquals(storeGraph.getBooleanValue(vAttrId, vxId1), selectAll);
            assertEquals(storeGraph.getBooleanValue(vAttrId, vxId2), selectAll);
            assertEquals(storeGraph.getBooleanValue(vAttrId, vxId3), selectAll);
            
            assertEquals(storeGraph.getBooleanValue(tAttrId, txId1), selectAll);
            assertEquals(storeGraph.getBooleanValue(tAttrId, txId2), selectAll);
            assertEquals(storeGraph.getBooleanValue(tAttrId, txId3), selectAll);
        } else switch (type) {
            case VERTEX:
                storeGraph.setBooleanValue(vAttrId, vxId1, selectAll);
                storeGraph.setBooleanValue(vAttrId, vxId2, selectAll);
                storeGraph.setBooleanValue(vAttrId, vxId3, selectAll);
                
                storeGraph.setBooleanValue(tAttrId, txId1, !selectAll);
                storeGraph.setBooleanValue(tAttrId, txId2, !selectAll);
                storeGraph.setBooleanValue(tAttrId, txId3, !selectAll);
                
                assertEquals(storeGraph.getBooleanValue(vAttrId, vxId1), selectAll);
                assertEquals(storeGraph.getBooleanValue(vAttrId, vxId2), selectAll);
                assertEquals(storeGraph.getBooleanValue(vAttrId, vxId3), selectAll);
                
                assertEquals(storeGraph.getBooleanValue(tAttrId, txId1), !selectAll);
                assertEquals(storeGraph.getBooleanValue(tAttrId, txId2), !selectAll);
                assertEquals(storeGraph.getBooleanValue(tAttrId, txId3), !selectAll);
                break;
            case TRANSACTION:
                storeGraph.setBooleanValue(vAttrId, vxId1, !selectAll);
                storeGraph.setBooleanValue(vAttrId, vxId2, !selectAll);
                storeGraph.setBooleanValue(vAttrId, vxId3, !selectAll);
                
                storeGraph.setBooleanValue(tAttrId, txId1, selectAll);
                storeGraph.setBooleanValue(tAttrId, txId2, selectAll);
                storeGraph.setBooleanValue(tAttrId, txId3, selectAll);
                
                assertEquals(storeGraph.getBooleanValue(vAttrId, vxId1), !selectAll);
                assertEquals(storeGraph.getBooleanValue(vAttrId, vxId2), !selectAll);
                assertEquals(storeGraph.getBooleanValue(vAttrId, vxId3), !selectAll);
                
                assertEquals(storeGraph.getBooleanValue(tAttrId, txId1), selectAll);
                assertEquals(storeGraph.getBooleanValue(tAttrId, txId2), selectAll);
                assertEquals(storeGraph.getBooleanValue(tAttrId, txId3), selectAll);
                break;
            default:
                break;
        }
    }
}
