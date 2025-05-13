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
package au.gov.asd.tac.constellation.utilities.text;

import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import java.util.List;
import java.util.prefs.Preferences;
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
import org.openide.util.NbPreferences;

/**
 * SpellCheckingTextArea is an InlineCssTextArea from the RichTextFX library
 * with added methods for highlighting spelling errors and some grammar errors.
 *
 * @author Auriga2
 */
public class SpellCheckingTextArea extends InlineCssTextArea {

    private static final Preferences PREFERENCES = NbPreferences.forModule(ApplicationPreferenceKeys.class);
    private final SpellChecker spellChecker = new SpellChecker(this);
    private final Insets insets = new Insets(4, 8, 4, 8);
    public static final double EXTRA_HEIGHT = 3;

    private static final String UNDERLINE_AND_HIGHLIGHT_STYLE = "-rtfx-background-color:derive(yellow,-30%);"
            + "-rtfx-underline-color: red; "
            + "-rtfx-underline-dash-array: 2 2;"
            + "-rtfx-underline-width: 2.0;"
            + "-fx-fill: black;";

    private static final String CLEAR_STYLE = "-rtfx-background-color: transparent;"
            + "-rtfx-underline-color: transparent;";

    public SpellCheckingTextArea(final boolean isSpellCheckEnabled) {
        final boolean enableSpellChecking = PREFERENCES.getBoolean(ApplicationPreferenceKeys.ENABLE_SPELL_CHECKING, ApplicationPreferenceKeys.ENABLE_SPELL_CHECKING_DEFAULT) && isSpellCheckEnabled;
        spellChecker.turnOffSpellChecking(!enableSpellChecking);

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

        this.setOnKeyReleased((final KeyEvent event) -> {
            if (spellChecker.canCheckSpelling(this.getText())) {
                spellChecker.checkSpelling();
            }
        });

        // Set the right click context menu
        final ContextMenu contextMenu = addRightClickContextMenu(enableSpellChecking);
        this.setContextMenu(contextMenu);
    }

    public SpellCheckingTextArea(final boolean isSpellCheckEnabled, final String text) {
        this(isSpellCheckEnabled);
        setText(text);
    }

    public final void setText(final String text) {
        this.replaceText(text);
    }

    /**
     * underline and highlight the text from start to end.
     */
    public void highlightText(final int start, final int end) {
        this.setStyle(start, end, UNDERLINE_AND_HIGHLIGHT_STYLE);
    }

    /**
     * Clear any previous highlighting.
     */
    public void clearStyles() {
        this.setStyle(0, this.getText().length(), CLEAR_STYLE);
    }

    public boolean isWordUnderCursorHighlighted(final int index) {
        return this.getStyleOfChar(index) != null && this.getStyleOfChar(index).equals(UNDERLINE_AND_HIGHLIGHT_STYLE);
    }


    public final void setTooltip(final Tooltip tooltip) {
        Tooltip.install(this, tooltip);
    }

    public final void setPromptText(final String promptText) {
        //TODO
    }


    private ContextMenu addRightClickContextMenu(final boolean enableSpellChecking) {
        final ContextMenu contextMenu = new ContextMenu();

        final MenuItem undoMenuItem = new MenuItem("Undo");
        final MenuItem redoMenuItem = new MenuItem("Redo");
        final MenuItem cutMenuItem = new MenuItem("Cut");
        final MenuItem copyMenuItem = new MenuItem("Copy");
        final MenuItem pasteMenuItem = new MenuItem("Paste");
        final MenuItem deleteMenuItem = new MenuItem("Delete");
        final MenuItem selectAllMenuItem = new MenuItem("Select All");

        undoMenuItem.setOnAction(e -> this.undo());
        redoMenuItem.setOnAction(e -> this.redo());
        cutMenuItem.setOnAction(e -> this.cut());
        copyMenuItem.setOnAction(e -> this.copy());
        pasteMenuItem.setOnAction(e -> this.paste());
        deleteMenuItem.setOnAction(e -> this.deleteText(this.getSelection()));
        selectAllMenuItem.setOnAction(e -> this.selectAll());

        // CheckMenuItem to toggle turn On/Off Spell Checking. On by default
        final CheckMenuItem toggleSpellCheckMenuItem = new CheckMenuItem("Turn On Spell Checking");
        toggleSpellCheckMenuItem.setSelected(true);
        toggleSpellCheckMenuItem.setDisable(!enableSpellChecking);
        toggleSpellCheckMenuItem.setVisible(enableSpellChecking);
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

        contextMenu.getItems().addAll(toggleSpellCheckMenuItem, new SeparatorMenuItem(), undoMenuItem, redoMenuItem, cutMenuItem, copyMenuItem, pasteMenuItem, deleteMenuItem, selectAllMenuItem);
        return contextMenu;
    }

    private BooleanBinding getSelectionBinding() {
        return Bindings.createBooleanBinding(() -> {
            final IndexRange selectionRange = this.getSelection();
            return selectionRange == null || selectionRange.getLength() == 0;
        }, this.selectionProperty());
    }

    public void autoComplete(final List<String> suggestions) {
        final Popup popup = new Popup();
        popup.setWidth(this.getWidth());
        final ListView<String> listView = new ListView<>();
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
                    popup.show(this, this.localToScreen(0, 0).getX(), this.localToScreen(0, 0).getY() + this.heightProperty().getValue());
                }
            }
        });
    }
}
