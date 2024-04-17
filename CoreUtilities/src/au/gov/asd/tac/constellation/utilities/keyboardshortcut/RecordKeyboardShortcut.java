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

package au.gov.asd.tac.constellation.utilities.keyboardshortcut;

import au.gov.asd.tac.constellation.utilities.file.FilenameEncoder;
import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCombination.Modifier;
import javafx.scene.input.KeyEvent;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author spica
 */
public class RecordKeyboardShortcut extends Dialog<String>  {
    
    private static final String KEYBOARD_SHORTCUT_DIALOG_TITLE = "Keyboard Shortcut";
    private static final String KEYBOARD_SHORTCUT_DIALOG_HEADER_TEXT = "Press keyboard shortcut for template";
    
    private static final String KEYBOARD_SHORTCUT_EXISTS_ALERT_TITLE = "Keyboard shortcut already assigned to other template";
    private static final String KEYBOARD_SHORTCUT_EXISTS_ALERT_ERROR_MSG_FORMAT
            = "'%s' already assigned. Do you want to assign it to this template?";
    
    public Optional<String> start(File preferenceDirectory) {       
        
        KeyPressLabelDialog td = new KeyPressLabelDialog();
        td.setTitle(KEYBOARD_SHORTCUT_DIALOG_TITLE);
        td.setHeaderText(KEYBOARD_SHORTCUT_DIALOG_HEADER_TEXT);
        td.getDialogPane().getStylesheets().addAll(JavafxStyleManager.getMainStyleSheet());

        td.showAndWait();
        
        String keyboardShortcut = (td.getLabel().getText().replace('+', ' ') +" ").trim();      
        
        
        if(StringUtils.isNotEmpty(keyboardShortcut)) {
            boolean go = true;
            
            File exisitngTemplateWithKs = keyboardShortCutAlreadyAssigned(preferenceDirectory, keyboardShortcut);
            
            if(exisitngTemplateWithKs != null) {
                
                final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setHeaderText(KEYBOARD_SHORTCUT_EXISTS_ALERT_TITLE);
                alert.setContentText(String.format(
                        KEYBOARD_SHORTCUT_EXISTS_ALERT_ERROR_MSG_FORMAT,
                        keyboardShortcut
                ));

                final Optional<ButtonType> option = alert.showAndWait();
                go = option.isPresent() && option.get() == ButtonType.OK;
             
                if(go) {
                    String rename = exisitngTemplateWithKs.getName().replaceAll(keyboardShortcut, StringUtils.EMPTY);
                    exisitngTemplateWithKs.renameTo( new File(
                            preferenceDirectory,
                            FilenameEncoder.encode(rename.trim())
                    ));                    
                } else {
                    return start(preferenceDirectory);
                }
            }
        }
        
        
        return Optional.of(keyboardShortcut);
    }   
    
    public File keyboardShortCutAlreadyAssigned(File preferenceDirectory, String keyboardShortcut) {
        
        File exisitngTemplateWithKs = null;
        
        FilenameFilter filenameFilter = (d, s) -> {            
             return s.startsWith(keyboardShortcut);
        };
        
        String[] fileNames = preferenceDirectory.list(filenameFilter);
        
        if(!ArrayUtils.isEmpty(fileNames)) {
            exisitngTemplateWithKs = new File(
                            preferenceDirectory,
                            FilenameEncoder.encode(fileNames[0])
                    );
        }
        
        return exisitngTemplateWithKs;
        
    }
    
    public static KeyCombination createCombo(final KeyEvent event) {
        final List<Modifier> modifiers = new ArrayList();
        if (event.isControlDown()) {
            modifiers.add(KeyCombination.CONTROL_DOWN);
        }
        if (event.isMetaDown()) {
            modifiers.add(KeyCombination.META_DOWN);
        }
        if (event.isAltDown()) {
            modifiers.add(KeyCombination.ALT_DOWN);
        }
        if (event.isShiftDown()) {
            modifiers.add(KeyCombination.SHIFT_DOWN);
        }
        return new KeyCodeCombination(event.getCode(), modifiers.toArray(Modifier[]::new));
    }

    
}
