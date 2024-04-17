/*
 * Copyright 2010-2024 Australian Signals Directorate
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
public class ObjectValueUpdater5 implements ValueUpdater32 {

    public static final ObjectValueUpdater5 INSTANCE = new ObjectValueUpdater5();

    @Override
    public int store(final UndoGraphEditState state, final int o) {
        int delta = o ^ state.getCurrentObject();
        final int lastBits = delta & 7;
        delta >>>= 3;
        state.setCurrentObject(o);
        if (delta == 0) {
            return lastBits;
        } else if (delta >= 0 && delta <= 255) {
            state.addByte((byte) (delta + Byte.MIN_VALUE));
            return 8 | lastBits; //1<<3 = 8
        } else if (delta >= 0 && delta <= 65535) {
            state.addShort((short) (delta + Short.MIN_VALUE));
            return 16 | lastBits; //2<<3 = 16
        } else {
            state.addInt(delta);
            return 24 | lastBits; //3<<3 = 24
        }
    }

    @Override
    public void updateExecute(final UndoGraphEditState state, final int parameters) {
        OBJECT_GETTERS[(parameters >>> 3) & 3].getExecute(state);
        state.setCurrentObject(state.getCurrentObject() ^ (parameters & 7));
    }

    @Override
    public void updateUndo(final UndoGraphEditState state, final int parameters) {
        OBJECT_GETTERS[(parameters >>> 3) & 3].getUndo(state);
        state.setCurrentObject(state.getCurrentObject() ^ (parameters & 7));
    }

    private static final ValueGetter[] OBJECT_GETTERS = new ValueGetter[]{
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
                edit.setCurrentObject(edit.getCurrentObject() ^ (((int) edit.getByteStack()[edit.getBytePointer()] - Byte.MIN_VALUE) << 3));
                edit.setBytePointer(edit.getBytePointer() + 1);
            }

            @Override
            public void getUndo(final UndoGraphEditState edit) {
                edit.setBytePointer(edit.getBytePointer() - 1);
                edit.setCurrentObject(edit.getCurrentObject() ^ (((int) edit.getByteStack()[edit.getBytePointer()] - Byte.MIN_VALUE) << 3));
            }
        },
        new ValueGetter() {
            @Override
            public void getExecute(final UndoGraphEditState edit) {
                edit.setCurrentObject(edit.getCurrentObject() ^ (((int) edit.getShortStack()[edit.getShortPointer()] - Short.MIN_VALUE) << 3));
                edit.setShortPointer(edit.getShortPointer() + 1);
            }

            @Override
            public void getUndo(final UndoGraphEditState edit) {
                edit.setShortPointer(edit.getShortPointer() - 1);
                edit.setCurrentObject(edit.getCurrentObject() ^ (((int) edit.getShortStack()[edit.getShortPointer()] - Short.MIN_VALUE) << 3));
            }
        },
        new ValueGetter() {
            @Override
            public void getExecute(final UndoGraphEditState edit) {
                edit.setCurrentObject(edit.getCurrentObject() ^ (edit.getIntStack()[edit.getIntPointer()] << 3));
                edit.setIntPointer(edit.getIntPointer() + 1);
            }

            @Override
            public void getUndo(final UndoGraphEditState edit) {
                edit.setIntPointer(edit.getIntPointer() - 1);
                edit.setCurrentObject(edit.getCurrentObject() ^ (edit.getIntStack()[edit.getIntPointer()] << 3));
            }
        }
    };
}
