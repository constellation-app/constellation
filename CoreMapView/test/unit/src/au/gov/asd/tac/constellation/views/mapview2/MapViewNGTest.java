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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.views.mapview2.layers.AbstractMapLayer;
import au.gov.asd.tac.constellation.views.mapview2.layers.DayNightLayer;
import au.gov.asd.tac.constellation.views.mapview2.markers.AbstractMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.ClusterMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.PointMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.UserPointMarker;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.text.Text;
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
public class MapViewNGTest {

    private final MapViewTopComponent mapViewTopComponent;

    public MapViewNGTest() {
        mapViewTopComponent = new MapViewTopComponent();
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
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
        MapView instance = mapViewTopComponent.mapViewPane.getMap();

        String coordinateKey = "-1" + "," + "-2";
        String coordinateKey2 = "-3" + "," + "-4";

        PointMarker p1 = new PointMarker(instance, -99, 0, (double) -1, (double) -2, 0.05, 0, 0, "#000000");
        PointMarker p2 = new PointMarker(instance, -100, 1, (double) -3, (double) -4, 0.05, 0, 0, "#000000");

        mapViewTopComponent.addMarker(coordinateKey, p1);
        mapViewTopComponent.addMarker(coordinateKey2, p2);

        p1.select();
        p2.select();

        assertEquals(p1.getMarkerSelected() && p2.getMarkerSelected(), true);

        instance.deselectAllMarkers();

        assertEquals(!p1.getMarkerSelected() && !p2.getMarkerSelected(), true);
    }

    /**
     * Test of getMarkerColourProperty method, of class MapView.
     */
    @Test
    public void testGetMarkerColourProperty() {
        System.out.println("getMarkerColourProperty");
        MapView instance = mapViewTopComponent.mapViewPane.getMap();
        String expResult = mapViewTopComponent.mapViewPane.DEFAULT_COLOURS;
        StringProperty result = instance.getMarkerColourProperty();


        assertEquals(result.get(), expResult);
    }

    /**
     * Test of removeUserMarker method, of class MapView.
     */
    @Test
    public void testRemoveUserMarker() {
        System.out.println("removeUserMarker");
        int id = 1;
        MapView instance = mapViewTopComponent.mapViewPane.getMap();
        instance.getUserMarkers().clear();
        UserPointMarker usm = new UserPointMarker(instance, id, 4, 4, 0.05, 4, 4);

        instance.getUserMarkers().add(usm);

        instance.removeUserMarker(id);

        assertEquals(instance.getUserMarkers().isEmpty(), true);
    }

    /**
     * Test of toggleOverlay method, of class MapView.
     */
    @Test
    public void testToggleOverlay() {
        System.out.println("toggleOverlay");
        String overlay = mapViewTopComponent.mapViewPane.INFO_OVERLAY;

        MapView instance = mapViewTopComponent.mapViewPane.getMap();
        instance.toggleOverlay(overlay, true);

        assertEquals(MapView.infoOverlay.getIsShowing(), true);
        instance.toggleOverlay(overlay, false);

        assertEquals(MapView.infoOverlay.getIsShowing(), false);
    }

    /**
     * Test of getNewMarkerID method, of class MapView.
     */
    @Test
    public void testGetNewMarkerID() {
        System.out.println("getNewMarkerID");
        MapView instance = mapViewTopComponent.mapViewPane.getMap();

        int result = instance.getNewMarkerID();
        assertEquals(result, 1);

        result = instance.getNewMarkerID();
        assertEquals(result, 2);


    }


    /**
     * Test of addLayer method, of class MapView.
     */
    @Test
    public void testAddLayer() {
        System.out.println("addLayer");
        MapView instance = mapViewTopComponent.mapViewPane.getMap();
        AbstractMapLayer layer = new DayNightLayer(instance, 0);

        instance.addLayer(layer);

        assertEquals(layer.getIsShowing(), true);
    }

    /**
     * Test of removeLayer method, of class MapView.
     */
    @Test
    public void testRemoveLayer() {
        System.out.println("removeLayer");
        int id = 0;
        MapView instance = mapViewTopComponent.mapViewPane.getMap();
        instance.removeLayer(id);

        assertEquals(instance.getLayers().isEmpty(), true);
    }

    /**
     * Test of updateShowingMarkers method, of class MapView.
     */
    @Test
    public void testUpdateShowingMarkers() {
        System.out.println("updateShowingMarkers");
        AbstractMarker.MarkerType type = AbstractMarker.MarkerType.POLYGON_MARKER;
        boolean adding = true;
        MapView instance = mapViewTopComponent.mapViewPane.getMap();
        instance.updateShowingMarkers(type, adding);

        assertEquals(instance.getMarkersShowing().contains(type), true);
    }

