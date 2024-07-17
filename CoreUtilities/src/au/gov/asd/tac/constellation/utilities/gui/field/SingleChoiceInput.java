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

import au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputConstants.ChoiceType;
import static au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputConstants.ChoiceType.SINGLE_DROPDOWN;
import static au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputConstants.ChoiceType.SINGLE_SPINNER;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.Button.ButtonType;
import java.util.ArrayList;
import java.util.List;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.AutoCompleteSupport;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.ChoiceInputField;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.ConstellationInputDropDown;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.LeftButtonSupport;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.RightButtonSupport;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.ShortcutSupport;

/**
 * A {@link ChoiceInput} for managing single choice selection. 
 * This input provides the following {@link ConstellationInput} support features
 * <ul>
 * <li>{@link RightButtonSupport} - Increments the choice in spinners and triggers a drop down menu to select a choice from the list of options.</li>
 * <li>{@link LeftButtonSupport} - Only used in Spinner inputs to decrement the choice.</li>
 * <li>{@link ShortcutSupport} - Increments and decrements the data chronologically with up and down arrow.</li>
 * <li>{@link AutoCompleteSupport} - Provides a list of colors with a name that matches the text in the input field.</li>
 * </ul>
 * See referenced classes and interfaces for further details on inherited and implemented features.
 * @param <C> The type of object represented by this input.
 * 
 * @author capricornunicorn123
 */
public final class SingleChoiceInput<C extends Object> extends ChoiceInputField<C, C> implements RightButtonSupport, LeftButtonSupport, AutoCompleteSupport, ShortcutSupport{
    
    private final ChoiceType type;

    public SingleChoiceInput(final ChoiceType type){    
        this.type = type;
        initialiseDepedantComponents();
    }    
    
    // <editor-fold defaultstate="collapsed" desc="Shortcut Support">   
    @Override
    public EventHandler<KeyEvent> getShortcuts() {
        //Add shortcuts where users can increment and decrement the date using up and down arrows
        return (event) -> {
            switch (event.getCode()){
                case UP -> {
                    this.decrementChoice();
                    event.consume();
                }
                case DOWN -> {
                    this.incrementChoice();
                    event.consume();
                }
            }
        };
    }
    // </editor-fold>
  
    // <editor-fold defaultstate="collapsed" desc="Local Private Methods">    
    
    public C getChoice(){
        final List<C> matches = getOptions().stream().filter(choice -> choice.toString().equals(getText())).toList();
        if (matches.isEmpty()){
            return null;
        } else {
            return matches.getFirst();
        }
    }
    /**
     * Changes the List of selected Choices to ensure the provided choice is included.
     * if single choice selection mode then the old choice is removed
     * if multi choice the old choice is retained
     * @param choice 
     */
    public void setChoice(final C choice){
        if (choice != null && this.getOptions().contains(choice)) {
            this.setText(choice.toString());
        }
    }
    
    /**
     * Removes the provided choice from the currently selected choices.
     * @param choice 
     */
    public void removeChoice(final C choice){
        if (getChoice() == choice){
            clearChoices();
        }
    }

    /**
     * Used in single choice Options to increment a selected choice.
     * If the choice is the last choice in the list of options the next choice is the first option.
     */
    private void incrementChoice() {
        final C selection = this.getChoice();
        if (selection != null){
            final int nextSelectionIndex = this.getOptions().indexOf(selection) + 1;
            if (nextSelectionIndex < this.getOptions().size()){
                this.setChoice(this.getOptions().get(nextSelectionIndex));
            } else {
                this.setChoice(this.getOptions().getFirst());
            }
        } else {
            this.setChoice(this.getOptions().getLast());
        }
    }
    
    /**
     * Used in single choice Options to decrement a selected choice.
     * If the choice is the first choice in the list of options the previous choice is the last option.
     */
    private void decrementChoice() {
        final C selection = this.getChoice();
        if (selection != null){
            final int prevSelectionIndex = this.getOptions().indexOf(selection) - 1;
            if (prevSelectionIndex < 0){
                this.setChoice(this.getOptions().getLast());
            } else {
                this.setChoice(this.getOptions().get(prevSelectionIndex));
            }
        } else {
            this.setChoice(this.getOptions().getFirst());
        }
    }
    // </editor-fold> 
        
    // <editor-fold defaultstate="collapsed" desc="Value Modification & Validation Implementation"> 
    @Override
    public C getValue() {
        return getChoice();
    }
    
    @Override
    public void setValue(final C value) {
        this.setChoice(value);
    }
    
    @Override
    public boolean isValidContent() {
        if (getText().isBlank()){
            return true;
        } else {
            return getChoice() != null;
        }
    }  
    // </editor-fold> 

    // <editor-fold defaultstate="collapsed" desc="ContextMenuContributor Implementation"> 
    @Override
    public List<MenuItem> getLocalMenuItems() {
        final List<MenuItem> items = new ArrayList();
        if (type != null){
            switch (type){
                case SINGLE_SPINNER -> {
                    final MenuItem next = new MenuItem("Increment");
                    next.setOnAction(value -> executeRightButtonAction());
                    items.add(next);

                    final MenuItem prev = new MenuItem("Decrement");
                    prev.setOnAction(value -> executeLeftButtonAction());
                    items.add(prev);
                }
            }   
            final MenuItem choose = new MenuItem("Select Choice");
            choose.setOnAction(value -> executeRightButtonAction());
            items.add(choose);   
        }
        return items;
    }
    // </editor-fold> 

