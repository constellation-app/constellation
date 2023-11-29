/*
 * Copyright 2010-2023 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.find.quicksearch;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 * Helper class that is useful for getting the currently active
 * <code>Graph</code>.
 *
 * @see Graph
 *
 * @author betelgeuse
 */
public class GraphRetriever implements LookupListener {

    private GraphNode graphNode = null;
    private final Lookup.Result<GraphNode> lookup;

    /**
     * Construct a new FindServices.
     */
    public GraphRetriever() {
        lookup = Utilities.actionsGlobalContext().lookupResult(GraphNode.class);
        lookup.addLookupListener(this);

        resultChanged(null);
    }

    /**
     * Determines and returns the currently active graph node.
     *
     * @return The currently active <code>Graph</code>.
     * @see Graph
     */
    public Graph getGraph() {
        if (graphNode != null) {
            return graphNode.getGraph();
        }
        return null;
    }

    @Override
    public void resultChanged(final LookupEvent lev) {
        final Node[] activatedNodes = TopComponent.getRegistry().getActivatedNodes();
        if (activatedNodes.length > 0 && activatedNodes[0] instanceof GraphNode) {
            graphNode = (GraphNode) activatedNodes[0];
        } else {
            graphNode = null;
        }
    }
}
