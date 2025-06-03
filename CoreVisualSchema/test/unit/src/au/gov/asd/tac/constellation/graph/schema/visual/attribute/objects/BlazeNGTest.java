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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects;

import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.Blaze.IllegalBlazeFormatException;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author serpens24
 */
public class BlazeNGTest {
    
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
     * Test of getAngle method, of class Blaze.
     */
    @Test
    public void testGetAngle() {
        System.out.println("BlazeNGTest.testGetAngle");
        final int angle = 23;
        Blaze instance = new Blaze(angle, ConstellationColor.YELLOW);
        assertEquals(instance.getAngle(), angle);
    }

    /**
     * Test of getColor method, of class Blaze.
     */
    @Test
    public void testGetColor() {
        System.out.println("BlazeNGTest.testGetColor");
        Blaze instance = new Blaze(0, ConstellationColor.YELLOW);
        assertEquals(instance.getColor(), ConstellationColor.YELLOW);
    }

    /**
     * Test of valueOf method, of class Blaze.
     */
    @Test
    public void testValueOf() {
        System.out.println("BlazeNGTest.testValueOf");

        assertNull(Blaze.valueOf(null));
        assertNull(Blaze.valueOf(""));
        assertNull(Blaze.valueOf("badsanta"));
        
        // invalid string (bad color)
        try {
            Blaze.valueOf("99;fruitsalad");
            fail("Exception not thrown");
        } catch (IllegalBlazeFormatException ex) {
            assertTrue("Undefined color for blaze.".equals(ex.getMessage()));
        } catch (Exception ex) {
            fail("Incorrect exception thrown");
        }
        
        // Valid Blaze - name color
        Blaze instance = Blaze.valueOf("99;red");
        assertEquals(instance.getAngle(), 99);
        assertEquals(instance.getColor(), ConstellationColor.RED);
        
        // Valid Blaze - RGB color
        instance = Blaze.valueOf("99;RGB255000000000");
        assertEquals(instance.getAngle(), 99);
        assertEquals(instance.getColor(), ConstellationColor.RED);
        
        // Valid Blaze - RGB (with comma) color
        instance = Blaze.valueOf("99;1.0,0.0,0.0");
        assertEquals(instance.getAngle(), 99);
        assertEquals(instance.getColor(), ConstellationColor.RED);
        
        // Valid Blaze - RGB (with comma) color
        instance = Blaze.valueOf("99;[1.0,0.0,0.0]");
        assertEquals(instance.getAngle(), 99);
        assertEquals(instance.getColor(), ConstellationColor.RED);
        
        // Valid Blaze - #HTML color
        instance = Blaze.valueOf("99;#FF0000");
        assertEquals(instance.getAngle(), 99);
        assertEquals(instance.getColor(), ConstellationColor.RED);
    }

    /**
     * Test of toString method, of class Blaze.
     */
    @Test
    public void testToString() {
        System.out.println("BlazeNGTest.testToString");
        Blaze instance = new Blaze(23, ConstellationColor.RED);
        assertEquals(instance.toString(), "23;Red");
        
        // TODO RGB color
    }

    /**
     * Test of hashCode method, of class Blaze.
     */
    @Test
    public void testHashCode() {
        System.out.println("BlazeNGTest.testHashCode");
        Blaze instance = new Blaze(23, ConstellationColor.RED);
        assertEquals(instance.hashCode(), -472389919);

        instance = new Blaze(23, null);
        assertEquals(instance.hashCode(), 20540);  
    }

    /**
     * Test of equals method, of class Blaze.
     */
    @Test
    public void testEquals() {
        System.out.println("BlazeNGTest.testEquals");

        Blaze instance = new Blaze(23, ConstellationColor.RED);
        assertFalse(instance.equals(null));
        assertFalse(instance.equals(new String("RED")));
        assertFalse(instance.equals(new Blaze(24, ConstellationColor.RED)));
        assertFalse(instance.equals(new Blaze(23, ConstellationColor.BLUE)));
        assertFalse(instance.equals(new Blaze(23, null)));
        assertTrue(instance.equals(new Blaze(23, ConstellationColor.RED)));
        
        instance = new Blaze(23, null);
        assertFalse(instance.equals(new Blaze(23, ConstellationColor.RED)));
        assertTrue(instance.equals(new Blaze(23, null)));
    }

    /**
     * Test of compareTo method, of class Blaze.
     */
    @Test
    public void testCompareTo() {
        System.out.println("BlazeNGTest.testCompareTo");

        Blaze instance = new Blaze(23, ConstellationColor.RED);
        
        // Show same color always results in 0 result
        assertEquals(instance.compareTo(new Blaze(24, ConstellationColor.RED)), 0);
        
        // Compare named colors
        assertEquals(instance.compareTo(new Blaze(23, ConstellationColor.BANANA)), 16);
        assertEquals(instance.compareTo(new Blaze(23, ConstellationColor.YELLOW)), -7);
    } 
}
