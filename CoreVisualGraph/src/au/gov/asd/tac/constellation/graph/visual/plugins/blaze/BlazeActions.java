/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.visual.plugins.blaze;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.manager.GraphManagerListener;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.utilities.BlazeUtilities;
import au.gov.asd.tac.constellation.graph.visual.VisualGraphPluginRegistry;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginRegistry;
import au.gov.asd.tac.constellation.plugins.parameters.DefaultPluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.preferences.GraphPreferenceKeys;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;
import javafx.util.Pair;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.actions.Presenter;

/*
 * toolbar options containing blaze controls
 */
@ActionID(category = "Selection", id = "au.gov.asd.tac.constellation.functionality.blaze.BlazeActions")
@ActionRegistration(displayName = "", lazy = false)
@ActionReference(path = "Toolbars/Display", position = 400)
@Messages("CTL_BlazeAction=Blaze")
public final class BlazeActions extends AbstractAction implements Presenter.Toolbar, GraphManagerListener {

    @StaticResource
    private static final String BLAZE_ACTIONS_ICON = "au/gov/asd/tac/constellation/graph/visual/plugins/blaze/resources/blaze_actions.png";
    @StaticResource
    private static final String SELECT_BLAZES_ICON = "au/gov/asd/tac/constellation/graph/visual/plugins/blaze/resources/selectblazes.png";
    @StaticResource
    private static final String DESELECT_BLAZES_ICON = "au/gov/asd/tac/constellation/graph/visual/plugins/blaze/resources/blaze.png";
    @StaticResource
    private static final String ADD_CUSTOM_BLAZE_ICON = "au/gov/asd/tac/constellation/graph/visual/plugins/blaze/resources/addblaze_custom.png";
    @StaticResource
    private static final String ADD_RECENT_BLAZE_ICON = "au/gov/asd/tac/constellation/graph/visual/plugins/blaze/resources/addblaze_recent.png";
    @StaticResource
    private static final String REMOVE_BLAZE_ICON = "au/gov/asd/tac/constellation/graph/visual/plugins/blaze/resources/removeblaze.png";

    private static final int MAXIMUM_CUSTOM_BLAZE_COLORS = 10;
    private static final int BLACK_COLOR = (new Color(0, 0, 0)).getRGB();

    private static final String SELECT_BLAZES_ACTION = "Select_Blazes";
    private static final String DESELECT_BLAZES_ACTION = "DeSelect_Blazes";
    private static final String ADD_BLUE_BLAZE_ACTION = "Add_Blaze_LightBlue";
    private static final String ADD_RED_BLAZE_ACTION = "Add_Blaze_Red";
    private static final String ADD_YELLOW_BLAZE_ACTION = "Add_Blaze_Yellow";
    private static final String ADD_CUSTOM_BLAZE_ACTION = "Add_Custom_Blaze";
    private static final String ADD_RECENT_BLAZE_ACTION = "Add_Recent_Blaze";
    private static final String ADD_PRESET_BLAZE_ACTION = "Add_Preset_Blaze";
    private static final String ADD_MOST_RECENT_BLAZE_ACTION = "Add_Most_Recent_Blaze";
    private static final String REMOVE_BLAZES_ACTION = "Remove_Blazes";

    private static final Color DEFAULT_COLOR = new Color(255, 255, 254);

    private final JPanel panel;
    private final JMenuBar menuBar;
    private final JMenu menu;

    private final List<JPanel> colorPanels;
    private final JPanel colorPanel1;
    private final JPanel colorPanel2;
    private final JPanel colorPanel3;
    private final JPanel colorPanel4;
    private final JPanel colorPanel5;
    private final JPanel colorPanel6;
    private final JPanel colorPanel7;
    private final JPanel colorPanel8;
    private final JPanel colorPanel9;
    private final JPanel colorPanel10;

