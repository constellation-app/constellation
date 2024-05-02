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
package au.gov.asd.tac.constellation.plugins.algorithms.sna.metrics;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.SnaConcept;
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
public class MultiplexityPluginNGTest {

    private int transactionMultiplexityAttribute, transactionTypeAttribute;
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
        transactionMultiplexityAttribute = SnaConcept.TransactionAttribute.MULTIPLEXITY.ensure(graph);
        transactionTypeAttribute = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);

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

        // add types
        graph.setObjectValue(transactionTypeAttribute, txId0, AnalyticConcept.TransactionType.COMMUNICATION);
        graph.setObjectValue(transactionTypeAttribute, txId1, AnalyticConcept.TransactionType.COMMUNICATION);
        graph.setObjectValue(transactionTypeAttribute, txId2, AnalyticConcept.TransactionType.COMMUNICATION);
        graph.setObjectValue(transactionTypeAttribute, txId3, AnalyticConcept.TransactionType.COMMUNICATION);
        graph.setObjectValue(transactionTypeAttribute, txId4, AnalyticConcept.TransactionType.COMMUNICATION);

        // add transactions
        txId5 = graph.addTransaction(vxId0, vxId1, true);
        txId6 = graph.addTransaction(vxId1, vxId2, true);
        txId7 = graph.addTransaction(vxId1, vxId3, true);
        txId8 = graph.addTransaction(vxId2, vxId3, true);
        txId9 = graph.addTransaction(vxId3, vxId4, true);

        // add types
        graph.setObjectValue(transactionTypeAttribute, txId5, AnalyticConcept.TransactionType.COMMUNICATION);
        graph.setObjectValue(transactionTypeAttribute, txId6, AnalyticConcept.TransactionType.LOCATION);
        graph.setObjectValue(transactionTypeAttribute, txId7, AnalyticConcept.TransactionType.LOCATION);
        graph.setObjectValue(transactionTypeAttribute, txId8, AnalyticConcept.TransactionType.LOCATION);
        graph.setObjectValue(transactionTypeAttribute, txId9, AnalyticConcept.TransactionType.LOCATION);

        // add transactions
        txId10 = graph.addTransaction(vxId0, vxId1, true);
        txId11 = graph.addTransaction(vxId1, vxId2, true);
        txId12 = graph.addTransaction(vxId1, vxId3, true);
        txId13 = graph.addTransaction(vxId2, vxId3, true);

        // add types
        graph.setObjectValue(transactionTypeAttribute, txId10, AnalyticConcept.TransactionType.COMMUNICATION);
        graph.setObjectValue(transactionTypeAttribute, txId11, AnalyticConcept.TransactionType.LOCATION);
        graph.setObjectValue(transactionTypeAttribute, txId12, AnalyticConcept.TransactionType.RELATIONSHIP);
        graph.setObjectValue(transactionTypeAttribute, txId13, AnalyticConcept.TransactionType.BEHAVIOUR);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        graph = null;
    }

    @Test
    public void testMultiplexity() throws Exception {
        final MultiplexityPlugin instance = new MultiplexityPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setBooleanValue(MultiplexityPlugin.GROUP_BY_TOP_LEVEL_TYPE, false);
        parameters.setBooleanValue(MultiplexityPlugin.NORMALISE_AVAILABLE_PARAMETER_ID, false);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getFloatValue(transactionMultiplexityAttribute, txId0), 1.0f);
        assertEquals(graph.getFloatValue(transactionMultiplexityAttribute, txId1), 2.0f);
        assertEquals(graph.getFloatValue(transactionMultiplexityAttribute, txId2), 3.0f);
        assertEquals(graph.getFloatValue(transactionMultiplexityAttribute, txId3), 3.0f);
        assertEquals(graph.getFloatValue(transactionMultiplexityAttribute, txId4), 2.0f);
        assertEquals(graph.getFloatValue(transactionMultiplexityAttribute, txId5), 1.0f);
        assertEquals(graph.getFloatValue(transactionMultiplexityAttribute, txId6), 2.0f);
        assertEquals(graph.getFloatValue(transactionMultiplexityAttribute, txId7), 3.0f);
        assertEquals(graph.getFloatValue(transactionMultiplexityAttribute, txId8), 3.0f);
        assertEquals(graph.getFloatValue(transactionMultiplexityAttribute, txId9), 2.0f);
        assertEquals(graph.getFloatValue(transactionMultiplexityAttribute, txId10), 1.0f);
        assertEquals(graph.getFloatValue(transactionMultiplexityAttribute, txId11), 2.0f);
        assertEquals(graph.getFloatValue(transactionMultiplexityAttribute, txId12), 3.0f);
        assertEquals(graph.getFloatValue(transactionMultiplexityAttribute, txId13), 3.0f);
    }

    @Test
    public void testNormalisedMultiplexity() throws Exception {
        final MultiplexityPlugin instance = new MultiplexityPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setBooleanValue(MultiplexityPlugin.GROUP_BY_TOP_LEVEL_TYPE, false);
        parameters.setBooleanValue(MultiplexityPlugin.NORMALISE_AVAILABLE_PARAMETER_ID, true);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getFloatValue(transactionMultiplexityAttribute, txId0), 1.0f / 3.0f);
        assertEquals(graph.getFloatValue(transactionMultiplexityAttribute, txId1), 2.0f / 3.0f);
        assertEquals(graph.getFloatValue(transactionMultiplexityAttribute, txId2), 3.0f / 3.0f);
        assertEquals(graph.getFloatValue(transactionMultiplexityAttribute, txId3), 3.0f / 3.0f);
        assertEquals(graph.getFloatValue(transactionMultiplexityAttribute, txId4), 2.0f / 3.0f);
        assertEquals(graph.getFloatValue(transactionMultiplexityAttribute, txId5), 1.0f / 3.0f);
        assertEquals(graph.getFloatValue(transactionMultiplexityAttribute, txId6), 2.0f / 3.0f);
        assertEquals(graph.getFloatValue(transactionMultiplexityAttribute, txId7), 3.0f / 3.0f);
        assertEquals(graph.getFloatValue(transactionMultiplexityAttribute, txId8), 3.0f / 3.0f);
        assertEquals(graph.getFloatValue(transactionMultiplexityAttribute, txId9), 2.0f / 3.0f);
        assertEquals(graph.getFloatValue(transactionMultiplexityAttribute, txId10), 1.0f / 3.0f);
        assertEquals(graph.getFloatValue(transactionMultiplexityAttribute, txId11), 2.0f / 3.0f);
        assertEquals(graph.getFloatValue(transactionMultiplexityAttribute, txId12), 3.0f / 3.0f);
        assertEquals(graph.getFloatValue(transactionMultiplexityAttribute, txId13), 3.0f / 3.0f);
    }
}
