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
 * Closeness Centrality Plugin Test.
 *
 * @author cygnus_x-1
 */
public class ClosenessCentralityPluginNGTest {

    private int vertexClosenessAttribute;
    private int vertexOutClosenessAttribute;
    private int vertexHarmonicClosenessAttribute;
    private int vertexOutHarmonicClosenessAttribute;
    
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
        vertexClosenessAttribute = SnaConcept.VertexAttribute.CLOSENESS_CENTRALITY.ensure(graph);
        vertexOutClosenessAttribute = SnaConcept.VertexAttribute.OUT_CLOSENESS_CENTRALITY.ensure(graph);
        vertexHarmonicClosenessAttribute = SnaConcept.VertexAttribute.HARMONIC_CLOSENESS_CENTRALITY.ensure(graph);
        vertexOutHarmonicClosenessAttribute = SnaConcept.VertexAttribute.OUT_HARMONIC_CLOSENESS_CENTRALITY.ensure(graph);
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
    public void testDirectedCloseness() throws Exception {
        final ClosenessCentralityPlugin instance = new ClosenessCentralityPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setBooleanValue(ClosenessCentralityPlugin.HARMONIC_PARAMETER_ID, false);
        parameters.setBooleanValue(ClosenessCentralityPlugin.INCLUDE_CONNECTIONS_IN_PARAMETER_ID, false);
        parameters.setBooleanValue(ClosenessCentralityPlugin.INCLUDE_CONNECTIONS_OUT_PARAMETER_ID, true);
        parameters.setBooleanValue(ClosenessCentralityPlugin.TREAT_UNDIRECTED_BIDIRECTIONAL, true);
        parameters.setBooleanValue(ClosenessCentralityPlugin.NORMALISE_AVAILABLE_PARAMETER_ID, false);
        parameters.setBooleanValue(ClosenessCentralityPlugin.NORMALISE_POSSIBLE_PARAMETER_ID, false);
        parameters.setBooleanValue(ClosenessCentralityPlugin.NORMALISE_CONNECTED_COMPONENTS_PARAMETER_ID, false);
        parameters.setBooleanValue(ClosenessCentralityPlugin.SELECTED_ONLY_PARAMETER_ID, false);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getFloatValue(vertexOutClosenessAttribute, vxId0), 0.125f);
        assertEquals(graph.getFloatValue(vertexOutClosenessAttribute, vxId1), 0.25f);
        assertEquals(graph.getFloatValue(vertexOutClosenessAttribute, vxId2), 0.33333334f);
        assertEquals(graph.getFloatValue(vertexOutClosenessAttribute, vxId3), 1f);
        assertEquals(graph.getFloatValue(vertexOutClosenessAttribute, vxId4), 0f);
    }

    @Test
    public void testNormalisedDirectedCloseness() throws Exception {
        final ClosenessCentralityPlugin instance = new ClosenessCentralityPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setBooleanValue(ClosenessCentralityPlugin.HARMONIC_PARAMETER_ID, false);
        parameters.setBooleanValue(ClosenessCentralityPlugin.INCLUDE_CONNECTIONS_IN_PARAMETER_ID, false);
        parameters.setBooleanValue(ClosenessCentralityPlugin.INCLUDE_CONNECTIONS_OUT_PARAMETER_ID, true);
        parameters.setBooleanValue(ClosenessCentralityPlugin.TREAT_UNDIRECTED_BIDIRECTIONAL, true);
        parameters.setBooleanValue(ClosenessCentralityPlugin.NORMALISE_AVAILABLE_PARAMETER_ID, true);
        parameters.setBooleanValue(ClosenessCentralityPlugin.NORMALISE_POSSIBLE_PARAMETER_ID, false);
        parameters.setBooleanValue(ClosenessCentralityPlugin.NORMALISE_CONNECTED_COMPONENTS_PARAMETER_ID, false);
        parameters.setBooleanValue(ClosenessCentralityPlugin.SELECTED_ONLY_PARAMETER_ID, false);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getFloatValue(vertexOutClosenessAttribute, vxId0), 0.125f / 1f);
        assertEquals(graph.getFloatValue(vertexOutClosenessAttribute, vxId1), 0.25f / 1f);
        assertEquals(graph.getFloatValue(vertexOutClosenessAttribute, vxId2), 0.33333334f / 1f);
        assertEquals(graph.getFloatValue(vertexOutClosenessAttribute, vxId3), 1f / 1f);
        assertEquals(graph.getFloatValue(vertexOutClosenessAttribute, vxId4), 0f / 1f);
    }

    @Test
    public void testDirectedHarmonicCloseness() throws Exception {
        final ClosenessCentralityPlugin instance = new ClosenessCentralityPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setBooleanValue(ClosenessCentralityPlugin.HARMONIC_PARAMETER_ID, true);
        parameters.setBooleanValue(ClosenessCentralityPlugin.INCLUDE_CONNECTIONS_IN_PARAMETER_ID, false);
        parameters.setBooleanValue(ClosenessCentralityPlugin.INCLUDE_CONNECTIONS_OUT_PARAMETER_ID, true);
        parameters.setBooleanValue(ClosenessCentralityPlugin.TREAT_UNDIRECTED_BIDIRECTIONAL, true);
        parameters.setBooleanValue(ClosenessCentralityPlugin.NORMALISE_AVAILABLE_PARAMETER_ID, false);
        parameters.setBooleanValue(ClosenessCentralityPlugin.NORMALISE_POSSIBLE_PARAMETER_ID, false);
        parameters.setBooleanValue(ClosenessCentralityPlugin.NORMALISE_CONNECTED_COMPONENTS_PARAMETER_ID, false);
        parameters.setBooleanValue(ClosenessCentralityPlugin.SELECTED_ONLY_PARAMETER_ID, false);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getFloatValue(vertexOutHarmonicClosenessAttribute, vxId0), 0.46666664f);
        assertEquals(graph.getFloatValue(vertexOutHarmonicClosenessAttribute, vxId1), 0.5f);
        assertEquals(graph.getFloatValue(vertexOutHarmonicClosenessAttribute, vxId2), 0.3f);
        assertEquals(graph.getFloatValue(vertexOutHarmonicClosenessAttribute, vxId3), 0.2f);
        assertEquals(graph.getFloatValue(vertexOutHarmonicClosenessAttribute, vxId4), 0f);
    }

    @Test
    public void testNormalisedDirectedHarmonicCloseness() throws Exception {
        final ClosenessCentralityPlugin instance = new ClosenessCentralityPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setBooleanValue(ClosenessCentralityPlugin.HARMONIC_PARAMETER_ID, true);
        parameters.setBooleanValue(ClosenessCentralityPlugin.INCLUDE_CONNECTIONS_IN_PARAMETER_ID, false);
        parameters.setBooleanValue(ClosenessCentralityPlugin.INCLUDE_CONNECTIONS_OUT_PARAMETER_ID, true);
        parameters.setBooleanValue(ClosenessCentralityPlugin.TREAT_UNDIRECTED_BIDIRECTIONAL, true);
        parameters.setBooleanValue(ClosenessCentralityPlugin.NORMALISE_AVAILABLE_PARAMETER_ID, true);
        parameters.setBooleanValue(ClosenessCentralityPlugin.NORMALISE_POSSIBLE_PARAMETER_ID, false);
        parameters.setBooleanValue(ClosenessCentralityPlugin.NORMALISE_CONNECTED_COMPONENTS_PARAMETER_ID, false);
        parameters.setBooleanValue(ClosenessCentralityPlugin.SELECTED_ONLY_PARAMETER_ID, false);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getFloatValue(vertexOutHarmonicClosenessAttribute, vxId0), 0.46666664f / 0.5f);
        assertEquals(graph.getFloatValue(vertexOutHarmonicClosenessAttribute, vxId1), 0.5f / 0.5f);
        assertEquals(graph.getFloatValue(vertexOutHarmonicClosenessAttribute, vxId2), 0.3f / 0.5f);
        assertEquals(graph.getFloatValue(vertexOutHarmonicClosenessAttribute, vxId3), 0.2f / 0.5f);
        assertEquals(graph.getFloatValue(vertexOutHarmonicClosenessAttribute, vxId4), 0f / 0.5f);
    }

    @Test
    public void testUndirectedCloseness() throws Exception {
        final ClosenessCentralityPlugin instance = new ClosenessCentralityPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setBooleanValue(ClosenessCentralityPlugin.HARMONIC_PARAMETER_ID, false);
        parameters.setBooleanValue(ClosenessCentralityPlugin.INCLUDE_CONNECTIONS_IN_PARAMETER_ID, true);
        parameters.setBooleanValue(ClosenessCentralityPlugin.INCLUDE_CONNECTIONS_OUT_PARAMETER_ID, true);
        parameters.setBooleanValue(ClosenessCentralityPlugin.TREAT_UNDIRECTED_BIDIRECTIONAL, true);
        parameters.setBooleanValue(ClosenessCentralityPlugin.NORMALISE_AVAILABLE_PARAMETER_ID, false);
        parameters.setBooleanValue(ClosenessCentralityPlugin.NORMALISE_POSSIBLE_PARAMETER_ID, false);
        parameters.setBooleanValue(ClosenessCentralityPlugin.NORMALISE_CONNECTED_COMPONENTS_PARAMETER_ID, false);
        parameters.setBooleanValue(ClosenessCentralityPlugin.SELECTED_ONLY_PARAMETER_ID, false);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getFloatValue(vertexClosenessAttribute, vxId0), 0.125f);
        assertEquals(graph.getFloatValue(vertexClosenessAttribute, vxId1), 0.2f);
        assertEquals(graph.getFloatValue(vertexClosenessAttribute, vxId2), 0.16666667f);
        assertEquals(graph.getFloatValue(vertexClosenessAttribute, vxId3), 0.2f);
        assertEquals(graph.getFloatValue(vertexClosenessAttribute, vxId4), 0.125f);
    }

    @Test
    public void testNormalisedUndirectedCloseness() throws Exception {
        final ClosenessCentralityPlugin instance = new ClosenessCentralityPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setBooleanValue(ClosenessCentralityPlugin.HARMONIC_PARAMETER_ID, false);
        parameters.setBooleanValue(ClosenessCentralityPlugin.INCLUDE_CONNECTIONS_IN_PARAMETER_ID, true);
        parameters.setBooleanValue(ClosenessCentralityPlugin.INCLUDE_CONNECTIONS_OUT_PARAMETER_ID, true);
        parameters.setBooleanValue(ClosenessCentralityPlugin.TREAT_UNDIRECTED_BIDIRECTIONAL, true);
        parameters.setBooleanValue(ClosenessCentralityPlugin.NORMALISE_AVAILABLE_PARAMETER_ID, true);
        parameters.setBooleanValue(ClosenessCentralityPlugin.NORMALISE_POSSIBLE_PARAMETER_ID, false);
        parameters.setBooleanValue(ClosenessCentralityPlugin.NORMALISE_CONNECTED_COMPONENTS_PARAMETER_ID, false);
        parameters.setBooleanValue(ClosenessCentralityPlugin.SELECTED_ONLY_PARAMETER_ID, false);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getFloatValue(vertexClosenessAttribute, vxId0), 0.125f / 0.2f);
        assertEquals(graph.getFloatValue(vertexClosenessAttribute, vxId1), 0.2f / 0.2f);
        assertEquals(graph.getFloatValue(vertexClosenessAttribute, vxId2), 0.16666667f / 0.2f);
        assertEquals(graph.getFloatValue(vertexClosenessAttribute, vxId3), 0.2f / 0.2f);
        assertEquals(graph.getFloatValue(vertexClosenessAttribute, vxId4), 0.125f / 0.2f);
    }

    @Test
    public void testUndirectedHarmonicCloseness() throws Exception {
        final ClosenessCentralityPlugin instance = new ClosenessCentralityPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setBooleanValue(ClosenessCentralityPlugin.HARMONIC_PARAMETER_ID, true);
        parameters.setBooleanValue(ClosenessCentralityPlugin.INCLUDE_CONNECTIONS_IN_PARAMETER_ID, true);
        parameters.setBooleanValue(ClosenessCentralityPlugin.INCLUDE_CONNECTIONS_OUT_PARAMETER_ID, true);
        parameters.setBooleanValue(ClosenessCentralityPlugin.TREAT_UNDIRECTED_BIDIRECTIONAL, true);
        parameters.setBooleanValue(ClosenessCentralityPlugin.NORMALISE_AVAILABLE_PARAMETER_ID, false);
        parameters.setBooleanValue(ClosenessCentralityPlugin.NORMALISE_POSSIBLE_PARAMETER_ID, false);
        parameters.setBooleanValue(ClosenessCentralityPlugin.NORMALISE_CONNECTED_COMPONENTS_PARAMETER_ID, false);
        parameters.setBooleanValue(ClosenessCentralityPlugin.SELECTED_ONLY_PARAMETER_ID, false);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getFloatValue(vertexHarmonicClosenessAttribute, vxId0), 0.9333334f);
        assertEquals(graph.getFloatValue(vertexHarmonicClosenessAttribute, vxId1), 1.4f);
        assertEquals(graph.getFloatValue(vertexHarmonicClosenessAttribute, vxId2), 1.2f);
        assertEquals(graph.getFloatValue(vertexHarmonicClosenessAttribute, vxId3), 1.4f);
        assertEquals(graph.getFloatValue(vertexHarmonicClosenessAttribute, vxId4), 0.9333334f);
    }

    @Test
    public void testNormalisedUndirectedHarmonicCloseness() throws Exception {
        final ClosenessCentralityPlugin instance = new ClosenessCentralityPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setBooleanValue(ClosenessCentralityPlugin.HARMONIC_PARAMETER_ID, true);
        parameters.setBooleanValue(ClosenessCentralityPlugin.INCLUDE_CONNECTIONS_IN_PARAMETER_ID, true);
        parameters.setBooleanValue(ClosenessCentralityPlugin.INCLUDE_CONNECTIONS_OUT_PARAMETER_ID, true);
        parameters.setBooleanValue(ClosenessCentralityPlugin.TREAT_UNDIRECTED_BIDIRECTIONAL, true);
        parameters.setBooleanValue(ClosenessCentralityPlugin.NORMALISE_AVAILABLE_PARAMETER_ID, true);
        parameters.setBooleanValue(ClosenessCentralityPlugin.NORMALISE_POSSIBLE_PARAMETER_ID, false);
        parameters.setBooleanValue(ClosenessCentralityPlugin.NORMALISE_CONNECTED_COMPONENTS_PARAMETER_ID, false);
        parameters.setBooleanValue(ClosenessCentralityPlugin.SELECTED_ONLY_PARAMETER_ID, false);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getFloatValue(vertexHarmonicClosenessAttribute, vxId0), 0.66666675f);
        assertEquals(graph.getFloatValue(vertexHarmonicClosenessAttribute, vxId1), 1.2f / 1.2f);
        assertEquals(graph.getFloatValue(vertexHarmonicClosenessAttribute, vxId2), 0.8571429f);
        assertEquals(graph.getFloatValue(vertexHarmonicClosenessAttribute, vxId3), 1.2f / 1.2f);
        assertEquals(graph.getFloatValue(vertexHarmonicClosenessAttribute, vxId4), 0.66666675f);
    }
    
    @Test
    public void testNoDirectionCloseness() throws Exception {
        final ClosenessCentralityPlugin instance = new ClosenessCentralityPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setBooleanValue(ClosenessCentralityPlugin.HARMONIC_PARAMETER_ID, false);
        parameters.setBooleanValue(ClosenessCentralityPlugin.INCLUDE_CONNECTIONS_IN_PARAMETER_ID, false);
        parameters.setBooleanValue(ClosenessCentralityPlugin.INCLUDE_CONNECTIONS_OUT_PARAMETER_ID, false);
        parameters.setBooleanValue(ClosenessCentralityPlugin.TREAT_UNDIRECTED_BIDIRECTIONAL, false);
        parameters.setBooleanValue(ClosenessCentralityPlugin.NORMALISE_AVAILABLE_PARAMETER_ID, false);
        parameters.setBooleanValue(ClosenessCentralityPlugin.NORMALISE_POSSIBLE_PARAMETER_ID, false);
        parameters.setBooleanValue(ClosenessCentralityPlugin.NORMALISE_CONNECTED_COMPONENTS_PARAMETER_ID, false);
        parameters.setBooleanValue(ClosenessCentralityPlugin.SELECTED_ONLY_PARAMETER_ID, false);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getFloatValue(vertexHarmonicClosenessAttribute, vxId0), 0F);
        assertEquals(graph.getFloatValue(vertexHarmonicClosenessAttribute, vxId1), 0F);
        assertEquals(graph.getFloatValue(vertexHarmonicClosenessAttribute, vxId2), 0F);
        assertEquals(graph.getFloatValue(vertexHarmonicClosenessAttribute, vxId3), 0F);
        assertEquals(graph.getFloatValue(vertexHarmonicClosenessAttribute, vxId4), 0F);
    }
}
