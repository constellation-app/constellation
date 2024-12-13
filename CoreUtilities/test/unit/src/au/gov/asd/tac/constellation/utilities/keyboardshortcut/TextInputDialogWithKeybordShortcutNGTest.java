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
import javafx.application.Platform;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.apache.commons.lang3.StringUtils;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 *
 * @author spica
 */
public class TextInputDialogWithKeybordShortcutNGTest {

    @Test
    public void testKeyPressLabelDialog() throws Exception {

        File outputFile = new File(System.getProperty("java.io.tmpdir"));

        try {

            outputFile.createNewFile();
         
            TextInputDialogWithKeybordShortcut textInputDialogWithKeybordShortcut = mock(TextInputDialogWithKeybordShortcut.class);
            when(textInputDialogWithKeybordShortcut.getDefaultValue()).thenReturn(StringUtils.EMPTY);
           // when(textInputDialogWithKeybordShortcut.getEditor()).thenReturn(createTextField(StringUtils.EMPTY));            

            DialogPane dialogPane = mock(DialogPane.class);
            when(textInputDialogWithKeybordShortcut.getDialogPane()).thenReturn(dialogPane);
            assertEquals(textInputDialogWithKeybordShortcut.getDefaultValue(), StringUtils.EMPTY);
            
            Optional<KeyboardShortcutSelectionResult> ksResult = Optional.of(new KeyboardShortcutSelectionResult("Ctrl 1", false, null));
            RecordKeyboardShortcut rk = mock(RecordKeyboardShortcut.class);
            when(rk.start(outputFile)).thenReturn(ksResult);

            Optional<KeyboardShortcutSelectionResult> actualResponse = TextInputDialogWithKeybordShortcut.getKeyboardShortcut(outputFile, rk);
          
            assertEquals(actualResponse, ksResult);
            

        } finally {
            //  Files.deleteIfExists(outputFile.toPath());
        }
    }

    private static TextField createTextField(final String text) {
        TextField textField = mock(TextField.class);
        textField.setMaxWidth(Double.MAX_VALUE);
        textField.setMaxHeight(Double.MAX_VALUE);
        textField.getStyleClass().add("content");        
        textField.setPrefWidth(360);
        return textField;
    }

}
