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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.interaction.plugins.clipboard.CopyToNewGraphPlugin;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.objects.CompositeNodeState;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.utilities.CompositeUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.visual.VisualGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.visual.plugins.select.DeselectAllPlugin;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginExecutor;
import au.gov.asd.tac.constellation.plugins.PluginRegistry;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Composites Test.
 *
 * @author twilight_sparkle
 */
public class CompositesNGTest {

    private Graph graph;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    @BeforeMethod
    public void setUpMethod() {
        graph = new DualGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());
    }

    @Test
    public void contractCompositesNoCompositesTest() throws InterruptedException, PluginException {
        final WritableGraph wg = graph.getWritableGraph("test", true);

        // Add two vertices then contract all composites
        try {
            wg.addVertex();
            wg.addVertex();
            PluginExecution.withPlugin(InteractiveGraphPluginRegistry.CONTRACT_ALL_COMPOSITES).executeNow(wg);
        } finally {
            wg.commit();
        }

        // Assert that nothing happened to the graph since there were no composites
        try (final ReadableGraph rg = graph.getReadableGraph()) {
            final int compositeAttr = AnalyticConcept.VertexAttribute.COMPOSITE_STATE.get(rg);
            assertEquals(rg.getVertexCount(), 2);
            assertEquals(compositeAttr, Graph.NOT_FOUND);
        }
    }

    @Test
    public void expandCompositesNoCompositesTest() throws InterruptedException, PluginException {
        final WritableGraph wg = graph.getWritableGraph("test", true);

        // Add two vertices then expand all composites
        try {
            wg.addVertex();
            wg.addVertex();
            PluginExecution.withPlugin(InteractiveGraphPluginRegistry.EXPAND_ALL_COMPOSITES).executeNow(wg);
        } finally {
            wg.commit();
        }

        // Assert that nothing happened to the graph since there were no composites
        try (final ReadableGraph rg = graph.getReadableGraph()) {
            final int compositeAttr = AnalyticConcept.VertexAttribute.COMPOSITE_STATE.get(rg);
            assertEquals(rg.getVertexCount(), 2);
            assertEquals(compositeAttr, Graph.NOT_FOUND);
        }
    }

    @Test
    public void makeExpandContractCompositeTest() throws InterruptedException, PluginException {
        final WritableGraph wg = graph.getWritableGraph("test", true);
        wg.getSchema().newGraph(wg);
        
        final int v0;
        final int v1;
        final int v2;
        
        final String v0name;
        final String v1name;
        final String v2name;
        
        final int nameAttr;
        final int selectedAttr;
        final int compositeAttr;
        
        try {
            // Add three vertices
            v0 = wg.addVertex();
            wg.getSchema().newVertex(wg, v0);
            v1 = wg.addVertex();
            wg.getSchema().newVertex(wg, v1);
            v2 = wg.addVertex();
            wg.getSchema().newVertex(wg, v2);

            // Store the names of these vertices
            nameAttr = VisualConcept.VertexAttribute.LABEL.get(wg);
            v0name = wg.getStringValue(nameAttr, v0);
            v1name = wg.getStringValue(nameAttr, v1);
            v2name = wg.getStringValue(nameAttr, v2);

            // Add transactions from v0 to v1, and from v1 to v2
            final int t0To1 = wg.addTransaction(v0, v1, true);
            wg.getSchema().newTransaction(wg, t0To1);
            final int t1To2 = wg.addTransaction(v1, v2, true);
            wg.getSchema().newTransaction(wg, t1To2);

            // Select v0 and v1, but not v2
            selectedAttr = VisualConcept.VertexAttribute.SELECTED.get(wg);
            wg.setBooleanValue(selectedAttr, v0, true);
            wg.setBooleanValue(selectedAttr, v1, true);
            wg.setBooleanValue(selectedAttr, v2, false);

            // Make a composite from the selection
            PluginExecution.withPlugin(InteractiveGraphPluginRegistry.CREATE_COMPOSITE_FROM_SELECTION).executeNow(wg);
            compositeAttr = AnalyticConcept.VertexAttribute.COMPOSITE_STATE.get(wg);
        } finally {
            wg.commit();
        }

        try (final ReadableGraph rg = graph.getReadableGraph()) {
            // Assert that there are two vertices, that the composite node has a composite state containing two nodes,
            // and that the non-composite node has a null composite state and its original name.
            assertEquals(rg.getVertexCount(), 2);
            assertEquals(rg.getStringValue(nameAttr, v2), v2name);
            assertNull(rg.getObjectValue(compositeAttr, v2));

            final int compositeNode = rg.getVertex(0) != v2 ? rg.getVertex(0) : rg.getVertex(1);
            final CompositeNodeState compositeState = (CompositeNodeState) rg.getObjectValue(compositeAttr, compositeNode);
            assertNotNull(compositeState);
            assertEquals(compositeState.getNumberOfNodes(), 2);
            assertTrue(compositeState.isComposite());

            // Assert that the link between the composite node and the non-composite node exists,
            // and contains one transaction from the composite node to the non-composite node
            final int compLink = rg.getLink(compositeNode, v2);
            assertEquals(rg.getLinkTransactionCount(compLink), 1);
            final int compTrans = rg.getLinkTransaction(compLink, 0);
            assertFalse(rg.getTransactionDirection(compTrans) == Graph.FLAT);
            assertEquals(rg.getTransactionSourceVertex(compTrans), compositeNode);
        }

        final WritableGraph wg2 = graph.getWritableGraph("test", true);
        try {
            // Contract all composites - nothing should happen
            PluginExecution.withPlugin(InteractiveGraphPluginRegistry.CONTRACT_ALL_COMPOSITES).executeNow(wg2);
        } finally {
            wg2.commit();
        }

        try (final ReadableGraph rg2 = graph.getReadableGraph()) {
            // Assert everything from above - nothing should have changed.
            assertEquals(rg2.getVertexCount(), 2);
            assertEquals(rg2.getStringValue(nameAttr, v2), v2name);
            assertNull(rg2.getObjectValue(compositeAttr, v2));

            final int compositeNode = rg2.getVertex(0) != v2 ? rg2.getVertex(0) : rg2.getVertex(1);
            final CompositeNodeState compositeState = (CompositeNodeState) rg2.getObjectValue(compositeAttr, compositeNode);
            assertNotNull(compositeState);
            assertEquals(compositeState.getNumberOfNodes(), 2);
            assertTrue(compositeState.isComposite());

            final int compLink = rg2.getLink(compositeNode, v2);
            assertEquals(rg2.getLinkTransactionCount(compLink), 1);
            final int compTrans = rg2.getLinkTransaction(compLink, 0);
            assertFalse(rg2.getTransactionDirection(compTrans) == Graph.FLAT);
            assertEquals(rg2.getTransactionSourceVertex(compTrans), compositeNode);
        }

        final WritableGraph wg3 = graph.getWritableGraph("test", true);
        try {
            // Expand all composites
            PluginExecution.withPlugin(InteractiveGraphPluginRegistry.EXPAND_ALL_COMPOSITES).executeNow(wg3);
        } finally {
            wg3.commit();
        }
        
        try (final ReadableGraph rg3 = graph.getReadableGraph()) {
            // Assert that there are three nodes, that v2 is the same with a null composite state,
            // and that the other two have expanded composite states
            assertEquals(rg3.getVertexCount(), 3);
            assertEquals(rg3.getStringValue(nameAttr, v2), v2name);
            assertNull(rg3.getObjectValue(compositeAttr, v2));

            final int v2pos = rg3.getVertexPosition(v2);
            final int comp0 = v2pos != 0 ? rg3.getVertex(0) : rg3.getVertex(2);
            final int comp1 = v2pos != 1 ? rg3.getVertex(1) : rg3.getVertex(2);
            final int expandedV0 = rg3.getStringValue(nameAttr, comp0).equals(v0name) ? comp0 : comp1;
            final int expandedV1 = expandedV0 == comp0 ? comp1 : comp0;

            final CompositeNodeState compositeState0 = (CompositeNodeState) rg3.getObjectValue(compositeAttr, expandedV0);
            final CompositeNodeState compositeState1 = (CompositeNodeState) rg3.getObjectValue(compositeAttr, expandedV1);
            assertNotNull(compositeState0);
            assertEquals(compositeState0.getNumberOfNodes(), 2);
            assertTrue(compositeState0.comprisesAComposite());
            assertEquals(rg3.getStringValue(nameAttr, expandedV0), v0name);
            assertNotNull(compositeState1);
            assertEquals(compositeState1.getNumberOfNodes(), 2);
            assertTrue(compositeState1.comprisesAComposite());
            assertEquals(rg3.getStringValue(nameAttr, expandedV1), v1name);

            // Assert that the the original transactions exist and no others.
            assertEquals(rg3.getTransactionCount(), 2);
            final int l0To1 = rg3.getLink(expandedV0, expandedV1);
            assertEquals(rg3.getLinkTransactionCount(l0To1), 1);
            final int t0To1 = rg3.getLinkTransaction(l0To1, 0);
            assertEquals(rg3.getTransactionSourceVertex(t0To1), expandedV0);
            assertFalse(rg3.getTransactionDirection(t0To1) == Graph.FLAT);
            final int l0To2 = rg3.getLink(expandedV0, v2);
            final int l1To2 = rg3.getLink(expandedV1, v2);
            assertEquals(l0To2, Graph.NOT_FOUND);
            assertEquals(rg3.getLinkTransactionCount(l1To2), 1);
            final int t1To2 = rg3.getLinkTransaction(l1To2, 0);
            assertEquals(rg3.getTransactionSourceVertex(t1To2), expandedV1);
            assertFalse(rg3.getTransactionDirection(t1To2) == Graph.FLAT);
        }

        final WritableGraph wg4 = graph.getWritableGraph("test", true);
        try {
            // Expand all composites - nothing should happen
            PluginExecution.withPlugin(PluginRegistry.get(InteractiveGraphPluginRegistry.EXPAND_ALL_COMPOSITES)).executeNow(wg4);
        } finally {
            wg4.commit();
        }
        
        try (final ReadableGraph rg4 = graph.getReadableGraph()) {
            // Assert everything from above - nothing should have changed.
            assertEquals(rg4.getVertexCount(), 3);
            assertEquals(rg4.getStringValue(nameAttr, v2), v2name);
            assertNull(rg4.getObjectValue(compositeAttr, v2));

            final int v2pos = rg4.getVertexPosition(v2);
            final int comp0 = v2pos != 0 ? rg4.getVertex(0) : rg4.getVertex(2);
            final int comp1 = v2pos != 1 ? rg4.getVertex(1) : rg4.getVertex(2);
            final int expandedV0 = rg4.getStringValue(nameAttr, comp0).equals(v0name) ? comp0 : comp1;
            final int expandedV1 = expandedV0 == comp0 ? comp1 : comp0;

            final CompositeNodeState compositeState0 = (CompositeNodeState) rg4.getObjectValue(compositeAttr, expandedV0);
            final CompositeNodeState compositeState1 = (CompositeNodeState) rg4.getObjectValue(compositeAttr, expandedV1);
            assertNotNull(compositeState0);
            assertEquals(compositeState0.getNumberOfNodes(), 2);
            assertTrue(compositeState0.comprisesAComposite());
            assertEquals(rg4.getStringValue(nameAttr, expandedV0), v0name);
            assertNotNull(compositeState1);
            assertEquals(compositeState1.getNumberOfNodes(), 2);
            assertTrue(compositeState1.comprisesAComposite());
            assertEquals(rg4.getStringValue(nameAttr, expandedV1), v1name);

            // Assert that the the original transactions exist and no others.
            assertEquals(rg4.getTransactionCount(), 2);
            final int l0To1 = rg4.getLink(expandedV0, expandedV1);
            assertEquals(rg4.getLinkTransactionCount(l0To1), 1);
            final int t0To1 = rg4.getLinkTransaction(l0To1, 0);
            assertEquals(rg4.getTransactionSourceVertex(t0To1), expandedV0);
            assertFalse(rg4.getTransactionDirection(t0To1) == Graph.FLAT);
            final int l0To2 = rg4.getLink(expandedV0, v2);
            final int l1To2 = rg4.getLink(expandedV1, v2);
            assertEquals(l0To2, Graph.NOT_FOUND);
            assertEquals(rg4.getLinkTransactionCount(l1To2), 1);
            final int t1To2 = rg4.getLinkTransaction(l1To2, 0);
            assertEquals(rg4.getTransactionSourceVertex(t1To2), expandedV1);
            assertFalse(rg4.getTransactionDirection(t1To2) == Graph.FLAT);
        }

        final WritableGraph wg5 = graph.getWritableGraph("test", true);
        try {
            // Contract all composites
            PluginExecution.withPlugin(PluginRegistry.get(InteractiveGraphPluginRegistry.CONTRACT_ALL_COMPOSITES)).executeNow(wg5);
        } finally {
            wg5.commit();
        }

        // Assert everything that was previously true when we first made the composites
        try (final ReadableGraph rg5 = graph.getReadableGraph()) {
            // Assert everything from above - nothing should have changed.
            assertEquals(rg5.getVertexCount(), 2);
            assertEquals(rg5.getStringValue(nameAttr, v2), v2name);
            assertNull(rg5.getObjectValue(compositeAttr, v2));

            final int compositeNode = rg5.getVertex(0) != v2 ? rg5.getVertex(0) : rg5.getVertex(1);
            final CompositeNodeState compositeState = (CompositeNodeState) rg5.getObjectValue(compositeAttr, compositeNode);
            assertNotNull(compositeState);
            assertEquals(compositeState.getNumberOfNodes(), 2);
            assertTrue(compositeState.isComposite());

            final int compLink = rg5.getLink(compositeNode, v2);
            assertEquals(rg5.getLinkTransactionCount(compLink), 1);
            final int compTrans = rg5.getLinkTransaction(compLink, 0);
            assertFalse(rg5.getTransactionDirection(compTrans) == Graph.FLAT);
            assertEquals(rg5.getTransactionSourceVertex(compTrans), compositeNode);
        }
    }

    @Test
    public void makeExpandDeleteNodeContractCompositeTest() throws InterruptedException, PluginException {
        final WritableGraph wg = graph.getWritableGraph("test", true);
        wg.getSchema().newGraph(wg);
        
        final int v0;
        final int v1;
        final int v2;
        
        final String v0name;
        final String v1name;
        final String v2name;
        
        final int nameAttr;
        final int selectedAttr;
        final int compositeAttr;
        
        try {
            // Add three vertices
            v0 = wg.addVertex();
            wg.getSchema().newVertex(wg, v0);
            v1 = wg.addVertex();
            wg.getSchema().newVertex(wg, v1);
            v2 = wg.addVertex();
            wg.getSchema().newVertex(wg, v2);

            // Store the names of these vertices
            nameAttr = VisualConcept.VertexAttribute.LABEL.get(wg);
            v0name = wg.getStringValue(nameAttr, v0);
            v1name = wg.getStringValue(nameAttr, v1);
            v2name = wg.getStringValue(nameAttr, v2);

            // Add transactions from v0 to v1, and from v1 to v2
            final int t0To1 = wg.addTransaction(v0, v1, true);
            wg.getSchema().newTransaction(wg, t0To1);
            final int t1To2 = wg.addTransaction(v1, v2, true);
            wg.getSchema().newTransaction(wg, t1To2);

            // Select v0 and v1, but not v2
            selectedAttr = VisualConcept.VertexAttribute.SELECTED.get(wg);
            wg.setBooleanValue(selectedAttr, v0, true);
            wg.setBooleanValue(selectedAttr, v1, true);
            wg.setBooleanValue(selectedAttr, v2, false);

            // Make a composite from the selection then expand all composites
            PluginExecutor.startWith(PluginRegistry.get(InteractiveGraphPluginRegistry.CREATE_COMPOSITE_FROM_SELECTION))
                    .followedBy(PluginRegistry.get(InteractiveGraphPluginRegistry.EXPAND_ALL_COMPOSITES))
                    .executeNow(wg);
            compositeAttr = AnalyticConcept.VertexAttribute.COMPOSITE_STATE.get(wg);

            // Delete the expanded vertex corresponding to v0 then contract all composites
            final int v2pos = wg.getVertexPosition(v2);
            final int comp0 = v2pos != 0 ? wg.getVertex(0) : wg.getVertex(2);
            final int comp1 = v2pos != 1 ? wg.getVertex(1) : wg.getVertex(2);
            final int expanded_v0 = wg.getStringValue(nameAttr, comp0).equals(v0name) ? comp0 : comp1;
            wg.removeVertex(expanded_v0);
            PluginExecution.withPlugin(PluginRegistry.get(InteractiveGraphPluginRegistry.CONTRACT_ALL_COMPOSITES)).executeNow(wg);

        } finally {
            wg.commit();
        }
        
        try (final ReadableGraph rg = graph.getReadableGraph()) {
            // Assert that there are two vertices, that the composite node has a composite state containing one node,
            // and that the non-composite node has a null composite state and its original name.
            assertEquals(rg.getVertexCount(), 2);
            assertEquals(rg.getStringValue(nameAttr, v2), v2name);
            assertNull(rg.getObjectValue(compositeAttr, v2));

            final int compositeNode = rg.getVertex(0) != v2 ? rg.getVertex(0) : rg.getVertex(1);
            final CompositeNodeState compositeState = (CompositeNodeState) rg.getObjectValue(compositeAttr, compositeNode);
            assertNotNull(compositeState);
            assertEquals(compositeState.getNumberOfNodes(), 1);
            assertTrue(compositeState.isComposite());

            // Assert that the link between the composite node and the non-composite node still exists,
            // and contains one transaction from the composite node to the non-composite node
            final int compLink = rg.getLink(compositeNode, v2);
            assertEquals(rg.getLinkTransactionCount(compLink), 1);
            final int compTrans = rg.getLinkTransaction(compLink, 0);
            assertFalse(rg.getTransactionDirection(compTrans) == Graph.FLAT);
            assertEquals(rg.getTransactionSourceVertex(compTrans), compositeNode);
        }

        final WritableGraph wg2 = graph.getWritableGraph("test", true);
        try {
            PluginExecution.withPlugin(PluginRegistry.get(InteractiveGraphPluginRegistry.EXPAND_ALL_COMPOSITES)).executeNow(wg2);
        } finally {
            wg2.commit();
        }
        
        final int expandedV1;
        try (final ReadableGraph rg2 = graph.getReadableGraph()) {
            // Assert that there are two nodes, that v2 is the same with a null composite state,
            // and that the other node corresponds to v1 with an expanded composite states
            assertEquals(rg2.getVertexCount(), 2);
            assertEquals(rg2.getStringValue(nameAttr, v2), v2name);
            assertNull(rg2.getObjectValue(compositeAttr, v2));

            final int v2pos = rg2.getVertexPosition(v2);
            expandedV1 = v2pos != 0 ? rg2.getVertex(0) : rg2.getVertex(1);

            final CompositeNodeState compositeState = (CompositeNodeState) rg2.getObjectValue(compositeAttr, expandedV1);
            assertNotNull(compositeState);
            assertEquals(compositeState.getNumberOfNodes(), 1);
            assertTrue(compositeState.comprisesAComposite());
            assertEquals(rg2.getStringValue(nameAttr, expandedV1), v1name);

            // Assert that the transaction between exapnded v1 and v2 exist, and no others.
            assertEquals(rg2.getTransactionCount(), 1);
            final int l0To1 = rg2.getLink(expandedV1, v2);
            assertEquals(rg2.getLinkTransactionCount(l0To1), 1);
            final int t0To1 = rg2.getLinkTransaction(l0To1, 0);
            assertEquals(rg2.getTransactionSourceVertex(t0To1), expandedV1);
            assertFalse(rg2.getTransactionDirection(t0To1) == Graph.FLAT);
        }

        final WritableGraph wg3 = graph.getWritableGraph("test", true);
        try {
            // Delete v1 and then contract all composites
            wg.removeVertex(expandedV1);
            PluginExecution.withPlugin(PluginRegistry.get(InteractiveGraphPluginRegistry.CONTRACT_ALL_COMPOSITES)).executeNow(wg3);
        } finally {
            wg3.commit();
        }
        
        try (final ReadableGraph rg3 = graph.getReadableGraph()) {
            // Assert that there is only one vertex - v2 with a null composite state
            assertEquals(rg3.getVertexCount(), 1);
            assertEquals(rg3.getStringValue(nameAttr, v2), v2name);
            assertNull(rg3.getObjectValue(compositeAttr, v2));

            // Assert that there are no transactions
            assertEquals(rg3.getTransactionCount(), 0);
        }
    }

    @Test
    public void makeExpandDeleteNodeWithTransactionContractCompositeTest() throws InterruptedException, PluginException {
        final WritableGraph wg = graph.getWritableGraph("test", true);
        wg.getSchema().newGraph(wg);
        
        final int v0;
        final int v1;
        final int v2;
        
        final String v0name;
        final String v1name;
        final String v2name;
        
        final int nameAttr;
        final int selectedAttr;
        final int compositeAttr;
        
        try {
            // Add three vertices
            v0 = wg.addVertex();
            wg.getSchema().newVertex(wg, v0);
            v1 = wg.addVertex();
            wg.getSchema().newVertex(wg, v1);
            v2 = wg.addVertex();
            wg.getSchema().newVertex(wg, v2);

            // Store the names of these vertices
            nameAttr = VisualConcept.VertexAttribute.LABEL.get(wg);
            v0name = wg.getStringValue(nameAttr, v0);
            v1name = wg.getStringValue(nameAttr, v1);
            v2name = wg.getStringValue(nameAttr, v2);

            // Add transactions from v0 to v1, and from v1 to v2
            final int t0To1 = wg.addTransaction(v0, v1, true);
            wg.getSchema().newTransaction(wg, t0To1);
            final int t1To2 = wg.addTransaction(v1, v2, true);
            wg.getSchema().newTransaction(wg, t1To2);

            // Select v0 and v1, but not v2
            selectedAttr = VisualConcept.VertexAttribute.SELECTED.get(wg);
            wg.setBooleanValue(selectedAttr, v0, true);
            wg.setBooleanValue(selectedAttr, v1, true);
            wg.setBooleanValue(selectedAttr, v2, false);

            // Make a composite from the selection then expand all composites
            PluginExecutor.startWith(PluginRegistry.get(InteractiveGraphPluginRegistry.CREATE_COMPOSITE_FROM_SELECTION))
                    .followedBy(PluginRegistry.get(InteractiveGraphPluginRegistry.EXPAND_ALL_COMPOSITES))
                    .executeNow(wg);
            compositeAttr = AnalyticConcept.VertexAttribute.COMPOSITE_STATE.get(wg);

            // Delete the expanded vertex corresponding to v1 then contract all composites
            final int v2pos = wg.getVertexPosition(v2);
            final int comp0 = v2pos != 0 ? wg.getVertex(0) : wg.getVertex(2);
            final int comp1 = v2pos != 1 ? wg.getVertex(1) : wg.getVertex(2);
            final int expandedV1 = wg.getStringValue(nameAttr, comp0).equals(v1name) ? comp0 : comp1;
            wg.removeVertex(expandedV1);
            PluginExecution.withPlugin(PluginRegistry.get(InteractiveGraphPluginRegistry.CONTRACT_ALL_COMPOSITES)).executeNow(wg);
        } finally {
            wg.commit();
        }
        
        try (final ReadableGraph rg = graph.getReadableGraph()) {
            // Assert that there are two vertices, that the composite node has a composite state containing one node,
            // and that the non-composite node has a null composite state and its original name.
            assertEquals(rg.getVertexCount(), 2);
            assertEquals(rg.getStringValue(nameAttr, v2), v2name);
            assertNull(rg.getObjectValue(compositeAttr, v2));

            final int compositeNode = rg.getVertex(0) != v2 ? rg.getVertex(0) : rg.getVertex(1);
            final CompositeNodeState compositeState = (CompositeNodeState) rg.getObjectValue(compositeAttr, compositeNode);
            assertNotNull(compositeState);
            assertEquals(compositeState.getNumberOfNodes(), 1);
            assertTrue(compositeState.isComposite());

            // Assert that there are no transactions in the graph.
            assertEquals(rg.getTransactionCount(), 0);
        }

        final WritableGraph wg2 = graph.getWritableGraph("test", true);
        try {
            PluginExecution.withPlugin(PluginRegistry.get(InteractiveGraphPluginRegistry.EXPAND_ALL_COMPOSITES)).executeNow(wg2);
        } finally {
            wg2.commit();
        }
        
        final int expandedV0;
        try (final ReadableGraph rg2 = graph.getReadableGraph()) {
            // Assert that there are two nodes, that v2 is the same with a null composite state,
            // and that the other node corresponds to v0 with an expanded composite states
            assertEquals(rg2.getVertexCount(), 2);
            assertEquals(rg2.getStringValue(nameAttr, v2), v2name);
            assertNull(rg2.getObjectValue(compositeAttr, v2));

            final int v2pos = rg2.getVertexPosition(v2);
            expandedV0 = v2pos != 0 ? rg2.getVertex(0) : rg2.getVertex(1);

            final CompositeNodeState compositeState = (CompositeNodeState) rg2.getObjectValue(compositeAttr, expandedV0);
            assertNotNull(compositeState);
            assertEquals(compositeState.getNumberOfNodes(), 1);
            assertTrue(compositeState.comprisesAComposite());
            assertEquals(rg2.getStringValue(nameAttr, expandedV0), v0name);

            // Assert that there are no transactions in the graph
            assertEquals(rg2.getTransactionCount(), 0);
        }
    }

    @Test
    public void makeDeleteTransactionExpandTest() throws InterruptedException, PluginException {
        final WritableGraph wg = graph.getWritableGraph("test", true);
        wg.getSchema().newGraph(wg);
        
        final int v0;
        final int v1;
        final int v2;
        
        final String v0name;
        final String v1name;
        final String v2name;
        
        final int nameAttr;
        final int selectedAttr;
        final int compositeAttr;
        
        try {
            // Add three vertices
            v0 = wg.addVertex();
            wg.getSchema().newVertex(wg, v0);
            v1 = wg.addVertex();
            wg.getSchema().newVertex(wg, v1);
            v2 = wg.addVertex();
            wg.getSchema().newVertex(wg, v2);

            // Store the names of these vertices
            nameAttr = VisualConcept.VertexAttribute.LABEL.get(wg);
            v0name = wg.getStringValue(nameAttr, v0);
            v1name = wg.getStringValue(nameAttr, v1);
            v2name = wg.getStringValue(nameAttr, v2);

            // Add transactions from v0 to v1, and from v1 to v2
            final int t0_1 = wg.addTransaction(v0, v1, true);
            wg.getSchema().newTransaction(wg, t0_1);
            final int t1_2 = wg.addTransaction(v1, v2, true);
            wg.getSchema().newTransaction(wg, t1_2);

            // Select v0 and v1, but not v2
            selectedAttr = VisualConcept.VertexAttribute.SELECTED.get(wg);
            wg.setBooleanValue(selectedAttr, v0, true);
            wg.setBooleanValue(selectedAttr, v1, true);
            wg.setBooleanValue(selectedAttr, v2, false);

            // Make a composite from the selection
            PluginExecution.withPlugin(PluginRegistry.get(InteractiveGraphPluginRegistry.CREATE_COMPOSITE_FROM_SELECTION)).executeNow(wg);
            compositeAttr = AnalyticConcept.VertexAttribute.COMPOSITE_STATE.get(wg);

            // Delete the transaction between the composite and the non-composite then expand all composites
            final int compositeNode = wg.getVertex(0) != v2 ? wg.getVertex(0) : wg.getVertex(1);
            final int compositeLink = wg.getLink(compositeNode, v2);
            final int compositeTransaction = wg.getLinkTransaction(compositeLink, 0);
            wg.removeTransaction(compositeTransaction);
            PluginExecution.withPlugin(PluginRegistry.get(InteractiveGraphPluginRegistry.EXPAND_ALL_COMPOSITES)).executeNow(wg);

        } finally {
            wg.commit();
        }
        
        try (final ReadableGraph rg = graph.getReadableGraph()) {
            // Assert that there are three nodes, that v2 is the same with a null composite state,
            // and that the other two have expanded composite states
            assertEquals(rg.getVertexCount(), 3);
            assertEquals(rg.getStringValue(nameAttr, v2), v2name);
            assertNull(rg.getObjectValue(compositeAttr, v2));

            final int v2pos = rg.getVertexPosition(v2);
            final int comp0 = v2pos != 0 ? rg.getVertex(0) : rg.getVertex(2);
            final int comp1 = v2pos != 1 ? rg.getVertex(1) : rg.getVertex(2);
            final int expandedV0 = rg.getStringValue(nameAttr, comp0).equals(v0name) ? comp0 : comp1;
            final int expandedV1 = expandedV0 == comp0 ? comp1 : comp0;

            final CompositeNodeState compositeState0 = (CompositeNodeState) rg.getObjectValue(compositeAttr, expandedV0);
            final CompositeNodeState compositeState1 = (CompositeNodeState) rg.getObjectValue(compositeAttr, expandedV1);
            assertNotNull(compositeState0);
            assertEquals(compositeState0.getNumberOfNodes(), 2);
            assertTrue(compositeState0.comprisesAComposite());
            assertEquals(rg.getStringValue(nameAttr, expandedV0), v0name);
            assertNotNull(compositeState1);
            assertEquals(compositeState1.getNumberOfNodes(), 2);
            assertTrue(compositeState1.comprisesAComposite());
            assertEquals(rg.getStringValue(nameAttr, expandedV1), v1name);

            // Assert that only the transaction between v0 and v1 exists and no others.
            assertEquals(rg.getTransactionCount(), 1);
            final int l0To1 = rg.getLink(expandedV0, expandedV1);
            assertEquals(rg.getLinkTransactionCount(l0To1), 1);
            final int t0To1 = rg.getLinkTransaction(l0To1, 0);
            assertEquals(rg.getTransactionSourceVertex(t0To1), expandedV0);
            assertFalse(rg.getTransactionDirection(t0To1) == Graph.FLAT);
            final int l0To2 = rg.getLink(expandedV0, v2);
            assertEquals(l0To2, Graph.NOT_FOUND);
            final int l1To2 = rg.getLink(expandedV1, v2);
            assertEquals(l1To2, Graph.NOT_FOUND);
        }
    }

    @Test
    public void makeAddTransactionExpandTest() throws InterruptedException, PluginException {
        final WritableGraph wg = graph.getWritableGraph("test", true);
        wg.getSchema().newGraph(wg);
        
        final int v0;
        final int v1;
        final int v2;
        
        final String v0name;
        final String v1name;
        final String v2name;
        
        final int nameAttr;
        final int selectedAttr;
        final int compositeAttr;
        
        try {
            // Add three vertices
            v0 = wg.addVertex();
            wg.getSchema().newVertex(wg, v0);
            v1 = wg.addVertex();
            wg.getSchema().newVertex(wg, v1);
            v2 = wg.addVertex();
            wg.getSchema().newVertex(wg, v2);

            // Store the names of these vertices
            nameAttr = VisualConcept.VertexAttribute.LABEL.get(wg);
            v0name = wg.getStringValue(nameAttr, v0);
            v1name = wg.getStringValue(nameAttr, v1);
            v2name = wg.getStringValue(nameAttr, v2);

            // Add transactions from v0 to v1, and from v1 to v2
            final int t0_1 = wg.addTransaction(v0, v1, true);
            wg.getSchema().newTransaction(wg, t0_1);
            final int t1_2 = wg.addTransaction(v1, v2, true);
            wg.getSchema().newTransaction(wg, t1_2);

            // Select v0 and v1, but not v2
            selectedAttr = VisualConcept.VertexAttribute.SELECTED.get(wg);
            wg.setBooleanValue(selectedAttr, v0, true);
            wg.setBooleanValue(selectedAttr, v1, true);
            wg.setBooleanValue(selectedAttr, v2, false);

            // Make a composite from the selection
            PluginExecution.withPlugin(PluginRegistry.get(InteractiveGraphPluginRegistry.CREATE_COMPOSITE_FROM_SELECTION)).executeNow(wg);
            compositeAttr = AnalyticConcept.VertexAttribute.COMPOSITE_STATE.get(wg);

            // Add a transaction from the composite to the non-composite then expand all composites
            final int compositeNode = wg.getVertex(0) != v2 ? wg.getVertex(0) : wg.getVertex(1);
            final int addedTx = wg.addTransaction(compositeNode, v2, true);
            wg.getSchema().newTransaction(wg, addedTx);
            PluginExecution.withPlugin(PluginRegistry.get(InteractiveGraphPluginRegistry.EXPAND_ALL_COMPOSITES)).executeNow(wg);

        } finally {
            wg.commit();
        }
        
        try (final ReadableGraph rg = graph.getReadableGraph()) {
            // Assert that there are three nodes, that v2 is the same with a null composite state,
            // and that the other two have expanded composite states
            assertEquals(rg.getVertexCount(), 3);
            assertEquals(rg.getStringValue(nameAttr, v2), v2name);
            assertNull(rg.getObjectValue(compositeAttr, v2));

            final int v2pos = rg.getVertexPosition(v2);
            final int comp0 = v2pos != 0 ? rg.getVertex(0) : rg.getVertex(2);
            final int comp1 = v2pos != 1 ? rg.getVertex(1) : rg.getVertex(2);
            final int expandedV0 = rg.getStringValue(nameAttr, comp0).equals(v0name) ? comp0 : comp1;
            final int expandedV1 = expandedV0 == comp0 ? comp1 : comp0;

            final CompositeNodeState compositeState0 = (CompositeNodeState) rg.getObjectValue(compositeAttr, expandedV0);
            final CompositeNodeState compositeState1 = (CompositeNodeState) rg.getObjectValue(compositeAttr, expandedV1);
            assertNotNull(compositeState0);
            assertEquals(compositeState0.getNumberOfNodes(), 2);
            assertTrue(compositeState0.comprisesAComposite());
            assertEquals(rg.getStringValue(nameAttr, expandedV0), v0name);
            assertNotNull(compositeState1);
            assertEquals(compositeState1.getNumberOfNodes(), 2);
            assertTrue(compositeState1.comprisesAComposite());
            assertEquals(rg.getStringValue(nameAttr, expandedV1), v1name);

            // Assert that there are two transaction from v1 to v2, and one from v0 to v2, as well as one from v0 to v1. This is as a result of the transaction added to the composite being added to all constituents upon expansion.
            assertEquals(rg.getTransactionCount(), 4);
            final int l0To1 = rg.getLink(expandedV0, expandedV1);
            assertEquals(rg.getLinkTransactionCount(l0To1), 1);
            final int t0To1 = rg.getLinkTransaction(l0To1, 0);
            assertEquals(rg.getTransactionSourceVertex(t0To1), expandedV0);
            assertFalse(rg.getTransactionDirection(t0To1) == Graph.FLAT);
            final int l0To2 = rg.getLink(expandedV0, v2);
            assertEquals(rg.getLinkTransactionCount(l0To2), 1);
            final int t0To2 = rg.getLinkTransaction(l0To1, 0);
            assertEquals(rg.getTransactionSourceVertex(t0To2), expandedV0);
            assertFalse(rg.getTransactionDirection(t0To2) == Graph.FLAT);
            final int l1To2 = rg.getLink(expandedV1, v2);
            assertEquals(rg.getLinkTransactionCount(l1To2), 2);
            final int t1To2To0 = rg.getLinkTransaction(l1To2, 0);
            final int t1To2To1 = rg.getLinkTransaction(l1To2, 1);
            assertEquals(rg.getTransactionSourceVertex(t1To2To0), expandedV1);
            assertEquals(rg.getTransactionSourceVertex(t1To2To1), expandedV1);
            assertFalse(rg.getTransactionDirection(t1To2To0) == Graph.FLAT);
            assertFalse(rg.getTransactionDirection(t1To2To1) == Graph.FLAT);
        }
    }

    @Test
    public void compositeTwoCompositesTest() throws InterruptedException, PluginException {
        final WritableGraph wg = graph.getWritableGraph("test", true);
        wg.getSchema().newGraph(wg);
        
        int v0;
        int v1;
        int v2;
        int v3;
        
        final String v0name;
        final String v1name;
        final String v2name;
        final String v3name;
        
        final int nameAttr;
        final int selectedAttr;
        final int compositeAttr;
        
        try {
            // Add four vertices
            v0 = wg.addVertex();
            wg.getSchema().newVertex(wg, v0);
            v1 = wg.addVertex();
            wg.getSchema().newVertex(wg, v1);
            v2 = wg.addVertex();
            wg.getSchema().newVertex(wg, v2);
            v3 = wg.addVertex();
            wg.getSchema().newVertex(wg, v3);

            // Store the names of these vertices
            nameAttr = VisualConcept.VertexAttribute.LABEL.get(wg);
            v0name = wg.getStringValue(nameAttr, v0);
            v1name = wg.getStringValue(nameAttr, v1);
            v2name = wg.getStringValue(nameAttr, v2);
            v3name = wg.getStringValue(nameAttr, v3);

            // Add transactions from v0 to v1, and from v1 to v2
            final int t0_1 = wg.addTransaction(v0, v1, true);
            wg.getSchema().newTransaction(wg, t0_1);
            final int t1_2 = wg.addTransaction(v1, v2, true);
            wg.getSchema().newTransaction(wg, t1_2);

            // Select v0 and v1, but not v2 or v3
            selectedAttr = VisualConcept.VertexAttribute.SELECTED.get(wg);
            wg.setBooleanValue(selectedAttr, v0, true);
            wg.setBooleanValue(selectedAttr, v1, true);
            wg.setBooleanValue(selectedAttr, v2, false);
            wg.setBooleanValue(selectedAttr, v3, false);

            // Make a composite from the selection then deselect all
            PluginExecutor.startWith(PluginRegistry.get(InteractiveGraphPluginRegistry.CREATE_COMPOSITE_FROM_SELECTION))
                    .followedBy(PluginRegistry.get(DeselectAllPlugin.class.getName()))
                    .executeNow(wg);
            compositeAttr = AnalyticConcept.VertexAttribute.COMPOSITE_STATE.get(wg);

            // Select v2 and v3
            wg.setBooleanValue(selectedAttr, v2, true);
            wg.setBooleanValue(selectedAttr, v3, true);

            // Make a composite from the selection
            PluginExecution.withPlugin(PluginRegistry.get(InteractiveGraphPluginRegistry.CREATE_COMPOSITE_FROM_SELECTION)).executeNow(wg);

            // Select all and composite the two composites
            PluginExecutor.startWith(VisualGraphPluginRegistry.SELECT_ALL)
                    .followedBy(PluginRegistry.get(InteractiveGraphPluginRegistry.CREATE_COMPOSITE_FROM_SELECTION))
                    .executeNow(wg);
        } finally {
            wg.commit();
        }
        
        try (final ReadableGraph rg = graph.getReadableGraph()) {
            // Assert that there is one vertex that is a composite node containing four nodes,
            assertEquals(rg.getVertexCount(), 1);
            final int compositeNode = rg.getVertex(0);
            final CompositeNodeState compositeState = (CompositeNodeState) rg.getObjectValue(compositeAttr, compositeNode);
            assertNotNull(compositeState);
            assertEquals(compositeState.getNumberOfNodes(), 4);
            assertTrue(compositeState.isComposite());

            // Assert that there are no transactions on the graph
            assertEquals(rg.getTransactionCount(), 0);
        }

        final WritableGraph wg2 = graph.getWritableGraph("test", true);
        try {
            // Expand all composites
            PluginExecution.withPlugin(PluginRegistry.get(InteractiveGraphPluginRegistry.EXPAND_ALL_COMPOSITES)).executeNow(wg2);
        } finally {
            wg2.commit();
        }
        
        try (final ReadableGraph rg2 = graph.getReadableGraph()) {
            // Assert that the original four vertices exist in expanded composite form
            assertEquals(rg2.getVertexCount(), 4);
            v0 = Graph.NOT_FOUND;
            v1 = Graph.NOT_FOUND;
            v2 = Graph.NOT_FOUND;
            v3 = Graph.NOT_FOUND;
            for (int i = 0; i < 4; i++) {
                final int id = rg2.getVertex(i);
                final String name = rg2.getStringValue(nameAttr, id);
                if (name.equals(v0name)) {
                    v0 = id;
                } else if (name.equals(v1name)) {
                    v1 = id;
                } else if (name.equals(v2name)) {
                    v2 = id;
                } else {
                    v3 = id;
                }
            }
            assertEquals(rg2.getStringValue(nameAttr, v0), v0name);
            final CompositeNodeState compositeState0 = (CompositeNodeState) rg2.getObjectValue(compositeAttr, v0);
            assertNotNull(compositeState0);
            assertEquals(compositeState0.getNumberOfNodes(), 4);
            assertTrue(compositeState0.comprisesAComposite());
            assertEquals(rg2.getStringValue(nameAttr, v1), v1name);
            final CompositeNodeState compositeState1 = (CompositeNodeState) rg2.getObjectValue(compositeAttr, v1);
            assertNotNull(compositeState1);
            assertEquals(compositeState1.getNumberOfNodes(), 4);
            assertTrue(compositeState1.comprisesAComposite());
            assertEquals(rg2.getStringValue(nameAttr, v2), v2name);
            final CompositeNodeState compositeState2 = (CompositeNodeState) rg2.getObjectValue(compositeAttr, v2);
            assertNotNull(compositeState2);
            assertEquals(compositeState2.getNumberOfNodes(), 4);
            assertTrue(compositeState2.comprisesAComposite());
            assertEquals(rg2.getStringValue(nameAttr, v3), v3name);
            final CompositeNodeState compositeState3 = (CompositeNodeState) rg2.getObjectValue(compositeAttr, v3);
            assertNotNull(compositeState3);
            assertEquals(compositeState3.getNumberOfNodes(), 4);
            assertTrue(compositeState3.comprisesAComposite());

            // assert that the original transactions exist and none others
            assertEquals(rg2.getTransactionCount(), 2);
            final int l0To1 = rg2.getLink(v0, v1);
            assertEquals(rg2.getLinkTransactionCount(l0To1), 1);
            final int t0To1 = rg2.getLinkTransaction(l0To1, 0);
            assertFalse(rg2.getTransactionDirection(t0To1) == Graph.FLAT);
            assertEquals(rg2.getTransactionSourceVertex(t0To1), v0);
            final int l1To2 = rg2.getLink(v1, v2);
            assertEquals(rg2.getLinkTransactionCount(l1To2), 1);
            final int t1To2 = rg2.getLinkTransaction(l1To2, 0);
            assertFalse(rg2.getTransactionDirection(t1To2) == Graph.FLAT);
            assertEquals(rg2.getTransactionSourceVertex(t1To2), v1);
        }
    }

    @Test
    public void compositeCompositeAndNonCompositeTest() throws InterruptedException, PluginException {
        final WritableGraph wg = graph.getWritableGraph("test", true);
        wg.getSchema().newGraph(wg);
        
        int v0;
        int v1;
        int v2;
        int v3;
        
        final String v0name;
        final String v1name;
        final String v2name;
        final String v3name;
        
        final int nameAttr;
        final int selectedAttr;
        final int compositeAttr;
        
        try {
            // Add four vertices
            v0 = wg.addVertex();
            wg.getSchema().newVertex(wg, v0);
            v1 = wg.addVertex();
            wg.getSchema().newVertex(wg, v1);
            v2 = wg.addVertex();
            wg.getSchema().newVertex(wg, v2);
            v3 = wg.addVertex();
            wg.getSchema().newVertex(wg, v3);

            // Store the names of these vertices
            nameAttr = VisualConcept.VertexAttribute.LABEL.get(wg);
            v0name = wg.getStringValue(nameAttr, v0);
            v1name = wg.getStringValue(nameAttr, v1);
            v2name = wg.getStringValue(nameAttr, v2);
            v3name = wg.getStringValue(nameAttr, v3);

            // Add transactions from v0 to v1, and from v1 to v2
            final int t0_1 = wg.addTransaction(v0, v1, true);
            wg.getSchema().newTransaction(wg, t0_1);
            final int t1_2 = wg.addTransaction(v1, v2, true);
            wg.getSchema().newTransaction(wg, t1_2);

            // Select v0 and v1, but not v2 or v3
            selectedAttr = VisualConcept.VertexAttribute.SELECTED.get(wg);
            wg.setBooleanValue(selectedAttr, v0, true);
            wg.setBooleanValue(selectedAttr, v1, true);
            wg.setBooleanValue(selectedAttr, v2, false);
            wg.setBooleanValue(selectedAttr, v3, false);

            // Make a composite from the selection then deselect all
            PluginExecutor.startWith(PluginRegistry.get(InteractiveGraphPluginRegistry.CREATE_COMPOSITE_FROM_SELECTION))
                    .followedBy(PluginRegistry.get(VisualGraphPluginRegistry.DESELECT_ALL))
                    .executeNow(wg);
            compositeAttr = AnalyticConcept.VertexAttribute.COMPOSITE_STATE.get(wg);

            // Select v2 and the newly made composite
            int compNode = wg.getVertex(0);
            if (compNode == v2 || compNode == v3) {
                compNode = wg.getVertex(1);
            }
            if (compNode == v2 || compNode == v3) {
                compNode = wg.getVertex(2);
            }

            wg.setBooleanValue(selectedAttr, v2, true);
            wg.setBooleanValue(selectedAttr, compNode, true);

            // Make a composite from the selection
            PluginExecution.withPlugin(PluginRegistry.get(InteractiveGraphPluginRegistry.CREATE_COMPOSITE_FROM_SELECTION)).executeNow(wg);
        } finally {
            wg.commit();
        }
        
        try (final ReadableGraph rg = graph.getReadableGraph()) {
            // Assert that there is one vertex that is a composite containing three nodes,
            // and v3 as the non-composite
            assertEquals(rg.getVertexCount(), 2);
            final int compositeNode = rg.getVertex(0) == v3 ? rg.getVertex(1) : rg.getVertex(0);
            final CompositeNodeState compositeState = (CompositeNodeState) rg.getObjectValue(compositeAttr, compositeNode);
            assertNotNull(compositeState);
            assertEquals(compositeState.getNumberOfNodes(), 3);
            assertTrue(compositeState.isComposite());

            assertEquals(rg.getStringValue(nameAttr, v3), v3name);
            final CompositeNodeState compositeState0 = (CompositeNodeState) rg.getObjectValue(compositeAttr, v3);
            assertNull(compositeState0);

            // Assert that there are no transactions on the graph
            assertEquals(rg.getTransactionCount(), 0);
        }

        final WritableGraph wg2 = graph.getWritableGraph("test", true);
        try {
            // Expand all composites
            PluginExecution.withPlugin(PluginRegistry.get(InteractiveGraphPluginRegistry.EXPAND_ALL_COMPOSITES)).executeNow(wg2);
        } finally {
            wg2.commit();
        }
        
        try (final ReadableGraph rg2 = graph.getReadableGraph()) {
            // Assert that the original four vertices exist; all with expanded composite states
            // except v3
            assertEquals(rg2.getVertexCount(), 4);
            v0 = Graph.NOT_FOUND;
            v1 = Graph.NOT_FOUND;
            v2 = Graph.NOT_FOUND;
            v3 = Graph.NOT_FOUND;
            for (int i = 0; i < 4; i++) {
                final int id = rg2.getVertex(i);
                final String name = rg2.getStringValue(nameAttr, id);
                if (name.equals(v0name)) {
                    v0 = id;
                } else if (name.equals(v1name)) {
                    v1 = id;
                } else if (name.equals(v2name)) {
                    v2 = id;
                } else {
                    v3 = id;
                }
            }
            assertEquals(rg2.getStringValue(nameAttr, v0), v0name);
            final CompositeNodeState compositeState0 = (CompositeNodeState) rg2.getObjectValue(compositeAttr, v0);
            assertNotNull(compositeState0);
            assertEquals(compositeState0.getNumberOfNodes(), 3);
            assertTrue(compositeState0.comprisesAComposite());
            assertEquals(rg2.getStringValue(nameAttr, v1), v1name);
            final CompositeNodeState compositeState1 = (CompositeNodeState) rg2.getObjectValue(compositeAttr, v1);
            assertNotNull(compositeState1);
            assertEquals(compositeState1.getNumberOfNodes(), 3);
            assertTrue(compositeState1.comprisesAComposite());
            assertEquals(rg2.getStringValue(nameAttr, v2), v2name);
            final CompositeNodeState compositeState2 = (CompositeNodeState) rg2.getObjectValue(compositeAttr, v2);
            assertNotNull(compositeState2);
            assertEquals(compositeState2.getNumberOfNodes(), 3);
            assertTrue(compositeState2.comprisesAComposite());
            assertEquals(rg2.getStringValue(nameAttr, v3), v3name);
            final CompositeNodeState compositeState3 = (CompositeNodeState) rg2.getObjectValue(compositeAttr, v3);
            assertNull(compositeState3);

            // assert that the original transactions exist and none others
            assertEquals(rg2.getTransactionCount(), 2);
            final int l0To1 = rg2.getLink(v0, v1);
            assertEquals(rg2.getLinkTransactionCount(l0To1), 1);
            final int t0To1 = rg2.getLinkTransaction(l0To1, 0);
            assertFalse(rg2.getTransactionDirection(t0To1) == Graph.FLAT);
            assertEquals(rg2.getTransactionSourceVertex(t0To1), v0);
            final int l1To2 = rg2.getLink(v1, v2);
            assertEquals(rg2.getLinkTransactionCount(l1To2), 1);
            final int t1To2 = rg2.getLinkTransaction(l1To2, 0);
            assertFalse(rg2.getTransactionDirection(t1To2) == Graph.FLAT);
            assertEquals(rg2.getTransactionSourceVertex(t1To2), v1);
        }
    }

    @Test
    public void compositeNonCompositeAndExpandedConstituentTest() throws InterruptedException, PluginException {
        final WritableGraph wg = graph.getWritableGraph("test", true);
        wg.getSchema().newGraph(wg);
        
        int v0;
        int v1;
        int v2;
        int v3;
        
        final String v0name;
        final String v1name;
        final String v2name;
        final String v3name;
        
        final int nameAttr;
        final int selectedAttr;
        final int compositeAttr;
        
        try {
            // Add four vertices
            v0 = wg.addVertex();
            wg.getSchema().newVertex(wg, v0);
            v1 = wg.addVertex();
            wg.getSchema().newVertex(wg, v1);
            v2 = wg.addVertex();
            wg.getSchema().newVertex(wg, v2);
            v3 = wg.addVertex();
            wg.getSchema().newVertex(wg, v3);

            // Store the names of these vertices
            nameAttr = VisualConcept.VertexAttribute.LABEL.get(wg);
            v0name = wg.getStringValue(nameAttr, v0);
            v1name = wg.getStringValue(nameAttr, v1);
            v2name = wg.getStringValue(nameAttr, v2);
            v3name = wg.getStringValue(nameAttr, v3);

            // Add transactions from v0 to v1, and from v1 to v2
            final int t0To1 = wg.addTransaction(v0, v1, true);
            wg.getSchema().newTransaction(wg, t0To1);
            final int t1To2 = wg.addTransaction(v1, v2, true);
            wg.getSchema().newTransaction(wg, t1To2);

            // Select v0 and v1, but not v2 or v3
            selectedAttr = VisualConcept.VertexAttribute.SELECTED.get(wg);
            wg.setBooleanValue(selectedAttr, v0, true);
            wg.setBooleanValue(selectedAttr, v1, true);
            wg.setBooleanValue(selectedAttr, v2, false);
            wg.setBooleanValue(selectedAttr, v3, false);

            // Make a composite from the selection then expand all composites, then finally deselect all
            PluginExecutor.startWith(PluginRegistry.get(InteractiveGraphPluginRegistry.CREATE_COMPOSITE_FROM_SELECTION))
                    .followedBy(PluginRegistry.get(InteractiveGraphPluginRegistry.EXPAND_ALL_COMPOSITES))
                    .followedBy(PluginRegistry.get(VisualGraphPluginRegistry.DESELECT_ALL))
                    .executeNow(wg);
            compositeAttr = AnalyticConcept.VertexAttribute.COMPOSITE_STATE.get(wg);

            // Select v2 and the expanded vertex corresponding to v0
            v0 = Graph.NOT_FOUND;
            for (int i = 0; i < 4; i++) {
                final int id = wg.getVertex(i);
                final String name = wg.getStringValue(nameAttr, id);
                if (name.equals(v0name)) {
                    v0 = id;
                    break;
                }
            }

            wg.setBooleanValue(selectedAttr, v2, true);
            wg.setBooleanValue(selectedAttr, v0, true);

            // Make a composite from the selection
            PluginExecution.withPlugin(PluginRegistry.get(InteractiveGraphPluginRegistry.CREATE_COMPOSITE_FROM_SELECTION)).executeNow(wg);
        } finally {
            wg.commit();
        }
        
        try (final ReadableGraph rg = graph.getReadableGraph()) {
            // Assert that there is one vertex that is a composite containing two nodes,
            // and both v1 and v3 as non-composite
            assertEquals(rg.getVertexCount(), 3);
            int compositeNode = Graph.NOT_FOUND;
            v1 = Graph.NOT_FOUND;
            for (int i = 0; i < 3; i++) {
                final int id = wg.getVertex(i);
                final String name = wg.getStringValue(nameAttr, id);
                if (name.equals(v1name)) {
                    v1 = id;
                } else if (id != v3) {
                    compositeNode = id;
                }
            }
            final CompositeNodeState compositeState = (CompositeNodeState) rg.getObjectValue(compositeAttr, compositeNode);
            assertNotNull(compositeState);
            assertEquals(compositeState.getNumberOfNodes(), 2);
            assertTrue(compositeState.isComposite());

            assertEquals(rg.getStringValue(nameAttr, v3), v3name);
            final CompositeNodeState compositeState0 = (CompositeNodeState) rg.getObjectValue(compositeAttr, v3);
            assertNull(compositeState0);
            assertEquals(rg.getStringValue(nameAttr, v1), v1name);
            final CompositeNodeState compositeState1 = (CompositeNodeState) rg.getObjectValue(compositeAttr, v1);
            assertNull(compositeState1);

            // Assert that there are two transactions between the composite and v1
            // (one for each of the constituents, should be in different directions), and no others.
            assertEquals(rg.getTransactionCount(), 2);
            final int lcTo1 = rg.getLink(compositeNode, v1);
            assertEquals(rg.getLinkTransactionCount(lcTo1), 2);
            final int tcTo1To0 = rg.getLinkTransaction(lcTo1, 0);
            assertFalse(rg.getTransactionDirection(tcTo1To0) == Graph.FLAT);
            final int tcTo1To1 = rg.getLinkTransaction(lcTo1, 1);
            assertFalse(rg.getTransactionDirection(tcTo1To1) == Graph.FLAT);
            assertTrue(rg.getTransactionSourceVertex(tcTo1To0) != rg.getTransactionSourceVertex(tcTo1To1));
        }

        final WritableGraph wg2 = graph.getWritableGraph("test", true);
        try {
            // Expand all composites
            PluginExecution.withPlugin(PluginRegistry.get(InteractiveGraphPluginRegistry.EXPAND_ALL_COMPOSITES)).executeNow(wg2);
        } finally {
            wg2.commit();
        }
        
        try (final ReadableGraph rg2 = graph.getReadableGraph()) {
            // Assert that the original four vertices exist; v2 and v0 with expanded composite states
            assertEquals(rg2.getVertexCount(), 4);
            v0 = Graph.NOT_FOUND;
            v1 = Graph.NOT_FOUND;
            v2 = Graph.NOT_FOUND;
            v3 = Graph.NOT_FOUND;
            for (int i = 0; i < 4; i++) {
                final int id = rg2.getVertex(i);
                final String name = rg2.getStringValue(nameAttr, id);
                if (name.equals(v0name)) {
                    v0 = id;
                } else if (name.equals(v1name)) {
                    v1 = id;
                } else if (name.equals(v2name)) {
                    v2 = id;
                } else {
                    v3 = id;
                }
            }
            assertEquals(rg2.getStringValue(nameAttr, v0), v0name);
            final CompositeNodeState compositeState0 = (CompositeNodeState) rg2.getObjectValue(compositeAttr, v0);
            assertNotNull(compositeState0);
            assertEquals(compositeState0.getNumberOfNodes(), 2);
            assertFalse(compositeState0.isComposite());
            assertEquals(rg2.getStringValue(nameAttr, v1), v1name);
            final CompositeNodeState compositeState1 = (CompositeNodeState) rg2.getObjectValue(compositeAttr, v1);
            assertNull(compositeState1);
            assertEquals(rg2.getStringValue(nameAttr, v2), v2name);
            final CompositeNodeState compositeState2 = (CompositeNodeState) rg2.getObjectValue(compositeAttr, v2);
            assertNotNull(compositeState2);
            assertEquals(compositeState2.getNumberOfNodes(), 2);
            assertFalse(compositeState2.isComposite());
            assertEquals(rg2.getStringValue(nameAttr, v3), v3name);
            final CompositeNodeState compositeState3 = (CompositeNodeState) rg2.getObjectValue(compositeAttr, v3);
            assertNull(compositeState3);

            // assert that the original transactions exist and none others
            assertEquals(rg2.getTransactionCount(), 2);
            final int l0To1 = rg2.getLink(v0, v1);
            assertEquals(rg2.getLinkTransactionCount(l0To1), 1);
            final int t0To1 = rg2.getLinkTransaction(l0To1, 0);
            assertFalse(rg2.getTransactionDirection(t0To1) == Graph.FLAT);
            assertEquals(rg2.getTransactionSourceVertex(t0To1), v0);
            final int l1To2 = rg2.getLink(v1, v2);
            assertEquals(rg2.getLinkTransactionCount(l1To2), 1);
            final int t1To2 = rg2.getLinkTransaction(l1To2, 0);
            assertFalse(rg2.getTransactionDirection(t1To2) == Graph.FLAT);
            assertEquals(rg2.getTransactionSourceVertex(t1To2), v1);
        }
    }

    @Test
    public void destroyContractedCompositeTest() throws InterruptedException, PluginException {
        final WritableGraph wg = graph.getWritableGraph("test", true);
        wg.getSchema().newGraph(wg);
        
        int v0;
        int v1;
        int v2;
        
        final String v0name;
        final String v1name;
        final String v2name;
        
        final int nameAttr;
        final int selectedAttr;
        final int compositeAttr;
        
        try {
            // Add three vertices
            v0 = wg.addVertex();
            wg.getSchema().newVertex(wg, v0);
            v1 = wg.addVertex();
            wg.getSchema().newVertex(wg, v1);
            v2 = wg.addVertex();
            wg.getSchema().newVertex(wg, v2);

            // Store the names of these vertices
            nameAttr = VisualConcept.VertexAttribute.LABEL.get(wg);
            v0name = wg.getStringValue(nameAttr, v0);
            v1name = wg.getStringValue(nameAttr, v1);
            v2name = wg.getStringValue(nameAttr, v2);

            // Add transactions from v0 to v1, and from v1 to v2
            final int t0To1 = wg.addTransaction(v0, v1, true);
            wg.getSchema().newTransaction(wg, t0To1);
            final int t1To2 = wg.addTransaction(v1, v2, true);
            wg.getSchema().newTransaction(wg, t1To2);

            // Select v0 and v1, but not v2
            selectedAttr = VisualConcept.VertexAttribute.SELECTED.get(wg);
            wg.setBooleanValue(selectedAttr, v0, true);
            wg.setBooleanValue(selectedAttr, v1, true);
            wg.setBooleanValue(selectedAttr, v2, false);

            // Make a composite from the selection then destroy all composites
            PluginExecution.withPlugin(PluginRegistry.get(InteractiveGraphPluginRegistry.CREATE_COMPOSITE_FROM_SELECTION)).executeNow(wg);
            compositeAttr = AnalyticConcept.VertexAttribute.COMPOSITE_STATE.get(wg);
            CompositeUtilities.destroyAllComposites(wg);
        } finally {
            wg.commit();
        }
        
        try (final ReadableGraph rg = graph.getReadableGraph()) {
            // Assert that there are three vertices, all with null composite states and their original names
            assertEquals(rg.getVertexCount(), 3);
            v0 = Graph.NOT_FOUND;
            v1 = Graph.NOT_FOUND;
            v2 = Graph.NOT_FOUND;
            for (int i = 0; i < 3; i++) {
                final int id = rg.getVertex(i);
                final String name = rg.getStringValue(nameAttr, id);
                if (name.equals(v0name)) {
                    v0 = id;
                } else if (name.equals(v1name)) {
                    v1 = id;
                } else {
                    v2 = id;
                }
            }
            assertEquals(rg.getStringValue(nameAttr, v0), v0name);
            assertNull(rg.getObjectValue(compositeAttr, v0));
            assertEquals(rg.getStringValue(nameAttr, v1), v1name);
            assertNull(rg.getObjectValue(compositeAttr, v1));
            assertEquals(rg.getStringValue(nameAttr, v2), v2name);
            assertNull(rg.getObjectValue(compositeAttr, v2));

            // Assert that the two transactions exist as expected and no others
            final int l0To1 = rg.getLink(v0, v1);
            assertEquals(rg.getLinkTransactionCount(l0To1), 1);
            final int t0To1 = rg.getLinkTransaction(l0To1, 0);
            assertFalse(rg.getTransactionDirection(t0To1) == Graph.FLAT);
            assertEquals(rg.getTransactionSourceVertex(t0To1), v0);
            final int l1To2 = rg.getLink(v1, v2);
            assertEquals(rg.getLinkTransactionCount(l1To2), 1);
            final int t1To2 = rg.getLinkTransaction(l1To2, 0);
            assertFalse(rg.getTransactionDirection(t1To2) == Graph.FLAT);
            assertEquals(rg.getTransactionSourceVertex(t1To2), v1);
            final int l0To2 = rg.getLink(v0, v2);
            assertEquals(l0To2, Graph.NOT_FOUND);
        }
    }

    @Test
    public void destroyExpandedCompositeTest() throws InterruptedException, PluginException {
        final WritableGraph wg = graph.getWritableGraph("test", true);
        wg.getSchema().newGraph(wg);
        
        int v0;
        int v1;
        int v2;
        
        final String v0name;
        final String v1name;
        final String v2name;
        
        final int nameAttr;
        final int selectedAttr;
        final int compositeAttr;
        
        try {
            // Add three vertices
            v0 = wg.addVertex();
            wg.getSchema().newVertex(wg, v0);
            v1 = wg.addVertex();
            wg.getSchema().newVertex(wg, v1);
            v2 = wg.addVertex();
            wg.getSchema().newVertex(wg, v2);

            // Store the names of these vertices
            nameAttr = VisualConcept.VertexAttribute.LABEL.get(wg);
            v0name = wg.getStringValue(nameAttr, v0);
            v1name = wg.getStringValue(nameAttr, v1);
            v2name = wg.getStringValue(nameAttr, v2);

            // Add transactions from v0 to v1, and from v1 to v2
            final int t0_1 = wg.addTransaction(v0, v1, true);
            wg.getSchema().newTransaction(wg, t0_1);
            final int t1_2 = wg.addTransaction(v1, v2, true);
            wg.getSchema().newTransaction(wg, t1_2);

            // Select v0 and v1, but not v2
            selectedAttr = VisualConcept.VertexAttribute.SELECTED.get(wg);
            wg.setBooleanValue(selectedAttr, v0, true);
            wg.setBooleanValue(selectedAttr, v1, true);
            wg.setBooleanValue(selectedAttr, v2, false);

            // Make a composite from the selection, exapnd all composites, then destroy all composites
            PluginExecutor.startWith(PluginRegistry.get(InteractiveGraphPluginRegistry.CREATE_COMPOSITE_FROM_SELECTION))
                    .followedBy(PluginRegistry.get(InteractiveGraphPluginRegistry.EXPAND_ALL_COMPOSITES))
                    .executeNow(wg);
            compositeAttr = AnalyticConcept.VertexAttribute.COMPOSITE_STATE.get(wg);
            CompositeUtilities.destroyAllComposites(wg);
        } finally {
            wg.commit();
        }
        
        try (final ReadableGraph rg = graph.getReadableGraph()) {
            // Assert that there are three vertices, all with null composite states and their original names
            assertEquals(rg.getVertexCount(), 3);
            for (int i = 0; i < 3; i++) {
                final int id = rg.getVertex(i);
                final String name = rg.getStringValue(nameAttr, id);
                if (name.equals(v0name)) {
                    v0 = id;
                } else if (name.equals(v1name)) {
                    v1 = id;
                } else {
                    v2 = id;
                }
            }
            assertEquals(rg.getStringValue(nameAttr, v0), v0name);
            assertNull(rg.getObjectValue(compositeAttr, v0));
            assertEquals(rg.getStringValue(nameAttr, v1), v1name);
            assertNull(rg.getObjectValue(compositeAttr, v1));
            assertEquals(rg.getStringValue(nameAttr, v2), v2name);
            assertNull(rg.getObjectValue(compositeAttr, v2));

            // Assert that the two transactions exist as expected and no others
            final int l0To1 = rg.getLink(v0, v1);
            assertEquals(rg.getLinkTransactionCount(l0To1), 1);
            final int t0To1 = rg.getLinkTransaction(l0To1, 0);
            assertFalse(rg.getTransactionDirection(t0To1) == Graph.FLAT);
            assertEquals(rg.getTransactionSourceVertex(t0To1), v0);
            final int l1To2 = rg.getLink(v1, v2);
            assertEquals(rg.getLinkTransactionCount(l1To2), 1);
            final int t1To2 = rg.getLinkTransaction(l1To2, 0);
            assertFalse(rg.getTransactionDirection(t1To2) == Graph.FLAT);
            assertEquals(rg.getTransactionSourceVertex(t1To2), v1);
            final int l0To2 = rg.getLink(v0, v2);
            assertEquals(l0To2, Graph.NOT_FOUND);
        }
    }

    // This test checks that expanding composite node states that have been copied to another graph do not affect the
    // composite node states in the original graph.
    @Test
    public void ensureCompositeStateImmutabilityTest() throws InterruptedException, PluginException {
        final WritableGraph wg = graph.getWritableGraph("test", true);
        wg.getSchema().newGraph(wg);
        
        int v0;
        int v1;
        int v2;
        
        final String v0name;
        final String v1name;
        final String v2name;
        
        final int nameAttr;
        final int selectedAttr;
        final Plugin copyPlugin;
        
        try {
            // Add three vertices
            v0 = wg.addVertex();
            wg.getSchema().newVertex(wg, v0);
            v1 = wg.addVertex();
            wg.getSchema().newVertex(wg, v1);
            v2 = wg.addVertex();
            wg.getSchema().newVertex(wg, v2);

            // Store the names of these vertices
            nameAttr = VisualConcept.VertexAttribute.LABEL.get(wg);
            v0name = wg.getStringValue(nameAttr, v0);
            v1name = wg.getStringValue(nameAttr, v1);
            v2name = wg.getStringValue(nameAttr, v2);

            // Add a transaction from v0 to v1
            final int t0_1 = wg.addTransaction(v0, v1, true);
            wg.getSchema().newTransaction(wg, t0_1);

            // Select v0 and v2, but not v1
            selectedAttr = VisualConcept.VertexAttribute.SELECTED.get(wg);
            wg.setBooleanValue(selectedAttr, v0, true);
            wg.setBooleanValue(selectedAttr, v2, true);
            wg.setBooleanValue(selectedAttr, v1, false);
        } finally {
            wg.commit();
        }

        // Make a composite from the selection, then select everything, and copy to a new graph
        copyPlugin = PluginRegistry.get(InteractiveGraphPluginRegistry.COPY_TO_NEW_GRAPH);
        PluginExecutor.startWith(InteractiveGraphPluginRegistry.CREATE_COMPOSITE_FROM_SELECTION)
                .followedBy(VisualGraphPluginRegistry.SELECT_ALL)
                .followedBy(copyPlugin)
                .executeNow(graph);

        final WritableGraph copyWg = ((CopyToNewGraphPlugin) copyPlugin).getCopy().getWritableGraph("test", true);
        try {
            // Expand all composites
            PluginExecution.withPlugin(InteractiveGraphPluginRegistry.EXPAND_ALL_COMPOSITES).executeNow(copyWg);
        } finally {
            copyWg.commit();
        }

        // Now we check there has been no effect on the composite state on the original graph
        // by expanding it and then checking it is the same as the orignal graph
        final WritableGraph wg2 = graph.getWritableGraph("test", true);
        try {
            // Expand all composites
            PluginExecution.withPlugin(InteractiveGraphPluginRegistry.EXPAND_ALL_COMPOSITES).executeNow(wg2);
        } finally {
            wg2.commit();
        }
        
        try (final ReadableGraph rg = graph.getReadableGraph()) {
            // Assert that there are three vertices all with their original names
            assertEquals(rg.getVertexCount(), 3);
            for (int i = 0; i < 3; i++) {
                final int id = rg.getVertex(i);
                final String name = rg.getStringValue(nameAttr, id);
                if (name.equals(v0name)) {
                    v0 = id;
                } else if (name.equals(v1name)) {
                    v1 = id;
                } else {
                    v2 = id;
                }
            }
            assertEquals(rg.getStringValue(nameAttr, v0), v0name);
            assertEquals(rg.getStringValue(nameAttr, v1), v1name);
            assertEquals(rg.getStringValue(nameAttr, v2), v2name);

            // Assert that the two transactions exist as expected and no others
            assertEquals(rg.getTransactionCount(), 1);
            final int l0To1 = rg.getLink(v0, v1);
            assertEquals(rg.getLinkTransactionCount(l0To1), 1);
            final int t0To1 = rg.getLinkTransaction(l0To1, 0);
            assertFalse(rg.getTransactionDirection(t0To1) == Graph.FLAT);
            assertEquals(rg.getTransactionSourceVertex(t0To1), v0);
            final int l1To2 = rg.getLink(v1, v2);
            assertEquals(l1To2, Graph.NOT_FOUND);
            final int l0To2 = rg.getLink(v0, v2);
            assertEquals(l0To2, Graph.NOT_FOUND);
        }
    }
}
