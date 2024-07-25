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
package au.gov.asd.tac.constellation.plugins.importexport.hashmod;

import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import au.gov.asd.tac.constellation.utilities.gui.filechooser.FileChooser;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.lang3.StringUtils;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 * UI panel for the entry of the hashmod attributes
 *
 * @author CrucisGamma
 */
@Messages(
        {
            "MSG_fg=Select foreground color",
            "MSG_bg=Select background color"
        })
public class HashmodPanel extends javax.swing.JPanel {

    private static final String TITLE = "Select a CSV for the Hashmod";

    private String hashmodCSVFileStr = "";
    private boolean isChainedHashmods = false;
    private int numChainedHashmods = 0;
    private Hashmod[] chainedHashmods = new Hashmod[10];

    /**
     * Creates new form HashmodPanel.
     *
     * @param hashmod The Hashmod to be used in the panel.
     */
    public HashmodPanel(final Hashmod hashmod) {
        initComponents();
    }

    public final Hashmod getHashmod() {
        if (!isChainedHashmods()) {
            return new Hashmod(hashmodCSVFileStr);
        }
        return numChainedHashmods > 0 ? chainedHashmods[0] : null;
    }

    public boolean isChainedHashmods() {
        return isChainedHashmods;
    }

    public final Hashmod[] getChainedHashmods() {
        return isChainedHashmods() ? chainedHashmods : null;
    }

    public int numChainedHashmods() {
        return isChainedHashmods() ? numChainedHashmods : 0;
    }

    public void setChainedHashmods(final String filesList) {
        numChainedHashmods = 0;
        isChainedHashmods = false;

        if (StringUtils.isBlank(filesList)) {
            return;
        }

        final String[] fileList = filesList.split(",");
        for (final String file : fileList) {
            if (numChainedHashmods < 10) {
                chainedHashmods[numChainedHashmods] = new Hashmod(file);
                numChainedHashmods++;
                isChainedHashmods = true;
            }
        }
    }

    public void setAttributeNames(final String key, final String attribute1, final String attribute2) {
        keyAttributeTextField.setText(key);
        value1AttributeTextField.setText(attribute1);
        if (attribute2 != null) {
            value2AttributeTextField.setText(attribute2);
        }
    }

    public boolean isCreateVerticesSelected() {
        return createAllCheckbox.isSelected();
    }

    public boolean isCreateAttributesSelected() {
        return createAttributesCheckbox.isSelected();
    }

    public boolean isCreateTransactionsSelected() {
        return createTransactionsCheckbox.isSelected();
    }

