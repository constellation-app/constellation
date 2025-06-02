/*
 * Copyright 2010-2025 Australian Signals Directorate
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
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;

/**
 * UI panel for the Data Access View categories.
 *
 * @author mimosa
 * @author sol695510
 */
final class DataAccessViewCategoryPanel extends javax.swing.JPanel {
    
    private static final String NO_CATEGORY_SELECTED = "No Category Selected";
    private static final String PLEASE_SELECT_CATEGORY_MESSAGE = "Please select a category first";

    private final DataAccessViewCategoryPanelController controller;

    private final DefaultListModel<String> visibleListModel;
    private final DefaultListModel<String> hiddenListModel;

    private final List<String> visibleResultList;
    
    private final Map<String, List<DataAccessPlugin>> allPlugins = DataAccessUtilities.getAllPlugins();

    private final List<String> availableCategories = allPlugins
            .entrySet()
            .stream()
            .filter(category -> !category.getValue().isEmpty())
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

    DataAccessViewCategoryPanel(final DataAccessViewCategoryPanelController controller) {
        this.controller = controller;
        initComponents();

        visibleList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        hiddenList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        visibleListModel = new DefaultListModel<>();
        hiddenListModel = new DefaultListModel<>();

        visibleResultList = new ArrayList<>(new LookupPluginsTask().get().keySet());
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
        // Set visible list with the dynamic list of categories.
        if (!categories.trim().isEmpty()) {
            getlistModelLeft().removeAllElements();
            final String[] visibleCategoryArray = (categories.replace("[", "").replace("]", "")).split(SeparatorConstants.COMMA);
            for (final String visibleCategory : visibleCategoryArray) {
                getlistModelLeft().addElement(visibleCategory.trim());
            }
        }
        visibleList.removeAll();
        visibleList.setModel(getlistModelLeft());
    }

    public void setHiddenCategory(final String categories) {
        // Set hidden list with the preference file options OR default.
        if (!categories.trim().isEmpty()) {
            getlistModelRight().removeAllElements();
            final String[] hiddenCategoryArray = (categories.replace("[", "").replace("]", "")).split(SeparatorConstants.COMMA);
            for (final String hiddenCategory : hiddenCategoryArray) {
                getlistModelRight().addElement(hiddenCategory.trim());
            }
        }
        hiddenList.removeAll();
        hiddenList.setModel(getlistModelRight());
    }

    public DefaultListModel<String> getlistModelLeft() {
        return visibleListModel;
    }

    public DefaultListModel<String> getlistModelRight() {
        return hiddenListModel;
    }

    public List<String> getVisibleResultList() {
        return Collections.unmodifiableList(visibleResultList);
    }
    
