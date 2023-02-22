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
package au.gov.tac.constellation.views.mapview2.utillities;

import static org.testng.Assert.*;
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

    public Vec3NGTest() {
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
     * Test of multiplyFloat method, of class Vec3.
     */
    @Test
    public void testMultiplyFloat() {
        System.out.println("multiplyFloat");
        float value = 3.0f;
        Vec3 instance = new Vec3(4, 4, 4);
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
        double value = 4;
        Vec3 instance = new Vec3(5, 5, 5);
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
        Vec3 value = new Vec3(1, 2, 3);
        Vec3 instance = new Vec3(4, 5, 6);
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
        Vec3 instance = new Vec3(5, 5, 5);
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
        Vec3 v1 = new Vec3(1, 1, 1);
        Vec3 v2 = new Vec3(4, 5, 1);
        double expResult = 5.0;
        double result = Vec3.getDistance(v1, v2);
        assertEquals(result, expResult);
    }

    /**
     * Test of cross method, of class Vec3.
     */
    @Test
    public void testCross() {
        System.out.println("cross");

        Vec3 v = new Vec3(3, 1, 4);

        Vec3 instance = new Vec3(-2, 0, 5);

        Vec3 expResult = new Vec3(5, -23, 2);

        Vec3 result = v.cross(instance);

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
        Vec3 instance = new Vec3(3, 3, 3);
        double expResult = 3.0;
        double result = instance.getX();
        assertEquals(result, expResult);
    }

    /**
     * Test of setX method, of class Vec3.
     */
    @Test
    public void testSetX() {
        System.out.println("setX");
        double x = 6;
        Vec3 instance = new Vec3();
        instance.setX(x);

        assertEquals(instance.getX(), x);
    }

    /**
     * Test of getY method, of class Vec3.
     */
    @Test
    public void testGetY() {
        System.out.println("getY");
        Vec3 instance = new Vec3(3, 3, 3);
        double expResult = 3.0;
        double result = instance.getY();
        assertEquals(result, expResult);
    }

    /**
     * Test of setY method, of class Vec3.
     */
    @Test
    public void testSetY() {
        System.out.println("setY");
        double y = 6;
        Vec3 instance = new Vec3();
        instance.setY(y);

        assertEquals(instance.getY(), y);
    }

    /**
     * Test of getZ method, of class Vec3.
     */
    @Test
    public void testGetZ() {
        System.out.println("getZ");
        Vec3 instance = new Vec3(3, 3, 3);
        double expResult = 3.0;
        double result = instance.getZ();
        assertEquals(result, expResult);
    }

    /**
     * Test of setZ method, of class Vec3.
     */
    @Test
    public void testSetZ() {
        System.out.println("setZ");
        double z = 6;
        Vec3 instance = new Vec3();
        instance.setZ(z);

        assertEquals(instance.getZ(), z);
    }

}
