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
package au.gov.asd.tac.constellation.graph.utilities.planes;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeEvent;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeListener;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.Plane;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.PlaneState;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.utilities.planes.DragDropList.MyListModel;
import au.gov.asd.tac.constellation.graph.visual.graphics.BBoxf;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.NotificationDisplayer;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 * Top component which allows the user to manipulate planes in the graph
 * display.
 */
@ConvertAsProperties(
        dtd = "-//au.gov.asd.tac.constellation.graph.utilities.planes//PlaneManager//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "PlaneManagerTopComponent",
        iconBase = "au/gov/asd/tac/constellation/graph/utilities/planes/resources/plane-manager.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(
        mode = "explorer",
        openAtStartup = false
)
@ActionID(
        category = "Window",
        id = "au.gov.asd.tac.constellation.graph.utilities.planes.PlaneManagerTopComponent"
)
@ActionReferences({
    @ActionReference(path = "Menu/Experimental/Views", position = 0)
})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_PlaneManagerAction",
        preferredID = "PlaneManagerTopComponent"
)
@Messages({
    "CTL_PlaneManagerAction=Plane Manager",
    "CTL_PlaneManagerTopComponent=Plane Manager",
    "HINT_PlaneManagerTopComponent=Plane Manager"
})
public final class PlaneManagerTopComponent extends TopComponent implements LookupListener, GraphChangeListener {

    private final Lookup.Result<GraphNode> result;
    private GraphNode graphNode;
    private Graph graph;
//    private final NodeActivationListener activationListener;
//    private final NodeChangeListener changeListener;
    private int planesAttr;
    private long planesModificationCounter;
    private boolean isAdjustingList;

    private final JPopupMenu actionsMenu;

    public PlaneManagerTopComponent() {
        initComponents();
        setName(Bundle.CTL_PlaneManagerTopComponent());
        setToolTipText(Bundle.HINT_PlaneManagerTopComponent());

        planesAttr = Graph.NOT_FOUND;
        planesModificationCounter = -1;

        isAdjustingList = false;

//        activationListener = new NodeActivationListener();
//        changeListener = new NodeChangeListener();
//        setActivatedNodes(null);
        planeList.addListSelectionListener(e -> {
            if (!isAdjustingList) {
                final DragDropList.MyListModel listModel = ((DragDropList) planeList).getModel();
                final BitSet visibleLayers = new BitSet();
                final int[] selectedIndices = planeList.getSelectedIndices();
                for (int i = 0; i < selectedIndices.length; i++) {
                    final int index = listModel.getMyElementAt(selectedIndices[i]).index;
                    visibleLayers.set(index);
                }

                PluginExecution.withPlugin(new UpdatePlaneVisibilityPlugin(visibleLayers)).executeLater(graph);

//                    graphNode.getVisualisationManager().setVisiblePlanes(visibleLayers);
            }
        });

        actionsMenu = new JPopupMenu();

        final JMenuItem importMI = new JMenuItem("Import plane...");
        importMI.addActionListener(this::importPlaneActionPerformed);
        actionsMenu.add(importMI);

        final JMenuItem removeMI = new JMenuItem("Remove selected planes");
        removeMI.addActionListener(this::removeSelectedPlanesActionPerformed);
        actionsMenu.add(removeMI);

        final JMenuItem moveToSelectedMI = new JMenuItem("Move to selected vertices...");
        moveToSelectedMI.addActionListener(this::moveToSelectedVerticesActionPerformed);
        actionsMenu.add(moveToSelectedMI);

        final JMenuItem scaleMI = new JMenuItem("Scale selected planes...");
        scaleMI.addActionListener(this::scaleSelectedPlanesAction);
        actionsMenu.add(scaleMI);

//        // Are there any graphs with planes already open?
//        //        activationListener.activate();
        result = Utilities.actionsGlobalContext().lookupResult(GraphNode.class);
        result.addLookupListener(this);
    }

    private void importPlaneActionPerformed(final ActionEvent e) {
        final FileNameExtensionFilter filter = new FileNameExtensionFilter("Image file", "png", "jpg");
        final File f = new FileChooserBuilder("Import plane").addFileFilter(filter).showOpenDialog();
        if (f != null) {
            PluginExecution.withPlugin(new ImportPlanePlugin(f)).executeLater(graph);
        }
    }

