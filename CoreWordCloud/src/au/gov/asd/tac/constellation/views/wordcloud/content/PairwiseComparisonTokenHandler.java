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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Delphinus8821
 */
public class PairwiseComparisonTokenHandler implements TokenHandler {

    final Map<String, Map[]> tokenElementFrequencies = new HashMap<>();
    final Map<Integer, Integer> elementCardinalities = new HashMap<>();

    final Set<Integer> elementsOfInterest;
    final int elementCapacity;

    private static final int MAX_CHUNK_SIZE = 2000;
    public final int totalChunks;
    public final int numChunks;

    public PairwiseComparisonTokenHandler(final int elementCapacity, final Set<Integer> elementsOfInterest) {
        this.elementsOfInterest = elementsOfInterest;
        this.elementCapacity = elementCapacity;
        numChunks = (int) Math.ceil((double) elementCapacity / MAX_CHUNK_SIZE);
        totalChunks = elementsOfInterest == null ? numChunks : numChunks * 2;
    }

    @Override
    public void registerToken(final String token, final int element) {
        // Update the number of tokens seen with the element in question 
        synchronized (elementCardinalities) {
            if (!elementCardinalities.containsKey(element)) {
                elementCardinalities.put(element, 0);
            } else {
                elementCardinalities.put(element, elementCardinalities.get(element) + 1);
            }
        }

        synchronized (tokenElementFrequencies) {
            Map[] frequencyMaps = tokenElementFrequencies.get(token);
            if (frequencyMaps == null) {
                frequencyMaps = new LinkedHashMap[totalChunks];
                for (int i = 0; i < totalChunks; i++) {
                    frequencyMaps[i] = new LinkedHashMap<>();
                }
                tokenElementFrequencies.put(token, frequencyMaps);
            }
            final int mapNum = elementsOfInterest != null && elementsOfInterest.contains(element) ? numChunks + (element / MAX_CHUNK_SIZE) : element / MAX_CHUNK_SIZE;
            final Map<Integer, Integer> frequencyMap = frequencyMaps[mapNum];
            final int existingValue = frequencyMap.containsKey(element) ? (int) frequencyMap.get(element) : 0;
            frequencyMap.put(element, existingValue + 1);
        }
    }
}
