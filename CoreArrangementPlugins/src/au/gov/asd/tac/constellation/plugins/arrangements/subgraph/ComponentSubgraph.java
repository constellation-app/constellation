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
package au.gov.asd.tac.constellation.plugins.arrangements.subgraph;

import au.gov.asd.tac.constellation.graph.GraphAttributeMerger;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphIndexResult;
import au.gov.asd.tac.constellation.graph.GraphIndexType;
import au.gov.asd.tac.constellation.graph.GraphKey;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.NativeAttributeType;
import au.gov.asd.tac.constellation.graph.attribute.AttributeDescription;
import au.gov.asd.tac.constellation.graph.operations.GraphOperation;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.value.readables.IntReadable;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * Subgraph write methods for a connected component
 *
 * @author twilight_sparkle
 */
public class ComponentSubgraph implements GraphWriteMethods {

    protected final GraphWriteMethods proxy;

    protected final Set<Integer> includedVertexIDs;
    protected final int[] vertexList;
    protected final int[] vertexPositions;
    protected int[] linkList = null;
    protected int[] linkPositions = null;
    protected int[] edgeList = null;
    protected int[] edgePositions = null;
    protected int[] transactionList = null;
    protected int[] transactionPositions = null;

    public static SubgraphFactory getSubgraphFactory() {
        return (final GraphWriteMethods wg, final Set<Integer> vertexIDs) -> new ComponentSubgraph(wg, vertexIDs);
    }

    public ComponentSubgraph(final GraphWriteMethods proxy, final Set<Integer> includedVertexIDs) {
        this.proxy = proxy;
        this.includedVertexIDs = includedVertexIDs;
        vertexList = new int[includedVertexIDs.size()];
        vertexPositions = new int[proxy.getVertexCapacity()];
        int pos = 0;
        for (final int vert : includedVertexIDs) {
            vertexPositions[vert] = pos;
            vertexList[pos++] = vert;
        }
    }

    private void calculateLinks() {
        int pos = 0;
        linkPositions = new int[proxy.getLinkCapacity()];
        linkList = new int[proxy.getLinkCount()];
        for (int i = 0; i < proxy.getLinkCount(); i++) {
            final int lxID = proxy.getLink(i);
            if (linkExists(lxID)) {
                linkPositions[lxID] = pos;
                linkList[pos++] = lxID;
            }
        }
        linkList = Arrays.copyOf(linkList, pos);
    }

    private void calculateEdges() {
        int pos = 0;
        edgePositions = new int[proxy.getEdgeCapacity()];
        edgeList = new int[proxy.getEdgeCount()];
        for (int i = 0; i < proxy.getEdgeCount(); i++) {
            final int exID = proxy.getEdge(i);
            if (edgeExists(exID)) {
                edgePositions[exID] = pos;
                edgeList[pos++] = exID;
            }
        }
        edgeList = Arrays.copyOf(edgeList, pos);
    }

    private void calculateTransactions() {
        int pos = 0;
        transactionPositions = new int[proxy.getTransactionCapacity()];
        transactionList = new int[proxy.getTransactionCount()];
        for (int i = 0; i < proxy.getTransactionCount(); i++) {
            final int txID = proxy.getTransaction(i);
            if (transactionExists(txID)) {
                transactionPositions[txID] = pos;
                transactionList[pos++] = txID;
            }
        }
        transactionList = Arrays.copyOf(transactionList, pos);
    }

    @Override
    public final boolean vertexExists(final int vertex) {
        return includedVertexIDs.contains(vertex);
    }

    @Override
    public final boolean linkExists(final int link) {
        return includedVertexIDs.contains(getLinkLowVertex(link)) && includedVertexIDs.contains(getLinkHighVertex(link));
    }

    @Override
    public final boolean edgeExists(final int edge) {
        return includedVertexIDs.contains(getEdgeSourceVertex(edge)) && includedVertexIDs.contains(getEdgeDestinationVertex(edge));
    }

    @Override
    public final boolean transactionExists(final int transaction) {
        return includedVertexIDs.contains(getTransactionSourceVertex(transaction)) && includedVertexIDs.contains(getTransactionDestinationVertex(transaction));
    }

