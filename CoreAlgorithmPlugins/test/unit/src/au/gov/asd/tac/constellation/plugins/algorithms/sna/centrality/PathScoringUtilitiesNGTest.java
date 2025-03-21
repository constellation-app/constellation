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
package au.gov.asd.tac.constellation.plugins.algorithms.sna.centrality;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import java.util.Arrays;
import java.util.BitSet;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class PathScoringUtilitiesNGTest {

    private StoreGraph graph;

    private StoreGraph bigGraph;
    private static final int NUM_NODES = 20;

    @BeforeMethod
    public void setUpMethod() throws Exception {
        /////// Small graph
        // create an analytic graph
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        graph = new StoreGraph(schema);

        // add attributes
        VisualConcept.VertexAttribute.SELECTED.ensure(graph);

        // add vertices
        final int vxId0 = graph.addVertex();
        final int vxId1 = graph.addVertex();
        graph.addVertex(); // vxId2 is on its own
        final int vxId3 = graph.addVertex();
        final int vxId4 = graph.addVertex();

        // add transactions
        graph.addTransaction(vxId0, vxId1, true);
        graph.addTransaction(vxId1, vxId3, true);
        graph.addTransaction(vxId3, vxId4, true);
        graph.addTransaction(vxId4, vxId0, true);

        /////// Big graph
        final Schema schema2 = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        bigGraph = new StoreGraph(schema2);

        // add attributes
        VisualConcept.VertexAttribute.SELECTED.ensure(bigGraph);

        // Set up nodes
        for (int i = 0; i < NUM_NODES; i++) {
            bigGraph.addVertex();
        }

        // Set up transactions
        bigGraph.addTransaction(0, 4, true);
        bigGraph.addTransaction(1, 7, true);
        bigGraph.addTransaction(3, 15, true);
        bigGraph.addTransaction(4, 14, true);
        bigGraph.addTransaction(4, 8, true);

        bigGraph.addTransaction(7, 10, true);
        bigGraph.addTransaction(8, 12, true);
        bigGraph.addTransaction(10, 13, true);
        bigGraph.addTransaction(11, 19, true);
        bigGraph.addTransaction(12, 16, true);

        bigGraph.addTransaction(13, 17, true);
        bigGraph.addTransaction(13, 1, true);
        bigGraph.addTransaction(17, 6, true);
        bigGraph.addTransaction(17, 19, true);
        bigGraph.addTransaction(19, 8, true);

    }

    /**
     * Test of computeShortestPathsDirected method, of class PathScoringUtilities.
     */
    @Test
    public void testComputeShortestPathsDirected() {
        System.out.println("computeShortestPathsDirected");

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

        // BIG GRAPH
        // Set up expected expected bit set array
        final BitSet[] expectedBitSetsBig = {
            BitSet.valueOf(new long[]{0b10101000100010001}),
            BitSet.valueOf(new long[]{0b10110011010111000010}),
            null,
            BitSet.valueOf(new long[]{0b00001000000000001000}),
            BitSet.valueOf(new long[]{0b00010101000100010001}),
            null,
            BitSet.valueOf(new long[]{0b00100010010011000010}),
            BitSet.valueOf(new long[]{0b10110011010111000010}),
            BitSet.valueOf(new long[]{0b10110011110110010011}),
            null,
            BitSet.valueOf(new long[]{0b10110011010111000010}),
            BitSet.valueOf(new long[]{0b10010001100100000000}),
            BitSet.valueOf(new long[]{0b10110011110110010011}),
            BitSet.valueOf(new long[]{0b10110011010111000010}),
            BitSet.valueOf(new long[]{0b00000100000000010001}),
            BitSet.valueOf(new long[]{0b00001000000000001000}),
            BitSet.valueOf(new long[]{0b10110011110110010011}),
            BitSet.valueOf(new long[]{0b10110011010111000010}),
            null,
            BitSet.valueOf(new long[]{0b10110011110110000010})};

        // Set up expected float array
        final float[] expectedFloatsbig = {0.0F, 3.0F, 0.0F, 0.0F, 4.0F, 0.0F, 0.0F, 9.0F, 18.0F, 0.0F, 15.0F, 0.0F, 10.0F, 21.0F, 0.0F, 0.0F, 0.0F, 20.0F, 0.0F, 18.0F};

        final Tuple resultBig = PathScoringUtilities.computeShortestPathsDirected(bigGraph, scoreType, includeConnectionsIn, includeConnectionsOut, treatUndirectedBidirectional, selectedOnly);

        assertTrue(Arrays.equals((BitSet[]) resultBig.getFirst(), expectedBitSetsBig));
        assertTrue(Arrays.equals((float[]) resultBig.getSecond(), expectedFloatsbig));
    }

    /**
     * Test of computeShortestPathsUndirected method, of class PathScoringUtilities.
     */
    @Test
    public void testComputeShortestPathsUndirected() {
        System.out.println("computeShortestPathsUndirected");

        final PathScoringUtilities.ScoreType scoreType = PathScoringUtilities.ScoreType.BETWEENNESS;
        final boolean selectedOnly = false;

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

        // BIG GRAPH
        // Set up expected float array
        final float[] expectedFloatsbig = {0.0F, 22.0F, 0.0F, 0.0F, 60.0F, 0.0F, 0.0F, 2.0F, 92.0F, 0.0F, 22.0F, 0.0F, 52.0F, 62.0F, 0.0F, 0.0F, 0.0F, 88.0F, 0.0F, 100.0F};

        final Tuple resultBig = PathScoringUtilities.computeShortestPathsUndirected(bigGraph, scoreType, selectedOnly);

        assertTrue(Arrays.equals((float[]) resultBig.getSecond(), expectedFloatsbig));
    }

    /**
     * Test of computeAllPathsUndirected method, of class PathScoringUtilities.
     */
    @Test
    public void testComputeAllPathsUndirected() {
        System.out.println("computeAllPathsUndirected");

        final PathScoringUtilities.ScoreType scoreType = PathScoringUtilities.ScoreType.ECCENTRICITY;

        // Set up expected expected bit set array
        final BitSet expectedBitSetA = BitSet.valueOf(new long[]{0b11011});
        final BitSet expectedBitSetB = null;

        final BitSet[] expectedBitSets = {expectedBitSetA, expectedBitSetA, expectedBitSetB, expectedBitSetA, expectedBitSetA};
        // Set up expected float array
        final float[] expectedFloats = {2.0F, 2.0F, 0.0F, 2.0F, 2.0F};

        // Run function
        final Tuple result = PathScoringUtilities.computeAllPathsUndirected(graph, scoreType);

        // Assert results equal
        assertTrue(Arrays.equals((BitSet[]) result.getFirst(), expectedBitSets));
        assertTrue(Arrays.equals((float[]) result.getSecond(), expectedFloats));

        // BIG GRAPH
        // Set up expected float array
        final float[] expectedFloatsbig = {7.0F, 6.0F, 0.0F, 1.0F, 6.0F, 0.0F, 5.0F, 7.0F, 5.0F, 0.0F, 6.0F, 5.0F, 6.0F, 5.0F, 7.0F, 1.0F, 7.0F, 4.0F, 0.0F, 4.0F};

        final Tuple resultBig = PathScoringUtilities.computeAllPathsUndirected(bigGraph, scoreType);

        assertTrue(Arrays.equals((float[]) resultBig.getSecond(), expectedFloatsbig));
    }

    /**
     * Test of computeAllPathsDirected method, of class PathScoringUtilities.
     */
    @Test
    public void testComputeAllPathsDirected() {
        System.out.println("computeAllPathsDirected");

        final PathScoringUtilities.ScoreType scoreType = PathScoringUtilities.ScoreType.ECCENTRICITY;

        // Set up expected expected bit set array
        final BitSet expectedBitSetA = BitSet.valueOf(new long[]{0b11011});
        final BitSet expectedBitSetB = null;

        final BitSet[] expectedBitSets = {expectedBitSetA, expectedBitSetA, expectedBitSetB, expectedBitSetA, expectedBitSetA};
        // Set up expected float array
        final float[] expectedFloats = {3.0F, 3.0F, 0.0F, 3.0F, 3.0F};

        // Run function
        final Tuple result = PathScoringUtilities.computeAllPathsDirected(graph, scoreType, false, true, false);

        // Assert results equal
        assertTrue(Arrays.equals((BitSet[]) result.getFirst(), expectedBitSets));
        assertTrue(Arrays.equals((float[]) result.getSecond(), expectedFloats));

        // BIG GRAPH
        // Set up expected float array
        final float[] expectedFloatsbig = {4.0F, 8.0F, 0.0F, 1.0F, 3.0F, 0.0F, 0.0F, 7.0F, 2.0F, 0.0F, 6.0F, 4.0F, 1.0F, 5.0F, 0.0F, 0.0F, 0.0F, 4.0F, 0.0F, 3.0F};

        final Tuple resultBig = PathScoringUtilities.computeAllPathsDirected(bigGraph, scoreType, false, true, false);

        assertTrue(Arrays.equals((float[]) resultBig.getSecond(), expectedFloatsbig));
    }

    /**
     * Test of computeAllPathsDirected method, of class PathScoringUtilities.
     */
    @Test
    public void testComputeAllPathsDirectedAverageDistance() {
        System.out.println("computeAllPathsDirectedAverageDistance");

        final PathScoringUtilities.ScoreType scoreType = PathScoringUtilities.ScoreType.AVERAGE_DISTANCE;

        // Set up expected expected bit set array
        final BitSet expectedBitSetA = BitSet.valueOf(new long[]{0b11011});
        final BitSet expectedBitSetB = null;

        final BitSet[] expectedBitSets = {expectedBitSetA, expectedBitSetA, expectedBitSetB, expectedBitSetA, expectedBitSetA};
        // Set up expected float array
        final float[] expectedFloats = {1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 2.0F, 2.0F, 2.0F};

        // Run function
        final Tuple result = PathScoringUtilities.calculateScores(graph, scoreType, true, true, true, false);

        // Assert results equal
        assertTrue(Arrays.equals((BitSet[]) result.getFirst(), expectedBitSets));
        assertTrue(Arrays.equals((float[]) result.getSecond(), expectedFloats));

        // BIG GRAPH
        // Set up expected float array
        final float[] expectedFloatsbig = {1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 2.0F, 2.0F, 2.0F, 2.0F, 2.0F, 2.0F, 2.0F, 2.0F, 3.0F, 3.0F, 3.0F, 3.0F, 4.0F};

        final Tuple resultBig = PathScoringUtilities.computeAllPathsDirected(bigGraph, scoreType, false, true, false);

        assertTrue(Arrays.equals((float[]) resultBig.getSecond(), expectedFloatsbig));
    }

}
