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
package au.gov.asd.tac.constellation.views.layers.utilities;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.LayersConcept;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.views.layers.query.BitMaskQuery;
import au.gov.asd.tac.constellation.views.layers.query.BitMaskQueryCollection;
import au.gov.asd.tac.constellation.views.layers.query.Query;
import au.gov.asd.tac.constellation.views.layers.shortcut.NewLayerPlugin;
import au.gov.asd.tac.constellation.views.layers.state.LayersViewConcept;
import au.gov.asd.tac.constellation.views.layers.state.LayersViewState;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for Layers Utilities 
 *
 * @author Delphinus8821
 */
public class LayersUtilitiesNGTest {
    
    private StoreGraph graph;

    private void setupGraph() {
        graph = new StoreGraph();

        // Create LayerMask attributes
        LayersConcept.VertexAttribute.LAYER_MASK.ensure(graph);
        LayersConcept.TransactionAttribute.LAYER_MASK.ensure(graph);

        // Create LayerVisilibity Attributes
        LayersConcept.VertexAttribute.LAYER_VISIBILITY.ensure(graph);
        LayersConcept.TransactionAttribute.LAYER_VISIBILITY.ensure(graph);
    }


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
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of calculateCurrentLayerSelectionBitMask method, of class LayersUtilities.
     * 
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testCalculateCurrentLayerSelectionBitMask() throws InterruptedException, PluginException {
        setupGraph();

        PluginExecution.withPlugin(new NewLayerPlugin()).executeNow(graph);

        final int layersViewStateAttributeId = LayersViewConcept.MetaAttribute.LAYERS_VIEW_STATE.get(graph);
        assertTrue(layersViewStateAttributeId != Graph.NOT_FOUND);

        final BitMaskQueryCollection vxQueries = new BitMaskQueryCollection(GraphElementType.VERTEX);
        final BitMaskQueryCollection txQueries = new BitMaskQueryCollection(GraphElementType.TRANSACTION);
        vxQueries.setQueries(BitMaskQueryCollection.getDefaultVxQueries());
        txQueries.setQueries(BitMaskQueryCollection.getDefaultTxQueries());

        // Test only one layer
        int result = LayersUtilities.calculateCurrentLayerSelectionBitMask(vxQueries, txQueries);
        int expected = 1;
        assertEquals(result, expected);

        // Test with 2 layers
        Query query1 = new Query(GraphElementType.VERTEX, "Type == 'Event'");
        BitMaskQuery bitMaskQuery1 = new BitMaskQuery(query1, 3, "Vertex");
        vxQueries.add(bitMaskQuery1);

        result = LayersUtilities.calculateCurrentLayerSelectionBitMask(vxQueries, txQueries);
        assertEquals(result, expected);

        // Test with transaction query 
        Query query2 = new Query(GraphElementType.TRANSACTION, "Type == 'Network'");
        BitMaskQuery bitMaskQuery2 = new BitMaskQuery(query2, 4, "Transaction");
        txQueries.add(bitMaskQuery2);

        result = LayersUtilities.calculateCurrentLayerSelectionBitMask(vxQueries, txQueries);
        assertEquals(result, expected);
    }

    /**
     * Test of addLayer method, of class LayersUtilities.
     */
    @Test
    public void testAddLayer() {
        LayersViewState state = new LayersViewState();
        LayersUtilities.addLayer(state);
        final int result = state.getLayerCount();

        assertEquals(result, 1);
    }

    /**
     * Test of addLayer method, of class LayersUtilities.
     */
    @Test
    public void testAddLayer_String() {
        LayersViewState state = new LayersViewState();
        String description = "Test layer";
        LayersUtilities.addLayer(state, description);

        final int result = state.getLayerCount();
        assertEquals(result, 1);

        final String descriptionResult = state.getVxQueriesCollection().getQuery(1).getDescription();
        assertEquals(descriptionResult, description);
    }

    /**
     * Test of addLayerAt method, of class LayersUtilities.
     */
    @Test
    public void testAddLayerAt() {
        LayersViewState state = new LayersViewState();
        String description = "Test layer";
        int layerNumber = 2;
        LayersUtilities.addLayerAt(state, description, layerNumber);

        final int result = state.getLayerCount();
        assertEquals(result, 2);

        final String descriptionResult = state.getVxQueriesCollection().getQuery(2).getDescription();
        assertEquals(descriptionResult, description);
    }
}
