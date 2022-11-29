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

import au.gov.asd.tac.constellation.utilities.visual.LineStyle;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class LineStyleAttributeDescriptionNGTest {

    public LineStyleAttributeDescriptionNGTest() {
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
     * Test of convertFromString method, of class LineStyleAttributeDescription.
     */
    @Test
    public void testConvertFromString() {
        System.out.println("convertFromString");

        final LineStyleAttributeDescription instance = new LineStyleAttributeDescription();

        final LineStyle nullResult = instance.convertFromString(null);
        // should be the default here
        assertEquals(nullResult, LineStyle.SOLID);

        final LineStyle blankResult = instance.convertFromString("   ");
        // should be the default here as well
        assertEquals(blankResult, LineStyle.SOLID);

        final LineStyle validResult = instance.convertFromString("DASHED");
        assertEquals(validResult, LineStyle.DASHED);
    }

    /**
     * Test of setInt method, of class LineStyleAttributeDescription. Trying to set before capacity has been allocated to do so
     */
    @Test(expectedExceptions = {ArrayIndexOutOfBoundsException.class})
    public void testSetIntBadSet1() {
        System.out.println("setIntBadSet1");

        final LineStyleAttributeDescription instance = new LineStyleAttributeDescription();
        assertEquals(instance.getCapacity(), 0);
        //trying to set when there is no capacity available
        instance.setInt(0, 0);
    }

    /**
     * Test of setInt method, of class LineStyleAttributeDescription. Trying to set to non-existent LineStyle
     */
    @Test(expectedExceptions = {ArrayIndexOutOfBoundsException.class})
    public void testSetIntBadSet2() {
        System.out.println("setIntBadSet2");

        final LineStyleAttributeDescription instance = new LineStyleAttributeDescription();
        instance.setCapacity(1);

        instance.setInt(0, 4);
    }

    /**
     * Test of setInt method, of class LineStyleAttributeDescription.
     */
    @Test
    public void testSetInt() {
        System.out.println("setInt");

        final LineStyleAttributeDescription instance = new LineStyleAttributeDescription();
        instance.setCapacity(1);

        assertEquals(instance.getObject(0), LineStyle.SOLID);

        instance.setInt(0, 2);
        assertEquals(instance.getObject(0), LineStyle.DASHED);
    }

}