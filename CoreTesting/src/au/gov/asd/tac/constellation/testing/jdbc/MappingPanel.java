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
package au.gov.asd.tac.constellation.testing.jdbc;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.file.io.GraphFileConstants;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author algol
 */
public class MappingPanel extends javax.swing.JPanel {

    private JdbcData data;
    final boolean isExporting;
    final ArrayList<String> vxAttrLabels;
    final ArrayList<String> txAttrLabels;
    final String direction;

    public MappingPanel(final Graph graph, final boolean isExporting) {
        initComponents();

        data = null;

        final ReadableGraph rg = graph.getReadableGraph();
        try {
            vxAttrLabels = getAttributes(rg, GraphElementType.VERTEX, isExporting);
            txAttrLabels = getAttributes(rg, GraphElementType.TRANSACTION, isExporting);
        } finally {
            rg.release();
        }

        this.isExporting = isExporting;
        direction = isExporting ? "←" : "→";
    }

    /**
     * Validate the column .. attribute mappings.
     *
     * @return Null if the mappings are valid, or an informational message if
     * not.
     */
    public String valid() {
        final String[][] vxValues = ((MappingTableModel) vxTable.getModel()).values;
        boolean foundVxIdColumn = false;
        for (int i = 0; i < vxValues[0].length; i++) {
            final String attrLabel = vxValues[1][i];
            if (GraphFileConstants.VX_ID.equals(attrLabel) && vxValues[0][i] != null && !vxValues[0][i].isEmpty()) {
                foundVxIdColumn = true;
                break;
            }
        }

        if (!foundVxIdColumn) {
            return String.format("No mapping found for vertex id (%s)", GraphFileConstants.VX_ID);
        }

        final String[][] txValues = ((MappingTableModel) txTable.getModel()).values;
        boolean foundTxIdColumn = false;
        boolean foundTxSrcColumn = false;
        boolean foundTxDstColumn = false;
        for (int i = 0; i < txValues[0].length; i++) {
            final String attrLabel = txValues[1][i];
            if (GraphFileConstants.TX_ID.equals(attrLabel) && txValues[0][i] != null && !txValues[0][i].isEmpty()) {
                foundTxIdColumn = true;
            }

            if (GraphFileConstants.SRC.equals(attrLabel) && txValues[0][i] != null && !txValues[0][i].isEmpty()) {
                foundTxSrcColumn = true;
            }

            if (GraphFileConstants.DST.equals(attrLabel) && txValues[0][i] != null && !txValues[0][i].isEmpty()) {
                foundTxDstColumn = true;
            }
        }

        if (!foundTxIdColumn) {
            return String.format("No mapping found for transaction id (%s)", GraphFileConstants.TX_ID);
        }

        if (!foundTxSrcColumn) {
            return String.format("No mapping found for transaction source vertex (%s)", GraphFileConstants.SRC);
        }

        if (!foundTxDstColumn) {
            return String.format("No mapping found for transaction destination vertex (%s)", GraphFileConstants.DST);
        }

        return null;
    }

    private static void setColumn(final JTable table, final int columnIndex, final ArrayList<String> values0) {
        // Copy the list of labels into a String[] with a leading "" as the default "no choice" value.
        final String[] values = new String[values0.size() + 1];
        values[0] = "";
        for (int i = 0; i < values0.size(); i++) {
            values[i + 1] = values0.get(i);
        }

        final TableColumn tcol = table.getColumnModel().getColumn(columnIndex);
        tcol.setCellRenderer(new MappingCellRenderer(values));
        tcol.setCellEditor(new DefaultCellEditor(new JComboBox(values)));
    }

