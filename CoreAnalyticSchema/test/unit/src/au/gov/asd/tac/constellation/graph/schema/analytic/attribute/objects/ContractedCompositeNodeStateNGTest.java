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
package au.gov.asd.tac.constellation.graph.schema.analytic.attribute.objects;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import java.util.Arrays;
import java.util.List;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
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
public class ContractedCompositeNodeStateNGTest {
    
    private RecordStore recordStore;
    private List<String> expandedIds;
    private float[] mean;
    
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
        recordStore = new GraphRecordStore();
        recordStore.add();
        recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, "Vertex #0");
        recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.X, 1.0);
        recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.Y, 2.0);
        recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.Z, 3.0);
        recordStore.set(GraphRecordStoreUtilities.SOURCE + GraphRecordStoreUtilities.ID, "copy.Identifier<Vertex #0>Type<Unknown>");
        recordStore.add();
        recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, "Vertex #1");
        recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.X, -1.0);
        recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.Y, -2.0);
        recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.Z, -3.0);
        recordStore.set(GraphRecordStoreUtilities.SOURCE + GraphRecordStoreUtilities.ID, "copy.Identifier<Vertex #1>Type<Unknown>");
        
        expandedIds = Arrays.asList("copy.Identifier<Vertex #0>Type<Unknown>", "copy.Identifier<Vertex #1>Type<Unknown>");
        
        mean = new float[]{1.5F ,1.0F, 0.5F};
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of getNumberOfNodes method, of class ContractedCompositeNodeState.
     */
    @Test
    public void testGetNumberOfNodes() {
        System.out.println("getNumberOfNodes");
        
        final ContractedCompositeNodeState instance = new ContractedCompositeNodeState(recordStore, expandedIds, expandedIds, mean);
        assertEquals(instance.getNumberOfNodes(), 2);
    }

    /**
     * Test of expand method, of class ContractedCompositeNodeState.
     */
    @Test
    public void testExpand() {
        System.out.println("expand");
        
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        final StoreGraph graph = new StoreGraph(schema);
        
        final int vxId = graph.addVertex();
        
        final int identifierVertexAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        final int compositeStateVertexAttribute = AnalyticConcept.VertexAttribute.COMPOSITE_STATE.ensure(graph);
        final int selectedVertexAttribute = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        final int xVertexAttribute = VisualConcept.VertexAttribute.X.ensure(graph);
        final int yVertexAttribute = VisualConcept.VertexAttribute.Y.ensure(graph);
        final int zVertexAttribute = VisualConcept.VertexAttribute.Z.ensure(graph);
        VisualConcept.TransactionAttribute.IDENTIFIER.ensure(graph);
        
        final ContractedCompositeNodeState instance = new ContractedCompositeNodeState(recordStore, expandedIds, expandedIds, mean);
        final CompositeNodeState state = new CompositeNodeState(vxId, instance);
        
        graph.setStringValue(identifierVertexAttribute, vxId, "my composite vertex");
        graph.setObjectValue(compositeStateVertexAttribute, vxId, state);
        graph.setBooleanValue(selectedVertexAttribute, vxId, true);
        graph.setFloatValue(xVertexAttribute, vxId, 3F);
        graph.setFloatValue(yVertexAttribute, vxId, 4F);
        graph.setFloatValue(zVertexAttribute, vxId, 5F);
        
        assertEquals(graph.getVertexCount(), 1);
        
        final List<Integer> result = instance.expand(graph, vxId);
        
        assertEquals(graph.getVertexCount(), 2);
        assertEquals(result.size(), 2);
        
        final int newVxId1 = result.get(0);
        final int newVxId2 = result.get(1);
        
        assertEquals(graph.getStringValue(identifierVertexAttribute, newVxId1), "Vertex #1");
        assertNotEquals(graph.getObjectValue(compositeStateVertexAttribute, newVxId1), state);
        assertTrue(graph.getBooleanValue(selectedVertexAttribute, newVxId1));
        assertEquals(graph.getFloatValue(xVertexAttribute, newVxId1), 0.5F);
        assertEquals(graph.getFloatValue(yVertexAttribute, newVxId1), 1.0F);
        assertEquals(graph.getFloatValue(zVertexAttribute, newVxId1), 1.5F);
        
        assertEquals(graph.getStringValue(identifierVertexAttribute, newVxId2), "Vertex #0");
        assertNotEquals(graph.getObjectValue(compositeStateVertexAttribute, newVxId2), state);
        assertTrue(graph.getBooleanValue(selectedVertexAttribute, newVxId2));
        assertEquals(graph.getFloatValue(xVertexAttribute, newVxId2), 2.5F);
        assertEquals(graph.getFloatValue(yVertexAttribute, newVxId2), 5F);
        assertEquals(graph.getFloatValue(zVertexAttribute, newVxId2), 7.5F);
    }
}
