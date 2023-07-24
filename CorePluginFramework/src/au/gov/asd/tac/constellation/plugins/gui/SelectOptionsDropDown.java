/*
* Copyright 2010-2023 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.gui;

import au.gov.asd.tac.constellation.plugins.gui.MultiChoiceInputPane.MultiChoiceComboBox;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import au.gov.asd.tac.constellation.plugins.parameters.types.ParameterValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;

/**
 *
 * @author capricornunicorn123
 */
public final class SelectOptionsDropDown {
    
    private final List<SelectionOption> selectionOptions;

    private final MultiChoiceComboBox<ParameterValue> field;
    private ContextMenu contextMenu = new ContextMenu();
    public SelectOptionsDropDown(final MultiChoiceComboBox<ParameterValue> field) {
        this.field = field ;
        this.selectionOptions = new ArrayList<>();
        
        setSelectionOption("Select All", event -> this.field.getCheckModel().checkAll());
        setSelectionOption("Clear All", event -> this.field.getCheckModel().clearChecks());
        
    }
    
    public void setSelectionOption(String itemName, EventHandler<ActionEvent> event){
        selectionOptions.add(new SelectionOption(itemName, event));
    }
    
    public MenuButton getMenuButton() {
        MenuButton menuButton = new MenuButton("...");
        Menu menu = new Menu();
        this.selectionOptions.stream().forEach(option -> menuButton.getItems().add(option.toMenuItem()));
        
        menuButton.setTooltip(new Tooltip("Selection Options"));
        menuButton.setPrefWidth(50);
        menuButton.setMinWidth(50);
        
        this.selectionOptions.stream().forEach(option -> contextMenu.getItems().add(option.toMenuItem()));
        field.setOnContextMenuRequested(event -> contextMenu.show(menuButton, event.getScreenX(), event.getScreenY()));

        return menuButton;
    }
}

final class SelectionOption {
    private final String displayText;
    private final EventHandler<ActionEvent> event;
    
    public SelectionOption(final String displayText, EventHandler<ActionEvent> event) {
        this.displayText = displayText;
        this.event = event;
    }
    
    public final String getDisplayText() {
        return this.displayText;
    }
    public final EventHandler<ActionEvent> getEvent() {
        return this.event;
    }
    
    public MenuItem toMenuItem(){
        final MenuItem menuItem = new MenuItem(this.displayText);
        menuItem.setOnAction(this.event);
        return menuItem;
    }
    
}
