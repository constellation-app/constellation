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
package au.gov.asd.tac.constellation.views.scripting.graph;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementMerger;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.mergers.PrioritySurvivingGraphElementMerger;
import au.gov.asd.tac.constellation.views.scripting.graph.exceptions.NoSuchAttributeException;
import java.util.BitSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A representation of a graph write lock for use with scripting.
 *
 * @author algol
 * @author cygnus_x-1
 */
public class SWritableGraph extends SReadableGraph {

    private static final Logger LOGGER = Logger.getLogger(SWritableGraph.class.getName());

    SWritableGraph(final SGraph graph, final WritableGraph getWritableGraph) {
        super(graph, (ReadableGraph) getWritableGraph);
    }

    /**
     * Get the actual write lock on the graph that this object represents.
     *
     * @return the write lock on the graph.
     */
    protected WritableGraph getWritableGraph() {
        return (WritableGraph) readableGraph;
    }

    /**
     * Commit changes made to the graph using this write lock, then release the
     * lock. This or {@link #rollback} should be called immediately after you
     * finish writing to the graph.
     */
    public void commit() {
        getWritableGraph().commit();
        LOGGER.log(Level.FINE, "committing {0}", this);
    }

    /**
     * Rollback changes made to the graph using this write lock, then release
     * the lock. This or {@link #commit} should be called immediately after you
     * finish writing to the graph.
     */
    public void rollback() {
        getWritableGraph().rollBack();
        LOGGER.log(Level.FINE, "rolling back {0}", this);
    }

    /**
     * Add a new attribute to the graph.
     *
     * @param elementType the element type of the attribute.
     * @param attributeType the type of the attribute.
     * @param name the name of the attribute.
     * @param description a description of the attribute.
     * @param defaultValue the default value for the attribute.
     * @param attributeMergerId the merger the attribute should use.
     * @return the new attribute as a {@link SAttribute}.
     */
    public SAttribute addAttribute(final GraphElementType elementType, final String attributeType,
            final String name, final String description, final Object defaultValue, final String attributeMergerId) {
        final int attributeId = getWritableGraph().addAttribute(elementType, attributeType, name, description, defaultValue, attributeMergerId);
        return new SAttribute(readableGraph, attributeId);
    }

    /**
     * Get a schema attribute from the graph if it exists, otherwise create it.
     *
     * @param elementType the element type of the attribute.
     * @param name the name of the attribute.
     * @return the attribute as a {@link SAttribute}.
     */
    public SAttribute ensureAttribute(final GraphElementType elementType, final String name) {
        final int attributeId = readableGraph.getSchema().getFactory().ensureAttribute(getWritableGraph(), elementType, name);
        return new SAttribute(readableGraph, attributeId);
    }

    /**
     * Remove an attribute from the graph.
     *
     * @param attributeId the attribute id.
     */
    public void removeAttribute(final int attributeId) {
        getWritableGraph().removeAttribute(attributeId);
    }

    /**
     * Add a vertex to the graph.
     *
     * @return the new vertex as a {@link SVertex}.
     */
    public SVertex addVertex() {
        final int vertexId = getWritableGraph().addVertex();
        return new SVertex(readableGraph, vertexId);
    }

    /**
     * Remove a vertex from the graph.
     *
     * @param vertexId the vertex id.
     */
    public void removeVertex(final int vertexId) {
        getWritableGraph().removeVertex(vertexId);
    }

    /**
     * Remove a collection of vertices from the graph.
     *
     * @param vertices the vertices to remove as a {@link SCollection}.
     */
    public void removeVertices(final SCollection vertices) {
        final BitSet removeVertices = vertices.elementIds();
        for (int vertexId = removeVertices.nextSetBit(0); vertexId >= 0; vertexId = removeVertices.nextSetBit(vertexId + 1)) {
            getWritableGraph().removeVertex(vertexId);
        }
    }