    /**
     * A sorted list of attribute labels from a graph.
     * <p>
     * If exporting, only the attributes actually on the graph need be fetched.
     * <p>
     * If importing, the user should be offered all possible attributes to map
     * to, including schema attributes as well as attributes on he graph.
     *
     * @param rg A GraphReadMethods instance.
     * @param etype The GraphElementType of the required attributes.
     * @param isExporting Determines whether exporting or importing is
     * happening.
     *
     * @return A sorted list of attribute labels from a graph.
     */
    private static ArrayList<String> getAttributes(final GraphReadMethods rg, final GraphElementType etype, final boolean isExporting) {
        final ArrayList<String> labels = new ArrayList<>();

        final Map<String, SchemaAttribute> schemaAttrs;
        if (isExporting || rg.getSchema() == null) {
            schemaAttrs = null;
        } else {
            schemaAttrs = rg.getSchema().getFactory().getRegisteredAttributes(etype);
            labels.addAll(schemaAttrs.keySet());
        }

        final int attrCount = rg.getAttributeCount(etype);
        for (int position = 0; position < attrCount; position++) {
            final Attribute attr = new GraphAttribute(rg, rg.getAttribute(etype, position));

            if (schemaAttrs == null || !schemaAttrs.containsKey(attr.getName())) {
                labels.add(attr.getName());
            }
        }

        labels.sort((final String s1, final String s2) -> s1.compareTo(s2));

        if (etype == GraphElementType.VERTEX) {
            labels.add(GraphFileConstants.VX_ID);
        } else if (etype == GraphElementType.TRANSACTION) {
            labels.add(GraphFileConstants.TX_ID);
            labels.add(GraphFileConstants.SRC);
            labels.add(GraphFileConstants.DST);
            labels.add(GraphFileConstants.DIR);
        }

        return labels;
    }

    void setData(final JdbcData data) {
        this.data = data;
        newModels();
    }

    private void newModels() {
        final MappingTableModel vxModel = new MappingTableModel(data.vxColumns, vxAttrLabels, data.vxMappings, isExporting);
        vxTable.setModel(vxModel);
        setColumn(vxTable, 0, data.vxColumns);
        setColumn(vxTable, 1, vxAttrLabels);

        final MappingTableModel txModel = new MappingTableModel(data.txColumns, txAttrLabels, data.txMappings, isExporting);
        txTable.setModel(txModel);
        setColumn(txTable, 0, data.txColumns);
        setColumn(txTable, 1, txAttrLabels);
    }

    public String[][] getVxModelValues() {
        return ((MappingTableModel) vxTable.getModel()).values;
    }

    public String[][] getTxModelValues() {
        return ((MappingTableModel) txTable.getModel()).values;
    }

    @Override
    public String getName() {
        return "Table column " + direction + " graph attribute mappings";
    }

    public static class MappingTableModel implements TableModel {

        final int len;
        final String[][] values;
//        private final List<TableModelListener> listeners;

        /**
         * Create a new MappingTable model.
         *
         * @param columnLabels The labels of the database columns.
         * @param attrLabels The labels of the graph attributes.
         * @param mappings A 2*n array where [0][i] is a table column and [1][i]
         * is the graph attribute it maps to/from.
         * @param showTableColumns If true, show the table column labels by
         * default, else show the attribute labels by default.
         */
        public MappingTableModel(final ArrayList<String> columnLabels, final ArrayList<String> attrLabels, final String[][] mappings, final boolean showTableColumns) {
            final HashMap<String, String> mapping = new HashMap<>();
            if (mappings != null) {
                final int fromTo = showTableColumns ? 0 : 1;
                for (int i = 0; i < mappings[0].length; i++) {
                    mapping.put(mappings[fromTo][i], mappings[1 - fromTo][i]);
                }
            } else {
                // There are no existing mappings, so attempt to create some reasonable defaults.
                final HashMap<String, String> canonical = new HashMap<>();
                if (showTableColumns) {
                    attrLabels.stream().forEach((label) -> {
                        final String clabel = JdbcUtilities.canonicalLabel(label);
                        canonical.put(clabel, label);
                    });

                    columnLabels.stream().forEach((label) -> {
                        final String clabel = JdbcUtilities.canonicalLabel(label);
                        if (canonical.containsKey(clabel)) {
                            mapping.put(label, canonical.get(clabel));
                        }
                    });
                } else {
                    columnLabels.stream().forEach((label) -> {
                        final String clabel = JdbcUtilities.canonicalLabel(label);
                        canonical.put(clabel, label);
                    });

                    attrLabels.stream().forEach((label) -> {
                        final String clabel = JdbcUtilities.canonicalLabel(label);
                        if (canonical.containsKey(clabel)) {
                            mapping.put(label, canonical.get(clabel));
                        }
                    });
                }
            }

            len = Math.min(columnLabels.size(), attrLabels.size());
            values = new String[2][len];
            for (int i = 0; i < len; i++) {
                final String label0;
                final String label1;
                if (showTableColumns) {
                    label0 = i < columnLabels.size() ? columnLabels.get(i) : "";
                    label1 = !label0.isEmpty() ? mapping.getOrDefault(label0, "") : "";
                } else {
                    label1 = i < attrLabels.size() ? attrLabels.get(i) : "";
                    label0 = !label1.isEmpty() ? mapping.getOrDefault(label1, "") : "";
                }

                values[0][i] = label0;
                values[1][i] = label1;
            }
        }

