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
package au.gov.asd.tac.constellation.utilities.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.reporters.Files;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author serpens24
 */
public class XmlUtilitiesNGTest {

    private static final String OUTPUT_FILE = "testOutputFile.xml";
    private static final String XML_HDR = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of newDocument method, of class XmlUtilities. This effectively creates a new empty document.
     */
    @Test
    public void testNewDocument() {
        System.out.println("testNewDocument");
        XmlUtilities instance = new XmlUtilities();

        Document expResult = null;
        try {
            expResult = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException ex) {
            fail("Exception thrown creating sample data");
        }
        Document result = instance.newDocument();
        assertTrue(result.isEqualNode(expResult));
    }

    /**
     * Test of write method, of class XmlUtilities.
     *
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     * @throws javax.xml.transform.TransformerException
     */
    @Test
    public void testWrite_Document() throws ParserConfigurationException, IOException, SAXException, TransformerException {
        System.out.println("testWrite_Document");
        XmlUtilities instance = new XmlUtilities();

        String expectedStr = XML_HDR + "<parent>\n"
                + "<child1>child1_value</child1>\n"
                + "<child2>child2_value</child2>\n"
                + "<child3>child3a_value</child3>\n"
                + "<child3>child3b_value</child3>\n"
                + "<child4><child4.1>child4.1</child4.1></child4>\n"
                + "</parent>\n";

        Document document = getXmlDocument("resources/testWrite_Document.xml", false);
        String results = new String(instance.write(document));

        assertEquals(removeWhitespacing(results), removeWhitespacing(expectedStr));
    }

    /**
     * Test of write method, of class XmlUtilities.
     *
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     * @throws javax.xml.transform.TransformerException
     */
    @Test
    public void testWrite_Document_File() throws ParserConfigurationException, IOException, SAXException, TransformerException {
        System.out.println("testWrite_Document_File");
        XmlUtilities instance = new XmlUtilities();

        String expectedOutput = XML_HDR + "<parent>\n"
                + "<child>child_value</child>\n"
                + "</parent>\n";

        Document document = getXmlDocument("resources/testWrite_Document_File.xml", false);
        String outputFilename = XmlUtilitiesNGTest.class.getResource("resources/").getPath() + OUTPUT_FILE;
        File outputFile = new File(outputFilename);
        instance.write(document, outputFile);
        String output = Files.readFile(outputFile);

        assertEquals(removeWhitespacing(output), removeWhitespacing(expectedOutput));
    }

    /**
     * Test of writeToString method, of class XmlUtilities.
     *
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     * @throws javax.xml.transform.TransformerException
     */
    @Test
    public void testWriteToString_Document() throws ParserConfigurationException, IOException, SAXException, TransformerException {
        System.out.println("testWriteToString_Document");
        XmlUtilities instance = new XmlUtilities();

        String expectedOutput = XML_HDR + "<parent>\n"
                + "<child>child_value</child>\n"
                + "</parent>\n";

        // Read the test file into a Document object
        Document document = getXmlDocument("resources/testWriteToString_Document.xml", false);
        String output = instance.writeToString(document);

        assertEquals(removeWhitespacing(output), removeWhitespacing(expectedOutput));
    }

