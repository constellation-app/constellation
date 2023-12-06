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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

/**
 *
 * @author twilight_sparkle
 * @author Delphinus8821
 */
public class ContentPairwiseSimilarityServices {
    
    private static final String UNSUPPORTED = "This calculator does not compute modulii";

    public static class MutableDouble {

        private double val;

        public MutableDouble(final double val) {
            this.val = val;
        }
    }

    private PairwiseSimilarityCalculator similarityCalculator;
    private final PairwiseComparisonTokenHandler handler;
    private final List<ElementSimilarity> pairwiseSimilarities = Collections.synchronizedList(new LinkedList<>());
    private final Map<Integer, Double> modulii = new HashMap<>();

    private ContentPairwiseSimilarityServices(final PairwiseComparisonTokenHandler handler) {
        this.handler = handler;
        pairwiseSimilaritiesCalculation = new HashMap[handler.elementCapacity];
    }

    public static Map<Integer, Integer> clusterSimilarElements(final PairwiseComparisonTokenHandler handler, final NGramAnalysisParameters nGramParams) {
        return pairwiseComparison(handler, nGramParams).cluster();
    }

    public static List<ElementSimilarity> scoreSimilarPairs(final PairwiseComparisonTokenHandler handler, final NGramAnalysisParameters nGramParams) {
        return pairwiseComparison(handler, nGramParams).pairwiseSimilarities;
    }

    private static ContentPairwiseSimilarityServices pairwiseComparison(final PairwiseComparisonTokenHandler handler, final NGramAnalysisParameters nGramParams) {
        final ContentPairwiseSimilarityServices cpss = new ContentPairwiseSimilarityServices(handler);
        if (nGramParams.isBinarySpace()) {
            cpss.similarityCalculator = new BinarySpaceTaxicabNormCalculator();
        } else {
            cpss.similarityCalculator = new IntegerSpaceTaxicabNormCalculator();
        }
        cpss.computeSimilarities(nGramParams.getThreshold());
        return cpss;
    }

    private void computeSimilarities(final double threshold) {

        if (similarityCalculator.computeModulii()) {
            for (int i = 0; i < handler.totalChunks; i++) {
                final int currentChunk = i;
                handler.tokenElementFrequencies.values().parallelStream().forEach(value -> computeModulii(value[currentChunk]));
            }
        }

        for (int i = 0; i < handler.numChunks; i++) {
            int j;
            final int max;
            if (handler.elementsOfInterest == null) {
                j = i;
                max = handler.numChunks;
            } else {
                j = handler.numChunks;
                max = handler.totalChunks;
            }
            for (; j < max; j++) {
                final int chunk1 = i;
                final int chunk2 = j;
                handler.tokenElementFrequencies.values().parallelStream().forEach(value -> processPairsWithToken(value[chunk1], value[chunk2]));
                for (int k = 0; k < pairwiseSimilaritiesCalculation.length; k++) {
                    Map<Integer, MutableDouble> elementSimilarities = pairwiseSimilaritiesCalculation[k];
                    if (elementSimilarities != null) {
                        processPairwiseSimilarities(k, elementSimilarities, threshold);
                    }
                    pairwiseSimilaritiesCalculation[k] = null;
                }
            }
        }
    }

    private final Map[] pairwiseSimilaritiesCalculation;

    private void computeModulii(final Map<Integer, Integer> freqMap) {
        freqMap.entrySet().forEach(elementEntry -> {
            final int element = elementEntry.getKey();
            final int freq = elementEntry.getValue();
            synchronized (modulii) {
                if (!modulii.containsKey(elementEntry.getKey())) {
                    modulii.put(element, similarityCalculator.updateModulus(0.0, freq));
                } else {
                    modulii.put(element, similarityCalculator.updateModulus(modulii.get(element), freq));
                }
            }
        });
    }

    private void processPairsWithToken(final Map<Integer, Integer> freqMap1, final Map<Integer, Integer> freqMap2) {
        freqMap1.keySet().forEach(loElement -> {
            for (final int hiElement : freqMap2.keySet()) {
                if (loElement >= hiElement) {
                    continue;
                }

                synchronized (pairwiseSimilaritiesCalculation) {
                    if (pairwiseSimilaritiesCalculation[loElement] == null) {
                        pairwiseSimilaritiesCalculation[loElement] = new HashMap<>();
                    }

                    Map<Integer, MutableDouble> elementMap = pairwiseSimilaritiesCalculation[loElement];
                    if (!elementMap.containsKey(hiElement)) {
                        elementMap.put(hiElement, new MutableDouble(0.0));
                    } else {
                        elementMap.get(hiElement).val += (freqMap1.get(loElement) * freqMap2.get(hiElement));
                    }
                }
            }
        });
    }

    private void processPairwiseSimilarities(final int loElement, Map<Integer, MutableDouble> elementSimilarities, final double threshold) {
        final double loModulii = similarityCalculator.computeModulii() ? (double) modulii.get(loElement) : (double) handler.elementCardinalities.get(loElement);
        for (final Entry<Integer, MutableDouble> similarity : elementSimilarities.entrySet()) {
            final int hiElement = similarity.getKey();
            final double hiModulii = similarityCalculator.computeModulii() ? (double) modulii.get(hiElement) : (double) handler.elementCardinalities.get(hiElement);
            final double score = similarity.getValue().val / (Math.sqrt(loModulii * hiModulii));
            if (score >= threshold) {
                pairwiseSimilarities.add(new ElementSimilarity(loElement, hiElement, score));
            }
        }
    }

    public abstract static class PairwiseSimilarityCalculator {

        // Called whenever two elements share a token to progressively update their similarity score
        public abstract double updateSimilarity(final double currentSimilarity, final int element1Freq, final int element2Freq);

