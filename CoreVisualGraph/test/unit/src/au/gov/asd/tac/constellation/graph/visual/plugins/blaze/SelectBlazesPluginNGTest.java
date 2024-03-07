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
package au.gov.asd.tac.constellation.graph.visual.plugins.blaze;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.Blaze;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.visual.VisualGraphPluginRegistry;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Select Blazes Plugin Test.
 *
 * @author elnath
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class SelectBlazesPluginNGTest extends ConstellationTest {

    private StoreGraph graph;

    @BeforeMethod
    public void setUpMethod() {
        // create an analytic graph
        final Schema schema = SchemaFactoryUtilities.getDefaultSchemaFactory().createSchema();
        graph = new StoreGraph(schema);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        graph = null;
    }

    @Test
    public void testSelectBlazesWithNoBlazes() throws Exception {
        // setup attributes
        final int vertexSelectedAttribute = VisualConcept.VertexAttribute.SELECTED.ensure(graph);

        // add a node to the graph
        final int vxId0 = graph.addVertex();

        final SelectBlazesPlugin instance = new SelectBlazesPlugin();

        PluginExecution.withPlugin(instance).executeNow(graph);
        assertEquals(graph.getBooleanValue(vertexSelectedAttribute, vxId0), false);

        PluginExecution.withPlugin(instance).executeNow(graph);
        assertEquals(graph.getBooleanValue(vertexSelectedAttribute, vxId0), false);
    }

    @Test
    public void testSelectBlazes() throws Exception {
        // setup attributes
        final int vertexBlazeAttribute = VisualConcept.VertexAttribute.BLAZE.ensure(graph);
        final int vertexSelectedAttribute = VisualConcept.VertexAttribute.SELECTED.ensure(graph);

        // add a node to the graph
        final int vxId0 = graph.addVertex();

        PluginExecution.withPlugin(VisualGraphPluginRegistry.SELECT_BLAZES).executeNow(graph);
        assertEquals(graph.getBooleanValue(vertexSelectedAttribute, vxId0), false);

        // add a blaze
        graph.setObjectValue(vertexBlazeAttribute, vxId0, new Blaze(0, ConstellationColor.BANANA));

        PluginExecution.withPlugin(VisualGraphPluginRegistry.SELECT_BLAZES).executeNow(graph);
        assertEquals(graph.getBooleanValue(vertexSelectedAttribute, vxId0), true);
    }
}
