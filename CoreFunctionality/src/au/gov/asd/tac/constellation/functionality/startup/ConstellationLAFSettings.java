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

    public static void applyTabColorSettings() {
        JFrame mainframe = (JFrame) WindowManager.getDefault().getMainWindow();
        String currentLafName = UIManager.getLookAndFeel().getName().toUpperCase();
        if (currentLafName.startsWith("WINDOWS")) {
            initWindowsTabColors();
        } else if (currentLafName.contains("NIMBUS")) {
            initNimbusTabColors(currentLafName.contains("DARK"));
        } else if (currentLafName.contains("FLAT")) {
            initFlatLafTabColors(currentLafName.contains("DARK"));
        } else if (currentLafName.contains("METAL")) {
            initMetalTabColors(currentLafName.contains("DARK"));
        }
        SwingUtilities.updateComponentTreeUI(mainframe);
    }

    /**
     * Applies a Blue color theme for the main window tabs when using either the
     * <b>Windows</b> or <b>Windows Classic</b> Look and Feel.
     */
    private static void initWindowsTabColors() {
        Color selectedUpperLightBlue = new Color(225, 235, 255);
        Color activeMidSectionBlue = new Color(140, 185, 255);
        Color selectedLowerDarkBlue = new Color(50, 130, 255);
        Color unselectedUpperGray = new Color(220, 220, 220);
        Color unselectedLowerGreyBlue = new Color(165, 175, 185);

        UIManager.getDefaults().put("tab_sel_fill", activeMidSectionBlue);
        UIManager.getDefaults().put("tab_focus_fill_upper", selectedUpperLightBlue);
        UIManager.getDefaults().put("tab_focus_fill_lower", selectedLowerDarkBlue);
        UIManager.getDefaults().put("tab_unsel_fill_upper", unselectedUpperGray);
        UIManager.getDefaults().put("tab_unsel_fill_lower", unselectedLowerGreyBlue);
        UIManager.getDefaults().put("tab_mouse_over_fill_upper", selectedUpperLightBlue);
        UIManager.getDefaults().put("tab_mouse_over_fill_lower", activeMidSectionBlue);
        UIManager.getDefaults().put("tab_attention_fill_upper", activeMidSectionBlue);
        UIManager.getDefaults().put("tab_attention_fill_lower", selectedLowerDarkBlue);

        try {
            Class<?> tabDisplayer = ((Class<?>) UIManager.getDefaults()
                    .get("org.netbeans.swing.tabcontrol.plaf.Windows8VectorViewTabDisplayerUI")).getSuperclass();
            
            // reset static field "colorsReady" value to false
            Field colsReady = tabDisplayer.getDeclaredField("colorsReady");
            colsReady.setAccessible(true);
            colsReady.setBoolean(tabDisplayer, false);
        } catch (IllegalAccessException | IllegalArgumentException | 
                NoSuchFieldException | SecurityException e) {
            String errorMessage = " >>> Error applying Windows LaF colors : " + e.toString();
            Logger.getLogger(ConstellationLAFSettings.class.getName()).info(errorMessage);
        }
    }

    /**
     * Applies a Blue color theme for the main window tabs when using the
     * <b>Nimbus</b> Look and Feel.
     *
     * @param darkMode When true, allocates Look and Feel tab colors suitable for 
     * <u>Dark Nimbus</u>, otherwise allocates colors suitable for <u>Nimbus</u>
     */
    private static void initNimbusTabColors(boolean darkMode) {
        Color activeBlue,
                activeDarkBlue,
                selectedBlue,
                selectedDarkBlue,
                unselectedGreyBlue,
                unselectedDarkGreyBlue;
        
        if (darkMode) {
            activeBlue = new Color(125, 170, 225);
            activeDarkBlue = new Color(65, 100, 155);
            selectedBlue = new Color(110, 155, 200);
            selectedDarkBlue = new Color(45, 80, 140);
            unselectedGreyBlue = new Color(140, 150, 155);
            unselectedDarkGreyBlue = new Color(100, 110, 115);
        } else {
            activeBlue = new Color(150, 200, 255);
            activeDarkBlue = new Color(90, 125, 180);
            selectedBlue = new Color(140, 180, 245);
            selectedDarkBlue = new Color(75, 115, 170);
            unselectedGreyBlue = new Color(190, 205, 215);
            unselectedDarkGreyBlue = new Color(150, 160, 175);
        }
        
        UIManager.getLookAndFeelDefaults().put("TabbedPane:TabbedPaneTab[MouseOver+Selected].backgroundPainter", 
                new NimbusCustomGradientTabPainter(activeBlue, activeDarkBlue));
        UIManager.getLookAndFeelDefaults().put("TabbedPane:TabbedPaneTab[Selected].backgroundPainter", 
                new NimbusCustomGradientTabPainter(selectedBlue, selectedDarkBlue));
        UIManager.getLookAndFeelDefaults().put("TabbedPane:TabbedPaneTab[Enabled].backgroundPainter", 
                new NimbusCustomGradientTabPainter(unselectedGreyBlue, unselectedDarkGreyBlue));
        
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
    private static void initFlatLafTabColors(boolean darkMode) {
        Color selectedUnderlineBlue,
                inactiveUnderlineBlue,
                selectedBackgroundBlue,
                hoverBackgroundBlue;

        if (darkMode) {
            selectedUnderlineBlue = new Color(30, 110, 190);
            inactiveUnderlineBlue = new Color(20, 100, 175);
            selectedBackgroundBlue = new Color(30, 60, 95);
            hoverBackgroundBlue = new Color(45, 75, 115);
        } else {
            selectedUnderlineBlue = new Color(105, 145, 240);
            inactiveUnderlineBlue = new Color(95, 130, 185);
            selectedBackgroundBlue = new Color(200, 220, 255);
            hoverBackgroundBlue = new Color(190, 210, 255);
        }

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

        try {
            Class<?> tabDisplayer = (Class<?>) UIManager.getDefaults()
                    .get("org.netbeans.swing.laf.flatlaf.ui.FlatViewTabDisplayerUI");
            
            // reset static field "colorsReady" value to false
            Field colready = tabDisplayer.getDeclaredField("colorsReady");
            colready.setAccessible(true);
            colready.setBoolean(tabDisplayer, false);
            
            // re-run class method "initColors" to load the updated colors
            Class<?>[] argList = null;
            Method initCols = tabDisplayer.getDeclaredMethod("initColors", argList);
            initCols.setAccessible(true);
            initCols.invoke(tabDisplayer);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | 
                NoSuchMethodException | SecurityException | InvocationTargetException e) {
            String errorMessage = " >>> Error applying FlatLaf colors : " + e.toString();
            Logger.getLogger(ConstellationLAFSettings.class.getName()).info(errorMessage);
        }
    }

    /**
     * Applies a Blue color theme for the main window tabs when using the
     * <b>Metal</b> Look and Feel.
     *
     * @param darkMode When true, allocates Look and Feel tab colors suitable for 
     * <u>Dark Metal</u>, otherwise allocates colors suitable for <u>Metal</u>
     */
    private static void initMetalTabColors(boolean darkMode) {
        Color activeTabBackground, inactiveTabBackground, tabDarkShadow;
        
        if (darkMode) {
            activeTabBackground = new Color(45, 95, 180);
            tabDarkShadow = new Color(20, 25, 30);
            inactiveTabBackground = new Color(40, 55, 90);
        } else {
            activeTabBackground = new Color(130, 170, 255);
            tabDarkShadow = new Color(220, 235, 250);
            inactiveTabBackground = new Color(170, 195, 230);
        }

        UIManager.put("control", inactiveTabBackground);
        UIManager.put("controlHighlight", activeTabBackground);
        UIManager.put("controlDkShadow", tabDarkShadow);
        UIManager.put("TabRenderer.selectedActivatedBackground", activeTabBackground);
        UIManager.put("TabRenderer.selectedBackground", inactiveTabBackground);
        
        try {
            Class<?> tabDisplayer = (Class<?>) UIManager.getDefaults()
                    .get("org.netbeans.swing.tabcontrol.plaf.MetalViewTabDisplayerUI");
            
            // reset static field "actBgColor" value to activeTabBackground
            Field actBgCol = tabDisplayer.getDeclaredField("actBgColor");
            actBgCol.setAccessible(true);
            actBgCol.set(tabDisplayer, activeTabBackground);
            
            // reset static field "inactBgColor" value to inactiveTabBackground
            Field inactBgCol = tabDisplayer.getDeclaredField("inactBgColor");
            inactBgCol.setAccessible(true);
            inactBgCol.set(tabDisplayer, inactiveTabBackground);
        } catch (IllegalAccessException | IllegalArgumentException | 
                NoSuchFieldException | SecurityException e) {
            String errorMessage = " >>> Error applying Metal Laf colors : " + e.toString();
            Logger.getLogger(ConstellationLAFSettings.class.getName()).info(errorMessage);
        }
    }

    /**
     * Custom painter required for Nimbus Look and Feel to override the default
     * colors used for tabs.
     * Will fill the background color of the tab with a gradient transition of
     * color between the color(s) specified in the constructor call.
     */
    private static class NimbusCustomGradientTabPainter implements Painter {

        private Color upperColor = Color.WHITE;
        private Color lowerColor = Color.BLACK;

        /**
         * Default constructor will use a White to Black color gradient.
         */
        public NimbusCustomGradientTabPainter() {
        }

        /**
         * Single color constructor will use the specified color as the upper
         * (lighter shade) color and calculate a darker shade of the same color
         * to use as the bottom shade.
         * The background color of the tab will be painted with a gradient
         * transition between the light and dark shades of the color.
         * @param baseColor upper/lighter shade of color to be used in the color gradient
         */
        public NimbusCustomGradientTabPainter(Color baseColor) {
            if (baseColor != null) {
                upperColor = baseColor;
                float[] baseHSB = new float[3];
                Color.RGBtoHSB(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), baseHSB);
                lowerColor = new Color(Color.HSBtoRGB(baseHSB[0], baseHSB[1], baseHSB[2] * 2 / 3));
            }
        }

        /**
         * Dual color constructor allows top and bottom shades to be specified
         * individually. The Tab background will be painted as a gradient from
         * top to bottom, transitioning between the specified colors.
         * @param topShadeColor Color to paint at top of Tab
         * @param bottomShadeColor Color to paint at bottom of Tab
         */
        public NimbusCustomGradientTabPainter(Color topShadeColor, Color bottomShadeColor) {
            if (topShadeColor != null) {
                upperColor = topShadeColor;
            }
            if (bottomShadeColor != null) {
                lowerColor = bottomShadeColor;
            }
        }

        @Override
        public void paint(Graphics2D g, Object j, int w, int h) {

            int startOffset = h / 6;
            int endOffset = h - startOffset;
            int gradientRange = endOffset > startOffset ? endOffset - startOffset : 1;
            
            // fill top section of tab with upper color
            g.setColor(upperColor);
            g.fillRect(1, 0, w - 2, startOffset);

            int highRed = upperColor.getRed();
            int highGreen = upperColor.getGreen();
            int highBlue = upperColor.getBlue();
            int lowRed = lowerColor.getRed();
            int lowGreen = lowerColor.getGreen();
            int lowBlue = lowerColor.getBlue();

            int newRedShade, newGreenShade, newBlueShade;

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
