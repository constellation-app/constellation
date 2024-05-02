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
package au.gov.asd.tac.constellation.plugins.gui;

import au.gov.asd.tac.constellation.plugins.parameters.ParameterChange;
import static au.gov.asd.tac.constellation.plugins.parameters.ParameterChange.ENABLED;
import static au.gov.asd.tac.constellation.plugins.parameters.ParameterChange.VALUE;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.types.DateTimeRange;
import au.gov.asd.tac.constellation.plugins.parameters.types.DateTimeRangeParameterType.DateTimeRangeParameterValue;
import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.apache.commons.lang3.StringUtils;

/**
 * A pane that allows the entry of a relative or absolute date-time range, along with a time-zone, which is the GUI
 * element corresponding to a {@link PluginParameter} of
 * {@link au.gov.asd.tac.constellation.plugins.parameters.types.DateTimeRangeParameterType}.
 * <p>
 * Selecting a relative range, altering the absolute range, or changing the time-zone will all update the object value
 * of the underlying {@link PluginParameter}.
 *
 * @see au.gov.asd.tac.constellation.plugins.parameters.types.DateTimeRangeParameterType
 *
 * @author algol
 */
public final class DateTimeRangeInputPane extends Pane {

    private static final String HIGHLIGHTED_CLASS = "titled-pane-datetime";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final Logger LOGGER = Logger.getLogger(DateTimeRangeInputPane.class.getName());

    private static final double CONTROLPANE_SPACING = 2;

    private final String FONT_COLOR = JavafxStyleManager.isDarkTheme() ? "-fx-text-fill: #FFFFFF;" : "-fx-text-fill: #111111;";

    private final ToggleGroup dateRangeGroup = new ToggleGroup();

    private final HBox relPane;
    private final TitledPane absPane;

    // Store the GUI elements.
    // As the elements get created (in the correct order), they'll be added here.
    // This keeps them easily accessible.
    private final List<TimeRangeToggleButton> relativeButtons;
    private final List<javafx.scene.control.DatePicker> datePickers;
    private final List<Spinner<Integer>> timeSpinners;
    private final ComboBox<String> timeZonesCombo;

    // True if the UI elements are being set behind the scenes.
    // Usage is "isAdjusting = true; /* adjust UI elements */; Platform.runLater(() -> {isAdjusting = false;});"
    // It seems that JavaFX change listeners are run asynchronously, so just setting "isAdjusting = false;"
    // after adjusting the UI means that the listener will possibly get the wrong value.
    // Since the listeners are run in the JavaFX  thread, using runLater() will ensure that isAdjusting doesn't
    // get reset until after the listener runs.
    private volatile boolean isAdjusting;

