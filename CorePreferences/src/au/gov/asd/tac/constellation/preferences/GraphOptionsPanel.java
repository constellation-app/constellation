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
package au.gov.asd.tac.constellation.preferences;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.LayoutStyle;
import org.apache.commons.lang3.StringUtils;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * UI panel to define the session parameters
 *
 * @author aldebaran30701
 */
final class GraphOptionsPanel extends javax.swing.JPanel {

    private final GraphOptionsPanelController controller;
    private final List<JPanel> colorPanels;
    final Preferences prefs = NbPreferences.forModule(GraphPreferenceKeys.class);
    private final static Color DEFAULT_COLOR = new Color(255, 255, 254);
        private static final String[] EYE_COLORS = new String[]{"Blue", "Cyan", "Green", "Magenta", "Red", "Yellow"};

    public GraphOptionsPanel(final GraphOptionsPanelController controller) {
        this.controller = controller;
        initComponents();

        colorPanels = new ArrayList<>();
        colorPanels.add(colorPanel1);
        colorPanels.add(colorPanel2);
        colorPanels.add(colorPanel3);
        colorPanels.add(colorPanel4);
        colorPanels.add(colorPanel5);
        colorPanels.add(colorPanel6);
        colorPanels.add(colorPanel7);
        colorPanels.add(colorPanel8);
        colorPanels.add(colorPanel9);
        colorPanels.add(colorPanel10);

        for (final JPanel colorPanel : colorPanels) {
            colorPanel.setBackground(DEFAULT_COLOR);

        }
    }

    public void setPresetColors(final List<Color> colors) {
        // loop throught items and add each to each panel
        int panelCounter = 0;
        for (final JPanel panel : colorPanels) {
            if (panelCounter < colors.size()) {
                panel.setBackground(colors.get(panelCounter) == null ? DEFAULT_COLOR : colors.get(panelCounter));
                panelCounter++;
            }
        }
    }

    public void setBlazeSize(final int value) {
        blazeSlider.setValue(value);
    }

    public void setBlazeOpacity(final int value) {
        blazeOpacitySlider.setValue(value);
    }

    public int getBlazeSize() {
        return blazeSlider.getValue();
    }

    public int getBlazeOpacity() {
        return blazeOpacitySlider.getValue();
    }

    /**
     * Get the HTML color from a java color
     *
     * @param color
     * @return
     */
    private String getHTMLColor(final Color color) {
        if (color == null) {
            return null;
        }

        final int r = color.getRed();
        final int g = color.getGreen();
        final int b = color.getBlue();
        return String.format("#%02x%02x%02x", r, g, b);
    }

    /**
     * Load a color picker dialog and save the picked color as the new custom
     * color.
     *
     * @param panelID the id of the colorPanel to grab the previous color from
     */
    private void loadColorPicker(final int panelID) {
        final Color newColor = JColorChooser.showDialog(null, "Choose a color", colorPanels.get(panelID - 1).getBackground());

        if (newColor != null) {
            colorPanels.get(panelID - 1).setBackground(newColor);
            final String colorString = prefs.get(GraphPreferenceKeys.BLAZE_PRESET_COLORS, GraphPreferenceKeys.BLAZE_PRESET_COLORS_DEFAULT);
            final String newColorString;
            if (StringUtils.isBlank(colorString)) {
                newColorString = getHTMLColor(newColor) + ";";
            } else {
                // build up a string based on entries of the colorpanels
                final StringBuilder colorStringBuilder = new StringBuilder();

                for (final JPanel panel : colorPanels) {
                    final String panelColor = getHTMLColor(panel.getBackground());
                    if (!StringUtils.equals(panelColor, getHTMLColor(DEFAULT_COLOR))) {
                        colorStringBuilder.append(panelColor);
                        colorStringBuilder.append(";");
                    } else {
                        colorStringBuilder.append("null");
                        colorStringBuilder.append(";");
                    }

                }
                newColorString = colorStringBuilder.toString();
            }
            prefs.put(GraphPreferenceKeys.BLAZE_PRESET_COLORS, newColorString);

        }
    }

