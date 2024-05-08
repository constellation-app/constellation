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
package au.gov.asd.tac.constellation.graph.file.io;

import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

/**
 * Tools Test.
 *
 * @author algol
 */
public class IOUtilitiesNGTest {

    static final SimpleDateFormat SDF_DT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
    static final SimpleDateFormat SDF_DT_MS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
    static final SimpleDateFormat SDF_D = new SimpleDateFormat("yyyy-MM-dd");
    static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    static {
        SDF_DT.setTimeZone(UTC);
        SDF_D.setTimeZone(UTC);
    }

    @Test
    public void escapeEmpty() {
        assertEquals("Empty string", "", IoUtilities.escape(""));
    }

    @Test
    public void unescapeEmpty() {
        assertEquals("Empty string", "", IoUtilities.unescape(""));
    }

    @Test
    public void roundTripEmpty() {
        assertEquals("Empty string", "", IoUtilities.unescape(IoUtilities.escape("")));
    }

    @Test
    public void escapeNull() {
        assertEquals("Null", "\\0", IoUtilities.escape(null));
    }

    @Test
    public void unescapeNull() {
        assertNull(IoUtilities.unescape("\\0"));
    }

    @Test
    public void roundTripNull() {
        assertNull(IoUtilities.unescape(IoUtilities.escape(null)));
    }

    @Test
    public void escapeBackslash() {
        assertEquals("Backslash", "\\\\", IoUtilities.escape("\\"));
    }

    @Test
    public void unescapeBackslash() {
        assertEquals("Backslash", "\\", IoUtilities.unescape("\\\\"));
    }

    @Test
    public void unescapeBackslash2() {
        assertEquals("Backslash", "x\\y", IoUtilities.unescape("x\\\\y"));
    }

    @Test
    public void roundTripBackslash() {
        final String in = "\\";
        assertEquals("Roundtrip backslash", in, IoUtilities.unescape(IoUtilities.escape(in)));
    }

    @Test
    public void escapeTab() {
        assertEquals("Tab", "\\t", IoUtilities.escape(SeparatorConstants.TAB));
    }

    @Test
    public void escapeData() {
        final String in = "GRATIS LOS@@^[D^]$^[O:\\^]^[D^]AF<UNKNOWN.unknown>";
        final String expected = "GRATIS LOS@@^[D^]$^[O:\\\\^]^[D^]AF<UNKNOWN.unknown>";
        assertEquals("Data", expected, IoUtilities.escape(in));
    }

    @Test
    public void unescapeTab() {
        assertEquals("Tab", SeparatorConstants.TAB, IoUtilities.unescape("\\t"));
    }

    @Test
    public void roundTripTab() {
        final String in = SeparatorConstants.TAB;
        assertEquals("Roundtrip tab", in, IoUtilities.unescape(IoUtilities.escape(in)));
    }

    @Test
    public void escapeNL() {
        assertEquals("New line", "\\n", IoUtilities.escape(SeparatorConstants.NEWLINE));
    }

    @Test
    public void unescapeNL() {
        assertEquals("New line", SeparatorConstants.NEWLINE, IoUtilities.unescape("\\n"));
    }

    @Test
    public void roundTripNL() {
        final String in = SeparatorConstants.NEWLINE;
        assertEquals("Roundtrip NL", in, IoUtilities.unescape(IoUtilities.escape(in)));
    }

    @Test
    public void escapeCR() {
        assertEquals("Carriage return", "\\r", IoUtilities.escape("\r"));
    }

    @Test
    public void unescapeCR() {
        assertEquals("Carriage return", "\r", IoUtilities.unescape("\\r"));
    }

    @Test
    public void roundTripCR() {
        final String in = "\r";
        assertEquals("Roundtrip CR", in, IoUtilities.unescape(IoUtilities.escape(in)));
    }

    @Test
    public void escapeControl() {
        String controlChars = "";
        for (int i = 0; i < ' '; i++) {
            if (i != 9 && i != 10 && i != 13) {
                controlChars += (char) i;
            }
        }

        assertEquals("Control characters", controlChars, IoUtilities.escape(controlChars));
    }

    @Test
    public void unescapeControl() {
        String controlChars = "";
        for (int i = 0; i < ' '; i++) {
            if (i != 9 && i != 10 && i != 13) {
                controlChars += (char) i;
            }
        }

        assertEquals("Control characters", controlChars, IoUtilities.unescape(controlChars));
    }

