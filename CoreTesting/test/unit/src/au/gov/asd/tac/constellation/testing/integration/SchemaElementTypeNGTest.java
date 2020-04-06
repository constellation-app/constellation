/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.testing.integration;

import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaElementType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Schema Element Type Test.
 *
 * @author arcturus
 */
public class SchemaElementTypeNGTest {

    public SchemaElementTypeNGTest() {
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
     * Test of getColor method, of class SchemaElementType.
     */
    @Test
    public void testGetColor() {
        SchemaElementType instance = SchemaVertexType.unknownType();
        ConstellationColor expResult = ConstellationColor.GREY;
        ConstellationColor result = instance.getColor();
        assertEquals(result, expResult);
    }

    /**
     * Test of getSuperType method, of class SchemaElementType.
     */
    @Test
    public void testGetSuperType() {
        SchemaElementType instance = AnalyticConcept.VertexType.COUNTRY;
        SchemaElementType expResult = AnalyticConcept.VertexType.LOCATION;
        SchemaElementType result = instance.getSuperType();
        assertEquals(result, expResult);
    }

    /**
     * Test of isTopLevelType method, of class SchemaElementType.
     */
    @Test
    public void testIsTopLevelTypeWhenItIs() {
        SchemaElementType instance = AnalyticConcept.VertexType.LOCATION;
        boolean expResult = true;
        boolean result = instance.isTopLevelType();
        assertEquals(result, expResult);
    }

    @Test
    public void testIsTopLevelTypeWhenItIsNot() {
        SchemaElementType instance = AnalyticConcept.VertexType.COUNTRY;
        boolean expResult = false;
        boolean result = instance.isTopLevelType();
        assertEquals(result, expResult);
    }

    /**
     * Test of getTopLevelType method, of class SchemaElementType.
     */
    @Test
    public void testGetTopLevelTypeWhenCheckingTopLevelType() {
        SchemaElementType instance = AnalyticConcept.VertexType.LOCATION;
        SchemaElementType expResult = AnalyticConcept.VertexType.LOCATION;
        SchemaElementType result = instance.getTopLevelType();
        assertEquals(result, expResult);
    }

    @Test
    public void testGetTopLevelTypeWhenCheckingSubType() {
        SchemaElementType instance = AnalyticConcept.VertexType.COUNTRY;
        SchemaElementType expResult = AnalyticConcept.VertexType.LOCATION;
        SchemaElementType result = instance.getTopLevelType();
        assertEquals(result, expResult);
    }

    /**
     * Test of isSubTypeOf method, of class SchemaElementType.
     */
    @Test
    public void testIsSubTypeOfWhenValid() {
        SchemaElementType type = AnalyticConcept.VertexType.LOCATION;
        SchemaElementType instance = AnalyticConcept.VertexType.COUNTRY;
        boolean expResult = true;
        boolean result = instance.isSubTypeOf(type);
        assertEquals(result, expResult);
    }

    @Test
    public void testIsSubTypeOfWhenInalid() {
        SchemaElementType type = AnalyticConcept.VertexType.LOCATION;
        SchemaElementType instance = AnalyticConcept.VertexType.DOCUMENT;
        boolean expResult = false;
        boolean result = instance.isSubTypeOf(type);
        assertEquals(result, expResult);
    }

    /**
     * Test of getHierachy method, of class SchemaElementType.
     */
    @Test
    public void testGetHierachyWithOneLevel() {
        final SchemaElementType instance = AnalyticConcept.VertexType.LOCATION;
        String expResult = "Location";
        String result = instance.getHierachy();
        assertEquals(result, expResult);
    }

    @Test
    public void testGetHierachyWithTwoLevels() {
        final SchemaElementType instance = AnalyticConcept.VertexType.COUNTRY;
        String expResult = "Location.Country";
        String result = instance.getHierachy();
        assertEquals(result, expResult);
    }

    /**
     * Test of isIncomplete method, of class SchemaElementType.
     */
    @Test
    public void testIsIncomplete() {
        SchemaElementType instance = AnalyticConcept.VertexType.COUNTRY;
        boolean expResult = false;
        boolean result = instance.isIncomplete();
        assertEquals(result, expResult);
    }

    /**
     * Test of toString method, of class SchemaElementType.
     */
    @Test
    public void testToString() {
        final SchemaElementType instance = AnalyticConcept.VertexType.COUNTRY;
        String expResult = "Location.Country";
        String result = instance.toString();
        assertEquals(result, expResult);
    }

    /**
     * Test of getUnknownType method, of class SchemaElementType.
     */
    @Test
    public void testGetUnknownType() {
        SchemaElementType instance = SchemaVertexType.unknownType();
        SchemaElementType expResult = SchemaVertexType.unknownType();
        SchemaElementType result = instance;
        assertEquals(result, expResult);
    }

    /**
     * Test of copy method, of class SchemaElementType.
     */
    @Test
    public void testCopy() {
        SchemaElementType instance = SchemaVertexType.unknownType();
        SchemaElementType expResult = SchemaVertexType.unknownType();
        SchemaElementType result = instance.copy();
        assertEquals(result, expResult);
    }

    /**
     * Test of rename method, of class SchemaElementType.
     */
    @Test
    public void testRename() {
        String name = "foo";
        SchemaElementType instance = SchemaVertexType.unknownType();
        SchemaElementType expResult = new SchemaVertexType.Builder(SchemaVertexType.unknownType(), name).build();
        SchemaElementType result = instance.rename(name);
        assertEquals(result, expResult);
    }
//
//    public class SchemaElementTypeImpl extends SchemaElementType {
//
//        public SchemaElementTypeImpl() {
//            super("", "", null, null, null, null, false);
//        }
//
//        public T getUnknownType() {
//            return null;
//        }
//
//        public T copy() {
//            return null;
//        }
//
//        public T rename(String name) {
//            return null;
//        }
//    }

}
