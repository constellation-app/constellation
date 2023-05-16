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
package au.gov.tac.constellation.views.mapview2.utilities;

import au.gov.asd.tac.constellation.views.mapview2.utilities.Vec3;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author altair1673
 */
public class Vec3NGTest {

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
     * Test of multiplyFloat method, of class Vec3.
     */
    @Test
    public void testMultiplyFloat() {
        System.out.println("multiplyFloat");
        final float value = 3.0f;
        final Vec3 instance = new Vec3(4, 4, 4);
        instance.multiplyFloat(value);

        assertEquals(instance.getX(), 12.0);
        assertEquals(instance.getY(), 12.0);
        assertEquals(instance.getZ(), 12.0);
    }

    /**
     * Test of multiplyDouble method, of class Vec3.
     */
    @Test
    public void testMultiplyDouble() {
        System.out.println("multiplyDouble");
        final double value = 4;
        final Vec3 instance = new Vec3(5, 5, 5);
        instance.multiplyDouble(value);

        assertEquals(instance.getX(), 20.0);
        assertEquals(instance.getY(), 20.0);
        assertEquals(instance.getZ(), 20.0);
    }

    /**
     * Test of addVector method, of class Vec3.
     */
    @Test
    public void testAddVector() {
        System.out.println("addVector");
        final Vec3 value = new Vec3(1, 2, 3);
        final Vec3 instance = new Vec3(4, 5, 6);
        instance.addVector(value);

        assertEquals(instance.getX(), 5.0);
        assertEquals(instance.getY(), 7.0);
        assertEquals(instance.getZ(), 9.0);
    }

    /**
     * Test of divVector method, of class Vec3.
     */
    @Test
    public void testDivVector() {
        System.out.println("divVector");
        double d = 0.0;
        final Vec3 instance = new Vec3(5, 5, 5);
        instance.divVector(d);

        assertEquals(instance.getX(), 5.0);
        assertEquals(instance.getY(), 5.0);
        assertEquals(instance.getZ(), 5.0);

        d = 2;
        instance.divVector(d);

        assertEquals(instance.getX(), 2.5);
        assertEquals(instance.getY(), 2.5);
        assertEquals(instance.getZ(), 2.5);
    }

    /**
     * Test of getDistance method, of class Vec3.
     */
    @Test
    public void testGetDistance() {
        System.out.println("getDistance");
        final Vec3 v1 = new Vec3(1, 1, 1);
        final Vec3 v2 = new Vec3(4, 5, 1);
        final double expResult = 5.0;
        final double result = Vec3.getDistance(v1, v2);
        assertEquals(result, expResult);
    }

    /**
     * Test of cross method, of class Vec3.
     */
    @Test
    public void testCross() {
        System.out.println("cross");

        final Vec3 v = new Vec3(3, 1, 4);

        final Vec3 instance = new Vec3(-2, 0, 5);

        final Vec3 expResult = new Vec3(5, -23, 2);

        final Vec3 result = v.cross(instance);

        assertEquals(result.getX(), expResult.getX());
        assertEquals(result.getY(), expResult.getY());
        assertEquals(result.getZ(), expResult.getZ());

    }

    /**
     * Test of getX method, of class Vec3.
     */
    @Test
    public void testGetX() {
        System.out.println("getX");
        final Vec3 instance = new Vec3(3, 3, 3);
        final double expResult = 3.0;
        final double result = instance.getX();
        assertEquals(result, expResult);
    }

    /**
     * Test of setX method, of class Vec3.
     */
    @Test
    public void testSetX() {
        System.out.println("setX");
        final double x = 6;
        final Vec3 instance = new Vec3();
        instance.setX(x);

        assertEquals(instance.getX(), x);
    }

    /**
     * Test of getY method, of class Vec3.
     */
    @Test
    public void testGetY() {
        System.out.println("getY");
        final Vec3 instance = new Vec3(3, 3, 3);
        final double expResult = 3.0;
        final double result = instance.getY();
        assertEquals(result, expResult);
    }

    /**
     * Test of setY method, of class Vec3.
     */
    @Test
    public void testSetY() {
        System.out.println("setY");
        final double y = 6;
        final Vec3 instance = new Vec3();
        instance.setY(y);

        assertEquals(instance.getY(), y);
    }

    /**
     * Test of getZ method, of class Vec3.
     */
    @Test
    public void testGetZ() {
        System.out.println("getZ");
        final Vec3 instance = new Vec3(3, 3, 3);
        final double expResult = 3.0;
        final double result = instance.getZ();
        assertEquals(result, expResult);
    }

    /**
     * Test of setZ method, of class Vec3.
     */
    @Test
    public void testSetZ() {
        System.out.println("setZ");
        final double z = 6;
        final Vec3 instance = new Vec3();
        instance.setZ(z);

        assertEquals(instance.getZ(), z);
    }

}