    /**
     * Test of writeToString method, of class XmlUtilities.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testWriteToString_InputStream_int() throws IOException {
        System.out.println("testWriteToString_InputStream_int");
        XmlUtilities instance = new XmlUtilities();

        String expectedOutput = "<parent>   <child>child_value</child> </parent> ";
        String testFile = XmlUtilitiesNGTest.class.getResource("resources/testWriteToString_InputStream_int.xml").getPath();
        InputStream inputStream = new FileInputStream(new File(testFile));
        String output = instance.writeToString(inputStream, inputStream.available());

        assertEquals(removeWhitespacing(output), removeWhitespacing(expectedOutput));
    }

    /**
     * Test of read method, of class XmlUtilities.
     *
     * @throws java.io.FileNotFoundException
     * @throws javax.xml.transform.TransformerException
     */
    @Test
    public void testRead_File() throws FileNotFoundException, TransformerException {
        System.out.println("testRead_File");
        XmlUtilities instance = new XmlUtilities();

        String testFile = XmlUtilitiesNGTest.class.getResource("resources/testRead_File.xml").getPath();
        Document document = instance.read(new File(testFile));

        NodeList parentNodeList = document.getElementsByTagName("parent");
        assertNotNull(parentNodeList);
        Node parent = parentNodeList.item(0);
        NodeList childNodeList = parent.getChildNodes();

        for (int child = 1; child <= 3; child++) {
            boolean foundChild = false;
            String nodeName = "child" + child;
            for (int i = 0; i < childNodeList.getLength(); i++) {
                Node childNode = childNodeList.item(i);
                if (nodeName.equals(childNode.getNodeName()) && childNode.getNodeType() == Node.ELEMENT_NODE) {
                    String nodeValue = childNode.getFirstChild().getNodeValue();
                    assertTrue(nodeValue.equals("child" + child + "_value"));
                    foundChild = true;
                    break;
                }
            }
            assertTrue(foundChild, nodeName);
        }
    }

    /**
     * Test of read method, of class XmlUtilities.
     *
     * @throws java.io.UnsupportedEncodingException
     * @throws javax.xml.transform.TransformerException
     */
    @Test
    public void testRead_String() throws UnsupportedEncodingException, TransformerException {
        System.out.println("testRead_String");
        XmlUtilities instance = new XmlUtilities();

        String data = """
                      <parent>
                      <child1>child1_value</child1>
                      <child2>child2_value</child2>
                      <child3>child3_value</child3>
                      </parent>
                      """;

        Document document = instance.read(data);

        NodeList parentNodeList = document.getElementsByTagName("parent");
        assertNotNull(parentNodeList);
        Node parent = parentNodeList.item(0);
        NodeList childNodeList = parent.getChildNodes();

        for (int child = 1; child <= 3; child++) {
            boolean foundChild = false;
            String nodeName = "child" + child;
            for (int i = 0; i < childNodeList.getLength(); i++) {
                Node childNode = childNodeList.item(i);
                if (nodeName.equals(childNode.getNodeName()) && childNode.getNodeType() == Node.ELEMENT_NODE) {
                    String nodeValue = childNode.getFirstChild().getNodeValue();
                    assertTrue(nodeValue.equals("child" + child + "_value"));
                    foundChild = true;
                    break;
                }
            }
            assertTrue(foundChild, nodeName);
        }
    }

    /**
     * Test of read method, of class XmlUtilities.
     *
     * @throws java.io.FileNotFoundException
     * @throws javax.xml.transform.TransformerException
     */
    @Test
    public void testRead_InputStream_boolean() throws FileNotFoundException, TransformerException {
        System.out.println("testRead_InputStream_boolean");
        XmlUtilities instance = new XmlUtilities();

        String testFile = XmlUtilitiesNGTest.class.getResource("resources/testRead_InputStream_boolean.xml").getPath();
        Document document = instance.read(new FileInputStream(new File(testFile)), true);

        NodeList parentNodeList = document.getElementsByTagName("parent");
        assertNotNull(parentNodeList);
        Node parent = parentNodeList.item(0);
        NodeList childNodeList = parent.getChildNodes();

        for (int child = 1; child <= 3; child++) {
            boolean foundChild = false;
            String nodeName = "child" + child;
            for (int i = 0; i < childNodeList.getLength(); i++) {
                Node childNode = childNodeList.item(i);
                if (nodeName.equals(childNode.getNodeName()) && childNode.getNodeType() == Node.ELEMENT_NODE) {
                    String nodeValue = childNode.getFirstChild().getNodeValue();
                    assertTrue(nodeValue.equals("child" + child + "_value"));
                    foundChild = true;
                    break;
                }
            }
            assertTrue(foundChild, nodeName);
        }
    }

