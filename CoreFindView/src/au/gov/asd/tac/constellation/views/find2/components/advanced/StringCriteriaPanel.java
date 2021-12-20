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

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import au.gov.asd.tac.constellation.views.find2.components.AdvancedFindTab;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.FindCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.StringCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.utilities.UseListInputWindow;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Child criteria BorderPane for the attributes of type Float.
 *
 * @author Atlas139mkm
 */
public class StringCriteriaPanel extends AdvancedCriteriaBorderPane {

    private final TextField searchField = new TextField();
    private final CheckBox caseSensitiveCheckBox = new CheckBox("Aa");
    private final CheckBox useListCheckBox = new CheckBox("Use List");
    private final Button moreDetailsButton = new Button("List");

    public StringCriteriaPanel(final AdvancedFindTab parentComponent, final String type, final GraphElementType graphElementType) {
        super(parentComponent, type, graphElementType);
        setGridContent();
        caseSensitiveCheckBox.setOnAction(action -> setAaText(caseSensitiveCheckBox.selectedProperty().get()));
        useListCheckBox.setOnAction(action -> activateMoreDetails(useListCheckBox.selectedProperty().get()));

        getFilterChoiceBox().getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observableValue, final String oldElement, final String newElement) -> {
            regexSelectionAction(newElement);
        });

        // Opens the useListWindow when the user clicks the moreDetials Button
        // It also updates the useListWindows text to match the searchField text
        moreDetailsButton.setOnAction(action -> {
            UseListInputWindow useListWindow = new UseListInputWindow(this, searchField.getText());
            useListWindow.showAndWait();
            useListWindow.updateText(getSearchFieldText());
        });
    }

    /**
     * Sets the UI content of the pane
     */
    private void setGridContent() {
        HBox.setHgrow(searchField, Priority.ALWAYS);
        getHboxTop().getChildren().addAll(caseSensitiveCheckBox, useListCheckBox);
        getHboxBot().getChildren().addAll(searchField, moreDetailsButton);

        // Adds the below choices to the filterChoiceBox
        getFilterChoiceBox().getItems().addAll("Contains", "Doesn't Contain", "Begins With", "Ends With", "Matches (Regex)");

        // Disables the moreDetailsButton
        moreDetailsButton.setDisable(true);
    }

    /**
     * Sets the search field text to be the text from the UseListInputWindow. It
     * formats the text to no longer be separated by new lines but now by
     * commas.
     *
     * This \n is \n a \n test = This,is,a,test
     *
     * @param text
     */
    public void setSearchFieldText(final String text) {
        final StringBuilder sb = new StringBuilder();
        final String[] splitText = text.split(SeparatorConstants.NEWLINE);
        for (int i = 0; i < splitText.length; i++) {
            sb.append(splitText[i]);
            sb.append(i == splitText.length - 1 ? "" : ",");
        }
        sb.toString();
        searchField.setText(sb.toString());
    }

    /**
     * Gets the current text in the search field
     *
     * @return
     */
    public String getSearchFieldText() {
        return searchField.getText();
    }

    /**
     * Changes the check box, Aa, text to alter between "aa" and "Aa" to
     * indicate if the search is case sensitive or not. This is called when the
     * check box changes selection status.
     *
     * Aa for case sensitive aa for not case sensitive
     *
     * @param isChecked
     */
    private void setAaText(final boolean isChecked) {
        caseSensitiveCheckBox.setText(isChecked ? "aa" : "Aa");
    }

    /**
     * Disables / enables the moreDetailsButton based on if the useList CheckBox
     * is selected.
     *
     * @param isChecked
     */
    private void activateMoreDetails(final boolean isChecked) {
        moreDetailsButton.setDisable(!isChecked);
    }

    /**
     * Enables / Disables relevant UI elements based on the
     * filterChoiceBoxSelection. this will disable the caseSenstiveCheckBox and
     * UseListCheckBox if "Matches (Regex)" is selected
     *
     * @param choiceSelection
     */
    private void regexSelectionAction(final String choiceSelection) {
        caseSensitiveCheckBox.setDisable(choiceSelection.equals("Matches (Regex)"));
        useListCheckBox.setDisable(choiceSelection.equals("Matches (Regex)"));
        if (useListCheckBox.isSelected()) {
            useListCheckBox.setSelected(!choiceSelection.equals("Matches (Regex)"));
            moreDetailsButton.setDisable(choiceSelection.equals("Matches (Regex)"));
        }
    }

    /**
     * This returns a FindCriteriaValue, specifically a StringCriteriaValues
     * containing this panes selections and the text values input into the text
     * field.
     *
     * @return
     */
    @Override
    public FindCriteriaValues getCriteriaValues() {
        // if use list is selected, retrieve all values within the list
        if (useListCheckBox.isSelected()) {
            final String[] splitStrings = searchField.getText().split(SeparatorConstants.COMMA);
            final List<String> stringList = new ArrayList<>();
            Collections.addAll(stringList, splitStrings);
            return new StringCriteriaValues(getType(), getAttributeName(), getFilterChoiceBox().getSelectionModel().getSelectedItem(),
                    stringList, caseSensitiveCheckBox.isSelected(), useListCheckBox.isSelected());
        }

        // else, retrieve the text field as 1 string
        return new StringCriteriaValues(getType(), getAttributeName(), getFilterChoiceBox().getSelectionModel().getSelectedItem(),
                searchField.getText(), caseSensitiveCheckBox.isSelected(), useListCheckBox.isSelected());
    }

    @Override
    public String getType() {
        return "string"; //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getDisplayString() {
        return "";
    }

}
