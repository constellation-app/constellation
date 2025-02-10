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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import static org.mockito.ArgumentMatchers.anyInt;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author serpens24
 */
public class ConnectionModeNGTest {

    // Define mocks
    GraphReadMethods mockGraphReadMethods;
    
    // Define constants for in/out IDs
    final int inId = 1;
    final int inPos = 2;
    final int outId = 23;
    
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
     * Perform reset of all mocks and argument captors to ensure clean test steps.
     */
    public void resetMocking() {
        mockGraphReadMethods = mock(GraphReadMethods.class);
    }
    
    /**
     * Test of getConnection method, of class ConnectionMode.
     */
    @Test
    public void testGetConnection() {
        resetMocking();
        when(mockGraphReadMethods.getLink(anyInt())).thenReturn(outId);
        assertEquals(ConnectionMode.LINK.getConnection(mockGraphReadMethods, inId), outId);
        Mockito.verify(mockGraphReadMethods, times(1)).getLink(inId); 
        Mockito.verify(mockGraphReadMethods, times(0)).getEdge(anyInt());
        Mockito.verify(mockGraphReadMethods, times(0)).getTransaction(anyInt());

        resetMocking();
        when(mockGraphReadMethods.getEdge(anyInt())).thenReturn(outId);
        assertEquals(ConnectionMode.EDGE.getConnection(mockGraphReadMethods, inId), outId);
        Mockito.verify(mockGraphReadMethods, times(0)).getLink(anyInt()); 
        Mockito.verify(mockGraphReadMethods, times(1)).getEdge(inId);
        Mockito.verify(mockGraphReadMethods, times(0)).getTransaction(anyInt());

        resetMocking();
        when(mockGraphReadMethods.getTransaction(anyInt())).thenReturn(outId);
        assertEquals(ConnectionMode.TRANSACTION.getConnection(mockGraphReadMethods, inId), outId);
        Mockito.verify(mockGraphReadMethods, times(0)).getLink(anyInt()); 
        Mockito.verify(mockGraphReadMethods, times(0)).getEdge(anyInt());
        Mockito.verify(mockGraphReadMethods, times(1)).getTransaction(inId);
    }

    /**
     * Test of getConnectionCount method, of class ConnectionMode.
     */
    @Test
    public void testGetConnectionCount() {
        resetMocking();
        when(mockGraphReadMethods.getLinkCount()).thenReturn(outId);
        assertEquals(ConnectionMode.LINK.getConnectionCount(mockGraphReadMethods), outId);
        Mockito.verify(mockGraphReadMethods, times(1)).getLinkCount(); 
        Mockito.verify(mockGraphReadMethods, times(0)).getEdgeCount();
        Mockito.verify(mockGraphReadMethods, times(0)).getTransactionCount();

        resetMocking();
        when(mockGraphReadMethods.getEdgeCount()).thenReturn(outId);
        assertEquals(ConnectionMode.EDGE.getConnectionCount(mockGraphReadMethods), outId);
        Mockito.verify(mockGraphReadMethods, times(0)).getLinkCount(); 
        Mockito.verify(mockGraphReadMethods, times(1)).getEdgeCount();
        Mockito.verify(mockGraphReadMethods, times(0)).getTransactionCount();

        resetMocking();
        when(mockGraphReadMethods.getTransactionCount()).thenReturn(outId);
        assertEquals(ConnectionMode.TRANSACTION.getConnectionCount(mockGraphReadMethods), outId);
        Mockito.verify(mockGraphReadMethods, times(0)).getLinkCount(); 
        Mockito.verify(mockGraphReadMethods, times(0)).getEdgeCount();
        Mockito.verify(mockGraphReadMethods, times(1)).getTransactionCount();
    }

