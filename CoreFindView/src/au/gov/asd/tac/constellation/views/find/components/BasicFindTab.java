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
package au.gov.asd.tac.constellation.views.find.components;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.gui.MultiChoiceInputField;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.views.find.FindViewController;
import au.gov.asd.tac.constellation.views.find.utilities.ActiveFindResultsList;
import au.gov.asd.tac.constellation.views.find.utilities.BasicFindReplaceParameters;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.openide.util.HelpCtx;

/**
 * BasicFindTab contains all the UI elements for the Basic find tab.
 *
 * @author Atlas139mkm
 */
public class BasicFindTab extends Tab {

    protected final FindViewTabs parentComponent;

    protected final VBox layers = new VBox();

    protected final GridPane textGrid = new GridPane();
    protected final GridPane settingsGrid = new GridPane();
    protected final HBox buttonsHBox = new HBox();
    protected final VBox buttonsVBox = new VBox();

    protected final Label findLabel = new Label("Find:");
    protected final TextField findTextField = new TextField();

    protected final Label lookForLabel = new Label("Look For:");
    protected final String[] elementTypes = {GraphElementType.VERTEX.getShortLabel(), GraphElementType.TRANSACTION.getShortLabel(), GraphElementType.EDGE.getShortLabel(), GraphElementType.LINK.getShortLabel()};
    protected final ChoiceBox<String> lookForChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(elementTypes));

    protected boolean onLoad = true;
    protected final Label inAttributesLabel = new Label("Search Attributes:");
    protected final MultiChoiceInputField<String> attributeFilterMultiChoiceInput = new MultiChoiceInputField<>();
    protected List<Attribute> attributes = new ArrayList<>();
    protected List<Attribute> selectedNodeAttributes = new ArrayList<>();
    protected List<Attribute> selectedTransAttributes = new ArrayList<>();
    protected List<Attribute> selectedEdgeAttributes = new ArrayList<>();
    protected List<Attribute> selectedLinkAttributes = new ArrayList<>();

    protected final ContextMenu contextMenu = new ContextMenu();
    protected final MenuItem selectAllMenuItem = new MenuItem("Select All");
    protected final MenuItem deselectAllMenuItem = new MenuItem("Deselect All");

    private static final long ATTRIBUTE_MODIFICATION_COUNTER = Long.MIN_VALUE;

    protected final GridPane preferencesGrid = new GridPane();
    protected final ToggleGroup textStyleTB = new ToggleGroup();
    protected final RadioButton standardRadioBtn = new RadioButton("Standard Text");
    protected final RadioButton regExBtn = new RadioButton("RegEx");
    protected final VBox toggleVBox = new VBox();
    protected final CheckBox ignoreCaseCB = new CheckBox("Ignore Case");
    protected final CheckBox exactMatchCB = new CheckBox("Exact Match Only");

    protected final Label searchInLabel = new Label("Search In:");
    protected final ChoiceBox searchInChoiceBox = new ChoiceBox();
    protected final Label postSearchLabel = new Label("Post-Search Action:");
    protected final ChoiceBox postSearchChoiceBox = new ChoiceBox();

    private final Label resultsFoundLabel = new Label();
    private final ImageView helpImage = new ImageView(UserInterfaceIconProvider.HELP.buildImage(16, ConstellationColor.SKY.getJavaColor()));
    private final Button helpButton = new Button("", helpImage);

    private final Button findNextButton = new Button("Find Next");
    private final Button findPrevButton = new Button("Find Previous");
    private final Button findAllButton = new Button("Find All");
    private final Button deleteResultsButton = new Button("Delete Results From Graph(s)");

    protected static final int LABEL_WIDTH = 90;
    protected static final int DROP_DOWN_WIDTH = 120;
    private static final Logger LOGGER = Logger.getLogger(BasicFindTab.class.getName());

    public BasicFindTab(final FindViewTabs parentComponent) {
        /**
         * Set the parent Component, set the text of the tab to Basic find, set
         * the grid content, and set the content to the layers which is
         * populated within setGridContent.
         */
        this.parentComponent = parentComponent;
        setText("Basic Find");
        setGridContent();
        setContent(layers);

        /**
         * Set the actions for changing the selected graphElementType in the
         * lookForChoiceBox. This should save the currently selected check boxes
         * for the old element, populate the attributes with the new
         * graphElementType and update the selectedAttributes to retrieve
         * potentially previously selected elements of the new GraphElementType
         */
        lookForChoiceBox.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observableValue, final String oldElement, final String newElement) -> {
            if (oldElement != null) {
                saveSelected(GraphElementType.getValue(oldElement));
            }
            populateAttributes(GraphElementType.getValue(newElement));
            updateSelectedAttributes(getMatchingAttributeList(GraphElementType.getValue(newElement)));
        });

        // set the action for changing the seleciton in the postSearchChoiceBox
        searchInChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> observableValue, final String oldElement, final String newElement) {
                updateSelectionFactors();
                updateBasicFindParamters();
            }
        });

        // set the action for changing the seleciton in the postSearchChoiceBox
        postSearchChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> observableValue, final String oldElement, final String newElement) {
                updateSelectionFactors();
                updateBasicFindParamters();
            }
        });

        //Set the actions for the 5 bottom buttons
        findAllButton.setOnAction(action -> findAllAction());
        findNextButton.setOnAction(action -> findNextAction());
        findPrevButton.setOnAction(action -> findPrevAction());
        deleteResultsButton.setOnAction(action -> deleteResultsAction());
        helpButton.setStyle("-fx-border-color: transparent; -fx-background-color: transparent; -fx-effect: null; ");
        helpButton.setOnAction(event -> new HelpCtx("au.gov.asd.tac.constellation.views.find").display());

        FindViewController.getDefault().getNumResultsFound().addListener((observable, oldValue, newValue) -> resultsFoundLabel.setText("Results Found: " + newValue));
    }

    /**
     * Sets all the UI elements to the basic find Tab
     */
    protected void setGridContent() {
        // Create column constrains for never and always grow
        final ColumnConstraints neverGrow = new ColumnConstraints();
        final ColumnConstraints alwaysGrow = new ColumnConstraints();
        alwaysGrow.setHgrow(Priority.ALWAYS);
        neverGrow.setHgrow(Priority.NEVER);

        /**
         * Set the padding for all of the grids that make up the BasicFindView
         */
        layers.setSpacing(5);
        textGrid.setPadding(new Insets(10, 10, 10, 10));
        settingsGrid.setPadding(new Insets(0, 10, 0, 10));
        preferencesGrid.setPadding(new Insets(5, 0, 5, 0));

        /**
         * Set the settings for the textGrid which contains the findLabel and
         * findTextField
         */
        findLabel.setMinWidth(LABEL_WIDTH);
        textGrid.setVgap(5);
        textGrid.add(findLabel, 0, 0);
        textGrid.add(findTextField, 1, 0);
        textGrid.getColumnConstraints().addAll(neverGrow, alwaysGrow);
        findTextField.requestFocus();

        /**
         * Set the preference for the lookForChoiceBox and label, and default
         * select Vertex
         */
        lookForLabel.setMinWidth(LABEL_WIDTH - settingsGrid.getHgap());
        lookForChoiceBox.setMinWidth(DROP_DOWN_WIDTH);
        lookForChoiceBox.getSelectionModel().select(GraphElementType.VERTEX.getShortLabel());

        // set the gaps for the settings grid
        settingsGrid.setHgap(5);
        settingsGrid.setVgap(5);

        // Add the lookForChoiceBox and label to the settingsGrid
        settingsGrid.add(lookForLabel, 0, 0);
        settingsGrid.add(lookForChoiceBox, 1, 0);

        // add the attributeFilterMultiChoiceInput and label to the settings grid
        settingsGrid.add(inAttributesLabel, 0, 1);
        settingsGrid.add(attributeFilterMultiChoiceInput, 1, 1);

        // set the maxWidth for the in attributes menu a default populate it
        // with vertex elements
        attributeFilterMultiChoiceInput.setMaxWidth(DROP_DOWN_WIDTH);
        populateAttributes(GraphElementType.VERTEX);

        // set the min width for the inAttributesLabel
        inAttributesLabel.setMinWidth(LABEL_WIDTH - settingsGrid.getHgap());

        // add selectAllMenuItem and deselectAllMenuItem to the contextMenu for
        // the attributesMenu
        contextMenu.getItems().addAll(selectAllMenuItem, deselectAllMenuItem);

        /**
         * Set the preferences for the check boxes and radio boxes in the
         * preferences grid
         */
        standardRadioBtn.setToggleGroup(textStyleTB);
        regExBtn.setToggleGroup(textStyleTB);
        standardRadioBtn.setSelected(true);
        toggleVBox.getChildren().addAll(standardRadioBtn, regExBtn);
        toggleVBox.setSpacing(5);
        preferencesGrid.add(toggleVBox, 0, 0, 1, 2);
        preferencesGrid.add(ignoreCaseCB, 1, 0);
        preferencesGrid.add(exactMatchCB, 1, 1);
        preferencesGrid.setHgap(5);
        preferencesGrid.setVgap(5);

        settingsGrid.add(preferencesGrid, 2, 0, 2, 2);

        /**
         * Add options to the searchInChoiceBox and set the default selection to
         * Current Selection. Set the sizing preferences and add the searchInLabel
         * and ChoiceBox to the settings grid
         */
        searchInChoiceBox.getItems().addAll("Current Graph", "Current Selection", "All Open Graphs");
        searchInChoiceBox.getSelectionModel().select(0);
        searchInChoiceBox.setMinWidth(DROP_DOWN_WIDTH);
        settingsGrid.add(searchInLabel, 0, 2);
        settingsGrid.add(searchInChoiceBox, 1, 2);

        /**
         * Add options to the postSearchChoiceBox and set the default selection to
         * Replace Selection. Set the sizing preferences and add the postSearchLabel
         * and ChoiceBox to the settings grid
         */
        postSearchChoiceBox.getItems().addAll("Replace Selection", "Add To Selection", "Remove From Selection");
        postSearchChoiceBox.getSelectionModel().select(0);
        postSearchChoiceBox.setMinWidth(DROP_DOWN_WIDTH);
        settingsGrid.add(postSearchLabel, 2, 2);
        settingsGrid.add(postSearchChoiceBox, 3, 2);

        // Set the preferences for the buttonsHbox and all relevant Buttons
        buttonsHBox.setAlignment(Pos.CENTER_LEFT);
        buttonsHBox.setPadding(new Insets(5, 10, 5, 10));
        buttonsHBox.setSpacing(5);
        buttonsHBox.getChildren().addAll(helpButton, deleteResultsButton, findAllButton, findPrevButton, findNextButton);
        buttonsHBox.setAlignment(Pos.CENTER_RIGHT);

        deleteResultsButton.setDisable(true);

        // add the buttonsHBox to the buttonsVbox
        buttonsVBox.getChildren().addAll(resultsFoundLabel, buttonsHBox);

        // Set the bottom of the pane to contain the buttonsVbox
        parentComponent.getParentComponent().setBottom(buttonsVBox);

        // add all the parent most grids to the layers
        layers.getChildren().addAll(textGrid, settingsGrid);
    }

    /**
     * Gets the BasicFindTabs parent component
     *
     * @return parentComponent
     */
    public FindViewTabs getParentComponent() {
        return parentComponent;
    }

    /**
     * Updates the current buttons at the bottom of the pane to match the
     * currently selected tab
     */
    public void updateButtons() {
        /**
         * Clear all buttons, add the relevant buttons and set the panes bottom
         * to the buttonsHbox
         */
        buttonsHBox.getChildren().clear();
        buttonsHBox.getChildren().addAll(helpButton, deleteResultsButton, findAllButton, findPrevButton, findNextButton);
        parentComponent.getParentComponent().setBottom(buttonsVBox);
    }

    /**
     * Populates the inAttributeMenu with all relevant String attributes based
     * on the type of graph element selected in the lookForChoiceBox
     *
     * @param type
     */
    public void populateAttributes(final GraphElementType type) {

        //retrieve a list of all current attributes that exist in all active
        //graphs that are of type string
        final List<String> attributeList = FindViewController.getDefault().populateAttributes(type, attributes, ATTRIBUTE_MODIFICATION_COUNTER);

        /**
         * Check if the current thread is the main application thread. If it is
         * run the logic. If it isnt set up a countdown latch to ensure it this
         * process is completed.
         */
        if (Platform.isFxApplicationThread()) {
            // clear all current checks and all items in the inAttributeMenu
            attributeFilterMultiChoiceInput.getCheckModel().clearChecks();
            attributeFilterMultiChoiceInput.getItems().clear();

            // Get the matching selected attribute list
            final List<Attribute> selected = getMatchingAttributeList(type);

            // loop through all attributes in the complete attribute list
            for (final String attribute : attributeList) {

                // add all attributes to the inAttributeMenu
                attributeFilterMultiChoiceInput.getItems().add(attribute);

                // loop through all selected attributes, reselect them if the
                // selected attributes name matches the current attribute
                for (int i = 0; i <= selected.size() - 1; i++) {
                    if (selected.get(i).getName() == attribute) {
                        attributeFilterMultiChoiceInput.getCheckModel().check(attribute);
                    }
                }
            }
        } else {
            final CountDownLatch cdl = new CountDownLatch(1);
            Platform.runLater(() -> {
                attributeFilterMultiChoiceInput.getCheckModel().clearChecks();
                attributeFilterMultiChoiceInput.getItems().clear();
                final List<Attribute> selected = getMatchingAttributeList(type);
                for (final String attribute : attributeList) {
                    attributeFilterMultiChoiceInput.getItems().add(attribute);
                    for (int i = 0; i <= selected.size() - 1; i++) {
                        if (selected.get(i).getName() == attribute) {
                            attributeFilterMultiChoiceInput.getCheckModel().check(attribute);
                        }
                    }
                }
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
     * Updates the check box menu with the items present within the selected
     * attributes list
     *
     * @param selectedAttributes
     */
    public void updateSelectedAttributes(final List<Attribute> selectedAttributes) {

        // Preform in a run later to ensure this process is done before the
        // next starts
        final CountDownLatch cdl = new CountDownLatch(1);
        Platform.runLater(() -> {
            // loops through all selectedAttributes
            for (int i = 0; i < selectedAttributes.size(); i++) {
                // checks if the current attribute is contained within
                // the attributes list
                if (checkSelectedContains(selectedAttributes.get(i), attributes)) {
                    // if it is check the matchng attribute in the
                    // inAttribute menu
                    attributeFilterMultiChoiceInput.getCheckModel().check(selectedAttributes.get(i).getName());
                }
            }
            cdl.countDown();
        });
    }

    /**
     * Gets the selected elements list for the matching type
     *
     * @param type
     * @return matching graphElementType list
     */
    public List<Attribute> getMatchingAttributeList(final GraphElementType type) {
        // based on the element type return the matching list
        return switch (type) {
            case VERTEX -> selectedNodeAttributes;
            case TRANSACTION -> selectedTransAttributes;
            case EDGE -> selectedEdgeAttributes;
            default -> selectedLinkAttributes;
        };
    }


    /**
     * Retrieves the selected list of the matching type, clears it, then re adds
     * all selected elements to that list
     *
     * @param type
     */
    public void saveSelected(final GraphElementType type) {
        final List<Attribute> selectedAttributes = getMatchingAttributeList(type);
        selectedAttributes.clear();

        // if the attributes list is not empty
        if (!attributes.isEmpty()) {
            // loop through all attributes
            for (final Attribute a : attributes) {
                // if there is attributes selected in the attributesMenu and
                // if that attribute is selected
                if ((!attributeFilterMultiChoiceInput.getCheckModel().isEmpty() && attributeFilterMultiChoiceInput.getCheckModel().isChecked(a.getName())) || onLoad) {
                    // add it to the selected attributes list
                    selectedAttributes.add(a);
                }
            }

            onLoad = false;
        }
    }

    /**
     * Checks if a attribute is present within a list of attributes, based on
     * the attributes name
     *
     * @param attribute
     * @param selectedAttributes
     * @return true if an attribute exists in the selectedAttributes else false
     */
    private boolean checkSelectedContains(final Attribute attribute, final List<Attribute> selectedAttributes) {
        for (final Attribute sa : selectedAttributes) {
            if (attribute.getName().equals(sa.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reads the find text, element type, attributes selected, standard text
     * selection, regEx selection, ignore case selection, exact match selected
     * and search all graphs selection and passes them to the controller to
     * create a BasicFindReplaceParameter
     */
    public void updateBasicFindParamters() {
        final GraphElementType elementType = GraphElementType.getValue(lookForChoiceBox.getSelectionModel().getSelectedItem());
        final List<Attribute> attributeList = new ArrayList<>(getMatchingAttributeList(elementType));
        boolean replaceSelection = false;
        boolean addTo = false;
        boolean removeFrom = false;
        boolean searchAllGraphs = false;
        boolean currentGraph = false;
        boolean currentSelection = false;

        // retrieves the currently selected index, setting the relevent boolean
        // to true
        switch (postSearchChoiceBox.getSelectionModel().getSelectedIndex()) {
            case 0 -> replaceSelection = true;
            case 1 -> addTo = true;
            case 2 -> removeFrom = true;
        }

        switch (searchInChoiceBox.getSelectionModel().getSelectedIndex()) {
            case 0 -> currentGraph = true;
            case 1 -> currentSelection = true;
            case 2 -> searchAllGraphs = true;
        }

        // creates a new basicFindReplaceParameter with the currently selected
        // UI parameters
        final BasicFindReplaceParameters parameters = new BasicFindReplaceParameters(findTextField.getText(), "",
                elementType, attributeList, standardRadioBtn.isSelected(), regExBtn.isSelected(),
                ignoreCaseCB.isSelected(), exactMatchCB.isSelected(), replaceSelection, addTo, removeFrom, false, currentSelection, currentGraph, searchAllGraphs
        );

        FindViewController.getDefault().updateBasicFindParameters(parameters);
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
     * This is run when the user presses the find all button. It confirms the
     * find string is not empty, saves the currently selected graph element,
     * updates the basic find parameters to ensure they are current then calls
     * the retrieveMatchinElements function in the FindViewController to call
     * the basicFindPlugin.
     */
    public void findAllAction() {
        if (!getFindTextField().getText().isEmpty()) {
            saveSelected(GraphElementType.getValue(getLookForChoiceBox().getSelectionModel().getSelectedItem()));
            updateBasicFindParamters();
            FindViewController.getDefault().retriveMatchingElements(true, false);
            getDeleteResultsButton().setDisable(false);
        }
    }

    /**
     * This is run when the user presses the find next button. It confirms the
     * find string is not empty, saves the currently selected graph element,
     * updates the basic find parameters to ensure they are current then calls
     * the retrieveMatchinElements function in the FindViewController to call
     * the basicFindPlugin.
     */
    public void findNextAction() {
        if (!getFindTextField().getText().isEmpty()) {
            saveSelected(GraphElementType.getValue(getLookForChoiceBox().getSelectionModel().getSelectedItem()));
            updateBasicFindParamters();
            FindViewController.getDefault().retriveMatchingElements(false, true);
        }
    }

    /**
     * This is run when the user presses the find previous button. It confirms
     * the find string is not empty, saves the currently selected graph element,
     * updates the basic find parameters to ensure they are current then calls
     * the retrieveMatchinElements function in the FindViewController to call
     * the basicFindPlugin.
     */
    public void findPrevAction() {
        if (!getFindTextField().getText().isEmpty()) {
            saveSelected(GraphElementType.getValue(getLookForChoiceBox().getSelectionModel().getSelectedItem()));
            updateBasicFindParamters();
            FindViewController.getDefault().retriveMatchingElements(false, false);
        }
    }

    /**
     * This is run when the user presses the delete results button.
     * It calls a dialog box to confirm that the user wishes to delete the results of the find from all graphs searched.
     * Then set the deleteResultsButton to be disabled to stop users from trying to delete results that have already been deleted.
     */
    private void deleteResultsAction() {
        if (!ActiveFindResultsList.getBasicResultsList().isEmpty()) {
            FindViewController.getDefault().deleteResults(ActiveFindResultsList.getBasicResultsList(), FindViewController.getGraphsSearched());
            deleteResultsButton.setDisable(true);
            Platform.runLater(() -> FindViewController.getDefault().setNumResultsFound(0));
        }
    }

    /**
     * requests the focus of the findTextField
     */
    public void requestTextFieldFocus() {
        findTextField.requestFocus();
    }

    /**
     * Gets and returns the findTextField
     *
     * @return findTextField
     */
    public TextField getFindTextField() {
        return findTextField;
    }

    /**
     * Gets and returns the findNextButton
     *
     * @return findNextButton
     */
    public Button getFindNextButton() {
        return findNextButton;
    }

    /**
     * Gets and returns the findPrevButton
     *
     * @return findPrevButton
     */
    public Button getFindPrevButton() {
        return findPrevButton;
    }

    /**
     * Gets and returns the findAllButton
     *
     * @return findAllButton
     */
    public Button getFindAllButton() {
        return findAllButton;
    }

    /**
     * Gets and returns the lookForChoiceBox
     *
     * @return lookForChoiceBox
     */
    public ChoiceBox<String> getLookForChoiceBox() {
        return lookForChoiceBox;
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
