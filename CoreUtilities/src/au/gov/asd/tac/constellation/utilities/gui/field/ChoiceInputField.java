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
import javafx.scene.control.CheckBox;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javax.swing.ImageIcon;

/**
 *
 * @author capricornunicorn123
 */
public class ChoiceInputField<C extends Object> extends ConstellationInputField {
    
    private List<C> choices = new ArrayList<>();
    private List<ImageView> icons = new ArrayList<>();
    private ChoiceType type;
    
    public ChoiceInputField(ChoiceType type){
        super(ConstellationInputFieldLayoutConstants.INPUT_DROPDOWN , TextType.SINGLELINE);

        this.setRightLabel("Select");
        this.registerRightButtonEvent(event -> {
            this.showDropDown();            
        });
        
        this.type = type;
    }

    public ChoiceType getType(){
        return this.type;
    }
    
    public void setItems(List<C> options){
        this.choices.clear();
        this.choices.addAll(options);
    }
    
    /**
     * Produce the list of icons for the context menu
     * @param icons
     */
    public void setIcons(List<ImageView> icons) {
        this.icons.clear();
        this.icons.addAll(icons);
    }
    
    public void select(List<C> choices){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i< choices.size(); i++) {
            if (i != 0){
                sb.append(", ");
            }
            sb.append(choices.get(i).toString());
        }
        this.setText(sb.toString());
    }
    
    public void select(C choice){
        String newOption = choice.toString();
        switch (type){
           
            case SINGLE -> {
                this.setText(newOption);
            }
            case MULTI -> {
                List<C> currentSelection = this.getSelectedItems();
                List<C> items = new ArrayList<>();
                for (C item : choices){
                    if (currentSelection.contains(item) || choice.equals(item)){
                        items.add(item);
                    }
                }
                select(items);
           
//                boolean include = true;
//                String oldText = this.getText();
//                String[] options = oldText.split(SeparatorConstants.COMMA);
//                for (String option : options){
//                    if (option.strip().equals(newOption)) {
//                        include = false;
//                    }
//                } 
//
//                if (include) {
//                    StringBuilder sb = new StringBuilder();
//                    if (!oldText.isBlank()){
//                    sb.append(oldText);
//                        if (options.length != 0){
//                            sb.append(", ");
//                        }
//                    }
//                    sb.append(newOption);
//                    this.setText(sb.toString());
//                }
            }
        }
    }
    
    public void deselect(C choice){
        switch (type){
            case SINGLE -> {
                this.clearSelection();
            }
            case MULTI -> {   
                List<C> selectedItems = getSelectedItems();
                this.clearSelection();
                selectedItems.remove(choice);
                this.select(selectedItems);
            }
        }
    }
        
    @Override
    public ConstellationInputDropDown getDropDown() {
        return new ChoiceInputDropDown(this);
    }
    
    @Override
    public boolean isValid(String value) {
        return true;
    }

    public C getSelectedItem() {
        for (C choice : choices){
            if (choice.toString().equals(this.getText())){
                return choice;
            }
        }
        return null;
    }
    
    public List<C> getSelectedItems() {
        List<C> selectedItems = new ArrayList<>();
        String oldText = this.getText();
        if (oldText != null && !oldText.isBlank()){
            String[] options = oldText.split(SeparatorConstants.COMMA);
            for (String option : options){
                for (C choice : choices){
                    if (choice.toString().equals(option.strip())){
                        selectedItems.add(choice);
                    }
                }
            }
        }
        return selectedItems;
    }

    public void clearSelection() {
       this.setText("");
    }
    
    private class ChoiceInputDropDown extends ConstellationInputDropDown {
        public ChoiceInputDropDown(ChoiceInputField field){
            super(field);
            
            if (field.type == ChoiceType.MULTI) {
            Label all = new Label("Select All");
            all.setOnMouseClicked(event -> field.select(field.choices)); 
            this.addMenuOption(all);
            
            Label clear = new Label("Clear All");
            clear.setOnMouseClicked(event -> field.clearSelection()); 
            this.addMenuOption(clear);
            
            this.addSeparator();
            }       
            
            List<C> selectedChoices = field.getSelectedItems();
            
            for (int i = 0 ; i < choices.size() ; i ++){
                final C choice = choices.get(i);
                
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
                        box.setSelected(selectedChoices.contains(choice));
                        yield box;
                    }
                    case SINGLE -> {
                        final Label label = new Label(choice.toString());
                        label.setOnMouseClicked(event -> {
                                field.select(choice);
                        }); 
                        yield label;
                    }
                };

                if (!icons.isEmpty()){
                    item.setGraphic(icons.get(i));
                }
                this.addMenuOption(item);   
            }
        }
    }
    
    public enum ChoiceType {
        SINGLE,
        MULTI;
    }
}