    /**
     * Test of getConnectionCapacity method, of class ConnectionMode.
     */
    @Test
    public void testGetConnectionCapacity() {
        resetMocking();
        when(mockGraphReadMethods.getLinkCapacity()).thenReturn(outId);
        assertEquals(ConnectionMode.LINK.getConnectionCapacity(mockGraphReadMethods), outId);
        Mockito.verify(mockGraphReadMethods, times(1)).getLinkCapacity(); 
        Mockito.verify(mockGraphReadMethods, times(0)).getEdgeCapacity();
        Mockito.verify(mockGraphReadMethods, times(0)).getTransactionCapacity();

        resetMocking();
        when(mockGraphReadMethods.getEdgeCapacity()).thenReturn(outId);
        assertEquals(ConnectionMode.EDGE.getConnectionCapacity(mockGraphReadMethods), outId);
        Mockito.verify(mockGraphReadMethods, times(0)).getLinkCapacity(); 
        Mockito.verify(mockGraphReadMethods, times(1)).getEdgeCapacity();
        Mockito.verify(mockGraphReadMethods, times(0)).getTransactionCapacity();

        resetMocking();
        when(mockGraphReadMethods.getTransactionCapacity()).thenReturn(outId);
        assertEquals(ConnectionMode.TRANSACTION.getConnectionCapacity(mockGraphReadMethods), outId);
        Mockito.verify(mockGraphReadMethods, times(0)).getLinkCapacity(); 
        Mockito.verify(mockGraphReadMethods, times(0)).getEdgeCapacity();
        Mockito.verify(mockGraphReadMethods, times(1)).getTransactionCapacity();
    }

    /**
     * Test of getConnectionSourceVertex method, of class ConnectionMode.
     */
    @Test
    public void testGetConnectionSourceVertex() {
        resetMocking();
        when(mockGraphReadMethods.getLinkLowVertex(anyInt())).thenReturn(outId);
        assertEquals(ConnectionMode.LINK.getConnectionSourceVertex(mockGraphReadMethods, inId), outId);
        Mockito.verify(mockGraphReadMethods, times(1)).getLinkLowVertex(inId); 
        Mockito.verify(mockGraphReadMethods, times(0)).getEdgeSourceVertex(anyInt());
        Mockito.verify(mockGraphReadMethods, times(0)).getTransactionSourceVertex(anyInt());

        resetMocking();
        when(mockGraphReadMethods.getEdgeSourceVertex(anyInt())).thenReturn(outId);
        assertEquals(ConnectionMode.EDGE.getConnectionSourceVertex(mockGraphReadMethods, inId), outId);
        Mockito.verify(mockGraphReadMethods, times(0)).getLinkLowVertex(anyInt()); 
        Mockito.verify(mockGraphReadMethods, times(1)).getEdgeSourceVertex(inId);
        Mockito.verify(mockGraphReadMethods, times(0)).getTransactionSourceVertex(anyInt());

        resetMocking();
        when(mockGraphReadMethods.getTransactionSourceVertex(anyInt())).thenReturn(outId);
        assertEquals(ConnectionMode.TRANSACTION.getConnectionSourceVertex(mockGraphReadMethods, inId), outId);
        Mockito.verify(mockGraphReadMethods, times(0)).getLinkLowVertex(anyInt()); 
        Mockito.verify(mockGraphReadMethods, times(0)).getEdgeSourceVertex(anyInt());
        Mockito.verify(mockGraphReadMethods, times(1)).getTransactionSourceVertex(inId);
    }

    /**
     * Test of getConnectionDestinationVertex method, of class ConnectionMode.
     */
    @Test
    public void testGetConnectionDestinationVertex() {
        resetMocking();
        when(mockGraphReadMethods.getLinkHighVertex(anyInt())).thenReturn(outId);
        assertEquals(ConnectionMode.LINK.getConnectionDestinationVertex(mockGraphReadMethods, inId), outId);
        Mockito.verify(mockGraphReadMethods, times(1)).getLinkHighVertex(inId); 
        Mockito.verify(mockGraphReadMethods, times(0)).getEdgeDestinationVertex(anyInt());
        Mockito.verify(mockGraphReadMethods, times(0)).getTransactionDestinationVertex(anyInt());

        resetMocking();
        when(mockGraphReadMethods.getEdgeDestinationVertex(anyInt())).thenReturn(outId);
        assertEquals(ConnectionMode.EDGE.getConnectionDestinationVertex(mockGraphReadMethods, inId), outId);
        Mockito.verify(mockGraphReadMethods, times(0)).getLinkHighVertex(anyInt()); 
        Mockito.verify(mockGraphReadMethods, times(1)).getEdgeDestinationVertex(inId);
        Mockito.verify(mockGraphReadMethods, times(0)).getTransactionDestinationVertex(anyInt());

        resetMocking();
        when(mockGraphReadMethods.getTransactionDestinationVertex(anyInt())).thenReturn(outId);
        assertEquals(ConnectionMode.TRANSACTION.getConnectionDestinationVertex(mockGraphReadMethods, inId), outId);
        Mockito.verify(mockGraphReadMethods, times(0)).getLinkHighVertex(anyInt()); 
        Mockito.verify(mockGraphReadMethods, times(0)).getEdgeDestinationVertex(anyInt());
        Mockito.verify(mockGraphReadMethods, times(1)).getTransactionDestinationVertex(inId);
    }

