/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.graph.schema.visual.attribute.io;

import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.VertexDecorators;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.DecoratorsAttributeDescription;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
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
public class DecoratorsIOProviderNGTest {
    
    public DecoratorsIOProviderNGTest() {
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
     * Test of getName method, of class DecoratorsIOProvider.
     */
    @Test
    public void testGetName() {
        System.out.println("DecoratorsIOProvider.testGetName");
        
        DecoratorsIOProvider instance = new DecoratorsIOProvider();
        String result = instance.getName();
        assertEquals(result, DecoratorsAttributeDescription.ATTRIBUTE_NAME);
    }

    /**
     * Test of readObject method, of class DecoratorsIOProvider.
     */
    @Test
    public void testReadObject() throws Exception {
        System.out.println("DecoratorsIOProvider.testReadObject");
        
        // Create object under test
        DecoratorsIOProvider instance = new DecoratorsIOProvider();
        
        // Create mocks
        final JsonNode mockJsonNode = mock(JsonNode.class);
        GraphWriteMethods mockGraph = mock(GraphWriteMethods.class);
        
        // Create argument captors
        ArgumentCaptor<Integer> captorAtributeId = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> captorElementId = ArgumentCaptor.forClass(Integer.class);
        final ArgumentCaptor<VertexDecorators> captorObjectAttributeValue = ArgumentCaptor.forClass(VertexDecorators.class);
        ArgumentCaptor<String> captorStringAttributeValue = ArgumentCaptor.forClass(String.class);
        
        int attributeId = 23;
        int elementId = 41;
        
        // Call method under test with JsonNode.isNull=false and JsonNode.isObject=true
        when(mockJsonNode.isNull()).thenReturn(false);
        when(mockJsonNode.isObject()).thenReturn(true);
        JsonNode decoratorCoordinate = new TextNode("decorator");
        when(mockJsonNode.get(anyString())).thenReturn(decoratorCoordinate);
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraph, null, null, null, null);
        Mockito.verify(mockGraph, times(1)).setObjectValue(captorAtributeId.capture(), captorElementId.capture(), captorObjectAttributeValue.capture());
        assertEquals((int)captorAtributeId.getAllValues().get(0), attributeId);
        assertEquals((int)captorElementId.getAllValues().get(0), elementId);
        assertEquals(captorObjectAttributeValue.getAllValues().get(0).getNorthEastDecoratorAttribute(), "decorator");
        assertEquals(captorObjectAttributeValue.getAllValues().get(0).getNorthWestDecoratorAttribute(), "decorator");
        assertEquals(captorObjectAttributeValue.getAllValues().get(0).getSouthEastDecoratorAttribute(), "decorator");
        assertEquals(captorObjectAttributeValue.getAllValues().get(0).getSouthWestDecoratorAttribute(), "decorator");
         
        // Call method under test with JsonNode.isNull=false and JsonNode.isObject=false
        mockGraph = mock(GraphWriteMethods.class);
        captorAtributeId = ArgumentCaptor.forClass(Integer.class);
        captorElementId = ArgumentCaptor.forClass(Integer.class);
        when(mockJsonNode.isObject()).thenReturn(false);
        when(mockJsonNode.textValue()).thenReturn("Test");
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraph, null, null, null, null);
        Mockito.verify(mockGraph, times(1)).setStringValue(captorAtributeId.capture(), captorElementId.capture(), captorStringAttributeValue.capture());
        assertEquals((int)captorAtributeId.getAllValues().get(0), attributeId);
        assertEquals((int)captorElementId.getAllValues().get(0), elementId);
        assertEquals(captorStringAttributeValue.getAllValues().get(0), "Test");