    private Graph graph;
    private final SliderMenuItem sizeSlider;
    private final SliderMenuItem opacitySlider;
    private final ChangeListener sliderChangeListener;

    private final JMenuItem[] presetCustomBlazeItems = new JMenuItem[MAXIMUM_CUSTOM_BLAZE_COLORS];
    private static final List<ConstellationColor> presetCustomColors = new ArrayList<>();
    private static final Preferences prefs = NbPreferences.forModule(GraphPreferenceKeys.class);

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
            String colorString = prefs.get(GraphPreferenceKeys.BLAZE_PRESET_COLORS, GraphPreferenceKeys.BLAZE_PRESET_COLORS_DEFAULT);

            if (colorString == null || colorString.isBlank()) {
                colorString = BlazeUtilities.getHTMLColor(newColor) + ";";
            } else {
                // build up a string based on entries of the colorpanels
                final StringBuilder colorStringBuilder = new StringBuilder();

                for (final JPanel colorPanel : colorPanels) {
                    final String panelColor = BlazeUtilities.getHTMLColor(colorPanel.getBackground());
                    if (!panelColor.equals(BlazeUtilities.getHTMLColor(DEFAULT_COLOR))) {
                        colorStringBuilder.append(BlazeUtilities.getHTMLColor(colorPanel.getBackground()));
                        colorStringBuilder.append(";");
                    }
                }
                colorString = colorStringBuilder.toString();
            }
            prefs.put(GraphPreferenceKeys.BLAZE_PRESET_COLORS, colorString);
        }
    }

    /**
     * Returns static list of colors used to generate preset blaze color menus.
     *
     * @return List of colors used to generate preset blaze color menus.
     */
    public static List<ConstellationColor> getPresetCustomColors() {
        presetCustomColors.clear();
        final String presetCustomColorsString = prefs.get(GraphPreferenceKeys.BLAZE_PRESET_COLORS,
                GraphPreferenceKeys.BLAZE_PRESET_COLORS_DEFAULT);

        for (final String currentColor : presetCustomColorsString.split(SeparatorConstants.SEMICOLON)) {
            if (StringUtils.isNotBlank(currentColor)) {
                // here the color should be populated correctly. Add to list.
                presetCustomColors.add(ConstellationColor.fromHtmlColor(currentColor) == null
                        ? ConstellationColor.getColorValue(currentColor)
                        : ConstellationColor.fromHtmlColor(currentColor));
            }
        }
        return presetCustomColors;
    }

    /**
     * Perform a deep copy of the buffered image instance to create a complete
     * unique copy.
     *
     * @param bi The buffered image to copy.
     * @return the copied image.
     */
    protected static BufferedImage copyImageBuffer(final BufferedImage bi) {
        final ColorModel cm = bi.getColorModel();
        final WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, cm.isAlphaPremultiplied(), null);
    }

    public BlazeActions() {
        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        menuBar = new JMenuBar();
        menuBar.setOpaque(true);

        menu = new JMenu();
        menu.setIcon(ImageUtilities.loadImageIcon(BLAZE_ACTIONS_ICON, false));
        menu.setToolTipText("Blaze Controls");
        menu.addChangeListener(e -> {
            if (graph != null) {
                updateSliders(graph);
                updateCustomBlazes();
            }
        });
        menu.setEnabled(false);

        colorPanel1 = new JPanel();
        colorPanel2 = new JPanel();
        colorPanel3 = new JPanel();
        colorPanel4 = new JPanel();
        colorPanel5 = new JPanel();
        colorPanel6 = new JPanel();
        colorPanel7 = new JPanel();
        colorPanel8 = new JPanel();
        colorPanel9 = new JPanel();
        colorPanel10 = new JPanel();

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

        final List<ConstellationColor> colors = getPresetCustomColors();

        if (colors != null) {
            int panelCounter = 0;
            for (final JPanel panel : colorPanels) {
                if (panelCounter < colors.size()) {
                    if (colors.get(panelCounter) != null) {
                        panel.setBackground(colors.get(panelCounter++).getJavaColor());
                    }
                }
            }
        }

        final JPanel verticalPanel = new JPanel();
        verticalPanel.setMaximumSize(new Dimension(200, 75));

        final GridLayout gridLayout = new GridLayout(0, 5, 5, 5);
        verticalPanel.setLayout(gridLayout);

        verticalPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Preset Colors"), new EmptyBorder(10, 10, 10, 10)));
        //verticalPanel.setBorder(BorderFactory.createTitledBorder("Preset Colors"));

        for (final JPanel colorPanel : colorPanels) {
            colorPanel.setBackground(DEFAULT_COLOR);
            verticalPanel.add(colorPanel);
        }

        colorPanel1.setMaximumSize(new Dimension(5, 5));
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

        colorPanel2.setMaximumSize(new Dimension(5, 5));
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
                .addGap(0, 0, Short.MAX_VALUE)
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

        colorPanel4.setMaximumSize(new Dimension(5, 5));
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
                .addGap(0, 25, Short.MAX_VALUE)
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

        colorPanel6.setMaximumSize(new Dimension(5, 5));
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

        colorPanel7.setMaximumSize(new Dimension(5, 5));
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

        colorPanel8.setMaximumSize(new Dimension(5, 5));
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
                .addGap(0, 0, Short.MAX_VALUE)
        );

        colorPanel9.setMaximumSize(new Dimension(5, 5));
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

        colorPanel10.setMaximumSize(new Dimension(5, 5));
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

        final JMenuItem selectBlazesItem = new JMenuItem("Select Blazes");
        selectBlazesItem.setIcon(ImageUtilities.loadImageIcon(SELECT_BLAZES_ICON, false));
        selectBlazesItem.setActionCommand(SELECT_BLAZES_ACTION);
        selectBlazesItem.addActionListener(this);
        menu.add(selectBlazesItem);

        final JMenuItem deselectBlazesItem = new JMenuItem("Deselect Blazes");
        deselectBlazesItem.setIcon(ImageUtilities.loadImageIcon(DESELECT_BLAZES_ICON, false));
        deselectBlazesItem.setActionCommand(DESELECT_BLAZES_ACTION);
        deselectBlazesItem.addActionListener(this);
        menu.add(deselectBlazesItem);

