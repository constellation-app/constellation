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

import au.gov.asd.tac.constellation.graph.GraphReadMethods;

/**
 * A GlobalMonitor monitors global changes to the graph and will update when
 * ever any change is made.
 *
 * @author sirius
 */
public class GlobalMonitor extends Monitor {

    public GlobalMonitor() {
    }

    public GlobalMonitor(final GraphReadMethods graph) {
        update(graph);
    }

    /**
     * Updates this StructureMonitor from a specified graph.
     *
     * @param graph the graph to update from.
     * @return the MonitorTransition representing the change of state that
     * occurred between now and when update was last called.
     */
    @Override
    public final MonitorTransition update(final GraphReadMethods graph) {
        if (graph == null) {
            reset();
        } else if (graph.getId().equals(currentGraphId)) {
            modificationCounter = readModificationCounter(graph);
            transition = MonitorTransition.UNDEFINED_TO_PRESENT;
            currentGraphId = graph.getId();
        } else if (transition == MonitorTransition.UNDEFINED) {
            modificationCounter = readModificationCounter(graph);
            transition = MonitorTransition.UNDEFINED_TO_PRESENT;
        } else {
            final long currentModificationCounter = modificationCounter;
            modificationCounter = readModificationCounter(graph);
            transition = currentModificationCounter == modificationCounter ? MonitorTransition.UNCHANGED : MonitorTransition.CHANGED;
        }
        return transition;
    }

    protected long readModificationCounter(final GraphReadMethods graph) {
        return graph.getGlobalModificationCounter();
    }

    @Override
    public String toString() {
        final StringBuilder out = new StringBuilder();
        out.append("GlobalMonitor[");
        out.append(",transition=").append(transition);
        out.append(",modificationCounter=").append(modificationCounter);
        out.append("]");
        return out.toString();
    }
}
