/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.utilities.statusline;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphIndexResult;
import au.gov.asd.tac.constellation.graph.GraphIndexType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedConstruction;
import org.openide.util.Utilities;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.windows.TopComponent;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 *
 * @author andromeda-224
 */
public class GraphStatusLineNGTest {

    @Test
    public void testInstantiation() {

        try (
                final MockedStatic<SwingUtilities> swingUtilsMockedStatic = Mockito.mockStatic(SwingUtilities.class);
                final MockedConstruction<JButton> mockedButton = mockConstruction(JButton.class); 
                final MockedConstruction<JPanel> mockedPanel = mockConstruction(JPanel.class); 
                final MockedStatic<Utilities> utilitiesMockedStatic = Mockito.mockStatic(Utilities.class); 
                final MockedStatic<TopComponent> topComponentMockedStatic = Mockito.mockStatic(TopComponent.class);) {

            final UserInterfaceIconProvider userInterfaceIconProvider = mock(UserInterfaceIconProvider.class);
            final Lookup.Result<GraphNode> mockResult = mock(Result.class);
            final Lookup mockLookup = mock(Lookup.class);
            when(mockLookup.lookupResult(GraphNode.class)).thenReturn(mockResult);
            utilitiesMockedStatic.when(Utilities::actionsGlobalContext).thenReturn(mockLookup);

            final TopComponent.Registry registry = mock(TopComponent.Registry.class);
            final GraphNode mockGraphNode = mock(GraphNode.class);
            final Graph mockGraph = mock(Graph.class);
            final ReadableGraph mockRg = mock(ReadableGraph.class);
            final Node[] mockNodes = {mockGraphNode};

            doNothing().when(mockGraph).addGraphChangeListener(Mockito.any());
            when(mockGraphNode.getGraph()).thenReturn(mockGraph);
            when(mockGraph.getReadableGraph()).thenReturn(mockRg);

            when(registry.getActivatedNodes()).thenReturn(mockNodes);
            topComponentMockedStatic.when(() -> TopComponent.getRegistry()).thenReturn(registry);

            final JButton buttonMock = mock(JButton.class);
            final Insets mockInset = mock(Insets.class);
            when(buttonMock.getMargin()).thenReturn(mockInset);

            final GraphIndexResult mockGraphIndexResult = mock(GraphIndexResult.class);
            when(mockGraphIndexResult.getCount()).thenReturn(5);
            when(mockRg.getElementsWithAttributeValue(Mockito.anyInt(), Mockito.anyBoolean())).thenReturn(mockGraphIndexResult);
            when(mockRg.getAttribute(Mockito.any(), Mockito.anyString())).thenReturn(0);
            when(mockRg.getAttributeIndexType(0)).thenReturn(GraphIndexType.NONE);

            when(mockRg.getVertexCount()).thenReturn(1);
            when(mockRg.getBooleanValue(Mockito.anyInt(), Mockito.anyInt())).thenReturn(true);

            when(mockRg.getEdgeCount()).thenReturn(2);
            when(mockRg.getLinkCount()).thenReturn(1);
            when(mockRg.getLinkEdgeCount(0)).thenReturn(1);
            when(mockRg.getTransactionCount()).thenReturn(1);
            when(mockRg.getEdgeTransactionCount(0)).thenReturn(1);

            // test for graphChanged which is invoked later
            swingUtilsMockedStatic.when(() -> SwingUtilities.invokeLater(any(Runnable.class)))
                    .thenAnswer(invocation -> {
                        final Runnable runnable = invocation.getArgument(0);

                        runnable.run();

                        verify(mockGraph, times(1)).getReadableGraph();
                        verify(mockRg, times(3)).getVertexCount(); // including the second for loop
                        verify(mockRg, times(1)).getTransactionCount();
                        verify(mockRg, times(2)).getEdgeTransactionCount(Mockito.anyInt());
                        verify(mockRg, times(3)).getLinkCount();
                        verify(mockRg, times(2)).getLinkEdgeCount(Mockito.anyInt());

                        return null;
                    });

            // Instantiate the class
            final GraphStatusLine instance = spy(new GraphStatusLine());

            // verify calls in resultsChanged
            verify(mockGraphNode, times(1)).getGraph();
            verify(mockGraph, times(1)).addGraphChangeListener(Mockito.any());
            assertEquals(instance.getGraph(), mockGraph);
        }
    }

}
