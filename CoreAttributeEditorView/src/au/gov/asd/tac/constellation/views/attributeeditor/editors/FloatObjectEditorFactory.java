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

import au.gov.asd.tac.constellation.graph.attribute.FloatObjectAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.interaction.ValueValidator;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.DefaultGetter;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.EditOperation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AttributeValueEditorFactory.class)
public class FloatObjectEditorFactory extends AttributeValueEditorFactory<Float> {

    @Override
    public AbstractEditor<Float> createEditor(final EditOperation editOperation, final DefaultGetter<Float> defaultGetter, final ValueValidator<Float> validator, final String editedItemName, final Float initialValue) {
        return new FloatObjectEditor(editOperation, defaultGetter, validator, editedItemName, initialValue);
    }

    @Override
    public String getAttributeType() {
        return FloatObjectAttributeDescription.ATTRIBUTE_NAME;
    }

    public class FloatObjectEditor extends AbstractEditor<Float> {

        private TextField numberField;

        protected FloatObjectEditor(final EditOperation editOperation, final DefaultGetter<Float> defaultGetter, final ValueValidator<Float> validator, final String editedItemName, final Float initialValue) {
            super(editOperation, defaultGetter, validator, editedItemName, initialValue);
        }

        @Override
        public void updateControlsWithValue(final Float value) {
            noValueCheckBox.setSelected(value == null);
            if (value != null) {
                numberField.setText(String.valueOf(value));
            }
        }

        @Override
        protected Float getValueFromControls() throws ControlsInvalidException {
            if (noValueCheckBox.isSelected()) {
                return null;
            }
            try {
                return Float.parseFloat(numberField.getText());
            } catch (final NumberFormatException ex) {
                throw new ControlsInvalidException("Entered value is not a float.");
            }
        }

        @Override
        protected Node createEditorControls() {
            final GridPane controls = new GridPane();
            controls.setAlignment(Pos.CENTER);
            controls.setVgap(CONTROLS_DEFAULT_VERTICAL_SPACING);

            numberField = new TextField();
            numberField.textProperty().addListener((o, n, v) -> update());

            noValueCheckBox = new CheckBox(NO_VALUE_LABEL);
            noValueCheckBox.setAlignment(Pos.CENTER);
            noValueCheckBox.selectedProperty().addListener((v, o, n) -> {
                numberField.setDisable(noValueCheckBox.isSelected());
                update();
            });

            controls.addRow(0, numberField);
            controls.addRow(1, noValueCheckBox);
            return controls;
        }
    }
}