    public DateTimeRangeInputPane(final PluginParameter<DateTimeRangeParameterValue> parameter) {
        isAdjusting = false;

        relativeButtons = new ArrayList<>();
        datePickers = new ArrayList<>(2);
        timeSpinners = new ArrayList<>(6);

        // Build the timezone selection with a default value of UTC.
        timeZonesCombo = new ComboBox<>(getZoneIds());
        setZoneId("UTC");

        // The change listener for the absolute range controls.
        // This called (directly or indirectly) when anything in the absolute range area is clicked or changed.
        final ChangeListener<String> changed = (final ObservableValue<? extends String> observable, final String oldValue, final String newValue) -> {
            if (!isAdjusting) {
                try {
                    final ZonedDateTime[] zdt = getAbsoluteRange(getZoneId());
                    if (zdt != null) {
                        isAdjusting = true;
                        parameter.setObjectValue(new DateTimeRange(zdt[0], zdt[1]));
                    }
                } catch (DateTimeException e) {
                    // chew up and throw away date time exception, this results in datetime reverting back into
                    // allowable range, ie if you enter 33 for hours, this will be thrown out and revert to 23.
                } finally {
                    Platform.runLater(() -> isAdjusting = false);
                }
            }
        };

        // When the timezone changes, we need to update the datetimes to match.
        // We keep the same instant with the new timezone.
        timeZonesCombo.getSelectionModel().selectedItemProperty().addListener((final ObservableValue<? extends String> observable, final String oldValue, final String newValue) -> {
            if (!isAdjusting) {
                final ZonedDateTime[] zdt = getAbsoluteRange(getZoneId(oldValue));
                isAdjusting = true;
                try {
                    final ZoneId zi = getZoneId();
                    final ZonedDateTime zdt0 = zdt[0].withZoneSameInstant(zi);
                    final ZonedDateTime zdt1 = zdt[1].withZoneSameInstant(zi);
                    datePickers.get(0).setValue(zdt0.toLocalDate());
                    datePickers.get(1).setValue(zdt1.toLocalDate());
                    timeSpinners.get(0).getValueFactory().setValue(zdt0.getHour());
                    timeSpinners.get(1).getValueFactory().setValue(zdt0.getMinute());
                    timeSpinners.get(2).getValueFactory().setValue(zdt0.getSecond());
                    timeSpinners.get(3).getValueFactory().setValue(zdt1.getHour());
                    timeSpinners.get(4).getValueFactory().setValue(zdt1.getMinute());
                    timeSpinners.get(5).getValueFactory().setValue(zdt1.getSecond());
                } finally {
                    Platform.runLater(() -> {
                        isAdjusting = false;

                        // Call the absolute range area listener.
                        changed.changed(observable, null, null);
                    });
                }
            }
        });

        final Pane picker1 = createPicker("From:", changed);
        final Pane picker2 = createPicker("To:", changed);
        final HBox pickers = new HBox(CONTROLPANE_SPACING, picker1, picker2);

        // take up half of the first date picker's width
        timeZonesCombo.maxWidthProperty().bind(pickers.widthProperty().divide(2.0));

        // Convenience button to change to UTC.
        final Button utcButton = new Button("UTC");
        utcButton.setOnAction(event -> setZoneId("UTC"));
        // take up half of the second date picker's width
        utcButton.minWidthProperty().bind(pickers.widthProperty().divide(4.0));
        utcButton.maxWidthProperty().bind(pickers.widthProperty().divide(4.0));

        // Convenience button to change to the local timezone.
        final Button localButton = new Button("Local");
        localButton.setOnAction(event -> setZoneId(ZoneId.systemDefault().getId()));
        // use the remainig width of the second date picker
        localButton.minWidthProperty().bind(pickers.widthProperty().divide(4.0));
        localButton.maxWidthProperty().bind(pickers.widthProperty().divide(4.0));

        // Convenience button to change to the max range
        final Button maxButton = new Button("Use Maximum Time Range");
        maxButton.setOnAction(event -> {
            isAdjusting = true;
            try {
                timeSpinners.get(0).getValueFactory().setValue(0);
                timeSpinners.get(1).getValueFactory().setValue(0);
                timeSpinners.get(2).getValueFactory().setValue(0);
                timeSpinners.get(3).getValueFactory().setValue(23);
                timeSpinners.get(4).getValueFactory().setValue(59);
                timeSpinners.get(5).getValueFactory().setValue(59);
            } finally {
                Platform.runLater(() -> {
                    isAdjusting = false;

                    // Call the absolute range area listener.
                    changed.changed(null, null, null);
                });
            }
        });
        maxButton.maxWidthProperty().bind(pickers.widthProperty().divide(2.0));

        final HBox tzBox = new HBox(timeZonesCombo, utcButton, localButton);

        absPane = new TitledPane("Absolute range", new VBox(pickers, tzBox, maxButton));
        absPane.setStyle(FONT_COLOR);

        // The change listener for a relative range button.
        // When a toggle button is selected, the absolute area is updated to match.
        final EventHandler<ActionEvent> toggleHandler = (final ActionEvent event) -> {
            if (!isAdjusting) {
                final Period period = ((TimeRangeToggleButton) event.getSource()).getPeriod();

                isAdjusting = true;
                try {
                    parameter.setObjectValue(new DateTimeRange(period, getZoneId()));
                    setAbsoluteNow(period);
                } finally {
                    Platform.runLater(() -> isAdjusting = false);
                }
            }

            setUsingAbsolute(false);
        };

        // Buttons to set relative ranges.
        relativeButtons.add(new TimeRangeToggleButton(Period.ofDays(1), dateRangeGroup, toggleHandler));
        relativeButtons.add(new TimeRangeToggleButton(Period.ofDays(2), dateRangeGroup, toggleHandler));
        relativeButtons.add(new TimeRangeToggleButton(Period.ofDays(3), dateRangeGroup, toggleHandler));
        relativeButtons.add(new TimeRangeToggleButton(Period.ofDays(4), dateRangeGroup, toggleHandler));
        relativeButtons.add(new TimeRangeToggleButton(Period.ofDays(7), dateRangeGroup, toggleHandler));
        relativeButtons.add(new TimeRangeToggleButton(Period.ofDays(14), dateRangeGroup, toggleHandler));
        relativeButtons.add(new TimeRangeToggleButton(Period.ofMonths(1), dateRangeGroup, toggleHandler));
        relativeButtons.add(new TimeRangeToggleButton(Period.ofMonths(3), dateRangeGroup, toggleHandler));
        relativeButtons.add(new TimeRangeToggleButton(Period.ofMonths(6), dateRangeGroup, toggleHandler));
        relativeButtons.add(new TimeRangeToggleButton(Period.ofMonths(12), dateRangeGroup, toggleHandler));
        relativeButtons.add(new TimeRangeToggleButton(Period.ofMonths(24), dateRangeGroup, toggleHandler));
        relPane = new HBox();
        relPane.setSpacing(3);
        relPane.getChildren().addAll(
                new Label("Days:"),
                relativeButtons.get(0),
                relativeButtons.get(1),
                relativeButtons.get(2),
                relativeButtons.get(3),
                relativeButtons.get(4),
                relativeButtons.get(5),
                new Label("Months:"),
                relativeButtons.get(6),
                relativeButtons.get(7),
                relativeButtons.get(8),
                relativeButtons.get(9),
                relativeButtons.get(10)
        );

        absPane.getContent().addEventFilter(MouseEvent.MOUSE_CLICKED, (final MouseEvent event) -> {
            changed.changed(null, null, null);
            setUsingAbsolute(true);
        });

        final VBox container = new VBox(CONTROLPANE_SPACING);
        container.getChildren().addAll(relPane, absPane);
        getChildren().add(container);

        // Initial values.
        // If the parameter is unset, set it with our default.
        // Otherwise, set ourself from the parameter.
        final DateTimeRange dtr = parameter.getDateTimeRangeValue();
        if (dtr.getPeriod() != null) {
            setPeriod(dtr.getPeriod(), dtr.getZoneId());
            absPane.setExpanded(false);
        } else {
            final ZonedDateTime[] zdt = dtr.getZonedStartEnd();
            setAbsolute(zdt[0], zdt[1]);
            absPane.setExpanded(true);
        }

        setDisable(!parameter.isEnabled());
        setManaged(parameter.isVisible());
        setVisible(parameter.isVisible());

        // The GUI is done.
        // Set up the parameter listener.
        parameter.addListener((final PluginParameter<?> pluginParameter, final ParameterChange change) -> Platform.runLater(() -> {
            switch (change) {
                case VALUE -> {
                    if (!isAdjusting) {
                        // don't change the value if it isn't necessary.
                        final DateTimeRange param = pluginParameter.getDateTimeRangeValue();
                        if (param.getPeriod() != null) {
                            setPeriod(param.getPeriod(), param.getZoneId());
                            absPane.setExpanded(false);
                        } else {
                            final ZonedDateTime[] z = param.getZonedStartEnd();
                            setAbsolute(z[0], z[1]);
                            absPane.setExpanded(true);
                        }
                    }
                }
                case ENABLED ->
                    setDisable(!pluginParameter.isEnabled());
                case VISIBLE -> {
                    setManaged(parameter.isVisible());
                    setVisible(parameter.isVisible());
                }
                default ->
                    LOGGER.log(Level.FINE, "ignoring parameter change type {0}.", change);
            }
        }));
    }

