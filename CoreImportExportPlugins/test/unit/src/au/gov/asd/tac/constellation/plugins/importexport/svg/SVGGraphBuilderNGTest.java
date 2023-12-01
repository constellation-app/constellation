/*
* Copyright 2010-2023 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.importexport.svg;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.node.plugins.DefaultPluginInteraction;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.svg.SVGObject;
import au.gov.asd.tac.constellation.utilities.visual.AxisConstants;
import au.gov.asd.tac.constellation.utilities.visual.VisualManager;
import java.awt.Component;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test for{@link SVGGraphBuilder}
 * @author capricornunicorn123
 */
public class SVGGraphBuilderNGTest {
    private MockedStatic<GraphManager> graphManagerStaticMock;
    private MockedStatic<GraphNode> graphNodeStaticMock;
    private VisualManager visualManagerMock;
    private GraphManager graphManagerMock;
    private Graph graph;
    private PluginInteraction interactionMock;
    private GraphNode contextMock;
    private Component visualComponentMock;
    private final String graphName = "Test Graph 1";
    
    // Positional Attributes
    private int vertexAttributeIdX, vertexAttributeIdY, vertexAttributeIdZ;
    
    // Vertex Ids
    private int vertexId1, vertexId2, vertexId3, vertexId4, vertexId5, vertexId6;
    
    // Transaction Ids
    private int transactionId1, transactionId2, transactionId3, transactionId4, transactionId5;
    
    // Other Attributea
    private int vertexAttributeIdSelected, transactionAttributeIdSelected, vertexAttributeIdPinned;
    
    public SVGGraphBuilderNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        
        graph = buildTestableGraph();
        graphNodeStaticMock = mockStatic(GraphNode.class);
        graphManagerStaticMock = mockStatic(GraphManager.class);
        visualManagerMock = mock(VisualManager.class);
        contextMock = mock(GraphNode.class);
        graphManagerMock = mock(GraphManager.class);
        interactionMock = mock(DefaultPluginInteraction.class);
        visualComponentMock = mock(Component.class);
        
        //Getting Graph from GraphNode
        doReturn(graphName).when(contextMock).getDisplayName();
        graphNodeStaticMock.when(() 
                -> GraphNode.getGraphNode(graphName))
                .thenReturn(contextMock);
       
        doReturn(graph).when(contextMock).getGraph();
        
        doReturn(graph).when(graphManagerMock).getActiveGraph(); 
        
        //Geting VisualManager from GraphNode 
        graphManagerStaticMock.when(() 
                -> GraphManager.getDefault())
                .thenReturn(graphManagerMock);
        graphNodeStaticMock.when(() 
                -> GraphNode.getGraphNode(graph))
                .thenReturn(contextMock);
        doReturn(visualManagerMock).when(contextMock).getVisualManager();
        
