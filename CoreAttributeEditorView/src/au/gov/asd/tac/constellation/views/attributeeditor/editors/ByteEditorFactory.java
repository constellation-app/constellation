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

import au.gov.asd.tac.constellation.graph.attribute.ByteAttributeDescription;
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
public class ByteEditorFactory extends AttributeValueEditorFactory<Byte> {

    @Override
    public AbstractEditor<Byte> createEditor(final EditOperation editOperation, final DefaultGetter<Byte> defaultGetter, final ValueValidator<Byte> validator, final String editedItemName, final Byte initialValue) {
        return new ByteEditor(editOperation, defaultGetter, validator, editedItemName, initialValue);
    }

    @Override
    public String getAttributeType() {
        return ByteAttributeDescription.ATTRIBUTE_NAME;
    }

    public class ByteEditor extends AbstractEditor<Byte> {

        private TextField numberField;

        protected ByteEditor(final EditOperation editOperation, final DefaultGetter<Byte> defaultGetter, final ValueValidator<Byte> validator, final String editedItemName, final Byte initialValue) {
            super(editOperation, defaultGetter, validator, editedItemName, initialValue);
        }

        @Override
        protected boolean canSet(final Byte value) {
            // This is an editor for primitive bytes, so prevent null values being set.
            return value != null;
        }

        @Override
        public void updateControlsWithValue(final Byte value) {
            numberField.setText(String.valueOf(value));
        }

        @Override
        protected Byte getValueFromControls() throws ControlsInvalidException {
            try {
                return Byte.parseByte(numberField.getText());
            } catch (final NumberFormatException ex) {
                throw new ControlsInvalidException("Entered value is not a byte.");
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
        public Boolean noValueCheckBoxAvailable() {
            return false;
        }
    }
}
