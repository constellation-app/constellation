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

import java.util.ArrayList;
import java.util.List;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 *
 * @author algol
 */
public class LabelUtilitiesNGTest {

    @Test
    public void ellipsisIsSingleChar() {
        assertEquals(LabelUtilities.ELLIPSIS.length(), 1);
    }

    @Test
    public void splitNull() {
        final List<String> out = LabelUtilities.splitTextIntoLines(null);
        assertEquals(out.size(), 0);
    }

    @Test
    public void split0() {
        final String text = "";
        final List<String> out = LabelUtilities.splitTextIntoLines(text);
        assertEquals(out.size(), 0);
    }

    @Test
    public void split1() {
        final String text = "This is a test.";
        final List<String> out = LabelUtilities.splitTextIntoLines(text);
        assertEquals(out.size(), 1);
        assertEquals(out.get(0), text);
    }

    /**
     * Check that a line won't be split within a word.
     */
    @Test
    public void splitWord() {
        final String text0 = ofLength(LabelUtilities.MAX_LINE_LENGTH_PER_ATTRIBUTE - 5);

        String word = "";

        String sp = "";
        for (int i = 0; i < 10; i++) {
            final String text = text0 + sp + word;
            final List<String> out = LabelUtilities.splitTextIntoLines(text);
            if (text.length() <= LabelUtilities.MAX_LINE_LENGTH_PER_ATTRIBUTE) {
                assertEquals(out.size(), 1);
                assertEquals(out.get(0), text);
            } else {
                assertEquals(out.size(), 2);
                assertEquals(out.get(0), text0);
                assertEquals(out.get(1), word.trim());
            }

            word += (char) ('a' + i);
            sp = " ";
        }
    }

    /**
     * A split on a space will discard the space.
     */
    @Test
    public void splitSpaceBefore() {
        final String text0 = ofLength(LabelUtilities.MAX_LINE_LENGTH_PER_ATTRIBUTE - 1);

        final String extra = "extra";
        final String text = text0 + " " + extra;

        final List<String> out = LabelUtilities.splitTextIntoLines(text);
        assertEquals(out.size(), 2);
        assertEquals(out.get(0), text0);
        assertEquals(out.get(1), extra);
    }

    /**
     * A split on a space will discard the space.
     */
    @Test
    public void splitSpaceAfter() {
        final String text0 = ofLength(LabelUtilities.MAX_LINE_LENGTH_PER_ATTRIBUTE);

        final String extra = "extra";
        final String text = text0 + " " + extra;

        final List<String> out = LabelUtilities.splitTextIntoLines(text);
        assertEquals(out.size(), 2);
        assertEquals(out.get(0), text0);
        assertEquals(out.get(1), extra);
    }

    /**
     * A split on consecutive spaces will discard the spaces at the split.
     */
    @Test
    public void splitSpaces1() {
        final String text0 = ofLength(LabelUtilities.MAX_LINE_LENGTH_PER_ATTRIBUTE - 5);
        final String text1 = "extra";
        final String text = text0 + ofLength(10, ' ') + text1;

        final List<String> out = LabelUtilities.splitTextIntoLines(text);
        assertEquals(out.size(), 2);
        assertEquals(out.get(0), text0);
        assertEquals(out.get(1), text1);
    }

    @Test
    public void splitSpaces2() {
        String text = "";
        for (int i = 0; i < LabelUtilities.MAX_LINES_PER_ATTRIBUTE; i++) {
            text += (char) ('A' + i) + ofLength(LabelUtilities.MAX_LINE_LENGTH_PER_ATTRIBUTE - 1, ' ');
        }

        final List<String> out = LabelUtilities.splitTextIntoLines(text);
        assertEquals(out.size(), LabelUtilities.MAX_LINES_PER_ATTRIBUTE);
        for (int i = 0; i < out.size(); i++) {
            assertEquals(out.get(i), String.format("%c", (char) ('A' + i)));
        }
    }

    /**
     * Check that multiple spaces elsewhere aren't discarded.
     */
    @Test
    public void splitNotOnMultipleSpaces() {
        final String text0 = "A" + ofLength(LabelUtilities.MAX_LINE_LENGTH_PER_ATTRIBUTE - 3, ' ') + "Z";
        final String text1 = "extra";
        final String text = text0 + "   " + text1;
        final List<String> out = LabelUtilities.splitTextIntoLines(text);
        assertEquals(out.size(), 2);
        assertEquals(out.get(0), text0);
        assertEquals(out.get(1), text1);
    }

