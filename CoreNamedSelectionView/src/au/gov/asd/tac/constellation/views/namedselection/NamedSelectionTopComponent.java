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
package au.gov.asd.tac.constellation.views.namedselection;

import au.gov.asd.tac.constellation.views.SwingTopComponent;
import au.gov.asd.tac.constellation.views.namedselection.panes.NamedSelectionListElement;
import au.gov.asd.tac.constellation.views.namedselection.panes.NamedSelectionModDescPanel;
import au.gov.asd.tac.constellation.views.namedselection.panes.NamedSelectionProtectedPanel;
import au.gov.asd.tac.constellation.views.namedselection.panes.NamedSelectionRenamerPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import javax.swing.AbstractListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

/**
 * Top component which displays the named selection browser, and handles named
 * selection user interactions.
 *
 * @author betelgeuse
 */
@ConvertAsProperties(
        dtd = "-//au.gov.asd.tac.constellation.views.namedselection//NamedSelection//EN",
        autostore = false)
@TopComponent.Description(
        preferredID = "NamedSelectionTopComponent",
        iconBase = "au/gov/asd/tac/constellation/views/namedselection/resources/named_selections.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(
        mode = "explorer",
        openAtStartup = false)
@ActionID(
        category = "Window",
        id = "au.gov.asd.tac.constellation.views.namedselection.NamedSelectionTopComponent")
@ActionReferences({
    @ActionReference(path = "Menu/Views", position = 800),
    @ActionReference(path = "Shortcuts", name = "CS-N")})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_NamedSelectionAction",
        preferredID = "NamedSelectionTopComponent")
@Messages({
    "No_Active_Graph=<No Active Graph>",
    "NamedSelection_Shortcut=Shortcut: ",
    "NamedSelection_CurrentSelection=<Current Selection>",
    "NamedSelection_CurrentSelectionToolTip=Represents the graph elements that are currently selected on the active graph.",
    "CTL_NamedSelectionAction=Named Selections",
    "CTL_NamedSelectionTopComponent=Named Selections",
    "HINT_NamedSelectionTopComponent=This window presents all named selections for an active graph."})
public final class NamedSelectionTopComponent extends SwingTopComponent<JPanel> {
    // Labels used to mask UI in the event of having no active graph:

