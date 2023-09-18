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

import au.gov.asd.tac.constellation.graph.attribute.BooleanObjectAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.interaction.ValueValidator;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.DefaultGetter;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.EditOperation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AttributeValueEditorFactory.class)
public class BooleanObjectEditorFactory extends AttributeValueEditorFactory<Boolean> {

    @Override
    public AbstractEditor<Boolean> createEditor(final EditOperation editOperation, final DefaultGetter<Boolean> defaultGetter, final ValueValidator<Boolean> validator, final String editedItemName, final Boolean initialValue) {
        return new BooleanObjectEditor(editOperation, defaultGetter, validator, editedItemName, initialValue);
    }

    @Override
    public String getAttributeType() {
        return BooleanObjectAttributeDescription.ATTRIBUTE_NAME;
    }

    public class BooleanObjectEditor extends AbstractEditor<Boolean> {

        private CheckBox checkBox;

        protected BooleanObjectEditor(final EditOperation editOperation, final DefaultGetter<Boolean> defaultGetter, final ValueValidator<Boolean> validator, final String editedItemName, final Boolean initialValue) {
            super(editOperation, defaultGetter, validator, editedItemName, initialValue);
        }

        @Override
        public void updateControlsWithValue(final Boolean value) {
            checkBox.setDisable(value == null);
            noValueCheckBox.setSelected(false);
            checkBox.setSelected(value != null && value);
        }

        @Override
        protected Boolean getValueFromControls() {
            return noValueCheckBox.isSelected() ? null : checkBox.isSelected();
        }

        @Override
        protected Node createEditorControls() {
            final GridPane controls = new GridPane();
            controls.setAlignment(Pos.CENTER);
            controls.setVgap(CONTROLS_DEFAULT_VERTICAL_SPACING);

            checkBox = new CheckBox("True:");
            checkBox.setAlignment(Pos.CENTER);
            checkBox.selectedProperty().addListener((v, o, n) -> update());

            noValueCheckBox = new CheckBox(NO_VALUE_LABEL);
            noValueCheckBox.setAlignment(Pos.CENTER);
            noValueCheckBox.selectedProperty().addListener((v, o, n) -> {
                checkBox.setDisable(noValueCheckBox.isSelected());
                update();
            });

            controls.addRow(0, checkBox);
            controls.addRow(1, noValueCheckBox);
            return controls;
        }
    }
}