    /**
     * Set a relative range ending at the current instant.
     *
     * @param period The period to set. Must be the same as one of the buttons.
     * @param zi The ZoneId used for displaying the corresponding absolute range.
     */
    public void setPeriod(final Period period, final ZoneId zi) {
        isAdjusting = true;
        try {
            for (final TimeRangeToggleButton b : relativeButtons) {
                if (b.period.equals(period)) {
                    b.setSelected(true);
                    setAbsoluteNow(period, zi);
                    setUsingAbsolute(false);
                    break;
                }
            }
        } finally {
            Platform.runLater(() -> isAdjusting = false);
        }
    }

    /**
     * Set an absolute range.
     *
     * @param zdt0 zoned date time 0.
     * @param zdt1 zoned date time 1.
     */
    public void setAbsolute(final ZonedDateTime zdt0, final ZonedDateTime zdt1) {
        isAdjusting = true;
        try {
            datePickers.get(0).setValue(zdt0.toLocalDate());
            datePickers.get(1).setValue(zdt1.toLocalDate());
            timeSpinners.get(0).getValueFactory().setValue(zdt0.getHour());
            timeSpinners.get(1).getValueFactory().setValue(zdt0.getMinute());
            timeSpinners.get(2).getValueFactory().setValue(zdt0.getSecond());
            timeSpinners.get(3).getValueFactory().setValue(zdt1.getHour());
            timeSpinners.get(4).getValueFactory().setValue(zdt1.getMinute());
            timeSpinners.get(5).getValueFactory().setValue(zdt1.getSecond());

            setZoneId(zdt0.getZone().getId());
        } finally {
            Platform.runLater(() -> isAdjusting = false);
        }

        setUsingAbsolute(true);

    }

