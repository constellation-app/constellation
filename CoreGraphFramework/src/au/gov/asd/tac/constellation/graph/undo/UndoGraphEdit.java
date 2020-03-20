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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Arrays;

/**
 *
 * @author sirius
 */
public class UndoGraphEdit implements GraphEdit {

    private static final boolean VERBOSE = false;

    private final UndoGraphEditState state;

    public UndoGraphEdit() {
        state = new UndoGraphEditState();
    }

    public UndoGraphEdit(DataInputStream in) throws Exception {
        state = new UndoGraphEditState(in);
    }

    @Override
    public void addChild(GraphEdit childEdit) {
        int childIndex = state.addObject(childEdit);
        UndoGraphEditOperation.EXECUTE_CHILD.addOperation(state, state.currentAttribute, state.currentId, state.currentInt, state.currentLong, childIndex);
    }

    @Override
    public void finish() {
        state.finish();
    }

    @Override
    public void execute(GraphWriteMethods graph) {
        state.execute(graph);
    }

    @Override
    public void undo(GraphWriteMethods graph) {
        state.undo(graph);
    }

    @Override
    public void setPrimaryKey(GraphElementType elementType, int[] oldKeys, int[] newKeys) {
        if (VERBOSE) {
            System.out.println("setPrimaryKey(" + elementType + ", " + Arrays.toString(oldKeys) + ", " + Arrays.toString(newKeys) + ")");
        }
        int oldIndex = state.addObject(oldKeys);
        int newIndex = state.addObject(newKeys);
        UndoGraphEditOperation.SET_PRIMARY_KEY.addOperation(state, elementType.ordinal(), oldIndex, state.currentInt, state.currentLong, newIndex);
    }

    @Override
    public void addVertex(int vertex) {
        if (VERBOSE) {
            System.out.println("addVertex(" + vertex + ")");
        }
        UndoGraphEditOperation.ADD_VERTEX.addOperation(state, state.currentAttribute, vertex, state.currentInt, state.currentLong, state.currentObject);
    }

    @Override
    public void removeVertex(int vertex) {
        if (VERBOSE) {
            System.out.println("removeVertex(" + vertex + ")");
        }
        UndoGraphEditOperation.REMOVE_VERTEX.addOperation(state, state.currentAttribute, vertex, state.currentInt, state.currentLong, state.currentObject);
    }

    @Override
    public void addTransaction(int sourceVertex, int destinationVertex, boolean directed, int transaction) {
        if (VERBOSE) {
            System.out.println("addTransaction(" + sourceVertex + ", " + destinationVertex + ", " + directed + ", " + transaction + ")");
        }
        if (directed) {
            UndoGraphEditOperation.ADD_DIRECTED_TRANSACTION.addOperation(state, state.currentAttribute, transaction, destinationVertex, state.currentLong, sourceVertex);
        } else {
            UndoGraphEditOperation.ADD_UNDIRECTED_TRANSACTION.addOperation(state, state.currentAttribute, transaction, destinationVertex, state.currentLong, sourceVertex);
        }
    }

    @Override
    public void removeTransaction(int sourceVertex, int destinationVertex, boolean directed, int transaction) {
        if (VERBOSE) {
            System.out.println("removeTransaction(" + sourceVertex + ", " + destinationVertex + ", " + directed + ", " + transaction + ")");
        }
        if (directed) {
            UndoGraphEditOperation.REMOVE_DIRECTED_TRANSACTION.addOperation(state, state.currentAttribute, transaction, destinationVertex, state.currentLong, sourceVertex);
        } else {
            UndoGraphEditOperation.REMOVE_UNDIRECTED_TRANSACTION.addOperation(state, state.currentAttribute, transaction, destinationVertex, state.currentLong, sourceVertex);
        }
    }

    @Override
    public void setTransactionSourceVertex(int transaction, int oldSourceVertex, int newSourceVertex, boolean reverseTransaction) {
        if (VERBOSE) {
            System.out.println("setTransactionSourceVertex(" + transaction + ", " + oldSourceVertex + ", " + newSourceVertex + ")");
        }
        // If the reverse transaction has been set then set this means that the transaction source and destination vertices will need to be swapped to ensure that the source vertex is not greater than the destination vertex.
        // If this is the case then set the high bit of the transaction id so that the undo step knowns to adjust to handle this case.
        UndoGraphEditOperation.SET_TRANSACTION_SOURCE_VERTEX.addOperation(state, state.currentAttribute, transaction | (reverseTransaction ? 0x80000000 : 0), oldSourceVertex ^ newSourceVertex, state.currentLong, state.currentObject);
    }