    /**
     * Test of getNode method, of class XmlUtilities.
     *
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    @Test
    public void testGetNode() throws ParserConfigurationException, IOException, SAXException {
        System.out.println("testGetNode");
        XmlUtilities instance = new XmlUtilities();

        Document document = getXmlDocument("resources/testGetNode.xml", false);
        NodeList nodeList = document.getElementsByTagName("parent");
        Node parent = nodeList.item(0);
        NodeList children = parent.getChildNodes();

        // Confirm matching node is found, and is the first match
        Node node = instance.getNode("child3", children);
        assertNotNull(node, "Found node");
        assertEquals(node.getNodeName(), "child3", "Node name matches");
        assertEquals(node.getChildNodes().getLength(), 1, "Node has one child element");
        assertEquals(node.getChildNodes().item(0).getNodeValue(), "child3a_value", "Node value matches");

        // Confirm searches are case insensitive
        node = instance.getNode("CHILD3", children);
        assertNotNull(node, "Found node (case insensitive)");
        assertEquals(node.getNodeName(), "child3", "Node name matches (case insensitive)");

        // Confirm no results found when node doesn't exist
        node = instance.getNode("missing_child", children);
        assertNull(node, "Can't find node");

        // Confirm nested nodes are not traversed
        node = instance.getNode("child4.1", children);
        assertNull(node, "Doesn't find nested node");
    }

    /**
     * Test of getNodeNS method, of class XmlUtilities.
     *
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    @Test
    public void testGetNodeNS() throws ParserConfigurationException, IOException, SAXException {
        System.out.println("testGetNodeNS");
        XmlUtilities instance = new XmlUtilities();

        Document document = getXmlDocument("resources/testGetNodeNS.xml", true);
        NodeList nodeList = document.getElementsByTagName("c:parent");
        Node parent = nodeList.item(0);
        NodeList children = parent.getChildNodes();

        // Confirm matching node is found, and is the first match
        Node node = instance.getNodeNS("http://www.consty.com/star", "child3", children);
        assertNotNull(node, "Found node");
        assertEquals(node.getNodeName(), "c:child3", "Node name matches");
        assertEquals(node.getChildNodes().getLength(), 1, "Node has one child element");
        assertEquals(node.getChildNodes().item(0).getNodeValue(), "child3a_value", "Node value matches");

        // Confirm searches are case insensitive
        node = instance.getNodeNS("http://www.consty.com/star", "CHILD3", children);
        assertNotNull(node, "Found node (case insensitive)");
        assertEquals(node.getNodeName(), "c:child3", "Node name matches (case insensitive)");

        // Confirm no results found when node doesn't exist
        node = instance.getNodeNS("http://www.consty.com/star", "missing_child", children);
        assertNull(node, "Can't find node");

        // Confirm nested nodes are not traversed
        node = instance.getNodeNS("http://www.consty.com/star", "child4.1", children);
        assertNull(node, "Doesn't find nested node");

        // Confirm nothing is found if invalid namespace is specified
        node = instance.getNodeNS("http://www.notconsty.com/star", "child3", children);
        assertNull(node, "Doesn't find if invalid namespace");
    }

    /**
     * Test of getNodes method, of class XmlUtilities.
     *
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    @Test
    public void testGetNodes() throws ParserConfigurationException, IOException, SAXException {
        System.out.println("testGetNodes");
        XmlUtilities instance = new XmlUtilities();

        Document document = getXmlDocument("resources/testGetNodes.xml", false);
        NodeList nodeList = document.getElementsByTagName("parent");
        Node parent = nodeList.item(0);
        NodeList children = parent.getChildNodes();

        // Confirm all matching nodes are found
        List<Node> nodes = instance.getNodes("child", children);
        assertEquals(nodes.size(), 3, "Found all 3 nodes called child");
        for (int index = 0; index < nodes.size(); index++) {
            assertEquals(nodes.get(index).getNodeName(), "child", "Node name matches");
            assertEquals(nodes.get(index).getChildNodes().getLength(), 1, "Node has one child element");
            assertEquals(nodes.get(index).getChildNodes().item(0).getNodeValue(), "child_value" + (index + 1), "Node value matches");
        }

        // Confirm searches are case insensitive
        nodes = instance.getNodes("CHILD", children);
        assertEquals(nodes.size(), 3, "Found all 3 nodes called child (case insensitive)");
        for (int index = 0; index < nodes.size(); index++) {
            assertEquals(nodes.get(index).getNodeName(), "child", "Node name matches (case insensitive)");
            assertEquals(nodes.get(index).getChildNodes().getLength(), 1, "Node has one child element (case insensitive)");
            assertEquals(nodes.get(index).getChildNodes().item(0).getNodeValue(), "child_value" + (index + 1), "Node value matches (case insensitive)");
        }

        // Confirm no results found when node doesn't exist
        nodes = instance.getNodes("missing_child", children);
        assertEquals(nodes.size(), 0, "Found 0 nodes called missing_child");

        // Confirm nested nodes are not traversed
        nodes = instance.getNodes("child_nest", children);
        assertEquals(nodes.size(), 0, "Found 0 nodes matching nested node name");
    }

    /**
     * Test of getNodesNS method, of class XmlUtilities.
     *
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    @Test
    public void testGetNodesNS() throws ParserConfigurationException, IOException, SAXException {
        System.out.println("testGetNodesNS");
        XmlUtilities instance = new XmlUtilities();

        Document document = getXmlDocument("resources/testGetNodesNS.xml", true);
        NodeList nodeList = document.getElementsByTagName("c:parent");
        Node parent = nodeList.item(0);
        NodeList children = parent.getChildNodes();

        // Confirm multiple matching nodes can be returned
        List<Node> nodes = instance.getNodesNS("http://www.consty.com/star", "child", children);
        assertEquals(nodes.size(), 2, "Found both nodes called child in given namespace");
        for (int index = 0; index < nodes.size(); index++) {
            assertEquals(nodes.get(index).getNodeName(), "c:child", "Node name matches");
            assertEquals(nodes.get(index).getChildNodes().getLength(), 1, "Node has one child element");
            assertEquals(nodes.get(index).getChildNodes().item(0).getNodeValue(), "child_value" + (index + 1), "Node value matches");
        }

        // Confirm searches are case insensitive
        nodes = instance.getNodesNS("http://www.consty.com/star", "CHILD", children);
        assertEquals(nodes.size(), 2, "Found both nodes called child in given namespace (case insensitive)");
        for (int index = 0; index < nodes.size(); index++) {
            assertEquals(nodes.get(index).getNodeName(), "c:child", "Node name matches (case insensitive)");
            assertEquals(nodes.get(index).getChildNodes().getLength(), 1, "Node has one child element (case insensitive)");
            assertEquals(nodes.get(index).getChildNodes().item(0).getNodeValue(), "child_value" + (index + 1), "Node value matches (case insensitive)");
        }

        // Confirm null is returned if no node matching all conditions is found
        nodes = instance.getNodesNS("http://www.consty.com/star", "missing_child", children);
        assertEquals(nodes.size(), 0, "Found 0 nodes called missing_child in namespace");

        // Confirm nested nodes are not traversed
        nodes = instance.getNodes("child_nest", children);
        assertEquals(nodes.size(), 0, "Found 0 nodes matching nested node name in namespace");

        // Test case where namespace doesn't exist
        nodes = instance.getNodesNS("http://www.notconsty.com/star", "child", children);
        assertEquals(nodes.size(), 0, "Found 0 nodes matching nodes in unknown namespace");

    }

    /**
     * Test of getNodeValue method, of class XmlUtilities.
     *
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    @Test
    public void testGetNodeValue_Node() throws ParserConfigurationException, IOException, SAXException {
        System.out.println("testGetNodeValue_Node");
        XmlUtilities instance = new XmlUtilities();

        Document document = getXmlDocument("resources/testGetNodeValue_Node.xml", false);
        NodeList nodeList = document.getElementsByTagName("parent");
        Node parent = nodeList.item(0);
        NodeList children = parent.getChildNodes();

        // Check that parent node doesnt contain content
        String result = instance.getNodeValue(parent);
        assertNull(result, "First child value matches expected");

        // Check that correct  child value is returned
        result = instance.getNodeValue(children.item(0));
        assertEquals(result, "child_value1", "First child value matches expected");
    }

    /**
     * Test of getNodeValue method, of class XmlUtilities.
     *
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    @Test
    public void testGetNodeValue_String_NodeList() throws ParserConfigurationException, IOException, SAXException {
        System.out.println("testGetNodeValue_String_NodeList");
        XmlUtilities instance = new XmlUtilities();

        Document document = getXmlDocument("resources/testGetNodeValue_String_NodeList.xml", false);
        NodeList nodeList = document.getElementsByTagName("parent");
        Node parent = nodeList.item(0);
        NodeList children = parent.getChildNodes();

        // Check that correct first child value is returned
        String result = instance.getNodeValue("child", children);
        assertEquals(result, "child_value1", "First child value matches expected");

        // Repeat above but use node name that doesnt match first child node
        result = instance.getNodeValue("child3", children);
        assertEquals(result, "child_value3", "Confirm actual node name is checked when returning child");

        // Check that only nodes with matching name that are of type Node.TEXT_NODE are considered
        result = instance.getNodeValue("child4", children);
        assertEquals(result, "child_value4", "First string child value matches expected");

        // Confirm null is returned if no node matching all conditions is found
        result = instance.getNodeValue("child5", children);
        assertNull(result, "Non text node returns null");
    }

    /**
     * Test of getNodeValueNS method, of class XmlUtilities.
     *
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    @Test
    public void testGetNodeValueNS() throws ParserConfigurationException, IOException, SAXException {
        System.out.println("testGetNodeValueNS");
        XmlUtilities instance = new XmlUtilities();

        Document document = getXmlDocument("resources/testGetNodeValueNS.xml", true);
        NodeList nodeList = document.getElementsByTagName("c:parent");
        Node parent = nodeList.item(0);
        NodeList children = parent.getChildNodes();

        // Check that correct first child value is returned
        String result = instance.getNodeValueNS("http://www.consty.com/star", "child", children);
        assertEquals(result, "child_value1", "First child value matches expected");

        // Confirm searches are case insensitive
        result = instance.getNodeValueNS("http://www.consty.com/star", "CHILD", children);
        assertEquals(result, "child_value1", "First child value matches expected (case insensitive)");

        // Confirm null is returned if no node matching all conditions is found
        result = instance.getNodeValueNS("http://www.consty.com/star", "missing_child", children);
        assertNull(result, "Missing node returns null");

        // Confirm nested nodes are not traversed
        result = instance.getNodeValueNS("http://www.consty.com/star", "child_nest", children);
        assertNull(result, "Nested node is not found and function returns null");

        // Confirm nodes that are not TEXT_NODE's are not considered
        result = instance.getNodeValueNS("http://www.consty.com/star", "nest_child", children);
        assertEquals(result, "second_nest_child", "Nodes not considered if not TEXT_NODEs");

        // Test case where namespace doesn't exist
        result = instance.getNodeValueNS("http://www.notconsty.com/star", "child", children);
        assertNull(result, "Namespace not found, hence no match - return null");
    }

    /**
     * Test of getNodeAttr method, of class XmlUtilities.
     *
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    @Test
    public void testGetNodeAttr_String_Node() throws ParserConfigurationException, IOException, SAXException {
        System.out.println("testGetNodeAttr_String_Node");
        XmlUtilities instance = new XmlUtilities();

        Document document = getXmlDocument("resources/testGetNodeAttr_String_Node.xml", false);
        NodeList nodeList = document.getElementsByTagName("parent");
        Node parent = nodeList.item(0);

        // Show attribute values are read
        String result = instance.getNodeAttr("attr1", parent);
        assertEquals(result, "attrib 1 value", "Successfully finds attribute");

        // Show attributes other than first can be read
        result = instance.getNodeAttr("attr2", parent);
        assertEquals(result, "attrib 2 value", "Successfully finds attribute");

        // Handling of missing attributes
        result = instance.getNodeAttr("attr3", parent);
        assertNull(result, "Null returned if attribute is not found");
    }

    /**
     * Test of getNodeAttr method, of class XmlUtilities.
     *
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    @Test
    public void testGetNodeAttr_3args() throws ParserConfigurationException, IOException, SAXException {
        System.out.println("testGetNodeAttr_3args");
        XmlUtilities instance = new XmlUtilities();

        Document document = getXmlDocument("resources/testGetNodeAttr_3args.xml", true);
        NodeList nodeList = document.getElementsByTagName("parent");
        Node parent = nodeList.item(0);
        NodeList children = parent.getChildNodes();

        // Show attribute values are read
        String result = instance.getNodeAttr("child", "attr1", children);
        assertEquals(result, "attrib 1 value", "Successfully finds attribute");

        // Show attributes other than first can be read
        result = instance.getNodeAttr("child", "attr2", children);
        assertEquals(result, "attrib 2 value", "Successfully finds attribute");

        // Handling of missing attributes
        result = instance.getNodeAttr("child", "attr3", children);
        assertNull(result, "Null returned if attribute is not found");

        // Show that both node name and attrib name need to match
        result = instance.getNodeAttr("offspring", "attr1", children);
        assertEquals(result, "attrib 1 value", "Successfully skips nodes missing attribute");
    }

    /**
     * Test of getNodeAttrNS method, of class XmlUtilities.
     *
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    @Test
    public void testGetNodeAttrNS() throws ParserConfigurationException, IOException, SAXException {
        System.out.println("testGetNodeAttrNS");
        XmlUtilities instance = new XmlUtilities();

        Document document = getXmlDocument("resources/testGetNodeAttrNS.xml", true);
        NodeList nodeList = document.getElementsByTagName("parent");
        Node parent = nodeList.item(0);
        NodeList children = parent.getChildNodes();

        // Check that correct first child value is returned
        String result = instance.getNodeAttrNS("http://www.consty.com/star", "child", "attr1", children);
        assertEquals(result, "c:attrib 1 value", "First child value matches expected");

        // Confirm searches are case insensitive
        result = instance.getNodeAttrNS("http://www.consty.com/star", "CHILD", "ATTR1", children);
        assertEquals(result, "c:attrib 1 value", "First child value matches expected (case insensitive)");

        // Confirm namnespace is considered
        result = instance.getNodeAttrNS("http://www.consty2.com/star2", "child", "attr1", children);
        assertEquals(result, "c2:attrib 1 value", "First child value matches expected");

        // Confirm that nodes not matching all conditions are stepped over
        result = instance.getNodeAttrNS("http://www.consty.com/star", "offspring", "attr1", children);
        assertEquals(result, "c:attrib 1 value", "TODO");
    }

    /**
     * Test of map method, of class XmlUtilities which takes in a URL.
     *
     * @throws java.net.MalformedURLException
     * @throws java.io.FileNotFoundException
     * @throws java.io.UnsupportedEncodingException
     * @throws javax.xml.transform.TransformerException
     */
    @Test
    public void testMap_String() throws MalformedURLException, FileNotFoundException, UnsupportedEncodingException, TransformerException {
        System.out.println("testMap_String");
        XmlUtilities instance = new XmlUtilities();

        // Extract content and validate it is as expected
        String testFile = XmlUtilitiesNGTest.class.getResource("resources/testMap_String.xml").getPath();
        URL url = new File(testFile).toURI().toURL();
        List<Map<String, String>> result = instance.map(url.toString());
        assertEquals(4, result.size());
        assertEquals(3, result.get(0).size());
        assertEquals(3, result.get(1).size());
        assertEquals(3, result.get(2).size());
        assertEquals(0, result.get(3).size());
        assertNotNull(result.get(0).get("col1"), "aaa");
        assertNotNull(result.get(0).get("col2"), "bbb");
        assertNotNull(result.get(0).get("col3"), "ccc");
        assertNotNull(result.get(1).get("col1"), "ddd");
        assertNotNull(result.get(1).get("col2"), "eee");
        assertNotNull(result.get(1).get("col3"), "fff");
        assertNotNull(result.get(2).get("col1"), "ggg");
        assertNotNull(result.get(2).get("col2"), "hhh");
        assertNotNull(result.get(2).get("col3"), "iii");
    }

