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
package au.gov.asd.tac.constellation.graph.visual.utilities;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.ConnectionMode;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.visual.framework.VisualGraphDefaults;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import au.gov.asd.tac.constellation.utilities.visual.DrawFlags;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
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
public class VisualGraphUtilitiesNGTest {
    
    private StoreGraph graph;
    private DualGraph dGraph;
    private int vxId1;
    private int vxId2;
    private int vxId3;
    
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
        
        selectedVertexAttribute = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        
        graph.setBooleanValue(selectedVertexAttribute, vxId1, true);
        graph.setBooleanValue(selectedVertexAttribute, vxId3, true);
        
        dGraph = new DualGraph(schema, graph);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of setCamera method, of class VisualGraphUtilities.
     */
    @Test
    public void testGetSetCamera() {
        System.out.println("getSetCamera");
        
        final Camera camera = new Camera();
        camera.setVisibilityLow(0.3F);
        camera.setVisibilityHigh(0.4F);
        
        // demonstrating here that the new camera is different from the default 
        // to provide more credibility to the asserts below
        assertNotEquals(camera, VisualGraphDefaults.DEFAULT_CAMERA);
        
        // can't set camera with camera attribute not defined. Should return default
        VisualGraphUtilities.setCamera(graph, camera);
        assertEquals(VisualGraphUtilities.getCamera(graph), VisualGraphDefaults.DEFAULT_CAMERA);
        
        // camera attribute defined. Should return camera to set
        VisualConcept.GraphAttribute.CAMERA.ensure(graph);
        VisualGraphUtilities.setCamera(graph, camera);
        assertEquals(VisualGraphUtilities.getCamera(graph), camera);
    }


    /**
     * Test of setVertexCoordinates method, of class VisualGraphUtilities.
     */
    @Test
    public void testGetSetVertexCoordinates() {
        System.out.println("getSetVertexCoordinates");
        
        // attributes don't exist, should return default values
        final Vector3f coordinates = new Vector3f(1F, 2F, 3.4F);
        VisualGraphUtilities.setVertexCoordinates(graph, coordinates, vxId1);
        
        final Vector3f resultVector1 = VisualGraphUtilities.getVertexCoordinates(graph, vxId1);
        assertEquals(resultVector1.getX(), VisualGraphDefaults.getDefaultX(vxId1));
        assertEquals(resultVector1.getY(), VisualGraphDefaults.getDefaultY(vxId1));
        assertEquals(resultVector1.getZ(), VisualGraphDefaults.getDefaultZ(vxId1));
        
        VisualConcept.VertexAttribute.X.ensure(graph);
        VisualConcept.VertexAttribute.Y.ensure(graph);
        VisualConcept.VertexAttribute.Z.ensure(graph);
        
        VisualGraphUtilities.setVertexCoordinates(graph, coordinates, vxId1);
        
        final Vector3f resultVector2 = VisualGraphUtilities.getVertexCoordinates(graph, vxId1);       
        assertEquals(resultVector2.getX(), 1F);
        assertEquals(resultVector2.getY(), 2F);
        assertEquals(resultVector2.getZ(), 3.4F);
    }

    /**
     * Test of getAlternateVertexCoordinates method, of class VisualGraphUtilities.
     */
    @Test
    public void testGetAlternateVertexCoordinates() {
        System.out.println("getAlternateVertexCoordinates");
        
        // attributes don't exist, should return default values        
        final Vector3f resultVector1 = VisualGraphUtilities.getAlternateVertexCoordinates(graph, vxId1);
        assertEquals(resultVector1.getX(), VisualGraphDefaults.DEFAULT_VERTEX_X2);
        assertEquals(resultVector1.getY(), VisualGraphDefaults.DEFAULT_VERTEX_Y2);
        assertEquals(resultVector1.getZ(), VisualGraphDefaults.DEFAULT_VERTEX_Z2);
        
        final int x2VertexAttribute = VisualConcept.VertexAttribute.X2.ensure(graph);
        final int y2VertexAttribute = VisualConcept.VertexAttribute.Y2.ensure(graph);
        final int z2VertexAttribute = VisualConcept.VertexAttribute.Z2.ensure(graph);
        
        graph.setFloatValue(x2VertexAttribute, vxId1, 1F);
        graph.setFloatValue(y2VertexAttribute, vxId1, 2F);
        graph.setFloatValue(z2VertexAttribute, vxId1, 3.4F);
        
        final Vector3f resultVector2 = VisualGraphUtilities.getAlternateVertexCoordinates(graph, vxId1);       
        assertEquals(resultVector2.getX(), 1F);
        assertEquals(resultVector2.getY(), 2F);
        assertEquals(resultVector2.getZ(), 3.4F);
    }