    private void setAbsoluteNow(final Period period) {
        setAbsoluteNow(period, getZoneId());
    }

    private void setAbsoluteNow(final Period period, final ZoneId zi) {
        final ZonedDateTime zdt1 = ZonedDateTime.now(zi);
        final ZonedDateTime zdt0 = zdt1.minus(period);
        datePickers.get(0).setValue(zdt0.toLocalDate());
        datePickers.get(1).setValue(zdt1.toLocalDate());
        timeSpinners.get(0).getValueFactory().setValue(zdt0.getHour());
        timeSpinners.get(1).getValueFactory().setValue(zdt0.getMinute());
        timeSpinners.get(2).getValueFactory().setValue(zdt0.getSecond());
        timeSpinners.get(3).getValueFactory().setValue(zdt1.getHour());
        timeSpinners.get(4).getValueFactory().setValue(zdt1.getMinute());
        timeSpinners.get(5).getValueFactory().setValue(zdt1.getSecond());
    }

    /**
     * Get the absolute range values from the absolute range area.
     * <p>
     * Can return null if the user hasn't entered anything in the date picker.
     *
     * @param zi The ZoneId to use for the returned values.
     *
     * @return A two-element array containing the start and end datetimes, or null if data entry is incomplete.
     */
    public ZonedDateTime[] getAbsoluteRange(final ZoneId zi) {
        // This gets called with values and throws exceptions depending on spinner values which must be handled
        // Check for nulls in case the values haven't been set yet.
        if (zi != null) {
            final LocalDate ld0 = datePickers.get(0).getValue();
            if (ld0 != null) {
                final LocalTime lt0 = LocalTime.of(getSpinnerValue(0), getSpinnerValue(1), getSpinnerValue(2));
                final ZonedDateTime zdt0 = ZonedDateTime.of(ld0, lt0, zi);

                final LocalDate ld1 = datePickers.get(1).getValue();
                if (ld1 != null) {
                    final LocalTime lt1 = LocalTime.of(getSpinnerValue(3), getSpinnerValue(4), getSpinnerValue(5));
                    final ZonedDateTime zdt1 = ZonedDateTime.of(ld1, lt1, zi);

                    return new ZonedDateTime[]{zdt0, zdt1};
                }
            }
        }
        return new ZonedDateTime[]{};
    }

    /**
     * Get the value of a NumberSpinner.
     * <p>
     * When the text is blank, the NumberSpinner returns null: we'll take this to mean 0.
     *
     * @param ix The index of the NumberSpinner to read from.
     * @return The value of the indexed NumberSpinner.
     */
    private int getSpinnerValue(final int ix) {
        final Number num = timeSpinners.get(ix).getValue();
        return num != null ? num.intValue() : 0;
    }