    /**
     * Test of redrawQueriedMarkers method, of class MapView.
     */
    /*@Test
    public void testRedrawQueriedMarkers() {
        System.out.println("redrawQueriedMarkers");
        MapView instance = null;
        instance.redrawQueriedMarkers();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/

    /**
     * Test of reScaleQueriedMarkers method, of class MapView.
     */
    /*@Test
    public void testReScaleQueriedMarkers() {
        System.out.println("reScaleQueriedMarkers");
        double scale = 0.0;
        MapView instance = null;
        instance.reScaleQueriedMarkers(scale);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/

    /**
     * Test of getAllMarkers method, of class MapView.
     */
    @Test
    public void testGetAllMarkers() {
        System.out.println("getAllMarkers");
        MapView instance = mapViewTopComponent.mapViewPane.getMap();

        instance.getAllMarkers().clear();

        PointMarker p1 = new PointMarker(instance, -999, 0, (double) -1, (double) -2, 0.05, 0, 0, "#000000");
        PointMarker p2 = new PointMarker(instance, -101, 1, (double) -3, (double) -4, 0.05, 0, 0, "#000000");

        mapViewTopComponent.addMarker("testCoord1", p1);
        mapViewTopComponent.addMarker("testCoord2", p2);

        Map result = instance.getAllMarkers();
        assertEquals(result.size(), 2);

    }

    /**
     * Test of getAllMarkersAsList method, of class MapView.
     */
    @Test
    public void testGetAllMarkersAsList() {
        System.out.println("getAllMarkersAsList");
        MapView instance = mapViewTopComponent.mapViewPane.getMap();
        instance.getAllMarkers().clear();
        instance.getUserMarkers().clear();

        PointMarker p1 = new PointMarker(instance, -999, 0, (double) -1, (double) -2, 0.05, 0, 0, "#000000");
        PointMarker p2 = new PointMarker(instance, -101, 1, (double) -3, (double) -4, 0.05, 0, 0, "#000000");

        mapViewTopComponent.addMarker("testCoord1", p1);
        mapViewTopComponent.addMarker("testCoord2", p2);

        UserPointMarker usm = new UserPointMarker(instance, 45, 4, 4, 0.05, 4, 4);

        instance.getUserMarkers().add(usm);

        List result = instance.getAllMarkersAsList();
        assertEquals(result.size(), 3);
    }

    /**
     * Test of clearQueriedMarkers method, of class MapView.
     */
    @Test
    public void testClearQueriedMarkers() {
        System.out.println("clearQueriedMarkers");
        MapView instance = mapViewTopComponent.mapViewPane.getMap();

        PointMarker p2 = new PointMarker(instance, -107, 1, (double) -3, (double) -4, 0.05, 0, 0, "#000000");

        mapViewTopComponent.addMarker("testCoord2", p2);

        UserPointMarker usm = new UserPointMarker(instance, 46, 4, 4, 0.05, 4, 4);

        instance.getUserMarkers().add(usm);

        instance.clearQueriedMarkers();

        assertEquals(instance.getAllMarkers().size(), 0);
    }

    /**
     * Test of clearAll method, of class MapView.
     */
    @Test
    public void testClearAll() {
        System.out.println("clearAll");
        MapView instance = mapViewTopComponent.mapViewPane.getMap();
        instance.clearAll();

        assertEquals(instance.getAllMarkers().isEmpty(), true);
        assertEquals(instance.getUserMarkers().isEmpty(), true);
    }


    /**
     * Test of addMarkerIdToSelectedList method, of class MapView.
     */
    @Test
    public void testAddMarkerIdToSelectedList() {
        System.out.println("addMarkerIdToSelectedList");
        int markerID = -107;
        List<Integer> selectedNodes = new ArrayList<Integer>();
        boolean selectingVertex = true;

        MapView instance = mapViewTopComponent.mapViewPane.getMap();

        PointMarker p2 = new PointMarker(instance, markerID, 1, (double) -3, (double) -4, 0.05, 0, 0, "#000000");

        mapViewTopComponent.addMarker("testCoord2", p2);

        instance.addMarkerIdToSelectedList(markerID, selectedNodes, selectingVertex);

        assertEquals(instance.getSelectedNodeList().size(), 1);
    }

    /**
     * Test of drawMarker method, of class MapView.
     */
    @Test
    public void testDrawMarker() {
        System.out.println("drawMarker");
        MapView instance = mapViewTopComponent.mapViewPane.getMap();
        AbstractMarker marker = new PointMarker(instance, -32, 1, (double) -3, (double) -4, 0.05, 0, 0, "#000000");

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
        String key = "60,89";
        MapView instance = mapViewTopComponent.mapViewPane.getMap();
        AbstractMarker e = new PointMarker(instance, 32, 1, (double) -3, (double) -4, 0.05, 0, 0, "#000000");

        instance.addMarkerToHashMap(key, e);

        assertEquals(instance.getAllMarkers().containsKey(key), true);
    }

}
