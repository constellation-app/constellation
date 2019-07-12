/*
 * Copyright 2010-2019 Australian Signals Directorate
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

import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Float Object Attribute Description Test.
 *
 * @author arcturus
 */
public class FloatObjectAttributeDescriptionNGTest {

    public FloatObjectAttributeDescriptionNGTest() {
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

    @Test
    public void testFloatObjectSetToEmptyString() {
        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
        instance.setCapacity(1);
        instance.setObject(0, "");
        Object value = instance.getObject(0);
        assertEquals(value, null);
    }

//    /**
//     * Test of getName method, of class FloatObjectAttributeDescription.
//     */
//    @Test
//    public void testGetName() {
//        System.out.println("getName");
//        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
//        String expResult = "";
//        String result = instance.getName();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getCapacity method, of class FloatObjectAttributeDescription.
//     */
//    @Test
//    public void testGetCapacity() {
//        System.out.println("getCapacity");
//        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
//        int expResult = 0;
//        int result = instance.getCapacity();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setCapacity method, of class FloatObjectAttributeDescription.
//     */
//    @Test
//    public void testSetCapacity() {
//        System.out.println("setCapacity");
//        int capacity = 0;
//        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
//        instance.setCapacity(capacity);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of clear method, of class FloatObjectAttributeDescription.
//     */
//    @Test
//    public void testClear() {
//        System.out.println("clear");
//        int id = 0;
//        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
//        instance.clear(id);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setByte method, of class FloatObjectAttributeDescription.
//     */
//    @Test
//    public void testSetByte() {
//        System.out.println("setByte");
//        int id = 0;
//        byte value = 0;
//        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
//        instance.setByte(id, value);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setShort method, of class FloatObjectAttributeDescription.
//     */
//    @Test
//    public void testSetShort() {
//        System.out.println("setShort");
//        int id = 0;
//        short value = 0;
//        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
//        instance.setShort(id, value);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setInt method, of class FloatObjectAttributeDescription.
//     */
//    @Test
//    public void testSetInt() {
//        System.out.println("setInt");
//        int id = 0;
//        int value = 0;
//        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
//        instance.setInt(id, value);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setLong method, of class FloatObjectAttributeDescription.
//     */
//    @Test
//    public void testSetLong() {
//        System.out.println("setLong");
//        int id = 0;
//        long value = 0L;
//        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
//        instance.setLong(id, value);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setFloat method, of class FloatObjectAttributeDescription.
//     */
//    @Test
//    public void testSetFloat() {
//        System.out.println("setFloat");
//        int id = 0;
//        float value = 0.0F;
//        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
//        instance.setFloat(id, value);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setDouble method, of class FloatObjectAttributeDescription.
//     */
//    @Test
//    public void testSetDouble() {
//        System.out.println("setDouble");
//        int id = 0;
//        double value = 0.0;
//        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
//        instance.setDouble(id, value);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setBoolean method, of class FloatObjectAttributeDescription.
//     */
//    @Test
//    public void testSetBoolean() {
//        System.out.println("setBoolean");
//        int id = 0;
//        boolean value = false;
//        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
//        instance.setBoolean(id, value);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setChar method, of class FloatObjectAttributeDescription.
//     */
//    @Test
//    public void testSetChar() {
//        System.out.println("setChar");
//        int id = 0;
//        char value = ' ';
//        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
//        instance.setChar(id, value);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setObject method, of class FloatObjectAttributeDescription.
//     */
//    @Test
//    public void testSetObject() {
//        System.out.println("setObject");
//        int id = 0;
//        Object value = null;
//        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
//        instance.setObject(id, value);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setString method, of class FloatObjectAttributeDescription.
//     */
//    @Test
//    public void testSetString() {
//        System.out.println("setString");
//        int id = 0;
//        String value = "";
//        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
//        instance.setString(id, value);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of acceptsString method, of class FloatObjectAttributeDescription.
//     */
//    @Test
//    public void testAcceptsString() {
//        System.out.println("acceptsString");
//        String value = "";
//        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
//        String expResult = "";
//        String result = instance.acceptsString(value);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getByte method, of class FloatObjectAttributeDescription.
//     */
//    @Test
//    public void testGetByte() {
//        System.out.println("getByte");
//        int id = 0;
//        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
//        byte expResult = 0;
//        byte result = instance.getByte(id);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getShort method, of class FloatObjectAttributeDescription.
//     */
//    @Test
//    public void testGetShort() {
//        System.out.println("getShort");
//        int id = 0;
//        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
//        short expResult = 0;
//        short result = instance.getShort(id);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getInt method, of class FloatObjectAttributeDescription.
//     */
//    @Test
//    public void testGetInt() {
//        System.out.println("getInt");
//        int id = 0;
//        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
//        int expResult = 0;
//        int result = instance.getInt(id);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getLong method, of class FloatObjectAttributeDescription.
//     */
//    @Test
//    public void testGetLong() {
//        System.out.println("getLong");
//        int id = 0;
//        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
//        long expResult = 0L;
//        long result = instance.getLong(id);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getFloat method, of class FloatObjectAttributeDescription.
//     */
//    @Test
//    public void testGetFloat() {
//        System.out.println("getFloat");
//        int id = 0;
//        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
//        float expResult = 0.0F;
//        float result = instance.getFloat(id);
//        assertEquals(result, expResult, 0.0);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getDouble method, of class FloatObjectAttributeDescription.
//     */
//    @Test
//    public void testGetDouble() {
//        System.out.println("getDouble");
//        int id = 0;
//        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
//        double expResult = 0.0;
//        double result = instance.getDouble(id);
//        assertEquals(result, expResult, 0.0);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getBoolean method, of class FloatObjectAttributeDescription.
//     */
//    @Test
//    public void testGetBoolean() {
//        System.out.println("getBoolean");
//        int id = 0;
//        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
//        boolean expResult = false;
//        boolean result = instance.getBoolean(id);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getChar method, of class FloatObjectAttributeDescription.
//     */
//    @Test
//    public void testGetChar() {
//        System.out.println("getChar");
//        int id = 0;
//        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
//        char expResult = ' ';
//        char result = instance.getChar(id);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getObject method, of class FloatObjectAttributeDescription.
//     */
//    @Test
//    public void testGetObject() {
//        System.out.println("getObject");
//        int id = 0;
//        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
//        Object expResult = null;
//        Object result = instance.getObject(id);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getString method, of class FloatObjectAttributeDescription.
//     */
//    @Test
//    public void testGetString() {
//        System.out.println("getString");
//        int id = 0;
//        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
//        String expResult = "";
//        String result = instance.getString(id);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of copy method, of class FloatObjectAttributeDescription.
//     */
//    @Test
//    public void testCopy() {
//        System.out.println("copy");
//        GraphReadMethods graph = null;
//        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
//        AttributeDescription expResult = null;
//        AttributeDescription result = instance.copy(graph);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getNativeClass method, of class FloatObjectAttributeDescription.
//     */
//    @Test
//    public void testGetNativeClass() {
//        System.out.println("getNativeClass");
//        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
//        Class expResult = null;
//        Class result = instance.getNativeClass();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setDefault method, of class FloatObjectAttributeDescription.
//     */
//    @Test
//    public void testSetDefault() {
//        System.out.println("setDefault");
//        Object value = null;
//        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
//        instance.setDefault(value);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getDefault method, of class FloatObjectAttributeDescription.
//     */
//    @Test
//    public void testGetDefault() {
//        System.out.println("getDefault");
//        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
//        Object expResult = null;
//        Object result = instance.getDefault();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of hashCode method, of class FloatObjectAttributeDescription.
//     */
//    @Test
//    public void testHashCode() {
//        System.out.println("hashCode");
//        int id = 0;
//        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
//        int expResult = 0;
//        int result = instance.hashCode(id);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of equals method, of class FloatObjectAttributeDescription.
//     */
//    @Test
//    public void testEquals() {
//        System.out.println("equals");
//        int id1 = 0;
//        int id2 = 0;
//        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
//        boolean expResult = false;
//        boolean result = instance.equals(id1, id2);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of canBeImported method, of class FloatObjectAttributeDescription.
//     */
//    @Test
//    public void testCanBeImported() {
//        System.out.println("canBeImported");
//        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
//        boolean expResult = false;
//        boolean result = instance.canBeImported();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of ordering method, of class FloatObjectAttributeDescription.
//     */
//    @Test
//    public void testOrdering() {
//        System.out.println("ordering");
//        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
//        int expResult = 0;
//        int result = instance.ordering();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of save method, of class FloatObjectAttributeDescription.
//     */
//    @Test
//    public void testSave() {
//        System.out.println("save");
//        int id = 0;
//        ParameterWriteAccess access = null;
//        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
//        instance.save(id, access);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of restore method, of class FloatObjectAttributeDescription.
//     */
//    @Test
//    public void testRestore() {
//        System.out.println("restore");
//        int id = 0;
//        ParameterReadAccess access = null;
//        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
//        instance.restore(id, access);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isClear method, of class FloatObjectAttributeDescription.
//     */
//    @Test
//    public void testIsClear() {
//        System.out.println("isClear");
//        int id = 0;
//        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
//        boolean expResult = false;
//        boolean result = instance.isClear(id);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of saveData method, of class FloatObjectAttributeDescription.
//     */
//    @Test
//    public void testSaveData() {
//        System.out.println("saveData");
//        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
//        Object expResult = null;
//        Object result = instance.saveData();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of restoreData method, of class FloatObjectAttributeDescription.
//     */
//    @Test
//    public void testRestoreData() {
//        System.out.println("restoreData");
//        Object savedData = null;
//        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
//        instance.restoreData(savedData);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getNativeType method, of class FloatObjectAttributeDescription.
//     */
//    @Test
//    public void testGetNativeType() {
//        System.out.println("getNativeType");
//        FloatObjectAttributeDescription instance = new FloatObjectAttributeDescription();
//        NativeAttributeType expResult = null;
//        NativeAttributeType result = instance.getNativeType();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
