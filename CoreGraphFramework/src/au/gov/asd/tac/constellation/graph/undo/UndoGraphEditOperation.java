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
import au.gov.asd.tac.constellation.graph.undo.access.AttributeValueUpdater2;
import au.gov.asd.tac.constellation.graph.undo.access.AttributeValueUpdater3;
import au.gov.asd.tac.constellation.graph.undo.access.DoubleValueUpdater2;
import au.gov.asd.tac.constellation.graph.undo.access.FloatValueUpdater2;
import au.gov.asd.tac.constellation.graph.undo.access.IdValueUpdater2;
import au.gov.asd.tac.constellation.graph.undo.access.IdValueUpdater3;
import au.gov.asd.tac.constellation.graph.undo.access.IntValueUpdater2;
import au.gov.asd.tac.constellation.graph.undo.access.LongValueUpdater2;
import au.gov.asd.tac.constellation.graph.undo.access.ObjectValueUpdater2;
import au.gov.asd.tac.constellation.graph.undo.access.ObjectValueUpdater4;
import au.gov.asd.tac.constellation.graph.undo.access.ObjectValueUpdater5;

/**
 *
 * @author sirius
 */
public enum UndoGraphEditOperation {

    SET_PRIMARY_KEY("setPrimaryKeyOperation") {

        @Override
        public void execute(UndoGraphEditState state, GraphWriteMethods graph) {
            graph.setPrimaryKey(GraphElementType.values()[state.currentAttribute], (int[]) state.objectStack[state.currentObject]);
        }

        @Override
        public void undo(UndoGraphEditState state, GraphWriteMethods graph) {
            graph.setPrimaryKey(GraphElementType.values()[state.currentAttribute], (int[]) state.objectStack[state.currentId]);
        }

        @Override
        void addOperation(UndoGraphEditState state, int attribute, int id, int i, long l, int o) {
            int instruction = ordinal();
            instruction |= AttributeValueUpdater2.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
            instruction |= IdValueUpdater2.INSTANCE.store(state, id) << (OPERATION_SHIFT + 2);
            instruction |= ObjectValueUpdater2.INSTANCE.store(state, o) << (OPERATION_SHIFT + 4);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(UndoGraphEditState state, int instruction) {
            AttributeValueUpdater2.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IdValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 2));
            ObjectValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 4));
        }

        @Override
        public void updateUndo(UndoGraphEditState state, int instruction) {
            ObjectValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 4));
            IdValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 2));
            AttributeValueUpdater2.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    ADD_VERTEX("addVertexOperation") {

        @Override
        public void execute(UndoGraphEditState state, GraphWriteMethods graph) {
            graph.addVertex();
        }

        @Override
        public void undo(UndoGraphEditState state, GraphWriteMethods graph) {
            graph.removeVertex(state.currentId);
        }

        @Override
        void addOperation(UndoGraphEditState state, int attribute, int id, int i, long l, int o) {
            int instruction = ordinal();
            instruction |= IdValueUpdater3.INSTANCE.store(state, id) << OPERATION_SHIFT;
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(UndoGraphEditState state, int instruction) {
            IdValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
        }

        @Override
        public void updateUndo(UndoGraphEditState state, int instruction) {
            IdValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    REMOVE_VERTEX("removeVertexOperation") {

        @Override
        public void execute(UndoGraphEditState state, GraphWriteMethods graph) {
            graph.removeVertex(state.currentId);
        }

        @Override
        public void undo(UndoGraphEditState state, GraphWriteMethods graph) {
            graph.addVertex();
        }

        @Override
        void addOperation(UndoGraphEditState state, int attribute, int id, int i, long l, int o) {
            int instruction = ordinal();
            instruction |= IdValueUpdater3.INSTANCE.store(state, id) << OPERATION_SHIFT;
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(UndoGraphEditState state, int instruction) {
            IdValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
        }

        @Override
        public void updateUndo(UndoGraphEditState state, int instruction) {
            IdValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    ADD_DIRECTED_TRANSACTION("addDirectedTransactionOperation") {

        @Override
        public void execute(UndoGraphEditState state, GraphWriteMethods graph) {
            graph.addTransaction(state.currentObject, state.currentInt, true);
        }

        @Override
        public void undo(UndoGraphEditState state, GraphWriteMethods graph) {
            graph.removeTransaction(state.currentId);
        }

        @Override
        void addOperation(UndoGraphEditState state, int attribute, int id, int i, long l, int o) {
            int instruction = ordinal();
            instruction |= IdValueUpdater3.INSTANCE.store(state, id) << OPERATION_SHIFT;
            instruction |= IntValueUpdater2.INSTANCE.store(state, i) << (OPERATION_SHIFT + 3);
            instruction |= ObjectValueUpdater2.INSTANCE.store(state, o) << (OPERATION_SHIFT + 5);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(UndoGraphEditState state, int instruction) {
            IdValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IntValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
            ObjectValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 5));
        }

        @Override
        public void updateUndo(UndoGraphEditState state, int instruction) {
            ObjectValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 5));
            IntValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            IdValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    ADD_UNDIRECTED_TRANSACTION("addUndirectedTransactionOperation") {

        @Override
        public void execute(UndoGraphEditState state, GraphWriteMethods graph) {
            graph.addTransaction(state.currentObject, state.currentInt, false);
        }

        @Override
        public void undo(UndoGraphEditState state, GraphWriteMethods graph) {
            graph.removeTransaction(state.currentId);
        }

        @Override
        void addOperation(UndoGraphEditState state, int attribute, int id, int i, long l, int o) {
            int instruction = ordinal();
            instruction |= IdValueUpdater3.INSTANCE.store(state, id) << OPERATION_SHIFT;
            instruction |= IntValueUpdater2.INSTANCE.store(state, i) << (OPERATION_SHIFT + 3);
            instruction |= ObjectValueUpdater2.INSTANCE.store(state, o) << (OPERATION_SHIFT + 5);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(UndoGraphEditState state, int instruction) {
            IdValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IntValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
            ObjectValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 5));
        }

        @Override
        public void updateUndo(UndoGraphEditState state, int instruction) {
            ObjectValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 5));
            IntValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            IdValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    REMOVE_DIRECTED_TRANSACTION("removeDirectedTransactionOperation") {

        @Override
        public void execute(UndoGraphEditState state, GraphWriteMethods graph) {
            graph.removeTransaction(state.currentId);
        }

        @Override
        public void undo(UndoGraphEditState state, GraphWriteMethods graph) {
            graph.addTransaction(state.currentObject, state.currentInt, true);
        }

        @Override
        void addOperation(UndoGraphEditState state, int attribute, int id, int i, long l, int o) {
            int instruction = ordinal();
            instruction |= IdValueUpdater3.INSTANCE.store(state, id) << OPERATION_SHIFT;
            instruction |= IntValueUpdater2.INSTANCE.store(state, i) << (OPERATION_SHIFT + 3);
            instruction |= ObjectValueUpdater2.INSTANCE.store(state, o) << (OPERATION_SHIFT + 5);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(UndoGraphEditState state, int instruction) {
            IdValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IntValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
            ObjectValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 5));
        }

        @Override
        public void updateUndo(UndoGraphEditState state, int instruction) {
            ObjectValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 5));
            IntValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            IdValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    REMOVE_UNDIRECTED_TRANSACTION("removeUndirectedTransactionOperation") {

        @Override
        public void execute(UndoGraphEditState state, GraphWriteMethods graph) {
            graph.removeTransaction(state.currentId);
        }

        @Override
        public void undo(UndoGraphEditState state, GraphWriteMethods graph) {
            graph.addTransaction(state.currentObject, state.currentInt, false);
        }

        @Override
        void addOperation(UndoGraphEditState state, int attribute, int id, int i, long l, int o) {
            int instruction = ordinal();
            instruction |= IdValueUpdater3.INSTANCE.store(state, id) << OPERATION_SHIFT;
            instruction |= IntValueUpdater2.INSTANCE.store(state, i) << (OPERATION_SHIFT + 3);
            instruction |= ObjectValueUpdater2.INSTANCE.store(state, o) << (OPERATION_SHIFT + 5);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(UndoGraphEditState state, int instruction) {
            IdValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IntValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
            ObjectValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 5));
        }

        @Override
        public void updateUndo(UndoGraphEditState state, int instruction) {
            ObjectValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 5));
            IntValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            IdValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    SET_TRANSACTION_SOURCE_VERTEX("setTransactionSourceVertex") {

        @Override
        public void execute(UndoGraphEditState state, GraphWriteMethods graph) {
            graph.setTransactionSourceVertex(state.currentId & 0x7FFFFFFF, state.currentInt ^ graph.getTransactionSourceVertex(state.currentId));
        }

        @Override
        public void undo(UndoGraphEditState state, GraphWriteMethods graph) {
            // If the high bit of the current id has been set then the transaction source and destination vertices will have been swapped during the execute phase
            // to keep the source vertex <= the destination vertex. This means that the undo step must operate on the destination vertex instead of the source vertex.
            if (state.currentId >= 0) {
                graph.setTransactionSourceVertex(state.currentId, state.currentInt ^ graph.getTransactionSourceVertex(state.currentId));
            } else {
                graph.setTransactionDestinationVertex(state.currentId & 0x7FFFFFFF, state.currentInt ^ graph.getTransactionDestinationVertex(state.currentId));
            }
        }

        @Override
        void addOperation(UndoGraphEditState state, int attribute, int id, int i, long l, int o) {
            int instruction = ordinal();
            instruction |= IdValueUpdater2.INSTANCE.store(state, id) << OPERATION_SHIFT;
            instruction |= IntValueUpdater2.INSTANCE.store(state, i) << (OPERATION_SHIFT + 2);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(UndoGraphEditState state, int instruction) {
            IdValueUpdater2.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IntValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 2));
        }

        @Override
        public void updateUndo(UndoGraphEditState state, int instruction) {
            IntValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 2));
            IdValueUpdater2.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    SET_TRANSACTION_DESTINATION_VERTEX("setTransactionDestinationVertex") {

        @Override
        public void execute(UndoGraphEditState state, GraphWriteMethods graph) {
            graph.setTransactionDestinationVertex(state.currentId & 0x7FFFFFFF, state.currentInt ^ graph.getTransactionDestinationVertex(state.currentId));
        }

        @Override
        public void undo(UndoGraphEditState state, GraphWriteMethods graph) {
            // If the high bit of the current id has been set then the transaction source and destination vertices will have been swapped during the execute phase
            // to keep the source vertex <= the destination vertex. This means that the undo step must operate on the source vertex instead of the source vertex.
            if (state.currentId >= 0) {
                graph.setTransactionDestinationVertex(state.currentId, state.currentInt ^ graph.getTransactionDestinationVertex(state.currentId));
            } else {
                graph.setTransactionSourceVertex(state.currentId & 0x7FFFFFFF, state.currentInt ^ graph.getTransactionSourceVertex(state.currentId));
            }
        }

        @Override
        void addOperation(UndoGraphEditState state, int attribute, int id, int i, long l, int o) {
            int instruction = ordinal();
            instruction |= IdValueUpdater2.INSTANCE.store(state, id) << OPERATION_SHIFT;
            instruction |= IntValueUpdater2.INSTANCE.store(state, i) << (OPERATION_SHIFT + 2);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(UndoGraphEditState state, int instruction) {
            IdValueUpdater2.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IntValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 2));
        }

        @Override
        public void updateUndo(UndoGraphEditState state, int instruction) {
            IntValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 2));
            IdValueUpdater2.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    ADD_ATTRIBUTE("addAttributeOperation") {

        @Override
        public void execute(UndoGraphEditState state, GraphWriteMethods graph) {
            Object[] params = (Object[]) state.objectStack[state.currentObject];
            graph.addAttribute((GraphElementType) params[0], (String) params[1], (String) params[2], (String) params[3], params[4], (String) params[5]);
        }

        @Override
        public void undo(UndoGraphEditState state, GraphWriteMethods graph) {
            graph.removeAttribute(state.currentAttribute);
        }

        @Override
        void addOperation(UndoGraphEditState state, int attribute, int id, int i, long l, int o) {
            int instruction = ordinal();
            instruction |= AttributeValueUpdater3.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
            instruction |= ObjectValueUpdater2.INSTANCE.store(state, o) << (OPERATION_SHIFT + 3);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(UndoGraphEditState state, int instruction) {
            AttributeValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            ObjectValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
        }

        @Override
        public void updateUndo(UndoGraphEditState state, int instruction) {
            ObjectValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            AttributeValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    REMOVE_ATTRIBUTE("removeAttributeOperation") {

        @Override
        public void execute(UndoGraphEditState state, GraphWriteMethods graph) {
            graph.removeAttribute(state.currentAttribute);
        }

        @Override
        public void undo(UndoGraphEditState state, GraphWriteMethods graph) {
            Object[] params = (Object[]) state.objectStack[state.currentObject];
            graph.addAttribute((GraphElementType) params[0], (String) params[1], (String) params[2], (String) params[3], params[4], (String) params[5]);
        }

        @Override
        void addOperation(UndoGraphEditState state, int attribute, int id, int i, long l, int o) {
            int instruction = ordinal();
            instruction |= AttributeValueUpdater3.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
            instruction |= ObjectValueUpdater2.INSTANCE.store(state, o) << (OPERATION_SHIFT + 3);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(UndoGraphEditState state, int instruction) {
            AttributeValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            ObjectValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
        }

        @Override
        public void updateUndo(UndoGraphEditState state, int instruction) {
            ObjectValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            AttributeValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    UPDATE_ATTRIBUTE_NAME("updateAttributeNameOperation") {

        @Override
        public void execute(UndoGraphEditState state, GraphWriteMethods graph) {
            graph.updateAttributeName(state.currentAttribute, (String) state.objectStack[state.currentObject]);
        }

        @Override
        public void undo(UndoGraphEditState state, GraphWriteMethods graph) {
            graph.updateAttributeName(state.currentAttribute, (String) state.objectStack[state.currentInt]);
        }

        @Override
        void addOperation(UndoGraphEditState state, int attribute, int id, int i, long l, int o) {
            int instruction = ordinal();
            instruction |= AttributeValueUpdater3.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
            instruction |= IntValueUpdater2.INSTANCE.store(state, i) << (OPERATION_SHIFT + 3);
            instruction |= ObjectValueUpdater2.INSTANCE.store(state, o) << (OPERATION_SHIFT + 5);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(UndoGraphEditState state, int instruction) {
            AttributeValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IntValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
            ObjectValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 5));
        }

        @Override
        public void updateUndo(UndoGraphEditState state, int instruction) {
            ObjectValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 5));
            IntValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            AttributeValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    UPDATE_ATTRIBUTE_DESCRIPTION("updateAttributeDescriptionOperation") {

        @Override
        public void execute(UndoGraphEditState state, GraphWriteMethods graph) {
            graph.updateAttributeDescription(state.currentAttribute, (String) state.objectStack[state.currentObject]);
        }

        @Override
        public void undo(UndoGraphEditState state, GraphWriteMethods graph) {
            graph.updateAttributeDescription(state.currentAttribute, (String) state.objectStack[state.currentInt]);
        }

        @Override
        void addOperation(UndoGraphEditState state, int attribute, int id, int i, long l, int o) {
            int instruction = ordinal();
            instruction |= AttributeValueUpdater3.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
            instruction |= IntValueUpdater2.INSTANCE.store(state, i) << (OPERATION_SHIFT + 3);
            instruction |= ObjectValueUpdater2.INSTANCE.store(state, o) << (OPERATION_SHIFT + 5);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(UndoGraphEditState state, int instruction) {
            AttributeValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IntValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
            ObjectValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 5));
        }

        @Override
        public void updateUndo(UndoGraphEditState state, int instruction) {
            ObjectValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 5));
            IntValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            AttributeValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    UPDATE_ATTRIBUTE_DEFAULT_VALUE("updateAttributeDefaultValueOperation") {

        @Override
        public void execute(UndoGraphEditState state, GraphWriteMethods graph) {
            graph.updateAttributeDefaultValue(state.currentAttribute, state.objectStack[state.currentObject]);
        }

        @Override
        public void undo(UndoGraphEditState state, GraphWriteMethods graph) {
            graph.updateAttributeDefaultValue(state.currentAttribute, state.objectStack[state.currentInt]);
        }

        @Override
        void addOperation(UndoGraphEditState state, int attribute, int id, int i, long l, int o) {
            int instruction = ordinal();
            instruction |= AttributeValueUpdater3.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
            instruction |= IntValueUpdater2.INSTANCE.store(state, i) << (OPERATION_SHIFT + 3);
            instruction |= ObjectValueUpdater2.INSTANCE.store(state, o) << (OPERATION_SHIFT + 5);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(UndoGraphEditState state, int instruction) {
            AttributeValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IntValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
            ObjectValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 5));
        }

        @Override
        public void updateUndo(UndoGraphEditState state, int instruction) {
            ObjectValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 5));
            IntValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            AttributeValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    SET_BYTE_VALUE("setByteValueOperation") {

        @Override
        public void execute(UndoGraphEditState state, GraphWriteMethods graph) {
            if (VERBOSE) {
                System.out.println("setByteValueOperation.execute(" + state.currentAttribute + ", " + state.currentId + ", " + (byte) (graph.getByteValue(state.currentAttribute, state.currentId) ^ (byte) state.currentInt) + ")");
            }
            graph.setByteValue(state.currentAttribute, state.currentId, (byte) (graph.getByteValue(state.currentAttribute, state.currentId) ^ (byte) state.currentInt));
        }

        @Override
        public void undo(UndoGraphEditState state, GraphWriteMethods graph) {
            graph.setByteValue(state.currentAttribute, state.currentId, (byte) (graph.getByteValue(state.currentAttribute, state.currentId) ^ (byte) state.currentInt));
        }

        @Override
        void addOperation(UndoGraphEditState state, int attribute, int id, int i, long l, int o) {
            int instruction = ordinal();
            instruction |= AttributeValueUpdater3.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
            instruction |= IdValueUpdater2.INSTANCE.store(state, id) << (OPERATION_SHIFT + 3);
            instruction |= IntValueUpdater2.INSTANCE.store(state, i) << (OPERATION_SHIFT + 5);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(UndoGraphEditState state, int instruction) {
            AttributeValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IdValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
            IntValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 5));
        }

        @Override
        public void updateUndo(UndoGraphEditState state, int instruction) {
            IntValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 5));
            IdValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            AttributeValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    SET_SHORT_VALUE("setShortValueOperation") {

        @Override
        public void execute(UndoGraphEditState state, GraphWriteMethods graph) {
            if (VERBOSE) {
                System.out.println("setShortValueOperation.execute(" + state.currentAttribute + ", " + state.currentId + ", " + (short) (graph.getShortValue(state.currentAttribute, state.currentId) ^ (short) state.currentInt) + ")");
            }
            graph.setShortValue(state.currentAttribute, state.currentId, (short) (graph.getShortValue(state.currentAttribute, state.currentId) ^ (short) state.currentInt));
        }

        @Override
        public void undo(UndoGraphEditState state, GraphWriteMethods graph) {
            graph.setShortValue(state.currentAttribute, state.currentId, (short) (graph.getShortValue(state.currentAttribute, state.currentId) ^ (short) state.currentInt));
        }

        @Override
        void addOperation(UndoGraphEditState state, int attribute, int id, int i, long l, int o) {
            int instruction = ordinal();
            instruction |= AttributeValueUpdater3.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
            instruction |= IdValueUpdater2.INSTANCE.store(state, id) << (OPERATION_SHIFT + 3);
            instruction |= IntValueUpdater2.INSTANCE.store(state, i) << (OPERATION_SHIFT + 5);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(UndoGraphEditState state, int instruction) {
            AttributeValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IdValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
            IntValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 5));
        }

        @Override
        public void updateUndo(UndoGraphEditState state, int instruction) {
            IntValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 5));
            IdValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            AttributeValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    SET_INT_VALUE("setIntValueOperation") {

        @Override
        public void execute(UndoGraphEditState state, GraphWriteMethods graph) {
            if (VERBOSE) {
                System.out.println("setIntValueOperation.execute(" + state.currentAttribute + ", " + state.currentId + ", " + (graph.getIntValue(state.currentAttribute, state.currentId) + state.currentInt) + ")");
            }
            graph.setIntValue(state.currentAttribute, state.currentId, graph.getIntValue(state.currentAttribute, state.currentId) + state.currentInt);
        }

        @Override
        public void undo(UndoGraphEditState state, GraphWriteMethods graph) {
            if (VERBOSE) {
                System.out.println("undoIntValueOperation.execute(" + state.currentAttribute + ", " + state.currentId + ", " + (graph.getIntValue(state.currentAttribute, state.currentId) - state.currentInt) + ")");
            }
            graph.setIntValue(state.currentAttribute, state.currentId, graph.getIntValue(state.currentAttribute, state.currentId) - state.currentInt);
        }

        @Override
        void addOperation(UndoGraphEditState state, int attribute, int id, int i, long l, int o) {
            int instruction = ordinal();
            instruction |= AttributeValueUpdater3.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
            instruction |= IdValueUpdater2.INSTANCE.store(state, id) << (OPERATION_SHIFT + 3);
            instruction |= IntValueUpdater2.INSTANCE.store(state, i) << (OPERATION_SHIFT + 5);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(UndoGraphEditState state, int instruction) {
            AttributeValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IdValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
            IntValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 5));
        }

        @Override
        public void updateUndo(UndoGraphEditState state, int instruction) {
            IntValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 5));
            IdValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            AttributeValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }

    },
    SET_LONG_VALUE("setLongValueOperation") {

        @Override
        public void execute(UndoGraphEditState state, GraphWriteMethods graph) {
            if (VERBOSE) {
                System.out.println("setLongValueOperation.execute(" + state.currentAttribute + ", " + state.currentId + ", " + (graph.getLongValue(state.currentAttribute, state.currentId) ^ state.currentLong) + ")");
            }
            graph.setLongValue(state.currentAttribute, state.currentId, graph.getLongValue(state.currentAttribute, state.currentId) + state.currentLong);
        }

        @Override
        public void undo(UndoGraphEditState state, GraphWriteMethods graph) {
            graph.setLongValue(state.currentAttribute, state.currentId, graph.getLongValue(state.currentAttribute, state.currentId) - state.currentLong);
        }

        @Override
        void addOperation(UndoGraphEditState state, int attribute, int id, int i, long l, int o) {
            int instruction = ordinal();
            instruction |= AttributeValueUpdater3.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
            instruction |= IdValueUpdater2.INSTANCE.store(state, id) << (OPERATION_SHIFT + 3);
            instruction |= LongValueUpdater2.INSTANCE.store(state, l) << (OPERATION_SHIFT + 5);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(UndoGraphEditState state, int instruction) {
            AttributeValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IdValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
            LongValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 5));
        }

        @Override
        public void updateUndo(UndoGraphEditState state, int instruction) {
            LongValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 5));
            IdValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            AttributeValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    SET_FLOAT_VALUE("setFloatValueOperation") {

        @Override
        public void execute(UndoGraphEditState state, GraphWriteMethods graph) {
            if (VERBOSE) {
                System.out.println("setFloatValueOperation.execute(" + state.currentAttribute + ", " + state.currentId + ", " + Float.intBitsToFloat(Float.floatToRawIntBits(graph.getFloatValue(state.currentAttribute, state.currentId)) ^ state.currentFloat) + ")");
            }
            graph.setFloatValue(state.currentAttribute, state.currentId, Float.intBitsToFloat(Float.floatToRawIntBits(graph.getFloatValue(state.currentAttribute, state.currentId)) ^ state.currentFloat));
        }

        @Override
        public void undo(UndoGraphEditState state, GraphWriteMethods graph) {
            graph.setFloatValue(state.currentAttribute, state.currentId, Float.intBitsToFloat(Float.floatToRawIntBits(graph.getFloatValue(state.currentAttribute, state.currentId)) ^ state.currentFloat));
        }

        @Override
        void addOperation(UndoGraphEditState state, int attribute, int id, int i, long l, int o) {
            int instruction = ordinal();
            instruction |= AttributeValueUpdater3.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
            instruction |= IdValueUpdater3.INSTANCE.store(state, id) << (OPERATION_SHIFT + 3);
            instruction |= FloatValueUpdater2.INSTANCE.store(state, i) << (OPERATION_SHIFT + 6);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(UndoGraphEditState state, int instruction) {
            AttributeValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IdValueUpdater3.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
            FloatValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 6));
        }

        @Override
        public void updateUndo(UndoGraphEditState state, int instruction) {
            FloatValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 6));
            IdValueUpdater3.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            AttributeValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    SET_DOUBLE_VALUE("setDoubleValueOperation") {

        @Override
        public void execute(UndoGraphEditState state, GraphWriteMethods graph) {
            if (VERBOSE) {
                System.out.println("setDoubleValueOperation.execute(" + state.currentAttribute + ", " + state.currentId + ", " + Double.longBitsToDouble(Double.doubleToRawLongBits(graph.getDoubleValue(state.currentAttribute, state.currentId)) ^ state.currentDouble) + ")");
            }
            graph.setDoubleValue(state.currentAttribute, state.currentId, Double.longBitsToDouble(Double.doubleToRawLongBits(graph.getDoubleValue(state.currentAttribute, state.currentId)) ^ state.currentDouble));
        }

        @Override
        public void undo(UndoGraphEditState state, GraphWriteMethods graph) {
            graph.setDoubleValue(state.currentAttribute, state.currentId, Double.longBitsToDouble(Double.doubleToRawLongBits(graph.getDoubleValue(state.currentAttribute, state.currentId)) ^ state.currentDouble));
        }

        @Override
        void addOperation(UndoGraphEditState state, int attribute, int id, int i, long l, int o) {
            int instruction = ordinal();
            instruction |= AttributeValueUpdater3.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
            instruction |= IdValueUpdater2.INSTANCE.store(state, id) << (OPERATION_SHIFT + 3);
            instruction |= DoubleValueUpdater2.INSTANCE.store(state, l) << (OPERATION_SHIFT + 5);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(UndoGraphEditState state, int instruction) {
            AttributeValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IdValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
            DoubleValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 5));
        }

        @Override
        public void updateUndo(UndoGraphEditState state, int instruction) {
            DoubleValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 5));
            IdValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            AttributeValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    SET_BOOLEAN_VALUE_TRUE("setBooleanValueTrueOperation") {

        @Override
        public void execute(UndoGraphEditState state, GraphWriteMethods graph) {
            if (VERBOSE) {
                System.out.println("setBooleanValueTrueOperation.execute(" + state.currentAttribute + ", " + state.currentId + ", " + true + ")");
            }
            graph.setBooleanValue(state.currentAttribute, state.currentId, true);
        }

        @Override
        public void undo(UndoGraphEditState state, GraphWriteMethods graph) {
            graph.setBooleanValue(state.currentAttribute, state.currentId, false);
        }

        @Override
        void addOperation(UndoGraphEditState state, int attribute, int id, int i, long l, int o) {
            int instruction = ordinal();
            instruction |= AttributeValueUpdater3.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
            instruction |= IdValueUpdater3.INSTANCE.store(state, id) << (OPERATION_SHIFT + 3);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(UndoGraphEditState state, int instruction) {
            AttributeValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IdValueUpdater3.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
        }

        @Override
        public void updateUndo(UndoGraphEditState state, int instruction) {
            IdValueUpdater3.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            AttributeValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    SET_BOOLEAN_VALUE_FALSE("setBooleanValueFalseOperation") {

        @Override
        public void execute(UndoGraphEditState state, GraphWriteMethods graph) {
            if (VERBOSE) {
                System.out.println("setBooleanValueFalseOperation.execute(" + state.currentAttribute + ", " + state.currentId + ", " + false + ")");
            }
            graph.setBooleanValue(state.currentAttribute, state.currentId, false);
        }

        @Override
        public void undo(UndoGraphEditState state, GraphWriteMethods graph) {
            graph.setBooleanValue(state.currentAttribute, state.currentId, true);
        }

        @Override
        void addOperation(UndoGraphEditState state, int attribute, int id, int i, long l, int o) {
            int instruction = ordinal();
            instruction |= AttributeValueUpdater3.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
            instruction |= IdValueUpdater3.INSTANCE.store(state, id) << (OPERATION_SHIFT + 3);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(UndoGraphEditState state, int instruction) {
            AttributeValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IdValueUpdater3.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
        }

        @Override
        public void updateUndo(UndoGraphEditState state, int instruction) {
            IdValueUpdater3.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            AttributeValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    SET_CHAR_VALUE("setCharValueOperation") {

        @Override
        public void execute(UndoGraphEditState state, GraphWriteMethods graph) {
            if (VERBOSE) {
                System.out.println("setCharValueOperation.execute(" + state.currentAttribute + ", " + state.currentId + ", " + (char) (graph.getCharValue(state.currentAttribute, state.currentId) ^ state.currentInt) + ")");
            }
            graph.setCharValue(state.currentAttribute, state.currentId, (char) (graph.getCharValue(state.currentAttribute, state.currentId) ^ state.currentInt));
        }

        @Override
        public void undo(UndoGraphEditState state, GraphWriteMethods graph) {
            graph.setCharValue(state.currentAttribute, state.currentId, (char) (graph.getCharValue(state.currentAttribute, state.currentId) ^ state.currentInt));
        }

        @Override
        void addOperation(UndoGraphEditState state, int attribute, int id, int i, long l, int o) {
            int instruction = ordinal();
            instruction |= AttributeValueUpdater3.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
            instruction |= IdValueUpdater2.INSTANCE.store(state, id) << (OPERATION_SHIFT + 3);
            instruction |= IntValueUpdater2.INSTANCE.store(state, i) << (OPERATION_SHIFT + 5);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(UndoGraphEditState state, int instruction) {
            AttributeValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IdValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
            IntValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 5));
        }

        @Override
        public void updateUndo(UndoGraphEditState state, int instruction) {
            IntValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 5));
            IdValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            AttributeValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    SET_OBJECT_VALUE("setObjectValueOperation") {

        @Override
        public void execute(UndoGraphEditState state, GraphWriteMethods graph) {
            if (VERBOSE) {
                System.out.println("setObjectValueOperation.execute(" + state.currentAttribute + ", " + state.currentId + ", " + state.objectStack[state.currentObject] + ")");
            }
            graph.setObjectValue(state.currentAttribute, state.currentId, state.objectStack[state.currentObject]);
        }

        @Override
        public void undo(UndoGraphEditState state, GraphWriteMethods graph) {
            graph.setObjectValue(state.currentAttribute, state.currentId, state.objectStack[state.currentInt]);
        }

        @Override
        void addOperation(UndoGraphEditState state, int attribute, int id, int i, long l, int o) {
            int instruction = ordinal();
            instruction |= AttributeValueUpdater3.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
            instruction |= IdValueUpdater2.INSTANCE.store(state, id) << (OPERATION_SHIFT + 3);
            instruction |= IntValueUpdater2.INSTANCE.store(state, i) << (OPERATION_SHIFT + 5);
            instruction |= ObjectValueUpdater2.INSTANCE.store(state, o) << (OPERATION_SHIFT + 7);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(UndoGraphEditState state, int instruction) {
            AttributeValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IdValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
            IntValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 5));
            ObjectValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 7));
        }

        @Override
        public void updateUndo(UndoGraphEditState state, int instruction) {
            ObjectValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 7));
            IntValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 5));
            IdValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            AttributeValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    SET_OBJECT_VALUE_FROM_NULL("setObjectValueFromNullOperation") {

        @Override
        public void execute(UndoGraphEditState state, GraphWriteMethods graph) {
            if (VERBOSE) {
                System.out.println("setObjectValueFromNullOperation.execute(" + state.currentAttribute + ", " + state.currentId + ", " + state.objectStack[state.currentObject] + ")");
            }
            graph.setObjectValue(state.currentAttribute, state.currentId, state.objectStack[state.currentObject]);
        }

        @Override
        public void undo(UndoGraphEditState state, GraphWriteMethods graph) {
            graph.setObjectValue(state.currentAttribute, state.currentId, null);
        }

        @Override
        void addOperation(UndoGraphEditState state, int attribute, int id, int i, long l, int o) {
            int instruction = ordinal();
            instruction |= AttributeValueUpdater3.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
            instruction |= IdValueUpdater2.INSTANCE.store(state, id) << (OPERATION_SHIFT + 3);
            instruction |= ObjectValueUpdater4.INSTANCE.store(state, o) << (OPERATION_SHIFT + 5);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(UndoGraphEditState state, int instruction) {
            AttributeValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IdValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
            ObjectValueUpdater4.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 5));
        }

        @Override
        public void updateUndo(UndoGraphEditState state, int instruction) {
            ObjectValueUpdater4.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 5));
            IdValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            AttributeValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    SET_OBJECT_VALUE_TO_NULL("setObjectValueToNullOperation") {

        @Override
        public void execute(UndoGraphEditState state, GraphWriteMethods graph) {
            if (VERBOSE) {
                System.out.println("setObjectValueToNullOperation.execute(" + state.currentAttribute + ", " + state.currentId + ", null)");
            }
            graph.setObjectValue(state.currentAttribute, state.currentId, null);
        }

        @Override
        public void undo(UndoGraphEditState state, GraphWriteMethods graph) {
            graph.setObjectValue(state.currentAttribute, state.currentId, state.objectStack[state.currentObject]);
        }

        @Override
        void addOperation(UndoGraphEditState state, int attribute, int id, int i, long l, int o) {
            int instruction = ordinal();
            instruction |= AttributeValueUpdater3.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
            instruction |= IdValueUpdater2.INSTANCE.store(state, id) << (OPERATION_SHIFT + 3);
            instruction |= ObjectValueUpdater2.INSTANCE.store(state, o) << (OPERATION_SHIFT + 5);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(UndoGraphEditState state, int instruction) {
            AttributeValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IdValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
            ObjectValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 5));
        }

        @Override
        public void updateUndo(UndoGraphEditState state, int instruction) {
            ObjectValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 5));
            IdValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            AttributeValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    EXECUTE_CHILD("executeChildOperation") {

        @Override
        public void execute(UndoGraphEditState state, GraphWriteMethods graph) {
            if (VERBOSE) {
                System.out.println("executeChildOperation.execute(" + state.currentObject + ")");
            }
            ((GraphEdit) state.objectStack[state.currentObject]).execute(graph);
        }

        @Override
        public void undo(UndoGraphEditState state, GraphWriteMethods graph) {
            if (VERBOSE) {
                System.out.println("undoChildOperation.execute(" + state.currentObject + ")");
            }
            ((GraphEdit) state.objectStack[state.currentObject]).undo(graph);
        }

        @Override
        void addOperation(UndoGraphEditState state, int attribute, int id, int i, long l, int o) {
            int instruction = ordinal();
            instruction |= ObjectValueUpdater2.INSTANCE.store(state, o) << OPERATION_SHIFT;
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(UndoGraphEditState state, int instruction) {
            ObjectValueUpdater2.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
        }

        @Override
        public void updateUndo(UndoGraphEditState state, int instruction) {
            ObjectValueUpdater2.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    SET_ATTRIBUTE_INDEX_TYPE("setAttributeIndexTypeOperation") {

        @Override
        public void execute(UndoGraphEditState state, GraphWriteMethods graph) {
            if (VERBOSE) {
                System.out.println("setAttributeIndexTypeOperation.execute(" + state.currentObject + ")");
            }
            graph.setAttributeIndexType(state.currentAttribute, GraphIndexType.values()[graph.getAttributeIndexType(state.currentAttribute).ordinal() + state.currentObject]);
        }

        @Override
        public void undo(UndoGraphEditState state, GraphWriteMethods graph) {
            if (VERBOSE) {
                System.out.println("setAttributeIndexTypeOperation.undo(" + state.currentObject + ")");
            }
            graph.setAttributeIndexType(state.currentAttribute, GraphIndexType.values()[graph.getAttributeIndexType(state.currentAttribute).ordinal() - state.currentObject]);
        }

        @Override
        void addOperation(UndoGraphEditState state, int attribute, int id, int i, long l, int o) {
            int instruction = ordinal();
            instruction |= AttributeValueUpdater3.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
            instruction |= ObjectValueUpdater5.INSTANCE.store(state, o) << OPERATION_SHIFT + 3;
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(UndoGraphEditState state, int instruction) {
            AttributeValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            ObjectValueUpdater5.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT + 3);
        }

        @Override
        public void updateUndo(UndoGraphEditState state, int instruction) {
            AttributeValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
            ObjectValueUpdater5.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT + 3);
        }
    },
    EXECUTE_GRAPH_OPERATION("executeGraphOperation") {

        @Override
        public void execute(UndoGraphEditState state, GraphWriteMethods graph) {
            if (VERBOSE) {
                System.out.println("executeGraphOperation.execute(" + state.currentObject + ")");
            }
            ((GraphOperation) state.objectStack[state.currentObject]).execute(graph);
        }

        @Override
        public void undo(UndoGraphEditState state, GraphWriteMethods graph) {
            if (VERBOSE) {
                System.out.println("executeGraphOperation.undo(" + state.currentObject + ")");
            }
            ((GraphOperation) state.objectStack[state.currentObject]).undo(graph);
        }

        @Override
        void addOperation(UndoGraphEditState state, int attribute, int id, int i, long l, int o) {
            int instruction = ordinal();
            instruction |= ObjectValueUpdater2.INSTANCE.store(state, o) << OPERATION_SHIFT;
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(UndoGraphEditState state, int instruction) {
            ObjectValueUpdater2.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
        }

        @Override
        public void updateUndo(UndoGraphEditState state, int instruction) {
            ObjectValueUpdater2.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    };

    static final int OPERATION_SHIFT = 7;

    private static final boolean VERBOSE = false;

    private final String name;

    private UndoGraphEditOperation(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    void addOperation(UndoGraphEditState state, int attribute, int id, int i, long l, int o) {
        int instruction = ordinal();
        instruction |= AttributeValueUpdater2.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
        instruction |= IdValueUpdater2.INSTANCE.store(state, id) << (OPERATION_SHIFT + 2);
        instruction |= IntValueUpdater2.INSTANCE.store(state, i) << (OPERATION_SHIFT + 4);
        instruction |= LongValueUpdater2.INSTANCE.store(state, l) << (OPERATION_SHIFT + 6);
        instruction |= ObjectValueUpdater2.INSTANCE.store(state, o) << (OPERATION_SHIFT + 8);
        state.addInstruction((short) instruction);
    }

    public void updateExecute(UndoGraphEditState state, int instruction) {
        AttributeValueUpdater2.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
        IdValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 2));
        IntValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 4));
        LongValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 6));
        ObjectValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 8));
    }

    public void updateUndo(UndoGraphEditState state, int instruction) {
        ObjectValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 8));
        LongValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 6));
        IntValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 4));
        IdValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 2));
        AttributeValueUpdater2.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
    }

    public abstract void execute(UndoGraphEditState state, GraphWriteMethods graph);

    public abstract void undo(UndoGraphEditState state, GraphWriteMethods graph);
}
