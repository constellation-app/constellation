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
package au.gov.asd.tac.constellation.graph.interaction.plugins.draw;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.LayersConcept;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import static au.gov.asd.tac.constellation.graph.interaction.plugins.draw.CreateTransactionPlugin.DESTINATION_PARAMETER_ID;
import static au.gov.asd.tac.constellation.graph.interaction.plugins.draw.CreateTransactionPlugin.DIRECTED_PARAMETER_ID;
import static au.gov.asd.tac.constellation.graph.interaction.plugins.draw.CreateTransactionPlugin.SOURCE_PARAMETER_ID;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import static org.testng.Assert.assertEquals;
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
public class CreateTransactionPluginNGTest {
    
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
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of createParameters method, of class CreateTransactionPlugin.
     */
    @Test
    public void testCreateParameters() {
        System.out.println("createParameters");
        
        final CreateTransactionPlugin instance = new CreateTransactionPlugin();
        
        final PluginParameters params = instance.createParameters();
        assertEquals(params.getParameters().size(), 3);
        assertTrue(params.getParameters().containsKey(SOURCE_PARAMETER_ID));
        assertTrue(params.getParameters().containsKey(DESTINATION_PARAMETER_ID));
        assertTrue(params.getParameters().containsKey(DIRECTED_PARAMETER_ID));
    }

    /**
     * Test of edit method, of class CreateTransactionPlugin.
     * 
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testEdit() throws InterruptedException {
        System.out.println("edit");
        
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema();
        final StoreGraph graph = new StoreGraph(schema);
        
        final int vxId1 = graph.addVertex();
        final int vxId2 = graph.addVertex();
        
        final int layerMaskAttribute = LayersConcept.TransactionAttribute.LAYER_MASK.ensure(graph);
        LayersConcept.GraphAttribute.LAYER_MASK_SELECTED.ensure(graph);
        
        final CreateTransactionPlugin instance = new CreateTransactionPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setIntegerValue(SOURCE_PARAMETER_ID, vxId2);
        parameters.setIntegerValue(DESTINATION_PARAMETER_ID, vxId1);
        parameters.setBooleanValue(DIRECTED_PARAMETER_ID, false);
        
        assertEquals(graph.getTransactionCount(), 0);
        
        instance.edit(graph, null, parameters);
        
        assertEquals(graph.getTransactionCount(), 1);
        
        final int newTn = graph.getTransaction(0);
        assertEquals(graph.getTransactionSourceVertex(newTn), vxId2);
        assertEquals(graph.getTransactionDestinationVertex(newTn), vxId1);
        assertEquals(graph.getTransactionDirection(newTn), Graph.UNDIRECTED);
        assertEquals(graph.getLongValue(layerMaskAttribute, newTn), 1L);
    }
}
