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
package au.gov.asd.tac.constellation.views.mapview2.markers;

import au.gov.asd.tac.constellation.views.mapview2.MapView;
import au.gov.asd.tac.constellation.views.mapview2.MapViewPane;
import au.gov.asd.tac.constellation.views.mapview2.MapViewTopComponent;
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
public class PointMarkerNGTest {

    private static final Logger LOGGER = Logger.getLogger(PointMarkerNGTest.class.getName());


    public PointMarkerNGTest() {

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
     * Test of changeMarkerColour method, of class PointMarker.
     */
    @Test
    public void testChangeMarkerColour() {

        System.out.println("changeMarkerColour");

        MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        MapView parent = Mockito.spy(new MapView(mapViewPane));

        String option = MapViewPane.DEFAULT_COLOURS;
        PointMarker instance = new PointMarker(parent, -99, -99, 0, 0, 0.05, 0, 0, "#ffffff");

        // Test default Colour
        instance.changeMarkerColour(option);
        assertEquals(instance.getCurrentColour(), instance.getDefaultColour());

        instance.changeMarkerColour(MapViewPane.USE_COLOUR_ATTR);
        assertEquals(instance.getCurrentColour(), "#ffffff");

        instance.setBlazeColour("Yellow;#cccccc");
        instance.changeMarkerColour(MapViewPane.USE_BLAZE_COL);
        assertEquals(instance.getCurrentColour(), "#cccccc");

        instance.setBlazeColour("Yellow;#gggggg");
        instance.changeMarkerColour(MapViewPane.USE_BLAZE_COL);
        assertEquals(instance.getCurrentColour(), "#D3D3D3");

        instance.setOverlayColour("#990a0a");
        instance.changeMarkerColour(MapViewPane.USE_OVERLAY_COL);
        assertEquals(instance.getCurrentColour(), "#990a0a");

        /*instance.setOverlayColour("#990a0a");
        instance.changeMarkerColour(MapViewPane.USE_OVERLAY_COL);
        assertEquals(instance.getCurrentColour(), "#D3D3D3");*/


    }

    /**
     * Test of getLattitude method, of class PointMarker.
     */
    @Test
    public void testGetLattitude() {
        System.out.println("getLattitude");

        MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        MapView parent = Mockito.spy(new MapView(mapViewPane));

        PointMarker instance = new PointMarker(parent, -99, -99, 108, 56, 0.05, 0, 0, "#ffffff");
        double expResult = 108;
        double result = instance.getLattitude();
        assertEquals(result, expResult, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getLongitude method, of class PointMarker.
     */
    @Test
    public void testGetLongitude() {
        System.out.println("getLongitude");

        MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        MapView parent = Mockito.spy(new MapView(mapViewPane));

        PointMarker instance = new PointMarker(parent, -99, -99, 108, 56, 0.05, 0, 0, "#ffffff");
        double expResult = 56;
        double result = instance.getLongitude();
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of getScale method, of class PointMarker.
     */
    @Test
    public void testGetScale() {
        System.out.println("getScale");

        MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        MapView parent = Mockito.spy(new MapView(mapViewPane));

        PointMarker instance = new PointMarker(parent, -99, -99, 108, 56, 0.05, 0, 0, "#ffffff");
        double expResult = 0.05;
        double result = instance.getScale();
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of getPath method, of class PointMarker.
     */
    @Test
    public void testGetPath() {
        System.out.println("getPath");

        MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        MapView parent = Mockito.spy(new MapView(mapViewPane));

        PointMarker instance = new PointMarker(parent, -99, -99, 108, 56, 0.05, 0, 0, "#ffffff");
        String expResult = "c-20.89-55.27-83.59-81.74-137-57.59-53.88,24.61-75.7,87.77-47.83,140.71,12.54,23.69,26.47,46.44,39.93,70.12,15.79,27.4,32,55.27,50.16,87.31a101.37,101.37,0,0,1,4.65-9.76c27.86-49.23,56.66-98,84-147.68,14.86-26,16.72-54.8,6-83.12z";
        String result = instance.getPath();
        assertEquals(result, expResult);
    }

    /**
     * Test of setMarkerPosition method, of class PointMarker.
     */
    /*@Test
    public void testSetMarkerPosition() {
        System.out.println("setMarkerPosition");
        double mapWidth = 0.0;
        double mapHeight = 0.0;
        PointMarker instance = null;
        instance.setMarkerPosition(mapWidth, mapHeight);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/

    /**
     * Test of getBlazeColour method, of class PointMarker.
     */
    @Test
    public void testGetBlazeColour() {
        System.out.println("getBlazeColour");

        MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        MapView parent = Mockito.spy(new MapView(mapViewPane));

        PointMarker instance = new PointMarker(parent, -99, -99, 108, 56, 0.05, 0, 0, "#ffffff");
        instance.setBlazeColour("Green;#gggggg");
        String expResult = "#gggggg";
        String result = instance.getBlazeColour();
        assertEquals(result, expResult);
    }

    /**
     * Test of getX method, of class PointMarker.
     */
    @Test
    public void testGetX() {
        System.out.println("getX");

        MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        MapView parent = Mockito.spy(new MapView(mapViewPane));

        PointMarker instance = new PointMarker(parent, -99, -99, 83.63001, -169.1110266, 0.05, 0, 0, "#ffffff");
        instance.setMarkerPosition(MapView.mapWidth, MapView.mapHeight);
        double expResult = 0.0;
        double result = instance.getX();
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of getY method, of class PointMarker.
     */
    @Test
    public void testGetY() {
        System.out.println("getY");

        MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        MapView parent = Mockito.spy(new MapView(mapViewPane));

        PointMarker instance = new PointMarker(parent, -99, -99, 83.63001, -169.1110266, 0.05, 0, 0, "#ffffff");
        double expResult = 0.0;
        double result = instance.getY();
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of setLabelAttr method, of class PointMarker.
     */
    @Test
    public void testSetLabelAttr() {
        System.out.println("setLabelAttr");

        MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        MapView parent = Mockito.spy(new MapView(mapViewPane));

        String labelAttribute = "Example Label";
        PointMarker instance = new PointMarker(parent, -99, -99, 83.63001, -169.1110266, 0.05, 0, 0, "#ffffff");
        instance.setLabelAttr(labelAttribute);
        assertEquals(instance.getLabelAttr(), "Example Label");

        instance.setLabelAttr("Another example");
        assertEquals(instance.getLabelAttr(), "<Multiple Values>");

    }

    /**
     * Test of setIdentAttr method, of class PointMarker.
     */
    @Test
    public void testSetIdentAttr() {
        System.out.println("setIdentAttr");

        MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        MapView parent = Mockito.spy(new MapView(mapViewPane));

        String identAttribute = "Example Identifier";
        PointMarker instance = new PointMarker(parent, -99, -99, 83.63001, -169.1110266, 0.05, 0, 0, "#ffffff");
        instance.setIdentAttr(identAttribute);
        assertEquals(instance.getIdentAttr(), "Example Identifier");

        instance.setIdentAttr("Example Identifier");
        assertEquals(instance.getIdentAttr(), "<Multiple Values>");
    }

    /**
     * Test of getIdentAttr method, of class PointMarker.
     */
    /*@Test
    public void testGetIdentAttr() {
        System.out.println("getIdentAttr");
        PointMarker instance = null;
        String expResult = "";
        String result = instance.getIdentAttr();
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/

}
