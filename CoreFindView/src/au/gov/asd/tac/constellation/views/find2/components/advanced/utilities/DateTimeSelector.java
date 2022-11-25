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
package au.gov.asd.tac.constellation.views.find2.components.advanced.utilities;

import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import au.gov.asd.tac.constellation.utilities.temporal.TimeZoneUtilities;
import au.gov.asd.tac.constellation.views.find2.components.advanced.DateTimeCriteriaPanel;
import java.time.LocalDate;
import javafx.scene.Scene;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;

/**
 * This class contains a window for selecting date / time / timezone data. This
 * is mostly a copy of the dateTimeSelctor used within the attribute editor
 * however this is not embedded within the attribute editor factory
 *
 * @author Atlas139mkm
 */
public class DateTimeSelector extends Stage {

    private final BorderPane bp = new BorderPane();
    private final HBox spinnerHbox = new HBox();
    private final VBox vbox = new VBox();

    private DatePicker datePicker = new DatePicker();
    private Spinner<Integer> hourSpinner = new Spinner<>();
    private Spinner<Integer> minSpinner = new Spinner<>();
    private Spinner<Integer> secSpinner = new Spinner<>();
    private Spinner<Integer> milliSpinner = new Spinner<>();

    private ComboBox<String> timeZoneComboBox;

    private final BorderPane buttonsBp = new BorderPane();
    private final HBox buttonsHbox = new HBox();
    private final Button saveButton = new Button("Save");
    private final Button cancelButton = new Button("Cancel");

    private static final int NUMBER_SPINNER_WIDTH = 55;
    private static final int MILLIS_SPINNER_WIDTH = 60;

    private final DateTimeCriteriaPanel parentComponent;

    public DateTimeSelector(final DateTimeCriteriaPanel parentComponent, final String date, final String time, final String timeZone) {
        setTitle("Date Time Picker");
        this.parentComponent = parentComponent;
        setContent(date, time, timeZone);
        setAlwaysOnTop(true);

        saveButton.setOnAction(action
                -> parentComponent.saveButtonAction(this)
        );

        cancelButton.setOnAction(action
                -> close()
        );

    }

