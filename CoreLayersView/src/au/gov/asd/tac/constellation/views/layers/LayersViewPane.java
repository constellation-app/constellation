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

import au.gov.asd.tac.constellation.views.layers.layer.LayerDescription;
import au.gov.asd.tac.constellation.views.layers.state.LayersViewState;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * Layers View Pane.
 *
 * @author aldebaran30701
 */
public class LayersViewPane extends BorderPane {

    private final LayersViewController controller;
    private final GridPane layersGridPane;
    private final VBox layersViewPane;
    private final HBox options;
    private int currentIndex;

    public LayersViewPane(final LayersViewController controller) {

        // create controller
        this.controller = controller;

        // create layers
        final Label layerIdHeadingText = new Label("Layer\nID");
        final Label visibilityHeadingText = new Label("Visibility");
        final Label queryHeadingText = new Label("Query");
        final Label descriptionHeadingText = new Label("Description");

        // create gridpane and alignments
        layersGridPane = new GridPane();
        layersGridPane.setHgap(5);
        layersGridPane.setVgap(5);
        layersGridPane.setPadding(new Insets(0, 10, 10, 10));
        layersGridPane.addRow(0, layerIdHeadingText,
                visibilityHeadingText, queryHeadingText, descriptionHeadingText);

        // set heading alignments
        GridPane.setMargin(layerIdHeadingText, new Insets(15, 0, 0, 0));
        layerIdHeadingText.setTextAlignment(TextAlignment.CENTER);
        layerIdHeadingText.setMinWidth(40);
        layerIdHeadingText.setMinHeight(25);
        layerIdHeadingText.setPrefWidth(30);
        visibilityHeadingText.setPrefWidth(55);
        visibilityHeadingText.setMinWidth(50);
        queryHeadingText.setPrefWidth(10000);
        queryHeadingText.setMinWidth(80);
        descriptionHeadingText.setPrefWidth(10000);
        descriptionHeadingText.setMinWidth(80);

        // add default layers
        this.currentIndex = 0;
        createLayer(true);
        createLayer(false);

        // create options
        final Button addButton = new Button("Add New Layer");
        addButton.setAlignment(Pos.CENTER_RIGHT);
        addButton.setOnAction(event -> {
            if (layersGridPane.getRowCount() <= 32) { // 32 layers plus headings
                createLayer(false);
                controller.updateState();
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
        options.setPadding(new Insets(0, 0, 0, 10));

        // add layers grid and options to pane
        this.layersViewPane = new VBox(5, layersGridPane, options);

        // create layout bindings
        layersViewPane.prefWidthProperty().bind(this.widthProperty());
        options.prefWidthProperty().bind(layersViewPane.widthProperty());

        this.setCenter(layersViewPane);
    }

    private int createLayer(final boolean defaultLayer) {
        final Label layerIdText = new Label(String.format("%02d", ++currentIndex));
        layerIdText.setMinWidth(30);
        layerIdText.setPrefWidth(40);
        layerIdText.setTextAlignment(TextAlignment.CENTER);
        layerIdText.setPadding(new Insets(0, 0, 0, 10));

        final CheckBox visibilityCheckBox = new CheckBox();
        visibilityCheckBox.setMinWidth(60);
        visibilityCheckBox.setPadding(new Insets(0, 30, 0, 15));
        visibilityCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            controller.submit();
            controller.execute();
            controller.updateState();
        });

        final TextArea queryTextArea = new TextArea();
        queryTextArea.setPrefRowCount(1);
        queryTextArea.textProperty().addListener((observable, oldVal, newVal) -> {
            controller.updateState();
        });

        final TextArea descriptionTextArea = new TextArea();
        descriptionTextArea.setPrefRowCount(1);
        descriptionTextArea.textProperty().addListener((observable, oldVal, newVal) -> {
            controller.updateState();
        });

        if (defaultLayer) {
            layerIdText.setDisable(true);
            visibilityCheckBox.setSelected(true);
            visibilityCheckBox.setDisable(true);
            queryTextArea.setText(LayerDescription.DEFAULT_QUERY_STRING);
            queryTextArea.setDisable(true);
            descriptionTextArea.setText(LayerDescription.DEFAULT_QUERY_DESCRIPTION);
            descriptionTextArea.setDisable(true);
        }
        layersGridPane.addRow(currentIndex, layerIdText,
                visibilityCheckBox, queryTextArea, descriptionTextArea);
        return currentIndex;
    }

    public synchronized GridPane getLayers() {
        return layersGridPane;
    }

    public HBox getOptions() {
        return options;
    }

    public void setLayers(final LayersViewState newState) {
        currentIndex = 0;
        Platform.runLater(() -> {
            synchronized (this) {
                layersGridPane.getChildren().removeIf(node -> GridPane.getRowIndex(node) > 0);
                for (final LayerDescription layer : newState.getAllLayers()) {
                    detailLayer(createLayer(false), layer);
                }
            }
        });
    }

    private void detailLayer(final int rowIndex, final LayerDescription layerInfo) {
        for (final Node cell : layersGridPane.getChildren()) {
            if (GridPane.getRowIndex(cell) == rowIndex && cell instanceof CheckBox) {
                ((CheckBox) cell).setSelected(layerInfo.getCurrentLayerVisibility());
                ((CheckBox) cell).setDisable(layerInfo.getLayerQuery().equals(LayerDescription.DEFAULT_QUERY_STRING));
            } else if (GridPane.getRowIndex(cell) == rowIndex && cell instanceof TextArea) {
                if (GridPane.getColumnIndex(cell) == 2) {
                    ((TextArea) cell).setText(layerInfo.getLayerQuery());
                    ((TextArea) cell).setDisable(layerInfo.getLayerQuery().equals(LayerDescription.DEFAULT_QUERY_STRING));
                } else if (GridPane.getColumnIndex(cell) == 3) {
                    ((TextArea) cell).setText(layerInfo.getLayerDescription());
                    ((TextArea) cell).setDisable(layerInfo.getLayerQuery().equals(LayerDescription.DEFAULT_QUERY_STRING));
                }
            }
        }
    }
}
