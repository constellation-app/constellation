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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute.io;

import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.ColorAttributeDescription;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.datastructure.ImmutableObjectCache;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.FloatNode;
import com.fasterxml.jackson.databind.node.TextNode;
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
public final class ColorIOProviderNGTest {
        
    // Create object under test
    ColorIOProvider instance;

    // Define mocks
    GraphReadMethods mockGraphReadMethods;
    GraphWriteMethods mockGraphWriteMethods;
    JsonNode mockJsonNode;
    JsonGenerator mockJsonGenerator;
    ImmutableObjectCache mockCache;
    
    // Test variables
    final int attributeId = 23;
    final int elementId = 41;
    final String attribValue = "GREY";
    final String attribValueRGB = "RGB255255204";
    final ConstellationColor redAttribValue = ConstellationColor.getColorValue("Red");
    final ConstellationColor tealAttribValue = ConstellationColor.getColorValue("Teal");
    final GraphAttribute attr = new GraphAttribute(attributeId, GraphElementType.GRAPH, "attrType", "attrName", "attrDesc", null, null);
    
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
        instance = new ColorIOProvider();
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }
    
    /**
     * Perform reset of all mocks and argument captors to ensure clean test steps.
     */
    public void resetMocking() {
        mockGraphReadMethods = mock(GraphReadMethods.class);
        mockGraphWriteMethods = mock(GraphWriteMethods.class);
        mockJsonNode = mock(JsonNode.class);
        mockJsonGenerator = mock(JsonGenerator.class);
        mockCache = mock(ImmutableObjectCache.class);
    }

    /**
     * Test of getName method, of class ColorIOProvider.
     */
    @Test
    public void testGetName() {
        System.out.println("ColorIOProviderNGTest.testGetName");
        assertEquals(instance.getName(), ColorAttributeDescription.ATTRIBUTE_NAME);
    }

    /**
     * Test of readObject method, of class ColorIOProvider.
     */
    @Test
    public void testReadObject() throws Exception {
        System.out.println("ColorIOProviderNGTest.testReadObject");

        // Call method under test with JsonNode.isNull=false and JsonNode.isObject=true, use both types of color definition
        resetMocking();
        when(mockJsonNode.isNull()).thenReturn(false);
        when(mockJsonNode.isObject()).thenReturn(true);
        when(mockJsonNode.has(anyString())).thenReturn(true);
        when(mockJsonNode.get(anyString())).thenReturn(new TextNode("red"));
        when(mockCache.deduplicate(any())).thenReturn(redAttribValue);
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraphWriteMethods, null, null, null, mockCache);
        Mockito.verify(mockGraphWriteMethods, times(1)).setObjectValue(attributeId, elementId, redAttribValue);

        resetMocking(); 
        when(mockJsonNode.isNull()).thenReturn(false);
        when(mockJsonNode.isObject()).thenReturn(true);       
        when(mockJsonNode.has(anyString())).thenReturn(false);
        when(mockJsonNode.get("red")).thenReturn(new FloatNode(0.0f));
        when(mockJsonNode.get("green")).thenReturn(new FloatNode(0.5f));
        when(mockJsonNode.get("blue")).thenReturn(new FloatNode(0.5f));
        when(mockJsonNode.get("alpha")).thenReturn(new FloatNode(1.0f));
        when(mockCache.deduplicate(any())).thenReturn(tealAttribValue);
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraphWriteMethods, null, null, null, mockCache);
        Mockito.verify(mockGraphWriteMethods, times(1)).setObjectValue(attributeId, elementId, tealAttribValue);
        Mockito.verify(mockGraphWriteMethods, times(0)).setStringValue(anyInt(), anyInt(), anyString());

        // Call method under test with JsonNode.isNull=false and JsonNode.isObject=false
        resetMocking();
        when(mockJsonNode.isNull()).thenReturn(false);
        when(mockJsonNode.isObject()).thenReturn(false);
        when(mockJsonNode.textValue()).thenReturn(attribValue);
        when(mockCache.deduplicate(any())).thenReturn(attribValue);
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraphWriteMethods, null, null, null, mockCache);
        Mockito.verify(mockGraphWriteMethods, times(0)).setObjectValue(anyInt(), anyInt(), any(ConstellationColor.class));
        Mockito.verify(mockGraphWriteMethods, times(1)).setStringValue(attributeId, elementId, attribValue);
                
        // Call method under test with JsonNode.isNull=true and JsonNode.isObject=false
        resetMocking();
        when(mockJsonNode.isNull()).thenReturn(true);
        when(mockJsonNode.isObject()).thenReturn(false);
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraphWriteMethods, null, null, null, mockCache);
        Mockito.verify(mockGraphWriteMethods, times(0)).setObjectValue(anyInt(), anyInt(), any(ConstellationColor.class));
        Mockito.verify(mockGraphWriteMethods, times(1)).setStringValue(attributeId, elementId, null);
        
        // Call method under test with JsonNode.isNull=true and JsonNode.isObject=true
        resetMocking();
        when(mockJsonNode.isNull()).thenReturn(true);
        when(mockJsonNode.isObject()).thenReturn(true);
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraphWriteMethods, null, null, null, mockCache);
        Mockito.verify(mockGraphWriteMethods, times(0)).setObjectValue(anyInt(), anyInt(), any(ConstellationColor.class));
        Mockito.verify(mockGraphWriteMethods, times(1)).setStringValue(attributeId, elementId, null);
    }

    /**
     * Test of writeObject method, of class ColorIOProvider.
     */
    @Test
    public void testWriteObject() throws Exception {
        System.out.println("ColorIOProviderNGTest.testWriteObject");

        // Test not verbose and graph.IsDefaultValue is true skips all processing
        resetMocking();
        when(mockGraphReadMethods.isDefaultValue(anyInt(), anyInt())).thenReturn(true);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraphReadMethods, null, false);
        Mockito.verify(mockGraphReadMethods, times(0)).getStringValue(anyInt(), anyInt());

        // Now turn on verbose, and configure getStringValue to return null
        resetMocking();
        when(mockGraphReadMethods.isDefaultValue(anyInt(), anyInt())).thenReturn(true);
        when(mockGraphReadMethods.getStringValue(anyInt(), anyInt())).thenReturn(null);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraphReadMethods, null, true);
        Mockito.verify(mockGraphReadMethods, times(1)).getStringValue(attributeId, elementId);
        Mockito.verify(mockJsonGenerator, times(1)).writeNullField(attr.getName());
        Mockito.verify(mockJsonGenerator, times(0)).writeStringField(anyString(), anyString());

        // Now turn verbose back off, but set graph.isDefaultValue to return false. Set color to a known color (Grey)
        resetMocking();
        when(mockGraphReadMethods.isDefaultValue(anyInt(), anyInt())).thenReturn(false);
        when(mockGraphReadMethods.getStringValue(anyInt(), anyInt())).thenReturn(attribValue);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraphReadMethods, null, false);
        Mockito.verify(mockGraphReadMethods, times(1)).getStringValue(attributeId, elementId);
        Mockito.verify(mockJsonGenerator, times(0)).writeNullField(attr.getName());
        Mockito.verify(mockJsonGenerator, times(1)).writeObjectFieldStart(attr.getName());
        Mockito.verify(mockJsonGenerator, times(1)).writeStringField("name", "Grey");
        Mockito.verify(mockJsonGenerator, times(1)).writeEndObject();
 
        // Repeat the above but use an RGB color
        resetMocking();
        when(mockGraphReadMethods.isDefaultValue(anyInt(), anyInt())).thenReturn(false);
        when(mockGraphReadMethods.getStringValue(anyInt(), anyInt())).thenReturn(attribValueRGB);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraphReadMethods, null, false);
        Mockito.verify(mockGraphReadMethods, times(1)).getStringValue(attributeId, elementId);
        Mockito.verify(mockJsonGenerator, times(0)).writeNullField(attr.getName());
        Mockito.verify(mockJsonGenerator, times(1)).writeObjectFieldStart(attr.getName());
        Mockito.verify(mockJsonGenerator, times(1)).writeNumberField("red", 1.0f);
        Mockito.verify(mockJsonGenerator, times(1)).writeNumberField("green", 1.0f);
        Mockito.verify(mockJsonGenerator, times(1)).writeNumberField("blue", 0.8f);
        Mockito.verify(mockJsonGenerator, times(1)).writeNumberField("alpha", 1.0f);
        Mockito.verify(mockJsonGenerator, times(1)).writeEndObject();
    }    
}
