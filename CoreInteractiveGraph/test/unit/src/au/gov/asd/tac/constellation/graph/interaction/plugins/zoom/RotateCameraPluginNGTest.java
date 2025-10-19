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
import static au.gov.asd.tac.constellation.graph.interaction.plugins.zoom.RotateCameraPlugin.ANIMATE_PARAMETER_ID;
import static au.gov.asd.tac.constellation.graph.interaction.plugins.zoom.RotateCameraPlugin.X_PARAMETER_ID;
import static au.gov.asd.tac.constellation.graph.interaction.plugins.zoom.RotateCameraPlugin.Y_PARAMETER_ID;
import static au.gov.asd.tac.constellation.graph.interaction.plugins.zoom.RotateCameraPlugin.Z_PARAMETER_ID;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
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
public class RotateCameraPluginNGTest {
    
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
     * Test of createParameters method, of class RotateCameraPlugin.
     */
    @Test
    public void testCreateParameters() {
        System.out.println("createParameters");
        
        final RotateCameraPlugin instance = new RotateCameraPlugin();
        
        final PluginParameters params = instance.createParameters();
        assertEquals(params.getParameters().size(), 4);
        assertTrue(params.getParameters().containsKey(X_PARAMETER_ID));
        assertTrue(params.getParameters().containsKey(Y_PARAMETER_ID));
        assertTrue(params.getParameters().containsKey(Z_PARAMETER_ID));
        assertTrue(params.getParameters().containsKey(ANIMATE_PARAMETER_ID));
    }

    /**
     * Test of edit method, of class RotateCameraPlugin.
     * 
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testEdit() throws InterruptedException, PluginException {
        System.out.println("edit");
        
        final RotateCameraPlugin instance = new RotateCameraPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setFloatValue(X_PARAMETER_ID, 90F);
        parameters.setFloatValue(Y_PARAMETER_ID, 90F);
        parameters.setFloatValue(Z_PARAMETER_ID, 90F);
        
        final Camera beforeCamera = graph.getObjectValue(cameraAttribute, 0);
        assertEquals(beforeCamera.lookAtCentre, new Vector3f(0F, 0F, 0F));
        assertEquals(beforeCamera.lookAtEye, new Vector3f(0F, 0F, 10F));
        assertEquals(beforeCamera.lookAtUp, new Vector3f(0F, 1F, 0F));
        
        instance.edit(graph, null, parameters);
        
        final Camera afterCamera = graph.getObjectValue(cameraAttribute, 0);
        assertEquals(afterCamera.lookAtCentre, new Vector3f(0F, 0F, 0F));
        // in an ideal world, this one should be 10,0,0
        // unfortunately the conversion from degrees to radians in the calculations can only be approximate
        // and so subsequent calculations using that will also not quite the expected correct answer
        assertEquals(afterCamera.lookAtEye, new Vector3f(9.999999F, 4.3711384e-7F, 3.5527133e-14F));
        // similarly this would be 0,-1,0 in an ideal world
        assertEquals(afterCamera.lookAtUp, new Vector3f(4.3711385e-8F, -0.99999994F, -8.742277e-8F));
    }   
}
