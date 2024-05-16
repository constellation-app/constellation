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

import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.image.ImageView;

/**
 * A {@link ConstellationinputField} for managing choice selection. 
 * 
 * @author capricornunicorn123
 */
public final class ChoiceInputField<C extends Object> extends ConstellationInputField<List<C>> implements ListChangeListener {
    
    private final ObservableList<C> options = FXCollections.observableArrayList();
    private final ObservableList<C> choices = FXCollections.observableArrayList();
    private final List<ImageView> icons = new ArrayList<>();
    private final ChoiceType type;

    public ChoiceInputField(ChoiceType type){
        super(type.getLayout(), TextType.SINGLELINE);
        switch (type){
            case SINGLE_SPINNER -> {
                this.setRightLabel("Next");
                this.registerRightButtonEvent(event -> {
                    this.incrementChoice();            
                });
                
                this.setLeftLabel("Prev");
                this.registerLeftButtonEvent(event -> {
                    this.decrementChoice();          
                });
            }
            case SINGLE_DROPDOWN, MULTI -> {
                this.setRightLabel("Select");
                this.registerRightButtonEvent(event -> {
                    this.showDropDown();            
                });
            }
        }
        
        this.type = type;
        
        //Add a listener to the choices list and update the text acrodingly
        this.addChoiceListener(this);
        
        this.getBaseField().textProperty().addListener((observable, oldValue, newValue) -> {
            if (isValid()){
                List<C> newChoices = this.stringToList(newValue);
                if (newChoices != null){
                    if (!this.choices.equals(newChoices)){
                        this.choices.setAll(newChoices);
                    }
                }
            }
        });
    }
    
    public final void addChoiceListener(ListChangeListener listener){
        this.choices.addListener(listener);
    }
    
    public final void removeChoiceListener(ListChangeListener listener){
        this.choices.removeListener(listener);
    }

    public ChoiceType getType(){
        return this.type;
    }
    
    /**
     * Defines the options that users can select from in this field.
     * @param options 
     */
    public void setOptions(List<C> options){
        this.options.setAll(options);
    }
    
    /**
     * Defines the options that users can select from in this field.
     * @param options 
     */
    public List<C> getOptions(){
        return this.options;
    }
    
    /**
     * DEfines the list of icons for the context menu
     * @param icons
     */
    public void setIcons(List<ImageView> icons) {
        this.icons.clear();
        this.icons.addAll(icons);
    }
    
    /**
     * Changes the List of selected Choices to include the provided choice.
     * this method willonly modify the list of choices for ChoiceInputFields with
     * ChoiceType.MULTI
     * @param choices 
     */
    public void select(List<C> choices) {
        if (this.type == ChoiceType.MULTI) {
            
            // Simply adding a choice does not enforce the sort order
            List<C> unsortedChoices = new ArrayList<>();
            unsortedChoices.addAll(this.choices);
            
            for (C choice : choices) {
                if (!unsortedChoices.contains(choice)){
                    unsortedChoices.add(choice);
                }
            }
            
            //Itterate over the options and add add the choices based on that order.
            List<C> sortedChoices = new ArrayList<>();
            for (C option : options){
                if (unsortedChoices.contains(option)){
                    sortedChoices.add(option);
                }
            }

            //Single Modiication
            this.choices.setAll(sortedChoices);
        }
    }
    
    /**
     * Changes the List of selected Choices to ensure the provided choice is included.
     * if single choice selection mode then the old choice is removed
     * if multi choice the old choice is retained
     * @param choice 
     */
    public void select(C choice){
        if (this.options.contains(choice)) {
            switch (type){
                case SINGLE_DROPDOWN, SINGLE_SPINNER -> {
                    this.choices.setAll(choice);
                } 
                
                case MULTI -> {
                    // Simply adding a choice does not enforce the sort order
                    List<C> unsortedChoices = new ArrayList<>();
                    unsortedChoices.addAll(this.choices);
                    if (!unsortedChoices.contains(choice)){
                        unsortedChoices.add(choice);
                    }
                    
                    List<C> sortedChoices = new ArrayList<>();
                    //Itterate over the options and add add the choices based on that order.
                    for (C option : options){
                        if (unsortedChoices.contains(option)){
                            sortedChoices.add(option);
                        }
                    }
                    
                    //Single Modiication
                    this.choices.setAll(sortedChoices);
                }
            } 
        }
    }
    
    /**
     * Removed the provided choice from the currently selected choices.
     * @param choice 
     */
    public void deselect(C choice){
        this.choices.remove(choice);
    }
        
    @Override
    public ContextMenu getDropDown() {
        try {
            return new ChoiceInputDropDown(this);
        } catch (Exception ex) {
            return null;
        }
    }
    
    @Override
    public boolean isValid() {
        if (getText().isBlank()){
            return true;
        } else {
            List<C> items = stringToList(getText());
            switch (type){
                case SINGLE_DROPDOWN, SINGLE_SPINNER -> {
                    return items != null && items.size() == 1;
                }
                case MULTI -> {
                    if (items != null){
                        List<C> duplicates = items.stream().collect(Collectors.groupingBy(i -> i)).entrySet().stream().filter(entry -> entry.getValue().size() > 1).map(entry -> entry.getKey()).toList();
                        return duplicates.isEmpty();
                    } 
                }
            }
            return false;
        }
    }
    
