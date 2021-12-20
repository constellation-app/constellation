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
package au.gov.asd.tac.constellation.utilities.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * utility class for parsing XML documents.
 *
 * @author cygnus_x-1
 * @author twinkle2_little
 * @author sirius
 */
public class XmlUtilities {

    private final DocumentBuilderFactory documentBuilderFactory;
    private final TransformerFactory transformerFactory;
    private DocumentBuilder documentBuilder;
    private Transformer transformer;

    public XmlUtilities() {
        this(false);
    }

    public XmlUtilities(final boolean namespaceAware) {
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(namespaceAware);
        transformerFactory = TransformerFactory.newInstance();
        try {
            // Some implemntations wont support these settings
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        } catch (IllegalArgumentException ex) {}
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            transformer = transformerFactory.newTransformer();
        } catch (ParserConfigurationException | TransformerConfigurationException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    /**
     * Creates a new empty document to which new elements can be added.
     *
     * @return the new document.
     */
    public Document newDocument() {
        return documentBuilder.newDocument();
    }

    /**
     * Saves an XML document to a byte array.
     *
     * @param document the document.
     * @return a byte array representing the XML encoding of the document.
     * @throws IOException if an error occurs while writing to the bytes.
     * @throws TransformerException if an error occurs while transforming the
     * document into XML.
     */
    public byte[] write(final Document document) throws IOException, TransformerException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        write(document, outputStream);
        return outputStream.toByteArray();
    }

    /**
     * Writes a document as XML text to a file.
     *
     * @param document the document to be written.
     * @param file the document will be written to this file.
     * @throws TransformerException if an error occurs while transforming the
     * document into XML.
     * @throws FileNotFoundException if the given file is not suitable for
     * writing to.
     * @throws IOException if an error occurs while writing to the file.
     */
    public void write(final Document document, final File file) throws IOException, TransformerException {
        write(document, new FileOutputStream(file));
    }

    /**
     * Writes a document as XML text to an output stream.
     *
     * @param document the document to be written.
     * @param outputStream the document will be written to this output stream.
     * @throws IOException if an error occurs while writing to the stream.
     * @throws TransformerException if an error occurs while transforming the
     * document into XML.
     */
    public void write(final Document document, final OutputStream outputStream) throws IOException, TransformerException {
        try (outputStream) {
            final Source source = new DOMSource(document);
            final Result result = new StreamResult(outputStream);
            transformer.transform(source, result);
        }
    }

