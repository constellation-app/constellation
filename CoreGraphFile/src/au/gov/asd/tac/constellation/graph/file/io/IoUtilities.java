/*
 * Copyright 2010-2024 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.graph.GraphElementType;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * A collection of convenient functions.
 *
 * @author algol
 */
public final class IoUtilities {

    private final SimpleDateFormat simpleDateFormatDateTime = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSSZ");
    private final SimpleDateFormat simpleDateFormatDate = new SimpleDateFormat("yyyy-MM-dd");
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    public IoUtilities() {
        simpleDateFormatDateTime.setTimeZone(UTC);
        simpleDateFormatDate.setTimeZone(UTC);
    }

    public static final Charset UTF8 = StandardCharsets.UTF_8;

    public static byte[] encode(final String s) {
        return s.getBytes(UTF8);
    }

    public static String decode(final byte[] bytes) {
        return UTF8.decode(ByteBuffer.wrap(bytes)).toString();
    }

    /**
     * Change specific characters in a string to harmless replacements.
     *
     * Arbitrary strings must be written to, and read from, tab-delimited lines
     * in a text file. This won't work if the strings contains characters such
     * as tab and newline. Therefore, any string which may contain a harmful
     * character must be escaped in such a way that eliminates those characters,
     * and allows the original string to be recovered.
     *
     * The following replacements are made: null &rarr; \0, tab &rarr; \t,
     * newline &rarr; \n, carriage-return &rarr; \r, \ &rarr; \\
     *
     * The documentation for BufferedReader.readLine() says "A line is
     * considered to be terminated by any one of a line feed ('\n'), a carriage
     * return ('\r'), or a carriage return followed immediately by a linefeed."
     * This means that an escaped string should be read back correctly from an
     * InputStream.
     *
     * @param s The string to be escaped.
     *
     * @return The escaped string.
     */
    public static String escape(final String s) {
        // Null is a special case.
        if (s == null) {
            return "\\0";
        }

        // Scan the string so if there aren't any changes to be made (which could be the common case),
        // there's no copying of substrings.
        final StringBuilder t = new StringBuilder();
        final int length = s.length();
        int begin = 0;
        for (int i = 0; i < length; i++) {
            final char c = s.charAt(i);

            final char replacement = switch (c) {
                case 9 -> 't';
                case 10 -> 'n';
                case 13 -> 'r';
                case '\\' -> '\\';
                default -> 0;
            };

            if (replacement != 0) {
                t.append(s.substring(begin, i)).append('\\').append(replacement);
                begin = i + 1;
            }
        }

        if (t.length() > 0) {
            t.append(s.substring(begin));
            return t.toString();
        }

        return s;
    }

    /**
     * The inverse of escape().
     *
     * Escape sequences that aren't one of the recognised replacement escapes
     * are errors.
     *
     * @param s The string to unescape.
     *
     * @return The unescaped string.
     */
    public static String unescape(final String s) {
        if ("\\0".equals(s)) {
            return null;
        }

        final StringBuilder t = new StringBuilder();
        final int length = s.length();
        int begin = 0;
        for (int i = 0; i < length; i++) {
            final char c = s.charAt(i);
            if (c == '\\') {
                if (i == length - 1) {
                    throw new IllegalArgumentException("Found illegal single trailing '\\'");
                }

                final char c2 = s.charAt(i + 1);
                final char replacement = switch (c2) {
                    case 't' -> 9;
                    case 'n' -> 10;
                    case 'r' -> 13;
                    case '\\' -> '\\';
                    default -> 0;
                };
                
                if (replacement == 0) {
                    throw new IllegalArgumentException(String.format("Unknown escaped character '%s' in «%s»", c2, s));
                }

                t.append(s.substring(begin, i)).append(replacement);
                i++;
                begin = i + 1;
            }
        }

        if (t.length() > 0) {
            t.append(s.substring(begin));
            return t.toString();
        }

        return s;
    }

