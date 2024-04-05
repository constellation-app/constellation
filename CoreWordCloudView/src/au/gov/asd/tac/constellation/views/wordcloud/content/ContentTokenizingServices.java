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
package au.gov.asd.tac.constellation.views.wordcloud.content;

import static au.gov.asd.tac.constellation.views.wordcloud.content.ContentAnalysisOptions.TokenizingMethod.DELIMITED_NGRAMS;
import static au.gov.asd.tac.constellation.views.wordcloud.content.ContentAnalysisOptions.TokenizingMethod.NGRAMS;
import static au.gov.asd.tac.constellation.views.wordcloud.content.ContentAnalysisOptions.TokenizingMethod.NWORDS;
import au.gov.asd.tac.constellation.views.wordcloud.phraseanalysis.PhraseAnalysisModelLoader;
import au.gov.asd.tac.constellation.views.wordcloud.phraseanalysis.PhrasiphyContentParameters;
import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides services for tokenizing string attributes from graph elements using a
 * multi-threaded adaptor pattern. Tokenizing includes preprocessing of strings
 * (e.g. filtering characters and converting case), as well as splitting string
 * into 'tokens' based on various rules (e.g. delimiting characters).
 *
 * @author twilight_sparkle
 */
public class ContentTokenizingServices {

    private static final Logger LOGGER = Logger.getLogger(ContentTokenizingServices.class.getName());

    private ContentSanitizer sanitizer;
    private ContentTokenizer tokenizer;

    private ContentTokenizingServices() {
    }

    /**
     * Factory method to create a ContentTokenizingServices object which will
     * filter and tokenize strings in a manner suitable for the purposes of
     * n-gram analysis
     *
     * @param handler The object which handles the result of this n-gram based
     * tokenization.
     * @param nGramParams The parameters for n-gram analysis which will inform
     * the construction of this object.
     * @param allocator The object which connects to the data source to be
     * tokenized and delegates portions thereof to various threads.
     */
    public static void computeNGrams(final TokenHandler handler, final NGramAnalysisParameters nGramParams, final ThreadAllocator allocator) {
        final ContentTokenizingServices cts = new ContentTokenizingServices();
        final char[] nGramTrimCharactersWithAt = {'@', ','};
        final char[] nGramTrimCharacters = {','};
        final char[] trimCharacters = nGramParams.isRemoveDomain() ? nGramTrimCharactersWithAt : nGramTrimCharacters;
        cts.tokenizer = new NGramTokenizer(handler, nGramParams.getNGramLength());
        cts.sanitizer = new TrimmingSanitizer(trimCharacters).setInnerSanitizer(new CaseSanitizer(nGramParams.isCaseSensitive()));
        cts.createAndRunThreadsWithAdaptors(allocator);
    }

    /**
     * Factory method to create a ContentTokenizingServices object which will
     * filter and tokenize strings in a manner suitable for purposes of
     * clustering documents by text similarity.
     *
     * @param handler The object which handles the results of this tokenization.
     * @param clusterDocumentParams The parameters for doucment clustering which
     * will inform the construction of this object.
     * @param allocator The object which connects to the data source to be
     * tokenized and delegates portions thereof to various threads.
     */
    public static void createDocumentClusteringTokenizingService(final TokenHandler handler, final ClusterDocumentsParameters clusterDocumentsParams, final ThreadAllocator allocator) {
        final ContentTokenizingServices cts = new ContentTokenizingServices();
        switch (clusterDocumentsParams.getTokenizingMethod()) {
            case NWORDS -> 
                cts.tokenizer = new NWordTokenizer(handler, clusterDocumentsParams.getDelimiter().getChar(), clusterDocumentsParams.getTokenLength());
            case DELIMITED_NGRAMS -> 
                cts.tokenizer = new DelimitedNGramTokenizer(handler, clusterDocumentsParams.getTokenLength(), clusterDocumentsParams.getDelimiter().getChar());
            case NGRAMS -> 
                cts.tokenizer = new NGramTokenizer(handler, clusterDocumentsParams.getTokenLength());
        }
        cts.sanitizer = new FilteringSanitizer(clusterDocumentsParams.getDelimiter().getChar(), clusterDocumentsParams.getToFilterSet()).setInnerSanitizer(new CaseSanitizer(clusterDocumentsParams.isCaseSensitive()));
        cts.createAndRunThreadsWithAdaptors(allocator);
    }

