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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.ktruss;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeEvent;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeListener;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.ClusteringConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Hashtable;
import java.util.Iterator;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 * Top component which controls the display of k-trusses after running the
 * k-truss plugin.
 */
@ConvertAsProperties(
        dtd = "-//au.gov.asd.tac.constellation.plugins.algorithms.clustering//KTruss//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "KTrussControllerTopComponent",
        iconBase = "au/gov/asd/tac/constellation/plugins/algorithms/clustering/ktruss/ktruss.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(
        mode = "output",
        openAtStartup = false
)
@ActionID(
        category = "Views",
        id = "au.gov.asd.tac.constellation.plugins.algorithms.clustering.ktruss.KTrussControllerTopComponent"
)
@ActionReferences({
    @ActionReference(path = "Menu/Tools/Cluster", position = 100)
})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_KTrussControllerAction",
        preferredID = "KTrussControllerTopComponent"
)
@Messages({
    "CTL_KTrussControllerAction=K-Truss",
    "CTL_KTrussControllerTopComponent=K-Truss",
    "HINT_KTrussControllerTopComponent=K-Truss"
})
public final class KTrussControllerTopComponent extends TopComponent implements LookupListener, GraphChangeListener, ComponentListener {

    private static final String TOGGLE_DISABLED = "Toggle Interactive: Disabled";
    private static final String TOGGLE_ENABLED = "Toggle Interactive: Enabled";

    private final Lookup.Result<GraphNode> result;
    private GraphNode graphNode;
    private Graph graph;
    private KTrussState state;
    private boolean isAdjusting;
    private boolean nestedPanelIsVisible;

    // The panel to display the nesting of the connected components of k-trusses as rectangles.
    private NestedKTrussDisplayPanel dp;
    // The height to allocate for the NestedKTrussDisplayPanel.
    private final int nestedTrussesHeight;

