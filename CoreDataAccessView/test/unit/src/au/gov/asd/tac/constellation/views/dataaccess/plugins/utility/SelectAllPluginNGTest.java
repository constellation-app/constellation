/*
 * Copyright 2010-2021 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.utility;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.BareSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import java.util.concurrent.CompletableFuture;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import org.openide.NotifyDescriptor;
import org.openide.awt.NotificationDisplayer;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * Cases of Edit tested: empty graph with visual schema, empty graph with bare
 * schema, empty graph visual schema with attributes, visual graph with two vx
 * and attributes, visual graph with two vx and two tx.
 *
 *
 * @author aldebaran30701
 */
public class SelectAllPluginNGTest {

    private static MockedStatic<NotifyDisplayer> notifyDisplayerMockedStatic;
    private static MockedStatic<NotificationDisplayer> notificationDisplayerMockedStatic;
    
    private Graph graph;
    private int selectedV, selectedT;
    private int vxId1, vxId2, txId1, txId2;

    @BeforeClass
    public static void beforeClass() {
        notifyDisplayerMockedStatic = Mockito.mockStatic(NotifyDisplayer.class);
        notificationDisplayerMockedStatic = Mockito.mockStatic(NotificationDisplayer.class);
        
        notifyDisplayerMockedStatic.when(() -> NotifyDisplayer.displayAndWait(any(NotifyDescriptor.class)))
                .thenReturn(CompletableFuture.completedFuture(null));
        
        notificationDisplayerMockedStatic.when(NotificationDisplayer::getDefault)
                .thenReturn(mock(NotificationDisplayer.class));
    }
    
    @AfterClass
    public static void afterClass() {
        notifyDisplayerMockedStatic.close();
        notificationDisplayerMockedStatic.close();
    }
    
    /**
     * Test of edit method, of class SelectAllPlugin. No attributes added so
     * should throw a pluginexception
     */
    @Test(expectedExceptions = PluginException.class)
    public void testEdit() throws Exception {
        System.out.println("select all edit with nothing on graph");
        // Open a new graph
        graph = new DualGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());

