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

import java.awt.Color;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.UIManager;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doAnswer;
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
    
    @Test
    public void runSetLAFTabColors() {
        try (final MockedStatic<WindowManager> windowManagerMockedStatic = Mockito.mockStatic(WindowManager.class)) {
            final WindowManager windowManager = mock(WindowManager.class);
            windowManagerMockedStatic.when(WindowManager::getDefault).thenReturn(windowManager);
            doAnswer(mockInvocation -> {
                final Runnable runnable = (Runnable) mockInvocation.getArgument(0);

                final JFrame frame = mock(JFrame.class);
                when(windowManager.getMainWindow()).thenReturn(frame);

                runnable.run();
                for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                    LOGGER.info("\n------ Found LAF: " + info.getName() + "\n");
                    if (info.getName().toUpperCase().contains("NIMBUS")) {
                        javax.swing.UIManager.setLookAndFeel(info.getClassName());
                        ConstellationLAFSettings.applyTabColorSettings();
                        final String painterName = UIManager.getLookAndFeelDefaults().get("TabbedPane:TabbedPaneTab[Enabled].backgroundPainter").getClass().getName();
                        Assert.assertTrue(painterName.contains("NimbusCustomGradientTabPainter"));
                    } else if (info.getName().toUpperCase().contains("FLATLAF")) {
                        javax.swing.UIManager.setLookAndFeel(info.getClassName());
                        final Class<?> tabDisplayer = (Class<?>) UIManager.getDefaults()
                                .get("org.netbeans.swing.laf.flatlaf.ui.FlatViewTabDisplayerUI");
                        if (tabDisplayer != null) {
                            ConstellationLAFSettings.applyTabColorSettings();
                            final String underlineHeight = (String) UIManager.get("ViewTab.underlineHeight");
                            Assert.assertEquals(underlineHeight, "5");
                        } else {
                            ConstellationLAFSettings.ouputUIDefaultValues(null, null);
                        }
                    } else if (info.getName().toUpperCase().contains("METAL")) {
                        javax.swing.UIManager.setLookAndFeel(info.getClassName());
                        final Class<?> tabDisplayer = (Class<?>) UIManager.getDefaults()
                            .get("org.netbeans.swing.tabcontrol.plaf.MetalViewTabDisplayerUI");            
                        if (tabDisplayer != null) {
                            ConstellationLAFSettings.applyTabColorSettings();
                            final Color selectedActivatedBackground = (Color) UIManager.get("TabRenderer.selectedActivatedBackground");
                            final Color expectedDarkBackground = new Color(45, 95, 180);
                            final Color expectedLightBackground = new Color(130, 170, 255);
                            if (info.getName().toUpperCase().contains("DARK")) {
                                Assert.assertTrue(selectedActivatedBackground.equals(expectedDarkBackground));
                            } else {
                                Assert.assertTrue(selectedActivatedBackground.equals(expectedLightBackground));
                            }
                        } else {
                            ConstellationLAFSettings.ouputUIDefaultValues(null, null);
                        }
                    } else if (info.getName().toUpperCase().contains("WINDOWS")) {
                        javax.swing.UIManager.setLookAndFeel(info.getClassName());
                        Class<?> displayerClass = (Class<?>) UIManager.getDefaults()
                            .get("org.netbeans.swing.tabcontrol.plaf.Windows8VectorViewTabDisplayerUI");
                        if (displayerClass == null) {
                            displayerClass = (Class<?>) UIManager.getDefaults()
                                .get("org.netbeans.swing.tabcontrol.plaf.WinXPViewTabDisplayerUI");
                            if (displayerClass != null) {
                                // check XP settings
                                ConstellationLAFSettings.applyTabColorSettings();
                                final Color selectionIndicator = (Color) UIManager.get("TabbedPane.selectionIndicator");
                                final Color expectedSelectionIndicator = new Color(180, 220, 255);                                                                
                                Assert.assertTrue(selectionIndicator.equals(expectedSelectionIndicator));                                                              
                            } else {
                               ConstellationLAFSettings.ouputUIDefaultValues(null, null);
                            }
                        } else {
                            // check Win8+ settings
                            ConstellationLAFSettings.applyTabColorSettings();
                            final Color selectedBackgroundFiller = (Color) UIManager.get("tab_sel_fill");
                            final Color expectedBackgroundFiller = new Color(160, 205, 255);                                                                
                            Assert.assertTrue(selectedBackgroundFiller.equals(expectedBackgroundFiller));                                                              
                        }
                    }

                }

                return null;
            }).when(windowManager).invokeWhenUIReady(any(Runnable.class));

            new Startup().run();
            
        } catch (Exception e) {
            LOGGER.info("\n******* EXCEPTION \n" + e.toString());
        }
    }

    
}
