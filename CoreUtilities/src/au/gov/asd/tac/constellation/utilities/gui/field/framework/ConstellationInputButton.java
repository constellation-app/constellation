/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.utilities.gui.field.framework;

import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

/**
 * This button will be used within the ConstellationInput framework.
 * Eg. This is used in RightSupportButton, LeftSupportButton etc.
 * It is an extension of ComboBox so it have the L&F of a ComboBox.
 * If ButtonType is not DROPDOWN, the layoutChildren() will not
 * display the dropdown arrow on the button.
 * 
 * @author capricornunicorn123
 * @author andromeda-224
 */
public abstract class ConstellationInputButton extends ComboBox<String> {

    private final ButtonType btnType;
    private Region arrowBtn = null ;
    
    protected ConstellationInputButton(final Label label, final ButtonType type) {        
        btnType = type;
        if (!label.getText().isEmpty()) {
            this.setValue(label.getText());
        }
    }
    
    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        // don't display arrow if not dropdown
        if (arrowBtn == null && btnType != ButtonType.DROPDOWN) {
            arrowBtn = (Region)lookup(".arrow-button");            
            arrowBtn.setMaxSize(0,0);
            arrowBtn.setMinSize(0,0);
            arrowBtn.setPadding(new Insets(0));

            Region arrow = (Region)lookup(".arrow");
            arrow.setMaxSize(0,0);
            arrow.setMinSize(0,0);
            arrow.setPadding(new Insets(0));
            
            // Call again the super method to relayout with the new bounds.
            super.layoutChildren();
        }
    }
    
        
    public enum ButtonType{
        POPUP,
        DROPDOWN,
        CHANGER;
    }
}
