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
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabel;
import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabels;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.mockito.ArgumentCaptor;
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
public final class AbstractGraphLabelsIOProviderNGTest {
    
    // Create object under test
    VertexGraphLabelsIOProvider instance;

    // Define mocks
    GraphWriteMethods mockGraph;
    JsonNode mockJsonNode;
    JsonGenerator mockJsonGenerator;
    ColorIOProvider mockColorIOProvider;

    // Create argument captors
    ArgumentCaptor<Integer> captorAtributeId;
    ArgumentCaptor<Integer> captorElementId;
    ArgumentCaptor<String> captorAttrNameStr;
    ArgumentCaptor<String> captorAttrValStr;
    ArgumentCaptor<GraphLabels> captorAttrVal;

    int attributeId = 23;
    int elementId = 41;
    GraphAttribute attr = new GraphAttribute(attributeId, GraphElementType.VERTEX, "attrType", "attrName", "attrDesc",  null, null);

    public AbstractGraphLabelsIOProviderNGTest() {
        resetMocking();
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        instance = new VertexGraphLabelsIOProvider();
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }
    
    /**
     * Perform reset of all mocks and argument captors to ensure clean test steps.
     */
    public void resetMocking() {
        mockGraph = mock(GraphWriteMethods.class);
        mockJsonNode = mock(JsonNode.class);
        mockJsonGenerator = mock(JsonGenerator.class);
        mockColorIOProvider = mock(ColorIOProvider.class);
        captorAtributeId = ArgumentCaptor.forClass(Integer.class);
        captorElementId = ArgumentCaptor.forClass(Integer.class);
        captorAttrNameStr = ArgumentCaptor.forClass(String.class);
        captorAttrValStr = ArgumentCaptor.forClass(String.class);
        captorAttrVal = ArgumentCaptor.forClass(GraphLabels.class);
    }

    /**
     * Test of readObject method, of class AbstractGraphLabelsIOProvider.
     */
    @Test
    public void testReadObject() throws Exception {
        System.out.println("AbstractGraphLabelsIOProvider.testReadObject");
        
        // Test case where isNull returns true and isArray is false
        resetMocking();
        when(mockJsonNode.isNull()).thenReturn(true);
        when(mockJsonNode.isArray()).thenReturn(false);
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraph, null, null, null, null);
        Mockito.verify(mockJsonNode, times(0)).textValue();
        Mockito.verify(mockGraph, times(1)).setStringValue(captorAtributeId.capture(), captorElementId.capture(), captorAttrValStr.capture());
        assertEquals((int)captorAtributeId.getValue(), attributeId);
        assertEquals((int)captorElementId.getValue(), elementId);
        assertEquals(captorAttrValStr.getValue(), null);

