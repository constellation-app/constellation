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
package au.gov.asd.tac.constellation.views.attributeeditor.editors;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.attribute.AttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.AttributeRegistry;
import au.gov.asd.tac.constellation.graph.attribute.ObjectAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.interaction.AbstractAttributeInteraction;
import au.gov.asd.tac.constellation.graph.attribute.interaction.AttributeValueTranslator;
import au.gov.asd.tac.constellation.graph.attribute.interaction.ValueValidator;
import au.gov.asd.tac.constellation.views.attributeeditor.AttributeEditorDialog;
import au.gov.asd.tac.constellation.views.attributeeditor.AttributePrototype;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.DefaultGetter;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.EditOperation;
import java.util.ArrayList;
import java.util.Collections;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

/**
 *
 * @author twilight_sparkle
 */
public class AttributeEditorFactory extends AbstractEditorFactory<AttributePrototype> {

    @Override
    public AbstractEditor<AttributePrototype> createEditor(final EditOperation editOperation, final DefaultGetter<AttributePrototype> defaultGetter, final ValueValidator<AttributePrototype> validator, final String editedItemName, final AttributePrototype initialValue) {
        return new AttributeEditor(editOperation, defaultGetter, validator, editedItemName, initialValue);
    }

    public class AttributeEditor extends AbstractEditor<AttributePrototype> {

        private static final int CONTROLPANE_SPACING = 10;

        private GraphElementType elementType;
        private ComboBox<String> typeCombo;
        private TextField nameText;
        private Button setDefaultButton;
        private Button clearDefaultButton;
        TextField descText;
        private Object defaultValue;
        private boolean isTypeModifiable;

        protected AttributeEditor(final EditOperation editOperation, final DefaultGetter<AttributePrototype> defaultGetter, final ValueValidator<AttributePrototype> validator, final String editedItemName, final AttributePrototype initialValue) {
            super(editOperation, defaultGetter, validator, editedItemName, initialValue);
        }

        public void setGraphElementType(final GraphElementType elementType) {
            this.elementType = elementType;
        }

        public void setTypeModifiable(final boolean isTypeModifiable) {
            this.isTypeModifiable = isTypeModifiable;
            if (typeCombo != null) {
                typeCombo.setDisable(!isTypeModifiable);
            }
        }

        @Override
        protected boolean canSet(final AttributePrototype value) {
            return value != null;
        }

        @Override
        public void updateControlsWithValue(final AttributePrototype value) {
            nameText.setText(value.getAttributeName());
            descText.setText(value.getAttributeDescription());
            typeCombo.getSelectionModel().select(value.getDataType());
            defaultValue = value.getDefaultValue();
            clearDefaultButton.setDisable(defaultValue == null);
        }

        @Override
        protected AttributePrototype getValueFromControls() throws ControlsInvalidException {
            if (nameText.getText().isEmpty()) {
                throw new ControlsInvalidException("Attribute name can't be empty.");
            }
            return new AttributePrototype(nameText.getText(), descText.getText(), elementType, typeCombo.getSelectionModel().getSelectedItem(), defaultValue);
        }

        @Override
        protected Node createEditorControls() {
            final GridPane controls = new GridPane();
            controls.setHgap(5);
            controls.setVgap(CONTROLPANE_SPACING);

            final Label nameLabel = new Label("Attribute Name:");
            final Label typeLabel = new Label("Attribute Type:");
            final Label descLabel = new Label("Attribute Description:");
            final Label defaultLabel = new Label("Default Value:");

            nameText = new TextField();
            nameText.textProperty().addListener((o, n, v) -> update());
            typeCombo = new ComboBox<>();
            typeCombo.setDisable(!isTypeModifiable);
            typeCombo.getSelectionModel().selectedItemProperty().addListener((o, n, v) -> update());
            descText = new TextField();
            descText.textProperty().addListener((o, n, v) -> update());
            setDefaultButton = new Button("Set Default");
            setDefaultButton.setOnAction(getSelectDefaultHandler());
            clearDefaultButton = new Button("Clear Default");
            clearDefaultButton.setOnAction(e -> {
                defaultValue = null;
                clearDefaultButton.setDisable(true);
                update();
            });

            // Populate the type combo with all of the possible graph types.
            final ArrayList<String> attributeTypes = new ArrayList<>();
            AttributeRegistry.getDefault().getAttributes().entrySet().stream().forEach(entry -> {
                final Class<? extends AttributeDescription> attrTypeDescr = entry.getValue();
                final boolean isObject = ObjectAttributeDescription.class.isAssignableFrom(attrTypeDescr);
                if (!isObject) {
                    final String attrTypeName = entry.getKey();
                    attributeTypes.add(attrTypeName);
                }
            });
            Collections.sort(attributeTypes);
            typeCombo.setItems(FXCollections.observableList(attributeTypes));

            controls.addRow(0, nameLabel, nameText);
            controls.addRow(1, typeLabel, typeCombo);
            controls.addRow(2, descLabel, descText);
            controls.addRow(3, defaultLabel, new HBox(5, setDefaultButton, clearDefaultButton));
            return controls;
        }

        @SuppressWarnings("unchecked")
        private <T> EventHandler<ActionEvent> getSelectDefaultHandler() {
            return e -> {
                final AbstractAttributeInteraction<T> interaction = (AbstractAttributeInteraction<T>) AbstractAttributeInteraction.getInteraction(currentValue.getDataType());
                final AttributeValueEditorFactory<T> editorFactory = (AttributeValueEditorFactory<T>) AttributeValueEditorFactory.getEditFactory(currentValue.getDataType());
                final String editType = editorFactory.getAttributeType();
                final AttributeValueTranslator fromTranslator = interaction.fromEditTranslator(editType);
                final AttributeValueTranslator toTranslator = interaction.toEditTranslator(editType);
                final ValueValidator<T> validator = interaction.fromEditValidator(editType);

                final EditOperation restoreDefaultEditOperation = value -> {
                    defaultValue = fromTranslator.translate(value);
                    clearDefaultButton.setDisable(defaultValue == null);
                    update();
                };

                final AbstractEditor<T> editor = editorFactory.createEditor(restoreDefaultEditOperation, validator, "the default", (T) toTranslator.translate(defaultValue));
                final AttributeEditorDialog dialog = new AttributeEditorDialog(false, editor);
                dialog.showDialog();
            };
        }
    }
}
