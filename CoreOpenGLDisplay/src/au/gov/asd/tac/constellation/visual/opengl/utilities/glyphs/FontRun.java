package au.gov.asd.tac.constellation.visual.opengl.utilities.glyphs;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Split a string into a sequence of runs such that each run contains a single font.
 * <p>
 * A sequence of fonts is applied to each codepoint in the string. The first font
 * that can display the codepoint determines the run.
 *
 * @author algol
 */
class FontRun {
    private static final Logger LOGGER = Logger.getLogger(GlyphManagerBI.class.getName());

    final String string;
    final Font font;

    private FontRun(final String s, final Font f) {
        this.string = s;
        this.font = f;
    }

    /**
     * Determine the font which the codepoint belongs to.
     * <p>
     * The first font in the fonts array that can display the given codepoint
     * id the font that determines the run.
     *
     * @param fonts An array of fonts.
     * @param codepoint We're looking for a font that can display this codepoint.
     *
     * @return The index of the first font in the array that can display this codepoint.
     *  If no font can display the codepoint, use the final font (which
     *  is a default fallback font anyway).
     */
    private static int whichFont(final FontInfo[] fontsInfo, final int codepoint) {
        for(int i=0; i<fontsInfo.length; i++) {
//            System.out.printf("Font: (%d) %s\n", i, fonts[i].getFontName());
//            if(fontsInfo[i].font.canDisplay(codepoint)) {
//                if(i==0) {
//                    // Controversy here.
//                    // If the first font is Arial for example, it includes Arabic characters,
//                    // so we don't get to use a subsequent Arabic font.
//                    // Therefore, we only use the first font if the codepoint is LATIN or COMMON.
//                    // To use the rest of the codepoints in the font, specify it again.
//                    //
//                    // TODO allow the user to specify which font displays which script?
//                    //
//                    final Character.UnicodeScript script = Character.UnicodeScript.of(codepoint);
//                    if(script.equals(Character.UnicodeScript.LATIN) || script.equals(Character.UnicodeScript.COMMON)) {
////                        System.out.printf("Font: %s %d script:%s block:%s\n", fonts[i].getFontName(), codepoint, Character.UnicodeScript.of(codepoint), Character.UnicodeBlock.of(codepoint));
//                        return i;
//                    }
//                } else {
////                    System.out.printf("Font: %s %d script:%s block:%s\n", fonts[i].getFontName(), codepoint, Character.UnicodeScript.of(codepoint), Character.UnicodeBlock.of(codepoint));
//                    return i;
//                }
//            }
            if(fontsInfo[i].canDisplay(codepoint)) {
                return i;
            }
        }

        LOGGER.warning(String.format("Font not found for codepoint U+%04X (%d.)", codepoint, codepoint));

        // If no font could display this codepoint, return the default font anyway.
        // TODO Figure out a way of displaying the missing glyph (U+FFFD) instead of a box.
        //
        return fontsInfo.length-1;
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
    static List<FontRun> getFontRuns(final String s, final FontInfo[] fontsInfo) {
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
            final int fontIx = codepoint==32 && currFontIx!=-1 ? currFontIx : whichFont(fontsInfo, codepoint);
            if(fontIx==-1) {
                final String t = new String(new int[]{fontsInfo[0].font.getMissingGlyphCode()}, 0, 1);
                frs.add(new FontRun(t, fontsInfo[0].font));
//                currFontIx = -1;
            } else {
                if(fontIx!=currFontIx) {
                    if(currFontIx!=-1) {
                        final String t = s.substring(start, offset);
                        frs.add(new FontRun(t, fontsInfo[currFontIx].font));
                    }
                    start = offset;
                    currFontIx = fontIx;
                }
            }

            offset += cc;
        }

       // TODO Fix non-displayable codepoints when a font doesn't exist.

        // Add the end of the final run.
        //
        final String t = s.substring(start, length);
        frs.add(new FontRun(t, fontsInfo[currFontIx].font));
//        System.out.printf("%d %d - [%s]\n", runs.get(runs.size()-2), length, text.subSequence(runs.get(runs.size()-2), length));

        return frs;
    }

    @Override
    public String toString() {
        return String.format("[FontRun[%s],%s]", string, font.getName());
    }
}
