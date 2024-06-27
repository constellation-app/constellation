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
package au.gov.asd.tac.constellation.utilities.gui.field;

import au.gov.asd.tac.constellation.utilities.gui.field.Button.ButtonType;
import au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputFieldConstants.ChoiceType;
import au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputFieldConstants.LayoutConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

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
 * @param <C> The Object type being represented by this ChoiceInputFiled
 */
public final class NumberInputField<C extends Number> extends ConstellationInputField<Number> implements ButtonLeft, ButtonRight{
    
    private final C min;
    private final C max;
    private final C init;
    private final C step;
    private static final Logger LOGGER = Logger.getLogger(NumberInputField.class.getName());
                    
    
    public NumberInputField(Number min, Number max, Number init, Number step) {
        super(LayoutConstants.UPDATER_INPUT_UPDATER);
        this.min = (C) min;
        this.max = (C) max;
        this.init = (C) init;
        this.step = (C) step;
        
        this.setValue(init);

        this.addShortcuts(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()){
                case UP -> this.increment();
                case DOWN -> this.decrement();
            }
        });
    }
    
    // <editor-fold defaultstate="collapsed" desc="Local Private Methods">   

    /**
     * Used increment a selected a number.
     */
    private void increment() {
        Number value = this.getValue();
        if (value != null) {
            float current = value.floatValue();
            float step = getStep().floatValue();

            float desired = current + step;
            if (desired > getMax().floatValue()){
                this.setValue(getMax());
            } else {
                this.setValue(desired);
            }
        } else {
            this.setValue(getMax());
        }
       
    }
    
    /**
     * Used in single choice Options to decrement a selected choice.
     * If the choice is the first choice in the list of options the previous choice is the last option.
     */
    private void decrement() {
        Number value = this.getValue();
        if (value != null){
            float current = value.floatValue();
            float step = getStep().floatValue();

            float desired = current - step;
            if (desired < getMin().floatValue()){
                this.setValue(getMin());
            } else {
                this.setValue(desired);
            }
        } else {
            this.setValue(getMin());
        }
    }
    
    private Number getMax(){
        
        if (max == null){
            switch (init) {
                case Integer integerValue -> {
                    return Integer.MAX_VALUE;
                }
                case Float floatvalue -> {
                    return Float.MAX_VALUE;
                }
                case Double floatvalue -> {
                    return Float.MAX_VALUE;
                }
                default ->{
                    throw new IllegalArgumentException(String.format("Unsupported type %s found.", max.getClass()));
                }
            }
        }
        return max;
    }
    
    private Number getMin(){
        
        if (min == null){
            switch (init) {
                case Integer integerValue -> {
                    return Integer.MIN_VALUE;
                }
                case Float floatvalue -> {
                    return Float.MIN_VALUE;
                }
                default ->{
                    throw new IllegalArgumentException(String.format("Unsupported type %s found.", min.getClass()));
                }
            }
        }
        return min;
    }
    
    private Number getStep(){
        if (step == null) {
            return getMax().floatValue() - getMin().floatValue();
        }
        return step;
    }
    
    private void setNumber(Number value){
        switch (init) {
            case Integer integerValue -> this.setText(Integer.toString(value.intValue()));
            case Float floatvalue -> this.setText(Float.toString(value.floatValue()));
            default -> {
                //do nothing
            }
        };   
    }
    
    private Number getNumber() throws NumberFormatException{
        String text = this.getText();
        if (!text.isBlank()){
            switch (init) {
                case Integer integerValue -> {
                    return Integer.parseInt(text);
                }
                case Float floatValue -> {
                    return Float.parseFloat(text);
                }
                default -> {
                    throw new UnsupportedOperationException(String.format("Numbers of type %s are not supported", init.getClass()));
                }
            } 
        }
        return null;
    }
    
    // </editor-fold> 
        
    // <editor-fold defaultstate="collapsed" desc="Value Modification & Validation Implementation"> 
    @Override
    public Number getValue() {
        try{
            return getNumber();
        } catch(NumberFormatException ex){
            return null;
        }
    }
    
    @Override
    public void setValue(final Number value) {
        this.setNumber(value);
    }
    
    @Override
    public boolean isValid() {
        try{
            Number value = this.getNumber();

            if (value == null){
                return true;
            } else {        
                return value.floatValue() >= getMin().floatValue() && value.floatValue() <= getMax().floatValue();
            }
        } catch (NumberFormatException ex){
            return false;
        }
    }  
    // </editor-fold> 

    // <editor-fold defaultstate="collapsed" desc="ContextMenuContributor Implementation"> 
    @Override
    public List<MenuItem> getLocalMenuItems() {
        final List<MenuItem> items = new ArrayList();

        final MenuItem next = new MenuItem("Increment");
        next.setOnAction(value -> executeRightButtonAction());
        items.add(next);

        final MenuItem prev = new MenuItem("Decrement");
        prev.setOnAction(value -> executeLeftButtonAction());
        items.add(prev);
            
        return items;
    }
    // </editor-fold> 

    // <editor-fold defaultstate="collapsed" desc="Button Event Implementation">   
    @Override
    public Button getLeftButton() {
        return new Button(new Label(ConstellationInputFieldConstants.PREVIOUS_BUTTON_LABEL), ButtonType.CHANGER) {
            @Override
            public EventHandler<? super MouseEvent> action() {
                return event -> executeLeftButtonAction();
            }
        };
    }

    @Override
    public Button getRightButton() {
        return new Button(new Label(ConstellationInputFieldConstants.NEXT_BUTTON_LABEL), ButtonType.CHANGER) {
            @Override
            public EventHandler<? super MouseEvent> action() {
                return event -> executeRightButtonAction();
            }
        };
    }
    
    @Override
    public void executeLeftButtonAction() {
        decrement();
    }
    
    @Override
    public void executeRightButtonAction() {
        this.increment();
    }
    // </editor-fold> 
    
    // <editor-fold defaultstate="collapsed" desc="Drop Down Implementation">   
    @Override
    public ContextMenu getDropDown() {
        throw new UnsupportedOperationException("NumberInputField does not provide a ContextMenu");
    } 
    // </editor-fold> 
}