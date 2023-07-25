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
package au.gov.asd.tac.constellation.plugins.parameters;

import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import org.controlsfx.control.CheckComboBox;

/**
 * This component enables selection options for input fields extending CheckComboBox's
 * By Default it enables Select All and Clear All functionality but also permits custom menu selection options. 
 * Selection options can be displayed in a Menu Button or in a context menu accessed through right clicking the input field. 
 * 
 * @author capricornunicorn123
 */
public final class SelectOptionsExtension{
    
    private final CheckComboBox PARENT_INPUT_FIELD;
    private final ContextMenu CONTEXT_MENU = new ContextMenu();
    private final MenuButton MENU_BUTTON = new MenuButton("...");
    
    /**
     * An extension to input fields that extend a @link{CheckComboBox} to enable default and custom selection options. 
     * Can be implemented with a menu button or as a popup menu or both. 
     * @param <T>
     * @param parentInputfield
     */
    public <T> SelectOptionsExtension(final CheckComboBox<T> parentInputfield) {
        PARENT_INPUT_FIELD = parentInputfield;
        MENU_BUTTON.setTooltip(new Tooltip("Selection Options"));
        MENU_BUTTON.setPrefWidth(50);
        MENU_BUTTON.setMinWidth(50);
        
        setSelectionOption("Select All", event -> this.PARENT_INPUT_FIELD.getCheckModel().checkAll());
        setSelectionOption("Clear All", event -> this.PARENT_INPUT_FIELD.getCheckModel().clearChecks());
        
    }
    
    /**
     * Sets a selection option in the menu.
     * @param displayText
     * @param event
     */
    public void setSelectionOption(String displayText, EventHandler<ActionEvent> event) {
        MenuItem menuItem = new MenuItem(displayText);
        menuItem.setOnAction(event);
        MENU_BUTTON.getItems().add(menuItem);
        
        // Setting a MenuItem to the MenuButton makes the MenuItem unavailable to the ContentMenu
        // so the MenuItem bust be made twice
        menuItem = new MenuItem(displayText);
        menuItem.setOnAction(event);
        CONTEXT_MENU.getItems().add(menuItem);
    }

    /**
     * Enables menu to be displayed when right clicking the parent input field.
     */
    public void enablePopUp() {
        this.PARENT_INPUT_FIELD.setOnContextMenuRequested(event -> CONTEXT_MENU.show(this.PARENT_INPUT_FIELD, event.getScreenX(), event.getScreenY()));
    }
    
    /**
     * Disables SelectOptions being displayed when right clicking the parent input field.
     */
    public void disablePopUp() {
        this.PARENT_INPUT_FIELD.setOnContextMenuRequested(null);
    }
    
    /**
     * Returns the MenuButton containing selection options
     * @return
     */
    public MenuButton getMenuButton() {
        return MENU_BUTTON;
    }
}

