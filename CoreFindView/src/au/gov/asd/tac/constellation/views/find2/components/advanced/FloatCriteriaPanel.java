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
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.views.find2.components.AdvancedFindTab;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.FindCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.FloatCriteriaValues;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Child criteria BorderPane for the attributes of type Float.
 *
 * @author Atlas139mkm
 */
public class FloatCriteriaPanel extends AdvancedCriteriaBorderPane {

    private final TextField searchField = new TextField();
    private final Label andLabel = new Label("And");
    private final TextField searchFieldTwo = new TextField();
    private static final Logger LOG = Logger.getLogger(FloatCriteriaPanel.class.getName());

    public FloatCriteriaPanel(final AdvancedFindTab parentComponent, final String type, final GraphElementType graphElementType) {
        super(parentComponent, type, graphElementType);
        setGridContent();

        getFilterChoiceBox().getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observableValue, final String oldElement, final String newElement) -> {
            enableLabelAndSearchFieldtwo(newElement);
        });

        searchField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> observable, String oldValue,
                    String newValue) {
                // Retrieve the index of a "."
                int index = searchField.getText().indexOf(".");
                // Save the current text but replace all non numbers with nothing
                String text = newValue.replaceAll("[^0-9]", "");
                //if the newValue isnt a number and a "." was found
                if (!newValue.matches("[0-9]") && (index != -1)) {
                    // save the text to re add the "." back in its orignal spot
                    text = text.substring(0, index) + "." + text.substring(index);
                }
                // set the text to text
                searchField.setText(text);
            }
        });
        searchFieldTwo.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> observable, String oldValue,
                    String newValue) {
                // Retrieve the index of a "."
                int index = searchFieldTwo.getText().indexOf(".");
                // Save the current text but replace all non numbers with nothing
                String text = newValue.replaceAll("[^0-9]", "");
                //if the newValue isnt a number and a "." was found
                if (!newValue.matches("[0-9]") && (index != -1)) {
                    // save the text to re add the "." back in its orignal spot
                    text = text.substring(0, index) + "." + text.substring(index);
                }
                // set the text to text
                searchFieldTwo.setText(text);
            }
        });

    }

    /**
     * Sets the UI content of the pane
     */
    private void setGridContent() {
        HBox.setHgrow(searchField, Priority.ALWAYS);
        // Adds the below choices to the filterChoiceBox
        getFilterChoiceBox().getItems().addAll("Is Less Than", "Is Greater Than", "Is Between");
        getHboxBot().getChildren().addAll(searchField, andLabel, searchFieldTwo);

        // disables the relevant buttons for Is Between Searches
        andLabel.setDisable(true);
        searchFieldTwo.setDisable(true);
    }

    /**
     * This returns a FindCriteriaValue, specifically a FloatCriteriaValues
     * containing this panes selections and the current float values
     *
     * @return
     */
    @Override
    public FindCriteriaValues getCriteriaValues() {
        final float floatNumberPrimary = searchField.getText().isBlank() ? 0 : Float.parseFloat(searchField.getText());
        if (getFilterChoiceBox().getSelectionModel().getSelectedItem().equals("Is Between")) {
            final float floatNumberSecondary = searchFieldTwo.getText().isBlank() ? 0 : Float.parseFloat(searchFieldTwo.getText());
            return new FloatCriteriaValues(getType(), getAttributeName(), getFilterChoiceBox().getSelectionModel().getSelectedItem(), floatNumberPrimary, floatNumberSecondary);
        }
        return new FloatCriteriaValues(getType(), getAttributeName(), getFilterChoiceBox().getSelectionModel().getSelectedItem(), floatNumberPrimary);
    }

    /**
     * Enables / Disables the andLabel and the searchFieldTwo based on the
     * choiceSelection passed in. This is called when the user changes
     * filterChoiceBoxSelection
     *
     * @param choiceSelection
     */
    private void enableLabelAndSearchFieldtwo(final String choiceSelection) {
        andLabel.setDisable(!choiceSelection.equals("Is Between"));
        searchFieldTwo.setDisable(!choiceSelection.equals("Is Between"));
    }

    @Override
    public String getType() {
        return FloatAttributeDescription.ATTRIBUTE_NAME;
    }
}
