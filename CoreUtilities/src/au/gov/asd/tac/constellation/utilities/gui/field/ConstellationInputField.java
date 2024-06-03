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
import au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputFieldConstants.LayoutConstants;
import au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputFieldConstants.TextType;
import au.gov.asd.tac.constellation.utilities.text.SpellChecker;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import org.apache.commons.lang3.StringUtils;


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
public abstract class ConstellationInputField<T> extends StackPane implements ObservableValue<T>, ChangeListener<Serializable>, ContextMenuContributor{   
    
    final int endCellPrefWidth = 50;
    final int endCellMinWidth = 50;
    final int centerCellPrefWidth = 200;
    final int centerCellMinWidth = 100;
    final int defaultCellHeight = 22;
    
    final ColumnConstraints leftConstraint = new ColumnConstraints(50);

    final ColumnConstraints rightConstraint = new ColumnConstraints(50);

    final ColumnConstraints centerConstraint = new ColumnConstraints(100, 400, 500);

    final ColumnConstraints doubleConstraint = new ColumnConstraints(100, 450, 550);

    final ColumnConstraints trippleConstraint = new ColumnConstraints(100, 500, 600);
    
    private GridPane gridPane;
    private ConstellationTextArea textArea;
    private final Label leftLabel = new Label();
    private final Label rightLabel = new Label();
    protected final Rectangle rightButton = new Rectangle(endCellPrefWidth, defaultCellHeight); 
    protected final Rectangle leftButton = new Rectangle(endCellPrefWidth, defaultCellHeight); 
    protected final List<ChangeListener> InputFieldListeners = new ArrayList<>();
    private Rectangle foreground;
        
    final int corner = 7;
    
    final Color optionColor = Color.color(97/255D, 99/255D, 102/255D);
    final Color fieldColor = Color.color(51/255D, 51/255D, 51/255D);
    final Color buttonColor = Color.color(25/255D, 84/255D, 154/255D);
   
    public ConstellationInputField(){
        throw new UnsupportedOperationException();
    }
    
    public ConstellationInputField(final LayoutConstants layout){
        this(layout, TextType.SINGLELINE);
    }
    
    public ConstellationInputField(final LayoutConstants layout, final TextType type) {
        textArea = new ConstellationTextArea(this, type);
        gridPane = getGridPaneWithChildCellPanes(layout);
        
        //Npt a nice sollution but need to run this later as the base classes neet to actualy exist to access their implementation
        Platform.runLater(() -> {
            rightButton.setOnMouseClicked(getRightButtonEventImplementation());
            leftButton.setOnMouseClicked(getLeftButtonEventImplementation());
        });
        
        this.setPrefWidth(500);
        this.setMinWidth(200);
        
        final Rectangle clippingMask = new Rectangle(300, defaultCellHeight);
        clippingMask.setArcWidth(corner);
        clippingMask.setArcHeight(corner);        
        clippingMask.setFill(Color.BLACK);
        clippingMask.setStroke(Color.BLACK);
        clippingMask.widthProperty().bind(gridPane.widthProperty());
        
        final Rectangle background = new Rectangle(300, defaultCellHeight);
        background.setArcWidth(corner);
        background.setArcHeight(corner);  
        background.setFill(fieldColor);
        background.widthProperty().bind(gridPane.widthProperty());
        
        foreground = new Rectangle(300, defaultCellHeight);
        foreground.setArcWidth(corner);
        foreground.setArcHeight(corner);        
        foreground.setFill(Color.TRANSPARENT);
        foreground.setMouseTransparent(true);
        foreground.widthProperty().bind(gridPane.widthProperty());
        
        for (final ContentDisplay area : layout.getAreas()) {
            if (null != area) switch (area) {
                case LEFT -> gridPane.add(this.getEndCellGroup(area, optionColor, leftLabel), 0, 0);
                case RIGHT -> gridPane.add(this.getEndCellGroup(area, layout.hasButton() ? buttonColor : optionColor, rightLabel), layout.getAreas().length - 1, 0);
                case CENTER -> {
                    insertBaseFieldIntoGrid(textArea);
                }
                default -> {
                    //Do Nothing
                }
            }
        }
        textArea.bindHeightProperty(background);
        textArea.bindHeightProperty(foreground);
        textArea.bindHeightProperty(clippingMask);
        gridPane.setClip(clippingMask);
        gridPane.setAlignment(Pos.CENTER);
        this.getChildren().addAll(background, gridPane, foreground);
        this.setAlignment(Pos.TOP_LEFT);
        
        final ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().clear();
        contextMenu.getItems().addAll(this.getAllMenuItems());
        // Set the right click context menu items
        // we want to update each time the context menu is requested 
        // can't make a new context menu each time as this event occurs after showing
        textArea.setContextMenuRequestedEvent(value -> {
            contextMenu.getItems().clear();
            contextMenu.getItems().addAll(this.getAllMenuItems());
            textArea.setContextMenu(contextMenu);
        });
        textArea.setContextMenu(contextMenu);
    }
    
