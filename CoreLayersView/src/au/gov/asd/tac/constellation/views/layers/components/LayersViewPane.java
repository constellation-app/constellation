/*
 * Copyright 2010-2025 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.views.NoGraphPane;
import au.gov.asd.tac.constellation.views.layers.LayersViewController;
import au.gov.asd.tac.constellation.views.layers.query.BitMaskQuery;
import au.gov.asd.tac.constellation.views.layers.query.Query;
import au.gov.asd.tac.constellation.views.layers.utilities.LayersUtilities;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
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
    protected VBox layersViewVBox;
    protected final VBox noGraphPane;
    private final MenuBar options;
    private final ComboBox modeBox;
    
    private final TitledPane layersHeading;
    private final ScrollPane attributeScrollPane;
    
    private static final int SCROLLPANE_HEIGHT = 1000;
    private static final int SCROLLPANE_VIEW_WIDTH = 400;
    private static final int SCROLLPANE_VIEW_HEIGHT = 900;

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
        
        layersHeading = new TitledPane();
        layersHeading.setText(" ");
        
        final String[] modeList = {"UNION mode" , "INTERSECTION mode"};
        modeBox = new ComboBox(FXCollections.observableArrayList(modeList));
        modeBox.getSelectionModel().selectFirst();
        modeBox.valueProperty().addListener((final ObservableValue observable, final Object oldValue, final Object newValue) -> 
            LayersViewController.getDefault().updateQueries(GraphManager.getDefault().getActiveGraph())
        );
        final BorderPane border = new BorderPane();
        final Label layersLabel = new Label("Layers ");
        layersLabel.setMinSize(60, 15);
        layersLabel.setPadding(new Insets(2,1,0,1));
        layersLabel.setStyle("-fx-font-size: 1.25em ");
        border.setPadding(new Insets(3,5,2,5));
        border.setLeft(layersLabel);
        border.setRight(modeBox);
        border.minWidthProperty().bind(layersHeading.widthProperty().subtract(10));
        
        layersHeading.setGraphic(border);
        layersHeading.setExpanded(true);
        layersHeading.setCollapsible(false);
        layersHeading.getStyleClass().add("titled-pane-heading");
        layersHeading.setId("title-style");
        
        // Add Layers Dynamically
        final VBox layersnew = new VBox();
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
        this.layersViewVBox = new VBox(layersHeading, attributeScrollPane, options);

        // create layout bindings
        layersViewVBox.prefWidthProperty().bind(this.widthProperty());
        options.prefWidthProperty().bind(layersViewVBox.widthProperty());

        this.setCenter(layersViewVBox);

        // add layers grid and options to pane
        this.noGraphPane = new NoGraphPane("Layers View", LayersUtilities.createHelpButton());

        // create layout bindings
        noGraphPane.prefWidthProperty().bind(this.widthProperty());
    }

    /**
     * Refresh the layers pane and update each layer
     *
     * @param vxLayers
     * @param txLayers
     */
    public synchronized void setLayers(final BitMaskQuery[] vxLayers, final BitMaskQuery[] txLayers) {
        final CountDownLatch cdl1 = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller.getVxQueryCollection().clear();
            controller.getTxQueryCollection().clear();
            controller.getVxQueryCollection().setQueries(vxLayers);
            controller.getTxQueryCollection().setQueries(txLayers);
            
            final VBox oldLayers = (VBox) attributeScrollPane.getContent();
            createLayers(oldLayers, vxLayers, txLayers);
            attributeScrollPane.setContent(oldLayers);
            this.layersViewVBox = new VBox(layersHeading, attributeScrollPane, options);
            // trigger refresh using enabled method
            setEnabled(true);
            cdl1.countDown();
        });
        try {
            cdl1.await();
        } catch (final InterruptedException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Set the layers to the defaults.
     */
    public synchronized void setDefaultLayers() {
        final CountDownLatch cdl1 = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller.getVxQueryCollection().setDefaultQueries();
            controller.getTxQueryCollection().setDefaultQueries();
            
            final VBox oldLayers = new VBox();
            createLayers(oldLayers, controller.getVxQueryCollection().getQueries(), controller.getTxQueryCollection().getQueries());
            attributeScrollPane.setContent(oldLayers);
            this.layersViewVBox = new VBox(layersHeading, attributeScrollPane, options);
            // trigger refresh using enabled method
            setEnabled(true);
            cdl1.countDown();
        });
        try {
            cdl1.await();
        } catch (final InterruptedException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            Thread.currentThread().interrupt();
        } 
    }

    /**
     * Set the pane enabled will switch between the real layers pane and a
     * message pane notifying the user that a graph is required.
     *
     * @param enable true if there is a graph
     */
    public void setEnabled(final boolean enable) {
        Platform.runLater(() -> this.setCenter(enable ? layersViewVBox : noGraphPane));
    }

    /**
     * Recreate the layers
     *
     * @param layersnew
     * @param vxQueries
     * @param txQueries
     */
    private void createLayers(final VBox layersnew, final BitMaskQuery[] vxQueries, final BitMaskQuery[] txQueries) {
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

            // Set query description to either the vertex of transaction description if not null
            String queryDescription = StringUtils.EMPTY;
            if (vxQuery != null) {
                queryDescription = vxQuery.getDescription();
            } else if (txQuery != null) {
                queryDescription = txQuery.getDescription();
            }
            
            final Query q;
            if (StringUtils.isBlank(txqueryString) && StringUtils.isNotBlank(vxqueryString)) {
                // VX query found
                q = new Query(GraphElementType.VERTEX, vxqueryString);

            } else {
                // TX query found
                q = new Query(GraphElementType.TRANSACTION, txqueryString);
            }
            
            if (position != 0 && queryVisibility) {
                // not default layer and a layer is visible.
                isQueryActive = true;
            }

            final BitMaskQuery bmq = new BitMaskQuery(q, position, queryDescription);
            bmq.setVisibility(queryVisibility);
            
            if (position == 0 && !isQueryActive) {
                bmq.setVisibility(!isQueryActive);
            }

            if (layersnew.getChildren().size() - 1 < queryIndex) {
                final LayerTitlePane tp = new LayerTitlePane(queryIndex,queryDescription, bmq);
                layersnew.getChildren().add(queryIndex, tp);
            } else {
                final LayerTitlePane oldTp = (LayerTitlePane) layersnew.getChildren().remove(queryIndex); // no 0 in list of 0
                oldTp.setDescription(queryDescription);
                oldTp.setQuery(bmq);
                layersnew.getChildren().add(queryIndex, oldTp);
            }
        }

        // remove the remaining old layers.
        final int totalLayers = layersnew.getChildren().size();
        if (totalLayers > iteratorEnd) {
            layersnew.getChildren().remove(iteratorEnd, totalLayers);
        }
        
        // set layer 0 selected when no other layer is enabled.
        final LayerTitlePane oldTp = (LayerTitlePane) layersnew.getChildren().remove(0);
        oldTp.setSelected(!isQueryActive);
        oldTp.getQuery().setVisibility(!isQueryActive);
        layersnew.getChildren().add(0, oldTp);
    }

    /**
     * Get the index of the last query created
     *
     * @param queries
     * @return
     */
    public int getHighestQueryIndex(final BitMaskQuery[] queries) {
        int highestIndex = 0;
        for (final BitMaskQuery bitMaskQuery : queries) {
            if (bitMaskQuery != null) {
                highestIndex = bitMaskQuery.getIndex();
            }
        }
        return highestIndex;
    }
    
    public int getOptionsLayerMode() {
        return modeBox.getSelectionModel().getSelectedIndex();
    }
}