    @Override
    public int getVertex(final int position) {
        return vertexList[position];
    }

    @Override
    public int getLink(final int position) {
        if (linkList == null) {
            calculateLinks();
        }
        return linkList[position];
    }

    @Override
    public int getEdge(final int position) {
        if (edgeList == null) {
            calculateEdges();
        }
        return edgeList[position];
    }

    @Override
    public int getTransaction(final int position) {
        if (transactionList == null) {
            calculateTransactions();
        }
        return transactionList[position];
    }

    @Override
    public int getVertexCount() {
        return vertexList.length;
    }

    @Override
    public int getLinkCount() {
        if (linkList == null) {
            calculateLinks();
        }
        return linkList.length;
    }

    @Override
    public int getEdgeCount() {
        if (edgeList == null) {
            calculateEdges();
        }
        return edgeList.length;
    }

    @Override
    public int getTransactionCount() {
        if (transactionList == null) {
            calculateTransactions();
        }
        return transactionList.length;
    }

    @Override
    public int getVertexPosition(final int vertex) {
        return vertexPositions[vertex];
    }

    @Override
    public int getLinkPosition(final int link) {
        if (linkList == null) {
            calculateLinks();
        }
        return linkPositions[link];
    }

    @Override
    public int getEdgePosition(final int edge) {
        if (edgeList == null) {
            calculateEdges();
        }
        return edgePositions[edge];
    }

    @Override
    public int getTransactionPosition(final int transaction) {
        if (transactionList == null) {
            calculateTransactions();
        }
        return transactionPositions[transaction];
    }

