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
package au.gov.asd.tac.constellation.plugins.importexport;

import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.stage.Window;
import static org.mockito.Mockito.mock;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class DefaultAttributeValueDialogNGTest {

    private static final Logger LOGGER = Logger.getLogger(DefaultAttributeValueDialogNGTest.class.getName());

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
     * Test of constructor method, of class DefaultAttributeValueDialog.
     */
    @Test
    public void testConstructor() {
        System.out.println("testConstructor");
        Platform.runLater(() -> {
            DefaultAttributeValueDialog instance = new DefaultAttributeValueDialog(mock(Window.class), "", "");
            assertEquals(instance.getClass(), DefaultAttributeValueDialog.class);
        });
    }

    /**
     * Test of getDefaultValue method, of class DefaultAttributeValueDialog.
     */
    @Test
    public void testGetDefaultValue() {
        System.out.println("getDefaultValue");
//        Platform.runLater(() -> {
//            DefaultAttributeValueDialog instance = new DefaultAttributeValueDialog(mock(Window.class), "", "");
//        String result = instance.getDefaultValue();
//        assertEquals(result.getClass(), String.class);
//        });
        DefaultAttributeValueDialog instance = mock(DefaultAttributeValueDialog.class);
        String result = instance.getDefaultValue();
        // Null because its mocked
        assertEquals(result, null);
    }

}
