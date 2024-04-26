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
package au.gov.asd.tac.constellation.functionality.startup;

import java.awt.Color;
import java.awt.Graphics2D;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.Painter;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.openide.windows.WindowManager;

/**
 * This class contains a collection of separate UI settings to apply for a
 * few different Look and Feels packaged with NetBeans 20.0
 *
 * *****************************************************************************
 * Note: Since the code is based on NetBeans 20.0, it may require updating with
 * newer versions NetBeans if the internal Look And Feel packages are changed.
 *******************************************************************************
 * 
 * @author OrionsGuardian
 */
public class ConstellationLAFSettings {

    private static final Logger LOGGER = Logger.getLogger(ConstellationLAFSettings.class.getName());
    
    private ConstellationLAFSettings() {
        // Added this private constructor to hide the implicit public one
    }
    
    public static void applyTabColorSettings() {
        final JFrame mainframe = (JFrame) WindowManager.getDefault().getMainWindow();
        final String currentLafName = UIManager.getLookAndFeel().getName().toUpperCase();
        // Call the appropriate init method for the current Look And Feel
        if (currentLafName.startsWith("WINDOWS")) {
            initWindowsTabColors();
        } else if (currentLafName.contains("NIMBUS")) {
            initNimbusTabColors(isDarkTheme());
        } else if (currentLafName.contains("FLAT")) {
            initFlatLafTabColors(isDarkTheme());
        } else if (currentLafName.contains("METAL")) {
            initMetalTabColors(isDarkTheme());
        }
        if (mainframe != null) {
            SwingUtilities.updateComponentTreeUI(mainframe);
        }
    }
    
    private static boolean isDarkTheme(){
        return UIManager.getLookAndFeel().getName().toUpperCase().contains("DARK");
    }

