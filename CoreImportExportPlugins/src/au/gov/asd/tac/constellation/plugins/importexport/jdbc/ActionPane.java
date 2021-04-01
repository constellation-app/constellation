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
package au.gov.asd.tac.constellation.plugins.importexport.jdbc;

import au.gov.asd.tac.constellation.plugins.PluginException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class ActionPane extends BorderPane {

    private static final Logger LOGGER = Logger.getLogger(ActionPane.class.getName());

    public ActionPane(final ImportController controller) {
        final HBox runBox = new HBox();
        runBox.setSpacing(5);
        runBox.setPadding(new Insets(5));
        setRight(runBox);

        final Button importButton = new Button("Import");
        importButton.setOnAction(t -> {
            try {
                controller.processImport();
            } catch (final IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (final PluginException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        });

        runBox.getChildren().add(importButton);
    }
}
