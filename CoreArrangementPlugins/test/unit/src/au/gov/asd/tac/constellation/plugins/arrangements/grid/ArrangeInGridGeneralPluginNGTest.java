/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.arrangements.grid;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import static au.gov.asd.tac.constellation.plugins.arrangements.grid.ArrangeInGridGeneralPlugin.MAINTAIN_MEAN_PARAMETER_ID;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class ArrangeInGridGeneralPluginNGTest {
    
    private StoreGraph graph;
    
    private int vxId1;
    private int vxId2;
    private int vxId3;
    private int vxId4;
    private int vxId5;
    
    private int xNodeAttribute;
    private int yNodeAttribute;
    private int zNodeAttribute;
    
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
        graph = new StoreGraph();
        
        vxId1 = graph.addVertex();
        vxId2 = graph.addVertex();
        vxId3 = graph.addVertex();
        vxId4 = graph.addVertex();
        vxId5 = graph.addVertex();
        
        xNodeAttribute = VisualConcept.VertexAttribute.X.ensure(graph);
        yNodeAttribute = VisualConcept.VertexAttribute.Y.ensure(graph);
        zNodeAttribute = VisualConcept.VertexAttribute.Z.ensure(graph);
        
        graph.setFloatValue(xNodeAttribute, vxId1, 4F);
        graph.setFloatValue(yNodeAttribute, vxId1, 5F);
        graph.setFloatValue(zNodeAttribute, vxId1, 6F);
        
        graph.setFloatValue(xNodeAttribute, vxId2, -1F);
        graph.setFloatValue(yNodeAttribute, vxId2, -2F);
        graph.setFloatValue(zNodeAttribute, vxId2, -3F);
        
        graph.setFloatValue(xNodeAttribute, vxId3, 1F);
        graph.setFloatValue(yNodeAttribute, vxId3, 2F);
        graph.setFloatValue(zNodeAttribute, vxId3, 3F);
        
        graph.setFloatValue(xNodeAttribute, vxId4, -3F);
        graph.setFloatValue(yNodeAttribute, vxId4, -2F);
        graph.setFloatValue(zNodeAttribute, vxId4, -1F);
        
        graph.setFloatValue(xNodeAttribute, vxId5, 2F);
        graph.setFloatValue(yNodeAttribute, vxId5, 2F);
        graph.setFloatValue(zNodeAttribute, vxId5, 2F);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of edit method, of class ArrangeInGridGeneralPlugin.
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testEdit() throws InterruptedException {
        System.out.println("edit");
        
        assertEquals(graph.getFloatValue(xNodeAttribute, vxId1), 4F);
        assertEquals(graph.getFloatValue(yNodeAttribute, vxId1), 5F);
        assertEquals(graph.getFloatValue(zNodeAttribute, vxId1), 6F);
        
        assertEquals(graph.getFloatValue(xNodeAttribute, vxId2), -1F);
        assertEquals(graph.getFloatValue(yNodeAttribute, vxId2), -2F);
        assertEquals(graph.getFloatValue(zNodeAttribute, vxId2), -3F);
        
        assertEquals(graph.getFloatValue(xNodeAttribute, vxId3), 1F);
        assertEquals(graph.getFloatValue(yNodeAttribute, vxId3), 2F);
        assertEquals(graph.getFloatValue(zNodeAttribute, vxId3), 3F);
        
        assertEquals(graph.getFloatValue(xNodeAttribute, vxId4), -3F);
        assertEquals(graph.getFloatValue(yNodeAttribute, vxId4), -2F);
        assertEquals(graph.getFloatValue(zNodeAttribute, vxId4), -1F);
        
        assertEquals(graph.getFloatValue(xNodeAttribute, vxId5), 2F);
        assertEquals(graph.getFloatValue(yNodeAttribute, vxId5), 2F);
        assertEquals(graph.getFloatValue(zNodeAttribute, vxId5), 2F);
        
        final ArrangeInGridGeneralPlugin instance = new ArrangeInGridGeneralPlugin();
        final PluginParameters params = instance.createParameters();
        instance.edit(graph, null, params);
        
        // should end up with a square arranged in vxId order
        // 4 5
        // 1 2 3
        assertEquals(graph.getFloatValue(xNodeAttribute, vxId1), -3.1999998F);
        assertEquals(graph.getFloatValue(yNodeAttribute, vxId1), -0.9F);
        assertEquals(graph.getFloatValue(zNodeAttribute, vxId1), 1.4F);
        
        assertEquals(graph.getFloatValue(xNodeAttribute, vxId2), 1.55F);
        assertEquals(graph.getFloatValue(yNodeAttribute, vxId2), -0.9F);
        assertEquals(graph.getFloatValue(zNodeAttribute, vxId2), 1.4F);
        
        assertEquals(graph.getFloatValue(xNodeAttribute, vxId3), 6.2999997F);
        assertEquals(graph.getFloatValue(yNodeAttribute, vxId3), -0.9F);
        assertEquals(graph.getFloatValue(zNodeAttribute, vxId3), 1.4F);
        
        assertEquals(graph.getFloatValue(xNodeAttribute, vxId4), -3.1999998F);
        assertEquals(graph.getFloatValue(yNodeAttribute, vxId4), 3.85F);
        assertEquals(graph.getFloatValue(zNodeAttribute, vxId4), 1.4F);
        
        assertEquals(graph.getFloatValue(xNodeAttribute, vxId5), 1.55F);
        assertEquals(graph.getFloatValue(yNodeAttribute, vxId5), 3.85F);
        assertEquals(graph.getFloatValue(zNodeAttribute, vxId5), 1.4F);
    }
    
    /**
     * Test of edit method, of class ArrangeInGridGeneralPlugin. Don't maintain mean
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testEditNoMaintainMean() throws InterruptedException {
        System.out.println("editNoMaintainMean");
        
        assertEquals(graph.getFloatValue(xNodeAttribute, vxId1), 4F);
        assertEquals(graph.getFloatValue(yNodeAttribute, vxId1), 5F);
        assertEquals(graph.getFloatValue(zNodeAttribute, vxId1), 6F);
        
        assertEquals(graph.getFloatValue(xNodeAttribute, vxId2), -1F);
        assertEquals(graph.getFloatValue(yNodeAttribute, vxId2), -2F);
        assertEquals(graph.getFloatValue(zNodeAttribute, vxId2), -3F);
        
        assertEquals(graph.getFloatValue(xNodeAttribute, vxId3), 1F);
        assertEquals(graph.getFloatValue(yNodeAttribute, vxId3), 2F);
        assertEquals(graph.getFloatValue(zNodeAttribute, vxId3), 3F);
        
        assertEquals(graph.getFloatValue(xNodeAttribute, vxId4), -3F);
        assertEquals(graph.getFloatValue(yNodeAttribute, vxId4), -2F);
        assertEquals(graph.getFloatValue(zNodeAttribute, vxId4), -1F);
        
        assertEquals(graph.getFloatValue(xNodeAttribute, vxId5), 2F);
        assertEquals(graph.getFloatValue(yNodeAttribute, vxId5), 2F);
        assertEquals(graph.getFloatValue(zNodeAttribute, vxId5), 2F);
        
        final ArrangeInGridGeneralPlugin instance = new ArrangeInGridGeneralPlugin();
        final PluginParameters params = instance.createParameters();
        params.getParameters().get(MAINTAIN_MEAN_PARAMETER_ID).setBooleanValue(false);
        instance.edit(graph, null, params);
        
        // should end up with a square arranged in vxId order
        // 4 5
        // 1 2 3
        assertEquals(graph.getFloatValue(xNodeAttribute, vxId1), -4.75F);
        assertEquals(graph.getFloatValue(yNodeAttribute, vxId1), -2.375F);
        assertEquals(graph.getFloatValue(zNodeAttribute, vxId1), 0F);
        
        assertEquals(graph.getFloatValue(xNodeAttribute, vxId2), 0F);
        assertEquals(graph.getFloatValue(yNodeAttribute, vxId2), -2.375F);
        assertEquals(graph.getFloatValue(zNodeAttribute, vxId2), 0F);
        
        assertEquals(graph.getFloatValue(xNodeAttribute, vxId3), 4.75F);
        assertEquals(graph.getFloatValue(yNodeAttribute, vxId3), -2.375F);
        assertEquals(graph.getFloatValue(zNodeAttribute, vxId3), 0F);
        
        assertEquals(graph.getFloatValue(xNodeAttribute, vxId4), -4.75F);
        assertEquals(graph.getFloatValue(yNodeAttribute, vxId4), 2.375F);
        assertEquals(graph.getFloatValue(zNodeAttribute, vxId4), 0F);
        
        assertEquals(graph.getFloatValue(xNodeAttribute, vxId5), 0F);
        assertEquals(graph.getFloatValue(yNodeAttribute, vxId5), 2.375F);
        assertEquals(graph.getFloatValue(zNodeAttribute, vxId5), 0F);
    }
}
