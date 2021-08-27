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
package au.gov.asd.tac.constellation.help.utilities;

import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

/**
 * Generate the table of contents file toc.md for displaying all currently
 * available help files
 *
 * @author aldebaran30701
 */
public class TOCGenerator {

    private final File toc;
    private static final Logger LOGGER = Logger.getLogger(TOCGenerator.class.getName());

    /**
     * Delete old TOC file and generate a new one
     *
     * @param filePath
     */
    public TOCGenerator(final String filePath) {
        if (filePath == null) {
            throw new IllegalArgumentException("Null file path used for creation of Table of Contents file");
        }
        try {
            boolean wasDeleted = Files.deleteIfExists(FileSystems.getDefault().getPath(filePath));
            if (wasDeleted) {
                LOGGER.log(Level.FINE, "Previous Table of Contents file was replaced at: {0}", filePath);
            }
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, "Path to Table of Contents file was invalid.", ex);
        }

        // initialise file with path
        toc = new File(filePath);
        try {
            toc.createNewFile();
            LOGGER.log(Level.FINE, "Table of Contents file was created at: {0}", filePath);

        } catch (final IOException ex) {
            Logger.getLogger(TOCGenerator.class.getName()).log(Level.SEVERE, "Unable to create table of contents file", ex);
        }
    }

    /**
     * Generate a table of contents from the mapping file
     */
    public static void generateTableOfContents(final Map<String, String> items) {

    }

    public void addItem(final FileWriter writer, final String item, final int indentLevel) {
        try {
            final String indent = StringUtils.repeat(SeparatorConstants.WHITESPACE, indentLevel);
            final StringBuilder sb = new StringBuilder();
            sb.append(indent);
            //sb.append(SeparatorConstants.);
            writer.write(item);
        } catch (final IOException ex) {
            Logger.getLogger(TOCGenerator.class.getName()).log(Level.SEVERE, "Failed to write to file", ex);
        }
    }

}
