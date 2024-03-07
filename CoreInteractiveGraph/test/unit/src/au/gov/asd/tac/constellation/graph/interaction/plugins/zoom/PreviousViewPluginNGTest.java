/*
 * Copyright 2010-2021 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author aldebaran30701
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class PreviousViewPluginNGTest extends ConstellationTest {
    
    private int vertexIdentifierAttribute, cameraAttribute;
    private int vxId1, vxId2;
    private StoreGraph graph;
    
    public PreviousViewPluginNGTest() {
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
        vertexIdentifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        cameraAttribute = VisualConcept.GraphAttribute.CAMERA.ensure(graph);

        // add vertices
        vxId1 = graph.addVertex();
        vxId2 = graph.addVertex();

        // set the identifier of the vertices 
        graph.setStringValue(vertexIdentifierAttribute, vxId1, "VERTEX_1");
        graph.setStringValue(vertexIdentifierAttribute, vxId2, "VERTEX_2");
        

    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of edit method, of class PreviousViewPlugin.
     */
    @Test
    public void testEdit() throws Exception {
        System.out.println("edit");
        
        // Create some vecs to add to the original camera
        final Vector3f lookAtCentre = new Vector3f(10,10,10);
        final Vector3f lookAtEye = new Vector3f(5,10,5);
        final Vector3f lookAtUp = new Vector3f(2,2,2);
        
        // Start with a default camera
        final Camera originalCamera = new Camera();
        graph.setObjectValue(cameraAttribute, 0, originalCamera);
        assertTrue(originalCamera.areSame(graph.getObjectValue(cameraAttribute, 0)));
        
        // Deep copy the default camera before adding vecs to it
        final Camera clonedCamera = new Camera(originalCamera);
        clonedCamera.lookAtCentre.add(lookAtCentre);
        clonedCamera.lookAtEye.add(lookAtEye);
        clonedCamera.lookAtUp.add(lookAtUp);
        
        // Set the previous positions to the original camera
        clonedCamera.lookAtPreviousEye.set(originalCamera.lookAtEye);
        clonedCamera.lookAtPreviousCentre.set(originalCamera.lookAtCentre);
        clonedCamera.lookAtPreviousUp.set(originalCamera.lookAtUp);
        
        graph.setObjectValue(cameraAttribute, 0, clonedCamera);
        
        // Assert that the camera is set correctly to the cloned, changed camera
        assertTrue(clonedCamera.areSame(graph.getObjectValue(cameraAttribute, 0)));
        
        // Create a plugin and run it, asserting that the camera is now back to the original camera
        final PreviousViewPlugin instance = new PreviousViewPlugin();
        instance.edit(graph, null, null);
        assertTrue(originalCamera.areSame(graph.getObjectValue(cameraAttribute, 0)));
    }
}
