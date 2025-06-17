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

import au.gov.asd.tac.constellation.graph.GraphElementType;
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
import java.util.List;
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
public class CreateCompositeFromSelectionPluginNGTest {
    
    private StoreGraph graph;
    
    private int vxId1;
    private int vxId2;
    private int vxId3;
    
    private int tId1;
    
    private int identifierVertexAttribute;
    private int selectedVertexAttribute;
    private int compositeStateVertexAttribute;
    private int identifierTransactionAttribute;
    
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
        
        identifierVertexAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        selectedVertexAttribute = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        compositeStateVertexAttribute = AnalyticConcept.VertexAttribute.COMPOSITE_STATE.ensure(graph);
        VisualConcept.VertexAttribute.X.ensure(graph);
        VisualConcept.VertexAttribute.Y.ensure(graph);
        VisualConcept.VertexAttribute.Z.ensure(graph);
        identifierTransactionAttribute = VisualConcept.TransactionAttribute.IDENTIFIER.ensure(graph);
        
        graph.setStringValue(identifierVertexAttribute, vxId1, "normal node");
        graph.setStringValue(identifierVertexAttribute, vxId2, "part of an expanded node");
        graph.setStringValue(identifierVertexAttribute, vxId3, "another part of an expanded node");
        
        graph.setStringValue(identifierTransactionAttribute, tId1, "connected to expanded");
        
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
        
        graph.setBooleanValue(selectedVertexAttribute, vxId1, true);
        graph.setBooleanValue(selectedVertexAttribute, vxId2, true);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of edit method, of class CreateCompositeFromSelectionPlugin.
     * 
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testEdit() throws InterruptedException, PluginException {
        System.out.println("edit");
        
        assertEquals(graph.getVertexCount(), 3);
        assertEquals(graph.getTransactionCount(), 1);
        assertNull(graph.getObjectValue(compositeStateVertexAttribute, vxId1));
        assertEquals(graph.getObjectValue(compositeStateVertexAttribute, vxId2), expandedState1);
        assertEquals(graph.getObjectValue(compositeStateVertexAttribute, vxId3), expandedState2);
        
        final CreateCompositeFromSelectionPlugin instance = new CreateCompositeFromSelectionPlugin();
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

    /**
     * Test of getItems method, of class CreateCompositeFromSelectionPlugin.
     */
    @Test
    public void testGetItems() {
        System.out.println("getItems");
        
        final CreateCompositeFromSelectionPlugin instance = new CreateCompositeFromSelectionPlugin();
        
        // vxId2 is selected so this should give the option to composite
        final List<String> result2 = instance.getItems(graph, GraphElementType.VERTEX, vxId2);
        assertEquals(result2.size(), 1);
        assertEquals(result2.get(0), "Composite Selected Nodes");
        
        // vxId3 isn't selected so no options should be presented
        final List<String> result3 = instance.getItems(graph, GraphElementType.VERTEX, vxId3);
        assertTrue(result3.isEmpty());
    }
}
