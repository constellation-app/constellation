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

import au.gov.asd.tac.constellation.utilities.gui.recentvalue.RecentValuesListener;
import au.gov.asd.tac.constellation.utilities.gui.recentvalue.RecentValuesChangeEvent;
import au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputConstants.TextType;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.Button;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.AutoCompleteSupport;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.ConstellationInputDropDown;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.RightButtonSupport;

/**
 * A {@link ConstellationInput} for managing choice selection. 
 * This input provides the following {@link ConstellationInput} support features:
 * <ul>
 * <li>{@link RightButtonSupport} - Triggers a drop down menu to display recent values.</li>
 * <li>{@link RecentValuesListener} - Enables this Input field to be notified when recent values change.</li>
 * <li>{@link AutoCompleteSupport} - Enables a list of ManuItems to be provided that are candidates fro auto complete text.</li>
 * </ul>
 * See referenced classes and interfaces for further details on inherited and implemented features.
 * 
 * @author capricornunicorn123
 */
public final class TextInput extends ConstellationInput<String> implements RecentValuesListener, AutoCompleteSupport, RightButtonSupport {
    
    private final List<String> recentValues = new ArrayList<>();
    private final String recentValueListeningId;
    
    public TextInput(final TextType type, final String recentValueListeningId){
        super(type);
        this.recentValueListeningId = recentValueListeningId;
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
    
    @Override
    public boolean isValid(){
        return true;
    }
    // </editor-fold> 
    
    // <editor-fold defaultstate="collapsed" desc="ContextMenuContributor Implementation">   
    @Override
    public List<MenuItem> getLocalMenuItems() {
        final MenuItem recent = new MenuItem("Recent Values");
        recent.setOnAction(value -> executeRightButtonAction());
        return Arrays.asList(recent);
    }
    // </editor-fold>  
    
    // <editor-fold defaultstate="collapsed" desc="Recent Value Implementation">   
    @Override
    public String getRecentValuesListenerID() {
        return this.recentValueListeningId;
    }
    
    @Override
    public void recentValuesChanged(final RecentValuesChangeEvent e) {
        if (e.getId().equals(recentValueListeningId)) {
            Platform.runLater(() -> {
                final List<String> recentValues = e.getNewValues();
                if (recentValues != null){
                    this.recentValues.clear();
                    this.recentValues.addAll(recentValues);
                }
            });
        }
    }   
    // </editor-fold>  
    
    // <editor-fold defaultstate="collapsed" desc="Auto Complete Implementation"> 
    @Override
    public List<MenuItem> getAutoCompleteSuggestions() {
        final List<MenuItem> suggestions = new ArrayList<>();
        this.recentValues
                .stream()
                .filter(value -> value.startsWith(this.getText()))
                .filter(value ->  !value.equals(this.getText()))
                .map(value -> new MenuItem(value))
                .forEach(item -> {
                    item.setOnAction(event -> {
                        this.setText(item.getText());
                    });
                    suggestions.add(item);
                });
        
        return suggestions;
    }
    // </editor-fold>    

    // <editor-fold defaultstate="collapsed" desc="RightButtonSupport Implementation">   
    @Override
    public RightButton getRightButton() {
        return new RightButton(new Label("Recent"), Button.ButtonType.DROPDOWN) {
            @Override
            public EventHandler<? super MouseEvent> action() {
                return event -> {
                    executeRightButtonAction();
                };
            }
        };
    }
    
    @Override
    public void executeRightButtonAction() {
        this.showDropDown(new TextInputDropDown(this));     
    }
    
    private class TextInputDropDown extends ConstellationInputDropDown {
        public TextInputDropDown(final TextInput field){
            super(field);
            final List<MenuItem> items = new ArrayList<>();
            for (final String recentValue : recentValues){
                final Label label = new Label(recentValue);
                
                label.setOnMouseClicked(event -> {
                    field.setText(recentValue);
                });
                items.add(this.buildCustomMenuItem(label));
            }
            this.addMenuItems(items);
        }
    }
    // </editor-fold> 
}
