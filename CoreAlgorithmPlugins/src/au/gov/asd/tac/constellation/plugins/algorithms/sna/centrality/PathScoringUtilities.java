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
package au.gov.asd.tac.constellation.plugins.algorithms.sna.centrality;

import au.gov.asd.tac.constellation.graph.GraphConstants;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import java.util.ArrayList;
import java.util.BitSet;

/**
 * Utilities for calculating scores on a graph based on shortest paths. This
 * utility makes use of the parallel breadth first search algorithm to
 * efficiently traverse the graph.
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
                int vxId = graph.getVertex(sourcePosition);
                for (int neighbourPosition = 0; neighbourPosition < graph.getVertexNeighbourCount(vxId); neighbourPosition++) {
                    int destinationPosition = graph.getVertexPosition(graph.getVertexNeighbour(vxId, neighbourPosition));
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
                    int neighbourId = graph.getVertexNeighbour(vertexId, vertexNeighbourPosition);
                    int neighbourPosition = graph.getVertexPosition(neighbourId);
                    if (!subgraph.get(neighbourPosition)) {
                        continue;
                    }

                    boolean isRequestedDirection = false;
                    int linkId = graph.getLink(vertexId, neighbourId);
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

    private static Tuple<BitSet[], float[]> computeAllPathsUndirected(final GraphReadMethods graph, final ScoreType scoreType) {
        final int vxCount = graph.getVertexCount();
        final BitSet[] traversal = new BitSet[vxCount];
        final float[] scores = new float[vxCount];
        final ArrayList<Float> distances = new ArrayList<>();

        final BitSet update = new BitSet(vxCount);
        final BitSet[] sendBuffer = new BitSet[vxCount];
        final BitSet turn = new BitSet(vxCount);
        final BitSet newUpdate = new BitSet(vxCount);

        // initialising variables
        for (int vxPosition = 0; vxPosition < vxCount; vxPosition++) {
            // get the vertex ID at this position
            final int vxId = graph.getVertex(vxPosition);

            traversal[vxPosition] = new BitSet(vxCount);
            sendBuffer[vxPosition] = new BitSet(vxCount);
            scores[vxPosition] = 0;

            // assuming the node has neighbours
            if (graph.getVertexNeighbourCount(vxId) > 0) {
                update.set(vxPosition);
            }
        }

        while (!update.isEmpty()) {

            // each node with messages needs to update its own information
            for (int vxPosition = update.nextSetBit(0); vxPosition >= 0; vxPosition = update.nextSetBit(vxPosition + 1)) {
                traversal[vxPosition].or(sendBuffer[vxPosition]);
                traversal[vxPosition].set(vxPosition);
                sendBuffer[vxPosition].clear();
            }

            // for each neighbour, check if there is any new information it needs to receive
            for (int sourcePosition = update.nextSetBit(0); sourcePosition >= 0; sourcePosition = update.nextSetBit(sourcePosition + 1)) {
                int vxId = graph.getVertex(sourcePosition);
                for (int neighbourPosition = 0; neighbourPosition < graph.getVertexNeighbourCount(vxId); neighbourPosition++) {
                    int destinationPosition = graph.getVertexPosition(graph.getVertexNeighbour(vxId, neighbourPosition));
                    if (!traversal[sourcePosition].equals(traversal[destinationPosition])) {

                        final BitSet diff = (BitSet) traversal[sourcePosition].clone();
                        diff.andNot(traversal[destinationPosition]);
                        sendBuffer[destinationPosition].or(diff);
                        turn.or(diff);
                        newUpdate.set(destinationPosition);
                    }
                }
            }
            // update scores based on the current traversal state
            switch (scoreType) {
                case ECCENTRICITY:
                    updateEccentricityScoresUndirected(scores, turn);
                    break;
                case AVERAGE_DISTANCE:
                    updateAveragePathScoresUndirected(distances, scores, turn, sendBuffer);
                    break;
                default:
                    throw new IllegalArgumentException(String.format(SCORETYPE_ERROR_FORMAT, scoreType));
            }

            turn.clear();

            update.clear();
            update.or(newUpdate);
            newUpdate.clear();
        }

        switch (scoreType) {
            case ECCENTRICITY:
                return Tuple.create(traversal, scores);
            case AVERAGE_DISTANCE:
                final float[] distanceArray = new float[distances.size()];
                for (int i = 0; i < distances.size(); i++) {
                    distanceArray[i] = distances.get(i);
                }
                return Tuple.create(traversal, distanceArray);
            default:
                throw new IllegalArgumentException(String.format(SCORETYPE_ERROR_FORMAT, scoreType));
        }
    }

    private static Tuple<BitSet[], float[]> computeAllPathsDirected(final GraphReadMethods graph, final ScoreType scoreType,
            final boolean includeConnectionsIn, final boolean includeConnectionsOut, final boolean treatUndirectedBidirectional) {
        final int vertexCount = graph.getVertexCount();
        final BitSet[] traversal = new BitSet[vertexCount];
        final float[] scores = new float[vertexCount];
        final ArrayList<Float> distances = new ArrayList<>();

        final BitSet update = new BitSet(vertexCount);
        final BitSet[] sendBuffer = new BitSet[vertexCount];
        final BitSet newUpdate = new BitSet(vertexCount);

        final BitSet turn = new BitSet(vertexCount);

        // initialising variables
        for (int vxPosition = 0; vxPosition < vertexCount; vxPosition++) {
            // get the vertex ID at this position
            final int vxId = graph.getVertex(vxPosition);

            traversal[vxPosition] = new BitSet(vertexCount);
            sendBuffer[vxPosition] = new BitSet(vertexCount);

            scores[vxPosition] = 0;

            // assuming the node has neighbours
            if (graph.getVertexNeighbourCount(vxId) > 0) {
                update.set(vxPosition);
            }
        }

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
                    int neighbourId = graph.getVertexNeighbour(vertexId, vertexNeighbourPosition);
                    int neighbourPosition = graph.getVertexPosition(neighbourId);

                    boolean isRequestedDirection = false;
                    int linkId = graph.getLink(vertexId, neighbourId);
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
                        final BitSet diff = (BitSet) traversal[vertexPosition].clone();
                        diff.andNot(traversal[neighbourPosition]);
                        turn.or(diff);
                        sendBuffer[neighbourPosition].or(diff);
                        newUpdate.set(neighbourPosition);
                    }
                }
            }

            // update scores based on the current traversal state
            switch (scoreType) {
                case ECCENTRICITY:
                    updateEccentricityScoresDirected(scores, turn);
                    break;
                case AVERAGE_DISTANCE:
                    updateAveragePathScoresUndirected(distances, scores, turn, sendBuffer);
                    break;
                default:
                    throw new IllegalArgumentException(String.format(SCORETYPE_ERROR_FORMAT, scoreType));
            }

            turn.clear();

            update.clear();
            update.or(newUpdate);
            newUpdate.clear();
        }

        switch (scoreType) {
            case ECCENTRICITY:
                return Tuple.create(traversal, scores);
            case AVERAGE_DISTANCE:
                final float[] distanceArray = new float[distances.size()];
                for (int i = 0; i < distances.size(); i++) {
                    distanceArray[i] = distances.get(i);
                }
                return Tuple.create(traversal, distanceArray);
            default:
                throw new IllegalArgumentException(String.format(SCORETYPE_ERROR_FORMAT, scoreType));
        }
    }

    private static Tuple<BitSet[], float[]> computeShortestPathsUndirected(final GraphReadMethods graph, final ScoreType scoreType, final boolean selectedOnly) {
        final int vertexCount = graph.getVertexCount();
        final BitSet[] traversal = new BitSet[vertexCount];
        final float[] scores = new float[vertexCount];

        final BitSet update = new BitSet(vertexCount);
        final BitSet[] sendFails = new BitSet[vertexCount];
        final BitSet[] sendBuffer = new BitSet[vertexCount];
        final BitSet[] exclusions = new BitSet[vertexCount];
        final BitSet newUpdate = new BitSet(vertexCount);
        final BitSet turn = new BitSet(vertexCount);

        // initialise variables
        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
            traversal[vertexPosition] = new BitSet(vertexCount);
            scores[vertexPosition] = 0;

            // only update nodes with neighbours
            final int vxId = graph.getVertex(vertexPosition);
            if (graph.getVertexNeighbourCount(vxId) > 0) {
                update.set(vertexPosition);
            }

            sendFails[vertexPosition] = new BitSet(vertexCount);
            sendBuffer[vertexPosition] = new BitSet(vertexCount);
            exclusions[vertexPosition] = new BitSet(vertexCount);
        }

        while (!update.isEmpty()) {
            // update the information of each node with messages
            for (int vertexPosition = update.nextSetBit(0); vertexPosition >= 0; vertexPosition = update.nextSetBit(vertexPosition + 1)) {
                traversal[vertexPosition].or(sendBuffer[vertexPosition]);
                traversal[vertexPosition].set(vertexPosition);
                sendFails[vertexPosition].clear();
                sendFails[vertexPosition].or(sendBuffer[vertexPosition]);
                sendBuffer[vertexPosition].clear();
            }

            // for each neighbour, check if there is any new information it needs to receive
            for (int vertexPosition = update.nextSetBit(0); vertexPosition >= 0; vertexPosition = update.nextSetBit(vertexPosition + 1)) {
                int vertexId = graph.getVertex(vertexPosition);

                for (int vertexNeighbourPosition = 0; vertexNeighbourPosition < graph.getVertexNeighbourCount(vertexId); vertexNeighbourPosition++) {
                    int neighbourId = graph.getVertexNeighbour(vertexId, vertexNeighbourPosition);
                    int neighbourPosition = graph.getVertexPosition(neighbourId);
                    if (!traversal[vertexPosition].equals(traversal[neighbourPosition])) {
                        turn.set(neighbourPosition, true);

                        final BitSet diff = (BitSet) traversal[vertexPosition].clone();
                        diff.andNot(traversal[neighbourPosition]);
                        sendBuffer[neighbourPosition].or(diff);
                        sendFails[vertexPosition].andNot(diff);
                        newUpdate.set(neighbourPosition);
                    }
                }
                for (int neighbourPosition = sendFails[vertexPosition].nextSetBit(0); neighbourPosition >= 0; neighbourPosition = sendFails[vertexPosition].nextSetBit(neighbourPosition + 1)) {
                    exclusions[neighbourPosition].set(vertexPosition, true);
                }
            }

            // update scores based on the current traversal state
            switch (scoreType) {
                case BETWEENNESS:
                    updateBetweennessScoresUndirected(graph, traversal, scores, sendBuffer, exclusions, turn, selectedOnly);
                    break;
                case CLOSENESS:
                case FARNESS:
                    updateFarnessScoresUndirected(graph, traversal, scores, sendBuffer, exclusions, turn, selectedOnly);
                    break;
                case HARMONIC_CLOSENESS:
                case HARMONIC_FARNESS:
                    updateHarmonicFarnessScoresUndirected(graph, traversal, scores, sendBuffer, exclusions, turn, selectedOnly);
                    break;
                default:
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

        return Tuple.create(traversal, scores);
    }

    private static Tuple<BitSet[], float[]> computeShortestPathsDirected(final GraphReadMethods graph, final ScoreType scoreType,
            final boolean includeConnectionsIn, final boolean includeConnectionsOut, final boolean treatUndirectedBidirectional, final boolean selectedOnly) {

        final int vertexCount = graph.getVertexCount();
        final BitSet[] traversalF = new BitSet[vertexCount];
        final BitSet[] traversalB = new BitSet[vertexCount];
        final float[] scores = new float[vertexCount];

        final BitSet updateF = new BitSet(vertexCount);
        final BitSet[] sendFailsF = new BitSet[vertexCount];
        final BitSet[] sendBufferF = new BitSet[vertexCount];
        final BitSet[] exclusionsF = new BitSet[vertexCount];
        final BitSet newUpdateF = new BitSet(vertexCount);

        final BitSet updateB = new BitSet(vertexCount);
        final BitSet[] sendFailsB = new BitSet[vertexCount];
        final BitSet[] sendBufferB = new BitSet[vertexCount];
        final BitSet[] exclusionsB = new BitSet[vertexCount];
        final BitSet newUpdateB = new BitSet(vertexCount);

        final BitSet turn = new BitSet(vertexCount);

        // initialise variables
        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
            traversalF[vertexPosition] = new BitSet(vertexCount);
            traversalB[vertexPosition] = new BitSet(vertexCount);
            scores[vertexPosition] = 0;

            // only update nodes with neighbours
            final int vxId = graph.getVertex(vertexPosition);
            if (graph.getVertexNeighbourCount(vxId) > 0) {
                updateF.set(vertexPosition);
                updateB.set(vertexPosition);
            }

            sendFailsF[vertexPosition] = new BitSet(vertexCount);
            sendBufferF[vertexPosition] = new BitSet(vertexCount);
            exclusionsF[vertexPosition] = new BitSet(vertexCount);

            sendFailsB[vertexPosition] = new BitSet(vertexCount);
            sendBufferB[vertexPosition] = new BitSet(vertexCount);
            exclusionsB[vertexPosition] = new BitSet(vertexCount);
        }

        while (!updateF.isEmpty() || !updateB.isEmpty()) {
            // update the information of each node with messages
            for (int vertexPosition = updateF.nextSetBit(0); vertexPosition >= 0; vertexPosition = updateF.nextSetBit(vertexPosition + 1)) {
                traversalF[vertexPosition].or(sendBufferF[vertexPosition]);
                traversalF[vertexPosition].set(vertexPosition);
                sendFailsF[vertexPosition].clear();
                sendFailsF[vertexPosition].or(sendBufferF[vertexPosition]);
                sendBufferF[vertexPosition].clear();
            }
            for (int vertexPosition = updateB.nextSetBit(0); vertexPosition >= 0; vertexPosition = updateB.nextSetBit(vertexPosition + 1)) {
                traversalB[vertexPosition].or(sendBufferB[vertexPosition]);
                traversalB[vertexPosition].set(vertexPosition);
                sendFailsB[vertexPosition].clear();
                sendFailsB[vertexPosition].or(sendBufferB[vertexPosition]);
                sendBufferB[vertexPosition].clear();
            }

            // for each neighbour, check if there is any new information it needs to receive
            for (int vertexPosition = updateF.nextSetBit(0); vertexPosition >= 0; vertexPosition = updateF.nextSetBit(vertexPosition + 1)) {
                int vertexId = graph.getVertex(vertexPosition);

                for (int vertexNeighbourPosition = 0; vertexNeighbourPosition < graph.getVertexNeighbourCount(vertexId); vertexNeighbourPosition++) {
                    int neighbourId = graph.getVertexNeighbour(vertexId, vertexNeighbourPosition);
                    int neighbourPosition = graph.getVertexPosition(neighbourId);

                    boolean isRequestedDirection = false;
                    int linkId = graph.getLink(vertexId, neighbourId);
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
                        newUpdateF.set(neighbourPosition);
                    }
                }
                for (int neighbourPosition = sendFailsF[vertexPosition].nextSetBit(0); neighbourPosition >= 0; neighbourPosition = sendFailsF[vertexPosition].nextSetBit(neighbourPosition + 1)) {
                    exclusionsF[neighbourPosition].set(vertexPosition, true);
                }
            }

            // for each neighbour, check if there is any new information it needs to receive
            for (int vertexPosition = updateB.nextSetBit(0); vertexPosition >= 0; vertexPosition = updateB.nextSetBit(vertexPosition + 1)) {
                int vertexId = graph.getVertex(vertexPosition);

                for (int vertexNeighbourPosition = 0; vertexNeighbourPosition < graph.getVertexNeighbourCount(vertexId); vertexNeighbourPosition++) {
                    int neighbourId = graph.getVertexNeighbour(vertexId, vertexNeighbourPosition);
                    int neighbourPosition = graph.getVertexPosition(neighbourId);

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
                        newUpdateB.set(neighbourPosition);
                    }
                }
                for (int neighbourPosition = sendFailsB[vertexPosition].nextSetBit(0); neighbourPosition >= 0; neighbourPosition = sendFailsB[vertexPosition].nextSetBit(neighbourPosition + 1)) {
                    exclusionsB[neighbourPosition].set(vertexPosition, true);
                }
            }

            // update scores based on the current traversal state
            switch (scoreType) {
                case BETWEENNESS:
                    updateBetweennessScoresDirected(graph, traversalF, traversalB, scores, sendBufferF, sendBufferB, exclusionsF, exclusionsB, turn, selectedOnly);
                    break;
                case CLOSENESS:
                case FARNESS:
                    updateFarnessScoresDirected(graph, traversalF, traversalB, scores, sendBufferF, sendBufferB, exclusionsF, exclusionsB, turn, selectedOnly);
                    break;
                case HARMONIC_CLOSENESS:
                case HARMONIC_FARNESS:
                    updateHarmonicFarnessScoresDirected(graph, traversalF, traversalB, scores, sendBufferF, sendBufferB, exclusionsF, exclusionsB, turn, selectedOnly);
                    break;
                default:
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

        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
            traversalF[vertexPosition].or(traversalB[vertexPosition]);
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

        return Tuple.create(traversalF, scores);
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

    private static void updateAveragePathScoresUndirected(final ArrayList<Float> distances, final float[] scores, final BitSet turn, final BitSet[] sendBuffer) {
        // for each node that has a message in transit, update its eccentricity
        for (int vxId = turn.nextSetBit(0); vxId >= 0; vxId = turn.nextSetBit(vxId + 1)) {
            scores[vxId]++;
            for (int nxId = sendBuffer[vxId].nextSetBit(0); nxId >= 0; nxId = sendBuffer[vxId].nextSetBit(nxId + 1)) {
                distances.add(scores[vxId]);
            }
        }
    }

    private static void updateAveragePathScoresScoresDirected(final ArrayList<Float> distances, final float[] scores, final BitSet turn, final BitSet[] sendBuffer) {
        // for each node that has a message in transit, update its eccentricity
        for (int vxId = turn.nextSetBit(0); vxId >= 0; vxId = turn.nextSetBit(vxId + 1)) {
            scores[vxId]++;
            for (int nxId = sendBuffer[vxId].nextSetBit(0); nxId >= 0; nxId = sendBuffer[vxId].nextSetBit(nxId + 1)) {
                distances.add(scores[vxId]);
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
}
