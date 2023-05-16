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
import javafx.scene.shape.Line;
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
public class CircleMarkerNGTest {

    private static final Logger LOGGER = Logger.getLogger(CircleMarkerNGTest.class.getName());

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
     * Test of getCenterX method, of class CircleMarker.
     */
    @Test
    public void testGetCenterX() {
        System.out.println("getCenterX");

        final MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        final MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        final MapView map = Mockito.spy(new MapView(mapViewPane));

        final CircleMarker instance = new CircleMarker(map, 0, 45, 45, 76, 0, 0);
        final double expResult = 45;
        final double result = instance.getCenterX();
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of getCenterY method, of class CircleMarker.
     */
    @Test
    public void testGetCenterY() {
        System.out.println("getCenterY");
        final MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        final MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        final MapView map = Mockito.spy(new MapView(mapViewPane));

        final CircleMarker instance = new CircleMarker(map, 0, 45, 45, 76, 0, 0);
        final double expResult = 45;
        final double result = instance.getCenterY();
        assertEquals(result, expResult, 0.0);
    }


    /**
     * Test of getRadius method, of class CircleMarker.
     */
    @Test
    public void testGetRadius() {
        System.out.println("getRadius");
        final MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        final MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        final MapView map = Mockito.spy(new MapView(mapViewPane));

        final CircleMarker instance = new CircleMarker(map, 0, 45, 45, 76, 0, 0);
        final double expResult = 76;
        final double result = instance.getRadius();
        assertEquals(result, expResult, 0.0);
    }


    /**
     * Test of setRadius method, of class CircleMarker.
     */
    @Test
    public void testSetRadius_double() {
        System.out.println("setRadius");

        final MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        final MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        final MapView map = Mockito.spy(new MapView(mapViewPane));

        final double radius = 54;
        final CircleMarker instance = new CircleMarker(map, 0, 45, 45, 76, 0, 0);
        final double expResult = 54;

        instance.setRadius(radius);
        final double result = instance.getRadius();

        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of setLineEnd method, of class CircleMarker.
     */
    @Test
    public void testSetLineEnd() {
        System.out.println("setLineEnd");

        final MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        final MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        final MapView map = Mockito.spy(new MapView(mapViewPane));

        final double x = 10.0;
        final double y = 50.0;
        final CircleMarker instance = new CircleMarker(map, 0, 45, 45, 76, 0, 0);
        instance.setLineEnd(x, y);

        final Line line = instance.getUILine();

        assertEquals((line.getEndX() == x) && (line.getEndY() == y), true);
    }

}
