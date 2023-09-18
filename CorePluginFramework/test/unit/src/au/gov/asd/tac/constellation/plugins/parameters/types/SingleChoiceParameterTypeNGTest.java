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
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import java.util.Arrays;
import java.util.List;
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
 * @author antares
 */
public class SingleChoiceParameterTypeNGTest {

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
     * Test of build method, of class SingleChoiceParameterType. One Parameter version
     */
    @Test
    public void testBuildOneParameter() {
        System.out.println("buildOneParameter");
        
        final PluginParameter<SingleChoiceParameterValue> singleChoiceParam = SingleChoiceParameterType.build("My Choices");
        
        assertEquals(singleChoiceParam.getParameterValue().getInnerClass(), StringParameterValue.class);
        assertEquals(singleChoiceParam.getId(), "My Choices");
        assertEquals(singleChoiceParam.getType().getId(), "choice");
    }

    /**
     * Test of build method, of class SingleChoiceParameterType. Two Parameter version specifying inner class
     */
    @Test
    public void testBuildTwoParameterClass() {
        System.out.println("buildTwoParametersClass");
        
        final PluginParameter<SingleChoiceParameterValue> singleChoiceParam = SingleChoiceParameterType.build("My Choices", ElementTypeParameterValue.class);
        
        assertEquals(singleChoiceParam.getParameterValue().getInnerClass(), ElementTypeParameterValue.class);
        assertEquals(singleChoiceParam.getId(), "My Choices");
        assertEquals(singleChoiceParam.getType().getId(), "choice");
    }

    /**
     * Test of build method, of class SingleChoiceParameterType. Two Parameter version specifying parameter value
     */
    @Test
    public void testBuildTwoParametersParameterValue() {
        System.out.println("buildTwoParametersParameterValue");
        
        final SingleChoiceParameterValue singleChoiceValue = new SingleChoiceParameterValue(ElementTypeParameterValue.class);
        final SingleChoiceParameterValue singleChoiceValue2 = new SingleChoiceParameterValue(singleChoiceValue);
        final PluginParameter<SingleChoiceParameterValue> singleChoiceParam = SingleChoiceParameterType.build("My Choices", singleChoiceValue);
        final PluginParameter<SingleChoiceParameterValue> singleChoiceParam2 = SingleChoiceParameterType.build("Also My Choices", singleChoiceValue2);
        
        assertEquals(singleChoiceParam.getParameterValue().getInnerClass(), ElementTypeParameterValue.class);
        assertEquals(singleChoiceParam.getId(), "My Choices");
        assertEquals(singleChoiceParam.getType().getId(), "choice");
        assertEquals(singleChoiceParam2.getParameterValue().getInnerClass(), ElementTypeParameterValue.class);
        assertEquals(singleChoiceParam2.getId(), "Also My Choices");
        assertEquals(singleChoiceParam2.getType().getId(), "choice");
    }

    /**
     * Test of setOptions method, of class SingleChoiceParameterType.
     */
    @Test
    public void testSetOptions() {
        System.out.println("setOptions");
        
        final PluginParameter<SingleChoiceParameterValue> singleChoiceParameter = SingleChoiceParameterType.build("my choices");
        assertTrue(SingleChoiceParameterType.getOptions(singleChoiceParameter).isEmpty());
        
        final List<String> options = Arrays.asList("option1", "option2", "option3");
        SingleChoiceParameterType.setOptions(singleChoiceParameter, options);
        assertEquals(SingleChoiceParameterType.getOptions(singleChoiceParameter).size(), 3);
        // no change made
        SingleChoiceParameterType.setOptions(singleChoiceParameter, options);
        assertEquals(SingleChoiceParameterType.getOptions(singleChoiceParameter).size(), 3);
    }

    /**
     * Test of setOptionsData method, of class SingleChoiceParameterType.
     */
    @Test
    public void testSetOptionsData() {
        System.out.println("setOptionsData");
        
        final PluginParameter<SingleChoiceParameterValue> singleChoiceParameter = SingleChoiceParameterType.build("my choices");
        assertTrue(SingleChoiceParameterType.getOptionsData(singleChoiceParameter).isEmpty());
        
        final List<StringParameterValue> options = Arrays.asList(new StringParameterValue("option1"), 
                new StringParameterValue("option2"), new StringParameterValue("option3"));
        SingleChoiceParameterType.setOptionsData(singleChoiceParameter, options);
        assertEquals(SingleChoiceParameterType.getOptionsData(singleChoiceParameter).size(), 3);
    }

