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
package au.gov.asd.tac.constellation.views.timeline;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class GraphManagerNGTest {

    /**
     * Test of selectAllInRange method, of class GraphManager.
     */
    @Test
    public void testSelectAllInRange() {
        System.out.println("selectAllInRange");
        long lowerTimeExtent = 0L;
        long upperTimeExtent = 0L;
        boolean isCtrlDown = false;
        boolean isDragSelection = false;
        boolean selectedOnly = false;

        final GraphNode mockGraphNode = mock(GraphNode.class);
        final Graph mockGraph = mock(Graph.class);
        final ReadableGraph mockReadableGraph = mock(ReadableGraph.class);
        when(mockGraphNode.getGraph()).thenReturn(mockGraph);
        when(mockGraph.getReadableGraph()).thenReturn(mockReadableGraph);

        final TopComponent.Registry mockReg = mock(TopComponent.Registry.class);
        final Node[] nodes = {mockGraphNode};
        when(mockReg.getActivatedNodes()).thenReturn(nodes);

        try (MockedStatic<TopComponent> mockTopComponent = Mockito.mockStatic(TopComponent.class)) {
            mockTopComponent.when(TopComponent::getRegistry).thenReturn(mockReg);
            assertThat(TopComponent.getRegistry()).isEqualTo(mockReg);

            final GraphManager instance = GraphManager.getDefault();
            assertEquals(instance.getClass(), GraphManager.class);

            instance.setDatetimeAttr("");
            instance.resultChanged(null);

            instance.selectAllInRange(lowerTimeExtent, upperTimeExtent, isCtrlDown, isDragSelection, selectedOnly);

            mockTopComponent.verify(TopComponent::getRegistry, times(2));
            verify(mockReg).getActivatedNodes();
            verify(mockGraphNode, times(2)).getGraph();
            verify(mockGraph).getReadableGraph();
        }
    }

    /**
     * Test of getVertexAttributeNames method, of class GraphManager.
     */
    @Test
    public void testGetVertexAttributeNames() {
        System.out.println("getVertexAttributeNames");
        final List expResult = new ArrayList<>();

        final GraphNode mockGraphNode = mock(GraphNode.class);
        final Graph mockGraph = mock(Graph.class);
        final ReadableGraph mockReadableGraph = mock(ReadableGraph.class);
        when(mockGraphNode.getGraph()).thenReturn(mockGraph);
        when(mockGraph.getReadableGraph()).thenReturn(mockReadableGraph);

        final TopComponent.Registry mockReg = mock(TopComponent.Registry.class);
        final Node[] nodes = {mockGraphNode};
        when(mockReg.getActivatedNodes()).thenReturn(nodes);

        try (MockedStatic<TopComponent> mockTopComponent = Mockito.mockStatic(TopComponent.class)) {
            mockTopComponent.when(TopComponent::getRegistry).thenReturn(mockReg);
            assertThat(TopComponent.getRegistry()).isEqualTo(mockReg);

            final GraphManager instance = GraphManager.getDefault();
            assertEquals(instance.getClass(), GraphManager.class);

            instance.setDatetimeAttr("");
            instance.resultChanged(null);

            final List result = instance.getVertexAttributeNames();
            assertEquals(result, expResult);

            mockTopComponent.verify(TopComponent::getRegistry, times(3));
            verify(mockReg).getActivatedNodes();
            verify(mockGraphNode, times(1)).getGraph();
            verify(mockGraph).getReadableGraph();
        }
    }
}
