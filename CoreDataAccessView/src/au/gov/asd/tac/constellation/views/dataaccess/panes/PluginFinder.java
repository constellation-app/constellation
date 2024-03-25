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
package au.gov.asd.tac.constellation.views.dataaccess.panes;

import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;

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
    private static final String PLUGIN_FINDER_TITLE = "Select a plugin";
    private static final String PLUGIN_FINDER_HEADER = "Select a plugin";
    
    private String result;

    /**
     * Build a cooperative TextArea and ListView.
     * <p>
     * The TextArea acts as a filter on the ListView. If there is only one item
     * in the filtered list, it will be used when the user fires the OK action.
     *
     * @param queryPhasePane
     */
    public void find(final QueryPhasePane queryPhasePane) {
        final ObservableList<String> texts = FXCollections.observableArrayList(
                queryPhasePane.getDataAccessPanes().stream()
                        .map(pane -> pane.getPlugin().getName())
                        .sorted((a, b) -> a.compareToIgnoreCase(b))
                        .collect(Collectors.toList())
        );

        final ListView<String> lv = new ListView<>();
        lv.setItems(texts);

        final Alert dialog = createAlertDialog();

        final TextField tf = new TextField();
        tf.textProperty().addListener(new TextFieldChangeListener(texts, lv));

        final VBox root = new VBox();
        root.getChildren().addAll(tf, lv);
        dialog.getDialogPane().setContent(root);

        lv.setOnMouseClicked(new MouseEventHandler(dialog, lv));
        lv.setOnKeyPressed(new KeyEventHandler(dialog, lv));

        final Optional<ButtonType> option = dialog.showAndWait();
        if (option.isPresent() && option.get() == ButtonType.OK && result != null) {
            queryPhasePane.expandPlugin(result);
        }
    }

    protected Alert createAlertDialog() {
        final Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle(PLUGIN_FINDER_TITLE);
        dialog.setHeaderText(PLUGIN_FINDER_HEADER);
        dialog.setResizable(true);
        dialog.getDialogPane().getStylesheets().addAll(JavafxStyleManager.getMainStyleSheet());

        return dialog;
    }

    class MouseEventHandler implements EventHandler<MouseEvent> {

        private final Alert dialog;
        private final ListView<String> listView;

        public MouseEventHandler(final Alert dialog, final ListView listView) {
            this.dialog = dialog;
            this.listView = listView;
        }

        @Override
        public void handle(final MouseEvent mouseEvent) {
            switch (mouseEvent.getClickCount()) {
                case 1 -> {
                    final ObservableList<String> items = listView.getSelectionModel().getSelectedItems();
                    if (items.size() == 1) {
                        result = listView.getSelectionModel().getSelectedItem();
                    } else {
                        result = null;
                    }
                }
                case 2 -> {
                    result = listView.getSelectionModel().getSelectedItem();
                    dialog.setResult(ButtonType.OK);
                }
                default -> {
                }
            }
        }

    }

    class KeyEventHandler implements EventHandler<KeyEvent> {
        private final Alert dialog;
        private final ListView<String> listView;

        public KeyEventHandler(final Alert dialog, final ListView listView) {
            this.dialog = dialog;
            this.listView = listView;
        }

        @Override
        public void handle(KeyEvent event) {
            final KeyCode c = event.getCode();
            if (c == KeyCode.ENTER) {
                final ObservableList<String> items = listView.getSelectionModel().getSelectedItems();
                if (items.size() == 1) {
                    result = listView.getSelectionModel().getSelectedItem();
                    dialog.setResult(ButtonType.OK);
                } else {
                    result = null;
                }
            }
        }

    }

    class TextFieldChangeListener implements ChangeListener<String> {
        private final ObservableList<String> texts;
        private final ListView listView;

        public TextFieldChangeListener(final ObservableList<String> texts,
                                       final ListView listView) {
            this.texts = texts;
            this.listView = listView;
        }

        @Override
        public void changed(final ObservableValue<? extends String> observable,
                            final String oldValue,
                            final String newValue) {
            if (!newValue.isEmpty()) {
                final List<String> ls = texts.stream()
                        .filter(a -> StringUtils.containsIgnoreCase(a, newValue))
                        .collect(Collectors.toList());
                
                final ObservableList<String> filtered = FXCollections.observableArrayList(ls);
                listView.setItems(filtered);
                
                if (filtered.size() == 1) {
                    listView.getSelectionModel().select(0);
                } else {
                    listView.getSelectionModel().clearSelection();
                }
            } else {
                listView.setItems(texts);
                listView.getSelectionModel().clearSelection();
            }

            final ObservableList<String> items = listView.getItems();
            result = items.size() == 1 ? items.get(0) : null;
        }

    }
}
