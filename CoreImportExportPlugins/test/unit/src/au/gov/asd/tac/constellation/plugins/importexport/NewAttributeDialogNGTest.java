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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class NewAttributeDialogNGTest {

    private static final Logger LOGGER = Logger.getLogger(NewAttributeDialogNGTest.class.getName());

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
     * Test of getType method, of class NewAttributeDialog.
     */
    @Test
    public void testGetType() {
        System.out.println("getType");
        //Platform.runLater(() -> {
            System.setProperty("java.awt.headless", "true");
            NewAttributeDialog instance = new NewAttributeDialog();
            String result = instance.getType();
            assertEquals(result.getClass(), String.class);
            System.clearProperty("java.awt.headless");
        //});
    }

    /**
     * Test of getLabel method, of class NewAttributeDialog.
     */
    @Test
    public void testGetLabel() {
        System.out.println("getLabel");
        //Platform.runLater(() -> {
            System.setProperty("java.awt.headless", "true");
            NewAttributeDialog instance = new NewAttributeDialog();
            String result = instance.getLabel();
            assertEquals(result.getClass(), String.class);
            System.clearProperty("java.awt.headless");
        //});
    }

    /**
     * Test of getDescription method, of class NewAttributeDialog.
     */
    @Test
    public void testGetDescription() {
        System.out.println("getDescription");
        //Platform.runLater(() -> {
            System.setProperty("java.awt.headless", "true");
            NewAttributeDialog instance = new NewAttributeDialog();
            String result = instance.getDescription();
            assertEquals(result.getClass(), String.class);
            System.clearProperty("java.awt.headless");
        //});
    }

    /**
     * Test of setOkButtonAction method, of class NewAttributeDialog.
     */
    @Test
    public void testSetGetOkButtonAction() {
        System.out.println("setOkButtonAction");
        //Platform.runLater(() -> {
            System.setProperty("java.awt.headless", "true");
            EventHandler<ActionEvent> event = null;
            NewAttributeDialog instance = new NewAttributeDialog();

            instance.setOkButtonAction(event);
            assertEquals(instance.getOkButtonAction(), event);
            System.clearProperty("java.awt.headless");
        //});
    }

}
