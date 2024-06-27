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
import au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputFieldConstants.TextType;
import au.gov.asd.tac.constellation.utilities.gui.field.Window.InfoWindow;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.AutoCompletable;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.Button;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.ButtonLeft;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.ButtonRight;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.ConstellationTextArea;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.KeyPressShortcut;
import au.gov.asd.tac.constellation.utilities.gui.recentvalue.RecentValueUtility;
import au.gov.asd.tac.constellation.utilities.gui.recentvalue.RecentValuesChangeEvent;
import au.gov.asd.tac.constellation.utilities.gui.recentvalue.RecentValuesListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Labeled;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;


/**
 * An Abstract base class for all input fields in Constellation. 
 * These input fields have been designed to be robust and adaptable to as many use cases as possible
 * whilst maintaining a strict design language. 
 * Amend this class and any classes which extend from it with caution as the impact of changes to functionality of 
 * this class may effect many components with Constellation.
 * 
 * To achieve this adaptive layout a number fo nuanced javaFX structures have been integrated into the input field 
 * that should be understood before extending this class. 
 * Input fields follow a magic-wand-like layout with three main sections. 
 * +--------------------------------------------+
 * |  Button  |      Input Area      |  Button  |
 * +--------------------------------------------+
 * A central input area and two end buttons.
 * in the case that 1 or both end button is not needed it can be removed and the subsequent Input Area shall adapt to fill the missing space.
 * +--------------------------------------------+
 * |           Input Area            |  Button  |
 * +--------------------------------------------+
 * When extending an input field the following considerations should be made:
 * For single button input fields, the button shall be placed on the Right. 
 * Buttons that initiate a context menu or modify the value or apperance of the text area should be grey.
 * Buttons that initiate a pop-up window shall be blue.
 * Buttons that initiate a pop-up window shall always be positioned on the right.
 * 
 * The Node construction of the class is as follows 
 *  StackPane
 *      Shape - A rounded rectangle acting as the background
 *      GridPane
 *          for each cell
 *              Group
 *                  cell contents
 *      Shape - A rounded transparent rectangle acting as the border
 * 
 * //TODO: Lock down the Text property on all input fields. this includes getText get stringValue etc. 
 * there should only be 1 way in and out of the input field for selection data and that is getValue() and setValue().
 * this is for the benefit of password input field but also benefits others
 * 
 * The Text of the input field should be taken as the source of truth for the current value. 
 * implementations of ConselationInput fields may implements their own back end data structures and objects to simplify the 
 * 
 * @author capricornunicorn123
 */
public abstract class ConstellationInputField<T> extends StackPane implements ChangeListener<Serializable>, ContextMenuContributor{   

    final int defaultCellHeight = 22;
    
    private final HBox interactableContent = new HBox();
    private ConstellationTextArea textArea;
    
    protected final List<ConstellationInputFieldListener> InputFieldListeners = new ArrayList<>();
    private Rectangle foreground;
    private Rectangle background;
        
    final int corner = 7;
    
    final Color fieldColor = Color.color(51/255D, 51/255D, 51/255D);
    final Color invalidColor = Color.color(238/255D, 66/255D, 49/255D);

    
    public ConstellationInputField(){
        this(TextType.SINGLELINE);
    }
    
    public ConstellationInputField(final TextType type) {

        textArea = new ConstellationTextArea(this, type);
        
        this.setPrefWidth(500);
        this.setMinWidth(200);
        
        final Rectangle clippingMask = new Rectangle(300, defaultCellHeight);
        clippingMask.setArcWidth(corner);
        clippingMask.setArcHeight(corner);        
        clippingMask.setFill(Color.BLACK);
        clippingMask.setStroke(Color.BLACK);
        clippingMask.widthProperty().bind(interactableContent.widthProperty());
        
        background = new Rectangle(300, defaultCellHeight);
        background.setArcWidth(corner);
        background.setArcHeight(corner);  
        background.setFill(fieldColor);
        background.widthProperty().bind(interactableContent.widthProperty());
        
        foreground = new Rectangle(300, defaultCellHeight);
        foreground.setArcWidth(corner);
        foreground.setArcHeight(corner);        
        foreground.setFill(Color.TRANSPARENT);
        foreground.setMouseTransparent(true);
        foreground.widthProperty().bind(interactableContent.widthProperty());

        textArea.bindHeightProperty(background, foreground, clippingMask);
        interactableContent.setClip(clippingMask);
        
        HBox.setHgrow(textArea, Priority.ALWAYS);
        interactableContent.setAlignment(Pos.CENTER);
        HBox.setHgrow(interactableContent, Priority.ALWAYS);
        
        this.getChildren().addAll(background, interactableContent, foreground);
        this.setAlignment(Pos.TOP_LEFT);
        
        final ContextMenu contextMenu = new ContextMenu();
//        contextMenu.getItems().clear();
//        contextMenu.getItems().addAll(this.getAllMenuItems());
        // Set the right click context menu items
        // we want to update each time the context menu is requested 
        // can't make a new context menu each time as this event occurs after showing
        this.setOnContextMenuRequested(value -> {
            contextMenu.getItems().clear();
            contextMenu.getItems().addAll(this.getAllMenuItems());
            this.showDropDown(contextMenu);
            //textArea.setContextMenu(contextMenu);
        });
        //textArea.setContextMenu(contextMenu);
    }
    
