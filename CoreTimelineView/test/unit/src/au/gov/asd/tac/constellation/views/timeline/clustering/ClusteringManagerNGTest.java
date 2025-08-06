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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
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

    @BeforeClass
    public static void setUpClass() throws Exception {
        // Left intentionally empty
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Left intentionally empty
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Left intentionally empty
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Left intentionally empty
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

    @Test
    public void testUpdateDimOrHidePluginFullDim() throws Exception {
        System.out.println("UpdateDimOrHidePlugin execute full dim");

        // Setup arguments
        final DualGraph graph = createGraph();
        final long lowerTimeExtent = 0L;
        final long upperTimeExtent = 1L;

        final int exclusionState = 1;

        updateDimOrHidePluginHelper(graph, lowerTimeExtent, upperTimeExtent, exclusionState);

        try (final ReadableGraph rg = graph.getReadableGraph()) {
            final int vertDimAttr = VisualConcept.VertexAttribute.DIMMED.get(rg);

            for (int pos = 0; pos < rg.getVertexCount(); pos++) {
                int vxID = rg.getVertex(pos);
                assertTrue(rg.getBooleanValue(vertDimAttr, vxID));
            }
        }
    }

    @Test
    public void testUpdateDimOrHidePluginPartialDim() throws Exception {
        System.out.println("UpdateDimOrHidePlugin execute partial dim");

        // Setup arguments
        final DualGraph graph = createGraph();
        final long lowerTimeExtent = 94677120000L;
        final long upperTimeExtent = 946944000000L;

        final int exclusionState = 1;

        updateDimOrHidePluginHelper(graph, lowerTimeExtent, upperTimeExtent, exclusionState);

        final boolean[] expectedResults = {false, false, false, false, true};

        try (final ReadableGraph rg = graph.getReadableGraph()) {
            final int vertDimAttr = VisualConcept.VertexAttribute.DIMMED.get(rg);

            for (int pos = 0; pos < rg.getVertexCount(); pos++) {
                int vxID = rg.getVertex(pos);
                assertEquals(rg.getBooleanValue(vertDimAttr, vxID), expectedResults[pos]);
            }
        }
    }

    @Test
    public void testUpdateDimOrHidePluginFullHide() throws Exception {
        System.out.println("UpdateDimOrHidePlugin execute full hide");

        // Setup arguments
        final DualGraph graph = createGraph();
        final long lowerTimeExtent = 0L;
        final long upperTimeExtent = 1L;
        final int exclusionState = 2;

        final int expectedResult = 0;

        updateDimOrHidePluginHelper(graph, lowerTimeExtent, upperTimeExtent, exclusionState);

        try (final ReadableGraph rg = graph.getReadableGraph()) {
            final int vertHideAttr = VisualConcept.VertexAttribute.VISIBILITY.get(rg);

            for (int pos = 0; pos < rg.getVertexCount(); pos++) {
                int vxID = rg.getVertex(pos);
                assertEquals(rg.getIntValue(vertHideAttr, vxID), expectedResult);
            }
        }
    }

    @Test
    public void testUpdateDimOrHidePluginPartialHide() throws Exception {
        System.out.println("UpdateDimOrHidePlugin execute partial hide");

        // Setup arguments
        final DualGraph graph = createGraph();
        final long lowerTimeExtent = 94677120000L;
        final long upperTimeExtent = 946944000000L;
        final int exclusionState = 2;

        final int[] expectedResults = {1, 1, 1, 1, 0};

        updateDimOrHidePluginHelper(graph, lowerTimeExtent, upperTimeExtent, exclusionState);

        try (final ReadableGraph rg = graph.getReadableGraph()) {
            final int vertHideAttr = VisualConcept.VertexAttribute.VISIBILITY.get(rg);

            for (int pos = 0; pos < rg.getVertexCount(); pos++) {
                int vxID = rg.getVertex(pos);
                assertEquals(rg.getIntValue(vertHideAttr, vxID), expectedResults[pos]);
            }
        }
    }

    private void updateDimOrHidePluginHelper(final DualGraph graph, final long lowerTimeExtent, final long upperTimeExtent, final int exclusionState) throws Exception {

        final String datetimeAttribute = "DateTime";
        final boolean selectedOnly = false;

        final ClusteringManager clusteringManager = new ClusteringManager();
        try (final ReadableGraph rg = graph.getReadableGraph()) {
            clusteringManager.generateTree(rg, datetimeAttribute, selectedOnly);
        }

        final ClusteringManager.ExclusionStateNotifier mockNotifier = mock(ClusteringManager.ExclusionStateNotifier.class);

        final PluginGraphs mockPluginGraphs = mock(PluginGraphs.class);
        when(mockPluginGraphs.getGraph()).thenReturn(graph);

        // Execute plugin to setup tree
        final ClusteringManager.UpdateDimOrHidePlugin plugin1 = clusteringManager.new UpdateDimOrHidePlugin(0, Long.MAX_VALUE, exclusionState, mockNotifier);
        plugin1.execute(mockPluginGraphs, null, null);

        // Execute plugin with desired upper and lower extents
        final ClusteringManager.UpdateDimOrHidePlugin plugin2 = clusteringManager.new UpdateDimOrHidePlugin(lowerTimeExtent, upperTimeExtent, exclusionState, mockNotifier);
        plugin2.execute(mockPluginGraphs, null, null);
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
            VisualConcept.TransactionAttribute.SELECTED.ensure(wg);
            final int datetimeAttributeId = TemporalConcept.TransactionAttribute.DATETIME.ensure(wg);

            // make the selection
            wg.setBooleanValue(selectedVertexAttr, vxId0, true);
            wg.setBooleanValue(selectedVertexAttr, vxId2, true);

            //ZonedDateTime min = ZonedDateTime.ofInstant(Instant.now(), TimeZoneUtilities.UTC);
            wg.setStringValue(datetimeAttributeId, tId0, "2000-01-01 00:00:00.000 +00:00 [UTC]");
            wg.setStringValue(datetimeAttributeId, tId1, "2000-01-02 00:00:00.000 +00:00 [UTC]");
            wg.setStringValue(datetimeAttributeId, tId2, "2000-01-03 00:00:00.000 +00:00 [UTC]");
            wg.setStringValue(datetimeAttributeId, tId3, "2000-01-04 00:00:00.000 +00:00 [UTC]");
            wg.setStringValue(datetimeAttributeId, tId4, "2000-01-05 00:00:00.000 +00:00 [UTC]");

        } finally {
            wg.commit();
        }

        return dualGraph;
    }
}
