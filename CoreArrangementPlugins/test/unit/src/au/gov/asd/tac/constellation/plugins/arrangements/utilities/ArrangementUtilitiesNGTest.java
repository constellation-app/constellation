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
import java.util.BitSet;
import java.util.Deque;
import static org.testng.Assert.assertEquals;
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
public class ArrangementUtilitiesNGTest {
    
    private StoreGraph graph;
    
    private int vxId1;
    private int vxId2;
    private int vxId3;
    private int vxId4;
    
    private int xNodeAttribute;
    private int yNodeAttribute;
    private int zNodeAttribute;
    private int x2NodeAttribute;
    private int y2NodeAttribute;
    private int z2NodeAttribute;

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
        
        graph.addTransaction(vxId1, vxId2, true);
        graph.addTransaction(vxId1, vxId3, true);
        graph.addTransaction(vxId2, vxId3, true);
        
        xNodeAttribute = VisualConcept.VertexAttribute.X.ensure(graph);
        yNodeAttribute = VisualConcept.VertexAttribute.Y.ensure(graph);
        zNodeAttribute = VisualConcept.VertexAttribute.Z.ensure(graph);
        x2NodeAttribute = VisualConcept.VertexAttribute.X2.ensure(graph);
        y2NodeAttribute = VisualConcept.VertexAttribute.Y2.ensure(graph);
        z2NodeAttribute = VisualConcept.VertexAttribute.Z2.ensure(graph);
        
        graph.setFloatValue(xNodeAttribute, vxId1, 0F);
        graph.setFloatValue(yNodeAttribute, vxId1, 0F);
        graph.setFloatValue(zNodeAttribute, vxId1, 0F);
        
        graph.setFloatValue(xNodeAttribute, vxId2, 1F);
        graph.setFloatValue(yNodeAttribute, vxId2, 1F);
        graph.setFloatValue(zNodeAttribute, vxId2, 1F);
        
        graph.setFloatValue(xNodeAttribute, vxId3, 2F);
        graph.setFloatValue(yNodeAttribute, vxId3, 2F);
        graph.setFloatValue(zNodeAttribute, vxId3, 2F);
        
        graph.setFloatValue(xNodeAttribute, vxId4, 3F);
        graph.setFloatValue(yNodeAttribute, vxId4, 3F);
        graph.setFloatValue(zNodeAttribute, vxId4, 3F);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of getXyzMean method, of class ArrangementUtilities.
     */
    @Test
    public void testGetXyzMean() {
        System.out.println("getXyzMean");
        
        final float[] result = ArrangementUtilities.getXyzMean(graph);
        assertEquals(result, new float[]{1.5F, 1.5F, 1.5F});
    }

    /**
     * Test of moveMean method, of class ArrangementUtilities.
     */
    @Test
    public void testMoveMean() {
        System.out.println("moveMean");
        
        assertEquals(graph.getFloatValue(xNodeAttribute, vxId1), 0F);
        assertEquals(graph.getFloatValue(yNodeAttribute, vxId1), 0F);
        assertEquals(graph.getFloatValue(zNodeAttribute, vxId1), 0F);
        assertEquals(graph.getFloatValue(x2NodeAttribute, vxId1), 0F);
        assertEquals(graph.getFloatValue(y2NodeAttribute, vxId1), 0F);
        assertEquals(graph.getFloatValue(z2NodeAttribute, vxId1), 0F);
        
        assertEquals(graph.getFloatValue(xNodeAttribute, vxId2), 1F);
        assertEquals(graph.getFloatValue(yNodeAttribute, vxId2), 1F);
        assertEquals(graph.getFloatValue(zNodeAttribute, vxId2), 1F);
        assertEquals(graph.getFloatValue(x2NodeAttribute, vxId2), 0F);
        assertEquals(graph.getFloatValue(y2NodeAttribute, vxId2), 0F);
        assertEquals(graph.getFloatValue(z2NodeAttribute, vxId2), 0F);
        
        assertEquals(graph.getFloatValue(xNodeAttribute, vxId3), 2F);
        assertEquals(graph.getFloatValue(yNodeAttribute, vxId3), 2F);
        assertEquals(graph.getFloatValue(zNodeAttribute, vxId3), 2F);
        assertEquals(graph.getFloatValue(x2NodeAttribute, vxId3), 0F);
        assertEquals(graph.getFloatValue(y2NodeAttribute, vxId3), 0F);
        assertEquals(graph.getFloatValue(z2NodeAttribute, vxId3), 0F);
        
        assertEquals(graph.getFloatValue(xNodeAttribute, vxId4), 3F);
        assertEquals(graph.getFloatValue(yNodeAttribute, vxId4), 3F);
        assertEquals(graph.getFloatValue(zNodeAttribute, vxId4), 3F);
        assertEquals(graph.getFloatValue(x2NodeAttribute, vxId4), 0F);
        assertEquals(graph.getFloatValue(y2NodeAttribute, vxId4), 0F);
        assertEquals(graph.getFloatValue(z2NodeAttribute, vxId4), 0F);
        
        final float[] oldMean = new float[]{1F, 1F, 1F};
        ArrangementUtilities.moveMean(graph, oldMean);
        
        assertEquals(graph.getFloatValue(xNodeAttribute, vxId1), -0.5F);
        assertEquals(graph.getFloatValue(yNodeAttribute, vxId1), -0.5F);
        assertEquals(graph.getFloatValue(zNodeAttribute, vxId1), -0.5F);
        assertEquals(graph.getFloatValue(x2NodeAttribute, vxId1), -0.5F);
        assertEquals(graph.getFloatValue(y2NodeAttribute, vxId1), -0.5F);
        assertEquals(graph.getFloatValue(z2NodeAttribute, vxId1), -0.5F);
        
        assertEquals(graph.getFloatValue(xNodeAttribute, vxId2), 0.5F);
        assertEquals(graph.getFloatValue(yNodeAttribute, vxId2), 0.5F);
        assertEquals(graph.getFloatValue(zNodeAttribute, vxId2), 0.5F);
        assertEquals(graph.getFloatValue(x2NodeAttribute, vxId2), -0.5F);
        assertEquals(graph.getFloatValue(y2NodeAttribute, vxId2), -0.5F);
        assertEquals(graph.getFloatValue(z2NodeAttribute, vxId2), -0.5F);
        
        assertEquals(graph.getFloatValue(xNodeAttribute, vxId3), 1.5F);
        assertEquals(graph.getFloatValue(yNodeAttribute, vxId3), 1.5F);
        assertEquals(graph.getFloatValue(zNodeAttribute, vxId3), 1.5F);
        assertEquals(graph.getFloatValue(x2NodeAttribute, vxId3), -0.5F);
        assertEquals(graph.getFloatValue(y2NodeAttribute, vxId3), -0.5F);
        assertEquals(graph.getFloatValue(z2NodeAttribute, vxId3), -0.5F);
        
        assertEquals(graph.getFloatValue(xNodeAttribute, vxId4), 2.5F);
        assertEquals(graph.getFloatValue(yNodeAttribute, vxId4), 2.5F);
        assertEquals(graph.getFloatValue(zNodeAttribute, vxId4), 2.5F);
        assertEquals(graph.getFloatValue(x2NodeAttribute, vxId4), -0.5F);
        assertEquals(graph.getFloatValue(y2NodeAttribute, vxId4), -0.5F);
        assertEquals(graph.getFloatValue(z2NodeAttribute, vxId4), -0.5F);
    }

