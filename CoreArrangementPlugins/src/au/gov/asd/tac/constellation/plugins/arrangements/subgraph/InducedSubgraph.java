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
package au.gov.asd.tac.constellation.plugins.arrangements.subgraph;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import java.util.Arrays;
import java.util.Set;

/**
 *
 * @author twilight_sparkle
 */
public final class InducedSubgraph extends ComponentSubgraph {

    public InducedSubgraph(final GraphWriteMethods proxy, final Set<Integer> includedVertexIDs) {
        super(proxy, includedVertexIDs);

        final int vertexCapacity = getVertexCapacity();
        vertexNeighbours = new int[vertexCapacity][];
        vertexLinks = new int[vertexCapacity][];
        vertexEdges = new int[vertexCapacity][];
        vertexTransactions = new int[vertexCapacity][];
    }

    private final int[][] vertexNeighbours;
    private final int[][] vertexLinks;
    private final int[][] vertexEdges;
    private final int[][] vertexTransactions;

    public static SubgraphFactory getSubgraphFactory() {
        return (final GraphWriteMethods wg, final Set<Integer> vertexIDs) -> new InducedSubgraph(wg, vertexIDs);
    }

    private void calculateNeighbours(final int vertex) {
        final int[] neighbours = new int[proxy.getVertexNeighbourCount(vertex)];
        int pos = 0;
        for (int i = 0; i < proxy.getVertexNeighbourCount(vertex); i++) {
            final int neighbourID = proxy.getVertexNeighbour(vertex, i);
            if (includedVertexIDs.contains(neighbourID)) {
                neighbours[pos++] = neighbourID;
            }
        }
        vertexNeighbours[vertex] = Arrays.copyOf(neighbours, pos);
    }

    private void calculateLinks(final int vertex) {
        final int[] links = new int[proxy.getVertexLinkCount(vertex)];
        int pos = 0;
        for (int i = 0; i < proxy.getVertexLinkCount(vertex); i++) {
            final int lxID = proxy.getVertexLink(vertex, i);
            final int neighbourID = getLinkLowVertex(lxID) == vertex ? getLinkHighVertex(lxID) : getLinkLowVertex(lxID);
            if (includedVertexIDs.contains(neighbourID)) {
                links[pos++] = lxID;
            }
        }
        vertexLinks[vertex] = Arrays.copyOf(links, pos);
    }

    private void calculateEdges(final int vertex) {
        final int[] edges = new int[proxy.getVertexEdgeCount(vertex)];
        int pos = 0;
        for (int i = 0; i < proxy.getVertexEdgeCount(vertex); i++) {
            final int exID = proxy.getVertexEdge(vertex, i);
            final int neighbourID = getEdgeSourceVertex(exID) == vertex ? getEdgeDestinationVertex(exID) : getEdgeSourceVertex(exID);
            if (includedVertexIDs.contains(neighbourID)) {
                edges[pos++] = exID;
            }
        }
        vertexEdges[vertex] = Arrays.copyOf(edges, pos);
    }

    private void calculateTransactions(final int vertex) {
        final int[] transactions = new int[proxy.getVertexTransactionCount(vertex)];
        int pos = 0;
        for (int i = 0; i < proxy.getVertexTransactionCount(vertex); i++) {
            final int txID = proxy.getVertexTransaction(vertex, i);
            final int neighbourID = getTransactionSourceVertex(txID) == vertex ? getTransactionDestinationVertex(txID) : getTransactionSourceVertex(txID);
            if (includedVertexIDs.contains(neighbourID)) {
                transactions[pos++] = txID;
            }
        }
        vertexTransactions[vertex] = Arrays.copyOf(transactions, pos);
    }

    @Override
    public int getVertexNeighbourCount(final int vertex) {
        if (vertexNeighbours[vertex] == null) {
            calculateNeighbours(vertex);
        }
        return vertexNeighbours[vertex].length;
    }

    @Override
    public int getVertexNeighbour(final int vertex, final int position) {
        if (vertexNeighbours[vertex] == null) {
            calculateNeighbours(vertex);
        }
        return vertexNeighbours[vertex][position];
    }

    @Override
    public int getVertexLinkCount(final int vertex) {
        if (vertexLinks[vertex] == null) {
            calculateLinks(vertex);
        }
        return vertexLinks[vertex].length;
    }

    @Override
    public int getVertexLink(final int vertex, final int position) {
        if (vertexLinks[vertex] == null) {
            calculateLinks(vertex);
        }
        return vertexLinks[vertex][position];
    }

    @Override
    public int getVertexEdgeCount(final int vertex) {
        if (vertexEdges[vertex] == null) {
            calculateEdges(vertex);
        }
        return vertexEdges[vertex].length;
    }

    @Override
    public int getVertexEdge(final int vertex, final int position) {
        if (vertexEdges[vertex] == null) {
            calculateEdges(vertex);
        }
        return vertexEdges[vertex][position];
    }

    @Override
    public int getVertexTransactionCount(final int vertex, final int direction) {
        if (vertexTransactions[vertex] == null) {
            calculateTransactions(vertex);
        }
        return vertexTransactions[vertex].length;
    }

    @Override
    public int getVertexTransaction(final int vertex, final int direction, final int position) {
        if (vertexTransactions[vertex] == null) {
            calculateTransactions(vertex);
        }
        return vertexTransactions[vertex][position];
    }
}
