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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.LayerName;
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
public class LayerNameAttributeDescriptionNGTest {

    public LayerNameAttributeDescriptionNGTest() {
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
     * Test of convertFromString method, of class LayerNameAttributeDescription.
     */
    @Test
    public void testConvertFromString() {
        System.out.println("convertFromString");

        final LayerNameAttributeDescription instance = new LayerNameAttributeDescription();

        final LayerName nullResult = instance.convertFromString(null);
        assertEquals(nullResult, new LayerName(Graph.NOT_FOUND, "DEFAULT"));

        final LayerName blankResult = instance.convertFromString("   ");
        assertEquals(blankResult, new LayerName(Graph.NOT_FOUND, "DEFAULT"));

        final LayerName defaultResult = instance.convertFromString("DEFAULT");
        assertEquals(defaultResult, new LayerName(Graph.NOT_FOUND, "DEFAULT"));

        final LayerName invalidResult = instance.convertFromString("Not a Layer");
        assertNull(invalidResult);

        final LayerName validResult = instance.convertFromString("3,Test Layer");
        assertEquals(validResult, new LayerName(3, "Test Layer"));
    }

    /**
     * Test of convertFromString method, of class LayerNameAttributeDescription. Don't supply a layer number
     */
    @Test(expectedExceptions = {IllegalArgumentException.class}, 
            expectedExceptionsMessageRegExp = "Error converting String 'Bad Number,Test Layer' to layer_name")
    public void testConvertFromStringBadLayerNumber() {
        System.out.println("convertFromStringBadLayerNumber");

        final LayerNameAttributeDescription instance = new LayerNameAttributeDescription();

        instance.convertFromString("Bad Number,Test Layer");
    }

    /**
     * Test of getInt method, of class LayerNameAttributeDescription.
     */
    @Test
    public void testGetInt() {
        System.out.println("getInt");

        final LayerNameAttributeDescription instance = new LayerNameAttributeDescription();
        instance.setCapacity(1);

        assertEquals(instance.getInt(0), Graph.NOT_FOUND);

        instance.setString(0, "3,Test Layer");

        assertEquals(instance.getInt(0), 3);
    }

    /**
     * Test of getString method, of class LayerNameAttributeDescription.
     */
    @Test
    public void testGetString() {
        System.out.println("getString");

        final LayerNameAttributeDescription instance = new LayerNameAttributeDescription();
        instance.setCapacity(1);

        assertEquals(instance.getString(0), "-1107,DEFAULT");

        instance.setString(0, "3,Test Layer");

        assertEquals(instance.getString(0), "3,Test Layer");
    }

}