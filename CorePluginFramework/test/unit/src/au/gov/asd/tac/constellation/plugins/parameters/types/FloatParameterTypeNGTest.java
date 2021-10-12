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
import static au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType.INSTANCE;
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
public class FloatParameterTypeNGTest {

    public FloatParameterTypeNGTest() {
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
     * Test of build method, of class FloatParameterType.
     */
    @Test
    public void testBuild_String() {
        System.out.println("build");

        String id = "floatParameter";
        PluginParameter result = FloatParameterType.build(id);
        FloatParameterType.FloatParameterValue expResult = new FloatParameterType.FloatParameterValue();

        assertEquals(result.getId(), id);
        assertTrue(result.getType() instanceof FloatParameterType);
        assertEquals(result.getParameterValue(), expResult);
    }

    /**
     * Test of build method, of class FloatParameterType.
     */
    @Test
    public void testBuild_String_FloatParameterTypeFloatParameterValue() {
        System.out.println("build");

        FloatParameterType.FloatParameterValue instance = new FloatParameterType.FloatParameterValue(2);
        float instanceValue = instance.get();
        float expResult = 2f;
        assertEquals(instanceValue, expResult);

        instance = new FloatParameterType.FloatParameterValue(-5);
        instanceValue = instance.get();
        expResult = -5f;
        assertEquals(instanceValue, expResult);

        instance = new FloatParameterType.FloatParameterValue();
        instanceValue = instance.get();
        expResult = 0f;
        assertEquals(instanceValue, expResult);

    }

    /**
     * Test of setShrinkInputWidth method, of class FloatParameterType.
     */
    @Test
    public void testSetShrinkInputWidth() {
        System.out.println("setShrinkInputWidth");

        String id = "Shrink";
        PluginParameter<FloatParameterType.FloatParameterValue> parameter = new PluginParameter<>(new FloatParameterType.FloatParameterValue(), INSTANCE, id);

        // Check setting shrink to false
        boolean expected = false;
        FloatParameterType.setShrinkInputWidth(parameter, false);
        assertEquals(parameter.getProperty(SHRINK_VAL), expected);

        // Check setting shrink to the same value that it already is
        expected = false;
        FloatParameterType.setShrinkInputWidth(parameter, false);
        assertEquals(parameter.getProperty(SHRINK_VAL), expected);

        // Check setting shrink to true
        expected = true;
        FloatParameterType.setShrinkInputWidth(parameter, true);
        assertEquals(parameter.getProperty(SHRINK_VAL), expected);
    }

    /**
     * Test of setMinimum method, of class FloatParameterType.
     */
    @Test
    public void testSetMinimum() {
        System.out.println("setMinimum");

        String id = "Minimum";
        PluginParameter<FloatParameterType.FloatParameterValue> parameter = new PluginParameter<>(new FloatParameterType.FloatParameterValue(), INSTANCE, id);

        float min = 0f;
        FloatParameterType.setMinimum(parameter, min);
        assertEquals(parameter.getParameterValue().getMinimumValue(), min);

        min = -5f;
        FloatParameterType.setMinimum(parameter, min);
        assertEquals(parameter.getParameterValue().getMinimumValue(), min);

        min = 64f;
        FloatParameterType.setMinimum(parameter, min);
        assertEquals(parameter.getParameterValue().getMinimumValue(), min);
    }

    /**
     * Test of setMaximum method, of class FloatParameterType.
     */
    @Test
    public void testSetMaximum() {
        System.out.println("setMaximum");

        String id = "Maxiumum";
        PluginParameter<FloatParameterType.FloatParameterValue> parameter = new PluginParameter<>(new FloatParameterType.FloatParameterValue(), INSTANCE, id);

        float max = 0f;
        FloatParameterType.setMaximum(parameter, max);
        assertEquals(parameter.getParameterValue().getMaximumValue(), max);

        max = -5f;
        FloatParameterType.setMaximum(parameter, max);
        assertEquals(parameter.getParameterValue().getMaximumValue(), max);

        max = 64f;
        FloatParameterType.setMaximum(parameter, max);
        assertEquals(parameter.getParameterValue().getMaximumValue(), max);
    }

    /**
     * Test of setStep method, of class FloatParameterType.
     */
    @Test
    public void testSetStep() {
        System.out.println("setStep");

        // Step shouldnt be allowed to be 0
        // unsure if it could be negative and go backwards?
        // step shouldn't be able to be greater than maximum value
        String id = "Step";
        PluginParameter<FloatParameterType.FloatParameterValue> parameter = new PluginParameter<>(new FloatParameterType.FloatParameterValue(), INSTANCE, id);

        // step shouldn't be allowed to be 0
        float step = 0f;
        FloatParameterType.setStep(parameter, step);
        assertEquals(parameter.getParameterValue().getStepValue(), step);

        // step should be allowed to be positive
        step = 6f;
        FloatParameterType.setStep(parameter, step);
        assertEquals(parameter.getParameterValue().getStepValue(), step);

        // step shouldn't be allowed to be negative
        step = -4f;
        FloatParameterType.setStep(parameter, step);
        assertEquals(parameter.getParameterValue().getStepValue(), step);
    }

    /**
     * Test of validateString method, of class FloatParameterType.
     */
    @Test
    public void testValidateString() {
        System.out.println("validateString");

        String id = "validate string";

        FloatParameterType instance = new FloatParameterType();
        PluginParameter<FloatParameterType.FloatParameterValue> parameter = new PluginParameter<>(new FloatParameterType.FloatParameterValue(), INSTANCE, id);
        FloatParameterType.setMaximum(parameter, 5f);
        FloatParameterType.setMinimum(parameter, 0f);

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
        assertEquals(result, "Not a valid float");

        // test a 0
        // should return null
        passingValue = "0";
        result = instance.validateString(parameter, passingValue);
        assertEquals(result, null);
    }

}
