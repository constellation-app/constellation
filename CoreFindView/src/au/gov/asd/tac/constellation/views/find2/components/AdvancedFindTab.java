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
package au.gov.asd.tac.constellation.views.find2.components;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.views.find2.components.advanced.AdvancedCriteriaBorderPane;
import au.gov.asd.tac.constellation.views.find2.components.advanced.BooleanCriteriaPanel;
import au.gov.asd.tac.constellation.views.find2.components.advanced.ColourCriteriaPanel;
import au.gov.asd.tac.constellation.views.find2.components.advanced.DateTimeCriteriaPanel;
import au.gov.asd.tac.constellation.views.find2.components.advanced.IconCriteriaPanel;
import au.gov.asd.tac.constellation.views.find2.components.advanced.FloatCriteriaPanel;
import au.gov.asd.tac.constellation.views.find2.components.advanced.StringCriteriaPanel;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * This class contains the UI tab for the Advanced Find Tab.
 *
 * @author Atlas139mkm
 */
public class AdvancedFindTab extends Tab {

    private final FindViewTabs parentComponent;

    private final GridPane settingsGrid = new GridPane();
    private final BorderPane settingsBorderPane = new BorderPane();

    private final ScrollPane listScrollPane = new ScrollPane();

    private final List<AdvancedCriteriaBorderPane> nodeFindCriteriaList = new ArrayList<>();
    private final List<AdvancedCriteriaBorderPane> transactionFindCriteriaList = new ArrayList<>();

    private final GridPane nodeFindCriteriaGrid = new GridPane();
    private final GridPane transactionFindCriteriaGrid = new GridPane();

    private final String[] elementTypes = {GraphElementType.VERTEX.getShortLabel(), GraphElementType.TRANSACTION.getShortLabel(), GraphElementType.EDGE.getShortLabel(), GraphElementType.LINK.getShortLabel()};
    private final String[] matchCriteriaTypes = {"All", "Any"};
    private final String[] currentSelectionTypes = {"Ignore", "Add To", "Find In", "Remove From"};

