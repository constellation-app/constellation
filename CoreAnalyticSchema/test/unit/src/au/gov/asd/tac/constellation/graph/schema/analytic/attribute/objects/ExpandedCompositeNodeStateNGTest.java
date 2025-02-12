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
public class ExpandedCompositeNodeStateNGTest {
    
    private RecordStore recordStore;
    
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
        recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, "Vertex #0 + 1 more...");
        recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.X, 1.0);
        recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.Y, 2.0);
        recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.Z, 3.0);
        recordStore.set(GraphRecordStoreUtilities.SOURCE + GraphRecordStoreUtilities.ID, "copy.Identifier<Vertex #0 + 1 more...>Type<Unknown>");
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of contract method, of class ExpandedCompositeNodeState.
     */
    @Test
    public void testContract() {
        System.out.println("contract");
        
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        final StoreGraph graph = new StoreGraph(schema);
        
        final int vxId1 = graph.addVertex();
        final int vxId2 = graph.addVertex();
        
        final int identifierVertexAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        final int compositeStateVertexAttribute = AnalyticConcept.VertexAttribute.COMPOSITE_STATE.ensure(graph);
        final int selectedVertexAttribute = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        final int xVertexAttribute = VisualConcept.VertexAttribute.X.ensure(graph);
        final int yVertexAttribute = VisualConcept.VertexAttribute.Y.ensure(graph);
        final int zVertexAttribute = VisualConcept.VertexAttribute.Z.ensure(graph);
        VisualConcept.TransactionAttribute.IDENTIFIER.ensure(graph);
        
        final ExpandedCompositeNodeState instance = new ExpandedCompositeNodeState(recordStore, "copy.Identifier<Vertex #0 + 1 more...>Type<Unknown>", true, 2);
        final CompositeNodeState state1 = new CompositeNodeState(vxId1, instance);
        final CompositeNodeState state2 = new CompositeNodeState(vxId2, instance);
        
        graph.setStringValue(identifierVertexAttribute, vxId1, "my expanded vertex");
        graph.setObjectValue(compositeStateVertexAttribute, vxId1, state1);
        graph.setBooleanValue(selectedVertexAttribute, vxId1, true);
        graph.setFloatValue(xVertexAttribute, vxId1, 3F);
        graph.setFloatValue(yVertexAttribute, vxId1, 4F);
        graph.setFloatValue(zVertexAttribute, vxId1, 5F);
        
        graph.setStringValue(identifierVertexAttribute, vxId2, "another expanded");
        graph.setObjectValue(compositeStateVertexAttribute, vxId2, state2);
        graph.setBooleanValue(selectedVertexAttribute, vxId2, true);
        graph.setFloatValue(xVertexAttribute, vxId2, 0F);
        graph.setFloatValue(yVertexAttribute, vxId2, 1F);
        graph.setFloatValue(zVertexAttribute, vxId2, 2F);
        
        assertEquals(graph.getVertexCount(), 2);
        
        final int resultId = instance.contract(graph);
        
        assertEquals(graph.getVertexCount(), 1);
        
        assertEquals(graph.getStringValue(identifierVertexAttribute, resultId), "Vertex #0 + 1 more...");
        assertNotEquals(graph.getObjectValue(compositeStateVertexAttribute, resultId), state1);
        assertNotEquals(graph.getObjectValue(compositeStateVertexAttribute, resultId), state2);
        assertTrue(graph.getBooleanValue(selectedVertexAttribute, resultId));
        assertEquals(graph.getFloatValue(xVertexAttribute, resultId), 1.5F);
        assertEquals(graph.getFloatValue(yVertexAttribute, resultId), 2.5F);
        assertEquals(graph.getFloatValue(zVertexAttribute, resultId), 3.5F);
    }
}
