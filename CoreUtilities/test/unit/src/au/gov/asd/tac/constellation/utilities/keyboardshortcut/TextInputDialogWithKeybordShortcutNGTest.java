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
package au.gov.asd.tac.constellation.utilities.keyboardshortcut;

import au.gov.asd.tac.constellation.utilities.genericjsonio.JsonIODialog;
import java.io.File;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import static org.testfx.util.NodeQueryUtils.hasText;
import org.testfx.util.WaitForAsyncUtils;
import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author spica
 */
public class TextInputDialogWithKeybordShortcutNGTest {

    private static final Logger LOGGER = Logger.getLogger(TextInputDialogWithKeybordShortcutNGTest.class.getName());

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
        } catch (final TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timed out trying to cleanup stages", ex);
        }
    }

    @Test
    public void testKeyPressLabelDialog() throws Exception {

        final File outputFile = new File(System.getProperty("java.io.tmpdir"));

        outputFile.createNewFile();

        final TextInputDialogWithKeybordShortcut textInputDialogWithKeybordShortcut = mock(TextInputDialogWithKeybordShortcut.class);
        when(textInputDialogWithKeybordShortcut.getDefaultValue()).thenReturn(StringUtils.EMPTY);

        final DialogPane dialogPane = mock(DialogPane.class);
        when(textInputDialogWithKeybordShortcut.getDialogPane()).thenReturn(dialogPane);
        assertEquals(textInputDialogWithKeybordShortcut.getDefaultValue(), StringUtils.EMPTY);

        final Optional<KeyboardShortcutSelectionResult> ksResult = Optional.of(new KeyboardShortcutSelectionResult("Ctrl 1", false, null));
        final RecordKeyboardShortcut rk = mock(RecordKeyboardShortcut.class);
        when(rk.start(outputFile)).thenReturn(ksResult);

        final Optional<KeyboardShortcutSelectionResult> actualResponse = TextInputDialogWithKeybordShortcut.getKeyboardShortcut(outputFile, rk);

        assertEquals(actualResponse, ksResult);

    }

    @Test
    public void testTextInputDialogWithKeybordShortcut() throws Exception {
        final File preferenceDirectory = new File(System.getProperty("java.io.tmpdir"));
        final Optional<String> ks = Optional.of("Ctrl+1");

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                final TextInputDialogWithKeybordShortcut textInputDialogWithKeybordShortcut = new TextInputDialogWithKeybordShortcut(preferenceDirectory, ks);
                Assert.assertNotNull(textInputDialogWithKeybordShortcut.getEditor());
                assertEquals(textInputDialogWithKeybordShortcut.getDefaultValue(), "");
            }
        });

    }

    @Test
    public void testClickOnShortcutButton() throws Exception {
        final Optional<String> ks = Optional.of("ctrl 1");
        final File preferenceDirectory = new File(System.getProperty("java.io.tmpdir") + "/my-preferences.json");

        WaitForAsyncUtils.asyncFx(
                () -> JsonIODialog.getPreferenceFileName(ks, preferenceDirectory));

        final Stage dialog = getDialog(robot);
        dialog.setX(0);
        dialog.setY(0);

        final String input = "myPreferenceFile";

        robot.clickOn(
                robot.from(dialog.getScene().getRoot())
                        .lookup(".text-field")
                        .queryAs(TextField.class)
        ).write(input);

        robot.clickOn(
                robot.from(dialog.getScene().getRoot())
                        .lookup(".button")
                        .lookup(hasText("Shortcut"))
                        .queryAs(Button.class)
        );

        robot.clickOn(
                robot.from(dialog.getScene().getRoot())
                        .lookup(".button")
                        .lookup(hasText("Cancel"))
                        .queryAs(Button.class)
        );

        robot.clickOn(
                robot.from(dialog.getScene().getRoot())
                        .lookup(".button")
                        .lookup(hasText("OK"))
                        .queryAs(Button.class)
        );

    }

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
