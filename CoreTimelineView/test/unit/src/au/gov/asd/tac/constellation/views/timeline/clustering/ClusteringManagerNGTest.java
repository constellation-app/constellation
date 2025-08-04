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
package au.gov.asd.tac.constellation.views.timeline.clustering;

import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.TemporalConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.views.timeline.TimeExtents;
import au.gov.asd.tac.constellation.views.timeline.clustering.ClusteringManager.UpdateDimOrHidePlugin;
import org.eclipse.emf.ecore.resource.URIConverter;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class ClusteringManagerNGTest {

    public ClusteringManagerNGTest() {
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
     * Test of generateTree method, of class ClusteringManager.
     */
    @Test
    public void testGenerateTree() throws Exception {
        System.out.println("generateTree");
        final DualGraph graph = createGraph();
        final String datetimeAttribute = "DateTime";
        final boolean selectedOnly = false;

        final ClusteringManager instance = new ClusteringManager();

        final TimeExtents result = instance.generateTree(graph.getReadableGraph(), datetimeAttribute, selectedOnly);

        //TimeExtents
        assertNotNull(result);
        assertEquals(result.getClass(), TimeExtents.class);
    }

//    /**
//     * Test of filterTree method, of class ClusteringManager.
//     */
//    @Test
//    public void testFilterTree() {
//        System.out.println("filterTree");
//        double pixelsPerTransaction = 0.0;
//        long lowerTimeExtent = 0L;
//        long upperTimeExtent = 0L;
//        ClusteringManager instance = new ClusteringManager();
//        instance.filterTree(pixelsPerTransaction, lowerTimeExtent, upperTimeExtent);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of dimOrHideTree method, of class ClusteringManager.
//     */
//    @Test
//    public void testDimOrHideTree() {
//        System.out.println("dimOrHideTree");
//        long lowerTimeExtent = 0L;
//        long upperTimeExtent = 0L;
//        int exclusionState = 0;
//        ClusteringManager instance = new ClusteringManager();
//        instance.dimOrHideTree(lowerTimeExtent, upperTimeExtent, exclusionState);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getLowestObservedTime method, of class ClusteringManager.
//     */
//    @Test
//    public void testGetLowestObservedTime() {
//        System.out.println("getLowestObservedTime");
//        ClusteringManager instance = new ClusteringManager();
//        long expResult = 0L;
//        long result = instance.getLowestObservedTime();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getHighestObservedTime method, of class ClusteringManager.
//     */
//    @Test
//    public void testGetHighestObservedTime() {
//        System.out.println("getHighestObservedTime");
//        ClusteringManager instance = new ClusteringManager();
//        long expResult = 0L;
//        long result = instance.getHighestObservedTime();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getElementsToDraw method, of class ClusteringManager.
//     */
//    @Test
//    public void testGetElementsToDraw() {
//        System.out.println("getElementsToDraw");
//        ClusteringManager instance = new ClusteringManager();
//        Set expResult = null;
//        Set result = instance.getElementsToDraw();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of cleanupVariables method, of class ClusteringManager.
//     */
//    @Test
//    public void testCleanupVariables() {
//        System.out.println("cleanupVariables");
//        ClusteringManager instance = new ClusteringManager();
//        instance.cleanupVariables();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of clearTree method, of class ClusteringManager.
//     */
//    @Test
//    public void testClearTree() {
//        System.out.println("clearTree");
//        ClusteringManager instance = new ClusteringManager();
//        instance.clearTree();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    @Test
    public void testUpdateDimOrHidePluginExecute() throws Exception {
        System.out.println("UpdateDimOrHidePlugin execute");

        // Generate Tree
        final DualGraph graph = createGraph();
        final String datetimeAttribute = "DateTime";
        final boolean selectedOnly = false;

        final ClusteringManager clusteringManager = new ClusteringManager();
        try (final ReadableGraph rg = graph.getReadableGraph()) {
            clusteringManager.generateTree(rg, datetimeAttribute, selectedOnly);
        }
        // Dim or Hide tree
        // Execute plugin

        final ClusteringManager.ExclusionStateNotifier mockNotifier = mock(ClusteringManager.ExclusionStateNotifier.class);
        final long lowerTimeExtent = 0;
        final long upperTimeExtent = 1422230401001L;
        //final long upperTimeExtent = Long.MAX_VALUE;
        final int exclusionState = 1;

        final ClusteringManager.UpdateDimOrHidePlugin plugin = clusteringManager.new UpdateDimOrHidePlugin(lowerTimeExtent, upperTimeExtent, exclusionState, mockNotifier);

        final PluginGraphs mockPluginGraphs = mock(PluginGraphs.class);
//        final Graph mockGraph = mock(Graph.class);
//        final WritableGraph mockWritableGraph = mock(WritableGraph.class);
//
        when(mockPluginGraphs.getGraph()).thenReturn(graph);
//        when(mockGraph.getWritableGraph(anyString(), anyBoolean(), any())).thenReturn(mockWritableGraph);

        plugin.execute(mockPluginGraphs, null, null);

        try (final ReadableGraph rg = graph.getReadableGraph()) {
            final int vertDimAttr = VisualConcept.VertexAttribute.DIMMED.get(rg);
            final int vertHideAttr = VisualConcept.VertexAttribute.VISIBILITY.get(rg);

            for (int pos = 0; pos < rg.getVertexCount(); pos++) {
                int vxID = rg.getVertex(pos);
                System.out.println(rg.getBooleanValue(vertDimAttr, vxID) + " " + rg.getIntValue(vertHideAttr, vxID));
            }
        }
    }

    private DualGraph createGraph() throws Exception {
        // create an analytic graph
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema();
        final DualGraph dualGraph = new DualGraph(schema);

        final WritableGraph wg = dualGraph.getWritableGraph("", true);
        try {
            // add vertices
            final int vxId0 = wg.addVertex();
            final int vxId1 = wg.addVertex();
            final int vxId2 = wg.addVertex();
            final int vxId3 = wg.addVertex();
            final int vxId4 = wg.addVertex();

            // add transactions
            final int tId0 = wg.addTransaction(vxId0, vxId1, false);
            final int tId1 = wg.addTransaction(vxId1, vxId2, false);
            final int tId2 = wg.addTransaction(vxId1, vxId3, false);
            final int tId3 = wg.addTransaction(vxId2, vxId3, false);
            final int tId4 = wg.addTransaction(vxId3, vxId4, false);

            final int selectedVertexAttr = VisualConcept.VertexAttribute.SELECTED.ensure(wg);
            final int selectedTransactionAttr = VisualConcept.TransactionAttribute.SELECTED.ensure(wg);
            final int datetimeAttributeId = TemporalConcept.TransactionAttribute.DATETIME.ensure(wg);

            // make the selection
            wg.setBooleanValue(selectedVertexAttr, vxId0, true);
            wg.setBooleanValue(selectedVertexAttr, vxId2, true);

            //ZonedDateTime min = ZonedDateTime.ofInstant(Instant.now(), TimeZoneUtilities.UTC);
            wg.setStringValue(datetimeAttributeId, tId0, "2015-01-26 00:00:01.000 +00:00 [UTC]");
            wg.setStringValue(datetimeAttributeId, tId1, "2015-01-27 00:00:01.000 +00:00 [UTC]");
            wg.setStringValue(datetimeAttributeId, tId2, "2015-01-28 00:00:01.000 +00:00 [UTC]");
            wg.setStringValue(datetimeAttributeId, tId3, "2015-01-29 00:00:01.000 +00:00 [UTC]");
            wg.setStringValue(datetimeAttributeId, tId4, "2015-01-30 00:00:01.000 +00:00 [UTC]");

        } finally {
            wg.commit();
        }

        return dualGraph;
    }
}
