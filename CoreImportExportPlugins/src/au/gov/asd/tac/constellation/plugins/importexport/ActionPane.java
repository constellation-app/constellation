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

import au.gov.asd.tac.constellation.plugins.PluginException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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

    private final ImportController importController;

    public ActionPane(final ImportController controller) {
        this.importController = controller;

        final HBox runBox = new HBox();
        runBox.setSpacing(HBOX_SPACING);
        runBox.setPadding(PADDING);
        setRight(runBox);

        final Button importButton = new Button("Import");
        importButton.setOnAction((ActionEvent t) -> {
            try {
                importController.processImport();
            } catch (final PluginException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                displayAlert("Import Failed", ex.getLocalizedMessage(), false);
            }
        });

        runBox.getChildren().add(importButton);
    }

    private void displayAlert(final String title, final String header, final boolean successful) {
        final Alert dialog;
        if (successful) {
            dialog = new Alert(Alert.AlertType.CONFIRMATION, "", ButtonType.OK);
            final ImageView successIcon = new ImageView(SUCCESS_ICON_PATH);
            dialog.setGraphic(successIcon);

        } else {
            dialog = new Alert(Alert.AlertType.ERROR, "", ButtonType.OK);
        }
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.showAndWait();
    }
}