    // <editor-fold defaultstate="collapsed" desc="Button Event Implementation">   
    @Override
    public LeftButton getLeftButton() {
        switch (type) {
            case SINGLE_SPINNER -> {
                return new LeftButton(new Label(ConstellationInputConstants.PREVIOUS_BUTTON_LABEL), ButtonType.CHANGER) {
                    @Override
                    public EventHandler<? super MouseEvent> action() {
                        return event -> executeLeftButtonAction();
                    }
                };
            }
            default -> {
                return null;
            }
        }
    }
    
    @Override
    public RightButton getRightButton() {
        
        final Label label;
        final ButtonType buttonType;
        
        switch (type) {
            case SINGLE_SPINNER -> {
                label = new Label(ConstellationInputConstants.NEXT_BUTTON_LABEL);
                buttonType = ButtonType.CHANGER;
            }
            case SINGLE_DROPDOWN -> {
                label = new Label(ConstellationInputConstants.SELECT_BUTTON_LABEL);
                buttonType = ButtonType.DROPDOWN;
                
            }
            default -> {
                return null;
            }
        }
        
        return new RightButton(label, buttonType) {
                @Override
                public EventHandler<? super MouseEvent> action() {
                    return event -> executeRightButtonAction();
                }
        };
    }

    @Override
    public void executeLeftButtonAction() {
        if (type == SINGLE_SPINNER) {
            decrementChoice(); 
        }
    }
    
    @Override
    public void executeRightButtonAction() {
        switch (type){
            case SINGLE_SPINNER -> this.incrementChoice();
            case SINGLE_DROPDOWN -> this.showDropDown(new ChoiceInputDropDown(this));
        }
    }  
    // </editor-fold> 
    
    // <editor-fold defaultstate="collapsed" desc="Drop Down Implementation">   
    /**
     * A Context Menu to be used as a drop down for {@link ChoiceInputFields}.
     * 
     * This Drop down has two unique characteristics ChoiceType.MULTI ChoiceInputField
     * 1. it will have bulk selection options of ... 
     * - Select All; and
     * - Clear All.
     * ... followed by a separator.
     * 2. choices will be displayed a s checkable boxes with the context menu remaining open after each selection.
     */
    private class ChoiceInputDropDown extends ConstellationInputDropDown {
        
        //A local reference to the check boxes displayed in Multi Choice context menu's
        final List<CheckBox> boxes = new ArrayList<>();
        
        public ChoiceInputDropDown(final SingleChoiceInput field){
            super(field);
            
            if (getOptions() != null){
                final Object[] optionsList = getOptions().toArray();
                for (int i = 0 ; i < optionsList.length ; i ++){
                    final C choice = (C) optionsList[i];

                    final Labeled item = switch(field.type){
                        case SINGLE_DROPDOWN -> {
                            final Label label = new Label(choice.toString());
                            label.setOnMouseClicked(event -> {
                                    field.setChoice(choice);
                            }); 
                            yield label;
                        }
                        //shouldnt reach here
                        case SINGLE_SPINNER -> new Label();
                    };

                    if (!icons.isEmpty()){
                        item.setGraphic(icons.get(i));
                    }

                    this.registerCustomMenuItem(item);   
                }
            }
            
            final ConstellationInputListener<List<C>> cl = (final List<C> newValue) -> {
                if (newValue != null) {
                    final List<String> stringrep = newValue.stream().map(Object::toString).toList();
                    for (CheckBox box : boxes){
                        box.setSelected(stringrep.contains(box.getText())); 
                    }
                }
            };
            
            //Register the Context Menu as a listener whilst it is open incase choices are modified externaly.
            this.setOnShowing(value -> {
                field.addListener(cl);
            });
            
            //This context menu may be superseeded by a new context menu so deregister it when hidden.
            this.setOnHiding(value -> {
                field.removeListener(cl);
            });
        }
    }
    // </editor-fold> 
    
    // <editor-fold defaultstate="collapsed" desc="Auto Complete Implementation"> 
    @Override
    public List<MenuItem> getAutoCompleteSuggestions() {
        final List<MenuItem> suggestions = new ArrayList<>();
        //do not show suggestions in the following cases
        //if there are two unknown choices that the user has enteres, i.e more than 1 null value.
        //if there is 1 or more valid choices in the event that this is a single choice input.
        if (this.getChoice() == null){
            //Remove blank entrys from here
            this.getOptions()
                    .stream()
                    .map(value -> value)
                    .filter(value -> value.toString().toUpperCase().contains(getText().toUpperCase()))
                    .forEach(value -> {
                        final MenuItem item = new MenuItem(value.toString());
                        item.setOnAction(event -> {
                            this.setChoice(value);
                        });
                        suggestions.add(item);
                    });    
        }
        return suggestions;
    }
    // </editor-fold> 
}
