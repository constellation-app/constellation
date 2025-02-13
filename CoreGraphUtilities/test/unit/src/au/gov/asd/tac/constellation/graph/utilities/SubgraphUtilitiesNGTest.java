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
public class SubgraphUtilitiesNGTest {
    
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
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of copyGraph method, of class SubGraphUtilities.
     */
    @Test
    public void testCopyGraphWithTransaction() {
        final String vxAttribute = "Name";
        final String vxValue = "Foo";
        final String txAttribute = "Name";
        final String txValue = "Bar";

        final StoreGraph graph = new StoreGraph();
        final int vx0 = graph.addVertex();
        final int vx1 = graph.addVertex();
        final int tx0 = graph.addTransaction(vx0, vx1, true);
        final int vxAttr = graph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, vxAttribute, "", "", null);
        final int txAttr = graph.addAttribute(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, txAttribute, "", "", null);
        graph.setPrimaryKey(GraphElementType.VERTEX, vxAttr);
        graph.setPrimaryKey(GraphElementType.TRANSACTION, txAttr);
        graph.setStringValue(vxAttr, vx0, vxValue);
        graph.setStringValue(txAttr, tx0, txValue);

        assertEquals(graph.getVertexCount(), 2);
        assertEquals(graph.getTransactionCount(), 1);
        assertTrue(graph.getAttribute(GraphElementType.VERTEX, vxAttribute) != Graph.NOT_FOUND);
        assertTrue(graph.getAttribute(GraphElementType.TRANSACTION, txAttribute) != Graph.NOT_FOUND);
        assertEquals(graph.getStringValue(vxAttr, vx0), vxValue);
        assertEquals(graph.getStringValue(txAttr, tx0), txValue);

        final StoreGraph copy = SubgraphUtilities.copyGraph(graph);
        final int vxAttrCopy = copy.getAttribute(GraphElementType.VERTEX, vxAttribute);
        final int txAttrCopy = copy.getAttribute(GraphElementType.TRANSACTION, txAttribute);

        assertEquals(copy.getVertexCount(), 2);
        assertEquals(copy.getTransactionCount(), 1);
        assertTrue(copy.getAttribute(GraphElementType.VERTEX, vxAttribute) != Graph.NOT_FOUND);
        assertTrue(copy.getAttribute(GraphElementType.TRANSACTION, txAttribute) != Graph.NOT_FOUND);
        assertEquals(copy.getStringValue(vxAttrCopy, vx0), vxValue);
        assertEquals(copy.getStringValue(txAttrCopy, tx0), txValue);
    }

    @Test
    public void testCopyGraphWithoutTransaction() {
        final String vxAttribute = "Name";
        final String vxValue = "Foo";

        final StoreGraph graph = new StoreGraph();
        final int vx0 = graph.addVertex();
        final int vxAttr = graph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, vxAttribute, "", "", null);
        graph.setPrimaryKey(GraphElementType.VERTEX, vxAttr);

        graph.setStringValue(vxAttr, vx0, vxValue);

        assertEquals(graph.getVertexCount(), 1);
        assertEquals(graph.getTransactionCount(), 0);
        assertTrue(graph.getAttribute(GraphElementType.VERTEX, vxAttribute) != Graph.NOT_FOUND);
        assertEquals(graph.getStringValue(vxAttr, vx0), vxValue);

        final StoreGraph copy = SubgraphUtilities.copyGraph(graph);
        final int vxAttrCopy = copy.getAttribute(GraphElementType.VERTEX, vxAttribute);

        assertEquals(copy.getVertexCount(), 1);
        assertEquals(copy.getTransactionCount(), 0);
        assertTrue(copy.getAttribute(GraphElementType.VERTEX, vxAttribute) != Graph.NOT_FOUND);
        assertEquals(copy.getStringValue(vxAttrCopy, vx0), vxValue);
    }

    @Test
    public void testCopyGraphWithoutTransactionButWithTransactionAttribute() {
        final String vxAttribute = "Name";
        final String vxValue = "Foo";
        final String txAttribute = "Name";

        final StoreGraph graph = new StoreGraph();
        final int vx0 = graph.addVertex();
        final int vxAttr = graph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, vxAttribute, "", "", null);
        final int txAttr = graph.addAttribute(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, txAttribute, "", "", null);
        graph.setPrimaryKey(GraphElementType.VERTEX, vxAttr);
        graph.setPrimaryKey(GraphElementType.TRANSACTION, txAttr);

        graph.setStringValue(vxAttr, vx0, vxValue);

        assertEquals(graph.getVertexCount(), 1);
        assertEquals(graph.getTransactionCount(), 0);
        assertTrue(graph.getAttribute(GraphElementType.VERTEX, vxAttribute) != Graph.NOT_FOUND);
        assertTrue(graph.getAttribute(GraphElementType.TRANSACTION, txAttribute) != Graph.NOT_FOUND);
        assertEquals(graph.getStringValue(vxAttr, vx0), vxValue);

        final StoreGraph copy = SubgraphUtilities.copyGraph(graph);
        final int vxAttrCopy = copy.getAttribute(GraphElementType.VERTEX, vxAttribute);

        assertEquals(copy.getVertexCount(), 1);
        assertEquals(copy.getTransactionCount(), 0);
        assertTrue(copy.getAttribute(GraphElementType.VERTEX, vxAttribute) != Graph.NOT_FOUND);
        assertTrue(copy.getAttribute(GraphElementType.TRANSACTION, txAttribute) != Graph.NOT_FOUND);
        assertEquals(copy.getStringValue(vxAttrCopy, vx0), vxValue);
    }
}
