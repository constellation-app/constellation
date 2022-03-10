/*
 * Copyright 2010-2021 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.graph.value.utilities.ExpressionUtilities;
import au.gov.asd.tac.constellation.views.layers.query.BitMaskQuery;
import au.gov.asd.tac.constellation.views.layers.query.BitMaskQueryCollection;
import au.gov.asd.tac.constellation.views.layers.query.Query;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import org.apache.commons.lang3.StringUtils;

/**
 * Layers View Pane.
 *
 * @author aldebaran30701
 */
public class LayersViewPane extends BorderPane {

    private static final Logger LOGGER = Logger.getLogger(LayersViewPane.class.getName());
    private final LayersViewController controller;
    private final GridPane layersGridPane;
    protected final VBox layersViewPane;
    protected final VBox noGraphPane;
    private final MenuBar options;

    private static final String QUERY_WARNING_STYLE = "-fx-background-color: rgba(241,74,74,0.85);";
    private static final String QUERY_DEFAULT_STYLE = "-fx-shadow-highlight-color; -fx-text-box-border; -fx-control-inner-background;";

    public LayersViewPane(final LayersViewController controller) {

        // create controller
        this.controller = controller;

        // create layer headings
        final Label layerIdHeadingText = new Label("Layer\nID");
        final Label visibilityHeadingText = new Label("Visibility");
        final Label vxqueryHeadingText = new Label("Vertex Query");
        final Label txqueryHeadingText = new Label("Transaction Query");
        final Label descriptionHeadingText = new Label("Description");

        this.options = new MenuBar();

        // create gridpane and alignments
        layersGridPane = new GridPane();
        layersGridPane.setHgap(5);
        layersGridPane.setVgap(5);
        layersGridPane.setPadding(new Insets(0, 10, 10, 10));
        layersGridPane.addRow(0, layerIdHeadingText, visibilityHeadingText,
                vxqueryHeadingText, txqueryHeadingText, descriptionHeadingText);

        // set heading alignments
        GridPane.setMargin(layerIdHeadingText, new Insets(15, 0, 0, 0));
        layerIdHeadingText.setTextAlignment(TextAlignment.CENTER);
        layerIdHeadingText.setMinWidth(40);
        layerIdHeadingText.setMinHeight(25);
        layerIdHeadingText.setPrefWidth(30);
        visibilityHeadingText.setPrefWidth(55);
        visibilityHeadingText.setMinWidth(75);
        vxqueryHeadingText.setPrefWidth(10000);
        vxqueryHeadingText.setMinWidth(80);
        txqueryHeadingText.setPrefWidth(10000);
        txqueryHeadingText.setMinWidth(80);
        descriptionHeadingText.setPrefWidth(10000);
        descriptionHeadingText.setMinWidth(80);

        // set default layers
        controller.getVxQueryCollection().setDefaultQueries();
        controller.getTxQueryCollection().setDefaultQueries();
        setLayers(BitMaskQueryCollection.DEFAULT_VX_QUERIES, BitMaskQueryCollection.DEFAULT_TX_QUERIES);

        // add layers grid and options to pane
        this.layersViewPane = new VBox(5, layersGridPane, options);

        // create layout bindings
        layersViewPane.prefWidthProperty().bind(this.widthProperty());
        options.prefWidthProperty().bind(layersViewPane.widthProperty());

        this.setCenter(layersViewPane);

        // add layers grid and options to pane
        this.noGraphPane = new NoGraphPane();

        // create layout bindings
        noGraphPane.prefWidthProperty().bind(this.widthProperty());
    }

