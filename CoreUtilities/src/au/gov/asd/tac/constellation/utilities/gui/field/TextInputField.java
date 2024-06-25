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

import au.gov.asd.tac.constellation.utilities.gui.RecentValue.RecentValuesListener;
import au.gov.asd.tac.constellation.utilities.gui.RecentValue.RecentValueUtility;
import au.gov.asd.tac.constellation.utilities.gui.RecentValue.RecentValuesChangeEvent;
import au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputFieldConstants.LayoutConstants;
import au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputFieldConstants.TextType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;

/**
 * A {@link ConstellationinputField} for managing {@link String} selection. 
 * 
 * @author capricornunicorn123
 */
public final class TextInputField extends ConstellationInputField<String> implements RecentValuesListener {
    
    private final List<String> recentValues = new ArrayList<>();
    private final String recentValueListeningId;
    
    public TextInputField(final TextType type, final String recentValueListeningId){
        super(recentValueListeningId == null ? LayoutConstants.INPUT : LayoutConstants.INPUT_DROPDOWN, type);
        this.recentValueListeningId = recentValueListeningId;
        if (recentValueListeningId != null){
            this.setRightLabel("Recent");
            
            RecentValueUtility.addListener(this);
            this.addRecentValues(RecentValueUtility.getRecentValues(recentValueListeningId));
        }
    }    
    
    @Override
    public void recentValuesChanged(final RecentValuesChangeEvent e) {
        if (e.getId().equals(recentValueListeningId)) {
            Platform.runLater(() -> {
                final List<String> recentValues = e.getNewValues();
                this.addRecentValues(recentValues);
            });
        }
    }
    
    public void addRecentValues(final List<String> options){
        if (options != null){
            this.recentValues.clear();
            this.recentValues.addAll(options);
        }
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
        recent.setOnAction(value -> getRightButtonEventImplementation().handle(null));
        return Arrays.asList(recent);
    }
    // </editor-fold>  
         
    // <editor-fold defaultstate="collapsed" desc="Button Event Implementation">   
    @Override
    public EventHandler<MouseEvent> getRightButtonEventImplementation() {
        return event -> this.showDropDown(getDropDown());     
    }

    @Override
    public EventHandler<MouseEvent> getLeftButtonEventImplementation() {
        return null;
    }
    // </editor-fold>   
    
    // <editor-fold defaultstate="collapsed" desc="DropDown Implementation">   
    @Override
    public ContextMenu getDropDown() {
        return new TextInputDropDown(this);
    }
    
    private class TextInputDropDown extends ConstellationInputDropDown {
        public TextInputDropDown(final TextInputField field){
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
    
    // <editor-fold defaultstate="collapsed" desc="InfoWindow Implementation"> 
    @Override
    public InputInfoWindow getInputInfoWindow() {
        return null;
    }
    // </editor-fold> 
    
    // <editor-fold defaultstate="collapsed" desc="Auto Complete Implementation"> 
    @Override
    protected List<MenuItem> getAutoCompleteSuggestions() {
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
}
