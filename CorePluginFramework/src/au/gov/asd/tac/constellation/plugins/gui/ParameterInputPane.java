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

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameterListener;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.ParameterValue;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.ConstellationInput;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.ConstellationInputListener;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.InputValidator;

/**
 * An abstract base class for {@link ParamerPanes} that get user input through a {@link InputField} 
 * to modify {@link PluginParameter} objects in Constellation.
 * This class and it's extended classes act as the link between {@link PluignParameter} objects
 * and {@link ConstellationInput} objects.
 * 
 * This class provides standard functionality for this link and defines some implementation specific 
 * methods to assist in the nuanced interactions across different {@link PluginParameterType} implementations.
 * 
 * Visually, extensions of this class are represented by a {@link HBox} which contains a 
 * {@link ConstellationInput}. This {@link HBox} is typically added to a {@link GridPane} by
 * the {@link PluginParametersPane}.
 * 
 * Visual examples of the {@link ParameterInputPane} can be found in the DataAccessView or the SphereGraphBuilder.
 * 
 * @author capricornunicorn123
 * @param <T> the Value represented by the {@link PluginParameter}
 * @param <V> the Value represented by the {@link ConstellationInput}
 */
public abstract class ParameterInputPane<T extends ParameterValue, V extends Object> extends HBox {
    
    public final ConstellationInput<V> input;
    public final PluginParameter<T> parameter;
    
    ParameterInputPane(final ConstellationInput<V> input, final PluginParameter<T> parameter){
        
        //Set local references to the PluginParameter and the ConstellationInput
        this.input = input;
        this.parameter = parameter;
        
        //Ensure the input pane is correctly vsible and enabled
        updateFieldEnablement();
        updateFieldVisability();

        //Update the prompt text of the input
        this.input.setPromptText(parameter.getDescription());
        
        // Ensure that the input and the parameter are listening to changes on eachother
        this.input.addListener(getFieldChangeListener(parameter));
        parameter.addListener(getPluginParameterListener());
        
        // Add a validator that uses the parameter validation to validae the value of an input.
        this.input.addValidator((String s) -> {
            return parameter.validateString(s);
        });
        
        getChildren().add(input);
    }
    
    /**
     * Gets the ConstellationInput that this ParameterInputPane uses.
     * Ideally, this method should not have to exist. we want to protect the ConstellationInput from being accessed directly.
     * @return 
     */
    protected final ConstellationInput<V> getInputReference(){
        return this.input;
    }
    
    /**
     * Sets the value of the input that this ParameterInputPane uses.
     * @return 
     */
    public void setFieldValue(final V value){
        this.input.setValue(value);
    }
    
    public V getFieldValue(){
        return this.input.getValue();
    }
    
    public void setFieldHeight(final int lineCount){
        input.setPrefRowCount(lineCount);
    }
    
    public final void updateFieldVisability(){
            input.setManaged(parameter.isVisible());
            this.setManaged(parameter.isVisible());
            
            input.setVisible(parameter.isVisible());
            this.setVisible(parameter.isVisible());
    }
    
    public final void updateFieldEnablement(){
            input.setDisable(!parameter.isEnabled());
    }
    
    /**
     * A {@link ConstellationInputListener} that can modify the relevant {@link PluginParameter} when a change on the {@link ConstellationInput} is found.
     * Note: {@link ConstellationInput} will only notify {@link ConstellationInputFieldListeners} if the input input's TextProperty has 
     * changed and the {@link ConstellationInput} has a valid value as defined by both the {@link ConstellationInput} and by the {@link PluginParameter} 
     * via the {@link InputValidator}.
     * 
     * <p> Example Implementation: (T is the generic type that is supported by the respective {@link ConstellationInput})
     * <pre>
     * return (ChangeListener<T>) (ObservableValue<? extends T> observable, T oldValue, T newValue) -> {
     *      if (newValue != null) {
     *          // manipulate the local parameter reference accordingly
     *      }
     *   };
     * </pre>
     * </p>
     * @param parameter
     * @return A ChangeListener that can be registered to a ConstelationInputField
     */
    public abstract ConstellationInputListener getFieldChangeListener(final PluginParameter<T> parameter);
    
    /**
     * A {@link PluginParameterListener} that can modify the relevant {@link ConstellationInput} when a change on 
     * the {@link PluginParameter} is found.
     * Note: Ensure that implementations of this method only modify the {@link ConstellationInput} if the value of the 
     * {@link ConstellationInput} and the value of the {@link PluginParameter} differ. Failing to do so could cause the listener 
     * implementations to call each other cyclically. 
     * <p> Example Implementation: (T is the {@link ParameterValue} for this {@link PluginParameter})
     * <pre>
       return (PluginParameter<?> parameter, ParameterChange change) -> Platform.runLater(() -> {
            //@SuppressWarnings("unchecked")
            final PluginParameter<T> p = (PluginParameter<T>) parameter;
            switch (change) {
                case VALUE -> {
                    // Don't change the value if it isn't necessary
                    // manipulate the InputField reference accordingly
                }
                case PROPERTY -> {

                    // Update the input if the something other than the ParameterValue has changed.
                    //Could be options, colors, filetypes etc
                }
                case ENABLED -> updateFieldEnablement();
                case VISIBLE -> updateFieldVisability();
                default -> LOGGER.log(Level.FINE, "ignoring parameter change type {0}.", change);
            }
        });
     * </pre>
     * </p>
     * @return A PluginParameterListener that can be registered to a PluginParameter
     */
    public abstract PluginParameterListener getPluginParameterListener();
    
}
