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
import au.gov.asd.tac.constellation.graph.value.expression.ExpressionParser;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.views.layers.query.BitMaskQuery;
import au.gov.asd.tac.constellation.views.layers.query.BitMaskQueryCollection;
import au.gov.asd.tac.constellation.views.layers.query.Query;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import org.apache.commons.lang3.StringUtils;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;

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
    private final HBox options;
    private final Label errorLabel;
    private static final String QUERY_WARNING_TEXT = "Invalid query structure";
    private static final String QUERY_WARNING_STYLE = "-fx-background-color: rgba(241,74,74,0.85);";
    private static final String QUERY_DEFAULT_STYLE = "-fx-shadow-highlight-color; -fx-text-box-border; -fx-control-inner-background;";

    public LayersViewPane(final LayersViewController controller) {

        // create controller
        this.controller = controller;

        // create help button
        final Button helpButton = new Button("", new ImageView(UserInterfaceIconProvider.HELP.buildImage(16, ConstellationColor.BLUEBERRY.getJavaColor())));
        helpButton.paddingProperty().set(new Insets(2, 0, 0, 0));
        helpButton.setTooltip(new Tooltip("Display help for Layers View"));
        helpButton.setOnAction(event -> new HelpCtx(LayersViewTopComponent.class.getName()).display());

        // Get rid of the ugly button look so the icon stands alone.
        helpButton.setStyle("-fx-border-color: transparent;-fx-background-color: transparent;");

        // create layer headings
        final Label layerIdHeadingText = new Label("Layer\nID");
        final Label visibilityHeadingText = new Label("Visibility");
        final Label vxqueryHeadingText = new Label("Vertex Query");
        final Label txqueryHeadingText = new Label("Transaction Query");
        final Label descriptionHeadingText = new Label("Description");

        errorLabel = new Label(QUERY_WARNING_TEXT);
        errorLabel.setPadding(new Insets(2, 0, 0, 0));
        errorLabel.setTextAlignment(TextAlignment.CENTER);
        errorLabel.setStyle("-fx-text-fill: rgba(241,74,74,0.85);");
        errorLabel.setVisible(false);
        HBox.setHgrow(errorLabel, Priority.ALWAYS);

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

        // create options
        final Button addButton = new Button("Add New Layer");
        addButton.setAlignment(Pos.CENTER_RIGHT);
        addButton.setTooltip(new Tooltip("CTRL + ALT + L"));
        addButton.setOnAction(event -> {
            if (layersGridPane.getRowCount() <= BitMaskQueryCollection.MAX_QUERY_AMT) {
                final int newQueryIndex = Math.max(controller.getTxQueryCollection().getHighestQueryIndex() + 1,
                        controller.getVxQueryCollection().getHighestQueryIndex() + 1);
                createLayer(newQueryIndex, false, null, StringUtils.EMPTY, StringUtils.EMPTY, true, true);
                controller.writeState();
            } else {
                NotifyDisplayer.display("You cannot have more than " + BitMaskQueryCollection.MAX_QUERY_AMT + " layers", NotifyDescriptor.WARNING_MESSAGE);
                LOGGER.log(Level.INFO, "Layer count maximum reached. Maximum is currently: {0}", BitMaskQueryCollection.MAX_QUERY_AMT);
            }
            event.consume();
        });
        HBox.setHgrow(addButton, Priority.ALWAYS);

        final Button deselectAllButton = new Button("Deselect All Layers");
        deselectAllButton.setTooltip(new Tooltip("CTRL + ALT + D"));
        deselectAllButton.setAlignment(Pos.CENTER_RIGHT);
        deselectAllButton.setOnAction(event -> {
            controller.getVxQueryCollection().setVisibilityOnAll(false);
            controller.getTxQueryCollection().setVisibilityOnAll(false);
            controller.execute();
            controller.writeState();
            event.consume();
        });
        HBox.setHgrow(deselectAllButton, Priority.ALWAYS);

        this.options = new HBox(5, addButton, deselectAllButton, helpButton, errorLabel);
        options.setAlignment(Pos.TOP_LEFT);
        options.setPadding(new Insets(0, 0, 0, 10));

        // add layers grid and options to pane
        this.layersViewPane = new VBox(5, layersGridPane, options);

        // create layout bindings
        layersViewPane.prefWidthProperty().bind(this.widthProperty());
        options.prefWidthProperty().bind(layersViewPane.widthProperty());

        this.setCenter(layersViewPane);
        
        final Label noGraphLabel = new Label("Open or create a graph to enable the Layers View.");
        // add layers grid and options to pane
        this.noGraphPane = new VBox(5, noGraphLabel, helpButton);
        noGraphPane.setPadding(new Insets(0,0,0,0));

        // create layout bindings
        noGraphPane.prefWidthProperty().bind(this.widthProperty());
    }

    private void createLayer(final int currentIndex, final boolean checkBoxSelected, final String vxQuery, final String txQuery, final String description, final boolean showVertices, final boolean showTransactions) {

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

        if (!errorLabel.isVisible()) {
            // check tx and vx
            errorLabel.setVisible(!testQueryValidity(vxQuery) || !testQueryValidity(txQuery));
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
            errorLabel.setVisible(false);
            final int iteratorEnd = Math.max(vxQueries.length, txQueries.length);
            for (int position = 0; position < iteratorEnd; position++) {
                final BitMaskQuery vxQuery = vxQueries[position];
                final BitMaskQuery txQuery = txQueries[position];
                if (vxQuery == null && txQuery == null) {
                    continue;
                }

                final int queryIndex = vxQuery != null ? vxQuery.getIndex() : txQuery.getIndex();
                final boolean queryVisibility = vxQuery != null ? vxQuery.getVisibility() : txQuery.getVisibility();
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
     * Tests if the Expression String parses correctly. Does not check if there
     * are returnable results. Suppresses the error dialogs within the parse
     * method.
     *
     * @param queryText the expression string to test
     * @return true if the query is valid, false otherwise
     */
    private boolean testQueryValidity(final String queryText) {
        boolean validity = true;
        if (queryText == null) {
            return validity;
        }
        ExpressionParser.hideErrorPrompts(true);
        if (ExpressionParser.parse(queryText) == null) {
            validity = false;
        }
        ExpressionParser.hideErrorPrompts(false);
        return validity;
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
                        errorLabel.setVisible(hasErrors);
                    }
                    ((TextArea) item).setStyle(validQuery ? QUERY_DEFAULT_STYLE : QUERY_WARNING_STYLE);
                    break;
                case 3:
                    txQuery = ((TextArea) item).getText();
                    boolean validQuery2 = testQueryValidity(txQuery);
                    if (!hasErrors) {
                        hasErrors = !validQuery2;
                        errorLabel.setVisible(hasErrors);
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
