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
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.LayerNameAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.LayerName;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.IOException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author serpens24
 */
public class LayerNameIOProviderNGTest {
        
    // Create object under test
    LayerNameIOProvider instance;

    // Define mocks
    GraphReadMethods mockGraphReadMethods;
    GraphWriteMethods mockGraphWriteMethods;
    JsonNode mockJsonNode;
    JsonGenerator mockJsonGenerator;
    
    // Test variables
    int attributeId = 23;
    int elementId = 41;
    GraphAttribute attr = new GraphAttribute(attributeId, GraphElementType.GRAPH, "attrType", "attrName", "attrDesc", null, null);
    
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
        instance = new LayerNameIOProvider();
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }
    
    /**
     * Perform reset of all mocks and argument captors to ensure clean test steps.
     */
    public void resetMocking() {
        mockGraphReadMethods = mock(GraphReadMethods.class);
        mockGraphWriteMethods = mock(GraphWriteMethods.class);
        mockJsonNode = mock(JsonNode.class);
        mockJsonGenerator = mock(JsonGenerator.class);
    }

    /**
     * Test of getName method, of class LayerNameIOProvider.
     */
    @Test
    public void testGetName() {
        System.out.println("LayerNameIOProviderNGTest.testGetName");
        assertEquals(instance.getName(), LayerNameAttributeDescription.ATTRIBUTE_NAME);
    }

    /**
     * Test of readObject method, of class LayerNameIOProvider.
     * @throws java.io.IOException
     */
    @Test
    public void testReadObject() throws IOException {
        System.out.println("LayerNameIOProviderNGTest.testReadObject");

        // Call method under test with JsonNode.isNull=true, name and layer tags found
        resetMocking();
        when(mockJsonNode.isNull()).thenReturn(true);
        when(mockJsonNode.has(anyString())).thenReturn(true);
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraphWriteMethods, null, null, null, null);
        Mockito.verify(mockJsonNode, times(0)).get(any());
    
        // Call method under test with JsonNode.isNull=true, name tag not found
        resetMocking();
        when(mockJsonNode.isNull()).thenReturn(true);
        when(mockJsonNode.has("name")).thenReturn(false);
        when(mockJsonNode.has("layer")).thenReturn(true);
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraphWriteMethods, null, null, null, null);
        Mockito.verify(mockJsonNode, times(0)).get(any());
    
        // Call method under test with JsonNode.isNull=true, layer tag not found
        resetMocking();
        when(mockJsonNode.isNull()).thenReturn(true);
        when(mockJsonNode.has("name")).thenReturn(true);
        when(mockJsonNode.has("layer")).thenReturn(false);
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraphWriteMethods, null, null, null, null);
        Mockito.verify(mockJsonNode, times(0)).get(any());
    
        // Call method under test with JsonNode.isNull=true, name and layers tag not found
        resetMocking();
        when(mockJsonNode.isNull()).thenReturn(true);
        when(mockJsonNode.has("name")).thenReturn(false);
        when(mockJsonNode.has("layer")).thenReturn(false);
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraphWriteMethods, null, null, null, null);
        Mockito.verify(mockJsonNode, times(0)).get(any());
    
        // Call method under test with JsonNode.isNull=false, name and layers tag not found
        resetMocking();
        when(mockJsonNode.isNull()).thenReturn(false);
        when(mockJsonNode.has("name")).thenReturn(false);
        when(mockJsonNode.has("layer")).thenReturn(false);
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraphWriteMethods, null, null, null, null);
        Mockito.verify(mockJsonNode, times(0)).get(any());
    
        // Call method under test with JsonNode.isNull=false, name tag not found
        resetMocking();
        when(mockJsonNode.isNull()).thenReturn(false);
        when(mockJsonNode.has("name")).thenReturn(false);
        when(mockJsonNode.has("layer")).thenReturn(true);
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraphWriteMethods, null, null, null, null);
        Mockito.verify(mockJsonNode, times(0)).get(any());
    
        // Call method under test with JsonNode.isNull=false, layer tag not found
        resetMocking();
        when(mockJsonNode.isNull()).thenReturn(false);
        when(mockJsonNode.has("name")).thenReturn(true);
        when(mockJsonNode.has("layer")).thenReturn(false);
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraphWriteMethods, null, null, null, null);
        Mockito.verify(mockJsonNode, times(0)).get(any());
    
        // Call method under test with JsonNode.isNull=false, name and layer tags found
        resetMocking();
        when(mockJsonNode.isNull()).thenReturn(false);
        when(mockJsonNode.has(anyString())).thenReturn(true);
        when(mockJsonNode.get("name")).thenReturn(new TextNode("name"));
        when(mockJsonNode.get("layer")).thenReturn(new IntNode(23));
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraphWriteMethods, null, null, null, null);
        Mockito.verify(mockJsonNode, times(1)).get("name");
        Mockito.verify(mockJsonNode, times(1)).get("layer");
        Mockito.verify(mockGraphWriteMethods, times(1)).setObjectValue(attributeId, elementId, new LayerName(23, "name"));
    }

    /**
     * Test of writeObject method, of class LayerNameIOProvider.
     * @throws java.io.IOException
     */
    @Test
    public void testWriteObject() throws IOException {
        System.out.println("LayerNameIOProviderNGTest.testWriteObject");

        // Test not verbose and graph.IsDefaultValue is true skips all processing
        resetMocking();
        when(mockGraphReadMethods.isDefaultValue(anyInt(), anyInt())).thenReturn(true);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraphReadMethods, null, false);
        Mockito.verify(mockGraphReadMethods, times(0)).getObjectValue(anyInt(), anyInt());

        // Test verbose, graph.IsDefaultValue is true, graph.getObjectValue returns null
        resetMocking();
        when(mockGraphReadMethods.isDefaultValue(anyInt(), anyInt())).thenReturn(true);
        when(mockGraphReadMethods.getObjectValue(anyInt(), anyInt())).thenReturn(null);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraphReadMethods, null, true);
        Mockito.verify(mockGraphReadMethods, times(1)).getObjectValue(attributeId, elementId);
        Mockito.verify(mockJsonGenerator, times(1)).writeNullField(attr.getName());
        Mockito.verify(mockJsonGenerator, times(0)).writeObjectFieldStart(any());

        // Test verbose, graph.IsDefaultValue is false, graph.getObjectValue returns LayerName object
        resetMocking();
        when(mockGraphReadMethods.isDefaultValue(anyInt(), anyInt())).thenReturn(false);
        when(mockGraphReadMethods.getObjectValue(anyInt(), anyInt())).thenReturn(new LayerName(23, "name"));
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraphReadMethods, null, true);
        Mockito.verify(mockGraphReadMethods, times(1)).getObjectValue(attributeId, elementId);
        Mockito.verify(mockJsonGenerator, times(0)).writeNullField(attr.getName());
        Mockito.verify(mockJsonGenerator, times(1)).writeObjectFieldStart(any());
        Mockito.verify(mockJsonGenerator, times(1)).writeStringField("name", "name");
        Mockito.verify(mockJsonGenerator, times(1)).writeNumberField("layer", 23);
        Mockito.verify(mockJsonGenerator, times(1)).writeEndObject();

        // Test not verbose, graph.IsDefaultValue is false, graph.getObjectValue returns null
        resetMocking();
        when(mockGraphReadMethods.isDefaultValue(anyInt(), anyInt())).thenReturn(false);
        when(mockGraphReadMethods.getObjectValue(anyInt(), anyInt())).thenReturn(null);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraphReadMethods, null, false);
        Mockito.verify(mockGraphReadMethods, times(1)).getObjectValue(attributeId, elementId);
        Mockito.verify(mockJsonGenerator, times(1)).writeNullField(attr.getName());
        Mockito.verify(mockJsonGenerator, times(0)).writeObjectFieldStart(any());
    }
}
