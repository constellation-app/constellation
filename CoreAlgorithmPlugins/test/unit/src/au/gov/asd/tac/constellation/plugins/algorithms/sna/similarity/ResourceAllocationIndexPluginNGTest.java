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
 * Resource Allocation Index Plugin Test.
 *
 * @author canis_majoris
 */
public class ResourceAllocationIndexPluginNGTest {

    private int transactionRaiAttribute, vertexSelectedAttribute;
    private int vxId0, vxId1, vxId2, vxId3, vxId4;
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
        transactionRaiAttribute = SnaConcept.TransactionAttribute.RESOURCE_ALLOCATION_INDEX.ensure(graph);
        vertexSelectedAttribute = VisualConcept.VertexAttribute.SELECTED.ensure(graph);

        // add vertices
        vxId0 = graph.addVertex();
        vxId1 = graph.addVertex();
        vxId2 = graph.addVertex();
        vxId3 = graph.addVertex();
        vxId4 = graph.addVertex();

        // add transactions
        graph.addTransaction(vxId0, vxId1, true);
        graph.addTransaction(vxId0, vxId2, true);
        graph.addTransaction(vxId0, vxId3, true);
        graph.addTransaction(vxId0, vxId4, true);
        graph.addTransaction(vxId1, vxId2, true);
        graph.addTransaction(vxId1, vxId3, true);
        graph.addTransaction(vxId1, vxId4, true);
        graph.addTransaction(vxId2, vxId3, true);
        graph.addTransaction(vxId2, vxId4, true);
        graph.addTransaction(vxId3, vxId4, true);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        graph = null;
    }

    @Test
    public void testRAI() throws Exception {
        final ResourceAllocationIndexPlugin instance = new ResourceAllocationIndexPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setBooleanValue(ResourceAllocationIndexPlugin.INCLUDE_CONNECTIONS_IN_PARAMETER_ID, true);
        parameters.setBooleanValue(ResourceAllocationIndexPlugin.INCLUDE_CONNECTIONS_OUT_PARAMETER_ID, true);
        parameters.setBooleanValue(ResourceAllocationIndexPlugin.TREAT_UNDIRECTED_BIDIRECTIONAL_PARAMETER_ID, true);
        parameters.setIntegerValue(ResourceAllocationIndexPlugin.MINIMUM_COMMON_FEATURES_PARAMETER_ID, 1);
        parameters.setBooleanValue(ResourceAllocationIndexPlugin.SELECTED_ONLY_PARAMETER_ID, false);
        parameters.setBooleanValue(ResourceAllocationIndexPlugin.COMMUNITY_PARAMETER_ID, false);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getFloatValue(transactionRaiAttribute, 11), 0.75f);
        assertEquals(graph.getFloatValue(transactionRaiAttribute, 18), 0.75f);
    }
}
