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
package au.gov.asd.tac.constellation.utilities.datastructure;

import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Four Tuple Test
 *
 * @author arcturus
 */
public class FourTupleNGTest {
    
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
     * Test of create method, of class FourTuple.
     */
    @Test
    public void testCreate() {
        final String first = "first";
        final String second = "second";
        final String third = "third";
        final String fourth = "fourth";
        final FourTuple<String, String, String, String> instance = FourTuple.create(first, second, third, fourth);
        assertEquals("first", instance.getFirst());
        assertEquals("second", instance.getSecond());
        assertEquals("third", instance.getThird());
    }

    /**
     * Test of getFirst method, of class FourTuple.
     */
    @Test
    public void testGetFirst() {
        final String first = "first";
        final String second = "second";
        final String third = "third";
        final String fourth = "fourth";
        final FourTuple<String, String, String, String> instance = FourTuple.create(first, second, third, fourth);
        final String result = instance.getFirst();
        assertEquals(result, "first");
    }

    /**
     * Test of setFirst method, of class FourTuple.
     */
    @Test
    public void testSetFirst() {
        final String first = "first";
        final String second = "second";
        final String third = "third";
        final String fourth = "fourth";
        final FourTuple<String, String, String, String> instance = FourTuple.create(first, second, third, fourth);
        instance.setFirst("updated");
        assertEquals(instance.getFirst(), "updated");
    }

    /**
     * Test of getSecond method, of class FourTuple.
     */
    @Test
    public void testGetSecond() {
        final String first = "first";
        final String second = "second";
        final String third = "third";
        final String fourth = "fourth";
        final FourTuple<String, String, String, String> instance = FourTuple.create(first, second, third, fourth);
        final String result = instance.getSecond();
        assertEquals(result, "second");
    }

    /**
     * Test of setSecond method, of class FourTuple.
     */
    @Test
    public void testSetSecond() {
        final String first = "first";
        final String second = "second";
        final String third = "third";
        final String fourth = "fourth";
        final FourTuple<String, String, String, String> instance = FourTuple.create(first, second, third, fourth);
        instance.setSecond("updated");
        assertEquals(instance.getSecond(), "updated");
    }

    /**
     * Test of getThird method, of class FourTuple.
     */
    @Test
    public void testGetThird() {
        final String first = "first";
        final String second = "second";
        final String third = "third";
        final String fourth = "fourth";
        final FourTuple<String, String, String, String> instance = FourTuple.create(first, second, third, fourth);
        final String result = instance.getThird();
        assertEquals(result, "third");
    }

    /**
     * Test of setThird method, of class FourTuple.
     */
    @Test
    public void testSetThird() {
        final String first = "first";
        final String second = "second";
        final String third = "third";
        final String fourth = "fourth";
        final FourTuple<String, String, String, String> instance = FourTuple.create(first, second, third, fourth);
        instance.setThird("updated");
        assertEquals(instance.getThird(), "updated");
    }

    /**
     * Test of getFourth method, of class FourTuple.
     */
    @Test
    public void testGetFourth() {
        final String first = "first";
        final String second = "second";
        final String third = "third";
        final String fourth = "fourth";
        final FourTuple<String, String, String, String> instance = FourTuple.create(first, second, third, fourth);
        final String result = instance.getFourth();
        assertEquals(result, "fourth");
    }

    /**
     * Test of setFourth method, of class FourTuple.
     */
    @Test
    public void testSetFourth() {
        final String first = "first";
        final String second = "second";
        final String third = "third";
        final String fourth = "fourth";
        final FourTuple<String, String, String, String> instance = FourTuple.create(first, second, third, fourth);
        instance.setFourth("updated");
        assertEquals(instance.getFourth(), "updated");
    }

    /**
     * Test of equals method, of class FourTuple.
     */
    @Test
    public void testEquals() {
        final String first = "first";
        final String second = "second";
        final String third = "third";
        final String fourth = "fourth";
        final FourTuple<String, String, String, String> instance1 = FourTuple.create(first, second, third, fourth);
        final FourTuple<String, String, String, String> instance2 = FourTuple.create(first, second, third, fourth);

        final boolean expResult = true;
        final boolean result = instance1.equals(instance2);
        assertEquals(result, expResult);
    }

