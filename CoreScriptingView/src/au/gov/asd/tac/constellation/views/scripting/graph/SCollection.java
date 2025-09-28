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
package au.gov.asd.tac.constellation.views.scripting.graph;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import java.util.BitSet;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * A collection which efficiently stores vertices or transactions for use with
 * scripting.
 *
 * @author algol
 */
public class SCollection {

    private final ScriptEngine engine;
    private final GraphReadMethods readableGraph;
    private final GraphElementType elementType;
    private final BitSet elementIds;

    public SCollection(final ScriptEngine engine, final GraphReadMethods readableGraph, final GraphElementType elementType, final BitSet elementIds) {
        this.engine = engine;
        this.readableGraph = readableGraph;
        this.elementType = elementType;
        this.elementIds = elementIds;

        if (elementType != GraphElementType.VERTEX && elementType != GraphElementType.TRANSACTION) {
            throw new IllegalArgumentException("filterType can only be VERTEX or TRANSACTION");
        }
    }

    /**
     * Get the element type of elements in this collection.
     *
     * @return the element type.
     */
    public GraphElementType elementType() {
        return elementType;
    }

    /**
     * Get the ids of elements in this collection.
     *
     * @return the element ids.
     */
    public BitSet elementIds() {
        return elementIds;
    }

    private SCollection filterVertices(final Object callback) throws ScriptException {
        final ScriptContext context = engine.getContext();
        context.setAttribute("__func", callback, ScriptContext.ENGINE_SCOPE);

        final BitSet vertices = new BitSet();
        for (int vxId = elementIds.nextSetBit(0); vxId >= 0; vxId = elementIds.nextSetBit(vxId + 1)) {
            context.setAttribute("__p1", new SVertex(readableGraph, vxId), ScriptContext.ENGINE_SCOPE);
            final Object b = engine.eval("__func(__p1)");
            if (Boolean.TRUE.equals(b)) {
                vertices.set(vxId);
            }
        }

        return new SCollection(engine, readableGraph, GraphElementType.VERTEX, vertices);
    }

    private SCollection filterTransactions(final Object callback) throws ScriptException {
        final ScriptContext context = engine.getContext();
        context.setAttribute("__func", callback, ScriptContext.ENGINE_SCOPE);

        final BitSet transactions = new BitSet();
        for (int txId = elementIds.nextSetBit(0); txId >= 0; txId = elementIds.nextSetBit(txId + 1)) {
            context.setAttribute("__p1", new STransaction(readableGraph, txId), ScriptContext.ENGINE_SCOPE);
            final Object b = engine.eval("__func(__p1)");
            if (Boolean.TRUE.equals(b)) {
                transactions.set(txId);
            }
        }

        return new SCollection(engine, readableGraph, GraphElementType.TRANSACTION, transactions);
    }

    /**
     * Filter this collection using the provided function. The provided function
     * must only return true or false.
     *
     * Note: This method will only work for Python scripts as it makes use of
     * Python specific syntax.
     *
     * @param callback the function which returns true or false.
     * @return a collection of elements as a {@link SCollection}.
     * @throws ScriptException
     */
    public SCollection filter(final Object callback) throws ScriptException {
        return elementType == GraphElementType.VERTEX ? filterVertices(callback) : filterTransactions(callback);
    }

    /**
     * Get the intersection of this collection with another.
     *
     * @param other the other collection as a {@link SCollection}.
     * @return the intersection of this collection and another as a
     * {@link SCollection}.
     */
    public SCollection intersection(final SCollection other) {
        final BitSet idSet = new BitSet();
        idSet.or(elementIds);
        idSet.and(other.elementIds);

        return new SCollection(engine, readableGraph, elementType, idSet);
    }

    /**
     * Get the union of this collection with another.
     *
     * @param other the other collection as a {@link SCollection}.
     * @return the union of this collection and another as a
     * {@link SCollection}.
     */
    public SCollection union(final SCollection other) {
        final BitSet idSet = new BitSet();
        idSet.or(elementIds);
        idSet.or(other.elementIds);

        return new SCollection(engine, readableGraph, elementType, idSet);
    }

    /**
     * Check if this collection is empty.
     *
     * @return true if this collection is empty, false otherwise.
     */
    public boolean empty() {
        return elementIds.isEmpty();
    }

    /**
     * Get the size of this collection.
     *
     * @return the size of this collection.
     */
    public int size() {
        return elementIds.cardinality();
    }

    @Override
    public String toString() {
        return elementIds.toString();
    }
}
