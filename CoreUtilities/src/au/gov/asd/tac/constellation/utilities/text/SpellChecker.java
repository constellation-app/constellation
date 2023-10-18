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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Popup;
import org.apache.commons.lang3.StringUtils;
import org.languagetool.JLanguageTool;
import org.languagetool.MultiThreadedJLanguageTool;
import org.languagetool.language.AustralianEnglish;
import org.languagetool.rules.RuleMatch;

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
    private int currentIndex;       // index for current position in misspells list
    private final ListView<String> suggestions = new ListView<>(FXCollections.observableArrayList());
    private String misspelledTextUnderCursor;
    private final JLanguageTool langTool;
    private Popup popup;
    private boolean turnOffSpellChecking = false;

    public SpellChecker(final SpellCheckingTextArea spellCheckingTextArea) {
        textArea = spellCheckingTextArea;
        langTool = new MultiThreadedJLanguageTool(new AustralianEnglish()); //Can pass in the language when supporting multiple languages
        initialize();
    }

    /**
     * set a listener on the list views selection model to get the text of the
     * selected row.
     */
    private void initialize() {
        popup = new Popup();
        suggestions.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                textArea.replaceText​(textArea.getText().replace(misspelledTextUnderCursor, newValue));
            }
            popup.hide();
        });
    }

    /**
     * Check Spelling of the entire text. This will ensure scenarios like
     * duplicate words, grammar mistakes etc. are triggered
     */
    public void checkSpelling() {
        textArea.clearStyles();
        misspells.clear();
        if (!turnOffSpellChecking && !StringUtils.isBlank(textArea.getText())) {
            try {
                matches = langTool.check(textArea.getText());
            } catch (IOException ex) {
                Logger.getLogger(SpellChecker.class.getName()).log(Level.SEVERE, null, ex);
            }

            matches.forEach(match -> {
                final int start = match.getFromPos();
                final int end = match.getToPos();
                String misspell = textArea.getText().substring(start, end);
                misspells.add(misspell);
                textArea.highlightText(start, end);
            });
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
                suggestionsList.addAll(matches.get(currentIndex).getSuggestedReplacements());
                suggestions.setItems(suggestionsList);

                final Label popupMsg = new Label("");
                popupMsg.setStyle(
                        "-fx-background-color: black;"
                        + "-fx-text-fill: white;"
                        + "-fx-padding: 5;");
                popup.getContent().clear();
                popup.getContent().add(popupMsg);
                popup.getContent().add(suggestions);

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
        final String fullText = textArea.getText();

        if (cursorIndex >= fullText.length()) { //= is to avoid the scenario of displaying the suggesttions of the last word (when it's incorrect)
            // when clicking on the empty space below the text
            return false;
        }

        for (RuleMatch match : matches) {
            int start = match.getFromPos();
            int end = match.getToPos();
            if (cursorIndex >= start && cursorIndex <= end) {
                misspelledTextUnderCursor = textArea.getText().substring(start, end);
                currentIndex = misspells.indexOf(misspelledTextUnderCursor);
                return true;
            }
        }
        return false;
    }

    public void turnOffSpellChecking(boolean turnOffSpellChecking) {
        this.turnOffSpellChecking = turnOffSpellChecking;
    }
}
