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
package au.gov.asd.tac.constellation.views.wordcloud.ui;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.views.wordcloud.content.PhraseTokenHandler;
import au.gov.asd.tac.constellation.views.wordcloud.content.SparseMatrix;
import au.gov.asd.tac.constellation.views.wordcloud.content.TaggedSparseMatrix;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.apache.commons.math3.stat.inference.TTest;

/**
 * A model of the word cloud generated from specific graph elements and a
 * textual attribute. Holds information about which graph elements contained
 * which words, the frequencies with which each word occurred, and stative data
 * relating to the selection, sorting and display of words on a WordCloudPane.
 *
 * @author twilight_sparkle
 */
public class WordCloud {

    public static final String WORD_CLOUD_ATTR = "WordCloud";
    public static final String WORD_CLOUD_DESCR = "Word Cloud";
    // Model data
    private final Map<String, Integer> wordsToHashes;
    private final Map<Integer, Set<Integer>> hashedWordSets;
    private final GraphElementType elementType;
    private final SortedMap<String, Float> wordListWithSizes;
    private final SortedMap<String, Float> wordListBySize;
    private final SortedMap<Double, Set<String>> wordSignificances;
    private SortedSet<String> currentWords;
    private String queryInfoString;
    // Stative data 
    private long modCount = 0;
    private final Set<String> selectedWords;
    private double currentSignificance = 0.05;
    private boolean isUnionSelect = true;
    private boolean isSizeSorted = true;
    private final boolean hasSignificances;

    /**
     * Constructor with all data explicity provided - used for saving and
     * loading
     *
     */
    public WordCloud(final Map<String, Integer> wordsToHashes, final Map<Integer, Set<Integer>> hashedWordSets, final GraphElementType elementType, final SortedMap<String, Float> wordListWithSizes, final SortedMap<Double, Set<String>> wordSignificances, final double currentSignificance, final String queryInfoString, final long modCount, final Set<String> selectedWords, final boolean isUnionSelect, final boolean isSizeSorted) {
        this.wordsToHashes = wordsToHashes;
        this.hashedWordSets = hashedWordSets;
        this.elementType = elementType;
        this.wordListWithSizes = wordListWithSizes;
        this.wordSignificances = wordSignificances;
        this.currentSignificance = currentSignificance;
        this.queryInfoString = queryInfoString;
        this.modCount = modCount;
        this.selectedWords = selectedWords;
        this.isUnionSelect = isUnionSelect;
        this.isSizeSorted = isSizeSorted;

        // Form a map of the words sorted by size
        wordListBySize = new TreeMap<>((final String o1, final String o2) -> {
            int wordSizeCompare = WordCloud.this.wordListWithSizes.get(o2).compareTo(WordCloud.this.wordListWithSizes.get(o1)); // Larger sized words should be earlier in the list
            return wordSizeCompare != 0 ? wordSizeCompare : o1.compareTo(o2); // For words with the same size, sort alphabectically.
        });
        wordListBySize.putAll(wordListWithSizes);
        hasSignificances = !wordSignificances.isEmpty();
        currentWords = new TreeSet<>((Comparator<String>) (isSizeSorted ? wordListBySize.comparator() : wordListWithSizes.comparator()));
        currentWords.addAll(wordListWithSizes.keySet());
        updateCurrentWords(1, currentSignificance);
    }

    /**
     * Constructs a new word cloud based on the results of tokenizing from a
     * ContentTokenizingServices object.
     */
    public WordCloud(final PhraseTokenHandler handler, final GraphElementType elementType, final int threshold, final boolean filterAllWords) {
        this(handler, null, elementType, threshold, filterAllWords);
    }

    public WordCloud(final PhraseTokenHandler handler, final PhraseTokenHandler bgHandler, final GraphElementType elementType, final int threshold, final boolean filterAllWords) {
        this(handler, bgHandler, elementType, threshold, filterAllWords, null);
    }

