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
package au.gov.asd.tac.constellation.functionality.startup;

import au.gov.asd.tac.constellation.security.ConstellationSecurityManager;
import au.gov.asd.tac.constellation.security.proxy.ProxyUtilities;
import au.gov.asd.tac.constellation.utilities.BrandingUtilities;
import au.gov.asd.tac.constellation.utilities.font.FontUtilities;
import java.awt.Color;
import java.awt.Graphics2D;
import java.lang.reflect.Field;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.Painter;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.openide.windows.OnShowing;
import org.openide.windows.WindowManager;


/**
 * Application Bootstrap on start up
 *
 * @author cygnus_x-1
 * @author arcturus
 */
@OnShowing()
public class Startup implements Runnable {

    private static final String SYSTEM_ENVIRONMENT = "constellation.environment";

    // DO NOT CHANGE THIS VALUE
    // continous integration scripts use this to update the version dynamically
    private static final String VERSION = "(under development)";

    /**
     * This is the system property that is set to true in order to make the AWT
     * thread run in headless mode for tests, etc.
     */
    private static final String AWT_HEADLESS_PROPERTY = "java.awt.headless";

    @Override
    public void run() {
        ConstellationSecurityManager.startSecurityLater(null);

        // application environment
        final String environment = System.getProperty(SYSTEM_ENVIRONMENT);
        final String name = environment != null
                ? String.format("%s %s", BrandingUtilities.APPLICATION_NAME, environment)
                : BrandingUtilities.APPLICATION_NAME;

        // We only want to run this if headless is NOT set to true
        if (!Boolean.TRUE.toString().equalsIgnoreCase(System.getProperty(AWT_HEADLESS_PROPERTY))) {
            // update the main window title with the version number
            WindowManager.getDefault().invokeWhenUIReady(() -> {
                final JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
                final String title = String.format("%s - %s", name, VERSION);
                frame.setTitle(title);
                try {
                  String currentLafName = UIManager.getLookAndFeel().getName();
                  if (currentLafName.contains("Windows")) {
                    initWindowsColors();
                    Class<?> tabDisplayer = ((Class<?>) UIManager.getDefaults().get("org.netbeans.swing.tabcontrol.plaf.Windows8VectorViewTabDisplayerUI")).getSuperclass();
                    Field colsReady = tabDisplayer.getDeclaredField("colorsReady");
                    colsReady.setAccessible(true);
                    colsReady.setBoolean(tabDisplayer, false);
                  } else if (currentLafName.contains("Nimbus")) {
                    if (currentLafName.contains("Dark")) {
                      initDarkNimbusColors();
                    } else {
                      initNimbusColors();
                    }
                    UIManager.getLookAndFeel().uninitialize();
                    UIManager.getLookAndFeel().initialize();
                    SwingUtilities.updateComponentTreeUI(frame);
                  }
                } catch (Exception e) {
                  Logger.getLogger(Startup.class.getName()).info(" > > > > > Error applying Consty colours to LaF : " + e.toString());
                }
        
            });
        }

        FontUtilities.initialiseOutputFontPreferenceOnFirstUse();
        FontUtilities.initialiseApplicationFontPreferenceOnFirstUse();

        ProxyUtilities.setProxySelector(null);
    }
    
    private static void initWindowsColors() {
      Color constyLightBlue = new Color(230, 240, 255);
      Color  constyMidBlue = new Color(135, 180, 255);
      Color  constyDarkBlue = new Color(50, 130, 255);
      Color constyBackBlue = new Color(165, 175, 185);
      Color constyGray = new Color(220, 220, 220);

      UIManager.getDefaults().put("tab_sel_fill", constyMidBlue);
      UIManager.getDefaults().put("tab_focus_fill_upper", constyLightBlue);
      UIManager.getDefaults().put("tab_focus_fill_lower", constyDarkBlue);
      UIManager.getDefaults().put("tab_unsel_fill_upper", constyGray);
      UIManager.getDefaults().put("tab_unsel_fill_lower", constyBackBlue);
      UIManager.getDefaults().put("tab_mouse_over_fill_upper", constyLightBlue);
      UIManager.getDefaults().put("tab_mouse_over_fill_lower", constyMidBlue);
      UIManager.getDefaults().put("tab_attention_fill_upper", constyMidBlue);
      UIManager.getDefaults().put("tab_attention_fill_lower", constyDarkBlue);

    }
 
