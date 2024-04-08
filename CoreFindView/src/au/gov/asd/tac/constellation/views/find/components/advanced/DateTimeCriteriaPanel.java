/*
 * Copyright 2010-2024 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.find.components.advanced;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.attribute.ZonedDateTimeAttributeDescription;
import au.gov.asd.tac.constellation.utilities.temporal.TimeZoneUtilities;
import au.gov.asd.tac.constellation.views.find.components.AdvancedFindTab;
import au.gov.asd.tac.constellation.views.find.components.advanced.criteriavalues.DateTimeCriteriaValues;
import au.gov.asd.tac.constellation.views.find.components.advanced.criteriavalues.FindCriteriaValues;
import au.gov.asd.tac.constellation.views.find.components.advanced.utilities.DateTimeSelector;
import java.time.LocalDate;
import java.time.ZoneId;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Tooltip;
import javafx.scene.text.TextAlignment;
import org.apache.commons.lang3.StringUtils;

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

    String dateTimeStringPrimary = "";
    String dateTimeStringSecondary = "";

    private static final String IS = "Is";
    private static final String IS_NOT = "Is Not";

    private static final String OCCURED_ON = "Occured On";
    private static final String DIDNT_OCCUR_ON = "Didn't Occur On";
    private static final String OCCURED_BEFORE = "Occured Before";
    private static final String OCCURED_AFTER = "Occured After";
    private static final String OCCURED_BETWEEN = "Occured Between";

    private static final String CUSTOM = "Custom";
    private static final String LAST_3_DAYS = "Last 3 Days";
    private static final String LAST_WEEK = "Last Week";
    private static final String LAST_MONTH = "Last Month";
    private static final String LAST_3_MONTHS = "Last 3 Months";
    private static final String LAST_6_MONTHS = "Last 6 Months";
    private static final String LAST_YEAR = "Last Year";

    public DateTimeCriteriaPanel(final AdvancedFindTab parentComponent, final String type, final GraphElementType graphElementType) {
        super(parentComponent, type, graphElementType);
        setGridContent();
        dateTimeSelector = new DateTimeSelector(this, dateString, timeString, timeZoneString);
        dateTimeSelectorTwo = new DateTimeSelector(this, dateStringTwo, timeStringTwo, timeZoneStringTwo);

        datePickerButton.setTooltip(new Tooltip(datePickerButton.getText()));
        datePickerTwoButton.setTooltip(new Tooltip(datePickerTwoButton.getText()));

        // On button click display dateTimePicker
        datePickerButton.setOnAction(action
                -> displayDateTimePicker(dateTimeSelector)
        );

        // On button click display dateTimePickerTwo
        datePickerTwoButton.setOnAction(action
                -> displayDateTimePicker(dateTimeSelectorTwo)
        );

        getFilterChoiceBox().getSelectionModel().selectedItemProperty().addListener((final ObservableValue<? extends String> observableValue, final String oldElement, final String newElement)
                -> betweenSeletionAction(newElement)
        );

        timeFrameChoiceBox.getSelectionModel().selectedItemProperty().addListener((final ObservableValue<? extends String> observableValue, final String oldElement, final String newElement)
                -> timeFrameSelectionAction(newElement)
        );
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
        getFilterChoiceBox().getItems().removeAll(IS, IS_NOT);
        getFilterChoiceBox().getItems().addAll(OCCURED_ON, DIDNT_OCCUR_ON, OCCURED_BEFORE, OCCURED_AFTER, OCCURED_BETWEEN);
        getFilterChoiceBox().getSelectionModel().selectFirst();

        // Add all choice Box relevant choice box options and select the first
        // for timeFrameChoiceBox
        timeFrameChoiceBox.getItems().addAll(CUSTOM, LAST_3_DAYS, LAST_WEEK, LAST_MONTH, LAST_3_MONTHS, LAST_6_MONTHS, LAST_YEAR);
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
        dateTimeStringPrimary = dateString + " " + timeString + " " + timeZoneString;

        datePickerButton.setTooltip(new Tooltip(datePickerButton.getText()));
        datePickerTwoButton.setTooltip(new Tooltip(datePickerTwoButton.getText()));
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
        dateTimeStringSecondary = dateStringTwo + " " + timeStringTwo + " " + timeZoneStringTwo;

        datePickerButton.setTooltip(new Tooltip(datePickerButton.getText()));
        datePickerTwoButton.setTooltip(new Tooltip(datePickerTwoButton.getText()));
    }

    /**
     * Called when the user changes selection in the filterChoiceBox. This
     * enables / disables the relevant UI elements based on the choiceSelection.
     *
     * @param choiceSelection
     */
    private void betweenSeletionAction(final String choiceSelection) {
        datePickerTwoButton.setDisable(!choiceSelection.equals(OCCURED_BETWEEN));
        timeFrameChoiceBox.setDisable(!choiceSelection.equals(OCCURED_BETWEEN));

        if (!timeFrameChoiceBox.getSelectionModel().getSelectedItem().equals(CUSTOM)) {
            timeFrameChoiceBox.getSelectionModel().select(CUSTOM);
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
        datePickerButton.setDisable(!choiceSelection.equals(CUSTOM));
        datePickerTwoButton.setDisable(!choiceSelection.equals(CUSTOM));

        // If the choiceSelection is one of the preset time frames
        if (!choiceSelection.equals(CUSTOM)) {
            // Format the timeZoneString to be in a format where it can be 
            // convered to a ZoneId
            final String formattedTimeZoneString = (StringUtils.isEmpty(timeZoneString) ? "" : timeZoneString.split("\\[")[1].replace("]", ""));

            // set dateTime two data to the current date, max time of today
            // and the previously selected timeZone
            dateStringTwo = LocalDate.now().toString();
            timeStringTwo = "23:59:59.999";
            timeZoneStringTwo = (StringUtils.isEmpty(timeZoneString) ? TimeZoneUtilities.getTimeZoneAsString(TimeZoneUtilities.UTC) : TimeZoneUtilities.getTimeZoneAsString(ZoneId.of(formattedTimeZoneString)));

            // set dateTime one data to the min time of today
            // and the previously selected timeZone
            timeString = "00:00:00.000";
            timeZoneString = (StringUtils.isEmpty(timeZoneString) ? TimeZoneUtilities.getTimeZoneAsString(TimeZoneUtilities.UTC) : TimeZoneUtilities.getTimeZoneAsString(ZoneId.of(formattedTimeZoneString)));

            // The Local Date value of the current date based of the timeZone
            final LocalDate calculatedDate = LocalDate.now(dateString.equals("") ? TimeZoneUtilities.UTC : ZoneId.of(formattedTimeZoneString));

            // Switch statment that determines the date string one value based
            // off the choiceSelection
            switch (choiceSelection) {
                case LAST_3_DAYS -> dateString = calculatedDate.minusDays(3).toString();
                case LAST_WEEK -> dateString = calculatedDate.minusDays(7).toString();
                case LAST_MONTH -> dateString = calculatedDate.minusMonths(1).toString();
                case LAST_3_MONTHS -> dateString = calculatedDate.minusMonths(3).toString();
                case LAST_6_MONTHS -> dateString = calculatedDate.minusMonths(6).toString();
                case LAST_YEAR -> dateString = calculatedDate.minusYears(1).toString();
            }
            // set the button texts to represent the current date, time and
            // timeZones
            datePickerButton.setText(dateString + " " + timeString + " " + timeZoneString);
            datePickerTwoButton.setText(dateStringTwo + " " + timeStringTwo + " " + timeZoneStringTwo);

            // save the dateTime Values as strings
            dateTimeStringPrimary = dateString + " " + timeString + " " + timeZoneString;
            dateTimeStringSecondary = dateStringTwo + " " + timeStringTwo + " " + timeZoneStringTwo;

        }
    }

    /**
     * This returns a FindCriteriaValue, specifically a DateTimeCriteriaValues
     * containing this panes selections and the date time values input
     *
     * @return
     */
    @Override
    public FindCriteriaValues getCriteriaValues() {
        if (getFilterChoiceBox().getSelectionModel().getSelectedItem().equals(OCCURED_BETWEEN)) {
            return new DateTimeCriteriaValues(getType(), getAttributeName(), getFilterChoiceBox().getSelectionModel().getSelectedItem(), dateTimeStringPrimary, dateTimeStringSecondary);
        }
        return new DateTimeCriteriaValues(getType(), getAttributeName(), getFilterChoiceBox().getSelectionModel().getSelectedItem(), dateTimeStringPrimary);
    }

    /**
     * Overrides the parents getType function to return the correct type name
     * being "dateTime"
     *
     * @return
     */
    @Override
    public String getType() {
        return ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME;
    }

}
