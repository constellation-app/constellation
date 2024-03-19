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
package au.gov.asd.tac.constellation.plugins.algorithms.sna.centrality;

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
 * Degree Centrality Plugin Test.
 *
 * @author cygnus_x-1
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class DegreeCentralityPluginNGTest extends ConstellationTest {

    private int vertexDegreeAttribute, vertexInDegreeAttribute, vertexOutDegreeAttribute;
    private int vertexSelectedAttribute;
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
        vertexDegreeAttribute = SnaConcept.VertexAttribute.DEGREE_CENTRALITY.ensure(graph);
        vertexInDegreeAttribute = SnaConcept.VertexAttribute.IN_DEGREE_CENTRALITY.ensure(graph);
        vertexOutDegreeAttribute = SnaConcept.VertexAttribute.OUT_DEGREE_CENTRALITY.ensure(graph);
        vertexSelectedAttribute = VisualConcept.VertexAttribute.SELECTED.ensure(graph);

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
    public void testDegree() throws Exception {
        final DegreeCentralityPlugin instance = new DegreeCentralityPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setBooleanValue(DegreeCentralityPlugin.INCLUDE_CONNECTIONS_IN_PARAMETER_ID, true);
        parameters.setBooleanValue(DegreeCentralityPlugin.INCLUDE_CONNECTIONS_OUT_PARAMETER_ID, true);
        parameters.setBooleanValue(DegreeCentralityPlugin.TREAT_UNDIRECTED_BIDIRECTIONAL, true);
        parameters.setBooleanValue(DegreeCentralityPlugin.NORMALISE_AVAILABLE_PARAMETER_ID, false);
        parameters.setBooleanValue(DegreeCentralityPlugin.NORMALISE_POSSIBLE_PARAMETER_ID, false);
        parameters.setBooleanValue(DegreeCentralityPlugin.SELECTED_ONLY_PARAMETER_ID, false);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getFloatValue(vertexDegreeAttribute, vxId0), 1f);
        assertEquals(graph.getFloatValue(vertexDegreeAttribute, vxId1), 3f);
        assertEquals(graph.getFloatValue(vertexDegreeAttribute, vxId2), 2f);
        assertEquals(graph.getFloatValue(vertexDegreeAttribute, vxId3), 3f);
        assertEquals(graph.getFloatValue(vertexDegreeAttribute, vxId4), 1f);
    }

    @Test
    public void testNormalisedDegree() throws Exception {
        final DegreeCentralityPlugin instance = new DegreeCentralityPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setBooleanValue(DegreeCentralityPlugin.INCLUDE_CONNECTIONS_IN_PARAMETER_ID, true);
        parameters.setBooleanValue(DegreeCentralityPlugin.INCLUDE_CONNECTIONS_OUT_PARAMETER_ID, true);
        parameters.setBooleanValue(DegreeCentralityPlugin.TREAT_UNDIRECTED_BIDIRECTIONAL, true);
        parameters.setBooleanValue(DegreeCentralityPlugin.NORMALISE_AVAILABLE_PARAMETER_ID, true);
        parameters.setBooleanValue(DegreeCentralityPlugin.NORMALISE_POSSIBLE_PARAMETER_ID, false);
        parameters.setBooleanValue(DegreeCentralityPlugin.SELECTED_ONLY_PARAMETER_ID, false);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getFloatValue(vertexDegreeAttribute, vxId0), 1f / 3f);
        assertEquals(graph.getFloatValue(vertexDegreeAttribute, vxId1), 3f / 3f);
        assertEquals(graph.getFloatValue(vertexDegreeAttribute, vxId2), 2f / 3f);
        assertEquals(graph.getFloatValue(vertexDegreeAttribute, vxId3), 3f / 3f);
        assertEquals(graph.getFloatValue(vertexDegreeAttribute, vxId4), 1f / 3f);
    }

    @Test
    public void testInDegree() throws Exception {
        final DegreeCentralityPlugin instance = new DegreeCentralityPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setBooleanValue(DegreeCentralityPlugin.INCLUDE_CONNECTIONS_IN_PARAMETER_ID, true);
        parameters.setBooleanValue(DegreeCentralityPlugin.INCLUDE_CONNECTIONS_OUT_PARAMETER_ID, false);
        parameters.setBooleanValue(DegreeCentralityPlugin.TREAT_UNDIRECTED_BIDIRECTIONAL, true);
        parameters.setBooleanValue(DegreeCentralityPlugin.NORMALISE_AVAILABLE_PARAMETER_ID, false);
        parameters.setBooleanValue(DegreeCentralityPlugin.NORMALISE_POSSIBLE_PARAMETER_ID, false);
        parameters.setBooleanValue(DegreeCentralityPlugin.SELECTED_ONLY_PARAMETER_ID, false);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getFloatValue(vertexInDegreeAttribute, vxId0), 0f);
        assertEquals(graph.getFloatValue(vertexInDegreeAttribute, vxId1), 1f);
        assertEquals(graph.getFloatValue(vertexInDegreeAttribute, vxId2), 1f);
        assertEquals(graph.getFloatValue(vertexInDegreeAttribute, vxId3), 2f);
        assertEquals(graph.getFloatValue(vertexInDegreeAttribute, vxId4), 1f);
    }

    @Test
    public void testNormalisedInDegree() throws Exception {
        final DegreeCentralityPlugin instance = new DegreeCentralityPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setBooleanValue(DegreeCentralityPlugin.INCLUDE_CONNECTIONS_IN_PARAMETER_ID, true);
        parameters.setBooleanValue(DegreeCentralityPlugin.INCLUDE_CONNECTIONS_OUT_PARAMETER_ID, false);
        parameters.setBooleanValue(DegreeCentralityPlugin.TREAT_UNDIRECTED_BIDIRECTIONAL, true);
        parameters.setBooleanValue(DegreeCentralityPlugin.NORMALISE_AVAILABLE_PARAMETER_ID, true);
        parameters.setBooleanValue(DegreeCentralityPlugin.NORMALISE_POSSIBLE_PARAMETER_ID, false);
        parameters.setBooleanValue(DegreeCentralityPlugin.SELECTED_ONLY_PARAMETER_ID, false);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getFloatValue(vertexInDegreeAttribute, vxId0), 0f / 2f);
        assertEquals(graph.getFloatValue(vertexInDegreeAttribute, vxId1), 1f / 2f);
        assertEquals(graph.getFloatValue(vertexInDegreeAttribute, vxId2), 1f / 2f);
        assertEquals(graph.getFloatValue(vertexInDegreeAttribute, vxId3), 2f / 2f);
        assertEquals(graph.getFloatValue(vertexInDegreeAttribute, vxId4), 1f / 2f);
    }

    @Test
    public void testOutDegree() throws Exception {
        final DegreeCentralityPlugin instance = new DegreeCentralityPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setBooleanValue(DegreeCentralityPlugin.INCLUDE_CONNECTIONS_IN_PARAMETER_ID, false);
        parameters.setBooleanValue(DegreeCentralityPlugin.INCLUDE_CONNECTIONS_OUT_PARAMETER_ID, true);
        parameters.setBooleanValue(DegreeCentralityPlugin.TREAT_UNDIRECTED_BIDIRECTIONAL, true);
        parameters.setBooleanValue(DegreeCentralityPlugin.NORMALISE_AVAILABLE_PARAMETER_ID, false);
        parameters.setBooleanValue(DegreeCentralityPlugin.NORMALISE_POSSIBLE_PARAMETER_ID, false);
        parameters.setBooleanValue(DegreeCentralityPlugin.SELECTED_ONLY_PARAMETER_ID, false);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getFloatValue(vertexOutDegreeAttribute, vxId0), 1f);
        assertEquals(graph.getFloatValue(vertexOutDegreeAttribute, vxId1), 2f);
        assertEquals(graph.getFloatValue(vertexOutDegreeAttribute, vxId2), 1f);
        assertEquals(graph.getFloatValue(vertexOutDegreeAttribute, vxId3), 1f);
        assertEquals(graph.getFloatValue(vertexOutDegreeAttribute, vxId4), 0f);
    }

    @Test
    public void testNormalisedOutDegree() throws Exception {
        final DegreeCentralityPlugin instance = new DegreeCentralityPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setBooleanValue(DegreeCentralityPlugin.INCLUDE_CONNECTIONS_IN_PARAMETER_ID, false);
        parameters.setBooleanValue(DegreeCentralityPlugin.INCLUDE_CONNECTIONS_OUT_PARAMETER_ID, true);
        parameters.setBooleanValue(DegreeCentralityPlugin.TREAT_UNDIRECTED_BIDIRECTIONAL, true);
        parameters.setBooleanValue(DegreeCentralityPlugin.NORMALISE_AVAILABLE_PARAMETER_ID, true);
        parameters.setBooleanValue(DegreeCentralityPlugin.NORMALISE_POSSIBLE_PARAMETER_ID, false);
        parameters.setBooleanValue(DegreeCentralityPlugin.SELECTED_ONLY_PARAMETER_ID, false);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getFloatValue(vertexOutDegreeAttribute, vxId0), 1f / 2f);
        assertEquals(graph.getFloatValue(vertexOutDegreeAttribute, vxId1), 2f / 2f);
        assertEquals(graph.getFloatValue(vertexOutDegreeAttribute, vxId2), 1f / 2f);
        assertEquals(graph.getFloatValue(vertexOutDegreeAttribute, vxId3), 1f / 2f);
        assertEquals(graph.getFloatValue(vertexOutDegreeAttribute, vxId4), 0f / 2f);
    }
    
    @Test
    public void testNoDirectionDegree() throws Exception {
        final DegreeCentralityPlugin instance = new DegreeCentralityPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setBooleanValue(DegreeCentralityPlugin.INCLUDE_CONNECTIONS_IN_PARAMETER_ID, false);
        parameters.setBooleanValue(DegreeCentralityPlugin.INCLUDE_CONNECTIONS_OUT_PARAMETER_ID, false);
        parameters.setBooleanValue(DegreeCentralityPlugin.TREAT_UNDIRECTED_BIDIRECTIONAL, false);
        parameters.setBooleanValue(DegreeCentralityPlugin.NORMALISE_AVAILABLE_PARAMETER_ID, false);
        parameters.setBooleanValue(DegreeCentralityPlugin.NORMALISE_POSSIBLE_PARAMETER_ID, false);
        parameters.setBooleanValue(DegreeCentralityPlugin.SELECTED_ONLY_PARAMETER_ID, false);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getFloatValue(vertexDegreeAttribute, vxId0), 0F);
        assertEquals(graph.getFloatValue(vertexDegreeAttribute, vxId1), 0F);
        assertEquals(graph.getFloatValue(vertexDegreeAttribute, vxId2), 0F);
        assertEquals(graph.getFloatValue(vertexDegreeAttribute, vxId3), 0F);
        assertEquals(graph.getFloatValue(vertexDegreeAttribute, vxId4), 0F);
    }
}