    @Override
    public int addVertex() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public int addVertex(final int vertex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeVertex(final int vertex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int addTransaction(final int sourceVertex, final int destinationVertex, final boolean directed) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int addTransaction(final int transaction, final int sourceVertex, final int destinationVertex, final boolean directed) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeTransaction(final int transaction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTransactionSourceVertex(final int transaction, final int newSourceVertex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTransactionDestinationVertex(final int transaction, final int newDestinationVertex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getId() {
        return proxy.getId();
    }

    @Override
    public Schema getSchema() {
        return proxy.getSchema();
    }

    @Override
    public long getGlobalModificationCounter() {
        return proxy.getGlobalModificationCounter();
    }

    @Override
    public long getAttributeModificationCounter() {
        return proxy.getAttributeModificationCounter();
    }

    @Override
    public long getStructureModificationCounter() {
        return proxy.getStructureModificationCounter();
    }

    @Override
    public long getValueModificationCounter(final int attribute) {
        return proxy.getValueModificationCounter(attribute);
    }

    @Override
    public int getEdgeCapacity() {
        return proxy.getEdgeCapacity();
    }

    @Override
    public int getEdgeDirection(final int edge) {
        return proxy.getEdgeDirection(edge);
    }

    @Override
    public int getEdgeSourceVertex(final int edge) {
        return proxy.getEdgeSourceVertex(edge);
    }

    @Override
    public int getEdgeDestinationVertex(final int edge) {
        return proxy.getEdgeDestinationVertex(edge);
    }

    @Override
    public int getEdgeLink(final int edge) {
        return proxy.getEdgeLink(edge);
    }

    @Override
    public int getEdgeTransactionCount(final int edge) {
        return proxy.getEdgeTransactionCount(edge);
    }

    @Override
    public int getEdgeTransaction(final int edge, final int position) {
        return proxy.getEdgeTransaction(edge, position);
    }

    @Override
    public int getLinkCapacity() {
        return proxy.getLinkCapacity();
    }

    @Override
    public int getLinkLowVertex(final int link) {
        return proxy.getLinkLowVertex(link);
    }

    @Override
    public int getLinkHighVertex(final int link) {
        return proxy.getLinkHighVertex(link);
    }

    @Override
    public int getLinkEdgeCount(final int link) {
        return proxy.getLinkEdgeCount(link);
    }

    @Override
    public int getLinkEdge(final int link, final int position) {
        return proxy.getLinkEdge(link, position);
    }

    @Override
    public int getLinkEdgeCount(final int link, final int direction) {
        return proxy.getLinkEdgeCount(link, direction);
    }

    @Override
    public int getLinkEdge(final int link, final int direction, final int position) {
        return proxy.getLinkEdge(link, direction, position);
    }

    @Override
    public int getLinkTransaction(final int link, final int position) {
        return proxy.getLinkTransaction(link, position);
    }

    @Override
    public int getLinkTransaction(final int link, final int direction, final int position) {
        return proxy.getLinkTransaction(link, direction, position);
    }

    @Override
    public int getLinkTransactionCount(final int link) {
        return proxy.getLinkTransactionCount(link);
    }

    @Override
    public int getLinkTransactionCount(final int link, final int direction) {
        return proxy.getLinkTransactionCount(link, direction);
    }

    @Override
    public int getTransactionDirection(final int transaction) {
        return proxy.getTransactionDirection(transaction);
    }

    @Override
    public int getTransactionLink(final int transaction) {
        return proxy.getTransactionLink(transaction);
    }

    @Override
    public int getTransactionEdge(final int transaction) {
        return proxy.getTransactionEdge(transaction);
    }

    @Override
    public int getVertexCapacity() {
        return proxy.getVertexCapacity();
    }

    @Override
    public int getVertexLink(final int vertex, final int position) {
        return proxy.getVertexLink(vertex, position);
    }

    @Override
    public int getVertexLinkCount(final int vertex) {
        return proxy.getVertexLinkCount(vertex);
    }

    @Override
    public int getVertexNeighbourCount(final int vertex) {
        return proxy.getVertexNeighbourCount(vertex);
    }

    @Override
    public int getVertexEdgeCount(final int vertex) {
        return proxy.getVertexEdgeCount(vertex);
    }

    @Override
    public int getVertexEdge(final int vertex, final int position) {
        return proxy.getVertexEdge(vertex, position);
    }

    @Override
    public int getVertexEdgeCount(final int vertex, final int direction) {
        return proxy.getVertexEdgeCount(vertex, direction);
    }

    @Override
    public int getVertexEdge(final int vertex, final int direction, final int position) {
        return proxy.getVertexEdge(vertex, direction, position);
    }

    @Override
    public int getVertexNeighbour(final int vertex, final int position) {
        return proxy.getVertexNeighbour(vertex, position);
    }

    @Override
    public int getVertexTransaction(final int vertex, final int position) {
        return proxy.getVertexTransaction(vertex, position);
    }

    @Override
    public int getVertexTransaction(final int vertex, final int direction, final int position) {
        return proxy.getVertexTransaction(vertex, direction, position);
    }

    @Override
    public int getVertexTransactionCount(final int vertex) {
        return proxy.getVertexTransactionCount(vertex);
    }

    @Override
    public int getVertexTransactionCount(final int vertex, final int direction) {
        return proxy.getVertexTransactionCount(vertex, direction);
    }

    @Override
    public int getLink(final int vertex1, final int vertex2) {
        return proxy.getLink(vertex1, vertex2);
    }

    @Override
    public int getTransactionSourceVertex(final int transaction) {
        return proxy.getTransactionSourceVertex(transaction);
    }

    @Override
    public int getTransactionDestinationVertex(final int transaction) {
        return proxy.getTransactionDestinationVertex(transaction);
    }

    @Override
    public int addAttribute(final GraphElementType elementType, final String attributeType, final String label, final String description, final Object defaultValue, final String attributeMergerId) {
        return proxy.addAttribute(elementType, attributeType, label, description, defaultValue, attributeMergerId);
    }

    @Override
    public void removeAttribute(final int attribute) {
        proxy.removeAttribute(attribute);
    }

    @Override
    public void updateAttributeName(final int attribute, final String newName) {
        proxy.updateAttributeName(attribute, newName);
    }

    @Override
    public void updateAttributeDescription(final int attribute, final String newDescription) {
        proxy.updateAttributeDescription(attribute, newDescription);
    }

    @Override
    public void updateAttributeDefaultValue(final int attribute, final Object newObject) {
        proxy.updateAttributeDefaultValue(attribute, newObject);
    }

    @Override
    public int getAttributeCount(final GraphElementType elementType) {
        return proxy.getAttributeCount(elementType);
    }

    @Override
    public int getAttributeCapacity() {
        return proxy.getAttributeCapacity();
    }

    @Override
    public int getAttribute(final GraphElementType elementType, final int position) {
        return proxy.getAttribute(elementType, position);
    }

    @Override
    public int getAttribute(final GraphElementType elementType, final String label) {
        return proxy.getAttribute(elementType, label);
    }

    @Override
    public String getAttributeName(final int attribute) {
        return proxy.getAttributeName(attribute);
    }

    @Override
    public GraphAttributeMerger getAttributeMerger(final int attribute) {
        return proxy.getAttributeMerger(attribute);
    }

    @Override
    public byte getByteValue(final int attribute, final int id) {
        return proxy.getByteValue(attribute, id);
    }

    @Override
    public short getShortValue(final int attribute, final int id) {
        return proxy.getShortValue(attribute, id);
    }

    @Override
    public int getIntValue(final int attribute, final int id) {
        return proxy.getIntValue(attribute, id);
    }

    @Override
    public long getLongValue(final int attribute, final int id) {
        return proxy.getLongValue(attribute, id);
    }

    @Override
    public float getFloatValue(final int attribute, final int id) {
        return proxy.getFloatValue(attribute, id);
    }

    @Override
    public double getDoubleValue(final int attribute, final int id) {
        return proxy.getDoubleValue(attribute, id);
    }

    @Override
    public boolean getBooleanValue(final int attribute, final int id) {
        return proxy.getBooleanValue(attribute, id);
    }

    @Override
    public char getCharValue(final int attribute, final int id) {
        return proxy.getCharValue(attribute, id);
    }

    @Override
    public String getStringValue(final int attribute, final int id) {
        return proxy.getStringValue(attribute, id);
    }

    @Override
    public String acceptsStringValue(final int attribute, final String value) {
        return proxy.acceptsStringValue(attribute, value);
    }

    @Override
    public <T> T getObjectValue(final int attribute, final int id) {
        return proxy.<T>getObjectValue(attribute, id);
    }

    @Override
    public void clearValue(final int attribute, final int id) {
        proxy.clearValue(attribute, id);
    }

    @Override
    public void setByteValue(final int attribute, final int id, final byte value) {
        proxy.setByteValue(attribute, id, value);
    }

    @Override
    public void setShortValue(final int attribute, final int id, final short value) {
        proxy.setShortValue(attribute, id, value);
    }

    @Override
    public void setIntValue(final int attribute, final int id, final int value) {
        proxy.setIntValue(attribute, id, value);
    }

    @Override
    public void setLongValue(final int attribute, final int id, final long value) {
        proxy.setLongValue(attribute, id, value);
    }

    @Override
    public void setFloatValue(final int attribute, final int id, final float value) {
        proxy.setFloatValue(attribute, id, value);
    }

    @Override
    public void setDoubleValue(final int attribute, final int id, final double value) {
        proxy.setDoubleValue(attribute, id, value);
    }

    @Override
    public void setBooleanValue(final int attribute, final int id, final boolean value) {
        proxy.setBooleanValue(attribute, id, value);
    }

    @Override
    public void setCharValue(final int attribute, final int id, final char value) {
        proxy.setCharValue(attribute, id, value);
    }

    @Override
    public void setStringValue(final int attribute, final int id, final String value) {
        proxy.setStringValue(attribute, id, value);
    }

    @Override
    public void setObjectValue(final int attribute, final int id, final Object value) {
        proxy.setObjectValue(attribute, id, value);
    }

    @Override
    public void setPrimaryKey(final GraphElementType elementType, final int... attributes) {
        proxy.setPrimaryKey(elementType, attributes);
    }

    @Override
    public void validateKey(final GraphElementType elementType, final boolean allowMerging) {
        proxy.validateKey(elementType, allowMerging);
    }

    @Override
    public void validateKey(final GraphElementType elementType, final int element, final boolean allowMerging) {
        proxy.validateKey(elementType, element, allowMerging);
    }

    @Override
    public GraphReadMethods copy() {
        return proxy.copy();
    }

    @Override
    public GraphReadMethods copy(final String id) {
        return proxy.copy(id);
    }

    @Override
    public GraphKey getPrimaryKeyValue(final GraphElementType elementType, final int id) {
        return proxy.getPrimaryKeyValue(elementType, id);
    }

    @Override
    public int[] getPrimaryKey(final GraphElementType elementType) {
        return proxy.getPrimaryKey(elementType);
    }

    @Override
    public Object copyAttribute(final int attribute) {
        return proxy.copyAttribute(attribute);
    }

    @Override
    public NativeAttributeType getNativeAttributeType(final int attribute) {
        return proxy.getNativeAttributeType(attribute);
    }

    @Override
    public Object createWriteAttributeObject(final int attribute, final IntReadable indexReadable) {
        return proxy.createWriteAttributeObject(attribute, indexReadable);
    }

    @Override
    public Object createReadAttributeObject(final int attribute, final IntReadable indexReadable) {
        return proxy.createReadAttributeObject(attribute, indexReadable);
    }

    @Override
    public boolean isPrimaryKey(final int attribute) {
        return proxy.isPrimaryKey(attribute);
    }

    @Override
    public long getEdgeUID(final int edge) {
        return proxy.getEdgeUID(edge);
    }

    @Override
    public long getLinkUID(final int link) {
        return proxy.getLinkUID(link);
    }

    @Override
    public long getTransactionUID(final int transaction) {
        return proxy.getTransactionUID(transaction);
    }

    @Override
    public long getVertexUID(final int vertex) {
        return proxy.getVertexUID(vertex);
    }

    @Override
    public long getAttributeUID(final int attribute) {
        return proxy.getAttributeUID(attribute);
    }

    @Override
    public void executeGraphOperation(final GraphOperation operation) {
        proxy.executeGraphOperation(operation);
    }

    @Override
    public boolean isRecordingEdit() {
        return proxy.isRecordingEdit();
    }

    @Override
    public void setAttributeIndexType(final int attribute, final GraphIndexType indexType) {
        proxy.setAttributeIndexType(attribute, indexType);
    }

    @Override
    public boolean attributeSupportsIndexType(final int attribute, final GraphIndexType indexType) {
        return proxy.attributeSupportsIndexType(attribute, indexType);
    }

    @Override
    public GraphIndexResult getElementsWithAttributeValue(final int attribute, final Object value) {
        return proxy.getElementsWithAttributeValue(attribute, value);
    }

    @Override
    public GraphIndexResult getElementsWithAttributeValueRange(final int attribute, final Object start, final Object end) {
        return proxy.getElementsWithAttributeValueRange(attribute, start, end);
    }

    @Override
    public GraphIndexType getAttributeIndexType(final int attribute) {
        return proxy.getAttributeIndexType(attribute);
    }

    @Override
    public IntStream vertexStream() {
        return proxy.vertexStream();
    }

    @Override
    public IntStream linkStream() {
        return proxy.linkStream();
    }

    @Override
    public IntStream edgeStream() {
        return proxy.edgeStream();
    }

    @Override
    public IntStream transactionStream() {
        return proxy.transactionStream();
    }

    @Override
    public boolean isDefaultValue(final int attribute, final int id) {
        return proxy.isDefaultValue(attribute, id);
    }

    @Override
    public int getTransactionCapacity() {
        return proxy.getTransactionCapacity();
    }

    @Override
    public String getAttributeType(final int attribute) {
        return proxy.getAttributeType(attribute);
    }

    @Override
    public String getAttributeDescription(final int attribute) {
        return proxy.getAttributeDescription(attribute);
    }

    @Override
    public GraphElementType getAttributeElementType(final int attribute) {
        return proxy.getAttributeElementType(attribute);
    }

    @Override
    public Class<? extends AttributeDescription> getAttributeDataType(final int attribute) {
        return proxy.getAttributeDataType(attribute);
    }

    @Override
    public Object getAttributeDefaultValue(final int attribute) {
        return proxy.getAttributeDefaultValue(attribute);
    }
}
