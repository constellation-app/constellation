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
import java.util.Collections;
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
    private static final List<String> misspells = new ArrayList<>();
    private final List<Object> matches = new ArrayList<>();
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

    static {
        // langToolStatic is used to initialize the JLanguageTool at the loading, because
        // the very first initializing of JLanguageTool is slow but after that it is fast.
        LANGTOOL_LOAD = CompletableFuture.supplyAsync(new Supplier<Void>() {
            @Override
            public Void get() {
                LanguagetoolClassLoader.loadDependencies();
                if (LanguagetoolClassLoader.getMultiThreadedJLanguageTool() != null) {
                    try {
                        final Constructor<?> constructor = LanguagetoolClassLoader.getLanguages().getDeclaredConstructor();
                        constructor.setAccessible(true);
                        final Object languages = constructor.newInstance();
                        language = LanguagetoolClassLoader.getLanguages().getMethod("getLanguageForShortCode", String.class).invoke(languages, "en-AU");
                        langToolStatic = LanguagetoolClassLoader.getMultiThreadedJLanguageTool().getDeclaredConstructor(LanguagetoolClassLoader.getLanguage()).newInstance(language);

                        //perform a check here to prevent the spell checking being too slow at the first word after loading consty
                        final Object initMatches = LanguagetoolClassLoader.getJLanguagetool().getMethod("check", String.class).invoke(langToolStatic, "random text");

                    } catch (final NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        logAndDisplayErrorMessage("Error while initializing spell checking. Spell checking may not be functioning.", ex);
                    }
                } else {
                    NotifyDisplayer.display("Error while loading spell checker. Spell checking will not be functioning.", NotifyDescriptor.ERROR_MESSAGE);
                }
                return null;
            }
        }, Executors.newSingleThreadExecutor());
    }

    public SpellChecker(final SpellCheckingTextArea spellCheckingTextArea) {
        textArea = spellCheckingTextArea;

        LANGTOOL_LOAD.thenRun(() -> {
            initializeRules();
        });

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

    Method checkMethod;
    Method getFromPosMethod;
    Method getToPosMethod;

    ArrayList<String> prevParts = new ArrayList<>();

    // may need two arrays for caching starts and ends
    final ArrayList<Integer> prevStarts = new ArrayList<>();
    final ArrayList<Integer> prevEnds = new ArrayList<>();

    // new 
    final ArrayList<Pair<Integer, Integer>> prevHighlights = new ArrayList<>();

    final ArrayList<Pair<Integer, Integer>> highlightsToRemove = new ArrayList<>();

    final int tokensPerPart = 20;

    /**
     * Check Spelling of the entire text. This will ensure scenarios like duplicate words, grammar mistakes etc. are
     * triggered
     */
    public void checkSpelling() {
        //textArea.clearStyles();
        misspells.clear();

        if (turnOffSpellChecking || !StringUtils.isNotBlank(textArea.getText())) {
            return;
        }
        try {

            if (checkMethod == null) {
                checkMethod = LanguagetoolClassLoader.getJLanguagetool().getMethod("check", String.class);
            }

            final long startTimeSplit = System.currentTimeMillis();

            final ArrayList<String> parts = new ArrayList<>();
            final ArrayList<Integer> partsOffsets = new ArrayList<>();

            final ArrayList<Pair<Integer, Integer>> partsSpans = new ArrayList<>();// new

            final String inputText = textArea.getText();
            int tokensRemaining = tokensPerPart;
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
                    tokensRemaining = tokensPerPart;
                }

                // If end of string, store remaining text in string
                if (i == inputText.length() - 1) {
                    final String partAsString = inputText.substring(subStringStart, i);
                    parts.add(partAsString);
                    partsOffsets.add(subStringStart);

                    partsSpans.add(new Pair(subStringStart, i));

                    subStringStart = i;
                    tokensRemaining = tokensPerPart;
                }
            }

            final ArrayList<String> diff = new ArrayList<>();
            final ArrayList<Integer> diffOffsets = new ArrayList<>();
            final ArrayList<Pair<Integer, Integer>> diffSpans = new ArrayList<>();// new

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

//            System.out.println("diff");
//            for (final String value : diff) {
//                System.out.println(value);
//            }
//
//            System.out.println("diffSpans");
//            for (final Pair<Integer, Integer> value : diffSpans) {
//                System.out.println(value);
//            }

            prevParts = parts;

            final long endTimeSplit = System.currentTimeMillis();
            System.out.println("Took " + (endTimeSplit - startTimeSplit) + " ms to split and find differences...");

            final long startTimeMatches = System.currentTimeMillis();
            matches.clear();
            int totalElements = 0;
            for (final String d : diff) {
                final List<Object> list = (List<Object>) checkMethod.invoke(langTool, d);
                matches.add(list); // treating matches like a list of lists
                totalElements += list.size();
            }

            final long endTimeMatches = System.currentTimeMillis();
            System.out.println("Took " + (endTimeMatches - startTimeMatches) + " ms to get matches...");

            final long startTimeLoop = System.currentTimeMillis();

            int startEndIndex = 0;
            final int[] starts = new int[totalElements];
            final int[] ends = new int[totalElements];

            // Delete elements from previous start and ends (which represent previous highlighted text) that will be changed
//            if (totalElements > 0) {
//                final int earliestChange = diffOffsets.getFirst();
//                final int latestChange = diffOffsets.getLast();
//                System.out.println("!!! earliestChange: " + earliestChange);
//                System.out.println("!!! latestChange: " + latestChange);
//
////                prevStarts.removeIf(n -> n >= earliestChange && n < latestChange);
////                prevEnds.removeIf(n -> n > earliestChange && n <= latestChange);
//                prevStarts.removeIf(n -> n >= earliestChange);
//                prevEnds.removeIf(n -> n > earliestChange);
//
//                prevHighlights.removeIf(n -> n.getKey() >= earliestChange);
//            }

            // for each list of matches
            for (int i = 0; i < matches.size(); i++) {
                final List<Object> listOfMatches = (List<Object>) matches.get(i);

                for (final Object match : listOfMatches) {
                    if (!LanguagetoolClassLoader.getRuleMatch().isInstance(match)) {
                        continue;
                    }

                    try {
                        if (getFromPosMethod == null) {
                            getFromPosMethod = LanguagetoolClassLoader.getRuleMatch().getMethod("getFromPos");
                        }

                        if (getToPosMethod == null) {
                            getToPosMethod = LanguagetoolClassLoader.getRuleMatch().getMethod("getToPos");
                        }

                        final int start = (int) getFromPosMethod.invoke(match);
                        final int end = (int) getToPosMethod.invoke(match);
                        final String misspell = textArea.getText().substring(start, end);
                        misspells.add(misspell);
                        // Add offset because start and end value is reletive to the string it's in, not the whole text input
                        starts[startEndIndex] = start + diffOffsets.get(i);
                        ends[startEndIndex] = end + diffOffsets.get(i);

                        startEndIndex++;
                    } catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
                        logAndDisplayErrorMessage("Error while checking spelling. It may not be functioning properly.", ex);
                    }
                }
            }
            final long endTimeLoop = System.currentTimeMillis();
            System.out.println("Took " + (endTimeLoop - startTimeLoop) + " ms to loop...");
            
            System.out.println("starts.length " + starts.length);

            // Put all new start values in prevStarts list
            // TODO: find if theres a clean way to do this
