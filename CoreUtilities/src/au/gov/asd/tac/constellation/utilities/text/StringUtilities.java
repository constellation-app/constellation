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
package au.gov.asd.tac.constellation.utilities.text;

import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * Provides various string operations such as escaping and pretty printing.
 *
 * @author algol
 * @author cygnus_x-1
 */
public class StringUtilities {

    public static final String ESCAPE_CHARACTER = "\\";
    public static final String SPECIAL_CHARACTERS = ".[]^$()*+?";

    private static final Pattern NON_SPECIAL_CHARACTERS = Pattern.compile("[^A-Za-z0-9]");
    private static final Pattern OPENING_SQUARE_BRACKET = Pattern.compile("^\\[");
    private static final Pattern ENDING_SQUARE_BRACKET = Pattern.compile("]$");

    public static boolean endsWithAny(String name, String... endings) {
        return Arrays.asList(endings).stream().anyMatch(ending -> StringUtils.endsWith(name, ending));
    }

    private StringUtilities() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Escape a String.
     * <p>
     * A "\" is used to escape the specified characters, and is therefore also
     * escaped. There are no special characters: "\n" is just an escaped "n".
     *
     * @param unescapedString The unescapedString to escape.
     * @param characters The characters to be escaped.
     *
     * @return An escaped unescapedString.
     */
    public static String escape(final String unescapedString, final String characters) {
        if (unescapedString == null) {
            return null;
        }

        final StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < unescapedString.length(); i++) {
            final String character = unescapedString.substring(i, i + 1);
            if (characters.contains(character) || character.equals(ESCAPE_CHARACTER)) {
                buffer.append(ESCAPE_CHARACTER);
            }

            buffer.append(character);
        }

