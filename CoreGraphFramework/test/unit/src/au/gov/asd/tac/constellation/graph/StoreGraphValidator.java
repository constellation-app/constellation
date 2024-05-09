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
package au.gov.asd.tac.constellation.graph;

import au.gov.asd.tac.constellation.graph.attribute.AttributeDescription;
import au.gov.asd.tac.constellation.graph.operations.GraphOperation;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.value.readables.IntReadable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * this class is required for the unit testing
 *
 * @author sirius
 */
public final class StoreGraphValidator implements GraphWriteMethods {

    @Override
    public int getLinkEdge(final int link, final int direction) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getEdgeSourceVertex(final int edge) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getEdgeDestinationVertex(final int edge) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getEdgeLink(final int edge) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getEdgeTransactionCount(final int edge) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getEdgeTransaction(final int edge, final int position) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getTransactionEdge(final int transaction) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getEdgeDirection(final int edge) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getVertexNeighbour(final int vertex, final int position) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getVertexNeighbourCount(final int vertex) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getVertexEdgeCount(final int vertex) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getVertexPosition(final int vertex) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getVertexEdge(final int vertex, final int position) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getVertexEdgeCount(final int vertex, final int direction) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getVertexEdge(final int vertex, final int direction, final int position) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getLinkEdgeCount(final int link) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getLinkEdgeCount(final int link, final int direction) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getLinkEdge(final int link, final int direction, final int position) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getEdgePosition(final int edge) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getLinkPosition(final int link) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getTransactionPosition(final int transaction) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long getGlobalModificationCounter() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long getAttributeModificationCounter() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long getStructureModificationCounter() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long getValueModificationCounter(final int attribute) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setPrimaryKey(final GraphElementType elementType, final int... attributes) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getId() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateAttributeName(int attribute, String newName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateAttributeDescription(int attribute, String newDescription) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateAttributeDefaultValue(int attribute, Object newObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GraphReadMethods copy() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GraphReadMethods copy(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isDefaultValue(int attribute, int id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GraphKey getPrimaryKeyValue(GraphElementType elementType, int id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int[] getPrimaryKey(GraphElementType elementType) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object copyAttribute(int attribute) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void validateKey(GraphElementType elementType, boolean allowMerging) throws DuplicateKeyException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public NativeAttributeType getNativeAttributeType(int attribute) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object createWriteAttributeObject(int attribute, IntReadable indexReadable) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object createReadAttributeObject(int attribute, IntReadable indexReadable) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Schema getSchema() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isPrimaryKey(int attribute) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String acceptsStringValue(int attribute, String value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void validateKey(GraphElementType elementType, int element, boolean allowMerging) throws DuplicateKeyException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getAttributeName(int attribute) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long getEdgeUID(int edge) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long getLinkUID(int link) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long getTransactionUID(int transaction) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long getVertexUID(int vertex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long getAttributeUID(int attribute) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setTransactionSourceVertex(int transaction, int newSourceVertex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setTransactionDestinationVertex(int transaction, int newDestinationVertex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void executeGraphOperation(GraphOperation operation) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setAttributeIndexType(int attribute, GraphIndexType indexType) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean attributeSupportsIndexType(int attribute, GraphIndexType indexType) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GraphIndexResult getElementsWithAttributeValue(int attribute, Object value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GraphIndexResult getElementsWithAttributeValueRange(int attribute, Object start, Object end) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GraphIndexType getAttributeIndexType(int attribute) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getAttributeType(int attribute) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GraphAttributeMerger getAttributeMerger(int attribute) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getAttributeDescription(int attribute) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GraphElementType getAttributeElementType(int attribute) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Class<? extends AttributeDescription> getAttributeDataType(int attribute) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getAttributeDefaultValue(int attribute) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private class Vertex {

        private int id;
        private final List<Link> links = new ArrayList<>();
        private final Map<Vertex, Link> uphillLinks = new HashMap<>();
        private final Map<Vertex, Link> downhillLinks = new HashMap<>();
        private final List<Transaction> transactions = new ArrayList<>();
        private final List<Transaction> outgoingTransactions = new ArrayList<>();
        private final List<Transaction> incomingTransactions = new ArrayList<>();
        private final List<Transaction> undirectedTransactions = new ArrayList<>();
    }

    private class Link {

        private int id;
        private Vertex lowVertex, highVertex;
        private final List<Transaction> transactions = new ArrayList<>();
        private final List<Transaction> uphillTransactions = new ArrayList<>();
        private final List<Transaction> downhillTransactions = new ArrayList<>();
        private final List<Transaction> undirectedTransactions = new ArrayList<>();
    }

    private class Transaction {

        private int id;
        private Link link;
        private int direction;
    }
    private final Map<Integer, Vertex> vertexMap = new HashMap<>();
    private final List<Vertex> vertexList = new ArrayList<>();
    private final Map<Integer, Link> linkMap = new HashMap<>();
    private final List<Link> linkList = new ArrayList<>();
    private final Map<Integer, Transaction> transactionMap = new HashMap<>();
    private final List<Transaction> transactionList = new ArrayList<>();

    @Override
    public int addTransaction(final int sourceVertex, final int destinationVertex, final boolean directed) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int addTransaction(final int transaction, final int sourceVertex, final int destinationVertex, final boolean directed) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addTransaction(final int sourceVertex, final int destinationVertex, final boolean directed, final int transaction, final int link) {

        Vertex s = vertexMap.get(sourceVertex);
        if (s == null) {
            throw new IllegalArgumentException("Attempt to add transaction to source vertex that does not exist: " + sourceVertex);
        }

        Vertex d = vertexMap.get(destinationVertex);
        if (d == null) {
            throw new IllegalArgumentException("Attempt to add transaction to destination vertex that does not exist: " + destinationVertex);
        }

        Transaction t = transactionMap.get(transaction);
        if (t != null) {
            throw new IllegalArgumentException("Attempt to add a transaction that already exists: " + transaction);
        }

        Vertex low, high;
        if (sourceVertex <= destinationVertex) {
            low = s;
            high = d;
        } else {
            low = d;
            high = s;
        }

        Link l = low.uphillLinks.get(high);
        if (l == null) {

            l = linkMap.get(link);
            if (l != null) {
                throw new IllegalArgumentException("Attempt to reuse a link between different source and destination vertices");
            }

            l = new Link();
            l.id = link;
            l.lowVertex = low;
            l.highVertex = high;
            linkList.add(l);
            linkMap.put(link, l);

            low.uphillLinks.put(high, l);
            high.downhillLinks.put(low, l);
            low.links.add(l);
            high.links.add(l);

        } else {

            if (l.id != link) {
                throw new IllegalArgumentException("Attempt to create another link between a pair of vertices that already have a link");
            }

        }

        t = new Transaction();
        t.id = transaction;
        t.link = l;

        transactionMap.put(transaction, t);
        transactionList.add(t);
        low.transactions.add(t);
        high.transactions.add(t);

        if (directed) {
            t.direction = low == s ? 0 : 1;
        } else {
            t.direction = 2;
        }

        switch (t.direction) {
            case 0:
                l.uphillTransactions.add(t);
                low.outgoingTransactions.add(t);
                high.incomingTransactions.add(t);
                break;
            case 1:
                l.downhillTransactions.add(t);
                low.incomingTransactions.add(t);
                high.outgoingTransactions.add(t);
                break;
            case 2:
                l.undirectedTransactions.add(t);
                low.undirectedTransactions.add(t);
                high.undirectedTransactions.add(t);
                break;
        }

        l.transactions.add(t);

    }

    @Override
    public int addVertex() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int addVertex(final int vertex) {

        Vertex v = vertexMap.get(vertex);

        if (v != null) {
            throw new IllegalArgumentException("Attempt to add vertex that already exists: " + vertex);
        }

        v = new Vertex();
        v.id = vertex;
        vertexList.add(v);
        vertexMap.put(vertex, v);
        
        return vertex;
    }

    @Override
    public int getLink(final int position) {
        return linkList.get(position).id;
    }

    @Override
    public int getLinkCapacity() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getLinkCount() {
        return linkList.size();
    }

    @Override
    public int getLinkTransaction(final int link, final int position) {
        return linkMap.get(link).transactions.get(position).id;
    }

    @Override
    public int getLinkTransaction(final int link, final int direction, final int position) {
        Link l = linkMap.get(link);
        switch (direction) {
            case 0:
                return l.uphillTransactions.get(position).id;
            case 1:
                return l.downhillTransactions.get(position).id;
            case 2:
                return l.undirectedTransactions.get(position).id;
            default:
                throw new IllegalArgumentException("Attempt to access transaction for direction that does not exist: " + direction);
        }
    }

    @Override
    public int getLinkTransactionCount(final int link) {
        return linkMap.get(link).transactions.size();
    }

    @Override
    public int getLinkTransactionCount(final int link, final int direction) {
        Link l = linkMap.get(link);
        switch (direction) {
            case 0:
                return l.uphillTransactions.size();
            case 1:
                return l.downhillTransactions.size();
            case 2:
                return l.undirectedTransactions.size();
            default:
                throw new IllegalArgumentException("Attempt to access transaction count for direction that does not exist: " + direction);
        }
    }

    @Override
    public int getTransaction(final int position) {
        return transactionList.get(position).id;
    }

    @Override
    public int getTransactionCapacity() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getTransactionCount() {
        return transactionList.size();
    }

    @Override
    public int getTransactionDirection(final int transaction) {
        return transactionMap.get(transaction).direction;
    }

    @Override
    public int getTransactionLink(final int transaction) {
        return transactionMap.get(transaction).link.id;
    }

    @Override
    public int getVertex(final int position) {
        return vertexList.get(position).id;
    }

    @Override
    public int getVertexCapacity() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getVertexCount() {
        return vertexList.size();
    }

    @Override
    public int getVertexLink(final int vertex, final int position) {
        return vertexMap.get(vertex).links.get(position).id;
    }

    @Override
    public int getVertexLinkCount(final int vertex) {
        return vertexMap.get(vertex).links.size();
    }

    @Override
    public int getVertexTransaction(final int vertex, final int position) {
        return vertexMap.get(vertex).transactions.get(position).id;
    }

    @Override
    public int getVertexTransaction(final int vertex, final int direction, final int position) {
        switch (direction) {
            case 0:
                return vertexMap.get(vertex).outgoingTransactions.get(position).id;
            case 1:
                return vertexMap.get(vertex).incomingTransactions.get(position).id;
            case 2:
                return vertexMap.get(vertex).undirectedTransactions.get(position).id;
            default:
                throw new IllegalArgumentException("Attempt to access transaction for direction that does not exist: " + direction);
        }
    }

    @Override
    public int getVertexTransactionCount(final int vertex) {
        return vertexMap.get(vertex).transactions.size();
    }

    @Override
    public int getVertexTransactionCount(final int vertex, final int direction) {
        switch (direction) {
            case 0:
                return vertexMap.get(vertex).outgoingTransactions.size();
            case 1:
                return vertexMap.get(vertex).incomingTransactions.size();
            case 2:
                return vertexMap.get(vertex).undirectedTransactions.size();
            default:
                throw new IllegalArgumentException("Attempt to access transaction count for direction that does not exist: " + direction);
        }
    }

    @Override
    public void removeTransaction(final int transaction) {

        Transaction t = transactionMap.get(transaction);

        if (t == null) {
            throw new IllegalArgumentException("Attempt to remove transaction that does not exist: " + transaction);
        }

        transactionMap.remove(transaction);
        transactionList.remove(t);

        Link l = t.link;
        l.transactions.remove(t);
        l.uphillTransactions.remove(t);
        l.downhillTransactions.remove(t);
        l.undirectedTransactions.remove(t);

        Vertex low = l.lowVertex;
        low.transactions.remove(t);
        low.outgoingTransactions.remove(t);
        low.incomingTransactions.remove(t);
        low.undirectedTransactions.remove(t);

        Vertex high = l.highVertex;
        high.transactions.remove(t);
        high.outgoingTransactions.remove(t);
        high.incomingTransactions.remove(t);
        high.undirectedTransactions.remove(t);

        if (l.transactions.isEmpty()) {

            low.links.remove(l);
            low.uphillLinks.remove(high);

            high.links.remove(l);
            high.downhillLinks.remove(low);

            linkMap.remove(l.id);
            linkList.remove(l);
        }
    }

    @Override
    public void removeVertex(final int vertex) {

        Vertex v = vertexMap.get(vertex);

        if (v == null) {
            throw new IllegalArgumentException("Attempt to remove vertex that does not exist: " + vertex);
        }

        while (!v.links.isEmpty()) {
            removeTransaction(v.links.get(0).transactions.get(0).id);
        }

        vertexMap.remove(vertex);
        vertexList.remove(v);
    }

    @Override
    public int getLink(final int vertex1, final int vertex2) {
        Vertex v1 = vertexMap.get(vertex1);
        Vertex v2 = vertexMap.get(vertex2);
        if (vertex1 <= vertex2) {
            Link l = v1.uphillLinks.get(v2);
            return l == null ? Graph.NOT_FOUND : l.id;
        } else {
            Link l = v1.downhillLinks.get(v2);
            return l == null ? Graph.NOT_FOUND : l.id;
        }
    }

    @Override
    public int getTransactionSourceVertex(final int transaction) {
        Transaction t = transactionMap.get(transaction);
        switch (t.direction) {
            case 0:
                return t.link.lowVertex.id;
            case 1:
                return t.link.highVertex.id;
            case 2:
                return t.link.lowVertex.id;
        }
        throw new IllegalStateException("Found transaction with invalid direction: transaction = " + transaction + ", direction = " + t.direction);
    }

    @Override
    public int getTransactionDestinationVertex(final int transaction) {
        Transaction t = transactionMap.get(transaction);
        switch (t.direction) {
            case 0:
                return t.link.highVertex.id;
            case 1:
                return t.link.lowVertex.id;
            case 2:
                return t.link.highVertex.id;
        }
        throw new IllegalStateException("Found transaction with invalid direction: transaction = " + transaction + ", direction = " + t.direction);
    }

    @Override
    public boolean linkExists(final int link) {
        return linkMap.get(link) != null;
    }

    @Override
    public boolean transactionExists(final int transaction) {
        return transactionMap.get(transaction) != null;
    }

    @Override
    public boolean vertexExists(final int vertex) {
        return vertexMap.get(vertex) != null;
    }

    @Override
    public int addAttribute(final GraphElementType elementType, final String attributeName, final String label, final String description, final Object defaultValue, final String attributeMergerId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeAttribute(final int attribute) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getAttributeCount(final GraphElementType elementType) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getAttributeCapacity() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getAttribute(final GraphElementType elementType, final int position) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getAttribute(final GraphElementType elementType, final String label) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte getByteValue(final int attribute, final int id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public short getShortValue(final int attribute, final int id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getIntValue(final int attribute, final int id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long getLongValue(final int attribute, final int id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float getFloatValue(final int attribute, final int id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getDoubleValue(final int attribute, final int id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getBooleanValue(final int attribute, final int id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public char getCharValue(final int attribute, final int id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getStringValue(final int attribute, final int id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object getObjectValue(final int attribute, final int id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clearValue(final int attribute, final int id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setByteValue(final int attribute, final int id, final byte value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setShortValue(final int attribute, final int id, final short value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setIntValue(final int attribute, final int id, final int value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setLongValue(final int attribute, final int id, final long value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setFloatValue(final int attribute, final int id, final float value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setDoubleValue(final int attribute, final int id, final double value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setBooleanValue(final int attribute, final int id, final boolean value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setCharValue(final int attribute, final int id, final char value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setStringValue(final int attribute, final int id, final String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setObjectValue(final int attribute, final int id, final Object value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getLinkLowVertex(final int link) {
        return linkMap.get(link).lowVertex.id;
    }

    @Override
    public int getLinkHighVertex(final int link) {
        return linkMap.get(link).highVertex.id;
    }

    @Override
    public int getEdgeCapacity() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getEdgeCount() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getEdge(final int position) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean edgeExists(final int edge) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isRecordingEdit() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IntStream vertexStream() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IntStream linkStream() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IntStream edgeStream() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IntStream transactionStream() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
