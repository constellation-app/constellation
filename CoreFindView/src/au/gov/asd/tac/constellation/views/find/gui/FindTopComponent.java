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
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.attribute.ZonedDateTimeAttributeDescription;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeEvent;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeListener;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.views.find.BasicFindPlugin;
import au.gov.asd.tac.constellation.views.find.ReplacePlugin;
import au.gov.asd.tac.constellation.views.find.advanced.AdvancedFindPlugin;
import au.gov.asd.tac.constellation.views.find.advanced.FindResult;
import au.gov.asd.tac.constellation.views.find.advanced.FindRule;
import au.gov.asd.tac.constellation.views.find.advanced.FindState;
import au.gov.asd.tac.constellation.views.find.advanced.FindStatePlugin;
import au.gov.asd.tac.constellation.views.find.advanced.FindTypeOperators;
import au.gov.asd.tac.constellation.views.find.advanced.GraphAttributePlugin;
import au.gov.asd.tac.constellation.views.find.advanced.SelectFindResultsPlugin;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 * Top component which displays the Find interface.
 * <p>
 * Note: This component is registered as the default Find action for
 * CONSTELLATION. CTRL-F will open this component.
 *
 * @author betelgeuse
 */
@TopComponent.Description(
        preferredID = "FindTopComponent",
        iconBase = "au/gov/asd/tac/constellation/views/find/resources/find.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(
        mode = "output",
        openAtStartup = false
)
@ActionID(
        category = "Selection",
        id = "au.gov.asd.tac.constellation.views.find.gui.FindTopComponent"
)
@ActionReferences({
    @ActionReference(path = "Menu/Edit", position = 1000, separatorBefore = 999),
    @ActionReference(path = "Shortcuts", name = "C-F")
})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_FindAction",
        preferredID = "FindTopComponent"
)
@Messages({
    "CTL_FindAction=Find...",
    "CTL_FindTopComponent=Find and Replace",
    "HINT_FindTopComponent=Use this window to perform find operations on the current graph.",
    "No_Active_Graph=<No Active Graph>",
    "Find_ALL=all",
    "Find_ANY=any",
    "Find_VERTEX=nodes",
    "Find_TRANSACTION=transactions",
    "Find_EDGE=edges",
    "Find_LINK=links"
})

public final class FindTopComponent extends TopComponent implements GraphChangeListener, LookupListener {
    
    private static final Logger LOGGER = Logger.getLogger(FindTopComponent.class.getName());

    private final JLabel lblNoGraph = new JLabel(Bundle.No_Active_Graph());
    private final JPanel panelNoGraph = new JPanel();
    private GraphNode graphNode = null;
    private final Lookup.Result<GraphNode> result;
    private ArrayList<Attribute> attributes = new ArrayList<>();
    private long attributeModificationCounter;
    private GraphElementType type = GraphElementType.VERTEX;
    private final BasicFindPanel basicFindPanel;
    private final ReplacePanel replacePanel;

