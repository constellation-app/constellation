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
package au.gov.asd.tac.constellation.graph.attribute.io;

import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author serpens24
 */
public class AbstractUncachedStringIOProviderNGTest {
    
    // Create object under test
    DateIOProvider instance;

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
        instance = new DateIOProvider();
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
     * Test of readObject method, of class AbstractUncachedStringIOProvider.
     * @throws java.io.IOException
     */
    @Test
    public void testReadObject() throws IOException {
        System.out.println("AbstractUncachedStringIOProviderNGTest.testReadObject");
        
        // Call method under test with JsonNode set to return isNull = true
        resetMocking();
        when(mockJsonNode.isNull()).thenReturn(true);
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraphWriteMethods, null, null, null, null);
        Mockito.verify(mockGraphWriteMethods, times(1)).setStringValue(attributeId, elementId, null);
        
        // Call method under test with JsonNode set to return isNull = false
        resetMocking();
        when(mockJsonNode.isNull()).thenReturn(false);
        when(mockJsonNode.textValue()).thenReturn(attribValue);
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraphWriteMethods, null, null, null, null);
        Mockito.verify(mockGraphWriteMethods, times(1)).setStringValue(attributeId, elementId, attribValue);
    }

    /**
     * Test of writeObject method, of class AbstractUncachedStringIOProvider.
     * @throws java.io.IOException
     */
    @Test
    public void testWriteObject() throws IOException {
        System.out.println("AbstractUncachedStringIOProviderNGTest.testWriteObject");
        
        // Test not verbose and graph.IsDefaultValue is true
        resetMocking();
        when(mockGraphReadMethods.isDefaultValue(anyInt(), anyInt())).thenReturn(true);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraphReadMethods, null, false);
        Mockito.verify(mockGraphReadMethods, times(0)).getStringValue(anyInt(), anyInt());
        
        // Test verbose and graph.IsDefaultValue is true, getStringValue to return null
        resetMocking();
        when(mockGraphReadMethods.isDefaultValue(anyInt(), anyInt())).thenReturn(true);
        when(mockGraphReadMethods.getStringValue(anyInt(), anyInt())).thenReturn(null);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraphReadMethods, null, true);
        Mockito.verify(mockJsonGenerator, times(1)).writeNullField(attr.getName());
        Mockito.verify(mockJsonGenerator, times(0)).writeStringField(anyString(), anyString());
        
        // Test not verbose and graph.isDefaultValue is false, getStringValue to return non null
        resetMocking();
        when(mockGraphReadMethods.isDefaultValue(anyInt(), anyInt())).thenReturn(false);
        when(mockGraphReadMethods.getStringValue(anyInt(), anyInt())).thenReturn(attribValue);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraphReadMethods, null, false);
        Mockito.verify(mockJsonGenerator, times(0)).writeNullField(attr.getName());
        Mockito.verify(mockJsonGenerator, times(1)).writeStringField(attr.getName(), attribValue);
        
        // Test verbose and graph.isDefaultValue is false, getStringValue to return null
        resetMocking();
        when(mockGraphReadMethods.isDefaultValue(anyInt(), anyInt())).thenReturn(false);
        when(mockGraphReadMethods.getStringValue(anyInt(), anyInt())).thenReturn(null);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraphReadMethods, null, true);
        Mockito.verify(mockJsonGenerator, times(1)).writeNullField(attr.getName());
        Mockito.verify(mockJsonGenerator, times(0)).writeStringField(anyString(), anyString());
    }
}
