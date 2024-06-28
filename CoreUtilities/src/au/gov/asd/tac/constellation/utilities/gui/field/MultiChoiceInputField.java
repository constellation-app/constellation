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

import au.gov.asd.tac.constellation.utilities.gui.field.framework.Button;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.Button.ButtonType;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.AutoCompleteSupport;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.ConstellationInputDropDown;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.InfoWindowSupport;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.RightButtonSupport;

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
public final class MultiChoiceInputField<C extends Object> extends ChoiceInputField<List<C>, C> implements RightButtonSupport, InfoWindowSupport, AutoCompleteSupport {

    public MultiChoiceInputField(){    
        initialiseDepedantComponents();
    }    
  
    // <editor-fold defaultstate="collapsed" desc="Local Private Methods">   
    /**
     * Changes the List of selected Choices to include the provided choice.
     * this method will only modify the list of choices for ChoiceInputFields with
     * ChoiceType.MULTI
     * @param requestedChoices 
     */
    private void setChoices(final List<C> requestedChoices) {
        if (requestedChoices != null) {
            //This meethod requres use of an Arraylist. Casting is not possible.
            final ArrayList<C> localList = new ArrayList<>();       
            localList.addAll(requestedChoices);
            // Only retain the choices from the selection that in te available options
            localList.retainAll(getOptions());
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
        if (this.getOptions().contains(choice) && ((currentChoices != null && !currentChoices.contains(choice)) || currentChoices == null)) {
            currentChoices.add(choice);
            this.setText(this.listToString(currentChoices));
        }
    }
    
    /**
     * Retrieves a list of the currently selected choices.
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
    // </editor-fold> 
        
    // <editor-fold defaultstate="collapsed" desc="Value Modification & Validation Implementation"> 
    @Override
    public List<C> getValue() {
        List<C> choices = getChoices();
        if (choices != null && !choices.contains(null) ) {
            return choices;
        } else {
            return null;
        }
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

            if (items != null && !items.contains(null)){
                final List<C> duplicates = items.stream().collect(Collectors.groupingBy(i -> i)).entrySet().stream().filter(entry -> entry.getValue().size() > 1).map(entry -> entry.getKey()).toList();
                return duplicates.isEmpty();
            }
            return false;
        }
    }  
    // </editor-fold> 

    // <editor-fold defaultstate="collapsed" desc="ContextMenuContributor Implementation"> 
    @Override
    public List<MenuItem> getLocalMenuItems() {
        final List<MenuItem> items = new ArrayList();

        final MenuItem choose = new MenuItem("Select Choice");
        choose.setOnAction(value -> executeRightButtonAction());
        items.add(choose);

        return items;
    }
    // </editor-fold> 

    // <editor-fold defaultstate="collapsed" desc="Button Event Implementation">   
    
    @Override
    public Button getRightButton() {
        return new Button(new Label(ConstellationInputFieldConstants.SELECT_BUTTON_LABEL), ButtonType.DROPDOWN) {
                @Override
                public EventHandler<? super MouseEvent> action() {
                    return event -> executeRightButtonAction();
                }
        };
    }
    
    @Override
    public void executeRightButtonAction() {
        showDropDown(new ChoiceInputDropDown(this));
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
        
        public ChoiceInputDropDown(final MultiChoiceInputField field){
            super(field);
            
            final List<MenuItem> items = new ArrayList<>(); 

            //Select All Bulk Selection Feature
            final Label all = new Label("Select All");
            all.setOnMouseClicked(event -> field.setChoices(field.getOptions())); 
            items.add(this.buildCustomMenuItem(all));

            //Clear All Bulk Selection Feature
            final Label clear = new Label("Clear All");
            clear.setOnMouseClicked(event -> field.clearChoices()); 
            items.add(this.buildCustomMenuItem(clear));

            items.add(new SeparatorMenuItem());
              
            
            if (getOptions() != null){
                final Object[] optionsList = getOptions().toArray();
                final List<C> choices = field.getChoices();
                for (int i = 0 ; i < optionsList.length ; i ++){
                    final C choice = (C) optionsList[i];

                        CheckBox item = new CheckBox(choice.toString());
                        item.setOnAction(event -> {
                            if (item.isSelected()) {
                                field.setChoice(choice);
                            } else {
                                field.removeChoice(choice);
                            }
                        }); 
                        item.setSelected(choices.contains(choice));
                        boxes.add(item);
                            

                    if (!icons.isEmpty()){
                        item.setGraphic(icons.get(i));
                    }

                    final CustomMenuItem menuItem = this.buildCustomMenuItem(item);   
                    //context menu should not be closed after an item is selected.
                    menuItem.setHideOnClick(false);
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
    
    // <editor-fold defaultstate="collapsed" desc="Info Window Implementation">   
    @Override
    public final InfoWindow getInfoWindow() {
        Label label = new Label();
        
        InfoWindow window = new InfoWindow(this){
            @Override
            protected void refreshWindow() {
                List<C> selectedOptions = getChoices();

                if (!isValid() || selectedOptions.size() < 2) {
                    if (hasInfoWindow()) {
                        removeInfoWindow(this);
                    }
                } else {
                    String text = String.format("%s/%s", selectedOptions.size(), getOptions().size());
                    label.setText(text);
                    if (!hasInfoWindow()){            

                        insertInfoWindow(this);
                    }
                }
            }
        };
        window.setWindowContents(label);
        return window;
    }
    //</editor-fold> 
    
    // <editor-fold defaultstate="collapsed" desc="Auto Complete Implementation"> 
    @Override
    public List<MenuItem> getAutoCompleteSuggestions() {
        List<C> choices = this.getChoices();
        //do not show suggestions in the following cases
        //if there are two unknown choices that the user has enteres, i.e more than 1 null value.
        //if there is 1 or more valid choices in the event that this is a single choice input.
        if (choices.stream().filter(value -> value == null).count() != 1){
            return null;
        } else {
            //Remove blank entrys from here
            String[] candidateArray = this.getText().split(SeparatorConstants.COMMA);
            String invalidEntry = candidateArray[choices.indexOf(null)].stripLeading().stripTrailing();

            final List<MenuItem> suggestions = new ArrayList<>();
            
            this.getOptions()
                    .stream()
                    .map(value -> value)
                    .filter(value -> !choices.contains(value))
                    .filter(value -> value.toString().startsWith(invalidEntry))
                    .forEach(value -> {
                        MenuItem item = new MenuItem(value.toString());
                        item.setOnAction(event -> {
                                choices.add(choices.indexOf(null), value);
                                this.setChoices(choices);
                        });
                        suggestions.add(item);
                    });
            return suggestions;
            }
    }
    // </editor-fold> 
}
