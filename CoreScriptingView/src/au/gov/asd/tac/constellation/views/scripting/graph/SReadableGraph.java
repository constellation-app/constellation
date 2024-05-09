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
import au.gov.asd.tac.constellation.graph.GraphConstants;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.views.scripting.graph.exceptions.NoSuchAttributeException;
import au.gov.asd.tac.constellation.views.scripting.graph.iterators.SAttributeIterator;
import au.gov.asd.tac.constellation.views.scripting.graph.iterators.SEdgeIterator;
import au.gov.asd.tac.constellation.views.scripting.graph.iterators.SLinkIterator;
import au.gov.asd.tac.constellation.views.scripting.graph.iterators.STransactionIterator;
import au.gov.asd.tac.constellation.views.scripting.graph.iterators.SVertexIterator;
import java.util.BitSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptContext;
import javax.script.ScriptException;

/**
 * A representation of a read lock on a graph for use with scripting.
 *
 * @author algol
 * @author cygnus_x-1
 */
public class SReadableGraph {

    private static final Logger LOGGER = Logger.getLogger(SReadableGraph.class.getName());

    protected final SGraph graph;
    protected final ReadableGraph readableGraph;

    private static final String FUNC = "__func";
    private static final String FUNC_P1 = "__func(__p1)";

    SReadableGraph(final SGraph graph, final ReadableGraph readableGraph) {
        this.graph = graph;
        this.readableGraph = readableGraph;
    }

    /**
     * Get the actual read lock on the graph that this object represents.
     *
     * @return the read lock on the graph.
     */
    protected ReadableGraph getReadableGraph() {
        return readableGraph;
    }

    /**
     * Release the read lock on the graph. This should be called immediately
     * after you finish reading from the graph.
     */
    public void release() {
        readableGraph.release();
    }

    /**
     * Get a count representing the number of modifications that have occurred
     * globally on the graph.
     *
     * @return the current global modification count.
     */
    public long globalModificationCounter() {
        return readableGraph.getGlobalModificationCounter();
    }

    /**
     * Get a count representing the number of modifications that have occurred
     * which modified the structure of the graph.
     *
     * @return the current structure modification count.
     */
    public long structureModificationCounter() {
        return readableGraph.getStructureModificationCounter();
    }

    /**
     * Get a count representing the number of modifications that have occurred
     * which modified attributes on the graph.
     *
     * @return the current attribute modification count.
     */
    public long attributeModificationCounter() {
        return readableGraph.getAttributeModificationCounter();
    }

    /**
     * Get a count representing the number of modifications that have occurred
     * to the value of a single attribute on the graph.
     *
     * @param type the element type of attribute.
     * @param name the name of the attribute.
     * @return the current attribute value modification count.
     */
    public long valueModificationCounter(final GraphElementType type, final String name) {
        final int attribute = readableGraph.getAttribute(type, name);
        return readableGraph.getValueModificationCounter(attribute);
    }

    /**
     * Get the number of attributes on the graph for a specific element type.
     *
     * @param elementType the element type.
     * @return the number of attributes for the given element type.
     */
    public int attributeCount(final GraphElementType elementType) {
        return readableGraph.getAttributeCount(elementType);
    }

    /**
     * Check if the specified attribute exists on the graph.
     *
     * @param elementType the element type of the attribute
     * @param name the name of the attribute
     * @return true if the attribute exists, false otherwise
     */
    public boolean hasAttribute(final GraphElementType elementType, final String name) {
        return readableGraph.getAttribute(elementType, name) != GraphConstants.NOT_FOUND;
    }

    /**
     * Get an attribute from the graph by id.
     *
     * @param attributeId the id of the attribute.
     * @return the requested attribute as a {@link SAttribute}.
     */
    public SAttribute attribute(final int attributeId) {
        return new SAttribute(readableGraph, attributeId);
    }

    /**
     * Get an attribute from the graph by name.
     *
     * @param elementType the element type of the attribute.
     * @param name the name of the attribute.
     * @return the requested attribute.
     * @throws NoSuchAttributeException
     */
    public SAttribute attribute(final GraphElementType elementType, final String name) throws NoSuchAttributeException {
        if (hasAttribute(elementType, name)) {
            return new SAttribute(readableGraph, elementType, name);
        } else {
            throw new NoSuchAttributeException(name);
        }
    }

    /**
     * Get an iterator over attributes of the specified element type.
     *
     * @param elementType the element type.
     * @return an attribute iterator.
     */
    public SAttributeIterator attributes(final GraphElementType elementType) {
        return new SAttributeIterator(readableGraph, elementType);
    }

