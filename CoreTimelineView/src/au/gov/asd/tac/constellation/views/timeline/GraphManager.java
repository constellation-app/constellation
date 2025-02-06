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
package au.gov.asd.tac.constellation.views.timeline;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 *
 * @author betelgeuse
 */
public class GraphManager implements LookupListener {

    private GraphNode graphNode;
    private final Lookup.Result<GraphNode> result;
    private String datetimeAttr = null;
    private boolean isElementSelected = false;

    public static GraphManager getDefault() {
        return GraphManagerSingletonHolder.INSTANCE;
    }

    private GraphManager() {
        // Attach listener that determines the active graphnode:
        result = Utilities.actionsGlobalContext().lookupResult(GraphNode.class);
        result.addLookupListener(this);
    }

    private static final class GraphManagerSingletonHolder {

        static final GraphManager INSTANCE = new GraphManager();
    }

    public void select(final int srcID, final int destID, final int transID, final boolean isCtrlDown) {
        final Set<Integer> vertices = new HashSet<>();
        final Set<Integer> transactions = new HashSet<>();

        vertices.add(srcID);
        vertices.add(destID);
        transactions.add(transID);

        // Execute the plugin:
        final TimelineSelectionPlugin tsp = new TimelineSelectionPlugin(vertices, transactions, !isCtrlDown, false);
        PluginExecution.withPlugin(tsp).executeLater(graphNode.getGraph());
    }

    public void selectAllInRange(final long lowerTimeExtent, final long upperTimeExtent,
            final boolean isCtrlDown, final boolean isDragSelection, final boolean selectedOnly) {
        if (datetimeAttr != null) {
            final Set<Integer> vertices = new HashSet<>();
            final Set<Integer> transactions = new HashSet<>();

            try (final ReadableGraph rg = graphNode.getGraph().getReadableGraph()) {
                final int datetimeAttrID = rg.getAttribute(GraphElementType.TRANSACTION, datetimeAttr);
                final int selectedAttr = VisualConcept.TransactionAttribute.SELECTED.get(rg);

                for (int pos = 0; pos < rg.getTransactionCount(); pos++) {
                    final int txID = rg.getTransaction(pos);
                    long datetime = rg.getLongValue(datetimeAttrID, txID);
                    if (lowerTimeExtent <= datetime && datetime <= upperTimeExtent && (!selectedOnly || rg.getBooleanValue(selectedAttr, txID))) {
                        transactions.add(txID);
                        final int vertA = rg.getTransactionDestinationVertex(txID);
                        final int vertB = rg.getTransactionSourceVertex(txID);

                        vertices.add(vertA);
                        vertices.add(vertB);
                    }
                }
            }

            final TimelineSelectionPlugin tsp = new TimelineSelectionPlugin(vertices, transactions, !isCtrlDown, isDragSelection);
            PluginExecution.withPlugin(tsp).executeLater(graphNode.getGraph());
        }
    }

    public void setElementSelected(final boolean isElementSelected) {
        this.isElementSelected = isElementSelected;
    }

    public boolean isElementSelected() {
        return isElementSelected;
    }

    public String getDatetimeAttr() {
        return datetimeAttr;
    }

    public void setDatetimeAttr(final String datetimeAttr) {
        this.datetimeAttr = datetimeAttr;
    }

    public List<String> getVertexAttributeNames() {
        final List<String> attrNames = new ArrayList<>();

        if (graphNode != null) {
            try (final ReadableGraph rg = graphNode.getGraph().getReadableGraph();) {
                for (int pos = 0; pos < rg.getAttributeCount(GraphElementType.VERTEX); pos++) {
                    final int attrID = rg.getAttribute(GraphElementType.VERTEX, pos);
                    attrNames.add(rg.getAttributeName(attrID));
                }
            }
        }

        return attrNames;
    }

    /**
     * Make the graph in the specified node the source for the manager.
     * <p>
     * If another graph is attached to the model, it is detached first.
     *
     * @param node The GraphNode containing the graph to be displayed.
     */
    private void setNode(final GraphNode node) {
        // Check if we are moving graphs:
        if (graphNode != null) {
            graphNode.getGraph();
        }

        // We are entering a new graph, so set up accordingly:
        // if node is null, there are no active graphs
        graphNode = node != null ? node : null;
    }

    @Override
    public void resultChanged(final LookupEvent lev) {
        final Node[] activatedNodes = TopComponent.getRegistry().getActivatedNodes();
        if (activatedNodes != null && activatedNodes.length == 1
                && activatedNodes[0] instanceof GraphNode gnode) {
            if (gnode != graphNode) {
                setNode(gnode);
            }
        } else {
            setNode(null);
        }
    }
}
