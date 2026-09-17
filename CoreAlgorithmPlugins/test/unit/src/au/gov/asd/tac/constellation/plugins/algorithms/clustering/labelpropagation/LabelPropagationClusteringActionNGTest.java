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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.labelpropagation;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.ClusterUtilities;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.labelpropagation.LabelPropagationClusteringAction.LabelPropagationClusteringCleanupPlugin;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import java.awt.event.ActionEvent;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 *
 * @author andromeda-224
 */
public class LabelPropagationClusteringActionNGTest {

    @Test
    public void testLabelPropagationClusteringCleanupPlugin() throws Exception {
        System.out.println("testLabelPropagationClusteringCleanupPlugin");
        final GraphWriteMethods mockGraph = mock(GraphWriteMethods.class);
        final int link = 101;

        when(mockGraph.getVertexCount()).thenReturn(1);
        when(mockGraph.getLinkCount()).thenReturn(1);
        when(mockGraph.getVertexCapacity()).thenReturn(1);
        when(mockGraph.getLinkCount()).thenReturn(1);
        when(mockGraph.getVertexLink(0, 0)).thenReturn(link);
        when(mockGraph.getVertexLinkCount(0)).thenReturn(1);
        when(mockGraph.getLinkTransactionCount(link)).thenReturn(1);
        PluginParameters parametersMock = mock(PluginParameters.class);
        PluginInteraction pluginInteractionMock = mock(PluginInteraction.class);

        final LabelPropagationClusteringCleanupPlugin instance = new LabelPropagationClusteringCleanupPlugin();
        try (MockedStatic<ClusterUtilities> clusterUtilitiesMockStatic = Mockito.mockStatic(ClusterUtilities.class)) {
            instance.edit(mockGraph, pluginInteractionMock, parametersMock);
            clusterUtilitiesMockStatic.verify(() -> ClusterUtilities.colorClusters(Mockito.any(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt()), Mockito.times(1));
            clusterUtilitiesMockStatic.verify(() -> ClusterUtilities.explodeGraph(Mockito.any(), Mockito.anyInt()), Mockito.times(1));
            instance.edit(mockGraph, pluginInteractionMock, parametersMock);
            clusterUtilitiesMockStatic.verify(() -> ClusterUtilities.colorClusters(Mockito.any(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt()), Mockito.times(2));
            clusterUtilitiesMockStatic.verify(() -> ClusterUtilities.explodeGraph(Mockito.any(), Mockito.anyInt()), Mockito.times(2));
        } 
    }

    @Test
    public void testLabelPropagationClusteringActionPerformed() throws Exception {
        System.out.println("testLabelPropagationClusteringActionPerformed");

        try (MockedConstruction<LabelPropagationClusteringCleanupPlugin> mockConstruction = Mockito.mockConstruction(LabelPropagationClusteringCleanupPlugin.class)) {
            final GraphNode mockContext = mock(GraphNode.class);
            final LabelPropagationClusteringAction instance = new LabelPropagationClusteringAction(mockContext);
            final ActionEvent mockEvent = mock(ActionEvent.class);
            
            instance.actionPerformed(mockEvent);            
            // Verify that the constructor was called exactly once
            assertEquals(1, mockConstruction.constructed().size());
            verify(mockContext, times(1)).getGraph();
        }
    }
}
