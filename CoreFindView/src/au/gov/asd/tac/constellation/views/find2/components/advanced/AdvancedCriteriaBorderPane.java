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
package au.gov.asd.tac.constellation.views.find2.components.advanced;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.views.find2.FindViewController;
import au.gov.asd.tac.constellation.views.find2.components.AdvancedFindTab;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * This is a parent class for the criteria panels. It defines the primary * UI elements that are contained within each criteria panel.
 *
 * @author Atlas139mkm
 */
public class AdvancedCriteriaBorderPane extends BorderPane {

    private final AdvancedFindTab parentComponent;
    private final String type;
    private final String attributeName;
    private final GraphElementType graphElementType;

    private final VBox vbox = new VBox();
    private final HBox hboxTop = new HBox();
    private final HBox hboxBot = new HBox();

    private final ChoiceBox<String> typeChoiceBox = new ChoiceBox<>();
    private final ChoiceBox<String> filterChoiceBox = new ChoiceBox<>();
    private final Button deleteButton = new Button("-");

    private final List<Attribute> attributesList = new ArrayList<>();

    public AdvancedCriteriaBorderPane(final AdvancedFindTab parentComponent, final String attributeName, final GraphElementType graphElementType) {
        this.type = "none";
        this.parentComponent = parentComponent;
        this.graphElementType = graphElementType;
        this.attributeName = attributeName;
        setGridContent();

        typeChoiceBox.getSelectionModel().select(attributeName);

        deleteButton.setOnAction(action -> parentComponent.deleteCriteriaPane(this, graphElementType));

        typeChoiceBox.getSelectionModel().selectedItemProperty().addListener((final ObservableValue<? extends String> observableValue, final String oldElement, final String newElement) -> {
            parentComponent.changeCriteriaPane(this, graphElementType, newElement);
        });
    }

    /**
     * Sets the UI content of the pane
     */
    private void setGridContent() {
        setPadding(new Insets(5));
        populateAttributesList();

        filterChoiceBox.getItems().addAll("Is", "Is Not");
        filterChoiceBox.getSelectionModel().select("Is");

        hboxTop.getChildren().addAll(typeChoiceBox, filterChoiceBox);
        hboxTop.setPadding(new Insets(2, 5, 1, 5));
        hboxTop.setSpacing(5);

        hboxBot.setPadding(new Insets(2, 5, 1, 5));
        hboxBot.setSpacing(5);

        vbox.getChildren().addAll(hboxTop, hboxBot);

        deleteButton.setMinSize(60, 25);
        setRight(deleteButton);
        deleteButton.setAlignment(Pos.CENTER);
        setLeft(vbox);
    }

    /**
     * Populates the Attribute List with all available attributes based on the
     * currently selected graph element type
     */
    private void populateAttributesList() {
        attributesList.addAll(FindViewController.getDefault().populateAllAttributes(parentComponent.getSelectedGraphElementType(), 0));
        typeChoiceBox.getItems().addAll(getStringAttributes(attributesList));
    }

    /**
     * Takes a list of attributes and returns a list containing the String names
     * of all the attributes
     *
     * @param attributeList
     * @return A list of the names of the attributes
     */
    private List<String> getStringAttributes(final List<Attribute> attributeList) {
        final List<String> stringList = new ArrayList<>();
        for (final Attribute a : attributeList) {
            stringList.add(a.getName());
        }
        return stringList;
    }

    public ChoiceBox<String> getFilterChoiceBox() {
        return filterChoiceBox;
    }

    public List<Attribute> getAttributesList() {
        return attributesList;
    }

    public ChoiceBox<String> getTypeChoiceBox() {
        return typeChoiceBox;
    }

    public String getType() {
        return type;
    }

    public HBox getHboxTop() {
        return hboxTop;
    }

    public HBox getHboxBot() {
        return hboxBot;
    }

    public VBox getVbox() {
        return vbox;
    }

    public String getDisplayString() {
        return "none";
    }

}
