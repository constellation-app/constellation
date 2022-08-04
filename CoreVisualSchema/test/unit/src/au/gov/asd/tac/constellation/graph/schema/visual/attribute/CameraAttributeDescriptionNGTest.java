/*
 * Copyright 2010-2021 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute;

import au.gov.asd.tac.constellation.utilities.camera.Camera;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class CameraAttributeDescriptionNGTest {

    public CameraAttributeDescriptionNGTest() {
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
     * Test of convertFromObject method, of class CameraAttributeDescription.
     */
    @Test
    public void testConvertFromObject() {
        System.out.println("convertFromObject");

        final CameraAttributeDescription instance = new CameraAttributeDescription();

        final Object nullResult = instance.convertFromObject(null);
        assertNull(nullResult);

        final Object stringResult = instance.convertFromObject("");
        assertTrue(stringResult instanceof Camera);
        final Camera resultCam = (Camera) stringResult;
        assertTrue(resultCam.areSame(new Camera()));

        final Object otherResult = instance.convertFromObject(42);
        // should return itself
        assertEquals(otherResult, 42);
    }

    /**
     * Test of getVersion method, of class CameraAttributeDescription.
     */
    @Test
    public void testGetVersion() {
        System.out.println("getVersion");

        final CameraAttributeDescription instance = new CameraAttributeDescription();
        assertEquals(instance.getVersion(), 1);
    }

}