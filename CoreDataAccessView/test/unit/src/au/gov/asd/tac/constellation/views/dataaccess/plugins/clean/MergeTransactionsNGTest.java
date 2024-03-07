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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.clean;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.mergers.PrioritySurvivingGraphElementMerger;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.TemporalConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.utilities.io.SaveGraphUtilities;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPluginCoreType;
import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

/**
 * Merge Transactions Test.
 *
 * @author arcturus
 * @author antares
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class MergeTransactionsNGTest extends ConstellationTest {

    private int vertexIdentifierAttribute, vertexTypeAttribute, transactionDateTimeAttribute, transactionTypeAttribute, transactionIdentifierAttribute, transactionSelectedAttribute;
    private int vxId1, vxId2;
    private int txId1, txId2, txId3, txId4, txId5;
    private StoreGraph graph;

    private final boolean SAVE_GRAPH_FILES = false; // change this to true if you want to see the graph files

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // create an analytic graph
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        graph = new StoreGraph(schema);

        // add attributes
        vertexIdentifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        vertexTypeAttribute = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
        transactionIdentifierAttribute = VisualConcept.TransactionAttribute.IDENTIFIER.ensure(graph);
        transactionTypeAttribute = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);
        transactionDateTimeAttribute = TemporalConcept.TransactionAttribute.DATETIME.ensure(graph);
        transactionSelectedAttribute = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);

        // add vertices
        vxId1 = graph.addVertex();
        vxId2 = graph.addVertex();

        // set the identifier of each vertex to something unique
        graph.setStringValue(vertexIdentifierAttribute, vxId1, "V1");
        graph.setStringValue(vertexIdentifierAttribute, vxId2, "V2");

        // add transactions
        txId1 = graph.addTransaction(vxId1, vxId2, false);
        txId2 = graph.addTransaction(vxId1, vxId2, false);
        txId3 = graph.addTransaction(vxId1, vxId2, false);
        txId4 = graph.addTransaction(vxId1, vxId2, false);
        txId5 = graph.addTransaction(vxId1, vxId2, false);

        // set the same activity
        graph.setStringValue(transactionIdentifierAttribute, txId1, "");
        graph.setStringValue(transactionIdentifierAttribute, txId2, "");
        graph.setStringValue(transactionIdentifierAttribute, txId3, "");
        graph.setStringValue(transactionIdentifierAttribute, txId4, null);
        graph.setStringValue(transactionIdentifierAttribute, txId5, null);

        // set the same type
        graph.setStringValue(transactionTypeAttribute, txId1, "");
        graph.setStringValue(transactionTypeAttribute, txId2, "");
        graph.setStringValue(transactionTypeAttribute, txId3, "");
        graph.setStringValue(transactionTypeAttribute, txId4, AnalyticConcept.VertexType.EMAIL_ADDRESS.getName());
        graph.setStringValue(transactionTypeAttribute, txId5, AnalyticConcept.VertexType.COUNTRY.getName());

        // set the time of each transaction 1 second apart
        graph.setStringValue(transactionDateTimeAttribute, txId1, "2015-01-28 00:00:01.000 +00:00 [UTC]");
        graph.setStringValue(transactionDateTimeAttribute, txId2, "2015-01-28 00:00:02.000 +00:00 [UTC]");
        graph.setStringValue(transactionDateTimeAttribute, txId3, "2015-01-28 00:00:03.000 +00:00 [UTC]");
        graph.setStringValue(transactionDateTimeAttribute, txId4, "2015-01-28 00:00:02.000 +00:00 [UTC]");
        graph.setStringValue(transactionDateTimeAttribute, txId5, "2015-01-28 00:00:03.000 +00:00 [UTC]");

        // select all
        graph.setBooleanValue(transactionSelectedAttribute, txId1, true);
        graph.setBooleanValue(transactionSelectedAttribute, txId2, true);
        graph.setBooleanValue(transactionSelectedAttribute, txId3, true);
        graph.setBooleanValue(transactionSelectedAttribute, txId4, true);
        graph.setBooleanValue(transactionSelectedAttribute, txId5, true);

    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        graph = null;
    }

    /**
     * Test of getType method, of class MergeTransactionsPlugin.
     */
    @Test
    public void testGetType() {
        MergeTransactionsPlugin instance = new MergeTransactionsPlugin();
        String expResult = DataAccessPluginCoreType.CLEAN;
        String result = instance.getType();
        assertEquals(result, expResult);
    }

    @Test
    public void testCompare1() throws Exception {
        int result = MergeTransactionsPlugin.EARLIEST_TRANSACTION_CHOOSER.compare((long) 10, (long) 20);
        assertEquals(1, result);
    }

    @Test
    public void testCompare2() throws Exception {
        int result = MergeTransactionsPlugin.EARLIEST_TRANSACTION_CHOOSER.compare((long) 20, (long) 10);
        assertEquals(-1, result);
    }

    @Test
    public void testCompare3() throws Exception {
        int result = MergeTransactionsPlugin.EARLIEST_TRANSACTION_CHOOSER.compare((long) 10, (long) 10);
        assertEquals(0, result);
    }

    @Test
    public void testCompare4() throws Exception {
        int result = MergeTransactionsPlugin.LATEST_TRANSACTION_CHOOSER.compare((long) 20, (long) 10);
        assertEquals(1, result);
    }

    @Test
    public void testCompare5() throws Exception {
        int result = MergeTransactionsPlugin.LATEST_TRANSACTION_CHOOSER.compare((long) 10, (long) 20);
        assertEquals(-1, result);
    }

    @Test
    public void testCompare6() throws Exception {
        int result = MergeTransactionsPlugin.LATEST_TRANSACTION_CHOOSER.compare((long) 10, (long) 10);
        assertEquals(0, result);
    }

    private void saveGraphToFile(final String filename) throws InterruptedException, IOException {
        if (SAVE_GRAPH_FILES) {
            SaveGraphUtilities.saveGraphToTemporaryDirectory(graph, filename);
            System.out.println("Saved graph to " + System.getProperty("java.io.tmpdir") + filename);
        }
    }

    @Test
    public void testSortTransactions1() throws Exception {
        MergeTransactionType instance = new TestMergeTransactionType();
        Integer[] transactions = new Integer[3];
        transactions[0] = txId1;
        transactions[1] = txId2;
        transactions[2] = txId3;

        Integer[] originalTransactions = transactions.clone();

        instance.sortTransactions(transactions, graph, transactionTypeAttribute, transactionDateTimeAttribute, MergeTransactionsPlugin.LATEST_TRANSACTION_CHOOSER);

        assertArrayEquals(originalTransactions, transactions);
    }

    @Test
    public void testSortTransactions2() throws Exception {
        MergeTransactionType instance = new TestMergeTransactionType();

        Integer[] transactions = new Integer[3];
        transactions[0] = txId3;
        transactions[1] = txId2;
        transactions[2] = txId1;

        Integer[] expecting = new Integer[3];
        expecting[0] = txId1;
        expecting[1] = txId2;
        expecting[2] = txId3;

        instance.sortTransactions(transactions, graph, transactionTypeAttribute, transactionDateTimeAttribute, MergeTransactionsPlugin.LATEST_TRANSACTION_CHOOSER);

        assertArrayEquals(expecting, transactions);
    }

    @Test
    public void testSortTransactionsWithEarliest() throws Exception {
        MergeTransactionType instance = new TestMergeTransactionType();
        Integer[] transactions = new Integer[3];
        transactions[0] = txId3;
        transactions[1] = txId2;
        transactions[2] = txId1;

        Integer[] expecting = new Integer[3];
        expecting[0] = txId3;
        expecting[1] = txId2;
        expecting[2] = txId1;

        instance.sortTransactions(transactions, graph, transactionTypeAttribute, transactionDateTimeAttribute, MergeTransactionsPlugin.EARLIEST_TRANSACTION_CHOOSER);

        assertArrayEquals(expecting, transactions);
    }

    @Test
    public void testSortTransactionsWithEarliest2() throws Exception {
        MergeTransactionType instance = new TestMergeTransactionType();
        Integer[] transactions = new Integer[3];
        transactions[0] = txId1;
        transactions[1] = txId2;
        transactions[2] = txId3;

        Integer[] expecting = new Integer[3];
        expecting[0] = txId3;
        expecting[1] = txId2;
        expecting[2] = txId1;

        instance.sortTransactions(transactions, graph, transactionTypeAttribute, transactionDateTimeAttribute, MergeTransactionsPlugin.EARLIEST_TRANSACTION_CHOOSER);

        assertArrayEquals(expecting, transactions);
    }

    @Test
    public void testMergeTransaction1() throws Exception {
        MergeTransactionsPlugin instance = new MergeTransactionsPlugin();
        MergeTransactionsByDateTime mergeTypeInstance = new MergeTransactionsByDateTime();

        saveGraphToFile("testMergeTransaction1-before");

        // threshold to 30 seconds
        int threshold = 30;

        int mergedCount = 0;
        final Map<Integer, Set<Integer>> transactionsToMerge = mergeTypeInstance.getTransactionsToMerge(graph, MergeTransactionsPlugin.LATEST_TRANSACTION_CHOOSER, threshold, false);
        for (final Integer leadTransaction : transactionsToMerge.keySet()) {
            mergedCount += instance.mergeTransactions(graph, transactionsToMerge.get(leadTransaction), leadTransaction, new PrioritySurvivingGraphElementMerger());
        }

        saveGraphToFile("testMergeTransaction1-after");

        assertEquals(2, mergedCount);
    }

    @Test
    public void testMergeTransactionCheckingThreshold1() throws Exception {
        MergeTransactionsPlugin instance = new MergeTransactionsPlugin();
        MergeTransactionsByDateTime mergeTypeInstance = new MergeTransactionsByDateTime();

        // set the time of each transaction 1 minute apart
        graph.setStringValue(transactionDateTimeAttribute, txId1, "2015-01-28 00:01:00.000 +00:00 [UTC]");
        graph.setStringValue(transactionDateTimeAttribute, txId2, "2015-01-28 00:02:00.000 +00:00 [UTC]");
        graph.setStringValue(transactionDateTimeAttribute, txId3, "2015-01-28 00:03:00.000 +00:00 [UTC]");

        saveGraphToFile("testMergeTransactionCheckingThreshold1-before");

        // threshold to 5 seconds
        int threshold = 5;

        int mergedCount = 0;
        final Map<Integer, Set<Integer>> transactionsToMerge = mergeTypeInstance.getTransactionsToMerge(graph, MergeTransactionsPlugin.LATEST_TRANSACTION_CHOOSER, threshold, false);
        for (final Integer leadTransaction : transactionsToMerge.keySet()) {
            mergedCount += instance.mergeTransactions(graph, transactionsToMerge.get(leadTransaction), leadTransaction, new PrioritySurvivingGraphElementMerger());
        }

        saveGraphToFile("testMergeTransactionCheckingThreshold1-after");

        assertEquals(0, mergedCount);
    }

    @Test
    public void testMergeTransactionCheckingThreshold2() throws Exception {
        MergeTransactionsPlugin instance = new MergeTransactionsPlugin();
        MergeTransactionsByDateTime mergeTypeInstance = new MergeTransactionsByDateTime();

        // set the time of each transaction 1 minute apart
        graph.setStringValue(transactionDateTimeAttribute, txId1, "2015-01-28 00:01:00.000 +00:00 [UTC]");
        graph.setStringValue(transactionDateTimeAttribute, txId2, "2015-01-28 00:02:00.000 +00:00 [UTC]");
        graph.setStringValue(transactionDateTimeAttribute, txId3, "2015-01-28 00:02:05.000 +00:00 [UTC]");

        saveGraphToFile("testMergeTransactionCheckingThreshold2-before");

        // threshold to 5 seconds
        int threshold = 5;

        int mergedCount = 0;
        final Map<Integer, Set<Integer>> transactionsToMerge = mergeTypeInstance.getTransactionsToMerge(graph, MergeTransactionsPlugin.LATEST_TRANSACTION_CHOOSER, threshold, false);
        for (final Integer leadTransaction : transactionsToMerge.keySet()) {
            mergedCount += instance.mergeTransactions(graph, transactionsToMerge.get(leadTransaction), leadTransaction, new PrioritySurvivingGraphElementMerger());
        }

        saveGraphToFile("testMergeTransactionCheckingThreshold2-after");

        assertEquals(1, mergedCount);
    }

    @Test
    public void testMergeTransactionCheckingThreshold3() throws Exception {
        MergeTransactionsPlugin instance = new MergeTransactionsPlugin();
        MergeTransactionsByDateTime mergeTypeInstance = new MergeTransactionsByDateTime();

        // set the time of each transaction 1 minute apart
        graph.setStringValue(transactionDateTimeAttribute, txId1, "2015-01-28 00:01:00.000 +00:00 [UTC]");
        graph.setStringValue(transactionDateTimeAttribute, txId2, "2015-01-28 00:02:00.000 +00:00 [UTC]");
        graph.setStringValue(transactionDateTimeAttribute, txId3, "2015-01-28 00:03:00.000 +00:00 [UTC]");

        saveGraphToFile("testMergeTransactionCheckingThreshold3-before");

        // threshold to 30 seconds
        int threshold = 30;

        int mergedCount = 0;
        final Map<Integer, Set<Integer>> transactionsToMerge = mergeTypeInstance.getTransactionsToMerge(graph, MergeTransactionsPlugin.LATEST_TRANSACTION_CHOOSER, threshold, false);
        for (final Integer leadTransaction : transactionsToMerge.keySet()) {
            mergedCount += instance.mergeTransactions(graph, transactionsToMerge.get(leadTransaction), leadTransaction, new PrioritySurvivingGraphElementMerger());
        }

        saveGraphToFile("testMergeTransactionCheckingThreshold3-after");

        assertEquals(0, mergedCount);
    }

    @Test
    public void testMergeTransactionCheckingThreshold4() throws Exception {
        MergeTransactionsPlugin instance = new MergeTransactionsPlugin();
        MergeTransactionsByDateTime mergeTypeInstance = new MergeTransactionsByDateTime();

        // set the time of each transaction 1 minute apart
        graph.setStringValue(transactionDateTimeAttribute, txId1, "2015-01-28 00:01:00.000 +00:00 [UTC]");
        graph.setStringValue(transactionDateTimeAttribute, txId2, "2015-01-28 00:02:00.000 +00:00 [UTC]");
        graph.setStringValue(transactionDateTimeAttribute, txId3, "2015-01-28 00:03:00.000 +00:00 [UTC]");

        saveGraphToFile("testMergeTransactionCheckingThreshold4-before");

        // threshold to 30 seconds
        int threshold = 30;

        int mergedCount = 0;
        final Map<Integer, Set<Integer>> transactionsToMerge = mergeTypeInstance.getTransactionsToMerge(graph, MergeTransactionsPlugin.LATEST_TRANSACTION_CHOOSER, threshold, false);
        for (final Integer leadTransaction : transactionsToMerge.keySet()) {
            mergedCount += instance.mergeTransactions(graph, transactionsToMerge.get(leadTransaction), leadTransaction, new PrioritySurvivingGraphElementMerger());
        }

        saveGraphToFile("testMergeTransactionCheckingThreshold4-after");

        assertEquals(0, mergedCount);
    }

    @Test
    public void testMergeTransactionCheckingThreshold5() throws Exception {
        MergeTransactionsPlugin instance = new MergeTransactionsPlugin();
        MergeTransactionsByDateTime mergeTypeInstance = new MergeTransactionsByDateTime();

        saveGraphToFile("testMergeTransactionCheckingThreshold5-before");

        // threshold to 30 seconds
        int threshold = 30;

        int mergedCount = 0;
        final Map<Integer, Set<Integer>> transactionsToMerge = mergeTypeInstance.getTransactionsToMerge(graph, MergeTransactionsPlugin.LATEST_TRANSACTION_CHOOSER, threshold, false);
        for (final Integer leadTransaction : transactionsToMerge.keySet()) {
            mergedCount += instance.mergeTransactions(graph, transactionsToMerge.get(leadTransaction), leadTransaction, new PrioritySurvivingGraphElementMerger());
        }

        saveGraphToFile("testMergeTransactionCheckingThreshold5-after");

        assertEquals(2, mergedCount);
    }

    @Test
    public void testMergeTransactionWithNoCommonActivities() throws Exception {
        MergeTransactionsPlugin instance = new MergeTransactionsPlugin();
        MergeTransactionsByDateTime mergeTypeInstance = new MergeTransactionsByDateTime();

        // overide the scenario and set the same activity
        graph.setStringValue(transactionIdentifierAttribute, txId1, "an activity");
        graph.setStringValue(transactionIdentifierAttribute, txId2, "a different activity");
        graph.setStringValue(transactionIdentifierAttribute, txId3, "yet another activity");

        saveGraphToFile("testMergeTransactionWithNoCommonActivities-before");

        // threshold to 30 seconds
        int threshold = 30;

        int mergedCount = 0;
        final Map<Integer, Set<Integer>> transactionsToMerge = mergeTypeInstance.getTransactionsToMerge(graph, MergeTransactionsPlugin.LATEST_TRANSACTION_CHOOSER, threshold, false);
        for (final Integer leadTransaction : transactionsToMerge.keySet()) {
            mergedCount += instance.mergeTransactions(graph, transactionsToMerge.get(leadTransaction), leadTransaction, new PrioritySurvivingGraphElementMerger());
        }

        saveGraphToFile("testMergeTransactionWithNoCommonActivities-after");

        assertEquals(2, mergedCount);
    }

    @Test
    public void testMergeTransactionWithNoCommonTypes() throws Exception {
        MergeTransactionsPlugin instance = new MergeTransactionsPlugin();
        MergeTransactionsByDateTime mergeTypeInstance = new MergeTransactionsByDateTime();

        // overide the scenario and set the same type
        graph.setStringValue(transactionTypeAttribute, txId1, "type 1");
        graph.setStringValue(transactionTypeAttribute, txId2, "type 2");
        graph.setStringValue(transactionTypeAttribute, txId3, "type 3");

        saveGraphToFile("testMergeTransactionWithNoCommonTypes-before");

        // threshold to 30 seconds
        int threshold = 30;

        int mergedCount = 0;
        final Map<Integer, Set<Integer>> transactionsToMerge = mergeTypeInstance.getTransactionsToMerge(graph, MergeTransactionsPlugin.LATEST_TRANSACTION_CHOOSER, threshold, false);
        for (final Integer leadTransaction : transactionsToMerge.keySet()) {
            mergedCount += instance.mergeTransactions(graph, transactionsToMerge.get(leadTransaction), leadTransaction, new PrioritySurvivingGraphElementMerger());
        }

        saveGraphToFile("testMergeTransactionWithNoCommonTypes-after");

        assertEquals(0, mergedCount);
    }

    @Test
    public void testMergeTransactionWithNoCommonTypesAndNoActivity() throws Exception {
        MergeTransactionsPlugin instance = new MergeTransactionsPlugin();
        MergeTransactionsByDateTime mergeTypeInstance = new MergeTransactionsByDateTime();

        // overide the scenario and nullify the activity so that the type will be checked
        graph.setStringValue(transactionIdentifierAttribute, txId1, null);
        graph.setStringValue(transactionIdentifierAttribute, txId2, null);
        graph.setStringValue(transactionIdentifierAttribute, txId3, null);

        // overide the scenario and set the same type
        graph.setStringValue(transactionTypeAttribute, txId1, "type 1");
        graph.setStringValue(transactionTypeAttribute, txId2, "type 2");
        graph.setStringValue(transactionTypeAttribute, txId3, "type 3");

        saveGraphToFile("testMergeTransactionWithNoCommonTypesAndNoActivity-before");

        // threshold to 30 seconds
        int threshold = 30;

        int mergedCount = 0;
        final Map<Integer, Set<Integer>> transactionsToMerge = mergeTypeInstance.getTransactionsToMerge(graph, MergeTransactionsPlugin.LATEST_TRANSACTION_CHOOSER, threshold, false);
        for (final Integer leadTransaction : transactionsToMerge.keySet()) {
            mergedCount += instance.mergeTransactions(graph, transactionsToMerge.get(leadTransaction), leadTransaction, new PrioritySurvivingGraphElementMerger());
        }

        saveGraphToFile("testMergeTransactionWithNoCommonTypesAndNoActivity-after");

        assertEquals(0, mergedCount);
    }

    @Test
    public void testMergeTransactionNotSelected() throws Exception {
        MergeTransactionsPlugin instance = new MergeTransactionsPlugin();
        MergeTransactionsByDateTime mergeTypeInstance = new MergeTransactionsByDateTime();

        // overide the scenario and set the same type
        graph.setBooleanValue(transactionSelectedAttribute, txId1, false);
        graph.setBooleanValue(transactionSelectedAttribute, txId2, false);
        graph.setBooleanValue(transactionSelectedAttribute, txId3, false);

        saveGraphToFile("testMergeTransactionNotSelected-before");

        // threshold to 30 seconds
        int threshold = 30;

        int mergedCount = 0;
        final Map<Integer, Set<Integer>> transactionsToMerge = mergeTypeInstance.getTransactionsToMerge(graph, MergeTransactionsPlugin.LATEST_TRANSACTION_CHOOSER, threshold, true);
        for (final Integer leadTransaction : transactionsToMerge.keySet()) {
            mergedCount += instance.mergeTransactions(graph, transactionsToMerge.get(leadTransaction), leadTransaction, new PrioritySurvivingGraphElementMerger());
        }

        saveGraphToFile("testMergeTransactionNotSelected-after");

        assertEquals(0, mergedCount);
    }

    @Test
    public void testMergeTransactionSomeSelected() throws Exception {
        MergeTransactionsPlugin instance = new MergeTransactionsPlugin();
        MergeTransactionsByDateTime mergeTypeInstance = new MergeTransactionsByDateTime();

        // overide the scenario and set the same type
        graph.setBooleanValue(transactionSelectedAttribute, txId1, false);
        graph.setBooleanValue(transactionSelectedAttribute, txId2, true);
        graph.setBooleanValue(transactionSelectedAttribute, txId3, true);

        saveGraphToFile("testMergeTransactionSomeSelected-before");

        // threshold to 30 seconds
        int threshold = 30;

        int mergedCount = 0;
        final Map<Integer, Set<Integer>> transactionsToMerge = mergeTypeInstance.getTransactionsToMerge(graph, MergeTransactionsPlugin.LATEST_TRANSACTION_CHOOSER, threshold, true);
        for (final Integer leadTransaction : transactionsToMerge.keySet()) {
            mergedCount += instance.mergeTransactions(graph, transactionsToMerge.get(leadTransaction), leadTransaction, new PrioritySurvivingGraphElementMerger());
        }

        saveGraphToFile("testMergeTransactionSomeSelected-after");

        assertEquals(1, mergedCount);
    }

    @Test
    public void testSortTransactionsWithNullType1() {
        MergeTransactionType instance = new TestMergeTransactionType();
        Integer[] transactions = new Integer[2];
        transactions[0] = txId4;
        transactions[1] = txId2;

        Integer[] expecting = new Integer[2];
        expecting[0] = txId4;
        expecting[1] = txId2;

        instance.sortTransactions(transactions, graph, transactionTypeAttribute, transactionDateTimeAttribute, MergeTransactionsPlugin.LATEST_TRANSACTION_CHOOSER);
        assertArrayEquals(expecting, transactions);
    }

    @Test
    public void testSortTransactionsWithNullType2() {
        MergeTransactionType instance = new TestMergeTransactionType();
        Integer[] transactions = new Integer[2];
        transactions[0] = txId3;
        transactions[1] = txId4;

        Integer[] expecting = new Integer[2];
        expecting[0] = txId4;
        expecting[1] = txId3;

        instance.sortTransactions(transactions, graph, transactionTypeAttribute, transactionDateTimeAttribute, MergeTransactionsPlugin.LATEST_TRANSACTION_CHOOSER);
        assertArrayEquals(expecting, transactions);
    }

    @Test
    public void testSortTransactionsWithNotNullTypes() {
        MergeTransactionType instance = new TestMergeTransactionType();
        Integer[] transactions = new Integer[2];
        transactions[0] = txId4;
        transactions[1] = txId5;

        Integer[] expecting = new Integer[2];
        expecting[0] = txId5;
        expecting[1] = txId4;

        instance.sortTransactions(transactions, graph, transactionTypeAttribute, transactionDateTimeAttribute, MergeTransactionsPlugin.LATEST_TRANSACTION_CHOOSER);
        assertArrayEquals(expecting, transactions);
    }

    @Test
    public void testCompareTypeHierarchy() {
        MergeTransactionType instance = new TestMergeTransactionType();
        Boolean result = instance.compareTypeHierarchy(graph.getObjectValue(transactionTypeAttribute, txId4),
                graph.getObjectValue(transactionTypeAttribute, txId5));
        assertFalse(result);

        result = instance.compareTypeHierarchy(graph.getObjectValue(transactionTypeAttribute, txId4),
                graph.getObjectValue(transactionTypeAttribute, txId4));
        assertTrue(result);

        result = instance.compareTypeHierarchy(null, null);
        assertTrue(result);
    }

    private class TestMergeTransactionType implements MergeTransactionType {

        public String getName() {
            return "";
        }

        public void updateParameters(Map<String, PluginParameter<?>> parameters) {
        }

        public Map<Integer, Set<Integer>> getTransactionsToMerge(GraphWriteMethods graph, Comparator<Long> leadTransactionChooser, int threshold, boolean selectedOnly) throws MergeTransactionType.MergeException {
            return null;
        }
    }
}
