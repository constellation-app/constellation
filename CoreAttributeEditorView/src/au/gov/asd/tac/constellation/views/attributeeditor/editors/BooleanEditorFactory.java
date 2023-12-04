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

import au.gov.asd.tac.constellation.graph.attribute.BooleanAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.interaction.ValueValidator;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.DefaultGetter;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.EditOperation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AttributeValueEditorFactory.class)
public class BooleanEditorFactory extends AttributeValueEditorFactory<Boolean> {

    @Override
    public AbstractEditor<Boolean> createEditor(final EditOperation editOperation, final DefaultGetter<Boolean> defaultGetter, final ValueValidator<Boolean> validator, final String editedItemName, final Boolean initialValue) {
        return new BooleanEditor(editOperation, defaultGetter, validator, editedItemName, initialValue);
    }

    @Override
    public String getAttributeType() {
        return BooleanAttributeDescription.ATTRIBUTE_NAME;
    }

    public class BooleanEditor extends AbstractEditor<Boolean> {

        private CheckBox checkBox;

        protected BooleanEditor(final EditOperation editOperation, final DefaultGetter<Boolean> defaultGetter, final ValueValidator<Boolean> validator, final String editedItemName, final Boolean initialValue) {
            super(editOperation, defaultGetter, validator, editedItemName, initialValue);
        }

        @Override
        protected boolean canSet(final Boolean value) {
            // This is an editor for primitive booleans, so prevent null values being set.
            return value != null;
        }

        @Override
        public void updateControlsWithValue(final Boolean value) {
            // A null boolean is treated as false
            checkBox.setSelected((value != null) && value);
        }

        @Override
        protected Boolean getValueFromControls() {
            return checkBox.isSelected();
        }

        @Override
        protected Node createEditorControls() {
            final VBox controls = new VBox();
            controls.setAlignment(Pos.CENTER);
            controls.setFillWidth(true);

            checkBox = new CheckBox("True");
            checkBox.setAlignment(Pos.CENTER);
            checkBox.selectedProperty().addListener((v, o, n) -> update());

            controls.getChildren().add(checkBox);

            return controls;
        }
    }
}
