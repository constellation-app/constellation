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
package au.gov.asd.tac.constellation.views.layers.components;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.value.utilities.ExpressionUtilities;
import au.gov.asd.tac.constellation.views.layers.LayersViewController;
import au.gov.asd.tac.constellation.views.layers.query.BitMaskQuery;
import au.gov.asd.tac.constellation.views.layers.query.BitMaskQueryCollection;
import au.gov.asd.tac.constellation.views.layers.query.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.Exceptions;

/**
 * Layers View Pane.
 *
 * @author aldebaran30701
 */
public class LayersViewPane extends BorderPane {
    
    
    private static final Logger LOGGER = Logger.getLogger(LayersViewPane.class.getName());
    private final LayersViewController controller;
    //private final GridPane layersGridPane;
    protected VBox layersViewPane;
    protected final VBox noGraphPane;
    private final MenuBar options;
    
    private final TitledPane layersHeading;
    private final ScrollPane attributeScrollPane;
    
    private static final int SCROLLPANE_HEIGHT = 1000;
    private static final int SCROLLPANE_VIEW_WIDTH = 400;
    private static final int SCROLLPANE_VIEW_HEIGHT = 900;

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
//        layersGridPane = new GridPane();
//        layersGridPane.setHgap(5);
//        layersGridPane.setVgap(5);
//        layersGridPane.setPadding(new Insets(0, 10, 10, 10));
//        layersGridPane.addRow(0, layerIdHeadingText, visibilityHeadingText,
//                vxqueryHeadingText, txqueryHeadingText, descriptionHeadingText);

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
        //setLayers(BitMaskQueryCollection.DEFAULT_VX_QUERIES, BitMaskQueryCollection.DEFAULT_TX_QUERIES);
        
        layersHeading = new TitledPane();
        layersHeading.setText("Layers");
        layersHeading.setExpanded(true);
        layersHeading.setCollapsible(false);
        layersHeading.getStyleClass().add("titled-pane-heading");
        
        // Add Layers Dynamically
        final VBox layersnew = new VBox();
        //setLayers( controller.getVxQueryCollection().getQueries(), controller.getTxQueryCollection().getQueries());
        createLayers(layersnew, controller.getVxQueryCollection().getQueries(), controller.getTxQueryCollection().getQueries());
        
        // A scroll pane to hold the attribute boxes
        attributeScrollPane = new ScrollPane();
        attributeScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        attributeScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        attributeScrollPane.setMaxWidth(Double.MAX_VALUE);
        attributeScrollPane.setContent(layersnew);
        attributeScrollPane.setPrefViewportWidth(SCROLLPANE_VIEW_WIDTH);
        attributeScrollPane.setPrefViewportHeight(SCROLLPANE_VIEW_HEIGHT);
        attributeScrollPane.setFitToWidth(true);
        attributeScrollPane.setPrefHeight(SCROLLPANE_HEIGHT);
        
        // add layers grid and options to pane
        this.layersViewPane = new VBox(layersHeading, attributeScrollPane, options);

        // create layout bindings
        layersViewPane.prefWidthProperty().bind(this.widthProperty());
        options.prefWidthProperty().bind(layersViewPane.widthProperty());

        this.setCenter(layersViewPane);

        // add layers grid and options to pane
        this.noGraphPane = new NoGraphPane();

