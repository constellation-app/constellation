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
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType.MultiChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.ParameterValue;
import au.gov.asd.tac.constellation.utilities.gui.field.ChoiceInputField;
import au.gov.asd.tac.constellation.utilities.gui.field.ChoiceInputField.ChoiceType;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * A drop-down combo box allowing multiple selections, which is the GUI element
 * corresponding to a {@link PluginParameter} of
 * {@link au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType}.
 * <p>
 * Changing the selected items in the drop down will set the checked data for
 * the underlying {@link PluginParameter}.
 *
 * @see
 * au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType
 *
 * @author twinkle2_little
 * @author capricornunicorn123
 */
public final class MultiChoiceInputPane extends ParameterInputPane<MultiChoiceParameterValue> {

    private static final Logger LOGGER = Logger.getLogger(MultiChoiceInputPane.class.getName());
    private final ObservableList<ParameterValue> options = FXCollections.observableArrayList();

    
    public MultiChoiceInputPane(final PluginParameter<MultiChoiceParameterValue> parameter) {
        super(new ChoiceInputField<ParameterValue>(ChoiceType.MULTI), parameter);
        final MultiChoiceParameterValue pv = parameter.getParameterValue();
        options.addAll(pv.getOptionsData());

        ((ChoiceInputField) field).setOptions(options);
        setFieldValue(pv.getChoicesData());

    }

    @Override
    public ChangeListener getFieldChangeListener(PluginParameter<MultiChoiceParameterValue> parameter) {
        return (ChangeListener<List<ParameterValue>>) (ObservableValue<? extends List<ParameterValue>> observable, List<ParameterValue> oldValue, List<ParameterValue> newValue) -> {
            if (newValue != null) {
                MultiChoiceParameterType.setChoicesData(parameter, newValue);
            }
        };
    }

    @Override
    public PluginParameterListener getPluginParameterListener() {
        return (PluginParameter<?> parameter, ParameterChange change) -> Platform.runLater(() -> {
            @SuppressWarnings("unchecked") //mcPluginParameter is a MultiChoiceParameter
            final PluginParameter<MultiChoiceParameterValue> mcPluginParameter = (PluginParameter<MultiChoiceParameterValue>) parameter;
            switch (change) {
                
                case VALUE -> {
                    // Don't change the value if it isn't necessary.
                    List<ParameterValue> selection = (List<ParameterValue>) getFieldValue();
                    if (!selection.equals(MultiChoiceParameterType.getChoicesData(mcPluginParameter))){
                        setFieldValue(selection);
                    }
                }
                
                case PROPERTY -> {
                    
                    // Update the Pane if the Optons have changed
                    List<ParameterValue> paramOptions = MultiChoiceParameterType.getOptionsData(mcPluginParameter);
                    if (!((ChoiceInputField) field).getOptions().equals(paramOptions)){
                        ((ChoiceInputField) field).setOptions(paramOptions);
                        
                        // Only keep the value if it's in the new choices.
                        if (paramOptions.contains(MultiChoiceParameterType.getChoicesData(mcPluginParameter))) {
                            setFieldValue(MultiChoiceParameterType.getChoicesData(mcPluginParameter));
                        } else {
                            setFieldValue(null);
                        }
                    }
                }
                
                case ENABLED -> updateFieldEnablement();
                case VISIBLE -> updateFieldVisability();
                default -> LOGGER.log(Level.FINE, "ignoring parameter change type {0}.", change);
            }
        });
    }
}
