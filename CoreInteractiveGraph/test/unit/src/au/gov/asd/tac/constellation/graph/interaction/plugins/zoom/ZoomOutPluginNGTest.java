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
package au.gov.asd.tac.constellation.graph.interaction.plugins.zoom;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
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
 * @author Quasar985
 */
public class ZoomOutPluginNGTest {

    private int cameraAttribute;
    private StoreGraph graph;

    public ZoomOutPluginNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // create an analytic graph
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        graph = new StoreGraph(schema);

        // add attributes
        cameraAttribute = VisualConcept.GraphAttribute.CAMERA.ensure(graph);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of edit method, of class ZoomOutPlugin.
     */
    @Test
    public void testEdit() throws Exception {
        System.out.println("Zoom out");

        final PluginInteraction interaction = null;
        final PluginParameters parameters = null;

        final Vector3f expectedVec = new Vector3f(0F, 0F, 2F);
        // Start with a default camera
        final Camera originalCamera = new Camera();
        graph.setObjectValue(cameraAttribute, 0, originalCamera);
        assertTrue(originalCamera.areSame(graph.getObjectValue(cameraAttribute, 0)));

        // Create a plugin and run it, asserting that the camera has moved 10 units
        final ZoomOutPlugin instance = new ZoomOutPlugin();
        instance.edit(graph, interaction, parameters);

        final Camera c = graph.getObjectValue(cameraAttribute, 0);
        // Compare coords, as float arrays
        assertEquals(c.lookAtCentre.a, expectedVec.a);
    }

}
