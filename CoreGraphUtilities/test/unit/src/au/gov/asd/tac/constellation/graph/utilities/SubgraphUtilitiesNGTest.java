/*
 * Copyright 2010-2021 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.utilities;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Subgraph Utilities Test.
 *
 * @author arcturus
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class SubgraphUtilitiesNGTest extends ConstellationTest {

    public SubgraphUtilitiesNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of copyGraph method, of class SubGraphUtilities.
     */
    @Test
    public void testCopyGraphWithTransaction() {
        final String VX_ATTRIBUTE = "Name";
        final String VX_VALUE = "Foo";
        final String TX_ATTRIBUTE = "Name";
        final String TX_VALUE = "Bar";

        final StoreGraph graph = new StoreGraph();
        final int vx0 = graph.addVertex();
        final int vx1 = graph.addVertex();
        final int tx0 = graph.addTransaction(vx0, vx1, true);
        final int vxAttr = graph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, VX_ATTRIBUTE, "", "", null);
        final int txAttr = graph.addAttribute(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, TX_ATTRIBUTE, "", "", null);
        graph.setPrimaryKey(GraphElementType.VERTEX, vxAttr);
        graph.setPrimaryKey(GraphElementType.TRANSACTION, txAttr);
        graph.setStringValue(vxAttr, vx0, VX_VALUE);
        graph.setStringValue(txAttr, tx0, TX_VALUE);

        assertEquals(graph.getVertexCount(), 2);
        assertEquals(graph.getTransactionCount(), 1);
        assertTrue(graph.getAttribute(GraphElementType.VERTEX, VX_ATTRIBUTE) != Graph.NOT_FOUND);
        assertTrue(graph.getAttribute(GraphElementType.TRANSACTION, TX_ATTRIBUTE) != Graph.NOT_FOUND);
        assertEquals(graph.getStringValue(vxAttr, vx0), VX_VALUE);
        assertEquals(graph.getStringValue(txAttr, tx0), TX_VALUE);

        final StoreGraph copy = SubgraphUtilities.copyGraph(graph);
        final int vxAttrCopy = copy.getAttribute(GraphElementType.VERTEX, VX_ATTRIBUTE);
        final int txAttrCopy = copy.getAttribute(GraphElementType.TRANSACTION, TX_ATTRIBUTE);

        assertEquals(copy.getVertexCount(), 2);
        assertEquals(copy.getTransactionCount(), 1);
        assertTrue(copy.getAttribute(GraphElementType.VERTEX, VX_ATTRIBUTE) != Graph.NOT_FOUND);
        assertTrue(copy.getAttribute(GraphElementType.TRANSACTION, TX_ATTRIBUTE) != Graph.NOT_FOUND);
        assertEquals(copy.getStringValue(vxAttrCopy, vx0), VX_VALUE);
        assertEquals(copy.getStringValue(txAttrCopy, tx0), TX_VALUE);
    }

    @Test
    public void testCopyGraphWithoutTransaction() {
        final String VX_ATTRIBUTE = "Name";
        final String VX_VALUE = "Foo";

        final StoreGraph graph = new StoreGraph();
        final int vx0 = graph.addVertex();
        final int vxAttr = graph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, VX_ATTRIBUTE, "", "", null);
        graph.setPrimaryKey(GraphElementType.VERTEX, vxAttr);

        graph.setStringValue(vxAttr, vx0, VX_VALUE);

        assertEquals(graph.getVertexCount(), 1);
        assertEquals(graph.getTransactionCount(), 0);
        assertTrue(graph.getAttribute(GraphElementType.VERTEX, VX_ATTRIBUTE) != Graph.NOT_FOUND);
        assertEquals(graph.getStringValue(vxAttr, vx0), VX_VALUE);

        final StoreGraph copy = SubgraphUtilities.copyGraph(graph);
        final int vxAttrCopy = copy.getAttribute(GraphElementType.VERTEX, VX_ATTRIBUTE);

        assertEquals(copy.getVertexCount(), 1);
        assertEquals(copy.getTransactionCount(), 0);
        assertTrue(copy.getAttribute(GraphElementType.VERTEX, VX_ATTRIBUTE) != Graph.NOT_FOUND);
        assertEquals(copy.getStringValue(vxAttrCopy, vx0), VX_VALUE);
    }

    @Test
    public void testCopyGraphWithoutTransactionButWithTransactionAttribute() {
        final String VX_ATTRIBUTE = "Name";
        final String VX_VALUE = "Foo";
        final String TX_ATTRIBUTE = "Name";

        final StoreGraph graph = new StoreGraph();
        final int vx0 = graph.addVertex();
        final int vxAttr = graph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, VX_ATTRIBUTE, "", "", null);
        final int txAttr = graph.addAttribute(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, TX_ATTRIBUTE, "", "", null);
        graph.setPrimaryKey(GraphElementType.VERTEX, vxAttr);
        graph.setPrimaryKey(GraphElementType.TRANSACTION, txAttr);

        graph.setStringValue(vxAttr, vx0, VX_VALUE);

        assertEquals(graph.getVertexCount(), 1);
        assertEquals(graph.getTransactionCount(), 0);
        assertTrue(graph.getAttribute(GraphElementType.VERTEX, VX_ATTRIBUTE) != Graph.NOT_FOUND);
        assertTrue(graph.getAttribute(GraphElementType.TRANSACTION, TX_ATTRIBUTE) != Graph.NOT_FOUND);
        assertEquals(graph.getStringValue(vxAttr, vx0), VX_VALUE);

        final StoreGraph copy = SubgraphUtilities.copyGraph(graph);
        final int vxAttrCopy = copy.getAttribute(GraphElementType.VERTEX, VX_ATTRIBUTE);

        assertEquals(copy.getVertexCount(), 1);
        assertEquals(copy.getTransactionCount(), 0);
        assertTrue(copy.getAttribute(GraphElementType.VERTEX, VX_ATTRIBUTE) != Graph.NOT_FOUND);
        assertTrue(copy.getAttribute(GraphElementType.TRANSACTION, TX_ATTRIBUTE) != Graph.NOT_FOUND);
        assertEquals(copy.getStringValue(vxAttrCopy, vx0), VX_VALUE);
    }
}