    /**
     * Factory method to create a ContentTokenizingServices object which will
     * filter and tokenize strings in a manner suitable for the purposes of
     * phrasiphying in order to generate a word cloud.
     *
     * @param handler The object which handle the result of this tokenization.
     * @param phrasiphyContentParams The parameters for phrasiphying which will
     * inform the construction of this object.
     * @param allocator The object which connects to the data source to be
     * tokenized and delegates portions thereof to various threads.
     */
    public static void createPhraseAnalysisTokenizingService(final TokenHandler handler, final PhrasiphyContentParameters phrasiphyContentParams, final ThreadAllocator allocator) {
        final ContentTokenizingServices cts = new ContentTokenizingServices();
        final int phrase_length = phrasiphyContentParams.getPhraseLength();
        final int proximity = phrasiphyContentParams.getProximity();

        try {
            PhraseAnalysisModelLoader.loadMap();
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
        }

        // These four lines can now easily be converted into a parameter as desired rather than hard coded
        final String[] excludedWordSetNames = {"English Basic"};
        final String[] wordDelimiterSetNames = {"Word Delimiters"};
        final String[] phraseDelimiterSetNames = {"Phrase Delimiters"};

        final Set<String> excludedWords = new HashSet<>();
        for (final String setName : excludedWordSetNames) {
            excludedWords.addAll(PhraseAnalysisModelLoader.getExcludedWords().get(setName));
        }

        final Set<Character> wordDelimiters = new HashSet<>();
        for (final String setName : wordDelimiterSetNames) {
            final Set<String> delimiters = PhraseAnalysisModelLoader.getDelimiters().get(setName);
            delimiters.forEach(delimiter -> 
                wordDelimiters.add(delimiter.charAt(0)));
        }

        final Set<Character> phraseDelimiters = new HashSet<>();
        for (final String setName : phraseDelimiterSetNames) {
            final Set<String> delimiters = PhraseAnalysisModelLoader.getDelimiters().get(setName);
            delimiters.forEach(delimiter -> 
                phraseDelimiters.add(delimiter.charAt(0)));
        }
        
        final char phrase_delimiter = '.';
        final char word_delimiter = ' ';
        final Set<Character> apostrophes = new HashSet<>();
        apostrophes.add('\'');

        // Set the sanitizer to, in the following order, convert to lower case, filter word delimiters, filter apostropes without adding spaces, and insert phrase blocks for phrase delimiters
        cts.sanitizer = new PhraseDelimitingSanitizer(phrase_delimiter, phraseDelimiters).setInnerSanitizer(new FilteringSanitizer(apostrophes).setInnerSanitizer(new FilteringSanitizer(word_delimiter, wordDelimiters).setInnerSanitizer(new CaseSanitizer(false))));
        cts.tokenizer = new PhraseTokenizer(handler, ' ', phrase_length, '.', proximity, excludedWords);
        cts.createAndRunThreadsWithAdaptors(allocator);
    }

    /**
     * Runs the tokenizing process with the given work allocator.
     *
     * @param allocator An allocator which retrieves work sets from an adaptor
     * and assigns them to threads.
     */
    public void createAndRunThreadsWithAdaptors(final ThreadAllocator allocator) {
        while (allocator.hasMore()) {
            final ThreadedTokenization tokenization = new ThreadedTokenization(allocator);
            final Thread t = new Thread(tokenization);
            t.start();
        }
        allocator.waitOnOthers();
    }

    /**
     * Represents a single thread for tokenizing content in a work package,
     * managed by an allocator
     */
    private class ThreadedTokenization implements Runnable {

        private final ThreadAllocator allocator;
        private final ThreadedPhraseAdaptor phraseAdaptor;
        private final int threadID;