//        final JMenuItem addRecentBlazeItem = new JMenuItem("Add Recent Blaze");
//        addRecentBlazeItem.setIcon(ImageUtilities.loadImageIcon(ADD_RECENT_BLAZE_ICON, false));
//        addRecentBlazeItem.setActionCommand(ADD_MOST_RECENT_BLAZE_ACTION);
//        addRecentBlazeItem.addActionListener(BlazeActions.this);
//        menu.add(addRecentBlazeItem);
        //final JMenuItem addRedBlazeItem = new JMenuItem("Add Red Blazes");
        //addRedBlazeItem.setIcon(ImageUtilities.loadImageIcon(ADD_RED_BLAZE_ICON, false));
        //addRedBlazeItem.setActionCommand(ADD_RED_BLAZE_ACTION);
        //addRedBlazeItem.addActionListener(BlazeActions.this);
        //menu.add(addRedBlazeItem);
        //final JMenuItem addYellowBlazeItem = new JMenuItem("Add Yellow Blazes");
        //addYellowBlazeItem.setIcon(ImageUtilities.loadImageIcon(ADD_YELLOW_BLAZE_ICON, false));
        //addYellowBlazeItem.setActionCommand(ADD_YELLOW_BLAZE_ACTION);
        //addYellowBlazeItem.addActionListener(BlazeActions.this);
        //menu.add(addYellowBlazeItem);
