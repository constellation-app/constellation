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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.hierarchical;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.ClusteringConcept;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.hierarchical.FastNewman.Group;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyInt;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openide.nodes.Node;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.openide.windows.TopComponent;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class HierarchicalControllerTopComponentNGTest {

    /**
     * Test of constructor method, of class HierarchicalControllerTopComponent.
     */
    @Test
    public void testConstructor() {
        System.out.println("testConstructor");
        final HierarchicalControllerTopComponent instance = new HierarchicalControllerTopComponent();
        assertEquals(instance.getClass(), HierarchicalControllerTopComponent.class);
    }

    @Test
    public void testUpdateEdit() throws InterruptedException {
        System.out.println("testUpdateEdit");
        
        final HierarchicalState state = new HierarchicalState();
        final Group g = new Group();
        final Group[] groups = {g};
        final int link = 101;
        final int lowVertex = 1001;

        state.setGroups(groups);
        state.setExcludeSingleVertices(true);

        final GraphWriteMethods mockGraph = mock(GraphWriteMethods.class);
        when(mockGraph.getVertexCount()).thenReturn(1);
        when(mockGraph.getLinkCount()).thenReturn(1);
        when(mockGraph.getLink(0)).thenReturn(link);
        when(mockGraph.getLinkLowVertex(link)).thenReturn(lowVertex);
        when(mockGraph.getBooleanValue(0, lowVertex)).thenReturn(true);
        when(mockGraph.getLinkTransactionCount(link)).thenReturn(1);

        final HierarchicalControllerTopComponent.Update instance = new HierarchicalControllerTopComponent.Update(state);
        assertEquals(instance.getClass(), HierarchicalControllerTopComponent.Update.class);

        assertEquals(state.getRedrawCount(), 0);
        instance.edit(mockGraph, null, null);
        assertEquals(state.getRedrawCount(), 1);
        verify(mockGraph, times(2)).setBooleanValue(anyInt(), anyInt(), anyBoolean());
        verify(mockGraph, times(2)).setFloatValue(anyInt(), anyInt(), anyFloat());
    }

    @Test
    public void testGraphChanged() throws InterruptedException {
        System.out.println("testGraphChanged");
        
        final TopComponent.Registry mockReg = mock(TopComponent.Registry.class);
        final GraphNode mockGraphNode = mock(GraphNode.class);
        final Node[] nodes = {mockGraphNode};
        final Graph mockGraph = mock(Graph.class);
        final ReadableGraph mockReadableGraph = mock(ReadableGraph.class);
        final int stateAttr = ClusteringConcept.MetaAttribute.HIERARCHICAL_CLUSTERING_STATE.get(mockReadableGraph);
        final HierarchicalState state = new HierarchicalState();
        final FastNewman.Group[] groups = {};

        when(mockReg.getActivatedNodes()).thenReturn(nodes);
        when(mockGraphNode.getGraph()).thenReturn(mockGraph);
        when(mockGraph.getReadableGraph()).thenReturn(mockReadableGraph);
        when(mockReadableGraph.getObjectValue(stateAttr, 0)).thenReturn(state);
        state.setGroups(groups);

        try (MockedStatic<TopComponent> mockTopComponent = Mockito.mockStatic(TopComponent.class)) {
            mockTopComponent.when(TopComponent::getRegistry).thenReturn(mockReg);
            assertThat(TopComponent.getRegistry()).isEqualTo(mockReg);

            final HierarchicalControllerTopComponent instance = new HierarchicalControllerTopComponent();
            assertEquals(instance.getClass(), HierarchicalControllerTopComponent.class);

            instance.resultChanged(null);
            mockTopComponent.verify(TopComponent::getRegistry, times(2));
            verify(mockReg).getActivatedNodes();
            verify(mockGraphNode).getGraph();
            verify(mockGraph).getReadableGraph();
            verify(mockReadableGraph).getObjectValue(stateAttr, 0);
        }

    }
}