        /**
         * Constructs a ThreadedTokenization object connected to the given
         * allocator.
         */
        public ThreadedTokenization(final ThreadAllocator allocator) {
            this.allocator = allocator;
            // Get the adaptor interface to the next work package from the allocator.
            this.phraseAdaptor = allocator.nextAdaptor();
            this.threadID = allocator.getNumAllocated();
        }

        @Override
        public void run() {
            Thread.currentThread().setName("NGram.NGramServices.Thread.NGramComputation" + threadID);
            // Connect the adaptor to the data source 
            phraseAdaptor.connect();
            // tokenize the content
            extractTokensFromElements();
            // disconnect the adaptor from the data source 
            phraseAdaptor.disconnect();
            // wait on the completion of all the other threads allocated by this thread's allocator
            allocator.waitOnOthers();
        }

        /**
         * Runs the actual logic behind tokenizing the work package.
         */
        private void extractTokensFromElements() {
            // For each phrase in the work package 
            for (int i = 0; i < phraseAdaptor.getWorkload(); i++) {
                final String uncleanPhrase = phraseAdaptor.getNextPhrase();
                // Sanitize the attribute value and convert to a character array
                final char[] phrase = (uncleanPhrase == null) ? null : sanitizer.getSanitizedString(uncleanPhrase).toCharArray();
                // If the sanitized phrase is not null, tokenize the phrase
                if (phrase != null) {
                    tokenizer.tokenizePhrase(phrase, phraseAdaptor.getCurrentElementID());
                }
            }
        }
    }

    /**
     * Abstract base class for tokenizing content. This class contains the
     * interface for tokenizing phrases as well as the logic behind hashing
     * tokens and adding the frequency with which they were seen for given
     * elements to the parent ContentTokenizingServices object's matrix.
     */
    private abstract static class ContentTokenizer {

        protected final TokenHandler handler;

        public ContentTokenizer(final TokenHandler handler) {
            this.handler = handler;
        }

        /**
         * Tokenize a phrase from a given element and record the data
         */
        public void tokenizePhrase(final char[] phrase, final int elementPos) {
            final TokenizerState state = getNewState(phrase);
            while (state.findNextToken()) {
                handler.registerToken(new String(state.getToken()), elementPos);
            }
        }

        /**
         * Abstract base class for the internal state of a tokenizer working on
         * a given phrase. This is to be implemented for specific tokenizing
         * algorithms, and is required because some tokenizing algorithms may be
         * context sensitive.
         */
        protected abstract class TokenizerState {

            public final char[] phrase;

            /**
             * Construct a TokenizerState with a given phrase
             */
            protected TokenizerState(final char[] phrase) {
                this.phrase = phrase;
            }

            /**
             * Get the current token from this TokenizerState's phrase
             */
            protected abstract char[] getToken();

            /**
             * Find the next token from this TokenizerState's phrase, having
             * regard to other stative data if necessary. This method returns
             * whether or not it found another token, the actual token itself
             * must then be retrieved with getToken()
             *
             * @return true if there is another token, false otherwise
             */
            protected abstract boolean findNextToken();

        }

        /**
         * Factory method to construct a new TokenizerState objext in order to
         * process the given phrase
         */
        protected abstract TokenizerState getNewState(final char[] phrase);

    }

    /**
     * Tokenizer which tokenizes phrases into n-grams. Every token consists of a
     * sequence of n-consecutive characters.
     */
    private static class NGramTokenizer extends ContentTokenizer {

        protected final int nGramLength;

        /**
         * Constructs an NGramTokenizer
         *
         * @param nGramLength the number of characters in each token
         */
        public NGramTokenizer(final TokenHandler handler, final int nGramLength) {
            super(handler);
            this.nGramLength = nGramLength;
        }

        /**
         * TokenizerState for NGramTokenizer
         */
        public class NGramTokenizerState extends TokenizerState {

            // Stores the current position in the phrase being tokenized 
            private int pos = -1;
            
            /**
             * Construct an NGramTokenizer state for the specified phrase
             */
            public NGramTokenizerState(final char[] phrase) {
                super(phrase);
            }