    /**
     * Test of getConnectionCountPerLink method, of class ConnectionMode.
     */
    @Test
    public void testGetConnectionCountPerLink() {
        resetMocking();
        assertEquals(ConnectionMode.LINK.getConnectionCountPerLink(mockGraphReadMethods, inId), 1);
        Mockito.verify(mockGraphReadMethods, times(0)).getEdgeDestinationVertex(anyInt());
        Mockito.verify(mockGraphReadMethods, times(0)).getLinkTransactionCount(anyInt());

        resetMocking();
        when(mockGraphReadMethods.getLinkEdgeCount(anyInt())).thenReturn(outId);
        assertEquals(ConnectionMode.EDGE.getConnectionCountPerLink(mockGraphReadMethods, inId), outId);
        Mockito.verify(mockGraphReadMethods, times(1)).getLinkEdgeCount(inId);
        Mockito.verify(mockGraphReadMethods, times(0)).getLinkTransactionCount(anyInt());

        resetMocking();
        when(mockGraphReadMethods.getLinkTransactionCount(anyInt())).thenReturn(outId);
        assertEquals(ConnectionMode.TRANSACTION.getConnectionCountPerLink(mockGraphReadMethods, inId), outId);
        Mockito.verify(mockGraphReadMethods, times(0)).getLinkEdgeCount(anyInt());
        Mockito.verify(mockGraphReadMethods, times(1)).getLinkTransactionCount(inId);
    }

    /**
     * Test of getConnectionLink method, of class ConnectionMode.
     */
    @Test
    public void testGetConnectionLink() {
        resetMocking();
        assertEquals(ConnectionMode.LINK.getConnectionLink(mockGraphReadMethods, inId), inId);
        Mockito.verify(mockGraphReadMethods, times(0)).getEdgeLink(anyInt());
        Mockito.verify(mockGraphReadMethods, times(0)).getTransactionLink(anyInt());

        resetMocking();
        when(mockGraphReadMethods.getEdgeLink(anyInt())).thenReturn(outId);
        assertEquals(ConnectionMode.EDGE.getConnectionLink(mockGraphReadMethods, inId), outId);
        Mockito.verify(mockGraphReadMethods, times(1)).getEdgeLink(inId);
        Mockito.verify(mockGraphReadMethods, times(0)).getTransactionLink(anyInt());

        resetMocking();
        when(mockGraphReadMethods.getTransactionLink(anyInt())).thenReturn(outId);
        assertEquals(ConnectionMode.TRANSACTION.getConnectionLink(mockGraphReadMethods, inId), outId);
        Mockito.verify(mockGraphReadMethods, times(0)).getEdgeLink(anyInt());
        Mockito.verify(mockGraphReadMethods, times(1)).getTransactionLink(inId);
    }

    /**
     * Test of getFirstTransaction method, of class ConnectionMode.
     */
    @Test
    public void testGetFirstTransaction() {
        resetMocking();
        when(mockGraphReadMethods.getLinkTransaction(anyInt(), anyInt())).thenReturn(outId);
        assertEquals(ConnectionMode.LINK.getFirstTransaction(mockGraphReadMethods, inId), outId);
        Mockito.verify(mockGraphReadMethods, times(1)).getLinkTransaction(inId, 0); 
        Mockito.verify(mockGraphReadMethods, times(0)).getEdgeTransaction(anyInt(), anyInt());

        resetMocking();
        when(mockGraphReadMethods.getEdgeTransaction(anyInt(), anyInt())).thenReturn(outId);
        assertEquals(ConnectionMode.EDGE.getFirstTransaction(mockGraphReadMethods, inId), outId);
        Mockito.verify(mockGraphReadMethods, times(0)).getLinkTransaction(anyInt(), anyInt());
        Mockito.verify(mockGraphReadMethods, times(1)).getEdgeTransaction(inId, 0);

        resetMocking();
        assertEquals(ConnectionMode.TRANSACTION.getFirstTransaction(mockGraphReadMethods, inId), inId);
        Mockito.verify(mockGraphReadMethods, times(0)).getLinkTransaction(anyInt(), anyInt());
        Mockito.verify(mockGraphReadMethods, times(0)).getEdgeTransaction(anyInt(), anyInt());
    }

