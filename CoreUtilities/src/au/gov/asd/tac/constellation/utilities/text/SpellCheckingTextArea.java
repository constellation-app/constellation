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
package au.gov.asd.tac.constellation.utilities.text;

import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.IndexRange;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Popup;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.richtext.InlineCssTextArea;
import org.fxmisc.richtext.util.UndoUtils;

/**
 * SpellCheckingTextArea is an InlineCssTextArea from the RichTextFX library
 * with added methods for highlighting spelling errors and some grammar errors.
 *
 * @author Auriga2
 */
public class SpellCheckingTextArea extends InlineCssTextArea {
    private final SpellChecker spellChecker = new SpellChecker(this);
    private final Insets insets = new Insets(4, 8, 4, 8);
    public static final double EXTRA_HEIGHT = 3;

    /**
     * Default constructor.
     */
    public SpellCheckingTextArea() {
        this.setAutoHeight(false);
        this.setWrapText(true);
        this.setPadding(insets);
        final String css = SpellCheckingTextArea.class.getResource("SpellChecker.css").toExternalForm();//"resources/test.css"
        this.getStylesheets().add(css);

        this.setOnMouseClicked((final MouseEvent event) -> {
            if (event.getButton() == MouseButton.PRIMARY && event.isStillSincePress()) {
                spellChecker.checkSpelling();
                spellChecker.popUpSuggestionsListAction(event);
            }
        });

        this.textProperty().addListener((observable, oldText, newText) -> {
            if (canCheckSpelling(newText)) {
                spellChecker.checkSpelling();
            }
        });

        // Set the right click context menu
        ContextMenu contextMenu = addRightClickContextMenu();
        this.setContextMenu(contextMenu);
    }

    /**
     * Constructor with input text.
     */
    public SpellCheckingTextArea(final String text) {
        this();
        setText(text);
    }

    public void setText(final String text) {
        this.replaceText(text);
    }

    /**
     * underline and highlight the text from start to end.
     */
    public void highlightText(final int start, final int end) {
        String underlineAndHighlight = "-rtfx-background-color:derive(yellow,-30%);"
                + "-rtfx-underline-color: red; "
                + "-rtfx-underline-dash-array: 2 2;"
                + "-rtfx-underline-width: 2.0;"
                + "-fx-fill: black;";

        this.setStyle(start, end, underlineAndHighlight);
    }

    /**
     * Clear any previous highlighting.
     */
    public void clearStyles() {
        this.setStyle(0, this.getText().length(),
                "-rtfx-background-color: transparent;"
                + "-rtfx-underline-color: transparent;");
    }

    public final void setTooltip(final Tooltip tooltip) {
        Tooltip.install(this, tooltip);
    }

    public final void setPromptText(final String promptText) {
        //TODO
    }

//    public void bindAutoCompletion(List<String> suggestions) {
//        this.setSpellCheckCallback(word -> {
//            if (!suggestions.isEmpty()) {
//                return suggestions.stream()
//                        .filter(suggestion -> suggestion.startsWith(word))
//                        .toArray(String[]::new);
//            }
//            return null;
//        });
//    }

    /**
     * Prevent highlighting while still typing at the end of a sentence. It
     * checks only after a non alpha numeric character except apostrophe symbol
     * (') is typed at the end. This checks spelling when the user is typing in
     * the middle of a sentence.
     */
    private boolean canCheckSpelling(final String newText) {
        return newText.length() > 0 && !(this.getCaretPosition() == this.getText().length()
                && newText.substring(newText.length() - 1).matches("[a-zA-Z0-9']"));

    }

    private ContextMenu addRightClickContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem undoMenuItem = new MenuItem("Undo");
        MenuItem redoMenuItem = new MenuItem("Redo");
        MenuItem cutMenuItem = new MenuItem("Cut");
        MenuItem copyMenuItem = new MenuItem("Copy");
        MenuItem pasteMenuItem = new MenuItem("Paste");
        MenuItem deleteMenuItem = new MenuItem("Delete");
        MenuItem selectAllMenuItem = new MenuItem("Select All");

        undoMenuItem.setOnAction(e -> this.undo());
        redoMenuItem.setOnAction(e -> this.redo());
        cutMenuItem.setOnAction(e -> this.cut());
        copyMenuItem.setOnAction(e -> this.copy());
        pasteMenuItem.setOnAction(e -> this.paste());
        deleteMenuItem.setOnAction(e -> this.deleteText(this.getSelection()));
        selectAllMenuItem.setOnAction(e -> this.selectAll());

        // CheckMenuItem to toggle turn On/Off Spell Checking. On by default
        CheckMenuItem toggleSpellCheckMenuItem = new CheckMenuItem("Turn On Spell Checking");
        toggleSpellCheckMenuItem.setSelected(true);
        toggleSpellCheckMenuItem.setOnAction(event -> {
            spellChecker.turnOffSpellChecking(!toggleSpellCheckMenuItem.isSelected());
            spellChecker.checkSpelling();
        });

        // avoid Undo redo of highlighting
        this.setUndoManager(UndoUtils.plainTextUndoManager(this));

        // Listener to enable/disable Undo and Redo menu items based on the undo stack
        this.getUndoManager().undoAvailableProperty().addListener((obs, oldValue, newValue) -> {
            undoMenuItem.setDisable(!(boolean) newValue);
        });

        this.getUndoManager().redoAvailableProperty().addListener((obs, oldValue, newValue) -> {
            redoMenuItem.setDisable(!(boolean) newValue);
        });

        // Bind the Cut, Copy and Delete menu item's disable property, to enable them only when there's a selection
        cutMenuItem.disableProperty().bind(getSelectionBinding());
        copyMenuItem.disableProperty().bind(getSelectionBinding());
        deleteMenuItem.disableProperty().bind(getSelectionBinding());

        contextMenu.getItems().addAll(toggleSpellCheckMenuItem, new SeparatorMenuItem(), new SeparatorMenuItem(), undoMenuItem, redoMenuItem, cutMenuItem, copyMenuItem, pasteMenuItem, deleteMenuItem, selectAllMenuItem);
        return contextMenu;
    }

    private BooleanBinding getSelectionBinding() {
        return Bindings.createBooleanBinding(() -> {
            final IndexRange selectionRange = this.getSelection();
            return selectionRange == null || selectionRange.getLength() == 0;
        }, this.selectionProperty());
    }

    public void autoComplete(final List<String> suggestions) {
        Popup popup = new Popup();
        popup.setWidth(this.getWidth());
        ListView<String> listView = new ListView<>();
        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                this.replaceText​(newValue);
            }
            popup.hide();
        });

        this.setOnKeyTyped((final KeyEvent event) -> {
            final String input = this.getText();
            popup.hide();
            popup.setAutoFix(true);
            popup.setAutoHide(true);
            popup.setHideOnEscape(true);
            popup.getContent().clear();
            listView.getItems().clear();

            if (!StringUtils.isBlank(input) && !suggestions.isEmpty()) {
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
                if (listView.getItems().size() > 0) {
                    popup.show(this, this.localToScreen(0, 0).getX(), this.localToScreen(0, 0).getY() + this.heightProperty().getValue());
                }
            }
        });
    }

}
