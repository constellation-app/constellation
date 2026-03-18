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
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;
import org.openide.NotifyDescriptor;

/**
 * Handles the SpellChecking functions of SpellCheckingTextArea. SpellChecker evaluates incorrect words/phrases and pops
 * up the suggestions when the user prompts
 *
 * @author Auriga2
 */
public final class SpellChecker {

    private final SpellCheckingTextArea textArea;
    private final List<Match> matches = new ArrayList<>();
    private int indexOfMisspelledTextUnderCursor;       // position of the current misspelled text in misspells list
    private Object langTool;
    private static Object langToolStatic;
    private Object spellingCheckRule;
    private final Label labelMessage = new Label();
    private boolean turnOffSpellChecking = false;
    private int startOfMisspelledTextUnderCursor;
    private int endOfMisspelledTextUnderCursor;
    private String specificRuleId;
    private static final Logger LOGGER = Logger.getLogger(SpellChecker.class.getName());
    private static final double MAX_SUGGESTIONS = 5;
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
    private static Method check;

    private final ContextMenu contextMenu = new ContextMenu();

    static {
        LanguagetoolClassLoader.loadDependencies();

        // langToolStatic is used to initialize the JLanguageTool at the loading, because
        // the very first initializing of JLanguageTool is slow but after that it is fast.
        LANGTOOL_LOAD = CompletableFuture.supplyAsync(new Supplier<Void>() {
            @Override
            public Void get() {
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
    }

    private void handleMenuItem(final String text) {
        final StringBuilder builder = new StringBuilder(textArea.getText());
        builder.replace(startOfMisspelledTextUnderCursor, endOfMisspelledTextUnderCursor, text);
        textArea.replaceText​(builder.toString());

        contextMenu.hide();
        checkSpelling();
        refreshHighlights();
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
            if (check == null) {
                check = LanguagetoolClassLoader.getJLanguagetool().getMethod("check", String.class);
            }
        } catch (final NoSuchMethodException | SecurityException ex) {
            LOGGER.log(Level.SEVERE, String.format("Error initialising methods in SpellChecker.java: %s", ex));
        }
    }

    private void checkSpellingForce() {
        prevParts.clear();
        textArea.clearStyles();
        checkSpelling();
    }

    /**
     * Check Spelling of the entire text. This will ensure scenarios like duplicate words, grammar mistakes etc. are
     * triggered
     */
    public synchronized void checkSpelling() {
        final String inputText = textArea.getText();

        if (turnOffSpellChecking || StringUtils.isBlank(inputText)) {
            matches.clear();
            return;
        }

        try {
            final List<String> parts = new ArrayList<>();
            final MutableIntList partsOffsets = new IntArrayList();
            final List<IntIntPair> partsSpans = new ArrayList<>();

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

                    partsSpans.add(PrimitiveTuples.pair(subStringStart, i));

                    subStringStart = i;
                    tokensRemaining = TOKENS_PER_PART;
                }

                // If end of string, store remaining text in string
                if (i == inputText.length() - 1) {
                    final String partAsString = inputText.substring(subStringStart, i);
                    parts.add(partAsString);
                    partsOffsets.add(subStringStart);

                    partsSpans.add(PrimitiveTuples.pair(subStringStart, i));

                    subStringStart = i;
                    tokensRemaining = TOKENS_PER_PART;
                }
            }

            final List<String> diff = new ArrayList<>();
            final MutableIntList diffOffsets = new IntArrayList();
            final List<IntIntPair> diffSpans = new ArrayList<>();

            // find major differences
            if (prevParts.isEmpty()) {
                // All new input will be different to old
                diff.addAll(parts);
                diffOffsets.addAll(partsOffsets);
                diffSpans.addAll(partsSpans);
            } else {
                // Iterate through the current input text and the prev input text and find differences
                for (int i = 0; i < prevParts.size() && i < parts.size(); i++) {

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

            final List<Object> listOfMatchLists = new ArrayList<>();

            // prune matches, so that data being overwritten is gone
            for (final IntIntPair span : diffSpans) {
                final int spanStart = span.getOne();
                final int spanEnd = span.getTwo();

                final List<Match> toRemove = new ArrayList<>();
                for (final Match match : matches) {

                    final int start = match.getFromPos();
                    final int end = match.getToPos();

                    if (start >= spanStart && end <= spanEnd) {
                        toRemove.add(match);
                    }
                }

                matches.removeAll(toRemove);
            }

            int totalElements = 0;
            for (int i = 0; i < diff.size(); i++) {
                final String d = diff.get(i);
                final List<Object> list = (List<Object>) check.invoke(langTool, d);

                for (final Object ruleMatch : list) {
                    final Match match = createMatch(ruleMatch, diffOffsets.get(i));
                    matches.add(match);
                }

                listOfMatchLists.add(list);
                totalElements += list.size();
            }

            int startEndIndex = 0;
            final int[] starts = new int[totalElements];
            final int[] ends = new int[totalElements];

            // for each list of matches
            for (int i = 0; i < listOfMatchLists.size(); i++) {
                final List<Object> matchList = (List<Object>) listOfMatchLists.get(i);

                for (final Object match : matchList) {
                    if (!LanguagetoolClassLoader.getRuleMatch().isInstance(match)) {
                        continue;
                    }

                    final int start = (int) getFromPos.invoke(match);
                    final int end = (int) getToPos.invoke(match);

                    // Add offset because start and end value is reletive to the string it's in, not the whole text input
                    starts[startEndIndex] = start + diffOffsets.get(i);
                    ends[startEndIndex] = end + diffOffsets.get(i);

                    startEndIndex++;
                }
            }

            if (totalElements > 0) {
                Platform.runLater(() -> {
                    for (final IntIntPair span : diffSpans) {
                        textArea.clearStyle(span.getOne(), span.getTwo());
                    }

                    textArea.highlightTextMultiple(starts, ends);
                });
            }

            prevParts = parts;
        } catch (final IllegalAccessException ex) {
            logAndDisplayErrorMessage("Error while checking spelling. It may not be functioning properly.", ex);
        } catch (final InvocationTargetException ex) {
            // Left intentionally blank, as this function is designed to be interrupted and interrupting this function causes an InvocationTargetException
        }
    }

    private void refreshHighlights() {
        final int totalElements = matches.size();
        final int[] starts = new int[totalElements];
        final int[] ends = new int[totalElements];
        for (int i = 0; i < totalElements; i++) {
            final Match m = matches.get(i);
            starts[i] = m.getFromPos();
            ends[i] = m.getToPos();
        }

        textArea.clearStyles();
        textArea.highlightTextMultiple(starts, ends);
    }

    /**
     * Creates a Match object with the relevant data from the given RuleMatch object, but adjusts the fromPos and toPos
     * by Offset value
     */
    private Match createMatch(final Object ruleMatch, final int offset) {
        try {
            final int fromPos = (int) getFromPos.invoke(ruleMatch) + offset;
            final int toPos = (int) getToPos.invoke(ruleMatch) + offset;
            final List<String> suggestedReplacements = (List<String>) getSuggestedReplacements.invoke(ruleMatch);
            final String ruleMatchSpecificRuleId = (String) getSpecificRuleId.invoke(ruleMatch);
            final String message = (String) getMessage.invoke(ruleMatch);

            return new Match(fromPos, toPos, suggestedReplacements, ruleMatchSpecificRuleId, message);
        } catch (final InvocationTargetException | IllegalAccessException ex) {
            LOGGER.log(Level.SEVERE, String.format("Error in creating match object in SpellChecker.java: %s", ex));
        }

        return null;
    }

    /**
     * Pop up the suggestions list if the word/phrase under the cursor is misspelled.
     *
     * @param event
     */
    public synchronized void popUpSuggestionsListAction(final MouseEvent event) {
        if (turnOffSpellChecking) {
            return;
        }

        contextMenu.hide();
        contextMenu.setAutoFix(true);
        contextMenu.setAutoHide(true);
        contextMenu.setHideOnEscape(true);
        try {
            if (!isWordUnderCursorMisspelled()) {
                return;
            }

            contextMenu.getItems().clear();
            final List<String> suggestionsList = matches.get(indexOfMisspelledTextUnderCursor).getSuggestedReplacements();

            if (suggestionsList.isEmpty()) {
                labelMessage.setText("No matching suggestions available");
            } else {
                final List<MenuItem> items = new ArrayList();

                for (int i = 0; i < suggestionsList.size() && i < MAX_SUGGESTIONS; i++) {
                    final MenuItem item = new MenuItem(suggestionsList.get(i));
                    item.setOnAction(e -> handleMenuItem(item.getText()));
                    items.add(item);
                }

                contextMenu.getItems().addAll(items);

                // Temporary check to remove ignore button on non spelling errors
                if (specificRuleId.equals("MORFOLOGIK_RULE_EN_AU")) {
                    // The button itself will not do anything
                    final Button ignoreButton = new Button("Ignore All");
                    // The menu item that contains the button will handle ignoring words
                    final CustomMenuItem customItemWithButton = new CustomMenuItem(ignoreButton);
                    customItemWithButton.setOnAction(e -> {
                        this.addWordsToIgnore();
                        checkSpellingForce();
                    });
                    contextMenu.getItems().add(customItemWithButton);
                }
            }

            contextMenu.setAutoFix(true);
            contextMenu.show(textArea, event.getScreenX(), event.getScreenY() + 10);
        } catch (final InvocationTargetException | NoSuchMethodException | IllegalAccessException ex) {
            logAndDisplayErrorMessage("Error while populating suggestions. Spell checking may not be functioning properly.", ex);
        }
    }

    /**
     * Retrieve the word/phrase under the cursor and check if it is misspelled. If it is misspelled the index is
     * populated.
     */
    private boolean isWordUnderCursorMisspelled() throws InvocationTargetException, NoSuchMethodException, SecurityException, IllegalAccessException {
        final int cursorIndex = textArea.getCaretPosition();

        if (cursorIndex <= 0 || cursorIndex >= textArea.getText().length()) {
            //= is to avoid the scenario of displaying the suggesttions of the first/last word
            // (if they are incorrect) when clicking on the empty space right/below the text
            return false;
        }

        for (int i = 0; i < matches.size(); i++) {
            final Match match = matches.get(i);

            final int start = match.getFromPos();
            final int end = match.getToPos();
            if (cursorIndex >= start && cursorIndex <= end) {
                indexOfMisspelledTextUnderCursor = i;
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
        }

        if (caretPosition <= newText.length()) {
            final Matcher nonWordMatcher = NON_WORD_MATCHER.matcher(Character.toString(textArea.getText().charAt(caretPosition - 1)));
            return !newText.isEmpty() && (textArea.isWordUnderCursorHighlighted(caretPosition - 1) || nonWordMatcher.matches());
        }

        return false;
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

    /**
     * Inner class that acts as data structure to some data from a RuleMatch object. This is done as data from a
     * RuleMatch can't be changed, such as fromPos and toPos.
     */
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
    }
}
