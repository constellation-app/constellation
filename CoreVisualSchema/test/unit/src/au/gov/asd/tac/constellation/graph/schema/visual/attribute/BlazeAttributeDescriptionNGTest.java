/*
 * Copyright 2010-2024 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.Blaze;
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
public class BlazeAttributeDescriptionNGTest {
    
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
     * Test of convertFromString method, of class BlazeAttributeDescription.
     */
    @Test
    public void testConvertFromString() {
        System.out.println("convertFromString");

        final BlazeAttributeDescription instance = new BlazeAttributeDescription();
        final Blaze nullResult = instance.convertFromString(null);
        // default value is null
        assertNull(nullResult);

        final Blaze blankResult = instance.convertFromString("   ");
        // also default value
        assertNull(blankResult);

        final Blaze dodgyBlazeResult = instance.convertFromString("Not a blaze");
        assertNull(dodgyBlazeResult);

        final Blaze validBlazeResult = instance.convertFromString("42;RGB254255106");
        final Blaze expectedBlaze = new Blaze(42, ConstellationColor.BANANA);
        assertEquals(validBlazeResult, expectedBlaze);
    }

    /**
     * Test of getVersion method, of class BlazeAttributeDescription.
     */
    @Test
    public void testGetVersion() {
        System.out.println("getVersion");

        final BlazeAttributeDescription instance = new BlazeAttributeDescription();
        assertEquals(instance.getVersion(), 1);
    }
}