        // Call method under test with JsonNode.isNull=true and JsonNode.isObject=false
        mockGraph = mock(GraphWriteMethods.class);
        captorAtributeId = ArgumentCaptor.forClass(Integer.class);
        captorElementId = ArgumentCaptor.forClass(Integer.class);
        captorStringAttributeValue = ArgumentCaptor.forClass(String.class);
        when(mockJsonNode.isNull()).thenReturn(true);
        when(mockJsonNode.isObject()).thenReturn(false);
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraph, null, null, null, null);
        Mockito.verify(mockGraph, times(1)).setStringValue(captorAtributeId.capture(), captorElementId.capture(), captorStringAttributeValue.capture());
        assertEquals((int)captorAtributeId.getAllValues().get(0), attributeId);
        assertEquals((int)captorElementId.getAllValues().get(0), elementId);
        assertEquals(captorStringAttributeValue.getAllValues().get(0), null);

        // Call method under test with JsonNode.isNull=true and JsonNode.isObject=true
        mockGraph = mock(GraphWriteMethods.class);
        captorAtributeId = ArgumentCaptor.forClass(Integer.class);
        captorElementId = ArgumentCaptor.forClass(Integer.class);
        captorStringAttributeValue = ArgumentCaptor.forClass(String.class);
        when(mockJsonNode.isNull()).thenReturn(true);
        when(mockJsonNode.isObject()).thenReturn(true);
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraph, null, null, null, null);
        Mockito.verify(mockGraph, times(1)).setStringValue(captorAtributeId.capture(), captorElementId.capture(), captorStringAttributeValue.capture());
        assertEquals((int)captorAtributeId.getAllValues().get(0), attributeId);
        assertEquals((int)captorElementId.getAllValues().get(0), elementId);
        assertEquals(captorStringAttributeValue.getAllValues().get(0), null);
    }

    /**
     * Test of writeObject method, of class DecoratorsIOProvider.
     */
    @Test
    public void testWriteObject() throws Exception {
        System.out.println("DecoratorsIOProvider.testWriteObject");
        
        // Create object under test
        DecoratorsIOProvider instance = new DecoratorsIOProvider();
        
        // Create mocks
        JsonGenerator mockJsonGenerator = mock(JsonGenerator.class);
        GraphWriteMethods mockGraph = mock(GraphWriteMethods.class);
        
        // Create argument captors
        final ArgumentCaptor<Integer> captorAtributeId = ArgumentCaptor.forClass(Integer.class);
        final ArgumentCaptor<Integer> captorElementId = ArgumentCaptor.forClass(Integer.class);
        final ArgumentCaptor<String> captorAttrName = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> captorFieldStart = ArgumentCaptor.forClass(String.class);
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
        when(mockGraph.getObjectValue(anyInt(), anyInt())).thenReturn(null);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraph, null, false);
        Mockito.verify(mockGraph, times(1)).getObjectValue(captorAtributeId.capture(), captorElementId.capture());
        Mockito.verify(mockJsonGenerator, times(1)).writeNullField(captorAttrName.capture());
        assertEquals((int)captorAtributeId.getValue(), attributeId);
        assertEquals((int)captorElementId.getValue(), elementId);
        
        // Test verbose and graph.IsDefaultValue is false, graph.getObjectValue returns null 
        mockJsonGenerator = mock(JsonGenerator.class);
        mockGraph = mock(GraphWriteMethods.class);
        when(mockGraph.isDefaultValue(anyInt(), anyInt())).thenReturn(false);
        when(mockGraph.getObjectValue(anyInt(), anyInt())).thenReturn(null);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraph, null, true);
        Mockito.verify(mockGraph, times(1)).getObjectValue(captorAtributeId.capture(), captorElementId.capture());
        Mockito.verify(mockJsonGenerator, times(1)).writeNullField(captorAttrName.capture());
        assertEquals((int)captorAtributeId.getValue(), attributeId);
        assertEquals((int)captorElementId.getValue(), elementId);
        
        // Test verbose and graph.IsDefaultValue is true, graph.getObjectValue returns decotrator object
        when(mockGraph.isDefaultValue(anyInt(), anyInt())).thenReturn(true);
        when(mockGraph.getObjectValue(anyInt(), anyInt())).thenReturn(new VertexDecorators("NW", "NE", "SE", "SW"));
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraph, null, true);
        Mockito.verify(mockJsonGenerator, times(1)).writeObjectFieldStart(captorFieldStart.capture());
        Mockito.verify(mockJsonGenerator, times(4)).writeStringField(captorField.capture(), captorValue.capture());
       assertEquals(captorFieldStart.getAllValues().get(0), attrName);
       assertEquals(captorField.getAllValues().get(0), "north_west");
       assertEquals(captorValue.getAllValues().get(0), "NW");
       assertEquals(captorField.getAllValues().get(1), "north_east");
       assertEquals(captorValue.getAllValues().get(1), "NE");
       assertEquals(captorField.getAllValues().get(2), "south_east");
       assertEquals(captorValue.getAllValues().get(2), "SE");
       assertEquals(captorField.getAllValues().get(3), "south_west");
       assertEquals(captorValue.getAllValues().get(3), "SW");
    } 
}