        return buffer.toString();
    }

    /**
     * Escape and join a list of Strings.
     * <p>
     * A "\" is used to escape the delimiter character, and is therefore also
     * escaped. There are no special characters: "\n" is just an escaped "n".
     *
     * @param unescapedStrings The list of strings to escape then join.
     * @param delimiter The join delimiter, treated as a single character.
     *
     * @return An escaped string.
     */
    public static String escape(final List<String> unescapedStrings, final String delimiter) {
        if (unescapedStrings == null) {
            return null;
        }

        final StringJoiner buffer = new StringJoiner(delimiter);
        unescapedStrings.forEach(string -> buffer.add(escape(string, delimiter)));

        return buffer.toString();
    }

    /**
     * Unescape a String.
     * <p>
     * It is assumed that the escape character is "\".
     *
     * @param escapedString The string to unescape.
     * @param characters The characters to be unescaped.
     *
     * @return An unescaped String.
     */
    public static String unescape(final String escapedString, final String characters) {
        if (escapedString == null) {
            return null;
        }

        final StringBuilder buffer = new StringBuilder();
        boolean escaped = false;
        for (int i = 0; i < escapedString.length(); i++) {
            final String character = escapedString.substring(i, i + 1);
            if (escaped) {
                buffer.append(character);
                escaped = false;
            } else if (character.equals(ESCAPE_CHARACTER)) {
                escaped = true;
            } else {
                buffer.append(character);
            }
        }

        if (escaped) {
            // The escapedString ends with a single escape, which shouldn't happen.
            throw new IllegalArgumentException(String.format("Invalid escaped string '%s'", escapedString));
        }

        return buffer.toString();
    }

    /**
     * Split an escaped String into its unescaped parts.
     *
     * @param escapedString The String to be split.
     * @param delimiter The delimiter character(s) to split on.
     *
     * @return A list of unescaped strings.
     */
    public static List<String> splitEscaped(final String escapedString, final String delimiter) {
        final List<String> splits = new ArrayList<>();

        if (escapedString == null) {
            return splits;
        }

        if (escapedString.isEmpty()) {
            splits.add("");
        } else {
            int part0 = 0; // where does the current part begin?
            boolean escaped = false;
            for (int i = 0; i < escapedString.length(); i++) {
                final String character = escapedString.substring(i, i + 1);
                if (escaped) {
                    escaped = false;
                } else if (character.equals(ESCAPE_CHARACTER)) {
                    escaped = true;
                } else if (delimiter.contains(character)) {
                    splits.add(unescape(escapedString.substring(part0, i), delimiter));
                    part0 = i + 1;
                }
            }

            if (part0 < escapedString.length()) {
                splits.add(unescape(escapedString.substring(part0, escapedString.length()), delimiter));
            }
        }

        return splits;
    }

    /**
     * Remove the specified characters from the given string, if they exist. For
     * example, when you convert a {@link java.util.Collection} to a
     * {@link String}, square brackets are added to the edges of the
     * originalString which may not be wanted.
     *
     * @param originalString the String to be modified.
     * @param characters the characters to remove.
     * @param trim only remove characters from the edges of the originalString.
     * @return A String with characters removed.
     */
    public static String remove(final String originalString, final String characters, final boolean trim) {
        if (originalString == null) {
            return null;
        }

        String removedString = originalString;
        for (int i = 0; i < characters.length(); i++) {
            final String character = characters.substring(i, i + 1);
            if (trim) {
                if (removedString.startsWith(character)) {
                    removedString = removedString.replaceAll("^" + escape(character, SPECIAL_CHARACTERS), "");
                }
                if (removedString.endsWith(character)) {
                    removedString = removedString.replaceAll(escape(character, SPECIAL_CHARACTERS) + "$", "");
                }
            } else {
                removedString = removedString.replaceAll(character, "");
            }
        }

        return removedString;
    }

    public static String camelCase(final String originalString) {
        if (originalString == null) {
            return null;
        }

        final StringBuilder camelCaseString = new StringBuilder();
        boolean whitespaceDetected = false;
        for (int i = 0; i < originalString.length(); i++) {
            final String character = originalString.substring(i, i + 1);
            if (i == 0 || whitespaceDetected) {
                whitespaceDetected = false;
                camelCaseString.append(character.toUpperCase());
            } else {
                if (character.matches(SeparatorConstants.WHITESPACE)) {
                    whitespaceDetected = true;
                }
                camelCaseString.append(character.toLowerCase());
            }
        }

        return camelCaseString.toString();
    }

    /**
     * Convert the string representation of GraphLabelsAndDecorators to a list
     * of strings based on character(s) to split. This usually requires the
     * string to be escaped first.
     *
     * @param labelsString the labels string to split.
     * @param toSplitOn the characters to split on.
     * @return the labels string split into individual labels.
     */
    public static List<String> splitLabelsWithEscapeCharacters(final String labelsString, final Set<Character> toSplitOn) {
        // Split up the components of the graph labels and decorators string by
        // toSplitOn, checking for escaped toSplitOns in attribute names.
        final List<String> decoratorsAndLabelsComponentsList = new ArrayList<>();
        int currentStart = 0;
        int currentIndex = 0;
        int currentNumSlashes = 0;
        while (true) {
            if (currentIndex == labelsString.length()) {
                decoratorsAndLabelsComponentsList.add(labelsString.substring(currentStart, currentIndex));
                break;
            }
            final char current = labelsString.charAt(currentIndex);
            if (toSplitOn.contains(current) && (currentNumSlashes % 2) == 0) {
                decoratorsAndLabelsComponentsList.add(labelsString.substring(currentStart, currentIndex));
                currentNumSlashes = 0;
                currentStart = currentIndex + 1;
            } else if (current == '\\') {
                currentNumSlashes++;
            } else {
                currentNumSlashes = 0;
            }
            currentIndex++;
        }
        return decoratorsAndLabelsComponentsList;
    }

    public static String quoteAndDelimitString(final List<String> items, final char delimiter) {
        return quoteAndDelimitString(items, delimiter, '"', '\\');
    }

    public static String quoteAndDelimitString(final List<String> items, final char delimiter, final char quote, final char escapeCharacter) {
        if (items == null) {
            return null;
        }
        final StringBuilder quotedDelimitedString = new StringBuilder();
        items.forEach(item -> {
            if (item != null) {
                quotedDelimitedString.append(quote);
                quotedDelimitedString.append(escapeString(item, new char[]{delimiter, quote}, escapeCharacter));
                quotedDelimitedString.append(quote);
            }
            quotedDelimitedString.append(delimiter);
        });
        return quotedDelimitedString.toString();
    }

    public static List<String> unquoteAndSplitString(final String originalString, final char delimiter) {
        return unquoteAndSplitString(originalString, delimiter, '"', '\\');
    }

    public static List<String> unquoteAndSplitString(String originalString, final char delimiter, final char quote, final char escapeCharacter) {
        final List<String> strings = new ArrayList<>();

        if (originalString == null) {
            return strings;
        }

        while (!originalString.isEmpty()) {
            int delimiterIndex = -1;
            do {
                delimiterIndex = originalString.indexOf(delimiter, delimiterIndex + 1);
            } while (delimiterIndex > 0 && originalString.charAt(delimiterIndex - 1) != quote);
            if (delimiterIndex == -1) {
                throw new IllegalArgumentException("No unescaped delimiters found in remaining portion of string: " + originalString);
            }
            if (delimiterIndex == 0) {
                strings.add(null);
            } else {
                strings.add(unescapeString(originalString.substring(1, delimiterIndex - 1), new char[]{delimiter, quote}, escapeCharacter));
            }
            originalString = originalString.substring(delimiterIndex + 1);
        }
        return strings;
    }

    /**
     * Returns the supplied string with all characters in the supplied list
     * escaped with backslashes.
     *
     * @param originalString The String to escape.
     * @param metaCharacters An array of characters to be escaped
     * @return the supplied string with all characters in the supplied list
     * escaped with backslashes.
     */
    public static String escapeString(final String originalString, final char[] metaCharacters) {
        return escapeString(originalString, metaCharacters, '\\');
    }

    public static String escapeString(String originalString, final char[] metaCharacters, final char escapeCharacter) {
        if (originalString == null) {
            return null;
        }
        final String escape = String.valueOf(escapeCharacter);
        final String escapedEscape = escape + escape;
        originalString = originalString.replace(escape, escapedEscape);
        for (final char c : metaCharacters) {
            originalString = originalString.replace(Character.toString(c), escape + Character.toString(c));
        }
        return originalString;
    }

    /**
     * Returns the supplied string with all characters in the supplied list
     * unescaped with backslashes.
     *
     * @param escapedString The string to unescape
     * @param metaCharacters An array of characters to be unescaped
     * @return the supplied string with all characters in the supplied list
     * unescaped with backslashes.
     */
    public static String unescapeString(final String escapedString, final char[] metaCharacters) {
        return unescapeString(escapedString, metaCharacters, '\\');
    }

    public static String unescapeString(String escapedString, final char[] metaCharacters, final char escapeCharacter) {
        if (escapedString == null) {
            return null;
        }
        final String escape = String.valueOf(escapeCharacter);
        final String escapedEscape = escape + escape;
        escapedString = escapedString.replace(escapedEscape, escape);
        for (final char c : metaCharacters) {
            escapedString = escapedString.replace(escape + Character.toString(c), Character.toString(c));
        }
        return escapedString;
    }

    /**
     * Remove the outer [ ] if either exist. When you convert a
     * {@link java.util.Collection} to a {@link String}, square brackets are
     * added to the string.
     *
     * @param originalString the string to be modified.
     * @return A {@link String} with outer square brackets removed
     */
    public static String removeSquareBracketsFromString(final String originalString) {
        if (originalString.startsWith("[") || originalString.endsWith("]")) {
            return ENDING_SQUARE_BRACKET.matcher(
                    OPENING_SQUARE_BRACKET.matcher(originalString).replaceAll("")
            ).replaceAll("");
        } else {
            return originalString;
        }
    }

    /**
     * Remove special characters from the {@link String}
     *
     * @param originalString the string to be modified.
     * @return A {@link String} without special characters
     */
    public static String removeSpecialCharacters(final String originalString) {
        return originalString != null
                ? NON_SPECIAL_CHARACTERS.matcher(originalString).replaceAll("") : null;
    }

    /**
     * Returns list of tuples of all found hits of a search string (Start
     * position and End position) within the supplied text.
     * @param text the text to search
     * @param searchStr the string to search within the text
     * @return A list of tuples of all found hits
     */
    public static List<Tuple<Integer, Integer>> searchRange(final String text, final String searchStr) {
        final List<Tuple<Integer, Integer>> expected = new ArrayList<>();
        if ((text != null) && (searchStr != null)) {
            String lwrText = text.toLowerCase();
            final String lwrSearch = searchStr.toLowerCase();
            final int txtLen = lwrText.length();
            int currLen = txtLen;
            int currTxtStart = 0;
            int origTxtStart = 0;
            int hitStart = 0;
            int hitEnd = 0;
            while (hitEnd < txtLen) {
                if (lwrText.contains(lwrSearch)) {
                    origTxtStart = origTxtStart + hitEnd;
                    hitStart = lwrText.indexOf(lwrSearch);
                    hitEnd = hitStart + lwrSearch.length();
                    final Tuple<Integer, Integer> tuple = Tuple.create(origTxtStart + hitStart, origTxtStart + hitEnd);
                    expected.add(tuple);
                    currTxtStart = hitEnd;
                    lwrText = lwrText.substring(currTxtStart, currLen);
                    currLen = currLen - currTxtStart;
                } else {
                    break;
                }
            }
            return expected;
        }
        return Collections.emptyList();
    }
}
