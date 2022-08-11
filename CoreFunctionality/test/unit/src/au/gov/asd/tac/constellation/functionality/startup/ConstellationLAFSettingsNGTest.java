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
    
    @Test
    public void runSetLAFTabColors() {
        try (final MockedStatic<WindowManager> windowManagerMockedStatic = Mockito.mockStatic(WindowManager.class)) {
            final WindowManager windowManager = mock(WindowManager.class);
            windowManagerMockedStatic.when(WindowManager::getDefault).thenReturn(windowManager);
            
            LOGGER.info("TESTING ConstellationLAFSettings ...");
            UIManager.setLookAndFeel(pseudoLAF);
            final JFrame frame = mock(JFrame.class);
            when(windowManager.getMainWindow()).thenReturn(frame);

            PseudoViewTabDisplayerUI pseudoViewDisplayerUI = new PseudoViewTabDisplayerUI();
            PseudoTabDisplayerUI pseudoTabDisplayerUI = new PseudoTabDisplayerUI();
            Class<?> pseudoViewDisplayerUIclass = pseudoViewDisplayerUI.getClass();
            Class<?> pseudoTabDisplayerUIclass = pseudoTabDisplayerUI.getClass();

            // Test XP LAF settings
            pseudoLAF.setName("Windows XP");
            UIManager.getDefaults().put("org.netbeans.swing.tabcontrol.plaf.Windows8VectorViewTabDisplayerUI", null);
            UIManager.getDefaults().put("org.netbeans.swing.tabcontrol.plaf.WinXPViewTabDisplayerUI", pseudoTabDisplayerUIclass);

            ConstellationLAFSettings.applyTabColorSettings();
            final Color selectionIndicator = (Color) UIManager.getDefaults().get("TabbedPane.selectionIndicator");
            final Color expectedSelectionIndicator = new Color(180, 220, 255);                                                                
            Assert.assertTrue(selectionIndicator != null && selectionIndicator.equals(expectedSelectionIndicator));                                                              
            LOGGER.info("Testing LAF: Windows XP : PASSED");

            // Test Win 8+ LAF settings
            pseudoLAF.setName("Windows 8+");
            UIManager.getDefaults().put("org.netbeans.swing.tabcontrol.plaf.Windows8VectorViewTabDisplayerUI", pseudoViewDisplayerUIclass);
            ConstellationLAFSettings.applyTabColorSettings();
            final Color selectedBackgroundFiller = (Color) UIManager.getDefaults().get("tab_sel_fill");
            final Color expectedBackgroundFiller = new Color(160, 205, 255);                                                                
            Assert.assertTrue(selectedBackgroundFiller != null && selectedBackgroundFiller.equals(expectedBackgroundFiller));                                                              
            LOGGER.info("Testing LAF: Windows 8+ : PASSED");
            
            // Test Nimbus LAF settings
            pseudoLAF.setName("Nimbus");
            ConstellationLAFSettings.applyTabColorSettings();
            final String nimbusPainterName = UIManager.getLookAndFeelDefaults().get("TabbedPane:TabbedPaneTab[Enabled].backgroundPainter").getClass().getName();
            Assert.assertTrue(nimbusPainterName != null && nimbusPainterName.contains("NimbusCustomGradientTabPainter"));
            LOGGER.info("Testing LAF: Nimbus : PASSED");

            pseudoLAF.setName("Dark Nimbus");
            UIManager.getDefaults().put("TabbedPane:TabbedPaneTab[Enabled].backgroundPainter", null);
            ConstellationLAFSettings.applyTabColorSettings();
            final String darkNimbusPainterName = UIManager.getLookAndFeelDefaults().get("TabbedPane:TabbedPaneTab[Enabled].backgroundPainter").getClass().getName();
            Assert.assertTrue(darkNimbusPainterName != null && darkNimbusPainterName.contains("NimbusCustomGradientTabPainter"));
            LOGGER.info("Testing LAF: Dark Nimbus : PASSED");

            // Test FlatLaf settings
            pseudoLAF.setName("FlatLafLight");
            UIManager.getDefaults().put("org.netbeans.swing.laf.flatlaf.ui.FlatViewTabDisplayerUI", pseudoTabDisplayerUIclass);
            ConstellationLAFSettings.applyTabColorSettings();
            final Color inactiveUnderlineColor = (Color) UIManager.getDefaults().get("ViewTab.inactiveUnderlineColor");
            final Color expectedInactiveUnderlineColor = new Color(95, 130, 185);
            Assert.assertTrue(inactiveUnderlineColor != null && inactiveUnderlineColor.equals(expectedInactiveUnderlineColor));
            LOGGER.info("Testing LAF: FlatLafLight : PASSED");

            pseudoLAF.setName("FlatLafDark");
            UIManager.getDefaults().put("ViewTab.underlineHeight", 1);
            ConstellationLAFSettings.applyTabColorSettings();
            final Color inactiveDarkUnderlineColor = (Color) UIManager.getDefaults().get("ViewTab.inactiveUnderlineColor");
            final Color expectedInactiveDarkUnderlineColor = new Color(20, 100, 175);
            Assert.assertTrue(inactiveDarkUnderlineColor != null && inactiveDarkUnderlineColor.equals(expectedInactiveDarkUnderlineColor));
            LOGGER.info("Testing LAF: FlatLafDark : PASSED");

            // Test Metal LAF settings
            pseudoLAF.setName("Metal");
            UIManager.getDefaults().put("org.netbeans.swing.tabcontrol.plaf.MetalViewTabDisplayerUI", pseudoTabDisplayerUIclass);
            ConstellationLAFSettings.applyTabColorSettings();
            final Color highlight = (Color) UIManager.getDefaults().get("controlHighlight");
            final Color expectedHighlight = new Color(130, 170, 255);
            Assert.assertTrue(highlight != null && highlight.equals(expectedHighlight));
            LOGGER.info("Testing LAF: Metal : PASSED");

            pseudoLAF.setName("Dark Metal");
            ConstellationLAFSettings.applyTabColorSettings();
            final Color darkHighlight = (Color) UIManager.getDefaults().get("controlHighlight");
            final Color expectedDarkHighlight = new Color(45, 95, 180);
            Assert.assertTrue(darkHighlight != null && darkHighlight.equals(expectedDarkHighlight));
            LOGGER.info("Testing LAF: Dark Metal : PASSED");
            
            // ERROR TESTS
            // Test that no changes are made when exceptions occur
            
            pseudoLAF.setName("Windows XP");
            UIManager.getDefaults().put("org.netbeans.swing.tabcontrol.plaf.Windows8VectorViewTabDisplayerUI", null);
            UIManager.getDefaults().put("org.netbeans.swing.tabcontrol.plaf.WinXPViewTabDisplayerUI", pseudoViewDisplayerUIclass); 
            // Incorrect class used as the WinXPViewTabDisplayerUI should log an exception and make no color changes
            
            UIManager.getDefaults().put("TabbedPane.selectionIndicator", Color.BLACK);            
            ConstellationLAFSettings.applyTabColorSettings();
            final Color selectionIndicatorSetting = (Color) UIManager.getDefaults().get("TabbedPane.selectionIndicator");
            Assert.assertTrue(selectionIndicatorSetting != null && selectionIndicatorSetting.equals(Color.BLACK));
            
            
            pseudoLAF.setName("FlatLafLight");
            UIManager.getDefaults().put("org.netbeans.swing.laf.flatlaf.ui.FlatViewTabDisplayerUI", pseudoViewDisplayerUIclass);
            // Incorrect class used as the FlatViewTabDisplayerUI should log an exception and make no color changes
            
            UIManager.getDefaults().put("ViewTab.inactiveUnderlineColor", Color.BLACK); 
            ConstellationLAFSettings.applyTabColorSettings();
            final Color inactiveUnderlineColorSetting = (Color) UIManager.getDefaults().get("ViewTab.inactiveUnderlineColor");
            Assert.assertTrue(inactiveUnderlineColorSetting != null && inactiveUnderlineColorSetting.equals(Color.BLACK));


            pseudoLAF.setName("Metal");
            UIManager.getDefaults().put("org.netbeans.swing.tabcontrol.plaf.MetalViewTabDisplayerUI", pseudoViewDisplayerUIclass);
            // Incorrect class used as the MetalViewTabDisplayerUI should log an exception and make no color changes
            
            UIManager.getDefaults().put("TabRenderer.selectedBackground", Color.BLACK); 
            ConstellationLAFSettings.applyTabColorSettings();
            final Color selectedBackgroundSetting = (Color) UIManager.getDefaults().get("TabRenderer.selectedBackground");
            Assert.assertTrue(selectedBackgroundSetting != null && selectedBackgroundSetting.equals(Color.BLACK));
            
            LOGGER.info("Testing exceptions / Processing invalid settings: PASSED");
            
            // CLASS COVERAGE CALLS
            // Tests more code paths in ConstellationLAFSettings class
            ConstellationLAFSettings.ouputUIDefaultValues(null, "Tabbed");
            ConstellationLAFSettings.ouputUIDefaultValues("Tabbed", null);

            LOGGER.info("********************************************");
            
            pseudoLAF.setName("Windows 95");
            UIManager.getDefaults().put("org.netbeans.swing.tabcontrol.plaf.Windows8VectorViewTabDisplayerUI", null);
            UIManager.getDefaults().put("org.netbeans.swing.tabcontrol.plaf.WinXPViewTabDisplayerUI", null); 
            ConstellationLAFSettings.applyTabColorSettings();
            UIManager.getDefaults().put("org.netbeans.swing.tabcontrol.plaf.Windows8VectorViewTabDisplayerUI", pseudoTabDisplayerUIclass);
            ConstellationLAFSettings.applyTabColorSettings();

            LOGGER.info("********************************************");
            
            pseudoLAF.setName("FlatLafLight");
            UIManager.getDefaults().put("org.netbeans.swing.laf.flatlaf.ui.FlatViewTabDisplayerUI", null);
            ConstellationLAFSettings.applyTabColorSettings();
            
            LOGGER.info("********************************************");
            
            pseudoLAF.setName("Metal");
            UIManager.getDefaults().put("org.netbeans.swing.tabcontrol.plaf.MetalViewTabDisplayerUI", null);
            ConstellationLAFSettings.applyTabColorSettings();
            
            LOGGER.info("*******************************************");
            
            new ConstellationLAFSettings.NimbusCustomGradientTabPainter();
            new ConstellationLAFSettings.NimbusCustomGradientTabPainter(null);
            new ConstellationLAFSettings.NimbusCustomGradientTabPainter(Color.ORANGE);
            new ConstellationLAFSettings.NimbusCustomGradientTabPainter(null, null);
            new ConstellationLAFSettings.NimbusCustomGradientTabPainter(null, Color.ORANGE);
            new ConstellationLAFSettings.NimbusCustomGradientTabPainter(Color.ORANGE, null);            
            
            LOGGER.info("\nTESTING ConstellationLAFSettings : COMPLETE");
            
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
