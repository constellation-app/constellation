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
import au.gov.asd.tac.constellation.graph.locking.ParameterReadAccess;
import au.gov.asd.tac.constellation.graph.locking.ParameterWriteAccess;
import au.gov.asd.tac.constellation.graph.utilities.MultiValueStore;
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
public class BooleanAttributeDescriptionNGTest {

    BooleanAttributeDescription instance;

    public BooleanAttributeDescriptionNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        instance = new BooleanAttributeDescription();
        instance.setCapacity(1);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of getName method, of class BooleanAttributeDescription.
     */
    @Test
    public void testGetName() {
        String expResult = "boolean";
        String result = instance.getName();
        assertEquals(result, expResult);
    }

    /**
     * Test of getNativeClass method, of class BooleanAttributeDescription.
     */
    @Test
    public void testGetNativeClass() {
        Class expResult = boolean.class;
        Class result = instance.getNativeClass();
        assertEquals(result, expResult);
    }

    /**
     * Test of getNativeType method, of class BooleanAttributeDescription.
     */
    @Test
    public void testGetNativeType() {
        NativeAttributeType expResult = NativeAttributeType.BOOLEAN;
        NativeAttributeType result = instance.getNativeType();
        assertEquals(result, expResult);
    }

    /**
     * Test of getDefault method, of class BooleanAttributeDescription.
     */
    @Test
    public void testGetDefault() {
        Object expResult = false;
        Object result = instance.getDefault();
        assertEquals(result, expResult);
    }

    /**
     * Test of setDefault method, of class BooleanAttributeDescription.
     */
    @Test
    public void testSetDefault() {
        Object value = null;
        instance.setDefault(value);
    }

    /**
     * Test of getCapacity method, of class BooleanAttributeDescription.
     */
    @Test
    public void testGetCapacity() {
        int expResult = 1;
        int result = instance.getCapacity();
        assertEquals(result, expResult);
    }

    /**
     * Test of setCapacity method, of class BooleanAttributeDescription.
     */
    @Test
    public void testSetCapacity() {
        int capacity = 0;
        instance.setCapacity(capacity);
    }

    /**
     * Test of getByte method, of class BooleanAttributeDescription.
     */
    @Test
    public void testGetByte() {
        int id = 0;
        byte expResult = 0;
        byte result = instance.getByte(id);
        assertEquals(result, expResult);
    }

    /**
     * Test of setByte method, of class BooleanAttributeDescription.
     */
    @Test
    public void testSetByte() {
        int id = 0;
        byte value = 0;
        instance.setByte(id, value);
    }

    /**
     * Test of getShort method, of class BooleanAttributeDescription.
     */
    @Test
    public void testGetShort() {
        int id = 0;
        short expResult = 0;
        short result = instance.getShort(id);
        assertEquals(result, expResult);
    }

    /**
     * Test of setShort method, of class BooleanAttributeDescription.
     */
    @Test
    public void testSetShort() {
        int id = 0;
        short value = 0;
        instance.setShort(id, value);
    }

    /**
     * Test of getInt method, of class BooleanAttributeDescription.
     */
    @Test
    public void testGetInt() {
        int id = 0;
        int expResult = 0;
        int result = instance.getInt(id);
        assertEquals(result, expResult);
    }

    /**
     * Test of setInt method, of class BooleanAttributeDescription.
     */
    @Test
    public void testSetInt() {
        int id = 0;
        int value = 0;
        instance.setInt(id, value);
    }

    /**
     * Test of getLong method, of class BooleanAttributeDescription.
     */
    @Test
    public void testGetLong() {
        int id = 0;
        long expResult = 0L;
        long result = instance.getLong(id);
        assertEquals(result, expResult);
    }

    /**
     * Test of setLong method, of class BooleanAttributeDescription.
     */
    @Test
    public void testSetLong() {
        int id = 0;
        long value = 0L;
        instance.setLong(id, value);
    }