        @Override
        public int getRowCount() {
            return len;
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(final int columnIndex) {
            return columnIndex == 0 ? "Table columns" : "Graph attributes";
        }

        @Override
        public Class<?> getColumnClass(final int columnIndex) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(final int rowIndex, final int columnIndex) {
            return true;
        }

        @Override
        public Object getValueAt(final int rowIndex, final int columnIndex) {
//            System.out.printf("@@MP getValue at %d %d [%s]%n", rowIndex, columnIndex, values[columnIndex][rowIndex]);
            return values[columnIndex][rowIndex];
        }

        @Override
        public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
//            System.out.printf("@@MP setValue at %d %d [%s]%n", rowIndex, columnIndex, aValue);
            values[columnIndex][rowIndex] = (String) aValue;
        }

        @Override
        public void addTableModelListener(final TableModelListener l) {
        }

        @Override
        public void removeTableModelListener(final TableModelListener l) {
        }
    }

    private static class MappingCellRenderer extends JComboBox<String> implements TableCellRenderer {

        MappingCellRenderer(final String[] items) {
            super(items);
        }

        @Override
        public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
//            System.out.printf("@@MP R %d %d [%s]%n", row, column, value);
            setSelectedItem(value);

            return this;
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

        saveButton = new javax.swing.JButton();
        labelText = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        vxTable = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        txTable = new javax.swing.JTable();
        defaultMappingsButton = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(saveButton, org.openide.util.NbBundle.getMessage(MappingPanel.class, "MappingPanel.saveButton.text")); // NOI18N
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        labelText.setText(org.openide.util.NbBundle.getMessage(MappingPanel.class, "MappingPanel.labelText.text")); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(MappingPanel.class, "MappingPanel.jPanel1.border.title"))); // NOI18N
        jPanel1.setPreferredSize(new java.awt.Dimension(400, 200));

        vxTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(vxTable);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(MappingPanel.class, "MappingPanel.jPanel2.border.title"))); // NOI18N
        jPanel2.setPreferredSize(new java.awt.Dimension(400, 200));

        txTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane4.setViewportView(txTable);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
        );

        org.openide.awt.Mnemonics.setLocalizedText(defaultMappingsButton, org.openide.util.NbBundle.getMessage(MappingPanel.class, "MappingPanel.defaultMappingsButton.text")); // NOI18N
        defaultMappingsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultMappingsButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGap(0, 285, Short.MAX_VALUE)
                        .addComponent(defaultMappingsButton)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(defaultMappingsButton)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelText)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveButton)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(saveButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_saveButtonActionPerformed
    {//GEN-HEADEREND:event_saveButtonActionPerformed
        final String label = labelText.getText().trim();
        if (label.isEmpty()) {
            final NotifyDescriptor nderr = new NotifyDescriptor.Message("A label must be specified for saving", NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nderr);
        } else {
            final MappingTableModel vxModel = (MappingTableModel) vxTable.getModel();
            data.vxMappings = JdbcData.copy(vxModel.values);

            final MappingTableModel txModel = (MappingTableModel) txTable.getModel();
            data.txMappings = JdbcData.copy(txModel.values);

            JdbcParameterIO.saveParameters(data, label);
        }
    }//GEN-LAST:event_saveButtonActionPerformed

    private void defaultMappingsButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_defaultMappingsButtonActionPerformed
    {//GEN-HEADEREND:event_defaultMappingsButtonActionPerformed
        data.vxMappings = null;
        data.txMappings = null;
        newModels();
    }//GEN-LAST:event_defaultMappingsButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton defaultMappingsButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTextField labelText;
    private javax.swing.JButton saveButton;
    private javax.swing.JTable txTable;
    private javax.swing.JTable vxTable;
    // End of variables declaration//GEN-END:variables
}
