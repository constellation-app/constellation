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
package au.gov.asd.tac.constellation.visual.opengl.utilities.glyphs;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Split a string into a sequence of runs such that each run contains a single
 * font.
 * <p>
 * A sequence of fonts is applied to each codepoint in the string. The first
 * font that can display the codepoint determines the run.
 *
 * @author algol
 */
class FontRunSequence {

    private static final Logger LOGGER = Logger.getLogger(FontRunSequence.class.getName());

    final String string;
    final Font font;

    private FontRunSequence(final String s, final Font f) {
        this.string = s;
        this.font = f;
    }

    /**
     * Determine the font which the code-point belongs to.
     * <p>
     * The first font in the fonts array that can display the given code-point
     * id the font that determines the run.
     *
     * @param fonts An array of fonts.
     * @param codepoint We're looking for a font that can display this
     * code-point.
     *
     * @return The index of the first font in the array that can display this
     * code-point. If no font can display the code-point, use the final font
     * (which is a default fallback font anyway).
     */
    private static int whichFont(final FontInfo[] fontsInfo, final int codepoint) {
        for (int i = 0; i < fontsInfo.length; i++) {
            if (fontsInfo[i].canDisplay(codepoint)) {
                return i;
            }
        }

        LOGGER.log(Level.WARNING, "{0}", String.format("Font not found for codepoint U+%04X (%d.)", codepoint, codepoint));

        // If no font could display this codepoint, return the default font anyway.
        return fontsInfo.length - 1;
    }

    /**
     * Find the beginnings and ends of runs of codepoints that have the same
     * font.
     * <p>
     * We don't want to use Font.canDisplayUpTo(). Consider the string
     * containing Chinese and English text "CCCEEECCC". A font such as Noto Sans
     * CJK SC Regular contains both Chinese and Latin characters, so
     * Font.CanDisplayUpTo() would consume the entire string. This is no good if
     * we want to use a different font style for Latin characters. Therefore, we
     * look at each individual character. Obviously this requires that specific
     * fonts appear first in the font list.
     *
     * @param text
     *
     * @return A List<FontRun> font runs.
     */
    static List<FontRunSequence> getFontRuns(final String s, final FontInfo[] fontsInfo) {
        final int length = s.length();

        int currFontIx = -1;
        int start = 0;
        final ArrayList<FontRunSequence> frs = new ArrayList<>();

        for (int offset = 0; offset < length;) {
            final int codepoint = s.codePointAt(offset);
            final int cc = Character.charCount(codepoint);

            // If this is a space, make it the same font as the previous codepoint, keeping words of the same font together.
            final int fontIx = codepoint == 32 && currFontIx != -1 ? currFontIx : whichFont(fontsInfo, codepoint);
            if (fontIx == -1) {
                final String t = new String(new int[]{fontsInfo[0].font.getMissingGlyphCode()}, 0, 1);
                frs.add(new FontRunSequence(t, fontsInfo[0].font));
            } else {
                if (fontIx != currFontIx) {
                    if (currFontIx != -1) {
                        final String t = s.substring(start, offset);
                        frs.add(new FontRunSequence(t, fontsInfo[currFontIx].font));
                    }
                    start = offset;
                    currFontIx = fontIx;
                }
            }

            offset += cc;
        }

        // TODO: fix non-displayable codepoints when a font doesn't exist.
        // Add the end of the final run.
        final String t = s.substring(start, length);
        frs.add(new FontRunSequence(t, fontsInfo[currFontIx].font));

        return frs;
    }

    @Override
    public String toString() {
        return String.format("[FontRunSequence[%s],%s]", string, font.getName());
    }
}
