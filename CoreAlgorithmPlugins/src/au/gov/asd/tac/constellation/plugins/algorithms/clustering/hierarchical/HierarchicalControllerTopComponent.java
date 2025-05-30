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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.hierarchical;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeEvent;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeListener;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.ClusteringConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.hierarchical.FastNewman.Group;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.hierarchical.HierarchicalState.ExcludedState;
import au.gov.asd.tac.constellation.plugins.algorithms.paths.DijkstraServices;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import javax.swing.ScrollPaneConstants;
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
 * Top component which controls a Community of Interest graph.
 */
@ConvertAsProperties(
        dtd = "-//au.gov.asd.tac.constellation.plugins.algorithms.clustering//HierarchicalController//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "HierarchicalControllerTopComponent",
        iconBase = "au/gov/asd/tac/constellation/plugins/algorithms/clustering/hierarchical/hierarchical.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(
        mode = "output",
        openAtStartup = false
)
@ActionID(
        category = "Views",
        id = "au.gov.asd.tac.constellation.plugins.algorithms.clustering.hierarchical.HierarchicalControllerTopComponent"
)
@ActionReferences({
    @ActionReference(path = "Menu/Tools/Cluster", position = 200)
})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_HierarchicalControllerAction",
        preferredID = "HierarchicalControllerTopComponent"
)
@Messages({
    "CTL_HierarchicalControllerAction=Hierarchical",
    "CTL_HierarchicalControllerTopComponent=Hierarchical",
    "HINT_HierarchicalControllerTopComponent=Use this window to view communities of interest in the graph"
})
public final class HierarchicalControllerTopComponent extends TopComponent implements LookupListener, GraphChangeListener {

    private static final String INFO_STRING = "%s clusters";
    private static final String TOGGLE_DISABLED = "Toggle Interactive: Disabled";
    private static final String TOGGLE_ENABLED = "Toggle Interactive: Enabled";

    private final Lookup.Result<GraphNode> result;
    private GraphNode graphNode;
    private Graph graph;
    private HierarchicalState state;
    private boolean isAdjusting;
    private boolean interactivityPermitted = true;

    private NestedHierarchicalDisplayPanel dp = null;

    /**
     * Construct a HierarchicalControllerTopComponent.
     */
    public HierarchicalControllerTopComponent() {
        initComponents();
        initNestedDisplay();
        setName(Bundle.CTL_HierarchicalControllerTopComponent());
        setToolTipText(Bundle.HINT_HierarchicalControllerTopComponent());

        this.putClientProperty(TopComponent.PROP_KEEP_PREFERRED_SIZE_WHEN_SLIDED_IN, Boolean.TRUE);

        isAdjusting = false;
        reclusterLabel.setVisible(false);

        result = Utilities.actionsGlobalContext().lookupResult(GraphNode.class);
        result.addLookupListener(this);
    }

