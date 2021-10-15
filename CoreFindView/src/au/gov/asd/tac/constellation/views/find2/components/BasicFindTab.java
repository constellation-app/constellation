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
import au.gov.asd.tac.constellation.views.find2.utilities.BasicFindReplaceParameters;
import au.gov.asd.tac.constellation.views.find2.FindViewController;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
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
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.controlsfx.control.CheckComboBox;

/**
 * BasicFindTab contains all the UI elements for the Basic find functionality
 *
 * @author Atlas139mkm
 */
public class BasicFindTab extends Tab {

    protected final FindViewTabs parentComponent;

    // TODO: name this something else other than "layers"
    protected final VBox layers = new VBox();

    protected final GridPane textGrid = new GridPane();
    protected final GridPane settingsGrid = new GridPane();
    protected final GridPane addRemoveSelectionGrid = new GridPane();
    protected final HBox buttonsHBox = new HBox();
    protected final VBox buttonsVBox = new VBox();

    protected final Label findLabel = new Label("Find:");
    protected final TextField findTextField = new TextField();

    protected final Label lookForLabel = new Label("Look For:");
    protected final String[] elementTypes = {GraphElementType.VERTEX.getShortLabel(), GraphElementType.TRANSACTION.getShortLabel(), GraphElementType.EDGE.getShortLabel(), GraphElementType.LINK.getShortLabel()};
    protected final ChoiceBox<String> lookForChoiceBox = new ChoiceBox<String>(FXCollections.observableArrayList(elementTypes));

    protected final Label inAttributesLabel = new Label("In Attributes:");
    protected final CheckComboBox<String> inAttributesMenu = new CheckComboBox<String>();
    protected List<Attribute> attributes = new ArrayList<>();
    protected List<Attribute> selectedNodeAttributes = new ArrayList<>();
    protected List<Attribute> selectedTransAttributes = new ArrayList<>();
    protected List<Attribute> selectedEdgeAttributes = new ArrayList<>();
    protected List<Attribute> selectedLinkAttributes = new ArrayList<>();

    final ContextMenu contextMenu = new ContextMenu();
    final MenuItem selectAllMenuItem = new MenuItem("Select All");
    final MenuItem deselectAllMenuItem = new MenuItem("Deselect All");

    final long attributeModificationCounter = Long.MIN_VALUE;

    // need to add menu items
    protected final GridPane preferencesGrid = new GridPane();
    protected final ToggleGroup textStyleTB = new ToggleGroup();
    protected final RadioButton standardRadioBtn = new RadioButton("Standard Text");
    protected final RadioButton regExBtn = new RadioButton("RegEx");
    protected final VBox toggleVBox = new VBox();
    protected final CheckBox ignoreCaseCB = new CheckBox("Ignore Case");
    protected final CheckBox exactMatchCB = new CheckBox("Exact Match Only");

    protected final CheckBox addToCurrent = new CheckBox("Add to Current Selection");
    protected final CheckBox removeFromCurrent = new CheckBox("Remove from Current Selection");
    protected final CheckBox findInSelected = new CheckBox("Find In Current Selection");
    protected final CheckBox searchAllGraphs = new CheckBox("Search all open Graphs");

    private final Button findNextButton = new Button("Find Next");
    private final Button findPrevButton = new Button("Find Prev");
    private final Button findAllButton = new Button("Find All");

    protected final int LABEL_WIDTH = 90;
    protected final int DROP_DOWN_WIDTH = 120;

    protected static final Logger LOGGER = Logger.getLogger(BasicFindTab.class.getName());

