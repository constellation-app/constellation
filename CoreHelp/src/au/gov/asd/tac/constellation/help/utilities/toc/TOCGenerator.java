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
package au.gov.asd.tac.constellation.help.utilities.toc;

import au.gov.asd.tac.constellation.help.utilities.Generator;
import com.jogamp.common.os.Platform;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

/**
 * Generate the table of contents file toc.md for displaying all currently
 * available help files
 *
 * @author aldebaran30701
 */
public class TOCGenerator {

    private static File toc;
    private static final Logger LOGGER = Logger.getLogger(TOCGenerator.class.getName());
    private static final String MARKDOWN_LINK_FORMAT = "[%s](%s)";
    private static final String HTML_LINK_FORMAT = "<a href=\"%s\">%s</a><br/>";
    private static final String BLANK_STRING = " ";
    private static final String EXCEPTION_MESSAGE = "Failed to write to file";

    private TOCGenerator() {
        // Intentionally left blank
    }

    /**
     * Creates a toc file at the given path.
     *
     * @param filePath
     * @return
     */
    public static boolean createTOCFile(final String filePath) {
        boolean success = true;
        if (filePath == null) {
            throw new IllegalArgumentException("Null file path used for creation of Table of Contents file");
        }
        try {
            final boolean wasDeleted = Files.deleteIfExists(FileSystems.getDefault().getPath(filePath));
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
            success = toc.createNewFile();
            LOGGER.log(Level.FINE, "Table of Contents file was created at: {0}", filePath);

        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, "Unable to create table of contents file", ex);
            success = false;
        }
        return success;

    }

    /**
     * Writes the converted XML mappings to the toc file
     *
     * @param xmlFromFile File of XML mappings
     */
    public static void convertXMLMappings(final List<File> xmlsFromFile, final TreeNode root) throws IOException {
        final FileWriter writer;
        writer = new FileWriter(toc);
        convertXMLMappings(xmlsFromFile, writer, root);
        writer.close();
    }

    /**
     * Generate a table of contents from the XML mapping file
     */
    public static void convertXMLMappings(final List<File> xmlsFromFile, final FileWriter markdownOutput, final TreeNode root) {
        writeText(markdownOutput, String.format("<div class=\"%s\">", "container"));
        writeText(markdownOutput, Platform.getNewline());
        writeText(markdownOutput, String.format("<div id=\"%s\">", "accordion"));
        writeText(markdownOutput, Platform.getNewline());

        // Parse XML to tree structure
        xmlsFromFile.forEach(file -> {
            try {
                TOCParser.parse(file, root);
            } catch (final ParserConfigurationException | SAXException ex) {
                LOGGER.log(Level.WARNING, "Failed to parse Help Contents XML file [{0}] - Help documentation may not be complete.", file == null ? "null" : file.getPath());
            } catch (final IOException ex) {
                LOGGER.log(Level.WARNING, "Failed to find Help Contents XML file [{0}] - Help documentation may not be complete.", file == null ? "null" : file.getPath());
            }
        });

        // Write tree structure to the output
        TreeNode.writeTree(root, markdownOutput, 0);
        writeText(markdownOutput, Platform.getNewline());
        writeText(markdownOutput, "</div>\n</div>\n</div>");
    }

    /**
     * Generate a markdown style link from a title and a url
     *
     * @param title the @String title to include as the links title
     * @param url the url to link to
     */
    public static String generateLink(final String title, final String url) {
        return String.format(MARKDOWN_LINK_FORMAT, title, url);
    }

    /**
     * Generate a HTML style link from a title and a url
     *
     * @param title the @String title to include as the links title
     * @param url the url to link to
     */
    public static String generateHTMLLink(final String title, final String url) {
        return String.format(HTML_LINK_FORMAT, url, title);
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
    public static void writeItem(final Writer writer, final String item, final int indentLevel) {
        final int spacesPerIndent = 2;
        try {
            final String indent = StringUtils.repeat(BLANK_STRING, indentLevel * spacesPerIndent);
            final StringBuilder sb = new StringBuilder();
            sb.append(indent);
            sb.append(item);
            writer.write(sb.toString());
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, EXCEPTION_MESSAGE, ex);
        }
    }

    /**
     * Writes a String item using the given writer for an accordion
     *
     * @param writer the FileWriter to use when writing
     * @param item the String to write
     * @param dataTarget the target to write on the item
     */
    public static void writeAccordionItem(final Writer writer, final String item, final String dataTarget) {
        try {
            final StringBuilder sb = new StringBuilder();
            sb.append("<div class=\"card\">");
            sb.append("<div class=\"card-header\">");
            sb.append("<h2 class=\"mb-0\">");
            if (item.equals(Generator.ROOT_NODE_NAME)) {
                sb.append(item);
            } else {
                sb.append("<button href=\"#\" role=\"button\" class=\"btn btn-link btn-block text-left collapsed\" data-toggle=\"collapse\" data-target=\"#");
                sb.append(dataTarget.replace(StringUtils.SPACE, StringUtils.EMPTY).replace("/", ""));
                sb.append("\" aria-expanded=\"false\" aria-controls=\"");
                sb.append(dataTarget.replace(StringUtils.SPACE, StringUtils.EMPTY).replace("/", ""));
                sb.append("\">");
                sb.append(item);
                sb.append("</button>");
            }
            sb.append("</h2>");
            sb.append("</div>");
            writer.write(sb.toString());
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, EXCEPTION_MESSAGE, ex);
        }
    }

    /**
     * Write a string to file with no change of formatting.
     *
     * @param writer the FileWriter to use when writing
     * @param item the String to write
     */
    public static void writeText(final Writer writer, final String item) {
        try {
            writer.write(item);
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, EXCEPTION_MESSAGE, ex);
        }
    }

}
