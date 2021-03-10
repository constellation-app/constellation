/*
 * Copyright 2010-2020 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.utilities.gui.DraggableCell;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;

/**
 * Displays a generic dialog window that can allow the user to select a Json
 * preference from a list
 *
 * @author formalhaut69
 * @author serpens24
 */
public class JsonIODialog {

    /**
     * Private constructor to hide implicit public one.
     */
    private JsonIODialog() {
        throw new IllegalStateException("Invalid call to private default constructor");
    }

    /**
     * *
     * Present a dialog allowing user to select an entry from a list of
     * available files.
     *
     * @param names list of filenames to choose from
     * @return the selected element text or null if nothing was selected
     */
    public static String getSelection(final String[] names) {
        final Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        final ObservableList<String> q = FXCollections.observableArrayList(names);
        final ListView<String> nameList = new ListView<>(q);

        nameList.setCellFactory(p -> new DraggableCell<>());
        nameList.setEditable(false);
        nameList.setOnMouseClicked(event -> {
            if (event.getClickCount() > 1) {
                dialog.setResult(ButtonType.OK);
            }
        });
        ButtonType removeButton = new ButtonType("Remove");
        dialog.getDialogPane().setContent(nameList);
        dialog.getButtonTypes().add(removeButton);
        dialog.setResizable(false);
        dialog.setTitle("Preferences");
        dialog.setHeaderText("Select a preference to load.");

        // The remove button has been wrapped inside the btOk, this has been done because any ButtonTypes added
        // to an alert window will automatically close the window when pressed. 
        // Wrapping it in another button can allow us to consume the closing event and keep the window open.
        final Button btOk = (Button) dialog.getDialogPane().lookupButton(removeButton);
        btOk.addEventFilter(ActionEvent.ACTION, event -> {
            JsonIO.deleteJsonPreference(nameList.getSelectionModel().getSelectedItem());
            q.remove(nameList.getSelectionModel().getSelectedItem());
            nameList.setCellFactory(p -> new DraggableCell<>());
            dialog.getDialogPane().setContent(nameList);
            event.consume();
        });
        final Optional<ButtonType> option = dialog.showAndWait();
        if (option.isPresent() && option.get() == ButtonType.OK) {
            return nameList.getSelectionModel().getSelectedItem();
        }

        return null;
    }

    /**
     * Displays a small window allowing the user to enter a name for the new
     * preference
     *
     * @author formalhaut69
     * @return A tuple, the first item is a Boolean indicating whether the user
     * selected to proceed with the operation or not, the second is the name of
     * the file requested by the user.
     */
    public static Tuple<Boolean, String> getName() {
        String returnedName = "";

        // opens up a slightly different dialog window to allow the user to name the
        // preference when it is being saved
        // create a text input dialog 
        TextInputDialog td = new TextInputDialog();
        td.setTitle("Preference name");
        // setHeaderText 
        td.setHeaderText("Enter a name for the preference");
        Optional<String> result = td.showAndWait();
        if (result.isPresent()) {
            returnedName = td.getEditor().getText();
        }
        return new Tuple<>(result.isPresent(), returnedName);
    }
}
