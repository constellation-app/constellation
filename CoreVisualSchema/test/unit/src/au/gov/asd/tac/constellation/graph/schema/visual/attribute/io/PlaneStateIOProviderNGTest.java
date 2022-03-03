/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.graph.schema.visual.attribute.io;

import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
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
public class PlaneStateIOProviderNGTest {
    
    public PlaneStateIOProviderNGTest() {
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
     * Test of getName method, of class PlaneStateIOProvider.
     */
    @Test
    public void testGetName() {
        System.out.println("PlaneStateIOProvider.testGetName");
        
        PlaneStateIOProvider instance = new PlaneStateIOProvider();
        String result = instance.getName();
        assertEquals(result, PlaneState.ATTRIBUTE_NAME);
    }

    /**
     * Test of readObject method, of class PlaneStateIOProvider.
     */
    @Test
    public void testReadObject() throws Exception {
        System.out.println("PlaneStateIOProvider.testReadObject");
        
        // Create object under test
        PlaneStateIOProvider instance = new PlaneStateIOProvider();
        
        // Create mocks
        final JsonNode mockJsonNode = mock(JsonNode.class);
        GraphWriteMethods mockGraph = mock(GraphWriteMethods.class);
        GraphByteReader mockByteReader = mock(GraphByteReader.class);
        
        // Create argument captors
        ArgumentCaptor<String> captorString = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> captorAtributeId = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> captorElementId = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<PlaneState> captorState = ArgumentCaptor.forClass(PlaneState.class);
        
        int attributeId = 23;
        int elementId = 41;

        // Call method under test with JsonNode.isNull=true and confirm nothing happens
        when(mockJsonNode.isNull()).thenReturn(true);
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraph, null, null, null, null);
        Mockito.verify(mockJsonNode, times(0)).get(captorString.capture());
      
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
        when(mockJsonNode.isNull()).thenReturn(false);
        when(mockJsonNode.get(anyString())).thenReturn(arrayNode);

        try (MockedStatic<Plane> mockPlane = Mockito.mockStatic(Plane.class)) {
            mockPlane.when(() -> Plane.readNode(any(), any(), any()))
                   .thenReturn(new Plane("plane", 0.0f, 0.1f, 0.2f, 0.3f, 0.4f, new BufferedImage(1,2,3), 5, 6));

            instance.readObject(attributeId, elementId, mockJsonNode, mockGraph, null, null, mockByteReader, null);
            Mockito.verify(mockGraph, times(1)).setObjectValue(captorAtributeId.capture(), captorElementId.capture(), captorState.capture());
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
    public void testWriteObject() throws Exception {
        System.out.println("PlaneStateIOProvider.testWriteOption");
        
        // Create object under test
        PlaneStateIOProvider instance = new PlaneStateIOProvider();
        
        // Create mocks
        JsonGenerator mockJsonGenerator = mock(JsonGenerator.class);
        GraphWriteMethods mockGraph = mock(GraphWriteMethods.class);
        
        // Create argument captors
        ArgumentCaptor<Integer> captorAtributeId = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> captorElementId = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<String> captorAttrName = ArgumentCaptor.forClass(String.class);

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
        mockJsonGenerator = mock(JsonGenerator.class);
        mockGraph = mock(GraphWriteMethods.class);
        captorAtributeId = ArgumentCaptor.forClass(Integer.class);
        captorElementId = ArgumentCaptor.forClass(Integer.class);
        when(mockGraph.isDefaultValue(anyInt(), anyInt())).thenReturn(false);
        when(mockGraph.getObjectValue(anyInt(), anyInt())).thenReturn(null);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraph, null, false);
        Mockito.verify(mockGraph, times(1)).getObjectValue(captorAtributeId.capture(), captorElementId.capture());
        Mockito.verify(mockJsonGenerator, times(1)).writeNullField(captorAttrName.capture());
        assertEquals((int)captorAtributeId.getValue(), attributeId);
        assertEquals((int)captorElementId.getValue(), elementId);
        assertEquals(captorAttrName.getValue(), attrName);

        // Test verbose and graph.IsDefaultValue is false, graph.getObjectValue returns null 
        mockJsonGenerator = mock(JsonGenerator.class);
        mockGraph = mock(GraphWriteMethods.class);
        captorAtributeId = ArgumentCaptor.forClass(Integer.class);
        captorElementId = ArgumentCaptor.forClass(Integer.class);
        captorAttrName = ArgumentCaptor.forClass(String.class);
        when(mockGraph.isDefaultValue(anyInt(), anyInt())).thenReturn(false);
        when(mockGraph.getObjectValue(anyInt(), anyInt())).thenReturn(null);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraph, null, true);
        Mockito.verify(mockGraph, times(1)).getObjectValue(captorAtributeId.capture(), captorElementId.capture());
        Mockito.verify(mockJsonGenerator, times(1)).writeNullField(captorAttrName.capture());
        assertEquals((int)captorAtributeId.getValue(), attributeId);
        assertEquals((int)captorElementId.getValue(), elementId);
        assertEquals(captorAttrName.getValue(), attrName);

        // Test verbose and graph.IsDefaultValue is true, graph.getObjectValue returns PlaneState object
        
        // Note, wasnt able to test this case ATM as writeNode could not be mocked as this method comes off internal
        // objects, also wsn't able to mock calls made within writeNode
    }
}
