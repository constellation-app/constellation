/*
 * Copyright 2010-2022 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.DrawFlagsAttributeDescription;
import au.gov.asd.tac.constellation.utilities.visual.DrawFlags;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import static org.mockito.ArgumentMatchers.anyInt;
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
public class DrawFlagsIOProviderNGTest {
        
    // Create object under test
    DrawFlagsIOProvider instance = new DrawFlagsIOProvider();

    // Define mocks
    GraphReadMethods mockGraphReadMethods;
    GraphWriteMethods mockGraphWriteMethods;
    JsonNode mockJsonNode;
    JsonGenerator mockJsonGenerator;
        
    // Test variables
    final int attributeId = 23;
    final int elementId = 41;
    final int flagId = 13;
    final String attribValue = "TestAttrib";
    final String decoratorStr = "TestDecorator";
    final GraphAttribute attr = new GraphAttribute(attributeId, GraphElementType.GRAPH, "attrType", "attrName", "attrDesc", null, null);
    
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
     * Perform reset of all mocks and argument captors to ensure clean test steps.
     */
    public void resetMocking() {
        mockGraphReadMethods = mock(GraphReadMethods.class);
        mockGraphWriteMethods = mock(GraphWriteMethods.class);
        mockJsonNode = mock(JsonNode.class);
        mockJsonGenerator = mock(JsonGenerator.class);
    }

    /**
     * Test of getName method, of class DrawFlagsIOProvider.
     */
    @Test
    public void testGetName() {
        System.out.println("DrawFlagsIOProviderNGTest.testGetName");
        assertEquals(instance.getName(), DrawFlagsAttributeDescription.ATTRIBUTE_NAME);
    }

    /**
     * Test of readObject method, of class DrawFlagsIOProvider.
     */
    @Test
    public void testReadObject() throws Exception {
        System.out.println("DrawFlagsIOProviderNGTest.testReadObject");

        // isNull returns true, default flags returned
        resetMocking();
        when(mockJsonNode.isNull()).thenReturn(true);
        when(mockGraphWriteMethods.getAttributeDefaultValue(anyInt())).thenReturn(new DrawFlags(flagId));
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraphWriteMethods, null, null, null, null);
        Mockito.verify(mockGraphWriteMethods, times(1)).getAttributeDefaultValue(attributeId);
        Mockito.verify(mockGraphWriteMethods, times(1)).setIntValue(attributeId, elementId, flagId);

        // isNull returns false, flag value returned from node
        resetMocking();
        when(mockJsonNode.isNull()).thenReturn(false);
        when(mockJsonNode.intValue()).thenReturn(flagId);
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraphWriteMethods, null, null, null, null);
        Mockito.verify(mockGraphWriteMethods, times(0)).getAttributeDefaultValue(anyInt());
        Mockito.verify(mockGraphWriteMethods, times(1)).setIntValue(attributeId, elementId, flagId);
    }

    /**
     * Test of writeObject method, of class DrawFlagsIOProvider.
     */
    @Test
    public void testWriteObject() throws Exception {
        System.out.println("DrawFlagsIOProviderNGTest.testWriteObject");

        // Test not verbose and graph.IsDefaultValue is true skips all processing
        resetMocking();
        when(mockGraphReadMethods.isDefaultValue(anyInt(), anyInt())).thenReturn(true);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraphReadMethods, null, false);
        Mockito.verify(mockGraphReadMethods, times(0)).getIntValue(anyInt(), anyInt());

        // Test not verbose but graph.IsDefaultValue is false
        resetMocking();
        mockJsonGenerator = mock(JsonGenerator.class);
        mockGraphReadMethods = mock(GraphWriteMethods.class);
        when(mockGraphReadMethods.isDefaultValue(anyInt(), anyInt())).thenReturn(false);
        when(mockGraphReadMethods.getIntValue(anyInt(), anyInt())).thenReturn(flagId);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraphReadMethods, null, false);
        Mockito.verify(mockGraphReadMethods, times(1)).getIntValue(attributeId, elementId);
        Mockito.verify(mockJsonGenerator, times(1)).writeNumberField(attr.getName(), flagId);
        
        // Test verbose and graph.IsDefaultValue is false
        resetMocking();
        when(mockGraphReadMethods.isDefaultValue(anyInt(), anyInt())).thenReturn(false);
        when(mockGraphReadMethods.getIntValue(anyInt(), anyInt())).thenReturn(flagId);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraphReadMethods, null, true);
        Mockito.verify(mockGraphReadMethods, times(1)).getIntValue(attributeId, elementId);
        Mockito.verify(mockJsonGenerator, times(1)).writeNumberField(attr.getName(), flagId);

        // Test verbose and graph.IsDefaultValue is true
        resetMocking();
        when(mockGraphReadMethods.isDefaultValue(anyInt(), anyInt())).thenReturn(false);
        when(mockGraphReadMethods.getIntValue(anyInt(), anyInt())).thenReturn(flagId);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraphReadMethods, null, true);
        Mockito.verify(mockGraphReadMethods, times(1)).getIntValue(attributeId, elementId);
        Mockito.verify(mockJsonGenerator, times(1)).writeNumberField(attr.getName(), flagId);
    }
}
