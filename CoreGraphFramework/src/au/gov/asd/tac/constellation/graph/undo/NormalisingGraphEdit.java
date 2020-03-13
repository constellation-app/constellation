/*
 * Copyright 2010-2019 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.graph.GraphOperation;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author sirius
 */
public class NormalisingGraphEdit implements GraphEdit {

    private final GraphEdit graphEdit;

    public NormalisingGraphEdit(GraphEdit graphEdit) {
        this.graphEdit = graphEdit;
    }

    @Override
    public void execute(GraphWriteMethods graph) {
        graphEdit.execute(graph);
    }

    @Override
    public void undo(GraphWriteMethods graph) {
        graphEdit.undo(graph);
    }

    @Override
    public void addChild(GraphEdit childEdit) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void finish() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setPrimaryKey(GraphElementType elementType, int[] oldKeys, int[] newKeys) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addVertex(int vertex) {
        Vertex newVertex = new Vertex(vertex);
        Vertex currentVertex = vertices.put(vertex, newVertex);
        if (currentVertex != null) {
            newVertex.previous = currentVertex;
            newVertex.version = currentVertex.version + 1;
        }
    }

    @Override
    public void removeVertex(int vertex) {
        Vertex currentVertex = vertices.get(vertex);
        if (currentVertex == null) {
            currentVertex = new Vertex(vertex);
            vertices.put(vertex, currentVertex);
        }
        currentVertex.deleted = true;
    }

    @Override
    public void addTransaction(int sourceVertex, int destinationVertex, boolean directed, int transaction) {

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

        Transaction newTransaction = new Transaction(transaction);
//        newTransaction.source = currentSource;
//        newTransaction.destination = currentDestination;
        newTransaction.directed = directed;

        Transaction currentTransaction = transactions.put(transaction, newTransaction);
        if (currentTransaction != null) {
            newTransaction.previous = currentTransaction;
            newTransaction.version = currentTransaction.version + 1;
        }
    }

    @Override
    public void removeTransaction(int sourceVertex, int destinationVertex, boolean directed, int transaction) {
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
//            currentTransaction.source = currentSource;
//            currentTransaction.destination = currentDestination;
            currentTransaction.directed = directed;

            transactions.put(transaction, currentTransaction);
        }

        currentTransaction.deleted = true;
    }

    @Override
    public void setTransactionSourceVertex(int transaction, int oldSourceVertex, int newSourceVertex, boolean reverseTransaction) {

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

//        currentTransaction.source = newSource;
    }

    @Override
    public void setTransactionDestinationVertex(int transaction, int oldDestinationVertex, int newDestinationVertex, boolean reverseTransaction) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addAttribute(GraphElementType elementType, String attributeType, String label, String description, Object defaultValue, String attributeMergerId, int attribute) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeAttribute(GraphElementType elementType, String attributeType, String label, String description, Object defaultValue, String attributeMergerId, int attribute) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateAttributeName(int attribute, String oldName, String newName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateAttributeDescription(int attribute, String oldDescription, String newDescription) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateAttributeDefaultValue(int attribute, Object oldObject, Object newObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setByteValue(int attribute, int id, byte oldValue, byte newValue) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setShortValue(int attribute, int id, short oldValue, short newValue) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setIntValue(int attribute, int id, int oldValue, int newValue) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setLongValue(int attribute, int id, long oldValue, long newValue) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setFloatValue(int attribute, int id, float oldValue, float newValue) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setDoubleValue(int attribute, int id, double oldValue, double newValue) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setBooleanValue(int attribute, int id, boolean oldValue, boolean newValue) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setCharValue(int attribute, int id, char oldValue, char newValue) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setObjectValue(int attribute, int id, Object oldValue, Object newValue) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void executeGraphOperation(GraphOperation operation) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setAttributeIndexType(int attribute, GraphIndexType oldValue, GraphIndexType newValue) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private class Element<E> {

        public final int id;
        public boolean deleted = false;
        public int version;
        public E previous = null;

        public Element(int id) {
            this.id = id;
        }
    }

    private class Vertex extends Element<Vertex> {

        public Vertex(int id) {
            super(id);
        }
    }

    private final Map<Integer, Vertex> vertices = new HashMap<>();

    private class Transaction extends Element<Transaction> {

        public Vertex newSource, oldSource, newDestination, oldDestination;
        public boolean directed;

        public Transaction(int id) {
            super(id);
        }
    }

    private final Map<Integer, Transaction> transactions = new HashMap<>();
}