            @Override
            protected char[] getToken() {
                // The current token is the sequence of n-characters from the current position in the phrase
                return Arrays.copyOfRange(phrase, pos, pos + nGramLength);
            }

            @Override
            protected boolean findNextToken() {
                // The next token is found by incrementing the position and checking that there are at least n characters left in the phrase 
                pos++;
                return (pos <= phrase.length - nGramLength);
            }

            public int getPos() {
                return pos;
            }

            public void setPos(final int pos) {
                this.pos = pos;
            }
        }

        @Override
        protected TokenizerState getNewState(final char[] phrase) {
            return new NGramTokenizerState(phrase);
        }
    }

    /**
     * Tokenizer which tokenizer phrases into n-grams, separated by a given
     * delimiter. Every token consists of a sequence of n-consecutive characters
     * not containing the delimiting character.
     */
    private static class DelimitedNGramTokenizer extends NGramTokenizer {

        private final char delimiter;
        
        public DelimitedNGramTokenizer(final TokenHandler handler, final int nGramLength, final char delimiter) {
            super(handler, nGramLength);
            this.delimiter = delimiter;
        }

        /**
         * Constructs a DelimitedNGramTokenizer
         *
         * @param nGramLength the number of characters in each token
         * @param delimiter the delimiter which tokens can not span public
         * DelimitedNGramTokenizer(final TokenHandler handler, final int
         * nGramLength, final char delimiter) { super(hanlder, nGramLength);
         * this.delimiter = delimiter; }
         *
         * /**
         * TokenizerState for DelimitedNGramTokenizer. Retrieving the token is
         * the same process as for NGramTokenizerState.
         */
        public class DelimitedNGramTokenizerState extends NGramTokenizerState {

            private boolean skip = true;

            /**
             * Construct a DelimitedNGramTokenizer state for the specified
             * phrase
             */
            public DelimitedNGramTokenizerState(final char[] phrase) {
                super(phrase);
            }

            @Override
            protected boolean findNextToken() {
                // Increment position, and check that there are at least n characters left, returning false if not.
                setPos(getPos() + 1);
                if (getPos() > phrase.length - nGramLength) {
                    return false;
                }

                // If we are not at the beginning and did not just skip over a delimiter, we only need to check that the last character in the current n-gram is not a delimiter.
                if (!skip) {
                    // If we find a delimiter, move pos to the delimiter and continue searching.
                    if (phrase[getPos() + nGramLength - 1] == delimiter) {
                        setPos(getPos() + (nGramLength - 1));
                        skip = true;
                        return findNextToken();
                    }
                    return true;
                }

                // If we are at the beginning or did just skip over a delimier, we need to check all n characters starting from pos.
                for (int i = 0; i < nGramLength; i++) {
                    // If we find a delimiter, move pos to the delimiter and continue searching.
                    if (phrase[getPos() + i] == delimiter) {
                        setPos(getPos() + i);
                        return findNextToken();
                    }
                }
                skip = false;
                return true;
            }
        }

        @Override
        protected TokenizerState getNewState(final char[] phrase) {
            return new DelimitedNGramTokenizerState(phrase);
        }
    }

    /**
     * Tokenizer which tokenizes phrases into n-words. Every token consists of a
     * sequence of n-consecutive words, where words are demarcated by a specific
     * delimiter.
     */
    private static class NWordTokenizer extends ContentTokenizer {

        protected final char delimiter;
        protected final int numOfWords;

        /**
         * Constructs an NWordTokenizer
         *
         * @param delimiter The delimiter which demarcates words
         * @param numOfWords the number of words in each token
         */
        public NWordTokenizer(final TokenHandler handler, final char delimiter, final int numOfWords) {
            super(handler);
            this.delimiter = delimiter;
            this.numOfWords = numOfWords;
        }

        /**
         * TokenizerState for NWordTokenizer. This TokenizerState finds
         * individual words and then uses these to build n-words.
         */
        public class NWordTokenizerState extends TokenizerState {

            private int wordEndPos = -1;
            private int wordStartPos;
            private int wordsLength = 0;
            private char[][] words;

