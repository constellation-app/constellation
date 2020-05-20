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
package au.gov.asd.tac.constellation.views.dataaccess.io;

import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;

/**
 * Display a list of query names and allow the user to select one.
 *
 * @author algol
 */
class QueryListDialog {

    static String getQueryName(final Object owner, final String[] queryNames) {
        final Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);

        final ObservableList<String> q = FXCollections.observableArrayList(queryNames);
        final ListView<String> nameList = new ListView<>(q);
        nameList.setCellFactory(p -> new DraggableCell<>());
        nameList.setEditable(false);
        nameList.setOnMouseClicked(event -> {
            if (event.getClickCount() > 1) {
                dialog.setResult(ButtonType.OK);
            }
        });

        dialog.setResizable(false);
        dialog.setTitle("Query names");
        dialog.setHeaderText("Select a query to load.");
        dialog.getDialogPane().setContent(nameList);
        final Optional<ButtonType> option = dialog.showAndWait();
        if (option.isPresent() && option.get() == ButtonType.OK) {
            return nameList.getSelectionModel().getSelectedItem();
        }

        return null;
    }
}
