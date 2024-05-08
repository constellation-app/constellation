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
import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabel;
import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabels;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.svg.SVGObject;
import au.gov.asd.tac.constellation.utilities.visual.AxisConstants;
import au.gov.asd.tac.constellation.utilities.visual.VisualManager;
import java.awt.Component;
import java.util.ArrayList;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
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
    private int vertexAttributeIdX;
    private int vertexAttributeIdY;
    private int vertexAttributeIdZ;
    
    // Vertex Ids
    private int vertexId1;
    private int vertexId2;
    private int vertexId3;
    private int vertexId4;
    private int vertexId5;
    private int vertexId6;
    
    // Transaction Ids
    private int transactionId1;
    private int transactionId2;
    private int transactionId3;
    private int transactionId4;
    private int transactionId5;
    
    
    // Other Attributea
    private int vertexAttributeIdSelected;
    private int vertexAttributeIdLabel;
    private int transactionAttributeIdSelected;
    private int vertexAttributeIdPinned;
    private int graphAttributeIdTopLabels;
    private int graphAttributeIdBottomLabels;
    private int graphAttributeIdTransactionLabels;
    
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
        final GraphReadMethods localGraph = GraphNode.getGraphNode(graphName).getGraph().getReadableGraph();
        final SVGGraphBuilder instance = new SVGGraphBuilder();
        final SVGGraphBuilder result = instance.withReadableGraph(localGraph);
        assertEquals(result, instance);
    }

    /**
     * Test of withInteraction method, of class SVGGraphBuilder.
     */
    @Test
    public void testWithInteraction() {
        System.out.println("withInteraction");
        final PluginInteraction interaction = interactionMock;
        final SVGGraphBuilder instance = new SVGGraphBuilder();
        final SVGGraphBuilder result = instance.withInteraction(interaction);
        assertEquals(result, instance);
    }

    /**
     * Test of withTitle method, of class SVGGraphBuilder.
     */
    @Test
    public void testWithTitle() {
        System.out.println("withTitle");
        final String title = "Test Title";
        final SVGGraphBuilder instance = new SVGGraphBuilder();
        final SVGGraphBuilder result = instance.withTitle(title);
        assertEquals(result, instance);
    }

    /**
     * Test of withBackground method, of class SVGGraphBuilder.
     */
    @Test
    public void testWithBackground() {
        System.out.println("withBackground");
        final ConstellationColor color = ConstellationColor.BANANA;
        final SVGGraphBuilder instance = new SVGGraphBuilder();
        final SVGGraphBuilder result = instance.withBackground(color);
        assertEquals(result, instance);
    }

    /**
     * Test of withSelectedElementsOnly method, of class SVGGraphBuilder.
     */
    @Test
    public void testWithElements() {
        System.out.println("withElements");
        final Boolean selectedElementsOnly = true;
        final SVGGraphBuilder instance = new SVGGraphBuilder();
        final SVGGraphBuilder result = instance.withSelectedElementsOnly(selectedElementsOnly);
        assertEquals(result, instance);
    }

    /**
     * Test of includeNodes method, of class SVGGraphBuilder.
     */
    @Test
    public void testIncludeNodes() {
        System.out.println("includeNodes");
        final Boolean showNodes = true;
        final SVGGraphBuilder instance = new SVGGraphBuilder();
        final SVGGraphBuilder result = instance.includeNodes(showNodes);
        assertEquals(result, instance);
    }
    
    /**
     * Test of includeConnections method, of class SVGGraphBuilder.
     */
    @Test
    public void testIncludeConnections() {
        System.out.println("includeConnections");
        final Boolean showConnections = true;
        final SVGGraphBuilder instance = new SVGGraphBuilder();
        final SVGGraphBuilder result = instance.includeConnections(showConnections);
        assertEquals(result, instance);
    }

    /**
     * Test of includeNodeLabels method, of class SVGGraphBuilder.
     */
    @Test
    public void testIncludeNodeLabels() {
        System.out.println("includeNodeLabels");
        final Boolean showNodeLabels = true;
        final SVGGraphBuilder instance = new SVGGraphBuilder();
        final SVGGraphBuilder result = instance.includeNodeLabels(showNodeLabels);
        assertEquals(result, instance);
    }

    /**
     * Test of includeConnectionLabels method, of class SVGGraphBuilder.
     */
    @Test
    public void testIncludeConnectionLabels() {
        System.out.println("includeConnectionLabels");
        final Boolean showConnectionLabels = true;
        final SVGGraphBuilder instance = new SVGGraphBuilder();
        final SVGGraphBuilder result = instance.includeConnectionLabels(showConnectionLabels);
        assertEquals(result, instance);
    }

     /**
     * Test of includeBlazes method, of class SVGGraphBuilder.
     */
    @Test
    public void testIncludeBlazes() {
        System.out.println("includeBlazes");
        final Boolean showBlazes = true;
        final SVGGraphBuilder instance = new SVGGraphBuilder();
        final SVGGraphBuilder result = instance.includeBlazes(showBlazes);
        assertEquals(result, instance);
    }
    
    /**
     * Test of fromPerspective method, of class SVGGraphBuilder.
     */
    @Test
    public void testFromPerspective() {
        System.out.println("fromPerspective");
        final AxisConstants exportPerspective = AxisConstants.X_POSITIVE;
        final SVGGraphBuilder instance = new SVGGraphBuilder();
        final SVGGraphBuilder result = instance.fromPerspective(exportPerspective);
        assertEquals(result, instance);
    }

    /**
     * Test of build method, of class SVGGraphBuilder.
     */
    @Test
    public void testBuild() throws IllegalArgumentException, InterruptedException {
        System.out.println("build");
        final SVGGraphBuilder instance = new SVGGraphBuilder()
                .withInteraction(interactionMock)
                .withReadableGraph(graph.getReadableGraph())
                .withTitle(graphName)
                .fromPerspective(AxisConstants.Z_POSITIVE)
                .withSelectedElementsOnly(false)
                .includeNodes(true)
                .includeNodeLabels(true)
                .includeConnections(true)
                .includeConnectionLabels(false)
                .includeBlazes(false);
        
        final SVGObject result = new SVGObject(instance.build());
        assertNotNull(result.getChild(String.format("node-%s",vertexId2)));
    }
    
    /**
     * Test of build method, of class SVGGraphBuilder.
     * @throws java.lang.InterruptedException
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBuild_withoutReadableGraph() throws InterruptedException {
        System.out.println("build");
        final SVGGraphBuilder instance = new SVGGraphBuilder()
                .withInteraction(interactionMock)
                .withTitle(graphName)
                .fromPerspective(AxisConstants.Z_POSITIVE)
                .withSelectedElementsOnly(false)
                .includeNodes(true)
                .includeNodeLabels(true)
                .includeConnections(true)
                .includeConnectionLabels(false)
                .includeBlazes(false); 
        instance.build();
    }
    
    /**
     * Test of build method, of class SVGGraphBuilder.
     * @throws java.lang.InterruptedException
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBuild_withoutInteraction() throws InterruptedException {
        System.out.println("build");
        final SVGGraphBuilder instance = new SVGGraphBuilder()
                .withReadableGraph(graph.getReadableGraph())
                .withTitle(graphName)
                .fromPerspective(AxisConstants.Z_POSITIVE)
                .withSelectedElementsOnly(false)
                .includeNodes(true)
                .includeNodeLabels(true)
                .includeConnections(true)
                .includeConnectionLabels(false)
                .includeBlazes(false); 
        instance.build();
    }
    
    /**
     * Test of build method, of class SVGGraphBuilder.
     * @throws java.lang.InterruptedException
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBuild_withoutGraphTitle() throws InterruptedException {
        System.out.println("build");
        final SVGGraphBuilder instance = new SVGGraphBuilder()
                .withInteraction(interactionMock)
                .withReadableGraph(graph.getReadableGraph())
                .fromPerspective(AxisConstants.Z_POSITIVE)
                .withSelectedElementsOnly(false)
                .includeNodes(true)
                .includeNodeLabels(true)
                .includeConnections(true)
                .includeConnectionLabels(false)
                .includeBlazes(false); 
        instance.build();
    }
    
    private Graph buildTestableGraph() throws InterruptedException{
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        final Graph localGraph = new DualGraph(schema);

        final WritableGraph wg = localGraph.getWritableGraph("Autosave", true);
        try {
            
            GraphLabel testLabel1 = new GraphLabel("Label", ConstellationColor.CLOUDS);
            GraphLabel testLabel2 = new GraphLabel("x", ConstellationColor.BANANA);
            GraphLabel testLabel3 = new GraphLabel("color", ConstellationColor.BLUE);
            
            ArrayList testNodeLabelsList = new ArrayList<>();
            testNodeLabelsList.add(testLabel1);
            testNodeLabelsList.add(testLabel2);
            GraphLabels topLabels = new GraphLabels(testNodeLabelsList);
            GraphLabels bottomLabels = new GraphLabels(testNodeLabelsList);
            
            graphAttributeIdTopLabels = VisualConcept.GraphAttribute.TOP_LABELS.ensure(wg);
            wg.setObjectValue(graphAttributeIdTopLabels, 0, topLabels);
            ArrayList testTransactionLabelsList = new ArrayList<>();
            testTransactionLabelsList.add(testLabel1);
            testTransactionLabelsList.add(testLabel3);
            GraphLabels transactionLabels = new GraphLabels(testTransactionLabelsList);
            
            graphAttributeIdBottomLabels = VisualConcept.GraphAttribute.BOTTOM_LABELS.ensure(wg);
            wg.setObjectValue(graphAttributeIdBottomLabels, 0, bottomLabels);
            
            graphAttributeIdTransactionLabels = VisualConcept.GraphAttribute.TRANSACTION_LABELS.ensure(wg);
            wg.setObjectValue(graphAttributeIdTransactionLabels, 0, transactionLabels);
            
            vertexAttributeIdX = VisualConcept.VertexAttribute.X.ensure(wg);
            vertexAttributeIdY = VisualConcept.VertexAttribute.Y.ensure(wg);
            vertexAttributeIdZ = VisualConcept.VertexAttribute.Z.ensure(wg);
            vertexAttributeIdLabel = VisualConcept.VertexAttribute.LABEL.ensure(wg);
            VisualConcept.VertexAttribute.BLAZE.ensure(wg);
            vertexAttributeIdPinned = VisualConcept.VertexAttribute.PINNED.ensure(wg);
            vertexAttributeIdSelected = VisualConcept.VertexAttribute.SELECTED.ensure(wg);
            transactionAttributeIdSelected = VisualConcept.TransactionAttribute.SELECTED.ensure(wg);

            vertexId1 = wg.addVertex();
            wg.setFloatValue(vertexAttributeIdX, vertexId1, 1.0f);
            wg.setFloatValue(vertexAttributeIdY, vertexId1, 1.0f);
            wg.setFloatValue(vertexAttributeIdZ, vertexId1, 1.0f);
            wg.setBooleanValue(vertexAttributeIdSelected, vertexId1, false);
            wg.setStringValue(vertexAttributeIdLabel, vertexId1, "vertex1");
            
            vertexId2 = wg.addVertex();
            wg.setFloatValue(vertexAttributeIdX, vertexId2, 5.0f);
            wg.setFloatValue(vertexAttributeIdY, vertexId2, 1.0f);
            wg.setFloatValue(vertexAttributeIdZ, vertexId2, 1.0f);
            wg.setBooleanValue(vertexAttributeIdSelected, vertexId2, true);
            wg.setBooleanValue(vertexAttributeIdPinned, vertexId2, true);
            wg.setStringValue(vertexAttributeIdLabel, vertexId2, "vertex2");
            
            vertexId3 = wg.addVertex();
            wg.setFloatValue(vertexAttributeIdX, vertexId3, 1.0f);
            wg.setFloatValue(vertexAttributeIdY, vertexId3, 5.0f);
            wg.setFloatValue(vertexAttributeIdZ, vertexId3, 1.0f);
            wg.setBooleanValue(vertexAttributeIdSelected, vertexId3, false);
            wg.setStringValue(vertexAttributeIdLabel, vertexId3, "vertex3");
            
            vertexId4 = wg.addVertex();
            wg.setFloatValue(vertexAttributeIdX, vertexId4, 5.0f);
            wg.setFloatValue(vertexAttributeIdY, vertexId4, 5.0f);
            wg.setFloatValue(vertexAttributeIdZ, vertexId4, 5.0f);
            wg.setBooleanValue(vertexAttributeIdSelected, vertexId4, false);
            wg.setStringValue(vertexAttributeIdLabel, vertexId4, "vertex4");
            
            vertexId5 = wg.addVertex();
            wg.setFloatValue(vertexAttributeIdX, vertexId5, 10.0f);
            wg.setFloatValue(vertexAttributeIdY, vertexId5, 10.0f);
            wg.setFloatValue(vertexAttributeIdZ, vertexId5, 10.0f);
            wg.setBooleanValue(vertexAttributeIdSelected, vertexId5, true);
            wg.setStringValue(vertexAttributeIdLabel, vertexId5, "vertex5");
            
            vertexId6 = wg.addVertex();
            wg.setFloatValue(vertexAttributeIdX, vertexId6, 0.0f);
            wg.setFloatValue(vertexAttributeIdY, vertexId6, 0.0f);
            wg.setFloatValue(vertexAttributeIdZ, vertexId6, 15.0f);
            wg.setStringValue(vertexAttributeIdLabel, vertexId6, "vertex6");

            transactionId1 = wg.addTransaction(vertexId1, vertexId2, false);
            transactionId2 = wg.addTransaction(vertexId2, vertexId3, false);
            transactionId3 = wg.addTransaction(vertexId5, vertexId2, true);
            transactionId4 = wg.addTransaction(vertexId2, vertexId2, true);
            transactionId5 = wg.addTransaction(vertexId2, vertexId2, false);
            
        } finally {
            wg.commit();
        }
        return localGraph;
    }
    
}
