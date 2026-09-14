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
package au.gov.asd.tac.constellation.plugins.arrangements;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import java.util.BitSet;
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
public class ExtentNGTest {
    
    private StoreGraph graph;
    
    private int vxId1;
    private int vxId2;
    private int vxId3;
    
    private int xNodeAttribute;
    private int yNodeAttribute;
    private int zNodeAttribute;
    private int nRadiusNodeAttribute;
    private int lRadiusNodeAttribute;
    
    private BitSet vertices;

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
        
        xNodeAttribute = VisualConcept.VertexAttribute.X.ensure(graph);
        yNodeAttribute = VisualConcept.VertexAttribute.Y.ensure(graph);
        zNodeAttribute = VisualConcept.VertexAttribute.Z.ensure(graph);
        nRadiusNodeAttribute = VisualConcept.VertexAttribute.NODE_RADIUS.ensure(graph);
        lRadiusNodeAttribute = VisualConcept.VertexAttribute.LABEL_RADIUS.ensure(graph);
        
        graph.setFloatValue(xNodeAttribute, vxId1, 2F);
        graph.setFloatValue(yNodeAttribute, vxId1, 1F);
        graph.setFloatValue(zNodeAttribute, vxId1, 0F);
        graph.setFloatValue(nRadiusNodeAttribute, vxId1, 1F);
        graph.setFloatValue(lRadiusNodeAttribute, vxId1, 1F);
        
        graph.setFloatValue(xNodeAttribute, vxId2, 1F);
        graph.setFloatValue(yNodeAttribute, vxId2, 0F);
        graph.setFloatValue(zNodeAttribute, vxId2, 2F);
        graph.setFloatValue(nRadiusNodeAttribute, vxId2, 2F);
        graph.setFloatValue(lRadiusNodeAttribute, vxId2, 3F);
        
        graph.setFloatValue(xNodeAttribute, vxId3, 0F);
        graph.setFloatValue(yNodeAttribute, vxId3, 2F);
        graph.setFloatValue(zNodeAttribute, vxId3, 1F);
        graph.setFloatValue(nRadiusNodeAttribute, vxId3, 3F);
        graph.setFloatValue(lRadiusNodeAttribute, vxId3, 1F);
        
        vertices = new BitSet();
        vertices.set(vxId1);
        vertices.set(vxId2);
        vertices.set(vxId3);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of getExtent method, of class Extent.
     */
    @Test
    public void testGetExtent() {
        System.out.println("getExtent");
        
        final Extent result = Extent.getExtent(graph, vertices);
        assertEquals(result.getX(), 0F);
        assertEquals(result.getY(), 1.5F);
        assertEquals(result.getZ(), 1F);
        assertEquals(result.getNRadius(), 3.5F);
        assertEquals(result.getLRadius(), 3F);
    }
}