    /**
     * Get the number of vertices on the graph.
     *
     * @return the number of vertices.
     */
    public int vertexCount() {
        return readableGraph.getVertexCount();
    }

    /**
     * Check if the specified vertex exists on the graph.
     *
     * @param vertexId the vertex id.
     * @return true if the vertex exists, false otherwise.
     */
    public boolean hasVertex(final int vertexId) {
        return readableGraph.vertexExists(vertexId);
    }

    /**
     * Get a vertex on the graph by id.
     *
     * @param vertexId the vertex id.
     * @return the vertex as a {link SVertex}.
     */
    public SVertex vertex(final int vertexId) {
        return new SVertex(readableGraph, vertexId);
    }

    /**
     * Get an iterator over vertices.
     *
     * @return a vertex iterator.
     */
    public SVertexIterator vertices() {
        return new SVertexIterator(readableGraph);
    }

    /**
     * Evaluate a function against each vertex on the graph.
     *
     * Note: This method will only work for Python scripts as it makes use of
     * Python specific syntax.
     *
     * @param callback the function to evaluate.
     * @throws ScriptException
     */
    public void withVertices(final Object callback) throws ScriptException {
        try {
            final ScriptContext context = graph.getEngine().getContext();
            context.setAttribute(FUNC, callback, ScriptContext.ENGINE_SCOPE);

            final int vertexCount = readableGraph.getVertexCount();
            for (int position = 0; position < vertexCount; position++) {
                final int vertexId = readableGraph.getVertex(position);

                context.setAttribute("__p1", new SVertex(readableGraph, vertexId), ScriptContext.ENGINE_SCOPE);
                graph.getEngine().eval(FUNC_P1);
            }
        } finally {
            readableGraph.release();
        }
    }

    /**
     * Evaluate a function against each vertex on the graph in order to gather a
     * filtered collection. The provided function must only return true or
     * false.
     *
     * Note: This method will only work for Python scripts as it makes use of
     * Python specific syntax.
     *
     * @param callback a function which returns true or false.
     * @return a collection of vertices as a {@link SCollection}.
     * @throws ScriptException
     */
    public SCollection filterVertices(final Object callback) throws ScriptException {
        final ScriptContext context = graph.getEngine().getContext();
        context.setAttribute(FUNC, callback, ScriptContext.ENGINE_SCOPE);

        final BitSet vertices = new BitSet();
        final int vertexCount = readableGraph.getVertexCount();
        for (int position = 0; position < vertexCount; position++) {
            final int vertexId = readableGraph.getVertex(position);

            context.setAttribute("__p1", new SVertex(readableGraph, vertexId), ScriptContext.ENGINE_SCOPE);
            final Object b = graph.getEngine().eval(FUNC_P1);
            if (Boolean.TRUE.equals(b)) {
                vertices.set(vertexId);
            }
        }

        return new SCollection(graph.getEngine(), readableGraph, GraphElementType.VERTEX, vertices);
    }

    /**
     * Get the number of transactions on the graph.
     *
     * @return the number of transactions.
     */
    public int transactionCount() {
        return readableGraph.getTransactionCount();
    }

    /**
     * Check if the specified transaction exists on the graph.
     *
     * @param transactionId the transaction id.
     * @return true if the transaction exists, false otherwise.
     */
    public boolean hasTransaction(final int transactionId) {
        return readableGraph.transactionExists(transactionId);
    }

    /**
     * Get a transaction on the graph by id.
     *
     * @param transactionId the transaction id.
     * @return the transaction as a {link STransaction}.
     */
    public STransaction transaction(final int transactionId) {
        return new STransaction(readableGraph, transactionId);
    }

    /**
     * Get an iterator over transactions.
     *
     * @return a transaction iterator.
     */
    public STransactionIterator transactions() {
        return new STransactionIterator(readableGraph);
    }

    /**
     * Evaluate a function against each transaction on the graph.
     *
     * Note: This method will only work for Python scripts as it makes use of
     * Python specific syntax.
     *
     * @param callback the function to evaluate.
     * @throws ScriptException
     */
    public void withTransactions(final Object callback) throws ScriptException {
        try {
            final ScriptContext context = graph.getEngine().getContext();
            context.setAttribute(FUNC, callback, ScriptContext.ENGINE_SCOPE);

            final int transactionCount = readableGraph.getTransactionCount();
            for (int position = 0; position < transactionCount; position++) {
                final int transaction = readableGraph.getTransaction(position);

                context.setAttribute("__p1", new STransaction(readableGraph, transaction), ScriptContext.ENGINE_SCOPE);
                graph.getEngine().eval(FUNC_P1);
            }
        } finally {
            readableGraph.release();
        }
    }