    /**
     * Applies a Blue color theme for the main window tabs when using either the
     * <b>Windows</b> or <b>Windows Classic</b> Look and Feel.
     */
    private static void initWindowsTabColors() {
        // Fixed set of Blue shade colors
        // appropriate for Windows LAF
        final Color selectedUpperLightBlue = new Color(225, 235, 255);
        final Color activeMidSectionBlue = new Color(155, 200, 255);
        final Color selectedLowerDarkBlue = new Color(70, 150, 255);
        final Color unselectedUpperGray = new Color(225, 225, 225);
        final Color unselectedLowerGreyBlue = new Color(175, 185, 195);
        final Color attentionUpperOrange = new Color(255, 220, 200);
        final Color attentionLowerOrange = new Color(220, 140, 120);

        try {
            // To apply new tab colors to a Windows LAF
            // we need to find the current TabDisplayerUI
            // and force it to re-read from a revised set of colors
            Class<?> displayerClass = (Class<?>) UIManager.getDefaults()
                    .get("org.netbeans.swing.tabcontrol.plaf.Windows8VectorViewTabDisplayerUI");
            if (displayerClass != null) {
                final Class<?> tabDisplayer = displayerClass.getSuperclass();

                if (tabDisplayer == null) {
                    LOGGER.log(Level.WARNING, " >>> Unable to apply Tab coloring to Windows LAF : no superclass for displayerClass :");
                    ouputUIDefaultValues(null, null);
                    return;
                }
                
                // get Reflection object before updating any UI settings
                final Field colsReady = tabDisplayer.getDeclaredField("colorsReady"); //NOSONAR

                // Update the UIManager with settings appropriate for Windows (8+) LAF
                UIManager.getDefaults().put("tab_sel_fill", activeMidSectionBlue);
                UIManager.getDefaults().put("tab_focus_fill_upper", selectedUpperLightBlue);
                UIManager.getDefaults().put("tab_focus_fill_lower", selectedLowerDarkBlue);
                UIManager.getDefaults().put("tab_unsel_fill_upper", unselectedUpperGray);
                UIManager.getDefaults().put("tab_unsel_fill_lower", unselectedLowerGreyBlue);
                UIManager.getDefaults().put("tab_mouse_over_fill_upper", selectedUpperLightBlue);
                UIManager.getDefaults().put("tab_mouse_over_fill_lower", activeMidSectionBlue);
                UIManager.getDefaults().put("tab_attention_fill_upper", attentionUpperOrange);
                UIManager.getDefaults().put("tab_attention_fill_lower", attentionLowerOrange);
                
                // reset static field "colorsReady" value to false
                colsReady.setAccessible(true); //NOSONAR
                colsReady.setBoolean(tabDisplayer, false); //NOSONAR
            } else {
                // check if XP settings are being used
                displayerClass = (Class<?>) UIManager.getDefaults()
                    .get("org.netbeans.swing.tabcontrol.plaf.WinXPViewTabDisplayerUI");
                if (displayerClass != null) {
                    // get Reflection objects before updating any UI settings
                    final Field colsReady = displayerClass.getDeclaredField("colorsReady"); //NOSONAR
                    final Method initCols = displayerClass.getDeclaredMethod("initColors");

                    // Update the UIManager with settings appropriate for Windows (XP) LAF
                    UIManager.getDefaults().put("TabbedPane.highlight", activeMidSectionBlue);
                    UIManager.getDefaults().put("tab_focus_fill_bright", selectedUpperLightBlue);
                    UIManager.getDefaults().put("tab_focus_fill_dark", selectedLowerDarkBlue);
                    UIManager.getDefaults().put("tab_unsel_fill_bright", unselectedUpperGray);
                    UIManager.getDefaults().put("tab_unsel_fill_dark", unselectedLowerGreyBlue);
                    // Put a light blue highlight on the selected tab
                    final Color activeLighterMidBlue = new Color(180, 220, 255);
                    UIManager.getDefaults().put("tab_highlight_header", activeLighterMidBlue);
                    UIManager.getDefaults().put("tab_highlight_header_fill", activeLighterMidBlue);
                    // The following settings are used in the WinXPEditorTabCellRenderer
                    UIManager.getDefaults().put("TabbedPane.selectionIndicator", activeLighterMidBlue);
                    UIManager.getDefaults().put("tab_sel_fill_bright", selectedUpperLightBlue);
                    UIManager.getDefaults().put("tab_sel_fill_dark", selectedLowerDarkBlue);

                    // reset static field "colorsReady" value to false
                    colsReady.setAccessible(true); //NOSONAR
                    colsReady.setBoolean(displayerClass, false); //NOSONAR    
                    
                    // re-run class method "initColors" to load the updated colors
                    initCols.setAccessible(true); //NOSONAR
                    initCols.invoke(displayerClass);
                } else {
                    // check if Windows FlatView settings are being used
                    displayerClass = (Class<?>) UIManager.getDefaults()
                        .get("org.netbeans.swing.tabcontrol.plaf.WinFlatViewTabDisplayerUI");
                    if (displayerClass != null) {
                        // The Windows FlatView actually uses FlatLaf settings
                        initFlatLafTabColors(false);                                                                    
                    } else {                    
                        // Unable to apply changes to Windows LAF
                        // Most likely caused by a change to a newer NetBeans version
                        LOGGER.log(Level.WARNING, " >>> Unable to apply Tab coloring to Windows LAF : neither Windows8VectorViewTabDisplayerUI nor WinXPViewTabDisplayerUI are defined :");
                        ouputUIDefaultValues(null, null);
                    }
                }
            }
        } catch (final IllegalAccessException | IllegalArgumentException | InvocationTargetException |
                NoSuchMethodException | NoSuchFieldException | SecurityException e) {
            // An exception here would indicate a change in the internal LAF 
            // packages provided by NetBeans
            LOGGER.log(Level.SEVERE, " >>> Error applying Windows LaF colors :", e);
        }     
    }

