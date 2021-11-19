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
import au.gov.asd.tac.constellation.utilities.xml.XmlUtilities;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.transform.TransformerException;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * A XMLImportFileParser implements an ImportFileParser that can parse XML
 * files.
 * <p>
 * The XML is parser assumes that the data is basically of tabular form but
 * stored in an XML file. In these cases, there is a tag that represents a row
 * and there will generally be many of these row tags under a common parent. To
 * find this tag, the parser examines each tag and compares the number of these
 * tags with the number of its parent tag. The tag with the highest value is
 * assumed to be the row tag.
 * <p>
 * Once a row tag has been determined, each tag that descends from the row tag,
 * and each attribute of those tags are assumed to be columns.
 *
 * @author sirius
 */
@ServiceProvider(service = ImportFileParser.class)
public class XMLImportFileParser extends ImportFileParser {

    public XMLImportFileParser() {
        super("XML", 3);
    }

    @Override
    public List<String[]> parse(final InputSource input, final PluginParameters parameters) throws IOException {

        try (InputStream in = input.getInputStream()) {
            final XmlUtilities xml = new XmlUtilities();
            final Document document = xml.read(in, true);
            final Element documentElement = document.getDocumentElement();

            final Map<List<String>, Counter> counts = new HashMap<>();
            final List<String> path = new ArrayList<>();
            path.add(documentElement.getTagName());
            exploreElement(documentElement, path, counts);

            final List<String> bestTag = findRowElement(counts);
            final Map<List<String>, List<String>> globalKeys = new HashMap<>();
            final List<Map<List<String>, String>> table = new ArrayList<>();
            findRowElements(documentElement, path, bestTag, globalKeys, table);

            return createResult(globalKeys, table);
        } catch (TransformerException ex) {
            throw new IOException("Error reading XML file", ex);
        }
    }

    @Override
    public List<String[]> preview(final InputSource input, final PluginParameters parameters, final int limit) throws IOException {
        return parse(input, parameters);
    }

    @Override
    public FileNameExtensionFilter getExtensionFilter() {
        return FileChooser.XML_FILE_FILTER;
    }

    private void exploreElement(final Element element, final List<String> path, final Map<List<String>, Counter> counts) {

        final Counter counter = counts.get(path);
        if (counter == null) {
            counts.put(new ArrayList<>(path), new Counter());
        } else {
            counter.count++;
        }

        NodeList childNodeList = element.getChildNodes();
        for (int childIndex = 0; childIndex < childNodeList.getLength(); childIndex++) {
            final Node childNode = childNodeList.item(childIndex);
            if (childNode instanceof Element) {
                final Element childElement = (Element) childNode;
                path.add(childElement.getTagName());
                exploreElement(childElement, path, counts);
                path.remove(path.size() - 1);
            }
        }
    }

    private void findRowElements(final Element element, final List<String> currentPath, final List<String> elementPath,
            final Map<List<String>, List<String>> globalKeys, final List<Map<List<String>, String>> table) {
        if (currentPath.equals(elementPath)) {
            final Map<List<String>, String> rowValues = new HashMap<>();
            table.add(rowValues);

            final List<String> rowPath = new ArrayList<>();
            final NodeList childNodeList = element.getChildNodes();
            for (int childIndex = 0; childIndex < childNodeList.getLength(); childIndex++) {
                final Node childNode = childNodeList.item(childIndex);
                if (childNode instanceof Element) {
                    final Element childElement = (Element) childNode;
                    rowPath.add(childElement.getTagName());
                    processRowElement(childElement, rowPath, rowValues, globalKeys);
                    rowPath.remove(rowPath.size() - 1);
                }
            }
        } else {
            final NodeList childNodeList = element.getChildNodes();
            for (int childIndex = 0; childIndex < childNodeList.getLength(); childIndex++) {
                final Node childNode = childNodeList.item(childIndex);
                if (childNode instanceof Element) {
                    final Element childElement = (Element) childNode;
                    currentPath.add(childElement.getTagName());
                    findRowElements(childElement, currentPath, elementPath, globalKeys, table);
                    currentPath.remove(currentPath.size() - 1);
                }
            }

        }
    }

