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
package au.gov.asd.tac.constellation.views.layers;

import au.gov.asd.tac.constellation.views.layers.utilities.LayersUtilities;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.TextAlignment;

/**
 *
 * MenuBar class holds multiple buttons and labels. The class is responsible for
 * directing the actions to the responsible classes.
 *
 * EG. field addButton will send a message to create a new layer.
 *
 * @author aldebaran30701
 */
public class MenuBar extends HBox {

    private final Button addButton;
    private final Button deselectButton;
    private final Button helpButton;
    private final Label queryErrorLabel;

    private static final Insets MENU_PADDING = new Insets(0, 0, 0, 10);

    private static final double SPACING = 5D;

    private static final String QUERY_WARNING_TEXT = "Invalid query structure";

    public MenuBar() {
        this.setAlignment(Pos.TOP_LEFT);
        this.setPadding(MENU_PADDING);
        this.setSpacing(SPACING);

        // create buttons
        addButton = createAddButton();
        deselectButton = createDeselectButton();
        helpButton = LayersUtilities.createHelpButton();

        // create error label
        queryErrorLabel = createErrorLabel();

        // add to HBox
        this.getChildren().add(addButton);
        this.getChildren().add(deselectButton);
        this.getChildren().add(helpButton);
        this.getChildren().add(queryErrorLabel);
    }

    private Button createAddButton() {
        final Button addLayerButton = new Button("Add New Layer");
        addLayerButton.setAlignment(Pos.CENTER_RIGHT);
        addLayerButton.setTooltip(new Tooltip("CTRL + ALT + L"));
        addLayerButton.setOnAction(event -> {
            LayersViewController.getDefault().createLayer();
            event.consume();
        });
        HBox.setHgrow(addLayerButton, Priority.ALWAYS);

        return addLayerButton;
    }

    private Button createDeselectButton() {
        final Button deselectAllButton = new Button("Deselect All Layers");
        deselectAllButton.setTooltip(new Tooltip("CTRL + ALT + D"));
        deselectAllButton.setAlignment(Pos.CENTER_RIGHT);
        deselectAllButton.setOnAction(event -> {
            LayersViewController.getDefault().deselectAll();
            event.consume();
        });
        HBox.setHgrow(deselectAllButton, Priority.ALWAYS);

        return deselectAllButton;
    }

    private Label createErrorLabel() {
        final Label errorLabel = new Label(QUERY_WARNING_TEXT);
        errorLabel.setPadding(new Insets(2, 0, 0, 0));
        errorLabel.setTextAlignment(TextAlignment.CENTER);
        errorLabel.setStyle("-fx-text-fill: rgba(241,74,74,0.85);");
        errorLabel.setVisible(false);
        HBox.setHgrow(errorLabel, Priority.ALWAYS);

        return errorLabel;
    }

    public void displayQueryErrorLabel(final boolean isVisible) {
        queryErrorLabel.setVisible(isVisible);
    }

    public boolean getQueryErrorLabelVisibility() {
        return queryErrorLabel.isVisible();
    }

}
