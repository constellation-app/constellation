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
package au.gov.asd.tac.constellation.plugins.gui;

import au.gov.asd.tac.constellation.plugins.parameters.ParameterChange;
import static au.gov.asd.tac.constellation.plugins.parameters.ParameterChange.ENABLED;
import static au.gov.asd.tac.constellation.plugins.parameters.ParameterChange.VALUE;
import static au.gov.asd.tac.constellation.plugins.parameters.ParameterChange.VISIBLE;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameterListener;
import au.gov.asd.tac.constellation.plugins.parameters.types.NumberParameterValue;
import au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputFieldListener;
import au.gov.asd.tac.constellation.utilities.gui.field.NumberInput;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;

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
 * @param <C> The type of {@link Number} values stored. Note that only
 * {@link Integer} and {@link Float} are supported.
 * @see au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType
 * @see au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType
 *
 * @author algol
 * @author antares
 */
public class NumberInputPane<C extends Number> extends ParameterInputPane<NumberParameterValue, Number> {

    private static final Logger LOGGER = Logger.getLogger(NumberInputPane.class.getName());

    public NumberInputPane(final PluginParameter<NumberParameterValue> parameter) {
        super(new NumberInput<C>(
                parameter.getParameterValue().getMinimumValue(),
                parameter.getParameterValue().getMaximumValue(),
                parameter.getParameterValue().getNumberValue(),
                parameter.getParameterValue().getStepValue()
        ), parameter);
    }

    @Override
    public ConstellationInputFieldListener getFieldChangeListener(PluginParameter<NumberParameterValue> parameter) {
        return (ConstellationInputFieldListener<Number>) (Number newValue) -> {
            if (newValue != null) {
                parameter.setNumberValue(newValue);
            }
        };
    }

    @Override
    public PluginParameterListener getPluginParameterListener() {
        return (PluginParameter<?> parameter, ParameterChange change) -> Platform.runLater(() -> {
            @SuppressWarnings("unchecked") //mcPluginParameter is a MultiChoiceParameter
            final PluginParameter<NumberParameterValue> nPluginParameter = (PluginParameter<NumberParameterValue>) parameter;
            switch (change) {
                case VALUE -> {
                    // Don't change the value if it isn't necessary.
                    Number number = getFieldValue();
                    if (!number.equals(nPluginParameter.getNumberValue())){
                        setFieldValue(number);
                    }
                }                
                case ENABLED -> updateFieldEnablement();
                case VISIBLE -> updateFieldVisability();
                default -> LOGGER.log(Level.FINE, "ignoring parameter change type {0}.", change);
            }
        });
    }
}
