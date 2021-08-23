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
package au.gov.asd.tac.constellation.graph.interaction.plugins.select;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.graphics.IntArray;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * PointSelectionPlugin Tests.
 * <br>
 * The parameters toggleSelection and clearSelection used by the plugin work as
 * follows:
 * <br>
 * toggleSelection: If true is as if control key is held down so consecutive
 * selections will select multiple elements, or deselect selected elements.
 * <br>
 * clearSelection: If true deselects all other elements aside from the one being
 * selected. If toggle selection is true this should be false, otherwise
 * consecutive selections will be deselected.
 *
 * @author sol695510
 */
public class PointSelectionPluginNGTest {

    private StoreGraph storeGraph;

    // Vertex and transaction attribute IDs respectively.
    private int vAttrId, tAttrId;

    // Vertex and transaction IDs respectively.
    private int vxId1, vxId2, vxId3;
    private int txId1, txId2, txId3;

    // Arrays used by the PointSelectionPlugin that contain the selected graph elements during plugin execution.
    private IntArray vxIds;
    private IntArray txIds;

    public PointSelectionPluginNGTest() {
        vxIds = new IntArray();
        txIds = new IntArray();
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Creates a basic graph with 3 vertices and 3 transactions with selection
     * attributes added for testing.
     *
     * @throws Exception
     */
    @BeforeMethod
    public void setUpMethod() throws Exception {

        storeGraph = new StoreGraph();

        vAttrId = VisualConcept.VertexAttribute.SELECTED.ensure(storeGraph);
        tAttrId = VisualConcept.TransactionAttribute.SELECTED.ensure(storeGraph);

        vxId1 = storeGraph.addVertex();
        vxId2 = storeGraph.addVertex();
        vxId3 = storeGraph.addVertex();

        txId1 = storeGraph.addTransaction(vxId1, vxId2, false);
        txId2 = storeGraph.addTransaction(vxId2, vxId3, false);
        txId3 = storeGraph.addTransaction(vxId3, vxId1, false);

        // Ensures arrays don't contain IDs from previous tests when going into a subsequent test.
        vxIds = new IntArray();
        txIds = new IntArray();

        selectAllAndAssert(null, false);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Tests selections with both the toggleSelection and clearSelection options
     * on a graph that begins with no elements selected.
     *
     * @throws Exception
     */
    @Test
    public void testNoElementsSelectedOnGraph() throws Exception {

        // Select vertex vxId1 with clearSelection. All other elements should remain deselected.
        vxIds.add(vxId1);

        Plugin selectPoint1 = new PointSelectionPlugin(vxIds, txIds, false, true);
        PluginExecution.withPlugin(selectPoint1).executeNow(storeGraph);

        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId3));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId1));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId2));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId3));

        vxIds.clear();
        txIds.clear();

        // Select vertex vxId2 with clearSelection. Should deselect vertex vxId1.
        vxIds.add(vxId2);

        Plugin selectPoint2 = new PointSelectionPlugin(vxIds, txIds, false, true);
        PluginExecution.withPlugin(selectPoint2).executeNow(storeGraph);

        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId3));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId1));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId2));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId3));

        vxIds.clear();
        txIds.clear();

        // Select transaction txId3 with toggleSelection. Vertex vxId2 should remain selected.
        txIds.add(txId3);

        Plugin selectPoint3 = new PointSelectionPlugin(vxIds, txIds, true, false);
        PluginExecution.withPlugin(selectPoint3).executeNow(storeGraph);

        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId3));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId1));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId2));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId3));

        vxIds.clear();
        txIds.clear();

        // Reselect vertex vxId2 with toggleSelection. Should deselect vertex vxId2 and transaction txId3 should remain selected.
        vxIds.add(vxId2);

        Plugin selectPoint4 = new PointSelectionPlugin(vxIds, txIds, true, false);
        PluginExecution.withPlugin(selectPoint4).executeNow(storeGraph);

        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId3));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId1));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId2));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId3));
    }

    /**
     * Tests selections with both the toggleSelection and clearSelection options
     * on a graph that begins with all elements selected.
     *
     * @throws Exception
     */
    @Test
    public void testAllElementsSelectedOnGraph() throws Exception {

        selectAllAndAssert(null, true);

        // Select vertex vxId1 with toggleSelection. All other elements should remain selected.
        vxIds.add(vxId1);

        Plugin selectPoint1 = new PointSelectionPlugin(vxIds, txIds, true, false);
        PluginExecution.withPlugin(selectPoint1).executeNow(storeGraph);

        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId3));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId1));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId2));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId3));

        vxIds.clear();
        txIds.clear();

        // Select vertex vxId2 with toggleSelection. Vertex vxId1 should remain deselected and all other elements should remain selected.
        vxIds.add(vxId2);

        Plugin selectPoint2 = new PointSelectionPlugin(vxIds, txIds, true, false);
        PluginExecution.withPlugin(selectPoint2).executeNow(storeGraph);

        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId3));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId1));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId2));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId3));

        vxIds.clear();
        txIds.clear();

        // Select transaction txId3 with clearSelection. All other elements should be deselected.
        txIds.add(txId3);

        Plugin selectPoint3 = new PointSelectionPlugin(vxIds, txIds, false, true);
        PluginExecution.withPlugin(selectPoint3).executeNow(storeGraph);

        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId3));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId1));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId2));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId3));

        vxIds.clear();
        txIds.clear();

        // Select no elements with clearSelection. Should deselect all elements as this simulates clicking on an empty space on the graph.
        Plugin selectPoint4 = new PointSelectionPlugin(vxIds, txIds, false, true);
        PluginExecution.withPlugin(selectPoint4).executeNow(storeGraph);

        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId3));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId1));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId2));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId3));
    }

    /**
     * Tests when a vertex is selected and; all vertices and transaction are
     * unselected, toggle selection is true, and clear selection is true.
     *
     * @throws Exception
     */
    @Test
    public void testNoElementsSelectedAndVertexSelected() throws Exception {

        vxIds.add(vxId1);

        Plugin selectPoint = new PointSelectionPlugin(vxIds, txIds, true, true);
        PluginExecution.withPlugin(selectPoint).executeNow(storeGraph);

        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId3));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId1));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId2));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId3));
    }

    /**
     * Tests when a transaction is selected and; all vertices and transaction
     * are unselected, toggle selection is true, and clear selection is true.
     *
     * @throws Exception
     */
    @Test
    public void testNoElementsSelectedAndTransactionSelected() throws Exception {

        txIds.add(txId1);

        Plugin selectPoint = new PointSelectionPlugin(vxIds, txIds, true, true);
        PluginExecution.withPlugin(selectPoint).executeNow(storeGraph);

        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId3));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId1));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId2));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId3));
    }

    /**
     * Tests when a vertex is selected and; all vertices and transactions are
     * selected, toggle selection is true, and clear selection is true.
     *
     * @throws Exception
     */
    @Test
    public void testAllElementsSelectedAndVertexSelected() throws Exception {

        selectAllAndAssert(null, true);

        vxIds.add(vxId1);

        Plugin selectPoint = new PointSelectionPlugin(vxIds, txIds, true, true);
        PluginExecution.withPlugin(selectPoint).executeNow(storeGraph);

        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId3));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId1));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId2));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId3));
    }

    /**
     * Tests when a transaction is selected and; all vertices and transactions
     * are selected, toggle selection is true, and clear selection is true.
     *
     * @throws Exception
     */
    @Test
    public void testAllElementsSelectedAndTransactionSelected() throws Exception {

        selectAllAndAssert(null, true);

        txIds.add(txId1);

        Plugin selectPoint = new PointSelectionPlugin(vxIds, txIds, true, true);
        PluginExecution.withPlugin(selectPoint).executeNow(storeGraph);

        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId3));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId1));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId2));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId3));
    }

    /**
     * Tests when a vertex is selected and; only vertices are selected, toggle
     * selection is true, and clear selection is true.
     *
     * @throws Exception
     */
    @Test
    public void testOnlyVerticesSelectedAndVertexSelected() throws Exception {

        selectAllAndAssert(GraphElementType.VERTEX, true);

        vxIds.add(vxId1);

        Plugin selectPoint = new PointSelectionPlugin(vxIds, txIds, true, true);
        PluginExecution.withPlugin(selectPoint).executeNow(storeGraph);

        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId3));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId1));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId2));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId3));
    }

    /**
     * Tests when a transaction is selected and; only vertices are selected,
     * toggle selection is true, and clear selection is true.
     *
     * @throws Exception
     */
    @Test
    public void testOnlyVerticesSelectedAndTransactionSelected() throws Exception {

        selectAllAndAssert(GraphElementType.VERTEX, true);

        txIds.add(txId1);

        Plugin selectPoint = new PointSelectionPlugin(vxIds, txIds, true, true);
        PluginExecution.withPlugin(selectPoint).executeNow(storeGraph);

        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId3));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId1));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId2));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId3));
    }

    /**
     * Tests when a vertex is selected and; only transactions are selected,
     * toggle selection is true, and clear selection is true.
     *
     * @throws Exception
     */
    @Test
    public void testOnlyTransactionsSelectedAndVertexSelected() throws Exception {

        selectAllAndAssert(GraphElementType.TRANSACTION, true);

        vxIds.add(vxId1);

        Plugin selectPoint = new PointSelectionPlugin(vxIds, txIds, true, true);
        PluginExecution.withPlugin(selectPoint).executeNow(storeGraph);

        assertTrue(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId3));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId1));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId2));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId3));
    }

    /**
     * Tests when a transaction is selected and; only transactions are selected,
     * toggle selection is true, and clear selection is true.
     *
     * @throws Exception
     */
    @Test
    public void testOnlyTransactionsSelectedAndTransactionSelected() throws Exception {

        selectAllAndAssert(GraphElementType.TRANSACTION, true);

        txIds.add(txId1);

        Plugin selectPoint = new PointSelectionPlugin(vxIds, txIds, true, true);
        PluginExecution.withPlugin(selectPoint).executeNow(storeGraph);

        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId1));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId2));
        assertFalse(storeGraph.getBooleanValue(vAttrId, vxId3));
        assertTrue(storeGraph.getBooleanValue(tAttrId, txId1));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId2));
        assertFalse(storeGraph.getBooleanValue(tAttrId, txId3));
    }

    /**
     * Selects all of given type of graph element or all graph elements, and
     * asserts whether the selections have been made correctly.
     *
     * @param type The type of graph element being selected, or null if all
     * graph elements are to be selected.
     * @param selectAll Whether the selection attribute for a graph element will
     * be set to true; selected, or false; unselected.
     */
    private void selectAllAndAssert(final GraphElementType type, final boolean selectAll) {

        if (null == type) {
            storeGraph.setBooleanValue(vAttrId, vxId1, selectAll);
            storeGraph.setBooleanValue(vAttrId, vxId2, selectAll);
            storeGraph.setBooleanValue(vAttrId, vxId3, selectAll);

            storeGraph.setBooleanValue(tAttrId, txId1, selectAll);
            storeGraph.setBooleanValue(tAttrId, txId2, selectAll);
            storeGraph.setBooleanValue(tAttrId, txId3, selectAll);

            assertEquals(storeGraph.getBooleanValue(vAttrId, vxId1), selectAll);
            assertEquals(storeGraph.getBooleanValue(vAttrId, vxId2), selectAll);
            assertEquals(storeGraph.getBooleanValue(vAttrId, vxId3), selectAll);

            assertEquals(storeGraph.getBooleanValue(tAttrId, txId1), selectAll);
            assertEquals(storeGraph.getBooleanValue(tAttrId, txId2), selectAll);
            assertEquals(storeGraph.getBooleanValue(tAttrId, txId3), selectAll);
        } else {
            switch (type) {
                case VERTEX:
                    storeGraph.setBooleanValue(vAttrId, vxId1, selectAll);
                    storeGraph.setBooleanValue(vAttrId, vxId2, selectAll);
                    storeGraph.setBooleanValue(vAttrId, vxId3, selectAll);

                    storeGraph.setBooleanValue(tAttrId, txId1, !selectAll);
                    storeGraph.setBooleanValue(tAttrId, txId2, !selectAll);
                    storeGraph.setBooleanValue(tAttrId, txId3, !selectAll);

                    assertEquals(storeGraph.getBooleanValue(vAttrId, vxId1), selectAll);
                    assertEquals(storeGraph.getBooleanValue(vAttrId, vxId2), selectAll);
                    assertEquals(storeGraph.getBooleanValue(vAttrId, vxId3), selectAll);

                    assertEquals(storeGraph.getBooleanValue(tAttrId, txId1), !selectAll);
                    assertEquals(storeGraph.getBooleanValue(tAttrId, txId2), !selectAll);
                    assertEquals(storeGraph.getBooleanValue(tAttrId, txId3), !selectAll);
                    break;
                case TRANSACTION:
                    storeGraph.setBooleanValue(vAttrId, vxId1, !selectAll);
                    storeGraph.setBooleanValue(vAttrId, vxId2, !selectAll);
                    storeGraph.setBooleanValue(vAttrId, vxId3, !selectAll);

                    storeGraph.setBooleanValue(tAttrId, txId1, selectAll);
                    storeGraph.setBooleanValue(tAttrId, txId2, selectAll);
                    storeGraph.setBooleanValue(tAttrId, txId3, selectAll);

                    assertEquals(storeGraph.getBooleanValue(vAttrId, vxId1), !selectAll);
                    assertEquals(storeGraph.getBooleanValue(vAttrId, vxId2), !selectAll);
                    assertEquals(storeGraph.getBooleanValue(vAttrId, vxId3), !selectAll);

                    assertEquals(storeGraph.getBooleanValue(tAttrId, txId1), selectAll);
                    assertEquals(storeGraph.getBooleanValue(tAttrId, txId2), selectAll);
                    assertEquals(storeGraph.getBooleanValue(tAttrId, txId3), selectAll);
                    break;
                default:
                    break;
            }
        }
    }
}