            /**
             * Construct an NWordTokenizer state for the specified phrase
             */
            public NWordTokenizerState(final char[] phrase) {
                super(phrase);
                words = new char[numOfWords][];
            }

            public int getWordEndPos() {
                return wordEndPos;
            }

            public void setWordEndPos(final int wordEndPos) {
                this.wordEndPos = wordEndPos;
            }

            public int getWordStartPos() {
                return wordStartPos;
            }

            public void setWordStartPos(final int wordStartPos) {
                this.wordStartPos = wordStartPos;
            }

            public int getWordsLength() {
                return wordsLength;
            }

            public void setWordsLength(final int wordsLength) {
                this.wordsLength = wordsLength;
            }

            public char[][] getWords() {
                return words;
            }

            public void setWords(final char[][] words) {
                this.words = words;
            }

            protected void resetState() {
                wordEndPos = -1;
                wordsLength = 0;
            }

            /**
             * Finds the next single word in the phrase, returning false if it
             * couldn't find one.
             */
            protected boolean findNextWord() {
                // set the start position to be just after the end of the last word 
                wordStartPos = wordEndPos + 1;
                // increment the start position while it is at a delimiter
                while (wordStartPos < phrase.length && phrase[wordStartPos] == delimiter) {
                    wordStartPos++;
                }
                // set the end position to be one more than the start position
                wordEndPos = wordStartPos + 1;
                // increment the end position until it is a delimiter
                while (wordEndPos < phrase.length && phrase[wordEndPos] != delimiter) {
                    wordEndPos++;
                }
                // if the start position is before the end of the phrase, return true
                return (wordStartPos < phrase.length);
            }

            /**
             * Gets the current single word in the phrase.
             */
            protected char[] getWord() {
                return Arrays.copyOfRange(phrase, wordStartPos, wordEndPos);
            }

            @Override
            protected boolean findNextToken() {
                // If we are looking for the first token, populate the array with the first n words, returning false if there are not n words in the phrase.
                if (wordEndPos == -1) {
                    for (int i = 0; i < numOfWords; i++) {
                        if (!findNextWord()) {
                            return false;
                        }
                        words[i] = getWord();
                        wordsLength += words[i].length;
                    }
                    return true;
                    // Otherwise, find the next word, returning false if there is not one, then shift the array of words to the left by one, adding the new word to the end.
                } else {
                    if (!findNextWord()) {
                        return false;
                    }
                    wordsLength -= words[0].length;
                    System.arraycopy(words, 1, words, 0, numOfWords - 1);
                    words[numOfWords - 1] = getWord();
                    wordsLength += words[numOfWords - 1].length;
                    return true;
                }
            }

            @Override
            protected char[] getToken() {
                // The token has length equals to the length of the current n words in the state plus room for the delimiter between words.
                final char[] token = new char[wordsLength + numOfWords - 1];
                int currentPos = 0;
                // Copy each current word in the state into the token
                for (int i = 0; i < numOfWords; i++) {
                    System.arraycopy(words[i], 0, token, currentPos, words[i].length);
                    // Add the delimiter if we are not at the last word
                    if (i != numOfWords - 1) {
                        token[currentPos + words[i].length] = delimiter;
                        currentPos += (words[i].length + 1);
                    }
                }
                return token;
            }
        }

        @Override
        protected TokenizerState getNewState(final char[] phrase) {
            return new NWordTokenizerState(phrase);
        }
    }

    private static class NWordWithSingleCharacterWordsTokenizer extends NWordTokenizer {

        public NWordWithSingleCharacterWordsTokenizer(final TokenHandler handler, final char delimiter, final int numOfWords) {
            super(handler, delimiter, numOfWords);
        }

        @Override
        protected TokenizerState getNewState(final char[] phrase) {
            return new NWordWithSingleCharacterWordsTokenizerState(phrase);
        }

        public class NWordWithSingleCharacterWordsTokenizerState extends NWordTokenizerState {

            protected boolean singleCharacterWord = false;

            public NWordWithSingleCharacterWordsTokenizerState(final char[] phrase) {
                super(phrase);
            }

