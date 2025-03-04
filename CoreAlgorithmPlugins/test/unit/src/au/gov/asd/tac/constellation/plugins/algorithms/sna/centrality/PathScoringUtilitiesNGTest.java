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
package au.gov.asd.tac.constellation.plugins.algorithms.sna.centrality;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import java.util.Arrays;
import java.util.BitSet;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class PathScoringUtilitiesNGTest {

    /**
     * Test of computeShortestPathsDirected method, of class PathScoringUtilities.
     */
    @Test
    public void testComputeShortestPathsDirected() {
        System.out.println("computeShortestPathsDirected");

        final int vertexCount = 5;

        // Set up mock graph
        final GraphReadMethods graph = mockGraphHelper(vertexCount);

        final PathScoringUtilities.ScoreType scoreType = PathScoringUtilities.ScoreType.BETWEENNESS;
        final boolean includeConnectionsIn = false;
        final boolean includeConnectionsOut = true;
        final boolean treatUndirectedBidirectional = true;
        final boolean selectedOnly = false;

        // Set up expected expected bit set array
        final BitSet expectedBitSetA = BitSet.valueOf(new long[]{0b11011});
        final BitSet expectedBitSetB = null;
        final BitSet[] expectedBitSets = {expectedBitSetA, expectedBitSetA, expectedBitSetB, expectedBitSetA, expectedBitSetA};
        // Set up expected float array
        final float[] expectedFloats = {3.0F, 3.0F, 0.0F, 3.0F, 3.0F};

        // Run function
        final Tuple result = PathScoringUtilities.computeShortestPathsDirected(graph, scoreType, includeConnectionsIn, includeConnectionsOut, treatUndirectedBidirectional, selectedOnly);

        // Assert results equal
        assertTrue(Arrays.equals((BitSet[]) result.getFirst(), expectedBitSets));
        assertTrue(Arrays.equals((float[]) result.getSecond(), expectedFloats));
    }

    /**
     * Test of computeShortestPathsUndirected method, of class PathScoringUtilities.
     */
    @Test
    public void testComputeShortestPathsUndirected() {
        System.out.println("computeShortestPathsUndirected");

        final PathScoringUtilities.ScoreType scoreType = PathScoringUtilities.ScoreType.BETWEENNESS;
        final boolean selectedOnly = false;

        final int vertexCount = 5;

        // Set up mock graph
        final GraphReadMethods graph = mockGraphHelper(vertexCount);

        // Set up expected expected bit set array
        final BitSet expectedBitSetA = BitSet.valueOf(new long[]{0b11011});
        final BitSet expectedBitSetB = null;
        final BitSet[] expectedBitSets = {expectedBitSetA, expectedBitSetA, expectedBitSetB, expectedBitSetA, expectedBitSetA};
        // Set up expected float array
        final float[] expectedFloats = {2.0F, 2.0F, 0.0F, 2.0F, 2.0F};

        // Run function
        final Tuple result = PathScoringUtilities.computeShortestPathsUndirected(graph, scoreType, selectedOnly);

        // Assert results equal
        assertTrue(Arrays.equals((BitSet[]) result.getFirst(), expectedBitSets));
        assertTrue(Arrays.equals((float[]) result.getSecond(), expectedFloats));
    }

    private GraphReadMethods mockGraphHelper(final int numVertices) {
        // Set up mock graph
        final GraphReadMethods graph = mock(GraphReadMethods.class);
        when(graph.getVertexCount()).thenReturn(numVertices);

        // Set up getting vertex from position and getting neighbour count
        for (int i = 0; i < 5; i++) {
            final int vId = i * 101;

            when(graph.getVertexNeighbourCount(vId)).thenReturn(2);
            when(graph.getVertex(i)).thenReturn(vId);
            when(graph.getVertexPosition(vId)).thenReturn(i);
        }

        // Specifaccly say vertex 2 has no neighbours
        when(graph.getVertexNeighbourCount(202)).thenReturn(0);

        // Set up cyclic neighbours
        when(graph.getVertexNeighbour(0, 0)).thenReturn(101);
        when(graph.getVertexNeighbour(0, 1)).thenReturn(404);

        when(graph.getVertexNeighbour(101, 0)).thenReturn(303);
        when(graph.getVertexNeighbour(101, 1)).thenReturn(0);

        when(graph.getVertexNeighbour(303, 0)).thenReturn(404);
        when(graph.getVertexNeighbour(303, 1)).thenReturn(101);

        when(graph.getVertexNeighbour(404, 0)).thenReturn(0);
        when(graph.getVertexNeighbour(404, 1)).thenReturn(303);

        // Get link
        when(graph.getLink(0, 101)).thenReturn(0);
        when(graph.getLink(101, 0)).thenReturn(0);

        when(graph.getLink(101, 303)).thenReturn(1);
        when(graph.getLink(303, 101)).thenReturn(1);

        when(graph.getLink(303, 404)).thenReturn(2);
        when(graph.getLink(404, 303)).thenReturn(2);

        when(graph.getLink(404, 0)).thenReturn(3);
        when(graph.getLink(0, 404)).thenReturn(3);

        // Get Link Edge Count
        when(graph.getLinkEdgeCount(anyInt())).thenReturn(1);

        // Get Link Edge
        when(graph.getLinkEdge(0, 0)).thenReturn(0);
        when(graph.getLinkEdge(1, 0)).thenReturn(1);
        when(graph.getLinkEdge(2, 0)).thenReturn(2);
        when(graph.getLinkEdge(3, 0)).thenReturn(3);

        // Get Edge Direction
        when(graph.getEdgeDirection(0)).thenReturn(0);
        when(graph.getEdgeDirection(1)).thenReturn(0);
        when(graph.getEdgeDirection(2)).thenReturn(0);

        when(graph.getEdgeDirection(3)).thenReturn(1);

        // Get Edge Destination Vertex
        when(graph.getEdgeDestinationVertex(3)).thenReturn(0);
        when(graph.getEdgeDestinationVertex(0)).thenReturn(101);
        when(graph.getEdgeDestinationVertex(1)).thenReturn(303);
        when(graph.getEdgeDestinationVertex(2)).thenReturn(404);

        when(graph.getEdgeSourceVertex(0)).thenReturn(0);
        when(graph.getEdgeSourceVertex(1)).thenReturn(101);
        when(graph.getEdgeSourceVertex(2)).thenReturn(303);
        when(graph.getEdgeSourceVertex(3)).thenReturn(404);

        return graph;
    }

}
