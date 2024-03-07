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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.clean;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.SpatialConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Split Nodes Plugin Test.
 *
 * @author arcturus
 * @author antares
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class SplitNodesPluginNGTest extends ConstellationTest {

    private int vertexIdentifierAttribute, vertexTypeAttribute, vertexLatitudeAttribute, vertexLongitudeAttribute, vertexSelectedAttribute,
            vertexAttributeX, vertexAttributeY, vertexAttributeZ;
    private int vxId1, vxId2, vxId3, vxId4, txId1, txId2, txId3;
    private int transactionTypeAttributeId;
    private StoreGraph graph;

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // create an analytic graph
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        graph = new StoreGraph(schema);

        transactionTypeAttributeId = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);

        final int attrX = VisualConcept.VertexAttribute.X.ensure(graph);
        final int attrY = VisualConcept.VertexAttribute.Y.ensure(graph);
        final int attrZ = VisualConcept.VertexAttribute.Z.ensure(graph);

        // add attributes
        vertexIdentifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        vertexAttributeX = VisualConcept.VertexAttribute.X.ensure(graph);
        vertexAttributeY = VisualConcept.VertexAttribute.Y.ensure(graph);
        vertexAttributeZ = VisualConcept.VertexAttribute.Z.ensure(graph);
        vertexTypeAttribute = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
        vertexLatitudeAttribute = SpatialConcept.VertexAttribute.LATITUDE.ensure(graph);
        vertexLongitudeAttribute = SpatialConcept.VertexAttribute.LONGITUDE.ensure(graph);
        vertexSelectedAttribute = VisualConcept.VertexAttribute.SELECTED.ensure(graph);

        // add vertices
        vxId1 = graph.addVertex();
        vxId2 = graph.addVertex();
        vxId3 = graph.addVertex();
        vxId4 = graph.addVertex();

        // set the identifier of four vertices to somthing unique but similar, and the remaining vertex to a duplicate
        graph.setStringValue(vertexIdentifierAttribute, vxId1, "VERTEX_1@VERTEX_2@VERTEX_3");
        graph.setStringValue(vertexIdentifierAttribute, vxId2, "VERTEX_4,VERTEX_5");
        graph.setStringValue(vertexIdentifierAttribute, vxId3, "VERTEX_6,VERTEX_7");
        graph.setStringValue(vertexIdentifierAttribute, vxId4, "YYY");

        // set the x,y,z of the vertices that will be splitted
        graph.setDoubleValue(vertexAttributeX, vxId1, attrX);
        graph.setDoubleValue(vertexAttributeY, vxId1, attrY);
        graph.setDoubleValue(vertexAttributeZ, vxId1, attrZ);
        graph.setDoubleValue(vertexAttributeX, vxId2, attrX + 10);
        graph.setDoubleValue(vertexAttributeY, vxId2, attrY + 10);
        graph.setDoubleValue(vertexAttributeZ, vxId2, attrZ);

        // set the type of four vertices to a schema type, and the remaining two vertices to a non-schema type
        graph.setStringValue(vertexTypeAttribute, vxId1, "Online Identifier");
        graph.setStringValue(vertexTypeAttribute, vxId2, "Online Identifier");
        graph.setStringValue(vertexTypeAttribute, vxId3, "Special Identifier");
        graph.setStringValue(vertexTypeAttribute, vxId4, "Special Identifier");

        // set the latitude and longitude of each pair of vertices to be geospatially close
        graph.setFloatValue(vertexLatitudeAttribute, vxId1, 25.0f);
        graph.setFloatValue(vertexLongitudeAttribute, vxId1, 25.0f);
        graph.setFloatValue(vertexLatitudeAttribute, vxId2, 26.0f);
        graph.setFloatValue(vertexLongitudeAttribute, vxId2, 26.0f);
        graph.setFloatValue(vertexLatitudeAttribute, vxId3, -25.0f);
        graph.setFloatValue(vertexLongitudeAttribute, vxId3, -25.0f);
        graph.setFloatValue(vertexLatitudeAttribute, vxId4, -30.0f);
        graph.setFloatValue(vertexLongitudeAttribute, vxId4, -30.0f);

        // set all vertices to be selected
        graph.setBooleanValue(vertexSelectedAttribute, vxId1, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId2, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId3, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId4, true);

        //Add Transactions
        txId1 = graph.addTransaction(vxId1, vxId3, true);
        txId2 = graph.addTransaction(vxId2, vxId4, true);
        txId3 = graph.addTransaction(vxId1, vxId2, true);

        //Set Transaction Type to 'Network'
        graph.setStringValue(transactionTypeAttributeId, txId1, "Network");
        graph.setStringValue(transactionTypeAttributeId, txId2, "Network");
        graph.setStringValue(transactionTypeAttributeId, txId3, "Network");
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        graph = null;
    }

    /**
     * Test of createParameters method, of class SplitNodesPlugin.
     */
    @Test
    public void testCreateParameters() {
        final SplitNodesPlugin instance = new SplitNodesPlugin();
        final PluginParameters params = instance.createParameters();

        assertEquals(params.getParameters().size(), 5);
        assertTrue(params.getParameters().containsKey(SplitNodesPlugin.SPLIT_PARAMETER_ID));
        assertTrue(params.getParameters().containsKey(SplitNodesPlugin.DUPLICATE_TRANSACTIONS_PARAMETER_ID));
        assertTrue(params.getParameters().containsKey(SplitNodesPlugin.TRANSACTION_TYPE_PARAMETER_ID));
        assertTrue(params.getParameters().containsKey(SplitNodesPlugin.ALL_OCCURRENCES_PARAMETER_ID));
        assertTrue(params.getParameters().containsKey(SplitNodesPlugin.COMPLETE_WITH_SCHEMA_OPTION_ID));
    }

    /**
     * Test of createParameters method, of class SplitNodesPlugin.
     */
    @Test
    public void testUpdateParameters() {
        final SplitNodesPlugin instance = new SplitNodesPlugin();
        final PluginParameters params = instance.createParameters();

        final PluginParameter<SingleChoiceParameterValue> transactionTypeParam = (PluginParameter<SingleChoiceParameterValue>) params.getParameters().get(SplitNodesPlugin.TRANSACTION_TYPE_PARAMETER_ID);
        assertTrue(SingleChoiceParameterType.getOptions(transactionTypeParam).isEmpty());

        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        instance.updateParameters(new DualGraph(schema, graph), params);

        // 9 is the number of transaction types in the analytic schema
        assertEquals(SingleChoiceParameterType.getOptions(transactionTypeParam).size(), 9);
    }

    /**
     * Test of query method, of class SplitNodesPlugin.
     *
     * @throws Exception
     */
    @Test
    public void testQueryWithNoTypes_WithoutDuplicateTransactions() throws Exception {
        final SplitNodesPlugin instance = new SplitNodesPlugin();
        final PluginParameters parameters = instance.createParameters();

        parameters.setStringValue(SplitNodesPlugin.SPLIT_PARAMETER_ID, "@");
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getVertexCount(), 5);
        assertEquals(graph.getTransactionCount(), 4);
        assertEquals(graph.getVertexTransactionCount(vxId1), 3);
        // Check transactions of new node (Assumed the new node is at vxId4 + 1)
        assertEquals(graph.getVertexTransactionCount(vxId4 + 1), 1);
        //assert new transaction is of type 'Correlation'
        assertEquals(graph.getStringValue(transactionTypeAttributeId, graph.getVertexTransaction(vxId4 + 1, 0)), "Correlation");
    }

    @Test
    public void testQueryWithNoTypes_WithDuplicateTransactions() throws Exception {
        final SplitNodesPlugin instance = new SplitNodesPlugin();
        final PluginParameters parameters = instance.createParameters();

        parameters.setStringValue(SplitNodesPlugin.SPLIT_PARAMETER_ID, "@");
        parameters.setBooleanValue(SplitNodesPlugin.DUPLICATE_TRANSACTIONS_PARAMETER_ID, true);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getVertexCount(), 5);
        assertEquals(graph.getTransactionCount(), 5);
        assertEquals(graph.getVertexTransactionCount(vxId1), 2);
        // Check transactions of new node(Assumed the new node is at vxId4 + 1)
        assertEquals(graph.getVertexTransactionCount(vxId4 + 1), 2);
        //assert new transactions are of type 'Network'
        assertEquals(graph.getStringValue(transactionTypeAttributeId, graph.getVertexTransaction(vxId4 + 1, 0)), "Network");
        assertEquals(graph.getStringValue(transactionTypeAttributeId, graph.getVertexTransaction(vxId4 + 1, 1)), "Network");
    }

    @Test
    public void testQueryWithTypes_WithoutDuplicateTransactions() throws Exception {
        final SplitNodesPlugin instance = new SplitNodesPlugin();
        final PluginParameters parameters = instance.createParameters();

        parameters.setStringValue(SplitNodesPlugin.SPLIT_PARAMETER_ID, "@");
        parameters.setStringValue(SplitNodesPlugin.TRANSACTION_TYPE_PARAMETER_ID, "Similarity");
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getVertexCount(), 5);
        assertEquals(graph.getTransactionCount(), 4);
        assertEquals(graph.getVertexTransactionCount(vxId1), 3);
        // Check transactions of new node (Assumed the new node is at vxId4 + 1)
        assertEquals(graph.getVertexTransactionCount(vxId4 + 1), 1);
        //assert new transaction is of type 'Correlation'
        assertEquals(graph.getStringValue(transactionTypeAttributeId, graph.getVertexTransaction(vxId4 + 1, 0)), "Similarity");

    }

    @Test
    public void testQueryWithTypes_WithDuplicateTransactions() throws Exception {
        final SplitNodesPlugin instance = new SplitNodesPlugin();
        final PluginParameters parameters = instance.createParameters();

        parameters.setStringValue(SplitNodesPlugin.SPLIT_PARAMETER_ID, "@");
        parameters.setBooleanValue(SplitNodesPlugin.DUPLICATE_TRANSACTIONS_PARAMETER_ID, true);
        parameters.setStringValue(SplitNodesPlugin.TRANSACTION_TYPE_PARAMETER_ID, "Similarity");
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getVertexCount(), 5);
        assertEquals(graph.getTransactionCount(), 5);
        assertEquals(graph.getVertexTransactionCount(vxId1), 2);
        // Check transactions of new node(Assumed the new node is at vxId4 + 1)
        assertEquals(graph.getVertexTransactionCount(vxId4 + 1), 2);
        //assert new transactions are of type 'Network'
        assertEquals(graph.getStringValue(transactionTypeAttributeId, graph.getVertexTransaction(vxId4 + 1, 0)), "Network");
        assertEquals(graph.getStringValue(transactionTypeAttributeId, graph.getVertexTransaction(vxId4 + 1, 1)), "Network");
    }

    @Test
    public void testQueryAllOccurancesSelected_WithoutDuplicateTransactions() throws Exception {
        final SplitNodesPlugin instance = new SplitNodesPlugin();
        final PluginParameters parameters = instance.createParameters();

        parameters.setStringValue(SplitNodesPlugin.SPLIT_PARAMETER_ID, "@");
        parameters.setBooleanValue(SplitNodesPlugin.ALL_OCCURRENCES_PARAMETER_ID, true);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getVertexCount(), 6);
        assertEquals(graph.getTransactionCount(), 5);
        assertEquals(graph.getVertexTransactionCount(vxId1), 4);
        // Check transactions of new nodes (Assumed the new nodes are at vxId4 + 1 and vxId4 + 2
        assertEquals(graph.getVertexTransactionCount(vxId4 + 1), 1);
        assertEquals(graph.getVertexTransactionCount(vxId4 + 2), 1);
        //assert new transaction is of type 'Correlation'
        assertEquals(graph.getStringValue(transactionTypeAttributeId, graph.getVertexTransaction(vxId4 + 1, 0)), "Correlation");
        assertEquals(graph.getStringValue(transactionTypeAttributeId, graph.getVertexTransaction(vxId4 + 2, 0)), "Correlation");
    }

    @Test
    public void testQueryAllOccurancesSelected_WithDuplicateTransactions() throws Exception {
        final SplitNodesPlugin instance = new SplitNodesPlugin();
        final PluginParameters parameters = instance.createParameters();

        parameters.setStringValue(SplitNodesPlugin.SPLIT_PARAMETER_ID, "@");
        parameters.setBooleanValue(SplitNodesPlugin.DUPLICATE_TRANSACTIONS_PARAMETER_ID, true);
        parameters.setBooleanValue(SplitNodesPlugin.ALL_OCCURRENCES_PARAMETER_ID, true);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getVertexCount(), 6);
        assertEquals(graph.getTransactionCount(), 7);
        assertEquals(graph.getVertexTransactionCount(vxId1), 2);
        // Check transactions of new nodes (Assumed the new nodes are at vxId4 + 1 and vxId4 + 2)
        assertEquals(graph.getVertexTransactionCount(vxId4 + 1), 2);
        assertEquals(graph.getVertexTransactionCount(vxId4 + 2), 2);
        //assert new transactions are of type 'Network'
        assertEquals(graph.getStringValue(transactionTypeAttributeId, graph.getVertexTransaction(vxId4 + 1, 0)), "Network");
        assertEquals(graph.getStringValue(transactionTypeAttributeId, graph.getVertexTransaction(vxId4 + 1, 1)), "Network");
        assertEquals(graph.getStringValue(transactionTypeAttributeId, graph.getVertexTransaction(vxId4 + 2, 0)), "Network");
        assertEquals(graph.getStringValue(transactionTypeAttributeId, graph.getVertexTransaction(vxId4 + 2, 1)), "Network");
    }

    @Test
    public void testQueryWithMultipleNodes_WithoutDuplicateTransactions() throws Exception {
        final SplitNodesPlugin instance = new SplitNodesPlugin();
        final PluginParameters parameters = instance.createParameters();

        parameters.setStringValue(SplitNodesPlugin.SPLIT_PARAMETER_ID, ",");
        parameters.setBooleanValue(SplitNodesPlugin.ALL_OCCURRENCES_PARAMETER_ID, true);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getVertexCount(), 6);
        assertEquals(graph.getTransactionCount(), 5);
        assertEquals(graph.getVertexTransactionCount(vxId3), 2);
        assertEquals(graph.getVertexTransactionCount(vxId2), 3);
        // Check transactions of new nodes (Assumed the new nodes are at vxId4 + 1 and vxId4 + 2)
        assertEquals(graph.getVertexTransactionCount(vxId4 + 1), 1);
        assertEquals(graph.getVertexTransactionCount(vxId4 + 2), 1);
        //assert new transaction is of type 'Correlation'
        assertEquals(graph.getStringValue(transactionTypeAttributeId, graph.getVertexTransaction(vxId4 + 1, 0)), "Correlation");
        assertEquals(graph.getStringValue(transactionTypeAttributeId, graph.getVertexTransaction(vxId4 + 2, 0)), "Correlation");
    }

    @Test
    public void testQueryWithMultipleNodes_WithDuplicateTransactions() throws Exception {
        final SplitNodesPlugin instance = new SplitNodesPlugin();
        final PluginParameters parameters = instance.createParameters();

        parameters.setStringValue(SplitNodesPlugin.SPLIT_PARAMETER_ID, ",");
        parameters.setBooleanValue(SplitNodesPlugin.DUPLICATE_TRANSACTIONS_PARAMETER_ID, true);
        parameters.setBooleanValue(SplitNodesPlugin.ALL_OCCURRENCES_PARAMETER_ID, true);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getVertexCount(), 6);
        assertEquals(graph.getTransactionCount(), 6);
        assertEquals(graph.getVertexTransactionCount(vxId3), 1);
        assertEquals(graph.getVertexTransactionCount(vxId2), 2);
        // Check transactions of new node(Assumed the new node from vxId2 is at vxId4 + 1 and the new node from vxId3 is at vxId4 + 2)
        assertEquals(graph.getVertexTransactionCount(vxId4 + 1), 2);
        assertEquals(graph.getVertexTransactionCount(vxId4 + 2), 1);
        //assert new transactions are of type 'Network'
        assertEquals(graph.getStringValue(transactionTypeAttributeId, graph.getVertexTransaction(vxId4 + 1, 0)), "Network");
        assertEquals(graph.getStringValue(transactionTypeAttributeId, graph.getVertexTransaction(vxId4 + 1, 1)), "Network");
        assertEquals(graph.getStringValue(transactionTypeAttributeId, graph.getVertexTransaction(vxId4 + 2, 0)), "Network");
    }

    @Test
    public void testQueryWithNoResultingNodesToSplitTo_AllOccurancesNotSelected_WithoutDuplicateTransactions() throws Exception {
        final SplitNodesPlugin instance = new SplitNodesPlugin();
        final PluginParameters parameters = instance.createParameters();

        parameters.setStringValue(SplitNodesPlugin.SPLIT_PARAMETER_ID, "Y");
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        // Assert the node is renamed to "YY"
        assertEquals(graph.getVertexCount(), 4);
        assertEquals(graph.getTransactionCount(), 3);
        assertEquals(graph.getStringValue(vertexIdentifierAttribute, vxId4), "YY");
    }

    @Test
    public void testQueryWithNoResultingNodesToSplitTo_AllOccurancesSelected_WithoutDuplicateTransactions() throws Exception {
        final SplitNodesPlugin instance = new SplitNodesPlugin();
        final PluginParameters parameters = instance.createParameters();

        parameters.setStringValue(SplitNodesPlugin.SPLIT_PARAMETER_ID, "Y");
        parameters.setBooleanValue(SplitNodesPlugin.ALL_OCCURRENCES_PARAMETER_ID, true);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        // Assert no new nodes are created
        assertEquals(graph.getVertexCount(), 4);
        assertEquals(graph.getTransactionCount(), 3);
        assertEquals(graph.getStringValue(vertexIdentifierAttribute, vxId4), "YYY");
    }

    @Test
    public void testQueryWithNoResultingNodesToSplitTo_AllOccurancesNotSelected_WithDuplicateTransactions() throws Exception {
        final SplitNodesPlugin instance = new SplitNodesPlugin();
        final PluginParameters parameters = instance.createParameters();

        parameters.setStringValue(SplitNodesPlugin.SPLIT_PARAMETER_ID, "Y");
        parameters.setBooleanValue(SplitNodesPlugin.DUPLICATE_TRANSACTIONS_PARAMETER_ID, true);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        // Assert the node is renamed to "YY"
        assertEquals(graph.getVertexCount(), 4);
        assertEquals(graph.getTransactionCount(), 3);
        assertEquals(graph.getStringValue(vertexIdentifierAttribute, vxId4), "YY");
    }

    @Test
    public void testQueryWithNoResultingNodesToSplitTo_AllOccurancesSelected_WithDuplicateTransactions() throws Exception {
        final SplitNodesPlugin instance = new SplitNodesPlugin();
        final PluginParameters parameters = instance.createParameters();

        parameters.setStringValue(SplitNodesPlugin.SPLIT_PARAMETER_ID, "Y");
        parameters.setBooleanValue(SplitNodesPlugin.ALL_OCCURRENCES_PARAMETER_ID, true);
        parameters.setBooleanValue(SplitNodesPlugin.DUPLICATE_TRANSACTIONS_PARAMETER_ID, true);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        // Assert no new nodes are created
        assertEquals(graph.getVertexCount(), 4);
        assertEquals(graph.getTransactionCount(), 3);
        assertEquals(graph.getStringValue(vertexIdentifierAttribute, vxId4), "YYY");
    }
}
