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
package au.gov.asd.tac.constellation.views.scripting.graph;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.views.scripting.graph.exceptions.NoSuchAttributeException;
import au.gov.asd.tac.constellation.views.scripting.graph.iterators.SAttributeIterator;
import au.gov.asd.tac.constellation.views.scripting.graph.iterators.SEdgeTransactionIterator;

/**
 * A representation of an edge for use with scripting.
 *
 * @author cygnus_x-1
 */
public class SEdge {

    private final GraphReadMethods readableGraph;
    private final int id;

    public SEdge(final GraphReadMethods readableGraph, final int id) {
        this.readableGraph = readableGraph;
        this.id = id;
    }

    private GraphWriteMethods writableGraph() {
        return (GraphWriteMethods) readableGraph;
    }

    /**
     * Get the id of this edge.
     *
     * @return the id of this edge.
     */
    public int id() {
        return id;
    }

    /**
     * Get the direction of this edge.
     *
     * @return 0 if the direction is 'uphill' (the id of the source vertex is
     * lower than the id of the destination vertex), 1 if the direction is
     * 'downhill' (the id of the source vertex is higher than the id of the
     * destination vertex), or 2 if the direction is 'flat' (the source id and
     * destination id are equal, ie. a loop).
     */
    public int direction() {
        return readableGraph.getEdgeDirection(id);
    }

    /**
     * Get an iterator over attributes attached to this edge.
     *
     * @return an attribute iterator.
     */
    public SAttributeIterator attributes() {
        return new SAttributeIterator(readableGraph, GraphElementType.EDGE);
    }

    /**
     * Get the source vertex of this edge.
     *
     * @return the source vertex as a {@link SVertex}.
     */
    public SVertex sourceVertex() {
        final int vxId = readableGraph.getEdgeSourceVertex(id);
        return new SVertex(readableGraph, vxId);
    }

    /**
     * Get the destination vertex of this edge.
     *
     * @return the destination vertex as a {@link SVertex}.
     */
    public SVertex destinationVertex() {
        final int vxId = readableGraph.getEdgeDestinationVertex(id);
        return new SVertex(readableGraph, vxId);
    }

    /**
     * Get the other vertex of this edge given one end.
     *
     * @param vertex the vertex at one end of this edge.
     * @return the vertex at the other end of this edge as a {@link SVertex}.
     */
    public SVertex otherVertex(final SVertex vertex) {
        final int otherVertex = GraphElementType.EDGE.getOtherVertex(readableGraph, id, vertex.id());
        return new SVertex(readableGraph, otherVertex);
    }

    /**
     * Get the number of transactions this edge represents.
     *
     * @return the number of transactions.
     */
    public int transactionCount() {
        return readableGraph.getTransactionCount();
    }

    /**
     * Get an iterator over transactions this edge represents.
     *
     * @return a transaction iterator.
     */
    public SEdgeTransactionIterator transactions() {
        return new SEdgeTransactionIterator(readableGraph, id);
    }

    /**
     * Python evaluation of self[key].
     *
     * @param key an object representing the attribute to query.
     * @return the value of the specified attribute.
     */
    public Object __getitem__(final Object key) {
        final int attributeId = readableGraph.getAttribute(GraphElementType.EDGE, (String) key);
        if (attributeId == Graph.NOT_FOUND) {
            throw new NoSuchAttributeException(key);
        }
        return readableGraph.getObjectValue(attributeId, id);
    }

    /**
     * Python assignment to self[key].
     *
     * @param key an object representing the attribute to set.
     * @param value the new value for the specified attribute.
     * @return the value that was set.
     */
    public Object __setitem__(final Object key, final Object value) {
        final int attributeId = writableGraph().getSchema() != null
                ? writableGraph().getSchema().getFactory().ensureAttribute(writableGraph(), GraphElementType.EDGE, (String) key)
                : Graph.NOT_FOUND;
        if (attributeId == Graph.NOT_FOUND) {
            throw new NoSuchAttributeException(key);
        }
        writableGraph().setObjectValue(attributeId, id, value);
        return value;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SEdge other = (SEdge) obj;
        return this.id == other.id;
    }

    @Override
    public String toString() {
        return String.format("[%s: %d]", this.getClass().getSimpleName(), id);
    }
}
