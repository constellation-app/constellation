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
import static au.gov.asd.tac.constellation.plugins.parameters.ParameterChange.VISIBLE;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.NumberParameterValue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import org.apache.commons.lang3.StringUtils;

/**
 * A NumberSpinner allowing numeric entries, which is the GUI element
 * corresponding to a {@link PluginParameter} of
 * {@link au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType}
 * or
 * {@link au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType}.
 * <p>
 * Editing the number spinner's field, either with the buttons or directly, will
 * set the string value of underlying {@link PluginParameter}, and also cause
 * the parameter to validate this value.
 *
 * @param <T> The type of {@link Number} values stored. Note that only
 * {@link Integer} and {@link Float} are supported.
 * @see
 * au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType
 * @see au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType
 *
 * @author algol
 * @author antares
 */
public class NumberInputPane<T> extends Pane {

    private static final Logger LOGGER = Logger.getLogger(NumberInputPane.class.getName());
    
    private final Spinner<T> field;

    private String currentTextValue = null;

    private static final int CHAR_SIZE = 8;
    private static final int BASE_WIDTH = 35;

    private static final String INVALID_ID = "invalid";
    private static final String INVALID_VALUE = "Invalid value";

    public NumberInputPane(final PluginParameter<?> parameter) {
        final NumberParameterValue pv = (NumberParameterValue) parameter.getParameterValue();
        final Number min = pv.getMinimumValue();
        final Number max = pv.getMaximumValue();
        final Number init = pv.getNumberValue();
        final Number step = pv.getStepValue();
        final Boolean shrinkWidth = (Boolean) parameter.getProperty(FloatParameterType.SHRINK_VAL);

        switch (parameter.getType().getId()) {
            case IntegerParameterType.ID:
                field = new Spinner<>(
                        min == null ? Integer.MIN_VALUE : min.intValue(),
                        max == null ? Integer.MAX_VALUE : max.intValue(),
                        init == null ? 0 : init.intValue(),
                        step == null ? 1 : step.intValue()
                );
                break;
            case FloatParameterType.ID:
                field = new Spinner<>(
                        min == null ? Double.MIN_VALUE : min.doubleValue(),
                        max == null ? Double.MAX_VALUE : max.doubleValue(),
                        init == null ? 0 : init.doubleValue(),
                        step == null ? 1 : step.doubleValue()
                );
                break;
            default:
                throw new IllegalArgumentException(String.format("Unsupported type %s found.", parameter.getType().getId()));
        }

        if (shrinkWidth != null && shrinkWidth) {
            final int maxIntegers = max == null ? 10 : (int) Math.floor(Math.log10(max.doubleValue()) + 1);
            int maxDecimals;
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

        final Tooltip tooltip = new Tooltip("");
        tooltip.setStyle("-fx-text-fill: white;");

        // For (FXcontrol) number spinners, we want to listen to the text property rather than the value property.
        // Just typing doesn't fire value property change events, and doesn't allow us to change the style
        // when the string doesn't validate.
        field.getEditor().textProperty().addListener((final ObservableValue<? extends String> ov, final String oldValue, final String newValue) -> {
            final String error = parameter.validateString(field.getValueFactory().getValue().toString());
            if (error != null) {
                tooltip.setText(error);
                field.setTooltip(tooltip);
                field.setId(INVALID_ID);
            } else {
                tooltip.setText("");
                field.setTooltip(null);
                field.setId("");
            }
            
            currentTextValue = newValue;
            parameter.fireChangeEvent(ParameterChange.VALUE);
        });

        parameter.addListener((pluginParameter, change) ->
            Platform.runLater(() -> {
                switch (change) {
                    case VALUE:
                        if (StringUtils.isNotBlank(currentTextValue) && (!currentTextValue.equals(parameter.getStringValue()) || parameter.getError() != null)) {
                            try {
                                parameter.setError(null);
                                switch (parameter.getType().getId()) {
                                    case IntegerParameterType.ID:
                                        final int currentIntegerValue = Integer.valueOf(currentTextValue);
                                        if ((min != null && currentIntegerValue < min.intValue()) 
                                                || (max != null && currentIntegerValue > max.intValue())) {
                                            field.setId(INVALID_ID);
                                            parameter.setError(INVALID_VALUE);
                                        }
                                        // this won't succeed if we entered the if block before this but it will
                                        // add some helpful logging to indicate the problem in that instance
                                        parameter.setIntegerValue(currentIntegerValue);
                                        break;
                                    case FloatParameterType.ID:
                                        final float currentFloatValue = Float.valueOf(currentTextValue);
                                        if ((min != null && currentFloatValue < min.doubleValue())
                                                || (max != null && currentFloatValue > max.doubleValue())) {
                                            field.setId(INVALID_ID);
                                            parameter.setError(INVALID_VALUE);
                                        }
                                        // this won't succeed if we entered the if block before this but it will
                                        // add some helpful logging to indicate the problem in that instance
                                        parameter.setFloatValue(currentFloatValue);
                                        break;
                                    default:
                                        break;
                                }
                            } catch (final NumberFormatException ex) {
                                field.setId(INVALID_ID);
                                parameter.setError(INVALID_VALUE);
                            }
                        } else if (currentTextValue != null && currentTextValue.isEmpty()) {
                            field.setId(INVALID_ID);
                            parameter.setError(INVALID_VALUE);
                        } else {
                            // Do nothing
                        }
                        break;
                    case ENABLED:
                        field.setDisable(!pluginParameter.isEnabled());
                        break;
                    case VISIBLE:
                        field.setManaged(parameter.isVisible());
                        field.setVisible(parameter.isVisible());
                        this.setVisible(parameter.isVisible());
                        this.setManaged(parameter.isVisible());
                        break;
                    default:
                        LOGGER.log(Level.FINE, "ignoring parameter change type {0}.", change);
                        break;
                }
            })
        );
        getChildren().add(field);
    }
}
