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

import au.gov.asd.tac.constellation.graph.attribute.DoubleAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.interaction.ValueValidator;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.DefaultGetter;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.EditOperation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = AttributeValueEditorFactory.class)
public class DoubleEditorFactory extends AttributeValueEditorFactory<Double> {

    @Override
    public AbstractEditor<Double> createEditor(final EditOperation editOperation, final DefaultGetter<Double> defaultGetter, final ValueValidator<Double> validator, final String editedItemName, final Double initialValue) {
        return new DoubleEditor(editOperation, defaultGetter, validator, editedItemName, initialValue);
    }

    @Override
    public String getAttributeType() {
        return DoubleAttributeDescription.ATTRIBUTE_NAME;
    }

    public class DoubleEditor extends AbstractEditor<Double> {

        private TextField numberField;

        protected DoubleEditor(final EditOperation editOperation, final DefaultGetter<Double> defaultGetter, final ValueValidator<Double> validator, final String editedItemName, final Double initialValue) {
            super(editOperation, defaultGetter, validator, editedItemName, initialValue);
        }

        @Override
        protected boolean canSet(final Double value) {
            // This is an editor for primitive doubles, so prevent null values being set.
            return value != null;
        }

        @Override
        public void updateControlsWithValue(final Double value) {
            numberField.setText(String.valueOf(value));
        }

        @Override
        protected Double getValueFromControls() throws ControlsInvalidException {
            try {
                return Double.parseDouble(numberField.getText());
            } catch (final NumberFormatException ex) {
                throw new ControlsInvalidException("Entered value is not a double.");
            }
        }

        @Override
        protected Node createEditorControls() {
            final VBox controls = new VBox();
            controls.setAlignment(Pos.CENTER);

            numberField = new TextField();
            numberField.textProperty().addListener((o, n, v) -> update());

            controls.getChildren().add(numberField);
            return controls;
        }

        @Override
        public boolean noValueCheckBoxAvailable() {
            return false;
        }
    }
}
