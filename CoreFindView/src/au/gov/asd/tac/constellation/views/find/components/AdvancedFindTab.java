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
package au.gov.asd.tac.constellation.views.find.components;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.attribute.BooleanAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.IntegerAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.LongAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.ZonedDateTimeAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.ColorAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.IconAttributeDescription;
import au.gov.asd.tac.constellation.views.find.FindViewController;
import au.gov.asd.tac.constellation.views.find.components.advanced.AdvancedCriteriaBorderPane;
import au.gov.asd.tac.constellation.views.find.components.advanced.BooleanCriteriaPanel;
import au.gov.asd.tac.constellation.views.find.components.advanced.ColorCriteriaPanel;
import au.gov.asd.tac.constellation.views.find.components.advanced.DateTimeCriteriaPanel;
import au.gov.asd.tac.constellation.views.find.components.advanced.FloatCriteriaPanel;
import au.gov.asd.tac.constellation.views.find.components.advanced.IconCriteriaPanel;
import au.gov.asd.tac.constellation.views.find.components.advanced.StringCriteriaPanel;
import au.gov.asd.tac.constellation.views.find.components.advanced.criteriavalues.FindCriteriaValues;
import au.gov.asd.tac.constellation.views.find.components.advanced.utilities.AdvancedSearchParameters;
import au.gov.asd.tac.constellation.views.find.utilities.ActiveFindResultsList;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
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
    private final String[] searchInTypes = {"Current Graph", "Current Selection", "All Open Graphs"};
    private final String[] postSearchTypes = {"Replace Selection", "Add To Selection", "Remove From Selection"};

    private final Label lookForLabel = new Label("Look For:");
    private final ChoiceBox<String> lookForChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(elementTypes));
    private final Label matchCriteriaLabel = new Label("Match Criteria:");
    private final ChoiceBox<String> matchCriteriaChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(matchCriteriaTypes));
    private final Label searchInLabel = new Label("Search In:");
    private final ChoiceBox<String> searchInChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(searchInTypes));
    private final Label postSearchLabel = new Label("Post-Search Actions:");
    private final ChoiceBox<String> postSearchChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(postSearchTypes));
    private final Button addCriteriaPaneButton = new Button("+");
    private final GridPane addButtonGP = new GridPane();
    private final Pane widthSpacer = new Pane();
    private final Pane heightSpacer = new Pane();

    private final GridPane currentSelectionPane = new GridPane();
    private final GridPane matchesFoundPane = new GridPane();

    private final GridPane bottomGrid = new GridPane();

    private boolean firstSearch = true;
    private final String foundLabelText = "Results Found: ";
    private final Label matchesFoundLabel = new Label("");
    private final Label matchesFoundCountLabel = new Label("");

    protected final HBox buttonsHBox = new HBox();
    protected final VBox buttonsVBox = new VBox();
    private final Button findNextButton = new Button("Find Next");
    private final Button findPrevButton = new Button("Find Previous");
    private final Button findAllButton = new Button("Find All");
    private final Button deleteResultsButton = new Button("Delete Results From Graph(s)");

    public AdvancedFindTab(final FindViewTabs parentComponent) {
        this.parentComponent = parentComponent;
        setText("Advanced Find");
        setGridContent();
        addCriteriaPaneButton.setOnAction(action -> addCriteriaPane(getSelectedGraphElementType()));

        // Change the displayed list based on the graph element type selection
        lookForChoiceBox.getSelectionModel().selectedItemProperty().addListener((final ObservableValue<? extends String> observableValue, final String oldElement, final String newElement) -> changeDisplayedList(newElement));

        searchInChoiceBox.getSelectionModel().selectedItemProperty().addListener((final ObservableValue<? extends String> observableValue, final String oldElement, final String newElement) -> updateSelectionFactors());

        postSearchChoiceBox.getSelectionModel().selectedItemProperty().addListener((final ObservableValue<? extends String> observableValue, final String oldElement, final String newElement) -> updateSelectionFactors());

        findAllButton.setOnAction(action -> findAllAction());
        findNextButton.setOnAction(action -> findNextAction());
        findPrevButton.setOnAction(action -> findPreviousAction());
        deleteResultsButton.setOnAction(action -> deleteResultsAction());

        matchesFoundPane.add(matchesFoundLabel, 0, 0);
        matchesFoundPane.add(matchesFoundCountLabel, 1, 0);
        
        FindViewController.getDefault().getNumResultsFound().addListener((observable, oldValue, newValue) -> {
            if (firstSearch) {
                matchesFoundLabel.setText(foundLabelText);
                firstSearch = false;
            }

            matchesFoundCountLabel.setText("" + newValue);
        });

    }

    /**
     * Sets the UI elements for the tab
     */
    private void setGridContent() {
        lookForChoiceBox.getItems().remove(2, 4);
        lookForChoiceBox.getSelectionModel().select(0);
        searchInChoiceBox.getSelectionModel().select(0);
        postSearchChoiceBox.getSelectionModel().select(0);
        matchCriteriaChoiceBox.getSelectionModel().select(0);

        currentSelectionPane.add(searchInLabel, 0, 0);
        currentSelectionPane.add(searchInChoiceBox, 0, 1);
        currentSelectionPane.add(postSearchLabel, 1, 0);
        currentSelectionPane.add(postSearchChoiceBox, 1, 1);
        currentSelectionPane.setPadding(new Insets(1, 0, 0, 25));
        currentSelectionPane.setVgap(4.25);

        settingsGrid.add(lookForLabel, 0, 0);
        settingsGrid.add(lookForChoiceBox, 0, 1);
        settingsGrid.add(matchCriteriaLabel, 1, 0);
        settingsGrid.add(matchCriteriaChoiceBox, 1, 1);
        settingsGrid.add(currentSelectionPane, 2, 0, 1, 2);

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

        matchesFoundPane.setPadding(new Insets(10, 12, 5, 10));

        updateGridColors(GraphElementType.getValue(lookForChoiceBox.getSelectionModel().getSelectedItem()));
    }

    /**
     * Updates the buttons at the bottom of the tab to match the tab selection
     */
    public void updateButtons() {
        //Clears all existing buttons, then adds this panes buttons
        buttonsHBox.getChildren().clear();
        buttonsHBox.getChildren().addAll(deleteResultsButton, findAllButton, findPrevButton, findNextButton);

        deleteResultsButton.setDisable(true);

        buttonsHBox.setAlignment(Pos.CENTER_RIGHT);

        bottomGrid.getChildren().clear();
        bottomGrid.add(buttonsHBox, 0, 0);
        bottomGrid.add(matchesFoundPane, 0, 1);

        parentComponent.getParentComponent().setBottom(bottomGrid);
    }

    /**
     * Gets the currently selected lookForChoiceBox selection as a graph element
     * type
     *
     * @return the selected graph element type
     */
    public GraphElementType getSelectedGraphElementType() {
        return GraphElementType.getValue(lookForChoiceBox.getSelectionModel().getSelectedItem());
    }

    /**
     * This is called on add and remove to current selection toggles to update
     * the variables values stored in the controller
     */
    public void updateSelectionFactors() {
        final boolean disable = postSearchChoiceBox.getSelectionModel().getSelectedIndex() == 1 || postSearchChoiceBox.getSelectionModel().getSelectedIndex() == 2;
        findNextButton.setDisable(disable);
        findPrevButton.setDisable(disable);
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
        for (final AdvancedCriteriaBorderPane criteriaPane : criteriaList) {
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
    private void updateGridColors(final GraphElementType type) {
        int i = 0;
        final List<AdvancedCriteriaBorderPane> criteriaList = getCorrespondingCriteriaList(type);

        for (final AdvancedCriteriaBorderPane criteriaPane : criteriaList) {
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
    public void deleteCriteriaPane(final AdvancedCriteriaBorderPane pane, final GraphElementType type, final int index) {
        //Retrieve the corresponding criteria list and grid pane for the type
        final List<AdvancedCriteriaBorderPane> criteriaList = getCorrespondingCriteriaList(type);
        final GridPane gridPane = getCorrespondingGridPane(type);

        // clear everything
        criteriaList.remove(pane);
        gridPane.getChildren().remove(pane);
        gridPane.getChildren().removeAll(gridPane.getChildren());

        // re add all criteriapanes with the one at the specified index removed
        int i = 0;
        for (final AdvancedCriteriaBorderPane cirteriaPane : criteriaList) {
            if (i == index) {
                i++;
            }
            gridPane.add(cirteriaPane, 0, i);
            i++;
        }
        // update the grid colors to match the new list order
        updateGridColors(type);
    }

    /**
     * This adds a criteriaPane to the provided graphElement type list. The
     * initial pane will be of type string.
     *
     * @param type
     */
    public void addCriteriaPane(final GraphElementType type) {
        //Retrieve the corresponding criteria list and grid pane for the type
        final List<AdvancedCriteriaBorderPane> criteriaList = getCorrespondingCriteriaList(type);
        final GridPane gridPane = getCorrespondingGridPane(type);

        // adds a new StrinCriteriaPanel to the end of the criteriaPaneList
        criteriaList.add(new StringCriteriaPanel(this, "Identifier", getSelectedGraphElementType()));
        gridPane.add(criteriaList.get(criteriaList.size() - 1), 0, gridPane.getRowCount());
        GridPane.setHgrow(criteriaList.get(criteriaList.size() - 1), Priority.ALWAYS);

        // update the grid colors to match the new list order
        updateGridColors(type);
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
    public void changeCriteriaPane(final AdvancedCriteriaBorderPane criteriaPane, final GraphElementType type, final String attributeName, final boolean updateUI) {
        if (!updateUI) {
            final List<AdvancedCriteriaBorderPane> criteriaList = getCorrespondingCriteriaList(type);
            final GridPane gridPane = getCorrespondingGridPane(type);

            // For each of the panes in the criteriaList.
            for (final AdvancedCriteriaBorderPane pane : criteriaList) {
                //  If the pane == the pane passed in, get its index and get the
                //  list of attributes it contains.
                if (criteriaPane == pane) {
                    final int paneIndex = criteriaList.indexOf(pane);
                    final List<Attribute> attributeList = new ArrayList<>(pane.getAttributesList());

                    // delete the pane
                    deleteCriteriaPane(pane, type, paneIndex);

                    // for each of the attributes within the attribute list
                    for (final Attribute a : attributeList) {

                        // if the new attribute requested == a.getName
                        if (attributeName == a.getName()) {

                            // Add a new CriteriaPane of the attributType passed
                            // at the index the pane was originaly on.
                            // Select the attributeName of the type passed
                            criteriaList.add(paneIndex, getNewCriteriaPanel(a.getAttributeType(), attributeName, type));
                            gridPane.add(criteriaList.get(paneIndex), 0, paneIndex);
                            pane.getTypeChoiceBox().getSelectionModel().select(attributeName);
                            GridPane.setHgrow(criteriaList.get(paneIndex), Priority.ALWAYS);

                            // update the colors of the list to match
                            updateGridColors(type);

                            return;
                        }
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
        //Switch statement to determine what type of panel is being requested
        switch (attributeType) {
            case StringAttributeDescription.ATTRIBUTE_NAME:
                return new StringCriteriaPanel(this, attributeName, type);
            case FloatAttributeDescription.ATTRIBUTE_NAME:
                return new FloatCriteriaPanel(this, attributeName, type);
            case IntegerAttributeDescription.ATTRIBUTE_NAME:
                return new FloatCriteriaPanel(this, attributeName, type);
            case LongAttributeDescription.ATTRIBUTE_NAME:
                return new FloatCriteriaPanel(this, attributeName, type);
            case BooleanAttributeDescription.ATTRIBUTE_NAME:
                return new BooleanCriteriaPanel(this, attributeName, type);
            case ColorAttributeDescription.ATTRIBUTE_NAME:
                return new ColorCriteriaPanel(this, attributeName, type);
            case ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME:
                return new DateTimeCriteriaPanel(this, attributeName, type);
            case IconAttributeDescription.ATTRIBUTE_NAME:
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
    public List<AdvancedCriteriaBorderPane> getCorrespondingCriteriaList(final GraphElementType type) {
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
    public GridPane getCorrespondingGridPane(final GraphElementType type) {
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
        final GridPane gridPane = getCorrespondingGridPane(GraphElementType.getValue(elementType));

        listScrollPane.setContent(gridPane);
    }

    /**
     * This function loops through all current criteria panes and reads their
     * values. It stores these values as a findCriteriaValues object and returns
     * them all in a list. This is used just before commencing a search
     * function.
     *
     * @param list The list of criteria panes to read values from
     * @return A list containing all criteria panes values
     */
    public List<FindCriteriaValues> getCriteriaValues(List<AdvancedCriteriaBorderPane> list) {
        List<FindCriteriaValues> criteriaValuesList = new ArrayList<>();
        for (final AdvancedCriteriaBorderPane pane : list) {
            criteriaValuesList.add(pane.getCriteriaValues());
        }
        return criteriaValuesList;
    }

    /**
     * This function takes a graph element type, gets the corresponding criteria
     * values list and saves all the UI preferences into a
     * AdvancedSearchParamters object. This object is then saved into the
     * AdvancedSerachParamter object in the controller.
     *
     * @param type The graph element type being saved
     */
    public void updateAdvancedSearchParameters(final GraphElementType type) {
        final List<FindCriteriaValues> criteriaValuesList = getCriteriaValues(getCorrespondingCriteriaList(type));
        final AdvancedSearchParameters parameters = new AdvancedSearchParameters(criteriaValuesList, type,
                matchCriteriaChoiceBox.getSelectionModel().getSelectedItem(),
                postSearchChoiceBox.getSelectionModel().getSelectedItem(), searchInChoiceBox.getSelectionModel().getSelectedItem());
        FindViewController.getDefault().updateAdvancedSearchParameters(parameters);
    }

    /**
     * This action is called when the find all button is pressed. It calls the
     * retrieveAdvancedSearch function in the controller with searchAll = true
     * and searchNext = false;
     */
    public void findAllAction() {
        if (!getCriteriaValues(getCorrespondingCriteriaList(GraphElementType.getValue(getLookForChoiceBox().getSelectionModel().getSelectedItem()))).isEmpty()) {
            updateAdvancedSearchParameters(GraphElementType.getValue(getLookForChoiceBox().getSelectionModel().getSelectedItem()));
            FindViewController.getDefault().retrieveAdvancedSearch(true, false);
            getDeleteResultsButton().setDisable(false);
        }
    }

    /**
     * This action is called when the find next button is pressed. It calls the
     * retrieveAdvancedSearch function in the controller with searchAll = false
     * and searchNext = true;
     */
    public void findNextAction() {
        if (!getCriteriaValues(getCorrespondingCriteriaList(GraphElementType.getValue(getLookForChoiceBox().getSelectionModel().getSelectedItem()))).isEmpty()) {
            updateAdvancedSearchParameters(GraphElementType.getValue(getLookForChoiceBox().getSelectionModel().getSelectedItem()));
            FindViewController.getDefault().retrieveAdvancedSearch(false, true);
        }
    }


    /**
     * This action is called when the find next prev is pressed. It calls the
     * retrieveAdvancedSearch function in the controller with searchAll = false
     * and searchNext = false;
     */
    public void findPreviousAction() {
        if (!getCriteriaValues(getCorrespondingCriteriaList(GraphElementType.getValue(getLookForChoiceBox().getSelectionModel().getSelectedItem()))).isEmpty()) {
            updateAdvancedSearchParameters(GraphElementType.getValue(getLookForChoiceBox().getSelectionModel().getSelectedItem()));
            FindViewController.getDefault().retrieveAdvancedSearch(false, false);
        }
    }

    /**
     * This is run when the user presses the delete results button.
     * It calls a dialog box to confirm that the user wishes to delete the results of the find from all graphs searched.
     * Then set the deleteResultsButton to be disabled to stop users from trying to delete results that have already been deleted.
     */
    private void deleteResultsAction() {
        if (!ActiveFindResultsList.getAdvancedResultsList().isEmpty()) {
            FindViewController.getDefault().deleteResults(ActiveFindResultsList.getAdvancedResultsList(), FindViewController.getGraphsSearched());
            deleteResultsButton.setDisable(true);
            Platform.runLater(() -> FindViewController.getDefault().setNumResultsFound(0));
        }
    }

    /**
     * Gets the lookForChoiceBox
     *
     * @return
     */
    public ChoiceBox<String> getLookForChoiceBox() {
        return lookForChoiceBox;
    }

    /**
     * Gets the postSearchChoiceBox. This contains: Replace Selection, Add To, Remove
     * From and Delete From
     *
     * @return currentSelectionChoiceBox
     */
    public ChoiceBox<String> getPostSearchChoiceBox() {
        return postSearchChoiceBox;
    }

    /**
     * Gets the matchCriteriaChoiceBox.This contains: All and Any
     *
     * @return matchCriteriaChoiceBox
     */
    public ChoiceBox<String> getMatchCriteriaChoiceBox() {
        return matchCriteriaChoiceBox;
    }

    /**
     * Gets the nodeFindCriteriaList. This is the list off all
     * advancedCriteriaBorderPanes for type Vertex
     *
     * @return nodeFindCriteriaList
     */
    public List<AdvancedCriteriaBorderPane> getNodeFindCriteriaList() {
        return nodeFindCriteriaList;
    }

    /**
     * Gets the transactionFindCriteriaList. This is the list off all
     * advancedCriteriaBorderPanes for type Transaction
     *
     * @return transactionFindCriteriaList
     */
    public List<AdvancedCriteriaBorderPane> getTransactionFindCriteriaList() {
        return transactionFindCriteriaList;
    }

    /**
     * Gets the addButton
     *
     * @return addCriteriaPaneButton
     */
    public Button getAddButton() {
        return addCriteriaPaneButton;
    }

    /**
     * Gets the FindNextButton
     *
     * @return findNextButton
     */
    public Button getFindNextButton() {
        return findNextButton;
    }

    /**
     * Gets the findPrevButton
     *
     * @return findPrevButton
     */
    public Button getFindPrevButton() {
        return findPrevButton;
    }

    /**
     * Gets the findAllButton
     *
     * @return findAllButton
     */
    public Button getFindAllButton() {
        return findAllButton;
    }

    /**
     * Gets and returns the deleteResultsButton
     *
     * @return deleteResultsButton
     */
    public Button getDeleteResultsButton() {
        return deleteResultsButton;
    }

}
