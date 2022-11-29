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
package au.gov.asd.tac.constellation.graph.processing;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.attribute.BooleanAttributeDescription;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Graph RecordStore Utilities Test.
 *
 * @author algol
 */
public class GraphRecordStoreUtilitiesNGTest {

    private static final int NUMBER_OF_VERTICES = 6;
    private static final int NUMBER_OF_TRANSACTIONS = 2;

    private StoreGraph graph;
    private int[] vxs;
    private int[] txs;

    public GraphRecordStoreUtilitiesNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Create a graph with N_VX vertices and N_TX transactions.
     * <p>
     * Two pairs of vertices are connected by a transaction each, leaving two
     * singleton vertices.
     *
     * @throws Exception
     */
    @BeforeMethod
    public void setUpMethod() throws Exception {
        graph = new StoreGraph();
        graph.addAttribute(GraphElementType.VERTEX, BooleanAttributeDescription.ATTRIBUTE_NAME, "selected", "", false, null);
        graph.addAttribute(GraphElementType.TRANSACTION, BooleanAttributeDescription.ATTRIBUTE_NAME, "selected", "", false, null);

        vxs = new int[NUMBER_OF_VERTICES];
        for (int i = 0; i < vxs.length; i++) {
            vxs[i] = graph.addVertex();
        }

        txs = new int[NUMBER_OF_TRANSACTIONS];
        txs[0] = graph.addTransaction(vxs[2], vxs[3], true);
        txs[1] = graph.addTransaction(vxs[4], vxs[5], true);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        while (graph.getVertexCount() > 0) {
            graph.removeVertex(graph.getVertex(0));
        }

        vxs = null;
        txs = null;
    }

    @Test
    public void getAll() {
        final GraphRecordStore result = GraphRecordStoreUtilities.getAll(graph, false, false);
        assertEquals(result.size(), NUMBER_OF_VERTICES + NUMBER_OF_TRANSACTIONS);
    }

    @Test
    public void getAllSelectedVx() {
        final int vxSelected = graph.getAttribute(GraphElementType.VERTEX, "selected");
        graph.setBooleanValue(vxSelected, vxs[0], true);
        final GraphRecordStore result = GraphRecordStoreUtilities.getAll(graph, true, false);
        assertEquals(result.size(), 1);
    }

    @Test
    public void getAllSelectedTx() {
        final int txSelected = graph.getAttribute(GraphElementType.TRANSACTION, "selected");
        graph.setBooleanValue(txSelected, txs[0], true);
        final GraphRecordStore result = GraphRecordStoreUtilities.getAll(graph, true, false);
        assertEquals(result.size(), 1);
    }

    @Test
    public void getAllSelected() {
        final int vxSelected = graph.getAttribute(GraphElementType.VERTEX, "selected");
        final int txSelected = graph.getAttribute(GraphElementType.TRANSACTION, "selected");
        graph.setBooleanValue(vxSelected, vxs[0], true);
        graph.setBooleanValue(txSelected, txs[0], true);
        final GraphRecordStore result = GraphRecordStoreUtilities.getAll(graph, true, false);
        assertEquals(result.size(), 1 + 1);
    }

    @Test
    public void getAllSelectedSingletons() {
        final int vxSelected = graph.getAttribute(GraphElementType.VERTEX, "selected");
        final int txSelected = graph.getAttribute(GraphElementType.TRANSACTION, "selected");
        graph.setBooleanValue(vxSelected, vxs[0], true);
        graph.setBooleanValue(txSelected, txs[0], true);
        final GraphRecordStore result = GraphRecordStoreUtilities.getAll(graph, true, false);
        assertEquals(result.size(), 1 + 1);
    }

    @Test
    public void getAllSingletons() {
        final GraphRecordStore result = GraphRecordStoreUtilities.getAll(graph, true, false, false);
        assertEquals(result.size(), 4);
    }

    @Test
    public void getVertices() {
        final GraphRecordStore result = GraphRecordStoreUtilities.getVertices(graph, false, false, false);
        assertEquals(result.size(), NUMBER_OF_VERTICES);
    }

    @Test
    public void getVerticesWhichAreSingletons() {
        final GraphRecordStore result = GraphRecordStoreUtilities.getVertices(graph, true, false, false);
        assertEquals(result.size(), 2);
    }

    @Test
    public void getVerticesWhichAreSelectedAndSingletons() {
        final int vxSelected = graph.getAttribute(GraphElementType.VERTEX, "selected");
        final int txSelected = graph.getAttribute(GraphElementType.TRANSACTION, "selected");
        graph.setBooleanValue(vxSelected, vxs[0], true);
        graph.setBooleanValue(txSelected, txs[0], true); // adding a transaction and making sure it isn't counted

        final GraphRecordStore result = GraphRecordStoreUtilities.getVertices(graph, true, true, false);
        assertEquals(result.size(), 1);
    }

    @Test
    public void getVerticesWhichAreSelectedAndNotSingletons() {
        final int vxSelected = graph.getAttribute(GraphElementType.VERTEX, "selected");
        final int txSelected = graph.getAttribute(GraphElementType.TRANSACTION, "selected");
        graph.setBooleanValue(vxSelected, vxs[2], true);
        graph.setBooleanValue(txSelected, txs[0], true); // adding a transaction and making sure it isn't counted

        final GraphRecordStore result = GraphRecordStoreUtilities.getVertices(graph, false, true, false);
        assertEquals(result.size(), 1);
    }

    @Test
    public void getVerticesWhereSelectedWhenNothingIsSelected() {
        final GraphRecordStore result = GraphRecordStoreUtilities.getVertices(graph, false, true, false);
        assertEquals(result.size(), 0);
    }

