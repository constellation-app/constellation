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
package au.gov.asd.tac.constellation.graph.schema.visual.plugins;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.node.plugins.DefaultPluginEnvironment;
import au.gov.asd.tac.constellation.graph.node.plugins.DefaultPluginInteraction;
import au.gov.asd.tac.constellation.graph.node.plugins.PluginManager;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginSynchronizer;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.reporting.GraphReport;
import au.gov.asd.tac.constellation.plugins.reporting.PluginReport;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
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
        graph = new StoreGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());
        
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
        final int vertexIdentiferAttr = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        final int vertexSelectedAttr = VisualConcept.VertexAttribute.SELECTED.ensure(graph);      

        final int sourceVxId = graph.addVertex();
        graph.setStringValue(vertexIdentiferAttr, sourceVxId, "foo");
        graph.setBooleanValue(vertexSelectedAttr, sourceVxId, false);
        
         // buildId the graph
        for (int i = 0; i < 5; i++) {
            final int desintationVxId = graph.addVertex();
            graph.setStringValue(vertexIdentiferAttr, desintationVxId, String.format("destination %s", i));
            for (int j = 0; j < 5; j++) {
                graph.addTransaction(sourceVxId, desintationVxId, true);
            }
        }

        final int vertexLabelAttribute = VisualConcept.VertexAttribute.LABEL.ensure(graph);
        String label = graph.getStringValue(vertexLabelAttribute, sourceVxId);
        // null is the default value for label
        assertNull(label);
        
        final PluginInteraction interaction = new DefaultPluginInteraction(manager, report);
        final CompleteSchemaPlugin instance = new CompleteSchemaPlugin();
        final PluginParameters parameters = instance.createParameters();
        assertEquals(graph.getVertexCount(), 6);
        assertEquals(graph.getTransactionCount(), 25);
        
        instance.edit(graph, interaction, parameters);
        
        assertTrue(interaction.getCurrentMessage().contains("Completed 6 nodes & 25 transactions"));
        assertEquals(graph.getVertexCount(), 6);
        assertEquals(graph.getTransactionCount(), 25);
        label = graph.getStringValue(vertexLabelAttribute, sourceVxId);
        assertEquals(label, "foo");
        assertEquals(report.getPluginName(), "Complete Graph Plugin");       
    }    
}
