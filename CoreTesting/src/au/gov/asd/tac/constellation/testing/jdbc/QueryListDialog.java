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
package au.gov.asd.tac.constellation.testing.jdbc;

import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;

/**
 * Display a list of labels and allow the user to select one.
 *
 * @author algol
 */
class QueryListDialog {

    static String getQueryName(final Object owner, final String[] labels) {
        final Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Saved JDBC parameters");

        final ObservableList<String> q = FXCollections.observableArrayList(labels);
        final ListView<String> labelList = new ListView<>(q);
        labelList.setEditable(false);
        labelList.setOnMouseClicked(event -> {
            if (event.getClickCount() > 1) {
                dialog.setResult(ButtonType.OK);
            }
        });

        dialog.setResizable(false);
        dialog.setHeaderText("Select a parameter set to load.");
        dialog.getDialogPane().setContent(labelList);
        final Optional<ButtonType> option = dialog.showAndWait();
        if (option.isPresent() && option.get() == ButtonType.OK) {
            return labelList.getSelectionModel().getSelectedItem();
        }

        return null;
    }
}
