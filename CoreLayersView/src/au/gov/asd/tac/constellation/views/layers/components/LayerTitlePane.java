/*
 * Copyright 2010-2022 Australian Signals Directorate
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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author aldebaran30701
 */
public class LayerTitlePane extends TitledPane {
    
    // Descriptions
    public static final String VERTEX_DESCRIPTION = "Enter a valid query for Vertices here.";
    public static final String TRANSACTION_DESCRIPTION = "Enter a valid query for Transactions here.";
    public static final String QUERY_DESCRIPTION = "Enter a query description here.";
    
    // Styles
    public static final String SELECTED_STYLE = "titled-pane-selected";
    public static final String SELECTED_INVALID_STYLE = "titled-pane-selected-invalid";
    public static final String MATCHED_STYLE = "titled-pane-matched";
    public static final String INVALID_STYLE = "titled-pane-invalid";
    
    public static final int MAX_DISPLAYED_CHARS = 45;
    
    // Components
    private final CheckBox enabled;
    private final Label label;
    
    private QueryInputPane vxQuery;
    private QueryInputPane txQuery;
    private QueryInputPane descinput;
    
    private BitMaskQuery query;
    
    private final ChangeListener<? super Boolean> enabledChanged;
    
    private final int layerId;
    private boolean isValid;

    public LayerTitlePane(final int layerId, final String layerName, final BitMaskQuery query) {
        this.query = query;
        isValid = testQueryValidity(query.getQueryString());
        enabled = new CheckBox();
        
        enabledChanged = (final ObservableValue<? extends Boolean> observable, final Boolean oldValue, final Boolean newValue) -> {
            recolourLayer();
            LayersViewController.getDefault().changeLayerVisibility(layerId, enabled.isSelected());
        };
        
        enabled.setSelected(query.isVisible());
        enabled.selectedProperty().addListener(enabledChanged);
        final String displayedLayerName = StringUtils.truncate(layerName, MAX_DISPLAYED_CHARS);
        label = new Label(StringUtils.isBlank(layerName) ? String.format("%d", layerId) : String.format("%-2.2s - %s",String.valueOf(layerId), displayedLayerName));
        this.layerId = layerId;
        final Button deleteButton = new Button("X");
        deleteButton.setMaxSize(10, 10);
        deleteButton.setStyle(deleteButton.getStyle() + "-fx-font-size:10;");
        deleteButton.setTooltip(new Tooltip("Delete this layer"));
        deleteButton.setOnMouseClicked(e -> delete());
        
        if (enabled.isSelected()) {
            getStyleClass().add(SELECTED_STYLE);
        } else if (!isValid) {
            getStyleClass().add(INVALID_STYLE);
            enabled.setDisable(true);
        }
        
        // Disable the default layer
        final boolean isCompleteGraphLayer = layerId == 0;
        this.setCollapsible(!isCompleteGraphLayer);
        enabled.setDisable(isCompleteGraphLayer || !isValid);
        deleteButton.setDisable(isCompleteGraphLayer);
        
        final Region region = new Region();
        region.setPrefSize(33.5, 7);
        final HBox box = isCompleteGraphLayer ? new HBox(8, region, label) : new HBox(10, enabled, label);
        final BorderPane border = new BorderPane();
        border.setLeft(box);
        border.setRight(isCompleteGraphLayer ? null : deleteButton);
        border.minWidthProperty().bind(this.widthProperty().subtract(40));
        HBox.setHgrow(label, Priority.ALWAYS);
        HBox.setHgrow(enabled, Priority.NEVER);
        setGraphic(border);
        setExpanded(false);
        
        setPadding(Insets.EMPTY);
        setTooltip(new Tooltip("Expand this layer to view more details"));
        setContent(createLayerDetailsPane(query));
    }

