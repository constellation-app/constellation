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
import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 * A TSVImportFileParser implements an ImportFileParser that can parse TSV
 * files.
 *
 * @author sirius
 */
@ServiceProvider(service = ImportFileParser.class)
public class TSVImportFileParser extends ImportFileParser {

    public TSVImportFileParser() {
        super("TSV", 1);
    }

    @Override
    public List<String[]> parse(final InputSource input, final PluginParameters parameters) throws IOException {
        final List<String[]> result = new ArrayList<>();
        try ( InputStream in = input.getInputStream()) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8.name()));

            String line = reader.readLine();
            while (line != null) {
                result.add(line.split(SeparatorConstants.TAB, -1));
                line = reader.readLine();
            }
        }

        return result;
    }

    @Override
    public List<String[]> preview(final InputSource input, final PluginParameters parameters, final int limit) throws IOException {
        final List<String[]> result = new ArrayList<>();
        try ( InputStream in = input.getInputStream()) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8.name()));

            int currentRow = 0;
            String line = reader.readLine();
            while ((line != null) && (currentRow < limit)) {
                result.add(line.split(SeparatorConstants.TAB, -1));
                line = reader.readLine();
                currentRow++;
            }
        }

        return result;
    }

    /**
     * Returns the file filter to use when browsing for files of this type.
     *
     * @return the file filter to use when browsing for files of this type.
     */
    @Override
    public FileFilter getFileFilter() {
        return new FileFilter() {
            @Override
            public boolean accept(final File file) {
                final String name = file.getName();
                return (file.isFile() && StringUtils.endsWithIgnoreCase(name, FileExtensionConstants.TAB_SEPARATED_VALUE)) || file.isDirectory();
            }

            @Override
            public String getDescription() {
                return "TSV Files (" + FileExtensionConstants.TAB_SEPARATED_VALUE + ")";
            }
        };
    }
}
