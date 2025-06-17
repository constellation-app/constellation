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
package au.gov.asd.tac.constellation.graph.node.plugins;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.manager.GraphManagerListener;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 * An implementation of GraphManager that links the active graph to the active
 * GraphNode in Netbeans.
 *
 * @author sirius
 */
@ServiceProvider(service = GraphManager.class)
public final class FrameworkGraphManager extends GraphManager implements LookupListener, GraphManagerListener {

    private static final String LISTENERS_LOG_MESSAGE = "{0} a GraphManagerListener taking the count of listeners to {1} which are: {2}";
    private static final Logger LOGGER = Logger.getLogger(FrameworkGraphManager.class.getName());
    private final Lookup.Result<GraphNode> graphNodeResult;
    private final List<GraphManagerListener> listeners = new ArrayList<>();
    private Graph activeGraph;

    public FrameworkGraphManager() {
        graphNodeResult = Utilities.actionsGlobalContext().lookupResult(GraphNode.class);
        graphNodeResult.addLookupListener(this);
        activeGraph = getActiveGraph();

        GraphNode.addGraphManagerListener(this);
    }

    @Override
    public final Graph getActiveGraph() {
        final Node[] graphNodes = TopComponent.getRegistry().getActivatedNodes();
        if (graphNodes != null && graphNodes.length == 1 && graphNodes[0] instanceof GraphNode) {
            return ((GraphNode) graphNodes[0]).getGraph();
        } else {
            return null;
        }
    }

    @Override
    public final void addGraphManagerListener(final GraphManagerListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
        LOGGER.log(Level.FINE, LISTENERS_LOG_MESSAGE, new Object[]{"Added", listeners.size(), listeners});
    }

    @Override
    public final void removeGraphManagerListener(final GraphManagerListener listener) {
        listeners.remove(listener);
        LOGGER.log(Level.FINE, LISTENERS_LOG_MESSAGE, new Object[]{"Removed", listeners.size(), listeners});
    }

    @Override
    public final void resultChanged(final LookupEvent ev) {
        final Graph oldActiveGraph = activeGraph;
        activeGraph = getActiveGraph();
        if (activeGraph != oldActiveGraph) {
            for (final GraphManagerListener listener : new ArrayList<>(listeners)) {
                listener.newActiveGraph(activeGraph);
            }
        }
    }

    @Override
    public Map<String, Graph> getAllGraphs() {
        return GraphNode.getAllGraphs();
    }

    @Override
    public void graphOpened(final Graph graph) {
        for (final GraphManagerListener listener : listeners) {
            listener.graphOpened(graph);
        }
    }

    @Override
    public void graphClosed(final Graph graph) {
        for (final GraphManagerListener listener : listeners) {
            listener.graphClosed(graph);
        }
    }

    @Override
    public void newActiveGraph(final Graph graph) {
        // required for implementation of GraphManagerListener
    }
}
