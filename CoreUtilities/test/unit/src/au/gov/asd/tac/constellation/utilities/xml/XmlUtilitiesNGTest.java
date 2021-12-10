/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.utilities.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.openide.util.Exceptions;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author mmattner
 */
public class XmlUtilitiesNGTest {
    
    public XmlUtilitiesNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of newDocument method, of class XmlUtilities. This effectively creates a new empty 
     * document.
     */
    @Test
    public void testNewDocument() {
       
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
     */
    @Test
    public void testWrite_Document() throws Exception {
        System.out.println("write");
    }

    /**
     * Test of write method, of class XmlUtilities.
     */
    @Test
    public void testWrite_Document_File() throws Exception {
        System.out.println("write");
    }

    /**
     * Test of write method, of class XmlUtilities.
     */
    @Test
    public void testWrite_Document_OutputStream() throws Exception {
        System.out.println("write");
    }

    /**
     * Test of writeToString method, of class XmlUtilities.
     */
    @Test
    public void testWriteToString_Document() throws Exception {
        System.out.println("writeToString");
    }

    /**
     * Test of writeToString method, of class XmlUtilities.
     */
    @Test
    public void testWriteToString_InputStream_int() throws Exception {
        System.out.println("writeToString");
    }

    /**
     * Test of read method, of class XmlUtilities.
     */
    @Test
    public void testRead_File() throws Exception {
        // TODO: just experimenting with how reads work!!!!

        // TODO: temporarially creating a test ?XML file to read
        
        FileWriter fw = new FileWriter("testFile.xml");    
        fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
"<CATALOG>\n" +
"  <CD>\n" +
"    <TITLE>EmpireMMMMMM Burlesque</TITLE>\n" +
"    <ARTIST>Bob Dylan</ARTIST>\n" +
"    <COUNTRY>USA</COUNTRY>\n" +
"    <COMPANY>Columbia</COMPANY>\n" +
"    <PRICE>10.90</PRICE>\n" +
"    <YEAR>1985</YEAR>\n" +
"  </CD>\n" +
"</CATALOG>");
        fw.close();
        
        // TODO test that xml can be read using other code, this works, so try work out
        // why other version doesn't
        File file = new File("testFile.xml");  
        FileInputStream fis = new FileInputStream(file);
        Transformer transformer=TransformerFactory.newInstance().newTransformer();
        Source xmlInput=new StreamSource(fis);
        final StreamResult xmlOutput=new StreamResult(new StringWriter());
        final DOMResult rresult = new DOMResult();

        transformer.transform(xmlInput,rresult);
        System.out.println(rresult.getNode().getNodeName().toString());
        Document doc = (Document)rresult.getNode();
        XmlUtilities instance = new XmlUtilities();

//        Document expResult = null;
//        Document result = instance.read(file);
//        System.out.println(result.toString());
//        assertEquals(result, expResult);

    }

    /**
     * Test of read method, of class XmlUtilities.
     */
    @Test
    public void testRead_String() throws Exception {
        System.out.println("read");
    }

    /**
     * Test of read method, of class XmlUtilities.
     */
    @Test
    public void testRead_byteArr() throws Exception {
        System.out.println("read");
    }

    /**
     * Test of read method, of class XmlUtilities.
     */
    @Test
    public void testRead_InputStream_boolean() throws Exception {
        System.out.println("read");
    }

    /**
     * Test of getNode method, of class XmlUtilities.
     */
    @Test
    public void testGetNode() {
        System.out.println("getNode");

        try {
            // TODO Create a test XML file for now, may become test file
            FileWriter fw = new FileWriter("testFile.xml");
            fw.write("<parent>\n"
                    + "  <child1>child1_value</child1>\n"
                    + "  <child2>child2_value</child2>\n"
                    + "  <child3>child3a_value</child3>\n"
                    + "  <child3>child3b_value</child3>\n"
                    + "  <child4><child4.1>child4.1</child4.1></child4>\n"
                    + "</parent>");
            fw.close();

            // Read the test file into a Document object and extract parent node and list of children
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(new File("testFile.xml"));
            NodeList nodeList = document.getElementsByTagName("parent");
            Node parent = nodeList.item(0);
            NodeList children = parent.getChildNodes();
            
            // Get XmlUtilities instance
            XmlUtilities instance = new XmlUtilities();

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
            
        } catch (Exception ex) {
            fail("The test threw an unexpected exception.");
        }
    }

