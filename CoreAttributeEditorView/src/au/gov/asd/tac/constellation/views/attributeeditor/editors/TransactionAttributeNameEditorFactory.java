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
package au.gov.asd.tac.constellation.views.attributeeditor.editors;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.attribute.TransactionAttributeNameAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.interaction.ValueValidator;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.DefaultGetter;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.EditOperation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AttributeValueEditorFactory.class)
public class TransactionAttributeNameEditorFactory extends AttributeValueEditorFactory<String> {

    @Override
    public AbstractEditor<String> createEditor(final EditOperation editOperation, final DefaultGetter<String> defaultGetter, final ValueValidator<String> validator, final String editedItemName, final String initialValue) {
        return new TransactionAttributeNameEditor(editOperation, defaultGetter, validator, editedItemName, initialValue);
    }

    @Override
    public String getAttributeType() {
        return TransactionAttributeNameAttributeDescription.ATTRIBUTE_NAME;
    }

    public class TransactionAttributeNameEditor extends AbstractEditor<String> {

        private ListView<String> attributeList;
        private TextField nameText;
        private boolean selectionIsActive = false;

        protected TransactionAttributeNameEditor(final EditOperation editOperation, final DefaultGetter<String> defaultGetter, final ValueValidator<String> validator, final String editedItemName, final String initialValue) {
            super(editOperation, defaultGetter, validator, editedItemName, initialValue);
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
            final GridPane controls = new GridPane();
            controls.setAlignment(Pos.CENTER);
            controls.setVgap(CONTROLS_DEFAULT_VERTICAL_SPACING);

            attributeList = new ListView<>();

            final List<String> attributeNames = new ArrayList<>();
            // get all vertex attributes currently in the graph
            final ReadableGraph rg = GraphManager.getDefault().getActiveGraph().getReadableGraph();
            try {
                for (int i = 0; i < rg.getAttributeCount(GraphElementType.TRANSACTION); i++) {
                    attributeNames.add(rg.getAttributeName(rg.getAttribute(GraphElementType.TRANSACTION, i)));
                }
            } finally {
                rg.release();
            }

            final Label nameLabel = new Label("Attribute name:");
            nameText = new TextField();
            final VBox nameBox = new VBox(10);
            nameBox.getChildren().addAll(nameLabel, nameText);

            nameText.textProperty().addListener(ev -> {
                if (!selectionIsActive) {
                    attributeList.getSelectionModel().select(null);
                }
                update();
            });

            final Label listLabel = new Label("Current attributes:");
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

            controls.addRow(0, nameBox);
            controls.addRow(1, listLabel);
            controls.addRow(2, attributeList);
            return controls;
        }

        @Override
        public boolean noValueCheckBoxAvailable() {
            return false;
        }
    }
}
