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

import au.gov.asd.tac.constellation.graph.GraphConstants;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.utilities.datastructure.ThreeTuple;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import java.util.ArrayList;
import java.util.BitSet;

/**
 * Utilities for calculating scores on a graph based on shortest paths. This utility makes use of the parallel breadth
 * first search algorithm to efficiently traverse the graph.
 *
 * @author canis_majoris
 * @author cygnus_x-1
 */
public class PathScoringUtilities {

    private static final String SCORETYPE_ERROR_FORMAT = "The requested ScoreType, %s, is not supported.";
    private static final String OUT_OF_BOUNDS_EXCEPTION_STRING = "The 'selected' attribute does not exist on the given graph.";

    public enum ScoreType {

        AVERAGE_DISTANCE(false),
        ECCENTRICITY(false),
        BETWEENNESS(true),
        CLOSENESS(true),
        FARNESS(true),
        HARMONIC_CLOSENESS(true),
        HARMONIC_FARNESS(true);

        private final boolean requiresShortestPaths;

        private ScoreType(final boolean shortestPaths) {
            this.requiresShortestPaths = shortestPaths;
        }

        public boolean requiresShortestPaths() {
            return requiresShortestPaths;
        }
    }

    public static int subgraphSize(final GraphReadMethods graph, final BitSet subgraph, final boolean selectedOnly) {
        int selectedCount = 0;
        final int selectedAttributeId = VisualConcept.VertexAttribute.SELECTED.get(graph);

        if (subgraph == null) {
            final int vertexCount = graph.getVertexCount();
            for (int vxPosition = 0; vxPosition < vertexCount; vxPosition++) {
                int vxId = graph.getVertex(vxPosition);
                if (!selectedOnly || graph.getBooleanValue(selectedAttributeId, vxId)) {
                    selectedCount++;
                }
            }
        } else {
            for (int vxPosition = subgraph.nextSetBit(0); vxPosition >= 0; vxPosition = subgraph.nextSetBit(vxPosition + 1)) {
                int vxId = graph.getVertex(vxPosition);
                if (!selectedOnly || (graph.getBooleanValue(selectedAttributeId, vxId))) {
                    selectedCount++;
                }
            }
        }

        return selectedCount;
    }

    public static Tuple<BitSet[], float[]> calculateScores(final GraphReadMethods graph, final ScoreType scoreType,
            final boolean includeConnectionsIn, final boolean includeConnectionsOut, final boolean treatUndirectedBidirectional, final boolean selectedOnly) {
        if (includeConnectionsIn && includeConnectionsOut) {
            return scoreType.requiresShortestPaths() ? computeShortestPathsUndirected(graph, scoreType, selectedOnly) : computeAllPathsUndirected(graph, scoreType);
        } else {
            return scoreType.requiresShortestPaths() ? computeShortestPathsDirected(graph, scoreType, includeConnectionsIn, includeConnectionsOut, treatUndirectedBidirectional, selectedOnly) : computeAllPathsDirected(graph, scoreType, includeConnectionsIn, includeConnectionsOut, treatUndirectedBidirectional);
        }
    }

    public static BitSet[] calculateSubgraphPaths(final GraphReadMethods graph, final BitSet subgraph,
            final boolean includeConnectionsIn, final boolean includeConnectionsOut, final boolean treatUndirectedBidirectional) {
        if (includeConnectionsIn && includeConnectionsOut) {
            return computeSubgraphPathsUndirected(graph, subgraph);
        } else {
            return computeSubgraphPathsDirected(graph, subgraph, includeConnectionsIn, includeConnectionsOut, treatUndirectedBidirectional);
        }
    }

    private static BitSet[] computeSubgraphPathsUndirected(final GraphReadMethods graph, final BitSet subgraph) {
        final int vxCount = graph.getVertexCount();
        final BitSet[] traversal = new BitSet[vxCount];

        final BitSet update = new BitSet(vxCount);
        final BitSet[] sendBuffer = new BitSet[vxCount];
        final BitSet turn = new BitSet(vxCount);
        final BitSet newUpdate = new BitSet(vxCount);

        // initialising variables
        for (int vxPosition = 0; vxPosition < vxCount; vxPosition++) {
            traversal[vxPosition] = new BitSet(vxCount);
            sendBuffer[vxPosition] = new BitSet(vxCount);
        }
        update.or(subgraph);

        while (!update.isEmpty()) {

            // each node with messages needs to update its own information
            for (int vxPosition = update.nextSetBit(0); vxPosition >= 0; vxPosition = update.nextSetBit(vxPosition + 1)) {
                traversal[vxPosition].or(sendBuffer[vxPosition]);
                traversal[vxPosition].set(vxPosition);
                sendBuffer[vxPosition].clear();
            }

            // for each neighbour, check if there is any new information it needs to receive
            for (int sourcePosition = update.nextSetBit(0); sourcePosition >= 0; sourcePosition = update.nextSetBit(sourcePosition + 1)) {
                final int vxId = graph.getVertex(sourcePosition);
                for (int neighbourPosition = 0; neighbourPosition < graph.getVertexNeighbourCount(vxId); neighbourPosition++) {
                    final int destinationPosition = graph.getVertexPosition(graph.getVertexNeighbour(vxId, neighbourPosition));
                    if (!subgraph.get(destinationPosition)) {
                        continue;
                    }
                    if (!traversal[sourcePosition].equals(traversal[destinationPosition])) {

                        final BitSet diff = (BitSet) traversal[sourcePosition].clone();
                        diff.andNot(traversal[destinationPosition]);

                        sendBuffer[destinationPosition].or(diff);
                        turn.or(sendBuffer[destinationPosition]);
                        newUpdate.set(destinationPosition);
                    }
                }
            }

            turn.clear();

            update.clear();
            update.or(newUpdate);
            newUpdate.clear();
        }

        return traversal;
    }

