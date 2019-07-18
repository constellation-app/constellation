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
package au.gov.asd.tac.constellation.functionality.intro;

import au.gov.asd.tac.constellation.functionality.tutorial.TutorialTopComponent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.util.Exceptions;
import org.apache.commons.lang3.StringUtils;
/**
 *
 * @author imranraza83
 */
public abstract class IntroProvider {
     /**
     * Get the path of a text file (relative to the implementing class) that
     * contains HTML Content.
     * <p>
     * The file will be opened using {@link java.lang.Class#getResource(java.lang.String)
     * }.
     *
     * @return A {@link String} representing the path of a "HTML Content" text
     * file.
     */
    public abstract String getResource();

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
    public static String getHtmlContent(final Class<?> cls, final String resourceName) {
        try {
            try (final BufferedReader r = new BufferedReader(new InputStreamReader(cls.getResourceAsStream(resourceName), StandardCharsets.UTF_8.name()))) {
                return parse(r);
            }
        } catch (final IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return StringUtils.EMPTY;
    }

    private static String parse(final BufferedReader reader) throws IOException
    {
        final StringBuilder text = new StringBuilder();
        while(true)
        {
            final String line = reader.readLine();
             if (line == null) {
                    break;
                }
            text.append(line);
            text.append("\n");
        }
        return text.toString();
    }
}
