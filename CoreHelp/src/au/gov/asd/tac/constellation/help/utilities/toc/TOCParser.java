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

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Parse all XML table of contents files and load them into a tree data
 * structure.
 *
 * @author aldebaran30701
 */
public class TOCParser {

    private static final Logger LOGGER = Logger.getLogger(TOCParser.class.getName());

    /**
     * Parse the XML file into the tree data structure.
     *
     * @param xmlFromFile the file to read
     * @param root the root node to place items into
     */
    public static void parse(final File xmlFromFile, final TreeNode root) {

        try {
            TreeNode currentParent = root;
            final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            final Document doc = dBuilder.parse(xmlFromFile);
            doc.getDocumentElement().normalize();

            // indent when no children
            final NodeList nList = doc.getElementsByTagName("tocitem");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                final Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    final Element eElement = (Element) nNode;
                    final TreeNode current = new TreeNode(new TOCItem(eElement.getAttribute("text"), eElement.getAttribute("target")));
                    // when has children, then set parent
                    if (nNode.hasChildNodes()) {
                        final TreeNode duplicate = TreeNode.search(eElement.getAttribute("text"), eElement.getAttribute("target"), root);

                        if (duplicate != null) {
                            //set the current parent to duplicate
                            currentParent = duplicate;
                            // dont add child
                        } else {
                            // Get parent node in xml, and add it to that node as a child
                            final Element parentElement = (Element) eElement.getParentNode();
                            final TreeNode parentDuplicate = TreeNode.search(parentElement.getAttribute("text"), parentElement.getAttribute("target"), root);
                            if (parentDuplicate == null) {
                                currentParent.addChild(current);
                                currentParent = current;
                            } else {
                                parentDuplicate.addChild(current);
                                currentParent = current;
                            }
                        }
                    } else {
                        final Element parentElement = (Element) eElement.getParentNode();
                        final TreeNode parentDuplicate = TreeNode.search(parentElement.getAttribute("text"), parentElement.getAttribute("target"), root);
                        if (parentDuplicate != null) {
                            currentParent.addChild(current);
                        }
                    }
                }
            }
        } catch (final ParserConfigurationException ex) {
            Exceptions.printStackTrace(ex);
            LOGGER.log(Level.SEVERE, "Failed to parse XML file", ex);
        } catch (final SAXException ex) {
            Exceptions.printStackTrace(ex);
            LOGGER.log(Level.SEVERE, "Failed to parse XML file", ex);
        } catch (final IOException ex) {
            Exceptions.printStackTrace(ex);
            LOGGER.log(Level.SEVERE, "Failed to parse XML file", ex);
        }
    }
}