    /**
     * Test of getNodeNS method, of class XmlUtilities.
     */
    @Test
    public void testGetNodeNS() {
        System.out.println("getNodeNS");

        try {
            // TODO Create a test XML file for now, may become test file
            FileWriter fw = new FileWriter("testFile.xml");
            fw.write("<c:parent xmlns:c=\"http://www.consty.com/star\">\n"
                    + "  <c:child1>child1_value</c:child1>\n"
                    + "  <c:child2>child2_value</c:child2>\n"
                    + "  <c:child3>child3a_value</c:child3>\n"
                    + "  <c:child3>child3b_value</c:child3>\n"
                    + "  <c:child4><c:child4.1>child4.1</c:child4.1></c:child4>\n"
                    + "</c:parent>");
            fw.close();

            // Read the test file into a Document object and extract parent node and list of children
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilderFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(new File("testFile.xml"));
            NodeList nodeList = document.getElementsByTagName("c:parent");
            Node parent = nodeList.item(0);
            NodeList children = parent.getChildNodes();
            
            // Get XmlUtilities instance
            XmlUtilities instance = new XmlUtilities();

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
            
        } catch (Exception ex) {
            System.out.println("Exception:" + ex.toString());
            fail("The test threw an unexpected exception.");
        }
    }

    /**
     * Test of getNodes method, of class XmlUtilities.
     */
    @Test
    public void testGetNodes() {
        System.out.println("getNodes");

        try {
            // TODO Create a test XML file for now, may become test file
            FileWriter fw = new FileWriter("testFile.xml");
            fw.write("<parent>\n"
                    + "  <child>child_value1</child>\n"
                    + "  <child>child_value2</child>\n"
                    + "  <child>child_value3</child>\n"
                    + "  <nest_child><child_nest>child_nest_value</child_nest></nest_child>\n"
                    + "</parent>");
            fw.close();

            // Read the test file into a Document object and extract parent node and list of children
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(new File("testFile.xml"));
            NodeList nodeList = document.getElementsByTagName("parent");
            Node parent = nodeList.item(0);
            NodeList children = parent.getChildNodes();
            
            // Get XmlUtilities instance
            XmlUtilities instance = new XmlUtilities();
            
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
            
        } catch (Exception ex) {
            System.out.println("Exception:" + ex.toString());
            fail("The test threw an unexpected exception.");
        }
    }

    /**
     * Test of getNodesNS method, of class XmlUtilities.
     */
    @Test
    public void testGetNodesNS() {
        System.out.println("getNodesNS");

        try {
            // TODO Create a test XML file for now, may become test file
            FileWriter fw = new FileWriter("testFile.xml");
            fw.write("<c:parent xmlns:c=\"http://www.consty.com/star\" xmlns:c2=\"http://www.consty2.com/star2\">>\n"
                    + "  <c:child>child_value1</c:child>\n"
                    + "  <c:child>child_value2</c:child>\n"
                    + "  <child>child_value3</child>\n"
                    + "  <c2:child>child_value4</c2:child>\n"
                    + "  <c:nest_child><c:child_nest>child_nest_value</c:child_nest></c:nest_child>\n"
                    + "</c:parent>");
            fw.close();

            // Read the test file into a Document object and extract parent node and list of children
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilderFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(new File("testFile.xml"));
            NodeList nodeList = document.getElementsByTagName("c:parent");
            Node parent = nodeList.item(0);
            NodeList children = parent.getChildNodes();
            
            // Get XmlUtilities instance
            XmlUtilities instance = new XmlUtilities();

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
            

        } catch (Exception ex) {
            System.out.println("Exception:" + ex.toString());
            fail("The test threw an unexpected exception.");
        }
    }

    /**
     * Test of getNodeValue method, of class XmlUtilities.
     */
    @Test
    public void testGetNodeValue_Node() {
        System.out.println("getNodeValue");

        try {
            // TODO Create a test XML file for now, may become test file
            FileWriter fw = new FileWriter("testFile.xml");
            fw.write("<parent><child>child_value1</child></parent>");
            fw.close();

            // Read the test file into a Document object and extract parent node and list of children
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(new File("testFile.xml"));
            NodeList nodeList = document.getElementsByTagName("parent");
            Node parent = nodeList.item(0);
            NodeList children = parent.getChildNodes();
            
            // Get XmlUtilities instance
            XmlUtilities instance = new XmlUtilities();
            
            // Check that parent node doesnt contain content
            String result = instance.getNodeValue(parent);
            assertNull(result, "First child value matches expected");

            // Check that correct  child value is returned
            result = instance.getNodeValue(children.item(0));
            assertEquals(result, "child_value1", "First child value matches expected");
            
        } catch (Exception ex) {
            System.out.println("Exception:" + ex.toString());
            fail("The test threw an unexpected exception.");
        }
    }

