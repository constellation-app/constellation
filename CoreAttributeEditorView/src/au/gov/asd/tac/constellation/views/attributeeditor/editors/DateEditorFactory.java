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

import au.gov.asd.tac.constellation.graph.attribute.DateAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.interaction.ValueValidator;
import au.gov.asd.tac.constellation.utilities.temporal.TemporalFormatting;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.EditOperation;
import java.time.LocalDate;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.VBox;
import javafx.util.converter.LocalDateStringConverter;
import org.openide.util.lookup.ServiceProvider;

/**
 * Editor Factory for attributes of type date
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AttributeValueEditorFactory.class)
public class DateEditorFactory extends AttributeValueEditorFactory<LocalDate> {

    @Override
    public AbstractEditor<LocalDate> createEditor(final EditOperation editOperation, final LocalDate defaultValue, final ValueValidator<LocalDate> validator, final String editedItemName, final LocalDate initialValue) {
        return new DateEditor(editOperation, defaultValue, validator, editedItemName, initialValue);
    }

    @Override
    public String getAttributeType() {
        return DateAttributeDescription.ATTRIBUTE_NAME;
    }

    public class DateEditor extends AbstractEditor<LocalDate> {

        private DatePicker datePicker;

        protected DateEditor(final EditOperation editOperation, final LocalDate defaultValue, final ValueValidator<LocalDate> validator, final String editedItemName, final LocalDate initialValue) {
            super(editOperation, defaultValue, validator, editedItemName, initialValue, true);
        }

        @Override
        public void updateControlsWithValue(final LocalDate value) {
            if (value != null) {
                datePicker.setValue(value);
            }
        }

        @Override
        protected LocalDate getValueFromControls() throws ControlsInvalidException {
            return datePicker.getValue();
        }

        @Override
        protected Node createEditorControls() {
            datePicker = new DatePicker();
            datePicker.setConverter(new LocalDateStringConverter(
                    TemporalFormatting.DATE_FORMATTER, TemporalFormatting.DATE_FORMATTER));
            datePicker.getEditor().textProperty().addListener((v, o, n) -> update());
            datePicker.setValue(LocalDate.now());
            datePicker.valueProperty().addListener((v, o, n) -> update());
            
            final VBox controls = new VBox(datePicker);
            controls.setAlignment(Pos.CENTER);
            controls.setFillWidth(true);
            
            return controls;
        }
    }
}
