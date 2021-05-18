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
 * PointSelectionPlugin Test.
 * 
 * @author sol695510
 */
public class PointSelectionPluginNGTest {
    
    private StoreGraph storeGraph;
    
    private int vAttrId, tAttrId;
    
    private int vxId1, vxId2, vxId3;
    private int txId1, txId2, txId3;
    
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
        
        vxIds.clear();
        txIds.clear();
        
        selectAll(null, false);
    }
    
    @AfterMethod
    public void tearDownMethod() throws Exception {
    }
    
    @Test
    public void testAllVxSelected() throws Exception {
        setUpMethod();
        selectAll(GraphElementType.VERTEX, true);
        
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId3));
        
        vxIds.add(vxId1);
        boolean toggleSelection = true;
        boolean clearSelection = true;
        
        Plugin selectPoint = new PointSelectionPlugin(vxIds, txIds, toggleSelection, clearSelection);
        PluginExecution.withPlugin(selectPoint).executeNow(storeGraph);
        
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId3));
    }
    
    @Test
    public void testAllTxSelected() throws Exception {
        setUpMethod();
        selectAll(GraphElementType.TRANSACTION, true);
        
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId1));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId2));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId3));
        
        txIds.add(txId1);
        boolean toggleSelection = true;
        boolean clearSelection = true;
        
        Plugin selectPoint = new PointSelectionPlugin(vxIds, txIds, toggleSelection, clearSelection);
        PluginExecution.withPlugin(selectPoint).executeNow(storeGraph);
        
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId1));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId2));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId3));
    }
    
    @Test
    public void testAllNsSelected() throws Exception {
        setUpMethod();
        selectAll(null, true);
        
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId3));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId1));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId2));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId3));
        
        vxIds.add(vxId1);
        boolean toggleSelection = true;
        boolean clearSelection = true;
        
        Plugin selectPoint = new PointSelectionPlugin(vxIds, txIds, toggleSelection, clearSelection);
        PluginExecution.withPlugin(selectPoint).executeNow(storeGraph);
        
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId3));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId1));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId2));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId3));
    }
    
    private void selectAll(final GraphElementType type, final boolean select) {
        if (null == type) {
            storeGraph.setBooleanValue(vAttrId, vxId1, select);
            storeGraph.setBooleanValue(vAttrId, vxId2, select);
            storeGraph.setBooleanValue(vAttrId, vxId3, select);
            storeGraph.setBooleanValue(tAttrId, txId1, select);
            storeGraph.setBooleanValue(tAttrId, txId2, select);
            storeGraph.setBooleanValue(tAttrId, txId3, select);
        } else switch (type) {
            case VERTEX:
                storeGraph.setBooleanValue(vAttrId, vxId1, select);
                storeGraph.setBooleanValue(vAttrId, vxId2, select);
                storeGraph.setBooleanValue(vAttrId, vxId3, select);
                break;
            case TRANSACTION:
                storeGraph.setBooleanValue(tAttrId, txId1, select);
                storeGraph.setBooleanValue(tAttrId, txId2, select);
                storeGraph.setBooleanValue(tAttrId, txId3, select);
                break;
            default:
                break;
        }
    }
}
