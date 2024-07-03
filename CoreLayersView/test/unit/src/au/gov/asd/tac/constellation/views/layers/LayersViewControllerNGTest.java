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
package au.gov.asd.tac.constellation.views.layers;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.LayersConcept;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.views.layers.query.Query;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.openide.util.Exceptions;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author aldebaran30701
 */
public class LayersViewControllerNGTest {

    private int layerMaskV, layerMaskT, layerVisibilityV, layerVisibilityT, selectedV, selectedT;
    private int vxId1, vxId2, txId1, txId2;
    private Graph graph;

    public LayersViewControllerNGTest() {
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
     * Test of getDefault method, of class LayersViewController.
     */
    @Test
    public void testGetDefault() {
        System.out.println("getDefault");

        final LayersViewController instance1 = LayersViewController.getDefault();
        final LayersViewController instance2 = LayersViewController.getDefault();
        assertEquals(instance1, instance2);
    }

    /**
     * Verify that no elements are added to the graph when an empty graph is
     * used
     */
    @Test
    public void testRemoveBitmaskFromElementsEmptyGraph() {
        System.out.println("removeBitmaskFromElements_EmptyGraph");
        setupEmptyGraph();

        final GraphManager gm = Mockito.mock(GraphManager.class);
        when(gm.getActiveGraph()).thenReturn(graph);

        // Create mock of DataAccessPane to return the query phase pane mock
        try (MockedStatic<GraphManager> mockedStatic = Mockito.mockStatic(GraphManager.class)) {
            mockedStatic.when(() -> GraphManager.getDefault()).thenReturn(gm);

            ReadableGraph rg = graph.getReadableGraph();
            assertEquals(rg.getVertexCount(), 0);
            rg.close();

            // remove bitmask from elements - nothing should be on layer 5
            int currentIndex = 5;
            LayersViewController instance = LayersViewController.getDefault().init(null);
            instance.removeBitmaskFromElements(currentIndex);

            ReadableGraph rg2 = graph.getReadableGraph();
            assertEquals(rg2.getVertexCount(), 0);
            rg2.close();

            currentIndex = 1;
            instance.removeBitmaskFromElements(currentIndex);

            ReadableGraph rg3 = graph.getReadableGraph();
            assertEquals(rg3.getVertexCount(), 0);
            rg3.close();

        }
    }

    /**
     * Test of removeBitmaskFromElements method, of class LayersViewController.
     */
    @Test
    public void testRemoveBitmaskFromElements() {
        System.out.println("removeBitmaskFromElements");

        // Setup a graph with elements
        setupGraph();

        final GraphManager gm = Mockito.mock(GraphManager.class);
        when(gm.getActiveGraph()).thenReturn(graph);

        // Create mock of DataAccessPane to return the query phase pane mock
        try (MockedStatic<GraphManager> mockedStatic = Mockito.mockStatic(GraphManager.class)) {
            mockedStatic.when(() -> GraphManager.getDefault()).thenReturn(gm);

            WritableGraph wg;
            try {
                wg = graph.getWritableGraph("", true);

                // Check Vertex set correctly
                assertEquals(wg.getIntValue(layerMaskV, vxId1), 1);
                assertEquals(wg.getFloatValue(layerVisibilityV, vxId1), 1.0f);
                assertFalse(wg.getBooleanValue(selectedV, vxId1));

                assertEquals(wg.getIntValue(layerMaskV, vxId2), 1);
                assertEquals(wg.getFloatValue(layerVisibilityV, vxId2), 1.0f);
                assertFalse(wg.getBooleanValue(selectedV, vxId2));

                // Check Transaction set correctly
                assertEquals(wg.getIntValue(layerMaskT, vxId1), 1);
                assertEquals(wg.getFloatValue(layerVisibilityT, vxId1), 1.0f);
                assertFalse(wg.getBooleanValue(selectedT, vxId1));

                assertEquals(wg.getIntValue(layerMaskT, vxId2), 1);
                assertEquals(wg.getFloatValue(layerVisibilityT, vxId2), 1.0f);
                assertFalse(wg.getBooleanValue(selectedT, vxId2));

                wg.commit();

            } catch (final InterruptedException ex) {
                Exceptions.printStackTrace(ex);
                Thread.currentThread().interrupt();
            }

            // remove bitmask from elements - nothing should be on layer 5
            int currentIndex = 5;
            LayersViewController instance = LayersViewController.getDefault().init(null);
            instance.removeBitmaskFromElements(currentIndex);

            try {
                wg = graph.getWritableGraph("", true);

                // Check Vertex unchanged
                assertEquals(wg.getIntValue(layerMaskV, vxId1), 1);
                assertEquals(wg.getFloatValue(layerVisibilityV, vxId1), 1.0f);
                assertFalse(wg.getBooleanValue(selectedV, vxId1));

                assertEquals(wg.getIntValue(layerMaskV, vxId2), 1);
                assertEquals(wg.getFloatValue(layerVisibilityV, vxId2), 1.0f);
                assertFalse(wg.getBooleanValue(selectedV, vxId2));

                // Check Transaction set correctly
                assertEquals(wg.getIntValue(layerMaskT, vxId1), 1);
                assertEquals(wg.getFloatValue(layerVisibilityT, vxId1), 1.0f);
                assertFalse(wg.getBooleanValue(selectedT, vxId1));

                assertEquals(wg.getIntValue(layerMaskT, vxId2), 1);
                assertEquals(wg.getFloatValue(layerVisibilityT, vxId2), 1.0f);
                assertFalse(wg.getBooleanValue(selectedT, vxId2));

                // Set vx2 and tx2 into layer 2. Both are visibility 0 because current layer is 1.
                wg.setIntValue(layerMaskV, vxId2, 0b11);
                wg.setFloatValue(layerVisibilityV, vxId2, 0.0f);
                wg.setIntValue(layerMaskT, txId2, 0b11);
                wg.setFloatValue(layerVisibilityT, txId2, 0.0f);

                wg.commit();

            } catch (final InterruptedException ex) {
                Exceptions.printStackTrace(ex);
                Thread.currentThread().interrupt();
            }

            // remove bitmask from elements - should only remove the elements vx2 and tx2
            currentIndex = 1;
            instance.removeBitmaskFromElements(currentIndex);

            try {
                wg = graph.getWritableGraph("", true);
                // Check Vertex 1 unchanged
                assertEquals(wg.getIntValue(layerMaskV, vxId1), 1);
                assertEquals(wg.getFloatValue(layerVisibilityV, vxId1), 1.0f);
                assertFalse(wg.getBooleanValue(selectedV, vxId1));

                // Check Vertex 2 changed - visibility 0.0 because no visibility update
                assertEquals(wg.getIntValue(layerMaskV, vxId2), 1);
                assertEquals(wg.getFloatValue(layerVisibilityV, vxId2), 0.0f);
                assertFalse(wg.getBooleanValue(selectedV, vxId2));

                // Check Transaction 1 unchanged
                assertEquals(wg.getIntValue(layerMaskT, vxId1), 1);
                assertEquals(wg.getFloatValue(layerVisibilityT, vxId1), 1.0f);
                assertFalse(wg.getBooleanValue(selectedT, vxId1));

                assertEquals(wg.getIntValue(layerMaskT, vxId2), 1);
                assertEquals(wg.getFloatValue(layerVisibilityT, vxId2), 0.0f);
                assertFalse(wg.getBooleanValue(selectedT, vxId2));

                wg.commit();

            } catch (final InterruptedException ex) {
                Exceptions.printStackTrace(ex);
                Thread.currentThread().interrupt();
            }
        }
    }

    private void setupEmptyGraph() {
        graph = new DualGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());
    }

