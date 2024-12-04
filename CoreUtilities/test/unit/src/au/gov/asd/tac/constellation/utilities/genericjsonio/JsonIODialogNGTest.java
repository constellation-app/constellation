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
package au.gov.asd.tac.constellation.utilities.genericjsonio;

import au.gov.asd.tac.constellation.utilities.keyboardshortcut.KeyboardShortcutSelectionResult;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import static org.testfx.util.NodeQueryUtils.hasText;
import org.testfx.util.WaitForAsyncUtils;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class JsonIODialogNGTest {

    private static final Logger LOGGER = Logger.getLogger(JsonIODialogNGTest.class.getName());

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
    public void getSelection_ok_pressed() throws InterruptedException, ExecutionException {
        final List<String> names = List.of("myPreferenceFile", "theirPreferenceFile");

        final Future<Optional<String>> future = WaitForAsyncUtils.asyncFx(
                () -> JsonIODialog.getSelection(names, Optional.of(""), Optional.of("")));

        final Stage dialog = getDialog(robot);
        dialog.setX(0);
        dialog.setY(0);
        WaitForAsyncUtils.asyncFx(() -> dialog.requestFocus()).get();

        robot.clickOn(robot.from(dialog.getScene().getRoot())
                .lookup(".list-cell")
                .lookup(hasText("myPreferenceFile"))
                .queryAs(ListCell.class));

        robot.clickOn(robot.from(dialog.getScene().getRoot())
                .lookup(".button")
                .lookup(hasText("OK"))
                .queryAs(Button.class));

        final Optional<String> result = WaitForAsyncUtils.waitFor(future);

        assertTrue(result.isPresent());
        assertEquals(result.get(), "myPreferenceFile");
    }

    @Test
    public void getSelection_cancel_pressed() throws InterruptedException, ExecutionException {
        final List<String> names = List.of("myPreferenceFile", "theirPreferenceFile");

        final Future<Optional<String>> future = WaitForAsyncUtils.asyncFx(
                () -> JsonIODialog.getSelection(names, Optional.of(""), Optional.of("")));

        final Stage dialog = getDialog(robot);
        dialog.setX(0);
        dialog.setY(0);

        WaitForAsyncUtils.asyncFx(() -> dialog.requestFocus()).get();

        robot.clickOn(robot.from(dialog.getScene().getRoot())
                .lookup(".list-cell")
                .lookup(hasText("myPreferenceFile"))
                .queryAs(ListCell.class));

        robot.clickOn(robot.from(dialog.getScene().getRoot())
                .lookup(".button")
                .lookup(hasText("Cancel"))
                .queryAs(Button.class));

        final Optional<String> result = WaitForAsyncUtils.waitFor(future);

        assertFalse(result.isPresent());
    }

    @Test
    public void getSelection_remove_pressed() throws InterruptedException, ExecutionException {
        final List<String> names = List.of("myPreferenceFile", "theirPreferenceFile");

        final Future<Optional<String>> future = WaitForAsyncUtils.asyncFx(() -> {

            // The JsonIO Mock happens here because the static mocking needs to be on
            // the same thread as the execution
            try (MockedStatic<JsonIO> jsonIOMockedStatic = Mockito.mockStatic(JsonIO.class)) {

                final Optional<String> result = JsonIODialog.getSelection(
                        names, Optional.of("loadDir"), Optional.of("filePrefix"));

                // Verify the call to delete
                jsonIOMockedStatic.verify(() -> JsonIO
                        .deleteJsonPreference("theirPreferenceFile", Optional.of("loadDir"),
                                Optional.of("filePrefix")));

                return result;
            }
        });

        final Stage dialog = getDialog(robot);
        dialog.setX(0);
        dialog.setY(0);

        // IMPORTANT. Request focus. Until this is done the JavaFX scene in the
        // dialog does not appear to initialize and the following robot lookup
        // code will fail
        WaitForAsyncUtils.asyncFx(() -> dialog.requestFocus()).get();

        // Select a row and delete it
        robot.clickOn(robot.from(dialog.getScene().getRoot())
                .lookup(".list-cell")
                .lookup(hasText("theirPreferenceFile"))
                .queryAs(ListCell.class));

        robot.clickOn(robot.from(dialog.getScene().getRoot())
                .lookup(".button")
                .lookup(hasText("Remove"))
                .queryAs(Button.class));

        // Pull out the list view and verify its current list state. There should
        // only be one item now.
        final ListView listView = robot.from(dialog.getScene().getRoot())
                .lookup(".list-view")
                .queryAs(ListView.class);

        // Cancel the dialog so it closes
        robot.clickOn(robot.from(dialog.getScene().getRoot())
                .lookup(".button")
                .lookup(hasText("Cancel"))
                .queryAs(Button.class));

        final Optional<String> result = WaitForAsyncUtils.waitFor(future);

        assertEquals(listView.getItems(), FXCollections.observableArrayList("myPreferenceFile"));
        assertFalse(result.isPresent());
    }

    @Test
    public void getPreferenceFileName_ok_pressed() {
        final Future<Optional<String>> future = WaitForAsyncUtils.asyncFx(
                () -> JsonIODialog.getPreferenceFileName());

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

        final Optional<String> result = WaitForAsyncUtils.waitFor(future);

        assertEquals(input, result.get());
    }

    @Test
    public void getPreferenceFileName_cancel_pressed() {
        final Future<Optional<String>> future = WaitForAsyncUtils.asyncFx(
                () -> JsonIODialog.getPreferenceFileName());

        final Stage dialog = getDialog(robot);
        dialog.setX(0);
        dialog.setY(0);

        robot.clickOn(
                robot.from(dialog.getScene().getRoot())
                        .lookup(".text-field")
                        .queryAs(TextField.class)
        ).write("myPreferenceFile");

        robot.clickOn(
                robot.from(dialog.getScene().getRoot())
                        .lookup(".button")
                        .lookup(hasText("Cancel"))
                        .queryAs(Button.class)
        );

        final Optional<String> result = WaitForAsyncUtils.waitFor(future);

        assertFalse(result.isPresent());
    }

   
    @Test
    public void getPreferenceFileNameWithKs_ok_pressed() {
        
        final Optional<String> ks = Optional.of("[ctrl 1]");         
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
    public void getPreferenceFileNameWithKs_cancel_pressed() {        
        
        final Optional<String> ks = Optional.of("[ctrl 1]");         
        final File preferenceDirectory = new File(System.getProperty("java.io.tmpdir") + "/my-preferences.json");
         
        final Future<Optional<KeyboardShortcutSelectionResult>> future = WaitForAsyncUtils.asyncFx(
                () -> JsonIODialog.getPreferenceFileName(ks, preferenceDirectory));

        final Stage dialog = getDialog(robot);
        dialog.setX(0);
        dialog.setY(0);

        robot.clickOn(
                robot.from(dialog.getScene().getRoot())
                        .lookup(".text-field")
                        .queryAs(TextField.class)
        ).write("myPreferenceFile");

        robot.clickOn(
                robot.from(dialog.getScene().getRoot())
                        .lookup(".button")
                        .lookup(hasText("Cancel"))
                        .queryAs(Button.class)
        );

        final Optional<KeyboardShortcutSelectionResult> result = WaitForAsyncUtils.waitFor(future);

        assertTrue(result.isPresent());
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