    /**
     * Test of map method, of class XmlUtilities, showing file not found exception thrown if file cant be found.
     *
     * @throws java.net.MalformedURLException
     * @throws java.io.FileNotFoundException
     * @throws java.io.UnsupportedEncodingException
     * @throws javax.xml.transform.TransformerException
     */
    @Test(expectedExceptions = FileNotFoundException.class)
    public void testMap_String_FilenotFound() throws MalformedURLException, FileNotFoundException, UnsupportedEncodingException, TransformerException {
        System.out.println("testMap_String_FilenotFound");
        XmlUtilities instance = new XmlUtilities();

        String testFile = XmlUtilitiesNGTest.class.getResource("resources/").getPath() + "missing.xml";
        URL url = new File(testFile).toURI().toURL();
        instance.map(url.toString());
    }

    /**
     * Test of map method, of class XmlUtilities, showing file not found exception thrown if file cant be found.
     *
     * @throws java.net.MalformedURLException
     * @throws java.io.FileNotFoundException
     * @throws java.io.UnsupportedEncodingException
     * @throws javax.xml.transform.TransformerException
     */
    @Test(expectedExceptions = TransformerException.class)
    public void testMap_String_TransformerException() throws MalformedURLException, FileNotFoundException, UnsupportedEncodingException, TransformerException {
        System.out.println("testMap_String_TransformerException");
        XmlUtilities instance = new XmlUtilities();

        // Execute test
        String testFile = XmlUtilitiesNGTest.class.getResource("resources/testMap_String_TransformerException.xml").getPath();
        URL url = new File(testFile).toURI().toURL();
        instance.map(url.toString());
    }

