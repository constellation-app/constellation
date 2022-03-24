/*
 * Copyright 2010-2022 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.tasks.LookupPluginsTask;
import au.gov.asd.tac.constellation.views.dataaccess.utilities.DataAccessUtilities;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import org.apache.commons.collections4.ListUtils;

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
    public final List<String> visibleResultList;

    public DefaultListModel visibleListModel;
    public DefaultListModel hiddenListModel;

    DataAccessViewCategoryPanel(DataAccessViewCategoryPanelController controller) {
        this.controller = controller;
        initComponents();

        visibleListModel = new DefaultListModel();
        hiddenListModel = new DefaultListModel();

        final String davHiddenString = LookupPluginsTask.DAV_CATS;
        final List<String> davHiddenList = Arrays.asList(LookupPluginsTask.addCategoryToList(davHiddenString));

        visibleResultList = (davHiddenList == null || davHiddenList.isEmpty()) ? DAV_CATEGORIES : ListUtils.subtract(DAV_CATEGORIES, davHiddenList);
    }

    public List<String> getVisibleCategory() {
        if (visibleList.getModel().getSize() != 0) {
            final List<String> VISIBLE_DAV_CATEGORY_ARRAY = new ArrayList(visibleList.getModel().getSize());
            for (int i = 0; i < visibleList.getModel().getSize(); i++) {
                VISIBLE_DAV_CATEGORY_ARRAY.add(visibleList.getModel().getElementAt(i));
            }
            return VISIBLE_DAV_CATEGORY_ARRAY;
        }
        return Collections.emptyList();
    }

    public List<String> getHiddenCategory() {
        if (hiddenList.getModel().getSize() != 0) {
            final List<String> HIDDEN_DAV_CATEGORY_ARRAY = new ArrayList(hiddenList.getModel().getSize());
            for (int i = 0; i < hiddenList.getModel().getSize(); i++) {
                HIDDEN_DAV_CATEGORY_ARRAY.add(hiddenList.getModel().getElementAt(i));
            }
            return HIDDEN_DAV_CATEGORY_ARRAY;
        }
        return Collections.emptyList();
    }

    public void setVisibleCategory(final String categories) {
//      Set visible list with the dynamic list of categories
        if (!categories.trim().isEmpty()) {
            getlistModelLeft().removeAllElements();
            final String visibleCategories = categories.replaceAll("\\[", "").replaceAll("\\]", "");
            final String[] visibleArray = visibleCategories.split(SeparatorConstants.COMMA);
            for (int i = 0; i < visibleArray.length; i++) {
                getlistModelLeft().addElement(visibleArray[i].trim());
            }
        }
        visibleList.removeAll();
        visibleList.setModel(getlistModelLeft());
    }

    public void setHiddenCategory(final String categories) {
//      Set hidden list with the preference file options OR default
        if (!categories.trim().isEmpty()) {
            getlistModelRight().removeAllElements();
            final String hiddenCategories = categories.replaceAll("\\[", "").replaceAll("\\]", "");
            final String[] hiddenArray = hiddenCategories.split(SeparatorConstants.COMMA);
            for (int i = 0; i < hiddenArray.length; i++) {
                getlistModelRight().addElement(hiddenArray[i].trim());
            }
        }
        hiddenList.removeAll();
        hiddenList.setModel(getlistModelRight());
    }

    public DefaultListModel getlistModelLeft() {
        return visibleListModel;
    }

    public DefaultListModel getlistModelRight() {
        return hiddenListModel;
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
        visibleList = new javax.swing.JList<>();
        jScrollPane2 = new javax.swing.JScrollPane();
        hiddenList = new javax.swing.JList<>();
        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        buttonDoubleRight = new javax.swing.JButton();
        buttonDoubleLeft = new javax.swing.JButton();

        jScrollPane1.setViewportView(visibleList);

        jScrollPane2.setViewportView(hiddenList);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(DataAccessViewCategoryPanel.class, "DataAccessViewCategoryPanel.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(DataAccessViewCategoryPanel.class, "DataAccessViewCategoryPanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(DataAccessViewCategoryPanel.class, "DataAccessViewCategoryPanel.jLabel2.text")); // NOI18N

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel4.setIcon(new javax.swing.ImageIcon("D:\\Code\\C\\constellation\\CorePreferences\\src\\au\\gov\\asd\\tac\\constellation\\preferences\\resources\\warning.png")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(DataAccessViewCategoryPanel.class, "DataAccessViewCategoryPanel.jLabel4.text")); // NOI18N

        buttonDoubleRight.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(buttonDoubleRight, org.openide.util.NbBundle.getMessage(DataAccessViewCategoryPanel.class, "DataAccessViewCategoryPanel.buttonDoubleRight.text")); // NOI18N
        buttonDoubleRight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonDoubleRightActionPerformed(evt);
            }
        });

        buttonDoubleLeft.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(buttonDoubleLeft, org.openide.util.NbBundle.getMessage(DataAccessViewCategoryPanel.class, "DataAccessViewCategoryPanel.buttonDoubleLeft.text")); // NOI18N
        buttonDoubleLeft.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonDoubleLeftActionPerformed(evt);
            }
        });

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
                            .addComponent(buttonDoubleRight)
                            .addComponent(buttonDoubleLeft))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 91, Short.MAX_VALUE)
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
                        .addGap(53, 53, 53)
                        .addComponent(buttonDoubleRight)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonDoubleLeft))
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
                .addContainerGap(440, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(OptionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(53, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void buttonDoubleRightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonDoubleRightActionPerformed
        if (visibleList.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(OptionPanel, "No Category selected...", "Error", 1);
        } else {
            //Add selected options to hidden list
           final List<String> selectedValues = visibleList.getSelectedValuesList();
           final int[] selectedIndices = visibleList.getSelectedIndices();
           final Object selectedValuesArray[] = selectedValues.toArray();

            for (int i = 0; i < selectedValues.size(); i++) {
                hiddenListModel.addElement(selectedValuesArray[i]);
            }
            hiddenList.setModel(hiddenListModel);

            //Remove seleted options from visible list
            if (visibleListModel.getSize() != 0) {
                for (int i = 0; i < selectedValues.size(); i++) {
                    visibleListModel.removeElement(selectedValuesArray[i]);
                }
            }
            visibleList.setModel(visibleListModel);
        }

    }//GEN-LAST:event_buttonDoubleRightActionPerformed

    private void buttonDoubleLeftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonDoubleLeftActionPerformed
         if (hiddenList.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(OptionPanel, "No Category selected...", "Error", 1);
        } else {
            //Add selected options to visible list
           final List<String> selectedValues = hiddenList.getSelectedValuesList();
           final int[] selectedIndices = hiddenList.getSelectedIndices();
           final Object selectedValuesArray[] = selectedValues.toArray();

            for (int i = 0; i < selectedValues.size(); i++) {
                visibleListModel.addElement(selectedValuesArray[i]);
            }
            visibleList.setModel(visibleListModel);

            //Remove selected options from hidden list
            if (hiddenListModel.getSize() != 0) {
                for (int i = 0; i < selectedValues.size(); i++) {
                    hiddenListModel.removeElement(selectedValuesArray[i]);
                }
            }
            hiddenList.setModel(hiddenListModel);
        }
    }//GEN-LAST:event_buttonDoubleLeftActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel OptionPanel;
    private javax.swing.JButton buttonDoubleLeft;
    private javax.swing.JButton buttonDoubleRight;
    private javax.swing.JList<String> hiddenList;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JList<String> visibleList;
    // End of variables declaration//GEN-END:variables
}
