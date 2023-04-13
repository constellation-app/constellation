/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.notes;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.Chronology;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;

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
    private static final String FROM_TEXT = "From:";
    private static final String TO_TEXT = "To:";

    boolean active = false;
    boolean from = false;

    private ZoneId zone;

    public DateTimePicker(final boolean from) {
        dateTimePane = new Pane();
        datePicker.setChronology(Chronology.ofLocale(Locale.CHINA));

        datePicker.setConverter(new StringConverter<LocalDate>() {
            String pattern = "yyyy-MM-dd";
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(pattern);

            @Override
            public String toString(LocalDate object) {
                if (object != null) {
                    return dateFormatter.format(object);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }

        });

        this.from = from;
        dateTimePane.getChildren().add(mainGridPane);

        final GridPane datePickerGridPane = new GridPane();

        final Label datePickerLabel = new Label();

        if (from) {
            datePickerLabel.setText(FROM_TEXT);
        } else {
            datePickerLabel.setText(TO_TEXT);
        }


        datePickerGridPane.add(datePickerLabel, 0, 0);
        datePickerGridPane.add(datePicker, 1, 0);

        final GridPane timePickerGrid = new GridPane();
        final Label hourLabel = new Label("Hour");
        final Label minLabel = new Label("Minute");
        final Label secLabel = new Label("Second");

        timePickerGrid.add(hourLabel, 0, 0);
        timePickerGrid.add(minLabel, 1, 0);
        timePickerGrid.add(secLabel, 2, 0);

        hourPicker.setMinWidth(60);
        minPicker.setMinWidth(60);
        secPicker.setMinWidth(60);

        hourPicker.setMaxWidth(60);
        minPicker.setMaxWidth(60);
        secPicker.setMaxWidth(60);

        hourPicker.setEditable(true);
        minPicker.setEditable(true);
        secPicker.setEditable(true);

        timePickerGrid.add(hourPicker, 0, 1);
        timePickerGrid.add(minPicker, 1, 1);
        timePickerGrid.add(secPicker, 2, 1);

        mainGridPane.add(datePickerGridPane, 0, 0);
        mainGridPane.add(timePickerGrid, 0, 1);
    }


    public Pane getPane() {
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
        if (convertTo == null || zone == convertTo) {
            return;
        }

        ZonedDateTime currentTime = ZonedDateTime.of(datePicker.getValue().getYear(),
                datePicker.getValue().getMonthValue(),
                datePicker.getValue().getDayOfMonth(),
                hourPicker.getValue(),
                minPicker.getValue(),
                secPicker.getValue(),
                0,
                zone);

        currentTime = currentTime.withZoneSameInstant(convertTo);
        zone = convertTo;

        datePicker.valueProperty().set(currentTime.toLocalDate());
        hourPicker.getValueFactory().setValue(currentTime.getHour());
        minPicker.getValueFactory().setValue(currentTime.getMinute());
        secPicker.getValueFactory().setValue(currentTime.getSecond());

    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    /**
     * Gets the current date time from the controls
     *
     * @return
     */
    public ZonedDateTime getCurrentDateTime() {
        return ZonedDateTime.of(datePicker.getValue().getYear(),
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
