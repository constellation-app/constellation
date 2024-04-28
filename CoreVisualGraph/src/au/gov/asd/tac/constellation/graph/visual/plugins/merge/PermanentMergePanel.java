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
package au.gov.asd.tac.constellation.graph.visual.plugins.merge;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * This class is the main panel for the Copy Attributes function.
 *
 * @author altair
 */
public final class PermanentMergePanel extends JPanel {

    private static final String NUM_NODES_STR = "Number of Selected Nodes: ";

    private final Graph graph;
    private final int primaryNode;
    private final ArrayList<Integer> nodeList;
    private ArrayList<Attribute> nodeAttrbiutes;
    private PermanentMergeTableModel tableModel;
    private HashMap<Integer, Integer> selectedAttributes;
    private static final String SELECTED_COLUMN = "SELECTED_COLUMN_FLAG";
    private static final String NODE_ID_COLUMN = "ID_";

    /**
     * constructor
     *
     * @param graph the graph that holds the vertices to be merged.
     * @param nodeSelections a list of vertices to be merged.
     * @param vxId the id of the vertex to be the surviving vertex.
     */
    public PermanentMergePanel(final Graph graph, final ArrayList<Integer> nodeSelections, final int vxId) {
        initComponents();
        this.graph = graph;
        nodeList = nodeSelections;
        primaryNode = vxId;
        selectedAttributes = new HashMap<>();
        populateTable();
        nodeTable.setDefaultRenderer(String.class, new AttributeCellRenderer(this));
        nodeTable.getTableHeader().setPreferredSize(new Dimension(nodeTable.getColumnModel().getTotalColumnWidth(), 40));
        setupSelectedAttributes();
    }

    public void setParameterValues(final PluginParameters params) {
        final Map<String, PluginParameter<?>> pmap = params.getParameters();
        pmap.get(PermanentMergePlugin.PRIMARY_NODE_PARAMETER_ID).setIntegerValue(primaryNode);
        pmap.get(PermanentMergePlugin.SELECTED_NODES_PARAMETER_ID).setObjectValue(getSelectedVertices());
        pmap.get(PermanentMergePlugin.ATTTRIBUTES_PARAMETER_ID).setObjectValue(getAttributes());
    }

    /**
     * this method will generate a list of the attribute columns (for a given
     * element type) and display them in the 'from' listing
     *
     * @param type a graph element type
     */
    private void populateTable() {
        this.getVertexAttributes();
        tableModel = (PermanentMergeTableModel) nodeTable.getModel();
        tableModel.initialise(nodeAttrbiutes);
        this.setupVertexData();
        this.includeAllVertices();
    }

    /**
     * this method will setup the object that gathers the selected attributes
     * for the merged node
     */
    private void setupSelectedAttributes() {
        selectedAttributes = new HashMap<>();
        processCellSelection(0, 1);
    }

    /**
     * method used to process the selected cell and update the set of selected
     * attributes
     *
     * @return ArrayList
     */
    private void processCellSelection(final int row, final int column) {
        if (column == 1) {
            for (int i = 2; i < tableModel.getColumnCount(); i++) {
                selectedAttributes.put(i, row);
                setColumnHeader(row, i);
            }
        } else if (column >= 2) {
            selectedAttributes.put(column, row);
            setColumnHeader(row, column);
        } else {
            // Do nothing
        }
        nodeTable.updateUI();
    }

    /**
     * return indicator as to whether cell has been selected
     *
     * @param row integer
     * @param column integer
     * @return boolean
     */
    public boolean isCellSelected(final int row, final int column) {
        return (selectedAttributes.containsKey(column) && (selectedAttributes.get(column) == row));
    }

    /**
     * set the column header value
     *
     * @param row integer
     * @param column integer
     */
    private void setColumnHeader(final int row, final int column) {
        if (column >= 2) {
            final String field = tableModel.getColumnName(column);
            String value = (String) (tableModel.getValueAt(row, column));
            if (value == null) {
                value = "";
            }
            if (value.length() > 0) {
                nodeTable.getColumnModel().getColumn(column).setHeaderValue("<html>" + field + "<br>(" + value + ")");
            } else {
                nodeTable.getColumnModel().getColumn(column).setHeaderValue(field);
            }
        }
    }

