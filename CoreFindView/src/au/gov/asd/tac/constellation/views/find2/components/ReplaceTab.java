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
package au.gov.asd.tac.constellation.views.find2.components;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.views.find2.FindViewController;
import au.gov.asd.tac.constellation.views.find2.utilities.BasicFindReplaceParameters;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * This class contains all the UI elements for the replace Tab
 *
 * @author Atlas139mkm
 */
public class ReplaceTab extends BasicFindTab {

    private final Label replaceLabel = new Label("Replace With:");
    private final TextField replaceTextField = new TextField();

    private final Button replaceNextButton = new Button("Replace Next");
    private final Button replaceAllButton = new Button("Replace All");

    public ReplaceTab(final FindViewTabs parentComponent) {
        super(parentComponent);
        this.setText("Replace");
        setReplaceGridContent();

        // Sets the actions for the replace all and replace next buttons.
        replaceAllButton.setOnAction(action -> replaceAllAction());
        replaceNextButton.setOnAction(action -> replaceNextAction());

    }

    /**
     * Adds the UI changes for the replace tab. This removes the content from
     * the unneeded basic find tab and adds anything new specific to the replace
     * tab
     */
    private void setReplaceGridContent() {
        // Add the replace text box and label
        textGrid.add(replaceLabel, 0, 1);
        textGrid.add(replaceTextField, 1, 1);

        // remove the buttons at the bottom of the pane
        buttonsHBox.getChildren().clear();

        // add the replace buttons
        buttonsHBox.getChildren().addAll(replaceNextButton, replaceAllButton);

        // remove addTo, findIn, removeFrom
        postSearchChoiceBox.getItems().remove(0, 3);

        // add replaceIn
        postSearchChoiceBox.getItems().add("Replace In");
        postSearchChoiceBox.getSelectionModel().select(0);

        // remove the choice box from the view
        // the options are still altered in this choicebox to have replace in selected to
        // ensure the parameters for the search are determined correctly 
        settingsGrid.getChildren().remove(postSearchChoiceBox);
        settingsGrid.getChildren().remove(postSearchLabel);

        // remove exact match checkBox
        preferencesGrid.getChildren().remove(exactMatchCB);
    }

    /**
     * Updates the buttons at the bottom of the pane to be specific to the
     * replace tab.
     */
    @Override
    public void updateButtons() {
        buttonsHBox.getChildren().clear();
        buttonsHBox.getChildren().addAll(replaceAllButton, replaceNextButton);
        getParentComponent().getParentComponent().setBottom(buttonsVBox);
    }

    /**
     * Reads the find text, element type, attributes selected, standard text
     * selection, regEx selection, ignore case selection, exact match selected
     * and search all graphs selection and passes them to the controller to
     * create a BasicFindReplaceParameter
     */
    public void updateBasicReplaceParamters() {
        // get the currently selected graphElementType
        final GraphElementType elementType = GraphElementType.getValue(lookForChoiceBox.getSelectionModel().getSelectedItem());

        // get the matching attributeList
        final List<Attribute> attributeList = new ArrayList<>(getMatchingAttributeList(elementType));
        boolean replaceIn = false;

        // determine what currentSelectionChoiceBox option is selected
        if (postSearchChoiceBox.getSelectionModel().getSelectedIndex() == 1) {
            replaceIn = true;
        }

        boolean searchAllGraphs = false;
        boolean currentGraph = false;
        boolean currentSelection = false;

        switch (searchInChoiceBox.getSelectionModel().getSelectedIndex()) {
            case 0:
                currentGraph = true;
                break;
            case 1:
                currentSelection = true;
                break;
            case 2:
                searchAllGraphs = true;
                break;
            default:
                break;
        }
        // Create the paramters with the current UI selections
        final BasicFindReplaceParameters parameters = new BasicFindReplaceParameters(findTextField.getText(), replaceTextField.getText(),
                elementType, attributeList, standardRadioBtn.isSelected(), regExBtn.isSelected(),
                ignoreCaseCB.isSelected(), exactMatchCB.isSelected(), false, false, false, replaceIn, currentSelection, currentGraph, searchAllGraphs);

        // Update the basic replace paramters with the newly created parameter
        FindViewController.getDefault().updateBasicReplaceParameters(parameters);
    }

    /**
     * This is run when the user presses the replace All button. It confirms the
     * find and replace strings are not empty, saves the currently selected
     * graph element, updates the basic replace parameters to ensure they are
     * current then calls the replaceMatchinElements function in the
     * FindViewController to call the replacePlugin.
     */
    public void replaceAllAction() {
        if (!getFindTextField().getText().isEmpty() && !getReplaceTextField().getText().isEmpty()) {
            saveSelected(GraphElementType.getValue(getLookForChoiceBox().getSelectionModel().getSelectedItem()));
            updateBasicReplaceParamters();
            FindViewController.getDefault().replaceMatchingElements(true, false);
        }
    }

    /**
     * This is run when the user presses the replace next button. It confirms
     * the find and replace strings are not empty, saves the currently selected
     * graph element, updates the basic replace parameters to ensure they are
     * current then calls the replaceMatchinElements function in the
     * FindViewController to call the replacePlugin.
     */
    public void replaceNextAction() {
        if (!getFindTextField().getText().isEmpty() && !getReplaceTextField().getText().isEmpty()) {
            saveSelected(GraphElementType.getValue(getLookForChoiceBox().getSelectionModel().getSelectedItem()));
            updateBasicReplaceParamters();
            FindViewController.getDefault().replaceMatchingElements(false, true);
        }
    }

    /**
     * Gets the replaceTextField
     *
     * @return replaceTextField
     */
    public TextField getReplaceTextField() {
        return replaceTextField;
    }

    /**
     * Gets the replaceNextButton
     *
     * @return replaceNextButton
     */
    public Button getReplaceNextButton() {
        return replaceNextButton;
    }

    /**
     * Gets the replaceAllButton
     *
     * @return replaceAllButton
     */
    public Button getReplaceAllButton() {
        return replaceAllButton;
    }

}