    /**
     * Test of setChoice method, of class SingleChoiceParameterType.
     */
    @Test
    public void testSetChoice() {
        System.out.println("setChoice");
        
        final PluginParameter<SingleChoiceParameterValue> singleChoiceParameter = SingleChoiceParameterType.build("my choices");
        //will fail as there are no options
        SingleChoiceParameterType.setChoice(singleChoiceParameter, "option2");
        assertNull(SingleChoiceParameterType.getChoice(singleChoiceParameter));
        
        final List<String> options = Arrays.asList("option1", "option2", "option3");
        SingleChoiceParameterType.setOptions(singleChoiceParameter, options);
        SingleChoiceParameterType.setChoice(singleChoiceParameter, "option2");
        assertEquals(SingleChoiceParameterType.getChoice(singleChoiceParameter), "option2");
    }

    /**
     * Test of setChoiceData method, of class SingleChoiceParameterType.
     */
    @Test
    public void testSetChoiceData() {
        System.out.println("setChoiceData");
        
        final PluginParameter<SingleChoiceParameterValue> singleChoiceParameter = SingleChoiceParameterType.build("my choices");
        final StringParameterValue value = new StringParameterValue("option2");
        //will fail as there are no options
        SingleChoiceParameterType.setChoiceData(singleChoiceParameter, value);
        assertNull(SingleChoiceParameterType.getChoiceData(singleChoiceParameter));
        
        final List<StringParameterValue> options = Arrays.asList(new StringParameterValue("option1"), 
                new StringParameterValue("option2"), new StringParameterValue("option3"));
        SingleChoiceParameterType.setOptionsData(singleChoiceParameter, options);
        SingleChoiceParameterType.setChoiceData(singleChoiceParameter, value);
        assertEquals(SingleChoiceParameterType.getChoiceData(singleChoiceParameter), value);
    }
    
    /**
     * Test of setEditable method, of class SingleChoiceParameterType.
     */
    @Test
    public void testSetEditable() {
        System.out.println("setEditable");
        
        final PluginParameter<SingleChoiceParameterValue> singleChoiceParameter = SingleChoiceParameterType.build("My Choices");
        assertFalse(SingleChoiceParameterType.isEditable(singleChoiceParameter));
        
        SingleChoiceParameterType.setEditable(singleChoiceParameter, true);
        assertTrue(SingleChoiceParameterType.isEditable(singleChoiceParameter));
        SingleChoiceParameterType.setEditable(singleChoiceParameter, false);
        assertFalse(SingleChoiceParameterType.isEditable(singleChoiceParameter));
    }
    
    /**
     * Test of validateString method, of class SingleChoiceParameterValue.
     */
    @Test
    public void testValidateString() {
        System.out.println("validateString");

        final SingleChoiceParameterValue singleChoiceValue = new SingleChoiceParameterValue();
        final SingleChoiceParameterValue singleChoiceValue2 = new SingleChoiceParameterValue(IntegerParameterValue.class);
        
        assertNull(singleChoiceValue.validateString("1"));
        assertNull(singleChoiceValue.validateString("Not an int"));

        assertNull(singleChoiceValue2.validateString("1"));
        assertNotNull(singleChoiceValue2.validateString("Not an int"));
    }
    
    /**
     * Test of setStringValue method, of class SingleChoiceParameterValue.
     */
    @Test
    public void testSetStringValue() {
        System.out.println("setStringValue");

        final SingleChoiceParameterValue singleChoiceValue = new SingleChoiceParameterValue();
        assertNull(singleChoiceValue.getChoice());
        
        // fails while choice hasn't been set
        assertFalse(singleChoiceValue.setStringValue(null));
        assertNull(singleChoiceValue.getChoice());
        
        // set a value in choice for the first time
        assertTrue(singleChoiceValue.setStringValue("my choice"));
        assertEquals(singleChoiceValue.getChoice(), "my choice");
        
        // fails as it is the same value
        assertFalse(singleChoiceValue.setStringValue("my choice"));
        assertEquals(singleChoiceValue.getChoice(), "my choice");
        
        // is ok now that choice has been set
        assertTrue(singleChoiceValue.setStringValue(null));
        assertNull(singleChoiceValue.getChoice());
    }
    
