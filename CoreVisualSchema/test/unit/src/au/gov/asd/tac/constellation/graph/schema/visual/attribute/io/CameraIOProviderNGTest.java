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
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.CameraAttributeDescription;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.graphics.Frame;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.ArgumentCaptor;
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
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class CameraIOProviderNGTest extends ConstellationTest {

    // Create object under test
    CameraIOProvider instance;

    // Define mocks
    GraphReadMethods mockGraphReadMethods;
    GraphWriteMethods mockGraphWriteMethods;
    JsonNode mockJsonNode;
    JsonGenerator mockJsonGenerator;
    
    // Test variables
    final int attributeId = 23;
    final int elementId = 41;
    final String attribValue = "TestAttrib";
    final GraphAttribute attr = new GraphAttribute(attributeId, GraphElementType.VERTEX, "attrType", "attrName", "attrDesc",  null, null);

    
    public CameraIOProviderNGTest() {
        
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        instance = new CameraIOProvider();
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
     * Test of getName method, of class CameraIOProvider.
     */
    @Test
    public void testGetName() {
        System.out.println("CameraIOProviderNGTest.testGetName");
        assertEquals(instance.getName(), CameraAttributeDescription.ATTRIBUTE_NAME);
    }

    /**
     * Test of readObject method, of class CameraIOProvider.
     */
    @Test
    public void testReadObject() throws Exception {
        System.out.println("CameraIOProviderNGTest.testReadObject");

        // Create argument captors
        ArgumentCaptor<Integer> captorAtributeId = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> captorElementId = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Camera> captorCamera = ArgumentCaptor.forClass(Camera.class);
        
        // Call method under test with JsonNode.isNull=true and show nothing happens
        resetMocking();
        when(mockJsonNode.isNull()).thenReturn(true);
        instance.readObject(attributeId, elementId, mockJsonNode, mockGraphWriteMethods, null, null, null, null);
        Mockito.verify(mockJsonNode, times(0)).hasNonNull(anyString());

        // Call method under test with JsonNode.isNull=true and show nothing happens
        resetMocking();
        Camera expected = new Camera();
        expected.setVisibilityLow(0.0f);
        expected.setVisibilityHigh(1.0f);
        expected.setMixRatio(2);
        expected.lookAtEye.set(0.0f, 0.1f, 0.2f);
        expected.lookAtCentre.set(1.0f, 1.1f, 1.2f);
        expected.lookAtUp.set(2.0f, 2.1f, 2.2f);
        expected.lookAtRotation.set(3.0f, 3.1f, 3.2f);
        expected.lookAtPreviousEye.set(4.0f, 4.1f, 4.2f);
        expected.lookAtPreviousCentre.set(5.0f, 5.1f, 5.2f);
        expected.lookAtPreviousUp.set(6.0f, 6.1f, 6.2f);
        expected.lookAtPreviousRotation.set(7.0f, 7.1f, 7.2f);
        Frame objFrame = new Frame();
        objFrame.setOrigin(new Vector3f(0.0f, 0.1f, 0.2f));
        objFrame.setForwardVector(new Vector3f(1.0f, 1.1f, 1.2f));
        objFrame.setUpVector(new Vector3f(2.0f, 2.1f, 2.2f));
        expected.setObjectFrame(objFrame);
        expected.boundingBox.set(new Vector3f(0.0f, 0.1f, 0.2f), new Vector3f(1.0f, 1.1f, 1.2f), new Vector3f(2.0f, 2.1f, 2.2f), new Vector3f(3.0f, 3.1f, 3.2f));
        
        ObjectMapper mapper = new ObjectMapper();
        when(mockJsonNode.isNull()).thenReturn(false);
        final JsonNode testNode = mapper.readTree("{\"look_at_eye\": [0.0, 0.1, 0.2],\"look_at_centre\": [1.0, 1.1, 1.2],\"look_at_up\": [2.0, 2.1, 2.2],\"look_at_rotation\": [3.0, 3.1, 3.2],\"look_at_previous_eye\": [4.0, 4.1, 4.2],\"look_at_previous_centre\": [5.0, 5.1, 5.2],\"look_at_previous_up\": [6.0, 6.1, 6.2],\"look_at_previous_rotation\": [7.0, 7.1, 7.2],\"bounding_box\": {\"is_empty\": false, \"min\": [0.0, 0.1, 0.2], \"max\": [1.0, 1.1, 1.2], \"min2\": [2.0, 2.1, 2.2], \"max2\": [3.0, 3.1, 3.2]},\"frame\": {\"origin\": [0.0, 0.1, 0.2], \"forward\": [1.0, 1.1, 1.2], \"up\": [2.0, 2.1, 2.2]}, \"visibility_low\": 0.0, \"visibility_high\": 1.0, \"mix_ratio\": 2}");
        instance.readObject(attributeId, elementId, testNode, mockGraphWriteMethods, null, null, null, null);  
        Mockito.verify(mockGraphWriteMethods, times(1)).setObjectValue(captorAtributeId.capture(), captorElementId.capture(), captorCamera.capture());
        assertEquals((int)captorAtributeId.getValue(), attributeId);
        assertEquals((int)captorElementId.getValue(), elementId);
        assertEquals(expected.areSame(captorCamera.getValue()), true);
    }

    /**
     * Test of writeObject method, of class CameraIOProvider.
     */
    @Test
    public void testWriteObject() throws Exception {
        System.out.println("CameraIOProviderNGTest.testWriteObject");
        
        // Test case where not verbose and default graph value
        resetMocking();
        when(mockGraphReadMethods.isDefaultValue(attributeId, elementId)).thenReturn(true);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraphReadMethods, null, false);
        Mockito.verify(mockGraphReadMethods, times(0)).getObjectValue(anyInt(), anyInt());

        // Test case where not verbose and not default graph value
        resetMocking();
        when(mockGraphReadMethods.isDefaultValue(attributeId, elementId)).thenReturn(false);
        when(mockGraphReadMethods.getObjectValue(attributeId, elementId)).thenReturn(null);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraphReadMethods, null, false);
        Mockito.verify(mockGraphReadMethods, times(1)).getObjectValue(attributeId, elementId);
        Mockito.verify(mockJsonGenerator, times(1)).writeNullField(attr.getName());
        Mockito.verify(mockJsonGenerator, times(0)).writeArrayFieldStart(anyString());

        // Test case where verbose and not default graph value
        resetMocking();
        when(mockGraphReadMethods.isDefaultValue(attributeId, elementId)).thenReturn(false);
        when(mockGraphReadMethods.getObjectValue(attributeId, elementId)).thenReturn(null);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraphReadMethods, null, true);
        Mockito.verify(mockGraphReadMethods, times(1)).getObjectValue(attributeId, elementId);
        Mockito.verify(mockJsonGenerator, times(1)).writeNullField(attr.getName());
        Mockito.verify(mockJsonGenerator, times(0)).writeArrayFieldStart(anyString());

        // Test case where verbose and default graph value
        resetMocking();
        Camera camera = new Camera();
        camera.setVisibilityLow(0.0f);
        camera.setVisibilityHigh(1.0f);
        camera.setMixRatio(2);
        camera.lookAtEye.set(0.0f, 0.1f, 0.2f);
        camera.lookAtCentre.set(1.0f, 1.1f, 1.2f);
        camera.lookAtUp.set(2.0f, 2.1f, 2.2f);
        camera.lookAtRotation.set(3.0f, 3.1f, 3.2f);
        camera.lookAtPreviousEye.set(4.0f, 4.1f, 4.2f);
        camera.lookAtPreviousCentre.set(5.0f, 5.1f, 5.2f);
        camera.lookAtPreviousUp.set(6.0f, 6.1f, 6.2f);
        camera.lookAtPreviousRotation.set(7.0f, 7.1f, 7.2f);
        Frame objFrame = new Frame();
        objFrame.setOrigin(new Vector3f(0.0f, 0.1f, 0.2f));
        objFrame.setForwardVector(new Vector3f(1.0f, 1.1f, 1.2f));
        objFrame.setUpVector(new Vector3f(2.0f, 2.1f, 2.2f));
        camera.setObjectFrame(objFrame);
        camera.boundingBox.set(new Vector3f(0.0f, 0.1f, 0.2f), new Vector3f(1.0f, 1.1f, 1.2f), new Vector3f(2.0f, 2.1f, 2.2f), new Vector3f(3.0f, 3.1f, 3.2f));

        when(mockGraphReadMethods.isDefaultValue(attributeId, elementId)).thenReturn(true);
        when(mockGraphReadMethods.getObjectValue(attributeId, elementId)).thenReturn(camera);
        instance.writeObject(attr, elementId, mockJsonGenerator, mockGraphReadMethods, null, true);
        Mockito.verify(mockGraphReadMethods, times(1)).getObjectValue(attributeId, elementId);
        Mockito.verify(mockJsonGenerator, times(0)).writeNullField(anyString());
        Mockito.verify(mockJsonGenerator, times(1)).writeObjectFieldStart(attr.getName());
        Mockito.verify(mockJsonGenerator, times(1)).writeArrayFieldStart("look_at_eye");
        Mockito.verify(mockJsonGenerator, times(1)).writeArrayFieldStart("look_at_centre");
        Mockito.verify(mockJsonGenerator, times(1)).writeArrayFieldStart("look_at_up");
        Mockito.verify(mockJsonGenerator, times(1)).writeArrayFieldStart("look_at_rotation");
        Mockito.verify(mockJsonGenerator, times(1)).writeArrayFieldStart("look_at_previous_eye");
        Mockito.verify(mockJsonGenerator, times(1)).writeArrayFieldStart("look_at_previous_centre");
        Mockito.verify(mockJsonGenerator, times(1)).writeArrayFieldStart("look_at_previous_up");
        Mockito.verify(mockJsonGenerator, times(1)).writeArrayFieldStart("look_at_previous_rotation");
        Mockito.verify(mockJsonGenerator, times(1)).writeObjectFieldStart("frame");
        Mockito.verify(mockJsonGenerator, times(1)).writeArrayFieldStart("origin");
        Mockito.verify(mockJsonGenerator, times(1)).writeArrayFieldStart("forward");
        Mockito.verify(mockJsonGenerator, times(1)).writeArrayFieldStart("up");
        Mockito.verify(mockJsonGenerator, times(1)).writeObjectFieldStart("bounding_box");
        Mockito.verify(mockJsonGenerator, times(1)).writeBooleanField("is_empty", false);
        Mockito.verify(mockJsonGenerator, times(1)).writeArrayFieldStart("min");
        Mockito.verify(mockJsonGenerator, times(1)).writeArrayFieldStart("max");
        Mockito.verify(mockJsonGenerator, times(1)).writeArrayFieldStart("min2");
        Mockito.verify(mockJsonGenerator, times(1)).writeArrayFieldStart("max2");
        Mockito.verify(mockJsonGenerator, times(15)).writeEndArray();
        Mockito.verify(mockJsonGenerator, times(1)).writeNumberField("visibility_low", 0.0f);
        Mockito.verify(mockJsonGenerator, times(1)).writeNumberField("visibility_high", 1.0f);
        Mockito.verify(mockJsonGenerator, times(1)).writeNumberField("mix_ratio", 2);
        Mockito.verify(mockJsonGenerator, times(3)).writeEndObject();
        Mockito.verify(mockJsonGenerator, times(3)).writeNumber(0.0f);
        Mockito.verify(mockJsonGenerator, times(3)).writeNumber(0.1f);
        Mockito.verify(mockJsonGenerator, times(3)).writeNumber(0.2f);
        Mockito.verify(mockJsonGenerator, times(3)).writeNumber(1.0f);
        Mockito.verify(mockJsonGenerator, times(3)).writeNumber(1.1f);
        Mockito.verify(mockJsonGenerator, times(3)).writeNumber(1.2f);
        Mockito.verify(mockJsonGenerator, times(3)).writeNumber(2.0f);
        Mockito.verify(mockJsonGenerator, times(3)).writeNumber(2.1f);
        Mockito.verify(mockJsonGenerator, times(3)).writeNumber(2.2f);
        Mockito.verify(mockJsonGenerator, times(2)).writeNumber(3.0f);
        Mockito.verify(mockJsonGenerator, times(2)).writeNumber(3.1f);
        Mockito.verify(mockJsonGenerator, times(2)).writeNumber(3.2f);
        Mockito.verify(mockJsonGenerator, times(1)).writeNumber(4.0f);
        Mockito.verify(mockJsonGenerator, times(1)).writeNumber(4.1f);
        Mockito.verify(mockJsonGenerator, times(1)).writeNumber(4.2f);
        Mockito.verify(mockJsonGenerator, times(1)).writeNumber(5.0f);
        Mockito.verify(mockJsonGenerator, times(1)).writeNumber(5.1f);
        Mockito.verify(mockJsonGenerator, times(1)).writeNumber(5.2f);
        Mockito.verify(mockJsonGenerator, times(1)).writeNumber(6.0f);
        Mockito.verify(mockJsonGenerator, times(1)).writeNumber(6.1f);
        Mockito.verify(mockJsonGenerator, times(1)).writeNumber(6.2f);
        Mockito.verify(mockJsonGenerator, times(1)).writeNumber(7.0f);
        Mockito.verify(mockJsonGenerator, times(1)).writeNumber(7.1f);
        Mockito.verify(mockJsonGenerator, times(1)).writeNumber(7.2f);
    }
}
