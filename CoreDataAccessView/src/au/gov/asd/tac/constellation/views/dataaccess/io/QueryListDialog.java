/*
 * Copyright 2010-2025 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import java.util.List;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;

/**
 * Display a list of query names and allow the user to select one.
 *
 * @author algol
 */
public final class QueryListDialog {
    private static final String QUERY_NAME_DIALOG_TITLE = "Query names";
    private static final String QUERY_NAME_DIALOG_HEADER = "Select a query to load.";

    /**
     * Private constructor to prevent initialization.
     */
    private QueryListDialog() {
    }
    
    /**
     * Displays a dialog listing the passed query names. They user can select one
     * query name and click OK. That selected query name is the value returned.
     * <p/>
     * This method will block until the user responds to the dialog.
     *
     * @param queryNames the list of query names to display
     * @return the selected item or null if no item is selected or cancel is selected
     */
    public static Optional<String> getQueryName(final List<String> queryNames) {
        final Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.getDialogPane().getStylesheets().addAll(JavafxStyleManager.getMainStyleSheet());

        final ListView<String> nameList = new ListView<>(
                FXCollections.observableArrayList(queryNames)
        );
        nameList.setCellFactory(p -> new DraggableCell<>());
        nameList.setEditable(false);
        nameList.setOnMouseClicked(event -> {
            if (event.getClickCount() > 1) {
                dialog.setResult(ButtonType.OK);
            }
        });

        dialog.setResizable(false);
        dialog.setTitle(QUERY_NAME_DIALOG_TITLE);
        dialog.setHeaderText(QUERY_NAME_DIALOG_HEADER);
        dialog.getDialogPane().setContent(nameList);
        
        final Optional<ButtonType> option = dialog.showAndWait();
        if (option.isPresent() && option.get() == ButtonType.OK) {
            return Optional.of(nameList.getSelectionModel().getSelectedItem());
        }

        return Optional.empty();
    }
}
