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
public class AttributeValueUpdater2 implements ValueUpdater32 {

    public static final boolean VERBOSE = false;

    public static final AttributeValueUpdater2 INSTANCE = new AttributeValueUpdater2();

    @Override
    public int store(UndoGraphEditState state, int attribute) {
        if (attribute != state.currentAttribute) {
            int delta = attribute - state.currentAttribute;
            state.currentAttribute = attribute;
            if (delta == 1) {
                return 1;
            } else if (delta >= Byte.MIN_VALUE && delta <= Byte.MAX_VALUE) {
                state.addByte((byte) delta);
                return 2;
            } else {
                state.addInt(delta);
                return 3;
            }
        } else {
            return 0;
        }
    }

    @Override
    public void updateExecute(UndoGraphEditState state, int parameters) {
        ATTRIBUTE_GETTERS[parameters & 3].getExecute(state);
    }

    @Override
    public void updateUndo(UndoGraphEditState state, int parameters) {
        ATTRIBUTE_GETTERS[parameters & 3].getUndo(state);
    }

    private static final ValueGetter[] ATTRIBUTE_GETTERS = new ValueGetter[]{
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
                edit.currentAttribute++;
            }

            @Override
            public void getUndo(UndoGraphEditState edit) {
                edit.currentAttribute--;
            }
        },
        new ValueGetter() {
            @Override
            public void getExecute(UndoGraphEditState edit) {
                edit.currentAttribute += edit.byteStack[edit.bytePointer++];
            }

            @Override
            public void getUndo(UndoGraphEditState edit) {
                edit.currentAttribute -= edit.byteStack[--edit.bytePointer];
            }
        },
        new ValueGetter() {
            @Override
            public void getExecute(UndoGraphEditState edit) {
                edit.currentAttribute += edit.intStack[edit.intPointer++];
            }

            @Override
            public void getUndo(UndoGraphEditState edit) {
                edit.currentAttribute -= edit.intStack[--edit.intPointer];
            }
        }
    };
}
