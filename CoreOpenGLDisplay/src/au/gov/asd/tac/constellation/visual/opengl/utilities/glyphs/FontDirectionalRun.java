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
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.List;

/**
 * A run of text that has a single direction (TextAttribute.RUN_DIRECTION_LTR or
 * TextAttribute.RUN_DIRECTION_RTL).
 *
 * @author algol
 */
class FontDirectionalRun {

    final String run;
    final Boolean direction;

    /**
     * A new DirectionRun instance.
     *
     * @param run Text that has a single direction.
     * @param direction One of TextAttribute.RUN_DIRECTION_LTR or
     * TextAttribute.RUN_DIRECTION_RTL.
     */
    private FontDirectionalRun(final String run, final Boolean direction) {
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
     * Uses Character.getDirectionality() to get the directionality. Codepoints
     * with specific directionality become left 'L' or right 'R', all other
     * characters are undefined 'U'.
     *
     * @param codepoint The Unicode codepoint to get the direction of.
     *
     * @return One of 'L', 'R', 'U' (left, right, undefined).
     */
    private static char codepointDirection(final int codepoint) {
        final byte d = Character.getDirectionality(codepoint);

        return switch (d) {
            case Character.DIRECTIONALITY_RIGHT_TO_LEFT, Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC, Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING, Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE -> 'R';
            case Character.DIRECTIONALITY_LEFT_TO_RIGHT, Character.DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING, Character.DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE -> 'L';
            default -> 'U';
        };
    }

    /**
     * Find the beginnings and ends of runs of codepoints that have the same
     * direction.
     * <p>
     * If there are any RTL runs, the order of the runs is reversed.
     *
     * @param text Text to be broken into runs of the same direction.
     *
     * @return A List<DirectionRun>.
     */
    static List<FontDirectionalRun> getDirectionRuns(final String text) {
        final int length = text.length();

        char currDir = ' ';
        final ArrayList<FontDirectionalRun> runs = new ArrayList<>();

        int start = 0;
        for (int offset = 0; offset < length;) {
            final int codepoint = text.codePointAt(offset);
            final int cc = Character.charCount(codepoint);

            final char dir;
            final int cptype = Character.getType(codepoint);
            if ((cptype == Character.SPACE_SEPARATOR || cptype == Character.NON_SPACING_MARK)) {
                dir = currDir;
            } else {
                final char d = codepointDirection(codepoint);
                dir = d != 'R' ? 'L' : 'R';
            }

            if (dir != currDir && currDir != ' ') {
                runs.add(new FontDirectionalRun(text.substring(start, offset), currDir == 'L' ? TextAttribute.RUN_DIRECTION_LTR : TextAttribute.RUN_DIRECTION_RTL));
                start = offset;
            }

            currDir = dir;
            offset += cc;
        }

        // Add the end of the final run.
        runs.add(new FontDirectionalRun(text.substring(start, length), currDir == 'L' ? TextAttribute.RUN_DIRECTION_LTR : TextAttribute.RUN_DIRECTION_RTL));

        // If there is more than one direction (which implies that at least one
        // run is right-to-left), modify the order of the runs.
        if (runs.size() > 1) {
            final FontDirectionalRun run = runs.get(runs.size() - 1);

            // If the last run is LTR and ends with a type "<...>", put the type
            // in its own run and move that run in front of the other runs.
            // This may mean splitting the run if there is
            // non-type text present.
            //
            // (This is a bit weird, because at this level we have no
            // knowledge of types, but we're trying to make it look right for
            // the users.)
            if (run.direction.equals(TextAttribute.RUN_DIRECTION_LTR) && run.run.endsWith(">")) {
                final int lt = run.run.lastIndexOf('<');
                if (lt > -1) {
                    // Remove the run; it ends with a type, so it doesn't belong at the end.
                    runs.remove(runs.size() - 1);

                    if (lt == 0) {
                        // The less-than is at the start of the run, therefore the type is already in a run by itself; just insert it at the front.
                        runs.add(0, run);
                    } else {
                        // Split the run into two new runs and put them in their
                        // rightful places.
                        final FontDirectionalRun runType = new FontDirectionalRun(run.run.substring(lt), run.direction);
                        final FontDirectionalRun runOther = new FontDirectionalRun(run.run.substring(0, lt), run.direction);

                        // The text before the "<" goes back at the end.
                        runs.add(runOther);

                        // If the first run has the same direction, merge the type run and the first run, else just insert the type run at the front.
                        final FontDirectionalRun run0 = runs.get(0);
                        if (run0.direction.equals(runType.direction)) {
                            runs.set(0, new FontDirectionalRun(runType.run + run0.run, runType.direction));
                        } else {
                            runs.add(0, runType);
                        }
                    }
                }
            }
        }

        return runs;
    }

    @Override
    public String toString() {
        return String.format("[FontDirctionalRun[%s],%s", run, direction.equals(TextAttribute.RUN_DIRECTION_LTR) ? "LTR" : "RTL");
    }
}
