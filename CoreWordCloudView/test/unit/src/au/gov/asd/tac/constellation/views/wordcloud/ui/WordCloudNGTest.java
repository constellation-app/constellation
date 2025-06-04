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
package au.gov.asd.tac.constellation.views.wordcloud.ui;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.views.wordcloud.content.PhraseTokenHandler;
import au.gov.asd.tac.constellation.views.wordcloud.content.SparseMatrix;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for WordCloud 
 * 
 * @author Delphinus8821
 */
public class WordCloudNGTest {
    
    private final Map<String, Integer> wordsToHashes = new HashMap<>();
    private final Map<Integer, Set<Integer>> hashedWordSets = new HashMap<>();
    private final GraphElementType elementType = GraphElementType.TRANSACTION;
    private final SortedMap<String, Float> wordListWithSizes = new TreeMap<>();
    private final SortedMap<Double, Set<String>> wordSignificances = new TreeMap<>();
    private final double currentSignificance = 0;
    private final String queryInfoString = "";
    private final long modCount = 0;
    private final Set<String> selectedWords = new HashSet<>();
    private final boolean isUnionSelect = true;
    private final boolean isSizeSorted = false;

    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of setModCount method, of class WordCloud.
     */
    @Test
    public void testSetModCount() {
        System.out.println("setModCount");
        final long newModCount = 2L;
        final WordCloud instance = new WordCloud(wordsToHashes, hashedWordSets, elementType, wordListWithSizes, wordSignificances, currentSignificance, queryInfoString, modCount, selectedWords, isUnionSelect, isSizeSorted);
        instance.setModCount(newModCount);
        final long result = instance.getModCount();
        assertEquals(result, newModCount);
    }

    /**
     * Test of tTestPhraseAgainstWords method, of class WordCloud.
     */
    @Test
    public void testTTestPhraseAgainstWords() {
        System.out.println("tTestPhraseAgainstWords");
        final PhraseTokenHandler handler = new PhraseTokenHandler();
        final PhraseTokenHandler bgHandler = new PhraseTokenHandler();
        final int key = 1;
        final Set<Integer> individualWordKeys = new HashSet<>(Arrays.asList(1, 2, 0, 3));
        final SparseMatrix<Integer> graphMatrix = handler.getTokenElementMatrix();
        
        final SparseMatrix<Integer> model = bgHandler.getTokenElementMatrix();
        final boolean filterAllWords = true;
        final double expResult = 1.0;
        final double result = WordCloud.tTestPhraseAgainstWords(key, individualWordKeys, graphMatrix, model, filterAllWords);
        assertEquals(result, expResult, 1.0);
    }

    /**
     * Test of setQueryInfo method, of class WordCloud.
     */
    @Test
    public void testSetQueryInfo() {
        System.out.println("setQueryInfo");
        final int phraseLength = 2;
        final int proximity = 3;
        final String attrName = "Label";
        final WordCloud instance = new WordCloud(wordsToHashes, hashedWordSets, elementType, wordListWithSizes, wordSignificances, currentSignificance, queryInfoString, modCount, selectedWords, isUnionSelect, isSizeSorted);
        instance.setQueryInfo(phraseLength, proximity, attrName);
        final String expResult = String.format("Phrases of length %d and span %d from the %s attribute '%s'.", phraseLength, proximity, elementType.getLabel(), attrName);
        final String queryInfoResult = instance.getQueryInfo();
        assertEquals(expResult, queryInfoResult);
    }

    /**
     * Test of getHasSignificances method, of class WordCloud.
     */
    @Test
    public void testGetHasSignificances() {
        System.out.println("getHasSignificances");
        final WordCloud instance = new WordCloud(wordsToHashes, hashedWordSets, elementType, wordListWithSizes, wordSignificances, currentSignificance, queryInfoString, modCount, selectedWords, isUnionSelect, isSizeSorted);
        final boolean expResult = false;
        final boolean result = instance.hasSignificances();
        assertEquals(result, expResult);
    }

    /**
     * Test of getHashedWordSets method, of class WordCloud.
     */
    @Test
    public void testGetHashedWordSets() {
        System.out.println("getHashedWordSets");
        hashedWordSets.put(2, new HashSet<>());
        hashedWordSets.put(8, new HashSet<>());
        final WordCloud instance = new WordCloud(wordsToHashes, hashedWordSets, elementType, wordListWithSizes, wordSignificances, currentSignificance, queryInfoString, modCount, selectedWords, isUnionSelect, isSizeSorted);
        final Map expResult = hashedWordSets;
        final Map result = instance.getHashedWordSets();
        assertEquals(result, expResult);
    }

    /**
     * Test of getWordToHashes method, of class WordCloud.
     */
    @Test
    public void testGetWordToHashes() {
        System.out.println("getWordToHashes");
        wordsToHashes.put("Query1", 2);
        wordsToHashes.put("Query2", 8);
        final WordCloud instance = new WordCloud(wordsToHashes, hashedWordSets, elementType, wordListWithSizes, wordSignificances, currentSignificance, queryInfoString, modCount, selectedWords, isUnionSelect, isSizeSorted);
        final Map expResult = wordsToHashes;
        final Map result = instance.getWordToHashes();
        assertEquals(result, expResult);
    }

    /**
     * Test of setIsUnionSelect method, of class WordCloud.
     */
    @Test
    public void testSetIsUnionSelect() {
        System.out.println("setIsUnionSelect");
        final boolean val = false;
        final WordCloud instance = new WordCloud(wordsToHashes, hashedWordSets, elementType, wordListWithSizes, wordSignificances, currentSignificance, queryInfoString, modCount, selectedWords, isUnionSelect, isSizeSorted);
        instance.setIsUnionSelect(val);
        final boolean result = instance.isUnionSelect();
        assertEquals(val, result);
    }

