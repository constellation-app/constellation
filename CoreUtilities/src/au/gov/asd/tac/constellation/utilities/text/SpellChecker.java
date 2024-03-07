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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import org.apache.commons.lang3.StringUtils;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.MultiThreadedJLanguageTool;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.SpellingCheckRule;
/**
 * Handles the SpellChecking functions of SpellCheckingTextArea. SpellChecker
 * evaluates incorrect words/phrases and pops up the suggestions when the user
 * prompts
 *
 * @author Auriga2
 */
public final class SpellChecker {

    private final SpellCheckingTextArea textArea;
    private static final List<String> misspells = new ArrayList<>();
    private List<RuleMatch> matches = new ArrayList<>();
    private int indexOfMisspelledTextUnderCursor;       // position of the current misspelled text in misspells list
    private final ListView<String> suggestions = new ListView<>(FXCollections.observableArrayList());
    private final AtomicReference<JLanguageTool> langTool;
    private static JLanguageTool langToolStatic;
    private SpellingCheckRule spellingCheckRule;
    private Popup popup;
    private boolean turnOffSpellChecking = false;
    private int startOfMisspelledTextUnderCursor;
    private int endOfMisspelledTextUnderCursor;
    private String specificRuleId;
    private static final Logger LOGGER = Logger.getLogger(SpellChecker.class.getName());
    private static final double POPUP_HEIGHT = 108;
    private static Language language;

    protected static final CompletableFuture<Void> LANGTOOL_LOAD;

    static {
        // langToolStatic is used to initialize the JLanguageTool at the loading, because
        // the very first initializing of JLanguageTool is slow but after that it is fast.
        LANGTOOL_LOAD = CompletableFuture.supplyAsync(() -> {
            while (true) {
                language = Languages.getLanguageForShortCode("en-AU");
                langToolStatic = new MultiThreadedJLanguageTool(language);
                try {
                    //perform a check here to prevent the spell checking being too slow at the first word after loading costy
                    final List<RuleMatch> initMatches = langToolStatic.check("random text");
                } catch (final IOException ex) {
                    LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
                }
                return null;
            }
        }, Executors.newSingleThreadExecutor());
    }

    public SpellChecker(final SpellCheckingTextArea spellCheckingTextArea) {
        textArea = spellCheckingTextArea;
        langTool = new AtomicReference<>();
        while (true) {
            LANGTOOL_LOAD.thenRun(() -> {
                JLanguageTool langToolNew = new MultiThreadedJLanguageTool(language);
                langTool.set(langToolNew);
                initialize();
            });
            break;
        }
    }

