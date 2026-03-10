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

import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.openide.NotifyDescriptor;

/**
 * Handles the SpellChecking functions of SpellCheckingTextArea. SpellChecker evaluates incorrect words/phrases and pops
 * up the suggestions when the user prompts
 *
 * @author Auriga2
 */
public final class SpellChecker {

    private final SpellCheckingTextArea textArea;
    //private static final List<String> misspells = new ArrayList<>();
    //private final List<Object> matches = new ArrayList<>();
    private final List<Match> matches2 = new ArrayList<>();
    private int indexOfMisspelledTextUnderCursor;       // position of the current misspelled text in misspells list
    private final ListView<String> suggestions = new ListView<>(FXCollections.observableArrayList());
    private Object langTool;
    private static Object langToolStatic;
    private Object spellingCheckRule;
    private Popup popup = new Popup();
    private final Label labelMessage = new Label();
    private boolean turnOffSpellChecking = false;
    private int startOfMisspelledTextUnderCursor;
    private int endOfMisspelledTextUnderCursor;
    private String specificRuleId;
    private static final Logger LOGGER = Logger.getLogger(SpellChecker.class.getName());
    private static final double POPUP_PADDING = 5;
    private static final double ITEM_HEIGHT = 24;
    private static Object language = null;
    private static final String NON_WORD_PATTERN = "[\\W]"; //excludes alphanumeric characters including the underscore
    private static final Pattern NON_WORD_MATCHER = Pattern.compile(NON_WORD_PATTERN);

    protected static final CompletableFuture<Void> LANGTOOL_LOAD;

    private static final int TOKENS_PER_PART = 20;
    private List<String> prevParts = new ArrayList<>();

    private static Method getFromPos;
    private static Method getToPos;
    private static Method getSuggestedReplacements;
    private static Method getSpecificRuleId;
    private static Method getMessage;

    static {
        // langToolStatic is used to initialize the JLanguageTool at the loading, because
        // the very first initializing of JLanguageTool is slow but after that it is fast.
        LANGTOOL_LOAD = CompletableFuture.supplyAsync(new Supplier<Void>() {
            @Override
            public Void get() {
                LanguagetoolClassLoader.loadDependencies();
                if (LanguagetoolClassLoader.getMultiThreadedJLanguageTool() == null) {
                    NotifyDisplayer.display("Error while loading spell checker. Spell checking will not be functioning.", NotifyDescriptor.ERROR_MESSAGE);
                    return null;
                }

                try {
                    final Constructor<?> constructor = LanguagetoolClassLoader.getLanguages().getDeclaredConstructor();
                    constructor.setAccessible(true);
                    final Object languages = constructor.newInstance();
                    language = LanguagetoolClassLoader.getLanguages().getMethod("getLanguageForShortCode", String.class).invoke(languages, "en-AU");
                    langToolStatic = LanguagetoolClassLoader.getMultiThreadedJLanguageTool().getDeclaredConstructor(LanguagetoolClassLoader.getLanguage()).newInstance(language);

                    //perform a check here to prevent the spell checking being too slow at the first word after loading consty
                    LanguagetoolClassLoader.getJLanguagetool().getMethod("check", String.class).invoke(langToolStatic, "random text");
                } catch (final NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    logAndDisplayErrorMessage("Error while initializing spell checking. Spell checking may not be functioning.", ex);
                }
                return null;
            }
        }, Executors.newSingleThreadExecutor());
    }

    public SpellChecker(final SpellCheckingTextArea spellCheckingTextArea) {
        textArea = spellCheckingTextArea;

        initMethods();

        LANGTOOL_LOAD.thenRun(() -> initializeRules());
        //initialize popup
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

    private void initializeRules() {
        try {
            langTool = LanguagetoolClassLoader.getMultiThreadedJLanguageTool().getDeclaredConstructor(LanguagetoolClassLoader.getLanguage()).newInstance(language);
            final List<?> rules = (List<?>) LanguagetoolClassLoader.getJLanguagetool().getMethod("getAllRules").invoke(langTool);
            for (final Object rule : rules) {
                if (LanguagetoolClassLoader.getRule().getMethod("getId").invoke(rule).equals("UPPERCASE_SENTENCE_START")) {
                    LanguagetoolClassLoader.getJLanguagetool().getMethod("disableRule", String.class).invoke(langTool, LanguagetoolClassLoader.getRule().getMethod("getId").invoke(rule));

                } else if (LanguagetoolClassLoader.getSpellingCheckRule().isInstance(rule)) {
                    spellingCheckRule = rule;
                }
            }
        } catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException | SecurityException ex) {
            logAndDisplayErrorMessage("Error while initializing spell checking rules. Spell checking may not be functioning properly.", ex);
        }
    }

