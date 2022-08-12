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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.LookAndFeel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.openide.windows.WindowManager;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author OrionsGuardian
 */
public class ConstellationLAFSettingsNGTest {
    
    private static final Logger LOGGER = Logger.getLogger(ConstellationLAFSettingsNGTest.class.getName());
    final static PseudoLookAndFeel pseudoLAF = new PseudoLookAndFeel();
    final static PseudoUIDefaults pseudoUIdefaults = new PseudoUIDefaults();
    final static PseudoViewTabDisplayerUI pseudoViewDisplayerUI = new PseudoViewTabDisplayerUI();
    final static PseudoTabDisplayerUI pseudoTabDisplayerUI = new PseudoTabDisplayerUI();
    final static Class<?> pseudoViewDisplayerUIclass = pseudoViewDisplayerUI.getClass();
    final static Class<?> pseudoTabDisplayerUIclass = pseudoTabDisplayerUI.getClass();

    @Test
    public void runSetWindowsXPTabColors() {
        try (final MockedStatic<WindowManager> windowManagerMockedStatic = Mockito.mockStatic(WindowManager.class)) {
            final WindowManager windowManager = mock(WindowManager.class);
            windowManagerMockedStatic.when(WindowManager::getDefault).thenReturn(windowManager);
            final JFrame frame = mock(JFrame.class);
            when(windowManager.getMainWindow()).thenReturn(frame);
            
            LOGGER.info("TESTING ConstellationLAFSettings for Windows XP ...");
            UIManager.setLookAndFeel(pseudoLAF);
            pseudoLAF.setName("Windows XP");
            UIManager.getDefaults().put("org.netbeans.swing.tabcontrol.plaf.Windows8VectorViewTabDisplayerUI", null);
            UIManager.getDefaults().put("org.netbeans.swing.tabcontrol.plaf.WinXPViewTabDisplayerUI", pseudoTabDisplayerUIclass);
            ConstellationLAFSettings.applyTabColorSettings();
            final Color selectionIndicator = (Color) UIManager.getDefaults().get("TabbedPane.selectionIndicator");
            final Color expectedSelectionIndicator = new Color(180, 220, 255);                                                                
            Assert.assertTrue(selectionIndicator != null && selectionIndicator.equals(expectedSelectionIndicator));                                                              
            LOGGER.info("Windows XP LAF Test: PASSED");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "\n******* EXCEPTION *******\n", e);            
        }
    }
    
    @Test
    public void runSetWindows8TabColors() {
        try (final MockedStatic<WindowManager> windowManagerMockedStatic = Mockito.mockStatic(WindowManager.class)) {
            final WindowManager windowManager = mock(WindowManager.class);
            windowManagerMockedStatic.when(WindowManager::getDefault).thenReturn(windowManager);            
            final JFrame frame = mock(JFrame.class);
            when(windowManager.getMainWindow()).thenReturn(frame);            
            
            LOGGER.info("TESTING ConstellationLAFSettings for Windows 8+ ...");
            UIManager.setLookAndFeel(pseudoLAF);
            pseudoLAF.setName("Windows 8+");
            UIManager.getDefaults().put("org.netbeans.swing.tabcontrol.plaf.Windows8VectorViewTabDisplayerUI", pseudoViewDisplayerUIclass);
            ConstellationLAFSettings.applyTabColorSettings();
            final Color selectedBackgroundFiller = (Color) UIManager.getDefaults().get("tab_sel_fill");
            final Color expectedBackgroundFiller = new Color(160, 205, 255);                                                                
            Assert.assertTrue(selectedBackgroundFiller != null && selectedBackgroundFiller.equals(expectedBackgroundFiller));                                                              
            LOGGER.info("Windows 8+ LAF Test: PASSED");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "\n******* EXCEPTION *******\n", e);            
        }
    }
    
    @Test
    public void runSetNimbusTabColors() {
        try (final MockedStatic<WindowManager> windowManagerMockedStatic = Mockito.mockStatic(WindowManager.class)) {
            final WindowManager windowManager = mock(WindowManager.class);
            windowManagerMockedStatic.when(WindowManager::getDefault).thenReturn(windowManager);            
            final JFrame frame = mock(JFrame.class);
            when(windowManager.getMainWindow()).thenReturn(frame);            
            
            LOGGER.info("TESTING ConstellationLAFSettings for Nimbus ...");
            UIManager.setLookAndFeel(pseudoLAF);
            pseudoLAF.setName("Nimbus");
            ConstellationLAFSettings.applyTabColorSettings();
            final String nimbusPainterName = UIManager.getLookAndFeelDefaults().get("TabbedPane:TabbedPaneTab[Enabled].backgroundPainter").getClass().getName();
            Assert.assertTrue(nimbusPainterName != null && nimbusPainterName.contains("NimbusCustomGradientTabPainter"));
            LOGGER.info("Nimbus LAF Test: PASSED");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "\n******* EXCEPTION *******\n", e);            
        }
    }

        @Test
    public void runSetDarkNimbusTabColors() {
        try (final MockedStatic<WindowManager> windowManagerMockedStatic = Mockito.mockStatic(WindowManager.class)) {
            final WindowManager windowManager = mock(WindowManager.class);
            windowManagerMockedStatic.when(WindowManager::getDefault).thenReturn(windowManager);            
            final JFrame frame = mock(JFrame.class);
            when(windowManager.getMainWindow()).thenReturn(frame);            
            
            LOGGER.info("TESTING ConstellationLAFSettings for Dark Nimbus ...");
            UIManager.setLookAndFeel(pseudoLAF);
            pseudoLAF.setName("Dark Nimbus");
            UIManager.getDefaults().put("TabbedPane:TabbedPaneTab[Enabled].backgroundPainter", null);
            ConstellationLAFSettings.applyTabColorSettings();
            final String darkNimbusPainterName = UIManager.getLookAndFeelDefaults().get("TabbedPane:TabbedPaneTab[Enabled].backgroundPainter").getClass().getName();
            Assert.assertTrue(darkNimbusPainterName != null && darkNimbusPainterName.contains("NimbusCustomGradientTabPainter"));
            LOGGER.info("Dark Nimbus LAF Test: PASSED");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "\n******* EXCEPTION *******\n", e);            
        }
    }

    @Test
    public void runSetMetalTabColors() {
        try (final MockedStatic<WindowManager> windowManagerMockedStatic = Mockito.mockStatic(WindowManager.class)) {
            final WindowManager windowManager = mock(WindowManager.class);
            windowManagerMockedStatic.when(WindowManager::getDefault).thenReturn(windowManager);            
            final JFrame frame = mock(JFrame.class);
            when(windowManager.getMainWindow()).thenReturn(frame);            
            
            LOGGER.info("TESTING ConstellationLAFSettings for Metal ...");
            UIManager.setLookAndFeel(pseudoLAF);
            pseudoLAF.setName("Metal");
            UIManager.getDefaults().put("org.netbeans.swing.tabcontrol.plaf.MetalViewTabDisplayerUI", pseudoTabDisplayerUIclass);
            ConstellationLAFSettings.applyTabColorSettings();
            final Color highlight = (Color) UIManager.getDefaults().get("controlHighlight");
            final Color expectedHighlight = new Color(130, 170, 255);
            Assert.assertTrue(highlight != null && highlight.equals(expectedHighlight));
            LOGGER.info("Metal LAF Test: PASSED");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "\n******* EXCEPTION *******\n", e);            
        }
    }
    
    @Test
    public void runSetDarkMetalTabColors() {
        try (final MockedStatic<WindowManager> windowManagerMockedStatic = Mockito.mockStatic(WindowManager.class)) {
            final WindowManager windowManager = mock(WindowManager.class);
            windowManagerMockedStatic.when(WindowManager::getDefault).thenReturn(windowManager);            
            final JFrame frame = mock(JFrame.class);
            when(windowManager.getMainWindow()).thenReturn(frame);            
            
            LOGGER.info("TESTING ConstellationLAFSettings for Dark Metal ...");
            UIManager.setLookAndFeel(pseudoLAF);
            pseudoLAF.setName("Dark Metal");
            UIManager.getDefaults().put("org.netbeans.swing.tabcontrol.plaf.MetalViewTabDisplayerUI", pseudoTabDisplayerUIclass);
            ConstellationLAFSettings.applyTabColorSettings();
            final Color darkHighlight = (Color) UIManager.getDefaults().get("controlHighlight");
            final Color expectedDarkHighlight = new Color(45, 95, 180);
            Assert.assertTrue(darkHighlight != null && darkHighlight.equals(expectedDarkHighlight));
            LOGGER.info("Dark Metal LAF Test: PASSED");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "\n******* EXCEPTION *******\n", e);            
        }
    }
    
    @Test
    public void runSetFlatLafLightTabColors() {
        try (final MockedStatic<WindowManager> windowManagerMockedStatic = Mockito.mockStatic(WindowManager.class)) {
            final WindowManager windowManager = mock(WindowManager.class);
            windowManagerMockedStatic.when(WindowManager::getDefault).thenReturn(windowManager);            
            final JFrame frame = mock(JFrame.class);
            when(windowManager.getMainWindow()).thenReturn(frame);            
            
            LOGGER.info("TESTING ConstellationLAFSettings for FlatLafLight ...");
            UIManager.setLookAndFeel(pseudoLAF);
            pseudoLAF.setName("FlatLafLight");
            UIManager.getDefaults().put("org.netbeans.swing.laf.flatlaf.ui.FlatViewTabDisplayerUI", pseudoTabDisplayerUIclass);
            ConstellationLAFSettings.applyTabColorSettings();
            final Color inactiveUnderlineColor = (Color) UIManager.getDefaults().get("ViewTab.inactiveUnderlineColor");
            final Color expectedInactiveUnderlineColor = new Color(95, 130, 185);
            Assert.assertTrue(inactiveUnderlineColor != null && inactiveUnderlineColor.equals(expectedInactiveUnderlineColor));
            LOGGER.info("FlatLafLight LAF Test: PASSED");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "\n******* EXCEPTION *******\n", e);            
        }
    }

    @Test
    public void runSetFlatLafDarkTabColors() {
        try (final MockedStatic<WindowManager> windowManagerMockedStatic = Mockito.mockStatic(WindowManager.class)) {
            final WindowManager windowManager = mock(WindowManager.class);
            windowManagerMockedStatic.when(WindowManager::getDefault).thenReturn(windowManager);            
            final JFrame frame = mock(JFrame.class);
            when(windowManager.getMainWindow()).thenReturn(frame);            
            
            LOGGER.info("TESTING ConstellationLAFSettings for FlatLafDark ...");
            UIManager.setLookAndFeel(pseudoLAF);
            pseudoLAF.setName("FlatLafDark");
            UIManager.getDefaults().put("org.netbeans.swing.laf.flatlaf.ui.FlatViewTabDisplayerUI", pseudoTabDisplayerUIclass);
            ConstellationLAFSettings.applyTabColorSettings();
            final Color inactiveDarkUnderlineColor = (Color) UIManager.getDefaults().get("ViewTab.inactiveUnderlineColor");
            final Color expectedInactiveDarkUnderlineColor = new Color(20, 100, 175);
            Assert.assertTrue(inactiveDarkUnderlineColor != null && inactiveDarkUnderlineColor.equals(expectedInactiveDarkUnderlineColor));
            LOGGER.info("FlatLafDark LAF Test: PASSED");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "\n******* EXCEPTION *******\n", e);            
        }
    }

    @Test
    public void runErroredWindowsSetTabColors() {
        try (final MockedStatic<WindowManager> windowManagerMockedStatic = Mockito.mockStatic(WindowManager.class)) {
            final WindowManager windowManager = mock(WindowManager.class);
            windowManagerMockedStatic.when(WindowManager::getDefault).thenReturn(windowManager);            
            final JFrame frame = mock(JFrame.class);
            when(windowManager.getMainWindow()).thenReturn(frame);            
            
            LOGGER.info("ERROR TESTING on Windows ConstellationLAFSettings ...");
            UIManager.setLookAndFeel(pseudoLAF);
            pseudoLAF.setName("Windows XP");
            UIManager.getDefaults().put("org.netbeans.swing.tabcontrol.plaf.Windows8VectorViewTabDisplayerUI", null);
            UIManager.getDefaults().put("org.netbeans.swing.tabcontrol.plaf.WinXPViewTabDisplayerUI", pseudoViewDisplayerUIclass); 
            // Incorrect class used as the WinXPViewTabDisplayerUI should log an exception and make no color changes
            
            UIManager.getDefaults().put("TabbedPane.selectionIndicator", Color.BLACK);            
            ConstellationLAFSettings.applyTabColorSettings();
            final Color selectionIndicatorSetting = (Color) UIManager.getDefaults().get("TabbedPane.selectionIndicator");
            Assert.assertTrue(selectionIndicatorSetting != null && selectionIndicatorSetting.equals(Color.BLACK));                        
            LOGGER.info("ERROR TESTS on Windows: PASSED");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "\n******* EXCEPTION *******\n", e);            
        }
    }

    @Test
    public void runErroredMetalSetTabColors() {
        try (final MockedStatic<WindowManager> windowManagerMockedStatic = Mockito.mockStatic(WindowManager.class)) {
            final WindowManager windowManager = mock(WindowManager.class);
            windowManagerMockedStatic.when(WindowManager::getDefault).thenReturn(windowManager);            
            final JFrame frame = mock(JFrame.class);
            when(windowManager.getMainWindow()).thenReturn(frame);            
            
            LOGGER.info("ERROR TESTING on Metal ConstellationLAFSettings ...");
            UIManager.setLookAndFeel(pseudoLAF);
            pseudoLAF.setName("Metal");
            UIManager.getDefaults().put("org.netbeans.swing.tabcontrol.plaf.MetalViewTabDisplayerUI", pseudoViewDisplayerUIclass);
            // Incorrect class used as the MetalViewTabDisplayerUI should log an exception and make no color changes
            
            UIManager.getDefaults().put("TabRenderer.selectedBackground", Color.BLACK); 
            ConstellationLAFSettings.applyTabColorSettings();
            final Color selectedBackgroundSetting = (Color) UIManager.getDefaults().get("TabRenderer.selectedBackground");
            Assert.assertTrue(selectedBackgroundSetting != null && selectedBackgroundSetting.equals(Color.BLACK));
            LOGGER.info("ERROR TESTS on Metal: PASSED");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "\n******* EXCEPTION *******\n", e);            
        }
    }

    @Test
    public void runErroredFlatLafSetTabColors() {
        try (final MockedStatic<WindowManager> windowManagerMockedStatic = Mockito.mockStatic(WindowManager.class)) {
            final WindowManager windowManager = mock(WindowManager.class);
            windowManagerMockedStatic.when(WindowManager::getDefault).thenReturn(windowManager);            
            final JFrame frame = mock(JFrame.class);
            when(windowManager.getMainWindow()).thenReturn(frame);            
            
            LOGGER.info("ERROR TESTING on FlatLafLight ConstellationLAFSettings ...");
            UIManager.setLookAndFeel(pseudoLAF);
            pseudoLAF.setName("FlatLafLight");
            UIManager.getDefaults().put("org.netbeans.swing.laf.flatlaf.ui.FlatViewTabDisplayerUI", pseudoViewDisplayerUIclass);
            // Incorrect class used as the FlatViewTabDisplayerUI should log an exception and make no color changes
            
            UIManager.getDefaults().put("ViewTab.inactiveUnderlineColor", Color.BLACK); 
            ConstellationLAFSettings.applyTabColorSettings();
            final Color inactiveUnderlineColorSetting = (Color) UIManager.getDefaults().get("ViewTab.inactiveUnderlineColor");
            Assert.assertTrue(inactiveUnderlineColorSetting != null && inactiveUnderlineColorSetting.equals(Color.BLACK));
            LOGGER.info("ERROR TESTS on FlatLafLight: PASSED");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "\n******* EXCEPTION *******\n", e);            
        }
    }

    @Test
    public void runClassCoverageTestingTabColors() {
        try (final MockedStatic<WindowManager> windowManagerMockedStatic = Mockito.mockStatic(WindowManager.class)) {
            final WindowManager windowManager = mock(WindowManager.class);
            windowManagerMockedStatic.when(WindowManager::getDefault).thenReturn(windowManager);            
            final JFrame frame = mock(JFrame.class);
            when(windowManager.getMainWindow()).thenReturn(frame);            
            
            UIManager.setLookAndFeel(pseudoLAF);
            LOGGER.info("CLASS COVERAGE TESTING on ConstellationLAFSettings ...");

            ConstellationLAFSettings.ouputUIDefaultValues(null, "Tabbed");
            ConstellationLAFSettings.ouputUIDefaultValues("Tabbed", null);
            
            new ConstellationLAFSettings.NimbusCustomGradientTabPainter();
            new ConstellationLAFSettings.NimbusCustomGradientTabPainter(null);
            new ConstellationLAFSettings.NimbusCustomGradientTabPainter(Color.ORANGE);
            new ConstellationLAFSettings.NimbusCustomGradientTabPainter(null, null);
            new ConstellationLAFSettings.NimbusCustomGradientTabPainter(null, Color.ORANGE);
            new ConstellationLAFSettings.NimbusCustomGradientTabPainter(Color.ORANGE, null);            
                                    
            pseudoLAF.setName("Windows 95");
            UIManager.getDefaults().put("org.netbeans.swing.tabcontrol.plaf.Windows8VectorViewTabDisplayerUI", null);
            UIManager.getDefaults().put("org.netbeans.swing.tabcontrol.plaf.WinXPViewTabDisplayerUI", null); 
            ConstellationLAFSettings.applyTabColorSettings();
            UIManager.getDefaults().put("org.netbeans.swing.tabcontrol.plaf.Windows8VectorViewTabDisplayerUI", pseudoTabDisplayerUIclass);
            ConstellationLAFSettings.applyTabColorSettings();

            pseudoLAF.setName("FlatLafLight");
            UIManager.getDefaults().put("org.netbeans.swing.laf.flatlaf.ui.FlatViewTabDisplayerUI", null);
            ConstellationLAFSettings.applyTabColorSettings();
            
            pseudoLAF.setName("Metal");
            UIManager.getDefaults().put("org.netbeans.swing.tabcontrol.plaf.MetalViewTabDisplayerUI", null);
            UIManager.getDefaults().put("TabRenderer.selectedBackground", Color.BLACK); 
            ConstellationLAFSettings.applyTabColorSettings();
            final Color selectedBackgroundSetting = (Color) UIManager.getDefaults().get("TabRenderer.selectedBackground");
            Assert.assertTrue(selectedBackgroundSetting != null && selectedBackgroundSetting.equals(Color.BLACK));

            LOGGER.info("CLASS COVERAGE TESTS COMPLETE");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "\n******* EXCEPTION *******\n", e);            
        }
    }
   
    /**
     * This Pseudo class is used for testing purposes to simulate
     * a changeable look and feel.
     */
    private static class PseudoLookAndFeel extends LookAndFeel {
        
        String lafName = "Windows";
        
        PseudoLookAndFeel(){
            super();
        }
        
        @Override
        public String getName() {
            return lafName;
        }
        
        public void setName(String newLafName){
            lafName = newLafName;
        }
        
        @Override
        public UIDefaults getDefaults(){
            return pseudoUIdefaults;
        }

        @Override
        public String getID() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getDescription() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isNativeLookAndFeel() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isSupportedLookAndFeel() {
            return true;
        }
    }
    
    /**
     * This Pseudo class is used for testing purposes to simulate
     * a custom empty UIDefaults class.
     */
    private static class PseudoUIDefaults extends UIDefaults {
        
        Map<String, Object> settings = new HashMap<>();
        
        PseudoUIDefaults() {
            super();
        }
        
        public Object get(String key) {
            return settings.get(key);
        }
        
        public void put(String key, Object value) {
            settings.put(key, value);
        }
        
    }
    
}
