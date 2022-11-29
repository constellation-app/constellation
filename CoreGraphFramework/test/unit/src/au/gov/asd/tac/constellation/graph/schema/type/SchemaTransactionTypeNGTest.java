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
import java.util.HashMap;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Schema Transaction Type Test.
 *
 * @author arcturus
 */
public class SchemaTransactionTypeNGTest {

    public SchemaTransactionTypeNGTest() {
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
//     * Test of unknownType method, of class SchemaTransactionType.
//     */
//    @Test
//    public void testUnknownType() {
//        System.out.println("unknownType");
//        SchemaTransactionType expResult = null;
//        SchemaTransactionType result = SchemaTransactionType.unknownType();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getStyle method, of class SchemaTransactionType.
//     */
//    @Test
//    public void testGetStyle() {
//        System.out.println("getStyle");
//        SchemaTransactionType instance = null;
//        LineStyle expResult = null;
//        LineStyle result = instance.getStyle();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isDirected method, of class SchemaTransactionType.
//     */
//    @Test
//    public void testIsDirected() {
//        System.out.println("isDirected");
//        SchemaTransactionType instance = null;
//        Boolean expResult = null;
//        Boolean result = instance.isDirected();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getOverridenType method, of class SchemaTransactionType.
//     */
//    @Test
//    public void testGetOverridenType() {
//        System.out.println("getOverridenType");
//        SchemaTransactionType instance = null;
//        SchemaTransactionType expResult = null;
//        SchemaTransactionType result = instance.getOverridenType();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of buildHierarchy method, of class SchemaTransactionType.
//     */
//    @Test
//    public void testBuildHierarchy() {
//        System.out.println("buildHierarchy");
//        SchemaTransactionType instance = null;
//        instance.buildHierarchy();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of hashCode method, of class SchemaTransactionType.
//     */
//    @Test
//    public void testHashCode() {
//        System.out.println("hashCode");
//        SchemaTransactionType instance = null;
//        int expResult = 0;
//        int result = instance.hashCode();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of equals method, of class SchemaTransactionType.
//     */
//    @Test
//    public void testEquals() {
//        System.out.println("equals");
//        Object obj = null;
//        SchemaTransactionType instance = null;
//        boolean expResult = false;
//        boolean result = instance.equals(obj);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of compareTo method, of class SchemaTransactionType.
//     */
//    @Test
//    public void testCompareTo() {
//        System.out.println("compareTo");
//        SchemaTransactionType type = null;
//        SchemaTransactionType instance = null;
//        int expResult = 0;
//        int result = instance.compareTo(type);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getUnknownType method, of class SchemaTransactionType.
//     */
//    @Test
//    public void testGetUnknownType() {
//        System.out.println("getUnknownType");
//        SchemaTransactionType instance = null;
//        SchemaTransactionType expResult = null;
//        SchemaTransactionType result = instance.getUnknownType();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of copy method, of class SchemaTransactionType.
//     */
//    @Test
//    public void testCopy() {
//        System.out.println("copy");
//        SchemaTransactionType instance = null;
//        SchemaTransactionType expResult = null;
//        SchemaTransactionType result = instance.copy();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of rename method, of class SchemaTransactionType.
//     */
//    @Test
//    public void testRename() {
//        System.out.println("rename");
//        String name = "";
//        SchemaTransactionType instance = null;
//        SchemaTransactionType expResult = null;
//        SchemaTransactionType result = instance.rename(name);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    @Test
    public void testBuildTypeObjectWithNothingSet() {
        final SchemaTransactionType type = new SchemaTransactionType.Builder(null).build();

        Assert.assertEquals(type.name, null);
        Assert.assertEquals(type.description, null);
        Assert.assertEquals(type.color, ConstellationColor.CLOUDS);
        Assert.assertEquals(type.properties, new HashMap<>());
        Assert.assertEquals(type.superType, type);
    }
}