    /**
     * this method will collect the set of vertex attributes
     *
     */
    private void getVertexAttributes() {
        final ReadableGraph rg = graph.getReadableGraph();
        try {
            int attrCount = rg.getAttributeCount(GraphElementType.VERTEX);
            nodeAttrbiutes = new ArrayList<>();

            for (int i = 0; i < attrCount; i++) {
                nodeAttrbiutes.add(new GraphAttribute(rg, rg.getAttribute(GraphElementType.VERTEX, i)));
            }
        } finally {
            rg.release();
        }

        Collections.sort(nodeAttrbiutes, new AttributeComparator());
        nodeAttrbiutes.add(0, new GraphAttribute(GraphElementType.VERTEX, NODE_ID_COLUMN, NODE_ID_COLUMN, NODE_ID_COLUMN));
        nodeAttrbiutes.add(0, new GraphAttribute(GraphElementType.VERTEX, SELECTED_COLUMN, SELECTED_COLUMN, SELECTED_COLUMN));
    }

    /**
     * Updates the tabular display to display the contents of the graph element
     * that the panel is responsible for.
     */
    public void setupVertexData() {
        final ReadableGraph rg = graph.getReadableGraph();
        try {
            if (primaryNode != Graph.NOT_FOUND) {
                tableModel.addRow(populateTableRow(rg, primaryNode));
            }

            for (int j = 0; j < nodeList.size(); j++) {
                if (primaryNode != nodeList.get(j)) {
                    tableModel.addRow(populateTableRow(rg, nodeList.get(j)));
                }
            }
        } finally {
            rg.release();
        }
        nodeTable.updateUI();
    }

    /**
     * populate table row with attributes for the specified row
     *
     * @param vxId node id
     */
    private Object[] populateTableRow(final ReadableGraph graph, final int vxId) {
        final Object[] row = new Object[nodeAttrbiutes.size() + 1];
        row[0] = true;
        row[1] = Integer.toString(vxId);

        for (int i = 2; i < nodeAttrbiutes.size(); i++) {
            Attribute attr = nodeAttrbiutes.get(i);
            if (!attr.getName().equalsIgnoreCase(SELECTED_COLUMN)) {
                row[i] = graph.getStringValue(attr.getId(), vxId);
            }
        }
        return row;
    }

    /**
     * sett he label showing the number of selected nodes
     */
    private void setSelectedNodeCountLabel(final int count) {
        selectedNodesLabel.setText(NUM_NODES_STR + count);
    }

