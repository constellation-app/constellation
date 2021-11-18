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
package au.gov.asd.tac.constellation.graph.utilities.perspectives;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.manager.GraphManagerListener;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.utilities.perspectives.PerspectiveModel.Perspective;
import au.gov.asd.tac.constellation.graph.visual.graphics.BBoxf;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

/**
 * Top component to manage perspective bookmarks.
 */
@ConvertAsProperties(
        dtd = "-//au.gov.asd.tac.constellation.graph.utilities.perspectives//PerspectiveBookmark//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "PerspectiveBookmarkTopComponent",
        iconBase = "au/gov/asd/tac/constellation/graph/utilities/perspectives/resources/perspective-bookmarks.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(
        mode = "explorer",
        openAtStartup = false
)
@ActionID(
        category = "Window",
        id = "au.gov.asd.tac.constellation.graph.utilities.perspectives.PerspectiveBookmarkTopComponent"
)
@ActionReferences({
    @ActionReference(path = "Menu/Experimental/Views", position = 0)
})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_PerspectiveBookmarkAction",
        preferredID = "PerspectiveBookmarkTopComponent"
)
@Messages({
    "CTL_PerspectiveBookmarkAction=Perspective Bookmarks",
    "CTL_PerspectiveBookmarkTopComponent=Perspective Bookmarks",
    "HINT_PerspectiveBookmarkTopComponent=This window remembers graph views from different camera positions",
    "MSG_AddPerspective=Add perspective",
    "MSG_RemovePerspective=Remove perspective"
})
public final class PerspectiveBookmarkTopComponent extends TopComponent implements GraphManagerListener {
    
    private static final Logger LOGGER = Logger.getLogger(PerspectiveBookmarkTopComponent.class.getName());

    private PerspectiveModel perspectiveModel;

    public PerspectiveBookmarkTopComponent() {
        initComponents();
        setName(Bundle.CTL_PerspectiveBookmarkTopComponent());
        setToolTipText(Bundle.HINT_PerspectiveBookmarkTopComponent());

        perspectiveModel = new PerspectiveModel();
        perspectivesList.setModel(perspectiveModel);
    }

    private void enableUI(final boolean enable) {
        addButton.setEnabled(enable);
        removeButton.setEnabled(enable);
    }

    private void moveToPerspective() {
        final Graph graph = GraphManager.getDefault().getActiveGraph();
        if (graph != null) {
            PluginExecution.withPlugin(new ChangePerspectivePlugin(perspectivesList.getSelectedValue())).executeLater(graph);
        }
    }

    /**
     * Update the data on the graph.
     * <p>
     * A new instance of the model is created, so graph undo/redo works
     * correctly.
     *
     */
    private void updateOnGraph(final String actionType) {
        final Graph graph = GraphManager.getDefault().getActiveGraph();
        if (graph != null) {
            perspectiveModel = new PerspectiveModel(perspectiveModel);
            final UpdatePerspectivePlugin perspectivePlugin = new UpdatePerspectivePlugin(perspectiveModel);
            perspectivePlugin.setName(actionType);
            PluginExecution.withPlugin(perspectivePlugin).executeLater(graph);
            perspectivesList.setModel(perspectiveModel);
        }
    }

