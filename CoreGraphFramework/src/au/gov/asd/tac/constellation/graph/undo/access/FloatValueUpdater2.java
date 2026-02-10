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
package au.gov.asd.tac.constellation.graph.undo.access;

import au.gov.asd.tac.constellation.graph.undo.UndoGraphEditState;

/**
 *
 * @author sirius
 */
public class FloatValueUpdater2 implements ValueUpdater32 {

    public static final FloatValueUpdater2 INSTANCE = new FloatValueUpdater2();

    @Override
    public int store(final UndoGraphEditState state, final int f) {
        if (f != state.getCurrentFloat()) {
            final int floatDelta = f - state.getCurrentFloat();
            state.setCurrentFloat(f);
            if (floatDelta >= Byte.MIN_VALUE && floatDelta <= Byte.MAX_VALUE) {
                state.addByte((byte) floatDelta);
                return 1;
            } else if (floatDelta >= Short.MIN_VALUE && floatDelta <= Short.MAX_VALUE) {
                state.addShort((short) floatDelta);
                return 2;
            } else {
                state.addInt(floatDelta);
                return 3;
            }
        }
        return 0;
    }

    @Override
    public void updateExecute(final UndoGraphEditState state, final int parameters) {
        FLOAT_GETTERS[parameters & 3].getExecute(state);
    }

    @Override
    public void updateUndo(final UndoGraphEditState state, final int parameters) {
        FLOAT_GETTERS[parameters & 3].getUndo(state);
    }

    private static final ValueGetter[] FLOAT_GETTERS = new ValueGetter[]{
        new ValueGetter() {
            @Override
            public void getExecute(final UndoGraphEditState state) {
                // Default case when the position of the value getter is equal to 0
                // It has been intentionally left blank
            }

            @Override
            public void getUndo(final UndoGraphEditState state) {
                // Default case when the position of the value getter is equal to 0
                // It has been intentionally left blank
            }
        },
        new ValueGetter() {
            @Override
            public void getExecute(final UndoGraphEditState edit) {
                edit.setCurrentFloat(edit.getCurrentFloat() + edit.getByteStack()[edit.getBytePointer()]);
                edit.setBytePointer(edit.getBytePointer() + 1);
            }

            @Override
            public void getUndo(final UndoGraphEditState edit) {
                edit.setBytePointer(edit.getBytePointer() - 1);
                edit.setCurrentFloat(edit.getCurrentFloat() - edit.getByteStack()[edit.getBytePointer()]);
            }
        },
        new ValueGetter() {
            @Override
            public void getExecute(final UndoGraphEditState edit) {
                edit.setCurrentFloat(edit.getCurrentFloat() + edit.getShortStack()[edit.getShortPointer()]);
                edit.setShortPointer(edit.getShortPointer() + 1);
            }

            @Override
            public void getUndo(final UndoGraphEditState edit) {
                edit.setShortPointer(edit.getShortPointer() - 1);
                edit.setCurrentFloat(edit.getCurrentFloat() - edit.getShortStack()[edit.getShortPointer()]);
            }
        },
        new ValueGetter() {
            @Override
            public void getExecute(final UndoGraphEditState edit) {
                edit.setCurrentFloat(edit.getCurrentFloat() + edit.getIntStack()[edit.getIntPointer()]);
                edit.setIntPointer(edit.getIntPointer() + 1);
            }

            @Override
            public void getUndo(final UndoGraphEditState edit) {
                edit.setIntPointer(edit.getIntPointer() - 1);
                edit.setCurrentFloat(edit.getCurrentFloat() - edit.getIntStack()[edit.getIntPointer()]);
            }
        }
    };
}
