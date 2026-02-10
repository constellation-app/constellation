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
package au.gov.asd.tac.constellation.graph.attribute;

import au.gov.asd.tac.constellation.graph.GraphIndexType;
import java.time.LocalDate;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
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
public class BooleanObjectAttributeDescriptionNGTest {

    BooleanObjectAttributeDescription instance;
    
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
        instance = new BooleanObjectAttributeDescription();
        instance.setCapacity(1);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of convertFromObject method, of class
     * BooleanObjectAttributeDescription.
     */
    @Test
    public void testConvertFromObject() {
        System.out.println("convertFromObject");
        
        assertNull(instance.convertFromObject(null));
        assertTrue(instance.convertFromObject(1));
        assertFalse(instance.convertFromObject((char) 0));
    }
    
    /**
     * Test of convertFromObject method, of class 
     * BooleanObjectAttributeDescription. Trying to convert an incompatible type to boolean object
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testconvertFromObjectCantConvert() {
        System.out.println("convertFromObjectObjectCantConvert");
        
        instance.convertFromObject(LocalDate.of(1999, 12, 31));
    }

    /**
     * Test of convertFromString method, of class
     * BooleanObjectAttributeDescription.
     */
    @Test
    public void testConvertFromString() {
        System.out.println("convertFromString");
        
        assertNull(instance.convertFromString(""));
        assertTrue(instance.convertFromString("true"));
    }

    /**
     * Test of getByte method, of class BooleanObjectAttributeDescription.
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
     * Test of setByte method, of class BooleanObjectAttributeDescription.
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
     * Test of getShort method, of class BooleanObjectAttributeDescription.
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
     * Test of setShort method, of class BooleanObjectAttributeDescription.
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
     * Test of getInt method, of class BooleanObjectAttributeDescription.
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
     * Test of setInt method, of class BooleanObjectAttributeDescription.
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
     * Test of getLong method, of class BooleanObjectAttributeDescription.
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
     * Test of setLong method, of class BooleanObjectAttributeDescription.
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
     * Test of getFloat method, of class BooleanObjectAttributeDescription.
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
     * Test of setFloat method, of class BooleanObjectAttributeDescription.
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
     * Test of getDouble method, of class BooleanObjectAttributeDescription.
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
     * Test of setDouble method, of class BooleanObjectAttributeDescription.
     */
    @Test
    public void testSetDouble() {
        System.out.println("setDouble");
        
        int id = 0;
        instance.setDouble(id, 0.0);
        assertEquals(instance.getDouble(id), 0.0);
        instance.setDouble(id, 1.0);
        assertEquals(instance.getDouble(id), 1.0);
    }

    /**
     * Test of getBoolean method, of class BooleanObjectAttributeDescription.
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
     * Test of setBoolean method, of class BooleanObjectAttributeDescription.
     */
    @Test
    public void testSetBoolean() {
        System.out.println("setBoolean");
        
        int id = 0;
        instance.setBoolean(id, true);
        assertTrue(instance.getBoolean(id));
        instance.setBoolean(id, false);
        assertFalse(instance.getBoolean(id));
    }

    /**
     * Test of getChar method, of class BooleanObjectAttributeDescription.
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
     * Test of setChar method, of class BooleanObjectAttributeDescription.
     */
    @Test
    public void testSetChar() {
        System.out.println("setChar");
        
        int id = 0;
        instance.setChar(id, (char) 0);
        assertEquals(instance.getChar(id), (char) 0);
        instance.setChar(id, (char) 1);
        assertEquals(instance.getChar(id), (char) 1);
    }

    /**
     * Test of supportsIndexType method, of class BooleanObjectAttributeDescription.
     */
    @Test
    public void testSupportsIndexType() {
        System.out.println("supportsIndexType");
        
        assertTrue(instance.supportsIndexType(GraphIndexType.UNORDERED));
        assertFalse(instance.supportsIndexType(GraphIndexType.ORDERED));
    }
}
