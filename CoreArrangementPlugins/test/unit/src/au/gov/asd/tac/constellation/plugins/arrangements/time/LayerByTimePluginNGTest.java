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

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.TemporalConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import static au.gov.asd.tac.constellation.plugins.arrangements.time.LayerByTimePlugin.DATETIME_ATTRIBUTE_PARAMETER_ID;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import java.util.Map;
import static org.mockito.Mockito.mock;
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
        // Setup graph

        // create an analytic graph
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        final StoreGraph graph = new StoreGraph(schema);

        // add vertices
        final int vxId0 = graph.addVertex();
        final int vxId1 = graph.addVertex();

        // add transactions
        final int txId0 = graph.addTransaction(vxId0, vxId1, false);

        final int dateTimeAttr = TemporalConcept.TransactionAttribute.DATETIME.ensure(graph);

        final PluginInteraction interaction = mock(PluginInteraction.class);

        final LayerByTimePlugin instance = new LayerByTimePlugin();
        final PluginParameters parameters = instance.createParameters();

//        final PluginParameters parametersToUpdate = new PluginParameters();
//
//        final PluginParameter<SingleChoiceParameterType.SingleChoiceParameterValue> dtAttrParam = SingleChoiceParameterType.build(DATETIME_ATTRIBUTE_PARAMETER_ID);
//        dtAttrParam.setName(DATETIME_PARAMETER_ID_NAME);
//        dtAttrParam.setDescription(DATETIME_ATTRIBUTE_PARAMETER_ID_DESCRIPTION);
//        dtAttrParam.setRequired(true);
//        parametersToUpdate.addParameter(dtAttrParam);
        // parameters.updateParameterValues(parametersToUpdate);
        parameters.setStringValue(DATETIME_ATTRIBUTE_PARAMETER_ID, "DateTime");

        instance.read(graph, null, parameters);
        instance.updateParameters(new DualGraph(null, graph), parameters);
        System.out.println(parameters);
        instance.read(graph, interaction, parameters);
    }

//    public static final String DATETIME_ATTRIBUTE_PARAMETER_ID = PluginParameter.buildId(LayerByTimePlugin.class, "date_time_attribute");
//    private static final String DATETIME_PARAMETER_ID_NAME = "Date-time attribute";
//    private static final String DATETIME_ATTRIBUTE_PARAMETER_ID_DESCRIPTION = "The date-time attribute to use for the layered graph.";
}
