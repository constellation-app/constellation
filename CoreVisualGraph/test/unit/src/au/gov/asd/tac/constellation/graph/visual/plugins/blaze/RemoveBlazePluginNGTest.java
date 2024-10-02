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
package au.gov.asd.tac.constellation.graph.visual.plugins.blaze;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.utilities.BlazeUtilities;
import static au.gov.asd.tac.constellation.graph.schema.visual.utilities.BlazeUtilities.VERTEX_IDS_PARAMETER_ID;
import static au.gov.asd.tac.constellation.graph.schema.visual.utilities.BlazeUtilities.VERTEX_ID_PARAMETER_ID;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import java.util.BitSet;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class RemoveBlazePluginNGTest {
    
    private StoreGraph graph;
    private int vxId1;
    private int vxId2;
    private int vxId3;
    
    private int blazeVertexAttribute;
    private int selectedVertexAttribute;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema();
        graph = new StoreGraph(schema);
        
        vxId1 = graph.addVertex();
        vxId2 = graph.addVertex();
        vxId3 = graph.addVertex();
        
        blazeVertexAttribute = VisualConcept.VertexAttribute.BLAZE.ensure(graph);
        selectedVertexAttribute = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        
        graph.setObjectValue(blazeVertexAttribute, vxId1, BlazeUtilities.DEFAULT_BLAZE);
        graph.setObjectValue(blazeVertexAttribute, vxId2, BlazeUtilities.DEFAULT_BLAZE);
        graph.setObjectValue(blazeVertexAttribute, vxId3, BlazeUtilities.DEFAULT_BLAZE);
        
        graph.setBooleanValue(selectedVertexAttribute, vxId1, true);
        graph.setBooleanValue(selectedVertexAttribute, vxId2, true);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of edit method, of class RemoveBlazePlugin. Blaze remove from one node
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testEditOneBlaze() throws InterruptedException {
        System.out.println("editOneBlaze");
        
        final RemoveBlazePlugin instance = new RemoveBlazePlugin();        
        final PluginParameters parameters = instance.createParameters();
        parameters.setIntegerValue(VERTEX_ID_PARAMETER_ID, vxId2);
        
        assertEquals(graph.getObjectValue(blazeVertexAttribute, vxId1), BlazeUtilities.DEFAULT_BLAZE);
        assertEquals(graph.getObjectValue(blazeVertexAttribute, vxId2), BlazeUtilities.DEFAULT_BLAZE);
        assertEquals(graph.getObjectValue(blazeVertexAttribute, vxId3), BlazeUtilities.DEFAULT_BLAZE);
        
        instance.edit(graph, null, parameters);
        
        assertEquals(graph.getObjectValue(blazeVertexAttribute, vxId1), BlazeUtilities.DEFAULT_BLAZE);
        assertNull(graph.getObjectValue(blazeVertexAttribute, vxId2));
        assertEquals(graph.getObjectValue(blazeVertexAttribute, vxId3), BlazeUtilities.DEFAULT_BLAZE);
    }
    
    /**
     * Test of edit method, of class RemoveBlazePlugin. Blaze removed from multiple nodes via bit set
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testEditMultipleBlazesBitSet() throws InterruptedException {
        System.out.println("editMultipleBlazesBitSet");
        
        final RemoveBlazePlugin instance = new RemoveBlazePlugin();        
        final PluginParameters parameters = instance.createParameters();
        final BitSet vertices = new BitSet();
        vertices.set(vxId1);
        vertices.set(vxId3);
        parameters.setObjectValue(VERTEX_IDS_PARAMETER_ID, vertices);
        
        assertEquals(graph.getObjectValue(blazeVertexAttribute, vxId1), BlazeUtilities.DEFAULT_BLAZE);
        assertEquals(graph.getObjectValue(blazeVertexAttribute, vxId2), BlazeUtilities.DEFAULT_BLAZE);
        assertEquals(graph.getObjectValue(blazeVertexAttribute, vxId3), BlazeUtilities.DEFAULT_BLAZE);
        
        instance.edit(graph, null, parameters);
        
        assertNull(graph.getObjectValue(blazeVertexAttribute, vxId1));
        assertEquals(graph.getObjectValue(blazeVertexAttribute, vxId2), BlazeUtilities.DEFAULT_BLAZE);
        assertNull(graph.getObjectValue(blazeVertexAttribute, vxId3));
    }
    
    /**
     * Test of edit method, of class RemoveBlazePlugin. Blaze removed from multiple nodes via selection
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testEditMultipleBlazesSelected() throws InterruptedException {
        System.out.println("editMultipleBlazesSelected");
        
        final RemoveBlazePlugin instance = new RemoveBlazePlugin();        
        final PluginParameters parameters = instance.createParameters();
        
        assertEquals(graph.getObjectValue(blazeVertexAttribute, vxId1), BlazeUtilities.DEFAULT_BLAZE);
        assertEquals(graph.getObjectValue(blazeVertexAttribute, vxId2), BlazeUtilities.DEFAULT_BLAZE);
        assertEquals(graph.getObjectValue(blazeVertexAttribute, vxId3), BlazeUtilities.DEFAULT_BLAZE);
        
        instance.edit(graph, null, parameters);
        
        assertNull(graph.getObjectValue(blazeVertexAttribute, vxId1));
        assertNull(graph.getObjectValue(blazeVertexAttribute, vxId2));
        assertEquals(graph.getObjectValue(blazeVertexAttribute, vxId3), BlazeUtilities.DEFAULT_BLAZE);
    }
}