    /**
     * Set the displayed timezone in the ComboBox.
     * <p>
     * This in turn will notify the timeZonesCombo listener and update the parameter.
     *
     * @param id The string specifying the ZoneId of the timezone to display.
     */
    private void setZoneId(final String id) {
        for (final String item : timeZonesCombo.getItems()) {
            final String itemPart = item.split(" ", 3)[1];
            if (itemPart.equals(id)) {
                timeZonesCombo.getSelectionModel().select(item);
                break;
            }
        }
    }

    /**
     * Get the selected ZoneId from the timezone ComboBox.
     * <p>
     * The ComboBox holds a list of strings in a human-readable form. We need to extract the zoneid part and turn it
     * into a ZoneId.
     *
     * @return The selected ZoneId, or null if nothing is selected.
     */
    private ZoneId getZoneId() {
        return getZoneId(timeZonesCombo.getValue());
    }

    /**
     * Get the selected ZoneId from the timezone ComboBox.
     * <p>
     * The ComboBox holds a list of strings in a human-readable form. We need to extract the zoneid part and turn it
     * into a ZoneId.
     *
     * @param tz a string value as used by the timezone ComboBox.
     *
     * @return The selected ZoneId, or null if nothing is selected.
     */
    private ZoneId getZoneId(final String tz) {
        if (tz != null) {
            final String id = tz.split(" ", 3)[1];
            return ZoneId.of(id);
        }

        return null;
    }

    /**
     * Set the highlighting of the TitlePane's title.
     * <p>
     * Highlighting is done by adding/removing our own style class. We attempt to make sure that we don't add it more
     * than once.
     *
     * @param highlight True to be highlighted, false to be unhighlighted.
     */
    private void setUsingAbsolute(final boolean highlight) {
        final ObservableList<String> classes = absPane.getStyleClass();
        if (highlight) {
            if (!classes.contains(HIGHLIGHTED_CLASS)) {
                classes.add(0, HIGHLIGHTED_CLASS);
                clearRangeButtons();
            }
        } else {
            classes.remove(HIGHLIGHTED_CLASS);
        }
    }

    public void clearRangeButtons() {
        dateRangeGroup.getToggles().stream().forEach(button -> button.setSelected(false));
    }

