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
package au.gov.asd.tac.constellation.plugins.importexport;

import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

/**
 * Action Pane is responsible for allowing the action of importing to be
 * performed
 *
 * @author sirius
 */
public class ActionPane extends BorderPane {

    private static final Logger LOGGER = Logger.getLogger(ActionPane.class.getName());
    private static final String SUCCESS_ICON_PATH = "au/gov/asd/tac/constellation/plugins/importexport/delimited/resources/success.jpg";
    private static final Insets PADDING = new Insets(5, 5, 35, 5);
    private static final int HBOX_SPACING = 5;
    private final Button importButton;
    private final CheckBox skipInvalidRowsCheckBox;
    private final ImportController<?> importController;

    public ActionPane(final ImportController<?> controller) {
        this.importController = controller;

        final HBox runBox = new HBox();
        runBox.setSpacing(HBOX_SPACING);
        runBox.setPadding(PADDING);
        setRight(runBox);

        importButton = new Button("Import");
        importButton.setDisable(true);
        importButton.setOnAction((ActionEvent t) -> {
            try {
                importController.processImport();
            } catch (final PluginException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                displayAlert("Import Failed", ex.getLocalizedMessage(), false);
            }
        });

        runBox.getChildren().add(importButton);

        skipInvalidRowsCheckBox = new CheckBox("Skip Invalid Rows");
        skipInvalidRowsCheckBox.setSelected(false);
        skipInvalidRowsCheckBox.setDisable(true);        
        
        skipInvalidRowsCheckBox.setOnAction((t) -> 
            controller.setSkipInvalidRows(skipInvalidRowsCheckBox.isSelected()));

        runBox.getChildren().add(skipInvalidRowsCheckBox);
    }

    private void displayAlert(final String title, final String header, final boolean successful) {
        final Alert dialog;
        if (successful) {
            dialog = new Alert(Alert.AlertType.CONFIRMATION, "", ButtonType.OK);
            dialog.getDialogPane().getStylesheets().addAll(JavafxStyleManager.getMainStyleSheet());
            final ImageView successIcon = new ImageView(SUCCESS_ICON_PATH);
            dialog.setGraphic(successIcon);

        } else {
            dialog = new Alert(Alert.AlertType.ERROR, "", ButtonType.OK);
            dialog.getDialogPane().getStylesheets().addAll(JavafxStyleManager.getMainStyleSheet());
        }
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.showAndWait();
    }

    // sets the enabled state of the import button
    public void disableButton(final boolean isEnabled) {
        importButton.setDisable(isEnabled);
        skipInvalidRowsCheckBox.setDisable(isEnabled);
    }

}
