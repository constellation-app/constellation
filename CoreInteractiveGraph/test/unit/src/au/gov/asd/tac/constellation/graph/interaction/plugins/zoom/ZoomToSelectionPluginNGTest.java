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
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import static org.testng.Assert.assertFalse;
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
public class ZoomToSelectionPluginNGTest {
    
    private StoreGraph graph;
    
    private int cameraAttribute;
    private int xAttribute;
    private int yAttribute;
    private int zAttribute;
    private int selectedAttribute;
    
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
        selectedAttribute = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        
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
        
        graph.setBooleanValue(selectedAttribute, vxId1, true);
        graph.setBooleanValue(selectedAttribute, vxId2, true);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of edit method, of class ZoomToSelectionPlugin.
     * 
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testEdit() throws InterruptedException, PluginException {
        System.out.println("edit");
        
        final Camera beforeCamera = graph.getObjectValue(cameraAttribute, 0);
        assertTrue(beforeCamera.areSame(new Camera()));
        
        final ZoomToSelectionPlugin instance = new ZoomToSelectionPlugin();
        instance.edit(graph, null, null);
        
        final Camera afterCamera = graph.getObjectValue(cameraAttribute, 0);
        assertFalse(afterCamera.areSame(new Camera()));
        assertTrue(afterCamera.lookAtCentre.areSame(new Vector3f(-2.5F, -2.5F, -2.5F)));
        assertTrue(afterCamera.lookAtEye.areSame(new Vector3f(-2.5F, -2.5F, 17.24302F)));
    }
}
