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
 * Katz Centrality Plugin Test.
 *
 * @author cygnus_x-1
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class KatzCentralityPluginNGTest extends ConstellationTest {

    private int vertexKatzAttribute;
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
        vertexKatzAttribute = SnaConcept.VertexAttribute.KATZ_CENTRALITY.ensure(graph);

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
    public void testKatz() throws Exception {
        final KatzCentralityPlugin instance = new KatzCentralityPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setFloatValue(KatzCentralityPlugin.ALPHA_PARAMETER_ID, 0.1f);
        parameters.setFloatValue(KatzCentralityPlugin.BETA_PARAMETER_ID, 1.0f);
        parameters.setIntegerValue(KatzCentralityPlugin.ITERATIONS_PARAMETER_ID, 100);
        parameters.setFloatValue(KatzCentralityPlugin.EPSILON_PARAMETER_ID, 1E-8f);
        parameters.setBooleanValue(KatzCentralityPlugin.NORMALISE_POSSIBLE_PARAMETER_ID, false);
        parameters.setBooleanValue(KatzCentralityPlugin.NORMALISE_AVAILABLE_PARAMETER_ID, false);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getFloatValue(vertexKatzAttribute, vxId0), 1.137931f);
        assertEquals(graph.getFloatValue(vertexKatzAttribute, vxId1), 1.3793104f);
        assertEquals(graph.getFloatValue(vertexKatzAttribute, vxId2), 1.2758621f);
        assertEquals(graph.getFloatValue(vertexKatzAttribute, vxId3), 1.3793104f);
        assertEquals(graph.getFloatValue(vertexKatzAttribute, vxId4), 1.137931f);
    }

    @Test
    public void testNormalisedKatz() throws Exception {
        final KatzCentralityPlugin instance = new KatzCentralityPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setFloatValue(KatzCentralityPlugin.ALPHA_PARAMETER_ID, 0.1f);
        parameters.setFloatValue(KatzCentralityPlugin.BETA_PARAMETER_ID, 1.0f);
        parameters.setIntegerValue(KatzCentralityPlugin.ITERATIONS_PARAMETER_ID, 100);
        parameters.setFloatValue(KatzCentralityPlugin.EPSILON_PARAMETER_ID, 1E-8f);
        parameters.setBooleanValue(KatzCentralityPlugin.NORMALISE_POSSIBLE_PARAMETER_ID, true);
        parameters.setBooleanValue(KatzCentralityPlugin.NORMALISE_AVAILABLE_PARAMETER_ID, false);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getFloatValue(vertexKatzAttribute, vxId0), 0.42875203f);
        assertEquals(graph.getFloatValue(vertexKatzAttribute, vxId1), 0.4646652f);
        assertEquals(graph.getFloatValue(vertexKatzAttribute, vxId2), 0.44779003f);
        assertEquals(graph.getFloatValue(vertexKatzAttribute, vxId3), 0.4646652f);
        assertEquals(graph.getFloatValue(vertexKatzAttribute, vxId4), 0.42875203f);
    }
}