    /**
     * Applies a Blue color theme for the main window tabs when using the
     * <b>Nimbus</b> Look and Feel.
     *
     * @param darkMode When true, allocates Look and Feel tab colors suitable for 
     * <u>Dark Nimbus</u>, otherwise allocates colors suitable for <u>Nimbus</u>
     */
    private static void initNimbusTabColors(final boolean darkMode) {
        final Color activeBlue;
        final Color activeDarkBlue;
        final Color selectedBlue;
        final Color selectedDarkBlue;
        final Color unselectedGreyBlue;
        final Color unselectedDarkGreyBlue;
        
        if (darkMode) {
            // Fixed set of Blue shade colors
            // appropriate for Nimbus dark LAF
            activeBlue = new Color(125, 170, 225);
            activeDarkBlue = new Color(60, 95, 150);
            selectedBlue = new Color(105, 155, 200);
            selectedDarkBlue = new Color(50, 85, 145);
            unselectedGreyBlue = new Color(140, 150, 155);
            unselectedDarkGreyBlue = new Color(100, 110, 115);
        } else {
            // Fixed set of Blue shade colors
            // appropriate for standard Nimbus LAF
            activeBlue = new Color(175, 204, 252);
            activeDarkBlue = new Color(121, 153, 228);
            selectedBlue = new Color(145, 185, 250);
            selectedDarkBlue = new Color(99, 137, 200);
            unselectedGreyBlue = new Color(190, 205, 215);
            unselectedDarkGreyBlue = new Color(150, 160, 175);
        }
        
        // Modifying the tab colors in Nimbus requires custom Tab background painters to be defined
        UIManager.getLookAndFeelDefaults().put("TabbedPane:TabbedPaneTab[MouseOver+Selected].backgroundPainter", 
                new NimbusCustomGradientTabPainter<>(activeBlue, activeDarkBlue));
        UIManager.getLookAndFeelDefaults().put("TabbedPane:TabbedPaneTab[Selected].backgroundPainter", 
                new NimbusCustomGradientTabPainter<>(selectedBlue, selectedDarkBlue));
        UIManager.getLookAndFeelDefaults().put("TabbedPane:TabbedPaneTab[Enabled].backgroundPainter", 
                new NimbusCustomGradientTabPainter<>(unselectedGreyBlue, unselectedDarkGreyBlue));
        
        // Some components may need to be reinitialized
        UIManager.getLookAndFeel().uninitialize();
        UIManager.getLookAndFeel().initialize();
    }

