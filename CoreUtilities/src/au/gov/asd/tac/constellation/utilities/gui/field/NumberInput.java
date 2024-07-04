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

import au.gov.asd.tac.constellation.utilities.gui.field.framework.Button.ButtonType;
import java.util.ArrayList;
import java.util.List;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.LeftButtonSupport;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.RightButtonSupport;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.ShortcutSupport;

/**
 * A {@link ConstellationInput} for managing choice selection. 
 * This input provides the following {@link ConstellationInput} support features
 * <ul>
 * <li>{@link RightButtonSupport} - Increments the number by the provided step value or to the highest value.</li>
 * <li>{@link LeftButtonSupport} - Decrements the number by the provided step value or to the lowest value.</li>
 * <li>{@link ShortcutSupport} - Increments and decrements the number chronologically with up and down arrow.</li>
 * </ul>
 * See referenced classes and interfaces for further details on inherited and implemented features.
 * @param <C> The type of Number represented by this input.
 * 
 * @author capricornunicorn123
 */
public final class NumberInput<C extends Number> extends ConstellationInput<Number> implements LeftButtonSupport, RightButtonSupport, ShortcutSupport{
    
    private final C min;
    private final C max;
    private final C init;
    private final C step;
    
    public NumberInput(final Number min, final Number max, final Number init, final Number step) {
        this.min = (C) min;
        this.max = (C) max;
        this.init = (C) init;
        this.step = (C) step;
        
        this.setValue(init);
        initialiseDepedantComponents();
    }
    
    // <editor-fold defaultstate="collapsed" desc="Shortcut Support">   
    @Override
    public EventHandler<KeyEvent> getShortcuts() {
        return (event) -> {
            switch (event.getCode()){
                case UP -> this.increment();
                case DOWN -> this.decrement();
            }
        };
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Local Private Methods">   

    /**
     * Used increment a selected a number.
     */
    private void increment() {
        final Number value = this.getValue();
        if (value != null) {
            final float current = value.floatValue();
            final float step = getStep().floatValue();

            final float desired = current + step;
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
        final Number value = this.getValue();
        if (value != null){
            final float current = value.floatValue();
            final float step = getStep().floatValue();

            final float desired = current - step;
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
    
    /**
     * Gets the step value for this NumberInput.
     * if a step value has not been defined the following generic steps will be returned. 
     * 1 for Integer types
     * 0.1 for Float types.
     * @return 
     */
    private Number getStep(){
        if (step == null) {
            switch(init) {
                case Float floatvalue-> {
                    return 0.1;
                }
                case Integer intValue -> {
                    return 1;
                }
                default -> {
                    //Do Nothing
                }
            }
            return getMax().floatValue() - getMin().floatValue();
        }
        return step;
    }
    
    private void setNumber(final Number value){
        switch (init) {
            case Integer integerValue -> this.setText(Integer.toString(value.intValue()));
            case Float floatvalue -> this.setText(Float.toString(value.floatValue()));
            default -> {
                //do nothing
            }
        };   
    }
    
    private Number getNumber() throws NumberFormatException{
        final String text = this.getText();
        if (!text.isBlank()){
            switch (init) {
                case Integer integerValue -> {
                    return Integer.valueOf(text);
                }
                case Float floatValue -> {
                    return Float.valueOf(text);
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
        try {
            return getNumber();
        } catch(final NumberFormatException ex){
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
            final Number value = this.getNumber();

            if (value == null){
                return true;
            } else {        
                return value.floatValue() >= getMin().floatValue() && value.floatValue() <= getMax().floatValue();
            }
        } catch (final NumberFormatException ex){
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
    public LeftButton getLeftButton() {
        return new LeftButton(new Label(ConstellationInputConstants.PREVIOUS_BUTTON_LABEL), ButtonType.CHANGER) {
            @Override
            public EventHandler<? super MouseEvent> action() {
                return event -> executeLeftButtonAction();
            }
        };
    }

    @Override
    public RightButton getRightButton() {
        return new RightButton(new Label(ConstellationInputConstants.NEXT_BUTTON_LABEL), ButtonType.CHANGER) {
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
}