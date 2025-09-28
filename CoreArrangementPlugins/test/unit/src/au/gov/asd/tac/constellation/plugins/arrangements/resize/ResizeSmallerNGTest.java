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
package au.gov.asd.tac.constellation.plugins.arrangements.resize;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.arrangements.ArrangementPluginRegistry;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Resize Smaller Test.
 *
 * @author altair
 */
public class ResizeSmallerNGTest {

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
        graph.setFloatValue(attrX, vxId1, 10f);
        graph.setFloatValue(attrY, vxId1, 20f);
        graph.setFloatValue(attrZ, vxId1, 30f);
        graph.setBooleanValue(attrSelected, vxId1, false);
        
        vxId2 = graph.addVertex();
        graph.setFloatValue(attrX, vxId2, -10f);
        graph.setFloatValue(attrY, vxId2, -20f);
        graph.setFloatValue(attrZ, vxId2, -30f);
        graph.setBooleanValue(attrSelected, vxId2, false);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }
    
    @Test
    public void makeSmallerTest() throws InterruptedException, PluginException {
        assertEquals(String.format("Vertex [%d] x value", vxId1), 10f, graph.getFloatValue(attrX, vxId1));
        assertEquals(String.format("Vertex [%d] y value", vxId1), 20f, graph.getFloatValue(attrY, vxId1));
        assertEquals(String.format("Vertex [%d] z value", vxId1), 30f, graph.getFloatValue(attrZ, vxId1));
        assertEquals(String.format("Vertex [%d] x value", vxId1), -10f, graph.getFloatValue(attrX, vxId2));
        assertEquals(String.format("Vertex [%d] y value", vxId1), -20f, graph.getFloatValue(attrY, vxId2));
        assertEquals(String.format("Vertex [%d] z value", vxId1), -30f, graph.getFloatValue(attrZ, vxId2));

        PluginExecution.withPlugin(ArrangementPluginRegistry.CONTRACT_GRAPH).executeNow(graph);

        assertEquals(String.format("Vertex [%d] x value", vxId1), 9.090909f, graph.getFloatValue(attrX, vxId1));
        assertEquals(String.format("Vertex [%d] y value", vxId1), 18.181818f, graph.getFloatValue(attrY, vxId1));
        assertEquals(String.format("Vertex [%d] z value", vxId1), 27.272728f, graph.getFloatValue(attrZ, vxId1));
        assertEquals(String.format("Vertex [%d] x value", vxId1), -9.090909f, graph.getFloatValue(attrX, vxId2));
        assertEquals(String.format("Vertex [%d] y value", vxId1), -18.181818f, graph.getFloatValue(attrY, vxId2));
        assertEquals(String.format("Vertex [%d] z value", vxId1), -27.272728f, graph.getFloatValue(attrZ, vxId2));
    }

    @Test
    public void makeSmallerSelectedTest() throws InterruptedException, PluginException {
        graph.setBooleanValue(attrSelected, vxId2, true);

        assertEquals(String.format("Vertex [%d] x value", vxId1), 10f, graph.getFloatValue(attrX, vxId1));
        assertEquals(String.format("Vertex [%d] y value", vxId1), 20f, graph.getFloatValue(attrY, vxId1));
        assertEquals(String.format("Vertex [%d] z value", vxId1), 30f, graph.getFloatValue(attrZ, vxId1));
        assertEquals(String.format("Vertex [%d] x value", vxId1), -10f, graph.getFloatValue(attrX, vxId2));
        assertEquals(String.format("Vertex [%d] y value", vxId1), -20f, graph.getFloatValue(attrY, vxId2));
        assertEquals(String.format("Vertex [%d] z value", vxId1), -30f, graph.getFloatValue(attrZ, vxId2));

        PluginExecution.withPlugin(ArrangementPluginRegistry.CONTRACT_GRAPH).executeNow(graph);

        assertEquals(String.format("Vertex [%d] x value", vxId1), 10f, graph.getFloatValue(attrX, vxId1));
        assertEquals(String.format("Vertex [%d] y value", vxId1), 20f, graph.getFloatValue(attrY, vxId1));
        assertEquals(String.format("Vertex [%d] z value", vxId1), 30f, graph.getFloatValue(attrZ, vxId1));
        assertEquals(String.format("Vertex [%d] x value", vxId1), -9.090909f, graph.getFloatValue(attrX, vxId2));
        assertEquals(String.format("Vertex [%d] y value", vxId1), -18.181818f, graph.getFloatValue(attrY, vxId2));
        assertEquals(String.format("Vertex [%d] z value", vxId1), -27.272728f, graph.getFloatValue(attrZ, vxId2));
    }
}
