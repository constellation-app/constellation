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
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.Blaze;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.ConnectionMode;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.svg.SVGObject;
import au.gov.asd.tac.constellation.utilities.threadpool.ConstellationGlobalThreadPool;
import au.gov.asd.tac.constellation.utilities.visual.AxisConstants;
import au.gov.asd.tac.constellation.utilities.visual.DrawFlags;
import au.gov.asd.tac.constellation.utilities.visual.VisualManager;
import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
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
    private static MockedStatic<ConstellationGlobalThreadPool> threadPoolStaticMock; 
    private VisualManager visualManagerMock;
    private GraphManager graphManagerMock;
    private Graph graph;
    private PluginInteraction interactionMock;
    private GraphNode contextMock;
    private ConstellationGlobalThreadPool globalThreadPoolMock;
    private Component visualComponentMock;
    private File fileMock;
    private final DrawFlags drawAllVisualElementsFlag = new DrawFlags(true, true, true, true, true);
    private final DrawFlags drawNoLabelsFlag = new DrawFlags(true, true, false, false, false);
    private final DrawFlags drawNoVisualElementsFlag = new DrawFlags(false, false, false, false, false);
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
    private int transactionId6;
    private int transactionId7;
    
    
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
        threadPoolStaticMock = mockStatic(ConstellationGlobalThreadPool.class);
        visualManagerMock = mock(VisualManager.class);
        contextMock = mock(GraphNode.class);
        graphManagerMock = mock(GraphManager.class);
        interactionMock = mock(DefaultPluginInteraction.class);
        visualComponentMock = mock(Component.class);
        globalThreadPoolMock = mock(ConstellationGlobalThreadPool.class);
        fileMock = mock(File.class);
        
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
        
        //Threadding
        threadPoolStaticMock.when(() 
                -> ConstellationGlobalThreadPool.getThreadPool())
                .thenReturn(globalThreadPoolMock);
        doReturn(false).when(fileMock).exists();
        
        doReturn(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())).when(globalThreadPoolMock).getFixedThreadPool(anyString(), anyInt());
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        graphNodeStaticMock.close();
        graphManagerStaticMock.close();
        threadPoolStaticMock.close();
    }

    /**
     * Tests the builder pattern of the SVGGraphBuilder.
     * Ensures that the of calling any "with" methods return the same instance of the Builder object
     */
    @Test
    public void testBuilderPattern_returnTypes() {
        System.out.println("withReadableGraph");
        final GraphReadMethods localGraph = GraphNode.getGraphNode(graphName).getGraph().getReadableGraph();
        final SVGGraphBuilder instance = new SVGGraphBuilder();
        
        final SVGGraphBuilder withBackgroundResult = instance.withBackground(ConstellationColor.BANANA);
        assertEquals(withBackgroundResult, instance);
        
        final SVGGraphBuilder withTitleResult = instance.withTitle("Test Title");
        assertEquals(withTitleResult, instance);
        
        final SVGGraphBuilder withInteractionResult = instance.withInteraction(interactionMock);
        assertEquals(withInteractionResult, instance);
        
        final SVGGraphBuilder result = instance.withReadableGraph(localGraph);
        assertEquals(result, instance);
        
        final SVGGraphBuilder fromPerspectiveResult = instance.fromPerspective(AxisConstants.Z_NEGATIVE);
        assertEquals(fromPerspectiveResult, instance);
        
        final SVGGraphBuilder withSelectedElementsOnlyResult = instance.withSelectedElementsOnly(true);
        assertEquals(withSelectedElementsOnlyResult, instance);
        
        final SVGGraphBuilder drawFlagsResult = instance.withDrawFlags(drawAllVisualElementsFlag);
        assertEquals(drawFlagsResult, instance);
        
        final SVGGraphBuilder atDirectoryResult = instance.atDirectory(fileMock);
        assertEquals(atDirectoryResult, instance);
        
        final SVGGraphBuilder withCoresResult = instance.withCores(2);
        assertEquals(withCoresResult, instance);
    }
        
    /**
     * Test to ensure that building a SVG graph without a {@link Graph} reference will throw an error.
     * @throws java.lang.InterruptedException
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBuilderPattern_withoutReadableGraph() throws InterruptedException {
        System.out.println("build");
        final SVGGraphBuilder instance = new SVGGraphBuilder()
                .withInteraction(interactionMock)
                .withTitle(graphName)
                .fromPerspective(AxisConstants.Z_POSITIVE)
                .withSelectedElementsOnly(false)
                .withDrawFlags(drawAllVisualElementsFlag)
                .withCores(2);
        instance.build();
    }
    
    /**
     * Test to ensure that building a SVG graph without a {@link PluginInteraction} reference will throw an error.
     * @throws java.lang.InterruptedException
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBuilderPattern_withoutInteraction() throws InterruptedException {
        System.out.println("build");
        final SVGGraphBuilder instance = new SVGGraphBuilder()
                .withReadableGraph(graph.getReadableGraph())
                .withTitle(graphName)
                .fromPerspective(AxisConstants.Z_POSITIVE)
                .withSelectedElementsOnly(false)
                .withDrawFlags(drawAllVisualElementsFlag)
                .withCores(2);
        instance.build();
    }
    
    /**
     * Test to ensure that building a SVG graph without a graph title will throw an error.
     * @throws java.lang.InterruptedException
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBuilderPattern_withoutGraphTitle() throws InterruptedException {
        System.out.println("build");
        final SVGGraphBuilder instance = new SVGGraphBuilder()
                .withInteraction(interactionMock)
                .withReadableGraph(graph.getReadableGraph())
                .fromPerspective(AxisConstants.Z_POSITIVE)
                .withSelectedElementsOnly(false)
                .withDrawFlags(drawAllVisualElementsFlag)
                .withCores(2);
        instance.build();
    }
    
    /**
     * Test of build method, of class SVGGraphBuilder.
     */
    @Test
    public void testBuildPatternOutput_allVisualElements() throws IllegalArgumentException, InterruptedException {
        System.out.println("build");
        final SVGGraphBuilder instance = new SVGGraphBuilder()
                .atDirectory(fileMock)
                .withInteraction(interactionMock)
                .withReadableGraph(graph.getReadableGraph())
                .withTitle(graphName)
                .fromPerspective(AxisConstants.Z_POSITIVE)
                .withSelectedElementsOnly(true)
                .withDrawFlags(drawAllVisualElementsFlag)
                .withCores(2);
            
        final SVGObject result = new SVGObject(instance.build());
        assertNotNull(result.getChild(String.format("node-%s",vertexId2)));
    }
    
    /**
     * Test of build method, of class SVGGraphBuilder.
     */
    @Test
    public void testBuildPatternOutput_noVisualElements() throws IllegalArgumentException, InterruptedException {
        System.out.println("build");
        final SVGGraphBuilder instance = new SVGGraphBuilder()
                .atDirectory(fileMock)
                .withInteraction(interactionMock)
                .withReadableGraph(graph.getReadableGraph())
                .withTitle(graphName)
                .fromPerspective(AxisConstants.Z_POSITIVE)
                .withSelectedElementsOnly(true)
                .withDrawFlags(this.drawNoVisualElementsFlag)
                .withCores(2);
            
        final SVGObject result = new SVGObject(instance.build());
        assertNull(result.getChild(String.format("node-%s",vertexId2)));
    }
    
        /**
     * Test of build method, of class SVGGraphBuilder.
     */
    @Test
    public void testBuildPatternOutput_noLabels() throws IllegalArgumentException, InterruptedException {
        System.out.println("build");
        final SVGGraphBuilder instance = new SVGGraphBuilder()
                .atDirectory(fileMock)
                .withInteraction(interactionMock)
                .withReadableGraph(graph.getReadableGraph())
                .withTitle(graphName)
                .fromPerspective(AxisConstants.Z_POSITIVE)
                .withSelectedElementsOnly(true)
                .withDrawFlags(this.drawNoLabelsFlag)
                .withCores(2);
            
        final SVGObject result = new SVGObject(instance.build());
        assertNotNull(result.getChild(String.format("node-%s",vertexId2)));
    }  
    
    
    /**
     * A local method to build a graph with known elements for testing.
     * @return
     * @throws InterruptedException 
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">  
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
            
            int vertexDimmedAttributeId = VisualConcept.VertexAttribute.DIMMED.ensure(wg);
            int vertexBlazeAttributeId = VisualConcept.VertexAttribute.BLAZE.ensure(wg);
            
            Blaze blaze = new Blaze(90, ConstellationColor.BANANA);

            vertexId1 = wg.addVertex();
            wg.setFloatValue(vertexAttributeIdX, vertexId1, 1.0f);
            wg.setFloatValue(vertexAttributeIdY, vertexId1, 1.0f);
            wg.setFloatValue(vertexAttributeIdZ, vertexId1, 1.0f);
            wg.setBooleanValue(vertexAttributeIdSelected, vertexId1, true);
            wg.setStringValue(vertexAttributeIdLabel, vertexId1, "vertex1");
            
            wg.setObjectValue(vertexBlazeAttributeId, vertexId1, blaze);
            
            vertexId2 = wg.addVertex();
            wg.setFloatValue(vertexAttributeIdX, vertexId2, 5.0f);
            wg.setFloatValue(vertexAttributeIdY, vertexId2, 1.0f);
            wg.setFloatValue(vertexAttributeIdZ, vertexId2, 1.0f);
            wg.setBooleanValue(vertexAttributeIdSelected, vertexId2, true);
            wg.setBooleanValue(vertexAttributeIdPinned, vertexId2, true);
            wg.setStringValue(vertexAttributeIdLabel, vertexId2, "vertex2");
            wg.setFloatValue(vertexDimmedAttributeId, vertexId2, 1F);
            
            vertexId3 = wg.addVertex();
            wg.setFloatValue(vertexAttributeIdX, vertexId3, 1.0f);
            wg.setFloatValue(vertexAttributeIdY, vertexId3, 5.0f);
            wg.setFloatValue(vertexAttributeIdZ, vertexId3, 1.0f);
            wg.setBooleanValue(vertexAttributeIdSelected, vertexId3, true);
            wg.setStringValue(vertexAttributeIdLabel, vertexId3, "vertex3");
            
            vertexId4 = wg.addVertex();
            wg.setFloatValue(vertexAttributeIdX, vertexId4, 5.0f);
            wg.setFloatValue(vertexAttributeIdY, vertexId4, 5.0f);
            wg.setFloatValue(vertexAttributeIdZ, vertexId4, 5.0f);
            wg.setBooleanValue(vertexAttributeIdSelected, vertexId4, true);
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
            wg.setBooleanValue(vertexAttributeIdSelected, vertexId6, false);

            transactionId1 = wg.addTransaction(vertexId1, vertexId2, false);
            transactionId2 = wg.addTransaction(vertexId2, vertexId3, false);
            transactionId3 = wg.addTransaction(vertexId5, vertexId2, true);
            transactionId4 = wg.addTransaction(vertexId2, vertexId2, true);
            transactionId5 = wg.addTransaction(vertexId2, vertexId2, false);
            transactionId6 = wg.addTransaction(vertexId1, vertexId2, true);
            transactionId7 = wg.addTransaction(vertexId2, vertexId1, true);
            wg.setObjectValue(VisualConcept.GraphAttribute.CONNECTION_MODE.ensure(wg), 0, ConnectionMode.TRANSACTION);
        } finally {
            wg.commit();
        }
        return localGraph;
    }
    // </editor-fold>  
    
}
