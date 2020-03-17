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
public class IntValueUpdater2 implements ValueUpdater32 {

    public static final IntValueUpdater2 INSTANCE = new IntValueUpdater2();

    @Override
    public int store(UndoGraphEditState state, int i) {
        if (i != state.currentInt) {
            int intDelta = i - state.currentInt;
            state.currentInt = i;
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
    public void updateExecute(UndoGraphEditState state, int parameters) {
        INT_GETTERS[parameters & 3].getExecute(state);
    }

    @Override
    public void updateUndo(UndoGraphEditState state, int parameters) {
        INT_GETTERS[parameters & 3].getUndo(state);
    }

    public static final ValueGetter[] INT_GETTERS = new ValueGetter[]{
        new ValueGetter() {
            @Override
            public void getExecute(UndoGraphEditState edit) {
            }

            @Override
            public void getUndo(UndoGraphEditState edit) {
            }
        },
        new ValueGetter() {
            @Override
            public void getExecute(UndoGraphEditState edit) {
                edit.currentInt += edit.byteStack[edit.bytePointer++];
            }

            @Override
            public void getUndo(UndoGraphEditState edit) {
                edit.currentInt -= edit.byteStack[--edit.bytePointer];
            }
        },
        new ValueGetter() {
            @Override
            public void getExecute(UndoGraphEditState edit) {
                edit.currentInt += edit.shortStack[edit.shortPointer++];
            }

            @Override
            public void getUndo(UndoGraphEditState edit) {
                edit.currentInt -= edit.shortStack[--edit.shortPointer];
            }
        },
        new ValueGetter() {
            @Override
            public void getExecute(UndoGraphEditState edit) {
                edit.currentInt += edit.intStack[edit.intPointer++];
            }

            @Override
            public void getUndo(UndoGraphEditState edit) {
                edit.currentInt -= edit.intStack[--edit.intPointer];
            }
        }
    };
}