    /**
     * include all vertices in the merge
     */
    private void udpateSelectedNodeCount() {
        int count = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if ((Boolean) tableModel.getValueAt(i, 0)) {
                count++;
            }
        }
        this.setSelectedNodeCountLabel(count);
    }

    /**
     * include all vertices in the merge
     */
    private void includeAllVertices() {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt(true, i, 0);
        }
        this.setSelectedNodeCountLabel(tableModel.getRowCount());
    }

    /**
     * exclude all vertices in the merge
     */
    private void excludeAllVertices() {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt(false, i, 0);
        }
        this.setSelectedNodeCountLabel(0);
    }

    /**
     * method used by the parent class to return the set of selected attributes
     *
     * @return ArrayList
     */
    public HashMap<Integer, String> getAttributes() {
        final HashMap<Integer, String> list = new HashMap<>();
        for (int i = 2; i < tableModel.getColumnCount(); i++) {
            Object value = tableModel.getValueAt(selectedAttributes.get(i), i);
            if (value == null) {
                value = "";
            }
            list.put(nodeAttrbiutes.get(i).getId(), value.toString());
        }
        return list;
    }

    /**
     * method used by the parent class to return the set of selected vertices
     *
     * @return ArrayList
     */
    public ArrayList<Integer> getSelectedVertices() {
        final ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if ((Boolean) tableModel.getValueAt(i, 0)) {
                final Integer key = Integer.parseInt((String) (tableModel.getValueAt(i, 1)));
                list.add(key);
            }
        }
        return list;
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

        jScrollPane3 = new javax.swing.JScrollPane();
        nodeTable = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        includeAllButton = new javax.swing.JButton();
        excludeAllButton = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        selectedNodesLabel = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(1200, 500));
        setLayout(new java.awt.GridBagLayout());

        nodeTable.setModel(new PermanentMergeTableModel());
        nodeTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        nodeTable.setCellSelectionEnabled(true);
        nodeTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        nodeTable.getTableHeader().setReorderingAllowed(false);
        nodeTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                nodeTableMouseClicked(evt);
            }
        });
        nodeTable.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                nodeTablePropertyChange(evt);
            }
        });
        jScrollPane3.setViewportView(nodeTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane3, gridBagConstraints);

        jPanel2.setLayout(new java.awt.BorderLayout());

        includeAllButton.setText(org.openide.util.NbBundle.getMessage(PermanentMergePanel.class, "PermanentMergePanel.includeAllButton.text")); // NOI18N
        includeAllButton.setActionCommand(org.openide.util.NbBundle.getMessage(PermanentMergePanel.class, "PermanentMergePanel.includeAllButton.text")); // NOI18N
        includeAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                includeAllButtonActionPerformed(evt);
            }
        });
        jPanel3.add(includeAllButton);

        excludeAllButton.setText(org.openide.util.NbBundle.getMessage(PermanentMergePanel.class, "PermanentMergePanel.excludeAllButton.text")); // NOI18N
        excludeAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                excludeAllButtonActionPerformed(evt);
            }
        });
        jPanel3.add(excludeAllButton);

        jPanel2.add(jPanel3, java.awt.BorderLayout.WEST);

        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        selectedNodesLabel.setText(org.openide.util.NbBundle.getMessage(PermanentMergePanel.class, "PermanentMergePanel.selectedNodesLabel.text")); // NOI18N
        jPanel4.add(selectedNodesLabel);

        jPanel2.add(jPanel4, java.awt.BorderLayout.EAST);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jPanel2, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    private void includeAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_includeAllButtonActionPerformed
        this.includeAllVertices();
    }//GEN-LAST:event_includeAllButtonActionPerformed

    private void excludeAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_excludeAllButtonActionPerformed
        this.excludeAllVertices();
    }//GEN-LAST:event_excludeAllButtonActionPerformed

    private void nodeTablePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_nodeTablePropertyChange
        this.udpateSelectedNodeCount();
    }//GEN-LAST:event_nodeTablePropertyChange

    private void nodeTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_nodeTableMouseClicked
        this.processCellSelection(nodeTable.getSelectedRow(), nodeTable.getSelectedColumn());
    }//GEN-LAST:event_nodeTableMouseClicked
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton excludeAllButton;
    private javax.swing.JButton includeAllButton;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable nodeTable;
    private javax.swing.JLabel selectedNodesLabel;
    // End of variables declaration//GEN-END:variables

    private class AttributeComparator implements Comparator<Attribute> {

        @Override
        public int compare(final Attribute a1, final Attribute a2) {
            return a1.getName().compareToIgnoreCase(a2.getName());
        }
    }

    private class AttributeCellRenderer extends DefaultTableCellRenderer {

        private final PermanentMergePanel panel;

        public AttributeCellRenderer(final PermanentMergePanel item) {
            super();
            panel = item;
        }

        @Override
        public Component getTableCellRendererComponent(final JTable table, final Object value,
                final boolean isSelected, final boolean hasFocus,
                final int row, final int column) {
            final Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (panel.isCellSelected(row, column)) {
                cell.setForeground(table.getSelectionForeground());
                cell.setBackground(table.getSelectionBackground());
            } else {
                cell.setForeground(table.getForeground());
                cell.setBackground(table.getBackground());

            }
            return cell;
        }
    }
}
