/*
 * Copyright 2010-2020 Australian Signals Directorate
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
 * Tuple Test
 *
 * @author arcturus
 */
public class TupleNGTest {

    public TupleNGTest() {
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
     * Test of create method, of class Tuple.
     */
    @Test
    public void testCreate() {
        final String first = "first";
        final String second = "second";
        final Tuple<String, String> instance = Tuple.create(first, second);
        assertEquals("first", instance.getFirst());
        assertEquals("second", instance.getSecond());
    }

    /**
     * Test of getFirst method, of class Tuple.
     */
    @Test
    public void testGetFirst() {
        final String first = "first";
        final String second = "second";
        final Tuple<String, String> instance = Tuple.create(first, second);
        final String result = instance.getFirst();
        assertEquals(result, "first");
    }

    /**
     * Test of setFirst method, of class Tuple.
     */
    @Test
    public void testSetFirst() {
        final String first = "first";
        final String second = "second";
        final Tuple<String, String> instance = Tuple.create(first, second);
        instance.setFirst("updated");
        assertEquals(instance.getFirst(), "updated");
    }

    /**
     * Test of getSecond method, of class Tuple.
     */
    @Test
    public void testGetSecond() {
        final String first = "first";
        final String second = "second";
        final Tuple<String, String> instance = Tuple.create(first, second);
        final String result = instance.getSecond();
        assertEquals(result, "second");
    }

    /**
     * Test of setSecond method, of class Tuple.
     */
    @Test
    public void testSetSecond() {
        final String first = "first";
        final String second = "second";
        final Tuple<String, String> instance = Tuple.create(first, second);
        instance.setSecond("updated");
        assertEquals(instance.getSecond(), "updated");
    }

    /**
     * Test of equals method, of class Tuple.
     */
    @Test
    public void testEquals() {
        final String first = "first";
        final String second = "second";
        final Tuple<String, String> instance1 = Tuple.create(first, second);
        final Tuple<String, String> instance2 = Tuple.create(first, second);

        final boolean expResult = true;
        final boolean result = instance1.equals(instance2);
        assertEquals(result, expResult);
    }

    /**
     * Test of toString method, of class Tuple.
     */
    @Test
    public void testToString() {
        final String first = "first";
        final String second = "second";
        final Tuple<String, String> instance = Tuple.create(first, second);

        final String expResult = "(first, second)";
        final String result = instance.toString();
        assertEquals(result, expResult);
    }

    /**
     * Test of compareTo method, of class Tuple.
     */
    @Test
    public void testCompareToWhereFirstIsLessThanSecond() {
        final String first = "apple";
        final String second = "banana";
        final Tuple<String, String> instance1 = Tuple.create(first, second);
        final Tuple<String, String> instance2 = Tuple.create(second, first);

        final int expResult = -1;
        final int result = instance1.compareTo(instance2);
        assertEquals(result, expResult);
    }

    /**
     * Test of compareTo method, of class Tuple.
     */
    @Test
    public void testCompareToWhereBothAreEqual() {
        final String first = "apple";
        final String second = "apple";
        final Tuple<String, String> instance1 = Tuple.create(first, second);
        final Tuple<String, String> instance2 = Tuple.create(second, first);

        final int expResult = 0;
        final int result = instance1.compareTo(instance2);
        assertEquals(result, expResult);
    }

    /**
     * Test of compareTo method, of class Tuple.
     */
    @Test
    public void testCompareToWhereFirstIsGreaterThanSecond() {
        final String first = "apple";
        final String second = "banana";
        final Tuple<String, String> instance1 = Tuple.create(first, second);
        final Tuple<String, String> instance2 = Tuple.create(second, first);

        final int expResult = 1;
        final int result = instance2.compareTo(instance1);
        assertEquals(result, expResult);
    }
}