    /**
     * Test of map method, of class XmlUtilities.
     *
     * @throws java.net.MalformedURLException
     * @throws java.io.FileNotFoundException
     * @throws java.io.UnsupportedEncodingException
     * @throws javax.xml.transform.TransformerException
     */
    @Test
    public void testMap_String_String() throws MalformedURLException, FileNotFoundException, UnsupportedEncodingException, TransformerException {
        System.out.println("testMap_String_String");
        XmlUtilities instance = new XmlUtilities();

        String testFile = XmlUtilitiesNGTest.class.getResource("resources/testMap_String_String.xml").getPath();
        URL url = new File(testFile).toURI().toURL();
        List<Map<String, String>> result = instance.map(url.toString(), "child");
        assertEquals(3, result.size());
        assertEquals(3, result.get(0).size());
        assertEquals(3, result.get(1).size());
        assertEquals(0, result.get(2).size());
        assertNotNull(result.get(0).get("col1"), "aaa");
        assertNotNull(result.get(0).get("col2"), "bbb");
        assertNotNull(result.get(0).get("col3"), "ccc");
        assertNotNull(result.get(1).get("col1"), "ddd");
        assertNotNull(result.get(1).get("col2"), "eee");
        assertNotNull(result.get(1).get("col3"), "fff");
    }

