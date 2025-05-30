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
package au.gov.asd.tac.constellation.graph.utilities.statusline;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphIndexType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeEvent;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeListener;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import org.openide.awt.StatusLineElementProvider;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 * display element information on status line of the main graph page
 *
 * @author algol
 */
@ServiceProvider(service = StatusLineElementProvider.class)
@Messages({
    "CTL_vx=Nodes",
    "CTL_lx=Links",
    "CTL_ex=Edges",
    "CTL_tx=Transactions",})
public final class GraphStatusLine implements StatusLineElementProvider, LookupListener, GraphChangeListener {

    private final Lookup.Result<GraphNode> result;
    private Graph graph;
    private static final JPanel PANEL = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    private static final JButton VX_BUTTON = new JButton(UserInterfaceIconProvider.NODES.buildIcon(16));
    private static final JButton LX_BUTTON = new JButton(UserInterfaceIconProvider.LINKS.buildIcon(16));
    private static final JButton EX_BUTTON = new JButton(UserInterfaceIconProvider.EDGES.buildIcon(16));
    private static final JButton TX_BUTTON = new JButton(UserInterfaceIconProvider.TRANSACTIONS.buildIcon(16));

    public GraphStatusLine() {
        VX_BUTTON.setToolTipText(Bundle.CTL_vx());
        LX_BUTTON.setToolTipText(Bundle.CTL_lx());
        EX_BUTTON.setToolTipText(Bundle.CTL_ex());
        TX_BUTTON.setToolTipText(Bundle.CTL_tx());

        result = Utilities.actionsGlobalContext().lookupResult(GraphNode.class);
        result.addLookupListener(this);

        resultChanged(null);

        PANEL.add(new JSeparator(SwingConstants.VERTICAL));
        for (final JButton b : new JButton[]{
            VX_BUTTON, LX_BUTTON, EX_BUTTON, TX_BUTTON
        }) {
            b.setBorderPainted(false);
            b.setContentAreaFilled(false);
            final Insets insets = b.getMargin();
            insets.left = 0;
            b.setMargin(insets);
            PANEL.add(b);
        }
    }

    @Override
    public Component getStatusLineElement() {
        return PANEL;
    }

    @Override
    public void resultChanged(final LookupEvent ev) {
        if (graph != null) {
            graph.removeGraphChangeListener(this);
        }

        final Node[] activatedNodes = TopComponent.getRegistry().getActivatedNodes();
        if (activatedNodes.length > 0 && activatedNodes[0] instanceof GraphNode) {
            final GraphNode gnode = (GraphNode) activatedNodes[0];
            graph = gnode.getGraph();
            graph.addGraphChangeListener(this);
        } else {
            graph = null;
        }

        graphChanged(null);
    }

    @Override
    public void graphChanged(final GraphChangeEvent evt) {
        if (graph != null) {
            ReadableGraph rg = graph.getReadableGraph();
            try {
                final int ndAttr = rg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.SELECTED.getName());
                final int txAttr = rg.getAttribute(GraphElementType.TRANSACTION, VisualConcept.TransactionAttribute.SELECTED.getName());

                // count number of selected nodes
                int vCount = 0;
                if (ndAttr != Graph.NOT_FOUND) {
                    GraphIndexType vertexSelectedIndexType = rg.getAttributeIndexType(ndAttr);
                    if (vertexSelectedIndexType == GraphIndexType.NONE) {
                        for (int i = 0; i < rg.getVertexCount(); i++) {
                            boolean sel = rg.getBooleanValue(ndAttr, rg.getVertex(i));
                            if (sel) {
                                vCount++;
                            }
                        }
                    } else {
                        vCount = rg.getElementsWithAttributeValue(ndAttr, Boolean.TRUE).getCount();
                    }
                }

                // count number of selected transactions
                int tCount = 0;
                int eCount = 0;
                int lCount = 0;

                if (txAttr != Graph.NOT_FOUND) {
                    for (int lxPos = 0; lxPos < rg.getLinkCount(); lxPos++) {
                        boolean linkSelected = false;
                        final int lxId = rg.getLink(lxPos);
                        for (int exPos = 0; exPos < rg.getLinkEdgeCount(lxId); exPos++) {
                            boolean edgeSelected = false;
                            final int exId = rg.getLinkEdge(lxId, exPos);
                            for (int txPos = 0; txPos < rg.getEdgeTransactionCount(exId); txPos++) {
                                boolean sel = rg.getBooleanValue(txAttr, rg.getEdgeTransaction(exId, txPos));
                                edgeSelected |= sel;
                                linkSelected |= sel;
                                if (sel) {
                                    tCount++;
                                }
                            }
                            if (edgeSelected) {
                                eCount++;
                            }
                        }
                        if (linkSelected) {
                            lCount++;
                        }
                    }
                }

                if (vCount > 0) {
                    VX_BUTTON.setText(String.valueOf(vCount) + "/" + String.valueOf(rg.getVertexCount()));
                } else {
                    VX_BUTTON.setText(String.valueOf(rg.getVertexCount()));
                }

                if (tCount > 0) {
                    TX_BUTTON.setText(String.valueOf(tCount) + "/" + String.valueOf(rg.getTransactionCount()));
                } else {
                    TX_BUTTON.setText(String.valueOf(rg.getTransactionCount()));
                }

                if (eCount > 0) {
                    EX_BUTTON.setText(String.valueOf(eCount) + "/" + String.valueOf(rg.getEdgeCount()));
                } else {
                    EX_BUTTON.setText(String.valueOf(rg.getEdgeCount()));
                }

                if (lCount > 0) {
                    LX_BUTTON.setText(String.valueOf(lCount) + "/" + String.valueOf(rg.getLinkCount()));
                } else {
                    LX_BUTTON.setText(String.valueOf(rg.getLinkCount()));
                }

            } finally {
                rg.release();
            }

        } else {
            VX_BUTTON.setText("");
            LX_BUTTON.setText("");
            EX_BUTTON.setText("");
            TX_BUTTON.setText("");
        }
    }
}