    /**
     * Constructs a FindTopComponent.
     */
    public FindTopComponent() {
        initComponents();
        setName(Bundle.CTL_FindTopComponent());
        setToolTipText(Bundle.HINT_FindTopComponent());

        // Setup the single column / vertical row layout:
        final BoxLayout findLayout = new BoxLayout(panelFind, BoxLayout.Y_AXIS);
        panelFind.setLayout(findLayout);
        // Create label and container to show user when no active graph selected:
        panelNoGraph.setLayout(new BorderLayout());
        panelNoGraph.setName("panelNoGraph");
        lblNoGraph.setOpaque(true);
        lblNoGraph.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblNoGraph.setVerticalAlignment(javax.swing.SwingConstants.CENTER);
        panelNoGraph.add(lblNoGraph, BorderLayout.CENTER);

        // Attach listener that determines the active graphnode:
        result = Utilities.actionsGlobalContext().lookupResult(GraphNode.class);
        result.addLookupListener(this);
        //basic find panel setup
        lblMatchPA.setVisible(false);
        lblMatchPA.setEnabled(false);
        cmbMatch.setVisible(false);
        cmbMatch.setEnabled(false);
        lblMatchPB.setVisible(false);
        lblMatchPB.setEnabled(false);
        basicFindPanel = new BasicFindPanel(this);
        basicFindPanel.setValidationListener((boolean valid1) -> {
            if (jTabbedPane1.getSelectedIndex() == 0) {
                //basic find panel is active
                btnFind.setEnabled(valid1);
            }
        });
        basicFindScrollpane.setViewportView(basicFindPanel);

        //replace panel setup
        replacePanel = new ReplacePanel();
        replacePanel.setValidationListener((boolean valid1) -> {
            if (jTabbedPane1.getSelectedIndex() == 1) {
                //replace panel is active
                btnFind.setEnabled(valid1);
            }
        });
        replaceScrollPane.setViewportView(replacePanel);
        // Start with nothing visible:
        toggleUI(false);

        jTabbedPane1.addChangeListener(e -> tabbedPaneChanged());
    }

    /**
     * Returns the panel that has all of the rules for the Find interface.
     *
     * @return panelFind The panel that contains all of the rules.
     */
    public JPanel getPanelFind() {
        return panelFind;
    }

    /**
     * Adds a new FindCriteriaPanel to the end of the scrlRuleBuilder component,
     * then shades and updates the UI accordingly.
     */
    public void addFindCriteriaPanel() {
        addFindCriteriaPanel(null);
    }

    /**
     * Adds a new FindCriteriaPanel to the end of the scrlRuleBuilder component,
     * then shades and updates the UI accordingly.
     *
     * @param rule the find rule to add to the panel.
     */
    public void addFindCriteriaPanel(final FindRule rule) {
        // Ensure that we know for sure what attributes we need:
        determineAttributes();

        FindCriteriaPanel findCriteriaPanel;

        if (rule != null) {
            findCriteriaPanel = new FindCriteriaPanel(this, rule, attributes);
        } else {
            findCriteriaPanel = new FindCriteriaPanel(this, attributes);
        }

        panelFind.add(findCriteriaPanel);

        // saveStateToGraph(), not saving the state as there are too many write
        // locks. The state is saved when you run a search, which is good enough.
        this.validate();
        this.repaint();
    }

    /**
     * Restores a given state to the FindTopComponent.
     *
     * @param state The state that contains all relevant information to restore
     * as to previously defined.
     */
    public void restoreState(final FindState state) {
        // Determine the state of the GraphElementType:
        switch (state.getGraphElementType()) {
            case TRANSACTION:
                cmbGraphElementType.setSelectedItem(Bundle.Find_TRANSACTION());
                break;
            case EDGE:
                cmbGraphElementType.setSelectedItem(Bundle.Find_EDGE());
                break;
            case LINK:
                cmbGraphElementType.setSelectedItem(Bundle.Find_LINK());
                break;
            case VERTEX:
            default:
                cmbGraphElementType.setSelectedItem(Bundle.Find_VERTEX());
                break;
        }

        // Start with a blank slate:
        panelFind.removeAll();

        // Determine the state of ANY/ALL:
        if (state.isAny()) {
            cmbMatch.setSelectedItem(Bundle.Find_ANY());
        } else {
            cmbMatch.setSelectedItem(Bundle.Find_ALL());
        }

        // Determine the state of HELD:
        chkAddToSelection.setSelected(state.isHeld());

        if (!state.getRules().isEmpty()) {
            for (FindRule rule : state.getRules()) {
                final FindCriteriaPanel findCriteriaPanel = new FindCriteriaPanel(this, rule, attributes);
                findCriteriaPanel.repaint();

                panelFind.add(findCriteriaPanel);
            }

            this.validate();
            this.repaint();
        } else {
            setDefaultFindCriteriaPanels();
        }
    }

