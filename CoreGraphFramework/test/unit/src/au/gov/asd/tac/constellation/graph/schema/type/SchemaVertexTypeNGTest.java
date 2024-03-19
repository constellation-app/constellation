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
package au.gov.asd.tac.constellation.graph.schema.type;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.CharacterIconProvider;
import au.gov.asd.tac.constellation.utilities.icon.DefaultIconProvider;
import java.util.HashMap;
import java.util.regex.Pattern;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Schema Vertex Type Test.
 *
 * @author arcturus
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class SchemaVertexTypeNGTest extends ConstellationTest {

    public SchemaVertexTypeNGTest() {
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

//    /**
//     * Test of getForegroundIcon method, of class SchemaVertexType.
//     */
//    @Test
//    public void testGetForegroundIcon() {
//        System.out.println("getForegroundIcon");
//        SchemaVertexType instance = null;
//        ConstellationIcon expResult = null;
//        ConstellationIcon result = instance.getForegroundIcon();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getBackgroundIcon method, of class SchemaVertexType.
//     */
//    @Test
//    public void testGetBackgroundIcon() {
//        System.out.println("getBackgroundIcon");
//        SchemaVertexType instance = null;
//        ConstellationIcon expResult = null;
//        ConstellationIcon result = instance.getBackgroundIcon();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getDetectionRegex method, of class SchemaVertexType.
//     */
//    @Test
//    public void testGetDetectionRegex() {
//        System.out.println("getDetectionRegex");
//        SchemaVertexType instance = null;
//        Pattern expResult = null;
//        Pattern result = instance.getDetectionRegex();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getValidationRegex method, of class SchemaVertexType.
//     */
//    @Test
//    public void testGetValidationRegex() {
//        System.out.println("getValidationRegex");
//        SchemaVertexType instance = null;
//        Pattern expResult = null;
//        Pattern result = instance.getValidationRegex();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getOverridenType method, of class SchemaVertexType.
//     */
//    @Test
//    public void testGetOverridenType() {
//        System.out.println("getOverridenType");
//        SchemaVertexType instance = null;
//        SchemaVertexType expResult = null;
//        SchemaVertexType result = instance.getOverridenType();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//    /**
//     * Test of buildHierarchy method, of class SchemaVertexType.
//     */
//    @Test
//    public void testBuildHierarchy() {
//        System.out.println("buildHierarchy");
//        SchemaVertexType instance = null;
//        instance.buildHierarchy();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//    /**
//     * Test of hashCode method, of class SchemaVertexType.
//     */
//    @Test
//    public void testHashCode() {
//        System.out.println("hashCode");
//        SchemaVertexType instance = null;
//        int expResult = 0;
//        int result = instance.hashCode();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//    /**
//     * Test of equals method, of class SchemaVertexType.
//     */
//    @Test
//    public void testEquals() {
//        System.out.println("equals");
//        Object obj = null;
//        SchemaVertexType instance = null;
//        boolean expResult = false;
//        boolean result = instance.equals(obj);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of compareTo method, of class SchemaVertexType.
//     */
//    @Test
//    public void testCompareTo() {
//        System.out.println("compareTo");
//        SchemaVertexType type = null;
//        SchemaVertexType instance = null;
//        int expResult = 0;
//        int result = instance.compareTo(type);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of unknownType method, of class SchemaVertexType.
//     */
//    @Test
//    public void testUnknownType() {
//        System.out.println("unknownType");
//        SchemaVertexType expResult = null;
//        SchemaVertexType result = SchemaVertexType.unknownType();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getUnknownType method, of class SchemaVertexType.
//     */
//    @Test
//    public void testGetUnknownType() {
//        System.out.println("getUnknownType");
//        SchemaVertexType instance = null;
//        SchemaVertexType expResult = null;
//        SchemaVertexType result = instance.getUnknownType();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of copy method, of class SchemaVertexType.
//     */
//    @Test
//    public void testCopy() {
//        System.out.println("copy");
//        SchemaVertexType instance = null;
//        SchemaVertexType expResult = null;
//        SchemaVertexType result = instance.copy();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of rename method, of class SchemaVertexType.
//     */
//    @Test
//    public void testRename() {
//        System.out.println("rename");
//        String name = "";
//        SchemaVertexType instance = null;
//        SchemaVertexType expResult = null;
//        SchemaVertexType result = instance.rename(name);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    @Test
    public void testBuildTypeObjectWithNothingSet() {
        final SchemaVertexType type = new SchemaVertexType.Builder(null).build();

        Assert.assertEquals(type.name, null);
        Assert.assertEquals(type.getName(), null);
        Assert.assertEquals(type.description, null);
        Assert.assertEquals(type.getDescription(), null);
        Assert.assertEquals(type.color, ConstellationColor.GREY);
        Assert.assertEquals(type.getColor(), ConstellationColor.GREY);
        Assert.assertEquals(type.superType, type);
        Assert.assertEquals(type.getSuperType(), type);
        Assert.assertEquals(type.properties, new HashMap<>());
        Assert.assertEquals(type.getProperties(), new HashMap<>());
        Assert.assertEquals(type.incomplete, false);
        Assert.assertEquals(type.isIncomplete(), false);
        Assert.assertEquals(type.getForegroundIcon(), DefaultIconProvider.UNKNOWN);
        Assert.assertEquals(type.getBackgroundIcon(), DefaultIconProvider.FLAT_SQUARE);
        Assert.assertEquals(type.getDetectionRegex(), null);
        Assert.assertEquals(type.getValidationRegex(), null);
        Assert.assertEquals(type.overridenType, null);
        Assert.assertEquals(type.getOverridenType(), null);
    }

    @Test
    public void testBuildTypeObjectWithAttributesSet() {
        final Pattern detectRegex = Pattern.compile("\\+?([0-9]{8,13})", Pattern.CASE_INSENSITIVE);
        final Pattern validationRegex = Pattern.compile("\\+?([0-9]{8,15})", Pattern.CASE_INSENSITIVE);

        final SchemaVertexType type = new SchemaVertexType.Builder("name")
                .setDescription("description")
                .setColor(ConstellationColor.GREEN)
                .setForegroundIcon(CharacterIconProvider.CHAR_0020)
                .setBackgroundIcon(DefaultIconProvider.FLAT_CIRCLE)
                .setDetectionRegex(detectRegex)
                .setValidationRegex(validationRegex)
                .setProperty("my key", "my value")
                .setIncomplete(true)
                .build();

        final HashMap<Object, Object> properties = new HashMap<>();
        properties.put("my key", "my value");

        Assert.assertEquals(type.name, "name");
        Assert.assertEquals(type.getName(), "name");
        Assert.assertEquals(type.description, "description");
        Assert.assertEquals(type.getDescription(), "description");
        Assert.assertEquals(type.color, ConstellationColor.GREEN);
        Assert.assertEquals(type.getColor(), ConstellationColor.GREEN);
        Assert.assertEquals(type.superType, type);
        Assert.assertEquals(type.getSuperType(), type);
        Assert.assertEquals(type.properties, properties);
        Assert.assertEquals(type.getProperties(), properties);
        Assert.assertEquals(type.incomplete, true);
        Assert.assertEquals(type.isIncomplete(), true);
        Assert.assertEquals(type.getForegroundIcon(), CharacterIconProvider.CHAR_0020);
        Assert.assertEquals(type.getBackgroundIcon(), DefaultIconProvider.FLAT_CIRCLE);
        Assert.assertEquals(type.getDetectionRegex(), detectRegex);
        Assert.assertEquals(type.getValidationRegex(), validationRegex);
        Assert.assertEquals(type.overridenType, null);
        Assert.assertEquals(type.getOverridenType(), null);
    }

    @Test
    public void testBuildTypeObjectWithTypeHavingParent() {
        final Pattern detectRegex = Pattern.compile("\\+?([0-9]{8,13})", Pattern.CASE_INSENSITIVE);
        final Pattern validationRegex = Pattern.compile("\\+?([0-9]{8,15})", Pattern.CASE_INSENSITIVE);

        final SchemaVertexType topLevel = new SchemaVertexType.Builder("top level")
                .build();

        final SchemaVertexType parent = new SchemaVertexType.Builder("parent")
                .setDescription("description")
                .setColor(ConstellationColor.GREEN)
                .setForegroundIcon(CharacterIconProvider.CHAR_0020)
                .setBackgroundIcon(DefaultIconProvider.FLAT_CIRCLE)
                .setDetectionRegex(detectRegex)
                .setValidationRegex(validationRegex)
                .setProperty("my key", "my value")
                .setSuperType(topLevel)
                .setIncomplete(true)
                .build();

        final SchemaVertexType child = new SchemaVertexType.Builder("child")
                .setSuperType(parent)
                .build();

        Assert.assertEquals(child.name, "child");
        Assert.assertEquals(child.getName(), "child");
        Assert.assertEquals(child.description, null);
        Assert.assertEquals(child.getDescription(), null);
        Assert.assertEquals(child.color, ConstellationColor.GREEN);
        Assert.assertEquals(child.getColor(), ConstellationColor.GREEN);
        Assert.assertEquals(child.superType, parent);
        Assert.assertEquals(child.getSuperType(), parent);
        Assert.assertEquals(child.properties, new HashMap<>()); // not inherited as expected ??
        Assert.assertEquals(child.getProperties(), new HashMap<>()); // not inherited as expected ??
        Assert.assertEquals(child.incomplete, false); // not inherited as expected
        Assert.assertEquals(child.isIncomplete(), false); // not inherited as expected
        Assert.assertEquals(child.getForegroundIcon(), CharacterIconProvider.CHAR_0020);
        Assert.assertEquals(child.getBackgroundIcon(), DefaultIconProvider.FLAT_CIRCLE);
        Assert.assertEquals(child.getDetectionRegex(), null); // not inherited as expected
        Assert.assertEquals(child.getValidationRegex(), null); // not inherited as expected
        Assert.assertEquals(child.overridenType, null);
        Assert.assertEquals(child.getOverridenType(), null);
    }

}
