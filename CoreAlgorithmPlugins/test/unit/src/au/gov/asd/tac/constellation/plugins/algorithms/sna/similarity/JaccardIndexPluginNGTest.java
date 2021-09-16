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
package au.gov.asd.tac.constellation.plugins.algorithms.sna.similarity;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
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
 * Jaccard Index Plugin Test.
 *
 * @author cygnus_x-1
 */
public class JaccardIndexPluginNGTest {

    private int transactionJaccardAttribute, vertexSelectedAttribute, transactionIdentifier;
    private int vxId0, vxId1, vxId2, vxId3, vxId4;
    private int txId0, txId1, txId2, txId3, txId4;
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
        transactionJaccardAttribute = SnaConcept.TransactionAttribute.JACCARD_INDEX.ensure(graph);
        vertexSelectedAttribute = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        transactionIdentifier = VisualConcept.TransactionAttribute.IDENTIFIER.ensure(graph);

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
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        graph = null;
    }

    @Test
    public void testDirectedJaccard() throws Exception {
        final JaccardIndexPlugin instance = new JaccardIndexPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setBooleanValue(JaccardIndexPlugin.INCLUDE_CONNECTIONS_IN_PARAMETER_ID, false);
        parameters.setBooleanValue(JaccardIndexPlugin.INCLUDE_CONNECTIONS_OUT_PARAMETER_ID, true);
        parameters.setBooleanValue(JaccardIndexPlugin.TREAT_UNDIRECTED_BIDIRECTIONAL_PARAMETER_ID, true);
        parameters.setIntegerValue(JaccardIndexPlugin.MINIMUM_COMMON_FEATURES_PARAMETER_ID, 1);
        parameters.setBooleanValue(JaccardIndexPlugin.SELECTED_ONLY_PARAMETER_ID, false);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        int transactionCount = graph.getTransactionCount();

        for (int transactionId = 0; transactionId < transactionCount; transactionId++) {
            int transaction = graph.getTransaction(transactionId);
            String identifier = graph.getStringValue(transactionIdentifier, transactionId);
            if ("2 == similarity == 3".equals(identifier)) {
                assertEquals(graph.getFloatValue(transactionJaccardAttribute, transactionId), 1f);
            }
            if ("0 == similarity == 2".equals(identifier)) {
                assertEquals(graph.getFloatValue(transactionJaccardAttribute, transactionId), 0.5f);
            }
            if ("2 == similarity == 4".equals(identifier)) {
                assertEquals(graph.getFloatValue(transactionJaccardAttribute, transactionId), 0.5f);
            }
            if ("1 == similarity == 2".equals(identifier)) {
                assertEquals(graph.getFloatValue(transactionJaccardAttribute, transactionId), 0.5f);
            }
            if ("0 == similarity == 3".equals(identifier)) {
                assertEquals(graph.getFloatValue(transactionJaccardAttribute, transactionId), 0.33333334f);
            }
            if ("1 == similarity == 4".equals(identifier)) {
                assertEquals(graph.getFloatValue(transactionJaccardAttribute, transactionId), 0.33333334f);
            }
            if ("1 == similarity == 3".equals(identifier)) {
                assertEquals(graph.getFloatValue(transactionJaccardAttribute, transactionId), 0.33333334f);
            }
        }
    }

    @Test
    public void testUndirectedJaccard() throws Exception {
        final JaccardIndexPlugin instance = new JaccardIndexPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setBooleanValue(JaccardIndexPlugin.INCLUDE_CONNECTIONS_IN_PARAMETER_ID, true);
        parameters.setBooleanValue(JaccardIndexPlugin.INCLUDE_CONNECTIONS_OUT_PARAMETER_ID, true);
        parameters.setBooleanValue(JaccardIndexPlugin.TREAT_UNDIRECTED_BIDIRECTIONAL_PARAMETER_ID, true);
        parameters.setIntegerValue(JaccardIndexPlugin.MINIMUM_COMMON_FEATURES_PARAMETER_ID, 1);
        parameters.setBooleanValue(JaccardIndexPlugin.SELECTED_ONLY_PARAMETER_ID, false);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        int transactionCount = graph.getTransactionCount();

        for (int transactionId = 0; transactionId < transactionCount; transactionId++) {
            int transaction = graph.getTransaction(transactionId);
            String identifier = graph.getStringValue(transactionIdentifier, transactionId);
            if ("0 == similarity == 2".equals(identifier)) {
                assertEquals(graph.getFloatValue(transactionJaccardAttribute, transactionId), 0.5f);
            }
            if ("2 == similarity == 4".equals(identifier)) {
                assertEquals(graph.getFloatValue(transactionJaccardAttribute, transactionId), 0.5f);
            }
            if ("1 == similarity == 2".equals(identifier)) {
                assertEquals(graph.getFloatValue(transactionJaccardAttribute, transactionId), 0.5f);
            }
            if ("2 == similarity == 3".equals(identifier)) {
                assertEquals(graph.getFloatValue(transactionJaccardAttribute, transactionId), 0.5f);
            }
            if ("0 == similarity == 3".equals(identifier)) {
                assertEquals(graph.getFloatValue(transactionJaccardAttribute, transactionId), 0.33333334f);
            }
            if ("1 == similarity == 4".equals(identifier)) {
                assertEquals(graph.getFloatValue(transactionJaccardAttribute, transactionId), 0.33333334f);
            }
            if ("1 == similarity == 3".equals(identifier)) {
                assertEquals(graph.getFloatValue(transactionJaccardAttribute, transactionId), 0.33333334f);
            }
        }
    }
}