    /**
     * Saves an XML document to a string.
     *
     * @param document the document.
     * @return a string representing the XML encoding of the document.
     * @throws TransformerException if an error occurs while transforming the
     * document into XML.
     * @throws IOException if an error occurs while writing to the string.
     */
    public String writeToString(final Document document) throws IOException, TransformerException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        write(document, outputStream);
        return new String(outputStream.toByteArray(), StandardCharsets.UTF_8.name());
    }

    /**
     * Convert an XML stream to a byte[], ignoring control characters.
     *
     * @param inputStream the input stream that supplies the XML stream.
     * @param length the number of bytes to read from the stream.
     * @return a string containing the XML with control characters removed.
     * @throws IOException if an error occurs while writing to the stream.
     */
    public String writeToString(final InputStream inputStream, final int length) throws IOException {
        final BadCharFilterInputStream bcfis = new BadCharFilterInputStream(inputStream);
        final byte[] charBuffer = new byte[length];
        bcfis.read(charBuffer, 0, length);
        return new String(charBuffer, StandardCharsets.UTF_8.name());
    }

    /**
     * Reads an XML document from a file.
     *
     * @param file the document will be read from this file.
     * @return the XML document as a Document object.
     * @throws TransformerException if an error occurs while transforming the
     * XML into a document.
     * @throws FileNotFoundException if the given file does not exist.
     */
    public Document read(final File file) throws FileNotFoundException, TransformerException {
        return read(new FileInputStream(file), false);
    }

    /**
     * Reads an XML document from a string.
     *
     * @param data a string containing the XML encoding of the document.
     * @return the document.
     * @throws TransformerException if an error occurs while transforming the
     * XML into a document.
     * @throws UnsupportedEncodingException if the encoding is not supported.
     */
    public Document read(final String data) throws UnsupportedEncodingException, TransformerException {
        return read(data.getBytes(StandardCharsets.UTF_8.name()));
    }

    /**
     * Reads an XML document from a byte array.
     *
     * @param data a byte array representing the XML encoding of a document.
     * @return the document.
     * @throws TransformerException if an error occurs while transforming the
     * XML into a document.
     */
    public Document read(final byte[] data) throws TransformerException {
        return read(new ByteArrayInputStream(data), false);
    }

    /**
     * Reads an XML document from an input stream.
     *
     * @param inputStream the document will be read from this input stream.
     * @param filterControlCharacters filter out characters which cannot be
     * parsed reliably
     * @return the XML document as a Document object.
     * @throws TransformerException if an error occurs while transforming the
     * XML into a document.
     */
    public Document read(final InputStream inputStream, final boolean filterControlCharacters) throws TransformerException {
        final Source source;
        if (filterControlCharacters) {
            final BadCharFilterInputStream bcfInputStream = new BadCharFilterInputStream(inputStream);
            source = new StreamSource(bcfInputStream);
        } else {
            source = new StreamSource(inputStream);
        }
        final Document document = documentBuilder.newDocument();
        final Result result = new DOMResult(document);
        transformer.transform(source, result);
        return document;
    }

    /**
     * Returns the first node with given tagName
     *
     * @param tagName the tag name.
     * @param nodes the nodes to search.
     * @return the first node with given tagName
     */
    public Node getNode(final String tagName, final NodeList nodes) {
        for (int i = 0; i < nodes.getLength(); i++) {
            final Node node = nodes.item(i);
            if (node.getNodeName().equalsIgnoreCase(tagName)) {
                return node;
            }
        }

        return null;
    }

    /**
     * Returns the first node with given tagName
     *
     * @param namespaceURI the namespace.
     * @param localName the tag name.
     * @param nodes the nodes to search.
     * @return the first node with given tagName
     */
    public Node getNodeNS(final String namespaceURI, final String localName, final NodeList nodes) {
        for (int i = 0; i < nodes.getLength(); i++) {
            final Node node = nodes.item(i);
            if (node.getNamespaceURI() != null 
                && node.getNamespaceURI().equalsIgnoreCase(namespaceURI)
                && node.getLocalName().equalsIgnoreCase(localName)) {
                return node;
            }
        }

        return null;
    }

    /**
     * Returns a list of nodes with the given tag name
     *
     * @param tagName the tag name.
     * @param nodes the nodes to search.
     * @return a list of nodes with the given tag name.
     */
    public List<Node> getNodes(final String tagName, final NodeList nodes) {
        final List<Node> requestedNodes = new ArrayList<>();

        for (int i = 0; i < nodes.getLength(); i++) {
            final Node node = nodes.item(i);
            if (node.getNodeName().equalsIgnoreCase(tagName)) {
                requestedNodes.add(node);
            }
        }

        return requestedNodes;
    }

    /**
     * Returns a list of nodes with the given tag name
     *
     * @param namespaceURI the namespace.
     * @param localName the tag name.
     * @param nodes the nodes to search.
     * @return a list of nodes with the given tag name.
     */
    public List<Node> getNodesNS(final String namespaceURI, final String localName, final NodeList nodes) {
        final List<Node> requestedNodes = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            final Node node = nodes.item(i);
            if (node.getNamespaceURI() != null 
                && node.getNamespaceURI().equalsIgnoreCase(namespaceURI)
                && node.getLocalName().equalsIgnoreCase(localName)) {
                requestedNodes.add(node);
            }
        }

        return requestedNodes;
    }

    /**
     * Returns the value of the node as a string
     *
     * @param node the node.
     * @return the value of the node as a string
     */
    public String getNodeValue(final Node node) {
        final NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node data = childNodes.item(i);
            if (data.getNodeType() == Node.TEXT_NODE) {
                return data.getNodeValue();
            }
        }

        return null;
    }

    /**
     * Searches the given list of nodes for a tag with the given name and
     * returns the first TEXT_NODE match as a string, nodes with matching name that
     * are not of type TEXT_NODE are ignored.
     *
     * @param tagName the tag name.
     * @param nodes the nodes to search.
     * @return the first matching node as a string.
     */
    public String getNodeValue(final String tagName, final NodeList nodes) {
        for (int i = 0; i < nodes.getLength(); i++) {
            final Node node = nodes.item(i);
            if (node.getNodeName().equalsIgnoreCase(tagName)) {
                final NodeList childNodes = node.getChildNodes();
                for (int j = 0; j < childNodes.getLength(); j++) {
                    final Node data = childNodes.item(j);
                    if (data.getNodeType() == Node.TEXT_NODE) {
                        return data.getNodeValue();
                    }
                }
            }
        }

        return null;
    }

    /**
     * Searches the given list of nodes for nodes in the given namespaceURI matching
     * the requested tagName. Within any matching nodes return the value of the first
     * node which is of type TEXT_NODE. Returns null if no node matching these conditions
     * is found.
     *
     * @param namespaceURI the namespace.
     * @param tagName the tag name.
     * @param nodes the nodes to search.
     * @return the first matching node as a string.
     */
    public String getNodeValueNS(final String namespaceURI, final String tagName, final NodeList nodes) {
        for (int i = 0; i < nodes.getLength(); i++) {
            final Node node = nodes.item(i);
            if (node.getNamespaceURI() != null
                && node.getNamespaceURI().equalsIgnoreCase(namespaceURI)
                && node.getLocalName().equalsIgnoreCase(tagName)) {
                final NodeList childNodes = node.getChildNodes();
                for (int j = 0; j < childNodes.getLength(); j++) {
                    final Node data = childNodes.item(j);
                    if (data.getNodeType() == Node.TEXT_NODE) {
                        return data.getNodeValue();
                    }
                }
            }
        }

        return null;
    }

    /**
     * Returns the attribute of a given node
     *
     * @param attrName the attribute to retrieve.
     * @param node the node that holds the attributes.
     * @return the attribute of a given node
     */
    public String getNodeAttr(final String attrName, final Node node) {
        final NamedNodeMap attrs = node.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            final Node attr = attrs.item(i);
            if (attr.getNodeName().equalsIgnoreCase(attrName)) {
                return attr.getNodeValue();
            }
        }

        return null;
    }

    /**
     * Returns the attribute of the node that has the provided tag name and attrName.
     *
     * @param tagName the tag name.
     * @param attrName the attribute name.
     * @param nodes the nodes to search for the given tag name / attribute name
     * combination.
     * @return the attribute of the node that has the provided tag name.
     */
    public String getNodeAttr(final String tagName, final String attrName, final NodeList nodes) {
        for (int i = 0; i < nodes.getLength(); i++) {
            final Node node = nodes.item(i);
            if (node.getNodeName().equalsIgnoreCase(tagName)) {
                String result = getNodeAttr(attrName, node);  
                if (result != null) {return result; }
            }
        }
        return null;
    }

    /**
     * Returns the attribute of the node that has the provided tag name and attrName for the supplied namespace.
     *
     * @param namespaceURI the namespace.
     * @param localName the tag name.
     * @param attrName the attribute name.
     * @param nodes the nodes to search for the given tag name / attribute name
     * combination.
     * @return the attribute of the node that has the provided tag name.
     */
    public String getNodeAttrNS(final String namespaceURI, final String localName, final String attrName, final NodeList nodes) {
        for (int i = 0; i < nodes.getLength(); i++) {
            final Node node = nodes.item(i);
            if (node.getNamespaceURI() != null
                && node.getNamespaceURI().equalsIgnoreCase(namespaceURI)
                && node.getLocalName().equalsIgnoreCase(localName)) {
                String result = getNodeAttr(attrName, node);  
                if (result != null) {return result; }     
            }
        }
        return null;
    }

    /**
     * Returns an array of maps, that represents a tabular structure. Each map
     * is a row with all the columns for that row.
     *
     * @param url the URL that specifies the location of the input.
     * @return an array of maps, that represents a tabular structure. Each map
     * is a row with all the columns for that row.
     * @throws java.net.MalformedURLException if supplied URL is malformed
     * @throws java.io.FileNotFoundException if file URL references is not found
     * @throws java.io.UnsupportedEncodingException if the encoding is not
     * supported.
     * @throws TransformerException if an error occurs while transforming the
     * XML into a document.
     */
    public List<Map<String, String>> map(final String url) throws MalformedURLException, FileNotFoundException, UnsupportedEncodingException, TransformerException {       
        return map(read(FileUtils.toFile(new URL(url))));
    }

    /**
     * Returns an array of maps, that represents a tabular structure. Each map
     * is a row with all the columns for that row.
     *
     * @param url the URL that specifies the location of the input.
     * @param rowTag the tags that represent a row in the resulting tabular
     * structure.
     * @return an array of maps, that represents a tabular structure. Each map
     * is a row with all the columns for that row.
     * @throws java.net.MalformedURLException if supplied URL is malformed
     * @throws java.io.FileNotFoundException if file URL references is not found
     * @throws UnsupportedEncodingException if the encoding is not supported.
     * @throws TransformerException if an error occurs while transforming the
     * XML into a document.
     */
    public List<Map<String, String>> map(final String url, final String rowTag) throws MalformedURLException, FileNotFoundException, UnsupportedEncodingException, TransformerException {
        final Document document = read(FileUtils.toFile(new URL(url)));
        return map(document, rowTag);
    }

    /**
     * Reads an XML document into an array of maps. The XML document is
     * recursively read and each time a tag with a specified name is
     * encountered, the children of that element are used to create a series of
     * key/value pairs that are placed into a new map in the array. The name of
     * each child tag becomes a new key while the text content of that element
     * becomes the value.
     *
     * @param document the XML document.
     * @param rowTag the tags that represent a row in the resulting tabular
     * structure.
     * @return a tabular representation of the given XML document.
     */
    public List<Map<String, String>> map(final Document document, final String rowTag) {
        final List<Map<String, String>> table = new LinkedList<>();
        final NodeList rowList = document.getElementsByTagName(rowTag);
        for (int r = 0; r < rowList.getLength(); r++) {
            final Node rowNode = rowList.item(r);
            if (rowNode instanceof Element) {
                final Element rowElement = (Element) rowNode;

                final Map<String, String> row = new HashMap<>();
                table.add(row);

                final NodeList cellList = rowElement.getChildNodes();
                for (int c = 0; c < cellList.getLength(); c++) {
                    final Node cellNode = cellList.item(c);
                    if (cellNode instanceof Element) {
                        final Element cellElement = (Element) cellNode;
                        row.put(cellElement.getTagName(), cellElement.getTextContent());
                    }
                }
            }
        }
        return table;
    }

    /**
     * Reads an XML document into an array of maps. The XML document is
     * recursively read and each time a tag is encountered, the children of that
     * element are used to create a series of key/value pairs that are placed
     * into a new map in the array. The name of each child tag becomes a new key
     * while the text content of that element becomes the value.
     *
     * @param document the XML document.
     * @return a tabular representation of the given XML document.
     */
    public List<Map<String, String>> map(final Document document) {
        final List<Map<String, String>> table = new LinkedList<>();
        final Element tableElement = document.getDocumentElement();
        final NodeList rowList = tableElement.getChildNodes();
        for (int r = 0; r < rowList.getLength(); r++) {
            final Node rowNode = rowList.item(r);
            if (rowNode instanceof Element) {
                final Element rowElement = (Element) rowNode;

                final Map<String, String> row = new HashMap<>();
                table.add(row);

                final NodeList cellList = rowElement.getChildNodes();
                for (int c = 0; c < cellList.getLength(); c++) {
                    Node cellNode = cellList.item(c);
                    if (cellNode instanceof Element) {
                        final Element cellElement = (Element) cellNode;
                        row.put(cellElement.getTagName(), cellElement.getTextContent());
                    }
                }
            }
        }
        return table;
    }

    /**
     * Converts the XML document at the specified URL into a 2-dimensional
     * String array and returns the result.
     *
     * @param url a URL referencing an XML document.
     * @param swap if true then the rows and columns are swapped.
     * @return the table.
     * @throws java.io.FileNotFoundException if file URL references is not found
     * @throws UnsupportedEncodingException if the encoding is not supported.
     * @throws TransformerException if an error occurs while transforming the
     * XML into a document.
     */
    public String[][] table(final String url, final boolean swap) throws MalformedURLException, FileNotFoundException, UnsupportedEncodingException, TransformerException {
        return table(read(FileUtils.toFile(new URL(url))), swap);
    }

    /**
     * Converts an XML document into a table and returns the result as a
     * 2-dimensional String array.
     *
     * @param document the document.
     * @param swap if true then the rows and columns are swapped.
     * @return the table.
     */
    private String[][] table(final Document document, boolean swap) {
        final SortedMap<Integer, SortedMap<Integer, String>> table = new TreeMap<>();
        final Element tableElement = document.getDocumentElement();
        final NodeList rowList = tableElement.getChildNodes();
        int rowIndex = 0;
        for (int r = 0; r < rowList.getLength(); r++) {
            final Node rowNode = rowList.item(r);
            if (rowNode instanceof Element) {
                final Element rowElement = (Element) rowNode;

                int columnIndex = 0;
                final NodeList cellList = rowElement.getChildNodes();
                for (int c = 0; c < cellList.getLength(); c++) {
                    final Node cellNode = cellList.item(c);
                    if (cellNode instanceof Element) {
                        final Element cellElement = (Element) cellNode;
                        setCell(table, rowIndex, columnIndex, cellElement.getTextContent(), swap);
                        columnIndex++;
                    }
                }

                rowIndex++;
            }
        }

        final String[][] result = new String[table.lastKey() + 1][];
        for (int r = 0; r < result.length; r++) {
            final SortedMap<Integer, String> row = table.get(r);
            if (row == null) {
                result[r] = new String[0];
            } else {
                result[r] = new String[row.lastKey() + 1];
                for (int c = 0; c < result[r].length; c++) {
                    result[r][c] = row.get(c);
                }
            }
        }

        return result;
    }

    private void setCell(final SortedMap<Integer, SortedMap<Integer, String>> table, int rowIndex, int columnIndex, final String value, final boolean swap) {

        if (swap) {
            int temp = rowIndex;
            rowIndex = columnIndex;
            columnIndex = temp;
        }

        SortedMap<Integer, String> row = table.get(rowIndex);
        if (row == null) {
            row = new TreeMap<>();
            table.put(rowIndex, row);
        }
        row.put(columnIndex, value);
    }

    private class BadCharFilterInputStream extends InputStream {

        final InputStream in;

        public BadCharFilterInputStream(final InputStream in) {
            this.in = in;
        }

        @Override
        public int read() throws IOException {
            int c = in.read();
            if (c >= 0 && c < ' ') {
                c = ' ';
            }

            return c;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            final int bytes = in.read(b, off, len);
            if (bytes != -1) {
                for (int ix = off; ix < off + bytes; ix++) {
                    if (b[ix] >= 0 && b[ix] < ' ') {
                        b[ix] = ' ';
                    }
                }
            }

            return bytes;
        }
    }
}
