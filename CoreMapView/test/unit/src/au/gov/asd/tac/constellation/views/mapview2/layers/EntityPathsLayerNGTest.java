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
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.SpatialConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.TemporalConcept;
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
public class EntityPathsLayerNGTest {

    private static final Logger LOGGER = Logger.getLogger(EntityPathsLayerNGTest.class.getName());

    public EntityPathsLayerNGTest() {
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
     * Test of setUp method, of class EntityPathsLayer.
     */
    @Test
    public void testSetUp() {
        System.out.println("setUp");

        final int vertexTypeAttributeId = 7;
        final int transDateTimeAttrId = 8;
        final int lonID2 = 9;
        final int latID2 = 10;

        final int vertexID = 56;
        final int neighbourID = 65;
        final int neighbourLinkID = 70;
        final int neighbourLinkTransID = 75;
        final int secondNeighbourID = 80;
        final int secondNeighbourLinkID = 85;
        final int neighbourCount = 1;
        final int secondNeighbourCount = 1;
        final int neighbourLinkTransactionCount = 1;
        final int secondNeighbourLinkTransactionCount = 1;
        final int secondNeighbourLinkTransactionId = 90;

        final MapView parent = Mockito.mock(MapView.class);
        final Graph graphMock = Mockito.mock(Graph.class);
        final ReadableGraph graph = Mockito.mock(ReadableGraph.class);

        final Map<String, AbstractMarker> queriedMarkers = new HashMap<>();
        final PointMarker pMarker = new PointMarker(parent, vertexID, vertexID, 0, 0, 0.05, 0, 0, "#ffffff");
        queriedMarkers.put("5,5", pMarker);

        Mockito.when(parent.getCurrentGraph()).thenReturn(graphMock);
        Mockito.when(graphMock.getReadableGraph()).thenReturn(graph);

        try (MockedStatic<AnalyticConcept> analConcept = Mockito.mockStatic(AnalyticConcept.class)) {
            analConcept.when(() -> AnalyticConcept.VertexAttribute.TYPE.get(graph)).thenReturn(vertexTypeAttributeId);
        }

        try (MockedStatic<TemporalConcept> tempConcept = Mockito.mockStatic(TemporalConcept.class)) {
            tempConcept.when(() -> TemporalConcept.TransactionAttribute.DATETIME.get(graph)).thenReturn(transDateTimeAttrId);
        }

        try (MockedStatic<SpatialConcept> spaceConcept = Mockito.mockStatic(SpatialConcept.class)) {
            spaceConcept.when(() -> SpatialConcept.VertexAttribute.LONGITUDE.get(graph)).thenReturn(lonID2);
            spaceConcept.when(() -> SpatialConcept.VertexAttribute.LATITUDE.get(graph)).thenReturn(latID2);
        }

        Mockito.when(graph.getVertex(vertexID)).thenReturn(vertexID);

        final SchemaVertexType vertexType = Mockito.mock(SchemaVertexType.class);

        Mockito.when(graph.getObjectValue(vertexTypeAttributeId, vertexID)).thenReturn(vertexType);
        Mockito.when(vertexType.isSubTypeOf(AnalyticConcept.VertexType.LOCATION)).thenReturn(true);
        Mockito.when(graph.getVertexNeighbourCount(vertexID)).thenReturn(neighbourCount);
        Mockito.when(graph.getVertexNeighbour(vertexID, 0)).thenReturn(neighbourID);

        final SchemaVertexType neighbourType = Mockito.mock(SchemaVertexType.class);

        Mockito.when(graph.getObjectValue(vertexTypeAttributeId, neighbourID)).thenReturn(neighbourType);
        Mockito.when(neighbourType.isSubTypeOf(AnalyticConcept.VertexType.LOCATION)).thenReturn(false);

        Mockito.when(graph.getLink(vertexID, neighbourID)).thenReturn(neighbourLinkID);
        Mockito.when(graph.getLinkTransactionCount(neighbourLinkID)).thenReturn(neighbourLinkTransactionCount);
        Mockito.when(graph.getLinkTransaction(neighbourLinkID, 0)).thenReturn(neighbourLinkTransID);

        Mockito.when(graph.getLongValue(transDateTimeAttrId, neighbourLinkTransID)).thenReturn(100l);
        Mockito.when(graph.getVertexNeighbourCount(neighbourID)).thenReturn(secondNeighbourCount);
        Mockito.when(graph.getVertexNeighbour(neighbourID, 0)).thenReturn(secondNeighbourID);

        final SchemaVertexType secondNeighbourType = Mockito.mock(SchemaVertexType.class);

        Mockito.when(graph.getObjectValue(vertexTypeAttributeId, secondNeighbourID)).thenReturn(secondNeighbourType);
        Mockito.when(secondNeighbourType.isSubTypeOf(AnalyticConcept.VertexType.LOCATION)).thenReturn(true);

        Mockito.when(graph.getLink(neighbourID, secondNeighbourID)).thenReturn(secondNeighbourLinkID);
        Mockito.when(graph.getLinkTransactionCount(secondNeighbourLinkID)).thenReturn(secondNeighbourLinkTransactionCount);
        Mockito.when(graph.getLinkTransaction(secondNeighbourLinkID, 0)).thenReturn(secondNeighbourLinkTransactionId);

        Mockito.when(graph.getLongValue(transDateTimeAttrId, secondNeighbourLinkTransactionId)).thenReturn(200l);

        Mockito.when(graph.getObjectValue(latID2, vertexID)).thenReturn(5f);
        Mockito.when(graph.getObjectValue(lonID2, vertexID)).thenReturn(5f);

        Mockito.when(graph.getObjectValue(latID2, secondNeighbourID)).thenReturn(10f);
        Mockito.when(graph.getObjectValue(lonID2, secondNeighbourID)).thenReturn(10f);

        final EntityPathsLayer instance = new EntityPathsLayer(parent, 6, queriedMarkers);
        instance.setUp();

        Mockito.verify(graph, Mockito.atMost(4)).getLongValue(Mockito.eq(transDateTimeAttrId), Mockito.eq(secondNeighbourLinkTransactionId));
    }

}
