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

import au.gov.asd.tac.constellation.utilities.file.FilenameEncoder;
import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import java.awt.EventQueue;
import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCombination.Modifier;
import javafx.scene.input.KeyEvent;
import javax.swing.JFrame;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.openide.windows.WindowManager;

/**
 *
 * @author spica
 */
public class RecordKeyboardShortcut  {

    private static final Logger LOGGER = Logger.getLogger(RecordKeyboardShortcut.class.getName());
    
    private static final String KEYBOARD_SHORTCUT_DIALOG_TITLE = "Keyboard Shortcut";
    private static final String KEYBOARD_SHORTCUT_DIALOG_HEADER_TEXT = "Press keyboard shortcut for template";
    
    public static final String KEYBOARD_SHORTCUT_EXISTS_ALERT_ERROR_MSG_FORMAT
            = "'%s' will be re-assigned to this template";
     
    public static final String KEYBOARD_SHORTCUT = "KEYBOARD_SHORTCUT";
    public static final String ALREADY_ASSIGNED = "ALREADY_ASSIGNED";
    public static final String YES = "Yes";
    public static final String NO = "No";
    
    private static double mainframeX = 0;
    private static double mainframeY = 0;
    private static double mainframeWidth = 1024;
    private static double mainframeHeight = 768;

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
        getMainframePosition();
        final double xOffset = mainframeWidth / 2 - 40;
        final double yOffset = mainframeHeight / 2 - 40;
        td.setX(mainframeX + xOffset);
        td.setY(mainframeY + yOffset);
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
        final List<Modifier> modifiers = new ArrayList<Modifier>();
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

    /**
     * Get a reference to the main application window so that popup dialogs can be centred against it
     */
    private void getMainframePosition() {
        if (Boolean.TRUE.toString().equalsIgnoreCase(System.getProperty(AWT_HEADLESS_PROPERTY))) {
            return;
        }
        try {
            EventQueue.invokeAndWait(() -> {
                final JFrame mainframe = (JFrame) WindowManager.getDefault().getMainWindow();
                mainframeX = mainframe.getX();
                mainframeY = mainframe.getY();
                mainframeWidth = mainframe.getSize().getWidth();
                mainframeHeight = mainframe.getSize().getHeight();                
            });
        } catch (InterruptedException | InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE, "Error Displaying Dialog", ex);
        }        
    }
    
}