    /**
     * Test of map method, of class XmlUtilities, showing file not found exception thrown if file cant be found.
     *
     * @throws java.net.MalformedURLException
     * @throws java.io.FileNotFoundException
     * @throws java.io.UnsupportedEncodingException
     * @throws javax.xml.transform.TransformerException
     */
    @Test(expectedExceptions = FileNotFoundException.class)
    public void testMap_String_String_FilenotFound() throws MalformedURLException, FileNotFoundException, UnsupportedEncodingException, TransformerException {
        System.out.println("testMap_String_String_FilenotFound");
        XmlUtilities instance = new XmlUtilities();

        String testFile = XmlUtilitiesNGTest.class.getResource("resources/").getPath() + "missing.xml";
        URL url = new File("Missing" + testFile).toURI().toURL();
        instance.map(url.toString(), "child");
    }

    /**
     * Test of map method, of class XmlUtilities, showing file not found exception thrown if file cant be found.
     *
     * @throws java.net.MalformedURLException
     * @throws java.io.FileNotFoundException
     * @throws java.io.UnsupportedEncodingException
     * @throws javax.xml.transform.TransformerException
     */
    @Test(expectedExceptions = TransformerException.class)
    public void testMap_String_String_TransformerException() throws MalformedURLException, FileNotFoundException, UnsupportedEncodingException, TransformerException {
        System.out.println("testMap_String_String_TransformerException");
        XmlUtilities instance = new XmlUtilities();

        String testFile = XmlUtilitiesNGTest.class.getResource("resources/testMap_String_String_TransformerException.xml").getPath();
        URL url = new File(testFile).toURI().toURL();
        instance.map(url.toString(), "child");
    }

