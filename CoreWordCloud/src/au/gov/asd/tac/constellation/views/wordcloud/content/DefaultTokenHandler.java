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
import java.util.Map;

/**
 *
 * @author twilight_sparkle
 */
public class DefaultTokenHandler implements TokenHandler {

    @SuppressWarnings("unchecked") // type of SparseMatrix will be integer
    private final SparseMatrix<Integer> tokenElementMatrix = (SparseMatrix<Integer>) SparseMatrix.constructMatrix((Integer) 0);
    private final Map<String, Integer> tokenHashes = new HashMap<>();

    public SparseMatrix<Integer> getTokenElementMatrix() {
        return tokenElementMatrix;
    }

    /*
	 * Hash function for n-grams by Bob Jenkins
     */
    static int oneAtATimeHash(final char[] token) {
        int hash = 0;

        for (int i = 0; i < token.length; i++) {
            hash += token[i];
            hash += (hash << 10);
            hash ^= (hash >> 6);
        }

        // final rounds of bit folding to ensure that the last bits of input affect all output bits
        hash += (hash << 3);
        hash ^= (hash >> 11);
        hash += (hash << 15);

        return hash;
    }

    @Override
    public void registerToken(final String token, final int element) {
        // Hash the token and add it to the map
        final int key = oneAtATimeHash(token.toCharArray());
        synchronized (tokenHashes) {
            tokenHashes.put(token, key);
        }

        // Look up the current frequency related to the given hash and element, and increment it by one
        synchronized (tokenElementMatrix) {
            final int currentFreq = tokenElementMatrix.getCellPrimitive(key, element);
            tokenElementMatrix.putCell(key, element, currentFreq + 1);
        }
    }
}
