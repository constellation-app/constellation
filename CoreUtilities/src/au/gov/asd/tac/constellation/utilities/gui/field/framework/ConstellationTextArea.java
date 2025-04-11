/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.utilities.gui.field.framework;

import au.gov.asd.tac.constellation.utilities.gui.context.ContextMenuContributor;
import static au.gov.asd.tac.constellation.utilities.gui.field.framework.ConstellationInputConstants.TextType.MULTILINE;
import static au.gov.asd.tac.constellation.utilities.gui.field.framework.ConstellationInputConstants.TextType.SECRET;
import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import java.util.Arrays;
import java.util.List;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

/**
 * This class represents the area of text that users can interact with inside of
 * a {@link ConstellationInputField}.
 *
 * The {@link ConstellationTextArea} has been designed to provide a minimal
 * interface to the {@link ConstellationIputField} with the intention of
 * simplifying its use and protecting the integrity of the data it stores from
 * uncontrolled manipulation.
 *
 * To assist with the protection of data within this class, the class and
 * methods are declared private with the class being also declared final.
 *
 * The {
 *
 * @linkConstellationTextArea itself} is a HBox that contains children of type
 * {@link TextInputControll} In almost all cases, the ConstellationText Area
 * will only have one child, a {@link TextArea} or a {@link TextField}. In cases
 * where the input field needs to be secret (have the characters hidden) a
 * primary input of {@link PasswordField} and a secondary input of
 * {@link TextField} will be used to facilitate hiding and showing of the hidden
 * text.
 *
 * the raw inputs can still be grabbed by using the get children methods. is
 * this an issue / vulnerability?
 *
 * @author capricornunicorn123
 */
public final class ConstellationTextArea extends StackPane implements ContextMenuContributor {

    private final Insets insets = new Insets(4, 8, 4, 8);
    private List<MenuItem> menuItems = null;

    private final TextInputControl primaryInput;
    private final TextInputControl secondaryInput;

