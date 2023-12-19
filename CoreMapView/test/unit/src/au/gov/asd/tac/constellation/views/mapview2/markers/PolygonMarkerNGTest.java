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

import au.gov.asd.tac.constellation.utilities.file.ConstellationInstalledFileLocator;
import au.gov.asd.tac.constellation.views.mapview2.MapDetails;
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
public class PolygonMarkerNGTest {

    private static final Logger LOGGER = Logger.getLogger(PolygonMarkerNGTest.class.getName());

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
     * Test of addNewLine method, of class PolygonMarker.
     */
    @Test
    public void testAddNewLine() {
        System.out.println("addNewLine");

        final MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        final MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        final MapView map = Mockito.spy(new MapView(mapViewPane, mapDetails));

        final double prevLineEndX = 54;
        final double prevLineEndY = 54;
        final PolygonMarker instance = new PolygonMarker(map, 65);

        final Line result = instance.addNewLine(prevLineEndX, prevLineEndY);

        assertEquals((result.getEndX() == prevLineEndX) && (result.getEndY() == prevLineEndY), true);

    }

    /**
     * Test of endDrawing method, of class PolygonMarker.
     */
    @Test
    public void testEndDrawing() {
        System.out.println("endDrawing");
        final MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        final MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        final MapView map = Mockito.spy(new MapView(mapViewPane, mapDetails));
        final PolygonMarker instance = new PolygonMarker(map, 65);
        instance.endDrawing();

        assertEquals(instance.getCurrentLine() == null, true);

    }

    /**
     * Test of setEnd method, of class PolygonMarker.
     */
    @Test
    public void testSetEnd() {
        System.out.println("setEnd");
        final MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        final MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        final MapView map = Mockito.spy(new MapView(mapViewPane, mapDetails));

        final double xNewLine = 3;
        final double yNewLine = 3;

        final double x = 23;
        final double y = 23;
        final PolygonMarker instance = new PolygonMarker(map, 65);

        instance.addNewLine(xNewLine, yNewLine);

        instance.setEnd(x, y);

        final Line result = instance.getCurrentLine();

        assertEquals((result.getEndX() == x) && (result.getEndY() == y), true);

    }

    /**
     * Test of generatePath method, of class PolygonMarker.
     */
    @Test
    public void testGeneratePath() {
        System.out.println("generatePath");
        final MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        final MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        final MapView map = Mockito.spy(new MapView(mapViewPane, mapDetails));
        final PolygonMarker instance = new PolygonMarker(map, 65);

        instance.addNewLine(640.5983769798281, 654.7539097213746);
        instance.addNewLine(707.6983769798281, 702.0539097213747);
        instance.addNewLine(738.4983769798281, 615.1539097213747);

        instance.generatePath();

        final String path = "M640.5983769798281,654.7539097213746L707.6983769798281,702.0539097213747L738.4983769798281,615.1539097213747L640.5983769798281,654.7539097213746";

        assertEquals(path.equals(instance.getRawPath()), true);

    }


}
