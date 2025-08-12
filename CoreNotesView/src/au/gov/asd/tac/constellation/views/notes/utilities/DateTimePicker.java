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
package au.gov.asd.tac.constellation.views.notes.utilities;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.Chronology;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Pattern;
import javafx.geometry.Insets;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

/**
 * A DateTime Selector that is in JavaFX instead of Swing.
 *
 * @author altair1673
 */
public class DateTimePicker {

    private final Pane dateTimePane;
    private final DatePicker datePicker = new DatePicker();
    private final Spinner<Integer> hourPicker = new Spinner<>(0, 23, 0);
    private final Spinner<Integer> minPicker = new Spinner<>(0, 59, 0);
    private final Spinner<Integer> secPicker = new Spinner<>(0, 59, 0);
    private final GridPane mainGridPane = new GridPane();
    private ZoneId zone;

    private static final String FROM_TEXT = "From:";
    private static final String TO_TEXT = "To:";
    private static final String PICKER_LABEL = "picker-label";
    private static final Pattern NUMBERS_ONLY_REGEX = Pattern.compile("\\d*");

    public DateTimePicker(final boolean from) {
        dateTimePane = new Pane();
        datePicker.setChronology(Chronology.ofLocale(Locale.ENGLISH));

        datePicker.setConverter(new StringConverter<LocalDate>() {
            static final String PATTERN = "yyyy-MM-dd";
            final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(PATTERN);

            @Override
            public String toString(final LocalDate object) {
                return object != null ? dateFormatter.format(object) : "";
            }

            @Override
            public LocalDate fromString(final String string) {
                return StringUtils.isNotBlank(string) ? LocalDate.parse(string, dateFormatter) : null;
            }
        });

        datePicker.setStyle("-fx-text-fill: black;");
        dateTimePane.getChildren().add(mainGridPane);

        final GridPane datePickerGridPane = new GridPane();
        final Label datePickerLabel = new Label();

        if (from) {
            datePickerLabel.setText(FROM_TEXT);
            datePickerLabel.setId(PICKER_LABEL);
        } else {
            datePickerLabel.setText(TO_TEXT);
            datePickerLabel.setId(PICKER_LABEL);
        }

        datePickerGridPane.add(datePickerLabel, 0, 0);
        datePickerGridPane.add(datePicker, 1, 0);
        datePicker.setMaxWidth(150);
        datePicker.setEditable(false);

        final Label hourLabel = new Label("Hour");
        final Label minLabel = new Label("Minute");
        final Label secLabel = new Label("Second");

        hourLabel.setId(PICKER_LABEL);
        minLabel.setId(PICKER_LABEL);
        secLabel.setId(PICKER_LABEL);

        final GridPane timePickerGridPane = new GridPane();
        timePickerGridPane.add(hourLabel, 0, 0);
        timePickerGridPane.add(minLabel, 1, 0);
        timePickerGridPane.add(secLabel, 2, 0);

        final TextField hourField = hourPicker.getEditor();
        final TextField minField = minPicker.getEditor();
        final TextField secField = secPicker.getEditor();

        hourField.textProperty().addListener((observable, oldValue, newValue) -> hourField.setText(validateInput(oldValue, newValue)));
        minField.textProperty().addListener((observable, oldValue, newValue) -> minField.setText(validateInput(oldValue, newValue)));
        secField.textProperty().addListener((observable, oldValue, newValue) -> secField.setText(validateInput(oldValue, newValue)));

        hourField.focusedProperty().addListener((observable, oldValue, newValue) -> hourField.setText(validateBlankInput(hourField)));
        minField.focusedProperty().addListener((observable, oldValue, newValue) -> minField.setText(validateBlankInput(minField)));
        secField.focusedProperty().addListener((observable, oldValue, newValue) -> secField.setText(validateBlankInput(secField)));

        hourPicker.setMinWidth(60);
        minPicker.setMinWidth(60);
        secPicker.setMinWidth(60);

        hourPicker.setMaxWidth(60);
        minPicker.setMaxWidth(60);
        secPicker.setMaxWidth(60);

        hourPicker.setEditable(true);
        minPicker.setEditable(true);
        secPicker.setEditable(true);

        timePickerGridPane.add(hourPicker, 0, 1);
        timePickerGridPane.add(minPicker, 1, 1);
        timePickerGridPane.add(secPicker, 2, 1);

        mainGridPane.add(datePickerGridPane, 0, 0);
        mainGridPane.add(timePickerGridPane, 0, 1);
        mainGridPane.setPadding(new Insets(1, 1, 1, 1));
    }

    public String validateInput(final String oldValue, final String newValue) {
        return (!NUMBERS_ONLY_REGEX.matcher(newValue).matches() || newValue.length() > 2) ? oldValue : newValue;
    }

    public String validateBlankInput(final TextField textField) {
        return (!textField.isFocused() && textField.getText().isBlank()) ? "0" : textField.getText();
    }

    public void disableControls(final boolean disable) {
        datePicker.setDisable(disable);
        hourPicker.setDisable(disable);
        minPicker.setDisable(disable);
        secPicker.setDisable(disable);
    }

    protected Pane getPane() {
        return dateTimePane;
    }

    /**
     * Sets the current date time to whatever the current local time is
     *
     * @param zone - zone ID of current location
     */
    public void setCurrentDateTime(final ZoneId zone) {
        this.zone = zone;

        final ZonedDateTime timeAtZone = ZonedDateTime.now(zone);

        datePicker.valueProperty().set(LocalDate.now(zone));
        hourPicker.getValueFactory().setValue(timeAtZone.getHour());
        minPicker.getValueFactory().setValue(timeAtZone.getMinute());
        secPicker.getValueFactory().setValue(timeAtZone.getSecond());
    }

    /**
     * Converts time to another time zone
     *
     * @param convertTo - id of zone to convert to
     */
    public void convertCurrentDateTime(final ZoneId convertTo) {
        if (convertTo != null || zone != convertTo) {
            final ZonedDateTime currentTime = ZonedDateTime.of(
                    datePicker.getValue().getYear(),
                    datePicker.getValue().getMonthValue(),
                    datePicker.getValue().getDayOfMonth(),
                    hourPicker.getValue(),
                    minPicker.getValue(),
                    secPicker.getValue(),
                    0,
                    zone).withZoneSameInstant(convertTo);

            zone = convertTo;

            datePicker.valueProperty().set(currentTime.toLocalDate());
            hourPicker.getValueFactory().setValue(currentTime.getHour());
            minPicker.getValueFactory().setValue(currentTime.getMinute());
            secPicker.getValueFactory().setValue(currentTime.getSecond());
        }
    }

    /**
     * Gets the current date time from the controls
     *
     * @return
     */
    public ZonedDateTime getCurrentDateTime() {
        return ZonedDateTime.of(
                datePicker.getValue().getYear(),
                datePicker.getValue().getMonthValue(),
                datePicker.getValue().getDayOfMonth(),
                hourPicker.getValue(),
                minPicker.getValue(),
                secPicker.getValue(),
                0,
                zone);
    }

    public ZoneId getZoneId() {
        return zone;
    }
}