            @Override
            protected boolean findNextWord() {
                // set the start position to be just after the end of the last word.
                setWordStartPos(singleCharacterWord ? getWordEndPos() : getWordEndPos() + 1);
                // increment the start position while it is at a delimiter 
                while (getWordStartPos() < phrase.length && phrase[getWordStartPos()] == delimiter) {
                    setWordStartPos(getWordStartPos() + 1);
                }
                // set the end position to be one more than the start position
                setWordEndPos(getWordStartPos());
                // increment the end position until it is a delimiter or a single character word
                singleCharacterWord = false;
                while (getWordEndPos() < phrase.length && phrase[getWordEndPos()] != delimiter) {
                    setWordEndPos(getWordEndPos() + 1);
                }
                // if the start position is before the end of the phrase, return true
                return getWordStartPos() < phrase.length;
            }
        }
    }

    /**
     * Tokenizer which tokenizes content into phrases. Phrases are collections
     * of a given number of words within a certain proximity and not spanning a
     * special 'phrase delimiter' characters.
     */
    private static class PhraseTokenizer extends NWordWithSingleCharacterWordsTokenizer {

        private final int proximity;
        private final char phraseDelimiter;
        private final Set<String> excludedWords;

        /**
         * Construct a PhraseTokenizer
         *
         * @param delimiter the delimiter to separate words
         * @param numOfWords the number of words in each token
         * @param phraseDelimiter the delimiter which phrases may not span
         * @param proximity the maximum number of words which may separate in
         * content the first and last words in a phrase. Must be greater than or
         * equal to the numOfWords - 1.
         * @param excludedWords a list of words which are to be ignored.
         */
        public PhraseTokenizer(final TokenHandler handler, final char delimiter, final int numOfWords, final char phraseDelimiter, final int proximity, final Set<String> excludedWords) {
            super(handler, delimiter, numOfWords);
            this.phraseDelimiter = phraseDelimiter;
            this.proximity = proximity;
            this.excludedWords = new HashSet<>(excludedWords);
        }

        @Override
        protected TokenizerState getNewState(final char[] phrase) {
            return new PhraseTokenizerState(phrase);
        }

        /**
         * Tokenize a phrase from a given element and record the data.
         */
        @Override
        public void tokenizePhrase(final char[] phrase, final int elementPos) {
            final TokenizerState state = getNewState(phrase);
            while (state.findNextToken()) {
                if (handler instanceof PhraseTokenHandler) {
                    ((PhraseTokenHandler) handler).registerToken(new String(state.getToken()), elementPos, ((PhraseTokenizerState) state).currentSingleWords, ((PhraseTokenizerState) state).storeSingleWords);
                } else {
                    handler.registerToken(new String(phrase), elementPos);
                }
            }
        }

        public class PhraseTokenizerState extends NWordWithSingleCharacterWordsTokenizerState {

            private char[] leadWord;
            private int[] skips;
            private boolean[] delimitersBeforeWord;
            private boolean newPhraseBlock = true;
            private int currentWord = 0;
            private int currentSpan = 0;
            private PermutationGenerationState pgs = null;
            private Set<String> currentSingleWords;
            protected boolean storeSingleWords;

            /**
             * Construct a PhraseTokenizer state for the specified phrase
             */
            public PhraseTokenizerState(final char[] phrase) {
                super(phrase);
                setWords(new char[proximity - 1][]);
                delimitersBeforeWord = new boolean[proximity - 1];
                skips = new int[proximity - 1];
                storeSingleWords = (numOfWords != 1);
            }

            @Override
            protected void resetState() {
                super.resetState();
                setWords(new char[proximity - 1][]);
                delimitersBeforeWord = new boolean[proximity - 1];
                skips = new int[proximity - 1];
                currentWord = 0;
                currentSpan = 0;
                newPhraseBlock = true;
                pgs = null;
                storeSingleWords = false;
            }

