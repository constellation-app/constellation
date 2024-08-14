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
package au.gov.asd.tac.constellation.views.layers.query;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.LayersConcept;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.value.values.IntValue;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.views.layers.LayersViewController;
import au.gov.asd.tac.constellation.views.layers.state.LayersViewConcept;
import au.gov.asd.tac.constellation.views.layers.state.LayersViewState;
import au.gov.asd.tac.constellation.views.layers.utilities.LayersUtilities;
import au.gov.asd.tac.constellation.views.layers.utilities.UpdateLayerSelectionPlugin;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.openide.util.Exceptions;
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

    private int layerMaskV, layerMaskT, layerVisibilityV, layerVisibilityT, selectedV, selectedT, vertexLabelAttribute, txnLabelAttribute;
    private int vxId1, vxId2, vxId3, vxId4, txId1, txId2, txId3;
    private Graph graph;
    private final BitMaskQueryCollection vxBitMaskCollection = new BitMaskQueryCollection(GraphElementType.VERTEX);
    private final BitMaskQueryCollection txBitMaskCollection = new BitMaskQueryCollection(GraphElementType.TRANSACTION);
    
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
     * Set up a graph with four vertices and three transactions
     */
    private void setupGraph() {
        graph = new DualGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());
        
        vxBitMaskCollection.setQueries(BitMaskQueryCollection.getDefaultVxQueries());
        txBitMaskCollection.setQueries(BitMaskQueryCollection.getDefaultTxQueries());

        WritableGraph wg;
        try {
            wg = graph.getWritableGraph("", true);
        
            // add attributes
            int vertexIdentifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(wg);
            vertexLabelAttribute = VisualConcept.VertexAttribute.LABEL.ensure(wg);
            txnLabelAttribute = VisualConcept.TransactionAttribute.LABEL.ensure(wg);

            // Create LayerMask attributes
            layerMaskV = LayersConcept.VertexAttribute.LAYER_MASK.ensure(wg);
            layerMaskT = LayersConcept.TransactionAttribute.LAYER_MASK.ensure(wg);

            // Create LayerVisilibity Attributes
            layerVisibilityV = LayersConcept.VertexAttribute.LAYER_VISIBILITY.ensure(wg);
            layerVisibilityT = LayersConcept.TransactionAttribute.LAYER_VISIBILITY.ensure(wg);

            // Create Selected Attributes
            selectedV = VisualConcept.VertexAttribute.SELECTED.ensure(wg);
            selectedT = VisualConcept.TransactionAttribute.SELECTED.ensure(wg);

            // add vertices
            vxId1 = wg.addVertex();
            vxId2 = wg.addVertex();
            vxId3 = wg.addVertex();
            vxId4 = wg.addVertex();

            // set the identifier of each vertex to something unique
            wg.setStringValue(vertexIdentifierAttribute, vxId1, "V1");
            wg.setStringValue(vertexIdentifierAttribute, vxId2, "V2");
            wg.setStringValue(vertexIdentifierAttribute, vxId3, "V3");
            wg.setStringValue(vertexIdentifierAttribute, vxId4, "V4");

            wg.setFloatValue(layerVisibilityV, vxId1, 1.0f);
            wg.setFloatValue(layerVisibilityV, vxId2, 1.0f);
            wg.setFloatValue(layerVisibilityV, vxId3, 1.0f);
            wg.setFloatValue(layerVisibilityV, vxId4, 1.0f);

            wg.setIntValue(layerMaskV, vxId1, 1); // no layer
            wg.setIntValue(layerMaskV, vxId2, 1); // no layer
            wg.setIntValue(layerMaskV, vxId3, 1); // no layer
            wg.setIntValue(layerMaskV, vxId4, 1); // no layer

            wg.setBooleanValue(selectedV, vxId1, false);
            wg.setBooleanValue(selectedV, vxId2, false);
            wg.setBooleanValue(selectedV, vxId3, false);
            wg.setBooleanValue(selectedV, vxId4, false);
            
            // Adding 3 Transactions - not selected, layer 1, visible
            txId1 = wg.addTransaction(vxId1, vxId2, true);
            wg.setIntValue(layerMaskT, txId1, 1);
            wg.setFloatValue(layerVisibilityT, txId1, 1.0f);
            wg.setBooleanValue(selectedT, txId1, false);

            txId2 = wg.addTransaction(vxId2, vxId3, false);
            wg.setIntValue(layerMaskT, txId2, 1);
            wg.setFloatValue(layerVisibilityT, vxId2, 1.0f);
            wg.setBooleanValue(selectedT, txId2, false);

            txId3 = wg.addTransaction(vxId3, vxId4, true);
            wg.setIntValue(layerMaskT, txId3, 1);
            wg.setFloatValue(layerVisibilityT, txId3, 1.0f);
            wg.setBooleanValue(selectedT, txId3, false);

            wg.commit();
        } catch (final InterruptedException ex) {
            Exceptions.printStackTrace(ex);
            Thread.currentThread().interrupt();
        }
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
     * Test of updateBitMask method, of class BitMaskQueryCollection.
     */
    @Test
    public void testCombineBitmap() {
        setupGraph();
        final GraphManager gm = Mockito.mock(GraphManager.class);
        when(gm.getActiveGraph()).thenReturn(graph);

        try (MockedStatic<GraphManager> mockedStatic = Mockito.mockStatic(GraphManager.class)) {
            mockedStatic.when(() -> GraphManager.getDefault()).thenReturn(gm);

            WritableGraph wg;
            try {
                wg = graph.getWritableGraph("", true);
                
                Query selQuery1 = new Query(GraphElementType.VERTEX, "Label contains '.5'");
                Query selQuery2 = new Query(GraphElementType.VERTEX, "Label contains \"1\"");
                Query selQuery3 = new Query(GraphElementType.TRANSACTION, "Label contains '.'");
                
                createLayer(wg, selQuery1, null, 1, "Point5");
                createLayer(wg, null, null, 2, null);
                createLayer(wg, null, null, 3, null);
                createLayer(wg, selQuery2, selQuery3, 4, "One");
                
                changeLayerVisibility(wg, 1, true); // query: v id .5
                changeLayerVisibility(wg, 2, true);
                changeLayerVisibility(wg, 3, true);
                changeLayerVisibility(wg, 4, true); // query: V Label 1 , T label .

                // test union and intersection modes based only on allocated layers to each vertex
                wg.setIntValue(layerMaskV, vxId1, 1); // base layer only
                wg.setIntValue(layerMaskV, vxId2, 3); // layer 1
                wg.setIntValue(layerMaskV, vxId3, 5); // layer 2
                wg.setIntValue(layerMaskV, vxId4, 11); // layers 1 and 3
                wg.setIntValue(layerMaskT, txId1, 7); // layers 1 and 2
                wg.setIntValue(layerMaskT, txId2, 9); // layer 3
                wg.setIntValue(layerMaskT, txId3, 17); // layer 4
                
                System.out.println("\n==== UNION ELEMENT TEST ====");

                sessionEdit(true, 10); // layers 1,3 selected, union mode
                
                // direct allocation matches for any element on either layer 1 or 3 will be visible.
                // union of results for visible vertices: vtx2, vtx4 , and txn1 txn2
                
                assertEquals(0.0f, wg.getFloatValue(layerVisibilityV, vxId1));
                assertEquals(1.0f, wg.getFloatValue(layerVisibilityV, vxId2));
                assertEquals(0.0f, wg.getFloatValue(layerVisibilityV, vxId3));
                assertEquals(1.0f, wg.getFloatValue(layerVisibilityV, vxId4));
                assertEquals(1.0f, wg.getFloatValue(layerVisibilityT, txId1));
                assertEquals(1.0f, wg.getFloatValue(layerVisibilityT, txId2));
                assertEquals(0.0f, wg.getFloatValue(layerVisibilityT, txId3));
                
                System.out.println("\n==== INTERSECTION ELEMENT TEST ====");

                sessionEdit(false, 10); // layers 1,3 selected, intersection mode
                
                // direct allocation matches for any element on both layers (1 and 3) will be visible.
                // intersection of results for visible elements:  vtx4
                
                assertEquals(0.0f, wg.getFloatValue(layerVisibilityV, vxId1));
                assertEquals(0.0f, wg.getFloatValue(layerVisibilityV, vxId2));
                assertEquals(0.0f, wg.getFloatValue(layerVisibilityV, vxId3));
                assertEquals(1.0f, wg.getFloatValue(layerVisibilityV, vxId4));
                assertEquals(0.0f, wg.getFloatValue(layerVisibilityT, txId1));
                assertEquals(0.0f, wg.getFloatValue(layerVisibilityT, txId2));
                assertEquals(0.0f, wg.getFloatValue(layerVisibilityT, txId3));
                
                // set the label of each vertex to something testable
                wg.setStringValue(vertexLabelAttribute, vxId1, "Label_1");
                wg.setStringValue(vertexLabelAttribute, vxId2, "Label_2.5");
                wg.setStringValue(vertexLabelAttribute, vxId3, "Label_3");
                wg.setStringValue(vertexLabelAttribute, vxId4, "Label_14.5");
                
                // set the label of each txn to something testable
                wg.setStringValue(txnLabelAttribute, txId1, "TxLabel_1");
                wg.setStringValue(txnLabelAttribute, txId2, "TxLabel_2.25");
                wg.setStringValue(txnLabelAttribute, txId3, "TxLabel_3.5");
                
                wg.setIntValue(layerMaskV, vxId1, 1); // base layer only
                wg.setIntValue(layerMaskV, vxId2, 1); // base layer only
                wg.setIntValue(layerMaskV, vxId3, 1); // base layer only
                wg.setIntValue(layerMaskV, vxId4, 1); // base layer only         
                wg.setIntValue(layerMaskT, txId1, 1); // base layer only
                wg.setIntValue(layerMaskT, txId2, 1); // base layer only
                wg.setIntValue(layerMaskT, txId3, 1); // base layer only
                
                changeLayerVisibility(wg, 1, true); // query: v id .5
                changeLayerVisibility(wg, 2, true);
                changeLayerVisibility(wg, 3, true);
                changeLayerVisibility(wg, 4, true); // query: V Label 1 , T label .

                System.out.println("\n==== UNION QUERY TEST ====");
                
                sessionEdit(true, 30); // layers 1,2,3,4 selected, union mode
                
                // Only the query matches for any layer will be visible.
                // vertex 1 matches layer 4 query, vertices 2 and 4 match layer 1 query
                // transactions txn2 and txn3 match layer 4 query 
                // union of results for visible vertices: vtx1, vtx2, vtx4 , and txn2, txn3
                
                assertEquals(1.0f, wg.getFloatValue(layerVisibilityV, vxId1));
                assertEquals(1.0f, wg.getFloatValue(layerVisibilityV, vxId2));
                assertEquals(0.0f, wg.getFloatValue(layerVisibilityV, vxId3));
                assertEquals(1.0f, wg.getFloatValue(layerVisibilityV, vxId4));
                assertEquals(0.0f, wg.getFloatValue(layerVisibilityT, txId1));
                assertEquals(1.0f, wg.getFloatValue(layerVisibilityT, txId2));
                assertEquals(1.0f, wg.getFloatValue(layerVisibilityT, txId3));
                
                wg.setIntValue(layerMaskV, vxId1, 3); // assign ro layer 1
                wg.setIntValue(layerMaskV, vxId3, 3); // assign ro layer 1
                wg.setIntValue(layerMaskT, txId1, 3); // assign to layer 1
                wg.setIntValue(layerMaskT, txId3, 3); // assign to layer 1
                
                System.out.println("\n==== INTERSECTION QUERY TEST ====");

                sessionEdit(false, 18); // layers 1 and 4 selected, intersection mode
                
                // vertex 1 and txn 3 should be visible ... they are both assigned to layer 2, and both match a layer 5 query
                // vertx 4 should also be visible as it passes the query for layer 2, and also the query for layer 5
                // intersection of results: vtx1 , vtx4,  txn3

                assertEquals(1.0f, wg.getFloatValue(layerVisibilityV, vxId1));
                assertEquals(0.0f, wg.getFloatValue(layerVisibilityV, vxId2));
                assertEquals(0.0f, wg.getFloatValue(layerVisibilityV, vxId3));
                assertEquals(1.0f, wg.getFloatValue(layerVisibilityV, vxId4));
                assertEquals(0.0f, wg.getFloatValue(layerVisibilityT, txId1));
                assertEquals(0.0f, wg.getFloatValue(layerVisibilityT, txId2));
                assertEquals(1.0f, wg.getFloatValue(layerVisibilityT, txId3));
                
                wg.commit();

            } catch (final InterruptedException ex) {
                Exceptions.printStackTrace(ex);
                Thread.currentThread().interrupt();
            }
        }
    }
    
    private void sessionEdit(boolean mode, long graphMask) {
        try {
            WritableGraph wg = graph.getWritableGraph(" sess ed ", mode);
            final int graphCurrentBitMaskAttrId = LayersViewConcept.GraphAttribute.LAYER_MASK_SELECTED.ensure(wg);
            wg.setLongValue(graphCurrentBitMaskAttrId, 0, graphMask);
            final long currentBitmask = wg.getLongValue(graphCurrentBitMaskAttrId, 0);

            final int vxbitmaskAttrId = LayersConcept.VertexAttribute.LAYER_MASK.get(wg);
            final int vxbitmaskVisibilityAttrId = LayersConcept.VertexAttribute.LAYER_VISIBILITY.get(wg);

            final int txbitmaskAttrId = LayersConcept.TransactionAttribute.LAYER_MASK.get(wg);
            final int txbitmaskVisibilityAttrId = LayersConcept.TransactionAttribute.LAYER_VISIBILITY.get(wg);

            final int stateAttributeId = LayersViewConcept.MetaAttribute.LAYERS_VIEW_STATE.ensure(wg);
            final LayersViewState currentState = wg.getObjectValue(stateAttributeId, 0);
            if (currentState != null) {
                currentState.getVxQueriesCollection().setActiveQueries(currentBitmask);
                currentState.getVxQueriesCollection().updateBitMasks(wg, vxbitmaskAttrId, vxbitmaskVisibilityAttrId, mode);
                currentState.getTxQueriesCollection().setActiveQueries(currentBitmask);
                currentState.getTxQueriesCollection().updateBitMasks(wg, txbitmaskAttrId, txbitmaskVisibilityAttrId, mode);
                currentState.extractLayerAttributes(wg);
                LayersViewController.getDefault().updateListenedAttributes();
            }
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        
    }

    public void changeLayerVisibility(GraphWriteMethods gwm, final int index, final boolean isVisible) {
        final BitMaskQuery vxQuery = vxBitMaskCollection.getQuery(index);
        final BitMaskQuery txQuery = txBitMaskCollection.getQuery(index);
        
        if (vxQuery != null) {
            vxQuery.setVisibility(isVisible);
        }
        if (txQuery != null) {
            txQuery.setVisibility(isVisible);
        }
        if (isVisible) {
            IntValue iv = new IntValue();
            iv.writeInt(index);        
            vxQuery.update(gwm, iv);
        }
        execute();
        writeState();
    }

    public void execute() {
        final int newBitmask = LayersUtilities.calculateCurrentLayerSelectionBitMask(vxBitMaskCollection, txBitMaskCollection);
        PluginExecution.withPlugin(new UpdateLayerSelectionPlugin(newBitmask))
                .executeLater(GraphManager.getDefault().getActiveGraph());
    }
    
    private void createLayer(GraphWriteMethods gwm, Query predefQuery, Query newTxQuery, int layerNumber, String desc) {
        if (layerNumber <= BitMaskQueryCollection.MAX_QUERY_AMT) {
            final Query vxQuery = new Query(GraphElementType.VERTEX, "");
            vxBitMaskCollection.add(predefQuery != null ? predefQuery : vxQuery, layerNumber, desc);
            if (predefQuery != null) {
                IntValue iv = new IntValue();
                iv.writeInt(layerNumber);
                vxBitMaskCollection.getQuery(layerNumber).getQuery().compile(gwm, iv);
            }
            final Query txQuery = new Query(GraphElementType.TRANSACTION, "");
            txBitMaskCollection.add(newTxQuery != null ? newTxQuery : txQuery, layerNumber, null);
            if (newTxQuery != null) {
                IntValue iv = new IntValue();
                iv.writeInt(layerNumber);
                txBitMaskCollection.getQuery(layerNumber).getQuery().compile(gwm, iv);
            }
        }
    }
    
    private void writeState() {
        try {
            WritableGraph wg = graph.getWritableGraph(" sess ed ", false);
            final int stateAttributeId = LayersViewConcept.MetaAttribute.LAYERS_VIEW_STATE.ensure(wg);
            LayersViewState currentState = wg.getObjectValue(stateAttributeId, 0);
            if (currentState == null) {
                currentState = new LayersViewState();
                currentState.setVxLayers(BitMaskQueryCollection.getDefaultVxQueries());
                currentState.setTxLayers(BitMaskQueryCollection.getDefaultTxQueries());
            } else {
                currentState = new LayersViewState(currentState);
            }
            currentState.setVxLayers(vxBitMaskCollection.getQueries());
            currentState.setTxLayers(txBitMaskCollection.getQueries());
            if (currentState.getVxQueriesCollection().getHighestQueryIndex() == 0 && currentState.getTxQueriesCollection().getHighestQueryIndex() == 0) {
                currentState.setVxLayers(BitMaskQueryCollection.getDefaultVxQueries());
                currentState.setTxLayers(BitMaskQueryCollection.getDefaultTxQueries());
            }

            wg.setObjectValue(stateAttributeId, 0, currentState);
            wg.commit();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
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