    private void initMethods() {
        try {
            if (getFromPos == null) {
                getFromPos = LanguagetoolClassLoader.getRuleMatch().getMethod("getFromPos");
            }
            if (getToPos == null) {
                getToPos = LanguagetoolClassLoader.getRuleMatch().getMethod("getToPos");
            }
            if (getSuggestedReplacements == null) {
                getSuggestedReplacements = LanguagetoolClassLoader.getRuleMatch().getMethod("getSuggestedReplacements");
            }
            if (getSpecificRuleId == null) {
                getSpecificRuleId = LanguagetoolClassLoader.getRuleMatch().getMethod("getSpecificRuleId");
            }
            if (getMessage == null) {
                getMessage = LanguagetoolClassLoader.getRuleMatch().getMethod("getMessage");
            }
        } catch (final NoSuchMethodException | SecurityException ex) {
        }
    }

    /**
     * Check Spelling of the entire text. This will ensure scenarios like duplicate words, grammar mistakes etc. are
     * triggered
     */
    public void checkSpelling() {
        System.out.println("checkSpelling");
        //misspells.clear();

        final String inputText = textArea.getText();

        if (turnOffSpellChecking || StringUtils.isBlank(inputText)) {
            System.out.println("text blank");
            matches2.clear();
            return;
        }

        try {
            final ArrayList<String> parts = new ArrayList<>();
            final ArrayList<Integer> partsOffsets = new ArrayList<>();
            final ArrayList<Pair<Integer, Integer>> partsSpans = new ArrayList<>();

            int tokensRemaining = TOKENS_PER_PART;
            int subStringStart = 0;
            for (int i = 0; i < inputText.length(); i++) {
                final char character = inputText.charAt(i);

                // non word character
                if (!Character.isLetterOrDigit(character)) {
                    tokensRemaining--;
                }

                // If we've counted enough tokens
                if (tokensRemaining <= 0) {
                    final String partAsString = inputText.substring(subStringStart, i);
                    parts.add(partAsString);
                    partsOffsets.add(subStringStart);

                    partsSpans.add(new Pair(subStringStart, i));

                    subStringStart = i;
                    tokensRemaining = TOKENS_PER_PART;
                }

                // If end of string, store remaining text in string
                if (i == inputText.length() - 1) {
                    final String partAsString = inputText.substring(subStringStart, i);
                    parts.add(partAsString);
                    partsOffsets.add(subStringStart);

                    partsSpans.add(new Pair(subStringStart, i));

                    subStringStart = i;
                    tokensRemaining = TOKENS_PER_PART;
                }
            }

            final ArrayList<String> diff = new ArrayList<>();
            final ArrayList<Integer> diffOffsets = new ArrayList<>();
            final ArrayList<Pair<Integer, Integer>> diffSpans = new ArrayList<>();

            // find major differences
            if (prevParts.isEmpty()) {
                // All new input will be different to old
                diff.addAll(parts);
                diffOffsets.addAll(partsOffsets);
                diffSpans.addAll(partsSpans);
            } else {
                // Iterate through the current input text and the prev input text and find differences
                for (int i = 0; i < prevParts.size() || i < parts.size(); i++) {
                    // If out of new input
                    if (i >= parts.size()) {
                        break;
                    }

                    // If elem doesnt exist in prev input, add it as it must be new
                    if (i >= prevParts.size()) {
                        diff.add(parts.get(i));
                        diffOffsets.add(partsOffsets.get(i));
                        diffSpans.add(partsSpans.get(i));
                        continue;
                    }

                    // If current elements are different, add to diff list
                    if (!prevParts.get(i).equals(parts.get(i))) {
                        diff.add(parts.get(i));
                        diffOffsets.add(partsOffsets.get(i));
                        diffSpans.add(partsSpans.get(i));
                    }

                }
            }

            final List<Object> matchesLocal = new ArrayList<>();

            // prune matches2, so that data being overwrriten is gone
            for (final Pair<Integer, Integer> span : diffSpans) {
                final int spanStart = span.getKey();
                final int spanEnd = span.getValue();

                final List<Match> toRemove = new ArrayList<>();
                for (final Match match : matches2) {

                    final int start = match.getFromPos();
                    final int end = match.getToPos();

                    if (start >= spanStart && end <= spanEnd) {
                        toRemove.add(match);
                    }
                }
                System.out.println("Removing from matches2: " + toRemove);
                matches2.removeAll(toRemove);
            }

            int totalElements = 0;
            // TODO: change this list of lists business to a regular list, with NEW rulematch variables with all the same data EXCEPT the to and from variables have the offsets added to them
//            for (final String d : diff) {
            for (int i = 0; i < diff.size(); i++) {
                final String d = diff.get(i);
                final List<Object> list = (List<Object>) LanguagetoolClassLoader.getJLanguagetool().getMethod("check", String.class).invoke(langTool, d);

                for (final Object ruleMatch : list) {
                    //final RuleMatch ruleMatch = new RuleMatch((RuleMatch) match);
                    final Match match = createMatch(ruleMatch, diffOffsets.get(i));
                    //System.out.println("Custom match to: " + match.getToPos() + " from: " + match.getFromPos());
                    matches2.add(match);
                }

                matchesLocal.add(list); // treating matches like a list of lists, because the index of each list corresponds to an index in diffOffsets to offset the start and end variables by
                totalElements += list.size();
            }

            System.out.println(matches2);

            //matches.clear(); //TODO REMOVE
            // Remove old matches in the ranges of what has changed
            // MIGHT NOT BE CORRECT IF MATCHES IS A LIST OF LISTS?
            // also current problem is matches to and from values are NOT offset to a correct global position
            /*
            for (final Pair<Integer, Integer> span : diffSpans) {
                final int spanStart = span.getKey();
                final int spanEnd = span.getValue();

                final List<Object> toRemove = new ArrayList<>();
                for (final Object match : matches) {
                    System.out.println("REMOVE: " + match);
                    if (!LanguagetoolClassLoader.getRuleMatch().isInstance(match)) {
                        continue;
                    }

                    final int start = (int) LanguagetoolClassLoader.getRuleMatch().getMethod("getFromPos").invoke(match);
                    final int end = (int) LanguagetoolClassLoader.getRuleMatch().getMethod("getToPos").invoke(match);

                    if (start >= spanStart && end <= spanEnd) {
                        toRemove.add(match);
                    }
                }

                matches.removeAll(toRemove);
            }
             */
            //matches.addAll(matchesLocal);
            int startEndIndex = 0;
            final int[] starts = new int[totalElements];
            final int[] ends = new int[totalElements];

            // for each list of matches
            for (int i = 0; i < matchesLocal.size(); i++) {
                final List<Object> listOfMatches = (List<Object>) matchesLocal.get(i);

                for (final Object match : listOfMatches) {
                    if (!LanguagetoolClassLoader.getRuleMatch().isInstance(match)) {
                        continue;
                    }

                    final int start = (int) LanguagetoolClassLoader.getRuleMatch().getMethod("getFromPos").invoke(match);
                    final int end = (int) LanguagetoolClassLoader.getRuleMatch().getMethod("getToPos").invoke(match);

                    //final String misspell = inputText.substring(start, end);
                    //misspells.add(misspell); // TODO: this might also not be accurate anymore
                    // Add offset because start and end value is reletive to the string it's in, not the whole text input
                    starts[startEndIndex] = start + diffOffsets.get(i);
                    ends[startEndIndex] = end + diffOffsets.get(i);

                    startEndIndex++;
                }
            }

            // NEW
//            misspells.clear();
//            for (final Object match : matches) {
//                System.out.println("MISSPELLS: " + match);
//                if (!LanguagetoolClassLoader.getRuleMatch().isInstance(match)) {
//                    continue;
//                }
//
//                final int start = (int) LanguagetoolClassLoader.getRuleMatch().getMethod("getFromPos").invoke(match);
//                final int end = (int) LanguagetoolClassLoader.getRuleMatch().getMethod("getToPos").invoke(match);
//
//                final String misspell = inputText.substring(start, end);
//                misspells.add(misspell);
//            }
//            System.out.println(misspells);
            if (totalElements > 0) {
                Platform.runLater(() -> {
                    for (final Pair<Integer, Integer> span : diffSpans) {
                        textArea.clearStyle(span.getKey(), span.getValue());
                    }

                    textArea.highlightTextMultiple(starts, ends);
                });
            }

            prevParts = parts;
        } catch (final IllegalAccessException | NoSuchMethodException ex) {
            logAndDisplayErrorMessage("Error while checking spelling. It may not be functioning properly.", ex);
        } catch (final InvocationTargetException ex) {
            // Left intentionally blank, as interrupting this function causes an InvocationTargetException
        }
    }

