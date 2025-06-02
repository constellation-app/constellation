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
public class IntValueUpdater2 implements ValueUpdater32 {

    public static final IntValueUpdater2 INSTANCE = new IntValueUpdater2();

    @Override
    public int store(final UndoGraphEditState state, final int i) {
        if (i != state.getCurrentInt()) {
            final int intDelta = i - state.getCurrentInt();
            state.setCurrentInt(i);
            if (intDelta >= Byte.MIN_VALUE && intDelta <= Byte.MAX_VALUE) {
                state.addByte((byte) intDelta);
                return 1;
            } else if (intDelta >= Short.MIN_VALUE && intDelta <= Short.MAX_VALUE) {
                state.addShort((short) intDelta);
                return 2;
            } else {
                state.addInt(intDelta);
                return 3;
            }
        }
        return 0;
    }

    @Override
    public void updateExecute(final UndoGraphEditState state, final int parameters) {
        INT_GETTERS[parameters & 3].getExecute(state);
    }

    @Override
    public void updateUndo(final UndoGraphEditState state, final int parameters) {
        INT_GETTERS[parameters & 3].getUndo(state);
    }

    private static final ValueGetter[] INT_GETTERS = new ValueGetter[]{
        new ValueGetter() {
            @Override
            public void getExecute(final UndoGraphEditState edit) {
                // Default case when the position of the value getter is equal to 0
                // It has been intentionally left blank
            }

            @Override
            public void getUndo(final UndoGraphEditState edit) {
                // Default case when the position of the value getter is equal to 0
                // It has been intentionally left blank
            }
        },
        new ValueGetter() {
            @Override
            public void getExecute(final UndoGraphEditState edit) {
                edit.setCurrentInt(edit.getCurrentInt() + edit.getByteStack()[edit.getBytePointer()]);
                edit.setBytePointer(edit.getBytePointer() + 1);
            }

            @Override
            public void getUndo(final UndoGraphEditState edit) {
                edit.setBytePointer(edit.getBytePointer() - 1);
                edit.setCurrentInt(edit.getCurrentInt() - edit.getByteStack()[edit.getBytePointer()]);
            }
        },
        new ValueGetter() {
            @Override
            public void getExecute(final UndoGraphEditState edit) {
                edit.setCurrentInt(edit.getCurrentInt() + edit.getShortStack()[edit.getShortPointer()]);
                edit.setShortPointer(edit.getShortPointer() + 1);
            }

            @Override
            public void getUndo(final UndoGraphEditState edit) {
                edit.setShortPointer(edit.getShortPointer() - 1);
                edit.setCurrentInt(edit.getCurrentInt() - edit.getShortStack()[edit.getShortPointer()]);
            }
        },
        new ValueGetter() {
            @Override
            public void getExecute(final UndoGraphEditState edit) {
                edit.setCurrentInt(edit.getCurrentInt() + edit.getIntStack()[edit.getIntPointer()]);
                edit.setIntPointer(edit.getIntPointer() + 1);
            }

            @Override
            public void getUndo(final UndoGraphEditState edit) {
                edit.setIntPointer(edit.getIntPointer() - 1);
                edit.setCurrentInt(edit.getCurrentInt() - edit.getIntStack()[edit.getIntPointer()]);
            }
        }
    };
}
