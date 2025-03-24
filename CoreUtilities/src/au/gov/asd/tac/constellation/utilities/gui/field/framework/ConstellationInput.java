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
import au.gov.asd.tac.constellation.utilities.gui.field.MultiChoiceInput;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.ConstellationInputConstants.TextType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.InfoWindowSupport.InfoWindow;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.LeftButtonSupport.LeftButton;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.RightButtonSupport.RightButton;
import com.fasterxml.jackson.core.io.NumberInput;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.TextInputControl;
import javafx.scene.effect.ColorInput;

/**
 * An Abstract base class for all inputs in Constellation.
 *
 * Input fields follow a magic-wand-like layout with four main sections:
 * <ol>
 * <li>{@link ConstellationInputButton} - LeftButton Optional</li>
 * <li>{@link ConstellationTextArea} - Interactable Area</li>
 * <li>{@link InfoWindow} - Interactable Area</li>
 * <li>{@link ConstellationInputButton} - RightButton Optional</li>
 * </ol>
 +--------------------------------------------+ 
 | ConstellationInputButton | Input Area | ConstellationInputButton
 | +--------------------------------------------+ 
 A central input area and two
 end buttons. in the case that 1 or both end button is not needed it can be
 removed and the subsequent Input Area shall adapt to fill the missing space.
 +--------------------------------------------+ 
 | Input Area | ConstellationInputButton |
 +--------------------------------------------+

 Current usage of these inputs is limited to {@link PluginParameterPane} and
 * the {@link PluginReporterPane} but should be slowly integrated into all views
 * and windows.
 *
 * Supported Input Fields:
 * <ul>
 * <li>{@link ColorInput}</li>
 * <li>{@link DateInput}</li>
 * <li>{@link FileInput}</li>
 * <li>{@link MultiChoiceInput}</li>
 * <li>{@link SingleChoiceInput}</li>
 * <li>{@link TextInput}</li>
 * <li>{@link NumberInput}</li>
 * </ul>
 *
 * Input Fields yet to be supported:
 * <ul>
 * <li>DateRangeInput</li>
 * <li>BooelanInput</li>
 * <li>ActionInput</li>
 * </ul>
 *
 * Inputs should be developed and maintained with the following considerations:
 * <ul>
 * <li>Independence from third party providers</li>
 * <li>Visual Consistency</li>
 * <li>Adaptability to various use cases</li>
 * <li>Simplicity</li>
 * <li>Adaptability to various use cases</li>
 * </ul>
 *
 * The following interfaces have been developed to support input functionality
 * <ul>
 * <li>{@link AutoCompleteSupport}</li>
 * <li>{@link InfoWindowSupport}</li>
 * <li>{@link LeftButtonSupport}</li>
 * <li>{@link RightButtonSupport}</li>
 * <li>{@link ShortcutSupport}</li>
 * </ul>
 *
 * When designing an extension of an input field the following considerations
 * should be made:
 * <ul>
 * <li>For single button input fields, the button shall be placed on the Right.
 * </li>
 * <li>Buttons that initiate a context menu or modify the value or appearance of
 * the text area should be gray.</li>
 * <li>Buttons that initiate a pop-up window shall be blue.</li>
 * <li>Buttons that initiate a pop-up window shall always be positioned on the
 * right.</li>
 * </ul>
 *
 * //TODO: Lock down the Text property on all input fields. this includes
 * getText get stringValue etc. there should only be 1 way in and out of the
 * input field for selection data and that is getValue() and setValue(). this is
 * for the benefit of password input field but also benefits others
 *
 * The Text of the input field should be taken as the source of truth for the
 * current value. Implementations of ConstellationInput fields may implement
 * their own back end data structures and objects to simplify the text value.
 *
 * @author capricornunicorn123
 * @author andromeda-224
 */
