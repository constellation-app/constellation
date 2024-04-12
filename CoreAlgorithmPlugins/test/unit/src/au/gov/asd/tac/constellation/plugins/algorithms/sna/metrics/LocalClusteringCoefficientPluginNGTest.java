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
 * Local Clustering Coefficient Plugin Test.
 *
 * @author cygnus_x-1
 */
public class LocalClusteringCoefficientPluginNGTest {

    private int vertexLocalClusteringCoefficientAttribute;
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
        vertexLocalClusteringCoefficientAttribute = SnaConcept.VertexAttribute.CLUSTERING_COEFFICIENT.ensure(graph);

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
    public void testLocalClusteringCoefficient() throws Exception {
        final LocalClusteringCoefficientPlugin instance = new LocalClusteringCoefficientPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setBooleanValue(LocalClusteringCoefficientPlugin.NORMALISE_AVAILABLE_PARAMETER_ID, false);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getFloatValue(vertexLocalClusteringCoefficientAttribute, vxId0), 0f);
        assertEquals(graph.getFloatValue(vertexLocalClusteringCoefficientAttribute, vxId1), 0.33333334f);
        assertEquals(graph.getFloatValue(vertexLocalClusteringCoefficientAttribute, vxId2), 1.0f);
        assertEquals(graph.getFloatValue(vertexLocalClusteringCoefficientAttribute, vxId3), 0.33333334f);
        assertEquals(graph.getFloatValue(vertexLocalClusteringCoefficientAttribute, vxId4), 0f);
    }

    @Test
    public void testNormalisedLocalClusteringCoefficient() throws Exception {
        final LocalClusteringCoefficientPlugin instance = new LocalClusteringCoefficientPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setBooleanValue(LocalClusteringCoefficientPlugin.NORMALISE_AVAILABLE_PARAMETER_ID, true);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getFloatValue(vertexLocalClusteringCoefficientAttribute, vxId0), 0f / 1.0f);
        assertEquals(graph.getFloatValue(vertexLocalClusteringCoefficientAttribute, vxId1), 0.33333334f / 1.0f);
        assertEquals(graph.getFloatValue(vertexLocalClusteringCoefficientAttribute, vxId2), 1.0f / 1.0f);
        assertEquals(graph.getFloatValue(vertexLocalClusteringCoefficientAttribute, vxId3), 0.33333334f / 1.0f);
        assertEquals(graph.getFloatValue(vertexLocalClusteringCoefficientAttribute, vxId4), 0f / 1.0f);
    }
}
