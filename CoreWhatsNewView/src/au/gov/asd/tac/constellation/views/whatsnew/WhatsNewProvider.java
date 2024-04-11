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
package au.gov.asd.tac.constellation.views.whatsnew;

import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A WhatsNewProvider handles parsing text files into the "What's New" section
 * on the {@link WhatsNewTopComponent} which acts as CONSTELLATION's welcome
 * screen. This gives you the opportunity to provide messages to any users of
 * CONSTELLATION for viewing immediately after launching the application.
 *
 * @author algol
 */
public abstract class WhatsNewProvider {
    
    private static final Logger LOGGER = Logger.getLogger(WhatsNewProvider.class.getName());

    /**
     * Get the path of a text file (relative to the implementing class) that
     * contains "What's New" text.
     * <p>
     * The file will be opened using {@link java.lang.Class#getResource(java.lang.String)
     * }.
     *
     * @return A {@link String} representing the path of a "What's New" text
     * file.
     */
    public abstract String getResource();

    /**
     * Get the section this provider belongs to. Sections can be defined however
     * you wish.
     *
     * @return A {@link String} representing a section.
     */
    public abstract String getSection();

    /**
     * Parse a given "what's new" text file into a list of {@link WhatsNewEntry}
     * objects.
     * <p>
     * This file should be of the form:
     * <pre>
     * == yyyy-mm-dd Header Text 1
     * Message 1.
     * == yyyy-mm-dd Header Text 2
     * Message 2.
     * ...
     * </pre>
     *
     * @param cls A {@link Class} object from which to call
     * {@link Class#getResourceAsStream} on the given resource name.
     * @param resourceName A {@link String} representing the name of the
     * resource to load as a {@link WhatsNewEntry}.
     * @param section A {@link String} representing the section to place these
     * "what's new" entries.
     * @return A {@link List} of {@link WhatsNewEntry} objects.
     */
    public static List<WhatsNewEntry> getWhatsNew(final Class<?> cls, final String resourceName, final String section) {
        try {
            try (final BufferedReader r = new BufferedReader(new InputStreamReader(cls.getResourceAsStream(resourceName), StandardCharsets.UTF_8.name()))) {
                return parse(r, section);
            }
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }

        return Collections.emptyList();
    }

    /**
     * Parse a given "What's New" text file into a list of {@link WhatsNewEntry}
     * objects.
     * <p>
     * The file is of the form:
     * <pre>
     * == yyyy-mm-dd Header Text 1
     * Message 1.
     * == yyyy-mm-dd Header Text 2
     * Message 2.
     * ...
     * </pre>
     *
     * @param reader A {@link BufferedReader} providing text to be parsed.
     * @return A {@link List} of {@link WhatsNewEntry} objects.
     * @throws IOException If something goes wrong while reading the text.
     */
    private static List<WhatsNewEntry> parse(final BufferedReader reader, final String section) throws IOException {
        final Pattern wneHeader = Pattern.compile("^==\\s+(\\d{4}-\\d{2}-\\d{2})\\s+(.*)");

        final ArrayList<WhatsNewEntry> wnes = new ArrayList<>();

        String date = null;
        String header = null;
        final StringBuilder text = new StringBuilder();

        while (true) {
            final String line = reader.readLine();
            if (line == null || line.startsWith("==")) {
                if (date != null) {
                    wnes.add(new WhatsNewEntry(date, header, section, text.toString()));
                    date = null;
                    header = null;
                    text.setLength(0);
                }

                if (line == null) {
                    break;
                }

                final Matcher m = wneHeader.matcher(line);
                if (m.matches()) {
                    date = m.group(1);
                    header = m.group(2);
                } else {
                    wnes.add(new WhatsNewEntry("Bad \"what's new\" text:", "[" + line + "]", "", "Bad " + section));
                }
            } else {
                text.append(line);
                text.append(SeparatorConstants.NEWLINE);
            }
        }

        return wnes;
    }

    /**
     * A container class for storing information for a single entry in the
     * "What's New" pane.
     */
    public static class WhatsNewEntry implements Comparable<WhatsNewEntry> {

        public final String date;
        public final String header;
        public final String text;
        public final String section;

        /**
         * Construct a WhatsNewEntry.
         * <p>
         * The date must be in the form "yyyy-mm-dd", an HTML fragment.
         * <p>
         * Entries will be displayed in descending datetime order. Dates with
         * year 3000 and later will not be displayed with a date, but rather
         * will be 'sticky' posts (ie. stuck to the top of the "What's New"
         * list). Any 'sticky' posts will be sorted amongst themselves according
         * to their datetime values.
         *
         * @param date A {@link String} representing a date in the form
         * "yyyy-mm-dd".
         * @param header A {@link String} representing a the headeing of the
         * message.
         * @param text A {@link String} representing the body of the message,
         * formated as a HTML fragment (suitable for inserting into
         * &lt;body&gt;).
         * @param section A {@link String} representing the section that this
         * entry belongs to.
         */
        public WhatsNewEntry(final String date, final String header, final String section, final String text) {
            this.date = date;
            this.header = header;
            this.section = section;
            this.text = text;
        }

        @Override
        public String toString() {
            return String.format("[%n%s %s %s%n%s%n]", date, header, section, text);
        }

        @Override
        public int compareTo(final WhatsNewEntry other) {
            return other.date.compareTo(date);
        }
    }
}
