/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.utilities.datastructure;

import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Three Tuple Test
 *
 * @author arcturus
 */
public class ThreeTupleNGTest {
    
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
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of create method, of class ThreeTuple.
     */
    @Test
    public void testCreate() {
        final String first = "first";
        final String second = "second";
        final String third = "third";
        final ThreeTuple<String, String, String> instance = ThreeTuple.create(first, second, third);
        assertEquals("first", instance.getFirst());
        assertEquals("second", instance.getSecond());
        assertEquals("third", instance.getThird());
    }

    /**
     * Test of getFirst method, of class ThreeTuple.
     */
    @Test
    public void testGetFirst() {
        final String first = "first";
        final String second = "second";
        final String third = "third";
        final ThreeTuple<String, String, String> instance = ThreeTuple.create(first, second, third);
        final String result = instance.getFirst();
        assertEquals(result, "first");
    }

    /**
     * Test of setFirst method, of class ThreeTuple.
     */
    @Test
    public void testSetFirst() {
        final String first = "first";
        final String second = "second";
        final String third = "third";
        final ThreeTuple<String, String, String> instance = ThreeTuple.create(first, second, third);
        instance.setFirst("updated");
        assertEquals(instance.getFirst(), "updated");
    }

    /**
     * Test of getSecond method, of class ThreeTuple.
     */
    @Test
    public void testGetSecond() {
        final String first = "first";
        final String second = "second";
        final String third = "third";
        final ThreeTuple<String, String, String> instance = ThreeTuple.create(first, second, third);
        final String result = instance.getSecond();
        assertEquals(result, "second");
    }

    /**
     * Test of setSecond method, of class ThreeTuple.
     */
    @Test
    public void testSetSecond() {
        final String first = "first";
        final String second = "second";
        final String third = "third";
        final ThreeTuple<String, String, String> instance = ThreeTuple.create(first, second, third);
        instance.setSecond("updated");
        assertEquals(instance.getSecond(), "updated");
    }

    /**
     * Test of getThird method, of class ThreeTuple.
     */
    @Test
    public void testGetThird() {
        final String first = "first";
        final String second = "second";
        final String third = "third";
        final ThreeTuple<String, String, String> instance = ThreeTuple.create(first, second, third);
        final String result = instance.getThird();
        assertEquals(result, "third");
    }

    /**
     * Test of setThird method, of class ThreeTuple.
     */
    @Test
    public void testSetThird() {
        final String first = "first";
        final String second = "second";
        final String third = "third";
        final ThreeTuple<String, String, String> instance = ThreeTuple.create(first, second, third);
        instance.setThird("updated");
        assertEquals(instance.getThird(), "updated");
    }

    /**
     * Test of equals method, of class ThreeTuple.
     */
    @Test
    public void testEquals() {
        final String first = "first";
        final String second = "second";
        final String third = "third";
        final ThreeTuple<String, String, String> instance1 = ThreeTuple.create(first, second, third);
        final ThreeTuple<String, String, String> instance2 = ThreeTuple.create(first, second, third);

        final boolean expResult = true;
        final boolean result = instance1.equals(instance2);
        assertEquals(result, expResult);
    }

    /**
     * Test of toString method, of class ThreeTuple.
     */
    @Test
    public void testToString() {
        final String first = "first";
        final String second = "second";
        final String third = "third";
        final ThreeTuple<String, String, String> instance = ThreeTuple.create(first, second, third);

        final String expResult = "(first, second, third)";
        final String result = instance.toString();
        assertEquals(result, expResult);
    }

    /**
     * Test of compareTo method, of class ThreeTuple.
     */
    @Test
    public void testCompareToWhereFirstIsLessThanSecond() {
        final String first = "apple";
        final String second = "banana";
        final String third = "carot";
        final ThreeTuple<String, String, String> instance1 = ThreeTuple.create(first, second, third);
        final ThreeTuple<String, String, String> instance2 = ThreeTuple.create(second, first, third);

        final int expResult = -1;
        final int result = instance1.compareTo(instance2);
        assertEquals(result, expResult);
    }

    /**
     * Test of compareTo method, of class ThreeTuple.
     */
    @Test
    public void testCompareToWhereBothAreEqual() {
        final String first = "apple";
        final String second = "apple";
        final String third = "carot";
        final ThreeTuple<String, String, String> instance1 = ThreeTuple.create(first, second, third);
        final ThreeTuple<String, String, String> instance2 = ThreeTuple.create(first, second, third);

        final int expResult = 0;
        final int result = instance1.compareTo(instance2);
        assertEquals(result, expResult);
    }

    /**
     * Test of compareTo method, of class ThreeTuple.
     */
    @Test
    public void testCompareToWhereFirstIsGreaterThanSecond() {
        final String first = "apple";
        final String second = "banana";
        final String third = "carot";
        final ThreeTuple<String, String, String> instance1 = ThreeTuple.create(first, second, third);
        final ThreeTuple<String, String, String> instance2 = ThreeTuple.create(second, first, third);

        final int expResult = 1;
        final int result = instance2.compareTo(instance1);
        assertEquals(result, expResult);
    }

    /**
     * Test of compareTo method, of class ThreeTuple.
     */
    @Test
    public void testCompareToWhereSecondIsLessThanThird() {
        final String first = "apple";
        final String second = "banana";
        final String third = "carot";
        final ThreeTuple<String, String, String> instance1 = ThreeTuple.create(first, second, third);
        final ThreeTuple<String, String, String> instance2 = ThreeTuple.create(first, third, second);

        final int expResult = -1;
        final int result = instance1.compareTo(instance2);
        assertEquals(result, expResult);
    }

    /**
     * Test of compareTo method, of class ThreeTuple.
     */
    @Test
    public void testCompareToWhereSecondIsGreaterThanThird() {
        final String first = "apple";
        final String second = "banana";
        final String third = "carot";
        final ThreeTuple<String, String, String> instance1 = ThreeTuple.create(first, second, third);
        final ThreeTuple<String, String, String> instance2 = ThreeTuple.create(first, third, second);

        final int expResult = 1;
        final int result = instance2.compareTo(instance1);
        assertEquals(result, expResult);
    }
}