    /**
     * Applies a Blue color theme for the main window tabs when using the
     * <b>FlatLaf</b> Look and Feel.
     *
     * @param darkMode When true, allocates Look and Feel tab colors suitable for 
     * <u>FlatLafDark</u>, otherwise allocates colors suitable for <u>FlatLafLight</u>
     */
    private static void initFlatLafTabColors(final boolean darkMode) {
        final Color selectedUnderlineBlue;
        final Color inactiveUnderlineBlue;
        final Color selectedBackgroundBlue;
        final Color hoverBackgroundBlue;
        final Color selectedHoverBackgroundBlue;
        final Color attentionBackgroundOrange;
        final Color gradientBackgroundBlue;
        final Color unselectedHoverBackgroundBlue;
        final Color unselectedBackground;

        if (darkMode) {
            // Fixed set of Blue shade colors
            // appropriate for FlatLafDark LAF
            selectedUnderlineBlue = new Color(35, 115, 200);
            inactiveUnderlineBlue = new Color(20, 95, 140);
            unselectedBackground = new Color(5,10,20);
            selectedBackgroundBlue = new Color(30, 60, 100);
            hoverBackgroundBlue = new Color(35, 65, 110);
            selectedHoverBackgroundBlue = new Color(40, 70, 120);
            unselectedHoverBackgroundBlue = new Color(40, 70, 110);
            gradientBackgroundBlue = new Color(40, 70, 110);
            attentionBackgroundOrange = new Color(148, 52, 40);
        } else {
            // Fixed set of Blue shade colors
            // appropriate for FlatLafLight LAF
            unselectedBackground = new Color(185, 195, 210);
            selectedUnderlineBlue = new Color(75, 130, 240);
            inactiveUnderlineBlue = new Color(88, 138, 190);
            selectedBackgroundBlue = new Color(200, 220, 255);
            gradientBackgroundBlue = new Color(175, 200, 250);
            hoverBackgroundBlue = new Color(195, 215, 250);
            selectedHoverBackgroundBlue = new Color(212, 230, 252);
            unselectedHoverBackgroundBlue = new Color(195, 215, 255);
            attentionBackgroundOrange = new Color(250, 170, 150);
        }

        try {
            Class<?> tabDisplayer = (Class<?>) UIManager.getDefaults()
                    .get("org.netbeans.swing.laf.flatlaf.ui.FlatViewTabDisplayerUI");
            if (tabDisplayer == null) {
                // Check for Windows FlatView
                tabDisplayer = (Class<?>) UIManager.getDefaults()
                    .get("org.netbeans.swing.tabcontrol.plaf.WinFlatViewTabDisplayerUI");
            }
            
            if (tabDisplayer != null) {
                // get Reflection objects before updating any UI settings
                final Field colready = tabDisplayer.getDeclaredField("colorsReady");
                final Method initCols = tabDisplayer.getDeclaredMethod("initColors");

                // Update the UIManager with settings appropriate for FlatLaf
                UIManager.getDefaults().put("ViewTab.background", unselectedBackground);
                UIManager.getDefaults().put("ViewTab.selectedBackground", selectedBackgroundBlue);
                UIManager.getDefaults().put("ViewTab.hoverBackground", hoverBackgroundBlue);
                UIManager.getDefaults().put("ViewTab.selectedHoverBackground", selectedHoverBackgroundBlue);
                UIManager.getDefaults().put("ViewTab.unselectedHoverBackground", unselectedHoverBackgroundBlue);
                UIManager.getDefaults().put("ViewTab.underlineHeight", 3);
                UIManager.getDefaults().put("ViewTab.underlineColor", selectedUnderlineBlue);
                UIManager.getDefaults().put("ViewTab.inactiveUnderlineColor", inactiveUnderlineBlue); // ViewTab.attentionForeground
                UIManager.getDefaults().put("ViewTab.attentionBackground", attentionBackgroundOrange);
                UIManager.getDefaults().put("ViewTab.showTabSeparators", true);
                
                UIManager.getDefaults().put("EditorTab.background", unselectedBackground);
                UIManager.getDefaults().put("EditorTab.selectedBackground", selectedBackgroundBlue);
                UIManager.getDefaults().put("EditorTab.selectedBackgroundBottomGradient", gradientBackgroundBlue);
                UIManager.getDefaults().put("EditorTab.hoverBackground", hoverBackgroundBlue);
                UIManager.getDefaults().put("EditorTab.selectedHoverBackground", selectedHoverBackgroundBlue);
                UIManager.getDefaults().put("EditorTab.unselectedHoverBackground", unselectedHoverBackgroundBlue);
                UIManager.getDefaults().put("EditorTab.underlineHeight", 3);
                UIManager.getDefaults().put("EditorTab.underlineColor", selectedUnderlineBlue);
                UIManager.getDefaults().put("EditorTab.inactiveUnderlineColor", inactiveUnderlineBlue);
                UIManager.getDefaults().put("EditorTab.attentionBackground", attentionBackgroundOrange);
                UIManager.getDefaults().put("EditorTab.showTabSeparators", true);

                // reset static field "colorsReady" value to false
                colready.setAccessible(true); //NOSONAR
                colready.setBoolean(tabDisplayer, false); //NOSONAR
                
                // re-run class method "initColors" to load the updated colors
                initCols.setAccessible(true); //NOSONAR
                initCols.invoke(tabDisplayer);
            } else {
                // Unable to apply changes to FlatLaf
                // Possibly caused by a change in a newer NetBeans version
                LOGGER.log(Level.WARNING, " >>> Unable to apply Tab coloring to FlatLAF : org.netbeans.swing.laf.flatlaf.ui.FlatViewTabDisplayerUI not defined :");
                ouputUIDefaultValues(null, null);
            }
        } catch (final IllegalAccessException | IllegalArgumentException | NoSuchFieldException | 
                NoSuchMethodException | SecurityException | InvocationTargetException e) {
            // An exception here would indicate a change in the internal LAF 
            // packages provided by NetBeans
            LOGGER.log(Level.SEVERE, " >>> Error applying FlatLaf colors :", e);
        }
    }

