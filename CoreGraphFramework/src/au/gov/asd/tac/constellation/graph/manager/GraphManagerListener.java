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
package au.gov.asd.tac.constellation.graph.manager;

import au.gov.asd.tac.constellation.graph.Graph;

/**
 * A GraphManagerListener receives events when the set of currently opened
 * graphs changed, such when a graph is opened or closed. It will also be
 * alerted when the currently active graph (the graph that has focus) changes.
 *
 * @author sirius
 */
public interface GraphManagerListener {

    /**
     * Called by the {@link GraphManager} when a new graph is opened in the
     * application.
     *
     * @param graph the graph that has just been opened.
     */
    public void graphOpened(final Graph graph);

    /**
     * Called by the {@link GraphManager} when a previously open graph is
     * closed.
     *
     * @param graph the graph that has just been closed.
     */
    public void graphClosed(final Graph graph);

    /**
     * Called by the {@link GraphManager} when the currently active graph
     * changes. In general, the active graph is the graph that currently has
     * focus in the application.
     *
     * @param graph the new active graph.
     */
    public void newActiveGraph(final Graph graph);
}
