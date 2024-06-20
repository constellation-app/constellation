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
package au.gov.asd.tac.constellation.preferences;

import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SpinnerNumberModel;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 * UI panel to define the session parameters
 *
 * @author algol
 */
final class ApplicationOptionsPanel extends JPanel {

    private final String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

    private static final String USER_HOME_PROPERTY = "user.home";

    public ApplicationOptionsPanel() {
        initComponents();
    }

    public String getUserDirectory() {
        return userDirectoryText.getText();
    }

    public void setUserDirectory(final String userDirectory) {
        userDirectoryText.setText(userDirectory);
    }

    public boolean isAutosaveEnabled() {
        return autosaveCheckBox.isSelected();
    }

    public void setAutosaveEnabled(final boolean autosaveEnabled) {
        autosaveCheckBox.setSelected(autosaveEnabled);
        autosaveSpinner.setEnabled(autosaveEnabled);
    }

    public int getAutosaveFrequency() {
        return (Integer) autosaveSpinner.getModel().getValue();
    }

    public void setAutosaveFrequency(final int autosaveFrequency) {
        autosaveSpinner.getModel().setValue(autosaveFrequency);
    }

    public boolean isWelcomeOnStartupSelected() {
        return startupWelcomeCheckbox.isSelected();
    }

    public void setWelcomeOnStartup(final boolean welcomeOnStartup) {
        startupWelcomeCheckbox.setSelected(welcomeOnStartup);
    }

    public boolean isWhatsNewOnStartupSelected() {
        return startupWhatsNewCheckbox.isSelected();
    }

    public void setWhatsNewOnStartup(final boolean whatsNewOnStartup) {
        startupWhatsNewCheckbox.setSelected(whatsNewOnStartup);
    }

    public int getWebserverPort() {
        return (Integer) webserverPortSpinner.getModel().getValue();
    }

    public void setWebserverPort(final int webserverPort) {
        webserverPortSpinner.getModel().setValue(webserverPort);
    }

    public String getNotebookDirectory() {
        return notebookDirectoryText.getText();
    }

    public void setNotebookDirectory(final String notebookDirectory) {
        notebookDirectoryText.setText(notebookDirectory);
    }

    public String getRestDirectory() {
        return restDirectoryText.getText();
    }

    public void setRestDirectory(final String restDirectory) {
        restDirectoryText.setText(restDirectory);
    }

    public boolean isDownloadPythonClientSelected() {
        return downloadPythonClientCheckBox.isSelected();
    }

    public void setDownloadPythonClient(final boolean downloadPythonClient) {
        downloadPythonClientCheckBox.setSelected(downloadPythonClient);
    }

    public String getCurrentFont() {
        return fontCombo.getSelectedItem().toString();
    }

    public void setCurrentFont(final String currentFont) {
        fontCombo.setSelectedItem(currentFont);
    }

    public String getFontSize() {
        return fontSizeSpinner.getValue().toString();
    }

    public void setFontSize(final String fontSize) {
        fontSizeSpinner.setValue(Integer.valueOf(fontSize));
    }

    public String[] getFontList() {
        return fonts.clone();
    }

    public String getColorModeSelection() {
        return colorblindDropdown.getSelectedItem().toString();
    }

    public void setColorModeSelection(final String currentColorMode) {
        colorblindDropdown.setSelectedItem(currentColorMode);
    }
    
    public boolean isEnableSpellCheckingSelected() {
        return enableSpellCheckingCheckBox.isSelected();
    }

