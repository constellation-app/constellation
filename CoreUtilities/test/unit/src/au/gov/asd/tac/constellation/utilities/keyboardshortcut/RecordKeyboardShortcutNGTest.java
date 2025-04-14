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
package au.gov.asd.tac.constellation.utilities.keyboardshortcut;

import java.io.File;
import java.nio.file.Files;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import org.apache.commons.lang3.StringUtils;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author spica
 */
public class RecordKeyboardShortcutNGTest {

    private static final Logger LOGGER = Logger.getLogger(RecordKeyboardShortcutNGTest.class.getName());


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
        } catch (final TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timed out trying to cleanup stages", ex);
        }
    }

    @Test
    public void test_recordKeyboardShortcut() throws Exception {

        final File outputFile = new File(System.getProperty("java.io.tmpdir") + "/my-preferences.json");

        try {

            outputFile.createNewFile();

            KeyPressLabelDialog keyPressLabelDialog = mock(KeyPressLabelDialog.class);
            when(keyPressLabelDialog.getDefaultValue()).thenReturn(StringUtils.EMPTY);
            when(keyPressLabelDialog.getLabel()).thenReturn(createContentLabel("ctrl+1"));
            when(keyPressLabelDialog.getResult()).thenReturn("ctrl 1");

            final DialogPane dialogPane = mock(DialogPane.class);
            when(dialogPane.getStylesheets()).thenReturn(mock(ObservableList.class));
            when(keyPressLabelDialog.getDialogPane()).thenReturn(dialogPane);

            keyPressLabelDialog.setResultConverter(dialogButton -> {
                String result = "ctrl 1";
                return result;
            });

            final RecordKeyboardShortcut rk = new RecordKeyboardShortcut(keyPressLabelDialog);
            final Optional<KeyboardShortcutSelectionResult> ksResult = rk.start(outputFile);
            assertTrue(ksResult.isPresent());            

        } finally {
            Files.deleteIfExists(outputFile.toPath());
        }

    }

    @Test
    public void test_recordKeyboardShortcut_alreadyassigned() throws Exception {

        final File preferenceFileDirectory = new File(System.getProperty("java.io.tmpdir"));
        final File outputFile = new File(System.getProperty("java.io.tmpdir") + "/[ctrl 1] my-preferences.json");

        try {
            outputFile.createNewFile();

            KeyPressLabelDialog keyPressLabelDialog = mock(KeyPressLabelDialog.class);
            when(keyPressLabelDialog.getDefaultValue()).thenReturn(StringUtils.EMPTY);
            when(keyPressLabelDialog.getLabel()).thenReturn(createContentLabel("ctrl+1"));
            when(keyPressLabelDialog.getResult()).thenReturn("ctrl+1");

            final DialogPane dialogPane = mock(DialogPane.class);
            when(dialogPane.getStylesheets()).thenReturn(mock(ObservableList.class));
            when(keyPressLabelDialog.getDialogPane()).thenReturn(dialogPane);

            keyPressLabelDialog.setResultConverter(dialogButton -> {
                String result = "ctrl+1";
                return result;
            });

            final RecordKeyboardShortcut rk = new RecordKeyboardShortcut(keyPressLabelDialog);
            final Optional<KeyboardShortcutSelectionResult> ksResult = rk.start(preferenceFileDirectory);
            assertTrue(ksResult.isPresent());

        } finally {
            Files.deleteIfExists(outputFile.toPath());
        }

    }

    @Test
    public void testCreateCombo() throws Exception {

        final KeyEvent keyEvent = new KeyEvent(KeyEvent.KEY_PRESSED, "Ctrl", "A", KeyCode.A, false, true, false, false);
        final KeyCombination keyCombination = RecordKeyboardShortcut.createCombo(keyEvent);
        assertTrue(keyCombination != null);
        assertTrue(keyCombination.getDisplayText().equals("Ctrl+A"));

        final KeyEvent keyEvent1 = new KeyEvent(KeyEvent.KEY_PRESSED, "Shift", "A", KeyCode.A, true, false, false, false);
        final KeyCombination keyCombination1 = RecordKeyboardShortcut.createCombo(keyEvent1);
        assertTrue(keyCombination1 != null);
        assertTrue(keyCombination1.getDisplayText().equals("Shift+A"));

        final KeyEvent keyEvent2 = new KeyEvent(KeyEvent.KEY_PRESSED, "Alt", "A", KeyCode.A, false, false, true, false);
        final KeyCombination keyCombination2 = RecordKeyboardShortcut.createCombo(keyEvent2);
        assertTrue(keyCombination2 != null);
        assertTrue(keyCombination2.getDisplayText().equals("Alt+A"));

        final KeyEvent keyEvent3 = new KeyEvent(KeyEvent.KEY_PRESSED, "Meta", "A", KeyCode.A, false, false, false, true);
        final KeyCombination keyCombination3 = RecordKeyboardShortcut.createCombo(keyEvent3);
        assertTrue(keyCombination3 != null);
        assertTrue(keyCombination3.getDisplayText().equals("Meta+A"));

    }

    private static Label createContentLabel(final String text) {
        final Label label = new Label(text);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        label.getStyleClass().add("content");
        label.setWrapText(true);
        label.setPrefWidth(360);
        return label;
    }
  
}
