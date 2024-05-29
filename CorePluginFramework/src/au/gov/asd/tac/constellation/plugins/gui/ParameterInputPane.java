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
import au.gov.asd.tac.constellation.plugins.parameters.types.ParameterValue;
import au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputField;
import javafx.beans.value.ChangeListener;
import javafx.scene.layout.HBox;

/**
 * An abstract base class for Panes that get user input into editing {@link PluginParameter} objects in Constellation.
 * This class and it's extended classes act as the link between {@link PluignParameter} objects
 * and {@link ConstellationInputField} objects.
 * 
 * This class provides standard functionality for this link and defines some implementation specific 
 * methods to assist in the nuanced interactions across different {@link PluginParameterType} implementations.
 * 
 * Visually, extensions of this class are represented by a {@link HBox} which contains a 
 * {@link ConstellationInputField}. This {@link HBox} is typically added to a {@link GridPane} by
 * the {@link PluginParametersPane}.
 * 
 * Visual examples of the {@link ParameterInputPane} can be found in the DataAccessView or the SphereGraphBuilder.
 * 
 * @author capricornunicorn123
 * @param <T> the Value represented by the {@link PluginParameter}
 * @param <V> the Value represented by the {@link ConstellationInputField}
 */
public abstract class ParameterInputPane<T extends ParameterValue, V extends Object> extends HBox {
    
    public final ConstellationInputField<V> field;
    public final PluginParameter<T> parameter;
    
    ParameterInputPane(ConstellationInputField<V> field, PluginParameter<T> parameter){
        this.field = field;
        this.parameter = parameter;
        
        updateFieldEnablement();
        updateFieldVisability();

        this.field.setPromptText(parameter.getDescription());
        field.enableSpellCheck(parameter.isSpellCheckEnabled());
        
        this.field.addListener(getFieldChangeListener(parameter));
        parameter.addListener(getPluginParameterListener());
        
        getChildren().add(field);
    }
    
    public final ConstellationInputField<V> getField(){
        return this.field;
    }
    
    public boolean fieldValid(){
        return true;
    }
    
    public void setFieldValue(V value){
        this.field.setValue(value);
    }
    
    public V getFieldValue(){
        return this.field.getValue();
    }
    
    public void setFieldLines(int lineCount){
        field.setLines(lineCount);
    }
    
    public final void updateFieldVisability(){
            field.setManaged(parameter.isVisible());
            field.setVisible(parameter.isVisible());
            this.setVisible(parameter.isVisible());
            this.setManaged(parameter.isVisible());
    }
    
    public final void updateFieldEnablement(){
            field.setDisable(!parameter.isEnabled());
    }
    
    /**
     * A ChangeListener that can modify the relevant {@link PluginParameter} when a change on the {@link ConstellationInputField} is found
     * Note: {@link ConstellationInputField} will only notify ChangeListeners if the input field's TextProperty has 
     * changed and the {@link ConstellationInputField} has a valid value as defined by the {@link ConstellationInputField}.
     * It is the responsibility of implementations of this method to ensure that the input field value is
     * valid with respect to the {@link PluginParameter}.
     * <p> Example Implementation: (T is the generic type that is supported by the respective {@link ConstellationInputField})
     * <pre>
     * return (ChangeListener<T>) (ObservableValue<? extends T> observable, T oldValue, T newValue) -> {
            if (newValue != null) {
                // manipulate the local parameter reference accordingly
            }
        };
     * </pre>
     * </p>
     * @param parameter
     * @return A ChangeListener that can be registered to a ConstelationInputField
     */
    public abstract ChangeListener getFieldChangeListener(final PluginParameter<T> parameter);
    
    /**
     * A {@link PluginParameterListener} that can modify the relevant {@link ConstellationInputField} when a change on 
     * the {@link PluginParameter} is found.
     * Note: Ensure that implementations of this method only modify the {@link ConstellationInputField} if the value of the 
     * {@link ConstellationInputField} and the value of the {@link PluginParameter} differ. Failing to do so could cause the listener 
     * implementations to call each other cyclically. 
     * <p> Example Implementation: (T is the {@link ParameterValue} for this {@link PluginParameter})
     * <pre>
     * return (PluginParameter<?> parameter, ParameterChange change) -> Platform.runLater(() -> {
     *      //@SuppressWarnings("unchecked")
     *      final PluginParameter<T> p = (PluginParameter<T>) parameter;
     *      switch (change) {
     *          case VALUE -> {
     *              // Don't change the value if it isn't necessary
     *              // manipulate the InputField reference accordingly
     *          }
     *          case PROPERTY -> {
     *              
     *              // Update the field if the something other than the ParameterValue has changed.
     *              //Could be options, colors, filetypes etc
     *          }
     *          case ENABLED -> updateFieldEnablement();
     *          case VISIBLE -> updateFieldVisability();
     *          default -> LOGGER.log(Level.FINE, "ignoring parameter change type {0}.", change);
     *      }
     *  });
     * </pre>
     * </p>
     * @return A PluginParameterListener that can be registered to a PluginParameter
     */
    public abstract PluginParameterListener getPluginParameterListener();
    
}
