/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.util;

/**
 *
 * @author algol
 */
public class InfoMath {

    public static final double M_LOG2E = 1.4426950408889634074;

    public static double log2(final double p) {
        return Math.log(p) * M_LOG2E;
    }

    public static double plogp(final double p) {
        return p > 0 ? p * log2(p) : 0;
    }

    /**
     * Get a random permutation of indices of the size of the input vector.
     *
     * @param randomOrder the array that will be filled with randomly permuted
     * integers.
     * @param randGen the random number generator.
     */
    public static void getRandomizedIndexVector(final int[] randomOrder, final Lcg randGen) {
        final int size = randomOrder.length;
        for (int i = 0; i < size; ++i) {
            randomOrder[i] = i;
        }

        for (int i = 0; i < size; ++i) {
            final int i2 = i + randGen.nextInt(size - i - 1);
            final int t = randomOrder[i];
            randomOrder[i] = randomOrder[i2];
            randomOrder[i2] = t;
        }
    }
}
