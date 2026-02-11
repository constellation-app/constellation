/*
 * Copyright 2010-2025 Australian Signals Directorate
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
import static au.gov.asd.tac.constellation.plugins.parameters.ParameterChange.PROPERTY;
import static au.gov.asd.tac.constellation.plugins.parameters.ParameterChange.VALUE;
import static au.gov.asd.tac.constellation.plugins.parameters.ParameterChange.VISIBLE;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameterListener;
import au.gov.asd.tac.constellation.plugins.parameters.types.ParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.utilities.gui.field.SingleChoiceInput;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.ConstellationInputConstants.ChoiceType;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.ConstellationInputListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A drop-down combo box which is the GUI element corresponding to a {@link PluginParameter} of
 * {@link au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType}.
 * <p>
 * Selecting an item from the drop-down will set the choice data for the underlying {@link PluginParameter}.
 *
 * @see au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType
 *
 * @author ruby_crucis
 */
public class SingleChoiceInputPane extends ParameterInputPane<SingleChoiceParameterValue, ParameterValue> {

    private static final Logger LOGGER = Logger.getLogger(SingleChoiceInputPane.class.getName());

    public SingleChoiceInputPane(final PluginParameter<SingleChoiceParameterValue> parameter) {
        super(new SingleChoiceInput<ParameterValue>(ChoiceType.SINGLE_DROPDOWN), parameter);
        final SingleChoiceParameterType.SingleChoiceParameterValue pv = parameter.getParameterValue();
        ((SingleChoiceInput) input).setOptions(pv.getOptionsData());
        ((SingleChoiceInput) input).setIcons(pv.getIcons());
        setFieldValue(pv.getChoiceData());
    }

    @Override
    public ConstellationInputListener getFieldChangeListener(final PluginParameter<SingleChoiceParameterValue> parameter) {
        return (ConstellationInputListener<ParameterValue>) (final ParameterValue newValue) -> {
            if (newValue != null) {
                SingleChoiceParameterType.setChoiceData(parameter, newValue);
            }
        };
    }

    @Override
    public PluginParameterListener getPluginParameterListener() {
        // The listener needs to be assigned and then returned otherwise it doesn't update as intended
        final PluginParameterListener listener = (final PluginParameter<?> parameter, final ParameterChange change) -> {
            final PluginParameter<SingleChoiceParameterValue> scParameterValue = (PluginParameter<SingleChoiceParameterValue>) parameter;
            switch (change) {
                case VALUE -> {
                    final List<ParameterValue> paramOptions = SingleChoiceParameterType.getOptionsData(scParameterValue);
                    ((SingleChoiceInput) input).setOptions(paramOptions);

                    // Only keep the value if its in the new choices
                    if (paramOptions.stream().anyMatch(paramOptions::contains)) {
                        setFieldValue(SingleChoiceParameterType.getChoiceData(scParameterValue));
                    } else {
                        setFieldValue(null);
                    }
                    // Don't change the value if it isn't necessary 
                    final ParameterValue selection = getFieldValue();
                    if (selection != null && !selection.equals(SingleChoiceParameterType.getChoiceData(scParameterValue))) {
                        setFieldValue(selection);
                    }
                }

                case PROPERTY -> {
                    // Update the pane if the options have changed 
                    final List<ParameterValue> paramOptions = (List<ParameterValue>) SingleChoiceParameterType.getChoiceData(scParameterValue);
                    if (paramOptions != null) {
                        ((SingleChoiceInput) input).setOptions(paramOptions);
                        if (paramOptions.contains(SingleChoiceParameterType.getChoiceData(scParameterValue))) {
                            setFieldValue(SingleChoiceParameterType.getChoiceData(scParameterValue));
                        } else {
                            setFieldValue(null);
                        }
                    }
                }
                case ENABLED ->
                    updateFieldEnablement();
                case VISIBLE ->
                    updateFieldVisibility();
                default ->
                    LOGGER.log(Level.FINE, "ignoring parameter change type {0}.", change);
            }
        };
        return listener;
    }
}
