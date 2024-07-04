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
package au.gov.asd.tac.constellation.plugins.gui;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.stage.Window;
import static org.mockito.Mockito.mock;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertThrows;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class PluginParametersDialogNGTest {
    
    private static final Logger LOGGER = Logger.getLogger(PluginParametersDialogNGTest.class.getName());
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        try {
            if (!FxToolkit.isFXApplicationThreadRunning()) {
                FxToolkit.registerPrimaryStage();
            }
        } catch (Exception e) {
            System.out.println("\n**** SETUP ERROR: " + e);
            throw e;
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timed out trying to cleanup stages", ex);
        } catch (Exception e) {
            if (e.toString().contains("HeadlessException")) {
                System.out.println("\n**** EXPECTED TEARDOWN ERROR: " + e.toString());
            } else {
                System.out.println("\n**** UN-EXPECTED TEARDOWN ERROR: " + e.toString());
                throw e;
            }
        }
    }

    /**
     * Test of constructor method, of class PluginParametersDialog.
     */
    @Test
    public void testConstructor() {
        System.out.println("testConstructorPluginParametersDialog");
        Platform.runLater(() -> {
            PluginParametersDialog instance = new PluginParametersDialog(mock(Window.class), "", new PluginParameters());
            assertEquals(instance.getClass(), PluginParametersDialog.class);
        });
    }
    
    /**
     * Test of constructor method, with null PluginParameters, of class PluginParametersDialog.
     */
    @Test
    public void testConstructorNullPluginParameters() throws IllegalArgumentException {
        System.out.println("testConstructorPluginParametersDialogNullPluginParameters");
        Platform.runLater(() -> {
            assertThrows(IllegalArgumentException.class, () -> new PluginParametersDialog(mock(Window.class), "", null));
        });
    }

    /**
     * Test of getResult method, of class PluginParametersDialog.
     */
    @Test
    public void testGetResult() {
        System.out.println("getResult");
        PluginParametersDialog instance = mock(PluginParametersDialog.class);
        assertNull(instance.getResult());
    }

}
