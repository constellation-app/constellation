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
import au.gov.asd.tac.constellation.views.mapview2.markers.AbstractMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.ClusterMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.PointMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.UserPointMarker;
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

        PointMarker p1 = new PointMarker(instance, mapViewTopComponent.getNewMarkerID(), 0, (double) -1, (double) -2, 0.05, 0, 0, "#000000");
        PointMarker p2 = new PointMarker(instance, mapViewTopComponent.getNewMarkerID(), 1, (double) -3, (double) -4, 0.05, 0, 0, "#000000");

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
        String overlay = "";
        boolean show = false;
        MapView instance = null;
        instance.toggleOverlay(overlay, show);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getNewMarkerID method, of class MapView.
     */
    @Test
    public void testGetNewMarkerID() {
        System.out.println("getNewMarkerID");
        MapView instance = null;
        int expResult = 0;
        int result = instance.getNewMarkerID();
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addClusterMarkers method, of class MapView.
     */
    @Test
    public void testAddClusterMarkers() {
        System.out.println("addClusterMarkers");
        List<ClusterMarker> clusters = null;
        List<Text> clusterValues = null;
        MapView instance = null;
        instance.addClusterMarkers(clusters, clusterValues);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addLayer method, of class MapView.
     */
    @Test
    public void testAddLayer() {
        System.out.println("addLayer");
        AbstractMapLayer layer = null;
        MapView instance = null;
        instance.addLayer(layer);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of removeLayer method, of class MapView.
     */
    @Test
    public void testRemoveLayer() {
        System.out.println("removeLayer");
        int id = 0;
        MapView instance = null;
        instance.removeLayer(id);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of updateShowingMarkers method, of class MapView.
     */
    @Test
    public void testUpdateShowingMarkers() {
        System.out.println("updateShowingMarkers");
        AbstractMarker.MarkerType type = null;
        boolean adding = false;
        MapView instance = null;
        instance.updateShowingMarkers(type, adding);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of redrawQueriedMarkers method, of class MapView.
     */
    @Test
    public void testRedrawQueriedMarkers() {
        System.out.println("redrawQueriedMarkers");
        MapView instance = null;
        instance.redrawQueriedMarkers();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of reScaleQueriedMarkers method, of class MapView.
     */
    @Test
    public void testReScaleQueriedMarkers() {
        System.out.println("reScaleQueriedMarkers");
        double scale = 0.0;
        MapView instance = null;
        instance.reScaleQueriedMarkers(scale);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAllMarkers method, of class MapView.
     */
    @Test
    public void testGetAllMarkers() {
        System.out.println("getAllMarkers");
        MapView instance = null;
        Map expResult = null;
        Map result = instance.getAllMarkers();
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAllMarkersAsList method, of class MapView.
     */
    @Test
    public void testGetAllMarkersAsList() {
        System.out.println("getAllMarkersAsList");
        MapView instance = null;
        List expResult = null;
        List result = instance.getAllMarkersAsList();
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of clearQueriedMarkers method, of class MapView.
     */
    @Test
    public void testClearQueriedMarkers() {
        System.out.println("clearQueriedMarkers");
        MapView instance = null;
        instance.clearQueriedMarkers();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of clearAll method, of class MapView.
     */
    @Test
    public void testClearAll() {
        System.out.println("clearAll");
        MapView instance = null;
        instance.clearAll();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCurrentGraph method, of class MapView.
     */
    @Test
    public void testGetCurrentGraph() {
        System.out.println("getCurrentGraph");
        MapView instance = null;
        Graph expResult = null;
        Graph result = instance.getCurrentGraph();
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addMarkerIdToSelectedList method, of class MapView.
     */
    @Test
    public void testAddMarkerIdToSelectedList() {
        System.out.println("addMarkerIdToSelectedList");
        int markerID = 0;
        List<Integer> selectedNodes = null;
        boolean selectingVertex = false;
        MapView instance = null;
        instance.addMarkerIdToSelectedList(markerID, selectedNodes, selectingVertex);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of panToAll method, of class MapView.
     */
    @Test
    public void testPanToAll() {
        System.out.println("panToAll");
        MapView instance = null;
        instance.panToAll();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of panToCenter method, of class MapView.
     */
    @Test
    public void testPanToCenter() {
        System.out.println("panToCenter");
        MapView instance = null;
        instance.panToCenter();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of panToSelection method, of class MapView.
     */
    @Test
    public void testPanToSelection() {
        System.out.println("panToSelection");
        MapView instance = null;
        instance.panToSelection();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of zoom method, of class MapView.
     */
    @Test
    public void testZoom() {
        System.out.println("zoom");
        double x = 0.0;
        double y = 0.0;
        boolean allMarkers = false;
        MapView instance = null;
        instance.zoom(x, y, allMarkers);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of generateZoomLocationUI method, of class MapView.
     */
    @Test
    public void testGenerateZoomLocationUI() {
        System.out.println("generateZoomLocationUI");
        MapView instance = null;
        instance.generateZoomLocationUI();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getPointMarkersOnMap method, of class MapView.
     */
    @Test
    public void testGetPointMarkersOnMap() {
        System.out.println("getPointMarkersOnMap");
        MapView instance = null;
        ObservableList expResult = null;
        ObservableList result = instance.getPointMarkersOnMap();
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMarkerTextProperty method, of class MapView.
     */
    @Test
    public void testGetMarkerTextProperty() {
        System.out.println("getMarkerTextProperty");
        MapView instance = null;
        StringProperty expResult = null;
        StringProperty result = instance.getMarkerTextProperty();
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of drawMarker method, of class MapView.
     */
    @Test
    public void testDrawMarker() {
        System.out.println("drawMarker");
        AbstractMarker marker = null;
        MapView instance = null;
        instance.drawMarker(marker);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addMarkerToHashMap method, of class MapView.
     */
    @Test
    public void testAddMarkerToHashMap() {
        System.out.println("addMarkerToHashMap");
        String key = "";
        AbstractMarker e = null;
        MapView instance = null;
        instance.addMarkerToHashMap(key, e);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
