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
import au.gov.asd.tac.constellation.graph.node.plugins.DefaultPluginEnvironment;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.reporting.PluginReport;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.RemoveNodesPlugin.REMOVE_TYPE_PARAMETER_ID;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.RemoveNodesPlugin.THRESHOLD_PARAMETER_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class RemoveNodesPluginNGTest {

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
     * Test of edit method, of class RemoveNodesPlugin. selected and identifier
     * attributes not found
     *
     * @throws InterruptedException
     * @throws PluginException
     */
//    @Test(expectedExceptions=PluginException.class)
//    public void testIdentifierAttributesNotFound() throws InterruptedException, PluginException {
//        System.out.println("identifierAttributesNotFound");
//        final RemoveNodesPlugin instance = new RemoveNodesPlugin();
//        final PluginParameters parameters = instance.createParameters();
//                
//        VisualConcept.VertexAttribute.SELECTED.ensure(graph);
//        DefaultPluginEnvironment env = spy(DefaultPluginEnvironment.class);
//        doNothing().when(env).reportException(anyString(), any(PluginInteraction.class), any(PluginReport.class), any(PluginNotificationLevel.class), any(Exception.class));
//        //Method should throw exception as Identifier attribute is not enabled on vertex
//        PluginExecution.withPlugin(instance).inEnvironment(env).withParameters(parameters).executeNow(graph);
//    }
//    
//    /**
//     * Test of edit method, of class RemoveNodesPlugin. selected and identifier
//     * attributes not found
//     *
//     * @throws InterruptedException
//     */
//    @Test(expectedExceptions=PluginException.class)
//    public void testSelectedAttributesNotFound() throws InterruptedException, PluginException {
//        System.out.println("selectedAttributesNotFound");
//        final RemoveNodesPlugin instance = new RemoveNodesPlugin();
//        final PluginParameters parameters = instance.createParameters(); 
//        vertexIdentifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
//        DefaultPluginEnvironment env = spy(DefaultPluginEnvironment.class);
//        doNothing().when(env).reportException(anyString(), any(PluginInteraction.class), any(PluginReport.class), any(PluginNotificationLevel.class), any(Exception.class));
//        //Method should throw exception as Selected attribute is not enabled on vertex
//        PluginExecution.withPlugin(instance).inEnvironment(env).withParameters(parameters).executeNow(graph);
//    }

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
