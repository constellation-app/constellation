/*
 * Copyright 2010-2021 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.algorithms.sna.similarity;

import java.util.Arrays;

/**
 *
 * @author canis_majoris
 */
public class LevenshteinDistanceFunction implements DistanceFunction<String> {

    private final int k;

    protected LevenshteinDistanceFunction(final int k) {
        this.k = k;
    }

    @Override
    public double getDistance(final String firstPoint, final String secondPoint) {
        if (firstPoint == null || secondPoint == null) {
            throw new IllegalArgumentException("Strings must not be null.");
        }
        if (k < 0) {
            throw new IllegalArgumentException("Maximum distance cannot be negative.");
        }

        CharSequence left = firstPoint;
        CharSequence right = secondPoint;

        int n = left.length();
        int m = right.length();

        if (n == 0) {
            return m <= k ? m : k + 1;
        } else if (m == 0) {
            return n <= k ? n : k + 1;
        }

        if (n > m) {
            final CharSequence temp = left;
            left = right;
            right = temp;
            n = m;
            m = right.length();
        }

        int[] p = new int[n + 1];
        int[] d = new int[n + 1];
        int[] tempD;

        final int boundary = Math.min(n, k) + 1;
        for (int i = 0; i < boundary; i++) {
            p[i] = i;
        }

        Arrays.fill(p, boundary, p.length, Integer.MAX_VALUE);
        Arrays.fill(d, Integer.MAX_VALUE);

        for (int j = 1; j <= m; j++) {
            final char rightJ = right.charAt(j - 1);
            d[0] = j;

            final int min = Math.max(1, j - k);
            final int max = j > Integer.MAX_VALUE - k ? n : Math.min(n, j + k);

            if (min > max) {
                return k + 1.0;
            }

            if (min > 1) {
                d[min - 1] = Integer.MAX_VALUE;
            }

            for (int i = min; i <= max; i++) {
                if (left.charAt(i - 1) == rightJ) {
                    d[i] = p[i - 1];
                } else {
                    d[i] = 1 + Math.min(Math.min(d[i - 1], p[i]), p[i - 1]);
                }
            }

            tempD = p;
            p = d;
            d = tempD;
        }

        if (p[n] <= k) {
            return p[n];
        }
        return k + 1.0;
    }

}
