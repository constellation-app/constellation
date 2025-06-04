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
package au.gov.asd.tac.constellation.plugins.algorithms.sna.metrics;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
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
 * Ratio Of Reciprocity Plugin Test.
 *
 * @author cygnus_x-1
 */
public class RatioOfReciprocityPluginNGTest {

    private int transactionReciprocityAttribute;
    
    private int vxId0;
    private int vxId1;
    private int vxId2;
    private int vxId3;
    private int vxId4;
    
    private int txId0;
    private int txId1;
    private int txId2;
    private int txId3;
    private int txId4;
    private int txId5;
    private int txId6;
    private int txId7;
    private int txId8;
    private int txId9;
    private int txId10;
    private int txId11;
    private int txId12;
    private int txId13;
    
    private StoreGraph graph;

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
        // create an analytic graph
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        graph = new StoreGraph(schema);

        // add attributes
        transactionReciprocityAttribute = SnaConcept.TransactionAttribute.RATIO_OF_RECIPROCITY.ensure(graph);

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

        //add reciprocal transactions
        txId5 = graph.addTransaction(vxId2, vxId1, true);
        txId6 = graph.addTransaction(vxId2, vxId1, true);
        txId7 = graph.addTransaction(vxId3, vxId1, true);
        txId8 = graph.addTransaction(vxId3, vxId1, true);
        txId9 = graph.addTransaction(vxId3, vxId1, true);
        txId10 = graph.addTransaction(vxId3, vxId2, true);
        txId11 = graph.addTransaction(vxId3, vxId2, true);
        txId12 = graph.addTransaction(vxId3, vxId2, true);
        txId13 = graph.addTransaction(vxId3, vxId2, true);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        graph = null;
    }

    @Test
    public void testRatioOfReciprocity() throws Exception {
        final RatioOfReciprocityPlugin instance = new RatioOfReciprocityPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setBooleanValue(RatioOfReciprocityPlugin.TREAT_UNDIRECTED_BIDIRECTIONAL_PARAMETER_ID, true);
        parameters.setBooleanValue(RatioOfReciprocityPlugin.NORMALISE_AVAILABLE_PARAMETER_ID, false);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getFloatValue(transactionReciprocityAttribute, txId0), 0f);
        assertEquals(graph.getFloatValue(transactionReciprocityAttribute, txId1), 0.5f);
        assertEquals(graph.getFloatValue(transactionReciprocityAttribute, txId2), 0.33333334f);
        assertEquals(graph.getFloatValue(transactionReciprocityAttribute, txId3), 0.25f);
        assertEquals(graph.getFloatValue(transactionReciprocityAttribute, txId4), 0f);
        assertEquals(graph.getFloatValue(transactionReciprocityAttribute, txId5), 0.5f);
        assertEquals(graph.getFloatValue(transactionReciprocityAttribute, txId6), 0.5f);
        assertEquals(graph.getFloatValue(transactionReciprocityAttribute, txId7), 0.33333334f);
        assertEquals(graph.getFloatValue(transactionReciprocityAttribute, txId8), 0.33333334f);
        assertEquals(graph.getFloatValue(transactionReciprocityAttribute, txId9), 0.33333334f);
        assertEquals(graph.getFloatValue(transactionReciprocityAttribute, txId10), 0.25f);
        assertEquals(graph.getFloatValue(transactionReciprocityAttribute, txId11), 0.25f);
        assertEquals(graph.getFloatValue(transactionReciprocityAttribute, txId12), 0.25f);
        assertEquals(graph.getFloatValue(transactionReciprocityAttribute, txId13), 0.25f);
    }

    @Test
    public void testNormalisedRatioOfReciprocity() throws Exception {
        final RatioOfReciprocityPlugin instance = new RatioOfReciprocityPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setBooleanValue(RatioOfReciprocityPlugin.TREAT_UNDIRECTED_BIDIRECTIONAL_PARAMETER_ID, true);
        parameters.setBooleanValue(RatioOfReciprocityPlugin.NORMALISE_AVAILABLE_PARAMETER_ID, true);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getFloatValue(transactionReciprocityAttribute, txId0), 0f / 0.5f);
        assertEquals(graph.getFloatValue(transactionReciprocityAttribute, txId1), 0.5f / 0.5f);
        assertEquals(graph.getFloatValue(transactionReciprocityAttribute, txId2), 0.33333334f / 0.5f);
        assertEquals(graph.getFloatValue(transactionReciprocityAttribute, txId3), 0.25f / 0.5f);
        assertEquals(graph.getFloatValue(transactionReciprocityAttribute, txId4), 0f / 0.5f);
        assertEquals(graph.getFloatValue(transactionReciprocityAttribute, txId5), 0.5f / 0.5f);
        assertEquals(graph.getFloatValue(transactionReciprocityAttribute, txId6), 0.5f / 0.5f);
        assertEquals(graph.getFloatValue(transactionReciprocityAttribute, txId7), 0.33333334f / 0.5f);
        assertEquals(graph.getFloatValue(transactionReciprocityAttribute, txId8), 0.33333334f / 0.5f);
        assertEquals(graph.getFloatValue(transactionReciprocityAttribute, txId9), 0.33333334f / 0.5f);
        assertEquals(graph.getFloatValue(transactionReciprocityAttribute, txId10), 0.25f / 0.5f);
        assertEquals(graph.getFloatValue(transactionReciprocityAttribute, txId11), 0.25f / 0.5f);
        assertEquals(graph.getFloatValue(transactionReciprocityAttribute, txId12), 0.25f / 0.5f);
        assertEquals(graph.getFloatValue(transactionReciprocityAttribute, txId13), 0.25f / 0.5f);
    }
}
