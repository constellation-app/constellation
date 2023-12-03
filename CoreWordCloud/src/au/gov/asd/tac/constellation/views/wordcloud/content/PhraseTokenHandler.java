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
package au.gov.asd.tac.constellation.views.wordcloud.content;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author twilight_sparkle
 */
public class PhraseTokenHandler implements TokenHandler {

    @SuppressWarnings("unchecked") // type of SparseMatrix is integer
    private final SparseMatrix<Integer> tokenElementMatrix = (TaggedSparseMatrix<Integer>) TaggedSparseMatrix.constructMatrix((Integer) 0);
    private final Map<String, Integer> tokenHashes = new HashMap<>();
    private final Map<Integer, Set<Integer>> constituentHashes = new HashMap<>();

    public SparseMatrix<Integer> getTokenElementMatrix() {
        return tokenElementMatrix;
    }

    public Map<String, Integer> getTokenHashes() {
        return tokenHashes;
    }

    public Map<Integer, Set<Integer>> getConstituentHashes() {
        return constituentHashes;
    }

    @Override
    public void registerToken(final String token, final int element) {
        registerToken(token, element, new HashSet<>(), false);
    }

    public void registerToken(final String token, final int element, final Set<String> singleWords, final boolean storeSingleWords) {
        // Hash the token and add it to the map 
        final int key = DefaultTokenHandler.oneAtATimeHash(token.toCharArray());
        synchronized (tokenHashes) {
            tokenHashes.put(token, key);
        }

        // Look up the current frequency related to the given hash and element, and increment it by one
        synchronized (tokenElementMatrix) {
            final int currentFreq = tokenElementMatrix.getCellPrimitive(key, element);
            tokenElementMatrix.putCell(key, element, currentFreq + 1);
            ((TaggedSparseMatrix) tokenElementMatrix).tagColumn(key, storeSingleWords);
        }
        synchronized (constituentHashes) {
            if (!constituentHashes.containsKey(key)) {
                final Set<Integer> hashes = new HashSet<>();
                for (final String word : singleWords) {
                    hashes.add(DefaultTokenHandler.oneAtATimeHash(word.toCharArray()));
                }
                constituentHashes.put(key, hashes);
            }
        }
    }
}
