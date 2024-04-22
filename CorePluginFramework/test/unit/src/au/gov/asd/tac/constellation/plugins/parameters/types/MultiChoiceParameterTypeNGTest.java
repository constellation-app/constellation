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
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType.MultiChoiceParameterValue;
import java.util.Arrays;
import java.util.List;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
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
public class MultiChoiceParameterTypeNGTest {

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
     * Test of build method, of class MultiChoiceParameterType. One Parameter version
     */
    @Test
    public void testBuildOneParameter() {
        System.out.println("buildOneParameter");
        
        final PluginParameter<MultiChoiceParameterValue> multiChoiceParam = MultiChoiceParameterType.build("My Choices");
        
        assertEquals(multiChoiceParam.getParameterValue().getInnerClass(), StringParameterValue.class);
        assertEquals(multiChoiceParam.getId(), "My Choices");
        assertEquals(multiChoiceParam.getType().getId(), "multichoice");
    }

    /**
     * Test of build method, of class MultiChoiceParameterType. Two Parameter version specifying inner class
     */
    @Test
    public void testBuildTwoParameterClass() {
        System.out.println("buildTwoParametersClass");
        
        final PluginParameter<MultiChoiceParameterValue> multiChoiceParam = MultiChoiceParameterType.build("My Choices", ElementTypeParameterValue.class);
        
        assertEquals(multiChoiceParam.getParameterValue().getInnerClass(), ElementTypeParameterValue.class);
        assertEquals(multiChoiceParam.getId(), "My Choices");
        assertEquals(multiChoiceParam.getType().getId(), "multichoice");
    }

    /**
     * Test of build method, of class MultiChoiceParameterType. Two Parameter version specifying parameter value
     */
    @Test
    public void testBuildTwoParametersParameterValue() {
        System.out.println("buildTwoParametersParameterValue");
        
        final MultiChoiceParameterValue multiChoiceValue = new MultiChoiceParameterValue(ElementTypeParameterValue.class);
        final MultiChoiceParameterValue multiChoiceValue2 = new MultiChoiceParameterValue(multiChoiceValue);
        final PluginParameter<MultiChoiceParameterValue> multiChoiceParam = MultiChoiceParameterType.build("My Choices", multiChoiceValue);
        final PluginParameter<MultiChoiceParameterValue> multiChoiceParam2 = MultiChoiceParameterType.build("Also My Choices", multiChoiceValue2);
        
        assertEquals(multiChoiceParam.getParameterValue().getInnerClass(), ElementTypeParameterValue.class);
        assertEquals(multiChoiceParam.getId(), "My Choices");
        assertEquals(multiChoiceParam.getType().getId(), "multichoice");
        assertEquals(multiChoiceParam2.getParameterValue().getInnerClass(), ElementTypeParameterValue.class);
        assertEquals(multiChoiceParam2.getId(), "Also My Choices");
        assertEquals(multiChoiceParam2.getType().getId(), "multichoice");
    }

    /**
     * Test of setOptions method, of class MultiChoiceParameterType.
     */
    @Test
    public void testSetOptions() {
        System.out.println("setOptions");
        
        final PluginParameter<MultiChoiceParameterValue> multiChoiceParameter = MultiChoiceParameterType.build("my choices");
        assertTrue(MultiChoiceParameterType.getOptions(multiChoiceParameter).isEmpty());
        
        final List<String> options = Arrays.asList("option1", "option2", "option3");
        MultiChoiceParameterType.setOptions(multiChoiceParameter, options);
        assertEquals(MultiChoiceParameterType.getOptions(multiChoiceParameter).size(), 3);
    }

    /**
     * Test of setOptionsData method, of class MultiChoiceParameterType.
     */
    @Test
    public void testSetOptionsData() {
        System.out.println("setOptionsData");
        
        final PluginParameter<MultiChoiceParameterValue> multiChoiceParameter = MultiChoiceParameterType.build("my choices");
        assertTrue(MultiChoiceParameterType.getOptionsData(multiChoiceParameter).isEmpty());
        
        final List<StringParameterValue> options = Arrays.asList(new StringParameterValue("option1"), 
                new StringParameterValue("option2"), new StringParameterValue("option3"));
        MultiChoiceParameterType.setOptionsData(multiChoiceParameter, options);
        assertEquals(MultiChoiceParameterType.getOptionsData(multiChoiceParameter).size(), 3);
    }

