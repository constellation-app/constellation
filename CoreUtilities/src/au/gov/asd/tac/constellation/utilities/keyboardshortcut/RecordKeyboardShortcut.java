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

import au.gov.asd.tac.constellation.utilities.SystemUtilities;
import au.gov.asd.tac.constellation.utilities.file.FilenameEncoder;
import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
public class RecordKeyboardShortcut  {

    private static final String KEYBOARD_SHORTCUT_DIALOG_TITLE = "Keyboard Shortcut";
    private static final String KEYBOARD_SHORTCUT_DIALOG_HEADER_TEXT = "Press keyboard shortcut for template";
    
    public static final String KEYBOARD_SHORTCUT_EXISTS_ALERT_ERROR_MSG_FORMAT
            = "'%s' will be re-assigned to this template";
     
    public static final String KEYBOARD_SHORTCUT = "KEYBOARD_SHORTCUT";
    public static final String ALREADY_ASSIGNED = "ALREADY_ASSIGNED";
    public static final String YES = "Yes";
    public static final String NO = "No";
    
    /**
     * This is the system property that is set to true in order to make the AWT
     * thread run in headless mode for tests, etc.
     */
    private static final String AWT_HEADLESS_PROPERTY = "java.awt.headless";

    public Optional<KeyboardShortcutSelectionResult> start(final File preferenceDirectory) {       
        
        final KeyPressLabelDialog td = new KeyPressLabelDialog();
        td.setTitle(KEYBOARD_SHORTCUT_DIALOG_TITLE);
        td.setHeaderText(KEYBOARD_SHORTCUT_DIALOG_HEADER_TEXT);
        td.getDialogPane().getStylesheets().addAll(JavafxStyleManager.getMainStyleSheet());
        final double xOffset = SystemUtilities.getMainframeWidth() / 2 - 40;
        final double yOffset = SystemUtilities.getMainframeHeight() / 2 - 40;
        td.setX(SystemUtilities.getMainframeXPos() + xOffset);
        td.setY(SystemUtilities.getMainframeYPos() + yOffset);
        td.showAndWait();
        
        final String keyboardShortcut = (td.getLabel().getText().replace('+', ' ') +" ").trim();      
        
        Optional<KeyboardShortcutSelectionResult> ksOptional = Optional.empty();
        
        if(StringUtils.isNotEmpty(keyboardShortcut)) {            
            boolean alreadyAssigned = false;
            
                        
            File exisitngTemplateWithKs = keyboardShortCutAlreadyAssigned(preferenceDirectory, keyboardShortcut);
            
            if(exisitngTemplateWithKs != null) {
                alreadyAssigned = true;
            }
            
            ksOptional = Optional.of(new KeyboardShortcutSelectionResult(keyboardShortcut, alreadyAssigned, exisitngTemplateWithKs));
        }
        
        
        return ksOptional;
    }   
    
    public static File keyboardShortCutAlreadyAssigned(final File preferenceDirectory, final String keyboardShortcut) {
        
        File exisitngTemplateWithKs = null;
        
        final FilenameFilter filenameFilter = (d, s) -> s.startsWith("[" + keyboardShortcut + "]");

        final String[] fileNames = preferenceDirectory.list(filenameFilter);

        if (!ArrayUtils.isEmpty(fileNames)) {
            exisitngTemplateWithKs = new File(
                    preferenceDirectory,
                    FilenameEncoder.encode(fileNames[0])
            );
        }
        
        return exisitngTemplateWithKs;
        
    }
    
    public static KeyCombination createCombo(final KeyEvent event) {
        final List<Modifier> modifiers = new ArrayList<>();
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
