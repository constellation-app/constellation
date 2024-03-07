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
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
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
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class IntegerParameterTypeNGTest extends ConstellationTest {

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
     * Test of build method, of class IntegerParameterType. One Parameter version
     */
    @Test
    public void testBuildOneParameter() {
        System.out.println("buildOneParameter");

        final String id = "integerParameter";
        final PluginParameter<IntegerParameterValue> result = IntegerParameterType.build(id);

        assertEquals(result.getId(), id);
        assertTrue(result.getType() instanceof IntegerParameterType);
        assertEquals(((IntegerParameterValue) result.getParameterValue()).get(), 0);
    }

    /**
     * Test of build method, of class IntegerParameterType. Two Parameter Version
     */
    @Test
    public void testBuildTwoParameters() {
        System.out.println("buildTwoParameters");

        final IntegerParameterValue intValue = new IntegerParameterValue(1);
        final String id = "integerParameter";
        final PluginParameter<IntegerParameterValue> result = IntegerParameterType.build(id, intValue);

        assertEquals(result.getId(), id);
        assertTrue(result.getType() instanceof IntegerParameterType);
        assertEquals(((IntegerParameterValue) result.getParameterValue()).get(), 1);
    }

    /**
     * Test of setShrinkInputWidth method, of class IntegerParameterType.
     */
    @Test
    public void testSetShrinkInputWidth() {
        System.out.println("setShrinkInputWidth");

        String id = "Shrink";
        PluginParameter<IntegerParameterValue> parameter = IntegerParameterType.build(id);

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
        PluginParameter<IntegerParameterValue> parameter = IntegerParameterType.build(id);

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
        PluginParameter<IntegerParameterValue> parameter = IntegerParameterType.build(id);

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
        PluginParameter<IntegerParameterValue> parameter = IntegerParameterType.build(id);

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

        final String id = "validate string";

        final IntegerParameterType instance = new IntegerParameterType();
        final PluginParameter<IntegerParameterValue> parameter = IntegerParameterType.build(id);
        
        assertEquals(instance.validateString(parameter, ""), "Value required");
        assertEquals(instance.validateString(parameter, "This is a Test"), "Not a valid integer");
        assertNull(instance.validateString(parameter, "1"));
               
        // set min and max
        IntegerParameterType.setMinimum(parameter, 0);
        IntegerParameterType.setMaximum(parameter, 2);

        assertEquals(instance.validateString(parameter, "-1"), "Value too small");
        assertEquals(instance.validateString(parameter, "3"), "Value too large");
        assertNull(instance.validateString(parameter, "2"));
    }
    
    /**
     * Test of set method, of class IntegerParameterValue.
     */
    @Test
    public void testSet() {
        System.out.println("set");

        final IntegerParameterValue intValue = new IntegerParameterValue();
        assertEquals(intValue.get(), 0);        
        // no min or max set
        assertTrue(intValue.set(1));
        assertEquals(intValue.get(), 1);
        
        // set min and max
        intValue.setMinimumValue(0);
        intValue.setMaximumValue(2);
        
        // fails as lower than min
        assertFalse(intValue.set(-1));
        assertEquals(intValue.get(), 1);
        // fails as higher than max
        assertFalse(intValue.set(3));
        assertEquals(intValue.get(), 1);
        // is in correct range
        assertTrue(intValue.set(2));
        assertEquals(intValue.get(), 2);
        // no change as new value is the same
        assertFalse(intValue.set(2));
        assertEquals(intValue.get(), 2);       
    }
    
    /**
     * Test of setStringValue method, of class IntegerParameterValue.
     */
    @Test
    public void testSetStringValue() {
        System.out.println("setStringValue");

        final IntegerParameterValue intValue = new IntegerParameterValue();
        assertEquals(intValue.get(), 0);
        
        assertTrue(intValue.setStringValue("1"));
        assertEquals(intValue.get(), 1);
        
        assertFalse(intValue.setStringValue(""));
        assertEquals(intValue.get(), 1);  
    }
    
    /**
     * Test of setObjectValue method, of class IntegerParameterValue.
     */
    @Test
    public void testSetObjectValue() {
        System.out.println("setObjectValue");

        final IntegerParameterValue intValue = new IntegerParameterValue();
        assertEquals(intValue.getObjectValue(), 0);
        
        assertTrue(intValue.setObjectValue(1));
        assertEquals(intValue.getObjectValue(), 1);
        
        assertTrue(intValue.setObjectValue(null));
        assertEquals(intValue.getObjectValue(), 0);  
    }
    
    /**
     * Test of setObjectValue method, of class IntegerParameterValue. Invalid Integer
     */
    @Test(expectedExceptions = IllegalArgumentException.class, 
            expectedExceptionsMessageRegExp = "Unexpected class class java.lang.String")
    public void testSetObjectValueInvalidFloat() {
        System.out.println("setObjectValueInvalidInteger");

        final IntegerParameterValue floatValue = new IntegerParameterValue();
        floatValue.setObjectValue("Not an integer");  
    }
    
    /**
     * Test of createCopy method, of class IntegerParameterValue.
     */
    @Test
    public void testCreateCopy() {
        System.out.println("createCopy");

        final IntegerParameterValue intValue = new IntegerParameterValue();
        intValue.setMinimumValue(1);
        intValue.setMaximumValue(5);
        intValue.setStepValue(2);
        final IntegerParameterValue intValueCopy = intValue.createCopy();
        assertTrue(intValueCopy.equals(intValue));

        // Ensure deep copy and not shallow
        assertTrue(intValue.set(2));
        assertFalse(intValueCopy.equals(intValue));  
    }
    
    /**
     * Test of equals method, of class IntegerParameterValue.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        
        final IntegerParameterValue intValue = new IntegerParameterValue();
        final IntegerParameterValue comp1 = new IntegerParameterValue(1);
        final IntegerParameterValue comp2 = new IntegerParameterValue(0);
        
        assertFalse(intValue.equals(null));
        assertFalse(intValue.equals(true));
        assertFalse(intValue.equals(comp1));
        assertTrue(intValue.equals(comp2));
    }
    
    /**
     * Test of toString method, of class IntegerParameterValue.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        
        final IntegerParameterValue intValue = new IntegerParameterValue();
        assertEquals(intValue.toString(), "0");
        intValue.set(1);
        assertEquals(intValue.toString(), "1");
    }
    
    /**
     * Test of setNumberValue method, of class IntegerParameterValue.
     */
    @Test
    public void testSetNumberValue() {
        System.out.println("setNumberValue");
        
        final IntegerParameterValue intValue = new IntegerParameterValue();
        assertEquals(intValue.getNumberValue(), 0);
        intValue.setNumberValue(1);
        assertEquals(intValue.getNumberValue(), 1);
        intValue.setNumberValue(1.2);
        assertEquals(intValue.getNumberValue(), 1);
    }
}
