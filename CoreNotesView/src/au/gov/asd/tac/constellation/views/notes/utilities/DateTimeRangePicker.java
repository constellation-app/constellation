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
package au.gov.asd.tac.constellation.views.notes.utilities;

import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

/**
 * A pane with 2 DateTimePicker objects which represent a time range This class provides a way to define a time range and a function to check if a certain time falls within the range. A CLEAR and APPLY button can be accessed publicly and appropriate event handlers can be set to them to handle if a time falls within the range. Different timezones can be set
 *
 * @author altair1673
 */
public class DateTimeRangePicker {

    // Time range selection accordion
    private final CustomMenuItem timeRangeCustomMenuItem;
    private final Label titleText;

    private final MenuButton timeFilterMenu = new MenuButton();

    private static final String TITLE = "Select a time range...";

    private final DateTimePicker fromDate = new DateTimePicker(true);
    private final DateTimePicker toDate = new DateTimePicker(false);

    private final Button clearButton = new Button("CLEAR");

    private final Button applyButton = new Button("APPLY");

    private final Button utcButton = new Button("UTC");
    private final Button localButton = new Button("LOCAL");

    private final Map<String, ZoneId> timeZoneMap = new HashMap<>();

    private boolean active = false;
    private final ComboBox<String> timeZoneChoiceBox;
    private final GridPane topBarGridPane = new GridPane();

    public DateTimeRangePicker() {

        // Set up the date time selection pane with to range selection components
        final VBox dateTimePane = new VBox(2, fromDate.getPane(), toDate.getPane());
        dateTimePane.setAlignment(Pos.CENTER);
        dateTimePane.setMaxHeight(95);
        timeRangeCustomMenuItem = new CustomMenuItem();
        timeFilterMenu.getStyleClass().add("column-filter");

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

        timeZoneChoiceBox = new ComboBox(FXCollections.observableList(timeZones));

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
        timeZoneChoiceBox.setMaxWidth(187);

        dateTimePane.getChildren().add(timeZoneChoiceBox);

        utcButton.setMinWidth(93);
        localButton.setMinWidth(93);

        final HBox timeZoneHBox = new HBox(1, utcButton, localButton);
        dateTimePane.getChildren().add(timeZoneHBox);

        clearButton.setStyle("-fx-background-color: #7FFFD4; -fx-text-fill: #111111; -fx-font-size: 11px; -fx-padding: 0px 9px;");

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

        // Event handler for when user hovers over clear
        clearButton.setOnMouseEntered(event -> {
            clearButton.setStyle("-fx-background-color: #23FFB5; -fx-text-fill: #111111; -fx-font-size: 11px; -fx-padding: 0px 9px;");
            clearButton.setCursor(Cursor.HAND);
        });
        clearButton.setOnMouseExited(event -> {
            clearButton.setStyle("-fx-background-color: #7FFFD4; -fx-text-fill: #111111; -fx-font-size: 11px; -fx-padding: 0px 9px;");
            clearButton.setCursor(Cursor.DEFAULT);
        });

        dateTimePane.getChildren().add(applyButton);

        // Set up title bar
        titleText = new Label(TITLE);
        titleText.setMinWidth(115);
        titleText.setId("filter-label");
        topBarGridPane.add(titleText, 0, 0);
        clearButton.setMinHeight(17);
        clearButton.setMaxHeight(17);
        clearButton.setTextAlignment(TextAlignment.JUSTIFY);

        applyButton.setTextAlignment(TextAlignment.JUSTIFY);

        topBarGridPane.setHgap(10);

        timeRangeCustomMenuItem.setContent(dateTimePane);
        timeRangeCustomMenuItem.setHideOnClick(false);

        timeFilterMenu.setGraphic(topBarGridPane);

        timeFilterMenu.getItems().add(timeRangeCustomMenuItem);
    }

    public Button getClearButton() {
        return clearButton;
    }

    public Button getApplyButton() {
        return applyButton;
    }

    public void showClearButton() {
        if (!topBarGridPane.getChildren().contains(clearButton)) {
            topBarGridPane.add(clearButton, 1, 0);
        }
    }

    public void disableAll(final boolean disable) {
        applyButton.setDisable(disable);
        timeZoneChoiceBox.setDisable(disable);
        utcButton.setDisable(disable);
        localButton.setDisable(disable);
        toDate.disableControls(disable);
        fromDate.disableControls(disable);
    }

    /**
     * Whether or not the time range selector is active, can be used to determine a on/off state
     *
     * @return
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Set whether or not this is active, changes title based on active status Also changes visibility of clear button
     *
     * @param active
     */
    public void setActive(final boolean active) {
        if (active) {
            titleText.setText("Filter applied");
            clearButton.setVisible(true);
        } else {
            titleText.setText(TITLE);
            clearButton.setVisible(false);
        }

        this.active = active;
    }

    /**
     * Get the actual pane that hold the DateTimePicker objects
     *
     * @return
     */
    public CustomMenuItem getTimeRangeCustomMenuItem() {
        return timeRangeCustomMenuItem;
    }

    public MenuButton getTimeFilterMenu() {
        return timeFilterMenu;
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
