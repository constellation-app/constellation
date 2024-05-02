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
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.views.scripting.graph.exceptions.NoSuchAttributeException;
import au.gov.asd.tac.constellation.views.scripting.graph.iterators.SAttributeIterator;

/**
 * A representation of a transaction for use with scripting.
 *
 * @author algol
 * @author cygnus_x-1
 */
public class STransaction {

    private final GraphReadMethods readableGraph;
    private final int id;

    public STransaction(final GraphReadMethods rg, final int id) {
        this.readableGraph = rg;
        this.id = id;
    }

    private GraphWriteMethods writableGraph() {
        return (GraphWriteMethods) readableGraph;
    }

    /**
     * Get the id of this transaction.
     *
     * @return the id of this transaction.
     */
    public int id() {
        return id;
    }

    /**
     * Initialise this transaction using the schema of the graph.
     */
    public void init() {
        writableGraph().getSchema().newTransaction(writableGraph(), id);
    }

    /**
     * Complete this transaction using the schema of the graph.
     */
    public void complete() {
        writableGraph().getSchema().completeTransaction(writableGraph(), id);
    }

    /**
     * Get the direction of this transaction.
     *
     * @return 0 if the direction is 'uphill' (the id of the source vertex is
     * lower than the id of the destination vertex), 1 if the direction is
     * 'downhill' (the id of the source vertex is higher than the id of the
     * destination vertex), or 2 if the direction is 'flat' (the source id and
     * destination id are equal, ie. a loop).
     */
    public int direction() {
        return readableGraph.getTransactionDirection(id);
    }

    /**
     * Get an iterator over attributes attached to this transaction.
     *
     * @return an attribute iterator.
     */
    public SAttributeIterator attributes() {
        return new SAttributeIterator(readableGraph, GraphElementType.TRANSACTION);
    }

    /**
     * Get the source vertex of this transaction.
     *
     * @return the source vertex as a {@link SVertex}.
     */
    public SVertex sourceVertex() {
        final int vxId = readableGraph.getTransactionSourceVertex(id);
        return new SVertex(readableGraph, vxId);
    }

    /**
     * Get the destination vertex of this transaction.
     *
     * @return the destination vertex as a {@link SVertex}.
     */
    public SVertex destinationVertex() {
        final int vxId = readableGraph.getTransactionDestinationVertex(id);
        return new SVertex(readableGraph, vxId);
    }

    public SVertex otherVertex(final SVertex vx) {
        final int otherVxId = GraphElementType.TRANSACTION.getOtherVertex(readableGraph, id, vx.id());
        return new SVertex(readableGraph, otherVxId);
    }

    /**
     * Python evaluation of self[key].
     *
     * @param key an object representing the attribute to query.
     * @return the value for the specified attribute.
     */
    public Object __getitem__(final Object key) {
        final int attrId = readableGraph.getAttribute(GraphElementType.TRANSACTION, (String) key);
        if (attrId == Graph.NOT_FOUND) {
            throw new NoSuchAttributeException(key);
        }
        return readableGraph.getObjectValue(attrId, id);
    }

    /**
     * Python assignment to self[key].
     *
     * @param key an object representing the attribute to set.
     * @param value the new value for the specified attribute.
     * @return the set value.
     */
    public Object __setitem__(final Object key, final Object value) {
        final int attrId = writableGraph().getSchema() != null
                ? writableGraph().getSchema().getFactory().ensureAttribute(writableGraph(), GraphElementType.TRANSACTION, (String) key)
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
    public boolean equals(final Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final STransaction other = (STransaction) obj;
        return this.id == other.id;
    }

    @Override
    public String toString() {
        return String.format("[%s: %d]", this.getClass().getSimpleName(), id);
    }
}
