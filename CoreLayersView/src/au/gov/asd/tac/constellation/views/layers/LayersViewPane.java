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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    private final List<LayerDescription> layers;

    public LayersViewPane(final LayersViewController controller) {

        // create controller
        this.controller = controller;

        // create layer headings
        final Label layerIdHeadingText = new Label("Layer\nID");
        final Label visibilityHeadingText = new Label("Visibility");
        final Label queryHeadingText = new Label("Query");
        final Label descriptionHeadingText = new Label("Description");

        // create gridpane and alignments
        layersGridPane = new GridPane();
        layersGridPane.setHgap(5);
        layersGridPane.setVgap(5);
        layersGridPane.setPadding(new Insets(0, 10, 10, 10));
        layersGridPane.addRow(0, layerIdHeadingText, visibilityHeadingText,
                queryHeadingText, descriptionHeadingText);

        // set heading alignments
        GridPane.setMargin(layerIdHeadingText, new Insets(15, 0, 0, 0));
        layerIdHeadingText.setTextAlignment(TextAlignment.CENTER);
        layerIdHeadingText.setMinWidth(40);
        layerIdHeadingText.setMinHeight(25);
        layerIdHeadingText.setPrefWidth(30);
        visibilityHeadingText.setPrefWidth(55);
        visibilityHeadingText.setMinWidth(75);
        queryHeadingText.setPrefWidth(10000);
        queryHeadingText.setMinWidth(80);
        descriptionHeadingText.setPrefWidth(10000);
        descriptionHeadingText.setMinWidth(80);

        // instantiate list of layers
        layers = new ArrayList<>();

        // set default layers
        setDefaultLayers();

        // create options
        final Button addButton = new Button("Add New Layer");
        addButton.setAlignment(Pos.CENTER_RIGHT);
        addButton.setOnAction(event -> {
            if (layersGridPane.getRowCount() <= 32) {
                createLayer(layers.size() + 1, false, "", "");
                controller.writeState();
            } else {
                final NotifyDescriptor nd = new NotifyDescriptor.Message(
                        "You cannot have more than 32 layers", NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            }
            event.consume();
        });
        HBox.setHgrow(addButton, Priority.ALWAYS);

        final Button deselectAllButton = new Button("Deselect All Layers");
        deselectAllButton.setAlignment(Pos.CENTER_RIGHT);
        deselectAllButton.setOnAction(event -> {
            for (final LayerDescription layer : layers) {
                layer.setCurrentLayerVisibility(false);
            }
            if (this.layers.isEmpty()) {
                setDefaultLayers();
            } else {
                setLayers(List.copyOf(layers));
            }
            controller.execute();

            event.consume();
        });
        HBox.setHgrow(deselectAllButton, Priority.ALWAYS);

        this.options = new HBox(5, addButton, deselectAllButton);
        options.setAlignment(Pos.TOP_LEFT);
        options.setPadding(new Insets(0, 0, 0, 10));

        // add layers grid and options to pane
        this.layersViewPane = new VBox(5, layersGridPane, options);

        // create layout bindings
        layersViewPane.prefWidthProperty().bind(this.widthProperty());
        options.prefWidthProperty().bind(layersViewPane.widthProperty());

        this.setCenter(layersViewPane);
        controller.writeState();
    }

    public LayersViewController getController() {
        return controller;
    }

    private int createLayer(final int currentIndex, final boolean checkBoxSelected, final String query, final String description) {
        final Label layerIdText = new Label(String.format("%02d", currentIndex));
        layerIdText.setMinWidth(30);
        layerIdText.setPrefWidth(40);
        layerIdText.setTextAlignment(TextAlignment.CENTER);
        layerIdText.setPadding(new Insets(0, 0, 0, 10));

        final Node visibilityCheckBox = new CheckBox();
        ((CheckBox) visibilityCheckBox).setMinWidth(60);
        ((CheckBox) visibilityCheckBox).setPadding(new Insets(0, 30, 0, 15));
        ((CheckBox) visibilityCheckBox).setSelected(checkBoxSelected);
        visibilityCheckBox.setOnMouseClicked(e -> {
            final Node source = (Node) e.getSource();
            final LayerDescription layer = layers.get(GridPane.getRowIndex(source) - 1);
            layer.setCurrentLayerVisibility(!layer.getCurrentLayerVisibility());
            controller.execute();
            controller.writeState();
        });

        final Node queryTextArea = new TextArea();
        ((TextArea) queryTextArea).setPrefRowCount(1);
        ((TextArea) queryTextArea).setText(query);
        ((TextArea) queryTextArea).focusedProperty().addListener((observable, oldVal, newVal) -> {
            if (!newVal) {
                final LayerDescription layer = layers.get(currentIndex - 1);
                layer.setQueryText(((TextArea) queryTextArea).getText());
                controller.writeState();
            }
        });

        final Node descriptionTextArea = new TextArea();
        ((TextArea) descriptionTextArea).setPrefRowCount(1);
        ((TextArea) descriptionTextArea).setText(description);
        ((TextArea) descriptionTextArea).focusedProperty().addListener((observable, oldVal, newVal) -> {
            if (!newVal) {
                final LayerDescription layer = layers.get(currentIndex - 1);
                layer.setDescriptionText(((TextArea) descriptionTextArea).getText());
                controller.writeState();
            }
        });

        if (query.equals(LayerDescription.DEFAULT_QUERY_STRING)) {
            layerIdText.setDisable(true);
            ((CheckBox) visibilityCheckBox).setSelected(true);
            visibilityCheckBox.setDisable(true);
            queryTextArea.setDisable(true);
            descriptionTextArea.setDisable(true);
        }
        layers.add(new LayerDescription(currentIndex, checkBoxSelected, query, description));
        layersGridPane.addRow(currentIndex, layerIdText,
                visibilityCheckBox, queryTextArea, descriptionTextArea);

        return currentIndex;
    }

    public List<LayerDescription> getlayers() {
        return Collections.unmodifiableList(layers);
    }

    public synchronized void setLayers(final List<LayerDescription> layers) {
        Platform.runLater(() -> {
            this.layers.clear();
            final List<LayerDescription> layersCopy = new ArrayList();
            layers.forEach((layer) -> {
                layersCopy.add(new LayerDescription(layer));
            });
            updateLayers(layersCopy);
        });
    }

    private void updateLayers(List<LayerDescription> layers) {
        synchronized (this) {
            layersGridPane.getChildren().removeIf(node -> GridPane.getRowIndex(node) > 0);
            for (final LayerDescription layer : layers) {
                createLayer(layer.getLayerIndex(), layer.getCurrentLayerVisibility(),
                        layer.getLayerQuery(), layer.getLayerDescription());

            }
        }
    }

    public synchronized void setDefaultLayers() {
        final List<LayerDescription> defaultLayers = new ArrayList<>();
        defaultLayers.add(new LayerDescription(1, true,
                LayerDescription.DEFAULT_QUERY_STRING, LayerDescription.DEFAULT_QUERY_DESCRIPTION));
        defaultLayers.add(new LayerDescription(2, false, "", ""));

        setLayers(defaultLayers);
    }
}
