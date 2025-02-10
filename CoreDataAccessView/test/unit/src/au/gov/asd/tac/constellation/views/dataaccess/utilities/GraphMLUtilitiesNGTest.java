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
package au.gov.asd.tac.constellation.views.dataaccess.utilities;

import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import java.util.HashMap;
import java.util.Map;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author antares
 */
public class GraphMLUtilitiesNGTest {
    
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
     * Test of addAttributes method, of class GraphMLUtilities.
     */
    @Test
    public void testAddAttributes() {
        System.out.println("addAttributes");
        
        final Node node = mock(Node.class);
        final NodeList childNodes = mock(NodeList.class);
        
        final Node firstChild = mock(Node.class);
        final NamedNodeMap firstChildAttributes = mock(NamedNodeMap.class);
        final Node firstChildKey = mock(Node.class);
        
        final Node secondChild = mock(Node.class);
        final NamedNodeMap secondChildAttributes = mock(NamedNodeMap.class);
        final Node secondChildKey = mock(Node.class);
        
        when(node.getChildNodes()).thenReturn(childNodes);        
        when(childNodes.getLength()).thenReturn(2);
        when(childNodes.item(0)).thenReturn(firstChild);
        when(childNodes.item(1)).thenReturn(secondChild);
        
        when(firstChild.getNodeName()).thenReturn("data");
        when(firstChild.getAttributes()).thenReturn(firstChildAttributes);
        when(firstChild.getTextContent()).thenReturn("a value");
        when(firstChildAttributes.getNamedItem("key")).thenReturn(firstChildKey);
        when(firstChildKey.getNodeValue()).thenReturn("key1");
        
        when(secondChild.getNodeName()).thenReturn("data");
        when(secondChild.getAttributes()).thenReturn(secondChildAttributes);
        when(secondChild.getTextContent()).thenReturn("true");
        when(secondChildAttributes.getNamedItem("key")).thenReturn(secondChildKey);
        when(secondChildKey.getNodeValue()).thenReturn("key2");
        
        final Map<String, String> nodeAttributes = new HashMap<>();
        nodeAttributes.put("key1", "attribute1,string");
        nodeAttributes.put("key2", "attribute2,boolean");
        
        final String element = "transaction.";
        final RecordStore result = new GraphRecordStore();
        result.add();
        
        GraphMLUtilities.addAttributes(node, nodeAttributes, result, element);
        //confirm the expected attributes (and attribute values) were added to the record store
        assertEquals(result.get(element + "attribute1"), "a value");
        assertEquals(result.get(element + "attribute2"), "true");
    }

    /**
     * Test of addAttribute method, of class GraphMLUtilities.
     */
    @Test
    public void testAddAttribute() {
        System.out.println("addAttribute");
        
        final RecordStore result = new GraphRecordStore();
        final String element = "transaction.";
        final String attrName = "myAttribute";
                
        result.add();
        GraphMLUtilities.addAttribute(result, element, "boolean", attrName, "true");
        assertEquals(result.get(element + attrName), "true");
        
        GraphMLUtilities.addAttribute(result, element, "int", attrName, "42");
        assertEquals(result.get(element + attrName), "42");
        
        GraphMLUtilities.addAttribute(result, element, "long", attrName, "42");
        assertEquals(result.get(element + attrName), "42");
        
        GraphMLUtilities.addAttribute(result, element, "float", attrName, "42.0");
        assertEquals(result.get(element + attrName), "42.0");
        
        GraphMLUtilities.addAttribute(result, element, "double", attrName, "42.0");
        assertEquals(result.get(element + attrName), "42.0");
        
        GraphMLUtilities.addAttribute(result, element, "string", attrName, "test string");
        assertEquals(result.get(element + attrName), "test string");
    }
}