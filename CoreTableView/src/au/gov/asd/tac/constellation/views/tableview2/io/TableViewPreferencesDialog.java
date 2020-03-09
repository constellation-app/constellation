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

import au.gov.asd.tac.constellation.visual.DraggableCell;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.TilePane;

/**
 * Displays a list of saved table preferences 
 * 
 * @author formalhaut69
 */
public class TableViewPreferencesDialog {

    public static String getTableViewPreferences(final String[] names) {
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
//        ButtonType removeButton = new ButtonType("Remove");
        dialog.getDialogPane().setContent(nameList);
//        dialog.getButtonTypes().add(removeButton);
        dialog.setResizable(false);
        dialog.setTitle("Table View Preferences");
        dialog.setHeaderText("Select a preference to load.");
//        final Button btOk = (Button) dialog.getDialogPane().lookupButton(removeButton);
//        btOk.addEventFilter(ActionEvent.ACTION, event -> {
//          event.consume();
//        });  
        final Optional<ButtonType> option = dialog.showAndWait();
        
//        if (option.isPresent() && option.get() == removeButton) {
//            System.out.println("NameList remove item: " + nameList.getItems());
//            nameList.getItems().remove(nameList.getSelectionModel().getSelectedItem());
//        } else 
        if(option.isPresent() && option.get() == ButtonType.OK){
            return nameList.getSelectionModel().getSelectedItem();
        }
        
        return null;
    }
    
    
    // opens up a slightly different dialog window to allow the user to name the
    // preference when it is being saved
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
