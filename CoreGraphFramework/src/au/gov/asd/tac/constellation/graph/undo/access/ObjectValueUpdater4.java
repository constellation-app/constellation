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
public class ObjectValueUpdater4 implements ValueUpdater32 {

    public static final ObjectValueUpdater4 INSTANCE = new ObjectValueUpdater4();

    @Override
    public int store(final UndoGraphEditState state, final int o) {
        int delta = o ^ state.getCurrentObject();
        final int lastBits = delta & 3;
        delta >>>= 2;
        state.setCurrentObject(o);
        if (delta == 0) {
            return lastBits;
        } else if (delta >= 0 && delta <= 255) {
            state.addByte((byte) (delta + Byte.MIN_VALUE));
            return 4 | lastBits; // 1<<2 = 4
        } else if (delta >= 0 && delta <= 65535) {
            state.addShort((short) (delta + Short.MIN_VALUE));
            return 8 | lastBits; //2<<2 = 8
        } else {
            state.addInt(delta);
            return 12 | lastBits; //3<<2 = 12
        }
    }

    @Override
    public void updateExecute(final UndoGraphEditState state, final int parameters) {
        OBJECT_GETTERS[(parameters >>> 2) & 3].getExecute(state);
        state.setCurrentObject(state.getCurrentObject() ^ (parameters & 3));
    }

    @Override
    public void updateUndo(final UndoGraphEditState state, final int parameters) {
        OBJECT_GETTERS[(parameters >>> 2) & 3].getUndo(state);
        state.setCurrentObject(state.getCurrentObject() ^ (parameters & 3));
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
                edit.setCurrentObject(edit.getCurrentObject() ^ (((int) edit.getByteStack()[edit.getBytePointer()] - Byte.MIN_VALUE) << 2));
                edit.setBytePointer(edit.getBytePointer() + 1);
            }

            @Override
            public void getUndo(final UndoGraphEditState edit) {
                edit.setBytePointer(edit.getBytePointer() - 1);
                edit.setCurrentObject(edit.getCurrentObject() ^ (((int) edit.getByteStack()[edit.getBytePointer()] - Byte.MIN_VALUE) << 2));
            }
        },
        new ValueGetter() {
            @Override
            public void getExecute(final UndoGraphEditState edit) {
                edit.setCurrentObject(edit.getCurrentObject() ^ (((int) edit.getShortStack()[edit.getShortPointer()] - Short.MIN_VALUE) << 2));
                edit.setShortPointer(edit.getShortPointer() + 1);
            }

            @Override
            public void getUndo(final UndoGraphEditState edit) {
                edit.setShortPointer(edit.getShortPointer() - 1);
                edit.setCurrentObject(edit.getCurrentObject() ^ (((int) edit.getShortStack()[edit.getShortPointer()] - Short.MIN_VALUE) << 2));
            }
        },
        new ValueGetter() {
            @Override
            public void getExecute(final UndoGraphEditState edit) {
                edit.setCurrentObject(edit.getCurrentObject() ^ (edit.getIntStack()[edit.getIntPointer()] << 2));
                edit.setIntPointer(edit.getIntPointer() + 1);
            }

            @Override
            public void getUndo(final UndoGraphEditState edit) {
                edit.setIntPointer(edit.getIntPointer() - 1);
                edit.setCurrentObject(edit.getCurrentObject() ^ (edit.getIntStack()[edit.getIntPointer()] << 2));
            }
        }
    };
}
