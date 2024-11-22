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

import au.gov.asd.tac.constellation.utilities.gui.DraggableCell;
import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import au.gov.asd.tac.constellation.utilities.keyboardshortcut.KeyboardShortcutSelectionResult;
import au.gov.asd.tac.constellation.utilities.keyboardshortcut.TextInputDialogWithKeybordShortcut;
import java.io.File;
import java.util.List;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

/**
 * Displays a generic dialog window that can allow the user to select a file
 * name from a list of provided files.
 *
 * @author formalhaut69
 * @author serpens24
 */
public class JsonIODialog {

    private static final String REMOVE_BUTTON_TEXT = "Remove";

    private static final String PREFERENCE_SELECTION_DIALOG_TITLE = "Preferences";
    private static final String PREFERENCE_SELECTION_DIALOG_HEADER_TEXT = "Select a preference to load.";

    private static final String PREFERENCE_NAME_DIALOG_TITLE = "Preference Name";
    private static final String PREFERENCE_NAME_DIALOG_HEADER_TEXT = "Enter a name for the preference";
    
    private JsonIODialog() {
    }

    /**
     * Present a dialog allowing the user to select an entry from a list of
     * available files. The user is also able to select one or more files from
     * that list and delete them before selecting one for the closure of the
     * dialog.
     *
     * @param names list of file names to choose from
     * @param loadDir the relative directory path from the user home preference
     * directory that the file names are located
     * @param filePrefix the prefix if any that was removed from the file names
     * @return the selected element text or null if nothing was selected
     */
    public static Optional<String> getSelection(final List<String> names, final Optional<String> loadDir, 
            final Optional<String> filePrefix) {
        final Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.getDialogPane().getStylesheets().addAll(JavafxStyleManager.getMainStyleSheet());

        final ObservableList<String> observableNamesList = FXCollections.observableArrayList(names);

        final ListView<String> nameList = new ListView<>(observableNamesList);
        nameList.setCellFactory(param -> new DraggableCell<>());
        nameList.setEditable(false);
        nameList.setOnMouseClicked(event -> {
            if (event.getClickCount() > 1) {
                dialog.setResult(ButtonType.OK);
            }
        });

        final ButtonType removeButtonType = new ButtonType(REMOVE_BUTTON_TEXT);

        dialog.getDialogPane().setContent(nameList);
        dialog.getButtonTypes().add(removeButtonType);
        dialog.setResizable(false);
        dialog.setTitle(PREFERENCE_SELECTION_DIALOG_TITLE);
        dialog.setHeaderText(PREFERENCE_SELECTION_DIALOG_HEADER_TEXT);

        // The remove button has been wrapped inside the removeButtonType, this
        // has been done because any ButtonTypes added to an alert window will
        // automatically close the window when pressed. Wrapping it in another button
        // can allow us to consume the closing event and keep the window open.
        final Button removeButton = (Button) dialog.getDialogPane().lookupButton(removeButtonType);

        // The remove button has been pressed, delete the selected file and
        // update the list by removing the selected file
        removeButton.addEventFilter(ActionEvent.ACTION, event -> {
            JsonIO.deleteJsonPreference(nameList.getSelectionModel().getSelectedItem(), loadDir, filePrefix);

            observableNamesList.remove(nameList.getSelectionModel().getSelectedItem());
            nameList.setCellFactory(param -> new DraggableCell<>());

            dialog.getDialogPane().setContent(nameList);

            event.consume();
        });

        final Optional<ButtonType> option = dialog.showAndWait();
        if (option.isPresent() && option.get() == ButtonType.OK) {
            return Optional.ofNullable(nameList.getSelectionModel().getSelectedItem());
        }

        return Optional.empty();
    }

    /**
     * Displays a small window allowing the user to enter a name for the new
     * preference file.
     *
     * @return an optional name for the new file, empty if the dialog was closed
     * without entering a name for the file
     */
    public static Optional<String> getPreferenceFileName() {
        final TextInputDialog td = new TextInputDialog();
        td.setTitle(PREFERENCE_NAME_DIALOG_TITLE);
        td.setHeaderText(PREFERENCE_NAME_DIALOG_HEADER_TEXT);
        td.getDialogPane().getStylesheets().addAll(JavafxStyleManager.getMainStyleSheet());

        return td.showAndWait();
    }
    
    public static Optional<KeyboardShortcutSelectionResult> getPreferenceFileName(final Optional<String> ks, final File preferenceDirectory) {
        final TextInputDialogWithKeybordShortcut td = new TextInputDialogWithKeybordShortcut(preferenceDirectory, ks);
        td.setTitle(PREFERENCE_NAME_DIALOG_TITLE);
        td.setHeaderText(PREFERENCE_NAME_DIALOG_HEADER_TEXT);
        td.getDialogPane().getStylesheets().addAll(JavafxStyleManager.getMainStyleSheet());
        td.showAndWait();        
        return Optional.ofNullable(td.getKeyboardShortcutSelectionResult());
    }

}
