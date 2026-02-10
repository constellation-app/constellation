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
public class SelectSourcesPluginNGTest {
    
    private StoreGraph graph;
    
    private int vxId1;
    private int vxId2;
    private int vxId3;
    private int vxId4;
    private int vxId5;
    private int vxId6;
    private int vxId7;
    private int vxId8;
    
    private int selectedVertexAttribute;

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
        vxId8 = graph.addVertex();
        
        graph.addTransaction(vxId1, vxId2, true);
        graph.addTransaction(vxId1, vxId4, true);
        graph.addTransaction(vxId1, vxId5, true);
        graph.addTransaction(vxId2, vxId2, true);
        graph.addTransaction(vxId6, vxId5, true);
        graph.addTransaction(vxId7, vxId5, true);
        graph.addTransaction(vxId7, vxId6, true);
        graph.addTransaction(vxId8, vxId8, true);
        
        selectedVertexAttribute = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of edit method, of class SelectSourcesPlugin.
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testEdit() throws InterruptedException {
        System.out.println("edit");
        
        final SelectSourcesPlugin instance = new SelectSourcesPlugin();
        
        assertFalse(graph.getBooleanValue(selectedVertexAttribute, vxId1));
        assertFalse(graph.getBooleanValue(selectedVertexAttribute, vxId2));
        assertFalse(graph.getBooleanValue(selectedVertexAttribute, vxId3));
        assertFalse(graph.getBooleanValue(selectedVertexAttribute, vxId4));
        assertFalse(graph.getBooleanValue(selectedVertexAttribute, vxId5));
        assertFalse(graph.getBooleanValue(selectedVertexAttribute, vxId6));
        assertFalse(graph.getBooleanValue(selectedVertexAttribute, vxId7));
        assertFalse(graph.getBooleanValue(selectedVertexAttribute, vxId8));
        
        instance.edit(graph, null, null);
        
        assertTrue(graph.getBooleanValue(selectedVertexAttribute, vxId1));
        assertFalse(graph.getBooleanValue(selectedVertexAttribute, vxId2));
        assertFalse(graph.getBooleanValue(selectedVertexAttribute, vxId3));
        assertFalse(graph.getBooleanValue(selectedVertexAttribute, vxId4));
        assertFalse(graph.getBooleanValue(selectedVertexAttribute, vxId5));
        assertFalse(graph.getBooleanValue(selectedVertexAttribute, vxId6));
        assertTrue(graph.getBooleanValue(selectedVertexAttribute, vxId7));
        assertFalse(graph.getBooleanValue(selectedVertexAttribute, vxId8));
    }  
}
