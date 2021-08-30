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

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author aldebaran30701
 */
public class TOCParser {

    /**
     * Parse
     */
    public static void parse(final File xmlFromFile) throws ParserConfigurationException, SAXException, IOException {
        final StringBuilder sb = new StringBuilder();
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFromFile);
        doc.getDocumentElement().normalize();

        //sb.append("");
        System.out.println("Root element :" + doc.getDocumentElement().getNodeName()); // toc vesrion="2.0"
        NodeList nList = doc.getElementsByTagName("tocitem"); // tocitem text="Views"
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            System.out.println("\nCurrent Element :" + nNode.getNodeName());

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                System.out.println("Student roll no : "
                        + eElement.getAttribute("rollno"));
                System.out.println("First Name : "
                        + eElement
                                .getElementsByTagName("firstname")
                                .item(0)
                                .getTextContent());
                System.out.println("Last Name : "
                        + eElement
                                .getElementsByTagName("lastname")
                                .item(0)
                                .getTextContent());
                System.out.println("Nick Name : "
                        + eElement
                                .getElementsByTagName("nickname")
                                .item(0)
                                .getTextContent());
                System.out.println("Marks : "
                        + eElement
                                .getElementsByTagName("marks")
                                .item(0)
                                .getTextContent());
            }
        }
    }

}
