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
package au.gov.asd.tac.constellation.views.histogram;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersDialog;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersSwingDialog;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.views.histogram.formats.BinFormatter;
import java.awt.event.ActionEvent;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import org.openide.util.HelpCtx;

/**
 * The panel that displays all the UI controls available to the user to
 * configure the histogram display.
 *
 * @author sirius
 */
public class HistogramControls extends JPanel {

    public static final Map<BinFormatter, PluginParameters> CURRENT_PARAMETER_IDS = new HashMap<>();

    private final HistogramTopComponent topComponent;
    private boolean isAdjusting = false;
    private HistogramState currentHistogramState = null;
    private final DefaultComboBoxModel<String> attributeChoiceModel;
    private final DefaultComboBoxModel<BinFormatter> binFormatterChoiceModel;

    private final JPopupMenu actionsMenu = new JPopupMenu();

    private final EnumMap<GraphElementType, JToggleButton> etToggles;

    public HistogramControls(HistogramTopComponent topComponent) {

        this.topComponent = topComponent;

        initComponents();

        etToggles = new EnumMap<>(GraphElementType.class);
        etToggles.put(GraphElementType.VERTEX, vertexToggle);
        etToggles.put(GraphElementType.TRANSACTION, transactionToggle);
        etToggles.put(GraphElementType.EDGE, edgeToggle);
        etToggles.put(GraphElementType.LINK, linkToggle);

        vertexToggle.setText(GraphElementType.VERTEX.getShortLabel());
        vertexToggle.setIcon(UserInterfaceIconProvider.NODES.buildIcon(16));
        vertexToggle.setToolTipText(GraphElementType.VERTEX.getLabel());
        transactionToggle.setText(GraphElementType.TRANSACTION.getShortLabel());
        transactionToggle.setIcon(UserInterfaceIconProvider.TRANSACTIONS.buildIcon(16));
        transactionToggle.setToolTipText(GraphElementType.TRANSACTION.getLabel());
        edgeToggle.setText(GraphElementType.EDGE.getShortLabel());
        edgeToggle.setIcon(UserInterfaceIconProvider.EDGES.buildIcon(16));
        edgeToggle.setToolTipText(GraphElementType.EDGE.getLabel());
        linkToggle.setText(GraphElementType.LINK.getShortLabel());
        linkToggle.setIcon(UserInterfaceIconProvider.LINKS.buildIcon(16));
        linkToggle.setToolTipText(GraphElementType.LINK.getLabel());

        attributeChoiceModel = new DefaultComboBoxModel<>();
        attributeChoice.setModel(attributeChoiceModel);

        binFormatterChoiceModel = new DefaultComboBoxModel<>();
        binFormatterCombo.setModel(binFormatterChoiceModel);

        for (BinComparator binComparator : BinComparator.values()) {
            if (binComparator.isAscending()) {
                sortChoice.addItem(binComparator);
            }
        }

        for (BinSelectionMode selectionMode : BinSelectionMode.values()) {
            selectionModeChoice.addItem(selectionMode);
        }

        final JMenuItem saveBinsToGraphMenuItem = new JMenuItem("Save Bins to Graph");
        saveBinsToGraphMenuItem.addActionListener(e -> saveBinsToGraph());
        actionsMenu.add(saveBinsToGraphMenuItem);

        final JMenuItem saveBinsToClipboardMenuItem = new JMenuItem("Save Bins to Clipboard");
        saveBinsToClipboardMenuItem.addActionListener((final ActionEvent e) -> topComponent.saveBinsToClipboard());
        actionsMenu.add(saveBinsToClipboardMenuItem);

        final JMenuItem decreaseHeightBarMenuItem = new JMenuItem("Decrease Height of Each Bin");
        decreaseHeightBarMenuItem.addActionListener((final ActionEvent e) -> topComponent.modifyBinHeight(-1));
        actionsMenu.add(decreaseHeightBarMenuItem);

        final JMenuItem increaseHeightBarMenuItem = new JMenuItem("Increase Height of Each Bin");
        increaseHeightBarMenuItem.addActionListener((final ActionEvent e) -> topComponent.modifyBinHeight(1));
        actionsMenu.add(increaseHeightBarMenuItem);

        setHistogramState(null, null);
    }