    /**
     * Applies a Blue color theme for the main window tabs when using the
     * <b>Metal</b> Look and Feel.
     *
     * @param darkMode When true, allocates Look and Feel tab colors suitable for 
     * <u>Dark Metal</u>, otherwise allocates colors suitable for <u>Metal</u>
     */
    private static void initMetalTabColors(final boolean darkMode) {
        final Color activeTabBackground;
        final Color inactiveTabBackground;
        final Color tabDarkShadow;
        
        if (darkMode) {
            // Fixed set of Blue shade colors
            // appropriate for Dark Metal LAF
            activeTabBackground = new Color(45, 95, 180);
            tabDarkShadow = new Color(20, 25, 30);
            inactiveTabBackground = new Color(40, 55, 90);
        } else {
            // Fixed set of Blue shade colors
            // appropriate for standard Metal LAF
            activeTabBackground = new Color(130, 170, 255);
            tabDarkShadow = new Color(220, 235, 250);
            inactiveTabBackground = new Color(170, 195, 230);
        }
        
        try {
            final Class<?> tabDisplayer = (Class<?>) UIManager.getDefaults()
                    .get("org.netbeans.swing.tabcontrol.plaf.MetalViewTabDisplayerUI");
            
            if (tabDisplayer != null) {
                // get Reflection objects before updating any UI settings
                final Field actBgCol = tabDisplayer.getDeclaredField("actBgColor");
                final Field inactBgCol = tabDisplayer.getDeclaredField("inactBgColor");

                // Update the UIManager with settings appropriate for Metal LAF
                UIManager.getDefaults().put("control", inactiveTabBackground);
                UIManager.getDefaults().put("controlHighlight", activeTabBackground);
                UIManager.getDefaults().put("controlDkShadow", tabDarkShadow);
                UIManager.getDefaults().put("TabRenderer.selectedActivatedBackground", activeTabBackground);
                UIManager.getDefaults().put("TabRenderer.selectedBackground", inactiveTabBackground);
                
                // reset static field "actBgColor" value to activeTabBackground
                actBgCol.setAccessible(true); //NOSONAR
                actBgCol.set(tabDisplayer, activeTabBackground); //NOSONAR

                // reset static field "inactBgColor" value to inactiveTabBackground
                inactBgCol.setAccessible(true); //NOSONAR
                inactBgCol.set(tabDisplayer, inactiveTabBackground); //NOSONAR
            } else {
                // Unable to apply changes to Metal LAF
                // Possibly caused by a change in a newer NetBeans version
                LOGGER.log(Level.WARNING, " >>> Unable to apply Tab coloring to Metal LAF : org.netbeans.swing.tabcontrol.plaf.MetalViewTabDisplayerUI not defined.");
                ouputUIDefaultValues(null, null);
            }
        } catch (final IllegalAccessException | IllegalArgumentException | 
                NoSuchFieldException | SecurityException e) {
            // An exception here would indicate a change in the internal LAF 
            // packages provided by NetBeans
            LOGGER.log(Level.SEVERE, " >>> Error applying Metal Laf colors :", e);
        }
    }

    /**
     * Convenience method to assist with identifying UImanager settings
     * 
     * Note: This method should only remain in the code temporarily, 
     * for a few releases after its introduction, to assist in any additional changes
     * that may be needed for other Look and Feels being used     
     * 
     * @param keyFilter limit output to key entries containing the keyFilter
     * @param valueFilter limit output to values containing the valueFilter
     */
    public static void ouputUIDefaultValues(final String keyFilter, final String valueFilter) {        
        List<Object> uiList = new ArrayList<>();
        uiList.addAll(UIManager.getDefaults().keySet());
        LOGGER.info(">> :: UIDefaults ::");
        for (final Object uiKey : uiList) {
            // check each key stored in UIDefaults
            // filter out any keys which do not contain the keyFilter string
            // filter out any values which do not contain the valueFilter string
            if ( (keyFilter == null || uiKey.toString().contains(keyFilter)) && 
                 (valueFilter == null || UIManager.get(uiKey).toString().contains(valueFilter)) ) {
                final String msg = ">> :: " + uiKey + " = " + UIManager.get(uiKey);
                LOGGER.info(msg);                
            }
        }
        uiList.clear();
        uiList.addAll(UIManager.getLookAndFeelDefaults().keySet());
        LOGGER.info(">> :::: UILookAndFeelDefaults ::::");
        for (final Object uiKey : uiList) {
            // check each key stored in UILookAndFeelDefaults
            // filter out any keys which do not contain the keyFilter string
            // filter out any values which do not contain the valueFilter string
            if ( (keyFilter == null || uiKey.toString().contains(keyFilter)) &&
                 (valueFilter == null || UIManager.getLookAndFeelDefaults().get(uiKey).toString().contains(valueFilter)) ) {
                final String msg = ">> :::: " + uiKey + " = " + UIManager.getLookAndFeelDefaults().get(uiKey);
                LOGGER.info(msg);                
            }
        }
    }    
    