        // Called after all similarities and modulii have been fully computed in order to normalize the similarity scores based on the modulii of the elements
        public abstract double normalizeSimilarity(final double currentSimilarityy, final double element1Modulus, final double element2Modulus);

        // Whether or not this method of similarity calculation requires modulii to be computed (if modulii aren't computed, each element is taken to have modulus given by the number of token it contains. This corresponds to eh L1/Taxicab norm).
        public abstract boolean computeModulii();

        // Called whenever an element has a token to progressively update its modulus
        public abstract double updateModulus(final double currentModulus, final int element1Freq);

        // Called to finalize modulus calculations, usually doing something such as taking a square root (for the L2/euclidian norm)
        public abstract double finalizeModulus(final double currentModulus);
    }

    private static class IntegerSpaceEuclidianNormCalculator extends PairwiseSimilarityCalculator {

        @Override
        public double updateSimilarity(final double currentSimilarity, final int element1Freq, final int element2Freq) {
            return currentSimilarity + (element1Freq * element2Freq);
        }

        @Override
        public double normalizeSimilarity(final double currentSimilarity, final double element1Modulus, final double element2Modulus) {
            return currentSimilarity / (element1Modulus * element2Modulus);
        }

        @Override
        public boolean computeModulii() {
            return true;
        }

        @Override
        public double updateModulus(final double currentModulus, final int element1Freq) {
            return currentModulus + (element1Freq * element1Freq);
        }

        @Override
        public double finalizeModulus(final double modulus) {
            return Math.sqrt(modulus);
        }

    }

    private static class BinarySpaceEuclidianNormCalculator extends PairwiseSimilarityCalculator {

        @Override
        public double updateSimilarity(final double currentSimilarity, final int element1Freq, final int element2Freq) {
            return currentSimilarity + 1;
        }

        @Override
        public double normalizeSimilarity(final double currentSimilarity, final double element1Modulus, final double element2Modulus) {
            return currentSimilarity / (element1Modulus * element2Modulus);
        }

        @Override
        public boolean computeModulii() {
            return true;
        }

        @Override
        public double updateModulus(final double currentModulus, final int element1Freq) {
            return currentModulus + 1;
        }

        @Override
        public double finalizeModulus(final double modulus) {
            return Math.sqrt(modulus);
        }
    }

    private static class BinarySpaceTaxicabNormCalculator extends PairwiseSimilarityCalculator {

        @Override
        public double updateSimilarity(final double currentSimilarity, final int element1Freq, final int element2Freq) {
            return currentSimilarity + 1;
        }

        @Override
        public double normalizeSimilarity(final double currentSimilarity, final double element1Modulus, final double element2Modulus) {
            return currentSimilarity / (element1Modulus * element2Modulus);
        }

        @Override
        public boolean computeModulii() {
            return false;
        }

        @Override
        public double updateModulus(final double currentModulus, final int element1Freq) {
            throw new UnsupportedOperationException(UNSUPPORTED);
        }

        @Override
        public double finalizeModulus(final double modulus) {
            throw new UnsupportedOperationException(UNSUPPORTED);
        }

    }

    private static class IntegerSpaceTaxicabNormCalculator extends PairwiseSimilarityCalculator {

        @Override
        public double updateSimilarity(final double currentSimilarity, final int element1Freq, final int element2Freq) {
            return currentSimilarity + (element1Freq * element2Freq);
        }

        @Override
        public double normalizeSimilarity(final double currentSimilarity, final double element1Modulus, final double element2Modulus) {
            return currentSimilarity / (element1Modulus * element2Modulus);
        }

        @Override
        public boolean computeModulii() {
            return false;
        }

        @Override
        public double updateModulus(final double currentModulus, final int element1Freq) {
            throw new UnsupportedOperationException(UNSUPPORTED);
        }

        @Override
        public double finalizeModulus(final double modulus) {
            throw new UnsupportedOperationException(UNSUPPORTED);
        }
    }

    private Map<Integer, Integer> cluster() {

        final Map<Integer, Integer> elementToCluster = new HashMap<>();
        final Queue<Integer> freeClusters = new LinkedList<>();
        final Map<Integer, Set<Integer>> clusters = new HashMap<>();
        int currentClusterNum = 1;

        for (final ElementSimilarity pair : pairwiseSimilarities) {
            if (elementToCluster.containsKey(pair.low) && elementToCluster.containsKey(pair.high)) {
                final int loCluster = elementToCluster.get(pair.low);
                final int hiCluster = elementToCluster.get(pair.high);
                if (loCluster == hiCluster) {
                    continue;
                }
                final Set<Integer> clusterToMove = clusters.remove(hiCluster);
                clusters.get(loCluster).addAll(clusterToMove);
                for (final int el : clusterToMove) {
                    elementToCluster.put(el, loCluster);
                }
                elementToCluster.put(pair.high, loCluster);
                freeClusters.add(hiCluster);

            } else if (elementToCluster.containsKey(pair.low)) {
                final int cluster = elementToCluster.get(pair.low);
                elementToCluster.put(pair.high, cluster);
                clusters.get(cluster).add(pair.high);

            } else if (elementToCluster.containsKey(pair.high)) {
                final int cluster = elementToCluster.get(pair.high);
                elementToCluster.put(pair.low, cluster);
                clusters.get(cluster).add(pair.low);

            } else {
                final int cluster = !freeClusters.isEmpty() ? freeClusters.remove() : currentClusterNum++;
                elementToCluster.put(pair.low, cluster);
                elementToCluster.put(pair.high, cluster);
                final Set<Integer> newCluster = new HashSet<>();
                newCluster.add(pair.low);
                newCluster.add(pair.high);
                clusters.put(cluster, newCluster);
            }
        }

        return elementToCluster;
    }

}
