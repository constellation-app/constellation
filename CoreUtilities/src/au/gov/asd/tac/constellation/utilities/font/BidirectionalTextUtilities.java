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
package au.gov.asd.tac.constellation.utilities.font;

import java.text.Bidi;

/**
 * Rearrange bidirectional text using the library Bidi class.
 * <p>
 * Note: this is not sufficient for displaying interesting text such as Arabic,
 * because it requires more complex layout than just rearranging characters. The
 * layout should be done using {@link java.awt.font.TextLayout}.
 *
 * @author algol
 */
public class BidirectionalTextUtilities {
    
    private BidirectionalTextUtilities() {
        throw new IllegalStateException("Utility class");
    }

    public static String doBidi(final String s) {
        if (s == null) {
            return s;
        }

        final Bidi bidi = new Bidi(s, Bidi.DIRECTION_LEFT_TO_RIGHT);
        if (bidi.isLeftToRight()) {
            return s;
        }

        final int runCount = bidi.getRunCount();
        final byte[] levels = new byte[runCount];
        final Integer[] ranges = new Integer[runCount];
        for (int index = 0; index < runCount; index++) {
            levels[index] = (byte) bidi.getRunLevel(index);
            ranges[index] = index;
        }

        Bidi.reorderVisually(levels, 0, ranges, 0, runCount);

        final StringBuilder result = new StringBuilder();
        for (int i = 0; i < runCount; i++) {
            final int index = ranges[i];
            final int start = bidi.getRunStart(index);
            int end = bidi.getRunLimit(index);
            final int level = levels[index];
            if ((level & 1) != 0) {
                for (; --end >= start;) {
                    result.append(s.charAt(end));
                }
            } else {
                result.append(s, start, end);
            }
        }

        return result.toString();
    }
}
