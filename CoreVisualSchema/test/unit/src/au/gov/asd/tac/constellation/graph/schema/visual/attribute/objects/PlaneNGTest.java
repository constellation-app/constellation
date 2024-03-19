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
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
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
 public class PlaneNGTest extends ConstellationTest {
        
    // Create object under test
    final BufferedImage bufferedImage = new BufferedImage(1, 2, 3);
    Plane instance;

    public PlaneNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        System.out.println("PlaneNGTest.");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        System.out.println("PlaneNGTest.");
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        instance = new Plane("label", 1.0f, 2.0f, 3.0f, 4.0f, 5.0f, bufferedImage, 7, 8);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of getImage method, of class Plane.
     */
    @Test
    public void testGetImage() {
        System.out.println("PlaneNGTest.testGetImage");
        final BufferedImage value = instance.getImage();
        assertEquals(value.getWidth(), bufferedImage.getWidth());
        assertEquals(value.getHeight(), bufferedImage.getHeight());
        assertEquals(value.getType(), bufferedImage.getType());
    }

    /**
     * Test of getLabel method, of class Plane.
     */
    @Test
    public void testGetLabel() {
        System.out.println("PlaneNGTest.testGetLabel");
        assertEquals(instance.getLabel(), "label");
    }

    /**
     * Test of getX method, of class Plane.
     */
    @Test
    public void testGetX() {
        System.out.println("PlaneNGTest.testGetX");
        assertEquals(instance.getX(), 1.0f);
    }

    /**
     * Test of setX method, of class Plane.
     */
    @Test
    public void testSetX() {
        System.out.println("PlaneNGTest.testSetX");
        instance.setX(1.1f);
        assertEquals(instance.getX(), 1.1f);
    }

    /**
     * Test of getY method, of class Plane.
     */
    @Test
    public void testGetY() {
        System.out.println("PlaneNGTest.testGetY");
        assertEquals(instance.getY(), 2.0f);
    }

    /**
     * Test of setY method, of class Plane.
     */
    @Test
    public void testSetY() {
        System.out.println("PlaneNGTest.testSetY");
        instance.setY(2.1f);
        assertEquals(instance.getY(), 2.1f);
    }

    /**
     * Test of getZ method, of class Plane.
     */
    @Test
    public void testGetZ() {
        System.out.println("PlaneNGTest.testGetZ");
        assertEquals(instance.getZ(), 3.0f);
    }

    /**
     * Test of setZ method, of class Plane.
     */
    @Test
    public void testSetZ() {
        System.out.println("PlaneNGTest.testSetZ");
        instance.setZ(3.1f);
        assertEquals(instance.getZ(), 3.1f);
    }

    /**
     * Test of getWidth method, of class Plane.
     */
    @Test
    public void testGetWidth() {
        System.out.println("PlaneNGTest.testGetWidth");
        assertEquals(instance.getWidth(), 4.0f);
    }

    /**
     * Test of setWidth method, of class Plane.
     */
    @Test
    public void testSetWidth() {
        System.out.println("PlaneNGTest.testSetWidth");
        instance.setWidth(4.1f);
        assertEquals(instance.getWidth(), 4.1f);
    }

    /**
     * Test of getHeight method, of class Plane.
     */
    @Test
    public void testGetHeight() {
        System.out.println("PlaneNGTest.testGetHeight");
        assertEquals(instance.getHeight(), 5.0f);
    }

    /**
     * Test of setHeight method, of class Plane.
     */
    @Test
    public void testSetHeight() {
        System.out.println("PlaneNGTest.testSetHeight");
        instance.setHeight(5.1f);
        assertEquals(instance.getHeight(), 5.1f);
    }

    /**
     * Test of getImageWidth method, of class Plane.
     */
    @Test
    public void testGetImageWidth() {
        System.out.println("PlaneNGTest.testGetImageWidth");
        assertEquals(instance.getImageWidth(), 7);
    }

    /**
     * Test of getImageHeight method, of class Plane.
     */
    @Test
    public void testGetImageHeight() {
        System.out.println("PlaneNGTest.testGetImageHeight");
        assertEquals(instance.getImageHeight(), 8);
    }

    /**
     * Test of isVisible method, of class Plane.
     */
    @Test
    public void testIsVisible() {
        System.out.println("PlaneNGTest.testIsVisible");
        assertTrue(instance.isVisible());
    }

    /**
     * Test of setVisible method, of class Plane.
     */
    @Test
    public void testSetVisible() {
        System.out.println("PlaneNGTest.testSetVisible");
        instance.setVisible(false);
        assertFalse(instance.isVisible());
    }

    /**
     * Test of readNode method, of class Plane.
     */
    @Test
    public void testReadNode() throws Exception {
        System.out.println("PlaneNGTest.testReadNode");
        // Not currently implemented, beed to deal with ImageIO
        
    }

    /**
     * Test of writeNode method, of class Plane.
     */
    @Test
    public void testWriteNode() throws Exception {
        System.out.println("PlaneNGTest.testWriteNode");
        // Not currently implemented, beed to deal with ImageIO
    }

    /**
     * Test of toString method, of class Plane.
     */
    @Test
    public void testToString() {
        System.out.println("PlaneNGTest.testToString");
        assertEquals(instance.toString(), "Plane[label@(1.000000,2.000000,3.000000) 7x8]");
    }
}