public abstract class ConstellationInput<T> extends StackPane implements
        ChangeListener<Serializable>, ContextMenuContributor {

    private int inferredCaretPos = 0;
    private boolean updatingText = false;
    
    /**
     * @return the menuShown
     */
    public boolean isMenuShown() {
        return menuShown;
    }

    /**
     * @param menuShown the menuShown to set
     */
    public void setMenuShown(final boolean menuShown) {
        this.menuShown = menuShown;
    }

    private static final int DEFAULT_CELL_HEIGHT = 22;
    private static final int BUTTON_VISIBILITY_WIDTH_THRESHOLD = 200;
    private static final int CORNER = 7;

    private final ConstellationTextArea textArea;

    protected final List<ConstellationInputListener> inputFieldListeners = new ArrayList<>();
    private final List<InputValidator> validators = new ArrayList<>();
    
    private boolean isValid = true;
    private boolean focused = false;
    protected boolean menuShown = false;

    protected ConstellationInput() {
        this(TextType.SINGLELINE);
    }

    protected ConstellationInput(final TextType type) {
        textArea = new ConstellationTextArea(this, type);
        buildInputFieldLayers(textArea);
        HBox.setHgrow(textArea, Priority.ALWAYS);

        setPrefWidth(500);
        setMinWidth(200);
        setMinHeight(DEFAULT_CELL_HEIGHT);
        setAlignment(Pos.TOP_LEFT);

        final ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().clear();
        contextMenu.getItems().addAll(getAllMenuItems());
        // Set the right click context menu items
        // Update each time the context menu is requested.
        // Can't make a new context menu each time as this event occurs after
        // showing
        textArea.setOnContextMenuRequested(value -> {
            contextMenu.getItems().clear();
            contextMenu.getItems().addAll(getAllMenuItems());
            textArea.setContextMenu(contextMenu);
        });
        textArea.setContextMenu(contextMenu);
        textArea.setMinHeight(DEFAULT_CELL_HEIGHT);
        textArea.setPadding(new Insets(0, 5, 0, 0));
    }

    // <editor-fold defaultstate="collapsed" desc="Local Private Methods">
    /**
     * Builds out the layers to a ConstellationInput. Each of these layers needs
     * to have their height bound to the height of a TextArea to ensure
     * appropriate resizing with contained text. There's a Layer that is the
     * interactable content layer a {@link HBox} that hosts all of the
     * "clickable" UI components. This layer need to have a clipping mask that
     * this the same size as the foreground to enable smooth corners. The next
     * layer is the foreground layer a {@link Rectangle} which acts as a border
     * around the input field to provide the focus effect for when the input is
     * selected.
     */
    private void buildInputFieldLayers(final ConstellationTextArea textArea) {
        final HBox interactableContentLayer = new HBox();
        interactableContentLayer.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(interactableContentLayer, Priority.ALWAYS);

        final Rectangle interactableContentClipingMask = new Rectangle();
        interactableContentClipingMask.setArcWidth(CORNER);
        interactableContentClipingMask.setArcHeight(CORNER);
        interactableContentClipingMask.widthProperty().bind(interactableContentLayer.widthProperty());
        interactableContentLayer.setClip(interactableContentClipingMask);

        final Rectangle foregroundLayer = new Rectangle();
        foregroundLayer.setArcWidth(CORNER);
        foregroundLayer.setArcHeight(CORNER);
        foregroundLayer.setFill(Color.TRANSPARENT);
        foregroundLayer.setMouseTransparent(true);
        foregroundLayer.widthProperty().bind(interactableContentLayer.widthProperty());

        textArea.bindHeightProperty(foregroundLayer, interactableContentClipingMask);

        getChildren().addAll(interactableContentLayer, foregroundLayer);
    }

    /**
     * To be called at the end of the constructor for extensions of this class.
     * Builds out all of the implemented input features such as buttons,
     * context, shortcuts and info windows. The framework of allowing each
     * extension of the ConstellationInput to implement its own combination of
     * input features leads to one slightly ugly requirement that this class
     * cannot build its visual aspects until the extended class has finished
     * initializing. If not done this way, we get some ugly errors where methods
     * and objects don't exist...
     */
    protected void initialiseDepedantComponents() {
        final HBox interactableContent = getInteractableContent();

        // Build out the visual Input components on the FX thread.
        Platform.runLater(() -> {
            // Add Left button
            if (this instanceof LeftButtonSupport leftButton) {
                final LeftButton button = leftButton.getLeftButton();
                if (button != null) {
                    interactableContent.getChildren().addFirst(button);
                    createWidthListener(button);
                }
            }

            // Add Base
            interactableContent.getChildren().add(textArea);

            // Add Window
            if (this instanceof InfoWindowSupport infoWindow) {
                insertInfoWindow(infoWindow.getInfoWindow());
            }

            // Add Right ConstellationInputButton
            if (this instanceof RightButtonSupport rightButton) {
                final RightButton button = rightButton.getRightButton();
                interactableContent.getChildren().add(button);
                createWidthListener(button);
            }
        });

        // Add Shortcuts
        if (this instanceof ShortcutSupport shortcut) {
            final EventHandler<KeyEvent> shortcutEvent = shortcut.getShortcuts();
            if (shortcutEvent != null) {
                textArea.addEventFilter(KeyEvent.KEY_PRESSED, shortcut.getShortcuts());
            }
        }

        // AutoComplete
        if (this instanceof AutoCompleteSupport autoComplete) {
            addListener(newValue -> {
                if (textArea.isInFocus()) {
                    final List<MenuItem> suggestions = autoComplete.getAutoCompleteSuggestions();
                    if (suggestions != null && !suggestions.isEmpty()) {
                        final ConstellationInputDropDown menu = new ConstellationInputDropDown(this);
                        menu.getItems().addAll(suggestions);
                        menu.setAutoHide(true);
                        menu.setAutoFix(true);
                        menu.setWidth(textArea.getWidth());
                        //Listen for key events for when arrows are pressed or when to hide the menu
                        addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
                            menu.hide();
                            setMenuShown(false);
                        });
                        showDropDown(menu);
                    }
                }
            });
        }
    }

    /**
     * Creates a generic listener that amends the visibility of a button based
     * on the width of the input field.
     *
     * @param content
     * @param side
     */
    private void createWidthListener(final ConstellationInputButton content) {

        final HBox interactableContent = getInteractableContent();

        //Allow this end cell to listen to the width of the total Input field
        widthProperty().addListener((value, oldValue, newValue) -> {

            //Hide this end cell if the width is too low
            if (newValue.intValue() < BUTTON_VISIBILITY_WIDTH_THRESHOLD) {
                if (content.isVisible()) {
                    interactableContent.getChildren().remove(content);
                }
                content.setVisible(false);

                //Show this end cell if the width is large enough
            } else {
                if (!content.isVisible()) {
                    //Build the components      
                    switch (content) {
                        case LeftButton leftButton ->
                            interactableContent.getChildren().addFirst(leftButton);
                        case RightButton rightButton ->
                            interactableContent.getChildren().addLast(rightButton);
                        default -> {
                            // Do Nothing
                        }
                    }
                }
                content.setVisible(true);
            }
        });
    }

    /**
     * Requests that this InputField be in focus. This does not modify the
     * actual java fx-focus properties, it simply gives the visual and logical
     * appearance of being focused from the perspective of the custom
     * ConstellationInput. classes and methods. If the requested focus state is
     * the same as the current focus state, nothing happens.
     *
     * @param focused
     */
    private void ensureInFocus(final boolean focused) {
        if (this.focused != focused) {
            if (focused) {
                getForegroundShape().setStroke(Color.web("#1B92E3"));
                updateFieldValidityVisuals(true);
            } else {
                getForegroundShape().setStroke(null);
                updateFieldValidityVisuals(isValid);
            }
            this.focused = focused;
        }
    }

    private HBox getInteractableContent() {
        //A clunky way of getting the InteractableCOntent...
        return (HBox) getChildren().stream().filter(HBox.class::isInstance).toList().getFirst();
    }

    private Rectangle getForegroundShape() {
        return (Rectangle) getChildren().getLast();
    }
    
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="ObservableValue and ConstellationInputListener Interface Support">
    /**
     * This method manages the handling of changes to the TextProperty and
     * FocusedProperty of the {@link TextInputControl} used by the
     * {@link ConstellationTextArea}.
     */
    @Override
    public final void changed(final ObservableValue<? extends Serializable> observable, final Serializable oldValue, final Serializable newValue) {

        // String changes are changes to the text value of the ConstellationTextArea
        if (newValue instanceof String newStr) {
            if (!updatingText) {
                // We need to manually determine what the caret position will be after an inline edit,
                // because the standard getCaretPosition() function gives us the position just prior to the edit
                inferredCaretPos = 0;
                if (oldValue instanceof String oldStr) {
                    final int newLen = newStr.length();
                    final int oldLen = oldStr.length();
                    inferredCaretPos = newLen;
                    for (int i = 0; i < newLen && i < oldLen; i++) {
                        if (newStr.charAt(i) != oldStr.charAt(i)) {
                            inferredCaretPos = i + (newLen > oldLen ? 1 : 0);
                            break;
                        }
                    }
                }
            }
            if (isValid()) {
                setValid(true);
                notifyListeners(getValue());
            } else {
                setValid(false);
                notifyListeners(null);
            }
        }

        //Boolean Changes are changs to the focused property of the ConstellationTextArea
        if (newValue instanceof Boolean newFocused) {
            ensureInFocus(newFocused);
            notifyListeners(null);
        }
    }

    /**
     * Notifies any {@link ConstellationInputListener} listening to this input.
     *
     * @param newValue
     */
    public final void notifyListeners(final T newValue) {
        for (final ConstellationInputListener listener : inputFieldListeners) {
            listener.changed(newValue);
        }
    }

    /**
     * Registers a {@link ConstellationInputListener} as a listener of this
     * input.
     *
     * @param listener
     */
    public final void addListener(final ConstellationInputListener listener) {
        inputFieldListeners.add(listener);
    }

    /**
     * Removes a {@link ConstellationInputListener} as a listener of this input.
     *
     * @param listener
     */
    public final void removeListener(final ConstellationInputListener listener) {
        inputFieldListeners.remove(listener);
    }
    // </editor-fold>   

    // <editor-fold defaultstate="collapsed" desc="ConstelationTextArea Modification Methods">
    /**
     * Sets the prompt text of the {@link ConstellationTextArea}.
     *
     * @param description
     */
    public final void setPromptText(final String description) {
        textArea.setPromptText(description);
    }

    /**
     * Sets the default {@link ToolTip} of the {@link ConstellationTextArea}.
     * toDo: enable to Tooltip to be temporarily overwritten to provide extra
     * context when the Input field becomes invalidated.
     *
     * @param tooltip
     */
    public final void setTooltip(final Tooltip tooltip) {
        textArea.setTooltip(tooltip);
    }

    /**
     * Gets the current string value of the {@link ConstellationTextArea}.
     *
     * ToDo: Ideally this method would become private and input information
     * could only be retrieved by using the getValue method.
     *
     * @return
     */
    public final String getText() {
        return textArea.getText();
    }

    /**
     * Sets the current string value of the {@link ConstellationTextArea}.
     *
     * ToDo: Ideally this method would become private and input information
     * could only be set by using the setValue method.
     *
     * @param stringValue
     */
    public final void setText(final String stringValue) {
        updatingText = true;
        inferredCaretPos = stringValue != null ? stringValue.length() : 0;
        textArea.setText(stringValue);
        updatingText = false;
    }

    /**
     * Determines if the input should be editable by the user. ToDo: review
     * usages of this method and it's effects on the user interaction. I believe
     * the current use disables the input field from being selected but visually
     * provides no change. Should probably grey-out the field and not enable it
     * to become focused.
     *
     * @param enabled
     */
    public final void setEditable(final boolean enabled) {
        textArea.setEditable(enabled);
    }

    /**
     * Sets the preferred number of rows that this input field should have.
     *
     * @param suggestedHeight the number of rows in this input field.
     */
    public void setPrefRowCount(final Integer suggestedHeight) {
        textArea.setPreferedRowCount(suggestedHeight);
    }

    /**
     * A feature for use in secret inputs, turns the input text value from plain
     * text to obscured values.
     */
    protected void hideSecret() {
        textArea.hide();
    }

    /**
     * A feature for use in secret inputs, turns the input text value from
     * obscured values to plain text.
     */
    protected void showSecret() {
        textArea.reveal();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Value Modification & Validation Declaration">  
    /**
     * Gets the value that this input field represents;
     *
     * @return
     */
    public abstract T getValue();

    /**
     * Sets the value that this input field represents
     *
     * @param value
     */
    public abstract void setValue(final T value);

    /**
     * Determine if the provided text is a valid value for the input field.
     * Validity is determined by the conditions of this inputField and the
     * registeredInputValidators
     *
     * @return
     */
    public boolean isValid() {
        for (final InputValidator validator : validators) {
            if (null != validator.validateString(getText())) {
                return false;
            }
        }
        return isValidContent();
    }

    /**
     * Determine if the provided text is a valid value for the input field.
     *
     * @return
     */
    public abstract boolean isValidContent();

    /**
     * Set this field as valid or invalid.
     *
     * If the field is not in focus the background color will be updated by this
     * method. if the field is in focus, the background color change will be
     * deferred to the serInFocus method();
     *
     * @param fieldValidity
     */
    private void setValid(final boolean fieldValidity) {
        isValid = fieldValidity;
        if (!focused) {
            updateFieldValidityVisuals(isValid);
        }
    }

    /**
     * Responsible for updating the color of the background of an input field.
     * Uses a boolean parameter to override the expression of validity in some
     * cases. Typically the isValid attribute will be passed in to ensure that
     * the input field reflects the validity of its contents.
     */
    private void updateFieldValidityVisuals(final boolean valid) {
        if (valid) {
            textArea.getStyleClass().remove("invalid-input");
        } else {
            textArea.getStyleClass().add("invalid-input");
        }
    }

    public void addValidator(final InputValidator validator) {
        validators.add(validator);
    }

    public final int getCaretPosition(){
        return textArea.getCaretPosition();
    }

    public final int getInferredCaretPosition(){
        return inferredCaretPos;
    }

    // </editor-fold>  
    // <editor-fold defaultstate="collapsed" desc="ContextMenuContributor Implementation"> 
    @Override
    public final List<MenuItem> getAllMenuItems() {
        final List<MenuItem> list = new ArrayList<>();
        final List<MenuItem> local = getLocalMenuItems(); // This method is implmented by extensions of this class.
        list.addAll(local);
        if (!local.isEmpty()) {
            list.add(new SeparatorMenuItem());
        }
        list.addAll(textArea.getAllMenuItems());
        return list;
    }
    // </editor-fold>  

    // <editor-fold defaultstate="collapsed" desc="DropDown Decleration & Functionality">    
    /**
     * Displays the provided ConstellationInputDropDown to the user. This
     * functionality has been captured in the base class intentionally to
     * consistency across all input fields regarding context displaying
     *
     * @param menu
     */
    protected final void showDropDown(final ConstellationInputDropDown menu) {
        menu.show(this, Side.TOP, USE_PREF_SIZE, USE_PREF_SIZE);
        setMenuShown(true);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="InfoWindow Functionality">  
    /**
     * Inserts an info window into this input field. Input windows are placed
     * directly after the {@link ConstellationTextArea}
     *
     * @param window
     */
    protected final void insertInfoWindow(final InfoWindow window) {
        Platform.runLater(() -> {
            if (window != null) {
                final HBox interactableContent = getInteractableContent();
                final int windowIndex = interactableContent.getChildren().indexOf(textArea) + 1;
                interactableContent.getChildren().add(windowIndex, window);
            }
        });
    }

    /**
     * Removes the info window into this input field.
     *
     * @param window
     */
    protected void removeInfoWindow(final InfoWindow window) {
        final HBox interactableContent = getInteractableContent();
        interactableContent.getChildren().remove(window);
    }

    /**
     * Checks if this input currently has an info window showing in its
     * interactable content.
     *
     * @return
     */
    protected boolean hasInfoWindow() {
        final HBox interactableContent = getInteractableContent();
        return interactableContent.getChildren().stream().anyMatch(InfoWindow.class::isInstance);
    }
//     </editor-fold>
}
