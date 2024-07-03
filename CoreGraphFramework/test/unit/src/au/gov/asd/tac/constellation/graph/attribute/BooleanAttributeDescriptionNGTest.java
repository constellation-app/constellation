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
package au.gov.asd.tac.constellation.graph.attribute;

import au.gov.asd.tac.constellation.graph.GraphIndexType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.NativeAttributeType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.locking.ParameterReadAccess;
import au.gov.asd.tac.constellation.graph.locking.ParameterWriteAccess;
import au.gov.asd.tac.constellation.graph.utilities.MultiValueStore;
import java.time.LocalDate;
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
        System.out.println("getName");
        
        String expResult = "boolean";
        String result = instance.getName();
        assertEquals(result, expResult);
    }

    /**
     * Test of getNativeClass method, of class BooleanAttributeDescription.
     */
    @Test
    public void testGetNativeClass() {
        System.out.println("getNativeClass");
        
        Class expResult = boolean.class;
        Class result = instance.getNativeClass();
        assertEquals(result, expResult);
    }

    /**
     * Test of getNativeType method, of class BooleanAttributeDescription.
     */
    @Test
    public void testGetNativeType() {
        System.out.println("getNativeType");
        
        NativeAttributeType expResult = NativeAttributeType.BOOLEAN;
        NativeAttributeType result = instance.getNativeType();
        assertEquals(result, expResult);
    }

    /**
     * Test of getDefault method, of class BooleanAttributeDescription.
     */
    @Test
    public void testGetDefault() {
        System.out.println("getDefault");
        
        Object expResult = false;
        Object result = instance.getDefault();
        assertEquals(result, expResult);
    }

    /**
     * Test of setDefault method, of class BooleanAttributeDescription.
     */
    @Test
    public void testSetDefault() {
        System.out.println("setDefault");
        
        Object value = null;
        instance.setDefault(value);
        assertEquals(instance.getDefault(), BooleanAttributeDescription.DEFAULT_VALUE);
    }

    /**
     * Test of getCapacity method, of class BooleanAttributeDescription.
     */
    @Test
    public void testGetCapacity() {
        System.out.println("getCapacity");
        
        int expResult = 1;
        int result = instance.getCapacity();
        assertEquals(result, expResult);
    }

    /**
     * Test of setCapacity method, of class BooleanAttributeDescription.
     */
    @Test
    public void testSetCapacity() {
        System.out.println("setCapacity");
        
        int capacity = 0;
        instance.setCapacity(capacity);
        assertEquals(instance.getCapacity(), capacity);
    }

    /**
     * Test of getByte method, of class BooleanAttributeDescription.
     */
    @Test
    public void testGetByte() {
        System.out.println("getByte");
        
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
        System.out.println("setByte");
        
        int id = 0;
        instance.setByte(id, (byte) 0);
        assertEquals(instance.getByte(id), (byte) 0);
        instance.setByte(id, (byte) 1);
        assertEquals(instance.getByte(id), (byte) 1);
    }

    /**
     * Test of getShort method, of class BooleanAttributeDescription.
     */
    @Test
    public void testGetShort() {
        System.out.println("getShort");
        
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
        System.out.println("setShort");
        
        int id = 0;
        instance.setShort(id, (short) 0);
        assertEquals(instance.getShort(id), (short) 0);
        instance.setShort(id, (short) 1);
        assertEquals(instance.getShort(id), (short) 1);
    }

    /**
     * Test of getInt method, of class BooleanAttributeDescription.
     */
    @Test
    public void testGetInt() {
        System.out.println("getInt");
        
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
        System.out.println("setInt");
        
        int id = 0;
        instance.setInt(id, 0);
        assertEquals(instance.getInt(id), 0);
        instance.setInt(id, 1);
        assertEquals(instance.getInt(id), 1);
    }

    /**
     * Test of getLong method, of class BooleanAttributeDescription.
     */
    @Test
    public void testGetLong() {
        System.out.println("getLong");
        
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
        System.out.println("setLong");
        
        int id = 0;
        instance.setLong(id, 0L);
        assertEquals(instance.getLong(id), 0L);
        instance.setLong(id, 1L);
        assertEquals(instance.getLong(id), 1L);
    }

    /**
     * Test of getFloat method, of class BooleanAttributeDescription.
     */
    @Test
    public void testGetFloat() {
        System.out.println("getFloat");
        
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
        System.out.println("setFloat");
        
        int id = 0;
        instance.setFloat(id, 0.0F);
        assertEquals(instance.getFloat(id), 0.0F);
        instance.setFloat(id, 1.0F);
        assertEquals(instance.getFloat(id), 1.0F);
    }

    /**
     * Test of getDouble method, of class BooleanAttributeDescription.
     */
    @Test
    public void testGetDouble() {
        System.out.println("getDouble");
        
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
        System.out.println("setDouble");
        
        int id = 0;
        instance.setDouble(id, 0.0);
        assertEquals(instance.getDouble(id), 0.0);
        instance.setDouble(id, 1.0);
        assertEquals(instance.getDouble(id), 1.0);;
    }

    /**
     * Test of getBoolean method, of class BooleanAttributeDescription.
     */
    @Test
    public void testGetBoolean() {
        System.out.println("getBoolean");
        
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
        System.out.println("setBoolean");
        
        int id = 0;
        boolean value = true;
        instance.setBoolean(id, value);
        assertEquals(instance.getBoolean(id), value);
    }

    /**
     * Test of getChar method, of class BooleanAttributeDescription.
     */
    @Test
    public void testGetChar() {
        System.out.println("getChar");
        
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
        System.out.println("setChar");
        
        int id = 0;
        instance.setChar(id, (char) 0);
        assertEquals(instance.getChar(id), (char) 0);
        instance.setChar(id, (char) 1);
        assertEquals(instance.getChar(id), (char) 1);;
    }

    /**
     * Test of getString method, of class BooleanAttributeDescription.
     */
    @Test
    public void testGetString() {
        System.out.println("getString");
        
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
        System.out.println("setString");
        
        int id = 0;
        String value = "true";
        instance.setString(id, value);
        assertEquals(instance.getString(id), value);
    }

    /**
     * Test of acceptsString method, of class BooleanAttributeDescription.
     */
    @Test
    public void testAcceptsString() {
        System.out.println("acceptsString");
        
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
        System.out.println("getObject");
        
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
        System.out.println("setObject");
        
        int id = 0;
        instance.setObject(id, null);
        assertEquals(instance.getObject(id), BooleanAttributeDescription.DEFAULT_VALUE);
        instance.setObject(id, "");
        assertEquals(instance.getObject(id), BooleanAttributeDescription.DEFAULT_VALUE);
        instance.setObject(id, 1);
        assertTrue((boolean) instance.getObject(id));
        instance.setObject(id, Boolean.FALSE);
        assertFalse((boolean) instance.getObject(id));
        instance.setObject(id, (char) 1);
        assertTrue((boolean) instance.getObject(id));
        instance.setObject(id, "false");
        assertFalse((boolean) instance.getObject(id));
    }
    
    /**
     * Test of setObject method, of class BooleanAttributeDescription. Trying to convert an incompatible type to boolean
     */
    @Test(expectedExceptions = IllegalArgumentException.class, 
            expectedExceptionsMessageRegExp = "Error converting Object \'class java.time.LocalDate\' to boolean")
    public void testSetObjectCantConvert() {
        System.out.println("setObjectCantConvert");
        
        int id = 0;
        instance.setObject(id, LocalDate.of(1999, 12, 31));
    }

    /**
     * Test of isClear method, of class BooleanAttributeDescription.
     */
    @Test
    public void testIsClear() {
        System.out.println("isClear");
        
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
        System.out.println("clear");
        int id = 0;
        instance.clear(id);
        assertTrue(instance.isClear(id));
    }

    /**
     * Test of copy method, of class BooleanAttributeDescription.
     */
    @Test
    public void testCopy() {
        System.out.println("copy");
        
        GraphReadMethods graph = new StoreGraph();
        AttributeDescription expResult = instance;
        AttributeDescription result = instance.copy(graph);
        assertEquals(result.getBoolean(0), expResult.getBoolean(0));
    }

    /**
     * Test of equals method, of class BooleanAttributeDescription.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        
        instance.setCapacity(3);
        instance.setBoolean(2, true);
        assertTrue(instance.equals(0, 1));
        assertFalse(instance.equals(0, 2));
    }

    /**
     * Test of save method, of class BooleanAttributeDescription.
     */
    @Test
    public void testSave() {
        System.out.println("save");
        
        int id = 0;
        ParameterWriteAccess access = new MultiValueStore();
        instance.save(id, access);
    }

    /**
     * Test of restore method, of class BooleanAttributeDescription.
     */
    @Test
    public void testRestore() {
        System.out.println("restore");
        
        int id = 0;
        ParameterReadAccess access = new MultiValueStore();
        instance.restore(id, access);
    }

    /**
     * Test of saveData method, of class BooleanAttributeDescription.
     */
    @Test
    public void testSaveData() {
        System.out.println("saveData");
        
        Object expResult = new boolean[1];
        Object result = instance.saveData();
        assertEquals(result, expResult);
    }

    /**
     * Test of restoreData method, of class BooleanAttributeDescription.
     */
    @Test
    public void testRestoreData() {
        System.out.println("restoreData");
        
        Object savedData = new boolean[1];
        instance.restoreData(savedData);
    }

    /**
     * Test of supportsIndexType method, of class BooleanAttributeDescription.
     */
    @Test
    public void testSupportsIndexType() {
        System.out.println("supportsIndexType");
        
        assertTrue(instance.supportsIndexType(GraphIndexType.UNORDERED));
        assertFalse(instance.supportsIndexType(GraphIndexType.ORDERED));
    }
}
