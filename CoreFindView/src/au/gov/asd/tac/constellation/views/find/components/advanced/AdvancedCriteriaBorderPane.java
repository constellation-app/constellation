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
package au.gov.asd.tac.constellation.views.find.components.advanced;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.views.find.FindViewController;
import au.gov.asd.tac.constellation.views.find.components.AdvancedFindTab;
import au.gov.asd.tac.constellation.views.find.components.advanced.criteriavalues.FindCriteriaValues;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * This is a parent class for the criteria panels. It defines the primary * UI
 * elements that are contained within each criteria panel.
 *
 * @author Atlas139mkm
 */
public class AdvancedCriteriaBorderPane extends BorderPane {

    private final AdvancedFindTab parentComponent;
    private final String type;
    private final String attributeName;
    private final VBox vbox = new VBox();
    private final HBox hboxTop = new HBox();
    private final HBox hboxBot = new HBox();

    private final ChoiceBox<String> typeChoiceBox = new ChoiceBox<>();
    private final ChoiceBox<String> filterChoiceBox = new ChoiceBox<>();
    private final Button deleteButton = new Button("-");

    private final List<Attribute> attributesList = new ArrayList<>();
    private boolean updateUI = false;
    private static final Logger LOGGER = Logger.getLogger(AdvancedCriteriaBorderPane.class.getName());

    public AdvancedCriteriaBorderPane(final AdvancedFindTab parentComponent, final String attributeName, final GraphElementType graphElementType) {
        this.type = "none";
        this.parentComponent = parentComponent;
        this.attributeName = attributeName;
        setGridContent();

        typeChoiceBox.getSelectionModel().select(attributeName);

        deleteButton.setOnAction(action -> parentComponent.deleteCriteriaPane(this, graphElementType, -1));

        typeChoiceBox.getSelectionModel().selectedItemProperty().addListener((final ObservableValue<? extends String> observableValue, final String oldElement, final String newElement)
                -> parentComponent.changeCriteriaPane(this, graphElementType, newElement, updateUI));
    }

    /**
     * Sets the UI content of the pane
     */
    private void setGridContent() {
        setPadding(new Insets(5));
        populateAttributesList();

        typeChoiceBox.setMinWidth(125);
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
     * This function updates the attribute list with the attributes present for
     * the specific graph element type passed in. It is called when the updateUI
     * function is called in the topComponent. It saves the currently selected
     * attribute, clears the list, gets all the attributes again, repopulates
     * the list, then selects the previously selected attribute.
     *
     * @param type
     */
    public synchronized void updateAttributesList(final GraphElementType type) {
        // get the currently selected attribute
        final String currentlySelected = (getAttributeName().isEmpty() ? attributesList.get(0).getName() : getAttributeName());

        // clear the attributes list and re add all attributes
        // (in case new attributes were added)
        attributesList.clear();
        attributesList.addAll(FindViewController.getDefault().populateAllAttributes(type, 0));

        /**
         * To avoid FX error, check what thread it is currently being run on. If
         * not the application thread, created Platform.runLater with a
         * countDownLatch. This removes all elements from the typeChoiceBox and
         * re adds the based off the attributeList populated earlier. It then
         * selects the previously selected element.
         */
        final CountDownLatch cdl = new CountDownLatch(1);
        if (Platform.isFxApplicationThread()) {
            typeChoiceBox.getItems().removeAll(typeChoiceBox.getItems());
            typeChoiceBox.getItems().addAll(getStringAttributes(attributesList));
            typeChoiceBox.getSelectionModel().select(currentlySelected);
        } else {
            Platform.runLater(() -> {
                typeChoiceBox.getItems().removeAll(typeChoiceBox.getItems());
                typeChoiceBox.getItems().addAll(getStringAttributes(attributesList));
                typeChoiceBox.getSelectionModel().select(currentlySelected);
                cdl.countDown();
            });
            try {
                cdl.await();
            } catch (final InterruptedException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                Thread.currentThread().interrupt();
            }
        }
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

    /**
     * Creates a new FindCriteriaValues Object passing the attribute type (eg.
     * String), the attributes name, the current selected filterChoiceBox item.
     *
     * @return new FindCriteriaValues Object
     */
    public FindCriteriaValues getCriteriaValues() {
        return new FindCriteriaValues(type, attributeName, getFilterChoiceBox().getSelectionModel().getSelectedItem());
    }

    /**
     * Gets the attributeName
     *
     * @return attributeName
     */
    public String getAttributeName() {
        return attributeName;
    }

    /**
     * Gets the filterChoiceBox
     *
     * @return filterChoiceBox
     */
    public ChoiceBox<String> getFilterChoiceBox() {
        return filterChoiceBox;
    }

    /**
     * Gets the attributesList
     *
     * @return attributesList
     */
    public List<Attribute> getAttributesList() {
        return attributesList;
    }

    /**
     * Gets the typeChoiceBox
     *
     * @return typeChoiceBox
     */
    public ChoiceBox<String> getTypeChoiceBox() {
        return typeChoiceBox;
    }

    /**
     * Gets the type. For example this would be "String" if the
     * criteriaBorderPane is of type StringCriteriaPanel.
     *
     * @return type
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the hboxTop
     *
     * @return hboxTop
     */
    public HBox getHboxTop() {
        return hboxTop;
    }

    /**
     * Gets the hboxBot
     *
     * @return hboxBot
     */
    public HBox getHboxBot() {
        return hboxBot;
    }

    /**
     * Gets the vbox
     *
     * @return vbox
     */
    public VBox getVbox() {
        return vbox;
    }

    /**
     * Gets the string to be displayed. For the parent object this is "none"
     *
     * @return "none"
     */
    public String getDisplayString() {
        return "none";
    }

    /**
     * Gets the updateUI value. Refer to FindViewTopComonent updateUI function
     * for why this exists
     *
     * @return updateUI
     */
    public boolean isUpdateUI() {
        return updateUI;
    }

    /**
     * Sets the updateUI Value. Refer to FindViewTopComonent updateUI function
     * for why this exists
     */
    public void setUpdateUI(final boolean update) {
        updateUI = update;
    }

}
