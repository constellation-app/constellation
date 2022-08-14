/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.layers.query;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for BitMaskQueryCollection
 *
 * @author Delphinus8821
 */
public class BitMaskQueryCollectionNGTest {

    public BitMaskQueryCollectionNGTest() {
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
     * Test of setQuery method, of class BitMaskQueryCollection.
     */
    @Test
    public void testSetQuery() {
        final String query1 = "Type == 'Event'";
        final int bitMaskIndex1 = 2;
        final BitMaskQueryCollection instance = new BitMaskQueryCollection(GraphElementType.VERTEX);
        instance.setQuery(query1, bitMaskIndex1);

        final String query2 = "Type == 'Word'";
        final int bitMaskIndex2 = 48;
        instance.setQuery(query2, bitMaskIndex2);

        assertEquals(instance.getQuery(bitMaskIndex1).getQuery().getQueryString(), query1);
        assertEquals(instance.getQuery(bitMaskIndex2).getQuery().getQueryString(), query2);
    }

    /**
     * Test of setActiveQueries method, of class BitMaskQueryCollection.
     */
    @Test
    public void testSetActiveQueries() {
        // check that there are no active queries by default
        final BitMaskQueryCollection instance = new BitMaskQueryCollection(GraphElementType.VERTEX);
        assertEquals(instance.getActiveQueries().size(), 0);

        // check with no active queries
        final long activeQueriesBitMask = 0L;
        instance.setDefaultQueries();
        instance.setActiveQueries(activeQueriesBitMask);
        assertEquals(instance.getActiveQueries().size(), 1);

        // add some active queries and recheck
        final BitMaskQuery query1 = new BitMaskQuery(new Query(GraphElementType.VERTEX, "Type == 'Event"), 2, "Event");
        query1.setVisibility(true);
        instance.add(query1);
        final BitMaskQuery query2 = new BitMaskQuery(new Query(GraphElementType.VERTEX, "Type == 'Word'"), 48, "Word");
        query2.setVisibility(true);
        instance.add(query2);

        instance.setActiveQueries(activeQueriesBitMask);
        assertEquals(instance.getActiveQueries().size(), 2);
    }

    /**
     * Test of updateBitMask method, of class BitMaskQueryCollection.
     */
    @Test
    public void testUpdateBitMask() {
        final long bitMask = 0L;
        final BitMaskQueryCollection instance = new BitMaskQueryCollection(GraphElementType.VERTEX);
        final long expResult = 0L;
        long result = instance.updateBitMask(bitMask);
        assertEquals(result, expResult);

        // test after adding some queries
        final long activeQueriesBitMask = 0L;
        instance.setDefaultQueries();
        // add some active queries and recheck
        final BitMaskQuery query1 = new BitMaskQuery(new Query(GraphElementType.VERTEX, "Type == 'Event"), 2, "Event");
        query1.setVisibility(true);
        instance.add(query1);
        final BitMaskQuery query2 = new BitMaskQuery(new Query(GraphElementType.VERTEX, "Type == 'Word'"), 48, "Word");
        query2.setVisibility(true);
        instance.add(query2);
        instance.setActiveQueries(activeQueriesBitMask);

        result = instance.updateBitMask(bitMask);
        assertEquals(result, expResult);
    }

    /**
     * Test of add method, of class BitMaskQueryCollection.
     */
    @Test
    public void testAdd() {
        final Query query = new Query(GraphElementType.VERTEX, "Type == 'Event'");
        final int bitIndex = 4;
        final String description = "Test query";
        final BitMaskQueryCollection instance = new BitMaskQueryCollection(GraphElementType.VERTEX);
        instance.add(query, bitIndex, description);

        assertEquals(instance.getQuery(bitIndex).getQuery(), query);
    }

    /**
     * Test of add method with an invalid query bit index
     */
    @Test(expectedExceptions = IndexOutOfBoundsException.class)
    public void testAddError() {
        // test adding an invalid query
        final Query query = new Query(GraphElementType.VERTEX, "Type == 'Event'");
        final int bitIndex = -1;
        final String description = "Test query";
        final BitMaskQueryCollection instance = new BitMaskQueryCollection(GraphElementType.VERTEX);
        instance.add(query, bitIndex, description);
    }

    /**
     * Test of removeQuery method, of class BitMaskQueryCollection.
     */
    @Test
    public void testRemoveQuery() {
        // add a query to remove
        final Query query = new Query(GraphElementType.VERTEX, "Type == 'Event'");
        final int bitIndex = 4;
        final String description = "Test query";
        final BitMaskQueryCollection instance = new BitMaskQueryCollection(GraphElementType.VERTEX);
        instance.add(query, bitIndex, description);

        // remove the query
        instance.removeQuery(bitIndex);
        assertEquals(instance.getQuery(bitIndex), null);
    }

    /**
     * Test of remove method with an invalid query bit index
     */
    @Test(expectedExceptions = IndexOutOfBoundsException.class)
    public void testRemoveError() {
        // test adding an invalid query
        final BitMaskQueryCollection instance = new BitMaskQueryCollection(GraphElementType.VERTEX);
        final int bitIndex = -1;
        instance.removeQuery(bitIndex);
    }

    /**
     * Test of removeQueryAndSort method, of class BitMaskQueryCollection.
     */
    @Test
    public void testRemoveQueryAndSort() {
        // add a query to remove
        final Query query = new Query(GraphElementType.VERTEX, "Type == 'Event'");
        final int bitIndex = 4;
        final String description = "Test query";
        final BitMaskQueryCollection instance = new BitMaskQueryCollection(GraphElementType.VERTEX);
        instance.add(query, bitIndex, description);

        // remove the query
        instance.removeQueryAndSort(bitIndex);
        assertEquals(instance.getQuery(bitIndex), null);
    }

}