    @Override
    public void setTransactionDestinationVertex(int transaction, int oldDestinationVertex, int newDestinationVertex, boolean reverseTransaction) {
        if (VERBOSE) {
            System.out.println("setTransactionDestinationVertex(" + transaction + ", " + oldDestinationVertex + ", " + newDestinationVertex + ")");
        }

        // If the reverse transaction has been set then set this means that the transaction source and destination vertices will need to be swapped to ensure that the source vertex is not greater than the destination vertex.
        // If this is the case then set the high bit of the transaction id so that the undo step knowns to adjust to handle this case.
        UndoGraphEditOperation.SET_TRANSACTION_DESTINATION_VERTEX.addOperation(state, state.currentAttribute, transaction | (reverseTransaction ? 0x80000000 : 0), oldDestinationVertex ^ newDestinationVertex, state.currentLong, state.currentObject);
    }

    @Override
    public void addAttribute(GraphElementType elementType, String attributeType, String label, String description, Object defaultValue, String attributeMergerId, int attribute) {
        if (VERBOSE) {
            System.out.println("addAttribute(" + elementType + ", " + attributeType + ", " + label + ", " + description + ", " + defaultValue + ", " + attribute + ")");
        }
        Object[] params = new Object[]{elementType, attributeType, label, description, defaultValue, attributeMergerId};
        int paramsIndex = state.addObject(params);
        UndoGraphEditOperation.ADD_ATTRIBUTE.addOperation(state, attribute, state.currentId, state.currentInt, state.currentLong, paramsIndex);
    }

    @Override
    public void removeAttribute(GraphElementType elementType, String attributeType, String label, String description, Object defaultValue, String attributeMergerId, int attribute) {
        if (VERBOSE) {
            System.out.println("removeAttribute(" + elementType + ", " + attributeType + ", " + label + ", " + description + ", " + defaultValue + ", " + attribute + ")");
        }
        Object[] params = new Object[]{elementType, attributeType, label, description, defaultValue, attributeMergerId};
        int paramsIndex = state.addObject(params);
        UndoGraphEditOperation.REMOVE_ATTRIBUTE.addOperation(state, attribute, state.currentId, state.currentInt, state.currentLong, paramsIndex);
    }

    @Override
    public void updateAttributeName(int attribute, String oldName, String newName) {
        if (VERBOSE) {
            System.out.println("updateAttributeName(" + attribute + ", " + oldName + ", " + newName + ")");
        }
        int oldIndex = state.addObject(oldName);
        int newIndex = state.addObject(newName);
        UndoGraphEditOperation.UPDATE_ATTRIBUTE_NAME.addOperation(state, attribute, state.currentId, oldIndex, state.currentLong, newIndex);
    }

    @Override
    public void updateAttributeDescription(int attribute, String oldDescription, String newDescription) {
        if (VERBOSE) {
            System.out.println("updateAttributeDescription(" + attribute + ", " + oldDescription + ", " + newDescription + ")");
        }
        int oldIndex = state.addObject(oldDescription);
        int newIndex = state.addObject(newDescription);
        UndoGraphEditOperation.UPDATE_ATTRIBUTE_DESCRIPTION.addOperation(state, attribute, state.currentId, oldIndex, state.currentLong, newIndex);
    }

    @Override
    public void updateAttributeDefaultValue(int attribute, Object oldDefault, Object newDefault) {
        if (VERBOSE) {
            System.out.println("updateAttributeDefaultValue(" + attribute + ", " + oldDefault + ", " + newDefault + ")");
        }
        int oldIndex = state.addObject(oldDefault);
        int newIndex = state.addObject(newDefault);
        UndoGraphEditOperation.UPDATE_ATTRIBUTE_DEFAULT_VALUE.addOperation(state, attribute, state.currentId, oldIndex, state.currentLong, newIndex);
    }

    @Override
    public void setByteValue(int attribute, int id, byte oldValue, byte newValue) {
        if (VERBOSE) {
            System.out.println("setByteValue(" + attribute + ", " + id + ", " + oldValue + ", " + newValue);
        }
        UndoGraphEditOperation.SET_BYTE_VALUE.addOperation(state, attribute, id, newValue ^ oldValue, state.currentLong, state.currentObject);
    }

    @Override
    public void setShortValue(int attribute, int id, short oldValue, short newValue) {
        if (VERBOSE) {
            System.out.println("setShortValue(" + attribute + ", " + id + ", " + oldValue + ", " + newValue + ")");
        }
        UndoGraphEditOperation.SET_SHORT_VALUE.addOperation(state, attribute, id, newValue ^ oldValue, state.currentLong, state.currentObject);
    }

