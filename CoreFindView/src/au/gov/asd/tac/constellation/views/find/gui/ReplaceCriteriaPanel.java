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
import au.gov.asd.tac.constellation.views.find.advanced.FindRule;
import java.awt.Color;
import java.util.ArrayList;

/**
 * similar to find criteria panel but for replace functionality
 *
 * @author twinkle2_little
 */
public class ReplaceCriteriaPanel extends javax.swing.JPanel {

    private final FindCriteriaPanel findCriteriaPanel;
    private final FindCriteriaPanel replaceCriteriaPanel;

    /**
     * Creates new form ReplaceCriteriaPanel
     *
     * @param parent the FindTopComponent this panel will belong to.
     * @param attributes the attributes to include.
     */
    public ReplaceCriteriaPanel(final FindTopComponent parent, final ArrayList<Attribute> attributes) {
        initComponents();
        findCriteriaPanel = new FindCriteriaPanel(parent, attributes);
        findPanel.add(findCriteriaPanel);
        replaceCriteriaPanel = new FindCriteriaPanel(parent, attributes);
        replacePanel.add(replaceCriteriaPanel);
        javax.swing.GroupLayout replacePanelLayout = new javax.swing.GroupLayout(replacePanel);
        replacePanel.setLayout(replacePanelLayout);
        replacePanelLayout.setHorizontalGroup(
                replacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, replacePanelLayout.createSequentialGroup()
                                .addComponent(replaceCriteriaPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)));
        replacePanelLayout.setVerticalGroup(
                replacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(replacePanelLayout.createSequentialGroup()
                                .addComponent(replaceCriteriaPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)));

        javax.swing.GroupLayout findPanelLayout = new javax.swing.GroupLayout(findPanel);
        findPanel.setLayout(findPanelLayout);
        findPanelLayout.setHorizontalGroup(
                findPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, findPanelLayout.createSequentialGroup()
                                .addComponent(findCriteriaPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)));
        findPanelLayout.setVerticalGroup(
                findPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(findPanelLayout.createSequentialGroup()
                                .addComponent(findCriteriaPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)));
    }

    /**
     * Creates new form ReplaceCriteriaPanel
     *
     * @param parent the FindTopComponent this panel will belong to.
     * @param localState the local state.
     * @param attributes the attributes to include.
     */
    public ReplaceCriteriaPanel(final FindTopComponent parent, final FindRule localState, final ArrayList<Attribute> attributes) {
        initComponents();
        findCriteriaPanel = new FindCriteriaPanel(parent, localState, attributes);
        findPanel.add(findCriteriaPanel);
        replaceCriteriaPanel = new FindCriteriaPanel(parent, localState, attributes);
        replacePanel.add(replaceCriteriaPanel);

        javax.swing.GroupLayout replacePanelLayout = new javax.swing.GroupLayout(replacePanel);
        replacePanel.setLayout(replacePanelLayout);
        replacePanelLayout.setHorizontalGroup(
                replacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, replacePanelLayout.createSequentialGroup()
                                .addComponent(replaceCriteriaPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)));
        replacePanelLayout.setVerticalGroup(
                replacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(replacePanelLayout.createSequentialGroup()
                                .addComponent(replaceCriteriaPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)));

        javax.swing.GroupLayout findPanelLayout = new javax.swing.GroupLayout(findPanel);
        findPanel.setLayout(findPanelLayout);
        findPanelLayout.setHorizontalGroup(
                findPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, findPanelLayout.createSequentialGroup()
                                .addComponent(findCriteriaPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)));
        findPanelLayout.setVerticalGroup(
                findPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(findPanelLayout.createSequentialGroup()
                                .addComponent(findCriteriaPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)));

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
        findPanel = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        replacePanel = new javax.swing.JPanel();

        setMaximumSize(new java.awt.Dimension(32767, 150));
        setMinimumSize(new java.awt.Dimension(0, 150));
        setPreferredSize(new java.awt.Dimension(838, 150));
        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        layout.columnWidths = new int[] {1};
        layout.rowHeights = new int[] {5};
        setLayout(layout);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ReplaceCriteriaPanel.class, "ReplaceCriteriaPanel.jLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        add(jLabel1, gridBagConstraints);

        findPanel.setMaximumSize(new java.awt.Dimension(32767, 47));
        findPanel.setMinimumSize(new java.awt.Dimension(100, 47));
        findPanel.setPreferredSize(new java.awt.Dimension(838, 47));

        javax.swing.GroupLayout findPanelLayout = new javax.swing.GroupLayout(findPanel);
        findPanel.setLayout(findPanelLayout);
        findPanelLayout.setHorizontalGroup(
            findPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 778, Short.MAX_VALUE)
        );
        findPanelLayout.setVerticalGroup(
            findPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 50, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(findPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        add(jSeparator1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ReplaceCriteriaPanel.class, "ReplaceCriteriaPanel.jLabel2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        add(jLabel2, gridBagConstraints);

        replacePanel.setMaximumSize(new java.awt.Dimension(32767, 47));
        replacePanel.setMinimumSize(new java.awt.Dimension(100, 47));
        replacePanel.setPreferredSize(new java.awt.Dimension(838, 47));

        javax.swing.GroupLayout replacePanelLayout = new javax.swing.GroupLayout(replacePanel);
        replacePanel.setLayout(replacePanelLayout);
        replacePanelLayout.setHorizontalGroup(
            replacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 778, Short.MAX_VALUE)
        );
        replacePanelLayout.setVerticalGroup(
            replacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 50, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(replacePanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel findPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPanel replacePanel;
    // End of variables declaration//GEN-END:variables

    public void updateAttributes(boolean b, ArrayList<Attribute> attributes) {
        findCriteriaPanel.updateAttributes(b, attributes);
        replaceCriteriaPanel.updateAttributes(b, attributes);
    }

    public FindRule getState() {
        FindRule result = null;
        if (findCriteriaPanel.getState().equals(replaceCriteriaPanel.getState())) {
            result = findCriteriaPanel.getState();
        }
        return result;
    }

//    @Override
//    public void repaint() {
//        super.repaint();
//        if (findPanel != null) {
//            findPanel.repaint();
//        }
//        if (findCriteriaPanel != null) {
//            findCriteriaPanel.repaint();
//        }
//        if (replacePanel != null) {
//            replacePanel.repaint();
//        }
//        if (replaceCriteriaPanel != null) {
//            replaceCriteriaPanel.repaint();
//        }
//    }
    @Override
    public void setBackground(final Color color) {
        super.setBackground(color);

        if (findPanel != null) {
            findPanel.setBackground(color);
        }
        if (findCriteriaPanel != null) {
            findCriteriaPanel.setBackground(color);
        }
        if (replacePanel != null) {
            replacePanel.setBackground(color);
        }
        if (replaceCriteriaPanel != null) {
            replaceCriteriaPanel.setBackground(color);
        }

    }

//    @Override
//    public void validate() {
//        super.validate();
//        if (findPanel != null) {
//            findPanel.validate();
//        }
//        if (findCriteriaPanel != null) {
//            findCriteriaPanel.validate();
//        }
//        if (replacePanel != null) {
//            replacePanel.validate();
//        }
//        if (replaceCriteriaPanel != null) {
//            replaceCriteriaPanel.validate();
//        }
//    }
}
