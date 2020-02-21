/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.tableview2.io;

import au.gov.asd.tac.constellation.functionality.dialog.ConstellationDialog;
import au.gov.asd.tac.constellation.visual.DraggableCell;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.TilePane;

/**
 * Displays a list of saved table prefrences 
 * 
 * @author formalhaut69
 */
public class TableViewPreferencesDialog extends ConstellationDialog {

    private static Button okButton;
    private static Button cancelButton;
    private static Button defaultButton;
    private static final String DARK_THEME = "/au/gov/asd/tac/constellation/views/attributeeditor/resources/editor-dark.css";
    
    
    public static String getTableViewPreferences(String[] names) {
        final Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        
        final String[] preferenceNames = names;
        
        final ObservableList<String> q = FXCollections.observableArrayList(preferenceNames);
        final ListView<String> nameList = new ListView<>(q);
        nameList.setCellFactory(p -> new DraggableCell<>());
        nameList.setEditable(false);
        nameList.setOnMouseClicked(event -> {
            if (event.getClickCount() > 1) {
                dialog.setResult(ButtonType.OK);
            }
        });
        ButtonType removeButton = new ButtonType("Remove");
        
        dialog.getButtonTypes().add(removeButton);
        dialog.setResizable(false);
        dialog.setTitle("Table View Preferences");
        dialog.setHeaderText("Select a preference to load.");
        dialog.getDialogPane().setContent(nameList);
        final Optional<ButtonType> option = dialog.showAndWait();
        if (option.isPresent() && option.get() == ButtonType.OK) {
            return nameList.getSelectionModel().getSelectedItem();
        }

        return null;
    }
    
    
    //Opens up a slightly different dialog window to allow the user to name the
    //preference when it is being saved
    public static String getTableViewPreferenceName() {
        // create a tile pane 
        TilePane r = new TilePane(); 
        // create a text input dialog 
        TextInputDialog td = new TextInputDialog(); 
        td.setTitle("Table view preference name");
        // setHeaderText 
        td.setHeaderText("Enter a name for the table view preference"); 
        
        td.showAndWait();
        if(!td.getEditor().getText().equals("")){
            return td.getEditor().getText();
        }
        
        return "";
    
    }
    
}
