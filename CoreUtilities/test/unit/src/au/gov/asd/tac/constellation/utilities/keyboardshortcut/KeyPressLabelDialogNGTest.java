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

import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.util.WaitForAsyncUtils;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author spica
 */
public class KeyPressLabelDialogNGTest {

    private static final Logger LOGGER = Logger.getLogger(KeyPressLabelDialogNGTest.class.getName());
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
    public void testKeyPressLabelDialog() throws Exception {
        
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                 KeyPressLabelDialog kl = new KeyPressLabelDialog("test");
                 assertEquals(kl.getDefaultValue(), "test");
                 assertEquals(kl.getLabel().getText(), "test");
                 
                 
                 KeyPressLabelDialog k2 = new KeyPressLabelDialog();
                 assertEquals(kl.getDefaultValue(), "test");
                 assertEquals(kl.getLabel().getText(), "test");
            }
        });

        KeyPressLabelDialog keyPressLabelDialog = mock(KeyPressLabelDialog.class);
        when(keyPressLabelDialog.getDefaultValue()).thenReturn(StringUtils.EMPTY);
        when(keyPressLabelDialog.getLabel()).thenReturn(createContentLabel(StringUtils.EMPTY));
         when(keyPressLabelDialog.getResult()).thenReturn("ctrl 1");
        
        DialogPane dialogPane = mock(DialogPane.class);        
        when(keyPressLabelDialog.getDialogPane()).thenReturn(dialogPane);
        
        keyPressLabelDialog.setResultConverter(dialogButton -> {            
            String result = "ctrl 1";
            return result;
        });
        assertEquals(keyPressLabelDialog.getDefaultValue(), StringUtils.EMPTY);
        assertEquals(keyPressLabelDialog.getResult(), "ctrl 1");       
        
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