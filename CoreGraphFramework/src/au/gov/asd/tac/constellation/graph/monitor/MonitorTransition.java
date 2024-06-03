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
package au.gov.asd.tac.constellation.graph.monitor;

/**
 *
 * @author sirius
 */
public enum MonitorTransition {

    /**
     * The initial transition state that occurs before a monitor is updated the
     * first time or when the monitor is updated with a null graph.
     */
    UNDEFINED(MonitorState.UNDEFINED, MonitorState.UNDEFINED),
    /**
     * The transition that occurs when a monitor in an undefined state is
     * updated with a graph in which the monitored entity is missing.
     */
    UNDEFINED_TO_MISSING(MonitorState.UNDEFINED, MonitorState.MISSING),
    /**
     * The transition that occurs when a monitor in an undefined state is
     * updated with a graph in which the monitored entity is present.
     */
    UNDEFINED_TO_PRESENT(MonitorState.UNDEFINED, MonitorState.PRESENT),
    CHANGED(MonitorState.PRESENT, MonitorState.PRESENT),
    ADDED(MonitorState.MISSING, MonitorState.PRESENT),
    REMOVED_AND_ADDED(MonitorState.PRESENT, MonitorState.PRESENT),
    UNCHANGED(MonitorState.PRESENT, MonitorState.PRESENT),
    STILL_MISSING(MonitorState.MISSING, MonitorState.MISSING),
    REMOVED(MonitorState.PRESENT, MonitorState.MISSING);

    private MonitorTransition(final MonitorState previousState, final MonitorState currentState) {
        this.previousState = previousState;
        this.currentState = currentState;
        this.mask = 1 << ordinal();
    }

    private final MonitorState previousState;
    private final MonitorState currentState;
    private final int mask;

    public MonitorState getPreviousState() {
        return previousState;
    }

    public MonitorState getCurrentState() {
        return currentState;
    }

    public int getMask() {
        return mask;
    }
}