    /**
     * A fast split method (slightly faster than String.split()).
     *
     * The maximum size of the resulting String[] must be known beforehand. The
     * actual size may be less.
     *
     * @param s The string to split.
     * @param size The expected size of the resulting array.
     * @param separator The separator character to split on.
     *
     * @return A String[] containing the fields that have been split out of the
     * string.
     */
    public static String[] split(final String s, final int size, final char separator) {
        final String[] a = new String[size];
        final int length = s.length();
        int pos = -1;
        int i = 0;
        while (pos < length) {
            int ixSep = s.indexOf(separator, pos + 1);
            if (ixSep == -1) {
                // If there are no more separators, there is still one more field to split.
                ixSep = s.length();
            }
            a[i++] = s.substring(pos + 1, ixSep);
            pos = ixSep;
        }

        return a;
    }

    /**
     * Parse a color string.
     *
     * The string is be three or four comma-separated float values, for example
     * "1,0,0" or "1,0,0,1" for red, "1,0.78,0" or "1,0.78,0,1" for orange. If
     * only three values are specified, the fourth (alpha) value is set to 1.0.
     *
     * A float[] rather than a Color is returned because it's smaller.
     *
     * @param s The string to be parsed.
     * @return A float[4] specifying the RGBA color components in the range
     * 0..1.
     *
     * @throws IllegalArgumentException if the color cannot be parsed.
     */
    public static float[] parseColor(final String s) {
        final float[] vec = new float[4];

        final String[] f = split(s, 4, ',');
        if (f[3] == null) {
            f[3] = "1.0";
        }
        for (int i = 0; i < f.length; i++) {
            vec[i] = Float.parseFloat(f[i]);
        }

        return vec;
    }

    /**
     * Parse a string in (something close to) ISO format to a Calendar.
     *
     * The following formats are accepted: 'yyyy-mm-dd hh:mm:ss+zzzz',
     * 'yyyy-mm-dd hh:mm:ss-zzzz', 'yyyy-mm-dd hh:mm:ss.SSS+zzzz', 'yyyy-mm-dd
     * hh:mm:ss.SSS-zzzz',
     *
     * (Note that this isn't strictly ISO format, which uses a T instead of a
     * space.)
     *
     * This is basically doing a SimpleDateFormat.parse(), but a lot faster. All
     * fields are numeric. The numeric timezone is mandatory.
     *
     * Parsing isn't strict: the date 2011-99-01 will be accepted. This reflects
     * the way that SimpleDateFormat.parse() works. The parsing is lenient in
     * other ways (for instance, punctuation isn't checked), but since the
     * context is parsing of dates from CONSTELLATION files, this isn't expected
     * to be a problem. However, this should not be taken as an excuse to write
     * syntactically incorrect datetime strings elsewhere.
     *
     * @param s An (almost) ISO datetime to be parsed.
     *
     * @return A Calendar representing the input datetime.
     *
     * @throws GraphParseException If the String can't be parsed.
     */
    public static Calendar parseDateTime(final String s) throws GraphParseException {
        try {
            final int y = Integer.parseInt(s.substring(0, 4), 10);
            final int m = Integer.parseInt(s.substring(5, 7), 10);
            final int d = Integer.parseInt(s.substring(8, 10), 10);

            final int h = Integer.parseInt(s.substring(11, 13), 10);
            final int min = Integer.parseInt(s.substring(14, 16), 10);
            final int sec = Integer.parseInt(s.substring(17, 19), 10);
            int ms;
            String z;
            final char c = s.charAt(19);
            if (c == '.') {
                // Optional milliseconds.
                ms = Integer.parseInt(s.substring(20, 23));
                z = s.substring(23, 28);
            } else {
                ms = 0;
                z = s.substring(19, 24);
            }

            final boolean isUtc = "Z".equals(z) || "z".equals(z) || "+0000".equals(z);
            final TimeZone tz = isUtc ? UTC : TimeZone.getTimeZone("GMT" + z);
            final Calendar cal = new GregorianCalendar(tz);
            cal.set(Calendar.YEAR, y);
            cal.set(Calendar.MONTH, m - 1);
            cal.set(Calendar.DAY_OF_MONTH, d);
            cal.set(Calendar.HOUR_OF_DAY, h);
            cal.set(Calendar.MINUTE, min);
            cal.set(Calendar.SECOND, sec);
            cal.set(Calendar.MILLISECOND, ms);

            return cal;
        } catch (final StringIndexOutOfBoundsException | NumberFormatException ex) {
            throw new GraphParseException("Can't parse datetime string '" + s + "'", ex);
        }
    }

