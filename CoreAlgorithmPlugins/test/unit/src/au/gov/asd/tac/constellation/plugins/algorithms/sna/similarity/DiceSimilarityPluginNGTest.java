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
 *
 * @author cygnus_x-1
 */
public class DiceSimilarityPluginNGTest {

    private int transactionDiceAttribute;
    private int transactionIdentifier;
    
    private int vxId0;
    private int vxId1;
    private int vxId2;
    private int vxId3;
    private int vxId4;
    
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
        transactionDiceAttribute = SnaConcept.TransactionAttribute.DICE_SIMILARITY.ensure(graph);
        transactionIdentifier = VisualConcept.TransactionAttribute.IDENTIFIER.ensure(graph);
        VisualConcept.VertexAttribute.SELECTED.ensure(graph);

        // add vertices
        vxId0 = graph.addVertex();
        vxId1 = graph.addVertex();
        vxId2 = graph.addVertex();
        vxId3 = graph.addVertex();
        vxId4 = graph.addVertex();

        // add transactions
        graph.addTransaction(vxId0, vxId1, true);
        graph.addTransaction(vxId1, vxId2, true);
        graph.addTransaction(vxId1, vxId3, true);
        graph.addTransaction(vxId2, vxId3, true);
        graph.addTransaction(vxId3, vxId4, true);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        graph = null;
    }

    @Test
    public void testDirectedDice() throws Exception {
        final DiceSimilarityPlugin instance = new DiceSimilarityPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setBooleanValue(DiceSimilarityPlugin.INCLUDE_CONNECTIONS_IN_PARAMETER_ID, false);
        parameters.setBooleanValue(DiceSimilarityPlugin.INCLUDE_CONNECTIONS_OUT_PARAMETER_ID, true);
        parameters.setBooleanValue(DiceSimilarityPlugin.TREAT_UNDIRECTED_BIDIRECTIONAL_PARAMETER_ID, true);
        parameters.setIntegerValue(DiceSimilarityPlugin.MINIMUM_COMMON_FEATURES_PARAMETER_ID, 1);
        parameters.setBooleanValue(DiceSimilarityPlugin.SELECTED_ONLY_PARAMETER_ID, false);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        int transactionCount = graph.getTransactionCount();

        for (int transaction = 0; transaction < transactionCount; transaction++) {
            int transactionId = graph.getTransaction(transaction);
            String identifier = graph.getStringValue(transactionIdentifier, transactionId);
            if ("0 == similarity == 2".equals(identifier)) {
                assertEquals(graph.getFloatValue(transactionDiceAttribute, transactionId), 0.66666667f);
            }
            if ("2 == similarity == 3".equals(identifier)) {
                assertEquals(graph.getFloatValue(transactionDiceAttribute, transactionId), 0.66666667f);
            }
            if ("2 == similarity == 4".equals(identifier)) {
                assertEquals(graph.getFloatValue(transactionDiceAttribute, transactionId), 0.66666667f);
            }
            if ("0 == similarity == 3".equals(identifier)) {
                assertEquals(graph.getFloatValue(transactionDiceAttribute, transactionId), 0.5f);
            }
            if ("1 == similarity == 4".equals(identifier)) {
                assertEquals(graph.getFloatValue(transactionDiceAttribute, transactionId), 0.5f);
            }
            if ("1 == similarity == 2".equals(identifier)) {
                assertEquals(graph.getFloatValue(transactionDiceAttribute, transactionId), 0.4f);
            }
            if ("1 == similarity == 3".equals(identifier)) {
                assertEquals(graph.getFloatValue(transactionDiceAttribute, transactionId), 0.33333334f);
            }
        }
    }

    @Test
    public void testUndirectedDice() throws Exception {
        final DiceSimilarityPlugin instance = new DiceSimilarityPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setBooleanValue(DiceSimilarityPlugin.INCLUDE_CONNECTIONS_IN_PARAMETER_ID, true);
        parameters.setBooleanValue(DiceSimilarityPlugin.INCLUDE_CONNECTIONS_OUT_PARAMETER_ID, true);
        parameters.setBooleanValue(DiceSimilarityPlugin.TREAT_UNDIRECTED_BIDIRECTIONAL_PARAMETER_ID, true);
        parameters.setIntegerValue(DiceSimilarityPlugin.MINIMUM_COMMON_FEATURES_PARAMETER_ID, 1);
        parameters.setBooleanValue(DiceSimilarityPlugin.SELECTED_ONLY_PARAMETER_ID, false);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        int transactionCount = graph.getTransactionCount();

        for (int transaction = 0; transaction < transactionCount; transaction++) {
            int transactionId = graph.getTransaction(transaction);
            String identifier = graph.getStringValue(transactionIdentifier, transactionId);
            if ("0 == similarity == 2".equals(identifier)) {
                assertEquals(graph.getFloatValue(transactionDiceAttribute, transactionId), 0.66666667f);
            }
            if ("2 == similarity == 4".equals(identifier)) {
                assertEquals(graph.getFloatValue(transactionDiceAttribute, transactionId), 0.66666667f);
            }
            if ("0 == similarity == 3".equals(identifier)) {
                assertEquals(graph.getFloatValue(transactionDiceAttribute, transactionId), 0.5f);
            }
            if ("1 == similarity == 4".equals(identifier)) {
                assertEquals(graph.getFloatValue(transactionDiceAttribute, transactionId), 0.5f);
            }
            if ("1 == similarity == 2".equals(identifier)) {
                assertEquals(graph.getFloatValue(transactionDiceAttribute, transactionId), 0.4f);
            }
            if ("2 == similarity == 3".equals(identifier)) {
                assertEquals(graph.getFloatValue(transactionDiceAttribute, transactionId), 0.4f);
            }
            if ("1 == similarity == 3".equals(identifier)) {
                assertEquals(graph.getFloatValue(transactionDiceAttribute, transactionId), 0.33333334f);
            }
        }
    }
}
