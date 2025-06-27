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
public class AttributeValueUpdater3 implements ValueUpdater32 {

    public static final boolean VERBOSE = false;

    public static final AttributeValueUpdater3 INSTANCE = new AttributeValueUpdater3();

    @Override
    public int store(final UndoGraphEditState state, final int attribute) {
        int delta = attribute - state.getCurrentAttribute();
        state.setCurrentAttribute(attribute);
        switch (delta) {
            case -2 -> {
                return 0;
            }
            case -1 -> {
                return 1;
            }
            case 0 -> {
                return 2;
            }
            case 1 -> {
                return 3;
            }
            case 2 -> {
                return 4;
            }
            default -> {
                if (delta >= Byte.MIN_VALUE && delta <= Byte.MAX_VALUE) {
                    state.addByte((byte) delta);
                    return 5;
                } else if (delta >= Short.MIN_VALUE && delta <= Short.MAX_VALUE) {
                    state.addShort((short) delta);
                    return 6;
                } else {
                    state.addInt(delta);
                    return 7;
                }
            }
        }
    }

    @Override
    public void updateExecute(final UndoGraphEditState state, final int parameters) {
        ATTRIBUTE_GETTERS[parameters & 7].getExecute(state);
    }

    @Override
    public void updateUndo(final UndoGraphEditState state, final int parameters) {
        ATTRIBUTE_GETTERS[parameters & 7].getUndo(state);
    }

    private static final ValueGetter[] ATTRIBUTE_GETTERS = new ValueGetter[]{
        new ValueGetter() {
            @Override
            public void getExecute(final UndoGraphEditState edit) {
                edit.setCurrentAttribute(edit.getCurrentAttribute() - 2);
            }

            @Override
            public void getUndo(final UndoGraphEditState edit) {
                edit.setCurrentAttribute(edit.getCurrentAttribute() + 2);
            }
        },
        new ValueGetter() {
            @Override
            public void getExecute(final UndoGraphEditState edit) {
                edit.setCurrentAttribute(edit.getCurrentAttribute() - 1);
            }

            @Override
            public void getUndo(final UndoGraphEditState edit) {
                edit.setCurrentAttribute(edit.getCurrentAttribute() + 1);
            }
        },
        new ValueGetter() {
            @Override
            public void getExecute(final UndoGraphEditState edit) {
                // This method has been intentionally left blank
            }

            @Override
            public void getUndo(final UndoGraphEditState edit) {
                // This method has been intentionally left blank
            }
        },
        new ValueGetter() {
            @Override
            public void getExecute(final UndoGraphEditState edit) {
                edit.setCurrentAttribute(edit.getCurrentAttribute() + 1);
            }

            @Override
            public void getUndo(final UndoGraphEditState edit) {
                edit.setCurrentAttribute(edit.getCurrentAttribute() - 1);
            }
        },
        new ValueGetter() {
            @Override
            public void getExecute(final UndoGraphEditState edit) {
                edit.setCurrentAttribute(edit.getCurrentAttribute() + 2);
            }

            @Override
            public void getUndo(final UndoGraphEditState edit) {
                edit.setCurrentAttribute(edit.getCurrentAttribute() - 2);
            }
        },
        new ValueGetter() {
            @Override
            public void getExecute(final UndoGraphEditState edit) {
                edit.setCurrentAttribute(edit.getCurrentAttribute() + edit.getByteStack()[edit.getBytePointer()]);
                edit.setBytePointer(edit.getBytePointer() + 1);
            }

            @Override
            public void getUndo(final UndoGraphEditState edit) {
                edit.setBytePointer(edit.getBytePointer() - 1);
                edit.setCurrentAttribute(edit.getCurrentAttribute() - edit.getByteStack()[edit.getBytePointer()]);
            }
        },
        new ValueGetter() {
            @Override
            public void getExecute(final UndoGraphEditState edit) {
                edit.setCurrentAttribute(edit.getCurrentAttribute() + edit.getShortStack()[edit.getShortPointer()]);
                edit.setShortPointer(edit.getShortPointer() + 1);
            }

            @Override
            public void getUndo(final UndoGraphEditState edit) {
                edit.setShortPointer(edit.getShortPointer() - 1);
                edit.setCurrentAttribute(edit.getCurrentAttribute() - edit.getShortStack()[edit.getShortPointer()]);
            }
        },
        new ValueGetter() {
            @Override
            public void getExecute(final UndoGraphEditState edit) {
                edit.setCurrentAttribute(edit.getCurrentAttribute() + edit.getIntStack()[edit.getIntPointer()]);
                edit.setIntPointer(edit.getIntPointer() + 1);
            }

            @Override
            public void getUndo(final UndoGraphEditState edit) {
                edit.setIntPointer(edit.getIntPointer() - 1);
                edit.setCurrentAttribute(edit.getCurrentAttribute() - edit.getIntStack()[edit.getIntPointer()]);
            }
        }
    };
}
