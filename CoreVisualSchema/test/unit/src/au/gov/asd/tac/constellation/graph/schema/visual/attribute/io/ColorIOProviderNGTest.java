/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.graph.schema.visual.attribute.io;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteReader;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteWriter;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.ColorAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.IconAttributeDescription;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.datastructure.ImmutableObjectCache;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.FloatNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.ObjectUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mockito;
import static org.mockito.Mockito.doNothing;
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
public class ColorIOProviderNGTest {
    
    public ColorIOProviderNGTest() {
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
     * Test of getName method, of class ColorIOProvider.
     */
    @Test
    public void testGetName() {
        System.out.println("ColorIOProvider.getName");
        
        ColorIOProvider instance = new ColorIOProvider();
        String result = instance.getName();
        assertEquals(result, ColorAttributeDescription.ATTRIBUTE_NAME);
    }

    /**
     * Test of readObject method, of class ColorIOProvider.
     */
    @Test
    public void testReadObject() throws Exception {
        System.out.println("ColorIOProvider.testReadObject");
        
        final JsonNode mockJsonNode = mock(JsonNode.class);
        final GraphWriteMethods mockGraph = mock(GraphWriteMethods.class);
        final ImmutableObjectCache mockCache = mock(ImmutableObjectCache.class);
        
        int attributeId = 23;
        int elementId = 41;
        ConstellationColor redAttribValue = ConstellationColor.getColorValue("Red");
        ConstellationColor tealAttribValue = ConstellationColor.getColorValue("Teal");
        
        // Create object under test
        ColorIOProvider instance = new ColorIOProvider();
        
        // Create captors for arguments to graph.setStringValue
        final ArgumentCaptor<Integer> captorAtributeId = ArgumentCaptor.forClass(Integer.class);
        final ArgumentCaptor<Integer> captorElementId = ArgumentCaptor.forClass(Integer.class);
        final ArgumentCaptor<ConstellationColor> captorAtributeValue = ArgumentCaptor.forClass(ConstellationColor.class);
        final ArgumentCaptor<String> captorStringAtributeValue = ArgumentCaptor.forClass(String.class);

        // Call method under test with JsonNode.isNull=false and JsonNode.isObject=true
        when(mockJsonNode.isNull()).thenReturn(false);
        when(mockJsonNode.isObject()).thenReturn(true);
        
        when(mockJsonNode.has(anyString())).thenReturn(true);
        
        JsonNode color = new TextNode("red");
        when(mockJsonNode.get(anyString())).thenReturn(color);
        when(mockCache.deduplicate(anyObject())).thenReturn(redAttribValue);
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraph, null, null, null, mockCache);
        
        when(mockJsonNode.has(anyString())).thenReturn(false);
        when(mockJsonNode.get("red")).thenReturn(new FloatNode(0.0f));
        when(mockJsonNode.get("green")).thenReturn(new FloatNode(0.5f));
        when(mockJsonNode.get("blue")).thenReturn(new FloatNode(0.5f));
        when(mockJsonNode.get("alpha")).thenReturn(new FloatNode(1.0f));
        when(mockCache.deduplicate(anyObject())).thenReturn(tealAttribValue);
 
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraph, null, null, null, mockCache);
        
        Mockito.verify(mockGraph, times(2)).setObjectValue(captorAtributeId.capture(), captorElementId.capture(), captorAtributeValue.capture());
        assertEquals((int)captorAtributeId.getAllValues().get(0), attributeId);
        assertEquals((int)captorElementId.getAllValues().get(0), elementId);
        assertEquals(captorAtributeValue.getAllValues().get(0).toString(), "Red");
        assertEquals((int)captorAtributeId.getAllValues().get(1), attributeId);
        assertEquals((int)captorElementId.getAllValues().get(1), elementId);
        assertEquals(captorAtributeValue.getAllValues().get(1).toString(), "Teal");       
        
