/*
 * Copyright 2010-2022 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.graph.GraphConstants;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.SpatialConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.views.mapview2.MapView;
import au.gov.asd.tac.constellation.views.mapview2.markers.AbstractMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.PointMarker;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testfx.api.FxToolkit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author altair1673
 */
public class LocationPathsLayerNGTest {

    private static final Logger LOGGER = Logger.getLogger(LocationPathsLayerNGTest.class.getName());

    public LocationPathsLayerNGTest() {
    }

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
     * Test of setUp method, of class LocationPathsLayer.
     */
    @Test
    public void testSetUp() {
        System.out.println("setUp");
        final int vertexID = 56;
        final int neighbourID = 65;
        final int neighbourLinkID = 70;
        final int vertexNeighbourCount = 1;
        final int linkOutgoingTransactionCount = 1;

        final MapView parent = Mockito.mock(MapView.class);
        final GraphManager graphManager = Mockito.mock(GraphManager.class);
        final Graph graphMock = Mockito.mock(Graph.class);
        final ReadableGraph graph = Mockito.mock(ReadableGraph.class);

        final int lonID2 = 11;
        final int latID2 = 22;
        final int vertexTypeAttributeId = 33;

        //Mockito.when(parent.getCurrentGraph()).thenReturn(graphMock);
        Mockito.when(graphMock.getReadableGraph()).thenReturn(graph);

        try (MockedStatic<SpatialConcept> spaceConcept = Mockito.mockStatic(SpatialConcept.class)) {
            spaceConcept.when(() -> SpatialConcept.VertexAttribute.LONGITUDE.get(graph)).thenReturn(lonID2);
            spaceConcept.when(() -> SpatialConcept.VertexAttribute.LATITUDE.get(graph)).thenReturn(latID2);
        }

        try (MockedStatic<AnalyticConcept> analConcept = Mockito.mockStatic(AnalyticConcept.class)) {
            analConcept.when(() -> AnalyticConcept.VertexAttribute.TYPE.get(graph)).thenReturn(vertexTypeAttributeId);
        }

        final SchemaVertexType vertexType = Mockito.mock(SchemaVertexType.class);
        Mockito.when(graph.getObjectValue(vertexTypeAttributeId, vertexID)).thenReturn(vertexType);
        Mockito.when(vertexType.isSubTypeOf(AnalyticConcept.VertexType.LOCATION)).thenReturn(true);

        Mockito.when(graph.getVertexNeighbourCount(vertexID)).thenReturn(vertexNeighbourCount);
        Mockito.when(graph.getVertexNeighbour(vertexID, 0)).thenReturn(neighbourID);

        final SchemaVertexType neighbourType = Mockito.mock(SchemaVertexType.class);
        Mockito.when(graph.getObjectValue(vertexTypeAttributeId, neighbourID)).thenReturn(neighbourType);
        Mockito.when(neighbourType.isSubTypeOf(AnalyticConcept.VertexType.LOCATION)).thenReturn(true);

        Mockito.when(graph.getLink(vertexID, neighbourID)).thenReturn(neighbourLinkID);
        Mockito.when(graph.getLinkTransactionCount(neighbourLinkID, GraphConstants.UPHILL)).thenReturn(linkOutgoingTransactionCount);

        Mockito.when(graph.getObjectValue(latID2, vertexID)).thenReturn(50f);
        Mockito.when(graph.getObjectValue(lonID2, vertexID)).thenReturn(50f);

        Mockito.when(graph.getObjectValue(latID2, neighbourID)).thenReturn(70f);
        Mockito.when(graph.getObjectValue(lonID2, neighbourID)).thenReturn(70f);

        final Map<String, AbstractMarker> queriedMarkers = new HashMap<>();
        final PointMarker pMarker = new PointMarker(parent, vertexID, vertexID, 0, 0, 0.05, 0, 0, "#ffffff");
        queriedMarkers.put("5,5", pMarker);

        try (MockedStatic<GraphManager> graphManagerMock = Mockito.mockStatic(GraphManager.class)) {
            graphManagerMock.when(GraphManager::getDefault).thenReturn(graphManager);
            Mockito.when(graphManager.getActiveGraph()).thenReturn(graphMock);
            final LocationPathsLayer instance = new LocationPathsLayer(parent, 20, queriedMarkers);
            instance.setUp();

            Mockito.verify(graph).getObjectValue(latID2, vertexID);
        }
    }

}