    /**
     * Test of getNodeValue method, of class XmlUtilities.
     */
    @Test
    public void testGetNodeValue_String_NodeList() {
        System.out.println("getNodeValue");

        try {
            // TODO Create a test XML file for now, may become test file
            FileWriter fw = new FileWriter("testFile.xml");
            fw.write("<parent>\n"
                    + "  <child>child_value1</child>\n"
                    + "  <child>child_value2</child>\n"
                    + "  <child3>child_value3</child3>\n"
                    + "  <child4><child_nest>child_nest_value</child_nest></child4>\n"
                    + "  <child4>child_value4</child4>\n"
                    + "  <child5><child_nest>child_nest_value</child_nest></child5>\n"
                    + "</parent>");
            fw.close();

            // Read the test file into a Document object and extract parent node and list of children
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(new File("testFile.xml"));
            NodeList nodeList = document.getElementsByTagName("parent");
            Node parent = nodeList.item(0);
            NodeList children = parent.getChildNodes();
            
            // Get XmlUtilities instance
            XmlUtilities instance = new XmlUtilities();
            
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
            
        } catch (Exception ex) {
            System.out.println("Exception:" + ex.toString());
            fail("The test threw an unexpected exception.");
        }
    }

    /**
     * Test of getNodeValueNS method, of class XmlUtilities.
     */
    @Test
    public void testGetNodeValueNS() {
        System.out.println("getNodeValueNS");

        try {
            // TODO Create a test XML file for now, may become test file
            FileWriter fw = new FileWriter("testFile.xml");
            fw.write("<c:parent xmlns:c=\"http://www.consty.com/star\" xmlns:c2=\"http://www.consty2.com/star2\">>\n"
                    + "  <c:child>child_value1</c:child>\n"
                    + "  <c:child>child_value2</c:child>\n"
                    + "  <child>child_value3</child>\n"
                    + "  <c2:child>child_value4</c2:child>\n"
                    + "  <c:nest_child><c:child_nest>child_nest_value1</c:child_nest><c:child_nest>child_nest_value2</c:child_nest></c:nest_child>\n"
                    + "  <c:nest_child>second_nest_child</c:nest_child>\n"
                    + "</c:parent>");
            fw.close();

            // Read the test file into a Document object and extract parent node and list of children
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilderFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(new File("testFile.xml"));
            NodeList nodeList = document.getElementsByTagName("c:parent");
            Node parent = nodeList.item(0);
            NodeList children = parent.getChildNodes();
            
            // Get XmlUtilities instance
            XmlUtilities instance = new XmlUtilities();
            
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
        } catch (Exception ex) {
            System.out.println("Exception:" + ex.toString());
            fail("The test threw an unexpected exception.");
        }
    }

    /**
     * Test of getNodeAttr method, of class XmlUtilities.
     */
    @Test
    public void testGetNodeAttr_String_Node() {
        System.out.println("getNodeAttr");
    }

    /**
     * Test of getNodeAttr method, of class XmlUtilities.
     */
    @Test
    public void testGetNodeAttr_3args() {
        System.out.println("getNodeAttr");
    }

    /**
     * Test of getNodeAttrNS method, of class XmlUtilities.
     */
    @Test
    public void testGetNodeAttrNS() {
        System.out.println("getNodeAttrNS");
    }

    /**
     * Test of map method, of class XmlUtilities.
     */
    @Test
    public void testMap_String() throws Exception {
        System.out.println("map");
    }

    /**
     * Test of map method, of class XmlUtilities.
     */
    @Test
    public void testMap_String_String() throws Exception {
        System.out.println("map");
    }

    /**
     * Test of map method, of class XmlUtilities.
     */
    @Test
    public void testMap_Document_String() {
        System.out.println("map");
    }

    /**
     * Test of map method, of class XmlUtilities.
     */
    @Test
    public void testMap_Document() {
        System.out.println("map");
    }

    /**
     * Test of table method, of class XmlUtilities.
     */
    @Test
    public void testTable() throws Exception {
        System.out.println("table");
    }
    
}
