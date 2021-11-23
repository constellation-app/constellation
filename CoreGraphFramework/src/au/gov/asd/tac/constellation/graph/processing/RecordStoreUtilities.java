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
package au.gov.asd.tac.constellation.graph.processing;

import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * A collection of utilities to perform operations on {@link RecordStore}
 * objects.
 *
 * @author cygnus_x-1
 */
public class RecordStoreUtilities {

    private static final Logger LOGGER = Logger.getLogger(RecordStoreUtilities.class.getName());
    
    private RecordStoreUtilities() {
        throw new IllegalStateException("Utility class");
    }

    private static String[] parseTsvRow(final String line) {
        final String[] fields = line.split(SeparatorConstants.TAB, -1);
        for (int f = 0; f < fields.length; f++) {
            final String field = fields[f];
            if (field.isEmpty()) {
                fields[f] = null;
            } else {
                final StringBuilder result = new StringBuilder();
                boolean escape = false;
                for (int i = 0; i < field.length(); i++) {
                    char c = field.charAt(i);
                    if (escape) {
                        switch (c) {
                            case 't':
                                result.append('\t');
                                break;
                            case 'n':
                                result.append('\n');
                                break;
                            case 'r':
                                result.append('\r');
                                break;
                            default:
                                result.append(c);
                                break;
                        }
                    } else if (c == '\\') {
                        escape = true;
                    } else {
                        result.append(c);
                    }
                }
                if (escape) {
                    result.append('\\');
                }
                fields[f] = result.toString();
            }
        }
        return fields;
    }

    /**
     * Convert a JSON {@link InputStream} into a {@link RecordStore}.
     * <p>
     * The specified JSON stream should contain an array of objects, with each
     * object being converted to a record in the resulting {@link RecordStore}.
     * Each field in each object is converted to an entry in the corresponding
     * record.
     *
     * @param in An {@link InputStream} through which the JSON document will be
     * transported.
     * @return A {@link RecordStore} derived from the JSON document.
     * @throws IOException If something goes wrong while reading the JSON
     * document.
     */
    public static RecordStore fromJson(final InputStream in) throws IOException {
        final RecordStore recordStore;
        try (final JsonParser parser = new MappingJsonFactory().createParser(in)) {
            recordStore = new GraphRecordStore();
            JsonToken currentToken = parser.nextToken();
            if (currentToken != JsonToken.START_ARRAY) {
                return null;
            }
            while (true) {
                currentToken = parser.nextToken();
                if (currentToken == JsonToken.START_OBJECT) {
                    recordStore.add();

                    while (true) {
                        currentToken = parser.nextToken();
                        if (currentToken == JsonToken.FIELD_NAME) {
                            String fieldName = parser.getCurrentName();

                            String fieldValue;
                            currentToken = parser.nextToken();
                            if (currentToken == JsonToken.VALUE_STRING || currentToken == JsonToken.VALUE_NUMBER_INT || currentToken == JsonToken.VALUE_NUMBER_FLOAT) {
                                fieldValue = parser.getValueAsString();
                            } else {
                                return null;
                            }

                            recordStore.set(fieldName, fieldValue);

                        } else if (currentToken == JsonToken.END_OBJECT) {
                            break;
                        } else {
                            return null;
                        }
                    }
                } else if (currentToken == JsonToken.END_ARRAY) {
                    break;
                } else {
                    return null;
                }
            }
        }

        return recordStore;
    }