    private void removeSelectedPlanesActionPerformed(final ActionEvent e) {
        final List<Integer> toRemove = getSelectedPlanes();
        if (!toRemove.isEmpty()) {
            final MyListModel model = ((DragDropList) planeList).getModel();
            for (int i = toRemove.size() - 1; i >= 0; i--) {
                model.removeMyElement(toRemove.get(i));
            }

            PluginExecution.withPlugin(new RemovePlanePlugin(toRemove)).executeLater(graph);
        }
    }

    private void moveToSelectedVerticesActionPerformed(final ActionEvent e) {
        final List<Integer> selectedPlanes = getSelectedPlanes();
        if (!selectedPlanes.isEmpty()) {
            final PlanePositionPanel ppp = new PlanePositionPanel();
            final DialogDescriptor dd = new DialogDescriptor(ppp, "Plane position");
            final Object option = DialogDisplayer.getDefault().notify(dd);
            if (option == DialogDescriptor.OK_OPTION) {
                PluginExecution.withPlugin(new SetPlanePositionPlugin(ppp, selectedPlanes)).executeLater(graph);
            }
        }
    }

    private void scaleSelectedPlanesAction(final ActionEvent e) {
        final List<Integer> selectedPlanes = getSelectedPlanes();
        if (!selectedPlanes.isEmpty()) {
            final Plane plane;
            final ReadableGraph rg = graph.getReadableGraph();
            try {
                final PlaneState state = (PlaneState) rg.getObjectValue(planesAttr, 0);
                plane = state.getPlane(selectedPlanes.get(0));
            } finally {
                rg.release();
            }

            final PlaneScalingPanel psp = new PlaneScalingPanel(plane);
            final DialogDescriptor dd = new DialogDescriptor(psp, "Plane scaling");
            final Object option = DialogDisplayer.getDefault().notify(dd);
            if (option == DialogDescriptor.OK_OPTION) {
                PluginExecution.withPlugin(new ScalePlanesPlugin(selectedPlanes, psp.getScale())).executeLater(graph);
            }
        }
    }

    private List<Integer> getSelectedPlanes() {
        final List<Integer> selected = new ArrayList<>();
        final ListSelectionModel lsm = planeList.getSelectionModel();
        if (lsm.getMinSelectionIndex() != -1) {
            for (int ix = lsm.getMinSelectionIndex(); ix <= lsm.getMaxSelectionIndex(); ix++) {
                if (lsm.isSelectedIndex(ix)) {
                    selected.add(ix);
                }
            }
        }

        return selected;
    }

