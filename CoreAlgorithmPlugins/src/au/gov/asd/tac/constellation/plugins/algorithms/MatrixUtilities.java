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
package au.gov.asd.tac.constellation.plugins.algorithms;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import org.ejml.simple.SimpleMatrix;

/**
 * Utilities for converting a graph into various matrices and performing linear
 * algebra operations.
 *
 * @author cygnus_x-1
 */
public class MatrixUtilities {

    private MatrixUtilities() {
        throw new IllegalStateException("Utility class");
    }
    
    public static SimpleMatrix identity(final GraphReadMethods graph) {
        return SimpleMatrix.identity(graph.getVertexCount());
    }

    public static SimpleMatrix adjacency(final GraphReadMethods graph, final boolean weighted) {
        final int vertexCount = graph.getVertexCount();
        if (vertexCount == 0) {
            return new SimpleMatrix(0, 0);
        }
        final double[][] data = new double[vertexCount][vertexCount];
        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
            final int vertexId = graph.getVertex(vertexPosition);
            final int neighbourCount = graph.getVertexNeighbourCount(vertexId);
            for (int neighbourPosition = 0; neighbourPosition < neighbourCount; neighbourPosition++) {
                final int neighbourId = graph.getVertexNeighbour(vertexId, neighbourPosition);
                final int vertexNeighbourLinkId = graph.getLink(vertexId, neighbourId);
                final int vertexNeighbourTransactionCount = graph.getLinkTransactionCount(vertexNeighbourLinkId);
                data[graph.getVertexPosition(vertexId)][graph.getVertexPosition(neighbourId)] = weighted ? vertexNeighbourTransactionCount : 1.0;
            }
        }

        return new SimpleMatrix(data);
    }

    public static SimpleMatrix incidence(final GraphReadMethods graph, final boolean weighted) {
        final int vertexCount = graph.getVertexCount();
        final int linkCount = graph.getLinkCount();
        final double[][] data = new double[vertexCount][linkCount];
        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
            final int vertexId = graph.getVertex(vertexPosition);
            final int adjacentLinkCount = graph.getVertexLinkCount(vertexId);
            for (int adjacentLinkPosition = 0; adjacentLinkPosition < adjacentLinkCount; adjacentLinkPosition++) {
                final int adjacentLinkId = graph.getVertexLink(vertexId, adjacentLinkPosition);
                final int adjacentLinkTransactionCount = graph.getLinkTransactionCount(adjacentLinkId);
                data[graph.getVertexPosition(vertexId)][graph.getLinkPosition(adjacentLinkId)] = weighted ? adjacentLinkTransactionCount : 1.0;
            }
        }

        return new SimpleMatrix(data);
    }

    public static SimpleMatrix degree(final GraphReadMethods graph) {
        final int vertexCount = graph.getVertexCount();
        final double[] data = new double[vertexCount];
        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
            final int vertexId = graph.getVertex(vertexPosition);
            final int neighbourCount = graph.getVertexNeighbourCount(vertexId);
            data[graph.getVertexPosition(vertexId)] = neighbourCount;
        }

        return SimpleMatrix.diag(data);
    }

    public static SimpleMatrix laplacian(final GraphReadMethods graph) {
        final SimpleMatrix adjacency = adjacency(graph, false);
        final SimpleMatrix degree = degree(graph);
        return degree.minus(adjacency);
    }

    public static SimpleMatrix inverseLaplacian(final GraphReadMethods graph) {
        if (graph.getVertexCount() == 0) {
            return new SimpleMatrix(0, 0);
        }
        final SimpleMatrix laplacian = laplacian(graph);
        return laplacian.pseudoInverse();
    }
}
