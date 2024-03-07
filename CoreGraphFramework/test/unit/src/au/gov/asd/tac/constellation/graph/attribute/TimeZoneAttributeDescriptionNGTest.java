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
package au.gov.asd.tac.constellation.graph.attribute;

import au.gov.asd.tac.constellation.utilities.temporal.TimeZoneUtilities;
import java.time.ZoneId;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author arcturus
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class TimeZoneAttributeDescriptionNGTest extends ConstellationTest {

    TimeZoneAttributeDescription instance;

    public TimeZoneAttributeDescriptionNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        instance = new TimeZoneAttributeDescription();
        instance.setCapacity(1);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of getVersion method, of class TimeZoneAttributeDescription.
     */
    @Test
    public void testGetVersion() {
        int expResult = 1;
        int result = instance.getVersion();
        assertEquals(result, expResult);
    }

    /**
     * Test of convertFromObject method, of class TimeZoneAttributeDescription.
     */
    @Test
    public void testConvertFromObject() {
        Object object = null;
        ZoneId expResult = TimeZoneUtilities.UTC;
        ZoneId result = instance.convertFromObject(object);
        assertEquals(result, expResult);
    }

    /**
     * Test of convertFromString method, of class TimeZoneAttributeDescription.
     */
    @Test
    public void testConvertFromString() {
        String string = "";
        ZoneId expResult = TimeZoneUtilities.UTC;
        ZoneId result = instance.convertFromString(string);
        assertEquals(result, expResult);
    }

    /**
     * Test of getString method, of class TimeZoneAttributeDescription.
     */
    @Test
    public void testGetString() {
        int id = 0;
        String expResult = "+00:00 [UTC]";
        String result = instance.getString(id);
        assertEquals(result, expResult);
    }

//    /**
//     * Test of hashCode method, of class TimeZoneAttributeDescription.
//     */
//    @Test
//    public void testHashCode() {
//        int id = 0;
//        int expResult = 0;
//        int result = instance.hashCode(id);
//        assertEquals(result, expResult);
//    }
}