    /**
     * Test of setObjectValue method, of class SingleChoiceParameterValue.
     */
    @Test
    public void testSetObjectValue() {
        System.out.println("setObjectValue");
        
        final List<String> myOptions = Arrays.asList("option1", "option2", "option3");
        
        final SingleChoiceParameterValue existingSingleChoiceValue = new SingleChoiceParameterValue();
        existingSingleChoiceValue.setOptions(myOptions);
        existingSingleChoiceValue.setChoice("option2");
        
        final SingleChoiceParameterValue singleChoiceValue = new SingleChoiceParameterValue();
        assertTrue(singleChoiceValue.getOptions().isEmpty());
        assertNull(singleChoiceValue.getChoice());
        
        assertTrue(singleChoiceValue.setObjectValue(existingSingleChoiceValue));
        assertEquals(singleChoiceValue.getOptions().size(), 3);
        assertEquals(singleChoiceValue.getChoice(), "option2");
        
        // no values changed
        assertFalse(singleChoiceValue.setObjectValue(existingSingleChoiceValue));
        assertEquals(singleChoiceValue.getOptions().size(), 3);
        assertEquals(singleChoiceValue.getChoice(), "option2");
        
        assertTrue(singleChoiceValue.setObjectValue(new StringParameterValue("option3")));
        assertEquals(singleChoiceValue.getOptions().size(), 3);
        assertEquals(singleChoiceValue.getChoice(), "option3");
        
        // everything is cleared
        assertTrue(singleChoiceValue.setObjectValue(null));
        assertTrue(singleChoiceValue.getOptions().isEmpty());
        assertNull(singleChoiceValue.getChoice());
    }
    
    /**
     * Test of setObjectValue method, of class SingleChoiceParameterValue. Invalid argument
     */
    @Test(expectedExceptions = IllegalArgumentException.class, 
            expectedExceptionsMessageRegExp = "Invalid argument")
    public void testSetObjectValueInvalidArgument() {
        System.out.println("setObjectValueInvalidArgument");

        final SingleChoiceParameterValue singleChoiceValue = new SingleChoiceParameterValue();
        singleChoiceValue.setObjectValue(true);
    }
    
    /**
     * Test of createCopy method, of class SingleChoiceParameterValue.
     */
    @Test
    public void testCreateCopy() {
        System.out.println("createCopy");

        final SingleChoiceParameterValue singleChoiceValue = new SingleChoiceParameterValue();
        final SingleChoiceParameterValue singleChoiceValueCopy = singleChoiceValue.createCopy();
        assertTrue(singleChoiceValueCopy.equals(singleChoiceValue));

        // Ensure deep copy and not shallow
        singleChoiceValue.setOptions(Arrays.asList("my choice"));
        assertTrue(singleChoiceValue.setChoice("my choice"));
        assertFalse(singleChoiceValueCopy.equals(singleChoiceValue));  
    }
    
    /**
     * Test of equals method, of class IntegerParameterValue.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        
        final SingleChoiceParameterValue singleChoiceValue = new SingleChoiceParameterValue();
        final SingleChoiceParameterValue comp1 = new SingleChoiceParameterValue(StringParameterValue.class);
        comp1.setOptions(Arrays.asList("my option"));
        comp1.setChoice("my option");
        final SingleChoiceParameterValue comp2 = new SingleChoiceParameterValue(StringParameterValue.class);
        
        assertFalse(singleChoiceValue.equals(null));
        assertFalse(singleChoiceValue.equals(true));
        assertFalse(singleChoiceValue.equals(comp1));
        assertTrue(singleChoiceValue.equals(comp2));
    }
    
    /**
     * Test of toString method, of class SingleChoiceParameterValue.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        
        final SingleChoiceParameterValue singleChoiceValue = new SingleChoiceParameterValue();
        assertNull(singleChoiceValue.toString());
        singleChoiceValue.setOptions(Arrays.asList("my option"));
        singleChoiceValue.setChoice("my option");
        assertEquals(singleChoiceValue.toString(), "my option");
    }
}