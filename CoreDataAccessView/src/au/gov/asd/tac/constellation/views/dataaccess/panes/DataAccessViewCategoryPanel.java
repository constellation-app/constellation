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
import java.util.ListIterator;
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
    private static final Map<String, List<DataAccessPlugin>> ALL_PLUGINS = DataAccessUtilities.getAllPlugins();
    private static final Map<String, List<DataAccessPlugin>> CATEGORIES = ALL_PLUGINS.entrySet()
            .stream()
            .filter(entry -> !entry.getValue().isEmpty())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    private static final List<String> DAV_CATEGORIES = new ArrayList<>(CATEGORIES.keySet());
    private final List<String> visibleResultList;

    private final DefaultListModel<String> visibleListModel;
    private final DefaultListModel<String> hiddenListModel;

    DataAccessViewCategoryPanel(final DataAccessViewCategoryPanelController controller) {
        this.controller = controller;
        initComponents();

        visibleListModel = new DefaultListModel<>();
        hiddenListModel = new DefaultListModel<>();

        final String davHiddenString = LookupPluginsTask.DAV_CATS;
        final List<String> davHiddenList = Arrays.asList(LookupPluginsTask.addCategoryToList(davHiddenString));

        visibleResultList = (davHiddenList == null || davHiddenList.isEmpty()) ? DAV_CATEGORIES : ListUtils.subtract(DAV_CATEGORIES, davHiddenList);
    }

    public List<String> getVisibleCategory() {
        if (visibleList.getModel().getSize() != 0) {
            final List<String> visibleCategoryList = new ArrayList<>(visibleList.getModel().getSize());
            for (int i = 0; i < visibleList.getModel().getSize(); i++) {
                visibleCategoryList.add(visibleList.getModel().getElementAt(i));
            }
            return visibleCategoryList;
        }
        return Collections.emptyList();
    }

    public List<String> getHiddenCategory() {
        if (hiddenList.getModel().getSize() != 0) {
            final List<String> hiddenCategoryList = new ArrayList<>(hiddenList.getModel().getSize());
            for (int i = 0; i < hiddenList.getModel().getSize(); i++) {
                hiddenCategoryList.add(hiddenList.getModel().getElementAt(i));
            }
            return hiddenCategoryList;
        }
        return Collections.emptyList();
    }

    public void setVisibleCategory(final String categories) {
//      Set visible list with the dynamic list of categories
        if (!categories.trim().isEmpty()) {
            getlistModelLeft().removeAllElements();
            final String visibleCategories = categories.replace("[", "");
            final String visibleCategoriesFinal = visibleCategories.replace("]", "");
            final String[] visibleArray = visibleCategoriesFinal.split(SeparatorConstants.COMMA);
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
            final String hiddenCatergories = categories.replace("[", "");
            final String hiddenCatergoriesFinal = hiddenCatergories.replace("]", "");
            final String[] hiddenArray = hiddenCatergoriesFinal.split(SeparatorConstants.COMMA);
            for (int i = 0; i < hiddenArray.length; i++) {
                getlistModelRight().addElement(hiddenArray[i].trim());
            }
        }
        hiddenList.removeAll();
        hiddenList.setModel(getlistModelRight());
    }

    public DefaultListModel<String> getlistModelLeft() {
        return this.visibleListModel;
    }

    public DefaultListModel<String> getlistModelRight() {
        return this.hiddenListModel;
    }

    public List<String> getVisibleResultList() {
        return this.visibleResultList;
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
        buttonRight = new javax.swing.JButton();
        buttonLeft = new javax.swing.JButton();

        jScrollPane1.setViewportView(visibleList);

        jScrollPane2.setViewportView(hiddenList);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(DataAccessViewCategoryPanel.class, "DataAccessViewCategoryPanel.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(DataAccessViewCategoryPanel.class, "DataAccessViewCategoryPanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(DataAccessViewCategoryPanel.class, "DataAccessViewCategoryPanel.jLabel2.text")); // NOI18N

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/au/gov/asd/tac/constellation/views/dataaccess/panes/resources/warning.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(DataAccessViewCategoryPanel.class, "DataAccessViewCategoryPanel.jLabel4.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(buttonRight, org.openide.util.NbBundle.getMessage(DataAccessViewCategoryPanel.class, "DataAccessViewCategoryPanel.buttonRight.text")); // NOI18N
        buttonRight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRightActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(buttonLeft, org.openide.util.NbBundle.getMessage(DataAccessViewCategoryPanel.class, "DataAccessViewCategoryPanel.buttonLeft.text")); // NOI18N
        buttonLeft.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonLeftActionPerformed(evt);
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
                            .addComponent(buttonRight)
                            .addComponent(buttonLeft))
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
                        .addComponent(buttonRight)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonLeft))
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

    private void buttonRightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRightActionPerformed

        if (visibleList.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(OptionPanel, "No Category selected...", "Error", 1);
        } else {
            //Add selected options to hidden list
            final List<String> selectedValues = visibleList.getSelectedValuesList();
            ListIterator<String> selectedValuesIterator = selectedValues.listIterator();
            while (selectedValuesIterator.hasNext()) {
                hiddenListModel.addElement(selectedValuesIterator.next());
            }
            hiddenList.setModel(hiddenListModel);

            //Remove seleted options from visible list
            if (visibleListModel.getSize() != 0) {
                selectedValuesIterator = selectedValues.listIterator();
                while (selectedValuesIterator.hasNext()) {
                    visibleListModel.removeElement(selectedValuesIterator.next());
                }
            }
            visibleList.setModel(visibleListModel);
        }
    }//GEN-LAST:event_buttonRightActionPerformed

    private void buttonLeftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonLeftActionPerformed

        if (hiddenList.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(OptionPanel, "No Category selected...", "Error", 1);
        } else {
            //Add selected options to visible list
            final List<String> selectedValues = hiddenList.getSelectedValuesList();
            ListIterator<String> selectedValuesIterator = selectedValues.listIterator();
            while (selectedValuesIterator.hasNext()) {
                visibleListModel.addElement(selectedValuesIterator.next());
            }
            visibleList.setModel(visibleListModel);

            //Remove selected options from hidden list
            if (hiddenListModel.getSize() != 0) {
                selectedValuesIterator = selectedValues.listIterator();
                while (selectedValuesIterator.hasNext()) {
                    hiddenListModel.removeElement(selectedValuesIterator.next());
                }
            }
            hiddenList.setModel(hiddenListModel);
        }
    }//GEN-LAST:event_buttonLeftActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel OptionPanel;
    private javax.swing.JButton buttonLeft;
    private javax.swing.JButton buttonRight;
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
