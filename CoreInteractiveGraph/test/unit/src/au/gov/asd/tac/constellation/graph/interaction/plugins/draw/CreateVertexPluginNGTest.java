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

import au.gov.asd.tac.constellation.graph.LayersConcept;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import static au.gov.asd.tac.constellation.graph.interaction.plugins.draw.CreateVertexPlugin.X_PARAMETER_ID;
import static au.gov.asd.tac.constellation.graph.interaction.plugins.draw.CreateVertexPlugin.Y_PARAMETER_ID;
import static au.gov.asd.tac.constellation.graph.interaction.plugins.draw.CreateVertexPlugin.Z_PARAMETER_ID;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
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
public class CreateVertexPluginNGTest {
    
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
     * Test of createParameters method, of class CreateVertexPlugin.
     */
    @Test
    public void testCreateParameters() {
        System.out.println("createParameters");
        
        final CreateVertexPlugin instance = new CreateVertexPlugin();
        
        final PluginParameters params = instance.createParameters();
        assertEquals(params.getParameters().size(), 3);
        assertTrue(params.getParameters().containsKey(X_PARAMETER_ID));
        assertTrue(params.getParameters().containsKey(Y_PARAMETER_ID));
        assertTrue(params.getParameters().containsKey(Z_PARAMETER_ID));
    }

    /**
     * Test of edit method, of class CreateVertexPlugin.
     * 
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testEdit() throws InterruptedException {
        System.out.println("edit");
        
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema();
        final StoreGraph graph = new StoreGraph(schema);
        
        final int xAttribute = VisualConcept.VertexAttribute.X.ensure(graph);
        final int yAttribute = VisualConcept.VertexAttribute.Y.ensure(graph);
        final int zAttribute = VisualConcept.VertexAttribute.Z.ensure(graph);
        
        final int layerMaskAttribute = LayersConcept.VertexAttribute.LAYER_MASK.ensure(graph);
        LayersConcept.GraphAttribute.LAYER_MASK_SELECTED.ensure(graph);
        
        final CreateVertexPlugin instance = new CreateVertexPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setFloatValue(X_PARAMETER_ID, 1F);
        parameters.setFloatValue(Y_PARAMETER_ID, 2F);
        parameters.setFloatValue(Z_PARAMETER_ID, 3F);
        
        assertEquals(graph.getVertexCount(), 0);
        
        instance.edit(graph, null, parameters);
        
        assertEquals(graph.getVertexCount(), 1);
        
        final int newVx = graph.getVertex(0);
        assertEquals(graph.getFloatValue(xAttribute, newVx), 1F);
        assertEquals(graph.getFloatValue(yAttribute, newVx), 2F);
        assertEquals(graph.getFloatValue(zAttribute, newVx), 3F);
        assertEquals(graph.getLongValue(layerMaskAttribute, newVx), 1L);
    }
}
