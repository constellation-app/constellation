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
package au.gov.asd.tac.constellation.graph.undo;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphIndexType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.operations.GraphOperation;

/**
 * A GraphEdit represents a single undo/redo step in the undo stack that is
 * created as a graph is modified.
 *
 * @author sirius
 */
public interface GraphEdit {

    public void execute(final GraphWriteMethods graph);

    public void undo(final GraphWriteMethods graph);

    public void addChild(final GraphEdit childEdit);

    public void finish();

    public void setPrimaryKey(final GraphElementType elementType, final int[] oldKeys, final int[] newKeys);

    public void addVertex(final int vertex);

    public void removeVertex(final int vertex);

    public void addTransaction(final int sourceVertex, final int destinationVertex, final boolean directed, final int transaction);

    public void removeTransaction(final int sourceVertex, final int destinationVertex, final boolean directed, final int transaction);

    /**
     * Sets the source vertex of a transaction.
     *
     * @param transaction the transaction that is having its source vertex set.
     * @param oldSourceVertex the current source vertex of the transaction.
     * @param newSourceVertex the new source vertex of the transaction.
     * @param reverseTransaction a flag indicating if this will cause the
     * transaction to be reversed to ensure that the source vertex is not
     * greater than the destination vertex (undirected transactions only).
     */
    public void setTransactionSourceVertex(final int transaction, final int oldSourceVertex,
            final int newSourceVertex, final boolean reverseTransaction);

    /**
     * Sets the destination vertex of a transaction.
     *
     * @param transaction the transaction that is having its destination vertex
     * set.
     * @param oldDestinationVertex the current destination vertex of the
     * transaction.
     * @param newDestinationVertex the new destination vertex of the
     * transaction.
     * @param reverseTransaction a flag indicating if this will cause the
     * transaction to be reversed to ensure that the source vertex is not
     * greater than the destination vertex (undirected transactions only).
     */
    public void setTransactionDestinationVertex(final int transaction,
            final int oldDestinationVertex, final int newDestinationVertex, final boolean reverseTransaction);

    public void addAttribute(final GraphElementType elementType, final String attributeType, final String label,
            final String description, final Object defaultValue, final String attributeMergerId, final int attribute);

    public void removeAttribute(final GraphElementType elementType, final String attributeType, final String label,
            final String description, final Object defaultValue, final String attributeMergerId, final int attribute);

    public void updateAttributeName(final int attribute, final String oldName, final String newName);

    public void updateAttributeDescription(final int attribute, final String oldDescription, final String newDescription);

    public void updateAttributeDefaultValue(final int attribute, final Object oldObject, final Object newObject);

    public void setByteValue(final int attribute, final int id, final byte oldValue, final byte newValue);

    public void setShortValue(final int attribute, final int id, final short oldValue, final short newValue);

    public void setIntValue(final int attribute, final int id, final int oldValue, final int newValue);

    public void setLongValue(final int attribute, final int id, final long oldValue, final long newValue);

    public void setFloatValue(final int attribute, final int id, final float oldValue, final float newValue);

    public void setDoubleValue(final int attribute, final int id, final double oldValue, final double newValue);

    public void setBooleanValue(final int attribute, final int id, final boolean oldValue, final boolean newValue);

    public void setCharValue(final int attribute, final int id, final char oldValue, final char newValue);

    public void setObjectValue(final int attribute, final int id, final Object oldValue, final Object newValue);

    public void executeGraphOperation(final GraphOperation operation);

    public void setAttributeIndexType(final int attribute, final GraphIndexType oldValue, final GraphIndexType newValue);
}
