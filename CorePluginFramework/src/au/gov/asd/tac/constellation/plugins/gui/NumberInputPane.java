/*
 * Copyright 2010-2026 Australian Signals Directorate
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

import static au.gov.asd.tac.constellation.plugins.parameters.ParameterChange.ENABLED;
import static au.gov.asd.tac.constellation.plugins.parameters.ParameterChange.VALUE;
import static au.gov.asd.tac.constellation.plugins.parameters.ParameterChange.VISIBLE;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.NumberParameterValue;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;

/**
 * A NumberSpinner allowing numeric entries, which is the GUI element corresponding to a {@link PluginParameter} of
 * {@link au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType} or
 * {@link au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType}.
 * <p>
 * Editing the number spinner's field, either with the buttons or directly, will set the string value of underlying
 * {@link PluginParameter}, and also cause the parameter to validate this value.
 *
 * @param <T> The type of {@link Number} values stored. Note that only {@link Integer} and {@link Float} are supported.
 * @see au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType
 * @see au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType
 *
 * @author algol
 * @author antares
 */
public class NumberInputPane<T> extends Pane {

    private static final Logger LOGGER = Logger.getLogger(NumberInputPane.class.getName());
    private static final int CHAR_SIZE = 8;
    private static final int BASE_WIDTH = 35;
    private static final String INVALID_ID = "invalid";
    private static final String INVALID_VALUE = "Invalid value";

    private final PluginParameter<?> parameter;
    private final Tooltip tooltip;
    private final Spinner<T> field;

    private int repeatedOccurrences = 0;

    public NumberInputPane(final PluginParameter<?> parameter) {
        final NumberParameterValue pv = (NumberParameterValue) parameter.getParameterValue();
        final Number min = pv.getMinimumValue();
        final Number max = pv.getMaximumValue();
        final Number init = pv.getNumberValue();
        final Number step = pv.getStepValue();
        final Boolean shrinkWidth = (Boolean) parameter.getProperty(FloatParameterType.SHRINK_VAL);

        switch (parameter.getType().getId()) {
            case IntegerParameterType.ID -> {
                final int minVal = min == null ? Integer.MIN_VALUE : min.intValue();
                final int maxVal = max == null ? Integer.MAX_VALUE : max.intValue();
                final int initVal = init == null ? 0 : init.intValue();
                final int stepVal = step == null ? 1 : step.intValue();

                field = new Spinner<>(minVal, maxVal, initVal, stepVal);
                field.setValueFactory((SpinnerValueFactory<T>) customIntegerSpinnerValueFactory(minVal, maxVal, initVal));
            }
            case FloatParameterType.ID ->
                field = new Spinner<>(
                        min == null ? Double.MIN_VALUE : min.doubleValue(),
                        max == null ? Double.MAX_VALUE : max.doubleValue(),
                        init == null ? 0 : init.doubleValue(),
                        step == null ? 1 : step.doubleValue()
                );
            default ->
                throw new IllegalArgumentException(String.format("Unsupported type %s found.", parameter.getType().getId()));
        }

        if (shrinkWidth != null && shrinkWidth) {
            final int maxIntegers = max == null ? 10 : (int) Math.floor(Math.log10(max.doubleValue()) + 1);
            final int maxDecimals;
            if (step == null) {
                maxDecimals = 3;
            } else {
                maxDecimals = Math.log10(step.doubleValue()) < 0 ? (int) -Math.ceil((Math.log10(step.doubleValue()) - 1)) : 0;
            }
            final int width = (maxIntegers + maxDecimals) * CHAR_SIZE + BASE_WIDTH;
            field.setPrefWidth(width);
            field.setMinWidth(width);
        }

        if (parameter.getParameterValue().getGuiInit() != null) {
            parameter.getParameterValue().getGuiInit().init(field);
        }

        field.setDisable(!parameter.isEnabled());
        field.setManaged(parameter.isVisible());
        field.setVisible(parameter.isVisible());
        field.setEditable(true);
        this.setManaged(parameter.isVisible());
        this.setVisible(parameter.isVisible());

        tooltip = new Tooltip("");
        tooltip.setStyle("-fx-text-fill: white;");
        this.parameter = parameter;

        // For (FXcontrol) number spinners, we want to listen to the text property rather than the value property.
        // Just typing doesn't fire value property change events, and doesn't allow us to change the style
        // when the string doesn't validate.
        field.getEditor().textProperty().addListener((ov, oldValue, newValue) -> {
            handleTextPropertyChange(oldValue, newValue);
        });

        // treat parameter value as truth?
        parameter.addListener((pluginParameter, change)
                -> Platform.runLater(() -> {
                    switch (change) {
                        case VALUE -> {
                            // Value has been set, so instead of setting again based on current text value, lets update text if needed
                            if (!field.getEditor().getText().equals(parameter.getStringValue())) {
                                field.getEditor().setText(parameter.getStringValue());
                            }
                            setParameterBasedOnType(parameter, min, max);
                        }
                        case ENABLED ->
                            field.setDisable(!pluginParameter.isEnabled());
                        case VISIBLE -> {
                            field.setManaged(parameter.isVisible());
                            field.setVisible(parameter.isVisible());
                            this.setVisible(parameter.isVisible());
                            this.setManaged(parameter.isVisible());
                        }
                        default ->
                            LOGGER.log(Level.FINE, "ignoring parameter change type {0}.", change);
                    }
                })
        );
        getChildren().add(field);
    }

