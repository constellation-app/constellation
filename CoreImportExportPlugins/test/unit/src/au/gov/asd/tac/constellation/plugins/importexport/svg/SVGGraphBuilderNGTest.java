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
package au.gov.asd.tac.constellation.plugins.importexport.svg;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.node.plugins.DefaultPluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.testing.construction.TestableGraphBuilder;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.svg.SVGObject;
import au.gov.asd.tac.constellation.utilities.threadpool.ConstellationGlobalThreadPool;
import au.gov.asd.tac.constellation.utilities.visual.AxisConstants;
import au.gov.asd.tac.constellation.utilities.visual.DrawFlags;
import au.gov.asd.tac.constellation.utilities.visual.VisualManager;
import java.awt.Component;
import java.io.File;
import java.util.Arrays;
import java.util.List;
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
    private File nonExistantFileMock;
    private File existantFileMock;
    private final DrawFlags drawAllVisualElementsFlag = new DrawFlags(true, true, true, true, false);
    private final DrawFlags drawNoLabelsFlag = new DrawFlags(true, true, false, false, false);
    private final DrawFlags drawNoVisualElementsFlag = new DrawFlags(false, false, false, false, false);
    private final String graphName = "Test Graph 1";
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
        
        graph = new TestableGraphBuilder().withNodes().withAllTransactions().withDecorators().withBottomLabels().withTopLabels().build();
        graphNodeStaticMock = mockStatic(GraphNode.class);
        graphManagerStaticMock = mockStatic(GraphManager.class);
        threadPoolStaticMock = mockStatic(ConstellationGlobalThreadPool.class);
        visualManagerMock = mock(VisualManager.class);
        contextMock = mock(GraphNode.class);
        graphManagerMock = mock(GraphManager.class);
        interactionMock = mock(DefaultPluginInteraction.class);
        visualComponentMock = mock(Component.class);
        globalThreadPoolMock = mock(ConstellationGlobalThreadPool.class);
        nonExistantFileMock = mock(File.class);
        existantFileMock = mock(File.class);
        
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
        doReturn(false).when(nonExistantFileMock).exists();
        doReturn(true).when(existantFileMock).exists();
        
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
        
        final SVGGraphBuilder atDirectoryResult = instance.atDirectory(nonExistantFileMock);
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
                .atDirectory(nonExistantFileMock)
                .withInteraction(interactionMock)
                .withReadableGraph(graph.getReadableGraph())
                .withTitle(graphName)
                .fromPerspective(AxisConstants.Z_POSITIVE)
                .withSelectedElementsOnly(false)
                .withDrawFlags(drawAllVisualElementsFlag)
                .withCores(2);
            
        final SVGObject result = new SVGObject(instance.build());
//        assertNotNull(result.getChild(String.format("node-%s", 0)));
//        StringBuilder sb = new StringBuilder();
//        List<String> lines = result.toSVGData().toLines();
//        for (String line : lines){
//            sb.append(line);
//        }
//        assertEquals("", sb.toString());
        assertNotNull(result.getChild(String.format("%s", 0)));
        assertNotNull(result.getChild(String.format("node-%s", 0)));
    }
    
    /**
     * Test of build method, of class SVGGraphBuilder.
     */
    @Test
    public void testBuildPatternOutput_linkedExport() throws IllegalArgumentException, InterruptedException {
        System.out.println("build");
        final SVGGraphBuilder instance = new SVGGraphBuilder()
                .atDirectory(existantFileMock)
                .withInteraction(interactionMock)
                .withReadableGraph(graph.getReadableGraph())
                .withTitle(graphName)
                .fromPerspective(AxisConstants.Z_POSITIVE)
                .withSelectedElementsOnly(false)
                .withDrawFlags(drawAllVisualElementsFlag)
                .withCores(2);
            
        final SVGObject result = new SVGObject(instance.build());
        assertNotNull(result.getChild(String.format("%s", 0)));
        assertNotNull(result.getChild(String.format("node-%s", 0)));
    }
    
    /**
     * Test of build method, of class SVGGraphBuilder.
     */
    @Test
    public void testBuildPatternOutput_SelectedElementsOnly() throws IllegalArgumentException, InterruptedException {
        System.out.println("build");
        final SVGGraphBuilder instance = new SVGGraphBuilder()
                .atDirectory(nonExistantFileMock)
                .withInteraction(interactionMock)
                .withReadableGraph(graph.getReadableGraph())
                .withTitle(graphName)
                .fromPerspective(AxisConstants.Z_POSITIVE)
                .withSelectedElementsOnly(true)
                .withDrawFlags(drawAllVisualElementsFlag)
                .withCores(2);
            
        final SVGObject result = new SVGObject(instance.build());

        for (int id : TestableGraphBuilder.getNodeIds()) {

            if (Arrays.stream(TestableGraphBuilder.getSelectedNodeIds()).anyMatch(i -> i == id)){
                assertNotNull(result.getChild(String.format("node-%s", id)));
            } else {
                assertNull(result.getChild(String.format("node-%s", id)));
            }
        }
    }
    
    /**
     * Test of build method, of class SVGGraphBuilder.
     */
    @Test
    public void testBuildPatternOutput_noVisualElements() throws IllegalArgumentException, InterruptedException {
        System.out.println("build");
        final SVGGraphBuilder instance = new SVGGraphBuilder()
                .atDirectory(nonExistantFileMock)
                .withInteraction(interactionMock)
                .withReadableGraph(graph.getReadableGraph())
                .withTitle(graphName)
                .fromPerspective(AxisConstants.Z_POSITIVE)
                .withSelectedElementsOnly(true)
                .withDrawFlags(this.drawNoVisualElementsFlag)
                .withCores(2);
            
        final SVGObject result = new SVGObject(instance.build());
        assertNull(result.getChild(String.format("%s", 0)));
        assertNull(result.getChild(String.format("node-%s", 2)));
    }
    
        /**
     * Test of build method, of class SVGGraphBuilder.
     */
    @Test
    public void testBuildPatternOutput_noLabels() throws IllegalArgumentException, InterruptedException {
        System.out.println("build");
        final SVGGraphBuilder instance = new SVGGraphBuilder()
                .atDirectory(nonExistantFileMock)
                .withInteraction(interactionMock)
                .withReadableGraph(graph.getReadableGraph())
                .withTitle(graphName)
                .fromPerspective(AxisConstants.Z_POSITIVE)
                .withSelectedElementsOnly(false)
                .withDrawFlags(this.drawNoLabelsFlag)
                .withCores(2);
            
        final SVGObject result = new SVGObject(instance.build());
        assertNotNull(result.getChild(String.format("%s", 0)));
        assertNotNull(result.getChild(String.format("node-%s", 0)));
    }  
}
