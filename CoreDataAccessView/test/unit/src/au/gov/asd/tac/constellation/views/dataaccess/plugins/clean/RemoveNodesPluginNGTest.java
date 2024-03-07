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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.clean;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.node.plugins.DefaultPluginInteraction;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.RemoveNodesPlugin.REMOVE_TYPE_PARAMETER_ID;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.RemoveNodesPlugin.THRESHOLD_PARAMETER_ID;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class RemoveNodesPluginNGTest extends ConstellationTest {

    private StoreGraph graph;

    private int vxId1;
    private int vxId2;
    private int vxId3;
    private int vxId4;

    private int txId1;
    private int txId2;
    private int txId3;
    private int txId4;

    private int vertexIdentifierAttribute;
    private int vertexSelectedAttribute;

    @BeforeMethod
    public void setUpMethod() throws Exception {
        graph = new StoreGraph();

        vxId1 = graph.addVertex();
        vxId2 = graph.addVertex();
        vxId3 = graph.addVertex();
        vxId4 = graph.addVertex();

        txId1 = graph.addTransaction(vxId1, vxId2, true);
        txId2 = graph.addTransaction(vxId2, vxId3, true);
        txId3 = graph.addTransaction(vxId3, vxId4, true);
        txId4 = graph.addTransaction(vxId4, vxId2, true);
    }

    /**
     * Test of createParameters method, of class RemoveNodesPlugin.
     */
    @Test
    public void testCreateParameters() {
        System.out.println("createParameters");

        final RemoveNodesPlugin instance = new RemoveNodesPlugin();

        final PluginParameters parameters = instance.createParameters();

        assertEquals(parameters.getParameters().size(), 2);
        assertTrue(parameters.getParameters().containsKey(REMOVE_TYPE_PARAMETER_ID));
        assertTrue(parameters.getParameters().containsKey(THRESHOLD_PARAMETER_ID));
    }

    /**
     * Test of edit method, of class RemoveNodesPlugin. 
     * Identifier attribute not found
     *
     * @throws InterruptedException
     * @throws PluginException
     */
    @Test(expectedExceptions = PluginException.class)
    public void testIdentifierAttributesNotFound() throws InterruptedException, PluginException {
        System.out.println("identifierAttributesNotFound");
        final RemoveNodesPlugin instance = new RemoveNodesPlugin();
        final PluginParameters parameters = instance.createParameters();
        final DefaultPluginInteraction interaction = mock(DefaultPluginInteraction.class);
        
        vertexSelectedAttribute = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        instance.edit(graph, interaction, parameters); 
    }
    
    /**
     * Test of edit method, of class RemoveNodesPlugin.
     * Selected attribute not found
     *
     * @throws InterruptedException
     */
    @Test(expectedExceptions = PluginException.class)
    public void testSelectedAttributesNotFound() throws InterruptedException, PluginException {
        System.out.println("selectedAttributesNotFound");
        final RemoveNodesPlugin instance = new RemoveNodesPlugin();
        final PluginParameters parameters = instance.createParameters(); 
        final DefaultPluginInteraction interaction = mock(DefaultPluginInteraction.class);
        
        vertexIdentifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        instance.edit(graph, interaction, parameters); 
    }

    /**
     * Test of edit method, of class RemoveNodesPlugin. Default plugin parameter
     * values used
     *
     * @throws InterruptedException
     * @throws PluginException
     */
    @Test
    public void testEditDefaultParameterValues() throws InterruptedException, PluginException {
        System.out.println("editDefaultParameterValues");

        // setup the graph
        vertexIdentifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        vertexSelectedAttribute = VisualConcept.VertexAttribute.SELECTED.ensure(graph);

        graph.setStringValue(vertexIdentifierAttribute, vxId1, "Node");
        graph.setStringValue(vertexIdentifierAttribute, vxId2, "Vertex");
        graph.setStringValue(vertexIdentifierAttribute, vxId3, "Vertex 3");
        graph.setStringValue(vertexIdentifierAttribute, vxId4, "Another Vertex");

        graph.setBooleanValue(vertexSelectedAttribute, vxId1, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId2, false);
        graph.setBooleanValue(vertexSelectedAttribute, vxId3, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId4, true);

        final RemoveNodesPlugin instance = new RemoveNodesPlugin();
        final PluginParameters parameters = instance.createParameters();
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getVertexCount(), 2);
        assertEquals(graph.getTransactionCount(), 1);

        assertEquals(graph.vertexExists(vxId1), false);
        assertEquals(graph.vertexExists(vxId2), true);
        assertEquals(graph.vertexExists(vxId3), false);
        assertEquals(graph.vertexExists(vxId4), true);

        assertEquals(graph.transactionExists(txId1), false);
        assertEquals(graph.transactionExists(txId2), false);
        assertEquals(graph.transactionExists(txId3), false);
        assertEquals(graph.transactionExists(txId4), true);
    }

    /**
     * Test of edit method, of class RemoveNodesPlugin. Threshold set to 7
     *
     * @throws InterruptedException
     * @throws PluginException
     */
    @Test
    public void testEditDifferentThreshold() throws InterruptedException, PluginException {
        System.out.println("editDifferentThreshold");

        // setup the graph
        vertexIdentifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        vertexSelectedAttribute = VisualConcept.VertexAttribute.SELECTED.ensure(graph);

        graph.setStringValue(vertexIdentifierAttribute, vxId1, "Node");
        graph.setStringValue(vertexIdentifierAttribute, vxId2, "Vertex");
        graph.setStringValue(vertexIdentifierAttribute, vxId3, "Vertex 3");
        graph.setStringValue(vertexIdentifierAttribute, vxId4, "Another Vertex");

        graph.setBooleanValue(vertexSelectedAttribute, vxId1, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId2, false);
        graph.setBooleanValue(vertexSelectedAttribute, vxId3, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId4, true);

        final RemoveNodesPlugin instance = new RemoveNodesPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setIntegerValue(THRESHOLD_PARAMETER_ID, 7);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getVertexCount(), 3);
        assertEquals(graph.getTransactionCount(), 3);

        assertEquals(graph.vertexExists(vxId1), false);
        assertEquals(graph.vertexExists(vxId2), true);
        assertEquals(graph.vertexExists(vxId3), true);
        assertEquals(graph.vertexExists(vxId4), true);

        assertEquals(graph.transactionExists(txId1), false);
        assertEquals(graph.transactionExists(txId2), true);
        assertEquals(graph.transactionExists(txId3), true);
        assertEquals(graph.transactionExists(txId4), true);
    }
}
