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
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameterListener;
import au.gov.asd.tac.constellation.plugins.parameters.types.ParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.utilities.gui.field.ChoiceInputField;
import au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputFieldConstants.ChoiceType;
import au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputFieldListener;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;

/**
 * A drop-down combo box which is the GUI element corresponding to a
 * {@link PluginParameter} of
 * {@link au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType}.
 * <p>
 * Selecting an item from the drop-down will set the choice data for the
 * underlying {@link PluginParameter}.
 *
 * @see
 * au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType
 *
 * @author ruby_crucis
 * @author capricornunicorn123
 */
public final class SingleChoiceInputPane extends ParameterInputPane<SingleChoiceParameterValue, List<ParameterValue>> {

    private static final Logger LOGGER = Logger.getLogger(SingleChoiceInputPane.class.getName());

    public SingleChoiceInputPane(final PluginParameter<SingleChoiceParameterValue> parameter) {
        super(new ChoiceInputField<ParameterValue>(ChoiceType.SINGLE_DROPDOWN), parameter);
        
        final SingleChoiceParameterType.SingleChoiceParameterValue pv = parameter.getParameterValue();

        ((ChoiceInputField) field).setOptions(pv.getOptionsData());
        ((ChoiceInputField) field).setIcons(pv.getIcons()); 
        setFieldValue(Arrays.asList(pv.getChoiceData()));
    }

    @Override
    public ConstellationInputFieldListener getFieldChangeListener(PluginParameter<SingleChoiceParameterValue> parameter) {
        return (ConstellationInputFieldListener<List<ParameterValue>>) (List<ParameterValue> newValue) -> {
            if (newValue != null && newValue.size() == 1) {
                SingleChoiceParameterType.setChoiceData(parameter, newValue.getFirst());
            }
        };
    }

    @Override
    public PluginParameterListener getPluginParameterListener() {
        return (PluginParameter<?> parameter, ParameterChange change) -> Platform.runLater(() -> {
            
            if (parameter.getParameterValue() instanceof SingleChoiceParameterValue scParameterValue){
                switch (change) {
                    
                    case VALUE -> {
                        // Don't change the value if it isn't necessary.
                        List<ParameterValue> selection = (List<ParameterValue>) getFieldValue();
                        if (selection != null && selection.size() == 1 && !selection.getFirst().equals(scParameterValue.getChoiceData())){
                            setFieldValue(selection);
                        }
                    }
                    
                    case PROPERTY -> {

                        // Update the Pane if the Optons have changed
                        List<ParameterValue> paramOptions = scParameterValue.getOptionsData();
                        if (!((ChoiceInputField) field).getOptions().equals(paramOptions)){
                            ((ChoiceInputField) field).setOptions(paramOptions);

                            // Only keep the value if it's in the new choices.
                            if (paramOptions.contains(scParameterValue.getChoiceData())) {
                                setFieldValue(Arrays.asList(scParameterValue.getChoiceData()));
                            } else {
                                setFieldValue(null);
                            }
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
