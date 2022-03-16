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
package au.gov.asd.tac.constellation.views.dataaccess.panes;

import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.utilities.DataAccessUtilities;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;

/**
 * UI panel for the Data Access View categories.
 *
 * @author mimosa
 */
final class DataAccessViewCategoryPanel extends javax.swing.JPanel {

    private final DataAccessViewCategoryPanelController controller;
    public static final Map<String, List<DataAccessPlugin>> allPlugins = DataAccessUtilities.getAllPlugins();
    public static final Map<String, List<DataAccessPlugin>> categories = allPlugins.entrySet()
            .stream()
            .filter(entry -> !entry.getValue().isEmpty())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    public static final List<String> DAV_CATEGORIES = new ArrayList<>(categories.keySet());
    public static final String[] DAV_CATEGORY_ARRAY = DAV_CATEGORIES.stream().toArray(String[]::new);

    DefaultListModel listLeftModel, listRightModel;

    DataAccessViewCategoryPanel(DataAccessViewCategoryPanelController controller) {
        this.controller = controller;
        initComponents();

        listLeftModel = new DefaultListModel();
        listRightModel = new DefaultListModel();

        for (int i = 0; i < DAV_CATEGORY_ARRAY.length; i++) {
            listLeftModel.addElement(DAV_CATEGORY_ARRAY[i]);
        }
        listLeft.setModel(listLeftModel);

    }

    // SY CHECK THIS
    public List<String> getLeftCategory() {
        if (listLeft.getModel().getSize() != 0) {
            List<String> LEFT_DAV_CATEGORY_ARRAY = new ArrayList(listLeft.getModel().getSize());
            for (int i = 0; i < listLeft.getModel().getSize(); i++) {
                LEFT_DAV_CATEGORY_ARRAY.add(listLeft.getModel().getElementAt(i));
            }
            return LEFT_DAV_CATEGORY_ARRAY;
        }
        return Collections.<String>emptyList();
    }

// SY CHECK THIS
    public List<String> getRightCategory() {
        if (listRight.getModel().getSize() != 0) {
            List<String> RIGHT_DAV_CATEGORY_ARRAY = new ArrayList(listRight.getModel().getSize());
            for (int i = 0; i < listRight.getModel().getSize(); i++) {
                RIGHT_DAV_CATEGORY_ARRAY.add(listRight.getModel().getElementAt(i));
            }
            return RIGHT_DAV_CATEGORY_ARRAY;
        }
        return Collections.<String>emptyList();
    }

    public void setLeftCategory(String visibleCategories) {
//      Set listLeft with the preference file options OR default values
        if (!visibleCategories.trim().isEmpty()) {
            getlistModelLeft().removeAllElements();
            visibleCategories = visibleCategories.replaceAll("\\[", "").replaceAll("\\]", "");
            final String[] visible = visibleCategories.split(",");
            for (int i = 0; i < visible.length; i++) {
                getlistModelLeft().addElement(visible[i].trim());
            }
        }
        listLeft.removeAll();
        listLeft.setModel(getlistModelLeft());
    }

    public void setRightCategory(String hiddenCategories) {
//      Set listLeft with the preference file options OR default
        if (!hiddenCategories.trim().isEmpty()) {
            getlistModelRight().removeAllElements();
            hiddenCategories = hiddenCategories.replaceAll("\\[", "").replaceAll("\\]", "");
            final String[] hidden = hiddenCategories.split(",");
            for (int i = 0; i < hidden.length; i++) {
                getlistModelRight().addElement(hidden[i].trim());
            }
        }
        listRight.removeAll();
        listRight.setModel(getlistModelRight());
    }

    public JList getJlistLeft() {
        return listLeft;
    }

    public JList getJlistRight() {
        return listRight;
    }
    
   public DefaultListModel getlistModelLeft() {
        return listLeftModel;
    }

