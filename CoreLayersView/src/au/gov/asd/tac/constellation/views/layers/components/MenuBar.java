/*
 * Copyright 2010-2024 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.views.layers.LayersViewController;
import au.gov.asd.tac.constellation.views.layers.utilities.LayersUtilities;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

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
    private final Button selectElements;
    private final Button deselectElements;
    private final Button helpButton;

    private static final Insets MENU_PADDING = new Insets(0, 0, 0, 10);

    private static final double SPACING = 5D;

    public MenuBar() {
        this.setAlignment(Pos.TOP_LEFT);
        this.setPadding(MENU_PADDING);
        this.setSpacing(SPACING);

        // create buttons
        addButton = createAddButton();
        deselectButton = createDeselectButton();
        selectElements = createSelectElementsButton();
        deselectElements = createDeselectElementsButton();
        helpButton = LayersUtilities.createHelpButton();

        // add to HBox
        this.getChildren().add(addButton);
        this.getChildren().add(deselectButton);
        this.getChildren().add(selectElements);
        this.getChildren().add(deselectElements);
        this.getChildren().add(helpButton);
    }

    /**
     * Create an add layer button for the menu bar
     *
     * @return add layer button
     */
    private Button createAddButton() {
        final Button addLayerButton = new Button("Add Layer");
        addLayerButton.setAlignment(Pos.CENTER_RIGHT);
        addLayerButton.setTooltip(new Tooltip("CTRL + ALT + L"));
        addLayerButton.setOnAction(event -> {
            LayersViewController.getDefault().createLayer();
            event.consume();
        });
        HBox.setHgrow(addLayerButton, Priority.ALWAYS);

        return addLayerButton;
    }

    /**
     * Create a deselect layer button for the menu bar
     *
     * @return
     */
    private Button createDeselectButton() {
        final Button deselectAllButton = new Button("Deselect Layers");
        deselectAllButton.setTooltip(new Tooltip("CTRL + ALT + D"));
        deselectAllButton.setAlignment(Pos.CENTER_RIGHT);
        deselectAllButton.setOnAction(event -> {
            LayersViewController.getDefault().deselectAll();
            event.consume();
        });
        HBox.setHgrow(deselectAllButton, Priority.ALWAYS);

        return deselectAllButton;
    }

    private Button createSelectElementsButton() {
        final Button selectElementsButton = new Button("Select Elements");
        selectElementsButton.setTooltip(new Tooltip("Select all visible elements that match the current Layer settings"));
        selectElementsButton.setOnAction(event -> LayersUtilities.selectVisibleElements(true));
        return selectElementsButton;
    }

    private Button createDeselectElementsButton() {
        final Button deselectElementsButton = new Button("De-Select Elements");
        deselectElementsButton.setTooltip(new Tooltip("De-Select all visible elements that match the current Layer settings"));
        deselectElementsButton.setOnAction(event -> LayersUtilities.selectVisibleElements(false));
        return deselectElementsButton;
    }

}
