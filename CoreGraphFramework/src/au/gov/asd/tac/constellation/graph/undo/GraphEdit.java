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

/**
 * A GraphEdit represents a single undo/redo step in the undo stack that is
 * created as a graph is modified.
 *
 * @author sirius
 */
public interface GraphEdit {

    public void execute(GraphWriteMethods graph);

    public void undo(GraphWriteMethods graph);

    public void addChild(GraphEdit childEdit);

    public void finish();

    public void setPrimaryKey(GraphElementType elementType, int[] oldKeys, int[] newKeys);

    public void addVertex(int vertex);

    public void removeVertex(int vertex);

    public void addTransaction(int sourceVertex, int destinationVertex, boolean directed, int transaction);

    public void removeTransaction(int sourceVertex, int destinationVertex, boolean directed, int transaction);

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
    public void setTransactionSourceVertex(int transaction, int oldSourceVertex, int newSourceVertex, boolean reverseTransaction);

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
    public void setTransactionDestinationVertex(int transaction, int oldDestinationVertex, int newDestinationVertex, boolean reverseTransaction);

    public void addAttribute(GraphElementType elementType, String attributeType, String label, String description, Object defaultValue, String attributeMergerId, int attribute);

    public void removeAttribute(GraphElementType elementType, String attributeType, String label, String description, Object defaultValue, String attributeMergerId, int attribute);

    public void updateAttributeName(int attribute, String oldName, String newName);

    public void updateAttributeDescription(int attribute, String oldDescription, String newDescription);

    public void updateAttributeDefaultValue(int attribute, Object oldObject, Object newObject);

    public void setByteValue(int attribute, int id, byte oldValue, byte newValue);

    public void setShortValue(int attribute, int id, short oldValue, short newValue);

    public void setIntValue(int attribute, int id, int oldValue, int newValue);

    public void setLongValue(int attribute, int id, long oldValue, long newValue);

    public void setFloatValue(int attribute, int id, float oldValue, float newValue);

    public void setDoubleValue(int attribute, int id, double oldValue, double newValue);

    public void setBooleanValue(int attribute, int id, boolean oldValue, boolean newValue);

    public void setCharValue(int attribute, int id, char oldValue, char newValue);

    public void setObjectValue(int attribute, int id, Object oldValue, Object newValue);

    public void executeGraphOperation(GraphOperation operation);

    public void setAttributeIndexType(int attribute, GraphIndexType oldValue, GraphIndexType newValue);

}
