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

import au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputConstants.TextType;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.Button;
import java.util.ArrayList;
import java.util.List;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.RightButtonSupport;

/**
 * A {@link ConstellationInput} for managing password entry. 
 * This input provides the following {@link ConstellationInput} support features
 * <ul>
 * <li>{@link RightButtonSupport} - Shows and Hides the value of the text.</li>
 * </ul>
 * See referenced classes and interfaces for further details on inherited and implemented features.
 * 
 * @author capricornunicorn123
 */
public final class PasswordInput extends ConstellationInput<String> implements RightButtonSupport {
    
    private boolean isVisible = false;
    private final Label label = new Label();
    public PasswordInput(){
        super(TextType.SECRET);  
        label.setText(ConstellationInputConstants.SHOW_BUTTON_LABEL);    
        initialiseDepedantComponents();
    }
    
    // <editor-fold defaultstate="collapsed" desc="Value Modification & Validation Implementation"> 
    @Override
    public String getValue() {
        return this.getText();
    }

    @Override
    public void setValue(final String value) {
        this.setText(value);
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
    // </editor-fold> 
    
    // <editor-fold defaultstate="collapsed" desc="ContextMenuContributor Implementation">   
    @Override
    public List<MenuItem> getLocalMenuItems() {
        return new ArrayList();
    }
    // </editor-fold> 
    
    // <editor-fold defaultstate="collapsed" desc="Button Event Implementation">
    @Override
    public Button getRightButton() {
        Button button = new Button(label, Button.ButtonType.CHANGER) {
            @Override
            public EventHandler<? super MouseEvent> action() {
                return event -> {
                    executeRightButtonAction();
                };
            }
        };
        return button;
    }
    
    @Override
    public void executeRightButtonAction() {
        if (isVisible){
            this.hideSecret();
            label.setText(ConstellationInputConstants.SHOW_BUTTON_LABEL);    
            isVisible = false;

        } else {
            this.showSecret();
            label.setText(ConstellationInputConstants.HIDE_BUTTON_LABEL);    
            isVisible = true;
        }
    }
    // </editor-fold> 
}
