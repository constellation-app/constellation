/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.utilities.text;

import java.util.Arrays;
import java.util.List;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 * String Utilities Test.
 *
 * @author algol
 */
public class StringUtilitiesNGTest {

    private static final String DELIM = ",";

    @Test
    public void escapeNothing() {
        final String s = "abc_123";
        assertEquals(StringUtilities.escape(s, DELIM), s);
    }

    @Test
    public void escape1() {
        final String s = "abc" + DELIM + "123";
        final String expected = "abc" + StringUtilities.ESCAPE_CHARACTER + DELIM + "123";
        assertEquals(StringUtilities.escape(s, DELIM), expected);
    }

    @Test
    public void escapeAll() {
        final String s = DELIM + DELIM + DELIM + DELIM;
        final String e = StringUtilities.ESCAPE_CHARACTER;
        final String expected = e + DELIM + e + DELIM + e + DELIM + e + DELIM;
        assertEquals(StringUtilities.escape(s, DELIM), expected);
    }

    @Test
    public void escapeEnd() {
        final String s = "abc" + StringUtilities.ESCAPE_CHARACTER;
        final String expected = s + StringUtilities.ESCAPE_CHARACTER;
        assertEquals(StringUtilities.escape(s, DELIM), expected);
    }

    @Test
    public void escapeList() {
        final List<String> ss = Arrays.asList("abc", "def", "ghi");
        final String expected = "abc" + DELIM + "def" + DELIM + "ghi";
        assertEquals(StringUtilities.escape(ss, DELIM), expected);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void unescapeEnd() {
        final String s = "abc" + StringUtilities.ESCAPE_CHARACTER;
        StringUtilities.unescape(s, DELIM);
    }

    @Test
    public void roundTrip1() {
        final String s = "abc" + DELIM + "123";
        assertEquals(StringUtilities.unescape(StringUtilities.escape(s, DELIM), DELIM), s);
    }

    @Test
    public void roundTripEscape1() {
        final String s = StringUtilities.ESCAPE_CHARACTER;
        assertEquals(StringUtilities.unescape(StringUtilities.escape(s, DELIM), DELIM), s);
    }

    @Test
    public void roundTripEscape2() {
        final String s = StringUtilities.ESCAPE_CHARACTER + StringUtilities.ESCAPE_CHARACTER;
        assertEquals(StringUtilities.unescape(StringUtilities.escape(s, DELIM), DELIM), s);
    }

    @Test
    public void roundTripEscape3() {
        final String s = StringUtilities.ESCAPE_CHARACTER + DELIM + StringUtilities.ESCAPE_CHARACTER;
        assertEquals(StringUtilities.unescape(StringUtilities.escape(s, DELIM), DELIM), s);
    }

    @Test
    public void roundTripAll() {
        final String s = DELIM + DELIM + DELIM + DELIM;
        assertEquals(StringUtilities.unescape(StringUtilities.escape(s, DELIM), DELIM), s);
    }

    @Test
    public void split() {
        final String s = "abc" + DELIM + "def";
        final List<String> expected = Arrays.asList("abc", "def");
        assertEquals(StringUtilities.splitEscaped(s, DELIM), expected);
    }

    @Test
    public void splitEmpty() {
        final String s = "";
        final List<String> expected = Arrays.asList("");
        assertEquals(StringUtilities.splitEscaped(s, DELIM), expected);
    }

    @Test
    public void splitMulti() {
        final String s = "abc" + DELIM + "def;ghi";
        final List<String> expected = Arrays.asList("abc", "def", "ghi");
        assertEquals(StringUtilities.splitEscaped(s, DELIM + SeparatorConstants.SEMICOLON), expected);
    }

    @Test
    public void splitNone() {
        final String s = "abc" + StringUtilities.ESCAPE_CHARACTER + DELIM + "def";
        final List<String> expected = Arrays.asList("abc" + DELIM + "def");
        assertEquals(StringUtilities.splitEscaped(s, DELIM), expected);
    }

    @Test
    public void splitEscapeNone() {
        final String s = "abc" + StringUtilities.ESCAPE_CHARACTER + StringUtilities.ESCAPE_CHARACTER + "def";
        final List<String> expected = Arrays.asList("abc" + StringUtilities.ESCAPE_CHARACTER + "def");
        assertEquals(StringUtilities.splitEscaped(s, DELIM), expected);
    }

    @Test
    public void splitEmbedded() {
        final String s = "abc,def\\,ghi,jkl";
        final List<String> expected = Arrays.asList("abc", "def,ghi", "jkl");
        assertEquals(StringUtilities.splitEscaped(s, DELIM), expected);
    }

    @Test
    public void trimSquareBrackets() {
        final String s = Arrays.asList("abc", "def", "ghi", "jkl").toString();
        final String expected = "abc, def, ghi, jkl";
        assertEquals(StringUtilities.remove(s, "[]", true), expected);
    }

    @Test
    public void camelCasePhrase() {
        final String s = "The quick brown fox";
        final String expected = "The Quick Brown Fox";
        assertEquals(StringUtilities.camelCase(s), expected);
    }

    @Test
    public void removeSpecialCharactersWithSymbols() {
        final String s = "@bcd3fgh! JklmnøP";
        final String expected = "bcd3fghJklmnP";
        assertEquals(StringUtilities.removeSpecialCharacters(s), expected);
    }
}