    @Override
    public void setIntValue(int attribute, int id, int oldValue, int newValue) {
        if (VERBOSE) {
            System.out.println("setIntValue(" + attribute + ", " + id + ", " + oldValue + ", " + newValue + ")");
        }
        UndoGraphEditOperation.SET_INT_VALUE.addOperation(state, attribute, id, newValue - oldValue, state.currentLong, state.currentObject);
    }

    @Override
    public void setLongValue(int attribute, int id, long oldValue, long newValue) {
        if (VERBOSE) {
            System.out.println("setLongValue(" + attribute + ", " + id + ", " + oldValue + ", " + newValue + ")");
        }
        UndoGraphEditOperation.SET_LONG_VALUE.addOperation(state, attribute, id, state.currentInt, newValue - oldValue, state.currentObject);
    }

    @Override
    public void setFloatValue(int attribute, int id, float oldValue, float newValue) {
        if (VERBOSE) {
            System.out.println("setFloatValue(" + attribute + ", " + id + ", " + oldValue + ", " + newValue + ")");
        }
        UndoGraphEditOperation.SET_FLOAT_VALUE.addOperation(state, attribute, id, Float.floatToRawIntBits(oldValue) ^ Float.floatToRawIntBits(newValue), 0, 0);
    }

    @Override
    public void setDoubleValue(int attribute, int id, double oldValue, double newValue) {
        if (VERBOSE) {
            System.out.println("setDoubleValue(" + attribute + ", " + id + ", " + oldValue + ", " + newValue + ")");
        }
        UndoGraphEditOperation.SET_DOUBLE_VALUE.addOperation(state, attribute, id, 0, Double.doubleToRawLongBits(oldValue) ^ Double.doubleToRawLongBits(newValue), 0);
    }

    @Override
    public void setBooleanValue(int attribute, int id, boolean oldValue, boolean newValue) {
        if (VERBOSE) {
            System.out.println("setBooleanValue(" + attribute + ", " + id + ", " + oldValue + ", " + newValue + ")");
        }
        if (newValue) {
            UndoGraphEditOperation.SET_BOOLEAN_VALUE_TRUE.addOperation(state, attribute, id, state.currentInt, state.currentLong, state.currentObject);
        } else {
            UndoGraphEditOperation.SET_BOOLEAN_VALUE_FALSE.addOperation(state, attribute, id, state.currentInt, state.currentLong, state.currentObject);
        }
    }

    @Override
    public void setCharValue(int attribute, int id, char oldValue, char newValue) {
        if (VERBOSE) {
            System.out.println("setCharValue(" + attribute + ", " + id + ", " + oldValue + ", " + newValue + ")");
        }
        UndoGraphEditOperation.SET_CHAR_VALUE.addOperation(state, attribute, id, oldValue ^ newValue, state.currentLong, state.currentObject);
    }

    @Override
    public void setObjectValue(int attribute, int id, Object oldValue, Object newValue) {
        if (VERBOSE) {
            System.out.println("setObjectValue(" + attribute + ", " + id + ", " + oldValue + ", " + newValue + ")");
        }
        if (oldValue == null) {
            int newIndex = state.addObject(newValue);
            UndoGraphEditOperation.SET_OBJECT_VALUE_FROM_NULL.addOperation(state, attribute, id, state.currentInt, state.currentLong, newIndex);
        } else if (newValue == null) {
            int oldIndex = state.addObject(oldValue);
            UndoGraphEditOperation.SET_OBJECT_VALUE_TO_NULL.addOperation(state, attribute, id, state.currentInt, state.currentLong, oldIndex);
        } else {
            int oldIndex = state.addObject(oldValue);
            int newIndex = state.addObject(newValue);
            UndoGraphEditOperation.SET_OBJECT_VALUE.addOperation(state, attribute, id, oldIndex, state.currentLong, newIndex);
        }
    }

    @Override
    public void setAttributeIndexType(int attribute, GraphIndexType oldValue, GraphIndexType newValue) {
        if (VERBOSE) {
            System.out.println("setAttributeIndexType(" + attribute + ", " + oldValue + ", " + newValue + ")");
        }
        int delta = newValue.ordinal() - oldValue.ordinal();
        UndoGraphEditOperation.SET_ATTRIBUTE_INDEX_TYPE.addOperation(state, attribute, state.currentId, state.currentInt, state.currentLong, delta);
    }

    @Override
    public void executeGraphOperation(GraphOperation operation) {
        if (VERBOSE) {
            System.out.println("executeGraphOperation()");
        }
        int objectIndex = state.addObject(operation);
        UndoGraphEditOperation.EXECUTE_GRAPH_OPERATION.addOperation(state, state.currentAttribute, state.currentId, state.currentInt, state.currentLong, objectIndex);
    }

    public void write(DataOutputStream out) throws Exception {
        state.write(out);
    }
}