    /**
     * Sets the UI content of the pane. If empty string values ("") are passed
     * in for date, time and timezone the default values will be used.
     *
     * @param date
     * @param time
     * @param timeZone
     */
    private void setContent(final String date, final String time, final String timeZone) {

        // Create 4 spinners for, hours, min, sec and milisec
        hourSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23));
        minSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59));
        secSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59));
        milliSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 999));

        // If a string time is passed parse it and format it to be displayed in
        // the window in the spinners
        if (!"".equals(time)) {
            final String[] splitTime = time.split(":");
            final String[] splitMiliSeconds = splitTime[2].split("\\.");
            hourSpinner.getValueFactory().setValue(Integer.parseInt(splitTime[0]));
            minSpinner.getValueFactory().setValue(Integer.parseInt(splitTime[1]));
            secSpinner.getValueFactory().setValue(Integer.parseInt(splitMiliSeconds[0]));
            milliSpinner.getValueFactory().setValue(splitMiliSeconds.length > 1 ? Integer.parseInt(splitMiliSeconds[1]) : 0);
        } else {
            hourSpinner.getValueFactory().setValue(LocalTime.now(ZoneOffset.UTC).getHour());
            minSpinner.getValueFactory().setValue(LocalTime.now(ZoneOffset.UTC).getMinute());
            secSpinner.getValueFactory().setValue(LocalTime.now(ZoneOffset.UTC).getSecond());
            milliSpinner.getValueFactory().setValue(0);
        }

        // set the label for the date picker
        final Label datePickerLabel = new Label("Date:");
        datePickerLabel.setLabelFor(datePicker);

        // Set the date picker value to the date passed in or the default date
        datePicker.setValue(!"".equals(date) ? LocalDate.parse(date) : LocalDate.now(TimeZoneUtilities.UTC));

        // set the labels for each of the spinners
        final Label hourSpinnerLabel = new Label("hr:");
        hourSpinnerLabel.setLabelFor(hourSpinner);

        final Label minSpinnerLabel = new Label("min:");
        minSpinnerLabel.setLabelFor(minSpinner);

        final Label secSpinnerLabel = new Label("sec:");
        secSpinnerLabel.setLabelFor(secSpinner);

        final Label milliSpinnerLabel = new Label("ms:");
        milliSpinnerLabel.setLabelFor(milliSpinner);

        hourSpinner.setPrefWidth(NUMBER_SPINNER_WIDTH);
        minSpinner.setPrefWidth(NUMBER_SPINNER_WIDTH);
        secSpinner.setPrefWidth(NUMBER_SPINNER_WIDTH);
        milliSpinner.setPrefWidth(MILLIS_SPINNER_WIDTH);

        hourSpinner.setEditable(true);
        minSpinner.setEditable(true);
        secSpinner.setEditable(true);
        milliSpinner.setEditable(true);

        // create individual VBoxs for each spinner and the datePicker
        // for format purposes
        final VBox dateLabelNode = new VBox(5);
        dateLabelNode.getChildren().addAll(datePickerLabel, datePicker);
        final VBox hourLabelNode = new VBox(5);
        hourLabelNode.getChildren().addAll(hourSpinnerLabel, hourSpinner);
        final VBox minLabelNode = new VBox(5);
        minLabelNode.getChildren().addAll(minSpinnerLabel, minSpinner);
        final VBox secLabelNode = new VBox(5);
        secLabelNode.getChildren().addAll(secSpinnerLabel, secSpinner);
        final VBox milliLabelNode = new VBox(5);
        milliLabelNode.getChildren().addAll(milliSpinnerLabel, milliSpinner);

        spinnerHbox.getChildren().addAll(dateLabelNode, hourLabelNode, minLabelNode, secLabelNode, milliLabelNode);
        spinnerHbox.setSpacing(10);

        vbox.setPadding(new Insets(10));
        vbox.getChildren().add(spinnerHbox);

        // Get a list of all the zoneId and add them to timeZones
        final ObservableList<ZoneId> timeZones = FXCollections.observableArrayList();
        ZoneId.getAvailableZoneIds().forEach(id
                -> timeZones.add(ZoneId.of(id))
        );

        // for each of the timeZones zoneIds format them and add them to
        // the timeZoneComboBox
        timeZoneComboBox = new ComboBox<>();
        final List<ZoneId> timeZoneList = timeZones.sorted(zoneIdComparator);
        for (ZoneId id : timeZoneList) {
            timeZoneComboBox.getItems().add(TimeZoneUtilities.getTimeZoneAsString(id));
        }
        // if the timeZone is not the default format it so it can be read
        // as a zoneID
        String selectedTimeZone = "";
        if (!"".equals(timeZone)) {
            selectedTimeZone = timeZone.split("\\[")[1].replace("]", "");
        }

        // select the default timeZone or the one passed in
        timeZoneComboBox.getSelectionModel().select(!"".equals(selectedTimeZone) ? selectedTimeZone : TimeZoneUtilities.getTimeZoneAsString(TimeZoneUtilities.UTC));

        final Label timeZoneLabel = new Label("Time Zone:");
        timeZoneLabel.setLabelFor(timeZoneComboBox);

        final VBox timeZoneNode = new VBox(5);
        timeZoneNode.getChildren().addAll(timeZoneLabel, timeZoneComboBox);

        vbox.getChildren().add(timeZoneNode);

        buttonsHbox.getChildren().addAll(saveButton, cancelButton);
        buttonsHbox.setSpacing(5);
        buttonsHbox.setPadding(new Insets(0, 10, 10, 0));
        buttonsBp.setRight(buttonsHbox);

        bp.setBottom(buttonsBp);
        bp.setCenter(vbox);

        final Scene scene = new Scene(bp);
        scene.getStylesheets().addAll(JavafxStyleManager.getMainStyleSheet());

        setScene(scene);
    }

    /**
     * Saves the currently selected date Value as a string
     *
     * @return
     */
    public String saveDate() {
        return datePicker.getValue().toString();
    }

    /**
     * Saves the currently select time values as a string formatted as
     * hh:mm:ss:mm
     *
     * @return
     */
    public String saveTime() {
        final StringBuilder sb = new StringBuilder();
        sb.append(hourSpinner.getValue() < 10 ? "0" + hourSpinner.getValue().toString() : hourSpinner.getValue().toString());
        sb.append(":" + (minSpinner.getValue() < 10 ? "0" + minSpinner.getValue().toString() : minSpinner.getValue().toString()));
        sb.append(":" + (secSpinner.getValue() < 10 ? "0" + secSpinner.getValue().toString() : secSpinner.getValue().toString()));
        if (milliSpinner.getValue() > 0) {
            if (milliSpinner.getValue() > 10 && milliSpinner.getValue() < 100) {
                sb.append(".0" + milliSpinner.getValue().toString());
            } else {
                sb.append("." + (milliSpinner.getValue() < 10 ? "00" + milliSpinner.getValue().toString() : milliSpinner.getValue().toString()));
            }
        }
        return sb.toString();
    }

    /**
     * Saves the currently selected timeZone
     *
     * @return
     */
    public String saveTimeZone() {
        return timeZoneComboBox.getSelectionModel().getSelectedItem();
    }

    /**
     * A comparator to format the zoneIds in a specific order
     */
    private final Comparator<ZoneId> zoneIdComparator = (t1, t2) -> {
        final int offsetCompare = Integer.compare(TimeZone.getTimeZone(t1).getRawOffset(), TimeZone.getTimeZone(t2).getRawOffset());
        return offsetCompare != 0 ? offsetCompare : t1.getId().compareTo(t2.getId());
    };
}
