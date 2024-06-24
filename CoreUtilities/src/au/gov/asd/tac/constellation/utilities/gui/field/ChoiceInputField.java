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

import au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputFieldConstants.ChoiceType;
import static au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputFieldConstants.ChoiceType.MULTI;
import static au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputFieldConstants.ChoiceType.SINGLE_DROPDOWN;
import static au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputFieldConstants.ChoiceType.SINGLE_SPINNER;
import au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputFieldConstants.TextType;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

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
public final class ChoiceInputField<C extends Object> extends ConstellationInputField<List<C>> {
    
    private final List<C> options = new ArrayList<>();
    private final List<ImageView> icons = new ArrayList<>();
    private final ChoiceType type;

    public ChoiceInputField(final ChoiceType type){
        super(type.getLayout(), TextType.SINGLELINE);
        switch (type){
            case SINGLE_SPINNER -> {
                setRightLabel(ConstellationInputFieldConstants.NEXT_BUTTON_LABEL);
                setLeftLabel(ConstellationInputFieldConstants.PREVIOUS_BUTTON_LABEL);
            }
            case SINGLE_DROPDOWN, MULTI -> setRightLabel(ConstellationInputFieldConstants.SELECT_BUTTON_LABEL);
        }
        
        //Add shortcuts where users can increment and decrement the date using up and down arrows
        if (type != ChoiceType.MULTI) {
            this.addShortcuts(KeyEvent.KEY_PRESSED, event -> {
                switch (event.getCode()){
                    case UP -> this.incrementChoice();
                    case DOWN -> this.decrementChoice();
                }
            });
        }
        
        this.type = type;
    }
    
    // <editor-fold defaultstate="collapsed" desc="Local Private Methods">   
    /**
     * Defines the options that users can select from in this field.
     * Any previously defined options will be overwritten with this new list.
     * @param options 
     */
    public void setOptions(final List<C> options){
        this.options.clear();
        this.options.addAll(options);
    }
    
    /**
     * Retrieves the options that users can select from in this field.
     * @return List of Options 
     */
    public List<C> getOptions(){
        return this.options;
    }
    
    /**
     * Defines the list of icons for the context menu
     * @param icons
     */
    public void setIcons(final List<ImageView> icons) {
        this.icons.clear();
        this.icons.addAll(icons);
    }
 
