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
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.objects.ContractedCompositeNodeState;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.objects.ExpandedCompositeNodeState;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import java.util.Arrays;
import java.util.List;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class DestroyAllCompositesPluginNGTest {
    
    private StoreGraph graph;
    
    private int vxId1;
    private int vxId2;
    private int vxId3;
    private int vxId4;
    
    private int tId1;
    private int tId2;
    
    private int identifierVertexAttribute;
    private int compositeStateVertexAttribute;
    private int identifierTransactionAttribute;
    
    private CompositeNodeState contractedState;
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
        // Not currently required
    }

    /**
     * Test of edit method, of class DestroyAllCompositesPlugin.
     * 
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testEdit() throws InterruptedException, PluginException {
        System.out.println("edit");
         
        assertEquals(graph.getVertexCount(), 4);
        assertEquals(graph.getTransactionCount(), 2);
        assertNotNull(graph.getObjectValue(compositeStateVertexAttribute, vxId1));
        assertNotNull(graph.getObjectValue(compositeStateVertexAttribute, vxId3));
        assertNotNull(graph.getObjectValue(compositeStateVertexAttribute, vxId4));
        
        final DestroyAllCompositesPlugin instance = new DestroyAllCompositesPlugin();
        instance.edit(graph, null, null);
        
        assertEquals(graph.getVertexCount(), 5);
        assertEquals(graph.getTransactionCount(), 3);
        
        for (int vertex = 0; vertex < graph.getVertexCount(); vertex++) {
            final int vxId = graph.getVertex(vertex);
            assertNull(graph.getObjectValue(compositeStateVertexAttribute, vxId));
        }
    }
}
