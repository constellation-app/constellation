/*
 * Copyright 2010-2022 Australian Signals Directorate
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

/**
 * A kind of connection (link, edge, or transaction).
 * <p>
 * When accessing a graph, rather than using links, edges, or transactions
 * directly, client code might want to work with "connections", without
 * bothering to know which particular kind of connection it is.
 * <p>
 * This enum allows such generic coding to be done.
 *
 * @author algol
 */
public enum ConnectionMode {

    /**
     * A connection representing all transactions between two vertices.
     */
    LINK {
        @Override
        public int getConnection(final GraphReadMethods graph, final int position) {
            return graph.getLink(position);
        }

        @Override
        public int getConnectionCount(final GraphReadMethods reader) {
            return reader.getLinkCount();
        }

        @Override
        public int getConnectionCapacity(final GraphReadMethods reader) {
            return reader.getLinkCapacity();
        }

        @Override
        public int getConnectionSourceVertex(final GraphReadMethods reader, final int connectionId) {
            return reader.getLinkLowVertex(connectionId);
        }

        @Override
        public int getConnectionDestinationVertex(final GraphReadMethods reader, final int connectionId) {
            return reader.getLinkHighVertex(connectionId);
        }

        @Override
        public int getConnectionCountPerLink(final GraphReadMethods reader, final int linkId) {
            return 1;
        }

        @Override
        public int getConnectionLink(final GraphReadMethods reader, final int connectionId) {
            return connectionId;
        }

        @Override
        public int getFirstTransaction(final GraphReadMethods reader, final int connectionId) {
            return reader.getLinkTransaction(connectionId, 0);
        }

        @Override
        public int getVertexConnectionCount(final GraphReadMethods graph, final int vertexId) {
            return graph.getVertexLinkCount(vertexId);
        }

        @Override
        public int getVertexConnection(final GraphReadMethods graph, final int vertexId, final int position) {
            return graph.getVertexLink(vertexId, position);
        }
    },
    /**
     * A connection representing all transactions in one direction between two
     * vertices.
     */
    EDGE {
        @Override
        public int getConnection(final GraphReadMethods graph, final int position) {
            return graph.getEdge(position);
        }

        @Override
        public int getConnectionCount(final GraphReadMethods reader) {
            return reader.getEdgeCount();
        }

        @Override
        public int getConnectionCapacity(final GraphReadMethods reader) {
            return reader.getEdgeCapacity();
        }

        @Override
        public int getConnectionSourceVertex(final GraphReadMethods reader, final int connectionId) {
            return reader.getEdgeSourceVertex(connectionId);
        }

        @Override
        public int getConnectionDestinationVertex(final GraphReadMethods reader, final int connectionId) {
            return reader.getEdgeDestinationVertex(connectionId);
        }

        @Override
        public int getConnectionCountPerLink(final GraphReadMethods reader, final int linkId) {
            return reader.getLinkEdgeCount(linkId);
        }

        @Override
        public int getConnectionLink(final GraphReadMethods reader, final int connectionId) {
            return reader.getEdgeLink(connectionId);
        }

        @Override
        public int getFirstTransaction(final GraphReadMethods reader, final int connectionId) {
            return reader.getEdgeTransaction(connectionId, 0);
        }

        @Override
        public int getVertexConnectionCount(final GraphReadMethods graph, final int vertexId) {
            return graph.getVertexEdgeCount(vertexId);
        }

        @Override
        public int getVertexConnection(final GraphReadMethods graph, final int vertexId, final int position) {
            return graph.getVertexEdge(vertexId, position);
        }
    },
    /**
     * A connection representing a single transaction between two vertices.
     */
    TRANSACTION {
        @Override
        public int getConnection(final GraphReadMethods graph, final int position) {
            return graph.getTransaction(position);
        }

        @Override
        public int getConnectionCount(final GraphReadMethods reader) {
            return reader.getTransactionCount();
        }

        @Override
        public int getConnectionCapacity(final GraphReadMethods reader) {
            return reader.getTransactionCapacity();
        }

        @Override
        public int getConnectionSourceVertex(final GraphReadMethods reader, final int connectionId) {
            return reader.getTransactionSourceVertex(connectionId);
        }

        @Override
        public int getConnectionDestinationVertex(final GraphReadMethods reader, final int connectionId) {
            return reader.getTransactionDestinationVertex(connectionId);
        }

        @Override
        public int getConnectionCountPerLink(final GraphReadMethods reader, final int linkId) {
            return reader.getLinkTransactionCount(linkId);
        }

        @Override
        public int getConnectionLink(final GraphReadMethods reader, final int connectionId) {
            return reader.getTransactionLink(connectionId);
        }

        @Override
        public int getFirstTransaction(final GraphReadMethods reader, final int connectionId) {
            return connectionId;
        }

        @Override
        public int getVertexConnectionCount(final GraphReadMethods graph, final int vertexId) {
            return graph.getVertexTransactionCount(vertexId);
        }

        @Override
        public int getVertexConnection(final GraphReadMethods graph, final int vertexId, final int position) {
            return graph.getVertexTransaction(vertexId, position);
        }
    };

    public abstract int getConnection(GraphReadMethods graph, int position);

    public abstract int getConnectionCount(GraphReadMethods reader);

    public abstract int getConnectionCapacity(GraphReadMethods reader);

    public abstract int getConnectionSourceVertex(GraphReadMethods reader, int connectionId);

    public abstract int getConnectionDestinationVertex(GraphReadMethods reader, int connectionId);

    public abstract int getConnectionCountPerLink(GraphReadMethods reader, int linkId);

    public abstract int getConnectionLink(GraphReadMethods reader, int connectionId);

    public abstract int getFirstTransaction(GraphReadMethods reader, int connectionId);

    public abstract int getVertexConnectionCount(GraphReadMethods graph, int vertexId);

    public abstract int getVertexConnection(GraphReadMethods graph, int vertexId, int position);
}
