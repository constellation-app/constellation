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
package au.gov.asd.tac.constellation.utilities.lucene;

/**
 * Lucene Utilities
 *
 * @author arcturus
 */
public class LuceneUtilities {

    /**
     * The special characters that need to be escaped in a Lucene query.
     */
    private static final String LUCENE_SPECIALS = "/+-&|!(){}[]^\"~*?:\\";

    private LuceneUtilities() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Escape a string so it becomes a legal non-wildcarded Lucene search value.
     *
     * @param text A string.
     * @return An escaped string value that is acceptable to Lucene. If a null
     * String was passed then null will be returned.
     */
    public static String escapeLucene(final String text) {
        if (text == null) {
            return null;
        }

        final StringBuilder buf = new StringBuilder(text.length() * 2);
        for (int i = 0; i < text.length(); i++) {
            final char ch = text.charAt(i);
            if (LUCENE_SPECIALS.indexOf(ch) != -1) {
                buf.append("\\");
            }
            buf.append(ch);
        }
        return buf.toString();
    }

}
