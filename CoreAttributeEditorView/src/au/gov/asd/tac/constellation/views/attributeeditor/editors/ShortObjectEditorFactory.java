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

import au.gov.asd.tac.constellation.graph.attribute.ShortObjectAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.interaction.ValueValidator;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.EditOperation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.openide.util.lookup.ServiceProvider;

/**
 * Editor Factory for attributes of type short_or_null
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = AttributeValueEditorFactory.class)
public class ShortObjectEditorFactory extends AttributeValueEditorFactory<Short> {

    @Override
    public AbstractEditor<Short> createEditor(final String editedItemName, final EditOperation editOperation, final ValueValidator<Short> validator, final Short defaultValue, final Short initialValue) {
        return new ShortObjectEditor(editedItemName, editOperation, validator, defaultValue, initialValue);
    }

    @Override
    public String getAttributeType() {
        return ShortObjectAttributeDescription.ATTRIBUTE_NAME;
    }

    public class ShortObjectEditor extends AbstractEditor<Short> {

        private TextField numberField;

        protected ShortObjectEditor(final String editedItemName, final EditOperation editOperation, final ValueValidator<Short> validator, final Short defaultValue, final Short initialValue) {
            super(editedItemName, editOperation, validator, defaultValue, initialValue, true);
        }
        
        protected String getNumberText() {
            return numberField.getText();
        }

        @Override
        public void updateControlsWithValue(final Short value) {
            if (value != null) {
                numberField.setText(String.valueOf(value));
            }
        }

        @Override
        protected Short getValueFromControls() throws ControlsInvalidException {
            try {
                return Short.valueOf(numberField.getText());
            } catch (final NumberFormatException ex) {
                throw new ControlsInvalidException("Entered value is not a short.");
            }
        }

        @Override
        protected Node createEditorControls() {
            numberField = new TextField();
            numberField.textProperty().addListener((o, n, v) -> update());
            
            final VBox controls = new VBox(numberField);
            controls.setAlignment(Pos.CENTER);
            
            return controls;
        }
    }
}
