/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.graph.schema.visual.attribute.io;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteReader;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteWriter;
import au.gov.asd.tac.constellation.graph.schema.visual.VertexDecorators;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.LayerNameAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.LayerName;
import au.gov.asd.tac.constellation.utilities.datastructure.ImmutableObjectCache;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.Map;
import org.mockito.ArgumentCaptor;
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
public class LayerNameIOProviderNGTest {
    
    public LayerNameIOProviderNGTest() {
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
     * Test of getName method, of class LayerNameIOProvider.
     */
    @Test
    public void testGetName() {
        System.out.println("LayerNameIOProvider.testGetName");
        
        LayerNameIOProvider instance = new LayerNameIOProvider();
        String result = instance.getName();
        assertEquals(result, LayerNameAttributeDescription.ATTRIBUTE_NAME);
    }

    /**
     * Test of readObject method, of class LayerNameIOProvider.
     */
    @Test
    public void testReadObject() throws Exception {
        System.out.println("LayerNameIOProvider.testReadObject");
        
        // Create object under test
        LayerNameIOProvider instance = new LayerNameIOProvider();
        
        // Create mocks
        final JsonNode mockJsonNode = mock(JsonNode.class);
        GraphWriteMethods mockGraph = mock(GraphWriteMethods.class);
        
        // Create argument captors
        ArgumentCaptor<String> captorString = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> captorAtributeId = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> captorElementId = ArgumentCaptor.forClass(Integer.class);
        final ArgumentCaptor<LayerName> captorObjectAttributeValue = ArgumentCaptor.forClass(LayerName.class);
        
        int attributeId = 23;
        int elementId = 41;
        JsonNode layerNameNode = new TextNode("layerName");
        JsonNode layerValueNode = new IntNode(15);
    
        // Call method under test with JsonNode.isNull=true and confirm nothing happens
        when(mockJsonNode.isNull()).thenReturn(true);
        when(mockJsonNode.has(anyString())).thenReturn(true);
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraph, null, null, null, null);
        Mockito.verify(mockJsonNode, times(0)).get(captorString.capture());
        
        // Call method under test with JsonNode.isNull=false and jnode has all required tags
        when(mockJsonNode.isNull()).thenReturn(false);
        when(mockJsonNode.has(anyString())).thenReturn(true);
        
        when(mockJsonNode.get("name")).thenReturn(layerNameNode);
        when(mockJsonNode.get("layer")).thenReturn(layerValueNode);
        
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraph, null, null, null, null);
        Mockito.verify(mockJsonNode, times(2)).get(captorString.capture());
        Mockito.verify(mockGraph, times(1)).setObjectValue(captorAtributeId.capture(), captorElementId.capture(), captorObjectAttributeValue.capture());
        assertEquals(captorString.getAllValues().get(0), "name");
        assertEquals(captorString.getAllValues().get(1), "layer");
        assertEquals((int)captorAtributeId.getAllValues().get(0), attributeId);
        assertEquals((int)captorElementId.getAllValues().get(0), elementId);
        assertEquals(((LayerName)captorObjectAttributeValue.getAllValues().get(0)).getName(), "layerName");
        assertEquals(((LayerName)captorObjectAttributeValue.getAllValues().get(0)).getLayer(), 15);
    }

    /**
     * Test of writeObject method, of class LayerNameIOProvider.
     */
    @Test
    public void testWriteObject() throws Exception {
    }
    
}
