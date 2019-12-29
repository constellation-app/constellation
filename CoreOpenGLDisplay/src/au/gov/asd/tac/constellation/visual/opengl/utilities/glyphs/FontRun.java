package au.gov.asd.tac.constellation.visual.opengl.utilities.glyphs;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author algol
 */
class FontRun {
    final String string;
    final Font font;

    private FontRun(final String s, final Font f) {
        this.string = s;
        this.font = f;
    }

    private static int whichFont(final Font[] fonts, final int codepoint) {
        for(int i=0; i<fonts.length; i++) {
            if(fonts[i].canDisplay(codepoint)) {
//                System.out.printf("-- %s %d\n", fonts[i].getName(), codepoint);
                return i;
            }
        }

//        System.out.printf("**** Font not found for codepoint %d\n", codepoint);
//        return -1;

        // If no font could display this codepoint, return the default font anyway.
        // TODO Figure out a way of displaying the missing glyph (U+FFFD) instead of a box.
        //
        return fonts.length-1;
    }

    /**
     * Find the beginnings and ends of runs of codepoints that have the same font.
     * <p>
     * We don't want to use Font.canDisplayUpTo().
     * Consider the string containing Chinese and English text "CCCEEECCC".
     * A font such as Noto Sans CJK SC Regular contains both Chinese and Latin
     * characters, so Font.CanDisplayUpTo() would consume the entire string.
     * This is no good if we want to use a different font style for Latin characters.
     * Therefore, we look at each individual character.
     * Obviously this requires that specific fonts appear first in the font list.
     *
     * @param text
     *
     * @return A List<FontRun> font runs.
     */
    static List<FontRun> getFontRuns(final String s, final Font[] fonts) {
        final int length = s.length();

        int currFontIx = -1;
        int start = 0;
        final ArrayList<FontRun> frs = new ArrayList<>();

        for(int offset = 0; offset < length;) {
            final int codepoint = s.codePointAt(offset);
            final int cc = Character.charCount(codepoint);

            // If this is a space, make it the same font as the previous codepoint.
            // This keeps words of the same font together.
            //
            final int fontIx = codepoint==32 && currFontIx!=-1 ? currFontIx : whichFont(fonts, codepoint);
            if(fontIx==-1) {
                final String t = new String(new int[]{fonts[0].getMissingGlyphCode()}, 0, 1);
                frs.add(new FontRun(t, fonts[0]));
//                currFontIx = -1;
            } else {
                if(fontIx!=currFontIx) {
                    if(currFontIx!=-1) {
                        final String t = s.substring(start, offset);
                        frs.add(new FontRun(t, fonts[currFontIx]));
                    }
                    start = offset;
                    currFontIx = fontIx;
                }
            }

            offset += cc;
        }

       // TODO Fix non-displayable codepoints when a font doesn't exist.
       // At this point fontIx could currently be -1 from whichFont. Fix it.

        // Add the end of the final run.
        //
        final String t = s.substring(start, length);
        frs.add(new FontRun(t, fonts[currFontIx]));
//        System.out.printf("%d %d - [%s]\n", runs.get(runs.size()-2), length, text.subSequence(runs.get(runs.size()-2), length));

        return frs;
    }

    @Override
    public String toString() {
        return String.format("[[%s],%s]", string, font.getName());
    }
}