    @Test
    public void getSelectedVerticesBatchesWhenNothingIsSelected() {
        final List<GraphRecordStore> batches = GraphRecordStoreUtilities.getSelectedVerticesBatches(graph, NUMBER_OF_VERTICES);
        assertEquals(batches.size(), 1);
        assertEquals(batches.get(0).size(), 0);
    }

    @Test
    public void getSelectedVerticesBatchesWhenSomeAreSelected() {
        final int vxSelected = graph.getAttribute(GraphElementType.VERTEX, "selected");
        final int txSelected = graph.getAttribute(GraphElementType.TRANSACTION, "selected");
        graph.setBooleanValue(vxSelected, vxs[0], true);
        graph.setBooleanValue(txSelected, txs[0], true); // adding a transaction and making sure it isn't counted

        final List<GraphRecordStore> batches = GraphRecordStoreUtilities.getSelectedVerticesBatches(graph, NUMBER_OF_VERTICES);
        assertEquals(batches.size(), 1);
        assertEquals(batches.get(0).size(), 1);
    }

    @Test
    public void getSelectedVerticesBatchesWhenOnlySingletonsAreSelected() {
        final int vxSelected = graph.getAttribute(GraphElementType.VERTEX, "selected");
        final int txSelected = graph.getAttribute(GraphElementType.TRANSACTION, "selected");
        graph.setBooleanValue(vxSelected, vxs[0], true);
        graph.setBooleanValue(vxSelected, vxs[1], true);
        graph.setBooleanValue(txSelected, txs[0], true); // adding a transaction and making sure it isn't counted

        final List<GraphRecordStore> batches = GraphRecordStoreUtilities.getSelectedVerticesBatches(graph, NUMBER_OF_VERTICES);
        assertEquals(batches.size(), 1);
        assertEquals(batches.get(0).size(), 2);
    }

    @Test
    public void getSelectedVerticesBatchesWhenNonSingletonsAreSelected() {
        final int vxSelected = graph.getAttribute(GraphElementType.VERTEX, "selected");
        final int txSelected = graph.getAttribute(GraphElementType.TRANSACTION, "selected");
        graph.setBooleanValue(vxSelected, vxs[2], true);
        graph.setBooleanValue(vxSelected, vxs[4], true);
        graph.setBooleanValue(txSelected, txs[0], true); // adding a transaction and making sure it isn't counted

        final List<GraphRecordStore> batches = GraphRecordStoreUtilities.getSelectedVerticesBatches(graph, NUMBER_OF_VERTICES);
        assertEquals(batches.size(), 1);
        assertEquals(batches.get(0).size(), 2);
    }

    @Test
    public void getTransactions() {
        final GraphRecordStore result = GraphRecordStoreUtilities.getTransactions(graph, false, false);
        assertEquals(result.size(), NUMBER_OF_TRANSACTIONS);
    }

    @Test
    public void getTransactionsWhereSelectedWhenNothingIsSelected() {
        final GraphRecordStore result = GraphRecordStoreUtilities.getTransactions(graph, true, false);
        assertEquals(result.size(), 0);
    }

    @Test
    public void addRecordStoreToGraphWithAnEmptyRecordStore() {
        final StoreGraph graph = new StoreGraph();
        final RecordStore recordStore = new GraphRecordStore();
        final boolean initializeWithSchema = false;
        final boolean completeWithSchema = false;
        final List<String> vertexIdAttributes = new ArrayList<>();
        final Map<String, Integer> vertexMap = new HashMap<>();
        final Map<String, Integer> transactionMap = new HashMap<>();

        final List<Integer> veritices = GraphRecordStoreUtilities.addRecordStoreToGraph(graph, recordStore, initializeWithSchema, completeWithSchema, vertexIdAttributes, vertexMap, transactionMap);
        assertEquals(veritices.size(), 0);
    }

    @Test
    public void addRecordStoreToGraphWithTransactionsInBothDirections() {
        final StoreGraph graph = new StoreGraph();
        final RecordStore recordStore = new GraphRecordStore();
        recordStore.add();
        recordStore.set(GraphRecordStoreUtilities.SOURCE + "Idenfitier", "vx0");
        recordStore.set(GraphRecordStoreUtilities.DESTINATION + "Idenfitier", "vx1");
        recordStore.add();
        recordStore.set(GraphRecordStoreUtilities.SOURCE + "Idenfitier", "vx1");
        recordStore.set(GraphRecordStoreUtilities.DESTINATION + "Idenfitier", "vx0");

        final boolean initializeWithSchema = false;
        final boolean completeWithSchema = false;
        final List<String> vertexIdAttributes = new ArrayList<>();
        final Map<String, Integer> vertexMap = new HashMap<>();
        final Map<String, Integer> transactionMap = new HashMap<>();

        final List<Integer> veritices = GraphRecordStoreUtilities.addRecordStoreToGraph(graph, recordStore, initializeWithSchema, completeWithSchema, vertexIdAttributes, vertexMap, transactionMap);
        assertEquals(2, veritices.size());
        assertEquals(2, graph.getVertexCount());
        assertEquals(2, graph.getTransactionCount());

        final int vx0 = graph.getVertex(0);
        final int vx1 = graph.getVertex(1);
        final int tx0 = graph.getTransaction(0);
        final int tx1 = graph.getTransaction(1);

        assertEquals(vx0, graph.getTransactionSourceVertex(tx0));
        assertEquals(vx1, graph.getTransactionSourceVertex(tx1));
    }

}
