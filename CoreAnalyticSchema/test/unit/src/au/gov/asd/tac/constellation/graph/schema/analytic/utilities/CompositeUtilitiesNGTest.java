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
package au.gov.asd.tac.constellation.graph.schema.analytic.utilities;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.objects.CompositeNodeState;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.objects.ContractedCompositeNodeState;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.objects.ExpandedCompositeNodeState;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import java.util.Arrays;
import java.util.List;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class CompositeUtilitiesNGTest {
    
    StoreGraph graph;
    
    int vxId1;
    int vxId2;
    int vxId3;
    int vxId4;
    
    int tId1;
    int tId2;
    
    int identifierVertexAttribute;
    int compositeStateVertexAttribute;
    int identifierTransactionAttribute;
    
    CompositeNodeState contractedState;
    CompositeNodeState expandedState1;
    CompositeNodeState expandedState2;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        graph = new StoreGraph(schema);
        
        vxId1 = graph.addVertex();
        vxId2 = graph.addVertex();
        vxId3 = graph.addVertex();
        vxId4 = graph.addVertex();
        
        tId1 = graph.addTransaction(vxId1, vxId2, true);
        tId2 = graph.addTransaction(vxId3, vxId2, true);
        
        identifierVertexAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        compositeStateVertexAttribute = AnalyticConcept.VertexAttribute.COMPOSITE_STATE.ensure(graph);
        VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        VisualConcept.VertexAttribute.X.ensure(graph);
        VisualConcept.VertexAttribute.Y.ensure(graph);
        VisualConcept.VertexAttribute.Z.ensure(graph);
        identifierTransactionAttribute = VisualConcept.TransactionAttribute.IDENTIFIER.ensure(graph);
        
        graph.setStringValue(identifierVertexAttribute, vxId1, "composite node");
        graph.setStringValue(identifierVertexAttribute, vxId2, "normal node");
        graph.setStringValue(identifierVertexAttribute, vxId3, "part of an expanded node");
        graph.setStringValue(identifierVertexAttribute, vxId4, "another part of an expanded node");
        
        graph.setStringValue(identifierTransactionAttribute, tId1, "connected to composite");
        graph.setStringValue(identifierTransactionAttribute, tId2, "connected to expanded");
        
        final RecordStore constituentNodeStore = new GraphRecordStore();
        constituentNodeStore.add();
        constituentNodeStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, "Vertex #0");
        constituentNodeStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.X, 1.0);
        constituentNodeStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.Y, 2.0);
        constituentNodeStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.Z, 3.0);
        constituentNodeStore.set(GraphRecordStoreUtilities.SOURCE + GraphRecordStoreUtilities.ID, "copy.Identifier<Vertex #0>Type<Unknown>");
        constituentNodeStore.add();
        constituentNodeStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, "Vertex #1");
        constituentNodeStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.X, -1.0);
        constituentNodeStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.Y, -2.0);
        constituentNodeStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.Z, -3.0);
        constituentNodeStore.set(GraphRecordStoreUtilities.SOURCE + GraphRecordStoreUtilities.ID, "copy.Identifier<Vertex #1>Type<Unknown>");
        
        final List<String> expandedIds = Arrays.asList("copy.Identifier<Vertex #0>Type<Unknown>", "copy.Identifier<Vertex #1>Type<Unknown>");
        
        final float[] mean = new float[]{1.5F ,1.0F, 0.5F};
        
        final ContractedCompositeNodeState contractedNodeState = new ContractedCompositeNodeState(constituentNodeStore, expandedIds, expandedIds, mean);
        contractedState = new CompositeNodeState(vxId1, contractedNodeState);
        
        final RecordStore compositeNodeStore = new GraphRecordStore();
        compositeNodeStore.add();
        compositeNodeStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, "Vertex #0 + 1 more...");
        compositeNodeStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.X, 1.0);
        compositeNodeStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.Y, 2.0);
        compositeNodeStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.Z, 3.0);
        compositeNodeStore.set(GraphRecordStoreUtilities.SOURCE + GraphRecordStoreUtilities.ID, "copy.Identifier<Vertex #0 + 1 more...>Type<Unknown>");
        
        final ExpandedCompositeNodeState expandedNodeState = new ExpandedCompositeNodeState(compositeNodeStore, "copy.Identifier<Vertex #0 + 1 more...>Type<Unknown>", true, 2);
        expandedState1 = new CompositeNodeState(vxId3, expandedNodeState);
        expandedState2 = new CompositeNodeState(vxId4, expandedNodeState);
        
        graph.setObjectValue(compositeStateVertexAttribute, vxId1, contractedState);
        graph.setObjectValue(compositeStateVertexAttribute, vxId3, expandedState1);
        graph.setObjectValue(compositeStateVertexAttribute, vxId4, expandedState2);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of destroyComposite method, of class CompositeUtilities. Destroy composite.
     */
    @Test
    public void testDestroyCompositeComposite() {
        System.out.println("destroyCompositeComposite");
        
        assertEquals(graph.getVertexCount(), 4);
        assertEquals(graph.getTransactionCount(), 2);
        assertNotNull(graph.getObjectValue(compositeStateVertexAttribute, vxId1));
        
        final List<Integer> result = CompositeUtilities.destroyComposite(graph, compositeStateVertexAttribute, identifierTransactionAttribute, vxId1);
        
        assertEquals(result.size(), 2);
        assertEquals(graph.getVertexCount(), 5);
        assertEquals(graph.getTransactionCount(), 3);
        
        final int newVxId1 = result.get(0);
        final int newVxId2 = result.get(1);
        
        assertEquals(graph.getStringValue(identifierVertexAttribute, newVxId1), "Vertex #1");
        assertNull(graph.getObjectValue(compositeStateVertexAttribute, newVxId1));
        assertEquals(graph.getVertexTransactionCount(newVxId1), 1);
        final int newTId1 = graph.getVertexTransaction(newVxId1, 0);
        assertEquals(graph.getStringValue(identifierTransactionAttribute, newTId1), "connected to composite");
        assertEquals(graph.getTransactionDestinationVertex(newTId1), vxId2);
           
        assertEquals(graph.getStringValue(identifierVertexAttribute, newVxId2), "Vertex #0");
        assertNull(graph.getObjectValue(compositeStateVertexAttribute, newVxId2));
        assertEquals(graph.getVertexTransactionCount(newVxId2), 1);
        final int newTId2 = graph.getVertexTransaction(newVxId2, 0);
        assertEquals(graph.getStringValue(identifierTransactionAttribute, newTId2), "connected to composite");
        assertEquals(graph.getTransactionDestinationVertex(newTId2), vxId2);
        
        // this last assert just confirms that they aren't the same transaction
        assertNotEquals(newTId1, newTId2);
    }
    
    /**
     * Test of destroyComposite method, of class CompositeUtilities. Destroy part of expanded.
     */
    @Test
    public void testDestroyCompositeExpanded() {
        System.out.println("destroyCompositeExpanded");
        
        assertEquals(graph.getVertexCount(), 4);
        assertEquals(graph.getTransactionCount(), 2);
        assertNotNull(graph.getObjectValue(compositeStateVertexAttribute, vxId3));
        assertNotNull(graph.getObjectValue(compositeStateVertexAttribute, vxId4));
        
        final List<Integer> result = CompositeUtilities.destroyComposite(graph, compositeStateVertexAttribute, identifierTransactionAttribute, vxId3);
        
        assertEquals(result.size(), 0);
        assertEquals(graph.getVertexCount(), 4);
        assertEquals(graph.getTransactionCount(), 2);
        
        assertNull(graph.getObjectValue(compositeStateVertexAttribute, vxId3));
        // since this node is part of the composite being destroyed, this should also have no reference to the old composiste state
        assertNull(graph.getObjectValue(compositeStateVertexAttribute, vxId4));
    }

    /**
     * Test of destroyAllComposites method, of class CompositeUtilities.
     */
    @Test
    public void testDestroyAllComposites() {
        System.out.println("destroyAllComposites");
        
        // this result of this test should effectively be the previous 2 tests put together
        assertEquals(graph.getVertexCount(), 4);
        assertEquals(graph.getTransactionCount(), 2);
        assertNotNull(graph.getObjectValue(compositeStateVertexAttribute, vxId1));
        assertNotNull(graph.getObjectValue(compositeStateVertexAttribute, vxId3));
        assertNotNull(graph.getObjectValue(compositeStateVertexAttribute, vxId4));
        
        final boolean result = CompositeUtilities.destroyAllComposites(graph);
        
        assertTrue(result);
        assertEquals(graph.getVertexCount(), 5);
        assertEquals(graph.getTransactionCount(), 3);
        
        for (int vertex = 0; vertex < graph.getVertexCount(); vertex++) {
            final int vxId = graph.getVertex(vertex);
            assertNull(graph.getObjectValue(compositeStateVertexAttribute, vxId));
        }
    }

    /**
     * Test of expandComposite method, of class CompositeUtilities.
     */
    @Test
    public void testExpandComposite() {
        System.out.println("expandComposite");
        
        assertEquals(graph.getVertexCount(), 4);
        assertEquals(graph.getTransactionCount(), 2);
        assertEquals(graph.getObjectValue(compositeStateVertexAttribute, vxId1), contractedState);
        
        final List<Integer> result = CompositeUtilities.expandComposite(graph, compositeStateVertexAttribute, vxId1);
        
        assertEquals(result.size(), 2);
        assertEquals(graph.getVertexCount(), 5);
        assertEquals(graph.getTransactionCount(), 3);
        
        final int newVxId1 = result.get(0);
        final int newVxId2 = result.get(1);
        
        assertEquals(graph.getStringValue(identifierVertexAttribute, newVxId1), "Vertex #1");
        assertNotNull(graph.getObjectValue(compositeStateVertexAttribute, newVxId1));
        assertNotEquals(graph.getObjectValue(compositeStateVertexAttribute, newVxId1), contractedState);
        assertEquals(graph.getVertexTransactionCount(newVxId1), 1);
        final int newTId1 = graph.getVertexTransaction(newVxId1, 0);
        assertEquals(graph.getStringValue(identifierTransactionAttribute, newTId1), "composite:false:false[copy.->]_connected to composite");
        assertEquals(graph.getTransactionDestinationVertex(newTId1), vxId2);
           
        assertEquals(graph.getStringValue(identifierVertexAttribute, newVxId2), "Vertex #0");
        assertNotNull(graph.getObjectValue(compositeStateVertexAttribute, newVxId2));
        assertNotEquals(graph.getObjectValue(compositeStateVertexAttribute, newVxId2), contractedState);
        assertEquals(graph.getVertexTransactionCount(newVxId2), 1);
        final int newTId2 = graph.getVertexTransaction(newVxId2, 0);
        assertEquals(graph.getStringValue(identifierTransactionAttribute, newTId2), "composite:false:false[copy.->]_connected to composite");
        assertEquals(graph.getTransactionDestinationVertex(newTId2), vxId2);
        
        // this last assert just confirms that they aren't the same transaction
        assertNotEquals(newTId1, newTId2);
    }

    /**
     * Test of expandAllComposites method, of class CompositeUtilities.
     */
    @Test
    public void testExpandAllComposites() {
        System.out.println("expandAllComposites");
        
        assertEquals(graph.getVertexCount(), 4);
        assertEquals(graph.getTransactionCount(), 2);
        assertEquals(graph.getObjectValue(compositeStateVertexAttribute, vxId1), contractedState);
        
        final boolean result = CompositeUtilities.expandAllComposites(graph);
        
        assertTrue(result);
        assertEquals(graph.getVertexCount(), 5);
        assertEquals(graph.getTransactionCount(), 3);
        
        for (int vertex = 0; vertex < graph.getVertexCount(); vertex++) {
            final int vxId = graph.getVertex(vertex);
            assertNotEquals(graph.getObjectValue(compositeStateVertexAttribute, vxId), contractedState);
        }
    }

    /**
     * Test of contractComposite method, of class CompositeUtilities.
     */
    @Test
    public void testContractComposite() {
        System.out.println("contractComposite");
        
        assertEquals(graph.getVertexCount(), 4);
        assertEquals(graph.getTransactionCount(), 2);
        assertEquals(graph.getObjectValue(compositeStateVertexAttribute, vxId3), expandedState1);
        assertEquals(graph.getObjectValue(compositeStateVertexAttribute, vxId4), expandedState2);
        
        final int resultId = CompositeUtilities.contractComposite(graph, compositeStateVertexAttribute, vxId3);
        
        assertEquals(graph.getVertexCount(), 3);
        assertEquals(graph.getTransactionCount(), 2);
        
        assertNotEquals(graph.getObjectValue(compositeStateVertexAttribute, resultId), expandedState1); 
        assertNotEquals(graph.getObjectValue(compositeStateVertexAttribute, resultId), expandedState2);
        
        assertEquals(graph.getVertexTransactionCount(resultId), 1);
        final int newTId = graph.getVertexTransaction(resultId, 0);
        assertEquals(graph.getStringValue(identifierTransactionAttribute, newTId), "composite:true:false[copy.->]_connected to expanded");
        assertEquals(graph.getTransactionDestinationVertex(newTId), vxId2);
    }

    /**
     * Test of contractAllComposites method, of class CompositeUtilities.
     */
    @Test
    public void testContractAllComposites() {
        System.out.println("contractAllComposites");
        
        assertEquals(graph.getVertexCount(), 4);
        assertEquals(graph.getTransactionCount(), 2);
        assertEquals(graph.getObjectValue(compositeStateVertexAttribute, vxId3), expandedState1);
        assertEquals(graph.getObjectValue(compositeStateVertexAttribute, vxId4), expandedState2);
        
        final boolean result = CompositeUtilities.contractAllComposites(graph);
        
        assertTrue(result);
        assertEquals(graph.getVertexCount(), 3);
        assertEquals(graph.getTransactionCount(), 2);
        
        for (int vertex = 0; vertex < graph.getVertexCount(); vertex++) {
            final int vxId = graph.getVertex(vertex);
            assertNotEquals(graph.getObjectValue(compositeStateVertexAttribute, vxId), expandedState1);
            assertNotEquals(graph.getObjectValue(compositeStateVertexAttribute, vxId), expandedState2);
        }
    }

    /**
     * Test of makeComposite method, of class CompositeUtilities.
     */
    @Test
    public void testMakeComposite() {
        System.out.println("makeComposite");
        
        assertEquals(graph.getVertexCount(), 4);
        assertEquals(graph.getTransactionCount(), 2);
        assertEquals(graph.getObjectValue(compositeStateVertexAttribute, vxId1), contractedState);
        assertNull(graph.getObjectValue(compositeStateVertexAttribute, vxId2));
        assertEquals(graph.getObjectValue(compositeStateVertexAttribute, vxId3), expandedState1);
        assertEquals(graph.getObjectValue(compositeStateVertexAttribute, vxId4), expandedState2);
        
        // forming a composite with vxId2, and vxId4
        // since vxId4 is already a part of a composite, the composite it is a part of should be destroyed
        // in the process
        final List<Integer> comprisingIds = Arrays.asList(vxId2, vxId4);
        CompositeUtilities.makeComposite(graph, comprisingIds, vxId2);
        
        assertEquals(graph.getVertexCount(), 3);
        assertEquals(graph.getTransactionCount(), 2);
        assertEquals(graph.getObjectValue(compositeStateVertexAttribute, vxId1), contractedState);
        // vxId3 was in a composite with vxId4, so it should've been destroyed
        assertNull(graph.getObjectValue(compositeStateVertexAttribute, vxId3));
        
        // we give it a default value knowing fully well that we will be replaced
        // but the compiler doesn't realise that we will definitely change it, so its necessary
        int newVxId = vxId2;
        // since we don't have the id for the newly formed composite vertex
        // we do this to get it, by identifying the one we didn't already know existed
        for (int vertex = 0; vertex < graph.getVertexCount(); vertex++) {
            final int vxId = graph.getVertex(vertex);
            if (vxId != vxId1 && vxId != vxId3) {
                newVxId = vxId;
                break;
            }
        }
        
        assertNotNull(graph.getObjectValue(compositeStateVertexAttribute, newVxId));
        assertNotEquals(graph.getObjectValue(compositeStateVertexAttribute, newVxId), expandedState2); 
        
        assertEquals(graph.getVertexTransactionCount(newVxId), 2);
        // both transactions on the graph were originally directed to vxId2
        // so they should now both be directed to the new composite node
        for (int transaction = 0; transaction < graph.getVertexTransactionCount(newVxId); transaction++) {
            final int tId = graph.getVertexTransaction(newVxId, transaction);
            assertEquals(graph.getTransactionDestinationVertex(tId), newVxId);   
        }
    }  
}