        // Call method under test with JsonNode.isNull=false and JsonNode.isObject=false
        // TODO this is hitting case where jnode.tx
        when(mockJsonNode.isObject()).thenReturn(false);
        when(mockJsonNode.textValue()).thenReturn("Test");
        when(mockCache.deduplicate(anyObject())).thenReturn("Null");
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraph, null, null, null, mockCache);
        Mockito.verify(mockGraph, times(1)).setStringValue(captorAtributeId.capture(), captorElementId.capture(), captorStringAtributeValue.capture());
        assertEquals((int)captorAtributeId.getAllValues().get(0), attributeId);
        assertEquals((int)captorElementId.getAllValues().get(0), elementId);
        assertEquals(captorStringAtributeValue.getAllValues().get(0), "Null");  
                
        // Call method under test with JsonNode.isNull=true and JsonNode.isObject=false
        when(mockJsonNode.isNull()).thenReturn(true);
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraph, null, null, null, mockCache);
        
        // Call method under test with JsonNode.isNull=true and JsonNode.isObject=true
        when(mockJsonNode.isObject()).thenReturn(true);
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraph, null, null, null, mockCache);
    }

    /**
     * Test of writeObject method, of class ColorIOProvider.
     */
    @Test
    public void testWriteObject() throws Exception {
        System.out.println("ColorIOProvider.testWriteObject");
        
        final JsonGenerator mockJsonGenerator = mock(JsonGenerator.class);
        final GraphReadMethods mockGraph = mock(GraphReadMethods.class);
        
        int attributeId = 23;
        int elementId = 41;
        String attrType = "attrType";
        String attrName = "attrName";
        String attrDesc = "attrDesc";
        String attrValue = "GREY";

        GraphAttribute attr = new GraphAttribute(attributeId, GraphElementType.GRAPH, attrType, attrName, attrDesc,
            null, null);
        
        // Create object under test
        ColorIOProvider instance = new ColorIOProvider();
        
        // Create captors for arguments to graph.setStringValue
        final ArgumentCaptor<Integer> captorAtributeId = ArgumentCaptor.forClass(Integer.class);
        final ArgumentCaptor<Integer> captorElementId = ArgumentCaptor.forClass(Integer.class);
        final ArgumentCaptor<String> captorAttrName = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> captorAttrValue = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> captorFieldStart = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> captorColorField = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> captorColorValue = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Float> captorColorFloatValue = ArgumentCaptor.forClass(Float.class);

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

        // Now turn verbose back off, but set graph.isDefaultValue to return false. Set color to a known color (Grey)
        when(mockGraph.isDefaultValue(anyInt(), anyInt())).thenReturn(false);
        when(mockGraph.getStringValue(anyInt(), anyInt())).thenReturn(attrValue);
        doNothing().when(mockJsonGenerator).writeObjectFieldStart(anyString());
        doNothing().when(mockJsonGenerator).writeStringField(anyString(), anyString());
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraph, null, false);
        Mockito.verify(mockGraph, times(2)).getStringValue(captorAtributeId.capture(), captorElementId.capture());
        Mockito.verify(mockJsonGenerator, times(1)).writeNullField(captorAttrName.capture());
        Mockito.verify(mockJsonGenerator, times(1)).writeStringField(captorAttrName.capture(), captorAttrValue.capture());
        Mockito.verify(mockJsonGenerator, times(1)).writeObjectFieldStart(captorFieldStart.capture());
        Mockito.verify(mockJsonGenerator, times(1)).writeStringField(captorColorField.capture(), captorColorValue.capture());
        assertEquals(captorFieldStart.getAllValues().get(0), attrName);
        assertEquals(captorColorField.getAllValues().get(0), "name");
        assertEquals(captorColorValue.getAllValues().get(0), "Grey");

        
        attrValue = "RGB255255255";
        doNothing().when(mockJsonGenerator).writeNumberField(anyString(), anyFloat());
        when(mockGraph.getStringValue(anyInt(), anyInt())).thenReturn(attrValue);
        // Repeat but set color to be RGB color
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraph, null, false);
        Mockito.verify(mockJsonGenerator, times(4)).writeNumberField(captorColorField.capture(), captorColorFloatValue.capture());
    }

    /**
     * Test of readColorObject method, of class ColorIOProvider.
     */
    @Test
    public void testReadColorObject() {
        System.out.println("ColorIOProvider.readColorObject");
    }

    /**
     * Test of writeColorObject method, of class ColorIOProvider.
     */
    @Test
    public void testWriteColorObject() throws Exception {
        System.out.println("ColorIOProvider.writeColorObject");
    }
    
}
