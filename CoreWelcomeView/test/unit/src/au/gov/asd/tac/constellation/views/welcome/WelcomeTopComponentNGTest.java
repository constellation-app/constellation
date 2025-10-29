/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.welcome;

import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.layout.HBox;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for WelcomeTopComponent
 * 
 * @author Delphinus8821
 */
public class WelcomeTopComponentNGTest {

    private static final Logger LOGGER = Logger.getLogger(WelcomeTopComponentNGTest.class.getName());

    @BeforeClass
    public static void setUpClass() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timed out trying to cleanup stages", ex);
        }
    }

    /**
     * Test of createStyle method, of class WelcomeTopComponent.
     */
    @Test
    public void testCreateStyle() {
        System.out.println("createStyle");
        final WelcomeTopComponent instance = mock(WelcomeTopComponent.class);
        doCallRealMethod().when(instance).createStyle();

        String expResult = "resources/light_welcome.css";

        assertEquals(instance.createStyle(), expResult);
    }
    
    /**
     * Test of createContent method, of class WelcomeTopComponent.
     */
    @Test
    public void testCreateContent() {
        System.out.println("createContent");
        final WelcomeTopComponent instance = spy(WelcomeTopComponent.class);
        WelcomeViewPane pane = instance.createContent();
        assertNotNull(pane);
        assertTrue(pane.getBottomRecentSection() instanceof HBox);
    }
}