    @Test
    public void roundTripControl() {
        String controlChars = "";
        for (int i = 0; i < ' '; i++) {
            if (i != 9 && i != 10 && i != 13) {
                controlChars += (char) i;
            }
        }

        assertEquals("Control characters", controlChars, IoUtilities.unescape(IoUtilities.escape(controlChars)));
    }

    @Test
    public void escapeAll() {
        final String in = "\t\r\n\\";
        final String out = "\\t\\r\\n\\\\";
        assertEquals("All", out, IoUtilities.escape(in));
    }

    @Test
    public void unescapeAll() {
        final String out = "\t\r\n\\";
        final String in = "\\t\\r\\n\\\\";
        assertEquals("All", out, IoUtilities.unescape(in));
    }

    @Test
    public void roundTripAll() {
        final String in = "\t\r\n\\";
        assertEquals("Roundtrip all", in, IoUtilities.unescape(IoUtilities.escape(in)));
    }

    @Test
    public void escapeMix1() {
        final String in = "\\_\n_\r_\t_\\_\\n_\\r_\\t_";
        final String out = "\\\\_\\n_\\r_\\t_\\\\_\\\\n_\\\\r_\\\\t_";
        assertEquals("Mix", out, IoUtilities.escape(in));
    }

    @Test
    public void unescapeMix1() {
        final String out = "\\_\n_\r_\t_\\_\\n_\\r_\\t_";
        final String in = "\\\\_\\n_\\r_\\t_\\\\_\\\\n_\\\\r_\\\\t_";
        assertEquals("Mix", out, IoUtilities.unescape(in));
    }

    @Test
    public void roundTripMix1() {
        final String in = "\\_\n_\r_\t_\\_\\n_\\r_\\t_";
        assertEquals("Roundtrip mix 1", in, IoUtilities.unescape(IoUtilities.escape(in)));
    }

    @Test
    public void escapeMix2() {
        final String in = "\\\n\r\t\\\\n\\r\\t";
        final String out = "\\\\\\n\\r\\t\\\\\\\\n\\\\r\\\\t";
        assertEquals("Mix2", out, IoUtilities.escape(in));
    }

    @Test
    public void unescapeMix2() {
        final String out = "\\\n\r\t\\\\n\\r\\t";
        final String in = "\\\\\\n\\r\\t\\\\\\\\n\\\\r\\\\t";
        assertEquals("Mix2", out, IoUtilities.unescape(in));
    }

