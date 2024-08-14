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
package au.gov.asd.tac.constellation.graph;

import au.gov.asd.tac.constellation.utilities.memory.MemoryManager;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.times;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class StoreGraphNGTest {

    @Test
    public void testGarbageCollection() {
        System.out.println("testGarbageCollection");
        final int numInstances = 5000;
        final String id = "id";

        try (MockedStatic<MemoryManager> mockMemoryManager = Mockito.mockStatic(MemoryManager.class, Mockito.CALLS_REAL_METHODS)) {
            for (int i = 0; i < numInstances; i++) {
                // Create graph and immediately overwrite its reference
                StoreGraph instance = new StoreGraph(id);
                assertEquals(instance.getId(), id);
                instance = null;
                assertNull(instance);
            }

            // Hint garbage collection
            System.gc();

            // Verify instances were made
            mockMemoryManager.verify(() -> MemoryManager.newObject(any()), times(numInstances));

            // Verify there are no remaining instances, because for some reason finalizeObject() verification doesn't work
            final MemoryManager.ClassStats stats = MemoryManager.getObjectCounts().get(StoreGraph.class);
            assertEquals(stats.getCurrentCount(), 0);
        }
    }

//    /**
//     * Test of setGraphEdit method, of class StoreGraph.
//     */
//    @Test
//    public void testSetGraphEdit() {
//        System.out.println("setGraphEdit");
//        GraphEdit graphEdit = null;
//        StoreGraph instance = new StoreGraph();
//        instance.setGraphEdit(graphEdit);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isRecordingEdit method, of class StoreGraph.
//     */
//    @Test
//    public void testIsRecordingEdit() {
//        System.out.println("isRecordingEdit");
//        StoreGraph instance = new StoreGraph();
//        boolean expResult = false;
//        boolean result = instance.isRecordingEdit();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setModificationCounters method, of class StoreGraph.
//     */
//    @Test
//    public void testSetModificationCounters() {
//        System.out.println("setModificationCounters");
//        long globalModificationCounter = 0L;
//        long structureModificationCounter = 0L;
//        long attributeModificationCounter = 0L;
//        StoreGraph instance = new StoreGraph();
//        instance.setModificationCounters(globalModificationCounter, structureModificationCounter, attributeModificationCounter);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setValueModificationCounter method, of class StoreGraph.
//     */
//    @Test
//    public void testSetValueModificationCounter() {
//        System.out.println("setValueModificationCounter");
//        int attribute = 0;
//        long modificationCounter = 0L;
//        StoreGraph instance = new StoreGraph();
//        instance.setValueModificationCounter(attribute, modificationCounter);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getModificationCounter method, of class StoreGraph.
//     */
//    @Test
//    public void testGetModificationCounter() {
//        System.out.println("getModificationCounter");
//        StoreGraph instance = new StoreGraph();
//        long expResult = 0L;
//        long result = instance.getModificationCounter();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of validateKeys method, of class StoreGraph.
//     */
//    @Test
//    public void testValidateKeys() {
//        System.out.println("validateKeys");
//        StoreGraph instance = new StoreGraph();
//        instance.validateKeys();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of executeGraphOperation method, of class StoreGraph.
//     */
//    @Test
//    public void testExecuteGraphOperation() {
//        System.out.println("executeGraphOperation");
//        GraphOperation operation = null;
//        StoreGraph instance = new StoreGraph();
//        instance.executeGraphOperation(operation);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of validateKey method, of class StoreGraph.
//     */
//    @Test
//    public void testValidateKey_GraphElementType_boolean() {
//        System.out.println("validateKey");
//        GraphElementType elementType = null;
//        boolean allowMerging = false;
//        StoreGraph instance = new StoreGraph();
//        instance.validateKey(elementType, allowMerging);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of validateKey method, of class StoreGraph.
//     */
//    @Test
//    public void testValidateKey_3args() {
//        System.out.println("validateKey");
//        GraphElementType elementType = null;
//        int element = 0;
//        boolean allowMerging = false;
//        StoreGraph instance = new StoreGraph();
//        instance.validateKey(elementType, element, allowMerging);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of copy method, of class StoreGraph.
//     */
//    @Test
//    public void testCopy_0args() {
//        System.out.println("copy");
//        StoreGraph instance = new StoreGraph();
//        GraphReadMethods expResult = null;
//        GraphReadMethods result = instance.copy();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of copy method, of class StoreGraph.
//     */
//    @Test
//    public void testCopy_String() {
//        System.out.println("copy");
//        String id = "";
//        StoreGraph instance = new StoreGraph();
//        GraphReadMethods expResult = null;
//        GraphReadMethods result = instance.copy(id);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getId method, of class StoreGraph.
//     */
//    @Test
//    public void testGetId() {
//        System.out.println("getId");
//        StoreGraph instance = new StoreGraph();
//        String expResult = "";
//        String result = instance.getId();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getSchema method, of class StoreGraph.
//     */
//    @Test
//    public void testGetSchema() {
//        System.out.println("getSchema");
//        StoreGraph instance = new StoreGraph();
//        Schema expResult = null;
//        Schema result = instance.getSchema();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getGlobalModificationCounter method, of class StoreGraph.
//     */
//    @Test
//    public void testGetGlobalModificationCounter() {
//        System.out.println("getGlobalModificationCounter");
//        StoreGraph instance = new StoreGraph();
//        long expResult = 0L;
//        long result = instance.getGlobalModificationCounter();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getAttributeModificationCounter method, of class StoreGraph.
//     */
//    @Test
//    public void testGetAttributeModificationCounter() {
//        System.out.println("getAttributeModificationCounter");
//        StoreGraph instance = new StoreGraph();
//        long expResult = 0L;
//        long result = instance.getAttributeModificationCounter();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getStructureModificationCounter method, of class StoreGraph.
//     */
//    @Test
//    public void testGetStructureModificationCounter() {
//        System.out.println("getStructureModificationCounter");
//        StoreGraph instance = new StoreGraph();
//        long expResult = 0L;
//        long result = instance.getStructureModificationCounter();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getValueModificationCounter method, of class StoreGraph.
//     */
//    @Test
//    public void testGetValueModificationCounter() {
//        System.out.println("getValueModificationCounter");
//        int attribute = 0;
//        StoreGraph instance = new StoreGraph();
//        long expResult = 0L;
//        long result = instance.getValueModificationCounter(attribute);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getVertexCapacity method, of class StoreGraph.
//     */
//    @Test
//    public void testGetVertexCapacity() {
//        System.out.println("getVertexCapacity");
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getVertexCapacity();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getLinkCapacity method, of class StoreGraph.
//     */
//    @Test
//    public void testGetLinkCapacity() {
//        System.out.println("getLinkCapacity");
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getLinkCapacity();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getEdgeCapacity method, of class StoreGraph.
//     */
//    @Test
//    public void testGetEdgeCapacity() {
//        System.out.println("getEdgeCapacity");
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getEdgeCapacity();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getTransactionCapacity method, of class StoreGraph.
//     */
//    @Test
//    public void testGetTransactionCapacity() {
//        System.out.println("getTransactionCapacity");
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getTransactionCapacity();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getVertexCount method, of class StoreGraph.
//     */
//    @Test
//    public void testGetVertexCount() {
//        System.out.println("getVertexCount");
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getVertexCount();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getLinkCount method, of class StoreGraph.
//     */
//    @Test
//    public void testGetLinkCount() {
//        System.out.println("getLinkCount");
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getLinkCount();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getEdgeCount method, of class StoreGraph.
//     */
//    @Test
//    public void testGetEdgeCount() {
//        System.out.println("getEdgeCount");
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getEdgeCount();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getTransactionCount method, of class StoreGraph.
//     */
//    @Test
//    public void testGetTransactionCount() {
//        System.out.println("getTransactionCount");
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getTransactionCount();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getVertex method, of class StoreGraph.
//     */
//    @Test
//    public void testGetVertex() {
//        System.out.println("getVertex");
//        int position = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getVertex(position);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getLink method, of class StoreGraph.
//     */
//    @Test
//    public void testGetLink_int() {
//        System.out.println("getLink");
//        int position = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getLink(position);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getLinkPosition method, of class StoreGraph.
//     */
//    @Test
//    public void testGetLinkPosition() {
//        System.out.println("getLinkPosition");
//        int link = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getLinkPosition(link);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getLinkUID method, of class StoreGraph.
//     */
//    @Test
//    public void testGetLinkUID() {
//        System.out.println("getLinkUID");
//        int link = 0;
//        StoreGraph instance = new StoreGraph();
//        long expResult = 0L;
//        long result = instance.getLinkUID(link);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getEdge method, of class StoreGraph.
//     */
//    @Test
//    public void testGetEdge() {
//        System.out.println("getEdge");
//        int position = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getEdge(position);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getEdgePosition method, of class StoreGraph.
//     */
//    @Test
//    public void testGetEdgePosition() {
//        System.out.println("getEdgePosition");
//        int edge = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getEdgePosition(edge);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getEdgeUID method, of class StoreGraph.
//     */
//    @Test
//    public void testGetEdgeUID() {
//        System.out.println("getEdgeUID");
//        int edge = 0;
//        StoreGraph instance = new StoreGraph();
//        long expResult = 0L;
//        long result = instance.getEdgeUID(edge);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getTransaction method, of class StoreGraph.
//     */
//    @Test
//    public void testGetTransaction() {
//        System.out.println("getTransaction");
//        int position = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getTransaction(position);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getTransactionPosition method, of class StoreGraph.
//     */
//    @Test
//    public void testGetTransactionPosition() {
//        System.out.println("getTransactionPosition");
//        int transaction = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getTransactionPosition(transaction);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getTransactionUID method, of class StoreGraph.
//     */
//    @Test
//    public void testGetTransactionUID() {
//        System.out.println("getTransactionUID");
//        int transaction = 0;
//        StoreGraph instance = new StoreGraph();
//        long expResult = 0L;
//        long result = instance.getTransactionUID(transaction);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of vertexExists method, of class StoreGraph.
//     */
//    @Test
//    public void testVertexExists() {
//        System.out.println("vertexExists");
//        int vertex = 0;
//        StoreGraph instance = new StoreGraph();
//        boolean expResult = false;
//        boolean result = instance.vertexExists(vertex);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of linkExists method, of class StoreGraph.
//     */
//    @Test
//    public void testLinkExists() {
//        System.out.println("linkExists");
//        int link = 0;
//        StoreGraph instance = new StoreGraph();
//        boolean expResult = false;
//        boolean result = instance.linkExists(link);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of edgeExists method, of class StoreGraph.
//     */
//    @Test
//    public void testEdgeExists() {
//        System.out.println("edgeExists");
//        int edge = 0;
//        StoreGraph instance = new StoreGraph();
//        boolean expResult = false;
//        boolean result = instance.edgeExists(edge);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of transactionExists method, of class StoreGraph.
//     */
//    @Test
//    public void testTransactionExists() {
//        System.out.println("transactionExists");
//        int transaction = 0;
//        StoreGraph instance = new StoreGraph();
//        boolean expResult = false;
//        boolean result = instance.transactionExists(transaction);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of addVertex method, of class StoreGraph.
//     */
//    @Test
//    public void testAddVertex_0args() {
//        System.out.println("addVertex");
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.addVertex();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of addVertex method, of class StoreGraph.
//     */
//    @Test
//    public void testAddVertex_int() {
//        System.out.println("addVertex");
//        int vertex = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.addVertex(vertex);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of removeVertex method, of class StoreGraph.
//     */
//    @Test
//    public void testRemoveVertex() {
//        System.out.println("removeVertex");
//        int vertex = 0;
//        StoreGraph instance = new StoreGraph();
//        instance.removeVertex(vertex);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getLink method, of class StoreGraph.
//     */
//    @Test
//    public void testGetLink_int_int() {
//        System.out.println("getLink");
//        int vertex1 = 0;
//        int vertex2 = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getLink(vertex1, vertex2);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of addTransaction method, of class StoreGraph.
//     */
//    @Test
//    public void testAddTransaction_3args() {
//        System.out.println("addTransaction");
//        int sourceVertex = 0;
//        int destinationVertex = 0;
//        boolean directed = false;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.addTransaction(sourceVertex, destinationVertex, directed);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of addTransaction method, of class StoreGraph.
//     */
//    @Test
//    public void testAddTransaction_4args() {
//        System.out.println("addTransaction");
//        int transaction = 0;
//        int sourceVertex = 0;
//        int destinationVertex = 0;
//        boolean directed = false;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.addTransaction(transaction, sourceVertex, destinationVertex, directed);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of removeTransaction method, of class StoreGraph.
//     */
//    @Test
//    public void testRemoveTransaction() {
//        System.out.println("removeTransaction");
//        int transaction = 0;
//        StoreGraph instance = new StoreGraph();
//        instance.removeTransaction(transaction);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setTransactionSourceVertex method, of class StoreGraph.
//     */
//    @Test
//    public void testSetTransactionSourceVertex() {
//        System.out.println("setTransactionSourceVertex");
//        int transaction = 0;
//        int newSourceVertex = 0;
//        StoreGraph instance = new StoreGraph();
//        instance.setTransactionSourceVertex(transaction, newSourceVertex);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setTransactionDestinationVertex method, of class StoreGraph.
//     */
//    @Test
//    public void testSetTransactionDestinationVertex() {
//        System.out.println("setTransactionDestinationVertex");
//        int transaction = 0;
//        int newDestinationVertex = 0;
//        StoreGraph instance = new StoreGraph();
//        instance.setTransactionDestinationVertex(transaction, newDestinationVertex);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getVertexLinkCount method, of class StoreGraph.
//     */
//    @Test
//    public void testGetVertexLinkCount() {
//        System.out.println("getVertexLinkCount");
//        int vertex = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getVertexLinkCount(vertex);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getVertexLink method, of class StoreGraph.
//     */
//    @Test
//    public void testGetVertexLink() {
//        System.out.println("getVertexLink");
//        int vertex = 0;
//        int position = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getVertexLink(vertex, position);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getVertexNeighbourCount method, of class StoreGraph.
//     */
//    @Test
//    public void testGetVertexNeighbourCount() {
//        System.out.println("getVertexNeighbourCount");
//        int vertex = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getVertexNeighbourCount(vertex);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getVertexEdgeCount method, of class StoreGraph.
//     */
//    @Test
//    public void testGetVertexEdgeCount_int() {
//        System.out.println("getVertexEdgeCount");
//        int vertex = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getVertexEdgeCount(vertex);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getVertexEdge method, of class StoreGraph.
//     */
//    @Test
//    public void testGetVertexEdge_int_int() {
//        System.out.println("getVertexEdge");
//        int vertex = 0;
//        int position = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getVertexEdge(vertex, position);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getVertexEdgeCount method, of class StoreGraph.
//     */
//    @Test
//    public void testGetVertexEdgeCount_int_int() {
//        System.out.println("getVertexEdgeCount");
//        int vertex = 0;
//        int direction = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getVertexEdgeCount(vertex, direction);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getVertexEdge method, of class StoreGraph.
//     */
//    @Test
//    public void testGetVertexEdge_3args() {
//        System.out.println("getVertexEdge");
//        int vertex = 0;
//        int direction = 0;
//        int position = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getVertexEdge(vertex, direction, position);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getVertexNeighbour method, of class StoreGraph.
//     */
//    @Test
//    public void testGetVertexNeighbour() {
//        System.out.println("getVertexNeighbour");
//        int vertex = 0;
//        int position = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getVertexNeighbour(vertex, position);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getLinkLowVertex method, of class StoreGraph.
//     */
//    @Test
//    public void testGetLinkLowVertex() {
//        System.out.println("getLinkLowVertex");
//        int link = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getLinkLowVertex(link);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getLinkHighVertex method, of class StoreGraph.
//     */
//    @Test
//    public void testGetLinkHighVertex() {
//        System.out.println("getLinkHighVertex");
//        int link = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getLinkHighVertex(link);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getLinkEdgeCount method, of class StoreGraph.
//     */
//    @Test
//    public void testGetLinkEdgeCount_int() {
//        System.out.println("getLinkEdgeCount");
//        int link = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getLinkEdgeCount(link);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getLinkEdge method, of class StoreGraph.
//     */
//    @Test
//    public void testGetLinkEdge_int_int() {
//        System.out.println("getLinkEdge");
//        int link = 0;
//        int position = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getLinkEdge(link, position);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getLinkEdgeCount method, of class StoreGraph.
//     */
//    @Test
//    public void testGetLinkEdgeCount_int_int() {
//        System.out.println("getLinkEdgeCount");
//        int link = 0;
//        int direction = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getLinkEdgeCount(link, direction);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getLinkEdge method, of class StoreGraph.
//     */
//    @Test
//    public void testGetLinkEdge_3args() {
//        System.out.println("getLinkEdge");
//        int link = 0;
//        int direction = 0;
//        int position = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getLinkEdge(link, direction, position);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getLinkTransactionCount method, of class StoreGraph.
//     */
//    @Test
//    public void testGetLinkTransactionCount_int() {
//        System.out.println("getLinkTransactionCount");
//        int link = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getLinkTransactionCount(link);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getLinkTransaction method, of class StoreGraph.
//     */
//    @Test
//    public void testGetLinkTransaction_int_int() {
//        System.out.println("getLinkTransaction");
//        int link = 0;
//        int position = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getLinkTransaction(link, position);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getLinkTransactionCount method, of class StoreGraph.
//     */
//    @Test
//    public void testGetLinkTransactionCount_int_int() {
//        System.out.println("getLinkTransactionCount");
//        int link = 0;
//        int direction = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getLinkTransactionCount(link, direction);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getLinkTransaction method, of class StoreGraph.
//     */
//    @Test
//    public void testGetLinkTransaction_3args() {
//        System.out.println("getLinkTransaction");
//        int link = 0;
//        int direction = 0;
//        int position = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getLinkTransaction(link, direction, position);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getTransactionLink method, of class StoreGraph.
//     */
//    @Test
//    public void testGetTransactionLink() {
//        System.out.println("getTransactionLink");
//        int transaction = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getTransactionLink(transaction);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getTransactionEdge method, of class StoreGraph.
//     */
//    @Test
//    public void testGetTransactionEdge() {
//        System.out.println("getTransactionEdge");
//        int transaction = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getTransactionEdge(transaction);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getTransactionDirection method, of class StoreGraph.
//     */
//    @Test
//    public void testGetTransactionDirection() {
//        System.out.println("getTransactionDirection");
//        int transaction = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getTransactionDirection(transaction);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getTransactionSourceVertex method, of class StoreGraph.
//     */
//    @Test
//    public void testGetTransactionSourceVertex() {
//        System.out.println("getTransactionSourceVertex");
//        int transaction = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getTransactionSourceVertex(transaction);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getTransactionDestinationVertex method, of class StoreGraph.
//     */
//    @Test
//    public void testGetTransactionDestinationVertex() {
//        System.out.println("getTransactionDestinationVertex");
//        int transaction = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getTransactionDestinationVertex(transaction);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getVertexTransactionCount method, of class StoreGraph.
//     */
//    @Test
//    public void testGetVertexTransactionCount_int() {
//        System.out.println("getVertexTransactionCount");
//        int vertex = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getVertexTransactionCount(vertex);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getVertexPosition method, of class StoreGraph.
//     */
//    @Test
//    public void testGetVertexPosition() {
//        System.out.println("getVertexPosition");
//        int vertex = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getVertexPosition(vertex);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getVertexUID method, of class StoreGraph.
//     */
//    @Test
//    public void testGetVertexUID() {
//        System.out.println("getVertexUID");
//        int vertex = 0;
//        StoreGraph instance = new StoreGraph();
//        long expResult = 0L;
//        long result = instance.getVertexUID(vertex);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getVertexTransaction method, of class StoreGraph.
//     */
//    @Test
//    public void testGetVertexTransaction_int_int() {
//        System.out.println("getVertexTransaction");
//        int vertex = 0;
//        int position = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getVertexTransaction(vertex, position);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getVertexTransactionCount method, of class StoreGraph.
//     */
//    @Test
//    public void testGetVertexTransactionCount_int_int() {
//        System.out.println("getVertexTransactionCount");
//        int vertex = 0;
//        int direction = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getVertexTransactionCount(vertex, direction);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getVertexTransaction method, of class StoreGraph.
//     */
//    @Test
//    public void testGetVertexTransaction_3args() {
//        System.out.println("getVertexTransaction");
//        int vertex = 0;
//        int direction = 0;
//        int position = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getVertexTransaction(vertex, direction, position);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getEdgeLink method, of class StoreGraph.
//     */
//    @Test
//    public void testGetEdgeLink() {
//        System.out.println("getEdgeLink");
//        int edge = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getEdgeLink(edge);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getEdgeDirection method, of class StoreGraph.
//     */
//    @Test
//    public void testGetEdgeDirection() {
//        System.out.println("getEdgeDirection");
//        int edge = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getEdgeDirection(edge);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getEdgeSourceVertex method, of class StoreGraph.
//     */
//    @Test
//    public void testGetEdgeSourceVertex() {
//        System.out.println("getEdgeSourceVertex");
//        int edge = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getEdgeSourceVertex(edge);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getEdgeDestinationVertex method, of class StoreGraph.
//     */
//    @Test
//    public void testGetEdgeDestinationVertex() {
//        System.out.println("getEdgeDestinationVertex");
//        int edge = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getEdgeDestinationVertex(edge);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getEdgeTransactionCount method, of class StoreGraph.
//     */
//    @Test
//    public void testGetEdgeTransactionCount() {
//        System.out.println("getEdgeTransactionCount");
//        int edge = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getEdgeTransactionCount(edge);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getEdgeTransaction method, of class StoreGraph.
//     */
//    @Test
//    public void testGetEdgeTransaction() {
//        System.out.println("getEdgeTransaction");
//        int edge = 0;
//        int position = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getEdgeTransaction(edge, position);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of updateAttributeName method, of class StoreGraph.
//     */
//    @Test
//    public void testUpdateAttributeName() {
//        System.out.println("updateAttributeName");
//        int attribute = 0;
//        String newName = "";
//        StoreGraph instance = new StoreGraph();
//        instance.updateAttributeName(attribute, newName);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of updateAttributeDescription method, of class StoreGraph.
//     */
//    @Test
//    public void testUpdateAttributeDescription() {
//        System.out.println("updateAttributeDescription");
//        int attribute = 0;
//        String newDescription = "";
//        StoreGraph instance = new StoreGraph();
//        instance.updateAttributeDescription(attribute, newDescription);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of updateAttributeDefaultValue method, of class StoreGraph.
//     */
//    @Test
//    public void testUpdateAttributeDefaultValue() {
//        System.out.println("updateAttributeDefaultValue");
//        int attribute = 0;
//        Object newDefault = null;
//        StoreGraph instance = new StoreGraph();
//        instance.updateAttributeDefaultValue(attribute, newDefault);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of addAttribute method, of class StoreGraph.
//     */
//    @Test
//    public void testAddAttribute() {
//        System.out.println("addAttribute");
//        GraphElementType elementType = null;
//        String attributeType = "";
//        String label = "";
//        String description = "";
//        Object defaultValue = null;
//        String attributeMergerId = "";
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.addAttribute(elementType, attributeType, label, description, defaultValue, attributeMergerId);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of removeAttribute method, of class StoreGraph.
//     */
//    @Test
//    public void testRemoveAttribute() {
//        System.out.println("removeAttribute");
//        int attribute = 0;
//        StoreGraph instance = new StoreGraph();
//        instance.removeAttribute(attribute);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getAttributeCount method, of class StoreGraph.
//     */
//    @Test
//    public void testGetAttributeCount() {
//        System.out.println("getAttributeCount");
//        GraphElementType elementType = null;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getAttributeCount(elementType);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getAttributeCapacity method, of class StoreGraph.
//     */
//    @Test
//    public void testGetAttributeCapacity() {
//        System.out.println("getAttributeCapacity");
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getAttributeCapacity();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getAttribute method, of class StoreGraph.
//     */
//    @Test
//    public void testGetAttribute_GraphElementType_int() {
//        System.out.println("getAttribute");
//        GraphElementType elementType = null;
//        int position = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getAttribute(elementType, position);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getAttribute method, of class StoreGraph.
//     */
//    @Test
//    public void testGetAttribute_GraphElementType_String() {
//        System.out.println("getAttribute");
//        GraphElementType elementType = null;
//        String name = "";
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getAttribute(elementType, name);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getAttributeName method, of class StoreGraph.
//     */
//    @Test
//    public void testGetAttributeName() {
//        System.out.println("getAttributeName");
//        int attribute = 0;
//        StoreGraph instance = new StoreGraph();
//        String expResult = "";
//        String result = instance.getAttributeName(attribute);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getAttributeUID method, of class StoreGraph.
//     */
//    @Test
//    public void testGetAttributeUID() {
//        System.out.println("getAttributeUID");
//        int attribute = 0;
//        StoreGraph instance = new StoreGraph();
//        long expResult = 0L;
//        long result = instance.getAttributeUID(attribute);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getAttributeType method, of class StoreGraph.
//     */
//    @Test
//    public void testGetAttributeType() {
//        System.out.println("getAttributeType");
//        int attribute = 0;
//        StoreGraph instance = new StoreGraph();
//        String expResult = "";
//        String result = instance.getAttributeType(attribute);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getAttributeMerger method, of class StoreGraph.
//     */
//    @Test
//    public void testGetAttributeMerger() {
//        System.out.println("getAttributeMerger");
//        int attribute = 0;
//        StoreGraph instance = new StoreGraph();
//        GraphAttributeMerger expResult = null;
//        GraphAttributeMerger result = instance.getAttributeMerger(attribute);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getAttributeDescription method, of class StoreGraph.
//     */
//    @Test
//    public void testGetAttributeDescription() {
//        System.out.println("getAttributeDescription");
//        int attribute = 0;
//        StoreGraph instance = new StoreGraph();
//        String expResult = "";
//        String result = instance.getAttributeDescription(attribute);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getAttributeElementType method, of class StoreGraph.
//     */
//    @Test
//    public void testGetAttributeElementType() {
//        System.out.println("getAttributeElementType");
//        int attribute = 0;
//        StoreGraph instance = new StoreGraph();
//        GraphElementType expResult = null;
//        GraphElementType result = instance.getAttributeElementType(attribute);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getAttributeDataType method, of class StoreGraph.
//     */
//    @Test
//    public void testGetAttributeDataType() {
//        System.out.println("getAttributeDataType");
//        int attribute = 0;
//        StoreGraph instance = new StoreGraph();
//        Class expResult = null;
//        Class result = instance.getAttributeDataType(attribute);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getAttributeDefaultValue method, of class StoreGraph.
//     */
//    @Test
//    public void testGetAttributeDefaultValue() {
//        System.out.println("getAttributeDefaultValue");
//        int attribute = 0;
//        StoreGraph instance = new StoreGraph();
//        Object expResult = null;
//        Object result = instance.getAttributeDefaultValue(attribute);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getNativeAttributeType method, of class StoreGraph.
//     */
//    @Test
//    public void testGetNativeAttributeType() {
//        System.out.println("getNativeAttributeType");
//        int attribute = 0;
//        StoreGraph instance = new StoreGraph();
//        NativeAttributeType expResult = null;
//        NativeAttributeType result = instance.getNativeAttributeType(attribute);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of createReadAttributeObject method, of class StoreGraph.
//     */
//    @Test
//    public void testCreateReadAttributeObject() {
//        System.out.println("createReadAttributeObject");
//        int attribute = 0;
//        IntReadable indexReadable = null;
//        StoreGraph instance = new StoreGraph();
//        Object expResult = null;
//        Object result = instance.createReadAttributeObject(attribute, indexReadable);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of createWriteAttributeObject method, of class StoreGraph.
//     */
//    @Test
//    public void testCreateWriteAttributeObject() {
//        System.out.println("createWriteAttributeObject");
//        int attribute = 0;
//        IntReadable indexReadable = null;
//        StoreGraph instance = new StoreGraph();
//        Object expResult = null;
//        Object result = instance.createWriteAttributeObject(attribute, indexReadable);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isDefaultValue method, of class StoreGraph.
//     */
//    @Test
//    public void testIsDefaultValue() {
//        System.out.println("isDefaultValue");
//        int attribute = 0;
//        int id = 0;
//        StoreGraph instance = new StoreGraph();
//        boolean expResult = false;
//        boolean result = instance.isDefaultValue(attribute, id);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getByteValue method, of class StoreGraph.
//     */
//    @Test
//    public void testGetByteValue() {
//        System.out.println("getByteValue");
//        int attribute = 0;
//        int id = 0;
//        StoreGraph instance = new StoreGraph();
//        byte expResult = 0;
//        byte result = instance.getByteValue(attribute, id);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getShortValue method, of class StoreGraph.
//     */
//    @Test
//    public void testGetShortValue() {
//        System.out.println("getShortValue");
//        int attribute = 0;
//        int id = 0;
//        StoreGraph instance = new StoreGraph();
//        short expResult = 0;
//        short result = instance.getShortValue(attribute, id);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getIntValue method, of class StoreGraph.
//     */
//    @Test
//    public void testGetIntValue() {
//        System.out.println("getIntValue");
//        int attribute = 0;
//        int id = 0;
//        StoreGraph instance = new StoreGraph();
//        int expResult = 0;
//        int result = instance.getIntValue(attribute, id);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getLongValue method, of class StoreGraph.
//     */
//    @Test
//    public void testGetLongValue() {
//        System.out.println("getLongValue");
//        int attribute = 0;
//        int id = 0;
//        StoreGraph instance = new StoreGraph();
//        long expResult = 0L;
//        long result = instance.getLongValue(attribute, id);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getFloatValue method, of class StoreGraph.
//     */
//    @Test
//    public void testGetFloatValue() {
//        System.out.println("getFloatValue");
//        int attribute = 0;
//        int id = 0;
//        StoreGraph instance = new StoreGraph();
//        float expResult = 0.0F;
//        float result = instance.getFloatValue(attribute, id);
//        assertEquals(result, expResult, 0.0);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getDoubleValue method, of class StoreGraph.
//     */
//    @Test
//    public void testGetDoubleValue() {
//        System.out.println("getDoubleValue");
//        int attribute = 0;
//        int id = 0;
//        StoreGraph instance = new StoreGraph();
//        double expResult = 0.0;
//        double result = instance.getDoubleValue(attribute, id);
//        assertEquals(result, expResult, 0.0);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getBooleanValue method, of class StoreGraph.
//     */
//    @Test
//    public void testGetBooleanValue() {
//        System.out.println("getBooleanValue");
//        int attribute = 0;
//        int id = 0;
//        StoreGraph instance = new StoreGraph();
//        boolean expResult = false;
//        boolean result = instance.getBooleanValue(attribute, id);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getCharValue method, of class StoreGraph.
//     */
//    @Test
//    public void testGetCharValue() {
//        System.out.println("getCharValue");
//        int attribute = 0;
//        int id = 0;
//        StoreGraph instance = new StoreGraph();
//        char expResult = ' ';
//        char result = instance.getCharValue(attribute, id);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getStringValue method, of class StoreGraph.
//     */
//    @Test
//    public void testGetStringValue() {
//        System.out.println("getStringValue");
//        int attribute = 0;
//        int id = 0;
//        StoreGraph instance = new StoreGraph();
//        String expResult = "";
//        String result = instance.getStringValue(attribute, id);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of acceptsStringValue method, of class StoreGraph.
//     */
//    @Test
//    public void testAcceptsStringValue() {
//        System.out.println("acceptsStringValue");
//        int attribute = 0;
//        String value = "";
//        StoreGraph instance = new StoreGraph();
//        String expResult = "";
//        String result = instance.acceptsStringValue(attribute, value);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getObjectValue method, of class StoreGraph.
//     */
//    @Test
//    public void testGetObjectValue() {
//        System.out.println("getObjectValue");
//        int attribute = 0;
//        int id = 0;
//        StoreGraph instance = new StoreGraph();
//        Object expResult = null;
//        Object result = instance.getObjectValue(attribute, id);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isPrimaryKey method, of class StoreGraph.
//     */
//    @Test
//    public void testIsPrimaryKey() {
//        System.out.println("isPrimaryKey");
//        int attribute = 0;
//        StoreGraph instance = new StoreGraph();
//        boolean expResult = false;
//        boolean result = instance.isPrimaryKey(attribute);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of copyAttribute method, of class StoreGraph.
//     */
//    @Test
//    public void testCopyAttribute() {
//        System.out.println("copyAttribute");
//        int attribute = 0;
//        StoreGraph instance = new StoreGraph();
//        Object expResult = null;
//        Object result = instance.copyAttribute(attribute);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of clearValue method, of class StoreGraph.
//     */
//    @Test
//    public void testClearValue() {
//        System.out.println("clearValue");
//        int attribute = 0;
//        int id = 0;
//        StoreGraph instance = new StoreGraph();
//        instance.clearValue(attribute, id);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setByteValue method, of class StoreGraph.
//     */
//    @Test
//    public void testSetByteValue() {
//        System.out.println("setByteValue");
//        int attribute = 0;
//        int id = 0;
//        byte value = 0;
//        StoreGraph instance = new StoreGraph();
//        instance.setByteValue(attribute, id, value);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setShortValue method, of class StoreGraph.
//     */
//    @Test
//    public void testSetShortValue() {
//        System.out.println("setShortValue");
//        int attribute = 0;
//        int id = 0;
//        short value = 0;
//        StoreGraph instance = new StoreGraph();
//        instance.setShortValue(attribute, id, value);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setIntValue method, of class StoreGraph.
//     */
//    @Test
//    public void testSetIntValue() {
//        System.out.println("setIntValue");
//        int attribute = 0;
//        int id = 0;
//        int value = 0;
//        StoreGraph instance = new StoreGraph();
//        instance.setIntValue(attribute, id, value);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setLongValue method, of class StoreGraph.
//     */
//    @Test
//    public void testSetLongValue() {
//        System.out.println("setLongValue");
//        int attribute = 0;
//        int id = 0;
//        long value = 0L;
//        StoreGraph instance = new StoreGraph();
//        instance.setLongValue(attribute, id, value);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setFloatValue method, of class StoreGraph.
//     */
//    @Test
//    public void testSetFloatValue() {
//        System.out.println("setFloatValue");
//        int attribute = 0;
//        int id = 0;
//        float value = 0.0F;
//        StoreGraph instance = new StoreGraph();
//        instance.setFloatValue(attribute, id, value);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setDoubleValue method, of class StoreGraph.
//     */
//    @Test
//    public void testSetDoubleValue() {
//        System.out.println("setDoubleValue");
//        int attribute = 0;
//        int id = 0;
//        double value = 0.0;
//        StoreGraph instance = new StoreGraph();
//        instance.setDoubleValue(attribute, id, value);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setBooleanValue method, of class StoreGraph.
//     */
//    @Test
//    public void testSetBooleanValue() {
//        System.out.println("setBooleanValue");
//        int attribute = 0;
//        int id = 0;
//        boolean value = false;
//        StoreGraph instance = new StoreGraph();
//        instance.setBooleanValue(attribute, id, value);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setCharValue method, of class StoreGraph.
//     */
//    @Test
//    public void testSetCharValue() {
//        System.out.println("setCharValue");
//        int attribute = 0;
//        int id = 0;
//        char value = ' ';
//        StoreGraph instance = new StoreGraph();
//        instance.setCharValue(attribute, id, value);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setStringValue method, of class StoreGraph.
//     */
//    @Test
//    public void testSetStringValue() {
//        System.out.println("setStringValue");
//        int attribute = 0;
//        int id = 0;
//        String value = "";
//        StoreGraph instance = new StoreGraph();
//        instance.setStringValue(attribute, id, value);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setObjectValue method, of class StoreGraph.
//     */
//    @Test
//    public void testSetObjectValue() {
//        System.out.println("setObjectValue");
//        int attribute = 0;
//        int id = 0;
//        Object value = null;
//        StoreGraph instance = new StoreGraph();
//        instance.setObjectValue(attribute, id, value);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setPrimaryKey method, of class StoreGraph.
//     */
//    @Test
//    public void testSetPrimaryKey() {
//        System.out.println("setPrimaryKey");
//        GraphElementType elementType = null;
//        int[] newPrimaryKeys = null;
//        StoreGraph instance = new StoreGraph();
//        instance.setPrimaryKey(elementType, newPrimaryKeys);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getPrimaryKey method, of class StoreGraph.
//     */
//    @Test
//    public void testGetPrimaryKey() {
//        System.out.println("getPrimaryKey");
//        GraphElementType elementType = null;
//        StoreGraph instance = new StoreGraph();
//        int[] expResult = null;
//        int[] result = instance.getPrimaryKey(elementType);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getPrimaryKeyValue method, of class StoreGraph.
//     */
//    @Test
//    public void testGetPrimaryKeyValue() {
//        System.out.println("getPrimaryKeyValue");
//        GraphElementType elementType = null;
//        int id = 0;
//        StoreGraph instance = new StoreGraph();
//        GraphKey expResult = null;
//        GraphKey result = instance.getPrimaryKeyValue(elementType, id);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of toString method, of class StoreGraph.
//     */
//    @Test
//    public void testToString() {
//        System.out.println("toString");
//        StoreGraph instance = new StoreGraph();
//        String expResult = "";
//        String result = instance.toString();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of save method, of class StoreGraph.
//     */
//    @Test
//    public void testSave() {
//        System.out.println("save");
//        int attribute = 0;
//        int id = 0;
//        ParameterWriteAccess access = null;
//        StoreGraph instance = new StoreGraph();
//        instance.save(attribute, id, access);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of restore method, of class StoreGraph.
//     */
//    @Test
//    public void testRestore() {
//        System.out.println("restore");
//        int attribute = 0;
//        int id = 0;
//        ParameterReadAccess access = null;
//        StoreGraph instance = new StoreGraph();
//        instance.restore(attribute, id, access);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of saveData method, of class StoreGraph.
//     */
//    @Test
//    public void testSaveData() {
//        System.out.println("saveData");
//        int attribute = 0;
//        StoreGraph instance = new StoreGraph();
//        Object expResult = null;
//        Object result = instance.saveData(attribute);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of restoreData method, of class StoreGraph.
//     */
//    @Test
//    public void testRestoreData() {
//        System.out.println("restoreData");
//        int attribute = 0;
//        Object savedData = null;
//        StoreGraph instance = new StoreGraph();
//        instance.restoreData(attribute, savedData);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of attributeSupportsIndexType method, of class StoreGraph.
//     */
//    @Test
//    public void testAttributeSupportsIndexType() {
//        System.out.println("attributeSupportsIndexType");
//        int attribute = 0;
//        GraphIndexType indexType = null;
//        StoreGraph instance = new StoreGraph();
//        boolean expResult = false;
//        boolean result = instance.attributeSupportsIndexType(attribute, indexType);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getAttributeIndexType method, of class StoreGraph.
//     */
//    @Test
//    public void testGetAttributeIndexType() {
//        System.out.println("getAttributeIndexType");
//        int attribute = 0;
//        StoreGraph instance = new StoreGraph();
//        GraphIndexType expResult = null;
//        GraphIndexType result = instance.getAttributeIndexType(attribute);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getElementsWithAttributeValue method, of class StoreGraph.
//     */
//    @Test
//    public void testGetElementsWithAttributeValue() {
//        System.out.println("getElementsWithAttributeValue");
//        int attribute = 0;
//        Object value = null;
//        StoreGraph instance = new StoreGraph();
//        GraphIndexResult expResult = null;
//        GraphIndexResult result = instance.getElementsWithAttributeValue(attribute, value);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getElementsWithAttributeValueRange method, of class StoreGraph.
//     */
//    @Test
//    public void testGetElementsWithAttributeValueRange() {
//        System.out.println("getElementsWithAttributeValueRange");
//        int attribute = 0;
//        Object start = null;
//        Object end = null;
//        StoreGraph instance = new StoreGraph();
//        GraphIndexResult expResult = null;
//        GraphIndexResult result = instance.getElementsWithAttributeValueRange(attribute, start, end);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setAttributeIndexType method, of class StoreGraph.
//     */
//    @Test
//    public void testSetAttributeIndexType() {
//        System.out.println("setAttributeIndexType");
//        int attribute = 0;
//        GraphIndexType indexType = null;
//        StoreGraph instance = new StoreGraph();
//        instance.setAttributeIndexType(attribute, indexType);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getAttributeRegistry method, of class StoreGraph.
//     */
//    @Test
//    public void testGetAttributeRegistry() {
//        System.out.println("getAttributeRegistry");
//        StoreGraph instance = new StoreGraph();
//        AttributeRegistry expResult = null;
//        AttributeRegistry result = instance.getAttributeRegistry();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setAttributeRegistry method, of class StoreGraph.
//     */
//    @Test
//    public void testSetAttributeRegistry() {
//        System.out.println("setAttributeRegistry");
//        AttributeRegistry attributeRegistry = null;
//        StoreGraph instance = new StoreGraph();
//        instance.setAttributeRegistry(attributeRegistry);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of vertexStream method, of class StoreGraph.
//     */
//    @Test
//    public void testVertexStream() {
//        System.out.println("vertexStream");
//        StoreGraph instance = new StoreGraph();
//        IntStream expResult = null;
//        IntStream result = instance.vertexStream();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of linkStream method, of class StoreGraph.
//     */
//    @Test
//    public void testLinkStream() {
//        System.out.println("linkStream");
//        StoreGraph instance = new StoreGraph();
//        IntStream expResult = null;
//        IntStream result = instance.linkStream();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of edgeStream method, of class StoreGraph.
//     */
//    @Test
//    public void testEdgeStream() {
//        System.out.println("edgeStream");
//        StoreGraph instance = new StoreGraph();
//        IntStream expResult = null;
//        IntStream result = instance.edgeStream();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of transactionStream method, of class StoreGraph.
//     */
//    @Test
//    public void testTransactionStream() {
//        System.out.println("transactionStream");
//        StoreGraph instance = new StoreGraph();
//        IntStream expResult = null;
//        IntStream result = instance.transactionStream();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