            @Override
            protected boolean findNextToken() {
                // If we are generating permutations from the current span, try to generate the next permutation and return true if successful. Otherwise stop generating and find more words.
                if (pgs != null) {
                    pgs.generateNextPermutation();
                    if (pgs.permutation != null) {
                        return true;
                    } else {
                        pgs = null;
                    }
                }

                // If we are starting a new phrase block (whole new set of proximal words), find the lead word for this phrase, or return false if there are no more words.
                if (newPhraseBlock) {
                    currentWord = 0;
                    currentSpan = 0;
                    do {
                        if (!findNextWord()) {
                            if (storeSingleWords) {
                                resetState();
                                return findNextToken();
                            }
                            return false;
                        }
                        leadWord = getWord();
                    } while (excludedWords.contains(new String(leadWord)) || (leadWord.length == 1 && leadWord[0] == phraseDelimiter));
                    if (numOfWords == 1 || storeSingleWords) {
                        return true;
                    }
                    Arrays.fill(skips, 0);
                    newPhraseBlock = false;
                } else {
                    // Otherwise, shift the words and decrement the current span
                    leadWord = getWords()[0];
                    currentSpan -= skips[0] + 1;
                    final char[][] newWords = getWords();
                    newWords[0] = null;
                    setWords(newWords);
                    delimitersBeforeWord[0] = false;
                    skips[0] = 0;
                    System.arraycopy(getWords(), 1, getWords(), 0, currentWord - 1);
                    System.arraycopy(delimitersBeforeWord, 1, delimitersBeforeWord, 0, currentWord - 1);
                    System.arraycopy(skips, 1, skips, 0, skips.length - 1);
                    currentWord--;
                }

                // Find any extra words if the current span is less than the proximity
                for (int i = currentSpan; i < proximity - 1; i++) {
                    currentSpan++;
                    if (currentWord == proximity - 1) {
                        break;
                    }
                    if (!findNextWord()) {
                        break;
                    }
                    char[] word = getWord();
                    if (excludedWords.contains(new String(word))) {
                        skips[currentWord]++;
                    } else if (word.length == 1 && word[0] == phraseDelimiter) {
                        newPhraseBlock = true;
                        break;
                    } else {
                        delimitersBeforeWord[currentWord] = !singleCharacterWord;
                        getWords()[currentWord++] = word;
                    }
                }

                // If there are no words in the current span, enforce a new phrase block
                if (currentWord == 0) {
                    newPhraseBlock = true;
                }

                // If we have enough non-excluded words in the current span, start generating permutations from them.
                if (currentWord >= numOfWords - 1) {
                    pgs = new PermutationGenerationState(currentWord, numOfWords - 1, true);
                }
                // Upon reaching this point we either have a new phrase or a new permutation generator. Calling the function recursively will dive into these.
                return findNextToken();
            }

            @Override
            protected char[] getToken() {
                currentSingleWords = new HashSet<>();
                currentSingleWords.add(new String(leadWord));

                // Special case for length 1 
                if (numOfWords == 1 || storeSingleWords) {
                    return leadWord;
                }

                // Find out the length of the phrase
                final char[] token;
                int length = leadWord.length;
                for (int i = 0; i < pgs.permutation.length; i++) {
                    length += getWords()[pgs.permutation[i]].length + (delimitersBeforeWord[pgs.permutation[i]] ? 1 : 0);
                }

                token = new char[length];

                // Build the phrase from the lead word and the proximal words specified by the current permutation
                System.arraycopy(leadWord, 0, token, 0, leadWord.length);
                int currentPos = leadWord.length;
                for (int i = 0; i < pgs.permutation.length; i++) {
                    if (delimitersBeforeWord[pgs.permutation[i]]) {
                        token[currentPos++] = delimiter;
                    }
                    final char[] word = getWords()[pgs.permutation[i]];
                    currentSingleWords.add(new String(word));
                    System.arraycopy(word, 0, token, currentPos, word.length);
                    currentPos += word.length;
                }
                return token;
            }

            private class PermutationGenerationState {

                private int n;
                private int k;
                private boolean ordered;
                private BitSet availableObjects;
                private int currentIndex = 0;
                private int[] permutation;