    /**
     * Test of getFloat method, of class BooleanAttributeDescription.
     */
    @Test
    public void testGetFloat() {
        int id = 0;
        float expResult = 0.0F;
        float result = instance.getFloat(id);
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of setFloat method, of class BooleanAttributeDescription.
     */
    @Test
    public void testSetFloat() {
        int id = 0;
        float value = 0.0F;
        instance.setFloat(id, value);
    }

    /**
     * Test of getDouble method, of class BooleanAttributeDescription.
     */
    @Test
    public void testGetDouble() {
        int id = 0;
        double expResult = 0.0;
        double result = instance.getDouble(id);
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of setDouble method, of class BooleanAttributeDescription.
     */
    @Test
    public void testSetDouble() {
        int id = 0;
        double value = 0.0;
        instance.setDouble(id, value);
    }

    /**
     * Test of getBoolean method, of class BooleanAttributeDescription.
     */
    @Test
    public void testGetBoolean() {
        int id = 0;
        boolean expResult = false;
        boolean result = instance.getBoolean(id);
        assertEquals(result, expResult);
    }

    /**
     * Test of setBoolean method, of class BooleanAttributeDescription.
     */
    @Test
    public void testSetBoolean() {
        int id = 0;
        boolean value = false;
        instance.setBoolean(id, value);
    }

    /**
     * Test of getChar method, of class BooleanAttributeDescription.
     */
    @Test
    public void testGetChar() {
        int id = 0;
        char expResult = 0;
        char result = instance.getChar(id);
        assertEquals(result, expResult);
    }

    /**
     * Test of setChar method, of class BooleanAttributeDescription.
     */
    @Test
    public void testSetChar() {
        int id = 0;
        char value = ' ';
        instance.setChar(id, value);
    }

    /**
     * Test of getString method, of class BooleanAttributeDescription.
     */
    @Test
    public void testGetString() {
        int id = 0;
        String expResult = "false";
        String result = instance.getString(id);
        assertEquals(result, expResult);
    }

    /**
     * Test of setString method, of class BooleanAttributeDescription.
     */
    @Test
    public void testSetString() {
        int id = 0;
        String value = "";
        instance.setString(id, value);
    }

    /**
     * Test of acceptsString method, of class BooleanAttributeDescription.
     */
    @Test
    public void testAcceptsString() {
        String value = "";
        String expResult = null;
        String result = instance.acceptsString(value);
        assertEquals(result, expResult);
    }

    /**
     * Test of getObject method, of class BooleanAttributeDescription.
     */
    @Test
    public void testGetObject() {
        int id = 0;
        Object expResult = false;
        Object result = instance.getObject(id);
        assertEquals(result, expResult);
    }

    /**
     * Test of setObject method, of class BooleanAttributeDescription.
     */
    @Test
    public void testSetObject() {
        int id = 0;
        Object value = null;
        instance.setObject(id, value);
    }

    /**
     * Test of isClear method, of class BooleanAttributeDescription.
     */
    @Test
    public void testIsClear() {
        int id = 0;
        boolean expResult = true;
        boolean result = instance.isClear(id);
        assertEquals(result, expResult);
    }

    /**
     * Test of clear method, of class BooleanAttributeDescription.
     */
    @Test
    public void testClear() {
        int id = 0;
        instance.clear(id);
    }

    /**
     * Test of copy method, of class BooleanAttributeDescription.
     */
    @Test
    public void testCopy() {
        GraphReadMethods graph = new StoreGraph();
        AttributeDescription expResult = instance;
        AttributeDescription result = instance.copy(graph);
        assertEquals(result.getBoolean(0), expResult.getBoolean(0));
    }

//    /**
//     * Test of hashCode method, of class BooleanAttributeDescription.
//     */
//    @Test
//    public void testHashCode() {
//        int id = 0;
//        int expResult = 0;
//        int result = instance.hashCode(id);
//        assertEquals(result, expResult);
//    }
    /**
     * Test of equals method, of class BooleanAttributeDescription.
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
     * Test of save method, of class BooleanAttributeDescription.
     */
    @Test
    public void testSave() {
        int id = 0;
        ParameterWriteAccess access = new MultiValueStore();
        instance.save(id, access);
    }

    /**
     * Test of restore method, of class BooleanAttributeDescription.
     */
    @Test
    public void testRestore() {
        int id = 0;
        ParameterReadAccess access = new MultiValueStore();
        instance.restore(id, access);
    }

    /**
     * Test of saveData method, of class BooleanAttributeDescription.
     */
    @Test
    public void testSaveData() {
        Object expResult = new boolean[1];
        Object result = instance.saveData();
        assertEquals(result, expResult);
    }

    /**
     * Test of restoreData method, of class BooleanAttributeDescription.
     */
    @Test
    public void testRestoreData() {
        Object savedData = new boolean[1];
        instance.restoreData(savedData);
    }

//    /**
//     * Test of supportsIndexType method, of class BooleanAttributeDescription.
//     */
//    @Test
//    public void testSupportsIndexType() {
//        GraphIndexType indexType = null;
//        boolean expResult = false;
//        boolean result = instance.supportsIndexType(indexType);
//        assertEquals(result, expResult);
//    }
//    /**
//     * Test of createIndex method, of class BooleanAttributeDescription.
//     */
//    @Test
//    public void testCreateIndex() {
//        GraphIndexType indexType = null;
//        GraphIndex expResult =  new BooleanAttributeDescription.Index();
//        GraphIndex result = instance.createIndex(indexType);
//        assertEquals(result, expResult);
//    }
}