    @Override
    public void resultChanged(final LookupEvent lev) {
        final Node[] nodes = TopComponent.getRegistry().getActivatedNodes();
        if (nodes != null && nodes.length == 1 && nodes[0] instanceof GraphNode) {
            final GraphNode gnode = ((GraphNode) nodes[0]);
            if (gnode != graphNode) {
                setNode(gnode);
            }
        } else {
            setNode(null);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        planeList = new DragDropList();
        unselectButton = new javax.swing.JButton();
        actionsButton = new javax.swing.JButton();

        jScrollPane1.setViewportView(planeList);

        org.openide.awt.Mnemonics.setLocalizedText(unselectButton, org.openide.util.NbBundle.getMessage(PlaneManagerTopComponent.class, "PlaneManagerTopComponent.unselectButton.text")); // NOI18N
        unselectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unselectButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(actionsButton, org.openide.util.NbBundle.getMessage(PlaneManagerTopComponent.class, "PlaneManagerTopComponent.actionsButton.text")); // NOI18N
        actionsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                actionsButtonMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(unselectButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(actionsButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(unselectButton)
                    .addComponent(actionsButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void unselectButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_unselectButtonActionPerformed
    {//GEN-HEADEREND:event_unselectButtonActionPerformed
        planeList.getSelectionModel().clearSelection();
    }//GEN-LAST:event_unselectButtonActionPerformed

    private void actionsButtonMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_actionsButtonMouseClicked
    {//GEN-HEADEREND:event_actionsButtonMouseClicked
        actionsMenu.show(actionsButton, evt.getX(), evt.getY());
    }//GEN-LAST:event_actionsButtonMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton actionsButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList<DragDropList.MyElement> planeList;
    private javax.swing.JButton unselectButton;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        result.addLookupListener(this);
        resultChanged(null);
    }

    @Override
    public void componentClosed() {
        result.removeLookupListener(this);
        setNode(null);
    }

    void writeProperties(final java.util.Properties p) {
        // Method intentionally left blank
    }

    void readProperties(final java.util.Properties p) {
        // Method intentionally left blank
    }

    @Override
    public void graphChanged(final GraphChangeEvent evt) {
        boolean update = false;
        final ReadableGraph rg = graph.getReadableGraph();
        try {
            if (planesAttr == Graph.NOT_FOUND) {
                final int pa = rg.getAttribute(GraphElementType.META, PlaneState.ATTRIBUTE_NAME);
                if (pa != Graph.NOT_FOUND) {
                    planesAttr = pa;
                    planesModificationCounter = rg.getValueModificationCounter(planesAttr);
                    update = true;
                }
            } else {
                final long pmc = rg.getValueModificationCounter(planesAttr);
                if (pmc != planesModificationCounter) {
                    planesModificationCounter = pmc;
                    update = true;
                }
            }

            if (update) {
                isAdjustingList = true;
                final PlaneState state = (PlaneState) rg.getObjectValue(planesAttr, 0);
                if (state != null) {
                    final List<Plane> planes = state.getPlanes();
                    final BitSet visiblePlanes = state.getVisiblePlanes();
                    ((DragDropList) planeList).setPlanes(planes, visiblePlanes);
                }
                isAdjustingList = false;
            }
        } finally {
            rg.release();
        }

    }

    private void setNode(final GraphNode node) {
        isAdjustingList = true;
        if (graphNode != null) {
            ((DragDropList) planeList).setPlanes(null, null);
            graph.removeGraphChangeListener(this);
        }

        if (node != null) {
            graphNode = node;
            graph = graphNode.getGraph();

            final ReadableGraph rg = graph.getReadableGraph();
            try {
                planesAttr = rg.getAttribute(GraphElementType.META, PlaneState.ATTRIBUTE_NAME);
                if (planesAttr != Graph.NOT_FOUND) {
                    final PlaneState state = (PlaneState) rg.getObjectValue(planesAttr, 0);
                    if (state != null) {
                        final List<Plane> planes = state.getPlanes();
                        final BitSet visiblePlanes = state.getVisiblePlanes();
                        ((DragDropList) planeList).setPlanes(planes, visiblePlanes);
                    }
                }
            } finally {
                rg.release();
            }

            graph.addGraphChangeListener(this);

        } else {
            graphNode = null;
            graph = null;
        }
        isAdjustingList = false;
    }


    /**
     * Plugin to update the plane visibility on the graph.
     */
    @PluginInfo(pluginType = PluginType.VIEW, tags = {PluginTags.MODIFY})
    private static class UpdatePlaneVisibilityPlugin extends SimpleEditPlugin {

        private final BitSet visibleLayers;

        public UpdatePlaneVisibilityPlugin(final BitSet visibleLayers) {
            this.visibleLayers = visibleLayers;
        }

        @Override
        public String getName() {
            return "Update Plane Visibility";
        }

        @Override
        protected void edit(GraphWriteMethods graph, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException {
            final int planesAttr = graph.getAttribute(GraphElementType.META, PlaneState.ATTRIBUTE_NAME);
            if (planesAttr != Graph.NOT_FOUND) {
                // We can't just change the object on the graph, the graph won't recognise it as a change.
                final PlaneState oldState = (PlaneState) graph.getObjectValue(planesAttr, 0);
                final PlaneState state = new PlaneState(oldState);
                state.setVisiblePlanes(visibleLayers);
                graph.setObjectValue(planesAttr, 0, state);
            }
        }

    }

    /**
     * Plugin to import the plane on the graph.
     */
    @PluginInfo(pluginType = PluginType.VIEW, tags = {PluginTags.IMPORT})
    private static class ImportPlanePlugin extends SimpleEditPlugin {
        
        private static final Logger LOGGER = Logger.getLogger(ImportPlanePlugin.class.getName());

        private final File f;

        public ImportPlanePlugin(final File f) {
            this.f = f;
        }

        @Override
        public String getName() {
            return "Import Plane";
        }

        @Override
        public void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            try {
                // Read the image and convert it to the required type if necessary.
                BufferedImage bi = ImageIO.read(f);
                if (bi.getType() != BufferedImage.TYPE_4BYTE_ABGR) {
                    final BufferedImage bi2 = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
                    bi2.createGraphics().drawImage(bi, 0, 0, null);
                    bi = bi2;
                }

                final String label = f.getName();

                // Icons have a default radius of 1, but images tend to be 100s or 1000s of pixels in size.
                // This means that images are just too big by default.
                // Change the displayed size to match the graph.
                final BBoxf box = BBoxf.getGraphBoundingBox(wg);
                final float graphScale = Math.min(box.getMax()[BBoxf.X] - box.getMin()[BBoxf.X], box.getMax()[BBoxf.Y] - box.getMin()[BBoxf.Y]);
                final float imgScale = Math.max(bi.getWidth(), bi.getHeight());
                final float sizeFactor = graphScale / imgScale;
                final float width = bi.getWidth() * sizeFactor;
                final float height = bi.getHeight() * sizeFactor;
                final float[] centre = box.getCentre();
                final Plane plane = new Plane(label, centre[0] - width / 2f, centre[1] - height / 2f, 0, width, height, bi, bi.getWidth(), bi.getHeight());
                int planesAttr = wg.getAttribute(GraphElementType.META, PlaneState.ATTRIBUTE_NAME);
                if (planesAttr == Graph.NOT_FOUND) {
                    planesAttr = wg.addAttribute(GraphElementType.META, PlaneState.ATTRIBUTE_NAME, PlaneState.ATTRIBUTE_NAME, PlaneState.ATTRIBUTE_NAME, null, null);
                }

                // We can't just change the object on the graph, the graph won't recognise it as a change.
                PlaneState oldState = (PlaneState) wg.getObjectValue(planesAttr, 0);
                final PlaneState state = oldState != null ? new PlaneState(oldState) : new PlaneState();
                state.addPlane(plane);
                wg.setObjectValue(planesAttr, 0, state);
            } catch (final IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
                NotificationDisplayer.getDefault().notify("Problem importing image",
                        UserInterfaceIconProvider.ERROR.buildIcon(16, ConstellationColor.CHERRY.getJavaColor()),
                        ex.getMessage(),
                        null
                );
            }
        }
    }

    /**
     * Plugin to remove the plane from the graph.
     */
    @PluginInfo(pluginType = PluginType.VIEW, tags = {PluginTags.DELETE})
    private static class RemovePlanePlugin extends SimpleEditPlugin {

        final List<Integer> toRemove;

        public RemovePlanePlugin(final List<Integer> toRemove) {
            this.toRemove = toRemove;
        }

        @Override
        public String getName() {
            return "Remove Plane";
        }

        @Override
        public void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            int planesAttr = wg.getAttribute(GraphElementType.META, PlaneState.ATTRIBUTE_NAME);
            if (planesAttr == Graph.NOT_FOUND) {
                planesAttr = wg.addAttribute(GraphElementType.META, PlaneState.ATTRIBUTE_NAME, PlaneState.ATTRIBUTE_NAME, PlaneState.ATTRIBUTE_NAME, null, null);
            }

            PlaneState oldState = (PlaneState) wg.getObjectValue(planesAttr, 0);
            final PlaneState state = oldState != null ? new PlaneState(oldState) : new PlaneState();
            for (int i = toRemove.size() - 1; i >= 0; i--) {
                state.removePlane(toRemove.get(i));
            }
            wg.setObjectValue(planesAttr, 0, state);
        }

    }

    /**
     * Plugin to set the plane position on the graph.
     */
    @PluginInfo(pluginType = PluginType.VIEW, tags = {PluginTags.MODIFY})
    private static class SetPlanePositionPlugin extends SimpleEditPlugin {

        final PlanePositionPanel ppp;
        final List<Integer> selectedPlanes;

        public SetPlanePositionPlugin(final PlanePositionPanel ppp, final List<Integer> selectedPlanes) {
            this.ppp = ppp;
            this.selectedPlanes = selectedPlanes;

        }

        @Override
        public String getName() {
            return "Set plane position";
        }

        @Override
        protected void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            final int xId = VisualConcept.VertexAttribute.X.get(wg);
            final int yId = VisualConcept.VertexAttribute.Y.get(wg);
            final int zId = VisualConcept.VertexAttribute.Z.get(wg);
            final int nradiusId = VisualConcept.VertexAttribute.NODE_RADIUS.get(wg);
            final int selectedId = VisualConcept.VertexAttribute.SELECTED.get(wg);
            int planesAttr = wg.getAttribute(GraphElementType.META, PlaneState.ATTRIBUTE_NAME);
            if (planesAttr == Graph.NOT_FOUND) {
                planesAttr = wg.addAttribute(GraphElementType.META, PlaneState.ATTRIBUTE_NAME, PlaneState.ATTRIBUTE_NAME, PlaneState.ATTRIBUTE_NAME, null, null);
            }
            final int vxCount = wg.getVertexCount();
            final BBoxf box = new BBoxf();
            int found = 0;
            float nradius = 0;
            for (int position = 0; position < vxCount; position++) {
                final int vxId = wg.getVertex(position);

                final boolean selected = wg.getBooleanValue(selectedId, vxId);
                if (selected) {
                    final float x = wg.getFloatValue(xId, vxId);
                    final float y = wg.getFloatValue(yId, vxId);
                    final float z = wg.getFloatValue(zId, vxId);
                    box.add(x, y, z);

                    found++;
                    nradius = wg.getFloatValue(nradiusId, vxId);
                }
            }

            if (found != 1) {
                nradius = 0;
            }

            if (!box.isEmpty()) {
                final float[] centre = box.getCentre();

                final PlaneState oldState = (PlaneState) wg.getObjectValue(planesAttr, 0);
                final PlaneState state = new PlaneState(oldState);
                for (int ix : selectedPlanes) {
                    final Plane plane = state.getPlane(ix);
                    final float[] xyz = ppp.getPosition(wg, centre[BBoxf.X], centre[BBoxf.Y], centre[BBoxf.Z], nradius, plane.getWidth(), plane.getHeight());
                    plane.setX(xyz[0]);
                    plane.setY(xyz[1]);
                    plane.setZ(xyz[2]);
                }

                wg.setObjectValue(planesAttr, 0, state);
            } else {
                NotificationDisplayer.getDefault().notify("No nodes selected",
                        UserInterfaceIconProvider.ERROR.buildIcon(16, ConstellationColor.CHERRY.getJavaColor()),
                        "Please select one or more nodes",
                        null
                );
            }
        }

    }

    /**
     * Plugin to scale the plane on the graph.
     */
    @PluginInfo(pluginType = PluginType.VIEW, tags = {PluginTags.MODIFY})
    private static class ScalePlanesPlugin extends SimpleEditPlugin {

        final List<Integer> selectedPlanes;
        final float newScale;

        public ScalePlanesPlugin(final List<Integer> selectedPlanes, final float newScale) {
            this.selectedPlanes = selectedPlanes;
            this.newScale = newScale;

        }

        @Override
        public String getName() {
            return "Scale selected planes";
        }

        @Override
        protected void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            int planesAttr = wg.getAttribute(GraphElementType.META, PlaneState.ATTRIBUTE_NAME);
            if (planesAttr == Graph.NOT_FOUND) {
                planesAttr = wg.addAttribute(GraphElementType.META, PlaneState.ATTRIBUTE_NAME, PlaneState.ATTRIBUTE_NAME, PlaneState.ATTRIBUTE_NAME, null, null);
            }
            final PlaneState oldState = (PlaneState) wg.getObjectValue(planesAttr, 0);
            final PlaneState state = new PlaneState(oldState);
            for (int ix : selectedPlanes) {
                final Plane plane = state.getPlane(ix);
                final float centrex = plane.getX() + plane.getWidth() / 2f;
                final float centrey = plane.getY() + plane.getHeight() / 2f;
                final float w = plane.getImageWidth() * newScale;
                final float h = plane.getImageHeight() * newScale;
                plane.setX(centrex - w / 2f);
                plane.setY(centrey - h / 2f);
                plane.setWidth(w);
                plane.setHeight(h);
            }
            wg.setObjectValue(planesAttr, 0, state);
        }
    }
}
