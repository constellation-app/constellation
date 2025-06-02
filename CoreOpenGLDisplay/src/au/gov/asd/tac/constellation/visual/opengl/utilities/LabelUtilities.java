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
package au.gov.asd.tac.constellation.visual.opengl.utilities;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.util.ArrayList;

/**
 * Utility methods and constants relating to the rendering of labels in the
 * {@link GLVisualProcessor}.
 *
 * @author algol
 */
public final class LabelUtilities {

    public static final String MIX_COLOR_ATTRIBUTE_NAME = "mix_color";
    public static final ConstellationColor DEFAULT_COLOR = ConstellationColor.CLOUDS;

    // Line splitting.
    public static final int MAX_LINE_LENGTH_PER_ATTRIBUTE = 50;
    public static final int MAX_LINES_PER_ATTRIBUTE = 4;
    public static final int MAX_LABELS_TO_DRAW = 4;
    public static final int MAX_LABEL_SIZE = 4;
    public static final int MAX_TRANSACTION_WIDTH = 16;
    public static final int MAX_TRANSACTIONS_PER_LINK_DEFAULT = 8;
    // In the user interface, the size of labels are specified as a proportion of the size of the node.
    // This factor is used to convert these sizes into 'label units' which are an integer from 0 to 64.
    public static final int NRADIUS_TO_LABEL_UNITS = 16;
    public static final int NRADIUS_TO_LINE_WIDTH_UNITS = 16;
    public static final String ELLIPSIS = "…"; // \u2026
    
    private LabelUtilities() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Split the attribute values into lines of text that are no more than
     * MAX_LINE_LENGTH_PER_ATTRIBUTE chars long.
     * <p>
     * String will be split on spaces only, unless there are more than
     * MAX_LINE_LENGTH_PER_ATTRIBUTE non-space characters in a row. Consecutive
     * spaces will be kept unless a split happens within the run of spaces, in
     * which case the spaces around the split will be discarded.
     * <p>
     * A maximum of MAX_LINES_PER_ATTRIBUTE lines will be generated.
     *
     * @param text A String to be split into lines.
     *
     * @return array of strings
     */
    public static ArrayList<String> splitTextIntoLines(final String text) {
        final ArrayList<String> lines = new ArrayList<>();

        if (text != null) {

            String remaining = text.trim();
            int prevSpace = Integer.MIN_VALUE;
            while (!remaining.isEmpty() && lines.size() < MAX_LINES_PER_ATTRIBUTE) {

                final int space = remaining.indexOf(' ', prevSpace);
                final int newLine = remaining.indexOf('\n');
                final int pos;
                if (space == -1) {
                    pos = newLine;
                } else {
                    pos = newLine == -1 ? space : Math.min(space, newLine);
                }
                final boolean isNewLine = pos == newLine;

                if (pos > MAX_LINE_LENGTH_PER_ATTRIBUTE || pos == -1) {
                    int trimPoint;
                    if (remaining.length() <= MAX_LINE_LENGTH_PER_ATTRIBUTE) {
                        trimPoint = remaining.length();
                    } else if (prevSpace == Integer.MIN_VALUE) {
                        trimPoint = MAX_LINE_LENGTH_PER_ATTRIBUTE;
                    } else {
                        trimPoint = Math.min(prevSpace, MAX_LINE_LENGTH_PER_ATTRIBUTE);
                    }
                    lines.add(remaining.substring(0, trimPoint).trim());
                    remaining = remaining.substring(trimPoint).trim();
                    prevSpace = Integer.MIN_VALUE;
                } else if (isNewLine) {
                    lines.add(remaining.substring(0, pos).trim());
                    remaining = remaining.substring(pos).trim();
                    prevSpace = Integer.MIN_VALUE;
                } else {
                    prevSpace = pos + 1;
                }
            }

            // If the last line is too long, substitute the final character with an ellipsis.
            if (!remaining.isEmpty()) {
                String t = lines.get(MAX_LINES_PER_ATTRIBUTE - 1);
                t = t.substring(0, Math.min(t.length(), MAX_LINE_LENGTH_PER_ATTRIBUTE - 1)) + ELLIPSIS;
                lines.set(MAX_LINES_PER_ATTRIBUTE - 1, t);
            }
        }

        return lines;
    }
}