        // Test case where isNull returns false but isArray is true
        resetMocking();
        when(mockJsonNode.isNull()).thenReturn(false);
        when(mockJsonNode.isArray()).thenReturn(false);
        when(mockJsonNode.textValue()).thenReturn("attrVal");
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraph, null, null, null, null);
        Mockito.verify(mockJsonNode, times(1)).textValue();
        Mockito.verify(mockGraph, times(1)).setStringValue(captorAtributeId.capture(), captorElementId.capture(), captorAttrValStr.capture());
        assertEquals((int)captorAtributeId.getValue(), attributeId);
        assertEquals((int)captorElementId.getValue(), elementId);
        assertEquals(captorAttrValStr.getValue(), "attrVal");

        // Test case where isNull returns true and isArray is true (in reality this isnt possible
        // unless there was a bug in JsonNode
        resetMocking();
        when(mockJsonNode.isNull()).thenReturn(true);
        when(mockJsonNode.isArray()).thenReturn(true);
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraph, null, null, null, null);
        Mockito.verify(mockJsonNode, times(0)).textValue();
        Mockito.verify(mockGraph, times(1)).setStringValue(captorAtributeId.capture(), captorElementId.capture(), captorAttrValStr.capture());
        assertEquals((int)captorAtributeId.getValue(), attributeId);
        assertEquals((int)captorElementId.getValue(), elementId);
        assertEquals(captorAttrValStr.getValue(), null);
        
        // Test case where a valid non null array exists
        ObjectMapper mapper = new ObjectMapper();
        JsonNode testNode = mapper.readTree("[{\"attribute_name\":\"test_name1\",\"color\":{\"name\":\"red\"},\"radius\":0.1},{\"attribute_name\":\"test_name2\",\"color\":{\"name\":\"blue\"},\"radius\":0.2}]");
        resetMocking();
        instance.readObject(attributeId, elementId, testNode, mockGraph, null, null, null, null);
        Mockito.verify(mockGraph, times(1)).setObjectValue(captorAtributeId.capture(), captorElementId.capture(), captorAttrVal.capture());
        assertEquals((int)captorAtributeId.getValue(), attributeId);
        assertEquals((int)captorElementId.getValue(), elementId);
        GraphLabels labels = (GraphLabels)captorAttrVal.getValue();
        assertEquals(labels.getNumberOfLabels(), 2);
        assertEquals(labels.getLabels().get(0).getAttributeName(), "test_name1");
        assertEquals(labels.getLabels().get(1).getAttributeName(), "test_name2");
        assertEquals(labels.getLabels().get(0).getColor(), ConstellationColor.RED);
        assertEquals(labels.getLabels().get(1).getColor(), ConstellationColor.BLUE);
        assertEquals(labels.getLabels().get(0).getSize(), 0.1f);
        assertEquals(labels.getLabels().get(1).getSize(), 0.2f);
    }

    /**
     * Test of writeObject method, of class AbstractGraphLabelsIOProvider.
     */
    @Test
    public void testWriteObject() throws Exception {
        
        // Test case where not verbose and default graph value
        resetMocking();
        when(mockGraph.isDefaultValue(attributeId, elementId)).thenReturn(true);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraph, null, false);
        Mockito.verify(mockGraph, times(0)).getObjectValue(captorAtributeId.capture(), captorElementId.capture());

        // Test case where not verbose and not default graph value
        resetMocking();
        when(mockGraph.isDefaultValue(attributeId, elementId)).thenReturn(false);
        when(mockGraph.getObjectValue(attributeId, elementId)).thenReturn(null);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraph, null, false);
        Mockito.verify(mockGraph, times(1)).getObjectValue(captorAtributeId.capture(), captorElementId.capture());
        Mockito.verify(mockJsonGenerator, times(1)).writeNullField(captorAttrNameStr.capture());
        assertEquals(captorAttrNameStr.getValue(), attr.getName());

        // Test case where verbose and default graph value
        final List<GraphLabel> labels = new ArrayList<>();
        labels.add(new GraphLabel("test_name1", ConstellationColor.RED, 0.1f));
        labels.add(new GraphLabel("test_name2", ConstellationColor.BLUE, 0.2f));
        resetMocking();
        when(mockGraph.isDefaultValue(attributeId, elementId)).thenReturn(true);
        when(mockGraph.getObjectValue(attributeId, elementId)).thenReturn(new GraphLabels(labels));
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraph, null, true);
        Mockito.verify(mockGraph, times(1)).getObjectValue(captorAtributeId.capture(), captorElementId.capture());
        Mockito.verify(mockJsonGenerator, times(1)).writeArrayFieldStart(captorAttrNameStr.capture());
        Mockito.verify(mockJsonGenerator, times(2)).writeStartObject();
        Mockito.verify(mockJsonGenerator, times(1)).writeStringField("attribute_name", "test_name1");
        Mockito.verify(mockJsonGenerator, times(1)).writeStringField("attribute_name", "test_name2");
        Mockito.verify(mockJsonGenerator, times(2)).writeObjectFieldStart("color");
        Mockito.verify(mockJsonGenerator, times(1)).writeNumberField("radius", 0.1f);
        Mockito.verify(mockJsonGenerator, times(1)).writeNumberField("radius", 0.2f);
        Mockito.verify(mockJsonGenerator, times(4)).writeEndObject();

        // Test case where verbose and not default graph value
        resetMocking();
        when(mockGraph.isDefaultValue(attributeId, elementId)).thenReturn(false);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraph, null, true);
        Mockito.verify(mockGraph, times(1)).getObjectValue(captorAtributeId.capture(), captorElementId.capture());
    }
}
