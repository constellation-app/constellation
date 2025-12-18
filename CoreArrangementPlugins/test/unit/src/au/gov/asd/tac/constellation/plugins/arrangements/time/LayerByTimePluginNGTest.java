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
package au.gov.asd.tac.constellation.plugins.arrangements.time;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;
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

    public LayerByTimePluginNGTest() {
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
            "LayerByTimePlugin.per_layer_direction",
            "LayerByTimePlugin.num_rows_or_cols",
            "LayerByTimePlugin.row_or_col",
            "LayerByTimePlugin.in_layer_direction",
            "LayerByTimePlugin.layer_dist",
            "LayerByTimePlugin.row_col_dist"
        };

        assertEquals(params.size(), parameterKeyArray.length);

        for (final String key : parameterKeyArray) {
            assertTrue(params.containsKey(key));
        }
    }

//    /**
//     * Test of updateParameters method, of class LayerByTimePlugin.
//     */
//    @Test
//    public void testUpdateParameters() {
//        System.out.println("updateParameters");
//        Graph graph = null;
//        PluginParameters parameters = null;
//        LayerByTimePlugin instance = new LayerByTimePlugin();
//        instance.updateParameters(graph, parameters);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
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

        // Set all vertices' postion to 0
        for (final int vert : vertArray) {
            storeGraph.setFloatValue(xAttr, vert, 1F);
            storeGraph.setFloatValue(yAttr, vert, 2F);
            storeGraph.setFloatValue(zAttr, vert, 3F);
        }

        System.out.println("Printing initial storeGraph setup...");
        for (final int vert : vertArray) {
            final float x = storeGraph.getFloatValue(xAttr, vert);
            final float y = storeGraph.getFloatValue(yAttr, vert);
            final float z = storeGraph.getFloatValue(zAttr, vert);

            System.out.println("x " + x + " y " + y + " z " + z);
        }

        // add transactions
        final int txId0 = storeGraph.addTransaction(vxId0, vxId1, false);
        final int txId1 = storeGraph.addTransaction(vxId2, vxId3, false);

        storeGraph.setLongValue(txDateTimeAttr, txId0, now.plusDays(1).toInstant().toEpochMilli());
        storeGraph.setLongValue(txDateTimeAttr, txId1, now.plusDays(2).toInstant().toEpochMilli());

        // Need to initalise dualGraph here, and to commit nothing to have storeGraph's vertices actually be reflected
        final DualGraph dualGraph = spy(new DualGraph(schema, storeGraph));
        // This might eb a bug currently
        final WritableGraph wg = dualGraph.getWritableGraph("Initial Setup", true);
        try {
        } finally {
            wg.commit();
        }

        final PluginInteraction interaction = mock(PluginInteraction.class);

        final LayerByTimePlugin instance = spy(new LayerByTimePlugin()); // Spy the instance to override copyGraph()
        doReturn(dualGraph).when(instance).copyGraph(storeGraph);

        final PluginParameters parameters = instance.createParameters();

        parameters.setStringValue(DATETIME_ATTRIBUTE_PARAMETER_ID, "DateTime");
        parameters.setBooleanValue(ARRANGE_2D_PARAMETER_ID, true);

        parameters.setDateTimeRangeValue(DATE_RANGE_PARAMETER_ID, new DateTimeRange(now, now.plusDays(4)));

        instance.updateParameters(dualGraph, parameters);
        System.out.println(parameters);
        instance.read(storeGraph, interaction, parameters);

        System.out.println("dualGraph vert positions after test...");
        try (final ReadableGraph rg = dualGraph.getReadableGraph()) {

            for (int i = 0; i < rg.getVertexCount(); i++) {
                final int vert = rg.getVertex(i);
                final float x = rg.getFloatValue(xAttr, vert);
                final float y = rg.getFloatValue(yAttr, vert);
                final float z = rg.getFloatValue(zAttr, vert);

                System.out.println("x " + x + " y " + y + " z " + z);
            }
        }

        verify(dualGraph, atLeast(1)).getWritableGraph("Layer by time", true);
        //verify(wgMock, atLeast(1)).setFloatValue(anyInt(), anyInt(), anyFloat());
    }

}
