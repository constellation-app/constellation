/*
 * Copyright 2010-2026 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.utilities.text.SpellCheckingTextArea;
import au.gov.asd.tac.constellation.views.notes.utilities.NewNotePane;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
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

    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
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
        assertTrue(instance.getTitleField() instanceof TextField);
    }

    /**
     * Test of getContentField method, of class NewNotePane.
     */
    @Test
    public void testGetContentField() {
        System.out.println("getContentField");
        final NewNotePane instance = new NewNotePane("#000000");
        assertTrue(instance.getContentField() instanceof SpellCheckingTextArea);
    }

    /**
     * Test of getAddButtion method, of class NewNotePane.
     */
    @Test
    public void testGetAddButtion() {
        System.out.println("getAddButtion");
        final NewNotePane instance = new NewNotePane("#000000");

        assertTrue(instance.getAddButtion() instanceof Button);
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
        assertTrue(instance.isApplySelected());
    }

    /**
     * Test of clearTextFields method, of class NewNotePane.
     */
    @Test
    public void testClearTextFields() {
        System.out.println("clearTextFields");

        try (final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class)) {
            // Make runLater run immediately
            platformMockedStatic.when(() -> Platform.runLater(any(Runnable.class))).thenAnswer(iom -> {
                ((Runnable) iom.getArgument(0)).run();
                return null;
            });
            final NewNotePane instance = new NewNotePane("#000000");

            instance.getTitleField().setText("Random Text");
            instance.getContentField().setText("Random text");

            assertFalse(instance.getTitleField().getText().isBlank());
            assertFalse(instance.getContentField().getText().isBlank());

            instance.clearTextFields();

            assertTrue(instance.getTitleField().getText().isBlank());
            assertTrue(instance.getContentField().getText().isBlank());
        }
    }
}