    /**
     * Constructs a new word cloud based on the results of tokenizing from a
     * ContentTokenizingServices object, but copying the sorting and selection
     * options from a pre-existing word cloud.
     */
    public WordCloud(final PhraseTokenHandler handler, final PhraseTokenHandler bgHandler, final GraphElementType elementType, final int threshold, final boolean filterAllWords, final WordCloud prevCloud) {
        if (prevCloud != null) {
            isUnionSelect = prevCloud.isUnionSelect;
            isSizeSorted = prevCloud.isSizeSorted;
            currentSignificance = prevCloud.currentSignificance;
        }
        selectedWords = new HashSet<>();
        final SparseMatrix<Integer> hashesToElements = handler.getTokenElementMatrix();
        hashedWordSets = hashesToElements.constructTokenSets();
        wordsToHashes = handler.getTokenHashes();
        this.elementType = elementType;
        wordListWithSizes = new TreeMap<>();
        wordSignificances = new TreeMap<>();
        final int maxSum = ((TaggedSparseMatrix<Integer>) hashesToElements).getLargestColumnSumWithTag(false);
        SparseMatrix<Integer> hashesToElementBg = null;
        if (bgHandler != null) {
            hashesToElementBg = bgHandler.getTokenElementMatrix();
        }
        for (final Entry<String, Integer> word : wordsToHashes.entrySet()) {

            // Skip this word if it is tagged as a single word constituent of a phrase, or if it doesn't meet the threshold
            if (((TaggedSparseMatrix) hashesToElements).getTag(word.getValue()) || hashedWordSets.get(word.getValue()).size() < threshold) {
                continue;
            }

            final Set<Integer> individualWordKeys = handler.getConstituentHashes().get(word.getValue());
            final float relativeSize = ((float) hashesToElements.getColumnSum(word.getValue())) / maxSum;
            wordListWithSizes.put(word.getKey(), relativeSize);

            if (bgHandler != null) {
                final double sig = tTestPhraseAgainstWords(word.getValue(), individualWordKeys, hashesToElements, hashesToElementBg, filterAllWords);
                Set<String> l = wordSignificances.get(sig);
                if (l == null) {
                    l = new HashSet<>();
                    wordSignificances.put(sig, l);
                }
                l.add(word.getKey());
            }
        }

        // Form a map of the words sorted by size 
        wordListBySize = new TreeMap<>((final String o1, final String o2) -> {
            final int wordSizeCompare = wordListWithSizes.get(o2).compareTo(wordListWithSizes.get(o1)); // Larger sized words should be earlier in the list
            return wordSizeCompare != 0 ? wordSizeCompare : o1.compareTo(o2); // For words with the same size, sort alphabetically 
        });
        wordListBySize.putAll(wordListWithSizes);
        hasSignificances = !wordSignificances.isEmpty();
        currentWords = new TreeSet<>((Comparator<String>) (isSizeSorted ? wordListBySize.comparator() : wordListWithSizes.comparator()));
        currentWords.addAll(wordListWithSizes.keySet());
        updateCurrentWords(1, currentSignificance);
    }

    public long getModCount() {
        return modCount;
    }
    
    public void setModCount(final long modCount) {
        this.modCount = modCount;
    }
    
    public static double tTestPhraseAgainstWords(final int key, final Set<Integer> individualWordKeys, final SparseMatrix<Integer> graph, final SparseMatrix<Integer> model, final boolean filterAllWords) {
        // Get all the elements containing the individual words, then get the arrays of phrase frequencies, extended by 0s
        // by the elements containing all constituent individual words but not the phrase 
        final Set<Integer> graphElements = filterAllWords ? graph.getColumnElementIntersection(individualWordKeys) : graph.getColumnElementUnion(individualWordKeys);
        Integer[] graphColumn = graph.getConstituentExtendedColumnAsArray(key, graphElements);
        final Set<Integer> modelElements = filterAllWords ? model.getColumnElementIntersection(individualWordKeys) : model.getColumnElementUnion(individualWordKeys);
        Integer[] modelColumn = model.getConstituentExtendedColumnAsArray(key, modelElements);

        // Do dummy resampling if the sample size of the graph was 1 or the sample size fo the model was 0 or 1
        if (graphColumn.length == 1) {
            final Integer[] dummyResample = {graphColumn[0], graphColumn[0]};
            graphColumn = dummyResample;
        }
        if (modelColumn == null) {
            final Integer[] dummyResample = {0, 0};
            modelColumn = dummyResample;
        } else if (modelColumn.length == 1) {
            final Integer[] dummyResample = {modelColumn[0], modelColumn[0]};
            modelColumn = dummyResample;
        }

        // Convert the arrays to their primitive types 
        final double[] graphColumnD = new double[graphColumn.length];
        for (int i = 0; i < graphColumnD.length; i++) {
            graphColumnD[i] = graphColumn[i];
        }

        final double[] modelColumnD = new double[modelColumn.length];
        for (int i = 0; i < modelColumnD.length; i++) {
            modelColumnD[i] = modelColumn[i];
        }

        // Return default values when both distributions have zero variance to avoid NaNs.
        final Variance v = new Variance();
        final Mean m = new Mean();
        if (v.evaluate(graphColumnD) == 0 && v.evaluate(modelColumnD) == 0) {
            return m.evaluate(graphColumnD) == m.evaluate(modelColumnD) ? 1 : 0;
        }

        // Run Welch's t-test through the apache common-math T-Testing framework
        final TTest t = new TTest();
        return t.tTest(graphColumnD, modelColumnD);
    }

    /**
     * Constructs and sets the string which provides information as to the
     * parameters used to generate this word cloud
     */
    public void setQueryInfo(final int phraseLength, final int proximity, final String attrName) {
        queryInfoString = String.format("Phrases of length %d and span %d from the %s attribute '%s'.", phraseLength, proximity, elementType.getLabel(), attrName);
    }

    /**
     * Gets he string containing information about the parameters from the query
     * used to generate this word cloud
     */
    public String getQueryInfo() {
        return queryInfoString;
    }

    public boolean getHasSignificances() {
        return hasSignificances;
    }

