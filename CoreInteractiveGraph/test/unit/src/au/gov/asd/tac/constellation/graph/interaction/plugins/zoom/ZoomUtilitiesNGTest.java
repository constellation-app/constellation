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

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class ZoomUtilitiesNGTest {

    private int cameraAttribute;
    private StoreGraph graph;

    public ZoomUtilitiesNGTest() {
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
     * Test of closestNodeCameraCoordinates method, of class ZoomUtilities. When graph is null
     */
    @Test
    public void closestNodeCameraCoordinatesNoGraph() {
        System.out.println("closestNodeCameraCoordinatesNoGraph");

        GraphWriteMethods graph = null;

        Vector3f expResult = null;
        Vector3f result = ZoomUtilities.closestNodeCameraCoordinates(graph);
        assertEquals(result, expResult);
    }

    /**
     * Test of closestNodeCameraCoordinates method, of class ZoomUtilities. When graph exists, but camera is null
     */
    @Test
    public void closestNodeCameraCoordinatesGraphNoCamera() {
        System.out.println("closestNodeCameraCoordinatesGraphNoCamera");

        graph.setObjectValue(cameraAttribute, 0, null);

        Vector3f expResult = null;
        Vector3f result = ZoomUtilities.closestNodeCameraCoordinates(graph);
        assertEquals(result, expResult);
    }

    /**
     * Test of closestNodeCameraCoordinates method, of class ZoomUtilities. When graph and camera exist, but no nodes are in
     * graph
     */
    @Test
    public void closestNodeCameraCoordinatesEmptyGraph() {
        System.out.println("closestNodeCameraCoordinatesEmptyGraph");

        final Camera originalCamera = new Camera();
        graph.setObjectValue(cameraAttribute, 0, originalCamera);

        Vector3f expResult = null;
        Vector3f result = ZoomUtilities.closestNodeCameraCoordinates(graph);
        assertEquals(result, expResult);
    }

}