    /**
     * Set up a graph with two vertices and two transactions on layer 1.
     */
    private void setupGraph() {
        graph = new DualGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());
        try {

            WritableGraph wg = graph.getWritableGraph("", true);

            // Create LayerMask attributes
            layerMaskV = LayersConcept.VertexAttribute.LAYER_MASK.ensure(wg);
            layerMaskT = LayersConcept.TransactionAttribute.LAYER_MASK.ensure(wg);

            // Create LayerVisilibity Attributes
            layerVisibilityV = LayersConcept.VertexAttribute.LAYER_VISIBILITY.ensure(wg);
            layerVisibilityT = LayersConcept.TransactionAttribute.LAYER_VISIBILITY.ensure(wg);

            // Create Selected Attributes
            selectedV = VisualConcept.VertexAttribute.SELECTED.ensure(wg);
            selectedT = VisualConcept.TransactionAttribute.SELECTED.ensure(wg);

            // Adding 2 Vertices - not selected, layer 1, visible
            vxId1 = wg.addVertex();
            wg.setIntValue(layerMaskV, vxId1, 1);
            wg.setFloatValue(layerVisibilityV, vxId1, 1.0f);
            wg.setBooleanValue(selectedV, vxId1, false);

            vxId2 = wg.addVertex();
            wg.setIntValue(layerMaskV, vxId2, 1);
            wg.setFloatValue(layerVisibilityV, vxId2, 1.0f);
            wg.setBooleanValue(selectedV, vxId2, false);

            // Adding 2 Transactions - not selected, layer 1, visible
            txId1 = wg.addTransaction(vxId1, vxId2, true);
            wg.setIntValue(layerMaskT, txId1, 1);
            wg.setFloatValue(layerVisibilityT, txId1, 1.0f);
            wg.setBooleanValue(selectedT, txId1, false);

            txId2 = wg.addTransaction(vxId1, vxId2, false);
            wg.setIntValue(layerMaskT, txId2, 1);
            wg.setFloatValue(layerVisibilityT, vxId2, 1.0f);
            wg.setBooleanValue(selectedT, vxId2, false);

            wg.commit();

        } catch (final InterruptedException ex) {
            Exceptions.printStackTrace(ex);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Test of deleteLayer, in LayersViewController
     */
    @Test
    public void testDeleteLayer() {
        LayersViewController instance = LayersViewController.getDefault().init(null);
        instance.createLayer();
        instance.deleteLayer(2);
        assertEquals(instance.getVxQueryCollection().getHighestQueryIndex(), 3);

    }

    /**
     * Test of deselectAll, in LayersViewController
     */
    @Test
    public void testDeselectAll() {
        LayersViewController instance = LayersViewController.getDefault().init(null);
        instance.createLayer();
        instance.createLayer();
        instance.getVxQueryCollection().setVisibilityOnAll(true);
        instance.getTxQueryCollection().setVisibilityOnAll(true);

        instance.deselectAll();
        assertEquals(instance.getVxQueryCollection().isVisibilityOnAll(), false);
    }

    /**
     * Test of changeLayerVisibility, in LayersViewController
     */
    @Test
    public void testChangeLayerVisibility() {
        LayersViewController instance = LayersViewController.getDefault().init(null);
        instance.createLayer();
        int index1 = 2;
        Query query1 = new Query(GraphElementType.VERTEX, "Type == 'Event'");
        instance.getVxQueryCollection().getQuery(index1).setQuery(query1);
        instance.getTxQueryCollection().getQuery(index1).setQuery(null);

        instance.changeLayerVisibility(index1, true);
        assertTrue(instance.getVxQueryCollection().getQuery(index1).isVisible());

        instance.createLayer();
        int index2 = 3;
        Query query2 = new Query(GraphElementType.TRANSACTION, "Type == 'Network'");
        instance.getTxQueryCollection().getQuery(index2).setQuery(query2);
        instance.getVxQueryCollection().getQuery(index2).setQuery(null);

        instance.changeLayerVisibility(index2, true);
        assertTrue(instance.getTxQueryCollection().getQuery(index2).isVisible());
    }

    /**
     * Test of updateDescription, in LayersViewController
     */
    @Test
    public void testUpdateDescription() {
        LayersViewController instance = LayersViewController.getDefault().init(null);
        String description1 = "Vertex Description";
        int index1 = 2;
        Query query1 = new Query(GraphElementType.VERTEX, "Type == 'Event'");
        instance.getVxQueryCollection().getQuery(index1).setQuery(query1);
        instance.getTxQueryCollection().getQuery(index1).setQuery(null);

        instance.updateDescription(description1, index1);
        assertEquals(instance.getVxQueryCollection().getQuery(index1).getDescription(), description1);

        String description2 = "Transaction Description";
        int index2 = 3;
        Query query2 = new Query(GraphElementType.TRANSACTION, "Type == 'Network'");
        instance.getTxQueryCollection().getQuery(index2).setQuery(query2);
        instance.getVxQueryCollection().getQuery(index2).setQuery(null);

        instance.updateDescription(description2, index2);
        assertEquals(instance.getTxQueryCollection().getQuery(index2).getDescription(), description2);
    }

    /**
     * Test of updateQuery, in LayersViewController
     */
    @Test
    public void testUpdateQuery() {
        LayersViewController instance = LayersViewController.getDefault().init(null);
        String queryString1 = "Type == 'Word'";
        int index1 = 2;
        Query query1 = new Query(GraphElementType.VERTEX, "Type == 'Event'");
        instance.getVxQueryCollection().getQuery(index1).setQuery(query1);
        instance.getTxQueryCollection().getQuery(index1).setQuery(null);
        assertEquals(instance.getVxQueryCollection().getQuery(index1).getQueryString(), "Type == 'Event'");

        instance.updateQuery(queryString1, index1, "Vertex Query: ");
        assertEquals(instance.getVxQueryCollection().getQuery(index1).getQueryString(), queryString1);

        String queryString2 = "Type == 'Unknown'";
        int index2 = 3;
        Query query2 = new Query(GraphElementType.TRANSACTION, "Type == 'Network'");
        instance.getTxQueryCollection().getQuery(index2).setQuery(query2);
        instance.getVxQueryCollection().getQuery(index2).setQuery(null);
        assertEquals(instance.getTxQueryCollection().getQuery(index2).getQueryString(), "Type == 'Network'");
        
        instance.updateQuery(queryString2, index2, "Transaction Query: ");
        assertEquals(instance.getTxQueryCollection().getQuery(index2).getQueryString(), queryString2);

    }
    
}
