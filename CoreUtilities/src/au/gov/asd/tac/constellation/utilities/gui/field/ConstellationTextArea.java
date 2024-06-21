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

import au.gov.asd.tac.constellation.utilities.gui.context.ContextMenuContributor;
import static au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputFieldConstants.TextType.MULTILINE;
import static au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputFieldConstants.TextType.SECRET;
import java.util.Arrays;
import java.util.List;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

/**
     * This class represents the area of text that users can interact with inside of a {@link ConstellationInputFiled}.
     * 
     * The {@link ConstellationTextArea} has been designed to provide a minimal interface to the {@link ConstellationIputField} with
     * the intention of simplifying its use and protecting the integrity of the data it stores from uncontrolled manipulation.
     * 
     * To assist with the protection of data within this class, the class and methods are declared private with 
     * the class being also declared final.
     * 
     * The {@linkConstellationTextArea itself} is a HBox that contains children of type {@link TextInputControll}
     * In almost all cases, the ConstellationText Area will only have one child, a {@link TextArea} or 
     * a {@link TextField}. In cases where the input field needs to be secret (have the characters hidden) 
     * a primary input of {@link PasswordField} and a secondary input of {@link TextField} will be used to 
     * facilitate hiding and showing of the hidden text.
     * 
     * the raw inputs can still be grabbed by using the get children methods. is this an issue / vunerability?
     * 
     * @author capricornunicorn123
     */
    public final class ConstellationTextArea extends StackPane implements ContextMenuContributor{
        
        private final Insets insets = new Insets(4, 8, 4, 8);
        private List<MenuItem> menuItems = null;
        
        private final TextInputControl primaryInput;
        private final TextInputControl secondaryInput;

        public ConstellationTextArea(ConstellationInputField parent, ConstellationInputFieldConstants.TextType type){
            switch (type){
                case MULTILINE -> {
                    TextArea area = new TextArea();
                    area.setWrapText(true);
                    primaryInput = area;
                }
                default -> {
                    primaryInput = new TextField();
                    primaryInput.setPadding(insets);
                }
            }
            //Set up the primary InputControl
            primaryInput.textProperty().addListener(parent);
            primaryInput.focusedProperty().addListener(parent);
            primaryInput.setStyle("-fx-background-radius: 0; -fx-background-color: transparent; -fx-border-color: transparent; -fx-stroke-width: 0; -fx-border-width: 0 ; -fx-focus-color: transparent; -fx-text-fill: #FFFFFF;");
            
            // Set up the optional secondary InputControl
            switch (type) {
                case SECRET -> {
                    // SecondaryInputs are only used in Secret Inputs and have bound properties wiht the primary input
                    secondaryInput = new PasswordField();
                    secondaryInput.textProperty().bindBidirectional(primaryInput.textProperty());
                    secondaryInput.textFormatterProperty().bindBidirectional(primaryInput.textFormatterProperty());
                    secondaryInput.promptTextProperty().bindBidirectional(primaryInput.promptTextProperty());
                    secondaryInput.styleProperty().bind(primaryInput.styleProperty());
                    secondaryInput.focusedProperty().addListener(parent);
                    primaryInput.setVisible(false);

                    this.getChildren().addAll(primaryInput, secondaryInput);
                }
                default -> {
                    secondaryInput = null;
                    this.getChildren().add(primaryInput);
                }
            } 

        }
        
        // <editor-fold defaultstate="collapsed" desc="Local Private Methods"> 
        /**
         * Binds the heightProperty of a Rectangle to the height property of the {@link TextInputControl}. 
         * @param bindable
         */
        public void bindHeightProperty(Rectangle bindable) {
            bindable.heightProperty().bind(primaryInput.heightProperty());
        }

        /**
         * Sets the prompt text of the {@link TextInputControl}.
         * @param promptText 
         */
        public final void setPromptText(final String promptText) {
            this.primaryInput.setPromptText(promptText);
        }

        /**
         * Sets the text value of the {@link TextInputControl}.
         * @param stringValue 
         */
        public void setText(String stringValue) {
            if (stringValue != null) {
                primaryInput.setText(stringValue);
            }
        }
        
        public void setContextMenu(ContextMenu menu){
            this.primaryInput.setContextMenu(menu);
        }

        /**
         * Specifies if the {@link TextInputControl} is editable
         * @param enabled 
         */
        public void setEditable(boolean enabled) {
            primaryInput.setEditable(enabled);
        }

        public String getText() {
            return primaryInput.getText();
        }

        public void setTooltip(Tooltip tooltip) {
             Tooltip.install(primaryInput, tooltip);
        }

        public void setPreferedRowCount(Integer suggestedHeight) {
            if (primaryInput instanceof TextArea textAreaField){
                textAreaField.setPrefRowCount(suggestedHeight);
                textAreaField.setMaxHeight(textAreaField.getPrefHeight() + 3);
            }
        }

        public void hide() {
            if (secondaryInput != null){
                this.primaryInput.setVisible(false);
                this.secondaryInput.setVisible(true);
            } else {
                throw new UnsupportedOperationException("Only ConstellationTextAreas of TextType.SECRET can be hidden");
            }
        }

        public void reveal() {
            if (secondaryInput != null){
                this.primaryInput.setVisible(true);
                this.secondaryInput.setVisible(false); 
            } else {
                throw new UnsupportedOperationException("Only ConstellationTextAreas of TextType.SECRET can be revealed");
            }
        }
        
        // <editor-fold defaultstate="collapsed" desc="ContextMenuContributor Implementation"> 
        @Override
        public List<MenuItem> getAllMenuItems() {
            return getLocalMenuItems();
        }
        
        @Override
        public List<MenuItem> getLocalMenuItems() {
            if (menuItems == null){
                final MenuItem cutMenuItem = new MenuItem("Cut");          
                final MenuItem copyMenuItem = new MenuItem("Copy");
                final MenuItem pasteMenuItem = new MenuItem("Paste");
                final MenuItem deleteMenuItem = new MenuItem("Delete");
                final MenuItem selectAllMenuItem = new MenuItem("Select All");
                final MenuItem undoMenuItem = new MenuItem("Undo");
                final MenuItem redoMenuItem = new MenuItem("Redo");
                undoMenuItem.setOnAction(e -> primaryInput.undo());
                redoMenuItem.setOnAction(e -> primaryInput.redo());
                cutMenuItem.setOnAction(e -> primaryInput.cut());
                copyMenuItem.setOnAction(e -> primaryInput.copy());
                pasteMenuItem.setOnAction(e -> primaryInput.paste());
                deleteMenuItem.setOnAction(e -> primaryInput.deleteText(primaryInput.getSelection()));
                selectAllMenuItem.setOnAction(e -> primaryInput.selectAll());

                menuItems = Arrays.asList(undoMenuItem, redoMenuItem, cutMenuItem, copyMenuItem, pasteMenuItem, deleteMenuItem, selectAllMenuItem);
            }
            return menuItems;
        }
        // </editor-fold> 

        public void setContextMenuRequestedEvent(EventHandler<? super ContextMenuEvent> value) {
            this.primaryInput.setOnContextMenuRequested(value);
        }

    void primaryInputSetOnMouseClicked(EventHandler<MouseEvent> event) {
        primaryInput.setOnMouseClicked(event);
    }

    void primaryInputSetOnKeyReleased(EventHandler<KeyEvent> event) {
        primaryInput.setOnKeyReleased(event);
    }
}   
