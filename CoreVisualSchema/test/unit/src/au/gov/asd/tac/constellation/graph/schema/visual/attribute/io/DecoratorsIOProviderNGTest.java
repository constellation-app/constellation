/*
 * Copyright 2010-2025 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.graph.schema.visual.VertexDecorators;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.DecoratorsAttributeDescription;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.IOException;
import org.mockito.ArgumentCaptor;
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
public class DecoratorsIOProviderNGTest {
        
    // Create object under test
    DecoratorsIOProvider instance = new DecoratorsIOProvider();

    // Define mocks
    GraphReadMethods mockGraphReadMethods;
    GraphWriteMethods mockGraphWriteMethods;
    JsonNode mockJsonNode;
    JsonGenerator mockJsonGenerator;
    
    // Define captors
    ArgumentCaptor<VertexDecorators> captureVertexDecorators;
    ArgumentCaptor<Integer> captorAtributeId;
    ArgumentCaptor<Integer> captorElementId;
    ArgumentCaptor<String> captorField;
    ArgumentCaptor<String> captorValue;
        
    // Test variables
    final int attributeId = 23;
    final int elementId = 41;
    final String attribValue = "TestAttrib";
    final String decoratorStr = "TestDecorator";
    final GraphAttribute attr = new GraphAttribute(attributeId, GraphElementType.GRAPH, "attrType", "attrName", "attrDesc", null, null);
    
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
        instance = new DecoratorsIOProvider();
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
        captorAtributeId = ArgumentCaptor.forClass(Integer.class);
        captorElementId = ArgumentCaptor.forClass(Integer.class);
        captureVertexDecorators = ArgumentCaptor.forClass(VertexDecorators.class);
        captorField = ArgumentCaptor.forClass(String.class);
        captorValue = ArgumentCaptor.forClass(String.class);
    }

    /**
     * Test of getName method, of class DecoratorsIOProvider.
     */
    @Test
    public void testGetName() {
        System.out.println("DecoratorsIOProviderNGTest.testGetName");
        assertEquals(instance.getName(), DecoratorsAttributeDescription.ATTRIBUTE_NAME);
    }

    /**
     * Test of readObject method, of class DecoratorsIOProvider.
     * @throws java.io.IOException
     */
    @Test
    public void testReadObject() throws IOException {
        System.out.println("DecoratorsIOProviderNGTest.testReadObject");

        // Call method under test with JsonNode.isNull=false and JsonNode.isObject=true
        resetMocking();
        final JsonNode decoratorCoordinate = new TextNode(decoratorStr);
        when(mockJsonNode.isNull()).thenReturn(false);
        when(mockJsonNode.isObject()).thenReturn(true);
        when(mockJsonNode.get(anyString())).thenReturn(decoratorCoordinate);
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraphWriteMethods, null, null, null, null);
        Mockito.verify(mockGraphWriteMethods, times(1)).setObjectValue(captorAtributeId.capture(), captorElementId.capture(), captureVertexDecorators.capture());
        assertEquals((int)captorAtributeId.getValue(), attributeId);
        assertEquals((int)captorElementId.getValue(), elementId);
        assertEquals(captureVertexDecorators.getValue().getNorthEastDecoratorAttribute(), decoratorStr);
        assertEquals(captureVertexDecorators.getValue().getNorthWestDecoratorAttribute(), decoratorStr);
        assertEquals(captureVertexDecorators.getValue().getSouthEastDecoratorAttribute(), decoratorStr);
        assertEquals(captureVertexDecorators.getValue().getSouthWestDecoratorAttribute(), decoratorStr);
         
        // Call method under test with JsonNode.isNull=false and JsonNode.isObject=false
        resetMocking();
        when(mockJsonNode.isNull()).thenReturn(false);
        when(mockJsonNode.isObject()).thenReturn(false);
        when(mockJsonNode.textValue()).thenReturn(attribValue);
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraphWriteMethods, null, null, null, null);
        Mockito.verify(mockGraphWriteMethods, times(1)).setStringValue(attributeId, elementId, attribValue);

        // Call method under test with JsonNode.isNull=true and JsonNode.isObject=false
        resetMocking();
        when(mockJsonNode.isNull()).thenReturn(true);
        when(mockJsonNode.isObject()).thenReturn(false);
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraphWriteMethods, null, null, null, null);
        Mockito.verify(mockJsonNode, times(0)).textValue();
        Mockito.verify(mockGraphWriteMethods, times(1)).setStringValue(attributeId, elementId, null);
//
        // Call method under test with JsonNode.isNull=true and JsonNode.isObject=true
        resetMocking();;
        when(mockJsonNode.isNull()).thenReturn(true);
        when(mockJsonNode.isObject()).thenReturn(true);
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraphWriteMethods, null, null, null, null);
        Mockito.verify(mockJsonNode, times(0)).textValue();
        Mockito.verify(mockGraphWriteMethods, times(1)).setStringValue(attributeId, elementId, null);
    }

    /**
     * Test of writeObject method, of class DecoratorsIOProvider.
     * @throws java.io.IOException
     */
    @Test
    public void testWriteObject() throws IOException {
        System.out.println("DecoratorsIOProviderNGTest.testWriteObject");

        // Test not verbose and graph.IsDefaultValue is true skips all processing
        resetMocking();
        when(mockGraphReadMethods.isDefaultValue(anyInt(), anyInt())).thenReturn(true);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraphReadMethods, null, false);
        Mockito.verify(mockGraphReadMethods, times(0)).getObjectValue(anyInt(), anyInt());

        // Test not verbose but graph.IsDefaultValue is false, graph.getObjectValue returns null
        resetMocking();
        when(mockGraphReadMethods.isDefaultValue(anyInt(), anyInt())).thenReturn(false);
        when(mockGraphReadMethods.getObjectValue(anyInt(), anyInt())).thenReturn(null);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraphReadMethods, null, false);
        Mockito.verify(mockGraphReadMethods, times(1)).getObjectValue(attributeId, elementId);
        Mockito.verify(mockJsonGenerator, times(1)).writeNullField(attr.getName());
        Mockito.verify(mockJsonGenerator, times(0)).writeObjectFieldStart(any());
        
        // Test verbose and graph.IsDefaultValue is false, graph.getObjectValue returns null 
        resetMocking();
        when(mockGraphReadMethods.isDefaultValue(anyInt(), anyInt())).thenReturn(false);
        when(mockGraphReadMethods.getObjectValue(anyInt(), anyInt())).thenReturn(null);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraphReadMethods, null, true);
        Mockito.verify(mockGraphReadMethods, times(1)).getObjectValue(attributeId, elementId);
        Mockito.verify(mockJsonGenerator, times(1)).writeNullField(attr.getName());
        Mockito.verify(mockJsonGenerator, times(0)).writeObjectFieldStart(any());

        // Test verbose and graph.IsDefaultValue is true, graph.getObjectValue returns decotrator object
        resetMocking();
        when(mockGraphReadMethods.isDefaultValue(anyInt(), anyInt())).thenReturn(true);
        when(mockGraphReadMethods.getObjectValue(anyInt(), anyInt())).thenReturn(new VertexDecorators("NW", "NE", "SE", "SW"));
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraphReadMethods, null, true);
        Mockito.verify(mockJsonGenerator, times(0)).writeNullField(any());
        Mockito.verify(mockJsonGenerator, times(1)).writeObjectFieldStart(attr.getName());
        Mockito.verify(mockJsonGenerator, times(4)).writeStringField(captorField.capture(), captorValue.capture());
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