    /**
     * Test of setIsSizeSorted method, of class WordCloud.
     */
    @Test
    public void testSetIsSizeSorted() {
        System.out.println("setIsSizeSorted");
        final boolean val = false;
        final WordCloud instance = new WordCloud(wordsToHashes, hashedWordSets, elementType, wordListWithSizes, wordSignificances, currentSignificance, queryInfoString, modCount, selectedWords, isUnionSelect, isSizeSorted);
        instance.setIsSizeSorted(val);
        final boolean result = instance.isSizeSorted();
        assertEquals(val, result);
    }

    /**
     * Test of setCurrentSignificance method, of class WordCloud.
     */
    @Test
    public void testSetCurrentSignificance() {
        System.out.println("setCurrentSignificance");
        final double val = 2.1;
        final WordCloud instance = new WordCloud(wordsToHashes, hashedWordSets, elementType, wordListWithSizes, wordSignificances, currentSignificance, queryInfoString, modCount, selectedWords, isUnionSelect, isSizeSorted);
        instance.setCurrentSignificance(val);
        final double result = instance.getCurrentSignificance();
        assertEquals(val, result);
    }

    /**
     * Test of getElementsCorrespondingToSelection method, of class WordCloud.
     */
    @Test
    public void testGetElementsCorrespondingToSelection() {
        System.out.println("getElementsCorrespondingToSelection");
        
        wordsToHashes.put("Query", 3);
        wordsToHashes.put("Query", 1);
        final Set integerSet = new HashSet<>();
        integerSet.add(0);
        integerSet.add(1);
        hashedWordSets.put(1, integerSet);
        hashedWordSets.put(2, integerSet);
        WordCloud instance = new WordCloud(wordsToHashes, hashedWordSets, elementType, wordListWithSizes, wordSignificances, currentSignificance, queryInfoString, modCount, selectedWords, isUnionSelect, isSizeSorted);
        instance.addWordToSelection("Query");
        Set result = instance.getElementsCorrespondingToSelection();
        // IsUnionSelect false
        assertEquals(result, integerSet);
        
        // IsUnionSelect true
        wordsToHashes.put("Vertex", 3);
        instance = new WordCloud(wordsToHashes, hashedWordSets, elementType, wordListWithSizes, wordSignificances, currentSignificance, queryInfoString, modCount, selectedWords, false, isSizeSorted);
        instance.addWordToSelection("Query");
        result = instance.getElementsCorrespondingToSelection();
        assertEquals(result, integerSet);
    }

    /**
     * Test of removeWordFromSelection method, of class WordCloud.
     */
    @Test
    public void testRemoveWordFromSelection() {
        System.out.println("removeWordFromSelection");
        final String word1 = "Query";
        final String word2 = "Vertex";
        final WordCloud instance = new WordCloud(wordsToHashes, hashedWordSets, elementType, wordListWithSizes, wordSignificances, currentSignificance, queryInfoString, modCount, selectedWords, isUnionSelect, isSizeSorted);
        instance.addWordToSelection(word1);
        instance.addWordToSelection(word2);
        final Set<String> words = instance.getSelectedWords();
        assertTrue(words.size() == 2);
        instance.removeWordFromSelection(word1);
        final Set<String> updatedWords = instance.getSelectedWords();
        assertTrue(updatedWords.size() == 1);
    }

    /**
     * Test of singleWordSelection method, of class WordCloud.
     */
    @Test
    public void testSingleWordSelection() {
        System.out.println("singleWordSelection");
        final String word1 = "Query";
        final String word2 = "Vertex";
        final String word3 = "New";
        final WordCloud instance = new WordCloud(wordsToHashes, hashedWordSets, elementType, wordListWithSizes, wordSignificances, currentSignificance, queryInfoString, modCount, selectedWords, isUnionSelect, isSizeSorted);
        instance.addWordToSelection(word1);
        instance.addWordToSelection(word2);
        final Set<String> words = instance.getSelectedWords();
        assertTrue(words.size() == 2);
        instance.singleWordSelection(word3);
        final Set<String> updatedWords = instance.getSelectedWords();
        assertTrue(updatedWords.size() == 1);
    }

    /**
     * Test of getElementType method, of class WordCloud.
     */
    @Test
    public void testGetElementType() {
        System.out.println("getElementType");
        final WordCloud instance = new WordCloud(wordsToHashes, hashedWordSets, elementType, wordListWithSizes, wordSignificances, currentSignificance, queryInfoString, modCount, selectedWords, isUnionSelect, isSizeSorted);
        final GraphElementType result = instance.getElementType();
        assertEquals(result, elementType);
    }

    /**
     * Test of getWordSignificances method, of class WordCloud.
     */
    @Test
    public void testGetWordSignificances() {
        System.out.println("getWordSignificances");
        final WordCloud instance = new WordCloud(wordsToHashes, hashedWordSets, elementType, wordListWithSizes, wordSignificances, currentSignificance, queryInfoString, modCount, selectedWords, isUnionSelect, isSizeSorted);
        final SortedMap result = instance.getWordSignificances();
        assertEquals(result, wordSignificances);
    }

    /**
     * Test of getWordListWithSizes method, of class WordCloud.
     */
    @Test
    public void testGetWordListWithSizes() {
        System.out.println("getWordListWithSizes");
        final WordCloud instance = new WordCloud(wordsToHashes, hashedWordSets, elementType, wordListWithSizes, wordSignificances, currentSignificance, queryInfoString, modCount, selectedWords, isUnionSelect, isSizeSorted);
        final SortedMap result = instance.getWordListWithSizes();
        assertEquals(result, wordListWithSizes);
    }
}