    public void setEnableSpellChecking(final boolean enableSpellChecking) {
        this.enableSpellCheckingCheckBox.setSelected(enableSpellChecking);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        userDirectoryLabel = new JLabel();
        userDirectoryText = new JTextField();
        userDirectoryButton = new JButton();
        autosavePanel = new JPanel();
        autosaveCheckBox = new JCheckBox();
        autosaveSpinner = new JSpinner();
        autosaveLabel = new JLabel();
        startupPanel = new JPanel();
        startupWelcomeCheckbox = new JCheckBox();
        startupWhatsNewCheckbox = new JCheckBox();
        webserverPanel = new JPanel();
        webserverPortLabel = new JLabel();
        webserverPortSpinner = new JSpinner();
        restDirectoryLabel = new JLabel();
        restDirectoryText = new JTextField();
        restDirectoryButton = new JButton();
        notebookPanel = new JPanel();
        notebookDirectoryLabel = new JLabel();
        notebookDirectoryText = new JTextField();
        notebookDirectoryButton = new JButton();
        downloadPythonClientCheckBox = new JCheckBox();
        fontPanel = new JPanel();
        fontLbl = new JLabel();
        fontSizeLbl = new JLabel();
        fontCombo = new JComboBox<>();
        fontSizeSpinner = new JSpinner();
        resetBtn = new JButton();
        colorblindPanel = new JPanel();
        colorblindDropdown = new JComboBox<>();
        colorblindLabel = new JLabel();
        restartLabel = new JLabel();
        spellcheckPanel = new JPanel();
        enableSpellCheckingCheckBox = new JCheckBox();
        leftClickReminderLabel = new JLabel();

        Mnemonics.setLocalizedText(userDirectoryLabel, NbBundle.getMessage(ApplicationOptionsPanel.class, "ApplicationOptionsPanel.userDirectoryLabel.text")); // NOI18N

        userDirectoryText.setText(NbBundle.getMessage(ApplicationOptionsPanel.class, "ApplicationOptionsPanel.userDirectoryText.text")); // NOI18N

        Mnemonics.setLocalizedText(userDirectoryButton, NbBundle.getMessage(ApplicationOptionsPanel.class, "ApplicationOptionsPanel.userDirectoryButton.text")); // NOI18N
        userDirectoryButton.setMargin(new Insets(2, 0, 2, 0));
        userDirectoryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                userDirectoryButtonActionPerformed(evt);
            }
        });

        autosavePanel.setBorder(BorderFactory.createTitledBorder(NbBundle.getMessage(ApplicationOptionsPanel.class, "ApplicationOptionsPanel.autosavePanel.border.title"))); // NOI18N

        Mnemonics.setLocalizedText(autosaveCheckBox, NbBundle.getMessage(ApplicationOptionsPanel.class, "ApplicationOptionsPanel.autosaveCheckBox.text")); // NOI18N
        autosaveCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                autosaveCheckBoxActionPerformed(evt);
            }
        });

        autosaveSpinner.setModel(new SpinnerNumberModel(10, 5, null, 1));

        Mnemonics.setLocalizedText(autosaveLabel, NbBundle.getMessage(ApplicationOptionsPanel.class, "ApplicationOptionsPanel.autosaveLabel.text")); // NOI18N

        GroupLayout autosavePanelLayout = new GroupLayout(autosavePanel);
        autosavePanel.setLayout(autosavePanelLayout);
        autosavePanelLayout.setHorizontalGroup(autosavePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(autosavePanelLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(autosaveCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(autosaveSpinner, GroupLayout.PREFERRED_SIZE, 55, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(autosaveLabel, GroupLayout.PREFERRED_SIZE, 230, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(202, Short.MAX_VALUE))
        );
        autosavePanelLayout.setVerticalGroup(autosavePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(autosavePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(autosavePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(autosaveCheckBox)
                    .addComponent(autosaveSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(autosaveLabel))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        startupPanel.setBorder(BorderFactory.createTitledBorder(NbBundle.getMessage(ApplicationOptionsPanel.class, "ApplicationOptionsPanel.startupPanel.border.title"))); // NOI18N

        Mnemonics.setLocalizedText(startupWelcomeCheckbox, NbBundle.getMessage(ApplicationOptionsPanel.class, "ApplicationOptionsPanel.startupWelcomeCheckbox.text")); // NOI18N
        startupWelcomeCheckbox.setActionCommand(NbBundle.getMessage(ApplicationOptionsPanel.class, "ApplicationOptionsPanel.startupWelcomeCheckbox.actionCommand")); // NOI18N

        Mnemonics.setLocalizedText(startupWhatsNewCheckbox, NbBundle.getMessage(ApplicationOptionsPanel.class, "ApplicationOptionsPanel.startupWhatsNewCheckbox.text")); // NOI18N
        startupWhatsNewCheckbox.setActionCommand(NbBundle.getMessage(ApplicationOptionsPanel.class, "ApplicationOptionsPanel.startupWhatsNewCheckbox.actionCommand")); // NOI18N

        GroupLayout startupPanelLayout = new GroupLayout(startupPanel);
        startupPanel.setLayout(startupPanelLayout);
        startupPanelLayout.setHorizontalGroup(startupPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(startupPanelLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(startupPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(startupWhatsNewCheckbox)
                    .addComponent(startupWelcomeCheckbox))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        startupPanelLayout.setVerticalGroup(startupPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(startupPanelLayout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(startupWelcomeCheckbox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(startupWhatsNewCheckbox)
                .addGap(12, 12, 12))
        );

        webserverPanel.setBorder(BorderFactory.createTitledBorder(NbBundle.getMessage(ApplicationOptionsPanel.class, "ApplicationOptionsPanel.webserverPanel.border.title"))); // NOI18N

        Mnemonics.setLocalizedText(webserverPortLabel, NbBundle.getMessage(ApplicationOptionsPanel.class, "ApplicationOptionsPanel.webserverPortLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(restDirectoryLabel, NbBundle.getMessage(ApplicationOptionsPanel.class, "ApplicationOptionsPanel.restDirectoryLabel.text")); // NOI18N

        restDirectoryText.setText(NbBundle.getMessage(ApplicationOptionsPanel.class, "ApplicationOptionsPanel.restDirectoryText.text")); // NOI18N

        Mnemonics.setLocalizedText(restDirectoryButton, NbBundle.getMessage(ApplicationOptionsPanel.class, "ApplicationOptionsPanel.restDirectoryButton.text")); // NOI18N
        restDirectoryButton.setMargin(new Insets(2, 0, 2, 0));
        restDirectoryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                restDirectoryButtonActionPerformed(evt);
            }
        });

        GroupLayout webserverPanelLayout = new GroupLayout(webserverPanel);
        webserverPanel.setLayout(webserverPanelLayout);
        webserverPanelLayout.setHorizontalGroup(webserverPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(webserverPanelLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(webserverPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(restDirectoryLabel)
                    .addComponent(webserverPortLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(webserverPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(webserverPanelLayout.createSequentialGroup()
                        .addComponent(webserverPortSpinner, GroupLayout.PREFERRED_SIZE, 85, GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(restDirectoryText))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(restDirectoryButton)
                .addGap(4, 4, 4))
        );
        webserverPanelLayout.setVerticalGroup(webserverPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(webserverPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(webserverPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(webserverPortLabel)
                    .addComponent(webserverPortSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(webserverPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(restDirectoryLabel)
                    .addComponent(restDirectoryText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(restDirectoryButton))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        notebookPanel.setBorder(BorderFactory.createTitledBorder(NbBundle.getMessage(ApplicationOptionsPanel.class, "ApplicationOptionsPanel.notebookPanel.border.title_1"))); // NOI18N

        Mnemonics.setLocalizedText(notebookDirectoryLabel, NbBundle.getMessage(ApplicationOptionsPanel.class, "ApplicationOptionsPanel.notebookDirectoryLabel.text")); // NOI18N

        notebookDirectoryText.setText(NbBundle.getMessage(ApplicationOptionsPanel.class, "ApplicationOptionsPanel.notebookDirectoryText.text")); // NOI18N

        Mnemonics.setLocalizedText(notebookDirectoryButton, NbBundle.getMessage(ApplicationOptionsPanel.class, "ApplicationOptionsPanel.notebookDirectoryButton.text")); // NOI18N
        notebookDirectoryButton.setMargin(new Insets(2, 0, 2, 0));
        notebookDirectoryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                notebookDirectoryButtonActionPerformed(evt);
            }
        });

        downloadPythonClientCheckBox.setSelected(true);
        Mnemonics.setLocalizedText(downloadPythonClientCheckBox, NbBundle.getMessage(ApplicationOptionsPanel.class, "ApplicationOptionsPanel.downloadPythonClientCheckBox.text")); // NOI18N

        GroupLayout notebookPanelLayout = new GroupLayout(notebookPanel);
        notebookPanel.setLayout(notebookPanelLayout);
        notebookPanelLayout.setHorizontalGroup(notebookPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(notebookPanelLayout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(notebookPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(notebookPanelLayout.createSequentialGroup()
                        .addComponent(downloadPythonClientCheckBox)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(notebookPanelLayout.createSequentialGroup()
                        .addComponent(notebookDirectoryLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(notebookDirectoryText)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(notebookDirectoryButton)))
                .addContainerGap())
        );
        notebookPanelLayout.setVerticalGroup(notebookPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(notebookPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(notebookPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(notebookDirectoryLabel)
                    .addComponent(notebookDirectoryText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(notebookDirectoryButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(downloadPythonClientCheckBox)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        fontPanel.setBorder(BorderFactory.createTitledBorder(NbBundle.getMessage(ApplicationOptionsPanel.class, "ApplicationOptionsPanel.fontPanel.border.title"))); // NOI18N

        Mnemonics.setLocalizedText(fontLbl, NbBundle.getMessage(ApplicationOptionsPanel.class, "ApplicationOptionsPanel.fontLbl.text")); // NOI18N

        Mnemonics.setLocalizedText(fontSizeLbl, NbBundle.getMessage(ApplicationOptionsPanel.class, "ApplicationOptionsPanel.fontSizeLbl.text")); // NOI18N

        fontCombo.setModel(new DefaultComboBoxModel(fonts));
        fontCombo.setSelectedItem(ApplicationPreferenceKeys.FONT_FAMILY_DEFAULT);
        fontCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                fontComboActionPerformed(evt);
            }
        });

        fontSizeSpinner.setModel(new SpinnerNumberModel(12, 8, 45, 1));

        Mnemonics.setLocalizedText(resetBtn, NbBundle.getMessage(ApplicationOptionsPanel.class, "ApplicationOptionsPanel.resetBtn.text")); // NOI18N
        resetBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                resetBtnActionPerformed(evt);
            }
        });

        GroupLayout fontPanelLayout = new GroupLayout(fontPanel);
        fontPanel.setLayout(fontPanelLayout);
        fontPanelLayout.setHorizontalGroup(fontPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(fontPanelLayout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(fontPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(fontLbl)
                    .addComponent(fontSizeLbl))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(fontPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(fontSizeSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(fontCombo, GroupLayout.PREFERRED_SIZE, 240, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(resetBtn))
        );
        fontPanelLayout.setVerticalGroup(fontPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(fontPanelLayout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(fontPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(fontLbl)
                    .addComponent(fontCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(fontPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(fontPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(fontSizeSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(fontSizeLbl))
                    .addComponent(resetBtn))
                .addGap(26, 26, 26))
        );

        colorblindPanel.setBorder(BorderFactory.createTitledBorder(NbBundle.getMessage(ApplicationOptionsPanel.class, "ApplicationOptionsPanel.colorblindPanel.border.title"))); // NOI18N
        colorblindPanel.setName(""); // NOI18N

        colorblindDropdown.setModel(new DefaultComboBoxModel<>(new String[] { "None", "Deuteranopia", "Protanopia", "Tritanopia" }));

        Mnemonics.setLocalizedText(colorblindLabel, NbBundle.getMessage(ApplicationOptionsPanel.class, "ApplicationOptionsPanel.colorblindLabel.text")); // NOI18N

        restartLabel.setIcon(new ImageIcon(getClass().getResource("/au/gov/asd/tac/constellation/preferences/resources/warning.png"))); // NOI18N
        Mnemonics.setLocalizedText(restartLabel, NbBundle.getMessage(ApplicationOptionsPanel.class, "ApplicationOptionsPanel.restartLabel.text")); // NOI18N

        GroupLayout colorblindPanelLayout = new GroupLayout(colorblindPanel);
        colorblindPanel.setLayout(colorblindPanelLayout);
        colorblindPanelLayout.setHorizontalGroup(colorblindPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(colorblindPanelLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(colorblindLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(colorblindPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(restartLabel)
                    .addComponent(colorblindDropdown, GroupLayout.PREFERRED_SIZE, 172, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        colorblindPanelLayout.setVerticalGroup(colorblindPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(colorblindPanelLayout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addGroup(colorblindPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(colorblindLabel)
                    .addComponent(colorblindDropdown, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(restartLabel)
                .addContainerGap(18, Short.MAX_VALUE))
        );

        spellcheckPanel.setBorder(BorderFactory.createTitledBorder(NbBundle.getMessage(ApplicationOptionsPanel.class, "ApplicationOptionsPanel.spellcheckPanel.border.title"))); // NOI18N
        spellcheckPanel.setName(""); // NOI18N

        Mnemonics.setLocalizedText(enableSpellCheckingCheckBox, NbBundle.getMessage(ApplicationOptionsPanel.class, "ApplicationOptionsPanel.enableSpellCheckingCheckBox.text")); // NOI18N

        leftClickReminderLabel.setIcon(new ImageIcon(getClass().getResource("/au/gov/asd/tac/constellation/preferences/resources/warning.png"))); // NOI18N
        Mnemonics.setLocalizedText(leftClickReminderLabel, NbBundle.getMessage(ApplicationOptionsPanel.class, "ApplicationOptionsPanel.leftClickReminderLabel.text")); // NOI18N

        GroupLayout spellcheckPanelLayout = new GroupLayout(spellcheckPanel);
        spellcheckPanel.setLayout(spellcheckPanelLayout);
        spellcheckPanelLayout.setHorizontalGroup(spellcheckPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(spellcheckPanelLayout.createSequentialGroup()
                .addGroup(spellcheckPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(spellcheckPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(enableSpellCheckingCheckBox))
                    .addGroup(spellcheckPanelLayout.createSequentialGroup()
                        .addGap(54, 54, 54)
                        .addComponent(leftClickReminderLabel)))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        spellcheckPanelLayout.setVerticalGroup(spellcheckPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(spellcheckPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(enableSpellCheckingCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(leftClickReminderLabel)
                .addContainerGap(10, Short.MAX_VALUE))
        );

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(startupPanel, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(autosavePanel, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(userDirectoryLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(userDirectoryText)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(userDirectoryButton))
                    .addComponent(webserverPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(notebookPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(fontPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(colorblindPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(spellcheckPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(userDirectoryLabel)
                    .addComponent(userDirectoryButton)
                    .addComponent(userDirectoryText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(autosavePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(startupPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(webserverPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(notebookPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fontPanel, GroupLayout.PREFERRED_SIZE, 95, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(colorblindPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spellcheckPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        notebookPanel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ApplicationOptionsPanel.class, "ApplicationOptionsPanel.notebookPanel.AccessibleContext.accessibleName")); // NOI18N
        spellcheckPanel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ApplicationOptionsPanel.class, "ApplicationOptionsPanel.spellcheckPanel.AccessibleContext.accessibleName")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void userDirectoryButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_userDirectoryButtonActionPerformed
    {//GEN-HEADEREND:event_userDirectoryButtonActionPerformed
        final JFileChooser fc = new JFileChooser(System.getProperty(USER_HOME_PROPERTY));
        final String dir = userDirectoryText.getText().trim();
        if (!dir.isEmpty()) {
            fc.setSelectedFile(new File(dir));
        }
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fc.showDialog(this, "Select CONSTELLATION directory") == JFileChooser.APPROVE_OPTION) {
            final String fnam = fc.getSelectedFile().getPath();
            userDirectoryText.setText(fnam);
        }
    }//GEN-LAST:event_userDirectoryButtonActionPerformed

    private void autosaveCheckBoxActionPerformed(ActionEvent evt)//GEN-FIRST:event_autosaveCheckBoxActionPerformed
    {//GEN-HEADEREND:event_autosaveCheckBoxActionPerformed
        autosaveSpinner.setEnabled(autosaveCheckBox.isSelected());
    }//GEN-LAST:event_autosaveCheckBoxActionPerformed

    private void notebookDirectoryButtonActionPerformed(ActionEvent evt)//GEN-FIRST:event_notebookDirectoryButtonActionPerformed
    {//GEN-HEADEREND:event_notebookDirectoryButtonActionPerformed
        final JFileChooser fc = new JFileChooser(System.getProperty(USER_HOME_PROPERTY));
        final String dir = notebookDirectoryText.getText().trim();
        if (!dir.isEmpty()) {
            fc.setSelectedFile(new File(dir));
        }
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fc.showDialog(this, "Select Jupyter notebook directory") == JFileChooser.APPROVE_OPTION) {
            final String fnam = fc.getSelectedFile().getPath();
            notebookDirectoryText.setText(fnam);
        }
    }//GEN-LAST:event_notebookDirectoryButtonActionPerformed

    private void restDirectoryButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_restDirectoryButtonActionPerformed
        final JFileChooser fc = new JFileChooser(System.getProperty(USER_HOME_PROPERTY));
        final String dir = restDirectoryText.getText().trim();
        if (!dir.isEmpty()) {
            fc.setSelectedFile(new File(dir));
        }
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fc.showDialog(this, "Select REST directory") == JFileChooser.APPROVE_OPTION) {
            final String fnam = fc.getSelectedFile().getPath();
            restDirectoryText.setText(fnam);
        }
    }//GEN-LAST:event_restDirectoryButtonActionPerformed

    private void fontComboActionPerformed(ActionEvent evt) {//GEN-FIRST:event_fontComboActionPerformed
        setFontSize(fontSizeSpinner.getValue().toString());
    }//GEN-LAST:event_fontComboActionPerformed

    private void resetBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_resetBtnActionPerformed
        fontCombo.setSelectedItem("Arial");
        fontSizeSpinner.setValue(12);
    }//GEN-LAST:event_resetBtnActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JCheckBox autosaveCheckBox;
    private JLabel autosaveLabel;
    private JPanel autosavePanel;
    private JSpinner autosaveSpinner;
    private JComboBox<String> colorblindDropdown;
    private JLabel colorblindLabel;
    private JPanel colorblindPanel;
    private JCheckBox downloadPythonClientCheckBox;
    private JCheckBox enableSpellCheckingCheckBox;
    private JComboBox<String> fontCombo;
    private JLabel fontLbl;
    private JPanel fontPanel;
    private JLabel fontSizeLbl;
    private JSpinner fontSizeSpinner;
    private JLabel leftClickReminderLabel;
    private JButton notebookDirectoryButton;
    private JLabel notebookDirectoryLabel;
    private JTextField notebookDirectoryText;
    private JPanel notebookPanel;
    private JButton resetBtn;
    private JButton restDirectoryButton;
    private JLabel restDirectoryLabel;
    private JTextField restDirectoryText;
    private JLabel restartLabel;
    private JPanel spellcheckPanel;
    private JPanel startupPanel;
    private JCheckBox startupWelcomeCheckbox;
    private JCheckBox startupWhatsNewCheckbox;
    private JButton userDirectoryButton;
    private JLabel userDirectoryLabel;
    private JTextField userDirectoryText;
    private JPanel webserverPanel;
    private JLabel webserverPortLabel;
    private JSpinner webserverPortSpinner;
    // End of variables declaration//GEN-END:variables
}
