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

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import static org.testng.Assert.assertEquals;
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
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class PasswordParameterTypeNGTest extends ConstellationTest {

    public PasswordParameterTypeNGTest() {
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
     * Test of build method, of class PasswordParameterType.
     */
    @Test
    public void testBuild_String() {

        System.out.println("build");
        String id = "password parameter";

        PluginParameter result = PasswordParameterType.build(id);
        PasswordParameterValue expResult = new PasswordParameterValue();
        System.out.println(expResult.toString());

        assertEquals(result.getId(), id);
        assertTrue(result.getType() instanceof PasswordParameterType);
        assertEquals(result.getParameterValue(), expResult);
    }

    /**
     * Test of build method, of class PasswordParameterType.
     */
    @Test
    public void testBuild_String_PasswordParameterValue() {

        System.out.println("build_string_parametertype");
        String id = "stringParameter";

        PasswordParameterValue parameterValue = new PasswordParameterValue();
        PluginParameter result = PasswordParameterType.build(id, parameterValue);

        assertEquals(result.getId(), id);
        assertTrue(result.getType() instanceof PasswordParameterType);
        assertEquals(result.getParameterValue(), parameterValue);
    }

    /**
     * Test of validateString method, of class PasswordParameterType.
     */
    @Test
    public void testValidateString() {
        System.out.println("validateString");

        PluginParameter<PasswordParameterValue> param = PasswordParameterType.build("stringParameter");
        PasswordParameterType instance = new PasswordParameterType();

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
