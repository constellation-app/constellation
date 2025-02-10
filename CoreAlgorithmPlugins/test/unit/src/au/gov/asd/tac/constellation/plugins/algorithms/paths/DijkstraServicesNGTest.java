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
        graph = new StoreGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of queryPaths method, of class DijkstraServices.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testQueryPathsWithNothingSelected() throws Exception {
        // Add 2 vertices
        final int vx0 = graph.addVertex();
        final int vx1 = graph.addVertex();
        
        // create a transaction from vx0 -> vx1
        final int tx0 = graph.addTransaction(vx0, vx1, true);
        
        final int selectedVertexAttr = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        final int selectedTransactionAttr = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);
        
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
        // Add 2 vertices
        final int vx0 = graph.addVertex();
        final int vx1 = graph.addVertex();
        
        // create a transaction from vx0 -> vx1
        final int tx0 = graph.addTransaction(vx0, vx1, true);
        
        final int selectedVertexAttr = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        final int selectedTransactionAttr = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);

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
        // Add 2 vertices
        final int vx0 = graph.addVertex();
        final int vx1 = graph.addVertex();
        
        // create a transaction from vx0 -> vx1
        final int tx0 = graph.addTransaction(vx0, vx1, true);
        
        final int selectedVertexAttr = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        final int selectedTransactionAttr = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);

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
        // Add 5 vertices
        final int vx0 = graph.addVertex();
        final int vx1 = graph.addVertex();
        final int vx2 = graph.addVertex();
        final int vx3 = graph.addVertex();
        final int vx4 = graph.addVertex();
        
        // create transactions
        final int tx0 = graph.addTransaction(vx0, vx2, true);
        final int tx1 = graph.addTransaction(vx2, vx1, true);
        final int tx2 = graph.addTransaction(vx1, vx0, true);
        final int tx3 = graph.addTransaction(vx0, vx3, true);
        final int tx4 = graph.addTransaction(vx3, vx4, true);
        final int tx5 = graph.addTransaction(vx4, vx2, true);
        
        final int selectedVertexAttr = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        final int selectedTransactionAttr = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);

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
        // Add 5 vertices
        final int vx0 = graph.addVertex();
        final int vx1 = graph.addVertex();
        final int vx2 = graph.addVertex();
        final int vx3 = graph.addVertex();
        final int vx4 = graph.addVertex();
        
        // create transactions
        final int tx0 = graph.addTransaction(vx2, vx0, true);
        final int tx1 = graph.addTransaction(vx2, vx1, true);
        final int tx2 = graph.addTransaction(vx1, vx0, true);
        final int tx3 = graph.addTransaction(vx0, vx3, true);
        final int tx4 = graph.addTransaction(vx3, vx4, true);
        final int tx5 = graph.addTransaction(vx4, vx2, true);
        
        final int selectedVertexAttr = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        final int selectedTransactionAttr = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);
        
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
        // Add 5 vertices
        final int vx0 = graph.addVertex();
        final int vx1 = graph.addVertex();
        final int vx2 = graph.addVertex();
        final int vx3 = graph.addVertex();
        final int vx4 = graph.addVertex();
        
        // create transactions
        final int tx0 = graph.addTransaction(vx2, vx1, true);
        final int tx1 = graph.addTransaction(vx1, vx0, true);
        final int tx2 = graph.addTransaction(vx0, vx3, true);
        final int tx3 = graph.addTransaction(vx3, vx4, true);
        final int tx4 = graph.addTransaction(vx4, vx2, true);
        
        final int selectedVertexAttr = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        final int selectedTransactionAttr = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);

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
        assertTrue(graph.getBooleanValue(selectedTransactionAttr, tx0));
        assertTrue(graph.getBooleanValue(selectedTransactionAttr, tx1));

        // deselected
        assertFalse(graph.getBooleanValue(selectedVertexAttr, vx3));
        assertFalse(graph.getBooleanValue(selectedVertexAttr, vx4));
        assertFalse(graph.getBooleanValue(selectedTransactionAttr, tx2));
        assertFalse(graph.getBooleanValue(selectedTransactionAttr, tx3));
        assertFalse(graph.getBooleanValue(selectedTransactionAttr, tx4));
    }

    /**
     * Test of queryPaths method, of class DijkstraServices.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testQueryPathsWithNoDirectionComplex3() throws Exception {
        // Add 5 vertices
        final int vx0 = graph.addVertex();
        final int vx1 = graph.addVertex();
        final int vx2 = graph.addVertex();
        final int vx3 = graph.addVertex();
        final int vx4 = graph.addVertex();
        
        // create transactions
        final int tx0 = graph.addTransaction(vx2, vx1, true);
        final int tx1 = graph.addTransaction(vx1, vx0, true);
        final int tx2 = graph.addTransaction(vx0, vx3, true);
        final int tx3 = graph.addTransaction(vx3, vx4, true);
        final int tx4 = graph.addTransaction(vx4, vx2, true);
        
        final int selectedVertexAttr = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        final int selectedTransactionAttr = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);

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
        assertTrue(graph.getBooleanValue(selectedTransactionAttr, tx0));
        assertTrue(graph.getBooleanValue(selectedTransactionAttr, tx1));

        // deselected
        assertFalse(graph.getBooleanValue(selectedVertexAttr, vx3));
        assertFalse(graph.getBooleanValue(selectedVertexAttr, vx4));
        assertFalse(graph.getBooleanValue(selectedTransactionAttr, tx2));
        assertFalse(graph.getBooleanValue(selectedTransactionAttr, tx3));
        assertFalse(graph.getBooleanValue(selectedTransactionAttr, tx4));
    }
}
