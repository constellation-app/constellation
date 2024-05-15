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

import au.gov.asd.tac.constellation.utilities.gui.field.ChoiceInputField.ChoiceType;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;


/**
 * This class is the base class for all input fields in Constellation. 
 * Inputs have been developed to adhere to a strict format that is highly adaptable to different use cases.
 * To achieve this adaptive layout a number fo nuanced javaFX scructures have been integrsted into the input field 
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
 * Buttons that initiate a context menu or modify the value or aperance of the text area should be grey.
 * Buttons that initiate a pop-up window shall be blue.
 * Buttons that initiate a pop-up window shall always be positioned on the right.
 * 
 * The construction of the class is as follows 
 *  StackPane
 *      Shape - A rounded rectangle acting as the background
 *      GridPane
 *          for each cell
 *              Group
 *                  cell contents
 *      Shape - A rounded transparent rectangle acting as the border
 * 
 * 
 * @author capricornunicorn123
 */
public abstract class ConstellationInputField extends StackPane {
    
    //types of button
    //Cinotext trigger
    //pop up trigger
    //valueupdater
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
    private TextInputControl field;
    private final Label leftLabel = new Label();
    private final Label rightLabel = new Label();
    protected final Rectangle rightButton = new Rectangle(endCellPrefWidth, defaultCellHeight); 
    protected final Rectangle leftButton = new Rectangle(endCellPrefWidth, defaultCellHeight); 
    
    private final ReadOnlyDoubleProperty heightBinding;
        
    final int corner = 7;
    
    final Color optionColor = Color.color(97/255D, 99/255D, 102/255D);
    final Color fieldColor = Color.color(51/255D, 51/255D, 51/255D);
    final Color buttonColor = Color.color(25/255D, 84/255D, 154/255D);
   
    public ConstellationInputField(){
        throw new UnsupportedOperationException();
    }
    
    public ConstellationInputField(final ConstellationInputFieldLayoutConstants layout){
        this(layout, TextType.SINGLELINE);
    }
    
    
    public ConstellationInputField(final ConstellationInputFieldLayoutConstants layout, final TextType type) {
        field = this.createInputField(type);
        this.heightBinding = field.heightProperty();
        
        gridPane = getGridPaneWithChildCellPanes(layout);
        
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
        
        final Rectangle foreground = new Rectangle(300, defaultCellHeight);
        foreground.setArcWidth(corner);
        foreground.setArcHeight(corner);        
        foreground.setFill(Color.TRANSPARENT);
        foreground.setMouseTransparent(true);
        foreground.widthProperty().bind(gridPane.widthProperty());
        this.bindFocusEffect(field, foreground);
        
        for (final ContentDisplay area : layout.getAreas()) {
            if (null != area) switch (area) {
                case LEFT -> gridPane.add(this.getEndCellGroup(ContentDisplay.LEFT, optionColor, leftLabel), 0, 0);
                case RIGHT -> gridPane.add(this.getEndCellGroup(ContentDisplay.RIGHT, layout.hasButton ? buttonColor : optionColor, rightLabel), layout.getAreas().length - 1, 0);
                case CENTER -> {
                    insertBaseFieldIntoGrid(field);
                }
                default -> {
                    //Do Nothing
                }
            }
        }
        
        background.heightProperty().bind(heightBinding);
        foreground.heightProperty().bind(heightBinding);
        clippingMask.heightProperty().bind(heightBinding);
        
        gridPane.setClip(clippingMask);
        gridPane.setAlignment(Pos.CENTER);
        this.getChildren().addAll(background, gridPane, foreground);
        this.setAlignment(Pos.TOP_CENTER);
    }
    
    protected TextInputControl getBaseField() {
        return this.field;
    }
    
