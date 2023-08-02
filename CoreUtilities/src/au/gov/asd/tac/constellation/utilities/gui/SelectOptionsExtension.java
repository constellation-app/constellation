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
package au.gov.asd.tac.constellation.utilities.gui;

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
    
    private final CheckComboBox<? extends Object> parentInputfield;
    private final ContextMenu contextMenu = new ContextMenu();
    private final MenuButton menuButton = new MenuButton("...");
    
    /**
     * An extension to input fields that extend a @link{CheckComboBox} to enable default and custom selection options. 
     * Can be implemented with a menu button or as a popup menu or both. 
     * @param <T>
     * @param parentInputfield
     */
    public <T> SelectOptionsExtension(final CheckComboBox<T> parentInputfield) {
        this.parentInputfield = parentInputfield;
        menuButton.setTooltip(new Tooltip("Selection Options"));
        menuButton.setPrefWidth(50);
        menuButton.setMinWidth(50);
        
        setSelectionOption("Select All", event -> this.parentInputfield.getCheckModel().checkAll());
        setSelectionOption("Clear All", event -> this.parentInputfield.getCheckModel().clearChecks());
    }
    
    /**
     * Sets a selection option in the menu.
     * @param displayText
     * @param event
     */
    public void setSelectionOption(final String displayText, final EventHandler<ActionEvent> event){
        // Note: Setting a MenuItem to the MenuButton makes that MenuItem unavailable to the ContentMenu
        // so the MenuItem must be made seperately for the MenuButton and the ContextMenu
        final MenuItem menuButtonItem = new MenuItem(displayText);
        menuButtonItem.setOnAction(event);
        menuButton.getItems().add(menuButtonItem);
        
        final MenuItem contextMenuItem = new MenuItem(displayText);
        contextMenuItem.setOnAction(event);
        contextMenu.getItems().add(contextMenuItem);
    }

    /**
     * Enables menu to be displayed when right clicking the parent input field.
     */
    public void enablePopUp() {
        this.parentInputfield.setOnContextMenuRequested(event -> contextMenu.show(this.parentInputfield, event.getScreenX(), event.getScreenY()));
    }
    
    /**
     * Disables SelectOptions being displayed when right clicking the parent input field.
     */
    public void disablePopUp() {
        this.parentInputfield.setOnContextMenuRequested(null);
    }
    
    /**
     * Returns the MenuButton containing selection options.
     * @return MenuButton
     */
    public MenuButton getMenuButton() {
        return menuButton;
    }
    
    protected CheckComboBox getField(){
        return this.parentInputfield;
    }
}

