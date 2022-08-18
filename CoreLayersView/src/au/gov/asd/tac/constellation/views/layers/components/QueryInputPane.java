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

import au.gov.asd.tac.constellation.graph.value.utilities.ExpressionUtilities;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.RecentParameterValues;
import au.gov.asd.tac.constellation.plugins.parameters.RecentValuesChangeEvent;
import au.gov.asd.tac.constellation.plugins.parameters.RecentValuesListener;
import au.gov.asd.tac.constellation.views.layers.LayersViewController;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;

/**
 * A text box allowing entry of single line text, multiple line text, or
 * passwords corresponding to a {@link PluginParameter} of
 * {@link au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType}.
 * <p>
 * Editing the value in the text box will set the string value for the
 * underlying {@link PluginParameter}.
 *
 * @see
 * au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType
 *
 * @author ruby_crucis
 */
public class QueryInputPane extends HBox implements RecentValuesListener {

    public static final int DEFAULT_WIDTH = 150;
    public static final int INTEGER_WIDTH = 160;

    private final ChangeListener<Number> recentValueSelectionListener;
    private final ComboBox<String> recentValuesCombo;
    private final TextInputControl field;
    private final String parameterId;
    private final LayerTitlePane parent;
    private final boolean validityCheckRequired;
    private List<String> recentValues = new ArrayList<>();

    public QueryInputPane(final LayerTitlePane parent, final String parameter, final String description, final String value, final boolean requiresValidityCheck) {
        this(parent, parameter, description, value, DEFAULT_WIDTH, null, requiresValidityCheck);
    }

    public QueryInputPane(final LayerTitlePane parent, final String parameter, final String description, final String value, final int defaultWidth, final boolean requiresValidityCheck) {
        this(parent, parameter, description,  value, defaultWidth, null, requiresValidityCheck);
    }

    /**
     * Primary constructor
     *
     * @param title parameter to link to value
     * @param defaultWidth default width (in pixels)
     * @param suggestedHeight suggested hight (in lines)
     */
    public QueryInputPane(final LayerTitlePane parent, final String title, final String description, final String value, final int defaultWidth, Integer suggestedHeight, final boolean requiresValidityCheck) {
        this.parent = parent;
        this.validityCheckRequired = requiresValidityCheck;
        if (suggestedHeight == null) {
            suggestedHeight = 75;
        }

        parameterId = "LAYER_QUERIES";

        final Label l = new Label(title);
        l.setStyle(l.getStyle() + "-fx-font-size:13; -fx-font-weight: bold");
        l.setWrapText(true);
        l.setPrefWidth(INTEGER_WIDTH);
        
        final Label descriptionLabel = new Label(description);
        descriptionLabel.setStyle(descriptionLabel.getStyle() + "-fx-font-size:10;");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setPrefWidth(INTEGER_WIDTH);
        
        final VBox labelsBox = new VBox(5);
        labelsBox.setMinWidth(INTEGER_WIDTH);
        labelsBox.getChildren().add(l);
        labelsBox.getChildren().add(descriptionLabel);
        getChildren().add(labelsBox);

        recentValuesCombo = new ComboBox<>();
        recentValuesCombo.setEditable(false);

        recentValuesCombo.setTooltip(new Tooltip("Recent values"));
        recentValuesCombo.setMaxWidth(5);
        if (RecentParameterValues.getRecentValues(parameterId) != null) {
            recentValues = RecentParameterValues.getRecentValues(parameterId);
        }
        setRecentValuesCombo(recentValues);

        final ListCell<String> button = new ListCell<String>() {
            @Override
            protected void updateItem(final String item, final boolean empty) {
                super.updateItem(item, empty);
                setText("...");
            }
        };
        recentValuesCombo.setButtonCell(button);

        field = new TextArea();
        field.setMinHeight(suggestedHeight);
        

        field.setPromptText(title);
        field.setText(value);

        field.setMinWidth(defaultWidth);

        recentValueSelectionListener = (ov, t, t1) -> {
            final String recentValue = recentValuesCombo.getValue();
            if (recentValue != null) {
                field.setText(recentValuesCombo.getValue());
                final boolean isValid = field.getText() == null || ExpressionUtilities.testQueryValidity(field.getText());
                updateQuery(field.getText(), field.getPromptText());
                setValidity(isValid);
            }
        };
        recentValuesCombo.getSelectionModel().selectedIndexProperty().addListener(recentValueSelectionListener);

        // If parameter is enabled, ensure widget is both enabled and editable.
        field.setEditable(true);
        field.setDisable(false);
        field.setVisible(true);
        this.setVisible(true);
        recentValuesCombo.setDisable(false);

        field.addEventFilter(KeyEvent.KEY_PRESSED, (final KeyEvent event) -> {
            if (event.getCode() == KeyCode.DELETE) {
                final IndexRange selection = field.getSelection();
                if (selection.getLength() == 0) {
                    field.deleteNextChar();
                } else {
                    field.deleteText(selection);
                }
                event.consume();
            } else if (event.isShortcutDown() && event.isShiftDown() && (event.getCode() == KeyCode.RIGHT)) {
                field.selectNextWord();
                event.consume();
            } else if (event.isShortcutDown() && event.isShiftDown() && (event.getCode() == KeyCode.LEFT)) {
                field.selectPreviousWord();
                event.consume();
            } else if (event.isShortcutDown() && (event.getCode() == KeyCode.RIGHT)) {
                field.nextWord();
                event.consume();
            } else if (event.isShortcutDown() && (event.getCode() == KeyCode.LEFT)) {
                field.previousWord();
                event.consume();
            } else if (event.isShiftDown() && (event.getCode() == KeyCode.RIGHT)) {
                field.selectForward();
                event.consume();
            } else if (event.isShiftDown() && (event.getCode() == KeyCode.LEFT)) {
                field.selectBackward();
                event.consume();
            } else if (event.isShortcutDown() && (event.getCode() == KeyCode.A)) {
                field.selectAll();
                event.consume();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                event.consume();
            } else {
                // Do nothing
            }
        });

        final Tooltip tooltip = new Tooltip("");
        tooltip.setStyle("-fx-text-fill: white;");
        field.focusedProperty().addListener((final ObservableValue<? extends Boolean> ov, final Boolean t, final Boolean t1) -> {
            if (!t1) {
                if(validityCheckRequired) {
                    final boolean isValid = field.getText() == null || ExpressionUtilities.testQueryValidity(field.getText());
                    updateQuery(field.getText(), field.getPromptText());
                    setValidity(isValid);
                    if (isValid && !recentValues.contains(field.getText())) {
                        recentValues.add(field.getText());
                        setRecentValuesCombo(recentValues);
                    }
                } else {
                    updateDescription(field.getText());
                }
            }
        });
        
        field.prefWidthProperty().bind(this.widthProperty());
        
        // A scroll pane to hold the attribute boxes
        final ScrollPane attributeScrollPane = new ScrollPane();
        attributeScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        attributeScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        attributeScrollPane.setMaxWidth(Double.MAX_VALUE);
        attributeScrollPane.setContent(field);
        attributeScrollPane.setPrefViewportWidth(defaultWidth);
        attributeScrollPane.setPrefViewportHeight(suggestedHeight);
        attributeScrollPane.setFitToWidth(true);
        attributeScrollPane.setPrefHeight(suggestedHeight);
        attributeScrollPane.prefWidthProperty().bind(this.widthProperty());
        attributeScrollPane.prefViewportWidthProperty().bind(this.widthProperty());

        getChildren().add(attributeScrollPane);

        if (requiresValidityCheck) {
            getChildren().add(recentValuesCombo);
        }
    }
    
