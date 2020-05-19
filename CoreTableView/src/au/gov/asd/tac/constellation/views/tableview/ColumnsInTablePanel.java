/*
 * Copyright 2010-2020 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.views.tableview.GraphTableModel.Segment;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.table.TableColumn;

/**
 * Allow the user to select attributes to be shown as table view columns.
 *
 * @author algol
 */
public class ColumnsInTablePanel extends javax.swing.JPanel {

    public ColumnsInTablePanel(final GraphReadMethods rg, final JTable table, final GraphElementType elementType) {
        initComponents();

        final CheckBoxLabelCellRenderer cellRenderer = new CheckBoxLabelCellRenderer();
        vxLabelList.setCellRenderer(cellRenderer);
        vxLabelList.setModel(new ColumnsModel(rg, GraphElementType.VERTEX, elementType == GraphElementType.VERTEX, table));
        setupChecklist(vxLabelList);

        if (elementType == GraphElementType.VERTEX) {
            txLabelList.setEnabled(false);
            txLabelList.setModel(new DefaultListModel<>());
        } else {
            txLabelList.setCellRenderer(cellRenderer);
            txLabelList.setModel(new ColumnsModel(rg, GraphElementType.TRANSACTION, false, table));
            setupChecklist(txLabelList);
        }
    }