    public final void insertBaseFieldIntoGrid(final TextInputControl field) {
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
        background.heightProperty().bind(heightBinding);

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
    private GridPane getGridPaneWithChildCellPanes(final ConstellationInputFieldLayoutConstants layout) {
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

    /**
     * Creates an imput field based off of the text type. 
     * This method is protected as the password input field uses it to create a non secret alternative for password showing and hiding. 
     * 
     * 
     * @param type
     * @return 
     */
    protected final TextInputControl createInputField(final TextType type) {
        
        final TextInputControl local = switch (type) {
            case SECRET -> new PasswordField();
            case MULTILINE -> new TextArea();
            default -> new TextField();
        };
        local.setStyle("-fx-background-radius: 0; -fx-background-color: transparent; -fx-border-color: transparent; -fx-focus-color: transparent;");
        local.textProperty().addListener((observable, oldValue, newValue) -> {
            if (isValid(newValue)){
                local.setStyle("-fx-background-color: transparent;");
            } else {
                local.setStyle("-fx-background-color: red;");
            }
        });
        return local;
        
    }
    
    /** 
     * Adds a listener to the focusedProperty of the base Input field to enable equivalent effects to be produced on the 
     * ConstellationInputField. 
     * 
     * Results in the entire field to display a blue glow effect instead of just the text area.
     * 
     * @param local
     * @param foreground 
     */
    private void bindFocusEffect(final TextInputControl local, final Rectangle foreground) {
        local.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                foreground.setStroke(Color.web("#1B92E3"));
            } else {
                foreground.setStroke(null);
            }
        });
    }
    
    public void setRightLabel(final String label) {
        this.rightLabel.setText(label);
    };
    
    public void setLeftLabel(final String label) {
        this.leftLabel.setText(label);
    };
    
    public void registerRightButtonEvent(final EventHandler<MouseEvent> event) {
        this.rightButton.setOnMouseClicked(event);
    }
    
    public void registerLeftButtonEvent(final EventHandler<MouseEvent> event) {
        this.leftButton.setOnMouseClicked(event);
    }
    
    public void setContextButtonDisable(boolean b) {
        //to do
        //want to reformat the grid pane to eliminate the context menu when button is disabled. this will be tricky
    }

    public void setPromptText(final String description) {
        this.field.setPromptText(description);
    }

    public void setText(final String stringValue) {
        this.field.setText(stringValue);
    }

    public void setEditable(final boolean enabled) {
        this.field.setEditable(enabled);
    }

    public StringProperty textProperty() {
        return this.field.textProperty();
    }

    public String getText() {
        return this.field.getText();
    }

    public void setTooltip(Tooltip tooltip) {
        this.field.setTooltip(tooltip);
    }

    public void selectAll() {
        this.field.selectAll();
    }

    public void selectBackward() {
        this.field.selectBackward();
    }

    public void selectForward() {
        this.field.selectForward();
    }

    public void previousWord() {
        this.field.previousWord();
    }

    public void nextWord() {
        this.field.nextWord();
    }

    public void selectPreviousWord() {
       this.field.selectPreviousWord();
    }

    public void selectNextWord() {
        this.field.selectNextWord();
    }

    public void deleteText(final IndexRange selection) {
        this.field.deleteText(selection);
    }

    public void deleteNextChar() {
        this.field.deleteNextChar();
    }

    public IndexRange getSelection() {
        return this.field.getSelection();
    }
    
    public void setWrapText(boolean wrapText) {
        if (this.field instanceof TextArea textAreaField){
            textAreaField.setWrapText(wrapText);
        }
    }

    public void setPrefRowCount(Integer suggestedHeight) {
               if (this.field instanceof TextArea textAreaField){
            textAreaField.setPrefRowCount(suggestedHeight);
        }
    }
    
    public boolean isEmpty(){
        return this.getText().isBlank();
    }
    
    /**
     * Determined if the provided text is a valid value for the input field.
     * Is implemented differently for different input fields.
     * 
     * @param value
     * @return 
     */
    public abstract boolean isValid(String value);
    
    public abstract ContextMenu getDropDown();
    
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
        
        public void addMenuOption(Labeled text) {
            text.prefWidthProperty().bind(parent.prefWidthProperty());
            CustomMenuItem item = new CustomMenuItem(text);
            
            if (parent instanceof ChoiceInputField field && field.getType() == ChoiceType.MULTI){
                item.setHideOnClick(false);
            }
            
            this.getItems().add(item);
        }
        
        public void addSeparator(){
            this.getItems().add(new SeparatorMenuItem());
        }
        
    }
    
    /**
     * A representation of the different layouts that a ConstellationInputField can take. 
     * INPUT represents the input area of the field.
     * DROPDWN represents a button that triggers a drop down menu on the field. 
     * POPUP represents a button that triggers a pop up window.     
     * UPDATER represents a button that updates the value of the Field when pressed.
     * 
     * The combination of these representative words represents their order in the ConstellationInputField
     */
    public enum ConstellationInputFieldLayoutConstants {        
        INPUT(false, ContentDisplay.CENTER),
        INPUT_DROPDOWN(false, ContentDisplay.CENTER, ContentDisplay.RIGHT),
        INPUT_POPUP(true, ContentDisplay.CENTER, ContentDisplay.RIGHT),
        DROPDOWN_INPUT_POPUP(true, ContentDisplay.LEFT, ContentDisplay.CENTER, ContentDisplay.RIGHT),
        UPDATER_INPUT_UPDATER(false, ContentDisplay.LEFT, ContentDisplay.CENTER, ContentDisplay.RIGHT);
        
        private final ContentDisplay[] areas;
        private final boolean hasButton;

        private ConstellationInputFieldLayoutConstants(final boolean hasButton, final ContentDisplay... areas) {
            this.areas = areas;
            this.hasButton = hasButton;
        }
        
        public ContentDisplay[] getAreas() {
            return this.areas;
        }
    }
    
    public enum TextType {
        SECRET,
        SINGLELINE,
        MULTILINE;
    }
}
