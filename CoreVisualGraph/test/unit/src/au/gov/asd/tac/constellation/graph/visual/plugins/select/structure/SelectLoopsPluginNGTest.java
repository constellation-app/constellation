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
package au.gov.asd.tac.constellation.graph.visual.plugins.select.structure;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
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
public class SelectLoopsPluginNGTest {
    
    private StoreGraph graph;
    
    private int vxId1;
    private int vxId2;
    private int vxId3;
    private int vxId4;
    private int vxId5;
    private int vxId6;
    private int vxId7;
    
    private int tId1;
    private int tId2;
    private int tId3;
    private int tId4;
    private int tId5;
    private int tId6;
    private int tId7;
    private int tId8;
    
    private int selectedTransactionAttribute;

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
        
        vxId1 = graph.addVertex();
        vxId2 = graph.addVertex();
        vxId3 = graph.addVertex();
        vxId4 = graph.addVertex();
        vxId5 = graph.addVertex();
        vxId6 = graph.addVertex();
        vxId7 = graph.addVertex();
        graph.addVertex();
        
        tId1 = graph.addTransaction(vxId1, vxId2, true);
        tId2 = graph.addTransaction(vxId1, vxId3, true);
        tId3 = graph.addTransaction(vxId1, vxId4, true);
        tId4 = graph.addTransaction(vxId2, vxId2, true);
        tId5 = graph.addTransaction(vxId5, vxId4, true);
        tId6 = graph.addTransaction(vxId6, vxId4, true);
        tId7 = graph.addTransaction(vxId6, vxId5, true);
        tId8 = graph.addTransaction(vxId7, vxId7, true);
        
        selectedTransactionAttribute = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of edit method, of class SelectLoopsPlugin.
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testEdit() throws InterruptedException {
        System.out.println("edit");
        
        final SelectLoopsPlugin instance = new SelectLoopsPlugin();
        
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId1));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId2));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId3));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId4));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId5));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId6));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId7));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId8));
        
        instance.edit(graph, null, null);
        
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId1));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId2));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId3));
        assertTrue(graph.getBooleanValue(selectedTransactionAttribute, tId4));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId5));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId6));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId7));
        assertTrue(graph.getBooleanValue(selectedTransactionAttribute, tId8));
    }    
}
