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

import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Skin;
import javafx.scene.control.Tooltip;
import org.controlsfx.control.CheckComboBox;

/**
 * Enables users to select multiple input options in an input field.
 * This object also facilitates automated bulk selection functionality.
 * By Default "Select All" and "Clear All" functionality is enabled. Custom menu selection options are also permitted. 
 * Selection options can be displayed in a {@link javafx.scene.control.MenuButton} or in a {@link javafx.scene.control.ContextMenu}. 
 * This class represents an input field node able to be added to a nodes children for screen rendering. 
 * To render the select option {@link javafx.scene.control.MenuButton} the function {@link au.gov.asd.tac.constellation.utilities.gui.MultiChoiceInputField#getBulkSelectionOptionsMenuButton()} must be called and the returned node added separately. 
 * 
 * @author capricornunicorn123
 * @param <T>
 */
public class MultiChoiceInputField<T extends Object> extends CheckComboBox<T> {
    
    private final ContextMenu contextMenu = new ContextMenu();
    private final MenuButton bulkSelectionOptionsMenuButton = new MenuButton("...");
    
    public MultiChoiceInputField() {
        this(true);
    }
    
    public MultiChoiceInputField(final boolean enableBulkSelectionOptionPopUp) {
        super();
        initialiseDefaultSelectionOptions();
        if (enableBulkSelectionOptionPopUp){
            this.enablePopUp();
        }
    }

    public MultiChoiceInputField(final ObservableList<T> items) {
        super(items);
        initialiseDefaultSelectionOptions();
        this.enablePopUp();
    }
    
    /**
     * Adds "Select All" and "Clear All" selection options to the input field.
     */
    private void initialiseDefaultSelectionOptions(){
        bulkSelectionOptionsMenuButton.setTooltip(new Tooltip("Selection Options"));
        bulkSelectionOptionsMenuButton.setPrefWidth(50);
        bulkSelectionOptionsMenuButton.setMinWidth(50);
        setSelectionOption("Select All", event -> this.getCheckModel().checkAll());
        setSelectionOption("Clear All", event -> this.getCheckModel().clearChecks());
    }
    
    /**
     * Sets a selection option in the selection option menu.
     * @param displayText
     * @param event
     */
    public final void setSelectionOption(final String displayText, final EventHandler<ActionEvent> event){
             
        // Note: Setting a MenuItem to the MenuButton makes that MenuItem unavailable to the ContentMenu
        // so the MenuItem must be made seperately for the MenuButton and the ContextMenu
        final MenuItem menuButtonItem = new MenuItem(displayText);
        menuButtonItem.setOnAction(event);
        bulkSelectionOptionsMenuButton.getItems().add(menuButtonItem);
        
        final MenuItem contextMenuItem = new MenuItem(displayText);
        contextMenuItem.setOnAction(event);
        contextMenu.getItems().add(contextMenuItem);
    }

    /**
     * Enables selection option menu to be displayed when right clicking the parent input field.
     */
    public final void enablePopUp() {
        this.setOnContextMenuRequested(event -> contextMenu.show(this, event.getScreenX(), event.getScreenY()));
    }
    
    /**
     * Disables selection option menu from being displayed when right clicking the parent input field.
     */
    public final void disablePopUp() {
        this.setOnContextMenuRequested(null);
    }
    
    /**
     * Retrieve the {@link javafx.scene.control.MenuButton} containing the bulk selection options.
     * @return {@link javafx.scene.control.MenuButton}
     */
    public MenuButton getBulkSelectionOptionsMenuButton(){
        return this.bulkSelectionOptionsMenuButton;
    }
    
    @Override
    protected Skin<?> createDefaultSkin() {
        return super.createDefaultSkin();
    }

    // --- prompt text (taken from JavaFX's ComboBoxBase.java)
    /**
     * The {@code ComboBox} prompt text to display, or <tt>null</tt> if no
     * prompt text is displayed. Prompt text is not displayed in all
     * circumstances, it is dependent upon the subclasses of ComboBoxBase to
     * clarify when promptText will be shown. For example, in most cases
     * prompt text will never be shown when a combo box is non-editable
     * (that is, prompt text is only shown when user input is allowed via
     * text input). This has been copied from JavaFX's ComboBoxBase.java.
     */
    private final StringProperty promptText = new SimpleStringProperty(this, "promptText", "") {
        @Override
        protected void invalidated() {
            // Strip out newlines
            String txt = get();
            if (txt != null && txt.contains(SeparatorConstants.NEWLINE)) {
                txt = txt.replace(SeparatorConstants.NEWLINE, "");
                set(txt);
            }
        }
    };

    public final StringProperty promptTextProperty() {
        return promptText;
    }

    public final String getPromptText() {
        return promptText.get();
    }

    public final void setPromptText(final String value) {
        promptText.set(value);
    }
    
}