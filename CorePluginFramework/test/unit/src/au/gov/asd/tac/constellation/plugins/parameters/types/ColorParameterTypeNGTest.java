/*
 * Copyright 2010-2025 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.plugins.parameters.types.ColorParameterType.ColorParameterValue;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
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
 * @author antares
 */
public class ColorParameterTypeNGTest {
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of build method, of class ColorParameterType. One Parameter version
     */
    @Test
    public void testBuildOneParameter() {
        System.out.println("buildOneParameter");
        
        final PluginParameter<ColorParameterValue> colorParam = ColorParameterType.build("My Color");
        
        assertEquals(colorParam.getColorValue(), ConstellationColor.CLOUDS); //the default color
        assertEquals(colorParam.getId(), "My Color");
        assertEquals(colorParam.getType().getId(), "color");
    }

    /**
     * Test of build method, of class ColorParameterType. Two Parameter Version
     */
    @Test
    public void testBuildTwoParameter() {
        System.out.println("buildTwoParameter");
        
        final ColorParameterValue myColor = new ColorParameterValue(ConstellationColor.BANANA);
        final PluginParameter<ColorParameterValue> colorParam = ColorParameterType.build("My Color", myColor);
        
        assertEquals(colorParam.getColorValue(), ConstellationColor.BANANA);
        assertEquals(colorParam.getId(), "My Color");
        assertEquals(colorParam.getType().getId(), "color");
    }
    
    /**
     * Test of set method, of class ColorParameterValue.
     */
    @Test
    public void testSet() {
        System.out.println("set");
        
        final ColorParameterValue color = new ColorParameterValue();
        assertEquals(color.get(), ConstellationColor.CLOUDS); //the default color
        
        assertTrue(color.set(ConstellationColor.BANANA));
        assertEquals(color.get(), ConstellationColor.BANANA);
        // same color so no change occurs
        assertFalse(color.set(ConstellationColor.BANANA));
    }
    
    /**
     * Test of validateString method, of class ColorParameterValue.
     */
    @Test
    public void testValidateString() {
        System.out.println("validateString");
        
        final ColorParameterValue color = new ColorParameterValue();
        assertNull(color.validateString("#FFFFFF"));
    }
    
    /**
     * Test of setStringValue method, of class ColorParameterValue.
     */
    @Test
    public void testSetStringValue() {
        System.out.println("setStringValue");
        
        final ColorParameterValue color = new ColorParameterValue();
        assertEquals(color.get(), ConstellationColor.CLOUDS); //the default color
        
        assertTrue(color.setStringValue("Banana"));
        assertEquals(color.get(), ConstellationColor.BANANA);
        
        assertTrue(color.setStringValue(null));
        assertNull(color.get());
        // evaluates to same color so no change occurs
        assertFalse(color.setStringValue("not a color"));
    }
    
    /**
     * Test of setObjectValue method, of class ColorParameterValue.
     */
    @Test
    public void testSetObjectValue() {
        System.out.println("setObjectValue");
        
        final ColorParameterValue color = new ColorParameterValue();
        assertEquals(color.getObjectValue(), ConstellationColor.CLOUDS); //the default color
        
        assertTrue(color.setObjectValue(ConstellationColor.BANANA));
        assertEquals(color.getObjectValue(), ConstellationColor.BANANA);
        
        assertTrue(color.setObjectValue(null));
        assertEquals(color.getObjectValue(), ConstellationColor.CLOUDS);
    }
    
    /**
     * Test of setObjectValue method, of class ColorParameterValue. Invalid color
     */
    @Test(expectedExceptions = IllegalArgumentException.class, 
            expectedExceptionsMessageRegExp = "Unexpected class class java.lang.Boolean")
    public void testSetObjectValueInvalidColor() {
        System.out.println("setObjectValueInvalidColor");
        
        final ColorParameterValue color = new ColorParameterValue();
        color.setObjectValue(Boolean.TRUE);
    }
    
    /**
     * Test of createCopy method, of class ColorParameterValue.
     */
    @Test
    public void testCreateCopy() {
        System.out.println("createCopy");
        
        final ColorParameterValue color = new ColorParameterValue();
        final ColorParameterValue colorCopy = color.createCopy();
        assertTrue(colorCopy.equals(color));

        // Ensure deep copy and not shallow
        assertTrue(color.set(ConstellationColor.BANANA));
        assertFalse(colorCopy.equals(color));
    }
    
    /**
     * Test of equals method, of class ColorParameterValue.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        
        final ColorParameterValue color = new ColorParameterValue();
        final ColorParameterValue comp1 = new ColorParameterValue(ConstellationColor.BANANA);
        final ColorParameterValue comp2 = new ColorParameterValue(ConstellationColor.CLOUDS);
        
        assertFalse(color.equals(null));
        assertFalse(color.equals(true));
        assertFalse(color.equals(comp1));
        assertTrue(color.equals(comp2));
    }
    
    /**
     * Test of toString method, of class ColorParameterValue.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        
        final ColorParameterValue color = new ColorParameterValue();
        assertEquals(color.toString(), "Clouds");
        color.setStringValue("#123456");
        assertEquals(color.toString(), "#123456");
    }
}
