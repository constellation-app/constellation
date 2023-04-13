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
package au.gov.asd.tac.constellation.views.notes;

import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Window;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author altair1673
 */
public class NewNotePaneNGTest {

    private static final Logger LOGGER = Logger.getLogger(NewNotePaneNGTest.class.getName());

    public NewNotePaneNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timed out trying to cleanup stages", ex);
        }
    }

    /**
     * Test of getTitleField method, of class NewNotePane.
     */
    @Test
    public void testGetTitleField() {
        System.out.println("getTitleField");
        final NewNotePane instance = new NewNotePane("#000000");
        assertEquals(instance.getTitleField() instanceof TextField, true);
    }

    /**
     * Test of getContentField method, of class NewNotePane.
     */
    @Test
    public void testGetContentField() {
        System.out.println("getContentField");
        final NewNotePane instance = new NewNotePane("#000000");
        assertEquals(instance.getContentField() instanceof TextArea, true);
    }

    /**
     * Test of getAddButtion method, of class NewNotePane.
     */
    @Test
    public void testGetAddButtion() {
        System.out.println("getAddButtion");
        final NewNotePane instance = new NewNotePane("#000000");

        assertEquals(instance.getAddButtion() instanceof Button, true);
    }

    /**
     * Test of getUserChosenColour method, of class NewNotePane.
     */
    @Test
    public void testGetUserChosenColour() {
        System.out.println("getUserChosenColour");
        final NewNotePane instance = new NewNotePane("#000000");
        final String expResult = "#000000";
        assertEquals(instance.getUserChosenColour(), expResult);
    }

    /**
     * Test of isApplySelected method, of class NewNotePane.
     */
    @Test
    public void testIsApplySelected() {
        System.out.println("isApplySelected");
        final NewNotePane instance = new NewNotePane("#000000");
        assertEquals(instance.isApplySelected(), true);
    }


    /**
     * Test of clearTextFields method, of class NewNotePane.
     */
    @Test
    public void testClearTextFields() {
        System.out.println("clearTextFields");
        final NewNotePane instance = new NewNotePane("#000000");

        instance.getTitleField().setText("Random Text");
        instance.getContentField().setText("Random text");

        assertEquals(!instance.getTitleField().getText().isBlank() && !instance.getContentField().getText().isBlank(), true);

        instance.clearTextFields();

        assertEquals(instance.getTitleField().getText().isBlank() && instance.getContentField().getText().isBlank(), true);

    }

}