    /**
     * Test of vertexBits method, of class ArrangementUtilities.
     */
    @Test
    public void testVertexBits() {
        System.out.println("vertexBits");
        
        final BitSet result = ArrangementUtilities.vertexBits(graph);
        assertEquals(result.cardinality(), 4);
        // the vxIds should be between 0 and 3 so based on that, those should all be set, and others not
        assertTrue(result.get(0));
        assertTrue(result.get(1));
        assertTrue(result.get(2));
        assertTrue(result.get(3));
        assertFalse(result.get(4));
    }

    /**
     * Test of getSources method, of class ArrangementUtilities.
     */
    @Test
    public void testGetSources() {
        System.out.println("getSources");
        
        final Deque<Integer> result = ArrangementUtilities.getSources(graph);
        assertEquals(result.size(), 2);
        assertTrue(result.contains(vxId1));
        assertFalse(result.contains(vxId2));
        assertFalse(result.contains(vxId3));
        // a sink by default given it doesn't have any connecting transactions
        assertTrue(result.contains(vxId4));
    }

    /**
     * Test of setXYZ2FromXYZ method, of class ArrangementUtilities.
     */
    @Test
    public void testSetXYZ2FromXYZ() {
        System.out.println("setXYZ2FromXYZ");
        
        assertEquals(graph.getFloatValue(x2NodeAttribute, vxId1), 0F);
        assertEquals(graph.getFloatValue(y2NodeAttribute, vxId1), 0F);
        assertEquals(graph.getFloatValue(z2NodeAttribute, vxId1), 0F);
        
        assertEquals(graph.getFloatValue(x2NodeAttribute, vxId2), 0F);
        assertEquals(graph.getFloatValue(y2NodeAttribute, vxId2), 0F);
        assertEquals(graph.getFloatValue(z2NodeAttribute, vxId2), 0F);
        
        assertEquals(graph.getFloatValue(x2NodeAttribute, vxId3), 0F);
        assertEquals(graph.getFloatValue(y2NodeAttribute, vxId3), 0F);
        assertEquals(graph.getFloatValue(z2NodeAttribute, vxId3), 0F);
        
        assertEquals(graph.getFloatValue(x2NodeAttribute, vxId4), 0F);
        assertEquals(graph.getFloatValue(y2NodeAttribute, vxId4), 0F);
        assertEquals(graph.getFloatValue(z2NodeAttribute, vxId4), 0F);
        
        ArrangementUtilities.setXYZ2FromXYZ(graph);
        
        assertEquals(graph.getFloatValue(x2NodeAttribute, vxId1), 0F);
        assertEquals(graph.getFloatValue(y2NodeAttribute, vxId1), 0F);
        assertEquals(graph.getFloatValue(z2NodeAttribute, vxId1), 0F);
        
        assertEquals(graph.getFloatValue(x2NodeAttribute, vxId2), 1F);
        assertEquals(graph.getFloatValue(y2NodeAttribute, vxId2), 1F);
        assertEquals(graph.getFloatValue(z2NodeAttribute, vxId2), 1F);
        
        assertEquals(graph.getFloatValue(x2NodeAttribute, vxId3), 2F);
        assertEquals(graph.getFloatValue(y2NodeAttribute, vxId3), 2F);
        assertEquals(graph.getFloatValue(z2NodeAttribute, vxId3), 2F);
        
        assertEquals(graph.getFloatValue(x2NodeAttribute, vxId4), 3F);
        assertEquals(graph.getFloatValue(y2NodeAttribute, vxId4), 3F);
        assertEquals(graph.getFloatValue(z2NodeAttribute, vxId4), 3F);
    }
}
