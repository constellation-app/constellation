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

import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
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
import org.openide.util.NbPreferences;
/**
 * Handles the SpellChecking functions of SpellCheckingTextArea. SpellChecker
 * evaluates incorrect words/phrases and pops up the suggestions when the user
 * prompts
 *
 * @author Auriga2
 * @author capricornunicorn123
 */
public final class SpellChecker implements PreferenceChangeListener{
    
    private static final List<StyleableTextArea> textAreas = new ArrayList<>();
    private static final Preferences PREFERENCES = NbPreferences.forModule(ApplicationPreferenceKeys.class);
    
    private static final SpellChecker spellChekerInstantiation = new SpellChecker();

    private static final AtomicReference<JLanguageTool> langTool = new AtomicReference<>();
    private static JLanguageTool langToolStatic;
    private static SpellingCheckRule spellingCheckRule;

    private static final Logger LOGGER = Logger.getLogger(SpellChecker.class.getName());
    private static final double POPUP_PADDING = 5;
    private static final double ITEM_HEIGHT = 24;
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
        
        while (true) {
            LANGTOOL_LOAD.thenRun(() -> {
                JLanguageTool langToolNew = new MultiThreadedJLanguageTool(language);
                langTool.set(langToolNew);
                for (final Rule rule : langTool.get().getAllRules()) {
                    if (rule.getId().equals("UPPERCASE_SENTENCE_START")) {
                        langTool.get().disableRule(rule.getId());
                    } else if (rule instanceof SpellingCheckRule) {
                        spellingCheckRule = (SpellingCheckRule) rule;
                    }
                }
            });
            break;
        }
    }

    private static boolean globalSpellCheckingEnabled() {
        return PREFERENCES.getBoolean(ApplicationPreferenceKeys.ENABLE_SPELL_CHECKING, ApplicationPreferenceKeys.ENABLE_SPELL_CHECKING_DEFAULT);
    }

    /**
     * Constructor to register a spell checker as a listener to the spell checking preference.
     */
    private SpellChecker() {
        NbPreferences.forModule(ApplicationPreferenceKeys.class).addPreferenceChangeListener(this);
    }
    
    /** 
     * This method registers a text area as having spell checking fucntionality.
     * Being registered is not the only mechanism to enable spell checking noor deos it ensure persistant spell checking. 
     * registered text areas are only able to be spell checked when the global spell cheking parameter is enabled.
     * @param area 
     */
    static void registerArea(StyleableTextArea area) {
        
        area.setOnMouseClicked((final MouseEvent event) -> {
            if (globalSpellCheckingEnabled() && event.getButton() == MouseButton.PRIMARY && event.isStillSincePress()) {
                checkSpelling(area);
                popUpSuggestionsListAction(area, event);
            }
        });
        
        area.setOnKeyReleased((final KeyEvent event) -> {
            if (canCheckSpelling(area)) {
                checkSpelling(area);
            }
        });
        
        textAreas.add(area);
        checkSpelling(area);
    }

    static void deregisterArea(StyleableTextArea area) {
        textAreas.remove(area);
        area.setOnMouseClicked(null);
        area.setOnKeyReleased(null);
        //will remove formatting
        area.clearStyles();
    }
    
    static List<RuleMatch> getMatches(StyleableTextArea area){
        try {
            return langTool.get().check(area.getText());
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
        }
        return new ArrayList<>();
    }
    
    /**
     * Checks spelling of the entire text. 
     * This method is essentially called whenever a change is detected on the text area.
     * This method ensures scenarios like duplicate words, grammar mistakes etc. are captured
     */
    static void checkSpelling(StyleableTextArea area) {
        if (textAreas.contains(area) && StringUtils.isNotBlank(area.getText())) {        
            area.clearStyles();
            final List<RuleMatch> matches = getMatches(area);
            matches.forEach(match -> {
                final int start = match.getFromPos();
                final int end = match.getToPos();
                area.highlightText(start, end);
            });
        }
    }

    /**
     * Pop up the suggestions list if the word/phrase under the cursor is
     * misspelled.
     */
    static void popUpSuggestionsListAction(StyleableTextArea area, MouseEvent event) {
        if (textAreas.contains(area)) {
            final ObservableList<String> suggestionsList = FXCollections.observableArrayList();
            final Popup popup = new Popup();

            popup.hide();
            popup.setAutoFix(true);
            popup.setAutoHide(true);
            popup.setHideOnEscape(true);
            MisspeltWordData data = getSelectedMisspeltWordData(area);
            if (data != null) {
                final Button ignoreButton = new Button("Ignore All");
                final VBox popupContent = new VBox(POPUP_PADDING);
                final ListView<String> suggestions = new ListView<>(FXCollections.observableArrayList());
                final Label labelMessage = new Label(data.rule.getMessage());
                popupContent.setStyle(
                        "-fx-background-color: black;"
                        + "-fx-text-fill: white;"
                        + "-fx-padding: " + POPUP_PADDING + ";");
                popupContent.getChildren().clear();
                suggestionsList.clear();
                popup.getContent().clear();
                
                suggestions.getSelectionModel().clearSelection();

                suggestionsList.addAll(data.rule.getSuggestedReplacements());
                if (suggestionsList.isEmpty()) {
                    popupContent.getChildren().addAll(labelMessage);
                } else {
                    suggestions.setItems(suggestionsList.size() > 5 ? FXCollections.observableArrayList(suggestionsList.subList(0, 5)) : suggestionsList);
                    ignoreButton.setOnAction(e -> {
                        addWordsToIgnore(data);
                        popup.hide();
                        checkSpelling(area);
                    });
                    suggestions.setPrefHeight(suggestions.getItems().size() * ITEM_HEIGHT);

                    // Temporary check to remove ignore button on non spelling errors
                    if (data.rule.getSpecificRuleId().equals("MORFOLOGIK_RULE_EN_AU")) {
                        popupContent.getChildren().addAll(suggestions, ignoreButton, labelMessage);
                    } else {
                        popupContent.getChildren().addAll(suggestions, labelMessage);
                    }
                }
                
                suggestions.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
                    if (newValue != null) {
                        final StringBuilder builder = new StringBuilder(area.getText());
                        builder.replace(data.startIndex, data.endsIndex, newValue);
                        area.replaceText​(builder.toString());
                    }
                    popup.hide();
                    checkSpelling(area);
                });

                popup.getContent().add(popupContent);
                popup.setAutoFix(true);
                popup.show(area, event.getScreenX(), event.getScreenY() + 10);
            }
        }
    }
    
    private static MisspeltWordData getSelectedMisspeltWordData(StyleableTextArea area) {
        final int cursorIndex = area.getCaretPosition();

        if (cursorIndex > 0 && cursorIndex < area.getText().length()) {
            final List<RuleMatch> matches = getMatches(area);
            for (final RuleMatch match : matches) {
                final int start = match.getFromPos();
                final int end = match.getToPos();
                if (cursorIndex >= start && cursorIndex <= end) {
                    return new MisspeltWordData(cursorIndex, match, start, end, area.getText().substring(start, end));

                }
            }
        }
        return null;
    }
    
    /**
     * Prevents highlighting while still typing. When a highlighted word is
     * corrected manually it'll be marked as correct, similar to that in
     * Microsoft Word.
     */
    public static boolean canCheckSpelling(final StyleableTextArea area) {
        final String newText = area.getText();
        final int caretPosition = area.getCaretPosition();
        if (!globalSpellCheckingEnabled()){
            return false;
        } else if (caretPosition == 0) {
            return true;
        } else if (caretPosition <= newText.length()) {
            final String charAtCaret = Character.toString(area.getText().charAt(caretPosition - 1));
            return !newText.isEmpty() && (area.isWordUnderCursorHighlighted(caretPosition - 1) || !charAtCaret.matches("[a-zA-Z0-9']"));
        } else {
            return false;
        }
    }
    
    // In the event that global spell checking 
    public static void globalSpellCheckingChanged(){
        boolean active = globalSpellCheckingEnabled();
        for (StyleableTextArea area : textAreas) {
            if (active){
                checkSpelling(area);
            } else {
                area.clearStyles();
            }
        }
    }

    public static void addWordsToIgnore(MisspeltWordData data) {
        if (spellingCheckRule != null) {
            spellingCheckRule.addIgnoreTokens(Arrays.asList(data.word));
        }
    }
    
    public static MenuItem getSpellCheckMenuItem(final StyleableTextArea area) {
        boolean localSpellCheckingEnabled = textAreas.contains(area);
        // CheckMenuItem to toggle turn On/Off Spell Checking. On by default
        final CheckMenuItem toggleSpellCheckMenuItem = new CheckMenuItem("Check Spelling");
        toggleSpellCheckMenuItem.setSelected(localSpellCheckingEnabled);
        toggleSpellCheckMenuItem.setDisable(!globalSpellCheckingEnabled());
        toggleSpellCheckMenuItem.setVisible(true);
        toggleSpellCheckMenuItem.setOnAction(event -> {
            area.enableSpellCheck(toggleSpellCheckMenuItem.isSelected());
        });
        return toggleSpellCheckMenuItem;
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        Platform.runLater(() ->{
            SpellChecker.globalSpellCheckingChanged();
        });
    }
    
    private static record MisspeltWordData(int curser, RuleMatch rule, int startIndex, int endsIndex, String word){};
}