    /**
     * Test of toString method, of class FourTuple.
     */
    @Test
    public void testToString() {
        final String first = "first";
        final String second = "second";
        final String third = "third";
        final String fourth = "fourth";
        final FourTuple<String, String, String, String> instance = FourTuple.create(first, second, third, fourth);

        final String expResult = "(first, second, third, fourth)";
        final String result = instance.toString();
        assertEquals(result, expResult);
    }

    /**
     * Test of compareTo method, of class FourTuple.
     */
    @Test
    public void testCompareToWhereFirstIsLessThanSecond() {
        final String first = "apple";
        final String second = "banana";
        final String third = "carot";
        final String fourth = "dumpling";
        final FourTuple<String, String, String, String> instance1 = FourTuple.create(first, second, third, fourth);
        final FourTuple<String, String, String, String> instance2 = FourTuple.create(second, first, third, fourth);

        final int expResult = -1;
        final int result = instance1.compareTo(instance2);
        assertEquals(result, expResult);
    }

    /**
     * Test of compareTo method, of class FourTuple.
     */
    @Test
    public void testCompareToWhereBothAreEqual() {
        final String first = "apple";
        final String second = "apple";
        final String third = "carot";
        final String fourth = "dumpling";
        final FourTuple<String, String, String, String> instance1 = FourTuple.create(first, second, third, fourth);
        final FourTuple<String, String, String, String> instance2 = FourTuple.create(first, second, third, fourth);

        final int expResult = 0;
        final int result = instance1.compareTo(instance2);
        assertEquals(result, expResult);
    }

    /**
     * Test of compareTo method, of class FourTuple.
     */
    @Test
    public void testCompareToWhereFirstIsGreaterThanSecond() {
        final String first = "apple";
        final String second = "banana";
        final String third = "carot";
        final String fourth = "dumpling";
        final FourTuple<String, String, String, String> instance1 = FourTuple.create(first, second, third, fourth);
        final FourTuple<String, String, String, String> instance2 = FourTuple.create(second, first, third, fourth);

        final int expResult = 1;
        final int result = instance2.compareTo(instance1);
        assertEquals(result, expResult);
    }

    /**
     * Test of compareTo method, of class FourTuple.
     */
    @Test
    public void testCompareToWhereSecondIsLessThanThird() {
        final String first = "apple";
        final String second = "banana";
        final String third = "carot";
        final String fourth = "dumpling";
        final FourTuple<String, String, String, String> instance1 = FourTuple.create(first, second, third, fourth);
        final FourTuple<String, String, String, String> instance2 = FourTuple.create(first, third, second, fourth);

        final int expResult = -1;
        final int result = instance1.compareTo(instance2);
        assertEquals(result, expResult);
    }

    /**
     * Test of compareTo method, of class FourTuple.
     */
    @Test
    public void testCompareToWhereSecondIsGreaterThanThird() {
        final String first = "apple";
        final String second = "banana";
        final String third = "carot";
        final String fourth = "dumpling";
        final FourTuple<String, String, String, String> instance1 = FourTuple.create(first, second, third, fourth);
        final FourTuple<String, String, String, String> instance2 = FourTuple.create(first, third, second, fourth);

        final int expResult = 1;
        final int result = instance2.compareTo(instance1);
        assertEquals(result, expResult);
    }

    /**
     * Test of compareTo method, of class FourTuple.
     */
    @Test
    public void testCompareToWhereThirdIsLessThanFourth() {
        final String first = "apple";
        final String second = "banana";
        final String third = "carot";
        final String fourth = "dumpling";
        final FourTuple<String, String, String, String> instance1 = FourTuple.create(first, second, third, fourth);
        final FourTuple<String, String, String, String> instance2 = FourTuple.create(first, second, fourth, third);

        final int expResult = -1;
        final int result = instance1.compareTo(instance2);
        assertEquals(result, expResult);
    }

    /**
     * Test of compareTo method, of class FourTuple.
     */
    @Test
    public void testCompareToWhereThirdIsGreaterThanFourth() {
        final String first = "apple";
        final String second = "banana";
        final String third = "carot";
        final String fourth = "dumpling";
        final FourTuple<String, String, String, String> instance1 = FourTuple.create(first, second, third, fourth);
        final FourTuple<String, String, String, String> instance2 = FourTuple.create(first, third, fourth, third);

        final int expResult = 1;
        final int result = instance2.compareTo(instance1);
        assertEquals(result, expResult);
    }
}
