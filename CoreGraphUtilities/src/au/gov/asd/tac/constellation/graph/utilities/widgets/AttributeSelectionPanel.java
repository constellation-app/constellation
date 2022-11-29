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
package au.gov.asd.tac.constellation.graph.utilities.widgets;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import javax.swing.DefaultComboBoxModel;
import javax.swing.border.TitledBorder;

/**
 * A JPanel that allows a user to select a graph attribute.
 *
 * @author algol
 */
public final class AttributeSelectionPanel extends javax.swing.JPanel implements ActionListener {

    public static final String ELEMENT_TYPE_ACTION = "elementtype";
    public static final String ATTRIBUTE_ACTION = "attribute";
    private static final String VERTEX = "Vertex";
    private static final String TRANSACTION = "Transaction";
    private Graph graph;
    private Collection<String> dataTypes;
    private Collection<String> excluded;

    public AttributeSelectionPanel() {
        this(null);
    }

    /**
     * Construct a new AttributeSelectionPanel.
     *
     * @param title The title of the panel.
     */
    public AttributeSelectionPanel(final String title) {
        initComponents();

        attributeCombo.addActionListener(this);

        setTitle(title);
    }

    public void setTitle(final String title) {
        ((TitledBorder) getBorder()).setTitle(title);
    }

    public String getTitle() {
        return ((TitledBorder) getBorder()).getTitle();
    }

    /**
     * Set the attribute selection details.
     * <p>
     * If any of elementTypes, dataTypes, or excluded are null, all options are
     * used (except for dataType "object").
     *
     * @param graph The graph to select an attribute from.
     * @param elementTypes The element types that the user can select from. Can
     * contain GraphElementType.VERTEX or GraphElementType.TRANSACTION.
     * @param dataTypes The data types that the user can select from. Can
     * contain strings returned by Attribute.getType().
     * @param excluded The names of attributes that are to be excluded from the
     * attribute selection.
     */
    public void setGraph(final Graph graph, final Collection<GraphElementType> elementTypes, final Collection<String> dataTypes, final Collection<String> excluded) {
        this.graph = graph;
        this.dataTypes = dataTypes != null ? Collections.unmodifiableCollection(dataTypes) : null;
        this.excluded = excluded != null ? Collections.unmodifiableCollection(excluded) : null;

        final ArrayList<String> et = new ArrayList<>();
        if (elementTypes == null || elementTypes.contains(GraphElementType.VERTEX)) {
            et.add(VERTEX);
        }
        if (elementTypes == null || elementTypes.contains(GraphElementType.TRANSACTION)) {
            et.add(TRANSACTION);
        }
        final String[] elementTypesModel = et.toArray(new String[et.size()]);
        elementTypeCombo.setModel(new DefaultComboBoxModel<>(elementTypesModel));

        setAttributeElements();
    }

    private void setAttributeElements() {
        final GraphElementType graphElementType = getElementType();

        // Always add a blank name so we don't give the user a default attribute.
        final ArrayList<String> attributeNames = new ArrayList<>();
        attributeNames.add("");

        ReadableGraph rg = graph.getReadableGraph();
        try {
            final int attributeCount = rg.getAttributeCount(graphElementType);
            for (int position = 0; position < attributeCount; position++) {
                final int attrId = rg.getAttribute(graphElementType, position);
                final Attribute attr = new GraphAttribute(rg, attrId);
                final boolean isValidType = dataTypes == null || dataTypes.contains(attr.getAttributeType());
                final boolean isNotExcluded = excluded == null || !excluded.contains(attr.getName());
                if (/* !attr.getType().equals("object") && */isValidType && isNotExcluded) {
                    attributeNames.add(attr.getName());
                }
            }
        } finally {
            rg.release();
        }

        // Sort into a consistent order.
        Collections.sort(attributeNames);

        attributeCombo.setModel(new DefaultComboBoxModel<>(attributeNames.toArray(new String[attributeNames.size()])));
    }

    /**
     * Return the id of the selected attribute.
     *
     * @return The id of the selected attribute.
     */
    public int getAttributeId() {
        final GraphElementType et = getElementType();
        final String item = (String) attributeCombo.getSelectedItem();
        final int attrId;
        if (!item.isEmpty()) {
            ReadableGraph rg = graph.getReadableGraph();
            try {
                attrId = rg.getAttribute(et, item);
            } finally {
                rg.release();
            }
        } else {
            attrId = Graph.NOT_FOUND;
        }

        return attrId;
    }

    /**
     * Return the name of the selected attribute.
     *
     * @return The name of the selected attribute, or null if no attribute was
     * selected.
     */
    public String getAttributeName() {
        final String item = (String) attributeCombo.getSelectedItem();

        return item.isEmpty() ? null : item;
    }

    /**
     * Return the selected element type.
     *
     * @return The selected element type.
     */
    private GraphElementType getElementType() {
        final String elementTypeChoice = (String) elementTypeCombo.getSelectedItem();
        final GraphElementType graphElementType;
        if (elementTypeChoice.equals(VERTEX)) {
            graphElementType = GraphElementType.VERTEX;
        } else {
            graphElementType = GraphElementType.TRANSACTION;
        }

        return graphElementType;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        elementTypeCombo = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        attributeCombo = new javax.swing.JComboBox<>();

        setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(AttributeSelectionPanel.class, "AttributeSelectionPanel.border.title"))); // NOI18N

        jLabel1.setText(org.openide.util.NbBundle.getMessage(AttributeSelectionPanel.class, "AttributeSelectionPanel.jLabel1.text")); // NOI18N

        elementTypeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                elementTypeComboActionPerformed(evt);
            }
        });

        jLabel2.setText(org.openide.util.NbBundle.getMessage(AttributeSelectionPanel.class, "AttributeSelectionPanel.jLabel2.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(elementTypeCombo, 0, 257, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(attributeCombo, 0, 257, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(elementTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(attributeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void elementTypeComboActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_elementTypeComboActionPerformed
    {//GEN-HEADEREND:event_elementTypeComboActionPerformed
        setAttributeElements();
    }//GEN-LAST:event_elementTypeComboActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> attributeCombo;
    private javax.swing.JComboBox<String> elementTypeCombo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    // End of variables declaration//GEN-END:variables

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == elementTypeCombo) {
            firePropertyChange(ELEMENT_TYPE_ACTION, null, attributeCombo.getSelectedItem());
        }
        if (e.getSource() == attributeCombo) {
            firePropertyChange(ATTRIBUTE_ACTION, null, attributeCombo.getSelectedItem());
        }
    }
}
