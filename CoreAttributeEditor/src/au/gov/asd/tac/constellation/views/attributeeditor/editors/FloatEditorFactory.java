/*
 * Copyright 2010-2019 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
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
public class FloatEditorFactory extends AttributeValueEditorFactory<Float> {

    @Override
    public AbstractEditor<Float> createEditor(final EditOperation editOperation, final DefaultGetter<Float> defaultGetter, final ValueValidator<Float> validator, final String editedItemName, final Float initialValue) {
        return new FloatEditor(editOperation, defaultGetter, validator, editedItemName, initialValue);
    }

    @Override
    public String getAttributeType() {
        return FloatAttributeDescription.ATTRIBUTE_NAME;
    }

    public class FloatEditor extends AbstractEditor<Float> {

        private TextField numberField;

        protected FloatEditor(final EditOperation editOperation, final DefaultGetter<Float> defaultGetter, final ValueValidator<Float> validator, final String editedItemName, final Float initialValue) {
            super(editOperation, defaultGetter, validator, editedItemName, initialValue);
        }

        @Override
        protected boolean canSet(final Float value) {
            // This is an editor for primitive floats, so prevent null values being set.
            return value != null;
        }

        @Override
        public void updateControlsWithValue(final Float value) {
            numberField.setText(String.valueOf(value));
        }

        @Override
        protected Float getValueFromControls() throws ControlsInvalidException {
            try {
                return Float.parseFloat(numberField.getText());
            } catch (final NumberFormatException ex) {
                throw new ControlsInvalidException("Entered value is not a float.");
            }
        }

        @Override
        protected Node createEditorControls() {
            VBox controls = new VBox();
            controls.setAlignment(Pos.CENTER);

            numberField = new TextField();
            numberField.textProperty().addListener((o, n, v) -> {
                update();
            });

            controls.getChildren().add(numberField);
            return controls;
        }

    }
}
