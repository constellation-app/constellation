/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.arrangements.time;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.TemporalConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import static au.gov.asd.tac.constellation.plugins.arrangements.time.LayerByTimePlugin.ARRANGE_2D_PARAMETER_ID;
import static au.gov.asd.tac.constellation.plugins.arrangements.time.LayerByTimePlugin.DATETIME_ATTRIBUTE_PARAMETER_ID;
import static au.gov.asd.tac.constellation.plugins.arrangements.time.LayerByTimePlugin.DATE_RANGE_PARAMETER_ID;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.DateTimeRange;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import java.time.ZonedDateTime;
import java.util.Map;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
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
public class LayerByTimePluginNGTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        // Left intentionally blank
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Left intentionally blank
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Left intentionally blank
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Left intentionally blank
    }

    /**
     * Test of createParameters method, of class LayerByTimePlugin.
     */
    @Test
    public void testCreateParameters() {
        System.out.println("createParameters");
        final LayerByTimePlugin instance = new LayerByTimePlugin();
        final PluginParameters result = instance.createParameters();
        final Map<String, PluginParameter<?>> params = result.getParameters();

        final String[] parameterKeyArray = {
            "LayerByTimePlugin.date_time_attribute",
            "LayerByTimePlugin.date_range",
            "LayerByTimePlugin.layer_by",
            "LayerByTimePlugin.amount",
            "LayerByTimePlugin.interval_unit",
            "LayerByTimePlugin.transaction_as_layer",
            "LayerByTimePlugin.keep_tx_colors",
            "LayerByTimePlugin.draw_tx_guides",
            "LayerByTimePlugin.arrange_in_2d",
            "LayerByTimePlugin.row_or_col",
            "LayerByTimePlugin.num_rows_or_cols",
            "LayerByTimePlugin.row_dist",
            "LayerByTimePlugin.col_dist",
            "LayerByTimePlugin.node_dist",
            "LayerByTimePlugin.how_to_arrange",
            "LayerByTimePlugin.oldest_or_newest"
        };

        assertEquals(params.size(), parameterKeyArray.length);

        for (final String key : parameterKeyArray) {
            assertTrue(params.containsKey(key));
        }
    }

    /**
     * Test of read method, of class LayerByTimePlugin.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testRead() throws Exception {
        System.out.println("read");
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        final StoreGraph storeGraph = new StoreGraph(schema);

        // Setup storeGraph
        final ZonedDateTime now = ZonedDateTime.now();

        final int xAttr = VisualConcept.VertexAttribute.X.ensure(storeGraph);
        final int yAttr = VisualConcept.VertexAttribute.Y.ensure(storeGraph);
        final int zAttr = VisualConcept.VertexAttribute.Z.ensure(storeGraph);

        final int txDateTimeAttr = TemporalConcept.TransactionAttribute.DATETIME.ensure(storeGraph);

        // add vertices
        final int vxId0 = storeGraph.addVertex();
        final int vxId1 = storeGraph.addVertex();
        final int vxId2 = storeGraph.addVertex();
        final int vxId3 = storeGraph.addVertex();
        final int[] vertArray = {vxId0, vxId1, vxId2, vxId3};

        // Set all vertices' postion to a default value
        for (final int vert : vertArray) {
            storeGraph.setFloatValue(xAttr, vert, 1F);
            storeGraph.setFloatValue(yAttr, vert, 2F);
            storeGraph.setFloatValue(zAttr, vert, 3F);
        }

        // add transactions
        final int txId0 = storeGraph.addTransaction(vxId0, vxId1, false);
        final int txId1 = storeGraph.addTransaction(vxId2, vxId3, false);

        storeGraph.setLongValue(txDateTimeAttr, txId0, now.plusDays(1).toInstant().toEpochMilli());
        storeGraph.setLongValue(txDateTimeAttr, txId1, now.plusDays(2).toInstant().toEpochMilli());

        final DualGraph dualGraph = spy(new DualGraph(schema, storeGraph));

        // Create and setup instance
        final LayerByTimePlugin instance = spy(new LayerByTimePlugin()); // Spy the instance to override copyGraph()
        doReturn(dualGraph).when(instance).copyGraph(storeGraph);

        final PluginParameters parameters = instance.createParameters();
        parameters.setStringValue(DATETIME_ATTRIBUTE_PARAMETER_ID, "DateTime");
        parameters.setBooleanValue(ARRANGE_2D_PARAMETER_ID, true);
        parameters.setDateTimeRangeValue(DATE_RANGE_PARAMETER_ID, new DateTimeRange(now, now.plusDays(4)));

        // Run function
        instance.updateParameters(dualGraph, parameters);
        instance.read(storeGraph, mock(PluginInteraction.class), parameters);

        // Verify nodes are in correct position
        try (final ReadableGraph rg = dualGraph.getReadableGraph()) {
            final Vector3f[] vertPosArray = {new Vector3f(0, 0, 0), new Vector3f(10, 0, 0), new Vector3f(10, -10, 0), new Vector3f(0, -10, 0)};

            for (int i = 0; i < rg.getVertexCount(); i++) {
                final int vert = rg.getVertex(i);
                final Vector3f expectedPos = vertPosArray[i];

                final float x = rg.getFloatValue(xAttr, vert);
                final float y = rg.getFloatValue(yAttr, vert);
                final float z = rg.getFloatValue(zAttr, vert);

                assertEquals(expectedPos.getX(), x);
                assertEquals(expectedPos.getY(), y);
                assertEquals(expectedPos.getZ(), z);
            }
        }

        verify(dualGraph, atLeast(1)).getWritableGraph("Layer by time", true);
    }

    @Test
    public void testCopyGraph() throws Exception {
        System.out.println("copyGraph");
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        final StoreGraph storeGraph = new StoreGraph(schema);

        final int xAttr = VisualConcept.VertexAttribute.X.ensure(storeGraph);
        final int yAttr = VisualConcept.VertexAttribute.Y.ensure(storeGraph);
        final int zAttr = VisualConcept.VertexAttribute.Z.ensure(storeGraph);

        // add vertices
        final int vxId0 = storeGraph.addVertex();
        final int vxId1 = storeGraph.addVertex();
        final int vxId2 = storeGraph.addVertex();
        final int vxId3 = storeGraph.addVertex();
        final int[] vertArray = {vxId0, vxId1, vxId2, vxId3};

        // Set all vertices' postion
        int count = 0;
        for (final int vert : vertArray) {
            storeGraph.setFloatValue(xAttr, vert, count);
            storeGraph.setFloatValue(yAttr, vert, count);
            storeGraph.setFloatValue(zAttr, vert, count);
            count++;
        }

        // Create instance and run function
        final LayerByTimePlugin instance = new LayerByTimePlugin();
        final Graph result = instance.copyGraph(storeGraph);

        // Verify the copied graph has the same vertices
        try (final ReadableGraph rg = result.getReadableGraph()) {
            for (int i = 0; i < rg.getVertexCount(); i++) {
                final int vert = rg.getVertex(i);

                final float x = rg.getFloatValue(xAttr, vert);
                final float y = rg.getFloatValue(yAttr, vert);
                final float z = rg.getFloatValue(zAttr, vert);

                assertEquals(storeGraph.getFloatValue(xAttr, vert), x);
                assertEquals(storeGraph.getFloatValue(yAttr, vert), y);
                assertEquals(storeGraph.getFloatValue(zAttr, vert), z);
            }
        }
    }

    @Test
    public void testCheckValuesExistInRange() throws Exception {
        System.out.println("checkValuesExistInRange");

        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        final StoreGraph storeGraph = new StoreGraph(schema);

        // Setup storeGraph
        final ZonedDateTime now = ZonedDateTime.now();
        final long date0 = now.plusDays(1).toInstant().toEpochMilli();
        final long date1 = now.plusDays(2).toInstant().toEpochMilli();
        final long date2 = now.plusDays(3).toInstant().toEpochMilli();
        final long date3 = now.plusDays(4).toInstant().toEpochMilli();

        final int txDateTimeAttr = TemporalConcept.TransactionAttribute.DATETIME.ensure(storeGraph);

        // add vertices
        final int vxId0 = storeGraph.addVertex();
        final int vxId1 = storeGraph.addVertex();
        final int vxId2 = storeGraph.addVertex();
        final int vxId3 = storeGraph.addVertex();

        // add transactions
        final int txId0 = storeGraph.addTransaction(vxId0, vxId1, false);
        final int txId1 = storeGraph.addTransaction(vxId2, vxId3, false);

        storeGraph.setLongValue(txDateTimeAttr, txId0, date0);
        storeGraph.setLongValue(txDateTimeAttr, txId1, date1);

        final LayerByTimePlugin instance = new LayerByTimePlugin();
        assertFalse(instance.checkValuesExistInRange(storeGraph, 0, 1, txDateTimeAttr));
        assertFalse(instance.checkValuesExistInRange(storeGraph, date2, date3, txDateTimeAttr));

        assertTrue(instance.checkValuesExistInRange(storeGraph, date0, date1, txDateTimeAttr));
        assertTrue(instance.checkValuesExistInRange(storeGraph, 0, date0, txDateTimeAttr));
        assertTrue(instance.checkValuesExistInRange(storeGraph, date1, date2, txDateTimeAttr));
    }
}
