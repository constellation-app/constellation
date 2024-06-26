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
import au.gov.asd.tac.constellation.plugins.parameters.types.PasswordParameterValue;
import au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputFieldListener;
import au.gov.asd.tac.constellation.utilities.gui.field.PasswordInputField;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;

/**
 * A text box allowing entry of passwords corresponding to a {@link PluginParameter} of
 * {@link au.gov.asd.tac.constellation.plugins.parameters.types.PasswordParameterType}.
 * <p>
 * Editing the value in the text box will display with "*" and set the value for the
 * underlying {@link PluginParameter}.
 *
 * @author Auriga2
 */
public class PasswordInputPane extends ParameterInputPane<PasswordParameterValue, String> {

    public static final int DEFAULT_WIDTH = 300;
    public static final int INTEGER_WIDTH = 75;

    private static final Logger LOGGER = Logger.getLogger(PasswordInputPane.class.getName());

    public PasswordInputPane(final PluginParameter<PasswordParameterValue> parameter) {
        super(new PasswordInputField(), parameter);
        final PasswordParameterValue pv = parameter.getParameterValue();
        setFieldValue(pv.get());
    }

    @Override
    public ConstellationInputFieldListener getFieldChangeListener(PluginParameter parameter) {
        return (ConstellationInputFieldListener<String>) (String newValue) -> {
            if (newValue != null) {
                parameter.setStringValue(field.getText());
            }
        };
    }

    @Override
    public PluginParameterListener getPluginParameterListener() {
        return (PluginParameter<?> pluginParameter, ParameterChange change) -> Platform.runLater(() -> {
            switch (change) {
                case VALUE -> {
                    // Don't change the value if it isn't necessary.
                    // Setting the text changes the cursor position, which makes it look like text is
                    // being entered right-to-left.
                    final String param = pluginParameter.getStringValue();
                    if (!field.getText().equals(param)) {
                        field.setText(param != null ? param : "");
                    }
                }
                case ENABLED -> updateFieldEnablement();
                case VISIBLE -> updateFieldVisability();
                default -> LOGGER.log(Level.FINE, "ignoring parameter change type {0}.", change);
            }
        });
    }
}