    private static BitSet[] computeSubgraphPathsDirected(final GraphReadMethods graph, final BitSet subgraph,
            final boolean includeConnectionsIn, final boolean includeConnectionsOut, final boolean treatUndirectedBidirectional) {
        final int vertexCount = graph.getVertexCount();
        final BitSet[] traversal = new BitSet[vertexCount];

        final BitSet update = new BitSet(vertexCount);
        final BitSet[] sendBuffer = new BitSet[vertexCount];
        final BitSet newUpdate = new BitSet(vertexCount);

        final BitSet turn = new BitSet(vertexCount);

        // initialising variables
        for (int vxPosition = 0; vxPosition < vertexCount; vxPosition++) {
            traversal[vxPosition] = new BitSet(vertexCount);
            sendBuffer[vxPosition] = new BitSet(vertexCount);
        }
        update.or(subgraph);

        while (!update.isEmpty()) {

            // update the information of each node with messages
            for (int vertexPosition = update.nextSetBit(0); vertexPosition >= 0; vertexPosition = update.nextSetBit(vertexPosition + 1)) {
                traversal[vertexPosition].or(sendBuffer[vertexPosition]);
                traversal[vertexPosition].set(vertexPosition);
                sendBuffer[vertexPosition].clear();
            }

            // for each neighbour, check if there is any new information it needs to receive
            for (int vertexPosition = update.nextSetBit(0); vertexPosition >= 0; vertexPosition = update.nextSetBit(vertexPosition + 1)) {
                int vertexId = graph.getVertex(vertexPosition);

                for (int vertexNeighbourPosition = 0; vertexNeighbourPosition < graph.getVertexNeighbourCount(vertexId); vertexNeighbourPosition++) {
                    final int neighbourId = graph.getVertexNeighbour(vertexId, vertexNeighbourPosition);
                    final int neighbourPosition = graph.getVertexPosition(neighbourId);
                    if (!subgraph.get(neighbourPosition)) {
                        continue;
                    }

                    boolean isRequestedDirection = false;
                    final int linkId = graph.getLink(vertexId, neighbourId);
                    for (int linkEdgePosition = 0; linkEdgePosition < graph.getLinkEdgeCount(linkId); linkEdgePosition++) {
                        final int edgeId = graph.getLinkEdge(linkId, linkEdgePosition);
                        final int edgeDirection = graph.getEdgeDirection(edgeId);
                        isRequestedDirection = (treatUndirectedBidirectional && edgeDirection == GraphConstants.UNDIRECTED)
                                || (includeConnectionsIn && graph.getEdgeSourceVertex(edgeId) == neighbourId)
                                || (includeConnectionsOut && graph.getEdgeDestinationVertex(edgeId) == neighbourId);
                        if (isRequestedDirection) {
                            break;
                        }
                    }

                    if (isRequestedDirection && !traversal[vertexPosition].equals(traversal[neighbourPosition])) {
                        turn.set(neighbourPosition, true);

                        final BitSet diff = (BitSet) traversal[vertexPosition].clone();
                        diff.andNot(traversal[neighbourPosition]);
                        sendBuffer[neighbourPosition].or(diff);
                        newUpdate.set(neighbourPosition);
                    }
                }
            }

            turn.clear();

            update.clear();
            update.or(newUpdate);
            newUpdate.clear();
        }

        return traversal;
    }

    protected static Tuple<BitSet[], float[]> computeAllPathsUndirected(final GraphReadMethods graph, final ScoreType scoreType) {
        // Run the algorithm
        final ThreeTuple<BitSet[], float[], BitSet> result = computeAllPathsUndirectedThreeTuple(graph, scoreType);

        // Convert results into a two tuple
        return convertThreeTupleResultsToTwo(result, graph.getVertexCount(), scoreType);
    }

    protected static ThreeTuple<BitSet[], float[], BitSet> computeAllPathsUndirectedThreeTuple(final GraphReadMethods graph, final ScoreType scoreType) {
        final int vxCount = graph.getVertexCount();

        final ArrayList<Float> distances = new ArrayList<>();

        final BitSet update = new BitSet(vxCount);
        final BitSet updateToReturn = new BitSet(vxCount);
        final BitSet turn = new BitSet(vxCount);
        final BitSet newUpdate = new BitSet(vxCount);

        final ArrayList<Integer> updatedVertexIndexArray = new ArrayList<>();

        // initialising variables
        for (int vxPosition = 0; vxPosition < vxCount; vxPosition++) {
            // assuming the node has neighbours
            if (graph.getVertexNeighbourCount(graph.getVertex(vxPosition)) > 0) {
                update.set(vxPosition);
                updateToReturn.set(vxPosition);
                updatedVertexIndexArray.add(vxPosition);
            }
        }

        final int updateVertexCount = update.cardinality();

        final BitSet[] traversal = new BitSet[updateVertexCount];
        final float[] scores = new float[updateVertexCount];
        final BitSet[] sendBuffer = new BitSet[updateVertexCount];

        for (int i = 0; i < updateVertexCount; i++) {
            traversal[i] = new BitSet(updateVertexCount);
            sendBuffer[i] = new BitSet(updateVertexCount);
        }

        while (!update.isEmpty()) {

            // each node with messages needs to update its own information
            for (int vxPosition = 0; vxPosition < updateVertexCount; vxPosition++) {
                traversal[vxPosition].or(sendBuffer[vxPosition]);
                traversal[vxPosition].set(vxPosition);
                sendBuffer[vxPosition].clear();
            }

            // for each neighbour, check if there is any new information it needs to receive
            for (int sourcePosition = update.nextSetBit(0); sourcePosition >= 0; sourcePosition = update.nextSetBit(sourcePosition + 1)) {
                final int vxId = graph.getVertex(sourcePosition);
                final int sourcePosLocal = updatedVertexIndexArray.indexOf(sourcePosition);

                for (int neighbourPosition = 0; neighbourPosition < graph.getVertexNeighbourCount(vxId); neighbourPosition++) {
                    final int destinationPositionGraph = graph.getVertexPosition(graph.getVertexNeighbour(vxId, neighbourPosition));
                    final int destinationPosition = updatedVertexIndexArray.indexOf(destinationPositionGraph);
                    if (!traversal[sourcePosLocal].equals(traversal[destinationPosition])) {

                        final BitSet diff = (BitSet) traversal[sourcePosLocal].clone();
                        diff.andNot(traversal[destinationPosition]);
                        sendBuffer[destinationPosition].or(diff);
                        turn.or(diff);
                        newUpdate.set(destinationPositionGraph);
                    }
                }
            }
            // update scores based on the current traversal state
            switch (scoreType) {
                case ECCENTRICITY ->
                    updateEccentricityScoresUndirected(scores, turn);
                case AVERAGE_DISTANCE ->
                    updateAveragePathScoresUndirected(distances, scores, turn, sendBuffer, updatedVertexIndexArray, graph);
                default ->
                    throw new IllegalArgumentException(String.format(SCORETYPE_ERROR_FORMAT, scoreType));
            }

            turn.clear();

            update.clear();
            update.or(newUpdate);
            newUpdate.clear();
        }

        return switch (scoreType) {
            case ECCENTRICITY ->
                ThreeTuple.create(traversal, scores, updateToReturn);
            case AVERAGE_DISTANCE -> {
                final float[] distanceArray = new float[distances.size()];
                for (int i = 0; i < distances.size(); i++) {
                    distanceArray[i] = distances.get(i);
                }
                yield ThreeTuple.create(traversal, distanceArray, updateToReturn);
            }
            default ->
                throw new IllegalArgumentException(String.format(SCORETYPE_ERROR_FORMAT, scoreType));
        };
    }

