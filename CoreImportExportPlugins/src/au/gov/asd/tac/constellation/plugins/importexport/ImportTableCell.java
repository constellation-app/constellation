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
package au.gov.asd.tac.constellation.plugins.importexport;

import au.gov.asd.tac.constellation.plugins.importexport.model.CellValue;
import au.gov.asd.tac.constellation.plugins.importexport.model.TableRow;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;

/**
 * ImportTableCell extends a standard JavaFX TableCell to provide status
 * information to the user by updating its label, and background color.
 *
 * @author sirius
 */
public class ImportTableCell extends TableCell<TableRow, CellValue> {

    @Override
    public void updateItem(final CellValue item, final boolean empty) {

        super.updateItem(item, empty);

        if (!empty) {
            final Label label = new Label(item.getText());
            setGraphic(label);

            if (!item.isIncluded()) {
                setStyle("-fx-background-color: grey;");
            } else {
                final String message = item.getMessage();
                final boolean error = item.isError();
                if (message == null) {
                    setStyle("-fx-background-color: transparent;");
                    setTooltip(null);
                } else {
                    setTooltip(new Tooltip(message));
                    if (error) {
                        setStyle("-fx-background-color: rgba(255,0,0,0.3);");
                    } else {
                        setStyle("-fx-background-color: transparent;");
                    }
                }
            }
        }
    }

}