    /**
     * Build a single datetime picker.
     *
     * @param label
     * @param ld
     * @param h
     * @param m
     * @param s
     * @param listener
     * @return
     */
    private Pane createPicker(final String label, final ChangeListener<String> changed) {
        final Label dpLabel = new Label(label);
        final DatePicker dp = new DatePicker();
        dp.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(final LocalDate date) {
                return date != null ? DATE_FORMATTER.format(date) : "";
            }

            @Override
            public LocalDate fromString(final String s) {
                return StringUtils.isNotBlank(s) ? LocalDate.parse(s, DATE_FORMATTER) : null;
            }
        });
        dpLabel.setLabelFor(dp);
        final HBox dpBox = new HBox(dpLabel, dp);
        final HBox spinnerBox = new HBox(CONTROLPANE_SPACING / 2);
        dp.maxWidthProperty().bind(spinnerBox.widthProperty().multiply(2.0 / 3.0));
        spinnerBox.getChildren().addAll(createSpinner("Hour", 0, 23, changed), createSpinner("Minute", 0, 59, changed), createSpinner("Second", 0, 59, changed));
        final VBox picker = new VBox(dpBox, spinnerBox);
        picker.setStyle("-fx-padding:4; -fx-border-radius:4; -fx-border-color: grey;");

        dp.getEditor().textProperty().addListener(changed);

        // The DatePicker has the horrible problem that you can type in the text field, but the value won't change,
        // so if you type a new date and click Go, the old date will be used, not the new date that you can see.
        // The simplest way to avoid this is to disable the text field. :-(
        dp.getEditor().setDisable(true);

        datePickers.add(dp);

        return picker;
    }

    /**
     * Build the hour/minute/second part of the datetime picker.
     *
     * @param label
     * @param min
     * @param max
     * @param value
     * @param changed
     * @return
     */
    private Pane createSpinner(final String label, final int min, final int max, final ChangeListener<String> changed) {
        final int NUMBER_SPINNER_WIDTH = 55;
        final String small = "-fx-font-size: 75%;";

        final Spinner<Integer> spinner = new Spinner<>(min, max, 1);
        spinner.setPrefWidth(NUMBER_SPINNER_WIDTH);

        // Create a filter to limit text entry to just numerical digits
        final NumberFormat format = NumberFormat.getIntegerInstance();
        final UnaryOperator<TextFormatter.Change> filter = c -> {
            if (c.isContentChange()) {
                final ParsePosition parsePosition = new ParsePosition(0);
                // NumberFormat evaluates the beginning of the text
                format.parse(c.getControlNewText(), parsePosition);
                if (parsePosition.getIndex() == 0 || c.getControlNewText().length() > 2
                        || parsePosition.getIndex() < c.getControlNewText().length()) {
                    // reject parsing the complete text failed
                    return null;
                }
            }
            return c;
        };

        // Ensure spinner is set to editable, meaning user can directly edit text, then hook in
        // a text formatter which in turn will trigger flitering of input text.
        spinner.setEditable(true);
        final TextFormatter<Integer> timeFormatter = new TextFormatter<>(new IntegerStringConverter(), 0, filter);
        spinner.getEditor().setTextFormatter(timeFormatter);

        // Set spinner to enable value wrapping
        spinner.getValueFactory().setWrapAround(true);

        final Label spinnerLabel = new Label(label);
        spinnerLabel.setLabelFor(spinner);
        spinnerLabel.setStyle(small);

        final VBox vbox = new VBox();
        vbox.getChildren().addAll(spinnerLabel, spinner);

        spinner.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
                -> changed.changed(null, null, null));

        timeSpinners.add(spinner);

        return vbox;
    }

    /**
     * Build a list of human readable ZoneId strings to display in the ComboBox.
     *
     * @return An ObservableList<String> of human readable ZoneId strings.
     */
    private static ObservableList<String> getZoneIds() {
        final DateTimeFormatter zf = DateTimeFormatter.ofPattern("Z");
        final Instant instant = Instant.now();
        final Set<String> zoneSet = ZoneId.getAvailableZoneIds();
        List<ZoneId> zoned = zoneSet.stream().map(ZoneId::of).collect(Collectors.toList());

        Collections.sort(zoned, (final ZoneId zi1, final ZoneId zi2) -> {
            final ZonedDateTime z1 = ZonedDateTime.ofInstant(instant, zi1);
            final ZonedDateTime z2 = ZonedDateTime.ofInstant(instant, zi2);
            final int off1 = z1.getOffset().getTotalSeconds();
            final int off2 = z2.getOffset().getTotalSeconds();
            if (off1 != off2) {
                return off1 - off2;
            }

            return zi1.getId().compareTo(zi2.getId());
        });

        final ObservableList<String> zones = FXCollections.observableArrayList();
        zoned.stream().forEach(zi -> {
            final ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, zi);
            zones.add(String.format("%s %s [%s]", zdt.format(zf), zi.getId(), zi.getDisplayName(TextStyle.FULL, Locale.getDefault())));
        });

        return zones;
    }

    /**
     * A ToggleButton that doesn't untoggle.
     */
    private static class NonToggleButton extends ToggleButton {

        /**
         * Toggles the state of the radio button if and only if the RadioButton has not already selected or is not part
         * of a {@link ToggleGroup}.
         */
        @Override
        public void fire() {
            // we don't toggle from selected to not selected if part of a group
            if (getToggleGroup() == null || !isSelected()) {
                super.fire();
            }
        }
    }

    /**
     * A toggle button to set a predefined time range
     */
    private static class TimeRangeToggleButton extends NonToggleButton {

        private final Period period;

        /**
         *
         * @param period A Period containing either months or days.
         *
         * @param group
         */
        public TimeRangeToggleButton(final Period period, final ToggleGroup group, final EventHandler<ActionEvent> toggleHandler) {
            this.period = period;

            final String label = String.valueOf(period.getMonths() != 0 ? period.getMonths() : period.getDays());
            setText(label);
            setToggleGroup(group);
            getStyleClass().add("time-range-toggle");

            setOnAction(event -> toggleHandler.handle(event));
        }

        public Period getPeriod() {
            return period;
        }

        @Override
        public final String toString() {
            return String.format("[%s %s selected=%s]", TimeRangeToggleButton.class.getSimpleName(), period, this.isSelected());
        }
    }
}
