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
package au.gov.asd.tac.constellation.graph.interaction.plugins.zoom;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import static au.gov.asd.tac.constellation.graph.interaction.plugins.zoom.SetCameraVisibilityRange.VISIBILITY_HIGH_ID;
import static au.gov.asd.tac.constellation.graph.interaction.plugins.zoom.SetCameraVisibilityRange.VISIBILITY_LOW_ID;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
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
public class SetCameraVisibilityRangeNGTest {
    
    private StoreGraph graph;
    
    private int cameraAttribute;
    
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
        
        cameraAttribute = VisualConcept.GraphAttribute.CAMERA.ensure(graph);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }
    
    /**
     * Test of createParameters method, of class SetCameraVisibilityRange.
     */
    @Test
    public void testCreateParameters() {
        System.out.println("createParameters");
        
        final SetCameraVisibilityRange instance = new SetCameraVisibilityRange();
        
        final PluginParameters params = instance.createParameters();
        assertEquals(params.getParameters().size(), 2);
        assertTrue(params.getParameters().containsKey(VISIBILITY_LOW_ID));
        assertTrue(params.getParameters().containsKey(VISIBILITY_HIGH_ID));
    }

    /**
     * Test of edit method, of class SetCameraVisibilityRange.
     * 
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testEdit() throws InterruptedException, PluginException {
        System.out.println("edit");
        
        final SetCameraVisibilityRange instance = new SetCameraVisibilityRange();
        final PluginParameters parameters = instance.createParameters();
        parameters.setFloatValue(VISIBILITY_LOW_ID, 0.2F);
        parameters.setFloatValue(VISIBILITY_HIGH_ID, 0.7F);
        
        final Camera beforeCamera = graph.getObjectValue(cameraAttribute, 0);
        assertEquals(beforeCamera.getVisibilityLow(), 0F);
        assertEquals(beforeCamera.getVisibilityHigh(), 1F);
        
        instance.edit(graph, null, parameters);
        
        final Camera afterCamera = graph.getObjectValue(cameraAttribute, 0);
        assertEquals(afterCamera.getVisibilityLow(), 0.2);
        assertEquals(afterCamera.getVisibilityHigh(), 0.7);
    }
}
