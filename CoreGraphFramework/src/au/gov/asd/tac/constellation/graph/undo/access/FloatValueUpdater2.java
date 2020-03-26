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
package au.gov.asd.tac.constellation.graph.undo.access;

import au.gov.asd.tac.constellation.graph.undo.UndoGraphEditState;

/**
 *
 * @author sirius
 */
public class FloatValueUpdater2 implements ValueUpdater32 {

    public static final FloatValueUpdater2 INSTANCE = new FloatValueUpdater2();

    @Override
    public int store(UndoGraphEditState state, int f) {
        if (f != state.currentFloat) {
            int floatDelta = f - state.currentFloat;
            state.currentFloat = f;
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
    public void updateExecute(UndoGraphEditState state, int parameters) {
        FLOAT_GETTERS[parameters & 3].getExecute(state);
    }

    @Override
    public void updateUndo(UndoGraphEditState state, int parameters) {
        FLOAT_GETTERS[parameters & 3].getUndo(state);
    }

    private static final ValueGetter[] FLOAT_GETTERS = new ValueGetter[]{
        new ValueGetter() {
            @Override
            public void getExecute(UndoGraphEditState state) {
            }

            @Override
            public void getUndo(UndoGraphEditState state) {
            }
        },
        new ValueGetter() {
            @Override
            public void getExecute(UndoGraphEditState edit) {
                edit.currentFloat += edit.getByteStack()[edit.getBytePointer()];
                edit.setBytePointer(edit.getBytePointer() + 1);
            }

            @Override
            public void getUndo(UndoGraphEditState edit) {
                edit.setBytePointer(edit.getBytePointer() - 1);
                edit.currentFloat -= edit.getByteStack()[edit.getBytePointer()];
            }
        },
        new ValueGetter() {
            @Override
            public void getExecute(UndoGraphEditState edit) {
                edit.currentFloat += edit.getShortStack()[edit.getShortPointer()];
                edit.setShortPointer(edit.getShortPointer() + 1);
            }

            @Override
            public void getUndo(UndoGraphEditState edit) {
                edit.setShortPointer(edit.getShortPointer() - 1);
                edit.currentFloat -= edit.getShortStack()[edit.getShortPointer()];
            }
        },
        new ValueGetter() {
            @Override
            public void getExecute(UndoGraphEditState edit) {
                edit.currentFloat += edit.getIntStack()[edit.getIntPointer()];
                edit.setIntPointer(edit.getIntPointer() + 1);
            }

            @Override
            public void getUndo(UndoGraphEditState edit) {
                edit.setIntPointer(edit.getIntPointer() - 1);
                edit.currentFloat -= edit.getIntStack()[edit.getIntPointer()];
            }
        }
    };
}
