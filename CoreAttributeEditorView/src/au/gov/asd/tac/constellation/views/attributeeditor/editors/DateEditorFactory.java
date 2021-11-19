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

import au.gov.asd.tac.constellation.graph.attribute.DateAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.interaction.ValueValidator;
import au.gov.asd.tac.constellation.utilities.temporal.TemporalFormatting;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.DefaultGetter;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.EditOperation;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.GridPane;
import javafx.util.converter.LocalDateStringConverter;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AttributeValueEditorFactory.class)
public class DateEditorFactory extends AttributeValueEditorFactory<LocalDate> {

    @Override
    public AbstractEditor<LocalDate> createEditor(final EditOperation editOperation, final DefaultGetter<LocalDate> defaultGetter, final ValueValidator<LocalDate> validator, final String editedItemName, LocalDate initialValue) {
        return new DateEditor(editOperation, defaultGetter, validator, editedItemName, initialValue);
    }

    @Override
    public String getAttributeType() {
        return DateAttributeDescription.ATTRIBUTE_NAME;
    }

    public class DateEditor extends AbstractEditor<LocalDate> {

        private DatePicker datePicker;
        private CheckBox noValueCheckBox;

        protected DateEditor(final EditOperation editOperation, final DefaultGetter<LocalDate> defaultGetter, final ValueValidator<LocalDate> validator, final String editedItemName, final LocalDate initialValue) {
            super(editOperation, defaultGetter, validator, editedItemName, initialValue);
        }

        @Override
        public void updateControlsWithValue(final LocalDate value) {
            noValueCheckBox.setSelected(value == null);
            if (value != null) {
                datePicker.setValue(value);
            }
        }

        @Override
        protected LocalDate getValueFromControls() throws ControlsInvalidException {
            if (noValueCheckBox.isSelected()) {
                return null;
            }
            final String dateString = datePicker.getEditor().getText();
            //The converter is being used here to try and determine if the entered date is a LocalDate
            //It will throw an exception and won't convert it if its invalid
            try {
                if (!StringUtils.isBlank(dateString)) {
                    datePicker.setValue(datePicker.getConverter().fromString(dateString));
                }
            } catch (final DateTimeParseException ex) {
                throw new ControlsInvalidException("Entered value is not a date of format yyyy-mm-dd.");
            }
            return datePicker.getValue();
        }

        @Override
        protected Node createEditorControls() {
            final GridPane controls = new GridPane();
            controls.setAlignment(Pos.CENTER);
            controls.setVgap(CONTROLS_DEFAULT_VERTICAL_SPACING);

            noValueCheckBox = new CheckBox(NO_VALUE_LABEL);
            noValueCheckBox.setAlignment(Pos.CENTER);
            noValueCheckBox.selectedProperty().addListener((v, o, n) -> {
                datePicker.setDisable(noValueCheckBox.isSelected());
                update();
            });

            datePicker = new DatePicker();
            datePicker.setConverter(new LocalDateStringConverter(
                    TemporalFormatting.DATE_FORMATTER, TemporalFormatting.DATE_FORMATTER));
            datePicker.getEditor().textProperty().addListener((v, o, n) -> update());
            datePicker.setValue(LocalDate.now());
            datePicker.valueProperty().addListener((v, o, n) -> update());

            controls.addRow(0, datePicker);
            controls.addRow(1, noValueCheckBox);
            return controls;
        }
    }
}
