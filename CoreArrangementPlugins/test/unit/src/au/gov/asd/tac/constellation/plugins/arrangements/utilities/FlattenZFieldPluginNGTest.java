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
package au.gov.asd.tac.constellation.plugins.arrangements.utilities;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
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
public class FlattenZFieldPluginNGTest {
    
    private int attrX;
    private int attrY;
    private int attrZ;
    private int vAttrId;
    
    private int vxId1;
    private int vxId2;
    private int vxId3;
    private int vxId4;
    private int vxId5;
    
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
        vAttrId = VisualConcept.VertexAttribute.SELECTED.ensure(graph);

        vxId1 = graph.addVertex();
        graph.setFloatValue(attrX, vxId1, 1F);
        graph.setFloatValue(attrY, vxId1, 1F);
        graph.setFloatValue(attrZ, vxId1, 1F);
        graph.setBooleanValue(vAttrId, vxId1, false);
        
        vxId2 = graph.addVertex();
        graph.setFloatValue(attrX, vxId2, 5F);
        graph.setFloatValue(attrY, vxId2, 1F);
        graph.setFloatValue(attrZ, vxId2, 2F);
        graph.setBooleanValue(vAttrId, vxId2, false);
        
        vxId3 = graph.addVertex();
        graph.setFloatValue(attrX, vxId3, 1F);
        graph.setFloatValue(attrY, vxId3, 5F);
        graph.setFloatValue(attrZ, vxId3, 3F);
        graph.setBooleanValue(vAttrId, vxId3, false);
        
        vxId4 = graph.addVertex();
        graph.setFloatValue(attrX, vxId4, 5F);
        graph.setFloatValue(attrY, vxId4, 5F);
        graph.setFloatValue(attrZ, vxId4, 4F);
        graph.setBooleanValue(vAttrId, vxId4, false);
        
        vxId5 = graph.addVertex();
        graph.setFloatValue(attrX, vxId5, 10F);
        graph.setFloatValue(attrY, vxId5, 10F);
        graph.setFloatValue(attrZ, vxId5, 5F);
        graph.setBooleanValue(vAttrId, vxId5, false);

        graph.addTransaction(vxId1, vxId2, false);
        graph.addTransaction(vxId1, vxId3, false);
        graph.addTransaction(vxId2, vxId4, true);
        graph.addTransaction(vxId4, vxId2, true);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of edit method, of class FlattenZFieldPlugin.
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testEdit() throws InterruptedException {
        System.out.println("edit");
        
        for (int i = 0; i < graph.getVertexCount(); i++) {
            final int txId = graph.getVertex(i);
            float value = graph.getFloatValue(attrZ, txId);
            assertNotEquals(value, 0F);
        }
        
        final FlattenZFieldPlugin instance = new FlattenZFieldPlugin();
        instance.edit(graph, null, null);

        for (int i = 0; i < graph.getVertexCount(); i++) {
            final int txId = graph.getVertex(i);
            float value = graph.getFloatValue(attrZ, txId);
            assertEquals(value, 0F);
        }
    }
}
