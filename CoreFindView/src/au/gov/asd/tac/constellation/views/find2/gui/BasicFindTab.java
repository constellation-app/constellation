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
package au.gov.asd.tac.constellation.views.find2.gui;

import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * This class contains all the UI elements for the Basic find Tab
 *
 * @author Atlas139mkm
 */
public class BasicFindTab extends Tab {

    private final FindViewTabs parentComponent;

    private final VBox layers = new VBox();

    private final GridPane textGrid = new GridPane();
    private final GridPane settingsGrid = new GridPane();
    private final GridPane addRemoveSelectionGrid = new GridPane();

    private final Label findLabel = new Label("Find:");
    private final TextField findTextField = new TextField();

    private final Label lookForLabel = new Label("Look For:");
    private final ChoiceBox lookForChoiceBox = new ChoiceBox();

    private final Label inAttributesLabel = new Label("In Attributes:");
    private final VBox inAttributesContainer = new VBox(inAttributesLabel);

    private final Menu inAttributesMenu = new Menu();
    // need to add menu items

    private final GridPane preferencesGrid = new GridPane();
    private final ToggleGroup textStyleTB = new ToggleGroup();
    private final RadioButton standardRadioBtn = new RadioButton("Standard Text");
    private final RadioButton regExBtn = new RadioButton("RegEx");
    private final VBox toggleVBox = new VBox();
    private final CheckBox ignoreCaseCB = new CheckBox("Ignore Case");
    private final CheckBox exactMatchCB = new CheckBox("Exact Match Only");

    private final CheckBox addToCurrent = new CheckBox("Add to Current Selection");
    private final CheckBox removeFromCurrent = new CheckBox("Remove from Current Selection");

    private final int LABEL_WIDTH = 90;

    public BasicFindTab(FindViewTabs parentComponent) {
        this.parentComponent = parentComponent;
        this.setText("Basic Find");

        setGridContent();
        this.setContent(layers);

    }

    /**
     * Sets all the UI elements to the basic find Tab
     */
    public void setGridContent() {
        ColumnConstraints neverGrow = new ColumnConstraints();
        neverGrow.setHgrow(Priority.NEVER);
        ColumnConstraints alwaysGrow = new ColumnConstraints();
        alwaysGrow.setHgrow(Priority.ALWAYS);

        layers.setSpacing(5);
        textGrid.setPadding(new Insets(10, 10, 10, 10));
        settingsGrid.setPadding(new Insets(0, 10, 0, 10));

        findLabel.setMinWidth(LABEL_WIDTH);
        textGrid.setVgap(5);
        textGrid.add(findLabel, 0, 0);
        textGrid.add(findTextField, 1, 0);
        textGrid.getColumnConstraints().addAll(neverGrow, alwaysGrow);

        settingsGrid.setHgap(5);
        settingsGrid.setVgap(5);

        lookForLabel.setMinWidth(LABEL_WIDTH - settingsGrid.getHgap());
        lookForChoiceBox.minWidth(LABEL_WIDTH);
        settingsGrid.add(lookForLabel, 0, 0);
        settingsGrid.add(lookForChoiceBox, 1, 0);

        inAttributesLabel.setMinWidth(LABEL_WIDTH - settingsGrid.getHgap());
        inAttributesContainer.minWidth(LABEL_WIDTH);
        settingsGrid.add(inAttributesLabel, 0, 1);
        settingsGrid.add(inAttributesContainer, 1, 1);

        standardRadioBtn.setToggleGroup(textStyleTB);
        regExBtn.setToggleGroup(textStyleTB);
        toggleVBox.getChildren().addAll(standardRadioBtn, regExBtn);
        toggleVBox.setSpacing(5);
        preferencesGrid.add(toggleVBox, 0, 0, 1, 2);

        preferencesGrid.add(ignoreCaseCB, 1, 0);
        preferencesGrid.add(exactMatchCB, 1, 1);
        preferencesGrid.add(addToCurrent, 2, 0);
        preferencesGrid.add(removeFromCurrent, 2, 1);
        preferencesGrid.setHgap(5);
        preferencesGrid.setVgap(5);

        settingsGrid.add(preferencesGrid, 2, 0, 2, 2);

//        addRemoveSelectionGrid.add(addToCurrent, 0, 0);
//        addRemoveSelectionGrid.add(removeFromCurrent, 1, 0);
        layers.getChildren().addAll(textGrid, settingsGrid, addRemoveSelectionGrid);

    }

    /**
     * This is used in the ReplaceTab class to avoid rewriting all the UI
     * elements again when only adding a label and a textField
     *
     * @return the textGrid GridPane
     */
    public GridPane getTextGrid() {
        return textGrid;
    }

}
