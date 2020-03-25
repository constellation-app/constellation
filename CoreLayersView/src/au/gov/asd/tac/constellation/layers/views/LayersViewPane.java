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
package au.gov.asd.tac.constellation.layers.views;

import au.gov.asd.tac.constellation.layers.LayersViewController;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 *
 * TODO: Note the code initially in this file is for proof of concept and trial
 * only. Full scale implementation will require refactoring and neatening of UI
 * elements.
 *
 * @author aldebaran30701
 */
public class LayersViewPane extends GridPane {

    private final SplitPane layersSplitPane = new SplitPane();
    private final VBox queries = new VBox();
    private final HBox layerExecution = new HBox();
    private final LayersViewController controller;
    private static final String FONT_FAMILY = Font.getDefault().getFamily();

    public LayersViewPane(final LayersViewController controller) {
        this.controller = controller;
        layerExecution.setAlignment(Pos.CENTER_RIGHT);
        layerExecution.setSpacing(15);
        GridPane.setHalignment(layerExecution, HPos.RIGHT);
        add(layerExecution, 0, 1);

        // Creating headings
        HBox headingBox = new HBox();
        Text layerIDHeadingText = new Text("Layer ID");
        Text visibleHeadingText = new Text("Visibility");
        Text queryStringHeadingText = new Text("Query String");
        Text descriptionHeadingText = new Text("Description");

        layerIDHeadingText.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 12));
        layerIDHeadingText.setFill(Color.web("#FFFFFF"));
        visibleHeadingText.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 12));
        visibleHeadingText.setFill(Color.web("#FFFFFF"));
        queryStringHeadingText.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 12));
        queryStringHeadingText.setFill(Color.web("#FFFFFF"));
        descriptionHeadingText.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 12));
        descriptionHeadingText.setFill(Color.web("#FFFFFF"));

        HBox.setMargin(layerIDHeadingText, new Insets(0, 10, 0, 0));
        HBox.setMargin(visibleHeadingText, new Insets(0, 8, 0, 5));
        HBox.setMargin(queryStringHeadingText, new Insets(0, 10, 0, 10));
        HBox.setMargin(descriptionHeadingText, new Insets(0, 0, 0, 10));

        // Adding headings
        headingBox.getChildren().addAll(layerIDHeadingText, visibleHeadingText, queryStringHeadingText, descriptionHeadingText);
        queries.getChildren().add(headingBox);

        // Creating layers
        for (int i = 1; i < 32; i++) {
            createLayer(i);
        }
        // adding textAreas to the splitpane
        layersSplitPane.getItems().addAll(queries);

        // code found in attribute calculator pane - unsure of exact need
        GridPane.setHalignment(layersSplitPane, HPos.LEFT);
        GridPane.setValignment(layersSplitPane, VPos.TOP);
        add(layersSplitPane, 0, 0);
    }

    private void createLayer(int i) {

        Text layerID = new Text(String.format("%02d", i));
        TextArea queryInput = new TextArea();
        TextArea descriptionInput = new TextArea();
        layerID.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 16));
        layerID.setFill(Color.web("#FFFFFF"));
        HBox.setMargin(layerID, new Insets(3, 10, 0, 10));
        HBox.setMargin(descriptionInput, new Insets(0, 5, 0, 5));

        CheckBox selectedOnlyCheckBox = new CheckBox();
        HBox.setMargin(selectedOnlyCheckBox, new Insets(5, 27, 0, 27));

        selectedOnlyCheckBox.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            controller.submit();
            controller.execute();
        });

        if (i == 1) { // select first layer and disable it
            selectedOnlyCheckBox.setSelected(true);
            selectedOnlyCheckBox.setDisable(true);
            queryInput.setText(" ");
            queryInput.setDisable(true);
            descriptionInput.setText("Display All Elements");
            descriptionInput.setDisable(true);
            layerID.setDisable(true);
        }

        HBox queryBox = new HBox();
        queryBox.getChildren().addAll(layerID, selectedOnlyCheckBox, queryInput, descriptionInput);
        VBox.setMargin(queryBox, new Insets(5, 0, 0, 0));
        queries.getChildren().add(queryBox);
    }

    public VBox getQueries() {
        return queries;
    }
}
