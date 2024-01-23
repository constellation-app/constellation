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
package au.gov.asd.tac.constellation.views.mapview2;

import au.gov.asd.tac.constellation.utilities.file.ConstellationInstalledFileLocator;
import au.gov.asd.tac.constellation.views.mapview2.layers.AbstractMapLayer;
import au.gov.asd.tac.constellation.views.mapview2.layers.DayNightLayer;
import au.gov.asd.tac.constellation.views.mapview2.markers.AbstractMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.PointMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.UserPointMarker;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import javafx.beans.property.StringProperty;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.logging.Logger;
import org.mockito.Mockito;

/**
 *
 * @author altair1673
 */
public class MapViewNGTest {

    private static final Logger LOGGER = Logger.getLogger(MapViewNGTest.class.getName());

    private static final MapDetails mapDetails = new MapDetails(MapDetails.MapType.SVG, 1000, 999, 85.0511, -85.0511, -180, 180, "Full World (default)",
                           ConstellationInstalledFileLocator.locate("modules/ext/data/WorldMap1000x999.svg", "au.gov.asd.tac.constellation.views.mapview", MapView.class.getProtectionDomain()));
    
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
     * Test of deselectAllMarkers method, of class MapView.
     */
    @Test
    public void testDeselectAllMarkers() {
        System.out.println("deselectAllMarkers");
        final MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        final MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        final MapView instance = Mockito.spy(new MapView(mapViewPane, mapDetails));

        final String coordinateKey = "-1" + "," + "-2";
        final String coordinateKey2 = "-3" + "," + "-4";

        final PointMarker p1 = new PointMarker(instance, -99, 0, (double) -1, (double) -2, 0.05, 0, 0, "#000000");
        final PointMarker p2 = new PointMarker(instance, -100, 1, (double) -3, (double) -4, 0.05, 0, 0, "#000000");

        instance.addMarkerToHashMap(coordinateKey, p1);
        instance.addMarkerToHashMap(coordinateKey2, p2);

        p1.select();
        p2.select();

        assertEquals(p1.isSelected() && p2.isSelected(), true);

        instance.deselectAllMarkers();

        assertEquals(!p1.isSelected() && !p2.isSelected(), true);
    }

    /**
     * Test of getMarkerColourProperty method, of class MapView.
     */
    @Test
    public void testGetMarkerColourProperty() {
        System.out.println("getMarkerColourProperty");
        final MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        final MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        final MapView instance = Mockito.spy(new MapView(mapViewPane, mapDetails));

        final String expResult = mapViewPane.DEFAULT_COLOURS;
        final StringProperty result = instance.getMarkerColourProperty();


        assertEquals(result.get(), expResult);
    }

    /**
     * Test of removeUserMarker method, of class MapView.
     */
    @Test
    public void testRemoveUserMarker() {
        System.out.println("removeUserMarker");
        final MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        final MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        final MapView instance = Mockito.spy(new MapView(mapViewPane, mapDetails));

        final int id = 1;
        instance.getUserCreatedMarkers().clear();
        UserPointMarker usm = new UserPointMarker(instance, id, 4, 4, 0.05, 4, 4);

        instance.getUserCreatedMarkers().add(usm);

        instance.removeUserMarker(id);

        assertEquals(instance.getUserCreatedMarkers().isEmpty(), true);
    }

    /**
     * Test of toggleOverlay method, of class MapView.
     */
    @Test
    public void testToggleOverlay() {
        System.out.println("toggleOverlay");

        final MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        final MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        final MapView instance = Mockito.spy(new MapView(mapViewPane, mapDetails));

        final String overlay = "Info Overlay";

        instance.toggleOverlay(overlay, true);

        assertEquals(instance.getInfoOverlay().isShowing(), true);
        instance.toggleOverlay(overlay, false);

        assertEquals(instance.getInfoOverlay().isShowing(), false);
    }


    /**
     * Test of addLayer method, of class MapView.
     */
    @Test
    public void testAddLayer() {
        System.out.println("addLayer");

        final MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        final MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        final MapView instance = Mockito.spy(new MapView(mapViewPane, mapDetails));

        final AbstractMapLayer layer = new DayNightLayer(instance, 0);

        instance.addLayer(layer);

        assertEquals(layer.isShowing(), true);
    }

    /**
     * Test of removeLayer method, of class MapView.
     */
    @Test
    public void testRemoveLayer() {
        System.out.println("removeLayer");

        final MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        final MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        final MapView instance = Mockito.spy(new MapView(mapViewPane, mapDetails));

        instance.removeLayer(0);

        assertEquals(instance.getLayers().isEmpty(), true);
    }

    /**
     * Test of updateShowingMarkers method, of class MapView.
     */
    @Test
    public void testUpdateShowingMarkers() {
        System.out.println("updateShowingMarkers");

        final MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        final MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        final MapView instance = Mockito.spy(new MapView(mapViewPane, mapDetails));

        final AbstractMarker.MarkerType type = AbstractMarker.MarkerType.POLYGON_MARKER;
        final boolean adding = true;

        instance.updateShowingMarkers(type, adding);

        assertEquals(instance.getMarkersShowing().contains(type), true);
    }