    private void initNestedDisplay() {
        nestedDiagramScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        nestedDiagramScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        nestedDiagramScrollPane.setSize(nestedDiagramScrollPane.getPreferredSize());
        dp = new NestedHierarchicalDisplayPanel(this, nestedDiagramScrollPane);
        nestedDiagramScrollPane.setViewportView(dp);
        nestedDiagramScrollPane.setOpaque(true);
        nestedDiagramScrollPane.getVerticalScrollBar().addAdjustmentListener(e -> dp.repaint());
        nestedDiagramScrollPane.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(final ComponentEvent e) {
                dp.componentResized(null);
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

    public void updateSlider() {
        if (!isAdjusting) {
            infoLabel.setText(String.format(INFO_STRING, state.getCurrentNumOfClusters()));
            updateGraph();
            if (dp != null) {
                dp.updateColorsAndBar();
                dp.repaint();
            }
            nestedDiagramScrollPane.repaint();
            repaint();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        excludedElementsLabel = new javax.swing.JLabel();
        excludeSingleVerticesCheckBox = new javax.swing.JCheckBox();
        upButton = new javax.swing.JButton();
        downButton = new javax.swing.JButton();
        hiddenRadioButton = new javax.swing.JRadioButton();
        dimmedRadioButton = new javax.swing.JRadioButton();
        nestedDiagramScrollPane = new javax.swing.JScrollPane();
        infoLabel = new javax.swing.JLabel();
        returnToOptimumButton = new javax.swing.JButton();
        reclusterButton = new javax.swing.JButton();
        reclusterButton.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        interactiveButton = new javax.swing.JToggleButton();
        interactiveButton.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        colorClustersCheckBox = new javax.swing.JCheckBox();
        shortestPathsButton = new javax.swing.JButton();
        reclusterLabel = new javax.swing.JLabel();
        helpButton = new javax.swing.JButton();
        showRadioButton = new javax.swing.JRadioButton();

        setDisplayName(org.openide.util.NbBundle.getMessage(HierarchicalControllerTopComponent.class, "HierarchicalControllerTopComponent.displayName")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(excludedElementsLabel, org.openide.util.NbBundle.getMessage(HierarchicalControllerTopComponent.class, "HierarchicalControllerTopComponent.excludedElementsLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(excludeSingleVerticesCheckBox, org.openide.util.NbBundle.getMessage(HierarchicalControllerTopComponent.class, "HierarchicalControllerTopComponent.excludeSingleVerticesCheckBox.text")); // NOI18N
        excludeSingleVerticesCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                excludeSingleVerticesCheckBoxItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(upButton, org.openide.util.NbBundle.getMessage(HierarchicalControllerTopComponent.class, "HierarchicalControllerTopComponent.upButton.text")); // NOI18N
        upButton.setToolTipText(org.openide.util.NbBundle.getMessage(HierarchicalControllerTopComponent.class, "HierarchicalControllerTopComponent.upButton.toolTipText")); // NOI18N
        upButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(downButton, org.openide.util.NbBundle.getMessage(HierarchicalControllerTopComponent.class, "HierarchicalControllerTopComponent.downButton.text")); // NOI18N
        downButton.setToolTipText(org.openide.util.NbBundle.getMessage(HierarchicalControllerTopComponent.class, "HierarchicalControllerTopComponent.downButton.toolTipText")); // NOI18N
        downButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(hiddenRadioButton);
        hiddenRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(hiddenRadioButton, org.openide.util.NbBundle.getMessage(HierarchicalControllerTopComponent.class, "HierarchicalControllerTopComponent.hiddenRadioButton.text")); // NOI18N
        hiddenRadioButton.setToolTipText(org.openide.util.NbBundle.getMessage(HierarchicalControllerTopComponent.class, "HierarchicalControllerTopComponent.hiddenRadioButton.toolTipText")); // NOI18N
        hiddenRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hiddenRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(dimmedRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(dimmedRadioButton, org.openide.util.NbBundle.getMessage(HierarchicalControllerTopComponent.class, "HierarchicalControllerTopComponent.dimmedRadioButton.text")); // NOI18N
        dimmedRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dimmedRadioButtonActionPerformed(evt);
            }
        });

        nestedDiagramScrollPane.setPreferredSize(new java.awt.Dimension(387, 505));

        org.openide.awt.Mnemonics.setLocalizedText(infoLabel, org.openide.util.NbBundle.getMessage(HierarchicalControllerTopComponent.class, "HierarchicalControllerTopComponent.infoLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(returnToOptimumButton, org.openide.util.NbBundle.getMessage(HierarchicalControllerTopComponent.class, "HierarchicalControllerTopComponent.returnToOptimumButton.text")); // NOI18N
        returnToOptimumButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                returnToOptimumButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(reclusterButton, org.openide.util.NbBundle.getMessage(HierarchicalControllerTopComponent.class, "HierarchicalControllerTopComponent.reclusterButton.text")); // NOI18N
        reclusterButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        reclusterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reclusterButtonActionPerformed(evt);
            }
        });

        interactiveButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(interactiveButton, org.openide.util.NbBundle.getMessage(HierarchicalControllerTopComponent.class, "HierarchicalControllerTopComponent.interactiveButton.text")); // NOI18N
        interactiveButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        interactiveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                interactiveButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(colorClustersCheckBox, org.openide.util.NbBundle.getMessage(HierarchicalControllerTopComponent.class, "HierarchicalControllerTopComponent.colorClustersCheckBox.text")); // NOI18N
        colorClustersCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorClustersCheckBoxActionPerformed(evt);
            }
        });

        shortestPathsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/au/gov/asd/tac/constellation/plugins/algorithms/paths/shortestpaths.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(shortestPathsButton, org.openide.util.NbBundle.getMessage(HierarchicalControllerTopComponent.class, "HierarchicalControllerTopComponent.shortestPathsButton.text")); // NOI18N
        shortestPathsButton.setToolTipText(org.openide.util.NbBundle.getMessage(HierarchicalControllerTopComponent.class, "HierarchicalControllerTopComponent.shortestPathsButton.toolTipText")); // NOI18N
        shortestPathsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                shortestPathsButtonActionPerformed(evt);
            }
        });

        reclusterLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        reclusterLabel.setForeground(new java.awt.Color(242, 122, 61));
        org.openide.awt.Mnemonics.setLocalizedText(reclusterLabel, org.openide.util.NbBundle.getMessage(HierarchicalControllerTopComponent.class, "HierarchicalControllerTopComponent.reclusterLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(helpButton, org.openide.util.NbBundle.getMessage(HierarchicalControllerTopComponent.class, "HierarchicalControllerTopComponent.helpButton.text")); // NOI18N
        helpButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                helpButtonMousePressed(evt);
            }
        });

        buttonGroup1.add(showRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(showRadioButton, org.openide.util.NbBundle.getMessage(HierarchicalControllerTopComponent.class, "HierarchicalControllerTopComponent.showRadioButton.text")); // NOI18N
        showRadioButton.setActionCommand(org.openide.util.NbBundle.getMessage(HierarchicalControllerTopComponent.class, "HierarchicalControllerTopComponent.showRadioButton.actionCommand")); // NOI18N
        showRadioButton.setLabel(org.openide.util.NbBundle.getMessage(HierarchicalControllerTopComponent.class, "HierarchicalControllerTopComponent.showRadioButton.label")); // NOI18N
        showRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showRadioButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(nestedDiagramScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(reclusterButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(interactiveButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(infoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(reclusterLabel)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(excludedElementsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(hiddenRadioButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(dimmedRadioButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(showRadioButton)
                                        .addGap(18, 18, 18)
                                        .addComponent(excludeSingleVerticesCheckBox)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(colorClustersCheckBox)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 641, Short.MAX_VALUE)
                                .addComponent(helpButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(shortestPathsButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(returnToOptimumButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(downButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(upButton)))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(reclusterButton)
                    .addComponent(interactiveButton)
                    .addComponent(infoLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(reclusterLabel)
                        .addGap(2, 2, 2)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(excludedElementsLabel)
                            .addComponent(hiddenRadioButton)
                            .addComponent(dimmedRadioButton)
                            .addComponent(excludeSingleVerticesCheckBox)
                            .addComponent(colorClustersCheckBox)
                            .addComponent(showRadioButton)))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(shortestPathsButton)
                            .addComponent(helpButton))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(returnToOptimumButton)
                            .addComponent(downButton)
                            .addComponent(upButton))))
                .addGap(18, 18, 18)
                .addComponent(nestedDiagramScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 467, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void excludeSingleVerticesCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_excludeSingleVerticesCheckBoxItemStateChanged
        state.setExcludeSingleVertices(excludeSingleVerticesCheckBox.isSelected());
        updateGraph();
    }//GEN-LAST:event_excludeSingleVerticesCheckBoxItemStateChanged

    private void downButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downButtonActionPerformed

        if (state.getCurrentStep() > 0) {
            state.setCurrentStep(state.getCurrentStep() - 1);
            updateSlider();
        }
    }//GEN-LAST:event_downButtonActionPerformed

    private void upButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upButtonActionPerformed
        if (state.getCurrentStep() < state.getSteps()) {
            state.setCurrentStep(state.getCurrentStep() + 1);
            updateSlider();
        }
    }//GEN-LAST:event_upButtonActionPerformed

    private void hiddenRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hiddenRadioButtonActionPerformed
        state.setExcludedElementsState(HierarchicalState.ExcludedState.HIDDEN);
        updateGraph();
    }//GEN-LAST:event_hiddenRadioButtonActionPerformed

    private void dimmedRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dimmedRadioButtonActionPerformed
        state.setExcludedElementsState(HierarchicalState.ExcludedState.DIMMED);
        updateGraph();
    }//GEN-LAST:event_dimmedRadioButtonActionPerformed

    private void returnToOptimumButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_returnToOptimumButtonActionPerformed
        state.setCurrentStep(state.getOptimumStep());
        updateSlider();
    }//GEN-LAST:event_returnToOptimumButtonActionPerformed

    private void reclusterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reclusterButtonActionPerformed
        PluginExecution.withPlugin(new HierarchicalReclusterPlugin(interactiveButton.isSelected())).executeLater(graph);
    }//GEN-LAST:event_reclusterButtonActionPerformed

    private void interactiveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_interactiveButtonActionPerformed
        if (state != null) {
            updateInteractivity();
        }
    }//GEN-LAST:event_interactiveButtonActionPerformed

    private void colorClustersCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorClustersCheckBoxActionPerformed
        setColoring();
    }//GEN-LAST:event_colorClustersCheckBoxActionPerformed

    private void shortestPathsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shortestPathsButtonActionPerformed
        PluginExecution.withPlugin(new HierarchicalShortestPathsPlugin(state)).executeLater(graph);
    }//GEN-LAST:event_shortestPathsButtonActionPerformed

    private void helpButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_helpButtonMousePressed
        final HelpCtx help = new HelpCtx("au.gov.asd.tac.constellation.plugins.algorithms.clustering.hierarchical.Hierarchical");
        help.display();
    }//GEN-LAST:event_helpButtonMousePressed

    private void showRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showRadioButtonActionPerformed
        state.setExcludedElementsState(HierarchicalState.ExcludedState.SHOW);
        updateGraph();
    }//GEN-LAST:event_showRadioButtonActionPerformed

    private void setColoring() {
        if (graphNode == null) {
            return;
        }

        final boolean wasColored = state.isColored();
        state.setColored(!state.isColored());

        final ColorClusters colorPlugin = new ColorClusters(!wasColored);
        PluginExecution.withPlugin(colorPlugin).interactively(true).executeLater(graph);
    }

    private void updateInteractivity() {
        state.setInteractive(!state.isInteractive());
        state.setColored(this.colorClustersCheckBox.isSelected());
        if (!state.isInteractive()) {
            interactiveButton.setText(TOGGLE_DISABLED);
            nestedDiagramScrollPane.setViewportView(null);
            nestedDiagramScrollPane.repaint();
            if (state.isColored()) {
                final ColorClusters uncolor = new ColorClusters(false);
                PluginExecution.withPlugin(uncolor).interactively(true).executeLater(graph);
            }
        } else {
            interactiveButton.setText(TOGGLE_ENABLED);
            nestedDiagramScrollPane.setViewportView(dp);
            nestedDiagramScrollPane.repaint();
            if (state.isColored()) {
                final ColorClusters color = new ColorClusters(true);
                PluginExecution.withPlugin(color).interactively(true).executeLater(graph);
            }
        }
        setGroups(true);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBox colorClustersCheckBox;
    private javax.swing.JRadioButton dimmedRadioButton;
    private javax.swing.JButton downButton;
    private javax.swing.JCheckBox excludeSingleVerticesCheckBox;
    private javax.swing.JLabel excludedElementsLabel;
    private javax.swing.JButton helpButton;
    private javax.swing.JRadioButton hiddenRadioButton;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JToggleButton interactiveButton;
    private javax.swing.JScrollPane nestedDiagramScrollPane;
    private javax.swing.JButton reclusterButton;
    private javax.swing.JLabel reclusterLabel;
    private javax.swing.JButton returnToOptimumButton;
    private javax.swing.JButton shortestPathsButton;
    private javax.swing.JRadioButton showRadioButton;
    private javax.swing.JButton upButton;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        result.addLookupListener(this);
        resultChanged(null);
    }

    @Override
    public void componentClosed() {
        result.removeLookupListener(this);
        if (state != null && state.isInteractive()) {
            updateInteractivity();
        }
        setNode(null);
    }

    public void writeProperties(final java.util.Properties p) {
        //Unused Method
    }

    public void readProperties(final java.util.Properties p) {
        //Unused Method
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

    private void setGroups(final boolean doUpdate) {
        interactiveButton.setEnabled(interactivityPermitted);
        if (state != null && state.isInteractive()) {
            final Component[] children = getComponents();
            for (final Component c : children) {
                if (!c.equals(reclusterButton) || c.equals(interactiveButton) || c.equals(reclusterLabel)) {
                    c.setEnabled(true);
                }
            }

            isAdjusting = true;
            infoLabel.setText(String.format(INFO_STRING, state.getCurrentNumOfClusters()));
            excludeSingleVerticesCheckBox.setSelected(state.isExcludeSingleVertices());

            final ExcludedState exclusion = state.getExcludedElementsState();
            dimmedRadioButton.setSelected(exclusion == ExcludedState.DIMMED);
            hiddenRadioButton.setSelected(exclusion == ExcludedState.HIDDEN);
            showRadioButton.setSelected(exclusion == ExcludedState.SHOW);

            dp.setState(state);
            revalidateParents(dp);

            if (doUpdate) {
                updateGraph();
            }

            isAdjusting = false;
        } else {
            final Component[] children = getComponents();
            for (final Component c : children) {
                if (!(c.equals(reclusterButton) || c.equals(interactiveButton)) || !c.equals(reclusterLabel)) {
                    c.setEnabled(false);
                }
            }
            if (state == null) {
                reclusterButton.setEnabled(true);
                reclusterLabel.setEnabled(true);
                interactiveButton.setEnabled(false);
                interactiveButton.setText(TOGGLE_DISABLED);
            }

            if (dp != null) {
                dp.setState(null);
                revalidateParents(dp);
            }
            if (state != null && doUpdate) {
                updateGraph();
            }
            interactiveButton.setText((state != null && state.isInteractive()) ? TOGGLE_ENABLED : TOGGLE_DISABLED);
        }
        interactiveButton.setEnabled(state != null);
        interactiveButton.setSelected(false);
    }

    private void updateGraph() {
        if (graphNode == null) {
            return;
        }

        final Update update = new Update(state);
        final Future<?> f = PluginExecution.withPlugin(update).interactively(true).executeLater(graph);
        if (state.isColored() && state.isInteractive()) {
            final ColorClusters color = new ColorClusters(true);
            PluginExecution.withPlugin(color).interactively(true).waitingFor(f).executeLater(graph);
        }
        if (state.isColored() && !state.isInteractive()) {
            final ColorClusters color = new ColorClusters(false);
            PluginExecution.withPlugin(color).interactively(true).waitingFor(f).executeLater(graph);
        }
    }

    /**
     * We need to listen to the graph to know when a new HierarchicalState has been set.
     *
     * @param evt The change event.
     */
    @Override
    public void graphChanged(final GraphChangeEvent evt) {
        long smc;
        final long mc;

        try (final ReadableGraph rg = graph.getReadableGraph()) {
            // Retrieve the COI state attribute, attribute mod counter, and structural mod counter from the graph
            final int stateAttr = ClusteringConcept.MetaAttribute.HIERARCHICAL_CLUSTERING_STATE.get(rg);
            smc = rg.getStructureModificationCounter();
            mc = stateAttr != Graph.NOT_FOUND ? rg.getValueModificationCounter(stateAttr) : Graph.NOT_FOUND;

            // If the COI state on the controller is null, or has a different modcount to the state on the graph, update this controller's state.
            if (state == null || mc != state.getModificationCounter()) {
                state = stateAttr != Graph.NOT_FOUND ? (HierarchicalState) rg.getObjectValue(stateAttr, 0) : null;
                setGroups(true);
            }
        }

        // Update the controller state's modcount, and make the recluster button active if the srtuctual mod count does not match the graph.
        if (state != null) {
            state.setModificationCounter(mc);
            reclusterButton.setEnabled(smc != state.getStrucModificationCount());
            reclusterLabel.setVisible(smc != state.getStrucModificationCount());
            reclusterLabel.setEnabled(smc != state.getStrucModificationCount());
        }
        // Interactive button should only be available if clustering has been done
        // and a state exists for the current graph
        // state exists - enable interactive,
        // state exists and graph not changed - enable interactive,
        // graph changed - disable interactive,
        // graph changed and state exists - disable interactive
        interactiveButton.setEnabled(state != null && smc == state.getStrucModificationCount());
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
            graphNode = node;
            graph = graphNode.getGraph();

            try (final ReadableGraph rg = graph.getReadableGraph()) {
                final int stateAttr = ClusteringConcept.MetaAttribute.HIERARCHICAL_CLUSTERING_STATE.get(rg);
                state = stateAttr != Graph.NOT_FOUND ? (HierarchicalState) rg.getObjectValue(stateAttr, 0) : null;

                if (rg.getSchema() != null && !(rg.getSchema().getFactory() instanceof VisualSchemaFactory)) {
                    interactiveButton.setSelected(false);
                    interactiveButton.setText(TOGGLE_ENABLED);
                    interactivityPermitted = false;
                } else if (state != null) {
                    interactiveButton.setText(state.isInteractive() ? TOGGLE_ENABLED : TOGGLE_DISABLED);
                    interactiveButton.setSelected(state.isInteractive());
                    colorClustersCheckBox.setSelected(state.isColored());
                    interactivityPermitted = true;
                } else {
                    interactiveButton.setText(TOGGLE_DISABLED);
                    interactiveButton.setSelected(true);
                    colorClustersCheckBox.setSelected(true);
                    interactivityPermitted = true;
                }
            }

            graph.addGraphChangeListener(this);
        } else {
            graphNode = null;
            graph = null;
            state = null;
        }

        setGroups(false);
    }

    @PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.MODIFY})
    public static final class ColorClusters extends SimpleEditPlugin {

        private final boolean setColors;

        public ColorClusters(final boolean setColors) {
            this.setColors = setColors;
        }

        @Override
        public String getName() {
            return "Hierarchical: Set/Remove Overlay Colors";
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
            final int vxColorRef = VisualConcept.GraphAttribute.NODE_COLOR_REFERENCE.ensure(graph);
            final int txColorRef = VisualConcept.GraphAttribute.TRANSACTION_COLOR_REFERENCE.ensure(graph);
            final String vxColorAttrName = setColors ? ClusteringConcept.VertexAttribute.HIERARCHICAL_COLOR.getName() : null;
            final String txColorAttrName = setColors ? ClusteringConcept.TransactionAttribute.HIERARCHICAL_COLOR.getName() : null;
            graph.setStringValue(vxColorRef, 0, vxColorAttrName);
            graph.setStringValue(txColorRef, 0, txColorAttrName);
        }
    }

    @PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.MODIFY})
    public static final class Update extends SimpleEditPlugin {

        private final HierarchicalState state;

        public Update(final HierarchicalState state) {
            this.state = state;
        }

        @Override
        public String getName() {
            return "Hierarchical: Update";
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
            state.setRedrawCount(state.getRedrawCount() + 1);

            final int vxOverlayColorAttr = ClusteringConcept.VertexAttribute.HIERARCHICAL_COLOR.ensure(graph);
            final int txOverlayColorAttr = ClusteringConcept.TransactionAttribute.HIERARCHICAL_COLOR.ensure(graph);
            final int vertexClusterAttribute = ClusteringConcept.VertexAttribute.HIERARCHICAL_CLUSTER.ensure(graph);
            final int vertexDimmedAttribute = VisualConcept.VertexAttribute.DIMMED.ensure(graph);
            final int vertexVisibilityAttribute = VisualConcept.VertexAttribute.VISIBILITY.ensure(graph);
            final int transactionDimmedAttribute = VisualConcept.TransactionAttribute.DIMMED.ensure(graph);
            final int transactionVisibilityAttribute = VisualConcept.TransactionAttribute.VISIBILITY.ensure(graph);

            int nextCluster = 0;
            final int vertexCount = graph.getVertexCount();
            for (int pos = 0; pos < vertexCount; pos++) {
                final int vertex = graph.getVertex(pos);
                Group group = state.getGroups()[pos];
                if (group == null) {
                    continue;
                }
                // When excluding single vertices
                if (state.isExcludeSingleVertices() && group.getSingleStep() > state.getCurrentStep()) {
                    graph.setIntValue(vertexClusterAttribute, vertex, -1);
                    if (state.isInteractive()) {
                        final boolean isDimmed = state.getExcludedElementsState() == ExcludedState.DIMMED;
                        final float visibiltyValue = state.getExcludedElementsState() == ExcludedState.HIDDEN ? -2.0F : 2.0F;

                        graph.setBooleanValue(vertexDimmedAttribute, vertex, isDimmed);
                        graph.setFloatValue(vertexVisibilityAttribute, vertex, visibiltyValue);

                    } else {
                        graph.setBooleanValue(vertexDimmedAttribute, vertex, false);
                        graph.setFloatValue(vertexVisibilityAttribute, vertex, 2.0F);
                    }
                } else {
                    // when keeping all vertices, do not dim, and show all.
                    // assign all nodes to a group/cluster
                    while (group.getMergeStep() <= state.getCurrentStep()) {
                        group = group.getParent();
                    }
                    graph.setFloatValue(vertexVisibilityAttribute, vertex, 2.0F);
                    graph.setBooleanValue(vertexDimmedAttribute, vertex, false);
                    graph.setObjectValue(vxOverlayColorAttr, vertex, group.getColor());

                    if (state.getClusterSeenBefore()[group.getVertex()] < state.getRedrawCount()) {
                        state.getClusterSeenBefore()[group.getVertex()] = state.getRedrawCount();
                        state.getClusterNumbers()[group.getVertex()] = nextCluster++;
                    }
                    graph.setIntValue(vertexClusterAttribute, vertex, state.getClusterNumbers()[group.getVertex()]);
                }
            }

            final int linkCount = graph.getLinkCount();
            for (int p = 0; p < linkCount; p++) {
                final int link = graph.getLink(p);
                final int highVertex = graph.getLinkHighVertex(link);
                final int lowVertex = graph.getLinkLowVertex(link);
                final ConstellationColor highVertexColor = graph.getObjectValue(vxOverlayColorAttr, highVertex);
                final ConstellationColor lowVertexColor = graph.getObjectValue(vxOverlayColorAttr, lowVertex);
                final boolean highDimmed = graph.getBooleanValue(vertexDimmedAttribute, highVertex);
                final boolean lowDimmed = graph.getBooleanValue(vertexDimmedAttribute, lowVertex);

                if (state.isInteractive()) {
                    // if transaction is between a cluster, do not dim or hide
                    if (highVertexColor == lowVertexColor && !highDimmed && !lowDimmed) {
                        final int transactionCount = graph.getLinkTransactionCount(link);
                        for (int transactionPosition = 0; transactionPosition < transactionCount; transactionPosition++) {
                            final int transaction = graph.getLinkTransaction(link, transactionPosition);
                            graph.setObjectValue(txOverlayColorAttr, transaction, highVertexColor);
                            graph.setBooleanValue(transactionDimmedAttribute, transaction, false);
                            graph.setFloatValue(transactionVisibilityAttribute, transaction, 2.0F);
                        }
                    } else { // dim or hide transaction depending on state selected.
                        final int transactionCount = graph.getLinkTransactionCount(link);
                        final boolean isDimmed = state.getExcludedElementsState() != ExcludedState.SHOW;
                        final float visibiltyValue = state.getExcludedElementsState() == ExcludedState.HIDDEN ? -2.0F : 2.0F;

                        for (int transactionPosition = 0; transactionPosition < transactionCount; transactionPosition++) {
                            final int transaction = graph.getLinkTransaction(link, transactionPosition);

                            graph.setBooleanValue(transactionDimmedAttribute, transaction, isDimmed);
                            graph.setFloatValue(transactionVisibilityAttribute, transaction, visibiltyValue);
                        }
                    }
                } else { // when not interactive, don't dim or hide transactions
                    final int transactionCount = graph.getLinkTransactionCount(link);
                    for (int transactionPosition = 0; transactionPosition < transactionCount; transactionPosition++) {
                        final int transaction = graph.getLinkTransaction(link, transactionPosition);
                        graph.setObjectValue(txOverlayColorAttr, transaction, highVertexColor);
                        graph.setBooleanValue(transactionDimmedAttribute, transaction, false);
                        graph.setFloatValue(transactionVisibilityAttribute, transaction, 2.0F);
                    }
                }
            }
        }
    }

    /**
     * Recluster using hierarchical algorithm fast newman
     */
    @PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.MODIFY})
    public static class HierarchicalReclusterPlugin extends SimpleEditPlugin {

        final boolean interactiveButtonSelected;

        public HierarchicalReclusterPlugin(final boolean interactiveButtonSelected) {
            this.interactiveButtonSelected = interactiveButtonSelected;
        }

        @Override
        public String getName() {
            return "Hierarchical: Recluster";
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
            HierarchicalClusteringServices.fastNewmanWithPendantsClusteredFinal(graph, interaction, interactiveButtonSelected);
        }

    }

    /**
     * Finds the shortest paths between clusters
     */
    @PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.MODIFY})
    public static class HierarchicalShortestPathsPlugin extends SimpleEditPlugin {

        final HierarchicalState state;

        public HierarchicalShortestPathsPlugin(final HierarchicalState state) {
            this.state = state;
        }

        @Override
        public String getName() {
            return "Hierarchical: Shortest Paths Between Clusters";
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
            final Set<Integer> verticesToPath = new HashSet<>();
            for (int pos = 0; pos < graph.getVertexCount(); pos++) {
                Group group = state.getGroups()[pos];
                if (group == null) {
                    continue;
                }
                while (group.getMergeStep() <= state.getCurrentStep()) {
                    group = group.getParent();
                }
                verticesToPath.add(group.getVertex());
            }
            final List<Integer> verticesToPathList = new ArrayList<>(verticesToPath);
            final DijkstraServices ds = new DijkstraServices(graph, verticesToPathList, false);
            ds.queryPaths(true);
        }
    }
}
