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
 * Connected Components Plugin Test.
 *
 * @author canis_majoris
 */
public class ConnectivityDegreePluginNGTest {

    private int connectivityDegreeAttribute;
    private int componentSizeAttribute;
    
    private int vxId0;
    private int vxId1;
    private int vxId2;
    private int vxId3;
    private int vxId4;
    private int vxId5;
    private int vxId6;
    private int vxId7;
    private int vxId8;
    private int vxId9;
    private int vxId10;
    private int vxId11;
    private int vxId12;
    private int vxId13;
    private int vxId14;
    
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
        connectivityDegreeAttribute = SnaConcept.VertexAttribute.CONNECTIVITY_DEGREE.ensure(graph);
        componentSizeAttribute = SnaConcept.VertexAttribute.COMPONENT_SIZE.ensure(graph);

        // add vertices
        vxId0 = graph.addVertex();
        vxId1 = graph.addVertex();
        vxId2 = graph.addVertex();
        vxId3 = graph.addVertex();
        vxId4 = graph.addVertex();
        vxId5 = graph.addVertex();
        vxId6 = graph.addVertex();
        vxId7 = graph.addVertex();
        vxId8 = graph.addVertex();
        vxId9 = graph.addVertex();
        vxId10 = graph.addVertex();
        vxId11 = graph.addVertex();
        vxId12 = graph.addVertex();
        vxId13 = graph.addVertex();
        vxId14 = graph.addVertex();
        graph.addVertex();

        // add transactions
        graph.addTransaction(vxId0, vxId0, true);
        graph.addTransaction(vxId1, vxId2, true);
        graph.addTransaction(vxId3, vxId4, true);
        graph.addTransaction(vxId4, vxId5, true);
        graph.addTransaction(vxId3, vxId5, true);
        graph.addTransaction(vxId6, vxId7, true);
        graph.addTransaction(vxId6, vxId8, true);
        graph.addTransaction(vxId6, vxId9, true);
        graph.addTransaction(vxId10, vxId11, true);
        graph.addTransaction(vxId11, vxId12, true);
        graph.addTransaction(vxId12, vxId13, true);
        graph.addTransaction(vxId13, vxId14, true);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        graph = null;
    }

    @Test
    public void testConnectivityDegree() throws Exception {
        final ConnectivityDegreePlugin instance = new ConnectivityDegreePlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setBooleanValue(ConnectivityDegreePlugin.INCLUDE_CONNECTIONS_IN_PARAMETER_ID, true);
        parameters.setBooleanValue(ConnectivityDegreePlugin.INCLUDE_CONNECTIONS_OUT_PARAMETER_ID, true);
        parameters.setBooleanValue(ConnectivityDegreePlugin.TREAT_UNDIRECTED_BIDIRECTIONAL, true);
        parameters.setBooleanValue(ConnectivityDegreePlugin.IGNORE_SINGLETONS, true);
        parameters.setBooleanValue(ConnectivityDegreePlugin.NORMALISE, false);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getFloatValue(connectivityDegreeAttribute, vxId12), 1.0f);
        assertEquals(graph.getFloatValue(componentSizeAttribute, vxId12), 5.0f);

        assertEquals(graph.getFloatValue(connectivityDegreeAttribute, vxId0), 0.0f);
        assertEquals(graph.getFloatValue(componentSizeAttribute, vxId0), 1.0f);

        assertEquals(graph.getFloatValue(connectivityDegreeAttribute, vxId4), 0.0f);
        assertEquals(graph.getFloatValue(componentSizeAttribute, vxId4), 3.0f);
    }

    @Test
    public void testConnectivityDegreeSingletons() throws Exception {
        final ConnectivityDegreePlugin instance = new ConnectivityDegreePlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setBooleanValue(ConnectivityDegreePlugin.INCLUDE_CONNECTIONS_IN_PARAMETER_ID, true);
        parameters.setBooleanValue(ConnectivityDegreePlugin.INCLUDE_CONNECTIONS_OUT_PARAMETER_ID, true);
        parameters.setBooleanValue(ConnectivityDegreePlugin.TREAT_UNDIRECTED_BIDIRECTIONAL, true);
        parameters.setBooleanValue(ConnectivityDegreePlugin.IGNORE_SINGLETONS, false);
        parameters.setBooleanValue(ConnectivityDegreePlugin.NORMALISE, false);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getFloatValue(connectivityDegreeAttribute, vxId12), 2.0f);
        assertEquals(graph.getFloatValue(componentSizeAttribute, vxId12), 5.0f);

        assertEquals(graph.getFloatValue(connectivityDegreeAttribute, vxId0), 0.0f);
        assertEquals(graph.getFloatValue(componentSizeAttribute, vxId0), 1.0f);

        assertEquals(graph.getFloatValue(connectivityDegreeAttribute, vxId4), 0.0f);
        assertEquals(graph.getFloatValue(componentSizeAttribute, vxId4), 3.0f);
    }

    @Test
    public void testConnectivityDegreeNormalised() throws Exception {
        final ConnectivityDegreePlugin instance = new ConnectivityDegreePlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setBooleanValue(ConnectivityDegreePlugin.INCLUDE_CONNECTIONS_IN_PARAMETER_ID, true);
        parameters.setBooleanValue(ConnectivityDegreePlugin.INCLUDE_CONNECTIONS_OUT_PARAMETER_ID, true);
        parameters.setBooleanValue(ConnectivityDegreePlugin.TREAT_UNDIRECTED_BIDIRECTIONAL, true);
        parameters.setBooleanValue(ConnectivityDegreePlugin.IGNORE_SINGLETONS, true);
        parameters.setBooleanValue(ConnectivityDegreePlugin.NORMALISE, true);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getFloatValue(connectivityDegreeAttribute, vxId12), 5.0f);
        assertEquals(graph.getFloatValue(componentSizeAttribute, vxId12), 5.0f);

        assertEquals(graph.getFloatValue(connectivityDegreeAttribute, vxId0), 4.0f);
        assertEquals(graph.getFloatValue(componentSizeAttribute, vxId0), 1.0f);

        assertEquals(graph.getFloatValue(connectivityDegreeAttribute, vxId4), 4.0f);
        assertEquals(graph.getFloatValue(componentSizeAttribute, vxId4), 3.0f);
    }

    @Test
    public void testConnectivityDegreeSingletonsNormalised() throws Exception {
        final ConnectivityDegreePlugin instance = new ConnectivityDegreePlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setBooleanValue(ConnectivityDegreePlugin.INCLUDE_CONNECTIONS_IN_PARAMETER_ID, true);
        parameters.setBooleanValue(ConnectivityDegreePlugin.INCLUDE_CONNECTIONS_OUT_PARAMETER_ID, true);
        parameters.setBooleanValue(ConnectivityDegreePlugin.TREAT_UNDIRECTED_BIDIRECTIONAL, true);
        parameters.setBooleanValue(ConnectivityDegreePlugin.IGNORE_SINGLETONS, false);
        parameters.setBooleanValue(ConnectivityDegreePlugin.NORMALISE, true);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getFloatValue(connectivityDegreeAttribute, vxId12), 8.0f);
        assertEquals(graph.getFloatValue(componentSizeAttribute, vxId12), 5.0f);

        assertEquals(graph.getFloatValue(connectivityDegreeAttribute, vxId0), 5.0f);
        assertEquals(graph.getFloatValue(componentSizeAttribute, vxId0), 1.0f);

        assertEquals(graph.getFloatValue(connectivityDegreeAttribute, vxId4), 6.0f);
        assertEquals(graph.getFloatValue(componentSizeAttribute, vxId4), 3.0f);
    }
}
