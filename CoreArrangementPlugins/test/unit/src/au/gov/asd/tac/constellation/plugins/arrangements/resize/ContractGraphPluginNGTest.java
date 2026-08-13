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
package au.gov.asd.tac.constellation.plugins.arrangements.resize;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.text.TextPluginInteraction;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author altair
 * @author antares
 */
public class ContractGraphPluginNGTest {
    
    private int attrX;
    private int attrY;
    private int attrZ;
    private int attrSelected;
    
    private int vxId1;
    private int vxId2;
    
    private StoreGraph graph;
    
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

        attrX = VisualConcept.VertexAttribute.X.ensure(graph);
        attrY = VisualConcept.VertexAttribute.Y.ensure(graph);
        attrZ = VisualConcept.VertexAttribute.Z.ensure(graph);
        attrSelected = VisualConcept.VertexAttribute.SELECTED.ensure(graph);

        vxId1 = graph.addVertex();
        graph.setFloatValue(attrX, vxId1, 10F);
        graph.setFloatValue(attrY, vxId1, 20F);
        graph.setFloatValue(attrZ, vxId1, 30F);
        graph.setBooleanValue(attrSelected, vxId1, false);
        
        vxId2 = graph.addVertex();
        graph.setFloatValue(attrX, vxId2, -10F);
        graph.setFloatValue(attrY, vxId2, -20F);
        graph.setFloatValue(attrZ, vxId2, -30F);
        graph.setBooleanValue(attrSelected, vxId2, false);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of edit method, of class ContractGraphPlugin.
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testEdit() throws InterruptedException {
        System.out.println("edit");
        
        assertEquals(graph.getFloatValue(attrX, vxId1), 10F);
        assertEquals(graph.getFloatValue(attrY, vxId1), 20F);
        assertEquals(graph.getFloatValue(attrZ, vxId1), 30F);
        assertEquals(graph.getFloatValue(attrX, vxId2), -10F);
        assertEquals(graph.getFloatValue(attrY, vxId2), -20F);
        assertEquals(graph.getFloatValue(attrZ, vxId2), -30F);
        
        final ContractGraphPlugin instance = new ContractGraphPlugin();
        final PluginInteraction interaction = new TextPluginInteraction();
        instance.edit(graph, interaction, null);

        assertEquals(graph.getFloatValue(attrX, vxId1), 9.090909F);
        assertEquals(graph.getFloatValue(attrY, vxId1), 18.181818F);
        assertEquals(graph.getFloatValue(attrZ, vxId1), 27.272728F);
        assertEquals(graph.getFloatValue(attrX, vxId2), -9.090909F);
        assertEquals(graph.getFloatValue(attrY, vxId2), -18.181818F);
        assertEquals(graph.getFloatValue(attrZ, vxId2), -27.272728F);
    }
    
    /**
     * Test of edit method, of class ContractGraphPlugin. A node selected.
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testEditSelect() throws InterruptedException {
        System.out.println("editSelect");
        
        graph.setBooleanValue(attrSelected, vxId2, true);
        
        assertEquals(graph.getFloatValue(attrX, vxId1), 10F);
        assertEquals(graph.getFloatValue(attrY, vxId1), 20F);
        assertEquals(graph.getFloatValue(attrZ, vxId1), 30F);
        assertEquals(graph.getFloatValue(attrX, vxId2), -10F);
        assertEquals(graph.getFloatValue(attrY, vxId2), -20F);
        assertEquals(graph.getFloatValue(attrZ, vxId2), -30F);
        
        final ContractGraphPlugin instance = new ContractGraphPlugin();
        final PluginInteraction interaction = new TextPluginInteraction();
        instance.edit(graph, interaction, null);

        assertEquals(graph.getFloatValue(attrX, vxId1), 10F);
        assertEquals(graph.getFloatValue(attrY, vxId1), 20F);
        assertEquals(graph.getFloatValue(attrZ, vxId1), 30F);
        assertEquals(graph.getFloatValue(attrX, vxId2), -9.090909F);
        assertEquals(graph.getFloatValue(attrY, vxId2), -18.181818F);
        assertEquals(graph.getFloatValue(attrZ, vxId2), -27.272728F);
    }
}
