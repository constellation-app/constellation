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
package au.gov.asd.tac.constellation.graph.file.opener;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.file.GraphDataObject;
import org.openide.util.Lookup;

/**
 * A service interface to provide an opener for a Graph.
 *
 * @author algol
 */
public abstract class GraphOpener {

    /**
     * Open a graph from an existing file.
     *
     * @param gdo A GraphDataObject representing an existing graph file.
     */
    public abstract void openGraph(final GraphDataObject gdo);

    /**
     * Open a graph using an existing graph and a name.
     * <p>
     * This is typically used when a new graph has been created with no
     * associated on-disk file.
     *
     * @param graph The graph to open.
     * @param name The name of the graph. This must be able to be used as a file
     * name.
     */
    public abstract void openGraph(final Graph graph, final String name);

    /**
     * Open a graph using an existing graph and a name.
     * <p>
     * This is typically used when a new graph has been created with no
     * associated on-disk file.
     *
     * @param graph The graph to open.
     * @param name The name of the graph. This must be able to be used as a file
     * name.
     * @param doAfter this runnable will be executed on the EDT once the graph
     * has been opened.
     */
    public abstract void openGraph(final Graph graph, final String name, final Runnable doAfter);

    /**
     * Open a graph using an existing graph and a name.
     * <p>
     * This is typically used when a new graph has been created with no
     * associated on-disk file.
     *
     * @param graph The graph to open.
     * @param name The name of the graph. This must be able to be used as a file
     * name.
     * @param numbered If true, a monotonically increasing number will be
     * appended to the name.
     */
    public abstract void openGraph(final Graph graph, final String name, final boolean numbered);

    public static GraphOpener getDefault() {
        return Lookup.getDefault().lookup(GraphOpener.class);
    }
}
