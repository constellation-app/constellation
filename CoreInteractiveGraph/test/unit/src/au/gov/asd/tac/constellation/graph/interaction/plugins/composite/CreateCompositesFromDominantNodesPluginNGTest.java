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
package au.gov.asd.tac.constellation.graph.interaction.plugins.composite;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.objects.CompositeNodeState;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.objects.ExpandedCompositeNodeState;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import static org.testng.Assert.assertEquals;
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
public class CreateCompositesFromDominantNodesPluginNGTest {
    
    private StoreGraph graph;
    
    private int vxId1;
    private int vxId2;
    private int vxId3;
    
    private int tId1;
    private int tId2;
    
    private int identifierVertexAttribute;
    private int typeVertexAttribute;
    private int selectedVertexAttribute;
    private int compositeStateVertexAttribute;
    private int identifierTransactionAttribute;
    private int typeTransactionAttribute;
    
    private CompositeNodeState expandedState1;
    private CompositeNodeState expandedState2;
    
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
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        graph = new StoreGraph(schema);
        
        vxId1 = graph.addVertex();
        vxId2 = graph.addVertex();
        vxId3 = graph.addVertex();
        
        tId1 = graph.addTransaction(vxId2, vxId1, true);
        tId2 = graph.addTransaction(vxId2, vxId3, false);
        
        identifierVertexAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        typeVertexAttribute = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
        selectedVertexAttribute = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        compositeStateVertexAttribute = AnalyticConcept.VertexAttribute.COMPOSITE_STATE.ensure(graph);
        VisualConcept.VertexAttribute.X.ensure(graph);
        VisualConcept.VertexAttribute.Y.ensure(graph);
        VisualConcept.VertexAttribute.Z.ensure(graph);
        identifierTransactionAttribute = VisualConcept.TransactionAttribute.IDENTIFIER.ensure(graph);
        typeTransactionAttribute = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);
        
        graph.setStringValue(identifierVertexAttribute, vxId1, "normal node");
        graph.setStringValue(identifierVertexAttribute, vxId2, "part of an expanded node");
        graph.setStringValue(identifierVertexAttribute, vxId3, "another part of an expanded node");
        
        graph.setObjectValue(typeVertexAttribute, vxId1, AnalyticConcept.VertexType.EVENT);
        graph.setObjectValue(typeVertexAttribute, vxId2, AnalyticConcept.VertexType.EVENT);
        graph.setObjectValue(typeVertexAttribute, vxId3, AnalyticConcept.VertexType.EVENT);
        
        graph.setStringValue(identifierTransactionAttribute, tId1, "connected to expanded");
        
        graph.setObjectValue(typeTransactionAttribute, tId2, AnalyticConcept.TransactionType.CORRELATION);
        
        final RecordStore compositeNodeStore = new GraphRecordStore();
        compositeNodeStore.add();
        compositeNodeStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, "Vertex #0 + 1 more...");
        compositeNodeStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.X, 1.0);
        compositeNodeStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.Y, 2.0);
        compositeNodeStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.Z, 3.0);
        compositeNodeStore.set(GraphRecordStoreUtilities.SOURCE + GraphRecordStoreUtilities.ID, "copy.Identifier<Vertex #0 + 1 more...>Type<Unknown>");
        
        final ExpandedCompositeNodeState expandedNodeState = new ExpandedCompositeNodeState(compositeNodeStore, "copy.Identifier<Vertex #0 + 1 more...>Type<Unknown>", true, 2);
        expandedState1 = new CompositeNodeState(vxId2, expandedNodeState);
        expandedState2 = new CompositeNodeState(vxId3, expandedNodeState);
        
        graph.setObjectValue(compositeStateVertexAttribute, vxId2, expandedState1);
        graph.setObjectValue(compositeStateVertexAttribute, vxId3, expandedState2);
        
        // having this node selected so that it is easier to verify things later
        graph.setBooleanValue(selectedVertexAttribute, vxId3, true);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of edit method, of class CreateCompositesFromDominantNodesPlugin.
     * 
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testEdit() throws InterruptedException, PluginException {
        System.out.println("edit");
        
        assertEquals(graph.getVertexCount(), 3);
        assertEquals(graph.getTransactionCount(), 2);
        assertNull(graph.getObjectValue(compositeStateVertexAttribute, vxId1));
        assertEquals(graph.getObjectValue(compositeStateVertexAttribute, vxId2), expandedState1);
        assertEquals(graph.getObjectValue(compositeStateVertexAttribute, vxId3), expandedState2);
        
        final CreateCompositesFromDominantNodesPlugin instance = new CreateCompositesFromDominantNodesPlugin();
        instance.edit(graph, null, null);
        
        assertEquals(graph.getVertexCount(), 2);
        assertEquals(graph.getTransactionCount(), 1);
        // node ids will have changed during the process
        // but we can verify that the selected node has a composite state, and the other node no longer has a composite state
        for (int i = 0; i < graph.getVertexCount(); i++) {
            final int vxId = graph.getVertex(i);
            final CompositeNodeState vxIdCompositeState = graph.getObjectValue(compositeStateVertexAttribute, vxId);
            if (graph.getBooleanValue(selectedVertexAttribute, vxId)) {
                assertNotNull(vxIdCompositeState);
                assertTrue(vxIdCompositeState.isComposite());
            } else {
                assertNull(vxIdCompositeState);
            }
        }
    }
}
