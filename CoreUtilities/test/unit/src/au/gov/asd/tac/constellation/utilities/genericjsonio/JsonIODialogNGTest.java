/*
 * Copyright 2010-2021 Australian Signals Directorate
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

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import static org.testfx.util.NodeQueryUtils.hasText;
import org.testfx.util.WaitForAsyncUtils;
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
 * @author formalhaunt
 */
public class JsonIODialogNGTest {
    private final FxRobot robot = new FxRobot();
    
    public JsonIODialogNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        FxToolkit.registerPrimaryStage();
        FxToolkit.showStage();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        FxToolkit.hideStage();
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }
    
    @Test
    public void getSelection_ok_pressed() throws InterruptedException, ExecutionException {
        final List<String> names = List.of("myPreferenceFile", "theirPreferenceFile");
        
        final Future<Optional<String>> future = WaitForAsyncUtils.asyncFx(
                () -> JsonIODialog.getSelection(names, Optional.of(""), Optional.of("")));
        
        final Stage dialog = getDialog(robot);
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
    }
    
    @Test
    public void getPreferenceFileName_ok_pressed() throws InterruptedException {
        final Future<Optional<String>> future = WaitForAsyncUtils.asyncFx(
                () -> JsonIODialog.getPreferenceFileName());
        
        final Stage dialog = getDialog(robot);
        
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
    public void getPreferenceFileName_cancel_pressed() throws InterruptedException {
        final Future<Optional<String>> future = WaitForAsyncUtils.asyncFx(
                () -> JsonIODialog.getPreferenceFileName());
        
        final Stage dialog = getDialog(robot);
        
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
    
    private Stage getDialog(final FxRobot robot) throws InterruptedException {
        Stage dialog = null;
        while(dialog == null) {
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
