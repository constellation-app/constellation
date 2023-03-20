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

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

/**
 *
 * @author altair1673
 */
public class DateTimeRangePicker {

    // Time range selection accordion
    private final Accordion timeRangeAccordian = new Accordion();
    private final TitledPane timeRangePane;

    private final DateTimePicker fromDate = new DateTimePicker(true);
    private final DateTimePicker toDate = new DateTimePicker(false);

    private final Button clearButton = new Button("CLEAR");

    private final Button applyButton = new Button("APPLY");

    private final Map<String, ZoneId> timeZoneMap = new HashMap<>();

    private boolean active = false;

    public DateTimeRangePicker() {

        // Set up the date time selection pane with to range selection components
        final Pane dateTimePane = new Pane();
        final GridPane dateTimeGridpane = new GridPane();

        dateTimeGridpane.add(fromDate.getPane(), 0, 0);
        dateTimeGridpane.add(toDate.getPane(), 1, 0);
        dateTimeGridpane.setHgap(15);
        dateTimeGridpane.setVgap(10);

        dateTimePane.getChildren().add(dateTimeGridpane);

        timeRangePane = new TitledPane("Select time range...", dateTimePane);
        timeRangeAccordian.getPanes().add(timeRangePane);

        // Get all available time zone ids
        final ArrayList<String> timeZones = new ArrayList<>();
        final ArrayList<String> plusFromGMT = new ArrayList<>();
        final ArrayList<String> minusFromGMT = new ArrayList<>();
        final DateTimeFormatter offSet = DateTimeFormatter.ofPattern("xxx");

        // Sort date times in there respective arrays
        for (String zone : ZoneId.getAvailableZoneIds()) {
            final ZoneId zoneID = ZoneId.of(zone);
            final String timeString = offSet.format(zoneID.getRules().getOffset(Instant.now())) + " " + zone;

            if (timeString.startsWith("-")) {
                minusFromGMT.add(timeString);
            } else if (timeString.startsWith("+")) {
                plusFromGMT.add(timeString);
            }

            // Populate a map containing the time zone string as key and the actual ID as value
            timeZoneMap.put(timeString, zoneID);

        }

        // Sort time zone strings in ascending and descending order
        Collections.sort(plusFromGMT);
        Collections.sort(minusFromGMT, Collections.reverseOrder());

        timeZones.addAll(minusFromGMT);
        timeZones.addAll(plusFromGMT);

        final ComboBox<String> timeZoneChoiceBox = new ComboBox(FXCollections.observableList(timeZones));

        final String localTimeString = offSet.format(ZoneId.systemDefault().getRules().getOffset(Instant.now())) + " " + ZoneId.systemDefault().getId();

        // Set time of the range selectors to the current time
        fromDate.setCurrentDateTime(ZoneId.systemDefault());
        toDate.setCurrentDateTime(ZoneId.systemDefault());
        timeZoneChoiceBox.getSelectionModel().select(localTimeString);

        // Event handler for changing time zones
        timeZoneChoiceBox.setOnAction(event -> {
            fromDate.convertCurrentDateTime(timeZoneMap.get(timeZoneChoiceBox.getSelectionModel().getSelectedItem()));
            toDate.convertCurrentDateTime(timeZoneMap.get(timeZoneChoiceBox.getSelectionModel().getSelectedItem()));
        });

        dateTimeGridpane.add(timeZoneChoiceBox, 0, 1);

        final Button utcButton = new Button("UTC");
        final Button localButton = new Button("LOCAL");

        final GridPane timeZoneButtons = new GridPane();
        timeZoneButtons.add(utcButton, 0, 0);
        timeZoneButtons.add(localButton, 1, 0);
        timeZoneButtons.add(clearButton, 2, 0);
        timeZoneButtons.add(applyButton, 3, 0);
        timeZoneButtons.setHgap(10);

        dateTimeGridpane.add(timeZoneButtons, 1, 1);

        clearButton.setStyle("-fx-background-color: #7FFFD4; ");
        clearButton.setTextFill(Color.BLACK);
        applyButton.setStyle("-fx-background-color: #0080FF; ");

        // Convert time to local time zone
        localButton.setOnAction(event -> {
            fromDate.convertCurrentDateTime(ZoneId.systemDefault());
            toDate.convertCurrentDateTime(ZoneId.systemDefault());
            timeZoneChoiceBox.getSelectionModel().select(localTimeString);
        });

        // Convert time to UTC
        utcButton.setOnAction(event -> {
            fromDate.convertCurrentDateTime(ZoneId.of("UTC"));
            toDate.convertCurrentDateTime(ZoneId.of("UTC"));
            timeZoneChoiceBox.getSelectionModel().select("+00:00 UTC");
        });

        // Event handler for when user hovers over active and apply button
        clearButton.setOnMouseEntered(event -> clearButton.setStyle("-fx-background-color: #23FFB5; "));

        clearButton.setOnMouseExited(event -> clearButton.setStyle("-fx-background-color: #7FFFD4;  "));

        applyButton.setOnMouseEntered(event -> applyButton.setStyle("-fx-background-color: #078BC9; "));

        applyButton.setOnMouseExited(event -> applyButton.setStyle("-fx-background-color: #0080FF; "));


    }

    public Button getClearButton() {
        return clearButton;
    }

    public Button getApplyButton() {
        return applyButton;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }


    public Accordion getTimeRangeAccordian() {
        return timeRangeAccordian;
    }

    public TitledPane getTimeRangePane() {
        return timeRangePane;
    }

    public boolean checkIsWithinRange(ZonedDateTime entryTime) {
        entryTime = entryTime.withZoneSameInstant(fromDate.getZoneId());

        ZonedDateTime fromTime = fromDate.getCurrentDateTime();
        ZonedDateTime toTime = toDate.getCurrentDateTime();

        return entryTime.isEqual(fromTime) || entryTime.isEqual(toTime) || (entryTime.isAfter(fromTime) && entryTime.isBefore(toTime));
    }

}
