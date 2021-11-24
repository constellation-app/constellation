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
package au.gov.asd.tac.constellation.views.find2.components.advanced;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.utilities.temporal.TimeZoneUtilities;
import au.gov.asd.tac.constellation.views.find2.components.AdvancedFindTab;
import au.gov.asd.tac.constellation.views.find2.components.advanced.utilities.DateTimeSelector;
import java.time.LocalDate;
import java.time.ZoneId;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.OverrunStyle;
import javafx.scene.text.TextAlignment;

/**
 * Child criteria BorderPane for the attributes of type date time.
 *
 * @author Atlas139mkm
 */
public class DateTimeCriteriaPanel extends AdvancedCriteriaBorderPane {

    private final DateTimeSelector dateTimeSelector;
    private final DateTimeSelector dateTimeSelectorTwo;
    private final Button datePickerButton = new Button("Select Date/Time");
    private final Label andLabel = new Label("And");
    private final Button datePickerTwoButton = new Button("Select Date/Time");
    private final ChoiceBox<String> timeFrameChoiceBox = new ChoiceBox<>();

    private String dateString = "";
    private String timeString = "";
    private String timeZoneString = "";

    private String dateStringTwo = "";
    private String timeStringTwo = "";
    private String timeZoneStringTwo = "";

    public DateTimeCriteriaPanel(final AdvancedFindTab parentComponent, final String type, final GraphElementType graphElementType) {
        super(parentComponent, type, graphElementType);
        setGridContent();
        dateTimeSelector = new DateTimeSelector(this, dateString, timeString, timeZoneString);
        dateTimeSelectorTwo = new DateTimeSelector(this, dateStringTwo, timeStringTwo, timeZoneStringTwo);

        // On button click display dateTimePicker
        datePickerButton.setOnAction(action -> {
            displayDateTimePicker(dateTimeSelector);
        });

        // On button click display dateTimePickerTwo
        datePickerTwoButton.setOnAction(action -> {
            displayDateTimePicker(dateTimeSelectorTwo);
        });

        getFilterChoiceBox().getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observableValue, final String oldElement, final String newElement) -> {
            betweenSeletionAction(newElement);
        });

        timeFrameChoiceBox.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observableValue, final String oldElement, final String newElement) -> {
            timeFrameSelectionAction(newElement);
        });
    }

    /**
     * Sets the UI content of the pane
     */
    private void setGridContent() {
        // Disable the UI elements needed for choicebox "Between" selection
        andLabel.setDisable(true);
        datePickerTwoButton.setDisable(true);
        timeFrameChoiceBox.setDisable(true);

        // Set the formatting and prefrences for datePickerTwoButton
        datePickerButton.setTextAlignment(TextAlignment.CENTER);
        datePickerButton.setTextOverrun(OverrunStyle.ELLIPSIS);
        datePickerButton.setMinWidth(163);
        datePickerButton.setMaxWidth(163);
        datePickerButton.setAlignment(Pos.CENTER);

        // Set the formatting and prefrences for datePickerTwoButton
        datePickerTwoButton.setTextAlignment(TextAlignment.CENTER);
        datePickerTwoButton.setTextOverrun(OverrunStyle.ELLIPSIS);
        datePickerTwoButton.setMinWidth(180);
        datePickerTwoButton.setMaxWidth(180);
        datePickerTwoButton.setAlignment(Pos.CENTER);

        // Add all choice Box relevant choice box options and select the first
        // for filterChoiceBox
        getFilterChoiceBox().getItems().removeAll("Is", "Is Not");
        getFilterChoiceBox().getItems().addAll("Occoured On", "Didn't Occour On", "Occured Before", "Occoured After", "Occoured Between");
        getFilterChoiceBox().getSelectionModel().selectFirst();

        // Add all choice Box relevant choice box options and select the first
        // for timeFrameChoiceBox
        timeFrameChoiceBox.getItems().addAll("Custom", "Last 3 Days", "Last Week", "Last Month", "Last 3 Months", "Last 6 Months", "Last Year");
        timeFrameChoiceBox.getSelectionModel().selectFirst();
        getHboxTop().getChildren().add(timeFrameChoiceBox);
        getHboxBot().getChildren().addAll(datePickerButton, andLabel, datePickerTwoButton);
    }

    /**
     * Calls the relevant getDateTimeData function based on if dateTimeSelector
     * one or two us passed
     *
     * @param selector
     */
    public void saveButtonAction(final DateTimeSelector selector) {
        if (selector.equals(dateTimeSelector)) {
            getDateTimeData();
        } else {
            getDateTimeDataTwo();
        }
    }

    /**
     * Displays the dateTimeSelector passed
     *
     * @param selector
     */
    private void displayDateTimePicker(final DateTimeSelector selector) {
        selector.showAndWait();
    }

    /**
     * Saves all the data within the dateTimeSelctor, saves it to the relevant
     * variables and sets the button text to the date time.
     */
    private void getDateTimeData() {
        dateString = dateTimeSelector.saveDate();
        timeString = dateTimeSelector.saveTime();
        timeZoneString = dateTimeSelector.saveTimeZone();
        dateTimeSelector.close();

        datePickerButton.setText(dateString + " " + timeString + " " + timeZoneString);
    }

    /**
     * Saves all the data within the dateTimeSelctorTwo, saves it to the
     * relevant variables and sets the button text to the date time.
     */
    private void getDateTimeDataTwo() {
        dateStringTwo = dateTimeSelector.saveDate();
        timeStringTwo = dateTimeSelector.saveTime();
        timeZoneStringTwo = dateTimeSelector.saveTimeZone();
        dateTimeSelectorTwo.close();

        datePickerTwoButton.setText(dateStringTwo + " " + timeStringTwo + " " + timeZoneStringTwo);
    }

    /**
     * Called when the user changes selection in the filterChoiceBox. This
     * enables / disables the relevant UI elements based on the choiceSelection.
     *
     * @param choiceSelection
     */
    private void betweenSeletionAction(final String choiceSelection) {
        datePickerTwoButton.setDisable(!choiceSelection.equals("Occoured Between"));
        timeFrameChoiceBox.setDisable(!choiceSelection.equals("Occoured Between"));

        if (!timeFrameChoiceBox.getSelectionModel().getSelectedItem().equals("Custom")) {
            timeFrameChoiceBox.getSelectionModel().select("Custom");
            datePickerTwoButton.setDisable(true);
        }
    }

    /**
     * Called when the user changes selection in the timeFrameChoiceBox. This
     * enables / disables the relevant UI elements based on the choiceSelection.
     * It also alters the date, time and timezone of the two dateTimes to match
     * the choiceSelection.
     *
     * @param choiceSelection
     */
    private void timeFrameSelectionAction(final String choiceSelection) {
        // enable buttons if custom is selected. disable if otherwise
        datePickerButton.setDisable(!choiceSelection.equals("Custom"));
        datePickerTwoButton.setDisable(!choiceSelection.equals("Custom"));

        // If the choiceSelection is one of the preset time frames
        if (!choiceSelection.equals("Custom")) {
            // Format the timeZoneString to be in a format where it can be 
            // convered to a ZoneId
            final String formattedTimeZoneString = (timeZoneString.equals("") ? "" : timeZoneString.split("\\[")[1].replace("]", ""));

            // set dateTime two data to the current date, max time of today
            // and the previously selected timeZone
            dateStringTwo = LocalDate.now().toString();
            timeStringTwo = "23:59:59";
            timeZoneStringTwo = (timeZoneString.equals("") ? TimeZoneUtilities.UTC.toString() : TimeZoneUtilities.getTimeZoneAsString(ZoneId.of(formattedTimeZoneString)));

            // set dateTime one data to the min time of today
            // and the previously selected timeZone
            timeString = "0:0:0";
            timeZoneString = (timeZoneString.equals("") ? TimeZoneUtilities.UTC.toString() : TimeZoneUtilities.getTimeZoneAsString(ZoneId.of(formattedTimeZoneString)));

            // The Local Date value of the current date based of the timeZone
            final LocalDate calculatedDate = LocalDate.now(dateString.equals("") ? TimeZoneUtilities.UTC : ZoneId.of(formattedTimeZoneString));

            // Switch statment that determines the date string one value based
            // off the choiceSelection
            switch (choiceSelection) {
                case "Last 3 Days":
                    dateString = calculatedDate.minusDays(3).toString();
                    break;
                case "Last Week":
                    dateString = calculatedDate.minusDays(7).toString();
                    break;
                case "Last Month":
                    dateString = calculatedDate.minusMonths(1).toString();
                    break;
                case "Last 3 Months":
                    dateString = calculatedDate.minusMonths(3).toString();
                    break;
                case "Last 6 Months":
                    dateString = calculatedDate.minusMonths(6).toString();
                    break;
                case "Last Year":
                    dateString = calculatedDate.minusYears(1).toString();
                    break;
                default:
                    break;
            }
            // set the button texts to represent the current date, time and
            // timeZones
            datePickerButton.setText(dateString + " " + timeString + " " + timeZoneString);
            datePickerTwoButton.setText(dateStringTwo + " " + timeStringTwo + " " + timeZoneStringTwo);
        }

    }

    @Override
    public String getType() {
        return "datetime";
    }

}
