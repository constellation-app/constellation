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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.utility;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.node.plugins.DefaultPluginEnvironment;
import au.gov.asd.tac.constellation.graph.node.plugins.DefaultPluginInteraction;
import au.gov.asd.tac.constellation.graph.node.plugins.PluginManager;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.plugins.CompleteSchemaPlugin;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginSynchronizer;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.reporting.GraphReport;
import au.gov.asd.tac.constellation.plugins.reporting.PluginReport;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Andromeda-224
 */
public class CompleteSchemaPluginNGTest {
    private PluginManager manager;
    private PluginReport report;

    private DefaultPluginEnvironment environment;
    private Plugin plugin;
    private StoreGraph graph;
    private PluginSynchronizer synchroniser;

    private GraphReport graphReport;
    
    @BeforeMethod
    public void setUpMethod() throws Exception {
        environment = new DefaultPluginEnvironment();
        plugin = new CompleteSchemaPlugin();
        graph = new StoreGraph(SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema());
        
        synchroniser = new PluginSynchronizer(1);
        graphReport = new GraphReport(graph.getId());

        manager = new PluginManager(environment, plugin, graph, false, synchroniser);
        report = new PluginReport(graphReport, plugin);
    }
 
     /**
     * Test of edit method, of class CompleteSchemaPlugin.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testEdit() throws Exception {
        final int vertexLabelAttr = VisualConcept.VertexAttribute.LABEL.ensure(graph);
        final int vertexSelectedAttr = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        final int vertexTypeAttr = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);       

        final int sourceVxId = graph.addVertex();
        graph.setStringValue(vertexLabelAttr, sourceVxId, "foo");
        graph.setBooleanValue(vertexSelectedAttr, sourceVxId, false);
        
         // buildId the graph
        for (int i = 0; i < 5; i++) {
            final int desintationVxId = graph.addVertex();
            graph.setStringValue(vertexLabelAttr, desintationVxId, String.format("destination %s", i));
            graph.setObjectValue(vertexTypeAttr, desintationVxId, AnalyticConcept.VertexType.COUNTRY);
            for (int j = 0; j < 5; j++) {
                graph.addTransaction(sourceVxId, desintationVxId, true);
            }
        }

        final int vertexIdentifierAttribute = VisualConcept.VertexAttribute.LABEL.ensure(graph);
        String label = graph.getStringValue(vertexIdentifierAttribute, sourceVxId);
        assertEquals("foo", label);
        
        final PluginInteraction interaction = new DefaultPluginInteraction(manager, report);
        final CompleteSchemaPlugin instance = new CompleteSchemaPlugin();
        final PluginParameters parameters = instance.createParameters();
        assertEquals(6, graph.getVertexCount());
        assertEquals(25, graph.getTransactionCount());
        
        instance.edit(graph, interaction, parameters);
        
        assertTrue(interaction.getCurrentMessage().contains("Completed 6 nodes & 25 transactions"));
        assertEquals(6, graph.getVertexCount());
        assertEquals(25, graph.getTransactionCount());
        label = graph.getStringValue(vertexIdentifierAttribute, sourceVxId);
        assertEquals("foo<Unknown>", label);
        assertEquals(report.getPluginName(), "Complete Graph Plugin");
        
    }    
}