    /**
     * Parse a string in date format to a Calendar.
     *
     * The following format is accepted: 'yyyy-mm-dd'
     *
     * Parsing isn't strict: the date 2011-99-01 will be accepted. This reflects
     * the way that SimpleDateFormat.parse() works.
     *
     * @param s A date to be parsed.
     *
     * @return A Calendar with UTC timezone representing the input date.
     *
     * @throws au.gov.asd.tac.constellation.graph.file.io.GraphParseException If
     * the date can't be parsed.
     */
    public static Calendar parseDate(final String s) throws GraphParseException {
        try {
            final int y = Integer.parseInt(s.substring(0, 4), 10);
            final int m = Integer.parseInt(s.substring(5, 7), 10);
            final int d = Integer.parseInt(s.substring(8, 10), 10);

            final Calendar cal = new GregorianCalendar(UTC);
            cal.set(Calendar.YEAR, y);
            cal.set(Calendar.MONTH, m - 1);
            cal.set(Calendar.DAY_OF_MONTH, d);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            return cal;
        } catch (final StringIndexOutOfBoundsException | NumberFormatException ex) {
            throw new GraphParseException("Can't parse date string '" + s + "'", ex);
        }
    }

    /**
     * Convert a GraphElementType to a String in an implementation-independent
     * way.
     *
     * @param type The GraphElementType to convert.
     *
     * @return A String representation of the given GraphElementType.
     */
    public static String getGraphElementTypeString(final GraphElementType type) {
        switch (type) {
            case GRAPH -> {
                return "graph";
            }
            case VERTEX -> {
                return "vertex";
            }
            case TRANSACTION -> {
                return "transaction";
            }
            case META -> {
                return "meta";
            }
            case null -> throw new IllegalArgumentException("Unwanted GraphElementType: " + type);
            default -> throw new IllegalArgumentException("Unwanted GraphElementType: " + type);
        }
    }

    /**
     * Convert an element type and simple name into a fully qualified name. This
     * allows us to use the same names for different element types.
     *
     * @param type A GraphElementType to qualify the name.
     * @param name The simple name.
     *
     * @return The fully qualified name.
     */
    public static String fullyQualifiedName(final GraphElementType type, final String name) {
        return getGraphElementTypeString(type) + String.valueOf(GraphFileConstants.FQ_SEPARATOR) + name;
    }

    /**
     * A Comparator that compares strings using lower case, and puts the special
     * names "_id", "_from", "_to" at the beginning.
     *
     */
    public static class LCComparator implements Comparator<String> {
        // Ensure that the special attributes are at the beginning of the names.

        private static String specialCaseSortName(final String name) {
            return switch (name) {
                case "_from" -> "\u0000";
                case "_to" -> "\u0001";
                case "_directed" -> "\u0002";
                default -> name.toLowerCase();
            };
        }

        @Override
        public int compare(final String s1, final String s2) {
            return specialCaseSortName(s1).compareTo(specialCaseSortName(s2));
        }
    }

    /**
     * Join the strings of an ArrayList&lt;String&gt; using the specified
     * character.
     *
     * @param a An ArrayList&lt;String&gt;.
     * @param separator The separator character used to join the strings
     * together.
     *
     * @return The strings joined using the separator character.
     */
    public static String join(final Iterable<String> a, final char separator) {
        final StringBuilder buf = new StringBuilder();
        for (final String s : a) {
            if (buf.length() > 0) {
                buf.append(separator);
            }
            buf.append(s);
        }

        return buf.toString();
    }
}