        // run select all plugin
        PluginExecution.withPlugin(new SelectAllPlugin()).executeNow(graph);
    }

    /**
     * Test of edit method, of class SelectAllPlugin. select all edit with only
     * attributes on graph
     */
    @Test
    public void testEdit0() throws Exception {
        System.out.println("select all edit with nothing on graph but attributes");
        // Open a new graph
        graph = new DualGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());
        int vxCount = 0;
        int txCount = 0;
        final WritableGraph wg = graph.getWritableGraph("TEST", true);
        try {
            // Create Selected Attributes
            selectedV = VisualConcept.VertexAttribute.SELECTED.ensure(wg);
            selectedT = VisualConcept.TransactionAttribute.SELECTED.ensure(wg);
        } finally {
            wg.commit();
        }

        // run select all plugin
        PluginExecution.withPlugin(new SelectAllPlugin()).executeNow(graph);

        // Verify no extra elements are added
        final ReadableGraph rg = graph.getReadableGraph();
        try {
            assertEquals(rg.getVertexCount(), vxCount);
            assertEquals(rg.getTransactionCount(), txCount);
        } finally {
            rg.close();
        }
    }

    /**
     * Test of edit method, of class SelectAllPlugin. select all edit with only
     * vx on graph
     */
    @Test
    public void testEdit1() throws Exception {
        System.out.println("select all edit with only vx on graph");
        // Open a new graph
        graph = new DualGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());
        int vxCount = 0;
        final WritableGraph wg = graph.getWritableGraph("TEST", true);
        try {
            // Create Selected Attributes
            selectedV = VisualConcept.VertexAttribute.SELECTED.ensure(wg);
            selectedT = VisualConcept.TransactionAttribute.SELECTED.ensure(wg);

            // Add vertices
            vxId1 = wg.addVertex();
            vxId2 = wg.addVertex();

            // check default value is unselected
            assertFalse(wg.getBooleanValue(selectedV, vxId1));
            assertFalse(wg.getBooleanValue(selectedV, vxId2));

            vxCount = wg.getVertexCount();
        } finally {
            wg.commit();
        }

        // run select all plugin
        PluginExecution.withPlugin(new SelectAllPlugin()).executeNow(graph);

        // Verify both selected and same amount of vx are present
        final ReadableGraph rg = graph.getReadableGraph();
        try {
            assertTrue(rg.getBooleanValue(selectedV, vxId1));
            assertTrue(rg.getBooleanValue(selectedV, vxId2));
            assertEquals(rg.getVertexCount(), vxCount);
        } finally {
            rg.close();
        }

        // rerun plugin to ensure values are not only toggled, but are set explicitly as selected
        // run select all plugin
        PluginExecution.withPlugin(new SelectAllPlugin()).executeNow(graph);

        // Verify both selected and same amount of vx are present
        final ReadableGraph rg2 = graph.getReadableGraph();
        try {
            assertTrue(rg2.getBooleanValue(selectedV, vxId1));
            assertTrue(rg2.getBooleanValue(selectedV, vxId2));
            assertEquals(rg2.getVertexCount(), vxCount);
        } finally {
            rg2.close();
        }
    }

    /**
     * Test of edit method, of class SelectAllPlugin. select all edit with both
     * vx and tx on graph
     */
    @Test
    public void testEdit2() throws Exception {
        System.out.println("select all edit with vx and tx on graph");
        // Open a new graph
        graph = new DualGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());
        int vxCount = 0;
        int txCount = 0;
        final WritableGraph wg = graph.getWritableGraph("TEST", true);
        try {
            // Create Selected Attributes
            selectedV = VisualConcept.VertexAttribute.SELECTED.ensure(wg);
            selectedT = VisualConcept.TransactionAttribute.SELECTED.ensure(wg);

            // Add vertices
            vxId1 = wg.addVertex();
            vxId2 = wg.addVertex();

            // Add transactions
            txId1 = wg.addTransaction(vxId1, vxId2, true);
            txId2 = wg.addTransaction(vxId2, vxId1, false);

            // check default value is unselected
            assertFalse(wg.getBooleanValue(selectedV, vxId1));
            assertFalse(wg.getBooleanValue(selectedV, vxId2));
            assertFalse(wg.getBooleanValue(selectedV, txId1));
            assertFalse(wg.getBooleanValue(selectedV, txId2));

            vxCount = wg.getVertexCount();
            txCount = wg.getTransactionCount();
        } finally {
            wg.commit();
        }

        // run select all plugin
        PluginExecution.withPlugin(new SelectAllPlugin()).executeNow(graph);

        // Verify both selected and same amount of vx and tx are present
        final ReadableGraph rg = graph.getReadableGraph();
        try {
            assertTrue(rg.getBooleanValue(selectedV, vxId1));
            assertTrue(rg.getBooleanValue(selectedV, vxId2));
            assertTrue(rg.getBooleanValue(selectedV, txId1));
            assertTrue(rg.getBooleanValue(selectedV, txId2));
            assertEquals(rg.getVertexCount(), vxCount);
            assertEquals(rg.getTransactionCount(), txCount);
        } finally {
            rg.close();
        }

        // rerun plugin to ensure values are not only toggled, but are set explicitly as selected
        // run select all plugin
        PluginExecution.withPlugin(new SelectAllPlugin()).executeNow(graph);

        // Verify both selected and same amount of vx and tx are present
        final ReadableGraph rg2 = graph.getReadableGraph();
        try {
            assertTrue(rg2.getBooleanValue(selectedV, vxId1));
            assertTrue(rg2.getBooleanValue(selectedV, vxId2));
            assertTrue(rg.getBooleanValue(selectedV, txId1));
            assertTrue(rg.getBooleanValue(selectedV, txId2));
            assertEquals(rg2.getVertexCount(), vxCount);
            assertEquals(rg.getTransactionCount(), txCount);
        } finally {
            rg2.close();
        }
    }

    /**
     * Test of edit method, of class SelectAllPlugin. select all edit with a
     * graph that has no selected element or graph elements via its schema.
     */
    @Test(expectedExceptions = PluginException.class)
    public void testEdit3() throws Exception {
        System.out.println("select all edit with no selected attribute or elements on graph");
        // Open a new graph
        graph = new DualGraph(SchemaFactoryUtilities.getSchemaFactory(BareSchemaFactory.NAME).createSchema());

        // run select all plugin
        PluginExecution.withPlugin(new SelectAllPlugin()).executeNow(graph);
    }

    /**
     * Test of edit method, of class SelectAllPlugin. select all edit with a
     * graph that has no selected element via its schema, but contains elements.
     *
     * Should throw a pluginexception
     */
    @Test(expectedExceptions = PluginException.class)
    public void testEdit4() throws Exception {
        System.out.println("select all edit with no selected attribute on graph, but with elements");
        // Open a new graph
        graph = new DualGraph(SchemaFactoryUtilities.getSchemaFactory(BareSchemaFactory.NAME).createSchema());

        int vxCount = 0;
        int txCount = 0;
        final WritableGraph wg = graph.getWritableGraph("TEST", true);
        try {

            // Add vertices
            vxId1 = wg.addVertex();
            vxId2 = wg.addVertex();

            // Add transactions
            txId1 = wg.addTransaction(vxId1, vxId2, true);
            txId2 = wg.addTransaction(vxId2, vxId1, false);

            vxCount = wg.getVertexCount();
            txCount = wg.getTransactionCount();

            // ensure both values are stored
            assertEquals(vxCount, 2);
            assertEquals(txCount, 2);
        } finally {
            wg.commit();
        }

        // run select all plugin
        PluginExecution.withPlugin(new SelectAllPlugin()).executeNow(graph);
    }
}
