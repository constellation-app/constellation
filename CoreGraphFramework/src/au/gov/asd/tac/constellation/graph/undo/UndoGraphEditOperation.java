/*
 * Copyright 2010-2021 Australian Signals Directorate
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sirius
 */
public enum UndoGraphEditOperation {

    SET_PRIMARY_KEY("setPrimaryKeyOperation") {

        @Override
        public void execute(final UndoGraphEditState state, final GraphWriteMethods graph) {
            graph.setPrimaryKey(GraphElementType.values()[state.getCurrentAttribute()], (int[]) state.getObjectStack()[state.getCurrentObject()]);
        }

        @Override
        public void undo(final UndoGraphEditState state, final GraphWriteMethods graph) {
            graph.setPrimaryKey(GraphElementType.values()[state.getCurrentAttribute()], (int[]) state.getObjectStack()[state.getCurrentId()]);
        }

        @Override
        void addOperation(final UndoGraphEditState state, final int attribute, final int id, final int i, final long l, final int o) {
            int instruction = ordinal();
            instruction |= AttributeValueUpdater2.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
            instruction |= IdValueUpdater2.INSTANCE.store(state, id) << (OPERATION_SHIFT + 2);
            instruction |= ObjectValueUpdater2.INSTANCE.store(state, o) << (OPERATION_SHIFT + 4);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(final UndoGraphEditState state, final int instruction) {
            AttributeValueUpdater2.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IdValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 2));
            ObjectValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 4));
        }

        @Override
        public void updateUndo(final UndoGraphEditState state, final int instruction) {
            ObjectValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 4));
            IdValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 2));
            AttributeValueUpdater2.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    ADD_VERTEX("addVertexOperation") {

        @Override
        public void execute(final UndoGraphEditState state, final GraphWriteMethods graph) {
            graph.addVertex();
        }

        @Override
        public void undo(final UndoGraphEditState state, final GraphWriteMethods graph) {
            graph.removeVertex(state.getCurrentId());
        }

        @Override
        void addOperation(final UndoGraphEditState state, final int attribute, final int id, final int i, final long l, final int o) {
            int instruction = ordinal();
            instruction |= IdValueUpdater3.INSTANCE.store(state, id) << OPERATION_SHIFT;
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(final UndoGraphEditState state, final int instruction) {
            IdValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
        }

        @Override
        public void updateUndo(final UndoGraphEditState state, final int instruction) {
            IdValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    REMOVE_VERTEX("removeVertexOperation") {

        @Override
        public void execute(final UndoGraphEditState state, final GraphWriteMethods graph) {
            graph.removeVertex(state.getCurrentId());
        }

        @Override
        public void undo(final UndoGraphEditState state, final GraphWriteMethods graph) {
            graph.addVertex();
        }

        @Override
        void addOperation(final UndoGraphEditState state, final int attribute, final int id, final int i, final long l, final int o) {
            int instruction = ordinal();
            instruction |= IdValueUpdater3.INSTANCE.store(state, id) << OPERATION_SHIFT;
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(final UndoGraphEditState state, final int instruction) {
            IdValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
        }

        @Override
        public void updateUndo(final UndoGraphEditState state, final int instruction) {
            IdValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    ADD_DIRECTED_TRANSACTION("addDirectedTransactionOperation") {

        @Override
        public void execute(final UndoGraphEditState state, final GraphWriteMethods graph) {
            graph.addTransaction(state.getCurrentObject(), state.getCurrentInt(), true);
        }

        @Override
        public void undo(final UndoGraphEditState state, final GraphWriteMethods graph) {
            graph.removeTransaction(state.getCurrentId());
        }

        @Override
        void addOperation(final UndoGraphEditState state, final int attribute, final int id, final int i, final long l, final int o) {
            int instruction = ordinal();
            instruction |= IdValueUpdater3.INSTANCE.store(state, id) << OPERATION_SHIFT;
            instruction |= IntValueUpdater2.INSTANCE.store(state, i) << (OPERATION_SHIFT + 3);
            instruction |= ObjectValueUpdater2.INSTANCE.store(state, o) << (OPERATION_SHIFT + 5);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(final UndoGraphEditState state, final int instruction) {
            IdValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IntValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
            ObjectValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 5));
        }

        @Override
        public void updateUndo(final UndoGraphEditState state, final int instruction) {
            ObjectValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 5));
            IntValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            IdValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    ADD_UNDIRECTED_TRANSACTION("addUndirectedTransactionOperation") {

        @Override
        public void execute(final UndoGraphEditState state, final GraphWriteMethods graph) {
            graph.addTransaction(state.getCurrentObject(), state.getCurrentInt(), false);
        }

        @Override
        public void undo(final UndoGraphEditState state, final GraphWriteMethods graph) {
            graph.removeTransaction(state.getCurrentId());
        }

        @Override
        void addOperation(final UndoGraphEditState state, final int attribute, final int id, final int i, final long l, final int o) {
            int instruction = ordinal();
            instruction |= IdValueUpdater3.INSTANCE.store(state, id) << OPERATION_SHIFT;
            instruction |= IntValueUpdater2.INSTANCE.store(state, i) << (OPERATION_SHIFT + 3);
            instruction |= ObjectValueUpdater2.INSTANCE.store(state, o) << (OPERATION_SHIFT + 5);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(final UndoGraphEditState state, final int instruction) {
            IdValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IntValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
            ObjectValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 5));
        }

        @Override
        public void updateUndo(final UndoGraphEditState state, final int instruction) {
            ObjectValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 5));
            IntValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            IdValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    REMOVE_DIRECTED_TRANSACTION("removeDirectedTransactionOperation") {

        @Override
        public void execute(final UndoGraphEditState state, final GraphWriteMethods graph) {
            graph.removeTransaction(state.getCurrentId());
        }

        @Override
        public void undo(final UndoGraphEditState state, final GraphWriteMethods graph) {
            graph.addTransaction(state.getCurrentObject(), state.getCurrentInt(), true);
        }

        @Override
        void addOperation(final UndoGraphEditState state, final int attribute, final int id, final int i, final long l, final int o) {
            int instruction = ordinal();
            instruction |= IdValueUpdater3.INSTANCE.store(state, id) << OPERATION_SHIFT;
            instruction |= IntValueUpdater2.INSTANCE.store(state, i) << (OPERATION_SHIFT + 3);
            instruction |= ObjectValueUpdater2.INSTANCE.store(state, o) << (OPERATION_SHIFT + 5);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(final UndoGraphEditState state, final int instruction) {
            IdValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IntValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
            ObjectValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 5));
        }

        @Override
        public void updateUndo(final UndoGraphEditState state, final int instruction) {
            ObjectValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 5));
            IntValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            IdValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    REMOVE_UNDIRECTED_TRANSACTION("removeUndirectedTransactionOperation") {

        @Override
        public void execute(final UndoGraphEditState state, final GraphWriteMethods graph) {
            graph.removeTransaction(state.getCurrentId());
        }

        @Override
        public void undo(final UndoGraphEditState state, final GraphWriteMethods graph) {
            graph.addTransaction(state.getCurrentObject(), state.getCurrentInt(), false);
        }

        @Override
        void addOperation(final UndoGraphEditState state, final int attribute, final int id, final int i, final long l, final int o) {
            int instruction = ordinal();
            instruction |= IdValueUpdater3.INSTANCE.store(state, id) << OPERATION_SHIFT;
            instruction |= IntValueUpdater2.INSTANCE.store(state, i) << (OPERATION_SHIFT + 3);
            instruction |= ObjectValueUpdater2.INSTANCE.store(state, o) << (OPERATION_SHIFT + 5);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(final UndoGraphEditState state, final int instruction) {
            IdValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IntValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
            ObjectValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 5));
        }

        @Override
        public void updateUndo(final UndoGraphEditState state, final int instruction) {
            ObjectValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 5));
            IntValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            IdValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    SET_TRANSACTION_SOURCE_VERTEX("setTransactionSourceVertex") {

        @Override
        public void execute(final UndoGraphEditState state, final GraphWriteMethods graph) {
            graph.setTransactionSourceVertex(state.getCurrentId() & 0x7FFFFFFF, state.getCurrentInt() ^ graph.getTransactionSourceVertex(state.getCurrentId()));
        }

        @Override
        public void undo(final UndoGraphEditState state, final GraphWriteMethods graph) {
            // If the high bit of the current id has been set then the transaction source and destination vertices will have been swapped during the execute phase
            // to keep the source vertex <= the destination vertex. This means that the undo step must operate on the destination vertex instead of the source vertex.
            if (state.getCurrentId() >= 0) {
                graph.setTransactionSourceVertex(state.getCurrentId(), state.getCurrentInt() ^ graph.getTransactionSourceVertex(state.getCurrentId()));
            } else {
                graph.setTransactionDestinationVertex(state.getCurrentId() & 0x7FFFFFFF, state.getCurrentInt() ^ graph.getTransactionDestinationVertex(state.getCurrentId()));
            }
        }

        @Override
        void addOperation(final UndoGraphEditState state, final int attribute, final int id, final int i, final long l, final int o) {
            int instruction = ordinal();
            instruction |= IdValueUpdater2.INSTANCE.store(state, id) << OPERATION_SHIFT;
            instruction |= IntValueUpdater2.INSTANCE.store(state, i) << (OPERATION_SHIFT + 2);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(final UndoGraphEditState state, final int instruction) {
            IdValueUpdater2.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IntValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 2));
        }

        @Override
        public void updateUndo(final UndoGraphEditState state, final int instruction) {
            IntValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 2));
            IdValueUpdater2.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    SET_TRANSACTION_DESTINATION_VERTEX("setTransactionDestinationVertex") {

        @Override
        public void execute(final UndoGraphEditState state, final GraphWriteMethods graph) {
            graph.setTransactionDestinationVertex(state.getCurrentId() & 0x7FFFFFFF, state.getCurrentInt() ^ graph.getTransactionDestinationVertex(state.getCurrentId()));
        }

        @Override
        public void undo(final UndoGraphEditState state, final GraphWriteMethods graph) {
            // If the high bit of the current id has been set then the transaction source and destination vertices will have been swapped during the execute phase
            // to keep the source vertex <= the destination vertex. This means that the undo step must operate on the source vertex instead of the source vertex.
            if (state.getCurrentId() >= 0) {
                graph.setTransactionDestinationVertex(state.getCurrentId(), state.getCurrentInt() ^ graph.getTransactionDestinationVertex(state.getCurrentId()));
            } else {
                graph.setTransactionSourceVertex(state.getCurrentId() & 0x7FFFFFFF, state.getCurrentInt() ^ graph.getTransactionSourceVertex(state.getCurrentId()));
            }
        }

        @Override
        void addOperation(final UndoGraphEditState state, final int attribute, final int id, final int i, final long l, final int o) {
            int instruction = ordinal();
            instruction |= IdValueUpdater2.INSTANCE.store(state, id) << OPERATION_SHIFT;
            instruction |= IntValueUpdater2.INSTANCE.store(state, i) << (OPERATION_SHIFT + 2);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(final UndoGraphEditState state, final int instruction) {
            IdValueUpdater2.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IntValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 2));
        }

        @Override
        public void updateUndo(final UndoGraphEditState state, final int instruction) {
            IntValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 2));
            IdValueUpdater2.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    ADD_ATTRIBUTE("addAttributeOperation") {

        @Override
        public void execute(final UndoGraphEditState state, final GraphWriteMethods graph) {
            Object[] params = (Object[]) state.getObjectStack()[state.getCurrentObject()];
            graph.addAttribute((GraphElementType) params[0], (String) params[1], (String) params[2], (String) params[3], params[4], (String) params[5]);
        }

        @Override
        public void undo(final UndoGraphEditState state, final GraphWriteMethods graph) {
            graph.removeAttribute(state.getCurrentAttribute());
        }

        @Override
        void addOperation(final UndoGraphEditState state, final int attribute, final int id, final int i, final long l, final int o) {
            int instruction = ordinal();
            instruction |= AttributeValueUpdater3.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
            instruction |= ObjectValueUpdater2.INSTANCE.store(state, o) << (OPERATION_SHIFT + 3);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(final UndoGraphEditState state, final int instruction) {
            AttributeValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            ObjectValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
        }

        @Override
        public void updateUndo(final UndoGraphEditState state, final int instruction) {
            ObjectValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            AttributeValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    REMOVE_ATTRIBUTE("removeAttributeOperation") {

        @Override
        public void execute(final UndoGraphEditState state, final GraphWriteMethods graph) {
            graph.removeAttribute(state.getCurrentAttribute());
        }

        @Override
        public void undo(final UndoGraphEditState state, final GraphWriteMethods graph) {
            Object[] params = (Object[]) state.getObjectStack()[state.getCurrentObject()];
            graph.addAttribute((GraphElementType) params[0], (String) params[1], (String) params[2], (String) params[3], params[4], (String) params[5]);
        }

        @Override
        void addOperation(final UndoGraphEditState state, final int attribute, final int id, final int i, final long l, final int o) {
            int instruction = ordinal();
            instruction |= AttributeValueUpdater3.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
            instruction |= ObjectValueUpdater2.INSTANCE.store(state, o) << (OPERATION_SHIFT + 3);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(final UndoGraphEditState state, final int instruction) {
            AttributeValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            ObjectValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
        }

        @Override
        public void updateUndo(final UndoGraphEditState state, final int instruction) {
            ObjectValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            AttributeValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    UPDATE_ATTRIBUTE_NAME("updateAttributeNameOperation") {

        @Override
        public void execute(final UndoGraphEditState state, final GraphWriteMethods graph) {
            graph.updateAttributeName(state.getCurrentAttribute(), (String) state.getObjectStack()[state.getCurrentObject()]);
        }

        @Override
        public void undo(final UndoGraphEditState state, final GraphWriteMethods graph) {
            graph.updateAttributeName(state.getCurrentAttribute(), (String) state.getObjectStack()[state.getCurrentInt()]);
        }

        @Override
        void addOperation(final UndoGraphEditState state, final int attribute, final int id, final int i, final long l, final int o) {
            int instruction = ordinal();
            instruction |= AttributeValueUpdater3.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
            instruction |= IntValueUpdater2.INSTANCE.store(state, i) << (OPERATION_SHIFT + 3);
            instruction |= ObjectValueUpdater2.INSTANCE.store(state, o) << (OPERATION_SHIFT + 5);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(final UndoGraphEditState state, final int instruction) {
            AttributeValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IntValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
            ObjectValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 5));
        }

        @Override
        public void updateUndo(final UndoGraphEditState state, final int instruction) {
            ObjectValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 5));
            IntValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            AttributeValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    UPDATE_ATTRIBUTE_DESCRIPTION("updateAttributeDescriptionOperation") {

        @Override
        public void execute(final UndoGraphEditState state, final GraphWriteMethods graph) {
            graph.updateAttributeDescription(state.getCurrentAttribute(), (String) state.getObjectStack()[state.getCurrentObject()]);
        }

        @Override
        public void undo(final UndoGraphEditState state, final GraphWriteMethods graph) {
            graph.updateAttributeDescription(state.getCurrentAttribute(), (String) state.getObjectStack()[state.getCurrentInt()]);
        }

        @Override
        void addOperation(final UndoGraphEditState state, final int attribute, final int id, final int i, final long l, final int o) {
            int instruction = ordinal();
            instruction |= AttributeValueUpdater3.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
            instruction |= IntValueUpdater2.INSTANCE.store(state, i) << (OPERATION_SHIFT + 3);
            instruction |= ObjectValueUpdater2.INSTANCE.store(state, o) << (OPERATION_SHIFT + 5);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(final UndoGraphEditState state, final int instruction) {
            AttributeValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IntValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
            ObjectValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 5));
        }

        @Override
        public void updateUndo(final UndoGraphEditState state, final int instruction) {
            ObjectValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 5));
            IntValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            AttributeValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    UPDATE_ATTRIBUTE_DEFAULT_VALUE("updateAttributeDefaultValueOperation") {

        @Override
        public void execute(final UndoGraphEditState state, final GraphWriteMethods graph) {
            graph.updateAttributeDefaultValue(state.getCurrentAttribute(), state.getObjectStack()[state.getCurrentObject()]);
        }

        @Override
        public void undo(final UndoGraphEditState state, final GraphWriteMethods graph) {
            graph.updateAttributeDefaultValue(state.getCurrentAttribute(), state.getObjectStack()[state.getCurrentInt()]);
        }

        @Override
        void addOperation(final UndoGraphEditState state, final int attribute, final int id, final int i, final long l, final int o) {
            int instruction = ordinal();
            instruction |= AttributeValueUpdater3.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
            instruction |= IntValueUpdater2.INSTANCE.store(state, i) << (OPERATION_SHIFT + 3);
            instruction |= ObjectValueUpdater2.INSTANCE.store(state, o) << (OPERATION_SHIFT + 5);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(final UndoGraphEditState state, final int instruction) {
            AttributeValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IntValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
            ObjectValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 5));
        }

        @Override
        public void updateUndo(final UndoGraphEditState state, final int instruction) {
            ObjectValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 5));
            IntValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            AttributeValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    SET_BYTE_VALUE("setByteValueOperation") {

        @Override
        public void execute(final UndoGraphEditState state, final GraphWriteMethods graph) {
            if (VERBOSE) {
                LOGGER.log(Level.INFO, "setByteValueOperation.execute(" + state.getCurrentAttribute() + ", " + state.getCurrentId() + ", " + (byte) (graph.getByteValue(state.getCurrentAttribute(), state.getCurrentId()) ^ (byte) state.getCurrentInt()) + ")");
            }
            graph.setByteValue(state.getCurrentAttribute(), state.getCurrentId(), (byte) (graph.getByteValue(state.getCurrentAttribute(), state.getCurrentId()) ^ (byte) state.getCurrentInt()));
        }

        @Override
        public void undo(final UndoGraphEditState state, final GraphWriteMethods graph) {
            graph.setByteValue(state.getCurrentAttribute(), state.getCurrentId(), (byte) (graph.getByteValue(state.getCurrentAttribute(), state.getCurrentId()) ^ (byte) state.getCurrentInt()));
        }

        @Override
        void addOperation(final UndoGraphEditState state, final int attribute, final int id, final int i, final long l, final int o) {
            int instruction = ordinal();
            instruction |= AttributeValueUpdater3.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
            instruction |= IdValueUpdater2.INSTANCE.store(state, id) << (OPERATION_SHIFT + 3);
            instruction |= IntValueUpdater2.INSTANCE.store(state, i) << (OPERATION_SHIFT + 5);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(final UndoGraphEditState state, final int instruction) {
            AttributeValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IdValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
            IntValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 5));
        }

        @Override
        public void updateUndo(final UndoGraphEditState state, final int instruction) {
            IntValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 5));
            IdValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            AttributeValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    SET_SHORT_VALUE("setShortValueOperation") {

        @Override
        public void execute(final UndoGraphEditState state, final GraphWriteMethods graph) {
            if (VERBOSE) {
                LOGGER.log(Level.INFO, "setShortValueOperation.execute(" + state.getCurrentAttribute() + ", " + state.getCurrentId() + ", " + (short) (graph.getShortValue(state.getCurrentAttribute(), state.getCurrentId()) ^ (short) state.getCurrentInt()) + ")");
            }
            graph.setShortValue(state.getCurrentAttribute(), state.getCurrentId(), (short) (graph.getShortValue(state.getCurrentAttribute(), state.getCurrentId()) ^ (short) state.getCurrentInt()));
        }

        @Override
        public void undo(final UndoGraphEditState state, final GraphWriteMethods graph) {
            graph.setShortValue(state.getCurrentAttribute(), state.getCurrentId(), (short) (graph.getShortValue(state.getCurrentAttribute(), state.getCurrentId()) ^ (short) state.getCurrentInt()));
        }

        @Override
        void addOperation(final UndoGraphEditState state, final int attribute, final int id, final int i, final long l, final int o) {
            int instruction = ordinal();
            instruction |= AttributeValueUpdater3.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
            instruction |= IdValueUpdater2.INSTANCE.store(state, id) << (OPERATION_SHIFT + 3);
            instruction |= IntValueUpdater2.INSTANCE.store(state, i) << (OPERATION_SHIFT + 5);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(final UndoGraphEditState state, final int instruction) {
            AttributeValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IdValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
            IntValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 5));
        }

        @Override
        public void updateUndo(final UndoGraphEditState state, final int instruction) {
            IntValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 5));
            IdValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            AttributeValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    SET_INT_VALUE("setIntValueOperation") {

        @Override
        public void execute(final UndoGraphEditState state, final GraphWriteMethods graph) {
            if (VERBOSE) {
                LOGGER.log(Level.INFO, "setIntValueOperation.execute(" + state.getCurrentAttribute() + ", " + state.getCurrentId() + ", " + (graph.getIntValue(state.getCurrentAttribute(), state.getCurrentId()) + state.getCurrentInt()) + ")");
            }
            graph.setIntValue(state.getCurrentAttribute(), state.getCurrentId(), graph.getIntValue(state.getCurrentAttribute(), state.getCurrentId()) + state.getCurrentInt());
        }

        @Override
        public void undo(final UndoGraphEditState state, final GraphWriteMethods graph) {
            if (VERBOSE) {
                LOGGER.log(Level.INFO, "undoIntValueOperation.execute(" + state.getCurrentAttribute() + ", " + state.getCurrentId() + ", " + (graph.getIntValue(state.getCurrentAttribute(), state.getCurrentId()) - state.getCurrentInt()) + ")");
            }
            graph.setIntValue(state.getCurrentAttribute(), state.getCurrentId(), graph.getIntValue(state.getCurrentAttribute(), state.getCurrentId()) - state.getCurrentInt());
        }

        @Override
        void addOperation(final UndoGraphEditState state, final int attribute, final int id, final int i, final long l, final int o) {
            int instruction = ordinal();
            instruction |= AttributeValueUpdater3.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
            instruction |= IdValueUpdater2.INSTANCE.store(state, id) << (OPERATION_SHIFT + 3);
            instruction |= IntValueUpdater2.INSTANCE.store(state, i) << (OPERATION_SHIFT + 5);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(final UndoGraphEditState state, final int instruction) {
            AttributeValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IdValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
            IntValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 5));
        }

        @Override
        public void updateUndo(final UndoGraphEditState state, final int instruction) {
            IntValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 5));
            IdValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            AttributeValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }

    },
    SET_LONG_VALUE("setLongValueOperation") {

        @Override
        public void execute(final UndoGraphEditState state, final GraphWriteMethods graph) {
            if (VERBOSE) {
                LOGGER.log(Level.INFO, "setLongValueOperation.execute(" + state.getCurrentAttribute() + ", " + state.getCurrentId() + ", " + (graph.getLongValue(state.getCurrentAttribute(), state.getCurrentId()) ^ state.getCurrentLong()) + ")");
            }
            graph.setLongValue(state.getCurrentAttribute(), state.getCurrentId(), graph.getLongValue(state.getCurrentAttribute(), state.getCurrentId()) + state.getCurrentLong());
        }

        @Override
        public void undo(final UndoGraphEditState state, final GraphWriteMethods graph) {
            graph.setLongValue(state.getCurrentAttribute(), state.getCurrentId(), graph.getLongValue(state.getCurrentAttribute(), state.getCurrentId()) - state.getCurrentLong());
        }

        @Override
        void addOperation(final UndoGraphEditState state, final int attribute, final int id, final int i, final long l, final int o) {
            int instruction = ordinal();
            instruction |= AttributeValueUpdater3.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
            instruction |= IdValueUpdater2.INSTANCE.store(state, id) << (OPERATION_SHIFT + 3);
            instruction |= LongValueUpdater2.INSTANCE.store(state, l) << (OPERATION_SHIFT + 5);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(final UndoGraphEditState state, final int instruction) {
            AttributeValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IdValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
            LongValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 5));
        }

        @Override
        public void updateUndo(final UndoGraphEditState state, final int instruction) {
            LongValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 5));
            IdValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            AttributeValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    SET_FLOAT_VALUE("setFloatValueOperation") {

        @Override
        public void execute(final UndoGraphEditState state, final GraphWriteMethods graph) {
            if (VERBOSE) {
                LOGGER.log(Level.INFO, "setFloatValueOperation.execute(" + state.getCurrentAttribute() + ", " + state.getCurrentId() + ", " + Float.intBitsToFloat(Float.floatToRawIntBits(graph.getFloatValue(state.getCurrentAttribute(), state.getCurrentId())) ^ state.getCurrentFloat()) + ")");
            }
            graph.setFloatValue(state.getCurrentAttribute(), state.getCurrentId(), Float.intBitsToFloat(Float.floatToRawIntBits(graph.getFloatValue(state.getCurrentAttribute(), state.getCurrentId())) ^ state.getCurrentFloat()));
        }

        @Override
        public void undo(final UndoGraphEditState state, final GraphWriteMethods graph) {
            graph.setFloatValue(state.getCurrentAttribute(), state.getCurrentId(), Float.intBitsToFloat(Float.floatToRawIntBits(graph.getFloatValue(state.getCurrentAttribute(), state.getCurrentId())) ^ state.getCurrentFloat()));
        }

        @Override
        void addOperation(final UndoGraphEditState state, final int attribute, final int id, final int i, final long l, final int o) {
            int instruction = ordinal();
            instruction |= AttributeValueUpdater3.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
            instruction |= IdValueUpdater3.INSTANCE.store(state, id) << (OPERATION_SHIFT + 3);
            instruction |= FloatValueUpdater2.INSTANCE.store(state, i) << (OPERATION_SHIFT + 6);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(final UndoGraphEditState state, final int instruction) {
            AttributeValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IdValueUpdater3.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
            FloatValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 6));
        }

        @Override
        public void updateUndo(final UndoGraphEditState state, final int instruction) {
            FloatValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 6));
            IdValueUpdater3.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            AttributeValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    SET_DOUBLE_VALUE("setDoubleValueOperation") {

        @Override
        public void execute(final UndoGraphEditState state, final GraphWriteMethods graph) {
            if (VERBOSE) {
                LOGGER.log(Level.INFO, "setDoubleValueOperation.execute(" + state.getCurrentAttribute() + ", " + state.getCurrentId() + ", " + Double.longBitsToDouble(Double.doubleToRawLongBits(graph.getDoubleValue(state.getCurrentAttribute(), state.getCurrentId())) ^ state.getCurrentDouble()) + ")");
            }
            graph.setDoubleValue(state.getCurrentAttribute(), state.getCurrentId(), Double.longBitsToDouble(Double.doubleToRawLongBits(graph.getDoubleValue(state.getCurrentAttribute(), state.getCurrentId())) ^ state.getCurrentDouble()));
        }

        @Override
        public void undo(final UndoGraphEditState state, final GraphWriteMethods graph) {
            graph.setDoubleValue(state.getCurrentAttribute(), state.getCurrentId(), Double.longBitsToDouble(Double.doubleToRawLongBits(graph.getDoubleValue(state.getCurrentAttribute(), state.getCurrentId())) ^ state.getCurrentDouble()));
        }

        @Override
        void addOperation(final UndoGraphEditState state, final int attribute, final int id, final int i, final long l, final int o) {
            int instruction = ordinal();
            instruction |= AttributeValueUpdater3.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
            instruction |= IdValueUpdater2.INSTANCE.store(state, id) << (OPERATION_SHIFT + 3);
            instruction |= DoubleValueUpdater2.INSTANCE.store(state, l) << (OPERATION_SHIFT + 5);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(final UndoGraphEditState state, final int instruction) {
            AttributeValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IdValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
            DoubleValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 5));
        }

        @Override
        public void updateUndo(final UndoGraphEditState state, final int instruction) {
            DoubleValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 5));
            IdValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            AttributeValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    SET_BOOLEAN_VALUE_TRUE("setBooleanValueTrueOperation") {

        @Override
        public void execute(final UndoGraphEditState state, final GraphWriteMethods graph) {
            if (VERBOSE) {
                LOGGER.log(Level.INFO, "setBooleanValueTrueOperation.execute(" + state.getCurrentAttribute() + ", " + state.getCurrentId() + ", " + true + ")");
            }
            graph.setBooleanValue(state.getCurrentAttribute(), state.getCurrentId(), true);
        }

        @Override
        public void undo(final UndoGraphEditState state, final GraphWriteMethods graph) {
            graph.setBooleanValue(state.getCurrentAttribute(), state.getCurrentId(), false);
        }

        @Override
        void addOperation(final UndoGraphEditState state, final int attribute, final int id, final int i, final long l, final int o) {
            int instruction = ordinal();
            instruction |= AttributeValueUpdater3.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
            instruction |= IdValueUpdater3.INSTANCE.store(state, id) << (OPERATION_SHIFT + 3);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(final UndoGraphEditState state, final int instruction) {
            AttributeValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IdValueUpdater3.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
        }

        @Override
        public void updateUndo(final UndoGraphEditState state, final int instruction) {
            IdValueUpdater3.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            AttributeValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    SET_BOOLEAN_VALUE_FALSE("setBooleanValueFalseOperation") {

        @Override
        public void execute(final UndoGraphEditState state, final GraphWriteMethods graph) {
            if (VERBOSE) {
                LOGGER.log(Level.INFO, "setBooleanValueFalseOperation.execute(" + state.getCurrentAttribute() + ", " + state.getCurrentId() + ", " + false + ")");
            }
            graph.setBooleanValue(state.getCurrentAttribute(), state.getCurrentId(), false);
        }

        @Override
        public void undo(final UndoGraphEditState state, final GraphWriteMethods graph) {
            graph.setBooleanValue(state.getCurrentAttribute(), state.getCurrentId(), true);
        }

        @Override
        void addOperation(final UndoGraphEditState state, final int attribute, final int id, final int i, final long l, final int o) {
            int instruction = ordinal();
            instruction |= AttributeValueUpdater3.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
            instruction |= IdValueUpdater3.INSTANCE.store(state, id) << (OPERATION_SHIFT + 3);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(final UndoGraphEditState state, final int instruction) {
            AttributeValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IdValueUpdater3.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
        }

        @Override
        public void updateUndo(final UndoGraphEditState state, final int instruction) {
            IdValueUpdater3.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            AttributeValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    SET_CHAR_VALUE("setCharValueOperation") {

        @Override
        public void execute(final UndoGraphEditState state, final GraphWriteMethods graph) {
            if (VERBOSE) {
                LOGGER.log(Level.INFO, "setCharValueOperation.execute(" + state.getCurrentAttribute() + ", " + state.getCurrentId() + ", " + (char) (graph.getCharValue(state.getCurrentAttribute(), state.getCurrentId()) ^ state.getCurrentInt()) + ")");
            }
            graph.setCharValue(state.getCurrentAttribute(), state.getCurrentId(), (char) (graph.getCharValue(state.getCurrentAttribute(), state.getCurrentId()) ^ state.getCurrentInt()));
        }

        @Override
        public void undo(final UndoGraphEditState state, final GraphWriteMethods graph) {
            graph.setCharValue(state.getCurrentAttribute(), state.getCurrentId(), (char) (graph.getCharValue(state.getCurrentAttribute(), state.getCurrentId()) ^ state.getCurrentInt()));
        }

        @Override
        void addOperation(final UndoGraphEditState state, final int attribute, final int id, final int i, final long l, final int o) {
            int instruction = ordinal();
            instruction |= AttributeValueUpdater3.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
            instruction |= IdValueUpdater2.INSTANCE.store(state, id) << (OPERATION_SHIFT + 3);
            instruction |= IntValueUpdater2.INSTANCE.store(state, i) << (OPERATION_SHIFT + 5);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(final UndoGraphEditState state, final int instruction) {
            AttributeValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IdValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
            IntValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 5));
        }

        @Override
        public void updateUndo(final UndoGraphEditState state, final int instruction) {
            IntValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 5));
            IdValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            AttributeValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    SET_OBJECT_VALUE("setObjectValueOperation") {

        @Override
        public void execute(final UndoGraphEditState state, final GraphWriteMethods graph) {
            if (VERBOSE) {
                LOGGER.log(Level.INFO, "setObjectValueOperation.execute(" + state.getCurrentAttribute() + ", " + state.getCurrentId() + ", " + state.getObjectStack()[state.getCurrentObject()] + ")");
            }
            graph.setObjectValue(state.getCurrentAttribute(), state.getCurrentId(), state.getObjectStack()[state.getCurrentObject()]);
        }

        @Override
        public void undo(final UndoGraphEditState state, final GraphWriteMethods graph) {
            graph.setObjectValue(state.getCurrentAttribute(), state.getCurrentId(), state.getObjectStack()[state.getCurrentInt()]);
        }

        @Override
        void addOperation(final UndoGraphEditState state, final int attribute, final int id, final int i, final long l, final int o) {
            int instruction = ordinal();
            instruction |= AttributeValueUpdater3.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
            instruction |= IdValueUpdater2.INSTANCE.store(state, id) << (OPERATION_SHIFT + 3);
            instruction |= IntValueUpdater2.INSTANCE.store(state, i) << (OPERATION_SHIFT + 5);
            instruction |= ObjectValueUpdater2.INSTANCE.store(state, o) << (OPERATION_SHIFT + 7);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(final UndoGraphEditState state, final int instruction) {
            AttributeValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IdValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
            IntValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 5));
            ObjectValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 7));
        }

        @Override
        public void updateUndo(final UndoGraphEditState state, final int instruction) {
            ObjectValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 7));
            IntValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 5));
            IdValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            AttributeValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    SET_OBJECT_VALUE_FROM_NULL("setObjectValueFromNullOperation") {

        @Override
        public void execute(final UndoGraphEditState state, final GraphWriteMethods graph) {
            if (VERBOSE) {
                LOGGER.log(Level.INFO, "setObjectValueFromNullOperation.execute(" + state.getCurrentAttribute() + ", " + state.getCurrentId() + ", " + state.getObjectStack()[state.getCurrentObject()] + ")");
            }
            graph.setObjectValue(state.getCurrentAttribute(), state.getCurrentId(), state.getObjectStack()[state.getCurrentObject()]);
        }

        @Override
        public void undo(final UndoGraphEditState state, final GraphWriteMethods graph) {
            graph.setObjectValue(state.getCurrentAttribute(), state.getCurrentId(), null);
        }

        @Override
        void addOperation(final UndoGraphEditState state, final int attribute, final int id, final int i, final long l, final int o) {
            int instruction = ordinal();
            instruction |= AttributeValueUpdater3.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
            instruction |= IdValueUpdater2.INSTANCE.store(state, id) << (OPERATION_SHIFT + 3);
            instruction |= ObjectValueUpdater4.INSTANCE.store(state, o) << (OPERATION_SHIFT + 5);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(final UndoGraphEditState state, final int instruction) {
            AttributeValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IdValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
            ObjectValueUpdater4.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 5));
        }

        @Override
        public void updateUndo(final UndoGraphEditState state, final int instruction) {
            ObjectValueUpdater4.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 5));
            IdValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            AttributeValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    SET_OBJECT_VALUE_TO_NULL("setObjectValueToNullOperation") {

        @Override
        public void execute(final UndoGraphEditState state, final GraphWriteMethods graph) {
            if (VERBOSE) {
                LOGGER.log(Level.INFO, "setObjectValueToNullOperation.execute(" + state.getCurrentAttribute() + ", " + state.getCurrentId() + ", null)");
            }
            graph.setObjectValue(state.getCurrentAttribute(), state.getCurrentId(), null);
        }

        @Override
        public void undo(final UndoGraphEditState state, final GraphWriteMethods graph) {
            graph.setObjectValue(state.getCurrentAttribute(), state.getCurrentId(), state.getObjectStack()[state.getCurrentObject()]);
        }

        @Override
        void addOperation(final UndoGraphEditState state, final int attribute, final int id, final int i, final long l, final int o) {
            int instruction = ordinal();
            instruction |= AttributeValueUpdater3.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
            instruction |= IdValueUpdater2.INSTANCE.store(state, id) << (OPERATION_SHIFT + 3);
            instruction |= ObjectValueUpdater2.INSTANCE.store(state, o) << (OPERATION_SHIFT + 5);
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(final UndoGraphEditState state, final int instruction) {
            AttributeValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            IdValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 3));
            ObjectValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 5));
        }

        @Override
        public void updateUndo(final UndoGraphEditState state, final int instruction) {
            ObjectValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 5));
            IdValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 3));
            AttributeValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    EXECUTE_CHILD("executeChildOperation") {

        @Override
        public void execute(final UndoGraphEditState state, final GraphWriteMethods graph) {
            if (VERBOSE) {
                LOGGER.log(Level.INFO, "executeChildOperation.execute(" + state.getCurrentObject() + ")");
            }
            ((GraphEdit) state.getObjectStack()[state.getCurrentObject()]).execute(graph);
        }

        @Override
        public void undo(final UndoGraphEditState state, final GraphWriteMethods graph) {
            if (VERBOSE) {
                LOGGER.log(Level.INFO, "undoChildOperation.execute(" + state.getCurrentObject() + ")");
            }
            ((GraphEdit) state.getObjectStack()[state.getCurrentObject()]).undo(graph);
        }

        @Override
        void addOperation(final UndoGraphEditState state, final int attribute, final int id, final int i, final long l, final int o) {
            int instruction = ordinal();
            instruction |= ObjectValueUpdater2.INSTANCE.store(state, o) << OPERATION_SHIFT;
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(final UndoGraphEditState state, final int instruction) {
            ObjectValueUpdater2.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
        }

        @Override
        public void updateUndo(final UndoGraphEditState state, final int instruction) {
            ObjectValueUpdater2.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    },
    SET_ATTRIBUTE_INDEX_TYPE("setAttributeIndexTypeOperation") {

        @Override
        public void execute(final UndoGraphEditState state, final GraphWriteMethods graph) {
            if (VERBOSE) {
                LOGGER.log(Level.INFO, "setAttributeIndexTypeOperation.execute(" + state.getCurrentObject() + ")");
            }
            graph.setAttributeIndexType(state.getCurrentAttribute(), GraphIndexType.values()[graph.getAttributeIndexType(state.getCurrentAttribute()).ordinal() + state.getCurrentObject()]);
        }

        @Override
        public void undo(final UndoGraphEditState state, final GraphWriteMethods graph) {
            if (VERBOSE) {
                LOGGER.log(Level.INFO, "setAttributeIndexTypeOperation.undo(" + state.getCurrentObject() + ")");
            }
            graph.setAttributeIndexType(state.getCurrentAttribute(), GraphIndexType.values()[graph.getAttributeIndexType(state.getCurrentAttribute()).ordinal() - state.getCurrentObject()]);
        }

        @Override
        void addOperation(final UndoGraphEditState state, final int attribute, final int id, final int i, final long l, final int o) {
            int instruction = ordinal();
            instruction |= AttributeValueUpdater3.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
            instruction |= ObjectValueUpdater5.INSTANCE.store(state, o) << OPERATION_SHIFT + 3;
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(final UndoGraphEditState state, final int instruction) {
            AttributeValueUpdater3.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
            ObjectValueUpdater5.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT + 3);
        }

        @Override
        public void updateUndo(final UndoGraphEditState state, final int instruction) {
            AttributeValueUpdater3.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
            ObjectValueUpdater5.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT + 3);
        }
    },
    EXECUTE_GRAPH_OPERATION("executeGraphOperation") {

        @Override
        public void execute(final UndoGraphEditState state, final GraphWriteMethods graph) {
            if (VERBOSE) {
                LOGGER.log(Level.INFO, "executeGraphOperation.execute(" + state.getCurrentObject() + ")");
            }
            ((GraphOperation) state.getObjectStack()[state.getCurrentObject()]).execute(graph);
        }

        @Override
        public void undo(final UndoGraphEditState state, final GraphWriteMethods graph) {
            if (VERBOSE) {
                LOGGER.log(Level.INFO, "executeGraphOperation.undo(" + state.getCurrentObject() + ")");
            }
            ((GraphOperation) state.getObjectStack()[state.getCurrentObject()]).undo(graph);
        }

        @Override
        void addOperation(final UndoGraphEditState state, final int attribute, final int id, final int i, final long l, final int o) {
            int instruction = ordinal();
            instruction |= ObjectValueUpdater2.INSTANCE.store(state, o) << OPERATION_SHIFT;
            state.addInstruction((short) instruction);
        }

        @Override
        public void updateExecute(final UndoGraphEditState state, final int instruction) {
            ObjectValueUpdater2.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
        }

        @Override
        public void updateUndo(final UndoGraphEditState state, final int instruction) {
            ObjectValueUpdater2.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
        }
    };

    private static final Logger LOGGER = Logger.getLogger(UndoGraphEditOperation.class.getName());

    static final int OPERATION_SHIFT = 7;

    private static final boolean VERBOSE = false;

    private final String name;

    private UndoGraphEditOperation(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    void addOperation(final UndoGraphEditState state, final int attribute, final int id, final int i, final long l, final int o) {
        int instruction = ordinal();
        instruction |= AttributeValueUpdater2.INSTANCE.store(state, attribute) << OPERATION_SHIFT;
        instruction |= IdValueUpdater2.INSTANCE.store(state, id) << (OPERATION_SHIFT + 2);
        instruction |= IntValueUpdater2.INSTANCE.store(state, i) << (OPERATION_SHIFT + 4);
        instruction |= LongValueUpdater2.INSTANCE.store(state, l) << (OPERATION_SHIFT + 6);
        instruction |= ObjectValueUpdater2.INSTANCE.store(state, o) << (OPERATION_SHIFT + 8);
        state.addInstruction((short) instruction);
    }

    public void updateExecute(final UndoGraphEditState state, final int instruction) {
        AttributeValueUpdater2.INSTANCE.updateExecute(state, instruction >>> OPERATION_SHIFT);
        IdValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 2));
        IntValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 4));
        LongValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 6));
        ObjectValueUpdater2.INSTANCE.updateExecute(state, instruction >>> (OPERATION_SHIFT + 8));
    }

    public void updateUndo(final UndoGraphEditState state, final int instruction) {
        ObjectValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 8));
        LongValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 6));
        IntValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 4));
        IdValueUpdater2.INSTANCE.updateUndo(state, instruction >>> (OPERATION_SHIFT + 2));
        AttributeValueUpdater2.INSTANCE.updateUndo(state, instruction >>> OPERATION_SHIFT);
    }

    public abstract void execute(final UndoGraphEditState state, final GraphWriteMethods graph);

    public abstract void undo(final UndoGraphEditState state, final GraphWriteMethods graph);
}
