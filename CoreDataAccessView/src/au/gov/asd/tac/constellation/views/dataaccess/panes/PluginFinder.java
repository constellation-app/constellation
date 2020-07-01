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
package au.gov.asd.tac.constellation.views.dataaccess.panes;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

/**
 * Present the user with a list of plugins and allow it to select one, then
 * expand that plugin.
 * <p>
 * This saves users from having to hunt through the various sections for a
 * plugin when they don't know where it is.
 *
 * @author algol
 */
public class PluginFinder {

    private String result;

    /**
     * Build a cooperative TextArea and ListView.
     * <p>
     * The TextArea acts as a filter on the ListView. If there is only one item
     * in the filtered list, it will be used when the user fires the OK action.
     *
     * @param dap
     * @param queryPhasePane
     */
    void find(final DataAccessPane dap, final QueryPhasePane queryPhasePane) {
        final ObservableList<String> texts = FXCollections.observableArrayList();

        queryPhasePane.getDataAccessPanes().stream().forEach(tp -> {
            texts.add(tp.getPlugin().getName());
        });

        Collections.sort(texts, (a, b) -> a.compareToIgnoreCase(b));

        final ListView<String> lv = new ListView<>();
        lv.setItems(texts);

        final Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Select a plugin");
        dialog.setHeaderText("Select a plugin");
        dialog.setResizable(true);

        final TextField tf = new TextField();
        tf.textProperty().addListener((ov, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                final String lower = newValue.toLowerCase();
                final List<String> ls = texts.stream().filter(a -> a.toLowerCase().contains(lower)).collect(Collectors.toList());
                final ObservableList<String> filtered = FXCollections.observableArrayList(ls);
                lv.setItems(filtered);
                if (filtered.size() == 1) {
                    lv.getSelectionModel().select(0);
                } else {
                    lv.getSelectionModel().clearSelection();
                }
            } else {
                lv.setItems(texts);
                lv.getSelectionModel().clearSelection();
            }

            final ObservableList<String> items = lv.getItems();
            result = items.size() == 1 ? items.get(0) : null;
        });

        final VBox root = new VBox();
        root.getChildren().addAll(tf, lv);
        dialog.getDialogPane().setContent(root);

        lv.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 1) {
                final ObservableList<String> items = lv.getSelectionModel().getSelectedItems();
                if (items.size() == 1) {
                    result = lv.getSelectionModel().getSelectedItem();
                } else {
                    result = null;
                }
            } else if (mouseEvent.getClickCount() == 2) {
                result = lv.getSelectionModel().getSelectedItem();
                dialog.setResult(ButtonType.OK);
            }
        });

        lv.setOnKeyPressed(event -> {
            final KeyCode c = event.getCode();
            if (c == KeyCode.ENTER) {
                final ObservableList<String> items = lv.getSelectionModel().getSelectedItems();
                if (items.size() == 1) {
                    result = lv.getSelectionModel().getSelectedItem();
                    dialog.setResult(ButtonType.OK);
                } else {
                    result = null;
                }
            }
        });

        final Optional<ButtonType> option = dialog.showAndWait();
        if (option.isPresent() && option.get() == ButtonType.OK && result != null) {
            queryPhasePane.expandPlugin(result);
        }
    }
}