    private final JLabel lblNoGraph = new JLabel(Bundle.No_Active_Graph());
    private final JPanel panelNoGraph = new JPanel();
    /**
     * Uses an overloaded MouseAdapter class to intercept mouse interactions on
     * the <code>lstNamedSelections</code>.
     *
     * @see MouseAdapter
     */
    private final MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(final MouseEvent e) {
            // On double click, retrieve the selection:
            if (e.getButton() == MouseEvent.BUTTON1) {
                if (e.getClickCount() == 2) {
                    retrieveSelection();
                }
                
            // Right click: open context menu on the named selection 'under' the mouse pointer:
            } else if (e.getButton() == MouseEvent.BUTTON3) {
                lstNamedSelections.setSelectedIndex(lstNamedSelections.locationToIndex(e.getPoint()));

                boolean isEnabled = true;
                if (lstNamedSelections.getSelectedIndex() == 0) {
                    isEnabled = false;
                }
                for (Component c : popContext.getComponents()) {
                    c.setEnabled(isEnabled);
                }

                // Update the context menu based on the whether or not the selection is a protected one:
                final boolean locked = ((NamedSelection) lstNamedSelections.getSelectedValue()).isLocked();
                mnuCheckLocked.setSelected(locked);
                mnuDescription.setEnabled(!locked);
                mnuOverwrite.setEnabled(!locked);
                mnuRemove.setEnabled(!locked);
                mnuRename.setEnabled(!locked);
            } else {
                // Do nothing
            }
        }
    };
    /**
     * Uses an overloaded KeyAdapter class to intercept mouse interactions on
     * the <code>lstNamedSelections</code>.
     *
     * @see KeyAdapter
     */
    private final KeyListener keyListener = new KeyAdapter() {
        @Override
        public void keyPressed(final KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_ENTER -> // On enter, retrieve the selection:
                    retrieveSelection();
                case KeyEvent.VK_F2 -> // On F2, rename the selection:
                    renameElement();
                default -> {
                }
            }
        }
    };

    /**
     * Constructs a new <code>NamedSelectionTopComponent</code>.
     */
    public NamedSelectionTopComponent() {
        initComponents();

        setName(Bundle.CTL_NamedSelectionTopComponent());
        setToolTipText(Bundle.HINT_NamedSelectionTopComponent());

        // Create label and container to show user when no active graph selected:
        panelNoGraph.setLayout(new BorderLayout());
        panelNoGraph.setName("panelNoGraph");
        lblNoGraph.setOpaque(true);
        lblNoGraph.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblNoGraph.setVerticalAlignment(javax.swing.SwingConstants.CENTER);
        panelNoGraph.add(lblNoGraph, BorderLayout.CENTER);

        // Assign custom cell renderer:
        lstNamedSelections.setCellRenderer(new NamedSelectionCellRenderer());

        // Assign mouse and keyboard listeners:
        lstNamedSelections.addMouseListener(mouseListener);
        lstNamedSelections.addKeyListener(keyListener);

        // Context menu (popupmenu):
        lstNamedSelections.setComponentPopupMenu(popContext);

        // Register the list of selections for tooltips:
        ToolTipManager.sharedInstance().registerComponent(lstNamedSelections);

        // Set the list model to the list:
        final NamedSelectionListModel listModel = new NamedSelectionListModel();
        lstNamedSelections.setModel(listModel);

        // Start with nothing visible:
        toggleUI(false);
    }

    /**
     * Overridden <code>componentOpened</code> method ensures that the
     * <code>NamedSelectionManager</code> is instantiated to ensure that
     * elements of this component are updated.
     */
    @Override
    protected void handleComponentOpened() {
        super.handleComponentOpened();
        // Ensure the manager is initialised and determine whether it is managing a graph:
        final boolean isEnabled = NamedSelectionManager.getDefault().isManagingActiveGraph();
        toggleUI(isEnabled);
    }

    /**
     * Updates the NamedSelectionTopComponent with the latest
     * <code>NamedSelection</code>s.
     *
     * @param selections The <code>ArrayList&lt;NamedSelection&gt;</code>
     * pertinent to the currently active graph.
     *
     * @see NamedSelection
     * @see NamedSelectionTopComponent
     */
    public void updateState(final ArrayList<NamedSelection> selections) {
        if (selections != null) {
            // Clear everything so we can start fresh:
            clearAllNamedSelections();

            // Add the current selection item:
            final NamedSelection currentSelection = new NamedSelection();
            currentSelection.setDescription(Bundle.NamedSelection_CurrentSelectionToolTip());
            addElement(currentSelection);

            // Ensure the UI is ready for use:
            toggleUI(true);

            // Load up any selections:
            if (!selections.isEmpty()) {
                for (NamedSelection selection : selections) {
                    addElement(selection);
                }
            }
        } else {
            // We don't have a graph, so disable accordingly.
            toggleUI(false);
        }
    }

    /**
     * Sets the selected state of the toggle button responsible for dimming
     * others.
     *
     * @param isDimOthers <code>true</code> sets the button selected state * to
     * <code>true</code>.
     */
    public void updateDimOthers(final boolean isDimOthers) {
        btnDimMode.setSelected(isDimOthers);
    }

    /**
     * Sets the selected state of the toggle button responsible for causing
     * result of named selection to be returned in a selected/non selected
     * manner.
     *
     * @param isSelectResults <code>true</code> sets the button selected state
     * to <code>true</code>.
     */
    public void updateSelectResults(final boolean isSelectResults) {
        btnSelectResults.setSelected(isSelectResults);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        popContext = new javax.swing.JPopupMenu();
        mnuSelect = new javax.swing.JMenuItem();
        mnuDimOthers = new javax.swing.JMenuItem();
        sprOne = new javax.swing.JPopupMenu.Separator();
        mnuCheckLocked = new javax.swing.JCheckBoxMenuItem();
        sprTwo = new javax.swing.JPopupMenu.Separator();
        mnuOverwrite = new javax.swing.JMenuItem();
        mnuClone = new javax.swing.JMenuItem();
        mnuRemove = new javax.swing.JMenuItem();
        sprThree = new javax.swing.JPopupMenu.Separator();
        mnuRename = new javax.swing.JMenuItem();
        mnuDescription = new javax.swing.JMenuItem();
        tlbActions = new javax.swing.JToolBar();
        btnAdd = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();
        sprtToolbar1 = new javax.swing.JToolBar.Separator();
        btnSelectResults = new javax.swing.JToggleButton();
        btnDimMode = new javax.swing.JToggleButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        btnUnion = new javax.swing.JButton();
        btnIntersection = new javax.swing.JButton();
        scrlContent = new javax.swing.JScrollPane();
        lstNamedSelections = new NamedSelectionList();

        popContext.setName("jPopupMenu"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(mnuSelect, org.openide.util.NbBundle.getMessage(NamedSelectionTopComponent.class, "NamedSelectionTopComponent.mnuSelect.text")); // NOI18N
        mnuSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSelectActionPerformed(evt);
            }
        });
        popContext.add(mnuSelect);

        org.openide.awt.Mnemonics.setLocalizedText(mnuDimOthers, org.openide.util.NbBundle.getMessage(NamedSelectionTopComponent.class, "NamedSelectionTopComponent.mnuDimOthers.text")); // NOI18N
        mnuDimOthers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuDimOthersActionPerformed(evt);
            }
        });
        popContext.add(mnuDimOthers);
        popContext.add(sprOne);

        org.openide.awt.Mnemonics.setLocalizedText(mnuCheckLocked, org.openide.util.NbBundle.getMessage(NamedSelectionTopComponent.class, "NamedSelectionTopComponent.mnuCheckLocked.text")); // NOI18N
        mnuCheckLocked.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuCheckLockedActionPerformed(evt);
            }
        });
        popContext.add(mnuCheckLocked);
        popContext.add(sprTwo);

        org.openide.awt.Mnemonics.setLocalizedText(mnuOverwrite, org.openide.util.NbBundle.getMessage(NamedSelectionTopComponent.class, "NamedSelectionTopComponent.mnuOverwrite.text")); // NOI18N
        mnuOverwrite.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOverwriteActionPerformed(evt);
            }
        });
        popContext.add(mnuOverwrite);

        org.openide.awt.Mnemonics.setLocalizedText(mnuClone, org.openide.util.NbBundle.getMessage(NamedSelectionTopComponent.class, "NamedSelectionTopComponent.mnuClone.text")); // NOI18N
        mnuClone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuCloneActionPerformed(evt);
            }
        });
        popContext.add(mnuClone);

        org.openide.awt.Mnemonics.setLocalizedText(mnuRemove, org.openide.util.NbBundle.getMessage(NamedSelectionTopComponent.class, "NamedSelectionTopComponent.mnuRemove.text")); // NOI18N
        mnuRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuRemoveActionPerformed(evt);
            }
        });
        popContext.add(mnuRemove);
        popContext.add(sprThree);

        org.openide.awt.Mnemonics.setLocalizedText(mnuRename, org.openide.util.NbBundle.getMessage(NamedSelectionTopComponent.class, "NamedSelectionTopComponent.mnuRename.text")); // NOI18N
        mnuRename.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuRenameActionPerformed(evt);
            }
        });
        popContext.add(mnuRename);

        org.openide.awt.Mnemonics.setLocalizedText(mnuDescription, org.openide.util.NbBundle.getMessage(NamedSelectionTopComponent.class, "NamedSelectionTopComponent.mnuDescription.text")); // NOI18N
        mnuDescription.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuDescriptionActionPerformed(evt);
            }
        });
        popContext.add(mnuDescription);

        tlbActions.setFloatable(false);
        tlbActions.setOrientation(javax.swing.SwingConstants.VERTICAL);
        tlbActions.setRollover(true);

        btnAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/au/gov/asd/tac/constellation/views/namedselection/resources/namedselection_add.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnAdd, org.openide.util.NbBundle.getMessage(NamedSelectionTopComponent.class, "NamedSelectionTopComponent.btnAdd.text")); // NOI18N
        btnAdd.setToolTipText(org.openide.util.NbBundle.getMessage(NamedSelectionTopComponent.class, "NamedSelectionTopComponent.btnAdd.toolTipText")); // NOI18N
        btnAdd.setFocusable(false);
        btnAdd.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAdd.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });
        tlbActions.add(btnAdd);

        btnRemove.setIcon(new javax.swing.ImageIcon(getClass().getResource("/au/gov/asd/tac/constellation/views/namedselection/resources/namedselection_trash.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnRemove, org.openide.util.NbBundle.getMessage(NamedSelectionTopComponent.class, "NamedSelectionTopComponent.btnRemove.text")); // NOI18N
        btnRemove.setToolTipText(org.openide.util.NbBundle.getMessage(NamedSelectionTopComponent.class, "NamedSelectionTopComponent.btnRemove.toolTipText")); // NOI18N
        btnRemove.setFocusable(false);
        btnRemove.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnRemove.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
            }
        });
        tlbActions.add(btnRemove);
        tlbActions.add(sprtToolbar1);

        btnSelectResults.setIcon(new javax.swing.ImageIcon(getClass().getResource("/au/gov/asd/tac/constellation/views/namedselection/resources/namedselection_selected.png"))); // NOI18N
        btnSelectResults.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(btnSelectResults, org.openide.util.NbBundle.getMessage(NamedSelectionTopComponent.class, "NamedSelectionTopComponent.btnSelectResults.text")); // NOI18N
        btnSelectResults.setToolTipText(org.openide.util.NbBundle.getMessage(NamedSelectionTopComponent.class, "NamedSelectionTopComponent.btnSelectResults.toolTipText")); // NOI18N
        btnSelectResults.setFocusable(false);
        btnSelectResults.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSelectResults.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSelectResults.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnSelectResultsMouseClicked(evt);
            }
        });
        tlbActions.add(btnSelectResults);

        btnDimMode.setIcon(new javax.swing.ImageIcon(getClass().getResource("/au/gov/asd/tac/constellation/views/namedselection/resources/namedselection_dimmed.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnDimMode, org.openide.util.NbBundle.getMessage(NamedSelectionTopComponent.class, "NamedSelectionTopComponent.btnDimMode.text")); // NOI18N
        btnDimMode.setToolTipText(org.openide.util.NbBundle.getMessage(NamedSelectionTopComponent.class, "NamedSelectionTopComponent.btnDimMode.toolTipText")); // NOI18N
        btnDimMode.setFocusable(false);
        btnDimMode.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnDimMode.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnDimMode.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnDimModeMouseClicked(evt);
            }
        });
        tlbActions.add(btnDimMode);
        tlbActions.add(jSeparator1);

        btnUnion.setIcon(new javax.swing.ImageIcon(getClass().getResource("/au/gov/asd/tac/constellation/views/namedselection/resources/namedselection_union.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnUnion, org.openide.util.NbBundle.getMessage(NamedSelectionTopComponent.class, "NamedSelectionTopComponent.btnUnion.text")); // NOI18N
        btnUnion.setToolTipText(org.openide.util.NbBundle.getMessage(NamedSelectionTopComponent.class, "NamedSelectionTopComponent.btnUnion.toolTipText")); // NOI18N
        btnUnion.setFocusable(false);
        btnUnion.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnUnion.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnUnion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUnionActionPerformed(evt);
            }
        });
        tlbActions.add(btnUnion);

        btnIntersection.setIcon(new javax.swing.ImageIcon(getClass().getResource("/au/gov/asd/tac/constellation/views/namedselection/resources/namedselection_intersection.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnIntersection, org.openide.util.NbBundle.getMessage(NamedSelectionTopComponent.class, "NamedSelectionTopComponent.btnIntersection.text")); // NOI18N
        btnIntersection.setToolTipText(org.openide.util.NbBundle.getMessage(NamedSelectionTopComponent.class, "NamedSelectionTopComponent.btnIntersection.toolTipText")); // NOI18N
        btnIntersection.setFocusable(false);
        btnIntersection.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnIntersection.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnIntersection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIntersectionActionPerformed(evt);
            }
        });
        tlbActions.add(btnIntersection);

        scrlContent.setAutoscrolls(true);

        lstNamedSelections.setInheritsPopupMenu(true);
        scrlContent.setViewportView(lstNamedSelections);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(tlbActions, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrlContent, javax.swing.GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tlbActions, javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE)
            .addComponent(scrlContent)
        );
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Event handler for <code>btnRemove</code> clicks.
     * <p>
     * This causes currently selected named selections to be removed.
     *
     * @param evt The registered event.
     */
    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed

        removeElement();
    }//GEN-LAST:event_btnRemoveActionPerformed

    /**
     * Event handler for <code>btnIntersection</code> clicks.
     * <p>
     * This triggers an intersection operation for the currently selected named
     * selections.
     *
     * @param evt The registered event.
     */
    private void btnIntersectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIntersectionActionPerformed
        performIntersection();
    }//GEN-LAST:event_btnIntersectionActionPerformed

    /**
     * Event handler for <code>btnUnion</code> clicks.
     * <p>
     * This triggers a union operation for the currently selected named
     * selections.
     *
     * @param evt The registered event.
     */
    private void btnUnionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUnionActionPerformed
        performUnion();
    }//GEN-LAST:event_btnUnionActionPerformed

    /**
     * Event handler for <code>btnAdd</code> clicks.
     * <p>
     * This triggers the creation of a new named selection.
     *
     * @param evt The registered event.
     */
    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        addSelection();
    }//GEN-LAST:event_btnAddActionPerformed

    /**
     * Event handler for <code>mnuSelect</code> selections.
     * <p>
     * This triggers the recall of the currently selected named selection.
     * <p>
     * As this menu item appears in the context menu (right-click menu), the
     * selection would typically be updated to what is under the mouse pointer
     * at the time of the mouse click.
     *
     * @param evt The registered event.
     */
    private void mnuSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSelectActionPerformed
        retrieveSelection();
    }//GEN-LAST:event_mnuSelectActionPerformed

    /**
     * Event handler for <code>mnuRemove</code> selections.
     * <p>
     * This causes currently selected named selection to be removed.
     * <p>
     * As this menu item appears in the context menu (right-click menu), the
     * selection would typically be updated to what is under the mouse pointer
     * at the time of the mouse click.
     *
     * @param evt The registered event.
     */
    private void mnuRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuRemoveActionPerformed
        removeElement();
    }//GEN-LAST:event_mnuRemoveActionPerformed

    /**
     * Event handler for <code>mnuRename</code> selections.
     * <p>
     * This causes currently selected named selection to be renamed.
     * <p>
     * As this menu item appears in the context menu (right-click menu), the
     * selection would typically be updated to what is under the mouse pointer
     * at the time of the mouse click.
     *
     * @param evt The registered event.
     */
    private void mnuRenameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuRenameActionPerformed
        renameElement();
    }//GEN-LAST:event_mnuRenameActionPerformed

    /**
     * Event handler for <code>btnDimMode</code> clicks.
     * <p>
     * This causes the state of the 'dim others' button to be toggled (and saved
     * to the graph).
     *
     * @param evt The registered event.
     */
    private void btnDimModeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnDimModeMouseClicked
        setDimState();
    }//GEN-LAST:event_btnDimModeMouseClicked

    /**
     * Event handler for <code>btnSelectResults</code> clicks.
     * <p>
     * This causes the state of the 'return results of operations as "selected"'
     * button to be toggled (and saved to the graph).
     *
     * @param evt The registered event.
     */
    private void btnSelectResultsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnSelectResultsMouseClicked
        setSelectResultsState();
    }//GEN-LAST:event_btnSelectResultsMouseClicked

    /**
     * Event handler for <code>mnuDimOthers</code> selections.
     * <p>
     * This causes currently selected named selection to be returned, and all
     * non-member graph elements of the given named selection to be dimmed.
     * <p>
     * As this menu item appears in the context menu (right-click menu), the
     * selection would typically be updated to what is under the mouse pointer
     * at the time of the mouse click.
     *
     * @param evt The registered event.
     */
    private void mnuDimOthersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuDimOthersActionPerformed
        dimAllOthers();
    }//GEN-LAST:event_mnuDimOthersActionPerformed

    /**
     * Event handler for <code>mnuClone</code> selections.
     * <p>
     * This causes currently selected named selections to be cloned to a new
     * named selection.
     * <p>
     * As this menu item appears in the context menu (right-click menu), the
     * selection would typically be updated to what is under the mouse pointer
     * at the time of the mouse click.
     *
     * @param evt The registered event.
     */
    private void mnuCloneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuCloneActionPerformed
        cloneSelection();
    }//GEN-LAST:event_mnuCloneActionPerformed

    /**
     * Event handler for <code>mnuDescritpion</code> selections.
     * <p>
     * This causes the a dialog box to be created that allows for the
     * modification of the given named selection's description.
     * <p>
     * As this menu item appears in the context menu (right-click menu), the
     * selection would typically be updated to what is under the mouse pointer
     * at the time of the mouse click.
     *
     * @param evt The registered event.
     */
    private void mnuDescriptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuDescriptionActionPerformed
        modifyDescription();
    }//GEN-LAST:event_mnuDescriptionActionPerformed

    /**
     * Event handler for <code>mnuCheckLocked</code> selections.
     * <p>
     * This causes the selection to be placed in a locked state.
     *
     * @param evt The registered event.
     */
    private void mnuCheckLockedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuCheckLockedActionPerformed
        setLocked();
    }//GEN-LAST:event_mnuCheckLockedActionPerformed

    /**
     * Event handler for <code>mnuOverwrite</code> selections.
     * <p>
     * This causes the selection to have its contents overwritten.
     *
     * @param evt The registered event.
     */
    private void mnuOverwriteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuOverwriteActionPerformed
        overwriteSelection();
    }//GEN-LAST:event_mnuOverwriteActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JToggleButton btnDimMode;
    private javax.swing.JButton btnIntersection;
    private javax.swing.JButton btnRemove;
    private javax.swing.JToggleButton btnSelectResults;
    private javax.swing.JButton btnUnion;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JList lstNamedSelections;
    private javax.swing.JCheckBoxMenuItem mnuCheckLocked;
    private javax.swing.JMenuItem mnuClone;
    private javax.swing.JMenuItem mnuDescription;
    private javax.swing.JMenuItem mnuDimOthers;
    private javax.swing.JMenuItem mnuOverwrite;
    private javax.swing.JMenuItem mnuRemove;
    private javax.swing.JMenuItem mnuRename;
    private javax.swing.JMenuItem mnuSelect;
    private javax.swing.JPopupMenu popContext;
    private javax.swing.JScrollPane scrlContent;
    private javax.swing.JPopupMenu.Separator sprOne;
    private javax.swing.JPopupMenu.Separator sprThree;
    private javax.swing.JPopupMenu.Separator sprTwo;
    private javax.swing.JToolBar.Separator sprtToolbar1;
    private javax.swing.JToolBar tlbActions;
    // End of variables declaration//GEN-END:variables

    void writeProperties(final java.util.Properties p) {
        // Method Implementation required for @ConvertAsProperties, intentionally left blank
    }

    void readProperties(final java.util.Properties p) {
        // Method Implementation required for @ConvertAsProperties, intentionally left blank
    }

    /**
     * Handles the enabling and disabling of the top component based on whether
     * there is an active graph.
     *
     * @param isEnabled <code>true</code> if there is an active graph.
     */
    private void toggleUI(final boolean isEnabled) {
        // Toggle interface:
        if (isEnabled) {
            scrlContent.setViewportView(lstNamedSelections);
        } else {
            scrlContent.setViewportView(panelNoGraph);
        }

        // Enable / disable all UI controls depending on availability of a graph to operate on:
        for (Component c : this.getComponents()) {
            c.setEnabled(isEnabled);
        }
        for (Component c : tlbActions.getComponents()) {
            c.setEnabled(isEnabled);
        }

        validate();
        repaint();
    }

    /**
     * Helper method that adds a <code>NamedSelection</code> to the list of
     * current Named Selections.
     *
     * @param selection The selection to be appended to the list.
     */
    private void addElement(final NamedSelection selection) {
        NamedSelectionListModel selections = (NamedSelectionListModel) lstNamedSelections.getModel();

        selections.addElement(selection);
        SwingUtilities.invokeLater(new UpdateRunner());
    }

    /**
     * Helper method that removes the currently selected
     * <code>NamedSelection</code>s from the list of current Named Selections.
     */
    private void removeElement() {
        final int[] indices = lstNamedSelections.getSelectedIndices();
        final NamedSelectionListModel selections = (NamedSelectionListModel) lstNamedSelections.getModel();
        final ArrayList<NamedSelection> removeSelections = new ArrayList<>();

        lstNamedSelections.clearSelection();

        for (int index = (indices.length - 1); index >= 0; index--) {
            // Current Selection item lives at 0, and we want to ensure it cannot be deleted.
            if (indices[index] != 0) {
                removeSelections.add(selections.getElementAt(indices[index]));
            }
        }

        if (removeSelections.size() > 0) {
            NamedSelectionManager.getDefault().clearSelections(removeSelections);
        }
    }

    /**
     * Helper method that renames the selected <code>NamedSelection</code>.
     */
    private void renameElement() {
        final int index = lstNamedSelections.getSelectedIndex();
        final NamedSelectionListModel selections = (NamedSelectionListModel) lstNamedSelections.getModel();

        if (index > 0) {
            final NamedSelection current = selections.getElementAt(index);

            if (!current.isLocked()) {
                final NamedSelectionRenamerPanel nsrp = new NamedSelectionRenamerPanel(current.getName());
                final DialogDescriptor dd = new DialogDescriptor(nsrp, "Rename " + current.getName());
                final Object result = DialogDisplayer.getDefault().notify(dd);

                final String potentialName = nsrp.getNewName();

                // Update the name only if it is different from the previous name:
                if (result == NotifyDescriptor.OK_OPTION && !potentialName.equals(current.getName())) {
                    NamedSelectionManager.getDefault().renameNamedSelection(current, potentialName);
                }
            } else {
                notifyProtected(current.getName());
            }
        }
    }

    /**
     * Helper method that modifies the description of the highlighted named
     * selection through the <code>NamedSelectionManager</code>.
     *
     * @see NamedSelectionManager
     */
    private void modifyDescription() {
        final int index = lstNamedSelections.getSelectedIndex();
        final NamedSelectionListModel selections = (NamedSelectionListModel) lstNamedSelections.getModel();

        if (index > 0) {
            final NamedSelection current = selections.getElementAt(index);

            if (!current.isLocked()) {
                final NamedSelectionModDescPanel nsmp = new NamedSelectionModDescPanel(current.getName(), current.getDescription());
                final DialogDescriptor dd = new DialogDescriptor(nsmp, "Set Description");
                final Object result = DialogDisplayer.getDefault().notify(dd);

                final String potentialDesc = nsmp.getNewDescription();

                // Update the name only if it is different from the previous name:
                if (result == NotifyDescriptor.OK_OPTION && !potentialDesc.equals(current.getName())) {
                    NamedSelectionManager.getDefault().setDescriptionNamedSelection(current, potentialDesc);
                }
            } else {
                notifyProtected(current.getName());
            }
        }
    }

    /**
     * Helper method that requests the locked state to be switched for the
     * current selection through the <code>NamedSelectionManager</code>.
     *
     * @see NamedSelectionManager
     */
    private void setLocked() {
        final int index = lstNamedSelections.getSelectedIndex();
        final NamedSelectionListModel selections = (NamedSelectionListModel) lstNamedSelections.getModel();

        if (index > 0) {
            final NamedSelection current = selections.getElementAt(index);

            NamedSelectionManager.getDefault().toggleLockedNamedSelection(current);
        }
    }

    /**
     * Helper method that requests the Manager dims all other graph elements
     * other than those in the currently highlighted named selection.
     *
     * @see NamedSelectionManager
     */
    private void dimAllOthers() {
        final int index = lstNamedSelections.getSelectedIndex();
        final NamedSelectionListModel selections = (NamedSelectionListModel) lstNamedSelections.getModel();
        final NamedSelection current = selections.getElementAt(index);

        NamedSelectionManager.getDefault().dimOtherThanSelection(current);
    }

    /**
     * Helper method that requests the current Named Selection gets cloned in
     * the <code>NamedSelectionManager</code>.
     *
     * @see NamedSelectionManager
     */
    private void cloneSelection() {
        final int index = lstNamedSelections.getSelectedIndex();
        final NamedSelectionListModel selections = (NamedSelectionListModel) lstNamedSelections.getModel();
        final NamedSelection current = selections.getElementAt(index);

        NamedSelectionManager.getDefault().cloneNamedSelection(current);
    }

    /**
     * Helper method that determines the currently highlighted named selections,
     * and requests an intersection operation be performed on them through the
     * <code>NamedSelectionManager</code>.
     *
     * @see NamedSelectionManager
     */
    private void performIntersection() {
        final int[] indices = lstNamedSelections.getSelectedIndices();
        final NamedSelectionListModel selections = (NamedSelectionListModel) lstNamedSelections.getModel();
        final ArrayList<NamedSelection> intersect = new ArrayList<>();

        for (int index = 0; index < indices.length; index++) {
            intersect.add(selections.getElementAt(indices[index]));
        }

        if (intersect.size() > 1) {
            NamedSelectionManager.getDefault().performIntersection(intersect);
        }
    }

    /**
     * Helper method that determines the currently highlighted named selections,
     * and requests a union operation be performed on them through the
     * <code>NamedSelectionManager</code>.
     *
     * @see NamedSelectionManager
     */
    private void performUnion() {
        final int[] indices = lstNamedSelections.getSelectedIndices();
        final NamedSelectionListModel selections = (NamedSelectionListModel) lstNamedSelections.getModel();
        final ArrayList<NamedSelection> union = new ArrayList<>();

        for (int index = 0; index < indices.length; index++) {
            union.add(selections.getElementAt(indices[index]));
        }

        if (union.size() > 1) {
            NamedSelectionManager.getDefault().performUnion(union);
        }
    }

    /**
     * Helper method that clears the entire list of named selections.
     */
    private void clearAllNamedSelections() {
        final NamedSelectionListModel selections = (NamedSelectionListModel) lstNamedSelections.getModel();
        selections.clearNamedSelections();
    }

    /**
     * Helper method that requests a named selection be created through the
     * <code>NamedSelectionManager</code>.
     *
     * @see NamedSelectionManager
     */
    private void addSelection() {
        NamedSelectionManager.getDefault().createNamedSelection();
    }

    /**
     * Helper method that requests a named selection be overwritten through the
     * <code>NamedSelectionManager</code>.
     *
     * @see NamedSelectionManager
     */
    private void overwriteSelection() {
        final NamedSelection selection = (NamedSelection) lstNamedSelections.getSelectedValue();

        NamedSelectionManager.getDefault().overwriteNamedSelection(selection);
    }

    /**
     * Helper method that requests a named selection be recalled through the
     * <code>NamedSelectionManager</code>.
     *
     * @see NamedSelectionManager
     */
    private void retrieveSelection() {
        final int index = lstNamedSelections.getSelectedIndex();
        if (index > 0) {
            final NamedSelection item = (NamedSelection) lstNamedSelections.getModel().getElementAt(index);

            NamedSelectionManager.getDefault().recallSelection(item);
        }
    }

    /**
     * Helper method that updates the dim others state in the
     * <code>NamedSelectionManager</code>
     *
     * @see NamedSelectionManager
     */
    private void setDimState() {
        NamedSelectionManager.getDefault().updateDimOthersState(btnDimMode.isSelected());
    }

    /**
     * Helper method that updates the select results state in the
     * <code>NamedSelectionManager</code>
     *
     * @see NamedSelectionManager
     */
    private void setSelectResultsState() {
        NamedSelectionManager.getDefault().updateSelectResultsState(btnSelectResults.isSelected());
    }

    /**
     * Helper method that notifies the user that they are trying to perform
     * modifications on a locked / protected named selection.
     *
     * @param name The name of the selection that is being intercepted from
     * modification.
     */
    private void notifyProtected(final String name) {
        final NamedSelectionProtectedPanel panel = new NamedSelectionProtectedPanel(name);
        final DialogDescriptor dd = new DialogDescriptor(panel, Bundle.ProtectedSelection());
        dd.setOptions(new Object[]{"OK"});
        DialogDisplayer.getDefault().notify(dd);
    }

    @Override
    protected JPanel createContent() {
        return panelNoGraph;
    }

    /**
     * Extended <code>JList</code> used in the generation of the list of named
     * selections.
     * <p>
     * This class has been extended so that tooltips can be intercepted and
     * populated with named selection descriptions.
     *
     * @see JList
     */
    private class NamedSelectionList extends JList<NamedSelection> {

        @Override
        public String getToolTipText(final MouseEvent e) {
            final NamedSelectionListModel selections = (NamedSelectionListModel) super.getModel();
            final NamedSelection item = selections.getElementAt(super.locationToIndex(e.getPoint()));

            return !item.getDescription().isEmpty() ? item.getDescription() : null;
        }
    }

    /**
     * Helper class used to force updates to <code>lstNamedSelections</code> to
     * the EDT thread.
     *
     * @see SwingUtilities.invokeLater
     */
    private class UpdateRunner implements Runnable {

        @Override
        public void run() {
            lstNamedSelections.validate();
            lstNamedSelections.repaint();
        }
    }

    /**
     * Custom <code>ListCellrenderer</code> which is able to handle
     * <code>NamedSelection</code>s.
     * <p>
     * This class is utilized by <code>lstNamedSelections</code>.
     *
     * @see ListCellRenderer
     * @see NamedSelection
     */
    private class NamedSelectionCellRenderer extends NamedSelectionListElement implements ListCellRenderer<NamedSelection> {

        @Override
        public Component getListCellRendererComponent(final JList<? extends NamedSelection> list, final NamedSelection value, final int index,
                final boolean isSelected, final boolean cellHasFocus) {
            lblNamedSelection.setText(value.getName());
            if (value.getID() < 0) { // Check if this is the 'current selection' entity, and act accordingly:
                // We have the 'current selection' list item, which is not actually a named selection:
                lblNamedSelection.setText(Bundle.NamedSelection_CurrentSelection());
                lblNamedSelection.setFont(new Font(lblNamedSelection.getFont().getFontName(),
                        Font.ITALIC, lblNamedSelection.getFont().getSize()));
                lblShortcutKey.setText("");
                lblShortcutKey.setVisible(false);
                lblShortcutText.setVisible(false);
                super.setLockedStatus(false);
            } else { // We have a regular named selection, so renderer it (along with shortcut if applicable).
                lblNamedSelection.setFont(new Font(lblNamedSelection.getFont().getFontName(),
                        Font.PLAIN, lblNamedSelection.getFont().getSize()));
                if (value.getHotkey() != null) {
                    lblShortcutKey.setText("<Ctrl-" + value.getHotkey() + ">");
                    lblShortcutKey.setVisible(true);
                    lblShortcutText.setVisible(true);
                } else {
                    lblShortcutKey.setText("");
                    lblShortcutKey.setVisible(false);
                    lblShortcutText.setVisible(false);
                }
            }

            if (isSelected) { // Change the color if the named selection has been selected:
                super.setBackground(list.getSelectionBackground());
                super.setLockedStatusSelected(value.isLocked());
                lblNamedSelection.setForeground(list.getSelectionForeground());
                lblShortcutKey.setForeground(list.getSelectionForeground());
                lblShortcutText.setForeground(list.getSelectionForeground());
            } else { // Change coloring for non selected items:
                super.setBackground(list.getBackground());
                super.setLockedStatus(value.isLocked());
                lblNamedSelection.setForeground(list.getForeground());
                lblShortcutKey.setForeground(list.getSelectionBackground()); // Make the shortcut key a contrasting color.
                lblShortcutText.setForeground(java.awt.SystemColor.controlDkShadow); // Less prominent than selection name.
            }

            return this;
        }
    }

    /**
     * Custom list model that is able to handle and store
     * <code>NamedSelection</code>s.
     *
     * @see AbstractListModel
     * @see NamedSelection
     */
    private class NamedSelectionListModel extends AbstractListModel<NamedSelection> {

        private final ArrayList<NamedSelection> selections = new ArrayList<>();

        @Override
        public int getSize() {
            return selections.size();
        }

        @Override
        public NamedSelection getElementAt(final int index) {
            return selections.get(index);
        }

        /**
         * Add a named selection to this list.
         *
         * @param selection The selection that is to be inserted.
         */
        public void addElement(final NamedSelection selection) {
            selections.add(selection);
            fireContentsChanged(this, (selections.size() - 1), (selections.size() - 1));
        }

        /**
         * Remove the named selection at the given index.
         *
         * @param index The index of the named selection that is to be removed.
         */
        public void removeElementAt(final int index) {
            selections.remove(index);
            fireContentsChanged(this, 0, (selections.size() - 1));
        }

        /**
         * Remove the named selection that matches the 'prototype' named
         * selection.
         *
         * @param selection The named selection that acts as a prototype for
         * removal.
         */
        public void removeElement(final NamedSelection selection) {
            final int index = selections.indexOf(selection);
            selections.remove(selection);
            fireContentsChanged(this, index, index);
        }

        /**
         * Causes the entire contents of the list to be cleared.
         */
        public void clearNamedSelections() {
            selections.clear();
            fireContentsChanged(this, 0, 0);
        }
    }
}
