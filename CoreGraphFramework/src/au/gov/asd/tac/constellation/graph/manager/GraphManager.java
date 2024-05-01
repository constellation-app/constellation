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
import java.util.Map;
import org.openide.util.Lookup;

/**
 * A GraphManager keeps track of all the graphs currently open in the
 * application.
 *
 * @author sirius
 */
public abstract class GraphManager {

    /**
     * Returns the default GraphManager.
     *
     * @return the default GraphManager.
     */
    public static GraphManager getDefault() {
        return Lookup.getDefault().lookup(GraphManager.class);
    }

    /**
     * Returns the currently active graph. This is the graph that currently has
     * focus in the application. When a plugin is run, it generally runs on the
     * active graph.
     *
     * @return the currently active graph.
     */
    public abstract Graph getActiveGraph();

    /**
     * Adds a listener to this GraphManager.
     *
     * @param listener the new listener.
     * @see GraphManagerListener
     */
    public abstract void addGraphManagerListener(final GraphManagerListener listener);

    /**
     * Removes a listener from this GraphManager.
     *
     * @param listener the listener to remove.
     * @see GraphManagerListener
     */
    public abstract void removeGraphManagerListener(final GraphManagerListener listener);

    /**
     * Returns a map of all graphs currently open in the application, keyed by
     * their graph ids. This map will be a copy meaning that changes to this map
     * will not effect the graphs that are open.
     *
     * @return a map of all graphs currently open in the application, keyed by
     * their graph ids.
     */
    public abstract Map<String, Graph> getAllGraphs();
}
