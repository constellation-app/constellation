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
package au.gov.asd.tac.constellation.views.find.advanced;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.views.find.advanced.FindResult;
import static au.gov.asd.tac.constellation.views.find.advanced.FindResult.SEPARATOR;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Atlas139mkm
 */
public class FindResultNGTest {

    FindResult findResult;

    public FindResultNGTest() {
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
     * Test of setID method, of class FindResult.
     */
    @Test
    public void testSetID() {
        System.out.println("setID");
        findResult = new FindResult(1, 2, GraphElementType.VERTEX);

        findResult.setID(2);
        assertEquals(findResult.getID(), 2);
    }

    /**
     * Test of getUID method, of class FindResult.
     */
    @Test
    public void testGetUID() {
        System.out.println("getUID");
        findResult = new FindResult(1, 2, GraphElementType.VERTEX);

        assertEquals(findResult.getUID(), 2);
    }

    /**
     * Test of setUID method, of class FindResult.
     */
    @Test
    public void testSetUID() {
        System.out.println("setUID");
        findResult = new FindResult(1, 2, GraphElementType.VERTEX);

        findResult.setUID(3);
        assertEquals(findResult.getUID(), 3);
    }

    /**
     * Test of getType method, of class FindResult.
     */
    @Test
    public void testGetType() {
        System.out.println("getType");
        findResult = new FindResult(1, 2, GraphElementType.VERTEX);

        assertEquals(findResult.getType(), GraphElementType.VERTEX);
    }

    /**
     * Test of setType method, of class FindResult.
     */
    @Test
    public void testSetType() {
        System.out.println("setType");
        findResult = new FindResult(1, 2, GraphElementType.VERTEX);

        findResult.setType(GraphElementType.TRANSACTION);
        assertEquals(findResult.getType(), GraphElementType.TRANSACTION);
    }

    /**
     * Test of getAttributeName method, of class FindResult.
     */
    @Test
    public void testGetAttributeName() {
        System.out.println("getAttributeName");

        String objectValue = "value";
        findResult = new FindResult(1, 2, GraphElementType.VERTEX, "attribute", objectValue);

        assertEquals(findResult.getAttributeName(), "attribute");
    }

    /**
     * Test of setAttributeName method, of class FindResult.
     */
    @Test
    public void testSetAttributeName() {
        System.out.println("setAttributeName");

        String objectValue = "value";
        findResult = new FindResult(1, 2, GraphElementType.VERTEX, "attribute", objectValue);
        findResult.setAttributeName("test");

        assertEquals(findResult.getAttributeName(), "test");
    }

    /**
     * Test of getAttributeValue method, of class FindResult.
     */
    @Test
    public void testGetAttributeValue() {
        System.out.println("getAttributeValue");
        String objectValue = "value";
        String objectValueExpected = "value";
        findResult = new FindResult(1, 2, GraphElementType.VERTEX, "attribute", objectValue);

        assertEquals(findResult.getAttributeValue(), objectValueExpected);
    }

    /**
     * Test of setAttributeValue method, of class FindResult.
     */
    @Test
    public void testSetAttributeValue() {
        System.out.println("setAttributeValue");

        String objectValue = "value";
        String objectValueExpected = "valueExpected";
        findResult = new FindResult(1, 2, GraphElementType.VERTEX, "attribute", objectValue);
        findResult.setAttributeValue(objectValueExpected);

        assertEquals(findResult.getAttributeValue(), objectValueExpected);
    }

    /**
     * Test of toString method, of class FindResult.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        String objectValue = "value";
        findResult = new FindResult(1, 2, GraphElementType.VERTEX, "attribute", objectValue);

        String toStringExpected = objectValue.toString() + SEPARATOR + "attribute";

        assertEquals(findResult.toString(), toStringExpected);
    }

    /**
     * Test of equals method, of class FindResult.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");

        String objectValue = "value";
        findResult = new FindResult(1, 2, GraphElementType.VERTEX, "attribute", objectValue);
        FindResult findResultExpected = new FindResult(1, 2, GraphElementType.VERTEX, "attribute", objectValue);

        assertEquals(findResult, findResultExpected);
        assertEquals(findResult, findResult);

        findResultExpected = new FindResult(5, 2, GraphElementType.VERTEX, "attribute", objectValue);
        assertNotEquals(findResult, findResultExpected);
        assertNotEquals(findResult, objectValue);
    }

    /**
     * Test of hashCode method, of class FindResult.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        String objectValue = "value";
        findResult = new FindResult(1, 2, GraphElementType.VERTEX, "attribute", objectValue);
        FindResult findResult2 = new FindResult(1, 2, GraphElementType.VERTEX, "attribute", objectValue);

        assertEquals(findResult.hashCode(), findResult2.hashCode());
    }

}
