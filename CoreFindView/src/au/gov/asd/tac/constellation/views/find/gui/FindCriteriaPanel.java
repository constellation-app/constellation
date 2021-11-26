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
package au.gov.asd.tac.constellation.views.find.gui;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.utilities.widgets.DateRangePanel;
import au.gov.asd.tac.constellation.graph.utilities.widgets.DateTimeListenerInterface;
import au.gov.asd.tac.constellation.graph.utilities.widgets.DateTimeRangePanel;
import au.gov.asd.tac.constellation.graph.utilities.widgets.TimeRangePanel;
import au.gov.asd.tac.constellation.views.find.advanced.FindRule;
import au.gov.asd.tac.constellation.views.find.advanced.FindTypeOperators;
import au.gov.asd.tac.constellation.views.find.advanced.FindTypeOperators.Operator;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import org.openide.util.NbBundle.Messages;

/**
 * Class that generates and manages an individual Find Rule's UI and state.
 *
 * @author betelgeuse
 */
@Messages({
    "No_Attributes=<No Attributes>",
    "Unsupported_Attribute=<Unsupported Attribute Type>",
    "No_Operators=<No Operators Available>"
})
public class FindCriteriaPanel extends JPanel implements DateTimeListenerInterface {

    private FindTopComponent parentTopComponent;
    // The current state of this component:
    private FindRule localState;
    private BooleanCriteriaPanel panelBoolean;
    private ColorCriteriaPanel panelColor;
    private DateRangePanel panelDate;
    private DateTimeRangePanel panelDateTime;
    private FloatCriteriaPanel panelFloat;
    private IntegerCriteriaPanel panelInt;
    private IconCriteriaPanel panelIcon;
    private StringCriteriaPanel panelString;
    private TimeRangePanel panelTime;

    /**
     * Creates new form FindCriteriaPanel
     *
     * @param parent The <code>FindTopComponent</code> that instantiated this.
     * @param attributes The list of <code>Attribute</code>s to set in this.
     *
     * @see FindTopComponent
     * @see ArrayList
     * @see Attribute
     */
    public FindCriteriaPanel(final FindTopComponent parent, final ArrayList<Attribute> attributes) {
        initComponents();

        this.parentTopComponent = parent;

        // Set the combobox renderer:
        cmbAttributes.setRenderer(new AttributeListRenderer());
        cmbOperators.setRenderer(new OperatorListRenderer());

        createCriteriaPanels();

        localState = new FindRule();

        updateAttributes(true, attributes);
    }