    public boolean validityCheckRequired() {
        return validityCheckRequired;
    }
    
    public String getQuery() {
        return field.getText() == "" ? null : field.getText();
    }
    
    public void setQuery(final String query) {
        field.setText(query);
    }
    
    public static final String INVALID_STYLE = "titled-pane-invalid";
    public static final String SELECTED_STYLE = "titled-pane-selected";
    
    private static final String INVALID_ID = "invalid";

    /**
     * Update the field UI to match whether the query is valid
     * @param isValid
     */
    public void setValidity(final boolean isValid) {
        field.setId(isValid ? StringUtils.EMPTY : INVALID_ID);
    }

    /**
     * Update this query from a text edit event.
     *
     * @param fieldText 
     */
    private void updateDescription(final String fieldText) {
        LayersViewController.getDefault().updateDescription(fieldText, parent.getQuery().getIndex());
    }

    /**
     * Update this query from a text edit event.
     *
     * @param fieldText
     * @param promptText
     */
    private void updateQuery(final String fieldText, final String promptText) {
        LayersViewController.getDefault().updateQuery(fieldText, parent.getQuery().getIndex(), promptText);
    }

    /**
     * Update the combo boxes next to each query with recent queries
     *
     * @param e
     */
    @Override
    public void recentValuesChanged(final RecentValuesChangeEvent e) {
        if (recentValuesCombo != null && parameterId.equals(e.getId())) {
            recentValuesCombo.getSelectionModel().selectedIndexProperty().removeListener(recentValueSelectionListener);
            final List<String> newRecentValues = e.getNewValues();
            if (recentValues != null) {
                recentValuesCombo.setItems(FXCollections.observableList(newRecentValues));
                recentValuesCombo.setDisable(false);
            } else {
                final List<String> empty = Collections.emptyList();
                recentValuesCombo.setItems(FXCollections.observableList(empty));
                recentValuesCombo.setDisable(true);
            }
            recentValuesCombo.setPromptText("...");
            recentValuesCombo.getSelectionModel().selectedIndexProperty().addListener(recentValueSelectionListener);
        }
    }

    /**
     * Set the values for the recent values combo box
     *
     * @param recentValues
     */
    private void setRecentValuesCombo(final List<String> recentValues) {
        if (recentValues != null) {
            recentValuesCombo.setItems(FXCollections.observableList(recentValues));
        } else {
            recentValuesCombo.setDisable(true);
        }
    }
}
