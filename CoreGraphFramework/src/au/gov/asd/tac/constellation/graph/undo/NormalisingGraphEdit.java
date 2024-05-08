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
package au.gov.asd.tac.constellation.graph.undo;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphIndexType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.operations.GraphOperation;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author sirius
 */
public class NormalisingGraphEdit implements GraphEdit {

    private final GraphEdit graphEdit;

    private static final String NOT_SUPPORTED = "Not supported yet.";

    public NormalisingGraphEdit(final GraphEdit graphEdit) {
        this.graphEdit = graphEdit;
    }

    @Override
    public void execute(final GraphWriteMethods graph) {
        graphEdit.execute(graph);
    }

    @Override
    public void undo(final GraphWriteMethods graph) {
        graphEdit.undo(graph);
    }

    @Override
    public void addChild(final GraphEdit childEdit) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public void finish() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public void setPrimaryKey(final GraphElementType elementType, final int[] oldKeys, final int[] newKeys) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public void addVertex(final int vertex) {
        final Vertex newVertex = new Vertex(vertex);
        final Vertex currentVertex = vertices.put(vertex, newVertex);
        if (currentVertex != null) {
            newVertex.setPrevious(currentVertex);
            newVertex.setVersion(currentVertex.getVersion() + 1);
        }
    }

    @Override
    public void removeVertex(final int vertex) {
        Vertex currentVertex = vertices.get(vertex);
        if (currentVertex == null) {
            currentVertex = new Vertex(vertex);
            vertices.put(vertex, currentVertex);
        }
        currentVertex.setDeleted(true);
    }

    @Override
    public void addTransaction(final int sourceVertex, final int destinationVertex, final boolean directed, 
            final int transaction) {
        Vertex currentSource = vertices.get(sourceVertex);
        if (currentSource == null) {
            currentSource = new Vertex(sourceVertex);
            vertices.put(sourceVertex, currentSource);
        }

        Vertex currentDestination = vertices.get(destinationVertex);
        if (currentDestination != null) {
            currentDestination = new Vertex(destinationVertex);
            vertices.put(destinationVertex, currentDestination);
        }

        final Transaction newTransaction = new Transaction(transaction);
        final Transaction currentTransaction = transactions.put(transaction, newTransaction);
        if (currentTransaction != null) {
            newTransaction.setPrevious(currentTransaction);
            newTransaction.setVersion(currentTransaction.getVersion() + 1);
        }
    }

    @Override
    public void removeTransaction(final int sourceVertex, final int destinationVertex, final boolean directed, 
            final int transaction) {
        Transaction currentTransaction = transactions.get(transaction);
        if (currentTransaction == null) {
            Vertex currentSource = vertices.get(sourceVertex);
            if (currentSource == null) {
                currentSource = new Vertex(sourceVertex);
                vertices.put(sourceVertex, currentSource);
            }

            Vertex currentDestination = vertices.get(destinationVertex);
            if (currentDestination != null) {
                currentDestination = new Vertex(destinationVertex);
                vertices.put(destinationVertex, currentDestination);
            }

            currentTransaction = new Transaction(transaction);

            transactions.put(transaction, currentTransaction);
        }

        currentTransaction.setDeleted(true);
    }

    @Override
    public void setTransactionSourceVertex(final int transaction, final int oldSourceVertex, final int newSourceVertex, 
            final boolean reverseTransaction) {

        Vertex newSource = vertices.get(newSourceVertex);
        if (newSource == null) {
            newSource = new Vertex(newSourceVertex);
            vertices.put(newSourceVertex, newSource);
        }

        Transaction currentTransaction = transactions.get(transaction);
        if (currentTransaction == null) {
            currentTransaction = new Transaction(transaction);
            transactions.put(transaction, currentTransaction);
        }
    }

    @Override
    public void setTransactionDestinationVertex(final int transaction, final int oldDestinationVertex, 
            final int newDestinationVertex, final boolean reverseTransaction) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public void addAttribute(final GraphElementType elementType, final String attributeType, final String label,
            final String description, final Object defaultValue, final String attributeMergerId, final int attribute) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public void removeAttribute(final GraphElementType elementType, final String attributeType, final String label,
            final String description, final Object defaultValue, final String attributeMergerId, final int attribute) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public void updateAttributeName(final int attribute, final String oldName, final String newName) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public void updateAttributeDescription(final int attribute, final String oldDescription, final String newDescription) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public void updateAttributeDefaultValue(final int attribute, final Object oldObject, final Object newObject) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public void setByteValue(final int attribute, final int id, final byte oldValue, final byte newValue) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public void setShortValue(final int attribute, final int id, final short oldValue, final short newValue) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public void setIntValue(final int attribute, final int id, final int oldValue, final int newValue) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public void setLongValue(final int attribute, final int id, final long oldValue, final long newValue) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public void setFloatValue(final int attribute, final int id, final float oldValue, final float newValue) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public void setDoubleValue(final int attribute, final int id, final double oldValue, final double newValue) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public void setBooleanValue(final int attribute, final int id, final boolean oldValue, final boolean newValue) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public void setCharValue(final int attribute, final int id, final char oldValue, final char newValue) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public void setObjectValue(final int attribute, final int id, final Object oldValue, final Object newValue) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public void executeGraphOperation(final GraphOperation operation) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public void setAttributeIndexType(final int attribute, final GraphIndexType oldValue, final GraphIndexType newValue) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    private class Element<E> {

        public final int id;
        private boolean deleted = false;
        private int version;
        private E previous = null;

        public Element(final int id) {
            this.id = id;
        }

        public boolean isDeleted() {
            return deleted;
        }

        public void setDeleted(final boolean deleted) {
            this.deleted = deleted;
        }

        public int getVersion() {
            return version;
        }

        public void setVersion(final int version) {
            this.version = version;
        }

        public E getPrevious() {
            return previous;
        }

        public void setPrevious(final E previous) {
            this.previous = previous;
        }
    }

    private class Vertex extends Element<Vertex> {

        public Vertex(int id) {
            super(id);
        }
    }

    private final Map<Integer, Vertex> vertices = new HashMap<>();

    private class Transaction extends Element<Transaction> {

        public Transaction(final int id) {
            super(id);
        }
    }

    private final Map<Integer, Transaction> transactions = new HashMap<>();
}