    protected static Tuple<BitSet[], float[]> computeAllPathsDirected(final GraphReadMethods graph, final ScoreType scoreType,
            final boolean includeConnectionsIn, final boolean includeConnectionsOut, final boolean treatUndirectedBidirectional) {

        // Run the algorithm
        final ThreeTuple<BitSet[], float[], BitSet> result = computeAllPathsDirectedThreeTuple(graph, scoreType, includeConnectionsIn, includeConnectionsOut, treatUndirectedBidirectional);

        // Convert results into a two tuple
        return convertThreeTupleResultsToTwo(result, graph.getVertexCount(), scoreType);

    }

    protected static ThreeTuple<BitSet[], float[], BitSet> computeAllPathsDirectedThreeTuple(final GraphReadMethods graph, final ScoreType scoreType,
            final boolean includeConnectionsIn, final boolean includeConnectionsOut, final boolean treatUndirectedBidirectional) {

        final int vertexCount = graph.getVertexCount();

        final ArrayList<Float> distances = new ArrayList<>();

        final BitSet update = new BitSet(vertexCount);
        final BitSet updateReturn = new BitSet(vertexCount);

        final BitSet newUpdate = new BitSet(vertexCount);
        final BitSet turn = new BitSet(vertexCount);

        final ArrayList<Integer> updatedVertexIndexArray = new ArrayList<>();

        // initialising variables
        for (int vxPosition = 0; vxPosition < vertexCount; vxPosition++) {
            // assuming the node has neighbours
            if (graph.getVertexNeighbourCount(graph.getVertex(vxPosition)) > 0) {
                update.set(vxPosition);
                updateReturn.set(vxPosition);
                updatedVertexIndexArray.add(vxPosition);
            }
        }

        final int updateVertexCount = update.cardinality();

        final BitSet[] traversal = new BitSet[updateVertexCount];
        final float[] scores = new float[updateVertexCount];
        final BitSet[] sendBuffer = new BitSet[updateVertexCount];

        for (int i = 0; i < updateVertexCount; i++) {
            traversal[i] = new BitSet(updateVertexCount);
            sendBuffer[i] = new BitSet(updateVertexCount);
        }

        while (!update.isEmpty()) {

            // update the information of each node with messages
            for (int vertexPosition = 0; vertexPosition < updateVertexCount; vertexPosition++) {
                traversal[vertexPosition].or(sendBuffer[vertexPosition]);
                traversal[vertexPosition].set(vertexPosition);
                sendBuffer[vertexPosition].clear();
            }

            // for each neighbour, check if there is any new information it needs to receive
            for (int vertexPosition = update.nextSetBit(0); vertexPosition >= 0; vertexPosition = update.nextSetBit(vertexPosition + 1)) {
                int vertexId = graph.getVertex(vertexPosition);
                final int vertexPosLocal = updatedVertexIndexArray.indexOf(vertexPosition);

                for (int vertexNeighbourPosition = 0; vertexNeighbourPosition < graph.getVertexNeighbourCount(vertexId); vertexNeighbourPosition++) {
                    final int neighbourId = graph.getVertexNeighbour(vertexId, vertexNeighbourPosition);
                    final int neighbourPositionGraph = graph.getVertexPosition(neighbourId);
                    final int neighbourPosition = updatedVertexIndexArray.indexOf(neighbourPositionGraph);

                    boolean isRequestedDirection = false;
                    final int linkId = graph.getLink(vertexId, neighbourId);
                    for (int linkEdgePosition = 0; linkEdgePosition < graph.getLinkEdgeCount(linkId); linkEdgePosition++) {
                        final int edgeId = graph.getLinkEdge(linkId, linkEdgePosition);
                        final int edgeDirection = graph.getEdgeDirection(edgeId);
                        isRequestedDirection = (treatUndirectedBidirectional && edgeDirection == GraphConstants.UNDIRECTED)
                                || (includeConnectionsIn && graph.getEdgeSourceVertex(edgeId) == neighbourId)
                                || (includeConnectionsOut && graph.getEdgeDestinationVertex(edgeId) == neighbourId);
                        if (isRequestedDirection) {
                            break;
                        }
                    }

                    if (isRequestedDirection && !traversal[vertexPosLocal].equals(traversal[neighbourPosition])) {
                        final BitSet diff = (BitSet) traversal[vertexPosLocal].clone();
                        diff.andNot(traversal[neighbourPosition]);
                        turn.or(diff);
                        sendBuffer[neighbourPosition].or(diff);
                        newUpdate.set(neighbourPositionGraph);
                    }
                }
            }

            // update scores based on the current traversal state
            switch (scoreType) {
                case ECCENTRICITY ->
                    updateEccentricityScoresDirected(scores, turn);
                case AVERAGE_DISTANCE ->
                    updateAveragePathScoresUndirected(distances, scores, turn, sendBuffer, updatedVertexIndexArray, graph);
                default ->
                    throw new IllegalArgumentException(String.format(SCORETYPE_ERROR_FORMAT, scoreType));
            }

            turn.clear();

            update.clear();
            update.or(newUpdate);
            newUpdate.clear();
        }

        return switch (scoreType) {
            case ECCENTRICITY ->
                ThreeTuple.create(traversal, scores, updateReturn);
            case AVERAGE_DISTANCE -> {
                final float[] distanceArray = new float[distances.size()];
                for (int i = 0; i < distances.size(); i++) {
                    distanceArray[i] = distances.get(i);
                }
                yield ThreeTuple.create(traversal, distanceArray, updateReturn);
            }
            default ->
                throw new IllegalArgumentException(String.format(SCORETYPE_ERROR_FORMAT, scoreType));
        };
    }