    protected void restoreDefaults(){
        visibleListModel.clear();
        visibleListModel.addAll(availableCategories);
        hiddenListModel.clear();
        controller.setMoveButtonPressed(true);
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
        buttonRight = new javax.swing.JButton();
        buttonLeft = new javax.swing.JButton();
        buttonUp = new javax.swing.JButton();
        buttonDown = new javax.swing.JButton();
        buttonRestore = new javax.swing.JButton();

        jScrollPane1.setViewportView(visibleList);

        jScrollPane2.setViewportView(hiddenList);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(DataAccessViewCategoryPanel.class, "DataAccessViewCategoryPanel.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(DataAccessViewCategoryPanel.class, "DataAccessViewCategoryPanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(DataAccessViewCategoryPanel.class, "DataAccessViewCategoryPanel.jLabel2.text")); // NOI18N

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

        org.openide.awt.Mnemonics.setLocalizedText(buttonUp, org.openide.util.NbBundle.getMessage(DataAccessViewCategoryPanel.class, "DataAccessViewCategoryPanel.buttonUp.text")); // NOI18N
        buttonUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonUpActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(buttonDown, org.openide.util.NbBundle.getMessage(DataAccessViewCategoryPanel.class, "DataAccessViewCategoryPanel.buttonDown.text")); // NOI18N
        buttonDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonDownActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(buttonRestore, org.openide.util.NbBundle.getMessage(DataAccessViewCategoryPanel.class, "DataAccessViewCategoryPanel.buttonRestore.text")); // NOI18N
        buttonRestore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRestoreActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout OptionPanelLayout = new javax.swing.GroupLayout(OptionPanel);
        OptionPanel.setLayout(OptionPanelLayout);
        OptionPanelLayout.setHorizontalGroup(
            OptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, OptionPanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 319, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(113, 113, 113))
            .addGroup(OptionPanelLayout.createSequentialGroup()
                .addGap(78, 78, 78)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addGap(144, 144, 144))
            .addGroup(OptionPanelLayout.createSequentialGroup()
                .addGroup(OptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(OptionPanelLayout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addGroup(OptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(OptionPanelLayout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(46, 46, 46)
                                .addGroup(OptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(buttonRight, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(buttonLeft, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(46, 46, 46)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(buttonRestore)))
                    .addGroup(OptionPanelLayout.createSequentialGroup()
                        .addGap(75, 75, 75)
                        .addComponent(buttonUp)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(buttonDown)))
                .addContainerGap(100, Short.MAX_VALUE))
        );
        OptionPanelLayout.setVerticalGroup(
            OptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(OptionPanelLayout.createSequentialGroup()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(OptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(OptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(OptionPanelLayout.createSequentialGroup()
                        .addGap(35, 35, 35)
                        .addComponent(buttonRight)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(buttonLeft))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)
                    .addComponent(jScrollPane2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(OptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonUp)
                    .addComponent(buttonDown))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 58, Short.MAX_VALUE)
                .addComponent(buttonRestore)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(OptionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
            JOptionPane.showMessageDialog(OptionPanel, PLEASE_SELECT_CATEGORY_MESSAGE, NO_CATEGORY_SELECTED, JOptionPane.WARNING_MESSAGE);
        } else {
            // Add selected options to hidden list.
            final List<String> selectedValues = visibleList.getSelectedValuesList();
            ListIterator<String> selectedValuesIterator = selectedValues.listIterator();
            while (selectedValuesIterator.hasNext()) {
                hiddenListModel.addElement(selectedValuesIterator.next());
            }
            hiddenList.setModel(hiddenListModel);

            // Remove seleted options from visible list.
            if (visibleListModel.getSize() != 0) {
                selectedValuesIterator = selectedValues.listIterator();
                while (selectedValuesIterator.hasNext()) {
                    visibleListModel.removeElement(selectedValuesIterator.next());
                }
            }
            visibleList.setModel(visibleListModel);
            controller.setMoveButtonPressed(true);
        }
    }//GEN-LAST:event_buttonRightActionPerformed

    private void buttonLeftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonLeftActionPerformed

        if (hiddenList.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(OptionPanel, PLEASE_SELECT_CATEGORY_MESSAGE, NO_CATEGORY_SELECTED, JOptionPane.WARNING_MESSAGE);
        } else {
            // Add selected options to visible list.
            final List<String> selectedValues = hiddenList.getSelectedValuesList();
            ListIterator<String> selectedValuesIterator = selectedValues.listIterator();
            while (selectedValuesIterator.hasNext()) {
                visibleListModel.addElement(selectedValuesIterator.next());
            }
            visibleList.setModel(visibleListModel);

            // Remove selected options from hidden list.
            if (hiddenListModel.getSize() != 0) {
                selectedValuesIterator = selectedValues.listIterator();
                while (selectedValuesIterator.hasNext()) {
                    hiddenListModel.removeElement(selectedValuesIterator.next());
                }
            }
            hiddenList.setModel(hiddenListModel);
            controller.setMoveButtonPressed(true);
        }
    }//GEN-LAST:event_buttonLeftActionPerformed

    private void buttonUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonUpActionPerformed
        if (visibleList.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(OptionPanel, PLEASE_SELECT_CATEGORY_MESSAGE, NO_CATEGORY_SELECTED, JOptionPane.WARNING_MESSAGE);
        } else {
            final int[] selectedIndices = visibleList.getSelectedIndices();

            if (selectedIndices[0] != 0) {
                final int beforeIndex = selectedIndices[0] - 1;
                final List<String> selectedItems = visibleList.getSelectedValuesList();

                visibleListModel.removeRange(selectedIndices[0], selectedIndices[selectedIndices.length - 1]);

                visibleListModel.addAll(beforeIndex, selectedItems);

                visibleList.setModel(visibleListModel);

                for (int i = 0; i < selectedIndices.length; i++) {
                    selectedIndices[i]--;
                }

                visibleList.setSelectedIndices(selectedIndices);
                controller.setReorderButtonPressed(true);
            }
        }
    }//GEN-LAST:event_buttonUpActionPerformed

    private void buttonDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonDownActionPerformed
        if (visibleList.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(OptionPanel, PLEASE_SELECT_CATEGORY_MESSAGE, NO_CATEGORY_SELECTED, JOptionPane.WARNING_MESSAGE);
        } else {
            final int[] selectedIndices = visibleList.getSelectedIndices();

            if (selectedIndices[selectedIndices.length - 1] != visibleList.getModel().getSize() - 1) {
                final int afterIndex = selectedIndices[selectedIndices.length - 1] + 1;
                final List<String> selectedItems = visibleList.getSelectedValuesList();

                visibleListModel.removeRange(selectedIndices[0], selectedIndices[selectedIndices.length - 1]);

                visibleListModel.addAll(afterIndex - selectedIndices.length + 1, selectedItems);

                visibleList.setModel(visibleListModel);

                for (int i = 0; i < selectedIndices.length; i++) {
                    selectedIndices[i]++;
                }

                visibleList.setSelectedIndices(selectedIndices);
                controller.setReorderButtonPressed(true);
            }
        }
    }//GEN-LAST:event_buttonDownActionPerformed

    private void buttonRestoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRestoreActionPerformed
        restoreDefaults();
    }//GEN-LAST:event_buttonRestoreActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel OptionPanel;
    private javax.swing.JButton buttonDown;
    private javax.swing.JButton buttonLeft;
    private javax.swing.JButton buttonRestore;
    private javax.swing.JButton buttonRight;
    private javax.swing.JButton buttonUp;
    private javax.swing.JList<String> hiddenList;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JList<String> visibleList;
    // End of variables declaration//GEN-END:variables
}
