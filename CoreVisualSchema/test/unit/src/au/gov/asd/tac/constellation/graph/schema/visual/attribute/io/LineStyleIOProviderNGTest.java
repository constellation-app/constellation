/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.graph.schema.visual.attribute.io;

import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.LineStyleAttributeDescription;
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
public class LineStyleIOProviderNGTest {
    
    public LineStyleIOProviderNGTest() {
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
     * Test of getName method, of class LineStyleIOProvider.
     */
    @Test
    public void testGetName() {
        System.out.println("LineStyleIOProvider.testGetName");
        
        LineStyleIOProvider instance = new LineStyleIOProvider();
        String result = instance.getName();
        assertEquals(result, LineStyleAttributeDescription.ATTRIBUTE_NAME);
    }

    /**
     * Test of readObject method, of class LineStyleIOProvider.
     */
    @Test
    public void testReadObject() throws Exception {
        System.out.println("LineStyleIOProvider.testReadObject");
        
        // Create object under test
        LineStyleIOProvider instance = new LineStyleIOProvider();
        
        // Create mocks
        JsonNode mockJsonNode = mock(JsonNode.class);
        GraphWriteMethods mockGraph = mock(GraphWriteMethods.class);
        
        // Create argument captors
        ArgumentCaptor<Integer> captorAtributeId = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> captorElementId = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<String> captorString = ArgumentCaptor.forClass(String.class);
        
        int attributeId = 23;
        int elementId = 41;

        // Test case where jnode.isnull is true
        when(mockJsonNode.isNull()).thenReturn(true);
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraph, null, null, null, null);
        Mockito.verify(mockJsonNode, times(0)).textValue();
        Mockito.verify(mockGraph, times(1)).setStringValue(captorAtributeId.capture(), captorElementId.capture(), captorString.capture());
        assertEquals((int)captorAtributeId.getAllValues().get(0), attributeId);
        assertEquals((int)captorElementId.getAllValues().get(0), elementId);
        assertEquals(captorString.getAllValues().get(0), null);

        // Test case where jnode.isnull is false
        mockJsonNode = mock(JsonNode.class);
        mockGraph = mock(GraphWriteMethods.class);
        captorAtributeId = ArgumentCaptor.forClass(Integer.class);
        captorElementId = ArgumentCaptor.forClass(Integer.class);
        captorString = ArgumentCaptor.forClass(String.class);
        when(mockJsonNode.isNull()).thenReturn(false);
        when(mockJsonNode.textValue()).thenReturn("testvalue");
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraph, null, null, null, null);
        Mockito.verify(mockJsonNode, times(1)).textValue();
        Mockito.verify(mockGraph, times(1)).setStringValue(captorAtributeId.capture(), captorElementId.capture(), captorString.capture());
        assertEquals((int)captorAtributeId.getAllValues().get(0), attributeId);
        assertEquals((int)captorElementId.getAllValues().get(0), elementId);
        assertEquals(captorString.getAllValues().get(0), "testvalue");

        
    }

    /**
     * Test of writeObject method, of class LineStyleIOProvider.
     */
    @Test
    public void testWriteObject() throws Exception {
        System.out.println("LineStyleIOProvider.testWriteObject");
        
        // Create object under test
        LineStyleIOProvider instance = new LineStyleIOProvider();
        
        // Create mocks
        JsonGenerator mockJsonGenerator = mock(JsonGenerator.class);
        GraphWriteMethods mockGraph = mock(GraphWriteMethods.class);
        
        // Create argument captors
        final ArgumentCaptor<Integer> captorAtributeId = ArgumentCaptor.forClass(Integer.class);
        final ArgumentCaptor<Integer> captorElementId = ArgumentCaptor.forClass(Integer.class);
        final ArgumentCaptor<String> captorAttrName = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> captorField = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> captorValue = ArgumentCaptor.forClass(String.class);

        int attributeId = 23;
        int elementId = 41;
        String attrType = "attrType";
        String attrName = "attrName";
        String attrDesc = "attrDesc";
        GraphAttribute attr = new GraphAttribute(attributeId, GraphElementType.GRAPH, attrType, attrName, attrDesc,
            null, null);

        // Test not verbose and graph.IsDefaultValue is true skips all processing
        when(mockGraph.isDefaultValue(anyInt(), anyInt())).thenReturn(true);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraph, null, false);
        Mockito.verify(mockGraph, times(0)).getObjectValue(captorAtributeId.capture(), captorElementId.capture());

        // Test not verbose but graph.IsDefaultValue is false, graph.getObjectValue returns null
        when(mockGraph.isDefaultValue(anyInt(), anyInt())).thenReturn(false);
        when(mockGraph.getStringValue(anyInt(), anyInt())).thenReturn(null);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraph, null, false);
        Mockito.verify(mockGraph, times(1)).getStringValue(captorAtributeId.capture(), captorElementId.capture());
        Mockito.verify(mockJsonGenerator, times(1)).writeNullField(captorAttrName.capture());
        assertEquals((int)captorAtributeId.getValue(), attributeId);
        assertEquals((int)captorElementId.getValue(), elementId);
        assertEquals(captorAttrName.getValue(), attrName);
        
        // Test verbose and graph.IsDefaultValue is false, graph.getObjectValue returns null 
        mockJsonGenerator = mock(JsonGenerator.class);
        mockGraph = mock(GraphWriteMethods.class);
        when(mockGraph.isDefaultValue(anyInt(), anyInt())).thenReturn(false);
        when(mockGraph.getStringValue(anyInt(), anyInt())).thenReturn(null);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraph, null, true);
        Mockito.verify(mockGraph, times(1)).getStringValue(captorAtributeId.capture(), captorElementId.capture());
        Mockito.verify(mockJsonGenerator, times(1)).writeNullField(captorAttrName.capture());
        assertEquals((int)captorAtributeId.getValue(), attributeId);
        assertEquals((int)captorElementId.getValue(), elementId);
        assertEquals(captorAttrName.getValue(), attrName);
        
        // Test verbose and graph.IsDefaultValue is true, graph.getObjectValue returns decotrator object
        when(mockGraph.isDefaultValue(anyInt(), anyInt())).thenReturn(true);
        when(mockGraph.getStringValue(anyInt(), anyInt())).thenReturn("teststring");
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraph, null, true);
        Mockito.verify(mockJsonGenerator, times(1)).writeStringField(captorField.capture(), captorValue.capture());
        assertEquals(captorField.getAllValues().get(0), attrName);
        assertEquals(captorValue.getAllValues().get(0), "teststring");
    }
    
}
