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
package au.gov.asd.tac.constellation.plugins.importexport.hashmod;

import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.openide.NotifyDescriptor;

/**
 * A HashmodCSVImportFileParser implements an ImportFileParser that can parse
 * CSV files.
 *
 * @author CrucisGamma
 */
public class HashmodCSVImportFileParser {

    protected CSVParser getCSVParser(final HashmodInputSource input) throws IOException {
        return CSVFormat.RFC4180.parse(new InputStreamReader(input.getInputStream(), StandardCharsets.UTF_8.name()));
    }

    public List<String[]> parse(final HashmodInputSource input) throws IOException {
        final ArrayList<String[]> results = new ArrayList<>();

        try (final CSVParser csvFileParser = getCSVParser(input)) {
            for (final CSVRecord csvRecord : csvFileParser) {
                final String[] line = new String[csvRecord.size()];

                for (int i = 0; i < csvRecord.size(); i++) {
                    line[i] = csvRecord.get(i);
                }

                results.add(line);
            }
        } catch (Exception ex) {
            NotifyDisplayer.display(String.format("Error detected for file %s",
                    input.getFile().getPath()), NotifyDescriptor.ERROR_MESSAGE);
        }

        return results;
    }

    public List<String[]> preview(final HashmodInputSource input, final int limit) throws IOException {
        // Leave the header on, as the importer expects this as the first entry.
        final ArrayList<String[]> results = new ArrayList<>();

        try (final CSVParser csvFileParser = getCSVParser(input)) {
            int count = 0;

            for (final CSVRecord csvRecord : csvFileParser) {
                final String[] line = new String[csvRecord.size()];

                for (int i = 0; i < csvRecord.size(); i++) {
                    line[i] = csvRecord.get(i);
                }

                results.add(line);
                count++;

                if (count >= limit) {
                    return results;
                }
            }
        }

        return results;
    }
}
