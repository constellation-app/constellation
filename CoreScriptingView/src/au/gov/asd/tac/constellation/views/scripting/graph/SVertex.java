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
import au.gov.asd.tac.constellation.views.scripting.graph.iterators.SVertexEdgeIterator;
import au.gov.asd.tac.constellation.views.scripting.graph.iterators.SVertexLinkIterator;
import au.gov.asd.tac.constellation.views.scripting.graph.iterators.SVertexTransactionIterator;

/**
 * A representation of a vertex for use with scripting.
 *
 * @author algol
 * @author cygnus_x-1
 */
public class SVertex {

    private final GraphReadMethods readableGraph;
    private final int id;

    public SVertex(final GraphReadMethods hlgraph, final int id) {
        this.readableGraph = hlgraph;
        this.id = id;
    }

    private GraphWriteMethods writableGraph() {
        return (GraphWriteMethods) readableGraph;
    }

    /**
     * Get the id of this vertex.
     *
     * @return the id of this vertex.
     */
    public int id() {
        return id;
    }

    /**
     * Initialise this vertex using the schema of the graph.
     */
    public void init() {
        writableGraph().getSchema().newVertex(writableGraph(), id);
    }

    /**
     * Complete this vertex using the schema of the graph.
     */
    public void complete() {
        writableGraph().getSchema().completeVertex(writableGraph(), id);
    }

    /**
     * Get an iterator over attributes attached to this vertex.
     *
     * @return an attribute iterator.
     */
    public SAttributeIterator attributes() {
        return new SAttributeIterator(readableGraph, GraphElementType.VERTEX);
    }

    /**
     * Get the number of transactions connected to this vertex.
     *
     * @return the number of transactions connected to this vertex.
     */
    public int transactionCount() {
        return readableGraph.getVertexTransactionCount(id);
    }

    /**
     * Get an iterator over transactions attached to this vertex.
     *
     * @return a transaction iterator.
     */
    public SVertexTransactionIterator transactions() {
        return new SVertexTransactionIterator(readableGraph, id);
    }

    /**
     * Get the number of edges connected to this vertex.
     *
     * @return the number of edges connected to this vertex.
     */
    public int edgeCount() {
        return readableGraph.getVertexEdgeCount(id);
    }

    /**
     * Get an iterator over edges attached to this vertex.
     *
     * @return a edge iterator.
     */
    public SVertexEdgeIterator edges() {
        return new SVertexEdgeIterator(readableGraph, id);
    }

    /**
     * Get the number of links connected to this vertex.
     *
     * @return the number of links connected to this vertex.
     */
    public int linkCount() {
        return readableGraph.getVertexLinkCount(id);
    }

    /**
     * Get an iterator over links attached to this vertex.
     *
     * @return a link iterator.
     */
    public SVertexLinkIterator links() {
        return new SVertexLinkIterator(readableGraph, id);
    }

    /**
     * Python evaluation of self[key].
     *
     * @param key an object representing the attribute to be queried.
     * @return the value of the specified attribute.
     */
    public Object __getitem__(final Object key) {
        final int attrId = readableGraph.getAttribute(GraphElementType.VERTEX, (String) key);
        if (attrId == Graph.NOT_FOUND) {
            throw new NoSuchAttributeException(key);
        }
        return readableGraph.getObjectValue(attrId, id);
    }

    /**
     * Python assignment to self[key].
     *
     * @param key an object representing the attribute to be set.
     * @param value the new value for the specified attribute.
     * @return the set value.
     */
    public Object __setitem__(final Object key, final Object value) {
        final int attrId = writableGraph().getSchema() != null
                ? writableGraph().getSchema().getFactory().ensureAttribute(writableGraph(), GraphElementType.VERTEX, (String) key)
                : Graph.NOT_FOUND;
        if (attrId == Graph.NOT_FOUND) {
            throw new NoSuchAttributeException(key);
        }
        writableGraph().setObjectValue(attrId, id, value);
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
        final SVertex other = (SVertex) obj;
        return this.id == other.id;
    }

    @Override
    public String toString() {
        return String.format("[%s: %d]", this.getClass().getSimpleName(), id);
    }
}