    protected void createLayer(final int currentIndex, final boolean checkBoxSelected, final String vxQuery, final String txQuery, final String description, final boolean showVertices, final boolean showTransactions) {

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
            syncLayers();
            controller.execute();
            controller.writeState();
        });

        // Layer Query Entry
        final Node vxQueryTextArea = new TextArea();
        ((TextArea) vxQueryTextArea).setPrefRowCount(1);
        ((TextArea) vxQueryTextArea).setText(vxQuery);
        ((TextArea) vxQueryTextArea).setWrapText(true);
        ((TextArea) vxQueryTextArea).setStyle(testQueryValidity(vxQuery) ? QUERY_DEFAULT_STYLE : QUERY_WARNING_STYLE);
        ((TextArea) vxQueryTextArea).focusedProperty().addListener((observable, oldVal, newVal) -> {
            if (!newVal) {
                syncLayers();
                controller.writeState();
            }
        });

        // Layer tx Query Entry
        final Node txQueryTextArea = new TextArea();
        ((TextArea) txQueryTextArea).setPrefRowCount(1);
        ((TextArea) txQueryTextArea).setText(txQuery);
        ((TextArea) txQueryTextArea).setWrapText(true);
        ((TextArea) txQueryTextArea).setStyle(testQueryValidity(txQuery) ? QUERY_DEFAULT_STYLE : QUERY_WARNING_STYLE);
        ((TextArea) txQueryTextArea).focusedProperty().addListener((observable, oldVal, newVal) -> {
            if (!newVal) {
                syncLayers();
                controller.writeState();
            }
        });

        // Layer Description Entry
        final Node descriptionTextArea = new TextArea();
        ((TextArea) descriptionTextArea).setPrefRowCount(1);
        ((TextArea) descriptionTextArea).setText(description);
        ((TextArea) descriptionTextArea).setWrapText(true);
        ((TextArea) descriptionTextArea).focusedProperty().addListener((observable, oldVal, newVal) -> {
            if (!newVal) {
                syncLayers();
                controller.writeState();
            }
        });

        // Layer Deletion
        final Node deleteButton = new Button("Delete");
        ((Button) deleteButton).setMinWidth(60);
        deleteButton.setOnMouseClicked(e -> {
            controller.getVxQueryCollection().removeQueryAndSort(currentIndex);
            controller.getTxQueryCollection().removeQueryAndSort(currentIndex);
            controller.removeBitmaskFromElements(currentIndex);
            controller.shuffleElementBitmasks(currentIndex);
            controller.writeState();
            controller.execute();
        });

        // Default layer Handling
        if (currentIndex == 0) {
            layerIdText.setDisable(true);
            ((CheckBox) visibilityCheckBox).setSelected(true);
            visibilityCheckBox.setDisable(true);
            vxQueryTextArea.setDisable(true);
            txQueryTextArea.setDisable(true);
            descriptionTextArea.setDisable(true);
            deleteButton.setDisable(true);
        }

        // Adding to collections based on checkbox preferences.
        if (showVertices) {
            final BitMaskQuery vxbitMaskQuery = new BitMaskQuery(new Query(GraphElementType.VERTEX, vxQuery), currentIndex, description);
            vxbitMaskQuery.setVisibility(checkBoxSelected);
            controller.getVxQueryCollection().add(vxbitMaskQuery);
        }

        if (showTransactions) {
            final BitMaskQuery txbitMaskQuery = new BitMaskQuery(new Query(GraphElementType.TRANSACTION, txQuery), currentIndex, description);
            txbitMaskQuery.setVisibility(checkBoxSelected);
            controller.getTxQueryCollection().add(txbitMaskQuery);
        }

        // Only show the error label, never hide
        if (!options.getQueryErrorLabelVisibility()) {
            options.displayQueryErrorLabel(!testQueryValidity(vxQuery) || !testQueryValidity(txQuery));
        }

        // Add created items to grid pane
        layersGridPane.addRow(currentIndex + 1, layerIdText, visibilityCheckBox, vxQueryTextArea, txQueryTextArea, descriptionTextArea, deleteButton);
    }

    public synchronized void setLayers(final BitMaskQuery[] vxLayers, final BitMaskQuery[] txLayers) {
        Platform.runLater(() -> {
            controller.getVxQueryCollection().clear();
            controller.getTxQueryCollection().clear();
            updateLayers(vxLayers, txLayers);
        });
    }

    private void updateLayers(final BitMaskQuery[] vxQueries, final BitMaskQuery[] txQueries) {
        synchronized (this) {
            layersGridPane.getChildren().removeIf(node -> GridPane.getRowIndex(node) > 0);
            options.displayQueryErrorLabel(false);
            final int iteratorEnd = Math.max(vxQueries.length, txQueries.length);
            for (int position = 0; position < iteratorEnd; position++) {
                final BitMaskQuery vxQuery = vxQueries[position];
                final BitMaskQuery txQuery = txQueries[position];
                if (vxQuery == null && txQuery == null) {
                    continue;
                }

                final int queryIndex = vxQuery != null ? vxQuery.getIndex() : txQuery.getIndex();
                final boolean queryVisibility = vxQuery != null ? vxQuery.isVisible() : txQuery.isVisible();
                final String vxqueryString = vxQuery != null ? vxQuery.getQueryString() : StringUtils.EMPTY;
                final String txqueryString = txQuery != null ? txQuery.getQueryString() : StringUtils.EMPTY;
                final String queryDescription = vxQuery != null ? vxQuery.getDescription() : txQuery.getDescription();
                createLayer(queryIndex, queryVisibility, vxqueryString, txqueryString, queryDescription, vxQuery != null, txQuery != null);
            }
        }
    }

    /**
     * Set the layers to the defaults.
     */
    public synchronized void setDefaultLayers() {
        controller.getVxQueryCollection().setDefaultQueries();
        controller.getTxQueryCollection().setDefaultQueries();
        setLayers(BitMaskQueryCollection.DEFAULT_VX_QUERIES, BitMaskQueryCollection.DEFAULT_TX_QUERIES);
    }

    /**
     * Tests if the text parses correctly or if the query is empty Allows null
     * values as that is how inactive layers are represented
     *
     * @param queryText the expression string to test
     * @return true if the query is valid or null, false otherwise
     */
    private boolean testQueryValidity(final String queryText) {
        return queryText == null || ExpressionUtilities.testQueryValidity(queryText);
    }

    private synchronized void syncLayers() {
        int index = 0;
        boolean visible = false;
        boolean hasErrors = false;
        String vxQuery = null;
        String txQuery = null;
        String description = null;

        for (final Node item : layersGridPane.getChildren()) {
            if (GridPane.getRowIndex(item) == 0) {
                continue;
            }

            switch (GridPane.getColumnIndex(item)) {
                case 0:
                    index = Integer.parseInt(((Label) item).getText());
                    break;
                case 1:
                    visible = ((CheckBox) item).isSelected();
                    break;
                case 2:
                    vxQuery = ((TextArea) item).getText();
                    boolean validQuery = testQueryValidity(vxQuery);
                    if (!hasErrors) {
                        hasErrors = !validQuery;
                        options.displayQueryErrorLabel(hasErrors);
                    }
                    ((TextArea) item).setStyle(validQuery ? QUERY_DEFAULT_STYLE : QUERY_WARNING_STYLE);
                    break;
                case 3:
                    txQuery = ((TextArea) item).getText();
                    boolean validQuery2 = testQueryValidity(txQuery);
                    if (!hasErrors) {
                        hasErrors = !validQuery2;
                        options.displayQueryErrorLabel(hasErrors);
                    }
                    ((TextArea) item).setStyle(validQuery2 ? QUERY_DEFAULT_STYLE : QUERY_WARNING_STYLE);
                    break;
                case 4:
                    description = ((TextArea) item).getText();
                    break;
                default:
                    break;
            }

            if (description != null) {
                final BitMaskQuery txQueryObject = new BitMaskQuery(new Query(GraphElementType.TRANSACTION, txQuery), index, description);
                txQueryObject.setVisibility(visible);
                controller.getTxQueryCollection().add(txQueryObject);

                final BitMaskQuery vxQueryObject = new BitMaskQuery(new Query(GraphElementType.VERTEX, vxQuery), index, description);
                vxQueryObject.setVisibility(visible);
                controller.getVxQueryCollection().add(vxQueryObject);

                if (StringUtils.isBlank(vxQuery) && StringUtils.isNotBlank(txQuery)) {
                    controller.getVxQueryCollection().removeQuery(index);
                } else if (StringUtils.isBlank(txQuery) && StringUtils.isNotBlank(vxQuery)) {
                    controller.getTxQueryCollection().removeQuery(index);
                } else {
                    // Do nothing
                }
                description = null;
            }
        }
    }

    /**
     * Set the pane enabled will switch between the real layers pane and a
     * message pane notifying the user that a graph is required.
     *
     * @param enable true if there is a graph
     */
    protected void setEnabled(final boolean enable) {
        Platform.runLater(() -> this.setCenter(enable ? layersViewPane : noGraphPane));
    }
}
