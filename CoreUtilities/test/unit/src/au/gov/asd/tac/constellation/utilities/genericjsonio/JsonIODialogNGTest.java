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

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.stage.Modality;
import javafx.stage.Window;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.util.WaitForAsyncUtils;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
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
    public void getSelection_ok_pressed() {
        
    }
    
    @Test
    public void getPreferenceFileName_ok_pressed() {
        final Future<Optional<String>> future = WaitForAsyncUtils.asyncFx(
                () -> JsonIODialog.getPreferenceFileName());
        
        final FxRobot robot = new FxRobot();
        
        final Window dialog = getDialog(robot);
        
        final String input = "myPreferenceFile";
        
        final Node textField = robot.rootNode(dialog.getScene()).lookup(".text-field");
        robot.clickOn(textField);
        robot.write(input);
        
        final Set<Node> buttons = robot.rootNode(dialog.getScene()).lookupAll(".button");
        
        final Optional<Button> okButton = findButton(buttons, "OK");
        
        robot.clickOn(okButton.get(), MouseButton.PRIMARY);
        
        final Optional<String> result = WaitForAsyncUtils.waitFor(future);
        
        assertEquals(input, result.get());
    }
    
    @Test
    public void getPreferenceFileName_cancel_pressed() {
        final Future<Optional<String>> future = WaitForAsyncUtils.asyncFx(
                () -> JsonIODialog.getPreferenceFileName());
        
        FxRobot robot = new FxRobot();
        
        final Window dialog = getDialog(robot);
        
        final Node textField = robot.rootNode(dialog.getScene()).lookup(".text-field");
        robot.clickOn(textField);
        robot.write("myPreferenceFile");
        
        final Set<Node> buttons = robot.rootNode(dialog.getScene()).lookupAll(".button");
        
        final Optional<Button> cancelButton = findButton(buttons, "Cancel");
        
        robot.clickOn(cancelButton.get(), MouseButton.PRIMARY);
        
        final Optional<String> result = WaitForAsyncUtils.waitFor(future);
        
        assertFalse(result.isPresent());
    }
    
    private Optional<Button> findButton(final Set<Node> buttons,
                                        final String expectedText) {
        return buttons.stream()
                .map(node -> (Button) node)
                .filter(button -> expectedText.equals(button.getText()))
                .findFirst();
    }
    
    private Window getDialog(final FxRobot robot) {
        Window dialog = null;
        while(dialog == null) {
            dialog = robot.robotContext().getWindowFinder().listWindows().stream()
                    .filter(window -> window instanceof javafx.stage.Stage)
                    .map(window -> (javafx.stage.Stage) window)
                    .filter(stage -> stage.getModality() == Modality.APPLICATION_MODAL)
                    .findFirst()
                    .orElse(null);
            
            Thread.yield();
        }
        
        return dialog;
    }
    
}