    /**
     * Merge two vertices together.
     *
     * @param leadVertex the id of the consuming vertex.
     * @param mergeVertex the id of the consumed vertex.
     */
    public void mergeVertices(final int leadVertex, final int mergeVertex) {
        final BitSet vertices = new BitSet();
        vertices.set(mergeVertex);
        mergeVertices(leadVertex, new SCollection(graph.getEngine(), getReadableGraph(), GraphElementType.VERTEX, vertices));
    }

    /**
     * Merge more than two vertices together.
     *
     * @param leadVertex the id of the consuming vertex.
     * @param vertices the consumed vertices as a {@link SCollection}.
     */
    public void mergeVertices(final int leadVertex, final SCollection vertices) {
        final GraphElementMerger merger = new PrioritySurvivingGraphElementMerger();
        final BitSet mergeVertices = vertices.elementIds();
        for (int vertexId = mergeVertices.nextSetBit(0); vertexId >= 0; vertexId = mergeVertices.nextSetBit(vertexId + 1)) {
            if (vertexId != leadVertex) {
                merger.mergeElement(getWritableGraph(), GraphElementType.VERTEX, leadVertex, vertexId);
            }
        }
    }

    /**
     * Add a transaction to the graph.
     *
     * @param sourceVertex the id of the source vertex.
     * @param destinationVertex the id of the destination vertex.
     * @param directed should the transaction be directed?
     * @return the new transaction as a {@link STransaction}.
     */
    public STransaction addTransaction(final SVertex sourceVertex, final SVertex destinationVertex, final boolean directed) {
        final int transaction = getWritableGraph().addTransaction(sourceVertex.id(), destinationVertex.id(), directed);
        return new STransaction(readableGraph, transaction);
    }

    /**
     * Remove a transaction from the graph.
     *
     * @param transaction the id of the transaction.
     */
    public void removeTransaction(final int transaction) {
        getWritableGraph().removeTransaction(transaction);
    }

    /**
     * Remove a collection of transactions from the graph.
     *
     * @param transactions the transaction to remove as a {@link STransaction}.
     */
    public void removeTransactions(final SCollection transactions) {
        final BitSet transactionIds = transactions.elementIds();
        for (int transaction = transactionIds.nextSetBit(0); transaction >= 0; transaction = transactionIds.nextSetBit(transaction + 1)) {
            getWritableGraph().removeTransaction(transaction);
        }
    }

    /**
     * Python assignment to self[key].
     *
     * @param key an object representing the attribute to set.
     * @param value the new value for the specified attribute.
     * @return the set value.
     */
    public Object __setitem__(final Object key, final Object value) {
        final int attributeId = readableGraph.getSchema() != null
                ? readableGraph.getSchema().getFactory().ensureAttribute(getWritableGraph(), GraphElementType.GRAPH, (String) key)
                : Graph.NOT_FOUND;
        if (attributeId == Graph.NOT_FOUND) {
            throw new NoSuchAttributeException(key);
        }
        getWritableGraph().setObjectValue(attributeId, 0, value);
        return value;
    }

    /**
     * The entry point for the Python context manager.
     *
     * @return a write lock on the graph.
     */
    @Override
    public SWritableGraph __enter__() {
        LOGGER.log(Level.FINE, "__enter__ {0}", this);
        return this;
    }

    /**
     * The exit point for the Python context manager.
     *
     * exc_Type exc_type the exception type.
     * @param excValue the exception value.
     * @param traceback the exception traceback.
     */
    @Override
    public void __exit__(final Object exc_Type, final Object excValue, final Object traceback) {
        LOGGER.log(Level.FINE, "__exit__ {0}", this);
        LOGGER.log(Level.FINE, "{0} {1} {2}", new Object[]{exc_Type, excValue, traceback});
        if (exc_Type == null) {
            commit();
        } else {
            rollback();
        }
    }

    @Override
    public String toString() {
        return String.format("[%s: %d; v: %d t: %d e: %d l: %d]",
                this.getClass().getSimpleName(), hashCode(),
                readableGraph.getVertexCount(),
                readableGraph.getTransactionCount(),
                readableGraph.getEdgeCount(),
                readableGraph.getLinkCount());
    }
}
