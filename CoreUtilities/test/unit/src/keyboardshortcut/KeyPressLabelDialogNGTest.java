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
package keyboardshortcut;

/**
 *
 * @author spica
 */

import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import au.gov.asd.tac.constellation.utilities.keyboardshortcut.KeyPressLabelDialog;
import java.util.concurrent.ExecutionException;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.testfx.api.FxRobot;
import static org.testfx.matcher.control.LabeledMatchers.hasText;
import org.testfx.util.WaitForAsyncUtils;
import org.testng.annotations.Test;

public class KeyPressLabelDialogNGTest {
    
    private final FxRobot robot = new FxRobot();
    
    @Test
    public void testKeyPressLabel() throws InterruptedException, ExecutionException {
                
        String KEYBOARD_SHORTCUT_DIALOG_TITLE = "Keyboard Shortcut";
        String KEYBOARD_SHORTCUT_DIALOG_HEADER_TEXT = "Press keyboard shortcut for template";
    
        final KeyPressLabelDialog td = new KeyPressLabelDialog();
        td.setTitle(KEYBOARD_SHORTCUT_DIALOG_TITLE);
        td.setHeaderText(KEYBOARD_SHORTCUT_DIALOG_HEADER_TEXT);
        td.getDialogPane().getStylesheets().addAll(JavafxStyleManager.getMainStyleSheet());
        
        WaitForAsyncUtils.asyncFx(() -> td.showAndWait().get());
        
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