    public final void insertBaseFieldIntoGrid(final ConstellationTextArea field) {
        for (final Node node : gridPane.getChildren()) {
            if (!ContentDisplay.LEFT.toString().equals(node.getId()) && !ContentDisplay.RIGHT.toString().equals(node.getId())) {
                gridPane.add(field, GridPane.getColumnIndex(node), GridPane.getRowIndex(node));
                if (ContentDisplay.CENTER.toString().equals(node.getId())) {
                    gridPane.getChildren().remove(node);
                }
                break;
            }
        }
    }
    
    private Pane getEndCellGroup(final ContentDisplay side, final Color color, final Label label) {
        final StackPane content = new StackPane();
        content.setId(side.toString());
        final Rectangle background = switch (side) {
            case LEFT -> leftButton;
            case RIGHT -> rightButton;
            default -> new Rectangle(); // never used but a nioce way to get rid of errors
        };
        
        background.setFill(color);
        background.setOnMouseEntered(event -> background.setFill(color.brighter()));
        background.setOnMouseExited(event -> background.setFill(color));
        textArea.bindHeightProperty(background);

        label.setMouseTransparent(true);
        label.setPrefWidth(endCellPrefWidth);
        label.setAlignment(Pos.CENTER);

        content.getChildren().addAll(background, label);
        return content;
    }
    
    protected void addToGridCellGroup(final ContentDisplay groupID, final Node item) {
        for (final Node node : gridPane.getChildren()) {
            if (groupID.toString().equals(node.getId())) {
                ((Pane) node).getChildren().add(item);
            }
        }
    }

    /**
     * Constructs a grid pane according to the layout provided. 
     * the grid pane will have the number of rows equal to the layouts number of areas.
     * The
     * @param layout
     * @return 
     */
    private GridPane getGridPaneWithChildCellPanes(final LayoutConstants layout) {
        final GridPane local = new GridPane();
               
        final ContentDisplay[] areas = layout.getAreas();
        for (final ContentDisplay area : areas) {
            switch (area) {
                case LEFT -> local.getColumnConstraints().add(leftConstraint);
                case CENTER -> {
                    switch (areas.length) {
                        case 1 -> local.getColumnConstraints().add(trippleConstraint);
                        case 2 -> local.getColumnConstraints().add(doubleConstraint);
                        case 3 -> local.getColumnConstraints().add(centerConstraint);   
                    }
                }
                case RIGHT -> local.getColumnConstraints().add(rightConstraint);
                default -> {
                    //Do Nothing
                }
            }
        }

        for (int i = 0 ; i< areas.length ; i++) {
            final Pane group = new Pane();
            group.setId(areas[i].toString());
            local.add(group, i, 0);
        }
        
        return local;
    }
    
    public void setRightLabel(final String label) {
        this.rightLabel.setText(label);
    };
    
    public void setLeftLabel(final String label) {
        this.leftLabel.setText(label);
    };
    
