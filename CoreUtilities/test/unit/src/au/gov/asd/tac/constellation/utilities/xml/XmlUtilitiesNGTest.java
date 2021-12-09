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
            fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<parent>\n"
                    + "  <child1>child1_value</child1>\n"
                    + "  <child2>child2_value</child2>\n"
                    + "  <child3>child3_value</child3>\n"
                    + "  <child4><child4.1>child4.1</child4.1></child4>\n"
                    + "</parent>");
            fw.close();

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(new File("testFile.xml"));
            NodeList nodeList = document.getElementsByTagName("parent");
            Node parent = nodeList.item(0);
            NodeList children = parent.getChildNodes();
            
            XmlUtilities instance = new XmlUtilities();
            Node node = instance.getNode("child3", children);
            assertNotNull(node, "Found node");
            assertEquals(node.getNodeName(), "child3", "Node name matches");
            assertEquals(node.getChildNodes().getLength(), 1, "Node has one child element");
            assertEquals(node.getChildNodes().item(0).getNodeValue(), "child3_value", "Node value matches");

            node = instance.getNode("CHILD3", children);
            assertNotNull(node, "Found node (case insensitive)");
            assertEquals(node.getNodeName(), "child3", "Node name matches (case insensitive)");
            
            node = instance.getNode("missing_child", children);
            assertNull(node, "Can't find node");
            
            node = instance.getNode("child4.1", children);
            assertNull(node, "Doesn't find nested node");
            
        } catch (Exception ex) {
            fail("The test through an unexpected exception.");
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
            fw.write(""         //"<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
                    + "<c:parent xmlns:c=\"http://www.consty.com/star\">\n"
                    + "  <c:child1>child1_value</c:child1>\n"
                    + "  <c:child2>child2_value</c:child2>\n"
                    + "  <c:child3>child3_value</c:child3>\n"
                    + "  <c:child4><c:child4.1>child4.1</c:child4.1></c:child4>\n"
                    + "</c:parent>");
            fw.close();

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilderFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(new File("testFile.xml"));
            
            NodeList nodeList = document.getElementsByTagName("c:parent");
            Node parent = nodeList.item(0);
            System.out.println("parent:" + parent.getNamespaceURI());
            NodeList children = parent.getChildNodes();
            
            XmlUtilities instance = new XmlUtilities();
            Node node = instance.getNodeNS("http://www.consty.com/star", "child3", children);
            assertNotNull(node, "Found node");
            assertEquals(node.getNodeName(), "c:child3", "Node name matches");
            assertEquals(node.getChildNodes().getLength(), 1, "Node has one child element");
            assertEquals(node.getChildNodes().item(0).getNodeValue(), "child3_value", "Node value matches");

            node = instance.getNodeNS("http://www.consty.com/star", "CHILD3", children);
            assertNotNull(node, "Found node (case insensitive)");
            assertEquals(node.getNodeName(), "c:child3", "Node name matches (case insensitive)");
            
            node = instance.getNodeNS("http://www.consty.com/star", "missing_child", children);
            assertNull(node, "Can't find node");
            
            node = instance.getNodeNS("http://www.consty.com/star", "child4.1", children);
            assertNull(node, "Doesn't find nested node");
            
            node = instance.getNodeNS("http://www.notconsty.com/star", "child3", children);
            assertNull(node, "Doesn't find if invalid namespace");
            
        } catch (Exception ex) {
            System.out.println("Exception:" + ex.toString());
            fail("The test through an unexpected exception.");
        }
    }

    /**
     * Test of getNodes method, of class XmlUtilities.
     */
    @Test
    public void testGetNodes() {
        System.out.println("getNodes");
    }

    /**
     * Test of getNodesNS method, of class XmlUtilities.
     */
    @Test
    public void testGetNodesNS() {
        System.out.println("getNodesNS");
    }

    /**
     * Test of getNodeValue method, of class XmlUtilities.
     */
    @Test
    public void testGetNodeValue_Node() {
        System.out.println("getNodeValue");
    }

    /**
     * Test of getNodeValue method, of class XmlUtilities.
     */
    @Test
    public void testGetNodeValue_String_NodeList() {
        System.out.println("getNodeValue");
    }

    /**
     * Test of getNodeValueNS method, of class XmlUtilities.
     */
    @Test
    public void testGetNodeValueNS() {
        System.out.println("getNodeValueNS");
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
