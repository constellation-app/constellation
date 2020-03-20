/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.tableview;

import au.gov.asd.tac.constellation.views.tableview.GraphTableModel.AttributeSegment;
import au.gov.asd.tac.constellation.views.tableview.GraphTableModel.Segment;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.awt.Color;
import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.openide.util.Exceptions;

/**
 * This panel appears as a dialog when the user right clicks on the table view
 * and chooses "Copy Columns..."
 *
 * @author twinkle2_little
 */
public class ColumnSelectPanel extends javax.swing.JPanel {

    private final DefaultListModel<AttributeSegment> availableColumnModel;
    private final DefaultListModel<AttributeSegment> selectedColumnModel;
    private JTable table;

    /**
     * Constructor for the column selection panel.
     *
     * the parent window can be accessed
     *
     * @param table the table for the ColumnSelectPanel.
     */
    public ColumnSelectPanel(final JTable table) {
        initComponents();
        availableColumnModel = new DefaultListModel();
        selectedColumnModel = new DefaultListModel();
        availableColumnList.setModel(availableColumnModel);
        selectedColumnList.setModel(selectedColumnModel);

        final SegAttrRenderer segAttrRenderer = new SegAttrRenderer();
        availableColumnList.setCellRenderer(segAttrRenderer);
        selectedColumnList.setCellRenderer(segAttrRenderer);

        selectAllButton.setText("");
        selectAllButton.setIcon(UserInterfaceIconProvider.CHEVRON_RIGHT_DOUBLE.buildIcon(16, ConstellationColor.BLACK.getJavaColor()));
        deselectAllButton.setText("");
        deselectAllButton.setIcon(UserInterfaceIconProvider.CHEVRON_LEFT_DOUBLE.buildIcon(16, ConstellationColor.BLACK.getJavaColor()));
        selectButton.setText("");
        selectButton.setIcon(UserInterfaceIconProvider.CHEVRON_RIGHT.buildIcon(16, ConstellationColor.BLACK.getJavaColor()));
        deselectButton.setText("");
        deselectButton.setIcon(UserInterfaceIconProvider.CHEVRON_LEFT.buildIcon(16, ConstellationColor.BLACK.getJavaColor()));
        moveDownButton.setText("");
        moveDownButton.setIcon(UserInterfaceIconProvider.CHEVRON_DOWN.buildIcon(16, ConstellationColor.BLACK.getJavaColor()));
        moveUpButton.setText("");
        moveUpButton.setIcon(UserInterfaceIconProvider.CHEVRON_UP.buildIcon(16, ConstellationColor.BLACK.getJavaColor()));
        setModelData(table);
        updateButtonStatus();
    }

