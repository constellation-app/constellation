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
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;
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
public class ZoomUtilitiesNGTest {

    private StoreGraph graph;
    
    private int cameraAttribute;
    private int xAttribute;
    private int yAttribute;
    private int zAttribute;
    
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

        // add attributes
        cameraAttribute = VisualConcept.GraphAttribute.CAMERA.ensure(graph);
        xAttribute = VisualConcept.VertexAttribute.X.ensure(graph);
        yAttribute = VisualConcept.VertexAttribute.Y.ensure(graph);
        zAttribute = VisualConcept.VertexAttribute.Z.ensure(graph);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of zoom method, of class ZoomUtilities.
     */
    @Test
    public void testZoom() {
        System.out.println("zoom");
        
        final int vxId1 = graph.addVertex();
        final int vxId2 = graph.addVertex();
        final int vxId3 = graph.addVertex();
        
        graph.setFloatValue(xAttribute, vxId1, 0F);
        graph.setFloatValue(yAttribute, vxId1, 1F);
        graph.setFloatValue(zAttribute, vxId1, 2F);
        
        graph.setFloatValue(xAttribute, vxId2, -5F);
        graph.setFloatValue(yAttribute, vxId2, -6F);
        graph.setFloatValue(zAttribute, vxId2, -7F);
        
        graph.setFloatValue(xAttribute, vxId3, 10F);
        graph.setFloatValue(yAttribute, vxId3, 11F);
        graph.setFloatValue(zAttribute, vxId3, 12F);
        
        final Camera beforeCamera = graph.getObjectValue(cameraAttribute, 0);
        assertEquals(beforeCamera, new Camera());
        
        ZoomUtilities.zoom(graph, 2, new Vector3f(1, 2, 2));
        
        final Camera afterCamera = graph.getObjectValue(cameraAttribute, 0);
        assertNotEquals(afterCamera, new Camera());
        assertEquals(afterCamera.lookAtCentre, new Vector3f(-2F/3, -4F/3, -4F/3));
        assertEquals(afterCamera.lookAtEye, new Vector3f(-2F/3, -4F/3, 26F/3));
    }
    
    /**
     * Test of closestNodeCameraCoordinates method, of class ZoomUtilities. When graph is null
     */
    @Test
    public void testClosestNodeCameraCoordinatesNoGraph() {
        System.out.println("closestNodeCameraCoordinatesNoGraph");
        
        assertNull(ZoomUtilities.closestNodeCameraCoordinates(null));
    }

    /**
     * Test of closestNodeCameraCoordinates method, of class ZoomUtilities. When graph exists, but camera is null
     */
    @Test
    public void testClosestNodeCameraCoordinatesGraphNoCamera() {
        System.out.println("closestNodeCameraCoordinatesGraphNoCamera");

        graph.setObjectValue(cameraAttribute, 0, null);
        
        assertNull(ZoomUtilities.closestNodeCameraCoordinates(graph));
    }

    /**
     * Test of closestNodeCameraCoordinates method, of class ZoomUtilities. When graph and camera exist, but no nodes are in
     * graph
     */
    @Test
    public void testClosestNodeCameraCoordinatesEmptyGraph() {
        System.out.println("closestNodeCameraCoordinatesEmptyGraph");
        
        assertNull(ZoomUtilities.closestNodeCameraCoordinates(graph));
    }
    
    /**
     * Test of closestNodeCameraCoordinates method, of class ZoomUtilities. When graph and camera exist, and nodes are on the graph
     */
    @Test
    public void testClosestNodeCameraCoordinates() {
        System.out.println("closestNodeCameraCoordinates");

        final int vxId1 = graph.addVertex();
        final int vxId2 = graph.addVertex();
        final int vxId3 = graph.addVertex();
        
        graph.setFloatValue(xAttribute, vxId1, 0F);
        graph.setFloatValue(yAttribute, vxId1, 1F);
        graph.setFloatValue(zAttribute, vxId1, 2F);
        
        graph.setFloatValue(xAttribute, vxId2, -5F);
        graph.setFloatValue(yAttribute, vxId2, -6F);
        graph.setFloatValue(zAttribute, vxId2, -7F);
        
        graph.setFloatValue(xAttribute, vxId3, 10F);
        graph.setFloatValue(yAttribute, vxId3, 11F);
        graph.setFloatValue(zAttribute, vxId3, 12F);
        
        final Vector3f result = ZoomUtilities.closestNodeCameraCoordinates(graph);
        assertTrue(result.areSame(new Vector3f(0F, 1F, -8F)));
    }
}
