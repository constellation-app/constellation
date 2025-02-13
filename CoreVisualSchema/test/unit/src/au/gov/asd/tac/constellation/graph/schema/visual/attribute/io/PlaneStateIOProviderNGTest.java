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
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteReader;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.Plane;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.PlaneState;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.MockedStatic;
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
public class PlaneStateIOProviderNGTest {
        
    // Create object under test
    PlaneStateIOProvider instance;

    // Define mocks
    GraphReadMethods mockGraphReadMethods;
    GraphWriteMethods mockGraphWriteMethods;
    JsonNode mockJsonNode;
    JsonGenerator mockJsonGenerator;
    GraphByteReader mockByteReader;
    
    // Test variables
    final int attributeId = 23;
    final int elementId = 41;
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
        instance = new PlaneStateIOProvider();
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
        mockByteReader = mock(GraphByteReader.class);
    }

    /**
     * Test of getName method, of class PlaneStateIOProvider.
     */
    @Test
    public void testGetName() {
        System.out.println("PlaneStateIOProviderNGTest.testGetName");
        assertEquals(instance.getName(), PlaneState.ATTRIBUTE_NAME);
    }

    /**
     * Test of readObject method, of class PlaneStateIOProvider.
     * @throws java.io.IOException
     */
    @Test
    public void testReadObject() throws IOException {
        System.out.println("PlaneStateIOProviderNGTest.testReadObject");
        
//        // Create argument captors
        ArgumentCaptor<Integer> captorAtributeId = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> captorElementId = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<PlaneState> captorState = ArgumentCaptor.forClass(PlaneState.class);

        // Call method under test with JsonNode.isNull=true and confirm nothing happens
        resetMocking();
        when(mockJsonNode.isNull()).thenReturn(true);
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraphWriteMethods, null, null, null, null);
        Mockito.verify(mockJsonNode, times(0)).get(anyString());
      
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode plane1 = mapper.createObjectNode();
        ObjectNode plane2 = mapper.createObjectNode();
        plane1.put("label", "tst_label1");
        plane1.put("x", 0.0);
        plane1.put("y", 0.0);
        plane1.put("z", 0.0);
        plane1.put("width", 1.0);
        plane1.put("height", 2.0);
        plane1.put("image_width", 3.0);
        plane1.put("image_height", 4.0);
        plane1.put("plane_ref", "reference");
        plane2.put("label", "tst_label2");
        plane2.put("x", 0.0);
        plane2.put("y", 0.0);
        plane2.put("z", 0.0);
        plane2.put("width", 1.0);
        plane2.put("height", 2.0);
        plane2.put("image_width", 3.0);
        plane2.put("image_height", 4.0);
        plane2.put("plane_ref", "reference");
        ArrayNode arrayNode = mapper.createArrayNode();
        arrayNode.addAll(Arrays.asList(plane1, plane2));
        
        // Call method under test with JsonNode.isNull=false
        resetMocking();
        when(mockJsonNode.isNull()).thenReturn(false);
        when(mockJsonNode.get(anyString())).thenReturn(arrayNode);

        try (MockedStatic<Plane> mockPlane = Mockito.mockStatic(Plane.class)) {
            mockPlane.when(() -> Plane.readNode(any(), any()))
                   .thenReturn(new Plane("plane", 0.0f, 0.1f, 0.2f, 0.3f, 0.4f, new BufferedImage(1,2,3), 5, 6));

            instance.readObject(attributeId, elementId, mockJsonNode, mockGraphWriteMethods, null, null, mockByteReader, null);
            Mockito.verify(mockGraphWriteMethods, times(1)).setObjectValue(captorAtributeId.capture(), captorElementId.capture(), captorState.capture());
            assertEquals((int)captorAtributeId.getValue(), attributeId);
            assertEquals((int)captorElementId.getValue(), elementId);
            assertEquals(((PlaneState)captorState.getValue()).toString(), "%s[\nPlane[plane@(0.000000,0.100000,0.200000) 5x6]\n" +
                "Plane[plane@(0.000000,0.100000,0.200000) 5x6]\n]");
        }
    }

    /**
     * Test of writeObject method, of class PlaneStateIOProvider.
     */
    @Test
    public void testWriteObject() throws IOException {
        System.out.println("PlaneStateIOProviderNGTest.testWriteOption");
        
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

        // Test verbose and graph.IsDefaultValue is false, graph.getObjectValue returns non null 
        resetMocking();
        when(mockGraphReadMethods.isDefaultValue(anyInt(), anyInt())).thenReturn(false);
        when(mockGraphReadMethods.getObjectValue(anyInt(), anyInt())).thenReturn(null);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraphReadMethods, null, true);
        Mockito.verify(mockGraphReadMethods, times(1)).getObjectValue(attributeId, elementId);
        Mockito.verify(mockJsonGenerator, times(1)).writeNullField(attr.getName());

        // Test verbose and graph.IsDefaultValue is true, graph.getObjectValue returns PlaneState object
        // Note, wasnt able to test this case ATM as writeNode could not be mocked as this method comes off internal
        // objects, also wasn't able to mock calls made within writeNode
    }
}
