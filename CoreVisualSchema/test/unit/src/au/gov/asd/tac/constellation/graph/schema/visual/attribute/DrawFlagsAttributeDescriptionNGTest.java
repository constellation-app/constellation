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

import au.gov.asd.tac.constellation.utilities.visual.DrawFlags;
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
public class DrawFlagsAttributeDescriptionNGTest {
    
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
     * Test of convertFromString method, of class DrawFlagsAttributeDescription.
     */
    @Test
    public void testConvertFromString() {
        System.out.println("convertFromString");

        final DrawFlagsAttributeDescription instance = new DrawFlagsAttributeDescription();

        final DrawFlags nullResult = instance.convertFromString(null);
        // should be the default here
        assertEquals(nullResult, DrawFlags.ALL);

        final DrawFlags blankResult = instance.convertFromString("   ");
        // should be the default here as well
        assertEquals(blankResult, DrawFlags.ALL);

        final DrawFlags validResult = instance.convertFromString("3");
        assertEquals(validResult, new DrawFlags(3));
    }

    /**
     * Test of setInt method, of class DrawFlagsAttributeDescription. Trying to set before capacity has been allocated to do so
     */
    @Test(expectedExceptions = ArrayIndexOutOfBoundsException.class)
    public void testSetIntBadSet() {
        System.out.println("setIntBadSet");

        final DrawFlagsAttributeDescription instance = new DrawFlagsAttributeDescription();
        assertEquals(instance.getCapacity(), 0);
        //trying to set when there is no capacity available
        instance.setInt(0, 0);
    }

    /**
     * Test of setInt method, of class DrawFlagsAttributeDescription.
     */
    @Test
    public void testSetInt() {
        System.out.println("setInt");

        final DrawFlagsAttributeDescription instance = new DrawFlagsAttributeDescription();
        instance.setCapacity(1);

        assertEquals(instance.getInt(0), 31);
        assertEquals(instance.getObject(0), DrawFlags.ALL);

        instance.setInt(0, 3);
        assertEquals(instance.getInt(0), 3);
        assertEquals(instance.getString(0), "3");
    }
}