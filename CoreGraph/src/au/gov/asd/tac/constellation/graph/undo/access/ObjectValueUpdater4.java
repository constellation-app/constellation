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
public class ObjectValueUpdater4 implements ValueUpdater32 {

    public static final ObjectValueUpdater4 INSTANCE = new ObjectValueUpdater4();

    @Override
    public int store(UndoGraphEditState state, int o) {
        int delta = o ^ state.currentObject;
        int lastBits = delta & 3;
        delta >>>= 2;
        state.currentObject = o;
        if (delta == 0) {
            return lastBits;
        } else if (delta >= 0 && delta <= 255) {
            state.addByte((byte) (delta + Byte.MIN_VALUE));
            return (1 << 2) | lastBits;
        } else if (delta >= 0 && delta <= 65535) {
            state.addShort((short) (delta + Short.MIN_VALUE));
            return (2 << 2) | lastBits;
        } else {
            state.addInt(delta);
            return (3 << 2) | lastBits;
        }
    }

    @Override
    public void updateExecute(UndoGraphEditState state, int parameters) {
        OBJECT_GETTERS[(parameters >>> 2) & 3].getExecute(state);
        state.currentObject ^= parameters & 3;
    }

    @Override
    public void updateUndo(UndoGraphEditState state, int parameters) {
        OBJECT_GETTERS[(parameters >>> 2) & 3].getUndo(state);
        state.currentObject ^= parameters & 3;
    }

    public static final ValueGetter[] OBJECT_GETTERS = new ValueGetter[]{
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
                edit.currentObject ^= ((int) edit.byteStack[edit.bytePointer++] - Byte.MIN_VALUE) << 2;
            }

            @Override
            public void getUndo(UndoGraphEditState edit) {
                edit.currentObject ^= ((int) edit.byteStack[--edit.bytePointer] - Byte.MIN_VALUE) << 2;
            }
        },
        new ValueGetter() {
            @Override
            public void getExecute(UndoGraphEditState edit) {
                edit.currentObject ^= ((int) edit.shortStack[edit.shortPointer++] - Short.MIN_VALUE) << 2;
            }

            @Override
            public void getUndo(UndoGraphEditState edit) {
                edit.currentObject ^= ((int) edit.shortStack[--edit.shortPointer] - Short.MIN_VALUE) << 2;
            }
        },
        new ValueGetter() {
            @Override
            public void getExecute(UndoGraphEditState edit) {
                edit.currentObject ^= (edit.intStack[edit.intPointer++]) << 2;
            }

            @Override
            public void getUndo(UndoGraphEditState edit) {
                edit.currentObject ^= (edit.intStack[--edit.intPointer]) << 2;
            }
        }
    };
}
