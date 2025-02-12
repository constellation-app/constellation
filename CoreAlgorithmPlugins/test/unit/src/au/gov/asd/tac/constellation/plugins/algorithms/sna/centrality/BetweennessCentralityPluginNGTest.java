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
 * Betweenness Centrality Plugin Test.
 *
 * @author cygnus_x-1
 */
public class BetweennessCentralityPluginNGTest {

    private int vertexBetweennessAttribute;
    private int vertexOutBetweennessAttribute;
    
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
        vertexBetweennessAttribute = SnaConcept.VertexAttribute.BETWEENNESS_CENTRALITY.ensure(graph);
        vertexOutBetweennessAttribute = SnaConcept.VertexAttribute.OUT_BETWEENNESS_CENTRALITY.ensure(graph);
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
    public void testDirectedBetweenness() throws Exception {
        final BetweennessCentralityPlugin instance = new BetweennessCentralityPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setBooleanValue(BetweennessCentralityPlugin.INCLUDE_CONNECTIONS_IN_PARAMETER_ID, false);
        parameters.setBooleanValue(BetweennessCentralityPlugin.INCLUDE_CONNECTIONS_OUT_PARAMETER_ID, true);
        parameters.setBooleanValue(BetweennessCentralityPlugin.TREAT_UNDIRECTED_BIDIRECTIONAL, true);
        parameters.setBooleanValue(BetweennessCentralityPlugin.NORMALISE_AVAILABLE_PARAMETER_ID, false);
        parameters.setBooleanValue(BetweennessCentralityPlugin.NORMALISE_POSSIBLE_PARAMETER_ID, false);
        parameters.setBooleanValue(BetweennessCentralityPlugin.NORMALISE_CONNECTED_COMPONENTS_PARAMETER_ID, false);
        parameters.setBooleanValue(BetweennessCentralityPlugin.SELECTED_ONLY_PARAMETER_ID, false);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getFloatValue(vertexOutBetweennessAttribute, vxId0), 0f);
        assertEquals(graph.getFloatValue(vertexOutBetweennessAttribute, vxId1), 3f);
        assertEquals(graph.getFloatValue(vertexOutBetweennessAttribute, vxId2), 0f);
        assertEquals(graph.getFloatValue(vertexOutBetweennessAttribute, vxId3), 3f);
        assertEquals(graph.getFloatValue(vertexOutBetweennessAttribute, vxId4), 0f);
    }

    @Test
    public void testNormalisedDirectedBetweenness() throws Exception {
        final BetweennessCentralityPlugin instance = new BetweennessCentralityPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setBooleanValue(BetweennessCentralityPlugin.INCLUDE_CONNECTIONS_IN_PARAMETER_ID, false);
        parameters.setBooleanValue(BetweennessCentralityPlugin.INCLUDE_CONNECTIONS_OUT_PARAMETER_ID, true);
        parameters.setBooleanValue(BetweennessCentralityPlugin.TREAT_UNDIRECTED_BIDIRECTIONAL, true);
        parameters.setBooleanValue(BetweennessCentralityPlugin.NORMALISE_AVAILABLE_PARAMETER_ID, true);
        parameters.setBooleanValue(BetweennessCentralityPlugin.NORMALISE_POSSIBLE_PARAMETER_ID, false);
        parameters.setBooleanValue(BetweennessCentralityPlugin.NORMALISE_CONNECTED_COMPONENTS_PARAMETER_ID, false);
        parameters.setBooleanValue(BetweennessCentralityPlugin.SELECTED_ONLY_PARAMETER_ID, false);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getFloatValue(vertexOutBetweennessAttribute, vxId0), 0f / 3f);
        assertEquals(graph.getFloatValue(vertexOutBetweennessAttribute, vxId1), 3f / 3f);
        assertEquals(graph.getFloatValue(vertexOutBetweennessAttribute, vxId2), 0f / 3f);
        assertEquals(graph.getFloatValue(vertexOutBetweennessAttribute, vxId3), 3f / 3f);
        assertEquals(graph.getFloatValue(vertexOutBetweennessAttribute, vxId4), 0f / 3f);
    }

    @Test
    public void testUndirectedBetweenness() throws Exception {
        final BetweennessCentralityPlugin instance = new BetweennessCentralityPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setBooleanValue(BetweennessCentralityPlugin.INCLUDE_CONNECTIONS_IN_PARAMETER_ID, true);
        parameters.setBooleanValue(BetweennessCentralityPlugin.INCLUDE_CONNECTIONS_OUT_PARAMETER_ID, true);
        parameters.setBooleanValue(BetweennessCentralityPlugin.TREAT_UNDIRECTED_BIDIRECTIONAL, true);
        parameters.setBooleanValue(BetweennessCentralityPlugin.NORMALISE_AVAILABLE_PARAMETER_ID, false);
        parameters.setBooleanValue(BetweennessCentralityPlugin.NORMALISE_POSSIBLE_PARAMETER_ID, false);
        parameters.setBooleanValue(BetweennessCentralityPlugin.NORMALISE_CONNECTED_COMPONENTS_PARAMETER_ID, false);
        parameters.setBooleanValue(BetweennessCentralityPlugin.SELECTED_ONLY_PARAMETER_ID, false);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getFloatValue(vertexBetweennessAttribute, vxId0), 0f);
        assertEquals(graph.getFloatValue(vertexBetweennessAttribute, vxId1), 6f);
        assertEquals(graph.getFloatValue(vertexBetweennessAttribute, vxId2), 0f);
        assertEquals(graph.getFloatValue(vertexBetweennessAttribute, vxId3), 6f);
        assertEquals(graph.getFloatValue(vertexBetweennessAttribute, vxId4), 0f);
    }

    @Test
    public void testNormalisedUndirectedBetweenness() throws Exception {
        final BetweennessCentralityPlugin instance = new BetweennessCentralityPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setBooleanValue(BetweennessCentralityPlugin.INCLUDE_CONNECTIONS_IN_PARAMETER_ID, true);
        parameters.setBooleanValue(BetweennessCentralityPlugin.INCLUDE_CONNECTIONS_OUT_PARAMETER_ID, true);
        parameters.setBooleanValue(BetweennessCentralityPlugin.TREAT_UNDIRECTED_BIDIRECTIONAL, true);
        parameters.setBooleanValue(BetweennessCentralityPlugin.NORMALISE_AVAILABLE_PARAMETER_ID, true);
        parameters.setBooleanValue(BetweennessCentralityPlugin.NORMALISE_POSSIBLE_PARAMETER_ID, false);
        parameters.setBooleanValue(BetweennessCentralityPlugin.NORMALISE_CONNECTED_COMPONENTS_PARAMETER_ID, false);
        parameters.setBooleanValue(BetweennessCentralityPlugin.SELECTED_ONLY_PARAMETER_ID, false);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getFloatValue(vertexBetweennessAttribute, vxId0), 0f / 6f);
        assertEquals(graph.getFloatValue(vertexBetweennessAttribute, vxId1), 6f / 6f);
        assertEquals(graph.getFloatValue(vertexBetweennessAttribute, vxId2), 0f / 6f);
        assertEquals(graph.getFloatValue(vertexBetweennessAttribute, vxId3), 6f / 6f);
        assertEquals(graph.getFloatValue(vertexBetweennessAttribute, vxId4), 0f / 6f);
    }
    
    @Test
    public void testNoDirectionBetweenness() throws Exception {
        final BetweennessCentralityPlugin instance = new BetweennessCentralityPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setBooleanValue(BetweennessCentralityPlugin.INCLUDE_CONNECTIONS_IN_PARAMETER_ID, false);
        parameters.setBooleanValue(BetweennessCentralityPlugin.INCLUDE_CONNECTIONS_OUT_PARAMETER_ID, false);
        parameters.setBooleanValue(BetweennessCentralityPlugin.TREAT_UNDIRECTED_BIDIRECTIONAL, false);
        parameters.setBooleanValue(BetweennessCentralityPlugin.NORMALISE_AVAILABLE_PARAMETER_ID, false);
        parameters.setBooleanValue(BetweennessCentralityPlugin.NORMALISE_POSSIBLE_PARAMETER_ID, false);
        parameters.setBooleanValue(BetweennessCentralityPlugin.NORMALISE_CONNECTED_COMPONENTS_PARAMETER_ID, false);
        parameters.setBooleanValue(BetweennessCentralityPlugin.SELECTED_ONLY_PARAMETER_ID, false);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getFloatValue(vertexBetweennessAttribute, vxId0), 0F);
        assertEquals(graph.getFloatValue(vertexBetweennessAttribute, vxId1), 0F);
        assertEquals(graph.getFloatValue(vertexBetweennessAttribute, vxId2), 0F);
        assertEquals(graph.getFloatValue(vertexBetweennessAttribute, vxId3), 0F);
        assertEquals(graph.getFloatValue(vertexBetweennessAttribute, vxId4), 0F);
    }
}
