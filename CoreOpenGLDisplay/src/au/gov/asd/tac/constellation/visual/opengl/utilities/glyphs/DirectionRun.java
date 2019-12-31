package au.gov.asd.tac.constellation.visual.opengl.utilities.glyphs;

import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A run of text that has a single direction (TextAttribute.RUN_DIRECTION_LTR or TextAttribute.RUN_DIRECTION_RTL).
 *
 * @author algol
 */
class DirectionRun {
    final String run;
    final Boolean direction;

    /**
     * A new DirectionRun instance.
     *
     * @param run Text that has a single direction.
     * @param direction One of TextAttribute.RUN_DIRECTION_LTR or TextAttribute.RUN_DIRECTION_RTL.
     */
    private DirectionRun(final String run, final Boolean direction) {
        this.run = run;
        this.direction = direction;
    }

    /**
     * The corresponding Font layout direction to this run direction.
     *
     * @return One of Font.LAYOUT_LEFT_TO_RIGHT or Font.LAYOUT_RIGHT_TO_LEFT.
     */
    int getFontLayoutDirection() {
        return direction.equals(TextAttribute.RUN_DIRECTION_LTR)
                ? Font.LAYOUT_LEFT_TO_RIGHT
                : Font.LAYOUT_RIGHT_TO_LEFT;
    }

    /**
     * Return the direction of the specified codepoint, one of 'L', 'R', 'U'.
     * <p>
     * Uses Character.getDirectionality() to get the directionality.
     * Codepoints with specific directionality become left 'L' or right 'R',
     * all other characters are undefined 'U'.
     *
     * @param codepoint The Unicode codepoint to get the direction of.
     *
     * @return One of 'L', 'R', 'U' (left, right, undefined).
     */
    private static char codepointDirection(final int codepoint) {
        final byte d = Character.getDirectionality(codepoint);

        switch (d) {
            case Character.DIRECTIONALITY_RIGHT_TO_LEFT:
            case Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC:
            case Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING:
            case Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE:
                return 'R';

            case Character.DIRECTIONALITY_LEFT_TO_RIGHT:
            case Character.DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING:
            case Character.DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE:
                return 'L';

            default:
                return 'U';
        }
    }

    /**
     * Find the beginnings and ends of runs of codepoints that have the same direction.
     * <p>
     * If there are any RTL runs, the order of the runs is reversed.
     *
     * @param text Text to be broken into runs of the same direction.
     *
     * @return A List<DirectionRun>.
     */
    static List<DirectionRun> getDirectionRuns(final String text) {
        final int length = text.length();

        char currDir = ' ';
        final ArrayList<DirectionRun> runs = new ArrayList<>();

        int start = 0;
        for(int offset = 0; offset < length;) {
            final int codepoint = text.codePointAt(offset);
            final int cc = Character.charCount(codepoint);

            final char dir;
            final int cptype = Character.getType(codepoint);
            /*codepoint==32*/
            if((cptype==Character.SPACE_SEPARATOR || cptype==Character.NON_SPACING_MARK)) {
                dir = currDir;
            } else {
                final char d = codepointDirection(codepoint);
                dir = d!='R' ? 'L' : 'R';
            }
//            System.out.printf("codepoint %d %d %c %c %d %s\n", offset, codepoint, currDir, dir, Character.getType(codepoint), Character.UnicodeBlock.of(codepoint));
            if(dir!=currDir && currDir!=' ') {
//                System.out.printf("CHDIR %s %s\n", start, offset);
                runs.add(new DirectionRun(text.substring(start, offset), currDir=='L'?TextAttribute.RUN_DIRECTION_LTR:TextAttribute.RUN_DIRECTION_RTL));
                start = offset;
            }

            currDir = dir;
            offset += cc;
        }

        // Add the end of the final run.
        //
        runs.add(new DirectionRun(text.substring(start, length), currDir=='L'?TextAttribute.RUN_DIRECTION_LTR:TextAttribute.RUN_DIRECTION_RTL));

        // If there is more than one direction (which implies that at least one
        // run is right-to-left), or if the first direction is RTL, reverse the runs.
        // Since what was the final run is now the first run, set the
        // first direction.
        //
        if(runs.size()>1 || runs.get(0).direction.equals(TextAttribute.RUN_DIRECTION_RTL)) {
            Collections.reverse(runs);
        }

//        System.out.printf("* dir runs %d %s\n", runs.size(), runs.get(0).direction);
//        runs.stream().forEach(s -> System.out.printf("* dir  run  %d %d [%s]\n", s.run.length(), s.run.codePointAt(0), s));

        return runs;
    }

    @Override
    public String toString() {
        return String.format("[[%s],%s", run, direction.equals(TextAttribute.RUN_DIRECTION_LTR) ? "LTR" : "RTL");
    }
}
