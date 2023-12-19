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

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.file.ConstellationInstalledFileLocator;
import au.gov.asd.tac.constellation.views.mapview2.MapDetails;
import au.gov.asd.tac.constellation.views.mapview2.MapView;
import au.gov.asd.tac.constellation.views.mapview2.MapViewPane;
import au.gov.asd.tac.constellation.views.mapview2.MapViewTopComponent;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mockito.Mockito;
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
public class PointMarkerNGTest {

    private static final Logger LOGGER = Logger.getLogger(PointMarkerNGTest.class.getName());

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
     * Test of changeMarkerColour method, of class PointMarker.
     */
    @Test
    public void testChangeMarkerColour() {

        System.out.println("changeMarkerColour");

        final MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        final MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        final MapView parent = Mockito.spy(new MapView(mapViewPane, mapDetails));

        final String option = MapViewPane.DEFAULT_COLOURS;
        final PointMarker instance = new PointMarker(parent, -99, -99, 0, 0, ConstellationColor.WHITE);

        // Test default Colour
        instance.changeMarkerColour(option);
        assertEquals(instance.getCurrentColour(), MapDetails.MARKER_DEFAULT_FILL_COLOUR);
    }

    /**
     * Test of getLattitude method, of class PointMarker.
     */
    @Test
    public void testGetLattitude() {
        System.out.println("getLattitude");

        final MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        final MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        final MapView parent = Mockito.spy(new MapView(mapViewPane, mapDetails));

        final PointMarker instance = new PointMarker(parent, -99, -99, 108, 56, ConstellationColor.WHITE); 
        final double expResult = 108;
        final double result = instance.getLattitude();
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of getLongitude method, of class PointMarker.
     */
    @Test
    public void testGetLongitude() {
        System.out.println("getLongitude");

        final MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        final MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        final MapView parent = Mockito.spy(new MapView(mapViewPane, mapDetails));

        final PointMarker instance = new PointMarker(parent, -99, -99, 108, 56, ConstellationColor.WHITE);
        final double expResult = 56;
        final double result = instance.getLongitude();
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of getScale method, of class PointMarker.
     */
    @Test
    public void testGetScale() {
        System.out.println("getScale");

        final MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        final MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        final MapView parent = Mockito.spy(new MapView(mapViewPane, mapDetails));
        parent.zoomIn();
        parent.zoomIn();
        parent.zoomIn();

        final PointMarker instance = new PointMarker(parent, -99, -99, 108, 56, ConstellationColor.WHITE);
        final double expResult = 0.7513148009015775;
        final double result = instance.getScalingFactor();
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of getPath method, of class PointMarker.
     */
    @Test
    public void testGetPath() {
        System.out.println("getPath");

        final MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        final MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        final MapView parent = Mockito.spy(new MapView(mapViewPane, mapDetails));

        final PointMarker instance = new PointMarker(parent, -99, -99, 108, 56, ConstellationColor.WHITE);
        final String expResult = "l-35-90 l-45-80 l-10-30 l0-45 l10-25 l15-20 l50-20 l30 0 l50 20 l15 20 l10 25 l0 45 l-10 30 l-45 80 l-35 90 m0-194 l-22-22 l22-22 l22 22 l-22 22 m0-8 l-14-14 l14-14 l14 14 l-14 14 m0-8 l-6-6 l6-6 l6 6 l-6 6";
        final String result = instance.getPath();
        assertEquals(result, expResult);
    }

    /**
     * Test of getBlazeColour method, of class PointMarker.
     */
    @Test
    public void testGetBlazeColour() {
        System.out.println("getBlazeColour");

        final MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        final MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        final MapView parent = Mockito.spy(new MapView(mapViewPane, mapDetails));

        final PointMarker instance = new PointMarker(parent, -99, -99, 108, 56, ConstellationColor.WHITE);
        instance.setBlazeColour("Green;#gggggg");
        final String expResult = "#gggggg";
        final String result = instance.getBlazeColour();
        assertEquals(result, expResult);
    }

    /**
     * Test of getX method, of class PointMarker.
     */
    @Test
    public void testGetX() {
        System.out.println("getX");

        final MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        final MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        final MapView parent = Mockito.spy(new MapView(mapViewPane, mapDetails));

        final PointMarker instance = new PointMarker(parent, -99, -99, 85.0511, -180, ConstellationColor.WHITE);
        instance.setMarkerPosition(MapView.MAP_VIEWPORT_WIDTH, MapView.MAP_VIEWPORT_HEIGHT);
        final double expResult = 0.0;
        final double result = instance.getX();
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of getY method, of class PointMarker.
     */
    @Test
    public void testGetY() {
        System.out.println("getY");

        final MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        final MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        final MapView parent = Mockito.spy(new MapView(mapViewPane, mapDetails));

        final PointMarker instance = new PointMarker(parent, -99, -99, 83.63001, -169.1110266, ConstellationColor.WHITE);
        final double expResult = 0.0;
        final double result = instance.getY();
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of setLabelAttr method, of class PointMarker.
     */
    @Test
    public void testSetLabelAttr() {
        System.out.println("setLabelAttr");

        final MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        final MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        final MapView parent = Mockito.spy(new MapView(mapViewPane, mapDetails));

        final String labelAttribute = "Example Label";
        final PointMarker instance = new PointMarker(parent, -99, -99, 83.63001, -169.1110266, ConstellationColor.WHITE);
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

        final MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        final MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        final MapView parent = Mockito.spy(new MapView(mapViewPane, mapDetails));

        final String identAttribute = "Example Identifier";
        final PointMarker instance = new PointMarker(parent, -99, -99, 83.63001, -169.1110266, ConstellationColor.WHITE);
        instance.setIdentAttr(identAttribute);
        assertEquals(instance.getIdentAttr(), "Example Identifier");

        instance.setIdentAttr("Example Identifier");
        assertEquals(instance.getIdentAttr(), "<Multiple Values>");
    }

    /**
     * Test of getIdentAttr method, of class PointMarker.
     */
    @Test
    public void testGetIdentAttr() {        
        System.out.println("getIdentAttr");

        final MapView parent = Mockito.mock(MapView.class);
        final PointMarker instance = new PointMarker(parent, -99, -99, 83.63001, -169.1110266, ConstellationColor.WHITE);

        String expResult = "TestIdentity";

        instance.setIdentAttr("TestIdentity");
        String result = instance.getIdentAttr();

        assertEquals(result, expResult);

        instance.setIdentAttr("TestIdentity2");
        expResult = "<Multiple Values>";
        result = instance.getIdentAttr();

        assertEquals(result, expResult);
        
    }

}
