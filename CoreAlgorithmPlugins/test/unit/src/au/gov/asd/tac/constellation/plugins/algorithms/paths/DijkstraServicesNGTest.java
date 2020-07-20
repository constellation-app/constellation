/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.algorithms.paths;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import java.util.ArrayList;
import java.util.List;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Dijkstra Services Test.
 *
 * @author arcturus
 */
public class DijkstraServicesNGTest {

    private StoreGraph graph;

    public DijkstraServicesNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        graph = new StoreGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of queryPaths method, of class DijkstraServices.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testQueryPathsWithNothingSelected() throws Exception {
        final int vx0, vx1, tx0;
        final int selectedVertexAttr, selectedTransactionAttr;

        selectedVertexAttr = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        selectedTransactionAttr = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);

        // Add 2 vertices
        vx0 = graph.addVertex();
        vx1 = graph.addVertex();

        // create a transaction from vx0 -> vx1
        tx0 = graph.addTransaction(vx0, vx1, true);

        final List<Integer> selectedNodes = new ArrayList<>();

        boolean deselectCurrent = false;
        final DijkstraServices instance = new DijkstraServices(graph, selectedNodes, false);
        instance.queryPaths(deselectCurrent);

        assertFalse(graph.getBooleanValue(selectedVertexAttr, vx0));
        assertFalse(graph.getBooleanValue(selectedVertexAttr, vx1));
        assertFalse(graph.getBooleanValue(selectedTransactionAttr, tx0));
    }

    /**
     * Test of queryPaths method, of class DijkstraServices.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testQueryPathsWithNoDirection() throws Exception {
        final int vx0, vx1, tx0;
        final int selectedVertexAttr, selectedTransactionAttr;

        selectedVertexAttr = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        selectedTransactionAttr = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);

        // Add 2 vertices
        vx0 = graph.addVertex();
        vx1 = graph.addVertex();

        // create a transaction from vx0 -> vx1
        tx0 = graph.addTransaction(vx0, vx1, true);

        // select vx0 and vx1
        graph.setBooleanValue(selectedVertexAttr, vx0, true);
        graph.setBooleanValue(selectedVertexAttr, vx1, true);

        final List<Integer> selectedNodes = new ArrayList<>();
        selectedNodes.add(vx0);
        selectedNodes.add(vx1);

        boolean deselectCurrent = false;
        final DijkstraServices instance = new DijkstraServices(graph, selectedNodes, false);
        instance.queryPaths(deselectCurrent);

        assertTrue(graph.getBooleanValue(selectedVertexAttr, vx0));
        assertTrue(graph.getBooleanValue(selectedVertexAttr, vx1));
        assertTrue(graph.getBooleanValue(selectedTransactionAttr, tx0));
    }

    /**
     * Test of queryPaths method, of class DijkstraServices.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testQueryPathsWithDirection() throws Exception {
        final int vx0, vx1, tx0;
        final int selectedVertexAttr, selectedTransactionAttr;

        selectedVertexAttr = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        selectedTransactionAttr = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);

        // Add 2 vertices
        vx0 = graph.addVertex();
        vx1 = graph.addVertex();

        // create a transaction from vx0 -> vx1
        tx0 = graph.addTransaction(vx0, vx1, true);

        // select vx0 and vx1
        graph.setBooleanValue(selectedVertexAttr, vx0, true);
        graph.setBooleanValue(selectedVertexAttr, vx1, true);

        final List<Integer> selectedNodes = new ArrayList<>();
        selectedNodes.add(vx0);
        selectedNodes.add(vx1);

        boolean deselectCurrent = false;
        final DijkstraServices instance = new DijkstraServices(graph, selectedNodes, true);
        instance.queryPaths(deselectCurrent);

        assertTrue(graph.getBooleanValue(selectedVertexAttr, vx0));
        assertTrue(graph.getBooleanValue(selectedVertexAttr, vx1));
        assertTrue(graph.getBooleanValue(selectedTransactionAttr, tx0));
    }

    /**
     * Test of queryPaths method, of class DijkstraServices.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testQueryPathsWithDirectionComplex1() throws Exception {
        final int vx0, vx1, vx2, vx3, vx4, tx0, tx1, tx2, tx3, tx4, tx5;
        final int selectedVertexAttr, selectedTransactionAttr;

        selectedVertexAttr = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        selectedTransactionAttr = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);

        // Add 2 vertices
        vx0 = graph.addVertex();
        vx1 = graph.addVertex();
        vx2 = graph.addVertex();
        vx3 = graph.addVertex();
        vx4 = graph.addVertex();

        // create transactions
        tx0 = graph.addTransaction(vx0, vx2, true);
        tx1 = graph.addTransaction(vx2, vx1, true);
        tx2 = graph.addTransaction(vx1, vx0, true);
        tx3 = graph.addTransaction(vx0, vx3, true);
        tx4 = graph.addTransaction(vx3, vx4, true);
        tx5 = graph.addTransaction(vx4, vx2, true);

        // make the selection
        graph.setBooleanValue(selectedVertexAttr, vx0, true);
        graph.setBooleanValue(selectedVertexAttr, vx2, true);

        final List<Integer> selectedNodes = new ArrayList<>();
        selectedNodes.add(vx0);
        selectedNodes.add(vx2);

        boolean deselectCurrent = false;
        final DijkstraServices instance = new DijkstraServices(graph, selectedNodes, true);
        instance.queryPaths(deselectCurrent);

        // selected
        assertTrue(graph.getBooleanValue(selectedVertexAttr, vx0));
        assertTrue(graph.getBooleanValue(selectedVertexAttr, vx2));
        assertTrue(graph.getBooleanValue(selectedTransactionAttr, tx0));

        // deselected
        assertFalse(graph.getBooleanValue(selectedVertexAttr, vx1));
        assertFalse(graph.getBooleanValue(selectedVertexAttr, vx3));
        assertFalse(graph.getBooleanValue(selectedVertexAttr, vx4));
        assertFalse(graph.getBooleanValue(selectedTransactionAttr, tx1));
        assertFalse(graph.getBooleanValue(selectedTransactionAttr, tx2));
        assertFalse(graph.getBooleanValue(selectedTransactionAttr, tx3));
        assertFalse(graph.getBooleanValue(selectedTransactionAttr, tx4));
        assertFalse(graph.getBooleanValue(selectedTransactionAttr, tx5));
    }

    /**
     * Test of queryPaths method, of class DijkstraServices.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testQueryPathsWithDirectionComplex2() throws Exception {
        final int vx0, vx1, vx2, vx3, vx4, tx0, tx1, tx2, tx3, tx4, tx5;
        final int selectedVertexAttr, selectedTransactionAttr;

        selectedVertexAttr = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        selectedTransactionAttr = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);

        // Add 2 vertices
        vx0 = graph.addVertex();
        vx1 = graph.addVertex();
        vx2 = graph.addVertex();
        vx3 = graph.addVertex();
        vx4 = graph.addVertex();

        // create transactions
        tx0 = graph.addTransaction(vx2, vx0, true);
        tx1 = graph.addTransaction(vx2, vx1, true);
        tx2 = graph.addTransaction(vx1, vx0, true);
        tx3 = graph.addTransaction(vx0, vx3, true);
        tx4 = graph.addTransaction(vx3, vx4, true);
        tx5 = graph.addTransaction(vx4, vx2, true);

        // make the selection
        graph.setBooleanValue(selectedVertexAttr, vx0, true);
        graph.setBooleanValue(selectedVertexAttr, vx2, true);

        final List<Integer> selectedNodes = new ArrayList<>();
        selectedNodes.add(vx2);
        selectedNodes.add(vx0);

        boolean deselectCurrent = false;
        final DijkstraServices instance = new DijkstraServices(graph, selectedNodes, true);
        instance.queryPaths(deselectCurrent);

        // selected
        assertTrue(graph.getBooleanValue(selectedVertexAttr, vx0));
        assertTrue(graph.getBooleanValue(selectedVertexAttr, vx2));
        assertTrue(graph.getBooleanValue(selectedTransactionAttr, tx0));

        // deselected
        assertFalse(graph.getBooleanValue(selectedVertexAttr, vx1));
        assertFalse(graph.getBooleanValue(selectedVertexAttr, vx3));
        assertFalse(graph.getBooleanValue(selectedVertexAttr, vx4));
        assertFalse(graph.getBooleanValue(selectedTransactionAttr, tx1));
        assertFalse(graph.getBooleanValue(selectedTransactionAttr, tx2));
        assertFalse(graph.getBooleanValue(selectedTransactionAttr, tx3));
        assertFalse(graph.getBooleanValue(selectedTransactionAttr, tx4));
        assertFalse(graph.getBooleanValue(selectedTransactionAttr, tx5));
    }

    /**
     * Test of queryPaths method, of class DijkstraServices.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testQueryPathsWithDirectionComplex3() throws Exception {
        final int vx0, vx1, vx2, vx3, vx4, tx0, tx1, tx2, tx3, tx4, tx5;
        final int selectedVertexAttr, selectedTransactionAttr;

        selectedVertexAttr = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        selectedTransactionAttr = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);

        // Add 2 vertices
        vx0 = graph.addVertex();
        vx1 = graph.addVertex();
        vx2 = graph.addVertex();
        vx3 = graph.addVertex();
        vx4 = graph.addVertex();

        // create transactions
        tx1 = graph.addTransaction(vx2, vx1, true);
        tx2 = graph.addTransaction(vx1, vx0, true);
        tx3 = graph.addTransaction(vx0, vx3, true);
        tx4 = graph.addTransaction(vx3, vx4, true);
        tx5 = graph.addTransaction(vx4, vx2, true);

        // make the selection
        graph.setBooleanValue(selectedVertexAttr, vx0, true);
        graph.setBooleanValue(selectedVertexAttr, vx2, true);

        final List<Integer> selectedNodes = new ArrayList<>();
        selectedNodes.add(vx2);
        selectedNodes.add(vx0);

        boolean deselectCurrent = false;
        final DijkstraServices instance = new DijkstraServices(graph, selectedNodes, true);
        instance.queryPaths(deselectCurrent);

        // selected
        assertTrue(graph.getBooleanValue(selectedVertexAttr, vx0));
        assertTrue(graph.getBooleanValue(selectedVertexAttr, vx2));
        assertTrue(graph.getBooleanValue(selectedVertexAttr, vx1));
        assertTrue(graph.getBooleanValue(selectedTransactionAttr, tx1));
        assertTrue(graph.getBooleanValue(selectedTransactionAttr, tx2));

        // deselected
        assertFalse(graph.getBooleanValue(selectedVertexAttr, vx3));
        assertFalse(graph.getBooleanValue(selectedVertexAttr, vx4));
        assertFalse(graph.getBooleanValue(selectedTransactionAttr, tx3));
        assertFalse(graph.getBooleanValue(selectedTransactionAttr, tx4));
        assertFalse(graph.getBooleanValue(selectedTransactionAttr, tx5));
    }

    /**
     * Test of queryPaths method, of class DijkstraServices.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testQueryPathsWithNoDirectionComplex3() throws Exception {
        final int vx0, vx1, vx2, vx3, vx4, tx0, tx1, tx2, tx3, tx4, tx5;
        final int selectedVertexAttr, selectedTransactionAttr;

        selectedVertexAttr = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        selectedTransactionAttr = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);

        // Add 2 vertices
        vx0 = graph.addVertex();
        vx1 = graph.addVertex();
        vx2 = graph.addVertex();
        vx3 = graph.addVertex();
        vx4 = graph.addVertex();

        // create transactions
        tx1 = graph.addTransaction(vx2, vx1, true);
        tx2 = graph.addTransaction(vx1, vx0, true);
        tx3 = graph.addTransaction(vx0, vx3, true);
        tx4 = graph.addTransaction(vx3, vx4, true);
        tx5 = graph.addTransaction(vx4, vx2, true);

        // make the selection
        graph.setBooleanValue(selectedVertexAttr, vx0, true);
        graph.setBooleanValue(selectedVertexAttr, vx2, true);

        final List<Integer> selectedNodes = new ArrayList<>();
        selectedNodes.add(vx0);
        selectedNodes.add(vx2);

        boolean deselectCurrent = false;
        final DijkstraServices instance = new DijkstraServices(graph, selectedNodes, false);
        instance.queryPaths(deselectCurrent);

        // selected
        assertTrue(graph.getBooleanValue(selectedVertexAttr, vx0));
        assertTrue(graph.getBooleanValue(selectedVertexAttr, vx2));
        assertTrue(graph.getBooleanValue(selectedVertexAttr, vx1));
        assertTrue(graph.getBooleanValue(selectedTransactionAttr, tx1));
        assertTrue(graph.getBooleanValue(selectedTransactionAttr, tx2));

        // deselected
        assertFalse(graph.getBooleanValue(selectedVertexAttr, vx3));
        assertFalse(graph.getBooleanValue(selectedVertexAttr, vx4));
        assertFalse(graph.getBooleanValue(selectedTransactionAttr, tx3));
        assertFalse(graph.getBooleanValue(selectedTransactionAttr, tx4));
        assertFalse(graph.getBooleanValue(selectedTransactionAttr, tx5));
    }
}