    /**
     * Test of setChoices method, of class MultiChoiceParameterType.
     */
    @Test
    public void testSetChoices() {
        System.out.println("setChoices");
        
        final PluginParameter<MultiChoiceParameterValue> multiChoiceParameter = MultiChoiceParameterType.build("my choices");
        //will be empty as there are no options
        MultiChoiceParameterType.setChoices(multiChoiceParameter, Arrays.asList("option2"));
        assertTrue(MultiChoiceParameterType.getChoices(multiChoiceParameter).isEmpty());
        
        final List<String> options = Arrays.asList("option1", "option2", "option3");
        MultiChoiceParameterType.setOptions(multiChoiceParameter, options);
        MultiChoiceParameterType.setChoices(multiChoiceParameter, Arrays.asList("option2"));
        final List<String> choices = MultiChoiceParameterType.getChoices(multiChoiceParameter);
        assertEquals(choices.size(), 1);
        assertTrue(choices.contains("option2"));
        
        MultiChoiceParameterType.setChoices(multiChoiceParameter, Arrays.asList("option1", "option3"));
        final List<String> choices2 = MultiChoiceParameterType.getChoices(multiChoiceParameter);
        assertEquals(choices2.size(), 2);
        assertFalse(choices2.contains("option2"));
    }

    /**
     * Test of setChoicesData method, of class MultiChoiceParameterType.
     */
    @Test
    public void testSetChoicesData() {
        System.out.println("setChoicesData");
        
        final PluginParameter<MultiChoiceParameterValue> multiChoiceParameter = MultiChoiceParameterType.build("my choices");
        //will be empty as there are no options
        MultiChoiceParameterType.setChoicesData(multiChoiceParameter, Arrays.asList(new StringParameterValue("option2")));
        assertTrue(MultiChoiceParameterType.getChoices(multiChoiceParameter).isEmpty());
        
        final StringParameterValue option1 = new StringParameterValue("option1");
        final StringParameterValue option2 = new StringParameterValue("option2");
        final StringParameterValue option3 = new StringParameterValue("option3");
        final List<StringParameterValue> options = Arrays.asList(option1, option2, option3);
        MultiChoiceParameterType.setOptionsData(multiChoiceParameter, options);
        MultiChoiceParameterType.setChoicesData(multiChoiceParameter, Arrays.asList(option2));
        final List<? extends ParameterValue> choices = MultiChoiceParameterType.getChoicesData(multiChoiceParameter);
        assertEquals(choices.size(), 1);
        assertTrue(choices.contains(option2));
        
        MultiChoiceParameterType.setChoicesData(multiChoiceParameter, Arrays.asList(option1, option3));
        final List<? extends ParameterValue> choices2 = MultiChoiceParameterType.getChoicesData(multiChoiceParameter);
        assertEquals(choices2.size(), 2);
        assertFalse(choices2.contains(option2));
    }

    /**
     * Test of setState method, of class MultiChoiceParameterType.
     */
    @Test
    public void testSetState() {
        System.out.println("setState");
        
        final PluginParameter<MultiChoiceParameterValue> multiChoiceParameter = MultiChoiceParameterType.build("my choices");
        assertTrue(MultiChoiceParameterType.getOptions(multiChoiceParameter).isEmpty());
        assertTrue(MultiChoiceParameterType.getChoices(multiChoiceParameter).isEmpty());
        
        final List<String> options = Arrays.asList("option1", "option2", "option3");
        final List<String> choices = Arrays.asList("option1", "option3");
        
        MultiChoiceParameterType.setState(multiChoiceParameter, options, choices);
        assertEquals(MultiChoiceParameterType.getOptions(multiChoiceParameter).size(), 3);
        assertEquals(MultiChoiceParameterType.getChoices(multiChoiceParameter).size(), 2);
    }
    
