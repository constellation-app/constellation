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
package au.gov.asd.tac.constellation.graph.visual.dragdrop;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.Image;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author algol
 */
@Messages("CTL_DragAction=Drag (and drop)")
public class DragAction extends AbstractAction {

    private static final Logger LOGGER = Logger.getLogger(DragAction.class.getName());

    private static final Icon DRAG_WORD_ICON = UserInterfaceIconProvider.DRAG_WORD.buildIcon(16);
    private static final Image DRAG_DROP_ICON = UserInterfaceIconProvider.DRAG_DROP.buildBufferedImage(16);
    private final GraphNode context;

    /**
     * Construct a new action.
     *
     * @param context Graph Node.
     */
    public DragAction(final GraphNode context) {
        this.context = context;
        putValue(Action.SMALL_ICON, DRAG_WORD_ICON);
        putValue(Action.SHORT_DESCRIPTION, Bundle.CTL_DragAction());
        putValue(Action.SELECTED_KEY, true);
    }

    @Override
    public void actionPerformed(final ActionEvent ev) {
        LOGGER.log(Level.INFO, "action performed");
    }

    private class DraggableButton extends JButton implements Transferable, DragGestureListener {

        private final TransferHandler t;
        private final DragSource source;

        public DraggableButton(final Action action) {
            super(action);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(final MouseEvent event) {
                    getTransferHandler().exportAsDrag(DraggableButton.this, event, TransferHandler.COPY);
                }
            });
            t = new TransferHandler("graph") {
                @Override
                public Transferable createTransferable(final JComponent c) {
                    return new StringSelection("graphSelection");
                }
            };

            setTransferHandler(t);
            source = new DragSource();
            source.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY, this);
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.stringFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(DataFlavor.stringFlavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            final Graph graph = context.getGraph();
            final List<Map<String, String>> vxList = new ArrayList<>();
            final List<Map<String, String>> txList = new ArrayList<>();
            final ReadableGraph rg = graph.getReadableGraph();
            try {
                // Get the selected vertices.
                final int vxSelectedId = rg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.SELECTED.getName());
                final int vxCount = rg.getVertexCount();
                for (int position = 0; position < vxCount; position++) {
                    final int vxId = rg.getVertex(position);

                    final boolean selected = rg.getBooleanValue(vxSelectedId, vxId);
                    if (selected) {
                        final Map<String, String> vxMap = new HashMap<>();
                        for (int apos = 0; apos < rg.getAttributeCount(GraphElementType.VERTEX); apos++) {
                            vxMap.put("vx_id_", Integer.toString(vxId));
                            final Attribute attr = new GraphAttribute(rg, rg.getAttribute(GraphElementType.VERTEX, apos));
                            vxMap.put(attr.getName(), rg.getStringValue(attr.getId(), vxId));
                        }

                        vxList.add(vxMap);
                    }
                }

                // Get the selected transactions.
                final int txSelectedId = rg.getAttribute(GraphElementType.TRANSACTION, VisualConcept.TransactionAttribute.SELECTED.getName());
                final int txCount = rg.getTransactionCount();
                for (int position = 0; position < txCount; position++) {
                    final int txId = rg.getTransaction(position);

                    final boolean selected = rg.getBooleanValue(txSelectedId, txId);
                    if (selected) {
                        final Map<String, String> txMap = new HashMap<>();
                        for (int apos = 0; apos < rg.getAttributeCount(GraphElementType.TRANSACTION); apos++) {
                            txMap.put("vx_src_", Integer.toString(rg.getTransactionSourceVertex(txId)));
                            txMap.put("vx_dst_", Integer.toString(rg.getTransactionDestinationVertex(txId)));
                            txMap.put("tx_dir_", Boolean.toString(rg.getTransactionDirection(position) != Graph.FLAT));

                            final Attribute attr = new GraphAttribute(rg, rg.getAttribute(GraphElementType.TRANSACTION, apos));
                            txMap.put(attr.getName(), rg.getStringValue(attr.getId(), txId));
                        }

                        txList.add(txMap);
                    }
                }
            } finally {
                rg.release();
            }

            final Map<String, List<Map<String, String>>> gmap = new HashMap<>();
            gmap.put("vertex", vxList);
            gmap.put("transaction", txList);
            final ObjectMapper om = new ObjectMapper();
            final String json = om.writeValueAsString(gmap);
            return String.format("JSON=%s", json);
        }

        @Override
        public void dragGestureRecognized(DragGestureEvent dge) {
            final Point offset = new Point(DRAG_DROP_ICON.getWidth(null), DRAG_DROP_ICON.getHeight(null));
            source.startDrag(dge, DragSource.DefaultCopyDrop, DRAG_DROP_ICON, offset, this, null);
        }
    }
}