//            for (final int value : starts) {
//                prevStarts.add(value);
//            }
//            Collections.sort(prevStarts);
//
//            for (final int value : ends) {
//                prevEnds.add(value);
//            }
//            Collections.sort(prevEnds);

            for (int i = 0; i < totalElements; i++) {
                prevHighlights.add(new Pair(starts[i], ends[i]));
            }

            // Convert to arrays
//            final int[] prevStartsArray = prevStarts.stream().mapToInt(i -> i).toArray();
//            final int[] prevEndsArray = prevEnds.stream().mapToInt(i -> i).toArray();

///
//            final int[] prevStartsArray = prevHighlights.stream().mapToInt(i -> i.getKey()).toArray();
//            final int[] prevEndsArray = prevHighlights.stream().mapToInt(i -> i.getValue()).toArray();

            if (totalElements > 0) {
                Platform.runLater(() -> {
                    final long startTimeHighlight = System.currentTimeMillis();
                    //textArea.clearStyles();
                    // clear only what needs to change
//                    if (!diffOffsets.isEmpty()) {
//                        textArea.clearStyles(diffOffsets.get(0), textArea.getLength()); // clears everything after first found difference, which works out currently
//                    }

                    // new
                    for (final Pair<Integer, Integer> span : diffSpans) {
                        textArea.clearStyle(span.getKey(), span.getValue());
                    }

                    //textArea.highlightTextMultiple(prevStartsArray, prevEndsArray);
                    textArea.highlightTextMultiple(starts, ends);

                    final long endTimeHighlight = System.currentTimeMillis();
                    System.out.println("Took " + (endTimeHighlight - startTimeHighlight) + " ms to clear and highlight...");
                });
            }
            //}
        } catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            logAndDisplayErrorMessage("Error while checking spelling. It may not be functioning properly.", ex);
        }
    }

    /**
     * Pop up the suggestions list if the word/phrase under the cursor is misspelled.
     *
     * @param event
     */
    public void popUpSuggestionsListAction(final MouseEvent event) {
        if (!turnOffSpellChecking) {
            final ObservableList<String> suggestionsList = FXCollections.observableArrayList();

            popup.hide();
            popup.setAutoFix(true);
            popup.setAutoHide(true);
            popup.setHideOnEscape(true);
            try {
                if (isWordUnderCursorMisspelled()) {
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

                    suggestionsList.addAll((List<String>) (LanguagetoolClassLoader.getRuleMatch().getMethod("getSuggestedReplacements").invoke(matches.get(indexOfMisspelledTextUnderCursor))));
                    if (suggestionsList.isEmpty()) {
                        labelMessage.setText("No matching suggestions available");
                        popupContent.getChildren().addAll(labelMessage);
                    } else {
                        suggestions.setItems(suggestionsList.size() > 5 ? FXCollections.observableArrayList(suggestionsList.subList(0, 5)) : suggestionsList);
                        ignoreButton.setOnAction(e -> {
                            this.addWordsToIgnore();
                        });
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
                }
            } catch (final SecurityException | InvocationTargetException | NoSuchMethodException | IllegalAccessException ex) {
                logAndDisplayErrorMessage("Error while populating suggestions. Spell checking may not be functioning properly.", ex);
            }
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

        for (final Object match : matches) {
            if (LanguagetoolClassLoader.getRuleMatch().isInstance(match)) {
                final int start = (int) LanguagetoolClassLoader.getRuleMatch().getMethod("getFromPos").invoke(match);
                final int end = (int) LanguagetoolClassLoader.getRuleMatch().getMethod("getToPos").invoke(match);
                if (cursorIndex >= start && cursorIndex <= end) {
                    indexOfMisspelledTextUnderCursor = misspells.indexOf(textArea.getText().substring(start, end));
                    startOfMisspelledTextUnderCursor = start;
                    endOfMisspelledTextUnderCursor = end;
                    specificRuleId = (String) LanguagetoolClassLoader.getRuleMatch().getMethod("getSpecificRuleId").invoke(match);
                    labelMessage.setText((String) LanguagetoolClassLoader.getRuleMatch().getMethod("getMessage").invoke(match));
                    return true;
                }
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
}