    /**
     * Wrapper function for computeShortestPathsUndirectedThreeTuple that transforms the result into a two tuple
     *
     * @param graph The graph to compute the shortest paths of
     * @param scoreType Either BETWEENNESS, CLOSENESS, FARNESS, HARMONIC_CLOSENESS, HARMONIC_FARNESS
     * @param selectedOnly Whether or not to restrict to only selected nodes
     *
     * @return Tuple containing an array of BitSets representing each node's traversal and an array of floats with each
     * node's score
     */
    protected static Tuple<BitSet[], float[]> computeShortestPathsUndirected(final GraphReadMethods graph, final ScoreType scoreType, final boolean selectedOnly) {
        // Run the algorithm
        final ThreeTuple<BitSet[], float[], BitSet> result = computeShortestPathsUndirectedThreeTuple(graph, scoreType, selectedOnly);

        return convertThreeTupleResultsToTwo(result, graph.getVertexCount(), scoreType);
    }

    /**
     * Calculates the shortest paths (undirected) for the given graph
     *
     * @param graph The graph to compute the shortest paths of
     * @param scoreType Either BETWEENNESS, CLOSENESS, FARNESS, HARMONIC_CLOSENESS, HARMONIC_FARNESS
     * @param selectedOnly Whether or not to restrict to only selected nodes
     *
     * @return ThreeTuple containing an array of BitSets representing each node's traversal, an array of floats with
     * each node's score, and a BitSet representing which nodes from the graph are actually influential to the function
     */
    protected static ThreeTuple<BitSet[], float[], BitSet> computeShortestPathsUndirectedThreeTuple(final GraphReadMethods graph, final ScoreType scoreType, final boolean selectedOnly) {
        final int vertexCount = graph.getVertexCount();
        final BitSet update = new BitSet(vertexCount);
        final BitSet updateUnmodified = new BitSet(vertexCount);

        final ArrayList<Integer> updatedVertexIndexArray = new ArrayList<>();

        // initialise variables
        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
            if (graph.getVertexNeighbourCount(graph.getVertex(vertexPosition)) > 0) {
                updatedVertexIndexArray.add(vertexPosition);
                update.set(vertexPosition);
                updateUnmodified.set(vertexPosition);
            }
        }

        final int updateVertexCount = updatedVertexIndexArray.size();
        final BitSet[] traversal = new BitSet[updateVertexCount];
        final float[] scores = new float[updateVertexCount]; // Array initialised with 0's

        final BitSet[] sendFails = new BitSet[updateVertexCount];
        final BitSet[] sendBuffer = new BitSet[updateVertexCount];
        final BitSet[] exclusions = new BitSet[updateVertexCount];
        final BitSet newUpdate = new BitSet(vertexCount);
        final BitSet turn = new BitSet(updateVertexCount);

        // initialise variables
        for (int vertexPosition = 0; vertexPosition < updateVertexCount; vertexPosition++) {
            traversal[vertexPosition] = new BitSet(updateVertexCount);
            sendFails[vertexPosition] = new BitSet(updateVertexCount);
            sendBuffer[vertexPosition] = new BitSet(updateVertexCount);
            exclusions[vertexPosition] = new BitSet(updateVertexCount);
        }

        while (!update.isEmpty()) {
            // update the information of each node with messages
            for (int i = 0; i < updateVertexCount; i++) {
                traversal[i].or(sendBuffer[i]);
                traversal[i].set(i);
                sendFails[i].clear();
                sendFails[i].or(sendBuffer[i]);
                sendBuffer[i].clear();
            }

            // for each neighbour, check if there is any new information it needs to receive
            for (int i = 0; i < updateVertexCount; i++) {
                final int vertexId = graph.getVertex(updatedVertexIndexArray.get(i));

                for (int vertexNeighbourPosition = 0; vertexNeighbourPosition < graph.getVertexNeighbourCount(vertexId); vertexNeighbourPosition++) {
                    final int neighbourId = graph.getVertexNeighbour(vertexId, vertexNeighbourPosition);
                    final int neighbourPosition = updatedVertexIndexArray.indexOf(graph.getVertexPosition(neighbourId));
                    if (!traversal[i].equals(traversal[neighbourPosition])) {
                        turn.set(neighbourPosition, true);

                        final BitSet diff = (BitSet) traversal[i].clone();
                        diff.andNot(traversal[neighbourPosition]);
                        sendBuffer[neighbourPosition].or(diff);
                        sendFails[i].andNot(diff);
                        newUpdate.set(graph.getVertexPosition(neighbourId));
                    }
                }
                for (int neighbourPosition = sendFails[i].nextSetBit(0); neighbourPosition >= 0; neighbourPosition = sendFails[i].nextSetBit(neighbourPosition + 1)) {
                    exclusions[neighbourPosition].set(i, true);
                }
            }

            // update scores based on the current traversal state
            switch (scoreType) {
                case BETWEENNESS ->
                    updateBetweennessScoresUndirected(graph, traversal, scores, sendBuffer, exclusions, turn, selectedOnly);
                case CLOSENESS, FARNESS ->
                    updateFarnessScoresUndirected(graph, traversal, scores, sendBuffer, exclusions, turn, selectedOnly);
                case HARMONIC_CLOSENESS, HARMONIC_FARNESS ->
                    updateHarmonicFarnessScoresUndirected(graph, traversal, scores, sendBuffer, exclusions, turn, selectedOnly);
                default ->
                    throw new IllegalArgumentException(String.format(SCORETYPE_ERROR_FORMAT, scoreType));
            }

            turn.clear();
            update.clear();
            update.or(newUpdate);
            newUpdate.clear();
        }

        // convert farness to closeness by taking the inverse of each score
        if (scoreType == ScoreType.CLOSENESS) {
            for (int index = 0; index < scores.length; index++) {
                scores[index] = scores[index] == 0 ? 0 : 1 / scores[index];
            }
        }