    /**
     * Evaluate a function against each transaction on the graph in order to
     * gather a filtered collection. The provided function must only return true
     * or false.
     *
     * Note: This method will only work for Python scripts as it makes use of
     * Python specific syntax.
     *
     * @param callback a function which returns true or false.
     * @return a collection of transactions as a {@link SCollection}.
     * @throws ScriptException
     */
    public SCollection filterTransactions(final Object callback) throws ScriptException {
        final ScriptContext context = graph.getEngine().getContext();
        context.setAttribute(FUNC, callback, ScriptContext.ENGINE_SCOPE);

        final BitSet transactions = new BitSet();
        final int transactionCount = readableGraph.getTransactionCount();
        for (int position = 0; position < transactionCount; position++) {
            final int transaction = readableGraph.getTransaction(position);

            context.setAttribute("__p1", new STransaction(readableGraph, transaction), ScriptContext.ENGINE_SCOPE);
            final Object b = graph.getEngine().eval(FUNC_P1);
            if (Boolean.TRUE.equals(b)) {
                transactions.set(transaction);
            }
        }

        return new SCollection(graph.getEngine(), readableGraph, GraphElementType.VERTEX, transactions);
    }

    /**
     * Get the number of edges on the graph.
     *
     * @return the number of edges.
     */
    public int edgeCount() {
        return readableGraph.getEdgeCount();
    }

    /**
     * Check if the specified edge exists on the graph.
     *
     * @param edgeId the edge id.
     * @return true if the edge exists, false otherwise.
     */
    public boolean hasEdge(final int edgeId) {
        return readableGraph.edgeExists(edgeId);
    }

    /**
     * Get a edge on the graph by id.
     *
     * @param edgeId the edge id.
     * @return the edge as a {link SEdge}.
     */
    public SEdge edge(final int edgeId) {
        return new SEdge(readableGraph, edgeId);
    }

    /**
     * Get an iterator over edges.
     *
     * @return a edge iterator.
     */
    public SEdgeIterator edges() {
        return new SEdgeIterator(readableGraph);
    }

    /**
     * Get the number of links on the graph.
     *
     * @return the number of links.
     */
    public int linkCount() {
        return readableGraph.getLinkCount();
    }

    /**
     * Check if the specified link exists on the graph.
     *
     * @param linkId the link id.
     * @return true if the link exists, false otherwise.
     */
    public boolean hasLink(final int linkId) {
        return readableGraph.linkExists(linkId);
    }

    /**
     * Get a link on the graph by id.
     *
     * @param linkId the link id.
     * @return the link as a {link SLink}.
     */
    public SLink link(final int linkId) {
        return new SLink(readableGraph, linkId);
    }

    /**
     * Get a link on the graph by its endpoints.
     *
     * @param sourceVertex the source vertex as a {@link SVertex}.
     * @param destinationVertex the destination vertex as a {@link SVertex}.
     * @return the link as a {link SLink}.
     */
    public SLink link(final SVertex sourceVertex, final SVertex destinationVertex) {
        final int link = readableGraph.getLink(sourceVertex.id(), destinationVertex.id());
        return link == Graph.NOT_FOUND ? null : new SLink(readableGraph, link);
    }

    /**
     * Get an iterator over links.
     *
     * @return a link iterator.
     */
    public SLinkIterator links() {
        return new SLinkIterator(readableGraph);
    }

    /**
     * Python assignment to self[key].
     *
     * @param key an object representing the attribute to query.
     * @return the value of the specified attribute.
     */
    protected Object __getitem__(final Object key) {
        final int attributeId = readableGraph.getAttribute(GraphElementType.GRAPH, (String) key);
        if (attributeId == Graph.NOT_FOUND) {
            throw new NoSuchAttributeException(key);
        }

        return readableGraph.getObjectValue(attributeId, 0);
    }

    /**
     * The entry point for the Python context manager.
     *
     * @return a read lock on the graph.
     */
    protected SReadableGraph __enter__() {
        LOGGER.log(Level.INFO, "__enter__ {0}", this);
        return this;
    }

    /**
     * The exit point for the Python context manager.
     *
     * @param excType the exception type.
     * @param excValue the exception value.
     * @param traceback the exception traceback.
     */
    protected void __exit__(final Object excType, final Object excValue, final Object traceback) {
        LOGGER.log(Level.INFO, "__exit__ {0}", this);
        LOGGER.log(Level.INFO, "    {0} {1} {2}", new Object[]{excType, excValue, traceback});
        readableGraph.release();
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
