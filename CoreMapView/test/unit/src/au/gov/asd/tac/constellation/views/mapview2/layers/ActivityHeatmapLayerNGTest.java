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
package au.gov.asd.tac.constellation.views.mapview2.layers;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.views.mapview2.MapView;
import au.gov.asd.tac.constellation.views.mapview2.markers.AbstractMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.PointMarker;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author altair1673
 */
public class ActivityHeatmapLayerNGTest {

    private static final Logger LOGGER = Logger.getLogger(ActivityHeatmapLayerNGTest.class.getName());

    @BeforeClass
    public static void setUpClass() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timed out trying to cleanup stages", ex);
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of getWeight method, of class ActivityHeatmapLayer.
     */
    @Test
    public void testGetWeight() {
        System.out.println("getWeight");

        MapView mapView = Mockito.mock(MapView.class);

        final GraphManager graphManager = Mockito.mock(GraphManager.class);
        final Graph graph = mock(Graph.class);
        final ReadableGraph readableGraph = mock(ReadableGraph.class);

        int vertexCount = 1;
        int vertexID = 2;

        final List<Integer> idList = new ArrayList<>();
        idList.add(vertexID);

        try (MockedStatic<GraphManager> graphManagerMock = Mockito.mockStatic(GraphManager.class)) {
            graphManagerMock.when(GraphManager::getDefault).thenReturn(graphManager);
            Mockito.when(graphManager.getActiveGraph()).thenReturn(graph);

            Mockito.when(graph.getReadableGraph()).thenReturn(readableGraph);
            Mockito.when(readableGraph.getVertexCount()).thenReturn(vertexCount);
            Mockito.when(readableGraph.getVertex(0)).thenReturn(vertexID);
            Mockito.when(readableGraph.getVertexTransactionCount(vertexID)).thenReturn(4);

            Mockito.doNothing().when(readableGraph).release();

            AbstractMarker marker = Mockito.mock(PointMarker.class);
            Mockito.when(marker.getConnectedNodeIdList()).thenReturn(idList);

            ActivityHeatmapLayer instance = new ActivityHeatmapLayer(mapView, 0);

            int expResult = 4;
            int result = instance.getWeight(marker);

            assertEquals(result, expResult);
        }

    }

}