    public List<C> getSelectedItems() {
        List<C> selectedItems = new ArrayList<>();
        selectedItems.addAll(choices);
        return selectedItems;
    }

    public void clearSelection() {
       this.choices.clear();
    }

    /**
     * Implementation of the ListChangeListener.
     * is triggered whenever the choices list is changed.
     * this change can be triggered by selecting aon opton in the drop down or by modifying the text in the input field. 
     * In the case that the event is trigered by selecting an option, the input field butst be updated.
     * In the event that the input field triggered the change on the chieces, the tefield does not need t be updated.d
     * @param change 
     */
    @Override
    public void onChanged(Change change) {
        ObservableList<C> changes = change.getList();
        //The text and choices are not up to date meaning the triggere was not from the textConroll field. 
        if (!this.choices.equals(stringToList(this.getText()))){
            this.setText(listToString(changes));
        }
    }

    private String listToString(ObservableList<C> set) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0 ; i < set.size() ; i++){
            C option = set.get(i);
                if (i != 0){
                    sb.append(", ");
                }
            sb.append(option.toString());
        }
        return sb.toString();          
    }
    
    private List<C> stringToList(String value) {
        
        List<C> foundChoices = new ArrayList<>();
        List<String> choiceIndex = this.options.stream().map(Object::toString).toList();
        
        String[] items = value.split(SeparatorConstants.COMMA);
        for (String item : items){
            if (!item.isBlank()){
                int index = choiceIndex.indexOf(item.strip());           
                if (index == -1){
                    return null;
                } else {
                    foundChoices.add(this.options.get(index));
                }    
            }
        }

        return foundChoices;          
    }

    private void incrementChoice() {
        List<C> selections = this.getSelectedItems();
        if (selections.size() == 1){
            int nextSelectionIndex = this.options.indexOf(selections.getFirst()) + 1;
            if (nextSelectionIndex < this.options.size()){
                this.select(this.options.get(nextSelectionIndex));
            } else {
                this.select(this.options.getFirst());
            }
        } else {
            this.select(this.options.getLast());
        }
    }

    private void decrementChoice() {
        List<C> selections = this.getSelectedItems();
        if (selections.size() == 1){
            int prevSelectionIndex = this.options.indexOf(selections.getFirst()) - 1;
            if (prevSelectionIndex < 0){
                this.select(this.options.getLast());
            } else {
                this.select(this.options.get(prevSelectionIndex));
            }
        } else {
            this.select(this.options.getFirst());
        }
    }

    @Override
    public List<C> getValue() {
        return this.getSelectedItems();
    }

    @Override
    public void setValue(List<C> value) {
        //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    private class ChoiceInputDropDown extends ConstellationInputDropDown implements ListChangeListener{
        List<CheckBox> boxes = new ArrayList<>();
        
        public ChoiceInputDropDown(ChoiceInputField field) throws Exception{
            super(field);
            
            if (field.type == ChoiceType.MULTI) {
            Label all = new Label("Select All");
            all.setOnMouseClicked(event -> field.select(field.options)); 
            this.addMenuOption(all);
            
            Label clear = new Label("Clear All");
            clear.setOnMouseClicked(event -> field.clearSelection()); 
            this.addMenuOption(clear);
            
            this.addSeparator();
            }       
            
            Object[] optionsList = options.toArray();
            for (int i = 0 ; i < optionsList.length ; i ++){
                final C choice = (C) optionsList[i];
                
                final Labeled item = switch(field.type){
                    case MULTI -> {
                        final CheckBox box = new CheckBox(choice.toString());
                        box.setOnAction(event -> {
                            if (box.isSelected()) {
                                field.select(choice);
                            } else {
                                field.deselect(choice);
                            }
                        }); 
                        box.setSelected(choices.contains(choice));
                        boxes.add(box);
                        yield box;
                    }
                    case SINGLE_DROPDOWN -> {
                        final Label label = new Label(choice.toString());
                        label.setOnMouseClicked(event -> {
                                field.select(choice);
                        }); 
                        yield label;
                    }
                    case SINGLE_SPINNER -> throw new Exception("changer inputs do not have context menus");
                };

                if (!icons.isEmpty()){
                    item.setGraphic(icons.get(i));
                }
                this.addMenuOption(item);   
            }
            
            //Listeners
            this.setOnShowing(value -> {
                field.addChoiceListener(this);
            });
            
            this.setOnHiding(value -> {
                field.removeChoiceListener(this);
            });
        }

        @Override
        public void onChanged(Change c) {
            List<String> list = c.getList().stream().map(Object::toString).toList();
                for (CheckBox box : boxes){
                    box.setSelected(list.contains(box.getText()));
                } 
        }
    }
    
    public enum ChoiceType {
        SINGLE_DROPDOWN(ConstellationInputFieldLayoutConstants.INPUT_DROPDOWN),
        SINGLE_SPINNER(ConstellationInputFieldLayoutConstants.UPDATER_INPUT_UPDATER),
        MULTI(ConstellationInputFieldLayoutConstants.INPUT_DROPDOWN);
        
        ConstellationInputFieldLayoutConstants layout;
        
        private ChoiceType(ConstellationInputFieldLayoutConstants layout) {
            this.layout = layout;
        }
        
        public ConstellationInputFieldLayoutConstants getLayout(){
            return this.layout;
        }
        
    }
}
