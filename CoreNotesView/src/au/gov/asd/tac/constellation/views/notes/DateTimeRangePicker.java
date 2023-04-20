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
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

/**
 * A pane with 2 DateTimePicker objects which represent a time range This class
 * provides a way to define a time range and a function to check if a certain
 * time falls within the range. A CLEAR and APPLY button can be accessed
 * publicly and appropriate event handlers can be set to them to handle if a
 * time falls within the range. Different timezones can be set
 *
 * @author altair1673
 */
public class DateTimeRangePicker {

    // Time range selection accordion
    private final Accordion timeRangeAccordian = new Accordion();
    private final TitledPane timeRangePane;
    private final Label titleText;

    private static final String TITLE = "Select a time range...";

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
        dateTimeGridpane.setHgap(10);
        dateTimeGridpane.setVgap(5);

        dateTimePane.getChildren().add(dateTimeGridpane);

        timeRangePane = new TitledPane("", dateTimePane);
        timeRangeAccordian.getPanes().add(timeRangePane);
        timeRangeAccordian.setMaxWidth(373);
        timeRangeAccordian.setMinWidth(373);
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
        timeZoneChoiceBox.setMaxWidth(180);

        final Button utcButton = new Button("UTC");
        final Button localButton = new Button("LOCAL");
        utcButton.setMinWidth(90);
        localButton.setMinWidth(90);

        final GridPane timeZoneButtons = new GridPane();

        final HBox timeZoneHBox = new HBox(1, utcButton, localButton);

        GridPane timeZoneGridPane = new GridPane();
        timeZoneGridPane.add(timeZoneChoiceBox, 0, 0);
        timeZoneGridPane.add(timeZoneHBox, 0, 1);

        timeZoneButtons.add(timeZoneGridPane, 0, 0);
        timeZoneButtons.setHgap(20);


        dateTimeGridpane.add(timeZoneButtons, 0, 1);
        dateTimeGridpane.add(applyButton, 1, 1);
        dateTimeGridpane.setHalignment(applyButton, HPos.CENTER);
        applyButton.setMinWidth(100);

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

        // Set up title bar
        titleText = new Label(TITLE);
        titleText.setTextFill(Color.WHITE);
        titleText.setStyle("-fx-text-fill:WHITE;");
        titleText.setMinWidth(115);
        final GridPane topBarGridPane = new GridPane();
        topBarGridPane.add(titleText, 0, 0);
        topBarGridPane.add(clearButton, 1, 0);
        clearButton.setMinHeight(17);
        clearButton.setMaxHeight(17);
        clearButton.setTextAlignment(TextAlignment.JUSTIFY);
        clearButton.setPadding(new Insets(0, 8, 0, 8));
        clearButton.setVisible(false);
        topBarGridPane.setHgap(160);
        timeRangePane.setGraphic(topBarGridPane);

    }

    public Button getClearButton() {
        return clearButton;
    }

    public Button getApplyButton() {
        return applyButton;
    }

    /**
     * Whether or not the time range selector is active, can be used to
     * determine a on/off state
     *
     * @return
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Set whether or not this is active, changes title based on active status
     * Also changes visibility of clear button
     *
     * @param active
     */
    public void setActive(final boolean active) {
        if (active) {
            titleText.setText("Filter applied");
            titleText.setStyle("-fx-text-fill:YELLOW;");
            clearButton.setVisible(true);
        } else {
            titleText.setText(TITLE);
            titleText.setStyle("-fx-text-fill:WHITE;");
            clearButton.setVisible(false);
        }

        this.active = active;
    }

    /**
     * Get collapsible of this control
     *
     * @return
     */
    public Accordion getTimeRangeAccordian() {
        return timeRangeAccordian;
    }

    /**
     * Get the actual pane that hold the DateTimePicker objects
     *
     * @return
     */
    public TitledPane getTimeRangePane() {
        return timeRangePane;
    }

    /**
     * Check if a passed in time is within the time range
     *
     * @param entryTime - Time that is being checked
     * @return whether entryTime is within range
     */
    public boolean checkIsWithinRange(ZonedDateTime entryTime) {
        // Convert entry time to time zone of DateTimePickers
        entryTime = entryTime.withZoneSameInstant(fromDate.getZoneId());

        final ZonedDateTime fromTime = fromDate.getCurrentDateTime();
        final ZonedDateTime toTime = toDate.getCurrentDateTime();

        return entryTime.isEqual(fromTime) || entryTime.isEqual(toTime) || (entryTime.isAfter(fromTime) && entryTime.isBefore(toTime));
    }

}
