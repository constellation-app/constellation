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
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputFieldConstants.TextType;
import au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputFieldListener;
import au.gov.asd.tac.constellation.utilities.gui.field.TextInputField;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;

/**
 * A text box allowing entry of single line text, multiple line text
 * corresponding to a {@link PluginParameter} of
 * {@link au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType}.
 * <p>
 * Editing the value in the text box will set the string value for the
 * underlying {@link PluginParameter}.
 *
 * @see
 * au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType
 *
 * check the is label, i think this feature is just used when a parameter value is uneditable and shown as plan text label, why does this need to be a label....
 * @author ruby_crucis
 */
public class ValueInputPane extends ParameterInputPane<StringParameterValue, String> {

    private static final Logger LOGGER = Logger.getLogger(ValueInputPane.class.getName());

    public ValueInputPane(final PluginParameter<StringParameterValue> parameter) {
        this(parameter, 1);
    }

    /**
     * Primary constructor
     *
     * @param parameter parameter to link to value
     * @param suggestedHeight suggested hight (in lines)
     */
    public ValueInputPane(final PluginParameter<StringParameterValue> parameter, Integer suggestedHeight) {
        super(suggestedHeight == null || suggestedHeight <= 1 ? new TextInputField(TextType.SINGLELINE, parameter.getId()) : new TextInputField(TextType.MULTILINE, parameter.getId()), parameter);
            StringParameterValue pv = (StringParameterValue) parameter.getParameterValue();
            if (parameter.getObjectValue() != null) {
                this.setFieldValue(pv.get());
            }
            
            if (suggestedHeight != null && suggestedHeight > 1){
                this.setFieldHeight(suggestedHeight);
            }
        }

    @Override
    public ConstellationInputFieldListener getFieldChangeListener(PluginParameter<StringParameterValue> parameter) {
        return (ConstellationInputFieldListener<String>) (String newValue) -> {
            if (newValue != null) {
                parameter.setStringValue(getFieldValue());
            }
        };
    }
    
    @Override
    public PluginParameterListener getPluginParameterListener() {
       return (PluginParameter<?> parameter, ParameterChange change) -> Platform.runLater(() -> {
            if (parameter.getParameterValue() instanceof StringParameterValue pv){
                switch (change) {
                    case VALUE -> {
                        // Don't change the value if it isn't necessary.
                        if (!pv.get().equals(getFieldValue())){
                            setFieldValue(pv.get());
                        }
                    }
                    case ENABLED -> updateFieldEnablement();
                    case VISIBLE -> updateFieldVisability();
                    default -> LOGGER.log(Level.FINE, "ignoring parameter change type {0}.", change);
                }
            }
        });
    }
}