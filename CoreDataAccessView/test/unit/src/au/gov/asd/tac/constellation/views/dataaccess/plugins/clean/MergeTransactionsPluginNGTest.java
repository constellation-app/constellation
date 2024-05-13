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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.clean;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.TemporalConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.text.TextPluginInteraction;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for MergeTransactionsPlugin
 *
 * @author Delphinus8821
 */
public class MergeTransactionsPluginNGTest {

    private int vertexIdentifierAttribute, vertexTypeAttribute, transactionDateTimeAttribute, transactionTypeAttribute, transactionIdentifierAttribute, transactionSelectedAttribute;
    private int vxId1, vxId2;
    private int txId1, txId2, txId3, txId4, txId5;
    private StoreGraph graph;

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // create an analytic graph
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        graph = new StoreGraph(schema);

        // add attributes
        vertexIdentifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        vertexTypeAttribute = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
        transactionIdentifierAttribute = VisualConcept.TransactionAttribute.IDENTIFIER.ensure(graph);
        transactionTypeAttribute = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);
        transactionDateTimeAttribute = TemporalConcept.TransactionAttribute.DATETIME.ensure(graph);
        transactionSelectedAttribute = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);

        // add vertices
        vxId1 = graph.addVertex();
        vxId2 = graph.addVertex();

        // set the identifier of each vertex to something unique
        graph.setStringValue(vertexIdentifierAttribute, vxId1, "V1");
        graph.setStringValue(vertexIdentifierAttribute, vxId2, "V2");

        // add transactions
        txId1 = graph.addTransaction(vxId1, vxId2, false);
        txId2 = graph.addTransaction(vxId1, vxId2, false);
        txId3 = graph.addTransaction(vxId1, vxId2, false);
        txId4 = graph.addTransaction(vxId1, vxId2, false);
        txId5 = graph.addTransaction(vxId1, vxId2, false);

        // set the same activity
        graph.setStringValue(transactionIdentifierAttribute, txId1, "");
        graph.setStringValue(transactionIdentifierAttribute, txId2, "");
        graph.setStringValue(transactionIdentifierAttribute, txId3, "");
        graph.setStringValue(transactionIdentifierAttribute, txId4, null);
        graph.setStringValue(transactionIdentifierAttribute, txId5, null);

        // set the same type
        graph.setStringValue(transactionTypeAttribute, txId1, "");
        graph.setStringValue(transactionTypeAttribute, txId2, "");
        graph.setStringValue(transactionTypeAttribute, txId3, "");
        graph.setStringValue(transactionTypeAttribute, txId4, AnalyticConcept.VertexType.EMAIL_ADDRESS.getName());
        graph.setStringValue(transactionTypeAttribute, txId5, AnalyticConcept.VertexType.COUNTRY.getName());

        // set the time of each transaction 1 second apart
        graph.setStringValue(transactionDateTimeAttribute, txId1, "2015-01-28 00:00:01.000 +00:00 [UTC]");
        graph.setStringValue(transactionDateTimeAttribute, txId2, "2015-01-28 00:00:02.000 +00:00 [UTC]");
        graph.setStringValue(transactionDateTimeAttribute, txId3, "2015-01-28 00:00:03.000 +00:00 [UTC]");
        graph.setStringValue(transactionDateTimeAttribute, txId4, "2015-01-28 00:00:02.000 +00:00 [UTC]");
        graph.setStringValue(transactionDateTimeAttribute, txId5, "2015-01-28 00:00:03.000 +00:00 [UTC]");

        // select all
        graph.setBooleanValue(transactionSelectedAttribute, txId1, true);
        graph.setBooleanValue(transactionSelectedAttribute, txId2, true);
        graph.setBooleanValue(transactionSelectedAttribute, txId3, true);
        graph.setBooleanValue(transactionSelectedAttribute, txId4, true);
        graph.setBooleanValue(transactionSelectedAttribute, txId5, true);
    }

    /**
     * Test of getPosition method, of class MergeTransactionsPlugin.
     */
    @Test
    public void testGetPosition() {
        MergeTransactionsPlugin instance = new MergeTransactionsPlugin();
        int expResult = 300;
        int result = instance.getPosition();
        assertEquals(result, expResult);
    }

    /**
     * Test of getDescription method, of class MergeTransactionsPlugin.
     */
    @Test
    public void testGetDescription() {
        MergeTransactionsPlugin instance = new MergeTransactionsPlugin();
        String expResult = "Merge transactions in your graph together";
        String result = instance.getDescription();
        assertEquals(result, expResult);
    }

    /**
     * Test of createParameters method, of class MergeTransactionsPlugin.
     */
    @Test
    public void testCreateParameters() {
        MergeTransactionsPlugin instance = new MergeTransactionsPlugin();
        PluginParameters result = instance.createParameters();

        // Test the correct number of parameters were added
        final int expResult = 5;
        assertEquals(result.getParameters().size(), expResult);
        assertTrue(result.hasParameter(MergeTransactionsPlugin.MERGE_TYPE_PARAMETER_ID));
        assertTrue(result.hasParameter(MergeTransactionsPlugin.LEAD_PARAMETER_ID));
    }

    /**
     * Test of edit method, of class MergeTransactionsPlugin.
     *
     * @throws Exception
     */
    @Test
    public void testEdit() throws Exception {
        MergeTransactionsPlugin instance = new MergeTransactionsPlugin();
        PluginInteraction interaction = new TextPluginInteraction();
        PluginParameters parameters = instance.createParameters();
        parameters.getParameters().get(MergeTransactionsPlugin.THRESHOLD_PARAMETER_ID).setIntegerValue(5);
        parameters.getParameters().get(MergeTransactionsPlugin.MERGE_TYPE_PARAMETER_ID).setStringValue("DateTime");
        parameters.getParameters().get(MergeTransactionsPlugin.LEAD_PARAMETER_ID).setEnabled(true);
        parameters.getParameters().get(MergeTransactionsPlugin.SELECTED_PARAMETER_ID).setBooleanValue(true);

        instance.edit(graph, interaction, parameters);

        // Check that the transactions were merged 
        assertEquals(graph.getTransactionCount(), 3);
        assertEquals(graph.getTransaction(txId1), 1);
        assertEquals(graph.getTransaction(txId3), 2);
        assertEquals(graph.getTransaction(txId4), 3);
    }
    
    /**
     * Test of edit method with a null merge transaction type name
     *
     * @throws Exception
     */
    @Test(expectedExceptions = PluginException.class)
    public void testEditNullTypeName() throws Exception {
        MergeTransactionsPlugin instance = new MergeTransactionsPlugin();
        PluginInteraction interaction = new TextPluginInteraction();
        PluginParameters parameters = instance.createParameters();
        parameters.getParameters().get(MergeTransactionsPlugin.MERGE_TYPE_PARAMETER_ID).setStringValue(null);
        instance.edit(graph, interaction, parameters);
    }

    /**
     * Test of edit method with an unknown merge transaction type
     *
     * @throws Exception
     */
    @Test(expectedExceptions = PluginException.class)
    public void testEditUnknownType() throws Exception {
        MergeTransactionsPlugin instance = new MergeTransactionsPlugin();
        PluginInteraction interaction = new TextPluginInteraction();
        PluginParameters parameters = instance.createParameters();
        parameters.getParameters().get(MergeTransactionsPlugin.MERGE_TYPE_PARAMETER_ID).setStringValue("None");
        instance.edit(graph, interaction, parameters);
    }
}