    /**
     * Test of getAllMarkers method, of class MapView.
     */
    @Test
    public void testGetAllMarkers() {
        System.out.println("getAllMarkers");

        final MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        final MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        final MapView instance = Mockito.spy(new MapView(mapViewPane, mapDetails));


        instance.getAllMarkers().clear();

        final PointMarker p1 = new PointMarker(instance, -999, 0, (double) -1, (double) -2, 0.05, 0, 0, "#000000");
        final PointMarker p2 = new PointMarker(instance, -101, 1, (double) -3, (double) -4, 0.05, 0, 0, "#000000");

        instance.addMarkerToHashMap("testCoord1", p1);
        instance.addMarkerToHashMap("testCoord2", p2);

        final Map result = instance.getAllMarkers();
        assertEquals(result.size(), 2);

    }

    /**
     * Test of getAllMarkersAsList method, of class MapView.
     */
    @Test
    public void testGetAllMarkersAsList() {
        System.out.println("getAllMarkersAsList");

        final MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        final MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        final MapView instance = Mockito.spy(new MapView(mapViewPane, mapDetails));

        instance.getAllMarkers().clear();
        instance.getUserCreatedMarkers().clear();

        final PointMarker p1 = new PointMarker(instance, -999, 0, (double) -1, (double) -2, 0.05, 0, 0, "#000000");
        final PointMarker p2 = new PointMarker(instance, -101, 1, (double) -3, (double) -4, 0.05, 0, 0, "#000000");

        instance.addMarkerToHashMap("testCoord1", p1);
        instance.addMarkerToHashMap("testCoord2", p2);

        final UserPointMarker usm = new UserPointMarker(instance, 45, 4, 4, 0.05, 4, 4);

        instance.getUserCreatedMarkers().add(usm);

        final List result = instance.getAllMarkersAsList();
        assertEquals(result.size(), 3);
    }

    /**
     * Test of clearQueriedMarkers method, of class MapView.
     */
    @Test
    public void testClearQueriedMarkers() {
        System.out.println("clearQueriedMarkers");

        final MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        final MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        final MapView instance = Mockito.spy(new MapView(mapViewPane, mapDetails));

        final PointMarker p2 = new PointMarker(instance, -107, 1, (double) -3, (double) -4, 0.05, 0, 0, "#000000");

        instance.addMarkerToHashMap("testCoord2", p2);

        final UserPointMarker usm = new UserPointMarker(instance, 46, 4, 4, 0.05, 4, 4);

        instance.getUserCreatedMarkers().add(usm);

        instance.clearQueriedMarkers();

        assertEquals(instance.getAllMarkers().size(), 0);
    }

    /**
     * Test of addMarkerIdToSelectedList method, of class MapView.
     */
    @Test
    public void testAddMarkerIdToSelectedList() {
        System.out.println("addMarkerIdToSelectedList");

        final MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        final MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        final MapView instance = Mockito.spy(new MapView(mapViewPane, mapDetails));

        final int markerID = -107;
        final List<Integer> selectedNodes = new ArrayList<Integer>();
        final boolean selectingVertex = true;

        final PointMarker p2 = new PointMarker(instance, markerID, 1, (double) -3, (double) -4, 0.05, 0, 0, "#000000");

        instance.addMarkerToHashMap("testCoord2", p2);

        instance.addMarkerIdToSelectedList(markerID, selectedNodes, selectingVertex);

        assertEquals(instance.getSelectedNodeList().size(), 1);
    }

    /**
     * Test of drawMarker method, of class MapView.
     */
    @Test
    public void testDrawMarker() {
        System.out.println("drawMarker");

        final MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        final MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        final MapView instance = Mockito.spy(new MapView(mapViewPane, mapDetails));

        final AbstractMarker marker = new PointMarker(instance, -32, 1, (double) -3, (double) -4, 0.05, 0, 0, "#000000");

        assertEquals(instance.getGraphMarkerGroup().getChildren().size(), 0);

        instance.drawMarker(marker);

        assertEquals(instance.getGraphMarkerGroup().getChildren().size(), 1);
    }

    /**
     * Test of addMarkerToHashMap method, of class MapView.
     */
    @Test
    public void testAddMarkerToHashMap() {
        System.out.println("addMarkerToHashMap");

        final MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        final MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        final MapView instance = Mockito.spy(new MapView(mapViewPane, mapDetails));

        final String key = "60,89";

        final AbstractMarker e = new PointMarker(instance, 32, 1, (double) -3, (double) -4, 0.05, 0, 0, "#000000");

        instance.addMarkerToHashMap(key, e);

        assertEquals(instance.getAllMarkers().containsKey(key), true);
    }

}
