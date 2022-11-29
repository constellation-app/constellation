/*
 * Copyright 2010-2021 Australian Signals Directorate
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
public class MonitorTransitionFilter {

    private final int mask;

    public MonitorTransitionFilter(final MonitorTransition... transitions) {
        int updateMask = 0;
        for (MonitorTransition transition : transitions) {
            updateMask |= transition.getMask();
        }
        this.mask = updateMask;
    }

    public boolean matchesTransition(final MonitorTransition transition) {
        return (mask & transition.getMask()) != 0;
    }

    public boolean matchesTransition(final Monitor monitor) {
        return (mask & monitor.getTransition().getMask()) != 0;
    }

    /**
     * Returns true if any of the specified monitors match this filter.
     *
     * @param monitors the monitors to test.
     * @return true if any of the specified monitors match this filter.
     */
    public boolean matchesTransitions(final AttributeValueMonitor... monitors) {
        for (Monitor monitor : monitors) {
            if (matchesTransition(monitor)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("MonitorTransitionFilter[");
        String delimiter = "";
        for (MonitorTransition transition : MonitorTransition.values()) {
            if ((mask & transition.getMask()) != 0) {
                out.append(delimiter);
                delimiter = ",";
                out.append(transition);
            }
        }
        out.append("]");
        return out.toString();
    }
}
