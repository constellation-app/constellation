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

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.attribute.VertexAttributeNameAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.interaction.ValueValidator;
import au.gov.asd.tac.constellation.graph.utilities.AttributeUtilities;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.EditOperation;
import java.util.Collections;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AttributeValueEditorFactory.class)
public class VertexAttributeNameEditorFactory extends AttributeValueEditorFactory<String> {

    @Override
    public AbstractEditor<String> createEditor(final EditOperation editOperation, final String defaultValue, final ValueValidator<String> validator, final String editedItemName, final String initialValue) {
        return new VertexAttributeNameEditor(editOperation, defaultValue, validator, editedItemName, initialValue);
    }

    @Override
    public String getAttributeType() {
        return VertexAttributeNameAttributeDescription.ATTRIBUTE_NAME;
    }

    public class VertexAttributeNameEditor extends AbstractEditor<String> {

        private ListView<String> attributeList;
        private TextField nameText;
        private boolean selectionIsActive = false;

        protected VertexAttributeNameEditor(final EditOperation editOperation, final String defaultValue, final ValueValidator<String> validator, final String editedItemName, final String initialValue) {
            super(editOperation, defaultValue, validator, editedItemName, initialValue);
        }

        @Override
        public void updateControlsWithValue(final String value) {
            if (attributeList.getItems().contains(value)) {
                attributeList.getSelectionModel().select(value);
            } else {
                nameText.setText(value);
                attributeList.getSelectionModel().clearSelection();
            }
        }

        @Override
        protected String getValueFromControls() throws ControlsInvalidException {
            if (attributeList.getSelectionModel().getSelectedItem() != null) {
                return attributeList.getSelectionModel().getSelectedItem();
            } else if (attributeList.getItems().contains(nameText.getText())) {
                return nameText.getText();
            } else {
                throw new ControlsInvalidException("Entered value is not an attribute which exists on this graph.");
            }
        }

        @Override
        protected Node createEditorControls() {
            attributeList = new ListView<>();
            
            final Label nameLabel = new Label("Attribute Name:");
            nameText = new TextField();
            nameText.textProperty().addListener(ev -> {
                if (!selectionIsActive) {
                    attributeList.getSelectionModel().select(null);
                }
                update();
            });
                      
            final Label listLabel = new Label("Current Attributes:");
            final List<String> attributeNames = AttributeUtilities.getAttributeNames(GraphElementType.VERTEX);
            Collections.sort(attributeNames);
            attributeList.getItems().addAll(attributeNames);

            attributeList.getSelectionModel().selectedItemProperty().addListener(ev -> {
                selectionIsActive = true;
                final String attributeName = attributeList.getSelectionModel().getSelectedItem();
                if (attributeName != null) {
                    nameText.setText(attributeName);
                }
                selectionIsActive = false;
            });
            
            final VBox controls = new VBox(CONTROLS_DEFAULT_VERTICAL_SPACING);

            controls.getChildren().addAll(nameLabel, nameText, listLabel, attributeList);
            return controls;
        }
    }
}