    /**
     * Changes the List of selected Choices to include the provided choice.
     * this method will only modify the list of choices for ChoiceInputFields with
     * ChoiceType.MULTI
     * @param requestedChoices 
     */
    private void setChoices(final List<C> requestedChoices) {
        if (requestedChoices != null && this.type == ChoiceType.MULTI) {
            //This meethod requres use of an Arraylist. Casting is not possible.
            final ArrayList<C> localList = new ArrayList<>();       
            localList.addAll(requestedChoices);
            // Only retain the choices from the selection that in te available options
            localList.retainAll(options);
            //Single Modiication
            this.setText(listToString(localList));
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
        if (this.options.contains(choice) && ((currentChoices != null && !currentChoices.contains(choice)) || currentChoices == null)) {
            switch (type){
                case SINGLE_DROPDOWN, SINGLE_SPINNER -> this.setText(choice.toString());
                case MULTI -> {
                    currentChoices.add(choice);
                    this.setText(this.listToString(currentChoices));
                }
            } 
        }
    }
    
    /**
     * Retrieves a list of the currently elected choices.
     * 
     * @return 
     */
    private List<C> getChoices() {
        return stringToList(this.getText());
    }
    
    /**
     * Removes the provided choice from the currently selected choices.
     * @param choice 
     */
    private void removeChoice(final C choice){
        final List<C> choices = this.getChoices();
        choices.remove(choice);
        this.setChoices(choices);
    }
    
    /**
     * Removes all choices.
     */
    private void clearChoices() {
       this.setText("");
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
        final List<String> choiceIndex = this.options.stream().map(Object::toString).toList();
        
        final String[] items = value.split(SeparatorConstants.COMMA);
        for (String item : items){
            if (!item.isBlank()){
                final int index = choiceIndex.indexOf(item.strip());           
                if (index == -1){
                    return null;
                } else {
                    foundChoices.add(this.options.get(index));
                }    
            }
        }
        return foundChoices;          
    }

    /**
     * Used in single choice Options to increment a selected choice.
     * If the choice is the last choice in the list of options the next choice is the first option.
     */
    private void incrementChoice() {
        if (type != ChoiceType.MULTI){
            final List<C> selections = this.getChoices();
            if (selections.size() == 1){
                final int nextSelectionIndex = this.options.indexOf(selections.getFirst()) + 1;
                if (nextSelectionIndex < this.options.size()){
                    this.setChoice(this.options.get(nextSelectionIndex));
                } else {
                    this.setChoice(this.options.getFirst());
                }
            } else {
                this.setChoice(this.options.getLast());
            }
        }
    }
    
    /**
     * Used in single choice Options to decrement a selected choice.
     * If the choice is the first choice in the list of options the previous choice is the last option.
     */
    private void decrementChoice() {
        final List<C> selections = this.getChoices();
        if (selections.size() == 1){
            final int prevSelectionIndex = this.options.indexOf(selections.getFirst()) - 1;
            if (prevSelectionIndex < 0){
                this.setChoice(this.options.getLast());
            } else {
                this.setChoice(this.options.get(prevSelectionIndex));
            }
        } else {
            this.setChoice(this.options.getFirst());
        }
    }
    // </editor-fold> 
        
    // <editor-fold defaultstate="collapsed" desc="Value Modification & Validation Implementation"> 
    @Override
    public List<C> getValue() {
        return this.getChoices();
    }
    
    @Override
    public void setValue(final List<C> value) {
        this.setChoices(value);
    }
    
    @Override
    public boolean isValid() {
        if (getText().isBlank()){
            return true;
        } else {
            final List<C> items = stringToList(getText());
            switch (type){
                case SINGLE_DROPDOWN, SINGLE_SPINNER -> {
                    return items != null && items.size() == 1 ;
                }
                case MULTI -> {
                    if (items != null){
                        final List<C> duplicates = items.stream().collect(Collectors.groupingBy(i -> i)).entrySet().stream().filter(entry -> entry.getValue().size() > 1).map(entry -> entry.getKey()).toList();
                        return duplicates.isEmpty();
                    } 
                }
            }
            return false;
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
                    next.setOnAction(value -> getRightButtonEventImplementation().handle(null));
                    items.add(next);

                    final MenuItem prev = new MenuItem("Decrement");
                    prev.setOnAction(value -> getLeftButtonEventImplementation().handle(null));
                    items.add(prev);
                }
                case SINGLE_DROPDOWN, MULTI -> {
                    final MenuItem choose = new MenuItem("Select Choice");
                    choose.setOnAction(value -> getRightButtonEventImplementation().handle(null));
                    items.add(choose);
                }
            }
        }
        return items;
    }
    // </editor-fold> 

    // <editor-fold defaultstate="collapsed" desc="Button Event Implementation">   
    @Override
    public EventHandler<MouseEvent> getRightButtonEventImplementation() {
        return switch (type){
            case SINGLE_SPINNER -> event -> this.incrementChoice();
            case SINGLE_DROPDOWN, MULTI -> event -> this.showDropDown();
        };
    }

    @Override
    public EventHandler<MouseEvent> getLeftButtonEventImplementation() {
        return switch (type){
            case SINGLE_SPINNER -> event -> decrementChoice(); 
            default -> null;
        };
    }
    // </editor-fold> 
    
    // <editor-fold defaultstate="collapsed" desc="Drop Down Implementation">   
    @Override
    public ContextMenu getDropDown() {
        try {
            return new ChoiceInputDropDown(this);
        } catch (Exception ex) {
            return null;
        }
    } 

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
        
        public ChoiceInputDropDown(final ChoiceInputField field) throws Exception{
            super(field);
            
            final List<MenuItem> items = new ArrayList<>(); 
            
            if (field.type == ChoiceType.MULTI) {
                
                //Select All Bulk Selection Feature
                final Label all = new Label("Select All");
                all.setOnMouseClicked(event -> field.setChoices(field.options)); 
                items.add(this.buildCustomMenuItem(all));

                //Clear All Bulk Selection Feature
                final Label clear = new Label("Clear All");
                clear.setOnMouseClicked(event -> field.clearChoices()); 
                items.add(this.buildCustomMenuItem(clear));

                items.add(new SeparatorMenuItem());
            }       
            
            if (options != null){
                final Object[] optionsList = options.toArray();
                final List<C> choices = field.getChoices();
                for (int i = 0 ; i < optionsList.length ; i ++){
                    final C choice = (C) optionsList[i];

                    final Labeled item = switch(field.type){
                        case MULTI -> {
                            final CheckBox box = new CheckBox(choice.toString());
                            box.setOnAction(event -> {
                                if (box.isSelected()) {
                                    field.setChoice(choice);
                                } else {
                                    field.removeChoice(choice);
                                }
                            }); 
                            box.setSelected(choices.contains(choice));
                            boxes.add(box);
                            yield box;
                        }
                        case SINGLE_DROPDOWN -> {
                            final Label label = new Label(choice.toString());
                            label.setOnMouseClicked(event -> {
                                    field.setChoice(choice);
                            }); 
                            yield label;
                        }
                        case SINGLE_SPINNER -> throw new Exception("Spinner inputs do not have context menus");
                    };

                    if (!icons.isEmpty()){
                        item.setGraphic(icons.get(i));
                    }

                    final CustomMenuItem menuItem = this.buildCustomMenuItem(item);   

                    //If in multiple choice mode the context menu should not be closed after an item is selected.
                    if (type == ChoiceType.MULTI){
                        menuItem.setHideOnClick(false);
                    }

                    items.add(menuItem);
                }
            }
            
            final ChangeListener<List<C>> cl = (ObservableValue<? extends List<C>> observable, List<C> oldValue, List<C> newValue) -> {
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
    
    // <editor-fold defaultstate="collapsed" desc="Info Window Implementation">   
    @Override
    public final InputInfoWindow getInputInfoWindow() {
         return new choiceInputInfoWondow(this);
    }
    
    private class choiceInputInfoWondow extends InputInfoWindow{
        Label label = new Label();
        private choiceInputInfoWondow(ConstellationInputField parent){
            super(parent);

            this.getChildren().add(label);
        }
        
        @Override
        protected void refreshWindow() {
            int totalOptions = getOptions().size();
            int selectedOptions = getChoices().size();
            String text = String.format("%s/%s", selectedOptions, totalOptions);
            label.setText(text);
            if (selectedOptions < 2) {
                if (hasInputInfoWindow()) {
                    removeInputInfoWindow(this);
                }
            } else {
                if (!hasInputInfoWindow()){
                    insertInputInfoWindow(this);
                }
            }
        }
    }   
    //</editor-fold> 
}