    @Test
    public void roundTripMix2() {
        final String in = "\\\\\\n\\r\\t\\\\\\\\n\\\\r\\\\t";
        assertEquals("Roundtrip mix 2", in, IoUtilities.unescape(IoUtilities.escape(in)));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void unescapeBadNotNull() {
        IoUtilities.unescape("x\\0");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void unescapeBadTrailingBackslash() {
        IoUtilities.unescape("trailing\\");
        fail("Trailing backslash should fail");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void unescapeBadChar() {
        IoUtilities.unescape("\\b");
        fail("Bad character should fail");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void unescapeBadNull() {
        IoUtilities.unescape(null);
        fail("Unescaping null should fail");
    }

    @Test
    public void split() {
        final String in = "a;b;c;d;e";
        final String[] expected = new String[]{"a", "b", "c", "d", "e"};
        final String[] actual = IoUtilities.split(in, expected.length, ';');
        assertArrayEquals("Split", expected, actual);
    }

    @Test
    public void splitWithBlank() {
        final String in = "a\tb\tc\td\t\te";
        final String[] expected = new String[]{"a", "b", "c", "d", "", "e"};
        final String[] actual = IoUtilities.split(in, expected.length, '\t');
        assertArrayEquals("Split", expected, actual);
    }

    @Test
    public void splitWithBlankEnd() {
        final String in = "a\tb\tc\td\te\t";
        final String[] expected = new String[]{"a", "b", "c", "d", "e", ""};
        final String[] actual = IoUtilities.split(in, expected.length, '\t');
        assertArrayEquals("Split", expected, actual);
    }

    @Test
    public void splitWithNonMaximumLength() {
        final String in = "a;b;c;d;e";
        final String[] expected = new String[]{"a", "b", "c", "d", "e", null};
        final String[] actual = IoUtilities.split(in, expected.length, ';');
        assertArrayEquals("Split", expected, actual);
    }

    @Test
    public void parseColor1() {
        final String in = "1,0,0,1";
        final float[] actual = IoUtilities.parseColor(in);
        final float[] expected = {1.0f, 0.0f, 0.0f, 1.0f};
        assertArrayEquals("Parsed color", expected, actual, 0.0f);
    }

    @Test
    public void parseColor2() {
        final String in = "1.0,0.0,0.0,1.0";
        final float[] actual = IoUtilities.parseColor(in);
        final float[] expected = {1.0f, 0.0f, 0.0f, 1.0f};
        assertArrayEquals("Parsed color", expected, actual, 0.0f);
    }

    @Test
    public void parseColorNoAlpha() {
        final String in = "1.0,0.0,0.0";
        final float[] actual = IoUtilities.parseColor(in);
        final float[] expected = {1.0f, 0.0f, 0.0f, 1.0f};
        assertArrayEquals("Parsed color", expected, actual, 0.0f);
    }

    @Test(expectedExceptions = NumberFormatException.class)
    public void parseColorNameBad() {
        final String in = "red";
        IoUtilities.parseColor(in);
        fail("Can't parse names");
    }

    @Test
    public void parseDateTime1() throws ParseException, GraphParseException {
        final String d = "2011-09-02 10:56:30+0000";
        final Date expected = SDF_DT.parse(d);
        final Date actual = IoUtilities.parseDateTime(d).getTime();
        assertEquals("Datetime parsed", expected, actual);
    }

    @Test
    public void parseDateTime1ms() throws ParseException, GraphParseException {
        final String d = "2011-09-02 10:56:30.123+0000";
        final Date expected = SDF_DT_MS.parse(d);
        final Date actual = IoUtilities.parseDateTime(d).getTime();
        assertEquals("Datetime parsed", expected, actual);
    }

    @Test
    public void parseDateTime2() throws ParseException, GraphParseException {
        final String d = "2011-09-02 12:56:30+1000";
        final Date expected = SDF_DT.parse(d);
        final Date actual = IoUtilities.parseDateTime(d).getTime();
        assertEquals("Datetime parsed", expected, actual);
    }

    @Test
    public void parseDateTime2ms() throws ParseException, GraphParseException {
        final String d = "2011-09-02 12:56:30.456+1000";
        final Date expected = SDF_DT_MS.parse(d);
        final Date actual = IoUtilities.parseDateTime(d).getTime();
        assertEquals("Datetime parsed", expected, actual);
    }

    @Test(expectedExceptions = GraphParseException.class)
    public void parseDateTimeBad1() throws ParseException, GraphParseException {
        final String d = "7";
        IoUtilities.parseDateTime(d);
        fail("Invalid datetime shouldn't parse");
    }

    @Test(expectedExceptions = GraphParseException.class)
    public void parseDateTimeBad2() throws ParseException, GraphParseException {
        final String d = "11-09-02 12:56:30.456+1000";
        IoUtilities.parseDateTime(d);
        fail("Invalid datetime shouldn't parse");
    }

    @Test
    public void parseDateTimeBadButWorks() throws ParseException, GraphParseException {
        final String d = "2011-99-02 12:56:30.456+1000";
        final Date expected = SDF_DT_MS.parse(d);
        final Date actual = IoUtilities.parseDateTime(d).getTime();
        assertEquals("Datetime parsed", expected, actual);
    }

    @Test
    public void parseDate1() throws ParseException, GraphParseException {
        final String d = "2011-09-02";
        final Date expected = SDF_D.parse(d);
        final Date actual = IoUtilities.parseDate(d).getTime();
        assertEquals("Date parsed", expected, actual);
    }

    @Test(expectedExceptions = GraphParseException.class)
    public void parseDateBad1() throws ParseException, GraphParseException {
        final String d = "7";
        IoUtilities.parseDate(d);
        fail("Invalid date shouldn't parse");
    }

    @Test(expectedExceptions = GraphParseException.class)
    public void parseDateBad2() throws ParseException, GraphParseException {
        final String d = "11-09-02";
        IoUtilities.parseDateTime(d);
        fail("Invalid date shouldn't parse");
    }

    @Test
    public void lcComparator() {
        final String[] strings = {"Second", "Zlast", "first", "third", "_aardvark", "_to", "_from", "_id"};
        final ArrayList<String> in = new ArrayList<>();
        in.addAll(Arrays.asList(strings));
        final String[] expected = {"_from", "_to", "_aardvark", "_id", "first", "Second", "third", "Zlast"};
        Collections.sort(in, new IoUtilities.LCComparator());
        assertArrayEquals("Sort ignore case", expected, in.toArray(new String[in.size()]));
    }

    @Test
    public void join() {
        final String[] strings = {"a", "b", "c", "d"};
        final ArrayList<String> in = new ArrayList<>();
        in.addAll(Arrays.asList(strings));
        final String expected = "a,b,c,d";
        final String actual = IoUtilities.join(in, ',');
        assertEquals("Joined string", expected, actual);
    }
}
