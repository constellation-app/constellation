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

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.views.layers.query.BitMaskQuery;
import au.gov.asd.tac.constellation.views.layers.query.BitMaskQueryCollection;
import au.gov.asd.tac.constellation.views.layers.query.Query;
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
import org.apache.commons.lang3.StringUtils;
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

    public LayersViewPane(final LayersViewController controller) {

        // create controller
        this.controller = controller;

        // create layer headings
        final Label layerIdHeadingText = new Label("Layer\nID");
        final Label visibilityHeadingText = new Label("Visibility");
        final Label queryHeadingText = new Label("Query");
        final Label descriptionHeadingText = new Label("Description");
        final Label vertexHeadingText = new Label("Vertex");
        final Label transactionHeadingText = new Label("Transaction");

        // create gridpane and alignments
        layersGridPane = new GridPane();
        layersGridPane.setHgap(5);
        layersGridPane.setVgap(5);
        layersGridPane.setPadding(new Insets(0, 10, 10, 10));
        layersGridPane.addRow(0, layerIdHeadingText, visibilityHeadingText,
                queryHeadingText, descriptionHeadingText, vertexHeadingText, transactionHeadingText);

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

        // set default layers
        this.setDefaultLayers();

        // create options
        final Button addButton = new Button("Add New Layer");
        addButton.setAlignment(Pos.CENTER_RIGHT);
        addButton.setOnAction(event -> {
            if (layersGridPane.getRowCount() <= BitMaskQueryCollection.MAX_QUERY_AMT) {
                createLayer(Math.max(controller.getTransactionQueryCollection().getHighestQueryIndex() + 1,
                        controller.getVertexQueryCollection().getHighestQueryIndex() + 1),
                        false, null, StringUtils.EMPTY, true, true);
                controller.writeState();
            } else {
                final NotifyDescriptor nd = new NotifyDescriptor.Message("You cannot have more than "
                        + BitMaskQueryCollection.MAX_QUERY_AMT + " layers", NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            }
            event.consume();
        });
        HBox.setHgrow(addButton, Priority.ALWAYS);

        final Button deselectAllButton = new Button("Deselect All Layers");
        deselectAllButton.setAlignment(Pos.CENTER_RIGHT);
        deselectAllButton.setOnAction(event -> {
            controller.getVertexQueryCollection().setVisibilityOnAll(false);
            controller.getTransactionQueryCollection().setVisibilityOnAll(false);
            controller.writeState();
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

    private void createLayer(final int currentIndex, final boolean checkBoxSelected, final String query, final String description, final boolean showVertices, final boolean showTransactions) {

        // Layer ID
        final Label layerIdText = new Label(String.format("%02d", currentIndex));
        layerIdText.setMinWidth(30);
        layerIdText.setPrefWidth(40);
        layerIdText.setTextAlignment(TextAlignment.CENTER);
        layerIdText.setPadding(new Insets(0, 0, 0, 10));

        // Layer Visibility
        final Node visibilityCheckBox = new CheckBox();
        ((CheckBox) visibilityCheckBox).setMinWidth(60);
        ((CheckBox) visibilityCheckBox).setPadding(new Insets(0, 30, 0, 15));
        ((CheckBox) visibilityCheckBox).setSelected(checkBoxSelected);
        visibilityCheckBox.setOnMouseClicked(e -> {
            final int gridIndex = GridPane.getRowIndex((Node) e.getSource()) - 1;
            final BitMaskQuery vxbitMaskQuery = controller.getVertexQueryCollection().getQuery(gridIndex);
            final BitMaskQuery txbitMaskQuery = controller.getTransactionQueryCollection().getQuery(gridIndex);
            if (vxbitMaskQuery != null) {
                vxbitMaskQuery.setVisibility(!vxbitMaskQuery.getVisibility());
            }
            if (txbitMaskQuery != null) {
                txbitMaskQuery.setVisibility(!txbitMaskQuery.getVisibility());
            }
            controller.execute();
            controller.writeState();
        });

        // Include Vertex Preference
        final Node vxCheckBox = new CheckBox("Vertex");
        ((CheckBox) vxCheckBox).setSelected(showVertices);
        ((CheckBox) vxCheckBox).setMinWidth(60);
        ((CheckBox) vxCheckBox).setPadding(new Insets(0, 30, 0, 15));
        vxCheckBox.setOnMouseClicked(e -> {
            final int gridIndex = GridPane.getRowIndex((Node) e.getSource()) - 1;
            final BitMaskQuery vxbitMaskQuery = controller.getVertexQueryCollection().getQuery(gridIndex);
            final BitMaskQuery txbitMaskQuery = controller.getTransactionQueryCollection().getQuery(gridIndex);

            if (vxbitMaskQuery != null && txbitMaskQuery != null) {
                controller.getVertexQueryCollection().removeQuery(gridIndex);
            } else if (vxbitMaskQuery != null) {
                final BitMaskQuery tempQuery = new BitMaskQuery(new Query(GraphElementType.TRANSACTION, controller.getVertexQueryCollection().getQuery(gridIndex).getQueryString()), gridIndex, controller.getVertexQueryCollection().getQuery(gridIndex).getDescription());
                tempQuery.setVisibility(controller.getVertexQueryCollection().getQuery(gridIndex).getVisibility());
                controller.getTransactionQueryCollection().add(tempQuery);
                controller.getVertexQueryCollection().removeQuery(gridIndex);
            } else if (txbitMaskQuery != null) {
                final BitMaskQuery tempQuery = new BitMaskQuery(new Query(GraphElementType.VERTEX, controller.getTransactionQueryCollection().getQuery(gridIndex).getQueryString()), gridIndex, controller.getTransactionQueryCollection().getQuery(gridIndex).getDescription());
                tempQuery.setVisibility(controller.getTransactionQueryCollection().getQuery(gridIndex).getVisibility());
                controller.getVertexQueryCollection().add(tempQuery);
            } else {
                controller.getVertexQueryCollection().add(new BitMaskQuery(new Query(GraphElementType.VERTEX, null), gridIndex, StringUtils.EMPTY));
            }

            controller.execute();
            controller.writeState();
        });

        // Include Transaction Preference
        final Node txCheckBox = new CheckBox("Transaction");
        ((CheckBox) txCheckBox).setMinWidth(60);
        ((CheckBox) txCheckBox).setPadding(new Insets(0, 30, 0, 15));
        ((CheckBox) txCheckBox).setSelected(showTransactions);
        txCheckBox.setOnMouseClicked(e -> {
            final int gridIndex = GridPane.getRowIndex((Node) e.getSource()) - 1;
            final BitMaskQuery vxbitMaskQuery = controller.getVertexQueryCollection().getQuery(gridIndex);
            final BitMaskQuery txbitMaskQuery = controller.getTransactionQueryCollection().getQuery(gridIndex);

            if (vxbitMaskQuery != null && txbitMaskQuery != null) {
                controller.getTransactionQueryCollection().removeQuery(gridIndex);
            } else if (vxbitMaskQuery != null) {
                final BitMaskQuery tempQuery = new BitMaskQuery(new Query(GraphElementType.TRANSACTION, controller.getVertexQueryCollection().getQuery(gridIndex).getQueryString()), gridIndex, controller.getVertexQueryCollection().getQuery(gridIndex).getDescription());
                tempQuery.setVisibility(controller.getVertexQueryCollection().getQuery(gridIndex).getVisibility());
                controller.getTransactionQueryCollection().add(tempQuery);
            } else if (txbitMaskQuery != null) {
                final BitMaskQuery tempQuery = new BitMaskQuery(new Query(GraphElementType.VERTEX, controller.getTransactionQueryCollection().getQuery(gridIndex).getQueryString()), gridIndex, controller.getTransactionQueryCollection().getQuery(gridIndex).getDescription());
                tempQuery.setVisibility(controller.getTransactionQueryCollection().getQuery(gridIndex).getVisibility());
                controller.getVertexQueryCollection().add(tempQuery);
                controller.getTransactionQueryCollection().removeQuery(gridIndex);
            } else {
                controller.getTransactionQueryCollection().add(new BitMaskQuery(new Query(GraphElementType.TRANSACTION, null), gridIndex, StringUtils.EMPTY));
            }

            controller.execute();
            controller.writeState();
        });

        // Layer Query Entry
        final Node queryTextArea = new TextArea();
        ((TextArea) queryTextArea).setPrefRowCount(1);
        ((TextArea) queryTextArea).setText(query);
        ((TextArea) queryTextArea).focusedProperty().addListener((observable, oldVal, newVal) -> {
            if (!newVal) {
                final BitMaskQuery vxbitMaskQuery = controller.getVertexQueryCollection().getQuery(currentIndex);
                final BitMaskQuery txbitMaskQuery = controller.getTransactionQueryCollection().getQuery(currentIndex);
                if (vxbitMaskQuery != null) {
                    vxbitMaskQuery.setQuery(new Query(GraphElementType.VERTEX, StringUtils.isBlank(((TextArea) queryTextArea).getText()) ? StringUtils.EMPTY : ((TextArea) queryTextArea).getText()));
                }
                if (txbitMaskQuery != null) {
                    txbitMaskQuery.setQuery(new Query(GraphElementType.TRANSACTION, StringUtils.isBlank(((TextArea) queryTextArea).getText()) ? StringUtils.EMPTY : ((TextArea) queryTextArea).getText()));
                }
                controller.writeState();
            }
        });

        // Layer Description Entry
        final Node descriptionTextArea = new TextArea();
        ((TextArea) descriptionTextArea).setPrefRowCount(1);
        ((TextArea) descriptionTextArea).setText(description);
        ((TextArea) descriptionTextArea).focusedProperty().addListener((observable, oldVal, newVal) -> {
            if (!newVal) {
                final BitMaskQuery vxbitMaskQuery = controller.getVertexQueryCollection().getQuery(currentIndex);
                if (vxbitMaskQuery != null) {
                    vxbitMaskQuery.setDescription(((TextArea) descriptionTextArea).getText());
                }
                final BitMaskQuery txbitMaskQuery = controller.getTransactionQueryCollection().getQuery(currentIndex);
                if (txbitMaskQuery != null) {
                    txbitMaskQuery.setDescription(((TextArea) descriptionTextArea).getText());
                }
                controller.writeState();
            }
        });

        // Default layer Handling
        if (currentIndex == 0) {
            layerIdText.setDisable(true);
            ((CheckBox) visibilityCheckBox).setSelected(true);
            visibilityCheckBox.setDisable(true);
            vxCheckBox.setDisable(true);
            txCheckBox.setDisable(true);
            queryTextArea.setDisable(true);
            descriptionTextArea.setDisable(true);
        }

        // Adding to collections based on checkbox preferences.
        if (showVertices) {
            final BitMaskQuery vxbitMaskQuery = new BitMaskQuery(new Query(GraphElementType.VERTEX, query), currentIndex, description);
            vxbitMaskQuery.setVisibility(checkBoxSelected);
            controller.getVertexQueryCollection().add(vxbitMaskQuery);
        }

        if (showTransactions) {
            final BitMaskQuery txbitMaskQuery = new BitMaskQuery(new Query(GraphElementType.TRANSACTION, query), currentIndex, description);
            txbitMaskQuery.setVisibility(checkBoxSelected);
            controller.getTransactionQueryCollection().add(txbitMaskQuery);
        }

        // Add created items to grid pane
        layersGridPane.addRow(currentIndex + 1, layerIdText,
                visibilityCheckBox, queryTextArea, descriptionTextArea, vxCheckBox, txCheckBox);
    }

    public synchronized void setLayers(final BitMaskQuery[] vxLayers, final BitMaskQuery[] txLayers) {
        Platform.runLater(() -> {
            controller.getVertexQueryCollection().clear();
            controller.getTransactionQueryCollection().clear();
            updateLayers(vxLayers, txLayers);
        });
    }

    private void updateLayers(final BitMaskQuery[] vxQueries, final BitMaskQuery[] txQueries) {
        synchronized (this) {
            layersGridPane.getChildren().removeIf(node -> GridPane.getRowIndex(node) > 0);
            for (int position = 0; position < Math.max(vxQueries.length, txQueries.length); position++) {
                final BitMaskQuery vxQuery = vxQueries[position];
                final BitMaskQuery txQuery = txQueries[position];
                if (vxQuery == null && txQuery == null) {
                    continue;
                }

                final int queryIndex = vxQuery != null ? vxQuery.getIndex() : txQuery.getIndex();
                final boolean queryVisibility = vxQuery != null ? vxQuery.getVisibility() : txQuery.getVisibility();
                final String queryString = vxQuery != null ? vxQuery.getQueryString() : txQuery.getQueryString();
                final String queryDescription = vxQuery != null ? vxQuery.getDescription() : txQuery.getDescription();
                createLayer(queryIndex, queryVisibility, queryString, queryDescription, vxQuery != null, txQuery != null);
            }
        }
    }

    /**
     * Set the layers to the defaults.
     */
    public synchronized void setDefaultLayers() {
        controller.getVertexQueryCollection().setDefaultQueries();
        controller.getTransactionQueryCollection().setDefaultQueries();
        setLayers(BitMaskQueryCollection.DEFAULT_VX_QUERIES, BitMaskQueryCollection.DEFAULT_TX_QUERIES);
    }
}
