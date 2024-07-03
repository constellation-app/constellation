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

import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import static au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept.GraphAttribute;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.importexport.ImportExportPluginRegistry;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import au.gov.asd.tac.constellation.utilities.visual.DrawFlags;
import java.awt.event.ActionEvent;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify; 
import org.openide.NotifyDescriptor;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author capricornunicorn123
 */
public class ExportToSVGActionNGTest {
    
    private static MockedStatic<PluginExecution> pluginExecutionStaticMock;
    private static MockedStatic<GraphNode> graphNodeStaticMock;
    private static MockedStatic<NotifyDisplayer> notifyDisplayerStaticMock;
    private static PluginExecution pluginExecutionMock;
    private static GraphNode contextMock;
    
    private static Graph graphMock;
    private static ReadableGraph readableGraphMock;
    
    final int drawFlagsID = 1;
    final int backgroundColorID = 2;
    final String graphName = "Test Graph 1";
    final DrawFlags drawFlags = new DrawFlags(true, true, true, true, true); 
    
    public ExportToSVGActionNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        pluginExecutionStaticMock = mockStatic(PluginExecution.class);
        graphNodeStaticMock = mockStatic(GraphNode.class);
        notifyDisplayerStaticMock = mockStatic(NotifyDisplayer.class);
        pluginExecutionMock = mock(PluginExecution.class);
        contextMock = mock(GraphNode.class);
        
        graphMock = mock(Graph.class);
        readableGraphMock = mock(ReadableGraph.class);
        
          
        pluginExecutionStaticMock.when(()
                -> PluginExecution.withPlugin(ImportExportPluginRegistry.EXPORT_SVG))
                .thenReturn(pluginExecutionMock);

        doReturn(pluginExecutionMock).when(pluginExecutionMock).withParameter(any(String.class), any(String.class));
        doReturn(pluginExecutionMock).when(pluginExecutionMock).withParameter(any(String.class), anyBoolean());
        doReturn(pluginExecutionMock).when(pluginExecutionMock).withParameter(any(String.class), any(ConstellationColor.class));
        doReturn(pluginExecutionMock).when(pluginExecutionMock).interactively(anyBoolean(), any(String.class));
        doReturn(graphMock).when(contextMock).getGraph();
        doReturn(readableGraphMock).when(graphMock).getReadableGraph();
        
        graphNodeStaticMock.when(() 
                -> GraphNode.getGraphNode(graphName))
                .thenReturn(contextMock);
        
        doReturn(graphName).when(contextMock).getDisplayName();
        doReturn(graphName).when(readableGraphMock).getId();
        
        doReturn(drawFlagsID).when((GraphReadMethods) readableGraphMock).getAttribute(GraphAttribute.DRAW_FLAGS.getElementType(),GraphAttribute.DRAW_FLAGS.getName());
        doReturn(backgroundColorID).when(readableGraphMock).getAttribute(GraphAttribute.BACKGROUND_COLOR.getElementType(),GraphAttribute.BACKGROUND_COLOR.getName());
        
        doReturn(drawFlags).when(readableGraphMock).getObjectValue(drawFlagsID, 0);
        doReturn(ConstellationColor.BLUE).when(readableGraphMock).getObjectValue(backgroundColorID, 0);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        graphNodeStaticMock.close();
        pluginExecutionStaticMock.close();
        notifyDisplayerStaticMock.close();
    }

    /**
     * Test of actionPerformed method, of class ExportToSVGAction.
     */
    @Test
    public void testActionPerformedWithEmptyGraph() {
        System.out.println("testActionPerformed");

        final ExportToSVGAction instance = new ExportToSVGAction(contextMock);
        final ActionEvent e = null;

        doReturn(0).when(readableGraphMock).getVertexCount();

        instance.actionPerformed(e);
        notifyDisplayerStaticMock.verify(() -> NotifyDisplayer.display(any(NotifyDescriptor.class)), times(1));
    }
    
    /**
     * Test of actionPerformed method, of class ExportToSVGAction.
     */
    @Test
    public void testActionPerformedWithFullGraph() {
        System.out.println("testActionPerformed");

        final ExportToSVGAction instance = new ExportToSVGAction(contextMock);
        final ActionEvent e = null;

        doReturn(6000).when(readableGraphMock).getVertexCount();

        instance.actionPerformed(e);
        
        verify(pluginExecutionMock, times(1)).withParameter(ExportToSVGPlugin.SELECTED_ELEMENTS_PARAMETER_ID, false);
        verify(pluginExecutionMock, times(1)).withParameter(ExportToSVGPlugin.SHOW_NODE_LABELS_PARAMETER_ID, true);
        verify(pluginExecutionMock, times(1)).withParameter(ExportToSVGPlugin.SHOW_CONNECTION_LABELS_PARAMETER_ID, true);
        verify(pluginExecutionMock, times(1)).withParameter(ExportToSVGPlugin.SHOW_CONNECTIONS_PARAMETER_ID, true);
        verify(pluginExecutionMock, times(1)).executeLater(graphMock);
        verify(contextMock, times(2)).getGraph();
        
    }
    
}