    private void handleTextPropertyChange(final String oldValue, final String newValue) {
        if (newValue.isEmpty() || "-".equals(newValue)) {
            // Detected a backspace/overwrite. The resulting value is just a minus sign, or an empty string.
            // Clear editor as it seems to retain the "-". Reset to minimum value.
            final NumberParameterValue pv = (NumberParameterValue) parameter.getParameterValue();
            field.getEditor().setText("");
            field.getEditor().setText(newValue + (pv.getMinimumValue() != null ? Integer.toString(pv.getMinimumValue().intValue()) : "0"));
            if (field.getEditor().getText().equals(oldValue)) {
                repeatedOccurrences++;
            } else {
                repeatedOccurrences = 0;
            }
            Platform.runLater(()
                    -> // Auto-select the numeric portion of the new text, to allow immediate overwriting of the inserted value.
                    field.getEditor().selectRange((repeatedOccurrences % 2 == 1) ? 0 : newValue.length(), field.getEditor().getText().length()));
            return;
        }

        final int dotPos = newValue.indexOf(SeparatorConstants.PERIOD);
        final String intPart = dotPos > -1 ? newValue.substring(0, dotPos) : newValue;
        final String decPart = dotPos > -1 ? newValue.substring(dotPos + 1) : "";
        final boolean isIntVal = parameter.getType().getId().equals(IntegerParameterType.ID);

        if (!isNewValueValid(intPart, decPart, isIntVal, dotPos)) {
            // Undo Editing. Revert to previous value.
            field.getEditor().setText(oldValue);
            return;
        }

        // Valid
        final String error = parameter.validateString(newValue);
        if (error != null) {
            // Error
            tooltip.setText(error);
            field.setTooltip(tooltip);
            field.setId(INVALID_ID);
            field.getEditor().setText(oldValue);
            parameter.setStringValue(oldValue);
        } else {
            // No error
            tooltip.setText("");
            field.setTooltip(null);
            field.setId("");
            parameter.setStringValue(newValue);
        }
    }

    private boolean isNewValueValid(final String intPart, final String decPart, final boolean isIntVal, final int dotPos) {
        // Integers: MAX_VALUE is 10 digits.  Floats: Max 8 digits before the decimal, and 2 digits after.
        return (intPart.matches("[\\-][0-9]{1," + (isIntVal ? "10}" : "8}")) || intPart.matches("[0-9]{1," + (isIntVal ? "10}" : "8}")))
                && (dotPos == -1 || (decPart.matches("[0-9]{0,2}") && !isIntVal));
    }

    private void setParameterBasedOnType(final PluginParameter<?> parameter, final Number min, final Number max) {
        try {
            parameter.setError(null);
            switch (parameter.getType().getId()) {
                case IntegerParameterType.ID -> {
                    final int currentIntegerValue = parameter.getIntegerValue();
                    if ((min != null && currentIntegerValue < min.intValue())
                            || (max != null && currentIntegerValue > max.intValue())) {
                        field.setId(INVALID_ID);
                        parameter.setError(INVALID_VALUE);
                    }
                }
                case FloatParameterType.ID -> {
                    final float currentFloatValue = parameter.getFloatValue();
                    if ((min != null && currentFloatValue < min.doubleValue())
                            || (max != null && currentFloatValue > max.doubleValue())) {
                        field.setId(INVALID_ID);
                        parameter.setError(INVALID_VALUE);
                    }
                }
                default -> {
                    // do nothing
                }
            }
        } catch (final NumberFormatException ex) {
            field.setId(INVALID_ID);
            parameter.setError(INVALID_VALUE);
        }
    }

    /**
     * Create a custom Integer SpinnerValueFactory to override the increment and decrement methods because if you set
     * the max to Integer.MAX_VALUE and increment beyond the Integer.MAX_VALUE, the spinner spins over to the minimum
     * value; and if you decrement the spinner below the min value of Integer.MIN_VALUE, it ticks over to the
     * Integer.MAX_VALUE.
     *
     * @param min Minimum integer to set on the spinner
     * @param max Maximum integer to set on the spinner
     * @param initialValue Default initial value in spinner
     * @return IntegerSpinnerValueFactory
     */
    private IntegerSpinnerValueFactory customIntegerSpinnerValueFactory(final int min, final int max, final int initialValue) {
        final SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory
                = new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, initialValue) {
            @Override
            public void increment(final int steps) {
                final int currentValue = getValue();
                final int max = getMax(); // Get the maximum value for resetting

                // Set to the Integer.MAX_VALUE if current value already at max,
                // otherwise it ticks over to the minimum.
                if (currentValue == max) {
                    setValue(max);
                } else {
                    final int newValue = currentValue + steps;
                    // Reset to maximum when max is exceeded
                    setValue(newValue > max ? max : newValue);
                }
            }

            // override decrement() to reset to minimum when min is exceeded for decrementing below min.
            @Override
            public void decrement(final int steps) {
                final int currentValue = getValue();
                final int min = getMin(); // Get the minimum value for resetting

                // Set to the Integer.MIN_VALUE if current value already at min,
                // otherwise it ticks over to the maximum.
                if (currentValue == min) {
                    setValue(min);
                } else {
                    final int newValue = currentValue - steps;
                    // Reset to minimum when min is exceeded (optional, for wrapping)
                    setValue(newValue < min ? min : newValue);
                }
            }
        };
        return valueFactory;
    }
}