    public ConstellationTextArea(final ConstellationInput parent, final ConstellationInputConstants.TextType type) {
        switch (type) {
            case MULTILINE -> {
                final TextArea area = new TextArea();

                //Set the next focus event to TAB instead of CTRL + TAB.
                area.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
                    if (event.getCode() == KeyCode.TAB && !event.isControlDown() && !event.isShiftDown() && !event.isMetaDown()) {
                        final KeyEvent newEvent = new KeyEvent(
                                event.getSource(), event.getTarget(),
                                KeyEvent.KEY_PRESSED,
                                "", "", KeyCode.TAB,
                                false, true, false, false
                        );
                        Event.fireEvent(event.getTarget(), newEvent);
                        event.consume();
                    }
                });

                primaryInput = area;
            }
            case null, default -> {
                final TextField field = new TextField();
                field.setPadding(insets);

                //The up down arrows allow for navigation to the begining and start of a line
                //This is being remapped to ALT + left and ALT + right for consistency between textArea and textField
                field.addEventFilter(KeyEvent.KEY_PRESSED, (final KeyEvent event) -> {
                    if ((event.getCode() == KeyCode.UP || event.getCode() == KeyCode.UP) && !event.isAltDown()) {
                        event.consume();
                    }
                });

                primaryInput = field;
            }
        }
        //Set up the primary InputControl
        primaryInput.textProperty().addListener(parent);
        primaryInput.focusedProperty().addListener(parent);
        primaryInput.getStylesheets().addAll(JavafxStyleManager.getMainStyleSheet());

        // Set up the optional secondary InputControl
        if (type == SECRET) {
            // SecondaryInputs are only used in Secret Inputs and have bound properties with the primary input
            secondaryInput = new PasswordField();
            secondaryInput.textProperty().bindBidirectional(primaryInput.textProperty());
            secondaryInput.textFormatterProperty().bindBidirectional(primaryInput.textFormatterProperty());
            secondaryInput.promptTextProperty().bindBidirectional(primaryInput.promptTextProperty());
            secondaryInput.styleProperty().bind(primaryInput.styleProperty());
            secondaryInput.focusedProperty().addListener(parent);
            primaryInput.setVisible(false);

            getChildren().addAll(primaryInput, secondaryInput);
        } else {
            secondaryInput = null;
            //The up down arrows allow for navigation to the begining and start of a line
            //This is being remappedt o ALT + left and ALT + right for consistency between textArea and textField
            primaryInput.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {

                // Navigate to extremem ends of line
                if (event.getCode() == KeyCode.LEFT && event.isAltDown()) {
                    primaryInput.positionCaret(0);
                    event.consume();
                }

                if (event.getCode() == KeyCode.RIGHT && event.isAltDown()) {
                    primaryInput.positionCaret(getText().length());
                    event.consume();
                }
            });
            getChildren().add(primaryInput);
        }
    }

    public boolean isInFocus() {
        return primaryInput.isFocused();
    }

    // <editor-fold defaultstate="collapsed" desc="Local Private Methods"> 
    /**
     * Binds the heightProperty of a Rectangle to the height property of the
     * {@link TextInputControl}.
     *
     * @param bindables
     */
    public void bindHeightProperty(final Rectangle... bindables) {
        for (final Rectangle bindable : bindables) {
            bindable.heightProperty().bind(primaryInput.heightProperty());
        }
    }

    /**
     * Sets the prompt text of the {@link TextInputControl}.
     *
     * @param promptText
     */
    public final void setPromptText(final String promptText) {
        primaryInput.setPromptText(promptText);
    }

    /**
     * Sets the text value of the {@link TextInputControl}.
     *
     * @param stringValue
     */
    public void setText(final String stringValue) {
        if (stringValue != null) {
            primaryInput.setText(stringValue);
            primaryInput.positionCaret(stringValue.length());
        }
    }

    public void setContextMenu(final ContextMenu menu) {
        primaryInput.setContextMenu(menu);
    }

    /**
     * Specifies if the {@link TextInputControl} is editable
     *
     * @param enabled
     */
    public void setEditable(final boolean enabled) {
        primaryInput.setEditable(enabled);
    }

    public String getText() {
        return primaryInput.getText();
    }

    public void setTooltip(final Tooltip tooltip) {
        Tooltip.install(primaryInput, tooltip);
    }

    public void setPreferedRowCount(final Integer suggestedRowCount) {
        if (primaryInput instanceof TextArea textAreaField) {
            textAreaField.setPrefRowCount(suggestedRowCount);
            //For some reason the text area grows in height with key presses, so ensure the max height has been set.
            textAreaField.setMaxHeight(suggestedRowCount * 16);
        }
    }

    public void hide() {
        if (secondaryInput != null) {
            primaryInput.setVisible(false);
            secondaryInput.setVisible(true);
        } else {
            throw new UnsupportedOperationException("Only ConstellationTextAreas of TextType.SECRET can be hidden");
        }
    }

    public void reveal() {
        if (secondaryInput != null) {
            primaryInput.setVisible(true);
            secondaryInput.setVisible(false);
        } else {
            throw new UnsupportedOperationException("Only ConstellationTextAreas of TextType.SECRET can be revealed");
        }
    }

    public int getCaretPosition() {
        return primaryInput.getCaretPosition();
    }

    public void setCaretPosition(final int position) {
        primaryInput.positionCaret(position);
    }

    // <editor-fold defaultstate="collapsed" desc="ContextMenuContributor Implementation"> 
    @Override
    public List<MenuItem> getAllMenuItems() {
        return getLocalMenuItems();
    }

    @Override
    public List<MenuItem> getLocalMenuItems() {
        if (menuItems == null) {
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

    public void setContextMenuRequestedEvent(final EventHandler<? super ContextMenuEvent> value) {
        primaryInput.setOnContextMenuRequested(value);
    }

    public void primaryInputSetOnMouseClicked(final EventHandler<MouseEvent> event) {
        primaryInput.setOnMouseClicked(event);
    }

    public void primaryInputSetOnKeyReleased(final EventHandler<KeyEvent> event) {
        primaryInput.setOnKeyReleased(event);
    }
}
