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

import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.IntegerAttributeDescription;
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
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class IntegerIOProviderNGTest extends ConstellationTest {

    // Create object under test
    IntegerIOProvider instance;

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

    
    public IntegerIOProviderNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        instance = new IntegerIOProvider();
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
     * Test of getName method, of class IntegerIOProvider.
     */
    @Test
    public void testGetName() {
        System.out.println("IntegerIOProviderNGTest.testGetName");
        assertEquals(instance.getName(), IntegerAttributeDescription.ATTRIBUTE_NAME);
    }

    /**
     * Test of readObject method, of class IntegerIOProvider.
     */
    @Test
    public void testReadObject() throws Exception {
        System.out.println("IntegerIOProviderNGTest.testReadObject");
        
        // Call method under test with JsonNode set to return isNull = true
        resetMocking();
        when(mockJsonNode.intValue()).thenReturn(5);
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraphWriteMethods, null, null, null, null);
        Mockito.verify(mockJsonNode, times(1)).intValue();
        Mockito.verify(mockGraphWriteMethods, times(1)).setIntValue(attributeId, elementId, 5);
    }

    /**
     * Test of writeObject method, of class IntegerIOProvider.
     */
    @Test
    public void testWriteObject() throws Exception {
        System.out.println("IntegerIOProviderNGTest.testWriteObject");
        
        // Test not verbose and graph.IsDefaultValue is true
        resetMocking();
        when(mockGraphReadMethods.isDefaultValue(anyInt(), anyInt())).thenReturn(true);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraphReadMethods, null, false);
        Mockito.verify(mockGraphReadMethods, times(0)).getIntValue(anyInt(), anyInt());
        
        // Test verbose and graph.IsDefaultValue is true
        resetMocking();
        when(mockGraphReadMethods.isDefaultValue(anyInt(), anyInt())).thenReturn(true);
        when(mockGraphReadMethods.getIntValue(anyInt(), anyInt())).thenReturn(5);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraphReadMethods, null, true);
        Mockito.verify(mockJsonGenerator, times(1)).writeNumberField(attr.getName(), 5);
        
        // Test not verbose and graph.IsDefaultValue is false
        resetMocking();
        when(mockGraphReadMethods.isDefaultValue(anyInt(), anyInt())).thenReturn(false);
        when(mockGraphReadMethods.getIntValue(anyInt(), anyInt())).thenReturn(5);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraphReadMethods, null, false);
        Mockito.verify(mockJsonGenerator, times(1)).writeNumberField(attr.getName(), 5);
        
        // Test verbose and graph.IsDefaultValue is false
        resetMocking();
        when(mockGraphReadMethods.isDefaultValue(anyInt(), anyInt())).thenReturn(false);
        when(mockGraphReadMethods.getIntValue(anyInt(), anyInt())).thenReturn(5);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraphReadMethods, null, true);
        Mockito.verify(mockJsonGenerator, times(1)).writeNumberField(attr.getName(), 5);
    }
}
