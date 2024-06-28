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
package au.gov.asd.tac.constellation.utilities.gui.field.framework;

import au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInput;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.image.ImageView;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;

/**
 * A {@link ConstellationinputField} for managing choice selection. 
 * 
 * This input has two main mechanisms for managing choice selection:
 * - a list of Options (the list of available options to be selected from)
 * - a list of choices (a sub-list representing options that the user has selected)
 * 
 * This field implements {@link ListChangeListener} enabling this input field to 
 * update its text area when the choices list is changed. 
 * 
 * look into the case where the options list changes but existing valid choices were present but are no longer valid after the change.
 * 
 * @author capricornunicorn123
 * @param <C> The Object type being returned by this ChoiceInputFiled
 * @param <O> The Object type being represnted by this ChoiceINputField.
 */
public abstract class ChoiceInputField<C extends Object, O extends Object> extends ConstellationInput<C> {
    
    private final List<O> options;
    private final ObservableList<O> observabeOptions;
    protected final List<ImageView> icons = new ArrayList<>();
  
    public ChoiceInputField(){
        this.options = new ArrayList();
        observabeOptions = null;
    }
    
    public ChoiceInputField(List<O> options){
        if (options instanceof ObservableList observable){
            observabeOptions = observable;
            this.options = null;
        } else {
            this.options = options;
            observabeOptions = null;
        }
    }
    // <editor-fold defaultstate="collapsed" desc="Local Private Methods">   
    
    /**
     * Defines the options that users can select from in this field.
     * Any previously defined options will be overwritten with this new list.
     * @param options 
     */
    public final void setOptions(final List<O> options){
        if (this.options != null){
            this.options.clear();
            this.options.addAll(options);
        } else {
            throw new InvalidOperationException("Attempting to Set Options when this ChoiceInputField is using an observableList of options");
        }
    }
    
    /**
     * Retrieves the options that users can select from in this field.
     * @return List of Options 
     */
    public final List<O> getOptions(){
        if (this.options == null){
            return this.observabeOptions;
        } else {
            return this.options;
        }
    }
    
    /**
     * Defines the list of icons for the context menu
     * @param icons
     */
    public final void setIcons(final List<ImageView> icons) {
        this.icons.clear();
        this.icons.addAll(icons);
    }
    
    /**
     * Removes all choices.
     */
    protected final void clearChoices() {
       this.setText("");
    }
    // </editor-fold> 
       
}
