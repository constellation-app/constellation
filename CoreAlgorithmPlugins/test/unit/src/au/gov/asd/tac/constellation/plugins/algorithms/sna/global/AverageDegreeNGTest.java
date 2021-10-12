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
package au.gov.asd.tac.constellation.plugins.algorithms.sna.global;

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
 * Global Average Degree Plugin Test.
 *
 * @author canis_majoris
 */
public class AverageDegreeNGTest {

    private int graphAttribute;
    private int vxId0, vxId1, vxId2, vxId3, vxId4;
    private int txId0, txId1, txId2, txId3, txId4, txId5, txId6, txId7, txId8, txId9, txId10, txId11, txId12, txId13;
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
        graphAttribute = SnaConcept.GraphAttribute.AVERAGE_DEGREE.ensure(graph);

        // add vertices
        vxId0 = graph.addVertex(); // singleton
        vxId1 = graph.addVertex(); // loop
        vxId2 = graph.addVertex(); // 3 - 2 - 4 
        vxId3 = graph.addVertex(); // 3 - 2 - 4 
        vxId4 = graph.addVertex(); // 3 - 2 - 4 

        // add transactions
        txId1 = graph.addTransaction(vxId1, vxId1, true);
        txId2 = graph.addTransaction(vxId2, vxId3, true);
        txId3 = graph.addTransaction(vxId2, vxId4, true);

    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        graph = null;
    }

    @Test
    public void testAverageDegree() throws Exception {
        final AverageDegreePlugin instance = new AverageDegreePlugin();
        final PluginParameters parameters = instance.createParameters();
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getFloatValue(graphAttribute, 0), 1.3333334f);
    }
    
    @Test
    public void testNoDirectionAverageDegree() throws Exception {
        final AverageDegreePlugin instance = new AverageDegreePlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setBooleanValue(AverageDegreePlugin.INCLUDE_CONNECTIONS_IN_PARAMETER_ID, false);
        parameters.setBooleanValue(AverageDegreePlugin.INCLUDE_CONNECTIONS_OUT_PARAMETER_ID, false);
        parameters.setBooleanValue(AverageDegreePlugin.TREAT_UNDIRECTED_BIDIRECTIONAL, false);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getFloatValue(graphAttribute, 0), 0 / 0F);
    }
}
