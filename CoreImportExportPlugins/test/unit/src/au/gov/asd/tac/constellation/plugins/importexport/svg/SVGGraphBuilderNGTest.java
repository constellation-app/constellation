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
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.node.plugins.DefaultPluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.svg.SVGData;
import au.gov.asd.tac.constellation.utilities.visual.AxisConstants;
import au.gov.asd.tac.constellation.utilities.visual.VisualManager;
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
    MockedStatic<GraphManager> graphManagerStaticMock;
    MockedStatic<GraphNode> graphNodeStaticMock;
    ReadableGraph readableGraphMock;
    VisualManager visualManagerMock;
    GraphManager graphManagerMock;
    Graph graphMock;
    PluginInteraction interactionMock;
    
    GraphNode contextMock;
    
    final String graphName = "Test Graph 1";
    
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
        graphNodeStaticMock = mockStatic(GraphNode.class);
        graphManagerStaticMock = mockStatic(GraphManager.class);
        readableGraphMock = mock(ReadableGraph.class);
        visualManagerMock = mock(VisualManager.class);
        graphMock = mock(Graph.class);
        contextMock = mock(GraphNode.class);
        graphManagerMock = mock(GraphManager.class);
        interactionMock = mock(DefaultPluginInteraction.class);
        
        //Getting Graph from GraphNode
        doReturn(graphName).when(contextMock).getDisplayName();
        doReturn(graphName).when(readableGraphMock).getId();
        graphNodeStaticMock.when(() 
                -> GraphNode.getGraphNode(graphName))
                .thenReturn(contextMock);
       
        doReturn(graphMock).when(contextMock).getGraph();
        doReturn(readableGraphMock).when(graphMock).getReadableGraph();
        
        doReturn(graphMock).when(graphManagerMock).getActiveGraph(); 
        
        //Geting VisualManager from GraphNode 
        graphManagerStaticMock.when(() 
                -> GraphManager.getDefault())
                .thenReturn(graphManagerMock);
        graphNodeStaticMock.when(() 
                -> GraphNode.getGraphNode(graphMock))
                .thenReturn(contextMock);
        doReturn(visualManagerMock).when(contextMock).getVisualManager();
        
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
     * Test of withNodes method, of class SVGGraphBuilder.
     */
    @Test
    public void testWithNodes() {
        System.out.println("withNodes");
        Boolean selectedNodesOnly = true;
        SVGGraphBuilder instance = new SVGGraphBuilder();
        SVGGraphBuilder result = instance.withNodes(selectedNodesOnly);
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
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
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
        SVGGraphBuilder expResult = null;
        SVGGraphBuilder result = instance.fromPerspective(exportPerspective);
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of build method, of class SVGGraphBuilder.
     */
    @Test
    public void testBuild() throws Exception {
        System.out.println("build");
        SVGGraphBuilder instance = new SVGGraphBuilder();
        SVGData expResult = null;
        SVGData result = instance.build();
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
