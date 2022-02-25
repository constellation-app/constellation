/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.graph.schema.visual.attribute.io;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.DrawFlagsAttributeDescription;
import au.gov.asd.tac.constellation.utilities.visual.DrawFlags;
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
public class DrawFlagsIOProviderNGTest {
    
    public DrawFlagsIOProviderNGTest() {
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
     * Test of getName method, of class DrawFlagsIOProvider.
     */
    @Test
    public void testGetName() {
        System.out.println("DrawFlagsIOProvider.testGetName");
        
        DrawFlagsIOProvider instance = new DrawFlagsIOProvider();
        String result = instance.getName();
        assertEquals(result, DrawFlagsAttributeDescription.ATTRIBUTE_NAME);
    }

    /**
     * Test of readObject method, of class DrawFlagsIOProvider.
     */
    @Test
    public void testReadObject() throws Exception {
        System.out.println("DrawFlagsIOProvider.testReadObject");
        
        // Create object under test
        DrawFlagsIOProvider instance = new DrawFlagsIOProvider();
        
        // Create mocks
        final JsonNode mockJsonNode = mock(JsonNode.class);
        GraphWriteMethods mockGraph = mock(GraphWriteMethods.class);
               
        // Create argument captors
        final ArgumentCaptor<Integer> captorAtributeId = ArgumentCaptor.forClass(Integer.class);
        final ArgumentCaptor<Integer> captorElementId = ArgumentCaptor.forClass(Integer.class);
        final ArgumentCaptor<Integer> captorAttributeValue = ArgumentCaptor.forClass(Integer.class);
                
        int attributeId = 23;
        int elementId = 41;

        // isNull returns true
        when(mockJsonNode.isNull()).thenReturn(true);
        when(mockGraph.getAttributeDefaultValue(anyInt())).thenReturn(new DrawFlags(13));
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraph, null, null, null, null);
        Mockito.verify(mockGraph, times(1)).getAttributeDefaultValue(captorAtributeId.capture());
        Mockito.verify(mockGraph, times(1)).setIntValue(captorAtributeId.capture(), captorElementId.capture(), captorAttributeValue.capture());
        assertEquals((int)captorAtributeId.getAllValues().get(0), attributeId);
        assertEquals((int)captorAtributeId.getAllValues().get(1), attributeId);
        assertEquals((int)captorElementId.getAllValues().get(0), elementId);
        assertEquals((int)captorAttributeValue.getAllValues().get(0), 13);

    }

    /**
     * Test of writeObject method, of class DrawFlagsIOProvider.
     */
    @Test
    public void testWriteObject() throws Exception {
        System.out.println("DrawFlagsIOProvider.testWriteObject");
    }
    
}
