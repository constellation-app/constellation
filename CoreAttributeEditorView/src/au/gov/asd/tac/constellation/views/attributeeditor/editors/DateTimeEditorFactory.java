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

import au.gov.asd.tac.constellation.graph.attribute.ZonedDateTimeAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.interaction.ValueValidator;
import au.gov.asd.tac.constellation.utilities.temporal.TemporalFormatting;
import au.gov.asd.tac.constellation.utilities.temporal.TemporalUtilities;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.DefaultGetter;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.EditOperation;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Comparator;
import java.util.TimeZone;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.converter.LocalDateStringConverter;
import org.openide.util.lookup.ServiceProvider;

/**
 * Editor Factory for attributes of type datetime
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AttributeValueEditorFactory.class)
public class DateTimeEditorFactory extends AttributeValueEditorFactory<ZonedDateTime> {

    @Override
    public AbstractEditor<ZonedDateTime> createEditor(final String editedItemName, final EditOperation editOperation, final ValueValidator<ZonedDateTime> validator, final ZonedDateTime defaultValue, final ZonedDateTime initialValue) {
        return new DateTimeEditor(editedItemName, editOperation, validator, defaultValue, initialValue);
    }

    @Override
    public String getAttributeType() {
        return ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME;
    }

    public class DateTimeEditor extends AbstractEditor<ZonedDateTime> {

        private static final int NUMBER_SPINNER_WIDTH = 55;
        private static final int MILLIS_SPINNER_WIDTH = 60;
        private static final int NANOSECONDS_IN_MILLISECOND = 1000000;
        private static final int MILLISECONDS_IN_SECOND = 1000;

        private static final String LABEL_ID = "label";

        private DatePicker datePicker;
        private Spinner<Integer> hourSpinner;
        private Spinner<Integer> minSpinner;
        private Spinner<Integer> secSpinner;
        private Spinner<Integer> milliSpinner;
        private ComboBox<ZoneId> timeZoneComboBox;
        private final ChangeListener<ZoneId> updateTimeFromZone = (v, o, n) -> {
            if (n != null) {
                setCurrentValue(ZonedDateTime.ofInstant(currentValue.toInstant(), n));
            }
        };

        private final Comparator<ZoneId> zoneIdComparator = (t1, t2) -> {
            final int offsetCompare = Integer.compare(currentValue == null ? TimeZone.getTimeZone(t1).getRawOffset() : TimeZone.getTimeZone(t1).getOffset(currentValue.toEpochSecond() * MILLISECONDS_IN_SECOND), currentValue == null ? TimeZone.getTimeZone(t2).getRawOffset() : TimeZone.getTimeZone(t2).getOffset(currentValue.toEpochSecond() * MILLISECONDS_IN_SECOND));
            return offsetCompare != 0 ? offsetCompare : t1.getId().compareTo(t2.getId());
        };

        protected DateTimeEditor(final String editedItemName, final EditOperation editOperation, final ValueValidator<ZonedDateTime> validator, final ZonedDateTime defaultValue, final ZonedDateTime initialValue) {
            super(editedItemName, editOperation, validator, defaultValue, initialValue, true);
        }
        
        protected LocalDate getDateValue() {
            return datePicker.getValue();
        }
        
        protected Integer getHourValue() {
            return hourSpinner.getValue();
        }
        
        protected Integer getMinValue() {
            return minSpinner.getValue();
        }
        
        protected Integer getSecValue() {
            return secSpinner.getValue();
        }
        
        protected Integer getMilliValue() {
            return milliSpinner.getValue();
        }
        
        protected ZoneId getTZValue() {
            return timeZoneComboBox.getValue();
        }

        @Override
        public void updateControlsWithValue(final ZonedDateTime value) {
            if (value != null) {
                datePicker.setValue(value.toLocalDate());
                hourSpinner.getValueFactory().setValue(value.toLocalDateTime().get(ChronoField.HOUR_OF_DAY));
                minSpinner.getValueFactory().setValue(value.toLocalDateTime().get(ChronoField.MINUTE_OF_HOUR));
                secSpinner.getValueFactory().setValue(value.toLocalDateTime().get(ChronoField.SECOND_OF_MINUTE));
                milliSpinner.getValueFactory().setValue(value.toLocalDateTime().get(ChronoField.MILLI_OF_SECOND));
                
                timeZoneComboBox.getSelectionModel().selectedItemProperty().removeListener(updateTimeFromZone);
                timeZoneComboBox.setValue(value.getZone());
                timeZoneComboBox.getSelectionModel().selectedItemProperty().addListener(updateTimeFromZone);
            }
        }

        @Override
        protected ZonedDateTime getValueFromControls() throws ControlsInvalidException {
            if (hourSpinner.getValue() == null || minSpinner.getValue() == null || secSpinner.getValue() == null || milliSpinner.getValue() == null) {
                throw new ControlsInvalidException("Time spinners must have numeric values");
            }

            return ZonedDateTime.of(datePicker.getValue(), LocalTime.of(
                    hourSpinner.getValue(),
                    minSpinner.getValue(),
                    secSpinner.getValue(),
                    milliSpinner.getValue() * NANOSECONDS_IN_MILLISECOND),
                    timeZoneComboBox.getValue());
        }

        @Override
        protected Node createEditorControls() {
            final ObservableList<ZoneId> timeZones = FXCollections.observableArrayList();
            ZoneId.getAvailableZoneIds().forEach(id -> timeZones.add(ZoneId.of(id)));
            timeZoneComboBox = new ComboBox<>(timeZones.sorted(zoneIdComparator));
            timeZoneComboBox.getSelectionModel().select(TimeZoneUtilities.UTC);
            timeZoneComboBox.getSelectionModel().selectedItemProperty().addListener(updateTimeFromZone);
            
            final Label timeZoneComboBoxLabel = new Label("Time Zone:");
            timeZoneComboBoxLabel.setId(LABEL_ID);
            timeZoneComboBoxLabel.setLabelFor(timeZoneComboBox);
            
            final Callback<ListView<ZoneId>, ListCell<ZoneId>> cellFactory = p -> new ListCell<>() {
                @Override
                protected void updateItem(final ZoneId item, final boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        setText(TemporalUtilities.getTimeZoneAsString(currentValue == null ? null : currentValue.toLocalDateTime(), item));
                    }
                }
            };
            timeZoneComboBox.setCellFactory(cellFactory);
            timeZoneComboBox.setButtonCell(cellFactory.call(null));
            timeZoneComboBox.getSelectionModel().select(TemporalUtilities.UTC);
            timeZoneComboBox.getSelectionModel().selectedItemProperty().addListener(updateTimeFromZone);

            final HBox timeZoneHbox = new HBox(timeZoneComboBoxLabel, timeZoneComboBox);
            timeZoneHbox.setSpacing(5);

            final HBox timeSpinnerContainer = createTimeSpinners();
            
            final VBox controls = new VBox(CONTROLS_DEFAULT_VERTICAL_SPACING, 
                    timeSpinnerContainer, timeZoneHbox);
            controls.setAlignment(Pos.CENTER);
            controls.setFillWidth(true);
            
            return controls;
        }

        private HBox createTimeSpinners() {
            datePicker = new DatePicker();
            datePicker.setConverter(new LocalDateStringConverter(
                    TemporalFormatting.DATE_FORMATTER, TemporalFormatting.DATE_FORMATTER));
            datePicker.setValue(LocalDate.now());
            datePicker.valueProperty().addListener((v, o, n) -> update());
            final Label dateLabel = createLabel("Date:", datePicker);
            
            hourSpinner = createTimeSpinner(23, LocalTime.now(ZoneOffset.UTC).getHour(), NUMBER_SPINNER_WIDTH);
            final Label hourSpinnerLabel = createLabel("Hour:", hourSpinner);
            
            minSpinner = createTimeSpinner(59, LocalTime.now(ZoneOffset.UTC).getMinute(), NUMBER_SPINNER_WIDTH);
            final Label minSpinnerLabel = createLabel("Minute:", minSpinner);
            
            secSpinner = createTimeSpinner(59, LocalTime.now(ZoneOffset.UTC).getSecond(), NUMBER_SPINNER_WIDTH);
            final Label secSpinnerLabel = createLabel("Second:", secSpinner);
            
            milliSpinner = createTimeSpinner(999, 0, MILLIS_SPINNER_WIDTH);
            final Label milliSpinnerLabel = createLabel("Millis:", milliSpinner);
                        
            final VBox dateLabelNode = new VBox(5, dateLabel, datePicker);
            final VBox hourLabelNode = new VBox(5, hourSpinnerLabel, hourSpinner);
            final VBox minLabelNode = new VBox(5, minSpinnerLabel, minSpinner);
            final VBox secLabelNode = new VBox(5, secSpinnerLabel, secSpinner);
            final VBox milliLabelNode = new VBox(5, milliSpinnerLabel, milliSpinner);

            return new HBox(CONTROLS_DEFAULT_HORIZONTAL_SPACING, 
                    dateLabelNode, hourLabelNode, minLabelNode, secLabelNode, milliLabelNode);
        }
        
        /**
         * Creates a spinner for a measurement of time, for the editor
         * 
         * @param maxValue The maximum value on the spinner
         * @param initialValue The initial value on the spinner
         * @param spinnerWidth The preferred width of the spinner
         * @return The newly created spinner object
         */
        private Spinner<Integer> createTimeSpinner(final int maxValue, final int initialValue, final int spinnerWidth) {
            final Spinner<Integer> timeSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, maxValue));
            timeSpinner.getValueFactory().setValue(initialValue);
            timeSpinner.setPrefWidth(spinnerWidth);
            timeSpinner.setEditable(true);
            timeSpinner.valueProperty().addListener((o, n, v) -> update());
            
            return timeSpinner;
        }
        
        /**
         * Creates a label associated with the given time spinner
         * 
         * @param labelText The label text
         * @param associatedObject The object to set the label for
         * @return The newly created label
         */
        private Label createLabel(final String labelText, final Control associatedObject) {
            final Label spinnerLabel = new Label(labelText);
            spinnerLabel.setId(LABEL_ID);
            spinnerLabel.setLabelFor(associatedObject);
            
            return spinnerLabel;
        }
    }
}
