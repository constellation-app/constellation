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

import au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputFieldConstants.LayoutConstants;
import au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputFieldConstants.TextType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TextInputControl;

/**
 * A {@link ConstellationinputField} for managing password inputs. 
 * This field is based off of a {@link PasswordField}. 
 * 
 * To facilitate showing and hiding of passwords an alternate {@link TextField}
 * is used and linked to the {@link PasswordField}.
 * When interacting with the Password Input Field through the ConstellationInputField Interface 
 * the {@link PasswordField} is always treated as the base field.
 * @author capricornunicorn123
 */
public final class PasswordInputField extends ConstellationInputField<String> {
    
    private boolean isVisible = false;
    
    public PasswordInputField(){
        super(LayoutConstants.INPUT_DROPDOWN, TextType.SECRET);
        this.setRightLabel(ConstellationInputFieldConstants.SHOW_BUTTON_LABEL);    
        
        this.registerRightButtonEvent(event -> {
            if (isVisible){
                this.hideSecret();
                this.setRightLabel(ConstellationInputFieldConstants.SHOW_BUTTON_LABEL);  
                isVisible = false;
                
            } else {
                this.showSecret();
                this.setRightLabel(ConstellationInputFieldConstants.HIDE_BUTTON_LABEL);  
                isVisible = true;
            }
        });
    }
    
    @Override
    public ContextMenu getDropDown() {
        throw new UnsupportedOperationException("PasswordInputField does not provide a ContextMenu");
    }
    
    /**
     * Password validation could be included here with tool tip triggers
     * things like minimum length, character combinations etc
     * @param value
     * @return 
     */
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
}
