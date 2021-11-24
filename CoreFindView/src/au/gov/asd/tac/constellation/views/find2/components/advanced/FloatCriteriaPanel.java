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
import au.gov.asd.tac.constellation.views.find2.components.AdvancedFindTab;
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

    public FloatCriteriaPanel(final AdvancedFindTab parentComponent, final String type, final GraphElementType graphElementType) {
        super(parentComponent, type, graphElementType);
        setGridContent();

        getFilterChoiceBox().getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observableValue, final String oldElement, final String newElement) -> {
            enableLabelAndSearchFieldtwo(newElement);
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

    @Override
    public String getType() {
        return "float"; //To change body of generated methods, choose Tools | Templates.
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

}
