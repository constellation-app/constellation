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
package au.gov.asd.tac.constellation.plugins.importexport.delimited.io;

import java.io.File;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Display a list of query names and allow the user to select one.
 *
 * @author algol
 */
class TemplateListDialog {

    private final Object owner;
    private final boolean isLoading;
    private final String initial;

    TemplateListDialog(final Object owner, final boolean isLoading, final String initial) {
        this.owner = owner;
        this.isLoading = isLoading;
        this.initial = initial;
    }

    private static String[] getFileLabels(final File delimIoDir) {
        final String[] names;
        if (delimIoDir.isDirectory()) {
            names = delimIoDir.list((final File dir, final String name) -> {
                return name.toLowerCase().endsWith(".json");
            });
        } else {
            names = new String[0];
        }

        // Chop off ".json".
        for (int i = 0; i < names.length; i++) {
            names[i] = decode(names[i].substring(0, names[i].length() - 5));
        }

        return names;
    }

    String getName(final Object owner, final File delimIoDir) {
        final String[] templateLabels = getFileLabels(delimIoDir);

        final Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);

        final TextField label = new TextField();
        label.setPromptText("Template label");

        final ObservableList<String> q = FXCollections.observableArrayList(templateLabels);
        final ListView<String> nameList = new ListView<>(q);
        nameList.setEditable(false);
        nameList.setOnMouseClicked(event -> {
            label.setText(nameList.getSelectionModel().getSelectedItem());
            if (event.getClickCount() > 1) {
                dialog.setResult(ButtonType.OK);
            }
        });

        final HBox prompt = new HBox(new Label("Name: "), label);
        prompt.setPadding(new Insets(10, 0, 0, 0));

        final VBox vbox = new VBox(nameList, prompt);

        dialog.setResizable(false);
        dialog.setTitle("Import template names");
        dialog.setHeaderText(String.format("Select an import template to %s.", isLoading ? "load" : "save"));
        dialog.getDialogPane().setContent(vbox);
        final Optional<ButtonType> option = dialog.showAndWait();
        if (option.isPresent() && option.get() == ButtonType.OK) {
            final String name = label.getText();
            final File f = new File(delimIoDir, encode(name + ".json"));
            boolean go = true;
            if (!isLoading && f.exists()) {
                final String msg = String.format("'%s' already exists. Do you want to overwrite it?", name);
                final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setHeaderText("Import template exists");
                alert.setContentText(msg);
                final Optional<ButtonType> confirm = alert.showAndWait();
                go = confirm.isPresent() && confirm.get() == ButtonType.OK;
            }

            if (go) {
                return name;
            }
        }

        return null;
    }

    /**
     * Encode a String so it can be used as a filename.
     *
     * @param s The String to be encoded.
     *
     * @return The encoded String.
     */
    public static String encode(final String s) {
        final StringBuilder b = new StringBuilder();
        for (final char c : s.toCharArray()) {
            if (isValidFileCharacter(c)) {
                b.append(c);
            } else {
                b.append(String.format("_%04x", (int) c));
            }
        }

        return b.toString();
    }

    /**
     * Decode a String that has been encoded by {@link encode(String)}.
     *
     * @param s The String to be decoded.
     *
     * @return The decoded String.
     */
    static String decode(final String s) {
        final StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c != '_') {
                b.append(c);
            } else {
                final String hex = s.substring(i + 1, Math.min(i + 5, s.length()));
                if (hex.length() == 4) {
                    try {
                        final int value = Integer.parseInt(hex, 16);
                        b.append((char) value);
                        i += 4;
                    } catch (final NumberFormatException ex) {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        }

        return b.toString();
    }

    static boolean isValidFileCharacter(char c) {
        return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == ' ' || c == '-' || c == '.';
    }
}
