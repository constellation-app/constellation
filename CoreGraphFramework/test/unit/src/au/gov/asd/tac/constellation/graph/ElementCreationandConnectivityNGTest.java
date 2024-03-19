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
package au.gov.asd.tac.constellation.graph;

import au.gov.asd.tac.constellation.graph.attribute.IntegerAttributeDescription;
import java.util.ArrayList;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Element Creation and Connectivity Test.
 *
 * @author algol
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class ElementCreationandConnectivityNGTest extends ConstellationTest {

    public ElementCreationandConnectivityNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of addElement method, of class StoreGraph.
     */
    @Test
    public void addElement() {
        final GraphWriteMethods graph = new StoreGraph();
        final int v1 = graph.addVertex();
        final int v2 = graph.addVertex();
        assertTrue("Two elements can't have the same index", v1 != v2);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void removeElementTwice() {
        final GraphWriteMethods graph = new StoreGraph();
        final int v0 = graph.addVertex();
        graph.removeVertex(v0);
        graph.removeVertex(v0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void removeOutOfBoundsElement() {
        final GraphWriteMethods graph = new StoreGraph();
        graph.removeVertex(99);
    }

    @Test
    public void sizeElements() {
        final GraphElementType elementType = GraphElementType.VERTEX;
        final String name = "node1";
        final String attributeType = "integer";
        final GraphWriteMethods graph = new StoreGraph();
        final int attr = graph.addAttribute(elementType, attributeType, name, null, null, null);

        final int N = 10;
        final int[] attrs = new int[N];
        assertEquals("Adding some elements", 0, graph.getVertexCount());
        for (int i = 0; i < N; i++) {
            attrs[i] = graph.addVertex();
            graph.setIntValue(attr, attrs[i], i);
            assertEquals("Adding some elements", i + 1, graph.getVertexCount());
            assertTrue("Adding, capacity", graph.getVertexCapacity() >= graph.getVertexCount());
        }

        for (int i = 0; i < N; i++) {
            graph.removeVertex(attrs[i]);
            assertEquals("Removing some elements", N - i - 1, graph.getVertexCount());
            assertTrue("Removing, capacity", graph.getVertexCapacity() >= graph.getVertexCount());
        }
    }

    @Test
    public void nodeIterator() {
        int numNodes = 10;
        StoreGraph graph = new StoreGraph();
        final int attrInt = graph.addAttribute(GraphElementType.VERTEX, IntegerAttributeDescription.ATTRIBUTE_NAME, "intattr", "descr", 23, null);

        for (int i = 0; i < numNodes; i++) {
            final int ix = graph.addVertex();
            graph.setIntValue(attrInt, ix, i);
        }

        int count = 0;
        for (int i = 0; i < graph.getVertexCount(); i++) {
            int vxId = graph.getVertex(i);
            int value = graph.getIntValue(attrInt, vxId);
            assertTrue("value is correct " + vxId, i == value);
            assertTrue("Iterate over all nodes except " + vxId, i == vxId);
            count++;
        }

        // This iteration will iterate over each attribute in order.
        assertTrue("Total nodes", graph.getVertexCount() == numNodes);

        // This iteration will iterate over each attribute in order except 1.
        // The removed element should not be seen.
        count = 0;
        while (graph.getVertexCount() > 0) {
            int vxId = graph.getVertex(0);
            graph.removeVertex(vxId);
            count++;
        }
        assertEquals("Count iterated removed", numNodes, count);
    }

    /**
     * Remove a node and make sure the attached edges are also removed.
     */
    @Test
    public void removeVertices() {
        final GraphWriteMethods graph = new StoreGraph();

        final int id0 = graph.addVertex();
        final int id1 = graph.addVertex();
        final int id2 = graph.addVertex();
        final int tx0 = graph.addTransaction(id0, id1, false);
        final int tx1 = graph.addTransaction(id1, id2, false);
        final int tx2 = graph.addTransaction(id2, id1, false);

        assertEquals("Number of vertices", 3, graph.getVertexCount());
        assertEquals("Number of transactions", 3, graph.getTransactionCount());

        graph.removeVertex(id2);
        assertEquals("Number of vertices", 2, graph.getVertexCount());
        assertEquals("Number of transactions", 1, graph.getTransactionCount());

        graph.removeVertex(id0);
        assertEquals("Number of vertices", 1, graph.getVertexCount());
        assertEquals("Number of transactions", 0, graph.getTransactionCount());

        graph.removeVertex(id1);
        assertEquals("Number of vertices", 0, graph.getVertexCount());
        assertEquals("Number of transactions", 0, graph.getTransactionCount());
    }

    @Test
    public void removeTransactions() {
        final GraphWriteMethods graph = new StoreGraph();

        final int id0 = graph.addVertex();
        final int id1 = graph.addVertex();
        final int id2 = graph.addVertex();
        final int tx0 = graph.addTransaction(id0, id1, false);
        final int tx1 = graph.addTransaction(id1, id2, false);
        final int tx2 = graph.addTransaction(id2, id1, false);

        assertEquals("Number of vertices", 3, graph.getVertexCount());
        assertEquals("Number of transactions", 3, graph.getTransactionCount());

        graph.removeTransaction(tx2);
        assertEquals("Number of vertices", 3, graph.getVertexCount());
        assertEquals("Number of transactions", 2, graph.getTransactionCount());

        graph.removeTransaction(tx0);
        assertEquals("Number of vertices", 3, graph.getVertexCount());
        assertEquals("Number of transactions", 1, graph.getTransactionCount());

        graph.removeTransaction(tx1);
        assertEquals("Number of vertices", 3, graph.getVertexCount());
        assertEquals("Number of transactions", 0, graph.getTransactionCount());
    }

    @Test
    public void neighbours() {
        StoreGraph graph = new StoreGraph();
        final int origin = graph.addVertex();
        final int numNodes = 20;

        // Add N nodes, remember half of them.
        ArrayList<Integer> otherNodes = new ArrayList<>();
        for (int i = 0; i < numNodes; i++) {
            final int node = graph.addVertex();
            if (node % 2 == 0) {
                otherNodes.add(node);
            }
        }

        assertEquals("Number of nodes that will be connected", numNodes / 2, otherNodes.size());

        // Of the half of the nodes we remembered, add edges to half and edges from half.
        // For extra fun, there are two outgoing edges to each neighbour and only one incoming edge.
        for (int i = 0; i < otherNodes.size(); i++) {
            if (i % 2 == 0) {
                final int edge = graph.addTransaction(otherNodes.get(i), origin, true);
            } else {
                final int edge = graph.addTransaction(origin, otherNodes.get(i), true);
            }
        }

        assertEquals("Number of neighbours", otherNodes.size(), graph.getVertexTransactionCount(origin));
    }
}
