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
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * Layers View Pane.
 * 
 * TODO: Note the code initially in this file is for proof of concept and trial
 * only. Full scale implementation will require refactoring and neatening of UI
 * elements.
 *
 * @author aldebaran30701
 */
public class LayersViewPane extends BorderPane {

    private static final String DEFAULT_LAYER_PLACEHOLDER = "Default";
    
    private final LayersViewController controller;
    private final VBox layersViewPane;
    private final VBox layers;
    private final HBox options;
    private int currentIndex;

    public LayersViewPane(final LayersViewController controller) {
        
        // create controller
        this.controller = controller;
        
        // create layers
        final Label layerIdHeadingText = new Label("Layer\nID");
        HBox.setHgrow(layerIdHeadingText, Priority.ALWAYS);
        final Label visibilityHeadingText = new Label("Visibility");
        HBox.setHgrow(visibilityHeadingText, Priority.ALWAYS);
        final Label queryHeadingText = new Label("Query");
        HBox.setHgrow(queryHeadingText, Priority.ALWAYS);
        final Label descriptionHeadingText = new Label("Description");
        HBox.setHgrow(descriptionHeadingText, Priority.ALWAYS);
        final HBox headingBox = new HBox(5, layerIdHeadingText, 
                visibilityHeadingText, queryHeadingText, descriptionHeadingText);
        VBox.setMargin(headingBox, new Insets(5));
        VBox.setVgrow(headingBox, Priority.ALWAYS);
        
        // set heading alignments
        layerIdHeadingText.setTextAlignment(TextAlignment.CENTER);
        layerIdHeadingText.setMinWidth(40);
        layerIdHeadingText.setMinHeight(25); 
        layerIdHeadingText.setPrefWidth(30);
        visibilityHeadingText.setPrefWidth(50);
        visibilityHeadingText.setMinWidth(50);
        queryHeadingText.setPrefWidth(10000);
        queryHeadingText.setMinWidth(80);
        descriptionHeadingText.setPrefWidth(10000);
        descriptionHeadingText.setMinWidth(80);
        
        this.layers = new VBox(5, headingBox);
        layers.setAlignment(Pos.TOP_LEFT);
//        layers.setBackground(new Background(new BackgroundFill(Color.ORANGE, CornerRadii.EMPTY, Insets.EMPTY)));

        // add default layers
        this.currentIndex = 0;
        createLayer(true);
        createLayer(false);
        
        // create options
        final Button addButton = new Button("Add New Layer");
        addButton.setAlignment(Pos.CENTER_RIGHT);
        addButton.setOnAction(event -> {
            if (layers.getChildren().size() <= 32) {
                createLayer(false);
            } else {
                final NotifyDescriptor nd = new NotifyDescriptor.Message(
                        "You cannot have more than 32 layers", NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            }
            event.consume();
        });
        HBox.setHgrow(addButton, Priority.ALWAYS);
        
        this.options = new HBox(5, addButton);
        options.setAlignment(Pos.TOP_LEFT);
//        options.setBackground(new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY)));
        
        // add layers and options to pane
        this.layersViewPane = new VBox(5, layers, options);
//        layersViewPane.setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY)));
        
        // create layout bindings
        headingBox.prefWidthProperty().bind(layers.widthProperty());
        layersViewPane.prefWidthProperty().bind(this.widthProperty());
        layers.prefWidthProperty().bind(layersViewPane.widthProperty());
        options.prefWidthProperty().bind(layersViewPane.widthProperty());
        
        this.setCenter(layersViewPane);
    }

    private void createLayer(final boolean defaultLayer) {
        final Label layerIdText = new Label(String.format("%02d", ++currentIndex));
        layerIdText.setMinWidth(30);
        layerIdText.setTextAlignment(TextAlignment.CENTER);
        layerIdText.setPadding(new Insets(0,0,0,10));
        HBox.setHgrow(layerIdText, Priority.ALWAYS);

        final CheckBox visibilityCheckBox = new CheckBox();
        visibilityCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            controller.submit();
            controller.execute();
        });
        HBox.setHgrow(visibilityCheckBox, Priority.ALWAYS);
        visibilityCheckBox.setMinWidth(60);
        visibilityCheckBox.setPadding(new Insets(0,25,0,25));
        
        final TextArea queryTextArea = new TextArea();
        queryTextArea.setPrefRowCount(1);
        HBox.setHgrow(queryTextArea, Priority.ALWAYS);
        
        final TextArea descriptionTextArea = new TextArea();
        descriptionTextArea.setPrefRowCount(1);
        HBox.setHgrow(descriptionTextArea, Priority.ALWAYS);

        if (defaultLayer) {
            layerIdText.setDisable(true);
            visibilityCheckBox.setSelected(true);
            visibilityCheckBox.setDisable(true);
            queryTextArea.setText(DEFAULT_LAYER_PLACEHOLDER);
            queryTextArea.setDisable(true);
            descriptionTextArea.setText("Show All");
            descriptionTextArea.setDisable(true);
        }

        final HBox layerBox = new HBox(5, layerIdText, 
                visibilityCheckBox, queryTextArea, descriptionTextArea);
        VBox.setMargin(layerBox, new Insets(5));
        VBox.setVgrow(layerBox, Priority.ALWAYS);
        layerBox.setAlignment(Pos.CENTER);
        layers.getChildren().add(layerBox);
        
        layerIdText.setPrefWidth(40);
        layerBox.prefWidthProperty().bind(layers.widthProperty());
    }

    public VBox getLayers() {
        return layers;
    }

    public HBox getOptions() {
        return options;
    }
}