                public PermutationGenerationState(final int n, final int k, final boolean ordered) {
                    this.n = n;
                    availableObjects = new BitSet(n);
                    availableObjects.set(0, n);
                    this.k = k;
                    permutation = new int[k];
                    permutation[0] = 0;
                    this.ordered = ordered;
                }

                public void generateNextPermutation() {
                    // move back until a position in the permutation is found which can take a higher indexed object 
                    // return null if we have generated all permutations
                    while (availableObjects.nextSetBit(permutation[currentIndex]) == -1 || (ordered && n - availableObjects.nextSetBit(permutation[currentIndex]) < k - currentIndex)) {
                        availableObjects.set(permutation[currentIndex]);
                        currentIndex--;
                        if (currentIndex == -1) {
                            permutation = null;
                            return;
                        }
                    }

                    // put back the candidate object and in its place take the next highest 
                    final int putBack = permutation[currentIndex];
                    permutation[currentIndex] = availableObjects.nextSetBit(putBack);
                    availableObjects.set(putBack);
                    availableObjects.clear(permutation[currentIndex]);
                    currentIndex++;
                    // fill up the rest of the available positions
                    while (currentIndex < k) {
                        final int nextAvailable = availableObjects.nextSetBit(0);
                        permutation[currentIndex++] = nextAvailable;
                        availableObjects.clear(nextAvailable);
                    }
                    currentIndex--;
                }
            }
        }
    }

    private abstract static class ContentSanitizer {

        private ContentSanitizer innerSanitizer = null;

        public ContentSanitizer setInnerSanitizer(final ContentSanitizer nextSanitizer) {
            this.innerSanitizer = nextSanitizer;
            return this;
        }

        public String getSanitizedString(final String str) {
            if (innerSanitizer == null) {
                return sanitizeString(str);
            } else {
                return sanitizeString(innerSanitizer.getSanitizedString(str));
            }
        }

        protected abstract String sanitizeString(final String str);
    }

    private static class CaseSanitizer extends ContentSanitizer {

        private final boolean caseSensitive;

        public CaseSanitizer(final boolean caseSensitive) {
            this.caseSensitive = caseSensitive;
        }

        @Override
        protected String sanitizeString(final String str) {
            return !caseSensitive ? str.toLowerCase(): "";
        }
    }

    private static class TrimmingSanitizer extends ContentSanitizer {

        private final char[] toTrim;

        public TrimmingSanitizer(final char[] toTrim) {
            this.toTrim = toTrim.clone();
        }

        @Override
        protected String sanitizeString(final String str) {
            // Find the lowest trim point based on all trimming characters
            int trimPoint = str.length();
            for (int i = 0; i < toTrim.length; i++) {
                final int index = str.indexOf(toTrim[i]);
                if (index != -1 && index < trimPoint) {
                    trimPoint = index;
                }
            }
            return str.substring(0, trimPoint);
        }
    }

    private static class FilteringSanitizer extends ContentSanitizer {

        private final Set<Character> toFilter;
        private final String replacement;

        public FilteringSanitizer(final Set<Character> toFilter) {
            this("", toFilter);
        }

        public FilteringSanitizer(final char delimiter, final Set<Character> toFilter) {
            this(String.valueOf(delimiter), toFilter);
        }

        public FilteringSanitizer(final String replacement, final Set<Character> toFilter) {
            this.replacement = replacement;
            this.toFilter = new HashSet<>(toFilter);
        }

        @Override
        protected String sanitizeString(final String str) {
            String newStr = str;
            for (int i = 0; i < str.length(); i++) {
                if (toFilter.contains(str.charAt(i)) && replacement.length() > 0) {
                    newStr = str.substring(0, i) + replacement + str.substring(i + 1, str.length());
                    i += (replacement.length() - 1);
                }
            }
            return newStr;
        }
    }

    private static class PhraseDelimitingSanitizer extends FilteringSanitizer {

        public PhraseDelimitingSanitizer(final char phraseDelimiter, final Set<Character> toDelimitPhrase) {
            super(" " + phraseDelimiter + " ", toDelimitPhrase);
        }
    }
}
