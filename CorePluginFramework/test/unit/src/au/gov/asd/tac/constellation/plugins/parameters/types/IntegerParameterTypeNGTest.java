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
import static au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.INSTANCE;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
import static au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.SHRINK_VAL;
import static org.testng.Assert.assertEquals;
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
public class IntegerParameterTypeNGTest {

    public IntegerParameterTypeNGTest() {
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
     * Test of build method, of class IntegerParameterType.
     */
    @Test
    public void testBuild_String() {
        System.out.println("build");

        String id = "integerParameter";
        PluginParameter result = IntegerParameterType.build(id);
        IntegerParameterValue expResult = new IntegerParameterValue();

        assertEquals(result.getId(), id);
        assertTrue(result.getType() instanceof IntegerParameterType);
        assertEquals(result.getParameterValue(), expResult);
    }

    /**
     * Test of build method, of class IntegerParameterType.
     */
    @Test
    public void testBuild_String_IntegerParameterTypeIntegerParameterValue() {
        System.out.println("build");

        IntegerParameterValue instance = new IntegerParameterValue(2);
        int instanceValue = instance.get();
        int expResult = 2;
        assertEquals(instanceValue, expResult);

        instance = new IntegerParameterValue(-5);
        instanceValue = instance.get();
        expResult = -5;
        assertEquals(instanceValue, expResult);

        instance = new IntegerParameterValue();
        instanceValue = instance.get();
        expResult = 0;
        assertEquals(instanceValue, expResult);

    }

    /**
     * Test of setShrinkInputWidth method, of class IntegerParameterType.
     */
    @Test
    public void testSetShrinkInputWidth() {
        System.out.println("setShrinkInputWidth");

        String id = "Shrink";
        PluginParameter<IntegerParameterType.IntegerParameterValue> parameter = new PluginParameter<>(new IntegerParameterValue(), INSTANCE, id);

        // Check setting shrink to false
        boolean expected = false;
        IntegerParameterType.setShrinkInputWidth(parameter, false);
        assertEquals(parameter.getProperty(SHRINK_VAL), expected);

        // Check setting shrink to the same value that it already is
        expected = false;
        IntegerParameterType.setShrinkInputWidth(parameter, expected);
        assertEquals(parameter.getProperty(SHRINK_VAL), expected);

        // Check setting shrink to true
        expected = true;
        IntegerParameterType.setShrinkInputWidth(parameter, expected);
        assertEquals(parameter.getProperty(SHRINK_VAL), expected);
    }

    /**
     * Test of setMinimum method, of class IntegerParameterType.
     */
    @Test
    public void testSetMinimum() {
        System.out.println("setMinimum");

        String id = "Minimum";
        PluginParameter<IntegerParameterType.IntegerParameterValue> parameter = new PluginParameter<>(new IntegerParameterValue(), INSTANCE, id);

        int min = 0;
        IntegerParameterType.setMinimum(parameter, min);
        assertEquals(parameter.getParameterValue().getMinimumValue(), min);

        min = -5;
        IntegerParameterType.setMinimum(parameter, min);
        assertEquals(parameter.getParameterValue().getMinimumValue(), min);

        min = 64;
        IntegerParameterType.setMinimum(parameter, min);
        assertEquals(parameter.getParameterValue().getMinimumValue(), min);
    }

    /**
     * Test of setMaximum method, of class IntegerParameterType.
     */
    @Test
    public void testSetMaximum() {
        System.out.println("setMaximum");

        String id = "Maxiumum";
        PluginParameter<IntegerParameterType.IntegerParameterValue> parameter = new PluginParameter<>(new IntegerParameterValue(), INSTANCE, id);

        int max = 0;
        IntegerParameterType.setMaximum(parameter, max);
        assertEquals(parameter.getParameterValue().getMaximumValue(), max);

        max = -5;
        IntegerParameterType.setMaximum(parameter, max);
        assertEquals(parameter.getParameterValue().getMaximumValue(), max);

        max = 64;
        IntegerParameterType.setMaximum(parameter, max);
        assertEquals(parameter.getParameterValue().getMaximumValue(), max);
    }

    /**
     * Test of setStep method, of class IntegerParameterType.
     */
    @Test
    public void testSetStep() {
        System.out.println("setStep");

        // Step shouldnt be allowed to be 0
        // unsure if it could be negative and go backwards?
        // step shouldn't be able to be greater than maximum value
        String id = "Step";
        PluginParameter<IntegerParameterType.IntegerParameterValue> parameter = new PluginParameter<>(new IntegerParameterValue(), INSTANCE, id);

        // step shouldn't be allowed to be 0
        int step = 0;
        IntegerParameterType.setStep(parameter, step);
        assertEquals(parameter.getParameterValue().getStepValue(), step);

        // step should be allowed to be positive
        step = 6;
        IntegerParameterType.setStep(parameter, step);
        assertEquals(parameter.getParameterValue().getStepValue(), step);

        // step shouldn't be allowed to be negative
        step = -4;
        IntegerParameterType.setStep(parameter, step);
        assertEquals(parameter.getParameterValue().getStepValue(), step);
    }

    /**
     * Test of validateString method, of class IntegerParameterType.
     */
    @Test
    public void testValidateString() {
        System.out.println("validateString");

        String id = "validate string";
        IntegerParameterType instance = new IntegerParameterType();
        PluginParameter<IntegerParameterType.IntegerParameterValue> parameter = new PluginParameter<>(new IntegerParameterValue(), INSTANCE, id);
        IntegerParameterType.setMaximum(parameter, 5);
        IntegerParameterType.setMinimum(parameter, 0);

        // test valid integer in min-max range
        // should return null
        String passingValue = "2";
        String result = instance.validateString(parameter, passingValue);
        assertEquals(result, null);

        // test invalid integer under min range
        // should return value too small
        passingValue = "-1";
        result = instance.validateString(parameter, passingValue);
        assertEquals(result, "Value too small");

        // test invalid integer over max range
        // should return value too small
        passingValue = "6";
        result = instance.validateString(parameter, passingValue);
        assertEquals(result, "Value too large");

        // test invalid integer
        // should return Not a valid integer
        passingValue = "This is a Test";
        result = instance.validateString(parameter, passingValue);
        assertEquals(result, "Not a valid integer");

        // test a 0
        // should return null
        passingValue = "0";
        result = instance.validateString(parameter, passingValue);
        assertEquals(result, null);
    }

}
