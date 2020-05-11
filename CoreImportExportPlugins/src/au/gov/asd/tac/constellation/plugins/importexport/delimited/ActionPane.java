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
package au.gov.asd.tac.constellation.plugins.importexport.delimited;

import au.gov.asd.tac.constellation.plugins.PluginException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

/**
 *
 * @author sirius
 */
public class ActionPane extends BorderPane {
    
    private static final Logger LOGGER = Logger.getLogger(ActionPane.class.getName());
    final private static String SUCCESS_ICON_PATH = "au/gov/asd/tac/constellation/plugins/importexport/delimited/resources/success.jpg";

    private final ImportController importController;
    
    private void displayAlert(String title, String header, boolean successful){
        final Alert dialog;
        if(successful){
            dialog = new Alert(Alert.AlertType.CONFIRMATION, "", ButtonType.OK);
            final ImageView SUCCESS_ICON = new ImageView(SUCCESS_ICON_PATH);
            dialog.setGraphic(SUCCESS_ICON);
            
        }else{
            dialog = new Alert(Alert.AlertType.ERROR, "", ButtonType.OK);
        }
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.showAndWait();
    }

    public ActionPane(final ImportController importController) {
        this.importController = importController;

        HBox runBox = new HBox();
        runBox.setSpacing(5);
        runBox.setPadding(new Insets(5));
        setRight(runBox);

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                importController.cancelImport();
            }
        });

        Button importButton = new Button("Import");
        importButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                try {
                    
                    List<File> importedFiles = importController.processImport();
                    String[] filenames = new String[importedFiles.size()];
                    long noOfRows = 0;
                    for (int i = 0; i < importedFiles.size(); i++) {
                        filenames[i] = importedFiles.get(i).getName();
                        Path path = importedFiles.get(i).toPath();
                        noOfRows += Files.lines(path).count();
                    }
                    displayAlert("Success", "Successfully imported "+ noOfRows
                            +" rows from the following file(s):\n"+String.join("\n", filenames), true);
                    
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                    displayAlert("Import Failed", ex.getLocalizedMessage(), false);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    displayAlert("import Failed", ex.getLocalizedMessage(), false);
                } catch (PluginException ex) {
                    displayAlert("Import Failed", ex.getLocalizedMessage(), false);
                }
            }
        });

        runBox.getChildren().addAll(cancelButton, importButton);
    }
}