    protected void initialiseDepedantComponents(){
        
        //Add Left button
        if (this instanceof ButtonLeft leftButton){
            Button button = leftButton.getLeftButton();
            if (button != null){
                button.getHeightProperty().bind(textArea.heightProperty());
                interactableContent.getChildren().addFirst(button);
                this.createWidthListener(button, ContentDisplay.LEFT);
            }
        }

        //Add Base
        interactableContent.getChildren().add(textArea);
        
        //Add Window
        if (this instanceof Window infoWindow){
            insertInfoWindow(infoWindow.getInfoWindow());
        }

        //Add Right Button
        if (this instanceof ButtonRight rightButton){
            Button button = rightButton.getRightButton();
            button.getHeightProperty().bind(textArea.heightProperty());
            interactableContent.getChildren().add(button);
            this.createWidthListener(button, ContentDisplay.RIGHT);
        }
        
        //Add Shortcuts
        if (this instanceof KeyPressShortcut shortcut) {
            EventHandler<KeyEvent> shortcutEvent = shortcut.getShortcuts();
            if (shortcutEvent != null){
                this.textArea.addEventFilter(KeyEvent.KEY_PRESSED, shortcut.getShortcuts());
            }
        }        
        
        //Be carefull with implementing autocomplete and recent values together
        
        //Add Recent Values
        if (this instanceof RecentValuesListener listener){
            String id = listener.getRecentValuesListenerID();
            if (id != null){
                RecentValueUtility.addListener(listener);
                List<String> values = RecentValueUtility.getRecentValues(id);
                if (values != null){
                    listener.recentValuesChanged(new RecentValuesChangeEvent(id, values));
                }
            }
        }
        
        //AutoComplete
        if (this instanceof AutoCompletable autoComplete) {
            this.addListener(newValue -> {
                if (textArea.isInFocus()){
                    final List<MenuItem> suggestions = autoComplete.getAutoCompleteSuggestions();
                    if (suggestions != null && !suggestions.isEmpty()){
                        ContextMenu menu = new ContextMenu();
                        menu.getItems().addAll(suggestions);
                        menu.setAutoHide(true);
                        menu.setAutoFix(true);
                        //Listen for key events for when arrows are pressed or when to hide the menu
                        this.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
                            menu.hide();
                        });
                        showDropDown(menu);
                    }
                }
            });
        }
    }
    
    private void createWidthListener(final Button content, ContentDisplay side){      
        //Allow this end cell to listen to the width of the total Input field
        this.widthProperty().addListener((value, oldValue, newValue)-> {
    
            //Node this end cell if the width is too low
            if (newValue.intValue() < 300){
                if (content.isVisible()){
                    interactableContent.getChildren().remove(content);
                }
                content.setVisible(false);
            
            //Show this end cell if the width is large enough
            } else {
                if (!content.isVisible()){
                    //Build the components      
                    switch (side) {
                        case LEFT -> interactableContent.getChildren().addFirst(content);
                        case RIGHT -> interactableContent.getChildren().addLast(content);
                        default -> {
                            //
                        }
                    };
                }
                content.setVisible(true);
            }
        });
        
    }
    
    // <editor-fold defaultstate="collapsed" desc="ObservableValue and ConstellationInputFieldListener Interface Support">
    /**
     * This method manages the handling of changes to the TextProperty and FocusedProperty of the {@link StypledTextArea} used
     * by the {@link ConstellationTextArea}.
     * 
     * This method is declared final to protect the integrity and privacy of the string value being returned. 
     * Important in the case of use with TextTpye.SECRET.
     */
    @Override
    public final void changed(ObservableValue<? extends Serializable> observable, Serializable oldValue, Serializable newValue) {
        
        // String changes are changes to the text value of the ConstellationTextArea
        if (newValue instanceof String){
            if (isValid()){
                setValid(true);
                notifyListeners(getValue());
            } else {
                setValid(false);
                notifyListeners(null);
            }
        }
        
        //Boolean Changes are changs to the focused property of the ConstellationTextArea
        if (newValue instanceof Boolean focused){
            this.setInFocus(focused);
        }
    }

    public void notifyListeners(T newValue){
        for (ConstellationInputFieldListener listener : InputFieldListeners){
            listener.changed(newValue);
        }
    }
    
    public void addListener(ConstellationInputFieldListener listener) {
        this.InputFieldListeners.add(listener);
    }

    public void removeListener(ConstellationInputFieldListener listener) {
        this.InputFieldListeners.remove(listener);
    }
    // </editor-fold>   
    
    // <editor-fold defaultstate="collapsed" desc="ConstelationTextArea Modification Methods">

    public void setPromptText(final String description) {
        this.textArea.setPromptText(description);
    }
    
    public void setTooltip(Tooltip tooltip) {
        this.textArea.setTooltip(tooltip);
    }
    
    public String getText() {
        return this.textArea.getText();
    }
    
    public void setText(final String stringValue) {
        this.textArea.setText(stringValue);
    }

    public void setEditable(final boolean enabled) {
        this.textArea.setEditable(enabled);
    }
    
    public void setCaratPosition(int position) {
        this.textArea.setCaretPosition(position);
    }

    public void setPrefRowCount(Integer suggestedHeight) {
        textArea.setPreferedRowCount(suggestedHeight);
    }
    
    protected void hideSecret(){
        textArea.hide();
    }
    
    protected void showSecret(){
        textArea.reveal();
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Value Modification & Validation Declaration">  
    /**
     * Gets the value that this input field represents;
     * @return 
     */    
    public abstract T getValue();
    
    /**
     * Sets the value that this input field represents
     * @param value 
     */
    public abstract void setValue(T value);
    
    /**
     * Determine if the provided text is a valid value for the input field.
     * Is implemented differently for different input fields.
     * 
     * @return 
     */
    public abstract boolean isValid();
    // </editor-fold>  
    
    // <editor-fold defaultstate="collapsed" desc="ContextMenuContributor Implementation"> 
    @Override
    public final List<MenuItem> getAllMenuItems() {
        List<MenuItem> list = new ArrayList<>();
        List<MenuItem> local = this.getLocalMenuItems();
        list.addAll(local);
        if (!local.isEmpty()){
            list.add(new SeparatorMenuItem());
        }
        list.addAll(this.textArea.getAllMenuItems());
        return list;
    }
    // </editor-fold>  
    
    // <editor-fold defaultstate="collapsed" desc="DropDown Decleration & Functionality">    
    /**
     * Displays the provided ConstellationInputDropDown to the user.
     * This functionality has been captured in the base class intentionally to consistency across 
     * all input fields regarding context displaying 
     * 
     * @param menu 
     */
    protected final void showDropDown(ContextMenu menu){
        menu.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if (event.getCode() == KeyCode.SPACE){
                event.consume();
            }
        });
        menu.show(this, Side.TOP, USE_PREF_SIZE, USE_PREF_SIZE);
    }
    
    public abstract ContextMenu getDropDown();

    private void setValid(boolean isValid) {
        if (isValid){
            background.setFill(fieldColor);
        } else {
            background.setFill(invalidColor);
        }
    }
    
    /**
     * An extension of a ContextMenu to provide features that enable its use as a drop down menu in ConstellationInputFields
     */
    public class ConstellationInputDropDown extends ContextMenu {
        final ConstellationInputField parent;
        public ConstellationInputDropDown(final ConstellationInputField field) {
            parent = field;
            
            //Constrain drop down menus to a height of 400
            this.setMaxHeight(200);
            this.setWidth(parent.getWidth());
            addEventHandler(Menu.ON_SHOWING, e -> {
                Node content = getSkin().getNode();
                if (content instanceof Region region) {
                    region.setMaxHeight(getMaxHeight());
                }
            });
            this.setAutoFix(true);
        }
        
        /**
         * Takes a {@link Labeled} object and converts it to a {@link MenuItem}.
         * 
         * The  {@link MenuItem} is not added to the ConstextMenu. it is returned for
         * Input field specific modification before being added to the COntext menu using
         *  addMenuItems();
         * 
         * This build method is important to correctly bind the width of the context menu to the parnte (but it currently doesnt work as they are bote rea only)....
         * 
         * @param text
         * @return 
         */
        public CustomMenuItem buildCustomMenuItem(Labeled text) {
            //text.prefWidthProperty().bind(parent.prefWidthProperty());
            CustomMenuItem item = new CustomMenuItem(text);
            return item;
        }
        
        /**
         * 
         * @param items 
         */
        public void addMenuItems(List<MenuItem> items){
            this.getItems().addAll(items);
        }
        
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="InfoWindow Functionality">  
    protected final void insertInfoWindow(InfoWindow window) {
        if (window != null) {
            int windowIndex = interactableContent.getChildren().indexOf(textArea) + 1;
            interactableContent.getChildren().add(windowIndex, window);
        }
    }
    
    protected boolean hasInfoWindow() {
        return interactableContent.getChildren().stream().anyMatch(node -> node instanceof InfoWindow);
    }
    
    protected void removeInfoWindow(InfoWindow window) {
        interactableContent.getChildren().remove(window);
    }
    // </editor-fold>
    
    public void setContextButtonDisable(boolean b) {
        //to do
        //want to reformat the grid pane to eliminate the context menu when button is disabled. this will be tricky
    }
    
    public void setInFocus(boolean focused){
        if (focused) {
            foreground.setStroke(Color.web("#1B92E3"));
        } else {
            foreground.setStroke(null);
        }
    }        
}
