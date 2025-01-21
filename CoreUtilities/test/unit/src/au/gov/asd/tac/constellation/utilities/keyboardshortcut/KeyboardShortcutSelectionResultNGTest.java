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

import au.gov.asd.tac.constellation.utilities.genericjsonio.JsonIODialog;
import java.io.File;
import java.nio.file.Files;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import static org.testfx.util.NodeQueryUtils.hasText;
import org.testfx.util.WaitForAsyncUtils;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author spica
 */
public class KeyboardShortcutSelectionResultNGTest {

    private static final Logger LOGGER = Logger.getLogger(KeyboardShortcutSelectionResultNGTest.class.getName());

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
    public void testKeyboardShortcutSelectionResult() {

        Optional<String> ks = Optional.of("ctrl 1");
        final File preferenceDirectory = new File(System.getProperty("java.io.tmpdir") + "/my-preferences.json");

        final Future<Optional<KeyboardShortcutSelectionResult>> future = WaitForAsyncUtils.asyncFx(
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
                        .lookup(hasText("OK"))
                        .queryAs(Button.class)
        );

        final Optional<KeyboardShortcutSelectionResult> result = WaitForAsyncUtils.waitFor(future);
        assertEquals(result.get().getKeyboardShortcut(), ks.get());
        assertTrue(result.get().getExisitngTemplateWithKs() == null);
        assertEquals(input, result.get().getFileName());
    }

    @Test
    public void testKeyboardShortcutSelectionResult_existingTemplate() throws Exception {

        final File outputFile = new File(System.getProperty("java.io.tmpdir") + "/my-preferences.json");

        try {
            outputFile.createNewFile();

            final String keyboardShortcut = "ctrl 1";

            final KeyboardShortcutSelectionResult ksResult = new KeyboardShortcutSelectionResult(keyboardShortcut, true, outputFile);

            final RecordKeyboardShortcut rks = mock(RecordKeyboardShortcut.class);
            Mockito.when(rks.start(outputFile)).thenReturn(Optional.of(ksResult));

            Optional<KeyboardShortcutSelectionResult> actualResult = rks.start(outputFile);

            assertEquals(ksResult, actualResult.get());
            assertTrue(actualResult.get().isAlreadyAssigned());
            assertTrue(actualResult.get().getExisitngTemplateWithKs() != null);
            
            ksResult.setAlreadyAssigned(false);
            ksResult.setExisitngTemplateWithKs(null);
            
            Mockito.when(rks.start(outputFile)).thenReturn(Optional.of(ksResult));
            actualResult = rks.start(outputFile);
            
            assertTrue(!actualResult.get().isAlreadyAssigned());
            assertTrue(actualResult.get().getExisitngTemplateWithKs() == null);

        } finally {
            Files.deleteIfExists(outputFile.toPath());
        }
    }

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