    /**
     * Test of getMixedVertexCoordinates method, of class VisualGraphUtilities.
     */
    @Test
    public void testGetMixedVertexCoordinates() {
        System.out.println("getMixedVertexCoordinates");
        
        VisualConcept.VertexAttribute.X.ensure(graph);
        VisualConcept.VertexAttribute.Y.ensure(graph);
        VisualConcept.VertexAttribute.Z.ensure(graph);
        final int x2VertexAttribute = VisualConcept.VertexAttribute.X2.ensure(graph);
        final int y2VertexAttribute = VisualConcept.VertexAttribute.Y2.ensure(graph);
        final int z2VertexAttribute = VisualConcept.VertexAttribute.Z2.ensure(graph);
        VisualConcept.GraphAttribute.CAMERA.ensure(graph);
        
        final Vector3f coordinates = new Vector3f(1F, 2F, 3F);
        graph.setFloatValue(x2VertexAttribute, vxId1, 4F);
        graph.setFloatValue(y2VertexAttribute, vxId1, 5F);
        graph.setFloatValue(z2VertexAttribute, vxId1, 6F);
        final Camera camera = new Camera();
        camera.setMixRatio(3);
        
        VisualGraphUtilities.setVertexCoordinates(graph, coordinates, vxId1);        
        VisualGraphUtilities.setCamera(graph, camera);
        
        final Vector3f resultVector = VisualGraphUtilities.getMixedVertexCoordinates(graph, vxId1);
        assertEquals(resultVector.getX(), 10.0F);
        assertEquals(resultVector.getY(), 11.0F);
        assertEquals(resultVector.getZ(), 12.0F);
    }