    private static void initNimbusColors() {

      Color  constyActiveBlue = new Color(155, 200, 255);
      Color  constyActiveDarkBlue = new Color(95, 130, 185);

      Color  constySelectedBlue = new Color(140, 185, 230);
      Color  constySelectedDarkBlue = new Color(75, 110, 170);

      Color constyUnselectedGreyBlue = new Color(190, 205, 215);
      Color constyUnselectedDarkGreyBlue = new Color(150, 160, 175);

      UIManager.getLookAndFeelDefaults().put("TabbedPane:TabbedPaneTab[Selected].backgroundPainter", new CustomTabPainter(constySelectedBlue, constySelectedDarkBlue));
      UIManager.getLookAndFeelDefaults().put("TabbedPane:TabbedPaneTab[MouseOver+Selected].backgroundPainter", new CustomTabPainter(constyActiveBlue, constyActiveDarkBlue));
      UIManager.getLookAndFeelDefaults().put("TabbedPane:TabbedPaneTab[Enabled].backgroundPainter", new CustomTabPainter(constyUnselectedGreyBlue, constyUnselectedDarkGreyBlue));

    }
    private static void initDarkNimbusColors() {

      Color  constyActiveBlue = new Color(125, 170, 225);
      Color  constyActiveDarkBlue = new Color(65, 100, 155);

      Color  constySelectedBlue = new Color(110, 155, 200);
      Color  constySelectedDarkBlue = new Color(45, 80, 140);

      Color constyUnselectedGreyBlue = new Color(140, 150, 155);
      Color constyUnselectedDarkGreyBlue = new Color(100, 110, 115);

      UIManager.getLookAndFeelDefaults().put("TabbedPane:TabbedPaneTab[Selected].backgroundPainter", new CustomTabPainter(constySelectedBlue, constySelectedDarkBlue));
      UIManager.getLookAndFeelDefaults().put("TabbedPane:TabbedPaneTab[MouseOver+Selected].backgroundPainter", new CustomTabPainter(constyActiveBlue, constyActiveDarkBlue));
      UIManager.getLookAndFeelDefaults().put("TabbedPane:TabbedPaneTab[Enabled].backgroundPainter", new CustomTabPainter(constyUnselectedGreyBlue, constyUnselectedDarkGreyBlue));

    }



    public static class CustomTabPainter implements Painter {

      private Color upperColor = Color.WHITE;
      private Color lowerColor = Color.BLACK;

      public CustomTabPainter(Color baseColor) {
        if (baseColor != null) {
          upperColor = baseColor;
          float[] baseHSB = new float[3];
          Color.RGBtoHSB(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), baseHSB);
          lowerColor = new Color(Color.HSBtoRGB(baseHSB[0], baseHSB[1], baseHSB[2] * 2 / 3));
        }
      }

      public CustomTabPainter(Color topShadeColor, Color bottomShadeColor) {
        if (topShadeColor != null) {
          upperColor = topShadeColor;
        }
        if (bottomShadeColor != null) {
          lowerColor = bottomShadeColor;
        }
      }

      @Override
      public void paint(Graphics2D g, Object j, int w, int h) {
        g.setColor(upperColor);
        int startOffset = h/6;
        int endOffset = h - startOffset;
        g.fillRect(1,0,w-2,startOffset);
        int highRed = upperColor.getRed();
        int highGreen = upperColor.getGreen();
        int highBlue = upperColor.getBlue();
        int lowRed = lowerColor.getRed();
        int lowGreen = lowerColor.getGreen();
        int lowBlue = lowerColor.getBlue();

        int newRedShade, newGreenShade, newBlueShade;

        for (int heightPosition = startOffset; heightPosition < endOffset; heightPosition++) {
          newRedShade = highRed - (highRed - lowRed) * (heightPosition - startOffset + 1) / (endOffset - startOffset + 1);
          newGreenShade = highGreen - (highGreen - lowGreen) * (heightPosition - startOffset + 1) / (endOffset - startOffset + 1);
          newBlueShade = highBlue - (highBlue - lowBlue) * (heightPosition - startOffset + 1) / (endOffset - startOffset + 1);
          g.setColor(new Color(newRedShade, newGreenShade, newBlueShade));
          g.fillRect(1, heightPosition, w-2, 1);
        }

        g.setColor(lowerColor);
        g.fillRect(1, endOffset, w-2, startOffset);
        g.drawLine(1,0,w-2,0);
        g.drawLine(1, 1, 1, 1);
        g.drawLine(w-2,1,w-2,1);
        g.drawLine(0,1,0,h-1);
        g.drawLine(w-1,1,w-1,h-1);

      }

    }
  
}
