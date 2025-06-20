/*
 * Copyright 2010-2025 Australian Signals Directorate
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
 * Test class for Complete Schema Selection Plugin
 * 
 * @author Delphinus8821
 */
public class CompleteSchemaSelectionPluginNGTest {
    
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
     * Test of edit method, of class CompleteSchemaSelectionPlugin.
     */
    @Test
    public void testEdit() throws Exception {
        final int vertexIdentiferAttr = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        final int vertexSelectedAttr = VisualConcept.VertexAttribute.SELECTED.ensure(graph);      

        final int sourceVxId = graph.addVertex();
        graph.setStringValue(vertexIdentiferAttr, sourceVxId, "foo");
        graph.setBooleanValue(vertexSelectedAttr, sourceVxId, true);
        
         // buildId the graph
        for (int i = 1; i < 5; i++) {
            final int destinationVxId = graph.addVertex();
            graph.setStringValue(vertexIdentiferAttr, destinationVxId, String.format("destination %s", i));
            graph.setBooleanValue(vertexSelectedAttr, destinationVxId, true);
            for (int j = 0; j < 5; j++) {
                graph.addTransaction(sourceVxId, destinationVxId, true);
            }
        }
        for (int i = 6; i < 10; i++) {
            final int destinationVxId = graph.addVertex();
            graph.setStringValue(vertexIdentiferAttr, destinationVxId, String.format("destination %s", i));
            graph.setBooleanValue(vertexSelectedAttr, destinationVxId, false);
            for (int j = 0; j < 5; j++) {
                graph.addTransaction(sourceVxId, destinationVxId, true);
            }
        }

        final int vertexLabelAttribute = VisualConcept.VertexAttribute.LABEL.ensure(graph);
        String label = graph.getStringValue(vertexLabelAttribute, sourceVxId);
        // null is the default value for label
        assertNull(label);
        
        final PluginInteraction interaction = new DefaultPluginInteraction(manager, report);
        final CompleteSchemaSelectionPlugin instance = new CompleteSchemaSelectionPlugin();
        final PluginParameters parameters = instance.createParameters();
        assertEquals(graph.getVertexCount(), 9);
        assertEquals(graph.getTransactionCount(), 40);
        
        instance.edit(graph, interaction, parameters);
        
        assertTrue(interaction.getCurrentMessage().contains("Completed 9 nodes & 40 transactions"));
        assertEquals(graph.getVertexCount(), 9);
        assertEquals(graph.getTransactionCount(), 40);
        label = graph.getStringValue(vertexLabelAttribute, sourceVxId);
        assertEquals(label, "foo");
        
        for (int i = 1; i < 5; i++) {
            final String destinationLabel = graph.getStringValue(vertexLabelAttribute, i);
            final String expectedResult = String.format("destination %s", i);
            assertEquals(destinationLabel, expectedResult);
        }
        
        for (int i = 6; i < 10; i++) {
            final String destinationLabel = graph.getStringValue(vertexLabelAttribute, i);
            assertEquals(destinationLabel, null);
        }
        
        assertEquals(report.getPluginName(), "Complete Graph Plugin");       
    }
}