    /**
     * Provides this dialog class with the table data from the main window,
     * allowing access to the data.
     *
     * @param table The JTable from the main window
     */
    private void setModelData(final JTable table) {
        this.table = table;

        // Just tracking names is no good.
        // For the transaction table, we need to keep track of the Segment
        // to differentiate between source and destination vertices.
        final TableColumnModel columnModel = table.getColumnModel();
        final GraphTableModel gtModel = (GraphTableModel) table.getModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            final TableColumn tc = columnModel.getColumn(i);
            final int modelIndex = tc.getModelIndex();
            final AttributeSegment attrseg = gtModel.getAttributeSegment(modelIndex);
            availableColumnModel.addElement(attrseg);
        }
    }

    /**
     * Render a list cell to look like a table header with the correct segment
     * color.
     */
    private static class SegAttrRenderer extends JLabel implements ListCellRenderer<AttributeSegment> {

        @Override
        public Component getListCellRendererComponent(final JList<? extends AttributeSegment> list, final AttributeSegment value, final int index, final boolean isSelected, final boolean cellHasFocus) {
            setOpaque(true);
            setText(value.attr.getName());
            final Color bg;
            if (value.segment == Segment.TX) {
                setIcon(TableViewTopComponent.TX_ICON);
                bg = TableViewTopComponent.TX_BG;
            } else {
                setIcon(TableViewTopComponent.VX_ICON);
                bg = value.segment == Segment.VX_SRC ? TableViewTopComponent.VX_SRC_BG : TableViewTopComponent.VX_DST_BG;
            }

            if (isSelected) {
                setForeground(Color.WHITE);
                setBackground(Color.BLUE);
            } else {
                setForeground(Color.BLACK);
                setBackground(bg);
            }

            return this;
        }
    }

    /**
     * Updates the status of buttons based on what is available on the lists.
     */
    private void updateButtonStatus() {
        if (selectedColumnModel.isEmpty()) {
            deselectAllButton.setEnabled(false);
            deselectButton.setEnabled(false);

        } else if (selectedColumnList.getSelectedIndex() != -1) {
            deselectButton.setEnabled(true);
            deselectAllButton.setEnabled(true);
        } else {
            deselectAllButton.setEnabled(true);
        }
        //only enable up and down arrow if there are more than 1 element on the selected side AND only 1 element is currently selected
        final boolean isOneColumnSelected = (selectedColumnList.getSelectedIndices().length == 1 && selectedColumnModel.size() > 1);
        moveDownButton.setEnabled(isOneColumnSelected);
        moveUpButton.setEnabled(isOneColumnSelected);

        //if selected is already on top disable up arrow
        if (selectedColumnList.getSelectedIndex() == 0) {
            moveUpButton.setEnabled(false);
        }
        //if selected is already on the lowest index disable down arrow
        if (selectedColumnList.getSelectedIndex() == selectedColumnModel.size() - 1) {
            moveDownButton.setEnabled(false);
        }

        if (availableColumnModel.isEmpty()) {
            selectAllButton.setEnabled(false);
            selectButton.setEnabled(false);
        } else if (availableColumnList.getSelectedIndex() != -1) {
            selectButton.setEnabled(true);
            selectAllButton.setEnabled(true);

        } else {
            selectAllButton.setEnabled(true);
        }

        if (availableColumnList.getSelectedIndex() == -1) {
            selectButton.setEnabled(false);
        }
        if (selectedColumnList.getSelectedIndex() == -1) {
            deselectButton.setEnabled(false);
        }
    }

    /**
     * The actual copying occurs here. Depending on whether selectRowCheckBox is
     * selected or not, if it is it will only copy selected rows otherwise all
     * rows will be copied. The dialog window closes at the end of this method.
     *
     * @param evt the ActionEvent.
     */
    public void okButtonActionPerformed(final java.awt.event.ActionEvent evt) {
        if (selectedColumnModel.isEmpty()) {
            return;
        }

        final ArrayList<AttributeSegment> selectedColumnAttrSegs = new ArrayList<>();
        for (int i = 0; i < selectedColumnModel.size(); i++) {
            selectedColumnAttrSegs.add(selectedColumnModel.get(i));
        }

        final CopyDataToClipboard entry = new CopyDataToClipboard("name", table);
        try {
            if (selectRowCheckBox.isSelected()) {
                entry.processSelectedRows(selectedColumnAttrSegs, includeHeaderCheckbox.isSelected());
            } else {
                entry.processAllRows(selectedColumnAttrSegs, includeHeaderCheckbox.isSelected());
            }
        } catch (final IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        selectedColumnLabel1 = new javax.swing.JLabel();
        availableColumnLabel1 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        selectedColumnList = new javax.swing.JList();
        selectRowCheckBox = new javax.swing.JCheckBox();
        jScrollPane4 = new javax.swing.JScrollPane();
        availableColumnList = new javax.swing.JList();
        jPanel2 = new javax.swing.JPanel();
        selectAllButton = new javax.swing.JButton();
        selectButton = new javax.swing.JButton();
        deselectButton = new javax.swing.JButton();
        deselectAllButton = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        moveDownButton = new javax.swing.JButton();
        moveUpButton = new javax.swing.JButton();
        includeHeaderCheckbox = new javax.swing.JCheckBox();

        setPreferredSize(new java.awt.Dimension(386, 354));
        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        layout.columnWidths = new int[] {4};
        layout.rowHeights = new int[] {1};
        setLayout(layout);

        java.awt.GridBagLayout jPanel1Layout = new java.awt.GridBagLayout();
        jPanel1Layout.columnWidths = new int[] {4};
        jPanel1Layout.rowHeights = new int[] {1};
        jPanel1.setLayout(jPanel1Layout);

        org.openide.awt.Mnemonics.setLocalizedText(selectedColumnLabel1, org.openide.util.NbBundle.getMessage(ColumnSelectPanel.class, "ColumnSelectPanel.selectedColumnLabel1.text")); // NOI18N
        selectedColumnLabel1.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(selectedColumnLabel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(availableColumnLabel1, org.openide.util.NbBundle.getMessage(ColumnSelectPanel.class, "ColumnSelectPanel.availableColumnLabel1.text")); // NOI18N
        availableColumnLabel1.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(availableColumnLabel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        add(jPanel1, gridBagConstraints);

        jScrollPane3.setPreferredSize(new java.awt.Dimension(0, 0));
        jScrollPane3.setRequestFocusEnabled(false);

        selectedColumnList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                selectedColumnListMouseClicked(evt);
            }
        });
        selectedColumnList.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                selectedColumnListFocusGained(evt);
            }
        });
        selectedColumnList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                selectedColumnListKeyReleased(evt);
            }
        });
        jScrollPane3.setViewportView(selectedColumnList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 10);
        add(jScrollPane3, gridBagConstraints);

        selectRowCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(selectRowCheckBox, org.openide.util.NbBundle.getMessage(ColumnSelectPanel.class, "ColumnSelectPanel.selectRowCheckBox.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
        add(selectRowCheckBox, gridBagConstraints);

        jScrollPane4.setPreferredSize(new java.awt.Dimension(0, 0));

        availableColumnList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                availableColumnListMouseClicked(evt);
            }
        });
        availableColumnList.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                availableColumnListFocusGained(evt);
            }
        });
        availableColumnList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                availableColumnListKeyReleased(evt);
            }
        });
        jScrollPane4.setViewportView(availableColumnList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 10);
        add(jScrollPane4, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        selectAllButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/au/gov/asd/tac/constellation/views/tableview/right-right-arrow.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(selectAllButton, org.openide.util.NbBundle.getMessage(ColumnSelectPanel.class, "ColumnSelectPanel.selectAllButton.text")); // NOI18N
        selectAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel2.add(selectAllButton, gridBagConstraints);

        selectButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/au/gov/asd/tac/constellation/views/tableview/right-arrow.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(selectButton, org.openide.util.NbBundle.getMessage(ColumnSelectPanel.class, "ColumnSelectPanel.selectButton.text")); // NOI18N
        selectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        jPanel2.add(selectButton, gridBagConstraints);

        deselectButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/au/gov/asd/tac/constellation/views/tableview/left-arrow.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(deselectButton, org.openide.util.NbBundle.getMessage(ColumnSelectPanel.class, "ColumnSelectPanel.deselectButton.text")); // NOI18N
        deselectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deselectButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        jPanel2.add(deselectButton, gridBagConstraints);

        deselectAllButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/au/gov/asd/tac/constellation/views/tableview/left-left-arrow.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(deselectAllButton, org.openide.util.NbBundle.getMessage(ColumnSelectPanel.class, "ColumnSelectPanel.deselectAllButton.text")); // NOI18N
        deselectAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deselectAllButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        jPanel2.add(deselectAllButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        add(jPanel2, gridBagConstraints);

        java.awt.GridBagLayout jPanel5Layout = new java.awt.GridBagLayout();
        jPanel5Layout.columnWidths = new int[] {1};
        jPanel5Layout.rowHeights = new int[] {2};
        jPanel5.setLayout(jPanel5Layout);

        moveDownButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/au/gov/asd/tac/constellation/views/tableview/down-arrow.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(moveDownButton, org.openide.util.NbBundle.getMessage(ColumnSelectPanel.class, "ColumnSelectPanel.moveDownButton.text")); // NOI18N
        moveDownButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveDownButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
        jPanel5.add(moveDownButton, gridBagConstraints);

        moveUpButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/au/gov/asd/tac/constellation/views/tableview/up-arrow.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(moveUpButton, org.openide.util.NbBundle.getMessage(ColumnSelectPanel.class, "ColumnSelectPanel.moveUpButton.text")); // NOI18N
        moveUpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveUpButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 10);
        jPanel5.add(moveUpButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weighty = 1.0;
        add(jPanel5, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(includeHeaderCheckbox, org.openide.util.NbBundle.getMessage(ColumnSelectPanel.class, "ColumnSelectPanel.includeHeaderCheckbox.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(includeHeaderCheckbox, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * The behavior after the select all button has been clicked. Moves all
     * elements from available to selected. Selected list is sorted after this
     * operation.
     *
     * @param evt
     */
    private void selectAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllButtonActionPerformed
        for (int i = 0; i < availableColumnModel.size(); i++) {
            selectedColumnModel.addElement(availableColumnModel.get(i));

        }
        availableColumnModel.clear();
        updateButtonStatus();
    }//GEN-LAST:event_selectAllButtonActionPerformed

    /**
     * The behavior after the select all button has been click. Moves selected
     * elements from available to selected. No sorting occurs after this
     * operation.
     *
     * @param evt
     */
    private void selectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectButtonActionPerformed
        int[] selectedIndices = availableColumnList.getSelectedIndices();
        ArrayList<AttributeSegment> movedObject = new ArrayList<>();
        for (int i : selectedIndices) {
            movedObject.add(availableColumnModel.get(i));
        }
        for (final AttributeSegment o : movedObject) {
            selectedColumnModel.addElement(o);
            availableColumnModel.removeElement(o);
        }
        updateButtonStatus();
    }//GEN-LAST:event_selectButtonActionPerformed

    /**
     * The behavior after the deselect button has been clicked. Moves selected
     * elements from selected to available. Available list is sorted after this
     * operation.
     *
     * @param evt
     */
    private void deselectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deselectButtonActionPerformed
        int[] selectedIndices = selectedColumnList.getSelectedIndices();
        ArrayList<AttributeSegment> movedObject = new ArrayList<>();
        for (int i : selectedIndices) {
            movedObject.add(selectedColumnModel.get(i));
        }
        for (final AttributeSegment o : movedObject) {
            availableColumnModel.addElement(o);
            selectedColumnModel.removeElement(o);
        }
        updateButtonStatus();
    }//GEN-LAST:event_deselectButtonActionPerformed

    /**
     * The behavior after the deselect all button has been click. Moves all
     * selected elements from selected to available. Available list is sorted
     * after this operation.
     *
     * @param evt
     */
    private void deselectAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deselectAllButtonActionPerformed
        for (int i = 0; i < selectedColumnModel.size(); i++) {
            availableColumnModel.addElement(selectedColumnModel.get(i));
        }
        selectedColumnModel.clear();
        updateButtonStatus();
    }//GEN-LAST:event_deselectAllButtonActionPerformed

    /**
     * Reorders the selected column list. The selected element is shifted one
     * position down. This only works if only 1 element is selected.
     *
     * @param evt
     */
    private void moveDownButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownButtonActionPerformed
        int[] selectedIndices = selectedColumnList.getSelectedIndices();
        //only move order when one item is selected on the selected jlist.
        if (selectedIndices.length == 1) {
            int selectedIndex = selectedIndices[0];
            // This 'object' is an AttributeSegment; the toArray cannot output a string array.
            Object[] reorderedList = selectedColumnModel.toArray();
            //i.e. first item is selected then do nothing because it can't move any further up
            if (selectedIndex >= reorderedList.length - 1) {
                return;
            }
            Object tmp = reorderedList[selectedIndex + 1];
            reorderedList[selectedIndex + 1] = reorderedList[selectedIndex];
            reorderedList[selectedIndex] = tmp;
            selectedColumnModel.clear();
            for (Object o : reorderedList) {
                selectedColumnModel.addElement((AttributeSegment) o);
            }
            selectedColumnList.setSelectedIndex(selectedIndex + 1);
        }
        updateButtonStatus();
    }//GEN-LAST:event_moveDownButtonActionPerformed

    /**
     * Reorders the selected column list. The selected element is shifted one
     * position up. This only works if only 1 element is selected.
     *
     * @param evt
     */
    private void moveUpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpButtonActionPerformed
        int[] selectedIndices = selectedColumnList.getSelectedIndices();
        //only move order when one item is selected on the selected jlist.
        if (selectedIndices.length == 1) {
            int selectedIndex = selectedIndices[0];
            // This 'object' is a AttributeSegment; the toArray cannot output a string array.
            Object[] reorderedList = selectedColumnModel.toArray();
            //i.e. first item is selected then do nothing because it can't move any further up
            if (selectedIndex <= 0) {
                return;
            }
            Object tmp = reorderedList[selectedIndex - 1];
            reorderedList[selectedIndex - 1] = reorderedList[selectedIndex];
            reorderedList[selectedIndex] = tmp;
            selectedColumnModel.clear();
            for (Object o : reorderedList) {
                selectedColumnModel.addElement((AttributeSegment) o);
            }
            selectedColumnList.setSelectedIndex(selectedIndex - 1);
        }
        updateButtonStatus();

    }//GEN-LAST:event_moveUpButtonActionPerformed

    private void availableColumnListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_availableColumnListMouseClicked
        updateButtonStatus();
    }//GEN-LAST:event_availableColumnListMouseClicked

    private void selectedColumnListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_selectedColumnListMouseClicked
        updateButtonStatus();
    }//GEN-LAST:event_selectedColumnListMouseClicked

    private void selectedColumnListKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_selectedColumnListKeyReleased
        updateButtonStatus();
    }//GEN-LAST:event_selectedColumnListKeyReleased

    private void availableColumnListKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_availableColumnListKeyReleased
        updateButtonStatus();
    }//GEN-LAST:event_availableColumnListKeyReleased

    private void availableColumnListFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_availableColumnListFocusGained
        updateButtonStatus();
    }//GEN-LAST:event_availableColumnListFocusGained

    private void selectedColumnListFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_selectedColumnListFocusGained
        updateButtonStatus();
    }//GEN-LAST:event_selectedColumnListFocusGained
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel availableColumnLabel1;
    private javax.swing.JList availableColumnList;
    private javax.swing.JButton deselectAllButton;
    private javax.swing.JButton deselectButton;
    private javax.swing.JCheckBox includeHeaderCheckbox;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JButton moveDownButton;
    private javax.swing.JButton moveUpButton;
    private javax.swing.JButton selectAllButton;
    private javax.swing.JButton selectButton;
    private javax.swing.JCheckBox selectRowCheckBox;
    private javax.swing.JLabel selectedColumnLabel1;
    private javax.swing.JList selectedColumnList;
    // End of variables declaration//GEN-END:variables
}
