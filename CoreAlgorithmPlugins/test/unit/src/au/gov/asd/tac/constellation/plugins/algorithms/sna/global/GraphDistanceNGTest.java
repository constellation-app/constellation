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
 * Global Graph Distance Plugin Test.
 *
 * @author canis_majoris
 */
public class GraphDistanceNGTest {

    private int dAttribute, rAttribute, aAttribute;
    private int vxId0, vxId1, vxId2, vxId3, vxId4, vxId5;
    private int txId0, txId1, txId2, txId3, txId4, txId5;
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
        dAttribute = SnaConcept.GraphAttribute.DIAMETER.ensure(graph);
        rAttribute = SnaConcept.GraphAttribute.RADIUS.ensure(graph);
        aAttribute = SnaConcept.GraphAttribute.AVERAGE_DISTANCE.ensure(graph);

        // add vertices
        vxId0 = graph.addVertex();
        vxId1 = graph.addVertex();
        vxId2 = graph.addVertex();
        vxId3 = graph.addVertex();
        vxId4 = graph.addVertex();
        vxId5 = graph.addVertex();

        // add transactions
        txId0 = graph.addTransaction(vxId0, vxId1, true);
        txId1 = graph.addTransaction(vxId2, vxId1, true);
        txId2 = graph.addTransaction(vxId1, vxId3, true);
        txId3 = graph.addTransaction(vxId1, vxId4, true);
        txId4 = graph.addTransaction(vxId3, vxId5, true);
        txId5 = graph.addTransaction(vxId4, vxId5, true);

    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        graph = null;
    }

    @Test
    public void testRadiusDiameter() throws Exception {
        final GraphDistancePlugin instance = new GraphDistancePlugin();
        final PluginParameters parameters = instance.createParameters();
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getFloatValue(rAttribute, 0), 2f);
        assertEquals(graph.getFloatValue(dAttribute, 0), 3f);
        assertEquals(graph.getFloatValue(aAttribute, 0), 1.7333333f);
    }
}
