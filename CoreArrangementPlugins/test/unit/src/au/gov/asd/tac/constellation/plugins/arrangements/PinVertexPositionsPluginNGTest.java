/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.arrangements;

import au.gov.asd.tac.constellation.graph.StoreGraph;
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
public class PinVertexPositionsPluginNGTest {
    
    private StoreGraph graph;
    
    private int vxId1;
    private int vxId2;
    private int vxId3;
    private int vxId4;
    
    private int selectedVertexAttribute;
    private int pinnedVertexAttribute;

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
        
        vxId1 = graph.addVertex();
        vxId2 = graph.addVertex();
        vxId3 = graph.addVertex();
        vxId4 = graph.addVertex();
        
        selectedVertexAttribute = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        pinnedVertexAttribute = VisualConcept.VertexAttribute.PINNED.ensure(graph);
        
        graph.setBooleanValue(selectedVertexAttribute, vxId1, true);
        graph.setBooleanValue(selectedVertexAttribute, vxId2, true);
        
        graph.setBooleanValue(pinnedVertexAttribute, vxId1, true);
        graph.setBooleanValue(pinnedVertexAttribute, vxId3, true);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of edit method, of class PinVertexPositionsPlugin.
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testEdit() throws InterruptedException {
        System.out.println("edit");
        
        assertTrue(graph.getBooleanValue(pinnedVertexAttribute, vxId1));
        assertFalse(graph.getBooleanValue(pinnedVertexAttribute, vxId2));
        assertTrue(graph.getBooleanValue(pinnedVertexAttribute, vxId3));
        assertFalse(graph.getBooleanValue(pinnedVertexAttribute, vxId4));
        
        final PinVertexPositionsPlugin instance = new PinVertexPositionsPlugin();
        instance.edit(graph, null, null);
        
        // should remain pinned (already being pinned shouldn't affect it)
        assertTrue(graph.getBooleanValue(pinnedVertexAttribute, vxId1));
        // should become pinned since it was selected
        assertTrue(graph.getBooleanValue(pinnedVertexAttribute, vxId2));
        // remains pinned despite not being selected (plugin shouldn't replace pinned group)
        assertTrue(graph.getBooleanValue(pinnedVertexAttribute, vxId3));
        // remains unpinned
        assertFalse(graph.getBooleanValue(pinnedVertexAttribute, vxId4));
    }
}
