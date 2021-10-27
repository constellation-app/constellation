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

        replaceAllButton.setOnAction(action -> {
            replaceAllAction();
        });
        replaceNextButton.setOnAction(action -> {
            replaceNextAction();
        });

    }

    /**
     * Adds the UI changes for the replace tab
     */
    private void setReplaceGridContent() {
        textGrid.add(replaceLabel, 0, 1);
        textGrid.add(replaceTextField, 1, 1);

        buttonsHBox.getChildren().clear();
        buttonsHBox.getChildren().addAll(replaceNextButton, replaceAllButton);

        currentSelectionChoiceBox.getItems().remove(1, 4);
        currentSelectionChoiceBox.getItems().add("Replace in");
        preferencesGrid.getChildren().remove(exactMatchCB);
    }

    public void updateButtons() {
        buttonsHBox.getChildren().clear();
        buttonsHBox.getChildren().addAll(replaceNextButton, replaceAllButton, searchAllGraphs);
        getParentComponent().getParentComponent().setBottom(buttonsVBox);

    }

    /**
     * Reads the find text, element type, attributes selected, standard text
     * selection, regEx selection, ignore case selection, exact match selected
     * and search all graphs selection and passes them to the controller to
     * create a BasicFindReplaceParameter
     */
    public void updateBasicReplaceParamters() {
        final GraphElementType elementType = GraphElementType.getValue(lookForChoiceBox.getSelectionModel().getSelectedItem());
        final List<Attribute> attributeList = new ArrayList<>(getMatchingAttributeList(elementType));
        boolean replaceIn = false;
        switch (currentSelectionChoiceBox.getSelectionModel().getSelectedIndex()) {
            case 0:
                break;
            case 1:
                replaceIn = true;
                break;
            default:
                break;
        }
        final BasicFindReplaceParameters parameters = new BasicFindReplaceParameters(findTextField.getText(), replaceTextField.getText(),
                elementType, attributeList, standardRadioBtn.isSelected(), regExBtn.isSelected(),
                ignoreCaseCB.isSelected(), exactMatchCB.isSelected(), false, false, false, replaceIn, searchAllGraphs.isSelected());

        FindViewController.getDefault().updateBasicReplaceParameters(parameters);
    }

    public void replaceAllAction() {
        if (!getFindTextField().getText().isEmpty() && !getReplaceTextField().getText().isEmpty()) {
            saveSelected(GraphElementType.getValue(getLookForChoiceBox().getSelectionModel().getSelectedItem()));
            updateBasicReplaceParamters();
            FindViewController.getDefault().replaceMatchingElements(true, false);
        }
    }

    public void replaceNextAction() {
        if (!getFindTextField().getText().isEmpty() && !getReplaceTextField().getText().isEmpty()) {
            saveSelected(GraphElementType.getValue(getLookForChoiceBox().getSelectionModel().getSelectedItem()));
            updateBasicReplaceParamters();
            FindViewController.getDefault().replaceMatchingElements(false, true);
        }
    }

    public TextField getReplaceTextField() {
        return replaceTextField;
    }

    public Button getReplaceNextButton() {
        return replaceNextButton;
    }

    public Button getReplaceAllButton() {
        return replaceAllButton;
    }


}
