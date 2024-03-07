/*
 * Copyright 2010-2023 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author aldebaran30701
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class BooleanParameterTypeNGTest extends ConstellationTest {

    public BooleanParameterTypeNGTest() {
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
     * Test of build method, of class BooleanParameterType.
     */
    @Test
    public void testBuild_String() {
        System.out.println("build_string");
        String id = "booleanParameter";

        PluginParameter result = BooleanParameterType.build(id);
        BooleanParameterValue expectedValue = new BooleanParameterValue();

        assertEquals(result.getId(), id);
        assertTrue(result.getType() instanceof BooleanParameterType);
        assertEquals(result.getParameterValue(), expectedValue);
    }

    /**
     * Test of build method, of class BooleanParameterType.
     */
    @Test
    public void testBuild_String_BooleanParameterTypeBooleanParameterValue() {
        System.out.println("build_string_parametertype");
        String id = "booleanParameter";
        BooleanParameterValue parameterValue = new BooleanParameterValue();

        PluginParameter result = BooleanParameterType.build(id, parameterValue);

        assertEquals(result.getId(), id);
        assertTrue(result.getType() instanceof BooleanParameterType);
        assertEquals(result.getParameterValue(), parameterValue);
    }

    // Start of testing BooleanParameterValue
    @Test
    public void testConstruction() {
        System.out.println("construction");
        BooleanParameterValue result = new BooleanParameterValue();
        assertNotNull(result);
        assertFalse(result.get());
    }

    @Test
    public void testGetSet() {
        System.out.println("getSet");
        BooleanParameterValue instance = new BooleanParameterValue();

        boolean expected = instance.get();
        assertEquals(instance.get(), expected);

        expected = true;
        instance.set(expected);
        assertEquals(instance.get(), expected);

        // Return true when value is different
        expected = false;
        assertTrue(instance.set(expected));
        assertEquals(instance.get(), expected);

        // Return false when value is set the same
        expected = false;
        assertFalse(instance.set(expected));
        assertEquals(instance.get(), expected);
    }

    /**
     * Always returns null because there will never be an error when validating
     * a boolean string. Mostly because it is not used for the boolean parameter
     * type.
     */
    @Test
    public void testValidateString() {
        System.out.println("validateString");
        BooleanParameterValue instance = new BooleanParameterValue();
        assertNull(instance.validateString("standard string"));

        assertNull(instance.validateString(""));

        assertNull(instance.validateString(null));

    }

    @Test
    public void testSetStringValue() {
        System.out.println("setStringValue");
        BooleanParameterValue instance = new BooleanParameterValue();

        // test empty string
        String newValue = "";
        assertFalse(instance.setStringValue(newValue));

        // test null string
        newValue = null;
        assertFalse(instance.setStringValue(newValue));

        // test invalid string
        newValue = "words";
        assertFalse(instance.setStringValue(newValue));

        // test valid string
        newValue = "true";
        assertTrue(instance.setStringValue(newValue));

        // test valid string
        newValue = "false";
        assertTrue(instance.setStringValue(newValue));

        // test valid string case sensitive
        newValue = "trUe";
        assertTrue(instance.setStringValue(newValue));

        // test valid string
        newValue = "false";
        assertTrue(instance.setStringValue(newValue));

        // test valid string when previously set as same value
        newValue = "false";
        assertFalse(instance.setStringValue(newValue));

    }

    @Test
    public void testGetSetObjectValue() {
        System.out.println("getSetObjectValue");
        BooleanParameterValue instance = new BooleanParameterValue();

        // get current value as object and verify that it is correct.
        Object expected = instance.getObjectValue();
        assertEquals(instance.getObjectValue(), expected);

        // change object value
        expected = Boolean.TRUE;
        instance.setObjectValue(expected);
        assertEquals(instance.getObjectValue(), expected);

        // Return true when value is different
        expected = Boolean.FALSE;
        assertTrue(instance.setObjectValue(expected));
        assertEquals(instance.getObjectValue(), expected);

        // Return false when value is set the same
        expected = Boolean.FALSE;
        assertFalse(instance.setObjectValue(expected));
        assertEquals(instance.getObjectValue(), expected);

        // Return false when value is set the same
        expected = false;
        assertFalse(instance.setObjectValue(null));
        assertEquals(instance.getObjectValue(), expected);

        // Return false when value is set the same
        expected = Boolean.FALSE;
        assertFalse(instance.setObjectValue(expected));
        assertEquals(instance.getObjectValue(), expected);

        // Return true when value is set from a boolean primitive correctly
        expected = true;
        assertTrue(instance.setObjectValue(expected));
    }

    /**
     * Test that parsing a wrong type throws an exception
     */
    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void testSetObjectValueException() {
        System.out.println("setObjectValueException");

        // Ensure initial setup is correct
        BooleanParameterValue instance = new BooleanParameterValue();
        instance.set(true);
        assertTrue(instance.get());

        assertFalse(instance.setObjectValue(1));
    }

    /**
     * Test of createCopy method, of class BooleanParameterValue.
     */
    @Test
    public void testCreateCopy() {
        System.out.println("createCopy");
        // Ensure initial setup is correct
        BooleanParameterValue instance = new BooleanParameterValue();
        instance.set(true);
        assertTrue(instance.get());

        // Copy and verify correct value in copy
        BooleanParameterValue instanceCopy = instance.createCopy();
        assertTrue(instanceCopy.get());

        // Ensure deep copy and not shallow
        assertTrue(instance.set(false));
        assertTrue(instanceCopy.get());
    }
    
    /**
     * Test of equals method, of class BooleanParameterValue.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        
        final BooleanParameterValue bool = new BooleanParameterValue();
        final BooleanParameterValue comp1 = new BooleanParameterValue(true);
        final BooleanParameterValue comp2 = new BooleanParameterValue(false);
        
        assertFalse(bool.equals(null));
        assertFalse(bool.equals(false));
        assertFalse(bool.equals(comp1));
        assertTrue(bool.equals(comp2));
    }
    
    /**
     * Test of toString method, of class BooleanParameterValue.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        
        final BooleanParameterValue bool1 = new BooleanParameterValue();
        final BooleanParameterValue bool2 = new BooleanParameterValue(true);
        final BooleanParameterValue bool3 = new BooleanParameterValue(false);
        
        assertEquals(bool1.toString(), "false");
        assertEquals(bool2.toString(), "true");
        assertEquals(bool3.toString(), "false");
    }
}