    // Todo: move this into a contructor
    private Match createMatch(final Object ruleMatch, final int offset) {
        try {
            final int fromPos = (int) getFromPos.invoke(ruleMatch) + offset;
            final int toPos = (int) getToPos.invoke(ruleMatch) + offset;
            final List<String> suggestedReplacements = (List<String>) getSuggestedReplacements.invoke(ruleMatch);
            final String ruleMatchSpecificRuleId = (String) getSpecificRuleId.invoke(ruleMatch);
            final String message = (String) getMessage.invoke(ruleMatch);

            return new Match(fromPos, toPos, suggestedReplacements, ruleMatchSpecificRuleId, message);
        } catch (final InvocationTargetException | IllegalAccessException ex) {
            // TODO add logger
        }

        return null;
    }

    /**
     * Pop up the suggestions list if the word/phrase under the cursor is misspelled.
     *
     * @param event
     */
    public void popUpSuggestionsListAction(final MouseEvent event) {
        if (turnOffSpellChecking) {
            return;
        }

        final ObservableList<String> suggestionsList = FXCollections.observableArrayList();

        popup.hide();
        popup.setAutoFix(true);
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);
        try {
            if (!isWordUnderCursorMisspelled()) {
                return;
            }

            final Button ignoreButton = new Button("Ignore All");
            final VBox popupContent = new VBox(POPUP_PADDING);
            popupContent.setStyle(
                    "-fx-background-color: black;"
                    + "-fx-text-fill: white;"
                    + "-fx-padding: " + POPUP_PADDING + ";");
            popupContent.getChildren().clear();
            suggestionsList.clear();
            popup.getContent().clear();
            suggestions.getSelectionModel().clearSelection();

//            suggestionsList.addAll((List<String>) (LanguagetoolClassLoader.getRuleMatch().getMethod("getSuggestedReplacements").invoke(matches.get(indexOfMisspelledTextUnderCursor))));
            suggestionsList.addAll(matches2.get(indexOfMisspelledTextUnderCursor).getSuggestedReplacements());
            if (suggestionsList.isEmpty()) {
                labelMessage.setText("No matching suggestions available");
                popupContent.getChildren().addAll(labelMessage);
            } else {
                suggestions.setItems(suggestionsList.size() > 5 ? FXCollections.observableArrayList(suggestionsList.subList(0, 5)) : suggestionsList);
                ignoreButton.setOnAction(e -> this.addWordsToIgnore());
                suggestions.setPrefHeight(suggestions.getItems().size() * ITEM_HEIGHT);

                // Temporary check to remove ignore button on non spelling errors
                if (specificRuleId.equals("MORFOLOGIK_RULE_EN_AU")) {
                    popupContent.getChildren().addAll(suggestions, ignoreButton, labelMessage);
                } else {
                    popupContent.getChildren().addAll(suggestions, labelMessage);
                }
            }

            popup.getContent().add(popupContent);
            popup.setAutoFix(true);
            popup.show(textArea, event.getScreenX(), event.getScreenY() + 10);

        } catch (final InvocationTargetException | NoSuchMethodException | IllegalAccessException ex) {
            logAndDisplayErrorMessage("Error while populating suggestions. Spell checking may not be functioning properly.", ex);
        }
    }

    /**
     * Retrieve the word/phrase under the cursor and check if it is misspelled. If it is misspelled the index is
     * populated.
     */
    // OLD FUNCTION
