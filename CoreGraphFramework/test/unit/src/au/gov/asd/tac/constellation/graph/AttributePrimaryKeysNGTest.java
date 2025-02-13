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
package au.gov.asd.tac.constellation.graph;

import au.gov.asd.tac.constellation.graph.attribute.IntegerAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.BareSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Attribute Primary Keys Test.
 *
 * @author algol
 */
public class AttributePrimaryKeysNGTest {

    private int attr1;
    private int attr2;
    private int attr3;
    private int attr4;
    private int attr5;
    private int attr6;
    private GraphWriteMethods graph;
    
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
        graph = new StoreGraph(SchemaFactoryUtilities.getSchemaFactory(BareSchemaFactory.NAME).createSchema());
        attr1 = graph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "field1", null, null, null);
        attr2 = graph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "field2", null, null, null);
        attr3 = graph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "field3", null, null, null);
        attr4 = graph.addAttribute(GraphElementType.VERTEX, IntegerAttributeDescription.ATTRIBUTE_NAME, "field4", null, null, null);
        attr5 = graph.addAttribute(GraphElementType.VERTEX, IntegerAttributeDescription.ATTRIBUTE_NAME, "field5", null, null, null);
        attr6 = graph.addAttribute(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "field6", null, null, null);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        graph = null;
    }

    @Test
    public void createKeysOnEmptyGraph() {
        int[] keys = new int[1];
        keys[0] = attr1;
        graph.setPrimaryKey(GraphElementType.VERTEX, keys);

        int[] value = graph.getPrimaryKey(GraphElementType.VERTEX);
        assertEquals("keys size same", keys.length, value.length);

        assertEquals("keys value same", keys[0], value[0]);
    }

    @Test
    public void resetKeysOnEmptyGraph() {
        int[] keys = new int[1];
        keys[0] = attr1;
        graph.setPrimaryKey(GraphElementType.VERTEX, keys);

        int[] value = graph.getPrimaryKey(GraphElementType.VERTEX);
        assertEquals("keys size same", keys.length, value.length);

        assertEquals("key value same", keys[0], value[0]);

        int[] keys2 = new int[2];
        keys2[0] = attr2;
        keys2[1] = attr3;
        graph.setPrimaryKey(GraphElementType.VERTEX, keys2);

        int[] value2 = graph.getPrimaryKey(GraphElementType.VERTEX);
        assertEquals("keys size same", keys2.length, value2.length);

        assertEquals("key 1 value same", keys2[0], value2[0]);
        assertEquals("key 2 value same", keys2[1], value2[1]);
    }

    @Test
    public void clearKeysOnEmptyGraph() {
        int[] keys = new int[1];
        keys[0] = attr5;
        graph.setPrimaryKey(GraphElementType.VERTEX, keys);

        int[] value = graph.getPrimaryKey(GraphElementType.VERTEX);
        assertEquals("keys size same", keys.length, value.length);

        assertEquals("key value same", keys[0], value[0]);

        int[] keys2 = new int[0];
        graph.setPrimaryKey(GraphElementType.VERTEX, keys2);

        int[] value2 = graph.getPrimaryKey(GraphElementType.VERTEX);
        assertEquals("keys size same", keys2.length, value2.length);
    }

    @Test
    public void createKeysOnPopulatedGraph() {
        generateDistinctNodes();

        int[] keys = new int[1];
        keys[0] = attr1;
        graph.setPrimaryKey(GraphElementType.VERTEX, keys);

        int[] value = graph.getPrimaryKey(GraphElementType.VERTEX);
        assertEquals("keys size same", keys.length, value.length);

        assertEquals("keys value same", keys[0], value[0]);

    }

    @Test
    public void createKeysOnPopulatedTransactionGraph() {
        generateDistinctTransactions();

        int[] keys = new int[1];
        keys[0] = attr6;
        graph.setPrimaryKey(GraphElementType.TRANSACTION, keys);

        int[] value = graph.getPrimaryKey(GraphElementType.TRANSACTION);
        assertEquals("keys size same", keys.length, value.length);

        assertEquals("keys value same", keys[0], value[0]);
    }

    @Test(expectedExceptions = DuplicateKeyException.class)
    public void createKeysFailure1OnPopulatedTransactionGraph() {
        generateNonDistinctTransactions1();

        int[] keys = new int[1];
        keys[0] = attr6;
        graph.setPrimaryKey(GraphElementType.TRANSACTION, keys);
        graph.validateKey(GraphElementType.TRANSACTION, false);
    }

    @Test(expectedExceptions = DuplicateKeyException.class)
    public void createKeysFailure2OnPopulatedTransactionGraph() {
        generateNonDistinctTransactions2();

        int[] keys = new int[1];
        keys[0] = attr6;
        graph.setPrimaryKey(GraphElementType.TRANSACTION, keys);
        graph.validateKey(GraphElementType.TRANSACTION, false);
    }

    @Test(expectedExceptions = DuplicateKeyException.class)
    public void createKeysFailure3OnPopulatedTransactionGraph() {
        generateNonDistinctTransactions3();

        int[] keys = new int[1];
        keys[0] = attr6;
        graph.setPrimaryKey(GraphElementType.TRANSACTION, keys);
        graph.validateKey(GraphElementType.TRANSACTION, false);

    }

    @Test(expectedExceptions = DuplicateKeyException.class)
    public void createStrKeysOnNonDistinctPopulatedGraph() {
        generateNonDistinctNodes();

        int[] keys = new int[1];
        keys[0] = attr2;
        graph.setPrimaryKey(GraphElementType.VERTEX, keys);

        graph.validateKey(GraphElementType.VERTEX, false);
    }

    @Test
    public void createKeysFailure1OnPopulatedTransactionGraphAndHandleKeyClash() {
        generateNonDistinctTransactions1();

        int[] keys = new int[1];
        keys[0] = attr6;
        graph.setPrimaryKey(GraphElementType.TRANSACTION, keys);

        graph.validateKey(GraphElementType.TRANSACTION, true);
    }

    @Test
    public void createKeysFailure2OnPopulatedTransactionGraphAndHandleKeyClash() {
        generateNonDistinctTransactions2();

        int[] keys = new int[1];
        keys[0] = attr6;
        graph.setPrimaryKey(GraphElementType.TRANSACTION, keys);

        graph.validateKey(GraphElementType.TRANSACTION, true);
    }

    @Test
    public void createKeysFailure3OnPopulatedTransactionGraphAndHandleKeyClash() {
        generateNonDistinctTransactions3();

        int[] keys = new int[1];
        keys[0] = attr6;
        graph.setPrimaryKey(GraphElementType.TRANSACTION, keys);

        graph.validateKey(GraphElementType.TRANSACTION, true);
    }

    @Test
    public void createStrKeysOnNonDistinctPopulatedGraphAndHandleKeyClash() {
        generateNonDistinctNodes();

        int[] keys = new int[1];
        keys[0] = attr2;
        graph.setPrimaryKey(GraphElementType.VERTEX, keys);

        graph.validateKey(GraphElementType.VERTEX, true);
    }

    @Test
    public void createIntKeysOnDistinctPopulatedGraph() {
        generateDistinctNodes();

        int[] keys = new int[1];
        keys[0] = attr5;
        graph.setPrimaryKey(GraphElementType.VERTEX, keys);

        int[] value = graph.getPrimaryKey(GraphElementType.VERTEX);
        assertEquals("keys size same", keys.length, value.length);

        assertEquals("keys value same", keys[0], value[0]);
    }

    @Test(expectedExceptions = DuplicateKeyException.class)
    public void createIntKeysOnNonDistinctPopulatedGraph() {
        generateNonDistinctNodes();

        int[] keys = new int[1];
        keys[0] = attr5;
        graph.setPrimaryKey(GraphElementType.VERTEX, keys);

        graph.validateKey(GraphElementType.VERTEX, false);
    }

    @Test
    public void createIntKeysOnNonDistinctPopulatedGraphAndHandleKeyClash() {
        generateNonDistinctNodes();

        int[] keys = new int[1];
        keys[0] = attr5;
        graph.setPrimaryKey(GraphElementType.VERTEX, keys);

        graph.validateKey(GraphElementType.VERTEX, true);
    }

    @Test
    public void resetKeysOnPopulatedGraph() {
        generateDistinctNodes();

        int[] keys = new int[1];
        keys[0] = attr1;
        graph.setPrimaryKey(GraphElementType.VERTEX, keys);

        int[] value = graph.getPrimaryKey(GraphElementType.VERTEX);
        assertEquals("keys size same", keys.length, value.length);

        assertEquals("key value same", keys[0], value[0]);

        int[] keys2 = new int[2];
        keys2[0] = attr2;
        keys2[1] = attr3;
        graph.setPrimaryKey(GraphElementType.VERTEX, keys2);

        int[] value2 = graph.getPrimaryKey(GraphElementType.VERTEX);
        assertEquals("keys size same", keys2.length, value2.length);

        assertEquals("key 1 value same", keys2[0], value2[0]);
        assertEquals("key 2 value same", keys2[1], value2[1]);
    }

    @Test
    public void clearKeysOnPopulatedGraph() {
        generateDistinctNodes();

        int[] keys = new int[1];
        keys[0] = attr1;
        graph.setPrimaryKey(GraphElementType.VERTEX, keys);

        int[] value = graph.getPrimaryKey(GraphElementType.VERTEX);
        assertEquals("keys size same", keys.length, value.length);

        assertEquals("key value same", keys[0], value[0]);

        int[] keys2 = new int[2];
        keys2[0] = attr2;
        keys2[1] = attr3;
        graph.setPrimaryKey(GraphElementType.VERTEX, keys2);

        int[] value2 = graph.getPrimaryKey(GraphElementType.VERTEX);
        assertEquals("keys size same", keys2.length, value2.length);

        assertEquals("key 1 value same", keys2[0], value2[0]);
        assertEquals("key 2 value same", keys2[1], value2[1]);

    }

    private void generateDistinctNodes() {
        int vxId1 = graph.addVertex();
        int vxId2 = graph.addVertex();
        int vxId3 = graph.addVertex();

        graph.setStringValue(attr1, vxId1, "a1");
        graph.setStringValue(attr2, vxId1, "b1");
        graph.setStringValue(attr3, vxId1, "c1");
        graph.setStringValue(attr4, vxId1, "10");
        graph.setStringValue(attr5, vxId1, "100");

        graph.setStringValue(attr1, vxId2, "a2");
        graph.setStringValue(attr2, vxId2, "b2");
        graph.setStringValue(attr3, vxId2, "c2");
        graph.setStringValue(attr4, vxId2, "20");
        graph.setStringValue(attr5, vxId2, "200");

        graph.setStringValue(attr1, vxId3, "a3");
        graph.setStringValue(attr2, vxId3, "b3");
        graph.setStringValue(attr3, vxId3, "c3");
        graph.setStringValue(attr4, vxId3, "30");
        graph.setStringValue(attr5, vxId3, "300");
    }

    private void generateNonDistinctNodes() {
        int vxId1 = graph.addVertex();
        int vxId2 = graph.addVertex();
        int vxId3 = graph.addVertex();

        graph.setStringValue(attr1, vxId1, "a1");
        graph.setStringValue(attr2, vxId1, "b1");
        graph.setStringValue(attr3, vxId1, "c1");
        graph.setStringValue(attr4, vxId1, "10");
        graph.setStringValue(attr5, vxId1, "100");

        graph.setStringValue(attr1, vxId2, "a2");
        graph.setStringValue(attr2, vxId2, "b1");
        graph.setStringValue(attr3, vxId2, "c2");
        graph.setStringValue(attr4, vxId2, "20");
        graph.setStringValue(attr5, vxId2, "200");

        graph.setStringValue(attr1, vxId3, "a3");
        graph.setStringValue(attr2, vxId3, "b3");
        graph.setStringValue(attr3, vxId3, "c3");
        graph.setStringValue(attr4, vxId3, "30");
        graph.setStringValue(attr5, vxId3, "100");
    }

    private void generateDistinctTransactions() {
        int vxId1 = graph.addVertex();
        int vxId2 = graph.addVertex();
        int vxId3 = graph.addVertex();

        graph.setStringValue(attr1, vxId1, "a1");
        graph.setStringValue(attr2, vxId1, "b1");
        graph.setStringValue(attr3, vxId1, "c1");
        graph.setStringValue(attr4, vxId1, "10");
        graph.setStringValue(attr5, vxId1, "100");

        graph.setStringValue(attr1, vxId2, "a2");
        graph.setStringValue(attr2, vxId2, "b1");
        graph.setStringValue(attr3, vxId2, "c2");
        graph.setStringValue(attr4, vxId2, "20");
        graph.setStringValue(attr5, vxId2, "200");

        graph.setStringValue(attr1, vxId3, "a3");
        graph.setStringValue(attr2, vxId3, "b3");
        graph.setStringValue(attr3, vxId3, "c3");
        graph.setStringValue(attr4, vxId3, "30");
        graph.setStringValue(attr5, vxId3, "100");

        graph.addTransaction(vxId1, vxId2, true);
        graph.addTransaction(vxId2, vxId1, true);
        graph.addTransaction(vxId1, vxId2, false);
        int txId4 = graph.addTransaction(vxId2, vxId3, true);
        int txId5 = graph.addTransaction(vxId2, vxId3, true);

        graph.setStringValue(attr6, txId4, "a1");
        graph.setStringValue(attr6, txId5, "a2");
    }

    /**
     * invalid data since there are 2 transactions for the same node pair with
     * the same attributes
     */
    private void generateNonDistinctTransactions1() {
        int vxId1 = graph.addVertex();
        int vxId2 = graph.addVertex();
        int vxId3 = graph.addVertex();

        graph.setStringValue(attr1, vxId1, "a1");
        graph.setStringValue(attr2, vxId1, "b1");
        graph.setStringValue(attr3, vxId1, "c1");
        graph.setStringValue(attr4, vxId1, "10");
        graph.setStringValue(attr5, vxId1, "100");

        graph.setStringValue(attr1, vxId2, "a2");
        graph.setStringValue(attr2, vxId2, "b1");
        graph.setStringValue(attr3, vxId2, "c2");
        graph.setStringValue(attr4, vxId2, "20");
        graph.setStringValue(attr5, vxId2, "200");

        graph.setStringValue(attr1, vxId3, "a3");
        graph.setStringValue(attr2, vxId3, "b3");
        graph.setStringValue(attr3, vxId3, "c3");
        graph.setStringValue(attr4, vxId3, "30");
        graph.setStringValue(attr5, vxId3, "100");

        graph.addTransaction(vxId1, vxId2, true);
        graph.addTransaction(vxId2, vxId1, true);
        graph.addTransaction(vxId1, vxId2, false);
        int txId4 = graph.addTransaction(vxId2, vxId3, true);
        int txId5 = graph.addTransaction(vxId2, vxId3, true);

        graph.setStringValue(attr6, txId4, "a1");
        graph.setStringValue(attr6, txId5, "a1");
    }

    /**
     * invalid data since there are 2 undirected transactions for the same node
     * pair (define in same directions)
     */
    private void generateNonDistinctTransactions2() {
        int vxId1 = graph.addVertex();
        int vxId2 = graph.addVertex();
        int vxId3 = graph.addVertex();

        graph.setStringValue(attr1, vxId1, "a1");
        graph.setStringValue(attr2, vxId1, "b1");
        graph.setStringValue(attr3, vxId1, "c1");
        graph.setStringValue(attr4, vxId1, "10");
        graph.setStringValue(attr5, vxId1, "100");

        graph.setStringValue(attr1, vxId2, "a2");
        graph.setStringValue(attr2, vxId2, "b1");
        graph.setStringValue(attr3, vxId2, "c2");
        graph.setStringValue(attr4, vxId2, "20");
        graph.setStringValue(attr5, vxId2, "200");

        graph.setStringValue(attr1, vxId3, "a3");
        graph.setStringValue(attr2, vxId3, "b3");
        graph.setStringValue(attr3, vxId3, "c3");
        graph.setStringValue(attr4, vxId3, "30");
        graph.setStringValue(attr5, vxId3, "100");

        graph.addTransaction(vxId1, vxId2, true);
        graph.addTransaction(vxId2, vxId1, true);
        graph.addTransaction(vxId1, vxId2, true);
        int txId4 = graph.addTransaction(vxId2, vxId3, false);
        int txId5 = graph.addTransaction(vxId2, vxId3, false);

        graph.setStringValue(attr6, txId4, "a1");
        graph.setStringValue(attr6, txId5, "a2");
    }

    /**
     * invalid data since there are 2 undirected transactions for the same node
     * pair (define in opposite directions)
     */
    private void generateNonDistinctTransactions3() {
        int vxId1 = graph.addVertex();
        int vxId2 = graph.addVertex();
        int vxId3 = graph.addVertex();

        graph.setStringValue(attr1, vxId1, "a1");
        graph.setStringValue(attr2, vxId1, "b1");
        graph.setStringValue(attr3, vxId1, "c1");
        graph.setStringValue(attr4, vxId1, "10");
        graph.setStringValue(attr5, vxId1, "100");

        graph.setStringValue(attr1, vxId2, "a2");
        graph.setStringValue(attr2, vxId2, "b1");
        graph.setStringValue(attr3, vxId2, "c2");
        graph.setStringValue(attr4, vxId2, "20");
        graph.setStringValue(attr5, vxId2, "200");

        graph.setStringValue(attr1, vxId3, "a3");
        graph.setStringValue(attr2, vxId3, "b3");
        graph.setStringValue(attr3, vxId3, "c3");
        graph.setStringValue(attr4, vxId3, "30");
        graph.setStringValue(attr5, vxId3, "100");

        graph.addTransaction(vxId1, vxId2, true);
        graph.addTransaction(vxId2, vxId1, false);
        graph.addTransaction(vxId1, vxId2, false);
        int txId4 = graph.addTransaction(vxId2, vxId3, true);
        int txId5 = graph.addTransaction(vxId2, vxId3, true);

        graph.setStringValue(attr6, txId4, "a1");
        graph.setStringValue(attr6, txId5, "a2");
    }
}