    /**
     * Creates a new form <code>FindCriteriaPanel</code> from a previously saved
     * state.
     *
     * @param parent The <code>FindTopComponent</code> that instantiated this.
     * @param localState The previous state to restore from.
     * @param attributes The list of <code>Attribute</code>s to set in this.
     *
     * @see FindTopComponent
     * @see FindRule
     * @see ArrayList
     */
    public FindCriteriaPanel(final FindTopComponent parent, final FindRule localState,
            final ArrayList<Attribute> attributes) {
        initComponents();

        this.parentTopComponent = parent;

        // Set the combobox renderer:
        cmbAttributes.setRenderer(new AttributeListRenderer());
        cmbOperators.setRenderer(new OperatorListRenderer());

        createCriteriaPanels();

        this.localState = localState;
        this.localState = this.getState();

        panelCriteriaHolder.removeAll();

        // Restore the state of the individual components:
        if (localState.getType() != null) {
            switch (localState.getType()) {
                case BOOLEAN:
                    panelBoolean = new BooleanCriteriaPanel(this, localState.getBooleanContent());
                    panelCriteriaHolder.add(panelBoolean);
                    break;
                case COLOR:
                    panelColor = new ColorCriteriaPanel(this, localState.getColorContent());
                    panelCriteriaHolder.add(panelColor);
                    break;
                case DATE:
                    panelDate = new DateRangePanel(this, localState.getDateFirstArg(), localState.getDateSecondArg(),
                            localState.getOperator().equals(FindTypeOperators.Operator.OCCURRED_BETWEEN));
                    panelCriteriaHolder.add(panelDate);
                    break;
                case DATETIME:
                    panelDateTime = new DateTimeRangePanel(this, localState.getDateTimeFirstArg(), localState.getDateTimeSecondArg(),
                            localState.getOperator().equals(FindTypeOperators.Operator.OCCURRED_BETWEEN));
                    panelCriteriaHolder.add(panelDateTime);
                    break;
                case FLOAT:
                    panelFloat = new FloatCriteriaPanel(this, localState.getFloatFirstArg(),
                            localState.getFloatSecondArg(), localState.getOperator().equals(FindTypeOperators.Operator.BETWEEN));
                    panelCriteriaHolder.add(panelFloat);
                    break;
                case INTEGER:
                    panelInt = new IntegerCriteriaPanel(this, localState.getIntFirstArg(),
                            localState.getIntSecondArg(), localState.getOperator().equals(FindTypeOperators.Operator.BETWEEN));
                    panelCriteriaHolder.add(panelInt);
                    break;
                case ICON:
                    panelIcon = new IconCriteriaPanel(this, localState.getIconContent());
                    panelCriteriaHolder.add(panelIcon);
                    break;
                case TIME:
                    panelTime = new TimeRangePanel(this, localState.getTimeFirstArg(), localState.getTimeSecondArg(),
                            localState.getOperator().equals(FindTypeOperators.Operator.OCCURRED_BETWEEN));
                    panelCriteriaHolder.add(panelTime);
                    break;
                case STRING:
                default:
                    panelString = new StringCriteriaPanel(this, localState.getStringContent(),
                            localState.isStringCaseSensitivity(), localState.isStringUsingList(),
                            localState.getOperator().equals(FindTypeOperators.Operator.REGEX));
                    panelCriteriaHolder.add(panelString);
                    break;
            }
        }

        updateAttributes(false, attributes);
    }

    /**
     * Creates a new <code>FindRule</code>, using one of FindRule's helper
     * methods for a given type where possible.
     *
     * @return The prepared <code>FindRule</code>
     *
     * @see FindRule
     */
    public FindRule getState() {
        if (localState.getArgs() != null) {
            localState.getArgs().clear();
        }

        if (localState.getType() != null) {
            // Add the arguments based on the type:
            switch (localState.getType()) {
                case BOOLEAN:
                    localState.addBooleanBasedRule(panelBoolean.getBooleanContent());
                    break;
                case COLOR:
                    localState.addColorBasedRule(panelColor.getColorContent());
                    break;
                case DATE:
                    localState.addDateBasedRule(panelDate.getFirstDate(), panelDate.getSecondDate());
                    break;
                case DATETIME:
                    localState.addDateTimeBasedRule(panelDateTime.getFirstDateTime(), panelDateTime.getSecondDateTime());
                    break;
                case FLOAT:
                    localState.addFloatBasedRule(panelFloat.getFirstFloat(), panelFloat.getSecondFloat());
                    break;
                case INTEGER:
                    localState.addIntegerBasedRule(panelInt.getFirstInt(), panelInt.getSecondInt());
                    break;
                case ICON:
                    localState.addIconBasedRule(panelIcon.getIconContent());
                    break;
                case TIME:
                    localState.addTimeBasedRule(panelTime.getFirstTime(), panelTime.getSecondTime());
                    break;
                case STRING:
                default:
                    localState.addStringBasedRule(panelString.getStringContent(),
                            panelString.isCaseSensitive(), panelString.isUsingList());
                    break;
            }
        }

        return localState;
    }

    /**
     * Updates the cmbAttributes UI component with the latest version of
     * attributes from the graph.
     *
     * @param isNew are these attribute from a new graph?
     * @param attributes the new attribute to display.
     */
    public void updateAttributes(final boolean isNew, final ArrayList<Attribute> attributes) {
        if (!attributes.isEmpty()) {
            cmbAttributes.setModel(new AttributeComboBoxModel(attributes));

            // Determine if this is a new find, or we are restoring from a previous state:
            if (isNew) {
                localState.setAttribute((Attribute) cmbAttributes.getSelectedItem());
            } else {
                cmbAttributes.getModel().setSelectedItem(localState.getAttribute());
            }

            setPanelEnabled(true);
        } else {
            // Nothing of use on this graph for this type, so display accordingly:
            cmbAttributes.setModel(new DefaultComboBoxModel(new String[]{Bundle.No_Attributes()}));
            panelCriteriaHolder.removeAll();

            setPanelEnabled(false);
        }

        updateOperators(isNew);
    }

