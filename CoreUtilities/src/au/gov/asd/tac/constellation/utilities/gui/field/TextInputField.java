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

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.controlsfx.control.textfield.TextFields;

/**
 * A {@link ConstellationinputField} for managing {@link String} selection. 
 * 
 * @author capricornunicorn123
 */
public final class TextInputField extends ConstellationInputField<String> {
    
    private List<String> recentValues = new ArrayList<>();
    
    public TextInputField(TextType type, boolean showRecentValues){
        super(showRecentValues ? ConstellationInputFieldLayoutConstants.INPUT_DROPDOWN : ConstellationInputFieldLayoutConstants.INPUT, type);
        if (showRecentValues){
            this.setRightLabel("Recent");
            if (type.equals(TextType.SINGLELINE)){
                Platform.runLater(() -> TextFields.bindAutoCompletion((TextField) this.getBaseField(), recentValues));
            }
        }
        
        this.registerRightButtonEvent(event -> {
            this.showDropDown();            
        });
        
    }
    
    public static void addRecentValues(ConstellationInputField inputField, List<String> options) {
        if (inputField instanceof TextInputField textField){
            textField.addRecentValues(options);
        }
    }
    
    public void addRecentValues(List<String> options){
        this.recentValues.clear();
        this.recentValues.addAll(options);
    }
    
    @Override
    public ContextMenu getDropDown() {
        return new TextInputDropDown(this);
    }
    
    @Override
    public boolean isValid(){
        return true;
    }

    @Override
    public String getValue() {
        return this.getText();
    }

    @Override
    public void setValue(String value) {
        this.setText(value);
    }
    
    private class TextInputDropDown extends ConstellationInputDropDown {
        public TextInputDropDown(TextInputField field){
            super(field);
            
            for (final String recentValue : recentValues){
                final Label label = new Label(recentValue);
                
                label.setOnMouseClicked(event -> {
                    field.setText(recentValue);
                });

                this.addMenuOption(label);
            }
        }
    }
}
