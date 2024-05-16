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
 * An abstract base class for PluginParameter Input Panes in Constellation.
 * This class and it's extended classes acts as the link between {@link PluignParameter} objects
 * and {@link ConstellationInputField} objects.
 * 
 * This class provides standard functionality for this link and defines some implementation specific 
 * methods to assist in the nuanced interactions across different parameter types.
 * 
 * Visually, extensions of this class are represented by a {@link HBox} which contains a 
 * {@link ConstellationInputField}. This {@link HBox} is typically added to a {@link GridPane} by
 * the {@link PluginParametersPane}.
 * 
 * Examples of {@link PluginInputPane} can be found in the DataAccessView or the SphereGraphBuilder.
 * 
 * @author capricornunicorn123
 * @param <T>
 */
public abstract class ParameterInputPane<T extends ParameterValue> extends HBox {
    public final ConstellationInputField field;
    public final PluginParameter<T> parameter;
    
    ParameterInputPane(ConstellationInputField field, PluginParameter<T> parameter){
        this.field = field;
        this.parameter = parameter;
        
        updateFieldEnablement();
        updateFieldVisability();

        this.field.setPromptText(parameter.getDescription());
        
        this.field.addListener(getFieldChangeListener(parameter));
        parameter.addListener(getPluginParameterListener());
        
        getChildren().add(field);
    }
    
    public final ConstellationInputField getField(){
        return this.field;
    }
    
    public boolean fieldValid(){
        return true;
    }
    
    public void setFieldValue(Object value){
        this.field.setValue(value);
    }
    
    public Object getFieldValue(){
        return this.field.getValue();
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
     * A ChangeListener that can modify the relevant PluginParameter when a change on the InputField is found
     * Note: ConstellationInputFields will only notify ChangeListeners if the input field's TextProperty has 
     * changed and the input field has a valid value as defined by the input field.
     * it is the responsibility of implementations of this method to ensure that the input field value is
     * valid with respect to the input field.
     * @param parameter
     * @return A ChangeListener that can be registered to a ConstelationInputField
     */
    public abstract ChangeListener getFieldChangeListener(PluginParameter<T> parameter);
    
    /**
     * A PluginParameterlistener that can modify the relevant InputField when a change on the Parameter is found
     * Note: Ensure that implementation fo this method only modify the input field if the value of the 
     * input field and the value of the Plugin parameter differ. Failing to do so could cause the listener 
     * implementations to call each other cyclically. 
     * @return A PluginParameterListener that can be registered to a PluginParameter
     */
    public abstract PluginParameterListener getPluginParameterListener();
    
}
