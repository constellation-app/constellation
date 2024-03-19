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
package au.gov.asd.tac.constellation.graph.schema.analytic.attribute.objects;

import static org.testng.Assert.assertEquals;
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
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class RawDataNGTest extends ConstellationTest {

    public RawDataNGTest() {
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
     * Test of hasRawIdentifier method, of class RawData.
     */
    @Test
    public void testHasRawIdentifierWithAnIdentifier() {
        RawData instance = new RawData("123.456.789<IP Address>");
        boolean expResult = true;
        boolean result = instance.hasRawIdentifier();
        assertEquals(result, expResult);
    }

    @Test
    public void testHasRawIdentifierWithoutAnIdentifier() {
        RawData instance = new RawData("<IP Address>");
        boolean expResult = false;
        boolean result = instance.hasRawIdentifier();
        assertEquals(result, expResult);
    }

    /**
     * Test of getRawIdentifier method, of class RawData.
     */
    @Test
    public void testGetRawIdentifier() {
        RawData instance = new RawData("123.456.789<IP Address>");
        String expResult = "123.456.789";
        String result = instance.getRawIdentifier();
        assertEquals(result, expResult);
    }

    /**
     * Test of hasRawType method, of class RawData.
     */
    @Test
    public void testHasRawTypeWithRawType() {
        RawData instance = new RawData("123.456.789<IP Address>");
        boolean expResult = true;
        boolean result = instance.hasRawType();
        assertEquals(result, expResult);
    }

    @Test
    public void testHasRawTypeWithoutRawType() {
        RawData instance = new RawData("123.456.789");
        boolean expResult = false;
        boolean result = instance.hasRawType();
        assertEquals(result, expResult);
    }

    /**
     * Test of getRawType method, of class RawData.
     */
    @Test
    public void testGetRawType() {
        RawData instance = new RawData("123.456.789<IP Address>");
        String expResult = "IP Address";
        String result = instance.getRawType();
        assertEquals(result, expResult);
    }

    /**
     * Test of merge method, of class RawData.
     */
    @Test
    public void testMerge() {
        RawData primaryValue = new RawData("123.456.789<IP Address>");
        RawData secondaryValue = new RawData("987.654.321<IP Address>");
        RawData expResult = new RawData("123.456.789<IP Address>");
        RawData result = RawData.merge(primaryValue, secondaryValue);
        assertEquals(result, expResult);
    }

    @Test
    public void testMergeWithThePrimayMissingAType() {
        RawData primaryValue = new RawData("123.456.789");
        RawData secondaryValue = new RawData("987.654.321<IP Address>");
        RawData expResult = new RawData("123.456.789<IP Address>");
        RawData result = RawData.merge(primaryValue, secondaryValue);
        assertEquals(result, expResult);
    }

    /**
     * Test of isEmpty method, of class RawData.
     */
    @Test
    public void testIsEmpty() {
        RawData instance = new RawData("");
        boolean expResult = true;
        boolean result = instance.isEmpty();
        assertEquals(result, expResult);
    }

    /**
     * Test of equals method, of class RawData.
     */
    @Test
    public void testEquals() {
        Object obj = new RawData("123.456.789<IP Address>");
        RawData instance = new RawData("123.456.789<IP Address>");
        boolean expResult = true;
        boolean result = instance.equals(obj);
        assertEquals(result, expResult);
    }

    /**
     * Test of toString method, of class RawData.
     */
    @Test
    public void testToString() {
        RawData instance = new RawData("123.456.789<IP Address>");
        String expResult = "123.456.789<IP Address>";
        String result = instance.toString();
        assertEquals(result, expResult);
    }
}