    private void initialize() {
        for (final Rule rule : langTool.get().getAllRules()) {
            if (rule.getId().equals("UPPERCASE_SENTENCE_START")) {
                langTool.get().disableRule(rule.getId());
            } else if (rule instanceof SpellingCheckRule) {
                spellingCheckRule = (SpellingCheckRule) rule;
            }
        }

        //initialize popup
        popup = new Popup();
        suggestions.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                final StringBuilder builder = new StringBuilder(textArea.getText());
                builder.replace(startOfMisspelledTextUnderCursor, endOfMisspelledTextUnderCursor, newValue);
                textArea.replaceText​(builder.toString());
            }
            popup.hide();
            checkSpelling();
        });
    }

    /**
     * Check Spelling of the entire text. This will ensure scenarios like
     * duplicate words, grammar mistakes etc. are triggered
     */
    public void checkSpelling() {
        textArea.clearStyles();
        misspells.clear();

        if (!turnOffSpellChecking && StringUtils.isNotBlank(textArea.getText())) {
            try {
                matches = langTool.get().check(textArea.getText());
                matches.forEach(match -> {
                    final int start = match.getFromPos();
                    final int end = match.getToPos();
                    final String misspell = textArea.getText().substring(start, end);
                    misspells.add(misspell);
                    textArea.highlightText(start, end);
                });
            } catch (final IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
            }
        }
    }

    /**
     * Pop up the suggestions list if the word/phrase under the cursor is
     * misspelled.
     */
    public void popUpSuggestionsListAction(final MouseEvent event) {
        if (!turnOffSpellChecking) {
            final ObservableList<String> suggestionsList = FXCollections.observableArrayList();

            popup.hide();
            popup.setAutoFix(true);
            popup.setAutoHide(true);
            popup.setHideOnEscape(true);

            if (isWordUnderCursorMisspelled()) {
                suggestionsList.clear();
                suggestions.getSelectionModel().clearSelection();
                suggestionsList.addAll(matches.get(indexOfMisspelledTextUnderCursor).getSuggestedReplacements());
                if (suggestionsList.isEmpty()) {
                    return;
                }

                suggestions.setItems(suggestionsList.size() > 5 ? FXCollections.observableArrayList(suggestionsList.subList(0, 5)) : suggestionsList);
                final Button ignoreButton = new Button("Ignore All");
                final VBox popupContent = new VBox();
                ignoreButton.setOnAction(e -> this.addWordsToIgnore());
                suggestions.setPrefHeight(POPUP_HEIGHT);

                popup.getContent().clear();
                popupContent.getChildren().clear();

                popupContent.setStyle(
                        "-fx-background-color: black;"
                        + "-fx-text-fill: white;"
                        + "-fx-padding: 5;");

                // Temporary check to remove ignore button on non spelling errors
                if (specificRuleId.equals("MORFOLOGIK_RULE_EN_AU")) {
                    popupContent.getChildren().addAll(suggestions, ignoreButton);
                } else {
                    popupContent.getChildren().addAll(suggestions);
                }

                popupContent.autosize();
                popup.getContent().add(popupContent);
                popup.setAutoFix(true);
                popup.show(textArea, event.getScreenX(), event.getScreenY() + 10);
            }
        }
    }

    /**
     * Retrieve the word/phrase under the cursor and check if it is misspelled.
     * If it is misspelled the index is populated.
     */
    private boolean isWordUnderCursorMisspelled() {
        final int cursorIndex = textArea.getCaretPosition();

        if (cursorIndex <= 0 || cursorIndex >= textArea.getText().length()) {
            //= is to avoid the scenario of displaying the suggesttions of the first/last word
            // (if they are incorrect) when clicking on the empty space right/below the text
            return false;
        }

        for (final RuleMatch match : matches) {
            final int start = match.getFromPos();
            final int end = match.getToPos();
            if (cursorIndex >= start && cursorIndex <= end) {
                indexOfMisspelledTextUnderCursor = misspells.indexOf(textArea.getText().substring(start, end));
                startOfMisspelledTextUnderCursor = start;
                endOfMisspelledTextUnderCursor = end;
                specificRuleId = match.getSpecificRuleId();
                return true;
            }
        }
        return false;
    }

    /**
     * Prevents highlighting while still typing. When a highlighted word is
     * corrected manually it'll be marked as correct, similar to that in
     * Microsoft Word.
     */
    public boolean canCheckSpelling(final String newText) {
        final int caretPosition = textArea.getCaretPosition();
        if (caretPosition == 0) {
            return true;
        } else if (caretPosition <= newText.length()) {
            final String charAtCaret = Character.toString(textArea.getText().charAt(caretPosition - 1));
            return !newText.isEmpty() && (textArea.isWordUnderCursorHighlighted(caretPosition - 1) || !charAtCaret.matches("[a-zA-Z0-9']"));
        } else {
            return false;
        }
    }

    public void turnOffSpellChecking(final boolean turnOffSpellChecking) {
        this.turnOffSpellChecking = turnOffSpellChecking;
    }

    public void addWordsToIgnore() {
        if (spellingCheckRule != null) {
            spellingCheckRule.addIgnoreTokens(Arrays.asList(textArea.getText().substring(startOfMisspelledTextUnderCursor, endOfMisspelledTextUnderCursor)));
            popup.hide();
            checkSpelling();
        }
    }
}
