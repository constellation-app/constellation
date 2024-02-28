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

import au.gov.asd.tac.constellation.graph.attribute.LongAttributeDescription;
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
public class LongEditorFactory extends AttributeValueEditorFactory<Long> {

    @Override
    public AbstractEditor<Long> createEditor(EditOperation editOperation, DefaultGetter<Long> defaultGetter, ValueValidator<Long> validator, String editedItemName, Long initialValue) {
        return new LongEditor(editOperation, defaultGetter, validator, editedItemName, initialValue);
    }

    @Override
    public String getAttributeType() {
        return LongAttributeDescription.ATTRIBUTE_NAME;
    }

    public class LongEditor extends AbstractEditor<Long> {

        private TextField numberField;

        protected LongEditor(final EditOperation editOperation, final DefaultGetter<Long> defaultGetter, final ValueValidator<Long> validator, final String editedItemName, final Long initialValue) {
            super(editOperation, defaultGetter, validator, editedItemName, initialValue);
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
                return Long.parseLong(numberField.getText());
            } catch (final NumberFormatException ex) {
                throw new ControlsInvalidException("Entered value is not a long.");
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