    public DefaultListModel getlistModelRight() {
        return listRightModel;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        OptionPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        listLeft = new javax.swing.JList<>();
        jScrollPane2 = new javax.swing.JScrollPane();
        listRight = new javax.swing.JList<>();
        jLabel3 = new javax.swing.JLabel();
        buttonSingleRight = new javax.swing.JButton();
        buttonSingleLeft = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();

        jScrollPane1.setViewportView(listLeft);

        jScrollPane2.setViewportView(listRight);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(DataAccessViewCategoryPanel.class, "DataAccessViewCategoryPanel.jLabel3.text")); // NOI18N

        buttonSingleRight.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(buttonSingleRight, org.openide.util.NbBundle.getMessage(DataAccessViewCategoryPanel.class, "DataAccessViewCategoryPanel.buttonSingleRight.text")); // NOI18N
        buttonSingleRight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSingleRightActionPerformed(evt);
            }
        });

        buttonSingleLeft.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(buttonSingleLeft, org.openide.util.NbBundle.getMessage(DataAccessViewCategoryPanel.class, "DataAccessViewCategoryPanel.buttonSingleLeft.text")); // NOI18N
        buttonSingleLeft.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSingleLeftActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(DataAccessViewCategoryPanel.class, "DataAccessViewCategoryPanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(DataAccessViewCategoryPanel.class, "DataAccessViewCategoryPanel.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(DataAccessViewCategoryPanel.class, "DataAccessViewCategoryPanel.jLabel4.text")); // NOI18N

        javax.swing.GroupLayout OptionPanelLayout = new javax.swing.GroupLayout(OptionPanel);
        OptionPanel.setLayout(OptionPanelLayout);
        OptionPanelLayout.setHorizontalGroup(
            OptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(OptionPanelLayout.createSequentialGroup()
                .addGap(85, 85, 85)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addGap(163, 163, 163))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, OptionPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 319, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(119, 119, 119))
            .addGroup(OptionPanelLayout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addGroup(OptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(OptionPanelLayout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(OptionPanelLayout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(46, 46, 46)
                        .addGroup(OptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(buttonSingleRight)
                            .addComponent(buttonSingleLeft))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 62, Short.MAX_VALUE)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(99, 99, 99))))
        );
        OptionPanelLayout.setVerticalGroup(
            OptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(OptionPanelLayout.createSequentialGroup()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(OptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(OptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(OptionPanelLayout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(buttonSingleRight)
                        .addGap(18, 18, 18)
                        .addComponent(buttonSingleLeft))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)
                    .addComponent(jScrollPane2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 56, Short.MAX_VALUE)
                .addComponent(jLabel4)
                .addGap(53, 53, 53))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(OptionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(469, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(OptionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(57, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void buttonSingleRightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSingleRightActionPerformed
        // TODO add your handling code here:
        String str2 = listLeft.getSelectedValue();
        if (listLeft.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(OptionPanel, "No data selected...", "Error", 1);
        } else {
            //Add options to list Right
            int value = listLeft.getSelectedIndex();
            listRightModel.addElement(str2);
            listRight.setModel(listRightModel);

            //Remove options from list Left
            if (listLeftModel.getSize() != 0) {
                listLeftModel.removeElementAt(value);
                listLeft.setModel(listLeftModel);
            }
        }
    }//GEN-LAST:event_buttonSingleRightActionPerformed

    private void buttonSingleLeftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSingleLeftActionPerformed
        // TODO add your handling code here:
        String str2 = listRight.getSelectedValue();
        if (listRight.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(OptionPanel, "No data selected...", "Error", 1);
        } else {
            //Add options to list Left
            int value = listRight.getSelectedIndex();
            listLeftModel.addElement(str2);
            listLeft.setModel(listLeftModel);

            //Remove options from list Right
            if (listRightModel.getSize() != 0) {
                listRightModel.removeElementAt(value);
                listRight.setModel(listRightModel);
            }
        }
    }//GEN-LAST:event_buttonSingleLeftActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel OptionPanel;
    private javax.swing.JButton buttonSingleLeft;
    private javax.swing.JButton buttonSingleRight;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JList<String> listLeft;
    private javax.swing.JList<String> listRight;
    // End of variables declaration//GEN-END:variables
}
