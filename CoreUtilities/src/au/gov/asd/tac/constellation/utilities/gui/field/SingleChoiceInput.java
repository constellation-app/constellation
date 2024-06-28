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
import au.gov.asd.tac.constellation.utilities.gui.field.framework.Button;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.Button.ButtonType;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.ListChangeListener;
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
    
    private C getChoice(){
        List<C> matches = getOptions().stream().filter(choice -> choice.toString().equals(getText())).toList();
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
    private void setChoice(final C choice){
        final List<C> currentChoices = stringToList(this.getText());
        if (this.getOptions().contains(choice) && ((currentChoices != null && !currentChoices.contains(choice)) || currentChoices == null)) {
            this.setText(choice.toString());
        }
    }
    
    /**
     * Removes the provided choice from the currently selected choices.
     * @param choice 
     */
    private void removeChoice(final C choice){
        if (getChoice() == choice){
            clearChoices();
        }
    }
    
    /**
     * Takes a List of Objects and converts the to a comma delimited string. 
     * The order of this list is not changed and its validity is not checked. 
     * @param set
     * @return 
     */
    private String listToString(final List<C> set) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0 ; i < set.size() ; i++){
            final C option = set.get(i);
                if (i != 0){
                    sb.append(", ");
                }
            sb.append(option.toString());
        }
        return sb.toString();          
    }
    
    /**
     * Takes a comma delimited string and converts it to a list of Objects.
     * Objects will be present in the possible options.
     * This list may contain null values in instances where a string could not be converted to an object. 
     * @param value
     * @return 
     */
    private List<C> stringToList(final String value) {
        
        final List<C> foundChoices = new ArrayList<>();
        final List<String> choiceIndex = this.getOptions().stream().map(Object::toString).toList();
        
        final String[] items = value.split(SeparatorConstants.COMMA);
        for (String item : items){
            final int index = choiceIndex.indexOf(item.strip());           
            if (index == -1){
                //return null;
                foundChoices.add(null);
            } else {
                foundChoices.add(this.getOptions().get(index));
            }    
        }
        return foundChoices;          
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
    public boolean isValid() {
        if (getText().isBlank()){
            return true;
        } else {
            final List<C> items = stringToList(getText());
            return items != null && !items.contains(null) && items.size() == 1 ;
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
    public Button getLeftButton() {
        switch (type) {
            case SINGLE_SPINNER -> {
                return  new Button(new Label(ConstellationInputConstants.PREVIOUS_BUTTON_LABEL), ButtonType.CHANGER) {
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
    public Button getRightButton() {
        
        Label label;
        ButtonType buttonType;
        
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
        
        return new Button(label, buttonType) {
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
            
            final List<MenuItem> items = new ArrayList<>(); 
            
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

                    final CustomMenuItem menuItem = this.buildCustomMenuItem(item);   


                    items.add(menuItem);
                }
            }
            
            final ConstellationInputFieldListener<List<C>> cl = (List<C> newValue) -> {
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
            
            //Add all of the items to the context menu
            this.addMenuItems(items);
        }
    }
    // </editor-fold> 
    
    // <editor-fold defaultstate="collapsed" desc="Auto Complete Implementation"> 
    @Override
    public List<MenuItem> getAutoCompleteSuggestions() {
        final List<MenuItem> suggestions = new ArrayList<>();
        C choice = this.getChoice();
        //do not show suggestions in the following cases
        //if there are two unknown choices that the user has enteres, i.e more than 1 null value.
        //if there is 1 or more valid choices in the event that this is a single choice input.
        if (choice == null){
            //Remove blank entrys from here
            this.getOptions()
                    .stream()
                    .map(value -> value)
                    .filter(value -> value.toString().startsWith(getText()))
                    .forEach(value -> {
                        MenuItem item = new MenuItem(value.toString());
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