    /**
     * Gets a map relating word hashes to ID's of element which contain the
     * words
     */
    public Map<Integer, Set<Integer>> getHashedWordSets() {
        return hashedWordSets;
    }

    /**
     * Gets a map relating words to their hashes
     */
    public Map<String, Integer> getWordToHashes() {
        return wordsToHashes;
    }
    
    /**
     * Sets whether or not the selection mode is union.
     */
    public void setIsUnionSelect(final boolean val) {
        isUnionSelect = val;
    }

    /**
     * Sets whether or not the sorting mode is by size
     */
    public void setIsSizeSorted(final boolean val) {
        isSizeSorted = val;
        @SuppressWarnings("unchecked") //Comparator will be of strings
        final SortedSet<String> copy = new TreeSet<>((Comparator<String>) (isSizeSorted ? wordListBySize.comparator() : wordListWithSizes.comparator()));
        copy.addAll(currentWords);
        currentWords = copy;
    }

    public void setCurrentSignificance(final double val) {
        updateCurrentWords(currentSignificance, val);
        currentSignificance = val;
    }
    
    /**
     * Gets whether or not the selection mode is union
     */
    public boolean getIsUnionSelect() {
        return isUnionSelect;
    }

    /**
     * Gets whether or not the sorting mode is by size
     */
    public boolean getIsSizeSorted() {
        return isSizeSorted;
    }

    public double getCurrentSignificance() {
        return currentSignificance;
    }

    /**
     * Returns the set of graph elements used to generate this cloud that
     * contain any of the currently selected words in this cloud
     */
    private Set<Integer> getElementsWithAnyWords() {
        final Set<Integer> set = new HashSet<>();
        selectedWords.stream().map(wordsToHashes::get).forEachOrdered(key -> 
            set.addAll(hashedWordSets.get(key)));
        return set;
    }

    /**
     * Returns the set of graph elements used to generate this cloud that
     * contain any of the currently selected words in this cloud
     */
    private Set<Integer> getElementsWithAllWords() {
        final Set<Integer> set = new HashSet<>();
        boolean firstSet = true;
        for (final String word : selectedWords) {
            final Integer key = wordsToHashes.get(word);
            final Set<Integer> wordSet = hashedWordSets.get(key);
            if (firstSet) {
                set.addAll(wordSet);
                firstSet = false;
            } else if (set.isEmpty()) {
                break;
            } else {
                final Iterator<Integer> iter = set.iterator();
                while (iter.hasNext()) {
                    final Integer el = iter.next();
                    if (!wordSet.contains(el)) {
                        iter.remove();
                    }
                }
            }
        }
        return set;
    }

    /**
     * Returns a set of graph elements corresponding to the currently selected
     * words and current selection mode
     */
    public Set<Integer> getElementsCorrespondingToSelection() {
        if (isUnionSelect) {
            return getElementsWithAnyWords();
        } else {
            return getElementsWithAllWords();
        }
    }

    /**
     * Add a word to the currently selected words
     */
    public void addWordToSelection(final String word) {
        selectedWords.add(word);
    }

    /**
     * Remove a word from the currently selected words
     */
    public void removeWordFromSelection(final String word) {
        selectedWords.remove(word);
    }

    /**
     * Set the collection of currently selected words to contain just a single
     * word
     */
    public void singleWordSelection(final String word) {
        selectedWords.clear();
        selectedWords.add(word);
    }

    /**
     * Gets a set containing all the currently selected words
     */
    public Set<String> getSelectedWords() {
        return selectedWords;
    }

    /**
     * Gets the GraphElementTupe which this word cloud was generated from
     */
    public GraphElementType getElementType() {
        return elementType;
    }

    /**
     * Gets the map which relates words to their significance
     */
    public SortedMap<Double, Set<String>> getWordSignificances() {
        return wordSignificances;
    }

    /**
     * Gets the map which relates words to their font sizes, sorted
     */
    public SortedMap<String, Float> getWordListWithSizes() {
        return wordListWithSizes;
    }

    /**
     * Gets the map which relates words to their font sizes, sorted by size
     */
    public SortedMap<String, Float> getWordListBySize() {
        return wordListBySize;
    }

    private void updateCurrentWords(final double oldSig, final double newSig) {
        final boolean remove = oldSig > newSig;
        final double low = remove ? newSig : oldSig;
        final double high = remove ? oldSig : newSig;
        final Map<Double, Set<String>> changeSets = wordSignificances.headMap(high + Double.MIN_NORMAL).tailMap(low + Double.MIN_NORMAL);
        if (remove) {
            changeSets.values().forEach(changeSet -> 
                changeSet.forEach(word -> 
                    currentWords.add(word)));
        }
    }

    public SortedMap<String, Float> getAllWords() {
        return isSizeSorted ? wordListBySize : wordListWithSizes;
    }

    /**
     * Gets the map which relates words to their font sizes, sorted using the
     * current method of sorting
     */
    public SortedSet<String> getCurrentWordList() {
        return currentWords;
    }
}
