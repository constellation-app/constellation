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
package au.gov.asd.tac.constellation.views.layers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
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
public class LayersViewPane extends BorderPane {

    private static final String FONT_FAMILY = Font.getDefault().getFamily();
    
    private final LayersViewController controller;
    private final VBox layersViewPane;
    private final VBox layers;
    private final HBox options;
    private int currentIndex;

    public LayersViewPane(final LayersViewController controller) {
        
        // create controller
        this.controller = controller;
        
        // create layers
        this.layers = new VBox();
        layers.setAlignment(Pos.TOP_LEFT);
        layers.setBackground(new Background(new BackgroundFill(Color.ORANGE, CornerRadii.EMPTY, Insets.EMPTY)));

        // create headings
        final Text layerIdHeadingText = new Text("Layer ID");
        layerIdHeadingText.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 12));
        layerIdHeadingText.setFill(Color.web("#FFFFFF"));
        HBox.setMargin(layerIdHeadingText, new Insets(0, 10, 0, 0));
        
        final Text visibilityHeadingText = new Text("Visibility");
        visibilityHeadingText.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 12));
        visibilityHeadingText.setFill(Color.web("#FFFFFF"));
        HBox.setMargin(visibilityHeadingText, new Insets(0, 8, 0, 5));
        
        final Text queryHeadingText = new Text("Query");
        queryHeadingText.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 12));
        queryHeadingText.setFill(Color.web("#FFFFFF"));
        HBox.setMargin(queryHeadingText, new Insets(0, 10, 0, 10));
        
        final Text descriptionHeadingText = new Text("Description");
        descriptionHeadingText.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 12));
        descriptionHeadingText.setFill(Color.web("#FFFFFF"));
        HBox.setMargin(descriptionHeadingText, new Insets(0, 0, 0, 10));

        // add headings to heading box
        final HBox headingBox = new HBox();
        headingBox.getChildren().addAll(layerIdHeadingText, visibilityHeadingText, 
                queryHeadingText, descriptionHeadingText);
        
        // add heading box to layers
        layers.getChildren().add(headingBox);

        // add default layers
        this.currentIndex = 0;
        createLayer(true);
        createLayer(false);
        
        // create options
        this.options = new HBox();
        options.setAlignment(Pos.TOP_LEFT);
        options.setBackground(new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY)));
        
        // create add button
        final Button addButton = new Button("Add New Layer");
        addButton.setAlignment(Pos.CENTER_RIGHT);
        addButton.setOnAction(event -> {
            createLayer(false);
            event.consume();
        });
        
        // add button to options
        options.getChildren().add(addButton);
        
        // add layers and options to pane
        this.layersViewPane = new VBox();
        layersViewPane.prefWidthProperty().bind(this.widthProperty());
        layersViewPane.getChildren().addAll(layers, options);
        layersViewPane.setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY)));
        
        layers.prefWidthProperty().bind(layersViewPane.widthProperty());
        options.prefWidthProperty().bind(layersViewPane.widthProperty());
        
        this.setCenter(layersViewPane);
    }

    private void createLayer(final boolean defaultLayer) {
        final Text layerIdText = new Text(String.format("%02d", ++currentIndex));
        layerIdText.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 16));
        layerIdText.setFill(Color.web("#FFFFFF"));
        HBox.setMargin(layerIdText, new Insets(0, 5, 0, 5));

        final CheckBox visibilityCheckBox = new CheckBox();
        HBox.setMargin(visibilityCheckBox, new Insets(0, 5, 0, 5));
        visibilityCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            controller.submit();
            controller.execute();
        });
        
        final TextArea queryTextArea = new TextArea();
        queryTextArea.setPrefRowCount(1);
        HBox.setMargin(queryTextArea, new Insets(0, 5, 0, 5));
        
        final TextArea descriptionTextArea = new TextArea();
        descriptionTextArea.setPrefRowCount(1);
        HBox.setMargin(descriptionTextArea, new Insets(0, 5, 0, 5));

        if (defaultLayer) {
            layerIdText.setDisable(true);
            visibilityCheckBox.setSelected(true);
            visibilityCheckBox.setDisable(true);
            queryTextArea.setText("");
            queryTextArea.setDisable(true);
            descriptionTextArea.setText("Display All Elements");
            descriptionTextArea.setDisable(true);
        }

        final HBox layerBox = new HBox();
        HBox.setMargin(layerBox, new Insets(5, 0, 5, 0));
        layerBox.getChildren().addAll(layerIdText, visibilityCheckBox, queryTextArea, descriptionTextArea);
        layers.getChildren().add(layerBox);
    }

    public VBox getLayers() {
        return layers;
    }

    public HBox getOptions() {
        return options;
    }
}
