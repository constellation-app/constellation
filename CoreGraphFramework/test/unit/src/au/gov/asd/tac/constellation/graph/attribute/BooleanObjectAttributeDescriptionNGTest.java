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
public class BooleanObjectAttributeDescriptionNGTest {

    BooleanObjectAttributeDescription instance;

    public BooleanObjectAttributeDescriptionNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        instance = new BooleanObjectAttributeDescription();
        instance.setCapacity(1);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of convertFromObject method, of class
     * BooleanObjectAttributeDescription.
     */
    @Test
    public void testConvertFromObject() {
        Object object = null;
        Boolean expResult = null;
        Boolean result = instance.convertFromObject(object);
        assertEquals(result, expResult);
    }

    /**
     * Test of convertFromString method, of class
     * BooleanObjectAttributeDescription.
     */
    @Test
    public void testConvertFromString() {
        String string = "";
        Boolean expResult = null;
        Boolean result = instance.convertFromString(string);
        assertEquals(result, expResult);
    }

    /**
     * Test of getByte method, of class BooleanObjectAttributeDescription.
     */
    @Test
    public void testGetByte() {
        int id = 0;
        byte expResult = 0;
        byte result = instance.getByte(id);
        assertEquals(result, expResult);
    }

    /**
     * Test of setByte method, of class BooleanObjectAttributeDescription.
     */
    @Test
    public void testSetByte() {
        int id = 0;
        byte value = 0;
        instance.setByte(id, value);
    }

    /**
     * Test of getShort method, of class BooleanObjectAttributeDescription.
     */
    @Test
    public void testGetShort() {
        int id = 0;
        short expResult = 0;
        short result = instance.getShort(id);
        assertEquals(result, expResult);
    }

    /**
     * Test of setShort method, of class BooleanObjectAttributeDescription.
     */
    @Test
    public void testSetShort() {
        int id = 0;
        short value = 0;
        instance.setShort(id, value);
    }

    /**
     * Test of getInt method, of class BooleanObjectAttributeDescription.
     */
    @Test
    public void testGetInt() {
        int id = 0;
        int expResult = 0;
        int result = instance.getInt(id);
        assertEquals(result, expResult);
    }

    /**
     * Test of setInt method, of class BooleanObjectAttributeDescription.
     */
    @Test
    public void testSetInt() {
        int id = 0;
        int value = 0;
        instance.setInt(id, value);
    }

    /**
     * Test of getLong method, of class BooleanObjectAttributeDescription.
     */
    @Test
    public void testGetLong() {
        int id = 0;
        long expResult = 0L;
        long result = instance.getLong(id);
        assertEquals(result, expResult);
    }

    /**
     * Test of setLong method, of class BooleanObjectAttributeDescription.
     */
    @Test
    public void testSetLong() {
        int id = 0;
        long value = 0L;
        instance.setLong(id, value);
    }

    /**
     * Test of getFloat method, of class BooleanObjectAttributeDescription.
     */
    @Test
    public void testGetFloat() {
        int id = 0;
        float expResult = 0.0F;
        float result = instance.getFloat(id);
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of setFloat method, of class BooleanObjectAttributeDescription.
     */
    @Test
    public void testSetFloat() {
        int id = 0;
        float value = 0.0F;
        instance.setFloat(id, value);
    }

    /**
     * Test of getDouble method, of class BooleanObjectAttributeDescription.
     */
    @Test
    public void testGetDouble() {
        int id = 0;
        double expResult = 0.0;
        double result = instance.getDouble(id);
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of setDouble method, of class BooleanObjectAttributeDescription.
     */
    @Test
    public void testSetDouble() {
        int id = 0;
        double value = 0.0;
        instance.setDouble(id, value);
    }

    /**
     * Test of getBoolean method, of class BooleanObjectAttributeDescription.
     */
    @Test
    public void testGetBoolean() {
        int id = 0;
        boolean expResult = false;
        boolean result = instance.getBoolean(id);
        assertEquals(result, expResult);
    }

    /**
     * Test of setBoolean method, of class BooleanObjectAttributeDescription.
     */
    @Test
    public void testSetBoolean() {
        int id = 0;
        boolean value = false;
        instance.setBoolean(id, value);
    }

    /**
     * Test of getChar method, of class BooleanObjectAttributeDescription.
     */
    @Test
    public void testGetChar() {
        int id = 0;
        char expResult = 0;
        char result = instance.getChar(id);
        assertEquals(result, expResult);
    }

    /**
     * Test of setChar method, of class BooleanObjectAttributeDescription.
     */
    @Test
    public void testSetChar() {
        int id = 0;
        char value = ' ';
        instance.setChar(id, value);
    }

//    /**
//     * Test of hashCode method, of class BooleanObjectAttributeDescription.
//     */
//    @Test
//    public void testHashCode() {
//        int id = 0;
//        int expResult = 0;
//        int result = instance.hashCode(id);
//        assertEquals(result, expResult);
//    }
//    /**
//     * Test of supportsIndexType method, of class BooleanObjectAttributeDescription.
//     */
//    @Test
//    public void testSupportsIndexType() {
//        GraphIndexType indexType = null;
//        boolean expResult = false;
//        boolean result = instance.supportsIndexType(indexType);
//        assertEquals(result, expResult);
//    }
//
//    /**
//     * Test of createIndex method, of class BooleanObjectAttributeDescription.
//     */
//    @Test
//    public void testCreateIndex() {
//        GraphIndexType indexType = null;
//        GraphIndex expResult = null;
//        GraphIndex result = instance.createIndex(indexType);
//        assertEquals(result, expResult);
//    }
}