    /**
     * Creates a new file chooser.
     *
     * @return the created file chooser.
     */
    public FileChooserBuilder getHasmodFileChooser() {
        return new FileChooserBuilder(TITLE)
                .setTitle(TITLE)
                .setAcceptAllFileFilterUsed(false)
                .setFilesOnly(true)
                .setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(final File file) {
                        final String name = file.getName();
                        return (file.isFile() && StringUtils.endsWithIgnoreCase(name, FileExtensionConstants.COMMA_SEPARATED_VALUE)) || file.isDirectory();
                    }

                    @Override
                    public String getDescription() {
                        return "CSV Files (" + FileExtensionConstants.COMMA_SEPARATED_VALUE + ")";
                    }
                });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        hashmodLabel = new JLabel();
        hashmodCSVFile = new JTextField();
        hashmodButton = new JButton();
        hashmodLabel1 = new JLabel();
        hashmodLabel2 = new JLabel();
        hashmodLabel3 = new JLabel();
        keyAttributeTextField = new JTextField();
        value1AttributeTextField = new JTextField();
        value2AttributeTextField = new JTextField();
        createAllCheckbox = new JCheckBox();
        hashmodLabel4 = new JLabel();
        hashmodLabel5 = new JLabel();
        jScrollPane1 = new JScrollPane();
        jCSVFileList = new JTextArea();
        hashmodButton1 = new JButton();
        hashmodLabel6 = new JLabel();
        createAttributesCheckbox = new JCheckBox();
        hashmodLabel7 = new JLabel();
        createTransactionsCheckbox = new JCheckBox();
        helpButton = new JButton();

        Mnemonics.setLocalizedText(hashmodLabel, NbBundle.getMessage(HashmodPanel.class, "Hashmod.csv.label")); // NOI18N

        hashmodCSVFile.setText(NbBundle.getMessage(HashmodPanel.class, "Hashmod.csv.textfield")); // NOI18N

        Mnemonics.setLocalizedText(hashmodButton, NbBundle.getMessage(HashmodPanel.class, "Hashmod.csv.button")); // NOI18N
        hashmodButton.setMargin(new Insets(2, 0, 2, 0));
        hashmodButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                hashmodButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(hashmodLabel1, NbBundle.getMessage(HashmodPanel.class, "HashmodPanel.hashmodLabel1.text")); // NOI18N

        Mnemonics.setLocalizedText(hashmodLabel2, NbBundle.getMessage(HashmodPanel.class, "HashmodPanel.hashmodLabel2.text")); // NOI18N

        Mnemonics.setLocalizedText(hashmodLabel3, NbBundle.getMessage(HashmodPanel.class, "HashmodPanel.hashmodLabel3.text")); // NOI18N

        keyAttributeTextField.setText(NbBundle.getMessage(HashmodPanel.class, "HashmodPanel.keyAttributeTextField.text")); // NOI18N
        keyAttributeTextField.setEnabled(false);
        keyAttributeTextField.setName("keyAttributeTextField"); // NOI18N

        value1AttributeTextField.setText(NbBundle.getMessage(HashmodPanel.class, "HashmodPanel.value1AttributeTextField.text")); // NOI18N
        value1AttributeTextField.setEnabled(false);
        value1AttributeTextField.setName("value1AttributeTextField"); // NOI18N

        value2AttributeTextField.setText(NbBundle.getMessage(HashmodPanel.class, "HashmodPanel.value2AttributeTextField.text")); // NOI18N
        value2AttributeTextField.setEnabled(false);
        value2AttributeTextField.setName("value2AttributeTextField"); // NOI18N

        Mnemonics.setLocalizedText(createAllCheckbox, NbBundle.getMessage(HashmodPanel.class, "HashmodPanel.createAllCheckbox.text")); // NOI18N

        Mnemonics.setLocalizedText(hashmodLabel4, NbBundle.getMessage(HashmodPanel.class, "HashmodPanel.hashmodLabel4.text")); // NOI18N

        Mnemonics.setLocalizedText(hashmodLabel5, NbBundle.getMessage(HashmodPanel.class, "HashmodPanel.hashmodLabel5.text")); // NOI18N

        jCSVFileList.setColumns(20);
        jCSVFileList.setRows(5);
        jScrollPane1.setViewportView(jCSVFileList);

        Mnemonics.setLocalizedText(hashmodButton1, NbBundle.getMessage(HashmodPanel.class, "HashmodPanel.hashmodButton1.text")); // NOI18N
        hashmodButton1.setMargin(new Insets(2, 0, 2, 0));
        hashmodButton1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                hashmodButton1ActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(hashmodLabel6, NbBundle.getMessage(HashmodPanel.class, "HashmodPanel.hashmodLabel6.text")); // NOI18N

        Mnemonics.setLocalizedText(createAttributesCheckbox, NbBundle.getMessage(HashmodPanel.class, "HashmodPanel.createAttributesCheckbox.text_1")); // NOI18N
        createAttributesCheckbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                createAttributesCheckboxActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(hashmodLabel7, NbBundle.getMessage(HashmodPanel.class, "HashmodPanel.hashmodLabel7.text")); // NOI18N

        Mnemonics.setLocalizedText(createTransactionsCheckbox, NbBundle.getMessage(HashmodPanel.class, "HashmodPanel.createTransactionsCheckbox.text")); // NOI18N
        createTransactionsCheckbox.setEnabled(false);
        createTransactionsCheckbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                createTransactionsCheckboxActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(helpButton, NbBundle.getMessage(HashmodPanel.class, "HashmodPanel.helpButton.text")); // NOI18N
        helpButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                helpButtonMousePressed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(hashmodLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(hashmodCSVFile, GroupLayout.DEFAULT_SIZE, 468, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(hashmodLabel2)
                            .addComponent(hashmodLabel1))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(keyAttributeTextField)
                            .addComponent(value1AttributeTextField)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(hashmodLabel3)
                            .addComponent(hashmodLabel4)
                            .addComponent(hashmodLabel5)
                            .addComponent(hashmodLabel6)
                            .addComponent(hashmodLabel7))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(value2AttributeTextField)
                            .addComponent(jScrollPane1)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addComponent(createAttributesCheckbox)
                                    .addComponent(createAllCheckbox)
                                    .addComponent(createTransactionsCheckbox))
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(hashmodButton, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE)
                    .addComponent(hashmodButton1, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 52, GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(helpButton))
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(hashmodLabel)
                    .addComponent(hashmodButton)
                    .addComponent(hashmodCSVFile, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(hashmodLabel1)
                    .addComponent(keyAttributeTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(hashmodLabel2)
                    .addComponent(value1AttributeTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(hashmodLabel3)
                    .addComponent(value2AttributeTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(createAllCheckbox)
                    .addComponent(hashmodLabel4))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(createAttributesCheckbox)
                    .addComponent(hashmodLabel6))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(createTransactionsCheckbox)
                    .addComponent(hashmodLabel7))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(hashmodLabel5)
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(hashmodButton1)
                            .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 169, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(helpButton)
                        .addGap(33, 33, 33))))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void hashmodButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_hashmodButtonActionPerformed
    {//GEN-HEADEREND:event_hashmodButtonActionPerformed
        FileChooser.openOpenDialog(getHasmodFileChooser()).thenAccept(optionalFile -> optionalFile.ifPresent(file -> {
            final String fname = file.getPath();
            hashmodCSVFile.setText(fname);
            hashmodCSVFileStr = fname;
            final Hashmod thisHashmod = getHashmod();
            if (thisHashmod != null) {
                setAttributeNames(thisHashmod.getCSVKey(), thisHashmod.getCSVHeader(1), thisHashmod.getCSVHeader(2));
            } else {
                setAttributeNames(null, null, null);
            }
        }));
    }//GEN-LAST:event_hashmodButtonActionPerformed

    private void hashmodButton1ActionPerformed(ActionEvent evt) {//GEN-FIRST:event_hashmodButton1ActionPerformed
        final String hashmodCSVList = jCSVFileList.getText().trim();
        if (!hashmodCSVList.isEmpty()) {
            setChainedHashmods(hashmodCSVList);
        }

        if (isChainedHashmods()) {
            final Hashmod[] hashmods = getChainedHashmods();
            final Hashmod firstHashmod = hashmods == null ? null : hashmods[0];
            if (firstHashmod != null) {
                setAttributeNames(firstHashmod.getCSVKey(), firstHashmod.getCSVHeader(1), firstHashmod.getCSVHeader(2));
                hashmodCSVFile.setText(firstHashmod.getCSVFileName());
                hashmodCSVFileStr = firstHashmod.getCSVFileName();
            } else {
                setAttributeNames(null, null, null);
                hashmodCSVFile.setText("null");
                hashmodCSVFileStr = null;
            }

        }
    }//GEN-LAST:event_hashmodButton1ActionPerformed

    private void createAttributesCheckboxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_createAttributesCheckboxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_createAttributesCheckboxActionPerformed

    private void createTransactionsCheckboxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_createTransactionsCheckboxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_createTransactionsCheckboxActionPerformed

    private void helpButtonMousePressed(MouseEvent evt) {//GEN-FIRST:event_helpButtonMousePressed
        final HelpCtx help = new HelpCtx("au.gov.asd.tac.constellation.graph.utilities.hashmod.HashmodPanel");
        help.display();
    }//GEN-LAST:event_helpButtonMousePressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JCheckBox createAllCheckbox;
    private JCheckBox createAttributesCheckbox;
    private JCheckBox createTransactionsCheckbox;
    private JButton hashmodButton;
    private JButton hashmodButton1;
    private JTextField hashmodCSVFile;
    private JLabel hashmodLabel;
    private JLabel hashmodLabel1;
    private JLabel hashmodLabel2;
    private JLabel hashmodLabel3;
    private JLabel hashmodLabel4;
    private JLabel hashmodLabel5;
    private JLabel hashmodLabel6;
    private JLabel hashmodLabel7;
    private JButton helpButton;
    private JTextArea jCSVFileList;
    private JScrollPane jScrollPane1;
    private JTextField keyAttributeTextField;
    private JTextField value1AttributeTextField;
    private JTextField value2AttributeTextField;
    // End of variables declaration//GEN-END:variables
}