//        // Custom color menu is only enabled when a custom color has been added
//        customColorMenu = new JMenu();
//        customColorMenu.setIcon(ImageUtilities.loadImageIcon(ADD_RECENTMENU_BLAZE_ICON, false));
//        customColorMenu.setText("Custom Blazes");
//        customColorMenu.setToolTipText("Custom Blazes");
//        customColorMenu.setEnabled(false);
//        menu.add(customColorMenu);
//        // Preset color menu which is added to the Custom Color menu.
//        presetColorMenu = new JMenu();
//        presetColorMenu.setIcon(ImageUtilities.loadImageIcon(ADD_RECENTMENU_BLAZE_ICON, false));
//        presetColorMenu.setText("Preset Blazes");
//        presetColorMenu.setToolTipText("Preset Blazes");
//        presetColorMenu.setEnabled(false);
//        customColorMenu.add(presetColorMenu);
//
//        // Populate the Preset Colors into the menu
        for (int idx = 0; idx < MAXIMUM_CUSTOM_BLAZE_COLORS; idx++) {
            presetCustomBlazeItems[idx] = new JMenuItem("Preset" + idx);
            presetCustomBlazeItems[idx].setIcon(ImageUtilities.loadImageIcon(ADD_RECENT_BLAZE_ICON, false));
            presetCustomBlazeItems[idx].setActionCommand(ADD_PRESET_BLAZE_ACTION + idx);
            presetCustomBlazeItems[idx].addActionListener(BlazeActions.this);
            presetCustomBlazeItems[idx].setVisible(false);
            menu.add(presetCustomBlazeItems[idx]);
        }

        final JMenuItem colorBlazeItem = new JMenuItem("Add Custom Blazes");
        colorBlazeItem.setIcon(ImageUtilities.loadImageIcon(ADD_CUSTOM_BLAZE_ICON, false));
        colorBlazeItem.setActionCommand(ADD_CUSTOM_BLAZE_ACTION);
        colorBlazeItem.addActionListener(BlazeActions.this);
        menu.add(colorBlazeItem);

        // Recent color menu which is added to the Custom Color menu.
//        recentColorMenu = new JMenu();
//        recentColorMenu.setIcon(ImageUtilities.loadImageIcon(ADD_RECENTMENU_BLAZE_ICON, false));
//        recentColorMenu.setText("Recent Blazes");
//        recentColorMenu.setToolTipText("Recent Blazes");
//        recentColorMenu.setEnabled(false);
        //customColorMenu.add(recentColorMenu);
        // Populate the Recent Colors into the menu
//        for (int idx = 0; idx < MAXIMUM_CUSTOM_BLAZE_COLORS; idx++) {
//            recentCustomBlazeItems[idx] = new JMenuItem("custom" + idx);
//            recentCustomBlazeItems[idx].setIcon(ImageUtilities.loadImageIcon(ADD_RECENT_BLAZE_ICON, false));
//            recentCustomBlazeItems[idx].setActionCommand(ADD_RECENT_BLAZE_ACTION + idx);
//            recentCustomBlazeItems[idx].addActionListener(BlazeActions.this);
//            recentCustomBlazeItems[idx].setVisible(false);
//            recentColorMenu.add(recentCustomBlazeItems[idx]);
//        }
        final JMenuItem removeBlazeItem = new JMenuItem("Remove Blazes");
        removeBlazeItem.setIcon(ImageUtilities.loadImageIcon(REMOVE_BLAZE_ICON, false));
        removeBlazeItem.setActionCommand(REMOVE_BLAZES_ACTION);
        removeBlazeItem.addActionListener(BlazeActions.this);
        menu.add(removeBlazeItem);

        this.sizeSlider = new SliderMenuItem("Size");
        sizeSlider.setValue((prefs.getInt(GraphPreferenceKeys.BLAZE_SIZE, GraphPreferenceKeys.BLAZE_SIZE_DEFAULT)));
        menu.add(sizeSlider);

        this.opacitySlider = new SliderMenuItem("Opacity");
        opacitySlider.setValue((prefs.getInt(GraphPreferenceKeys.BLAZE_OPACITY, GraphPreferenceKeys.BLAZE_OPACITY_DEFAULT)));
        menu.add(opacitySlider);

        menu.add(verticalPanel);

        this.sliderChangeListener = e
                -> setBlazeProperties(sizeSlider.getValue() / 100f, opacitySlider.getValue() / 100f);
        sizeSlider.addChangeListener(sliderChangeListener);
        opacitySlider.addChangeListener(sliderChangeListener);

        menuBar.add(menu);
        panel.add(menuBar, BorderLayout.CENTER);

        GraphManager.getDefault().addGraphManagerListener(BlazeActions.this);
    }

    private void setBlazeProperties(final float size, final float opacity) {
        if (graph != null) {
            PluginExecution.withPlugin(VisualGraphPluginRegistry.UPDATE_BLAZE_SIZE_OPACITY)
                    .withParameter(UpdateBlazeSizeOpacityPlugin.SIZE_PARAMETER_ID, size)
                    .withParameter(UpdateBlazeSizeOpacityPlugin.OPACITY_PARAMETER_ID, opacity)
                    .executeLater(graph);
        }
    }
