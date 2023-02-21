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
package au.gov.asd.tac.constellation.views.mapview2.plugins;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.SpatialConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.views.mapview2.MapView;
import au.gov.asd.tac.constellation.views.mapview2.MapViewPane;
import au.gov.asd.tac.constellation.views.mapview2.MapViewTopComponent;
import au.gov.asd.tac.constellation.views.mapview2.markers.AbstractMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.PointMarker;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mockito.Mockito;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author altair1673
 */
public class ExtractCoordsFromGraphPluginNGTest {

    private static final Logger LOGGER = Logger.getLogger(ExtractCoordsFromGraphPluginNGTest.class.getName());

    public ExtractCoordsFromGraphPluginNGTest() {
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
     * Test of read method, of class ExtractCoordsFromGraphPlugin.
     */
    @Test
    public void testRead() throws Exception {
        System.out.println("read");

        MapViewTopComponent component = Mockito.spy(MapViewTopComponent.class);
        MapViewPane mapViewPane = Mockito.mock(MapViewPane.class);
        MapView mapView = Mockito.spy(new MapView(mapViewPane));

        Mockito.doCallRealMethod().when(component).addMarker(Mockito.anyString(), Mockito.any(AbstractMarker.class));
        Mockito.when(component.getMapViewPane()).thenReturn(mapViewPane);
        Mockito.when(mapViewPane.getMap()).thenReturn(mapView);

        Mockito.doNothing().when(mapView).clearQueriedMarkers();
        Mockito.doNothing().when(mapView).parseMapSVG();
        Mockito.doCallRealMethod().when(mapView).addMarkerToHashMap(Mockito.anyString(), Mockito.any(AbstractMarker.class));
        Mockito.when(mapView.getAllMarkers()).thenCallRealMethod();

        GraphWriteMethods graph = Mockito.spy(new StoreGraph());

        int vertexCount = 1;
        int vertexID = 1;

        Mockito.doReturn(vertexCount).when(graph).getVertexCount();
        Mockito.doReturn(vertexID).when(graph).getVertex(0);
        Mockito.doCallRealMethod().when(component).getMapViewPane();

        SpatialConcept.VertexAttribute.LATITUDE.ensure(graph);
        SpatialConcept.VertexAttribute.LONGITUDE.ensure(graph);

        int lonID = SpatialConcept.VertexAttribute.LONGITUDE.get(graph);
        int latID = SpatialConcept.VertexAttribute.LATITUDE.get(graph);

        Mockito.doReturn(Float.parseFloat("100")).when(graph).getObjectValue(lonID, vertexID);
        Mockito.doReturn(Float.parseFloat("100")).when(graph).getObjectValue(latID, vertexID);
        Mockito.doReturn("#000000").when(graph).getStringValue(Mockito.anyInt(), Mockito.anyInt());

        PluginInteraction interaction = Mockito.mock(PluginInteraction.class);
        PluginParameters parameters = Mockito.mock(PluginParameters.class);

        ExtractCoordsFromGraphPlugin instance = new ExtractCoordsFromGraphPlugin(component);
        instance.read(graph, interaction, parameters);

        String exp = "100.0,100.0";

        Set<String> markerKeys = component.getMapViewPane().getMap().getAllMarkers().keySet();

        assertEquals(markerKeys.contains(exp), true);
    }

}