    /**
     * Test of getVertexConnectionCount method, of class ConnectionMode.
     */
    @Test
    public void testGetVertexConnectionCount() {
        resetMocking();
        when(mockGraphReadMethods.getVertexLinkCount(anyInt())).thenReturn(outId);
        assertEquals(ConnectionMode.LINK.getVertexConnectionCount(mockGraphReadMethods, inId), outId);
        Mockito.verify(mockGraphReadMethods, times(1)).getVertexLinkCount(inId); 
        Mockito.verify(mockGraphReadMethods, times(0)).getVertexEdgeCount(anyInt());
        Mockito.verify(mockGraphReadMethods, times(0)).getVertexTransactionCount(anyInt());

        resetMocking();
        when(mockGraphReadMethods.getVertexEdgeCount(anyInt())).thenReturn(outId);
        assertEquals(ConnectionMode.EDGE.getVertexConnectionCount(mockGraphReadMethods, inId), outId);
        Mockito.verify(mockGraphReadMethods, times(0)).getVertexLinkCount(anyInt()); 
        Mockito.verify(mockGraphReadMethods, times(1)).getVertexEdgeCount(inId);
        Mockito.verify(mockGraphReadMethods, times(0)).getVertexTransactionCount(anyInt());

        resetMocking();
        when(mockGraphReadMethods.getVertexTransactionCount(anyInt())).thenReturn(outId);
        assertEquals(ConnectionMode.TRANSACTION.getVertexConnectionCount(mockGraphReadMethods, inId), outId);
        Mockito.verify(mockGraphReadMethods, times(0)).getVertexLinkCount(anyInt()); 
        Mockito.verify(mockGraphReadMethods, times(0)).getVertexEdgeCount(anyInt());
        Mockito.verify(mockGraphReadMethods, times(1)).getVertexTransactionCount(inId);
    }

    /**
     * Test of getVertexConnection method, of class ConnectionMode.
     */
    @Test
    public void testGetVertexConnection() {
        resetMocking();
        when(mockGraphReadMethods.getVertexLink(anyInt(), anyInt())).thenReturn(outId);
        assertEquals(ConnectionMode.LINK.getVertexConnection(mockGraphReadMethods, inId, inPos), outId);
        Mockito.verify(mockGraphReadMethods, times(1)).getVertexLink(inId, inPos); 
        Mockito.verify(mockGraphReadMethods, times(0)).getVertexEdge(anyInt(), anyInt());
        Mockito.verify(mockGraphReadMethods, times(0)).getVertexTransaction(anyInt(), anyInt());

        resetMocking();
        when(mockGraphReadMethods.getVertexEdge(anyInt(), anyInt())).thenReturn(outId);
        assertEquals(ConnectionMode.EDGE.getVertexConnection(mockGraphReadMethods, inId, inPos), outId);
        Mockito.verify(mockGraphReadMethods, times(0)).getVertexLink(anyInt(), anyInt()); 
        Mockito.verify(mockGraphReadMethods, times(1)).getVertexEdge(inId, inPos);
        Mockito.verify(mockGraphReadMethods, times(0)).getVertexTransaction(anyInt(), anyInt());

        resetMocking();
        when(mockGraphReadMethods.getVertexTransaction(anyInt(), anyInt())).thenReturn(outId);
        assertEquals(ConnectionMode.TRANSACTION.getVertexConnection(mockGraphReadMethods, inId, inPos), outId);
        Mockito.verify(mockGraphReadMethods, times(0)).getVertexLink(anyInt(), anyInt()); 
        Mockito.verify(mockGraphReadMethods, times(0)).getVertexEdge(anyInt(), anyInt());
        Mockito.verify(mockGraphReadMethods, times(1)).getVertexTransaction(inId, inPos);
    }
}
