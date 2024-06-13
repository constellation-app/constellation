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
package au.gov.asd.tac.constellation.graph.visual.plugins.blaze;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.attribute.BooleanAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.BlazeAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.utilities.BlazeUtilities;
import au.gov.asd.tac.constellation.graph.visual.VisualGraphPluginRegistry;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import java.util.BitSet;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Blaze Test.
 *
 * @author altair
 */
public class BlazeNGTest {

    private StoreGraph graph;
    private int vxId4;
    private int vxId5;

    public BlazeNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        int blazeAttr;
        int selectedAttr;

        graph = new StoreGraph();
        blazeAttr = graph.addAttribute(GraphElementType.VERTEX, BlazeAttributeDescription.ATTRIBUTE_NAME, "blaze", null, null, null);
        if (blazeAttr == Graph.NOT_FOUND) {
            fail();
        }

        selectedAttr = graph.addAttribute(GraphElementType.VERTEX, BooleanAttributeDescription.ATTRIBUTE_NAME, "selected", "selected", false, null);
        if (selectedAttr == Graph.NOT_FOUND) {
            fail();
        }
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    private void generateData() throws InterruptedException {
        graph.addVertex();
        graph.addVertex();
        graph.addVertex();
        vxId4 = graph.addVertex();
        vxId5 = graph.addVertex();
    }

    @Test
    public void setBlazeTest() throws InterruptedException, PluginException {
        generateData();

        int attrId = graph.getAttribute(GraphElementType.VERTEX, "blaze");
        assertTrue("Blaze column found", (attrId >= 0));

        for (int i = 0; i < graph.getVertexCount(); i++) {
            final int txId = graph.getVertex(i);
            Object blaze = graph.getObjectValue(attrId, txId);
            assertTrue(String.format("Vertex [%d] should not have a blaze", txId), (blaze == null));
        }

        attrId = graph.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.SELECTED.getName());
        assertTrue("selected column found", (attrId >= 0));

        graph.setBooleanValue(attrId, vxId4, true);
        graph.setBooleanValue(attrId, vxId5, true);

        final BitSet vertices = new BitSet();
        vertices.set(vxId4);
        vertices.set(vxId5);

        PluginExecution.withPlugin(VisualGraphPluginRegistry.ADD_BLAZE)
                .withParameter(BlazeUtilities.VERTEX_IDS_PARAMETER_ID, vertices)
                .executeNow(graph);

        attrId = graph.getAttribute(GraphElementType.VERTEX, "blaze");
        assertTrue("Blaze column found", (attrId >= 0));

        for (int i = 0; i < graph.getVertexCount(); i++) {
            final int txId = graph.getVertex(i);
            Object blaze = graph.getObjectValue(attrId, txId);
            if ((txId == vxId4) || (txId == vxId5)) {
                assertTrue(String.format("Vertex [%d] should have blaze", txId), (blaze != null));
            } else {
                assertTrue(String.format("Vertex [%d] should not have blaze", txId), (blaze == null));
            }
        }
    }

    @Test
    public void unsetBlazeTest() throws InterruptedException, PluginException {
        generateData();

        int attrId = graph.getAttribute(GraphElementType.VERTEX, "blaze");
        assertTrue("Blaze column found", (attrId >= 0));

        for (int i = 0; i < graph.getVertexCount(); i++) {
            final int txId = graph.getVertex(i);
            Object blaze = graph.getObjectValue(attrId, txId);
            assertTrue(String.format("Vertex [%d] should not have a blaze", txId), (blaze == null));
        }

        attrId = graph.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.SELECTED.getName());
        assertTrue("selected column found", (attrId >= 0));

        graph.setBooleanValue(attrId, vxId4, true);
        graph.setBooleanValue(attrId, vxId5, true);

        BitSet vertices = new BitSet();
        vertices.set(vxId4);
        vertices.set(vxId5);

        PluginExecution.withPlugin(VisualGraphPluginRegistry.ADD_BLAZE)
                .withParameter(BlazeUtilities.VERTEX_IDS_PARAMETER_ID, vertices)
                .executeNow(graph);

        attrId = graph.getAttribute(GraphElementType.VERTEX, "blaze");
        assertTrue("Blaze column found", (attrId >= 0));

        for (int i = 0; i < graph.getVertexCount(); i++) {
            final int txId = graph.getVertex(i);
            Object blaze = graph.getObjectValue(attrId, txId);
            if ((txId == vxId4) || (txId == vxId5)) {
                assertTrue(String.format("Vertex [%d] should have blaze", txId), (blaze != null));
            } else {
                assertTrue(String.format("Vertex [%d] should not have blaze", txId), (blaze == null));
            }
        }

        vertices = new BitSet();
        vertices.set(vxId4);
        vertices.set(vxId5);

        PluginExecution.withPlugin(VisualGraphPluginRegistry.REMOVE_BLAZE)
                .withParameter(BlazeUtilities.VERTEX_IDS_PARAMETER_ID, vertices)
                .executeNow(graph);

        attrId = graph.getAttribute(GraphElementType.VERTEX, "blaze");
        assertTrue("Blaze column found", (attrId >= 0));

        for (int i = 0; i < graph.getVertexCount(); i++) {
            final int txId = graph.getVertex(i);
            Object blaze = graph.getObjectValue(attrId, txId);
            assertTrue(String.format("Vertex [%d] should not have blaze", txId), (blaze == null));
        }
    }
}
