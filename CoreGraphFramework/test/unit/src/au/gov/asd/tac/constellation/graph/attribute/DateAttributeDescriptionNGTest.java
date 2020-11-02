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
package au.gov.asd.tac.constellation.graph.attribute;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.NativeAttributeType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author arcturus
 */
public class DateAttributeDescriptionNGTest {

    DateAttributeDescription instance;

    public DateAttributeDescriptionNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        instance = new DateAttributeDescription();
        instance.setCapacity(1);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of convertFromString method, of class DateAttributeDescription.
     */
    @Test
    public void testConvertFromString() {
        String string = "";
        long expResult = 0L;
        long result = instance.convertFromString(string);
        assertEquals(result, expResult);
    }

    /**
     * Test of getName method, of class DateAttributeDescription.
     */
    @Test
    public void testGetName() {
        String expResult = "date";
        String result = instance.getName();
        assertEquals(result, expResult);
    }

    /**
     * Test of getVersion method, of class DateAttributeDescription.
     */
    @Test
    public void testGetVersion() {
        int expResult = 1;
        int result = instance.getVersion();
        assertEquals(result, expResult);
    }

    /**
     * Test of getNativeClass method, of class DateAttributeDescription.
     */
    @Test
    public void testGetNativeClass() {
        Class expResult = Long.class;
        Class result = instance.getNativeClass();
        assertEquals(result, expResult);
    }

    /**
     * Test of getNativeType method, of class DateAttributeDescription.
     */
    @Test
    public void testGetNativeType() {
        NativeAttributeType expResult = NativeAttributeType.LONG;
        NativeAttributeType result = instance.getNativeType();
        assertEquals(result, expResult);
    }

    /**
     * Test of getDefault method, of class DateAttributeDescription.
     */
    @Test
    public void testGetDefault() {
        Object expResult = (long) 0;
        Object result = instance.getDefault();
        assertEquals(result, expResult);
    }

    /**
     * Test of setDefault method, of class DateAttributeDescription.
     */
    @Test
    public void testSetDefault() {
        Object value = null;
        instance.setDefault(value);
        assertEquals(instance.getDefault(), DateAttributeDescription.DEFAULT_VALUE);
    }

    /**
     * Test of getCapacity method, of class DateAttributeDescription.
     */
    @Test
    public void testGetCapacity() {
        int expResult = 1;
        int result = instance.getCapacity();
        assertEquals(result, expResult);
    }

    /**
     * Test of setCapacity method, of class DateAttributeDescription.
     */
    @Test
    public void testSetCapacity() {
        int capacity = 0;
        instance.setCapacity(capacity);
        assertEquals(instance.getCapacity(), capacity);
    }

    /**
     * Test of getLong method, of class DateAttributeDescription.
     */
    @Test
    public void testGetLong() {
        int id = 0;
        long expResult = 0L;
        long result = instance.getLong(id);
        assertEquals(result, expResult);
    }

    /**
     * Test of setLong method, of class DateAttributeDescription.
     */
    @Test
    public void testSetLong() {
        int id = 0;
        long value = 0L;
        instance.setLong(id, value);
        assertEquals(instance.getLong(id), value);
    }

    /**
     * Test of getString method, of class DateAttributeDescription.
     */
    @Test
    public void testGetString() {
        int id = 0;
        String expResult = null;
        String result = instance.getString(id);
        assertEquals(result, expResult);
    }

    /**
     * Test of setString method, of class DateAttributeDescription.
     */
    @Test
    public void testSetString() {
        int id = 0;
        String value = "";
        instance.setString(id, value);
        assertEquals(instance.getString(id), null);
    }

    /**
     * Test of acceptsString method, of class DateAttributeDescription.
     */
    @Test
    public void testAcceptsString() {
        String value = "";
        String expResult = null;
        String result = instance.acceptsString(value);
        assertEquals(result, expResult);
    }

    /**
     * Test of getObject method, of class DateAttributeDescription.
     */
    @Test
    public void testGetObject() {
        int id = 0;
        Object expResult = null;
        Object result = instance.getObject(id);
        assertEquals(result, expResult);
    }

    /**
     * Test of setObject method, of class DateAttributeDescription.
     */
    @Test
    public void testSetObject() {
        int id = 0;
        Object value = null;
        instance.setObject(id, value);
        assertEquals(instance.getObject(id), value);
    }

    /**
     * Test of convertToNativeValue method, of class DateAttributeDescription.
     */
    @Test
    public void testConvertToNativeValue() {
        Object object = null;
        Object expResult = (long) 0;
        Object result = instance.convertToNativeValue(object);
        assertEquals(result, expResult);
    }

    /**
     * Test of isClear method, of class DateAttributeDescription.
     */
    @Test
    public void testIsClear() {
        int id = 0;
        boolean expResult = true;
        boolean result = instance.isClear(id);
        assertEquals(result, expResult);
    }

    /**
     * Test of clear method, of class DateAttributeDescription.
     */
    @Test
    public void testClear() {
        int id = 0;
        instance.clear(id);
        assertTrue(instance.isClear(id));
    }

    /**
     * Test of copy method, of class DateAttributeDescription.
     */
    @Test
    public void testCopy() {
        GraphReadMethods graph = new StoreGraph();
        AttributeDescription expResult = instance;
        AttributeDescription result = instance.copy(graph);
        assertEquals(result.getObject(0), expResult.getObject(0));
    }

    /**
     * Test of hashCode method, of class DateAttributeDescription.
     */
    @Test
    public void testHashCode() {
        int id = 0;
        int expResult = 0;
        int result = instance.hashCode(id);
        assertEquals(result, expResult);
    }

    /**
     * Test of equals method, of class DateAttributeDescription.
     */
    @Test
    public void testEquals() {
        int id1 = 0;
        int id2 = 0;
        boolean expResult = true;
        boolean result = instance.equals(id1, id2);
        assertEquals(result, expResult);
    }

    /**
     * Test of saveData method, of class DateAttributeDescription.
     */
    @Test
    public void testSaveData() {
        Object expResult = new long[1];
        Object result = instance.saveData();
        assertEquals(result, expResult);
    }

    /**
     * Test of restoreData method, of class DateAttributeDescription.
     */
    @Test
    public void testRestoreData() {
        Object savedData = new long[1];
        instance.restoreData(savedData);
    }

}
