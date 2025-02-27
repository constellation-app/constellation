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
package au.gov.asd.tac.constellation.utilities.gui.field.framework;

import au.gov.asd.tac.constellation.utilities.gui.field.MultiChoiceInput;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Andromeda-224
 */
public class ConstellationTextAreaNGTest {

    private static final Logger LOGGER = Logger.getLogger(ConstellationTextAreaNGTest.class.getName());

    
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
            LOGGER.log(Level.WARNING, "FxToolkit timedout trying to cleanup stages", ex);
        }
    }

    @Test
    public void testConstellationTextArea_setPromptText() {

        final MultiChoiceInput multiChoiceInput = spy(new MultiChoiceInput());
        final ConstellationTextArea cTextArea = spy(new ConstellationTextArea(
                multiChoiceInput, ConstellationInputConstants.TextType.MULTILINE));

        assertTrue(cTextArea.getChildren().getFirst() instanceof TextArea);
        final String prompt_text_test = "Testing 1,2,3";

        cTextArea.setPromptText(prompt_text_test);
        final TextArea primaryInput = (TextArea) cTextArea.getChildren().getFirst();
        assertEquals(primaryInput.getPromptText(), prompt_text_test);
    }

    @Test
    public void testConstellationTextArea_setText() {

        final MultiChoiceInput multiChoiceInput = spy(new MultiChoiceInput());
        final ConstellationTextArea cTextArea = spy(new ConstellationTextArea(
                multiChoiceInput, ConstellationInputConstants.TextType.MULTILINE));

        assertTrue(cTextArea.getChildren().getFirst() instanceof TextArea);
        final String text_test = "Select choice";

        cTextArea.setText(text_test);
        final TextArea primaryInput = (TextArea) cTextArea.getChildren().getFirst();
        assertEquals(primaryInput.getText(), text_test);
        // setting to null does not change anything
        cTextArea.setText(null);
        assertEquals(primaryInput.getText(), text_test);
    }

    @Test
    public void testConstellationTextArea_setEditable() {

        final MultiChoiceInput multiChoiceInput = spy(new MultiChoiceInput());
        final ConstellationTextArea cTextArea = spy(new ConstellationTextArea(
                multiChoiceInput, ConstellationInputConstants.TextType.MULTILINE));

        assertTrue(cTextArea.getChildren().getFirst() instanceof TextArea);

        cTextArea.setEditable(false);
        final TextArea primaryInput = (TextArea) cTextArea.getChildren().getFirst();
        assertEquals(primaryInput.isEditable(), false);
        cTextArea.setEditable(true);
        assertEquals(primaryInput.isEditable(), true);
    }

    @Test
    public void testConstellationTextArea_setContextMenu() {

        final ContextMenu contextMenuMock = mock(ContextMenu.class);
        final MultiChoiceInput multiChoiceInput = spy(new MultiChoiceInput());
        final ConstellationTextArea cTextArea = spy(new ConstellationTextArea(
                multiChoiceInput, ConstellationInputConstants.TextType.MULTILINE));

        assertTrue(cTextArea.getChildren().getFirst() instanceof TextArea);

        cTextArea.setContextMenu(contextMenuMock);
        final TextArea primaryInput = (TextArea) cTextArea.getChildren().getFirst();
        assertEquals(primaryInput.getContextMenu(), contextMenuMock);
    }

    @Test
    public void testConstellationTextArea_setPreferredRowCounter() {

        final MultiChoiceInput multiChoiceInput = spy(new MultiChoiceInput());
        final ConstellationTextArea cTextArea = spy(new ConstellationTextArea(
                multiChoiceInput, ConstellationInputConstants.TextType.MULTILINE));

        assertTrue(cTextArea.getChildren().getFirst() instanceof TextArea);
        cTextArea.setPreferedRowCount(10);
        final TextArea primaryInput = (TextArea) cTextArea.getChildren().getFirst();
        assertEquals(primaryInput.getPrefRowCount(), 10);
    }

    @Test
    public void testConstellationTextArea_hideReveal() {

        final MultiChoiceInput multiChoiceInput = spy(new MultiChoiceInput());
        final ConstellationTextArea cTextArea = spy(new ConstellationTextArea(
                multiChoiceInput, ConstellationInputConstants.TextType.SECRET));

        assertTrue(cTextArea.getChildren().size() == 2);
        cTextArea.hide();
        assertEquals(cTextArea.getChildren().getFirst().isVisible(), false);
        assertEquals(cTextArea.getChildren().getLast().isVisible(), true);

        cTextArea.reveal();
        assertEquals(cTextArea.getChildren().getFirst().isVisible(), true);
        assertEquals(cTextArea.getChildren().getLast().isVisible(), false);

        // test when secondaryInput is null
        final ConstellationTextArea cTextArea2 = spy(new ConstellationTextArea(
                multiChoiceInput, ConstellationInputConstants.TextType.SINGLELINE));

        // expect unsupportedOperationException
        assertThrows(UnsupportedOperationException.class,
                () -> cTextArea2.hide());

        assertThrows(UnsupportedOperationException.class,
                () -> cTextArea2.reveal());
    }

    @Test
    public void testConstellationTextArea_secretType() {

        final MultiChoiceInput multiChoiceInput = spy(new MultiChoiceInput());
        final ConstellationTextArea cTextArea = spy(new ConstellationTextArea(
                multiChoiceInput, ConstellationInputConstants.TextType.SECRET));

        assertTrue(cTextArea.getChildren().size() == 2);
        assertTrue(cTextArea.getChildren().getFirst() instanceof TextField);
        assertTrue(cTextArea.getChildren().getLast() instanceof PasswordField);

        final TextField primaryInput = (TextField) cTextArea.getChildren().getFirst();
        assertEquals(primaryInput.isVisible(), false);
    }
}