    private void processRowElement(final Element element, final List<String> rowPath,
            final Map<List<String>, String> rowValues, final Map<List<String>, List<String>> globalKeys) {
        List<String> globalKey = createGlobalKey(rowPath, globalKeys);
        if (rowValues.containsKey(globalKey)) {
            final String lastElement = rowPath.remove(rowPath.size() - 1);
            rowPath.add(lastElement + "(1)");
            globalKey = createGlobalKey(rowPath, globalKeys);
            rowPath.set(rowPath.size() - 1, lastElement);
            int nextIndex = 2;

            while (rowValues.containsKey(globalKey)) {
                rowPath.set(rowPath.size() - 1, lastElement + "(" + nextIndex++ + ")");
                globalKey = createGlobalKey(rowPath, globalKeys);
                rowPath.set(rowPath.size() - 1, lastElement);
            }
        }

        final StringBuilder text = new StringBuilder();
        final NodeList childNodeList = element.getChildNodes();
        for (int childIndex = 0; childIndex < childNodeList.getLength(); childIndex++) {
            final Node childNode = childNodeList.item(childIndex);
            if (childNode instanceof Element) {
                final Element childElement = (Element) childNode;
                rowPath.add(childElement.getTagName());
                processRowElement(childElement, rowPath, rowValues, globalKeys);
                rowPath.remove(rowPath.size() - 1);
            } else if (childNode instanceof Text) {
                final Text textNode = (Text) childNode;
                text.append(textNode.getNodeValue());
            } else {
                // Do nothing
            }
        }

        if (text.length() > 0) {
            rowValues.put(globalKey, text.toString());
        }

        final NamedNodeMap attributeMap = element.getAttributes();
        if (attributeMap.getLength() > 0) {
            final List<String> attributeKey = new ArrayList<>(globalKey);
            for (int attributeIndex = 0; attributeIndex < attributeMap.getLength(); attributeIndex++) {
                final Node attributeNode = attributeMap.item(attributeIndex);
                if (attributeNode instanceof Attr) {
                    final Attr attribute = (Attr) attributeNode;
                    attributeKey.add("[" + attribute.getName() + "]");
                    final List<String> globalAttributeKey = createGlobalKey(attributeKey, globalKeys);
                    attributeKey.remove(attributeKey.size() - 1);
                    rowValues.put(globalAttributeKey, attribute.getValue());
                }
            }
        }
    }

    private static List<String> createGlobalKey(final List<String> key, final Map<List<String>, List<String>> globalKeys) {
        List<String> globalKey = globalKeys.get(key);
        if (globalKey == null) {
            globalKey = new ArrayList<>(key);
            globalKeys.put(globalKey, globalKey);
        }
        return globalKey;
    }

    private static List<String> findRowElement(final Map<List<String>, Counter> counts) {
        List<String> bestTag = null;
        float bestExpansionRatio = -Float.MIN_VALUE;

        for (final Entry<List<String>, Counter> entry : counts.entrySet()) {
            final List<String> tag = entry.getKey();
            final Counter count = entry.getValue();

            float expansionRatio;

            if (tag.size() == 1) {
                expansionRatio = count.count;
            } else {
                final List<String> parentTag = new ArrayList<>(tag);
                parentTag.remove(parentTag.size() - 1);
                final Counter parentCount = counts.get(parentTag);
                expansionRatio = (float) count.count / (float) parentCount.count;
            }

            if (expansionRatio > bestExpansionRatio) {
                bestExpansionRatio = expansionRatio;
                bestTag = tag;
            }
        }

        return bestTag;
    }

    private static List<String[]> createResult(final Map<List<String>, List<String>> globalKeys, final List<Map<List<String>, String>> table) {
        final Map<String, List<String>> columns = new TreeMap<>();

        for (final List<String> path : globalKeys.keySet()) {
            final StringBuilder pathBuilder = new StringBuilder();
            String delimiter = "";

            for (String segment : path) {
                pathBuilder.append(delimiter).append(segment);
                delimiter = "/";
            }

            columns.put(pathBuilder.toString(), path);
        }

        final List<String[]> result = new ArrayList<>();
        result.add(columns.keySet().toArray(new String[columns.size()]));

        for (final Map<List<String>, String> rowValues : table) {
            final String[] row = new String[columns.size()];
            result.add(row);

            int columnIndex = 0;

            for (final List<String> path : columns.values()) {
                final String value = rowValues.get(path);
                row[columnIndex++] = value;
            }
        }

        return result;
    }

    private class Counter {

        private int count = 1;

        @Override
        public String toString() {
            return String.valueOf(count);
        }
    }
}