    /**
     * Convert a {@link RecordStore} into a JSON document.
     * <p>
     * The JSON document consist of an array of objects, with each object
     * representing a record in the specified {@link RecordStore}. Each field in
     * each object is corresponds to an entry in the corresponding record of the
     * {@link RecordStore}.
     *
     * @param recordStore The {@link RecordStore} you wish to parse into a JSON
     * document.
     * @return A {@link String} representing a JSON document derived from the
     * specified {@link RecordStore}.
     * @throws IOException If something goes wrong while writing to the JSON
     * document.
     */
    public static String toJson(final RecordStore recordStore) throws IOException {
        final String json;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try (final JsonGenerator jg = new JsonFactory().createGenerator(outputStream)) {
                jg.writeStartArray();

                if (recordStore != null && recordStore.size() > 0) {
                    recordStore.reset();
                    while (recordStore.next()) {
                        jg.writeStartObject();
                        for (String key : recordStore.keys()) {
                            final String value = recordStore.get(key);
                            if (value == null) {
                                continue;
                            }
                            jg.writeStringField(key, value);
                        }
                        jg.writeEndObject();
                    }
                }
                jg.writeEndArray();
            }
            json = outputStream.toString(StandardCharsets.UTF_8.name());
        }
        return json;
    }

    /**
     * Loads a serialized {@link RecordStore} from an {@link InputStream}.
     *
     * @param in An {@link InputStream} pointing to a serialized
     * {@link RecordStore}.
     * @return The {@link RecordStore} object as loaded from the stream.
     * @throws IOException If there is an issue reading from the stream.
     */
    public static RecordStore fromTsv(final InputStream in) throws IOException {
        final RecordStore recordStore = new GraphRecordStore();
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8.name()))) {
            String line = reader.readLine();
            final String[] headers = parseTsvRow(line);

            line = reader.readLine();
            while (line != null) {
                final String[] values = parseTsvRow(line);

                recordStore.add();
                final int fieldCount = Math.min(headers.length, values.length);
                for (int f = 0; f < fieldCount; f++) {
                    if (values[f] != null) {
                        recordStore.set(headers[f], values[f]);
                    }
                }

                line = reader.readLine();
            }
        }

        return recordStore;
    }

    /**
     * Loads a serialized {@link RecordStore} from an {@link InputStream}. The
     * first row will be assumed to be the heading.
     *
     * @param in An {@link InputStream} pointing to a serialized
     * {@link RecordStore}.
     * @return The {@link RecordStore} object as loaded from the stream.
     * @throws IOException If there is an issue reading from the stream.
     */
    public static RecordStore fromCsv(final InputStream in) throws IOException {
        final RecordStore recordStore = new GraphRecordStore();

        try (final CSVParser csvFileParser = CSVFormat.DEFAULT.parse(new InputStreamReader(in, StandardCharsets.UTF_8.name()))) {
            final List<CSVRecord> recs = csvFileParser.getRecords();
            for (int i = 1; i < recs.size(); i++) {
                recordStore.add();
                for (int j = 0; j < recs.get(i).size(); j++) {
                    recordStore.set(recs.get(0).get(j), recs.get(i).get(j));
                }
            }
        }

        return recordStore;
    }

    /**
     * Convert a {@link RecordStore} into a comma-separated value table.
     *
     * The result is defined as follows:
     *
     * Write out a comma separated list of the column names, followed by a
     * newline. If there are no columns, a single newline will be written to
     * represent the header.
     *
     * For each row, write out a comma separated list of that rows values. If
     * the row is empty, a single newline will be written for that row.
     *
     * An empty record store, therefore, will result in a single newline
     * character, whilst a record store with no columns and 'N' lines will
     * result in the string of 'N+1' newline characters.
     *
     * @param recordStore The {@link RecordStore} object you wish to convert to
     * CSV.
     * @param outputStream The {@link OutputStream} to write to
     */
    public static void toCsv(final RecordStore recordStore, final OutputStream outputStream) {
        final StringBuilder line = new StringBuilder();
        boolean columnsWritten = false;

        recordStore.reset();
        while (recordStore.next()) {
            // write the heading
            if (!columnsWritten) {
                line.setLength(0);
                for (final String key : recordStore.keys()) {
                    final String columnValue = key == null ? "" : key.replaceAll("(\n|\")", "");
                    if (columnValue.contains(",")) {
                        line.append("\"");
                        line.append(columnValue);
                        line.append("\"");
                    } else {
                        line.append(columnValue);
                    }
                    line.append(",");
                }
                line.setLength(line.length() > 0 ? line.length() - 1 : 0);
                line.append(SeparatorConstants.NEWLINE);
                columnsWritten = true;

                try {
                    outputStream.write(line.toString().getBytes(StandardCharsets.UTF_8.name()));
                } catch (IOException ex) {
                    LOGGER.severe(ex.getLocalizedMessage());
                }
            }

            // write a row
            line.setLength(0);
            for (final String key : recordStore.keys()) {
                final String value = recordStore.get(key) == null ? "" : recordStore.get(key).replaceAll("(\n|\")", "");
                if (value.contains(",")) {
                    line.append("\"");
                    line.append(value);
                    line.append("\"");
                } else {
                    line.append(value);
                }
                line.append(",");
            }
            line.setLength(line.length() > 0 ? line.length() - 1 : 0);
            line.append(SeparatorConstants.NEWLINE);

            try {
                outputStream.write(line.toString().getBytes(StandardCharsets.UTF_8.name()));
            } catch (IOException ex) {
                LOGGER.severe(ex.getLocalizedMessage());
            }
        }
    }
}
