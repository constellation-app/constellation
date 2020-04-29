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
package au.gov.asd.tac.constellation.utilities.csv;

import java.io.IOException;
import java.io.Writer;

/**
 * A CSVWRiter that is designed to take the place of the OpenCSV CSVWriter
 * class. The difference is that this writer does not place quotes around all
 * fields. It only uses quotes if the field contains a character that needs to
 * be escaped.
 *
 * @author sirius
 */
public class SmartCSVWriter implements AutoCloseable {

    private final Writer out;
    private final char separator;
    private final boolean escapeAlways;

    /**
     * Create a new SmarterCSVWriter that uses a comma as a separator and only
     * surrounds fields with quotes if they contain an escaped character.
     *
     * @param out the Writer thar will receive the output.
     */
    public SmartCSVWriter(Writer out) {
        this(out, ',', false);
    }

    /**
     * Create a new SmarterCSVWriter that uses a comma as a separator.
     *
     * @param out the Writer that will receive the output.
     * @param escapeAlways should the writer surround all fields with quotes or
     * just those that contain escaped characters.
     */
    public SmartCSVWriter(Writer out, boolean escapeAlways) {
        this(out, ',', escapeAlways);
    }

    /**
     * Create a new SmarterCSVWriter that only surrounds fields with quotes if
     * they contain an escaped character.
     *
     * @param out the Writer thar will receive the output.
     * @param separator the separator between fields.
     */
    public SmartCSVWriter(Writer out, char separator) {
        this(out, separator, false);
    }

    /**
     * Create a new SmarterCSVWriter.
     *
     * @param out the Writer thar will receive the output.
     * @param separator the separator between fields.
     * @param escapeAlways should the writer surround all fields with quotes or
     * just those that contain escaped characters.
     */
    public SmartCSVWriter(Writer out, char separator, boolean escapeAlways) {
        this.out = out;
        this.separator = separator;
        this.escapeAlways = escapeAlways;
    }

    /**
     * Output a new row.
     *
     * @param row the fields of the new row.
     * @throws IOException if the underlying Writer throws an IOException.
     */
    public void writeNext(String[] row) throws IOException {

        boolean needsDelimiter = false;
        for (String field : row) {

            if (needsDelimiter) {
                out.write(separator);
            } else {
                needsDelimiter = true;
            }

            if (field != null) {
                if (escapeAlways || field.indexOf('"') >= 0 || field.indexOf(separator) >= 0 || field.indexOf('\n') >= 0 || field.indexOf('\r') >= 0) {
                    out.write('"');
                    out.write(field.replaceAll("\"", "\"\""));
                    out.write('"');
                } else {
                    out.write(field);
                }
            }
        }

        out.write('\n');
    }

    @Override
    public void close() throws IOException {
        out.close();
    }
}
