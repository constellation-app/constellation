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

/**
 *
 * @author twilight_sparkle
 */
public class DistanceCalculation {

    public abstract class ElementSimilarityCalculator {

        public abstract int getSimilarity(final int currentSimilarity, final int element1Freq, final int element2Freq);

        public abstract int getModulus(final int currentModulus, final int element1Freq);

    }

    public class IntegerSpaceDotProductCalculator extends ElementSimilarityCalculator {

        @Override
        public int getSimilarity(final int currentSimilarity, final int element1Freq, final int element2Freq) {
            return currentSimilarity + (element1Freq * element2Freq);
        }

        @Override
        public int getModulus(final int currentModulus, final int element1Freq) {
            return currentModulus + (element1Freq * element1Freq);
        }
    }

    public class BinarySpaceDotProductCalculator extends ElementSimilarityCalculator {

        @Override
        public int getSimilarity(final int currentSimilarity, final int element1Freq, final int element2Freq) {
            return currentSimilarity + 1;
        }

        @Override
        public int getModulus(final int currentModulus, final int element1Freq) {
            return currentModulus + 1;
        }
    }

    public abstract class SimilarityAdjustmentCalculator {

        private final int threshold;

        public SimilarityAdjustmentCalculator(final int threshold) {
            this.threshold = threshold;
        }

        protected abstract int getAdjustedSimilarity(final int similarity, final int modulus1, final int modulus2);

        public boolean isAboveThreshold(final int similarity, final int modulus1, final int modulus2) {
            return (getAdjustedSimilarity(similarity, modulus1, modulus2) >= threshold);
        }
    }

    public class CosineSimilarityCalculator extends SimilarityAdjustmentCalculator {

        public CosineSimilarityCalculator(final int threshold) {
            super(threshold);
        }

        @Override
        public int getAdjustedSimilarity(final int similarity, final int modulus1, final int modulus2) {
            return Math.round(similarity * 100 / (float) Math.sqrt(modulus1 * modulus2));
        }
    }
}