    /**
     * Custom painter required for Nimbus Look and Feel to override the default
     * colors used for tabs.
     * Will fill the background color of the tab with a gradient transition of
     * color between the color(s) specified in the constructor call.
     */
    public static class NimbusCustomGradientTabPainter<T> implements Painter<T> {

        private final Color upperColor;
        private final Color lowerColor;

        /**
         * Default constructor will use a White to Black color gradient.
         */
        public NimbusCustomGradientTabPainter() {
            upperColor = Color.WHITE;
            lowerColor = Color.BLACK;
        }

        /**
         * Single color constructor will use the specified color as the upper
         * (lighter shade) color and calculate a darker shade of the same color
         * to use as the bottom shade.
         * The background color of the tab will be painted with a gradient
         * transition between the light and dark shades of the color.
         * @param baseColor upper/lighter shade of color to be used in the color gradient
         */
        public NimbusCustomGradientTabPainter(final Color baseColor) {
            if (baseColor != null) {
                upperColor = baseColor;
                float[] baseHSB = new float[3];
                Color.RGBtoHSB(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), baseHSB);
                lowerColor = new Color(Color.HSBtoRGB(baseHSB[0], baseHSB[1], baseHSB[2] * 2 / 3));
            } else {
                upperColor = Color.WHITE;
                lowerColor = Color.BLACK;
            }
        }

        /**
         * Dual color constructor allows top and bottom shades to be specified
         * individually. The Tab background will be painted as a gradient from
         * top to bottom, transitioning between the specified colors.
         * @param topShadeColor Color to paint at top of Tab
         * @param bottomShadeColor Color to paint at bottom of Tab
         */
        public NimbusCustomGradientTabPainter(final Color topShadeColor, final Color bottomShadeColor) {
            upperColor = (topShadeColor != null) ? topShadeColor : Color.WHITE;
            lowerColor = (bottomShadeColor != null) ? bottomShadeColor : Color.BLACK;
        }

        
        @Override
        public void paint(final Graphics2D g, final Object j, final int w, final int h) {

            // set some fixed positioning values relating to the tab's height
            final int startOffset = h / 6;
            final int endOffset = h - startOffset;
            final int gradientRange = endOffset > startOffset ? endOffset - startOffset : 1;
            
            // fill top section of tab with upper color
            g.setColor(upperColor);
            g.fillRect(1, 0, w - 2, startOffset);

            // store "high" values for each color component
            final int highRed = upperColor.getRed();
            final int highGreen = upperColor.getGreen();
            final int highBlue = upperColor.getBlue();
            
            // store "low" values for each color component
            final int lowRed = lowerColor.getRed();
            final int lowGreen = lowerColor.getGreen();
            final int lowBlue = lowerColor.getBlue();

            // note: whether the high and low values are actually higher or lower
            // than each other doesn't matter.
            // The gradient will still be calculated as a range between the
            // "high" and "low" values.
            
            int newRedShade;
            int newGreenShade;
            int newBlueShade;

            // The newShade colors will have step by step color adjustments
            // calculated as the gradient tab background is being painted

            // draw a color gradient background in the middle section of the tab
            for (int heightPosition = startOffset; heightPosition < endOffset; heightPosition++) {
                // calculate a linear increase for each color component
                newRedShade = highRed - (highRed - lowRed) * (heightPosition - startOffset) / gradientRange;
                newGreenShade = highGreen - (highGreen - lowGreen) * (heightPosition - startOffset) / gradientRange;
                newBlueShade = highBlue - (highBlue - lowBlue) * (heightPosition - startOffset) / gradientRange;
                
                // iteratively draw a line with the adjusted color components
                g.setColor(new Color(newRedShade, newGreenShade, newBlueShade));
                g.fillRect(1, heightPosition, w - 2, 1);
            }

            // fill bottom section of tab with lower color
            g.setColor(lowerColor);
            g.fillRect(1, endOffset, w - 2, startOffset);

            // draw a border line around the tab with a smoothed corner
            g.drawLine(1, 0, w - 2, 0);
            g.drawLine(1, 1, 1, 1);
            g.drawLine(w - 2, 1, w - 2, 1);
            g.drawLine(0, 1, 0, h - 1);
            g.drawLine(w - 1, 1, w - 1, h - 1);
        }
    }
}