//    private boolean isWordUnderCursorMisspelled() throws InvocationTargetException, NoSuchMethodException, SecurityException, IllegalAccessException {
//        System.out.println("isWordUnderCursorMisspelled");
//        final int cursorIndex = textArea.getCaretPosition();
//
//        if (cursorIndex <= 0 || cursorIndex >= textArea.getText().length()) {
//            //= is to avoid the scenario of displaying the suggesttions of the first/last word
//            // (if they are incorrect) when clicking on the empty space right/below the text
//            return false;
//        }
//
////        System.out.println("isWordUnderCursorMisspelled " + matches);
//        for (final Object match : matches) {
//            if (!LanguagetoolClassLoader.getRuleMatch().isInstance(match)) {
//                continue;
//            }
//
//            final int start = (int) LanguagetoolClassLoader.getRuleMatch().getMethod("getFromPos").invoke(match);
//            final int end = (int) LanguagetoolClassLoader.getRuleMatch().getMethod("getToPos").invoke(match);
//            if (cursorIndex >= start && cursorIndex <= end) {
//                indexOfMisspelledTextUnderCursor = misspells.indexOf(textArea.getText().substring(start, end)); // This could be errors prone? Also misspells only purpose is to get the index in matches?
//                System.out.println("indexOfMisspelledTextUnderCursor " + indexOfMisspelledTextUnderCursor);
//                startOfMisspelledTextUnderCursor = start;
//                endOfMisspelledTextUnderCursor = end;
//                specificRuleId = (String) LanguagetoolClassLoader.getRuleMatch().getMethod("getSpecificRuleId").invoke(match);
//                labelMessage.setText((String) LanguagetoolClassLoader.getRuleMatch().getMethod("getMessage").invoke(match));
//                return true;
//            }
//        }
//        return false;
//    }
    private boolean isWordUnderCursorMisspelled() throws InvocationTargetException, NoSuchMethodException, SecurityException, IllegalAccessException {
        System.out.println("isWordUnderCursorMisspelled NEW");
        final int cursorIndex = textArea.getCaretPosition();

        if (cursorIndex <= 0 || cursorIndex >= textArea.getText().length()) {
            //= is to avoid the scenario of displaying the suggesttions of the first/last word
            // (if they are incorrect) when clicking on the empty space right/below the text
            return false;
        }

        for (int i = 0; i < matches2.size(); i++) {
            final Match match = matches2.get(i);

            final int start = match.getFromPos();
            final int end = match.getToPos();
            if (cursorIndex >= start && cursorIndex <= end) {
                indexOfMisspelledTextUnderCursor = i;
                System.out.println("indexOfMisspelledTextUnderCursor " + indexOfMisspelledTextUnderCursor);
                startOfMisspelledTextUnderCursor = start;
                endOfMisspelledTextUnderCursor = end;
                specificRuleId = match.getSpecificRuleId();
                labelMessage.setText(match.getMessage());
                return true;
            }
        }
        return false;
    }

    /**
     * Prevents highlighting while still typing. When a highlighted word is corrected manually it'll be marked as
     * correct, similar to that in Microsoft Word.
     *
     * @param newText
     * @return
     */
    public boolean canCheckSpelling(final String newText) {
        final int caretPosition = textArea.getCaretPosition();
        if (caretPosition == 0) {
            return true;
        } else if (caretPosition <= newText.length()) {
            final Matcher nonWordMatcher = NON_WORD_MATCHER.matcher(Character.toString(textArea.getText().charAt(caretPosition - 1)));
            return !newText.isEmpty() && (textArea.isWordUnderCursorHighlighted(caretPosition - 1) || nonWordMatcher.matches());
        } else {
            return false;
        }
    }

    public void turnOffSpellChecking(final boolean turnOffSpellChecking) {
        this.turnOffSpellChecking = turnOffSpellChecking;
    }

    public void addWordsToIgnore() {
        if (spellingCheckRule == null) {
            initializeRules();
        }
        if (spellingCheckRule != null) {
            try {
                final List<String> ss = Arrays.asList(textArea.getText().substring(startOfMisspelledTextUnderCursor, endOfMisspelledTextUnderCursor));
                LanguagetoolClassLoader.getSpellingCheckRule().getMethod("addIgnoreTokens", List.class).invoke(spellingCheckRule, ss);
                popup.hide();
                checkSpelling();
            } catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                logAndDisplayErrorMessage("Error while adding words to ignore. Spell checking may not be functioning properly.", ex);
            }
        }
    }

    private static void logAndDisplayErrorMessage(final String message, final Exception ex) {
        LOGGER.log(Level.SEVERE, String.format("%s: %s", message, ex));
        NotifyDisplayer.display(message, NotifyDescriptor.ERROR_MESSAGE);
    }

    private class Match {

        private final int fromPos;
        private final int toPos;
        private final List<String> suggestedReplacements;
        private final String specificRuleId;
        private final String message;

        public Match(final int fromPos, final int toPos, final List<String> suggestedReplacements, final String specificRuleId, final String message) {
            this.fromPos = fromPos;
            this.toPos = toPos;
            this.suggestedReplacements = suggestedReplacements;
            this.specificRuleId = specificRuleId;
            this.message = message;
        }

        public int getFromPos() {
            return fromPos;
        }

        public int getToPos() {
            return toPos;
        }

        public List<String> getSuggestedReplacements() {
            return suggestedReplacements;
        }

        public String getSpecificRuleId() {
            return specificRuleId;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return "{" + getFromPos() + ", " + getToPos() + "}";
        }
    }
}
