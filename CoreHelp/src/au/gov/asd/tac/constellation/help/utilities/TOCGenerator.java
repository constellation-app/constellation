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

import com.jogamp.common.os.Platform;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
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
    private static final String OPEN_RECTANGLE_BRACE = "[";
    private static final String CLOSE_RECTANGLE_BRACE = "]";
    private static final String OPEN_PARENTHESIS = "(";
    private static final String CLOSE_PARENTHESIS = ")";
    private static final String BLANK_STRING = " ";
    private static final String ASTERISK = "*";

    /**
     * Delete old TOC file and generate a new one at the specified path
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
        LOGGER.log(Level.WARNING, "file path is: {0}", toc.getAbsolutePath());
        try {
            toc.createNewFile();
            LOGGER.log(Level.FINE, "Table of Contents file was created at: {0}", filePath);

        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, "Unable to create table of contents file", ex);
        }
    }

    /**
     * // WARNING THIS IS HARDCODED TEST CODE FOR POC // Remove me once done
     *
     * @param xmlFromFile File of XML mappings
     */
    public void convertXMLMappingsTEST(final File tocLocation) {
        final FileWriter writer;
        try {
            writer = new FileWriter(toc);
            convertXMLMappingsHARDCODED(tocLocation, writer);
            writer.close();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to write mappings to file", ex);
        }
    }

    /**
     * Writes the converted XML mappings to the toc file
     *
     * @param xmlFromFile File of XML mappings
     */
    public void convertXMLMappings(final File xmlFromFile) {
        final FileWriter writer;
        try {
            writer = new FileWriter(toc);
            convertXMLMappings(xmlFromFile, writer);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to write mappings to file", ex);
        }
    }

    /**
     * Generate a table of contents from the XML mapping file
     */
    public static void convertXMLMappings(final File xmlFromFile, final FileWriter markdownOutput) {
        //TOCParser.parse(xmlFromFile);

        // TODO: Implement this
        // open the file
        // parse using xml parser
        // loop each entry
        // if <tocitem> then check if text val exists
        // if exists then set text to parent
        // read more entries and place under child
    }

    /**
     * WARNING THIS IS HARDCODED TEST CODE FOR POC
     *
     * @param markdownWriter
     */
    public static void convertXMLMappingsHARDCODED(final File xmlFromFile, final FileWriter markdownWriter) {
        writeText(markdownWriter, "# Table of Contents");
        writeText(markdownWriter, Platform.NEWLINE);
        writeText(markdownWriter, Platform.NEWLINE);
        writeItem(markdownWriter, "Overview", 0);
        writeText(markdownWriter, Platform.NEWLINE);
        writeItem(markdownWriter, generateLink("About Constellation", ".\\CoreFunctionality\\src\\au\\gov\\asd\\tac\\constellation\\functionality\\docs\\about-constellation.md"), 1);
        writeText(markdownWriter, Platform.NEWLINE);
        writeItem(markdownWriter, generateLink("Getting Started", ".\\CoreFunctionality\\src\\au\\gov\\asd\\tac\\constellation\\functionality\\docs\\getting-started.md"), 1);
        writeText(markdownWriter, Platform.NEWLINE);
        writeItem(markdownWriter, generateLink("The Graph Window", ".\\CoreFunctionality\\src\\au\\gov\\asd\\tac\\constellation\\functionality\\docs\\the-graph-window.md"), 1);
        writeText(markdownWriter, Platform.NEWLINE);
        writeItem(markdownWriter, "Tools", 0);
        writeText(markdownWriter, Platform.NEWLINE);
        writeItem(markdownWriter, "Cluster", 1);
        writeText(markdownWriter, Platform.NEWLINE);
        writeItem(markdownWriter, generateLink("Chinese Whispers", ".\\CoreAlgorithmPlugins\\src\\au\\gov\\asd\\tac\\constellation\\plugins\\algorithms\\docs\\chinese-whispers.md"), 2);
        writeText(markdownWriter, Platform.NEWLINE);
        writeItem(markdownWriter, generateLink("K-Truss", ".\\CoreAlgorithmPlugins\\src\\au\\gov\\asd\\tac\\constellation\\plugins\\algorithms\\docs\\k-truss.md"), 2);
        writeText(markdownWriter, Platform.NEWLINE);
        writeItem(markdownWriter, "Compare Graph", 1);
        writeText(markdownWriter, Platform.NEWLINE);
        writeItem(markdownWriter, "Views", 0);
        writeText(markdownWriter, Platform.NEWLINE);
        writeItem(markdownWriter, "Analytic View", 1);
        writeText(markdownWriter, Platform.NEWLINE);
        writeItem(markdownWriter, "Analytics", 2);
        writeText(markdownWriter, Platform.NEWLINE);
        writeItem(markdownWriter, "Centrality", 3);
        writeText(markdownWriter, Platform.NEWLINE);
        writeItem(markdownWriter, generateLink("Betweenness Centrality", ".\\CoreAnalyticView\\src\\au\\gov\\asd\\tac\\constellation\\views\\analyticview\\docs\\analytic-betweenness-centrality.md"), 4);
        writeText(markdownWriter, Platform.NEWLINE);
        writeItem(markdownWriter, "Global", 3);
        writeText(markdownWriter, Platform.NEWLINE);
        writeItem(markdownWriter, "Questions", 2);
        writeText(markdownWriter, Platform.NEWLINE);
        writeItem(markdownWriter, generateLink("Connects the Network Best?", ".\\CoreAnalyticView\\src\\au\\gov\\asd\\tac\\constellation\\views\\analyticview\\docs\\question-best-connects-network.md"), 3);
        writeText(markdownWriter, Platform.NEWLINE);
        writeItem(markdownWriter, "Layers View", 1);
        writeText(markdownWriter, Platform.NEWLINE);
        writeItem(markdownWriter, generateLink("Layers View", ".\\CoreLayersView\\src\\au\\gov\\asd\\tac\\constellation\\views\\layers\\docs\\layers-view.md"), 2);
        writeText(markdownWriter, Platform.NEWLINE);

    }

    /**
     * Generate a markdown style link from a title and a url
     */
    public static String generateLink(final String title, final String url) {
        final StringBuilder sb = new StringBuilder();
        sb.append(OPEN_RECTANGLE_BRACE);
        sb.append(title);
        sb.append(CLOSE_RECTANGLE_BRACE);
        sb.append(OPEN_PARENTHESIS);
        sb.append(url);
        sb.append(CLOSE_PARENTHESIS);
        return sb.toString();
    }

    /**
     * Writes a String item using the given writer, at the given indentLevel. 0
     * indent will leave no blankspace. Format:
     * <indentLevel>* String item
     *
     * @param writer the FileWriter to use when writing
     * @param item the String to write
     * @param indentLevel the amount of indents to include
     */
    public static void writeItem(final FileWriter writer, final String item, final int indentLevel) {
        final int spacesPerIndent = 4;
        try {
            final String indent = StringUtils.repeat(BLANK_STRING, indentLevel * spacesPerIndent);
            final StringBuilder sb = new StringBuilder();
            sb.append(indent);
            sb.append(ASTERISK);
            sb.append(BLANK_STRING);
            sb.append(item);
            writer.write(sb.toString());
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to write to file", ex);
        }
    }

    /**
     * Write a string to file with no change of formatting.
     *
     * @param writer the FileWriter to use when writing
     * @param item the String to write
     */
    public static void writeText(final FileWriter writer, final String item) {
        try {
            writer.write(item);
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to write to file", ex);
        }
    }

}
