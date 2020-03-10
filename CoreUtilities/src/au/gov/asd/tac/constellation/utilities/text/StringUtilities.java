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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Provides various string operations such as escaping and pretty printing.
 *
 * @author algol
 * @author cygnus_x-1
 */
public class StringUtilities {

    public static final String ESCAPE_CHARACTER = "\\";
    public static final String SPECIAL_CHARACTERS = ".[]^$()*+?";

    /**
     * Escape a String.
     * <p>
     * A "\" is used to escape the specified characters, and is therefore also
     * escaped. There are no special characters: "\n" is just an escaped "n".
     *
     * @param string The string to escape.
     * @param characters The characters to be escaped.
     *
     * @return An escaped string.
     */
    public static String escape(final String string, final String characters) {
        if (string == null) {
            return null;
        }

        final StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            final String character = string.substring(i, i + 1);
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
     * @param strings The list of strings to escape then join.
     * @param delimiter The join delimiter, treated as a single character.
     *
     * @return An escaped string.
     */
    public static String escape(final List<String> strings, final String delimiter) {
        if (strings == null) {
            return null;
        }

        final StringJoiner buffer = new StringJoiner(delimiter);
        strings.forEach(string -> buffer.add(escape(string, delimiter)));

        return buffer.toString();
    }

    /**
     * Unescape a String.
     * <p>
     * It is assumed that the escape character is "\".
     *
     * @param string The string to unescape.
     * @param characters The characters to be unescaped.
     *
     * @return An unescaped string.
     */
    public static String unescape(final String string, final String characters) {
        if (string == null) {
            return null;
        }

        final StringBuilder buffer = new StringBuilder();
        boolean escaped = false;
        for (int i = 0; i < string.length(); i++) {
            final String character = string.substring(i, i + 1);
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
            // The string ends with a single escape, which shouldn't happen.
            throw new IllegalArgumentException(String.format("Invalid escaped string '%s'", string));
        }

        return buffer.toString();
    }

    /**
     * Split an escaped String into its unescaped parts.
     *
     * @param string The string to be split.
     * @param delimiter The delimiter character(s) to split on.
     *
     * @return A list of unescaped strings.
     */
    public static List<String> splitEscaped(final String string, final String delimiter) {
        if (string == null) {
            return null;
        }

        final List<String> splits = new ArrayList<>();
        if (string.isEmpty()) {
            splits.add("");
        } else {
            int part0 = 0; // where does the current part begin?
            boolean escaped = false;
            for (int i = 0; i < string.length(); i++) {
                final String character = string.substring(i, i + 1);
                if (escaped) {
                    escaped = false;
                } else if (character.equals(ESCAPE_CHARACTER)) {
                    escaped = true;
                } else if (delimiter.contains(character)) {
                    splits.add(unescape(string.substring(part0, i), delimiter));
                    part0 = i + 1;
                }
            }

            if (part0 < string.length()) {
                splits.add(unescape(string.substring(part0, string.length()), delimiter));
            }
        }

        return splits;
    }

    /**
     * Remove the specified characters from the given string, if they exist. For
     * example, when you convert a {@link java.util.Collection} to a
     * {@link String}, square brackets are added to the edges of the string
     * which may not be wanted.
     *
     * @param string the string to be modified.
     * @param characters the characters to remove.
     * @param trim only remove characters from the edges of the string.
     * @return A string with characters removed.
     */
    public static String remove(final String string, final String characters, final boolean trim) {
        if (string == null) {
            return null;
        }

        String removedString = string;
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

    public static String camelCase(final String string) {
        if (string == null) {
            return null;
        }

        final StringBuilder camelCaseString = new StringBuilder();
        boolean whitespaceDetected = false;
        for (int i = 0; i < string.length(); i++) {
            final String character = string.substring(i, i + 1);
            if (i == 0 || whitespaceDetected) {
                whitespaceDetected = false;
                camelCaseString.append(character.toUpperCase());
            } else {
                if (character.matches("\\s")) {
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
    public static List<String> splitLabelsWithEscapeCharacters(final String labelsString, Set<Character> toSplitOn) {

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
        StringBuilder quotedDelimitedString = new StringBuilder();
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

    public static List<String> unquoteAndSplitString(final String string, final char delimiter) {
        return unquoteAndSplitString(string, delimiter, '"', '\\');
    }

    public static List<String> unquoteAndSplitString(String string, final char delimiter, final char quote, final char escapeCharacter) {
        if (string == null) {
            return null;
        }
        final List<String> strings = new ArrayList<>();
        while (!string.isEmpty()) {
            int delimiterIndex = -1;
            do {
                delimiterIndex = string.indexOf(delimiter, delimiterIndex + 1);
            } while (delimiterIndex > 0 && string.charAt(delimiterIndex - 1) != quote);
            if (delimiterIndex == -1) {
                throw new IllegalArgumentException("No unescaped delimiters found in remaining portion of string: " + string);
            }
            if (delimiterIndex == 0) {
                strings.add(null);
            } else {
                strings.add(unescapeString(string.substring(1, delimiterIndex - 1), new char[]{delimiter, quote}, escapeCharacter));
            }
            string = string.substring(delimiterIndex + 1);
        }
        return strings;
    }

    /**
     * Returns the supplied string with all characters in the supplied list
     * escaped with backslashes.
     *
     * @param string The String to escape.
     * @param metaCharacters An array of characters to be escaped
     * @return the supplied string with all characters in the supplied list
     * escaped with backslashes.
     */
    public static String escapeString(String string, char[] metaCharacters) {
        return escapeString(string, metaCharacters, '\\');
    }

    public static String escapeString(String string, char[] metaCharacters, char escapeCharacter) {
        if (string == null) {
            return null;
        }
        final String escape = String.valueOf(escapeCharacter);
        final String escapedEscape = escape + escape;
        string = string.replace(escape, escapedEscape);
        for (char c : metaCharacters) {
            string = string.replace(Character.toString(c), escape + Character.toString(c));
        }
        return string;
    }

    /**
     * Returns the supplied string with all characters in the supplied list
     * unescaped with backslashes.
     *
     * @param string The string to unescape
     * @param metaCharacters An array of characters to be unescaped
     * @return the supplied string with all characters in the supplied list
     * unescaped with backslashes.
     */
    public static String unescapeString(String string, char[] metaCharacters) {
        return unescapeString(string, metaCharacters, '\\');
    }

    public static String unescapeString(String string, char[] metaCharacters, char escapeCharacter) {
        if (string == null) {
            return null;
        }
        final String escape = String.valueOf(escapeCharacter);
        final String escapedEscape = escape + escape;
        string = string.replace(escapedEscape, escape);
        for (char c : metaCharacters) {
            string = string.replace(escape + Character.toString(c), Character.toString(c));
        }
        return string;
    }

    /**
     * Remove the outer [ ] if either exist. When you convert a
     * {@link java.util.Collection} to a {@link String}, square brackets are
     * added to the string.
     *
     * @param text the string to be modified.
     * @return A {@link String} with outer square brackets removed
     */
    public static String removeSquareBracketsFromString(final String text) {
        if (text.startsWith("[") || text.endsWith("]")) {
            return text.replaceAll("\\[", "").replaceFirst("]$", "");
        } else {
            return text;
        }
    }
}
