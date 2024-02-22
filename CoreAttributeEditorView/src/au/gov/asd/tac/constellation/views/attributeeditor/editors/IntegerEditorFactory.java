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

import au.gov.asd.tac.constellation.graph.attribute.IntegerAttributeDescription;
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
 * @author twilight_sparkle
 */
@ServiceProvider(service = AttributeValueEditorFactory.class)
public class IntegerEditorFactory extends AttributeValueEditorFactory<Integer> {

    @Override
    public AbstractEditor<Integer> createEditor(final EditOperation editOperation, final DefaultGetter<Integer> defaultGetter, final ValueValidator<Integer> validator, final String editedItemName, final Integer initialValue) {
        return new IntegerEditor(editOperation, defaultGetter, validator, editedItemName, initialValue);
    }

    @Override
    public String getAttributeType() {
        return IntegerAttributeDescription.ATTRIBUTE_NAME;
    }

    public class IntegerEditor extends AbstractEditor<Integer> {

        private TextField numberField;

        protected IntegerEditor(final EditOperation editOperation, final DefaultGetter<Integer> defaultGetter, final ValueValidator<Integer> validator, final String editedItemName, final Integer initialValue) {
            super(editOperation, defaultGetter, validator, editedItemName, initialValue);
        }

        @Override
        protected boolean canSet(final Integer value) {
            // This is an editor for primitive ints, so prevent null values being set.
            return value != null;
        }

        @Override
        public void updateControlsWithValue(final Integer value) {
            numberField.setText(String.valueOf(value));
        }

        @Override
        protected Integer getValueFromControls() throws ControlsInvalidException {
            try {
                return Integer.parseInt(numberField.getText());
            } catch (final NumberFormatException ex) {
                throw new ControlsInvalidException("Entered value is not an integer.");
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