    private static void setupChecklist(final JList<CheckListItem<Attribute>> checklist) {
        checklist.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent event) {
                toggleItem(checklist, checklist.locationToIndex(event.getPoint()));
            }
        });
        final KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0);
        final Object mapKey = keyStroke.toString();
        checklist.getInputMap().put(keyStroke, mapKey);
        checklist.getActionMap().put(mapKey, new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                toggleItem(checklist, checklist.getSelectedIndex());
            }
        });
    }

    private static void toggleItem(final JList<CheckListItem<Attribute>> list, final int index) {
        if (index >= 0) {
            final CheckListItem<Attribute> item = list.getModel().getElementAt(index);
            item.isSelected = !item.isSelected;
            list.repaint();
        }
    }

    /**
     * Return the attribute ids of the columns that have been selected by the
     * user.
     * <p>
     * If elementType==VERTEX, just return the selected vertex attributes. If
     * elementType==TRANSACTION, return both the selected transaction and
     * selected vertex attributes.
     *
     * @param rg the operation will be performed using this read lock.
     * @param elementType the element type.
     * @return the attribute ids of the columns that have been selected by the
     * user.
     */
    public List<Integer> getSelectedAttributeIds(final GraphReadMethods rg, final GraphElementType elementType) {
        final JList<CheckListItem<Attribute>> list = elementType == GraphElementType.VERTEX ? vxLabelList : txLabelList;
        final ColumnsModel listModel = (ColumnsModel) list.getModel();
        final ArrayList<Integer> selectedAttrs = new ArrayList<>();
        for (int ix = 0; ix < listModel.getSize(); ix++) {
            if (listModel.getElementAt(ix).isSelected) {
                final Attribute attr = listModel.getAttribute(ix);
                selectedAttrs.add(attr.getId());
            }
        }

        if (elementType == GraphElementType.TRANSACTION) {
            selectedAttrs.addAll(getSelectedAttributeIds(rg, GraphElementType.VERTEX));
        }

        return selectedAttrs;
    }

    private static class ColumnsModel extends AbstractListModel<CheckListItem<Attribute>> {

        private final ArrayList<CheckListItem<Attribute>> labels;

        /**
         *
         * @param rg
         * @param elementType
         * @param includeVxId If this is the transaction table, we don't want
         * the vxId displayed.
         * @param table
         */
        ColumnsModel(final GraphReadMethods rg, final GraphElementType elementType, final boolean includeVxId, final JTable table) {
            final GraphTableModel tm = (GraphTableModel) table.getModel();
            labels = new ArrayList<>();
            final Set<Integer> alreadyHave = new HashSet<>();

            // Which columns are already visible in the table?
            for (int i = 0; i < table.getColumnCount(); i++) {
                final TableColumn tc = table.getColumnModel().getColumn(i);
                final int modelIndex = tc.getModelIndex();
                final Attribute attr = tm.getAttribute(modelIndex);
                final Segment segment = tm.getSegment(modelIndex);

                // If this is the vertex checklist, we only add the vertex attributes.
                // If this is the transaction checklist, we only add the transaction attributes.
                final boolean add = elementType == GraphElementType.VERTEX && segment == Segment.VX_SRC
                        || elementType == GraphElementType.TRANSACTION && segment == Segment.TX;
                if (add) {
                    labels.add(new CheckListItem<>(attr, true));
                    alreadyHave.add(attr.getId());
                }
            }

            // Add the remaining attributes from the graph.
            final int attrCount = rg.getAttributeCount(elementType);
            for (int position = 0; position < attrCount; position++) {
                final int attrId = rg.getAttribute(elementType, position);
                final Attribute attr = new GraphAttribute(rg, attrId);
                if (!alreadyHave.contains(attr.getId())) {
                    labels.add(new CheckListItem<>(attr, false));
                }
            }

            if (elementType == GraphElementType.VERTEX && includeVxId && !alreadyHave.contains(GraphTableModel.VX_ATTR.getId())) {
                labels.add(new CheckListItem<>(GraphTableModel.VX_ATTR, false));
            }

            if (elementType == GraphElementType.TRANSACTION) {
                if (!alreadyHave.contains(GraphTableModel.TX_ATTR.getId())) {
                    labels.add(new CheckListItem<>(GraphTableModel.TX_ATTR, false));
                }
                if (!alreadyHave.contains(GraphTableModel.TX_SRC_ATTR.getId())) {
                    labels.add(new CheckListItem<>(GraphTableModel.TX_SRC_ATTR, false));
                }
                if (!alreadyHave.contains(GraphTableModel.TX_DST_ATTR.getId())) {
                    labels.add(new CheckListItem<>(GraphTableModel.TX_DST_ATTR, false));
                }
            }
        }

        public Attribute getAttribute(final int index) {
            return labels.get(index).item;
        }

        public int indexOf(final String label) {
            return labels.indexOf(label);
        }

        @Override
        public int getSize() {
            return labels.size();
        }

        @Override
        public CheckListItem<Attribute> getElementAt(int index) {
            return labels.get(index);
        }
    }

    protected class CheckBoxLabelCellRenderer extends JCheckBox implements ListCellRenderer<Object> {

        @Override
        public Component getListCellRendererComponent(final JList<? extends Object> list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
            setOpaque(true);
            @SuppressWarnings("unchecked") //value will be an attribute checklist item
            final CheckListItem<Attribute> listItem = (CheckListItem<Attribute>) value;
            setSelected(listItem.isSelected);
            setText(listItem.item.getName());
            setBackground(cellHasFocus ? list.getSelectionBackground() : list.getBackground());
            setForeground(cellHasFocus ? list.getSelectionForeground() : list.getForeground());
            setEnabled(isEnabled());
            setFont(getFont());
            setFocusPainted(false);
            setBorderPainted(true);
            setBorder(list.getBorder());

            return this;
        }
    }

    /**
     * A wrapper for a T item which adds a selected indicator.
     *
     * @param <T> The type of the item in the checklist.
     */
    private static class CheckListItem<T> {

        public final T item;
        private boolean isSelected;

        public CheckListItem(final T item, final boolean isSelected) {
            this.item = item;
            this.isSelected = isSelected;
        }

        @Override
        public String toString() {
            return String.format("[%s %s %s]", CheckListItem.class.getSimpleName(), item.toString(), isSelected);
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

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        vxLabelList = new javax.swing.JList<>();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txLabelList = new javax.swing.JList<>();
        vxAllButton = new javax.swing.JButton();
        vxNoneButton = new javax.swing.JButton();
        txAllButton = new javax.swing.JButton();
        txNoneButton = new javax.swing.JButton();
        vxImportantButton = new javax.swing.JButton();
        txImportantButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ColumnsInTablePanel.class, "ColumnsInTablePanel.jLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 10, 0, 0);
        add(jLabel1, gridBagConstraints);

        jScrollPane1.setViewportView(vxLabelList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 150;
        gridBagConstraints.ipady = 225;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 5);
        add(jScrollPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ColumnsInTablePanel.class, "ColumnsInTablePanel.jLabel2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 5, 0, 0);
        add(jLabel2, gridBagConstraints);

        jScrollPane2.setViewportView(txLabelList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 150;
        gridBagConstraints.ipady = 225;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 10);
        add(jScrollPane2, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(vxAllButton, org.openide.util.NbBundle.getMessage(ColumnsInTablePanel.class, "ColumnsInTablePanel.vxAllButton.text")); // NOI18N
        vxAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vxAllButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        add(vxAllButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(vxNoneButton, org.openide.util.NbBundle.getMessage(ColumnsInTablePanel.class, "ColumnsInTablePanel.vxNoneButton.text")); // NOI18N
        vxNoneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vxNoneButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        add(vxNoneButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(txAllButton, org.openide.util.NbBundle.getMessage(ColumnsInTablePanel.class, "ColumnsInTablePanel.txAllButton.text")); // NOI18N
        txAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txAllButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        add(txAllButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(txNoneButton, org.openide.util.NbBundle.getMessage(ColumnsInTablePanel.class, "ColumnsInTablePanel.txNoneButton.text")); // NOI18N
        txNoneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txNoneButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        add(txNoneButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(vxImportantButton, org.openide.util.NbBundle.getMessage(ColumnsInTablePanel.class, "ColumnsInTablePanel.vxImportantButton.text")); // NOI18N
        vxImportantButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vxImportantButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        add(vxImportantButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(txImportantButton, org.openide.util.NbBundle.getMessage(ColumnsInTablePanel.class, "ColumnsInTablePanel.txImportantButton.text")); // NOI18N
        txImportantButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txImportantButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        add(txImportantButton, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void vxAllButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_vxAllButtonActionPerformed
    {//GEN-HEADEREND:event_vxAllButtonActionPerformed
        setItems(vxLabelList, true, true);
    }//GEN-LAST:event_vxAllButtonActionPerformed

    private void vxNoneButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_vxNoneButtonActionPerformed
    {//GEN-HEADEREND:event_vxNoneButtonActionPerformed
        setItems(vxLabelList, false, true);
    }//GEN-LAST:event_vxNoneButtonActionPerformed

    private void txAllButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_txAllButtonActionPerformed
    {//GEN-HEADEREND:event_txAllButtonActionPerformed
        setItems(txLabelList, true, true);
    }//GEN-LAST:event_txAllButtonActionPerformed

    private void txNoneButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_txNoneButtonActionPerformed
    {//GEN-HEADEREND:event_txNoneButtonActionPerformed
        setItems(txLabelList, false, true);
    }//GEN-LAST:event_txNoneButtonActionPerformed

    private void vxImportantButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_vxImportantButtonActionPerformed
    {//GEN-HEADEREND:event_vxImportantButtonActionPerformed
        setItems(vxLabelList, true, false);
    }//GEN-LAST:event_vxImportantButtonActionPerformed

    private void txImportantButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_txImportantButtonActionPerformed
    {//GEN-HEADEREND:event_txImportantButtonActionPerformed
        setItems(txLabelList, true, false);
    }//GEN-LAST:event_txImportantButtonActionPerformed

    /**
     * Helper class for button events.
     *
     * @param list
     * @param isSelected
     */
    private static void setItems(final JList<CheckListItem<Attribute>> list, final boolean isSelected, final boolean all) {
        final int size = list.getModel().getSize();
        for (int i = 0; i < size; i++) {
            final CheckListItem<Attribute> item = list.getModel().getElementAt(i);
            if (all) {
                item.isSelected = isSelected;
            } else {
                item.isSelected = TableViewTopComponent.isImportant(item.item.getName());
            }
        }

        list.repaint();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton txAllButton;
    private javax.swing.JButton txImportantButton;
    private javax.swing.JList<CheckListItem<Attribute>> txLabelList;
    private javax.swing.JButton txNoneButton;
    private javax.swing.JButton vxAllButton;
    private javax.swing.JButton vxImportantButton;
    private javax.swing.JList<CheckListItem<Attribute>> vxLabelList;
    private javax.swing.JButton vxNoneButton;
    // End of variables declaration//GEN-END:variables
}