    /**
     * Test of getConnectionMode method, of class VisualGraphUtilities.
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testGetConnectionMode() throws InterruptedException {
        System.out.println("getConnectionMode");
        
        assertEquals(VisualGraphUtilities.getConnectionMode(dGraph), VisualGraphDefaults.DEFAULT_CONNECTION_MODE);
        
        final WritableGraph wg = dGraph.getWritableGraph("Connection Mode test", true);
        try {
            final int connectionModeGraphAttribute = VisualConcept.GraphAttribute.CONNECTION_MODE.ensure(wg);
            wg.setObjectValue(connectionModeGraphAttribute, 0, ConnectionMode.TRANSACTION);
        } finally {
            wg.commit();
        }
        
        assertEquals(VisualGraphUtilities.getConnectionMode(dGraph), ConnectionMode.TRANSACTION);
        assertNotEquals(VisualGraphUtilities.getConnectionMode(dGraph), VisualGraphDefaults.DEFAULT_CONNECTION_MODE);
    }

    /**
     * Test of isDisplayModeIn3D method, of class VisualGraphUtilities.
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testIsDisplayModeIn3D() throws InterruptedException {
        System.out.println("isDisplayModeIn3D");
        
        assertEquals(VisualGraphUtilities.isDisplayModeIn3D(dGraph), VisualGraphDefaults.DEFAULT_DISPLAY_MODE_3D);
        
        final WritableGraph wg = dGraph.getWritableGraph("Is Display Mode test", true);
        try {
            final int displayModeGraphAttribute = VisualConcept.GraphAttribute.DISPLAY_MODE_3D.ensure(wg);
            wg.setBooleanValue(displayModeGraphAttribute, 0, false);
        } finally {
            wg.commit();
        }
        
        assertFalse(VisualGraphUtilities.isDisplayModeIn3D(dGraph));
        assertNotEquals(VisualGraphUtilities.isDisplayModeIn3D(dGraph), VisualGraphDefaults.DEFAULT_DISPLAY_MODE_3D);
    }

    /**
     * Test of getDrawFlags method, of class VisualGraphUtilities.
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testGetDrawFlags() throws InterruptedException {
        System.out.println("getDrawFlags");
        
        assertEquals(VisualGraphUtilities.getDrawFlags(dGraph), VisualGraphDefaults.DEFAULT_DRAW_FLAGS.getFlags());
        
        final WritableGraph wg = dGraph.getWritableGraph("Draw Flags test", true);
        try {
            final int drawFlagsGraphAttribute = VisualConcept.GraphAttribute.DRAW_FLAGS.ensure(wg);
            wg.setIntValue(drawFlagsGraphAttribute, 0, DrawFlags.CONNECTION_LABELS);
        } finally {
            wg.commit();
        }
        
        assertEquals(VisualGraphUtilities.getDrawFlags(dGraph), DrawFlags.CONNECTION_LABELS);
        assertNotEquals(VisualGraphUtilities.getDrawFlags(dGraph), VisualGraphDefaults.DEFAULT_DRAW_FLAGS.getFlags());
    }

    /**
     * Test of isDrawingMode method, of class VisualGraphUtilities.
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testIsDrawingMode() throws InterruptedException {
        System.out.println("isDrawingMode");
        
        assertEquals(VisualGraphUtilities.isDrawingMode(dGraph), VisualGraphDefaults.DEFAULT_DRAWING_MODE);
        
        final WritableGraph wg = dGraph.getWritableGraph("Is Drawing Mode test", true);
        try {
            final int drawingModeGraphAttribute = VisualConcept.GraphAttribute.DRAWING_MODE.ensure(wg);
            wg.setBooleanValue(drawingModeGraphAttribute, 0, true);
        } finally {
            wg.commit();
        }
        
        assertTrue(VisualGraphUtilities.isDrawingMode(dGraph));
        assertNotEquals(VisualGraphUtilities.isDrawingMode(dGraph), VisualGraphDefaults.DEFAULT_DRAWING_MODE);
    }

    /**
     * Test of isDrawingDirectedTransactions method, of class VisualGraphUtilities.
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testIsDrawingDirectedTransactions() throws InterruptedException {
        System.out.println("isDrawingDirectedTransactions");
        
        assertEquals(VisualGraphUtilities.isDrawingDirectedTransactions(dGraph), VisualGraphDefaults.DEFAULT_DRAWING_DIRECTED_TRANSACTIONS);
        
        final WritableGraph wg = dGraph.getWritableGraph("Is Drawing Directed Transactions test", true);
        try {
            final int drawDirectedTransactionsGraphAttribute = VisualConcept.GraphAttribute.DRAW_DIRECTED_TRANSACTIONS.ensure(wg);
            wg.setBooleanValue(drawDirectedTransactionsGraphAttribute, 0, false);
        } finally {
            wg.commit();
        }
        
        assertFalse(VisualGraphUtilities.isDrawingDirectedTransactions(dGraph));
        assertNotEquals(VisualGraphUtilities.isDrawingDirectedTransactions(dGraph), VisualGraphDefaults.DEFAULT_DRAWING_DIRECTED_TRANSACTIONS);
    }

    /**
     * Test of streamVertexWorldLocations method, of class VisualGraphUtilities.
     */
    @Test
    public void testStreamVertexWorldLocations() {
        System.out.println("streamVertexWorldLocations");
        
        final int xVertexAttribute = VisualConcept.VertexAttribute.X.ensure(graph);
        final int yVertexAttribute = VisualConcept.VertexAttribute.Y.ensure(graph);
        final int zVertexAttribute = VisualConcept.VertexAttribute.Z.ensure(graph);
        final int x2VertexAttribute = VisualConcept.VertexAttribute.X2.ensure(graph);
        final int y2VertexAttribute = VisualConcept.VertexAttribute.Y2.ensure(graph);
        final int z2VertexAttribute = VisualConcept.VertexAttribute.Z2.ensure(graph);
        
        graph.setFloatValue(xVertexAttribute, vxId1, 1F);
        graph.setFloatValue(yVertexAttribute, vxId1, 2F);
        graph.setFloatValue(zVertexAttribute, vxId1, 3F);
        graph.setFloatValue(x2VertexAttribute, vxId1, 4F);
        graph.setFloatValue(y2VertexAttribute, vxId1, 5F);
        graph.setFloatValue(z2VertexAttribute, vxId1, 6F);
        
        graph.setFloatValue(xVertexAttribute, vxId2, 0F);
        graph.setFloatValue(yVertexAttribute, vxId2, 0.5F);
        graph.setFloatValue(zVertexAttribute, vxId2, 1F);
        graph.setFloatValue(x2VertexAttribute, vxId2, 1.5F);
        graph.setFloatValue(y2VertexAttribute, vxId2, 2F);
        graph.setFloatValue(z2VertexAttribute, vxId2, 2.5F);
        
        graph.setFloatValue(xVertexAttribute, vxId3, 10F);
        graph.setFloatValue(yVertexAttribute, vxId3, 9F);
        graph.setFloatValue(zVertexAttribute, vxId3, 8F);
        graph.setFloatValue(x2VertexAttribute, vxId3, 7F);
        graph.setFloatValue(y2VertexAttribute, vxId3, 6F);
        graph.setFloatValue(z2VertexAttribute, vxId3, 5F);
        
        final Camera camera = new Camera();
        camera.setMixRatio(10);
        
        try (final Stream<Vector3f> result = VisualGraphUtilities.streamVertexWorldLocations(graph, camera)) {
            final List<Vector3f> coordinates = result.toList();
            
            final List<Vector3f> expCoordinates = Arrays.asList(new Vector3f(2.5F, 3.5F, 4.5F), 
                    new Vector3f(0.75F, 1.25F, 1.75F), new Vector3f(8.5F, 7.5F, 6.5F));
            
            assertEquals(coordinates.size(), expCoordinates.size());
            
            assertEquals(coordinates.get(0).getX(), expCoordinates.get(0).getX());
            assertEquals(coordinates.get(0).getY(), expCoordinates.get(0).getY());
            assertEquals(coordinates.get(0).getZ(), expCoordinates.get(0).getZ());
            
            assertEquals(coordinates.get(1).getX(), expCoordinates.get(1).getX());
            assertEquals(coordinates.get(1).getY(), expCoordinates.get(1).getY());
            assertEquals(coordinates.get(1).getZ(), expCoordinates.get(1).getZ());
            
            assertEquals(coordinates.get(2).getX(), expCoordinates.get(2).getX());
            assertEquals(coordinates.get(2).getY(), expCoordinates.get(2).getY());
            assertEquals(coordinates.get(2).getZ(), expCoordinates.get(2).getZ());
        }
    }