    /**
     * Test of setStringValue method, of class MultiChoiceParameterValue.
     */
    @Test
    public void testSetStringValue() {
        System.out.println("setStringValue");

        final MultiChoiceParameterValue multiChoiceValue = new MultiChoiceParameterValue();
        assertTrue(multiChoiceValue.getOptions().isEmpty());
        assertTrue(multiChoiceValue.getChoices().isEmpty());
               
        multiChoiceValue.setStringValue("""
                                        option1
                                        option2
                                        \u2713 option3""");
        assertEquals(multiChoiceValue.getOptions().size(), 3);
        assertEquals(multiChoiceValue.getChoices().size(), 1);
        assertTrue(multiChoiceValue.getChoices().contains("option3"));
        
        multiChoiceValue.setStringValue(null);
        assertTrue(multiChoiceValue.getOptions().isEmpty());
        assertTrue(multiChoiceValue.getChoices().isEmpty());
    }
    
    /**
     * Test of setObjectValue method, of class SingleChoiceParameterValue.
     */
    @Test
    public void testSetObjectValue() {
        System.out.println("setObjectValue");
        
        final MultiChoiceParameterValue multiChoiceValue = new MultiChoiceParameterValue();
        multiChoiceValue.setOptions(Arrays.asList("option1", "option2", "option3"));
        multiChoiceValue.setChoices(Arrays.asList("option2"));
        final MultiChoiceParameterValue multiChoiceValue2 = new MultiChoiceParameterValue();
        multiChoiceValue2.setOptions(Arrays.asList("option4", "option5"));
        
        assertEquals(multiChoiceValue.getOptions().size(), 3);
        assertTrue(multiChoiceValue.getOptions().contains("option2"));
        assertFalse(multiChoiceValue.getOptions().contains("option4"));
        assertEquals(multiChoiceValue.getChoices().size(), 1);
        
        multiChoiceValue.setObjectValue(multiChoiceValue2);
        
        assertEquals(multiChoiceValue.getOptions().size(), 2);
        assertFalse(multiChoiceValue.getOptions().contains("option2"));
        assertTrue(multiChoiceValue.getOptions().contains("option4"));
        assertTrue(multiChoiceValue.getChoices().isEmpty());
    }
    
    /**
     * Test of createCopy method, of class MultiChoiceParameterValue.
     */
    @Test
    public void testCreateCopy() {
        System.out.println("createCopy");

        final MultiChoiceParameterValue multiChoiceValue = new MultiChoiceParameterValue();
        final MultiChoiceParameterValue multiChoiceValueCopy = multiChoiceValue.createCopy();
        assertTrue(multiChoiceValueCopy.equals(multiChoiceValue));

        // Ensure deep copy and not shallow
        multiChoiceValue.setOptions(Arrays.asList("my choice"));
        multiChoiceValue.setChoices(Arrays.asList("my choice"));
        assertFalse(multiChoiceValueCopy.equals(multiChoiceValue));  
    }
    
    /**
     * Test of equals method, of class MultiChoiceParameterValue.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        
        final MultiChoiceParameterValue multiChoiceValue = new MultiChoiceParameterValue();
        final MultiChoiceParameterValue comp1 = new MultiChoiceParameterValue(StringParameterValue.class);
        comp1.setOptions(Arrays.asList("my option"));
        comp1.setChoices(Arrays.asList("my option"));
        final MultiChoiceParameterValue comp2 = new MultiChoiceParameterValue(StringParameterValue.class);
        
        assertFalse(multiChoiceValue.equals(null));
        assertFalse(multiChoiceValue.equals(true));
        assertFalse(multiChoiceValue.equals(comp1));
        assertTrue(multiChoiceValue.equals(comp2));
    }
    
     /**
     * Test of toString method, of class MultiChoiceParameterValue.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        
        final MultiChoiceParameterValue multiChoiceValue = new MultiChoiceParameterValue();
        multiChoiceValue.setOptions(Arrays.asList("option1", "option2", "option3"));
        multiChoiceValue.setChoices(Arrays.asList("option2"));
        assertEquals(multiChoiceValue.toString(), """
                                                  option1
                                                  \u2713 option2
                                                  option3""");
    }
}