    /**
     * Sets the relevant operators to this form.
     *
     * @param isNew <code>true</code> to get new operator list,
     * <code>false</code> to use existing.
     */
    public void updateOperators(final boolean isNew) {
        if (cmbAttributes.getSelectedItem() instanceof Attribute) {
            final Attribute attr = localState.getAttribute();
            final FindTypeOperators.Type type = FindTypeOperators.Type.getTypeEnum(attr.getAttributeType());
            localState.setType(type);

            final ArrayList<Operator> operators = new ArrayList<>();
            for (final Operator currentItem : type.getOperatorSet()) {
                operators.add(currentItem);
            }

            cmbOperators.setModel(new OperatorComboBoxModel(operators));

            // Determine if this is a new find, or we are restoring from a previous state:
            if (isNew) { 
                localState.setOperator((Operator) cmbOperators.getSelectedItem());
            } else {
                cmbOperators.setSelectedItem(localState.getOperator());
            }
            localState.setOperator((Operator) cmbOperators.getSelectedItem());
        } else {
            cmbOperators.setModel(new DefaultComboBoxModel(new String[]{Bundle.No_Operators()}));
            localState.setType(null);
        }

        updateArguments();
    }

    /**
     * Updates the criteria panel to contain a component designed to accept
     * input for the given attribute's type.
     */
    public void updateArguments() {
        panelCriteriaHolder.removeAll();

        if (localState.getType() != null) {
            switch (localState.getType()) {
                case BOOLEAN:
                    panelCriteriaHolder.add(panelBoolean);
                    break;

                case COLOR:
                    panelCriteriaHolder.add(panelColor);
                    break;

                case DATE:
                    panelCriteriaHolder.add(panelDate);
                    handleUIDate();
                    break;

                case DATETIME:
                    panelCriteriaHolder.add(panelDateTime);
                    handleUIDateTime();
                    break;

                case FLOAT:
                    panelCriteriaHolder.add(panelFloat);
                    handleUIFloat();
                    break;

                case INTEGER:
                    panelCriteriaHolder.add(panelInt);
                    handleUIInt();
                    break;

                case ICON:
                    panelCriteriaHolder.add(panelIcon);
                    break;

                case TIME:
                    panelCriteriaHolder.add(panelTime);
                    handleUITime();
                    break;

                case STRING:
                    panelCriteriaHolder.add(panelString);
                    handleUIString();
                    break;
                default:
                    break;
            }
        }

        this.validate();
        this.repaint();
    }

    /**
     * Iterates through this panel's components, and sets their enabled flag to
     * isEnabled.
     *
     * @param isEnabled <code>true</code> to enable all of this panel's
     * components, <code>false</code> to disable.
     */
    public void setPanelEnabled(final boolean isEnabled) {
        for (Component c : this.getComponents()) {
            if (c instanceof JPanel) {
                final JPanel jpanel = (JPanel) c;
                for (Component i : jpanel.getComponents()) {
                    i.setEnabled(isEnabled);
                }
            }
            c.setEnabled(isEnabled);
        }

        validate();
        repaint();
    }

