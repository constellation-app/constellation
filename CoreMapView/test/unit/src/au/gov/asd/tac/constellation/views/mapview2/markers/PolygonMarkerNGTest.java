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
import au.gov.asd.tac.constellation.views.mapview2.MapViewTopComponent;
import javafx.scene.shape.Line;
import javafx.scene.shape.SVGPath;
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
public class PolygonMarkerNGTest {

    private MapViewTopComponent component = new MapViewTopComponent();
    private MapView map = component.mapViewPane.getMap();

    public PolygonMarkerNGTest() {
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
     * Test of addNewLine method, of class PolygonMarker.
     */
    @Test
    public void testAddNewLine() {
        System.out.println("addNewLine");
        double prevLineEndX = 54;
        double prevLineEndY = 54;
        PolygonMarker instance = new PolygonMarker(map, 65, 0, 0);

        Line result = instance.addNewLine(prevLineEndX, prevLineEndY);

        assertEquals((result.getEndX() == prevLineEndX) && (result.getEndY() == prevLineEndY), true);

    }

    /**
     * Test of endDrawing method, of class PolygonMarker.
     */
    @Test
    public void testEndDrawing() {
        System.out.println("endDrawing");
        PolygonMarker instance = new PolygonMarker(map, 65, 0, 0);
        instance.endDrawing();

        assertEquals(instance.getCurrentLine() == null, true);

    }

    /**
     * Test of setEnd method, of class PolygonMarker.
     */
    @Test
    public void testSetEnd() {
        System.out.println("setEnd");

        double xNewLine = 3;
        double yNewLine = 3;

        double x = 23;
        double y = 23;
        PolygonMarker instance = new PolygonMarker(map, 65, 0, 0);

        instance.addNewLine(xNewLine, yNewLine);

        instance.setEnd(x, y);

        Line result = instance.getCurrentLine();

        assertEquals((result.getEndX() == x) && (result.getEndY() == y), true);

    }

    /**
     * Test of generatePath method, of class PolygonMarker.
     */
    @Test
    public void testGeneratePath() {
        System.out.println("generatePath");
        PolygonMarker instance = new PolygonMarker(map, 65, 0, 0);

        instance.addNewLine(640.5983769798281, 654.7539097213746);
        instance.addNewLine(707.6983769798281, 702.0539097213747);
        instance.addNewLine(738.4983769798281, 615.1539097213747);
        //instance.addNewLine(595.9868734233639, 589.9531776579943);

        instance.generatePath();

        String path = "M640.5983769798281,654.7539097213746L707.6983769798281,702.0539097213747L738.4983769798281,615.1539097213747L640.5983769798281,654.7539097213746";

        assertEquals(path.equals(instance.getRawPath()), true);

    }


}