    public BasicFindTab(final FindViewTabs parentComponent) {
        this.parentComponent = parentComponent;
        setText("Basic Find");
        setGridContent();
        setContent(layers);

        lookForChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldElement, String newElement) {
                if (oldElement != null) {
                    saveSelected(GraphElementType.getValue(oldElement));
                }
                inAttributesMenu.getCheckModel().clearChecks();
                populateAttributes(GraphElementType.getValue(newElement));
                updateSelectedAttributes(getMatchingAttributeList(GraphElementType.getValue(newElement)));

            }
        });

        selectAllMenuItem.setOnAction(event -> {
            inAttributesMenu.getCheckModel().checkAll();
        });
        deselectAllMenuItem.setOnAction(event -> {
            inAttributesMenu.getCheckModel().clearChecks();
        });
        inAttributesMenu.setOnContextMenuRequested(event -> {
            contextMenu.show(inAttributesMenu, event.getScreenX(), event.getScreenY());
        });
        addToCurrent.setOnAction(action -> {
            removeFromCurrent.setSelected(false);
            findInSelected.setSelected(false);
            findNextButton.setDisable(false);
            findPrevButton.setDisable(false);
            updateSelectionFactors();
        });
        removeFromCurrent.setOnAction(action -> {
            addToCurrent.setSelected(false);
            findInSelected.setSelected(false);
            findNextButton.setDisable(false);
            findPrevButton.setDisable(false);
            updateSelectionFactors();
        });
        findInSelected.setOnAction(action -> {
            addToCurrent.setSelected(false);
            removeFromCurrent.setSelected(false);
            findNextButton.setDisable(true);
            findPrevButton.setDisable(true);
            updateSelectionFactors();
        });

        findAllButton.setOnAction(action -> {
            findAllAction();
        });
        findNextButton.setOnAction(action -> {
            findNextAction();
        });
        findPrevButton.setOnAction(action -> {
            findPrevAction();
        });
    }

    /**
     * Sets all the UI elements to the basic find Tab
     */
    private void setGridContent() {
        final ColumnConstraints neverGrow = new ColumnConstraints();
        final ColumnConstraints alwaysGrow = new ColumnConstraints();
        alwaysGrow.setHgrow(Priority.ALWAYS);
        neverGrow.setHgrow(Priority.NEVER);

        layers.setSpacing(5);
        textGrid.setPadding(new Insets(10, 10, 10, 10));
        settingsGrid.setPadding(new Insets(0, 10, 0, 10));
        addRemoveSelectionGrid.setPadding(new Insets(10, 10, 10, 10));
        preferencesGrid.setPadding(new Insets(5, 0, 5, 0));

        findLabel.setMinWidth(LABEL_WIDTH);
        textGrid.setVgap(5);
        textGrid.add(findLabel, 0, 0);
        textGrid.add(findTextField, 1, 0);
        textGrid.getColumnConstraints().addAll(neverGrow, alwaysGrow);
        findTextField.requestFocus();

        settingsGrid.setHgap(5);
        settingsGrid.setVgap(5);

        lookForLabel.setMinWidth(LABEL_WIDTH - settingsGrid.getHgap());
        lookForChoiceBox.setMinWidth(DROP_DOWN_WIDTH);
        lookForChoiceBox.getSelectionModel().select(GraphElementType.VERTEX.getShortLabel());
        settingsGrid.add(lookForLabel, 0, 0);
        settingsGrid.add(lookForChoiceBox, 1, 0);

        inAttributesLabel.setMinWidth(LABEL_WIDTH - settingsGrid.getHgap());

        settingsGrid.add(inAttributesLabel, 0, 1);
        settingsGrid.add(inAttributesMenu, 1, 1);

        inAttributesMenu.setMaxWidth(DROP_DOWN_WIDTH);
        populateAttributes(GraphElementType.VERTEX);

        contextMenu.getItems().addAll(selectAllMenuItem, deselectAllMenuItem);

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

        addRemoveSelectionGrid.add(addToCurrent, 0, 0);
        addRemoveSelectionGrid.add(removeFromCurrent, 1, 0);
        addRemoveSelectionGrid.add(findInSelected, 0, 1);
        addRemoveSelectionGrid.setHgap(5);
        addRemoveSelectionGrid.setVgap(5);

        buttonsHBox.setAlignment(Pos.CENTER_LEFT);
        buttonsHBox.setPadding(new Insets(5, 10, 5, 10));
        buttonsHBox.setSpacing(5);
        buttonsHBox.getChildren().addAll(findPrevButton, findNextButton, findAllButton, searchAllGraphs);

        buttonsVBox.getChildren().addAll(addRemoveSelectionGrid, buttonsHBox);

        parentComponent.getParentComponent().setBottom(buttonsVBox);

        layers.getChildren().addAll(textGrid, settingsGrid);

    }

    public FindViewTabs getParentComponent() {
        return parentComponent;
    }

    public void updateButtons() {
        buttonsHBox.getChildren().clear();
        buttonsHBox.getChildren().addAll(findPrevButton, findNextButton, findAllButton, searchAllGraphs);
        parentComponent.getParentComponent().setBottom(buttonsVBox);
    }

    /**
     * Populates the inAttributeMenu with all relevant String attributes based
     * on the type of graph element selected in the lookForChoiceBox
     *
     * @param type
     */
    public void populateAttributes(final GraphElementType type) {
        final List<String> attributeList = FindViewController.getDefault().populateAttributes(type, attributes, attributeModificationCounter);
        inAttributesMenu.getItems().clear();

        final List<Attribute> selected = getMatchingAttributeList(type);
        for (final String attribute : attributeList) {
            inAttributesMenu.getItems().add(attribute);
            for (int i = 0; i < selected.size() - 1; i++) {
                if (selected.get(i).getName() == attribute) {
                    inAttributesMenu.getCheckModel().check(attribute);
                }
            }
        }
    }

    /**
     * updates the check box menu with the items present within the selected
     * attributes list
     *
     * @param selectedAttributes
     */
    public void updateSelectedAttributes(final List<Attribute> selectedAttributes) {
        for (int i = 0; i < selectedAttributes.size(); i++) {
            if (checkSelectedContains(selectedAttributes.get(i), attributes)) {
                inAttributesMenu.getCheckModel().check(selectedAttributes.get(i).getName());
            }
        }
    }

    /**
     * Gets the selected elements list for the matching type
     *
     * @param type
     * @return
     */
    public List<Attribute> getMatchingAttributeList(final GraphElementType type) {
        switch (type) {
            case VERTEX:
                return selectedNodeAttributes;
            case TRANSACTION:
                return selectedTransAttributes;
            case EDGE:
                return selectedEdgeAttributes;
            default:
                return selectedLinkAttributes;
        }
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

        for (final Attribute a : attributes) {
            if (a.getAttributeType().equals("string")) {
                if (inAttributesMenu.getCheckModel().isChecked(a.getName())) {
                    selectedAttributes.add(a);
                }
            }
        }
    }

    /**
     * Checks if a attribute is present within a list of attributes, based on
     * the attributes name
     *
     * @param attribute
     * @param selectedAttributes
     * @return
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

        final BasicFindReplaceParameters parameters = new BasicFindReplaceParameters(findTextField.getText(), "",
                elementType, attributeList, standardRadioBtn.isSelected(), regExBtn.isSelected(),
                ignoreCaseCB.isSelected(), exactMatchCB.isSelected(), searchAllGraphs.isSelected());

        FindViewController.getDefault().updateBasicFindParameters(parameters);
    }

    /**
     * This is called on add and remove to current selection toggles to update
     * the variables values stored in the controller
     */
    public void updateSelectionFactors() {
        FindViewController.getDefault().updateSelectionFactors(addToCurrent.isSelected(), removeFromCurrent.isSelected(), findInSelected.isSelected());
    }

    public void findAllAction() {
        if (!findTextField.getText().isEmpty()) {
            saveSelected(GraphElementType.getValue(lookForChoiceBox.getSelectionModel().getSelectedItem()));
            updateBasicFindParamters();
            FindViewController.getDefault().retriveMatchingElements(true, false);
        }
    }

    public void findNextAction() {
        if (!findTextField.getText().isEmpty()) {
            saveSelected(GraphElementType.getValue(lookForChoiceBox.getSelectionModel().getSelectedItem()));
            updateBasicFindParamters();
            FindViewController.getDefault().retriveMatchingElements(false, true);
        }
    }

    public void findPrevAction() {
        if (!findTextField.getText().isEmpty()) {
            saveSelected(GraphElementType.getValue(lookForChoiceBox.getSelectionModel().getSelectedItem()));
            updateBasicFindParamters();
            FindViewController.getDefault().retriveMatchingElements(false, false);
        }
    }

    public void requestTextFieldFocus() {
        findTextField.requestFocus();
    }
}