    /**
     * Test of table method, of class XmlUtilities.
     *
     * @throws java.net.MalformedURLException
     * @throws java.io.FileNotFoundException
     * @throws java.io.UnsupportedEncodingException
     * @throws javax.xml.transform.TransformerException
     */
    @Test
    public void testTable_String_Boolean() throws MalformedURLException, FileNotFoundException, UnsupportedEncodingException, TransformerException {
        System.out.println("testTable_String_Boolean");
        XmlUtilities instance = new XmlUtilities();

        String testFile = XmlUtilitiesNGTest.class.getResource("resources/testTable_String_Boolean.xml").getPath();
        URL url = new File(testFile).toURI().toURL();
        String[][] result = instance.table(url.toString(), false);
        assertEquals(result.length, 3);
        assertEquals(result[0].length, 3);
        assertEquals(result[1].length, 2);
        assertEquals(result[2].length, 3);
        assertEquals(result[0][0], "aaa");
        assertEquals(result[0][1], "bbb");
        assertEquals(result[0][2], "ccc");
        assertEquals(result[1][0], "ddd");
        assertEquals(result[1][1], "eee");
        assertEquals(result[2][0], "ggg");
        assertEquals(result[2][1], "hhh");
        assertEquals(result[2][2], "iii");

        String[][] swappedResult = instance.table(url.toString(), true);
        assertEquals(swappedResult.length, 3);
        assertEquals(swappedResult[0].length, 3);
        assertEquals(swappedResult[1].length, 3);
        assertEquals(swappedResult[2].length, 3);
        assertEquals(swappedResult[0][0], "aaa");
        assertEquals(swappedResult[0][1], "ddd");
        assertEquals(swappedResult[0][2], "ggg");
        assertEquals(swappedResult[1][0], "bbb");
        assertEquals(swappedResult[1][1], "eee");
        assertEquals(swappedResult[1][2], "hhh");
        assertEquals(swappedResult[2][0], "ccc");
        assertNull(swappedResult[2][1]);
        assertEquals(swappedResult[2][2], "iii");
    }

