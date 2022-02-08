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
import au.gov.asd.tac.constellation.graph.attribute.BooleanAttributeDescription;
import au.gov.asd.tac.constellation.views.find2.components.AdvancedFindTab;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.BooleanCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.FindCriteriaValues;
import javafx.geometry.Insets;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Child criteria BorderPane for the attributes of type boolean.
 *
 * @author Atlas139mkm
 */
public class BooleanCriteriaPanel extends AdvancedCriteriaBorderPane {

    private final TextField searchField = new TextField();

    private final ToggleGroup toggleGroup = new ToggleGroup();
    private final RadioButton trueToggle = new RadioButton("True");
    private final RadioButton falseToggle = new RadioButton("False");

    public BooleanCriteriaPanel(final AdvancedFindTab parentComponent, final String type, final GraphElementType graphElementType) {
        super(parentComponent, type, graphElementType);
        setGridContent();
    }

    /**
     * Sets the UI content of the pane
     */
    private void setGridContent() {
        HBox.setHgrow(searchField, Priority.ALWAYS);
        trueToggle.setToggleGroup(toggleGroup);
        falseToggle.setToggleGroup(toggleGroup);
        trueToggle.setSelected(true);

        /**
         * Filter box should only contain "Is" remove "Is Not". Add a two toggle
         * radio buttons for true and false
         */
        getFilterChoiceBox().getItems().remove("Is Not");
        getHboxBot().getChildren().addAll(trueToggle, falseToggle);
        getHboxBot().setPadding(new Insets(5));

    }

    private boolean isSelectedBoolean() {
        return trueToggle.isSelected();
    }

    /**
     * This returns a FindCriteriaValue, specifically a BooleanCriteriaValues
     * containing this panes selections and the selected boolean value
     *
     * @return
     */
    @Override
    public FindCriteriaValues getCriteriaValues() {
        return new BooleanCriteriaValues(getType(), getAttributeName(), getFilterChoiceBox().getSelectionModel().getSelectedItem(), isSelectedBoolean());
    }

    @Override
    public String getType() {
        return BooleanAttributeDescription.ATTRIBUTE_NAME;
    }
}
