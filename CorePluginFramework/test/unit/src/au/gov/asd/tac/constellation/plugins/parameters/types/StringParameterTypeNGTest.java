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
 * @author Atlas139mkm
 */
public class StringParameterTypeNGTest {

    public StringParameterTypeNGTest() {
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
     * Test of build method, of class StringParameterType.
     */
    @Test
    public void testBuild_String() {
        System.out.println("build string");
        String id = "stringParameter";

        PluginParameter result = StringParameterType.build(id);
        StringParameterValue expResult = new StringParameterValue();
        System.out.println(expResult.toString());

        assertEquals(result.getId(), id);
        assertTrue(result.getType() instanceof StringParameterType);
        assertEquals(result.getParameterValue(), expResult);
    }

    /**
     * Test of build method, of class StringParameterType.
     */
    @Test
    public void testBuild_String_StringParameterValue() {
        System.out.println("build_string_parametertype");
        String id = "stringParameter";

        StringParameterValue parameterValue = new StringParameterValue();
        PluginParameter result = StringParameterType.build(id, parameterValue);

        assertEquals(result.getId(), id);
        assertTrue(result.getType() instanceof StringParameterType);
        assertEquals(result.getParameterValue(), parameterValue);
    }

    /**
     * Test of getLines method, of class StringParameterType.
     */
    @Test
    public void testGetLines() {
        System.out.println("getLines");

        PluginParameter instance = StringParameterType.build("stringParameter");

        // check the expected result and result equal when postive integers
        Integer expResult = 5;
        instance.setProperty(StringParameterType.LINES, expResult);
        Integer result = StringParameterType.getLines(instance);
        assertEquals(result, expResult);

        // check the expected result and result equal when 0
        expResult = 0;
        instance.setProperty(StringParameterType.LINES, expResult);
        result = StringParameterType.getLines(instance);
        assertEquals(result, expResult);

        // check the expected result and result equal when negative integers
        expResult = -1;
        instance.setProperty(StringParameterType.LINES, expResult);
        result = StringParameterType.getLines(instance);
        assertEquals(result, expResult);

        // check the expected result and result equal when null
        expResult = null;
        instance.setProperty(StringParameterType.LINES, expResult);
        result = StringParameterType.getLines(instance);
        assertEquals(result, expResult);
    }

    /**
     * Test of setLines method, of class StringParameterType.
     */
    @Test
    public void testSetLines() {
        System.out.println("setLines");

        PluginParameter instance = StringParameterType.build("stringParameter");

        // check the expected result and result equal when postive integers
        Integer expResult = 5;
        StringParameterType.setLines(instance, expResult);
        Integer result = StringParameterType.getLines(instance);
        assertEquals(instance.getProperty(StringParameterType.LINES), expResult);
        assertEquals(result, expResult);

        // check the expected result and result equal when 0
        expResult = 0;
        StringParameterType.setLines(instance, expResult);
        result = StringParameterType.getLines(instance);
        assertEquals(instance.getProperty(StringParameterType.LINES), expResult);
        assertEquals(result, expResult);

        // check the expected result and result equal when negative integers
        expResult = -1;
        StringParameterType.setLines(instance, expResult);
        result = StringParameterType.getLines(instance);
        assertEquals(instance.getProperty(StringParameterType.LINES), expResult);
        assertEquals(result, expResult);
    }

    /**
     * Test of isLabel method, of class StringParameterType.
     */
    @Test
    public void testIsLabel() {
        System.out.println("isLabel");

        PluginParameter instance = StringParameterType.build("stringParameter");
        assertFalse(StringParameterType.isLabel(instance));

        // check if when set to true it remains true
        instance.setProperty(StringParameterType.IS_LABEL, true);
        assertTrue(StringParameterType.isLabel(instance));

        // check if when set to false it remains false
        instance.setProperty(StringParameterType.IS_LABEL, false);
        assertFalse(StringParameterType.isLabel(instance));
    }

    /**
     * Test of setIsLabel method, of class StringParameterType.
     */
    @Test
    public void testSetIsLabel() {
        System.out.println("setIsLabel");

        PluginParameter instance = StringParameterType.build("stringParameter");

        // check if when set to true it remains true
        StringParameterType.setIsLabel(instance, true);
        assertTrue((boolean) instance.getProperty(StringParameterType.IS_LABEL));

        // check if when set to false it remains false
        StringParameterType.setIsLabel(instance, false);
        assertFalse((boolean) instance.getProperty(StringParameterType.IS_LABEL));
    }

    /**
     * Test of validateString method, of class StringParameterType.
     */
    @Test
    public void testValidateString() {
        System.out.println("validateString");

        PluginParameter<StringParameterValue> param = StringParameterType.build("stringParameter");
        StringParameterType instance = new StringParameterType();

        // Test should return null if the string is valid (not empty or null)
        // Test should return "Parameter is Empty!" if invalid (null or empty)
        String stringValue = "This is a test";
        assertNull(instance.validateString(param, stringValue));
        stringValue = null;
        assertNotNull(instance.validateString(param, stringValue));
        stringValue = "";
        assertNotNull(instance.validateString(param, stringValue));
        stringValue = "     ";
        assertNotNull(instance.validateString(param, stringValue));
        stringValue = "?!@#4[]=";
        assertNull(instance.validateString(param, stringValue));
    }
}
