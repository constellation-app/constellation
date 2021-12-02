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
package au.gov.asd.tac.constellation.plugins.importexport.delimited.parser;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.utilities.gui.filechooser.FileChooser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.openide.util.lookup.ServiceProvider;

/**
 * A CSVImportFileParser implements an ImportFileParser that can parse CSV
 * files.
 *
 * @author sirius
 */
@ServiceProvider(service = ImportFileParser.class)
public class CSVImportFileParser extends ImportFileParser {

    public CSVImportFileParser() {
        super("CSV", 0);
    }

    protected CSVParser getCSVParser(final InputSource input) throws IOException {
        return CSVFormat.RFC4180.parse(new InputStreamReader(input.getInputStream(), StandardCharsets.UTF_8.name()));
    }

    @Override
    public List<String[]> parse(final InputSource input, final PluginParameters parameters) throws IOException {
        final ArrayList<String[]> results = new ArrayList<>();
        try (final CSVParser csvFileParser = getCSVParser(input)) {
            for (final CSVRecord record : csvFileParser) {
                final String[] line = new String[record.size()];
                for (int i = 0; i < record.size(); i++) {
                    line[i] = record.get(i);
                }
                results.add(line);
            }
        }
        return results;
    }

    @Override
    public List<String[]> preview(final InputSource input, final PluginParameters parameters, final int limit) throws IOException {
        // Leave the header on, as the importer expects this as the first entry.
        final ArrayList<String[]> results = new ArrayList<>();
        try (final CSVParser csvFileParser = getCSVParser(input)) {
            int count = 0;
            for (final CSVRecord record : csvFileParser) {
                final String[] line = new String[record.size()];
                for (int i = 0; i < record.size(); i++) {
                    line[i] = record.get(i);
                }
                results.add(line);
                if (++count >= limit) {
                    return results;
                }
            }
        }
        return results;
    }

    @Override
    public FileNameExtensionFilter getExtensionFilter() {
        return FileChooser.CSV_FILE_FILTER;
    }
}
