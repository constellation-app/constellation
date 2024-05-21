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
package au.gov.asd.tac.constellation.graph.visual.plugins.select;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.attribute.BooleanAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.IntegerAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.visual.VisualGraphPluginRegistry;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Select All Transactions Nodes Test.
 *
 * @author altair
 */
public class SelectAllNGTest {

    private int attr1;
    private int attr2;
    private int attr3;
    private int attr4;
    private int attr5;
    private int attr6;
    private StoreGraph graph;

    public SelectAllNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        int xAttr, yAttr, zAttr;
        int selectedAttr;

        graph = new StoreGraph();
        attr1 = graph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "field1", null, null, null);
        attr2 = graph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "field2", null, null, null);
        attr3 = graph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "field3", null, null, null);
        attr4 = graph.addAttribute(GraphElementType.VERTEX, IntegerAttributeDescription.ATTRIBUTE_NAME, "field4", null, null, null);
        attr5 = graph.addAttribute(GraphElementType.VERTEX, IntegerAttributeDescription.ATTRIBUTE_NAME, "field5", null, null, null);
        attr6 = graph.addAttribute(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "field6", null, null, null);

        xAttr = graph.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "x", "x", 0.0, null);
        if (xAttr == Graph.NOT_FOUND) {
            fail();
        }

        yAttr = graph.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "y", "y", 0.0, null);
        if (yAttr == Graph.NOT_FOUND) {
            fail();
        }

        zAttr = graph.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "z", "z", 0.0, null);
        if (zAttr == Graph.NOT_FOUND) {
            fail();
        }

        selectedAttr = graph.addAttribute(GraphElementType.TRANSACTION, BooleanAttributeDescription.ATTRIBUTE_NAME, "selected", "selected", false, null);
        if (selectedAttr == Graph.NOT_FOUND) {
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

    private void generateDistinctTransactions() throws InterruptedException {
        int vxId1 = graph.addVertex();
        int vxId2 = graph.addVertex();
        int vxId3 = graph.addVertex();

        graph.setStringValue(attr1, vxId1, "a1");
        graph.setStringValue(attr2, vxId1, "b1");
        graph.setStringValue(attr3, vxId1, "c1");
        graph.setStringValue(attr4, vxId1, "10");
        graph.setStringValue(attr5, vxId1, "100");

        graph.setStringValue(attr1, vxId2, "a2");
        graph.setStringValue(attr2, vxId2, "b1");
        graph.setStringValue(attr3, vxId2, "c2");
        graph.setStringValue(attr4, vxId2, "20");
        graph.setStringValue(attr5, vxId2, "200");

        graph.setStringValue(attr1, vxId3, "a3");
        graph.setStringValue(attr2, vxId3, "b3");
        graph.setStringValue(attr3, vxId3, "c3");
        graph.setStringValue(attr4, vxId3, "30");
        graph.setStringValue(attr5, vxId3, "100");

        int txId1 = graph.addTransaction(vxId1, vxId2, true);
        int txId2 = graph.addTransaction(vxId2, vxId1, true);
        int txId3 = graph.addTransaction(vxId1, vxId2, false);
        int txId4 = graph.addTransaction(vxId2, vxId3, true);
        int txId5 = graph.addTransaction(vxId2, vxId3, true);

        graph.setStringValue(attr6, txId4, "a1");
        graph.setStringValue(attr6, txId5, "a2");
    }

    @Test
    public void selectAllTest() throws InterruptedException, PluginException {
        generateDistinctTransactions();

        int attrId = graph.getAttribute(GraphElementType.TRANSACTION, VisualConcept.TransactionAttribute.SELECTED.getName());
        assertTrue("Selection column found", (attrId >= 0));

        for (int i = 0; i < graph.getTransactionCount(); i++) {
            final int txId = graph.getTransaction(i);
            boolean flag = graph.getBooleanValue(attrId, txId);
            assertFalse(String.format("Transaction [%d] should be de-selected", txId), flag);
        }

        attrId = graph.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.SELECTED.getName());
        assertTrue("Selection column found", (attrId >= 0));

        for (int i = 0; i < graph.getVertexCount(); i++) {
            final int txId = graph.getVertex(i);
            boolean flag = graph.getBooleanValue(attrId, txId);
            assertFalse(String.format("Vertex [%d] should be de-selected", txId), flag);
        }

        PluginExecution.withPlugin(VisualGraphPluginRegistry.SELECT_ALL).executeNow(graph);

        attrId = graph.getAttribute(GraphElementType.TRANSACTION, VisualConcept.TransactionAttribute.SELECTED.getName());
        assertTrue("Selection column found", (attrId >= 0));

        for (int i = 0; i < graph.getTransactionCount(); i++) {
            final int txId = graph.getTransaction(i);
            boolean flag = graph.getBooleanValue(attrId, txId);
            assertTrue(String.format("Transaction [%d] should be selected", txId), flag);
        }

        attrId = graph.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.SELECTED.getName());
        assertTrue("Selection column found", (attrId >= 0));

        for (int i = 0; i < graph.getVertexCount(); i++) {
            final int txId = graph.getVertex(i);
            boolean flag = graph.getBooleanValue(attrId, txId);
            assertTrue(String.format("Vertex [%d] should be selected", txId), flag);
        }
    }

    @Test
    public void selectAllTransactionsTest() throws InterruptedException, PluginException {
        generateDistinctTransactions();

        int attrId = graph.getAttribute(GraphElementType.TRANSACTION, VisualConcept.TransactionAttribute.SELECTED.getName());
        assertTrue("Selection column found", (attrId >= 0));

        for (int i = 0; i < graph.getTransactionCount(); i++) {
            final int txId = graph.getTransaction(i);
            boolean flag = graph.getBooleanValue(attrId, txId);
            assertFalse(String.format("Transaction [%d] should be de-selected", txId), flag);
        }

        attrId = graph.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.SELECTED.getName());
        assertTrue("Selection column found", (attrId >= 0));

        for (int i = 0; i < graph.getVertexCount(); i++) {
            final int txId = graph.getVertex(i);
            boolean flag = graph.getBooleanValue(attrId, txId);
            assertFalse(String.format("Vertex [%d] should be de-selected", txId), flag);
        }

        PluginExecution.withPlugin(VisualGraphPluginRegistry.SELECT_ALL).executeNow(graph);
        PluginExecution.withPlugin(VisualGraphPluginRegistry.DESELECT_VERTICES).executeNow(graph);

        attrId = graph.getAttribute(GraphElementType.TRANSACTION, VisualConcept.TransactionAttribute.SELECTED.getName());
        assertTrue("Selection column found", (attrId >= 0));

        for (int i = 0; i < graph.getTransactionCount(); i++) {
            final int txId = graph.getTransaction(i);
            boolean flag = graph.getBooleanValue(attrId, txId);
            assertTrue(String.format("Transaction [%d] should be selected", txId), flag);
        }

        attrId = graph.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.SELECTED.getName());
        assertTrue("Selection column found", (attrId >= 0));

        for (int i = 0; i < graph.getVertexCount(); i++) {
            final int txId = graph.getVertex(i);
            boolean flag = graph.getBooleanValue(attrId, txId);
            assertFalse(String.format("Vertex [%d] should be de-selected", txId), flag);
        }
    }

    @Test
    public void selectAllNodesTest() throws InterruptedException, PluginException {
        generateDistinctTransactions();

        int attrId = graph.getAttribute(GraphElementType.TRANSACTION, VisualConcept.TransactionAttribute.SELECTED.getName());
        assertTrue("Selection column found", (attrId >= 0));

        for (int i = 0; i < graph.getTransactionCount(); i++) {
            final int txId = graph.getTransaction(i);
            boolean flag = graph.getBooleanValue(attrId, txId);
            assertFalse(String.format("Transaction [%d] should be de-selected", txId), flag);
        }

        attrId = graph.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.SELECTED.getName());
        assertTrue("Selection column found", (attrId >= 0));

        for (int i = 0; i < graph.getVertexCount(); i++) {
            final int txId = graph.getVertex(i);
            boolean flag = graph.getBooleanValue(attrId, txId);
            assertFalse(String.format("Vertex [%d] should be de-selected", txId), flag);
        }

        PluginExecution.withPlugin(VisualGraphPluginRegistry.SELECT_ALL).executeNow(graph);
        PluginExecution.withPlugin(VisualGraphPluginRegistry.DESELECT_TRANSACTIONS).executeNow(graph);

        attrId = graph.getAttribute(GraphElementType.TRANSACTION, VisualConcept.TransactionAttribute.SELECTED.getName());
        assertTrue("Selection column found", (attrId >= 0));

        for (int i = 0; i < graph.getTransactionCount(); i++) {
            final int txId = graph.getTransaction(i);
            boolean flag = graph.getBooleanValue(attrId, txId);
            assertFalse(String.format("Transaction [%d] should be de-selected", txId), flag);
        }

        attrId = graph.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.SELECTED.getName());
        assertTrue("Selection column found", (attrId >= 0));

        for (int i = 0; i < graph.getVertexCount(); i++) {
            final int txId = graph.getVertex(i);
            boolean flag = graph.getBooleanValue(attrId, txId);
            assertTrue(String.format("Vertex [%d] should be selected", txId), flag);
        }
    }

    @Test
    public void deselectAllTest() throws InterruptedException, PluginException {
        generateDistinctTransactions();

        int attrId = graph.getAttribute(GraphElementType.TRANSACTION, VisualConcept.TransactionAttribute.SELECTED.getName());
        assertTrue("Selection column found", (attrId >= 0));

        for (int i = 0; i < graph.getTransactionCount(); i++) {
            final int txId = graph.getTransaction(i);
            boolean flag = graph.getBooleanValue(attrId, txId);
            assertFalse(String.format("Transaction [%d] should be de-selected", txId), flag);
        }

        attrId = graph.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.SELECTED.getName());
        assertTrue("Selection column found", (attrId >= 0));

        for (int i = 0; i < graph.getVertexCount(); i++) {
            final int txId = graph.getVertex(i);
            boolean flag = graph.getBooleanValue(attrId, txId);
            assertFalse(String.format("Vertex [%d] should be de-selected", txId), flag);
        }

        PluginExecution.withPlugin(VisualGraphPluginRegistry.SELECT_ALL).executeNow(graph);

        attrId = graph.getAttribute(GraphElementType.TRANSACTION, VisualConcept.TransactionAttribute.SELECTED.getName());
        assertTrue("Selection column found", (attrId >= 0));

        for (int i = 0; i < graph.getTransactionCount(); i++) {
            final int txId = graph.getTransaction(i);
            boolean flag = graph.getBooleanValue(attrId, txId);
            assertTrue(String.format("Transaction [%d] should be selected", txId), flag);
        }

        attrId = graph.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.SELECTED.getName());
        assertTrue("Selection column found", (attrId >= 0));

        for (int i = 0; i < graph.getVertexCount(); i++) {
            final int txId = graph.getVertex(i);
            boolean flag = graph.getBooleanValue(attrId, txId);
            assertTrue(String.format("Vertex [%d] should be selected", txId), flag);
        }

        PluginExecution.withPlugin(VisualGraphPluginRegistry.DESELECT_ALL).executeNow(graph);

        attrId = graph.getAttribute(GraphElementType.TRANSACTION, VisualConcept.TransactionAttribute.SELECTED.getName());
        assertTrue("Selection column found", (attrId >= 0));

        for (int i = 0; i < graph.getTransactionCount(); i++) {
            final int txId = graph.getTransaction(i);
            boolean flag = graph.getBooleanValue(attrId, txId);
            assertFalse(String.format("Transaction [%d] should be deselected", txId), flag);
        }

        attrId = graph.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.SELECTED.getName());
        assertTrue("Selection column found", (attrId >= 0));

        for (int i = 0; i < graph.getVertexCount(); i++) {
            final int txId = graph.getVertex(i);
            boolean flag = graph.getBooleanValue(attrId, txId);
            assertFalse(String.format("Vertex [%d] should be deselected", txId), flag);
        }
    }
}
