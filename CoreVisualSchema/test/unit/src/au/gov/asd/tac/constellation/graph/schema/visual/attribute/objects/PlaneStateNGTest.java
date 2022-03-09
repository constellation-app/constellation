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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author serpens24
 */
public class PlaneStateNGTest {

    // Create object under test
    final BufferedImage bufferedImage = new BufferedImage(1, 2, 3);
    final Plane plane1 = new Plane("label1", 1.0f, 2.0f, 3.0f, 4.0f, 5.0f, bufferedImage, 7, 8);
    final Plane plane2 = new Plane("label2", 1.0f, 2.0f, 3.0f, 4.0f, 5.0f, bufferedImage, 7, 8);
    final Plane plane3 = new Plane("label3", 1.0f, 2.0f, 3.0f, 4.0f, 5.0f, bufferedImage, 7, 8);
    final PlaneState instance = new PlaneState();
    
    public PlaneStateNGTest() {
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
     * Test of addPlane method, of class PlaneState.
     */
    @Test
    public void testPlaneState() {
        System.out.println("PlaneStateNGTest.testPlaneState");
        instance.addPlane(plane1);
        assertEquals(instance.getPlanes().size(), 1);
        assertEquals(instance.getVisiblePlanes().toString(), "{0}");
        Plane receivedPlane = instance.getPlane(0);
        assertEquals(receivedPlane.toString(), plane1.toString()); 
        instance.removePlane(0);
        assertEquals(instance.getPlanes().size(), 0);
        assertEquals(instance.getVisiblePlanes().toString(), "{}");
        plane1.setVisible(false);
        
        List<Plane> planeList = new ArrayList<>();
        planeList.add(plane1);
        instance.setPlanes(planeList);
        assertEquals(instance.getPlanes().size(), 1);
        assertEquals(instance.getVisiblePlanes().toString(), "{}");
        
        planeList.add(plane2);
        planeList.add(plane3);
        instance.setPlanes(planeList);
        assertEquals(instance.getPlanes().size(), 3);
        assertEquals(instance.getVisiblePlanes().toString(), "{1, 2}");
        
        BitSet bitset = instance.getVisiblePlanes();
        plane2.setVisible(false);
        plane3.setVisible(false);
        instance.setPlanes(planeList);
        assertEquals(instance.getPlanes().size(), 3);
        assertEquals(instance.getVisiblePlanes().toString(), "{}");
        assertFalse(instance.isVisibilityUpdate());
        instance.setVisiblePlanes(bitset);
        assertEquals(instance.getPlanes().size(), 3);
        assertEquals(instance.getVisiblePlanes().toString(), "{1, 2}");
        
        assertEquals(instance.toString(), "%s[\nPlane[label1@(1.000000,2.000000,3.000000) 7x8]\nPlane[label2@(1.000000,2.000000,3.000000) 7x8]\nPlane[label3@(1.000000,2.000000,3.000000) 7x8]\n]");   

        PlaneState newPlaneState = new PlaneState(instance);
        assertEquals(newPlaneState.toString(), instance.toString());   

    }
}
