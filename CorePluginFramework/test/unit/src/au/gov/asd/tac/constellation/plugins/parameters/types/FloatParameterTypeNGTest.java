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
import au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType.FloatParameterValue;
import static au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.SHRINK_VAL;
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
     * Test of build method, of class FloatParameterType. One Parameter Version
     */
    @Test
    public void testBuildOneParameter() {
        System.out.println("buildOneParameter");

        final String id = "floatParameter";
        final PluginParameter<FloatParameterValue> result = FloatParameterType.build(id);

        assertEquals(result.getId(), id);
        assertTrue(result.getType() instanceof FloatParameterType);
        assertEquals(((FloatParameterValue) result.getParameterValue()).get(), 0F);
    }

    /**
     * Test of build method, of class FloatParameterType. Two Parameter version
     */
    @Test
    public void testBuildTwoParameters() {
        System.out.println("buildTwoParameters");

        final FloatParameterValue floatValue = new FloatParameterValue(1.2F);
        final String id = "floatParameter";
        final PluginParameter<FloatParameterValue> result = FloatParameterType.build(id, floatValue);

        assertEquals(result.getId(), id);
        assertTrue(result.getType() instanceof FloatParameterType);
        assertEquals(((FloatParameterValue) result.getParameterValue()).get(), 1.2F);
    }

    /**
     * Test of setShrinkInputWidth method, of class FloatParameterType.
     */
    @Test
    public void testSetShrinkInputWidth() {
        System.out.println("setShrinkInputWidth");

        String id = "Shrink";
        PluginParameter<FloatParameterValue> parameter = FloatParameterType.build(id);

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
        PluginParameter<FloatParameterValue> parameter = FloatParameterType.build(id);

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
        PluginParameter<FloatParameterValue> parameter = FloatParameterType.build(id);

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
        PluginParameter<FloatParameterValue> parameter = FloatParameterType.build(id);

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

        final String id = "validate string";

        final FloatParameterType instance = new FloatParameterType();
        final PluginParameter<FloatParameterValue> parameter = FloatParameterType.build(id);
        
        assertEquals(instance.validateString(parameter, ""), "Value required");
        assertEquals(instance.validateString(parameter, "This is a Test"), "Not a valid float");
        assertNull(instance.validateString(parameter, "1"));
               
        // set min and max
        FloatParameterType.setMinimum(parameter, 0F);
        FloatParameterType.setMaximum(parameter, 2F);

        assertEquals(instance.validateString(parameter, "-1"), "Value too small");
        assertEquals(instance.validateString(parameter, "3"), "Value too large");
        assertNull(instance.validateString(parameter, "1.2"));
    }
    
    /**
     * Test of set method, of class FloatParameterValue.
     */
    @Test
    public void testSet() {
        System.out.println("set");

        final FloatParameterValue floatValue = new FloatParameterValue();
        assertEquals(floatValue.get(), 0F);        
        // no min or max set
        assertTrue(floatValue.set(1.2F));
        assertEquals(floatValue.get(), 1.2F);
        
        // set min and max
        floatValue.setMinimumValue(1F);
        floatValue.setMaximumValue(2F);
        
        // fails as lower than min
        assertFalse(floatValue.set(0.5F));
        assertEquals(floatValue.get(), 1.2F);
        // fails as higher than max
        assertFalse(floatValue.set(2.5F));
        assertEquals(floatValue.get(), 1.2F);
        // is in correct range
        assertTrue(floatValue.set(1.5F));
        assertEquals(floatValue.get(), 1.5F);
        // no change as new value is the same
        assertFalse(floatValue.set(1.5F));
        assertEquals(floatValue.get(), 1.5F);       
    }
    
    /**
     * Test of setStringValue method, of class FloatParameterValue.
     */
    @Test
    public void testSetStringValue() {
        System.out.println("setStringValue");

        final FloatParameterValue floatValue = new FloatParameterValue();
        assertEquals(floatValue.get(), 0F);
        
        assertTrue(floatValue.setStringValue("1.2"));
        assertEquals(floatValue.get(), 1.2F);
        
        assertFalse(floatValue.setStringValue(""));
        assertEquals(floatValue.get(), 1.2F);  
    }
    
    /**
     * Test of setObjectValue method, of class FloatParameterValue.
     */
    @Test
    public void testSetObjectValue() {
        System.out.println("setObjectValue");

        final FloatParameterValue floatValue = new FloatParameterValue();
        assertEquals(floatValue.getObjectValue(), 0F);
        
        assertTrue(floatValue.setObjectValue(1.2F));
        assertEquals(floatValue.getObjectValue(), 1.2F);
        
        assertTrue(floatValue.setObjectValue(null));
        assertEquals(floatValue.getObjectValue(), 0F);  
    }
    
    /**
     * Test of setObjectValue method, of class FloatParameterValue. Invalid Float
     */
    @Test(expectedExceptions = IllegalArgumentException.class, 
            expectedExceptionsMessageRegExp = "Unexpected object value class java.lang.String")
    public void testSetObjectValueInvalidFloat() {
        System.out.println("setObjectValueInvalidFloat");

        final FloatParameterValue floatValue = new FloatParameterValue();
        floatValue.setObjectValue("Not a float");  
    }
    
    /**
     * Test of createCopy method, of class FloatParameterValue.
     */
    @Test
    public void testCreateCopy() {
        System.out.println("createCopy");

        final FloatParameterValue floatValue = new FloatParameterValue();
        floatValue.setMinimumValue(1.0F);
        floatValue.setMaximumValue(5.0F);
        floatValue.setStepValue(0.2F);
        final FloatParameterValue floatValueCopy = floatValue.createCopy();
        assertTrue(floatValueCopy.equals(floatValue));

        // Ensure deep copy and not shallow
        assertTrue(floatValue.set(2.0F));
        assertFalse(floatValueCopy.equals(floatValue));  
    }
    
    /**
     * Test of equals method, of class FloatParameterValue.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        
        final FloatParameterValue floatValue = new FloatParameterValue();
        final FloatParameterValue comp1 = new FloatParameterValue(1.2F);
        final FloatParameterValue comp2 = new FloatParameterValue(0F);
        
        assertFalse(floatValue.equals(null));
        assertFalse(floatValue.equals(true));
        assertFalse(floatValue.equals(comp1));
        assertTrue(floatValue.equals(comp2));
    }
    
    /**
     * Test of toString method, of class FloatParameterValue.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        
        final FloatParameterValue floatValue = new FloatParameterValue();
        assertEquals(floatValue.toString(), "0.0");
        floatValue.set(1.2F);
        assertEquals(floatValue.toString(), "1.2");
    }
    
    /**
     * Test of setNumberValue method, of class FloatParameterValue.
     */
    @Test
    public void testSetNumberValue() {
        System.out.println("setNumberValue");
        
        final FloatParameterValue floatValue = new FloatParameterValue();
        assertEquals(floatValue.getNumberValue(), 0F);
        floatValue.setNumberValue(1);
        assertEquals(floatValue.getNumberValue(), 1F);
        floatValue.setNumberValue(1.2);
        assertEquals(floatValue.getNumberValue(), 1.2F);
    }
}
