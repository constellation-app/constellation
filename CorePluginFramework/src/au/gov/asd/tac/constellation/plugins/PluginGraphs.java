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
package au.gov.asd.tac.constellation.plugins;

import au.gov.asd.tac.constellation.graph.Graph;
import java.util.Map;

/**
 * Implementations of the PluginGraphs interface provide access to the
 * collection of graphs that are currently available in Constellation.
 *
 * @author sirius
 */
public interface PluginGraphs {

    /**
     * Returns the active graph for this plugin.
     *
     * @return the active graph for this plugin.
     */
    public Graph getGraph();

    /**
     * Returns all currently open graphs. The graphs are keyed by their names.
     *
     * @return all currently open graphs.
     */
    public Map<String, Graph> getAllGraphs();

    /**
     * determines whether a particular graph exists or not, based on the graph
     * ID
     *
     * @param id graph unique identifier
     * @return boolean
     */
    public boolean graphExists(final String id);

    /**
     * Creates and opens a new graph with the specified name.
     *
     * @param name The name to give the new graph
     * @return the newly created graph.
     */
    public Graph createNewGraph(final String name);

    /**
     * Causes the current thread to start/stop sending update events. This call
     * is re-entrant meaning that multiple calls to setSilent(true) must be
     * followed by the same number of calls to setSilent(false) in order to
     * allow update events to start being sent again.
     *
     * @param silent specifies whether the graphs go into silent mode.
     */
    public void setSilent(final boolean silent);

    /**
     * Suspend execution until all concurrently running plugins have either also
     * waited for a gate at least as high as this gate or have finished
     * execution.
     *
     * @param gate specifies which gate to wait at.
     *
     * @throws java.lang.InterruptedException if the thread is interrupted while
     * waiting.
     */
    public void waitAtGate(final int gate) throws InterruptedException;
}
