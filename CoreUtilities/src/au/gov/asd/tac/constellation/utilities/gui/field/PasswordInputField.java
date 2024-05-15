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

import javafx.scene.control.ContextMenu;
import javafx.scene.control.TextInputControl;

/**
 * An input field for handling the entry of passwords.
 * This field is based off of a {@link PasswordField}. 
 * 
 * To facilitate showing and hiding of passwords an alternate {@link TextField}
 * is used and linked to the {@link PasswordField}.
 * When interacting with the Password Input Field through the COnstellationInputField Interface 
 * the {@link PasswordField} is always treated as the base field.
 * @author capricornunicorn123
 */
public class PasswordInputField extends ConstellationInputField {
    final private TextInputControl alternate;
    
    public PasswordInputField(){
        super(ConstellationInputFieldLayoutConstants.INPUT_DROPDOWN, TextType.SECRET);
        this.setRightLabel("Show");    
        
        final TextInputControl base = this.getBaseField();
        alternate = this.createInputField(TextType.SINGLELINE);
        alternate.textProperty().bindBidirectional(base.textProperty());
        alternate.textFormatterProperty().bindBidirectional(base.textFormatterProperty());
        alternate.promptTextProperty().bindBidirectional(base.promptTextProperty());
        alternate.setVisible(false);
        this.insertBaseFieldIntoGrid(alternate);
        
        this.registerRightButtonEvent(event -> {
            if (base.isVisible()){
                base.setVisible(false);
                alternate.setVisible(true);
                this.setRightLabel("Hide");  
            } else {
                base.setVisible(true);
                alternate.setVisible(false);
                this.setRightLabel("Show");  
            }
        });
    }
    
    @Override
    public ContextMenu getDropDown() {
        throw new UnsupportedOperationException("PasswordInputField does not provide a ContextMenu");
    }
    
    /**
     * Password validation could be included here with tool top triggers
     * things like minimum length, character combinations etc
     * @param value
     * @return 
     */
    @Override
    public boolean isValid(String value){
        return true;
    }
}