    private final Label lookForLabel = new Label("Look For:");
    private final ChoiceBox<String> lookForChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(elementTypes));
    private final Label matchCriteriaLabel = new Label("Match Criteria:");
    private final ChoiceBox<String> matchCriteriaChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(matchCriteriaTypes));
    private final Label currentSeletionLabel = new Label("Current Selection:");
    private final ChoiceBox<String> currentSelectionChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(currentSelectionTypes));
    private final Button addCriteriaPaneButton = new Button("+");
    private final GridPane addButtonGP = new GridPane();
    private final Pane widthSpacer = new Pane();
    private final Pane heightSpacer = new Pane();

    protected final HBox buttonsHBox = new HBox();
    protected final VBox buttonsVBox = new VBox();
    protected final CheckBox searchAllGraphs = new CheckBox("Search all open Graphs");
    private final Button findNextButton = new Button("Find Next");
    private final Button findPrevButton = new Button("Find Previous");
    private final Button findAllButton = new Button("Find All");

    public AdvancedFindTab(final FindViewTabs parentComponent) {
        this.parentComponent = parentComponent;
        setText("Advanced Find");
        setGridContent();
        addCriteriaPaneButton.setOnAction(action -> addCriteriaPane(getSelectedGraphElementType()));

        // Change the
        lookForChoiceBox.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observableValue, final String oldElement, final String newElement) -> {
            changeDisplayedList(newElement);
        });

    }

    /**
     * Sets the UI elements for the tab
     */
    private void setGridContent() {
        lookForChoiceBox.getItems().remove(2, 4);
        lookForChoiceBox.getSelectionModel().select(0);
        currentSelectionChoiceBox.getSelectionModel().select(0);
        matchCriteriaChoiceBox.getSelectionModel().select(0);

        settingsGrid.add(lookForLabel, 0, 0);
        settingsGrid.add(lookForChoiceBox, 0, 1);
        settingsGrid.add(matchCriteriaLabel, 1, 0);
        settingsGrid.add(matchCriteriaChoiceBox, 1, 1);
        settingsGrid.add(currentSeletionLabel, 2, 0);
        settingsGrid.add(currentSelectionChoiceBox, 2, 1);

        settingsGrid.setPadding(new Insets(5));
        settingsGrid.setHgap(5);
        settingsGrid.setVgap(5);

        settingsBorderPane.setCenter(settingsGrid);
        settingsBorderPane.setPadding(new Insets(0, 0, 10, 0));

        heightSpacer.setMinSize(20, 25);
        widthSpacer.setMinSize(22, 40);
        addButtonGP.add(heightSpacer, 0, 0);
        addButtonGP.add(widthSpacer, 1, 0, 1, 2);
        addButtonGP.add(addCriteriaPaneButton, 0, 1);
        addCriteriaPaneButton.setMinSize(60, 25);
        settingsBorderPane.setRight(addButtonGP);

        updateGridCriteriaPanes(getSelectedGraphElementType());

        listScrollPane.setFitToWidth(true);

        listScrollPane.setContent(nodeFindCriteriaGrid);
        listScrollPane.setPadding(new Insets(2, 5, 2, 5));
        listScrollPane.fitToWidthProperty();

        final BorderPane parentBorderPane = new BorderPane();
        parentBorderPane.setTop(settingsBorderPane);
        parentBorderPane.setCenter(listScrollPane);
        setContent(parentBorderPane);

        buttonsHBox.setPadding(new Insets(10, 10, 5, 10));
        buttonsHBox.setSpacing(5);

        updateGridColours(GraphElementType.getValue(lookForChoiceBox.getSelectionModel().getSelectedItem()));
    }

    /**
     * Updates the buttons at the bottom of the tab to match the tab selection
     */
    public void updateButtons() {
        buttonsHBox.getChildren().clear();
        buttonsHBox.getChildren().addAll(searchAllGraphs, findAllButton, findPrevButton, findNextButton);

        buttonsHBox.setAlignment(Pos.CENTER_RIGHT);
        parentComponent.getParentComponent().setBottom(buttonsHBox);
    }

    /**
     * Gets the currently selected lookForChoiceBox selection as a graph element
     * type
     *
     * @return
     */
    public GraphElementType getSelectedGraphElementType() {
        return GraphElementType.getValue(lookForChoiceBox.getSelectionModel().getSelectedItem());
    }

    /**
     * Updates the GridCriteriaPane based on the currently selected Graph
     * Element Type
     *
     * @param type
     */
    private void updateGridCriteriaPanes(final GraphElementType type) {
        int i = 0;
        final List<AdvancedCriteriaBorderPane> criteriaList = getCorrespondingCriteriaList(type);
        final GridPane gridPane = getCorrespondingGridPane(type);

        // adds all the criteriaPanes that are containined within the type
        // specific list
        for (AdvancedCriteriaBorderPane criteriaPane : criteriaList) {
            gridPane.add(criteriaPane, 0, i);
            GridPane.setHgrow(criteriaPane, Priority.ALWAYS);
            i++;
        }
    }

    /**
     * Updates the grid colors to cycle between #4d4d4d and #222222. This
     * ensures every odd advancedCriteriaBorderPane is #4d4d4d and every even is
     * #222222
     *
     * @param type
     */
    private void updateGridColours(final GraphElementType type) {
        int i = 0;
        final List<AdvancedCriteriaBorderPane> criteriaList = getCorrespondingCriteriaList(type);

        for (AdvancedCriteriaBorderPane criteriaPane : criteriaList) {
            criteriaPane.setStyle(i % 2 == 0 ? "-fx-background-color: #4d4d4d;" : "-fx-background-color: #222222;");
            i++;
        }
    }

    /**
     * This removes the advanced criteriaborderpane from both the UI list and
     * the background list.
     *
     * @param pane
     * @param type
     */
    public void deleteCriteriaPane(AdvancedCriteriaBorderPane pane, GraphElementType type) {
        final List<AdvancedCriteriaBorderPane> criteriaList = getCorrespondingCriteriaList(type);
        final GridPane gridPane = getCorrespondingGridPane(type);

        criteriaList.remove(pane);
        gridPane.getChildren().remove(pane);

        updateGridColours(type);
    }

    /**
     * This adds a criteriaPane to the provided graphElement type list. The
     * initial pane will be of type string.
     *
     * @param type
     */
    public void addCriteriaPane(final GraphElementType type) {
        final List<AdvancedCriteriaBorderPane> criteriaList = getCorrespondingCriteriaList(type);
        final GridPane gridPane = getCorrespondingGridPane(type);

        // adds a new StrinCriteriaPanel to the end of the criteriaPaneList
        criteriaList.add(new StringCriteriaPanel(this, "Identifier", getSelectedGraphElementType()));
        gridPane.add(criteriaList.get(criteriaList.size() - 1), 0, gridPane.getRowCount());
        GridPane.setHgrow(criteriaList.get(criteriaList.size() - 1), Priority.ALWAYS);

        updateGridColours(type);
    }

    /**
     * This is called when the user changes the selected attribute type of a
     * specific criteria pane. For example if the user changes the attribute
     * from "Label" to "Color".
     *
     * @param criteriaPane
     * @param type
     * @param attributeName
     */
    public void changeCriteriaPane(final AdvancedCriteriaBorderPane criteriaPane, final GraphElementType type, final String attributeName) {
        final List<AdvancedCriteriaBorderPane> criteriaList = getCorrespondingCriteriaList(type);
        final GridPane gridPane = getCorrespondingGridPane(type);

        // For each of the panes in the criteriaList.
        for (AdvancedCriteriaBorderPane pane : criteriaList) {
            //  If the pane == the pane passed in, get its index and get the
            //  list of attributes it contains.
            if (criteriaPane == pane) {
                final int paneIndex = criteriaList.indexOf(pane);
                final List<Attribute> attributeList = new ArrayList<>(pane.getAttributesList());

                // delete the pane
                deleteCriteriaPane(pane, type);

                // for each of the attributes within the attribute list
                for (Attribute a : attributeList) {

                    // if the new attribute requested == a.getName
                    if (attributeName == a.getName()) {

                        // Add a new CriteriaPane of the attributType passed
                        // at the index the pane was originaly on.
                        // Select the attributeName of the type passed
                        criteriaList.add(paneIndex, getNewCriteriaPanel(a.getAttributeType(), attributeName, type));
                        gridPane.add(criteriaList.get(paneIndex), 0, paneIndex);
                        pane.getTypeChoiceBox().getSelectionModel().select(attributeName);
                        GridPane.setHgrow(criteriaList.get(paneIndex), Priority.ALWAYS);

                        // update the colours of the list to match
                        updateGridColours(type);

                        return;
                    }
                }

            }
        }
    }

    /**
     * This gets the corresponding criteriaPanel based on the attributeType and
     * attributeName. For example if the attribute name is Label, its type is
     * then String so a StringCriteriaPanel is then returned
     *
     * @param attributeType
     * @param attributeName
     * @param type
     * @return
     */
    private AdvancedCriteriaBorderPane getNewCriteriaPanel(final String attributeType, final String attributeName, final GraphElementType type) {
        switch (attributeType) {
            case "String":
                return new StringCriteriaPanel(this, attributeName, type);
            case "float":
                return new FloatCriteriaPanel(this, attributeName, type);
            case "boolean":
                return new BooleanCriteriaPanel(this, attributeName, type);
            case "color":
                return new ColourCriteriaPanel(this, attributeName, type);
            case "datetime":
                return new DateTimeCriteriaPanel(this, attributeName, type);
            case "icon":
                return new IconCriteriaPanel(this, attributeName, type);
            default:
                return new StringCriteriaPanel(this, attributeName, type);
        }
    }

    /**
     * Gets the corresponding Criteria list based on the type passed. Return
     * Vertex if type == vertex, or transaction if not. As these are the only
     * two possible selections they are the only corresponding lists that can be
     * returned.
     *
     * @param type
     * @return
     */
    private List<AdvancedCriteriaBorderPane> getCorrespondingCriteriaList(final GraphElementType type) {
        return (type == GraphElementType.VERTEX ? nodeFindCriteriaList : transactionFindCriteriaList);

    }

    /**
     * Gets the corresponding gridPane based on the type passed. Return Vertex
     * if type == vertex, or transaction if not. As these are the only two
     * possible selections they are the only corresponding lists that can be
     * returned.
     *
     * @param type
     * @return
     */
    private GridPane getCorrespondingGridPane(final GraphElementType type) {
        return (type == GraphElementType.VERTEX ? nodeFindCriteriaGrid : transactionFindCriteriaGrid);
    }

    /**
     * Changes the displayed list based on the String graphElement type passed.
     * This allows the user to cycle between the node and transaction lists
     * based on their selection.
     *
     * @param elementType
     */
    private void changeDisplayedList(final String elementType) {
        GridPane gridPane = getCorrespondingGridPane(GraphElementType.getValue(elementType));

        listScrollPane.setContent(gridPane);
    }

    public Button getAddButton() {
        return addCriteriaPaneButton;
    }
}
