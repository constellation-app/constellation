/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.attributeeditor.editors;

import au.gov.asd.tac.constellation.graph.attribute.interaction.ValueValidator;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.EditOperation;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Editor Factory for selecting items in a list of strings
 *
 * @author twilight_sparkle
 */
public class ListSelectionEditorFactory extends AbstractEditorFactory<List<String>> {

    @Override
    public AbstractEditor<List<String>> createEditor(final EditOperation editOperation, final List<String> defaultValue, final ValueValidator<List<String>> validator, final String editedItemName, final List<String> initialValue) {
        return new ListSelectionEditor(editOperation, defaultValue, validator, editedItemName, initialValue);
    }

    public class ListSelectionEditor extends AbstractEditor<List<String>> {

        private List<String> possibleItems;
        private ObservableList<String> availableItemsList;
        private ObservableList<String> selectedItemsList;

        protected ListSelectionEditor(final EditOperation editOperation, final List<String> defaultValue, final ValueValidator<List<String>> validator, final String editedItemName, final List<String> initialValue) {
            super(editOperation, defaultValue, validator, editedItemName, initialValue);
        }

        public void setPossibleItems(final List<String> possibleItems) {
            this.possibleItems = new ArrayList<>(possibleItems);
        }

        @Override
        protected boolean canSet(final List<String> value) {
            return value != null;
        }

        @Override
        public void updateControlsWithValue(final List<String> value) {
            selectedItemsList.clear();
            availableItemsList.clear();
            possibleItems.forEach(item -> {
                if (value.contains(item)) {
                    selectedItemsList.add(item);
                } else {
                    availableItemsList.add(item);
                }
            });
        }

        @Override
        protected List<String> getValueFromControls() {
            return new ArrayList<>(selectedItemsList);
        }

        @Override
        protected Node createEditorControls() {
            availableItemsList = FXCollections.observableArrayList();
            final Label nonSelectedLabel = new Label("Available Items:");
            final ListView<String> availableItems = new ListView<>(availableItemsList);
            
            final VBox nonSelectedItemsBox = new VBox(CONTROLS_DEFAULT_VERTICAL_SPACING, 
                    nonSelectedLabel, availableItems);
            
            selectedItemsList = FXCollections.observableArrayList();
            final Label selectedLabel = new Label("Selected Items:");
            final ListView<String> selectedItems = new ListView<>(selectedItemsList);
            
            final VBox selectedItemsBox = new VBox(CONTROLS_DEFAULT_VERTICAL_SPACING, 
                    selectedLabel, selectedItems);
            
            final Button addButton = new Button("", new ImageView(UserInterfaceIconProvider.CHEVRON_RIGHT.buildImage(16)));
            addButton.setOnAction(event -> {
                final String selectedItem = availableItems.getSelectionModel().getSelectedItem();
                if (selectedItem == null) {
                    return;
                }
                availableItemsList.remove(selectedItem);
                selectedItemsList.add(selectedItem);
                update();
            });
            
            final Button removeButton = new Button("", new ImageView(UserInterfaceIconProvider.CHEVRON_LEFT.buildImage(16)));
            removeButton.setOnAction(event -> {
                final String selectedItem = selectedItems.getSelectionModel().getSelectedItem();
                if (selectedItem == null) {
                    return;
                }
                availableItemsList.add(selectedItem);
                selectedItemsList.remove(selectedItem);
                update();
            });

            
            final VBox addAndRemoveButtons = new VBox(CONTROLS_DEFAULT_VERTICAL_SPACING, 
                    addButton, removeButton);
            addAndRemoveButtons.setAlignment(Pos.CENTER);
            
            final HBox controls = new HBox(nonSelectedItemsBox, addAndRemoveButtons, selectedItemsBox);
            controls.setAlignment(Pos.CENTER);
            
            return controls;
        }
    }
}