    /**
     * Test of table method, of class XmlUtilities, showing file not found exception thrown if file cant be found.
     *
     * @throws java.net.MalformedURLException
     * @throws java.io.FileNotFoundException
     * @throws java.io.UnsupportedEncodingException
     * @throws javax.xml.transform.TransformerException
     */
    @Test(expectedExceptions = FileNotFoundException.class)
    public void testTable_String_Boolean_FilenotFound() throws MalformedURLException, FileNotFoundException, UnsupportedEncodingException, TransformerException {
        System.out.println("testTable_String_Boolean_FilenotFound");
        XmlUtilities instance = new XmlUtilities();

        String testFile = XmlUtilitiesNGTest.class.getResource("resources/").getPath() + "missing.xml";
        URL url = new File(testFile).toURI().toURL();
        instance.table(url.toString(), false);
    }

    /**
     * Test of table method, of class XmlUtilities, showing file not found exception thrown if file cant be found.
     *
     * @throws java.net.MalformedURLException
     * @throws java.io.FileNotFoundException
     * @throws java.io.UnsupportedEncodingException
     * @throws javax.xml.transform.TransformerException
     */
    @Test(expectedExceptions = TransformerException.class)
    public void testTable_String_Boolean_TransformerException() throws MalformedURLException, FileNotFoundException, UnsupportedEncodingException, TransformerException {
        System.out.println("testTable_String_Boolean_TransformerException");
        XmlUtilities instance = new XmlUtilities();

        String testFile = XmlUtilitiesNGTest.class.getResource("resources/testTable_String_Boolean_TransformerException.xml").getPath();
        URL url = new File(testFile).toURI().toURL();
        instance.table(url.toString(), false);
    }

    private String removeWhitespacing(final String input) {
        return input != null ? input.replaceAll("\\s+", "") : null;
    }

    private Document getXmlDocument(final String filename, final boolean namespace) throws ParserConfigurationException, IOException, SAXException {
        final String testFile = XmlUtilitiesNGTest.class.getResource(filename).getPath();
        final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setNamespaceAware(namespace);
        final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        return docBuilder.parse(new File(testFile));
    }
}
