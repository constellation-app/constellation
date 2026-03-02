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
package au.gov.asd.tac.constellation.utilities.text;

import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class SpellCheckingTextAreaNGTest {

    private static final Logger LOGGER = Logger.getLogger(SpellCheckingTextAreaNGTest.class.getName());

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

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of setText method, of class SpellCheckingTextArea.
     */
    @Test
    public void testSetText() {
        System.out.println("setText");
        final String text = "This is the text";
        final SpellCheckingTextArea instance = new SpellCheckingTextArea(true);
        instance.setText(text);
        assertEquals(instance.getText(), text);
    }

    /**
     * Test of highlightText method, of class SpellCheckingTextArea.
     */
    @Test
    public void testHighlightText() {
        System.out.println("highlightText");
        final SpellCheckingTextArea instance = new SpellCheckingTextArea(true);
        final String testText = "test";
        int start = 0;
        int end = 1;

        instance.setText(testText);
        instance.highlightText(start, end);
        assertEquals(instance.getStyleAtPosition(0), SpellCheckingTextArea.UNDERLINE_AND_HIGHLIGHT_STYLE);
        assertEquals(instance.getStyleAtPosition(1), SpellCheckingTextArea.UNDERLINE_AND_HIGHLIGHT_STYLE);
    }

    /**
     * Test of highlightTextMultiple method, of class SpellCheckingTextArea.
     */
    @Test
    public void testHighlightTextMultiple() {
        System.out.println("highlightTextMultiple");
        final int[] starts = {0, 1};
        final int[] ends = {1, 2};
        final String testText = "test";
        final SpellCheckingTextArea instance = new SpellCheckingTextArea(true);

        instance.setText(testText);
        instance.highlightTextMultiple(starts, ends);

        for (int i = 0; i < 3; i++) {
            assertEquals(instance.getStyleAtPosition(i), SpellCheckingTextArea.UNDERLINE_AND_HIGHLIGHT_STYLE);
        }
    }

    /**
     * Test of clearStyles method, of class SpellCheckingTextArea.
     */
    @Test
    public void testClearStyles_0args() {
        System.out.println("clearStyles");
        final SpellCheckingTextArea instance = new SpellCheckingTextArea(true);
        final String testText = "test";

        instance.setText(testText);
        instance.highlightText(0, 1);
        assertEquals(instance.getStyleAtPosition(0), SpellCheckingTextArea.UNDERLINE_AND_HIGHLIGHT_STYLE);
        assertEquals(instance.getStyleAtPosition(1), SpellCheckingTextArea.UNDERLINE_AND_HIGHLIGHT_STYLE);

        instance.clearStyles();
        assertEquals(instance.getStyleAtPosition(0), SpellCheckingTextArea.CLEAR_STYLE);
        assertEquals(instance.getStyleAtPosition(1), SpellCheckingTextArea.CLEAR_STYLE);
    }

    /**
     * Test of clearStyles method, of class SpellCheckingTextArea.
     */
    @Test
    public void testClearStyles_int_int() {
        System.out.println("clearStyles");
        final int from = 0;
        final int to = 1;
        final String testText = "test";

        final SpellCheckingTextArea instance = new SpellCheckingTextArea(true);
        instance.setText(testText);
        instance.highlightText(0, 2);
        for (int i = 0; i < 3; i++) {
            assertEquals(instance.getStyleAtPosition(i), SpellCheckingTextArea.UNDERLINE_AND_HIGHLIGHT_STYLE);
        }

        instance.clearStyles(from, to);
        assertEquals(instance.getStyleAtPosition(0), SpellCheckingTextArea.CLEAR_STYLE);
        assertEquals(instance.getStyleAtPosition(1), SpellCheckingTextArea.CLEAR_STYLE);
        assertEquals(instance.getStyleAtPosition(2), SpellCheckingTextArea.UNDERLINE_AND_HIGHLIGHT_STYLE);
    }

    /**
     * Test of handleKeyReleased method, of class SpellCheckingTextArea.
     */
    @Test
    public void testHandleKeyReleased() {
        System.out.println("handleKeyReleased");
        final String text = "This is some text";

        try (final MockedConstruction<SpellChecker> mockSpellChecker = Mockito.mockConstruction(SpellChecker.class,
                (mock, context) -> {
                    when(mock.canCheckSpelling(text)).thenReturn(true);
                })) {

            final SpellCheckingTextArea instance = new SpellCheckingTextArea(true);
            instance.setText(text);
            instance.handleKeyReleased();

            final SpellChecker sc = mockSpellChecker.constructed().get(0);
            verify(sc).canCheckSpelling(text);
            verify(sc).checkSpelling();
        }
    }
}