        // visual manager retruning sizing info 
        doReturn(visualComponentMock).when(visualManagerMock).getVisualComponent();
        doReturn(1080).when(visualComponentMock).getHeight();
        doReturn(1080).when(visualComponentMock).getWidth();
        
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        graphNodeStaticMock.close();
        graphManagerStaticMock.close();
    }

    /**
     * Test of withReadableGraph method, of class SVGGraphBuilder.
     */
    @Test
    public void testWithReadableGraph() {
        System.out.println("withReadableGraph");
        GraphReadMethods graph = GraphNode.getGraphNode(graphName).getGraph().getReadableGraph();
        SVGGraphBuilder instance = new SVGGraphBuilder();
        SVGGraphBuilder result = instance.withReadableGraph(graph);
        assertEquals(result, instance);
    }

    /**
     * Test of withInteraction method, of class SVGGraphBuilder.
     */
    @Test
    public void testWithInteraction() {
        System.out.println("withInteraction");
        PluginInteraction interaction = interactionMock;
        SVGGraphBuilder instance = new SVGGraphBuilder();
        SVGGraphBuilder result = instance.withInteraction(interaction);
        assertEquals(result, instance);
    }

    /**
     * Test of withTitle method, of class SVGGraphBuilder.
     */
    @Test
    public void testWithTitle() {
        System.out.println("withTitle");
        String title = "Test Title";
        SVGGraphBuilder instance = new SVGGraphBuilder();
        SVGGraphBuilder result = instance.withTitle(title);
        assertEquals(result, instance);
    }

    /**
     * Test of withBackground method, of class SVGGraphBuilder.
     */
    @Test
    public void testWithBackground() {
        System.out.println("withBackground");
        ConstellationColor color = ConstellationColor.BANANA;
        SVGGraphBuilder instance = new SVGGraphBuilder();
        SVGGraphBuilder result = instance.withBackground(color);
        assertEquals(result, instance);
    }

    /**
     * Test of withElements method, of class SVGGraphBuilder.
     */
    @Test
    public void testWithNodes() {
        System.out.println("withNodes");
        Boolean selectedNodesOnly = true;
        SVGGraphBuilder instance = new SVGGraphBuilder();
        SVGGraphBuilder result = instance.withElements(selectedNodesOnly);
        assertEquals(result, instance);
    }

    /**
     * Test of includeConnections method, of class SVGGraphBuilder.
     */
    @Test
    public void testIncludeConnections() {
        System.out.println("includeConnections");
        Boolean showConnections = true;
        SVGGraphBuilder instance = new SVGGraphBuilder();
        SVGGraphBuilder result = instance.includeConnections(showConnections);
        assertEquals(result, instance);
    }

    /**
     * Test of includeNodeLabels method, of class SVGGraphBuilder.
     */
    @Test
    public void testIncludeNodeLabels() {
        System.out.println("includeNodeLabels");
        Boolean showNodeLabels = true;
        SVGGraphBuilder instance = new SVGGraphBuilder();
        SVGGraphBuilder result = instance.includeNodeLabels(showNodeLabels);
        assertEquals(result, instance);
    }

    /**
     * Test of includeConnectionLabels method, of class SVGGraphBuilder.
     */
    @Test
    public void testIncludeConnectionLabels() {
        System.out.println("includeConnectionLabels");
        Boolean showConnectionLabels = true;
        SVGGraphBuilder instance = new SVGGraphBuilder();
        SVGGraphBuilder result = instance.includeConnectionLabels(showConnectionLabels);
        assertEquals(result, instance);
    }

    /**
     * Test of fromPerspective method, of class SVGGraphBuilder.
     */
    @Test
    public void testFromPerspective() {
        System.out.println("fromPerspective");
        AxisConstants exportPerspective = AxisConstants.X_POSITIVE;
        SVGGraphBuilder instance = new SVGGraphBuilder();
        SVGGraphBuilder result = instance.fromPerspective(exportPerspective);
        assertEquals(result, instance);

    }

    /**
     * Test of build method, of class SVGGraphBuilder.
     */
    @Test
    public void testBuild() throws Exception {
        System.out.println("build");
        SVGGraphBuilder instance = new SVGGraphBuilder()
                .withInteraction(interactionMock)
                .withReadableGraph(graph.getReadableGraph())
                .withTitle(graphName)
                .fromPerspective(AxisConstants.Z_POSITIVE)
                .withElements(true)
                .includeNodeLabels(true)
                .includeConnections(true)
                .includeConnectionLabels(false);
        
        SVGObject result = new SVGObject(instance.build());
        
        assertNotNull(result.getChild(String.format("node-%s",vertexId2)));
    }
    
    /**
     * Test of build method, of class SVGGraphBuilder.
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testBuild_withoutReadableGraph() throws InterruptedException {
        System.out.println("build");
        SVGGraphBuilder instance = new SVGGraphBuilder()
                .withInteraction(interactionMock)
                .withTitle(graphName)
                .fromPerspective(AxisConstants.Z_POSITIVE)
                .withElements(true)
                .includeNodeLabels(true)
                .includeConnections(true)
                .includeConnectionLabels(false); 
        assertNull(instance.build());
    }
    
    /**
     * Test of build method, of class SVGGraphBuilder.
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testBuild_withoutInteraction() throws InterruptedException {
        System.out.println("build");
        SVGGraphBuilder instance = new SVGGraphBuilder()
                .withReadableGraph(graph.getReadableGraph())
                .withTitle(graphName)
                .fromPerspective(AxisConstants.Z_POSITIVE)
                .withElements(true)
                .includeNodeLabels(true)
                .includeConnections(true)
                .includeConnectionLabels(false); 
        assertNull(instance.build());
    }
    
    /**
     * Test of build method, of class SVGGraphBuilder.
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testBuild_graphTitle() throws InterruptedException {
        System.out.println("build");
        SVGGraphBuilder instance = new SVGGraphBuilder()
                .withInteraction(interactionMock)
                .withReadableGraph(graph.getReadableGraph())
                .fromPerspective(AxisConstants.Z_POSITIVE)
                .withElements(true)
                .includeNodeLabels(true)
                .includeConnections(true)
                .includeConnectionLabels(false); 
        assertNull(instance.build());
    }
    
    private Graph buildTestableGraph() throws InterruptedException{
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        final Graph localGraph = new DualGraph(schema);

        WritableGraph wg = localGraph.getWritableGraph("Autosave", true);
        try {
            vertexAttributeIdX = VisualConcept.VertexAttribute.X.ensure(wg);
            vertexAttributeIdY = VisualConcept.VertexAttribute.Y.ensure(wg);
            vertexAttributeIdZ = VisualConcept.VertexAttribute.Z.ensure(wg);
            vertexAttributeIdPinned = VisualConcept.VertexAttribute.PINNED.ensure(wg);
            vertexAttributeIdSelected = VisualConcept.VertexAttribute.SELECTED.ensure(wg);
            transactionAttributeIdSelected = VisualConcept.TransactionAttribute.SELECTED.ensure(wg);

            vertexId1 = wg.addVertex();
            wg.setFloatValue(vertexAttributeIdX, vertexId1, 1.0f);
            wg.setFloatValue(vertexAttributeIdY, vertexId1, 1.0f);
            wg.setBooleanValue(vertexAttributeIdSelected, vertexId1, false);
            
            vertexId2 = wg.addVertex();
            wg.setFloatValue(vertexAttributeIdX, vertexId2, 5.0f);
            wg.setFloatValue(vertexAttributeIdY, vertexId2, 1.0f);
            wg.setBooleanValue(vertexAttributeIdSelected, vertexId2, true);
            wg.setBooleanValue(vertexAttributeIdPinned, vertexId2, true);
            
            vertexId3 = wg.addVertex();
            wg.setFloatValue(vertexAttributeIdX, vertexId3, 1.0f);
            wg.setFloatValue(vertexAttributeIdY, vertexId3, 5.0f);
            wg.setBooleanValue(vertexAttributeIdSelected, vertexId3, false);
            
            vertexId4 = wg.addVertex();
            wg.setFloatValue(vertexAttributeIdX, vertexId4, 5.0f);
            wg.setFloatValue(vertexAttributeIdY, vertexId4, 5.0f);
            wg.setBooleanValue(vertexAttributeIdSelected, vertexId4, false);
            
            vertexId5 = wg.addVertex();
            wg.setFloatValue(vertexAttributeIdX, vertexId5, 10.0f);
            wg.setFloatValue(vertexAttributeIdY, vertexId5, 10.0f);
            wg.setBooleanValue(vertexAttributeIdSelected, vertexId5, true);
            
            vertexId6 = wg.addVertex();
            wg.setFloatValue(vertexAttributeIdX, vertexId6, 15.0f);
            wg.setFloatValue(vertexAttributeIdY, vertexId6, 15.0f);

            transactionId1 = wg.addTransaction(vertexId1, vertexId2, false);
            transactionId2 = wg.addTransaction(vertexId2, vertexId3, false);
            transactionId3 = wg.addTransaction(vertexId2, vertexId5, true);
            transactionId4 = wg.addTransaction(vertexId2, vertexId2, true);
            transactionId5 = wg.addTransaction(vertexId2, vertexId2, false);
        } finally {
            wg.commit();
        }
        return localGraph;
    }
    
}