    /**
     * Construct a KTrussControllerTopComponent.
     */
    public KTrussControllerTopComponent() {
        initComponents();
        setName(Bundle.CTL_KTrussControllerTopComponent());
        setToolTipText(Bundle.HINT_KTrussControllerTopComponent());
        result = Utilities.actionsGlobalContext().lookupResult(GraphNode.class);
        result.addLookupListener(this);
        isAdjusting = false;
        nestedTrussesHeight = 500;
        nestedPanelIsVisible = false;
        updateInteractiveButton(interactiveButton.getText().equals(TOGGLE_DISABLED));
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        stepSlider = new javax.swing.JSlider();
        excludedElementsLabel = new javax.swing.JLabel();
        upButton = new javax.swing.JButton();
        downButton = new javax.swing.JButton();
        hiddenRadioButton = new javax.swing.JRadioButton();
        dimmedRadioButton = new javax.swing.JRadioButton();
        selectButton = new javax.swing.JButton();
        nestedTrussButton = new javax.swing.JButton();
        reclusterButton = new javax.swing.JButton();
        interactiveButton = new javax.swing.JToggleButton();
        colorNestedTrussesCheckBox = new javax.swing.JCheckBox();
        nestedTrussPane = new javax.swing.JScrollPane();
        helpButton = new javax.swing.JButton();

        setBackground(new java.awt.Color(0, 255, 204));
        setMaximumSize(new java.awt.Dimension(800, 580));
        setMinimumSize(new java.awt.Dimension(530, 185));

        stepSlider.setMajorTickSpacing(1);
        stepSlider.setMaximum(10);
        stepSlider.setMinorTickSpacing(1);
        stepSlider.setPaintLabels(true);
        stepSlider.setPaintTicks(true);
        stepSlider.setSnapToTicks(true);
        stepSlider.setName("stepSlider"); // NOI18N
        stepSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                stepSliderStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(excludedElementsLabel, org.openide.util.NbBundle.getMessage(KTrussControllerTopComponent.class, "KTrussControllerTopComponent.excludedElementsLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(upButton, org.openide.util.NbBundle.getMessage(KTrussControllerTopComponent.class, "KTrussControllerTopComponent.upButton.text")); // NOI18N
        upButton.setToolTipText(org.openide.util.NbBundle.getMessage(KTrussControllerTopComponent.class, "KTrussControllerTopComponent.upButton.toolTipText")); // NOI18N
        upButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(downButton, org.openide.util.NbBundle.getMessage(KTrussControllerTopComponent.class, "KTrussControllerTopComponent.downButton.text")); // NOI18N
        downButton.setToolTipText(org.openide.util.NbBundle.getMessage(KTrussControllerTopComponent.class, "KTrussControllerTopComponent.downButton.toolTipText")); // NOI18N
        downButton.setActionCommand(org.openide.util.NbBundle.getMessage(KTrussControllerTopComponent.class, "KTrussControllerTopComponent.downButton.actionCommand")); // NOI18N
        downButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(hiddenRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(hiddenRadioButton, org.openide.util.NbBundle.getMessage(KTrussControllerTopComponent.class, "KTrussControllerTopComponent.hiddenRadioButton.text")); // NOI18N
        hiddenRadioButton.setToolTipText(org.openide.util.NbBundle.getMessage(KTrussControllerTopComponent.class, "KTrussControllerTopComponent.hiddenRadioButton.toolTipText")); // NOI18N
        hiddenRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hiddenRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(dimmedRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(dimmedRadioButton, org.openide.util.NbBundle.getMessage(KTrussControllerTopComponent.class, "KTrussControllerTopComponent.dimmedRadioButton.text")); // NOI18N
        dimmedRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dimmedRadioButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(selectButton, org.openide.util.NbBundle.getMessage(KTrussControllerTopComponent.class, "KTrussControllerTopComponent.selectButton.text")); // NOI18N
        selectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(nestedTrussButton, org.openide.util.NbBundle.getMessage(KTrussControllerTopComponent.class, "KTrussControllerTopComponent.nestedTrussButton.text")); // NOI18N
        nestedTrussButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nestedTrussButtonActionPerformed(evt);
            }
        });

        reclusterButton.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(reclusterButton, org.openide.util.NbBundle.getMessage(KTrussControllerTopComponent.class, "KTrussControllerTopComponent.reclusterButton.text")); // NOI18N
        reclusterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reclusterButtonActionPerformed(evt);
            }
        });

        interactiveButton.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(interactiveButton, org.openide.util.NbBundle.getMessage(KTrussControllerTopComponent.class, "KTrussControllerTopComponent.interactiveButton.text")); // NOI18N
        interactiveButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        interactiveButton.setFocusable(false);
        interactiveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                interactiveButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(colorNestedTrussesCheckBox, org.openide.util.NbBundle.getMessage(KTrussControllerTopComponent.class, "KTrussControllerTopComponent.colorNestedTrussesCheckBox.text")); // NOI18N
        colorNestedTrussesCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorNestedTrussesCheckBoxActionPerformed(evt);
            }
        });

        nestedTrussPane.setBorder(null);
        nestedTrussPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        org.openide.awt.Mnemonics.setLocalizedText(helpButton, org.openide.util.NbBundle.getMessage(KTrussControllerTopComponent.class, "KTrussControllerTopComponent.helpButton.text")); // NOI18N
        helpButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                helpButtonMousePressed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(excludedElementsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)
                        .addComponent(hiddenRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dimmedRadioButton)
                        .addGap(18, 18, 18)
                        .addComponent(colorNestedTrussesCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 665, Short.MAX_VALUE)
                        .addComponent(helpButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(reclusterButton, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(interactiveButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(selectButton))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(downButton)
                            .addComponent(nestedTrussButton, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(stepSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(nestedTrussPane))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(upButton)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(stepSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(upButton, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(downButton, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(nestedTrussButton)
                    .addComponent(nestedTrussPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 46, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(reclusterButton)
                    .addComponent(interactiveButton)
                    .addComponent(selectButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(excludedElementsLabel)
                        .addComponent(hiddenRadioButton)
                        .addComponent(dimmedRadioButton)
                        .addComponent(colorNestedTrussesCheckBox))
                    .addComponent(helpButton))
                .addContainerGap())
        );

        hiddenRadioButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(KTrussControllerTopComponent.class, "KTrussControllerTopComponent.hiddenRadioButton.AccessibleContext.accessibleName")); // NOI18N
        dimmedRadioButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(KTrussControllerTopComponent.class, "KTrussControllerTopComponent.dimmedRadioButton.AccessibleContext.accessibleName")); // NOI18N
        interactiveButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(KTrussControllerTopComponent.class, "KTrussControllerTopComponent.interactiveButton.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void stepSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_stepSliderStateChanged
        if (!isAdjusting) {
            state.setCurrentK(stepSlider.getValue());
            updateGraph();
        }
    }//GEN-LAST:event_stepSliderStateChanged

    private void downButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downButtonActionPerformed
        if (state.getCurrentK() > 0) {
            final int val = stepSlider.getValue() - 1;
            stepSlider.setValue(val);
            state.setCurrentK(val);
            updateGraph();
        }
    }//GEN-LAST:event_downButtonActionPerformed

    private void upButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upButtonActionPerformed
        if (state.getCurrentK() < state.getHighestK()) {
            final int val = stepSlider.getValue() + 1;
            stepSlider.setValue(val);
            state.setCurrentK(val);
            updateGraph();
        }
    }//GEN-LAST:event_upButtonActionPerformed

    private void hiddenRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hiddenRadioButtonActionPerformed
        state.setExcludedElementsDimmed(false);
        state.displayOptionHasToggled();
        updateGraph();
    }//GEN-LAST:event_hiddenRadioButtonActionPerformed

    private void dimmedRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dimmedRadioButtonActionPerformed
        state.setExcludedElementsDimmed(true);
        state.displayOptionHasToggled();
        updateGraph();
    }//GEN-LAST:event_dimmedRadioButtonActionPerformed

    private void selectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectButtonActionPerformed
        selectOnGraph();
    }//GEN-LAST:event_selectButtonActionPerformed

    private void nestedTrussButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nestedTrussButtonActionPerformed
        // If the nested trusses display panel is already visible, hide it,
        // otherwise, show the nested trusses display panel
        if (state.isNestedTrussesVisible()) {
            hideNestedTrussesPanel();
        } else {
            showNestedTrussesPanel();
        }
        revalidateParents(this);
        // toggle the status of whether the nested trusses display panel is visible
        state.toggleNestedTrussesVisible();
    }//GEN-LAST:event_nestedTrussButtonActionPerformed

    private void reclusterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reclusterButtonActionPerformed
        PluginExecution.withPlugin(new KTrussCalculatePlugin(interactiveButton.isSelected())).executeLater(graph);
    }//GEN-LAST:event_reclusterButtonActionPerformed

    private void interactiveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_interactiveButtonActionPerformed
        if (state != null) {
            updateInteractivity();
        }
    }//GEN-LAST:event_interactiveButtonActionPerformed

    private void colorNestedTrussesCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorNestedTrussesCheckBoxActionPerformed
        setColoring();
    }//GEN-LAST:event_colorNestedTrussesCheckBoxActionPerformed

    private void helpButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_helpButtonMousePressed
        final HelpCtx help = new HelpCtx("au.gov.asd.tac.constellation.plugins.algorithms.clustering.ktruss.KTruss");
        help.display();
    }//GEN-LAST:event_helpButtonMousePressed

    private void updateInteractivity() {
        state.setInteractive(!state.isInteractive());
        if (!state.isInteractive()) {
            updateInteractiveButton(true);
            if (state.isNestedTrussesVisible()) {
                hideNestedTrussesPanel();
            }
            if (state.isNestedTrussesColored()) {
                final RemoveOverlayColors uncolor = new RemoveOverlayColors();
                PluginExecution.withPlugin(uncolor).interactively(true).executeLater(graph);
            }
        } else {
            updateInteractiveButton(false);
            if (state.isNestedTrussesVisible()) {
                showNestedTrussesPanel();
            }
            if (state.isNestedTrussesColored()) {
                final ColorTrusses color = new ColorTrusses(state);
                PluginExecution.withPlugin(color).interactively(true).executeLater(graph);
            }
        }
        setGroups(true);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBox colorNestedTrussesCheckBox;
    private javax.swing.JRadioButton dimmedRadioButton;
    private javax.swing.JButton downButton;
    private javax.swing.JLabel excludedElementsLabel;
    private javax.swing.JButton helpButton;
    private javax.swing.JRadioButton hiddenRadioButton;
    private javax.swing.JToggleButton interactiveButton;
    private javax.swing.JButton nestedTrussButton;
    private javax.swing.JScrollPane nestedTrussPane;
    private javax.swing.JButton reclusterButton;
    private javax.swing.JButton selectButton;
    private javax.swing.JSlider stepSlider;
    private javax.swing.JButton upButton;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        result.addLookupListener(this);
        resultChanged(null);
    }

    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);
        revalidateParents(this);
    }

    @Override
    public void requestActive() {
        super.requestActive();
        revalidateParents(this);
    }

    @Override
    public void componentClosed() {

        // If interactive was enabled when component closed, clear this state so that
        // graph doesn't render with interactive visuals. Also store this fact so that
        // if the component is reopened it can revert to previous state. This is stored in
        // a variable on this class rather than in state as state is cleared on close.
        if (state != null) {
            state.setInteractive(false);
            updateGraph();
        }
        result.removeLookupListener(this);
        setNode(null);
    }

    private void hideNestedTrussesPanel() {
        if (!nestedPanelIsVisible) {
            return;
        }
        Dimension d;
        final int sizeDifference = nestedTrussesHeight - nestedTrussButton.getHeight();

        nestedTrussPane.setSize(200, 0);
        nestedTrussPane.setMinimumSize(new Dimension(nestedTrussPane.getMinimumSize().width, 0));
        nestedTrussPane.setPreferredSize(new Dimension(200, 0));

        nestedTrussButton.setText("V");

        // resize this top component
        setSize(getWidth(), getHeight() - sizeDifference);
        d = getPreferredSize();
        d.height -= sizeDifference;
        setPreferredSize(d);
        d = getMinimumSize();
        d.height -= sizeDifference;
        setMinimumSize(d);

        // The grandparent of this TopComponent is the netbeans level JPanel we need to resize when in 'sliding/docked+minimised' mode. The parent of this TopComponent represents a tab, which we are not
        // interested in since it will be resized along with the JPanel
        final Container grandParentContainer = getParent().getParent();
        grandParentContainer.setSize(grandParentContainer.getWidth(), grandParentContainer.getHeight() - sizeDifference);
        d = grandParentContainer.getPreferredSize();
        d.height -= sizeDifference;
        grandParentContainer.setPreferredSize(d);

        nestedPanelIsVisible = false;
    }

    private void showNestedTrussesPanel() {
        if (nestedPanelIsVisible) {
            return;
        }
        Dimension d;
        final int sizeDifference = nestedTrussesHeight - nestedTrussButton.getHeight();

        nestedTrussPane.setSize(200, nestedTrussesHeight);
        nestedTrussPane.setMinimumSize(new Dimension(nestedTrussPane.getMinimumSize().width, nestedTrussesHeight));
        nestedTrussPane.setPreferredSize(new Dimension(200, nestedTrussesHeight));

        nestedTrussButton.setText("^");

        //resize this top component
        setSize(getWidth(), getHeight() + sizeDifference);
        d = getPreferredSize();
        d.height += sizeDifference;
        setPreferredSize(d);
        d = getMinimumSize();
        d.height += sizeDifference;
        setMinimumSize(d);

        // resize the grandparent of this top component
        final Container grandParentContainer = getParent().getParent();
        grandParentContainer.setSize(grandParentContainer.getWidth(), grandParentContainer.getHeight() + sizeDifference);
        d = grandParentContainer.getPreferredSize();
        d.height += sizeDifference;
        grandParentContainer.setPreferredSize(d);

        nestedPanelIsVisible = true;
    }

    // Make the entire parent chain of the container revalidate and repaint
    private static void revalidateParents(Container container) {
        while (container != null) {
            container.invalidate();
            container.validate();
            container.repaint();
            container = container.getParent();
        }
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
     * Make the graph in the specified node the source for the clustering model.
     * <p>
     * If another graph is attached to the model, it is detached first.
     *
     * @param node The GraphNode containing the graph to be displayed.
     */
    private void setNode(final GraphNode node) {
        if (graphNode != null) {
            graph.removeGraphChangeListener(this);
        }

        if (node != null) {

            // If the nested trusses panel was visible in the previous state, hide it
            if (state != null && state.isNestedTrussesVisible()) {
                hideNestedTrussesPanel();
            }

            graphNode = node;
            graph = graphNode.getGraph();
            final ReadableGraph rg = graph.getReadableGraph();
            try {
                final int stateAttr = ClusteringConcept.MetaAttribute.K_TRUSS_CLUSTERING_STATE.get(rg);
                state = stateAttr != Graph.NOT_FOUND ? (KTrussState) rg.getObjectValue(stateAttr, 0) : null;
                // If the nested trusses panel is visible in the new state, show it
                // Note that it won't be correctly repainted until setGroups is called.
                if (state != null && state.isNestedTrussesVisible() && state.isInteractive()) {
                    showNestedTrussesPanel();
                }
                if (state != null) {
                    colorNestedTrussesCheckBox.setSelected(state.isNestedTrussesColored());
                    interactiveButton.setSelected(state.isInteractive());
                    updateInteractiveButton(!state.isInteractive());
                } else {
                    interactiveButton.setSelected(false);
                    updateInteractiveButton(true);
                    colorNestedTrussesCheckBox.setSelected(false);
                }
            } finally {
                rg.release();
            }

            graph.addGraphChangeListener(this);
        } else {
            graphNode = null;
            graph = null;
            state = null;
        }
        revalidateParents(this);
        setGroups(false);
    }

    /**
     * We need to listen to the graph to know when a new KTrussState has been
     * set.
     *
     * @param evt The change event.
     */
    @Override
    public void graphChanged(final GraphChangeEvent evt) {

        long smc;
        final long mc;
        final ReadableGraph rg = graph.getReadableGraph();
        try {

            // Retrieve the k-truss state attribute, attribute mod counter, and structural mod counter from the graph
            final int stateAttr = ClusteringConcept.MetaAttribute.K_TRUSS_CLUSTERING_STATE.get(rg);
            smc = rg.getStructureModificationCounter();
            mc = stateAttr != Graph.NOT_FOUND ? rg.getValueModificationCounter(stateAttr) : Graph.NOT_FOUND;

            // If the k-truss state on the controller is null, or has a different modcount to the state on the graph, update this controller's state.
            if (state == null || mc != state.modificationCounter) {
                state = stateAttr != Graph.NOT_FOUND ? (KTrussState) rg.getObjectValue(stateAttr, 0) : null;
                setGroups(true);
            }

        } finally {
            rg.release();
        }

        // Update the controller state's modcount, and make the recluster button active if the srtuctual mod count does not match the graph.
        if (state != null) {
            state.modificationCounter = mc;
            reclusterButton.setEnabled(smc != state.strucModificationCount);
        }

        // Interactive button should only be available if state is available
        interactiveButton.setEnabled(state != null);
    }

    private void setGroups(final boolean doUpdate) {
        if (state != null && state.isInteractive()) {
            final Component[] children = getComponents();
            for (final Component c : children) {
                if (!c.equals(reclusterButton)) {
                    c.setEnabled(true);
                }
            }

            final int highestK = Math.max(state.getHighestK(), 2);
            final int currentK = highestK == 2 ? 2 : state.getCurrentK();
            final boolean excludedElementsDimmed = state.isExcludedElementsDimmed();

            isAdjusting = true;
            stepSlider.setMinimum(2);
            stepSlider.setMaximum(highestK);
            stepSlider.setValue(currentK);
            if (highestK > 20) {
                stepSlider.setMajorTickSpacing(5);
                stepSlider.setMinorTickSpacing(1);
            } else {
                stepSlider.setMajorTickSpacing(1);
                stepSlider.setMinorTickSpacing(1);
            }

            // Set the labels for the step slider
            final Hashtable<Integer, JComponent> labelTable = stepSlider.createStandardLabels(stepSlider.getMajorTickSpacing(), 2);
            final JLabel firstLabel = (JLabel) labelTable.get(2);
            firstLabel.setText("all");
            final Font labelFont = firstLabel.getFont();
            final Font labelBoldFont = labelFont.deriveFont(Font.BOLD | labelFont.getStyle());

            // Bold the labels which have significant values of k
            final Iterator<?> iter = labelTable.keySet().iterator();
            while (iter.hasNext()) {
                final int key = (Integer) iter.next();
                if (state.isKTrussExtant(key)) {
                    ((JLabel) labelTable.get(key)).setFont(labelBoldFont);
                }
            }
            stepSlider.setLabelTable(labelTable);

            hiddenRadioButton.setSelected(!excludedElementsDimmed);
            dimmedRadioButton.setSelected(excludedElementsDimmed);

            dp = new NestedKTrussDisplayPanel(state, graph);
            dp.setSize(stepSlider.getWidth(), nestedTrussPane.getHeight());  //set initial size before calculating rectangles
            dp.calculateRectangles();
            nestedTrussPane.setViewportView(dp);

            nestedTrussPane.getVerticalScrollBar().addAdjustmentListener(e -> dp.repaint());

            nestedTrussPane.addComponentListener(new ComponentListener() {
                @Override
                public void componentResized(final ComponentEvent e) {
                    dp.setSize(stepSlider.getWidth(), dp.getHeight());
                    revalidateParents(dp.getParent());
                }

                @Override
                public void componentMoved(final ComponentEvent e) {
                    // Override required for ComponentListener, intentionally left blank
                }

                @Override
                public void componentShown(final ComponentEvent e) {
                    // Override required for ComponentListener, intentionally left blank
                }

                @Override
                public void componentHidden(final ComponentEvent e) {
                    // Override required for ComponentListener, intentionally left blank
                }
            });
            revalidateParents(dp);

            isAdjusting = false;
        } else {
            final Component[] children = getComponents();
            for (final Component c : children) {
                if (!(c.equals(reclusterButton) || c.equals(interactiveButton))) {
                    c.setEnabled(false);
                }
            }
            if (state != null) {
                // Interactive button should only be available if state is available
                interactiveButton.setEnabled(true);
                updateInteractiveButton(false);
            } else {
                reclusterButton.setEnabled(true);
                interactiveButton.setEnabled(false);
                updateInteractiveButton(true);
            }
        }

        if ((state != null) && doUpdate) {
            updateGraph();
        }
    }

    private void updateGraph() {
        if (graphNode == null) {
            return;
        }

        // Determine if interactive is enabled, if it isn't, then overlay colors need to be removed.
        // This is used to revert the graph display when the component is closed and was previously
        // set to interactive.
        final boolean interactive = state.isInteractive();
        updateInteractiveButton(!interactive);

        if (!interactive) {
            final RemoveOverlayColors removeColors = new RemoveOverlayColors();
            PluginExecution.withPlugin(removeColors).interactively(true).executeLater(graph);
        }

        final Update update = new Update(state);
        PluginExecution.withPlugin(update).interactively(true).executeLater(graph);
        if (state.isNestedTrussesColored() && interactive) {
            final ColorTrusses color = new ColorTrusses(state);
            PluginExecution.withPlugin(color).interactively(true).executeLater(graph);
        }
    }

    private void setColoring() {
        if (graphNode == null) {
            return;
        }

        final boolean wasColored = state.isNestedTrussesColored();
        state.toggleNestedTrussesColored();

        final SimpleEditPlugin colorPlugin;
        if (wasColored) {
            colorPlugin = new RemoveOverlayColors();
        } else {
            colorPlugin = new ColorTrusses(state);
        }
        PluginExecution.withPlugin(colorPlugin).interactively(true).executeLater(graph);
    }

    private void selectOnGraph() {
        if (graphNode == null) {
            return;
        }

        if (dp != null) {
            dp.selectRectangles(state.getCurrentK());
        }

        final Select select = new Select(state);
        PluginExecution.withPlugin(select).interactively(true).executeLater(graph);
    }

    @PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.MODIFY})
    public static final class RemoveOverlayColors extends SimpleEditPlugin {

        @Override
        public String getName() {
            return "Remove Overlay Colors";
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
            int nodeColorRefAttr = VisualConcept.GraphAttribute.NODE_COLOR_REFERENCE.get(graph);
            if (nodeColorRefAttr != Graph.NOT_FOUND) {
                graph.setStringValue(nodeColorRefAttr, 0, null);
            }
            int transColorRefAttr = VisualConcept.GraphAttribute.TRANSACTION_COLOR_REFERENCE.get(graph);
            if (transColorRefAttr != Graph.NOT_FOUND) {
                graph.setStringValue(transColorRefAttr, 0, null);
            }
        }
    }

    @PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.MODIFY})
    public static final class ColorTrusses extends SimpleEditPlugin {

        private final KTrussState state;

        public ColorTrusses(final KTrussState state) {
            this.state = state;
        }

        @Override
        public String getName() {
            return "K-Truss: Color Nested";
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {

            // Retrieve (or if not extant, create) the node and transaction attributes pertaining to k-trusses and selection.
            final int vxKTrussAttr = ClusteringConcept.VertexAttribute.K_TRUSS_CLUSTER.ensure(graph);
            final int txKTrussAttr = ClusteringConcept.TransactionAttribute.K_TRUSS_CLUSTER.ensure(graph);
            final int vxOverlayColorAttr = ClusteringConcept.VertexAttribute.K_TRUSS_COLOR.ensure(graph);
            final int txOverlayColorAttr = ClusteringConcept.TransactionAttribute.K_TRUSS_COLOR.ensure(graph);
            final int vxColorRef = VisualConcept.GraphAttribute.NODE_COLOR_REFERENCE.ensure(graph);
            final int txColorRef = VisualConcept.GraphAttribute.TRANSACTION_COLOR_REFERENCE.ensure(graph);
            graph.setStringValue(vxColorRef, 0, ClusteringConcept.VertexAttribute.K_TRUSS_COLOR.getName());
            graph.setStringValue(txColorRef, 0, ClusteringConcept.TransactionAttribute.K_TRUSS_COLOR.getName());

            final ConstellationColor[] colors = ConstellationColor.createPalettePhi(state.getNumUniqueValuesOfK() + 2, 0, 0.5F, 0.95F);
            colors[0] = colors[colors.length - 1];

            // Determine and set the overlay color for each vertex
            for (int i = 0; i < graph.getVertexCount(); i++) {
                final int vxID = graph.getVertex(i);
                final int k = graph.getIntValue(vxKTrussAttr, vxID);
                final int colorIndex = state.getIndexOfKTruss(k);
                graph.setObjectValue(vxOverlayColorAttr, vxID, colors[colorIndex + 1]);
            }

            // Determine and set the overlay color for each transaction
            for (int i = 0; i < graph.getTransactionCount(); i++) {
                // Determine if we should display the current transaction based on its own k-truss attribute
                final int txID = graph.getTransaction(i);
                final int k = graph.getIntValue(txKTrussAttr, txID);
                final int colorIndex = state.getIndexOfKTruss(k);
                graph.setObjectValue(txOverlayColorAttr, txID, colors[colorIndex + 1]);
            }

        }
    }

    @PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.SELECT})
    public static final class Select extends SimpleEditPlugin {

        private final KTrussState state;

        public Select(final KTrussState state) {
            this.state = state;
        }

        @Override
        public String getName() {
            return "K-Truss: Select";
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {

            // Retrieve (or if not extant, create) the node and transaction attributes pertaining to k-trusses and selection.
            final int vxKTrussAttr = ClusteringConcept.VertexAttribute.K_TRUSS_CLUSTER.ensure(graph);
            final int txKTrussAttr = ClusteringConcept.TransactionAttribute.K_TRUSS_CLUSTER.ensure(graph);

            final int txSelectedAttr = VisualConcept.TransactionAttribute.SELECTED.get(graph);
            final int vxSelectedAttr = VisualConcept.VertexAttribute.SELECTED.get(graph);

            // Retrieve the current value of K from the KTrussState
            final int currentK = state.getCurrentK();

            // Update the selection of the graph's nodes
            for (int i = 0; i < graph.getVertexCount(); i++) {
                // Determine if we should select the current vertex based on its own k-truss attribute
                final int vxID = graph.getVertex(i);
                final int k = graph.getIntValue(vxKTrussAttr, vxID);
                final boolean selectCurrentVertex = k >= currentK;
                // Select (or deselect) the vertex
                graph.setBooleanValue(vxSelectedAttr, vxID, selectCurrentVertex);
            }

            // Update the selection of the graph's transactions
            for (int i = 0; i < graph.getTransactionCount(); i++) {
                // Determine if we should display the current transaction based on its own k-truss attribute
                final int txID = graph.getTransaction(i);
                final int k = graph.getIntValue(txKTrussAttr, txID);
                final boolean selectCurrentTransaction = k >= currentK;
                // Select (or deselect) the vertex
                graph.setBooleanValue(txSelectedAttr, txID, selectCurrentTransaction);
            }

        }
    }

    @PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.MODIFY})
    public static final class Update extends SimpleEditPlugin {

        private final KTrussState state;

        public Update(final KTrussState state) {
            this.state = state;
        }

        @Override
        public String getName() {
            return "K-Truss: Update Visiblity";
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {

            // Retrieve (or if not extant, create) the node and transaction attributes pertaining to k-trusses, and visibility/dimming.
            final int vxKTrussAttr = ClusteringConcept.VertexAttribute.K_TRUSS_CLUSTER.ensure(graph);
            final int txKTrussAttr = ClusteringConcept.TransactionAttribute.K_TRUSS_CLUSTER.ensure(graph);

            final int vxDimmedAttr = VisualConcept.VertexAttribute.DIMMED.ensure(graph);
            final int txDimmedAttr = VisualConcept.TransactionAttribute.DIMMED.ensure(graph);

            final int vxVisibilityAttr = VisualConcept.VertexAttribute.VISIBILITY.ensure(graph);
            final int txVisibilityAttr = VisualConcept.TransactionAttribute.VISIBILITY.ensure(graph);

            // Retrieve the display options from the KTrussState
            final int currentK = state.isInteractive() ? state.getCurrentK() : 0;
            final boolean dim = state.isExcludedElementsDimmed();
            final boolean displayOptionHasToggled = state.hasDisplayOptionToggled();
            final boolean interactive = state.isInteractive();

            // Update the display of the graph's nodes
            for (int i = 0; i < graph.getVertexCount(); i++) {
                // Determine if we should display the current vertex based on its own k-truss attribute
                final int vxID = graph.getVertex(i);
                final int k = graph.getIntValue(vxKTrussAttr, vxID);
                // Only display K-Truss visuals if interactive is set
                final boolean displayCurrentVertex = ((k >= currentK) || (!interactive));

                // Update the relevant attributes which affect the display of the vertex
                if (dim) {
                    graph.setBooleanValue(vxDimmedAttr, vxID, !displayCurrentVertex);
                    if (displayOptionHasToggled) {
                        graph.setFloatValue(vxVisibilityAttr, vxID, 1.0F);
                    }
                } else {
                    graph.setFloatValue(vxVisibilityAttr, vxID, (displayCurrentVertex ? 1.0F : -1.0F));
                    if (displayOptionHasToggled) {
                        graph.setBooleanValue(vxDimmedAttr, vxID, false);
                    }
                }
            }

            // Update the display of the graph's transactions
            for (int i = 0; i < graph.getTransactionCount(); i++) {
                // Determine if we should display the current transaction based on its own k-truss attribute
                final int txID = graph.getTransaction(i);
                final int k = graph.getIntValue(txKTrussAttr, txID);
                // Only display K-Truss visuals if interactive is set
                final boolean displayCurrentTransaction = ((k >= currentK) || (!interactive));

                // Update the relevant attributes which affect the display of the transaction
                if (dim) {
                    graph.setBooleanValue(txDimmedAttr, txID, !displayCurrentTransaction);
                    if (displayOptionHasToggled) {
                        graph.setFloatValue(txVisibilityAttr, txID, 1.0F);
                    }
                } else {
                    graph.setFloatValue(txVisibilityAttr, txID, (displayCurrentTransaction ? 1.0F : -1.0F));
                    if (displayOptionHasToggled) {
                        graph.setBooleanValue(txDimmedAttr, txID, false);
                    }
                }
            }

            // If there was a display option toggle between dimming and hiding, report that this has been handled.
            if (displayOptionHasToggled) {
                state.displayOptionToggleHandled();
            }
        }
    }

    @Override
    public void componentResized(final ComponentEvent e) {
        nestedTrussPane.setSize(stepSlider.getWidth(), nestedTrussPane.getHeight());
        dp.setSize(stepSlider.getWidth(), dp.getHeight());
        dp.repaint();
    }

    @Override
    public void componentMoved(final ComponentEvent e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void componentShown(final ComponentEvent e) {
        nestedTrussPane.setSize(stepSlider.getWidth(), nestedTrussPane.getHeight());
        dp.setSize(stepSlider.getWidth(), dp.getHeight());
        dp.repaint();
    }

    @Override
    public void componentHidden(final ComponentEvent e) {
        throw new UnsupportedOperationException();
    }

    void writeProperties(final java.util.Properties p) {
        // Required for @ConvertAsProperties, intentionally left blank
    }

    void readProperties(final java.util.Properties p) {
        // Required for @ConvertAsProperties, intentionally left blank
    }

    @PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.MODIFY})
    public static class KTrussCalculatePlugin extends SimpleEditPlugin {

        final boolean isInteractiveButtonSelected;

        public KTrussCalculatePlugin(final boolean isInteractiveButtonSelected) {
            this.isInteractiveButtonSelected = isInteractiveButtonSelected;
        }

        @Override
        public String getName() {
            return "K-Truss: Calculate";
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
            KTruss.run(graph, new KTruss.KTrussPluginResultHandler(graph, isInteractiveButtonSelected));
        }

    }

    public void updateInteractiveButton(final boolean disableButton) {
        if (disableButton) {
            interactiveButton.setText(TOGGLE_DISABLED);
            interactiveButton.setSelected(false);
        } else {
            interactiveButton.setText(TOGGLE_ENABLED);
            interactiveButton.setSelected(false);
        }
    }
}
