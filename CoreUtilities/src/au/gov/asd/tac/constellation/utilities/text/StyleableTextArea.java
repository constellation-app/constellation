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
package au.gov.asd.tac.constellation.utilities.text;

import au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputFieldConstants.TextType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.IndexRange;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Popup;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.richtext.InlineCssTextArea;
import org.fxmisc.richtext.util.UndoUtils;

/**
 * StyleableTextArea is an extension of InlineCssTextArea from the RichTextFX library
 * with added methods for highlighting segments of text within the area
 *
 * @author Auriga2
 * @author capricornunicorn123
 */
public class StyleableTextArea {

    public final InlineCssTextArea field = new InlineCssTextArea();
    public static final String VALID_WORD_STYLE = "-rtfx-underline-color: transparent;";
    public static final String MISSPELT_WORD_STYLE = "-rtfx-underline-color: red; "
    + "-rtfx-underline-dash-array: 2 2;"
    + "-rtfx-underline-width: 2.0;";

    private final Insets insets = new Insets(4, 8, 4, 8);
    public static final double EXTRA_HEIGHT = 3;

    public StyleableTextArea() {
        
        field.setAutoHeight(false);
        field.setWrapText(true);
        field.setPadding(insets);
        final String css = StyleableTextArea.class.getResource("SpellChecker.css").toExternalForm();//"resources/test.css"
        field.getStylesheets().add(css);
        
        final ContextMenu contextMenu = new ContextMenu();
        final List<MenuItem> areaModificationItems = getAreaModificationMenuItems();
        // Set the right click context menu items
        // we want to update each time the context menu is requested 
        // can't make a new context menu each time as this event occurs after showing
        field.setOnContextMenuRequested(value -> {
            contextMenu.getItems().clear();
            contextMenu.getItems().addAll(SpellChecker.getSpellCheckMenuItem(this), new SeparatorMenuItem());
            contextMenu.getItems().addAll(areaModificationItems);
            field.setContextMenu(contextMenu);
        });
    }

    public void setText(final String text) {
        field.replaceText(text);
    }

    /**
     * underline and highlight the text from start to end.
     */
    public void highlightText(final int start, final int end) {
        field.setStyle(start, end, MISSPELT_WORD_STYLE);
    }

    /**
     * Clear any previous highlighting.
     */
    public void clearStyles() {
        field.setStyle(0, field.getText().length(), VALID_WORD_STYLE);
    }

    public boolean isWordUnderCursorHighlighted(final int index) {
        return field.getStyleOfChar(index) == MISSPELT_WORD_STYLE;
    }


    public final void setTooltip(final Tooltip tooltip) {
        Tooltip.install(field, tooltip);
    }

    public final void setPromptText(final String promptText) {
        //TODO
    }

    private List<MenuItem> getAreaModificationMenuItems() {

        final MenuItem undoMenuItem = new MenuItem("Undo");
        final MenuItem redoMenuItem = new MenuItem("Redo");
        final MenuItem cutMenuItem = new MenuItem("Cut");
        final MenuItem copyMenuItem = new MenuItem("Copy");
        final MenuItem pasteMenuItem = new MenuItem("Paste");
        final MenuItem deleteMenuItem = new MenuItem("Delete");
        final MenuItem selectAllMenuItem = new MenuItem("Select All");

        undoMenuItem.setOnAction(e -> field.undo());
        redoMenuItem.setOnAction(e -> field.redo());
        cutMenuItem.setOnAction(e -> field.cut());
        copyMenuItem.setOnAction(e -> field.copy());
        pasteMenuItem.setOnAction(e -> field.paste());
        deleteMenuItem.setOnAction(e -> field.deleteText(field.getSelection()));
        selectAllMenuItem.setOnAction(e -> field.selectAll());

        // avoid Undo redo of highlighting
        field.setUndoManager(UndoUtils.plainTextUndoManager(field));

        // Listener to enable/disable Undo and Redo menu items based on the undo stack
        field.getUndoManager().undoAvailableProperty().addListener((obs, oldValue, newValue) -> {
            undoMenuItem.setDisable(!(boolean) newValue);
        });

        field.getUndoManager().redoAvailableProperty().addListener((obs, oldValue, newValue) -> {
            redoMenuItem.setDisable(!(boolean) newValue);
        });

        // Bind the Cut, Copy and Delete menu item's disable property, to enable them only when there's a selection
        cutMenuItem.disableProperty().bind(getSelectionBinding());
        copyMenuItem.disableProperty().bind(getSelectionBinding());
        deleteMenuItem.disableProperty().bind(getSelectionBinding());

        return Arrays.asList(undoMenuItem, redoMenuItem, cutMenuItem, copyMenuItem, pasteMenuItem, deleteMenuItem, selectAllMenuItem);
    }

    private BooleanBinding getSelectionBinding() {
        return Bindings.createBooleanBinding(() -> {
            final IndexRange selectionRange = field.getSelection();
            return selectionRange == null || selectionRange.getLength() == 0;
        }, field.selectionProperty());
    }

    public void autoComplete(final List<String> suggestions) {
        final Popup popup = new Popup();
        popup.setWidth(field.getWidth());
        final ListView<String> listView = new ListView<>();
        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                field.replaceText​(newValue);
            }
            popup.hide();
        });

        field.setOnKeyTyped((final KeyEvent event) -> {
            final String input = field.getText();
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
                        listView.setPrefHeight(listView.getItems().size() * cell.getHeight() + EXTRA_HEIGHT);
                    }
                });

                // Show the popup under this text area
                if (!listView.getItems().isEmpty()) {
                    popup.show(field, field.localToScreen(0, 0).getX(), field.localToScreen(0, 0).getY() + field.heightProperty().getValue());
                }
            }
        });
    }

    public void enableSpellCheck(boolean spellCheckEnabled) {
        if (spellCheckEnabled){
            SpellChecker.registerArea(this);
        } else {
            SpellChecker.deregisterArea(this);
        }
        
    }
}