    /**
     * Helper method that can be called by children wanting to request a save
     * state as there has been a change on their components.
     */
    public void saveStateToGraph() {
        parentTopComponent.saveStateToGraph();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlAddR = new javax.swing.JPanel();
        btnNewRule = new javax.swing.JButton();
        btnDeleteThis = new javax.swing.JButton();
        panelAttributes = new javax.swing.JPanel();
        cmbAttributes = new javax.swing.JComboBox<>();
        panelOperators = new javax.swing.JPanel();
        cmbOperators = new javax.swing.JComboBox<>();
        panelCriteriaHolder = new javax.swing.JPanel();
        filler = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));

        setMaximumSize(new java.awt.Dimension(32767, 47));
        setMinimumSize(new java.awt.Dimension(0, 47));
        setName("FindCriteriaPanel"); // NOI18N
        setPreferredSize(new java.awt.Dimension(838, 47));
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.X_AXIS));

        pnlAddR.setOpaque(false);

        org.openide.awt.Mnemonics.setLocalizedText(btnNewRule, org.openide.util.NbBundle.getMessage(FindCriteriaPanel.class, "FindCriteriaPanel.btnNewRule.text")); // NOI18N
        btnNewRule.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewRuleActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnDeleteThis, org.openide.util.NbBundle.getMessage(FindCriteriaPanel.class, "FindCriteriaPanel.btnDeleteThis.text")); // NOI18N
        btnDeleteThis.setMaximumSize(new java.awt.Dimension(41, 23));
        btnDeleteThis.setMinimumSize(new java.awt.Dimension(41, 23));
        btnDeleteThis.setPreferredSize(new java.awt.Dimension(41, 23));
        btnDeleteThis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteThisActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlAddRLayout = new javax.swing.GroupLayout(pnlAddR);
        pnlAddR.setLayout(pnlAddRLayout);
        pnlAddRLayout.setHorizontalGroup(
            pnlAddRLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAddRLayout.createSequentialGroup()
                .addGroup(pnlAddRLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlAddRLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(btnDeleteThis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlAddRLayout.createSequentialGroup()
                        .addGap(50, 50, 50)
                        .addComponent(btnNewRule)))
                .addContainerGap())
        );
        pnlAddRLayout.setVerticalGroup(
            pnlAddRLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAddRLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlAddRLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnDeleteThis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNewRule))
                .addContainerGap())
        );

        add(pnlAddR);

        panelAttributes.setOpaque(false);

        cmbAttributes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbAttributesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelAttributesLayout = new javax.swing.GroupLayout(panelAttributes);
        panelAttributes.setLayout(panelAttributesLayout);
        panelAttributesLayout.setHorizontalGroup(
            panelAttributesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelAttributesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cmbAttributes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        panelAttributesLayout.setVerticalGroup(
            panelAttributesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelAttributesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cmbAttributes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        add(panelAttributes);

        panelOperators.setOpaque(false);

        cmbOperators.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbOperatorsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelOperatorsLayout = new javax.swing.GroupLayout(panelOperators);
        panelOperators.setLayout(panelOperatorsLayout);
        panelOperatorsLayout.setHorizontalGroup(
            panelOperatorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelOperatorsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cmbOperators, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        panelOperatorsLayout.setVerticalGroup(
            panelOperatorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelOperatorsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cmbOperators, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        add(panelOperators);

        panelCriteriaHolder.setOpaque(false);
        panelCriteriaHolder.setLayout(new javax.swing.BoxLayout(panelCriteriaHolder, javax.swing.BoxLayout.X_AXIS));
        add(panelCriteriaHolder);
        add(filler);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Event handler for <code>btnNewRule</code> presses.
     * <p>
     * This triggers float validation checks.
     *
     * @param evt The registered event.
     */
    private void btnNewRuleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewRuleActionPerformed

        parentTopComponent.addFindCriteriaPanel();
    }//GEN-LAST:event_btnNewRuleActionPerformed

    /**
     * Event handler for <code>btnDeleteThis</code> presses.
     * <p>
     * This triggers float validation checks.
     *
     * @param evt The registered event.
     */
    private void btnDeleteThisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteThisActionPerformed

        parentTopComponent.removeFindCriteriaPanel(this);
    }//GEN-LAST:event_btnDeleteThisActionPerformed

    /**
     * Event handler for <code>cmbAttributes</code> selection changes.
     * <p>
     * This triggers float validation checks.
     *
     * @param evt The registered event.
     */
    private void cmbAttributesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbAttributesActionPerformed

        if (cmbAttributes.getSelectedItem() instanceof Attribute) {
            localState.setAttribute((Attribute) cmbAttributes.getSelectedItem());
        } else {
            localState.setAttribute(null);
        }

        updateOperators(false);
    }//GEN-LAST:event_cmbAttributesActionPerformed

    /**
     * Event handler for <code>cmbOperators</code> selection changes.
     * <p>
     * This triggers float validation checks.
     *
     * @param evt The registered event.
     */
    private void cmbOperatorsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbOperatorsActionPerformed

        if (cmbOperators.getSelectedItem() instanceof Operator) {
            localState.setOperator((Operator) cmbOperators.getSelectedItem());
        } else {
            localState.setAttribute(null);
        }

        updateArguments();
    }//GEN-LAST:event_cmbOperatorsActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDeleteThis;
    private javax.swing.JButton btnNewRule;
    private javax.swing.JComboBox<Attribute> cmbAttributes;
    private javax.swing.JComboBox<Operator> cmbOperators;
    private javax.swing.Box.Filler filler;
    private javax.swing.JPanel panelAttributes;
    private javax.swing.JPanel panelCriteriaHolder;
    private javax.swing.JPanel panelOperators;
    private javax.swing.JPanel pnlAddR;
    // End of variables declaration//GEN-END:variables

    /**
     * Determines whether panelDate needs to be set in 'extended' mode.
     */
    private void handleUIDate() {
        final DateRangePanel dcp = panelDate;

        // Determine whether we need to adjust the panel for values 'between'.
        dcp.setUIState(Operator.OCCURRED_BETWEEN.equals(localState.getOperator()));

        this.validate();
        this.repaint();
    }

    /**
     * Determines whether panelDateTime needs to be set in 'extended' mode.
     */
    private void handleUIDateTime() {
        final DateTimeRangePanel dcp = panelDateTime;

        // Determine whether we need to adjust the panel for values 'between'.
        dcp.setUIState(Operator.OCCURRED_BETWEEN.equals(localState.getOperator()));

        this.validate();
        this.repaint();
    }

    /**
     * Determines whether panelFloat needs to be set in 'extended' mode.
     */
    private void handleUIFloat() {
        final FloatCriteriaPanel fcp = panelFloat;

        // Determine whether we need to adjust the panel for values 'between'.
        fcp.setUIState(Operator.BETWEEN.equals(localState.getOperator()));

        this.validate();
        this.repaint();
    }

    /**
     * Determines whether panelFloat needs to be set in 'extended' mode.
     */
    private void handleUIInt() {
        final IntegerCriteriaPanel icp = panelInt;

        // Determine whether we need to adjust the panel for values 'between'.
        icp.setUIState(Operator.BETWEEN.equals(localState.getOperator()));

        this.validate();
        this.repaint();
    }

    /**
     * Determines whether panelString needs to be set in 'extended' mode.
     */
    private void handleUIString() {
        final StringCriteriaPanel scp = panelString;

        // Determine whether we need to adjust the panel for regex strings:
        scp.setUIState(Operator.REGEX.equals(localState.getOperator()));

        this.validate();
        this.repaint();
    }

    /**
     * Determines whether panelTime needs to be set in 'extended' mode.
     */
    private void handleUITime() {
        final TimeRangePanel tcp = panelTime;

        // Determine whether we need to adjust the panel for values 'between'.
        tcp.setUIState(Operator.OCCURRED_BETWEEN.equals(localState.getOperator()));

        this.validate();
        this.repaint();
    }

    /**
     * Creates a new instance of each panel type.
     */
    private void createCriteriaPanels() {
        panelBoolean = new BooleanCriteriaPanel(this);
        panelColor = new ColorCriteriaPanel(this);
        panelDate = new DateRangePanel(this);
        panelDateTime = new DateTimeRangePanel(this);
        panelFloat = new FloatCriteriaPanel(this);
        panelInt = new IntegerCriteriaPanel(this);
        panelIcon = new IconCriteriaPanel(this);
        panelString = new StringCriteriaPanel(this);
        panelTime = new TimeRangePanel(this);
    }

    @Override
    public void focusChanged() {
        saveStateToGraph();
    }

    // <editor-fold defaultstate="collapsed" desc="Custom ComboBox Handlers/Renderers">
    /**
     * Custom ComboBoxModel that handles <code>Attribute</code>s.
     *
     * @see Attribute
     * @see AbstractListModel
     * @see ComboBoxModel
     */
    private class AttributeComboBoxModel extends AbstractListModel<Attribute> implements ComboBoxModel<Attribute> {

        private Attribute selectedAttribute;
        private final ArrayList<Attribute> attributes;

        /**
         * Constructs a new <code>AttributeComboBoxModel</code>.
         *
         * @param attributes The attributes to place in * * this
         * <code>ComboBoxModel</code>.
         *
         * @see ArrayList
         * @see Attribute
         */
        public AttributeComboBoxModel(final ArrayList<Attribute> attributes) {
            super();

            this.attributes = attributes;

            // Sort the list with capital-case appearing before lowercase.
            Collections.sort(this.attributes, new AttributeComparator());

            if (this.attributes.size() > 0) {
                selectedAttribute = this.attributes.get(0);
            } else {
                selectedAttribute = null;
            }
        }

        /**
         * Acts as a custom comparator that lists capitalised elements before
         * lower case elements.
         * <p>
         * This acts to place user added attributes above built in attributes in
         * the <code>ComboBoxModel</code>.
         */
        private class AttributeComparator implements Comparator<Attribute> {

            @Override
            public int compare(Attribute attr1, Attribute attr2) {
                return attr1.getName().compareTo(attr2.getName());
            }
        }

        @Override
        public void setSelectedItem(final Object selectedAttribute) {
            if ((selectedAttribute != null) && (selectedAttribute.getClass().getSuperclass().isInstance(Attribute.class))) {
                Attribute selected = (Attribute) selectedAttribute;
                for (Attribute attr : attributes) {
                    if (attr.getId() == selected.getId()) {
                        this.selectedAttribute = attr;
                        fireContentsChanged(this, -1, -1);
                        return;
                    }
                }
            }
        }

        @Override
        public Object getSelectedItem() {
            return selectedAttribute;
        }

        @Override
        public int getSize() {
            return attributes.size();
        }

        @Override
        public Attribute getElementAt(final int index) {
            if (index >= 0 && index < getSize()) {
                return attributes.get(index);
            }

            return null;
        }
    }

    /**
     * Custom renderer to correctly handle the rendering of
     * <code>Attribute</code>s.
     *
     * @see Attribute
     */
    private class AttributeListRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(
                final JList<?> list, final Object value, final int index,
                final boolean isSelected, final boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof Attribute) {
                final Attribute attr = (Attribute) value;
                setText(attr.getName());
            }

            return this;
        }
    }

    /**
     * Custom ComboBoxModel that handles <code>Operator</code>s.
     *
     * @see Operator
     * @see AbstractListModel
     * @see ComboBoxModel
     */
    private class OperatorComboBoxModel extends AbstractListModel<Operator> implements ComboBoxModel<Operator> {

        private Operator selectedOperator;
        private final ArrayList<Operator> operators;

        /**
         * Constructs a new <code>OperatorComboBoxModel</code>.
         *
         * @param operators The operators to place in * * this
         * <code>ComboBoxModel</code>.
         *
         * @see ArrayList
         * @see Operator
         */
        public OperatorComboBoxModel(final ArrayList<Operator> operators) {
            this.operators = operators;

            if (this.operators.size() > 0) {
                selectedOperator = this.operators.get(0);
            } else {
                selectedOperator = null;
            }
        }

        @Override
        public void setSelectedItem(final Object selectedOperator) {
            if (selectedOperator != null) {
                for (Operator oper : operators) {
                    if (oper.equals(selectedOperator)) {
                        this.selectedOperator = oper;
                        fireContentsChanged(this, -1, -1);
                        return;
                    }
                }
            }
        }

        @Override
        public Object getSelectedItem() {
            return selectedOperator;
        }

        @Override
        public int getSize() {
            return operators.size();
        }

        @Override
        public Operator getElementAt(final int index) {
            if (index >= 0 && index < getSize()) {
                return operators.get(index);
            } else {
                return null;
            }
        }
    }

    /**
     * Custom OperatorListRenderer which renders the friendly names of the
     * operator enums.
     */
    private class OperatorListRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(
                final JList<?> list, final Object value, final int index,
                final boolean isSelected, final boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof Operator) {
                final Operator oper = (Operator) value;
                setText(oper.toString());
            }

            return this;
        }
    }
    // </editor-fold>
}
