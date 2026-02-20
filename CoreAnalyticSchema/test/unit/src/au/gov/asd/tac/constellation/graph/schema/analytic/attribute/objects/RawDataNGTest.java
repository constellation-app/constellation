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
package au.gov.asd.tac.constellation.graph.schema.analytic.attribute.objects;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Raw Data Test.
 *
 * @author arcturus
 */
public class RawDataNGTest {
    
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
     * Test of hasRawIdentifier method, of class RawData.
     */
    @Test
    public void testHasRawIdentifier() {
        System.out.println("hasRawIdentifier");
        
        final RawData instance1 = new RawData("123.456.789<IP Address>");
        final RawData instance2 = new RawData("<IP Address>");
        
        assertTrue(instance1.hasRawIdentifier());
        assertFalse(instance2.hasRawIdentifier());
    }
    
    /**
     * Test of getRawIdentifier method, of class RawData.
     */
    @Test
    public void testGetRawIdentifier() {
        System.out.println("getRawIdentifier");
        
        final RawData instance = new RawData("123.456.789<IP Address>");
        assertEquals(instance.getRawIdentifier(), "123.456.789");
    }

    /**
     * Test of hasRawType method, of class RawData.
     */
    @Test
    public void testHasRawType() {
        System.out.println("hasRawType");
        
        final RawData instance1 = new RawData("123.456.789<IP Address>");
        final RawData instance2 = new RawData("123.456.789");
        
        assertTrue(instance1.hasRawType());
        assertFalse(instance2.hasRawType());
    }
    
    /**
     * Test of getRawType method, of class RawData.
     */
    @Test
    public void testGetRawType() {
        System.out.println("getRawType");
        
        final RawData instance = new RawData("123.456.789<IP Address>");
        assertEquals(instance.getRawType(), "IP Address");
    }

    /**
     * Test of merge method, of class RawData.
     */
    @Test
    public void testMerge() {
        System.out.println("merge");
        
        final RawData primaryValue = new RawData("123.456.789<IP Address>");
        final RawData primaryValueNoIdentifier = new RawData("<IP Address>");
        final RawData primaryValueNoType = new RawData("123.456.789");
        final RawData secondaryValue = new RawData("987.654.321<IPv4 Address>");
        
        assertEquals(RawData.merge(primaryValue, null), primaryValue);
        assertEquals(RawData.merge(null, secondaryValue), secondaryValue);
        
        assertEquals(RawData.merge(primaryValue, secondaryValue), new RawData("123.456.789<IP Address>"));
        assertEquals(RawData.merge(primaryValueNoIdentifier, secondaryValue), new RawData("987.654.321<IP Address>"));
        assertEquals(RawData.merge(primaryValueNoType, secondaryValue), new RawData("123.456.789<IPv4 Address>"));
    }
    
    /**
     * Test of isEmpty method, of class RawData.
     */
    @Test
    public void testIsEmpty() {
        System.out.println("isEmpty");
        
        final RawData instance1 = new RawData("");
        final RawData instance2 = new RawData("123.456.789<IP Address>");
        
        assertTrue(instance1.isEmpty());
        assertFalse(instance2.isEmpty());
    }

    /**
     * Test of equals method, of class RawData.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        
        final RawData instance1 = new RawData("123.456.789<IP Address>");
        final RawData instance2 = new RawData("123.456.789", "IP Address");
        final RawData instance3 = new RawData(null);
        
        assertTrue(instance1.equals(instance2));
        assertFalse(instance1.equals(instance3));
        assertFalse(instance1.equals(null));
        assertFalse(instance1.equals("123.456.789<IP Address>"));
    }

    /**
     * Test of toString method, of class RawData.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        
        final RawData instance = new RawData("123.456.789<IP Address>");
        final RawData instanceNoType = new RawData("123.456.789");
        
        assertEquals(instance.toString(), "123.456.789<IP Address>");
        assertEquals(instanceNoType.toString(), "123.456.789");
    }
    
    /**
     * Test of compareTo method, of class RawData.
     */
    @Test
    public void testCompareTo() {
        System.out.println("compareTo");
        
        final RawData instance1 = new RawData("123.456.789<IP Address>");
        final RawData instance2 = new RawData("123.456.789", "IP ADDRESS");
        final RawData instance3 = new RawData("123.456.789<IPv4 Address>");
        
        // we don't care about the specific value in this case, only the value with respect to 0
        assertTrue(instance1.compareTo(instance2) == 0);
        assertTrue(instance1.compareTo(instance3) < 0);
    }
}
