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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class ColorAttributeDescriptionNGTest {

    public ColorAttributeDescriptionNGTest() {
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
     * Test of convertFromObject method, of class ColorAttributeDescription.
     */
    @Test
    public void testConvertFromObject() {
        System.out.println("convertFromObject");

        final ColorAttributeDescription instance = new ColorAttributeDescription();

        final ConstellationColor result1 = instance.convertFromObject("BANANA");
        assertEquals(result1, ConstellationColor.BANANA);

        final ConstellationColor result2 = instance.convertFromObject(0xFEFF6AFF);
        assertEquals(result2, ConstellationColor.BANANA);
    }

    /**
     * Test of convertFromObject method, of class ColorAttributeDescription. Invalid colour passed through
     */
    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void testConvertFromObjectNotAColour() {
        System.out.println("convertFromObjectNotAColour");

        final ColorAttributeDescription instance = new ColorAttributeDescription();        
        instance.convertFromObject(true);
    }

    /**
     * Test of convertFromString method, of class ColorAttributeDescription.
     */
    @Test
    public void testConvertFromString() {
        System.out.println("convertFromString");

        final ColorAttributeDescription instance = new ColorAttributeDescription();

        final ConstellationColor nullResult = instance.convertFromString(null);
        assertNull(nullResult);

        final ConstellationColor blankResult = instance.convertFromString("   ");
        assertNull(blankResult);

        final ConstellationColor validResult = instance.convertFromObject("BANANA");
        assertEquals(validResult, ConstellationColor.BANANA);
    }

    /**
     * Test of setDefault method, of class ColorAttributeDescription.
     */
    @Test
    public void testSetDefault() {
        System.out.println("setDefault");

        final ColorAttributeDescription instance = new ColorAttributeDescription();
        assertNull(instance.getDefault());

        instance.setDefault(ConstellationColor.CYAN);
        assertEquals(instance.getDefault(), ConstellationColor.CYAN);

        instance.setDefault("BANANA");
        assertEquals(instance.getDefault(), ConstellationColor.BANANA);
    }

    /**
     * Test of setInt method, of class ColorAttributeDescription. Trying to set before capacity has been allocated to do so
     */
    @Test(expectedExceptions = {ArrayIndexOutOfBoundsException.class})
    public void testSetIntBadSet() {
        System.out.println("setIntBadSet");

        final ColorAttributeDescription instance = new ColorAttributeDescription();
        assertEquals(instance.getCapacity(), 0);
        //trying to set when there is no capacity available
        instance.setInt(0, 0);
    }

    /**
     * Test of setInt method, of class ColorAttributeDescription.
     */
    @Test
    public void testSetInt() {
        System.out.println("setInt");

        final ColorAttributeDescription instance = new ColorAttributeDescription();
        instance.setCapacity(1);

        assertEquals(instance.getInt(0), 0);
        assertNull(instance.getObject(0));

        instance.setInt(0, 0xFEFF6AFF);
        assertEquals(instance.getInt(0), 0xFEFF6AFF);
        assertEquals(instance.getObject(0), ConstellationColor.BANANA);
    }   
}