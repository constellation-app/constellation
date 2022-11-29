/*
 * Copyright 2010-2021 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.parameters.types;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Atlas139mkm
 */
public class StringParameterValueNGTest {

    public StringParameterValueNGTest() {
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
     * Test of get method, of class StringParameterValue.
     */
    @Test
    public void testGet() {
        System.out.println("get");

        // test if get retrieves a blank string
        StringParameterValue instance = new StringParameterValue("");
        String expResult = "";
        String result = instance.get();
        assertEquals(result, expResult);

        // test if get retrieves a non-blank string
        instance = new StringParameterValue("This is a Test");
        expResult = "This is a Test";
        result = instance.get();
        assertEquals(result, expResult);

        // test if get retrieves a null value
        instance = new StringParameterValue();
        result = instance.get();
        assertEquals(result, null);
    }

    /**
     * Test of set method, of class StringParameterValue.
     */
    @Test
    public void testSet() {
        System.out.println("set");

        // Checking a string value changing to another
        // should return true
        StringParameterValue instance = new StringParameterValue("Starting Value");
        String expResult = "New Value";
        assertTrue(instance.set(expResult));
        assertEquals(instance.get(), expResult);

        // Checking a string value changing to an empty string
        // should return true
        instance = new StringParameterValue("Starting Value");
        expResult = "";
        assertTrue(instance.set(expResult));
        assertEquals(instance.get(), expResult);

        // Checking a string value changing to null
        // should return true
        instance = new StringParameterValue("Starting Value");
        expResult = null;
        assertTrue(instance.set(expResult));
        assertEquals(instance.get(), expResult);

        // Checking a null value changing to another null
        // should return false
        instance = new StringParameterValue(null);
        expResult = null;
        assertFalse(instance.set(expResult));
        assertEquals(instance.get(), expResult);

        // Checking a string value changing to the same string value
        // should return false
        instance = new StringParameterValue("test");
        expResult = "test";
        assertFalse(instance.set(expResult));
        assertEquals(instance.get(), expResult);

        // Checking a null value changing to a string
        // should return true
        instance = new StringParameterValue(null);
        expResult = "New Value";
        assertTrue(instance.set(expResult));
        assertEquals(instance.get(), expResult);
    }

    /**
     * Test of validateString method, of class StringParameterValue.
     */
    @Test
    public void testValidateString() {
        System.out.println("validateString");

        // validateString always returns null
        StringParameterValue instance = new StringParameterValue("Starting Value");
        String expResult = null;
        instance.validateString(expResult);
        assertEquals(instance.validateString(expResult), expResult);

        instance = new StringParameterValue(null);
        expResult = null;
        instance.validateString(expResult);
        assertEquals(instance.validateString(expResult), expResult);

        instance = new StringParameterValue("");
        expResult = null;
        instance.validateString(expResult);
        assertEquals(instance.validateString(expResult), expResult);
    }

    /**
     * Test of setStringValue method, of class StringParameterValue.
     */
    @Test
    public void testSetStringValue() {
        System.out.println("setStringValue");

        // Checking a string value changing to another
        // should return true
        StringParameterValue instance = new StringParameterValue("Starting Value");
        String expResult = "New Value";
        assertTrue(instance.setStringValue(expResult));
        assertEquals(instance.get(), expResult);

        // Checking a string value changing to an empty string
        // should return true
        instance = new StringParameterValue("Starting Value");
        expResult = "";
        assertTrue(instance.setStringValue(expResult));
        assertEquals(instance.get(), expResult);

        // Checking a string value changing to null
        // should return true
        instance = new StringParameterValue("Starting Value");
        expResult = null;
        assertTrue(instance.setStringValue(expResult));
        assertEquals(instance.get(), expResult);

        // Checking a null value changing to another null
        // should return false
        instance = new StringParameterValue(null);
        expResult = null;
        assertFalse(instance.setStringValue(expResult));
        assertEquals(instance.get(), expResult);

        // Checking a string value changing to the same string value
        // should return false
        instance = new StringParameterValue("test");
        expResult = "test";
        assertFalse(instance.setStringValue(expResult));
        assertEquals(instance.get(), expResult);

        // Checking a null value changing to a string
        // should return true
        instance = new StringParameterValue(null);
        expResult = "New Value";
        assertTrue(instance.setStringValue(expResult));
        assertEquals(instance.get(), expResult);
    }

    /**
     * Test of getObjectValue method, of class StringParameterValue.
     */
    @Test
    public void testGetObjectValue() {
        System.out.println("getObjectValue");

        // test if get retrieves a blank string
        StringParameterValue instance = new StringParameterValue("");
        String expResult = "";
        Object result = instance.getObjectValue();
        assertEquals(result, expResult);

        // test if get retrieves a non-blank string
        instance = new StringParameterValue("This is a Test");
        expResult = "This is a Test";
        result = instance.getObjectValue();
        assertEquals(result, expResult);

        // test if get retrieves a null value
        instance = new StringParameterValue();
        result = instance.getObjectValue();
        assertEquals(result, null);

    }

    /**
     * Test of setObjectValue method, of class StringParameterValue.
     */
    @Test
    public void testSetObjectValue() {
        System.out.println("setObjectValue");

        StringParameterValue instance = new StringParameterValue();
        String expResult = "This is a Test";
        assertTrue(instance.setObjectValue(expResult));

        instance = new StringParameterValue();
        expResult = "";
        assertTrue(instance.setObjectValue(expResult));

        instance = new StringParameterValue();
        String expResultNull = null;
        assertFalse(instance.setObjectValue(expResultNull));

        instance = new StringParameterValue("This is a Test");
        expResult = "This is a Test";
        assertFalse(instance.setObjectValue(expResult));
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void testSetObjectValueEx() {
        System.out.println("setObjectValuEx");

        StringParameterValue instance = new StringParameterValue();
        Integer expIntResult = 5;
        assertTrue(instance.setObjectValue(expIntResult));
    }

    /**
     * Test of createCopy method, of class StringParameterValue.
     */
    @Test
    public void testCreateCopy() {
        System.out.println("createCopy");

        StringParameterValue instance = new StringParameterValue(null);

        // Instance and result should both equal with null values
        StringParameterValue result = instance.createCopy();
        assertEquals(result, instance);

        // Instance and result should both equal with "This is a test" values
        instance = new StringParameterValue("This is a test");
        result = instance.createCopy();
        assertEquals(result, instance);

        // Instance should now be different to result
        instance.set("Is it still a copy?");
        assertNotEquals(result, instance);

        // Result should now be equal to instance again
        result = instance.createCopy();
        assertEquals(result, instance);
    }

    /**
     * Test of equals method, of class StringParameterValue.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");

        // Test equals on two variables with the same values
        // Should return true
        StringParameterValue instance = new StringParameterValue("test");
        StringParameterValue instance2 = new StringParameterValue("test");
        assertTrue(instance.equals(instance2));

        // Test equals on two variables with the differnt values
        // Should return false
        instance = new StringParameterValue("Test");
        instance2 = new StringParameterValue();
        assertFalse(instance.equals(instance2));

        // Test equals on one StringParameterValue variable and one Object \
        // with the same value
        // Should return true
        instance = new StringParameterValue("Test");
        Object objectInstance = new StringParameterValue("Test");
        assertTrue(instance.equals(objectInstance));

        // Test equals on one StringParameterValue variable and one Object with
        // a different value
        // Should return false
        instance = new StringParameterValue();
        objectInstance = new StringParameterValue("Test");
        assertFalse(instance.equals(objectInstance));

        // Test equals on one StringParameterValue variable and one Integer
        // Should return false
        instance = new StringParameterValue("Test");
        Integer integerInstance = 4;
        assertFalse(instance.equals(integerInstance));

        // Test equals on two variables with null values
        // should return true
        instance = new StringParameterValue(null);
        instance2 = new StringParameterValue(null);
        assertTrue(instance.equals(instance2));
    }

    /**
     * Test of hashCode method, of class StringParameterValue.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");

        // Testing hashcode is 0 when object is null
        // should return 0
        StringParameterValue instance = new StringParameterValue();
        Integer nullObjectHash = 0;
        Integer instanceHash = instance.hashCode();
        assertEquals(instanceHash, nullObjectHash);

        // Testing hashcode isn't 0 when object isn't null
        // should return an intger not equal to 0
        instance = new StringParameterValue("test");
        instanceHash = instance.hashCode();
        assertNotEquals(instanceHash, 0);
    }

    /**
     * Test of toString method, of class StringParameterValue.
     */
    @Test
    public void testToString() {
        System.out.println("toString");

        // test if get retrieves a blank string
        StringParameterValue instance = new StringParameterValue("");
        String expResult = "";
        String result = instance.toString();
        assertEquals(result, expResult);

        // test if get retrieves a non-blank string
        instance = new StringParameterValue("This is a Test");
        expResult = "This is a Test";
        result = instance.toString();
        assertEquals(result, expResult);

        // test if get retrieves a null value
        instance = new StringParameterValue();
        result = instance.toString();
        assertEquals(result, null);
    }

}
