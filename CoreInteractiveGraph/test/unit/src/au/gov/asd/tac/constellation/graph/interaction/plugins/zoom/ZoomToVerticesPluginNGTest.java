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
import static au.gov.asd.tac.constellation.graph.interaction.plugins.zoom.ZoomToVerticesPlugin.VERTEX_PARAMETER_ID;
import static au.gov.asd.tac.constellation.graph.interaction.plugins.zoom.ZoomToVerticesPlugin.VERTICES_PARAMETER_ID;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import java.util.Arrays;
import static org.testng.Assert.assertEquals;
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
public class ZoomToVerticesPluginNGTest {
    
    private StoreGraph graph;
    
    private int cameraAttribute;
    private int xAttribute;
    private int yAttribute;
    private int zAttribute;
    
    private int vxId1;
    private int vxId2;
    private int vxId3;
    
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
        
        vxId1 = graph.addVertex();
        vxId2 = graph.addVertex();
        vxId3 = graph.addVertex();
        
        graph.setFloatValue(xAttribute, vxId1, 0F);
        graph.setFloatValue(yAttribute, vxId1, 1F);
        graph.setFloatValue(zAttribute, vxId1, 2F);
        
        graph.setFloatValue(xAttribute, vxId2, -5F);
        graph.setFloatValue(yAttribute, vxId2, -6F);
        graph.setFloatValue(zAttribute, vxId2, -7F);
        
        graph.setFloatValue(xAttribute, vxId3, 10F);
        graph.setFloatValue(yAttribute, vxId3, 11F);
        graph.setFloatValue(zAttribute, vxId3, 12F);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of createParameters method, of class ZoomToVerticesPlugin.
     */
    @Test
    public void testCreateParameters() {
        System.out.println("createParameters");
        
        final ZoomToVerticesPlugin instance = new ZoomToVerticesPlugin();
        
        final PluginParameters params = instance.createParameters();
        assertEquals(params.getParameters().size(), 2);
        assertTrue(params.getParameters().containsKey(VERTICES_PARAMETER_ID));
        assertTrue(params.getParameters().containsKey(VERTEX_PARAMETER_ID));
    }

    /**
     * Test of edit method, of class ZoomToVerticesPlugin. Only one node given
     * 
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testEditOneNode() throws InterruptedException, PluginException {
        System.out.println("editOneNode");
        
        final ZoomToVerticesPlugin instance = new ZoomToVerticesPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setIntegerValue(VERTEX_PARAMETER_ID, vxId3);
        
        final Camera beforeCamera = graph.getObjectValue(cameraAttribute, 0);
        assertEquals(beforeCamera, new Camera());
        
        instance.edit(graph, null, parameters);
        
        final Camera afterCamera = graph.getObjectValue(cameraAttribute, 0);
        assertNotEquals(afterCamera, new Camera());
        assertEquals(afterCamera.lookAtCentre, new Vector3f(10, 11, 12));
        assertEquals(afterCamera.lookAtEye, new Vector3f(10, 11, 28));
    }
    
    /**
     * Test of edit method, of class ZoomToVerticesPlugin. Array of nodes given
     * 
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testEditArrayOfNodes() throws InterruptedException, PluginException {
        System.out.println("editArrayOfNodes");
        
        final ZoomToVerticesPlugin instance = new ZoomToVerticesPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setObjectValue(VERTICES_PARAMETER_ID, new int[]{vxId1, vxId2});
        
        final Camera beforeCamera = graph.getObjectValue(cameraAttribute, 0);
        assertEquals(beforeCamera, new Camera());
        
        instance.edit(graph, null, parameters);
        
        final Camera afterCamera = graph.getObjectValue(cameraAttribute, 0);
        assertNotEquals(afterCamera, new Camera());
        assertEquals(afterCamera.lookAtCentre, new Vector3f(-2.5F, -2.5F, -2.5F));
        assertEquals(afterCamera.lookAtEye, new Vector3f(-2.5F, -2.5F, 17.24302F));
    }
    
    /**
     * Test of edit method, of class ZoomToVerticesPlugin. List of nodes given
     * 
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testEditListOfNodes() throws InterruptedException, PluginException {
        System.out.println("editArrayOfNodes");
        
        final ZoomToVerticesPlugin instance = new ZoomToVerticesPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setObjectValue(VERTICES_PARAMETER_ID, Arrays.asList(vxId1, vxId2));
        
        final Camera beforeCamera = graph.getObjectValue(cameraAttribute, 0);
        assertEquals(beforeCamera, new Camera());
        
        instance.edit(graph, null, parameters);
        
        // this should be the same value as it was with the array
        final Camera afterCamera = graph.getObjectValue(cameraAttribute, 0);
        assertNotEquals(afterCamera, new Camera());
        assertEquals(afterCamera.lookAtCentre, new Vector3f(-2.5F, -2.5F, -2.5F));
        assertEquals(afterCamera.lookAtEye, new Vector3f(-2.5F, -2.5F, 17.24302F));
    }
}