    /**
     * Add the ellipsis character at the end if the final line is too long.
     */
    @Test
    public void splitTooLong() {
        String text = "";
        for (int i = 0; i < LabelUtilities.MAX_LINES_PER_ATTRIBUTE; i++) {
            text += ofLength(LabelUtilities.MAX_LINE_LENGTH_PER_ATTRIBUTE - 1) + " ";
        }

        text += "xyzzy";

        final List<String> out = LabelUtilities.splitTextIntoLines(text);
        assertEquals(out.size(), LabelUtilities.MAX_LINES_PER_ATTRIBUTE);
        assertTrue(out.get(LabelUtilities.MAX_LINES_PER_ATTRIBUTE - 1).endsWith(LabelUtilities.ELLIPSIS));
    }

    /**
     * A single long word with no spaces should be chopped up.
     */
    @Test
    public void splitSingleLongWord() {
        // A single word (no spaces) with length greater than MAX_LINE_LENGTH_PER_ATTRIBUTE * MAX_LINES_PER_ATTRIBUTE).
        final String text = "0000000000111111111122222222223333333333444444444455555555556666666666777777777788888888889999999999aaaaaaaaaabbbbbbbbbbccccccccccddddddddddeeeeeeeeeeffffffffffgggggggggghhhhhhhhhhiiiiiiiiiijjjjjjjjjjkkkkkkkkkk";

        final List<String> out = LabelUtilities.splitTextIntoLines(text);
        assertEquals(out.size(), LabelUtilities.MAX_LINES_PER_ATTRIBUTE);
        assertEquals(out.get(0), "00000000001111111111222222222233333333334444444444");
        assertEquals(out.get(1), "55555555556666666666777777777788888888889999999999");
        assertEquals(out.get(2), "aaaaaaaaaabbbbbbbbbbccccccccccddddddddddeeeeeeeeee");
        assertEquals(out.get(3), "ffffffffffgggggggggghhhhhhhhhhiiiiiiiiiijjjjjjjjj" + LabelUtilities.ELLIPSIS);
    }

    /**
     * A single long word with an irrelevant space should be chopped up.
     */
    @Test
    public void splitLongWordThenSpace() {
        // A single word (with an irrelevant space) with length greater than MAX_LINE_LENGTH_PER_ATTRIBUTE * MAX_LINES_PER_ATTRIBUTE).
        final String text = "0000000000111111111122222222223333333333444444444455555555556666666666777777777788888888889999999999aaaaaaaaaabbbbbbbbbbccccccccccddddddddddeeeeeeeeeeffffffffffgggggggggghhhhhhhhhhiiiiiiiiiijjjjjjjjjjkkkkkkkkk k";

        final List<String> out = LabelUtilities.splitTextIntoLines(text);
        assertEquals(out.size(), LabelUtilities.MAX_LINES_PER_ATTRIBUTE);
        assertEquals(out.get(0), "00000000001111111111222222222233333333334444444444");
        assertEquals(out.get(1), "55555555556666666666777777777788888888889999999999");
        assertEquals(out.get(2), "aaaaaaaaaabbbbbbbbbbccccccccccddddddddddeeeeeeeeee");
        assertEquals(out.get(3), "ffffffffffgggggggggghhhhhhhhhhiiiiiiiiiijjjjjjjjj" + LabelUtilities.ELLIPSIS);
        assertTrue(out.get(LabelUtilities.MAX_LINES_PER_ATTRIBUTE - 1).endsWith(LabelUtilities.ELLIPSIS));
    }

    @Test
    public void splitExample1() {
        final String text = "Now is the time for all good men to come to the aid of the party.";
        final List<String> out = LabelUtilities.splitTextIntoLines(text);
        assertEquals(out.size(), 2);
        assertEquals(out.get(0), "Now is the time for all good men to come to the");
        assertEquals(out.get(1), "aid of the party.");
    }

    private static String ofLength(final int len) {
        return ofLength(len, -1);
    }

    /**
     * Create a String of length n.
     * <p>
     * If ch is less than 0, the contents will consist of the characters 'A' ..
     * 'Z', otherwise if will consist of the character ch.
     *
     * @param len The length of the String to be created.
     * @param ch The contents of the String.
     *
     * @return A String of length len.
     */
    private static String ofLength(final int len, final int ch) {
        String s = "";
        for (int i = 0; i < len; i++) {
            s += (char) (ch < 0 ? 'A' + i % 26 : ch);
        }

        return s;
    }
}
