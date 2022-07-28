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
package au.gov.asd.tac.constellation.functionality.startup;

import java.awt.Color;
import java.awt.Graphics2D;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.Painter;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.openide.windows.WindowManager;

/**
 * This class contains a collection of separate UI settings to apply for a
 * few different Look and Feels packaged with NetBeans 12.0
 *
 * Note: Since the code is based on NetBeans 12.0, it may require updating with
 * newer versions NetBeans if the internal Look And Feel packages are changed.
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
            initNimbusTabColors(currentLafName.contains("DARK"));
        } else if (currentLafName.contains("FLAT")) {
            initFlatLafTabColors(currentLafName.contains("DARK"));
        } else if (currentLafName.contains("METAL")) {
            initMetalTabColors(currentLafName.contains("DARK"));
        }
        if (mainframe != null) {
            SwingUtilities.updateComponentTreeUI(mainframe);
        }
    }

    /**
     * Applies a Blue color theme for the main window tabs when using either the
     * <b>Windows</b> or <b>Windows Classic</b> Look and Feel.
     */
    private static void initWindowsTabColors() {
        // Fixed set of Blue shade colors
        // appropriate for Windows LAF
        final Color selectedUpperLightBlue = new Color(225, 235, 255);
        final Color activeMidSectionBlue = new Color(160, 205, 255);
        final Color selectedLowerDarkBlue = new Color(80, 160, 255);
        final Color unselectedUpperGray = new Color(225, 225, 225);
        final Color unselectedLowerGreyBlue = new Color(175, 185, 195);

        try {
            // To apply new tab colors to a Windows LAF
            // we need to find the current TabDisplayerUI
            // and force it to re-read from a revised set of colors
            Class<?> displayerClass = (Class<?>) UIManager.getDefaults()
                    .get("org.netbeans.swing.tabcontrol.plaf.Windows8VectorViewTabDisplayerUI");
            if (displayerClass != null) {
                final Class<?> tabDisplayer = displayerClass.getSuperclass();

                if (tabDisplayer == null) {
                    LOGGER.info(" >>> Windows LAF note : no superclass for displayerClass :");
                    ouputUIDefaultValues(null, null);
                    return;
                }                
                // Update the UIManager with settings appropriate for Windows (8+) LAF
                UIManager.getDefaults().put("tab_sel_fill", activeMidSectionBlue);
                UIManager.getDefaults().put("tab_focus_fill_upper", selectedUpperLightBlue);
                UIManager.getDefaults().put("tab_focus_fill_lower", selectedLowerDarkBlue);
                UIManager.getDefaults().put("tab_unsel_fill_upper", unselectedUpperGray);
                UIManager.getDefaults().put("tab_unsel_fill_lower", unselectedLowerGreyBlue);
                UIManager.getDefaults().put("tab_mouse_over_fill_upper", selectedUpperLightBlue);
                UIManager.getDefaults().put("tab_mouse_over_fill_lower", activeMidSectionBlue);
                UIManager.getDefaults().put("tab_attention_fill_upper", activeMidSectionBlue);
                UIManager.getDefaults().put("tab_attention_fill_lower", selectedLowerDarkBlue);
                
                // reset static field "colorsReady" value to false
                final Field colsReady = tabDisplayer.getDeclaredField("colorsReady"); //NOSONAR
                colsReady.setAccessible(true); //NOSONAR
                colsReady.setBoolean(tabDisplayer, false); //NOSONAR
            } else {
                LOGGER.info(" >>> Windows LAF note : org.netbeans.swing.tabcontrol.plaf.Windows8VectorViewTabDisplayerUI not defined :");
                // check if XP settings are being used
                displayerClass = (Class<?>) UIManager.getDefaults()
                    .get("org.netbeans.swing.tabcontrol.plaf.WinXPViewTabDisplayerUI");
                if (displayerClass != null) {
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

                    // reset static field "colorsReady" value to false
                    final Field colsReady = displayerClass.getDeclaredField("colorsReady"); //NOSONAR
                    colsReady.setAccessible(true); //NOSONAR
                    colsReady.setBoolean(displayerClass, false); //NOSONAR                
                    
                    Class<?>[] argList = null;
                    final Method initCols = displayerClass.getDeclaredMethod("initColors", argList);
                    initCols.setAccessible(true); //NOSONAR
                    initCols.invoke(displayerClass);
                } else {
                    LOGGER.info(" >>> Windows LAF note : org.netbeans.swing.tabcontrol.plaf.WinXPViewTabDisplayerUI not defined :");
                    /*  Unable to apply changes to Windows LAF
                        Possibly caused by a change in a newer NetBeans version */
                    ouputUIDefaultValues(null, null);
                }
            }
        } catch (final IllegalAccessException | IllegalArgumentException | InvocationTargetException |
                NoSuchMethodException | NoSuchFieldException | SecurityException e) {
            /*  An exception here would indicate a change in the internal LAF 
                packages provided by NetBeans */
            final String errorMessage = " >>> Error applying Windows LaF colors : " + e.toString();
            LOGGER.info(errorMessage);
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
            activeDarkBlue = new Color(65, 100, 155);
            selectedBlue = new Color(110, 155, 200);
            selectedDarkBlue = new Color(45, 80, 140);
            unselectedGreyBlue = new Color(140, 150, 155);
            unselectedDarkGreyBlue = new Color(100, 110, 115);
        } else {
            // Fixed set of Blue shade colors
            // appropriate for standard Nimbus LAF
            activeBlue = new Color(150, 200, 255);
            activeDarkBlue = new Color(90, 125, 180);
            selectedBlue = new Color(140, 180, 245);
            selectedDarkBlue = new Color(75, 115, 170);
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

        if (darkMode) {
            // Fixed set of Blue shade colors
            // appropriate for FlatLafDark LAF
            selectedUnderlineBlue = new Color(30, 110, 190);
            inactiveUnderlineBlue = new Color(20, 100, 175);
            selectedBackgroundBlue = new Color(30, 60, 95);
            hoverBackgroundBlue = new Color(45, 75, 115);
        } else {
            // Fixed set of Blue shade colors
            // appropriate for FlatLafLight LAF
            selectedUnderlineBlue = new Color(105, 145, 240);
            inactiveUnderlineBlue = new Color(95, 130, 185);
            selectedBackgroundBlue = new Color(200, 220, 255);
            hoverBackgroundBlue = new Color(190, 210, 255);
        }

        try {
            final Class<?> tabDisplayer = (Class<?>) UIManager.getDefaults()
                    .get("org.netbeans.swing.laf.flatlaf.ui.FlatViewTabDisplayerUI");
            
            if (tabDisplayer != null) {

                // Update the UIManager with settings appropriate for FlatLaf

                UIManager.put("ViewTab.selectedBackground", selectedBackgroundBlue);
                UIManager.put("ViewTab.hoverBackground", hoverBackgroundBlue);
                UIManager.put("ViewTab.underlineHeight", 5);
                UIManager.put("ViewTab.underlineColor", selectedUnderlineBlue);
                UIManager.put("ViewTab.inactiveUnderlineColor", inactiveUnderlineBlue);

                UIManager.put("EditorTab.selectedBackground", selectedBackgroundBlue);
                UIManager.put("EditorTab.hoverBackground", hoverBackgroundBlue);
                UIManager.put("EditorTab.underlineHeight", 5);
                UIManager.put("EditorTab.underlineColor", selectedUnderlineBlue);
                UIManager.put("EditorTab.inactiveUnderlineColor", inactiveUnderlineBlue);

                // reset static field "colorsReady" value to false
                final Field colready = tabDisplayer.getDeclaredField("colorsReady");
                colready.setAccessible(true); //NOSONAR
                colready.setBoolean(tabDisplayer, false); //NOSONAR

                // re-run class method "initColors" to load the updated colors
                Class<?>[] argList = null;
                final Method initCols = tabDisplayer.getDeclaredMethod("initColors", argList);
                initCols.setAccessible(true); //NOSONAR
                initCols.invoke(tabDisplayer);
            } else {
                /*  Unable to apply changes to FlatLaf
                    Possibly caused by a change in a newer NetBeans version */
                LOGGER.info(" >>> FlatLAF note : org.netbeans.swing.laf.flatlaf.ui.FlatViewTabDisplayerUI not defined :");
                ouputUIDefaultValues(null, null);
            }
        } catch (final IllegalAccessException | IllegalArgumentException | NoSuchFieldException | 
                NoSuchMethodException | SecurityException | InvocationTargetException e) {
            /*  An exception here would indicate a change in the internal LAF 
                packages provided by NetBeans */
            final String errorMessage = " >>> Error applying FlatLaf colors : " + e.toString();
            LOGGER.info(errorMessage);
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

                // Update the UIManager with settings appropriate for Metal LAF
                UIManager.put("control", inactiveTabBackground);
                UIManager.put("controlHighlight", activeTabBackground);
                UIManager.put("controlDkShadow", tabDarkShadow);
                UIManager.put("TabRenderer.selectedActivatedBackground", activeTabBackground);
                UIManager.put("TabRenderer.selectedBackground", inactiveTabBackground);
                
                // reset static field "actBgColor" value to activeTabBackground
                final Field actBgCol = tabDisplayer.getDeclaredField("actBgColor");
                actBgCol.setAccessible(true); //NOSONAR
                actBgCol.set(tabDisplayer, activeTabBackground); //NOSONAR

                // reset static field "inactBgColor" value to inactiveTabBackground
                final Field inactBgCol = tabDisplayer.getDeclaredField("inactBgColor");
                inactBgCol.setAccessible(true); //NOSONAR
                inactBgCol.set(tabDisplayer, inactiveTabBackground); //NOSONAR
            } else {
                /*  Unable to apply changes to Metal LAF
                    Possibly caused by a change in a newer NetBeans version */
                LOGGER.info(" >>> Metal LAF note : org.netbeans.swing.tabcontrol.plaf.MetalViewTabDisplayerUI not defined :");
                ouputUIDefaultValues(null, null);
            }
        } catch (final IllegalAccessException | IllegalArgumentException | 
                NoSuchFieldException | SecurityException e) {
            /*  An exception here would indicate a change in the internal LAF 
                packages provided by NetBeans */
            final String errorMessage = " >>> Error applying Metal Laf colors : " + e.toString();
            LOGGER.info(errorMessage);
        }
    }

    /**
     * Convenience method to assist with identifying UImanager settings
     * 
     * @param keyFilter limit output to key entries containing the keyFilter
     * @param valueFilter limit output to values containing the valueFilter
     */
    public static void ouputUIDefaultValues(final String keyFilter, final String valueFilter) {        
        List<Object> uiList = new ArrayList<>();
        uiList.addAll(UIManager.getDefaults().keySet());
        LOGGER.info(">> :: UIDefaults ::");
        String msg = "";
        for (Object uiKey : uiList) {
            // check each key stored in UIDefaults
            // filter out any keys which do not contain the keyFilter string
            // filter out any values which do not contain the valueFilter string
            if ( (keyFilter == null || uiKey.toString().contains(keyFilter)) && 
                 (valueFilter == null || UIManager.get(uiKey).toString().contains(valueFilter)) ) {
                msg = ">> :: " + uiKey + " = " + UIManager.get(uiKey);
                LOGGER.info(msg);                
            }
        }
        uiList.clear();
        uiList.addAll(UIManager.getLookAndFeelDefaults().keySet());
        LOGGER.info(">> :::: UILookAndFeelDefaults ::::");
        for (Object uiKey : uiList) {
            // check each key stored in UILookAndFeelDefaults
            // filter out any keys which do not contain the keyFilter string
            // filter out any values which do not contain the valueFilter string
            if ( (keyFilter == null || uiKey.toString().contains(keyFilter)) &&
                 (valueFilter == null || UIManager.getLookAndFeelDefaults().get(uiKey).toString().contains(valueFilter)) ) {
                msg = ">> :::: " + uiKey + " = " + UIManager.getLookAndFeelDefaults().get(uiKey);
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
    private static class NimbusCustomGradientTabPainter<T> implements Painter<T> {

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
            if (topShadeColor != null) {
                upperColor = topShadeColor;
            } else {
                upperColor = Color.WHITE;
            }
            if (bottomShadeColor != null) {
                lowerColor = bottomShadeColor;
            } else {
                lowerColor = Color.BLACK;
            }
        }

        
        @Override
        public void paint(Graphics2D g, Object j, int w, int h) {

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

            /*  note: whether the high and low values are actually higher or lower
                than each other doesn't matter.
                The gradient will still be calculated as a range between the
                "high" and "low" values. */
            
            int newRedShade;
            int newGreenShade;
            int newBlueShade;

            /*  The newShade colors will have step by step color adjustments
                calculated as the gradient tab background is being painted */ 

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
