/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.locking;

import au.gov.asd.tac.constellation.graph.DuplicateKeyException;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttributeMerger;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.attribute.BooleanAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.mergers.ConcatenatedSetGraphAttributeMerger;
import au.gov.asd.tac.constellation.graph.schema.BareSchemaFactory;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import org.testng.annotations.Test;

/**
 * Primary Key Unit Test.
 *
 * @author sirius
 */
public class PrimaryKeyUnitNGTest {

    /**
     * When a graph has no schema, it will also have no element merger which
     * will cause a DuplicateKeyException to be thrown when there are two
     * elements with the same primary key, even if validateKey is called with
     * allowMerging = true.
     */
    @Test(expectedExceptions = {DuplicateKeyException.class})
    public void noMergerTest() {
        final StoreGraph g = new StoreGraph();
        final int keyAttribute = g.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "key", null, null, null);
        g.setPrimaryKey(GraphElementType.VERTEX, keyAttribute);
        final int v1 = g.addVertex();
        final int v2 = g.addVertex();
        g.setStringValue(keyAttribute, v1, "DuplicateValue");
        g.setStringValue(keyAttribute, v2, "DuplicateValue");
        g.validateKey(GraphElementType.VERTEX, true);
    }

    /**
     * When a graph is created with a schema, the schema will provide the
     * element merger. The default behaviour of the schema is return the highest
     * priority element merger. This should allow merging to happen successfully
     * when two elements exist with the same primary key.
     */
    @Test
    public void defaultMergerTest() {
        final StoreGraph g = new StoreGraph(new BareSchemaFactory().createSchema());
        final int keyAttribute = g.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "key", null, null, null);
        g.setPrimaryKey(GraphElementType.VERTEX, keyAttribute);
        final int v1 = g.addVertex();
        final int v2 = g.addVertex();
        g.setStringValue(keyAttribute, v1, "DuplicateValue");
        g.setStringValue(keyAttribute, v2, "DuplicateValue");

        assertEquals(g.getVertexCount(), 2);

        g.validateKey(GraphElementType.VERTEX, true);

        assertEquals(g.getVertexCount(), 1);
    }

    /**
     * Tests that adding an attribute merger to an attribute has the desired
     * effect, and also that specifying no attribute merger results in the
     * expected default behaviour.
     */
    @Test
    public void attributeMergerTest() {
        final StoreGraph g = new StoreGraph(new BareSchemaFactory().createSchema());
        final int keyAttribute = g.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "key", null, null, null);
        final int defaultMergerAttribute = g.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "defaultMerger", null, null, GraphAttributeMerger.getDefault().getId());
        final int customMergerAttribute = g.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "customMerger", null, null, ConcatenatedSetGraphAttributeMerger.ID);
        final int noMergerAttribute = g.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "noMerger", null, null, null);

        g.setPrimaryKey(GraphElementType.VERTEX, keyAttribute);
        final int survivingVertex = g.addVertex();
        final int mergedVertex = g.addVertex();
        g.setStringValue(keyAttribute, survivingVertex, "DuplicateValue");
        g.setStringValue(keyAttribute, mergedVertex, "DuplicateValue");

        g.setStringValue(defaultMergerAttribute, survivingVertex, "survivingValue");
        g.setStringValue(defaultMergerAttribute, mergedVertex, "mergedValue");

        g.setStringValue(customMergerAttribute, survivingVertex, "survivingValue");
        g.setStringValue(customMergerAttribute, mergedVertex, "mergedValue");

        g.setStringValue(noMergerAttribute, survivingVertex, "survivingValue");
        g.setStringValue(noMergerAttribute, mergedVertex, "mergedValue");

        assertEquals(g.getVertexCount(), 2);

        g.validateKey(GraphElementType.VERTEX, true);

        assertEquals(g.getVertexCount(), 1);

        assertEquals(g.getStringValue(defaultMergerAttribute, survivingVertex), "mergedValue");
        assertEquals(g.getStringValue(customMergerAttribute, survivingVertex), "mergedValue,survivingValue");
        assertEquals(g.getStringValue(noMergerAttribute, survivingVertex), "mergedValue");
    }

    @Test
    public void primaryKeyUpdateTest() throws InterruptedException {
        Graph graph = new DualGraph(null);
        int a1 = 0;
        int a2 = 0;
        int v1 = 0;
        int v2 = 0;

        WritableGraph wg = graph.getWritableGraph("Primary Key Test", true);
        try {
            a1 = wg.addAttribute(GraphElementType.VERTEX, BooleanAttributeDescription.ATTRIBUTE_NAME, "a", null, null, null);
            a2 = wg.addAttribute(GraphElementType.VERTEX, BooleanAttributeDescription.ATTRIBUTE_NAME, "b", null, null, null);

            wg.setPrimaryKey(GraphElementType.VERTEX, a1, a2);

            v1 = wg.addVertex();
        } finally {
            wg.commit();
        }

        try {
            wg = graph.getWritableGraph("Primary Key Test", true);
            try {

                wg.setBooleanValue(a1, v1, true);

                v2 = wg.addVertex();
                wg.setBooleanValue(a1, v2, true);
            } finally {
                wg.commit();
            }
        } catch (DuplicateKeyException e) {
            // continue
        }

        try (final ReadableGraph rg = graph.getReadableGraph()) {
            assertFalse(rg.getBooleanValue(a1, v1));
        }
    }

    @Test
    public void rollbackTest() throws InterruptedException {
        Graph graph = new DualGraph(null);

        int vName;
        int tName;

        int source;
        int destination;

        int transaction1;
        int transaction2;
        int transaction3;

        WritableGraph wg = graph.getWritableGraph("Set Up Graph", true);
        try {
            vName = wg.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "name", "name", null, null);
            tName = wg.addAttribute(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "name", "name", null, null);

            wg.setPrimaryKey(GraphElementType.VERTEX, vName);
            wg.setPrimaryKey(GraphElementType.TRANSACTION, tName);

            source = wg.addVertex();
            wg.setStringValue(vName, source, "Source");

            destination = wg.addVertex();
            wg.setStringValue(vName, destination, "Destination");

            transaction1 = wg.addTransaction(source, destination, true);
            wg.setStringValue(tName, transaction1, "transaction");

        } finally {
            wg.commit();
        }

        try {
            wg = graph.getWritableGraph("Add Duplicate Transactions", true);
            try {
                transaction2 = wg.addTransaction(source, destination, true);
                wg.setStringValue(transaction2, tName, "transaction");

                transaction3 = wg.addTransaction(source, destination, true);
                wg.setStringValue(transaction3, tName, "transaction");

            } finally {
                wg.commit();
            }
        } catch (DuplicateKeyException ex) {
            // continue
        }

        wg = graph.getWritableGraph("Add Unique Transaction", true);
        try {
            transaction2 = wg.addTransaction(source, destination, true);
            wg.setStringValue(transaction2, tName, "transaction2");

        } finally {
            wg.commit();
        }

        try (final ReadableGraph rg = graph.getReadableGraph()) {
            assert rg.getTransactionCount() == 2;
        }
    }

    @Test
    public void nestedRollbackTest() throws InterruptedException {
        Graph graph = new DualGraph(null);

        int vName;
        int tName;

        int source;
        int destination;

        int transaction1;
        int transaction2;
        int transaction3;

        WritableGraph wg = graph.getWritableGraph("Set Up Graph", true);
        try {
            vName = wg.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "name", "name", null, null);
            tName = wg.addAttribute(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "name", "name", null, null);

            wg.setPrimaryKey(GraphElementType.VERTEX, vName);
            wg.setPrimaryKey(GraphElementType.TRANSACTION, tName);

            source = wg.addVertex();
            wg.setStringValue(vName, source, "Source");

            destination = wg.addVertex();
            wg.setStringValue(vName, destination, "Destination");

            transaction1 = wg.addTransaction(source, destination, true);
            wg.setStringValue(tName, transaction1, "transaction");

        } finally {
            wg.commit();
        }

        try {
            wg = graph.getWritableGraph("Add Duplicate Transactions", true);
            try {
                transaction2 = wg.addTransaction(source, destination, true);
                wg.setStringValue(tName, transaction2, "transaction");

                transaction3 = wg.addTransaction(source, destination, true);
                wg.setStringValue(tName, transaction3, "transaction");

                WritableGraph wg2 = graph.getWritableGraph("Not doing anything wrong", true);
                try {

                } finally {
                    wg2.commit();
                }

            } finally {
                wg.commit();
            }
        } catch (DuplicateKeyException ex) {
            // continue
        }

        wg = graph.getWritableGraph("Add Unique Transaction", true);
        try {
            transaction2 = wg.addTransaction(source, destination, true);
            wg.setStringValue(tName, transaction2, "transaction2");

        } finally {
            wg.commit();
        }

        try (final ReadableGraph rg = graph.getReadableGraph()) {
            assert rg.getTransactionCount() == 2;
        }
    }
}
