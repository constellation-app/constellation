/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.graph.schema.visual.attribute.io;

import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.ConnectionModeAttributeDescription;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.anyInt;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author serpens24
 */
public class ConnectionModeIOProviderNGTest {
    
    public ConnectionModeIOProviderNGTest() {
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
     * Test of getName method, of class ConnectionModeIOProvider.
     */
    @Test
    public void testGetName() {
        System.out.println("ConnectionModeIOProvider.getName");
        
        ConnectionModeIOProvider instance = new ConnectionModeIOProvider();
        String result = instance.getName();
        assertEquals(result, ConnectionModeAttributeDescription.ATTRIBUTE_NAME);
    }

    /**
     * Test of readObject method, of class ConnectionModeIOProvider.
     */
    @Test
    public void testReadObject() throws Exception {
        System.out.println("ConnectionModeIOProvider.readObject");
        
        final JsonNode mockJsonNode = mock(JsonNode.class);
        final GraphWriteMethods mockGraph = mock(GraphWriteMethods.class);
        
        int attributeId = 23;
        int elementId = 41;
        String attribValue = new String("TestAttrib");
        
        // Create object under test
        ConnectionModeIOProvider instance = new ConnectionModeIOProvider();
        
        // Create captors for arguments to graph.setStringValue
        final ArgumentCaptor<Integer> captorAtributeId = ArgumentCaptor.forClass(Integer.class);
        final ArgumentCaptor<Integer> captorElementId = ArgumentCaptor.forClass(Integer.class);
        final ArgumentCaptor<String> captorAtributeValue = ArgumentCaptor.forClass(String.class);
        
        // Call method under test with JsonNode set to returen isNull = true
        when(mockJsonNode.isNull()).thenReturn(true);
        when(mockJsonNode.textValue()).thenReturn(attribValue);
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraph, null, null, null, null);
        
        // Call method under test with JsonNode set to returen isNull = false
        when(mockJsonNode.isNull()).thenReturn(false);
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraph, null, null, null, null);
        
        // Verify calls to graph.setStringValue are as expected across 2 calls
        Mockito.verify(mockGraph, times(2)).setStringValue(captorAtributeId.capture(), captorElementId.capture(), captorAtributeValue.capture());
        assertEquals((int)captorAtributeId.getAllValues().get(0), attributeId);
        assertEquals((int)captorElementId.getAllValues().get(0), elementId);
        assertEquals(captorAtributeValue.getAllValues().get(0), null);
        assertEquals((int)captorAtributeId.getAllValues().get(1), attributeId);
        assertEquals((int)captorElementId.getAllValues().get(1), elementId);
        assertEquals(captorAtributeValue.getAllValues().get(1), attribValue); 
    }

    /**
     * Test of writeObject method, of class ConnectionModeIOProvider.
     */
    @Test
    public void testWriteObject() throws Exception {
        System.out.println("ConnectionModeIOProvider.writeObject");
        
        final JsonGenerator mockJsonGenerator = mock(JsonGenerator.class);
        final GraphReadMethods mockGraph = mock(GraphReadMethods.class);
        
        int attributeId = 23;
        int elementId = 41;
        String attrType = "attrType";
        String attrName = "attrName";
        String attrDesc = "attrDesc";
        String attrValue = "attrValue";

        GraphAttribute attr = new GraphAttribute(attributeId, GraphElementType.GRAPH, attrType, attrName, attrDesc,
            null, null);
       
        // Create object under test
        ConnectionModeIOProvider instance = new ConnectionModeIOProvider();
        
        // Create captors for arguments to graph.setStringValue
        final ArgumentCaptor<Integer> captorAtributeId = ArgumentCaptor.forClass(Integer.class);
        final ArgumentCaptor<Integer> captorElementId = ArgumentCaptor.forClass(Integer.class);
        final ArgumentCaptor<String> captorAttrName = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> captorAttrValue = ArgumentCaptor.forClass(String.class);
        
        // Test not verbose and graph.IsDefaultValue is true skips all processing
        when(mockGraph.isDefaultValue(anyInt(), anyInt())).thenReturn(true);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraph, null, false);
        Mockito.verify(mockGraph, times(0)).getStringValue(captorAtributeId.capture(), captorElementId.capture());
        
        // Now turn on verbose, and configure getStringValue to return null
        when(mockGraph.getStringValue(anyInt(), anyInt())).thenReturn(null);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraph, null, true);
        Mockito.verify(mockGraph, times(1)).getStringValue(captorAtributeId.capture(), captorElementId.capture());
        Mockito.verify(mockJsonGenerator, times(1)).writeNullField(captorAttrName.capture());
        Mockito.verify(mockJsonGenerator, times(0)).writeStringField(captorAttrName.capture(), captorAttrValue.capture());
        assertEquals((int)captorAtributeId.getAllValues().get(0), attributeId);
        assertEquals((int)captorElementId.getAllValues().get(0), elementId);
        assertEquals(captorAttrName.getAllValues().get(0), attrName);
        
        // Now turn verbose back off, but set graph.isDefaultValue to return false
        when(mockGraph.isDefaultValue(anyInt(), anyInt())).thenReturn(false);
        when(mockGraph.getStringValue(anyInt(), anyInt())).thenReturn(attrValue);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraph, null, false);
        Mockito.verify(mockGraph, times(2)).getStringValue(captorAtributeId.capture(), captorElementId.capture());
        Mockito.verify(mockJsonGenerator, times(1)).writeNullField(captorAttrName.capture());
        Mockito.verify(mockJsonGenerator, times(1)).writeStringField(captorAttrName.capture(), captorAttrValue.capture());
        assertEquals((int)captorAtributeId.getAllValues().get(0), attributeId);
        assertEquals((int)captorElementId.getAllValues().get(0), elementId);
        assertEquals(captorAttrName.getAllValues().get(0), attrName);
        assertEquals(captorAttrValue.getAllValues().get(0), attrValue);
    }
}
