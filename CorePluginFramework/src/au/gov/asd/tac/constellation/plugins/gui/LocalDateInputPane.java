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
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameterListener;
import au.gov.asd.tac.constellation.plugins.parameters.types.LocalDateParameterType.LocalDateParameterValue;
import au.gov.asd.tac.constellation.utilities.gui.field.DateInputField;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * A Date Picker, which is the GUI element corresponding to a
 * {@link PluginParameter} of
 * {@link au.gov.asd.tac.constellation.plugins.parameters.types.LocalDateParameterType}.
 * <p>
 * Selecting a date will set the local date value of the underlying
 * {@link PluginParameter}.
 *
 * @see
 * au.gov.asd.tac.constellation.plugins.parameters.types.LocalDateParameterType
 *
 * @author algol
 * @author capricornunicorn123
 */
public final class LocalDateInputPane extends ParameterInputPane<LocalDateParameterValue> {
    
    private static final Logger LOGGER = Logger.getLogger(LocalDateInputPane.class.getName());

    public LocalDateInputPane(final PluginParameter<LocalDateParameterValue> parameter) {
        super(new DateInputField(), parameter);
        final LocalDateParameterValue pv = parameter.getParameterValue();
        
        setFieldValue(pv.get());
    }

    @Override
    public ChangeListener getFieldChangeListener(PluginParameter<LocalDateParameterValue> parameter) {
        return (ChangeListener<LocalDate>) (ObservableValue<? extends LocalDate> observable, LocalDate oldValue, LocalDate newValue) -> {
            if (newValue != null) {
                parameter.setLocalDateValue(newValue);
            }
        };
    }

    @Override
    public PluginParameterListener getPluginParameterListener() {
        return (PluginParameter<?> pluginParameter, ParameterChange change) -> Platform.runLater(() -> {
                switch (change) {
                    case VALUE -> {
                        // Don't change the value if it isn't necessary.
                        final LocalDate param = pluginParameter.getLocalDateValue();
                        if (!param.equals(getFieldValue())) {
                            field.setValue(param);
                        }
                    }
                    case ENABLED -> updateFieldEnablement();
                    case VISIBLE -> updateFieldVisability();
                    default -> LOGGER.log(Level.FINE, "ignoring parameter change type {0}.", change);
                }
        });
    }
}