    /**
     * Create the pane containing the details for the layer
     *
     * @param query
     * @return vbox
     */
    private Pane createLayerDetailsPane(final BitMaskQuery query) {
        vxQuery = new QueryInputPane(this, "Vertex Query: ", VERTEX_DESCRIPTION, query.getQueryElementType() == GraphElementType.VERTEX ? query.getQueryString() : StringUtils.EMPTY, true);
        txQuery = new QueryInputPane(this, "Transaction Query: ", TRANSACTION_DESCRIPTION, query.getQueryElementType() == GraphElementType.TRANSACTION ? query.getQueryString() : StringUtils.EMPTY, true);
        descinput = new QueryInputPane(this, "Query Description", QUERY_DESCRIPTION, query.getDescription(), false);
        final VBox box = new VBox(10, descinput, vxQuery, txQuery);
        box.prefWidthProperty().bind(this.widthProperty());

        return box;
    }

    /**
     * Set the vertex or transaction query to the given query
     *
     * @param query
     */
    public void setQuery(final BitMaskQuery query) {
        this.query = query;
        
        enabled.selectedProperty().removeListener(enabledChanged);
        enabled.setSelected(query.isVisible());
        enabled.selectedProperty().addListener(enabledChanged);
        
        final GraphElementType type = query.getQueryElementType();
        
        if (type == GraphElementType.VERTEX) {
            setVxQuery(query.getQueryString());
        } else if (type == GraphElementType.TRANSACTION) {
            setTxQuery(query.getQueryString());
        }
    }

    /**
     * Set the current layer as selected
     *
     * @param value
     */
    public void setSelected(final boolean value) {
        enabled.selectedProperty().removeListener(enabledChanged);
        enabled.setSelected(value);
        enabled.selectedProperty().addListener(enabledChanged);
        recolourLayer();
    }

    /**
     * Set the current vertex query to the given query and check if it is valid
     *
     * @param query
     */
    public void setVxQuery(final String query) {
        vxQuery.setValidity(true);
        vxQuery.setQuery(query);
        recheckValidity();
        
        if (!isValid) {
            // when vx is invalid, 
            vxQuery.setValidity(false);
        }
    }

    /**
     * Set the current transaction query to the given query and check if it is valid
     *
     * @param query
     */
    public void setTxQuery(final String query) {
        txQuery.setValidity(true);
        txQuery.setQuery(query);
        recheckValidity();
        if (!isValid) {
            // when tx is invalid, 
            txQuery.setValidity(false);
        }
    }

    /**
     * Set the description of the current query
     *
     * @param description
     */
    public void setDescription(final String description) {
        descinput.setQuery(description);
        final String displayedLayerName = StringUtils.truncate(description, MAX_DISPLAYED_CHARS);
        label.setText(StringUtils.isBlank(description) ? String.format("%d", layerId) : String.format("%-2.2s - %s",String.valueOf(layerId), displayedLayerName));
    }

    /**
     * Set the colour of the current layer to the selected style when selected or to the invalid style when a query is invalid
     */
    private void recolourLayer() {
        if (enabled.isSelected()) {
            getStyleClass().add(SELECTED_STYLE);
        } else if (!isValid) {
            // not selected and invalid
            while (getStyleClass().contains(SELECTED_STYLE)) {
                getStyleClass().remove(SELECTED_STYLE);
            }
            getStyleClass().add(INVALID_STYLE);
            enabled.setDisable(true);
            
        } else {
            // not selected, valid
            while (getStyleClass().contains(SELECTED_STYLE) || getStyleClass().contains(INVALID_STYLE)) {
                getStyleClass().remove(SELECTED_STYLE);
                getStyleClass().remove(INVALID_STYLE);
            }
            enabled.setDisable(false);
        }
    }
    
    /**
     * Delete this layer
     */
    private void delete() {
        LayersViewController.getDefault().deleteLayer(layerId);
    }
    
    public boolean isValid() {
        return isValid;
    }
    
    public void setValidity(final boolean isValid) {
        this.isValid = isValid;
        recolourLayer();
    }
    
    public void recheckValidity() {
        final boolean validity = query.getQueryString() == null || ExpressionUtilities.testQueryValidity(query.getQueryString());
        setValidity(validity);
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

    protected BitMaskQuery getQuery() {
        return query;
    }
}
