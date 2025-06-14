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
public class DoubleValueUpdater2 implements ValueUpdater64 {

    public static final DoubleValueUpdater2 INSTANCE = new DoubleValueUpdater2();

    @Override
    public int store(final UndoGraphEditState state, final long d) {
        if (d != state.getCurrentDouble()) {
            final long delta = d - state.getCurrentDouble();
            state.setCurrentDouble(d);
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
    public void updateExecute(final UndoGraphEditState state, final int parameters) {
        DOUBLE_GETTERS[parameters & 3].getExecute(state);
    }

    @Override
    public void updateUndo(final UndoGraphEditState state, final int parameters) {
        DOUBLE_GETTERS[parameters & 3].getUndo(state);
    }

    private static final ValueGetter[] DOUBLE_GETTERS = new ValueGetter[]{
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
            public void getExecute(final UndoGraphEditState state) {
                state.setCurrentDouble(state.getCurrentDouble() + state.getShortStack()[state.getShortPointer()]);
                state.setShortPointer(state.getShortPointer() + 1);
            }

            @Override
            public void getUndo(final UndoGraphEditState state) {
                state.setShortPointer(state.getShortPointer() - 1);
                state.setCurrentDouble(state.getCurrentDouble() - state.getShortStack()[state.getShortPointer()]);
            }
        },
        new ValueGetter() {
            @Override
            public void getExecute(final UndoGraphEditState state) {
                state.setCurrentDouble(state.getCurrentDouble() + state.getIntStack()[state.getIntPointer()]);
                state.setIntPointer(state.getIntPointer() + 1);
            }

            @Override
            public void getUndo(final UndoGraphEditState state) {
                state.setIntPointer(state.getIntPointer() - 1);
                state.setCurrentDouble(state.getCurrentDouble() - state.getIntStack()[state.getIntPointer()]);
            }
        },
        new ValueGetter() {
            @Override
            public void getExecute(final UndoGraphEditState state) {
                state.setCurrentDouble(state.getCurrentDouble() + state.getLongStack()[state.getLongPointer()]);
                state.setLongPointer(state.getLongPointer() + 1);
            }

            @Override
            public void getUndo(final UndoGraphEditState state) {
                state.setLongPointer(state.getLongPointer() - 1);
                state.setCurrentDouble(state.getCurrentDouble() - state.getLongStack()[state.getLongPointer()]);
            }
        }
    };
}