    /**
     * Removes the given FindCriteriaPanel from the form.
     *
     * @param panel FindCriteriaPanel to be removed from the form.
     */
    public void removeFindCriteriaPanel(final FindCriteriaPanel panel) {
        panelFind.remove(panel);

        // Add a new fcp if this was the last one.
        if (panelFind.getComponentCount() == 0) {
            addFindCriteriaPanel();
        }

        // We now have a new state:
        saveStateToGraph();

        this.validate();
        this.repaint();
    }

    @Override
    public void componentOpened() {
        result.addLookupListener(this);
        resultChanged(null);
    }

    @Override
    public void componentClosed() {
        result.removeLookupListener(this);
        setNode(null);
    }

    @Override
    public void resultChanged(final LookupEvent lev) {
        final Node[] activatedNodes = TopComponent.getRegistry().getActivatedNodes();
        if (activatedNodes != null && activatedNodes.length == 1
                && activatedNodes[0] instanceof GraphNode) {
            final GraphNode gnode = ((GraphNode) activatedNodes[0]);

            if (gnode != graphNode) {
                setNode(gnode);
            }
        } else {
            setNode(null);
        }
    }

    /**
     * Listen to attribute changes in the graph so we can reflect them across
     * the find rules.
     * <p>
     * The event may be null, since we call this manually from setNode() after a
     * graph change.
     *
     * @param evt PropertyChangeEvent.
     */
    @Override
    public void graphChanged(final GraphChangeEvent evt) {
        if (graphNode != null) {
            final Graph graph = graphNode.getGraph();
            ReadableGraph rg = graph.getReadableGraph();
            try {
                final long amc = rg.getAttributeModificationCounter();
                if (amc != attributeModificationCounter) {
                    determineAttributes();
                }
            } finally {
                rg.release();
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        basicFindScrollpane = new javax.swing.JScrollPane();
        replaceScrollPane = new javax.swing.JScrollPane();
        scrlRuleBuilder = new javax.swing.JScrollPane();
        panelFind = new javax.swing.JPanel();
        btnFind = new javax.swing.JButton();
        lblMatchPA = new javax.swing.JLabel();
        cmbMatch = new javax.swing.JComboBox<>();
        lblMatchPB = new javax.swing.JLabel();
        btnReset = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        lblSelect = new javax.swing.JLabel();
        cmbGraphElementType = new javax.swing.JComboBox<>();
        chkAddToSelection = new javax.swing.JCheckBox();

        setPreferredSize(new java.awt.Dimension(1024, 310));

        jTabbedPane1.addTab("Basic Find", basicFindScrollpane);
        jTabbedPane1.addTab("Replace", replaceScrollPane);

        scrlRuleBuilder.setAutoscrolls(true);
        scrlRuleBuilder.setMinimumSize(new java.awt.Dimension(23, 47));
        scrlRuleBuilder.setName("scrlRuleBuilder"); // NOI18N
        scrlRuleBuilder.setPreferredSize(new java.awt.Dimension(380, 47));

        javax.swing.GroupLayout panelFindLayout = new javax.swing.GroupLayout(panelFind);
        panelFind.setLayout(panelFindLayout);
        panelFindLayout.setHorizontalGroup(
                panelFindLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 997, Short.MAX_VALUE)
        );
        panelFindLayout.setVerticalGroup(
                panelFindLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 192, Short.MAX_VALUE)
        );

        scrlRuleBuilder.setViewportView(panelFind);

        jTabbedPane1.addTab("Advanced Find", scrlRuleBuilder);

        org.openide.awt.Mnemonics.setLocalizedText(btnFind, "Find");
        btnFind.setToolTipText("Select on the graph all data that matches the criteria specified.");
        btnFind.setEnabled(false);
        btnFind.addActionListener(this::btnFindActionPerformed);

        org.openide.awt.Mnemonics.setLocalizedText(lblMatchPA, "that match");

        cmbMatch.setMaximumRowCount(2);
        cmbMatch.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{Bundle.Find_ALL(), Bundle.Find_ANY()}));
        cmbMatch.addActionListener(this::cmbMatchActionPerformed);

        org.openide.awt.Mnemonics.setLocalizedText(lblMatchPB, "of the following criteria:");

        org.openide.awt.Mnemonics.setLocalizedText(btnReset, "Reset To Default");
        btnReset.setToolTipText("Removes all of the currently defined criteria and resets the find window to the default.");
        btnReset.addActionListener(this::btnResetActionPerformed);

        org.openide.awt.Mnemonics.setLocalizedText(lblSelect, "Find");

        cmbGraphElementType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{Bundle.Find_VERTEX(), Bundle.Find_TRANSACTION(), Bundle.Find_EDGE(), Bundle.Find_LINK()}));
        cmbGraphElementType.addActionListener(this::cmbGraphElementTypeActionPerformed);

        org.openide.awt.Mnemonics.setLocalizedText(chkAddToSelection, "Add results to current selection");
        chkAddToSelection.addActionListener(this::chkAddToSelectionActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jTabbedPane1)
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                                .addComponent(btnReset)
                                                .addGap(18, 18, 18)
                                                .addComponent(btnFind)
                                                .addGap(0, 0, Short.MAX_VALUE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addGap(119, 119, 119)
                                                                .addComponent(filler1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(lblSelect)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(cmbGraphElementType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(lblMatchPA)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(cmbMatch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(lblMatchPB)))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 571, Short.MAX_VALUE)
                                                .addComponent(chkAddToSelection)))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblMatchPA)
                                        .addComponent(cmbMatch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(lblMatchPB)
                                        .addComponent(lblSelect)
                                        .addComponent(cmbGraphElementType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(chkAddToSelection))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(filler1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(btnReset)
                                        .addComponent(btnFind))
                                .addContainerGap())
        );

        jTabbedPane1.getAccessibleContext().setAccessibleName("Advanced Find");
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Event handler for <code>btnFind</code> presses.
     * <p>
     * This triggers float validation checks.
     *
     * @param evt The registered event.
     */
    private void btnFindActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnFindActionPerformed
        switch (jTabbedPane1.getSelectedIndex()) {
            case 0:
                // basic find panel is active
                performBasicSearch();
                break;
            case 1:
                // replace panel is active
                performReplace();
                break;
            case 2:
                // advanced find panel is active
                performSearch();
                break;
            default:
                break;
        }
    }//GEN-LAST:event_btnFindActionPerformed

    /**
     * Event handler for <code>btnReset</code> presses.
     * <p>
     * This triggers float validation checks.
     *
     * @param evt The registered event.
     */
    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetActionPerformed
        resetToDefault();
    }//GEN-LAST:event_btnResetActionPerformed

    /**
     * Event handler for <code>cmbMatch</code> selection changes.
     * <p>
     * This triggers float validation checks.
     *
     * @param evt The registered event.
     */
    private void cmbMatchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbMatchActionPerformed
        // saveStateToGraph(), not saving the state as there are too many write
        // locks. The state is saved when you run a search, which is good enough.
    }//GEN-LAST:event_cmbMatchActionPerformed

    /**
     * Event handler for <code>cmbGraphElement</code> selection changes.
     * <p>
     * This triggers float validation checks.
     *
     * @param evt The registered event.
     */
    private void cmbGraphElementTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbGraphElementTypeActionPerformed
        if (evt.getSource().equals(cmbGraphElementType)) {
            final GraphElementType currentType = type;

            if (cmbGraphElementType.getSelectedItem().equals(Bundle.Find_VERTEX())) {
                type = GraphElementType.VERTEX;
            } else if (cmbGraphElementType.getSelectedItem().equals(Bundle.Find_TRANSACTION())) {
                type = GraphElementType.TRANSACTION;
            } else if (cmbGraphElementType.getSelectedItem().equals(Bundle.Find_EDGE())) {
                type = GraphElementType.EDGE;
            } else if (cmbGraphElementType.getSelectedItem().equals(Bundle.Find_LINK())) {
                type = GraphElementType.LINK;
            } else {
                // We don't have a valid type, so don't perform any further action.
                return;
            }

            // Make sure we are actually dealing with a new type here:
            if (!type.equals(currentType)) {
                // We have a new type, so make sure we have a new slate to accompany it:
                panelFind.removeAll();

                // Ensure that we know that we need to refresh attributes:
                attributeModificationCounter++;
                determineAttributes();

                setDefaultFindCriteriaPanels();
            }

            // saveStateToGraph(), not saving the state as there are too many write
            // locks. The state is saved when you run a search, which is good enough.
        }
    }//GEN-LAST:event_cmbGraphElementTypeActionPerformed

    private void chkAddToSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkAddToSelectionActionPerformed
        // saveStateToGraph(), not saving the state as there are too many write
        // locks. The state is saved when you run a search, which is good enough.
    }//GEN-LAST:event_chkAddToSelectionActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane basicFindScrollpane;
    private javax.swing.JButton btnFind;
    private javax.swing.JButton btnReset;
    private javax.swing.JCheckBox chkAddToSelection;
    private javax.swing.JComboBox<String> cmbGraphElementType;
    private javax.swing.JComboBox<String> cmbMatch;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lblMatchPA;
    private javax.swing.JLabel lblMatchPB;
    private javax.swing.JLabel lblSelect;
    private javax.swing.JPanel panelFind;
    private javax.swing.JScrollPane replaceScrollPane;
    private javax.swing.JScrollPane scrlRuleBuilder;
    // End of variables declaration//GEN-END:variables

    /**
     * Resets the <code>FindTopComponent</code> to its original state.
     */
    private void resetToDefault() {
        panelFind.removeAll();
        setDefaultFindCriteriaPanels();
        basicFindPanel.reset();
        replacePanel.reset();
        // Things have changed, save the new state to the graph:
        saveStateToGraph();
    }

    /**
     * add set of find criteria panel based on primary keys, otherwise just add
     * one with the first column in it
     */
    private void setDefaultFindCriteriaPanels() {
        final Graph graph = graphNode.getGraph();
        ReadableGraph rg = graph.getReadableGraph();
        try {
            int[] keys = rg.getPrimaryKey(type);
            if (keys.length == 0) {
                addFindCriteriaPanel();
            } else {
                for (int i = 0; i < keys.length; i++) {
                    Attribute attr = new GraphAttribute(rg, keys[i]);
                    FindRule rule = new FindRule();
                    rule.setAttribute(attr);
                    if ("boolean".equalsIgnoreCase(attr.getAttributeType())) {
                        rule.addBooleanBasedRule(true);
                        rule.setOperator(FindTypeOperators.Operator.IS);
                    } else if ("color".equalsIgnoreCase(attr.getAttributeType())) {
                        rule.addColorBasedRule(Color.BLACK);
                        rule.setOperator(FindTypeOperators.Operator.IS);
                    } else if ("date".equalsIgnoreCase(attr.getAttributeType())) {
                        rule.addDateBasedRule(new Date(), new Date());
                        rule.setOperator(FindTypeOperators.Operator.OCCURRED_ON);
                    } else if (attr.getAttributeType().equalsIgnoreCase(ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME)) {
                        rule.addDateTimeBasedRule(new GregorianCalendar(), new GregorianCalendar());
                        rule.setOperator(FindTypeOperators.Operator.OCCURRED_ON);
                    } else if ("time".equalsIgnoreCase(attr.getAttributeType())) {
                        rule.addTimeBasedRule(new GregorianCalendar(), new GregorianCalendar());
                        rule.setOperator(FindTypeOperators.Operator.OCCURRED_ON);
                    } else if ("float".equalsIgnoreCase(attr.getAttributeType())) {
                        rule.addFloatBasedRule(0.0F, 0.0F);

                        rule.setOperator(FindTypeOperators.Operator.IS);
                    } else if ("integer".equalsIgnoreCase(attr.getAttributeType())) {
                        rule.addIntegerBasedRule(0, 0);
                        rule.setOperator(FindTypeOperators.Operator.IS);
                    } else if ("icon".equalsIgnoreCase(attr.getAttributeType())) {
                        rule.addIconBasedRule("");
                        rule.setOperator(FindTypeOperators.Operator.IS);
                    } else { // string
                        rule.addStringBasedRule("", false, false);
                        rule.setOperator(FindTypeOperators.Operator.CONTAINS);
                    }
                    addFindCriteriaPanel(rule);
                }
            }
        } finally {
            rg.release();
        }
    }

    /**
     * Causes the state to be gathered by collecting each 'rule' from all of the
     * <code>FindCriteriaPanel</code>s hosted.
     * <p>
     * Upon all rules being wrapped in an overall <code>FindState</code>,
     * invokes the write to graph plugin and passes it the state to write in the
     * background.
     *
     * @return The state saved to the graph.
     *
     * @see FindCriteriaPanel
     * @see FindState
     */
    @SuppressWarnings("unchecked")
    public FindState saveStateToGraph() {
        final Graph graph = graphNode.getGraph();

        // Collect the state of all of the FCPs (ie the current rules):
        final FindState newState = new FindState();
        for (Component c : panelFind.getComponents()) {
            if (c instanceof FindCriteriaPanel && !attributes.isEmpty()) {
                final FindCriteriaPanel fcp = (FindCriteriaPanel) c;
                newState.addRule(fcp.getState());
            }
        }

        // Save the GraphElementType:
        if (cmbGraphElementType.getSelectedItem().equals(Bundle.Find_VERTEX())) {
            newState.setGraphElementType(GraphElementType.VERTEX);
        } else if (cmbGraphElementType.getSelectedItem().equals(Bundle.Find_TRANSACTION())) {
            newState.setGraphElementType(GraphElementType.TRANSACTION);
        } else if (cmbGraphElementType.getSelectedItem().equals(Bundle.Find_EDGE())) {
            newState.setGraphElementType(GraphElementType.EDGE);
        } else {
            newState.setGraphElementType(GraphElementType.LINK);
        }

        // Save the AND/OR mode ('any'/'all'):
        newState.setAny(this.cmbMatch.getSelectedItem().equals(Bundle.Find_ANY()));

        // Save the hold selection mode:
        newState.setHeld(chkAddToSelection.isSelected());

        // Write what we have to the graph:
        final FindStatePlugin findStatePlugin = new FindStatePlugin(newState);
        PluginExecution.withPlugin(findStatePlugin).executeLater(graph);

        return newState;
    }

    /**
     * Retrieves the <code>FindState</code> from the graph if there is one
     * present, and passes it to the <code>restoreState()</code> method to be
     * restored to the interface.
     *
     * @param graph The graph to read the state from.
     *
     * @see FindState
     */
    @SuppressWarnings("unchecked")
    private void readStateFromGraph(final GraphReadMethods graph) {
        final int attrID = graph.getAttribute(GraphElementType.META, FindState.ATTRIBUTE_NAME);

        if (attrID != Graph.NOT_FOUND) {
            final Object possibleState = graph.getObjectValue(attrID, 0);
            if (possibleState instanceof FindState) {
                restoreState((FindState) possibleState);
            }
        } else {
            // This is a new Find Search, so start with a fresh FCP:
            cmbMatch.setSelectedItem(Bundle.Find_ALL());
            chkAddToSelection.setSelected(false);
            panelFind.removeAll();
            setDefaultFindCriteriaPanels();
        }
    }

    /**
     * Gathers the state from the graph, and executes the
     * <code>AdvancedQueryPlugin</code>, passing it the rules from the state.
     * <p>
     * (Where each <code>FindCriteriaPanel</code> represents an individual
     * rule.)
     */
    @SuppressWarnings("unchecked")
    private void performSearch() {
        final Graph graph = graphNode.getGraph();

        final FindState state = saveStateToGraph();

        final AdvancedFindPlugin queryPlugin = new AdvancedFindPlugin(type, state.getRules(), !state.isAny());
        final Future<?> future = PluginExecution.withPlugin(queryPlugin).interactively(true).executeLater(graph);

        // Wait for the search to find its results:
        try {
            future.get();
        } catch (final InterruptedException ex) {
            LOGGER.log(Level.SEVERE, "Thread was interrupted", ex);
            Thread.currentThread().interrupt();
        } catch (final ExecutionException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }

        final List<FindResult> results = queryPlugin.getResults();

        final SelectFindResultsPlugin selectPlugin = new SelectFindResultsPlugin(results, state.isHeld());
        PluginExecution.withPlugin(selectPlugin).interactively(true).executeLater(graph);
    }

    private void performReplace() {
        final ArrayList<Attribute> selectedAttributes = replacePanel.getSelectedAttributes();
        final String findString = replacePanel.getFindString();
        final String replaceString = replacePanel.getReplaceString();
        final Boolean regex = replacePanel.getRegex();
        final Boolean ignoreCase = replacePanel.getIgnorecase();
        final ReplacePlugin replacePlugin = new ReplacePlugin(type, selectedAttributes, findString, replaceString, regex, ignoreCase);
        PluginExecution.withPlugin(replacePlugin).executeLater(graphNode.getGraph());

    }

    // public because BasicFindPanel calls this on enter as well.
    public void performBasicSearch() {
        final ArrayList<Attribute> selectedAttributes = basicFindPanel.getSelectedAttributes();
        final String findString = basicFindPanel.getFindString();
        final boolean regex = basicFindPanel.hasRegex();
        final boolean ignoreCase = basicFindPanel.isIgnorecase();
        final boolean matchWholeWord = basicFindPanel.isExactMatch();
        final BasicFindPlugin basicfindPlugin = new BasicFindPlugin(type, selectedAttributes, findString, regex, ignoreCase, matchWholeWord, chkAddToSelection.isSelected());
        PluginExecution.withPlugin(basicfindPlugin).executeLater(graphNode.getGraph());
    }

    /**
     * Enables and disables the FindTopComponent UI components.
     *
     * @param showUI True to enable UI features, False to disable.
     */
    private void toggleUI(final boolean showUI) {
        if (showUI) {
            scrlRuleBuilder.setViewportView(panelFind);
            replaceScrollPane.setViewportView(replacePanel);
            basicFindScrollpane.setViewportView(basicFindPanel);

            for (Component c : this.getComponents()) {
                c.setEnabled(true);
            }

            if (panelFind.getComponentCount() == 0) {
                setDefaultFindCriteriaPanels();
            }

        } else {
            scrlRuleBuilder.setViewportView(panelNoGraph);
            replaceScrollPane.setViewportView(panelNoGraph);
            basicFindScrollpane.setViewportView(panelNoGraph);
            btnFind.setEnabled(false);
            for (Component c : this.getComponents()) {
                c.setEnabled(false);
            }
        }
        basicFindPanel.setEnabled(showUI);
        replacePanel.setEnabled(showUI);
        this.validate();
        this.repaint();
    }

    /**
     * Executes the <code>GraphAttributePlugin</code> to determine whether there
     * are new or different attributes to set to all relevant parties.
     */
    @SuppressWarnings("unchecked")
    private void determineAttributes() {
        if (graphNode == null) {
            return;
        }

        final Graph graph = graphNode.getGraph();

        final GraphAttributePlugin attrPlugin = new GraphAttributePlugin(type, attributes, attributeModificationCounter);
        final Future<?> future = PluginExecution.withPlugin(attrPlugin).interactively(true).executeLater(graph);

        // Wait for the search to find its results:
        try {
            future.get();
        } catch (final InterruptedException ex) {
            LOGGER.log(Level.SEVERE, "Thread was interrupted", ex);
            Thread.currentThread().interrupt();
        } catch (final ExecutionException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }

        if (attributeModificationCounter != attrPlugin.getAttributeModificationCounter()) {
            attributes = attrPlugin.getAttributes();
            attributeModificationCounter = attrPlugin.getAttributeModificationCounter();

            for (Component c : panelFind.getComponents()) {
                if (c instanceof FindCriteriaPanel) {
                    final FindCriteriaPanel fcp = (FindCriteriaPanel) c;
                    fcp.updateAttributes(false, attributes);
                }
            }
            replacePanel.updateAttributes(attributes);
            basicFindPanel.updateAttributes(attributes);

            // Only enable search if we have attributes to search over:
            if (jTabbedPane1.getSelectedIndex() == 0) {// only in find
                btnFind.setEnabled(!attributes.isEmpty());
            }
        }
    }

    /**
     * Make the graph in the specified node the source for the Find Component.
     * <p>
     * If another graph is attached to the model, it is detached first.
     *
     * @param node The GraphNode containing the graph to be displayed.
     */
    private void setNode(final GraphNode node) {
        if (graphNode != null) {
            final Graph graph = graphNode.getGraph();

            // As we are navigating away from this graph, save the state and remove the listeners.
            saveStateToGraph();
            graph.removeGraphChangeListener(this);
            replacePanel.reset();
            toggleUI(false);
        }

        if (node != null) {
            graphNode = node;
            final Graph graph = graphNode.getGraph();

            // Force a refresh as we are moving graphs!
            attributeModificationCounter = -1;
            determineAttributes();

            graph.addGraphChangeListener(this);

            ReadableGraph rg = graph.getReadableGraph();
            try {
                readStateFromGraph(rg);
            } finally {
                rg.release();
            }

            toggleUI(true);
        } else {
            graphNode = null;

            // Hide interface:
            toggleUI(false);
        }
    }

    private void tabbedPaneChanged() {
        int currentIndex = jTabbedPane1.getSelectedIndex();
        switch (currentIndex) {
            case 0:
                //find
                lblMatchPA.setVisible(false);
                lblMatchPA.setEnabled(false);
                cmbMatch.setVisible(false);
                cmbMatch.setEnabled(false);
                lblMatchPB.setVisible(false);
                lblMatchPB.setEnabled(false);
                chkAddToSelection.setVisible(true);
                chkAddToSelection.setEnabled(true);
                btnFind.setEnabled(basicFindPanel.isValidity());
                lblSelect.setText("Find");
                btnFind.setText("Find");
                break;
            case 1:
                //replace
                lblMatchPA.setVisible(false);
                lblMatchPA.setEnabled(false);
                cmbMatch.setVisible(false);
                cmbMatch.setEnabled(false);
                lblMatchPB.setVisible(false);
                lblMatchPB.setEnabled(false);
                chkAddToSelection.setVisible(false);
                chkAddToSelection.setEnabled(false);
                btnFind.setEnabled(replacePanel.isValidity());
                lblSelect.setText("Replace");
                btnFind.setText("Replace");
                break;
            case 2:
                //advanced find
                lblMatchPA.setVisible(true);
                lblMatchPA.setEnabled(true);
                cmbMatch.setVisible(true);
                cmbMatch.setEnabled(true);
                lblMatchPB.setVisible(true);
                lblMatchPB.setEnabled(true);
                chkAddToSelection.setVisible(true);
                chkAddToSelection.setEnabled(true);
                lblSelect.setText("Find");
                btnFind.setText("Find");
                break;
            default:
                break;
        }
    }
}
