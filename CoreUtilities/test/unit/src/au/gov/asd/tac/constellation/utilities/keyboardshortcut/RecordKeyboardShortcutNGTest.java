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
import java.io.FilenameFilter;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.util.WaitForAsyncUtils;
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

    private final FxRobot robot = new FxRobot();

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

    @Test
    public void test_recordKeyboardShortcut() throws Exception {

        File outputFile = new File(System.getProperty("java.io.tmpdir") + "/my-preferences.json");

        try {

            outputFile.createNewFile();

            KeyPressLabelDialog keyPressLabelDialog = mock(KeyPressLabelDialog.class);
            when(keyPressLabelDialog.getDefaultValue()).thenReturn(StringUtils.EMPTY);
            when(keyPressLabelDialog.getLabel()).thenReturn(createContentLabel("ctrl+1"));
            when(keyPressLabelDialog.getResult()).thenReturn("ctrl 1");

            DialogPane dialogPane = mock(DialogPane.class);
            when(dialogPane.getStylesheets()).thenReturn(mock(ObservableList.class));
            when(keyPressLabelDialog.getDialogPane()).thenReturn(dialogPane);

            keyPressLabelDialog.setResultConverter(dialogButton -> {
                String result = "ctrl 1";
                return result;
            });

            RecordKeyboardShortcut rk = new RecordKeyboardShortcut(keyPressLabelDialog);
            Optional<KeyboardShortcutSelectionResult> ksResult = rk.start(outputFile);
            assertTrue(ksResult.isPresent());            

        } finally {
            Files.deleteIfExists(outputFile.toPath());
        }

    }

    @Test
    public void test_recordKeyboardShortcut_alreadyassigned() throws Exception {

        File preferenceFileDirectory = new File(System.getProperty("java.io.tmpdir"));
        final File outputFile = new File(System.getProperty("java.io.tmpdir") + "/[ctrl 1] my-preferences.json");

        try {
            outputFile.createNewFile();

            KeyPressLabelDialog keyPressLabelDialog = mock(KeyPressLabelDialog.class);
            when(keyPressLabelDialog.getDefaultValue()).thenReturn(StringUtils.EMPTY);
            when(keyPressLabelDialog.getLabel()).thenReturn(createContentLabel("ctrl+1"));
            when(keyPressLabelDialog.getResult()).thenReturn("ctrl+1");

            DialogPane dialogPane = mock(DialogPane.class);
            when(dialogPane.getStylesheets()).thenReturn(mock(ObservableList.class));
            when(keyPressLabelDialog.getDialogPane()).thenReturn(dialogPane);

            keyPressLabelDialog.setResultConverter(dialogButton -> {
                String result = "ctrl+1";
                return result;
            });

            RecordKeyboardShortcut rk = new RecordKeyboardShortcut(keyPressLabelDialog);
            Optional<KeyboardShortcutSelectionResult> ksResult = rk.start(preferenceFileDirectory);
            assertTrue(ksResult.isPresent());

        } finally {
            Files.deleteIfExists(outputFile.toPath());
        }

    }

    @Test
    public void testCreateCombo() throws Exception {

        KeyEvent keyEvent = new KeyEvent(KeyEvent.KEY_PRESSED, "Ctrl", "A", KeyCode.A, false, true, false, false);
        KeyCombination keyCombination = RecordKeyboardShortcut.createCombo(keyEvent);
        assertTrue(keyCombination != null);
        assertTrue(keyCombination.getDisplayText().equals("Ctrl+A"));

        KeyEvent keyEvent1 = new KeyEvent(KeyEvent.KEY_PRESSED, "Shift", "A", KeyCode.A, true, false, false, false);
        KeyCombination keyCombination1 = RecordKeyboardShortcut.createCombo(keyEvent1);
        assertTrue(keyCombination1 != null);
        assertTrue(keyCombination1.getDisplayText().equals("Shift+A"));

        KeyEvent keyEvent2 = new KeyEvent(KeyEvent.KEY_PRESSED, "Alt", "A", KeyCode.A, false, false, true, false);
        KeyCombination keyCombination2 = RecordKeyboardShortcut.createCombo(keyEvent2);
        assertTrue(keyCombination2 != null);
        assertTrue(keyCombination2.getDisplayText().equals("Alt+A"));

        KeyEvent keyEvent3 = new KeyEvent(KeyEvent.KEY_PRESSED, "Meta", "A", KeyCode.A, false, false, false, true);
        KeyCombination keyCombination3 = RecordKeyboardShortcut.createCombo(keyEvent3);
        assertTrue(keyCombination3 != null);
        assertTrue(keyCombination3.getDisplayText().equals("Meta+A"));

    }

    private static Label createContentLabel(final String text) {
        Label label = new Label(text);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        label.getStyleClass().add("content");
        label.setWrapText(true);
        label.setPrefWidth(360);
        return label;
    }

    /*  @Test
    public void test_keyboardShortCutAlreadyAssigned() throws Exception {

        final File outputFile = new File(System.getProperty("java.io.tmpdir") + "/my-preferences.json");

        try {
            outputFile.createNewFile();

            String keyboardShortcut = "ctrl 1";
            MockedStatic<RecordKeyboardShortcut> recordKeyboardShortcutDialogMockedStatic = Mockito.mockStatic(RecordKeyboardShortcut.class);

            setupStaticMocksForKeyboardShortCutAlreadyAssigned(recordKeyboardShortcutDialogMockedStatic, outputFile, keyboardShortcut);

            File file = RecordKeyboardShortcut.keyboardShortCutAlreadyAssigned(outputFile, keyboardShortcut);

            assertEquals(outputFile, file);

        } finally {
            Files.deleteIfExists(outputFile.toPath());
        }

    }

    private void setupStaticMocksForKeyboardShortCutAlreadyAssigned(final MockedStatic<RecordKeyboardShortcut> recordKeyboardShortcutMockedStatic,
            File outputFile, final String keyboardShortcut) {

        recordKeyboardShortcutMockedStatic.when(() -> RecordKeyboardShortcut.keyboardShortCutAlreadyAssigned(outputFile, keyboardShortcut))
                .thenReturn(outputFile);

    }*/
    /**
     * Get a dialog that has been displayed to the user. This will iterate
     * through all open windows and identify one that is modal. The assumption
     * is that there will only ever be one dialog open.
     * <p/>
     * If a dialog is not found then it will wait for the JavaFX thread queue to
     * empty and try again.
     *
     * @param robot the FX robot for these tests
     * @return the found dialog
     */
    private Stage getDialog(final FxRobot robot) {
        Stage dialog = null;
        while (dialog == null) {
            dialog = robot.robotContext().getWindowFinder().listWindows().stream()
                    .filter(window -> window instanceof javafx.stage.Stage)
                    .map(window -> (javafx.stage.Stage) window)
                    .filter(stage -> stage.getModality() == Modality.APPLICATION_MODAL)
                    .findFirst()
                    .orElse(null);

            if (dialog == null) {
                WaitForAsyncUtils.waitForFxEvents();
            }
        }
        return dialog;
    }

}
