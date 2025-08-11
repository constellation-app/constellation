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

import au.gov.asd.tac.constellation.graph.attribute.LongAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.interaction.ValueValidator;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.EditOperation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.openide.util.lookup.ServiceProvider;

/**
 * Editor Factory for attributes of type long
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = AttributeValueEditorFactory.class)
public class LongEditorFactory extends AttributeValueEditorFactory<Long> {

    @Override
    public AbstractEditor<Long> createEditor(final String editedItemName, final EditOperation editOperation, final ValueValidator<Long> validator, final Long defaultValue, final Long initialValue) {
        return new LongEditor(editedItemName, editOperation, validator, defaultValue, initialValue);
    }

    @Override
    public String getAttributeType() {
        return LongAttributeDescription.ATTRIBUTE_NAME;
    }

    public class LongEditor extends AbstractEditor<Long> {

        private TextField numberField;

        protected LongEditor(final String editedItemName, final EditOperation editOperation, final ValueValidator<Long> validator, final Long defaultValue, final Long initialValue) {
            super(editedItemName, editOperation, validator, defaultValue, initialValue);
        }

        @Override
        protected boolean canSet(final Long value) {
            // This is an editor for primitive longs, so prevent null values being set.
            return value != null;
        }

        @Override
        public void updateControlsWithValue(final Long value) {
            numberField.setText(String.valueOf(value));
        }

        @Override
        protected Long getValueFromControls() throws AbstractEditorFactory.ControlsInvalidException {
            try {
                return Long.valueOf(numberField.getText());
            } catch (final NumberFormatException ex) {
                throw new ControlsInvalidException("Entered value is not a long.");
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
