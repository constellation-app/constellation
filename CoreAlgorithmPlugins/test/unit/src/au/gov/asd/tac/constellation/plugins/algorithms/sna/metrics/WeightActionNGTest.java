/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.algorithms.sna.metrics;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author cygnus_x-1
 */
public class WeightActionNGTest {

    private int transactionWeightAttribute;
    private int vxId0, vxId1, vxId2, vxId3, vxId4;
    private int txId0, txId1, txId2, txId3, txId4, txId5, txId6, txId7, txId8, txId9, txId10, txId11, txId12, txId13;
    private StoreGraph graph;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // create an analytic graph
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        graph = new StoreGraph(schema);

        // add attributes
        transactionWeightAttribute = AnalyticConcept.TransactionAttribute.WEIGHT.ensure(graph);

        // add vertices
        vxId0 = graph.addVertex();
        vxId1 = graph.addVertex();
        vxId2 = graph.addVertex();
        vxId3 = graph.addVertex();
        vxId4 = graph.addVertex();

        // add transactions
        txId0 = graph.addTransaction(vxId0, vxId1, true);
        txId1 = graph.addTransaction(vxId1, vxId2, true);
        txId2 = graph.addTransaction(vxId1, vxId3, true);
        txId3 = graph.addTransaction(vxId2, vxId3, true);
        txId4 = graph.addTransaction(vxId3, vxId4, true);

        //add more transactions
        txId5 = graph.addTransaction(vxId1, vxId2, true);
        txId6 = graph.addTransaction(vxId1, vxId3, true);
        txId7 = graph.addTransaction(vxId1, vxId3, true);
        txId8 = graph.addTransaction(vxId2, vxId3, true);
        txId9 = graph.addTransaction(vxId2, vxId3, true);
        txId10 = graph.addTransaction(vxId2, vxId3, true);
        txId11 = graph.addTransaction(vxId3, vxId4, true);
        txId12 = graph.addTransaction(vxId3, vxId4, true);
        txId13 = graph.addTransaction(vxId3, vxId4, true);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        graph = null;
    }

    @Test
    public void testRatioOfReciprocity() throws Exception {
        final WeightPlugin instance = new WeightPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setBooleanValue(WeightPlugin.NORMALISE_AVAILABLE_PARAMETER_ID, false);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getFloatValue(transactionWeightAttribute, txId0), 1.0f);
        assertEquals(graph.getFloatValue(transactionWeightAttribute, txId1), 2.0f);
        assertEquals(graph.getFloatValue(transactionWeightAttribute, txId2), 3.0f);
        assertEquals(graph.getFloatValue(transactionWeightAttribute, txId3), 4.0f);
        assertEquals(graph.getFloatValue(transactionWeightAttribute, txId4), 4.0f);
        assertEquals(graph.getFloatValue(transactionWeightAttribute, txId5), 2.0f);
        assertEquals(graph.getFloatValue(transactionWeightAttribute, txId6), 3.0f);
        assertEquals(graph.getFloatValue(transactionWeightAttribute, txId7), 3.0f);
        assertEquals(graph.getFloatValue(transactionWeightAttribute, txId8), 4.0f);
        assertEquals(graph.getFloatValue(transactionWeightAttribute, txId9), 4.0f);
        assertEquals(graph.getFloatValue(transactionWeightAttribute, txId10), 4.0f);
        assertEquals(graph.getFloatValue(transactionWeightAttribute, txId11), 4.0f);
        assertEquals(graph.getFloatValue(transactionWeightAttribute, txId12), 4.0f);
        assertEquals(graph.getFloatValue(transactionWeightAttribute, txId13), 4.0f);
    }

    @Test
    public void testNormalisedRatioOfReciprocity() throws Exception {
        final WeightPlugin instance = new WeightPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setBooleanValue(WeightPlugin.NORMALISE_AVAILABLE_PARAMETER_ID, true);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getFloatValue(transactionWeightAttribute, txId0), 1.0f / 4.0f);
        assertEquals(graph.getFloatValue(transactionWeightAttribute, txId1), 2.0f / 4.0f);
        assertEquals(graph.getFloatValue(transactionWeightAttribute, txId2), 3.0f / 4.0f);
        assertEquals(graph.getFloatValue(transactionWeightAttribute, txId3), 4.0f / 4.0f);
        assertEquals(graph.getFloatValue(transactionWeightAttribute, txId4), 4.0f / 4.0f);
        assertEquals(graph.getFloatValue(transactionWeightAttribute, txId5), 2.0f / 4.0f);
        assertEquals(graph.getFloatValue(transactionWeightAttribute, txId6), 3.0f / 4.0f);
        assertEquals(graph.getFloatValue(transactionWeightAttribute, txId7), 3.0f / 4.0f);
        assertEquals(graph.getFloatValue(transactionWeightAttribute, txId8), 4.0f / 4.0f);
        assertEquals(graph.getFloatValue(transactionWeightAttribute, txId9), 4.0f / 4.0f);
        assertEquals(graph.getFloatValue(transactionWeightAttribute, txId10), 4.0f / 4.0f);
        assertEquals(graph.getFloatValue(transactionWeightAttribute, txId11), 4.0f / 4.0f);
        assertEquals(graph.getFloatValue(transactionWeightAttribute, txId12), 4.0f / 4.0f);
        assertEquals(graph.getFloatValue(transactionWeightAttribute, txId13), 4.0f / 4.0f);
    }
}
