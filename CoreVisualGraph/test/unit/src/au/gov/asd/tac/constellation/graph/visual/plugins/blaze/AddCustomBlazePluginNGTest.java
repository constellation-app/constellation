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
import static au.gov.asd.tac.constellation.graph.schema.visual.utilities.BlazeUtilities.COLOR_PARAMETER_ID;
import static au.gov.asd.tac.constellation.graph.schema.visual.utilities.BlazeUtilities.VERTEX_IDS_PARAMETER_ID;
import static au.gov.asd.tac.constellation.graph.schema.visual.utilities.BlazeUtilities.VERTEX_ID_PARAMETER_ID;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
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
public class AddCustomBlazePluginNGTest {
    
    private StoreGraph graph;
    private int vxId1;
    private int vxId2;
    private int vxId3;
    
    private int blazeVertexAttribute;
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
        
        blazeVertexAttribute = VisualConcept.VertexAttribute.BLAZE.ensure(graph);
        selectedVertexAttribute = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        
        graph.setBooleanValue(selectedVertexAttribute, vxId1, true);
        graph.setBooleanValue(selectedVertexAttribute, vxId2, true);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }
    
    /**
     * Test of edit method, of class AddCustomBlazePlugin. Blaze added to one node, default color
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testEditOneBlazeDefaultColor() throws InterruptedException {
        System.out.println("editOneBlazeDefaultColor");
        
        final AddCustomBlazePlugin instance = new AddCustomBlazePlugin();        
        final PluginParameters parameters = instance.createParameters();
        parameters.setIntegerValue(VERTEX_ID_PARAMETER_ID, vxId2);
        
        assertNull(graph.getObjectValue(blazeVertexAttribute, vxId1));
        assertNull(graph.getObjectValue(blazeVertexAttribute, vxId2));
        assertNull(graph.getObjectValue(blazeVertexAttribute, vxId3));
        
        instance.edit(graph, null, parameters);
        
        assertNull(graph.getObjectValue(blazeVertexAttribute, vxId1));
        assertEquals(graph.getObjectValue(blazeVertexAttribute, vxId2), BlazeUtilities.DEFAULT_BLAZE);
        assertNull(graph.getObjectValue(blazeVertexAttribute, vxId3));
    }
    
    /**
     * Test of edit method, of class AddCustomBlazePlugin. Blaze added to one node
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testEditOneBlaze() throws InterruptedException {
        System.out.println("editOneBlaze");
        
        final AddCustomBlazePlugin instance = new AddCustomBlazePlugin();        
        final PluginParameters parameters = instance.createParameters();
        parameters.setIntegerValue(VERTEX_ID_PARAMETER_ID, vxId2);
        parameters.setColorValue(COLOR_PARAMETER_ID, ConstellationColor.BANANA);
        
        assertNull(graph.getObjectValue(blazeVertexAttribute, vxId1));
        assertNull(graph.getObjectValue(blazeVertexAttribute, vxId2));
        assertNull(graph.getObjectValue(blazeVertexAttribute, vxId3));
        
        instance.edit(graph, null, parameters);
        
        assertNull(graph.getObjectValue(blazeVertexAttribute, vxId1));
        assertEquals(graph.getStringValue(blazeVertexAttribute, vxId2), "45;Banana");
        assertNull(graph.getObjectValue(blazeVertexAttribute, vxId3));
    }
    
    /**
     * Test of edit method, of class AddBlazePlugin. Blaze added to multiple nodes via bit set
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testEditMultipleBlazesBitSet() throws InterruptedException {
        System.out.println("editMultipleBlazesBitSet");
        
        final AddCustomBlazePlugin instance = new AddCustomBlazePlugin();        
        final PluginParameters parameters = instance.createParameters();
        final BitSet vertices = new BitSet();
        vertices.set(vxId1);
        vertices.set(vxId3);
        parameters.setObjectValue(VERTEX_IDS_PARAMETER_ID, vertices);
        parameters.setColorValue(COLOR_PARAMETER_ID, ConstellationColor.BANANA);
        
        assertNull(graph.getObjectValue(blazeVertexAttribute, vxId1));
        assertNull(graph.getObjectValue(blazeVertexAttribute, vxId2));
        assertNull(graph.getObjectValue(blazeVertexAttribute, vxId3));
        
        instance.edit(graph, null, parameters);
        
        assertEquals(graph.getStringValue(blazeVertexAttribute, vxId1), "45;Banana");
        assertNull(graph.getObjectValue(blazeVertexAttribute, vxId2));
        assertEquals(graph.getStringValue(blazeVertexAttribute, vxId3), "45;Banana");
    }
    
    /**
     * Test of edit method, of class AddCustomBlazePlugin. Blaze added to multiple nodes via selection
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testEditMultipleBlazesSelected() throws InterruptedException {
        System.out.println("editMultipleBlazesSelected");
        
        final AddCustomBlazePlugin instance = new AddCustomBlazePlugin();        
        final PluginParameters parameters = instance.createParameters();
        parameters.setColorValue(COLOR_PARAMETER_ID, ConstellationColor.BANANA);
        
        assertNull(graph.getObjectValue(blazeVertexAttribute, vxId1));
        assertNull(graph.getObjectValue(blazeVertexAttribute, vxId2));
        assertNull(graph.getObjectValue(blazeVertexAttribute, vxId3));
        
        instance.edit(graph, null, parameters);
        
        assertEquals(graph.getStringValue(blazeVertexAttribute, vxId1), "45;Banana");
        assertEquals(graph.getStringValue(blazeVertexAttribute, vxId2), "45;Banana");
        assertNull(graph.getObjectValue(blazeVertexAttribute, vxId3));
    }
}