        // create layout bindings
        noGraphPane.prefWidthProperty().bind(this.widthProperty());
    }

    public void createLayer(final int currentIndex, final boolean checkBoxSelected, final String vxQuery, final String txQuery, final String description, final boolean showVertices, final boolean showTransactions) {

//        // Layer ID
//        final Label layerIdText = new Label(String.format("%02d", currentIndex));
//        layerIdText.setMinWidth(30);
//        layerIdText.setPrefWidth(40);
//        layerIdText.setTextAlignment(TextAlignment.CENTER);
//        layerIdText.setPadding(new Insets(0, 0, 0, 10));
//
//        // Layer Visibility
//        final Node visibilityCheckBox = new CheckBox();
//        ((CheckBox) visibilityCheckBox).setMinWidth(60);
//        ((CheckBox) visibilityCheckBox).setPadding(new Insets(0, 30, 0, 15));
//        ((CheckBox) visibilityCheckBox).setSelected(checkBoxSelected);
//        visibilityCheckBox.setOnMouseClicked(e -> {
//            controller.execute();
//            controller.writeState();
//        });
//
//        // Layer Query Entry
//        final Node vxQueryTextArea = new TextArea();
//        ((TextArea) vxQueryTextArea).setPrefRowCount(1);
//        ((TextArea) vxQueryTextArea).setText(vxQuery);
//        ((TextArea) vxQueryTextArea).setWrapText(true);
//        ((TextArea) vxQueryTextArea).setStyle(testQueryValidity(vxQuery) ? QUERY_DEFAULT_STYLE : QUERY_WARNING_STYLE);
//        ((TextArea) vxQueryTextArea).focusedProperty().addListener((observable, oldVal, newVal) -> {
//            if (!newVal) {
//                controller.writeState();
//            }
//        });
//
//        // Layer tx Query Entry
//        final Node txQueryTextArea = new TextArea();
//        ((TextArea) txQueryTextArea).setPrefRowCount(1);
//        ((TextArea) txQueryTextArea).setText(txQuery);
//        ((TextArea) txQueryTextArea).setWrapText(true);
//        ((TextArea) txQueryTextArea).setStyle(testQueryValidity(txQuery) ? QUERY_DEFAULT_STYLE : QUERY_WARNING_STYLE);
//        ((TextArea) txQueryTextArea).focusedProperty().addListener((observable, oldVal, newVal) -> {
//            if (!newVal) {
//                controller.writeState();
//            }
//        });
//
//        // Layer Description Entry
//        final Node descriptionTextArea = new TextArea();
//        ((TextArea) descriptionTextArea).setPrefRowCount(1);
//        ((TextArea) descriptionTextArea).setText(description);
//        ((TextArea) descriptionTextArea).setWrapText(true);
//        ((TextArea) descriptionTextArea).focusedProperty().addListener((observable, oldVal, newVal) -> {
//            if (!newVal) {
//                controller.writeState();
//            }
//        });
//
//        // Layer Deletion
//        final Node deleteButton = new Button("Delete");
//        ((Button) deleteButton).setMinWidth(60);
//        deleteButton.setOnMouseClicked(e -> {
//            controller.deleteLayer(currentIndex);
//        });
//
//        // Default layer Handling
//        if (currentIndex == 0) {
//            layerIdText.setDisable(true);
//            ((CheckBox) visibilityCheckBox).setSelected(true);
//            visibilityCheckBox.setDisable(true);
//            vxQueryTextArea.setDisable(true);
//            txQueryTextArea.setDisable(true);
//            descriptionTextArea.setDisable(true);
//            deleteButton.setDisable(true);
//        }
//
//        // Adding to collections based on checkbox preferences.
//        if (showVertices) {
//            final BitMaskQuery vxbitMaskQuery = new BitMaskQuery(new Query(GraphElementType.VERTEX, vxQuery), currentIndex, description);
//            vxbitMaskQuery.setVisibility(checkBoxSelected);
//            controller.getVxQueryCollection().add(vxbitMaskQuery);
//        }
//
//        if (showTransactions) {
//            final BitMaskQuery txbitMaskQuery = new BitMaskQuery(new Query(GraphElementType.TRANSACTION, txQuery), currentIndex, description);
//            txbitMaskQuery.setVisibility(checkBoxSelected);
//            controller.getTxQueryCollection().add(txbitMaskQuery);
//        }
//
//        // Only show the error label, never hide
//        if (!options.getQueryErrorLabelVisibility()) {
//            options.displayQueryErrorLabel(!testQueryValidity(vxQuery) || !testQueryValidity(txQuery));
//        }

        // Add created items to grid pane
        //layersGridPane.addRow(currentIndex + 1, layerIdText, visibilityCheckBox, vxQueryTextArea, txQueryTextArea, descriptionTextArea, deleteButton);
    }

    public synchronized void setLayers(final BitMaskQuery[] vxLayers, final BitMaskQuery[] txLayers) {
        
                    controller.getVxQueryCollection().clear();
        controller.getTxQueryCollection().clear();
        controller.getVxQueryCollection().setQueries(vxLayers);
        controller.getTxQueryCollection().setQueries(txLayers);
        Platform.runLater(() -> {

            options.displayQueryErrorLabel(false);
            
            final VBox oldLayers = (VBox) attributeScrollPane.getContent();
            createLayers(oldLayers, vxLayers, txLayers);
            attributeScrollPane.setContent(oldLayers);
            this.layersViewPane = new VBox(layersHeading, attributeScrollPane, options);
            // trigger refresh using enabled method
            setEnabled(true);
        });
    }

    /**
     * Set the layers to the defaults.
     */
    public synchronized void setDefaultLayers() {
        CountDownLatch cdl1 = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller.getVxQueryCollection().setDefaultQueries();
            controller.getTxQueryCollection().setDefaultQueries();
            options.displayQueryErrorLabel(false);
            
            final VBox oldLayers = (VBox) attributeScrollPane.getContent();
            createLayers(oldLayers, controller.getVxQueryCollection().getQueries(), controller.getTxQueryCollection().getQueries());
            attributeScrollPane.setContent(oldLayers);
            this.layersViewPane = new VBox(layersHeading, attributeScrollPane, options);
            // trigger refresh using enabled method
            setEnabled(true);
            cdl1.countDown();
        });
        try {
            cdl1.await();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
//
//    /**
//     * Tests if the text parses correctly or if the query is empty Allows null
//     * values as that is how inactive layers are represented
//     *
//     * @param queryText the expression string to test
//     * @return true if the query is valid or null, false otherwise
//     */
//    private boolean testQueryValidity(final String queryText) {
//        return queryText == null || ExpressionUtilities.testQueryValidity(queryText);
//    }


    /**
     * Set the pane enabled will switch between the real layers pane and a
     * message pane notifying the user that a graph is required.
     *
     * @param enable true if there is a graph
     */
    public void setEnabled(final boolean enable) {
        Platform.runLater(() -> this.setCenter(enable ? layersViewPane : noGraphPane));
    }
    
    private void createLayers(final VBox layersnew, final BitMaskQuery[] vxQueries, final BitMaskQuery[] txQueries) {
        final List<Node> layers = new ArrayList<>();
        
        final int iteratorEnd = Math.max(getHighestQueryIndex(vxQueries), getHighestQueryIndex(txQueries)) + 1;
        boolean isQueryActive = false;
        
        for (int position = 0; position < iteratorEnd; position++) {
            final BitMaskQuery vxQuery = vxQueries[position];
            final BitMaskQuery txQuery = txQueries[position];
            if (vxQuery == null && txQuery == null) {
                continue;
                // TODO: Can we break here? both null meaning no layer - should be in order.
            }

            final int queryIndex = vxQuery != null ? vxQuery.getIndex() : txQuery.getIndex();
            final boolean queryVisibility = vxQuery != null ? vxQuery.isVisible() : txQuery.isVisible();
            final String vxqueryString = vxQuery != null ? vxQuery.getQueryString() : StringUtils.EMPTY;
            final String txqueryString = txQuery != null ? txQuery.getQueryString() : StringUtils.EMPTY;
            final String queryDescription = vxQuery != null ? vxQuery.getDescription() : txQuery.getDescription();
            //createLayer(queryIndex, queryVisibility, vxqueryString, txqueryString, queryDescription, vxQuery != null, txQuery != null);
            final Query q;
            if(StringUtils.isEmpty(txqueryString) && StringUtils.isNotEmpty(vxqueryString)) {
                // VX query found
                q = new Query(GraphElementType.VERTEX, vxqueryString);

            }else{
                // TX query found
                q = new Query(GraphElementType.TRANSACTION, txqueryString);
            }
            
            if(position != 0 && queryVisibility){
                // not default layer and a layer is visible.
                isQueryActive = true;
            }

            // TODO: It rebuilds the whole pane whenever the text is changed - partial update?
            final BitMaskQuery bmq = new BitMaskQuery(q, position, queryDescription);
            bmq.setVisibility(queryVisibility);

            //layers.add(tp);

            if(layersnew.getChildren().size() -1 < queryIndex){
                LOGGER.log(Level.WARNING, "Creating new layer: " + queryIndex + " - current layer count = " + (layersnew.getChildren().size() - 1));
                LayerTitlePane tp = new LayerTitlePane(queryIndex,queryDescription, bmq);
                layersnew.getChildren().add(queryIndex, tp);
            } else{
                LOGGER.log(Level.WARNING, "Using existing layer: " + queryIndex + " - current layer count = " + (layersnew.getChildren().size() - 1));
                LayerTitlePane oldTp = (LayerTitlePane) layersnew.getChildren().remove(queryIndex); // no 0 in list of 0
                oldTp.setDescription(queryDescription);
                oldTp.setQuery(bmq);
                layersnew.getChildren().add(queryIndex, oldTp);
            }


        }

        // remove the remaining old layers.
        final int totalLayers = layersnew.getChildren().size();
        if(totalLayers > iteratorEnd) {
            layersnew.getChildren().remove(iteratorEnd, totalLayers); // was -1
        }
        
        // set layer 0 selected when no other layer is enabled.
        LayerTitlePane oldTp = (LayerTitlePane) layersnew.getChildren().remove(0);
        oldTp.setSelected(!isQueryActive);
        layersnew.getChildren().add(0, oldTp);
        
                
        
        //layersnew.getChildren().clear();
        //layersnew.getChildren().addAll(layers);
    }
    
    
    
    public int getHighestQueryIndex(final BitMaskQuery[] queries) {
        int highestIndex = 0;
        for (final BitMaskQuery bitMaskQuery : queries) {
            if (bitMaskQuery != null) {
                highestIndex = bitMaskQuery.getIndex();
            }
        }
        return highestIndex;
    }
}