    /**
     * Test of streamVertexSceneLocations method, of class VisualGraphUtilities.
     */
    @Test
    public void testStreamVertexSceneLocations() {
        System.out.println("streamVertexSceneLocations");
        
        final int xVertexAttribute = VisualConcept.VertexAttribute.X.ensure(graph);
        final int yVertexAttribute = VisualConcept.VertexAttribute.Y.ensure(graph);
        final int zVertexAttribute = VisualConcept.VertexAttribute.Z.ensure(graph);
        final int x2VertexAttribute = VisualConcept.VertexAttribute.X2.ensure(graph);
        final int y2VertexAttribute = VisualConcept.VertexAttribute.Y2.ensure(graph);
        final int z2VertexAttribute = VisualConcept.VertexAttribute.Z2.ensure(graph);
        
        graph.setFloatValue(xVertexAttribute, vxId1, 1F);
        graph.setFloatValue(yVertexAttribute, vxId1, 2F);
        graph.setFloatValue(zVertexAttribute, vxId1, 3F);
        graph.setFloatValue(x2VertexAttribute, vxId1, 4F);
        graph.setFloatValue(y2VertexAttribute, vxId1, 5F);
        graph.setFloatValue(z2VertexAttribute, vxId1, 6F);
        
        graph.setFloatValue(xVertexAttribute, vxId2, 0F);
        graph.setFloatValue(yVertexAttribute, vxId2, 0.5F);
        graph.setFloatValue(zVertexAttribute, vxId2, 1F);
        graph.setFloatValue(x2VertexAttribute, vxId2, 1.5F);
        graph.setFloatValue(y2VertexAttribute, vxId2, 2F);
        graph.setFloatValue(z2VertexAttribute, vxId2, 2.5F);
        
        graph.setFloatValue(xVertexAttribute, vxId3, 10F);
        graph.setFloatValue(yVertexAttribute, vxId3, 9F);
        graph.setFloatValue(zVertexAttribute, vxId3, 8F);
        graph.setFloatValue(x2VertexAttribute, vxId3, 7F);
        graph.setFloatValue(y2VertexAttribute, vxId3, 6F);
        graph.setFloatValue(z2VertexAttribute, vxId3, 5F);
        
        final Camera camera = new Camera();
        camera.setMixRatio(10);
        
        try (final Stream<Vector3f> result = VisualGraphUtilities.streamVertexSceneLocations(graph, camera)) {
            final List<Vector3f> coordinates = result.toList();
            
            final List<Vector3f> expCoordinates = Arrays.asList(new Vector3f(2.5F, 3.5F, -5.5F), 
                    new Vector3f(0.75F, 1.25F, -8.25F), new Vector3f(8.5F, 7.5F, -3.5F));
            
            assertEquals(coordinates.size(), expCoordinates.size());
            
            assertEquals(coordinates.get(0).getX(), expCoordinates.get(0).getX());
            assertEquals(coordinates.get(0).getY(), expCoordinates.get(0).getY());
            assertEquals(coordinates.get(0).getZ(), expCoordinates.get(0).getZ());
            
            assertEquals(coordinates.get(1).getX(), expCoordinates.get(1).getX());
            assertEquals(coordinates.get(1).getY(), expCoordinates.get(1).getY());
            assertEquals(coordinates.get(1).getZ(), expCoordinates.get(1).getZ());
            
            assertEquals(coordinates.get(2).getX(), expCoordinates.get(2).getX());
            assertEquals(coordinates.get(2).getY(), expCoordinates.get(2).getY());
            assertEquals(coordinates.get(2).getZ(), expCoordinates.get(2).getZ());
        }
    }

    /**
     * Test of getSelectedVertices method, of class VisualGraphUtilities.
     */
    @Test
    public void testGetSelectedVertices() {
        System.out.println("getSelectedVertices");
        
        final List<Integer> expResult = Arrays.asList(vxId1, vxId3);
        final List<Integer> result = VisualGraphUtilities.getSelectedVertices(graph);
        assertEquals(result, expResult);
    }   
}