    // <editor-fold defaultstate="collapsed" desc="ChangeListener Interface Support">
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
                textArea.setValid(true);
                notifyListeners(getValue());
            } else {
                textArea.setValid(false);
            }
        }
        
        //Boolean Changes are changs to the focused propert of the ConstellationTextArea
        if (newValue instanceof Boolean focused){
            this.setInFocus(focused);
        }
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="ObservableValue Interface Support">   
    public void notifyListeners(T newValue){
        for (ChangeListener listener : InputFieldListeners){
            listener.changed(this, null, newValue);
        }
    }
    
    @Override
    public void addListener(ChangeListener listener) {
        this.InputFieldListeners.add(listener);
    }

    @Override
    public void removeListener(ChangeListener listener) {
        this.InputFieldListeners.remove(listener);
    }

    @Override
    public void addListener(InvalidationListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    // </editor-fold>   
    
    // <editor-fold defaultstate="collapsed" desc="Shortcut Support">   
    /**
     * Registers shortcut keys specific to each input field implementation. 
     * Things like up and down arrows are examples of a shortcut that may be handed differently per field.
     * @param <E>
     * @param eventType
     * @param eventFilter 
     */
    protected final <E extends Event> void addShortcuts(final EventType<E> eventType, final EventHandler<E> eventFilter) {
        this.textArea.addEventFilter(eventType, eventFilter);
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="ConstelationTextArea Modification Methods">
        /**
     *
     * @param event
     */
    public void registerTextClickedEvent(final EventHandler<MouseEvent> event){
        this.textArea.primaryInputSetOnMouseClicked(event);        
    }
    
    /**
     *
     * @param event
     */
    public void registerTextKeyedEvent(final EventHandler<KeyEvent> event){
        this.textArea.primaryInputSetOnKeyReleased(event); 
    }
    
    public void clearTextClickedEvent(){
        this.textArea.primaryInputSetOnMouseClicked(null);        
    }
    
    public void clearTextKeyedEvent(){
        this.textArea.primaryInputSetOnKeyReleased(null);
    }
    public void setPromptText(final String description) {
        this.textArea.setPromptText(description);
    }

    public void setText(final String stringValue) {
        this.textArea.setText(stringValue);
    }

    public void setEditable(final boolean enabled) {
        this.textArea.setEditable(enabled);
    }

    public String getText() {
        return this.textArea.getText();
    }

    public void setTooltip(Tooltip tooltip) {
        this.textArea.setTooltip(tooltip);
    }

    public void selectAll() {
        this.textArea.selectAll();
    }

    public void selectBackward() {
        this.textArea.selectBackward();
    }

    public void selectForward() {
        this.textArea.selectForward();
    }

    public void previousWord() {
        this.textArea.previousWord();
    }

    public void nextWord() {
        this.textArea.nextWord();
    }

    public void selectPreviousWord() {
       this.textArea.selectPreviousWord();
    }

    public void selectNextWord() {
        this.textArea.selectNextWord();
    }

    public void deleteText(final IndexRange selection) {
        this.textArea.deleteText(selection);
    }

    public void deleteNextChar() {
        this.textArea.deleteNextChar();
    }

    public IndexRange getSelection() {
        return this.textArea.getSelection();
    }
    
    public void setWrapText(boolean wrapText) {
        this.textArea.setWrapText(wrapText);
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

    public void clearTextStyles() {
        textArea.clearStyles();
    }



    public void highlightText(int start, int end) {
        textArea.highlightText(start, end);
    }

    public int getCaretPosition() {
        return textArea.getCaretPosition();
    }
    
    public boolean isWordUnderCursorHighlighted() {
        return textArea.isWordUnderCursorHighlighted(getCaretPosition() -1);
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="SpellCheck Support">   
    public void enableSpellCheck(boolean spellCheckEnabled) {
        if (spellCheckEnabled){
            SpellChecker.registerArea(this);
        } else {
            SpellChecker.deregisterArea(this);
        }
    }
    //</editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Value Modification & Validation Declaration">  
    /**
     * Sets the value that this input field represents
     * @param value 
     */
    public abstract void setValue(T value);
    
    /**
     * Gets the value that this input field represents;
     * @return 
     */    
    @Override
    public abstract T getValue();
    
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
    
    // <editor-fold defaultstate="collapsed" desc="Button Event Declaration">  
    public abstract EventHandler<MouseEvent> getRightButtonEventImplementation();
    
    public abstract EventHandler<MouseEvent> getLeftButtonEventImplementation();
    // </editor-fold>  
    
    // <editor-fold defaultstate="collapsed" desc="DropDown Decleration & Functionality">   
    
    /**
     * Displays the provided ConstellationInputDropDown to the user.
     * This functionality has been captured in the base class intentionally to consistency across 
     * all input fields regarding context displaying 
     * 
     * @param menu 
     */
    protected final void showDropDown(){
        getDropDown().show(this, Side.TOP, USE_PREF_SIZE, USE_PREF_SIZE);
    }
    
    public abstract ContextMenu getDropDown();
    
    /**
     * An extension of a ContextMenu to provide features that enable its use as a drop down menu in ConstellationInputFields
     */
    public class ConstellationInputDropDown extends ContextMenu {
        final ConstellationInputField parent;
        public ConstellationInputDropDown(final ConstellationInputField field) {
            parent = field;
            
            //Constrain drop down menus to a height of 400
            this.setMaxHeight(200);
            addEventHandler(Menu.ON_SHOWING, e -> {
                Node content = getSkin().getNode();
                if (content instanceof Region region) {
                    region.setMaxHeight(getMaxHeight());
                }
            });
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
            text.prefWidthProperty().bind(parent.prefWidthProperty());
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
    
    
    
    public void setContextButtonDisable(boolean b) {
        //to do
        //want to reformat the grid pane to eliminate the context menu when button is disabled. this will be tricky
    }
    
    
    
    public boolean isEmpty(){
        return this.getText().isBlank();
    }
    
    public void setInFocus(boolean focused){

        if (focused) {
            foreground.setStroke(Color.web("#1B92E3"));
        } else {
            foreground.setStroke(null);
        }

    }
    
    
    public void setLines(Integer suggestedHeight) {
        Platform.runLater(() -> {
                final Text t = (Text) textArea.lookup(".text");
                if (t != null) {
                    this.textArea.setPrefHeight(suggestedHeight * t.getBoundsInLocal().getHeight() + 3);
                }
            });
    }
        
    //Move this class, concider maving to specific field like the choice input
    //has been deactivated for text/value input atm
    public void autoComplete(final List<String> suggestions) {
            final Popup popup = new Popup();
            popup.setWidth(textArea.getWidth());
            final ListView<String> listView = new ListView<>();
            listView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue != null) {
                    textArea.setText​(newValue);
                }
                popup.hide();
            });

            textArea.setOnKeyTyped((final javafx.scene.input.KeyEvent event) -> {
                final String input = textArea.getText();
                popup.hide();
                popup.setAutoFix(true);
                popup.setAutoHide(true);
                popup.setHideOnEscape(true);
                popup.getContent().clear();
                listView.getItems().clear();

                if (StringUtils.isNotBlank(input) && !suggestions.isEmpty()) {
                    final List<String> filteredSuggestions = suggestions.stream()
                            .filter(suggestion -> suggestion.toLowerCase().startsWith(input.toLowerCase()) && !suggestion.equals(input))
                            .collect(Collectors.toList());
                    listView.setItems(FXCollections.observableArrayList(filteredSuggestions));

                    popup.getContent().add(listView);

                    Platform.runLater(() -> {
                        final ListCell<?> cell = (ListCell<?>) listView.lookup(".list-cell");
                        if (cell != null) {
                            listView.setPrefHeight(listView.getItems().size() * cell.getHeight() + 3);
                        }
                    });

                    // Show the popup under this text area
                    if (!listView.getItems().isEmpty()) {
                        popup.show(textArea, textArea.localToScreen(0, 0).getX(), textArea.localToScreen(0, 0).getY() + textArea.heightProperty().getValue());
                    }
                }
            });
        }
        
}
