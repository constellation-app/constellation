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
import static org.mockito.ArgumentMatchers.anyInt;
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
public class LabelPropagationClusteringNGTest {
    
    @Test
    public void testCluster() {
        System.out.println("testCluster");
        final GraphWriteMethods mockGraph = mock(GraphWriteMethods.class);
        final int link = 101;
        
        when(mockGraph.getVertexCount()).thenReturn(1);
        when(mockGraph.getLinkCount()).thenReturn(1);
        when(mockGraph.getVertexCapacity()).thenReturn(1);
        when(mockGraph.getLinkCount()).thenReturn(1);
        when(mockGraph.getVertexLink(0, 0)).thenReturn(link);
        when(mockGraph.getVertexLinkCount(0)).thenReturn(1);
        when(mockGraph.getLinkTransactionCount(link)).thenReturn(1);

        final LabelPropagationClustering instance = new LabelPropagationClustering(mockGraph);
        instance.cluster();
        assertEquals(instance.getCluster(0), 0);
        verify(mockGraph, times(3)).getVertex(anyInt());
        verify(mockGraph, times(1)).setIntValue(anyInt(), anyInt(), anyInt());
    }
}
