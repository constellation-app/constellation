/*
 * Copyright 2010-2020 Australian Signals Directorate
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

    public UndoGraphEdit(final DataInputStream in) throws Exception {
        state = new UndoGraphEditState(in);
    }

    @Override
    public void addChild(final GraphEdit childEdit) {
        int childIndex = state.addObject(childEdit);
        UndoGraphEditOperation.EXECUTE_CHILD.addOperation(state, state.getCurrentAttribute(),
                state.getCurrentId(), state.getCurrentInt(), state.getCurrentLong(), childIndex);
    }

    @Override
    public void finish() {
        state.finish();
    }

    @Override
    public void execute(final GraphWriteMethods graph) {
        state.execute(graph);
    }

    @Override
    public void undo(final GraphWriteMethods graph) {
        state.undo(graph);
    }

    @Override
    public void setPrimaryKey(final GraphElementType elementType, final int[] oldKeys, final int[] newKeys) {
        if (VERBOSE) {
            System.out.println("setPrimaryKey(" + elementType + ", " + Arrays.toString(oldKeys) + ", " + Arrays.toString(newKeys) + ")");
        }
        int oldIndex = state.addObject(oldKeys);
        int newIndex = state.addObject(newKeys);
        UndoGraphEditOperation.SET_PRIMARY_KEY.addOperation(state, elementType.ordinal(),
                oldIndex, state.getCurrentInt(), state.getCurrentLong(), newIndex);
    }

    @Override
    public void addVertex(final int vertex) {
        if (VERBOSE) {
            System.out.println("addVertex(" + vertex + ")");
        }
        UndoGraphEditOperation.ADD_VERTEX.addOperation(state, state.getCurrentAttribute(),
                vertex, state.getCurrentInt(), state.getCurrentLong(), state.getCurrentObject());
    }

    @Override
    public void removeVertex(final int vertex) {
        if (VERBOSE) {
            System.out.println("removeVertex(" + vertex + ")");
        }
        UndoGraphEditOperation.REMOVE_VERTEX.addOperation(state, state.getCurrentAttribute(),
                vertex, state.getCurrentInt(), state.getCurrentLong(), state.getCurrentObject());
    }

    @Override
    public void addTransaction(final int sourceVertex, final int destinationVertex, final boolean directed, final int transaction) {
        if (VERBOSE) {
            System.out.println("addTransaction(" + sourceVertex + ", " + destinationVertex + ", " + directed + ", " + transaction + ")");
        }
        if (directed) {
            UndoGraphEditOperation.ADD_DIRECTED_TRANSACTION.addOperation(state, state.getCurrentAttribute(),
                    transaction, destinationVertex, state.getCurrentLong(), sourceVertex);
        } else {
            UndoGraphEditOperation.ADD_UNDIRECTED_TRANSACTION.addOperation(state, state.getCurrentAttribute(),
                    transaction, destinationVertex, state.getCurrentLong(), sourceVertex);
        }
    }

    @Override
    public void removeTransaction(final int sourceVertex, final int destinationVertex, final boolean directed, final int transaction) {
        if (VERBOSE) {
            System.out.println("removeTransaction(" + sourceVertex + ", " + destinationVertex + ", " + directed + ", " + transaction + ")");
        }
        if (directed) {
            UndoGraphEditOperation.REMOVE_DIRECTED_TRANSACTION.addOperation(state, state.getCurrentAttribute(),
                    transaction, destinationVertex, state.getCurrentLong(), sourceVertex);
        } else {
            UndoGraphEditOperation.REMOVE_UNDIRECTED_TRANSACTION.addOperation(state, state.getCurrentAttribute(),
                    transaction, destinationVertex, state.getCurrentLong(), sourceVertex);
        }
    }

    @Override
    public void setTransactionSourceVertex(final int transaction, final int oldSourceVertex, final int newSourceVertex, final boolean reverseTransaction) {
        if (VERBOSE) {
            System.out.println("setTransactionSourceVertex(" + transaction + ", " + oldSourceVertex + ", " + newSourceVertex + ")");
        }
        // If the reverse transaction has been set then set this means that the transaction source and destination vertices will need 
        //to be swapped to ensure that the source vertex is not greater than the destination vertex.
        // If this is the case then set the high bit of the transaction id so that the undo step knowns to adjust to handle this case.
        UndoGraphEditOperation.SET_TRANSACTION_SOURCE_VERTEX.addOperation(state, state.getCurrentAttribute(),
                transaction | (reverseTransaction ? 0x80000000 : 0), oldSourceVertex ^ newSourceVertex, state.getCurrentLong(), state.getCurrentObject());
    }

    @Override
    public void setTransactionDestinationVertex(final int transaction, final int oldDestinationVertex, final int newDestinationVertex, final boolean reverseTransaction) {
        if (VERBOSE) {
            System.out.println("setTransactionDestinationVertex(" + transaction + ", " + oldDestinationVertex + ", " + newDestinationVertex + ")");
        }

        // If the reverse transaction has been set then set this means that the transaction source and destination vertices will need 
        // to be swapped to ensure that the source vertex is not greater than the destination vertex.
        // If this is the case then set the high bit of the transaction id so that the undo step knowns to adjust to handle this case.
        UndoGraphEditOperation.SET_TRANSACTION_DESTINATION_VERTEX.addOperation(state, state.getCurrentAttribute(),
                transaction | (reverseTransaction ? 0x80000000 : 0), oldDestinationVertex ^ newDestinationVertex, state.getCurrentLong(), state.getCurrentObject());
    }

    @Override
    public void addAttribute(final GraphElementType elementType, final String attributeType, final String label,
            final String description, final Object defaultValue, final String attributeMergerId, final int attribute) {
        if (VERBOSE) {
            System.out.println("addAttribute(" + elementType + ", " + attributeType + ", "
                    + label + ", " + description + ", " + defaultValue + ", " + attribute + ")");
        }
        Object[] params = new Object[]{elementType, attributeType, label, description, defaultValue, attributeMergerId};
        int paramsIndex = state.addObject(params);
        UndoGraphEditOperation.ADD_ATTRIBUTE.addOperation(state, attribute,
                state.getCurrentId(), state.getCurrentInt(), state.getCurrentLong(), paramsIndex);
    }

    @Override
    public void removeAttribute(final GraphElementType elementType, final String attributeType, final String label,
            final String description, final Object defaultValue, final String attributeMergerId, final int attribute) {
        if (VERBOSE) {
            System.out.println("removeAttribute(" + elementType + ", " + attributeType + ", "
                    + label + ", " + description + ", " + defaultValue + ", " + attribute + ")");
        }
        Object[] params = new Object[]{elementType, attributeType, label, description, defaultValue, attributeMergerId};
        int paramsIndex = state.addObject(params);
        UndoGraphEditOperation.REMOVE_ATTRIBUTE.addOperation(state, attribute, state.getCurrentId(),
                state.getCurrentInt(), state.getCurrentLong(), paramsIndex);
    }

    @Override
    public void updateAttributeName(final int attribute, final String oldName, final String newName) {
        if (VERBOSE) {
            System.out.println("updateAttributeName(" + attribute + ", " + oldName + ", " + newName + ")");
        }
        int oldIndex = state.addObject(oldName);
        int newIndex = state.addObject(newName);
        UndoGraphEditOperation.UPDATE_ATTRIBUTE_NAME.addOperation(state, attribute,
                state.getCurrentId(), oldIndex, state.getCurrentLong(), newIndex);
    }

    @Override
    public void updateAttributeDescription(final int attribute, final String oldDescription, final String newDescription) {
        if (VERBOSE) {
            System.out.println("updateAttributeDescription(" + attribute + ", " + oldDescription + ", " + newDescription + ")");
        }
        int oldIndex = state.addObject(oldDescription);
        int newIndex = state.addObject(newDescription);
        UndoGraphEditOperation.UPDATE_ATTRIBUTE_DESCRIPTION.addOperation(state, attribute,
                state.getCurrentId(), oldIndex, state.getCurrentLong(), newIndex);
    }

    @Override
    public void updateAttributeDefaultValue(final int attribute, final Object oldDefault, final Object newDefault) {
        if (VERBOSE) {
            System.out.println("updateAttributeDefaultValue(" + attribute + ", " + oldDefault + ", " + newDefault + ")");
        }
        int oldIndex = state.addObject(oldDefault);
        int newIndex = state.addObject(newDefault);
        UndoGraphEditOperation.UPDATE_ATTRIBUTE_DEFAULT_VALUE.addOperation(state, attribute,
                state.getCurrentId(), oldIndex, state.getCurrentLong(), newIndex);
    }

    @Override
    public void setByteValue(final int attribute, final int id, final byte oldValue, final byte newValue) {
        if (VERBOSE) {
            System.out.println("setByteValue(" + attribute + ", " + id + ", " + oldValue + ", " + newValue);
        }
        UndoGraphEditOperation.SET_BYTE_VALUE.addOperation(state, attribute, id,
                newValue ^ oldValue, state.getCurrentLong(), state.getCurrentObject());
    }

    @Override
    public void setShortValue(final int attribute, final int id, final short oldValue, final short newValue) {
        if (VERBOSE) {
            System.out.println("setShortValue(" + attribute + ", " + id + ", " + oldValue + ", " + newValue + ")");
        }
        UndoGraphEditOperation.SET_SHORT_VALUE.addOperation(state, attribute, id,
                newValue ^ oldValue, state.getCurrentLong(), state.getCurrentObject());
    }

    @Override
    public void setIntValue(final int attribute, final int id, final int oldValue, final int newValue) {
        if (VERBOSE) {
            System.out.println("setIntValue(" + attribute + ", " + id + ", " + oldValue + ", " + newValue + ")");
        }
        UndoGraphEditOperation.SET_INT_VALUE.addOperation(state, attribute, id,
                newValue - oldValue, state.getCurrentLong(), state.getCurrentObject());
    }

    @Override
    public void setLongValue(final int attribute, final int id, final long oldValue, final long newValue) {
        if (VERBOSE) {
            System.out.println("setLongValue(" + attribute + ", " + id + ", " + oldValue + ", " + newValue + ")");
        }
        UndoGraphEditOperation.SET_LONG_VALUE.addOperation(state, attribute, id,
                state.getCurrentInt(), newValue - oldValue, state.getCurrentObject());
    }

    @Override
    public void setFloatValue(final int attribute, final int id, final float oldValue, final float newValue) {
        if (VERBOSE) {
            System.out.println("setFloatValue(" + attribute + ", " + id + ", " + oldValue + ", " + newValue + ")");
        }
        UndoGraphEditOperation.SET_FLOAT_VALUE.addOperation(state, attribute, id,
                Float.floatToRawIntBits(oldValue) ^ Float.floatToRawIntBits(newValue), 0, 0);
    }

    @Override
    public void setDoubleValue(final int attribute, final int id, final double oldValue, final double newValue) {
        if (VERBOSE) {
            System.out.println("setDoubleValue(" + attribute + ", " + id + ", " + oldValue + ", " + newValue + ")");
        }
        UndoGraphEditOperation.SET_DOUBLE_VALUE.addOperation(state, attribute, id,
                0, Double.doubleToRawLongBits(oldValue) ^ Double.doubleToRawLongBits(newValue), 0);
    }

    @Override
    public void setBooleanValue(final int attribute, final int id, final boolean oldValue, final boolean newValue) {
        if (VERBOSE) {
            System.out.println("setBooleanValue(" + attribute + ", " + id + ", " + oldValue + ", " + newValue + ")");
        }
        if (newValue) {
            UndoGraphEditOperation.SET_BOOLEAN_VALUE_TRUE.addOperation(state, attribute, id,
                    state.getCurrentInt(), state.getCurrentLong(), state.getCurrentObject());
        } else {
            UndoGraphEditOperation.SET_BOOLEAN_VALUE_FALSE.addOperation(state, attribute, id,
                    state.getCurrentInt(), state.getCurrentLong(), state.getCurrentObject());
        }
    }

    @Override
    public void setCharValue(final int attribute, final int id, final char oldValue, final char newValue) {
        if (VERBOSE) {
            System.out.println("setCharValue(" + attribute + ", " + id + ", " + oldValue + ", " + newValue + ")");
        }
        UndoGraphEditOperation.SET_CHAR_VALUE.addOperation(state, attribute, id,
                oldValue ^ newValue, state.getCurrentLong(), state.getCurrentObject());
    }

    @Override
    public void setObjectValue(final int attribute, final int id, final Object oldValue, final Object newValue) {
        if (VERBOSE) {
            System.out.println("setObjectValue(" + attribute + ", " + id + ", " + oldValue + ", " + newValue + ")");
        }
        if (oldValue == null) {
            int newIndex = state.addObject(newValue);
            UndoGraphEditOperation.SET_OBJECT_VALUE_FROM_NULL.addOperation(state, attribute, id, state.getCurrentInt(), state.getCurrentLong(), newIndex);
        } else if (newValue == null) {
            int oldIndex = state.addObject(oldValue);
            UndoGraphEditOperation.SET_OBJECT_VALUE_TO_NULL.addOperation(state, attribute, id, state.getCurrentInt(), state.getCurrentLong(), oldIndex);
        } else {
            int oldIndex = state.addObject(oldValue);
            int newIndex = state.addObject(newValue);
            UndoGraphEditOperation.SET_OBJECT_VALUE.addOperation(state, attribute, id, oldIndex, state.getCurrentLong(), newIndex);
        }
    }

    @Override
    public void setAttributeIndexType(final int attribute, final GraphIndexType oldValue, final GraphIndexType newValue) {
        if (VERBOSE) {
            System.out.println("setAttributeIndexType(" + attribute + ", " + oldValue + ", " + newValue + ")");
        }
        int delta = newValue.ordinal() - oldValue.ordinal();
        UndoGraphEditOperation.SET_ATTRIBUTE_INDEX_TYPE.addOperation(state, attribute,
                state.getCurrentId(), state.getCurrentInt(), state.getCurrentLong(), delta);
    }

    @Override
    public void executeGraphOperation(final GraphOperation operation) {
        if (VERBOSE) {
            System.out.println("executeGraphOperation()");
        }
        int objectIndex = state.addObject(operation);
        UndoGraphEditOperation.EXECUTE_GRAPH_OPERATION.addOperation(state, state.getCurrentAttribute(),
                state.getCurrentId(), state.getCurrentInt(), state.getCurrentLong(), objectIndex);
    }

    public void write(final DataOutputStream out) throws Exception {
        state.write(out);
    }
}
