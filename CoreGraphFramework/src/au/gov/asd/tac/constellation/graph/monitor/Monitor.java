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
package au.gov.asd.tac.constellation.graph.monitor;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;

/**
 *
 * @author sirius
 */
public abstract class Monitor {

    protected String currentGraphId = null;
    protected MonitorTransition transition = MonitorTransition.UNDEFINED;
    protected long modificationCounter = -1L;

    public MonitorState getState() {
        return transition.getCurrentState();
    }

    public MonitorTransition getTransition() {
        return transition;
    }

    public long getModificationCounter() {
        return modificationCounter;
    }

    public void reset() {
        transition = MonitorTransition.UNDEFINED;
        currentGraphId = null;
        modificationCounter = -1L;
    }

    public MonitorTransition reset(final GraphReadMethods graph) {
        reset();
        return update(graph);
    }

    /**
     * Updates this Monitor from a specified graph.
     *
     * @param graph the graph to update from.
     * @return
     */
    public abstract MonitorTransition update(final GraphReadMethods graph);

    public static void update(final GraphReadMethods graph, final Monitor... monitors) {
        for (final Monitor monitor : monitors) {
            monitor.update(graph);
        }
    }
}
