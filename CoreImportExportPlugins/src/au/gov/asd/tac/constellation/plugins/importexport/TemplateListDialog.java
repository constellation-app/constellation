/*
 * Copyright 2010-2021 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.importexport;

import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import au.gov.asd.tac.constellation.utilities.file.FilenameEncoder;
import java.io.File;
import java.util.Locale;
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
import javafx.stage.Modality;
import javafx.stage.Window;

/**
 * Display a list of query names and allow the user to select one.
 *
 * @author algol
 */
public class TemplateListDialog {

    private static final Insets PADDING = new Insets(10, 0, 0, 0);
    private static final int NAMES_OFFSET = 5;
    private final Window owner;
    private final boolean isLoading;

    public TemplateListDialog(final Window owner, final boolean isLoading) {
        this.owner = owner;
        this.isLoading = isLoading;
    }

    private static String[] getFileLabels(final File delimIoDir) {
        final String[] names;
        if (delimIoDir.isDirectory()) {
            names = delimIoDir.list((final File dir, final String name) -> name.toLowerCase(Locale.ENGLISH).endsWith(FileExtensionConstants.JSON));
        } else {
            names = new String[0];
        }

        // Chop off ".json".
        for (int i = 0; i < names.length; i++) {
            names[i] = FilenameEncoder.decode(names[i].substring(0, names[i].length() - NAMES_OFFSET));
        }

        return names;
    }

    public String getName(final File delimIoDir) {
        final String[] templateLabels = getFileLabels(delimIoDir);

        final Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
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
        prompt.setPadding(PADDING);

        final VBox vbox = new VBox(nameList, prompt);

        dialog.setResizable(false);
        dialog.setTitle("Import template names");
        dialog.setHeaderText(String.format("Select an import template to %s.", isLoading ? "load" : "save"));
        dialog.getDialogPane().setContent(vbox);
        final Optional<ButtonType> option = dialog.showAndWait();
        if (option.isPresent() && option.get() == ButtonType.OK) {
            final String name = label.getText();
            final File f = new File(delimIoDir, FilenameEncoder.encode(name + FileExtensionConstants.JSON));
            boolean go = true;
            if (!isLoading && f.exists()) {
                final String msg = String.format("'%s' already exists. Do you want to overwrite it?", name);
                final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setHeaderText("Import template exists");
                alert.setContentText(msg);
                alert.initModality(Modality.WINDOW_MODAL);
                alert.initOwner(owner);
                final Optional<ButtonType> confirm = alert.showAndWait();
                go = confirm.isPresent() && confirm.get() == ButtonType.OK;
            }

            if (go) {
                return name;
            }
        }

        return null;
    }
}
