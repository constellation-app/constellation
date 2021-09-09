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
package au.gov.asd.tac.constellation.help.utilities.toc;

import com.jogamp.common.os.Platform;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.List;
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
            toc.createNewFile();
            LOGGER.log(Level.FINE, "Table of Contents file was created at: {0}", filePath);

        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, "Unable to create table of contents file", ex);
        }
    }

    /**
     * Writes the converted XML mappings to the toc file
     *
     * @param xmlFromFile File of XML mappings
     */
    public void convertXMLMappings(final List<File> xmlsFromFile, final TreeNode root) {
        final FileWriter writer;
        try {
            writer = new FileWriter(toc);
            convertXMLMappings(xmlsFromFile, writer, root);
            writer.close();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to write mappings to file", ex);
        }
    }

    /**
     * Generate a table of contents from the XML mapping file
     */
    public static void convertXMLMappings(final List<File> xmlsFromFile, final FileWriter markdownOutput, final TreeNode root) {
        writeText(markdownOutput, "<div class=\"container\">");
        writeText(markdownOutput, "<div class='row'> <div class=\"card-body btn btn-link sidebar-sticky accordion-item col-4 col-sm-4\" id=\"accordion\">");
        writeText(markdownOutput, Platform.NEWLINE);

        // Parse XML to tree structure
        xmlsFromFile.forEach(file -> TOCParser.parse(file, root));

        // Write tree structure to the output
        TreeNode.writeTree(root, markdownOutput, 0);
        writeText(markdownOutput, Platform.NEWLINE);
        writeText(markdownOutput, "</div>\n</div>\n</div>");

    }

    /**
     * Generate a markdown style link from a title and a url
     *
     * @param title the @String title to include as the links title
     * @param url the url to link to
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
     * Generate a HTML style link from a title and a url
     *
     * @param title the @String title to include as the links title
     * @param url the url to link to
     */
    public static String generateHTMLLink(final String title, final String url) {
        final StringBuilder sb = new StringBuilder();
        sb.append("<a class=\"accordion-item link-primary nav flex-column\" aria-expanded=\"true\" href =\"");
        sb.append(url);
        sb.append("\">");
        sb.append(title);
        sb.append("</a>");
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
        final int spacesPerIndent = 2;
        try {
            final String indent = StringUtils.repeat(BLANK_STRING, indentLevel * spacesPerIndent);
            final StringBuilder sb = new StringBuilder();
            sb.append(indent);
            sb.append(item);
            writer.write(sb.toString());
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to write to file", ex);
        }
    }

    /**
     * Writes a String item using the given writer for an accordion
     *
     * @param writer the FileWriter to use when writing
     * @param item the String to write
     * @param dataTarget the target to write on the item
     */
    public static void writeAccordionItem(final FileWriter writer, final String item, final String dataTarget) {
        try {
            //final String indent = StringUtils.repeat(BLANK_STRING, indentLevel * spacesPerIndent);
            final StringBuilder sb = new StringBuilder();
            sb.append("<h2 text-align=\"left\" class=\"mb-0 nav flex-column\">");
            sb.append("<a href=\"#\" role=\"button\" class=\"link-secondary\" data-toggle=\"collapse\" data-target=\"#");//collapseOne\" aria-controls=\"collapseOne\">");
            sb.append(dataTarget.replace(StringUtils.SPACE, StringUtils.EMPTY).replace("/", ""));
            sb.append("\" aria-controls=\"");
            sb.append(dataTarget.replace(StringUtils.SPACE, StringUtils.EMPTY).replace("/", ""));
            sb.append("\">");
            //sb.append("<a href=\"#\" data-toggle=\"collapse\" data-target=\"#");
            //sb.append(checkedTarget);
            //sb.append("\">");
            sb.append(item);
            sb.append("</a>");
            sb.append("</h2>");
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