    public final void setHistogramState(HistogramState histogramState, Map<String, BinCreator> attributes) {

        isAdjusting = true;

        if (histogramState != currentHistogramState) {

            if (histogramState == null) {

                vertexToggle.setSelected(true);
                etToggles.values().stream().forEach(toggle -> toggle.setEnabled(false));

                attributeTypeChoice.setSelectedIndex(0);
                attributeTypeChoice.setEnabled(false);

                attributeChoiceModel.removeAllElements();
                attributeChoiceModel.addElement("");
                attributeChoice.setSelectedIndex(0);
                attributeChoice.setEnabled(false);

                binFormatterChoiceModel.removeAllElements();
                binFormatterChoiceModel.addElement(BinFormatter.DEFAULT_BIN_FORMATTER);
                binFormatterCombo.setSelectedIndex(0);
                binFormatterCombo.setEnabled(false);

                sortChoice.setSelectedIndex(0);
                sortChoice.setEnabled(false);

                descendingButton.setSelected(false);
                descendingButton.setEnabled(false);

                selectionModeChoice.setSelectedIndex(0);
                selectionModeChoice.setEnabled(false);

                selectButton.setEnabled(false);

                filterSelectionButton.setEnabled(false);
                clearFilterButton.setEnabled(false);

            } else {

                if (currentHistogramState == null) {
                    etToggles.values().stream().forEach(toggle -> toggle.setEnabled(true));
                    attributeTypeChoice.setEnabled(true);
                    attributeChoice.setEnabled(true);
                    binFormatterCombo.setEnabled(true);
                    sortChoice.setEnabled(true);
                    descendingButton.setEnabled(true);
                    selectionModeChoice.setEnabled(true);
                }

                etToggles.get(histogramState.getElementType()).setSelected(true);

                attributeTypeChoice.removeAllItems();
                for (AttributeType attributeType : AttributeType.values()) {
                    if (attributeType.appliesToElementType(histogramState.getElementType())) {
                        attributeTypeChoice.addItem(attributeType);
                    }
                }
                attributeTypeChoice.setSelectedItem(histogramState.getAttributeType());

                attributeChoiceModel.removeAllElements();
                attributes.keySet().stream().forEach(attribute -> attributeChoiceModel.addElement(attribute));
                attributeChoice.setSelectedItem(histogramState.getAttribute());

                binFormatterChoiceModel.removeAllElements();
                if (histogramState.getAttribute() != null) {
                    Bin checkBin = attributes.get(histogramState.getAttribute()).getBin();
                    if (checkBin != null) {
                        checkBin = checkBin.create();
                        final ReadableGraph rg = topComponent.currentGraph.getReadableGraph();
                        try {
                            if (histogramState.getAttributeType().getBinCreatorsGraphElementType() == null) {
                                checkBin.init(rg, rg.getAttribute(histogramState.getElementType(), histogramState.getAttribute()));
                            } else {
                                checkBin.init(rg, rg.getAttribute(histogramState.getAttributeType().getBinCreatorsGraphElementType(), histogramState.getAttribute()));
                            }
                        } finally {
                            rg.release();
                        }
                    }
                    BinFormatter.getFormatters(checkBin).stream().forEach(formatter -> binFormatterChoiceModel.addElement(formatter));
                    binFormatterCombo.setSelectedItem(histogramState.getBinFormatter());
                }

                if (histogramState.getBinComparator().isAscending()) {
                    descendingButton.setSelected(false);
                    sortChoice.setSelectedItem(histogramState.getBinComparator());
                } else {
                    descendingButton.setSelected(true);
                    sortChoice.setSelectedItem(histogramState.getBinComparator().getReverse());
                }

                selectionModeChoice.setSelectedItem(histogramState.getBinSelectionMode());

                selectButton.setEnabled(histogramState.getBinSelectionMode() != BinSelectionMode.FREE_SELECTION);

                filterSelectionButton.setEnabled(true);
                clearFilterButton.setEnabled(histogramState.getFilter(histogramState.getElementType()) != null);
            }

            currentHistogramState = histogramState;

        } else {

            attributeChoiceModel.removeAllElements();
            if (attributes != null) {
                attributes.keySet().stream().forEach(attribute -> attributeChoiceModel.addElement(attribute));
                if (currentHistogramState != null) {
                    attributeChoice.setSelectedItem(currentHistogramState.getAttribute());
                } else {
                    attributeChoice.setSelectedIndex(0);
                }
            } else {
                attributeChoiceModel.addElement("");
                attributeChoice.setSelectedIndex(0);
            }
        }

        isAdjusting = false;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sortOrderGroup = new javax.swing.ButtonGroup();
        graphElementGroup = new javax.swing.ButtonGroup();
        uiPanel = new javax.swing.JPanel();
        propertyLabel = new javax.swing.JLabel();
        attributeChoice = new javax.swing.JComboBox<>();
        sortChoice = new javax.swing.JComboBox<>();
        sortLabel = new javax.swing.JLabel();
        descendingButton = new javax.swing.JToggleButton();
        selectionModeChoice = new javax.swing.JComboBox<>();
        selectionModeLabel = new javax.swing.JLabel();
        selectButton = new javax.swing.JButton();
        categoryLabel = new javax.swing.JLabel();
        attributeTypeChoice = new javax.swing.JComboBox<>();
        graphElementLabel = new javax.swing.JLabel();
        filterLabel = new javax.swing.JLabel();
        filterSelectionButton = new javax.swing.JButton();
        clearFilterButton = new javax.swing.JButton();
        binFormatterCombo = new javax.swing.JComboBox<>();
        actionsButton = new javax.swing.JButton();
        vertexToggle = new javax.swing.JToggleButton();
        transactionToggle = new javax.swing.JToggleButton();
        edgeToggle = new javax.swing.JToggleButton();
        linkToggle = new javax.swing.JToggleButton();
        helpButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        propertyLabel.setText(org.openide.util.NbBundle.getMessage(HistogramControls.class, "HistogramControls.propertyLabel.text")); // NOI18N

        attributeChoice.setMaximumRowCount(16);
        attributeChoice.setEnabled(false);
        attributeChoice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attributeChoiceActionPerformed(evt);
            }
        });

        sortChoice.setEnabled(false);
        sortChoice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sortChoiceActionPerformed(evt);
            }
        });

        sortLabel.setText(org.openide.util.NbBundle.getMessage(HistogramControls.class, "HistogramControls.sortLabel.text")); // NOI18N

        descendingButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/au/gov/asd/tac/constellation/views/histogram/resources/down.png"))); // NOI18N
        descendingButton.setText(org.openide.util.NbBundle.getMessage(HistogramControls.class, "HistogramControls.descendingButton.text")); // NOI18N
        descendingButton.setToolTipText(org.openide.util.NbBundle.getMessage(HistogramControls.class, "HistogramControls.descendingButton.toolTipText")); // NOI18N
        descendingButton.setEnabled(false);
        descendingButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/au/gov/asd/tac/constellation/views/histogram/resources/up.png"))); // NOI18N
        descendingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                descendingButtonActionPerformed(evt);
            }
        });

        selectionModeChoice.setEnabled(false);
        selectionModeChoice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectionModeChoiceActionPerformed(evt);
            }
        });

        selectionModeLabel.setText(org.openide.util.NbBundle.getMessage(HistogramControls.class, "HistogramControls.selectionModeLabel.text")); // NOI18N

        selectButton.setText(org.openide.util.NbBundle.getMessage(HistogramControls.class, "HistogramControls.selectButton.text")); // NOI18N
        selectButton.setEnabled(false);
        selectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectButtonActionPerformed(evt);
            }
        });

        categoryLabel.setText(org.openide.util.NbBundle.getMessage(HistogramControls.class, "HistogramControls.categoryLabel.text")); // NOI18N

        attributeTypeChoice.setEnabled(false);
        attributeTypeChoice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attributeTypeChoiceActionPerformed(evt);
            }
        });

        graphElementLabel.setText(org.openide.util.NbBundle.getMessage(HistogramControls.class, "HistogramControls.graphElementLabel.text")); // NOI18N

        filterLabel.setText(org.openide.util.NbBundle.getMessage(HistogramControls.class, "HistogramControls.filterLabel.text")); // NOI18N

        filterSelectionButton.setText(org.openide.util.NbBundle.getMessage(HistogramControls.class, "HistogramControls.filterSelectionButton.text")); // NOI18N
        filterSelectionButton.setEnabled(false);
        filterSelectionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterSelectionButtonActionPerformed(evt);
            }
        });

        clearFilterButton.setText(org.openide.util.NbBundle.getMessage(HistogramControls.class, "HistogramControls.clearFilterButton.text")); // NOI18N
        clearFilterButton.setEnabled(false);
        clearFilterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearFilterButtonActionPerformed(evt);
            }
        });

        binFormatterCombo.setEnabled(false);
        binFormatterCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                binFormatterComboActionPerformed(evt);
            }
        });

        actionsButton.setText(org.openide.util.NbBundle.getMessage(HistogramControls.class, "HistogramControls.actionsButton.text")); // NOI18N
        actionsButton.setActionCommand(org.openide.util.NbBundle.getMessage(HistogramControls.class, "HistogramControls.actionsButton.actionCommand")); // NOI18N
        actionsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                actionsButtonMousePressed(evt);
            }
        });

        graphElementGroup.add(vertexToggle);
        vertexToggle.setText(org.openide.util.NbBundle.getMessage(HistogramControls.class, "HistogramControls.vertexToggle.text")); // NOI18N
        vertexToggle.setEnabled(false);
        vertexToggle.setMargin(new java.awt.Insets(2, 2, 2, 2));
        vertexToggle.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                vertexToggleStateChanged(evt);
            }
        });

        graphElementGroup.add(transactionToggle);
        transactionToggle.setText(org.openide.util.NbBundle.getMessage(HistogramControls.class, "HistogramControls.transactionToggle.text")); // NOI18N
        transactionToggle.setEnabled(false);
        transactionToggle.setMargin(new java.awt.Insets(2, 2, 2, 2));
        transactionToggle.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                transactionToggleStateChanged(evt);
            }
        });

        graphElementGroup.add(edgeToggle);
        edgeToggle.setText(org.openide.util.NbBundle.getMessage(HistogramControls.class, "HistogramControls.edgeToggle.text")); // NOI18N
        edgeToggle.setEnabled(false);
        edgeToggle.setMargin(new java.awt.Insets(2, 2, 2, 2));
        edgeToggle.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                edgeToggleStateChanged(evt);
            }
        });

        graphElementGroup.add(linkToggle);
        linkToggle.setText(org.openide.util.NbBundle.getMessage(HistogramControls.class, "HistogramControls.linkToggle.text")); // NOI18N
        linkToggle.setEnabled(false);
        linkToggle.setMargin(new java.awt.Insets(2, 2, 2, 2));
        linkToggle.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                linkToggleStateChanged(evt);
            }
        });

        helpButton.setText(org.openide.util.NbBundle.getMessage(HistogramControls.class, "HistogramControls.helpButton.text")); // NOI18N
        helpButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                helpButtonMousePressed(evt);
            }
        });

        javax.swing.GroupLayout uiPanelLayout = new javax.swing.GroupLayout(uiPanel);
        uiPanel.setLayout(uiPanelLayout);
        uiPanelLayout.setHorizontalGroup(
            uiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(uiPanelLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(uiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(uiPanelLayout.createSequentialGroup()
                        .addGroup(uiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(sortLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(propertyLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(categoryLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(graphElementLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(uiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(uiPanelLayout.createSequentialGroup()
                                .addComponent(attributeChoice, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(10, 10, 10)
                                .addComponent(binFormatterCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(uiPanelLayout.createSequentialGroup()
                                .addComponent(sortChoice, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(10, 10, 10)
                                .addComponent(descendingButton, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(attributeTypeChoice, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, uiPanelLayout.createSequentialGroup()
                                .addComponent(vertexToggle)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(transactionToggle)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(edgeToggle)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(linkToggle)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(uiPanelLayout.createSequentialGroup()
                        .addGroup(uiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(filterLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(selectionModeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 82, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(uiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(uiPanelLayout.createSequentialGroup()
                                .addComponent(selectionModeChoice, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(10, 10, 10)
                                .addComponent(selectButton, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(uiPanelLayout.createSequentialGroup()
                                .addComponent(filterSelectionButton)
                                .addGap(10, 10, 10)
                                .addComponent(clearFilterButton, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 22, Short.MAX_VALUE)
                                .addComponent(helpButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(actionsButton)))))
                .addGap(5, 5, 5))
        );
        uiPanelLayout.setVerticalGroup(
            uiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(uiPanelLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(uiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(graphElementLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(vertexToggle)
                    .addComponent(transactionToggle)
                    .addComponent(edgeToggle)
                    .addComponent(linkToggle))
                .addGap(10, 10, 10)
                .addGroup(uiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(categoryLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(attributeTypeChoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(uiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(propertyLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(attributeChoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(binFormatterCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(uiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sortLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sortChoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(descendingButton, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(uiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(selectionModeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectionModeChoice, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectButton))
                .addGap(10, 10, 10)
                .addGroup(uiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(filterLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(filterSelectionButton)
                    .addComponent(clearFilterButton)
                    .addGroup(uiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(actionsButton)
                        .addComponent(helpButton))))
        );

        add(uiPanel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void clearFilterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearFilterButtonActionPerformed
        if (!isAdjusting) {
            topComponent.clearFilter();
        }
    }//GEN-LAST:event_clearFilterButtonActionPerformed

    private void filterSelectionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterSelectionButtonActionPerformed
        if (!isAdjusting) {
            topComponent.filterOnSelection();
        }
    }//GEN-LAST:event_filterSelectionButtonActionPerformed

    private void attributeTypeChoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attributeTypeChoiceActionPerformed
        if (!isAdjusting) {
            topComponent.setAttributeType((AttributeType) attributeTypeChoice.getSelectedItem());
        }
    }//GEN-LAST:event_attributeTypeChoiceActionPerformed

    private void selectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectButtonActionPerformed
        if (!isAdjusting) {
            currentHistogramState.getBinSelectionMode().select(topComponent);
        }
    }//GEN-LAST:event_selectButtonActionPerformed

    private void selectionModeChoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectionModeChoiceActionPerformed
        if (!isAdjusting) {
            topComponent.setBinSelectionMode((BinSelectionMode) selectionModeChoice.getSelectedItem());
        }
    }//GEN-LAST:event_selectionModeChoiceActionPerformed

    private void descendingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_descendingButtonActionPerformed
        if (!isAdjusting) {
            updateBinComparator();
        }
    }//GEN-LAST:event_descendingButtonActionPerformed

    private void sortChoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sortChoiceActionPerformed
        if (!isAdjusting) {
            updateBinComparator();
        }
    }//GEN-LAST:event_sortChoiceActionPerformed

    private void attributeChoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attributeChoiceActionPerformed
        if (!isAdjusting) {
            topComponent.setAttribute((String) attributeChoice.getSelectedItem());
        }
    }//GEN-LAST:event_attributeChoiceActionPerformed

    private void binFormatterComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_binFormatterComboActionPerformed
        if (!isAdjusting) {
            final BinFormatter binFormatter = (BinFormatter) binFormatterCombo.getSelectedItem();

            final PluginParameters parameters;
            if (currentHistogramState != null && binFormatter.getClass() == currentHistogramState.getBinFormatter().getClass()) {
                PluginParameters p = currentHistogramState.getBinFormatterParameters();
                parameters = p == null ? null : p.copy();
                binFormatter.updateParameters(parameters);
            } else if (CURRENT_PARAMETER_IDS.containsKey(binFormatter)) {
                parameters = CURRENT_PARAMETER_IDS.get(binFormatter).copy();
                binFormatter.updateParameters(parameters);
            } else {
                parameters = binFormatter.createParameters();
                binFormatter.updateParameters(parameters);
            }

            if (parameters != null) {
                final PluginParametersSwingDialog dialog = new PluginParametersSwingDialog(binFormatter.getLabel(), parameters);
                dialog.showAndWait();
                if (PluginParametersDialog.OK.equals(dialog.getResult())) {
                    CURRENT_PARAMETER_IDS.put(binFormatter, parameters.copy());
                    topComponent.setBinFormatter((BinFormatter) binFormatterCombo.getSelectedItem(), parameters);
                } else if (currentHistogramState != null) {
                    binFormatterCombo.setSelectedItem(currentHistogramState.getBinFormatter());
                } else {
                    // Do nothing
                }
            } else {
                topComponent.setBinFormatter((BinFormatter) binFormatterCombo.getSelectedItem(), null);
            }
        }
    }//GEN-LAST:event_binFormatterComboActionPerformed

    private void actionsButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_actionsButtonMousePressed
        actionsMenu.show(actionsButton, evt.getX(), evt.getY());
    }//GEN-LAST:event_actionsButtonMousePressed

    private void vertexToggleStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_vertexToggleStateChanged
    {//GEN-HEADEREND:event_vertexToggleStateChanged
        if (!isAdjusting && vertexToggle.isSelected()) {
            topComponent.setGraphElementType(GraphElementType.VERTEX);
        }
    }//GEN-LAST:event_vertexToggleStateChanged

    private void transactionToggleStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_transactionToggleStateChanged
    {//GEN-HEADEREND:event_transactionToggleStateChanged
        if (!isAdjusting && transactionToggle.isSelected()) {
            topComponent.setGraphElementType(GraphElementType.TRANSACTION);
        }
    }//GEN-LAST:event_transactionToggleStateChanged

    private void edgeToggleStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_edgeToggleStateChanged
    {//GEN-HEADEREND:event_edgeToggleStateChanged
        if (!isAdjusting && edgeToggle.isSelected()) {
            topComponent.setGraphElementType(GraphElementType.EDGE);
        }
    }//GEN-LAST:event_edgeToggleStateChanged

    private void linkToggleStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_linkToggleStateChanged
    {//GEN-HEADEREND:event_linkToggleStateChanged
        if (!isAdjusting && linkToggle.isSelected()) {
            topComponent.setGraphElementType(GraphElementType.LINK);
        }
    }//GEN-LAST:event_linkToggleStateChanged

    private void helpButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_helpButtonMousePressed
        final HelpCtx help = new HelpCtx("au.gov.asd.tac.constellation.views.histogram");
        help.display();
    }//GEN-LAST:event_helpButtonMousePressed

    private void saveBinsToGraph() {
        topComponent.saveBinsToGraph();
    }

    private void updateBinComparator() {
        BinComparator binComparator = (BinComparator) sortChoice.getSelectedItem();
        topComponent.setBinComparator(this.descendingButton.isSelected() ? binComparator.getReverse() : binComparator);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton actionsButton;
    private javax.swing.JComboBox<String> attributeChoice;
    private javax.swing.JComboBox<AttributeType> attributeTypeChoice;
    private javax.swing.JComboBox<BinFormatter> binFormatterCombo;
    private javax.swing.JLabel categoryLabel;
    private javax.swing.JButton clearFilterButton;
    private javax.swing.JToggleButton descendingButton;
    private javax.swing.JToggleButton edgeToggle;
    private javax.swing.JLabel filterLabel;
    private javax.swing.JButton filterSelectionButton;
    private javax.swing.ButtonGroup graphElementGroup;
    private javax.swing.JLabel graphElementLabel;
    private javax.swing.JButton helpButton;
    private javax.swing.JToggleButton linkToggle;
    private javax.swing.JLabel propertyLabel;
    private javax.swing.JButton selectButton;
    private javax.swing.JComboBox<BinSelectionMode> selectionModeChoice;
    private javax.swing.JLabel selectionModeLabel;
    private javax.swing.JComboBox<BinComparator> sortChoice;
    private javax.swing.JLabel sortLabel;
    private javax.swing.ButtonGroup sortOrderGroup;
    private javax.swing.JToggleButton transactionToggle;
    private javax.swing.JPanel uiPanel;
    private javax.swing.JToggleButton vertexToggle;
    // End of variables declaration//GEN-END:variables
}