        // convert harmonic farness to harmonic closeness by normalising each
        // score by the number of vertices on the graph
        if (scoreType == ScoreType.HARMONIC_CLOSENESS) {
            for (int index = 0; index < scores.length; index++) {
                scores[index] = scores[index] == 0 ? 0 : scores[index] / scores.length;
            }
        }
        return ThreeTuple.create(traversal, scores, updateUnmodified);
    }

    protected static Tuple<BitSet[], float[]> computeShortestPathsDirected(final GraphReadMethods graph, final ScoreType scoreType,
            final boolean includeConnectionsIn, final boolean includeConnectionsOut, final boolean treatUndirectedBidirectional, final boolean selectedOnly) {
        // Run the algorithm
        final ThreeTuple<BitSet[], float[], BitSet> result = computeShortestPathsDirectedThreeTuple(graph, scoreType,
                includeConnectionsIn, includeConnectionsOut, treatUndirectedBidirectional, selectedOnly);

        // Convert results into a two tuple
        return convertThreeTupleResultsToTwo(result, graph.getVertexCount(), scoreType);
    }

    protected static ThreeTuple<BitSet[], float[], BitSet> computeShortestPathsDirectedThreeTuple(final GraphReadMethods graph, final ScoreType scoreType,
            final boolean includeConnectionsIn, final boolean includeConnectionsOut, final boolean treatUndirectedBidirectional, final boolean selectedOnly) {

        final int vertexCount = graph.getVertexCount();

        final BitSet updateUnmodified = new BitSet(vertexCount); // A Copy of updateF that wont be modified during this functions execution, will be returned
        final BitSet updateF = new BitSet(vertexCount);
        final BitSet updateB = new BitSet(vertexCount);
        final ArrayList<Integer> updatedVertexIndexArray = new ArrayList<>();

        // initialise variables
        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
            if (graph.getVertexNeighbourCount(graph.getVertex(vertexPosition)) > 0) {
                updatedVertexIndexArray.add(vertexPosition);

                updateF.set(vertexPosition);
                updateB.set(vertexPosition);
                updateUnmodified.set(vertexPosition);
            }
        }

        final int updateVertexCount = updatedVertexIndexArray.size();

        final BitSet[] traversalF = new BitSet[updateVertexCount];
        final BitSet[] traversalB = new BitSet[updateVertexCount];
        final float[] scores = new float[updateVertexCount]; // Initialises with 0

        final BitSet[] sendFailsF = new BitSet[updateVertexCount];
        final BitSet[] sendBufferF = new BitSet[updateVertexCount];
        final BitSet[] exclusionsF = new BitSet[updateVertexCount];
        final BitSet newUpdateF = new BitSet(vertexCount);

        final BitSet[] sendFailsB = new BitSet[updateVertexCount];
        final BitSet[] sendBufferB = new BitSet[updateVertexCount];
        final BitSet[] exclusionsB = new BitSet[updateVertexCount];
        final BitSet newUpdateB = new BitSet(vertexCount);

        final BitSet turn = new BitSet(updateVertexCount);

        // initialise variables
        for (int vertexPosition = 0; vertexPosition < updateVertexCount; vertexPosition++) {
            traversalF[vertexPosition] = new BitSet(updateVertexCount);
            sendFailsF[vertexPosition] = new BitSet(updateVertexCount);
            sendBufferF[vertexPosition] = new BitSet(updateVertexCount);
            exclusionsF[vertexPosition] = new BitSet(updateVertexCount);

            traversalB[vertexPosition] = new BitSet(updateVertexCount);
            sendFailsB[vertexPosition] = new BitSet(updateVertexCount);
            sendBufferB[vertexPosition] = new BitSet(updateVertexCount);
            exclusionsB[vertexPosition] = new BitSet(updateVertexCount);

        }

        while (!updateF.isEmpty() || !updateB.isEmpty()) {
            // update the information of each node with messages
            for (int i = 0; i < updateVertexCount; i++) {
                traversalF[i].or(sendBufferF[i]);
                traversalF[i].set(i);
                sendFailsF[i].clear();
                sendFailsF[i].or(sendBufferF[i]);
                sendBufferF[i].clear();

                traversalB[i].or(sendBufferB[i]);
                traversalB[i].set(i);
                sendFailsB[i].clear();
                sendFailsB[i].or(sendBufferB[i]);
                sendBufferB[i].clear();
            }

            // for each neighbour, check if there is any new information it needs to receive
            for (int vertexPositionGlobal = updateF.nextSetBit(0); vertexPositionGlobal >= 0; vertexPositionGlobal = updateF.nextSetBit(vertexPositionGlobal + 1)) {
                final int vertexId = graph.getVertex(vertexPositionGlobal);
                final int vertexPosition = updatedVertexIndexArray.indexOf(graph.getVertexPosition(vertexId));

                for (int vertexNeighbourPosition = 0; vertexNeighbourPosition < graph.getVertexNeighbourCount(vertexId); vertexNeighbourPosition++) {
                    final int neighbourId = graph.getVertexNeighbour(vertexId, vertexNeighbourPosition);
                    final int neighbourPosition = updatedVertexIndexArray.indexOf(graph.getVertexPosition(neighbourId));

                    boolean isRequestedDirection = false;
                    final int linkId = graph.getLink(vertexId, neighbourId);
                    for (int linkEdgePosition = 0; linkEdgePosition < graph.getLinkEdgeCount(linkId); linkEdgePosition++) {
                        final int edgeId = graph.getLinkEdge(linkId, linkEdgePosition);
                        final int edgeDirection = graph.getEdgeDirection(edgeId);
                        isRequestedDirection = (treatUndirectedBidirectional && edgeDirection == GraphConstants.UNDIRECTED)
                                || (includeConnectionsIn && graph.getEdgeDestinationVertex(edgeId) == neighbourId)
                                || (includeConnectionsOut && graph.getEdgeSourceVertex(edgeId) == neighbourId);
                        if (isRequestedDirection) {
                            break;
                        }
                    }

                    if (isRequestedDirection && !traversalF[vertexPosition].equals(traversalF[neighbourPosition])) {
                        turn.set(neighbourPosition, true);

                        final BitSet diff = (BitSet) traversalF[vertexPosition].clone();
                        diff.andNot(traversalF[neighbourPosition]);
                        sendBufferF[neighbourPosition].or(diff);
                        sendFailsF[vertexPosition].andNot(diff);
                        newUpdateF.set(graph.getVertexPosition(neighbourId));
                    }
                }
                for (int neighbourPosition = sendFailsF[vertexPosition].nextSetBit(0); neighbourPosition >= 0; neighbourPosition = sendFailsF[vertexPosition].nextSetBit(neighbourPosition + 1)) {
                    exclusionsF[neighbourPosition].set(vertexPosition, true);
                }
            }

            // for each neighbour, check if there is any new information it needs to receive
            for (int vertexPositionGlobal = updateB.nextSetBit(0); vertexPositionGlobal >= 0; vertexPositionGlobal = updateB.nextSetBit(vertexPositionGlobal + 1)) {
                final int vertexId = graph.getVertex(vertexPositionGlobal);
                final int vertexPosition = updatedVertexIndexArray.indexOf(graph.getVertexPosition(vertexId));

                for (int vertexNeighbourPosition = 0; vertexNeighbourPosition < graph.getVertexNeighbourCount(vertexId); vertexNeighbourPosition++) {
                    final int neighbourId = graph.getVertexNeighbour(vertexId, vertexNeighbourPosition);
                    final int neighbourPosition = updatedVertexIndexArray.indexOf(graph.getVertexPosition(neighbourId));

                    boolean isOppositeDirection = false;
                    int linkId = graph.getLink(vertexId, neighbourId);
                    for (int linkEdgePosition = 0; linkEdgePosition < graph.getLinkEdgeCount(linkId); linkEdgePosition++) {
                        final int edgeId = graph.getLinkEdge(linkId, linkEdgePosition);
                        final int edgeDirection = graph.getEdgeDirection(edgeId);
                        isOppositeDirection = (treatUndirectedBidirectional && edgeDirection == GraphConstants.UNDIRECTED)
                                || (includeConnectionsIn && graph.getEdgeSourceVertex(edgeId) == neighbourId)
                                || (includeConnectionsOut && graph.getEdgeDestinationVertex(edgeId) == neighbourId);
                        if (isOppositeDirection) {
                            break;
                        }
                    }

                    if (isOppositeDirection && !traversalB[vertexPosition].equals(traversalB[neighbourPosition])) {
                        final BitSet diff = (BitSet) traversalB[vertexPosition].clone();
                        diff.andNot(traversalB[neighbourPosition]);
                        sendBufferB[neighbourPosition].or(diff);
                        sendFailsB[vertexPosition].andNot(diff);
                        newUpdateB.set(graph.getVertexPosition(neighbourId));
                    }
                }
                for (int neighbourPosition = sendFailsB[vertexPosition].nextSetBit(0); neighbourPosition >= 0; neighbourPosition = sendFailsB[vertexPosition].nextSetBit(neighbourPosition + 1)) {
                    exclusionsB[neighbourPosition].set(vertexPosition, true);
                }
            }

            // update scores based on the current traversal state
            switch (scoreType) {
                case BETWEENNESS ->
                    updateBetweennessScoresDirected(graph, traversalF, traversalB, scores, sendBufferF, sendBufferB, exclusionsF, exclusionsB, turn, selectedOnly);
                case CLOSENESS, FARNESS ->
                    updateFarnessScoresDirected(graph, traversalF, traversalB, scores, sendBufferF, sendBufferB, exclusionsF, exclusionsB, turn, selectedOnly);
                case HARMONIC_CLOSENESS, HARMONIC_FARNESS ->
                    updateHarmonicFarnessScoresDirected(graph, traversalF, traversalB, scores, sendBufferF, sendBufferB, exclusionsF, exclusionsB, turn, selectedOnly);
                default ->
                    throw new IllegalArgumentException(String.format(SCORETYPE_ERROR_FORMAT, scoreType));
            }

            turn.clear();

            updateF.clear();
            updateF.or(newUpdateF);
            newUpdateF.clear();

            updateB.clear();
            updateB.or(newUpdateB);
            newUpdateB.clear();
        }

        for (int vertexPosition = 0; vertexPosition < updateVertexCount; vertexPosition++) {
            if (traversalB[vertexPosition] != null) {
                traversalF[vertexPosition].or(traversalB[vertexPosition]);
            }
        }

        // convert farness to closeness by taking the inverse of each score
        if (scoreType == ScoreType.CLOSENESS) {
            for (int index = 0; index < scores.length; index++) {
                scores[index] = scores[index] == 0 ? 0 : 1 / scores[index];
            }
        }

        // convert harmonic farness to harmonic closeness by normalising each
        // score by the number of vertices on the graph
        if (scoreType == ScoreType.HARMONIC_CLOSENESS) {
            for (int index = 0; index < scores.length; index++) {
                scores[index] = scores[index] == 0 ? 0 : scores[index] / scores.length;
            }
        }

        return ThreeTuple.create(traversalF, scores, updateUnmodified);
    }

    private static void updateEccentricityScoresUndirected(final float[] scores, final BitSet turn) {
        // for each node that has a message in transit, update its eccentricity
        for (int vxId = turn.nextSetBit(0); vxId >= 0; vxId = turn.nextSetBit(vxId + 1)) {
            scores[vxId]++;
        }
    }

    private static void updateEccentricityScoresDirected(final float[] scores, final BitSet turn) {
        // for each node that has a message in transit, update its eccentricity
        for (int vxId = turn.nextSetBit(0); vxId >= 0; vxId = turn.nextSetBit(vxId + 1)) {
            scores[vxId]++;
        }
    }

    private static void updateAveragePathScoresUndirected(final ArrayList<Float> distances, final float[] scores, final BitSet turn, final BitSet[] sendBuffer, final ArrayList<Integer> updateVertexArray, final GraphReadMethods graph) {
        // for each node that has a message in transit, update its eccentricity
        for (int vxPos = turn.nextSetBit(0); vxPos >= 0; vxPos = turn.nextSetBit(vxPos + 1)) {
            int vxId = graph.getVertex(vxPos);
            final int localVxId = updateVertexArray.indexOf(vxId);

            if (localVxId == -1) {
                continue;
            }

            scores[localVxId]++;
            for (int nxId = sendBuffer[localVxId].nextSetBit(0); nxId >= 0; nxId = sendBuffer[localVxId].nextSetBit(nxId + 1)) {
                distances.add(scores[localVxId]);
            }
        }
    }

    private static void updateBetweennessScoresUndirected(final GraphReadMethods graph, final BitSet[] traversal, final float[] scores,
            final BitSet[] sendBuffer, final BitSet[] exclusions, final BitSet turn, final boolean selectedOnly) {
        final int selectedAttribute = VisualConcept.VertexAttribute.SELECTED.get(graph);
        if (selectedOnly && selectedAttribute == GraphConstants.NOT_FOUND) {
            throw new ArrayIndexOutOfBoundsException(OUT_OF_BOUNDS_EXCEPTION_STRING);
        }
        for (int vertexPosition = turn.nextSetBit(0); vertexPosition >= 0; vertexPosition = turn.nextSetBit(vertexPosition + 1)) {
            final BitSet diff = (BitSet) sendBuffer[vertexPosition].clone();
            diff.andNot(traversal[vertexPosition]);
            for (int newVertexPosition = diff.nextSetBit(0); newVertexPosition >= 0; newVertexPosition = diff.nextSetBit(newVertexPosition + 1)) {
                if (sendBuffer[vertexPosition].get(newVertexPosition)) {
                    final boolean vertexSelected = graph.getBooleanValue(selectedAttribute, graph.getVertex(vertexPosition));
                    final boolean newVertexSelected = graph.getBooleanValue(selectedAttribute, graph.getVertex(newVertexPosition));
                    if (!selectedOnly || (vertexSelected && newVertexSelected)) {
                        final BitSet intersection = (BitSet) traversal[newVertexPosition].clone();
                        intersection.and(traversal[vertexPosition]);
                        intersection.andNot(sendBuffer[vertexPosition]);
                        intersection.andNot(sendBuffer[newVertexPosition]);
                        for (int index = intersection.nextSetBit(0); index >= 0; index = intersection.nextSetBit(index + 1)) {
                            if (exclusions[vertexPosition].get(index) && exclusions[newVertexPosition].get(index)) {
                                continue;
                            }
                            scores[index]++;
                        }
                    }
                }
            }
        }
    }

    private static void updateBetweennessScoresDirected(final GraphReadMethods graph, final BitSet[] traversalF, final BitSet[] traversalB, final float[] scores,
            final BitSet[] sendBufferF, final BitSet[] sendBufferB, final BitSet[] exclusionsF, final BitSet[] exclusionsB, final BitSet turn, final boolean selectedOnly) {
        final int selectedAttribute = VisualConcept.VertexAttribute.SELECTED.get(graph);
        if (selectedOnly && selectedAttribute == GraphConstants.NOT_FOUND) {
            throw new ArrayIndexOutOfBoundsException(OUT_OF_BOUNDS_EXCEPTION_STRING);
        }
        for (int vertexPosition = turn.nextSetBit(0); vertexPosition >= 0; vertexPosition = turn.nextSetBit(vertexPosition + 1)) {
            final BitSet diff = (BitSet) sendBufferF[vertexPosition].clone();
            diff.andNot(traversalF[vertexPosition]);
            for (int newVertexPosition = diff.nextSetBit(0); newVertexPosition >= 0; newVertexPosition = diff.nextSetBit(newVertexPosition + 1)) {
                if (sendBufferF[vertexPosition].get(newVertexPosition) && sendBufferB[newVertexPosition].get(vertexPosition)) {
                    final boolean vertexSelected = graph.getBooleanValue(selectedAttribute, graph.getVertex(vertexPosition));
                    final boolean newVertexSelected = graph.getBooleanValue(selectedAttribute, graph.getVertex(newVertexPosition));
                    if (!selectedOnly || (vertexSelected && newVertexSelected)) {
                        final BitSet intersection = (BitSet) traversalB[newVertexPosition].clone();
                        intersection.and(traversalF[vertexPosition]);
                        intersection.andNot(sendBufferF[vertexPosition]);
                        intersection.andNot(sendBufferB[newVertexPosition]);
                        for (int index = intersection.nextSetBit(0); index >= 0; index = intersection.nextSetBit(index + 1)) {
                            if (exclusionsB[vertexPosition].get(index) && exclusionsF[newVertexPosition].get(index)) {
                                continue;
                            }
                            scores[index]++;
                        }
                    }
                }
            }
        }
    }

    private static void updateFarnessScoresUndirected(final GraphReadMethods graph, final BitSet[] traversal, final float[] scores,
            final BitSet[] sendBuffer, final BitSet[] exclusions, final BitSet turn, final boolean selectedOnly) {
        final int selectedAttribute = VisualConcept.VertexAttribute.SELECTED.get(graph);
        if (selectedOnly && selectedAttribute == GraphConstants.NOT_FOUND) {
            throw new ArrayIndexOutOfBoundsException(OUT_OF_BOUNDS_EXCEPTION_STRING);
        }
        for (int vertexPosition = turn.nextSetBit(0); vertexPosition >= 0; vertexPosition = turn.nextSetBit(vertexPosition + 1)) {
            final BitSet diff = (BitSet) sendBuffer[vertexPosition].clone();
            diff.andNot(traversal[vertexPosition]);
            for (int newVertexPosition = diff.nextSetBit(0); newVertexPosition >= 0; newVertexPosition = diff.nextSetBit(newVertexPosition + 1)) {
                if (sendBuffer[vertexPosition].get(newVertexPosition)) {
                    final boolean newVertexSelected = graph.getBooleanValue(selectedAttribute, graph.getVertex(newVertexPosition));
                    if (!selectedOnly || newVertexSelected) {
                        final BitSet intersection = (BitSet) traversal[newVertexPosition].clone();
                        intersection.and(traversal[vertexPosition]);
                        intersection.andNot(sendBuffer[vertexPosition]);
                        intersection.andNot(sendBuffer[newVertexPosition]);
                        for (int index = intersection.nextSetBit(0); index >= 0; index = intersection.nextSetBit(index + 1)) {
                            if (exclusions[vertexPosition].get(index) && exclusions[newVertexPosition].get(index)) {
                                intersection.set(index, false);
                            }
                        }
                        scores[vertexPosition] += (intersection.cardinality() + 1);
                    }
                }
            }
        }
    }

    private static void updateFarnessScoresDirected(final GraphReadMethods graph, final BitSet[] traversalF, final BitSet[] traversalB, final float[] scores,
            final BitSet[] sendBufferF, final BitSet[] sendBufferB, final BitSet[] exclusionsF, final BitSet[] exclusionsB, final BitSet turn, final boolean selectedOnly) {
        final int selectedAttribute = VisualConcept.VertexAttribute.SELECTED.get(graph);
        if (selectedOnly && selectedAttribute == GraphConstants.NOT_FOUND) {
            throw new ArrayIndexOutOfBoundsException(OUT_OF_BOUNDS_EXCEPTION_STRING);
        }
        for (int vertexPosition = turn.nextSetBit(0); vertexPosition >= 0; vertexPosition = turn.nextSetBit(vertexPosition + 1)) {
            final BitSet diff = (BitSet) sendBufferF[vertexPosition].clone();
            diff.andNot(traversalF[vertexPosition]);
            for (int newVertexPosition = diff.nextSetBit(0); newVertexPosition >= 0; newVertexPosition = diff.nextSetBit(newVertexPosition + 1)) {
                if (sendBufferF[vertexPosition].get(newVertexPosition) && sendBufferB[newVertexPosition].get(vertexPosition)) {
                    final boolean newVertexSelected = graph.getBooleanValue(selectedAttribute, graph.getVertex(newVertexPosition));
                    if (!selectedOnly || newVertexSelected) {
                        final BitSet intersection = (BitSet) traversalB[newVertexPosition].clone();
                        intersection.and(traversalF[vertexPosition]);
                        intersection.andNot(sendBufferF[vertexPosition]);
                        intersection.andNot(sendBufferB[newVertexPosition]);
                        for (int index = intersection.nextSetBit(0); index >= 0; index = intersection.nextSetBit(index + 1)) {
                            if (exclusionsB[vertexPosition].get(index) && exclusionsF[newVertexPosition].get(index)) {
                                intersection.set(index, false);
                            }
                        }
                        scores[vertexPosition] += (intersection.cardinality() + 1);
                    }
                }
            }
        }
    }

    private static void updateHarmonicFarnessScoresUndirected(final GraphReadMethods graph, final BitSet[] traversal, final float[] scores,
            final BitSet[] sendBuffer, final BitSet[] exclusions, final BitSet turn, final boolean selectedOnly) {
        final int selectedAttribute = VisualConcept.VertexAttribute.SELECTED.get(graph);
        if (selectedOnly && selectedAttribute == GraphConstants.NOT_FOUND) {
            throw new ArrayIndexOutOfBoundsException(OUT_OF_BOUNDS_EXCEPTION_STRING);
        }
        for (int vertexPosition = turn.nextSetBit(0); vertexPosition >= 0; vertexPosition = turn.nextSetBit(vertexPosition + 1)) {
            final BitSet diff = (BitSet) sendBuffer[vertexPosition].clone();
            diff.andNot(traversal[vertexPosition]);
            for (int newVertexPosition = diff.nextSetBit(0); newVertexPosition >= 0; newVertexPosition = diff.nextSetBit(newVertexPosition + 1)) {
                if (sendBuffer[vertexPosition].get(newVertexPosition)) {
                    final boolean newVertexSelected = graph.getBooleanValue(selectedAttribute, graph.getVertex(newVertexPosition));
                    if (!selectedOnly || newVertexSelected) {
                        final BitSet intersection = (BitSet) traversal[newVertexPosition].clone();
                        intersection.and(traversal[vertexPosition]);
                        intersection.andNot(sendBuffer[vertexPosition]);
                        intersection.andNot(sendBuffer[newVertexPosition]);
                        for (int index = intersection.nextSetBit(0); index >= 0; index = intersection.nextSetBit(index + 1)) {
                            if (exclusions[vertexPosition].get(index) && exclusions[newVertexPosition].get(index)) {
                                intersection.set(index, false);
                            }
                        }
                        scores[vertexPosition] += (1.0 / (intersection.cardinality() + 1));
                        scores[newVertexPosition] += (1.0 / (intersection.cardinality() + 1));
                    }
                }
            }
        }
    }

    private static void updateHarmonicFarnessScoresDirected(final GraphReadMethods graph, final BitSet[] traversalF, final BitSet[] traversalB, final float[] scores,
            final BitSet[] sendBufferF, final BitSet[] sendBufferB, final BitSet[] exclusionsF, final BitSet[] exclusionsB, final BitSet turn, final boolean selectedOnly) {
        final int selectedAttribute = VisualConcept.VertexAttribute.SELECTED.get(graph);
        if (selectedOnly && selectedAttribute == GraphConstants.NOT_FOUND) {
            throw new ArrayIndexOutOfBoundsException(OUT_OF_BOUNDS_EXCEPTION_STRING);
        }
        for (int vertexPosition = turn.nextSetBit(0); vertexPosition >= 0; vertexPosition = turn.nextSetBit(vertexPosition + 1)) {
            final BitSet diff = (BitSet) sendBufferF[vertexPosition].clone();
            diff.andNot(traversalF[vertexPosition]);
            for (int newVertexPosition = diff.nextSetBit(0); newVertexPosition >= 0; newVertexPosition = diff.nextSetBit(newVertexPosition + 1)) {
                if (sendBufferF[vertexPosition].get(newVertexPosition) && sendBufferB[newVertexPosition].get(vertexPosition)) {
                    final boolean newVertexSelected = graph.getBooleanValue(selectedAttribute, graph.getVertex(newVertexPosition));
                    if (!selectedOnly || newVertexSelected) {
                        final BitSet intersection = (BitSet) traversalB[newVertexPosition].clone();
                        intersection.and(traversalF[vertexPosition]);
                        intersection.andNot(sendBufferF[vertexPosition]);
                        intersection.andNot(sendBufferB[newVertexPosition]);
                        for (int index = intersection.nextSetBit(0); index >= 0; index = intersection.nextSetBit(index + 1)) {
                            if (exclusionsB[vertexPosition].get(index) && exclusionsF[newVertexPosition].get(index)) {
                                intersection.set(index, false);
                            }
                        }
                        scores[vertexPosition] += (1.0 / (intersection.cardinality() + 1));
                    }
                }
            }
        }
    }

    private static Tuple<BitSet[], float[]> convertThreeTupleResultsToTwo(final ThreeTuple<BitSet[], float[], BitSet> threeTuple, final int vertexCount, final ScoreType scoreType) {
        final BitSet[] traversal = threeTuple.getFirst();
        final float[] secondArray = threeTuple.getSecond();
        final BitSet indexes = threeTuple.getThird();

        // Convert results into a two tuple
        final BitSet[] convertedTraversal = new BitSet[vertexCount];
        final float[] convertedSecond = scoreType == ScoreType.AVERAGE_DISTANCE ? secondArray : new float[vertexCount];

        int currentIndex = 0;
        for (int index = indexes.nextSetBit(0); index >= 0; index = indexes.nextSetBit(index + 1)) {
            final BitSet convertedBitSet = new BitSet(vertexCount);
            final BitSet currentBitSet = traversal[currentIndex];
            int i = 0;
            for (int indexBitSet = indexes.nextSetBit(0); indexBitSet >= 0; indexBitSet = indexes.nextSetBit(indexBitSet + 1)) {
                convertedBitSet.set(indexBitSet, currentBitSet.get(i));
                i++;
            }

            convertedTraversal[index] = convertedBitSet;
            if (scoreType != ScoreType.AVERAGE_DISTANCE) {
                convertedSecond[index] = secondArray[currentIndex];
            }
            currentIndex++;
        }

        return Tuple.create(convertedTraversal, convertedSecond);
    }
}
