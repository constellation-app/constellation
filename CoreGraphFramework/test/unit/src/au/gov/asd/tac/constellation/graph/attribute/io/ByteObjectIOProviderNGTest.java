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
package au.gov.asd.tac.constellation.graph.attribute.io;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.ByteObjectAttributeDescription;
import au.gov.asd.tac.constellation.utilities.datastructure.ImmutableObjectCache;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import static org.mockito.ArgumentMatchers.anyBoolean;
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
public class ByteObjectIOProviderNGTest {
    
    // Create object under test
    ByteObjectIOProvider instance;

    // Define mocks
    GraphReadMethods mockGraphReadMethods;
    GraphWriteMethods mockGraphWriteMethods;
    JsonNode mockJsonNode;
    JsonGenerator mockJsonGenerator;

    // Test variables
    final int attributeId = 23;
    final int elementId = 41;
    final String attribValue = "TestAttrib";
    final GraphAttribute attr = new GraphAttribute(attributeId, GraphElementType.GRAPH, "attrType", "attrName", "attrDesc", null, null);

    public ByteObjectIOProviderNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        instance = new ByteObjectIOProvider();
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
     * Test of getName method, of class ByteObjectIOProvider.
     */
    @Test
    public void testGetName() {
        System.out.println("ByteObjectIOProviderNGTest.testGetName");
        assertEquals(instance.getName(), ByteObjectAttributeDescription.ATTRIBUTE_NAME);
    }

    /**
     * Test of readObject method, of class ByteObjectIOProvider.
     */
    @Test
    public void testReadObject() throws Exception {
        System.out.println("ByteObjectIOProviderNGTest.testReadObject");
        
        // Call method under test with JsonNode set to return isNull = true
        resetMocking();
        when(mockJsonNode.isNull()).thenReturn(true);
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraphWriteMethods, null, null, null, null);
        Mockito.verify(mockJsonNode, times(0)).asInt();
        Mockito.verify(mockGraphWriteMethods, times(1)).setObjectValue(attributeId, elementId, null);
        
        // Call method under test with JsonNode set to return isNull = false
        resetMocking();
        when(mockJsonNode.isNull()).thenReturn(false);
        when(mockJsonNode.asInt()).thenReturn(9);
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraphWriteMethods, null, null, null, null);
        Mockito.verify(mockJsonNode, times(1)).asInt();
        Mockito.verify(mockGraphWriteMethods, times(1)).setObjectValue(attributeId, elementId, (byte)9);
    }

    /**
     * Test of writeObject method, of class ByteObjectIOProvider.
     */
    @Test
    public void testWriteObject() throws Exception {
        System.out.println("ByteObjectIOProviderNGTest.testWriteObject");
        
        // Test not verbose and graph.IsDefaultValue is true
        resetMocking();
        when(mockGraphReadMethods.isDefaultValue(anyInt(), anyInt())).thenReturn(true);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraphReadMethods, null, false);
        Mockito.verify(mockGraphReadMethods, times(0)).getObjectValue(anyInt(), anyInt());
        
        // Test verbose and graph.IsDefaultValue is true, getStringValue to return null
        resetMocking();
        when(mockGraphReadMethods.isDefaultValue(anyInt(), anyInt())).thenReturn(true);
        when(mockGraphReadMethods.getObjectValue(anyInt(), anyInt())).thenReturn(null);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraphReadMethods, null, true);
        Mockito.verify(mockJsonGenerator, times(1)).writeNullField(attr.getName());
        Mockito.verify(mockJsonGenerator, times(0)).writeNumberField(anyString(), anyInt());
        
        // Test not verbose and graph.IsDefaultValue is false, getObjectValue returns null 
        resetMocking();
        when(mockGraphReadMethods.isDefaultValue(anyInt(), anyInt())).thenReturn(false);
        when(mockGraphReadMethods.getObjectValue(anyInt(), anyInt())).thenReturn(null);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraphReadMethods, null, false);
        Mockito.verify(mockJsonGenerator, times(1)).writeNullField(attr.getName());
        Mockito.verify(mockJsonGenerator, times(0)).writeNumberField(anyString(), anyInt());
        
        // Test verbose and graph.IsDefaultValue is false, getObjectValue returns bool
        resetMocking();
        when(mockGraphReadMethods.isDefaultValue(anyInt(), anyInt())).thenReturn(false);
        when(mockGraphReadMethods.getObjectValue(anyInt(), anyInt())).thenReturn((byte)9);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraphReadMethods, null, true);
        Mockito.verify(mockJsonGenerator, times(0)).writeNullField(anyString());
        Mockito.verify(mockJsonGenerator, times(1)).writeNumberField(attr.getName(), 9);
    }
}
