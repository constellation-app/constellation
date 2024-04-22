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
import au.gov.asd.tac.constellation.views.scripting.graph.iterators.SLinkEdgeIterator;
import au.gov.asd.tac.constellation.views.scripting.graph.iterators.SLinkTransactionIterator;

/**
 * A representation of a link for use with scripting.
 *
 * @author cygnus_x-1
 */
public class SLink {

    private final GraphReadMethods readableGraph;
    private final int id;

    public SLink(final GraphReadMethods rg, final int id) {
        this.readableGraph = rg;
        this.id = id;
    }

    private GraphWriteMethods writableGraph() {
        return (GraphWriteMethods) readableGraph;
    }

    /**
     * Get the id of this link.
     *
     * @return the id of this link.
     */
    public int id() {
        return id;
    }

    /**
     * Get an iterator over attributes attached to this link.
     *
     * @return an attribute iterator.
     */
    public SAttributeIterator attributes() {
        return new SAttributeIterator(readableGraph, GraphElementType.LINK);
    }

    /**
     * Get the vertex the end of this link with the higher id.
     *
     * @return a vertex as a {@link SVertex}.
     */
    public SVertex highVertex() {
        final int vxId = readableGraph.getLinkHighVertex(id);
        return new SVertex(readableGraph, vxId);
    }

    /**
     * Get the vertex the end of this link with the lower id.
     *
     * @return a vertex as a {@link SVertex}.
     */
    public SVertex lowVertex() {
        final int vxId = readableGraph.getLinkLowVertex(id);
        return new SVertex(readableGraph, vxId);
    }

    /**
     * Get the other vertex of this link given one end.
     *
     * @param vertex the vertex at one end of this link.
     * @return the vertex at the other end of this link as a {@link SVertex}.
     */
    public SVertex otherVertex(final SVertex vertex) {
        final int otherVxId = GraphElementType.LINK.getOtherVertex(readableGraph, id, vertex.id());
        return new SVertex(readableGraph, otherVxId);
    }

    /**
     * Get the number of transactions this link represents.
     *
     * @return the number of transactions.
     */
    public int transactionCount() {
        return readableGraph.getLinkTransactionCount(id);
    }

    /**
     * Get an iterator over transactions this link represents.
     *
     * @return a transaction iterator.
     */
    public SLinkTransactionIterator transactions() {
        return new SLinkTransactionIterator(readableGraph, id);
    }

    /**
     * Get the number of edges this link represents.
     *
     * @return the number of edges.
     */
    public int edgeCount() {
        return readableGraph.getLinkEdgeCount(id);
    }

    /**
     * Get an iterator over edges this link represents.
     *
     * @return an edge iterator.
     */
    public SLinkEdgeIterator edges() {
        return new SLinkEdgeIterator(readableGraph, id);
    }

    /**
     * Python evaluation of self[key].
     *
     * @param key an object representing the attribute to query.
     * @return the value of the specified attribute.
     */
    public Object __getitem__(final Object key) {
        final int attrId = readableGraph.getAttribute(GraphElementType.LINK, (String) key);
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
                ? writableGraph().getSchema().getFactory().ensureAttribute(writableGraph(), GraphElementType.LINK, (String) key)
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
        final SLink other = (SLink) obj;
        return this.id == other.id;
    }

    @Override
    public String toString() {
        return String.format("[%s: %d]", this.getClass().getSimpleName(), id);
    }
}
