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

import au.gov.asd.tac.constellation.graph.attribute.LocalDateTimeAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.interaction.ValueValidator;
import au.gov.asd.tac.constellation.utilities.temporal.TemporalFormatting;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.DefaultGetter;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.EditOperation;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.converter.LocalDateStringConverter;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AttributeValueEditorFactory.class)
public class LocalDateTimeEditorFactory extends AttributeValueEditorFactory<LocalDateTime> {

    @Override
    public AbstractEditor<LocalDateTime> createEditor(final EditOperation editOperation, final DefaultGetter<LocalDateTime> defaultGetter, final ValueValidator<LocalDateTime> validator, final String editedItemName, final LocalDateTime initialValue) {
        return new LocalDateTimeEditor(editOperation, defaultGetter, validator, editedItemName, initialValue);
    }

    @Override
    public String getAttributeType() {
        return LocalDateTimeAttributeDescription.ATTRIBUTE_NAME;
    }

    public class LocalDateTimeEditor extends AbstractEditor<LocalDateTime> {

        private static final int NUMBER_SPINNER_WIDTH = 55;
        private static final int MILLIS_SPINNER_WIDTH = 60;
        private static final int NANOSECONDS_IN_MILLISECOND = 1000000;

        private static final String LABEL_ID = "label";

        private DatePicker datePicker;
        private Spinner<Integer> hourSpinner;
        private Spinner<Integer> minSpinner;
        private Spinner<Integer> secSpinner;
        private Spinner<Integer> milliSpinner;

        protected LocalDateTimeEditor(final EditOperation editOperation, final DefaultGetter<LocalDateTime> defaultGetter, final ValueValidator<LocalDateTime> validator, final String editedItemName, final LocalDateTime initialValue) {
            super(editOperation, defaultGetter, validator, editedItemName, initialValue);
        }

        @Override
        public void updateControlsWithValue(final LocalDateTime value) {
            if (value != null) {
                datePicker.setValue(value.toLocalDate());
                hourSpinner.getValueFactory().setValue(value.get(ChronoField.HOUR_OF_DAY));
                minSpinner.getValueFactory().setValue(value.get(ChronoField.MINUTE_OF_HOUR));
                secSpinner.getValueFactory().setValue(value.get(ChronoField.SECOND_OF_MINUTE));
                milliSpinner.getValueFactory().setValue(value.get(ChronoField.MILLI_OF_SECOND));
            }
        }

        @Override
        protected LocalDateTime getValueFromControls() throws ControlsInvalidException {
            if (hourSpinner.getValue() == null || minSpinner.getValue() == null || secSpinner.getValue() == null || milliSpinner.getValue() == null) {
                throw new ControlsInvalidException("Time spinners must have numeric values");
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

            return LocalDateTime.of(datePicker.getValue(), LocalTime.of(
                    hourSpinner.getValue(),
                    minSpinner.getValue(),
                    secSpinner.getValue(),
                    milliSpinner.getValue() * NANOSECONDS_IN_MILLISECOND));
        }

        @Override
        protected Node createEditorControls() {
            final GridPane controls = new GridPane();
            controls.setAlignment(Pos.CENTER);
            controls.setVgap(CONTROLS_DEFAULT_VERTICAL_SPACING);

            final HBox timeSpinnerContainer = createTimeSpinners();
            controls.addRow(0, timeSpinnerContainer);

            return controls;
        }

        private HBox createTimeSpinners() {
            datePicker = new DatePicker();
            datePicker.setConverter(new LocalDateStringConverter(
                    TemporalFormatting.DATE_FORMATTER, TemporalFormatting.DATE_FORMATTER));
            datePicker.getEditor().textProperty().addListener((v, o, n) -> update());
            datePicker.setValue(LocalDate.now());
            datePicker.valueProperty().addListener((v, o, n) -> update());

            hourSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23));
            minSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59));
            secSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59));
            milliSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 999));
            hourSpinner.getValueFactory().setValue(LocalTime.now(ZoneOffset.UTC).getHour());
            minSpinner.getValueFactory().setValue(LocalTime.now(ZoneOffset.UTC).getMinute());
            secSpinner.getValueFactory().setValue(LocalTime.now(ZoneOffset.UTC).getSecond());
            milliSpinner.getValueFactory().setValue(0);

            final HBox timeSpinnerContainer = new HBox(CONTROLS_DEFAULT_VERTICAL_SPACING);

            final Label dateLabel = new Label("Date:");
            dateLabel.setId(LABEL_ID);
            dateLabel.setLabelFor(datePicker);

            final Label hourSpinnerLabel = new Label("Hour:");
            hourSpinnerLabel.setId(LABEL_ID);
            hourSpinnerLabel.setLabelFor(hourSpinner);

            final Label minSpinnerLabel = new Label("Minute:");
            minSpinnerLabel.setId(LABEL_ID);
            minSpinnerLabel.setLabelFor(minSpinner);

            final Label secSpinnerLabel = new Label("Second:");
            secSpinnerLabel.setId(LABEL_ID);
            secSpinnerLabel.setLabelFor(secSpinner);

            final Label milliSpinnerLabel = new Label("Millis:");
            milliSpinnerLabel.setId(LABEL_ID);
            milliSpinnerLabel.setLabelFor(milliSpinner);

            hourSpinner.setPrefWidth(NUMBER_SPINNER_WIDTH);
            minSpinner.setPrefWidth(NUMBER_SPINNER_WIDTH);
            secSpinner.setPrefWidth(NUMBER_SPINNER_WIDTH);
            milliSpinner.setPrefWidth(MILLIS_SPINNER_WIDTH);

            hourSpinner.setEditable(true);
            minSpinner.setEditable(true);
            secSpinner.setEditable(true);
            milliSpinner.setEditable(true);

            hourSpinner.valueProperty().addListener((o, n, v) -> update());
            minSpinner.valueProperty().addListener((o, n, v) -> update());
            secSpinner.valueProperty().addListener((o, n, v) -> update());
            milliSpinner.valueProperty().addListener((o, n, v) -> update());

            final VBox dateLabelNode = new VBox(5);
            dateLabelNode.getChildren().addAll(dateLabel, datePicker);
            final VBox hourLabelNode = new VBox(5);
            hourLabelNode.getChildren().addAll(hourSpinnerLabel, hourSpinner);
            final VBox minLabelNode = new VBox(5);
            minLabelNode.getChildren().addAll(minSpinnerLabel, minSpinner);
            final VBox secLabelNode = new VBox(5);
            secLabelNode.getChildren().addAll(secSpinnerLabel, secSpinner);
            final VBox milliLabelNode = new VBox(5);
            milliLabelNode.getChildren().addAll(milliSpinnerLabel, milliSpinner);

            timeSpinnerContainer.getChildren().addAll(dateLabelNode, hourLabelNode, minLabelNode, secLabelNode, milliLabelNode);

            return timeSpinnerContainer;
        }

        @Override
        public Boolean noValueCheckBoxAvailable() {
            return true;
        }
    }
}
