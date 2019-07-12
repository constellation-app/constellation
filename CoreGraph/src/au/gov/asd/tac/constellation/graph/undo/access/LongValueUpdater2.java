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
public class LongValueUpdater2 implements ValueUpdater64 {

    public static final LongValueUpdater2 INSTANCE = new LongValueUpdater2();

    @Override
    public int store(UndoGraphEditState state, long l) {
        if (l != state.currentLong) {
            long delta = l - state.currentLong;
            state.currentLong = l;
            if (delta >= Short.MIN_VALUE && delta <= Short.MAX_VALUE) {
                state.addShort((short) delta);
                return 1;
            } else if (delta >= Integer.MIN_VALUE && delta <= Integer.MAX_VALUE) {
                state.addInt((int) delta);
                return 2;
            } else {
                state.addLong(delta);
                return 3;
            }
        }
        return 0;
    }

    @Override
    public void updateExecute(UndoGraphEditState state, int parameters) {
        LONG_GETTERS[parameters & 3].getExecute(state);
    }

    @Override
    public void updateUndo(UndoGraphEditState state, int parameters) {
        LONG_GETTERS[parameters & 3].getUndo(state);
    }

    public static final ValueGetter[] LONG_GETTERS = new ValueGetter[]{
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
            public void getExecute(UndoGraphEditState state) {
                state.currentLong += state.shortStack[state.shortPointer++];
            }

            @Override
            public void getUndo(UndoGraphEditState state) {
                state.currentLong -= state.shortStack[--state.shortPointer];
            }
        },
        new ValueGetter() {
            @Override
            public void getExecute(UndoGraphEditState state) {
                state.currentLong += state.intStack[state.intPointer++];
            }

            @Override
            public void getUndo(UndoGraphEditState state) {
                state.currentLong -= state.intStack[--state.intPointer];
            }
        },
        new ValueGetter() {
            @Override
            public void getExecute(UndoGraphEditState state) {
                state.currentLong += state.longStack[state.longPointer++];
            }

            @Override
            public void getUndo(UndoGraphEditState state) {
                state.currentLong -= state.longStack[--state.longPointer];
            }
        }
    };
}
