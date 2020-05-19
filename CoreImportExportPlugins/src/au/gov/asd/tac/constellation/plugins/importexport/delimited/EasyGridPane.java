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
package au.gov.asd.tac.constellation.plugins.importexport.delimited;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

/**
 * The EasyGridPane is an extension of GridPane that simplifies the act of
 * adding column and row constraints to the grid.
 *
 * @author sirius
 */
public class EasyGridPane extends GridPane {

    public void addColumnConstraint(boolean fillWidth, HPos alignment, Priority grow, double maxWidth, double minWidth, double prefWidth, double percentWidth) {
        ColumnConstraints constraint = new ColumnConstraints();
        constraint.setFillWidth(fillWidth);
        constraint.setHalignment(alignment);
        constraint.setHgrow(grow);
        constraint.setMaxWidth(maxWidth);
        constraint.setMinWidth(minWidth);
        constraint.setPrefWidth(prefWidth);

        if (percentWidth >= 0) {
            constraint.setPercentWidth(percentWidth);
        }

        getColumnConstraints().add(constraint);
    }

    public void addRowConstraint(boolean fillHeight, VPos alignment, Priority grow, double maxHeight, double minHeight, double prefHeight, double percentHeight) {
        RowConstraints constraint = new RowConstraints();
        constraint.setFillHeight(fillHeight);
        constraint.setValignment(alignment);
        constraint.setVgrow(grow);
        constraint.setMaxHeight(maxHeight);
        constraint.setMinHeight(minHeight);
        constraint.setPrefHeight(prefHeight);

        if (percentHeight >= 0) {
            constraint.setPercentHeight(percentHeight);
        }

        getRowConstraints().add(constraint);
    }
}