// TODO: Here i should be fixing the errors so that the presets load correctly.

    private void updateCustomBlazes() {
        for (int idx = 0; idx < MAXIMUM_CUSTOM_BLAZE_COLORS; idx++) {
            //this.recentCustomBlazeItems[BlazeActions.getRecentCustomColors().size()].setVisible(false);
            this.presetCustomBlazeItems[BlazeActions.getPresetCustomColors().size()].setVisible(false);
        }

        final List<ConstellationColor> colors = getPresetCustomColors();

        if (colors != null) {
            int panelCounter = 0;
            for (final JPanel panel : colorPanels) {
                if (panelCounter < colors.size()) {
                    if (colors.get(panelCounter) != null) {
                        panel.setBackground(colors.get(panelCounter++).getJavaColor());
                    }
                }
            }
        }

        //final boolean isRecentsEmpty = BlazeActions.getRecentCustomColors().isEmpty();
        //final boolean isPresetsEmpty = BlazeActions.getPresetCustomColors().isEmpty();
        //customColorMenu.setEnabled(!isRecentsEmpty || !isPresetsEmpty);
        //recentColorMenu.setEnabled(!isRecentsEmpty);
        //presetColorMenu.setEnabled(!isPresetsEmpty);
        //setBlazeItems(BlazeActions.getRecentCustomColors(), this.recentCustomBlazeItems);
        setBlazeItems(BlazeActions.getPresetCustomColors(), this.presetCustomBlazeItems);
    }

    /**
     * Set the items for the JMenuItem blazeItems with the colors passed in
     * throught the colors parameter.
     *
     * @param colors - the colors to set in the menu
     * @param blazeItems - the menu item to set the color of
     */
    private void setBlazeItems(final List<ConstellationColor> colors, final JMenuItem[] blazeItems) {

        Collections.reverse(colors);
        int idx = 0;
        for (final ConstellationColor color : colors) {
            if (color != null) {

                final Color javaColor = color.getJavaColor();
                String colorName = "#" + String.format("%02x", javaColor.getRed())
                        + String.format("%02x", javaColor.getGreen())
                        + String.format("%02x", javaColor.getBlue());

                if (color.getName() != null) {
                    colorName = color.getName();
                }

                blazeItems[MAXIMUM_CUSTOM_BLAZE_COLORS - idx - 1].setText(colorName);
                blazeItems[MAXIMUM_CUSTOM_BLAZE_COLORS - idx - 1].setVisible(true);
                blazeItems[MAXIMUM_CUSTOM_BLAZE_COLORS - idx - 1].setIcon(generateCustomImage(javaColor));
                idx++;
            }
        }
    }

    /**
     * Generate the color of a menu item by specifying the color and return a
     * suitable ImageIcon
     *
     * @param javaColor - the color to paint
     * @return the painted ImageIcon
     */
    private ImageIcon generateCustomImage(final Color javaColor) {
        final BufferedImage customImage = this.copyImageBuffer((BufferedImage) ImageUtilities.loadImage(ADD_RECENT_BLAZE_ICON, false));
        for (int x = 0; x < customImage.getWidth(); x++) {
            for (int y = 0; y < customImage.getHeight(); y++) {
                if (customImage.getRGB(x, y) == BLACK_COLOR) {
                    customImage.setRGB(x, y, javaColor.getRGB());
                }
            }
        }
        return new ImageIcon(customImage);
    }

    private void colorPanel1MouseClicked(MouseEvent evt) {
        loadColorPicker(1);
    }

    private void colorPanel2MouseClicked(MouseEvent evt) {
        loadColorPicker(2);
    }

    private void colorPanel3MouseClicked(MouseEvent evt) {
        loadColorPicker(3);
    }

    private void colorPanel4MouseClicked(MouseEvent evt) {
        loadColorPicker(4);
    }

    private void colorPanel5MouseClicked(MouseEvent evt) {
        loadColorPicker(5);
    }

    private void colorPanel6MouseClicked(MouseEvent evt) {
        loadColorPicker(6);
    }

    private void colorPanel7MouseClicked(MouseEvent evt) {
        loadColorPicker(3);
    }

    private void colorPanel8MouseClicked(MouseEvent evt) {
        loadColorPicker(4);
    }

    private void colorPanel9MouseClicked(MouseEvent evt) {
        loadColorPicker(5);
    }

    private void colorPanel10MouseClicked(MouseEvent evt) {
        loadColorPicker(6);
    }

    private void updateSliders(final Graph graph) {
        final ReadableGraph rg = graph.getReadableGraph();
        try {

            final int blazeSizeAttributeId = VisualConcept.GraphAttribute.BLAZE_SIZE.get(rg);
            final float blazeSize = blazeSizeAttributeId == Graph.NOT_FOUND
                    ? (prefs.getInt(GraphPreferenceKeys.BLAZE_SIZE, GraphPreferenceKeys.BLAZE_SIZE_DEFAULT)) / 100f
                    : rg.getFloatValue(blazeSizeAttributeId, 0);

            final int blazeOpacityAttributeId = VisualConcept.GraphAttribute.BLAZE_OPACITY.get(rg);
            final float blazeOpacity = blazeOpacityAttributeId == Graph.NOT_FOUND
                    ? (prefs.getInt(GraphPreferenceKeys.BLAZE_OPACITY, GraphPreferenceKeys.BLAZE_OPACITY_DEFAULT)) / 100f
                    : rg.getFloatValue(blazeOpacityAttributeId, 0);

            sizeSlider.removeChangeListener(sliderChangeListener);
            sizeSlider.setValue((int) (blazeSize * 100));
            sizeSlider.addChangeListener(sliderChangeListener);
            opacitySlider.removeChangeListener(sliderChangeListener);
            opacitySlider.setValue((int) (blazeOpacity * 100));
            opacitySlider.addChangeListener(sliderChangeListener);
        } finally {
            rg.release();
        }
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final String command = e.getActionCommand();
        final Plugin plugin;
        final PluginParameters parameters;
        final Pair<BitSet, ConstellationColor> selectionResult = BlazeUtilities.getSelection(graph, null);

        switch (command) {
            case ADD_CUSTOM_BLAZE_ACTION:
                final Pair<Boolean, ConstellationColor> colorResult = BlazeUtilities.colorDialog(selectionResult.getValue());
                if (colorResult.getKey()) {
                    final ConstellationColor color = colorResult.getValue();
                    plugin = PluginRegistry.get(VisualGraphPluginRegistry.ADD_CUSTOM_BLAZE);
                    parameters = DefaultPluginParameters.getDefaultParameters(plugin);
                    parameters.getParameters().get(BlazeUtilities.VERTEX_IDS_PARAMETER_ID).setObjectValue(selectionResult.getKey());
                    parameters.getParameters().get(BlazeUtilities.COLOR_PARAMETER_ID).setColorValue(color);
                    PluginExecution.withPlugin(plugin).withParameters(parameters).executeLater(graph);
                }
                break;
            case SELECT_BLAZES_ACTION:
                PluginExecution.withPlugin(VisualGraphPluginRegistry.SELECT_BLAZES).executeLater(graph);
                break;
            case DESELECT_BLAZES_ACTION:
                PluginExecution.withPlugin(VisualGraphPluginRegistry.DESELECT_BLAZES).executeLater(graph);
                break;
            case REMOVE_BLAZES_ACTION:
                plugin = PluginRegistry.get(VisualGraphPluginRegistry.REMOVE_BLAZE);
                parameters = DefaultPluginParameters.getDefaultParameters(plugin);
                parameters.getParameters().get(BlazeUtilities.VERTEX_IDS_PARAMETER_ID).setObjectValue(selectionResult.getKey());
                PluginExecution.withPlugin(plugin).withParameters(parameters).executeLater(graph);
                break;
            default:
                // check for the overloaded command name. In this case the default action name
                // ADD_RECENT_BLAZE_ACTION has the index of the custom color of interest appended
                // to it.
                if (command.startsWith(ADD_PRESET_BLAZE_ACTION)) {
                    final String idxStr = command.replaceFirst(ADD_PRESET_BLAZE_ACTION, "");
                    final ConstellationColor color = BlazeActions.presetCustomColors.get(MAXIMUM_CUSTOM_BLAZE_COLORS - Integer.parseInt(idxStr) - 1);

                    plugin = PluginRegistry.get(VisualGraphPluginRegistry.ADD_CUSTOM_BLAZE);
                    parameters = DefaultPluginParameters.getDefaultParameters(plugin);
                    parameters.getParameters().get(BlazeUtilities.VERTEX_IDS_PARAMETER_ID).setObjectValue(selectionResult.getKey());
                    parameters.getParameters().get(BlazeUtilities.COLOR_PARAMETER_ID).setColorValue(color);
                    PluginExecution.withPlugin(plugin).withParameters(parameters).executeLater(graph);
                }
                break;
        }
    }

    @Override
    public Component getToolbarPresenter() {
        return panel;
    }

    @Override
    public void graphOpened(final Graph graph) {
        // Required for GraphManagerListener, intentionally left blank
    }

    @Override
    public void graphClosed(final Graph graph) {
        // Required for GraphManagerListener, intentionally left blank
    }

    @Override
    public void newActiveGraph(final Graph graph) {
        if (this.graph != graph) {
            this.graph = graph;
            if (graph == null) {
                menu.setEnabled(false);
            } else {
                updateSliders(graph);
                updateCustomBlazes();
                menu.setEnabled(true);
            }
        }
    }

    private class SliderMenuItem extends JSlider implements MenuElement {

        public SliderMenuItem(final String title) {
            setBorder(new CompoundBorder(new TitledBorder(title), new EmptyBorder(3, 5, 3, 5)));
            setMaximum(100);
            setMinimum(10);
        }

        @Override
        public void processMouseEvent(MouseEvent event, MenuElement[] path, MenuSelectionManager manager) {
            processMouseMotionEvent(event);
            processMouseEvent(event);
        }

        @Override
        public void processKeyEvent(KeyEvent event, MenuElement[] path, MenuSelectionManager manager) {
            // Required for MenuElement, intentionally left blank
        }

        @Override
        public void menuSelectionChanged(boolean isIncluded) {
            // Required for MenuElement, intentionally left blank
        }

        @Override
        public MenuElement[] getSubElements() {
            return new MenuElement[0];
        }

        @Override
        public Component getComponent() {
            return this;
        }
    }
}