    private void renamePerspective() {
        final int pix = perspectivesList.getSelectedIndex();
        if (pix >= 0) {
            final Perspective p = perspectiveModel.getElementAt(pix);
            final RenamePanel rp = new RenamePanel(p.label);
            final DialogDescriptor dd = new DialogDescriptor(rp, "Rename " + p.label);
            final Object result = DialogDisplayer.getDefault().notify(dd);
            if (result == NotifyDescriptor.OK_OPTION) {
                final String newLabel = rp.getLabel().trim();
                if (!newLabel.equals(p.label)) {
                    perspectiveModel.removeElementAt(pix);
                    final int ix = perspectiveModel.addElement(new Perspective(newLabel, p));
                    SwingUtilities.invokeLater(() -> {
                        perspectivesList.setSelectedIndex(ix);
                    });

                    final Graph graph = GraphManager.getDefault().getActiveGraph();
                    if (graph != null) {
                        PluginExecution.withPlugin(new UpdatePerspectivePlugin(perspectiveModel)).executeLater(graph);
                    }
                }
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        addButton = new javax.swing.JButton();
        renameButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        perspectivesList = new javax.swing.JList<>();

        jToolBar1.setFloatable(false);
        jToolBar1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jToolBar1.setRollover(true);

        addButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/au/gov/asd/tac/constellation/graph/utilities/perspectives/resources/add.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(PerspectiveBookmarkTopComponent.class, "PerspectiveBookmarkTopComponent.addButton.text")); // NOI18N
        addButton.setToolTipText(org.openide.util.NbBundle.getMessage(PerspectiveBookmarkTopComponent.class, "PerspectiveBookmarkTopComponent.addButton.toolTipText")); // NOI18N
        addButton.setFocusable(false);
        addButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(addButton);

        renameButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/au/gov/asd/tac/constellation/graph/utilities/perspectives/resources/rename.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(renameButton, org.openide.util.NbBundle.getMessage(PerspectiveBookmarkTopComponent.class, "PerspectiveBookmarkTopComponent.renameButton.text")); // NOI18N
        renameButton.setToolTipText(org.openide.util.NbBundle.getMessage(PerspectiveBookmarkTopComponent.class, "PerspectiveBookmarkTopComponent.renameButton.toolTipText")); // NOI18N
        renameButton.setFocusable(false);
        renameButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        renameButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        renameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                renameButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(renameButton);

        removeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/au/gov/asd/tac/constellation/graph/utilities/perspectives/resources/remove.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(PerspectiveBookmarkTopComponent.class, "PerspectiveBookmarkTopComponent.removeButton.text")); // NOI18N
        removeButton.setToolTipText(org.openide.util.NbBundle.getMessage(PerspectiveBookmarkTopComponent.class, "PerspectiveBookmarkTopComponent.removeButton.toolTipText")); // NOI18N
        removeButton.setFocusable(false);
        removeButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        removeButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(removeButton);

        perspectivesList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                perspectivesListMouseClicked(evt);
            }
        });
        perspectivesList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                perspectivesListKeyPressed(evt);
            }
        });
        jScrollPane1.setViewportView(perspectivesList);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 369, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_addButtonActionPerformed
    {//GEN-HEADEREND:event_addButtonActionPerformed
        final Graph graph = GraphManager.getDefault().getActiveGraph();
        if (graph != null) {
            Future<?> f = PluginExecution.withPlugin(new AddPerspectivePlugin(perspectiveModel, perspectivesList)).executeLater(graph);
            try {
                f.get();
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.SEVERE, "Thread was interrupted", ex);
            } catch (final ExecutionException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
            updateOnGraph("Add Perspective Model");
        }
    }//GEN-LAST:event_addButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_removeButtonActionPerformed
    {//GEN-HEADEREND:event_removeButtonActionPerformed
        final int[] selection = perspectivesList.getSelectedIndices();
        for (int i = selection.length - 1; i >= 0; i--) {
            perspectiveModel.removeElementAt(selection[i]);
        }
        updateOnGraph("Remove Perspective Model");
    }//GEN-LAST:event_removeButtonActionPerformed

    private void perspectivesListMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_perspectivesListMouseClicked
    {//GEN-HEADEREND:event_perspectivesListMouseClicked
        if (evt.getButton() == MouseEvent.BUTTON1 && evt.getClickCount() == 2) {
            moveToPerspective();
        }
    }//GEN-LAST:event_perspectivesListMouseClicked

    private void perspectivesListKeyPressed(java.awt.event.KeyEvent evt)//GEN-FIRST:event_perspectivesListKeyPressed
    {//GEN-HEADEREND:event_perspectivesListKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            moveToPerspective();
            evt.consume();
        } else if (evt.getKeyCode() == KeyEvent.VK_F2) {
            renamePerspective();
            evt.consume();
        } else {
            // Do nothing
        }
    }//GEN-LAST:event_perspectivesListKeyPressed

    private void renameButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_renameButtonActionPerformed
    {//GEN-HEADEREND:event_renameButtonActionPerformed
        renamePerspective();
    }//GEN-LAST:event_renameButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JList<Perspective> perspectivesList;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton renameButton;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        enableUI(GraphManager.getDefault().getActiveGraph() != null);
        GraphManager.getDefault().addGraphManagerListener(this);
    }

    @Override
    public void componentClosed() {
        GraphManager.getDefault().removeGraphManagerListener(this);
    }

    void writeProperties(java.util.Properties p) {
        // required for @ConvertAsProperties
    }

    void readProperties(java.util.Properties p) {
        // required for @ConvertAsProperties
    }

    @Override
    public void graphOpened(final Graph graph) {
        // required for implementation of GraphManagerListener
    }

    @Override
    public void graphClosed(final Graph graph) {
//        enableUI(false);
    }

    @Override
    public void newActiveGraph(final Graph graph) {
        enableUI(graph != null);
        if (graph != null) {
            final ReadableGraph rg = graph.getReadableGraph();
            try {
                final int pId = rg.getAttribute(GraphElementType.META, PerspectiveAttributeDescription.ATTRIBUTE_NAME);
                if (pId != Graph.NOT_FOUND) {
                    perspectiveModel = (PerspectiveModel) rg.getObjectValue(pId, 0);
                    perspectivesList.setModel(perspectiveModel != null ? perspectiveModel : new PerspectiveModel());

                    return;
                }
            } finally {
                rg.release();
            }
        }

        perspectiveModel = new PerspectiveModel();
        perspectivesList.setModel(perspectiveModel);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("au.gov.asd.tac.constellation.functionality.perspectives.Perspective");
    }

    /**
     * Plugin to change the camera perspective
     */
    @PluginInfo(pluginType = PluginType.VIEW, tags = {PluginTags.VIEW})
    private static class ChangePerspectivePlugin extends SimpleEditPlugin {

        private final Perspective p;

        public ChangePerspectivePlugin(final Perspective p) {
            this.p = p;
        }

        @Override
        public String getName() {
            return "Change Camera Perspective";
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            final int cameraAttribute = VisualConcept.GraphAttribute.CAMERA.get(graph);
            final Camera oldCamera = graph.getObjectValue(cameraAttribute, 0);
            final Camera camera = new Camera(oldCamera);
            camera.lookAtPreviousCentre.set(camera.lookAtCentre);
            camera.lookAtPreviousEye.set(camera.lookAtEye);
            camera.lookAtPreviousUp.set(camera.lookAtUp);
            camera.lookAtPreviousRotation.set(camera.lookAtRotation);
            camera.lookAtCentre.set(p.centre);
            camera.lookAtEye.set(p.eye);
            camera.lookAtUp.set(p.up);
            camera.lookAtRotation.set(p.rotate);

            // Modify the lookAt relative to the bounding box,
            // so if the graph moves, we move with it.
            final float[] c = BBoxf.getGraphBoundingBox(graph).getCentre();
            final Vector3f centre = new Vector3f(c[BBoxf.X], c[BBoxf.Y], c[BBoxf.Z]);
            camera.lookAtCentre.add(centre);
            camera.lookAtEye.add(centre);
            graph.setObjectValue(cameraAttribute, 0, camera);
        }
    }

    /**
     * Plugin to update the perspective model for the graph
     */
    @PluginInfo(pluginType = PluginType.VIEW, tags = {PluginTags.VIEW})
    private static class UpdatePerspectivePlugin extends SimpleEditPlugin {

        private final PerspectiveModel perspectiveModel;
        private String pluginName = "Update Perspective";

        public UpdatePerspectivePlugin(final PerspectiveModel perspectiveModel) {
            this.perspectiveModel = perspectiveModel;
        }

        public void setName(final String newName) {
            this.pluginName = newName;
        }

        @Override
        public String getName() {
            return pluginName;
        }

        @Override
        public void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            int perspectiveId = wg.getAttribute(GraphElementType.META, PerspectiveAttributeDescription.ATTRIBUTE_NAME);
            if (perspectiveId == Graph.NOT_FOUND) {
                perspectiveId = wg.addAttribute(GraphElementType.META, PerspectiveAttributeDescription.ATTRIBUTE_NAME, PerspectiveAttributeDescription.ATTRIBUTE_NAME, PerspectiveAttributeDescription.ATTRIBUTE_NAME, null, null);
            }
            wg.setObjectValue(perspectiveId, 0, perspectiveModel);
        }
    }

    /**
     * Plugin to update the perspective model for the graph
     */
    @PluginInfo(pluginType = PluginType.VIEW, tags = {PluginTags.VIEW})
    private static class AddPerspectivePlugin extends SimpleEditPlugin {

        private final PerspectiveModel perspectiveModel;
        private final JList<Perspective> perspectivesList;

        public AddPerspectivePlugin(final PerspectiveModel perspectiveModel, final JList<Perspective> perspectivesList) {
            this.perspectiveModel = perspectiveModel;
            this.perspectivesList = perspectivesList;
        }

        @Override
        public String getName() {
            return "Add Perspective";
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            final int cameraAttribute = VisualConcept.GraphAttribute.CAMERA.get(graph);
            final Camera camera = new Camera(graph.getObjectValue(cameraAttribute, 0));

            // Modify the lookAt relative to the bounding box,
            // so if the graph moves, we move with it.
            final float[] c = BBoxf.getGraphBoundingBox(graph).getCentre();
            final Vector3f centre = new Vector3f(c[BBoxf.X], c[BBoxf.Y], c[BBoxf.Z]);
            camera.lookAtCentre.subtract(centre);
            camera.lookAtEye.subtract(centre);

            final Perspective p = new Perspective(perspectiveModel.getNewLabel(), Graph.NOT_FOUND, camera.lookAtCentre, camera.lookAtEye, camera.lookAtUp, camera.lookAtRotation);
            final int ix = perspectiveModel.addElement(p);
            SwingUtilities.invokeLater(() -> {
                perspectivesList.setSelectedIndex(ix);
            });
        }
    }
}