    public String getLeftColor() {
        return (String)leftEyeColor.getSelectedItem();
    }

    public void setLeftColor(final String color) {
        leftEyeColor.setSelectedItem(color);
    }

    public String getRightColor() {
        return (String)rightEyeColor.getSelectedItem();
    }

    public void setRightColor(final String color) {
        rightEyeColor.setSelectedItem(color);
    }
    
    public boolean getAnimationsEnabled(){
        return this.enableAnimationsCheckBox.isSelected();
    }
    
    public void setAnimationsEnabled(final boolean selected){
        this.enableAnimationsCheckBox.setSelected(selected);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        blazeSizePanel = new JPanel();
        blazeSlider = new JSlider();
        blazeSizeDescription = new JLabel();
        blazeOpacitySlider = new JSlider();
        blazeOpacityDescription = new JLabel();
        blazeColorPanel = new JPanel();
        blazeColorDescription = new JLabel();
        colorPanel1 = new JPanel();
        colorPanel2 = new JPanel();
        colorPanel3 = new JPanel();
        colorPanel4 = new JPanel();
        colorPanel5 = new JPanel();
        colorPanel6 = new JPanel();
        colorPanel7 = new JPanel();
        colorPanel9 = new JPanel();
        colorPanel10 = new JPanel();
        colorPanel8 = new JPanel();
        blazeColours = new JLabel();
        warningLabel = new JLabel();
        anaglyphicDisplayPanel1 = new JPanel();
        anaglyphicOptionsPanel = new JPanel();
        leftEyeLabel = new JLabel();
        leftEyeColor = new JComboBox<>();
        rightEyeLabel = new JLabel();
        rightEyeColor = new JComboBox<>();
        changeColoursLabel = new JLabel();
        animaitonPanel = new JPanel();
        enableAnimationsCheckBox = new JCheckBox();

        blazeSizePanel.setBorder(BorderFactory.createTitledBorder(NbBundle.getMessage(GraphOptionsPanel.class, "GraphOptionsPanel.blazeSizePanel.border.title"))); // NOI18N

        Mnemonics.setLocalizedText(blazeSizeDescription, NbBundle.getMessage(GraphOptionsPanel.class, "GraphOptionsPanel.blazeSizeDescription.text")); // NOI18N

        Mnemonics.setLocalizedText(blazeOpacityDescription, NbBundle.getMessage(GraphOptionsPanel.class, "GraphOptionsPanel.blazeOpacityDescription.text")); // NOI18N

        GroupLayout blazeSizePanelLayout = new GroupLayout(blazeSizePanel);
        blazeSizePanel.setLayout(blazeSizePanelLayout);
        blazeSizePanelLayout.setHorizontalGroup(blazeSizePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(blazeSizePanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(blazeSizePanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(blazeOpacityDescription)
                    .addComponent(blazeSizeDescription, GroupLayout.Alignment.LEADING))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(blazeSizePanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                    .addComponent(blazeOpacitySlider, GroupLayout.PREFERRED_SIZE, 253, GroupLayout.PREFERRED_SIZE)
                    .addComponent(blazeSlider, GroupLayout.PREFERRED_SIZE, 253, GroupLayout.PREFERRED_SIZE))
                .addGap(39, 39, 39))
        );
        blazeSizePanelLayout.setVerticalGroup(blazeSizePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(blazeSizePanelLayout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(blazeSizePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(blazeSizeDescription)
                    .addComponent(blazeSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(blazeSizePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(blazeOpacitySlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(blazeOpacityDescription))
                .addContainerGap())
        );

        blazeColorPanel.setBorder(BorderFactory.createTitledBorder(NbBundle.getMessage(GraphOptionsPanel.class, "GraphOptionsPanel.blazeColorPanel.border.title"))); // NOI18N

        Mnemonics.setLocalizedText(blazeColorDescription, NbBundle.getMessage(GraphOptionsPanel.class, "GraphOptionsPanel.blazeColorDescription.text")); // NOI18N

        colorPanel1.setPreferredSize(new Dimension(25, 25));
        colorPanel1.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                colorPanel1MouseClicked(evt);
            }
        });

        GroupLayout colorPanel1Layout = new GroupLayout(colorPanel1);
        colorPanel1.setLayout(colorPanel1Layout);
        colorPanel1Layout.setHorizontalGroup(colorPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 25, Short.MAX_VALUE)
        );
        colorPanel1Layout.setVerticalGroup(colorPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 25, Short.MAX_VALUE)
        );

        colorPanel2.setPreferredSize(new Dimension(23, 0));
        colorPanel2.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                colorPanel2MouseClicked(evt);
            }
        });

        GroupLayout colorPanel2Layout = new GroupLayout(colorPanel2);
        colorPanel2.setLayout(colorPanel2Layout);
        colorPanel2Layout.setHorizontalGroup(colorPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 23, Short.MAX_VALUE)
        );
        colorPanel2Layout.setVerticalGroup(colorPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 25, Short.MAX_VALUE)
        );

        colorPanel3.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                colorPanel3MouseClicked(evt);
            }
        });

        GroupLayout colorPanel3Layout = new GroupLayout(colorPanel3);
        colorPanel3.setLayout(colorPanel3Layout);
        colorPanel3Layout.setHorizontalGroup(colorPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 25, Short.MAX_VALUE)
        );
        colorPanel3Layout.setVerticalGroup(colorPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        colorPanel4.setPreferredSize(new Dimension(25, 0));
        colorPanel4.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                colorPanel4MouseClicked(evt);
            }
        });

        GroupLayout colorPanel4Layout = new GroupLayout(colorPanel4);
        colorPanel4.setLayout(colorPanel4Layout);
        colorPanel4Layout.setHorizontalGroup(colorPanel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 25, Short.MAX_VALUE)
        );
        colorPanel4Layout.setVerticalGroup(colorPanel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        colorPanel5.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                colorPanel5MouseClicked(evt);
            }
        });

        GroupLayout colorPanel5Layout = new GroupLayout(colorPanel5);
        colorPanel5.setLayout(colorPanel5Layout);
        colorPanel5Layout.setHorizontalGroup(colorPanel5Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 25, Short.MAX_VALUE)
        );
        colorPanel5Layout.setVerticalGroup(colorPanel5Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        colorPanel6.setPreferredSize(new Dimension(25, 25));
        colorPanel6.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                colorPanel6MouseClicked(evt);
            }
        });

        GroupLayout colorPanel6Layout = new GroupLayout(colorPanel6);
        colorPanel6.setLayout(colorPanel6Layout);
        colorPanel6Layout.setHorizontalGroup(colorPanel6Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 25, Short.MAX_VALUE)
        );
        colorPanel6Layout.setVerticalGroup(colorPanel6Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        colorPanel7.setPreferredSize(new Dimension(25, 25));
        colorPanel7.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                colorPanel7MouseClicked(evt);
            }
        });

        GroupLayout colorPanel7Layout = new GroupLayout(colorPanel7);
        colorPanel7.setLayout(colorPanel7Layout);
        colorPanel7Layout.setHorizontalGroup(colorPanel7Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 25, Short.MAX_VALUE)
        );
        colorPanel7Layout.setVerticalGroup(colorPanel7Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        colorPanel9.setPreferredSize(new Dimension(25, 25));
        colorPanel9.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                colorPanel9MouseClicked(evt);
            }
        });

        GroupLayout colorPanel9Layout = new GroupLayout(colorPanel9);
        colorPanel9.setLayout(colorPanel9Layout);
        colorPanel9Layout.setHorizontalGroup(colorPanel9Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 25, Short.MAX_VALUE)
        );
        colorPanel9Layout.setVerticalGroup(colorPanel9Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        colorPanel10.setPreferredSize(new Dimension(25, 25));
        colorPanel10.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                colorPanel10MouseClicked(evt);
            }
        });

        GroupLayout colorPanel10Layout = new GroupLayout(colorPanel10);
        colorPanel10.setLayout(colorPanel10Layout);
        colorPanel10Layout.setHorizontalGroup(colorPanel10Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 25, Short.MAX_VALUE)
        );
        colorPanel10Layout.setVerticalGroup(colorPanel10Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        colorPanel8.setPreferredSize(new Dimension(25, 25));
        colorPanel8.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                colorPanel8MouseClicked(evt);
            }
        });

        GroupLayout colorPanel8Layout = new GroupLayout(colorPanel8);
        colorPanel8.setLayout(colorPanel8Layout);
        colorPanel8Layout.setHorizontalGroup(colorPanel8Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 25, Short.MAX_VALUE)
        );
        colorPanel8Layout.setVerticalGroup(colorPanel8Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 25, Short.MAX_VALUE)
        );

        Mnemonics.setLocalizedText(blazeColours, NbBundle.getMessage(GraphOptionsPanel.class, "GraphOptionsPanel.blazeColours.text")); // NOI18N

        GroupLayout blazeColorPanelLayout = new GroupLayout(blazeColorPanel);
        blazeColorPanel.setLayout(blazeColorPanelLayout);
        blazeColorPanelLayout.setHorizontalGroup(blazeColorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(blazeColorPanelLayout.createSequentialGroup()
                .addGroup(blazeColorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(blazeColorPanelLayout.createSequentialGroup()
                        .addGap(102, 102, 102)
                        .addComponent(blazeColorDescription))
                    .addGroup(blazeColorPanelLayout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addComponent(blazeColours, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(colorPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(colorPanel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(colorPanel3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(colorPanel4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(colorPanel5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(colorPanel6, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(colorPanel7, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(colorPanel8, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(colorPanel9, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(colorPanel10, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(42, 42, 42))
        );
        blazeColorPanelLayout.setVerticalGroup(blazeColorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(blazeColorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(blazeColorPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(colorPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addGroup(blazeColorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(colorPanel8, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGroup(blazeColorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                            .addComponent(colorPanel10, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(colorPanel9, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(colorPanel7, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(blazeColorPanelLayout.createSequentialGroup()
                                .addComponent(blazeColorDescription)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(blazeColours))
                            .addComponent(colorPanel6, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(colorPanel5, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(colorPanel4, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                            .addComponent(colorPanel3, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(colorPanel2, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        warningLabel.setFont(new Font("Tahoma", 1, 11)); // NOI18N
        warningLabel.setForeground(new Color(102, 102, 102));
        warningLabel.setIcon(new ImageIcon(getClass().getResource("/au/gov/asd/tac/constellation/preferences/resources/warning.png"))); // NOI18N
        Mnemonics.setLocalizedText(warningLabel, NbBundle.getMessage(GraphOptionsPanel.class, "GraphOptionsPanel.warningLabel.text")); // NOI18N

        anaglyphicDisplayPanel1.setBorder(BorderFactory.createTitledBorder(NbBundle.getMessage(GraphOptionsPanel.class, "GraphOptionsPanel.anaglyphicDisplayPanel1.border.title"))); // NOI18N

        Mnemonics.setLocalizedText(leftEyeLabel, NbBundle.getMessage(GraphOptionsPanel.class, "GraphOptionsPanel.leftEyeLabel.text")); // NOI18N

        leftEyeColor.setModel(new DefaultComboBoxModel<String>(EYE_COLORS));
        leftEyeColor.setSelectedItem(GraphPreferenceKeys.LEFT_COLOR_DEFAULT);

        Mnemonics.setLocalizedText(rightEyeLabel, NbBundle.getMessage(GraphOptionsPanel.class, "GraphOptionsPanel.rightEyeLabel.text")); // NOI18N

        rightEyeColor.setModel(new DefaultComboBoxModel<String>(EYE_COLORS));
        rightEyeColor.setSelectedItem(GraphPreferenceKeys.RIGHT_COLOR_DEFAULT);

        Mnemonics.setLocalizedText(changeColoursLabel, NbBundle.getMessage(GraphOptionsPanel.class, "GraphOptionsPanel.changeColoursLabel.text")); // NOI18N

        GroupLayout anaglyphicOptionsPanelLayout = new GroupLayout(anaglyphicOptionsPanel);
        anaglyphicOptionsPanel.setLayout(anaglyphicOptionsPanelLayout);
        anaglyphicOptionsPanelLayout.setHorizontalGroup(anaglyphicOptionsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(anaglyphicOptionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(anaglyphicOptionsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(changeColoursLabel, GroupLayout.PREFERRED_SIZE, 319, GroupLayout.PREFERRED_SIZE)
                    .addGroup(anaglyphicOptionsPanelLayout.createSequentialGroup()
                        .addGroup(anaglyphicOptionsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(leftEyeLabel)
                            .addComponent(leftEyeColor, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGap(30, 30, 30)
                        .addGroup(anaglyphicOptionsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(rightEyeColor, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(rightEyeLabel))))
                .addContainerGap(262, Short.MAX_VALUE))
        );
        anaglyphicOptionsPanelLayout.setVerticalGroup(anaglyphicOptionsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(anaglyphicOptionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(anaglyphicOptionsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(leftEyeLabel)
                    .addComponent(rightEyeLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(anaglyphicOptionsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(leftEyeColor, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(rightEyeColor, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 27, Short.MAX_VALUE)
                .addComponent(changeColoursLabel)
                .addContainerGap())
        );

        GroupLayout anaglyphicDisplayPanel1Layout = new GroupLayout(anaglyphicDisplayPanel1);
        anaglyphicDisplayPanel1.setLayout(anaglyphicDisplayPanel1Layout);
        anaglyphicDisplayPanel1Layout.setHorizontalGroup(anaglyphicDisplayPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(anaglyphicDisplayPanel1Layout.createSequentialGroup()
                .addComponent(anaglyphicOptionsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(0, 49, Short.MAX_VALUE))
        );
        anaglyphicDisplayPanel1Layout.setVerticalGroup(anaglyphicDisplayPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(anaglyphicDisplayPanel1Layout.createSequentialGroup()
                .addComponent(anaglyphicOptionsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(0, 12, Short.MAX_VALUE))
        );

        animaitonPanel.setBorder(BorderFactory.createTitledBorder(NbBundle.getMessage(GraphOptionsPanel.class, "GraphOptionsPanel.animaitonPanel.border.title"))); // NOI18N

        enableAnimationsCheckBox.setSelected(true);
        Mnemonics.setLocalizedText(enableAnimationsCheckBox, NbBundle.getMessage(GraphOptionsPanel.class, "GraphOptionsPanel.enableAnimationsCheckBox.text")); // NOI18N

        GroupLayout animaitonPanelLayout = new GroupLayout(animaitonPanel);
        animaitonPanel.setLayout(animaitonPanelLayout);
        animaitonPanelLayout.setHorizontalGroup(animaitonPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(animaitonPanelLayout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(enableAnimationsCheckBox)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        animaitonPanelLayout.setVerticalGroup(animaitonPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(animaitonPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(enableAnimationsCheckBox)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(blazeSizePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(blazeColorPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(warningLabel))
                    .addComponent(anaglyphicDisplayPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(animaitonPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(warningLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(blazeSizePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(blazeColorPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(anaglyphicDisplayPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(animaitonPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(93, Short.MAX_VALUE))
        );

        blazeSizePanel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(GraphOptionsPanel.class, "GraphOptionsPanel.blazeSizePanel.AccessibleContext.accessibleName")); // NOI18N
        animaitonPanel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(GraphOptionsPanel.class, "GraphOptionsPanel.animaitonPanel.AccessibleContext.accessibleName")); // NOI18N
        animaitonPanel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GraphOptionsPanel.class, "GraphOptionsPanel.animaitonPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void colorPanel8MouseClicked(MouseEvent evt) {//GEN-FIRST:event_colorPanel8MouseClicked
        loadColorPicker(8);
    }//GEN-LAST:event_colorPanel8MouseClicked

    private void colorPanel10MouseClicked(MouseEvent evt) {//GEN-FIRST:event_colorPanel10MouseClicked
        loadColorPicker(10);
    }//GEN-LAST:event_colorPanel10MouseClicked

    private void colorPanel9MouseClicked(MouseEvent evt) {//GEN-FIRST:event_colorPanel9MouseClicked
        loadColorPicker(9);
    }//GEN-LAST:event_colorPanel9MouseClicked

    private void colorPanel7MouseClicked(MouseEvent evt) {//GEN-FIRST:event_colorPanel7MouseClicked
        loadColorPicker(7);
    }//GEN-LAST:event_colorPanel7MouseClicked

    private void colorPanel6MouseClicked(MouseEvent evt) {//GEN-FIRST:event_colorPanel6MouseClicked
        loadColorPicker(6);
    }//GEN-LAST:event_colorPanel6MouseClicked

    private void colorPanel5MouseClicked(MouseEvent evt) {//GEN-FIRST:event_colorPanel5MouseClicked
        loadColorPicker(5);
    }//GEN-LAST:event_colorPanel5MouseClicked

    private void colorPanel4MouseClicked(MouseEvent evt) {//GEN-FIRST:event_colorPanel4MouseClicked
        loadColorPicker(4);
    }//GEN-LAST:event_colorPanel4MouseClicked

    private void colorPanel3MouseClicked(MouseEvent evt) {//GEN-FIRST:event_colorPanel3MouseClicked
        loadColorPicker(3);
    }//GEN-LAST:event_colorPanel3MouseClicked

    private void colorPanel2MouseClicked(MouseEvent evt) {//GEN-FIRST:event_colorPanel2MouseClicked
        loadColorPicker(2);
    }//GEN-LAST:event_colorPanel2MouseClicked

    private void colorPanel1MouseClicked(MouseEvent evt) {//GEN-FIRST:event_colorPanel1MouseClicked
        loadColorPicker(1);
    }//GEN-LAST:event_colorPanel1MouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel anaglyphicDisplayPanel1;
    private JPanel anaglyphicOptionsPanel;
    private JPanel animaitonPanel;
    private JLabel blazeColorDescription;
    private JPanel blazeColorPanel;
    private JLabel blazeColours;
    private JLabel blazeOpacityDescription;
    private JSlider blazeOpacitySlider;
    private JLabel blazeSizeDescription;
    private JPanel blazeSizePanel;
    private JSlider blazeSlider;
    private JLabel changeColoursLabel;
    private JPanel colorPanel1;
    private JPanel colorPanel10;
    private JPanel colorPanel2;
    private JPanel colorPanel3;
    private JPanel colorPanel4;
    private JPanel colorPanel5;
    private JPanel colorPanel6;
    private JPanel colorPanel7;
    private JPanel colorPanel8;
    private JPanel colorPanel9;
    private JCheckBox enableAnimationsCheckBox;
    private JComboBox<String> leftEyeColor;
    private JLabel leftEyeLabel;
    private JComboBox<String> rightEyeColor;
    private JLabel rightEyeLabel;
    private JLabel warningLabel;
    // End of variables declaration//GEN-END:variables
}
